package com.gionee.calendar.setting;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarRecentSuggestionsProvider;
import com.android.calendar.TimezoneAdapter;
import com.android.calendar.Utils;
import com.android.calendar.alerts.AlertReceiver;
import com.mediatek.calendar.MTKToast;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraRingtonePreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.provider.CalendarContract;
import android.provider.SearchRecentSuggestions;
import android.provider.CalendarContract.CalendarCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.calendar.R;
import com.gionee.calendar.statistics.Statistics;
//Gionee <jiating> <2013-05-06> modify for CR00000000 begin
public class GNCalendarReminderSettingFragment extends AuroraPreferenceFragment implements
OnSharedPreferenceChangeListener, OnPreferenceChangeListener{
	
	static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";

 


    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS = "preferences_alerts";
    public static final String KEY_ALERTS_VIBRATE = "preferences_alerts_vibrate";
    public static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";
    ///M:Add a new key to save vibrate setting for calendar event reminder.
    public static final String KEY_VIBRATE_FOR_EVENT_REMINDER = "preferences_vibrate_forEventReminder";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_POPUP = "preferences_alerts_popup";

    public static final String KEY_SHOW_CONTROLS = "preferences_show_controls";

    public static final String KEY_DEFAULT_REMINDER = "preferences_default_reminder";
    public static final int NO_REMINDER = -1;
    public static final String NO_REMINDER_STRING = "-1";
    public static final int REMINDER_DEFAULT_TIME = 10; // in minutes

    public static final String KEY_DEFAULT_CELL_HEIGHT = "preferences_default_cell_height";
    public static final String KEY_VERSION = "preferences_version";

    /** Key to SharePreference for default view (CalendarController.ViewType) */
    public static final String KEY_START_VIEW = "preferred_startView";
    /**
     *  Key to SharePreference for default detail view (CalendarController.ViewType)
     *  Typically used by widget
     */
    public static final String KEY_DETAILED_VIEW = "preferred_detailedView";
    public static final String KEY_DEFAULT_CALENDAR = "preference_defaultCalendar";

    // These must be in sync with the array preferences_week_start_day_values
    public static final String WEEK_START_DEFAULT = "-1";
    public static final String WEEK_START_SATURDAY = "7";
    public static final String WEEK_START_SUNDAY = "1";
    public static final String WEEK_START_MONDAY = "2";

    // These keys are kept to enable migrating users from previous versions
    private static final String KEY_ALERTS_TYPE = "preferences_alerts_type";
    private static final String ALERT_TYPE_ALERTS = "0";
    private static final String ALERT_TYPE_STATUS_BAR = "1";
    private static final String ALERT_TYPE_OFF = "2";


    AuroraCheckBoxPreference mAlert;
    ///M:Change the type of mVibrateWhen from AuroraListPreference to AuroraCheckBoxPreference.
    AuroraCheckBoxPreference mVibrateWhen;
    AuroraRingtonePreference mRingtone;
    AuroraCheckBoxPreference mPopup;

    AuroraListPreference mDefaultReminder;
    

    ///@}
    /** Return a properly configured SharedPreferences instance */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Set the default shared preferences in the proper context */
    public static void setDefaultValues(Context context) {
        AuroraPreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME, Context.MODE_PRIVATE,
                R.xml.gn_reminder_general_preferences, false);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final AuroraActivity activity = (AuroraActivity)getActivity();

        // Make sure to always use the same preferences file regardless of the package name
        // we're running under
        final AuroraPreferenceManager preferenceManager = getPreferenceManager();
        final SharedPreferences sharedPreferences = getSharedPreferences(activity);
        preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.gn_reminder_general_preferences);

        final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
        mAlert = (AuroraCheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS);
        //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item begin
//        mVibrateWhen = (AuroraCheckBoxPreference) preferenceScreen.findPreference(KEY_VIBRATE_FOR_EVENT_REMINDER);
     
//        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
//        if (vibrator == null || !vibrator.hasVibrator()) {
//            AuroraPreferenceCategory mAlertGroup = (AuroraPreferenceCategory) preferenceScreen
//                    .findPreference(KEY_ALERTS_CATEGORY);
//            mAlertGroup.removePreference(mVibrateWhen);
//        }
        //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item end
        mRingtone = (AuroraRingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
    	Log.i("jiating","ReminderPrefreenceonCreate..mRingtone="+(mRingtone==null));
        mPopup = (AuroraCheckBoxPreference) preferenceScreen.findPreference(KEY_ALERTS_POPUP);
       
        mDefaultReminder = (AuroraListPreference) preferenceScreen.findPreference(KEY_DEFAULT_REMINDER);
     

       
        mDefaultReminder.setSummary(mDefaultReminder.getEntry());

       

        migrateOldPreferences(sharedPreferences);

        updateChildPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        setPreferenceListeners(this);
		Statistics.onResume(this.getActivity());
    }

    /**
     * Sets up all the preference change listeners to use the specified
     * listener.
     */
    private void setPreferenceListeners(OnPreferenceChangeListener listener) {
      
        mDefaultReminder.setOnPreferenceChangeListener(listener);
        mRingtone.setOnPreferenceChangeListener(listener);

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAlert.isChecked()){
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_NOTIFICATIONS_ON);
        }else{
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_NOTIFICATIONS_OFF);
        }
        if(mPopup.isChecked()){
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_NOTIFICATION_ON);
        }else{
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_NOTIFICATION_OFF);
        }
        getSelectReminder();
      	getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        setPreferenceListeners(null);
		Statistics.onPause(this.getActivity());
    }

	private void getSelectReminder() {
		Resources res = getResources();
		String[] remindValues = res
				.getStringArray(R.array.preferences_default_reminder_values);
		Log.v("wpeng","GNCalendarReminderSettingFragment---onPause---mDefaultReminder == "
				+ mDefaultReminder.getValue());
		try {
			if (remindValues[0].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_NONE);
			} else if (remindValues[1].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_0);
			} else if (remindValues[2].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1);
			} else if (remindValues[3].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_5);
			} else if (remindValues[4].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_10);
			} else if (remindValues[5].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_15);
			} else if (remindValues[6].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_20);
			} else if (remindValues[7].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_25);
			} else if (remindValues[8].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_30);
			} else if (remindValues[9].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_45);
			} else if (remindValues[10].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1h);
			} else if (remindValues[11].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_2h);
			} else if (remindValues[12].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_3h);
			} else if (remindValues[13].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_12h);
			} else if (remindValues[14].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_24h);
			} else if (remindValues[15].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_2D);
			} else if (remindValues[16].equals(mDefaultReminder.getValue())) {
				Statistics
						.onEvent(
								this.getActivity(),
								Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1D);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("wpeng","GNCalendarReminderSettingFragment---onPause---e == " + e);
		}
	}
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity a = getActivity();
        if (key.equals(KEY_ALERTS)) {
            updateChildPreferences();
            ///M: check whether provider is available first.
            boolean canUseProvider = Utils.canUseProviderByUri(a.getContentResolver(),
                                           CalendarContract.CalendarAlerts.CONTENT_URI);
            if(canUseProvider){
                if (a != null) {
                    Intent intent = new Intent();
                    intent.setClass(a, AlertReceiver.class);
                    if (mAlert.isChecked()) {
                        intent.setAction(AlertReceiver.ACTION_DISMISS_OLD_REMINDERS);
                    } else {
                        intent.setAction(CalendarContract.ACTION_EVENT_REMINDER);
                    }
                    a.sendBroadcast(intent);
                }
            } else {
                Toast.makeText(getActivity(), R.string.operation_failed, Toast.LENGTH_LONG).show();
            }
        }
        if (a != null) {
            BackupManager.dataChanged(a.getPackageName());
        }
    }

    /**
     * Handles time zone preference changes
     */
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
  
      if (preference == mDefaultReminder) {
            mDefaultReminder.setValue((String) newValue);
            mDefaultReminder.setSummary(mDefaultReminder.getEntry());
        } else if (preference == mRingtone) {
            // TODO update this after b/3417832 is fixed
            return true;
        } else {
            return true;
        }
        return false;
    }

    /**
     * If necessary, upgrades previous versions of preferences to the current
     * set of keys and values.
     * @param prefs the preferences to upgrade
     */
    private void migrateOldPreferences(SharedPreferences prefs) {
        // If needed, migrate vibration setting from a previous version
        ///M: If key KEY_VIBRATE_FOR_EVENT_REMINDER does not exist, should upgrade setting from old version,
        ///M: for KEY_ALERTS_VIBRATE_WHEN, "silent" and "always" is counted as "vibrate for event reminder" 
    	 //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item begin
    	//        if (!prefs.contains(KEY_VIBRATE_FOR_EVENT_REMINDER)) {
//            if (prefs.contains(KEY_ALERTS_VIBRATE_WHEN)) {
//                String conf = prefs.getString(KEY_ALERTS_VIBRATE_WHEN, "never");
//                mVibrateWhen.setChecked(!conf.equals("never"));
//            } else if (prefs.contains(KEY_ALERTS_VIBRATE)) {
//                boolean checked = prefs.getBoolean(KEY_ALERTS_VIBRATE, false) ? true : false;
//                mVibrateWhen.setChecked(checked);
//            }
//        }
    	 //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item end
        // If needed, migrate the old alerts type setting
        if (!prefs.contains(KEY_ALERTS) && prefs.contains(KEY_ALERTS_TYPE)) {
            String type = prefs.getString(KEY_ALERTS_TYPE, ALERT_TYPE_STATUS_BAR);
            if (type.equals(ALERT_TYPE_OFF)) {
                mAlert.setChecked(false);
                mPopup.setChecked(false);
                mPopup.setEnabled(false);
            } else if (type.equals(ALERT_TYPE_STATUS_BAR)) {
                mAlert.setChecked(true);
                mPopup.setChecked(false);
                mPopup.setEnabled(true);
            } else if (type.equals(ALERT_TYPE_ALERTS)) {
                mAlert.setChecked(true);
                mPopup.setChecked(true);
                mPopup.setEnabled(true);
            }
            // clear out the old setting
            prefs.edit().remove(KEY_ALERTS_TYPE).commit();
        }
    }

    /**
     * Keeps the dependent settings in sync with the parent preference, so for
     * example, when notifications are turned off, we disable the preferences
     * for configuring the exact notification behavior.
     */
    private void updateChildPreferences() {
   	 //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item begin
        if (mAlert.isChecked()) {
//            mVibrateWhen.setEnabled(true);
        	Log.i("jiating","updateChildPreferences..mRingtone="+(mRingtone==null));
            mRingtone.setEnabled(true);
            mPopup.setEnabled(true);
        } else {
//            mVibrateWhen.setEnabled(false);
            mRingtone.setEnabled(false);
            mPopup.setEnabled(false);
        }
   	 //Gionee <jiating><2013-06-13> modify for CR00825951 delete Vibrate Item end
    }


    @Override
    public boolean onPreferenceTreeClick(
            AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        final String key = preference.getKey();
       
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        
    }
    
	public boolean onPreferenceClick(AuroraPreference preference) {
		// TODO Auto-generated method stub
      	Log.v("wpeng","GNCalendarReminderSettingFragment---onPreferenceClick---preference == " + preference);
		if (preference == mRingtone){
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_CHOOSE_RINGTONE);
			return true;
		}else if(preference == mDefaultReminder){
			Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIMEF);
			return true;
		}
		return false;
	}

	
}
//Gionee <jiating> <2013-05-06> modify for CR00000000 end