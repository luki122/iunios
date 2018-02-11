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
package com.android.settings.inputmethod;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;
import android.view.MenuItem;
//Gionee fangbin 20120619 added for CR00622030 start
import com.android.settings.GnSettingsUtils;
import com.android.settings.R;
//Gionee fangbin 20120619 added for CR00622030 end
import com.android.settings.SettingsActivity;

public class InputMethodAndSubtypeEnablerActivity extends SettingsActivity {
    private static final String FRAGMENT_NAME = InputMethodAndSubtypeEnabler.class.getName();

    @Override
    protected void onCreate(final Bundle savedState) {
    	/* super.onCreate(savedState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }*/
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        super.onCreate(savedState);
        
        /*Gionee: huangsf 20121210 add for CR00741405 start*/
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.captioning_locale);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        /*Gionee: huangsf 20121210 add for CR00741405 end*/
        
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    @Override
    public Intent getIntent() {
        final Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, FRAGMENT_NAME);
            //add by jiyouguang
           // modIntent.putExtra(EXTRA_NO_HEADERS, true);
            //end
        }
        return modIntent;
    }
    
    /*Gionee: huangsf 20121210 add for CR00741405 start*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    /*Gionee: huangsf 20121210 add for CR00741405 end*/

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return FRAGMENT_NAME.equals(fragmentName);
    }
}
