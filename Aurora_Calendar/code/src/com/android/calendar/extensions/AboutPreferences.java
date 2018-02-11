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

package com.android.calendar.extensions;

import aurora.app.AuroraActivity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.calendar.CalendarApplication;
import com.android.calendar.R;

public class AboutPreferences extends AuroraPreferenceFragment {
    private static final String BUILD_VERSION = "build_version";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        
//        addPreferencesFromResource(R.xml.about_preferences);
//
//        final AuroraActivity activity = getActivity();
//        try {
//            final PackageInfo packageInfo =
//                activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
//            findPreference(BUILD_VERSION).setSummary(packageInfo.versionName);
//        } catch (NameNotFoundException e) {
//            findPreference(BUILD_VERSION).setSummary("?");
//        }
//        
//        
        
        
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	View view = inflater.inflate(R.layout.gn_about_activity_layout,null);
    	
    	
    	TextView textView = (TextView) view.findViewById(R.id.about_update_button);
    	textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Log.d("upgrade", "respond ???");
				
				//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
		        if (CalendarApplication.isEnableUpgrade()) {
		            Intent checkIntent = new Intent("android.intent.action.GN_APP_UPGRADE_CHECK_VERSION");
		            checkIntent.putExtra("package", AboutPreferences.this.getActivity().getApplicationContext().getPackageName());
		            checkIntent.putExtra("isAuto", "false");
		            AboutPreferences.this.getActivity().startService(checkIntent);
		            
		           
		            //Log.d("upgrade", "startService");
		            Log.d("upgrade", "about  "+AboutPreferences.this.getActivity().getApplicationContext().getPackageName());
		        }
		        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
				
			}
		});
    	
    	
    	
    	return view;
    }
    
    @Override
    public void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    	//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
    	CalendarApplication temp = (CalendarApplication) getActivity().getApplication();
    	temp.registerVersionCallback(getActivity());
        
        Log.d("upgrade", "onResume     "+"1111111111111111111111");
        
        //Log
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
    }
    
    
    @Override
    public void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	
    	//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
        ((CalendarApplication) getActivity().getApplication()).unregisterVersionCallback(getActivity());
        
        Log.d("upgrade", "unregisterVersionCallback2");
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
    	
    }
    
    
    
}