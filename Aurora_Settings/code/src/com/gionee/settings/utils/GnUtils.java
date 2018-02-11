package com.gionee.settings.utils;

import android.content.Context;
import android.util.Log;

//Aurora <likai> <2013-10-19> modify begin
//import android.provider.Telephony.SIMInfo;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.os.storage.GnStorageManager;
//Aurora <likai> <2013-10-19> modify end

import java.util.List;

//Gionee <chenml> <2013-07-25> modify for CR00839374 begin 
import android.os.storage.IMountService;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
//Gionee <chenml> <2013-07-25> modify for CR00839374 end 
import com.android.settings.Settings;
import android.os.SystemProperties;


public class GnUtils {
    private static final String TAG = "GnUtils";
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 begin
    private static boolean GnGeminiSupport = SystemProperties.get("ro.gn.gemini.support", "no").equals("yes");
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 end
    
    //add by jiyouguang for judge whether abroad version
    private static boolean AuroraAbroadVer = "true".equals(SystemProperties.get("phone.type.oversea"));
    //end jiyouguang
    
    public static boolean isSimManagementAvailable(Context context) {
        int isInternetCallEnabled = 
                android.provider.Settings.System.getInt(
                        context.getContentResolver(), 
                        gionee.provider.GnSettings.System.ENABLE_INTERNET_CALL, 0);
        Log.i(TAG, " isInternetCallEnabled = " + isInternetCallEnabled);
        boolean isVoipSupported = 
                (android.net.sip.SipManager.isVoipSupported(context)) && (isInternetCallEnabled != 0);

        // Aurora <likai> <2013-10-19> modify begin
        //boolean isHasSimCards = 
        //       (android.provider.Telephony.SIMInfo.getInsertedSIMCount(context) != 0);
        boolean isHasSimCards = 
                (SIMInfo.getInsertedSIMCount(context) != 0);
        // Aurora <likai> <2013-10-19> modify end

        Log.i(TAG, " isVoipSupported = " + isVoipSupported);
        Log.i(TAG, " isHasSimCards = " + isHasSimCards);
        
        return isHasSimCards || isVoipSupported;
    }
	
    public static int getSimNum(Context context){    
        List<SIMInfo> simList = SIMInfo.getInsertedSIMList(context);
	 int mSimNum = simList.size();
	 return mSimNum;
    }
    //Gionee <chenml> <2013-07-25> modify for CR00839374 begin 
    public static boolean isExSdcardInserted(Context context) {
        /*IBinder service = ServiceManager.getService("mount");
        Log.d(TAG, "Util:service is " + service);

        if (service != null) {
            IMountService mountService = IMountService.Stub.asInterface(service);
            Log.d(TAG, "Util:mountService is " + mountService);
            if (mountService == null) {
                return false;
            }
            try {
                return mountService.isSDExist();
            } catch (RemoteException e) {
                Log.d(TAG, "Util:RemoteException when isSDExist: " + e);
                return false;
            }
        } else {
            return false;
        }*/
        return GnStorageManager.getInstance(context).isSDExist_ex();
    }	
    //Gionee <chenml> <2013-07-25> modify for CR00839374 end 
    public static void setSettingsmkey(String key) {
        Settings.mKey = key;
    }
    public static boolean isDoubleCard(){
        return GnGeminiSupport;
    }
    
    public static boolean isAbroadVersion() {
    	
    	//Log.d("111", "isAbroadVersion :   " + "true".equals(SystemProperties.get("phone.type.oversea")));
     	return AuroraAbroadVer;
    	//return true;
    }
}
