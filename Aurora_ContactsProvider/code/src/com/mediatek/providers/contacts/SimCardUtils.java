package com.mediatek.providers.contacts;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.mediatek.providers.contacts.ContactsFeatureConstants.FeatureOption;

import com.gionee.internal.telephony.GnITelephony;
import gionee.telephony.GnTelephonyManager;

public class SimCardUtils {
    public static final String TAG = "ProviderSimCardUtils";

    public static interface SimSlot {
        public static final int SLOT_NONE = -1;
        public static final int SLOT_SINGLE = 0;
        public static final int SLOT_ID1 = 0;
        public static final int SLOT_ID2 = 1;
    }

    public static interface SimType {
        public static final String SIM_TYPE_USIM_TAG = "USIM";

        public static final int SIM_TYPE_SIM = 0;
        public static final int SIM_TYPE_USIM = 1;
    }
    
    public static class SimUri {
        public static final Uri mIccUri = Uri.parse("content://icc/adn/");   
        public static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
        public static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
        
        public static final Uri mIccUsimUri = Uri.parse("content://icc/pbr");
        public static final Uri mIccUsim1Uri = Uri.parse("content://icc/pbr1/");
        public static final Uri mIccUsim2Uri = Uri.parse("content://icc/pbr2/");
        
        public static Uri getSimUri(int slotId) {
            boolean isUsim = isSimUsimType(slotId);
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotId == 0) {
                    return isUsim ? mIccUsim1Uri : mIccUri1;
                } else {
                    return isUsim ? mIccUsim2Uri : mIccUri2;
                }
            } else {
                return isUsim ? mIccUsimUri : mIccUri;
            }
        }
    }
    
    public static boolean isSimPukRequest(int slotId) {
        boolean isPukRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
//                    .getDefault().getSimStateGemini(slotId));
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == GnTelephonyManager.getSimStateGemini(slotId));
        } else {
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPukRequest;
    }

    public static boolean isSimPinRequest(int slotId) {
        boolean isPinRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
//                    .getDefault().getSimStateGemini(slotId));
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == GnTelephonyManager.getSimStateGemini(slotId));
        } else {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPinRequest;
    }

    public static boolean isSimStateReady(int slotId) {
        boolean isSimStateReady = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
//                    .getDefault().getSimStateGemini(slotId));
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(slotId));
        } else {
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                    .getDefault().getSimState());
        }
        return isSimStateReady;
    }
    
    public static boolean isSimInserted(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    //isSimInsert = iTel.isSimInsert(slotId);
                    isSimInsert = GnITelephony.isSimInsert(iTel, slotId);
                } else {
                    //isSimInsert = iTel.isSimInsert(0);
                    isSimInsert = GnITelephony.isSimInsert(iTel, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSimInsert = false;
        }
        return isSimInsert;
    }
    
    public static boolean isFdnEnabed(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isFdnEnabled = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    //isFdnEnabled = iTel.isFDNEnabledGemini(slotId);
                    isFdnEnabled = GnITelephony.isFDNEnabledGemini(iTel, slotId);
                } else {
                    //isFdnEnabled = iTel.isFDNEnabled();
                    isFdnEnabled = GnITelephony.isFDNEnabled(iTel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isFdnEnabled = false;
        }
        return isFdnEnabled;
    }
    
    public static boolean isSetRadioOn(ContentResolver resolver, int slotId) {
        boolean isRadioOn = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            int dualSimSet = Settings.System.getInt(resolver,
//                    Settings.System.DUAL_SIM_MODE_SETTING, 3);
            int dualSimSet = Settings.System.getInt(resolver,
                    "dual_sim_mode_setting", 3);
            isRadioOn = (Settings.System.getInt(resolver,
                    Settings.System.AIRPLANE_MODE_ON, 0) == 0)
                    && ((slotId + 1 == dualSimSet) || (3 == dualSimSet));
        } else {
            isRadioOn = Settings.System.getInt(resolver,
                    Settings.System.AIRPLANE_MODE_ON, 0) == 0;
        }
        return isRadioOn;
    }
    
    /**
     * check PhoneBook State is ready if ready, then return true.
     * 
     * @param slotId
     * @return
     */
    public static boolean isPhoneBookReady(int slotId) {
        final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (null == iPhb) {
            Log.d(TAG, "checkPhoneBookState, iPhb == null");
            return false;
        }
        boolean isPbReady = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //isPbReady = iPhb.isPhbReadyGemini(slotId);
                isPbReady = GnITelephony.isPhbReadyGemini(iPhb, slotId);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);

            } else {
                //isPbReady = iPhb.isPhbReady();
                isPbReady = GnITelephony.isPhbReady(iPhb);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);
            }
        } catch (Exception e) {
            Log.w(TAG, "e.getMessage is " + e.getMessage());
        }
        return isPbReady;
    }
    
    public static int getSimTypeBySlot(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        int simType = SimType.SIM_TYPE_SIM;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                if (SimType.SIM_TYPE_USIM_TAG.equals(GnITelephony.getIccCardTypeGemini(iTel, slotId)))
                    simType = SimType.SIM_TYPE_USIM;
            } else {
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                if (SimType.SIM_TYPE_USIM_TAG.equals(GnITelephony.getIccCardType(iTel)))
                    simType = SimType.SIM_TYPE_USIM;
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return simType;
    }
    
    public static boolean isSimUsimType(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isUsim = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                if (SimType.SIM_TYPE_USIM_TAG.equals(GnITelephony.getIccCardTypeGemini(iTel, slotId)))
                    isUsim = true;
            } else {
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                if (SimType.SIM_TYPE_USIM_TAG.equals(GnITelephony.getIccCardType(iTel)))
                    isUsim = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return isUsim;
    }
    
}
