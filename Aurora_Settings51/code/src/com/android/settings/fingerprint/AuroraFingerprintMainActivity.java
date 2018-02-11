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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;

import com.android.settings.R;
import com.mediatek.settings.sim.Log;

import java.util.HashMap;
import java.util.Map;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;

public class AuroraFingerprintMainActivity extends AuroraPreferenceActivity {
    static final String TAG = "AuroraFingerprintMainActivity";

    private static final int REQUEST_CODE_ADD_FINGERPRINT = 100;
    private static final int REQUEST_CODE_CONFIRM_PASSWORD = 101;
    private static final int REQUEST_CODE_EDIT_FINGERPRINT = 102;
    private static final int REQUEST_CODE_IDENTIFY_FINGERPRINT = 103;

    public static final String FAIL_REASON = "reason";
    public static final int FAIL_NORMAL = 0;
    public static final int FAIL_TIMEOUT = 1;
    public static final int FAIL_PAUSE = 2;

    private static final String KEY_MANAGE_CAT = "finger_cat";
    private static final String KEY_Finger_LOCK_SWITCH = "toggle_lock_switch";
    private static final String KEY_ADD_FINGERPRINT = "add_fingerprint";
    public static final String FINGERPRINT_ID_INCREASE = "fingerprint_id_increase";
    private static final String SYSTEM_PROPERY_FINGER_LOCK = "finger_function_status";

    private AuroraPreference mFingerAdd;
    private AuroraSwitchPreference mFingerLockSwitch;
    private AuroraPreferenceCategory mFingerManageCat;
    private int[] mFingerIdList;
    private String[] mFingerNameList;
    private AuroraFingerprintUtils mFingerPrintUtils;
    private int[] FINGER_ID = new int[]{1, 2, 3, 4, 5};
    private boolean mFinishExit = false;

    private int fingerMode = 0;
    private static final String FINGER_MODE = "finger_mode";
    private static final String FINGER_NAME_KEY = "finger_name_key";
    public static final String FINGER_ID_KEY = "finger_id_key";
    private static final int MODE_NORMAL = 0;
    private static final int MODE_CHOOSE = 1;
    private static final int MODE_ADD = 2;
    private final int CHOOSE_FAILED = 0;
    private final int CHOOSE_CUCCESSED = 1;
    private boolean identifyNeddPasswd = false;
    public static final String KEY_FINGER_ID = "key_finger_id";
    public static final String KEY_FINGER_NAME = "key_finger_name";
    public static final String KEY_FINGER_NAMES = "key_finger_names";
    private Handler mHandler = new Handler();
    private Map<Integer, String> idNameMap;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setAuroraContentView(R.layout.aurora_fingerprint_main, AuroraActionBar.Type.Normal);

        fingerMode = getIntent().getIntExtra(FINGER_MODE, MODE_NORMAL);
        Log.e(TAG, "onCreate fingerMode = " + fingerMode);

        initData();
        initViews();
        setListener();

        switch (fingerMode) {
            case MODE_NORMAL:
            case MODE_ADD:
                Intent intent = new Intent("aurora.intent.action.CONFIRM_PASSWORD");
                startActivityForResult(intent, REQUEST_CODE_CONFIRM_PASSWORD);
                mFinishExit = true;
                break;
            /*case MODE_CHOOSE:
                break;
            default:
                break;*/
        }
    }

    private void initData() {
        idNameMap = new HashMap<Integer, String>();
        mFingerPrintUtils = new AuroraFingerprintUtils();
        mFingerIdList = mFingerPrintUtils.getIds();
        mFingerNameList = mFingerPrintUtils.getNames();
        getIdNameMap();
    }

    private void initViews() {
        addPreferencesFromResource(R.xml.aurora_fingerprint_main_prefs);
        mFingerLockSwitch = (AuroraSwitchPreference) findPreference(KEY_Finger_LOCK_SWITCH);
        mFingerManageCat = (AuroraPreferenceCategory) findPreference(KEY_MANAGE_CAT);
        if (mFingerIdList != null && mFingerIdList.length > 0) {
            if (mFingerNameList == null) {
                finish();
            }
            for (int i = 0; i < mFingerIdList.length; i++) {
                AuroraPreference pref = new AuroraPreference(this);
                mFingerManageCat.addPreference(pref);
                pref.setKey("gnfinger" + mFingerIdList[i]);
                pref.setTitle(mFingerNameList[i]);
            }
        }
        getAddFingerPref();
    }

    private void initSwitch() {
        int lockStatus = Settings.System.getInt(getContentResolver(), SYSTEM_PROPERY_FINGER_LOCK, 0);
        lockStatus = lockStatus & 0x1;
        if (lockStatus == 1 && (mFingerIdList != null && mFingerIdList.length > 0)) {
            mFingerLockSwitch.setChecked(true);
        }
    }

    private void setListener() {
        mFingerLockSwitch.setOnPreferenceChangeListener(new AuroraPreference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(AuroraPreference auroraPreference, Object objValue) {
                boolean isChecked = (Boolean) objValue;
                if (isChecked) {
                    if (mFingerIdList == null || mFingerIdList.length == 0) {
                        gotoAddFingerPage();
                    } else if (mFingerIdList.length > 0) {
                        enableFingerLock();
                    }
                } else {
                    disableFingerLock();
                }

                return true;
            }
        });
    }

    private void disableFingerLock() {
        int value = getFingerFunctionProperty();
        value = value & ~(1 << 0); //置1 ：a=a|(1<<k)；置0:a=a&~(1<<k)；
        setFingerFunctionProperty(value);
    }

    private void enableFingerLock() {
        int value = getFingerFunctionProperty();
        value = value | 0x1; //置1 ：a=a|(1<<k)；置0:a=a&~(1<<k)；
        setFingerFunctionProperty(value);
    }

    private void setFingerFunctionProperty(int value) {
        Settings.System.putInt(getContentResolver(), SYSTEM_PROPERY_FINGER_LOCK, value);
    }

    private int getFingerFunctionProperty() {
        return Settings.System.getInt(getContentResolver(), SYSTEM_PROPERY_FINGER_LOCK, 0);
    }

    private void getIdNameMap() {
        if (mFingerIdList == null || idNameMap == null) {
            return;
        }
        idNameMap.clear();
        int length = mFingerIdList.length;
        for (int i = 0; i < length; i++) {
            idNameMap.put(mFingerIdList[i], mFingerNameList[i]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (mFingerIdList != null) {
            if (mFingerIdList.length == FINGER_ID.length) {
                mFingerAdd.setEnabled(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mFingerPrintUtils.cancel();
        if (!mFinishExit) {
            finish();
        }
    }


    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
                                         AuroraPreference preference) {
        final String key = preference.getKey();
        switch (fingerMode) {
            case MODE_NORMAL:
                if (KEY_ADD_FINGERPRINT.equals(key)) {
                    gotoAddFingerPage();
                } else if (key.contains("gnfinger")) {
                    mFinishExit = true;
                    Intent intent = new Intent();
                    intent.setClass(this, AuroraFingerprintEditActivity.class);
                    String temp = key;
                    temp = temp.replace("gnfinger", "");
                    int id = Integer.valueOf(temp);
                    Bundle bundle = new Bundle();
                    //intent.putExtra(KEY_FINGER_ID, id);
                    //intent.putStringArrayListExtra(KEY_FINGER_NAME, StringUtil.stringsToList(mFingerNameList));
                    bundle.putInt(KEY_FINGER_ID, id);
                    bundle.putStringArray(KEY_FINGER_NAMES, mFingerNameList);
                    bundle.putString(KEY_FINGER_NAME, idNameMap.get(id));
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_CODE_EDIT_FINGERPRINT);
                } else {
                    return super.onPreferenceTreeClick(preferenceScreen, preference);
                }
                break;
            case MODE_CHOOSE:
                if (KEY_ADD_FINGERPRINT.equals(key)) {
                    gotoAddFingerPage();
                } else if (key.contains("gnfinger")) {
                    mFinishExit = true;
                    String temp = key;
                    temp = temp.replace("gnfinger", "");
                    int id = Integer.valueOf(temp);
                    Intent idIntent = new Intent(AuroraFingerprintMainActivity.this, AuroraFingerprintIDactivity.class);
                    idIntent.putExtra(FINGER_ID_KEY, new int[]{id});
                    idIntent.putExtra(AuroraFingerprintIDactivity.IS_NEED_PASSWD, identifyNeddPasswd);
                    startActivityForResult(idIntent, REQUEST_CODE_IDENTIFY_FINGERPRINT);
                }
                break;
            case MODE_ADD:
                break;
            default:
                break;
        }

        return true;
    }

    private void gotoAddFingerPage() {
        mFinishExit = true;
        Intent intent = new Intent();
        intent.putExtra(KEY_FINGER_ID, getAddFingerId());
        intent.putExtra(KEY_FINGER_NAME, getAddFingerName());
        intent.setClass(this, AuroraFingerprintAddActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADD_FINGERPRINT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "AuroraFingerprintMainActivity onActivityResult resultCode = "
                + resultCode + " fingMode " + fingerMode);
        switch (fingerMode) {
            case MODE_NORMAL:
                if (requestCode == REQUEST_CODE_ADD_FINGERPRINT
                        || requestCode == REQUEST_CODE_EDIT_FINGERPRINT) {
                    mFinishExit = false;
                    if (resultCode == RESULT_OK) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mFingerIdList = mFingerPrintUtils.getIds();
                            }
                        }, 100);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                initPreference();
                                getIdNameMap();
                                freshFingerFunction();
                            }
                        }, 200);
                    } else {
                        if (data != null) {
                            int type = data.getIntExtra(FAIL_REASON, FAIL_NORMAL);
                            if (FAIL_TIMEOUT == type || FAIL_PAUSE == type) {
                                finish();
                            }
                        }
                        freshSwitchBar();
                    }
                } else if (requestCode == REQUEST_CODE_CONFIRM_PASSWORD) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (mFingerIdList == null || mFingerIdList.length == 0) {
                            Intent intent = new Intent();
                            intent.putExtra(KEY_FINGER_ID, getAddFingerId());
                            intent.putExtra(KEY_FINGER_NAME, getAddFingerName());
                            intent.setClass(this, AuroraFingerprintAddActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_ADD_FINGERPRINT);
                            mFinishExit = true;
                        } else {
                            mFinishExit = false;
                        }
                        initSwitch();
                    } else {
                        finish();
                    }
                }
                break;
            case MODE_CHOOSE:
                Log.i(TAG, "onActivityResult MODE_CHOOSE");
                if (requestCode == REQUEST_CODE_ADD_FINGERPRINT
                        || requestCode == REQUEST_CODE_EDIT_FINGERPRINT) {
                    mFinishExit = false;
                    if (resultCode == RESULT_OK) {
                        int id = data.getIntExtra(AuroraFingerprintAddActivity.RETURN_FINGER_ID, 0);
                        Intent backIntent = new Intent();
                        backIntent.putExtra(FINGER_ID_KEY, id);
                        backIntent.putExtra(FINGER_NAME_KEY, mFingerPrintUtils.getNameById(id));
                        setResult(CHOOSE_CUCCESSED, backIntent);
                        Log.i(TAG, "onActivityResult MODE_CHOOSE finish() SUCCESSS");
                        finish();
                    }
                } else if (requestCode == REQUEST_CODE_IDENTIFY_FINGERPRINT) {
                    mFinishExit = false;
                    if (resultCode == AuroraFingerprintIDactivity.IDENTIFY_SUCCESSED) {
                        Intent backIntent = new Intent();
                        int id = data.getIntExtra(FINGER_ID_KEY, -1);
                        backIntent.putExtra(FINGER_ID_KEY, id);
                        backIntent.putExtra(FINGER_NAME_KEY, mFingerPrintUtils.getNameById(id));
                        setResult(CHOOSE_CUCCESSED, backIntent);
                        finish();
                    } else if (resultCode == AuroraFingerprintIDactivity.IDENTIFY_FAILED) {
                        identifyNeddPasswd = true;
                    }
                }
                break;
            case MODE_ADD:
                if (requestCode == REQUEST_CODE_CONFIRM_PASSWORD) {
                    if (resultCode == Activity.RESULT_OK) {
                        gotoAddFingerPage();
                    } else {
                        setResult(CHOOSE_FAILED);
                        finish();
                    }
                } else if (requestCode == REQUEST_CODE_ADD_FINGERPRINT) {
                    if (data != null) {
                        int id = data.getIntExtra(AuroraFingerprintAddActivity.RETURN_FINGER_ID, -1);
                        if (id > 0) {
                            Intent backIntent = new Intent();
                            backIntent.putExtra(FINGER_ID_KEY, id);
                            backIntent.putExtra(FINGER_NAME_KEY, mFingerPrintUtils.getNameById(id));
                            setResult(CHOOSE_CUCCESSED, backIntent);
                            enableFingerFunction();
                        }
                    } else {
                        setResult(CHOOSE_FAILED);
                    }
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void freshFingerFunction() {
        if (mFingerIdList != null && mFingerIdList.length > 0) {
            mFingerLockSwitch.setChecked(true);
            enableFingerFunction();
        } else {
            mFingerLockSwitch.setChecked(false);
            int value = getFingerFunctionProperty();
            value = value & ~(1 << 0); //置1 ：a=a|(1<<k)；置0:a=a&~(1<<k)；
            value = value & ~(1 << 1);
            setFingerFunctionProperty(value);
        }
    }

    private void freshSwitchBar() {
        if (mFingerIdList == null) {
            mFingerLockSwitch.setChecked(false);
        }
    }

    private void enableFingerFunction() {
        int value = getFingerFunctionProperty();
        value = value | (1 << 0); //置1 ：a=a|(1<<k)；置0:a=a&~(1<<k)；
        value = value | (1 << 1);
        setFingerFunctionProperty(value);
    }

    private void initPreference() {
        mFingerNameList = mFingerPrintUtils.getNames();
        mFingerManageCat.removeAll();
        if (mFingerIdList != null && mFingerIdList.length > 0) {
            if (mFingerNameList == null) {
                finish();
            }
            int length = mFingerIdList.length;
            for (int i = 0; i < length; i++) {
                AuroraPreference pref = new AuroraPreference(this);
                mFingerManageCat.addPreference(pref);
                pref.setKey("gnfinger" + mFingerIdList[i]);
                pref.setTitle(mFingerNameList[i]);
            }
        }
        getAddFingerPref();
        mFingerManageCat.addPreference(mFingerAdd);
    }

    private void getAddFingerPref() {
        mFingerAdd = new AuroraPreference(this);
        mFingerAdd.setKey(KEY_ADD_FINGERPRINT);
        mFingerAdd.setIcon(R.drawable.aurora_settings_finger_add);
        mFingerAdd.setTitle(R.string.add_fingerprint);
        if (mFingerIdList != null && mFingerIdList.length >= FINGER_ID.length) {
            mFingerAdd.setSummary(R.string.add_fingerprint_summary);
            mFingerAdd.setEnabled(false);
        }
        mFingerManageCat.addPreference(mFingerAdd);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fingerMode == MODE_CHOOSE) {
                setResult(CHOOSE_FAILED);
                Log.i(TAG, "onActivityResult MODE_CHOOSE finish() FAILED");
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getAddFingerId() {
        int addId = Settings.Secure.getInt(getContentResolver(),
                FINGERPRINT_ID_INCREASE, 0);
        addId++;
        Settings.Secure.putInt(getContentResolver(), FINGERPRINT_ID_INCREASE, addId);
        Log.i(TAG, "getAddFingerId addId=" + addId);
        return addId;
    }

    private String getAddFingerName() {
        String name = null;
        String realName = null;

        int addId = 1;
        if (mFingerIdList == null) {
            name = getString(R.string.fingerprint) + addId;
            return name;
        }

        for (int i = 0; i < FINGER_ID.length; i++) {
            addId = FINGER_ID[i];
            name = getString(R.string.fingerprint) + addId;
            boolean flag = true;

            for (int j = 0; j < mFingerIdList.length; j++) {
                //realName = mFingerPrintUtils.getNameById(mFingerIdList[j]);
                if (name.equals(mFingerNameList[j])) {
                    //if (name.equals(realName)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                break;
            }
        }
        return name;
    }

    @Override
    public void onBackPressed() {
        if (fingerMode != MODE_NORMAL) {
            setResult(CHOOSE_FAILED);
        }
        super.onBackPressed();
    }

}
