/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraActivity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageActivity;
//import android.drm.DrmHelper;
//gionee gaoj 2012-5-7 added for CR00589318 start
import android.view.inputmethod.InputMethodManager;
//gionee gaoj 2012-5-7 added for CR00589318 end
// gionee zhouyj 2012-08-30 add for CR00679237 start 
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
// gionee zhouyj 2012-08-30 add for CR00679237 end 

public class MsgChooseLockPassword extends AuroraActivity implements OnClickListener, OnEditorActionListener,
        TextWatcher {
    private static final String KEY_FIRST_PIN = "first_pin";
    private static final String KEY_UI_STAGE = "ui_stage";
    private TextView mPasswordEntry;
    private int mPasswordMinLength = 4;
    private int mPasswordMaxLength = 16;
    private boolean mIsDecryption  = false;
    private com.gionee.mms.ui.MsgChooseLockPassword.Stage mUiStage = Stage.Introduction;
    private TextView mHeaderText;
    private String mFirstPin;
    private KeyboardView mKeyboardView;
    private PasswordEntryKeyboardHelper mKeyboardHelper;
    private boolean isFinish = false;
    private AuroraButton mCancelButton;
    private AuroraButton mNextButton;
    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";
    private static Handler mHandler = new Handler();
    static final int RESULT_FINISHED = RESULT_FIRST_USER;
    private static final long ERROR_MESSAGE_TIMEOUT = 3000;

    private static String mPassword;
    /**
     * Keep track internally of where the user is in choosing a pattern.
     */
    protected enum Stage {

        Introduction(R.string.lockpassword_choose_your_password_header,
                R.string.lockpassword_continue_label),

        NeedToConfirm(R.string.lockpassword_confirm_your_password_header,
                R.string.lockpassword_ok_label),

        ConfirmWrong(R.string.lockpassword_confirm_passwords_dont_match,
                R.string.lockpassword_continue_label);

        /**
         * @param headerMessage The message displayed at the top.
         */
      Stage(int hintInAlpha, int nextButtonText) {
            this.alphaHint = hintInAlpha;
            this.buttonText = nextButtonText;
        }

        public final int alphaHint;
        public final int buttonText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(savedInstanceState);
        mPasswordMinLength = getIntent().getIntExtra(PASSWORD_MIN_KEY, mPasswordMinLength);
        mPasswordMaxLength = getIntent().getIntExtra(PASSWORD_MAX_KEY, mPasswordMaxLength);
        mIsDecryption = getIntent().getBooleanExtra("isdecryption", mIsDecryption);

        initViews();

        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            //gionee gaoj added for CR00725602 20121201 start
            actionBar.setDisplayHomeAsUpEnabled(true);
            //gionee gaoj added for CR00725602 20121201 end
            // gionee zhouyj 2012-08-30 add for CR00679237 start 
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            filter.addAction("android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/);
            // Aurora xuyong 2013-11-15 modified for S4 adapt end
            registerReceiver(mReceiver, filter);
            // gionee zhouyj 2012-08-30 add for CR00679237 end 
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
    }

    //gionee gaoj added for CR00725602 20121201 start
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            setResult(RESULT_OK, new Intent("cancel"));
            isFinish = true;
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //gionee gaoj added for CR00725602 20121201 end

    private void initViews() {
//Gionee guoyangxu 20120921 modified for CR00683310 begin          
        if (MmsApp.mLightTheme){
            setContentView(R.layout.gn_light_choose_lock_password);
        } else {
            //MmsApp.mDarkStyle
            setContentView(R.layout.gn_choose_lock_password);
        }
//Gionee guoyangxu 20120921 modified for CR00683310 end        
        // Disable IME on our window since we provide our own keyboard
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
//                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        mCancelButton = (AuroraButton) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mNextButton = (AuroraButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mNextButton.setEnabled(false);
        
        //Gionee guoyangxu 20121003 modified for CR00705411 begin
        if (MmsApp.mDarkStyle) {
            //Gionee <zhouyj> <2013-05-29> modify for CR00799376 begin for super theme
            mNextButton.setTextColor(getResources().getColor(R.color.gn_gray_color));
            //Gionee <zhouyj> <2013-05-29> modify for CR00799376 end
        }
        //Gionee guoyangxu 20121003 modified for CR00705411 end
//Gionee guoyangxu 20120921 removed for CR00683310 begin     
//        //gionee gaoj 2012-6-27 added for CR00628364 start
//        if (MmsApp.mDarkStyle) {
//            mCancelButton.setTextColor(Color.WHITE);
//            mNextButton.setTextColor(Color.WHITE);
//        } else if (MmsApp.mLightTheme) {
//            mCancelButton.setTextColor(Color.BLACK);
//            mNextButton.setTextColor(Color.BLACK);
//        }
//        //gionee gaoj 2012-6-27 added for CR00628364 end
//Gionee guoyangxu 20120921 removed for CR00683310 begin             


        mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
        mPasswordEntry = (TextView) findViewById(R.id.password_entry);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);

        mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        mKeyboardHelper = new PasswordEntryKeyboardHelper(this, mKeyboardView, mPasswordEntry);

        mKeyboardHelper.setKeyboardMode(PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);

        mHeaderText = (TextView) findViewById(R.id.headerText);
        if (mIsDecryption) {
            mHeaderText.setText(R.string.lockpassword_input_your_password);
        }
        mKeyboardView.requestFocus();


        int currentType = mPasswordEntry.getInputType();
        //Gionee <guoyx> <2013-06-20> modify for CR00828110 begin
        /*Gionee:liuxiangrong 2012-09-12 modify for CR00689685 start*/
        mPasswordEntry.setInputType(false ? currentType
                : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
        /*Gionee:liuxiangrong 2012-09-12 modify for CR00689685 end*/
        //Gionee <guoyx> <2013-06-20> modify for CR00828110 end

        WindowManager windowManager =
            (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int LcdWidth = windowManager.getDefaultDisplay().getWidth();
        int LcdHeight = windowManager.getDefaultDisplay().getHeight();

        if((LcdHeight <= 320)&&(LcdWidth <= 240)){
            TextView tv0 = (TextView) findViewById(R.id.headerText);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2,1);
            tv0.setLayoutParams(params);

            View v0 = (View) findViewById(R.id.spacerBottom);
            params = new LinearLayout.LayoutParams(-1, 1,1);
            params.topMargin = 0;
            v0.setLayoutParams(params);

            params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = 0;
            params.topMargin = 0;
            mKeyboardView.setLayoutParams(params);
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!mIsDecryption) {
            updateStage(mUiStage);
//        }
        mKeyboardView.requestFocus();
        // gionee zhouyj 2012-11-14 add for CR00728951 start 
        if (MmsApp.mIsSafeModeSupport) {
            finish();
        }
        // gionee zhouyj 2012-11-14 add for CR00728951 end 
    }

    //gionee gaoj 2012-5-7 added for CR00589318 start
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        InputMethodManager inputMethodManager =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(this.getWindow()!=null && this.getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(this.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }
    //gionee gaoj 2012-5-7 added for CR00589318 end

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_UI_STAGE, mUiStage.name());
        outState.putString(KEY_FIRST_PIN, mFirstPin);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String state = savedInstanceState.getString(KEY_UI_STAGE);
        mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
        if (state != null) {
            mUiStage = Stage.valueOf(state);
            updateStage(mUiStage);
        }
    }

    protected void updateStage(Stage stage) {
        mUiStage = stage;
        updateUi();
    }

    /**
     * Validates PIN and returns a message to display if PIN fails test.
     * @param password the raw password the user typed in
     * @return error message to show to user or null if password is OK
     */
    private String validatePassword(String password) {
        if (password.length() < mPasswordMinLength) {
            return getString(R.string.lockpassword_password_too_short, mPasswordMinLength);
        }
        if (password.length() > mPasswordMaxLength) {
            return getString(R.string.lockpassword_password_too_long, mPasswordMaxLength);
        }
//        boolean hasAlpha = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            // allow non white space Latin-1 characters only
            if (c <= 32 || c > 127) {
                return getString(R.string.lockpassword_illegal_character);
            }
            if (c == '#' || c == '%' || c == '/' || c == '?') {
                String getstring = getString(R.string.lockpassword_illegal_character);
                return getstring + " '" + c + "'";
            }
//            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
//                hasAlpha = true;
//            }
        }
//            if (!hasAlpha) {
//                return getString(R.string.lockpassword_password_requires_alpha);
//            }
        return null;
    }

    private void handleNext() {
        final String pin = mPasswordEntry.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            return;
        }
        if (mIsDecryption) {
            String savepassword = Conversation.cachesmspsw(getApplicationContext());//ReadSharedPreferences();
            if (savepassword != null && savepassword.equals(pin)) {
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                setResult(RESULT_OK, new Intent("confirm"));
                isFinish = true;
                finish();
            } else {
//                if (pin.equalsIgnoreCase(DrmHelper.getIMEI(getApplicationContext()))) {
//                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//                    setResult(RESULT_OK, new Intent("confirm"));
//                    isFinish = true;
//                    finish();
//                } else {
                //gionee yewq 2012-11-14 modify for CR00723723 begin--
                mNextButton.setEnabled(false);
                showError(R.string.lockpattern_need_to_unlock_wrong);
                //gionee gaoj 2012-8-7 added for CR00624895 start
                //mNextButton.setEnabled(false);
                //gionee gaoj 2012-8-7 added for CR00624895 end
                //gionee yewq 2012-11-14 modify for CR00723723 begin--
//                }
            }
        } else {
            String errorMsg = null;
            if (mUiStage == Stage.Introduction) {
                errorMsg = validatePassword(pin);
                if (errorMsg == null) {
                    mFirstPin = pin;
                    updateStage(Stage.NeedToConfirm);
                    mPasswordEntry.setText("");
                }
            } else if (mUiStage == Stage.NeedToConfirm) {
                if (mFirstPin.equals(pin)) {
                    mPassword = pin;
                    Conversation.savepsw(getApplicationContext(), mPassword);
                    Conversation.setFirstEncryption(false);
//                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

                    setResult(RESULT_OK, new Intent("succeed"));
                    isFinish = true;
                    finish();
                } else {
                    updateStage(Stage.ConfirmWrong);
                    CharSequence tmp = mPasswordEntry.getText();
                    if (tmp != null) {
                        Selection.setSelection((Spannable) tmp, 0, tmp.length());
                    }
                }
            }
            if (errorMsg != null) {
                showError(errorMsg, mUiStage);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                handleNext();
                if(!isFinish){
                    mPasswordEntry.requestFocus();
                }
                break;

            case R.id.cancel_button:
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

                setResult(RESULT_OK, new Intent("cancel"));
                isFinish = true;
                finish();
                break;
        }
    }

    private void showError(String msg, final Stage next) {
        mHeaderText.setText(msg);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateStage(next);
            }
        }, ERROR_MESSAGE_TIMEOUT);
    }

    private void showError(int msg) {
        mHeaderText.setText(msg);
        mPasswordEntry.setText(null);
        mHandler.postDelayed(new Runnable() {
            public void run() {
              mHeaderText.setText(R.string.lockpassword_input_your_password);
            }
        }, ERROR_MESSAGE_TIMEOUT);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Check if this was the result of hitting the enter key
        // gionee zhouyj 2012-07-11 modify for CR00637008 start 
        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_DONE) {
            handleNext();
            return true;
        }
        // gionee zhouyj 2012-07-11 modify for CR00637008 end 
        return false;
    }

    /**
     * Update the hint based on current Stage and length of password entry
     */
    private void updateUi() {
        String password = mPasswordEntry.getText().toString();
        final int length = password.length();

        if (mIsDecryption) {
            if (length > 0) {
                if (length < mPasswordMinLength) {
                    mNextButton.setEnabled(false);
                } else {
                    String error = validatePassword(password);
                    if (error != null) {
                        mHeaderText.setText(error);
                        mNextButton.setEnabled(false);
                    } else {
                        mNextButton.setEnabled(true);
                    }
                }
            }
            return;
        }
        if (mUiStage == Stage.Introduction && length > 0) {
            if (length < mPasswordMinLength) {
                String msg = getString(R.string.lockpassword_password_too_short, mPasswordMinLength);
                mHeaderText.setText(msg);
                mNextButton.setEnabled(false);
            } else {
                String error = validatePassword(password);
                if (error != null) {
                    mHeaderText.setText(error);
                    mNextButton.setEnabled(false);
                } else {
                    mHeaderText.setText(R.string.lockpassword_press_continue);
                    mNextButton.setEnabled(true);
                }
            }
        } else {
            mHeaderText.setText(mUiStage.alphaHint);
            mNextButton.setEnabled(length > 0);
        }
        mNextButton.setText(mUiStage.buttonText);
    }

    public void afterTextChanged(Editable s) {
        // Changing the text while error displayed resets to NeedToConfirm state
//        if (!mIsDecryption) {
            if (mUiStage == Stage.ConfirmWrong) {
                mUiStage = Stage.NeedToConfirm;
            }
            updateUi();
            //Gionee guoyangxu 20121003 modified for CR00705411 begin
            if (MmsApp.mDarkStyle) {
                //Gionee <zhouyj> <2013-05-29> modify for CR00799376 begin super theme
                mNextButton.setTextColor(mNextButton.isEnabled() ? getResources().getColor(R.color.gn_color_white) : 
                    getResources().getColor(R.color.gn_gray_color));
                //Gionee <zhouyj> <2013-05-29> modify for CR00799376 end
            }
            //Gionee guoyangxu 20121003 modified for CR00705411 end
//        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private String ReadSharedPreferences() {
        String strName, strPassword;
        SharedPreferences user = getSharedPreferences("MsgEncryption", 0);
        strName = user.getString("action", null);
        strPassword = user.getString("password", null);
        if (strName.equals("saveencryption") && strPassword != null) {
            return strPassword;
        }
        return null;
    }

    private void WriteSharedPreferences(String strName, String strPassword) {
        SharedPreferences user = getSharedPreferences("MsgEncryption", 0);
        Editor editor = user.edit();
        editor.putString("action", strName);
        editor.putString("password", strPassword);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

                setResult(RESULT_OK, new Intent("back"));
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    // gionee zhouyj 2012-08-30 add for CR00679237 start 
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    // gionee zhouyj 2012-08-30 add for CR00679237 end 

    // gionee zhouyj 2012-08-30 add for CR00679237 start 
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            if(Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())
                    || "android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/.equals(intent.getAction())) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt end
                MsgChooseLockPassword.this.finish();
            }
        }
    };
    // gionee zhouyj 2012-08-30 add for CR00679237 end 
}

