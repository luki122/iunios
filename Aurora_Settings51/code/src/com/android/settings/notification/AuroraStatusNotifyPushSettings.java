package com.android.settings.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;

import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;

public class AuroraStatusNotifyPushSettings extends
        AuroraSettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "AuroraStatusNotifyPushSettings";

    private static final String KEY_DISPLAY_NETWORK_SPEED = "display_network_speed";
    private static final String KEY_DIGIT_BATTERY_PRECENT = "display_digit_battery_precent";
    private static final String KEY_APPLICATION_NOTIFY_MGR = "application_notify_mgr";
    private static final String KEY_NOTIFY_MGR = "notify_mgr";
    private static final String KEY_LOCKSCREEN_NOTIFY = "lockscreen_notify";

    private static final String BATTERY_PERCENTAGE = "battery_percentage";
    private static final String ACTION_NETWORKS_SPEED = "action_isdisplay_network_speed";
    private static final String TABLE_NETWORK_DISPLAY = "isdisplay_network_speed";
    private static final String DISPLAY = "isdisplay";
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AuroraSwitchPreference mDisplayNetSpeed;
    private AuroraSwitchPreference mDisplayBatPrecent;
    private AuroraPreferenceCategory mApplicationNotifyMgr;
    private AuroraPreferenceScreen mLockscreenNotify;
    private AuroraPreferenceScreen mNotifyMgr;

    private boolean isOpen;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.aurora_status_notify_push_settings);
        sharedPreferences = getActivity().getSharedPreferences(
                TABLE_NETWORK_DISPLAY,
                Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();
        isOpen = sharedPreferences.getBoolean(DISPLAY, false);

        mDisplayNetSpeed = (AuroraSwitchPreference) findPreference(KEY_DISPLAY_NETWORK_SPEED);
        mDisplayNetSpeed.setChecked(isOpen);
        mDisplayNetSpeed
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(
                            AuroraPreference preference, Object check) {
                        boolean checked = ((Boolean) check).booleanValue();
                        // MonkeyTest
                        if (getActivity() == null) {
                            return false;
                        }
                        Intent intent = new Intent(ACTION_NETWORKS_SPEED);
                        intent.putExtra(DISPLAY, checked);
                        editor.putBoolean(DISPLAY, checked);
                        editor.commit();
                        if (checked) {
                            Settings.System.putString(getContentResolver(),
                                    "network_display_status", "true");
                        } else {
                            Settings.System.putString(getContentResolver(),
                                    "network_display_status", "false");
                        }
                        getActivity().sendBroadcast(intent);
                        return true;
                    }
                });

        mDisplayBatPrecent = (AuroraSwitchPreference) findPreference(KEY_DIGIT_BATTERY_PRECENT);
        mDisplayBatPrecent.setOnPreferenceChangeListener(this);
        boolean enable = Settings.Secure.getInt(getActivity()
                .getContentResolver(), BATTERY_PERCENTAGE, 0) != 0;
        mDisplayBatPrecent.setChecked(enable);
        //getPreferenceScreen().removePreference(mDisplayBatPrecent);

        mLockscreenNotify = (AuroraPreferenceScreen) findPreference(KEY_LOCKSCREEN_NOTIFY);
        mApplicationNotifyMgr = (AuroraPreferenceCategory) findPreference(KEY_APPLICATION_NOTIFY_MGR);
        mNotifyMgr = (AuroraPreferenceScreen) findPreference(KEY_NOTIFY_MGR);
        mNotifyMgr.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(AuroraPreference preference) {
                try {
                    Intent intent = new Intent();
                    ComponentName cn = new ComponentName(
                            "com.android.settings",
                            "com.android.settings.notification.AuroraNotificationAppListActivity");
                    intent.setComponent(cn);
                    startActivity(intent);
                    ((AuroraActivity) getActivity()).overridePendingTransition(
                            com.aurora.R.anim.aurora_activity_open_enter,
                            com.aurora.R.anim.aurora_activity_open_exit);
                } catch (Exception e) {
                }
                return false;
            }
        });

        //getPreferenceScreen().removePreference(mApplicationNotifyMgr);
        //getPreferenceScreen().removePreference(mNotifyMgr);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mLockscreenNotify.setSummary(getLockScreenNotifyMode());
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference pref, Object newValue) {
        // TODO Auto-generated method stub
        if (KEY_DIGIT_BATTERY_PRECENT.equals(pref.getKey())) {
            int state = ((Boolean) newValue) ? 1 : 0;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    BATTERY_PERCENTAGE, state);
            Intent intent = new Intent(ACTION_BATTERY_PERCENTAGE_SWITCH);
            intent.putExtra("state", state);
            getActivity().sendBroadcast(intent);
        }
        return true;
    }

    public String getLockScreenNotifyMode() {
        int show = 0;
        int value = 0;
        try {
            show = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS);

            value = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (show == 1) {
            if (value == 1) {
                return getString(R.string.lockscreen_showall);
            } else {
                return getString(R.string.lockscreen_hidenotifydetail);
            }
        } else {
            return getString(R.string.lockscreen_notshow);
        }
    }

}
