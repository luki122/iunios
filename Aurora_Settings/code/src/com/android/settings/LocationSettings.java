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

package com.android.settings;


import android.app.Dialog;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import android.os.SystemProperties;
import android.net.ConnectivityManager;
import java.util.Observable;
import java.util.Observer;

import com.gionee.settings.utils.GnUtils;

/**
 * Gesture lock pattern settings.
 */
public class LocationSettings extends SettingsPreferenceFragment
        implements AuroraPreference.OnPreferenceChangeListener {

    // Location Settings
    private static final String KEY_LOCATION_TOGGLE = "location_toggle";
    private static final String KEY_LOCATION_NETWORK = "location_network";
    private static final String KEY_LOCATION_GPS = "location_gps";
    private static final String KEY_ASSISTED_GPS = "assisted_gps";

    private AuroraSwitchPreference mNetwork;
    private AuroraSwitchPreference mGps;
//    private AuroraCheckBoxPreference mAssistedGps;
    private AuroraSwitchPreference mLocationAccess;
    private Dialog mDialog;
       
    //private boolean  mIsSimEnable = GnUtils.isSimManagementAvailable(getActivity());
   // private boolean mIsAirplaneMode = (android.provider.Settings.Global.getInt(getActivity().getContentResolver(),android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0);
    // These provide support for receiving notification when Location Manager settings change.
    // This is necessary because the Network Location Provider can change settings
    // if the user does not confirm enabling the provider.
    private ContentQueryMap mContentQueryMap;

    private Observer mSettingsObserver;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
    }
    
   
    
    @Override
    public void onPause() {
        super.onPause();
       

    } 

    @Override
    public void onStart() {
        super.onStart();
        // listen for Location Manager settings changes
        Cursor settingsCursor = getContentResolver().query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED},
                null);
        mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSettingsObserver != null) {
            mContentQueryMap.deleteObserver(mSettingsObserver);
        }
    }

    private AuroraPreferenceScreen createPreferenceHierarchy() {
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.location_settings);
        root = getPreferenceScreen();

        mLocationAccess = (AuroraSwitchPreference) root.findPreference(KEY_LOCATION_TOGGLE);
        
//qy        mNetwork = (AuroraCheckBoxPreference) root.findPreference(KEY_LOCATION_NETWORK);
        mNetwork = (AuroraSwitchPreference) root.findPreference(KEY_LOCATION_NETWORK);
        mNetwork.setOnPreferenceChangeListener(this);
        
        mGps = (AuroraSwitchPreference) root.findPreference(KEY_LOCATION_GPS);
//        mAssistedGps = (AuroraCheckBoxPreference) root.findPreference(KEY_ASSISTED_GPS);
        mGps.setOnPreferenceChangeListener(this);
        mLocationAccess.setOnPreferenceChangeListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();
        updateLocationToggles();

        if (mSettingsObserver == null) {
            mSettingsObserver = new Observer() {
                public void update(Observable o, Object arg) {
                    updateLocationToggles();
                }
            };
        }

        mContentQueryMap.addObserver(mSettingsObserver);
        
    }
    
   

   /* @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        final ContentResolver cr = getContentResolver();
        if (preference == mNetwork) {
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.NETWORK_PROVIDER, mNetwork.isChecked());
        } else if (preference == mGps) {
            boolean enabled = mGps.isChecked();
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.GPS_PROVIDER, enabled);
            if (mAssistedGps != null) {
                mAssistedGps.setEnabled(enabled);
            }
        } else if (preference == mAssistedGps) {
            Settings.Global.putInt(cr, Settings.Global.ASSISTED_GPS_ENABLED,
                    mAssistedGps.isChecked() ? 1 : 0);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }*/

    /*
     * Creates toggles for each available location provider
     */
    private void updateLocationToggles() {
        ContentResolver res = getContentResolver();
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.GPS_PROVIDER);
        boolean networkEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.NETWORK_PROVIDER);
        mGps.setChecked(gpsEnabled);
        mNetwork.setChecked(networkEnabled);
        mLocationAccess.setChecked(gpsEnabled || networkEnabled);
        /*if (mAssistedGps != null) {
            mAssistedGps.setChecked(Settings.Global.getInt(res,
                    Settings.Global.ASSISTED_GPS_ENABLED, 2) == 1);
            mAssistedGps.setEnabled(gpsEnabled);
        }*/
    }

    /**
     * see confirmPatternThenDisableAndClear
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        createPreferenceHierarchy();
    }

    /** Enable or disable all providers when the master toggle is changed. */
    private void onToggleLocationAccess(boolean checked) {
        final ContentResolver cr = getContentResolver();
        if(mLocationAccess.isChecked() != checked){
        	
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.GPS_PROVIDER, checked);
          
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.NETWORK_PROVIDER, checked);
        }
        if(checked&&SystemProperties.get("ro.gn.iuniznvernumber").contains("i1")&&com.android.settings.Settings.isHiden&&!isDataNetworkAvailable()){
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity(),
                    AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
            builder.setTitle(getString(R.string.location_network_title));
            builder.setMessage(getString(R.string.location_network_text));
            builder.setNegativeButton(getString(R.string.location_network_cancel),
                  null);
            builder.setPositiveButton(getString(R.string.location_network_confirm),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	/*Intent intent = new Intent(); 
                        	intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
        					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        					startActivity(intent);*/
                        	final ConnectivityManager cm = ( ConnectivityManager ) getActivity()
                                    .getSystemService(Context.CONNECTIVITY_SERVICE);
      
                            	cm.setMobileDataEnabled(true);
           
                        }
                    });
            mDialog  = builder.create();
            mDialog.show();
        }
       
        
        updateLocationToggles();
    }
    
    private boolean isDataNetworkAvailable(){
    	ConnectivityManager cm = (ConnectivityManager) getActivity()
    	            .getSystemService(Context.CONNECTIVITY_SERVICE) ;
    	return cm.getMobileDataEnabled();
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference pref, Object newValue) {
    	if(null != pref) {
    		if (pref.getKey().equals(KEY_LOCATION_TOGGLE)) {
                onToggleLocationAccess((Boolean) newValue);
            }else if(pref.getKey().equals(KEY_LOCATION_NETWORK)) {
            	Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                        LocationManager.NETWORK_PROVIDER, (Boolean) newValue);
            }else if(pref.getKey().equals(KEY_LOCATION_GPS)) {
            
    	        boolean enabled = (Boolean) newValue;
    	        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
    	                LocationManager.GPS_PROVIDER, enabled);
    	        /*if (mAssistedGps != null) {
    	            mAssistedGps.setEnabled(enabled);
    	        }*/
            }
    	}
        return true;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_location_access;
    }
}

class WrappingSwitchPreference extends AuroraSwitchPreference {

    public WrappingSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WrappingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView title = (TextView) view.findViewById(android.R.id.title);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }
    }
}

class WrappingCheckBoxPreference extends /*AuroraCheckBoxPreference*/AuroraSwitchPreference {

    public WrappingCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WrappingCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView title = (TextView) view.findViewById(android.R.id.title);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }      
        
    }
}
