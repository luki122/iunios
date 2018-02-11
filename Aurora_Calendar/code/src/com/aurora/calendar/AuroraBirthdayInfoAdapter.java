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

package com.aurora.calendar;

import com.android.calendar.R;
import com.android.calendar.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.util.Locale;
import java.util.TimeZone;

public class AuroraBirthdayInfoAdapter extends ResourceCursorAdapter {

    private Context mContext;
    private long selectMillis;

    private LayoutInflater mInflater;

    public AuroraBirthdayInfoAdapter(Context context, int resource, long millis) {
        super(context, resource, null, true);
        mContext = context;
        selectMillis = millis;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    	/*RelativeLayout customView = (RelativeLayout) view.findViewById(com.aurora.R.id.aurora_listview_front);
        View childView = mInflater.inflate(R.layout.aurora_birthday_info_item, null);
        if (customView.getChildCount() > 0) {
            customView.removeViewAt(0);
        }
        customView.addView(childView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));*/

        String eventName = cursor.getString(AuroraBirthdayInfoActivity.PROJECTION_TITLE_INDEX);
        long startMillis = cursor.getLong(AuroraBirthdayInfoActivity.PROJECTION_DTSTART_INDEX);
        updateView(context, view, eventName, startMillis);
    }

    private void updateView(Context context, View view, String eventName, long startMillis) {
        Resources res = context.getResources();

        TextView titleView = (TextView) view.findViewById(R.id.birthday_info_title);
        TextView contentView = (TextView) view.findViewById(R.id.birthday_info_content);
        ImageView wishView = (ImageView) view.findViewById(R.id.birthday_info_wish);

        // What
        if (eventName == null || eventName.length() == 0) {
            eventName = res.getString(R.string.no_title_label);
        }
        titleView.setText(eventName);

        Time time = new Time();
        time.set(selectMillis);
        time.normalize(true);
        int thisYear = time.year;

        time.set(startMillis);
        time.normalize(true);
        int birthYear = time.year;

        contentView.setText(res.getString(R.string.aurora_birthday_content, thisYear - birthYear));

        wishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.aurora_happy_birthday));
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.aurora_happy_birthday));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.aurora_wish_birthday)));
            }
        });
    }
}
