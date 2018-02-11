/** 
 * Copyright (c) 2012 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import aurora.app.AuroraActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraDatePicker.OnDateChangedListener;

import com.android.calendar.R;

public class ChooseDateActivity extends AuroraActivity implements OnClickListener, OnDateChangedListener {
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_MONTH = "month";

    private AuroraDatePicker mPicker;
    private AuroraButton mCancel;
    private AuroraButton mOk;

    private int mYear;
    private int mMonth;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (bundle == null) {
            Intent intent = getIntent();
            Time time = new Time();
            time.setToNow();
            mYear = intent.getIntExtra(EXTRA_YEAR, time.year);
            mMonth = intent.getIntExtra(EXTRA_MONTH, time.month);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gn_widget_choose_date);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                              WindowManager.LayoutParams.WRAP_CONTENT);

        mCancel = (AuroraButton) findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        mOk = (AuroraButton) findViewById(R.id.ok);
        mOk.setOnClickListener(this);

        mPicker = (AuroraDatePicker) findViewById(R.id.picker);
        mPicker.init(mYear, mMonth, 1, this);
    }

    /**
     * Caused by we didn't want it could go to background.
     * So we will finish it if it is onPause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ok) {
            // When click ok button, the user may be edit the
            // year or month, we should get the update year
            // and month and set to the widget.
            if (mPicker != null) {
                mPicker.clearFocus();
                mYear = mPicker.getYear();
                mMonth = mPicker.getMonth();
            }
            WidgetManager.setDate(this, mYear, mMonth);
        }
        finish();
    }

    @Override
    public void onDateChanged(AuroraDatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
    }

}
