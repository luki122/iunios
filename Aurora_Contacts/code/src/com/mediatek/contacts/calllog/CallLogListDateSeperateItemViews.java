/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.contacts.calllog;

import com.android.contacts.R;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Simple value object containing the various views within a call log entry.
 */
public final class CallLogListDateSeperateItemViews {

    /** The text of date. */
    public final TextView date;
    /** The divider to be shown below items. */
    public final View bottomDivider;

    private CallLogListDateSeperateItemViews(TextView date, View bottomDivider) {
        this.date = date;
        this.bottomDivider = bottomDivider;
    }

    public static CallLogListDateSeperateItemViews fromView(View view) {
        return new CallLogListDateSeperateItemViews(
                (TextView) view.findViewById(R.id.call_log_date),
                view.findViewById(R.id.call_log_divider));
    }

    public static CallLogListDateSeperateItemViews createForTest(Context context) {
        return new CallLogListDateSeperateItemViews(
                new TextView(context),
                new View(context));
    }
}
