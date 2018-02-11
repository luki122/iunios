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

package com.android.systemui.statusbar.util;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;

// Aurora <Felix.Duan> <2014-8-19> <NEW FILE> Add guidance on hiding navigation bar
/**
 * Helper class of navigation bar hide event.
 * Guide user how to recover from navigation bar hidden status.
 * 
 * Current using a text dialog. Show dialog on the first several @TIMES hide event.
 * 
 * @author Felix.Duan.
 * @date 2014-8-15
 */

public class NavBarHiddenHelper {

    // preference file
    private static final String PREF_NAME = "nav_bar_hidden_guidance";
    // init flag
    private static final String KEY_CREATED = "created";
    // guide times left
    private static final String KEY_COUNT = "guide_count";
    // default guide times
    private static final int TIMES = 2;

    Context mContext;
    SharedPreferences pref;

    public NavBarHiddenHelper(Context context) {
        mContext = context;

        pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean created = pref.getBoolean(KEY_CREATED, false);
        if (!created) {
            // init once
            pref.edit()
                .putInt(KEY_COUNT, TIMES)
                .putBoolean(KEY_CREATED, true)
                .commit();
        }
    }

    /**
     * Call this to show guidance on first several @TIMES navigation bar hide
     */
    public void nbHidden() {
        int count = pref.getInt(KEY_COUNT, -1);
        if (count > 0) {
            showGuidance();
            pref.edit()
                .putInt(KEY_COUNT, --count)
                .commit();
        }
    }

    private void showGuidance() {
        showDialog();
    }

    protected void showDialog() {
        // use standard aurora alert dialog
        AuroraAlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle(com.android.systemui.R.string.aurora_dialog_title_notice);
        builder.setMessage(com.android.systemui.R.string.aurora_nav_bar_hidden_msg);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AuroraAlertDialog dialog = builder.create();

        // set up window attrs
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.getWindow().getAttributes().privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;

        dialog.show();
    }
}
