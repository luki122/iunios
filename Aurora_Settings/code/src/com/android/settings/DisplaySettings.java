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

package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.os.Bundle;
import android.os.RemoteException;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import aurora.provider.AuroraSettings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Build;
import com.android.settings.R;
import com.android.internal.view.RotationPolicy;
import com.android.settings.DreamSettings;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.database.ContentObserver;
import android.database.Observable;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
// Gionee:niejn 20120523 add for CR00605063 begin
import android.os.SystemProperties;
import android.content.ComponentName;
// Gionee:niejn 20120523 add for CR00605063 end
import static android.provider.Settings.Secure;
import android.app.ActivityManager;

import java.util.List;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.os.IPowerManager;
import android.os.PowerManager;
import aurora.app.AuroraAlertDialog;
import android.util.DisplayMetrics;

public class DisplaySettings extends SettingsPreferenceFragment implements
		AuroraPreference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "DisplaySettings";

	/** If there is no setting in the provider, use this. */
	private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 60000;

	private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
	private static final String KEY_ACCELEROMETER = "accelerometer";
	private static final String KEY_FONT_SIZE = "font_size";
	private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
	private static final String KEY_SCREEN_SAVER = "screensaver";
	private static final String KEY_WIFI_DISPLAY = "wifi_display";

	private static final int DLG_GLOBAL_CHANGE_WARNING = 1;
	// Gionee <zhang_xin><2013-03-23> add for CR00788411 begin
	private static final String KEY_GN_FONT_SIZE = "gn_font_size";
	// Gionee <zhang_xin><2013-03-23> add for CR00788411 end
	// Gionee <chenml><2013-04-10> add for CR00795467 begin
	private static final String KEY_GN_BUTTON_LIGHT = "gn_button_light";
	// Gionee <chenml><2013-04-10> add for CR00795467 end
	private static final String BUTTON_LIGHT_STATE = "gn_button_light"; // E6
	private static final String BUTTON_KEY_LIGHT = "button_key_light"; // U2 s4
	private static final String MI_BUTTON_KEY = "screen_buttons_state"; // M3
	private static final String BUTTON_LIGHT_MODE_1 = "button_light_mode"; // 1+

	private static final String SCREEN_BUTTON_TURN_ON = "screen_buttons_turn_on"; // m4

	private static final int DEFAULT_BUTTON_KEY_LIGHT = 1500;// 1.5s

	// Gionee:niejn 20120523 add for CR00605063 begin
	private static final String KEY_FONT_CATEGORY = "font_category";
	private AuroraPreference mFontCategoryPref;
	private static boolean mIsKomoxoSupport = SystemProperties.get(
			"ro.gn.3rd.komoxo.prop").equals("yes");
	// Gionee:niejn 20120523 add for CR00605063 end
	private DisplayManager mDisplayManager;
	// Gionee lihq 2012-09-04 add for CR00682401 start
	private static boolean isExitChange = SystemProperties.get(
			"ro.gn.change.support", "no").equals("yes");
	private static final String KEY_SCREEN_WALLPAPER_CHANGE = "wallpaper_change";
	private AuroraPreference mWallpaper;
	private AuroraPreference mWallpaper_change;
	// Gionee lihq 2012-09-04 add for CR00682401 end

	// Aurora <likai> modify begin
	// private AuroraCheckBoxPreference mAccelerometer;
	private AuroraSwitchPreference mAccelerometer;
	// Aurora <likai> modify end

	// Aurora <likai> <2013-11-04> add begin
	private static final String KEY_AURORA_AUTOMATIC = "aurora_automatic";
	private static final String KEY_AURORA_BRIGHTNESS = "aurora_brightness";
	// qydel private static final String KEY_AURORA_SCREEN_TIMEOUT =
	// "aurora_screen_timeout";
	private static final String KEY_AURORA_PREF_SCREEN_TIMEOUT = "aurora_pref_screen_timeout";
	// qy add 2014 04 06
	private static final String KEY_AURORA_FONT_SIZE_NEW = "aurora_font_size_new";
	private static final String KEY_AURORA_FONT_BOLD = "font_bold";
	private AuroraSwitchPreference mFondBoldPref;

	private String[] mFontSizeNames;

	private static final String KEY_AURORA_FONT_SIZE = "aurora_font_size";

	private AuroraSwitchPreference mAuroraAutomaticPreference;
	private AuroraBrightnessPreference2 mAuroraBrightnessPreferenceSeekbar;
	private AuroraScreenTimeoutPreference mAuroraScreenTimeoutPreference;
	private AuroraFontSizePreference mAuroraFontSizePref;

	/*
	 * private ContentObserver mBrightnessModeObserver = new ContentObserver(new
	 * Handler()) {
	 * 
	 * @Override public void onChange(boolean selfChange) {
	 * onBrightnessModeChanged(); } };
	 */
	// Aurora <likai> <2013-11-04> add end

	private WarnedListPreference mFontSizePref;
	private AuroraCheckBoxPreference mNotificationPulse;
	// Gionee <zhang_xin><2013-03-23> add for CR00788411 begin
	private AuroraPreference mGnFontSizePref;
	// Gionee <zhang_xin><2013-03-23> add for CR00788411 end
	// Gionee <chenml><2013-04-10> add for CR00795467 begin

	// Aurora <likai> modify begin
	/**
	 * Notice: Original means whether shut down button light, now it means
	 * whether set up. We can't change original values, 0 is button light on and
	 * 1 is off.
	 */
	// private AuroraCheckBoxPreference mButtonLight;
	private AuroraSwitchPreference mButtonLight;
	// Aurora <likai> modify end

	// Gionee <chenml><2013-04-10> add for CR00795467 end
	private final Configuration mCurConfig = new Configuration();

	private AuroraListPreference mScreenTimeoutPreference;
	private AuroraPreference mScreenSaverPreference;
	private AuroraPreference mAuroraPrefScreenTimeout;
	private AuroraPreference mAuroraFontSizePreference;

	private WifiDisplayStatus mWifiDisplayStatus;
	private AuroraPreference mWifiDisplayPreference;
	// **********************************
	private static final String KEY_WALLPAPER = "wallpaper";
	AuroraPreference mWallpaperPref;
	// **********************************
	private static final String KEY_LOCK_SCREEN_NOTIFICATIONS = "lock_screen_notifications";
	private static final String INCOMING_INDICATOR_ON_LOCKSCREEN = "incoming_indicator_on_lockscreen";
	private static final int DEFAULT_LOCK_SCREEN_NOTIFICATIONS = 1;
	private AuroraCheckBoxPreference mLockScreenNotifications;

	private static final String GN_GUEST_MODE = "gionee_guest_mode";

	private final RotationPolicy.RotationPolicyListener mRotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
		@Override
		public void onChange() {
			updateAccelerometerRotationCheckbox();
		}
	};

	// Gionee <chenml><2013-04-10> add for CR00795467 begin
	// del 2014 05 22 qy
	/*
	 * private ContentObserver mButtonLightObserver =new ContentObserver(new
	 * Handler()) {
	 * 
	 * @Override public void onChange(boolean selfChange){
	 * updateButtonLightCheckbox(); } };
	 */
	// Gionee <chenml><2013-04-10> add for CR00795467 end
	// Gionee <chenml> <2013-08-08> modify for CR00850629 begin
	private boolean isGuestModeEnabled() {
		boolean enabled = Settings.Secure.getInt(getContentResolver(),
				GN_GUEST_MODE, 0) == 1;
		Log.d(TAG, "Guest mode is " + enabled);
		return enabled;
	}

	// Gionee <chenml> <2013-08-08> modify for CR00850629 end
	// qy 2014 04 25 begin

	private SharedPreferences mIuniSP;
	// qy 2014 04 25 end

	private Handler mHandler = new Handler();
	private AutoBrightnessObserver mAutoBrightnessObserver;

	class AutoBrightnessObserver extends ContentObserver {
		public AutoBrightnessObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			final boolean autoLight = Settings.System.getInt(getActivity()
					.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
			Log.v(TAG, "autoLight===" + autoLight);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mAuroraAutomaticPreference.setChecked(autoLight);

				}
			});

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContentResolver resolver = getActivity().getContentResolver();

		addPreferencesFromResource(R.xml.display_settings);

		// Aurora <likai> <2013-11-04> add begin
		mAuroraAutomaticPreference = (AuroraSwitchPreference) findPreference(KEY_AURORA_AUTOMATIC);
		mAuroraBrightnessPreferenceSeekbar = (AuroraBrightnessPreference2) findPreference(KEY_AURORA_BRIGHTNESS);
		mAuroraAutomaticPreference.setOnPreferenceChangeListener(this);

		// qy add ,always set mode manual

		mIuniSP = getActivity().getSharedPreferences("iuni",
				Context.MODE_PRIVATE);

		// get the autobrightness value
		// boolean autoLight = mIuniSP.getBoolean("aurora_automatic", true);
		boolean autoLight = Settings.System.getInt(getActivity()
				.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
				0) != 0;
		mAuroraAutomaticPreference.setChecked(autoLight);
		// startLightSensorService( autoLight);
		mFontSizeNames = getResources().getStringArray(
				R.array.entries_font_size);

		// end

		boolean automaticAvailable = getResources().getBoolean(
				R.bool.config_automatic_brightness_available);
		if (automaticAvailable) {
			mAuroraAutomaticPreference.setEnabled(true);
		} else {
			mAuroraAutomaticPreference.setEnabled(false);
		}
		// Aurora <likai> <2013-11-04> add end

		// Aurora <likai> modify begin
		// mAccelerometer = (AuroraCheckBoxPreference)
		// findPreference(KEY_ACCELEROMETER);
		mAccelerometer = (AuroraSwitchPreference) findPreference(KEY_ACCELEROMETER);
		mAccelerometer.setOnPreferenceChangeListener(this);
		// Aurora <likai> modify end

		mAccelerometer.setPersistent(false);
		if (RotationPolicy.isRotationLockToggleSupported(getActivity())) {
			// If rotation lock is supported, then we do not provide this option
			// in
			// Display settings. However, is still available in Accessibility
			// settings.
			getPreferenceScreen().removePreference(mAccelerometer);
		}

		mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
		// Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
		// if (mScreenSaverPreference != null
		// && getResources().getBoolean(
		// com.android.internal.R.bool.config_dreamsSupported) == false) {
		if (mScreenSaverPreference != null) {
			getPreferenceScreen().removePreference(mScreenSaverPreference);
		}
		// Gionee <wangguojing> <2013-07-25> add for CR00837650 end

		mScreenTimeoutPreference = (AuroraListPreference) findPreference(KEY_SCREEN_TIMEOUT);
		final long currentTimeout = Settings.System.getLong(resolver,
				SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
		mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
		mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
		disableUnusableTimeouts(mScreenTimeoutPreference);
		updateTimeoutPreferenceDescription(currentTimeout);

		// qy
		mAuroraPrefScreenTimeout = (AuroraPreference) findPreference(KEY_AURORA_PREF_SCREEN_TIMEOUT);
		mFondBoldPref = (AuroraSwitchPreference) findPreference(KEY_AURORA_FONT_BOLD);

		mFondBoldPref.setChecked(Settings.System.getInt(getContentResolver(),
				"font_bold", 0) != 0);
		mFondBoldPref.setOnPreferenceChangeListener(this);

		// Aurora penggangding add begin
		Settings.System.putInt(getContentResolver(), "font_bold", 0);
		getPreferenceScreen().removePreference(mFondBoldPref);
		// Aurora penggangding add end

		// mFondBoldPref.setOnPreferenceClickListener(this);

		// del new font size
		/*
		 * mAuroraFontSizePreference = (AuroraPreference)
		 * findPreference(KEY_AURORA_FONT_SIZE_NEW);
		 * 
		 * String[] screenTimeoutEntries =
		 * getResources().getStringArray(R.array.aurora_screen_timeout_entries);
		 * String[] screenTimeoutValues =
		 * getResources().getStringArray(R.array.aurora_screen_timeout_values);
		 * 
		 * for(int i =0; i< screenTimeoutValues.length; i++){
		 * if((int)currentTimeout == Integer.parseInt(screenTimeoutValues[i])){
		 * String summary =
		 * getActivity().getString(R.string.screen_timeout_summary
		 * ,screenTimeoutEntries[i]);
		 * mAuroraPrefScreenTimeout.auroraSetArrowText(summary,true); } }
		 */

		// mAuroraPrefScreenTimeout.auroraSetArrowText("",true);

		// Aurora <likai> add begin
		if (mScreenTimeoutPreference != null) {
			getPreferenceScreen().removePreference(mScreenTimeoutPreference);
		}
		/*
		 * mAuroraScreenTimeoutPreference = (AuroraScreenTimeoutPreference)
		 * findPreference(KEY_AURORA_SCREEN_TIMEOUT); // qydel
		 * updateScreenTimeoutDes(mAuroraScreenTimeoutPreference);
		 */
		// Aurora <likai> add end

		mFontSizePref = (WarnedListPreference) findPreference(KEY_FONT_SIZE);
		// Gionee:niejn 20120523 add for CR00605063 begin
		mFontCategoryPref = findPreference(KEY_FONT_CATEGORY);

		// Aurora <likai> modify begin
		/*
		 * if (false == mIsKomoxoSupport) {
		 * getPreferenceScreen().removePreference(mFontCategoryPref); }
		 */
		if (mFontCategoryPref != null) {
			getPreferenceScreen().removePreference(mFontCategoryPref);
		}
		// Aurora <likai> modify end

		// Gionee:niejn 20120523 add for CR00605063 end

		// Gionee <zhang_xin><2013-03-23> add for CR00788411 begin
		getPreferenceScreen().removePreference(mFontSizePref); // del
																// src_feature
		mGnFontSizePref = findPreference(KEY_GN_FONT_SIZE);

		// Aurora <likai> add begin
		if (mGnFontSizePref != null) {
			getPreferenceScreen().removePreference(mGnFontSizePref);
		}
		// Aurora <likai> add end

		mAuroraFontSizePref = (AuroraFontSizePreference) findPreference(KEY_AURORA_FONT_SIZE);
		if (mAuroraFontSizePref != null) {
			getPreferenceScreen().removePreference(mAuroraFontSizePref);
		}
		// Gionee <zhang_xin><2013-03-23> add for CR00788411 end
		mFontSizePref.setOnPreferenceChangeListener(this);
		mFontSizePref.setOnPreferenceClickListener(this);
		mNotificationPulse = (AuroraCheckBoxPreference) findPreference(KEY_NOTIFICATION_PULSE);
		if (mNotificationPulse != null
				&& getResources().getBoolean(
						R.bool.config_intrusiveNotificationLed) == false) {
			getPreferenceScreen().removePreference(mNotificationPulse);
		} else {
			try {
				mNotificationPulse.setChecked(Settings.System.getInt(resolver,
						Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
				mNotificationPulse.setOnPreferenceChangeListener(this);
			} catch (SettingNotFoundException snfe) {
				Log.e(TAG, Settings.System.NOTIFICATION_LIGHT_PULSE
						+ " not found");
			}
		}
		mWallpaperPref = findPreference(KEY_WALLPAPER);
		// Gionee lihq 2012-09-04 add for CR00682401 start
		mWallpaper_change = findPreference(KEY_SCREEN_WALLPAPER_CHANGE);

		// Aurora <likai> modify begin
		/*
		 * if(!isExitChange){
		 * getPreferenceScreen().removePreference(mWallpaperPref); }else{
		 * getPreferenceScreen().removePreference(mWallpaper_change); }
		 */
		if (mWallpaperPref != null) {
			getPreferenceScreen().removePreference(mWallpaperPref);
		}
		if (mWallpaper_change != null) {
			getPreferenceScreen().removePreference(mWallpaper_change);
		}
		// Aurora <likai> modify end

		// Gionee <chenml> <2013-08-08> modify for CR00850629 begin
		if (isGuestModeEnabled()) {
			mWallpaper_change.setEnabled(false);
		}
		// Gionee <chenml> <2013-08-08> modify for CR00850629 end

		// Gionee lihq 2012-09-04 add for CR00682401 end
		mDisplayManager = (DisplayManager) getActivity().getSystemService(
				Context.DISPLAY_SERVICE);
		mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
		mWifiDisplayPreference = (AuroraPreference) findPreference(KEY_WIFI_DISPLAY);

		// Aurora <likai> modify begin
		/*
		 * if (mWifiDisplayStatus.getFeatureState() ==
		 * WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE) {
		 * getPreferenceScreen().removePreference(mWifiDisplayPreference);
		 * mWifiDisplayPreference = null; }
		 */
		if (mWifiDisplayPreference != null) {
			getPreferenceScreen().removePreference(mWifiDisplayPreference);
		}
		// Aurora <likai> modify end

		mLockScreenNotifications = (AuroraCheckBoxPreference) findPreference(KEY_LOCK_SCREEN_NOTIFICATIONS);
		mLockScreenNotifications.setOnPreferenceChangeListener(this);

		// Aurora <likai> add begin
		if (mLockScreenNotifications != null) {
			getPreferenceScreen().removePreference(mLockScreenNotifications);
		}
		// Aurora <likai> add end

		// Gionee <chenml><2013-04-10> add for CR00795467 begin

		// Aurora <likai> modify begin
		// mButtonLight =(AuroraCheckBoxPreference)
		// findPreference(KEY_GN_BUTTON_LIGHT);
		mButtonLight = (AuroraSwitchPreference) findPreference(KEY_GN_BUTTON_LIGHT);
		// Aurora <likai> modify end

		mButtonLight.setOnPreferenceChangeListener(this);
		// Gionee <chenml><2013-04-10> add for CR00795467 end

		mAutoBrightnessObserver = new AutoBrightnessObserver(null);
	}

	private void updateTimeoutPreferenceDescription(long currentTimeout) {
		AuroraListPreference preference = mScreenTimeoutPreference;
		String summary;
		if (currentTimeout < 0) {
			// Unsupported value
			// Gionee:baorui 2012-05-02 modify for CR00586196 begin
			// summary = "";
			final CharSequence[] entries = preference.getEntries();
			if (entries.length != 0) {
				summary = preference.getContext().getString(
						R.string.screen_timeout_summary,
						entries[entries.length - 1]);
			} else {
				summary = "";
			}
			// Gionee:baorui 2012-05-02 modify for CR00586196 end
		} else {
			final CharSequence[] entries = preference.getEntries();
			final CharSequence[] values = preference.getEntryValues();
			if (entries == null || entries.length == 0) {
				summary = "";
			} else {
				int best = 0;
				for (int i = 0; i < values.length; i++) {
					long timeout = Long.parseLong(values[i].toString());
					// Gionee:baorui 2012-05-02 modify for CR00586196 begin
					/*
					 * if (currentTimeout >= timeout) { best = i; }
					 */
					if (currentTimeout == timeout) {
						best = i;
						break;
					}
					// Gionee:baorui 2012-05-02 modify for CR00586196 end
				}
				summary = preference.getContext().getString(
						R.string.screen_timeout_summary, entries[best]);
			}
		}
		preference.setSummary(summary);
	}

	private void disableUnusableTimeouts(
			AuroraListPreference screenTimeoutPreference) {
		final DevicePolicyManager dpm = (DevicePolicyManager) getActivity()
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null)
				: 0;
		if (maxTimeout == 0) {
			return; // policy not enforced
		}
		final CharSequence[] entries = screenTimeoutPreference.getEntries();
		final CharSequence[] values = screenTimeoutPreference.getEntryValues();
		ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
		ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
		for (int i = 0; i < values.length; i++) {
			long timeout = Long.parseLong(values[i].toString());
			if (timeout <= maxTimeout) {
				revisedEntries.add(entries[i]);
				revisedValues.add(values[i]);
			}
		}
		if (revisedEntries.size() != entries.length
				|| revisedValues.size() != values.length) {
			screenTimeoutPreference.setEntries(revisedEntries
					.toArray(new CharSequence[revisedEntries.size()]));
			screenTimeoutPreference.setEntryValues(revisedValues
					.toArray(new CharSequence[revisedValues.size()]));
			final int userPreference = Integer.parseInt(screenTimeoutPreference
					.getValue());
			if (userPreference <= maxTimeout) {
				screenTimeoutPreference
						.setValue(String.valueOf(userPreference));
			} else {
				// There will be no highlighted selection since nothing in the
				// list matches
				// maxTimeout. The user can still select anything less than
				// maxTimeout.
				// TODO: maybe append maxTimeout to the list and mark selected.
			}
		}
		screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
	}

	int floatToIndex(float val) {
		String[] indices = getResources().getStringArray(
				R.array.entryvalues_font_size);
		float lastVal = Float.parseFloat(indices[0]);
		for (int i = 1; i < indices.length; i++) {
			float thisVal = Float.parseFloat(indices[i]);
			if (val < (lastVal + (thisVal - lastVal) * .5f)) {
				return i - 1;
			}
			lastVal = thisVal;
		}
		return indices.length - 1;
	}

	public void readFontSizePreference(AuroraListPreference pref) {
		try {
			mCurConfig.updateFrom(ActivityManagerNative.getDefault()
					.getConfiguration());
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to retrieve font size");
		}

		// mark the appropriate item in the preferences list
		int index = floatToIndex(mCurConfig.fontScale);
		pref.setValueIndex(index);

		// report the current size in the summary text
		final Resources res = getResources();
		String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
		pref.setSummary(String.format(
				res.getString(R.string.summary_font_size), fontSizeNames[index]));
	}

	@Override
	public void onResume() {
		super.onResume();

		// Aurora <likai> <2013-11-04> add begin
		/*
		 * getContentResolver().registerContentObserver(
		 * Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
		 * true, mBrightnessModeObserver);
		 */
		// Aurora <likai> <2013-11-04> add end

		// Gionee <chenml><2013-04-10> add for CR00795467 begin
		/*
		 * getContentResolver().registerContentObserver(Settings.System.getUriFor
		 * (BUTTON_LIGHT_STATE), true, mButtonLightObserver);
		 */
		// Gionee <chenml><2013-04-10> add for CR00795467 end
		RotationPolicy.registerRotationPolicyListener(getActivity(),
				mRotationPolicyListener);

		if (mWifiDisplayPreference != null) {
			getActivity().registerReceiver(
					mReceiver,
					new IntentFilter(
							DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
			mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
		}

		updateState();
		mLockScreenNotifications.setChecked(Settings.System.getInt(
				getContentResolver(), INCOMING_INDICATOR_ON_LOCKSCREEN,
				DEFAULT_LOCK_SCREEN_NOTIFICATIONS) == 1);

		// qy update the screen timeout summary
		String[] screenTimeoutEntries = getResources().getStringArray(
				R.array.aurora_screen_timeout_entries);
		String[] screenTimeoutValues = getResources().getStringArray(
				R.array.aurora_screen_timeout_values);
		final int currentTimeout = Settings.System.getInt(getContentResolver(),
				Settings.System.SCREEN_OFF_TIMEOUT,
				FALLBACK_SCREEN_TIMEOUT_VALUE);
		for (int i = 0; i < screenTimeoutValues.length; i++) {
			if (currentTimeout == Integer.parseInt(screenTimeoutValues[i])) {
				String summary = getActivity().getString(
						R.string.screen_timeout_summary,
						screenTimeoutEntries[i]);
				if (mAuroraPrefScreenTimeout != null)
					mAuroraPrefScreenTimeout.auroraSetArrowText(summary, true);
			}
		}
		// update the fontSize pref summary
		// updateAurorFontSizeSummary(mAuroraFontSizePreference);
		// 2014 04 30 del font bold
		// mFondBoldPref.setChecked(Settings.System.getInt(getContentResolver(),
		// "font_bold",0) != 0);

		// end

		getContentResolver()
				.registerContentObserver(
						Settings.System
								.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
						true, mAutoBrightnessObserver);
	}

	private void updateAurorFontSizeSummary(AuroraPreference pref) {
		try {
			mCurConfig.updateFrom(ActivityManagerNative.getDefault()
					.getConfiguration());
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to retrieve font size");
		}
		int index = floatToIndex(mCurConfig.fontScale);

		if (pref != null) {
			pref.auroraSetArrowText(mFontSizeNames[index], true);

		}

	}

	@Override
	public void onPause() {
		super.onPause();

		RotationPolicy.unregisterRotationPolicyListener(getActivity(),
				mRotationPolicyListener);
		// Gionee <chenml><2013-04-10> add for CR00795467 begin
		// getContentResolver().unregisterContentObserver(mButtonLightObserver);
		// Gionee <chenml><2013-04-10> add for CR00795467 end
		if (mWifiDisplayPreference != null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}

	// Aurora <likai> <2013-11-06> add begin
	@Override
	public void onStop() {
		super.onStop();

		// getContentResolver().unregisterContentObserver(mBrightnessModeObserver);

		getContentResolver().unregisterContentObserver(mAutoBrightnessObserver);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mAuroraBrightnessPreferenceSeekbar.stop();

	}

	// Aurora <likai> <2013-11-06> add end

	@Override
	public Dialog onCreateDialog(int dialogId) {
		if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
			return Utils.buildGlobalChangeWarningDialog(getActivity(),
					R.string.global_font_change_title, new Runnable() {
						public void run() {
							mFontSizePref.click();
						}
					});
		}
		return null;
	}

	private void updateState() {
		// Aurora <likai> <2013-11-06> add begin
		// onBrightnessModeChanged();
		// Aurora <likai> <2013-11-06> add end

		updateAccelerometerRotationCheckbox();
		// Gionee <chenml><2013-04-10> add for CR00795467 begin
		updateButtonLightCheckbox();
		// Gionee <chenml><2013-04-10> add for CR00795467 end
		// Gionee <zhang_xin><2013-03-23> add for CR00788411 begin
		updateGnFontSizeSummary(mGnFontSizePref);
		// Gionee <zhang_xin><2013-03-23> add for CR00788411 end
		readFontSizePreference(mFontSizePref);
		updateScreenSaverSummary();
		updateWifiDisplaySummary();
	}

	private void updateScreenSaverSummary() {
		if (mScreenSaverPreference != null) {
			mScreenSaverPreference.setSummary(DreamSettings
					.getSummaryTextWithDreamName(getActivity()));
		}
	}

	private void updateWifiDisplaySummary() {
		if (mWifiDisplayPreference != null) {
			switch (mWifiDisplayStatus.getFeatureState()) {
			case WifiDisplayStatus.FEATURE_STATE_OFF:
				mWifiDisplayPreference
						.setSummary(R.string.wifi_display_summary_off);
				break;
			case WifiDisplayStatus.FEATURE_STATE_ON:
				mWifiDisplayPreference
						.setSummary(R.string.wifi_display_summary_on);
				break;
			case WifiDisplayStatus.FEATURE_STATE_DISABLED:
			default:
				mWifiDisplayPreference
						.setSummary(R.string.wifi_display_summary_disabled);
				break;
			}
		}
	}

	private void updateAccelerometerRotationCheckbox() {
		if (getActivity() == null)
			return;

		mAccelerometer.setChecked(!RotationPolicy
				.isRotationLocked(getActivity()));
	}

	// Gionee <chenml><2013-04-10> add for CR00795467 begin
	private void updateButtonLightCheckbox() {
		String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
		Log.v(TAG, "deviceName======" + deviceName);
		String[] strName = deviceName.split("-");
        if(strName.length > 1){
        	if(strName[1].equals("U3") || strName[1].contains("9004") ||strName[1].contains("MI2") 
        			|| strName[1].equals("H60L01") ||  strName[1].equals("HM1STD")){
        		getPreferenceScreen().removePreference(mButtonLight);
        		Log.v(TAG, "----------return-----");
        		return;
        	}
        }

		if (deviceName.contains("MI4") ||deviceName.contains("HMNOTE")) {
			Log.v(TAG, "--contains mi4--");
			boolean mi4 = Settings.Secure.getInt(getContentResolver(),
					SCREEN_BUTTON_TURN_ON, 1) != 0 ? true : false;
			mButtonLight.setChecked(mi4);
		} else {
			Log.v(TAG, "-----not----contains mi4--");
			boolean st1 = Settings.System.getInt(getContentResolver(),
					BUTTON_LIGHT_STATE, 1) == 0 ? true : false;
			boolean st2 = Settings.System.getInt(getContentResolver(),
					BUTTON_KEY_LIGHT, 0) > 0 ? true : false;
			boolean st3 = Settings.Secure.getInt(getContentResolver(),
					MI_BUTTON_KEY, 1) == 0 ? true : false;
			boolean st4 = Settings.System.getInt(getContentResolver(),
					BUTTON_LIGHT_MODE_1, 2) == 1 ? true : false;
			Log.v(TAG, "---st1----==" + st1);
			Log.v(TAG, "---st2----==" + st2);
			Log.v(TAG, "---st3----==" + st3);
			Log.v(TAG, "---st4----==" + st4);

			mButtonLight.setChecked(st1 && st2 && st3 && st4);
		}
	}

	// Gionee <chenml><2013-04-10> add for CR00795467 end
	public void writeFontSizePreference(Object objValue) {
		try {
			mCurConfig.fontScale = Float.parseFloat(objValue.toString());
			ActivityManagerNative.getDefault().updatePersistentConfiguration(
					mCurConfig);
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to save font size");
		}
	}

	@Override
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		if (preference == mAccelerometer) {
			// Aurora <likai> delete begin
			// RotationPolicy.setRotationLockForAccessibility(
			// getActivity(), !mAccelerometer.isChecked());
			// Aurora <likai> delete end
		} else // qy
		if (KEY_AURORA_PREF_SCREEN_TIMEOUT.equals(preference.getKey())) {

			Intent it = new Intent(getActivity(),
					AuroraScreeTimeoutPickerActivity.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			getActivity().startActivity(it);

		}// del 2014 05 12
		/*
		 * else if(KEY_AURORA_FONT_SIZE_NEW.equals(preference.getKey())){ Intent
		 * fontIntent = new
		 * Intent(getActivity(),AuroraFontSizePickerActivity.class);
		 * getActivity().startActivity(fontIntent); }
		 */
		// qy end
		else if (preference == mNotificationPulse) {
			boolean value = mNotificationPulse.isChecked();
			Settings.System.putInt(getContentResolver(),
					Settings.System.NOTIFICATION_LIGHT_PULSE, value ? 1 : 0);
			return true;
		} else if (preference == mLockScreenNotifications) {
			boolean value = mLockScreenNotifications.isChecked();
			Settings.System.putInt(getContentResolver(),
					INCOMING_INDICATOR_ON_LOCKSCREEN, value ? 1 : 0);
			// Gionee <chenml> <2013-04-10> added for CR00795467 begin
		} else if (preference == mButtonLight) {
			// Aurora <likai> delete begin
			/*
			 * try { boolean state = mButtonLight.isChecked();
			 * Settings.System.putInt(getContentResolver(),
			 * Settings.System.Button_Light_State, state ? 1 : 0); } catch
			 * (Exception e) { // TODO: handle exception Log.e(TAG,
			 * "can not write value in datebase."); }
			 */
			// Aurora <likai> delete end
			// Gionee <chenml> <2013-04-10> added for CR00795467 end

			// Gionee <chenml> <2013-03-12> added for CR00783446 begin
			// Gionee:niejn 20120523 add for CR00605063 begin
		} else if (true == mIsKomoxoSupport && preference == mFontCategoryPref) {
			Intent intent = new Intent();
			// intent.setComponent(new ComponentName("com.komoxo.fontmgr",
			// "com.komoxo.fontmgr.FontManagerActivity"));
			intent.setComponent(new ComponentName("com.komoxo.fontmgrsrc",
					"com.komoxo.fontmgrsrc.FontManagerActivity"));
			startActivity(intent);
		}
		// Gionee:niejn 20120523 add for CR00605063 end
		// Gionee <chenml> <2013-03-12> added for CR00783446 end

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
		if (getContentResolver() == null)
			return true; // BUG #714

		final String key = preference.getKey();
		if (KEY_SCREEN_TIMEOUT.equals(key)) {
			int value = Integer.parseInt((String) objValue);
			try {
				Settings.System.putInt(getContentResolver(),
						SCREEN_OFF_TIMEOUT, value);
				updateTimeoutPreferenceDescription(value);
			} catch (NumberFormatException e) {
				Log.e(TAG, "could not persist screen timeout setting", e);
			}
		}
		if (KEY_FONT_SIZE.equals(key)) {
			writeFontSizePreference(objValue);
		} else if (KEY_AURORA_FONT_BOLD.equals(preference.getKey())) {
			final boolean fontBoldState = (Boolean) objValue;
			boolean stateTemp = false;
			if (Settings.System.getInt(getContentResolver(), "font_bold", 0) != 0) {
				stateTemp = true;
			}

			if (fontBoldState == stateTemp) {
				return true;
			}

			Settings.System.putInt(getContentResolver(), "font_bold",
					fontBoldState ? 1 : 0);
			try {
				Settings.System.getConfiguration(getActivity()
						.getContentResolver(), mCurConfig);
				mCurConfig.fontScale += fontBoldState ? 0.00001 : -0.00001;
				ActivityManagerNative.getDefault()
						.updatePersistentConfiguration(mCurConfig);
			} catch (RemoteException e) {
				Log.w(TAG, "Unable to save font size");
			}
		}

		if (KEY_AURORA_AUTOMATIC.equals(key)) {
			boolean state = ((Boolean) objValue).booleanValue();
			Log.v(TAG, "--onPreferenceChange----KEY_AURORA_AUTOMATIC---state=="
					+ state);
			Settings.System.putInt(getActivity().getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, state ? 1 : 0);
		} else if (KEY_ACCELEROMETER.equals(key)) {
			boolean state = ((Boolean) objValue).booleanValue();
			RotationPolicy.setRotationLockForAccessibility(getActivity(),
					!state);
		} else if (KEY_GN_BUTTON_LIGHT.equals(key)) {
			boolean state = ((Boolean) objValue).booleanValue();
			mIuniSP.edit().putBoolean("aurora_buttonkey_light", state).commit();
			try {
				String deviceName = SystemProperties
						.get("ro.gn.iuniznvernumber");
				if (deviceName.contains("MI4")|| deviceName.contains("HMNOTE")) {
					Log.v(TAG, "----------xiao mi--devices");
					Settings.Secure.putInt(getContentResolver(),
							SCREEN_BUTTON_TURN_ON, state ? 1 : 0);
					Settings.Secure.putInt(getContentResolver(), MI_BUTTON_KEY,
							0);
				} else {
					Log.v(TAG, "----------not mi devices");
					Settings.System.putInt(getContentResolver(),
							BUTTON_LIGHT_STATE, state ? 0 : 1);
					Settings.System.putInt(getContentResolver(),
							BUTTON_KEY_LIGHT, state ? DEFAULT_BUTTON_KEY_LIGHT
									: 0);
					Settings.Secure.putInt(getContentResolver(), MI_BUTTON_KEY,
							state ? 0 : 1);
					Settings.System.putInt(getContentResolver(),
							BUTTON_LIGHT_MODE_1, state ? 1 : 2);
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "can not write value in datebase.");
			}
		}

		return true;
	}

	private void startLightSensorService(boolean state) {
		Intent it = new Intent();
		it.putExtra("AUTO_ADJ_LIGHT", state);
		it.setClass(getActivity(), AuroraLightSensorService.class);
		getActivity().startService(it);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
				mWifiDisplayStatus = (WifiDisplayStatus) intent
						.getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
				updateWifiDisplaySummary();
			}
		}
	};

	@Override
	public boolean onPreferenceClick(AuroraPreference preference) {
		if (preference == mFontSizePref) {
			if (Utils.hasMultipleUsers(getActivity())) {
				showDialog(DLG_GLOBAL_CHANGE_WARNING);
				return true;
			} else {
				mFontSizePref.click();
			}
		}
		if (preference == mFondBoldPref) {
			Log.i("qy", "onPreferenceClick()");
			return true;
		}
		return false;
	}

	// Gionee <zhang_xin><2013-03-23> add for CR00788411 begin
	private void updateGnFontSizeSummary(AuroraPreference preference) {
		int fontState = 0;
		int index = 0;
		try {
			fontState = AuroraSettings.getInt(getContentResolver(),
					AuroraSettings.FONT_SIZE, 0);
			mCurConfig.updateFrom(ActivityManagerNative.getDefault()
					.getConfiguration());
		} catch (RemoteException e) {
			Log.w(TAG, "Unable to retrieve font size");
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (fontState == AuroraSettings.FONT_SIZE_LARGE) {
			index = 3;
		} else if (fontState == AuroraSettings.FONT_SIZE_EXTRA_LARGE) {
			index = 4;
		} else {
			index = GnFontSizeActivity.floatToIndex(getActivity(),
					mCurConfig.fontScale);
		}

		String[] fontSizeNames = getResources().getStringArray(
				R.array.gn_entries_font_size);
		preference.setSummary(String.format(
				getResources().getString(R.string.summary_font_size),
				fontSizeNames[index]));
	}

	// Gionee <zhang_xin><2013-03-23> add for CR00788411 end

	// Aurora <likai> add begin
	/*
	 * private void updateScreenTimeoutDes(AuroraScreenTimeoutPreference
	 * preference) { final int currentTimeoutValue =
	 * Settings.System.getInt(getContentResolver(), SCREEN_OFF_TIMEOUT,
	 * FALLBACK_SCREEN_TIMEOUT_VALUE); String[] screenTimeoutEntries =
	 * getResources().getStringArray(R.array.aurora_screen_timeout_entries);
	 * String[] screenTimeoutValues =
	 * getResources().getStringArray(R.array.aurora_screen_timeout_values); for
	 * (int i = 0; i < screenTimeoutValues.length; i++) { if
	 * (currentTimeoutValue == Integer.parseInt(screenTimeoutValues[i])) { //
	 * Aurora <likai> <2013-10-29> modify begin
	 * //preference.setSummary(getString
	 * (R.string.aurora_display_screen_timeout_summary,
	 * screenTimeoutEntries[i]));
	 * preference.auroraSetArrowText(getString(R.string
	 * .aurora_display_screen_timeout_summary, screenTimeoutEntries[i])); //
	 * Aurora <likai> <2013-10-29> modify end break; } } }
	 */
	// Aurora <likai> add end *qy del

	// Aurora <likai> <2013-11-04> add begin
	/*
	 * private void onBrightnessModeChanged() { boolean checked =
	 * getBrightnessMode(0) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
	 * mAuroraAutomaticPreference.setChecked(checked); }
	 */

	private int getBrightnessMode(int defaultValue) {
		int brightnessMode = defaultValue;
		try {
			brightnessMode = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException snfe) {
		}
		return brightnessMode;
	}
	// Aurora <likai> <2013-11-04> add end
}