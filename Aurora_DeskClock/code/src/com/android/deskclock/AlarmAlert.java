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

import com.android.deskclock.R;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.Window;

/**
 * Full screen alarm alert: pops visible indicator and plays alarm tone. This
 * activity shows the alert as a dialog.
 */
public class AlarmAlert extends AlarmAlertFullScreen {

    // If we try to check the keyguard more than 5 times, just launch the full
    // screen activity.
    private int mKeyguardRetryCount;
    private final int MAX_KEYGUARD_CHECKS = 5;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleScreenOff((KeyguardManager) msg.obj);
        }
    };

    private final BroadcastReceiver mScreenOffReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    KeyguardManager km =
                            (KeyguardManager) context.getSystemService(
                            Context.KEYGUARD_SERVICE);
                    handleScreenOff(km);
                }
            };

    @Override
    protected void onCreate(Bundle icicle) {
    	// Gionee baorui 2012-12-28 modify for CR00753101 begin
    	//requestWindowFeature(Window.FEATURE_NO_TITLE);
    	/*
    	switch (DeskClockApplication.thremeType) {
		case DeskClockApplication.THEME_TYPE_BLUCK:
			setTheme(R.style.AlarmAlertDialog);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(icicle);
			break;
		case DeskClockApplication.THEME_TYPE_WHITE:
			setTheme(R.style.AlarmAlertDialogWhite);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(icicle);
			
			break;
		default:
			break;
		}
    	*/
    	// Gionee baorui 2012-12-28 modify for CR00753101 end
        super.onCreate(icicle);
        
        // Listen for the screen turning off so that when the screen comes back
        // on, the user does not need to unlock the phone to dismiss the alarm.
        registerReceiver(mScreenOffReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//取消显示home键状态栏
		 cancleRingStatusBar();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//设置显示home键状态栏
		 if(showActionBar){
		        setRingStatusBar();
		 }
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        //取消显示home键状态栏
        cancleRingStatusBar();
        unregisterReceiver(mScreenOffReceiver);
        // Remove any of the keyguard messages just in case
        mHandler.removeMessages(0);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.alarm_alert;
    }
    
    private boolean checkRetryCount() {
        if (mKeyguardRetryCount++ >= MAX_KEYGUARD_CHECKS) {
            return false;
        }
        return true;
    }

    private void handleScreenOff(final KeyguardManager km) {
        if (!km.inKeyguardRestrictedInputMode() && checkRetryCount()) {
            if (checkRetryCount()) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0, km), 500);
            }
        } else {
            // Launch the full screen activity but do not turn the screen on.
            Intent i = new Intent(this, AlarmAlertFullScreen.class);
            i.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
            i.putExtra(SCREEN_OFF, true);
            startActivity(i);
            finish();
        }
    }
  //设置home键actionbar
    public void setRingStatusBar(){
    	Bitmap bp=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
   	 NotificationManager manager = (NotificationManager) AlarmAlert.this  
   		        .getSystemService(Context.NOTIFICATION_SERVICE);  
   		        // 创建一个Notification  
   		        Intent openintent = new Intent(AlarmAlert.this, AlarmAlert.class);
   		     openintent.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
   		     openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
   		    	PendingIntent contentIntent = PendingIntent.getActivity(AlarmAlert.this, 0, openintent, PendingIntent.FLAG_UPDATE_CURRENT);//当点击消息时就会向系统发送openintent意图
   		        Notification notification = new NotificationCompat.Builder(AlarmAlert.this)
   		                .setLargeIcon(bp).setSmallIcon(R.drawable.ic_launcher)

   		                .setContentTitle("ContentTitle").setContentText("ContentText")
   		                .setAutoCancel(true)
   		                .setDefaults(Notification.FLAG_AUTO_CANCEL)
   		                .setContentIntent(contentIntent)
   		                .build();
   		        manager.notify(16210, notification);
   		        manager.cancel(16210);
    }
    //取消home键actionbar
    public void cancleRingStatusBar(){
    	Bitmap bp=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    	 NotificationManager manager = (NotificationManager) AlarmAlert.this  
    		        .getSystemService(Context.NOTIFICATION_SERVICE);  
    		        // 创建一个Notification  
    		        Intent openintent = new Intent(AlarmAlert.this, AlarmAlert.class);
    		        openintent.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
    		        openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		    	PendingIntent contentIntent = PendingIntent.getActivity(AlarmAlert.this, 0, openintent, PendingIntent.FLAG_UPDATE_CURRENT);//当点击消息时就会向系统发送openintent意图
    		        Notification notification = new NotificationCompat.Builder(AlarmAlert.this)
    		                .setLargeIcon(bp).setSmallIcon(R.drawable.ic_launcher)
    		                .setContentInfo(" ")

    		                .setContentTitle("ContentTitle").setContentText("ContentText")
    		                
    		                .setAutoCancel(false).setDefaults(Notification.FLAG_AUTO_CANCEL)
    		               .setContentIntent(contentIntent)
    		                .build();
    		        manager.notify(16220, notification);
    		        manager.cancel(16220);

    }
}
