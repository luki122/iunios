package com.aurora.puremanager.traffic;

import android.content.Context;
import android.content.SharedPreferences;

public class TrafficPreference {

    private static final String TRAFFIC_PREFERENCE = "traffic_preference";
    private static final String SIM_SETTING = "SIM%d_SETTING";
    private static final String SIM_FLOWLINKFLAG = "SIM%d_FLOWLINKFLAG";
    private static final String SIM_STOPWARNING = "SIM%d_STOPWARNING";
    private static final String SIM_RESET = "SIM%d_RESET";
    private static final String SIM_NOTIFICATION = "SIM%d_NOTIFICATION";
    private static final String ENTRY_FLAG = "ENTRY_FLAG";// check whether is
                                                          // first into
                                                          // trafficassistant
                                                          // windows.

    private static final int TOTAL_SIZE = 5;
    private static final String TOTAL_VALUE = "TOTAL";
    private static final String PERCENT_VALUE = "PERCENT";
    private static final String START_DATE_VALUE = "START_DATE";
    private static final String USER_DEFINED_VALUE = "USER_DEFINED";
    private static final String CURRENT_DATE_VALUE = "CURRENT_DATE";
    private static final String SIM_VALUE = "SIM";
    private static final String TRAFFIC_CYCLE = "weekly";

    public static void setPreference(Context context, int simIndex, String[] data) {
        String name = TRAFFIC_PREFERENCE + String.valueOf(simIndex);
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        info.edit().putString(TOTAL_VALUE, data[0]).putString(PERCENT_VALUE, data[1])
                .putString(START_DATE_VALUE, data[2]).putString(USER_DEFINED_VALUE, data[3])
                .putString(CURRENT_DATE_VALUE, data[4]).commit();
    }

    public static String[] getPreference(Context context, int simIndex) {
        String name = TRAFFIC_PREFERENCE + String.valueOf(simIndex);
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        String[] value = new String[TOTAL_SIZE];
        value[0] = info.getString(TOTAL_VALUE, "");
        value[1] = info.getString(PERCENT_VALUE, "");
        value[2] = info.getString(START_DATE_VALUE, "");
        value[3] = info.getString(USER_DEFINED_VALUE, "");
        value[4] = info.getString(CURRENT_DATE_VALUE, "");
        return value;
    }

    public static String getTrafficCycle() {
        return TRAFFIC_CYCLE;
    }

    public static String getFirstEntryFlag() {
        return ENTRY_FLAG;
    }

    public static String getSimSetting(int simIndex) {
        return String.format(SIM_SETTING, simIndex);
    }

    public static String getSimFlowlinkFlag(int simIndex) {
        return String.format(SIM_FLOWLINKFLAG, simIndex);
    }

    public static String getSimStopWarning(int simIndex) {
        return String.format(SIM_STOPWARNING, simIndex);
    }

    public static String getSimReset(int simIndex) {
        return String.format(SIM_RESET, simIndex);
    }

    public static String getSimNotification(int simIndex) {
        return String.format(SIM_NOTIFICATION, simIndex);
    }

    public static String getSimValue() {
        return SIM_VALUE;
    }

    public static String getUserDefined() {
        return USER_DEFINED_VALUE;
    }

    public static SharedPreferences getInstance(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
    }
}
