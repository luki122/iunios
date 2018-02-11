/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats;
import android.os.SystemClock;
import aurora.preference.AuroraPreference;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;

/**
 * Custom preference for displaying power consumption as a bar and an icon on the left for the
 * subsystem/app type.
 *
 */
public class BatteryHistoryPreference extends AuroraPreference {

    private BatteryStats mStats;
    private String mDurationString = null;

    public BatteryHistoryPreference(Context context, BatteryStats stats) {
        super(context);
        mStats = stats;
        // qy modify
//        setLayoutResource(R.layout.preference_batteryhistory);      
        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000,
                BatteryStats.STATS_SINCE_CHARGED);        
        String durationString = Utils.formatElapsedTime(context, uSecTime / 1000);
        Log.i("qy", "********"+ durationString);
        mDurationString = context.getString(R.string.battery_stats_on_battery,
                durationString);
    }

    BatteryStats getStats() {
        return mStats;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        /*BatteryHistoryChart chart = (BatteryHistoryChart)view.findViewById(
                R.id.battery_history_chart);
        chart.setStats(mStats);*/
        TextView tv = (TextView) view.findViewById(android.R.id.title);
        if(tv != null && mDurationString != null){
        	tv.setText(mDurationString);
        }
        setTitle("test");
        Log.i("qy", "********"+mDurationString);

    }
}
