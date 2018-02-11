package com.aurora.lib.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class AuroraLog {

    private static final boolean FLAG = true;
    private static final String LOG_HEAD = "Aurora_framework";

    public static void i(String tag, String msg) {
        if (FLAG) {
            Log.i(tag, "" + msg);

        }
    }

    public static void v(String tag, String msg) {
        if (FLAG) {
            Log.v(tag, "" + msg);

        }
    }

    public static void d(String tag, String msg) {
        if (FLAG) {
            Log.d( tag, "" + msg);

        }
    }

    public static void e(String tag, String msg) {
        if (FLAG) {
            Log.e(tag , "" + msg);

        }
    }


}
