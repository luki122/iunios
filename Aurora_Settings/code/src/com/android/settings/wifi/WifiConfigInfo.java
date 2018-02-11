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

import aurora.app.AuroraActivity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.WifiConfiguration;
import java.util.List;

import com.android.settings.R;
// Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
// Gionee fangbin 20120619 added for CR00622030 end


/**
 * Configuration details saved by the user on the WifiSettings screen
 */
public class WifiConfigInfo extends AuroraActivity {

    private static final String TAG = "WifiConfigInfo";

    private TextView mConfigList;
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

        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        setContentView(R.layout.wifi_config_info);
        mConfigList = (TextView) findViewById(R.id.config_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();
        if (wifiConfigs == null) return;

        StringBuffer configList  = new StringBuffer();
        for (int i = wifiConfigs.size() - 1; i >= 0; i--) {
            configList.append(wifiConfigs.get(i));
        }
        mConfigList.setText(configList);
    }

}
