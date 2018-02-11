package com.android.keyguard.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AuroraLog {
    private final static String TAG = "lock_screen_tag";
    private final static boolean LOCK_SCREEN_DBG = true;
    private final static boolean OPEN_ALL_LOG = true;//false;

    public static void d(String tag, String msg) {
        if (LOCK_SCREEN_DBG) {
            Log.d(tag, msg);
            if (OPEN_ALL_LOG) {
                Log.d(TAG, tag + " :" + msg);
            }
        }
    }

    public static void d(String tag, String msg, Exception e) {
        if (LOCK_SCREEN_DBG) {
            Log.d(tag, msg, e);
        }
    }

    public static void lockScreenLog(String msg) {
        d(TAG, msg);
    }

    public static void lockScreenLog(String msg, Exception e) {
        d(TAG, msg, e);
    }

    public static void lockScreenLog(String tag, String msg) {
        d(TAG + "_" + tag, msg);
    }

    public static void lockScreenLog(String tag, String msg, Exception e) {
        d(TAG + "_" + tag, msg, e);
    }
    
    public static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static void debug(String tag, String msg) {
        if (LOCK_SCREEN_DBG) {
            Log.d(tag, msg);
        }
    }

    public static void debug(String msg) {
        if (LOCK_SCREEN_DBG) {
            Log.d(TAG, msg);
        }
    }

    public static void error(String tag, String error) {
        Log.e(tag, error);
    }

    public static void error(String error) {
        Log.e(TAG, error);
    }
}
