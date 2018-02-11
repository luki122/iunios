package com.aurora.somatosensory;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.os.Bundle;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.widget.AuroraListView;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.R.integer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import gionee.telephony.GnTelephonyManager;
import android.widget.AbsListView.OnScrollListener;
import android.os.SystemProperties;
import gionee.telephony.AuroraTelephoneManager;
import com.android.settings.AuroraSettingsPreferenceFragment;

import com.android.settings.R;
import android.provider.Settings;

import com.android.settings.nfc.NfcEnabler;
import android.nfc.NfcAdapter;
import aurora.preference.AuroraSwitchPreference;


public class SomatosensorySettings extends AuroraSettingsPreferenceFragment implements AuroraPreference.OnPreferenceChangeListener {
	
	 private static final String TAG = "SomatosensorySettings";
	 
    private static final String KEY_SMART_CALL = "smart_call";
    private static final String KEY_SMART_ANSWER = "smart_answer";
	private static final String KEY_GESTURE_PHOTO = "gesture_photo";
	private static final String KEY_GESTURE_ANSWER = "gesture_answer";
	private static final String KEY_GESTURE_VEDIO = "gesture_vedio";
	private static final String KEY_SOMATOSENSORY_PAUSE = "somatosensory_pause";
    private static final String KEY_SOMATOSENSORY_MUSIC = "somatosensory_music";
    
    private static final String FILE_SOMATOSENSORY_MUSIC = "/sys/bus/platform/devices/tp_wake_switch/gesture_for_music";
    
    public static final String SMART_CALL = "smart_phone_call";
    public static final String SMART_ANSWER = "smart_phone_answer";
    public static final String GESTURE_PHOTO = "smart_gesture_photo";
    public static final String GESTURE_ANSWER = "smart_gesture_answer";
    public static final String GESTURE_VEDIO = "smart_gesture_vedio";
    
	private AuroraSwitchPreference mSmartCall;
	private AuroraSwitchPreference mSmartAnswer;
	private AuroraSwitchPreference mGesturePhoto;
	private AuroraSwitchPreference mGestureAnswer;
	private AuroraSwitchPreference mGestureVedio;
	private AuroraSwitchPreference mSomatoPause;
	private AuroraSwitchPreference mSomatoMusic;
	

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	addPreferencesFromResource(R.xml.somatosensory);
    	initializeAllPreferences();
    	
    }
   

	private void initializeAllPreferences() {
    	mSmartCall = (AuroraSwitchPreference) findPreference(KEY_SMART_CALL);
    	mSmartAnswer = (AuroraSwitchPreference) findPreference(KEY_SMART_ANSWER);
    	mGesturePhoto = (AuroraSwitchPreference) findPreference(KEY_GESTURE_PHOTO);
    	mGestureAnswer = (AuroraSwitchPreference) findPreference(KEY_GESTURE_ANSWER);
    	mGestureVedio = (AuroraSwitchPreference) findPreference(KEY_GESTURE_VEDIO);
    	mSomatoPause = (AuroraSwitchPreference) findPreference(KEY_SOMATOSENSORY_PAUSE);
    	mSomatoMusic = (AuroraSwitchPreference) findPreference(KEY_SOMATOSENSORY_MUSIC);
    	if(null != mSomatoMusic){
    		mSomatoMusic.setOnPreferenceChangeListener(this);
    		mSomatoMusic.setChecked(readPreferenceClick(FILE_SOMATOSENSORY_MUSIC));
    	}
    	if(null != mSomatoPause){
    		mSomatoPause.setOnPreferenceChangeListener(this);
    	}
    	if(null != mGestureVedio){
    		mGestureVedio.setOnPreferenceChangeListener(this);
    		mGestureVedio.setChecked(Settings.System.getInt(getContentResolver(),GESTURE_VEDIO, 1) == 1 ? true : false);
    	}
    	if(null != mGestureAnswer){
    		mGestureAnswer.setOnPreferenceChangeListener(this);
    		mGestureAnswer.setChecked(Settings.System.getInt(getContentResolver(),GESTURE_ANSWER, 0) == 1 ? true : false);
    	}
    	if(null != mGesturePhoto){
    		mGesturePhoto.setOnPreferenceChangeListener(this);
    		mGesturePhoto.setChecked(Settings.System.getInt(getContentResolver(),GESTURE_PHOTO, 1) == 1 ? true : false);
    	}
    	if(null != mSmartAnswer){
    		mSmartAnswer.setOnPreferenceChangeListener(this);
    		mSmartAnswer.setChecked(Settings.System.getInt(getContentResolver(),SMART_ANSWER, 0) == 1 ? true : false);
    	}
    	if(null != mSmartCall){
    		mSmartCall.setOnPreferenceChangeListener(this);
    		mSmartCall.setChecked(Settings.System.getInt(getContentResolver(),SMART_CALL, 0) == 1 ? true : false);
    	}
    }
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
    	if(mSomatoMusic == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			writePreferenceClick(true,FILE_SOMATOSENSORY_MUSIC);
    		}else {
    			writePreferenceClick(false,FILE_SOMATOSENSORY_MUSIC);
    		}
    		return true;
    		
    	}else if(mSmartCall == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			Settings.System.putInt(getContentResolver(),
    					SMART_CALL, 1);
    		}else {
    			Settings.System.putInt(getContentResolver(),
    					SMART_CALL, 0);
    		}
    		return true;
    	}else if(mSmartAnswer == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			Settings.System.putInt(getContentResolver(),
    					SMART_ANSWER, 1);
    		}else {
    			Settings.System.putInt(getContentResolver(),
    					SMART_ANSWER, 0);
    		}
    		return true;
    	}else if(mGesturePhoto == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_PHOTO, 1);
    		}else {
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_PHOTO, 0);
    		}
    		return true;
    	}else if(mGestureAnswer == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_ANSWER, 1);
    		}else {
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_ANSWER, 0);
    		}
    		return true;
    	}else if(mGestureVedio == preference){
    		final boolean isChecked = (Boolean) newValue;
    		if(isChecked){
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_VEDIO, 1);
    		}else {
    			Settings.System.putInt(getContentResolver(),
    					GESTURE_VEDIO, 0);
    		}
    		return true;
    	}
    	return false;
    }
    public static void writePreferenceClick(boolean isChecked, String filePath) {
    	FileOutputStream out = null;
    	File outFile = null;
    	try {
	    	outFile = new File(filePath);
	    	if(!outFile.exists()){
	    		return;
	    	}
	    	out = new FileOutputStream(outFile);
	    	if(isChecked) {
	    		
	    		out.write("1\n".getBytes());
	    	} else {
	    		out.write("0\n".getBytes());
	    	}
	
	    	if (null != out) {  
				out.flush();  
				out.close();
		    }
	    } catch (Exception e) {
	    
			e.printStackTrace();
		}
    }
    
    public static boolean readPreferenceClick(String filePath) {
    	File file = new File(filePath);
        BufferedReader reader = null;
        String fileString = null;
        boolean isChecked = false;
        String resultString = null;
        try {
            // 一次读一个字节
            reader = new BufferedReader(new FileReader(file));
            
            while ((fileString = reader.readLine()) != null) {
            	resultString = fileString;
            }
            if (null != reader) {  
            	reader.close();
		    }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(resultString==null)
        {
	        return false ;
        }
		if(resultString.length() != 1){
			int index = resultString.indexOf(",");
			if(index == 1) {
				resultString = resultString.substring(0, index);
			}
		}
        if(resultString.equals("1")) {
        	isChecked = true;
        } else {
        	isChecked = false;
        }
        return isChecked;
    }
	

}
