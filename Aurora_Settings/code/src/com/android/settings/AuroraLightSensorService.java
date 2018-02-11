package com.android.settings;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gionee.settings.utils.GnUtils;

import android.location.LocationManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.R.integer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.ServiceManager;
import android.os.IPowerManager;
import android.os.storage.StorageManager;

import android.os.IHardwareService;

public class AuroraLightSensorService extends Service implements SensorEventListener{
	private boolean mAutomaticMode;
    private SensorManager mSensorManager;
    private Sensor mLigntSensor;
    private int mSensorValue;
	private int mLastScreenBright;
	private int mLastDefaultLightLevel;
	
	//private ArrayList<Integer> mListLowest;
	private ArrayList<Integer> mListA;
	private ArrayList<Integer> mListB;
	private ArrayList<Integer> mListC;
	private ArrayList<Integer> mListD;
	private ArrayList<Integer> mListE;
	private ArrayList<Integer> mListF;
	
	//private static final int LIGHT_LEVEL_LOWEST = 5;
	private static final int LIGHT_LEVEL_A = 15;
	private static final int LIGHT_LEVEL_B = 30;
	private static final int LIGHT_LEVEL_C = 300;
	private static final int LIGHT_LEVEL_D = 700;
	private static final int LIGHT_LEVEL_E = 10000;
	
	//private static final int DEFAULT_LIGHT_LEVEL_LOWEST = 3;
	private static final int DEFAULT_LIGHT_LEVEL_A = 10;
	private static final int DEFAULT_LIGHT_LEVEL_B = 20;
	private static final int DEFAULT_LIGHT_LEVEL_C = 200;
	private static final int DEFAULT_LIGHT_LEVEL_D = 500;
	private static final int DEFAULT_LIGHT_LEVEL_E = 1000;
	private static final int DEFAULT_LIGHT_LEVEL_F = 15000;
	
	private static final int SCREEN_LEVEL_A = 25;
	private static final int SCREEN_LEVEL_B = 40;
	private static final int SCREEN_LEVEL_C = 155;
	private static final int SCREEN_LEVEL_D = 210;
	private static  int SCREEN_LEVEL_E = 245;    
	private static  int SCREEN_LEVEL_F = 255;
	
	private static int DEFAULT_SCREEN_LEVEL_A =  23;//20;
	private static int DEFAULT_SCREEN_LEVEL_B = 50;//30;
	private static int DEFAULT_SCREEN_LEVEL_C = 160;//150;
	private static int DEFAULT_SCREEN_LEVEL_D = 195;//190;
	private static int DEFAULT_SCREEN_LEVEL_E = 225;
	private static int DEFAULT_SCREEN_LEVEL_F = 255;
	
	// deal with the shortcut brightness
	public static final String ACTION_SEEKBAR_BRIGHTNESS_VALUE = "com.android.settings.action.SEEKBAR_BRIGHTNESS_VALUE";
	private boolean mIsRegisterAutoLightSwitchReceiver = false;
    private boolean isAutoBrightnessValidate = true; // battery low
    
    private AuroraSettingsInit mAuroraSettingsInit;
    private final static String TAG = "AuroraLightSensorService";
    
    private  boolean isStartSample =true;	
	private  boolean isHandlerSend = false;
	private static final int SAMPLE_TIME = 2000;
	private static final int SLEEP_TIME = 2000;
	private static final int SET_BRIGHT = 0;
	private static final int REGEST_SENSOR = 1;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			  case SET_BRIGHT:
				  handleSensorValue();
				  resetValue();
				  break;
			  case REGEST_SENSOR:
				  registerLightSensor();
				  break;
			}
		}
		
	};
    
	private final BroadcastReceiver mStatesChangeReceiver =  new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			//Log.v(TAG, "----mStatesChangeReceiver-----action===="+action);
    		if(Intent.ACTION_SCREEN_ON.equals(action)){
    			if(mAutomaticMode){    				
    				registerLightSensor();
    				registAutoLighSwithReceiver();
    				//set default screen level
    				setDefaultScreenLevel();
    				//reset 
    				resetValue();
    			}
    		}else if(Intent.ACTION_SCREEN_OFF.equals(action)){
    			if(mAutomaticMode){
    				isStartSample = false;
    				saveDefaultScreenLevel();
					unregisterLightSensor();
					unregistAutoLighSwithReceiver();
    			}
    		}else if (action.equals(Intent.ACTION_BATTERY_CHANGED)){
    			int level = intent.getIntExtra("level", 0);
    			int scale = intent.getIntExtra("scale", 100);
    			int status = intent.getIntExtra("status", 0);
    			if(level*100/scale <=5){ 
    				isAutoBrightnessValidate = false;
    			}else{
    				isAutoBrightnessValidate = true;
    			}
    			if(status == BatteryManager.BATTERY_STATUS_CHARGING){
    				isAutoBrightnessValidate = true;
    			}
    		}else if(action.equals(AuroraBrightnessPreference2.BRIGHTNESS_SEEKBAR_TOUCH_ACTION)){
    			if(mAutomaticMode){
    				int size  = intent.getIntExtra("seekbar_size",-1);
    				//Log.v(TAG, "----------mAutoLightSwitchReceiver--------seekbar_size ==="+size);
        			if(size != -1){
        				setBrightnessByManualSlide(size);
        			}
    			}
    			
    		}else if(action.equals(ACTION_SEEKBAR_BRIGHTNESS_VALUE)){
    			if(mAutomaticMode){
					int slide = intent.getIntExtra("SEEKBAR_BRIGHTNESS_VALUE", -1);
	    			if(slide != -1){
	    				setBrightnessByManualSlide(slide);
	    			}
			  }    	
    		}
		}
	};
	
	private final BroadcastReceiver mAutoLightSwitchReceiver =  new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			//Log.v(TAG, "----mAutoLightSwitchReceiver-----action===="+action);
			if (action.equals(AuroraBrightnessPreference2.BRIGHTNESS_SEEKBAR_TOUCH_ACTION) && mAutomaticMode){	
    			int size  = intent.getIntExtra("seekbar_size",-1);
    			//Log.v(TAG, "----------mAutoLightSwitchReceiver--------seekbar_size ==="+size);
    			if(size != -1){
    				setBrightnessByManualSlide(size);
    			}
			}else if(action.equals(ACTION_SEEKBAR_BRIGHTNESS_VALUE)){ 
				if(mAutomaticMode){
					int slide = intent.getIntExtra("SEEKBAR_BRIGHTNESS_VALUE", -1);
	    			if(slide != -1){
	    				setBrightnessByManualSlide(slide);
	    			}
			  }    			
    	   }
		}
	};
	
	// auto-brightness observer
	 private ContentObserver mAutoBrightnessObserver = new ContentObserver(new Handler()) {
	        @Override
	        public void onChange(boolean selfChange) {
	        	mAutomaticMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
	        	if(mAutomaticMode){
					registerLightSensor();
					registAutoLighSwithReceiver();
    				setDefaultScreenLevel();
    				resetValue();
				}else{
    				isStartSample = false;
    				saveDefaultScreenLevel();
					unregisterLightSensor();
					unregistAutoLighSwithReceiver();
				}
	        }
	 };

    private void setDefaultScreenLevel(){
			SharedPreferences iuniSP = getSharedPreferences("iuni", Context.MODE_PRIVATE);
			DEFAULT_SCREEN_LEVEL_A = iuniSP.getInt("screen_level_a", 23); 
			DEFAULT_SCREEN_LEVEL_B = iuniSP.getInt("screen_level_b", 50); 
			DEFAULT_SCREEN_LEVEL_C = iuniSP.getInt("screen_level_c", 160); 
			DEFAULT_SCREEN_LEVEL_D = iuniSP.getInt("screen_level_d", 195); 
			DEFAULT_SCREEN_LEVEL_E = iuniSP.getInt("screen_level_e", 225); 
			DEFAULT_SCREEN_LEVEL_F = iuniSP.getInt("screen_level_f", 255); 
			/*
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_A------=="+DEFAULT_SCREEN_LEVEL_A);
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_B------=="+DEFAULT_SCREEN_LEVEL_B);
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_C------=="+DEFAULT_SCREEN_LEVEL_C);
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_D------=="+DEFAULT_SCREEN_LEVEL_D);
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_E------=="+DEFAULT_SCREEN_LEVEL_E);
			Log.v(TAG, "------DEFAULT_SCREEN_LEVEL_F------=="+DEFAULT_SCREEN_LEVEL_F);
			*/
   }
	
   private void saveDefaultScreenLevel(){
		SharedPreferences iuniSP = getSharedPreferences("iuni", Context.MODE_PRIVATE);
	    Editor et = iuniSP.edit();
	    et.putInt("screen_level_a", DEFAULT_SCREEN_LEVEL_A); 
	    et.putInt("screen_level_b", DEFAULT_SCREEN_LEVEL_B); 
	    et.putInt("screen_level_c", DEFAULT_SCREEN_LEVEL_C); 
	    et.putInt("screen_level_d", DEFAULT_SCREEN_LEVEL_D); 
	    et.putInt("screen_level_e", DEFAULT_SCREEN_LEVEL_E); 
	    et.putInt("screen_level_f", DEFAULT_SCREEN_LEVEL_F); 
		et.commit();
  }
   
   //
   private void setBrightnessByManualSlide(int seekbarSize){
	   mHandler.removeMessages(SET_BRIGHT);
	   isStartSample = false;
	   int minEffectiveSlidePoint = 0;
	   int maxEffectiveSlidePoint = 0;
	   int middlePoint = 0;
	   int bright = 0;
	   
	   setLastDefaultScreenLevel();
	   
	   if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_A){
		   middlePoint = SCREEN_LEVEL_A /2;
		   minEffectiveSlidePoint = 0;
		   maxEffectiveSlidePoint = (int)(255 * 0.2);
		   
		   if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
			   if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
		    	   float result = (float)(seekbarSize - minEffectiveSlidePoint) / (maxEffectiveSlidePoint -minEffectiveSlidePoint);
		    	   DEFAULT_SCREEN_LEVEL_A =  Math.round(result * SCREEN_LEVEL_A);;
		      }
		   }
	   }else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_B){
		      middlePoint = (SCREEN_LEVEL_A +  SCREEN_LEVEL_B) /2;
		      minEffectiveSlidePoint = middlePoint - (int)(255 * 0.1);
		      maxEffectiveSlidePoint = middlePoint + (int)(255 * 0.1);
		      if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
		    	   float result = (float)(seekbarSize - minEffectiveSlidePoint) / (maxEffectiveSlidePoint -minEffectiveSlidePoint);
		    	   int size = Math.round(result *(SCREEN_LEVEL_B -SCREEN_LEVEL_A));
		    	   DEFAULT_SCREEN_LEVEL_B = SCREEN_LEVEL_A +  size;
		      }
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_C){
			  middlePoint = (SCREEN_LEVEL_B +  SCREEN_LEVEL_C)/2;
		      minEffectiveSlidePoint = middlePoint - (int)(255 * 0.2);
		      maxEffectiveSlidePoint = middlePoint + (int)(255 * 0.2);
		      if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
		    	   float result = (float)(seekbarSize - minEffectiveSlidePoint) / (maxEffectiveSlidePoint -minEffectiveSlidePoint);
		    	   int size = Math.round(result *(SCREEN_LEVEL_C -SCREEN_LEVEL_B));
		    	   DEFAULT_SCREEN_LEVEL_C = SCREEN_LEVEL_B +  size;
		      }
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_D){
			  middlePoint = (SCREEN_LEVEL_C +  SCREEN_LEVEL_D)/2;
		      minEffectiveSlidePoint = middlePoint - (int)(255 * 0.2);
		      maxEffectiveSlidePoint = middlePoint + (int)(255 * 0.2);
		      
		      if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
		    	   float result = (float)(seekbarSize - minEffectiveSlidePoint) / (maxEffectiveSlidePoint -minEffectiveSlidePoint);
		    	   int size = Math.round(result *(SCREEN_LEVEL_D -SCREEN_LEVEL_C));
		    	   DEFAULT_SCREEN_LEVEL_D = SCREEN_LEVEL_C +  size;
		      }
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_E){
			  middlePoint = (SCREEN_LEVEL_D +  SCREEN_LEVEL_E)/2;
		      minEffectiveSlidePoint = middlePoint - (int)(255 * 0.2);
		      maxEffectiveSlidePoint = middlePoint + (int)(255 * 0.2);
		      
		      if(seekbarSize>= minEffectiveSlidePoint && seekbarSize < maxEffectiveSlidePoint){
		    	   float result = (float)(seekbarSize - minEffectiveSlidePoint) / (maxEffectiveSlidePoint -minEffectiveSlidePoint);
		    	   int size = Math.round(result *(SCREEN_LEVEL_E -SCREEN_LEVEL_D));
		    	   DEFAULT_SCREEN_LEVEL_E = SCREEN_LEVEL_D +  size;
		      }
		}
	    setAutomaticModeBrightness(seekbarSize);
   }	 
	 
	private void registerLightSensor(){
		if(mSensorManager == null){
			mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			mLigntSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mSensorManager.registerListener(AuroraLightSensorService.this, mLigntSensor, SensorManager.SENSOR_DELAY_UI);
        }
	}
	
	private void unregisterLightSensor(){
		if(mSensorManager != null){
			mSensorManager.unregisterListener(this);
			mSensorManager = null;
		}
	}
	
	private void registAutoLighSwithReceiver(){
		if(mIsRegisterAutoLightSwitchReceiver){
			IntentFilter autoLightFilter = new IntentFilter();
			autoLightFilter.addAction(AuroraBrightnessPreference2.BRIGHTNESS_SEEKBAR_TOUCH_ACTION);	
			autoLightFilter.addAction(ACTION_SEEKBAR_BRIGHTNESS_VALUE);
			registerReceiver(mAutoLightSwitchReceiver, autoLightFilter);
			mIsRegisterAutoLightSwitchReceiver =true;
		}
	}
	private void unregistAutoLighSwithReceiver(){
		if(mIsRegisterAutoLightSwitchReceiver){
			unregisterReceiver(mAutoLightSwitchReceiver);
			mIsRegisterAutoLightSwitchReceiver =false;
		}
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();	
		mAuroraSettingsInit = new AuroraSettingsInit(this);
		mAuroraSettingsInit.init();
				
		SharedPreferences iuniSP = getSharedPreferences("iuni",
				Context.MODE_PRIVATE);
		boolean isFirstSet = iuniSP.getBoolean("is_set_screen_brightness", true);
		if(isFirstSet){
			if(GnUtils.isAbroadVersion()){
			Settings.System.putInt( getContentResolver(),
	                	Settings.System.SCREEN_BRIGHTNESS, 255);
			}else{
				Settings.System.putInt( getContentResolver(),
	                	Settings.System.SCREEN_BRIGHTNESS, 130);
			}
			
			if(GnUtils.isAbroadVersion()){
					Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
			}
			else{
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
			}
        	Editor et = iuniSP.edit();
            et.putBoolean("is_set_screen_brightness", false);
    		et.commit();
        }
		
		mAutomaticMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
		if(mAutomaticMode){
			mHandler.sendEmptyMessageDelayed(REGEST_SENSOR, 3000);
		}
		
		getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mAutoBrightnessObserver);

		IntentFilter filter = new IntentFilter();
    	filter.addAction(Intent.ACTION_SCREEN_ON);
    	filter.addAction(Intent.ACTION_SCREEN_OFF);	
    	filter.addAction(Intent.ACTION_BATTERY_CHANGED);
    	filter.addAction(AuroraBrightnessPreference2.BRIGHTNESS_SEEKBAR_TOUCH_ACTION);
    	filter.addAction(ACTION_SEEKBAR_BRIGHTNESS_VALUE);
    	registerReceiver(mStatesChangeReceiver, filter);
    	
    	mListA = new ArrayList<Integer>();
    	mListB = new ArrayList <Integer>();
    	mListC = new ArrayList <Integer>();
    	mListD = new ArrayList <Integer>();
    	mListE = new ArrayList <Integer>();
    	mListF = new ArrayList <Integer>();
    	
    	//Begin add for Screen and button both on
    	if(SystemProperties.getBoolean("ro.aurora.screenbuttonbothon", false)){
    		IntentFilter filter2 = new IntentFilter();
    		filter2.addAction(Intent.ACTION_SCREEN_ON);
    		registerReceiver(mScreenAndButtonBothOn, filter2);
    	}
    	//End add for Screen and button both on
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//Log.v(TAG, "--------onStartCommand------");
		mAutomaticMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;	
		if(mAutomaticMode){
			registAutoLighSwithReceiver();
		}else{
			unregistAutoLighSwithReceiver();	
			int brightness = Settings.System.getInt( getContentResolver(),
	                Settings.System.SCREEN_BRIGHTNESS, 100);   	
    		try {
	            IPowerManager power = IPowerManager.Stub.asInterface(
	                    ServiceManager.getService("power"));
	            if (power != null) {
	                power.setTemporaryScreenBrightnessSettingOverride(brightness);
	            }
			} catch (RemoteException doe) {
	        }
		}
		return flags =  START_STICKY;
	}
	
	//
	private void setLastDefaultScreenLevel(){
		int brightness = Settings.System.getInt( getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 100);
		//Log.v(TAG, "-------last brightness-------==="+brightness);
		if(brightness <= SCREEN_LEVEL_A){
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_A;
		}else if(brightness <= SCREEN_LEVEL_B ){
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_B;
		}else if(brightness <= SCREEN_LEVEL_C){
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_C;
		}else if(brightness <= SCREEN_LEVEL_D){
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_D;
		}else if(brightness <= SCREEN_LEVEL_E){
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_E;
		}else {
			mLastDefaultLightLevel = DEFAULT_LIGHT_LEVEL_F;
		}
	}
	
	private void setAutomaticModeBrightness(int brightness){
		 //Log.v(TAG, "----------setAutomaticModeBrightness--------bright=="+brightness);//
	     if(isAutoBrightnessValidate){
	            Settings.System.putInt(getContentResolver(),
	                    Settings.System.SCREEN_BRIGHTNESS, brightness);
	    }
	}
	
	private int getTotal(ArrayList <Integer> list){
		if(list == null || list.isEmpty()){
			return 0;
		}
	
		int total=0;
		for(int i=0;i<list.size();i++){
			total += list.get(i);
		}
		return total;
	}
	
	private int getAverage(ArrayList <Integer> list){
		if(list == null || list.isEmpty()){
			return 0;
		}
		
		int total=0;
		for(int i=0;i<list.size();i++){
			total += list.get(i);
		}
		return Math.round(total/list.size());
	}
	
	private void resetValue(){
		mListA.clear();
		mListB.clear();
		mListC.clear();
		mListD.clear();
		mListE.clear();
		mListF.clear();
		isStartSample = true;
		isHandlerSend = false;
	}
	
	public void handleSensorValue(){
		//Log.v(TAG, "------handleSensorValue----");
		isStartSample = false;
		int totalCount = mListA.size() + mListB.size() + mListC.size() + mListD.size() + mListE.size() + mListF.size();
		
		int nextValue = 0;
		if(totalCount >= 5){
			int valuableCount = totalCount *60/100;
			if(mListA.size() >= valuableCount){
				nextValue = getAverage(mListA);
				//Log.v(TAG, "------mListA----");
			}else if(mListB.size() >= valuableCount){
				//Log.v(TAG, "------mListB--");
				nextValue = getAverage(mListB);
			}else if(mListC.size() >= valuableCount){
				nextValue = getAverage(mListC);
				//Log.v(TAG, "------mListC----");
			}else if(mListD.size() >= valuableCount){
				nextValue = getAverage(mListD);
				//Log.v(TAG, "------mListD----");
			}else if(mListE.size() >= valuableCount){
				nextValue = getAverage(mListE);
				//Log.v(TAG, "------mListE----");
			}else if(mListF.size() >= valuableCount){
				nextValue = getAverage(mListF);
				//Log.v(TAG, "------mListF----");
			}
		}else{
			if(totalCount >= 1){
				nextValue = Math.round((getTotal(mListA)+getTotal(mListB)+ getTotal(mListC)+ getTotal(mListD)
					          + getTotal(mListE)+ getTotal(mListF))/totalCount) ;
				//Log.v(TAG, "------totalCount < 5----nextValue=="+nextValue);
			}
		}   
		
		setLastDefaultScreenLevel();
		
		int Y_X = nextValue - mLastDefaultLightLevel;
		int X_Y = mLastDefaultLightLevel - nextValue;
		//Log.v(TAG, "------x----=="+mLastDefaultLightLevel);
			
		if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_A){
			if(Y_X > 1000){
					setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_E);
			}else if(Y_X > 500){
					setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_D);
			}else if(Y_X > 200){
					setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_C);
			}else if(Y_X >15 && Y_X <= 200){
					setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_B);
			}
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_B){
			if(Y_X > 500){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_D);
			}else if(Y_X > 200){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_C);
			}else if(X_Y > 10){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_A);
			}
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_C){
			if(X_Y > 100){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_B);
			}else if(Y_X > 500){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_D);
			}
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_D){
			if(X_Y > 350){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_C);
			}else if(Y_X > 1000){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_E);
			}
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_E){
			if(X_Y > 700){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_D);
			}else if(Y_X > 3000){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_F);
			}
		}else if(mLastDefaultLightLevel == DEFAULT_LIGHT_LEVEL_F){
			if(X_Y > 12000){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_D);
			}else if(X_Y > 145000){
				setAutomaticModeBrightness(DEFAULT_SCREEN_LEVEL_C);
			}
		}
	}
	
	 @Override
	public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
		 	mSensorValue = (int)event.values[0];
		    String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");  
		    if(deviceName.contains("OnePlusOne")){
		    	mSensorValue  = mSensorValue + 15; 
		    }
	       
		    if(isStartSample){
		    	//Log.v(TAG, "------mSensorValue----=="+mSensorValue);
		    	//sendBroadcast(new Intent("com.android.auroralightsensorservice.sensor").putExtra("sensor_value",mSensorValue )); 
		    	if(!isHandlerSend){
		    		mHandler.sendEmptyMessageDelayed(SET_BRIGHT, SAMPLE_TIME);
		    		isHandlerSend = true;
		    	}
		    	
		    	if(mSensorValue < LIGHT_LEVEL_A){
		    		mListA.add(mSensorValue);
		    	}else if(mSensorValue < LIGHT_LEVEL_B){
		    		mListB.add(mSensorValue);
		    	}else if(mSensorValue < LIGHT_LEVEL_C){
		    		mListC.add(mSensorValue);
		    	}else if(mSensorValue < LIGHT_LEVEL_D){
		    		mListD.add(mSensorValue);
		    	}else if(mSensorValue < LIGHT_LEVEL_E){
		    		mListE.add(mSensorValue);
		    	}else{
		    		mListF.add(mSensorValue);
		    	}
		    }
	}
	    

    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
	}
		
		@Override
	public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
	}

		@Override
	public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
		    if(mStatesChangeReceiver !=null){
					unregisterReceiver(mStatesChangeReceiver);
			}
		    
			if(mSensorManager !=null){
				mSensorManager.unregisterListener(AuroraLightSensorService.this);
			}
			if(mAutoBrightnessObserver !=null){
				getContentResolver().unregisterContentObserver(mAutoBrightnessObserver);
			}		
			unregistAutoLighSwithReceiver();				
  }	

	private final BroadcastReceiver mScreenAndButtonBothOn = new BroadcastReceiver() {

		IHardwareService mLight;
		private boolean mButtonLightEnabled;

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mButtonLightEnabled = Settings.System.getInt(
					context.getContentResolver(), "button_key_light", 1) == 0 ? false
					: true;
			if (mButtonLightEnabled) {
				mLight = IHardwareService.Stub.asInterface(ServiceManager
						.getService("hardware"));
				if (mLight != null) {
					if(PlatformUtils.isSupportScreenAndButtonOn()){
						PlatformUtils.setButtonLightEnabled(mLight);
					}
				} else {
					Log.v("SettingGary", "mLight is null");
				}

			}
		}
	};
}
