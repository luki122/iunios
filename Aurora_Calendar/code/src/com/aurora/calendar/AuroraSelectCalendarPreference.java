package com.aurora.calendar;

import android.content.Context;
import android.util.AttributeSet;

import aurora.preference.AuroraSwitchPreference;

import com.android.calendar.R;

public class AuroraSelectCalendarPreference extends AuroraSwitchPreference {

    public AuroraSelectCalendarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.aurora_preference);
    }

    public AuroraSelectCalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.aurora_preference);
    }

    public AuroraSelectCalendarPreference(Context context) {
        super(context, null);
        setLayoutResource(R.layout.aurora_preference);
    }
}