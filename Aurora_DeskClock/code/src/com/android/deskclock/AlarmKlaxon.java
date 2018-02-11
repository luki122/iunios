/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Vibrator;
import aurora.preference.AuroraPreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.webkit.WebView.PrivateAccess;
import android.provider.MediaStore;
import aurora.provider.AuroraSettings;
import android.provider.Settings;

import java.io.IOException;

import com.android.internal.telephony.ITelephony;
//Gionee <baorui><2013-05-27> modify for CR00798633 begin
//import com.mediatek.featureoption.FeatureOption;
import com.aurora.utils.FeatureOption;



//Gionee <baorui><2013-05-27> modify for CR00798633 end
//android:liuying 2012-5-10 modify for CR00596852 start
import android.os.SystemProperties;
//android:liuying 2012-5-10 modify for CR00596852 end
//Gionee <baorui><2013-03-22> modify for CR00783443 begin
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;



//Gionee <baorui><2013-03-22> modify for CR00783443 end
//Gionee <baorui><2013-05-06> modify for CR00803588 begin
import com.aurora.utils.GnRingtoneUtil;
//Gionee <baorui><2013-05-06> modify for CR00803588 end

/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play if
 * another activity overrides the AlarmAlert dialog.
 */
public class AlarmKlaxon extends Service {
	// Default of 10 minutes until alarm is silenced.
	// android zjy 20120424 add for CR00576747 start
	/*
	 * private static final String DEFAULT_ALARM_TIMEOUT = "10";
	 */
	// android:liuying 2012-5-10 modify for CR00596852 begin
	private static String DEFAULT_ALARM_TIMEOUT = "180";
	private static final Boolean gnMMXflag = SystemProperties.get(
			"ro.gn.oversea.custom").equals("INDIA_MICROMAX");
	static {
		if (gnMMXflag == true) {
			DEFAULT_ALARM_TIMEOUT = "40";
		}
	}
	// android:liuying 2012-5-10 modify for CR00596852 end
	// android zjy 20120424 add for CR00576747 end
	/*
	 * Retry to play rintone after 1 seconds if power off alarm can not find
	 * external resource.
	 */
	private static final int MOUNT_TIMEOUT_SECONDS = 1;

	private static final long[] sVibratePattern = new long[] { 500, 500 };
	private static final int GIMINI_SIM_1 = 0;
	private static final int GIMINI_SIM_2 = 1;
	/** the times to retry play rintone */
	private int mRetryCount = 0;
	private static final int MAX_RETRY_COUNT = 3;

	private boolean mPlaying = false;
	private Vibrator mVibrator;
	private MediaPlayer mMediaPlayer;
	private Alarm mCurrentAlarm;
	private long mStartTime;
	private TelephonyManager mTelephonyManager;
	//private ITelephony mTelephonyService;
	private int mCurrentCallState;

	/* Whether the alarm is using an external alert. */
	private boolean mUsingExternalUri;

	// Internal messages
	private static final int KILLER = 1000;
	private static final int DELAY_TO_PLAY = 1001;

	private static final String POWER_OFF_FROM_ALARM = "isPoweroffAlarm";
	private boolean mbootFromPoweroffAlarm;
	// Gionee baorui 2012-10-09 modify for CR00709641 begin
	private static String DEFAULT_AUTO_SILENCE = "1";
	// Gionee baorui 2012-10-09 modify for CR00709641 end
	
    // Gionee <baorui><2013-03-22> modify for CR00783443 begin
    private int mCountUp = 0;
    private int mCountDown = 0;
    private boolean mIsUpsideTurn = false;
    private boolean mIsDownsideTurn = false;
    private SensorManager mSensorMgr;
    private Sensor mGravitySensor;
    private Sensor mAccelerometerSensor;
    private boolean mIsExistGravitySensor = false;
    private float mXData = 0;
    private float mYData = 0;
    private boolean mBoo = false;
    private boolean mBoo1 = false;
    private boolean mBoo2 = false;
    private boolean mBoo3 = false;
    private boolean mBoo4 = false;
    private boolean mBoo5 = false;
    private boolean mBoo6 = false;
    
    private BroadcastReceiver mStopAlarmRingReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            if(intent == null) return;
            String action = intent.getAction();
            if ( action.equals("com.android.deskclock.stoptalarmring") ) {
            	Log.e("-----receive  com.android.deskclock.stoptalarmring-----");
            	pauseMediaPlayer();
            	
            	if (mVibrator != null) {

					mVibrator.cancel();

				}
            }
        }
    };
    
    private BroadcastReceiver mPhoneRingingReceiver = new BroadcastReceiver() {
    	
		
		@Override
		public void onReceive(Context context, Intent intent) {

			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				mMediaPlayer.start();
				mVibrator.vibrate(sVibratePattern, 0);
			}
			
			if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				pauseMediaPlayer();
				mVibrator.cancel();
			}

			// TODO Auto-generated method stub	
		}
	};

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            Log.v("sensorChanged (" + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2] + ")");
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (event.values[SensorManager.DATA_Z] > 8) {
                    mCountUp++;
                } else {
                    mCountUp = 0;
                }

                if (event.values[SensorManager.DATA_Z] < -8) {
                    mCountDown++;
                } else {
                    mCountDown = 0;
                }

                if (mCountUp >= 5) {
                    mIsUpsideTurn = true;
                }

                if (mCountDown >= 5 && true == mIsUpsideTurn) {
                    mIsDownsideTurn = true;
                }

                if (!mIsExistGravitySensor) {
                    mIsExistGravitySensor = true;
                }
                
                Log.v("GravitySensor:mCountUp = " + mCountUp + " , mCountDown = " + mCountDown
                        + " , mIsUpsideTurn = " + mIsUpsideTurn + " , mIsDownsideTurn = " + mIsDownsideTurn
                        + " , mIsExistGravitySensor = " + mIsExistGravitySensor);

                if (true == mIsDownsideTurn) {
                    if (AlarmAlertFullScreen.mInstanse != null) {
                        Log.v("gnSnooze() begin");
                        AlarmAlertFullScreen.mInstanse.gnSnooze();
                        mCountUp = 0;
                        mCountDown = 0;
                        mIsUpsideTurn = false;
                        mIsDownsideTurn = false;
                        mIsExistGravitySensor = false;
                    }
                }
            }
            
            if (!mIsExistGravitySensor) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // By the value of Z and X or Z and Y value judgment whether to reverse it
                    if (10 > event.values[SensorManager.DATA_Z] && event.values[SensorManager.DATA_Z] > 8) {
                        mCountUp++;
                    } else {
                        mCountUp = 0;
                    }

                    if (-12 < event.values[SensorManager.DATA_Z] && event.values[SensorManager.DATA_Z] < -8) {
                        mCountDown++;
                    } else {
                        mCountDown = 0;
                    }
                    if (mCountUp >= 5) {
                        mIsUpsideTurn = true;
                    }
                    mXData = event.values[SensorManager.DATA_X];
                    mYData = event.values[SensorManager.DATA_Y];

                    // Flip horizontal
                    if (mXData > -2 && mXData < 2) {
                        mBoo1 = true;
                    }

                    if (true == mBoo1 && ((10 > mXData && mXData > 6) || (-10 < mXData && mXData < -6))) {
                        mBoo2 = true;
                    }

                    if (true == mBoo2 && (mXData > -2 && mXData < 2)) {
                        mBoo3 = true;
                    }

                    if (true == mBoo1 && true == mBoo2 && true == mBoo3) {
                        mBoo = true;
                    }

                    // Vertical flip
                    if (mYData > -2 && mYData < 2) {
                        mBoo4 = true;
                    }

                    if (true == mBoo4 && ((10 > mYData && mYData > 5) || (-10 < mYData && mYData < -5))) {
                        mBoo5 = true;
                    }

                    if (true == mBoo5 && (mYData > -2 && mYData < 2)) {
                        mBoo6 = true;
                    }

                    if (true == mBoo4 && true == mBoo5 && true == mBoo6) {
                        mBoo = true;
                    }
                    if (mCountDown >= 5 && true == mIsUpsideTurn && true == mBoo) {
                        mIsDownsideTurn = true;
                    }

                    Log.v("AccelerometerSensor:mCountUp = " + mCountUp + " , mCountDown = " + mCountDown
                            + " , mIsUpsideTurn = " + mIsUpsideTurn + " , mIsDownsideTurn = " + mIsDownsideTurn);
                    
                    Log.v("mXData = " + mXData + " , mYData = " + mYData + " , mBoo1 = " + mBoo1
                            + " , mBoo2 = " + mBoo2 + " , mBoo3 = " + mBoo3 + " , mBoo4 = " + mBoo4
                            + " , mBoo5 = " + mBoo5 + " , mBoo = " + mBoo);

                    if (true == mIsDownsideTurn) {
                        if (AlarmAlertFullScreen.mInstanse != null) {
                            Log.v("gnSnooze() begin");
                            AlarmAlertFullScreen.mInstanse.gnSnooze();
                            mCountUp = 0;
                            mCountDown = 0;
                            mIsUpsideTurn = false;
                            mIsDownsideTurn = false;
                            mBoo = false;
                            mBoo1 = false;
                            mBoo2 = false;
                            mBoo3 = false;
                            mBoo4 = false;
                            mBoo5 = false;
                            mBoo6 = false;
                        }
                    }
                }
            } else {
                if (mAccelerometerSensor != null) {
                    Log.v("mAccelerometerSensor unregister");
                    mSensorMgr.unregisterListener(mSensorEventListener, mAccelerometerSensor);
                    mAccelerometerSensor = null;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };
    // Gionee <baorui><2013-03-22> modify for CR00783443 end

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case KILLER:
				Log.v("*********** Alarm killer triggered ***********");
				// android 20120504 zjy add for CR00576747 start
				/* stopPlayAlert((Alarm) msg.obj); */
				sendRepeatRingBroadcast((Alarm) msg.obj);
				// android 20120504 zjy add for CR00576747 end
				break;

			case DELAY_TO_PLAY:
				Log.v("Alarm play external ringtone failed, retry to play after 1 seconds.");
				play((Alarm) msg.obj);
				break;

			default:
				break;
			}
		}
	};

	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			// The user might already be in a call when the alarm fires. When
			// we register onCallStateChanged, we get the initial in-call state
			// which kills the alarm. Check against the initial call state so
			// we don't kill the alarm during a call.
			int newPhoneState = TelephonyManager.CALL_STATE_IDLE;
			
			//aurora mod by tangjun 2013.12.25 start
			/*
			if (mTelephonyService != null) {
				try {
					newPhoneState = mTelephonyService.getPreciseCallState();
				} catch (RemoteException ex) {
					Log.v("Catch exception when getPreciseCallState: ex = "
							+ ex.getMessage());
				}
			}
			*/
			if (mTelephonyManager != null) {
				try {
					newPhoneState = mTelephonyManager.getCallState();
				} catch (Exception ex) {
					Log.v("Catch exception when getPreciseCallState: ex = "
							+ ex.getMessage());
				}
			}
			//aurora mod by tangjun 2013.12.25 end

			if (newPhoneState == TelephonyManager.CALL_STATE_IDLE) {
				mCurrentCallState = TelephonyManager.CALL_STATE_IDLE;
			}

			Log.v("onCallStateChanged : current state = " + newPhoneState
					+ ",state = " + state + ",mInitialCallState = "
					+ mCurrentCallState);
			if (newPhoneState != TelephonyManager.CALL_STATE_IDLE
					&& newPhoneState != mCurrentCallState) {
				Log.v("Call state changed: mInitialCallState = "
						+ mCurrentCallState + ",mCurrentAlarm = "
						+ mCurrentAlarm);
				if (mCurrentAlarm != null) {
					stopPlayAlert(mCurrentAlarm);
				}
			}
		}
	};

	@Override
	public void onCreate() {
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Listen for incoming calls to kill the alarm.
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//mTelephonyService = ITelephony.Stub.asInterface(ServiceManager
		//		.getService(Context.TELEPHONY_SERVICE));

		// Check if the device is gemini supported
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mTelephonyManager.listenGemini(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE
							| PhoneStateListener.LISTEN_SERVICE_STATE,
					GIMINI_SIM_1);
			mTelephonyManager.listenGemini(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE
							| PhoneStateListener.LISTEN_SERVICE_STATE,
					GIMINI_SIM_2);
		} else {
			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
		}
		*/
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		//aurora mod by tangjun 2013.12.25 end
		
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mediaActionMonitor, filter);
        //Gionee <jiating><2013-08-22> modify for CR00858473 begin
        
        IntentFilter filter2 = new IntentFilter();
        //监听计时器响铃广播  aurora add by tangjun 2014.3.4 start
        Log.e("--register com.android.deskclock.stoptalarmring---");
        filter2.addAction("com.android.deskclock.stoptalarmring");
        //监听计时器响铃广播  aurora add by tangjun 2014.3.4 end
        registerReceiver(mStopAlarmRingReceiver, filter2);
        
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mPhoneRingingReceiver, filter3);
        
        // Gionee <baorui><2013-03-22> modify for CR00783443 begin

        if (AuroraSettings.getInt(getApplicationContext().getContentResolver(), AuroraSettings.GN_SSG_SWITCH, 0) == 1 && AuroraSettings.getInt(getApplicationContext().getContentResolver(), "ssg_delay_alarm", 0) == 1) {
            mSensorMgr = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
            mGravitySensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
            mAccelerometerSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.v("mGravitySensor = " + mGravitySensor + " , mAccelerometerSensor = " + mAccelerometerSensor);
        }
        // Gionee <baorui><2013-03-22> modify for CR00783443 end
        //Gionee <jiating><2013-08-22> modify for CR00858473 end
	}

	@Override
	public void onDestroy() {
		
		Log.e("---AlarmKlaxon onDestroy-----");
		mMediaVolumeHandler.removeMessages(RING_RAISE);
		
		// Gionee baorui 2012-09-25 modify for CR00703590 begin
		Alarms.mIfDismiss = false;
		Alarms.mAlarmId = -1;
		// Gionee baorui 2012-09-25 modify for CR00703590 end
		stop();
		// Stop listening for incoming calls.
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mTelephonyManager
					.listenGemini(mPhoneStateListener, 0, GIMINI_SIM_1);
			mTelephonyManager
					.listenGemini(mPhoneStateListener, 0, GIMINI_SIM_2);
		} else {
			mTelephonyManager.listen(mPhoneStateListener, 0);
		}
		*/
		mTelephonyManager.listen(mPhoneStateListener, 0);
		//aurora mod by tangjun 2013.12.25 end
		
		mHandler.removeMessages(DELAY_TO_PLAY);
		Log.v("mHandler.removeMessages DELAY_TO_PLAY");
		AlarmAlertWakeLock.releaseCpuLock();
		super.onDestroy();
		Log.e("--unregister com.android.deskclock.stoptalarmring---");
		unregisterReceiver(mStopAlarmRingReceiver);
        unregisterReceiver(mediaActionMonitor);
        unregisterReceiver(mPhoneRingingReceiver);
        //Gionee <jiating><2013-08-22> modify for CR00858473 begin
        // Gionee <baorui><2013-03-22> modify for CR00783443 begin
        if (AuroraSettings.getInt(getApplicationContext().getContentResolver(), AuroraSettings.GN_SSG_SWITCH, 0) == 1 && Settings.System.getInt(getApplicationContext().getContentResolver(), "ssg_delay_alarm", 0) == 1) {
        //Gionee <jiating><2013-08-22> modify for CR00858473 end
        	Log.v("Sensor unregister");
            if (mGravitySensor != null) {
                Log.v("mGravitySensor unregister");
                mSensorMgr.unregisterListener(mSensorEventListener, mGravitySensor);
            }
            if (mAccelerometerSensor != null) {
                Log.v("mAccelerometerSensor unregister");
                mSensorMgr.unregisterListener(mSensorEventListener, mAccelerometerSensor);
            }
        }
        // Gionee <baorui><2013-03-22> modify for CR00783443 end

        // Gionee <baorui><2013-07-31> modify for CR00836546 begin
        gnAbandonAudioFocus(null);
        // Gionee <baorui><2013-07-31> modify for CR00836546 end
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// No intent, tell the system not to restart us.
		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		final Alarm alarm = intent
				.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

		if (alarm == null) {
			Log.v("AlarmKlaxon failed to parse the alarm from the intent");
			stopSelf();
			return START_NOT_STICKY;
		}

		if (intent.getBooleanExtra(POWER_OFF_FROM_ALARM, false)
				&& Alarms.bootFromPoweroffAlarm()) {
			mbootFromPoweroffAlarm = true;
			Log.v("AlarmKlaxon mbootFromPoweroffAlarm is true");
		}
		if (mCurrentAlarm != null && mCurrentAlarm.time != alarm.time
				&& mCurrentAlarm.id != alarm.id) {
			Log.v("*********** onStartCommand ***********");
			if (mbootFromPoweroffAlarm) {
				long millis = System.currentTimeMillis() - mStartTime;
				int minutes = (int) Math.round(millis / 60000.0);
				Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
				alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, mCurrentAlarm);
				alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
				alarmKilled.putExtra("dismissAlarm", true);
				Log.v("sendKillBroadcast: mStartTime = " + mStartTime
						+ ",millis = " + millis + ",minutes = " + minutes
						+ ",this = " + this);
				sendBroadcast(alarmKilled);
			} else {
				sendKillBroadcast(mCurrentAlarm);
			}
		}

		Log.v("onStartCommand: intent = " + intent + "alarm id = " + alarm.id
				+ ",alert = " + alarm.alert);
		if (alarm.alert != null) {
			mUsingExternalUri = usingExternalUri(alarm.alert);
		}

		play(alarm);
		mCurrentAlarm = alarm;
		// Record the initial call state here so that the new alarm has the
		// newest state.
		
		//aurora mod by tangjun 2013.12.25 start
		/*
		if (mTelephonyService != null) {
			try {
				mCurrentCallState = mTelephonyService.getPreciseCallState();
			} catch (RemoteException ex) {
				Log.v("Catch exception when getPreciseCallState: ex = "
						+ ex.getMessage());
			}
		}
		*/
		
		if (mTelephonyManager != null) {
			try {
				mCurrentCallState = mTelephonyManager.getCallState();
			} catch (Exception ex) {
				Log.v("Catch exception when getPreciseCallState: ex = "
						+ ex.getMessage());
			}
		}
		//aurora mod by tangjun 2013.12.25 end
		
		//Gionee <jiating><2013-08-22> modify for CR00858473 begin
        // Gionee <baorui><2013-03-22> modify for CR00783443 begin
        if (AuroraSettings.getInt(getApplicationContext().getContentResolver(), AuroraSettings.GN_SSG_SWITCH, 0) == 1 && AuroraSettings.getInt(getApplicationContext().getContentResolver(), "ssg_delay_alarm", 0) == 1) {
        //Gionee <jiating><2013-08-22> modify for CR00858473 begin 
        	Log.v("Sensors register");
            if (mGravitySensor != null) {
                Log.v("mGravitySensor register");
                mSensorMgr.registerListener(mSensorEventListener, mGravitySensor,
                        SensorManager.SENSOR_DELAY_GAME);
            }
            if (mAccelerometerSensor != null) {
                Log.v("mAccelerometerSensor register");
                mSensorMgr.registerListener(mSensorEventListener, mAccelerometerSensor,
                        SensorManager.SENSOR_DELAY_GAME);
            }
        }
        // Gionee <baorui><2013-03-22> modify for CR00783443 end

		return START_STICKY;
	}

	// android 20120504 zjy add for CR00576747 start
	private void sendRepeatRingBroadcast(Alarm alarm) {
		Log.e("---sendRepeatRingBroadcast-----");
		stopSelf();
		long millis = System.currentTimeMillis() - mStartTime;
		int minutes = (int) Math.round(millis / 60000.0);
		Intent alarmRepeat = new Intent();
		
		//aurora mod by tangjun 2013.12.28 start  修改成贪睡3次就直接关闭
		if ( AlarmAlertFullScreen.alarmTimes == 3 ) {
			alarmRepeat = alarmRepeat.setAction(Alarms.ALARM_DISMISS_ACTION);
			AlarmAlertFullScreen.alarmTimes = 1;
		} else {
			alarmRepeat = alarmRepeat.setAction(Alarms.ALARM_REPEAT_RING);
		}
		//aurora mod by tangjun 2013.12.28 end 
		
		alarmRepeat.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		sendBroadcast(alarmRepeat);
	}

	// android 20120504 zjy add for CR00576747 end
	private void sendKillBroadcast(Alarm alarm) {
		long millis = System.currentTimeMillis() - mStartTime;
		int minutes = (int) Math.round(millis / 60000.0);
		Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
		alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
		Log.v("sendKillBroadcast: mStartTime = " + mStartTime + ",millis = "
				+ millis + ",minutes = " + minutes + ",this = " + this);
		sendBroadcast(alarmKilled);
	}

	// Volume suggested by media team for in-call alarms.
	private static final float IN_CALL_VOLUME = 0.125f;

	private void play(Alarm alarm) {
		
		//是否是勿扰模式  add by tangjun 2013.12.26 start
		boolean isDontDisturb = AuroraPreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_DONTDISTURB, false);
		//是否是勿扰模式  add by tangjun 2013.12.26 end
		
		// stop() checks to see if we are already playing.
		stop();

		Log.v("AlarmKlaxon.play() " + alarm.id + " alert " + alarm.alert);
		
        // Gionee baorui 2012-12-12 modify for CR00738567 begin
        // Gionee baorui 2013-01-17 modify for CR00763726 begin
        boolean mIsSilent = true;
        // Gionee baorui 2013-01-17 modify for CR00763726 end
        if (RingtoneManager.isDefault(alarm.alert)) {
            // Gionee <baorui><2013-05-21> modify for CR00814151 begin
            /*
            String uri = Settings.System.getString(getApplicationContext().getContentResolver(),
                    Settings.System.ALARM_ALERT);
            */
            String uri = Settings.System.getString(getContentResolver(), Settings.System.ALARM_ALERT);
            // Gionee <baorui><2013-05-21> modify for CR00814151 end
            if (uri == null) {
                // Gionee baorui 2013-01-17 modify for CR00763726 begin
                mIsSilent = false;
                // Gionee baorui 2013-01-17 modify for CR00763726 end
            }
        }
        // Gionee baorui 2012-12-12 modify for CR00738567 end

        // Gionee baorui 2013-01-17 modify for CR00763726 begin

        Log.e("--alarm.silent = " + alarm.silent + ", mIsSilent = " + mIsSilent);
        //aurora mod by tangjun 2013.12.27 勿扰模式不响铃
        if (!alarm.silent && mIsSilent && !isDontDisturb) {
        // Gionee baorui 2013-01-17 modify for CR00763726 end
			Uri alert = alarm.alert;
			// Fall back on the default alarm if the database does not have an
			// alarm stored.
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
				Log.v("Using default alarm: " + alert.toString());
			}
			
			//aurora add by tangjun 2013.12.26 start 都用系统当前默认的铃声
			//aurora modify by zhanjiandong 2014.12.17每个闹钟对应一个铃声
//			alert = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
	        Log.e("AlarmKlaxon --- alert = " + alert);
            //aurora add by tangjun 2013.12.26 end
			
            // Gionee <baorui><2013-07-16> modify for CR00836845 begin
//            alert = UpdateAlert(this, alert, alarm);
            // Gionee <baorui><2013-07-16> modify for CR00836845 end

			// TODO: Reuse mMediaPlayer instead of creating a new one and/or use
			// RingtoneManager.
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e("Error occurred while playing audio.");
					mp.stop();
					mp.release();
					mMediaPlayer = null;
					return true;
				}
			});

			try {
				// Check if we are in a call. If we are, use the in-call alarm
				// resource at a low volume to not disrupt the call.
				
				//aurora mod by tangjun 2013.12.25 start
				/*
				if (mTelephonyService != null) {
					try {
						mCurrentCallState = mTelephonyService.getPreciseCallState();
					} catch (RemoteException ex) {
						Log.v("Catch exception when getPreciseCallState: ex = " + ex.getMessage());
					}
				}
				*/
				
				if (mTelephonyManager != null) {
					try {
						mCurrentCallState = mTelephonyManager.getCallState();
					} catch (Exception ex) {
						Log.v("Catch exception when getPreciseCallState: ex = " + ex.getMessage());
					}
				}
				//aurora mod by tangjun 2013.12.25 end
				
				if (mCurrentCallState != TelephonyManager.CALL_STATE_IDLE) {
					Log.v("Using the in-call alert: mUsingExternalUri = "
							+ mUsingExternalUri);
					mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
					setDataSourceFromResource(getResources(), mMediaPlayer,
							R.raw.in_call_alarm);
				} else {
					try {
						mMediaPlayer.setDataSource(this, alert);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						mMediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
					}
				}
				startAlarm(mMediaPlayer);
			} catch (IOException ex) {
				Log.v("Exception occured mUsingExternalUri = "
						+ mUsingExternalUri);
				Log.v("Exception occured retryCount = " + mRetryCount);
				if (mUsingExternalUri && mRetryCount < MAX_RETRY_COUNT) {
					delayToPlayAlert(alarm);
					// Reset it to false.
					// mUsingExternalUri = false;
					mRetryCount++;
					mStartTime = System.currentTimeMillis();
					return;
				} else {
					Log.v("Using the fallback ringtone");
					// The alert may be on the sd card which could be busy right
					// now. Use the fallback ringtone.
					try {
						// Must reset the media player to clear the error state.
						try {
							mMediaPlayer.reset();
							alert = RingtoneManager
									.getDefaultUri(RingtoneManager.TYPE_ALARM);
							mMediaPlayer.setDataSource(this, alert);
							startAlarm(mMediaPlayer);
						} catch (Exception exception) {
							mMediaPlayer.reset();
							setDataSourceFromResource(getResources(),
									mMediaPlayer, R.raw.in_call_alarm);
							startAlarm(mMediaPlayer);
						}
					} catch (IOException ioe2) {
						// At this point we just don't play anything.
						Log.e("Failed to play fallback ringtone", ioe2);
					}
				}
			}
		}
        //aurora add by tangjun 2013.12.27 start 按策划要求统一用设置里面的振动了
        boolean tmpvibrate =  AuroraPreferenceManager.getDefaultSharedPreferences(this).getBoolean("default_vibrate", true);
        //aurora add by tangjun 2013.12.27 end
        
		/* Start the vibrator after everything is ok with the media player */
        //aurora add by tangjun 2013.12.27 start 按策划要求统一用设置里面的振动了
        if (tmpvibrate || isDontDisturb) {
    	//if (alarm.vibrate) {
        //aurora add by tangjun 2013.12.27 end
			mVibrator.vibrate(sVibratePattern, 0);
		} else {
			mVibrator.cancel();
		}

		enableKiller(alarm);
		mPlaying = true;
		mStartTime = System.currentTimeMillis();
	}

	//铃声大小渐变 aurora add by tangjun 2014.3.18 
	private static final int RING_RAISE = 1234;
    private Handler mMediaVolumeHandler = new Handler() {  
    	  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            switch (msg.what) {  
            case RING_RAISE:  
                //MediaPlayer player = (MediaPlayer)msg.obj;
                //Log.e("---mMediaVolumeHandler arg2 = ---" + msg.arg2);
                //Log.e("---mMediaVolumeHandler arg1 = ---" + msg.arg1);
            	if(mMediaPlayer!=null)
            	{
            	    mMediaPlayer.setVolume( (float)msg.arg2/msg.arg1, (float)msg.arg2/msg.arg1 );
            	}
                if ( msg.arg2 < msg.arg1 ) {
                	msg.arg2 ++;
                	mMediaVolumeHandler.sendMessageDelayed(mMediaVolumeHandler.obtainMessage(RING_RAISE, msg.arg1, msg.arg2), 2000);
                }
            	
                break;  
            }  
        }
    }; 
	// Do the common stuff when starting the alarm.
	private void startAlarm(MediaPlayer player) throws java.io.IOException,
			IllegalArgumentException, IllegalStateException {
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//铃声前先监听电话状态
		 TelephonyManager mTelephonyMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);  
				 mTelephonyMgr.listen(new TeleListener(),  
				PhoneStateListener.LISTEN_CALL_STATE);  
        // Gionee <baorui><2013-07-31> modify for CR00836546 begin
        gnRequestAudioFocus(audioManager);
        // Gionee <baorui><2013-07-31> modify for CR00836546 end

		// do not play alarms if stream volume is 0
		// (typically because ringer mode is silent).
		Log.v("the audio volume: "
				+ audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
			
			//铃声大小渐变 aurora add by tangjun 2014.3.18 
			player.setVolume(0, 0);
			int mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			mMediaVolumeHandler.sendMessageDelayed(mMediaVolumeHandler.obtainMessage(RING_RAISE, mediaVolume, 1), 1000);
		}
		
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}

	/**
	 * Stops alarm audio and disables alarm if it not snoozed and not repeating
	 */
	public void stop() {
		Log.v("AlarmKlaxon.stop().");
		if (mPlaying) {
			mPlaying = false;

			Intent alarmDone = new Intent(Alarms.ALARM_DONE_ACTION);
			sendBroadcast(alarmDone);

			// Stop audio playing
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

			// Stop vibrator
			mVibrator.cancel();
		}
		disableKiller();
	}
	
	/**
	 * 暂停闹铃声音 aurora add by tangjun 2014.3.5
	 */
	private void pauseMediaPlayer( ) {
		if (mPlaying) {

			// Pause audio playing
			if (mMediaPlayer != null) {
				mMediaPlayer.pause();
				
			}
		}
	}

	/**
	 * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm won't run all
	 * day.
	 * 
	 * This just cancels the audio, but leaves the notification popped, so the
	 * user will know that the alarm tripped.
	 */
	private void enableKiller(Alarm alarm) {
		Log.v("enableKiller: alarm = " + alarm + ",this = " + this);
		// Gionee baorui 2012-10-09 modify for CR00709641 begin
        //android:liuying 2012-5-10 modify for CR00596852 begin
        /*
        final String autoSnooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_AUTO_SILENCE,
                        DEFAULT_ALARM_TIMEOUT);
        */
        final String autoSnooze =
                AuroraPreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_AUTO_SILENCE,
                		DEFAULT_AUTO_SILENCE);
		// Gionee baorui 2012-10-09 modify for CR00709641 end
		int autoSnoozeMinutes = Integer.parseInt(autoSnooze);
		if (autoSnoozeMinutes != -1) {
			mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
					1000 * autoSnoozeMinutes * 60);
		}
		// android zjy 20120424 add for CR00576747 start
		else if (autoSnoozeMinutes == -1) {
			int autoSnoozeseconds = Integer.parseInt(DEFAULT_ALARM_TIMEOUT);
			mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
					1000 * autoSnoozeseconds);
		}
		// android zjy 20120424 add for CR00576747 end
		// android:liuying 2012-5-10 modify for CR00596852 end
	}

	private void disableKiller() {
		mHandler.removeMessages(KILLER);
	}

	private void delayToPlayAlert(Alarm alarm) {
		Log.v("delayToPlayAlert: alarm = " + alarm + ",this = " + this);
		mHandler.sendMessageDelayed(
				mHandler.obtainMessage(DELAY_TO_PLAY, alarm),
				1000 * MOUNT_TIMEOUT_SECONDS);
	}

	private boolean usingExternalUri(Uri alert) {
		Uri mediaUri = null;
		final String scheme = alert.getScheme();
		if ("content".equals(scheme)) {
			if (AuroraSettings.AUTHORITY.equals(alert.getAuthority())) {
				String uriString = android.provider.Settings.System.getString(
						this.getContentResolver(), "alarm_alert");
				if (uriString != null) {
					mediaUri = Uri.parse(uriString);
				} else {
					mediaUri = alert;
				}
			} else {
				mediaUri = alert;
			}

			if (MediaStore.AUTHORITY.equals(mediaUri.getAuthority())) {
				Log.v("AlarmKlaxon onStartCommand mediaUri = " + mediaUri
						+ ",segment 0 = " + mediaUri.getPathSegments().get(0));
				if (mediaUri.getPathSegments().get(0)
						.equalsIgnoreCase("external")) {
					// Alert is using an external ringtone.
					return true;
				}
			}
		}
		return false;
	}

	private void stopPlayAlert(Alarm alarm) {
		Log.v("stopPlayAlert: alarm = " + alarm);
		mHandler.removeMessages(DELAY_TO_PLAY);
		sendKillBroadcast(alarm);
		stopSelf();
		// Gionee baorui 2012-10-09 modify for CR00706052 begin
		Alarms.mIfDismiss = false;
		Alarms.mAlarmId = -1;
		// Gionee baorui 2012-10-09 modify for CR00706052 end
	}
    private BroadcastReceiver mediaActionMonitor = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            if(intent == null) return;
            String action = intent.getAction();
            if(Intent.ACTION_MEDIA_EJECT.equals(action) || 
                    Intent.ACTION_MEDIA_UNMOUNTED.equals(action)){
                if(mMediaPlayer != null){
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }
    };

    // Gionee <baorui><2013-07-16> modify for CR00836845 begin
    private Uri UpdateAlert(Context context, Uri oldUri, Alarm alarm) {
        String mData = Alarms.getAlertInfoStr(context, alarm.id);
        int mVolumes = Alarms.getVolumes(context, alarm.id);
        Uri newUri = oldUri;

        if (!isRingtoneExist(oldUri, context)) {
            if (Alarms.isUpdateRintoneUri(mData, oldUri, context, mVolumes)) {
                newUri = Alarms.updateRintoneUri(mData, oldUri, context, mVolumes);
            }
        }

        return newUri;
    }

    private boolean isRingtoneExist(Uri uri, Context context) {
        Uri tempUri = uri;

        if (RingtoneManager.isDefault(uri)) {
            String mUriStr = Settings.System.getString(context.getContentResolver(),
                    Settings.System.ALARM_ALERT);
            tempUri = Uri.parse(mUriStr);
        }

        return GnRingtoneUtil.isRingtoneExist(tempUri, context.getContentResolver());
    }
    // Gionee <baorui><2013-07-16> modify for CR00836845 end

    // Gionee <baorui><2013-07-31> modify for CR00836546 begin
    private void gnRequestAudioFocus(AudioManager audioManager) {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        audioManager
                .requestAudioFocus(null, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void gnAbandonAudioFocus(AudioManager audioManager) {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        audioManager.abandonAudioFocus(null);
    }
    // Gionee <baorui><2013-07-31> modify for CR00836546 end
    //监听电话状态，通话中状态或响铃状态时暂停铃声和震动
 	class TeleListener extends PhoneStateListener {
 		@Override
 		public void onCallStateChanged(int state, String incomingNumber) {
 			super.onCallStateChanged(state, incomingNumber);
 			switch (state) {
 			case TelephonyManager.CALL_STATE_IDLE: {

 				break;
 			}
 			case TelephonyManager.CALL_STATE_OFFHOOK: {
 				pauseMediaPlayer( );
 				 mVibrator.cancel();

 				break;
 			}
 			case TelephonyManager.CALL_STATE_RINGING: {
 				pauseMediaPlayer( );
 				 mVibrator.cancel();
 				break;
 			}
 			default:
 				break;
 			}
 		}
 	}
}
