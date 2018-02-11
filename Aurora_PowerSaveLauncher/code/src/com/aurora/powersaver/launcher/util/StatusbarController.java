package com.aurora.powersaver.launcher.util;

import android.content.Context;
import android.util.Log;
import android.app.StatusBarManager;

public class StatusbarController {
    private static int mStatusBarFlag = StatusBarManager.DISABLE_NONE;
    private static final String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
    private static boolean DEBUG = true;
    private static String TAG = "dzmdzm/StatusbarController";

    public static void enableStatusbar(Context context) {
        int enable_value = 1;
        updateDatabase(context, enable_value);
        // setStatusbarState(context, StatusBarManager.DISABLE_NONE);
        setSatusBarExpand(context, true);
    }

    public static void disableStatusbar(Context context) {
        int disable_value = 0;
        updateDatabase(context, disable_value);
        // setStatusbarState(context, StatusBarManager.DISABLE_EXPAND);
        setSatusBarExpand(context, false);
    }

    // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 begin
    public static void updateDatabase(Context context, int value) {
        Log.d(TAG, "updateDatabase in StatusbarController--->" + value);
        //AmigoSettings.putInt(context.getContentResolver(), AMIGO_SETTING_CC_SWITCH, value);
    }

    // Gionee <yangxinruo> <2015-09-06> modify for CR01548328 end

    private static void setSatusBarExpand(Context context, boolean isEnable) {
        if (isEnable) {
            mStatusBarFlag &= ~StatusBarManager.DISABLE_EXPAND;
        } else {
            mStatusBarFlag |= StatusBarManager.DISABLE_EXPAND;
        }
        setStatusbarState(context, mStatusBarFlag);
    }

    private static void setStatusbarState(Context context, int state) {
        Log.d(TAG, "setStatusbarState in StatusbarController ------->" + state);
        StatusBarManager barManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        barManager.disable(state);
    }

    public static void enableOnlyStatusbar(Context context) {
        // setSatusBarExpand(context, true);
        StatusBarManager barManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        Log.d(TAG, "enableOnlyStatusbar in StatusbarController ------->" + StatusBarManager.DISABLE_NONE);
        barManager.disable(StatusBarManager.DISABLE_NONE);
    }

    public static void disableOnlyStatusbar(Context context) {
        // setSatusBarExpand(context, false);
        StatusBarManager barManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        Log.d(TAG, "disableOnlyStatusbar in StatusbarController ------->" + StatusBarManager.DISABLE_EXPAND);
        barManager.disable(StatusBarManager.DISABLE_EXPAND);
    }

    public static void enableControlCenter(Context context) {
        int enable_value = 1;
        updateDatabase(context, enable_value);
    }

    public static void disableControlCenter(Context context) {
        int disable_value = 0;
        updateDatabase(context, disable_value);
    }
}
