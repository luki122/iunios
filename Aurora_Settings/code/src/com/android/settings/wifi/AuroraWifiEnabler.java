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
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraSwitchPreference;

import com.android.settings.R;
import com.android.settings.WirelessSettings;

import java.util.concurrent.atomic.AtomicBoolean;

public class AuroraWifiEnabler implements AuroraPreference.OnPreferenceChangeListener {
	private static final String TAG="AuroraWifiEnabler";
    private final Context mContext;
    private AuroraSwitchPreference mSwitch;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private final WifiManager mWifiManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;
    private boolean isAirplane;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
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
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            	isAirplane = true;
            }
        }
    };

    public AuroraWifiEnabler(Context context, AuroraSwitchPreference switch_) {
        mContext = context;
        mSwitch = switch_;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mSwitch.setOnPreferenceChangeListener(this);
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnPreferenceChangeListener(null);
    }

    public void setSwitch(AuroraSwitchPreference switch_) {
        if (mSwitch == switch_) return;

        mSwitch.setOnPreferenceChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnPreferenceChangeListener(this);

        final int wifiState = mWifiManager.getWifiState();
        boolean isEnabled = wifiState == WifiManager.WIFI_STATE_ENABLED;
        boolean isDisabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
        mSwitch.setChecked(isEnabled);
        mSwitch.setEnabled(isEnabled || isDisabled);
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        boolean isChecked = (Boolean) objValue;
        String buildModel = Build.MODEL;
        WifiSettings.PrintLog(TAG, "Checked:"+isChecked + "  -- BuildMode:"+buildModel);
        if (buildModel.contains("I9500")) {
        	if(isChecked) {
            	mSwitch.setEnabled(false);
            	new Handler().postDelayed(new Runnable() {
    			   	@Override
    			   	public void run() {
    			   		mSwitch.setEnabled(true);
    				}}, 3500);
            }
		}
        //获取到sharepreference 对象，
        SharedPreferences iuniSP = mContext.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
      	//获取到编辑对象  
      	SharedPreferences.Editor edit = iuniSP.edit(); 
        if(isChecked) {
        	if(!isAirplane) {
              	//添加新的值，该值是保存用户输入的新的签名 
              	edit.putString("wifiState", "isChecked");  
              	//提交.  
              	edit.commit();
            }
        } else {
        	if(!isAirplane) {
        		//添加新的值，该值是保存用户输入的新的签名 
              	edit.putString("wifiState", "unChecked");  
              	//提交.  
              	edit.commit();
        	}
        }
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return true;
        }
        // Show toast message if Wi-Fi is not allowed in airplane mode
        if (isChecked && !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_WIFI)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off. No infinite check/listenenr loop.
            ((AuroraSwitchPreference) preference).setChecked(false);
        }

        // Disable tethering if enabling Wifi
        int wifiApState = mWifiManager.getWifiApState();
        if (isChecked && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
            mWifiManager.setWifiApEnabled(null, false);
        }

        if (mWifiManager.setWifiEnabled(isChecked)) {
            // Intent has been taken into account, disable until new state is active
            //mSwitch.setEnabled(false);
        } else {
            // Error
            Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void handleWifiStateChanged(int state) {
    	isAirplane = false;
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
//                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                //setSwitchChecked(true);
//                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
//                mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                //setSwitchChecked(false);
//                mSwitch.setEnabled(true);
                break;
            default:
                //setSwitchChecked(false);
//                mSwitch.setEnabled(true);
                break;
        }
    }

    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }

    private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a AuroraCheckBoxPreference to a AuroraSwitch, this method is useless since
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
}
