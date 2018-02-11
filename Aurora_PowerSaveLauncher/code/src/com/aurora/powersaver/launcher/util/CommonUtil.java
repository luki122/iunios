package com.aurora.powersaver.launcher.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.SystemProperties;
import android.util.Log;

public class CommonUtil {
    private static final int HOUR_TO_MINS = 60;
    private Context mContext;
    private PowerConfig mConfig;

    public CommonUtil(Context context) {
        mContext = context;
        mConfig = PowerConfigParser.getProjectConfig(context);
    }

    // TODO:需要根据测试结果获得在极致省电模式时的可用时间
    public int getTimeInSuperMode() {
        int level = getCurrentLevel();
        if (level < 0) {
            return 0;
        }

        int capacity, current;
        String deviceName = SystemProperties.get(Globle.DEVICE_NAME_KEY);
        if (deviceName.equals(Globle.DEVICE_U5_KEY)) {
            capacity = (int) (level * mConfig.battery_capacity_u5 / (float) 100);
            current = mConfig.current_in_supermode_u5;
        } else if (deviceName.equals(Globle.DEVICE_N1_KEY)) {
            capacity = (int) (level * mConfig.battery_capacity_n1 / (float) 100);
            current = mConfig.current_in_supermode_n1;
        } else {
            capacity = (int) (level * mConfig.battery_capacity_default / (float) 100);
            current = mConfig.current_in_supermode_default;
        }

        int time = (int) (HOUR_TO_MINS * (capacity / (float) current));
        return time;
    }

    /*计算在出厂状态下的可用时间*/
    private int getTimeAccordingLevel(int[] durations) {
        /*        int[] durations = {mConfig.zero_ten_time, mConfig.ten_twenty_time, mConfig.twenty_thirty_time,
                        mConfig.thirty_forty_time, mConfig.forty_fifty_time, mConfig.fifty_sixty_time,
                        mConfig.sixty_seventy_time, mConfig.seventy_eight_time, mConfig.eight_ninety_time,
                        mConfig.ninety_hundred_time};*/
        int sum = 0;
        int level = getCurrentLevel();
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 begin
        // int size = durations.length;
        int size = 100 / durations.length;
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 end
        int tens = level / size;
        int single = level % size;

        Log.e("dzmdzm", "tens = " + tens + ", single = " + single);

        for (int i = 0; i < tens; i++) {
            sum += durations[i];
        }

        if (level != 100) {
            sum += single * durations[tens] / size;
        }
        sum = (int) (sum * getMagnification(level));

        return sum;
    }

    private int getCurrentLevel() {
        return BatteryStateHelper.getBatteryLevel(mContext);
    }

    /*
     * 当电量高于某一值时，放大真实的测试使用时间
     * y = k*x + b
     * multiple = 3 放大倍数
     * xMax = (100 - lowerLimit) / 50f
     * bArg = multiple / xMax 
     * kArg = multiple + bArg 斜率
     * */
    private float getMagnification(int level) {
        /*        int lowerLimit = 40;
                if (level < lowerLimit) {
                    return 1.0f;
                } else {
                    float xMax = (100 - lowerLimit) / 50f;
                    float multiple = 3;
                    float bArg = multiple / xMax;
                    float kArg = multiple + bArg;
                    float x = (100f - level) / 50f;
                    return ( kArg / (x + 1.0f) ) + 1.0f;
                }*/

        return 1.0f;
    }

    public int getChargeTime() {
        int time = 0;
        int chargeType = BatteryStateHelper.getBatteryPlugType(mContext);
        int consumedCurrent = getConsumedCapacity(mContext);
        Log.e("dzmdzm", "chargeType = " + chargeType + ", consumedCurrent = " + consumedCurrent);
        // TODO: maybe this situation maybe can't be happen
        if (mConfig.ac_current == 0) {
            mConfig.ac_current = 900;
        }

        if (mConfig.usb_current == 0) {
            mConfig.usb_current = 350;
        }

        if (chargeType == BatteryManager.BATTERY_PLUGGED_AC) {
            time = consumedCurrent * HOUR_TO_MINS / mConfig.ac_current;
        } else if (chargeType == BatteryManager.BATTERY_PLUGGED_USB) {
            time = consumedCurrent * HOUR_TO_MINS / mConfig.usb_current;
        }
        Log.e("dzmdzm", "PowerTimer->getChargeTime(), time = " + time);

        return time;
    }

    private int getConsumedCapacity(Context context) {
        int capacity;
        String deviceName = SystemProperties.get(Globle.DEVICE_NAME_KEY);
        if (deviceName.equals(Globle.DEVICE_U5_KEY)) {
            capacity = (mConfig.battery_capacity_u5 / 100) * (100 - getCurrentLevel());
        } else if (deviceName.equals(Globle.DEVICE_N1_KEY)) {
            capacity = (mConfig.battery_capacity_n1 / 100) * (100 - getCurrentLevel());
        } else {
            capacity = (mConfig.battery_capacity_default / 100) * (100 - getCurrentLevel());
        }
        return capacity;
    }

    public String formatTime(int time) {
        String timeStr;
        Resources res = mContext.getResources();
        if (time < 0) {
            timeStr = res.getString(com.aurora.powersaver.launcher.R.string.power_cannotget);
        } else {
            int hours = time / 60;
            int minutes = time % 60;
            timeStr = hours + res.getString(com.aurora.powersaver.launcher.R.string.power_hours) + minutes
                    + res.getString(com.aurora.powersaver.launcher.R.string.power_minutes);
        }

        return timeStr;
    }

}