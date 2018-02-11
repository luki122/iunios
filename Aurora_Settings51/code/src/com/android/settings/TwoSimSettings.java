package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import gionee.telephony.AuroraTelephoneManager;

public class TwoSimSettings extends AuroraSettingsPreferenceFragment implements
		OnPreferenceClickListener {

	private static final String KEY_SIM01_SETTINGS = "sim01_settings";
	private static final String KEY_SIM02_SETTINGS = "sim02_settings";

	private AuroraPreference mSim01Settings;
	private AuroraPreference mSim02Settings;

	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.aurora_twosim_settings);
		mSim01Settings = (AuroraPreference) findPreference(KEY_SIM01_SETTINGS);
		mSim01Settings.auroraSetArrowText("", true);
		mSim01Settings.setOnPreferenceClickListener(this);

		mSim02Settings = (AuroraPreference) findPreference(KEY_SIM02_SETTINGS);
		mSim02Settings.auroraSetArrowText("", true);
		mSim02Settings.setOnPreferenceClickListener(this);

	}

	@Override
	public boolean onPreferenceClick(AuroraPreference preference) {
		Log.i("qy",
				"----TwoSimSettings--onPreferenceClick ---");
		if (preference == mSim01Settings) {
			try {
				Log.i("qy",
						"----mSim01Settings--onPreferenceClick ---");
				Intent intent = new Intent();
				intent.setClassName("com.android.stk",
						"com.android.stk.StkLauncherActivity");
				getActivity().startActivity(intent);
			} catch (Exception e) {
				Log.i("qy",
						"mSimSettings---exception for start activity ---com.android.stk.StkLauncherActivity");
			}
		} else if (preference == mSim02Settings) {
			try {
				Log.i("qy",
						"----mSim02Settings--onPreferenceClick ---");
				Intent in = new Intent();
				if(AuroraTelephoneManager.isMtkGemini()){
					 in.setClassName("com.android.stk",
								"com.android.stk.StkLauncherActivityII");
				 }else{
					 in.setClassName("com.android.stk",
								"com.android.stk.StkLauncherActivity2");
				}
				getActivity().startActivity(in);
			} catch (Exception e) {
				Log.i("qy",
						"mSim02Settings--exception for start activity ---");
			}
		}
		return false;
	}

}
