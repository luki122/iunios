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

package com.android.settings;

import com.android.settings.widget.AuroraBrightView;

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
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import aurora.preference.AuroraPreference;

public class AuroraBrightnessPreference2 extends AuroraPreference implements SeekBar.OnSeekBarChangeListener {
    // If true, enables the use of the screen auto-brightness adjustment setting.
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT =
            PowerManager.useScreenAutoBrightnessAdjustmentFeature();

    private final int mScreenBrightnessMinimum;
    private final int mScreenBrightnessMaximum;
    private boolean mAutomaticMode;    
   
    private  int mLightSensorAdj;
    public static final String ACTION_AUTO_ADJ_LIGHT = "com.android.settings.action.AUTO_ADJ_LIGHT";
    
   
    private Context mContext;
    private SeekBar mSeekBar;
    private AuroraBrightView mAuroraBrightView;
    private boolean mAutomaticAvailable;
    

    private int mCurBrightness = -1;

    private static final int SEEK_BAR_RANGE = 10000;      
    
    public static final String BRIGHTNESS_SEEKBAR_TOUCH_ACTION = "com.android.settings.action.BRIGHTNESS_SEEKBAR_TOUCH_ACTION";
    private static final int MODE_WEAK = 1;
	private static final int MODE_NORMAL = 2;
	private static final int MODE_STRONG = 3;
	private static final int MODE_STRONGEST = 4;
	private boolean mIsFromUser;

	//Begin add by gary.gou 
	public final static int NO_DELAY = 0;
	public final static int DELAY = 1;
	
    private Handler mSeekBarHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case AuroraBrightnessPreference2.DELAY:
				mSeekBarHandler.removeMessages(DELAY);
				int brightness =  Settings.System.getInt(getContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,mSeekBar.getProgress());
				if(brightness > mSeekBar.getProgress()){
					mSeekBarHandler.sendEmptyMessageDelayed(NO_DELAY, 500);
	            }else{
	            	mSeekBarHandler.sendEmptyMessage(NO_DELAY);
	            }
				break;
			case AuroraBrightnessPreference2.NO_DELAY:
				 mSeekBarHandler.removeMessages(NO_DELAY);
				 setBrightness(mSeekBar.getProgress(), true);
				break;
			}
		}
    	
    };
    //End add by gary.gou
	

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {

        	if(mSeekBar != null){
//        		mSeekBar.setProgress(getBrightness());
        		int init = progressToData(mSeekBar.getProgress());
        		int end = Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 100);
        		Log.i("qy", "init = "+init );
        		Log.i("qy", "end = "+end );
        		if((end - init)!=0){
        			new UpdateProgressThread(hd,init,end).start();
        		}
        	}
        	
            
        }
    };

    /*private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };*/
  
   
    private Handler hd = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(mSeekBar != null ){				
				mSeekBar.setProgress(dataToProgress(msg.arg1));
			}
		}
    	
    };
    private final class UpdateProgressThread extends Thread{
    	private Handler mHandler;
    	private int mInitBrightness;
    	private int mEndBrightness;
    	
		public UpdateProgressThread(Handler handler, int initBrightness,int endBrightness) {
			
			this.mHandler = handler;
			this.mInitBrightness = initBrightness;
			this.mEndBrightness = endBrightness;
		}

		@Override
		public void run() {
			int t = mEndBrightness - mInitBrightness;
			int brightness = mInitBrightness;
			while (t != 0 ){
				if(t > 0){
					brightness++;
					t--;
				}
				if(t < 0){
					brightness--;
					t++;
				}
//				Log.i("qy", "brightness = "+brightness );

				mHandler.sendMessage(mHandler.obtainMessage(0, brightness, 0));
				try {
		   			Thread.sleep(5); // 50					 	   			
	 	   		} catch (InterruptedException e) {
	 	   			// TODO Auto-generated catch block
	 	   			e.printStackTrace();
	 	   		}
			}
		}
    	
    }
    private int progressToData(int progress){
        int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        return (progress * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
    }
    
    private int dataToProgress(int data){
    	return (int) (data - mScreenBrightnessMinimum)* SEEK_BAR_RANGE
                / (mScreenBrightnessMaximum - mScreenBrightnessMinimum);        

    }
    

    public AuroraBrightnessPreference2(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.aurora_preference_brightness_layout);
        setWidgetLayoutResource(R.layout.aurora_preference_widget_brightness);
        mContext = context;
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
        mScreenBrightnessMaximum = pm.getMaximumScreenBrightnessSetting();
        
        try {
        int br = Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS);
        
        } catch (SettingNotFoundException snfe) {
        }
        

        

        mAutomaticAvailable = context.getResources().getBoolean(
                R.bool.config_automatic_brightness_available);

        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);
        

        /*context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);*/
    }

    protected void onBindView(View view) {
        super.onBindView(view);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mAuroraBrightView = (AuroraBrightView) view.findViewById(R.id.aurora_bright_view);
        mAuroraBrightView.rest();
        
        mSeekBar.setMax(SEEK_BAR_RANGE);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(getBrightness());
        

        /*if (mAutomaticAvailable) {
            mAutomaticMode = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            mSeekBar.setEnabled(!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT);
        } else {
            mSeekBar.setEnabled(true);
        }*/
       
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
    	if(fromTouch){
    		setBrightness(progress, false);
    	}        
        mAuroraBrightView.rotate((int)(progress*3/250));
        mIsFromUser = fromTouch;
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    	mAutomaticMode = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
    	
//    	Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL); // note3 autoBrightness
        
    	//getContext().sendBroadcast(new Intent(BRIGHTNESS_SEEKBAR_TOUCH_ACTION).putExtra("TOUCH",true ));	
    }
    
    

    public void onStopTrackingTouch(SeekBar seekBar) {
        
    	//setBrightness(seekBar.getProgress(), true);
    //	mSeekBarHandler.sendEmptyMessage(AuroraBrightnessPreference2.DELAY); //add by gary.gou
  /*      
     // save
        int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        int br = (seekBar.getProgress() * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
        if(mIsFromUser){
        	saveBrightnessValues(br);
        }*/
 
    	int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        int br = (seekBar.getProgress() * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
        if(mAutomaticMode){
            getContext().sendBroadcast(new Intent(BRIGHTNESS_SEEKBAR_TOUCH_ACTION).putExtra("seekbar_size",br));
        }else{
        	mSeekBarHandler.sendEmptyMessage(AuroraBrightnessPreference2.DELAY);
        }	
    }
    

   /* private void onBrightnessChanged() {
        mSeekBar.setProgress(getBrightness());
    }*/

   /* private void onBrightnessModeChanged() {
//        mAutomaticMode = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        setBrightness(mSeekBar.getProgress(), false);
        mSeekBar.setProgress(getBrightness());
//        mSeekBar.setEnabled(!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT);
        if (!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT) {
        	setBrightness(mSeekBar.getProgress(), false);
        }
    }*/

    /*private int getBrightness() {
        int mode = getBrightnessMode(0);
        float brightness = 0;
        if (USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT
                && mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(getContext().getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0);
           
            brightness = (brightness+1) / 2;
        	// qy 
        	 if (mCurBrightness < 0) {
                 brightness = Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.SCREEN_BRIGHTNESS, 100);
             } else {
                 brightness = mCurBrightness;
             }
             brightness = (brightness - MIN_BRIGHTNESS)
                     / (MAX_BRIGHTNESS - MIN_BRIGHTNESS) -mLightSensorAdj ; // QY END
        	
        } else {
            if (mCurBrightness < 0) {
                brightness = Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 100);
            } else {
                brightness = mCurBrightness;
            }
            brightness = (brightness - mScreenBrightnessMinimum)
                    / (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        }
        return (int) (brightness * SEEK_BAR_RANGE);
    }*/
    
    private int getBrightness() {
//        int mode = getBrightnessMode(0);
        float brightness = 0;
        
        	brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
//        	Log.i("qy",  "AuroraBrightnessPreference2 brightness = "+ brightness);
        	brightness = (brightness - mScreenBrightnessMinimum)
                    / (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
            
        return (int) (brightness * SEEK_BAR_RANGE);
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        }
        return brightnessMode;
    }

    private void setBrightness(int brightness, boolean write) {
      
        int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        brightness = (brightness * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
        
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
            	                	
            	if (write) {
                    mCurBrightness = -1;
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                } else {
                    mCurBrightness = brightness;
                }
            	
                power.setTemporaryScreenBrightnessSettingOverride(brightness);
            }
            
        } catch (RemoteException doe) {
        }
        // save
       /* if(mIsFromUser){
        	saveBrightnessValues(brightness);
        }*/
        

       
    }
    private void saveBrightnessValues(int transferedBrightness){
    	SharedPreferences iuniSP = mContext.getSharedPreferences("iuni", Context.MODE_PRIVATE);    	
//    	boolean autoLight = iuniSP.getBoolean("aurora_automatic", false);
    	boolean autoLight = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
    	int envMode = iuniSP.getInt("env_mode", -1);
    	Intent brightnessIntent = new Intent(BRIGHTNESS_SEEKBAR_TOUCH_ACTION);
    	if(autoLight){
    		
    		Editor et = iuniSP.edit();
    		if(transferedBrightness >=mScreenBrightnessMinimum && transferedBrightness <= 80 && envMode == MODE_WEAK){
    			et.putInt("init_weak", transferedBrightness);
//    			

    		}else if(transferedBrightness >=95 && transferedBrightness <= 160 && envMode == MODE_NORMAL){
    			et.putInt("init_normal", transferedBrightness);
//    			

    		}else if(transferedBrightness >=175 && transferedBrightness <= 210 && envMode == MODE_STRONG){
    			et.putInt("init_strong", transferedBrightness);
//    			

    		}else if(transferedBrightness >=225 && transferedBrightness <= 255 && envMode == MODE_STRONGEST){
    			et.putInt("init_strongest", transferedBrightness);
//    			
    		}
    		brightnessIntent.putExtra("TOUCH_CHANGED", true);
    		et.commit();	
    	}
    	
    	
        getContext().sendBroadcast(brightnessIntent.putExtra("TOUCH",false ));

    }

    private void setMode(int mode) {
//        mAutomaticMode = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        /*Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);*/
    	
    }
    
    public void stop(){
    	
    	getContext().getContentResolver().unregisterContentObserver(mBrightnessObserver);

//    	getContext().getContentResolver().unregisterContentObserver(mBrightnessModeObserver);
    	
    }
}