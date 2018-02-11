/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiWatchdogStateMachine;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class AdvancedWifiSettings extends SettingsPreferenceFragment
        implements AuroraPreference.OnPreferenceChangeListener {

    private static final String TAG = "AdvancedWifiSettings";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_FREQUENCY_BAND = "frequency_band";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_POOR_NETWORK_DETECTION = "wifi_poor_network_detection";
    private static final String KEY_SUSPEND_OPTIMIZATIONS = "suspend_optimizations";

    private static final int SLEEP_POLICY_REQUEST_CODE = 13; 

    private WifiManager mWifiManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_advanced_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
        refreshWifiInfo();
    }

    private void initPreferences() {
        // Aurora <likai> <2013-11-04> modify begin
        //AuroraCheckBoxPreference notifyOpenNetworks =
        //    (AuroraCheckBoxPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
    	AuroraSwitchPreference notifyOpenNetworks =
                (AuroraSwitchPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        notifyOpenNetworks.setOnPreferenceChangeListener(this);
        // Aurora <likai> <2013-11-04> modify end

        notifyOpenNetworks.setChecked(Settings.Global.getInt(getContentResolver(),
                Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        notifyOpenNetworks.setEnabled(mWifiManager.isWifiEnabled());

        AuroraCheckBoxPreference poorNetworkDetection =
            (AuroraCheckBoxPreference) findPreference(KEY_POOR_NETWORK_DETECTION);
        // Aurora <likai> modify begin
        /*
        if (poorNetworkDetection != null) {
            if (Utils.isWifiOnly(getActivity())) {
                getPreferenceScreen().removePreference(poorNetworkDetection);
            } else {
                poorNetworkDetection.setChecked(Global.getInt(getContentResolver(),
                        Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED,
                        WifiWatchdogStateMachine.DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED ?
                        1 : 0) == 1);
            }
        }
        */
        if (poorNetworkDetection != null) {
        	getPreferenceScreen().removePreference(poorNetworkDetection);
        }
        // Aurora <likai> modify end

        AuroraCheckBoxPreference suspendOptimizations =
            (AuroraCheckBoxPreference) findPreference(KEY_SUSPEND_OPTIMIZATIONS);
        // Aurora <likai> modify begin
        //suspendOptimizations.setChecked(Global.getInt(getContentResolver(),
        //        Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1) == 1);
        if (suspendOptimizations != null) {
        	getPreferenceScreen().removePreference(suspendOptimizations);
        }
        // Aurora <likai> modify end

        AuroraListPreference frequencyPref = (AuroraListPreference) findPreference(KEY_FREQUENCY_BAND);
        // Aurora <likai> modify begin
        /*
        if (mWifiManager.isDualBandSupported()) {
            frequencyPref.setOnPreferenceChangeListener(this);
            int value = mWifiManager.getFrequencyBand();
            if (value != -1) {
                frequencyPref.setValue(String.valueOf(value));
            } else {
                Log.e(TAG, "Failed to fetch frequency band");
            }
        } else {
            if (frequencyPref != null) {
                // null if it has already been removed before resume
                getPreferenceScreen().removePreference(frequencyPref);
            }
        }
        */
        if (frequencyPref != null) {
        	getPreferenceScreen().removePreference(frequencyPref);
        }
        // Aurora <likai> modify end

        // Aurora <likai> modify begin
        /*
        AuroraListPreference sleepPolicyPref = (AuroraListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            if (Utils.isWifiOnly(getActivity())) {
                sleepPolicyPref.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
            }
            sleepPolicyPref.setOnPreferenceChangeListener(this);
            int value = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.WIFI_SLEEP_POLICY,
                    Settings.Global.WIFI_SLEEP_POLICY_NEVER);
            String stringValue = String.valueOf(value);
            sleepPolicyPref.setValue(stringValue);
            updateSleepPolicySummary(sleepPolicyPref, stringValue);
        }
        */
        AuroraPreferenceScreen sleepPolicyPref = (AuroraPreferenceScreen) findPreference(KEY_SLEEP_POLICY);
        int value = Settings.Global.getInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                Settings.Global.WIFI_SLEEP_POLICY_NEVER);
        updateSleepPolicySummary(sleepPolicyPref, String.valueOf(value));
        if (sleepPolicyPref != null) {
//        	getPreferenceScreen().removePreference(sleepPolicyPref);
        }
        // Aurora <likai> modify end
    }

    private void updateSleepPolicySummary(AuroraPreference sleepPolicyPref, String value) {
        if (sleepPolicyPref == null) return;

        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            // Aurora <likai> modify begin
            //final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
            //        R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.aurora_wifi_sleep_policy_entries_wifi_only : R.array.aurora_wifi_sleep_policy_entries;
            // Aurora <likai> modify end
            String[] summaries = getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    if (i < summaries.length) {
                    	// Aurora <likai> <2013-10-29> modify begin
                        //sleepPolicyPref.setSummary(summaries[i]);
                        String summary = summaries[i];
                        if (summary.indexOf("（") != -1) {
                            summary = summary.substring(0, summary.indexOf("（"));
                        }
                    	sleepPolicyPref.auroraSetArrowText(summary);
                        // Aurora <likai> <2013-10-29> modify end
                        return;
                    }
                }
            }
        }

        sleepPolicyPref.setSummary("");
        Log.e(TAG, "Invalid sleep policy value: " + value);
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
        String key = preference.getKey();

        // Aurora <likai> add begin
        if (KEY_SLEEP_POLICY.equals(key)) {
            Intent intent = new Intent(getActivity(), WifiSleepPolicyActivity.class);
            startActivityForResult(intent, SLEEP_POLICY_REQUEST_CODE);
        }
        // Aurora <likai> add end

        if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            // Aurora <likai> <2013-11-04> delete begin
            /*
            Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    ((AuroraCheckBoxPreference) preference).isChecked() ? 1 : 0);
            */
            // Aurora <likai> <2013-11-04> delete end
        } else if (KEY_POOR_NETWORK_DETECTION.equals(key)) {
            Global.putInt(getContentResolver(),
                    Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED,
                    ((AuroraCheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else if (KEY_SUSPEND_OPTIMIZATIONS.equals(key)) {
            Global.putInt(getContentResolver(),
                    Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED,
                    ((AuroraCheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        String key = preference.getKey();

        // Aurora <likai> <2013-11-04> add begin
        if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            boolean isChecked = (Boolean) newValue;
        	Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, isChecked ? 1 : 0);
        }
        // Aurora <likai> <2013-11-04> add end

        if (KEY_FREQUENCY_BAND.equals(key)) {
            try {
                mWifiManager.setFrequencyBand(Integer.parseInt((String) newValue), true);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_frequency_band_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (KEY_SLEEP_POLICY.equals(key)) {
            try {
                String stringValue = (String) newValue;
                Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                        Integer.parseInt(stringValue));
                updateSleepPolicySummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void refreshWifiInfo() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        AuroraPreference wifiMacAddressPref = findPreference(KEY_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();

        // Aurora <likai> <2013-10-29> modify begin
        //wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress) ? macAddress
        //        : getActivity().getString(R.string.status_unavailable));
        //wifiMacAddressPref.auroraSetArrowText(!TextUtils.isEmpty(macAddress) ? macAddress
        //        : getActivity().getString(R.string.status_unavailable));
	String mMacAddress = !TextUtils.isEmpty(macAddress) ? macAddress
                : getActivity().getString(R.string.status_unavailable);
	wifiMacAddressPref.setSummary(mMacAddress);
        // Aurora <likai> <2013-10-29> modify end

        AuroraPreference wifiIpAddressPref = findPreference(KEY_CURRENT_IP_ADDRESS);
        String ipAddress = Utils.getWifiIpAddresses(getActivity());

        // Aurora <likai> <2013-10-29> modify begin
        //wifiIpAddressPref.setSummary(ipAddress == null ?
        //        getActivity().getString(R.string.status_unavailable) : ipAddress);
	String mIpAddress = ipAddress == null ?
                getActivity().getString(R.string.status_unavailable) : ipAddress;
	wifiIpAddressPref.setSummary(mIpAddress);
        //wifiIpAddressPref.auroraSetArrowText(ipAddress == null ?
         //       getActivity().getString(R.string.status_unavailable) : ipAddress);
        // Aurora <likai> <2013-10-29> modify end
    }

}
