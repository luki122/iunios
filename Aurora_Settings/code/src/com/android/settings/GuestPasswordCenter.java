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

package com.android.settings;

import aurora.app.AuroraActivity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreferenceActivity;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import aurora.widget.AuroraEditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;

import aurora.widget.AuroraActionBar;

public class GuestPasswordCenter extends AuroraActivity implements View.OnClickListener,OnEditorActionListener,  TextWatcher {
    
    private Button mCancelButton;
    private Button mNextButton;
    
    private TextView mPasswordEntry;
    private TextView mHeaderText;
    private String mFirstPass;
    
    private static final int MSG_SHOW_ERROR = 1;
    private static final long ERROR_MESSAGE_TIMEOUT = 3000;
    private int mPasswordMinLength = 4;
    private int mPasswordMaxLength = 16;
    private PasswordEntryKeyboardHelper mKeyboardHelper;
    private KeyboardView mKeyboardView;
    
    private Stage mUiStage;
    
    private boolean mIsAlterPass = false;

    private static final String GN_GUEST_MODE = "gionee_guest_mode";
    private static final String GN_GUEST_PASS = "gionee_guest_pass";
    private static final String GN_GUEST_PASS_ENABLE = "gionee_guest_pass_enable";
    
    protected enum Stage {
        
        SetPass(R.string.lockpassword_choose_your_password_header, R.string.lockpassword_continue_label),

        NeedToConfirm(R.string.lockpassword_confirm_your_password_header, R.string.lockpassword_ok_label),

        AlterPass(R.string.comfirm_password, R.string.lockpassword_ok_label),
        
        SetNewPass(R.string.choose_new_password_header, R.string.lockpassword_ok_label),
        
        ValidatePass(R.string.need_password_summary, R.string.lockpassword_ok_label),
        
        ConfirmWrong(R.string.lockpassword_confirm_passwords_dont_match, R.string.lockpassword_ok_label),
        
        ExitPass(R.string.comfirm_password, R.string.lockpassword_ok_label),
        
        OpenPass(R.string.comfirm_password, R.string.lockpassword_ok_label);

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
    public void onCreate(Bundle savedInstanceState) {
        if (GnSettingsUtils.sGnSettingSupport) {
            if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
                setTheme(R.style.GnSettingsLightTheme);
            } else {
                setTheme(R.style.GnSettingsDarkTheme);
            }
        }
        // TODO: Fix on phones
        // Disable IME on our window since we provide our own keyboard
        super.onCreate(savedInstanceState);
         
       //AURORA-START::delete temporarily for compile::waynelin::2013-9-18 
       /*
        String mode = getIntent().getStringExtra("guest_mode");
        if(mode.equals("set_pass")){
            mUiStage = Stage.SetPass;
            setTitle(R.string.set_pass);
        }else if(mode.equals("validate_pass")){
            mUiStage = Stage.ValidatePass;
            setTitle(R.string.guest_pass_confirm);
        }else if(mode.equals("alter_pass")){
            mIsAlterPass = true;
            mUiStage = Stage.AlterPass;
            setTitle(R.string.alter_password);
        }else if(mode.equals("exit_pass")){
            mUiStage = Stage.ExitPass;
            setTitle(R.string.exit_pass);
        }else{
            mUiStage = Stage.OpenPass;
            setTitle(R.string.open_pass);
        }
        
        setContentView(R.layout.gn_guest_pass_center);
        */
        setAuroraContentView(R.layout.gn_guest_pass_center,AuroraActionBar.Type.Normal);
        String mode = getIntent().getStringExtra("guest_mode");
        if(mode.equals("set_pass")){
            mUiStage = Stage.SetPass;
            getAuroraActionBar().setTitle(R.string.set_pass);
        }else if(mode.equals("validate_pass")){
            mUiStage = Stage.ValidatePass;
            getAuroraActionBar().setTitle(R.string.guest_pass_confirm);
        }else if(mode.equals("alter_pass")){
            mIsAlterPass = true;
            mUiStage = Stage.AlterPass;
            getAuroraActionBar().setTitle(R.string.alter_password);
        }else if(mode.equals("exit_pass")){
            mUiStage = Stage.ExitPass;
            getAuroraActionBar().setTitle(R.string.exit_pass);
        }else{
            mUiStage = Stage.OpenPass;
            getAuroraActionBar().setTitle(R.string.open_pass);
        }
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-18
        
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mNextButton.setEnabled(false);
        
        mHeaderText = (TextView) findViewById(R.id.headerText);
        mHeaderText.setText(mUiStage.alphaHint);
        mPasswordEntry = (TextView) findViewById(R.id.password_entry);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);
        //mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
        mKeyboardHelper = new PasswordEntryKeyboardHelper(this,
                mKeyboardView, mPasswordEntry);
        mKeyboardHelper.setKeyboardMode(PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);
        
        mKeyboardView.requestFocus();
        
        mPasswordEntry.setInputType((InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
        
        if (GnSettingsUtils.sGnSettingSupport) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
      	/*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
            getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                handleNext();
                break;

            case R.id.cancel_button:
                finish();
                break;
        }
    }
    
    private void handleNext() {
        // TODO Auto-generated method stub
        final String pass = mPasswordEntry.getText().toString();
        if (TextUtils.isEmpty(pass)) {
            return;
        }
        String errorMsg = null;
        if (mUiStage == Stage.SetPass) {
            errorMsg = validatePassword(pass);
            if (errorMsg == null) {
                mFirstPass = pass;
                mPasswordEntry.setText("");
                updateStage(Stage.NeedToConfirm);
            }
        } else if (mUiStage == Stage.NeedToConfirm) {
            if (mFirstPass.equals(pass)) {
                // save pass
                if(!mIsAlterPass){
                    Settings.Secure.putInt(getContentResolver(),
                            GN_GUEST_PASS_ENABLE, 1);
                }
                Settings.Secure.putString(getContentResolver(), GN_GUEST_PASS, pass);
                finish();
            } else {
                CharSequence tmp = mPasswordEntry.getText();
                if (tmp != null) {
                    Selection.setSelection((Spannable) tmp, 0, tmp.length());
                }
                updateStage(Stage.ConfirmWrong);
            }
        } else if(mUiStage == Stage.AlterPass){
            String guestPass =  Settings.Secure.getString(getContentResolver(), GN_GUEST_PASS);
            if(guestPass.equals(pass)){
                mPasswordEntry.setText("");
                updateStage(Stage.SetPass);
            }else{
                mHeaderText.setText(R.string.input_pass_wrong);
                // Gionee <qiuxd> <2013-06-18> add for CR00826661 begin 
                CharSequence tmp = mPasswordEntry.getText();
                if (tmp != null) {
                    Selection.setSelection((Spannable) tmp, 0, tmp.length());
                }
               // Gionee <qiuxd> <2013-06-18> add for CR00826661 end 
            }
        } else if(mUiStage == Stage.SetNewPass){
            errorMsg = validatePassword(pass);
            if(errorMsg == null){
                Settings.Secure.putString(getContentResolver(), GN_GUEST_PASS, pass);
                finish();
            }
        } else if(mUiStage == Stage.ValidatePass){
            String guestPass =  Settings.Secure.getString(getContentResolver(), GN_GUEST_PASS);
            if(guestPass.equals(pass)){
                Settings.Secure.putInt(getContentResolver(),
                        GN_GUEST_MODE, 0);
                // Gionee <qiuxd> <2013-05-13> add for CR00809610 begin
                //GnSettingsUtils.setPackageEnabled(getApplicationContext(),false);
                // Gionee <qiuxd> <2013-05-13> add for CR00809610 end
                finish();
            }else{
                mHeaderText.setText(R.string.input_pass_wrong);
                // Gionee <qiuxd> <2013-06-18> add for CR00826661 begin 
                CharSequence tmp = mPasswordEntry.getText();
                if (tmp != null) {
                    Selection.setSelection((Spannable) tmp, 0, tmp.length());
                }
               // Gionee <qiuxd> <2013-06-18> add for CR00826661 end 
            }
        } else if(mUiStage == Stage.ExitPass || mUiStage == Stage.OpenPass){
            String guestPass =  Settings.Secure.getString(getContentResolver(), GN_GUEST_PASS);
            if(guestPass.equals(pass)){
                if(mUiStage == Stage.ExitPass){
                    Settings.Secure.putInt(getContentResolver(),
                            GN_GUEST_PASS_ENABLE, 0);
                }else{
                    Settings.Secure.putInt(getContentResolver(),
                            GN_GUEST_PASS_ENABLE, 1);
                }
                finish();
            }else{
                mHeaderText.setText(R.string.input_pass_wrong);
                // Gionee <qiuxd> <2013-06-18> add for CR00826661 begin 
                CharSequence tmp = mPasswordEntry.getText();
                if (tmp != null) {
                    Selection.setSelection((Spannable) tmp, 0, tmp.length());
                }
               // Gionee <qiuxd> <2013-06-18> add for CR00826661 end 
            }
        } 
        if (errorMsg != null) {
            showError(errorMsg, mUiStage);
        }
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHOW_ERROR) {
                updateStage((Stage) msg.obj);
            }
        }
    };
    
    private void showError(String errorMsg, Stage next) {
        // TODO Auto-generated method stub
        mHeaderText.setText(errorMsg);
        mHeaderText.announceForAccessibility(mHeaderText.getText());
        Message mesg = mHandler.obtainMessage(MSG_SHOW_ERROR, next);
        mHandler.removeMessages(MSG_SHOW_ERROR);
        mHandler.sendMessageDelayed(mesg, ERROR_MESSAGE_TIMEOUT);
    }

    private void updateStage(Stage stage) {
        final Stage previousStage = mUiStage;
        mUiStage = stage;
        updateUi();

        // If the stage changed, announce the header for accessibility. This
        // is a no-op when accessibility is disabled.
        if (previousStage != stage) {
            mHeaderText.announceForAccessibility(mHeaderText.getText());
        }
    }

    private void updateUi() {
        // TODO Auto-generated method stub
        String password = mPasswordEntry.getText().toString();
        final int length = password.length();
        if(((mUiStage == Stage.SetPass) || (mUiStage == Stage.SetNewPass) )  && length > 0){
            if (length < mPasswordMinLength) {
                String msg = getString( R.string.guest_password_too_short, mPasswordMinLength);
                mHeaderText.setText(msg);
                mNextButton.setEnabled(false);
            } else {
                String error = validatePassword(password);
                if (error != null) {
                    mHeaderText.setText(error);
                    mNextButton.setEnabled(false);
                } else {
                    if(mUiStage == Stage.SetPass){
                        mHeaderText.setText(R.string.lockpassword_press_continue);
                    }else{
                        mHeaderText.setText(R.string.lockpassword_press_ok);
                    }
                    mNextButton.setEnabled(true);
                }
            }
        }else{
            mHeaderText.setText(mUiStage.alphaHint);
            mNextButton.setEnabled(length > 0);
            
            if(mUiStage == Stage.ConfirmWrong){
                mUiStage = Stage.NeedToConfirm;
            }
        }
        
        mNextButton.setText(mUiStage.buttonText);
    }

    private String validatePassword(String password) {
        if (password.length() < mPasswordMinLength) {
            return getString(R.string.guest_password_too_short, mPasswordMinLength);
        }
        if (password.length() > mPasswordMaxLength) {
            return getString(R.string.guest_password_too_long, mPasswordMaxLength + 1);
        }
        return null;
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

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        updateUi();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }
    
}
