/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.os.storage.ExternalStorageFormatter;

import java.util.Locale;

import aurora.app.AuroraAlertActivity;
import aurora.app.AuroraAlertDialog;

/**
 * Confirm and execute a format of the sdcard.
 * Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE SD CARD" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 */
public class MediaFormat extends AuroraAlertActivity {

    private static final String TAG = "MediaFormat";

    private static final int KEYGUARD_REQUEST = 55;

    private static final int DIALOG_FORMAT_SDCARD = 0;

    private static final int DIALOG_FORMAT_OTG = 1;

    private LayoutInflater mInflater;

    private View mInitialView;
    private Button mOkButton;

    private View mFinalView;
    private Button mCancelButton;

    ///M:
    private StorageVolume mVolume;

    private Context mContext;

    private Resources mRes;

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Mount Service to format the SD card.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
        public void onClick(View v) {

            if (Utils.isMonkeyRunning()) {
                return;
            }
            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            // Transfer the storage volume to the new intent
            intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
            mContext.startService(intent);
            dismiss();
        }
    };

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     */
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper((AuroraAlertActivity) mContext)
                .launchConfirmationActivity(request, null,
                        getDescription(mVolume, R.string.media_format_gesture_explanation));
    }

    public void setExtra(StorageVolume volume) {
        mVolume = volume;
    }

//    public MediaFormat(Context context){
//    	super(context,com.aurora.R.style.Theme_Aurora_Light_Dialog_Alert);
//    	mContext = context;
//    	mRes = mContext.getResources();
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            establishFinalConfirmationState();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            dismiss();
        } else {
            //establishInitialState();
        }
    }


    private boolean isUSBDevices() {
        return mVolume.getPath().startsWith(Environment.DIRECTORY_USBOTG);
    }


    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private View.OnClickListener mInitiateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == android.R.id.button1) {
                Log.e(TAG, "nav dismmis");
                finish();
                overridePendingTransition(com.aurora.R.anim.aurora_dialog_enter, com.aurora.R.anim.aurora_dialog_exit);
            } else if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                establishFinalConfirmationState();
            }
        }
    };

    //        @Override
    public void showConfirmDialog(int dialogId) {
        Dialog dialog = createDialog(dialogId);
        dialog.show();
    }
//        <string name="format_sd_card_dialog_title">Format the SD-Card</string>
//        <string name="format_otg_dialog_title">Format the USB storage devices</string>

    public android.app.Dialog createDialog(int id) {

        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
        builder.setMessage(R.string.confirm_format_msg);
        builder.setPositiveButton(R.string.comfirm_format_button_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                if (Utils.isMonkeyRunning()) {
                    return;
                }
                Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                // Transfer the storage volume to the new intent
                intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, mVolume);
                mContext.startService(intent);
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.format_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //dialog.dismiss();
                dismiss();
            }
        });
        switch (id) {
            case DIALOG_FORMAT_OTG:
                builder.setTitle(R.string.format_otg_dialog_title);
                break;
            case DIALOG_FORMAT_SDCARD:
                builder.setTitle(R.string.format_sd_card_dialog_title);
                break;
            default:
                break;
        }
        AuroraAlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        if (isUSBDevices()) {
            showConfirmDialog(DIALOG_FORMAT_OTG);
        } else {
            showConfirmDialog(DIALOG_FORMAT_SDCARD);
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mContext = this;
        mRes = mContext.getResources();
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(mContext);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mVolume = bundle.getParcelable(StorageVolume.EXTRA_STORAGE_VOLUME);
        }

        //establishInitialState();

        LayoutInflater.from(this).inflate(com.aurora.R.layout.aurora_alert_dialog, null);
        mAlertParams.mTitle = getDescription(mVolume, R.string.media_format_title);
        mAlertParams.mMessage = getDescription(mVolume, R.string.media_format_desc);
        mAlertParams.mPositiveButtonText = getString(R.string.media_format);
        mAlertParams.mNegativeButtonText = getString(R.string.format_cancel);
        setupAlert();
        initViews();
    }

    private void establishInitialState() {
        //etDescription(mVolume, R.string.media_format_button_text)
        /*setContentView(R.layout.media_format_ly);
        setContentView(com.aurora.R.layout.aurora_alert_dialog);*/

        TextView initialText = (TextView) findViewById(android.R.id.message);
        TextView title = (TextView) findViewById(com.aurora.R.id.aurora_alertTitle);
        title.setText(getDescription(mVolume, R.string.media_format_title));
        initialText.setText(getDescription(mVolume, R.string.media_format_desc));
        View middleButton = findViewById(android.R.id.button3);
        middleButton.setVisibility(View.GONE);
        mOkButton =
                (Button) findViewById(android.R.id.button2);
        mOkButton.setText(R.string.continue_format_storage);

        mCancelButton = (Button) findViewById(android.R.id.button1);
        mCancelButton.setText(R.string.format_cancel);
        mCancelButton.setOnClickListener(mInitiateListener);
        mOkButton.setOnClickListener(mInitiateListener);
    }

    private void initViews() {
        mOkButton = (Button) findViewById(android.R.id.button2);
        mCancelButton = (Button) findViewById(android.R.id.button1);
        mCancelButton.setOnClickListener(mInitiateListener);
        mOkButton.setOnClickListener(mInitiateListener);
    }

    /**
     * Abandon all progress through the confirmation sequence by returning
     * to the initial view any time the activity is interrupted (e.g. by
     * idle timeout).
     */
    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            //establishInitialState();
        }
    }

    ///M:
    private String getDescription(StorageVolume volume, int stringId) {
        String volumeDescription = volume.getDescription(mContext);
        String path = volume.getPath();
        boolean isInternalSD = !volume.isRemovable();
        boolean isOTG = volume.getPath().startsWith(Environment.DIRECTORY_USBOTG);

        if (volumeDescription == null || (!isInternalSD && !isOTG)) {
            Log.d(TAG, "Volume volumeDescription is null or it is an external sd card");
            return mRes.getString(stringId);
        }
        //SD card string
        String sdCardString = mRes.getString(R.string.sdcard_setting);
        String description = mRes.getString(stringId).replace(sdCardString, volumeDescription);
        if (description != null && description.equals(mRes.getString(stringId))) {
            sdCardString = sdCardString.toLowerCase();
            // restore to SD
            sdCardString = sdCardString.replace("sd", "SD");
            description = mRes.getString(stringId).replace(sdCardString, volumeDescription);
        }
        if (description != null && description.equals(mRes.getString(stringId))) {
            description = mRes.getString(stringId).replace("SD", volumeDescription);
        }
        Locale tr = Locale.getDefault();
        // For chinese there is no space
        if (tr.getCountry().equals(Locale.CHINA.getCountry())
                || tr.getCountry().equals(Locale.TAIWAN.getCountry())) {
            description = description.replace(" " + volumeDescription, volumeDescription);
        }
        return description;
    }
}
