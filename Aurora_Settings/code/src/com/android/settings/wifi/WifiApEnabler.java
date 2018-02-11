/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import com.android.settings.R;
import com.android.settings.TetherSettings;
import com.android.settings.WirelessSettings;

import java.util.ArrayList;

import aurora.app.AuroraAlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import aurora.preference.AuroraCheckBoxPreference;
//Aurora <likai> <2013-10-28> add begin
import aurora.preference.AuroraSwitchPreference;
//Aurora <likai> <2013-10-28> add end
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class WifiApEnabler {
	private static final String TAG="WifiApEnabler";
    private final Context mContext;
    // Aurora <likai> <2013-10-28> modify begin
    //private final AuroraCheckBoxPreference mCheckBox;
    private final AuroraSwitchPreference mCheckBox;
    // Aurora <likai> <2013-10-28> modify end
    private final CharSequence mOriginalSummary;

    private WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;

    ConnectivityManager mCm;
    private String[] mWifiRegexs;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WifiSettings.PrintLog(TAG, "action : "+ action );
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
                updateTetherState(available.toArray(), active.toArray(), errored.toArray());
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiCheckBox();
            }

        }
    };

    // Aurora <likai> <2013-10-28> modify begin
    //public WifiApEnabler(Context context, AuroraCheckBoxPreference checkBox) {
    public WifiApEnabler(Context context, AuroraSwitchPreference checkBox) {
    // Aurora <likai> <2013-10-28> modify end
        mContext = context;
        mCheckBox = checkBox;
        mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        enableWifiCheckBox();
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void enableWifiCheckBox() {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if(!isAirplaneMode) {
            mCheckBox.setEnabled(true);
        } else {
            // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() begin
            //mCheckBox.setSummary(mOriginalSummary);
            // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() end
            mCheckBox.setEnabled(false);
        }
    }

    public void setSoftapEnabled(boolean enable) {
        final ContentResolver cr = mContext.getContentResolver();
        
        /**
         * Disable Wifi if enabling tethering
         * 
         */
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }
        if (mWifiManager.setWifiApEnabled(null, enable)) {
            /* Disable here, enabled on receiving success broadcast */
            mCheckBox.setEnabled(false);
        } else {
            // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() begin
            //mCheckBox.setSummary(R.string.wifi_error);
            // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() end
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
        String s = mContext.getString(
        		R.string.aurora_wifi_tether_configure_ssid_default);
        mCheckBox.setSummary(String.format(
                    mContext.getString(R.string.wifi_tether_enabled_subtext),
                    (wifiConfig == null) ? s : wifiConfig.SSID));
    }

    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;

        for (Object o : tethered) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiTethered = true;
            }
        }
        for (Object o: errored) {
            String s = (String)o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) wifiErrored = true;
            }
        }
        //Aurora penggangding 2014-09-03 begin        
        if(getSwitchChecked())
        {
        	mCheckBox.setEnabled(true);
        }
        //Aurora penggangding 2014-09-03 end  
        
        // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() begin
        if (wifiTethered) {
            WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
            //updateConfigSummary(wifiConfig);
        } else if (wifiErrored) {
            //mCheckBox.setSummary(R.string.wifi_error);
        }
        // Aurora <likai> <2013-10-28> delete mCheckBox.setSummary() end
    }

    private void handleWifiApStateChanged(int state) {
    	WifiSettings.PrintLog(TAG, "state:" + state);
        switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
//                mCheckBox.setEnabled(false);   // pgd
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /**
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
//                mCheckBox.setChecked(true);
            	if(TetherSettings.IsHotAccess)
            	{
            		setSwitchChecked(true);     //pgd
            		TetherSettings.IsHotAccess=false;
            	}
                /* Doesnt need the airplane check */
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                mCheckBox.setEnabled(true); 
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:   
//                mCheckBox.setChecked(false);
            	if(TetherSettings.IsHotAccess)
            	{
            		setSwitchChecked(false);  //pgd
            		TetherSettings.IsHotAccess=false;
            	}
                enableWifiCheckBox();
                break;
            default:
                //mCheckBox.setChecked(false);
            	WifiSettings.PrintLog(TAG, "default");
            	setSwitchChecked(false);
                enableWifiCheckBox();
        }
    }

    private void setSwitchChecked(boolean checked) {
        if (checked != mCheckBox.isChecked()) {
            mCheckBox.setChecked(checked);
        }
    }
	
	public boolean getSwitchChecked() {
		return mWifiManager.isWifiApEnabled() ? true : false;
	}
}
