/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.SettingInjectorService;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.widget.SwitchBar;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.UtilsExt;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraSwitchPreference;


/*********************************************
*    LOCATION_MODE_OFF = 0;
*    LOCATION_MODE_SENSORS_ONLY = 1;
*    LOCATION_MODE_BATTERY_SAVING = 2;
*    LOCATION_MODE_HIGH_ACCURACY = 3; 
 * ********************************************/

/**
 * Location access settings.
 */
public class LocationSettings extends LocationSettingsBase
        implements SwitchBar.OnSwitchChangeListener ,AuroraPreference.OnPreferenceChangeListener, RadioButtonPreference.OnClickListener{

    private static final String TAG = "LocationSettings";

    /**
     * Key for managed profile location preference category. Category is shown only
     * if there is a managed profile
     */
    private static final String KEY_MANAGED_PROFILE_CATEGORY = "managed_profile_location_category";
    /**
     * Key for managed profile location preference. Note it used to be a switch pref and we had to
     * keep the key as strings had been submitted for string freeze before the decision to
     * demote this to a simple preference was made. TODO: Candidate for refactoring.
     */
    private static final String KEY_MANAGED_PROFILE_PREFERENCE = "managed_profile_location_switch";
    /** Key for preference screen "Mode" */
    
    /** Key for preference category "Recent location requests" */
    private static final String KEY_RECENT_LOCATION_REQUESTS = "recent_location_requests";
    /** Key for preference category "Location services" */
    private static final String KEY_LOCATION_SERVICES = "location_services";
    
    private static final String KEY_LOCATION_TOGGLE = "location_toggle";
    
    private SwitchBar mSwitchBar;
    private aurora.widget.AuroraSwitch mSwitch;
    private boolean mValidListener = false;
    private UserHandle mManagedProfile;
    private AuroraPreference mManagedProfilePreference;
   
    private AuroraPreferenceCategory mCategoryRecentLocationRequests;
    /** Receives UPDATE_INTENT  */
    private BroadcastReceiver mReceiver;
    private SettingsInjector injector;
    private UserManager mUm;
    private ISettingsMiscExt mExt;
    
    private AuroraSwitchPreference mLocationAccess;
    
    //Aurora hujianwei 20160216 modify for location service start
    private static final String KEY_HIGH_ACCURACY = "high_accuracy";
    private RadioButtonPreference mHighAccuracy;
    private static final String KEY_BATTERY_SAVING = "battery_saving";
    private RadioButtonPreference mBatterySaving;
    private static final String KEY_SENSORS_ONLY = "sensors_only";
    private RadioButtonPreference mSensorsOnly;
  //Aurora hujianwei 20160216 modify for location service end
    
    //Aurora hujianwei 20160317 modify for add flags start
    private int mLocationMode = Settings.Secure.LOCATION_MODE_OFF;
    //Aurora hujianwei 20160317 modify for add flags end
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SettingsActivity activity = (SettingsActivity) getActivity();
                activity.setWindowFocusChangedListener(new SettingsActivity.WindowFocusChangeListener() {
			

			@Override
			public void onChange(boolean hasFocus) {
				 Log.i(TAG, "WindowFocusChangeListener -- > onChange  hasFocus = " + hasFocus);
				if(hasFocus){
					setActive(true);
					createPreferenceHierarchy();
				}else{
					setActive(false);
				}
			}
		});
                mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);

        mSwitchBar = activity.getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();
                mSwitchBar.setVisibility(View.GONE); //隐藏掉原生的开关，但是不改变原生的处理逻辑
        
        mExt = UtilsExt.getMiscPlugin(activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
        Log.i(TAG, "onDestroyView " );
                ((SettingsActivity) getActivity()).setWindowFocusChangedListener(null);
            }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "LocationSettings -- onResume ");
        createPreferenceHierarchy();
        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
    }

    @Override
    public void onPause() {
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (RuntimeException e) {
            // Ignore exceptions caused by race condition
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Swallowing " + e);
            }
        }
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mValidListener = false;
        }
        super.onPause();
    }

    private void addPreferencesSorted(List<AuroraPreference> prefs, AuroraPreferenceGroup container) {
        for (AuroraPreference entry : prefs) {
            container.addPreference(entry);
        }
    }

    private AuroraPreferenceScreen createPreferenceHierarchy() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        AuroraPreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.location_settings);
        root = getPreferenceScreen();

        setupManagedProfileCategory(root);
                mLocationAccess = (AuroraSwitchPreference) root.findPreference(KEY_LOCATION_TOGGLE);
        mLocationAccess.setOnPreferenceChangeListener(this);
        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        mLocationMode = mode;
        final boolean enabled = (mode != android.provider.Settings.Secure.LOCATION_MODE_OFF);
        mLocationAccess.setChecked(enabled);

        mCategoryRecentLocationRequests =
                (AuroraPreferenceCategory) root.findPreference(KEY_RECENT_LOCATION_REQUESTS);
        RecentLocationApps recentApps = new RecentLocationApps(activity);
        List<AuroraPreference> recentLocationRequests = recentApps.getAppList();
        if (recentLocationRequests.size() > 0) {
            addPreferencesSorted(recentLocationRequests, mCategoryRecentLocationRequests);
        } else {
            // If there's no item to display, add a "No recent apps" item.
            AuroraPreference banner = new AuroraPreference(activity);
            banner.setLayoutResource(R.layout.location_list_no_item);
            banner.setTitle(R.string.location_no_recent_apps);
            banner.setSelectable(false);
            mCategoryRecentLocationRequests.addPreference(banner);
        }

        boolean lockdownOnLocationAccess = false;
        // Checking if device policy has put a location access lock-down on the managed
        // profile. If managed profile has lock-down on location access then its
        // injected location services must not be shown.
        if (mManagedProfile != null
                && mUm.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION, mManagedProfile)) {
            lockdownOnLocationAccess = true;
        }
        addLocationServices(activity, root, lockdownOnLocationAccess);

        //Aurora hujianwei 20160216 modify for location service start
        mHighAccuracy = (RadioButtonPreference) root.findPreference(KEY_HIGH_ACCURACY);
        mBatterySaving = (RadioButtonPreference) root.findPreference(KEY_BATTERY_SAVING);
        mSensorsOnly = (RadioButtonPreference) root.findPreference(KEY_SENSORS_ONLY);
        mHighAccuracy.setOnClickListener(this);
        mBatterySaving.setOnClickListener(this);
        mSensorsOnly.setOnClickListener(this);
        //Aurora hujianwei 20160216 modify for location service end
        
        refreshLocationMode();
        return root;
    }

    private void setupManagedProfileCategory(AuroraPreferenceScreen root) {
        // Looking for a managed profile. If there are no managed profiles then we are removing the
        // managed profile category.
        mManagedProfile = Utils.getManagedProfile(mUm);
        if (mManagedProfile == null) {
            // There is no managed profile
            mManagedProfilePreference = root.findPreference(KEY_MANAGED_PROFILE_CATEGORY);
            if (mManagedProfilePreference != null)
            root.removePreference(mManagedProfilePreference);
            mManagedProfilePreference = null;
        } else {
            mManagedProfilePreference = root.findPreference(KEY_MANAGED_PROFILE_PREFERENCE);
            mManagedProfilePreference.setOnPreferenceClickListener(null);
        }
    }

    private void changeManagedProfileLocationAccessStatus(boolean enabled, int summaryResId) {
        if (mManagedProfilePreference == null) {
            return;
        }
        mManagedProfilePreference.setEnabled(enabled);
        mManagedProfilePreference.setSummary(summaryResId);
    }

    /**
     * Add the settings injected by external apps into the "App Settings" category. Hides the
     * category if there are no injected settings.
     *
     * Reloads the settings whenever receives
     * {@link SettingInjectorService#ACTION_INJECTED_SETTING_CHANGED}.
     */
    private void addLocationServices(Context context, AuroraPreferenceScreen root,
            boolean lockdownOnLocationAccess) {
        AuroraPreferenceCategory categoryLocationServices =
                (AuroraPreferenceCategory) root.findPreference(KEY_LOCATION_SERVICES);
        injector = new SettingsInjector(context);
        // If location access is locked down by device policy then we only show injected settings
        // for the primary profile.
        List<AuroraPreference> locationServices = injector.getInjectedSettings(lockdownOnLocationAccess ?
                UserHandle.myUserId() : UserHandle.USER_CURRENT);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Received settings change intent: " + intent);
                }
                injector.reloadStatusMessages();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingInjectorService.ACTION_INJECTED_SETTING_CHANGED);
        context.registerReceiver(mReceiver, filter);

        if (locationServices.size() > 0) {
            addPreferencesSorted(locationServices, categoryLocationServices);
        } else {
            // If there's no item to display, remove the whole category.
            root.removePreference(categoryLocationServices);
        }
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_location_access;
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
    	Log.d(TAG, "*** updateRadioButtons");
        if (activated == null) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mHighAccuracy) {
            mHighAccuracy.setChecked(true);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mBatterySaving) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(true);
            mSensorsOnly.setChecked(false);
        } else if (activated == mSensorsOnly) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(true);
        }
    }
    
    @Override
    public void onModeChanged(int mode, boolean restricted) {
    	Log.d(TAG, "*** onModeChanged");
    	//Aurora hujianwei 20160216 modify for location service start
        switch (mode) {
		case Settings.Secure.LOCATION_MODE_OFF:
			updateRadioButtons(null);
			break;
		case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
			updateRadioButtons(mSensorsOnly);
			break;
		case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
			updateRadioButtons(mBatterySaving);
			break;
		case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
			updateRadioButtons(mHighAccuracy);
			break;
		default:
			break;
		}

		boolean enabled = (mode != Settings.Secure.LOCATION_MODE_OFF)
				&& !restricted;
		mHighAccuracy.setEnabled(enabled);
		mBatterySaving.setEnabled(enabled);
		mSensorsOnly.setEnabled(enabled);

        // Restricted user can't change the location mode, so disable the master switch. But in some
        // corner cases, the location might still be enabled. In such case the master switch should
        // be disabled but checked.
        
        mSwitchBar.setEnabled(!restricted);
        
        mExt.updateCustomizedLocationSettings();
        mCategoryRecentLocationRequests.setEnabled(enabled);

        if (enabled != mSwitch.isChecked()) {
            // set listener to null so that that code below doesn't trigger onCheckedChanged()
            if (mValidListener) {
                mSwitchBar.removeOnSwitchChangeListener(this);
            }
            mSwitch.setChecked(enabled);
            if (mValidListener) {
                mSwitchBar.addOnSwitchChangeListener(this);
            }
        }
      //Aurora hujianwei 20160216 modify for location service end
        
        if (mManagedProfilePreference != null) {
            if (mUm.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION, mManagedProfile)) {
                changeManagedProfileLocationAccessStatus(false,
                        R.string.managed_profile_location_switch_lockdown);
            } else {
                if (enabled) {
                    changeManagedProfileLocationAccessStatus(true, R.string.switch_on_text);
                } else {
                    changeManagedProfileLocationAccessStatus(false, R.string.switch_off_text);
                }
            }
        }

        // As a safety measure, also reloads on location mode change to ensure the settings are
        // up-to-date even if an affected app doesn't send the setting changed broadcast.
        injector.reloadStatusMessages();
    }

    /**
     * Listens to the state change of the location master switch.
     */
    @Override
    public void onSwitchChanged(aurora.widget.AuroraSwitch switchView, boolean isChecked) {
        if (isChecked) {
            setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        } else {
            setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_OFF);
        }
    }
    	@Override
	public boolean onPreferenceChange(AuroraPreference paramAuroraPreference,
			Object newValue) {
    		
    	Log.d(TAG, "*** onPreferenceChange");
		if((Boolean) newValue){
			setLocationMode(mLocationMode);
			// setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
		}else {
			 setLocationMode(android.provider.Settings.Secure.LOCATION_MODE_OFF);
		}
		
		return true;
	}

	// Aurora hujianwei 20160216 modify for location service start
	@Override
	public void onRadioButtonClicked(RadioButtonPreference emiter) {
		Log.d(TAG, "*** onRadioButtonClicked");
		int mode = Settings.Secure.LOCATION_MODE_OFF;
		if (emiter == mHighAccuracy){
			mode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
		} else if (emiter == mBatterySaving) {
				mode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
		} else if (emiter == mSensorsOnly) {
			mode = Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
		}
		mLocationMode = mode;
		setLocationMode(mode);
	}
	// Aurora hujianwei 20160216 modify for location service end
}
