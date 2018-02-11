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

package com.android.settings.securitypasswd;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;

public class AuroraPasswordSetDialogActivity extends AuroraActivity {
    static final String TAG = "AuroraPasswordSetDialogActivity";
    private LockPatternUtils mLockPatternUtils;

    public static final int CONFIRM_PASSWORD_FAIL = -1;
    public static final int CONFIRM_PASSWORD_SUCCESS = 0;
    public static final int CONFIRM_PASSWORD_CANCEL = 1;

    private static final int SET_NEW_PASSWORD_REQUEST = 100;
    private static final int CONFIRM_EXISTING_REQUEST = 101;
    public static final String EXTRA_KEY_CONFIRM_RESULT = "confirm_result";

    private boolean isAppConfirmPwd = false;
    //private View mPasswordScreen;
    private boolean mDialogClickOk = false;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mLockPatternUtils = new LockPatternUtils(this);

        Intent modIntent = getIntent();
        String actionStr = modIntent.getAction();
        Log.i(TAG, "AuroraPasswordSetDialogActivity actionStr=" + actionStr);
        if ("aurora.intent.action.CONFIRM_PASSWORD".equals(actionStr)) {
            isAppConfirmPwd = true;
        }

        if (!SecurityUtils.isSecurityPasswordEnable(mLockPatternUtils) && isAppConfirmPwd) {
            //showSetPasswordDialog();
            startSecurityPasswordActivity();
        } else {
            Log.i(TAG, "AuroraPasswordSetDialogActivity oncreate");
            ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
            helper.launchConfirmationExitActivity(
                    CONFIRM_EXISTING_REQUEST, null, null);
            Log.e(TAG, "launchConfirmationActivity END");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_EXISTING_REQUEST) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
            } else {
                setResult(RESULT_CANCELED);
            }
        } else if (requestCode == SET_NEW_PASSWORD_REQUEST) {
            int result = data.getIntExtra(EXTRA_KEY_CONFIRM_RESULT, CONFIRM_PASSWORD_CANCEL);
            if (result == CONFIRM_PASSWORD_CANCEL) {
                setResult(RESULT_CANCELED);
            }
        }
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "AuroraPasswordSetDialogActivity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "AuroraPasswordSetDialogActivity onPause");
    }

    private void showSetPasswordDialog() {
        final Context context = this;
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        builder.setTitle(R.string.aurora_securitypassword_set_dialog_title)
                .setMessage(R.string.aurora_securitypassword_set_dialog_message)
                .setPositiveButton(R.string.aurora_securitypassword_set_password, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "AuroraPasswordSetDialogActivity aurora_dlg_ok");
                        //mPasswordScreen.setAlpha(ORIGINAL_VALUE);
                        //setTheme(R.style.AuroraSettingsLightTheme);
                        mDialogClickOk = true;
                        startSecurityPasswordActivity();

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "AuroraPasswordSetDialogActivity aurora_dlg_cancel");
            }
        }).setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "AuroraPasswordSetDialogActivity onDismiss");
                if (!mDialogClickOk) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }).show().setCanceledOnTouchOutside(false);

    }

    private void startSecurityPasswordActivity() {
        boolean extraData = getIntent().getBooleanExtra(SecurityUtils.APP_EXTRA_DATA, false);
        Intent intent = new Intent();
        intent.putExtra("isappconfirm", isAppConfirmPwd);
        intent.putExtra(SecurityUtils.APP_EXTRA_DATA, extraData);
        intent.setClass(this, AuroraSecurityPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

}
