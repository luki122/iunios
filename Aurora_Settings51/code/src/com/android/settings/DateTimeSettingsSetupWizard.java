/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.os.SystemProperties;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;
import com.android.setupwizard.navigationbar.SetupWizardNavBar.NavigationBarListener;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceFragment;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraTimePicker;

public class DateTimeSettingsSetupWizard extends AuroraActivity
        implements OnClickListener, OnItemClickListener, OnCheckedChangeListener,
        SetupWizardNavBar.NavigationBarListener, AuroraPreferenceFragment.OnPreferenceStartFragmentCallback {
    private static final String TAG = DateTimeSettingsSetupWizard.class.getSimpleName();

    // force the first status of auto datetime flag.
    private static final String EXTRA_INITIAL_AUTO_DATETIME_VALUE =
            "extra_initial_auto_datetime_value";

    // If we have enough screen real estate, we use a radically different layout with
    // big date and time pickers right on the screen, which requires very different handling.
    // Otherwise, we use the standard date time settings fragment.
    private boolean mUsingXLargeLayout;

    /* Available only in XL */
    private CompoundButton mAutoDateTimeButton;
    // private CompoundButton mAutoTimeZoneButton;

    private Button mTimeZoneButton;
    private ListPopupWindow mTimeZonePopup;
    private SimpleAdapter mTimeZoneAdapter;
    private TimeZone mSelectedTimeZone;

    private AuroraTimePicker mTimePicker;
    private AuroraDatePicker mDatePicker;
    private InputMethodManager mInputMethodManager;
    
    private TextView mNextButton;
    private TextView mBackButton;
    private NavigationBarListener mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee fangbin 20120619 added for CR00622030 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120619 added for CR00622030 end
        setTheme(R.style.setup_wizard_style);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.date_time_settings_setupwizard);
        getAuroraActionBar().setVisibility(View.GONE);

        // we know we've loaded the special xlarge layout because it has controls
        // not present in the standard layout
        mUsingXLargeLayout = findViewById(R.id.time_zone_button) != null;
        if (mUsingXLargeLayout) {
            initUiForXl();
        }
        /*else {
            findViewById(R.id.ll_skip).setOnClickListener(this);
            findViewById(R.id.iv_back).setOnClickListener(this);
        }*/
        mTimeZoneAdapter = ZonePickerWizard.constructTimezoneAdapter(this, false,
                R.layout.date_time_setup_custom_list_item_2);

        // For the normal view, disable Back since changes stick immediately
        // and can't be canceled, and we already have a Next button. For xLarge,
        // though, we save up our changes and set them upon Next, so Back can
        // cancel. And also, in xlarge, we need the keyboard dismiss button
        // to be available.
        if (!mUsingXLargeLayout) {
            final View layoutRoot = findViewById(R.id.layout_root);
            layoutRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_BACK);
        }
        mCallback = this;
        mNextButton = (TextView) this.findViewById(R.id.setup_wizard_navbar_next);
        mBackButton = (TextView) this.findViewById(R.id.setup_wizard_navbar_back);
        mNextButton.setOnClickListener(this);
        mNextButton.setText("下一步");
        mBackButton.setOnClickListener(this);
    }

    public void initUiForXl() {
        // Currently just comment out codes related to auto timezone.
        // TODO: Remove them when we are sure they are unnecessary.
        /*
        final boolean autoTimeZoneEnabled = isAutoTimeZoneEnabled();
        mAutoTimeZoneButton = (CompoundButton)findViewById(R.id.time_zone_auto);
        mAutoTimeZoneButton.setChecked(autoTimeZoneEnabled);
        mAutoTimeZoneButton.setOnCheckedChangeListener(this);
        mAutoTimeZoneButton.setText(autoTimeZoneEnabled ? R.string.zone_auto_summaryOn :
                R.string.zone_auto_summaryOff);*/

        final TimeZone tz = TimeZone.getDefault();
        mSelectedTimeZone = tz;
        mTimeZoneButton = (Button) findViewById(R.id.time_zone_button);
        mTimeZoneButton.setText(tz.getDisplayName());
        // mTimeZoneButton.setText(DateTimeSettings.getTimeZoneText(tz));
        //mTimeZoneButton.setOnClickListener(this);

        final boolean autoDateTimeEnabled;
        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_INITIAL_AUTO_DATETIME_VALUE)) {
            autoDateTimeEnabled = intent.getBooleanExtra(EXTRA_INITIAL_AUTO_DATETIME_VALUE, false);
        } else {
            autoDateTimeEnabled = isAutoDateTimeEnabled();
        }

        mAutoDateTimeButton = (CompoundButton) findViewById(R.id.date_time_auto_button);
        mAutoDateTimeButton.setChecked(autoDateTimeEnabled);
        mAutoDateTimeButton.setOnCheckedChangeListener(this);

        mTimePicker = (AuroraTimePicker) findViewById(R.id.time_picker);
        mTimePicker.setEnabled(!autoDateTimeEnabled);
        mDatePicker = (AuroraDatePicker) findViewById(R.id.date_picker);
        mDatePicker.setEnabled(!autoDateTimeEnabled);
        mDatePicker.setCalendarViewShown(false);

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        /*((Button) findViewById(R.id.next_button)).setOnClickListener(this);
        final Button skipButton = (Button) findViewById(R.id.skip_button);
        if (skipButton != null) {
            skipButton.setOnClickListener(this);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter, null, null);
        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);

    }
    
    @Override
    public void onAttachedToWindow () {
        super.onAttachedToWindow();
        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                   com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, DateTimeSettingsSetupWizard.this);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    	 com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                 com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, DateTimeSettingsSetupWizard.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.time_zone_button: {
                showTimezonePicker(R.id.time_zone_button);
                break;
            }
           /* case R.id.ll_skip: {
                if (mSelectedTimeZone != null) {
                    final TimeZone systemTimeZone = TimeZone.getDefault();
                    if (!systemTimeZone.equals(mSelectedTimeZone)) {
                        Log.i(TAG, "Another TimeZone is selected by a user. Changing system TimeZone.");
                        final AlarmManager alarm = (AlarmManager)
                                getSystemService(Context.ALARM_SERVICE);
                        alarm.setTimeZone(mSelectedTimeZone.getID());
                    }
                }
                if (mAutoDateTimeButton != null) {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME,
                            mAutoDateTimeButton.isChecked() ? 1 : 0);
                    if (!mAutoDateTimeButton.isChecked()) {
                        DateTimeSettings.setDate(this, mDatePicker.getYear(), mDatePicker.getMonth(),
                                mDatePicker.getDayOfMonth());
                        DateTimeSettings.setTime(this,
                                mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                    }
                }

                Intent intent = new Intent();
                intent.setClassName("com.aurora.setupwizard",
                        "com.aurora.setupwizard.AuroraInputMethodPickerActivity");
                startActivity(intent);
                break;
            }
            case R.id.iv_back: {
                onBackPressed();
            }*/
             // $FALL-THROUGH$
            /*case R.id.skip_button: {
                setResult(RESULT_OK);
                //finish();
                break;
            }*/
        }
        if (view == mBackButton) {
            mCallback.onNavigateBack();
        } else if (view == mNextButton) {
            mCallback.onNavigateNext();
        }
    }

    /**
	 * 跳到下一页
	 */
	public void goNext(){
		
		//是否指纹支持
		String fingerStr = SystemProperties.get("ro.gn.fingerprint.support",
				"no");
		if (fingerStr.equals("no")) {
			//是否有应用推荐的列表
			File file = new File("/data/apk_recommend/appdatas.json");
			boolean exists = file.exists();//根据此文件来判断是否跳到推荐页
			if(exists){
				//应用推荐页
				Intent intent = new Intent("com.aurora.setupwizard.apprecommend");
				startActivity(intent);
			}else{
				//应用完成页
				Intent intent = new Intent("com.aurora.setupwizard.complete");
				startActivity(intent);
			}
		} else {
			//指纹页
			Intent intent = new Intent("com.aurora.setupwizard.settingfingerprint");
			startActivity(intent);
		}
	}
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final boolean autoEnabled = isChecked;  // just for readibility.
        /*if (buttonView == mAutoTimeZoneButton) {
            // In XL screen, we save all the state only when the next button is pressed.
            if (!mUsingXLargeLayout) {
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.AUTO_TIME_ZONE,
                        isChecked ? 1 : 0);
            }
            mTimeZone.setEnabled(!autoEnabled);
            if (isChecked) {
                findViewById(R.id.current_time_zone).setVisibility(View.VISIBLE);
                findViewById(R.id.zone_picker).setVisibility(View.GONE);
            }
        } else */
        if (buttonView == mAutoDateTimeButton) {
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.AUTO_TIME,
                    isChecked ? 1 : 0);
            mTimePicker.setEnabled(!autoEnabled);
            mDatePicker.setEnabled(!autoEnabled);
        }
        if (autoEnabled) {
            final View focusedView = getCurrentFocus();
            if (focusedView != null) {
                mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final TimeZone tz = ZonePickerWizard.obtainTimeZoneFromItem(parent.getItemAtPosition(position));
        if (mUsingXLargeLayout) {
            mSelectedTimeZone = tz;
            final Calendar now = Calendar.getInstance(tz);
            if (mTimeZoneButton != null) {
                mTimeZoneButton.setText(tz.getDisplayName());
            }
            // mTimeZoneButton.setText(DateTimeSettings.getTimeZoneText(tz));
            mDatePicker.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH));
            mTimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(now.get(Calendar.MINUTE));
        } else {
            // in prefs mode, we actually change the setting right now, as opposed to waiting
            // until Next is pressed in xLarge mode
            final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setTimeZone(tz.getID());
            DateTimeSettings settingsFragment = (DateTimeSettings) getFragmentManager().
                    findFragmentById(R.id.date_time_settings_fragment);
            settingsFragment.updateTimeAndDateDisplay(this);
        }
        mTimeZonePopup.dismiss();
    }

    /**
     * If this is called, that means we're in prefs style portrait mode for a large display
     * and the user has tapped on the time zone preference. If we were a AuroraPreferenceActivity,
     * we'd then launch the timezone fragment in a new activity, but we aren't, and here
     * on a tablet display, we really want more of a popup picker look' like the one we use
     * for the xlarge version of this activity. So we just take this opportunity to launch that.
     * <p/>
     * TODO: For phones, we might want to change this to do the "normal" opening
     * of the ZonePickerWizard fragment in its own activity. Or we might end up just
     * creating a separate DateTimeSettingsSetupWizardPhone activity that subclasses
     * AuroraPreferenceActivity in the first place to handle all that automatically.
     */
    @Override
    public boolean onPreferenceStartFragment(AuroraPreferenceFragment caller, AuroraPreference pref) {
        //showTimezonePicker(R.id.timezone_dropdown_anchor);
        Intent intent = new Intent(this, ZonePickerActivity.class);
        startActivity(intent);
        return true;
    }

    private void showTimezonePicker(int anchorViewId) {
        View anchorView = findViewById(anchorViewId);
        if (anchorView == null) {
            Log.e(TAG, "Unable to find zone picker anchor view " + anchorViewId);
            return;
        }
        mTimeZonePopup = new ListPopupWindow(this, null);
        mTimeZonePopup.setWidth(anchorView.getWidth());
        mTimeZonePopup.setAnchorView(anchorView);
        mTimeZonePopup.setAdapter(mTimeZoneAdapter);
        mTimeZonePopup.setOnItemClickListener(this);
        mTimeZonePopup.setModal(true);
        mTimeZonePopup.show();
    }

    private boolean isAutoDateTimeEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME) > 0;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    /*
    private boolean isAutoTimeZoneEnabled() {
        try {
            return Settings.Global.getInt(getContentResolver(),
                    Settings.Global.AUTO_TIME_ZONE) > 0;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }*/

    private void updateTimeAndDateDisplay() {
        if (!mUsingXLargeLayout) {
            return;
        }
        final Calendar now = Calendar.getInstance();
        mTimeZoneButton.setText(now.getTimeZone().getDisplayName());
        mDatePicker.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        mTimePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(now.get(Calendar.MINUTE));
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeAndDateDisplay();
        }
    };

    @Override
    public void onNavigationBarCreated(SetupWizardNavBar bar) {

    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (mSelectedTimeZone != null) {
            final TimeZone systemTimeZone = TimeZone.getDefault();
            if (!systemTimeZone.equals(mSelectedTimeZone)) {
                Log.i(TAG, "Another TimeZone is selected by a user. Changing system TimeZone.");
                final AlarmManager alarm = (AlarmManager)
                        getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(mSelectedTimeZone.getID());
            }
        }
        if (mAutoDateTimeButton != null) {
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME,
                    mAutoDateTimeButton.isChecked() ? 1 : 0);
            if (!mAutoDateTimeButton.isChecked()) {
                DateTimeSettings.setDate(this, mDatePicker.getYear(), mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth());
                DateTimeSettings.setTime(this,
                        mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
            }
        }

//        Intent intent = new Intent();
//        intent.setClassName("com.aurora.setupwizard",
//                "com.aurora.setupwizard.AuroraInputMethodPickerActivity");
//        startActivity(intent);
        goNext();
    }
    

}
