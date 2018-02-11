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

import com.android.settings.ButtonBarHandler;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;
import android.widget.Button;
import android.widget.Button;
//Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
import com.android.settings.R;
//Gionee fangbin 20120619 added for CR00622030 end

public class WifiPickerActivity extends AuroraPreferenceActivity implements ButtonBarHandler {

    // Same as what are in AuroraPreferenceActivity as private.
    private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    private static final String EXTRA_WIFI_SHOW_ACTION_BAR = "wifi_show_action_bar";
    private static final String EXTRA_WIFI_SHOW_MENUS = "wifi_show_menus";

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, WifiSettings.class.getName());
        }
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    /**
     * Almost dead copy of
     * {@link AuroraPreferenceActivity#startWithFragment(String, Bundle, Fragment, int)}, except
     * this has additional codes for button bar handling.
     */
    @Override
    public void startWithFragment(String fragmentName, Bundle args,
            Fragment resultTo, int resultRequestCode) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, getClass());
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(EXTRA_NO_HEADERS, true);

        final Intent orgIntent = getIntent();
        if (orgIntent.hasExtra(EXTRA_PREFS_SHOW_BUTTON_BAR)) {
            intent.putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR,
                    orgIntent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false));
        }
        if (orgIntent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
            intent.putExtra(EXTRA_PREFS_SET_NEXT_TEXT,
                    orgIntent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT));
        }
        if (orgIntent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
            intent.putExtra(EXTRA_PREFS_SET_BACK_TEXT,
                    orgIntent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT));
        }
        if (orgIntent.hasExtra(EXTRA_WIFI_SHOW_ACTION_BAR)) {
            intent.putExtra(EXTRA_WIFI_SHOW_ACTION_BAR,
                    orgIntent.getBooleanExtra(EXTRA_WIFI_SHOW_ACTION_BAR, true));
        }
        if (orgIntent.hasExtra(EXTRA_WIFI_SHOW_MENUS)) {
            intent.putExtra(EXTRA_WIFI_SHOW_MENUS,
                    orgIntent.getBooleanExtra(EXTRA_WIFI_SHOW_MENUS, true));
        }

        if (resultTo == null) {
            startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    @Override
    public boolean hasNextButton() {
        // AuroraPreferenceActivity#hasNextButton() is protected, so we need to expose it here.
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        // AuroraPreferenceActivity#getNextButton() is protected, so we need to expose it here.
        return super.getNextButton();
    }
    
    // Gionee fangbin 20120619 added for CR00622030 start
    @Override
    protected void onCreate(Bundle icicle) {
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onCreate(icicle);
        
        //Gionee:zhang_xin 20121215 add for CR00746521 start
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
	/*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
	//AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        //Gionee:zhang_xin 20121215 add for CR00746521 end
    }
    // Gionee fangbin 20120619 added for CR00622030 end
}