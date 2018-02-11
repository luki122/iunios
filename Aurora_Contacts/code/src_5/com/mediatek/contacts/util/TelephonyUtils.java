package com.mediatek.contacts.util;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
//import com.mediatek.featureoption.FeatureOption;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.contacts.ContactsApplication;
import com.android.internal.telephony.ITelephony;
import com.gionee.internal.telephony.GnITelephony;

import android.content.Intent;
import android.telecom.TelecomManager;

public class TelephonyUtils {

    private static final int DEFAULT_SIM = -3;

    private static final String USIM = "USIM";

    public static int get3GCapabilitySIM() {
        int retval = 0;
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));
            try {
                retval = ((telephony == null) ? -1 : GnITelephony.get3GCapabilitySIM(telephony));
            } catch (Exception e) {
                retval = 0;
            }
        }

        return retval;
    }

    public static boolean isSimReady() {
        return isSimReadyInner(DEFAULT_SIM);
    }

    /**
     * 
     * @param id : SIM id
     * @return
     */
    public static boolean isSimReady(int id) {
        final int slot = SIMInfoWrapper.getDefault().getSimSlotById(id);
        return isSimReadyInner(slot);
    }

    /**
     * 
     * @param slot : SIM Slot
     * @return
     */
    public static boolean isSimReadyInner(int slot) {
        boolean retval = false;
        final TelephonyManager telephonyManager = TelephonyManager.getDefault();
        if(telephonyManager == null)
            return retval;

        if(slot == -1)
            return retval;

        if(slot == DEFAULT_SIM)
            retval = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        else {
            retval = gionee.telephony.GnTelephonyManager.getSimStateGemini(slot) == TelephonyManager.SIM_STATE_READY;
        }
        return retval;
    }

    /**
     * using ITelephony API to get SIM insert state
     * @return
     */
    public static boolean isSimInsert() {
        return isSimInsertInner(DEFAULT_SIM);
    }

    public static boolean isSimInsert(int id) {
        final int slot = SIMInfoWrapper.getDefault().getSimSlotById(id);
        return isSimInsertInner(slot);
    }

    static boolean isSimInsertInner(int slot) {
          boolean retval = false;
//        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
//        if(telephony == null)
//            return retval;
//
//        try {
//            if(slot == DEFAULT_SIM)
//                retval = telephony.isSimInsert();
//            if(slot >= 0)
//                retval = telephony.isSimInsert(slot);
//        } catch(Exception e) {
//            retval = false;
//        }
        return retval;
    }

    public static boolean isRadioOn() {
        return isRadioOnInner(DEFAULT_SIM);
    }

    public static boolean isRadioOn(int id) {
        final int slot = SIMInfoWrapper.getDefault().getSimSlotById(id);
        return isRadioOnInner(slot);
    }

    public static boolean isRadioOnInner() {
        return isRadioOnInner(DEFAULT_SIM);
    }

    public static boolean isRadioOnInner(int slot) {
        boolean retval = false;
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if(telephony == null)
            return retval;

        try {
            if(slot == DEFAULT_SIM)
                retval = telephony.isRadioOn();
            if(slot >= 0)
                retval = GnITelephony.isRadioOnGemini(telephony, slot);
        } catch(Exception e) {
            retval = false;
        }
        return retval;
    }

    public static boolean isUSIMInner(int slot) {
        boolean retval = false;
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if(telephony == null)
            return retval;

//        try {
//            if(slot == DEFAULT_SIM)
//                retval = USIM.equals(telephony.getIccCardType());
//            if(slot >= 0)
//                retval = USIM.equals(telephony).getIccCardTypeGemini(slot);
//        } catch(Exception e) {
//            retval = false;
//        }
        return retval;
    }
    
    // Gionee liuyanbo 2012-07-31 add for CR00657509 start
    private static final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
            .getService(Context.TELEPHONY_SERVICE));

    private static int mPhoneType = -1;

    public static int getPhoneType() {
        if (mPhoneType == -1) {
            try {
                mPhoneType = iTel.getActivePhoneType();
            } catch (Exception e) {
            }
        }
        return mPhoneType;
    }

    public static boolean isCDMAPhone() {
        return (iTel == null) ? false : (TelephonyManager.PHONE_TYPE_CDMA == getPhoneType());
    }
    // Gionee liuyanbo 2012-07-31 add for CR00657509 end
	
	public static void showInCallScreen(Context context, boolean showDialpad){
        getTelecomManager(context).showInCallScreen(showDialpad);
	}
	
    private static TelecomManager getTelecomManager(Context context) {
        return (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
    }
    
    public static boolean isSendKeyWhileInCall(Context context, Intent intent, boolean recentCallsRequest){
        // If there is a call in progress and the user launched the dialer by hitting the call
        // button, go straight to the in-call screen.
    	if (recentCallsRequest) {
            final boolean callKey = Intent.ACTION_CALL_BUTTON.equals(intent.getAction());

            if (callKey) {
                getTelecomManager(context).showInCallScreen(false);
                return true;
            }
    	}

        return false;
    }
    
    private static boolean isInCall() {
    	return getTelecomManager(ContactsApplication.getContext()).isInCall();
    }
    
    public static void sleepInCall() {
       	while(isInCall()) {
    	  	try {
        		Thread.sleep(1000);
        	} catch (Exception e) {
        	}
    	}
    }

}
