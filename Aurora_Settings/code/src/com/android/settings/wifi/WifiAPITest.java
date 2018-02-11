/*
 * Copyright (C) 2009 The Android Open Source Project
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

import aurora.app.AuroraAlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import android.text.Editable;
import aurora.widget.AuroraEditText;
// Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
// Gionee fangbin 20120619 added for CR00622030 end


/**
 * Provide an interface for testing out the Wifi API
 */
public class WifiAPITest extends AuroraPreferenceActivity implements
AuroraPreference.OnPreferenceClickListener {

    private static final String TAG = "WifiAPITest";
    private int netid;

    //============================
    // AuroraPreference/activity member variables
    //============================

    private static final String KEY_DISCONNECT = "disconnect";
    private static final String KEY_DISABLE_NETWORK = "disable_network";
    private static final String KEY_ENABLE_NETWORK = "enable_network";

    private AuroraPreference mWifiDisconnect;
    private AuroraPreference mWifiDisableNetwork;
    private AuroraPreference mWifiEnableNetwork;

    private WifiManager mWifiManager;


    //============================
    // AuroraActivity lifecycle
    //============================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee fangbin 20120619 added for CR00622030 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120619 added for CR00622030 end
        super.onCreate(savedInstanceState);

        onCreatePreferences();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    }


    private void onCreatePreferences() {
        addPreferencesFromResource(R.layout.wifi_api_test);

        final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();

        mWifiDisconnect = (AuroraPreference) preferenceScreen.findPreference(KEY_DISCONNECT);
        mWifiDisconnect.setOnPreferenceClickListener(this);

        mWifiDisableNetwork = (AuroraPreference) preferenceScreen.findPreference(KEY_DISABLE_NETWORK);
        mWifiDisableNetwork.setOnPreferenceClickListener(this);

        mWifiEnableNetwork = (AuroraPreference) preferenceScreen.findPreference(KEY_ENABLE_NETWORK);
        mWifiEnableNetwork.setOnPreferenceClickListener(this);

    }

    //============================
    // AuroraPreference callbacks
    //============================

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        return false;
    }

    /**
     *  Implements OnPreferenceClickListener interface
     */
    public boolean onPreferenceClick(AuroraPreference pref) {
        if (pref == mWifiDisconnect) {
            mWifiManager.disconnect();
        } else if (pref == mWifiDisableNetwork) {
            AuroraAlertDialog.Builder alert = new AuroraAlertDialog.Builder(this);
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            // Set an AuroraEditText view to get user input
            final AuroraEditText input = new AuroraEditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    netid = Integer.parseInt(value.toString());
                    mWifiManager.disableNetwork(netid);
                    }
                    });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    }
                    });
            alert.show();
        } else if (pref == mWifiEnableNetwork) {
            AuroraAlertDialog.Builder alert = new AuroraAlertDialog.Builder(this);
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            // Set an AuroraEditText view to get user input
            final AuroraEditText input = new AuroraEditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    netid =  Integer.parseInt(value.toString());
                    mWifiManager.enableNetwork(netid, false);
                    }
                    });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    }
                    });
            alert.show();
        }
        return true;
    }
}
