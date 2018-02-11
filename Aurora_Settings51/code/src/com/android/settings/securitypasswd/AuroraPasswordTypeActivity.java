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

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyStore;
import android.util.MutableBoolean;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockPassword;
import com.android.settings.ChooseLockPattern;
import com.android.settings.R;
import com.aurora.lockscreen.ChooseLockDigit;
import com.aurora.utils.AuroraUtils;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.widget.AuroraActionBar;

public class AuroraPasswordTypeActivity extends AuroraPreferenceActivity {

    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final String KEY_UNLOCK_SET_PIN = "unlock_set_pin";
    private static final String KEY_UNLOCK_SET_PASSWORD_FOUR = "unlock_set_password_four";
    private static final String KEY_UNLOCK_SET_PATTERN = "unlock_set_pattern";

    private static final int CONFIRM_EXISTING_REQUEST = 100;
    private static final int FALLBACK_REQUEST = 101;
    private static final int ENABLE_ENCRYPTION_REQUEST = 102;
    private static final String PASSWORD_CONFIRMED = "password_confirmed";

    private static final String WAITING_FOR_CONFIRMATION = "waiting_for_confirmation";
    private static final String FINISH_PENDING = "finish_pending";
    public static final String MINIMUM_QUALITY_KEY = "minimum_quality";
    public static final String ENCRYPT_REQUESTED_QUALITY = "encrypt_requested_quality";
    public static final String ENCRYPT_REQUESTED_DISABLED = "encrypt_requested_disabled";

    private static final boolean ALWAY_SHOW_TUTORIAL = true;
    private static final int CREATE_PASSWD = 101;

    private DevicePolicyManager mDPM;
    private KeyStore mKeyStore;
    private boolean mPasswordConfirmed = false;
    private boolean mWaitingForConfirmation = false;
    private boolean mFinishPending = false;
    private int mEncryptionRequestQuality;
    private boolean mEncryptionRequestDisabled;
    private boolean mRequirePassword;
    private LockPatternUtils mLockPatternUtils;
    private boolean mFinishExit = false;
    public static final String CONFIRM_CREDENTIALS = "confirm_credentials";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.aurora_securitypassword_picker_layout, AuroraActionBar.Type.Normal);

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mKeyStore = KeyStore.getInstance();
        mLockPatternUtils = new LockPatternUtils(this);
        addPreferencesFromResource(R.xml.aurora_securitypassword_picker);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mFinishExit) {
            finish();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
                                         AuroraPreference preference) {
        final String key = preference.getKey();
        boolean handled = true;

        if (KEY_UNLOCK_SET_PATTERN.equals(key)) {
            updateUnlockMethodAndFinish(
                    DevicePolicyManager.PASSWORD_QUALITY_SOMETHING, false);
        } else if (KEY_UNLOCK_SET_PASSWORD_FOUR.equals(key)) {
            updateUnlockMethodAndFinish(
                    AuroraUtils.PASSWORD_QUALITY_NUMERIC_FOUR, false);
        } else {
            handled = false;
        }
        return handled;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    /**
     * increases the quality if necessary, and returns whether biometric is allowed
     */
    private int upgradeQuality(int quality, MutableBoolean allowBiometric) {
        quality = upgradeQualityForDPM(quality);
        quality = upgradeQualityForKeyStore(quality);
        return quality;
    }

    private int upgradeQualityForDPM(int quality) {
        // Compare min allowed password quality
        int minQuality = mDPM.getPasswordQuality(null);
        if (quality < minQuality) {
            quality = minQuality;
        }
        return quality;
    }

    private int upgradeQualityForKeyStore(int quality) {
        if (!mKeyStore.isEmpty()) {
            //Gionee <chenml> <2014-12-01> modify for CR01405905 begin
            if (quality < SecurityUtils.MIN_PASSWORD_QUALITY
                    && (quality != AuroraUtils.PASSWORD_QUALITY_NUMERIC_FOUR)) {
                quality = SecurityUtils.MIN_PASSWORD_QUALITY;
            }
            //Gionee <chenml> <2014-12-01> modify for CR01405905 end
        }
        return quality;
    }


    /**
     * Invokes an activity to change the user's pattern, password or PIN based on given quality
     * and minimum quality specified by DevicePolicyManager. If quality is
     * {@link DevicePolicyManager#PASSWORD_QUALITY_UNSPECIFIED}, password is cleared.
     *
     * @param quality  the desired quality. IAuroraored if DevicePolicyManager requires more security
     * @param disabled whether or not to show LockScreen at all. Only meaningful when quality is
     *                 {@link DevicePolicyManager#PASSWORD_QUALITY_UNSPECIFIED}
     */
    void updateUnlockMethodAndFinish(int quality, boolean disabled) {

        quality = upgradeQuality(quality, null);
        mFinishExit = true;
        if (quality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
            Intent intent = new Intent(this, ChooseLockPattern.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        } else if (quality == AuroraUtils.PASSWORD_QUALITY_NUMERIC_FOUR) {
            Intent intent = new Intent().setClass(this, ChooseLockDigit.class);
            intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
            intent.putExtra(ChooseLockDigit.PASSWORD_MIN_KEY, 4);
            intent.putExtra(ChooseLockDigit.PASSWORD_MAX_KEY, 4);
            intent.putExtra(CONFIRM_CREDENTIALS, false);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            intent.putExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK, false);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    private Intent getLockPasswordIntent(Context context, int quality,
                                         final boolean isFallback, int minLength, final int maxLength,
                                         boolean requirePasswordToDecrypt, boolean confirmCredentials) {
        return ChooseLockPassword.createIntent(context, quality,
                isFallback, minLength, maxLength, requirePasswordToDecrypt,
                confirmCredentials);
    }

}
