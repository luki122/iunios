package com.android.settings;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceGroup;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;
//import android.provider.Settings;
//import android.provider.Settings.System;
import aurora.provider.AuroraSettings;
/**
 * 
 * @author chenminli
 * @date 2013-05-20
 *
 */

public class GnSoundControlProfile extends AuroraPreferenceActivity {

    private static final String TAG="GnSoundControlProfile";
    
    private static final String SOUND_CONTROL="sound_control";
    private static final String CALLING_KEY="sound_control_calling";
    private static final String MASSAGE_KEY = "sound_control_message";
    private static final String LOCKSCREEN_KEY = "sound_control_lockscreen";
    private static final String ALARMCLOCK_KEY= "sound_control_alarmclock";  
    
    private static String DIALOG_ALERT_STATE = "dialog_alert_state";
    private static SharedPreferences mSharedPreferences;
    
    private static String DIALOG_SHOW_STATE = "dialog_show_state";
//    private static SharedPreferences mSharedPreferences1;
    
    private AuroraPreferenceCategory mSoundControl;
    private AuroraCheckBoxPreference mCallingPref;
    private AuroraCheckBoxPreference mMessagePref;
    private AuroraCheckBoxPreference mLockScreenPref;
    private AuroraCheckBoxPreference mAlarmClockPref;
    
    private Context mContext;
    private AuroraSwitch mSwitch;
//    private boolean mIsChecked;
    private CheckBox mCheckBox;
    private Editor mEditor ;
//    private Editor mEditor1 ;
    
    private String mPackageName="com.android.settings";
//    private String mMessage ;
    private String mItem;
    private static final String STOP_BROADCAST="stop_broadcast";
    
    private static final int  DEFAULT_SOUND_CONTROL_CALLING_STATE = 0;
    private static final int  DEFAULT_SOUND_CONTROL_MASSAGE_STATE = 0;
    private static final int  DEFAULT_SOUND_CONTROL_LOCKSCREEN_STATE = 0;
    private static final int  DEFAULT_SOUND_CONTROL_ALARMCLOCK_STATE = 0;
    
    private ContentObserver mSwitchobserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateSwitch();
        }
    };

    private ContentObserver mCallingobserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateCallingCheckBox();
        }
    };
    private ContentObserver mMessageobserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateMessageCheckBox();
        }
    };
    private ContentObserver mLockscreenobserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateLockScreenCheckBox();
        }
    };
    private ContentObserver mAlarmClockobserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateAlarmClockCheckBox();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        
        if (GnSettingsUtils.sGnSettingSupport) {
            if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                    GnSettingsUtils.TYPE_LIGHT_THEME)) {
                setTheme(R.style.GnSettingsLightTheme);
            } else {
//                setTheme(R.style.GnSettingsDarkTheme);
                setTheme(R.style.GnSettingsLightTheme);
            }
        }
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.gn_sound_control_profile);
        
        mSoundControl = (AuroraPreferenceCategory)findPreference(SOUND_CONTROL);
        mCallingPref = (AuroraCheckBoxPreference) findPreference(CALLING_KEY);
        mMessagePref = (AuroraCheckBoxPreference) findPreference(MASSAGE_KEY);
        mLockScreenPref = (AuroraCheckBoxPreference) findPreference(LOCKSCREEN_KEY);
        mAlarmClockPref = (AuroraCheckBoxPreference) findPreference(ALARMCLOCK_KEY);
        
        mContext=GnSoundControlProfile.this;
        mEditor = getEditor();
//        mEditor1 = getEditor();
//        mSharedPreferences1 = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        mSwitch = new AuroraSwitch(mContext);
        updateSwitch();
        if(mSwitch !=null){
            mSwitch.setOnCheckedChangeListener( new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // TODO Auto-generated method stub
                    if (isChecked && !mSharedPreferences.getBoolean(DIALOG_ALERT_STATE, false)
                                 &&(mSharedPreferences.getBoolean(DIALOG_SHOW_STATE, true))) {
                        showDialog();
                        mEditor.putBoolean(DIALOG_SHOW_STATE, false);
                        mEditor.commit();
                        }
                    if(!isChecked) {
                        mEditor.putBoolean(DIALOG_SHOW_STATE, true);
                        mEditor.commit();
                        sendBroadcastToStopTTS(STOP_BROADCAST);
                    }
                    try {
                      AuroraSettings.putInt(getContentResolver(), AuroraSettings.SOUND_CONTROL_SWITCH,
                              mSwitch.isChecked() ? 1 : 0);
                  } catch (Exception e) {
                      // TODO: handle exception
                      Log.e(TAG, "can not write the value of mSwitch in datebase");
                  }
                    setSoundControlEnabled(isChecked);
                }
            });
        }
        setSoundControlEnabled(AuroraSettings.getInt(getContentResolver(),
                AuroraSettings.SOUND_CONTROL_SWITCH,0) == 1);        
        
        final int padding = mContext.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mSwitch.setPadding(0, 0, padding, 0);
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
        getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                AuroraActionBar.DISPLAY_SHOW_CUSTOM);
        getAuroraActionBar().setCustomView(mSwitch, new AuroraActionBar.LayoutParams(
                AuroraActionBar.LayoutParams.WRAP_CONTENT,
                AuroraActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
        */
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        if (GnSettingsUtils.sGnSettingSupport) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.gn_sound_control_title);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        if(mAlarmClockPref != null) {
//            getPreferenceScreen().removePreference(mAlarmClockPref);
            ((AuroraPreferenceGroup) findPreference(SOUND_CONTROL)).removePreference(mAlarmClockPref);
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        setSoundControlEnabled(mSwitch.isChecked());
        updateState();
        getContentResolver().registerContentObserver(AuroraSettings.getUriFor(AuroraSettings.SOUND_CONTROL_SWITCH), 
                true, mSwitchobserver);
        getContentResolver().registerContentObserver(AuroraSettings.getUriFor(AuroraSettings.SOUND_CONTROL_CALLING), 
                true, mCallingobserver);
        getContentResolver().registerContentObserver(AuroraSettings.getUriFor(AuroraSettings.SOUND_CONTROL_MESSAGE), 
                true, mMessageobserver);
        getContentResolver().registerContentObserver(AuroraSettings.getUriFor(AuroraSettings.SOUND_CONTROL_LOCKSCREEN), 
                true, mLockscreenobserver);
        getContentResolver().registerContentObserver(AuroraSettings.getUriFor(AuroraSettings.SOUND_CONTROL_ALARMCLOCK), 
                true, mAlarmClockobserver);
        updateState();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mSwitchobserver);
        getContentResolver().unregisterContentObserver(mCallingobserver);
        getContentResolver().unregisterContentObserver(mMessageobserver);
        getContentResolver().unregisterContentObserver(mLockscreenobserver);
        getContentResolver().unregisterContentObserver(mAlarmClockobserver);
        sendBroadcastToStopTTS(STOP_BROADCAST);
        }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        // TODO Auto-generated method stub
        
        if(preference == mCallingPref){
            try {
                boolean state = mCallingPref.isChecked(); 
                AuroraSettings.putInt(getContentResolver(),
                        AuroraSettings.SOUND_CONTROL_CALLING, state ? 1 : 0);
                if(state){
//                    mMessage = getResources().getString(R.string.SOUND_CONTROL_calling_summary);
                    mItem = CALLING_KEY;
                    startServiceToStartTTS(mPackageName,mItem);
                }else{
                    sendBroadcastToStopTTS(mItem);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.fillInStackTrace();
                Log.e(TAG, "can not write the value of mCallingPref in datebase");
            }
        }else if(preference == mMessagePref){
            try {
                boolean state = mMessagePref.isChecked(); 
                if(state){
//                    mMessage = getResources().getString(R.string.SOUND_CONTROL_message_summary);
                    mItem = MASSAGE_KEY;
                    startServiceToStartTTS(mPackageName,mItem);
                }else{
                    sendBroadcastToStopTTS(mItem);
                }
                AuroraSettings.putInt(getContentResolver(),
                        AuroraSettings.SOUND_CONTROL_MESSAGE, state ? 1 : 0);
            } catch (Exception e) {
                // TODO: handle exception
                e.fillInStackTrace();
                Log.e(TAG, "can not write the value of mMessagePref in datebase");
            }
        }else if (preference == mLockScreenPref) {
            
            try {
                boolean state = mLockScreenPref.isChecked(); 
                if(state){
//                    mMessage = getResources().getString(R.string.SOUND_CONTROL_lockscreen_summary);
                    mItem = LOCKSCREEN_KEY;
                    startServiceToStartTTS(mPackageName,mItem);
                }else{
                    sendBroadcastToStopTTS(mItem);
                }
                AuroraSettings.putInt(getContentResolver(),
                        AuroraSettings.SOUND_CONTROL_LOCKSCREEN, state ? 1 : 0);
            } catch (Exception e) {
                // TODO: handle exception
                e.fillInStackTrace();
                Log.e(TAG, "can not write the value of mLockScreenPref in datebase");
            }
        }else if (preference == mAlarmClockPref) {
            try {
                boolean state = mAlarmClockPref.isChecked(); 
                if(state){
//                    mMessage = getResources().getString(R.string.SOUND_CONTROL_alarmclock_summary);
                    mItem = ALARMCLOCK_KEY;
                    startServiceToStartTTS(mPackageName,mItem);
                }else{
                    sendBroadcastToStopTTS(mItem);
                }
                AuroraSettings.putInt(getContentResolver(),
                        AuroraSettings.SOUND_CONTROL_ALARMCLOCK, state ? 1 : 0);
            } catch (Exception e) {
                // TODO: handle exception
                e.fillInStackTrace();
                Log.e(TAG, "can not write the value of mAlarmClockPref in datebase");
            }
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void updateState() {
        updateSwitch();
        updateCallingCheckBox();
        updateMessageCheckBox();
        updateLockScreenCheckBox();
        updateAlarmClockCheckBox();
    }
    
    private void updateSwitch(){
        if(mSwitch !=null){
            mSwitch.setChecked(AuroraSettings.getInt(getContentResolver(),
                    AuroraSettings.SOUND_CONTROL_SWITCH,0) == 1); 
        }
    }
    
    private void updateCallingCheckBox(){
       if(mCallingPref !=null){
           mCallingPref.setChecked(AuroraSettings.getInt(getContentResolver(),
                  AuroraSettings.SOUND_CONTROL_CALLING,DEFAULT_SOUND_CONTROL_CALLING_STATE) == 1); 
       }
    }
    private void updateMessageCheckBox(){
        if(mMessagePref !=null){
            mMessagePref.setChecked(AuroraSettings.getInt(getContentResolver(),
                    AuroraSettings.SOUND_CONTROL_MESSAGE, DEFAULT_SOUND_CONTROL_MASSAGE_STATE) == 1); 
       }
    }
    private void updateLockScreenCheckBox(){
        if(mLockScreenPref !=null){
            mLockScreenPref.setChecked(AuroraSettings.getInt(getContentResolver(),
                    AuroraSettings.SOUND_CONTROL_LOCKSCREEN, DEFAULT_SOUND_CONTROL_LOCKSCREEN_STATE) == 1);  
        }
    }
    private void updateAlarmClockCheckBox(){
        if(mAlarmClockPref !=null){
            mAlarmClockPref.setChecked(AuroraSettings.getInt(getContentResolver(),
                    AuroraSettings.SOUND_CONTROL_ALARMCLOCK, DEFAULT_SOUND_CONTROL_ALARMCLOCK_STATE) == 1);
        }
    }
    
    private void showDialog(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialogview, null);
        mCheckBox = (CheckBox)view.findViewById(R.id.check);

        AuroraAlertDialog.Builder mDialogBuilder = new AuroraAlertDialog.Builder(mContext/*,AuroraAlertDialog.THEME_GIONEEVIEW_FULLSCREEN*/);
        mDialogBuilder.setTitle(R.string.gn_sound_control_dialog)
              .setView(view)
              .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
               @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                   setSoundControlEnabled(true);
                   mEditor.putBoolean(DIALOG_ALERT_STATE, mCheckBox.isChecked());
                   mEditor.commit();
                   dialog.dismiss();
                }
             })
              .setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    setSoundControlEnabled(false);
                    mSwitch.setChecked(false);
                    try {
                        AuroraSettings.putInt(getContentResolver(), AuroraSettings.SOUND_CONTROL_SWITCH,0);
                    } catch (Exception e) {
                        // TODO: handle exception
                        Log.e(TAG, "can not write the value of mSwitch in datebase");
                    }
                    dialog.dismiss();
                }
            });
        
       AuroraAlertDialog dialog = mDialogBuilder.create();
       dialog.setCanceledOnTouchOutside(false);
       dialog.setCancelable(false);
       dialog.show();
    }
    
    
    private void startServiceToStartTTS(String packageName,String item) {
        Intent intent = new Intent("gn.voice.service.TTSService");
        intent.putExtra("type", "speak_info");
        intent.putExtra("appid", packageName);
//        intent.putExtra("message",message);
        intent.putExtra("setting_item", item);
        intent.putExtra("operation", "read_setting_info");
        startService(intent);
    }
    
    private void sendBroadcastToStopTTS(String item) {
        Intent intent = new Intent("gn.voice.broadcast.TTSService.stop");
        intent.putExtra("type", "speak_info");
        intent.putExtra("appid", mPackageName);
        intent.putExtra("setting_item", item);
        intent.putExtra("operation", "read_stting_info");
        sendBroadcast(intent);
    }
    
    private void setSoundControlEnabled(boolean enable) {
           mSoundControl.setEnabled(enable);
    }  
    
//    public boolean isShowDialogAgainWriteValue( boolean state) {
//        mEditor.putBoolean(DIALOG_ALERT_STATE, state);
//        return mEditor.commit();
//    }
//    
//    public boolean isShowDialogAgainReadValue( boolean state) {
//        mSharedPreferences.getBoolean(DIALOG_ALERT_STATE, state);
//        return mEditor.commit();
//    }
    
    private Editor getEditor() {
        SharedPreferences pref = getSharedPreferences();
        return pref.edit();
    }
    
    private SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null)
            mSharedPreferences = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        return mSharedPreferences;
    }
    
}
