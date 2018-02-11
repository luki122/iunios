package com.aurora.calendar.period;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.calendar.R;
import com.android.calendar.Utils;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraNumberPicker;

/**
 * Created by joy on 3/30/15.
 */
public class PeriodCycleChooseActivity extends AuroraActivity {

    private AuroraNumberPicker periodPicker;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.period_cycle_choose, AuroraActionBar.Type.Normal, false);

        getAuroraActionBar().setTitle(R.string.period_cycle_title);
        periodPicker = (AuroraNumberPicker) findViewById(R.id.cycle_picker);
        sp = getSharedPreferences(Utils.PERIOD_SP, Context.MODE_PRIVATE);

        periodPicker.setMinValue(1);
        periodPicker.setMaxValue(90);
        periodPicker.setValue(sp.getInt(Utils.PERIOD_CYCLE, 28));
        periodPicker.setLabel(getString(R.string.day_view));
    }

    @Override
    protected void onPause() {
        super.onPause();
        int oldPeriod = sp.getInt(Utils.PERIOD_CYCLE, 28);
        int currentPeriod = periodPicker.getValue();
        if (oldPeriod != currentPeriod) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(Utils.PERIOD_CYCLE, currentPeriod);
            editor.commit();
        }

    }
}