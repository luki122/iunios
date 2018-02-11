package com.android.settings;


import android.os.Bundle;
import android.provider.Settings;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import android.os.SystemProperties;
public class AuroraVirtualKeySettings extends SettingsPreferenceFragment implements AuroraPreference.OnPreferenceClickListener{
	
	public static final String KEY_LEFT_MODE = "left_mode";
	public static final String KEY_RIGHT_MODE = "right_mode";
	public static final String KEY_MODE = "navigation_key_swap";
	static final String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
    private AuroraUsbPreference mAuroraRightPref;
    private AuroraUsbPreference mAuroraLeftPref;

	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.aurora_virtual_key_settings);
		mAuroraLeftPref = newPref(KEY_LEFT_MODE,R.string.virtual_key_setting_mode_left_pref_title, R.string.virtual_key_setting_mode_left_pref_summary,0);
		mAuroraRightPref = newPref(KEY_RIGHT_MODE,R.string.virtual_key_setting_mode_right_pref_title, R.string.virtual_key_setting_mode_right_pref_summary,1);
		 int temp;
		if(deviceName.contains("i1")){
   		 temp = Settings.System.getInt( getContentResolver(),AuroraVirtualKeySettings.KEY_MODE, 1);
		}else{
   		 temp = Settings.System.getInt( getContentResolver(),AuroraVirtualKeySettings.KEY_MODE, 0);
		}
		if(temp == 1){
			mAuroraRightPref.setChecked(true);
			mAuroraLeftPref.setChecked(false);
		}else{

			mAuroraRightPref.setChecked(false);
			mAuroraLeftPref.setChecked(true);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
    private AuroraUsbPreference newPref(String key, int titleId,int summaryId,int order){
    	AuroraUsbPreference pref = new AuroraUsbPreference(getActivity());
        

        pref.setKey(key);

        pref.setTitle(titleId);
        pref.setSummary(summaryId);
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        pref.setOrder(order);
        getPreferenceScreen().addPreference(pref);
        return pref;
    }

	@Override
	public boolean onPreferenceClick(AuroraPreference arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKey().equals(KEY_LEFT_MODE)){
			Settings.System.putInt( getContentResolver(),KEY_MODE, 0);
			mAuroraRightPref.setChecked(false);
			mAuroraLeftPref.setChecked(true);
		}else if(arg0.getKey().equals(KEY_RIGHT_MODE)){
			Settings.System.putInt( getContentResolver(),KEY_MODE, 1);
			mAuroraRightPref.setChecked(true);
			mAuroraLeftPref.setChecked(false);
		}
		return false;
	}


}
