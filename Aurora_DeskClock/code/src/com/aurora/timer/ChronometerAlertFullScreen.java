/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.aurora.timer;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.deskclock.R;
import com.aurora.internal.widget.AuroraDialogTitle;

/**
 * The reference of alarm clock to achieve
 * Chronometer alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class ChronometerAlertFullScreen extends Activity {

    protected static final String SCREEN_OFF = "screen_off";
    
    private static final String ALARM_DISMISS_ACTION = "com.android.intent.chronometer.ALARM_DISMISS";

    boolean mFullscreenStyle;
    private static String countdownTime;
    private TextView tvCountdownTime;
    private Intent intent;
    
    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ALARM_DISMISS_ACTION)) {
                dismiss(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        // Gionee baorui 2012-12-26 modify for CR00753181 begin
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Gionee baorui 2012-12-26 modify for CR00753181 end
        super.onCreate(icicle);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                );
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        updateLayout();
//        getAuroraActionBar().setVisibility(View.GONE);

        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    protected int getLayoutResId() {
//		return R.layout.chronometer_alarm_alert_fullscreen;
    	return R.layout.aurora_stk_msg_dialog;
        
    }
    
    private AuroraDialogTitle titleView;
    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(getLayoutResId());

        /* dismiss button: close notification */
        findViewById(R.id.button_cancel).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(false);
                    }
                });

        // alarm the countdown time in the dialog
        Intent intent = getIntent();
        String tempTime = intent.getStringExtra(TimerFragment.COUNTDOWN_TIME);
        titleView = (AuroraDialogTitle) findViewById(R.id.aurora_alertTitle);
        if(!TextUtils.isEmpty(intent.getStringExtra(TimerFragment.SHOW_TITLE)))
        {
        	titleView.setText(intent.getStringExtra(TimerFragment.SHOW_TITLE));
        }
        if(! getString(R.string.chronometer_alart_title).equals(tempTime) && tempTime != null){
        	countdownTime = tempTime;
        }
        
        tvCountdownTime = (TextView) findViewById(R.id.dialog_message);
        tvCountdownTime.setText(countdownTime);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    protected void dismiss(boolean killed) {
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(0);
            stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
            // set the state of the alert dialog Dismissed 
            TimerFragment.setAlertDismiss(true);
            ChronometerAlarmAlertWakeLock.releaseCpuLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
    		ChronometerAlarmAlertWakeLock.releaseScreenOnLock(ChronometerAlarmAlertWakeLock.CHRONMENTER);
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
		// Gionee baorui 2012-09-24 modify for CR00686744 begin
        TimerFragment.setStartMusic(false);
		// Gionee baorui 2012-09-24 modify for CR00686744 end
        tvCountdownTime.setText(countdownTime);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		// Gionee baorui 2012-09-24 modify for CR00686744 begin
        TimerFragment.setStartMusic(true);
		// Gionee baorui 2012-09-24 modify for CR00686744 end
		// No longer care about the alarm being killed.
		unregisterReceiver(mReceiver);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        // Do this on key down to handle a few of the system keys.
//        boolean up = event.getAction() == KeyEvent.ACTION_UP;
//        switch (event.getKeyCode()) {
//            // Volume keys and camera keys dismiss the alarm
//            case KeyEvent.KEYCODE_VOLUME_UP:
//            case KeyEvent.KEYCODE_VOLUME_DOWN:
//            case KeyEvent.KEYCODE_VOLUME_MUTE:
//            case KeyEvent.KEYCODE_CAMERA:
//            case KeyEvent.KEYCODE_FOCUS:
//                if (up) {
//                    switch (mVolumeBehavior) {
//                        case 1:
//                            break;
//                        case 2:
//                            dismiss(false);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//                return true;
//            default:
//                break;
//        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
    	dismiss(false);
        return;
    }
    
}
