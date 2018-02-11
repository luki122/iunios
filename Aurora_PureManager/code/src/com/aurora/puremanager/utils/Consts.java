package com.aurora.puremanager.utils;

/**
 * Created by joy on 12/22/15.
 */
public class Consts {
    public static final String POWER_SAVE_LAUNCHER_PKG_NAME = "com.aurora.powersaver.launcher";
    public static final String POWER_SAVE_LAUNCHER_ACITVITY_NAME = "com.aurora.powersaver.launcher.Main";
    public static final String AURORA_LAUNCHER_PKG_NAME = "com.aurora.launcher";
    public static final String DEFAULT_LAUNCHER_PKG_NAME = "default_pkg";
    public static final String DEFAULT_LAUNCHER_ACTIVITY_NAME = "default_activity";

    // FIXME: 12/26/15
    public static final String OBJ_PTAH = "data/data/com.aurora.puremanager/object.obj";
    public static final String MODE_CHANGE_INTENT = "gionee.action.powermanager.changemode";

    public static final int NONE_MODE = 0;
    public static final int NORMAL_MODE = 1;
    public static final int SUPER_MODE = 2;

    public static final String POWER_MODE_PROCESSING = "amigo_powermode_processing";
    public static final int PROCESSING = 0;
    public static final int DONE = 1;

    public static final String NONE_POWER = "none_";
    public static final String NORMAL_POWER = "normal_";
    public static final String SUPER_POWER = "super_";

    public static final String WIFI_KEY = "state_wifi";
    public static final String WIFI_AP_KEY = "state_wifi_ap";
    public static final String BLUETOOTH_KEY = "state_bluetooth";
    public static final String DATA_KEY = "state_dataconnection";
    public static final String GPS_KEY = "state_gps";
    public static final String SYNC_KEY = "state_sync";
    public static final String ADB_KEY = "state_adb";
    public static final String BRIGHTNESS_KEY = "brightness_value";
    public static final String BRIGHTNESS_STATE_KEY = "state_brightness";
    public static final String TIMEOUT_KEY = "state_timeout";
    public static final String BRIGHTNESS_MODE_KEY = "state_brightness_mode";
    public static final String POWER_MODE_KEY = "aurora_power_mode";
    public static final String POWER_MODE_SUPER = "super";
    public static final String POWER_MODE_NORMAL = "normal";

    public static final String DISABLE_HANDLER = "com.android.systemui.recent.AURORA_DISABLE_HANDLER";
    public static final String ENABLE_HANDLER = "com.android.systemui.recent.AURORA_ENABLE_HANDLER";
    public static final int ADB_ON = 1;
    public static final int ADB_OFF = 0;

    public static final String DEVICE_NAME_KEY = "ro.product.name";
    public static final String DEVICE_N1_KEY = "N1";
    public static final String DEVICE_U5_KEY = "U0003";
    public static int battery_capacity_u5 = 5020;
    public static int battery_capacity_n1 = 1200;
    public static int battery_capacity_default = 1800;
    public static int current_in_supermode_u5 = 14;
    public static int current_in_supermode_n1 = 10;
    public static int current_in_supermode_default = 12;
    public static final int HOUR_TO_MINS = 60;

}
