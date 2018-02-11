package com.android.contacts;

import com.android.contacts.model.AccountType;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.RawContacts;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.gionee.internal.telephony.GnITelephony;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import gionee.telephony.GnTelephonyManager;
import com.android.internal.telephony.ITelephony;
import android.os.SystemProperties;
import com.gionee.android.contacts.SimContactsService;

/*
 *  QC special.
 */
public class GNContactsUtils {
    private static final String TAG = "GNContactsUtils";
    
    public static final String SUB = "sub_id";
    public static final int SUB_1 = 0;
    public static final int SUB_2 = 1;
    public static final String OPERATION = "operation";
    public static final String SIM_STATE = "sim_state";
    public static final int OP_PHONE = 1;
    public static final int OP_SIM = 2;
    public static final int OP_SIM_REFRESH = 3;
    public static final int OP_SIM_DELETE = 4;
    public static final String STR_TAG = "tag";
    public static final String STR_NUMBER = "number";
    public static final String STR_EMAILS = "emails";
    public static final String STR_ANRS = "anrs";
    public static final String STR_NEW_TAG = "newTag";
    public static final String STR_NEW_NUMBER = "newNumber";
    public static final String STR_NEW_EMAILS = "newEmails";
    public static final String STR_NEW_ANRS = "newAnrs";
    public static final String STR_INDEX = "index";
    public static final String STR_INDICATE_SIM_OR_PHONE = "indicateSimPhone";
    
    public static final int INCOMMING_VIDEO_TYPE = 5;
    public static final int OUTGOING_VIDEO_TYPE = 6;
    public static final int MISSED_VIDEO_TYPE = 7;
    
    public static final int SIM_STATE_READY = 1;
    public static final int SIM_STATE_NOT_READY = 2;
    public static final int SIM_STATE_ERROR = 3;
    public static final String SUBSCRIPTION_KEY  = "subscription";
    
    public static final Uri SINGLE_SIM_URI = Uri.parse("content://icc/adn");
    public static final Uri MULTI_SIM_SUB_1_URI = Uri.parse("content://iccmsim/adn");
    public static final Uri MULTI_SIM_SUB_2_URI = Uri.parse("content://iccmsim/adn_sub2");
    
    public static final String QC_USIM_STR_ANRS = "anrs";
    public static final String QC_USIM_STR_NEW_ANRS = "newAnrs";
    
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final String CALL_SETTINGS_CLASS_NAME =
            "com.android.phone.CallFeaturesSetting";
    private static final String MSIM_CALL_SETTINGS_CLASS_NAME =
            "com.android.phone.MSimCallFeaturesSetting";
    
    private static final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
            .getService(Context.TELEPHONY_SERVICE));

	public static int[] mSimState=new int[] {1, 1};//aurora add zhouxiaobing 20140707 for simcontacts
	
    public static boolean isOnlyQcContactsSupport() {
        //return GnTelephonyManager.isMultiSimEnabled(); // modify in the future
        return !(SystemProperties.get("ro.mediatek.platform").contains("MT"));
//        return !gionee.telephony.AuroraTelephoneManager.isMtkGemini();
    }
    
    public static boolean  isMultiSimEnabled() {
        return GnTelephonyManager.isMultiSimEnabled();
    }
    
    public static int getPhoneCount() {
        int result = 2;
        //return MSimTelephonyManager.getDefault().getPhoneCount();
        return result;
    }
    
    //aurora modify liguangyu 20140915 for BUG #8306 start
    public static boolean cardIsUsim(int sub) {
    	return cardIsUsimInternal(sub);
    }
    
    
    public static boolean cardIsUsimInternal(int sub) {
        String type = AuroraCardTypeUtils.getIccCardTypeGemini(iTel, sub);
        if (!FeatureOption.MTK_GEMINI_SUPPORT && sub == 0) {
            type = AuroraCardTypeUtils.getIccCardType(iTel);
        }
        Log.d(TAG, "the sub: " + sub + "    card type is " + type);
        if (type == null) {
            return false;
        }
        
        return (type.equals("USIM"))?true:false;
    }
    //aurora modify liguangyu 20140915 for BUG #8306 end
    
    
    public static boolean hasIccCard(int sub) {
        return GnITelephony.hasIccCardGemini(iTel, sub);
    }
    
    public static String getSimAccountNameBySlot(int sub) {
        String retSimName = null;
        int simType = SimCardUtils.SimType.SIM_TYPE_SIM;

        Log.i(TAG, "getSimAccountNameBySlot()+ slotId:" + sub);

        simType = SimCardUtils.getSimTypeBySlot(sub);
        Log.i(TAG, "getSimAccountNameBySlot() slotId:" + sub + " simType(0-SIM/1-USIM):" + simType);

        if (SimCardUtils.SimType.SIM_TYPE_SIM == simType) {
            retSimName = AccountType.ACCOUNT_NAME_SIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == sub) {
                retSimName = AccountType.ACCOUNT_NAME_SIM2;
            }
        } else if (SimCardUtils.SimType.SIM_TYPE_USIM == simType) {
            retSimName = AccountType.ACCOUNT_NAME_USIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == sub) {
                retSimName = AccountType.ACCOUNT_NAME_USIM2;
            }
        } else if (isOnlyQcContactsSupport()) {
            if (ContactsApplication.isMultiSimEnabled && cardIsUsim(sub)) {
                retSimName = AccountType.ACCOUNT_NAME_USIM;
                if (SimCardUtils.SimSlot.SLOT_ID2 == sub) {
                    retSimName = AccountType.ACCOUNT_NAME_USIM2;
                }
            }
            
            if (SimCardUtils.SimType.SIM_TYPE_UIM == simType) {
                retSimName = AccountType.ACCOUNT_NAME_UIM;
                if (SimCardUtils.SimSlot.SLOT_ID2 == sub) {
                    retSimName = AccountType.ACCOUNT_NAME_UIM2;
                }
            }
        } else {
            Log.e(TAG, "getSimAccountNameBySlot() Error!  get SIM Type error! simType:" + simType);
        }

        Log.i(TAG, "getSimAccountNameBySlot()- slotId:" + sub + " SimName:" + retSimName);
        return retSimName;
    }
    
    public static String getSimAccountTypeBySlot(int sub) {
        String retSimType = AccountType.ACCOUNT_TYPE_SIM;
        
        if (cardIsUsim(sub)) {
            retSimType = AccountType.ACCOUNT_TYPE_USIM;
        }
        
        return retSimType;
    }

    public static Uri getQcSimUri(int sub) {
        Uri res = null;
        
        if (isMultiSimEnabled()) {
            if (sub == SUB_1) {
                res = MULTI_SIM_SUB_1_URI;
            } else if (sub == SUB_2) {
                res = MULTI_SIM_SUB_2_URI;
            }
        } else {
            res = SINGLE_SIM_URI;
        }
        
        return res;
    }
    
    public static int querySlotIdByLookupUri(Context context, Uri lookupUri) {
        int res = -1;
        String contactId = null;
        
        if (null != lookupUri) {
            contactId = lookupUri.getLastPathSegment();
        }
        
        Cursor cursor = context.getContentResolver().query(
                GnContactsContract.Contacts.CONTENT_URI, 
                new String[]{RawContacts.INDICATE_PHONE_SIM}, 
                GnContactsContract.Contacts._ID + " = " + contactId, 
                null, null); 
        
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                res = cursor.getInt(0);
            }
            
            cursor.close();
        }
        
        if (isMultiSimEnabled()) {
            return SIMInfoWrapper.getDefault().getSlotIdBySimId(res);
        }
        
        return res - 1;
    }
    
    public static boolean isQCSimReady(int sub) {
        boolean result = false;
        boolean isRadioOn = false;
        try {
            isRadioOn = GnITelephony.isRadioOnGemini(iTel, sub);
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        if (hasIccCard(sub) && GnTelephonyManager.isValidSimState(sub)
                && isRadioOn) {
            result = true;
        }
        return result;
    }
    
    public static int getQcIccSubSize(int sub) {
        //return TelephonyManager.getDefault().getIccSubSize(sub);
//        return GnTelephonyManager.getIccSubSize(sub);
        if (cardIsUsim(sub)) {
            return 500;
        } else {
            return 250;
        }
    }
    
    public static int getContactsInIcc(Context context, int sub) {
        int indicatePhoneSim = sub + 1;
        if (isMultiSimEnabled()) {
            indicatePhoneSim = SIMInfoWrapper.getDefault().getSimIdBySlotId(sub);
        }
        Cursor c = context.getContentResolver().query(RawContacts.CONTENT_URI, 
                null, 
                RawContacts.INDICATE_PHONE_SIM + " = " + indicatePhoneSim +" and deleted = 0", 
                null,
                null);
        int result = 0;
        
        try {
            if (c != null) {
                result = c.getCount();
                c.close();
                c = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Log.d(TAG, "now Icc have contacts records is:" + result);
        return result;
    }
    
    /*
     * qc video call
     */
    public static Intent startQcVideoCallIntent(String number) {
        if (number == null) {
            return null;
        }
        
        Intent intent = new Intent("com.borqs.videocall.action.LaunchVideoCallScreen");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        intent.putExtra("IsCallOrAnswer", true); // true as a
        // call,
        // while
        // false as
        // answer

        intent.putExtra("LaunchMode", 1); // nLaunchMode: 1 as
        // telephony, while
        // 0 as socket
        intent.putExtra("call_number_key", number.replaceAll(" ", ""));
        return intent;
    }
    
    public static Intent getCallSettingsIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        if (isMultiSimEnabled()) {
            intent.setClassName(PHONE_PACKAGE, MSIM_CALL_SETTINGS_CLASS_NAME);
        } else {
            intent.setClassName(PHONE_PACKAGE, CALL_SETTINGS_CLASS_NAME);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
    
    public static boolean isAlwaysAsk() {
        boolean mAlwaysAsk = false;
        try {
            int subId = Settings.System.getInt(ContactsApplication.getInstance().getContentResolver(),
                    "multi_sim_voice_call");
            
            if (subId == 2) 
                mAlwaysAsk = true;
        } catch (SettingNotFoundException snfe) {
            Log.d(TAG, "multi_sim_voice_call   setting does not exist");
        }
        return mAlwaysAsk;
    }

//aurora change zhouxiaobing 20140707 for simcontacts start
    public static boolean isContactsSimProcess()
    {

      if(isOnlyQcContactsSupport())
      	{
		if(ContactsApplication.isMultiSimEnabled)
		 {
			if(SimCardUtils.isSimStateReady(0)&&SimCardUtils.isSimStateReady(1))
			 {
		
				return !(mSimState[0]==SIM_STATE_READY&&mSimState[1]==SIM_STATE_READY);
		
			 }
			else if(SimCardUtils.isSimStateReady(0))
			 {
		
				return !(mSimState[0]==SIM_STATE_READY);
			 }
			else if(SimCardUtils.isSimStateReady(1))
			 {
				Log.v(TAG,"mSimState[1]="+mSimState[1]);
				return !(mSimState[1]==SIM_STATE_READY);
			 }
			else 
			 {
		
				return false;
			 }
		
		 }
	   else
	   	{
	   	   if(hasIccCard(0))
	   	   {

		     return !(mSimState[0]==SIM_STATE_READY);

		   }

	    }
	   
      	}
		  return false;

    }
//aurora change zhouxiaobing 20140707 for simcontacts end	
    
    public static boolean isIndia() {    
        String prop = SystemProperties.get("ro.iuni.country.option");
  		return prop != null && prop.equalsIgnoreCase("INDIA");
    }
    
    public static String getPhoneNumberEqualString(String number) {
    	return " PHONE_NUMBERS_EQUAL(number, \"" + number + "\", 0) ";
    }
}
