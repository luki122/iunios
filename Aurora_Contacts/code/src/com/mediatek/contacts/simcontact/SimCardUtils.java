package com.mediatek.contacts.simcontact;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.contacts.GNContactsUtils;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Gionee liuyanbo 2012-07-31 add for CR00657509 start
import com.mediatek.contacts.util.TelephonyUtils;
// Gionee liuyanbo 2012-07-31 add for CR00657509 end
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnITelephony;
import com.android.contacts.AuroraCardTypeUtils;
import gionee.telephony.AuroraTelephoneManager;
import android.os.Build;

public class SimCardUtils {
    public static final String TAG = "SimCardUtils";

    public static interface SimSlot {
        public static final int SLOT_NONE = -1;
        public static final int SLOT_SINGLE = 0;
        public static final int SLOT_ID1 = ContactsFeatureConstants.GEMINI_SIM_1;
        public static final int SLOT_ID2 = ContactsFeatureConstants.GEMINI_SIM_2;
    }

    public static interface SimType {
        public static final String SIM_TYPE_USIM_TAG = "USIM";

        public static final int SIM_TYPE_SIM = 0;
        public static final int SIM_TYPE_USIM = 1;
        // Gionee liuyanbo 2012-07-31 add for CR00657509 start
        public static final int SIM_TYPE_UIM = 2;
        // Gionee liuyanbo 2012-07-31 add for CR00657509 end
    }
    
    public static class SimUri {
        public static final Uri mIccUri = Uri.parse("content://icc/adn/");   
        public static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
        public static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
        
        public static final Uri mIccUsimUri = Uri.parse("content://icc/pbr");
        public static final Uri mIccUsim1Uri = Uri.parse("content://icc/pbr1/");
        public static final Uri mIccUsim2Uri = Uri.parse("content://icc/pbr2/");
        
        public static Uri getSimUri(int slotId) {
            // qc begin
            if (GNContactsUtils.isOnlyQcContactsSupport()) {
                return GNContactsUtils.getQcSimUri(slotId);
            }
            // qc end
            
            boolean isUsim = isSimUsimType(slotId);
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotId == 0) {
                    return isUsim ? mIccUsim1Uri : mIccUri1;
                } else {
                    return isUsim ? mIccUsim2Uri : mIccUri2;
                }
            } else {
                return  mIccUri;//isUsim ? mIccUsimUri : mIccUri;//aurora change zhouxiaobing 20131223
            }
        }
    }
    
    public static boolean isSimPukRequest(int slotId) {
        boolean isPukRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
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
                // qc begin
                if (GNContactsUtils.isOnlyQcContactsSupport()) {
                    if (GNContactsUtils.isMultiSimEnabled()) {
                        return isSimInsert = GNContactsUtils.hasIccCard(slotId);
                    }
                }
                // qc end
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isSimInsert = GnITelephony.isSimInsert(iTel, slotId);
                } else {
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
                    isFdnEnabled = GnITelephony.isFDNEnabledGemini(iTel, slotId);
                } else {
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
                isPbReady = GnITelephony.isPhbReadyGemini(null, slotId);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);

            } else {
            	final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));//aurora change zhouxiaobing 20140514
                if (null == iPhb) {
                    Log.d(TAG, "checkPhoneBookState, iPhb == null");
                    return false;
                }
                isPbReady = GnITelephony.isPhbReady(iPhb);
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
    
    
    public static boolean isSimUsimType(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isUsim = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT || 
                    (GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.isMultiSimEnabled())) {
                //qc--mtk
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                if (SimType.SIM_TYPE_USIM_TAG.equals(AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slotId)))
                    isUsim = true;
            } else {
                //qc--mtk
                //if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                if (SimType.SIM_TYPE_USIM_TAG.equals(AuroraCardTypeUtils.getIccCardType(iTel)))
                    isUsim = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return isUsim;
    }
    
    public static boolean isSimInfoReady() {
        String simInfoReady = SystemProperties.get(ContactsFeatureConstants.PROPERTY_SIM_INFO_READY);//qc--mtk
        return "true".equals(simInfoReady);
    }
    
    //for 5.0 slot is really subId
    public static Uri getSimContactsUri(int slot, boolean isUsim) {
    	if(Build.VERSION.SDK_INT >= 21) {
			if (!isUsim) {
				return Uri.parse("content://icc/adn/subId/" + slot);
			} else {
				return Uri.parse("content://icc/pbr/subId/" + slot);
			}
    	} else {
    		return AuroraTelephoneManager.getSimContactsUri(slot);
    	}
    }
}
