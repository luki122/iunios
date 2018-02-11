
package com.mediatek.contacts.activities;

import com.android.contacts.ContactsUtils; //import com.mediatek.contacts.SubContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroup;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroupException;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.activities.GroupEditorActivity;
import com.android.contacts.editor.AuroraContactEditorFragment.SaveMode;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.group.GroupEditorFragment.Listener;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDeltaList;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.WeakAsyncTask;
//import com.mediatek.featureoption.FeatureOption;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;

//import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.contacts.ContactsFeatureConstants;

import gionee.provider.GnTelephony.SIMInfo;
import com.android.internal.telephony.ITelephony;

import android.accounts.Account;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import aurora.widget.AuroraButton;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import aurora.widget.AuroraListView;
import android.widget.Toast;
//Gionee zhangxx 2012-05-23 add for CR00605159 begin
import com.android.contacts.ContactsApplication;
// Gionee zhangxx 2012-05-23 add for CR00605159 end

import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnITelephony;

import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;
import gionee.telephony.AuroraTelephoneManager;
import com.android.contacts.AuroraCardTypeUtils;
import android.os.Build;

public class EditSimContactActivity extends AuroraActivity {

    public static final String EDIT_SIM_CONTACT = "com.android.contacts.action.EDIT_SIM_CONTACT";

    private static final String TAG = "EditSimContactActivity";

    private static final String SIM_DATA = "simData";

    private static final String SIM_OLD_DATA = "simOldData";

    private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#]]+[[0-9][*#,]]*";

    private static final String USIM_EMAIL_PATTERN = "[[0-9][a-z][A-Z][_]][[0-9][a-z][A-Z][-_.]]*@[[0-9][a-z][A-Z][-_.]]+";

    private static String mAfterPhone = "";
    private static String mAfterOtherPhone = "";
    private static String mName = "";

    private static String mPhone = "";

    private static String mOtherPhone = "";

    private static String mEmail = "";

    private String updateName = "";

    private String updatephone = "";

    private String updatemail = "";

    private String update_additional_number = "";

    private String mAccountType = "";

    private String mSimType = "SIM";

    private String mAccountName = "";

    private String mOldName = "";

    private String mOldPhone = "";

    private String mOldEmail = "";

    private String mOldOtherPhone = "";

    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[] {
            "name", "number", "emails", "additionalNumber", "groupIds"
    };

    private int mSlotId = 0;

    private AuroraProgressDialog mSaveDialog;

    private long mIndexInSim = -1;

    private boolean mAirPlaneModeOn = false;

    private boolean mAirPlaneModeOnNotEdit = false;

    private boolean mFDNEnabled = false;

    private boolean mSIMInvalid = false;

    private boolean mNumberIsNull = false;

    private boolean mNumberInvalid = false;

    private boolean mFixNumberInvalid = false;

    private boolean mNumberLong = false;

    private boolean mNameLong = false;

    private boolean mFixNumberLong = false;

    private boolean mStorageFull = false;

    private boolean mGeneralFailure = false;

    private int mSaveFailToastStrId = -1;

    private boolean mOnBackGoing = true;

    private String mPhoneTypeSuffix = null; // mtk80909 for ALPS00023212

    private boolean mEmailInvalid = false;

    private boolean mEmail2GInvalid = false;

    private boolean mDoublePhoneNumber = false;
    private boolean mQuitEdit = false;
    private Account mAccount;

    private int mSaveMode = 0;

    private static final int LISTEN_PHONE_STATES = 1;// 80794

    private static final int LISTEN_PHONE_NONE_STATES = 2;

    private Uri mLookupUri;

    private ContentResolver mContentResolver;

    final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
            .getService(Context.TELEPHONY_SERVICE));

    static final int MODE_DEFAULT = 0;

    static final int MODE_INSERT = 1;

    static final int MODE_EDIT = 2;

    boolean mPhbReady = false;

    long indicate = 0;

    int mMode = MODE_DEFAULT;

    long raw_contactId = -1;

    int contact_id = 0;

    int groupNum = 1;

    HashMap<Long, String> mGroupAddList = new HashMap<Long, String>();

    HashMap<Long, String> mOldGroupAddList = new HashMap<Long, String>();

    private Handler saveContactHandler = null;

    private Handler getsaveContactHandler() {
        if (null == saveContactHandler) {
            HandlerThread controllerThread = new HandlerThread("saveContacts");
            controllerThread.start();
            saveContactHandler = new Handler(controllerThread.getLooper());
        }
        return saveContactHandler;
    }

    private ArrayList<EntityDelta> simData = new ArrayList<EntityDelta>();

    private ArrayList<EntityDelta> simOldData = new ArrayList<EntityDelta>();

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // for modem switch
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH);
            registerReceiver(mModemSwitchListener, intentFilter);
        }

        final Intent intent = getIntent();

        simData = intent.getParcelableArrayListExtra(SIM_DATA);
        simOldData = intent.getParcelableArrayListExtra(SIM_OLD_DATA);
        // get mSlot and indicate
        mSlotId = intent.getIntExtra("slotId", -1);
        indicate = intent.getLongExtra(RawContacts.INDICATE_PHONE_SIM, RawContacts.INDICATE_PHONE);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
        	if (mSlotId > -1) {
        		indicate = SIMInfoWrapper.getDefault().getSimIdBySlotId(mSlotId);
                Log.d(TAG, "indicate = " + indicate);
        	}
        } else {
        	mSlotId = 0;
            if (Build.VERSION.SDK_INT >= 21) {
                // Aurora xuyong 2015-07-13 added for bug #14161 start
            	indicate = 1;
                // Aurora xuyong 2015-07-13 added for bug #14161 end
            }
        }

        // 1 for new contact, 2 for existing contact
        mSaveMode = intent.getIntExtra("simSaveMode", MODE_DEFAULT);
        mLookupUri = intent.getData();
        mAccountType = simData.get(0).getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        if (mAccountType.equals(AccountType.ACCOUNT_TYPE_USIM)) {
            groupNum = intent.getIntExtra("groupNum", 0);
            Log.i(TAG, "groupNum : " + groupNum);
        }
        mAccountName = simData.get(0).getValues().getAsString(RawContacts.ACCOUNT_NAME);
        if (mAccountType != null && mAccountName != null) {
            mAccount = new Account(mAccountName, mAccountType);
        } else {
            finish();
            return;
        }

        Log.i(TAG, "the mSlotId is =" + mSlotId + " the indicate is =" + indicate
                + " the mSaveMode = " + mSaveMode + " the accounttype is = " + mAccountType
                + " the uri is  = " + mLookupUri);

        final ContentResolver resolver = getContentResolver();
        Log.w(TAG, "the resolver is = " + resolver);

        if (mSlotId == -1) {
            SIMInfo info = SIMInfo.getSIMInfoById(this, indicate);

            if (info != null) {
                mSlotId = info.mSlot;
            }
        }
        Log.i(TAG, "onCreate indicate is " + indicate);
        Log.i(TAG, "onCreate mSlotId is " + mSlotId);
        String[] buffer = new String[2];
        String[] bufferName = new String[2];

        long[] bufferGroup = new long[groupNum];

        // the kind number
        // int count = simData.get(0).getEntryCount(false);
        int count = simData.get(0).getContentValues().size();
        Log.i(TAG, "onCreate count:" + count);

        // get data
        int j = 0;
        int k = 0;
        int m = 0;
        for (int i = 0; i < count; i++) {
            if (StructuredName.CONTENT_ITEM_TYPE.equals(simData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
                mName = simData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
            } else if (Phone.CONTENT_ITEM_TYPE.equals(simData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
                // aurora <wangth> <2014-1-3> modify for aurora begin
//                if (simData.get(0).getContentValues().get(i).getAsString(Data.DATA2).equals("7")) {
//                    buffer[j] = simData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
//                    j++;
//                } else {
                    bufferName[m] = simData.get(0).getContentValues().get(i)
                            .getAsString(Data.DATA1);
                    m++;
//                }
                // aurora <wangth> <2014-1-3> modify for aurora end

            } else if (Email.CONTENT_ITEM_TYPE.equals(simData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
                mEmail = simData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
            } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(simData.get(0).getContentValues()
                    .get(i).getAsString(Data.MIMETYPE))) {
                bufferGroup[k] = simData.get(0).getContentValues().get(i).getAsLong(Data.DATA1);
                k++;

            }
        }

        // put group id and title to hasmap
		if (mAccountType.equals(AccountType.ACCOUNT_TYPE_USIM)) {
			String[] groupName = new String[groupNum];
			long[] groupId = new long[groupNum];
			int bufferGroupNum = bufferGroup.length;
			Log.i(TAG, "bufferGroupNum : " + bufferGroupNum);
			groupName = intent.getStringArrayExtra("groupName");
			groupId = intent.getLongArrayExtra("groupId");
			for (int i = 0; i < bufferGroupNum; i++) {
				for (int grnum = 0; grnum < groupNum; grnum++) {
					if (bufferGroup[i] == groupId[grnum]) {
						String title = groupName[grnum];
						long groupid = bufferGroup[i];
						mGroupAddList.put(groupid, title);
					}
				}

			}
		}

        // if user chose two "mobile" phone type
        if (buffer[1] != null || bufferName[1] != null) {
            mDoublePhoneNumber = true;
            setSaveFailToastText();
        } else {
            mOtherPhone = buffer[0];
            mPhone = bufferName[0];
        }

        Log.w(TAG, "the mName is = " + mName + " the mPhone is =" + mPhone + " the buffer[] is "
                + buffer[0] + " the mOtherPhone is = " + mOtherPhone + "the email is =" + mEmail);

        // checkcheckPhbReady in ContactsUtils
        mPhbReady = SimCardUtils.isPhoneBookReady(mSlotId);

        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //if (iTel != null && iTel.getIccCardTypeGemini(mSlotId).equals("USIM")) {
                if (iTel != null && AuroraCardTypeUtils.getIccCardTypeGemini(iTel, mSlotId).equals("USIM")) {
                    mSimType = "USIM";
                }
            } else {
                //if (iTel.getIccCardType().equals("USIM")) {
                if (AuroraCardTypeUtils.getIccCardType(iTel).equals("USIM")) {
                    mSimType = "USIM";
                }
            }
            
            // qc begin
            if (GNContactsUtils.isOnlyQcContactsSupport() 
                    && mAccountType.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                mSimType = "USIM";
            }
            // qc end
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.i(TAG, "initial phone number " + mPhone);
        mAfterPhone = mPhone;
        if (!TextUtils.isEmpty(mPhone)) {
            mAfterPhone = PhoneNumberUtils.stripSeparators(mPhone);
//            mAfterPhone = ((String) mPhone).replaceAll("-", "");
//            mAfterPhone = ((String) mPhone).replaceAll("\\(", "");
//            mAfterPhone = ((String) mPhone).replaceAll("\\)", "");
//            mAfterPhone = ((String) mPhone).replaceAll(" ", "");

            Log.i(TAG, "*********** after split phone number " + mAfterPhone);

            if (!Pattern.matches(SIM_NUM_PATTERN, mAfterPhone)) {
                mNumberInvalid = true;
            }
            if (setSaveFailToastText()) {
                finish();
                return;
            }

        }
        Log.i(TAG, "initial mOtherPhone number " + mOtherPhone);
        mAfterOtherPhone = mOtherPhone;
        if (!TextUtils.isEmpty(mOtherPhone)) {
//            mAfterOtherPhone = ((String) mOtherPhone).replaceAll("-", "");
//            mAfterOtherPhone = ((String) mOtherPhone).replaceAll("\\(", "");
//            mAfterOtherPhone = ((String) mOtherPhone).replaceAll("\\)", "");
//            mAfterOtherPhone = ((String) mOtherPhone).replaceAll(" ", "");
            mAfterOtherPhone = PhoneNumberUtils.stripSeparators(mOtherPhone);
            Log.i(TAG, "*********** after split mOtherPhone number " + mAfterOtherPhone);

            if (!Pattern.matches(SIM_NUM_PATTERN, mAfterOtherPhone)) {
                mNumberInvalid = true;
            }
            if (setSaveFailToastText()) {
                finish();
                return;
            }

        }
        Log.i(TAG, "initial name is  " + mName);
        /*if (!TextUtils.isEmpty(mName)) {
            mName = ((String) mName).replaceAll("-", "");

        }*/ //for ALPS00117700
        if (mSaveMode == MODE_EDIT) {

            mMode = MODE_EDIT;
            if (mLookupUri != null)
                fixIntent();
            else {
                finish();
                return;
            }
        }
        
        //boolean hasImported = SubContactsUtils.hasSimContactsImported(mSlotId);
        boolean hasImported = AbstractStartSIMService.isServiceRunning(mSlotId);
        int serviceSate = AbstractStartSIMService.getServiceState(mSlotId);
        Log.i(TAG,"[onCreate] serviceState : "+serviceSate+" | hasImported : "+hasImported);
        // check hasSimContactsImported in ContactsUtils
        if (hasImported) {
            String toastMsg = getString(R.string.msg_loading_sim_contacts_toast);
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        doSaveAction(mSaveMode);

        Log.i(TAG, "StructuredName.CONTENT_ITEM_TYPE = " + StructuredName.CONTENT_ITEM_TYPE);
        Log.i(TAG, "Phone.CONTENT_ITEM_TYPE = " + Phone.CONTENT_ITEM_TYPE);
        Log.i(TAG, "Email.CONTENT_ITEM_TYPE = " + Email.CONTENT_ITEM_TYPE);
        Log.i(TAG, "GroupMembership.CONTENT_ITEM_TYPE = " + GroupMembership.CONTENT_ITEM_TYPE);
        Log.i(TAG, "the mName is = " + mName + " the mPhone is =" + mPhone + " the buffer[] is "
                + buffer[0] + " the mOtherPhone is = " + mOtherPhone + "the email is =" + mEmail);

    }

    public void fixIntent() {
        Intent intent = getIntent();
        ContentResolver resolver = this.getContentResolver();
        Log.w(TAG, "the fixintent resolver = " + resolver);
        Uri uri = mLookupUri;
        Log.i(TAG, "uri is " + uri);
        final String authority = uri.getAuthority();
        final String mimeType = intent.resolveType(resolver);
        // long raw_contactId = -1;
        if (GnContactsContract.AUTHORITY.equals(authority)) {
            if (Contacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle selected aggregate
                final long contactId = ContentUris.parseId(uri);
                raw_contactId = SubContactsUtils.queryForRawContactId(resolver, contactId);
            } else if (RawContacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final long rawContactId = ContentUris.parseId(uri);
            }
        }
        Log.i(TAG, "raw_contactId IS " + raw_contactId);

        Cursor tmpCursor = resolver.query(uri, new String[] {
            Contacts.INDEX_IN_SIM
        }, null, null, null);
        Log.i(TAG, " ***** tmpCursor.getCount() = " + tmpCursor.getCount());
        if (tmpCursor.moveToFirst()) {
            mIndexInSim = tmpCursor.getLong(0);
            Log.i(TAG, "mIndexInSim = " + mIndexInSim);
        }
        tmpCursor.close();

        Uri dataUri = Uri.withAppendedPath(ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                raw_contactId), RawContacts.Data.CONTENT_DIRECTORY);
        Log.i(TAG, "dataUri IS " + dataUri);
        Cursor dataCursor = resolver.query(dataUri, null, null, null, null);
        if (null != dataCursor && dataCursor.moveToFirst()) {
            indicate = dataCursor.getInt(dataCursor
                    .getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));

            mSlotId = SIMInfo.getSlotById(EditSimContactActivity.this, indicate);
        }
        
        dataCursor.close();

        // int oldcount = simOldData.get(0).getEntryCount(false);
        int oldcount = simOldData.get(0).getContentValues().size();
        Log.i(TAG, "[fixIntent] oldcount:" + oldcount);
        String[] oldbuffer = new String[2];

        long[] oldbufferGroup = new long[groupNum];
        int k = 0;
        for (int i = 0; i < oldcount; i++) {
            Log.i(TAG, "simOldData.get(0).getContentValues().get(i).getAsString(Data.MIMETYPE)   "
                    + simOldData.get(0).getContentValues().get(i).getAsString(Data.MIMETYPE));
            if (StructuredName.CONTENT_ITEM_TYPE.equals(simOldData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
                mOldName = simOldData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
            } else if (Phone.CONTENT_ITEM_TYPE.equals(simOldData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
            	try {
            		if (simOldData.get(0).getContentValues().get(i).getAsString(Data.DATA2).equals("7")) {
                        mOldOtherPhone = simOldData.get(0).getContentValues().get(i).getAsString(
                                Data.DATA1);
                    } else {
                        mOldPhone = simOldData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
                    }
            	} catch (Exception e) {
            		finish();
            		return;
            	}
            } else if (Email.CONTENT_ITEM_TYPE.equals(simOldData.get(0).getContentValues().get(i)
                    .getAsString(Data.MIMETYPE))) {
                mOldEmail = simOldData.get(0).getContentValues().get(i).getAsString(Data.DATA1);
            } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(simOldData.get(0)
                    .getContentValues().get(i).getAsString(Data.MIMETYPE))) {
                oldbufferGroup[k] = simOldData.get(0).getContentValues().get(i).getAsLong(
                        Data.DATA1);
                k++;

            }
        }
        Log.i(TAG, "the mOldName is : " + mOldName + "   mOldOtherPhone : " + mOldOtherPhone
                + "  mOldPhone:  " + mOldPhone + " mOldEmail : " + mOldEmail);
        Log.i(TAG, "[fixIntent] the indicate : " + indicate + " | the mSlotId : " + mSlotId);

        // put group id and title to hasmap
        if (mAccountType.equals(AccountType.ACCOUNT_TYPE_USIM)) {
			String[] groupName = new String[groupNum];
			long[] groupId = new long[groupNum];
			int bufferGroupNum = oldbufferGroup.length;
			Log.i(TAG, "bufferGroupNum : " + bufferGroupNum);
			groupName = intent.getStringArrayExtra("groupName");
			groupId = intent.getLongArrayExtra("groupId");
			for (int i = 0; i < bufferGroupNum; i++) {
				for (int grnum = 0; grnum < groupNum; grnum++) {
					if (oldbufferGroup[i] == groupId[grnum]) {
						String title = groupName[grnum];
						long groupid = oldbufferGroup[i];
						mOldGroupAddList.put(groupid, title);
					}
				}

			}
		}
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.sendEmptyMessage(LISTEN_PHONE_NONE_STATES);// 80794
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(LISTEN_PHONE_STATES);// 80794
    }

    @Override
    protected void onDestroy() {
        // for modem switch
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            unregisterReceiver(mModemSwitchListener);
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        if (!mOnBackGoing) {
            mOnBackGoing = true;
//            // doSaveAction(mMode);
//            Intent intent = new Intent();
//            intent.putParcelableArrayListExtra("simData1", simData);
//            setResult(RESULT_CANCELED, intent);
            Log.i(TAG,"[onBackPressed]");
            finish();
        }
    }

    private class InsertSimContactThread extends Thread {
        public boolean mCanceled = false;

        int mode = 0;

        public InsertSimContactThread(int md) {
            super("InsertSimContactThread");
            mode = md;
            Log.i(TAG, "InsertSimContactThread");
        }

        @Override
        public void run() {
            Uri checkUri = null;
            int result = 0;
            final ContentResolver resolver = getContentResolver();
            updateName = mName;
            updatephone = mAfterPhone;

            Log.i(TAG, "before replace - updatephone is " + updatephone);
            if (!TextUtils.isEmpty(updatephone)) {
                Log.i(TAG,"[run] befor replaceall updatephone : "+updatephone);
                updatephone = updatephone.replaceAll("-", "");
                updatephone = updatephone.replaceAll(" ", "");
                Log.i(TAG,"[run] after replaceall updatephone : "+updatephone);
            }

            Log.i(TAG, "after replace - updatephone is " + updatephone);
            ContentValues values = new ContentValues();
            values.put("tag", TextUtils.isEmpty(updateName) ? "" : updateName);
            values.put("number", TextUtils.isEmpty(updatephone) ? "" : updatephone);

            if (mSimType.equals("USIM")) {// for USIM
                updatemail = mEmail;
                update_additional_number = mAfterOtherPhone;
                Log.i(TAG, "before replace - update_additional_number is "
                        + update_additional_number);
                if (!TextUtils.isEmpty(update_additional_number)) {
                    Log.i(TAG,"[run] befor replaceall update_additional_number : "+update_additional_number);
                    update_additional_number = update_additional_number.replaceAll("-", "");
                    update_additional_number = update_additional_number.replaceAll(" ", "");
                    Log.i(TAG,"[run] after replaceall update_additional_number : "+update_additional_number);
                }

                Log.i(TAG, "after replace - update_additional_number is "
                        + update_additional_number);
                // modify begin
                /*
                values.put("anr", TextUtils.isEmpty(update_additional_number) ? ""
                        : update_additional_number);
                */
                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                    values.put("anrs", TextUtils.isEmpty(update_additional_number) ? ""
                            : update_additional_number);
                } else {
                    values.put("anr", TextUtils.isEmpty(update_additional_number) ? ""
                            : update_additional_number);
                }
                // modify end
                values.put("emails", TextUtils.isEmpty(updatemail) ? "" : updatemail);
            }
            mPhbReady = SimCardUtils.isPhoneBookReady(mSlotId);
            Log.i(TAG, "the mPhbReady is = " + mPhbReady + " the mSlotId is = " + mSlotId);
            if (mode == MODE_INSERT) {

                Log.i("huibin", "thread mode == MODE_INSERT");
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    try {
                        //if (iTel != null && !iTel.isRadioOnGemini(mSlotId)) {
                        if (iTel != null && !GnITelephony.isRadioOnGemini(iTel, mSlotId)) {
                            mAirPlaneModeOn = true;
                        }
                        if (iTel != null && GnITelephony.isFDNEnabledGemini(iTel, mSlotId)) {
                            mFDNEnabled = true;
                        }
                        /*
                        if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimStateGemini(mSlotId))) {
                        */
                        if (!(TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(mSlotId))) {
                            mSIMInvalid = true;
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        if (iTel != null && !iTel.isRadioOn()) {
                            mAirPlaneModeOn = true;
                        }
                        if (iTel != null && GnITelephony.isFDNEnabled(iTel)) {
                            mFDNEnabled = true;
                        }
                        if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimState())) {
                            mSIMInvalid = true;
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                if (mSimType.equals("USIM")) {
                    if (TextUtils.isEmpty(updateName) && TextUtils.isEmpty(updatephone)
                            && TextUtils.isEmpty(updatemail)
                            && TextUtils.isEmpty(update_additional_number)
                            && mGroupAddList.isEmpty()) {

                        finish();
                        return;
                    } else if (TextUtils.isEmpty(updatephone)
                            && TextUtils.isEmpty(updateName)
                            && (!TextUtils.isEmpty(updatemail)
                                    || !TextUtils.isEmpty(update_additional_number) || !mGroupAddList
                                    .isEmpty())) {
                        mNumberIsNull = true;
                    } else if (!TextUtils.isEmpty(updatephone)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, updatephone)) {
                            mNumberInvalid = true;
                        }
                    }
                    if (!TextUtils.isEmpty(update_additional_number)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, update_additional_number)) {
                            mFixNumberInvalid = true;
                        }
                    }
//                    if (!TextUtils.isEmpty(updatemail)) {
//                        if (!Pattern.matches(USIM_EMAIL_PATTERN, updatemail)) {
//                            mEmailInvalid = true;
//                        }
//                    }

                } else {
                    if (TextUtils.isEmpty(updatephone) && TextUtils.isEmpty(updateName)) {
                        setResult(RESULT_OK, null);
                        finish();
                        return;
                    } else if (!TextUtils.isEmpty(updatephone)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, updatephone)) {
                            mNumberInvalid = true;
                        }
                    }
                }
                if (setSaveFailToastText()) {
                    mOnBackGoing = false;
                    return;
                }
                
                // Gionee:wangth 20130307 add for CR00778421 begin
                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                    int iccRecordsSize =  GNContactsUtils.getContactsInIcc(EditSimContactActivity.this, mSlotId);
                    int iccRecordSizeCapacity = GNContactsUtils.getQcIccSubSize(mSlotId);
                    
                    if (iccRecordsSize >= iccRecordSizeCapacity) {
                        mSaveFailToastStrId = R.string.storage_full;
                        EditSimContactActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(EditSimContactActivity.this, mSaveFailToastStrId,
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        
                        return;
                    }
                    // Gionee:wangth 20130323 add for CR00772304 begin
                    if (setSaveFailToastText3(values)) {
                        mOnBackGoing = false;
                        return;
                    }
                    // Gionee:wangth 20130323 add for CR00772304 end
                }
                // Gionee:wangth 20130307 add for CR00778421 end
                
                Log.i(TAG, "********BEGIN insert to SIM card ");
                if (mSlotId == -1) {
                    Toast.makeText(EditSimContactActivity.this, R.string.generic_failure,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                if (!SimCardUtils.isSimStateReady(mSlotId) || GNContactsUtils.isContactsSimProcess()) {
                    Toast.makeText(EditSimContactActivity.this, R.string.aurora_sim_not_ready, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                if (Build.VERSION.SDK_INT < 21) {
                    checkUri = resolver.insert(/*SimCardUtils.SimUri.getSimUri(mSlotId)*/SimCardUtils.getSimContactsUri(mSlotId, false), values);
                } else {
                    // Aurora xuyong 2015-07-13 modified for bug #14161 start
                    checkUri = resolver.insert(/*SimCardUtils.SimUri.getSimUri(mSlotId)*/SimCardUtils.getSimContactsUri((int)indicate,
                    		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(EditSimContactActivity.this, indicate))), values);
                    // Aurora xuyong 2015-07-13 modified for bug #14161 end
                }
       
                Log.i(TAG, "********END insert to SIM card ");
                Log.i(TAG, "values is " + values);
                Log.i(TAG, "checkUri is " + checkUri);
                
                if (GNContactsUtils.isOnlyQcContactsSupport() && checkUri == null) {
                	Toast.makeText(EditSimContactActivity.this, R.string.generic_failure,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                if (setSaveFailToastText2(checkUri)) {
                    mOnBackGoing = false;
                    return;
                }
                
                // index in SIM
                long indexFromUri = ContentUris.parseId(checkUri);

                Log.i(TAG, "insert to db");
                // USIM group begin
                int errorType = -1;
                if (mSimType.equals("USIM")) {
                    int ugrpId = -1;
                    Iterator<Entry<Long, String>> iter = mGroupAddList.entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<Long, String> entry = iter.next();
                        long grpId = entry.getKey();
                        String grpName = entry.getValue();
                        try {
                            ugrpId = USIMGroup.syncUSIMGroupNewIfMissing(mSlotId, grpName);
                        } catch (RemoteException e) {
                            ugrpId = -1;
                        } catch (USIMGroupException e) {
                            errorType = e.getErrorType();
                            ugrpId = -1;
                        }
                        Log.d(TAG, "[USIM group]syncUSIMGroupNewIfMissing ugrpId:" + ugrpId);
                        if (ugrpId > 0) {
                            boolean suFlag = USIMGroup.addUSIMGroupMember(mSlotId,
                                    (int) indexFromUri, ugrpId);
                            Log.d(TAG, "[USIM group]addUSIMGroupMember suFlag:" + suFlag);
                        } else {
                            iter.remove();
                        }
                    }
                }
                // USIM group end
                Uri lookupUri = SubContactsUtils.insertToDB(mAccount, updateName, mPhone,
                        updatemail, mOtherPhone, resolver, indicate, mSimType,
                        indexFromUri, mGroupAddList.keySet());

                // google default has toast, so hide here
                showResultToastText(errorType, null);

                if (errorType == -1) {
                    startViewActivity(lookupUri);
                }

                // USIM group end

                finish();
                return;
            } else if (mode == MODE_EDIT) {
                ContentValues updatevalues = new ContentValues();
                if (!TextUtils.isEmpty(mPhoneTypeSuffix)) {
                    mName = (TextUtils.isEmpty(mName)) ? ("/" + mPhoneTypeSuffix)
                            : (mName + "/" + mPhoneTypeSuffix);
                    updateName = (TextUtils.isEmpty(updateName)) ? ("/" + mPhoneTypeSuffix)
                            : (updateName + "/" + mPhoneTypeSuffix);
                }
                // mtk80909 for ALPS00023212
                update_additional_number = mAfterOtherPhone;
                if (!TextUtils.isEmpty(update_additional_number)) {
                    Log.i(TAG,"[run -edit] befor replaceall update_additional_number : "+update_additional_number);
                    update_additional_number = update_additional_number.replaceAll("-", "");
                    update_additional_number = update_additional_number.replaceAll(" ", "");
                    Log.i(TAG,"[run -edit] after replaceall update_additional_number : "+update_additional_number);
                }

                // to comment old values for index in SIM
                updatevalues.put("newTag", TextUtils.isEmpty(updateName) ? "" : updateName);
                updatevalues.put("newNumber", TextUtils.isEmpty(updatephone) ? "" : updatephone);

                updatevalues.put("tag", mOldName);
                updatevalues.put("number", mOldPhone);//aurora add zhouxiaobing 20140304
                // modify begin
                /*
                updatevalues.put("newAnr", TextUtils.isEmpty(update_additional_number) ? ""
                        : update_additional_number);
                */
                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                    updatevalues.put("newAnrs", TextUtils.isEmpty(update_additional_number) ? ""
                            : update_additional_number);
                } else {
                    updatevalues.put("newAnr", TextUtils.isEmpty(update_additional_number) ? ""
                            : update_additional_number);
                }
                // modify end
                
                updatevalues.put("newEmails", TextUtils.isEmpty(updatemail) ? "" : updatemail);

                // to use for index in SIM
                updatevalues.put("index", mIndexInSim);

                Log.i(TAG, "updatevalues IS " + updatevalues);
                Log.i(TAG, "mode IS " + mode);
                Cursor cursor = null;
                Log.i(TAG, "indicate  is " + indicate);
                if (FeatureOption.MTK_GEMINI_SUPPORT) {

                    try {
                        //qc--mtk
                        //if (iTel != null && !iTel.isRadioOnGemini(mSlotId)) {
                        if (iTel != null && !GnITelephony.isRadioOnGemini(iTel, mSlotId)) {
                            mAirPlaneModeOn = true;
                        }
                        if (iTel != null && GnITelephony.isFDNEnabledGemini(iTel, mSlotId)) {
                            mFDNEnabled = true;
                        }
                        if (!(TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(mSlotId))) {
                            mSIMInvalid = true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        if (iTel != null && !iTel.isRadioOn()) {
                            mAirPlaneModeOn = true;
                        }
                        if (iTel != null && GnITelephony.isFDNEnabled(iTel)) {
                            mFDNEnabled = true;
                        }
                        if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
                                .getSimState())) {
                            mSIMInvalid = true;
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, new String[] {
                    RawContacts.CONTACT_ID
                }, RawContacts._ID + "=" + raw_contactId, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        contact_id = c.getInt(0);
                        Log.i(TAG, "contact_id is " + contact_id);
                    }
                    c.close();
                }
                if (mSimType == "SIM") {
                    if (TextUtils.isEmpty(updateName) && TextUtils.isEmpty(updatephone)) {// if
                        // name
                        // and
                        // number
                        // is
                        // null,
                        // delete
                        // this
                        // contact
                        String where;

                        Uri iccUri;
                        if (Build.VERSION.SDK_INT < 21) {
                        	iccUri = SimCardUtils.getSimContactsUri(mSlotId, false);//SubContactsUtils.getUri(mSlotId);//aurora change zhouxiaobing 20140606
                        } else {
                            // Aurora xuyong 2015-07-13 modified for bug #14161 start
                            iccUri = SimCardUtils.getSimContactsUri((int)indicate,
                            		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(EditSimContactActivity.this, indicate)));//SubContactsUtils.getUri(mSlotId);//aurora change zhouxiaobing 20140606
                            // Aurora xuyong 2015-07-13 modified for bug #14161 end
                        }

                        // empty name and phone number
                        // use the new 'where' for index in SIM
                        //where = "index = " + mIndexInSim;
                        where="tag = "+mOldName+" number = "+mOldPhone;//aurora change zhouxiaobing 20140304
                        Log.d(TAG, "where " + where);
                        Log.d(TAG, "iccUri ******** " + iccUri);
                        int deleteDone = getContentResolver().delete(iccUri, where, null);
                        Log.i(TAG, "deleteDone is " + deleteDone);
                        if (deleteDone == 1) {
                            Uri deleteUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                                    contact_id);
                            int deleteDB = getContentResolver().delete(deleteUri, null, null);
                            Log.i(TAG, "deleteDB is " + deleteDB);
                        }
                        finish();
                        return;
                    } else if (!TextUtils.isEmpty(updatephone)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, updatephone)) {
                            mNumberInvalid = true;
                        }
                    }
                } else if (mSimType == "USIM") {
                    // if all items are empty, delete this contact
                    if (TextUtils.isEmpty(updatephone) && TextUtils.isEmpty(updateName)
                            && TextUtils.isEmpty(updatemail)
                            && TextUtils.isEmpty(update_additional_number)
                            && mGroupAddList.isEmpty()) {
                        String where;
                        Uri iccUri;
                        if(Build.VERSION.SDK_INT < 21) {
                            iccUri = SimCardUtils.getSimContactsUri(mSlotId, false);//SubContactsUtils.getUri(mSlotId);//aurora change zhouxiaobing 20140606
                        } else {
                            // Aurora xuyong 2015-07-13 modified for bug #14161 start
                            iccUri = SimCardUtils.getSimContactsUri((int)indicate, 
                            		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(EditSimContactActivity.this, indicate)));//SubContactsUtils.getUri(mSlotId);//aurora change zhouxiaobing 20140606
                            // Aurora xuyong 2015-07-13 modified for bug #14161 end
                        }

                        // use the new 'where' for index in SIM
                        //where = "index = " + mIndexInSim;
                        where="tag = "+mOldName+" number = "+mOldPhone;//aurora change zhouxiaobing 20140304
                        Log.d(TAG, "where " + where);
                        Log.d(TAG, "iccUri ******** " + iccUri);

                        int deleteDone = getContentResolver().delete(iccUri, where, null);
                        Log.i(TAG, "deleteDone is " + deleteDone);
                        if (deleteDone == 1) {
                            Uri deleteUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
                                    contact_id);
                            int deleteDB = getContentResolver().delete(deleteUri, null, null);
                            Log.i(TAG, "deleteDB is " + deleteDB);
                        }
                        finish();
                        return;
                    } else if (TextUtils.isEmpty(updatephone)
                            && TextUtils.isEmpty(updateName)
                            && (!TextUtils.isEmpty(updatemail)
                                    || !TextUtils.isEmpty(update_additional_number)
                                    || !mGroupAddList.isEmpty() || !mOldGroupAddList.isEmpty())) {
                        mNumberIsNull = true;
                    } else if (!TextUtils.isEmpty(updatephone)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, updatephone)) {
                            mNumberInvalid = true;
                        }
                    }
                    if (!TextUtils.isEmpty(update_additional_number)) {
                        if (!Pattern.matches(SIM_NUM_PATTERN, update_additional_number)) {
                            mFixNumberInvalid = true;
                        }
                    }
                    Log.i(TAG, "mFixNumberInvalid is " + mFixNumberInvalid);
//                    if (!TextUtils.isEmpty(updatemail)) {
//                        if (!Pattern.matches(USIM_EMAIL_PATTERN, updatemail)) {
//                            mEmailInvalid = true;
//                        }
//                    }
                }
                if (setSaveFailToastText()) {
                    mOnBackGoing = false;
                    return;
                }
                
                // Gionee:wangth 20130323 add for CR00772304 begin
                if (GNContactsUtils.isOnlyQcContactsSupport() && setSaveFailToastText3(updatevalues)) {
                    mOnBackGoing = false;
                    return;
                }
                // Gionee:wangth 20130323 add for CR00772304 end
                
                if (mSlotId == -1) {
                    Toast.makeText(EditSimContactActivity.this, R.string.generic_failure,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                if (!SimCardUtils.isSimStateReady(mSlotId)||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
                    Toast.makeText(EditSimContactActivity.this, R.string.aurora_sim_not_ready, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                // query phonebookto load contacts to cache for update
                if(Build.VERSION.SDK_INT < 21) {
                	cursor = resolver.query(SubContactsUtils.getUri(mSlotId),
                        ADDRESS_BOOK_COLUMN_NAMES, null, null, null);
                } else {
                    // Aurora xuyong 2015-07-13 modified for bug #14161 start
                    cursor = resolver.query(/*SubContactsUtils.getUri(mSlotId)*/SimCardUtils.getSimContactsUri((int)indicate,
                    		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(EditSimContactActivity.this, indicate))),
                            ADDRESS_BOOK_COLUMN_NAMES, null, null, null);
                    // Aurora xuyong 2015-07-13 modified for bug #14161 end
                }
                if (cursor != null) {
                	if(Build.VERSION.SDK_INT < 21) {
	                    result = resolver.update(/*SubContactsUtils.getUri(mSlotId)*/SimCardUtils.getSimContactsUri(mSlotId, false), updatevalues, null,
	                            null);//aurora change zhouxiaobing 20140606
                	}  else {
                      	// Aurora xuyong 2015-07-13 modified for bug #14161 start
                        result = resolver.update(/*SubContactsUtils.getUri(mSlotId)*/SimCardUtils.getSimContactsUri((int)indicate,
                        		SimCardUtils.isSimUsimType(SIMInfo.getSlotById(EditSimContactActivity.this, indicate))), updatevalues, null,
                                null);//aurora change zhouxiaobing 20140606
                        // Aurora xuyong 2015-07-13 modified for bug #14161 end
                	}
                    Log.i(TAG, "uri IS " + SubContactsUtils.getUri(mSlotId));
                    Log.i(TAG, "updatevalues IS " + updatevalues);
                    Log.i(TAG, "result IS " + result);
                    if (updateFailToastText(result)) {
                        mOnBackGoing = false;
                        return;
                    }
                    cursor.close();
                    
                    // Gionee:wangth 20130323 add for CR00787107 begin
                    if (GNContactsUtils.isOnlyQcContactsSupport() && result == 0) {
                        Toast.makeText(EditSimContactActivity.this, R.string.generic_failure,
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Gionee:wangth 20130323 add for CR00787107 end
                }
                Log.i(TAG, "update to db");
                // mtk80909 for ALPS00023212
                // final SubContactsUtils.NamePhoneTypePair namePhoneTypePair =
                // new SubContactsUtils.NamePhoneTypePair(
                // updateName);
                // updateName = namePhoneTypePair.name;
                // final int phoneType = namePhoneTypePair.phoneType;
                // final String phoneTypeSuffix =
                // namePhoneTypePair.phoneTypeSuffix;
                ContentValues namevalues = new ContentValues();
                String wherename = Data.RAW_CONTACT_ID + " = \'" + raw_contactId + "\'" + " AND "
                        + Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'";
                Log.i(TAG, "wherename is " + wherename + " updatename:" + updateName + "   ");

                // updata name
                // Gionee:wangth 20120608 add for CR00607774 begin
                if (ContactsUtils.mIsGnContactsSupport
                        && mOldName == null) {
                    int deleteName = resolver.delete(Data.CONTENT_URI, wherename, null);
                    Log.i(TAG, "mOldName is null, delete name is " + deleteName);
                }
                //Gionee:wangth 20120608 add for CR00607774 end
                
                if (!TextUtils.isEmpty(updateName) && !TextUtils.isEmpty(mOldName)) {
                    namevalues.put(StructuredName.DISPLAY_NAME, updateName);
                    int upname = resolver.update(Data.CONTENT_URI, namevalues, wherename, null);
                    Log.i(TAG, "upname is " + upname);
                } else if (!TextUtils.isEmpty(updateName) && TextUtils.isEmpty(mOldName)) {
                    namevalues.put(StructuredName.RAW_CONTACT_ID, raw_contactId);
                    namevalues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                    namevalues.put(StructuredName.DISPLAY_NAME, updateName);
                    Uri upNameUri = resolver.insert(Data.CONTENT_URI, namevalues);
                    Log.i(TAG, "upNameUri is " + upNameUri);
                } else if (TextUtils.isEmpty(updateName)) {
                    // update name is null,delete name row
                    int deleteName = resolver.delete(Data.CONTENT_URI, wherename, null);
                    Log.i(TAG, "deleteName is " + deleteName);
                }

                // update number
                ContentValues phonevalues = new ContentValues();
                String wherephone = Data.RAW_CONTACT_ID + " = \'" + raw_contactId + "\'" + " AND "
                        + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'" + " AND "
                        + Data.IS_ADDITIONAL_NUMBER + "=0";
                Log.i(TAG, " wherephone is " + wherephone);
                Log.i(TAG, " mOldPhone:" + mOldPhone + "|updatephone:" + mPhone);
                if (!TextUtils.isEmpty(updatephone) && !TextUtils.isEmpty(mOldPhone)) {
                    phonevalues.put(Phone.NUMBER, mPhone);
                    int upnumber = resolver.update(Data.CONTENT_URI, phonevalues, wherephone, null);
                    Log.i(TAG, "upnumber is " + upnumber);
                } else if (TextUtils.isEmpty(mOldPhone) && !TextUtils.isEmpty(updatephone)) {
                    phonevalues.put(Phone.RAW_CONTACT_ID, raw_contactId);
                    phonevalues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    phonevalues.put(Phone.NUMBER, updatephone);
                    phonevalues.put(Data.IS_ADDITIONAL_NUMBER, 0);
                    phonevalues.put(Phone.TYPE, 2);
                    Uri upNumberUri = resolver.insert(Data.CONTENT_URI, phonevalues);
                    Log.i(TAG, "upNumberUri is " + upNumberUri);
                } else if (TextUtils.isEmpty(updatephone)) {
                    int deletePhone = resolver.delete(Data.CONTENT_URI, wherephone, null);
                    Log.i(TAG, "deletePhone is " + deletePhone);
                }
                // else if (TextUtils.isEmpty(mPhone) &&
                // !TextUtils.isEmpty(updatephone)) {
                //                    
                // phonevalues.put(Phone.RAW_CONTACT_ID, raw_contactId);
                // phonevalues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                // phonevalues.put(Data.IS_ADDITIONAL_NUMBER, 0);
                // // phonevalues.put(Phone.TYPE, phoneType);
                // phonevalues.put(Data.DATA2, 2);
                // phonevalues.put(Phone.NUMBER, updatephone);
                // // mtk80909 for ALPS00023212
                // if (!TextUtils.isEmpty(phoneTypeSuffix)) {
                // phonevalues.put(Data.DATA15, phoneTypeSuffix);
                // } else {
                // phonevalues.putNull(Data.DATA15);
                // }
                // Uri upNumberUri = resolver.insert(Data.CONTENT_URI,
                // phonevalues);
                // Log.i(TAG, "upNumberUri is " + upNumberUri);
                // }

                // if USIM
                int errorType = -1;
                // Comment out by mtk80908, it can be removed after W1145
                // StringBuilder groupNameList = new StringBuilder();
                // Comment out by mtk80908, it can be removed after W1145
                if (mSimType.equals("USIM")) {
                    // update emails
                    ContentValues emailvalues = new ContentValues();

                    emailvalues.put(Email.TYPE, Email.TYPE_MOBILE);

                    String wheremail = Data.RAW_CONTACT_ID + " = \'" + raw_contactId + "\'"
                            + " AND " + Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'";
                    Log.i(TAG, "wheremail is " + wheremail);
                    if (!TextUtils.isEmpty(updatemail) && !TextUtils.isEmpty(mOldEmail)) {
                        emailvalues.put(Email.DATA, updatemail);
                        int upemail = resolver.update(Data.CONTENT_URI, emailvalues, wheremail,
                                null);
                        Log.i(TAG, "upemail is " + upemail);
                    } else if (!TextUtils.isEmpty(updatemail) && TextUtils.isEmpty(mOldEmail)) {
                        emailvalues.put(Email.RAW_CONTACT_ID, raw_contactId);
                        emailvalues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                        emailvalues.put(Email.DATA, updatemail);
                        Uri upEmailUri = resolver.insert(Data.CONTENT_URI, emailvalues);
                        Log.i(TAG, "upEmailUri is " + upEmailUri);
                    } else if (TextUtils.isEmpty(updatemail)) {
                        // update email is null,delete email row
                        int deleteEmail = resolver.delete(Data.CONTENT_URI, wheremail, null);
                        Log.i(TAG, "deleteEmail is " + deleteEmail);
                    }

                    // update additional number
                    ContentValues additionalvalues = new ContentValues();
                    String whereadditional = Data.RAW_CONTACT_ID + " = \'" + raw_contactId + "\'"
                            + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
                            + " AND " + Data.IS_ADDITIONAL_NUMBER + " =1";
                    Log.i(TAG, "whereadditional is " + whereadditional);
                    if (!TextUtils.isEmpty(update_additional_number)
                            && !TextUtils.isEmpty(mOldOtherPhone)) {
                        additionalvalues.put(Phone.NUMBER, mOtherPhone);
                        int upadditional = resolver.update(Data.CONTENT_URI, additionalvalues,
                                whereadditional, null);
                        Log.i(TAG, "upadditional is " + upadditional);
                    } else if (!TextUtils.isEmpty(update_additional_number)
                            && TextUtils.isEmpty(mOldOtherPhone)) {
                        additionalvalues.put(Phone.RAW_CONTACT_ID, raw_contactId);
                        additionalvalues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                        additionalvalues.put(Phone.NUMBER, update_additional_number);
                        additionalvalues.put(Data.IS_ADDITIONAL_NUMBER, 1);
                        additionalvalues.put(Data.DATA2, 7);
                        Uri upAdditionalUri = resolver.insert(Data.CONTENT_URI, additionalvalues);
                        Log.i(TAG, "upAdditionalUri is " + upAdditionalUri);
                    } else if (TextUtils.isEmpty(update_additional_number)) {// update
                        // additional
                        // number is null,
                        // deleteadditional
                        // number row
                        int deleteAdditional = resolver.delete(Data.CONTENT_URI, whereadditional,
                                null);
                        Log.i(TAG, "deleteAdditional is " + deleteAdditional);
                    }

                    // update group
                    if (mOldGroupAddList.size() > 0) {
                        for (Entry<Long, String> entry : mOldGroupAddList.entrySet()) {
                            long grpId = entry.getKey();
                            String grpName = entry.getValue();
                            int ugrpId = -1;
                            try {
                                ugrpId = USIMGroup.hasExistGroup(mSlotId, grpName);
                            } catch (RemoteException e) {
                                ugrpId = -1;
                            }
                            if (ugrpId > 0) {
                                USIMGroup.deleteUSIMGroupMember(mSlotId, (int) mIndexInSim, ugrpId);
                            }
                            int delCount = resolver.delete(Data.CONTENT_URI, Data.MIMETYPE + "='"
                                    + GroupMembership.CONTENT_ITEM_TYPE + "' AND "
                                    + Data.RAW_CONTACT_ID + "=" + raw_contactId + " AND "
                                    + GnContactsContract.Data.DATA1 + "=" + grpId, null);
                            Log.d(TAG, "[USIM group]DB deleteCount:" + delCount);
                            // sync USIM group info. Delete current group if its
                            // member is null.
                            if (delCount > 0) {
                                USIMGroup.syncExistUSIMGroupDelIfNoMember(
                                        EditSimContactActivity.this, mSlotId, (int) indicate,
                                        grpName, ugrpId);
                            }
                        }
                    }
                    if (mGroupAddList.size() > 0) {
                        Iterator<Entry<Long, String>> iter = mGroupAddList.entrySet().iterator();
                        while (iter.hasNext()) {
                            Entry<Long, String> entry = iter.next();
                            long grpId = entry.getKey();
                            String grpName = entry.getValue();
                            int ugrpId = -1;
                            try {
                                ugrpId = USIMGroup.syncUSIMGroupNewIfMissing(mSlotId, grpName);
                            } catch (RemoteException e) {
                                ugrpId = -1;
                            } catch (USIMGroupException e) {
                                errorType = e.getErrorType();
                                ugrpId = -1;
                            }
                            if (ugrpId > 0) {
                                USIMGroup.addUSIMGroupMember(mSlotId, (int) mIndexInSim, ugrpId);
                                // insert into contacts DB
                                additionalvalues.clear();
                                additionalvalues.put(Data.MIMETYPE,
                                        GroupMembership.CONTENT_ITEM_TYPE);
                                additionalvalues.put(GroupMembership.GROUP_ROW_ID, grpId);
                                additionalvalues.put(Data.RAW_CONTACT_ID, raw_contactId);
                                resolver.insert(Data.CONTENT_URI, additionalvalues);
                            }
                        }
                    }
                }

                showResultToastText(errorType, null);
                // USIM group end
                if (errorType == -1) {
                    setResult(RESULT_OK, null);
                    // startViewActivity(mLookupUri);

                }
                finish();
                return;
            }
        }
    }

    private boolean setSaveFailToastText() {
        mSaveFailToastStrId = -1;
        Log.i(TAG, "setSaveFailToastText mPhbReady is " + mPhbReady);
        if (!mPhbReady) {
            mSaveFailToastStrId = R.string.phone_book_busy;
            mQuitEdit = true;
        } else if (mAirPlaneModeOn) {
            mSaveFailToastStrId = R.string.AirPlane_mode_on;
            mAirPlaneModeOn = false;
            mQuitEdit = true;
        } else if (mFDNEnabled) {
            mSaveFailToastStrId = R.string.FDNEnabled;
            mFDNEnabled = false;
            mQuitEdit = true;
        } else if (mSIMInvalid && !GNContactsUtils.isOnlyQcContactsSupport()) {
            mSaveFailToastStrId = R.string.sim_invalid;
            mSIMInvalid = false;
            mQuitEdit = true;
        } else if (mNumberIsNull) {
            // Gionee zhangxx 2012-05-23 add for CR00605159 begin
            if (ContactsApplication.sIsGnContactsSupport) {
                mSaveFailToastStrId = R.string.gn_contactSavedNoNameError_Toast;
            } else {
            // Gionee zhangxx 2012-05-23 add for CR00605159 end
            mSaveFailToastStrId = R.string.cannot_insert_null_number;
            // Gionee zhangxx 2012-05-23 add for CR00605159 begin
            }
            // Gionee zhangxx 2012-05-23 add for CR00605159 end
            mNumberIsNull = false;
        } else if (mNumberInvalid) {
            mSaveFailToastStrId = R.string.sim_invalid_number;
            mNumberInvalid = false;
        } else if (mEmailInvalid) {
            mSaveFailToastStrId = R.string.email_invalid;
            mEmailInvalid = false;
        } else if (mEmail2GInvalid) {
            mSaveFailToastStrId = R.string.email_2g_invalid;
            mEmail2GInvalid = false;
        } else if (mFixNumberInvalid) {
            mSaveFailToastStrId = R.string.sim_invalid_fix_number;
            mFixNumberInvalid = false;
        } else if (mAirPlaneModeOnNotEdit) {
            mSaveFailToastStrId = R.string.AirPlane_mode_on_edit;
            mAirPlaneModeOnNotEdit = false;
            mQuitEdit = true;
        } else if (mDoublePhoneNumber) {
            mSaveFailToastStrId = R.string.has_double_phone_number;
            mDoublePhoneNumber = false;
        }

        Log.i(TAG, "mSaveFailToastStrId IS " + mSaveFailToastStrId);
        if (mSaveFailToastStrId >= 0) {
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ContactsUtils.toastManager(EditSimContactActivity.this, mSaveFailToastStrId);
                    backToFragment();
                }
            });
            return true;
        }
        return false;
    }

    private boolean setSaveFailToastText2(Uri checkUri) {
        if (checkUri != null && "error".equals(checkUri.getPathSegments().get(0))) {
            mSaveFailToastStrId = -1;
            if ("-1".equals(checkUri.getPathSegments().get(1))) {
                mNumberLong = true;
                mSaveFailToastStrId = R.string.number_too_long;
                mNumberLong = false;
            } else if ("-2".equals(checkUri.getPathSegments().get(1))) {
                mNameLong = true;
                mSaveFailToastStrId = R.string.name_too_long;
                mNameLong = false;
            } else if ("-3".equals(checkUri.getPathSegments().get(1))) {
                mStorageFull = true;
                mSaveFailToastStrId = R.string.storage_full;
                mStorageFull = false;
                mQuitEdit = true;
            } else if ("-6".equals(checkUri.getPathSegments().get(1))) {
                mFixNumberLong = true;
                mSaveFailToastStrId = R.string.fix_number_too_long;
                mFixNumberLong = false;
            } else if ("-10".equals(checkUri.getPathSegments().get(1))) {
                mGeneralFailure = true;
                mSaveFailToastStrId = R.string.generic_failure;
                mGeneralFailure = false;
                mQuitEdit = true;
            } else if ("-11".equals(checkUri.getPathSegments().get(1))) {
                mGeneralFailure = true;
                mSaveFailToastStrId = R.string.phone_book_busy;
                mGeneralFailure = false;
                mQuitEdit = true;
            }
            Log.i(TAG, "setSaveFailToastText2 mSaveFailToastStrId IS " + mSaveFailToastStrId);
            if (mSaveFailToastStrId >= 0) {
                EditSimContactActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(EditSimContactActivity.this, mSaveFailToastStrId,
                                Toast.LENGTH_SHORT).show();
                        backToFragment();
                    }
                });
                return true;
            }
            return false;
        } else if (checkUri != null) {
            return false;
        } else {
            Log.e(TAG, "setSaveFailToastText2 checkUri is null");
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    backToFragment();
                }
            });
            return true;
        }
    }

    private boolean updateFailToastText(int result) {
        mSaveFailToastStrId = -1;
        if (result == -1) {
            mSaveFailToastStrId = R.string.number_too_long;
        } else if (result == -2) {
            mSaveFailToastStrId = R.string.name_too_long;
        } else if (result == -3) {
            mSaveFailToastStrId = R.string.storage_full;
            mQuitEdit = true;
        } else if (result == -6) {
            mSaveFailToastStrId = R.string.fix_number_too_long;
        } else if (result == -10) {
            mSaveFailToastStrId = R.string.generic_failure;
            mQuitEdit = true;
        }
        if (mSaveFailToastStrId >= 0) {
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(EditSimContactActivity.this, mSaveFailToastStrId,
                            Toast.LENGTH_SHORT).show();
                    backToFragment();
                }
            });

            return true;
        }
        return false;
    }

    private void showResultToastText(int errorType, String param1) {
        String toastMsg = null;
        if (errorType == -1) {
            toastMsg = getString(R.string.contactSavedToast);
            // added for performance auto-test cases.
            Log.i(TAG, "[mtk performance result]:" + System.currentTimeMillis());
        } else if (errorType == USIMGroupException.GROUP_NAME_OUT_OF_BOUND) {
            toastMsg = getString(R.string.usim_group_name_exceed_limit);
        } else if (errorType == USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND) {
            toastMsg = getString(R.string.usim_group_count_exceed_limit);
        } else {
            Log.i(TAG, "errorType " + errorType + " does not exist.");
            return;// do nothing
        }
        final String msg = toastMsg;
        if (errorType == -1 && compleDate()) {
            return;
            //do nothing 
        } else if(errorType == -1 && !compleDate()){
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(EditSimContactActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else{
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(EditSimContactActivity.this, msg, Toast.LENGTH_SHORT).show();
                    backToFragment();
                }
            });
        }
    }

    private void doSaveAction(int mode) {
        Log.i(TAG, "In doSaveAction ");

        if (mode == MODE_INSERT) {
            Log.i("huibin", "doSaveAction mode == MODE_INSERT");
            Log.i(TAG, "mode == MODE_INSERT");

            Handler handler = getsaveContactHandler();
            if (handler != null) {
                handler.post(new InsertSimContactThread(MODE_INSERT));
            }
        } else if (mode == MODE_EDIT) {
            Log.i("huibin", "doSaveAction mode == MODE_EDIT");
            Handler handler = getsaveContactHandler();
            if (handler != null) {
                handler.post(new InsertSimContactThread(MODE_EDIT));
            }
        }

    }

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /**
         * Listen for phone state changes so that we can take down the
         * "dialpad chooser" if the phone becomes idle while the chooser UI is
         * visible.
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(TAG, "IN onServiceStateChanged ");
            EditSimContactActivity.this.closeContextMenu();

            final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));// 80794

            boolean sim1RadioOn = true;
            boolean simRadioOn = true;
            boolean hasSim1Card = true;
            boolean hasSimCard = true;

            boolean simReady = false;
            boolean sim1Ready = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                sim1Ready = SubContactsUtils
                        .simStateReady(ContactsFeatureConstants.GEMINI_SIM_1);

                Log.i(TAG, "sim1Ready IS " + sim1Ready);
                Log.i(TAG, "before sim1RadioOn is " + sim1RadioOn);
                Log.i(TAG, "before hasSim1Card is " + hasSim1Card);

                try {
                    if (null != iTel
                            //qc--mtk
                            /*
                            && !iTel
                                    .isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1)) {
                            */
                            && !GnITelephony.isRadioOnGemini(iTel, ContactsFeatureConstants.GEMINI_SIM_1)) {
                        sim1RadioOn = false;
                    }
                    if (null != iTel
                            && !GnITelephony
                                    .hasIccCardGemini(iTel, ContactsFeatureConstants.GEMINI_SIM_1)) {
                        hasSim1Card = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Log.i(TAG, "after sim1RadioOn is " + sim1RadioOn);
                Log.i(TAG, "after hasSim1Card is " + hasSim1Card);

                Log
                        .i(TAG, "PhoneStateListener.onServiceStateChanged: serviceState="
                                + serviceState);
                /*
                 * if ((sim1PINReq || sim1PUKReq ||!sim1RadioOn || !sim1Ready)
                 * && hasSim1Card) {
                 */
                if (!sim1RadioOn/* || !sim1Ready */) {
                    Log
                            .i(
                                    TAG,
                                    "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) is "
                                            + GnTelephonyManager.getSimStateGemini(ContactsFeatureConstants.GEMINI_SIM_1));
                    sim1RadioOn = true;
                    if (hasSim1Card && mSlotId == ContactsFeatureConstants.GEMINI_SIM_1) {
                        mAirPlaneModeOnNotEdit = true;
                        if (setSaveFailToastText()) {
                            finish();
                        }
                    }
                }
                /*
                 * if ((sim2PINReq || sim2PUKReq ||!sim2RadioOn || !sim1Ready)
                 * && hasSim2Card) {
                 */
            } else {
                /*
                 * simPUKReq = ContactsUtils.pukRequest(); simPINReq =
                 * ContactsUtils.pinRequest();
                 */
                simReady = SubContactsUtils.simStateReady();
                Log.i(TAG, "simReady IS " + simReady);
                try {
                    if (null != iTel && !iTel.isRadioOn()) {
                        simRadioOn = false;
                    }
                    if (null != iTel && !iTel.hasIccCard()) {
                        hasSimCard = false;
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                if ((!simRadioOn /* || !simReady */)
                /* && hasSimCard */) {
                    Log.i(TAG, "simRadioOn is " + simRadioOn);
                    Log.i(TAG, "TelephonyManager.getDefault().getSimState() is "
                            + TelephonyManager.getDefault().getSimState());
                    Log.i(TAG, "hasSimCard is " + hasSimCard);
                    simRadioOn = true;
                    if (hasSimCard && mSlotId == ContactsFeatureConstants.GEMINI_SIM_1) {
                        mAirPlaneModeOnNotEdit = true;
                        if (setSaveFailToastText()) {
                            finish();
                        }

                    }
                }
            }
        }
    };

    PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
        /**
         * Listen for phone state changes so that we can take down the
         * "dialpad chooser" if the phone becomes idle while the chooser UI is
         * visible.
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(TAG, "IN onServiceStateChanged ");
            EditSimContactActivity.this.closeContextMenu();

            final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));// 80794

            boolean sim2RadioOn = true;
            boolean hasSim2Card = true;
            boolean sim2Ready = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                sim2Ready = SubContactsUtils
                        .simStateReady(ContactsFeatureConstants.GEMINI_SIM_2);

                Log.i(TAG, "sim2Ready IS " + sim2Ready);
                Log.i(TAG, "before sim2RadioOn is " + sim2RadioOn);
                Log.i(TAG, "before hasSim2Card is " + hasSim2Card);

                try {
                    if (null != iTel
                            && !GnITelephony.isRadioOnGemini(iTel, ContactsFeatureConstants.GEMINI_SIM_2)) {
                        sim2RadioOn = false;
                    }
                    if (null != iTel
                            && !GnITelephony.hasIccCardGemini(iTel, ContactsFeatureConstants.GEMINI_SIM_2)) {
                        hasSim2Card = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Log.i(TAG, "after sim2RadioOn is " + sim2RadioOn);
                Log.i(TAG, "after hasSim2Card is " + hasSim2Card);

                Log
                        .i(TAG, "PhoneStateListener.onServiceStateChanged: serviceState="
                                + serviceState);

                if (!sim2RadioOn/* || !sim2Ready */) {
                    Log
                            .i(
                                    TAG,
                                    "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "
                                            + GnTelephonyManager.getSimStateGemini(ContactsFeatureConstants.GEMINI_SIM_2));
                    sim2RadioOn = true;
                    if (hasSim2Card && mSlotId == ContactsFeatureConstants.GEMINI_SIM_2) {
                        mAirPlaneModeOnNotEdit = true;
                        if (setSaveFailToastText()) {
                            finish();
                        }

                    }
                }
            }
        }
    };

    private void listenPhoneStates() {
        // While we're in the foreground, listen for phone state changes,
        // purely so that we can take down the "dialpad chooser" if the
        // phone becomes idle while the chooser UI is visible.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            GnTelephonyManager.listenGemini(mPhoneStateListener2,
                    PhoneStateListener.LISTEN_SERVICE_STATE,
                    ContactsFeatureConstants.GEMINI_SIM_2);
            GnTelephonyManager.listenGemini(mPhoneStateListener,
                    PhoneStateListener.LISTEN_SERVICE_STATE,
                    ContactsFeatureConstants.GEMINI_SIM_1);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        }
    }

    private void stopListenPhoneStates() {
        // Stop listening for phone state changes.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_NONE,
                    ContactsFeatureConstants.GEMINI_SIM_1);
            GnTelephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_NONE,
                    ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LISTEN_PHONE_STATES:
                    listenPhoneStates();
                    break;
                case LISTEN_PHONE_NONE_STATES:
                    stopListenPhoneStates();
            }
        }
    };

    private BroadcastReceiver mModemSwitchListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactsFeatureConstants.EVENT_PRE_3G_SWITCH)) {
                Log.i(TAG, "Before modem switch .....");
                finish();
            }
        }

    };

    public void checkGroupNameStatus(String selectedTxt) {
        int selectedNameLen = 0;
        try {
            selectedNameLen = selectedTxt.getBytes("GBK").length;
        } catch (java.io.UnsupportedEncodingException e) {
            selectedNameLen = selectedTxt.length();
        }
        if (selectedNameLen > USIMGroup.getUSIMGrpMaxNameLen(mSlotId)) {
            String msg = getString(R.string.usim_group_name_exceed_limit);
            Toast.makeText(EditSimContactActivity.this, msg, Toast.LENGTH_SHORT).show();
            backToFragment();
            finish();
        }
    }

    public void startViewActivity(Uri uri) {
    	//Gionee:huangzy 20130401 modify for CR00792013 start
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);*/
    	Intent intent = IntentFactory.newViewContactIntent(uri);
    	//Gionee:huangzy 20130401 modify for CR00792013 end
    	
        startActivity(intent);
        setResult(RESULT_OK, null);
    }

    public void backToFragment() {
        Log.i(TAG,"[backToFragment]");
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("simData1", simData);
        intent.putExtra("mQuitEdit", mQuitEdit);
        setResult(RESULT_CANCELED, intent);
        mQuitEdit = false;
        finish();
    }

    public boolean compleDate() {
        
        boolean compleName = false;
        if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mOldName)) {
            if (mName.equals(mOldName)) {
                compleName = true;
            }
        } else if (TextUtils.isEmpty(mName) && TextUtils.isEmpty(mOldName)) {
            compleName = true;
        }

        boolean complePhone = false;
        if (!TextUtils.isEmpty(mPhone) && !TextUtils.isEmpty(mOldPhone)) {
            if (mPhone.equals(mOldPhone)) {
                complePhone = true;
            }
        } else if (TextUtils.isEmpty(mPhone) && TextUtils.isEmpty(mOldPhone)) {
            complePhone = true;
        }

        boolean compleEmail = false;
        if (!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mOldEmail)) {
            if (mEmail.equals(mOldEmail)) {
                compleEmail = true;
            }
        } else if (TextUtils.isEmpty(mEmail) && TextUtils.isEmpty(mOldEmail)) {
            compleEmail = true;
        }

        boolean compleOther = false;
        if (!TextUtils.isEmpty(mOtherPhone) && !TextUtils.isEmpty(mOldOtherPhone)) {
            if (mOtherPhone.equals(mOldOtherPhone)) {
                compleOther = true;
            }
        } else if (TextUtils.isEmpty(mOtherPhone) && TextUtils.isEmpty(mOldOtherPhone)) {
            compleOther = true;
        }

        boolean compleGroup = false;
        if (mGroupAddList != null && mOldGroupAddList != null) {
            if (mGroupAddList.equals(mOldGroupAddList)) {
                compleGroup = true;
            }
        } else if (mGroupAddList == null && mOldGroupAddList == null) {
            compleGroup = true;
        }
        Log.i(TAG, "[showResultToastText]compleName : " + compleName + " | complePhone : "
                + complePhone + " | compleOther : " + compleOther + " | compleEmail: "
                + compleEmail + " | compleGroup : " + compleGroup);
        Log.i(TAG,"[showResultToastText] mName : "+mName+" | mOldName : "+mOldName+" | mEmail : "+mEmail+" | mOldEmail : "+mOldEmail);
        return (compleName && complePhone && compleOther && compleEmail && compleGroup);
    }
    
    // Gionee:wangth 20130323 add for CR00772304 begin
    private boolean hasChinese(String tag) {
        return tag != null && tag.getBytes().length > tag.length();
    }
    
    private int checkSimContactValidity(ContentValues values) {
        String tag = values.getAsString("newTag") != null ? values.getAsString("newTag") : values.getAsString("tag");
        String number = values.getAsString("newNumber") != null ?  values.getAsString("newNumber") : values.getAsString("number");
        String anrs = values.getAsString("anrs") != null ? values.getAsString("anrs") : values.getAsString("newAnrs");
        String emails = values.getAsString("emails") != null ? values.getAsString("emails") : values.getAsString("newEmails");
        int result = 0;

        if (!TextUtils.isEmpty(tag)) {
            if (!hasChinese(tag)) {
                if (null != tag && tag.length() > 14) {
                    return result = R.string.name_too_long;
                }
            } else {
                if ((null != tag && tag.length() > 6)) {
                    return result = R.string.name_too_long;
                }
            }
        }
  //aurora add zhouxiaobing 20140226 start      
        if(TextUtils.isEmpty(tag) || (tag != null &&  TextUtils.isEmpty(tag.replaceAll(" ", ""))))
        	return result = R.string.gn_contactSavedNoNameError_Toast;
        
        if(TextUtils.isEmpty(number))
        	return result = R.string.cannot_insert_null_number;
  //aurora add zhouxiaobing 20140226 end   
        if ((!TextUtils.isEmpty(number) && number.length() > 20)
                || (!TextUtils.isEmpty(anrs) && anrs.length() > 20)) {
            return result = R.string.number_too_long;
        }

        if (!TextUtils.isEmpty(number) && TextUtils.isEmpty(PhoneNumberUtils
                .stripSeparators(number))) {
            return result = R.string.sim_invalid;
        }

        if (!TextUtils.isEmpty(emails) && emails.length() >= 40) {
            return result = R.string.email_invalid;
        }

        return result;
    }
    
    private boolean setSaveFailToastText3(ContentValues values) {
        boolean result = false;
        final int showId = checkSimContactValidity(values);
        if (showId > 0) {
            EditSimContactActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(EditSimContactActivity.this, showId,
                            Toast.LENGTH_SHORT).show();
                    backToFragment();
                }
            });
            result = true;
        }
        
        return result;
    }
    // Gionee:wangth 20130323 add for CR00772304 end

}
