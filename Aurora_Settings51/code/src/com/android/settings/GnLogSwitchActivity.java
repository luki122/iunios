//Gionee <wangguojing> <2013-08-14> add for CR00854573 begin
package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import android.provider.Settings;
import android.view.MenuItem;
import android.os.SystemProperties;
import android.util.Log;

public class GnLogSwitchActivity extends AuroraPreferenceActivity 
	implements AuroraPreference.OnPreferenceChangeListener{

    private static final String GN_ENABLE_LOG_PROPERTY = "persist.sys.gionee.debug";

    private static final String KEY_AURORA_LOG_SWITCH = "aurora_log_onoff";
    private static final String KEY_AURORA_DUMP_SWITCH = "aurora_dump _onoff";

    private static final String KEY_MASTER_SWITCH = "gn_master_switch";
    private static final String KEY_VERBOSE_SWITCH = "gn_verbose_switch";
    private static final String KEY_DEBUG_SWITCH = "gn_debug_switch";
    private static final String KEY_INFO_SWITCH = "gn_info_switch";
	
    private static final String KEY_WARN_SWITCH = "gn_warn_switch";
    private static final String KEY_ERROR_SWITCH = "gn_error_switch";

	
    private static final int MASTER_POS = 0;
    private static final int VERBOSE_POS = 1;
    private static final int DEBUG_POS = 2;
    private static final int INFO_POS = 3;
    private static final int WARN_POS = 4;
    private static final int ERROR_POS = 5;

    private AuroraCheckBoxPreference mAuroraLogSwitch;
    private AuroraCheckBoxPreference mAuroraDumpSwitch;

    private AuroraCheckBoxPreference mMasterSwitch;
    private AuroraCheckBoxPreference mVerboseSwitch;
    private AuroraCheckBoxPreference mDebugSwitch;
    private AuroraCheckBoxPreference mInfoSwitch;	
    private AuroraCheckBoxPreference mWarnSwitch;
    private AuroraCheckBoxPreference mErrorSwitch;
    String[] mSwitchValues;
    private boolean mIsChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        //AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.gn_enable_debug_title);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        addPreferencesFromResource(R.xml.gn_log_switch_prefs);

        mAuroraLogSwitch = (AuroraCheckBoxPreference) findPreference(KEY_AURORA_LOG_SWITCH);
        mAuroraLogSwitch.setOnPreferenceChangeListener(this);
        
        // qy add 2014 05 26 begin
        mAuroraDumpSwitch = (AuroraCheckBoxPreference) findPreference(KEY_AURORA_DUMP_SWITCH);
        mAuroraDumpSwitch.setOnPreferenceChangeListener(this);
        Log.i("qy",  "persist.sys.gn.dumplog = " +SystemProperties.get("persist.sys.gn.dumplog"));
        mAuroraDumpSwitch.setChecked(SystemProperties.get("persist.sys.gn.dumplog").equals("1")
        		? true :false);      
        
        // qy add end

        mMasterSwitch= (AuroraCheckBoxPreference) findPreference(KEY_MASTER_SWITCH);
        mMasterSwitch.setOnPreferenceChangeListener(this);
		
        mVerboseSwitch = (AuroraCheckBoxPreference) findPreference(KEY_VERBOSE_SWITCH);
        mVerboseSwitch.setOnPreferenceChangeListener(this);
		
        mDebugSwitch= (AuroraCheckBoxPreference) findPreference(KEY_DEBUG_SWITCH);
        mDebugSwitch.setOnPreferenceChangeListener(this);
		
        mInfoSwitch= (AuroraCheckBoxPreference) findPreference(KEY_INFO_SWITCH);
        mInfoSwitch.setOnPreferenceChangeListener(this);
		
        mWarnSwitch= (AuroraCheckBoxPreference) findPreference(KEY_WARN_SWITCH);
        mWarnSwitch.setOnPreferenceChangeListener(this);
		
        mErrorSwitch= (AuroraCheckBoxPreference) findPreference(KEY_ERROR_SWITCH);
        mErrorSwitch.setOnPreferenceChangeListener(this);

        if (SystemProperties.get("persist.sys.aurora.debug").equals("yes")) {
            mIsChecked = true;
        } else {
            mIsChecked =false;
        }
        mAuroraLogSwitch.setChecked(mIsChecked);
		
        String values = SystemProperties.get(GN_ENABLE_LOG_PROPERTY);
        if(values == null ||values.length() == 0){
            values = "111111";
        }
        int valueLength = values.length();
        mSwitchValues = new String[valueLength];
        for (int i = 0; i < valueLength; i++) {
            mSwitchValues[i] = values.substring(i,i+1);
        }
        if(MASTER_POS < valueLength){
            mMasterSwitch.setChecked((mSwitchValues[MASTER_POS].equals("1")) ? true:false);
        }
        if(VERBOSE_POS < valueLength){
            mVerboseSwitch.setChecked((mSwitchValues[VERBOSE_POS].equals("1")) ? true:false);
        }
        if(DEBUG_POS < valueLength){
            mDebugSwitch.setChecked((mSwitchValues[DEBUG_POS].equals("1")) ? true:false);
        }
        if(INFO_POS < valueLength){
            mInfoSwitch.setChecked((mSwitchValues[INFO_POS].equals("1")) ? true:false);
        }
        if(WARN_POS < valueLength){
            mWarnSwitch.setChecked((mSwitchValues[WARN_POS].equals("1")) ? true:false);
        }
        if(ERROR_POS < valueLength){
            mErrorSwitch.setChecked((mSwitchValues[ERROR_POS].equals("1")) ? true:false);
        }
		
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        final String key = preference.getKey();	 
        final boolean desiredState = (Boolean) objValue;
        if(key.equals(KEY_MASTER_SWITCH)){
//            SystemProperties.set("persist.sys.aurora.debug", desiredState ? "yes" : "no");

            mVerboseSwitch.setChecked(desiredState);
            mDebugSwitch.setChecked(desiredState);
            mInfoSwitch.setChecked(desiredState);
            mWarnSwitch.setChecked(desiredState);
            mErrorSwitch.setChecked(desiredState);
            String str = "";
            for (int i = 0; i < mSwitchValues.length; i++) {
                mSwitchValues[i] = desiredState ? "1" : "0";
                str= str+(desiredState ? "1" : "0");
            }
	     writeEnableLogValues(str);
	     return true;
	 }else if(key.equals(KEY_VERBOSE_SWITCH)){
            mVerboseSwitch.setChecked(desiredState);
            mSwitchValues[VERBOSE_POS] = desiredState ? "1" : "0";
	 }else if(key.equals(KEY_DEBUG_SWITCH)){
            mDebugSwitch.setChecked(desiredState);
            mSwitchValues[DEBUG_POS] = desiredState ? "1" : "0";
	 }else if(key.equals(KEY_INFO_SWITCH)){
            mInfoSwitch.setChecked(desiredState);
            mSwitchValues[INFO_POS] = desiredState ? "1" : "0";
	 }else if(key.equals(KEY_WARN_SWITCH)){
            mWarnSwitch.setChecked(desiredState);
            mSwitchValues[WARN_POS] = desiredState ? "1" : "0";
	 }else if(key.equals(KEY_ERROR_SWITCH)){
            mErrorSwitch.setChecked(desiredState);
            mSwitchValues[ERROR_POS] = desiredState ? "1" : "0";
	 } else if (key.equals(KEY_AURORA_LOG_SWITCH)) {
        mAuroraLogSwitch.setChecked(desiredState);
        SystemProperties.set("persist.sys.aurora.debug", desiredState ? "yes" : "no");
        
     } else if(key.equals(KEY_AURORA_DUMP_SWITCH)){
    	 mAuroraDumpSwitch.setChecked(desiredState);
    	 SystemProperties.set("persist.sys.gn.dumplog", desiredState ? "1" : "0");
     }
	 
	 if(key.equals(KEY_VERBOSE_SWITCH) ||key.equals(KEY_DEBUG_SWITCH)
	 	||key.equals(KEY_INFO_SWITCH)||key.equals(KEY_WARN_SWITCH)
	 	||key.equals(KEY_ERROR_SWITCH)){
            String str1 = "";
	     String str2 = "";
            boolean materstatus = false;
            boolean oldstatus = false;

            for (int i = 1; i < mSwitchValues.length; i++) {
                if(mSwitchValues[i].equals("1")){
			materstatus = true;
                }
                str2= str2+mSwitchValues[i];
            }
	     oldstatus = mSwitchValues[0].equals("1")? true: false;
	     if(oldstatus != materstatus){
		   mMasterSwitch.setChecked(materstatus);
		   mSwitchValues[0] =  	materstatus ? "1" : "0";
	     }
	     str1 = mSwitchValues[0];
	     writeEnableLogValues(str1+str2);
	     return true;
	 }
	 return false;
    }
    private void writeEnableLogValues(String value) {
        SystemProperties.set(GN_ENABLE_LOG_PROPERTY, value);
    }
    
}
//Gionee <wangguojing> <2013-08-14> add for CR00854573 end

