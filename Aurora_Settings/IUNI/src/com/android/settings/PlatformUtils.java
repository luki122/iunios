package com.android.settings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.UserManager;
import android.service.notification.NotificationListenerService;

import android.app.ActivityManager;
import android.util.Log;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.Intent;
import android.content.ComponentName;
import android.os.IHardwareService;

public class PlatformUtils {

	private  final static String TAG = "PlatformUtils";
	
	// -----Notification Access Begin----//
	public static boolean isSupportNotification() {
		return true;
	}

	public int getListenersCount(PackageManager pPm) {
		return NotificationAccessSettings.getListenersCount(pPm);
	}

	public int getNumEnabledNotificationListeners(Context pContext) {
		final String flat = Settings.Secure.getString(
				pContext.getContentResolver(),
				Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
		if (flat == null || "".equals(flat))
			return 0;
		final String[] components = flat.split(":");
		return components.length;
	}

	// -----Notification Access End----//

	// Begin Add by gary.gou--SecuritySettings.java ----Credential storage
	public static boolean isAddCredentialStorage() {
		return true;
	}

	public static boolean isUserManager_HasUserRestriction(Context context) {
		UserManager um = (UserManager) context
				.getSystemService(Context.USER_SERVICE);
		return !um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS);
	}

	// End Add by gary.gou---SecuritySettings.java ----Credential storage


       public static boolean isSupportScreenAndButtonOn(){
           return false;
       }         
 
        public static void setButtonLightEnabled(IHardwareService light){
                 
        }
       
	public static boolean isSupportDefLSAppChioce() {
		return true;
	}

	 public static  void defLSAppChioce(Context mContext) {
		PackageManager mPM = mContext.getPackageManager();
		final int user = ActivityManager.getCurrentUser();
		List<ResolveInfo> installedServices = mPM.queryIntentServicesAsUser(
				new Intent(NotificationListenerService.SERVICE_INTERFACE),
				PackageManager.GET_SERVICES | PackageManager.GET_META_DATA,
				user);

		for (int i = 0, count = installedServices.size(); i < count; i++) {
			ResolveInfo resolveInfo = installedServices.get(i);

			ServiceInfo info = resolveInfo.serviceInfo;
			if (!android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
					.equals(info.permission)) {
				Log.w(TAG,
						"Skipping notification listener service "
								+ info.packageName
								+ "/"
								+ info.name
								+ ": it does not require the permission "
								+ android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
				continue;
			}
			if (info.packageName.equals("com.android.keyguard")) {
				final ComponentName cn = new ComponentName(info.packageName,
						info.name);
				StringBuilder sb = new StringBuilder();
				sb.append(cn.flattenToString());
				Settings.Secure.putString(mContext.getContentResolver(),
						Settings.Secure.ENABLED_NOTIFICATION_LISTENERS,
						sb != null ? sb.toString() : "");
			}
		}
	}
}
