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

package com.android.settings.bluetooth;

import com.android.settings.R;

import aurora.app.AuroraActivity;
import android.os.Bundle;
// Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
// Gionee fangbin 20120619 added for CR00622030 end

/**
 * AuroraActivity for Bluetooth device picker dialog. The device picker logic
 * is implemented in the {@link BluetoothSettings} fragment.
 */
public final class DevicePickerActivity extends AuroraActivity {

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
        setContentView(R.layout.bluetooth_device_picker);
    
    }


@Override
public void onResume(){
    super.onResume();
    changeStatusBar(true);
}


}
