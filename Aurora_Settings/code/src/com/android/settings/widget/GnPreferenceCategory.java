/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings.widget;

import com.android.settings.R;

import java.util.Map;

import android.content.Context;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceGroup;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Used to group {@link AuroraPreference} objects
 * and provide a disabled title above the group.
 */
public class GnPreferenceCategory extends AuroraPreferenceCategory {
    private static final String TAG = "GnPreferenceCategory";
    
    AuroraPreference mEmptyPref;
    
    public GnPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mEmptyPref = new AuroraPreference(getContext());
        mEmptyPref.setLayoutResource(R.layout.gn_empty_pref_lyt);
        mEmptyPref.setTitle(R.string.pref_empty_promp);
    }

    public GnPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceCategoryStyle);
    }

    public GnPreferenceCategory(Context context) {
        this(context, null);
    }
    
    @Override
    protected boolean onPrepareAddPreference(AuroraPreference preference) {
        if (preference instanceof GnPreferenceCategory) {
            throw new IllegalArgumentException(
                    "Cannot add a " + TAG + " directly to a " + TAG);
        }
        
        return super.onPrepareAddPreference(preference);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    
    @Override
    protected void onAttachedToActivity() {
    	// TODO Auto-generated method stub
    	super.onAttachedToActivity();
    	
    	if(getPreferenceCount() == 0)
    	{
    		addPreference(mEmptyPref);
    	}
    }
    
    @Override
    protected void onPrepareForRemoval() {
    	// TODO Auto-generated method stub
    	super.onPrepareForRemoval();
    }
    
    @Override
    public boolean addPreference(AuroraPreference preference) {
    	// TODO Auto-generated method stub
    	boolean rtn = super.addPreference(preference);
    	
    	//Once we are adding new pref,remove empty promp pref at same time
    	if(preference != mEmptyPref)
    	{
			removePreference(mEmptyPref);
    	}
		
    	return rtn;
    }
    
    @Override
    public boolean removePreference(AuroraPreference preference) {
    	// TODO Auto-generated method stub
    	boolean rtn = super.removePreference(preference);
    	
    	//if prefs has been removed,we need check whether no pref left
    	if(preference != mEmptyPref)
    	{
        	if(getPreferenceCount() == 0)
        	{
        		addPreference(mEmptyPref);
        	}
    	}

    	return rtn;
    }
}
