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

package com.android.contacts.preference;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.ResConstant.ContactPhotoStyle;
import com.android.contacts.editor.AuroraContactEditorFragment;
import com.android.contacts.util.IntentFactory;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference; // import android.preference.CheckBoxPreference;
import aurora.preference.AuroraListPreference; // import android.preference.ListPreference;
import aurora.preference.AuroraPreference; // import android.preference.Preference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener; //import android.preference.Preference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference.OnPreferenceClickListener; // import android.preference.Preference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceCategory; // import android.preference.PreferenceCategory;
import aurora.preference.AuroraPreferenceFragment;// import android.preference.PreferenceFragment;
import aurora.preference.AuroraPreferenceManager.OnActivityStopListener; // import android.preference.PreferenceManager.OnActivityStopListener;
import android.provider.ContactsContract.Profile;
import android.util.Log;

/**
 * This fragment shows the preferences for the first header.
 */
public class DisplayOptionsPreferenceFragment extends AuroraPreferenceFragment{

	public static String PHOTO_STYLE_KEY = "photoStyle";
	
	//Gionee:huangzy 20130131 add for CR00770449 start
	private String[] mPhotoOptionsEntries;
	private String[] mPhotoOptionsEntrieValues;
	//Gionee:huangzy 20130131 add for CR00770449 end
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*// Load the preferences from an XML resource
        if (!ContactsPreferenceActivity.isEmpty(getActivity())) {
        	addPreferencesFromResource(R.xml.preference_display_options);
        }*/

        //Goinee:huangzy 20130320 modify for CR00786812 start
        addPreferencesFromResource(R.xml.preference_display_options);
        
        if (isEmpty(getActivity())) {
    		AuroraPreferenceCategory pc = (AuroraPreferenceCategory) findPreference("displayOptions");        		
        	AuroraPreference preference = findPreference("sortOrder");
        	pc.removePreference(preference);
        	
        	preference = findPreference("displayOrder");
        	pc.removePreference(preference);
        }               
    	//Goinee:huangzy 20130320 modify for CR00786812 end
        
    	new SimpleAsynTask() {
        	boolean isProfileNoExist = true;
			@Override
			protected Integer doInBackground(Integer... params) {
				Cursor c = getActivity().getContentResolver().query(
	            		Profile.CONTENT_URI, null, null, null, "_id LIMIT 1");        
	            if (null != c) {
	            	try {
	            		if (c.moveToFirst()) {
	            			isProfileNoExist = c.getCount() <= 0;
	                	}	
	            	} finally {
	            		c.close();	
	            	}
	            }
	            
				return null;
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				if (isProfileNoExist) {
	            	addPreferencesFromResource(R.xml.gn_preference_other_options);
	            	
	            	AuroraPreference preference = findPreference("set_self_profile");
	            	if (null != preference) {
	            		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	    					@Override
	    					public boolean onPreferenceClick(AuroraPreference preference) {
	    						Intent intent = IntentFactory.newInsertContactIntent(true, null, null, null);
	    		                intent.putExtra(AuroraContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
	    		                startActivity(intent);
	    		                getActivity().finish();
	    						return true;
	    					}
	            		});
	            	}	
	            }
			}
		}.execute();	
        
    }
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	  public static boolean isEmpty(Context context) {
	        return !context.getResources().getBoolean(R.bool.config_sort_order_user_changeable)
	                && !context.getResources().getBoolean(R.bool.config_display_order_user_changeable);
	    }
}

