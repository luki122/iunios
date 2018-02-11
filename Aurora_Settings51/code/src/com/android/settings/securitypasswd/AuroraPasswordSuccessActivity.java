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

package com.android.settings.securitypasswd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;


public class AuroraPasswordSuccessActivity extends AuroraActivity {
    static final String TAG = "AuroraPasswordSuccessActivity";

    private AuroraLockPatternUtils mLockPatternUtils;

    public static final String EXTRA_PASSWORD_QUALITY = "extra_password_quality";
    public static final String EXTRA_PASSWORD_VALUE = "extra_password_value";
    public static final String EXTRA_PATTERN_VALUE = "extra_pattern_value";

    private AuroraButton mNoBindFinsh;

    private boolean mFinishExit = false;

    private final AuroraButton.OnClickListener mSuccessListener = new AuroraButton.OnClickListener() {

        public void onClick(View v) {
            if (v == mNoBindFinsh) {
                mFinishExit = true;
                finishActivity();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockPatternUtils = new AuroraLockPatternUtils(this);
        setAuroraContentView(R.layout.aurora_securitypassword_success, AuroraActionBar.Type.Normal);

        mNoBindFinsh = (AuroraButton) findViewById(R.id.no_bind_finish);
        mNoBindFinsh.setOnClickListener(mSuccessListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "AuroraPasswordSuccessActivity onPause");
        if (!mFinishExit) {
            finish();
        }
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(AuroraSecurityPasswordActivity.EXTRA_KEY_CONFIRM_RESULT,
                AuroraSecurityPasswordActivity.CONFIRM_PASSWORD_SUCCESS);
        setResult(RESULT_OK, intent);
        finish();
    }

}
