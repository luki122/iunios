package gn.com.android.update.ui;

import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.settings.ApplicationDataManager.OtaSettingKey;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.settings.OtaSettings.AutoCheckCycle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import com.youju.statistics.YouJuAgent;
import android.os.SystemProperties;
//import aurora.widget.AuroraActionBar;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceManager;
import gn.com.android.update.R;

public class UpdateSettingsActivity extends AuroraPreferenceActivity implements
AuroraPreference.OnPreferenceChangeListener {
    private OtaUpgradeManager mOtaUpgradeManager = null;
    private AuroraListPreference mCheckRoundPreference = null;
    private AuroraSwitchPreference mAutoCheckEnablePreference = null;
    private static final String PRE_AUTO_CHECK_ROUND = "pre_round";
    private static final String PRE_AUTO_CHECK_ENABLE = "pre_auto_check_enable";
    private AuroraSwitchPreference mOnlyWlanPreference;
    private static final String ONLY_WLAN = "auto_download_only_wlan";
    private OtaUpgradeInfo mOtaUpgradeInfo = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        YouJuAgent.onResume(UpdateSettingsActivity.this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        YouJuAgent.onPause(UpdateSettingsActivity.this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	/*
        if (GnSettingUpdateThemeUtils.getThemeType(getApplicationContext()).equals(
                GnSettingUpdateThemeUtils.TYPE_DARK_THEME)) {
            setTheme(R.style.GnSettingUpdateDarkTheme);
        } else if (GnSettingUpdateThemeUtils.getThemeType(getApplicationContext()).equals(
                GnSettingUpdateThemeUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingUpdateLightTheme);
        }
        */
    	setTheme(R.style.SettingUpdateLightTheme);
        super.onCreate(savedInstanceState);
        /*
        AuroraActionBar actionBar = getAuroraActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    AuroraActionBar.DISPLAY_HOME_AS_UP | AuroraActionBar.DISPLAY_SHOW_TITLE,
                    AuroraActionBar.DISPLAY_HOME_AS_UP | AuroraActionBar.DISPLAY_SHOW_TITLE
                            | AuroraActionBar.DISPLAY_SHOW_HOME);
        }
		*/
        getAuroraActionBar().setTitle(R.string.setting);
        addPreferencesFromResource(R.xml.setting_preferences);
        mOnlyWlanPreference = (AuroraSwitchPreference) findPreference(ONLY_WLAN);
        mOnlyWlanPreference.setOnPreferenceChangeListener(this);
        mCheckRoundPreference = (AuroraListPreference) findPreference(PRE_AUTO_CHECK_ROUND);
        mCheckRoundPreference.setOnPreferenceChangeListener(this);
        mAutoCheckEnablePreference = (AuroraSwitchPreference) findPreference(PRE_AUTO_CHECK_ENABLE);
        mAutoCheckEnablePreference.setOnPreferenceChangeListener(this);
        mOtaUpgradeManager = OtaUpgradeManager.getInstance(this);
        mOtaUpgradeInfo = mOtaUpgradeManager.getOtaUpgradeInfo();
        setListPrefSummary();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CharSequence getVisualTextName(String enumName, int choiceNameResId, int choiceValueResId) {
        CharSequence[] visualNames = getResources().getTextArray(choiceNameResId);
        CharSequence[] enumNames = getResources().getTextArray(choiceValueResId);

        if (visualNames.length != enumNames.length) {
            return "";
        }

        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }

    private String getCheckDuration() {
        AutoCheckCycle durationValue = OtaSettings.getAutoCheckCycle(UpdateSettingsActivity.this);
        switch (durationValue) {
            case SEVEN_DAYS:
                return "7";
            case FOURTEEN_DAYS:
                return "14";
            case THIRTY_DAYS:
                return "30";
            case NINETY_DAYS:
                return "90";
            default:
                return "30";
        }

    }

    @Override
    public boolean onPreferenceChange(AuroraPreference arg0, Object arg1) {

        boolean onlyWlanOpen = OtaSettings.getAutoDownloadEnabled(UpdateSettingsActivity.this, true);
        boolean isAuto = OtaSettings.getAutoCheckEnabled(UpdateSettingsActivity.this, false);
        CharSequence visualTextName;
        String duration = getCheckDuration();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("S2w");
        final String key = arg0.getKey();
        if (PRE_AUTO_CHECK_ROUND.equals(key)) {
            visualTextName = getVisualTextName((String) arg1, R.array.round_time, R.array.round_time_values);
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(visualTextName.toString());
            if (matcher.find() && !"".equals(matcher.group())) {
                duration = matcher.group();
            }
            mCheckRoundPreference.setSummary(visualTextName);
            OtaSettings.changeAutoCheckCycle(UpdateSettingsActivity.this);
        } else if (PRE_AUTO_CHECK_ENABLE.equals(key)) {
            if ((Boolean) arg1) {
                OtaSettings.setAutoCheckEnable(UpdateSettingsActivity.this, true);
                isAuto = true;
            } else {
                OtaSettings.setAutoCheckEnable(UpdateSettingsActivity.this, false);
                isAuto = false;
            }
        } else {
            if ((Boolean) arg1) {
                onlyWlanOpen = true;
            } else {
                onlyWlanOpen = false;
            }
        }
        writeSettingInfo(onlyWlanOpen, isAuto, stringBuilder, duration);
        return true;
    }

    public void writeSettingInfo(boolean onlyWlanOpen, boolean isAuto, StringBuilder stringBuilder,
            String duration) {
        if (onlyWlanOpen) {
            stringBuilder.append("1a");
            if (isAuto) {
                stringBuilder.append(duration);
            } else {
                stringBuilder.append("0");
            }
        } else {
            stringBuilder.append("0a");
            if (isAuto) {
                stringBuilder.append(duration);
            } else {
                stringBuilder.append("0");
            }
        }
        String wxax = stringBuilder.toString();
        YouJuAgent.onEvent(UpdateSettingsActivity.this, wxax);
    }

    private void setListPrefSummary() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String stored = sp.getString(PRE_AUTO_CHECK_ROUND, getString(R.string.def_auto_check_round));
        mCheckRoundPreference.setSummary(getVisualTextName(stored, R.array.round_time,
                R.array.round_time_values));
    }
}
