package com.android.settings.notification;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;

public class AuroraLockscreenNotify extends AuroraSettingsPreferenceFragment
        implements OnPreferenceClickListener {

    public static final String TAG = "AuroraLockscreenNotify";

    private AuroraCheckBoxPreference mShowAll;
    private AuroraCheckBoxPreference mNotShow;
    private AuroraCheckBoxPreference mHideNotifyDetail;

    private static final String KEY_SHOW_ALL = "showall";
    private static final String KEY_NOT_SHOW = "notshow";
    private static final String KEY_HIDE_NOTIFY_DETAIL = "hidenotifydetail";

    private boolean mSecure;

    private boolean mAfterPreferenceClickActivityFinish;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.aurora_lockscreen_notify_settings);
        mShowAll = (AuroraCheckBoxPreference) findPreference(KEY_SHOW_ALL);
        mNotShow = (AuroraCheckBoxPreference) findPreference(KEY_NOT_SHOW);
        mHideNotifyDetail = (AuroraCheckBoxPreference) findPreference(KEY_HIDE_NOTIFY_DETAIL);

        mShowAll.setOnPreferenceClickListener(this);
        mNotShow.setOnPreferenceClickListener(this);
        mHideNotifyDetail.setOnPreferenceClickListener(this);

        mSecure = new LockPatternUtils(getActivity()).isSecure();
        if (!mSecure) {
            getPreferenceScreen().removePreference(mHideNotifyDetail);
        }

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
                mShowAll.setChecked(true);
                if (mSecure) {
                    mHideNotifyDetail.setChecked(false);
                }
            } else {
                mShowAll.setChecked(false);
                if (mSecure) {
                    mHideNotifyDetail.setChecked(true);
                }
            }
            mNotShow.setChecked(false);
        } else {
            mShowAll.setChecked(false);
            if (mSecure) {
                mHideNotifyDetail.setChecked(false);
            }
            mNotShow.setChecked(true);
        }

        Intent intent = getActivity().getIntent();
        mAfterPreferenceClickActivityFinish = intent.getBooleanExtra(Utils.ACTIVITY_FINISH_AFTER_ON_PREFERENCE_CLICK, false);
        if (mAfterPreferenceClickActivityFinish) {
            EnableFingerLock();
        }
    }

    private static final String SYSTEM_PROPERY_FINGER_LOCK = "finger_function_status";

    private void EnableFingerLock() {
        int value = getFingerFunctionProperty();
        int idExist = (value >> 1) & 0x1;
        int lockFlag = value & 0x1;
        if (idExist == 0x1 && lockFlag == 0x0) {
            value = value | 0x1;
            setFingerFunctionProperty(value);
        }
    }

    private void setFingerFunctionProperty(int value) {
        Settings.System.putInt(getContentResolver(), SYSTEM_PROPERY_FINGER_LOCK, value);
    }

    private int getFingerFunctionProperty() {
        return Settings.System.getInt(getContentResolver(), SYSTEM_PROPERY_FINGER_LOCK, 0);
    }

    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {
        // TODO Auto-generated method stub
        Log.v(TAG, "onPreferenceClick");
        if (preference.getKey().equals(KEY_SHOW_ALL)) {
            mShowAll.setChecked(true);
            mNotShow.setChecked(false);
            if (mSecure) {
                mHideNotifyDetail.setChecked(false);
            }
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 1);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 1);
        } else if (preference.getKey().equals(KEY_NOT_SHOW)) {
            mShowAll.setChecked(false);
            mNotShow.setChecked(true);
            if (mSecure) {
                mHideNotifyDetail.setChecked(false);
            }
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0);
        } else if (preference.getKey().equals(KEY_HIDE_NOTIFY_DETAIL)) {
            mShowAll.setChecked(false);
            mNotShow.setChecked(false);
            if (mSecure) {
                mHideNotifyDetail.setChecked(true);
            }
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 1);
        }

        if (mAfterPreferenceClickActivityFinish) {
            getActivity().finish();
        }

        return true;
    }
}