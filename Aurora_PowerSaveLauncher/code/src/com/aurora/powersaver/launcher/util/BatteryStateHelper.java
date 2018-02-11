package com.aurora.powersaver.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/*
 * 主动获取电池的状态
 * 1）当前电量
 * 2）是否充电
 * 3）采用何种充电模式
 * 4）etc...
 */
public class BatteryStateHelper {
    // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 begin
    private static final String TAG = "SystemManager/BatteryStateHelper";

    // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 end
    public static int getBatteryLevel(Context context) {
        Intent intent = getBatteryChangedIntent(context);
        if (intent == null) {
            return 0;
        }
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 begin
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        if (level < 0) {
            Log.e(TAG, "ERROR!! HERE battery level lower then 0");
            level = 0;
        } else if (level > 100) {
            Log.e(TAG, "ERROR!! HERE battery level over 100");
            level = 100;
        }
        return level;
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 end
    }

    public static boolean isChargingNow(Context context) {
        Intent intent = getBatteryChangedIntent(context);
        if (intent == null) {
            return false;
        }
        // Gionee <yangxinruo> <2015-08-05> modify for CR01532815 begin
        /*
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return (status == BatteryManager.BATTERY_STATUS_CHARGING)
                || (status == BatteryManager.BATTERY_STATUS_FULL);
        */
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        return usbCharge || acCharge;
        // Gionee <yangxinruo> <2015-08-05> modify for CR01532815 end
    }

    public static int getBatteryPlugType(Context context) {
        int ON_BATTERY = 0;
        Intent intent = getBatteryChangedIntent(context);
        if (intent == null) {
            return ON_BATTERY;
        }

        return intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, ON_BATTERY);
    }

    private static Intent getBatteryChangedIntent(Context context) {
        if (context == null) {
            return null;
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return context.registerReceiver(null, filter);
    }

}
