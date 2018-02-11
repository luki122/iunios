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

import com.android.contacts.PhoneCallDetailsViews;

import com.android.contacts.R;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * Simple value object containing the various views within a call log entry.
 */
public final class CallLogMultipleDeleteListItemViews {
    /** The check box for deleting selection */
    public final CheckBox deleteSelectCheckBox;
    /** The quick contact badge for the contact. */
    public final QuickContactBadge quickContactView;
    /** The divider between the primary and secondary actions. */
//    public final View dividerView;
    /** The details of the phone call. */
    public final PhoneCallDetailsViews phoneCallDetailsViews;
    /** The text of the header of a section. */
    public final View bottomDivider;

    private CallLogMultipleDeleteListItemViews(CheckBox deleteSelectCheckBox,
                                               QuickContactBadge quickContactView,
//                                               View dividerView,
                                               PhoneCallDetailsViews phoneCallDetailsViews,
                                               View bottomDivider) {
        this.deleteSelectCheckBox = deleteSelectCheckBox;
        this.quickContactView = quickContactView;
//        this.dividerView = dividerView;
        this.phoneCallDetailsViews = phoneCallDetailsViews;
        this.bottomDivider = bottomDivider;
    }

    public static CallLogMultipleDeleteListItemViews fromView(View view) {
        return new CallLogMultipleDeleteListItemViews(
                (CheckBox) view.findViewById(R.id.delete_select_box),
                (QuickContactBadge) view.findViewById(R.id.quick_contact_photo),
//                view.findViewById(R.id.divider),
                PhoneCallDetailsViews.fromView(view),
                view.findViewById(R.id.call_log_divider));
    }

    public static CallLogMultipleDeleteListItemViews createForTest(Context context) {
        return new CallLogMultipleDeleteListItemViews(
                new CheckBox(context),
                new QuickContactBadge(context),
//                new View(context),
                PhoneCallDetailsViews.createForTest(context),
                new View(context));
    }
}
