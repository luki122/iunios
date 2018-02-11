
package com.mediatek.contacts.list.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import android.util.Log;

import com.android.contacts.vcard.ProcessorBase;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.list.ContactsMultiDeletionFragment;
// Aurora xuyong 2015-07-13 added for bug #14161 start
import com.mediatek.contacts.simcontact.SimCardUtils;
// Aurora xuyong 2015-07-13 added for bug #14161 end

import gionee.provider.GnTelephony.SIMInfo;
import android.telephony.TelephonyManager;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.os.PowerManager;
import android.os.Process;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import android.os.RemoteException;
import android.content.Context;
//import com.android.internal.telephony.gemini.GeminiPhone;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.database.Cursor;
// Gionee:wangth add for CR00576696 begin
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
// Gionee:wangth add for CR00576696 end
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnITelephony;
import android.os.Build;

public class DeleteProcessor extends ProcessorBase {

    private static final String LOG_TAG = ContactsMultiDeletionFragment.TAG;
    private static final boolean DEBUG = ContactsMultiDeletionFragment.DEBUG;

    private final MultiChoiceService mService;
    private final ContentResolver mResolver;
    private final List<MultiChoiceRequest> mRequests;
    private final int mJobId;
    private final MultiChoiceHandlerListener mListener;

    private PowerManager.WakeLock mWakeLock;

    private volatile boolean mCanceled = false;
    private volatile boolean mDone = false;
    private volatile boolean mIsRunning = false;

    //Gionee <wangth><2013-04-24> modify for CR00790944 begin
    /*
    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 500;
    */
    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 50;
    //Gionee <wangth><2013-04-24> modify for CR00790944 end

    public DeleteProcessor(final MultiChoiceService service,
            final MultiChoiceHandlerListener listener, final List<MultiChoiceRequest> requests,
            final int jobId) {
        mService = service;
        mResolver = mService.getContentResolver();
        mListener = listener;

        mRequests = requests;
        mJobId = jobId;

        final PowerManager powerManager = (PowerManager) mService.getApplicationContext()
                .getSystemService("power");
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (DEBUG)
            Log.d(LOG_TAG, "DeleteProcessor received cancel request");
        if (mDone || mCanceled) {
            return false;
        }
        Log.i(LOG_TAG,"[cancel]!mIsRunning : "+!mIsRunning);
        
        mCanceled = true;
        if (!mIsRunning) {
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_DELETE, mJobId, -1, -1, -1);
        }
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     xxx
         *   CR ID: ALPS00249590
         *   Descriptions: 
         */
        else {
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceling(MultiChoiceService.TYPE_DELETE, mJobId);
        }
        /*
         * Bug Fix by Mediatek End.
         */
        return true;
    }

    @Override
    public int getType() {
        return MultiChoiceService.TYPE_DELETE;
    }

    @Override
    public synchronized boolean isCancelled() {
        return mCanceled;
    }

    @Override
    public synchronized boolean isDone() {
        return mDone;
    }

    @Override
    public void run() {
        try {
            mIsRunning = true;
            mWakeLock.acquire();
            Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
            registerReceiver();
            runInternal();
            unregisterReceiver();
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "RuntimeException thrown during delete", e);
            throw e;
        } finally {
            synchronized (this) {
                mDone = true;
            }
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    private void runInternal() {
        if (isCancelled()) {
            Log.i(LOG_TAG, "Canceled before actually handling");
            return;
        }

        boolean succeessful = true;
        int totalItems = mRequests.size();
        int successfulItems = 0;
        int currentCount = 0;

        final ArrayList<Long> contactIdsList = new ArrayList<Long>();
        for (MultiChoiceRequest request : mRequests) {
            if (mCanceled) {
                Log.d(LOG_TAG, "runInternal run: mCanceled = true, break looper");
                break;
            }
            currentCount++;

            mListener.onProcessed(MultiChoiceService.TYPE_DELETE, mJobId, currentCount, totalItems,
                    request.mContactName);
            Log.d(LOG_TAG, "runInternal run: request.mIndicator = " + request.mIndicator);
            // delete contacts from sim card
            if (request.mIndicator > RawContacts.INDICATE_PHONE) {
                int slot = SIMInfo.getSlotById(mService.getApplicationContext(), request.mIndicator);
                
                // qc begin
                if (!FeatureOption.MTK_GEMINI_SUPPORT) {
                    slot = 0;
                    Log.e(LOG_TAG, "slot = " + slot);
                }
                // qc end
                
                if (mReveiced3GSwitch || !isReadyForDelete(slot) && !GNContactsUtils.isOnlyQcContactsSupport()) {
                    Log.d(LOG_TAG, "runInternal run: isReadyForDelete("+slot+") = false");
                    succeessful = false;
                    continue;
                }
                
                Uri delSimUri;
                if(Build.VERSION.SDK_INT < 21) {
                    delSimUri = SubContactsUtils.getUri(slot);
                } else {
                    // Aurora xuyong 2015-07-13 modified for bug #14161 start
                    delSimUri = /*SubContactsUtils.getUri(slot)*/SimCardUtils.getSimContactsUri((int)request.mIndicator, 
                    		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mService.getApplicationContext(), request.mIndicator)));
                    // Aurora xuyong 2015-07-13 modified for bug #14161 end
                }

                String where = ("index = " + request.mSimIndex);

                // Gionee:wangth modify for CR00576696 begin
                /*
                if (mResolver.delete(delSimUri, where, null) <= 0) {
                    Log.d(LOG_TAG, "runInternal run: delete the sim contact failed");
                    //Just workaround to ensure UI is right. 
                    //succeessful = false;
                    //continue;
                }
                */
                int result = mResolver.delete(delSimUri, where, null);
                
                if (ContactsUtils.mIsGnContactsSupport) {
                    if (result > 0) {
                        successfulItems++;
                    } else {
                        Log.d(LOG_TAG, "runInternal run: delete the sim contact failed");
                    }
                } else if (mResolver.delete(delSimUri, where, null) <= 0) {
                    Log.d(LOG_TAG, "runInternal run: delete the sim contact failed");
                }
                // Gionee:wangth modify for CR00576696 end
            }

            // delete contacts from database
            contactIdsList.add(Long.valueOf(request.mContactId));
            if (contactIdsList.size() >= MAX_OP_COUNT_IN_ONE_BATCH) {
                successfulItems += ActualBatchDelete(contactIdsList);
                contactIdsList.clear();
            }
        }

        if (contactIdsList.size() > 0) {
            // Gionee:wangth modify for CR00576696 begin
            /*
            successfulItems += ActualBatchDelete(contactIdsList);
            */
            if (ContactsUtils.mIsGnContactsSupport) {
                ActualBatchDelete(contactIdsList);
            } else {
                successfulItems += ActualBatchDelete(contactIdsList);
            }
            // Gionee:wangth modify for CR00576696 end
            contactIdsList.clear();
        }

        if (mCanceled) {
            Log.d(LOG_TAG, "runInternal run: mCanceled = true, return");
            succeessful = false;
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_DELETE, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
            return;
        }
        
        mService.handleFinishNotification(mJobId, succeessful);
        if (succeessful) {
            mListener.onFinished(MultiChoiceService.TYPE_DELETE, mJobId, totalItems);
        } else {
            mListener.onFailed(MultiChoiceService.TYPE_DELETE, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
        }
    }

    private int ActualBatchDelete(ArrayList<Long> contactIdList) {
        Log.d(LOG_TAG, "ActualBatchDelete");
        if (contactIdList == null || contactIdList.size() == 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Long id : contactIdList) {
            sb.append(String.valueOf(id));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        Log.d(LOG_TAG, "ActualBatchDelete ContactsIds " + sb.toString() + " ");

        //Gionee <wangth><2013-04-27> modify for CR00802874 begin
        /*
        int deleteCount = mResolver.delete(
                addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("batch", "true").build()), 
                RawContacts.CONTACT_ID+ " IN " + sb.toString() + " AND " + RawContacts.DELETED + "=0", 
                null);
        */
        int deleteCount = 0;
        try {
            deleteCount = mResolver.delete(
                    addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("batch", "true").build()), 
                    RawContacts.CONTACT_ID+ " IN " + sb.toString() + " AND " + RawContacts.DELETED + "=0", 
                    null);
        } catch (IllegalStateException ie) {
            ie.printStackTrace();
            Log.d(LOG_TAG, "db error, delete failed");
        }
        //Gionee <wangth><2013-04-27> modify for CR00802874 end
        Log.d(LOG_TAG, "ActualBatchDelete ContactsIds " + deleteCount);
        return deleteCount;
    }

//    private void ActualBatchDelete(ArrayList<Long> contactIdList) {
//        Log.d(LOG_TAG, "ActualBatchDelete");
//        if (contactIdList == null || contactIdList.size() == 0) {
//            return;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("(");
//        for (Long id : contactIdList) {
//            sb.append(String.valueOf(id));
//            sb.append(",");
//        }
//        sb.deleteCharAt(sb.length() - 1);
//        sb.append(")");
//        Log.d(LOG_TAG, "ActualBatchDelete ContactsIds " + sb.toString() + " ");
//
//        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
//
//        Cursor cursor = mResolver.query(RawContacts.CONTENT_URI, new String[] {RawContacts._ID},
//                RawContacts.CONTACT_ID + " IN " + sb.toString() + " AND " + RawContacts.DELETED + "=0", 
//                null, null);
//        if (cursor != null) {
//            cursor.moveToPosition(-1);
//            while (cursor.moveToNext()) {
//                Log.d(LOG_TAG, "ActualBatchDelete rawContactsId is " + cursor.getLong(0));
//                Uri delDbUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, cursor
//                        .getLong(0));
//                delDbUri = addCallerIsSyncAdapterParameter(delDbUri);
//                ContentProviderOperation.Builder builder = ContentProviderOperation
//                        .newDelete(delDbUri);
//                operationList.add(builder.build());
//                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
//                    try {
//                        Log.i(LOG_TAG, "Before applyBatch in while. ");
//                        mResolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
//                        Thread.sleep(500);
//                        Log.i(LOG_TAG, "After applyBatch in while. ");
//                    } catch (android.os.RemoteException e) {
//                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                    } catch (android.content.OperationApplicationException e) {
//                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                    } catch (java.lang.InterruptedException e) {
//                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                    }
//                    operationList.clear();
//                }
//            }
//            if (operationList.size() > 0) {
//                try {
//                    Log.i(LOG_TAG, "Before applyBatch. ");
//                    mResolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
//                    Thread.sleep(500);
//                    Log.i(LOG_TAG, "After applyBatch ");
//                } catch (android.os.RemoteException e) {
//                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                } catch (android.content.OperationApplicationException e) {
//                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                } catch (java.lang.InterruptedException e) {
//                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                }
//                operationList.clear();
//            }
//            cursor.close();
//        }
//    }

    /**
     * -1 -- for single SIM
     * 0  -- for gemini SIM 1
     * 1  -- for gemini SIM 2
     */
    private boolean isReadyForDelete(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if(null == iTel) return false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {  // Gemini
                return GnITelephony.hasIccCardGemini(iTel, slotId)
                && GnITelephony.isRadioOnGemini(iTel, slotId)
                && !GnITelephony.isFDNEnabledGemini(iTel, slotId)
                && SubContactsUtils.checkPhbReady(slotId)
                && TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(slotId);
            } else {    // Single SIM
                return iTel.hasIccCard()
                && iTel.isRadioOn()
                && !GnITelephony.isFDNEnabled(iTel)
                && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
            } 
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "isReadyForDelete: RemoteException -> " + e);            
            return false;
        }
    }
    
    
    
        
    private void registerReceiver(){
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH);
            mService.getApplicationContext().registerReceiver(mModemSwitchListener, intentFilter);
        }       
    }
    
    private void unregisterReceiver(){
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            mService.getApplicationContext().unregisterReceiver(mModemSwitchListener);
        }
    }
    
    private Boolean mReveiced3GSwitch = false;
    
    private BroadcastReceiver mModemSwitchListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH)) {
                Log.i(LOG_TAG, "receive 3G Switch ...");
                mReveiced3GSwitch = true;
            }
        }
    };

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(GnContactsContract.CALLER_IS_SYNCADAPTER,
                String.valueOf(true)).build();
    }
}
