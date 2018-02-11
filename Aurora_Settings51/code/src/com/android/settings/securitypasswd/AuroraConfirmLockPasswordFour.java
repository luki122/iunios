package com.android.settings.securitypasswd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.internal.widget.PasswordEntryKeyboardView;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.fingerprint.AuroraFingerprintUtils;
import com.aurora.utils.AuroraUtils;
import com.gionee.fingerprint.IGnIdentifyCallback;
import com.mediatek.settings.sim.Log;

import java.util.Timer;
import java.util.TimerTask;

import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraTextView;

public class AuroraConfirmLockPasswordFour extends AuroraPreferenceActivity {
    public static class InternalActivity extends AuroraConfirmLockPasswordFour {

    }

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, AuroraConfirmLockPasswordFourFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.aurora_securitypassword_confirm_title);
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

    public static class AuroraConfirmLockPasswordFourFragment extends Fragment
            implements TextView.OnEditorActionListener {
        private static final int DISPLAY_PASSWORD_TIMEOUT = 60;
        private static final int DELETE_PASSWORD_TIMEOUT = 50;
        private static final String TAG = "AuroraConfirmLockPasswordFourFragment";
        private static final long ERROR_MESSAGE_TIMEOUT = 2000/*3000*/;
        private TextView mPasswordEntry1;
        private TextView mPasswordEntry2;
        private TextView mPasswordEntry3;
        private TextView mPasswordEntry4;
        private TextWatcher mTextWatcher1;
        private TextWatcher mTextWatcher2;
        private TextWatcher mTextWatcher3;
        private TextWatcher mTextWatcher4;
        private AuroraLockPatternUtils mLockPatternUtils;
        private AuroraTextView mHeaderText;
        private Handler mHandler = new Handler();
        private PasswordEntryKeyboardView mKeyboardView;
        private View.OnKeyListener mKeyListner1;
        private AuroraTextView mForgetPassword;
        private InputMethodManager mInputMethodManager;
        /********************
         * fingerprint
         **************/
        private AuroraFingerprintUtils mFingerprintUtils;
        private int[] mFingerIdList;
        /********************
         * fingerprint
         **************/
        private boolean needReturnPassword = false;
        private boolean isFirstIn = true;

        private static final int CHANGE_PASSWORD_REQUEST = 102;
        private Timer mTimer;


        // required constructor for fragments
        public AuroraConfirmLockPasswordFourFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new AuroraLockPatternUtils(getActivity());
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            /********************fingerprint**************/
            Log.d(TAG, "ConfirmLockPasswordFourFragment onCreate");
            mFingerprintUtils = new AuroraFingerprintUtils();
            mFingerIdList = mFingerprintUtils.getIds();
            Log.d(TAG, "ConfirmLockPasswordFourFragment mFingerIdList=" + mFingerIdList);
            /********************fingerprint**************/
            if (getActivity() instanceof AuroraConfirmLockPasswordFour.InternalActivity) {
                needReturnPassword = true;
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //final int storedQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality();
            View view = inflater.inflate(R.layout.aurora_confirm_lock_password_four, null);
            // Disable IME on our window since we provide our own keyboard

            addTextListener();
            mPasswordEntry1 = (TextView) view.findViewById(R.id.password_entry1);
            mPasswordEntry1.setOnEditorActionListener(this);
            mPasswordEntry1.addTextChangedListener(mTextWatcher1);

            mPasswordEntry2 = (TextView) view.findViewById(R.id.password_entry2);
            mPasswordEntry2.setOnEditorActionListener(this);
            mPasswordEntry2.addTextChangedListener(mTextWatcher2);
            mPasswordEntry2.setOnKeyListener(new View.OnKeyListener() {
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
            mPasswordEntry3.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        Log.d("keyCode", "3KeyEvent.KEYCODE_DEL");
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
            mPasswordEntry4.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        Log.d("keyCode", "4KeyEvent.KEYCODE_DEL");
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

            mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(R.id.keyboard);
            mHeaderText = (AuroraTextView) view.findViewById(R.id.headerText);
            setDefaultHeaderText();
            final Activity activity = getActivity();

            // Update the breadcrumb (title) if this is embedded in a AuroraPreferenceActivity
            if (activity instanceof AuroraPreferenceActivity) {
                final AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
                int id = R.string.aurora_securitypassword_confirm_title;
                CharSequence title = getText(id);
                preferenceActivity.showBreadCrumbs(title, title);
            }

            mForgetPassword = (AuroraTextView) view.findViewById(R.id.forget_password);
            mForgetPassword.setOnClickListener(mForgetListener);
            int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                    AuroraUtils.INPUT_ERROR_NUMBER, 0);
            if (number < AuroraUtils.START_TIMING_NUMBER) {
                mForgetPassword.setVisibility(View.GONE);
            }

            return view;
        }

        @Override
        public void onPause() {
            super.onPause();
            mKeyboardView.requestFocus();
            cancelIdentifyFinger();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;

            }
            mHandler.removeCallbacks(mStartIdentifyRunnable);
        }

        @Override
        public void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            Intent pwdintent = getActivity().getIntent();
            if (pwdintent != null) {
                boolean isAppConfirmPwd = pwdintent.getBooleanExtra(SecurityUtils.APP_CONFIRM, false);
                if (isAppConfirmPwd) {
                    long currentType = SecurityUtils.getSecurityPasswordType(mLockPatternUtils);
                    if (currentType != SecurityUtils.SECURITYPWD_FOUR_NUMBER_TYPE) {
                        Intent intent = new Intent();
                        intent.putExtra(SecurityUtils.NEED_CONFIRM_AGAIN, true);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                        return;

                    }
                }
            }


            mKeyboardView.requestFocus();

            mPasswordEntry1.requestFocus();
            refreshScreen();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mPasswordEntry1 != null) {
                mInputMethodManager.hideSoftInputFromWindow(mPasswordEntry1.getWindowToken(), 0);
            }
        }

        private void handleNext() {
            final String pin = mPasswordEntry1.getText().toString()
                    + mPasswordEntry2.getText().toString()
                    + mPasswordEntry3.getText().toString()
                    + mPasswordEntry4.getText().toString();
            Log.i(TAG, " pin = " + pin + "  checkPassword : " + mLockPatternUtils.checkPassword(pin));
            if (mLockPatternUtils.checkPassword(pin)) {
                Intent intent = new Intent();
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, pin);
                //Gionee <wangguojing> <2015-05-26> add for CR01487753 begin
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE, StorageManager.CRYPT_TYPE_PIN);
                //Gionee <wangguojing> <2015-05-26> add for CR01487753 end
                confirmOkFinish(intent);
            } else {
                showError();

            }
        }

        // {@link OnEditorActionListener} methods.
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext();
                return true;
            }
            return false;
        }


        private void addTextListener() {
            mTextWatcher1 = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    Log.i(TAG,
                            "et1-->et2 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
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
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    Log.i(TAG,
                            "et2-->et3 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
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
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    Log.i(TAG,
                            "et3-->et4 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
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
                    Log.i(TAG,
                            "et3-->et4 s:" + s + "     s.length:" + s.length());
                    if (s.length() >= 1) {
                        handleNext();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };
        }

        private void showError() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mHeaderText.announceForAccessibility(mHeaderText.getText());
            mPasswordEntry1.setText(null);
            mPasswordEntry2.setText(null);
            mPasswordEntry3.setText(null);
            mPasswordEntry4.setText(null);

            int number = addErrorNumber();
            String msg;
            long time;
            if (needDisplayForgetPwd(number)) {

                Log.i(TAG, "showError toggleSoftInput-------");
                mInputMethodManager.hideSoftInputFromWindow(mPasswordEntry4.getWindowToken(), 0);
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
                mPasswordEntry1.requestFocus();
                msg = getActivity().getString(R.string.aurora_securitypassword_input_error_note,
                        AuroraUtils.START_TIMING_NUMBER - number);
                mHeaderText.setText(msg);
                setTimerTaskForHeader(ERROR_MESSAGE_TIMEOUT);

            }

        }

        private void showErrorDisplayForgetPwd(String msg) {
            mForgetPassword.setVisibility(View.VISIBLE);
            mPasswordEntry1.setVisibility(View.GONE);
            mPasswordEntry2.setVisibility(View.GONE);
            mPasswordEntry3.setVisibility(View.GONE);
            mPasswordEntry4.setVisibility(View.GONE);

            mHeaderText.setText(msg);
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
                    mHeaderText.setText(msg);

                    showErrorDisplayForgetPwd(msg);
                    setTimerTask(firstTime, SecurityUtils.ONE_MIN_TO_MILLI);
                    if (isFirstIn) {
                        mHandler.postDelayed(mHideSoftInputRunnable, 150);
                        isFirstIn = false;
                    }

                } else {
                    displayInputScreen(true);
                }

            } else {
                mForgetPassword.setVisibility(View.GONE);
                startIdentifyFinger(true);

            }

        }

        private void displayInputScreen(boolean softInputDelayed) {
            setDefaultHeaderText();
            mPasswordEntry1.setVisibility(View.VISIBLE);
            mPasswordEntry2.setVisibility(View.VISIBLE);
            mPasswordEntry3.setVisibility(View.VISIBLE);
            mPasswordEntry4.setVisibility(View.VISIBLE);
            mPasswordEntry1.requestFocus();
            if (softInputDelayed) {
                mHandler.postDelayed(mShowSoftInputRunnable, 150);
            } else {
                mInputMethodManager.showSoftInput(mPasswordEntry1, 0);
            }
            Settings.Secure.putLong(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_TIME, 0);
            startIdentifyFinger(false);

        }

        private int addErrorNumber() {
            int number = Settings.Secure.getInt(getActivity().getContentResolver(),
                    AuroraUtils.INPUT_ERROR_NUMBER, 0);

            if (number <= AuroraUtils.TOTAL_TIMING_NUMBER) {
                number++;
                Settings.Secure.putInt(getActivity().getContentResolver(), AuroraUtils.INPUT_ERROR_NUMBER, number);
            }
            return number;

        }

        private void setDefaultHeaderText() {
            if (canUseFingerPrint()) {
                mHeaderText.setText(R.string.aurora_fingerprint_confirm_your_password_header);
            } else {
                mHeaderText.setText(R.string.aurora_confirm_your_password_header);
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


        private Runnable mHideSoftInputRunnable = new Runnable() {
            public void run() {
                mInputMethodManager.hideSoftInputFromWindow(mPasswordEntry4.getWindowToken(), 0);

            }
        };

        private Runnable mShowSoftInputRunnable = new Runnable() {
            public void run() {
                mInputMethodManager.showSoftInput(mPasswordEntry1, 0);
                ;

            }
        };
        private Runnable mStartIdentifyRunnable = new Runnable() {
            public void run() {
                Log.d("Fingerprint_Settings", "startIdentifyFinger()---for delay 500ms");
                mFingerprintUtils.startIdentify(identifyCb, mFingerIdList);

            }
        };


        private final AuroraButton.OnClickListener mForgetListener = new AuroraButton.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AuroraForgetActivity.class);
                startActivity(intent);
            }
        };

        private boolean canUseFingerPrint() {
            if (needReturnPassword) {
                return false;
            }

            if (mFingerIdList != null && mFingerIdList.length > 0) {
                return true;
            }
            return false;

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
                        displayInputScreen(false);
                        break;

                    case AuroraFingerprintUtils.MSG_REFRESH_HEADER:
                        int displayMin = msg.arg1;
                        String text = getActivity().getString(R.string.aurora_securitypassword_retry_note, displayMin);
                        mHeaderText.setText(text);
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
