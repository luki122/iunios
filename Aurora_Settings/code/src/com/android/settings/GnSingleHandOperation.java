package com.android.settings;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;

/**
 * 
 * @author chenml
 * @date 2013-05-30
 *
 */
public class GnSingleHandOperation extends AuroraPreferenceActivity  implements
       AuroraPreference.OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "GnSingleHandOperation";
   
    private static final String PHONE_KEYBOARD = "phone_keyboard";
    private static final String INPUT_METHOD_KEYBOARD = "input_method_keyboard";
    private static final String PATTERN_UNLOCKSCREEN = "pattern_unlockscreen";
    private static final String SMALL_SCREEN_MODE = "small_screen_mode";
    private static final String SCREEN_SIZE = "screen_size";
    private static final String ADV_SETTING = "advance_setting";

    private static final String GN_PHONE_KEYBOARD = "gn_phone_keyboard";
    private static final String GN_INPUT_METHOD_KEYBOARD = "gn_input_method_keyboard";
    private static final String GN_PATTERN_UNLOCKSCREEN = "gn_pattern_unlockscreen";
    private static final String GN_SMALL_SCREEN_MODE = "gn_small_screen_mode";
    private static final String GN_SCREEN_SIZE = "gn_screen_size";

    
    private AuroraSwitchPreference mPhoneKeyboard;
    private AuroraSwitchPreference mInputMethodKeyboard;
    private AuroraSwitchPreference mPatternUnlockscreen;
    private AuroraSwitchPreference mSmallScreenMode;
    private AuroraListPreference mScreenSize;
    private AuroraPreferenceCategory mADV_Setting ;
    
    private ContentObserver mPhoneKbObserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updatePhoneKeyboardSwitch();
        }
    };
    private ContentObserver mInputMethodKbObserver = new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateInputMethodKeyboardSwitch();
        }
    };
    private ContentObserver mPatternUnLSObserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updatePatternUnlockscreenSwitch();
        }
    };
    private ContentObserver mSmallScreenModeObserver= new ContentObserver( new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            updateSmallScreenModeSwitch();
        }
    };
    private ContentObserver mScreenSizeObserver = new ContentObserver(new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            int value=Settings.System.getInt(
                    getContentResolver(), GN_SCREEN_SIZE, 0);
            mScreenSize.setValue(String.valueOf(value));
            updateScreensizePreferenceDescription(value);
            AuroraAlertDialog dlg = (AuroraAlertDialog)mScreenSize.getDialog();
            if (dlg == null || !dlg.isShowing()) {
                return;
            }
            ListView listview = dlg.getListView();
            int checkedItem = mScreenSize.findIndexOfValue(
                    mScreenSize.getValue());
            if(checkedItem > -1) {
                listview.setItemChecked(checkedItem, true);
                listview.setSelection(checkedItem);
            }
        }
  
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        
        addPreferencesFromResource(R.xml.gn_single_hand_operation);
   
        if (GnSettingsUtils.sGnSettingSupport) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
	getAuroraActionBar().setTitle(R.string.gn_single_hand_operation);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        mPhoneKeyboard = (AuroraSwitchPreference)findPreference(PHONE_KEYBOARD);
        mInputMethodKeyboard = (AuroraSwitchPreference)findPreference(INPUT_METHOD_KEYBOARD);
        mPatternUnlockscreen = (AuroraSwitchPreference)findPreference(PATTERN_UNLOCKSCREEN);
        mSmallScreenMode = (AuroraSwitchPreference)findPreference(SMALL_SCREEN_MODE);
        mScreenSize = (AuroraListPreference) findPreference(SCREEN_SIZE);
        mADV_Setting = (AuroraPreferenceCategory) findPreference(ADV_SETTING);
        
        mPhoneKeyboard.setOnPreferenceChangeListener(this);
        mInputMethodKeyboard.setOnPreferenceChangeListener(this);
        mPatternUnlockscreen.setOnPreferenceChangeListener(this);
        mSmallScreenMode.setOnPreferenceChangeListener(this);
        
        final int currentScreensize = getScreensizeValue();
        mScreenSize.setValue(String.valueOf(currentScreensize));
        mScreenSize.setOnPreferenceChangeListener(this);
        updateScreensizePreferenceDescription(currentScreensize);
        
        if(mADV_Setting != null){
            getPreferenceScreen().removePreference(mADV_Setting);
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
        getContentResolver().registerContentObserver(Settings.System.getUriFor(GN_PHONE_KEYBOARD), 
                true, mPhoneKbObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(GN_INPUT_METHOD_KEYBOARD), 
                true, mInputMethodKbObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(GN_PATTERN_UNLOCKSCREEN), 
                true, mPatternUnLSObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(GN_SMALL_SCREEN_MODE), 
                true, mSmallScreenModeObserver);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(GN_SCREEN_SIZE),
                false, mScreenSizeObserver);  
        
        final int currentScreensize = getScreensizeValue();
        updateScreensizePreference(currentScreensize); 
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getContentResolver().unregisterContentObserver(mPhoneKbObserver);
        getContentResolver().unregisterContentObserver(mInputMethodKbObserver);
        getContentResolver().unregisterContentObserver(mPatternUnLSObserver);
        getContentResolver().unregisterContentObserver(mSmallScreenModeObserver);
        getContentResolver().unregisterContentObserver(mScreenSizeObserver);  
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        final String key = preference.getKey();
        
          if (PHONE_KEYBOARD.equals(key)) {
            try {
                boolean state = (Boolean) objValue;
                Settings.System.putInt(getContentResolver(),
                        GN_PHONE_KEYBOARD, state ? 1 : 0);
           } catch (Exception e) {
               // TODO: handle exception
               Log.d(TAG, "PHONE_KEYBOARD e: " + e);
           }
        }else if (INPUT_METHOD_KEYBOARD.equals(key)){
            try {
                boolean state = (Boolean) objValue; 
                Settings.System.putInt(getContentResolver(),
                        GN_INPUT_METHOD_KEYBOARD, state ? 1 : 0);
           } catch (Exception e) {
               // TODO: handle exception
               Log.d(TAG, "INPUT_METHOD e: " + e);
           }
        }else if(PATTERN_UNLOCKSCREEN.equals(key)){
            try {
                boolean state = (Boolean) objValue; 
                Settings.System.putInt(getContentResolver(),
                        GN_PATTERN_UNLOCKSCREEN, state ? 1 : 0);
           } catch (Exception e) {
               // TODO: handle exception
               Log.d(TAG, " PATTERN_UNLOCKSCREEN e: " + e);
           }
        }else if(SMALL_SCREEN_MODE.equals(key)){
            try {
                boolean state = (Boolean) objValue; 
                Settings.System.putInt(getContentResolver(), GN_SMALL_SCREEN_MODE,state ? 1: 0);
           } catch (Exception e) {
               // TODO: handle exception
               Log.d(TAG, " SMALL_SCREEN_MODE e: " + e);
           }
        }else if (SCREEN_SIZE.equals(preference.getKey())) {
            int value = Integer.parseInt((String) objValue);
            Log.d(TAG, " SCREEN_SIZE value : " + value);
            try {
                Settings.System.putInt(getContentResolver(), GN_SCREEN_SIZE, value);
                updateScreensizePreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen size setting", e);
            }
       }
        return true;
    }
    
    private int getScreensizeValue() {
        int currentValue = Settings.System.getInt(getContentResolver(), GN_SCREEN_SIZE,0);
        int bestMatch = 0;
        int screensize = 0;
        final CharSequence[] valuesScreensize = mScreenSize.getEntryValues();
        for (int i = 0; i < valuesScreensize.length; i++) {
            screensize = Integer.parseInt(valuesScreensize[i].toString());
            if (currentValue == screensize) {
                return currentValue;
            } else {
                if (currentValue > screensize) {
                    bestMatch = i;
                }
            }
        }
        return Integer.parseInt(valuesScreensize[bestMatch].toString());

    }
    private void updateScreensizePreference(int currentScreensize) {
        mScreenSize.setValue(String.valueOf(currentScreensize));
        updateScreensizePreferenceDescription(currentScreensize);    
        AuroraAlertDialog dlg = (AuroraAlertDialog)mScreenSize.getDialog();
        if (dlg == null || !dlg.isShowing()) {
            return;
        }
        ListView listview = dlg.getListView();
        int checkedItem = mScreenSize.findIndexOfValue(mScreenSize.getValue());
        if (checkedItem > -1) {
            listview.setItemChecked(checkedItem, true);
            listview.setSelection(checkedItem);
        }
    }
    private void updateScreensizePreferenceDescription(int currentScreensize) {
        AuroraListPreference preference = mScreenSize;
        String summary;
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
            int best = 0;
            for (int i = 0; i < values.length; i++) {
                int screensize = Integer.parseInt(values[i].toString());
                if (currentScreensize == screensize) {
                    best = i;
                    break;
                }
            }
            if (entries.length != 0) {
                summary = preference.getContext().getString(R.string.gn_screen_size_summary,entries[best]);
            } else {
                summary = "";
            }

         }
        preference.setSummary(summary);
    }
    private void updateState(){
        updatePhoneKeyboardSwitch();
        updateInputMethodKeyboardSwitch();
        updatePatternUnlockscreenSwitch();
        updateSmallScreenModeSwitch();
    }
    
   private void updatePhoneKeyboardSwitch(){
       if(mPhoneKeyboard !=null){
           mPhoneKeyboard.setChecked(Settings.System.getInt(getContentResolver(),
                   GN_PHONE_KEYBOARD,0) == 1); 
       }
   }
   private void updateInputMethodKeyboardSwitch(){
       if(mInputMethodKeyboard !=null){
           mInputMethodKeyboard.setChecked(Settings.System.getInt(getContentResolver(),
                   GN_INPUT_METHOD_KEYBOARD,0) == 1); 
       }
   }
   private void updatePatternUnlockscreenSwitch(){
       if(mPatternUnlockscreen !=null){
           mPatternUnlockscreen.setChecked(Settings.System.getInt(getContentResolver(),
                   GN_PATTERN_UNLOCKSCREEN,0) == 1); 
       }
   }
   private void updateSmallScreenModeSwitch(){
       if(mSmallScreenMode !=null){
           mSmallScreenMode.setChecked(Settings.System.getInt(getContentResolver(),
                   GN_SMALL_SCREEN_MODE,0) == 1); 
       }
       if(Settings.System.getInt(getContentResolver(),
               GN_SMALL_SCREEN_MODE,0) == 1){
           mScreenSize.setEnabled(true);
       }else{
           mScreenSize.setEnabled(false);
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
    public boolean onPreferenceClick(AuroraPreference arg0) {
        // TODO Auto-generated method stub
        return false;
    }
}
