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
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.fingerprint.AuroraFingerprintUtils;
import com.aurora.utils.AuroraUtils;
import com.gionee.fingerprint.IGnIdentifyCallback;
import com.mediatek.settings.sim.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraTextView;

/**
 * Launch this when you want the user to confirm their lock pattern.
 * <p/>
 * Sets an activity result of {@link Activity#RESULT_OK} when the user
 * successfully confirmed their pattern.
 */
public class AuroraConfirmLockPattern extends AuroraPreferenceActivity {
    private static final String TAG = "AuroraConfirmLockPattern";
    public static class InternalActivity extends AuroraConfirmLockPattern {

    }

    /**
     * Names of {@link CharSequence} fields within the originating {@link Intent}
     * that are used to configure the keyguard confirmation view's labeling.
     * The view will use the system-defined resource strings for any labels that
     * the caller does not supply.
     */
    public static final String PACKAGE = "com.android.settings";
    public static final String HEADER_TEXT = PACKAGE + ".ConfirmLockPattern.header";
    public static final String FOOTER_TEXT = PACKAGE + ".ConfirmLockPattern.footer";
    public static final String HEADER_WRONG_TEXT = PACKAGE + ".ConfirmLockPattern.header_wrong";
    public static final String FOOTER_WRONG_TEXT = PACKAGE + ".ConfirmLockPattern.footer_wrong";

    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate");
        CharSequence msg = getText(R.string.aurora_securitypassword_confirm_title);
        setTitle(msg);
    }

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, AuroraConfirmLockPatternFragment.class.getName());
        return modIntent;
    }

    public static class AuroraConfirmLockPatternFragment extends Fragment {

        private static final String TAG = "AuroraConfirmLockPatternFragment";
        // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;

        private static final String KEY_NUM_WRONG_ATTEMPTS = "num_wrong_attempts";

        private LockPatternView mLockPatternView;
        private AuroraLockPatternUtils mLockPatternUtils;
        private int mNumWrongConfirmAttempts;
        private CountDownTimer mCountdownTimer;

        private AuroraTextView mHeaderTextView;
        private AuroraTextView mFooterTextView;

        // caller-supplied text for various prompts
        private CharSequence mHeaderText;
        private CharSequence mFooterText;
        private CharSequence mHeaderWrongText;
        private CharSequence mFooterWrongText;
        /********************
         * fingerprint
         **************/
        private AuroraFingerprintUtils mFingerprintUtils;
        private int[] mFingerIdList;
        /********************
         * fingerprint
         **************/
        private boolean needReturnPassword = false;
        private static final int CHANGE_PASSWORD_REQUEST = 102;
        private AuroraTextView mForgetPassword;
        private Handler mHandler = new Handler();
        private Timer mTimer;

        // required constructor for fragments
        public AuroraConfirmLockPatternFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new AuroraLockPatternUtils(getActivity());
            /********************fingerprint**************/
            Log.d(TAG, "AuroraConfirmLockPattern onCreate");
            mFingerprintUtils = new AuroraFingerprintUtils();
            mFingerIdList = mFingerprintUtils.getIds();
            Log.d(TAG, "AuroraConfirmLockPattern mFingerIdList=" + mFingerIdList);
            /********************fingerprint**************/
            if (getActivity() instanceof AuroraConfirmLockPattern.InternalActivity) {
                needReturnPassword = true;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.aurora_confirm_lock_pattern, null);
            mHeaderTextView = (AuroraTextView) view.findViewById(R.id.headerText);
            mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
            mFooterTextView = (AuroraTextView) view.findViewById(R.id.footerText);
            mForgetPassword = (AuroraTextView) view.findViewById(R.id.forget_password);
            mForgetPassword.setOnClickListener(mForgetListener);

            // make it so unhandled touch events within the unlock screen go to the
            // lock pattern view.
            final LinearLayoutWithDefaultTouchRecepient topLayout
                    = (LinearLayoutWithDefaultTouchRecepient) view.findViewById(R.id.topLayout);
            topLayout.setDefaultTouchRecepient(mLockPatternView);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mHeaderText = intent.getCharSequenceExtra(HEADER_TEXT);
                mFooterText = intent.getCharSequenceExtra(FOOTER_TEXT);
                mHeaderWrongText = intent.getCharSequenceExtra(HEADER_WRONG_TEXT);
                mFooterWrongText = intent.getCharSequenceExtra(FOOTER_WRONG_TEXT);
            }

            mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());
            mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
            mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled());
            updateStage(Stage.NeedToUnlock);

            if (savedInstanceState != null) {
                mNumWrongConfirmAttempts = savedInstanceState.getInt(KEY_NUM_WRONG_ATTEMPTS);
            } else {
                // on first launch, if no lock pattern is set, then finish with
                // success (don't want user to get stuck confirming something that
                // doesn't exist).
                if (!mLockPatternUtils.savedPatternExists()) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
            return view;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            // deliberately not calling super since we are managing this in full
            outState.putInt(KEY_NUM_WRONG_ATTEMPTS, mNumWrongConfirmAttempts);
        }

        @Override
        public void onPause() {
            super.onPause();

            if (mCountdownTimer != null) {
                mCountdownTimer.cancel();
            }
            cancelIdentifyFinger();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;

            }
            mHandler.removeCallbacks(mStartIdentifyRunnable);
        }

        @Override
        public void onResume() {
            super.onResume();

            Intent pwdintent = getActivity().getIntent();
            if (pwdintent != null) {
                boolean isAppConfirmPwd = pwdintent.getBooleanExtra(SecurityUtils.APP_CONFIRM, false);
                if (isAppConfirmPwd) {
                    long currentType = SecurityUtils.getSecurityPasswordType(mLockPatternUtils);
                    if (currentType != SecurityUtils.SECURITYPWD_PATTERN_TYPE) {
                        Intent intent = new Intent();
                        intent.putExtra(SecurityUtils.NEED_CONFIRM_AGAIN, true);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                        return;

                    }
                }
            }

            // if the user is currently locked out, enforce it.
            long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
            if (!mLockPatternView.isEnabled()) {
                // The deadline has passed, but the timer was cancelled...
                // Need to clean up.
                mNumWrongConfirmAttempts = 0;
                updateStage(Stage.NeedToUnlock);
            }
            refreshScreen();
        }

        private void updateStage(Stage stage) {
            switch (stage) {
                case NeedToUnlock:
                    if (mHeaderText != null) {
                        mHeaderTextView.setText(mHeaderText);
                    } else {
                        mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                    }
                    if (mFooterText != null) {
                        mFooterTextView.setText(mFooterText);
                    } else {
                        mFooterTextView.setText(R.string.lockpattern_need_to_unlock_footer);
                    }

                    mLockPatternView.setEnabled(true);
                    mLockPatternView.enableInput();
                    break;
                case NeedToUnlockWrong:
                    if (mHeaderWrongText != null) {
                        mHeaderTextView.setText(mHeaderWrongText);
                    } else {

                        mHeaderTextView.setText(R.string.lockpattern_need_to_unlock_wrong);
                    }
                    if (mFooterWrongText != null) {
                        mFooterTextView.setText(mFooterWrongText);
                    } else {
                        mFooterTextView.setText(R.string.lockpattern_need_to_unlock_wrong_footer);
                    }

                    mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    mLockPatternView.setEnabled(true);
                    mLockPatternView.enableInput();
                    break;
                case LockedOut:
                    mLockPatternView.clearPattern();
                    // enabled = false means: disable input, and have the
                    // appearance of being disabled.
                    mLockPatternView.setEnabled(false); // appearance of being disabled
                    break;
            }

            // Always announce the header for accessibility. This is a no-op
            // when accessibility is disabled.
            mHeaderTextView.announceForAccessibility(mHeaderTextView.getText());
        }


        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                mLockPatternView.clearPattern();
            }
        };

        // clear the wrong pattern unless they have started a new one
        // already
        private void postClearPatternRunnable() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }

        /**
         * The pattern listener that responds according to a user confirming
         * an existing lock pattern.
         */
        private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
                = new LockPatternView.OnPatternListener() {

            public void onPatternStart() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCleared() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

            }

            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (mLockPatternUtils.checkPattern(pattern)) {

                    Intent intent = new Intent();
                    if (getActivity() instanceof AuroraConfirmLockPattern.InternalActivity) {
                        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE,
                                StorageManager.CRYPT_TYPE_PATTERN);
                        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD,
                                LockPatternUtils.patternToString(pattern));
                    }

                    confirmOkFinish(intent);
                } else {
                    updateStage(Stage.NeedToUnlockWrong);
                    postClearPatternRunnable();

                    if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                        showError();
                    }
                }
            }
        };


        private void showError() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            int number = addErrorNumber();
            String msg;
            long time;
            if (needDisplayForgetPwd(number)) {
                long currentTime = System.currentTimeMillis();
                Settings.Secure.putLong(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_TIME, currentTime);

                long min = SecurityUtils.getWaitInputPwdMinute(number);
                time = min * SecurityUtils.ONE_MIN_TO_MILLI;
                msg = getActivity().getString(R.string.aurora_securitypassword_retry_note, min);

                Log.e(TAG, " time-------" + time);
                cancelIdentifyFinger();
                showErrorDisplayForgetPwd(msg);

                setTimerTask(SecurityUtils.ONE_MIN_TO_MILLI, SecurityUtils.ONE_MIN_TO_MILLI);

            } else {
                msg = getActivity().getString(R.string.aurora_securitypassword_input_error_note,
                        AuroraUtils.START_TIMING_NUMBER - number);
                mHeaderTextView.setText(msg);
                setTimerTaskForHeader(WRONG_PATTERN_CLEAR_TIMEOUT_MS);

            }

        }

        private void showErrorDisplayForgetPwd(String msg) {
            mForgetPassword.setVisibility(View.VISIBLE);
            mLockPatternView.setVisibility(View.GONE);
            mLockPatternView.setEnabled(false);

            mHeaderTextView.setText(msg);
        }

        private void refreshScreen() {
            int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                    AuroraUtils.INPUT_ERROR_NUMBER, 0);
            if (needDisplayForgetPwd(number)) {
                mForgetPassword.setVisibility(View.VISIBLE);
                long min = SecurityUtils.getWaitInputPwdMinute(number);
                long waitTime = min * SecurityUtils.ONE_MIN_TO_MILLI;
                Log.e(TAG, "onResume waitTime-------" + waitTime);
                long durationTime;
                if ((durationTime = isCannotInputPwdTime(waitTime)) > 0) {
                    long remainTime = waitTime - durationTime;
                    long displayMin = getDisplayMinute(remainTime);
                    long firstTime = getFirstScheduleTime(remainTime);

                    String msg = getActivity().getString(R.string.aurora_securitypassword_retry_note, displayMin);
                    mHeaderTextView.setText(msg);

                    showErrorDisplayForgetPwd(msg);
                    setTimerTask(firstTime, SecurityUtils.ONE_MIN_TO_MILLI);

                } else {
                    displayInputScreen();
                }

            } else {
                mForgetPassword.setVisibility(View.GONE);
                setDefaultHeaderText();
                startIdentifyFinger(true);

            }

        }

        private void displayInputScreen() {
            setDefaultHeaderText();
            mLockPatternView.setVisibility(View.VISIBLE);
            mLockPatternView.setEnabled(true);
            Settings.Secure.putLong(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_TIME, 0);
            startIdentifyFinger(false);

        }

        private void setDefaultHeaderText() {
            if (canUseFingerPrint()) {
                mHeaderTextView.setText(R.string.aurora_fingerprint_confirm_your_password_header);
            } else {
                mHeaderTextView.setText(R.string.aurora_confirm_your_password_header);
            }
        }

        private long getDisplayMinute(long remainTime) {
            long displayMin;
            if ((remainTime % SecurityUtils.ONE_MIN_TO_MILLI) == 0) {
                displayMin = remainTime / SecurityUtils.ONE_MIN_TO_MILLI;

            } else {
                displayMin = remainTime / SecurityUtils.ONE_MIN_TO_MILLI + 1;
            }

            return displayMin;
        }

        private long getFirstScheduleTime(long remainTime) {
            long firstTime;
            if ((remainTime % SecurityUtils.ONE_MIN_TO_MILLI) == 0) {
                firstTime = SecurityUtils.ONE_MIN_TO_MILLI;

            } else {
                firstTime = remainTime % SecurityUtils.ONE_MIN_TO_MILLI;
            }
            return firstTime;

        }

        private boolean needDisplayForgetPwd(int number) {

            if (number >= AuroraUtils.START_TIMING_NUMBER) {
                return true;
            } else {
                return false;
            }
        }

        private long isCannotInputPwdTime(long waitTime) {
            long oldTime = Settings.Secure.getLong(getActivity().getContentResolver(),
                    AuroraUtils.INPUT_ERROR_TIME, 0);
            if (oldTime != 0) {
                long currentTime = System.currentTimeMillis();
                long durationTime = currentTime - oldTime;
                if (durationTime < waitTime) {
                    return durationTime;

                } else {
                    return 0;
                }
            }

            return 0;
        }

        private void confirmOkFinish(Intent intent) {
            Settings.Secure.putInt(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_NUMBER, 0);
            Settings.Secure.putLong(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_TIME, 0);

            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }

        private void confirmOkFinish() {
            Settings.Secure.putInt(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_NUMBER, 0);
            Settings.Secure.putLong(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_TIME, 0);

            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case CHANGE_PASSWORD_REQUEST:
                    if (resultCode == Activity.RESULT_OK) {
                        confirmOkFinish();
                    }
                    break;
                default:
                    break;
            }
        }

        private void setTimerTask(long firstTime, long scheduleTime) {
            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                            AuroraUtils.INPUT_ERROR_NUMBER, 0);
                    long min = SecurityUtils.getWaitInputPwdMinute(number);
                    long waitTime = min * SecurityUtils.ONE_MIN_TO_MILLI;

                    long durationTime;
                    if ((durationTime = isCannotInputPwdTime(waitTime)) > 0) {

                        Log.i(TAG, "setTimerTask durationTime-------" + durationTime);
                        //refresh wait time
                        long remainTime = waitTime - durationTime;
                        long displayMin = getDisplayMinute(remainTime);
                        Log.i(TAG, "setTimerTask displayMin-------" + displayMin);

                        final Message msg = mHandler.obtainMessage(AuroraFingerprintUtils.MSG_REFRESH_HEADER);
                        msg.arg1 = (int) displayMin;
                        mFingerHandler.sendMessage(msg);
                    } else {
                        //display input password screen
                        mFingerHandler.sendEmptyMessage(AuroraFingerprintUtils.MSG_REFRESH_SCREEN);
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                    }
                }
            }, firstTime, scheduleTime);
        }

        private void setTimerTaskForHeader(long time) {
            if (mTimer == null) {
                mTimer = new Timer();
            }

            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mFingerHandler.sendEmptyMessage(AuroraFingerprintUtils.MSG_DEFAULT_HEADER);
                }
            }, time);
        }

        private Runnable mStartIdentifyRunnable = new Runnable() {
            public void run() {
                Log.d("Fingerprint_Settings", "startIdentifyFinger()---for delay 500ms");
                mFingerprintUtils.startIdentify(identifyCb, mFingerIdList);
            }
        };

        private final View.OnClickListener mForgetListener = new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AuroraForgetActivity.class);
                startActivity(intent);
            }
        };

        private int addErrorNumber() {
            int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                    AuroraUtils.INPUT_ERROR_NUMBER, 0);

            if (number <= AuroraUtils.TOTAL_TIMING_NUMBER) {
                number++;
                Settings.Secure.putInt(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_NUMBER, number);
            }
            Log.e(TAG, " addErrorNumber number=" + number);
            return number;

        }

        private boolean canUseFingerPrint() {
            if (needReturnPassword) {
                return false;
            }
            if (mFingerIdList != null && mFingerIdList.length > 0) {
                return true;
            }
            return false;
        }

        /********************
         * fingerprint
         **************/
        private void startIdentifyFinger(boolean delay) {
            if (canUseFingerPrint()) {
                if (delay) {
                    mHandler.postDelayed(mStartIdentifyRunnable, 500);
                } else {
                    Log.d("Fingerprint_Settings", "startIdentifyFinger()---");
                    mFingerprintUtils.startIdentify(identifyCb, mFingerIdList);
                }
            }
        }

        private void cancelIdentifyFinger() {
            if (canUseFingerPrint()) {
                mFingerprintUtils.cancel();
            }
        }

        // identify callback
        private IGnIdentifyCallback identifyCb = new IGnIdentifyCallback() {

            public void onWaitingForInput() {

                Log.d("Fingerprint_Settings", "onWaitingForInput()---");
            }

            public void onInput() {
                Log.d("Fingerprint_Settings", "onInput()---");

            }

            public void onCaptureCompleted() {
                Log.d("Fingerprint_Settings", "onCaptureCompleted()---");

            }

            public void onCaptureFailed(int reason) {
                Log.d("Fingerprint_Settings", "onCaptureFailed()---");
            }

            public void onIdentified(int fingerId, boolean updated) {
                Log.d("Fingerprint_Settings", "onIdentified()---");
                confirmOkFinish();
            }

            public void onNoMatch(int reason) {
                Log.d("Fingerprint_Settings", "onNoMatch()---reason=" + reason);
                if (reason == AuroraFingerprintUtils.REASON_NOMATCH) {
                    showError();
                    int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                            AuroraUtils.INPUT_ERROR_NUMBER, 0);
                    if (!needDisplayForgetPwd(number)) {
                        mFingerHandler.sendEmptyMessageDelayed(AuroraFingerprintUtils.MSG_REIDENTIFY, 300);
                    }
                } else if (reason == AuroraFingerprintUtils.REASON_TIMEOUT) {

                    mFingerHandler.sendEmptyMessageDelayed(AuroraFingerprintUtils.MSG_REIDENTIFY, 300);

                }

            }

            public void onExtIdentifyMsg(Message msg, String description) {
                Log.d("Fingerprint_Settings", "onExtIdentifyMsg()---");
            }
        };

        private Handler mFingerHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AuroraFingerprintUtils.MSG_REIDENTIFY:
                        startIdentifyFinger(false);
                        break;

                    case AuroraFingerprintUtils.MSG_REFRESH_SCREEN:
                        Log.e(TAG, "AuroraFingerprintUtils.MSG_REFRESH_SCREEN");
                        displayInputScreen();
                        break;

                    case AuroraFingerprintUtils.MSG_REFRESH_HEADER:
                        int displayMin = msg.arg1;
                        String text = getActivity().getString(R.string.aurora_securitypassword_retry_note, displayMin);
                        mHeaderTextView.setText(text);
                        break;

                    case AuroraFingerprintUtils.MSG_DEFAULT_HEADER:
                        setDefaultHeaderText();
                        break;

                    default:
                        break;
                }

            }
        };
        /********************fingerprint**************/
    }

}
