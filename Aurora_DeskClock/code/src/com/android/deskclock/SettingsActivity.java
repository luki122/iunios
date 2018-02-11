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

import com.android.deskclock.R;


import android.R.anim;
//import aurora.app.AuroraActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
//Gionee baorui 2012-12-12 modify for CR00733082 begin
//import android.preference.CheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import android.view.MenuItem;
import android.widget.CompoundButton;
//Gionee baorui 2012-12-12 modify for CR00733082 end
//Gionee baorui 2013-01-11 modify for CR00762851 begin
import aurora.preference.AuroraPreferenceGroup;

import com.aurora.utils.Blur;
//Gionee baorui 2013-01-11 modify for CR00762851 end
//Gionee <baorui><2013-05-06> modify for CR00803588 begin
import com.aurora.utils.GnRingtoneUtil;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//Gionee <baorui><2013-05-06> modify for CR00803588 end
import android.content.SharedPreferences.Editor;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends AuroraPreferenceActivity
        implements AuroraPreference.OnPreferenceChangeListener {

    private static final int ALARM_STREAM_TYPE_BIT =
            1 << AudioManager.STREAM_ALARM;

    private static final String KEY_ALARM_IN_SILENT_MODE =
            "alarm_in_silent_mode";
    static final String KEY_ALARM_SNOOZE =
            "snooze_duration";
    static final String KEY_VOLUME_BEHAVIOR =
            "volume_button_setting";
    static final String KEY_DEFAULT_RINGTONE =
            "default_ringtone";
    static final String KEY_AUTO_SILENCE =
            "auto_silence";
    
    static final String KEY_DONTDISTURB =
            "dontdisturb";
    
    static final String KEY_ALARMVOLUME =
            "alarm_volume";
    
    static final String KEY_DEFAULT_VIBRATE = "default_vibrate";
    
    public static final String DEFAULT_SNOOZE = "10";

    // Gionee baorui 2012-12-12 modify for CR00733082 begin
    private boolean mChange = true;
    // Gionee baorui 2012-12-12 modify for CR00733082 end
    
//    private AlarmPreference ringtone;
    
    private AuroraSwitchPreference mVibratePref;
    
    private AuroraSwitchPreference mDontDisturb;
    
    private AuroraAlarmVolumePreference alarmVolume;
    
    public static final int REQUEST_CODE = 1;
    
    private int mProgress = 0;
    private boolean isvibrateChecked = false;
    private SharedPreferences mSharedPreferences;
    private boolean isUseful = false;
    
    private Bitmap bgBitmap = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		//setTheme(R.style.Theme_AndroidDevelopers);
    	//this.setTheme(com.aurora.R.style.Theme_aurora);
    	this.setTheme(com.aurora.R.style.Theme_Aurora_Dark_Transparent);
        super.onCreate(savedInstanceState);
        
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        addPreferencesFromResource(R.xml.settings);
        setAuroraActionbarSplitLineVisibility(View.GONE);
        
        //bgBitmap = Blur.getBlurBitmapByPath(this, AlarmClock.lockscreenDefaultPath);
        bgBitmap = Blur.getBackgroundPic(this, "Default-Wallpaper.png");
        if (bgBitmap != null && !bgBitmap.isRecycled() ) {
			getWindow().setBackgroundDrawable(new BitmapDrawable(bgBitmap));
		} else {
			getWindow().setBackgroundDrawableResource(R.drawable.background);
		}
        getAuroraActionBar().setBackgroundColor(android.R.color.transparent);
        //getAuroraActionBar().setBackgroundResource(R.drawable.mengban);
        //setParentLayoutBackgroundResource(R.drawable.mengban);
        //aurora add by tangjun 2013.12.23 start
        
        mVibratePref = (AuroraSwitchPreference) findPreference(KEY_DEFAULT_VIBRATE);
        //android.util.Log.d("cjslog", "6");
        mVibratePref.setChecked(AuroraPreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_DEFAULT_VIBRATE, true));
        mVibratePref.setTitleColor(this.getResources().getColorStateList(R.color.repeatcolor1));
        mVibratePref.setSummaryColor(this.getResources().getColorStateList(R.color.repeatcolor2));
        mVibratePref.setOnPreferenceChangeListener(this);
        
        mDontDisturb = (AuroraSwitchPreference) findPreference(KEY_DONTDISTURB);
        mDontDisturb.setOnPreferenceChangeListener(this);
        mDontDisturb.setTitleColor(this.getResources().getColor(R.color.gn_white));
        mDontDisturb.setSummaryColor(this.getResources().getColor(R.color.sometransparent));
        
        alarmVolume = (AuroraAlarmVolumePreference)findPreference(KEY_ALARMVOLUME);
        
        
        Log.e("SettingsActivity---onCreate--");
        mSharedPreferences = this.getSharedPreferences("somesettingstate",Context.MODE_PRIVATE);
        mProgress = mSharedPreferences.getInt("volumeprogress", 0);
        isvibrateChecked = mSharedPreferences.getBoolean("vibratecheck", true);
        isUseful = mSharedPreferences.getBoolean("isuseful", false);
        Log.e("SettingsActivity---mProgress = --" + mProgress);
        Log.e("SettingsActivity---isvibrateChecked = --" + isvibrateChecked);
        
        if ( mDontDisturb.isChecked() ) {
        	Log.e("SettingsActivity---mDontDisturb checked");
    		alarmVolume.setVolumeProgress(0, true);
    		//android.util.Log.d("cjslog", "3");
    		mVibratePref.setChecked(true);
        }
                
        
        //aurora add by tangjun 2013.12.23 end
    }

    @Override
    protected void onResume() {
    	Log.e("SettingsActivity---onResume--");
        super.onResume();
        refresh();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	Editor editor = mSharedPreferences.edit();
        
        editor.putInt("volumeprogress", mProgress);
        editor.putBoolean("vibratecheck", isvibrateChecked);
        editor.putBoolean("isuseful", isUseful);
    	editor.commit();
    }

    @Override
    protected void onDestroy() {
    	alarmVolume.stopRingRes();
    	super.onDestroy();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	Log.e("---onActivityResult--");
    	if ( requestCode == REQUEST_CODE ) {
    		if ( resultCode == RESULT_OK) {
    			Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
//    			ringtone.getSaveRingtone(ringtoneUri);
    		}
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
            // Gionee baorui 2012-12-22 modify for CR00733082 begin
            /*
            CheckBoxPreference pref = (CheckBoxPreference) preference;

            int ringerModeStreamTypes = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

            if (pref.isChecked()) {
                ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
            } else {
                ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                    ringerModeStreamTypes);
            */
            mChange = false;
            // Gionee baorui 2012-12-22 modify for CR00733082 end

            return true;
        } else if ( KEY_DEFAULT_RINGTONE.equals(preference.getKey())) {
        	Intent intent = new Intent("gn.com.android.audioprofile.action.RINGTONE_PICKER");
        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
//        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, ringtone.getRestoreRingtone());
        	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, this.getResources().getString(R.string.default_ringtone_setting_title));
//        	intent.putExtra("deskclock", AlarmClock.mScreenBitmapMatrix);
        	intent.putExtra("lockscreenpath", AlarmClock.lockscreenDefaultPath);
        	intent.putExtra("fullscreen", true);
        	startActivityForResult(intent, REQUEST_CODE);
        	
        	return true;
        }
        
        //aurora add by tangjun 2013.12.23 start
        if (preference == mVibratePref) {
        	//android.util.Log.d("cjslog", "4" + mVibratePref.isChecked());
        	mVibratePref.setChecked(isvibrateChecked);
        }
        //aurora add by tangjun 2013.12.23 end

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(AuroraPreference pref, Object newValue) {
    	//android.util.Log.d("cjslog", "onPreferenceChange ");
    	//android.util.Log.e("jadon", "key = "+pref.getKey()+"     value = "+newValue);
        if (KEY_ALARM_SNOOZE.equals(pref.getKey())) {
            final AuroraListPreference listPref = (AuroraListPreference) pref;
            final int idx = listPref.findIndexOfValue((String) newValue);
            listPref.setSummary(listPref.getEntries()[idx]);
        } else if (KEY_AUTO_SILENCE.equals(pref.getKey())) {
            final AuroraListPreference listPref = (AuroraListPreference) pref;
            String delay = (String) newValue;
            updateAutoSnoozeSummary(listPref, delay);
        }
        // Gionee baorui 2012-12-12 modify for CR00733082 begin
        else if (KEY_ALARM_IN_SILENT_MODE.equals(pref.getKey())) {
        	Log.e("----KEY_ALARM_IN_SILENT_MODE true------");
        	if ( alarmVolume != null ) {
        		try {				
	        	alarmVolume.setVolumeProgress(mProgress, false);
	        	alarmVolume.stopRingTone();
        		} catch (Exception e) {
        			e.printStackTrace();
				}
        	}
            if (mChange == true) {
                AuroraSwitchPreference mPref = (AuroraSwitchPreference) pref;
                int ringerModeStreamTypes = Settings.System.getInt(getContentResolver(),
                        Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

                mPref.setChecked(!mPref.isChecked());
                if (mPref.isChecked()) {
                    ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
                } else {
                    ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
                }

                Settings.System.putInt(getContentResolver(), Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                        ringerModeStreamTypes);
            } else {
                mChange = true;
            }
        } else if ( KEY_DONTDISTURB.equals(pref.getKey())) {
        	dontDisturbOperate(pref);
        } else if ( KEY_DEFAULT_VIBRATE.equals(pref.getKey())) {
        	//android.util.Log.d("cjslog", "onPreferenceChange " + "vibrate");
        	isvibrateChecked = !mVibratePref.isChecked();
        }
        // Gionee baorui 2012-12-12 modify for CR00733082 end
        return true;
    }
    
    /**
     * @param pref        aurora add by tangjun 2014.2.13
     */
    private void dontDisturbOperate( AuroraPreference pref ) {
    	AuroraSwitchPreference mPref = (AuroraSwitchPreference) pref;
    	alarmVolume.setTitleEnable( !mPref.isChecked() );
    	
    	Log.e("-------dontDisturbOperate----mPref.isChecked() = ----" + mPref.isChecked());
    	if ( !mPref.isChecked() ) {
    		mProgress = alarmVolume.getVolumeProgress();
    		Log.e("-------111dontDisturbOperate----mProgress = ----" + mProgress);
    		alarmVolume.setVolumeProgress(0, true);
    		
    		isvibrateChecked = mVibratePref.isChecked();
    		//android.util.Log.d("cjslog", "1");
    		mVibratePref.setChecked(true);
    	} else {
    		Log.e("-------222dontDisturbOperate----mProgress = ----" + mProgress);
    		alarmVolume.setVolumeProgress(mProgress, true);
    		//android.util.Log.d("cjslog", "2");
    		mVibratePref.setChecked(isvibrateChecked);
    	}
    	
    	alarmVolume.stopRingTone();
    }

    private void updateAutoSnoozeSummary(AuroraListPreference listPref,
            String delay) {
        int i = Integer.parseInt(delay);
        if (i == -1) {
            listPref.setSummary(R.string.auto_silence_never);
        } else {
            listPref.setSummary(getString(R.string.auto_silence_summary, i));
        }
    }


    private void refresh() {
        // Gionee baorui 2012-12-12 modify for CR00733082 begin
        /*
        final CheckBoxPreference alarmInSilentModePref =
                (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        */
        final AuroraSwitchPreference alarmInSilentModePref = (AuroraSwitchPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        
        alarmInSilentModePref.setTitleColor(this.getResources().getColorStateList(R.color.repeatcolor1));
        alarmInSilentModePref.setSummaryColor(this.getResources().getColorStateList(R.color.repeatcolor2));
        
        alarmInSilentModePref.setOnPreferenceChangeListener(this);
        // Gionee baorui 2012-12-12 modify for CR00733082 end
        final int silentModeStreams =
                Settings.System.getInt(getContentResolver(),
                        Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        alarmInSilentModePref.setChecked(
                (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);
        
        //aurora mod by tangjun 2013.12.23 start  按策划要求删除小睡时间设置 --------------------
        AuroraPreferenceGroup mPrefGroup = ((AuroraPreferenceGroup) findPreference("favorite"));

        AuroraListPreference listPref = (AuroraListPreference) findPreference(KEY_ALARM_SNOOZE);
        //listPref.setSummary(listPref.getEntry());
        //listPref.setOnPreferenceChangeListener(this);
        
        if ( listPref != null ) {
        	 mPrefGroup.removePreference(listPref);
        }
        
        //aurora mod by tangjun 2013.12.23 end------------------------------

        listPref = (AuroraListPreference) findPreference(KEY_AUTO_SILENCE);
        // Gionee baorui 2012-11-29 modify for CR00733082 begin
        // String delay = listPref.getValue();
        // updateAutoSnoozeSummary(listPref, delay);
        // listPref.setOnPreferenceChangeListener(this);

        if (listPref != null) {
            String delay = listPref.getValue();
            updateAutoSnoozeSummary(listPref, delay);
            listPref.setOnPreferenceChangeListener(this);

            // getPreferenceScreen().removePreference(listPref);
            mPrefGroup.removePreference(listPref);

             
        }
        listPref = (AuroraListPreference) findPreference(KEY_VOLUME_BEHAVIOR);
        if (listPref != null) {

            // getPreferenceScreen().removePreference(listPref);
            mPrefGroup.removePreference(listPref);

        }

//        ringtone = (AlarmPreference) findPreference(KEY_DEFAULT_RINGTONE);
//        ringtone.setTitleColor(this.getResources().getColorStateList(R.color.repeatcolor1));
//        ringtone.setSummaryColor(this.getResources().getColorStateList(R.color.repeatcolor2));

        Uri alert = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        if (alert != null && GnRingtoneUtil.isRingtoneExist(alert, getContentResolver())) {
            SharedPreferences prefs = getSharedPreferences("SettingsActivity", AuroraActivity.MODE_PRIVATE);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString("alert", alert.toString());
            ed.putString("_data", Alarms.getExternalUriData(this, alert));
            ed.putInt("volumes", Alarms.getVolumes(this));
            ed.apply();
        }

        /*
        if (alert != null) {
            ringtone.setAlert(alert);
        }
        */
//        ringtone.setAlert(alert);
//        ringtone.setChangeDefault();
        // Gionee baorui 2012-12-12 for CR00738567 end
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//    	if(){
//    		
//    	}
    	return super.onTouchEvent(event);
    }

    // Gionee baorui 2012-12-27 modify for CR00754579 begin
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    // Gionee baorui 2012-12-27 modify for CR00754579 end
}
