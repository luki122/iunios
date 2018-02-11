/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.android.internal.view.RotationPolicy;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.mediatek.settings.DisplaySettingsExt;
import com.mediatek.settings.FeatureOption;
import com.mediatek.xlog.Xlog;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;

import static android.provider.Settings.Secure.DOZE_ENABLED;
import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import static android.provider.Settings.System.SCREEN_SAVING_MODE;
import static android.provider.Settings.System.SCREEN_SAVING_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_SAVING_MODE_MANUAL_ENABLE;

//<Aurora><hujianwei> 20150721add start
//<Aurora><hujianwei> 20150721add end

public class DisplaySettings extends AuroraSettingsPreferenceFragment implements
        AuroraPreference.OnPreferenceChangeListener, OnPreferenceClickListener,
        Indexable,CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_LIFT_TO_WAKE = "lift_to_wake";
    private static final String KEY_DOZE = "doze";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final String KEY_AUTO_ROTATE = "auto_rotate";

    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;
    private static final int DLG_GLOBAL_CHANGE_WARNING_SCREEN_TIMEOUT = 2;

    private WarnedListPreference mFontSizePref;

    private final Configuration mCurConfig = new Configuration();

    private AuroraListPreference mScreenTimeoutPreference;
    private AuroraPreference mScreenSaverPreference;
    private AuroraSwitchPreference mLiftToWakePreference;
    private AuroraSwitchPreference mDozePreference;
    private AuroraSwitchPreference mAutoBrightnessPreference;

    private static final String KEY_AURORA_BRIGHTNESS = "aurora_brightness";
    private static final String KEY_AURORA_PREF_SCREEN_TIMEOUT = "aurora_pref_screen_timeout";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_BUTTON_LIGHT = "button_light";
	private static final String BUTTON_KEY_LIGHT = "button_key_light";
	private static final String KEY_SCREEN_COLOR_MODE = "screen_color_mode";

	private AuroraPreference mAuroraScreenColorMode;
	private WarnedListPreference mAuroraPrefScreenTimeout;
	private AuroraSwitchPreference mAccelerometer;
	private AuroraSwitchPreference mButtonLight;
	private AuroraBrightnessPreference  mAuroraBrightness;
	
    ///M: MTK feature
    private DisplaySettingsExt mDisplaySettingsExt;
    
  //<Aurora><hujianwei> 20150721add start
    private AuroraSwitchPreference mScreenSavingPreference;
    private static final String KEY_SCREEN_SAVING = "screen_saving";
    public static final String FILE_SAVING_SCREEN_NODE= "/sys/class/leds/amoled_lcm_acl/brightness";
  //<Aurora><hujianwei> 20150721add end

    private ContentObserver mScreenTimeoutObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Xlog.d(TAG, "mScreenTimeoutObserver onChanged");
                int value = Settings.System.getInt(
                        getContentResolver(), SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
                updateTimeoutPreference(value);
            }

        };

     private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
             new RotationPolicy.RotationPolicyListener() {
    		@Override
    		public void onChange() {
    			updateAccelerometerRotationCheckbox();
    		}
    	};   
    	
    	private Handler mHandler = new Handler();
    	private AutoBrightnessObserver mAutoBrightnessObserver;
    	private final class AutoBrightnessObserver extends ContentObserver {
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
    					mAutoBrightnessPreference.setChecked(autoLight);
    				}
    			});
    		}
    	} 	
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();

        addPreferencesFromResource(R.xml.aurora_display_settings);

//        /M: MTK feature @{
//           mDisplaySettingsExt = new DisplaySettingsExt(getActivity());
//           mDisplaySettingsExt.onCreate(getPreferenceScreen());
//        / @}

        mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
        if (mScreenSaverPreference != null
                && getResources().getBoolean(
                com.android.internal.R.bool.config_dreamsSupported) == false) {
//            mDisplaySettingsExt.removePreference(mScreenSaverPreference);
        }

        mScreenTimeoutPreference = (AuroraListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        /**M: for fix bug ALPS00266723 @{*/
        final long currentTimeout = getTimoutValue();
        Xlog.d(TAG, "currentTimeout=" + currentTimeout);
        /**@}*/
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);

        mAuroraScreenColorMode = (AuroraPreference) findPreference(KEY_SCREEN_COLOR_MODE);

        mAccelerometer = (AuroraSwitchPreference) findPreference(KEY_ACCELEROMETER);
		mAccelerometer.setOnPreferenceChangeListener(this);
		mAccelerometer.setPersistent(false);
		
		mButtonLight = (AuroraSwitchPreference) findPreference(KEY_BUTTON_LIGHT);
        mButtonLight.setOnPreferenceChangeListener(this);

        mFontSizePref = (WarnedListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);

        mAuroraPrefScreenTimeout = (WarnedListPreference) findPreference(KEY_AURORA_PREF_SCREEN_TIMEOUT);
        mAuroraPrefScreenTimeout.setOnPreferenceChangeListener(this);
        mAuroraPrefScreenTimeout.setOnPreferenceClickListener(this);

        if (isAutomaticBrightnessAvailable(getResources())) {
            mAutoBrightnessPreference = (AuroraSwitchPreference) findPreference(KEY_AUTO_BRIGHTNESS);
            mAutoBrightnessPreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_AUTO_BRIGHTNESS);
            // mDisplaySettingsExt.removePreference(findPreference(KEY_AUTO_BRIGHTNESS));
        }

        mAuroraBrightness = (AuroraBrightnessPreference) findPreference(KEY_AURORA_BRIGHTNESS);

        if (isLiftToWakeAvailable(activity)) {
            mLiftToWakePreference = (AuroraSwitchPreference) findPreference(KEY_LIFT_TO_WAKE);
            mLiftToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_LIFT_TO_WAKE);
            // mDisplaySettingsExt.removePreference(findPreference(KEY_LIFT_TO_WAKE));
        }

        if (isDozeAvailable(activity)) {
            mDozePreference = (AuroraSwitchPreference) findPreference(KEY_DOZE);
            mDozePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_DOZE);
          //  mDisplaySettingsExt.removePreference(findPreference(KEY_DOZE));
        }

        //<Aurora><hujianwei> 20150721add start
        mScreenSavingPreference = (AuroraSwitchPreference) findPreference(KEY_SCREEN_SAVING);
        if(mScreenSavingPreference != null){
        	mScreenSavingPreference.setChecked(getScreenSavingFlag());
        	mScreenSavingPreference.setOnPreferenceChangeListener(this);
        }
        //<Aurora><hujianwei> 20150721add end
        
    	mAutoBrightnessObserver = new AutoBrightnessObserver(null);

        /*
        if (RotationPolicy.isRotationLockToggleVisible(activity)) {
            DropDownPreference rotatePreference ;
            (DropDownPreference) findPreference(KEY_AUTO_ROTATE);
            rotatePreference.addItem(activity.getString(R.string.display_auto_rotate_rotate),
                    false);
            int rotateLockedResourceId;
            // The following block sets the string used when rotation is locked.
            // If the device locks specifically to portrait or landscape (rather than current
            // rotation), then we use a different string to include this information.
            if (allowAllRotations(activity)) {
                rotateLockedResourceId = R.string.display_auto_rotate_stay_in_current;
            } else {
                if (RotationPolicy.getRotationLockOrientation(activity)
                        == Configuration.ORIENTATION_PORTRAIT) {
                    rotateLockedResourceId =
                            R.string.display_auto_rotate_stay_in_portrait;
                } else {
                    rotateLockedResourceId =
                            R.string.display_auto_rotate_stay_in_landscape;
                }
            }
            rotatePreference.addItem(activity.getString(rotateLockedResourceId), true);
            mDisplaySettingsExt.setRotatePreference(rotatePreference);
            rotatePreference.setSelectedItem(RotationPolicy.isRotationLocked(activity) ?
                    1 : 0);
            rotatePreference.setCallback(new Callback() {
                @Override
                public boolean onItemSelected(int pos, Object value) {
                    RotationPolicy.setRotationLock(activity, (Boolean) value);
                    return true;
                }
            });
        } else {
            removePreference(KEY_AUTO_ROTATE);
//            mDisplaySettingsExt.removePreference(findPreference(KEY_AUTO_ROTATE));
        }*/
    }

    private void updateAccelerometerRotationCheckbox() {
		if (getActivity() == null)
			return;

		mAccelerometer.setChecked(!RotationPolicy
				.isRotationLocked(getActivity()));
	}
    
    private static boolean allowAllRotations(Context context) {
        return Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_allowAllRotations);
    }

    private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(
                    com.android.internal.R.string.config_dozeComponent);
        }
        return !TextUtils.isEmpty(name);
    }

    private static boolean isAutomaticBrightnessAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_automatic_brightness_available);
    }

 @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        mCurConfig.updateFrom(newConfig);
    }

    private int getTimoutValue() {
        int currentValue = Settings.System.getInt(getActivity()
                .getContentResolver(), SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        Xlog.d(TAG, "getTimoutValue()---currentValue=" + currentValue);
        int bestMatch = 0;
        int timeout = 0;
        final CharSequence[] valuesTimeout = mScreenTimeoutPreference
                .getEntryValues();
        for (int i = 0; i < valuesTimeout.length; i++) {
            timeout = Integer.parseInt(valuesTimeout[i].toString());
            if (currentValue == timeout) {
                return currentValue;
            } else {
                if (currentValue > timeout) {
                    bestMatch = i;
                }
            }
        }
        Xlog.d(TAG, "getTimoutValue()---bestMatch=" + bestMatch);
        return Integer.parseInt(valuesTimeout[bestMatch].toString());

    }

    private void updateTimeoutAuroraPreferenceDescription(long currentTimeout) {
        AuroraListPreference preference = mAuroraPrefScreenTimeout;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
            ///M: to prevent index out of bounds @{
            if (entries.length != 0) {
                summary = preference.getContext().getString(
                        R.string.screen_timeout_summary, entries[best]);
            } else {
                summary = "";
            }
           ///M: @}

            }
        }
        preference.setSummary(summary);
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {

        AuroraListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                ///M: to prevent index out of bounds @{
                if (entries.length != 0) {
                    summary = preference.getContext().getString(
                            R.string.screen_timeout_summary, entries[best]);
                } else {
                    summary = "";
                }
                ///M: @}

            }
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(AuroraListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
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
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else if (revisedValues.size() > 0
                    && Long.parseLong(revisedValues.get(revisedValues.size() - 1).toString())
                    == maxTimeout) {
                // If the last one happens to be the same as the max timeout, select that
                screenTimeoutPreference.setValue(String.valueOf(maxTimeout));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    int floatToIndex(float val) {
        Xlog.w(TAG, "floatToIndex enter val = " + val);
        ///M: modify by MTK for EM @{
        int res = -1;//mDisplaySettingsExt.floatToIndex(mFontSizePref, val);
        if (res != -1) {
            return res;
        }
        /// @}

        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    public void readFontSizePreference(AuroraListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        Xlog.d(TAG, "readFontSizePreference index = " + index);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }

    public void readScreenTimeOutPreference(AuroraListPreference pref) {
        int index;
        final int currentTimeout;
        String[] mScreenTimeoutValues;

        mScreenTimeoutValues = getResources().getStringArray(R.array.aurora_screen_timeout_values);
        currentTimeout = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        for(index = 0; index < mScreenTimeoutValues.length; index++){
            if(currentTimeout == Integer.parseInt(mScreenTimeoutValues[index])){
                break;
            }
        }
        Xlog.d(TAG, "readScreenTimeOutPreference index = " + index);
        pref.setValueIndex(index);
        updateTimeoutAuroraPreferenceDescription(currentTimeout);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateState();

        ///M: MTK feature
        //mDisplaySettingsExt.onResume();

        //Not used
        removePreference(KEY_SCREEN_SAVER); //互动屏保
        removePreference(KEY_SCREEN_TIMEOUT); //休眠
        removePreference(KEY_LIFT_TO_WAKE); //拿起设备时唤醒
        removePreference(KEY_DOZE); //主动显示
        removePreference(KEY_BUTTON_LIGHT); //按键灯开关
        
//    	String[] screenTimeoutEntries = getResources().getStringArray(
//				R.array.aurora_screen_timeout_entries);
//		String[] screenTimeoutValues = getResources().getStringArray(
//				R.array.aurora_screen_timeout_values);
		final int currentTimeout = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
//		for (int i = 0; i < screenTimeoutValues.length; i++) {
//			if (currentTimeout == Integer.parseInt(screenTimeoutValues[i])) {
//				String summary = getActivity().getString(
//						R.string.screen_timeout_summary,
//						screenTimeoutEntries[i]);
//				if (mAuroraPrefScreenTimeout != null)
//					mAuroraPrefScreenTimeout.auroraSetArrowText(summary, true);
//			}
//		}

		RotationPolicy.registerRotationPolicyListener(getActivity(), mRotationPolicyListener);

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(SCREEN_OFF_TIMEOUT),
                false, mScreenTimeoutObserver);
		
		getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                true, mAutoBrightnessObserver);

//        mAuroraBrightness.registerCallbacks();
    }

    @Override
    public void onPause() {
        super.onPause();
        ///M: MTK feature
      //  mDisplaySettingsExt.onPause();
       // mAuroraBrightness.unregisterCallbacks();
    }

    @Override
	public void onStop() {
		super.onStop();
		getContentResolver().unregisterContentObserver(mAutoBrightnessObserver);
	}
    
    @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mAuroraBrightness.stop();
	}

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        } else if (dialogId == DLG_GLOBAL_CHANGE_WARNING_SCREEN_TIMEOUT) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.aurora_display_screen_timeout,
                    new Runnable() {
                        public void run() {
                            mAuroraPrefScreenTimeout.click();
                        }
                    });
        }
        return null;
    }

    private void updateState() {
        readFontSizePreference(mFontSizePref);
        readScreenTimeOutPreference(mAuroraPrefScreenTimeout);
        updateScreenSaverSummary();

        // Update auto brightness if it is available.
        if (mAutoBrightnessPreference != null) {
            int brightnessMode = Settings.System.getInt(getContentResolver(),
                    SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
            mAutoBrightnessPreference.setChecked(brightnessMode != SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        // Update lift-to-wake if it is available.
        if (mLiftToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), WAKE_GESTURE_ENABLED, 0);
            mLiftToWakePreference.setChecked(value != 0);
        }

        // Update doze if it is available.
        if (mDozePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOZE_ENABLED, 1);
            mDozePreference.setChecked(value != 0);
        }

        // Update accelerometer if it is available.
        if (mAccelerometer != null) {
            updateAccelerometerRotationCheckbox();
        }
    }

    /**M: for fix bug not sync status bar when lock screen @{*/
    private void updateTimeoutPreference(int currentTimeout) {
        Xlog.d(TAG, "currentTimeout=" + currentTimeout);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        updateTimeoutPreferenceDescription(currentTimeout);
        AlertDialog dlg = (AlertDialog) mScreenTimeoutPreference.getDialog();
        if (dlg == null || !dlg.isShowing()) {
            return;
        }
        ListView listview = dlg.getListView();
        int checkedItem = mScreenTimeoutPreference.findIndexOfValue(
        mScreenTimeoutPreference.getValue());
        if (checkedItem > -1) {
            listview.setItemChecked(checkedItem, true);
            listview.setSelection(checkedItem);
        }
    }
    /**@}*/

    private void updateScreenSaverSummary() {
        if (mScreenSaverPreference != null) {
            mScreenSaverPreference.setSummary(
                    DreamSettings.getSummaryTextWithDreamName(getActivity()));
        }
    }

    public void writeFontSizePreference(Object objValue) {
        mCurConfig.fontScale = Float.parseFloat(objValue.toString());
        Xlog.d(TAG, "writeFontSizePreference font size =  " + Float.parseFloat(objValue.toString()));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
                } catch (RemoteException e) {
                    Log.w(TAG, "Unable to save font size");
                }
            }
        }, 300);
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        ///M: add MTK feature @{
        //mDisplaySettingsExt.onPreferenceClick(preference);
        /// @}
    	/*if (KEY_AURORA_PREF_SCREEN_TIMEOUT.equals(preference.getKey())) {
			Intent it = new Intent(getActivity(), AuroraScreenTimeoutPickerActivity.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			getActivity().startActivity(it);
		}*/
        if (KEY_SCREEN_COLOR_MODE.equals(preference.getKey())){
			Intent it = new Intent(getActivity(), AuroraScreenColorMode.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			getActivity().startActivity(it);
		}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }
        if (KEY_AURORA_PREF_SCREEN_TIMEOUT.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                updateTimeoutAuroraPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
            Xlog.d(TAG, objValue.toString());
        }
        if (preference == mAutoBrightnessPreference) {
            boolean auto = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE,
                    auto ? SCREEN_BRIGHTNESS_MODE_AUTOMATIC : SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        //<Aurora><hujianwei> 20150721add start
        if(preference == mScreenSavingPreference)
        {
        	boolean bFlag = (Boolean) objValue;
        	if(getScreenSavingFlag() != bFlag){
        		//状态值有改变再更新数据库
        		setScreenSavingFlag(bFlag);
        		//更新节点数据
        		writePreferenceClick(bFlag, FILE_SAVING_SCREEN_NODE);
        	}
        }
        //<Aurora><hujianwei> 20150721add end
        if (preference == mLiftToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), WAKE_GESTURE_ENABLED, value ? 1 : 0);
        }
        if (preference == mDozePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOZE_ENABLED, value ? 1 : 0);
        }
        if (KEY_ACCELEROMETER.equals(key)) {
            boolean state = ((Boolean) objValue).booleanValue();
            RotationPolicy.setRotationLockForAccessibility(getActivity(), !state);
        }
        if(preference == mButtonLight){
			boolean state = ((Boolean) objValue).booleanValue();
			try {
					Settings.System.putInt(getContentResolver(),
							BUTTON_KEY_LIGHT, state ? 1500: 0);
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "can not write value in datebase.");
			}
        }
 
        return true;
    }

    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            } else {
                mFontSizePref.click();
            }
        } else if (preference == mAuroraPrefScreenTimeout) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING_SCREEN_TIMEOUT);
                return true;
            } else {
                mAuroraPrefScreenTimeout.click();
            }
        }
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.aurora_display_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    if (!context.getResources().getBoolean(
                            com.android.internal.R.bool.config_dreamsSupported)
                            || FeatureOption.MTK_GMO_RAM_OPTIMIZE) {
                        result.add(KEY_SCREEN_SAVER);
                    }
                    if (!isAutomaticBrightnessAvailable(context.getResources())) {
                        result.add(KEY_AUTO_BRIGHTNESS);
                    }
                    if (!isLiftToWakeAvailable(context)) {
                        result.add(KEY_LIFT_TO_WAKE);
                    }
                    if (!isDozeAvailable(context)) {
                        result.add(KEY_DOZE);
                    }
                    if (!RotationPolicy.isRotationLockToggleVisible(context)) {
                        result.add(KEY_AUTO_ROTATE);
                    }
                    return result;
                }
            };

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	
	//<Aurora> <hujianwei> 20150721 add start
	/**************************************
	 * 接口：setScreenSavingFlag
	 * 描述：设置屏幕省电状态值
	 * 返回值：void
	 *  ０：屏幕省电关闭
	 *  １：屏幕省电开启
	 *  
	 *  修改记录：
	 *  
	 ***************************************/
	public  void setScreenSavingFlag(boolean bFlag){
//        Settings.System.putInt(getContentResolver(), SCREEN_SAVING_MODE, bFlag ? SCREEN_SAVING_MODE_MANUAL_ENABLE : SCREEN_SAVING_MODE_MANUAL);
        //// FIXME: 2/1/16  SCREEN_SAVING_MODE_MANUAL_ENABLE ?== SCREEN_SAVING_MODE_MANUAL
        Settings.System.putInt(getContentResolver(), SCREEN_SAVING_MODE, bFlag ? 1 : 0);
    }
	
	/**************************************
	 * 接口：getScreenSavingFlag
	 * 描述：获取屏幕省电状态值
	 * 返回值：
	 *  ０：屏幕省电关闭
	 *  １：屏幕省电开启
	 *  
	 *  修改记录：
	 *  
	 ***************************************/
	public  boolean getScreenSavingFlag(){
		return Settings.System.getInt(getContentResolver(),SCREEN_SAVING_MODE, SCREEN_SAVING_MODE_MANUAL) == SCREEN_SAVING_MODE_MANUAL_ENABLE ? true : false;
	}
	/**************************************
	 * 接口：writePreferenceClick
	 * 描述：根据读取的状态值写节点数据
	 * 参数说明：
	 * isChecked     0 关闭功能　　１开启功能
	 * 返回值：void
	 *  
	 *  修改记录：
	 *  
	 ***************************************/
	 public static void writePreferenceClick(boolean isChecked, String filePath) {
	    	FileOutputStream out = null;
	    	File outFile = null;
	    	try {
		    	outFile = new File(filePath);
		    	if(!outFile.exists()){
		    		return;
		    	}
		    	out = new FileOutputStream(outFile);
		    	if(isChecked) {

		    		out.write("1\n".getBytes());
		    	} else {
		    		out.write("0\n".getBytes());
		    	}
		
		    	if (null != out) {  
					out.flush();  
					out.close();
			    }
		    } catch (Exception e) {

				e.printStackTrace();
			}
	    }
	
	//<Aurora> <hujianwei> 20150721 add end
	
}
