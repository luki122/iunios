/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import aurora.app.AuroraAlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import aurora.preference.AuroraListPreference;
import android.util.AttributeSet;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class RepeatPreference extends AuroraListPreference {
	
	

    // Initial value that can be set with the values saved in the database.
    private Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
    // New value that will be set if a positive result comes back from the
    // dialog.
    private Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);

    // Gionee <baorui><2013-08-02> modify for CR00844557 begin
    private Alarm.DaysOfWeek mOldDaysOfWeek = new Alarm.DaysOfWeek(0);
    // Gionee <baorui><2013-08-02> modify for CR00844557 end

    public RepeatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        String[] weekdays = new DateFormatSymbols().getWeekdays();
        String[] values = new String[] {
            weekdays[Calendar.MONDAY],
            weekdays[Calendar.TUESDAY],
            weekdays[Calendar.WEDNESDAY],
            weekdays[Calendar.THURSDAY],
            weekdays[Calendar.FRIDAY],
            weekdays[Calendar.SATURDAY],
            weekdays[Calendar.SUNDAY],
        };
        setEntries(values);
        setEntryValues(values);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mDaysOfWeek.set(mNewDaysOfWeek);
            // Gionee <baorui><2013-08-02> modify for CR00844557 begin
            mOldDaysOfWeek.set(mNewDaysOfWeek);
            // Gionee <baorui><2013-08-02> modify for CR00844557 end
            setSummary(mDaysOfWeek.toString(getContext(), true));
            callChangeListener(mDaysOfWeek);
        } 
        // Gionee <baorui><2013-08-02> modify for CR00844557 begin
        else {
            mNewDaysOfWeek.set(mOldDaysOfWeek);
        }
        // Gionee <baorui><2013-08-02> modify for CR00844557 end
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();

        builder.setMultiChoiceItems(
                entries, mDaysOfWeek.getBooleanArray(),
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        mNewDaysOfWeek.set(which, isChecked);
                    }
                });
    }

    public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
        mDaysOfWeek.set(dow);
        mNewDaysOfWeek.set(dow);
        // Gionee <baorui><2013-08-02> modify for CR00844557 begin
        mOldDaysOfWeek.set(dow);
        // Gionee <baorui><2013-08-02> modify for CR00844557 end
        setSummary(dow.toString(getContext(), true));
    }

    public Alarm.DaysOfWeek getDaysOfWeek() {
        return mDaysOfWeek;
    }
}
