package com.aurora.puremanager.permission;

import android.util.Log;

/**
 * 
 File Description: Class for debug.
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-28 Change List:
 */
public class DebugUtil {
    /**
     * Debug switch.
     */
    public static final boolean DEBUG = true;
    /**
     * For debug tag.
     */
    public static final String TAG = "Amigo_Settings/permission";

    public static void v(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message) {

        if (DEBUG) {
            Log.d(TAG, tag + message);
        }
    }
    
    public static void e(boolean condition, String message) {
        if (condition) {
            Log.e(TAG, message);
        }
    }
}
