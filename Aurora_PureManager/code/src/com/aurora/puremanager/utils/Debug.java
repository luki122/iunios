package com.aurora.puremanager.utils;

import android.util.Log;

public class Debug {
    private static boolean mIsOpen = true;

    public static void log(boolean isOpen, String logFlag, String logInfo) {
        if (mIsOpen && isOpen) {
            Log.d(logFlag, logInfo);
        }
    }

    public static void logError(boolean isOpen, String tag, String msg, Throwable tr) {
        if (mIsOpen && isOpen) {
            Log.e(tag, msg, tr);
        }
    }
}