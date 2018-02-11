// Gionee <wangpf> <2013-09-09> modify for CR00894142 begin
package com.android.providers.downloads.util;

import android.content.Context;
import android.content.res.Resources;

import com.android.providers.downloads.R;

public class DateUtils {
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

    public static CharSequence formatDuration(Context context, long millis) {
        final Resources res = context.getResources();
        if (millis >= HOUR_IN_MILLIS) {
            final int hours = (int) ((millis + 1800000) / HOUR_IN_MILLIS);
            return String.format(res.getString(R.string.duration_hours), hours);
        } else if (millis >= MINUTE_IN_MILLIS) {
            final int minutes = (int) ((millis + 30000) / MINUTE_IN_MILLIS);
            return String.format(res.getString(R.string.duration_minutes), minutes);
        } else {
            final int seconds = (int) ((millis + 500) / SECOND_IN_MILLIS);
            return String.format(res.getString(R.string.duration_seconds), seconds);
        }
    }
}
//Gionee <wangpf> <2013-09-09> modify for CR00894142 end
