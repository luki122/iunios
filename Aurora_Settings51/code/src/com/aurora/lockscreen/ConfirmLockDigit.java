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

package com.aurora.lockscreen;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.PrivacySettings;
import com.android.settings.R;

import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceActivity;

public class ConfirmLockDigit extends AuroraPreferenceActivity {

    public boolean needExitOnPause = false;
    public static final String NEED_PAUSE_EXIT = "need_pause_exit";
    public static final String MSG_TITLE = "title_msg";

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        needExitOnPause = modIntent.getBooleanExtra(NEED_PAUSE_EXIT, false);
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, ConfirmLockDigitFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }*/
        // Disable IME on our window since we provide our own keyboard
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        //WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        super.onCreate(savedInstanceState);
        //CharSequence msg = getText(tilteMsgId);
        //showBreadCrumbs("hello", "hello");
        //getAuroraActionBar().setTitle(msg.toString());
        //getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (needExitOnPause) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ConfirmLockDigitFragment extends Fragment implements OnClickListener,
            OnEditorActionListener, TextWatcher {
        private static final String KEY_NUM_WRONG_CONFIRM_ATTEMPTS
                = "confirm_lock_password_fragment.key_num_wrong_confirm_attempts";
        private static final long ERROR_MESSAGE_TIMEOUT = 500;
        private TextView mPasswordEntry;
        private LockPatternUtils mLockPatternUtils;
        private TextView mHeaderText;
        private ShowDigitView mShowDigitView;
        private Handler mHandler = new Handler();
        private static final int SHOW_DIGIT_ERROR = 5;
        private int mNumWrongConfirmAttempts;
        private CountDownTimer mCountdownTimer;
        private RelativeLayout numKeyView;


        // required constructor for fragments
        public ConfirmLockDigitFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new LockPatternUtils(getActivity());
            if (savedInstanceState != null) {
                mNumWrongConfirmAttempts = savedInstanceState.getInt(
                        KEY_NUM_WRONG_CONFIRM_ATTEMPTS, 0);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.confirm_lock_digit, null);
            // Disable IME on our window since we provide our own keyboard

            mPasswordEntry = (TextView) view.findViewById(R.id.digit_entry);
            mPasswordEntry.setOnEditorActionListener(this);
            mPasswordEntry.addTextChangedListener(this);

            mShowDigitView = (ShowDigitView) view.findViewById(R.id.show_digit_view);
            numKeyView = (RelativeLayout) view.findViewById(R.id.num_key);
            // The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
            // not a separate view
            View pinDelete = view.findViewById(R.id.delete_button);
            if (pinDelete != null) {
                pinDelete.setVisibility(View.VISIBLE);
                pinDelete.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        // check for time-based lockouts
                        if (mPasswordEntry.isEnabled()) {
                            CharSequence str = mPasswordEntry.getText();
                            if (str.length() > 0) {
                                mPasswordEntry.setText(str.subSequence(0, str.length() - 1));
                            }
                        }
                    }
                });
                pinDelete.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        // check for time-based lockouts
                        if (mPasswordEntry.isEnabled()) {
                            mPasswordEntry.setText("");
                        }
                        return true;
                    }
                });
            }

            mHeaderText = (TextView) view.findViewById(R.id.headerText);
            mHeaderText.setText(R.string.lockpassword_confirm_digit_title);

            final Activity activity = getActivity();

            mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

            // Update the breadcrumb (title) if this is embedded in a PreferenceActivity
            if (activity instanceof AuroraPreferenceActivity) {
                final AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
                int id = getActivity().getIntent().getIntExtra(MSG_TITLE, R.string.set_lock_text_title);
                CharSequence title = getText(id);
                preferenceActivity.showBreadCrumbs(title, title);
            }

            return view;
        }

        @Override
        public void onPause() {
            super.onPause();
            mShowDigitView.requestFocus();
            if (mCountdownTimer != null) {
                mCountdownTimer.cancel();
                mCountdownTimer = null;
            }
        }

        @Override
        public void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            mShowDigitView.requestFocus();
            long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
            if (deadline != 0) {
                handleAttemptLockout(deadline);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(KEY_NUM_WRONG_CONFIRM_ATTEMPTS, mNumWrongConfirmAttempts);
        }

        private void handleNext() {
            mPasswordEntry.setEnabled(false);
            final String pin = mPasswordEntry.getText().toString();
            if (mLockPatternUtils.checkPassword(pin)) {

                Intent intent = new Intent();
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, pin);

                getActivity().setResult(AuroraActivity.RESULT_OK, intent);
                // qy 2014 04 04
                getActivity().sendBroadcast(new Intent(PrivacySettings.ACTION_CONFIRM_KEY));
                getActivity().finish();
            } else {
                if (++mNumWrongConfirmAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                    handleAttemptLockout(deadline);
                } else {
                    showError(R.string.lockpassword_confirm_passwords_dont_match);
                }
            }
        }

        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            showError(R.string.lockpattern_too_many_failed_confirmation_attempts_header, 0);
            mPasswordEntry.setEnabled(false);
            numKeyView.setVisibility(View.INVISIBLE);
            //numKeyView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mCountdownTimer = new CountDownTimer(
                    elapsedRealtimeDeadline - elapsedRealtime,
                    LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

                @Override
                public void onTick(long millisUntilFinished) {
                    ///M: CR ALPS01808515. Avoid fragment did not attach to activity.
                    if (!isAdded()) return;
                    final int secondsCountdown = (int) (millisUntilFinished / 1000);
                    mHeaderText.setText(getString(
                            R.string.lockpattern_too_many_failed_confirmation_attempts_footer,
                            secondsCountdown));
                }

                @Override
                public void onFinish() {
                    mPasswordEntry.setEnabled(true);
                    numKeyView.setVisibility(View.VISIBLE);
                    //numKeyView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                    mHeaderText.setText(getDefaultHeader());
                    mNumWrongConfirmAttempts = 0;
                }
            }.start();
        }

        private final Runnable mResetErrorRunnable = new Runnable() {
            public void run() {
                mHeaderText.setText(getDefaultHeader());
            }
        };

        private int getDefaultHeader() {
            return R.string.lockpassword_confirm_your_password_header;
        }

        private void showError(int msg, long timeout) {
            mHeaderText.setText(msg);
            mHeaderText.announceForAccessibility(mHeaderText.getText());
            mPasswordEntry.setText(null);
            mHandler.removeCallbacks(mResetErrorRunnable);
            if (timeout != 0) {
                mHandler.postDelayed(mResetErrorRunnable, timeout);
            }
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next_button:
                    handleNext();
                    break;

                case R.id.cancel_button:
                    getActivity().setResult(AuroraActivity.RESULT_CANCELED);
                    getActivity().finish();
                    break;
            }
        }

        private void showError(int msg) {
            mHeaderText.setText(msg);
            mHeaderText.announceForAccessibility(mHeaderText.getText());
            if (mShowDigitView != null) {
                mShowDigitView.onTextChange(SHOW_DIGIT_ERROR);
            }
            mPasswordEntry.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    mHeaderText.setText(R.string.lockpassword_confirm_your_password_header);
                    mPasswordEntry.setText(null);
                    mPasswordEntry.setEnabled(true);
                }
            }, ERROR_MESSAGE_TIMEOUT);
        }

        // {@link OnEditorActionListener} methods.
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Check if this was the result of hitting the enter or "done" key
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext();
                return true;
            }
            return false;
        }

        // {@link TextWatcher} methods.
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mShowDigitView != null && s.length() < 5) {
                mShowDigitView.onTextChange(s.length());
            }
            if (s.length() == 4) {
                handleNext();
            }
        }
    }
}
