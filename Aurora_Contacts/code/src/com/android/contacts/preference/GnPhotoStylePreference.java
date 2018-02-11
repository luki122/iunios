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

import android.content.Context;
import android.content.res.Resources;
import aurora.preference.AuroraListPreference; // import android.preference.ListPreference;
import aurora.preference.AuroraPreference; // import android.preference.Preference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener; //import android.preference.Preference.OnPreferenceChangeListener;
import gionee.provider.GnContactsContract;
import android.util.AttributeSet;

/**
 * Custom preference: view-name-as (first name first or last name first).
 */
public final class GnPhotoStylePreference extends AuroraListPreference implements OnPreferenceChangeListener {
	public static String PHOTO_STYLE_KEY = "photoStyle";
	
    private Context mContext;
    
    private String[] mPhotoOptionsEntries;
	private String[] mPhotoOptionsEntrieValues;

    public GnPhotoStylePreference(Context context) {
        super(context);
        prepare();
    }

    public GnPhotoStylePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        mContext = getContext();
        Resources res = getContext().getResources();

        int entriesResId = 0;
        int entryValuesResId = 0;
        String defaultValue = null;
    	if (ContactsApplication.sIsColorfulContactPhotoSupport) {
    		entriesResId = R.array.gn_entries_list_photo_options_v2;
    		entryValuesResId = R.array.gn_entryvalues_list_photo_options_v2;
    		defaultValue = res.getString(R.string.gn_photo_style_options_default_value_v2);
    	} else {
    		entriesResId = R.array.gn_entries_list_photo_options;
    		entryValuesResId = R.array.gn_entryvalues_list_photo_options;
    		defaultValue = res.getString(R.string.gn_photo_style_options_default_value);
    	}
    	
    	setEntries(entriesResId);
    	setEntryValues(entryValuesResId);
    	setDefaultValue(defaultValue);
    	
    	mPhotoOptionsEntries = res.getStringArray(entriesResId);
        mPhotoOptionsEntrieValues = res.getStringArray(entryValuesResId);
        
        setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
		if (PHOTO_STYLE_KEY.equals(preference.getKey())) {
			CharSequence summary = null;
			if (null != mPhotoOptionsEntries &&
					null != mPhotoOptionsEntrieValues &&
						mPhotoOptionsEntrieValues.length == mPhotoOptionsEntries.length) {
				for (int i = 0; i < mPhotoOptionsEntrieValues.length; i++) {
					if (mPhotoOptionsEntrieValues[i].equals(newValue)) {
						summary = mPhotoOptionsEntries[i]; 
						break;
					}
				}
			}
			ResConstant.setContactPhotoOptions(Integer.valueOf(newValue.toString()));
			
			notifyChanged();
			return true;
    	}
		
		return false;
	}
	
	@Override
	public CharSequence getSummary() {
		return getEntry();
	}
}
