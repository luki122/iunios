/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.phone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.sip.SipSharedPreferences;

import aurora.preference.*;
import aurora.app.*;
import android.os.Build;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 *
 * This preference screen is the root of the "MSim Call settings" hierarchy
 * available from the Phone app; the settings here let you control various
 * features related to phone calls (including voicemail settings, SIP
 * settings, the "Respond via SMS" feature, and others.)  It's used only
 * on voice-capable phone devices.
 *
 * Note that this activity is part of the package com.android.phone, even
 * though you reach it from the "Phone" app (i.e. DialtactsActivity) which
 * is from the package com.android.contacts.
 *
 * For the "MSim Mobile network settings" screen under the main Settings app,
 * See {@link MSimMobileNetworkSettings}.
 *
 * @see com.android.phone.MSimMobileNetworkSettings
 */
public class MSimCallFeaturesSetting extends AuroraPreferenceActivity
        implements AuroraPreference.OnPreferenceChangeListener{
    private static final String LOG_TAG = "MSimCallFeaturesSetting";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    //Information about logical "up" Activity
    private static final String UP_ACTIVITY_PACKAGE = "com.android.dialer";
    private static final String UP_ACTIVITY_CLASS =
            "com.android.dialer.DialtactsActivity";

    // String keys for preference lookup
    // TODO: Naming these "BUTTON_*" is confusing since they're not actually buttons(!)
    private static final String BUTTON_PLAY_DTMF_TONE  = "button_play_dtmf_tone";
    private static final String BUTTON_DTMF_KEY        = "button_dtmf_settings";
    private static final String BUTTON_RETRY_KEY       = "button_auto_retry_key";
    private static final String BUTTON_HAC_KEY         = "button_hac_key";
    private static final String BUTTON_DIALPAD_AUTOCOMPLETE = "button_dialpad_autocomplete";
    private static final String BUTTON_XDIVERT_KEY     = "button_xdivert";

    private static final String BUTTON_SIP_CALL_OPTIONS =
            "sip_call_options_key";
    private static final String BUTTON_SIP_CALL_OPTIONS_WIFI_ONLY =
            "sip_call_options_wifi_only_key";
    private static final String SIP_SETTINGS_CATEGORY_KEY =
            "sip_settings_category_key";



    public static final String HAC_KEY = "HACSetting";
    public static final String HAC_VAL_ON = "ON";
    public static final String HAC_VAL_OFF = "OFF";

    private Phone mPhone;
    private boolean mForeground;
    private AudioManager mAudioManager;
    private SipManager mSipManager;

    /** Whether dialpad plays DTMF tone or not. */
    private AuroraCheckBoxPreference mPlayDtmfTone;
    private AuroraCheckBoxPreference mDialpadAutocomplete;
    private AuroraCheckBoxPreference mButtonAutoRetry;
    private AuroraCheckBoxPreference mButtonHAC;
    private AuroraListPreference mButtonDTMF;
    private AuroraListPreference mButtonSipCallOptions;
    private SipSharedPreferences mSipSharedPreferences;
    private AuroraPreference mCf1, mCf2, mCw1,mCw2, mSimCategory1, mSimCategory2;
    

    private AuroraPreferenceScreen mButtonXDivert;
    private int mNumPhones;

    /*
     * Click Listeners, handle click based on objects attached to UI.
     */

    // Click listener for all toggle events
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference == mButtonDTMF) {
            return true;
        }/* else if (preference == mDialpadAutocomplete) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DIALPAD_AUTOCOMPLETE,
                    mDialpadAutocomplete.isChecked() ? 1 : 0);//aurora change zhouxiaobing 20140512 for 4.4 build and the 4.4 phone is not have this
        } */
        else if (preference == mButtonAutoRetry) {
            android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.CALL_AUTO_RETRY,
                    mButtonAutoRetry.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mButtonHAC) {
            int hac = mButtonHAC.isChecked() ? 1 : 0;
            // Update HAC value in Settings database
            Settings.System.putInt(mPhone.getContext().getContentResolver(),
                    Settings.System.HEARING_AID, hac);

            // Update HAC Value in AudioManager
            mAudioManager.setParameter(HAC_KEY, hac != 0 ? HAC_VAL_ON : HAC_VAL_OFF);
            return true;
        }
        return false;
    }

    /**
     * Implemented to support onPreferenceChangeListener to look for preference
     * changes.
     *
     * @param preference is the preference to be changed
     * @param objValue should be the value of the selection, NOT its localized
     * display value.
     */
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        if (DBG) {
            log("onPreferenceChange(). preferenece: \"" + preference + "\""
                    + ", value: \"" + objValue + "\"");
        }
        if (preference == mButtonDTMF) {
            int index = mButtonDTMF.findIndexOfValue((String) objValue);
            Settings.System.putInt(mPhone.getContext().getContentResolver(),
                    Settings.System.DTMF_TONE_TYPE_WHEN_DIALING, index);
        }  else if (preference == mButtonSipCallOptions) {
            handleSipCallOptionsChange(objValue);
        }
        // always let the preference setting proceed.
        return true;
    }

    /*
     * Activity class methods
     */

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (DBG) log("onCreate(). Intent: " + getIntent());
        new Thread(new Runnable() {
            @Override
            public void run() {
            	ReporterUtils.addSettingCount();
            }
        }).start();

        mPhone = PhoneGlobals.getInstance().getPhone();

        addPreferencesFromResource(R.xml.msim_call_feature_setting);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        // get buttons
        AuroraPreferenceScreen prefSet = getPreferenceScreen();

        mPlayDtmfTone = (AuroraCheckBoxPreference) findPreference(BUTTON_PLAY_DTMF_TONE);
        //mDialpadAutocomplete = (AuroraCheckBoxPreference) findPreference(BUTTON_DIALPAD_AUTOCOMPLETE);//aurora change zhouxiaobing 20140512 for 4.4 build and the 4.4 phone is not have this
        mButtonDTMF = (AuroraListPreference) findPreference(BUTTON_DTMF_KEY);
        mButtonAutoRetry = (AuroraCheckBoxPreference) findPreference(BUTTON_RETRY_KEY);
        mButtonHAC = (AuroraCheckBoxPreference) findPreference(BUTTON_HAC_KEY);
        mButtonXDivert = (AuroraPreferenceScreen) findPreference(BUTTON_XDIVERT_KEY);

        final ContentResolver contentResolver = getContentResolver();

        if (mPlayDtmfTone != null) {
            mPlayDtmfTone.setChecked(Settings.System.getInt(contentResolver,
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) != 0);
        }

/*        if (mDialpadAutocomplete != null) {
            mDialpadAutocomplete.setChecked(Settings.Secure.getInt(contentResolver,
                    Settings.Secure.DIALPAD_AUTOCOMPLETE, 0) != 0);
        }*///aurora change zhouxiaobing 20140512 for 4.4 build and the 4.4 phone is not have this

        if (mButtonDTMF != null) {
            if (getResources().getBoolean(R.bool.dtmf_type_enabled)) {
                mButtonDTMF.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonDTMF);
                mButtonDTMF = null;
            }
        }

        if (mButtonAutoRetry != null) {
            if (getResources().getBoolean(R.bool.auto_retry_enabled)) {
                mButtonAutoRetry.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonAutoRetry);
                mButtonAutoRetry = null;
            }
        }

        if (mButtonHAC != null) {
            if (getResources().getBoolean(R.bool.hac_enabled)) {

                mButtonHAC.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonHAC);
                mButtonHAC = null;
            }
        }

   

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mNumPhones = PhoneUtils.getPhoneCount();
        
//        AuroraPreference mOtherPreferenceCategory = findPreference("button_misc_category_key");
//        if(mOtherPreferenceCategory != null) {
//            prefSet.removePreference(mOtherPreferenceCategory);
//        }
        if(mPlayDtmfTone != null) {
            prefSet.removePreference(mPlayDtmfTone);
        }
/*        if(mDialpadAutocomplete != null) {
            prefSet.removePreference(mDialpadAutocomplete);
        }*///aurora change zhouxiaobing 20140512 for 4.4 build and the 4.4 phone is not have this
        if(mButtonXDivert != null) {
            prefSet.removePreference(mButtonXDivert);
        }           
        
        mCf1= findPreference("button_cf_expand_key1");
        mCf1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
        mCf2= findPreference("button_cf_expand_key2");
        mCf2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mCw1= findPreference("button_more_expand_key1");
        mCw1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
        mCw2= findPreference("button_more_expand_key2");
        mCw2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mSimCategory1= findPreference("sim1_category_key");
        mSimCategory2= findPreference("sim2_category_key");
        
        AuroraListPreference mButtonTTY = (AuroraListPreference) findPreference("button_tty_mode_key");
        if (mButtonTTY != null) {
            prefSet.removePreference(mButtonTTY);
            mButtonTTY = null;            
        }
        
    }


    private boolean isAnySubCdma() {
        for (int i = 0; i < mNumPhones; i++) {
            Phone phone = PhoneGlobals.getInstance().getPhone(i);
            if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) return true;
        }
        return false;
    }

    private boolean isValidLine1Number(String[] line1Numbers) {
        for (int i = 0; i < mNumPhones; i++) {
            if (TextUtils.isEmpty(line1Numbers[i])) return false;
        }
        return true;
    }


    private void displayAlertDialog(int resId) {
        new AlertDialog.Builder(this).setMessage(resId)
            .setTitle(R.string.xdivert_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(LOG_TAG, "X-Divert onClick");
                    }
                })
            .show()
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(LOG_TAG, "X-Divert onDismiss");
                    }
            });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mForeground = true;

        if (isAirplaneModeOn()) {
            AuroraPreference sipSettings = findPreference(SIP_SETTINGS_CATEGORY_KEY);
            AuroraPreferenceScreen screen = getPreferenceScreen();
            int count = screen.getPreferenceCount();
            for (int i = 0 ; i < count ; ++i) {
                AuroraPreference pref = screen.getPreference(i);
                if (pref != sipSettings) pref.setEnabled(false);
            }
            return;
        }

        if (mButtonDTMF != null) {
            int dtmf = Settings.System.getInt(getContentResolver(),
                    Settings.System.DTMF_TONE_TYPE_WHEN_DIALING, Constants.DTMF_TONE_TYPE_NORMAL);
            mButtonDTMF.setValueIndex(dtmf);
        }

        if (mButtonAutoRetry != null) {
            int autoretry = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.CALL_AUTO_RETRY, 0);
            mButtonAutoRetry.setChecked(autoretry != 0);
        }

        if (mButtonHAC != null) {
            int hac = Settings.System.getInt(getContentResolver(), Settings.System.HEARING_AID, 0);
            mButtonHAC.setChecked(hac != 0);
        }

    

        
        updateUiState();
    }
    
    protected void onPause() {
        if (DBG) log("onPause()...");
        super.onPause();
    }

    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
   

    private void handleSipCallOptionsChange(Object objValue) {
        String option = objValue.toString();
        mSipSharedPreferences.setSipCallOption(option);
        mButtonSipCallOptions.setValueIndex(
                mButtonSipCallOptions.findIndexOfValue(option));
        mButtonSipCallOptions.setSummary(mButtonSipCallOptions.getEntry());
    }



    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            Intent intent = new Intent();
            intent.setClassName(UP_ACTIVITY_PACKAGE, UP_ACTIVITY_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Finish current Activity and go up to the top level Settings ({@link CallFeaturesSetting}).
     * This is useful for implementing "HomeAsUp" capability for second-level Settings.
     */
    public static void goUpToTopLevelSetting(Activity activity) {
        Intent intent = new Intent(activity, CallFeaturesSetting.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
    
    
    private final int EVENT_SIM_STATE_CHANGED = 1002;
    
    private Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        AsyncResult ar;
	
	        switch(msg.what) {
	            case EVENT_SIM_STATE_CHANGED:
	                Log.d(LOG_TAG, "EVENT_SIM_STATE_CHANGED");
		        	updateUiState();
	                break;    
	            default:
	                Log.w(LOG_TAG, "Unknown Event " + msg.what);
	                break;
	        }
	    }
    };
    
	private void updateUiState(){
 	    Log.d(LOG_TAG, "updateUiState");

		boolean iscard1Enable = TelephonyManager.getDefault().getSimState(0) != TelephonyManager.SIM_STATE_ABSENT;
		boolean iscard2Enable = TelephonyManager.getDefault().getSimState(1) != TelephonyManager.SIM_STATE_ABSENT;
 	    mSimCategory1.setEnabled(iscard1Enable);
 	    mSimCategory2.setEnabled(iscard2Enable);	     
 	}
}
