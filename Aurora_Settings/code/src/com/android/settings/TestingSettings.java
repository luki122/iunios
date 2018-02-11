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


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.os.SystemProperties;
import android.provider.Settings;

import android.util.Log;
import android.view.View;

public class TestingSettings extends AuroraPreferenceActivity 
        implements OnPreferenceChangeListener {

    private AuroraSwitchPreference mLogOnoffSwitch; 
    private final String TESTING_LOG_ONOFF_KEY = "settings_testing_log";
    private boolean mIsChecked = false;
    
    private final static String TAG = "TestingSettings";
    private AuroraPreferenceCategory mCalibrationCategory;
    private AuroraButtonPerence mCalibration;
    private final String TESTING_CALIBRATION = "aurora_calibration";
    private ProgressDialog mProgressDialog = null;
    private boolean mScreenBrightOpen;
    
    public final int SHOW_DIALOG = 0;
    public final int BEGIN_CALIB = 1;
    public final int END_CALIB = 2;
    
    private static final int CAL_FAIL = 0;
    
    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			  case SHOW_DIALOG:
				   if(mProgressDialog == null){
					   mProgressDialog = new ProgressDialog(TestingSettings.this);
					   mProgressDialog.setTitle(R.string.aurora_calibration_title);
					   mProgressDialog.setMessage(getString(R.string.aurora_calibration_dialog_tip));
					   mProgressDialog.setCancelable(false);
					   mProgressDialog.setCanceledOnTouchOutside(false);
					   mProgressDialog.show();
					   mHandler.sendEmptyMessageDelayed(BEGIN_CALIB, 500);
				   }
				break;
			
			   case BEGIN_CALIB:
				   //开始发校准命令给驱动
				   int result =  SensorTest.runNativeSensorTest(40, 0, 5, true, true);
				   Log.v(TAG, "-------result----=="+result);
				   //返回值为0才算校准成功
				   if(result != 0){
					   showDialog(CAL_FAIL);
				   }else{
					   mHandler.sendEmptyMessageDelayed(END_CALIB, 2000);
				   }
				   break;
			   case END_CALIB:
				   if(mProgressDialog != null){
					   mProgressDialog.cancel();
					   mProgressDialog = null;
				   }
				   endCalibration();
				   break;
			}
		}
    	
    };
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee fangbin 20120619 added for CR00622030 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120619 added for CR00622030 end
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.testing_settings);
        mLogOnoffSwitch = (AuroraSwitchPreference)findPreference(TESTING_LOG_ONOFF_KEY);
        if (mLogOnoffSwitch != null) {
            if (SystemProperties.get("persist.sys.aurora.debug").equals("yes")) {
                //Log.v("xiaoyong", "get debug yes true");
                mIsChecked = true;
            } else {
                //Log.v("xiaoyong", "get debug no false");
                mIsChecked =false;
            }
            mLogOnoffSwitch.setChecked(mIsChecked);
            mLogOnoffSwitch.setOnPreferenceChangeListener(this); 
        }
        
        mCalibrationCategory = (AuroraPreferenceCategory)findPreference("aurora_calibration_category");
        mCalibration = (AuroraButtonPerence)findPreference("aurora_calibration");
        
        String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
        String[] strName = deviceName.split("-");
        
        if(strName.length > 1){
        	if(strName[1].contains("U3")||strName[1].contains("i1")){
        		mCalibration.setOnClickListener(new View.OnClickListener() {
        			
    				@Override
    				public void onClick(View v) {
    					// 	TODO Auto-generated method stub
    					beginCalibration();
    					//关闭光感需要一定时间，首先先弹出对话框后，再进行校准
    					mHandler.sendEmptyMessage(SHOW_DIALOG);
    				}
    			});
        	}else{
        		getPreferenceScreen().removePreference(mCalibrationCategory);
        		getPreferenceScreen().removePreference(mCalibration);
        	}
        }else{
        	getPreferenceScreen().removePreference(mCalibrationCategory);
        	getPreferenceScreen().removePreference(mCalibration);
        }
        
    }

    
    public void beginCalibration(){
    	//校准时，需要关闭自动亮度
    	if(Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0){
    		mScreenBrightOpen = true;
			Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
		}else{
			mScreenBrightOpen = false;
		}
    	
    }
    
    public void endCalibration(){
    	if(mScreenBrightOpen){
    		Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
    	}
    }
    
    protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case CAL_FAIL:
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
			builder.setMessage(R.string.aurora_calibration_failed)
			       .setCancelable(false)
				   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessageDelayed(BEGIN_CALIB,2000);
					}
					   
				   })
			       .setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mHandler.sendEmptyMessage(END_CALIB);
					}
			    	   
			       });
			dialog = builder.create();
			break;
		}
		return dialog;
	}
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        // TODO Auto-generated method stub
        boolean checked = ((Boolean)newValue).booleanValue();

        if (checked) {
            SystemProperties.set("persist.sys.aurora.debug", "yes");
        } else {
            SystemProperties.set("persist.sys.aurora.debug", "no");
        }

        return true;
    }

}
