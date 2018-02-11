/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;
 
import android.os.Bundle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import android.content.Intent;
import android.util.Log;

public class LocalFactorySettings extends SettingsPreferenceFragment
                    implements AuroraPreference.OnPreferenceClickListener {

    private static final String TAG = "LocalFactorySettings";
    private static final String KEY_SMART_SCREEN_SETTINGS = "smart_screen_settings";
    private static final String KEY_SCREEN_MODE_SETTINGS = "screen_mode_settings";
    private static final String KEY_POWER_SAVE_MODE_SETTINGS = "power_save_mode_settings";

    private AuroraPreferenceScreen mSmartScreenPref;
    private AuroraPreferenceScreen mScreenModePref;
    private AuroraPreferenceScreen mPowerSaveModePref;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO 自动生成的方法存根
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.local_factory_settings);

//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.setClassName("android", com.android.fenghensettings.smartscreen.SmartScreenSettings);
//        startActivity(intent);
        mSmartScreenPref = (AuroraPreferenceScreen)findPreference(KEY_SMART_SCREEN_SETTINGS);
        mSmartScreenPref.setOnPreferenceClickListener(this);
        mScreenModePref = (AuroraPreferenceScreen)findPreference(KEY_SCREEN_MODE_SETTINGS);
        mScreenModePref.setOnPreferenceClickListener(this);
        mPowerSaveModePref = (AuroraPreferenceScreen)findPreference(KEY_POWER_SAVE_MODE_SETTINGS);
        mPowerSaveModePref.setOnPreferenceClickListener(this);

    }

    public boolean onPreferenceClick(AuroraPreference preference) {
        if (preference.getKey().equals(KEY_SMART_SCREEN_SETTINGS)) {
            Intent intent = new Intent("android.settings.SMART_SCREEN_SETTINGS");
            try {
//                Class cl = Class.forName("android.os.SystemProperties"); 
//                Object invoker = cl.newInstance(); 
//                Method m = cl.getMethod("get", new Class[] { String.class,String.class }); 
//                Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "no message"}); 
                //System.out.println("基带版本: " +(String)result); 
//                Log.v("xiaoyong", "基带版本：" + (String)result);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Unable to start activity " + intent.toString());
            }
            Log.v("xiaoyong", "onPreferenceClick");
        } else if (preference.getKey().equals(KEY_SCREEN_MODE_SETTINGS)) {
            Intent intent = new Intent("com.android.fenghensettings.ModePreview");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Unable to start activity " + intent.toString());
            }
        } else if (preference.getKey().equals(KEY_POWER_SAVE_MODE_SETTINGS)) {
//            Intent intent = new Intent("com.android.fenghensettings.powersavingmode.MenuPowerSavingModeSettings");
              Intent intent = new Intent("android.settings.PSM_SETTINGS");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Unable to start activity " + intent.toString());
            }
        }

        return true;
    }
}
