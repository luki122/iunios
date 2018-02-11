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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.widget.AuroraActionBar;

public class AuroraSecurityPasswordActivity extends AuroraPreferenceActivity {
    static final String TAG = "GnSecurityPasswordActivity";
    private AuroraLockPatternUtils mLockPatternUtils;

    public static final int CONFIRM_PASSWORD_FAIL = -1;
    public static final int CONFIRM_PASSWORD_SUCCESS = 0;
    public static final int CONFIRM_PASSWORD_CANCEL = 1;

    private static final int SET_NEW_PASSWORD_REQUEST = 100;
    private static final int CONFIRM_EXISTING_REQUEST = 101;
    private static final int CHANGE_PASSWORD_REQUEST = 102;
    private static final int CHANGE_PASSWORD_SET_REQUEST = 103;
    private static final int VIEW_PRIVACY_REQUEST = 104;
    public static final String EXTRA_KEY_CONFIRM_RESULT = "confirm_result";

    private static final String KEY_SET_PASSWORD_CAT = "set_password_cat";
    private static final String KEY_CHANGE_PASSWORD = "change_password";
    private static final String KEY_USED_CAT = "used_cat";
    private static final String KEY_USED_FOR = "used_for";

    private static final float DISPLAY_NULL_VALUE = 0f;//50% of 0xff in transparent
    private static final float ORIGINAL_VALUE = 1f;

    private AuroraPreference mBindAmigoAccount;
    private boolean isAppConfirmPwd = false;
    private boolean mFinishExit = false;
    private Context mContext;

    private boolean mDialogClickOk = false;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mContext = this;
        isAppConfirmPwd = getIntent().getBooleanExtra("isappconfirm", false);

        mLockPatternUtils = new AuroraLockPatternUtils(this);

        setAuroraContentView(R.layout.aurora_securitypassword_activity, AuroraActionBar.Type.Normal);

        addPreferencesFromResource(R.xml.aurora_securitypassword_main);

        Log.e(TAG, "GnSecurityPasswordActivity onCreate mLockPatternUtils.savedPatternExists()=" + mLockPatternUtils.savedPatternExists());
        Log.e(TAG, "GnSecurityPasswordActivity onCreate mLockPatternUtils.savedPasswordExists()=" + mLockPatternUtils.savedPasswordExists());
        if (SecurityUtils.isSecurityPasswordEnable(mLockPatternUtils)) {
            mFinishExit = true;
            startConfirmPassword(CONFIRM_EXISTING_REQUEST);
        } else {
            mFinishExit = true;
            Intent intent = new Intent();
            intent.setClass(this, AuroraPasswordTypeActivity.class);
            startActivityForResult(intent, SET_NEW_PASSWORD_REQUEST);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "GnSecurityPasswordActivity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "GnSecurityPasswordActivity onPause mFinishExit=" + mFinishExit);
        if (!mFinishExit) {
            if (SecurityUtils.isSecurityPasswordEnable(mLockPatternUtils)) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
                                         AuroraPreference preference) {
        final String key = preference.getKey();

        if (KEY_CHANGE_PASSWORD.equals(key)) {
            mFinishExit = true;
            startConfirmPassword(CHANGE_PASSWORD_REQUEST);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent();
        Log.i(TAG, "GnSecurityPasswordActivity password requestCode=" + requestCode + " resultCode=" + resultCode);
        if (requestCode == CONFIRM_EXISTING_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                boolean confirmAgain = false;

                if (data != null) {
                    confirmAgain = data.getBooleanExtra(SecurityUtils.NEED_CONFIRM_AGAIN, false);
                }
                if (confirmAgain) {

                    mFinishExit = true;
                    startConfirmPassword(CONFIRM_EXISTING_REQUEST);
                } else {
                    boolean extraData = getIntent().getBooleanExtra(SecurityUtils.APP_EXTRA_DATA, false);
                    intent.putExtra(SecurityUtils.APP_EXTRA_DATA, extraData);
                    Log.i(TAG, "GnSecurityPasswordActivity password extraData=" + extraData);

                    intent.putExtra(EXTRA_KEY_CONFIRM_RESULT, CONFIRM_PASSWORD_SUCCESS);
                    setResult(RESULT_OK, intent);
                    if (isAppConfirmPwd) {
                        mFinishExit = true;
                        finish();
                    } else {
                        mFinishExit = false;
                    }
                }
            } else {
                intent.putExtra(EXTRA_KEY_CONFIRM_RESULT, CONFIRM_PASSWORD_CANCEL);
                setResult(RESULT_OK, intent);
                mFinishExit = true;
                finish();
            }

        } else if (requestCode == SET_NEW_PASSWORD_REQUEST) {
            Log.e("JOY", "requestCode " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                boolean extraData = getIntent().getBooleanExtra(SecurityUtils.APP_EXTRA_DATA, false);
                intent.putExtra(SecurityUtils.APP_EXTRA_DATA, extraData);

                setResult(RESULT_OK, intent);
                if (isAppConfirmPwd) {
                    mFinishExit = true;
                    finish();
                } else {
                    mFinishExit = false;
                }
            } else {
                mFinishExit = true;
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        } else if (requestCode == CHANGE_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mFinishExit = true;
                intent.setClass(this, AuroraPasswordTypeActivity.class);
                startActivityForResult(intent, SET_NEW_PASSWORD_REQUEST);
            } else {
                mFinishExit = false;
            }
        } else if (requestCode == VIEW_PRIVACY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            } else {
                mFinishExit = false;
            }
        }
    }

    private long getSecurityPasswordType() {
        return mLockPatternUtils.gnGetLong(SecurityUtils.SECURITYPASSWORD_TYPE, 0);
    }

    private void startConfirmPassword(int requestCode) {
        Intent intent = new Intent();
        long type = getSecurityPasswordType();

        LockPatternUtils lockPatternUtils = new LockPatternUtils(mContext);


        Log.e(TAG, "startConfirmPassword isAppConfirmPwd = " + isAppConfirmPwd + " type " + type);
        if (isAppConfirmPwd) {
            intent.putExtra(SecurityUtils.APP_CONFIRM, true);
        }
        if (type == SecurityUtils.SECURITYPWD_PATTERN_TYPE) {
            intent.setClass(this, AuroraConfirmLockPattern.class);
            startActivityForResult(intent, requestCode);
        } else if (type == SecurityUtils.SECURITYPWD_FOUR_NUMBER_TYPE) {
            intent.setClass(this, AuroraConfirmLockPasswordFour.class);
            startActivityForResult(intent, requestCode);
        }
    }

}
