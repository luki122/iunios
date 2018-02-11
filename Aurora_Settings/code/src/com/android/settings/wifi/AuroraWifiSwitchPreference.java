package com.android.settings.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraSwitch;

public class AuroraWifiSwitchPreference extends AuroraSwitchPreference{

	private Context mContext;
	private WifiEnabler mWifiEnabler = null;
	private final WifiManager mWifiManager;

	public AuroraWifiSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	@Override
    protected void onBindView(View view) {
		super.onBindView(view);

        AuroraSwitch wifiSwitch = (AuroraSwitch) view.findViewById(com.aurora.internal.R.id.aurora_switchWidget);

        if (wifiSwitch != null) {
        	final int wifiState = mWifiManager.getWifiState();
            boolean isEnabled = wifiState == WifiManager.WIFI_STATE_ENABLED;
            boolean isDisabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
            wifiSwitch.setChecked(isEnabled);
            wifiSwitch.setEnabled(isEnabled || isDisabled);
            wifiSwitch.setClickable(true);
        	if (mWifiEnabler == null) {
	            mWifiEnabler = new WifiEnabler(mContext, wifiSwitch);
	            mWifiEnabler.resume();
        	} else {
        		mWifiEnabler.setSwitch(wifiSwitch);
        	}
        }
    }

	public WifiEnabler getWifiEnabler(){
		return mWifiEnabler;
	}
}
