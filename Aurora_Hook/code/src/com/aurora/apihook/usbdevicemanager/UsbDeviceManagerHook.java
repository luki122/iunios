package com.aurora.apihook.usbdevicemanager;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.aurora.apihook.R;

public class UsbDeviceManagerHook {
	private final String TAG = "UsbDeviceManagerHook";

	public void after_updateUsbNotification(MethodHookParam param) {
		Log.v(TAG, "---after_updateUsbNotification----begin");

		Object parentClass = ClassHelper.getSurroundingThis(param.thisObject);
		NotificationManager mNotificationManager = (NotificationManager) ClassHelper
				.getObjectField(parentClass, "mNotificationManager");

		boolean mUseUsbNotification = ClassHelper.getBooleanField(parentClass,
				"mUseUsbNotification");
		Context mContext = (Context) ClassHelper.getObjectField(parentClass,
				"mContext");
		
		boolean mConnected = ClassHelper.getBooleanField(param.thisObject,
				"mConnected");
		String mCurrentFunctions = (String) ClassHelper.getObjectField(
				param.thisObject, "mCurrentFunctions");
		int mUsbNotificationId =  ClassHelper.getIntField(param.thisObject, "mUsbNotificationId");

		if (mNotificationManager == null || !mUseUsbNotification) {
			return;
		}
		
		int id = 0;
		Resources r = mContext.getResources();

		if (mConnected) {
			boolean mtp = (Boolean) ClassHelper.callStaticMethod(
					parentClass.getClass(), "containsFunction",
					mCurrentFunctions, UsbManager.USB_FUNCTION_MTP);
			boolean ptp = (Boolean) ClassHelper.callStaticMethod(
					parentClass.getClass(), "containsFunction",
					mCurrentFunctions, UsbManager.USB_FUNCTION_PTP);
			boolean mass = (Boolean) ClassHelper.callStaticMethod(
					parentClass.getClass(), "containsFunction",
					mCurrentFunctions, UsbManager.USB_FUNCTION_MASS_STORAGE);
			boolean accessory = (Boolean) ClassHelper.callStaticMethod(
					parentClass.getClass(), "containsFunction",
					mCurrentFunctions, UsbManager.USB_FUNCTION_ACCESSORY);

			if (mtp) {
				id = com.aurora.R.string.usb_mtp_notification_title;
			} else if (ptp) {
				id = com.aurora.R.string.usb_ptp_notification_title;
			} else if (mass) {
				id = com.aurora.R.string.usb_cd_installer_notification_title;
			} else if (accessory) {
				id = com.aurora.R.string.usb_accessory_notification_title;
			}
		}

		if (id != mUsbNotificationId) {
			// clear notification if title needs changing
			if (mUsbNotificationId != 0) {
				mNotificationManager.cancelAsUser(null, mUsbNotificationId,
						UserHandle.ALL);
				//mUsbNotificationId = 0;
				ClassHelper.setIntField(param.thisObject, "mUsbNotificationId",0);
			}
			if (id != 0) {
				CharSequence message = r
						.getText(com.aurora.R.string.usb_notification_message);
				CharSequence title = r.getText(id);

				Notification notification = new Notification();
				notification.icon = com.aurora.R.drawable.stat_sys_data_usb;
				notification.when = 0;
				notification.flags = Notification.FLAG_ONGOING_EVENT;
				notification.tickerText = title;
				notification.defaults = 0; // please be quiet
				if (com.aurora.R.string.usb_mtp_notification_title == id) {

					final Uri soundUri = Uri
							.parse("file:///system/media/audio/ui/USBConnect.ogg");
					if (soundUri != null) {
						notification.sound = soundUri;// USBConnect.ogg Uri
						final Ringtone sfx = RingtoneManager.getRingtone(
								mContext, soundUri);
						if (sfx != null) {
							sfx.setStreamType(AudioManager.STREAM_SYSTEM);
							sfx.play();
							Log.v(TAG,
									"---after_updateUsbNotification---play-----");
						}
					}
				} else {
					notification.sound = null;
				}
				notification.vibrate = null;
				notification.priority = Notification.PRIORITY_MIN;

				Intent intent = Intent
						.makeRestartActivityTask(new ComponentName(
								"com.android.settings",
								"com.android.settings.UsbSettings"));
				PendingIntent pi = PendingIntent.getActivityAsUser(mContext, 0,
						intent, 0, null, UserHandle.CURRENT);
				notification.setLatestEventInfo(mContext, title, message, pi);
				mNotificationManager.notifyAsUser(null, id, notification,
						UserHandle.ALL);
				//mUsbNotificationId = id;
				ClassHelper.setIntField(param.thisObject, "mUsbNotificationId",id);
			}
		}
		Log.v(TAG, "---after_updateUsbNotification----end");
	}

	public void after_updateAdbNotification(MethodHookParam param) {
		Log.v(TAG, "---after_updateAdbNotification----");
		Object parentClass = ClassHelper.getSurroundingThis(param.thisObject);
		NotificationManager mNotificationManager = (NotificationManager) ClassHelper
				.getObjectField(parentClass, "mNotificationManager");

		if (mNotificationManager == null)
			return;

		boolean mAdbEnabled = (Boolean) ClassHelper.getBooleanField(
				parentClass, "mAdbEnabled");
		Context mContext = (Context) ClassHelper.getObjectField(parentClass,
				"mContext");

		boolean mConnected = ClassHelper.getBooleanField(param.thisObject,
				"mConnected");
		boolean mAdbNotificationShown = ClassHelper.getBooleanField(
				param.thisObject, "mAdbNotificationShown");

		final int id = com.aurora.R.string.adb_active_notification_title;

		if (mAdbEnabled && mConnected) {
			if ("0".equals(SystemProperties.get("persist.adb.notify"))){
				return;
			}

			if (!mAdbNotificationShown) {
				Resources r = mContext.getResources();
				CharSequence title = r.getText(id);
				CharSequence message = r
						.getText(com.aurora.R.string.adb_active_notification_message);

				Notification notification = new Notification();
				notification.icon = com.aurora.R.drawable.stat_sys_adb;
				notification.when = 0;
				notification.flags = Notification.FLAG_ONGOING_EVENT;
				notification.tickerText = title;
				notification.defaults = 0; // please be quiet
				notification.sound = null;
				notification.vibrate = null;
				notification.priority = Notification.PRIORITY_LOW;

				Intent intent = Intent
						.makeRestartActivityTask(new ComponentName(
								"com.android.settings",
								"com.android.settings.DevelopmentSettings"));
				PendingIntent pi = PendingIntent.getActivityAsUser(mContext, 0,
						intent, 0, null, UserHandle.CURRENT);
				notification.setLatestEventInfo(mContext, title, message, pi);
				//mAdbNotificationShown = true;
				ClassHelper.setBooleanField(param.thisObject, "mAdbNotificationShown",true);
				mNotificationManager.notifyAsUser(null, id, notification,
						UserHandle.ALL);
			}
		} else if (mAdbNotificationShown) {
			//mAdbNotificationShown = false;
			ClassHelper.setBooleanField(param.thisObject, "mAdbNotificationShown",false);
			mNotificationManager.cancelAsUser(null, id, UserHandle.ALL);
		}
		Log.v(TAG, "---after_updateAdbNotification----end");
	}

}
