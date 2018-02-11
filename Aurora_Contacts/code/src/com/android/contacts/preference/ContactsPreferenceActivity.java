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

package com.android.contacts.preference;

import com.android.contacts.R;
import com.android.contacts.activities.PeopleActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity; // import android.preference.PreferenceActivity ;
import android.view.MenuItem;

import java.util.List;
// Gionee lixiaohu 2012-11-01 added for CR00722772 start
import com.android.contacts.ContactsApplication;
// Gionee lixiaohu 2012-11-01 added for CR00722772 end

/**
 * Contacts settings.
 */
public final class ContactsPreferenceActivity extends AuroraPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		// Gionee lixiaohu 2012-11-01 added for CR00722772 start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_PeopleTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_PeopleTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_PeopleTheme_light);
        } 
        // Gionee lixiaohu 2012-11-01 added for CR00722772 end	
        super.onCreate(savedInstanceState);

        // This Activity will always fall back to the "top" Contacts screen when touched on the
        // app up icon, regardless of launch context.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
			// Gionee lixiaohu 2012-11-01 added for CR00722772 start
	        if (ContactsApplication.sIsGnContactsSupport) {
			   actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE, 
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);                   
			} else {
			   actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
			}
		    // Gionee lixiaohu 2012-11-01 added for CR00722772 end
        }
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /**
     * Returns true if there are no preferences to display and therefore the
     * corresponding menu item can be removed.
     */
    public static boolean isEmpty(Context context) {
        return !context.getResources().getBoolean(R.bool.config_sort_order_user_changeable)
                && !context.getResources().getBoolean(R.bool.config_display_order_user_changeable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, PeopleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return false;
    }
}
