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

package com.android.settings.fingerprint;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;

public class AuroraFingerprintEditActivity extends AuroraPreferenceActivity {
    private static final String TAG = "AuroraFingerprintEditActivity";

    private static final int MAX_NAME_LEN = 10;
    private AuroraFingerprintUtils mFingerPrintUtils;
    private int mFingerId = 0;
    private String mName = null;
    private AuroraEditText mFingerNameEdit = null;
    private AuroraButton mButtonDel;
    private String[] mFingerNameList;
    private final String KEY_EDIT_CAT = "edit_cat";
    private final String KEY_RENAME = "key_rename";
    private final String KEY_DELETE = "key_delete";
    private AuroraPreferenceCategory mFingerManageCat;
    private boolean isEditTvInit = false;
    private AuroraAlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setAuroraContentView(R.layout.aurora_fingerprint_edit, AuroraActionBar.Type.Normal);

        initData();
        initViews();
        initActionBar();
    }

    private void initActionBar() {
        getAuroraActionBar().setTitle(mName);
    }

    private void initViews() {
        addPreferencesFromResource(R.xml.aurora_fingerprint_edit_prefs);
        mFingerManageCat = (AuroraPreferenceCategory) findPreference(KEY_EDIT_CAT);
        //重命名
        AuroraPreference pref = new AuroraPreference(this);
        mFingerManageCat.addPreference(pref);
        pref.setKey(KEY_RENAME);
        pref.setTitle(getString(R.string.rename_fingerprint));
        //删除
        pref = new AuroraPreference(this);
        mFingerManageCat.addPreference(pref);
        pref.setKey(KEY_DELETE);
        pref.setTitle(getString(R.string.delete));
    }

    private void initData() {
        mFingerPrintUtils = new AuroraFingerprintUtils();
        //mFingerNameList = mFingerPrintUtils.getNames();
        //mName = mFingerPrintUtils.getNameById(mFingerId);
        mFingerNameList = getIntent().getStringArrayExtra(AuroraFingerprintMainActivity.KEY_FINGER_NAMES);
        mFingerId = getIntent().getIntExtra(AuroraFingerprintMainActivity.KEY_FINGER_ID, 0);
        mName = getIntent().getStringExtra(AuroraFingerprintMainActivity.KEY_FINGER_NAME);
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
                                         AuroraPreference preference) {
        final String key = preference.getKey();
        if (KEY_RENAME.equals(key)) {
            Log.e(TAG, "KEY_RENAME");
            showEditFingerDialog();
        } else if (KEY_DELETE.equals(key)) {
            Log.e(TAG, "KEY_DELETE");
            showDeleteFingerDialog();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.putExtra(AuroraFingerprintMainActivity.FAIL_REASON, AuroraFingerprintMainActivity.FAIL_PAUSE);
        //setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void saveFingerName(String newName) {
        if (newName == null || newName.length() == 0) {
            Toast.makeText(AuroraFingerprintEditActivity.this,
                    R.string.aurora_fingerprint_edit_save_note, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mName.equals(newName)) {
            finish();
            return;
        }

        if (isFingerNameExist(newName)) {
            Toast.makeText(AuroraFingerprintEditActivity.this,
                    R.string.aurora_fingerprint_name_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e(TAG, "mFingerId = " + mFingerId + " newName = " + newName);
        mFingerPrintUtils.renameById(mFingerId, newName);

        setResult(RESULT_OK);
        finish();

        return;

    }

    private boolean isFingerNameExist(String name) {
        if (mFingerNameList != null) {
            for (int i = 0; i < mFingerNameList.length; i++) {
                if (mFingerNameList[i].compareTo(String.valueOf(name)) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showDeleteFingerDialog() {
        final Context context = this;
        final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        builder.setTitle(getString(R.string.aurora_fingerprint_delete_dialog_title))
                .setMessage(getString(R.string.aurora_fingerprint_delete_dialog_msg))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFingerPrintUtils.removeData(mFingerId);
                        setResult(RESULT_OK);
                        finish();
                        /*new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Object result = mFingerPrintUtils.removeData(mFingerId);
                                if (result == null) {
                                    Log.e(TAG, "RESULT == NULL");
                                } else {
                                    Log.e(TAG, "RESULT != NULL " + result.toString());
                                }
                                *//*if (result) {
                                    setResult(RESULT_OK);
                                    finish();
                                }*//*
                            }
                        }).start();*/
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditFingerDialog() {
        if (!isEditTvInit) {
            isEditTvInit = true;
            mFingerNameEdit = new AuroraEditText(this);
            mFingerNameEdit.setText(mName);
            mFingerNameEdit.setMaxLines(1);
            mFingerNameEdit.requestFocus();
            mFingerNameEdit.addTextChangedListener(mNameWatcher);
        }

        final Context context = this;
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
        builder.setView(mFingerNameEdit);
        builder.setTitle(R.string.aurora_fingerprint_edit_dialog_title)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "mFingerNameEdit.getText = " + mFingerNameEdit.getText());
                        saveFingerName(mFingerNameEdit.getText().toString());
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
        mFingerNameEdit.setText(mName);
        if (mName != null && mName.length() > 0) {
            Log.e(TAG, "mName.length = " + mName.length());
            mFingerNameEdit.setSelection(0, mName.length());
        }
    }

    private final TextWatcher mNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                if (s.length() > MAX_NAME_LEN) {
                    String newStr = s.toString().substring(0, MAX_NAME_LEN);
                    mFingerNameEdit.setText(newStr);
                    mFingerNameEdit.setSelection(newStr.length());
                    Toast.makeText(AuroraFingerprintEditActivity.this,
                            R.string.aurora_fingerprint_name_len_note, Toast.LENGTH_SHORT).show();
                }
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
            } else {
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

}
