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

package com.android.systemui.recent;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ImageButton;
import aurora.preference.AuroraPreference;
import com.android.systemui.R;

import com.android.systemui.totalCount.CountUtil;

public class AuroraBrightnessView extends LinearLayout implements SeekBar.OnSeekBarChangeListener{
    // If true, enables the use of the screen auto-brightness adjustment setting.
    //update to 5.0 begin
/*    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT =
            PowerManager.useScreenAutoBrightnessAdjustmentFeature();*/
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = true;
    //update to 5.0 end
    private final int mScreenBrightnessMinimum;
    private final int mScreenBrightnessMaximum;
    private static boolean mAutomaticMode = false;
//    private boolean isSave = true;
    // QY 

    private  int mLightSensorAdj;
    public static final String ACTION_AUTO_ADJ_LIGHT = "com.android.settings.action.AUTO_ADJ_LIGHT";
    public static final String ACTION_SEEKBAR_BRIGHTNESS_VALUE = "com.android.settings.action.SEEKBAR_BRIGHTNESS_VALUE";
    

    private SeekBar mSeekBar;
    ImageButton mAutoBrightnessImageButton;

    private boolean mAutomaticAvailable;
    

    private int mCurBrightness = -1;

    private static final int SEEK_BAR_RANGE = 10000;
    private static  boolean isStartThread = false;
    private AuroraBrightView mAuroraBrightView;
    private Context mContext;

	// Aurora <steve.tang> 2014-08-20 do not run thread as user control seekbar. start
	private boolean mIsFromUser;
	// Aurora <steve.tang> 2014-08-20 do not run thread as user control seekbar. end
    
    
    

   /* private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mCurBrightness = -1;
//            onBrightnessChanged();
            Log.i("qy", "brightnes*****");
        }
    };*/

    /*private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };*/

    public AuroraBrightnessView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setLayoutResource(R.layout.aurora_preference_brightness_layout);
//        setWidgetLayoutResource(R.layout.aurora_preference_widget_brightness);
        mContext = context;
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
        mScreenBrightnessMaximum = pm.getMaximumScreenBrightnessSetting();
        //qy test
//        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
//        mLigntSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
//        mSensorManager.registerListener(this, mLigntSensor, SensorManager.SENSOR_DELAY_NORMAL);
//       IntentFilter filter = new IntentFilter(ACTION_AUTO_ADJ_LIGHT);
//        context.registerReceiver(mAutoAdjLightReceiver, filter);
        
        try {
        int br = Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS);
        
        } catch (SettingNotFoundException snfe) {
        }
        

        

        mAutomaticAvailable = context.getResources().getBoolean(
                R.bool.config_automatic_brightness_available);

        /*context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);

        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);*/
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
//        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
//        mSeekBar.setMax(SEEK_BAR_RANGE);
//        mSeekBar.setProgress(getBrightness());
//
//        /*if (mAutomaticAvailable) {
//            mAutomaticMode = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
//            mSeekBar.setEnabled(!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT);
//        } else {
//            mSeekBar.setEnabled(true);
//        }*/
//        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
    	mAuroraBrightView.rotate(progress*3/250);
//    	if(isSave){
    		setBrightness(progress, false);
//    	}else{
//    		setBrightness(progress, false);
//    	}
//        setBrightness(progress, true);
		// Aurora <steve.tang> 2014-08-20 whether is control by user. start
		mIsFromUser = fromTouch;
		// Aurora <steve.tang> 2014-08-20 whether is control by user. end
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
		// Aurora <Steve.Tang> 2015-03-02, count brightness change counts. start
		CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_021, 1);
		// Aurora <Steve.Tang> 2015-03-02, count brightness change counts. end
    	mAuroraBrightView.rest();
        setBrightness(seekBar.getProgress(), true);
    }

	// Aurora <Steve.Tang> 2015-03-02, count brightness change counts. start

    /*private void onBrightnessChanged() {
        mSeekBar.setProgress(getBrightness());
    }*/

//    private void onBrightnessModeChanged() {
//        mAutomaticMode = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
//        setBrightness(mSeekBar.getProgress(), false);
     /*   mSeekBar.setProgress(getBrightness());
//        mSeekBar.setEnabled(!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT);
        if (!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT) {
        	setBrightness(mSeekBar.getProgress(), false);
        }*/
//    }

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
        /*if (mAutomaticMode) {
        	int range = (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
        	brightness = (brightness * range) / SEEK_BAR_RANGE + MIN_BRIGHTNESS ;
        	
            if (USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT) {         	
            	
                float valf = (((float) brightness * 2) / SEEK_BAR_RANGE) - 1.0f;
                try {
                    IPowerManager power = IPowerManager.Stub.asInterface(
                            ServiceManager.getService("power"));
                    if (power != null) {
                        power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf);
                    }
                    if (write) {
                        final ContentResolver resolver = getContext().getContentResolver();
                        Settings.System.putFloat(resolver,
                                Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
                    }
                } catch (RemoteException doe) {
                }
            }
        	
        	
        	
        } else {*/
            int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
            brightness = (brightness * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                if (power != null) {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness);
                }
                if (write) {
                    mCurBrightness = -1;
                    final ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putInt(resolver,
                            Settings.System.SCREEN_BRIGHTNESS, brightness);
                    Intent intent = new Intent(ACTION_SEEKBAR_BRIGHTNESS_VALUE);
                    intent.putExtra("SEEKBAR_BRIGHTNESS_VALUE", brightness);
                    mContext.sendBroadcast(intent);
                } else {
                    mCurBrightness = brightness;
                }
            } catch (RemoteException doe) {
            }
//        }
       
    }

    private void setMode(int mode) {
//        mAutomaticMode = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        /*Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);*/
    	
    }
    
    public void stop(){
//    	mSensorManager.unregisterListener(this);
//    	getContext().getContentResolver().unregisterContentObserver(mBrightnessObserver);
//    	getContext().getContentResolver().unregisterContentObserver(mBrightnessModeObserver);
//    	getContext().unregisterReceiver(mAutoAdjLightReceiver);
    }
    
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//   	    super.onLayout(changed, left, top, right, bottom);
//     /*if (mAutomaticAvailable) {
//         mAutomaticMode = getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
//         mSeekBar.setEnabled(!mAutomaticMode || USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT);
//     } else {
//         mSeekBar.setEnabled(true);
//     }*/
//    }

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
//		isSave = false;
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
	    mSeekBar.setMax(SEEK_BAR_RANGE);
	    mSeekBar.setProgress(getBrightness());
	    mSeekBar.setOnSeekBarChangeListener(this);
	    mAuroraBrightView = (AuroraBrightView)findViewById(R.id.aurora_bright_view);
		mAuroraBrightView.rest();
		
		mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                true, mScreenBrightnessChangeObserver);


        mAutoBrightnessImageButton = (ImageButton)findViewById(R.id.aurora_auto_brightness);
        if (mAutoBrightnessImageButton != null) {
			mAutomaticMode = Settings.System.getInt(mContext.getContentResolver(), 
			    Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
		    Log.v("Baisha", "mAutomaticMode = " + mAutomaticMode);	
		    if (mAutomaticMode) {
                mAutoBrightnessImageButton.setBackgroundResource(R.drawable.aurora_auto_brightness_yes);  
            } else {
                mAutoBrightnessImageButton.setBackgroundResource(R.drawable.aurora_auto_brightness_no);        
            }

		    mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                    mAutoBrightnessObserver);

		    mAutoBrightnessImageButton.setOnClickListener(new ImageButton.OnClickListener() {
			    public void onClick(View v) {
				    Log.v("Baisha", "setOnClickListener, mAutomaticMode" + mAutomaticMode);
					// Aurora <Steve.Tang> 2015-03-02, count brightness mode click counts. start
					CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_020, 1);
					// Aurora <Steve.Tang> 2015-03-02, count brightness mode click counts. end
				    if (mAutomaticMode) {
					    Settings.System.putInt(mContext.getContentResolver(), 
						    Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
					    Log.v("Baisha", "putInt 0");
				    } else {
				        Settings.System.putInt(mContext.getContentResolver(), 
						    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
					    Log.v("Baisha", "putInt 1");
				    }
			    }
		    });
        }
	}
	
    protected void onDetachedFromWindow() {
    	mContext.getContentResolver().unregisterContentObserver(mScreenBrightnessChangeObserver);
    }
	
	
	
	 private ContentObserver mScreenBrightnessChangeObserver = new ContentObserver(new Handler()) {
	        @Override
	        public void onChange(boolean selfChange) {
				// Aurora <steve.tang> 2014-08-20 seek bar changed smoothly. start
		    	//mSeekBar.setProgress(getBrightness());
				if(mSeekBar != null){
					int init = progressToData(mSeekBar.getProgress());
					int end = Settings.System.getInt(getContext().getContentResolver(),
			                Settings.System.SCREEN_BRIGHTNESS, 100);
					Log.i("qy1", "init = "+init );
					Log.i("qy1", "end = "+end );
					mIsFromUser = false;
					if((end - init)!=0){
						new UpdateProgressThread(hd,init,end).start();
					}
				}
				// Aurora <steve.tang> 2014-08-20 seek bar changed smoothly. end
		    }
	 };
	 
	// Aurora <steve.tang> 2014-08-20 seekbar changed smoothly. start
	//thread to update seekbar smoothly
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
			while (t != 0 && !mIsFromUser){
				if(t > 0){
					brightness++;
					t--;
				}
				if(t < 0){
					brightness--;
					t++;
				}
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

	//handle to update seekbar
    private Handler hd = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(mSeekBar != null ){				
				mSeekBar.setProgress(dataToProgress(msg.arg1));
			}
		}
    	
    };

    private int progressToData(int progress){
        int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        return (progress * range) / SEEK_BAR_RANGE + mScreenBrightnessMinimum;
    }
    
    private int dataToProgress(int data){
    	return (int) (data - mScreenBrightnessMinimum)* SEEK_BAR_RANGE
                / (mScreenBrightnessMaximum - mScreenBrightnessMinimum);        

    }
	// Aurora <steve.tang> 2014-08-20 seek bar changed smoothly. end

	// auto-brightness observer
	 private ContentObserver mAutoBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mAutoBrightnessImageButton != null) {
        	    mAutomaticMode = Settings.System.getInt(mContext.getContentResolver(), 
				    Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
		        Log.v("Baisha", "mAutoBrightnessObserver onChange mAutomaticMode = " + mAutomaticMode);	
        	    if (mAutomaticMode) {
			        mAutoBrightnessImageButton.setBackgroundResource(R.drawable.aurora_auto_brightness_yes);
			    } else{
			        mAutoBrightnessImageButton.setBackgroundResource(R.drawable.aurora_auto_brightness_no);
			    }
            }
        }
	 };		
}
