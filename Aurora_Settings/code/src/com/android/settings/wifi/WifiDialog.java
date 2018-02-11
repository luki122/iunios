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

package com.android.settings.wifi;

import com.android.settings.R;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import aurora.widget.AuroraButton;

//Gionee <xuwen><2013-07-09> added for CR00834689 begin
import android.view.WindowManager;
//Gionee <xuwen><2013-07-09> added for CR00834689 end

class WifiDialog extends AuroraAlertDialog implements WifiConfigUiBase {
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    private final boolean mEdit;
    private final DialogInterface.OnClickListener mListener;
    private final AccessPoint mAccessPoint;

    private View mView;
    private WifiConfigController mController;
    
    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
    private static boolean mDialogState = false;
    //Gionee <xuwen><2013-07-09> added for CR00834689 end

    public WifiDialog(Context context, DialogInterface.OnClickListener listener,
            AccessPoint accessPoint, boolean edit) {
        super(context, R.style.Theme_WifiDialog);
        mEdit = edit;
        mListener = listener;
        mAccessPoint = accessPoint;
    }

    @Override
    public WifiConfigController getController() {
        return mController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);
        mController = new WifiConfigController(this, mView, mAccessPoint, mEdit);
        super.onCreate(savedInstanceState);
        /* During creation, the submit button can be unavailable to determine
         * visibility. Right after creation, update button visibility */
        mController.enableSubmitIfAppropriate();
    }

    @Override
    public boolean isEdit() {
        return mEdit;
    }
	
	// Aurora liugj 2013-10-24 modified for aurora's new feature start
    @Override
    public Button getSubmitButton() {
        return getButton(BUTTON_SUBMIT);
    }

    @Override
    public Button getForgetButton() {
        return getButton(BUTTON_FORGET);
    }

    @Override
    public Button getCancelButton() {
        return getButton(BUTTON_NEGATIVE);
    }
	// Aurora liugj 2013-10-24 modified for aurora's new feature end

    @Override
    public void setSubmitButton(CharSequence text) {
        setButton(BUTTON_SUBMIT, text, mListener);
    }

    @Override
    public void setForgetButton(CharSequence text) {
        setButton(BUTTON_FORGET, text, mListener);
    }
    
    @Override
    public void setModifyButton(CharSequence text) {
    	
    }
    
    @Override
    public void setForgetCommit() {
    	
    }

    @Override
    public void setCancelButton(CharSequence text) {
        setButton(BUTTON_NEGATIVE, text, mListener);
    }
    
    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
    public static void setDialogState(boolean bState) {
        mDialogState = bState;
    }

    public static boolean getDialogState() {
        return mDialogState;
    }
    //Gionee <xuwen><2013-07-09> added for CR00834689 end
}
