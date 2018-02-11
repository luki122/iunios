package com.android.settings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.UserManager;
import android.os.IHardwareService;

public class PlatformUtils {

	// -----Notification Access Begin----//
	public boolean isSupportNotification() {
		return false;
	}

	public int getListenersCount(PackageManager pPm) {
		return 0;
	}

	public int getNumEnabledNotificationListeners(Context pContext) {

		return 0;
	}

	// -----Notification Access End----//

	// Begin Add by gary.gou--SecuritySettings.java ----Credential storage
	public static boolean isAddCredentialStorage() {
		return false;
	}

	public static boolean isUserManager_HasUserRestriction(Context context) {
		return false;
	}

	// End Add by gary.gou---SecuritySettings.java ----Credential storage

	public static boolean isSupportDefLSAppChioce() {
		return false;
	}

	public static void defLSAppChioce(Context mContext) {
	}


       public static boolean isSupportScreenAndButtonOn(){
           return false;
       }         
 
        public static void setButtonLightEnabled(IHardwareService light){
                 
        }
}
