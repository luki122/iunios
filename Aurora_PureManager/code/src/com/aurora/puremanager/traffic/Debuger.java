package com.aurora.puremanager.traffic;

import android.util.Log;

public class Debuger {
    private static final String TAG = "trafficassistant";

    public static void print(String logInfo) {
        Log.e(TAG, logInfo);
    }
}