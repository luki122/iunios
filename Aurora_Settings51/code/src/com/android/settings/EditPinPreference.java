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

package com.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * TODO: Add a soft dialpad for PIN entry.
 */
//<Aurora><hujianwei>20150724 modify start 
import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraEditTextPreference;
import aurora.widget.AuroraEditText;
import android.content.DialogInterface;
//class EditPinPreference extends EditTextPreference {
class EditPinPreference extends AuroraEditTextPreference{
	private static boolean mOKIsEnable;
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
    }
    
    private OnPinEnteredListener mPinListener;
    
    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final EditText editText = getEditText();

        if (editText != null) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        }
    }

    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            showDialog(null);
        }
        AuroraAlertDialog d = (AuroraAlertDialog) getDialog();
        
        d.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(mOKIsEnable);
    }
}
//<Aurora><hujianwei>20150724 modify end
