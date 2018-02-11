/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;


import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.sax.StartElementListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.os.ServiceManager;

import com.android.deskclock.Alarms;
import com.android.deskclock.R;
import com.android.internal.telephony.ITelephony;
import android.os.SystemProperties;
import android.provider.Settings;
//android:zjy 20110721 add for CR00576747 start 
import aurora.preference.AuroraPreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import android.widget.Toast;
//android:zjy 20110721 add for CR00576747 end
//Gionee <baorui><2013-07-12> modify for CR00835747 begin

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 30 * 60 * 1000;
    private ITelephony mTelephonyService;
    private static final int GIMINI_SIM_1 = 0;
	private static final int GIMINI_SIM_2 = 1;
	private boolean mIsOp01;
	private static final String ALARM_PHONE_LISTENER = "com.android.deskclock.ALARM_PHONE_LISTENER";
    // Gionee <baorui><2013-04-11> modify for CR00792291 begin
    private final static String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";
    // Gionee <baorui><2013-04-11> modify for CR00792291 end
    
    private final static String ALARM_FORUNLOCK = "com.aurora.lancher.start.anim";
    private final static String ALARM_WAKEUP_NOTALARM = "alarm_notalarm";
    public static boolean is_wakeup_noalarm = false;
    public static int wakeupAlarmId = -1;
    private static boolean isfirstUnlock = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {
    	Calendar cal=Calendar.getInstance();
    	android.util.Log.e("jadon", "接收到广播："+intent.getAction()+"当前时间是="+(cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)));
        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                handleIntent(context, intent);
                result.finish();
                wl.release();
            }
        });
    }
    
    /**
     * @param context add by tangjun 2013.12.27
     */
    private void setGetupNotification( Context context ) {
    	
    	SharedPreferences sharedPreferences= context.getSharedPreferences("getupnotification",Context.MODE_PRIVATE);
   	 	Editor editor = sharedPreferences.edit();
   	 	
    	Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        
        int counttime = hour*60 + minutes;
    	
		isfirstUnlock = sharedPreferences.getBoolean("isfirstUnlock", true);
    	Log.e("---isfirstUnlock = " + isfirstUnlock);
    	if ( !isfirstUnlock && (counttime >= 270 && counttime <= 630) ) {
    		return;
    	}
    	
        Log.e("setGetupNotification--hour = " + hour);
        Log.e("setGetupNotification--minutes = " + minutes);
        //between 4:30 and 10:30
        if ( counttime < 270 || counttime > 630 ) {
        	isfirstUnlock = true;
        	editor.putBoolean("isfirstUnlock", isfirstUnlock);
        	editor.commit();
        	return;
        }
        
    	Alarm alarm = null;
    	Alarm usealarm = null;
        Cursor cursor = Alarms.getSortedEnabledAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                    	alarm = new Alarm(cursor);
                        Log.e("setGetupNotification--a.hour = " + alarm.hour);
                        Log.e("setGetupNotification--a.minutes = " + alarm.minutes);
                        if (alarm.daysOfWeek.isRepeatSet() ) {
                        	int dis = alarm.hour*60 + alarm.minutes - (hour*60 + minutes);
                        	//xxj  计算下次响铃的天数，只有小于一天才弹提示
                        	long timeInMillis = Alarms.calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();                       	
                        	 long delta = timeInMillis - System.currentTimeMillis();
                             long hours = delta / (1000 * 60 * 60);                           
                             long days = hours / 24;                           
                        	Log.e("---dis = " + dis);
                        	if ( dis <= 30 && dis > 0&& days<1) {
                        		usealarm = alarm;
                        		break;
                        	}
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        
        if ( usealarm != null ) {
        	
        	//保存符合条件的闹钟 aurora mod by tangjun 2014.3.1
//        	wakeupAlarmId = usealarm.id;
        	
        	isfirstUnlock = false;
        	editor.putBoolean("isfirstUnlock", isfirstUnlock);
        	editor.commit();
        	
        	//发送通知
		    Intent cancelSnooze = new Intent(context,AlarmReceiver.class);
		    cancelSnooze.setAction(ALARM_WAKEUP_NOTALARM);
		    cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, usealarm);
		    PendingIntent broadcast =
		            PendingIntent.getBroadcast(context, usealarm.id, cancelSnooze, PendingIntent.FLAG_CANCEL_CURRENT);
		    NotificationManager nm = getNotificationManager(context);
		    Notification n = new Notification(R.drawable.stat_notify_alarm_white,
		            context.getString(R.string.notify_wakeup_notalarm), 0);
		    n.setLatestEventInfo(context, context.getString(R.string.notify_wakeup_notalarm),
		    		context.getString(R.string.notify_clicktoclosewakeupalarm), broadcast);
		    n.flags |= Notification.FLAG_AUTO_CANCEL
		            | Notification.FLAG_ONGOING_EVENT;
		    nm.notify(usealarm.id, n);
        }
    }
    
    /** 
     * 设置手机飞行模式 
     * @param context 
     * @param enabling true:设置为飞行模式 false:取消飞行模式 
     */  
    private void setAirplaneModeOff(Context context) { 
    	int isAirplaneMode = Settings.Global.getInt(context.getContentResolver(),  
                Settings.Global.AIRPLANE_MODE_ON, 0);
    	Log.e("------isAirplaneMode = --------------------" + isAirplaneMode);
    	//开启了飞行模式就关闭
    	if ( isAirplaneMode ==  1 ) {
    		Toast.makeText(context, context.getResources().getString(R.string.airplanemodeoff), Toast.LENGTH_SHORT).show();
	        Settings.Global.putInt(context.getContentResolver(),  
	                             Settings.Global.AIRPLANE_MODE_ON, 0);  
	        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
	        intent.putExtra("state", 0);  
	        context.sendBroadcast(intent);  
    	}
    } 
private static final String LAUNCH_PWROFF_ALARM = "android.intent.action.LAUNCH_POWEROFF_ALARM";
    private void handleIntent(Context context, Intent intent) {
    	Log.e("AlarmReceiver----handleIntent--intent.getAction() = " + intent.getAction());
    	
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (Alarm)
                    intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
        	
        	//点击通知栏取消闹钟时把贪睡次数也置1
        	AlarmAlertFullScreen.alarmTimes = 1;
        	
            Alarm alarm = null;
            if (intent.hasExtra(Alarms.ALARM_INTENT_EXTRA)) {
                // Get the alarm out of the Intent
                alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            }

            if (alarm != null) {
            	
                //通知栏关闭起床闹铃也关闭飞行模式
                int counttime  = alarm.hour*60 + alarm.minutes;
                //Log.e("--AlarmReceiver--alarm.id = -" + alarm.id);
                //Log.e("--AlarmReceiver--alarm.hour = -" + alarm.hour);
                //Log.e("--AlarmReceiver--alarm.minute = -" + alarm.minutes);
                //Log.e("--AlarmReceiver--counttime = ---" + counttime);
                //Log.e("--AlarmReceiver--isRepeat = ---" + alarm.daysOfWeek.isRepeatSet());
                if ( counttime >= 300 && counttime <= 660 && alarm.daysOfWeek.isRepeatSet() ) {
             	   setAirplaneModeOff(context);
                }
                Alarms.disableSnoozeAlert(context, alarm.id);
                Alarms.setNextAlert(context);
            } else {
                // Don't know what snoozed alarm to cancel, so cancel them all.  This
                // shouldn't happen
                	Log.wtf("Unable to parse Alarm from intent.");
                Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
            }
            return;
		} 
        //android:zjy 20120503 add for CR00576747  start 
        else if (Alarms.ALARM_REPEAT_RING.equals(intent.getAction())) {
        	settingAlarmDelayed(context, (Alarm)
                    intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA));
		}
        //android:zjy 20120503 add for CR00576747  end
        //aurora add by tangjun 2013.12.28 start  
        else if (ALARM_FORUNLOCK.equals(intent.getAction())) {
        	setGetupNotification(context);
        	return;
        } else if (ALARM_WAKEUP_NOTALARM.equals(intent.getAction())) {
        	Log.e("----------ALARM_WAKEUP_NOTALARM-----------------");
        	Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        	if(alarm!=null)
        	{
        		wakeupAlarmId = alarm.id;
        	}else{
        		return;
        	}
        	is_wakeup_noalarm = true;
        	setAirplaneModeOff(context);
            Alarms.setNextAlert(context);
        	return;
        } else if ( Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
        	Log.e("----------Intent.ACTION_DATE_CHANGED-----------------");
        	//isfirstUnlock = true;
        	return;
        }
        //aurora add by tangjun 2013.12.28 end
        else if (Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            showAlarmAlert(intent,context);
            return;
        }else if(LAUNCH_PWROFF_ALARM.equals(intent.getAction()))
        {
           
           return;
         }
        
    }

    private void showAlarmAlert(Intent intent,Context context){
    	 Alarm alarm = null;
         Log.e("----is_wakeup_noalarm1111 = " + is_wakeup_noalarm);
         // Grab the alarm from the intent. Since the remote AlarmManagerService
         // fills in the Intent to add some extra data, it must unparcel the
         // Alarm object. It throws a ClassNotFoundException when unparcelling.
         // To avoid this, do the marshalling ourselves.
 		if (intent.getBooleanExtra("setNextAlert", true)) {
 			final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
 			if (data != null) {
 				Parcel in = Parcel.obtain();
 				in.unmarshall(data, 0, data.length);
 				in.setDataPosition(0);
 				alarm = Alarm.CREATOR.createFromParcel(in);
 			}

 			if (alarm == null) {
 					Log.wtf("Failed to parse the alarm from the intent");
 				// Make sure we set the next alert if needed.
 				Alarms.setNextAlert(context);
 				return;
 			}

 			// Disable the snooze alert if this alarm is the snooze.
 			Alarms.disableSnoozeAlert(context, alarm.id);
 			// Disable this alarm if it does not repeat.
 			if (!alarm.daysOfWeek.isRepeatSet()) {
 				Alarms.enableAlarm(context, alarm.id, false);
 			} else {
 				// Enable the next alert if there is one. The above call to
 				// enableAlarm will call setNextAlert so avoid calling it twice.
 				Alarms.setNextAlert(context);
 			}
 		} else {
 			if (intent.hasExtra(Alarms.ALARM_INTENT_EXTRA)) {
 				// Get the alarm out of the Intent
 				alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
 			}
 		}
 		Calendar cal=Calendar.getInstance();
 		android.util.Log.e("jadon","当前时间是="+(cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND))
 				+"  闹铃时间是"+(alarm.hour+":"+alarm.minutes));
 		
 		Log.e("----is_wakeup_noalarm2222 = " + is_wakeup_noalarm);
 		android.util.Log.e("jadon", "wakeupAlarmId = "+wakeupAlarmId+"  is_wakeup_noalarm = "+is_wakeup_noalarm);
 		//aurora add by tangjun 2013.12.28 start
         if( is_wakeup_noalarm && alarm.daysOfWeek.isRepeatSet()&&wakeupAlarmId==alarm.id) {
         	Log.e("----111is_wakeup_noalarm = " + is_wakeup_noalarm);
         	wakeupAlarmId=-1;
         	is_wakeup_noalarm = false;
         	return;
         }
         //aurora add by tangjun 2013.12.28 end
 		
 		final String optr = SystemProperties.get("ro.operator.optr");
         mIsOp01 = (optr != null && optr.equals("OP01"));
 		if (mIsOp01) {
 			try {

                 int mCurrentCallState = -1;
                 //aurora mod by tangjun 2013.12.25 start 
                 /*
                 if (Alarms.mIsGnMtkPoweroffAlarmSupport) {
                     mTelephonyService = ITelephony.Stub.asInterface(ServiceManager
                             .getService(Context.TELEPHONY_SERVICE));
                     mCurrentCallState = mTelephonyService.getPreciseCallState();
                 } else {
                     TelephonyManager mTelephonyManager = (TelephonyManager) context
                             .getSystemService(Context.TELEPHONY_SERVICE);
                     mCurrentCallState = mTelephonyManager.getCallState();
                 }
                 */
                 TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                 mCurrentCallState = mTelephonyManager.getCallState();
                 //aurora mod by tangjun 2013.12.25 end 

 				if (mCurrentCallState != TelephonyManager.CALL_STATE_IDLE) {
 					Log.v("mCurrentCallState != TelephonyManager.CALL_STATE_IDLE and mCurrentCallState = "
 									+ mCurrentCallState);
 					Intent phoneListener = new Intent(ALARM_PHONE_LISTENER);
 					phoneListener.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
 					context.startService(phoneListener);
 					return;
 				}
 			} catch (Exception ex) {
 				Log.v("Catch exception when getPreciseCallState: ex = "
 						+ ex.getMessage());
 			}
 		}
         
         // Intentionally verbose: always log the alarm time to provide useful
         // information in bug reports.
         long now = System.currentTimeMillis();
         	Log.v("Recevied alarm set for " + Log.formatTime(alarm.time));

         // Always verbose to track down time change problems.
         if (now > alarm.time + STALE_WINDOW) {
         		Log.v("Ignoring stale alarm");
             return;
         }

         // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
         // pick it up.
         AlarmAlertWakeLock.acquireCpuWakeLock(context);

         /* Close dialogs and window shade */
         Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
         context.sendBroadcast(closeDialogs);

         // Decide which activity to start based on the state of the keyguard.
         Class c = AlarmAlert.class;
         KeyguardManager km = (KeyguardManager) context.getSystemService(
                 Context.KEYGUARD_SERVICE);
         if (km.inKeyguardRestrictedInputMode()) {
             // Use the full screen activity for security.
             c = AlarmAlertFullScreen.class;
         }
         // Gionee <baorui><2013-04-11> modify for CR00792291 begin
         else if (!km.inKeyguardRestrictedInputMode() && Alarms.bootFromPoweroffAlarm()
                 && !intent.getBooleanExtra(POWER_OFF_FROM_ALARM, false)) {
             c = AlarmAlertFullScreen.class;
         }
         // Gionee <baorui><2013-04-11> modify for CR00792291 end

         // Play the alarm alert and vibrate the device.
         Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
         playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         playAlarm.setPackage(context.getPackageName()); //cjs add
         context.startService(playAlarm);
         // Gionee baorui 2012-09-06 modify for CR00683131 begin
         Alarms.mIfDismiss = true;
         // Gionee baorui 2012-09-06 modify for CR00683131 end
         // Gionee baorui 2012-09-17 modify for CR00689742 begin
         Alarms.mAlarmId = alarm.id;
         // Gionee baorui 2012-09-17 modify for CR00689742 end

         // Trigger a notification that, when clicked, will show the alarm alert
         // dialog. No need to check for fullscreen since this will always be
         // launched from a user action.
         Intent notify = new Intent(context, c);//AlarmAlert.class);
         notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         
         Log.e("111----c = " + c.toString());
         
         //lable rename jiating 201206151132
         PendingIntent pendingNotify = PendingIntent.getActivity(context,
                 alarm.id, notify,  PendingIntent.FLAG_UPDATE_CURRENT);

         // Use the alarm's label or the default label as the ticker text and
         // main text of the notification.
         String label = alarm.getLabelOrDefault(context);
 		Notification n = new Notification(R.drawable.stat_notify_alarm_white,label, alarm.time);
        
         n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_text),
                 pendingNotify);
         n.flags |= Notification.FLAG_SHOW_LIGHTS
                 | Notification.FLAG_ONGOING_EVENT;
         n.defaults |= Notification.DEFAULT_LIGHTS;

         // NEW: Embed the full-screen UI here. The notification manager will
         // take care of displaying it if it's OK to do so.
         Intent alarmAlert = new Intent(context, c);
         alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
         alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                 | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
         alarmAlert.setPackage(context.getPackageName()); //cjs add
         context.startActivity(alarmAlert);
       //lable rename jiating 201206151132
         n.fullScreenIntent = PendingIntent.getActivity(context, alarm.id, alarmAlert, PendingIntent.FLAG_UPDATE_CURRENT);

         // Send the notification using the alarm id to easily identify the
         // correct notification.
         NotificationManager nm = getNotificationManager(context);
         nm.cancel(alarm.id);
         nm.notify(alarm.id, n);
    }





    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {
                Log.v("Cannot update notification for killer callback");
            return;
        }

        // Launch SetAlarm when clicked.
        Intent viewAlarm = new Intent(context, AuroraSetAlarm.class);
        viewAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent intent =
                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

        // Update the notification to indicate that the alert has been
        // silenced.
        String label = alarm.getLabelOrDefault(context);
		
		Notification n = new Notification(R.drawable.stat_notify_alarm_white,label, alarm.time);

        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_alert_alert_silenced, timeout),
                intent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        nm.cancel(alarm.id);
        nm.notify(alarm.id, n);
    }
    
    //aurora mod by tangjun 2013.12.27 一分钟后发送延时闹铃的广播 settingAlarmDelayed
    //android:zjy 20120503 add for CR00576747  start 
    private void settingAlarmDelayed(Context context, Alarm alarm){
    	
    	final String snooze =
            AuroraPreferenceManager.getDefaultSharedPreferences(context)
            .getString(SettingsActivity.KEY_ALARM_SNOOZE, SettingsActivity.DEFAULT_SNOOZE);
	    int snoozeMinutes = Integer.parseInt(snooze);
	
        // Gionee <baorui><2013-04-03> modify for CR00793143 begin
        /*
        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        */
        // Turn off the alarm clock, we don't want to in 59 seconds time to an alarm clock, so easy to fall
        // into a dead loop
        long snoozeTime = -1;
        if (Alarms.bootFromPoweroffAlarm()) {
            long mTempTime = (System.currentTimeMillis() % 2 == 0) ? System.currentTimeMillis() : (System
                    .currentTimeMillis() + 1000);
            snoozeTime = mTempTime + (1000 * 60 * snoozeMinutes);
        } else {
            snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        }
        // Gionee <baorui><2013-04-03> modify for CR00793143 end
	    
	    Alarms.saveSnoozeAlert(context, alarm.id,snoozeTime);
	
		// Gionee baorui 2012-09-18 modify for CR00693305 begin
		final Alarm a = Alarms.getAlarm(context.getContentResolver(), alarm.id);
		if (a == null) {
			context.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
			return;
		}
		// Gionee baorui 2012-09-18 modify for CR00693305 end
	    
	    final Calendar c = Calendar.getInstance();
	    c.setTimeInMillis(snoozeTime);
	
	    String label = alarm.getLabelOrDefault(context);
	    label = context.getString(R.string.alarm_notify_delay_label, label);
	
	    Intent cancelSnooze = new Intent(context,AlarmReceiver.class);
	    cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
	    cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
	    PendingIntent broadcast =
	            PendingIntent.getBroadcast(context, alarm.id, cancelSnooze, PendingIntent.FLAG_CANCEL_CURRENT);
	    NotificationManager nm = getNotificationManager(context);
	    Notification n = new Notification(R.drawable.stat_notify_alarm_white,
	            label, 0);
	    n.setLatestEventInfo(context, label,
	            context.getString(R.string.alarm_notify_delay_text,
	                Alarms.formatTime(context, c)), broadcast);
	    n.flags |= Notification.FLAG_AUTO_CANCEL
	            | Notification.FLAG_ONGOING_EVENT;
	    nm.notify(alarm.id, n);
	
	    String displayTime = context.getString(R.string.alarm_alert_delay_set,
	            snoozeMinutes);
	
	    Toast.makeText(context, displayTime,
	            Toast.LENGTH_LONG).show();
	    Intent intent = new Intent(Alarms.ALARM_ALERT_ACTION);
	    intent.setPackage(context.getPackageName());
	    context.stopService(intent);
	    //context.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
           if(Alarms.bootFromPoweroffAlarm())
	    {
	    	shutDown(context);
	    }

	    
    }

private void shutDown(Context context) {
		Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
	}
    //android:zjy 20120503 add for CR00576747  end

}
