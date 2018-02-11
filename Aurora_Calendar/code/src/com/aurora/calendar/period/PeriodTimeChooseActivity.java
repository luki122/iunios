package com.aurora.calendar.period;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.Time;

import com.android.calendar.R;
import com.android.calendar.Utils;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraNumberPicker;

/**
 * Created by joy on 3/30/15.
 */
public class PeriodTimeChooseActivity extends AuroraActivity {

    private AuroraNumberPicker periodPicker;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.period_time_choose, AuroraActionBar.Type.Normal, false);

        periodPicker = (AuroraNumberPicker) findViewById(R.id.period_picker);
        sp = getSharedPreferences(Utils.PERIOD_SP, Context.MODE_PRIVATE);

        periodPicker.setMinValue(1);
        periodPicker.setMaxValue(15);
        periodPicker.setValue(sp.getInt(Utils.PERIOD_TIME, 5));
        periodPicker.setLabel(getString(R.string.day_view));

        getAuroraActionBar().setTitle(R.string.period_time_title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        int oldPeriod = sp.getInt(Utils.PERIOD_TIME, 4);
        int currentPeriod = periodPicker.getValue();
        if (oldPeriod != currentPeriod) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(Utils.PERIOD_TIME, currentPeriod);
            editor.commit();

            handlePeriodDaysChange(currentPeriod);
        }
    }

    private void handlePeriodDaysChange(int defaultPeriodDays) {
        Time now = new Time();
        now.setToNow();
        now.normalize(true);
        int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);

        PeriodInfoAdapter periodAdaper = new PeriodInfoAdapter(this);
        periodAdaper.open();

        PeriodInfo info = periodAdaper.queryByJulianDay(todayJulianDay);
        if (info != null) {
            int startDay = info.getStartDay();
            int finishDay = info.getFinishDay();
            if (finishDay != startDay + defaultPeriodDays - 1) {
                info.setFinishDay(startDay + defaultPeriodDays - 1);
                periodAdaper.update(info);
            }
        }

        periodAdaper.close();
        periodAdaper = null;
    }

}