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

package com.android.deskclock;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import com.android.deskclock.Alarms;

import com.android.deskclock.R;
import com.android.deskclock.ShakeListener.OnShakeListener;
import com.aurora.utils.DensityUtil;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import aurora.preference.AuroraPreferenceManager;

import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import aurora.widget.AuroraButton;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.aurora.timer.TimerFragment;
import com.aurora.worldtime.UsefulUtils;


/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreen extends AuroraActivity implements OnTouchListener{
	
	private boolean showbar=false;
	 private  KeyguardManager mKeyguardManager;
	 private static final String CLOSE_FULLSCREEN_VIEW = "close.fullscreen.view";
    // These defaults must match the values in res/xml/settings.xml
    // Gionee baorui 2012-09-08 modify for CR00687666 begin
    // private static final String DEFAULT_SNOOZE = "10";
    //private static final String DEFAULT_SNOOZE = "5";
    // Gionee baorui 2012-09-08 modify for CR00687666 end
	// Gionee baorui 2012-09-28 modify for CR00705225 begin
	// private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
	private static final String DEFAULT_VOLUME_BEHAVIOR = "1";
	// Gionee baorui 2012-09-28 modify for CR00705225 end
    protected static final String SCREEN_OFF = "screen_off";
    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;
    private TextView  titleLabel;
    //android:hxc start
    private static final String ALARM_REQUEST_SHUTDOWN_ACTION = "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final String DISABLE_POWER_KEY_ACTION = "android.intent.action.DISABLE_POWER_KEY";
    // delay time to finish activity after the boot anim start
	private static final int DELAY_FINISH_TIME = 2;
    // delay time to stop boot anim
    private static final int DELAY_TIME_SECONDS = 7;
    private static final String NORMAL_BOOT_DONE_ACTION = "android.intent.action.normal.boot.done";
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String DEFAULT_POWER_ON_VOLUME_BEHAVIOR = "0";
    private static final String KEY_VOLUME_BEHAVIOR ="power_on_volume_behavior";
    private static final String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";
    private static final String POWER_ON_VOLUME_BEHAVIOR_PREFERENCES = "PowerOnVolumeBehavior";
	private boolean mBootFromPoweroffAlarm;
    private boolean mIsPoweroffAlarm;
    private LayoutInflater mInflater;
    //android:hxc end
   
    private int mLastMoveY = 0;
    private int mYY = 0;
    //关闭闹钟后的红色背景
    private BackgroundView backgroundView;
    private RelativeLayout content_fullcreen;
    //关闭闹钟后提示“正在关闭”
    private TextView textView_bg;
    private AnimationSet as;
    private AnimationSet as2;
    private boolean downInRect = false;
    
    private LinearLayout slidetocloseLY;
    private TextView stillsleepText;
    private Rect mRect;
    
    private int distance;
    private boolean isTouchable = true;

    // Gionee <baorui><2013-03-22> modify for CR00783443 begin
    public static AlarmAlertFullScreen mInstanse = null;
    // Gionee <baorui><2013-03-22> modify for CR00783443 end
    
    //aurora add by tangjun 2013.12.22 start
    private ShakeListener shakeListener;
    private static final int SENSOR_SHAKE = 10;
    public static int alarmTimes = 1;
    //记录上一个闹钟的信息
    public static Alarm lastAlarm;
    private boolean isSensorChanged = false;
    private boolean isPowerChanged =false;
    private ScrollView scrollviewToClose;
    private LinearLayout stillsleepLinear;
    private DigitalClock digitalclock;
    public boolean showActionBar;
    private long timeCount = 0;
    Vibrator mVibrator;
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";
    //aurora add by tangjun 2013.12.22 end

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_KEYCODE_POWER_SHORTPRESS)) {
				Log.e("--AlarmAlertFullScreens--intent.getAction()--" + intent.getAction());
            	if(!isPowerChanged){
            		isPowerChanged=true;

                    if ( alarmTimes < 3) {
                    	showActionBar=false;
                    	snooze();
                    	alarmTimes ++;
                    	//setAirplaneModeOff( AlarmAlertFullScreen.this );
                    	Log.e("---222alarmTimes--- = " + alarmTimes);
                    }
                    
            		Log.v("power button.");	

            	}	

            }
            if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
                dismiss(false);
            }else if(CLOSE_FULLSCREEN_VIEW.equals(action)){
            	   Log.v("receive the close fullscreen view bRs.");
                dismiss(true);
            }            
            //android:zjy 20120504 add for CR00576747  start 
			else if (action.equals(Alarms.ALARM_REPEAT_RING)) {

				if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
					shutDown();
				}
				dismiss(false);
			}
            //android:zjy 20120504 add for CR00576747  end  
			else {
				if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
				    if (!intent.getBooleanExtra("dismissAlarm", false)) {
					snooze();
				    }
				} else {
				    Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
				    if (alarm != null && mAlarm.id == alarm.id) {
                        // Gionee baorui 2012-09-06 modify for CR00683131 begin
                        // dismiss(true);
                        if (Alarms.mIfDismiss == false) {
                            dismiss(true);
                        }
                        // Gionee baorui 2012-09-06 modify for CR00683131 end
					
				    }
				}
            }
        }
    };
    //隐藏虚拟按键
    private void hideNaviBar(boolean hide) {
        ContentValues values = new ContentValues();
        values.put("name", NAVI_KEY_HIDE);
        values.put("value", (hide ? 1 : 0));
        ContentResolver cr = AlarmAlertFullScreen.this.getContentResolver();
        cr.insert(Settings.System.CONTENT_URI, values);
    }
    @Override
    protected void onCreate(Bundle icicle) {
    	android.util.Log.d("cjslog", "fullscreen oncreate");
        // Gionee baorui 2012-12-29 modify for CR00753101 begin
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Gionee baorui 2012-12-29 modify for CR00753101 end
        super.onCreate(icicle);
      //  Intent intent=new Intent("com.aurora.launcher.disable_expand_statusbar");
        //home键监听
        mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);  
        //如果计时器在响铃就关掉计时器声音 aurora add by tangjun 2014.3.4
        //stopService(new Intent(TimerFragment.ALARM_ALERT_ACTION));
        //cjs modify
        Intent intent = new Intent(TimerFragment.ALARM_ALERT_ACTION);
        intent.setPackage(getPackageName());
        stopService(intent);
        
        // Gionee <baorui><2013-03-22> modify for CR00783443 begin
        mInstanse = this;
        // Gionee <baorui><2013-03-22> modify for CR00783443 end
        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        
//        AuroraActionBar actionBar = getAuroraActionBar();
//        if(actionBar!=null){
//        	actionBar.hide();
//        }
        // Gionee <baorui><2013-07-12> modify for CR00835747 begin
        if (Alarms.mIsGnMtkPoweroffAlarmSupport) {
            // android:hxc start
            // +MediaTek 2012-02-28 enable key dispatch
            try {
                final IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager
                        .getService(Context.WINDOW_SERVICE));
                wm.setEventDispatching(true);
            } catch (RemoteException e) {
            }
            // -MediaTek 2012-02-28 enable key dispatch
        }
        // Gionee <baorui><2013-07-12> modify for CR00835747 end

		mIsPoweroffAlarm = getIntent().getBooleanExtra(POWER_OFF_FROM_ALARM,false);
		mBootFromPoweroffAlarm = Alarms.bootFromPoweroffAlarm();   
		mInflater = LayoutInflater.from(this);
		// Get the volume/camera button behavior setting
		if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
			AlarmAlertWakeLock.acquireAlarmAlertFSCpuWakeLock(this);
				Log.v("AlarmAlertFullScreen acquireCpuWakeLock");
			getPowerOnVolumeBehaviod();
		} else {
	        final String vol =
	                AuroraPreferenceManager.getDefaultSharedPreferences(this)
	                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
	                        DEFAULT_VOLUME_BEHAVIOR);
	        mVolumeBehavior = Integer.parseInt(vol);
		}
        
        
        //android:hxc end

		//android:hxc start anota
        // Get the volume/camera button behavior setting
//        final String vol =
//                AuroraPreferenceManager.getDefaultSharedPreferences(this)
//                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
//                        DEFAULT_VOLUME_BEHAVIOR);
//        mVolumeBehavior = Integer.parseInt(vol);
        //android:hxc end
        
       // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
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
        //android:hxc start
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       // hideNaviBar(true);
        hideSystemUI();
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
			//win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);

			getLayoutResId();
			
			
			updateLayoutForPowerOn();
		} else {
			updateLayout();
		}
        remarks=(TextView)findViewById(R.id.remarks);
        remarks.setText(mAlarm.getRemakrs(this).equals(getString(R.string.empty_label))?"":mAlarm.getRemakrs(this));
        //android:hxc end

        //android:hxc start anota
//        updateLayout();
        //android:hxc end anota

        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        //filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_KEYCODE_POWER_SHORTPRESS);
      
        
      //android:zjy 20120504 add for CR00576747  start 
        filter.addAction(Alarms.ALARM_REPEAT_RING);
        //android:zjy 20120504 add for CR00576747  end 
        
        registerReceiver(mReceiver, filter);
        
        shakeListener = new ShakeListener(this);  
        shakeListener.setOnShakeListener(mOnShakeListener);
    	mVibrator = (Vibrator)getApplication().getSystemService(VIBRATOR_SERVICE);
    	shakeListener.start();
    }
    private void hideSystemUI() {

		if (VERSION.SDK_INT > 18) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
																	// bar
							| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
							| 4096);

		}

	}

    //add by zhanjiandong 2014.12.18
    private TextView remarks;//备注
    protected int getLayoutResId() {
		return R.layout.alarm_alert_fullscreen;
		
       
    }

   
    protected int getPowerOnLayoutResId() {
		return R.layout.alarm_alert_power_on_fullscreen;
		
		
	}
    
    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflater.inflate(getLayoutResId(), null));
        backgroundView=(BackgroundView)findViewById(R.id.bg);
        content_fullcreen=(RelativeLayout)findViewById(R.id.content_fullcreen);
        textView_bg=(TextView)findViewById(R.id.textView_bg);
        
        
        slidetocloseLY = (LinearLayout)findViewById(R.id.slider_layout);
        stillsleepText = (TextView)findViewById(R.id.stillsleeptext);
        
        scrollviewToClose = (ScrollView)findViewById(R.id.scrollviewtoclose);
        scrollviewToClose.setOnTouchListener(this);
        
        stillsleepLinear = (LinearLayout)findViewById(R.id.stillsleeplinear);
        digitalclock = (DigitalClock)findViewById(R.id.digitalClock);
    }
    //关闭闹钟动画并finish
    public void startCloseTextAnimation(){
    	 as=new AnimationSet(false);
     	TranslateAnimation translateAnimation=new TranslateAnimation(0,0,300,-120);
 	   	//va7.setFillAfter(true);
     	translateAnimation.setDuration(300);
     	translateAnimation.setInterpolator(new DecelerateInterpolator());

 	   	AlphaAnimation alphaAnimation=new AlphaAnimation(0,1);
 	   alphaAnimation.setDuration(300);
 	  alphaAnimation.setInterpolator(new DecelerateInterpolator());
     	as.setFillAfter(true);
 	   	as.addAnimation(translateAnimation);
 	   	as.addAnimation(alphaAnimation);  
 	   	as.setAnimationListener(new Animation.AnimationListener() {
 				
 				@Override
 				public void onAnimationStart(Animation animation) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					 dismiss(false);
 					
 				}
 			});
 	   textView_bg.startAnimation(as);
	
    }
    //关闭闹钟时背景动画
    public void startBackgroundAnimation(){
    
       	
       	TranslateAnimation translateAnimation=new TranslateAnimation(0,0,0,-600);
       	translateAnimation.setDuration(500);
       	translateAnimation.setInterpolator(new DecelerateInterpolator());
       	AlphaAnimation alphaAnimation=new AlphaAnimation(1,0);
       	alphaAnimation.setDuration(500);
       	alphaAnimation.setInterpolator(new DecelerateInterpolator());
       	as2=new AnimationSet(false);
       	as2.addAnimation(translateAnimation);
       	as2.addAnimation(alphaAnimation); 
       
       	as2.setAnimationListener(new Animation.AnimationListener() {
    			
    			@Override
    			public void onAnimationStart(Animation animation) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    			@Override
    			public void onAnimationRepeat(Animation animation) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				// TODO Auto-generated method stub
    				
    				content_fullcreen.setVisibility(View.GONE);
    				slidetocloseLY.setVisibility(View.GONE);
    				textView_bg.setVisibility(View.VISIBLE);
    				startCloseTextAnimation();
    			
    								
    			}
    		});  
    	
       	content_fullcreen.startAnimation(as2);
       	slidetocloseLY.startAnimation(as2);
    	
    	
    	
    	
    }

    // Attempt to snooze this alert.
    private void snooze() {
    	if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
    		setButtonCannotClick();
		}
        // Do not snooze if the snooze button is disabled.
//        if (!findViewById(R.id.snooze).isEnabled()) {
//            dismiss(false);
//            return;
//        }
        final String snooze =
                AuroraPreferenceManager.getDefaultSharedPreferences(this)
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
        if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
            long mTempTime = (System.currentTimeMillis() % 2 == 0) ? System.currentTimeMillis() : (System
                    .currentTimeMillis() + 1000);
            snoozeTime = mTempTime + (1000 * 60 * snoozeMinutes);
        } else {
            snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        }
        // Gionee <baorui><2013-04-03> modify for CR00793143 end
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id,
                snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
        PendingIntent broadcast =
                PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm = getNotificationManager();
		
        Notification n = new Notification(R.drawable.stat_notify_alarm_white,label, 0);
        
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text,
                    Alarms.formatTime(this, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        	Log.v(displayTime);
        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime,
                Toast.LENGTH_LONG).show();
        //stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        //cjs modify 
        shakeListener.stop();
        Intent intent = new Intent(Alarms.ALARM_ALERT_ACTION);
        intent.setPackage(this.getPackageName());
        stopService(intent);
        //cjs modify end
        if(this instanceof AlarmAlert){
            closeFullScreenView();            
        }
		// if it is poweron alarm,then shut down the device
		if (mBootFromPoweroffAlarm && mIsPoweroffAlarm) {
			Log.v("------AlarmAlertScreen shutDown-----------");
			shutDown();
		}else{
			Log.v("------AlarmAlertScreen finish-----------");
			finish();
		}
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
    	showActionBar=false;
    	   Log.i(killed ? "Alarm killed" : "Alarm dismissed by user");
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
            //stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
            //cjs modify
            Intent intent = new Intent(Alarms.ALARM_ALERT_ACTION);
            intent.setPackage(getPackageName());
            stopService(intent);
        }
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

       Log.v("AlarmAlert.OnNewIntent()");
        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

        //setTitle();
    }
    
    private void judgeIfAlarmEqual( ) {
    	Log.e("judgeIfAlarmEqual alarmTimes = " + alarmTimes);
//    	if ( lastAlarm != null ) {
//    	Log.e("lastAlarm.id = " + lastAlarm.id + ", mAlarm.id = " + mAlarm.id);
//    	Log.e("lastAlarm.minutes = " + lastAlarm.minutes + ", mAlarm.minutes = " + mAlarm.minutes);
//    	Log.e("lastAlarm.hour = " + lastAlarm.hour + ", mAlarm.hour = " + mAlarm.hour);
//    	Log.e("lastAlarm.enabled = " + lastAlarm.enabled + ", mAlarm.enabled = " + mAlarm.enabled);
//    	Log.e("lastAlarm.daysOfWeek.getCoded() = " + lastAlarm.daysOfWeek.getCoded() + 
//    			", mAlarm.daysOfWeek.getCoded() = " + mAlarm.daysOfWeek.getCoded());
//    	}
    	//上一次闹铃跟当前闹铃不一致时就把贪睡次数还原吧 aurora add by tangjun 2014.2.21
    	if ( lastAlarm == null ) {
    		alarmTimes = 1;
    	} else if ( lastAlarm.id != mAlarm.id || lastAlarm.minutes != mAlarm.minutes || lastAlarm.hour != mAlarm.hour 
    			|| lastAlarm.daysOfWeek.getCoded() != mAlarm.daysOfWeek.getCoded() ) {
    		alarmTimes = 1;
    	}
    	lastAlarm = mAlarm;
    }
    @Override
    protected void onResume() {
        super.onResume();
        //xiexiujie 12.2 for statusbar sidable expand
        Intent intent=new Intent(  "com.android.systemui.recent.AURORA_DISABLE_HANDLER");
        sendBroadcast(intent);
        showActionBar=true;
        Log.e("---AlarmAlertFullScreen onResume---");
        // If the alarm was deleted at some point, disable snooze.
        if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
            //AuroraButton snooze = (AuroraButton) findViewById(R.id.snooze);
            //snooze.setEnabled(false);
        }
        
        isSensorChanged = false;
        
        //aurora add by tangjun 2013.12.22
        // 注册监听器
        if ( shakeListener != null ) {
        	shakeListener.start();
        }
        cancleRingStatusBar();
        judgeIfAlarmEqual( );
        
        if( alarmTimes == 2 ) {
        	stillsleepText.setText(getString(R.string.stillsleepsecond));
        } else if ( alarmTimes == 3 ){
        	stillsleepText.setText(getString(R.string.stillsleepthird));
        }
        
        sendAuroraAlarmBroadcast( "com.aurora.deskclock.startalarm" );
    }
    //设置home键actionbar
    public void setRingStatusBar(){
    	Bitmap bp=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
   	 NotificationManager manager = (NotificationManager) AlarmAlertFullScreen.this  
   		        .getSystemService(Context.NOTIFICATION_SERVICE);  
   		        // 创建一个Notification  
   		        Intent openintent = new Intent(AlarmAlertFullScreen.this, AlarmAlert.class);
   		     openintent.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
   		     openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
   		    	PendingIntent contentIntent = PendingIntent.getActivity(AlarmAlertFullScreen.this, 0, openintent, PendingIntent.FLAG_UPDATE_CURRENT);//当点击消息时就会向系统发送openintent意图
   		        Notification notification = new NotificationCompat.Builder(AlarmAlertFullScreen.this)
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
    	 NotificationManager manager = (NotificationManager) AlarmAlertFullScreen.this  
    		        .getSystemService(Context.NOTIFICATION_SERVICE);  
    		        // 创建一个Notification  
    		        Intent openintent = new Intent(AlarmAlertFullScreen.this, AlarmAlert.class);
    		        openintent.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
    		        openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		    	PendingIntent contentIntent = PendingIntent.getActivity(AlarmAlertFullScreen.this, 0, openintent, PendingIntent.FLAG_UPDATE_CURRENT);//当点击消息时就会向系统发送openintent意图
    		        Notification notification = new NotificationCompat.Builder(AlarmAlertFullScreen.this)
    		                .setLargeIcon(bp).setSmallIcon(R.drawable.ic_launcher)
    		                .setContentInfo(" ")

    		                .setContentTitle("ContentTitle").setContentText("ContentText")
    		                
    		                .setAutoCancel(false).setDefaults(Notification.FLAG_AUTO_CANCEL)
    		               .setContentIntent(contentIntent)
    		                .build();
    		        manager.notify(16220, notification);
    		        manager.cancel(16220);

    }
    //aurora add by tangjun 2013.12.22 start
    @Override  
    protected void onPause() {  
        super.onPause(); 
        //xiexiujie 12.2 for statusbar enable expand
        Intent intent=new Intent("com.android.systemui.recent.AURORA_ENABLE_HANDLER");
        sendBroadcast(intent);
       if(showActionBar&&showbar&& mKeyguardManager.inKeyguardRestrictedInputMode()){
    	   setRingStatusBar();    	   
       }
       showbar=true;
        // 取消监听器  
        if ( shakeListener != null ) {
        	shakeListener.stop();  
        }
        mInstanse = null;
        sendAuroraAlarmBroadcast( "com.aurora.deskclock.stopalarm" );
    }
    
    private void sendAuroraAlarmBroadcast( String action ) {
    	Intent intent = new Intent();
    	intent.setAction(action);

    	//发送 一个无序广播
    	this.sendBroadcast(intent);
    }
    
    /** 
     * 重力感应监听 
     */  
    private OnShakeListener mOnShakeListener = new OnShakeListener() {  
		@Override
		public void onShake() {
				
			shakeListener.stop();			
			startVibrato(); 
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run(){
					Log.e("检测到摇晃，执行操作！ isSensorChanged = " + isSensorChanged);
					if ( !isSensorChanged ) {
						isSensorChanged = true;
						Message msg = new Message();  
			            msg.what = SENSOR_SHAKE;  
			            handler.sendMessage(msg);
					}					
					//	   mVibrator.cancel();
						   shakeListener.start();
				}
			}, 500);
						
		}
    };
    
    
    public void startVibrato(){	
    	    	
    		mVibrator.vibrate( new long[]{500,200,500,200}, -1); 
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
    		Toast.makeText(AlarmAlertFullScreen.this, "已自动帮你关闭飞行模式!", Toast.LENGTH_SHORT).show();
	        Settings.Global.putInt(context.getContentResolver(),  
	                             Settings.Global.AIRPLANE_MODE_ON, 0);  
	        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
	        intent.putExtra("state", 0);  
	        context.sendBroadcast(intent);  
    	}
    } 
  
    /** 
     * 动作执行 
     */  
    Handler handler = new Handler() {  
  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            switch (msg.what) {  
            case SENSOR_SHAKE:  
                if ( alarmTimes < 3) {
                	showActionBar=false;
                	snooze();
                	alarmTimes ++;
                	//setAirplaneModeOff( AlarmAlertFullScreen.this );
                	Log.e("---alarmTimes--- = " + alarmTimes);
                }
                break;  
            }  
        }  
  
    }; 
    
    //aurora add by tangjun 2013.12.22 end

    @Override
    public void onDestroy() {
        super.onDestroy();
      //  hideNaviBar(false);        
//        Intent intent=new Intent("com.aurora.launcher.enable_expand_statusbar");
       
        Log.v("AlarmAlert.onDestroy()");
        // No longer care about the alarm being killed.
        android.util.Log.e("333333", "--222--AlarmAlertWakeLock.releaseCpuLock();-----");
        AlarmAlertWakeLock.releaseCpuLock();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            //snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }
    //android:hxc start
    private void getPowerOnVolumeBehaviod() {
		SharedPreferences prefs = getSharedPreferences(
				POWER_ON_VOLUME_BEHAVIOR_PREFERENCES, 0);
		if (!prefs.contains(KEY_VOLUME_BEHAVIOR)) {
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString(KEY_VOLUME_BEHAVIOR, DEFAULT_POWER_ON_VOLUME_BEHAVIOR);
			ed.apply();
		}
		final String poweronVol = prefs.getString(KEY_VOLUME_BEHAVIOR,
				DEFAULT_POWER_ON_VOLUME_BEHAVIOR);
		mVolumeBehavior = Integer.parseInt(poweronVol);
		if (mVolumeBehavior == 2) {
			mVolumeBehavior = 0;
		}
	}
    
    private void closeFullScreenView(){
    	   Log.v("close fired from alarm alert dialog");
        Intent intent = new Intent(CLOSE_FULLSCREEN_VIEW);
        sendBroadcast(intent);
    }
    
    private void updateLayoutForPowerOn() {
    	setContentView(mInflater.inflate(getPowerOnLayoutResId(), null));
    	
    	setContentView(mInflater.inflate(getLayoutResId(), null));
    	
    	slidetocloseLY = (LinearLayout)findViewById(R.id.slider_layout);
    	stillsleepText = (TextView)findViewById(R.id.stillsleeptext);
    	
    	scrollviewToClose = (ScrollView)findViewById(R.id.scrollviewtoclose);
    	scrollviewToClose.setOnTouchListener(this);
    	
        stillsleepLinear = (LinearLayout)findViewById(R.id.stillsleeplinear);
        digitalclock = (DigitalClock)findViewById(R.id.digitalClock);
    	
    	disablePowerKey();
        }
    
    
 // power on the device
	private void powOn() {
		setButtonCannotClick();
			// +MediaTek 2012-02-28 disable key dispatch
			try {
				final IWindowManager wm = IWindowManager.Stub.asInterface(
					ServiceManager.getService(Context.WINDOW_SERVICE));
				wm.setEventDispatching(false);
			} catch (RemoteException e) {}
			// -MediaTek 2012-02-28 disable key dispatch
		stopPlayAlarm();
		// start boot animation
		SystemProperties.set("ctl.start", "bootanim");
			Log.v("start boot animation");
		// send broadcast to power on the phone
		Intent bootIntent = new Intent(NORMAL_BOOT_ACTION);
		sendBroadcast(bootIntent);
			Log.v("send broadcast: android.intent.action.normal.boot");
		enablePowerKey();
		Handler handler = new Handler();
		// close boot animation after 5 seconds
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//SystemProperties.set("ctl.stop", "bootanim");
				Intent bootDoneIntent = new Intent(NORMAL_BOOT_DONE_ACTION);
				sendBroadcast(bootDoneIntent);
					Log.v("stop boot animation");
			}
		}, 1000 * DELAY_TIME_SECONDS);
		// finish after the boot animation start
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, 1000 * DELAY_FINISH_TIME);
		SystemProperties.set("sys.boot.reason", "0");
		AlarmAlertWakeLock.releaseAlarmAlertFSCpuLock();
	}
    
	// power off the device
	private void powOff() {
		setButtonCannotClick();

			// +MediaTek 2012-02-28 disable key dispatch
			try {
			    final IWindowManager wm = IWindowManager.Stub.asInterface(
			    ServiceManager.getService(Context.WINDOW_SERVICE));
			    wm.setEventDispatching(false);
			} catch (RemoteException e) {}
			// -MediaTek 2012-02-28 disable key dispatch
		stopPlayAlarm();
		shutDown();
	}
	
	private void setButtonCannotClick(){
		//findViewById(R.id.power_on).setClickable(false);
		//findViewById(R.id.snooze).setClickable(false);
		//findViewById(R.id.power_off).setClickable(false);
	}
	
	// Cancel the notification and stop playing the alarm
	private void stopPlayAlarm() {
		NotificationManager nm = getNotificationManager();
		nm.cancel(mAlarm.id);
		Intent intent = new Intent(Alarms.ALARM_ALERT_ACTION);
		intent.setPackage(this.getPackageName());
		stopService(intent);
	}
	
	// enable the power key when power on or power off the device
	private void enablePowerKey() {
		Intent enablePowerKeyIntent = new Intent(DISABLE_POWER_KEY_ACTION);
		enablePowerKeyIntent.putExtra("state", false);
		sendBroadcast(enablePowerKeyIntent);
			Log.v("send enablePowerKey broadcast: android.intent.action.DISABLE_POWER_KEY");
	}
	
	// shut down the device
	private void shutDown() {
		// send normal shutdown broadcast
		Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
		sendBroadcast(shutdownIntent);
			Log.v("send shutdown broadcast: android.intent.action.normal.shutdown");
		enablePowerKey();
		// shutdown the device
		Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
		intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	// disable the power key when the device is boot from alarm but not ipo boot
	private void disablePowerKey() {
		Intent disablePowerKeyIntent = new Intent(DISABLE_POWER_KEY_ACTION);
		disablePowerKeyIntent.putExtra("state", true);
		sendBroadcast(disablePowerKeyIntent);
			Log.v("send disablePowerKey broadcast: android.intent.action.DISABLE_POWER_KEY");
	}
    //android:hxc end
    
    // Gionee <baorui><2013-03-22> modify for CR00783443 begin
    public void gnSnooze() {
        snooze();
    }
    // Gionee <baorui><2013-03-22> modify for CR00783443 end
    
    
    //aurora add by tangjun 2013.12.16 start 
    private boolean handleActionDownEvenet(MotionEvent event) { 
    	//矩形区域是滑动块响应区域,根据需要修改
    	mRect = new Rect(0, DensityUtil.dip2px(this,465 ), DensityUtil.dip2px(this,360), DensityUtil.dip2px(this,615 )); 
        Log.e("rect = " + mRect.toString() + ", x = " + event.getX() + ", y = " + event.getY());
        boolean isHit = mRect.contains((int) event.getX(), (int) event.getY()-DensityUtil.dip2px(this,25));  
        Log.e("isHit = " + isHit); 
     
        return isHit;  
    }  
	
    /*
    public boolean onTouchEvent(MotionEvent event) {  
        int y = (int) event.getY();  
        //Log.i(TAG, "onTouchEvent Y is " + y);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            mLastMoveY = (int) event.getY();  
            mYY = mLastMoveY;
            downInRect = handleActionDownEvenet(event);
            //处理Action_Down事件：  判断是否点击了滑动区域  
            break;  
        case MotionEvent.ACTION_MOVE:  
        	
        	if ( Math.abs(mYY  - y) > 30 && downInRect && mLastMoveY - y >= 0 && isTouchable) {
        		//Log.e(TAG, "11111111111111");
        		//slidetocloseImage.setPivotX(0);
        		//slidetocloseImage.setPivotY(0);
        		slidetocloseImage.setTranslationY(y - mLastMoveY);
        		mYY = y;
        	}
        	
        	//mLastMoveY = y;
        	break;  
        case MotionEvent.ACTION_UP:  
            //处理Action_Up事件：  判断是否解锁成功，成功则结束我们的Activity ；否则 ，缓慢回退该图片。
        	Log.e("handleActionUpEvent : ACTION_UP -->isTouchable--" + isTouchable);
        	if ( isTouchable && downInRect && slidetocloseImage.getTranslationY() < 0) {
        		isTouchable = false;
        		handleActionUpEvent(event);
        	}
        	
        	downInRect = false;
        	break; 
        }  
        return super.onTouchEvent(event);  
    }  
    */
    
    //回退动画时间间隔值   
    private static int BACK_DURATION = 15 ;   // 20ms  
    //水平方向前进速率  
    private static float VE_HORIZONTAL = 25 ;  //0.1dip/ms  
      
    //判断松开手指时，是否达到末尾即可以开锁了 , 是，则开锁，否则，通过一定的算法使其回退。  
    private void handleActionUpEvent(MotionEvent event){          
        int y = (int) event.getY() ;      
        
        Log.e("--AlarmAlertFullScreen timecount = ---" + timeCount);
        boolean isSucess= ( y <= DensityUtil.dip2px(this,400 ) || (scrollviewToClose.getScrollY() > 200 && timeCount < DensityUtil.dip2px(this,67)) );           
        Log.e("handleActionUpEvent : isSucess -->" + isSucess);    
        if(isSucess){  
        	//启动关闭闹钟时红色背景动画
        	backgroundView.startDisplayAnim();
        //	开启关闭闹钟动画
        	startBackgroundAnimation();
        	       	
           isTouchable = true;
           //resetViewState();
        //   dismiss(false);
           //aurora add by tangjun 2013.12.27 start 关闭的时候还原次数
           alarmTimes = 1;
           //aurora add by tangjun 2013.12.27 end 关闭的时候还原次数
           
           //关闭起床闹铃则关闭飞行模式
           int counttime  = mAlarm.hour*60 + mAlarm.minutes;
           Log.e("--counttime = ---" + counttime);
           if ( counttime >= 300 && counttime <= 660 && mAlarm.daysOfWeek.isRepeatSet() ) {       
        	   setAirplaneModeOff(AlarmAlertFullScreen.this);
           }
        }  
        else if (scrollviewToClose.getScrollY() > 0){//没有成功解锁，以一定的算法使其回退  
            //每隔20ms , 速率为0.6dip/ms ,  使当前的图片往后回退一段距离，直到到达最左端     
           // mLastMoveY = y ;  //记录手势松开时，当前的坐标位置。  
        	shakeListener.start();  //cjs add 
            distance = scrollviewToClose.getScrollY();  
            Log.e("handleActionUpEvent : distance -->" + distance);  
            //只有移动了足够距离才回退  
            //Log.e(TAG, "handleActionUpEvent : mLastMoveX -->" + mLastMoveX + " distance -->" + distance );  
            if(distance > 0)  
                mHandler.postDelayed(BackDragImgTask, BACK_DURATION);  
            else{  //复原初始场景  
                resetViewState();  
            }  
        }  
    }  
    //重置初始的状态，显示tv_slider_icon图像，使bitmap不可见  
    private void resetViewState(){  
    	scrollviewToClose.setScrollY(0);
    	stillsleepLinear.setAlpha(1);
    	digitalclock.setAlpha(1);
		isTouchable = true;
		Log.e( "resetViewState");
    }  
      
    //通过延时控制当前绘制bitmap的位置坐标  
    private Runnable BackDragImgTask = new Runnable(){  
          
        public void run(){  
            //一下次Bitmap应该到达的坐标值  
            distance = distance - (int)(VE_HORIZONTAL);  
            
            //是否需要下一次动画 ？ 到达了初始位置，不在需要绘制          
            if(distance > 5)  {
            	scrollviewToClose.setScrollY(distance);
            	//Log.e("----222alpha----- = " + String.valueOf(255 - scrollviewToClose.getScrollY()));
            	stillsleepLinear.setAlpha(((float)(500 - scrollviewToClose.getScrollY())) /500 );
            	digitalclock.setAlpha(((float)(500 - scrollviewToClose.getScrollY())) /500 );
                mHandler.postDelayed(BackDragImgTask, BACK_DURATION);  
            } else { //复原初始场景  
                resetViewState();     
            }                 
        }  
    };  
    private Handler mHandler =new Handler ();
    //aurora add by tangjun 2013.12.16 end

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
        int y = (int) event.getY();  
        //Log.i(TAG, "onTouchEvent Y is " + y);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            downInRect = handleActionDownEvenet(event);
            timeCount = System.currentTimeMillis();
            shakeListener.stop();   //cjs add   first stop the shakeListener
            //处理Action_Down事件：  判断是否点击了滑动区域  
            break;
        case MotionEvent.ACTION_MOVE:
        	//Log.e("----111alpha----- = " + String.valueOf(255 - scrollviewToClose.getScrollY()));
        	stillsleepLinear.setAlpha(((float)(500 - scrollviewToClose.getScrollY())) /500 );
        	digitalclock.setAlpha(((float)(500 - scrollviewToClose.getScrollY())) /500 );
        	break;  
        case MotionEvent.ACTION_UP:  
            //处理Action_Up事件：  判断是否解锁成功，成功则结束我们的Activity ；否则 ，缓慢回退该图片。
        	Log.e("handleActionUpEvent : ACTION_UP -->isTouchable--" + isTouchable);
        	if ( isTouchable && downInRect && scrollviewToClose.getScrollY() > 0) {
        		isTouchable = false;
        		timeCount = System.currentTimeMillis() - timeCount;
        		handleActionUpEvent(event);
        	}
        	
        	downInRect = false;
        	break; 
        }
        
        if ( downInRect ) {
        	return false;
        }
        return true;
	}
}
