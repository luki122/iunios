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

package com.android.settings.lockscreen;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.GnSettingsUtils;
import com.android.settings.R;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreferenceActivity;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ImageView;
import android.content.Context;

public class ChooseLockDigit extends AuroraPreferenceActivity {
    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, ChooseLockDigitFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO: Fix on phones
        // Disable IME on our window since we provide our own keyboard
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        // WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.lockpassword_choose_your_digit_title);
        showBreadCrumbs(msg, msg);
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
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

    public static class ChooseLockDigitFragment extends Fragment implements OnClickListener,
            OnEditorActionListener, TextWatcher {
        private static final String KEY_FIRST_PIN = "first_pin";
        private static final String KEY_UI_STAGE = "ui_stage";
        private TextView mPasswordEntry;
        private int mPasswordMinLength = 4;
        private int mPasswordMaxLength = 16;
        private LockPatternUtils mLockPatternUtils;
        private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private Stage mUiStage = Stage.Introduction;
        private TextView mHeaderText;
        private String mFirstPin;
        private ShowDigitView mShowDigitView;
        private static final int CONFIRM_EXISTING_REQUEST = 58;
        static final int RESULT_FINISHED = RESULT_FIRST_USER;
        private static final long ERROR_MESSAGE_TIMEOUT = 3000;
        private static final int MSG_SHOW_ERROR = 1;
        private static final int MSG_PASSWORD_CLEAR = 2;
        private static final int MSG_CONFIRM_PASSWORD = 3;
        private static final int MSG_SAVE_PASSWORD = 4;
        private static final int SHOW_DIGIT_ERROR = 5;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_ERROR) {
                    updateStage(( Stage ) msg.obj);
                } else if (msg.what == MSG_PASSWORD_CLEAR) {
                    mPasswordEntry.setText("");
                    mPasswordEntry.setEnabled(true);
                } else if (msg.what == MSG_CONFIRM_PASSWORD) {
                    handleNext();
                } else if (msg.what == MSG_SAVE_PASSWORD) {
                    mPasswordEntry.setEnabled(true);
                    getActivity().finish();
                }
            }
        };

        /**
         * Keep track internally of where the user is in choosing a pattern.
         */
        protected enum Stage {

            Introduction(R.string.lockpassword_choose_your_digit_header),

            NeedToConfirm(R.string.lockpassword_confirm_your_digit_header),

            ConfirmWrong(R.string.lockpassword_again_confirm_passwords_dont_match);

            /**
             * @param headerMessage
             *            The message displayed at the top.
             */
            Stage(int hintInNumeric) {
                this.numericHint = hintInNumeric;
            }

            public final int numericHint;
        }

        // required constructor for fragments
        public ChooseLockDigitFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new LockPatternUtils(getActivity());
            Intent intent = getActivity().getIntent();
//            mRequestedQuality = Math.max(intent.getIntExtra(LockPatternUtils.PASSWORD_TYPE_KEY,
//                    mRequestedQuality), mLockPatternUtils.getRequestedPasswordQuality());
            mPasswordMinLength = Math.max(intent.getIntExtra(PASSWORD_MIN_KEY, mPasswordMinLength),
                    mLockPatternUtils.getRequestedMinimumPasswordLength());
            mPasswordMaxLength = intent.getIntExtra(PASSWORD_MAX_KEY, mPasswordMaxLength);

            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.choose_lock_digit, null);

            mPasswordEntry = ( TextView ) view.findViewById(R.id.digit_entry);
            mPasswordEntry.setOnEditorActionListener(this);
            mPasswordEntry.addTextChangedListener(this);

            final Activity activity = getActivity();

            mHeaderText = ( TextView ) view.findViewById(R.id.headerText);

            mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

            Intent intent = getActivity().getIntent();
            final boolean confirmCredentials = intent.getBooleanExtra("confirm_credentials", true);
            if (savedInstanceState == null) {
                updateStage(Stage.Introduction);
                if (confirmCredentials) {
                    mChooseLockSettingsHelper
                            .launchConfirmationActivity(CONFIRM_EXISTING_REQUEST, null, null);
                }
            } else {
                mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
                final String state = savedInstanceState.getString(KEY_UI_STAGE);
                if (state != null) {
                    mUiStage = Stage.valueOf(state);
                    updateStage(mUiStage);
                }
            }
            // Update the breadcrumb (title) if this is embedded in a PreferenceActivity
            if (activity instanceof AuroraPreferenceActivity) {
                final AuroraPreferenceActivity preferenceActivity = ( AuroraPreferenceActivity ) activity;
                int id = R.string.lockpassword_choose_your_digit_title;
                CharSequence title = getText(id);
                preferenceActivity.showBreadCrumbs(title, title);
            }

            mShowDigitView = ( ShowDigitView ) view.findViewById(R.id.show_digit_view);
            // The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
            // not a separate view
            ImageView pinDelete = (ImageView)view.findViewById(R.id.delete_button);
            if ("CN".equals(getResources().getConfiguration().locale.getCountry())) {
                pinDelete.setImageResource(R.drawable.lockscreen_del_button);
            } else {
                pinDelete.setImageResource(R.drawable.lockscreen_del_eng_button);
            }
			
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

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateStage(mUiStage);
        }

        @Override
        public void onPause() {
            mHandler.removeMessages(MSG_SHOW_ERROR);
            mHandler.removeMessages(MSG_PASSWORD_CLEAR);

            super.onPause();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_UI_STAGE, mUiStage.name());
            outState.putString(KEY_FIRST_PIN, mFirstPin);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case CONFIRM_EXISTING_REQUEST:
                    if (resultCode != AuroraActivity.RESULT_OK) {
                        getActivity().setResult(RESULT_FINISHED);
                        getActivity().finish();
                    }
                    break;
            }
        }

        protected void updateStage(Stage stage) {
            final Stage previousStage = mUiStage;
            mUiStage = stage;
            updateUi();

            // If the stage changed, announce the header for accessibility. This
            // is a no-op when accessibility is disabled.
            if (previousStage != stage) {
                mHeaderText.announceForAccessibility(mHeaderText.getText());
            }
        }

        /**
         * Validates PIN and returns a message to display if PIN fails test.
         * 
         * @param password
         *            the raw password the user typed in
         * @return error message to show to user or null if password is OK
         */
        private String validatePassword(String password) {
            if (mLockPatternUtils.checkPasswordHistory(password)) {
                return getString(R.string.lockpassword_pin_recently_used);
            }
            return null;
        }

        private void handleNext() {
            final String pin = mPasswordEntry.getText().toString();
            if (TextUtils.isEmpty(pin)) {
                return;
            }
            mPasswordEntry.setEnabled(false);
            String errorMsg = null;
            if (mUiStage == Stage.Introduction) {
                errorMsg = validatePassword(pin);
                if (errorMsg == null) {
                    mPasswordEntry.setEnabled(true);
                    mFirstPin = pin;
                    mPasswordEntry.setText("");
                    updateStage(Stage.NeedToConfirm);
                }
            } else if (mUiStage == Stage.NeedToConfirm) {
                if (mFirstPin.equals(pin)) {
                    final boolean isFallback = getActivity().getIntent().getBooleanExtra(
                            LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK, false);
                    mLockPatternUtils.clearLock(isFallback);
                    mLockPatternUtils.saveLockPassword(pin, mRequestedQuality, isFallback);
                    Message msg = mHandler.obtainMessage(MSG_SAVE_PASSWORD);
                    mHandler.removeMessages(MSG_SAVE_PASSWORD);
                    mHandler.sendMessageDelayed(msg, 50);
                } else {
                    if (mShowDigitView != null) {
                        mShowDigitView.onTextChange(SHOW_DIGIT_ERROR);
                    }
                    updateStage(Stage.ConfirmWrong);
                    Message mesg = mHandler.obtainMessage(MSG_PASSWORD_CLEAR);
                    mHandler.removeMessages(MSG_PASSWORD_CLEAR);
                    mHandler.sendMessageDelayed(mesg, 500);
                }
            }
            if (errorMsg != null) {
                showError(errorMsg, mUiStage);
            }
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next_button:
                    handleNext();
                    break;

                case R.id.cancel_button:
                    getActivity().finish();
                    break;
            }
        }

        private void showError(String msg, final Stage next) {
            mHeaderText.setText(msg);
            mHeaderText.announceForAccessibility(mHeaderText.getText());
            Message mesg = mHandler.obtainMessage(MSG_SHOW_ERROR, next);
            mHandler.removeMessages(MSG_SHOW_ERROR);
            mHandler.sendMessageDelayed(mesg, ERROR_MESSAGE_TIMEOUT);
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Check if this was the result of hitting the enter or "done" key
            if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext();
                return true;
            }
            return false;
        }

        /**
         * Update the hint based on current Stage and length of password entry
         */
        private void updateUi() {
            mHeaderText.setText(mUiStage.numericHint);
        }

        public void afterTextChanged(Editable s) {
            // Changing the text while error displayed resets to NeedToConfirm state
            if (mUiStage == Stage.ConfirmWrong) {
                mUiStage = Stage.NeedToConfirm;
            }
            updateUi();
            if (mShowDigitView != null && s.length() < 5) {
                mShowDigitView.onTextChange(s.length());
            }
            if (s.length() == 4) {
                Message mesg = mHandler.obtainMessage(MSG_CONFIRM_PASSWORD);
                mHandler.removeMessages(MSG_CONFIRM_PASSWORD);
                mHandler.sendMessageDelayed(mesg, 80);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }
}
