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

package com.android.deskclock;

import com.android.deskclock.R;
import com.android.deskclock.RepeatListPopupWindow.OnButtonListClickListener;
import com.android.deskclock.RepeatPopupWindow.OnButtonClickListener;
import com.aurora.utils.GnRingtoneUtil;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraTimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase;
import android.widget.TextView;
import aurora.widget.AuroraTimePicker;
import android.widget.Toast;
//Gionee baorui 2012-09-22 modify for CR00683095 begin
import android.text.Editable;
import android.text.TextWatcher;
//Gionee baorui 2012-09-22 modify for CR00683095 end
//Gionee baorui 2012-11-20 modify for CR00733082 begin
import android.widget.CompoundButton;
import android.view.MenuItem;
//Gionee baorui 2012-11-20 modify for CR00733082 end
//Gionee baorui 2013-01-15 modify for CR00762851 begin
import aurora.preference.AuroraPreferenceGroup;
import android.widget.LinearLayout;
//Gionee baorui 2013-01-15 modify for CR00762851 end
// Gionee <baorui><2013-03-25> modify for CR00788702 begin

import android.media.RingtoneManager;
import android.provider.Settings;

/**
 * Manages each alarm
 */
public class SetAlarm extends AuroraPreferenceActivity implements AuroraPreference.OnPreferenceChangeListener,
        AuroraTimePickerDialog.OnTimeSetListener, OnCancelListener {
    private static final String KEY_CURRENT_ALARM = "currentAlarm";
    private static final String KEY_ORIGINAL_ALARM = "originalAlarm";
    private static final String KEY_TIME_PICKER_BUNDLE = "timePickerBundle";

    private AuroraEditText mLabel;

    // private AuroraCheckBoxPreference mEnabledPref;

    private AuroraPreference mTimePref;
    private AlarmPreference mAlarmPref;

    // private AuroraCheckBoxPreference mVibratePref;
    private GnVibratePreference mVibratePref;

    //private RepeatPreference mRepeatPref;
    private AuroraPreference mRepeatPref;

    private int     mId;
    private int     mHour;
    private int     mMinute;
    private AuroraTimePickerDialog mTimePickerDialog;
    private Alarm   mOriginalAlarm;
    
	private static final int MAX_INPUT_LENGTH = 60;
	private Vibrator v;
	
	private Alarm.DaysOfWeek mSetDaysOfWeek = new Alarm.DaysOfWeek(0);
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        AuroraActionBar actionBar = getAuroraActionBar();
        
		//setAuroraMenuCallBack(auroraMenuCallBack);
		//setAuroraMenuItems(R.menu.aurora_deskclockrepeatmenu);
        
        actionBar.goToActionBarSelectView();
        TextView righttext = (TextView)actionBar.getSelectRightButton();
        righttext.setText(R.string.sure);
        actionBar.getSelectLeftButton().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                revert();
                finish();
			}
		});
        righttext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 long time = saveAlarm(null);
	             popAlarmSetToast(SetAlarm.this, time);
	             finish();
			}
		});

        addPreferencesFromResource(R.xml.alarm_prefs);
        
        mTimePref = findPreference("time");
        mAlarmPref = (AlarmPreference) findPreference("alarm");
		mAlarmPref.setWidgetLayoutResource(R.layout.gn_pref_image_white);
        mAlarmPref.setOnPreferenceChangeListener(this);

        // mVibratePref = (AuroraCheckBoxPreference) findPreference("vibrate");
        // mVibratePref.setOnPreferenceChangeListener(this);
        mVibratePref = (GnVibratePreference) findPreference("vibrate");
        mVibratePref.setSwitchClickListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (mVibratePref.isSwitchOn()) {
                    mVibratePref.setSwitchOn(false);
                } else {
                    mVibratePref.setSwitchOn(true);
                }

                saveAlarm(null);

            }
        });
        
        //aurora mod by tangjun 2013.12.23 start
        /*
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            // getPreferenceScreen().removePreference(mVibratePref);
            AuroraPreferenceGroup mPrefGroup = ((AuroraPreferenceGroup) findPreference("favorite"));
            mPrefGroup.removePreference(mVibratePref);
        }
        */
        //把振动和铃声选择都按策划要求移除,不给用户做选择.
        AuroraPreferenceGroup mPrefGroup = ((AuroraPreferenceGroup) findPreference("favorite"));
        mPrefGroup.removePreference(mVibratePref);
        mPrefGroup.removePreference(mAlarmPref);
        
        //aurora mod by tangjun 2013.12.23 end
        
        mRepeatPref = (AuroraPreference) findPreference("setRepeat");
        mRepeatPref.setOnPreferenceChangeListener(this);
        mRepeatPref.setWidgetLayoutResource(R.layout.gn_pref_image_white);

        Intent i = getIntent();
        Alarm alarm = i.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        
        if (alarm == null) {
            // No alarm means create a new alarm.
            alarm = new Alarm();
            
            //aurora add by tangjun 2013.12.23 start
            alarm.vibrate =  AuroraPreferenceManager.getDefaultSharedPreferences(this).getBoolean("default_vibrate", true);
            //aurora add by tangjun 2013.12.23 end
            Log.e("---alarm.vibrate--- = " + alarm.vibrate);
            
            actionBar.setTitle(R.string.add_alarm);
        }
        mOriginalAlarm = alarm;

        // Populate the prefs with the original alarm data.  updatePrefs also
        // sets mId so it must be called before checking mId below.
        updatePrefs(mOriginalAlarm);

        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
        getListView().setItemsCanFocus(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ORIGINAL_ALARM, mOriginalAlarm);
        outState.putParcelable(KEY_CURRENT_ALARM, buildAlarmFromUi());
        if (mTimePickerDialog != null) {
            if (mTimePickerDialog.isShowing()) {
                outState.putParcelable(KEY_TIME_PICKER_BUNDLE, mTimePickerDialog
                        .onSaveInstanceState());
                mTimePickerDialog.dismiss();
            }
            mTimePickerDialog = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        Alarm alarmFromBundle = state.getParcelable(KEY_ORIGINAL_ALARM);
        if (alarmFromBundle != null) {
            mOriginalAlarm = alarmFromBundle;
        }

        alarmFromBundle = state.getParcelable(KEY_CURRENT_ALARM);
        if (alarmFromBundle != null) {
            updatePrefs(alarmFromBundle);
        }

        Bundle b = state.getParcelable(KEY_TIME_PICKER_BUNDLE);
        if (b != null) {
            showTimePicker();
            mTimePickerDialog.onRestoreInstanceState(b);
        }
    }

    // Used to post runnables asynchronously.
    private static final Handler sHandler = new Handler();

    public boolean onPreferenceChange(final AuroraPreference p, Object newValue) {
        // Asynchronously save the alarm since this method is called _before_
        // the value of the preference has changed.
        sHandler.post(new Runnable() {
            public void run() {
                // Editing any preference (except enable) enables the alarm.

                /*
                if (p != mEnabledPref) {
                    mEnabledPref.setChecked(true);
                }
                */

                saveAlarm(null);
            }
        });
        return true;
    }

    
    @Override
    protected void onDestroy() {

    	super.onDestroy();
    }
    private void updatePrefs(Alarm alarm) {
        mId = alarm.id;

        // mEnabledPref.setChecked(alarm.enabled);

        mHour = alarm.hour;
        mMinute = alarm.minutes;
        mSetDaysOfWeek = alarm.daysOfWeek;
        //mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
        
        //aurora add by tangjun
        mRepeatPref.setSummary(alarm.daysOfWeek.toString(this, true));

        // mVibratePref.setChecked(alarm.vibrate);
        mVibratePref.setSwitchOn(alarm.vibrate);

        if (alarm.alert != null && !RingtoneManager.isDefault(alarm.alert)
                && !GnRingtoneUtil.isRingtoneExist(alarm.alert, getContentResolver())) {
            // alarm.alert = Settings.System.DEFAULT_ALARM_ALERT_URI;
            String mData = Alarms.getAlertInfoStr(this, alarm.id);
            int mVolumes = Alarms.getVolumes(this, alarm.id);
            if (Alarms.isUpdateRintoneUri(mData, alarm.alert, this, mVolumes)) {
                alarm.alert = Alarms.updateRintoneUri(mData, alarm.alert, this, mVolumes);
            } else {
                alarm.alert = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }

            saveAlarm(alarm);
        }
        mAlarmPref.setAlert(alarm.alert);
        updateTime();
    }
    
    /**
     * @param position PopupWindow按钮响应
     */
    private void buttonClickRespond(int position) {
		Alarm.DaysOfWeek setDaysOfWeek;
		switch (position) {
		case 0:
			setDaysOfWeek = new Alarm.DaysOfWeek(0);
			mSetDaysOfWeek.set(setDaysOfWeek);
			mRepeatPref.setSummary(mSetDaysOfWeek.toString(SetAlarm.this, true));
			break;
		case 1:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x7f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			mRepeatPref.setSummary(mSetDaysOfWeek.toString(SetAlarm.this, true));
			break;	
		case 2:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x1f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			mRepeatPref.setSummary(mSetDaysOfWeek.toString(SetAlarm.this, true));
			break;
		case 3:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x9f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			mRepeatPref.setSummary(mSetDaysOfWeek.toString(SetAlarm.this, true));
			break;
		case 4:

			final RepeatPopupWindow repeat = new RepeatPopupWindow(SetAlarm.this, R.layout.repeatcheck);
			repeat.showRepeatPopupWindow();
			repeat.setOnButtonClickListener(new OnButtonClickListener() {
				
				@Override
				public void onSureClick() {
					// TODO Auto-generated method stub
					Alarm.DaysOfWeek setDaysOfWeek = new Alarm.DaysOfWeek(repeat.getSelectDays());
					mSetDaysOfWeek.set(setDaysOfWeek);
					mRepeatPref.setSummary(mSetDaysOfWeek.toString(SetAlarm.this, true));
				}
				
				@Override
				public void onCancelClick() {
					// TODO Auto-generated method stub
				}
			});
			break;
		default:
			break;
		}
    }
    
    /**
     * 显示周期选择列表
     */
    private void showRepeatListPopupWindow ( ) {
		final RepeatListPopupWindow repeat = new RepeatListPopupWindow(SetAlarm.this, R.layout.repeatlistcheck);
		repeat.showRepeatListPopupWindow();
		repeat.setOnButtonListClickListener(new OnButtonListClickListener() {

			@Override
			public void onButtonListClick(int position) {
				// TODO Auto-generated method stub
				buttonClickRespond(position);
			}
			
		});
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
            AuroraPreference preference) {
        if (preference == mTimePref) {
            showTimePicker();
        }

        if (preference == mVibratePref) {
            if (mVibratePref.isSwitchOn()) {
                mVibratePref.setSwitchOn(false);
            } else {
                mVibratePref.setSwitchOn(true);
            }

            saveAlarm(null);

        }
        
        if ( preference == mRepeatPref ) {
        	Log.e("preference == mRepeatPref");
        	//showAuroraMenu();
        	showRepeatListPopupWindow();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onBackPressed() {
        // revert();
        long time = saveAlarm(null);
        popAlarmSetToast(SetAlarm.this, time);
        finish();
    }

    private void showTimePicker() {
        if (mTimePickerDialog != null) {
            if (mTimePickerDialog.isShowing()) {
                Log.e("mTimePickerDialog is already showing.");
                mTimePickerDialog.dismiss();
            } else {
                Log.e("mTimePickerDialog is not null");
            }

            // mTimePickerDialog.dismiss();

            /*
            if (mTimePickerDialog != null) {
                mTimePickerDialog.dismiss();
            }
            */
            if (!isFinishing() && mTimePickerDialog != null) {
                mTimePickerDialog.dismiss();
            }
        }

        /*
        mTimePickerDialog = new AuroraTimePickerDialog(this, this, mHour, mMinute,
                DateFormat.is24HourFormat(this));
        */
        mTimePickerDialog = new AuroraTimePickerDialog(this, this,
                mHour, mMinute, DateFormat.is24HourFormat(this));

        mTimePickerDialog.setOnCancelListener(this);
        mTimePickerDialog.show();
    }

    public void onTimeSet(AuroraTimePicker view, int hourOfDay, int minute) {
        // onTimeSet is called when the user clicks "Set"
        mTimePickerDialog = null;
        mHour = hourOfDay;
        mMinute = minute;
        updateTime();
        // If the time has been changed, enable the alarm.
        // mEnabledPref.setChecked(true);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mTimePickerDialog = null;
    }

    private void updateTime() {
//        mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinute,
//                mRepeatPref.getDaysOfWeek()));
    	mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinute,
    			mSetDaysOfWeek));
    }

    private long saveAlarm(Alarm alarm) {
        if (alarm == null) {
            alarm = buildAlarmFromUi();
        }

        long time;
        if (alarm.id == -1) {
            time = Alarms.addAlarm(this, alarm);
            // addAlarm populates the alarm with the new id. Update mId so that
            // changes to other preferences update the new alarm.
            mId = alarm.id;
        } else {
            time = Alarms.setAlarm(this, alarm);
        }
        return time;
    }

    private Alarm buildAlarmFromUi() {
        Alarm alarm = new Alarm();
        alarm.id = mId;
        // alarm.enabled = mEnabledPref.isChecked();
        alarm.enabled = true;
        alarm.hour = mHour;
        alarm.minutes = mMinute;
        //alarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
        alarm.daysOfWeek = mSetDaysOfWeek;
        // alarm.vibrate = mVibratePref.isChecked();
        alarm.vibrate = mVibratePref.isSwitchOn();
        
        //alarm.label = mLabel.getText().toString();
        alarm.alert = mAlarmPref.getAlert();
        return alarm;
    }

    private void deleteAlarm() {
    	//android jiating 20120608 change dialog info begin

        new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                  .setTitle(getString(R.string.android_delete_alarm))
                   .setMessage(getString(R.string.isDelete_the_alarm))
                   //android jiating 20120608 change dialog info begin
                  .setPositiveButton(android.R.string.ok,
                  
         
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                Alarms.deleteAlarm(SetAlarm.this, mId);
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void revert() {
        int newId = mId;
        // "Revert" on a newly created alarm should delete it.
        if (mOriginalAlarm.id == -1) {
            Alarms.deleteAlarm(SetAlarm.this, newId);
        } else {
            saveAlarm(mOriginalAlarm);
        }
    }

    /**
     * Display a toast that tells the user how long until the alarm
     * goes off.  This helps prevent "am/pm" mistakes.
     */
    static void popAlarmSetToast(Context context, int hour, int minute,
                                 Alarm.DaysOfWeek daysOfWeek) {
        popAlarmSetToast(context,
                Alarms.calculateAlarm(hour, minute, daysOfWeek)
                .getTimeInMillis());
    }

    static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from
     * now"
     */
    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                    (dispHour ? 2 : 0) |
                    (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }
    
    private void vibrate() {
        if (v.hasVibrator()) {
            v.vibrate(new long[] { 100, 100 }, -1);
        } else {
            Log.e("Device not have vibrator");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mAlarmPref.setAlert(mAlarmPref.getAlert());
        super.onResume();
    }
}

