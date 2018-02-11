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
package com.android.contacts.interactions;

import java.util.Timer;
import java.util.TimerTask;

import com.android.contacts.R;

import aurora.app.AuroraAlertDialog; 
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

/**
 * A common superclass for creating and renaming groups.
 */
public abstract class GroupNameDialogFragment extends DialogFragment
        implements TextWatcher, OnShowListener {
    private AuroraEditText mEdit;

    protected abstract int getTitleResourceId();
    protected abstract void initializeGroupLabelEditText(AuroraEditText editText);
    protected abstract void onCompleted(String groupLabel);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.group_name_dialog, null);
        mEdit = (AuroraEditText) view.findViewById(R.id.group_label);
        initializeGroupLabelEditText(mEdit);
        
        mEdit.addTextChangedListener(this);
        // aurora <wangth> <2013-10-25> add for aurora ui begin 
        mEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEdit.setSingleLine(true);
        mEdit.setTextSize(18);
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    ((InputMethodManager) (getActivity())
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0,
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }, 600);//old is 300
        //  aurora <wangth> <2013-10-25> add for aurora ui end

        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getActivity())
                .setTitle(getTitleResourceId())
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            onCompleted(mEdit.getText().toString().trim());
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,   
                //aurora add zhouxiaobing 20131218 start, old is null		
                		new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	onCancel(dialog);
                    }
                })//aurora add zhouxiaobing 20131218 end
                .create();

        dialog.setOnShowListener(this);
        return dialog;
    }

    public void onShow(DialogInterface dialog) {
        updateOkButtonState((AuroraAlertDialog) dialog);
    }

    @Override
    public void afterTextChanged(Editable s) {
        AuroraAlertDialog dialog = (AuroraAlertDialog) getDialog();
        // Make sure the dialog has not already been dismissed or destroyed.
        if (dialog != null) {
            updateOkButtonState(dialog);
        }
    }

    private void updateOkButtonState(AuroraAlertDialog dialog) {
        Button okButton = dialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(!TextUtils.isEmpty(mEdit.getText().toString().trim()));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
