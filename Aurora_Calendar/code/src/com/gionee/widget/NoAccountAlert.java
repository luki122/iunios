/** 
 * Copyright (c) 2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import aurora.app.AuroraActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import aurora.widget.AuroraButton;

import com.android.calendar.R;

public class NoAccountAlert extends AuroraActivity implements OnClickListener {

    private AuroraButton mCancel;
    private AuroraButton mAddAccount;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gn_widget_no_account);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                              WindowManager.LayoutParams.WRAP_CONTENT);

        mCancel = (AuroraButton) findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        mAddAccount = (AuroraButton) findViewById(R.id.add_account);
        mAddAccount.setOnClickListener(this);
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
        if (v.getId() == R.id.add_account) {
            Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] { CalendarContract.AUTHORITY });
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(nextIntent);
        }
        finish();
    }
}
