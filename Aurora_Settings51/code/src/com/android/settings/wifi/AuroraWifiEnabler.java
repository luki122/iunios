/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.BassBoost.OnParameterChangeListener;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.WirelessSettings;
import com.mediatek.settings.PDebug;
import com.mediatek.settings.UtilsExt;
import com.android.settings.search.Index;

import java.util.concurrent.atomic.AtomicBoolean;

public class AuroraWifiEnabler implements OnPreferenceChangeListener  {
    private static final String TAG = "WifiEnabler";
    private Context mContext;
    private AuroraSwitchPreference mCheckBox;
    private boolean mListeningToOnSwitchChange = false;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private final WifiManager mWifiManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action="+action.toString());
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };

    private static final String EVENT_DATA_IS_WIFI_ON = "is_wifi_on";
    private static final int EVENT_UPDATE_INDEX = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_INDEX:
                    final boolean isWiFiOn = msg.getData().getBoolean(EVENT_DATA_IS_WIFI_ON);
                    Index.getInstance(mContext).updateFromClassNameResource(
                            WifiSettings.class.getName(), true, isWiFiOn);
                    break;
            }
        }
    };

    public AuroraWifiEnabler(Context context, AuroraSwitchPreference mCheckBox) {
        mContext = context;
        this.mCheckBox = mCheckBox;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        setupSwitchBar();
    }


	public void setupSwitchBar() {
        final int state = mWifiManager.getWifiState();
        handleWifiStateChanged(state);
        if (!mListeningToOnSwitchChange) {
        	mCheckBox.setOnPreferenceChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
    }

    public void teardownSwitchBar() {
        if (mListeningToOnSwitchChange) {
        	mCheckBox.setOnPreferenceChangeListener(null);
            mListeningToOnSwitchChange = false;
        }
    }

    public void resume(Context context) {
        PDebug.Start("WifiEnabler.resume");
        mContext = context;
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        if (!mListeningToOnSwitchChange) {
        	mCheckBox.setOnPreferenceChangeListener(this);
            mListeningToOnSwitchChange = true;
        }

        PDebug.Start("WifiEnabler.initSwitchState");
        PDebug.End("WifiEnabler.initSwitchState");
        PDebug.End("WifiEnabler.resume");
    }

    public void pause() {
    	Log.d(TAG, " pause");
        mContext.unregisterReceiver(mReceiver);
        if (mListeningToOnSwitchChange) {
        	mCheckBox.setOnPreferenceChangeListener(null);
            mListeningToOnSwitchChange = false;
        }
    }
    
    

    private void handleWifiStateChanged(int state) {
        Log.d(TAG, "handleWifiStateChanged, state = " + state);
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
            	mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchBarChecked(true);
                mCheckBox.setEnabled(true);
                updateSearchIndex(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
            	mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchBarChecked(false);
                mCheckBox.setEnabled(true);
                updateSearchIndex(false);
                break;
            default:
                setSwitchBarChecked(false);
                mCheckBox.setEnabled(true);
                updateSearchIndex(false);
        }
    }

    private void updateSearchIndex(boolean isWiFiOn) {
        mHandler.removeMessages(EVENT_UPDATE_INDEX);

        Message msg = new Message();
        msg.what = EVENT_UPDATE_INDEX;
        msg.getData().putBoolean(EVENT_DATA_IS_WIFI_ON, isWiFiOn);
        mHandler.sendMessage(msg);
    }

    private void setSwitchBarChecked(boolean checked) {
        Log.d(TAG, "setSwitchChecked, checked = " + checked); //M
        mStateMachineEvent = true;
        if(mCheckBox != null){
        		mCheckBox.setChecked(checked);
        }
        mStateMachineEvent = false;
    }

    private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a CheckBoxPreference to a Switch, this method is useless since
        // there is nowhere to display a summary.
        // This code is kept in case a future change re-introduces an associated text.
        /*
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the switch as an optimization.
        if (state != null && mSwitch.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //setSummary(Summary.get(mContext, info.getSSID(), state));
            }
        }
        */
    }

	@Override
	public boolean onPreferenceChange(AuroraPreference arg0, Object objValue) {
		  boolean isChecked = (Boolean) objValue;
	      Log.d(TAG, "onCheckedChanged, isChecked = " + isChecked);
	        //Do nothing if called as a result of a state machine event
	        if (mStateMachineEvent) {
	            return false;
	        }
	        // Show toast message if Wi-Fi is not allowed in airplane mode
	        if (isChecked && !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_WIFI)) {
	            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
	            // Reset switch to off. No infinite check/listenenr loop.
	            mCheckBox.setChecked(false);
	            return false;
	        }

	        // Disable tethering if enabling Wifi
	        int wifiApState = mWifiManager.getWifiApState();
	        if (isChecked && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
	                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
	            mWifiManager.setWifiApEnabled(null, false);
	        }

	        Log.d(TAG, "onCheckedChanged, setWifiEnabled = " + isChecked);

	        if (!mWifiManager.setWifiEnabled(isChecked)) {
	            // Error
	        	mCheckBox.setEnabled(true);
	            Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
	        }
		    return true;
	}
}

