package com.aurora.android.contacts;

import android.content.Context;
import android.os.ServiceManager;
import android.os.IBinder;

import com.android.internal.telephony.ITelephony;
//import com.android.internal.telephony.msim.ITelephonyMSim;
import android.telephony.TelephonyManager;

// Gionee <fengjianyi><2013-03-13> add for CR00779348 start
import android.os.RemoteException;
// Gionee <fengjianyi><2013-03-13> add for CR00779348 end
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import com.mediatek.internal.telephony.ITelephonyEx;

public class AuroraITelephony {
	public static final ITelephony iTelephonyService = ITelephony.Stub
			.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
	private static TelephonyManager telManager = TelephonyManager.getDefault();

	public static String getIccCardTypeGemini(ITelephony iTel, int slotId) {
		// return iTelephonyService.getIccCardTypeGemini(slotId);
		ITelephony iTelephonyService = ITelephony.Stub
				.asInterface(ServiceManager
						.checkService(Context.TELEPHONY_SERVICE));
		String string = "";
		if (slotId == 0)
			string = "SIMORUSIM1";
		else
			string = "SIMORUSIM2";
		try {
			int at = iTelephonyService.checkCarrierPrivilegesForPackage(string);
			switch (at) {
			case 1:// APPTYPE_SIM:
				return "SIM";
			case 2:// APPTYPE_USIM:
				return "USIM";
			case 3:// APPTYPE_RUIM:
				return "UIM";
			case 4:// APPTYPE_CSIM:
				return "SIM";
			case 5:// APPTYPE_ISIM:
				return "SIM";

			}
		} catch (Exception e) {
		}
		return "unknown";// telManager.getCardType();
	}

	public static String getIccCardType(ITelephony iTel) {
		// return iTelephonyService.getIccCardType();
		// return "UIM";
		try {
			int at = iTelephonyService
					.checkCarrierPrivilegesForPackage("SIMORUSIM");
			switch (at) {
			case 1:// APPTYPE_SIM:
				return "SIM";
			case 2:// APPTYPE_USIM:
				return "USIM";
			case 3:// APPTYPE_RUIM:
				return "UIM";
			case 4:// APPTYPE_CSIM:
				return "SIM";
			case 5:// APPTYPE_ISIM:
				return "SIM";

			}
		} catch (Exception e) {
		}
		// try{
		// iTelephonyService.enableApnType("SIMORUSIM");
		// }catch(Exception e){}
		// String simtype=SystemProperties.get("persist.iuni.sim.type");
		return "unknown";
	}

	public static boolean isTestIccCard(ITelephony iTel) {
		// return iTel.isTestIccCard();
		return false;
	}

	public static int get3GCapabilitySIM(ITelephony iTelephony) {
		// return iTelephony.get3GCapabilitySIM();
		return -1;
	}

	public static boolean set3GCapabilitySIM(ITelephony iTelephony, int simId) {
		// return iTelephony.set3GCapabilitySIM(int simId);
		return false;
	}

	// Gionee <fengjianyi><2013-03-13> modify for CR00779348 start
	public static boolean isRadioOnGemini(ITelephony iTelephony,
			int currentSlotId) throws RemoteException {
	    ITelephony iTelephonyService = ITelephony.Stub.asInterface(
                ServiceManager.checkService(Context.TELEPHONY_SERVICE));
        if (iTelephonyService == null) {
            return false;
        }
        return iTelephonyService.isRadioOnForSubscriber(SubscriptionManager.getSubIdUsingPhoneId(currentSlotId));
	}

	// Gionee <fengjianyi><2013-03-13> modify for CR00779348 end

	public static boolean is3GSwitchLocked(ITelephony iTelephony) {
		// return iTelephony.is3GSwitchLocked();
		return false;
	}

	public static boolean isSimInsert(ITelephony iTelephony, int simId) {

		try {
			ITelephony iTelephonyService = ITelephony.Stub
					.asInterface(ServiceManager
							.checkService(Context.TELEPHONY_SERVICE));
			return iTelephonyService.hasIccCardUsingSlotId(simId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void setDataRoamingEnabledGemini(ITelephony iTelephony,
			boolean enable, int simId) {
		// iTelephony.setDataRoamingEnabledGemini(boolean enable, int simId);
	}

	public static void setRoamingIndicatorNeddedProperty(ITelephony iTelephony,
			boolean property1, boolean property2) {
		// iTelephony.setRoamingIndicatorNeddedProperty(boolean property1,
		// boolean property2);
	}

	public static void registerForSimModeChange(ITelephony iTelephony,
			IBinder binder, int what) {
		// iTelephony.registerForSimModeChange(IBinder binder, int what);
	}

	public static void unregisterForSimModeChange(ITelephony iTelephony,
			IBinder binder) {
		// iTelephony.unregisterForSimModeChange(IBinder binder);
	}

	// gionee jiaoyuan 20130319 modify CR00786550 start
	public static boolean hasIccCardGemini(ITelephony iTelephony, int slotId) {
		return telManager.hasIccCard(slotId);
	}

	// gionee jiaoyuan 20130319 modify CR00786550 end

	public static boolean isFDNEnabledGemini(ITelephony iTelephony, int slotId) {
		return false;
	}

	public static boolean isFDNEnabled(ITelephony iTelephony) {
		return false;
	}

	public static boolean isPhbReady(ITelephony iTelephony) {
		   boolean isPbReady = false;
	        try {
	        	final ITelephonyEx iPhb = ITelephonyEx.Stub.asInterface(ServiceManager
	                    .getService("phoneEx"));//aurora change zhouxiaobing 20140514
	            if (null == iPhb) {
	                return false;
	            }
	            isPbReady = iPhb.isPhbReady(SubscriptionManager.getDefaultSubId()); 
	        } catch (Exception e) {
	        }
	        return isPbReady;
	}

	public static boolean isPhbReadyGemini(ITelephony iTelephony, int slotId) {
	   boolean isPbReady = false;
        try {
        	final ITelephonyEx iPhb = ITelephonyEx.Stub.asInterface(ServiceManager
                    .getService("phoneEx"));//aurora change zhouxiaobing 20140514
            if (null == iPhb) {
                return false;
            }
            isPbReady = iPhb.isPhbReady(SubscriptionManager.getSubIdUsingPhoneId(slotId)); 
        } catch (Exception e) {
        }
        return isPbReady;
	}

	public static boolean isRejectAllVoiceCall(ITelephony iTelephony) {
		return false;
	}

	public static boolean isRejectAllVideoCall(ITelephony iTelephony) {
		return false;
	}

	public static boolean handlePinMmiGemini(ITelephony iTelephony,
			String dialString, int simId) {
		return false;
	}

	// Gionee <wangym><2013-0510> add for support CMDA card begin
	public static int getGeminiPhoneType(ITelephony iTelephony, int slotId) {
//		return telManager.getPhoneTypeFromProperty(slotId);
		return 1;
	}

	public static int getPhoneType(ITelephony iTelephony) {
		return telManager.getPhoneType();
	}

	// Gionee <wangym><2013-0510> add for support CMDA card end

	// aurora add zhouxiaobing 20131115 start
	public static int getCallStateGemini(int simId) {
		return telManager.getCallState(SubscriptionManager.getSubIdUsingPhoneId(simId));
	}

	public static int get3GCapabilitySIM() {
		// return iTelephony.get3GCapabilitySIM();
		return -1;
	}

	public static int getSimIndicatorState() {
		return 1;
	}

	public static int getSimIndicatorStateGemini(int simId) {
		return getSimIndicatorState();
	}

	// aurora add zhouxiaobing 20131115 end

}
