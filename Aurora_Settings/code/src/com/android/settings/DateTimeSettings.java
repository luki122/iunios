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
    

import aurora.app.AuroraActivity;
import android.app.Activity;
import aurora.app.AuroraAlertDialog;
import android.app.AlertDialog;
import aurora.app.AuroraDatePickerDialog;
import android.app.Dialog;
import aurora.app.AuroraTimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraTimePicker;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import aurora.widget.AuroraSwitch;
import aurora.widget.AuroraActionBar;
import libcore.icu.LocaleData;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeSettings extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener,
                AuroraTimePickerDialog.OnTimeSetListener, AuroraDatePickerDialog.OnDateSetListener {

    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    // Used for showing the current date format, which looks like "12/31/2010", "2010/12/13", etc.
    // The date value is dummy (independent of actual date).
    private Calendar mDummyDate;

    private static final String KEY_DATE_FORMAT = "date_format";
    private static final String KEY_AUTO_TIME = "auto_time";
    private static final String KEY_AUTO_TIME_ZONE = "auto_zone";

    private static final int DIALOG_DATEPICKER = 0;
    private static final int DIALOG_TIMEPICKER = 1;

    // have we been launched from the setup wizard?
    protected static final String EXTRA_IS_FIRST_RUN = "firstRun";

    private AuroraSwitchPreference mAutoTimePref;
    private AuroraPreference mTimePref;
    private AuroraDateTimeSwitchPreference mTime24Pref;
    private AuroraSwitchPreference mAutoTimeZonePref;
    private AuroraPreference mTimeZone;
    private AuroraPreference mDatePref;
    private AuroraListPreference mDateFormat;
    // qy add 
    public static final String ACTION_UPDATE_DATETIME = "com.android.settings.action.update.datetime";
    
    private final BroadcastReceiver mDateTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE_DATETIME)) {
            	updateTimeAndDateDisplay(getActivity());
                timeUpdated();
      
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.date_time_prefs);
        

        initUI();
    }

    private void initUI() {
      

        Intent intent = getActivity().getIntent();

        boolean isFirstRun = intent.getBooleanExtra(EXTRA_IS_FIRST_RUN, false);
        
        
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
        
        mDummyDate = Calendar.getInstance();

        mAutoTimePref = (AuroraSwitchPreference) findPreference(KEY_AUTO_TIME); 
        
        mAutoTimePref.setChecked(autoTimeEnabled);
        mAutoTimeZonePref = (AuroraSwitchPreference) findPreference(KEY_AUTO_TIME_ZONE);
        mAutoTimeZonePref.setPersistent(true);
        // Override auto-timezone if it's a wifi-only device or if we're still in setup wizard.
        // TODO: Remove the wifiOnly test when auto-timezone is implemented based on wifi-location.
        if (Utils.isWifiOnly(getActivity()) || isFirstRun) {
            getPreferenceScreen().removePreference(mAutoTimeZonePref);
            autoTimeZoneEnabled = false;
        }
        mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);

        mTimePref = findPreference("time");
        mTime24Pref = (AuroraDateTimeSwitchPreference)findPreference("24_hour");     

        		
        mTimeZone = findPreference("timezone");
        mDatePref = findPreference("date");
        mDateFormat = (AuroraListPreference) findPreference(KEY_DATE_FORMAT);
        if (isFirstRun) {
            getPreferenceScreen().removePreference(mTime24Pref);
            getPreferenceScreen().removePreference(mDateFormat);
        }

        
       /* String [] dateFormats = getResources().getStringArray(R.array.date_format_values);
        String [] formattedDates = new String[dateFormats.length];
        String currentFormat = getDateFormat();
     
        
      //qy modify start
        // Prevents duplicated values on date format selector.
        mDummyDate.set(mDummyDate.get(Calendar.YEAR), mDummyDate.get(Calendar.MONTH), mDummyDate.get(Calendar.DAY_OF_MONTH), 13, 0, 0);
        
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(getActivity());
        String tempFormat =  shortDateFormat.format(mDummyDate.getTime());
        
        for (int i = 0; i < formattedDates.length; i++) {
            String formatted =
                    DateFormat.getDateFormatForSetting(getActivity(), dateFormats[i])
                    .format(mDummyDate.getTime());

         // Initialize if DATE_FORMAT is not set in the system settings
            // This can happen after a factory reset (or data wipe)
            if (currentFormat == null && tempFormat.equals(formatted)) {
            	currentFormat= dateFormats[i];
             
            }
            

            if (dateFormats[i].length() == 0) {
                formattedDates[i] = getResources().
                    getString(R.string.normal_date_format, formatted);
            } else {
                formattedDates[i] = formatted;
            }
        }
        
     

 *    
 *         int year = mDummyDate.get(Calendar.YEAR); 
        int month = mDummyDate.get(Calendar.MONTH)+1; 
        int day = mDummyDate.get(Calendar.DAY_OF_MONTH);
        
        formattedDates[1] = month+"月"+day+"日"+year+"年";
        formattedDates[2] = day+"日"+month+"月"+year+"年";
        formattedDates[3] = year+"年"+month+"月"+day+"日";
//        String dateTemp = year+"年"+month+"月"+day+"日";
        String dateFormat = mDateFormat.getValue();
        if (dateFormat.equals("MM-dd-yyyy")){
        	formattedDates[0] = "所在区域（"+ month+"月"+day+"日"+year+"年）";
        }else if (dateFormat.equals("dd-MM-yyyy")){
        	formattedDates[0] = "所在区域（"+day+"日"+month+"月"+year+"年）";
        }else if (dateFormat.equals("yyyy-MM-dd")){
        	formattedDates[0] = "所在区域（"+ year+"年"+month+"月"+day+"日）";
        }else{
        	formattedDates[0] = "所在区域（"+year+"年"+month+"月"+day+"日）";
        }
      //qy modify end
 
        	
  
        
        
        mDateFormat.setEntries(formattedDates);
        mDateFormat.setEntryValues(R.array.date_format_values);
        mDateFormat.setValue(currentFormat);*/
        
        updateDateFormat();

        mTimePref.setEnabled(!autoTimeEnabled);
        mDatePref.setEnabled(!autoTimeEnabled);
        mTimeZone.setEnabled(!autoTimeZoneEnabled);
        
        // qy add 
        mTimePref.setSelectable(!autoTimeEnabled);
        mDatePref.setSelectable(!autoTimeEnabled);
        mTimeZone.setSelectable(!autoTimeZoneEnabled); //end
    }
    
    /*
     *   更新日期格式
     */
    public void updateDateFormat(){
    	 String [] dateFormats = getResources().getStringArray(R.array.date_format_values);
         String [] formattedDates = new String[dateFormats.length];
         String currentFormat = getDateFormat();

         mDummyDate.set(mDummyDate.get(Calendar.YEAR), mDummyDate.get(Calendar.MONTH), mDummyDate.get(Calendar.DAY_OF_MONTH), 13, 0, 0);
         
        // java.text.DateFormat shortDateFormat = DateFormat.getDateFormatForSetting(getActivity(),"yyyy-MM-dd");
         java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(getActivity());
         String tempFormat =  shortDateFormat.format(mDummyDate.getTime());
         
         for (int i = 0; i < formattedDates.length; i++) {
             String formatted =
                     DateFormat.getDateFormatForSetting(getActivity(), dateFormats[i])
                     .format(mDummyDate.getTime());

             if (currentFormat == null && tempFormat.equals(formatted)) {
             	currentFormat= dateFormats[i];
             }
             

             if (dateFormats[i].length() == 0) {
                 formattedDates[i] = getResources().
                     getString(R.string.normal_date_format, formatted);
             } else {
                 formattedDates[i] = formatted;
             }
         }

         mDateFormat.setEntries(formattedDates);
         mDateFormat.setEntryValues(R.array.date_format_values);
         mDateFormat.setValue(currentFormat);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        mTime24Pref.setChecked(is24Hour());

        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, filter, null, null);
         // qy add 
        IntentFilter dateTimeFilter = new IntentFilter(ACTION_UPDATE_DATETIME);
        getActivity().registerReceiver(mDateTimeReceiver, dateTimeFilter);  // end 

        updateTimeAndDateDisplay(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
        getActivity().unregisterReceiver(mDateTimeReceiver);
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updateTimeAndDateDisplay(Context context) {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(context);
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        // We use December 31st because it's unambiguous when demonstrating the date format.
        // We use 13:00 so we can demonstrate the 12/24 hour options.
        mDummyDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 13, 0, 0);
        Date dummyDate = mDummyDate.getTime();
        mTimeZone.setSummary(getTimeZoneText(now.getTimeZone()));
        
        //qy modify start
        /*mTimePref.setSummary(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
      
        mDatePref.setSummary(shortDateFormat.format(now.getTime()));
        mDateFormat.setSummary(shortDateFormat.format(dummyDate));
        mTime24Pref.setSummary(DateFormat.getTimeFormat(getActivity()).format(dummyDate));
        int year = now.get(Calendar.YEAR); 
        int month = now.get(Calendar.MONTH)+1; 
        int day = now.get(Calendar.DAY_OF_MONTH);*/
//        mTimeZone.auroraSetArrowText(getTimeZoneText(now.getTimeZone()));
        
     // deal with the AM and PM        
        if(is24Hour()){
//        	 mTimePref.auroraSetArrowText(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
        	mTimePref.auroraSetArrowText(DateFormat.format("kk:mm", now.getTime()).toString(),true);
        }else{
        	LocaleData localeData = LocaleData.get(Locale.getDefault());
            Calendar cc = new GregorianCalendar();
            cc.setTime(now.getTime());
            String replacement;
            replacement = localeData.amPm[cc.get(Calendar.AM_PM) - Calendar.AM];
            
            String total = DateFormat.format("hh:mm ", cc.getTime()).toString(); 
            if(replacement.equals("上午")){
            	replacement = "AM";
            }else if(replacement.equals("下午")){
            	replacement = "PM";
            }           
      
            
            mTimePref.auroraSetArrowText(total+ replacement,true);
        }

        mDatePref.auroraSetArrowText(shortDateFormat.format(now.getTime()),true);
        mDateFormat.auroraSetArrowText(shortDateFormat.format(now.getTime()),true);
        updateDateFormat();
       
}

    @Override
    public void onDateSet(AuroraDatePicker view, int year, int month, int day) {
        setDate(year, month, day);
        final Activity activity = getActivity();
        if (activity != null) {
            updateTimeAndDateDisplay(activity);
        }
    }

    @Override
    public void onTimeSet(AuroraTimePicker view, int hourOfDay, int minute) {
        setTime(hourOfDay, minute);
        final Activity activity = getActivity();
        if (activity != null) {
            updateTimeAndDateDisplay(activity);
        }

        // We don't need to call timeUpdated() here because the TIME_CHANGED
        // broadcast is sent by the AlarmManager as a side effect of setting the
        // SystemClock time.
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(KEY_DATE_FORMAT)) {
            String format = preferences.getString(key,
                    getResources().getString(R.string.default_date_format));
            Settings.System.putString(getContentResolver(),
                    Settings.System.DATE_FORMAT, format);
            updateTimeAndDateDisplay(getActivity());
        } else if (key.equals(KEY_AUTO_TIME)) {
            boolean autoEnabled = preferences.getBoolean(key, true);
            Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME,
                    autoEnabled ? 1 : 0);
            mTimePref.setEnabled(!autoEnabled);
            mDatePref.setEnabled(!autoEnabled);
            
            //qy add 
            mTimePref.setSelectable(!autoEnabled);
            mDatePref.setSelectable(!autoEnabled);
            
        } else if (key.equals(KEY_AUTO_TIME_ZONE)) {
            boolean autoZoneEnabled = preferences.getBoolean(key, true);
            Settings.Global.putInt(
                    getContentResolver(), Settings.Global.AUTO_TIME_ZONE, autoZoneEnabled ? 1 : 0);            
            
            mTimeZone.setEnabled(!autoZoneEnabled);
            mTimeZone.setSelectable(!autoZoneEnabled);
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        Dialog d;        
        switch (id) {
        case DIALOG_DATEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            d = new AuroraDatePickerDialog(
                getActivity(),
              //  AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, // Gionee: huangsf 20121210 add for CR00741405
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
            break;
        }
        case DIALOG_TIMEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            d = new AuroraTimePickerDialog(
                    getActivity(),
               //     AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, // Gionee: huangsf 20121210 add for CR00741405
                    this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity()));
            		
            break;
        }
        default:
            d = null;
            break;
        }

        return d;
    }

    /*
    @Override
    public void onPrepareDialog(int id, Dialog d) {
        switch (id) {
        case DIALOG_DATEPICKER: {
            AuroraDatePickerDialog datePicker = (AuroraDatePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            datePicker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            break;
        }
        case DIALOG_TIMEPICKER: {
            AuroraTimePickerDialog timePicker = (AuroraTimePickerDialog)d;
            final Calendar calendar = Calendar.getInstance();
            timePicker.updateTime(
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));
            break;
        }
        default:
            break;
        }
    }
    */
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference == mDatePref) {
            showDialog(DIALOG_DATEPICKER);
        } else if (preference == mTimePref) {
            // The 24-hour mode may have changed, so recreate the dialog
            removeDialog(DIALOG_TIMEPICKER);
            showDialog(DIALOG_TIMEPICKER);
        } //else if (preference == mTime24Pref) {
//            set24Hour(mTime24Pref.isChecked());
//            updateTimeAndDateDisplay(getActivity());
//            timeUpdated();
//        }  //qy modify
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }

    private void timeUpdated() {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        getActivity().sendBroadcast(timeChanged);
    }

    /*  Get & Set values from the system settings  */

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour? HOURS_24 : HOURS_12);
    }

    private String getDateFormat() {
        return Settings.System.getString(getContentResolver(),
                Settings.System.DATE_FORMAT);
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getContentResolver(), name) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }

    /* package */ static void setDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /* package */ static void setTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
    }

    /*  Helper routines to format timezone */

    /* package */ static String getTimeZoneText(TimeZone tz) {
        // Similar to new SimpleDateFormat("'GMT'Z, zzzz").format(new Date()), but
        // we want "GMT-03:00" rather than "GMT-0300".
        Date now = new Date();
        return formatOffset(new StringBuilder(), tz, now).
            append(", ").
            append(tz.getDisplayName(tz.inDaylightTime(now), TimeZone.LONG)).toString();
    }

    private static StringBuilder formatOffset(StringBuilder sb, TimeZone tz, Date d) {
        int off = tz.getOffset(d.getTime()) / 1000 / 60;

        sb.append("GMT");
        if (off < 0) {
            sb.append('-');
            off = -off;
        } else {
            sb.append('+');
        }

        int hours = off / 60;
        int minutes = off % 60;

        sb.append((char) ('0' + hours / 10));
        sb.append((char) ('0' + hours % 10));

        sb.append(':');

        sb.append((char) ('0' + minutes / 10));
        sb.append((char) ('0' + minutes % 10));

        return sb;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                updateTimeAndDateDisplay(activity);
            }
        }
    };
}
