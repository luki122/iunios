package com.aurora.account.util;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.aurora.account.AccountApp;
import com.aurora.account.R;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.model.CountryCode;
import com.aurora.account.receiver.AutoBackupAlarmReceiver;
import com.aurora.account.receiver.StartSyncReceiver;

public class CommonUtil {
	
    private static final String TAG = "CommonUtil";
    
    /**自动同步的闹铃服务的req code*/
    private static final int ALARM_REQ_CODE_AUTO_SYNC = 1;
    /**恢复同步的闹铃服务的req code*/
    private static final int ALARM_REQ_CODE_RESUME_SYNC = 2;
    /**云相册请求同步的闹铃服务的req code**/
    private static final int ALARM_REQ_CODE_PHOTO_SYNC = 3;
    
	/**
	* @Title: getDefaultCountryCode
	* @Description: TODO 获取默认CountryCode对象
	* @param @param context
	* @param @return
	* @return CountryCode
	* @throws
	 */
	public static CountryCode getDefaultCountryCode(Context context) {
		CountryCode countryCode = new CountryCode();
		countryCode.setCode(context.getString(R.string.default_code));
		countryCode.setCountryOrRegions(context.getString(R.string.default_countryOrRegion));
		countryCode.setCountryOrRegionsCN(context.getString(R.string.default_countryOrRegionsCH));
		countryCode.setAbbreviation(context.getString(R.string.default_abbreviation));
		return countryCode;
	}
	
	/**设置自动同步的闹铃服务*/
	public static void setAutoSyncAlarm() {
	    AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(AccountApp.getInstance());
        if (pref.isAutoSyncEnabled()) {
            long triggerAtTime = getAutoSyncTriggerTime();
            Log.d(TAG, "Jim, setAutoSyncAlarm, triggerAtTime: " + pref.getAutoSyncTime());
            AlarmManager alarmManager = (AlarmManager) AccountApp.getInstance().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_AUTO_SYNC,
                    new Intent(StartSyncReceiver.ACTION), PendingIntent.FLAG_NO_CREATE);
            if (pi != null) {
                alarmManager.cancel(pi);
                Log.d(TAG, "Jim, original auto sync pi is not null, cancel it.");
            } else {
                pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_AUTO_SYNC,
                        new Intent(StartSyncReceiver.ACTION), 0);
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, 24 * 3600 * 1000, pi);
        }
    }
	
	/**
	 * 设置恢复同步的闹铃服务
	 */
	public static void setResumeSyncAlarm() {
	    long triggerTime = getResumeSyncTriggerTime(12); // 12小时以后恢复同步
	    Log.d(TAG, "Jim, setResumeSyncAlarm, triggerTime: " + triggerTime);
        AlarmManager alarmManager = (AlarmManager) AccountApp.getInstance().getSystemService(Context.ALARM_SERVICE);
        Intent resumeSyncIntent = new Intent(StartSyncReceiver.ACTION);
        resumeSyncIntent.putExtra(StartSyncReceiver.PARAMS_RESUME_SYNC, true);
        PendingIntent pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_RESUME_SYNC,
                resumeSyncIntent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            alarmManager.cancel(pi);
            Log.d(TAG, "Jim, original resume sync pi is not null, cancel it.");
        } else {
            pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_RESUME_SYNC,
                    resumeSyncIntent, 0);
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
	}
	
	public static void clearResumeSyncAlarm() {
	    AlarmManager alarmManager = (AlarmManager) AccountApp.getInstance().getSystemService(Context.ALARM_SERVICE);
	    Intent resumeSyncIntent = new Intent(StartSyncReceiver.ACTION);
        resumeSyncIntent.putExtra(StartSyncReceiver.PARAMS_RESUME_SYNC, true);
        PendingIntent pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_RESUME_SYNC,
                resumeSyncIntent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            alarmManager.cancel(pi);
        }
	}
	
	/**
	 * 获取恢复同步的触发时间
	 * @param extraHours
	 * @return
	 */
	private static long getResumeSyncTriggerTime(int extraHours) {
	    Calendar c = Calendar.getInstance();
//	    c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 10); // 测试用，10秒后恢复同步
	    c.set(Calendar.SECOND, 00);
        c.set(Calendar.MILLISECOND, 00);
        c.add(Calendar.HOUR_OF_DAY, extraHours);
                
        return c.getTimeInMillis();
	}
    
    /**
     * 获取自动同步的触发时间
     * @return
     */
    private static long getAutoSyncTriggerTime() {
        Calendar c = Calendar.getInstance();
        int[] times = TimeUtils.getTime(AccountPreferencesUtil.getInstance(AccountApp.getInstance()).getAutoSyncTime());
        c.set(Calendar.HOUR_OF_DAY, times[0]);
        c.set(Calendar.MINUTE, times[1]);
        c.set(Calendar.SECOND, 00);
        c.set(Calendar.MILLISECOND, 00);
        
        long tiggerTime = c.getTimeInMillis();
        long todayTime = System.currentTimeMillis();
        if (tiggerTime < todayTime){//触发时间已经过去
            while (true) {
                c.add(Calendar.DAY_OF_MONTH, 1);
                tiggerTime = c.getTimeInMillis();
                if (tiggerTime > todayTime) {
                    break;
                }
            }
        }
        
        return tiggerTime;
    }
    
    /**
     * 获取自动备份的触发时间
     * @return
     */
    private static long getAutoBackupTriggerTime(int index) {
    	Calendar c = Calendar.getInstance();
        int[] times = TimeUtils.getTime(AccountPreferencesUtil.getInstance(AccountApp.getInstance()).getAutoBackupTime());
        c.set(Calendar.HOUR_OF_DAY, times[0]);
        c.set(Calendar.MINUTE, times[1]);
        c.set(Calendar.SECOND, 00);
        c.set(Calendar.MILLISECOND, 00);
        
        // 错开时间
        c.add(Calendar.MINUTE, index * Globals.AUTO_BACKUP_DELAY_TIME);
        
        long tiggerTime = c.getTimeInMillis();
        long todayTime = System.currentTimeMillis();
        if (tiggerTime < todayTime){//触发时间已经过去
            while (true) {
                c.add(Calendar.DAY_OF_MONTH, 1);
                tiggerTime = c.getTimeInMillis();
                if (tiggerTime > todayTime) {
                    break;
                }
            }
        }
        
        return tiggerTime;
    }
    
    
    /**
     * @param context
     * @param edit
     */
	public static void hideSoftInput(Context context,EditText edit){
		if(context == null || edit == null){
			return ;
		}
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}
	
	/**
	 * @param context
	 * @param edit
	 */
	public static void showSoftInput(Context context,EditText edit){
		if(context == null || edit == null){
			return ;
		}
		
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(edit, 0);
	}
	
	public static void showSoftInputDelay(Context context, final EditText edit, long delay) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) edit
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(edit, 0);
			}
		}, delay);
	}
	
	public static int getResInteger(int integerResId) {
	    return AccountApp.getInstance().getResources().getInteger(integerResId);
	}
	
	/**
	* @Title: hidePhoneNum
	* @Description: TODO 隐藏手机号中间部分，如18*******5
	* @param @param phoneNum
	* @param @return
	* @return String
	* @throws
	 */
	public static String hidePhoneNum(String phoneNum) {
		if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() > 2) {
			StringBuffer sb = new StringBuffer();
			sb.append(phoneNum.substring(0, 2));
			sb.append("******");
			sb.append(phoneNum.charAt(phoneNum.length() - 1));
			return sb.toString();
		}
		return phoneNum;
	}
	
	/**
	* @Title: hideEmail
	* @Description: TODO 隐藏email中间部分，如zzyan387@163.com
	* @param @param email
	* @param @return
	* @return String
	* @throws
	 */
	public static String hideEmail(String email) {
		int index = email.indexOf('@');
		if (index != -1) {
			StringBuffer sb = new StringBuffer();
			sb.append(email.charAt(0));
			sb.append("******");
			if (index > 1) {
				sb.append(email.substring(index - 1, email.length()));
			} else {
				sb.append(email.substring(index, email.length()));
			}
			return sb.toString();
		}
		return email;
	}
	
	//================================== 同步通知栏相关 ==================================//
	
	// 通知栏组给的ID号
	private static final int SYNC_NOTI_ID = 1000;
	// 是否显示同步通知的功能开关
	private static final boolean showSyncNotificationSwitch = false;
	
	public static void showSyncNotification(Context context) {
		if (!showSyncNotificationSwitch) {
			return;
		}
		
		FileLog.i(TAG, "in showSyncNotification()");
		
		if (context == null) {
			return;
		}
		// Log.d("0203", "sdk  = " + android.os.Build.VERSION.SDK);
		// Intent openintent = new
		// Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		// openintent, 0);//当点击消息时就会向系统发送openintent意图
		Notification notification = new NotificationCompat.Builder(context)
		// .setLargeIcon(icon)
				.setSmallIcon(R.drawable.aurora_systemui_aurora_sock_anim)
				// .setTicker(null)
				// .setContentTitle("已连接USB调试")
				// .setSubText("界面简洁，资源丰富")
				// .setContentText("触摸可停用USB调试")
				// .setNumber(++messageNum)
				.setAutoCancel(true)
				// .setDefaults(Notification.DEFAULT_ALL)
				// .setContentIntent(contentIntent)
				.build();
		notification.defaults = Notification.DEFAULT_LIGHTS;
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(SYNC_NOTI_ID, notification);
	}
    
	public static void cancelSyncNotificaiton(Context context) {
		if (!showSyncNotificationSwitch) {
			return;
		}
		
		FileLog.i(TAG, "in cancelSyncNotificaiton()");
		
		if (context == null) {
			return;
		}
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(SYNC_NOTI_ID);
	}
	
	//================================== 同步通知栏相关 ==================================//
	
	//================================== 备份相关 ==================================//
	
	// 是否显示同步通知的功能开关
	private static final boolean autoBackSwitch = true;
	
	/**
	* @Title: clearPhotoBackupAlarm
	* @Description: TODO 清除已设定的alarm服务
	* @param 
	* @return void
	* @throws
	 */
	public static void clearAutoBackupAlarm() {
		Log.d(TAG, "clearAutoBackupAlarm() called! autoBackSwitch: " + autoBackSwitch);
		if (!autoBackSwitch) {
			return;
		}
		
		clearPhotoBackupAlarm();
        
        // 备忘录等相关
        //
        //
        
        Log.d(TAG, "Xiaobin, clearAutoBackupAlarm");
	}
	
	/**
	* @Title: checkAndSetPhotoBackupAlarm
	* @Description: TODO 检查并设置定时备份alarm服务
	* @param 
	* @return void
	* @throws
	 */
	public static void checkAndSetAppBackupAlarm() {
		Log.d(TAG, "checkAndSetAppBackupAlarm() called! autoBackSwitch: " + autoBackSwitch);
		if (!autoBackSwitch) {
			return;
		}
		
		clearAutoBackupAlarm();
		
		int index = 0;
		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(AccountApp.getInstance());
		for (AppConfigInfo app : apps) {
			
			if (app.isApp_syncself() && app.isSync()) {	// 是否打开了开关
				
				if (app.getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) {
					setPhotoBackupAlarm(index);
				} 
//				else if (app.getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) { 	// 备忘录
//					
//				}
				index++;
			}
		}
	}
	
	/**
	* @Title: setPhotoBackupAlarm
	* @Description: TODO 设置备份相册alarm
	* @param @param index 第几个备份（时间会错开）
	* @return void
	* @throws
	 */
	public static void setPhotoBackupAlarm(int index) {
		long triggerAtTime = getAutoBackupTriggerTime(index);
		
		AlarmManager alarmManager = (AlarmManager) AccountApp.getInstance().getSystemService(Context.ALARM_SERVICE);
		
		PendingIntent pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_PHOTO_SYNC,
          new Intent(AutoBackupAlarmReceiver.PHOTO_ACTION), PendingIntent.FLAG_NO_CREATE);
		if (pi != null) {
    	  alarmManager.cancel(pi);
    	  Log.d(TAG, "Xiaobin, original photobackup pi is not null, cancel it.");
		} else {
    	  pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_AUTO_SYNC,
              new Intent(AutoBackupAlarmReceiver.PHOTO_ACTION), 0);
		}
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, 24 * 3600 * 1000, pi);
		
		Log.d(TAG, "Xiaobin, startPhotoBackup, triggerAtTime: " + getAutoBackupTriggerTime(index));
	}
	
	public static void clearPhotoBackupAlarm() {
		AlarmManager alarmManager = (AlarmManager) AccountApp.getInstance().getSystemService(Context.ALARM_SERVICE);
		
		PendingIntent pi = PendingIntent.getBroadcast(AccountApp.getInstance(), ALARM_REQ_CODE_PHOTO_SYNC,
		          new Intent(AutoBackupAlarmReceiver.PHOTO_ACTION), PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            alarmManager.cancel(pi);
        }
        
        Log.d(TAG, "Xiaobin, clearPhotoBackupAlarm");
	}
	
	/**
	* @Title: checkPhotoBackupHasBind
	* @Description: 检查当前账户是否已绑定百度云
	* @param @param context
	* @param @return
	* @return boolean
	* @throws
	 */
	public static boolean checkPhotoBackupHasBind(Context context) {
		boolean hasBind = false;

		try {
			Uri BINDACCOUNTPROVIDERPROVIDER_URI = Uri.parse("content://com.android.gallery3d.BindAccountProvider");
			
			Bundle mBundle = new Bundle();
			ContentResolver mContentResolver = context.getContentResolver();
			
			mBundle = mContentResolver.call(BINDACCOUNTPROVIDERPROVIDER_URI, "",
					"", null);
			
			if (mBundle != null) {
				hasBind = mBundle.getBoolean("ACCOUNT_BIND");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasBind;
	}
	
	/**
	* @Title: startPhotoBackupBind
	* @Description: TODO 打开云相册绑定百度云
	* @param @param context
	* @return void
	* @throws
	 */
	public static void startPhotoBackupBind(Context context) {
		Intent intent = new Intent("com.android.gallery3d.xcloudalbum.BIndAccountActivity");    
		context.startActivity(intent); 
	}
	
	//================================== 备份相关 ==================================//
	
}
