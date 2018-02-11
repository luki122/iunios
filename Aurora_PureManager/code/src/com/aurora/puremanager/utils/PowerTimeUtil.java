package com.aurora.puremanager.utils;

import android.os.SystemProperties;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joy on 2/19/16.
 */
public class PowerTimeUtil {
    public static int getTimeInSuperMode(int level) {
        if (level < 0) {
            return 0;
        }

        int capacity, current;
        String deviceName = SystemProperties.get(Consts.DEVICE_NAME_KEY);
        if (deviceName.equals(Consts.DEVICE_U5_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_u5 / (float) 100);
            current = Consts.current_in_supermode_u5;
        } else if (deviceName.equals(Consts.DEVICE_N1_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_n1 / (float) 100);
            current = Consts.current_in_supermode_n1;
        } else {
            capacity = (int) (level * Consts.battery_capacity_default / (float) 100);
            current = Consts.current_in_supermode_default;
        }

        int time = (int) (Consts.HOUR_TO_MINS * (capacity / (float) current));
        return time;
    }

    public static String getTimeStrInSuperMode(int level) {
        if (level < 0) {
            return null;
        }

        int capacity, current;
        String deviceName = SystemProperties.get(Consts.DEVICE_NAME_KEY);
        if (deviceName.equals(Consts.DEVICE_U5_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_u5 / (float) 100);
            current = Consts.current_in_supermode_u5;
        } else if (deviceName.equals(Consts.DEVICE_N1_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_n1 / (float) 100);
            current = Consts.current_in_supermode_n1;
        } else {
            capacity = (int) (level * Consts.battery_capacity_default / (float) 100);
            current = Consts.current_in_supermode_default;
        }

        int time = (int) (Consts.HOUR_TO_MINS * (capacity / (float) current));
        int hours = time / 60;
        int minutes = time % 60;
        StringBuilder builder = new StringBuilder("可待机");
        builder.append(String.valueOf(hours));
        builder.append("小时");
        builder.append(String.valueOf(minutes));
        builder.append("分");
        return builder.toString();
    }

    static final double[] u5Current = new double[]{20.2, 147, 384.2, 89.5};
    static final double[] u5IntelCurrent = new double[]{19.1, 145.6, 338.2, 88.7};
    static final double[] n1Current = new double[]{20.2, 147, 384.2, 89.5};
    static final double[] n1IntelCurrent = new double[]{20.2, 147, 384.2, 89.5};

    public static List<String> getMiscStandbyTime(int level, boolean mode) {
        List<String> list = new ArrayList<String>();

        if (level < 0) {
            return null;
        }

        int capacity;
        double[] current;
        String deviceName = SystemProperties.get(Consts.DEVICE_NAME_KEY);
        if (deviceName.equals(Consts.DEVICE_U5_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_u5 / (float) 100);
            if (mode) {
                current = u5IntelCurrent;
            } else {
                current = u5Current;
            }
        } else if (deviceName.equals(Consts.DEVICE_N1_KEY)) {
            capacity = (int) (level * Consts.battery_capacity_n1 / (float) 100);
            if (mode) {
                current = n1IntelCurrent;
            } else {
                current = n1Current;
            }
        } else {
            capacity = (int) (level * Consts.battery_capacity_u5 / (float) 100);
            current = u5Current;
        }

        for (int i = 0; i < 4; i++) {
            double hour = capacity / current[i];
            DecimalFormat df = new DecimalFormat("###.0");
            String hourStr;
            if (hour < 1) {
                hourStr = "0" + df.format(hour) + "h";
            } else {
                hourStr = df.format(hour) + "h";
            }
            list.add(hourStr);
        }

        return list;
    }
}
