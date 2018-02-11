package com.mediatek.hotknot;

import android.os.Bundle;
import android.util.Log;
import com.android.settings.AuroraAirplaneModeEnabler;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import aurora.preference.AuroraSwitchPreference;

public class MoreNetworkSettings extends AuroraSettingsPreferenceFragment {

    private static final String KEY_AIRMODE = "air_mode";

    private AuroraAirplaneModeEnabler mAirplaneEnabler;
    private AuroraSwitchPreference mAirplaneModePreference;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.aurora_more_network_settings);

        /*Aurora linchunhui add 20160219*/
        mAirplaneModePreference = (AuroraSwitchPreference) findPreference(KEY_AIRMODE);
        mAirplaneEnabler =  new AuroraAirplaneModeEnabler(getActivity(), mAirplaneModePreference);
        mAirplaneEnabler.resume();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAirplaneEnabler != null){
        	mAirplaneEnabler.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAirplaneEnabler != null){
        	mAirplaneEnabler.pause();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if(mAirplaneEnabler != null){
        	mAirplaneEnabler.destroy();
        }
    }
        /*Aurora linchunhui add end 20160219*/
	
}
