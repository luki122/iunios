package com.aurora.android.contacts;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
// Gionee:wangth 20130228 add for CR00773823 begin
import java.io.IOException;
//import com.android.qualcomm.qcnvitems.QcNvItems;
// Gionee:wangth 20130228 add for CR00773823 end
import android.util.Log;
import com.android.internal.telephony.ITelephony;
//import com.android.internal.telephony.IVideoTelephony;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;

public class AuroraTelephonyManager {
    //Gionee guoyx 20130223 add for Qualcomm solution CR00773050 begin
    /** SIM card state: SIM Card Deactivated, only the sim card is activated,
     *   we can get the other sim card state(eg:ready, pin lock...)
     *@hide
     */
     public static int SIM_STATE_DEACTIVATED = 0x0A;
   //Gionee guoyx 20130223 add for Qualcomm solution CR00773050 end
     
     // Gionee:wangth 20130228 add for CR00773823 begin
//     private static QcNvItems mQcNvItems = new QcNvItems();
     // Gionee:wangth 20130228 add for CR00773823 end

    private static AuroraTelephonyManager mInstance = new AuroraTelephonyManager();
    //Gionee guoyx 20130218 add for CR00766605 begin
    //This is use for Quclomm solution if in the MTK solution return the value false. 
    public static boolean isMultiSimEnabled() {
        return telManager.isMultiSimEnabled();
    }
	//Gionee guoyx 20130218 add for CR00766605 end
    
    public static AuroraTelephonyManager getDefault() {
        return mInstance;
    }

    public static boolean hasIccCardGemini(int subscription) {
        return telManager.hasIccCard(subscription);
    }
    
    private static TelephonyManager telManager = TelephonyManager.getDefault();

    public static String getLine1Number() {
        return telManager.getLine1Number();
    }

    public static boolean hasIccCard() {
        return telManager.hasIccCard();
    }

    public static int getDataState() {
        return telManager.getDataState();
    }

    public static String getLine1NumberGemini(int slotId) {
//        return MmsApp.getApplication().getTelephonyManager().getLine1NumberGemini(slotId);
        return telManager.getLine1NumberForSubscriber(SubscriptionManager.getSubIdUsingPhoneId(slotId)); //guoyx 20130116
    }
    
    public static boolean isNetworkRoamingGemini(int slotId) {
        return telManager.isNetworkRoaming(SubscriptionManager.getSubIdUsingPhoneId(slotId));
    }

    public static int getDataStateGemini(int simId) {
        try {
            return telManager.getDataState(SubscriptionManager.getSubIdUsingPhoneId(simId));
        } catch (Exception ex) {
            // the phone process is restarting.
            return telManager.DATA_DISCONNECTED;
        }
    }
    
    public static void listenGemini(PhoneStateListener listener, int state, int simId) {
//        ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE))
//                .listenGemini(listener, state, simId);
      //Gionee guoyx 20130201 add for MTK-QC compile pass CR00766605 begin
 //       listener.SetListenerSub(simId);
      //Gionee guoyx 20130201 add for MTK-QC compile pass CR00766605 end
        listenGemini(listener, state);
    }
    
    public static void listenGemini(PhoneStateListener listener, int state) {
        telManager.listen(listener,state);
    }
    
    public static int getCallStateGemini(int subscription) {
        return telManager.getCallState(SubscriptionManager.getSubIdUsingPhoneId(subscription));
    }

    public static int getSimStateGemini(int currentSlotId) {
        return telManager.getSimState(currentSlotId);
    }

    //--------add for contacts--------
    private static String mResultStr = "gionee";
    
    public static String getVoiceMailNumberGemini(int slotId) {
        return mResultStr;
    }
    
    public static String getDeviceIdGemini(int simId) {
        return telManager.getDeviceId(simId); //mResultStr; //guoyx 20130116
    }
    public static String getSN(Context context) {
        // Gionee:wangth 20130228 modify for CR00773823 
        return null;
        //return null;
        // Gionee:wangth 20130228 modify for CR00773823 end
    }    
    public static String getSN() {
        // Gionee:wangth 20130228 modify for CR00773823 
 /*       String factoryResult = mResultStr;
        try {
            factoryResult = mQcNvItems.getFactoryResult();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }
        */
        return null;//factoryResult;
        // Gionee:wangth 20130228 modify for CR00773823 end
    }
    
    public static String getNetworkOperatorGemini(int slotId) {
        return telManager.getNetworkOperatorForPhone(slotId);
    }

    public static String getSubscriberIdGemini(int slotId) {
        return telManager.getSubscriberId(SubscriptionManager.getSubIdUsingPhoneId(slotId));
    }
    
    public static String getSimOperatorGemini(int slotId) {
        return telManager.getSimOperator(SubscriptionManager.getSubIdUsingPhoneId(slotId));
    }

    public static boolean isValidSimState(int slotId) {
        return false;//telManager.isValidSimState();
    }
    
    public static int getIccSubSize(int slotId) {
        return 0;//TelephonyManager.getDefault().getIccSubSize();
    }

    public static int getSIMStateIcon(int simStatus) {
        return -1;
    }
    
    // Gionee:wangth 20130329 add for CR00791133 begin
    public static boolean isVTCallActive() {
        boolean isVTActive = false;
        
/*        try {
            if (isMultiSimEnabled()) {
                IVideoTelephony vtCall = IVideoTelephony.Stub.asInterface(ServiceManager
                        .checkService("videophone"));

                if (vtCall != null) {
                    if (!vtCall.isVtIdle()) {
                        isVTActive = true;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.w("AuroraTelephonyManager", "isVTActive() failed", e);
        }
*/
        return isVTActive;
    }
    
    public static void showCallScreenWithDialpad(boolean show) {
        Log.w("AuroraTelephonyManager", "   show. " + show);
        ITelephony telephonyServiceMSim = ITelephony.Stub
                .asInterface(ServiceManager.checkService("phone"));
        if (telephonyServiceMSim == null) {

            return;
        } else {
    //        try {

   //             telephonyServiceMSim.showCallScreenWithDialpad(show);
    //        } catch (RemoteException e) {

    //        }
        }
    }
    // Gionee:wangth 20130329 add for CR00791133 end

//aurora add zhouxiaobing 20131115 start
public int getNetworkTypeGemini(int simId)
{

    return telManager.getNetworkType(SubscriptionManager.getSubIdUsingPhoneId(simId));
}


//aurora add zhouxiaobing 20131115 end
//aurora add zhouxiaobing 20131115 end
public static int getPhoneCount()
{
	
   return telManager.getPhoneCount();	
}
//aurora add zhouxiaobing 20140521 end

	public static int getDefaultSubscription(Context context) {
		return SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultVoicePhoneId());
	}
	
	public static int getPreferredDataSubscription(Context context) {
		return SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubId());
	}
	
	public static String getNetworkOperatorNameGemini(int simId) {
		return telManager.getNetworkOperatorName(SubscriptionManager.getSubIdUsingPhoneId(simId));
	}
	
	public static boolean isSimClosed(Context context, int slot) {
        return false;
	}
} 
