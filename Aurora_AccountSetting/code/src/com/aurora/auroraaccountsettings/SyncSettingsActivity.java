/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.aurora.auroraaccountsettings;

import android.content.Intent;
import aurora.preference.AuroraPreferenceActivity;
//Gionee <chenml> <2013-07-16> add for CR00835818 begin
import android.os.Bundle;
import android.view.MenuItem;

//import com.android.settings.GnSettingsUtils;
//Gionee <chenml> <2013-07-16> add for CR00835818 end
/**
 * Launcher activity for the SyncSettings fragment.
 *
 */
public class SyncSettingsActivity extends AuroraPreferenceActivity {
    //Gionee <chenml> <2013-07-16> add for CR00835818 begin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
           /* if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
                setTheme(R.style.GnSettingsLightTheme);
            } else {
                setTheme(R.style.GnSettingsDarkTheme);
            }*/

        super.onCreate(savedInstanceState);
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        /*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.account_sync_settings_title);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //Gionee <chenml> <2013-07-16> add for CR00835818 end
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, SyncSettings.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
}
