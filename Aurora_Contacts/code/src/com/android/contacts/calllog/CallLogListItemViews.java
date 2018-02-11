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

package com.android.contacts.calllog;

import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.R;
import com.mediatek.contacts.calllog.CallLogListItemView;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * Simple value object containing the various views within a call log entry.
 */
public final class CallLogListItemViews {
    private static final String TAG = "CallLogListItemViews";

    public final TextView callLogDate;
    public final QuickContactBadge quickContactPhoto;
    public final TextView calllogName;
    public final TextView calllogNumber;
    public final com.android.contacts.calllog.CallTypeIconsView calllogType;
    public final TextView calllogCount;
    public final TextView calllogSimName;
    public final TextView calllogTime;
    public final ImageView calllogNewCall;
    
    public final PhoneCallDetailsViews phoneCallDetailsViews;

    private CallLogListItemViews(TextView callLogDateTextView,
            QuickContactBadge quickContactView,
            TextView calllogNameTextView,
            TextView calllogNumberTextView,
            com.android.contacts.calllog.CallTypeIconsView calllogTypeImageView,
            TextView calllogCountTextView,
            TextView calllogSimNameTextView,
            TextView calllogTimeTextView,
            ImageView calllogNewCallImage,
            PhoneCallDetailsViews phoneCallDetails) {
        this.callLogDate = callLogDateTextView;
        this.quickContactPhoto = quickContactView;
        this.calllogName = calllogNameTextView;
        this.calllogNumber = calllogNumberTextView;
        this.calllogType = calllogTypeImageView;
        this.calllogCount = calllogCountTextView;
        this.calllogSimName = calllogSimNameTextView;
        this.calllogTime = calllogTimeTextView;
        this.calllogNewCall = calllogNewCallImage;
        this.phoneCallDetailsViews = phoneCallDetails;
    }

    /*public static CallLogListItemViews fromCustomView(View view) {
        if (null == view) {
            Log.e(TAG, "Error!!! - fromCustomView() view is null!");
            return null;
        }

        if (!(view instanceof CallLogListItemView)) {
            Log.e(TAG, "Error!!! - fromCustomView() view is not CallLogListItemView!");
            return null;
        }
        CallLogListItemView itemView = (CallLogListItemView) view;
        return new CallLogListItemViews(
                itemView.getSectionDate(),
                itemView.getQuickContact(),
                itemView.getCallLogNameTextView(),
                itemView.getNumberTextView(),
                itemView.getCallTypeIconView(),
                itemView.getCallCountTextView(),
                itemView.getSimNameTextView(),
                itemView.getCallTimeTextView(),
                itemView.getCallButton(),
                PhoneCallDetailsViews.fromCustomView(view));
    }*/

    public static CallLogListItemViews createForTest(Context context) {
        return new CallLogListItemViews(
                new TextView(context),
                new QuickContactBadge(context),
                new TextView(context),
                new TextView(context),
                new CallTypeIconsView(context),
                new TextView(context),
                new TextView(context),
                new TextView(context),
                new ImageView(context),
                PhoneCallDetailsViews.createForTest(context));
        
    }
}
