/*
 * Copyright 2014, Medietek inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.settings.sim;

/**
 * Manages logging for the entire module.
 */
public class Log {

    private static boolean mbLoggable = true;
    private static String GREP = "SystemSettingsExt <-> ";

    public static void setLoggable(boolean bLoggable)
    {
        mbLoggable = bLoggable;
    }

    public static boolean isLoggable()
    {
        return mbLoggable;
    }

    public static boolean isDebuggable()
    {
        return mbLoggable;
    }

    public static int i(String tag, String msg) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];

        return android.util.Log.i(GREP + tag, "\t  Line " + ste.getLineNumber() + "\t" + msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.i(GREP + tag, "\t  Line " + ste.getLineNumber() + "\t" + msg, tr);
    }

    public static int d(String tag, String msg) {
        if (!mbLoggable)
            return -1;
        StackTraceElement ste = new Throwable().getStackTrace()[1];

        return android.util.Log.d(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.d(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }

    public static int e(String tag, String msg) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.e(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.e(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }

    public static int v(String tag, String msg) {
        if (!mbLoggable)
            return -1;
        StackTraceElement ste = new Throwable().getStackTrace()[1];

        return android.util.Log.v(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.v(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.w(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!mbLoggable)
            return -1;

        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.w(GREP + tag, "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }
}
