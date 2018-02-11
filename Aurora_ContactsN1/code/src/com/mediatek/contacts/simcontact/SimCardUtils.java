package com.mediatek.contacts.simcontact;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Gionee liuyanbo 2012-07-31 add for CR00657509 start
import com.mediatek.contacts.util.TelephonyUtils;
// Gionee liuyanbo 2012-07-31 add for CR00657509 end
import com.aurora.android.contacts.AuroraTelephonyManager;
import com.aurora.android.contacts.AuroraITelephony;
import com.android.contacts.AuroraCardTypeUtils;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.android.contacts.R;

public class SimCardUtils {
     
    private static final String TAG = "SimCardUtils";

    public static interface SimSlot {
        public static final int SLOT_NONE = -1;
        public static final int SLOT_SINGLE = 0;
        public static final int SLOT_ID1 = ContactsFeatureConstants.GEMINI_SIM_1;
        public static final int SLOT_ID2 = ContactsFeatureConstants.GEMINI_SIM_2;
    }

    public interface SimType {
        String SIM_TYPE_USIM_TAG = "USIM";
        String SIM_TYPE_SIM_TAG = "SIM";
        String SIM_TYPE_UIM_TAG = "RUIM";
        String SIM_TYPE_CSIM_TAG = "CSIM";

        int SIM_TYPE_SIM = 0;
        int SIM_TYPE_USIM = 1;
        int SIM_TYPE_UIM = 2;
        int SIM_TYPE_CSIM = 3;
        int SIM_TYPE_UNKNOWN = -1;
    }

    public static boolean isSimPinRequest(int slotId) {
        boolean isPinRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == AuroraTelephonyManager.getSimStateGemini(slotId));
        } else {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPinRequest;
    }

    public static boolean isSimStateReady(int slotId) {
        boolean isSimStateReady = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
        	int  dualSimModeSetting = Settings.System.getInt(ContactsApplication.getInstance().getContentResolver(), "msim_mode_setting", 3);
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == AuroraTelephonyManager.getSimStateGemini(slotId)) && (((slotId +1) & dualSimModeSetting)  > 0);
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
                // qc begin
                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                    if (GNContactsUtils.isMultiSimEnabled()) {
                        return isSimInsert = GNContactsUtils.hasIccCard(slotId);
                    }
                }
                // qc end
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isSimInsert = AuroraITelephony.isSimInsert(iTel, slotId);
                } else {
                    isSimInsert = AuroraITelephony.isSimInsert(iTel, 0);
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
                    isFdnEnabled = AuroraITelephony.isFDNEnabledGemini(iTel, slotId);
                } else {
                    isFdnEnabled = AuroraITelephony.isFDNEnabled(iTel);
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
            int dualSimSet = Settings.System.getInt(resolver,
                    ContactsFeatureConstants.DUAL_SIM_MODE_SETTING, 3);
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
        
        boolean isPbReady = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                isPbReady = AuroraITelephony.isPhbReadyGemini(null, slotId);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);

            } else {
            	final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));//aurora change zhouxiaobing 20140514
                if (null == iPhb) {
                    Log.d(TAG, "checkPhoneBookState, iPhb == null");
                    return false;
                }
                isPbReady = AuroraITelephony.isPhbReady(iPhb);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);
            }
        } catch (Exception e) {
            Log.w(TAG, "e.getMessage is " + e.getMessage());
        }
        return isPbReady;
    }
    
    //aurora modify liguangyu 20140915 for BUG #8306 start
    public static int getSimTypeBySlot(int slotId) {
//        int simType = SimType.SIM_TYPE_SIM;
//    	if(slotId == 1) {
//    		simType = mSimType1;
//    	} else if(slotId == 0) {
//    		simType = mSimType0;
//    	} 
//        return simType;
    	return  getSimTypeBySlotInternal(slotId);
    }
    
    private static int mSimType0, mSimType1;
    public static int getSimTypeBySlotInternal(int slotId) { 
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        int simType = SimType.SIM_TYPE_SIM;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //qc--mtk
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                if (SimType.SIM_TYPE_USIM_TAG.equals(AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slotId)))
                    simType = SimType.SIM_TYPE_USIM;
            } else {
                //qc--mtk
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                if (SimType.SIM_TYPE_USIM_TAG.equals(AuroraCardTypeUtils.getIccCardType(iTel)))
                    simType = SimType.SIM_TYPE_USIM;
                // Gionee liuyanbo 2012-07-31 add for CR00657509 start
                if (TelephonyUtils.isCDMAPhone()) {
                    simType = SimType.SIM_TYPE_UIM;
                }
                // Gionee liuyanbo 2012-07-31 add for CR00657509 end
                
                if (GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.cardIsUsim(slotId)) {
                    simType = SimType.SIM_TYPE_USIM;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
      	if(slotId == 1) {
      		mSimType1 = simType;
    	} else if(slotId == 0) {
    		mSimType0 = simType;
    	} 
        return simType;
    }
    //aurora modify liguangyu 20140915 for BUG #8306 end
    
    /**
     * [Gemini+] get sim type integer by subId
     * sim type is integer defined in SimCardUtils.SimType
     * @param subId
     * @return SimCardUtils.SimType
     */
    public static int getSimTypeBySubId(int subId) {
        int simType = -1;

        final ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE_EX));
        if (iTel == null) {
            Log.w(TAG, "[getSimTypeBySubId]iTel == null");
            return simType;
        }

        try {
            String iccCardType = iTel.getIccCardType(subId);
            if (SimType.SIM_TYPE_USIM_TAG.equals(iccCardType)) {
                simType = SimType.SIM_TYPE_USIM;
            } else if (SimType.SIM_TYPE_UIM_TAG.equals(iccCardType)) {
                simType = SimType.SIM_TYPE_UIM;
            } else if (SimType.SIM_TYPE_SIM_TAG.equals(iccCardType)) {
                simType = SimType.SIM_TYPE_SIM;
            } else if (SimType.SIM_TYPE_CSIM_TAG.equals(iccCardType)) {
                simType = SimType.SIM_TYPE_CSIM;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[getSimTypeBySubId]catch exception:");
            e.printStackTrace();
        }

        Log.d(TAG, "[getSimTypeBySubId]subId:" + subId +
                ",simType:" + simType);

        return simType;
    }
    
    public static String getIccCardType(int subId) {
        final ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE_EX));
        if (iTel == null) {
            Log.w(TAG, "[getIccCardType]iTel == null");
            return null;
        }

        String iccCardType = null;
        try {
            iccCardType = iTel.getIccCardType(subId);
        } catch (RemoteException e) {
            Log.e(TAG, "[getIccCardType]catch exception:");
            e.printStackTrace();
        }

        Log.d(TAG, "[getIccCardType]subId:" + subId +
                ",iccCardType:" + iccCardType);
        return iccCardType;
    }
    
    public static boolean isSimUsimType(int subId) {
    	boolean isUsim = false;
    	final ITelephonyEx iTel = ITelephonyEx.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE_EX));

        if (iTel == null) {
            Log.w(TAG, "[isSimUsimType]iTel == null");
            return isUsim;
        }

        try {
            if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType(subId))
                    || SimType.SIM_TYPE_CSIM_TAG.equals(iTel.getIccCardType(subId))) {
                isUsim = true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "[isSimUsimType]catch exception:");
            e.printStackTrace();
        }

        Log.d(TAG, "[isSimUsimType]subId:" + subId +
                ",isUsim:" + isUsim);
        return isUsim;
    }
    
    private static final String NO_SLOT = String.valueOf(-1);
    private static final String SIM_KEY_WITHSLOT_PUK_REQUEST = "isSimPukRequest";
    private static final String SIM_KEY_WITHSLOT_PIN_REQUEST = "isSimPinRequest";
    private static final String SIM_KEY_WITHSLOT_STATE_READY = "isSimStateReady";
    private static final String SIM_KEY_WITHSLOT_SIM_INSERTED = "isSimInserted";
    private static final String SIM_KEY_WITHSLOT_FDN_ENABLED = "isFdnEnabed";
    private static final String SIM_KEY_WITHSLOT_SET_RADIO_ON = "isSetRadioOn";
    private static final String SIM_KEY_WITHSLOT_PHB_READY = "isPhoneBookReady";
    private static final String SIM_KEY_WITHSLOT_SIM_TYPE = "getSimTypeBySlot";
    private static final String SIM_KEY_WITHSLOT_IS_USIM = "isSimUsimType";
    private static final String SIM_KEY_SIMINFO_READY = "isSimInfoReady";
    private static final String SIM_KEY_WITHSLOT_RADIO_ON = "isRadioOn";
    private static final String SIM_KEY_WITHSLOT_HAS_ICC_CARD = "hasIccCard";
    private static final String SIM_KEY_WITHSLOT_GET_SIM_INDICATOR_STATE = "getSimIndicatorState";
    
    /**
     * Check that whether the phone book is ready only
     * @param context the caller's context.
     * @param subId the slot to check.
     * @return true the phb is ready false the phb is not ready
     */
    public static boolean isPhoneBookReady(Context context, int subId) {
        boolean hitError = false;
        int errorToastId = -1;
        if (!isPhoneBookReady(subId)) {
            hitError = true;
            errorToastId = R.string.icc_phone_book_invalid;
        }
        if (context == null) {
            Log.w(TAG, "[checkPHBState] context is null,subId:" + subId);
        }
        if (hitError && context != null) {
            Toast.makeText(context, errorToastId, Toast.LENGTH_LONG).show();
            Log.d(TAG, "[checkPHBState] hitError=" + hitError);
        }
        return !hitError;
    }
    
    /**
     * Check subid and return the sim type value.
     * @param subId The sim card subid.
     * @return sim type string value.
     */
    public static String getSimTypeTagBySubId(int subId) {
        int simType = getSimTypeBySubId(subId);
        String value;
        switch (simType) {
            case SimType.SIM_TYPE_SIM:
                value = "SIM";
                break;
            case SimType.SIM_TYPE_USIM:
                value = "USIM";
                break;
            case SimType.SIM_TYPE_UIM:
                value = "UIM";
                break;
            case SimType.SIM_TYPE_CSIM:
                value = "UIM";
                break;
            default:
                value = "UNKNOWN";
                break;
        }
        Log.d(TAG, "[getSimTypeTagBySubId] simType=" + simType + " | subId : " + subId
                + " | value : " + value);
        return value;
    }
    
}
