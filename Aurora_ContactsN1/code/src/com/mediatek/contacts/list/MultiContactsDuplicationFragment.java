
package com.mediatek.contacts.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ContactsUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.WeakAsyncTask;
import com.android.contacts.vcard.ExportVCardActivity;
import com.android.internal.telephony.ITelephony;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.service.MultiChoiceHandlerListener;
import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Adapter;
import aurora.widget.AuroraListView;
import android.widget.Toast;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import com.gionee.CellConnService.GnCellConnMgr;
// Gionee:wangth 20120630 add for CR00632622 begin
import android.os.BadParcelableException;
// Gionee:wangth 20120630 add for CR00632622 end

public class MultiContactsDuplicationFragment extends MultiContactsPickerBaseFragment {

    public static final String TAG = "CopyMultiContacts";
    public static final boolean DEBUG = true;

    private static final String TOSDCARD = "toSDCard";
    private static final String FROMACCOUNT = "fromaccount";
    private static final String TOACCOUNT = "toaccount";

    private static final int DST_STORE_TYPE_NONE = 0;
    private static final int DST_STORE_TYPE_PHONE = 1;
    private static final int DST_STORE_TYPE_SIM = 2;
    private static final int DST_STORE_TYPE_USIM = 3;
    private static final int DST_STORE_TYPE_SDCARD = 4;
    private static final int DST_STORE_TYPE_ACCOUNT = 5;

    private boolean mToSDCard = false;
    private int mDstStoreType = DST_STORE_TYPE_NONE;
    private int mSrcStoreType = DST_STORE_TYPE_NONE;
    private Account mAccountSrc = null;
    private Account mAccountDst = null;

    private SendRequestHandler mRequestHandler = null;
    private HandlerThread mHandlerThread = null;

    private CopyRequestConnection mConnection = null;

    private GnCellConnMgr mCellMgr;
    private List<MultiChoiceRequest> mRequests = new ArrayList<MultiChoiceRequest>();

    private int mRetryCount = 20;

    private volatile CountDownLatch mEntryCount;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Intent intent = this.getArguments().getParcelable(FRAGMENT_ARGS);

        // Gionee:wangth 20120630 modify for CR00632622 begin
        /*
        mToSDCard = intent.getBooleanExtra(TOSDCARD, false);

        mAccountSrc = (Account) intent.getParcelableExtra(FROMACCOUNT);
        */
        try {
            mToSDCard = intent.getBooleanExtra(TOSDCARD, false);
            mAccountSrc = (Account) intent.getParcelableExtra(FROMACCOUNT);
        } catch (BadParcelableException badPE) {
            Log.e(TAG, "BadParcelableException...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gionee:wangth 20120630 modify for CR00632622 end
        
        if (!mToSDCard) {
            // gionee xuhz 20120724 modify for CR00655064 start
            try {
                // Gionee:wangth 20120821 modify for CR00678919 begin
                try {
                    mAccountDst = (Account) intent.getParcelableExtra(TOACCOUNT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Gionee:wangth 20120821 modify for CR00678919 end
            } catch (RuntimeException e) {
                Log.e(TAG, "RuntimeException...");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mAccountDst != null) {
                mDstStoreType = getStoreType(mAccountDst, mToSDCard);
            } else {
                mDstStoreType = DST_STORE_TYPE_ACCOUNT;
            }
            // gionee xuhz 20120724 modify for CR00655064 end
        } else {
            mDstStoreType = DST_STORE_TYPE_SDCARD;
        }

        // gionee xuhz 20120728 modify for CR00658621 start
        if (mAccountDst != null) {
            mSrcStoreType = getStoreType(mAccountDst, mToSDCard);
        }
        // gionee xuhz 20120728 modify for CR00658621 end

        ContactsApplication app = ContactsApplication.getInstance();
        if (app != null) {
            mCellMgr = app.cellConnMgr;
        }

        mEntryCount = new CountDownLatch(1);

        Log.d(TAG, "Destination store type is " + StoreTypeToString(mDstStoreType));
    }

    @Override
    protected void configureAdapter() {
        super.configureAdapter();
        // Gionee:wangth 20120807 modify for CR00668031 begin
        /*
        ContactListFilter filter = ContactListFilter.createAccountFilter(mAccountSrc.type,
                mAccountSrc.name, null, null);
        */
        ContactListFilter filter = null;
        if (mAccountSrc != null) {
            filter = ContactListFilter.createAccountFilter(mAccountSrc.type,
                    mAccountSrc.name, null, null);
        }
        // Gionee:wangth 20120807 modify for CR00668031 end
        super.setListFilter(filter);
        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter)getAdapter();
    }

    @Override
    public boolean isAccountFilterEnable() {
        return false;
    }

    @Override
    public void onDestroyView() {
 
            mEntryCount.countDown();
        

        super.onDestroyView();
    }

    @Override
    public void onOptionAction() {

        if (getListView().getCheckedItemCount() == 0) {
            return;
        }

        if (mEntryCount.getCount() > 0) {
            //Gionee:wangth 20120424 remove for CR00577133 begin
            //mEntryCount.countDown();
            //Gionee:wangth 20120424 remove for CR00577133 end
        } else {
            Log.d(TAG, "Avoid re-entrence");
            return;
        }

        setDataSetChangedNotifyEnable(false);
        if (mDstStoreType == DST_STORE_TYPE_SDCARD) {
            doExportVCardToSDCard();
        } else {
            startCopyService();

            if (mHandlerThread == null) {
                mHandlerThread = new HandlerThread(TAG);
                mHandlerThread.start();
                mRequestHandler = new SendRequestHandler(mHandlerThread.getLooper());
            }

            MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
            int count = getListView().getCount();
            for (int position = 0; position < count; ++position) {
                if (getListView().isItemChecked(position)) {
                    mRequests.add(new MultiChoiceRequest(adapter.getContactIndicator(position), adapter
                            .getSimIndex(position), adapter.getContactID(position), adapter.getContactDisplayName(position)));
                }
            }

            if (mDstStoreType == DST_STORE_TYPE_SIM || mDstStoreType == DST_STORE_TYPE_USIM) {
                // Check Radio state
                int slot = ((AccountWithDataSetEx) ContactImportExportActivity.mCheckedAccountEnd)
                        .getSlotId();
                Log.d(TAG, "Slot is " + slot);
                final int result = mCellMgr.handleCellConn(slot, GnCellConnMgr.REQUEST_TYPE_FDN,
                        serviceComplete);
                Log.d(TAG, "result = " + result);
            } else {
                mRequestHandler.sendMessage(mRequestHandler.obtainMessage(
                        SendRequestHandler.MSG_REQUEST, mRequests));
            }
        }
        //setDataSetChangedNotifyEnable(true);
    }

    private static int getStoreType(Account account, boolean mToSDCard) {
        if (mToSDCard) {
            return DST_STORE_TYPE_SDCARD;
        } else if (AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(account.type)) {
            return DST_STORE_TYPE_PHONE;
        } else if (AccountType.ACCOUNT_TYPE_SIM.equals(account.type)) {
            return DST_STORE_TYPE_SIM;
        } else if (AccountType.ACCOUNT_TYPE_USIM.equals(account.type)) {
            return DST_STORE_TYPE_USIM;
        }

        return DST_STORE_TYPE_ACCOUNT;
    }

    private static String StoreTypeToString(int type) {
        switch (type) {
            case DST_STORE_TYPE_NONE:
                return "DST_STORE_TYPE_NONE";
            case DST_STORE_TYPE_PHONE:
                return "DST_STORE_TYPE_PHONE";
            case DST_STORE_TYPE_SIM:
                return "DST_STORE_TYPE_SIM";
            case DST_STORE_TYPE_USIM:
                return "DST_STORE_TYPE_USIM";
            case DST_STORE_TYPE_SDCARD:
                return "DST_STORE_TYPE_SDCARD";
            case DST_STORE_TYPE_ACCOUNT:
                return "DST_STORE_TYPE_ACCOUNT";
            default:
                return "DST_STORE_TYPE_UNKNOWN";
        }
    }

    private void doExportVCardToSDCard() {
        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        AuroraListView listView = (AuroraListView)getListView();
        int count = getListView().getCount();
        StringBuilder exportSelection = new StringBuilder();
        exportSelection.append(Contacts._ID + " IN (");

        int curIndex = 0;
        for (int position = 0; position < count; ++position) {
            if (listView.isItemChecked(position)) {
                int contactId = adapter.getContactID(position);
                Log.d(TAG, "contactId = " + contactId);
                if (curIndex++ != 0) {
                    exportSelection.append("," + contactId);
                } else {
                    exportSelection.append(contactId);
                }
            }
        }

        exportSelection.append(")");

        Log.d(TAG, "doExportVCardToSDCard exportSelection is " + exportSelection.toString());

        Intent exportIntent = new Intent(getActivity(), ExportVCardActivity.class);
        exportIntent.putExtra("exportselection", exportSelection.toString());
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     getActivity().startActivity(exportIntent);
         *   CR ID: ALPS00117699
         */
        getActivity().startActivityForResult(exportIntent, ContactImportExportActivity.REQUEST_CODE);
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private class CopyRequestConnection implements ServiceConnection {
        private MultiChoiceService mService;

        public boolean sendCopyRequest(final List<MultiChoiceRequest> requests) {
            Log.d(TAG, "Send an copy request");
            if (mService == null) {
                Log.i(TAG, "mService is not ready");
                return false;
            }
            mService.handleCopyRequest(requests, new MultiChoiceHandlerListener(mService), mAccountSrc, mAccountDst);
            return true;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            mService = ((MultiChoiceService.MyBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from MultiChoiceService");
            //Gionee <huangzy> <2013-04-23> add for CR00797421 begin
            mService = null;
            //Gionee <huangzy> <2013-04-23> add for CR00797421 end
        }
        
        //Gionee <huangzy> <2013-04-23> add for CR00797421 begin
        public boolean isServiceBinded() {
        	return null != mService;
        }
        //Gionee <huangzy> <2013-04-23> add for CR00797421 end
    }

    private class SendRequestHandler extends Handler {

        public static final int MSG_REQUEST = 100;
        public static final int MSG_END = 200;

        public SendRequestHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REQUEST) {
                if (!mConnection.sendCopyRequest((List<MultiChoiceRequest>) msg.obj)) {
                    if (mRetryCount-- > 0) {
                        sendMessageDelayed(obtainMessage(msg.what, msg.obj), 500);
                    } else {
                        sendMessage(obtainMessage(MSG_END));
                    }
                } else {
                    sendMessage(obtainMessage(MSG_END));
                }
                return;
            } else if (msg.what == MSG_END) {
                destroyMyself();
                return;
            }
            super.handleMessage(msg);
        }

    }

    void startCopyService() {
        mConnection = new CopyRequestConnection();

        Log.i(TAG, "Bind to MultiChoiceService.");
        // We don't want the service finishes itself just after this connection.
        Intent intent = new Intent(this.getActivity(), MultiChoiceService.class);
        getContext().startService(intent);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void destroyMyself() {
        //Gionee <huangzy> <2013-04-23> modify for CR00797421 begin
        /*// Gionee liuyanbo 2012-08-02 modified for CR00653855 start
        if (mConnection != null) {
            getContext().unbindService(mConnection);
        }
        // Gionee liuyanbo 2012-08-02 modified for CR00653855 end*/
    	if (mConnection != null && mConnection.isServiceBinded()) {
    		//Gionee <huangzy> <2013-06-13> modify for CR00825461 begin
            /*getContext().unbindService(mConnection);*/
    		try {
    			getContext().unbindService(mConnection);
    		} catch (Exception e) {
    			
    		}
            //Gionee <huangzy> <2013-06-13> modify for CR00825461 end
        }
    	//Gionee <huangzy> <2013-04-23> modify for CR00797421 end
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private Runnable serviceComplete = new Runnable() {
        public void run() {
            Log.d(TAG, "serviceComplete run");
            int nRet = mCellMgr.getResult();
            Log.d(TAG, "serviceComplete result = " + GnCellConnMgr.resultToString(nRet));
            if (mCellMgr.RESULT_ABORT == nRet) {
                return;
            } else {
                mRequestHandler.sendMessage(mRequestHandler.obtainMessage(
                        SendRequestHandler.MSG_REQUEST, mRequests));
                return;
            }
        }
    };
}
