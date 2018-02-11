package com.android.settings.securitypasswd;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraTextView;

public class AuroraChooseLockPasswordFour extends AuroraPreferenceActivity {
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, AuroraChooseLockPasswordFourFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.lockpassword_choose_your_password_four_header);
        showBreadCrumbs(msg, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AuroraChooseLockPasswordFourFragment extends Fragment
            implements OnEditorActionListener {
        private static final String TAG = "AuroraChooseLockPasswordFourFragment";
        private static final String KEY_FIRST_PIN = "first_pin";
        private static final String KEY_UI_STAGE = "ui_stage";

        private TextView mPasswordEntry1;
        private TextView mPasswordEntry2;
        private TextView mPasswordEntry3;
        private TextView mPasswordEntry4;
        private TextWatcher mTextWatcher1;
        private TextWatcher mTextWatcher2;
        private TextWatcher mTextWatcher3;
        private TextWatcher mTextWatcher4;

        private AuroraLockPatternUtils mLockPatternUtils;
        //private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_COMPLEX;
        private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private Stage mUiStage = Stage.Introduction;
        private AuroraTextView mHeaderText;
        private String mFirstPin;
        private KeyboardView mKeyboardView;
        private PasswordEntryKeyboardHelper mKeyboardHelper;
        private static final int CONFIRM_EXISTING_REQUEST = 58;
        static final int RESULT_FINISHED = RESULT_FIRST_USER;
        private static final long ERROR_MESSAGE_TIMEOUT = 3000;
        private static final int DISPLAY_PASSWORD_TIMEOUT = 60;
        private static final int DELETE_PASSWORD_TIMEOUT = 50;
        private static final int MSG_SHOW_ERROR = 1;
        private boolean mFinishExit = false;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_ERROR) {
                    Log.i(TAG, "MSG_SHOW_ERROR  msg.obj = " + msg.obj);
                    updateStage((Stage) msg.obj);
                }
            }
        };

        /**
         * Keep track internally of where the user is in choosing a pattern.
         */
        protected enum Stage {

            Introduction(R.string.unlock_set_password),

            NeedToConfirm(R.string.unlock_set_password_confirm),

            ConfirmWrong(R.string.lockpassword_confirm_pins_dont_match_four);

            /**
             * @param headerMessage The message displayed at the top.
             */
            Stage(int hintInNumeric) {
                this.numericHint = hintInNumeric;
            }

            public final int numericHint;
        }

        public AuroraChooseLockPasswordFourFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new AuroraLockPatternUtils(getActivity());

            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.aurora_choose_lock_password_four, null);

            mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(R.id.keyboard);

            addTextListener();

            mPasswordEntry1 = (TextView) view.findViewById(R.id.password_entry1);
            mPasswordEntry1.setOnEditorActionListener(this);
            mPasswordEntry1.addTextChangedListener(mTextWatcher1);

            mPasswordEntry2 = (TextView) view.findViewById(R.id.password_entry2);
            mPasswordEntry2.setOnEditorActionListener(this);
            mPasswordEntry2.addTextChangedListener(mTextWatcher2);
            mPasswordEntry2.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        Log.d("keyCode", "2KeyEvent.KEYCODE_DEL");
                        if (TextUtils.isEmpty(mPasswordEntry2.getText())) {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mPasswordEntry1.requestFocus();
                                    mPasswordEntry1.setText("");
                                }

                            }, DELETE_PASSWORD_TIMEOUT);
                        }
                    }
                    return false;
                }
            });

            mPasswordEntry3 = (TextView) view.findViewById(R.id.password_entry3);
            mPasswordEntry3.setOnEditorActionListener(this);
            mPasswordEntry3.addTextChangedListener(mTextWatcher3);
            mPasswordEntry3.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        Log.d("keyCode", "2KeyEvent.KEYCODE_DEL");
                        if (TextUtils.isEmpty(mPasswordEntry3.getText())) {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mPasswordEntry2.requestFocus();
                                    mPasswordEntry2.setText("");
                                }

                            }, DELETE_PASSWORD_TIMEOUT);
                        }
                    }
                    return false;
                }
            });

            mPasswordEntry4 = (TextView) view.findViewById(R.id.password_entry4);
            mPasswordEntry4.setOnEditorActionListener(this);
            mPasswordEntry4.addTextChangedListener(mTextWatcher4);
            mPasswordEntry4.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        Log.d("keyCode", "2KeyEvent.KEYCODE_DEL");
                        if (TextUtils.isEmpty(mPasswordEntry4.getText())) {
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mPasswordEntry3.requestFocus();
                                    mPasswordEntry3.setText("");
                                }

                            }, DELETE_PASSWORD_TIMEOUT);
                        }
                    }
                    return false;
                }
            });


            final Activity activity = getActivity();

            mHeaderText = (AuroraTextView) view.findViewById(R.id.headerText);
            mKeyboardView.requestFocus();

            Intent intent = getActivity().getIntent();
            final boolean confirmCredentials = intent.getBooleanExtra("confirm_credentials", true);
            if (savedInstanceState == null) {
                updateStage(Stage.Introduction);
                if (confirmCredentials) {
                    mChooseLockSettingsHelper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST,
                            null, null);
                }
            } else {
                mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
                final String state = savedInstanceState.getString(KEY_UI_STAGE);
                if (state != null) {
                    mUiStage = Stage.valueOf(state);
                    Log.i(TAG, "mUiStage = " + mUiStage);
                    updateStage(mUiStage);
                }
            }
            if (activity instanceof AuroraPreferenceActivity) {
                final AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
                int id = R.string.lockpassword_choose_your_password_four_header;
                CharSequence title = getText(id);
                preferenceActivity.showBreadCrumbs(title, title);
            }

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateStage(mUiStage);
            mKeyboardView.requestFocus();
        }

        @Override
        public void onPause() {
            mHandler.removeMessages(MSG_SHOW_ERROR);

            super.onPause();
            if (!mFinishExit) {
                getActivity().finish();
            }

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_UI_STAGE, mUiStage.name());
            outState.putString(KEY_FIRST_PIN, mFirstPin);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case CONFIRM_EXISTING_REQUEST:
                    if (resultCode != AuroraActivity.RESULT_OK) {
                        getActivity().setResult(RESULT_FINISHED);
                        getActivity().finish();
                    }
                    break;
                default:
                    break;
            }
        }

        protected void updateStage(Stage stage) {
            final Stage previousStage = mUiStage;
            mUiStage = stage;
            Log.i(TAG, "updateStage mUiStage = " + mUiStage);
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
         * @param password the raw password the user typed in
         * @return error message to show to user or null if password is OK
         */
        private String validatePassword(String password) {
            int letters = 0;
            int numbers = 0;
            int lowercase = 0;
            int symbols = 0;
            int uppercase = 0;
            int nonletter = 0;
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                // allow non control Latin-1 characters only
                if (c < 32 || c > 127) {
                    return getString(R.string.lockpassword_illegal_character);
                }
                if (c >= '0' && c <= '9') {
                    numbers++;
                    nonletter++;
                } else if (c >= 'A' && c <= 'Z') {
                    letters++;
                    uppercase++;
                } else if (c >= 'a' && c <= 'z') {
                    letters++;
                    lowercase++;
                } else {
                    symbols++;
                    nonletter++;
                }
            }
            if (DevicePolicyManager.PASSWORD_QUALITY_NUMERIC == mRequestedQuality
                    && (letters > 0 || symbols > 0)) {
                return getString(R.string.lockpassword_pin_contains_non_digits);
            }
            return null;
        }

        private void handleNext() {
            final String pin = mPasswordEntry1.getText().toString()
                    + mPasswordEntry2.getText().toString()
                    + mPasswordEntry3.getText().toString()
                    + mPasswordEntry4.getText().toString();
            if (TextUtils.isEmpty(pin)) {
                return;
            }
            String errorMsg = null;
            if (mUiStage == Stage.Introduction) {
                errorMsg = validatePassword(pin);
                if (errorMsg == null) {
                    mFirstPin = pin;
                    mPasswordEntry1.setText("");
                    mPasswordEntry2.setText("");
                    mPasswordEntry3.setText("");
                    mPasswordEntry4.setText("");

                    updateStage(Stage.NeedToConfirm);
                }
            } else if (mUiStage == Stage.NeedToConfirm) {
                Log.i(TAG, "handleNext mFirstPin = " + mFirstPin + " ; pin = "
                        + pin + "; mRequestedQuality = " + mRequestedQuality);
                if (mFirstPin.equals(pin)) {
                    //final boolean isFallback = getActivity().getIntent().getBooleanExtra(
                    //        LockPatternUtils.LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK, false);
                    //mLockPatternUtils.clearLock(isFallback);
                    //final boolean required = getActivity().getIntent().getBooleanExtra(
                    //        EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true);
                    //mLockPatternUtils.setCredentialRequiredToDecrypt(required);
                    //mLockPatternUtils.saveLockPassword(pin, mRequestedQuality, isFallback);
                    //Gionee <chenml> <2015-01-07> add for CR01434175 begin
                    //startActivity(NotificationCardLockScreen.createStartIntent(getActivity()));
                    //Gionee <chenml> <2015-01-07> add for CR01434175 end
                    mFinishExit = true;
                    if (SecurityUtils.isSecurityPasswordEnable(mLockPatternUtils)) {
                        getActivity().setResult(RESULT_OK);

                    } else {
                        remindUserBind(pin);
                    }
                    savePassword(pin);

                    getActivity().finish();
                } else {
                    updateStage(Stage.ConfirmWrong);
                }
            }
            if (errorMsg != null) {
                showError(errorMsg, mUiStage);
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
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
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
            String password = mPasswordEntry1.getText().toString()
                    + mPasswordEntry2.getText().toString()
                    + mPasswordEntry3.getText().toString()
                    + mPasswordEntry4.getText().toString();
            final int length = password.length();

            Log.i(TAG, "password = " + password + " length = " + length);
            if (mUiStage == Stage.Introduction && length > 0) {
                String error = validatePassword(password);
                if (error != null) {
                    mHeaderText.setText(error);
                } else {
                    mHeaderText.setText(R.string.lockpassword_choose_your_password_four_header);
                }
            } else {
                mHeaderText.setText(mUiStage.numericHint);
                if (mUiStage == Stage.ConfirmWrong) {
                    mPasswordEntry1.setText(null);
                    mPasswordEntry2.setText(null);
                    mPasswordEntry3.setText(null);
                    mPasswordEntry4.setText(null);
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            mPasswordEntry1.requestFocus();
                        }
                    }, DISPLAY_PASSWORD_TIMEOUT);
                }
            }
        }

        private void addTextListener() {
            mTextWatcher1 = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    Log.i(TAG, "et1-->et2 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                mPasswordEntry2.requestFocus();
                            }
                        }, DISPLAY_PASSWORD_TIMEOUT);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

            };

            mTextWatcher2 = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    Log.i(TAG, "et2-->et3 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                mPasswordEntry3.requestFocus();
                            }
                        }, DISPLAY_PASSWORD_TIMEOUT);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

            };

            mTextWatcher3 = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    Log.i(TAG, "et3-->et4 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                mPasswordEntry4.requestFocus();
                            }
                        }, DISPLAY_PASSWORD_TIMEOUT);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

            };

            mTextWatcher4 = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    Log.i(TAG, "et4 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {

                        if (mUiStage == Stage.ConfirmWrong) {
                            mUiStage = Stage.NeedToConfirm;
                        }

                        updateUi();

                        handleNext();
                        if (mUiStage == Stage.NeedToConfirm) {
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    mPasswordEntry1.requestFocus();
                                }
                            }, DISPLAY_PASSWORD_TIMEOUT);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

            };
        }

        private void savePassword(String pwd) {
            boolean isUnlocked = SecurityUtils.isKeyStoreUnlocked();

            if (mLockPatternUtils.isSecure()) {
                mLockPatternUtils.saveLockPassword(pwd, mRequestedQuality, false);
            } else {
                mLockPatternUtils.gnSetLockPassword(pwd);
            }
            mLockPatternUtils.gnSetString(SecurityUtils.BACKUP_PASSWORD, pwd);
            mLockPatternUtils.gnSetLong(SecurityUtils.SECURITYPASSWORD_TYPE, SecurityUtils.SECURITYPWD_FOUR_NUMBER_TYPE);

            SecurityUtils.resetKeyStore(isUnlocked);
        }

        private void remindUserBind(String pwd) {
            Intent intent = new Intent().setClass(getActivity(), AuroraPasswordSuccessActivity.class);
            intent.putExtra(AuroraPasswordSuccessActivity.EXTRA_PASSWORD_QUALITY, mRequestedQuality);
            intent.putExtra(AuroraPasswordSuccessActivity.EXTRA_PASSWORD_VALUE, pwd);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
        }
    }
}

