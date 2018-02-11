package com.aurora.puremanager.utils;

import android.content.ContentResolver;
import android.provider.Settings;

/**
 * Created by joy on 1/11/16.
 */
public class SettingValueUtil {
    private ContentResolver mResolver;
    public final static String BRIGHTNESS_VALUE = "brightness_value";
    public final static String TIMEOUT_VALUE = "screen_timeout_value";
    private static final String TAG = "SettingValueUtil";

    public SettingValueUtil(ContentResolver mResolver) {
        this.mResolver = mResolver;
    }

    public int getBrightnessValue() {
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, 128);
    }

    public void setBrightnessValue(int value) {
        Log.e(TAG, "setBrightnessValue " + value);
        Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, value);
    }

    public int getBrightnessMode() {
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public boolean isBrightnessAutomaticMode() {
        if (getBrightnessMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            return true;
        } else {
            return false;
        }
    }

    public void setScreenTimeout(int timeout) {
        int value = (timeout == -1) ? -1 : timeout;
        Log.e(TAG, "setBrightnessValue " + timeout);
        Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, value);
    }

    public int getScreenTimeout() {
        int defTimeout = 15000;
        return Settings.System.getInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, defTimeout);
    }

}
