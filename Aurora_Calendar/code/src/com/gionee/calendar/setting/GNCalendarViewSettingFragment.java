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
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import android.provider.CalendarContract;
import android.provider.SearchRecentSuggestions;
import android.provider.CalendarContract.CalendarCache;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.calendar.R;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.Log;
//Gionee <jiating> <2013-05-06> modify for CR00000000 begin
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class GNCalendarViewSettingFragment extends AuroraPreferenceFragment implements
OnSharedPreferenceChangeListener, OnPreferenceChangeListener{
	
	 static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";

	    // AuroraPreference keys
	    public static final String KEY_HIDE_DECLINED = "preferences_hide_declined";
	    public static final String KEY_WEEK_START_DAY = "preferences_week_start_day";
	    public static final String KEY_SHOW_WEEK_NUM = "preferences_show_week_num";
	    public static final String KEY_DAYS_PER_WEEK = "preferences_days_per_week";
	    public static final String KEY_SKIP_SETUP = "preferences_skip_setup";

	    public static final String KEY_CLEAR_SEARCH_HISTORY = "preferences_clear_search_history";
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

	 
	    static final String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
	    static final String KEY_HOME_TZ = "preferences_home_tz";
	    
	    
	    AuroraCheckBoxPreference mUseHomeTZ;
	    AuroraCheckBoxPreference mHideDeclined;
	    AuroraListPreference mHomeTZ;
	    AuroraListPreference mWeekStart;

	    private CharSequence[][] mTimezones;
	    // Default preference values
	    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.WEEK;
	    public static final int DEFAULT_DETAILED_VIEW = CalendarController.ViewType.DAY;
	    public static final boolean DEFAULT_SHOW_WEEK_NUM = false;
	    
	    
	    
	    public static SharedPreferences getSharedPreferences(Context context) {
	        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	    }

	    /** Set the default shared preferences in the proper context */
	    public static void setDefaultValues(Context context) {
	        AuroraPreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME, Context.MODE_PRIVATE,
	                R.xml.gn_view_general_preferences, false);
	    }
	    
	    
	    
	    @Override
		public void onCreate(Bundle savedInstanceState) {
	    	// TODO Auto-generated method stub
	    	super.onCreate(savedInstanceState);

	    	 final AuroraActivity activity =(AuroraActivity)getActivity();

	         // Make sure to always use the same preferences file regardless of the package name
	         // we're running under
	         final AuroraPreferenceManager preferenceManager = getPreferenceManager();
	         final SharedPreferences sharedPreferences = getSharedPreferences(activity);
	         preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);

	         // Load the preferences from an XML resource
	         addPreferencesFromResource(R.xml.gn_view_general_preferences);
	         final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
	         mUseHomeTZ = (AuroraCheckBoxPreference) preferenceScreen.findPreference(KEY_HOME_TZ_ENABLED);
	         // Gionee <jiangxiao> <2013-06-21> delete for CR00828506 begin
	         // mHideDeclined = (AuroraCheckBoxPreference) preferenceScreen.findPreference(KEY_HIDE_DECLINED);
	         // Gionee <jiangxiao> <2013-06-21> delete for CR00828506 end
	         mWeekStart = (AuroraListPreference) preferenceScreen.findPreference(KEY_WEEK_START_DAY);
	         mHomeTZ = (AuroraListPreference) preferenceScreen.findPreference(KEY_HOME_TZ);
	         String tz = mHomeTZ.getValue();
	         mWeekStart.setSummary(mWeekStart.getEntry());
	         if (mTimezones == null) {
	             mTimezones = (new TimezoneAdapter(activity, tz, System.currentTimeMillis()))
	                     .getAllTimezones();
	         }
	         mHomeTZ.setEntryValues(mTimezones[0]);
	         mHomeTZ.setEntries(mTimezones[1]);
	         CharSequence tzName = mHomeTZ.getEntry();
	         ///M: Should not set TimeZone value to summary.It will restore the
	         /// systemTZ as homeTZ at the first time launch the calendar setting.@{
	         if (TextUtils.isEmpty(tzName)) {
	             mHomeTZ.setValue(Utils.getTimeZone(activity, null));
	             mHomeTZ.setSummary(mHomeTZ.getEntry());
	         } else {
	             mHomeTZ.setSummary(tzName);
	         }
	         ///@}


	        
	    }
	    
	    @Override
	    public void onResume() {
	        super.onResume();
	        getPreferenceScreen().getSharedPreferences()
	                .registerOnSharedPreferenceChangeListener(this);
	        setPreferenceListeners(this);
	        Statistics.onResume(getActivity());
	    }
	    
	    
	    /**
	     * Sets up all the preference change listeners to use the specified
	     * listener.
	     */
	    private void setPreferenceListeners(OnPreferenceChangeListener listener) {
	        mUseHomeTZ.setOnPreferenceChangeListener(listener);
	        mHomeTZ.setOnPreferenceChangeListener(listener);
	        mWeekStart.setOnPreferenceChangeListener(listener);
	        // Gionee <jiangxiao> <2013-06-21> delete for CR00828506 begin
	        // mHideDeclined.setOnPreferenceChangeListener(listener);
	        // Gionee <jiangxiao> <2013-06-21> delete for CR00828506 end
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        String weekStartValue = mWeekStart.getValue();
	        Log.v("GNCalendarViewSettingFragmet---onPause---weekStartValue == " + weekStartValue);
	        Log.v("GNCalendarViewSettingFragmet---onPause---mUseHomeTzBool == " + mUseHomeTZ.isChecked());
	        if(mUseHomeTZ.isChecked()){
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_HOME_TIME_ZONE_ON);
	        }else{
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_HOME_TIME_ZONE_OFF);
	        }
	        if(WEEK_START_SATURDAY.equals(weekStartValue)){
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_ON_SAT);
	        }else if(WEEK_START_SUNDAY.equals(weekStartValue)){
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_ON_SUN);
	        }else if(WEEK_START_MONDAY.equals(weekStartValue)){
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_ON_MON);
	        }else if(WEEK_START_DEFAULT.equals(weekStartValue)){
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_ON_LOCAL);
	        }
	        getPreferenceScreen().getSharedPreferences()
	                .unregisterOnSharedPreferenceChangeListener(this);
	        setPreferenceListeners(null);
	        Statistics.onPause(getActivity());
	    }

	    @Override
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	        Activity a = getActivity();
	    
	        if (a != null) {
	            BackupManager.dataChanged(a.getPackageName());
	        }
	    }

	    /**
	     * Handles time zone preference changes
	     */
	    @Override
	    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
	        String tz;
	        if (preference == mUseHomeTZ) {
	            if ((Boolean)newValue) {
	                tz = mHomeTZ.getValue();
	            } else {
	                tz = CalendarCache.TIMEZONE_TYPE_AUTO;
	            }
	            Utils.setTimeZone(getActivity(), tz);
	            return true;
	        } else if (preference == mHideDeclined) {
	            mHideDeclined.setChecked((Boolean) newValue);
	            Activity act = getActivity();
	            Intent intent = new Intent(Utils.getWidgetScheduledUpdateAction(act));
	            intent.setDataAndType(CalendarContract.CONTENT_URI, Utils.APPWIDGET_DATA_TYPE);
	            act.sendBroadcast(intent);
	            return true;
	        } else if (preference == mHomeTZ) {
	            tz = (String) newValue;
	            // We set the value here so we can read back the entry
	            mHomeTZ.setValue(tz);
	            mHomeTZ.setSummary(mHomeTZ.getEntry());
	            Utils.setTimeZone(getActivity(), tz);
	        } else if (preference == mWeekStart) {
				Statistics.onEvent(this.getActivity(),Statistics.SLIDING_VIEW_SETTING_VIEW_START_ON);
	            mWeekStart.setValue((String) newValue);
	            mWeekStart.setSummary(mWeekStart.getEntry());
	        } else {
	            return true;
	        }
	        return false;
	    }

	  




	    @Override
	    public boolean onPreferenceTreeClick(
	            AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
	        final String key = preference.getKey();
	        if (KEY_CLEAR_SEARCH_HISTORY.equals(key)) {
	            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
	                    Utils.getSearchAuthority(getActivity()),
	                    CalendarRecentSuggestionsProvider.MODE);
	            suggestions.clearHistory();
	            ///M:here use MTKToast to avoid show very long time when click many times 
	            MTKToast.toast(getActivity(), R.string.search_history_cleared);
	            ///@}
	            return true;
	        } else {
	            return super.onPreferenceTreeClick(preferenceScreen, preference);
	        }
	    }

}
//Gionee <jiating> <2013-05-06> modify for CR00000000 end
//Gionee <pengwei><20130807> modify for CR00850530 end