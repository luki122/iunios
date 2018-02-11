package com.mediatek.contacts.list.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.os.PowerManager;
import android.os.ServiceManager;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.model.AccountType;
import com.android.contacts.vcard.ProcessorBase;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.internal.telephony.ITelephony;
import com.mediatek.contacts.activities.EditSimContactActivity;
import com.mediatek.contacts.list.MultiContactsDuplicationFragment;


import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.ErrorCause;

import com.aurora.android.contacts.AuroraTelephonyManager;

import com.aurora.android.contacts.AuroraITelephony;

// Gionee jialf 20130319 added for CR00785982 start
import com.mediatek.contacts.util.OperatorUtils;
// Gionee jialf 20130319 added for CR00785982 end

public class CopyProcessor extends ProcessorBase {

    private static final String LOG_TAG = MultiContactsDuplicationFragment.TAG;
    private static final boolean DEBUG = MultiContactsDuplicationFragment.DEBUG;

    private final MultiChoiceService mService;
    private final ContentResolver mResolver;
    private final List<MultiChoiceRequest> mRequests;
    private final int mJobId;
    private final MultiChoiceHandlerListener mListener;

    private PowerManager.WakeLock mWakeLock;

    private final Account mAccountSrc;
    private final Account mAccountDst;

    private volatile boolean mCanceled = false;
    private volatile boolean mDone = false;
    private volatile boolean mIsRunning = false;
    
    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 400;
    private static int RETRYCOUNT = 20;

    public CopyProcessor(final MultiChoiceService service,
            final MultiChoiceHandlerListener listener, final List<MultiChoiceRequest> requests,
            final int jobId, final Account sourceAccount, final Account destinationAccount) {
        mService = service;
        mResolver = mService.getContentResolver();
        mListener = listener;

        mRequests = requests;
        mJobId = jobId;
        mAccountSrc = sourceAccount;
        mAccountDst = destinationAccount;

        final PowerManager powerManager = (PowerManager) mService.getApplicationContext()
                .getSystemService("power");
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, LOG_TAG);
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (DEBUG)
            Log.d(LOG_TAG, "CopyProcessor received cancel request");
        if (mDone || mCanceled) {
            return false;
        }
        mCanceled = true;
        if (!mIsRunning) {
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, -1, -1, -1);
        }
        return true;
    }

    @Override
    public int getType() {
        return MultiChoiceService.TYPE_COPY;
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
            if (AccountType.ACCOUNT_TYPE_SIM.equals(mAccountDst.type)
                    || AccountType.ACCOUNT_TYPE_USIM.equals(mAccountDst.type)) {
                //copyContactsToSim();
                copyContactsToSimWithRadioStateCheck();
            } else {
                copyContactsToAccount();
            }
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "RuntimeException thrown during copy", e);
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

    private void copyContactsToSim() {
        int errorCause = ErrorCause.NO_ERROR;

        // Process sim data, sim id or slot
        AccountWithDataSetEx account = (AccountWithDataSetEx) mAccountDst;
        Log.d(LOG_TAG, "[copyContactsToSim]AccountName:" + account.name
                + "|accountType:" + account.type);
        int dstSlotId = account.getSlotId();
        long dstSimId = -1;
        if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            dstSimId = 1;
        } else {
            dstSimId = ContactsUtils.getSubIdbySlot(dstSlotId);
        }
 
        // qc modify end
        Log.d(LOG_TAG, "[copyContactsToSim]dstSlotId:" + dstSlotId + "|dstSimId:" + dstSimId);
        boolean isTargetUsim = SimCardUtils.isSimUsimType((int)dstSimId);
        String dstSimType = isTargetUsim ? "USIM" : "SIM";
        Log.d(LOG_TAG, "[copyContactsToSim]dstSimType:" + dstSimType);

        if (!isSimReady(dstSlotId) && !isPhoneBookReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }

        ArrayList<String> numberArray = new ArrayList<String>();
        ArrayList<String> additionalNumberArray = new ArrayList<String>();
        ArrayList<String> emailArray = new ArrayList<String>();
        String targetName = null;

         ContentResolver resolver = this.mResolver;

// The following lines are provided and maintained by Mediatek inc.
// Keep previous code here.
// Description: 
//    The following code is used to do copy group data to usim. However, it also needs
//        to implement function that can copy group data in different account before 
//        using the following code.        
//
// Previous Code:        
//    HashMap<Integer, String> grpIdNameCache = new HashMap<Integer, String>();
//    HashMap<String, Integer> ugrpNameIdCache = new HashMap<String, Integer>();
//    HashSet<Long> grpIdSet = new HashSet<Long>();
//    ArrayList<Integer> ugrpIdArray = new ArrayList<Integer>();
//    Cursor groupCursor = resolver.query(Groups.CONTENT_SUMMARY_URI, 
//            new String[] {Groups._ID, Groups.TITLE}, 
//            Groups.DELETED + "=0 AND " 
//                + Groups.ACCOUNT_NAME + "='" + mAccountSrc.name + "' AND "
//                + Groups.ACCOUNT_TYPE + "='" + mAccountSrc.type + "'", 
//            null, null);
//    try {
//        while (groupCursor.moveToNext()) {
//            int gId = groupCursor.getInt(0);
//            String gTitle = groupCursor.getString(1);
//            grpIdNameCache.put(gId, gTitle);
//            Log.d(LOG_TAG, "[USIM Group]cache phone group. gId:" + gId + "|gTitle:" + gTitle);
//        }
//    } finally {
//        if (groupCursor != null)
//            groupCursor.close();
//    }
// The previous lines are provided and maintained by Mediatek inc.        

        // Process request one by one
        int totalItems = mRequests.size();
        int successfulItems = 0;
        int currentCount = 0;

        boolean isSimStorageFull = false;
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        
        // Gionee:wangth 20130307 add for CR00778421 begin
        int iccRecordsSize = 0;
        int iccRecordSizeCapacity = 0;
        int needImportCount = 0;
        int nSuccessCount = 0;
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            iccRecordsSize =  GNContactsUtils.getContactsInIcc(mService.getApplicationContext(), dstSlotId);
            iccRecordSizeCapacity = GNContactsUtils.getQcIccSubSize(dstSlotId);
            needImportCount = iccRecordSizeCapacity - iccRecordsSize;
            Log.d(LOG_TAG, "need to import count is:" + needImportCount);
        }
        // Gionee:wangth 20130307 add for CR00778421 end
        
        for (MultiChoiceRequest request : this.mRequests) {
            if (mCanceled) {
                break;
            }
            if (!isSimReady(dstSlotId) && !isPhoneBookReady(dstSlotId)) {
                Log.d(LOG_TAG, "copyContactsToSim run: sim not ready");
                errorCause = ErrorCause.ERROR_UNKNOWN;
                break;
            }
            currentCount++;
            // Notify the copy process on notification bar
            mListener.onProcessed(MultiChoiceService.TYPE_COPY, mJobId, currentCount, totalItems,
                    request.mContactName);

            // reset data
            numberArray.clear();
            additionalNumberArray.clear();
            emailArray.clear();
            targetName = null;

            int contactId = request.mContactId;

            // Query to get all src data resource.
            Uri dataUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                    Contacts.Data.CONTENT_DIRECTORY);
            final String[] projection = new String[] {
                    Contacts._ID, 
                    Contacts.Data.MIMETYPE, 
                    Contacts.Data.DATA1,
                    Contacts.Data.IS_ADDITIONAL_NUMBER
            };
            Cursor c = resolver.query(dataUri, projection, null, null, null);
            
            if (c != null && c.moveToFirst()) {
                do {
                    String mimeType = c.getString(1);
                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // For phone number
                        String number = c.getString(2);
                        String isAdditionalNumber = c.getString(3);
                        if (isAdditionalNumber != null && isAdditionalNumber.equals("1")) {
                            additionalNumberArray.add(number);
                        } else {
                            numberArray.add(number);
                        }
                    } else if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // For name
                        targetName = c.getString(2);
                    }
                    if (isTargetUsim) {
                        if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // For email
                            String email = c.getString(2);
                            emailArray.add(email);
                        } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            
                        }
                    }
                } while (c.moveToNext());
            }
            if (c != null)
                c.close();

            // copy new resournce to target sim or usim,
            // and insert into database if sucessful
            // Aurora xuyong 2015-07-14 modified for bug #14165 start
            int indicate = ContactsUtils.getSubIdbySlot(dstSlotId);
            Uri dstSimUri = ContactsUtils.getSimContactsUri((int)indicate,
            		SimCardUtils.isSimUsimType((int)indicate));
            // Aurora xuyong 2015-07-14 modified for bug #14165 end
            int maxCount = TextUtils.isEmpty(targetName) ? 0 : 1;
            if (isTargetUsim) {
                int numberCount = numberArray.size();
                int additionalCount = additionalNumberArray.size();
                int emailCount = emailArray.size();
                maxCount = (maxCount > numberCount) ? maxCount : numberCount;
                maxCount = (maxCount > additionalCount) ? maxCount : additionalCount;
                maxCount = (maxCount > emailCount) ? maxCount : emailCount;
            } else {
                numberArray.addAll(additionalNumberArray);
                additionalNumberArray.clear();
                int numberCount = numberArray.size();
                maxCount = maxCount > numberCount ? maxCount : numberCount;
            }
            int sameNameCount = 0;
            ContentValues values = new ContentValues();
            String simTag = null;
            String simNum = null;
            String simAnrNum = null;
            String simEmail = null;

            simTag = sameNameCount > 0 ? (targetName + sameNameCount) : targetName;
            simTag = TextUtils.isEmpty(simTag) ? "" : simTag;
            if ((simTag == null || simTag.isEmpty() || simTag.length() == 0)
                    && numberArray.isEmpty()) {
                Log.e(LOG_TAG, " name and number are empty");
                errorCause = ErrorCause.ERROR_UNKNOWN;
                continue;
            }

            int subContact = 0;
            for (int i = 0; i < maxCount; i++) {
                // Gionee:wangth 20130307 add for CR00778421 begin
                if (GNContactsUtils.isOnlyQcContactsSupport() && nSuccessCount >= needImportCount) {
                    errorCause = ErrorCause.SIM_STORAGE_FULL;
                    isSimStorageFull = true;
                    Log.d(LOG_TAG, "********* storage full");
                    break; // SIM card filled up
                }
                // Gionee:wangth 20130307 add for CR00778421 end
                
                values.put("tag", simTag);
                Log.d(LOG_TAG, "copyContactsToSim tag is " + simTag);
                simNum = null;
                simAnrNum = null;
                simEmail = null;
                if (!numberArray.isEmpty()) {
                    simNum = numberArray.remove(0);
                    simNum = TextUtils.isEmpty(simNum) ? "" : simNum.replace("-", "");
                    values.put("number", PhoneNumberUtils.stripSeparators(simNum));
                    Log.d(LOG_TAG, "copyContactsToSim number is " + simNum);
                }

                if (isTargetUsim) {
                    Log.d(LOG_TAG, "copyContactsToSim copy to USIM");
                    if (!additionalNumberArray.isEmpty()) {
                        simAnrNum = additionalNumberArray.remove(0);
                        simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-",
                                "");
                        // Gionee:wangth 20130306 modify begin
                        /*
                        values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                        */
                        if (GNContactsUtils.isOnlyQcContactsSupport()) {
                            values.put("anrs", PhoneNumberUtils.stripSeparators(simAnrNum));
                        } else {
                            values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                        }
                        // Gionee:wangth 20130306 modify end
                        Log.d(LOG_TAG, "copyContactsToSim anr is " + simAnrNum);
                    }
                    // Gionee jialf 20130319 added for CR00785982 start
                    else {
                        if (OperatorUtils.getOptrProperties().equals("OP02")) {
                            if (!numberArray.isEmpty()) {
                                simAnrNum = numberArray.remove(0);
                                simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-","");
                                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                                    values.put("anrs", PhoneNumberUtils.stripSeparators(simAnrNum));
                                } else {
                                    values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                                }
                                maxCount--;
                            }
                        }
                    }
                    // Gionee jialf 20130319 added for CR00785982 end
                    

                    if (!emailArray.isEmpty()) {
                        simEmail = emailArray.remove(0);
                        simEmail = TextUtils.isEmpty(simEmail) ? "" : simEmail;
                        values.put("emails", simEmail);
                        Log.d(LOG_TAG, "copyContactsToSim emails is " + simEmail);
                    }
                }
                Log.i(LOG_TAG, "Before insert Sim card.");
                Uri retUri = resolver.insert(dstSimUri, values);
                Log.i(LOG_TAG, "After insert Sim card.");

                Log.i(LOG_TAG, "retUri is " + retUri);
                if (retUri != null) {
                    List<String> checkUriPathSegs = retUri.getPathSegments();
                    if ("error".equals(checkUriPathSegs.get(0))) {
                        String errorCode = checkUriPathSegs.get(1);
                        Log.i(LOG_TAG, "error code = " + errorCode);
                        if (DEBUG) {
                            printSimErrorDetails(errorCode);
                        }
                        errorCause = ErrorCause.ERROR_UNKNOWN;
                        if ("-3".equals(checkUriPathSegs.get(1))) {
                            errorCause = ErrorCause.SIM_STORAGE_FULL;
                            isSimStorageFull = true;
                            Log.e(LOG_TAG, "Fail to insert sim contacts fail"
                                    + " because sim storage is full.");
                            break;
                        }
                    } else {
                        Log.d(LOG_TAG, "insertUsimFlag = true");
                        long indexInSim = ContentUris.parseId(retUri);
                        
                        
                        SubContactsUtils.buildInsertOperation(operationList,
                                mAccountDst, simTag, simNum, simEmail,
                                simAnrNum, resolver, dstSimId, dstSimType,
                                indexInSim, null);
                        subContact ++;
                        //successfulItems++;
                    }
                    
                    // Gionee:wangth 20130307 add for CR00778421 begin
                    if (GNContactsUtils.isOnlyQcContactsSupport()) {
                        nSuccessCount++;
                    }
                    // Gionee:wangth 20130307 add for CR00778421 end
                } else {
                    errorCause = ErrorCause.ERROR_UNKNOWN;
                }
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        Log.i(LOG_TAG, "Before applyBatch. ");
                        resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                        Log.i(LOG_TAG, "After applyBatch ");
                    } catch (android.os.RemoteException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (android.content.OperationApplicationException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    }
                    operationList.clear();
                }
            }// inner looper
            if (subContact > 0) {
                successfulItems ++;
            }
            if (isSimStorageFull)
                break;
        }
        
        if (operationList.size() > 0) {
            try {
                Log.i(LOG_TAG, "Before end applyBatch. ");
                resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                Log.i(LOG_TAG, "After end applyBatch ");
            } catch (android.os.RemoteException e) {
                Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            } catch (android.content.OperationApplicationException e) {
                Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            }
            operationList.clear();
        }
        
        if (mCanceled) {
            Log.d(LOG_TAG, "copyContactsToSim run: mCanceled = true");
            errorCause = ErrorCause.USER_CANCEL;
            mService.handleFinishNotification(mJobId, false);
            mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
            return;
        }
        
        mService.handleFinishNotification(mJobId, errorCause == ErrorCause.NO_ERROR);
        if (errorCause == ErrorCause.NO_ERROR) {
            mListener.onFinished(MultiChoiceService.TYPE_COPY, mJobId, totalItems);
        } else {
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems, errorCause);
        }
    }

    private boolean isSimReady(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (null == iTel)
            return false;

        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (DEBUG) {
                    Log.i(LOG_TAG, "iTel.hasIccCardGemini(simId) is "
                            + AuroraITelephony.hasIccCardGemini(iTel, slot));
                    Log.i(LOG_TAG, "iTel.isRadioOnGemini(simId) is " + AuroraITelephony.isRadioOnGemini(iTel, slot));
                    Log.i(LOG_TAG, "iTel.isFDNEnabledGemini(simId) is "
                            + AuroraITelephony.isFDNEnabledGemini(iTel, slot));
                    Log.i(LOG_TAG, "getSimStateGemini(simId) "
                            + (TelephonyManager.SIM_STATE_READY == AuroraTelephonyManager.getSimStateGemini(slot)));
                }
                return AuroraITelephony.hasIccCardGemini(iTel, slot)
                        && AuroraITelephony.isRadioOnGemini(iTel, slot)
                        && !AuroraITelephony.isFDNEnabledGemini(iTel, slot)
                        && TelephonyManager.SIM_STATE_READY == AuroraTelephonyManager.getSimStateGemini(slot);
            } else { // Single SIM
                return iTel.hasIccCard()
                        && iTel.isRadioOn()
                        && !AuroraITelephony.isFDNEnabled(iTel)
                        && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimState();
            }
        } catch (android.os.RemoteException e) {
            Log.w(LOG_TAG, "RemoteException!");
            return false;
        }
    }

    private boolean isPhoneBookReady(int slot) {
        Log.i(LOG_TAG, "isPhoneBookReady " + SimCardUtils.isPhoneBookReady(slot));
        return SimCardUtils.isPhoneBookReady(slot);
    }

    private final static String[] DATA_ALLCOLUMNS = new String[] {
        Data._ID,
        Data.MIMETYPE,
        Data.IS_PRIMARY,
        Data.IS_SUPER_PRIMARY,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15,
        Data.SYNC1,
        Data.SYNC2,
        Data.SYNC3,
        Data.SYNC4,
        Data.IS_ADDITIONAL_NUMBER
    };

    private void copyContactsToAccount() {
        Log.d(LOG_TAG, "copyContactsToAccount");
        if (mCanceled) {
            return;
        }

        int successfulItems = 0;
        int currentCount = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (MultiChoiceRequest request : this.mRequests) {
            sb.append(String.valueOf(request.mContactId));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        Log.d(LOG_TAG, "copyContactsToAccount contactIds " + sb.toString() + " ");
        Cursor rawContactsCursor = mResolver.query(
                RawContacts.CONTENT_URI, 
                new String[] {RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY}, 
                RawContacts.CONTACT_ID + " IN " + sb.toString() 
                // Gionee:wangth 20121030 add for CR00716135 begin
                + " AND " + RawContacts.ACCOUNT_NAME + " = '" + mAccountSrc.name + "'"
                + " AND " + RawContacts.ACCOUNT_TYPE + " = '" + mAccountSrc.type + "'", 
                // Gionee:wangth 20121030 add for CR00716135 end
                null, null);
        
        int totalItems = rawContactsCursor == null? 0 : rawContactsCursor.getCount();

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        // Process request one by one
        if (rawContactsCursor != null) {
            Log.d(LOG_TAG, "copyContactsToAccount: rawContactsCursor.size = " + rawContactsCursor.getCount());

            long nOldRawContactId;
            while (rawContactsCursor.moveToNext()) {
                if (mCanceled) {
                    Log.d(LOG_TAG, "runInternal run: mCanceled = true");
                    break;
                }
                currentCount++;
                String displayName = rawContactsCursor.getString(1);

                mListener.onProcessed(MultiChoiceService.TYPE_COPY, mJobId,
                        currentCount, totalItems, displayName);

                nOldRawContactId = rawContactsCursor.getLong(0);

                Cursor dataCursor = mResolver.query(Data.CONTENT_URI, 
                        DATA_ALLCOLUMNS, Data.RAW_CONTACT_ID + "=? ", 
                        new String[] { String.valueOf(nOldRawContactId) }, null);
                if (dataCursor == null || dataCursor.getCount() <= 0) {
                	//aurora add by liguangyu for cursor leak
                	if(dataCursor != null) {
                		dataCursor.close();
                	}
                    continue;
                }
                
                int backRef = operationList.size();
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(RawContacts.CONTENT_URI);
                if (!TextUtils.isEmpty(mAccountDst.name)
                        && !TextUtils.isEmpty(mAccountDst.type)) {
                    builder.withValue(RawContacts.ACCOUNT_NAME,mAccountDst.name);
                    builder.withValue(RawContacts.ACCOUNT_TYPE,mAccountDst.type);
                } else {
                    builder.withValues(new ContentValues());
                }
                operationList.add(builder.build());
                
                dataCursor.moveToPosition(-1);
                String[] columnNames = dataCursor.getColumnNames();
                while (dataCursor.moveToNext()) {
                    //do not copy group data between different account.
                    String mimeType = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
                    Log.i(LOG_TAG, "mimeType:" + mimeType);
                    if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType))
                        continue;
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    for (int i = 1; i < columnNames.length; i++) {
                        cursorColumnToBuilder(dataCursor, columnNames, i, builder);
                    }
                    builder.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
                    operationList.add(builder.build());
                }
                dataCursor.close();
                successfulItems++;
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        Log.i(LOG_TAG, "Before applyBatch. ");
                        mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                        Log.i(LOG_TAG, "After applyBatch ");
                    } catch (android.os.RemoteException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (android.content.OperationApplicationException e) {
                        Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    }
                    operationList.clear();
                }
            }
            rawContactsCursor.close();
            if (operationList.size() > 0) {
                try {
                    Log.i(LOG_TAG, "Before end applyBatch. ");
                    mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                    Log.i(LOG_TAG, "After end applyBatch ");
                } catch (android.os.RemoteException e) {
                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (android.content.OperationApplicationException e) {
                    Log.e(LOG_TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                }
                operationList.clear();
            }
            if (mCanceled) {
                Log.d(LOG_TAG, "runInternal run: mCanceled = true");
                mService.handleFinishNotification(mJobId, false);
                mListener.onCanceled(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                        successfulItems, totalItems - successfulItems);
                if (rawContactsCursor != null && !rawContactsCursor.isClosed()) {
                    rawContactsCursor.close();
                }
                return;
            }
        }

        mService.handleFinishNotification(mJobId, successfulItems == totalItems);
        if (successfulItems == totalItems) {
            mListener.onFinished(MultiChoiceService.TYPE_COPY, mJobId, totalItems);
        } else {
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, totalItems,
                    successfulItems, totalItems - successfulItems);
        }

        Log.d(LOG_TAG, "copyContactsToAccount: end");
    }

    private void cursorColumnToBuilder(Cursor cursor, String [] columnNames, int index, ContentProviderOperation.Builder builder) {
        switch (cursor.getType(index)) {
            case Cursor.FIELD_TYPE_NULL:
                // don't put anything in the content values
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                builder.withValue(columnNames[index], cursor.getLong(index));
                break;
            case Cursor.FIELD_TYPE_STRING:
                builder.withValue(columnNames[index], cursor.getString(index));
                break;
            case Cursor.FIELD_TYPE_BLOB:    
                builder.withValue(columnNames[index], cursor.getBlob(index));
                break;
            default:
                throw new IllegalStateException("Invalid or unhandled data type");
        }
    }
    
    private void printSimErrorDetails(String errorCode) {
        int iccError = Integer.valueOf(errorCode);
        switch (iccError) {
            case ErrorCause.SIM_NUMBER_TOO_LONG:
                Log.d(LOG_TAG, "ERROR PHONE NUMBER TOO LONG");
                break;
            case ErrorCause.SIM_NAME_TOO_LONG:
                Log.d(LOG_TAG, "ERROR NAME TOO LONG");
                break;
            case ErrorCause.SIM_STORAGE_FULL:
                Log.d(LOG_TAG, "ERROR STORAGE FULL");
                break;
            case ErrorCause.SIM_ICC_NOT_READY:
                Log.d(LOG_TAG, "ERROR ICC NOT READY");
                break;
            case ErrorCause.SIM_PASSWORD_ERROR:
                Log.d(LOG_TAG, "ERROR ICC PASSWORD ERROR");
                break;
            case ErrorCause.SIM_ANR_TOO_LONG:
                Log.d(LOG_TAG, "ERROR ICC ANR TOO LONG");
                break;
            case ErrorCause.SIM_GENERIC_FAILURE:
                Log.d(LOG_TAG, "ERROR ICC GENERIC FAILURE");
                break;
            case ErrorCause.SIM_ADN_LIST_NOT_EXIT:
                Log.d(LOG_TAG, "ERROR ICC ADN LIST NOT EXIST");
                break;
            default:
                Log.d(LOG_TAG, "ERROR ICC UNKNOW");
                break;
        }
    }

    private void copyContactsToSimWithRadioStateCheck() {
        if (mCanceled) {
            return;
        }

        int errorCause = ErrorCause.NO_ERROR;

        AccountWithDataSetEx account = (AccountWithDataSetEx) mAccountDst;
        Log.d(LOG_TAG, "[copyContactsToSimWithRadioCheck]AccountName: " + account.name
                + " | accountType: " + account.type);
        int dstSlotId = account.getSlotId();
        
        // qc modify begin
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            if (!GNContactsUtils.isQCSimReady(dstSlotId)) {
                Log.e(LOG_TAG, "sim not ready, return");
                return;
            }
        } else {
        if (!isSimReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }

        if (!isPhoneBookReady(dstSlotId)) {
            int i = 0;
            while (i++ < RETRYCOUNT) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isPhoneBookReady(dstSlotId)) {
                    break;
                }
            }
        }
        if (!isPhoneBookReady(dstSlotId)) {
            errorCause = ErrorCause.SIM_NOT_READY;
            mService.handleFinishNotification(mJobId, false);
            mListener.onFailed(MultiChoiceService.TYPE_COPY, mJobId, mRequests.size(),
                    0, mRequests.size(), errorCause);
            return;
        }
        }
        // qc end
        copyContactsToSim();
    }
}
