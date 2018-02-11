/* Copyright (c) 2011-13, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.android.phone;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncResult;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.internal.telephony.MSimConstants;
import com.android.internal.telephony.Phone;
import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.Subscription.SubscriptionStatus;
import com.codeaurora.telephony.msim.SubscriptionManager;
import com.codeaurora.telephony.msim.CardSubscriptionManager;
import com.codeaurora.telephony.msim.SubscriptionData;
import com.codeaurora.telephony.msim.Subscription;

import java.lang.Object;
import java.util.regex.PatternSyntaxException;

import aurora.preference.*;
import aurora.app.*;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.view.KeyEvent;
import static com.android.internal.telephony.MSimConstants.SUBSCRIPTION_KEY;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;

public class SetSubscription extends AuroraPreferenceActivity implements DialogInterface.
        OnDismissListener, DialogInterface.OnClickListener, AuroraPreference.OnPreferenceChangeListener  {
    private static final String TAG = "AuroraSetSubscription";

    private static final String KEY_VOICE = "voice";
    private static final String KEY_DATA = "data";
    private static final String KEY_SMS = "sms";
    private static final String KEY_CONFIG_SUB = "config_sub";

    private static final String CONFIG_SUB = "CONFIG_SUB";
    private static final String TUNE_AWAY = "tune_away";
    private static final String PRIORITY_SUB = "priority_subscription";

    private static final int DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS = 100;

    static final int EVENT_SET_DATA_SUBSCRIPTION_DONE = 1;
    static final int EVENT_SUBSCRIPTION_ACTIVATED = 2;
    static final int EVENT_SUBSCRIPTION_DEACTIVATED = 3;
    static final int EVENT_SET_VOICE_SUBSCRIPTION = 4;
    static final int EVENT_SET_SMS_SUBSCRIPTION = 5;
    static final int EVENT_SET_TUNE_AWAY = 6;
    static final int EVENT_SET_TUNE_AWAY_DONE = 7;
    static final int EVENT_SET_PRIORITY_SUBSCRIPTION = 8;
    static final int EVENT_SET_PRIORITY_SUBSCRIPTION_DONE = 9;
    static final int EVENT_SET_VOICE_SUBSCRIPTION_DONE = 10;
    

    protected boolean mIsForeground = false;
    static final int SUBSCRIPTION_ID_INVALID = -1;
    static final int SUBSCRIPTION_DUAL_STANDBY = 2;

    private AuroraListPreference mVoice;
    private AuroraListPreference mData;
    private AuroraListPreference mSms;
    private AuroraPreferenceScreen mConfigSub;
    private int mNumPhones = MSimTelephonyManager.getDefault().getPhoneCount();
    private CharSequence[] entries; // Used for entries like Subscription1, Subscription2 ...
    private CharSequence[] entryValues; // Used for entryValues like 0, 1 ,2 ...
    private CharSequence[] summaries; // Used for Summaries like Aubscription1, Subscription2....
    private CharSequence[] entriesPrompt; // Used in case of prompt option is required.
    private CharSequence[] entryValuesPrompt; // Used in case of prompt option is required.
    private CharSequence[] summariesPrompt; // Used in case of prompt option is required.

    /* tune away initial/old state */
    private boolean mTuneAwayValue = false;
    /* Priority subscription initial/old state */
    private int mPrioritySubValue = 0;
    /* Default voice subscription initial/old state */
    private int mVoiceSub = 0;
    private Phone mPhone = null;
    private Phone mPhone2;
    
    private AuroraCheckBoxPreference mTuneAway;
    private AuroraListPreference mPrioritySub;

    SubscriptionManager subManager = SubscriptionManager.getInstance();
    AuroraPreference mSim1Operator,mSim2Operator;
    AuroraImagePreferenceScreen mSim1Icon, mSim2Icon;
    AuroraSwitchPreference mSim1Enable, mSim2Enable;
    AuroraSimSpinnerPreference mSimSipnner;
    AuroraDataSpinnerPreference mDataSpinner;
    SharedPreferences mSharedPreferences;
    
 	AuroraPreferenceCategory manageSub1, manageSub2;
    private AuroraSwitchPreference mButtonDataRoam;
    private AuroraSwitchPreference mButtonDataRoam2;
    private AuroraListPreference mButtonPreferredNetworkMode;
    private AuroraPreference mButtonPreferredNetworkMode2, mButtonPreferredNetworkMode4;
    private AuroraPreferenceScreen mButtonAPNExpand1;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand1;
    private AuroraPreferenceScreen mButtonAPNExpand2;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand2;
    
    private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
    private static final String BUTTON_ROAMING_KEY2 = "button_roaming_key2";
    private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE2 = "preferred_network_mode_key2";
    private static final String BUTTON_PREFERED_NETWORK_MODE4 = "preferred_network_mode_key4";
    private static final String BUTTON_APN_EXPAND_KEY1 = "button_apn_key_1";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY1 = "button_carrier_sel_key_1";
    private static final String BUTTON_APN_EXPAND_KEY2 = "button_apn_key_2";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY2 = "button_carrier_sel_key_2";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aurora_multi_sim_settings_v2);
        getAuroraActionBar().setTitle(R.string.callind_multi_sim_card);

        mSharedPreferences = getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
        mVoice = (AuroraListPreference) findPreference(KEY_VOICE);
        mVoice.setOnPreferenceChangeListener(this);
        mData = (AuroraListPreference) findPreference(KEY_DATA);
        mData.setOnPreferenceChangeListener(this);
        mSms = (AuroraListPreference) findPreference(KEY_SMS);
        mSms.setOnPreferenceChangeListener(this);
        mConfigSub = (AuroraPreferenceScreen) findPreference(KEY_CONFIG_SUB);
        mConfigSub.getIntent().putExtra(CONFIG_SUB, true);
        mTuneAway = (AuroraCheckBoxPreference) findPreference(TUNE_AWAY);
        mTuneAway.setOnPreferenceChangeListener(this);
        mPrioritySub = (AuroraListPreference) findPreference(PRIORITY_SUB);
        mPrioritySub.setOnPreferenceChangeListener(this);
    	if(AuroraPhoneUtils.isSimulate()) {
            mPhone = MSimSimulatedPhoneFactory.getPhone(MSimConstants.SUB1);
            mPhone2 = PhoneGlobals.getInstance().getPhone(MSimConstants.SUB2);
    	} else {
            mPhone = PhoneGlobals.getInstance().getPhone(MSimConstants.SUB1);
            mPhone2 = PhoneGlobals.getInstance().getPhone(MSimConstants.SUB2);
    	}

        for (int subId = 0; subId < SubscriptionManager.NUM_SUBSCRIPTIONS; subId++) {
            subManager.registerForSubscriptionActivated(subId,
                    mHandler, EVENT_SUBSCRIPTION_ACTIVATED, null);
            subManager.registerForSubscriptionDeactivated(subId,
                    mHandler, EVENT_SUBSCRIPTION_DEACTIVATED, null);
        }

        // Create and Intialize the strings required for MultiSIM
        // Dynamic creation of entries instead of using static array vlues.
        // entries are Subscription1, Subscription2, Subscription3 ....
        // EntryValues are 0, 1 ,2 ....
        // Summaries are Subscription1, Subscription2, Subscription3 ....
        entries = new CharSequence[mNumPhones];
        entryValues = new CharSequence[mNumPhones];
        summaries = new CharSequence[mNumPhones];
        entriesPrompt = new CharSequence[mNumPhones + 1];
        entryValuesPrompt = new CharSequence[mNumPhones + 1];
        summariesPrompt = new CharSequence[mNumPhones + 1];
        CharSequence[] subString = getResources().getTextArray(R.array.multi_sim_entries);
        int i = 0;
        for (i = 0; i < mNumPhones; i++) {
            entries[i] = subString[i];
            summaries[i] = subString[i];
            summariesPrompt[i] = subString[i];
            entriesPrompt[i] = subString[i];
            entryValues[i] = Integer.toString(i);
            entryValuesPrompt[i] = Integer.toString(i);
        }
        entryValuesPrompt[i] = Integer.toString(i);
        entriesPrompt[i] = getResources().getString(R.string.prompt);
        summariesPrompt[i] = getResources().getString(R.string.prompt_user);
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mAirplaneModeBroadcastReceiver, intentFilter);
        
        
        AuroraPreferenceScreen prefSet = getPreferenceScreen();
        mSim1Operator = (AuroraPreference) findPreference("sim1_operator");
//        mSim1Operator.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB1)),false);
//        mSim1Operator.auroraSetArrowText(MSimTelephonyManager.getDefault().getSimOperatorName(0),false);
        mSim2Operator = (AuroraPreference) findPreference("sim2_operator");
//        mSim2Operator.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB2)),false);
        mSim1Icon = (AuroraImagePreferenceScreen) findPreference("sim1_icon");
        mSim2Icon = (AuroraImagePreferenceScreen) findPreference("sim2_icon");
        mSim1Enable = (AuroraSwitchPreference) findPreference("sim1_enable");
        mSim2Enable = (AuroraSwitchPreference) findPreference("sim2_enable");
        mSim1Enable.setOnPreferenceChangeListener(this);
        mSim2Enable.setOnPreferenceChangeListener(this);
        mSimSipnner = (AuroraSimSpinnerPreference)findPreference("sim_3rd_spinner_key");
        mDataSpinner = (AuroraDataSpinnerPreference)findPreference("data_switch");
        ((AuroraPreference)findPreference("sim_3rd_spinner_key_summary")).setEnabled(false);
        
        mMobileHandler = new MyHandler();
        manageSub1 = (AuroraPreferenceCategory) prefSet.findPreference("sim1_category_key");        
        manageSub2 = (AuroraPreferenceCategory) prefSet.findPreference("sim2_category_key");
        mButtonDataRoam = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_ROAMING_KEY);
        mButtonDataRoam.setOnPreferenceChangeListener(this);
        mButtonDataRoam2 = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_ROAMING_KEY2);
        mButtonDataRoam2.setOnPreferenceChangeListener(this);
        mButtonPreferredNetworkMode = (AuroraListPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE);        
        mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
        mButtonPreferredNetworkMode.setValue(Integer.toString(getPreferredNetworkMode(0)));
        mButtonPreferredNetworkMode2 = (AuroraPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE2);
        mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
        Intent intent = mButtonPreferredNetworkMode2.getIntent();
        intent.putExtra(SUBSCRIPTION_KEY, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mButtonPreferredNetworkMode4 = (AuroraPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE4);
        mButtonPreferredNetworkMode4.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(1)),true);
        intent = mButtonPreferredNetworkMode4.getIntent();
        intent.putExtra(SUBSCRIPTION_KEY, 1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        
        mButtonAPNExpand1 = (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_APN_EXPAND_KEY1);
        mButtonAPNExpand1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mButtonOperatorSelectionExpand1 =
                (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY1);
        mButtonOperatorSelectionExpand1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);    
        mButtonOperatorSelectionExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mButtonOperatorSelectionExpand1.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB1)),false);
        
        mButtonAPNExpand2 = (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_APN_EXPAND_KEY2);
        mButtonAPNExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mButtonOperatorSelectionExpand2 =
                (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY2);
        mButtonOperatorSelectionExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mButtonOperatorSelectionExpand2.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mButtonOperatorSelectionExpand2.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB2)),false);
        Resources res = getResources();
        if (!res.getBoolean(R.bool.config_apn_expand)) {
        	prefSet.removePreference(prefSet.findPreference(BUTTON_APN_EXPAND_KEY1));
        	prefSet.removePreference(prefSet.findPreference(BUTTON_APN_EXPAND_KEY2));
        }
        if (!res.getBoolean(R.bool.config_operator_selection_expand)) {
            if (mButtonOperatorSelectionExpand1 != null) {
            	prefSet.removePreference(mButtonOperatorSelectionExpand1);
                mButtonOperatorSelectionExpand1 = null;
           }
            if (mButtonOperatorSelectionExpand2 != null) {
            	prefSet.removePreference(mButtonOperatorSelectionExpand2);
                mButtonOperatorSelectionExpand2 = null;
           }
        }     

        AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory)findPreference("sim_data_support_key");
	    if(dataPreferenceCategory != null) {
	    	prefSet.removePreference(dataPreferenceCategory);
        }  
        
        AuroraPreference otherPreferenceCategory = findPreference("multi_sim_settings");
        if(otherPreferenceCategory != null) {
            prefSet.removePreference(otherPreferenceCategory);
        }
        
        AuroraPreferenceCategory thridPreferenceCategory = (AuroraPreferenceCategory)findPreference("sim_3rd_support_key");
	    if(thridPreferenceCategory != null) {
	    	prefSet.removePreference(thridPreferenceCategory);
        }          
        
        
        mSubscriptionManager = SubscriptionManager.getInstance();
        mCardSubscriptionManager = CardSubscriptionManager.getInstance();
//        if (newCardNotify) {
//            Log.d(TAG, "onCreate: Notify new cards are available!!!!");
//            notifyNewCardAvailable();
//        } else {
            // get the card subscription info from the Proxy Manager.
            mCardSubscrInfo = new SubscriptionData[MAX_SUBSCRIPTIONS];
            for (int j = 0; j < MAX_SUBSCRIPTIONS; j++) {
                mCardSubscrInfo[j] = mCardSubscriptionManager.getCardSubscriptions(j);
            }

            // To store the selected subscriptions
            // index 0 for sub0 and index 1 for sub1
//            subArray = new AuroraCheckBoxPreference[MAX_SUBSCRIPTIONS];
            mIsSimEnable = new boolean[MAX_SUBSCRIPTIONS];

            if(mCardSubscrInfo != null) {
//                populateList();

                mUserSelSub = new SubscriptionData(MAX_SUBSCRIPTIONS);

                updateCheckBoxes();
            } else {
                Log.d(TAG, "onCreate: Card info not available: mCardSubscrInfo == NULL");
            }
            
//            mSim1Enable.setChecked(mIsSimEnable[0]);
//            mSim2Enable.setChecked(mIsSimEnable[1]);

//            mCardSubscriptionManager.registerForSimStateChanged(mHandler,
//                    EVENT_SIM_STATE_CHANGED, null);
            
            
            if (mSubscriptionManager.isSetSubscriptionInProgress()) {
                Log.d(TAG, "onCreate: SetSubscription is in progress when started this activity");
//                showDialog(DIALOG_SET_SUBSCRIPTION_IN_PROGRESS);
                showSetSubProgressDialog();
                mSubscriptionManager.registerForSetSubscriptionCompleted(
                        mHandler, EVENT_SET_SUBSCRIPTION_DONE, null);
            }
//        }
            
//            boolean newCardNotify2 = getIntent().getBooleanExtra("NOTIFY_NEW_CARD_AVAILABLE2", false);
//            if (newCardNotify2) {
//                Log.d(TAG, "onCreate: Notify new cards are available2!!!!");
//                setNewCardSubscription();
//                notifyNewCardAvailable();
//            } 
            
            boolean newCardNotify = getIntent().getBooleanExtra("NOTIFY_NEW_CARD_AVAILABLE", false);
            if (newCardNotify) {
                Log.d(TAG, "onCreate: Notify new cards are available!!!!");
//                getNewCardState();
//                setNewCardSubscription();
                mHandler.removeMessages(EVENT_SET_SUBSCRIPTION_DONE);
                finish();
            } 
            
      	   //aurora add liguangyu 20140819 for BUG #7694 start
//            mIsNew = getIntent().getBooleanExtra("new", false);
//            if(mIsNew) {
                IntentFilter homeIf =new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                registerReceiver(mHomeRecevier, homeIf);
//            }
      	   //aurora add liguangyu 20140819 for BUG #7694 end
            
            
            if (!DeviceUtils.isIUNI()) {
//              mButtonPreferredNetworkMode.setEntries(R.array.cm_preferred_network_mode_choices);
//              mButtonPreferredNetworkMode.setEntryValues(R.array.cm_preferred_network_mode_values); 
    	        if(mButtonPreferredNetworkMode2 != null) {
    	        	manageSub1.removePreference(mButtonPreferredNetworkMode2);
    	        }
    	  	} else {
    	          if(mButtonPreferredNetworkMode != null) {
    	        	  manageSub1.removePreference(mButtonPreferredNetworkMode);
    	         }	
    	  	}
            
            
       	 if (!DeviceUtils.isSupportDualData()) {
		        if (mButtonPreferredNetworkMode4 != null) {
		        	manageSub2.removePreference(mButtonPreferredNetworkMode4);
		       }
		        
		        if(mButtonAPNExpand2 != null) {
		        	manageSub2.removePreference(mButtonAPNExpand2);
		        }
		        
		        if(mButtonDataRoam2 != null) {
		        	manageSub2.removePreference(mButtonDataRoam2);
		        }
		        
		 }
           
       	 if(mSim1Operator != null) {
       		 manageSub1.removePreference(mSim1Operator);
       	 }
       	 
     	 if(mSim2Operator != null) {
       		 manageSub2.removePreference(mSim2Operator);
       	 }
     	 
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAirplaneModeOn()) {
            Log.d(TAG, "Airplane mode is ON, grayout the config subscription menu!!!");
            mConfigSub.setEnabled(false);
        } else {
            mConfigSub.setEnabled(true);
        }
        updateMultiSimEntriesForVoice();
        updateMultiSimEntriesForData();
        updateMultiSimEntriesForSms();
        mIsForeground = true;
        updateState();
        updateTuneAwayState();
        updatePrioritySubState();
        updateUiState();
    }

    /**
     ** Receiver for Airplane mode changed intent broadcasts.
     **/
    private AirplaneModeBroadcastReceiver mAirplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver();
    private class AirplaneModeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                Log.d(TAG, "Airplane mode is: " + isAirplaneModeOn());
                if (isAirplaneModeOn()) {
                    Log.d(TAG, "Airplane mode is ON, grayout the config subscription menu!!!");
                    mConfigSub.setEnabled(false);
                } else {
                    mConfigSub.setEnabled(true);
                }
            }
        }
    }

    protected void updateMultiSimEntriesForData() {
        mData.setEntries(entries);
        mData.setEntryValues(entryValues);
    }

    protected void updateMultiSimEntriesForSms() {
        int count = subManager.getActiveSubscriptionsCount();
        if (count >= SUBSCRIPTION_DUAL_STANDBY) {
            mSms.setEntries(entriesPrompt);
            mSms.setEntryValues(entryValuesPrompt);
        } else  {
            mSms.setEntries(entries);
            mSms.setEntryValues(entryValues);
        }
    }

    protected void updateMultiSimEntriesForVoice() {
        int count = subManager.getActiveSubscriptionsCount();
        if (count >= SUBSCRIPTION_DUAL_STANDBY) {
            mVoice.setEntries(entriesPrompt);
            mVoice.setEntryValues(entryValuesPrompt);
        } else  {
            mVoice.setEntries(entries);
            mVoice.setEntryValues(entryValues);
        }
    }

    private void updateTuneAwayState() {
        boolean tuneAwayStatus = (Settings.Global.getInt(getContentResolver(),
                Settings.Global.TUNE_AWAY_STATUS,  0) == 1);
        int resId = tuneAwayStatus ? R.string.tune_away_enabled : R.string.tune_away_disabled;

        mTuneAway.setChecked(tuneAwayStatus);
        mTuneAway.setSummary(getResources().getString(resId));
    }

    private void updatePrioritySubState() {
        mPrioritySub.setEntries(entries);
        mPrioritySub.setEntryValues(entryValues);

        try {
            int priorityValue = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.MULTI_SIM_PRIORITY_SUBSCRIPTION);
            mPrioritySub.setValue(Integer.toString(priorityValue));
            mPrioritySub.auroraSetArrowText(summaries[priorityValue]);
//            mPrioritySub.setSummary(summaries[priorityValue]);
            mPrioritySubValue = priorityValue;

        } catch (SettingNotFoundException snfe) {
            Log.e(TAG, "Settings Exception Reading Dual Sim Priority Subscription Values");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsForeground = false;
    }

    private boolean isAirplaneModeOn() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void updateState() {
        Log.d(TAG, "updateState");
        updateVoiceSummary();
        updateDataSummary();
        updateSmsSummary();
    	mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
    	mButtonDataRoam2.setChecked(mPhone2.getDataRoamingEnabled());
        mPhone.getPreferredNetworkType(mMobileHandler.obtainMessage(
                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 0, -1));
        if(mButtonPreferredNetworkMode4 != null) {
	        mPhone2.getPreferredNetworkType(mMobileHandler.obtainMessage(
	                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 1, -1));
        }
        updateOperatorSelectionVisibility();
    	updateDataPreference();    	    	    	
    }

    private void updateVoiceSummary() {

        int voiceSub = MSimPhoneFactory.getVoiceSubscription();
        boolean promptEnabled  = MSimPhoneFactory.isPromptEnabled();
        int count = subManager.getActiveSubscriptionsCount();

        Log.d(TAG, "updateVoiceSummary: voiceSub =  " + voiceSub
                + " promptEnabled = " + promptEnabled
                + " number of active SUBs = " + count);

        if (promptEnabled && count >= SUBSCRIPTION_DUAL_STANDBY) {

            Log.d(TAG, "prompt is enabled: setting value to : " + mNumPhones);
            mVoice.setValue(Integer.toString(mNumPhones));
            mVoice.setSummary(summariesPrompt[mNumPhones]);
        } else {
            String sub = Integer.toString(voiceSub);
            Log.d(TAG, "setting value to : " + sub);
            mVoice.setValue(sub);
            mVoice.setSummary(summaries[voiceSub]);
        }
    }

    private void updateDataSummary() {
        int dataSub = MSimPhoneFactory.getDataSubscription();

        Log.d(TAG, "updateDataSummary: Data Subscription : = " + dataSub);
        mData.setValue(Integer.toString(dataSub));
//        mData.setSummary(summaries[dataSub]);
        mData.auroraSetArrowText(summaries[dataSub]);
    }

    private void updateSmsSummary() {
        int smsSub = MSimPhoneFactory.getSMSSubscription();
        boolean promptEnabled  = MSimPhoneFactory.isSMSPromptEnabled();
        int count = subManager.getActiveSubscriptionsCount();

        Log.d(TAG, "updateSmsSummary: SmsSub =  " + smsSub
                + " promptEnabled = " + promptEnabled
                + " number of active SUBs = " + count);

        if (promptEnabled && count >= SUBSCRIPTION_DUAL_STANDBY) {
            Log.d(TAG, "prompt is enabled: setting value to : " + mNumPhones);
            mSms.setValue(Integer.toString(mNumPhones));
            mSms.setSummary(summariesPrompt[mNumPhones]);
        } else {
            String sub = Integer.toString(smsSub);
            Log.d(TAG, "setting value to : " + sub);
            mSms.setValue(sub);
            mSms.setSummary(summaries[smsSub]);
        }
    }

    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        final String key = preference.getKey();
        String status;
        Log.d(TAG, "onPreferenceChange:::: " );

        if (KEY_VOICE.equals(key)) {

            mVoiceSub = Integer.parseInt((String) objValue);
            if (mVoiceSub == mNumPhones) { //mNumPhones is the maximum index of the UI options.
                                         //This will be the Prompt option.
                MSimPhoneFactory.setPromptEnabled(true);
                mVoice.setSummary(summariesPrompt[mVoiceSub]);
                Log.d(TAG, "prompt is enabled " + mVoiceSub);
            } else if (subManager.getCurrentSubscription(mVoiceSub).subStatus
                    == SubscriptionStatus.SUB_ACTIVATED) {
                Log.d(TAG, "setVoiceSubscription " + mVoiceSub);
                MSimPhoneFactory.setPromptEnabled(false);
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_VOICE_SUBSCRIPTION,
                        mVoiceSub));
            } else {
                status = getResources().getString(R.string.set_voice_error);
                displayAlertDialog(status);
            }
        }

        if (KEY_DATA.equals(key)) {
            int dataSub = Integer.parseInt((String) objValue);
            Log.d(TAG, "setDataSubscription " + dataSub);
            if (mIsForeground) {
                showDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
            }
            SubscriptionManager mSubscriptionManager = SubscriptionManager.getInstance();
            Message setDdsMsg = Message.obtain(mHandler, EVENT_SET_DATA_SUBSCRIPTION_DONE, null);
            mSubscriptionManager.setDataSubscription(dataSub, setDdsMsg);
        }

        if (KEY_SMS.equals(key)) {
            int smsSub = Integer.parseInt((String) objValue);
            if (smsSub == mNumPhones) { //mNumPhones is the maximum index of the UI options.
                                         //This will be the Prompt option.
                MSimPhoneFactory.setSMSPromptEnabled(true);
                mSms.setSummary(summariesPrompt[smsSub]);
                Log.d(TAG, "prompt is enabled " + smsSub);
            } else if (subManager.getCurrentSubscription(smsSub).subStatus
                   == SubscriptionStatus.SUB_ACTIVATED) {
                Log.d(TAG, "setSMSSubscription " + smsSub);
                MSimPhoneFactory.setSMSPromptEnabled(false);
                MSimPhoneFactory.setSMSSubscription(smsSub);
                mSms.setSummary(summaries[smsSub]);
            } else {
                status = getResources().getString(R.string.set_sms_error);
                displayAlertDialog(status);
            }
            mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_SMS_SUBSCRIPTION));
        }

        if (TUNE_AWAY.equals(key)) {
            mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_TUNE_AWAY));
        }

        if (PRIORITY_SUB.equals(key)) {
            int prioritySubIndex = Integer.parseInt((String) objValue);
            Log.d(TAG, "onPreferenceChange::::  prioritySubIndex =" + prioritySubIndex );
            if (subManager.getCurrentSubscription(prioritySubIndex).subStatus
                    == SubscriptionStatus.SUB_ACTIVATED) {
                mPrioritySubValue = prioritySubIndex;
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_PRIORITY_SUBSCRIPTION,
                        prioritySubIndex, 0));                
                
                //voice
                MSimPhoneFactory.setPromptEnabled(false);
                mVoiceSub = prioritySubIndex;
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_VOICE_SUBSCRIPTION,
                		prioritySubIndex, 0));
                
                //sms
                MSimPhoneFactory.setSMSSubscription(prioritySubIndex);
                
            } else {
                status = getResources().getString(R.string.set_priority_sub_error);
                displayAlertDialog(status);
            }
        }
        
//        if(preference == mSim1Enable || preference == mSim2Enable) {
//            setSubscription();
//        }
        
        if (preference == mButtonDataRoam) {
            boolean isChecked = (Boolean)objValue;
            handleRoamSwitch(0, isChecked);
        } else if (preference == mButtonDataRoam2) {
            boolean isChecked = (Boolean)objValue;
            handleRoamSwitch(1, isChecked);
        } else if(preference == mButtonPreferredNetworkMode){

            mButtonPreferredNetworkMode.setValue((String) objValue);
            int buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
            int settingsNetworkMode = getPreferredNetworkMode(0);
            if (buttonNetworkMode != settingsNetworkMode) {
                int modemNetworkMode;
                if(buttonNetworkMode >= 0 && buttonNetworkMode <=22) {
                	  modemNetworkMode = buttonNetworkMode;
                } else {
                    log("Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
                    return true;
                }
                UpdatePreferredNetworkModeSummary(buttonNetworkMode);

                setPreferredNetworkMode(modemNetworkMode , 0);
                mPhone.setPreferredNetworkType(modemNetworkMode, mMobileHandler
                        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
            }
        
        } else if(preference == mSim1Enable) {
            boolean isChecked = (Boolean)objValue;
            mSim1Enable.setEnabled(false);
            handleSimSwitch(0, isChecked);
        } else if(preference == mSim2Enable) {
            boolean isChecked = (Boolean)objValue;
            mSim2Enable.setEnabled(false);
            handleSimSwitch(1, isChecked);
        }
        
        return true;
    } 


    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS) {
            AuroraProgressDialog dialog = new AuroraProgressDialog(this);

            dialog.setMessage(getResources().getString(R.string.set_data_subscription_progress));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);

            return dialog;
        } else if (id == DIALOG_SET_SUBSCRIPTION_IN_PROGRESS) {
        	AuroraProgressDialog dialog = new AuroraProgressDialog(this);

            dialog.setMessage(getResources().getString(R.string.set_uicc_subscription_progress));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);

            return dialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS) {
            // when the dialogs come up, we'll need to indicate that
            // we're in a busy state to disallow further input.
            getPreferenceScreen().setEnabled(false);
        } else if (id == DIALOG_SET_SUBSCRIPTION_IN_PROGRESS) {
            // when the dialogs come up, we'll need to indicate that
            // we're in a busy state to disallow further input.
            getPreferenceScreen().setEnabled(false);
        }
    }


    void displayAlertDialog(String msg) {
        if (!mIsForeground) {
            Log.d(TAG, "The activitiy is not in foreground. Do not display dialog!!!");
            return;
        }
        Log.d(TAG, "displayErrorDialog!" + msg);
        new AuroraAlertDialog.Builder(this).setMessage(msg)
               .setTitle(android.R.string.dialog_alert_title)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .setPositiveButton(android.R.string.yes, this)
               .show()
               .setOnDismissListener(this);
        }

        private void updateTuneAwayStatus() {
            boolean tuneAwayValue = mTuneAway.isChecked();
            mTuneAwayValue = tuneAwayValue;
            Log.d(TAG," updateTuneAwayStatus change tuneAwayValue to: " + tuneAwayValue);
            Message setTuneAwayMsg = Message.obtain(mHandler, EVENT_SET_TUNE_AWAY_DONE, null);
            mPhone.setTuneAway(tuneAwayValue, setTuneAwayMsg);
        }

        private void updatePrioritySub(int priorityIndex) {
            Log.d(TAG, "updatePrioritySub change priority sub to: " + priorityIndex);
            Message setPrioritySubMsg = Message.obtain(mHandler,
                    EVENT_SET_PRIORITY_SUBSCRIPTION_DONE, null);
            mPhone.setPrioritySub(priorityIndex, setPrioritySubMsg);
        }

        private void updateVoiceSub(int subIndex) {
            Log.d(TAG, "updateVoiceSub change voice sub to: " + subIndex);
            Message setVoiceSubMsg = Message.obtain(mHandler,
                    EVENT_SET_VOICE_SUBSCRIPTION_DONE, null);
            mPhone.setDefaultVoiceSub(subIndex, setVoiceSubMsg);
        }

        private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;

            switch(msg.what) {
                case EVENT_SET_DATA_SUBSCRIPTION_DONE:
                    Log.d(TAG, "EVENT_SET_DATA_SUBSCRIPTION_DONE");
                    if (mIsForeground) {
                        dismissDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
                    }
                    getPreferenceScreen().setEnabled(true);
                    updateDataSummary();

                    ar = (AsyncResult) msg.obj;
                    String status;
                    if (ar.exception != null) {
                        status = getResources().getString(R.string.set_dds_error)
                                           + " " + ar.exception.getMessage();
                        displayAlertDialog(status);
                        break;
                    }

                    boolean result = (Boolean)ar.result;

                    Log.d(TAG, "SET_DATA_SUBSCRIPTION_DONE: result = " + result);
                    if (result == true) {
                        status = getResources().getString(R.string.set_dds_success);
                        Toast toast = Toast.makeText(getApplicationContext(), status,
                                Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        status = getResources().getString(R.string.set_dds_failed);
                        displayAlertDialog(status);
                    }

                    break;
                case EVENT_SUBSCRIPTION_ACTIVATED:
                case EVENT_SUBSCRIPTION_DEACTIVATED:
                    updateMultiSimEntriesForVoice();
                    updateMultiSimEntriesForSms();
                    break;

                case EVENT_SET_VOICE_SUBSCRIPTION:
                    updateVoiceSub(msg.arg1);
                    break;
                case EVENT_SET_VOICE_SUBSCRIPTION_DONE:
                    Log.d(TAG, "EVENT_SET_VOICE_SUBSCRIPTION_DONE");
                    ar = (AsyncResult) msg.obj;
                    String sub;
                    if (ar.exception != null) {
                        Log.e(TAG, "SET_VOICE_SUBSCRIPTION_DONE: returned Exception: "
                                + ar.exception);
                        int voiceSub = MSimPhoneFactory.getVoiceSubscription();
                        sub = Integer.toString(voiceSub);
                        mVoice.setValue(sub);
                        mVoice.setSummary(summaries[voiceSub]);
                        mVoiceSub = voiceSub;
                        break;
                    }
                    sub = Integer.toString(mVoiceSub);
                    mVoice.setValue(sub);
                    mVoice.setSummary(summaries[mVoiceSub]);
                    MSimPhoneFactory.setVoiceSubscription(mVoiceSub);
                    break;
                case EVENT_SET_SMS_SUBSCRIPTION:
                    updateSmsSummary();
                    break;
                case EVENT_SET_TUNE_AWAY:
                    updateTuneAwayStatus();
                    break;
                case EVENT_SET_TUNE_AWAY_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Log.e(TAG, "SET_TUNE_AWAY_DONE: returned Exception: " + ar.exception);
                        updateTuneAwayState();
                        break;
                    }
                    Log.d(TAG, "SET_TUNE_AWAY_DONE: mTuneAwayValue = " + mTuneAwayValue);
                    mTuneAway.setChecked(mTuneAwayValue);
                    int taResId = mTuneAwayValue ? R.string.tune_away_enabled : R.string.tune_away_disabled;
//                    mTuneAway.setSummary(mTuneAwayValue ? "Enable" : "Disable");
                    mTuneAway.setSummary(getResources().getString(taResId));
                    MSimPhoneFactory.setTuneAway(mTuneAwayValue);
                    break;
                case EVENT_SET_PRIORITY_SUBSCRIPTION:
                    updatePrioritySub(msg.arg1);
                    break;
                case EVENT_SET_PRIORITY_SUBSCRIPTION_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Log.e(TAG, "EVENT_SET_PRIORITY_SUBSCRIPTION_DONE: returned Exception: "
                                + ar.exception);
                        updatePrioritySubState();
                        break;
                    }
                    Log.d(TAG, "EVENT_SET_PRIORITY_SUBSCRIPTION_DONE : mPrioritySubValue "
                            + mPrioritySubValue);
                    mPrioritySub.setValue(Integer.toString(mPrioritySubValue));
//                    mPrioritySub.setSummary(summaries[mPrioritySubValue]);
                    mPrioritySub.auroraSetArrowText(summaries[mPrioritySubValue]);
                    MSimPhoneFactory.setPrioritySubscription(mPrioritySubValue);
                    break;
                case EVENT_SET_SUBSCRIPTION_DONE:
                    Log.d(TAG, "EVENT_SET_SUBSCRIPTION_DONE");
                    mSubscriptionManager.unRegisterForSetSubscriptionCompleted(mHandler);
//                    dismissDialogSafely(DIALOG_SET_SUBSCRIPTION_IN_PROGRESS);
     	    		dismissAuroraDialogSafely();

                    getPreferenceScreen().setEnabled(true);
                    ar = (AsyncResult) msg.obj;

                    String auroraResult[] = (String[]) ar.result;

                    if (auroraResult != null) {
//                        displayAlertDialog(auroraResult);
                      Toast toast = Toast.makeText(getApplicationContext(), R.string.set_sub_success, Toast.LENGTH_SHORT);
		              toast.show();
		              if(mIsNewCardPending) {
                          setNewCardSubscription();
                      	mIsNewCardPending = false;
                      } else {
                          updateUiState();	
                      }
                    	
                    } else {
//                        finish();
                          Toast toast = Toast.makeText(getApplicationContext(), R.string.set_sub_failed, Toast.LENGTH_SHORT);
  		                  toast.show();
                          updateUiState();	
                    }
                    break; 
                case EVENT_SIM_STATE_CHANGED:
                    Log.d(TAG, "EVENT_SIM_STATE_CHANGED");
//                    AuroraPreferenceScreen prefParent = (AuroraPreferenceScreen) getPreferenceScreen()
//                                             .findPreference(PREF_PARENT_KEY);
//
//                    for (int i = 0; i < mCardSubscrInfo.length; i++) {
//                    	AuroraPreferenceCategory subGroup = (AuroraPreferenceCategory) prefParent
//                                 .findPreference("sub_group_" + i);
//                        if (subGroup != null) {
//                            subGroup.removeAll();
//                        }
//                    }
//                    prefParent.removeAll();
//                    populateList();
//                    updateCheckBoxes();
             	    mHandler.postDelayed(new Runnable(){
		     	    	public void run(){
				        	updateUiState();
		     	    	}
		     	    }, 1500);//aurora change zhouxiaobing 20140514 for bug phone gua diao;
                    break;    
                default:
                        Log.w(TAG, "Unknown Event " + msg.what);
                        break;
            }
        }
    };
    
//    中国移动的460+  00 、02 、07
//    中国联通的460+01、10
//    中国电信的460+03.
    private String getTitleFromOperatorNumber(String number){
        Log.w(TAG, "getTitleFromOperatorNumber =" + number);
    	int resId = R.string.unknown;
    	if(!TextUtils.isEmpty(number)) { 
	    	if(number.equalsIgnoreCase("46000")
	    			||number.equalsIgnoreCase("46002")
	    			||number.equalsIgnoreCase("46007")) {
	    		resId = R.string.operator_china_mobile;
	    		  Log.w(TAG, "getTitleFromOperatorNumber2 =" + getResources().getString(resId));
	    	} else if(number.equalsIgnoreCase("46001")
	    			||number.equalsIgnoreCase("46010")) {
	    		resId = R.string.operator_china_unicom;
	    	} else if(number.equalsIgnoreCase("46003")) {
	    		resId = R.string.operator_china_telecom;
	    	} 
    	} else {
    		return "";
    	}
    	return getResources().getString(resId);
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference == mSim1Icon || preference == mSim2Icon) {
        	Intent intent = new Intent();
        	intent.setClass(this, MSimIconSeclectActivity.class);
        	intent.putExtra(MSimConstants.SUBSCRIPTION_KEY, preference == mSim1Icon ? 0 : 1);
        	startActivityForResult(intent, 0);
        } 
        return false;
    }  
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
    	   case RESULT_OK:
    	        int slot = data.getIntExtra(MSimConstants.SUBSCRIPTION_KEY, PhoneGlobals.getInstance().getDefaultSubscription());
    	        int resId = data.getIntExtra("sim_icon_res", SimIconUtils.getSimIcon(slot));
    	        Log.w(TAG, "onActivityResult slot = " + slot + " resId=" + resId );
    	        if(slot == 0) {
    	        	mSim1Icon.setSimIcon(resId);    	  
    	        } else if(slot == 1) {
    	        	mSim2Icon.setSimIcon(resId);
    	        }
    	        SimIconUtils.setColorForSIM(SimIconUtils.resId2color(resId), slot);    	   
    	    break;
    default:
    	    break;
    	  }
   }
       
    public static final int SUBSCRIPTION_INDEX_INVALID = 99999;

    private boolean subErr = false;
    private SubscriptionData[] mCardSubscrInfo;
    private SubscriptionData mCurrentSelSub;
    private SubscriptionData mUserSelSub;
    private SubscriptionManager mSubscriptionManager;
    private CardSubscriptionManager mCardSubscriptionManager;
    //mIsForeground is added to track if activity is in foreground

    //String keys for preference lookup

    private final int MAX_SUBSCRIPTIONS = SubscriptionManager.NUM_SUBSCRIPTIONS;

    private final int EVENT_SET_SUBSCRIPTION_DONE = 1001;
    
    private static final int EVENT_SET_NEW_SUBSCRIPTION_DONE = 1003;
    private static final int EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT = 1004;
    
    private final int EVENT_SIM_STATE_CHANGED = 1002;

    private final int DIALOG_SET_SUBSCRIPTION_IN_PROGRESS = 101;    
    private boolean[] mIsSimEnable;
    private boolean[] mAuroraSwitchState = new boolean[MAX_SUBSCRIPTIONS];
    
    protected void onDestroy () {
        super.onDestroy();
        unregisterReceiver(mAirplaneModeBroadcastReceiver);
//        mCardSubscriptionManager.unRegisterForSimStateChanged(mHandler);
        mSubscriptionManager.unRegisterForSetSubscriptionCompleted(mHandler);
        dismissAuroraDialogSafely();
  	    //aurora add liguangyu 20140819 for BUG #7694 start
//        if(mIsNew) {
            unregisterReceiver(mHomeRecevier);
//        }
  	    //aurora add liguangyu 20140819 for BUG #7694 end
        if(mErrorDialog != null) {
        	mErrorDialog.dismiss();
        	mErrorDialog = null;
        }
    }
    
    
//    private void notifyNewCardAvailable() {
//        Log.d(TAG, "notifyNewCardAvailable()");
//
//        new AuroraAlertDialog.Builder(this).setMessage(R.string.new_sim_insert)
//            .setTitle(R.string.config_sub_title)
//            .setIcon(android.R.drawable.ic_dialog_alert)
//            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        Log.d(TAG, "new card dialog box:  onClick");
//                        //finish();
//                    }
//                })      
//            .show()
//            .setNegativeButton(android.R.string.no, new DialogInterface.OnDismissListener() {
//                    public void onDismiss(DialogInterface dialog) {
//                        Log.d(TAG, "new card dialog box:  onDismiss");
////                        finish();
//                    }
//                });
//        
//  
//    }
    
    //原有的逻辑是更新所有卡的所有卡的所有模的列表，如双模卡就会有两个checkbox可以点击，一般来说一卡模，IUNI的只操作第一个模，双模卡的情况暂时不考虑
    private void updateCheckBoxes() {

        mCurrentSelSub = new SubscriptionData(MAX_SUBSCRIPTIONS);
        for (int i = 0; i < MAX_SUBSCRIPTIONS; i++) {
            Subscription sub = mSubscriptionManager.getCurrentSubscription(i);
                    mCurrentSelSub.subscription[i].copyFrom(sub);
        }

        if (mCurrentSelSub != null) {
            for (int i = 0; i < MAX_SUBSCRIPTIONS; i++) {
                Log.d(TAG, "updateCheckBoxes: mCurrentSelSub.subscription[" + i + "] = "
                           + mCurrentSelSub.subscription[i]);
                if (mCurrentSelSub.subscription[i].subStatus ==
                        Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                     mIsSimEnable[i] = true;                    
                } else {
                	 mIsSimEnable[i] = false;    
                }
            }
            mUserSelSub.copyFrom(mCurrentSelSub);
        }
        SharedPreferences.Editor editor =  mSharedPreferences.edit();
		SIMInfo sim1 = SIMInfo.getSIMInfoBySlot(this, 0);
        if(sim1 != null) {
    		editor.putBoolean(String.valueOf(sim1.mSimId), mIsSimEnable[0]);
        } 
        SIMInfo sim2 = SIMInfo.getSIMInfoBySlot(this, 1);
        if(sim2 != null) {
    		editor.putBoolean(String.valueOf(sim2.mSimId), mIsSimEnable[1]);
        } 
		editor.commit();
        Log.d(TAG, "updateCheckBoxes sim1 = " + mIsSimEnable[0] + " sim2 = " + mIsSimEnable[1]);       
    }

    
//    AuroraPreference.OnPreferenceClickListener mCheckBoxListener =
//            new AuroraPreference.OnPreferenceClickListener() {
//        public boolean onPreferenceClick(AuroraPreference preference) {
//        	AuroraCheckBoxPreference subPref = (AuroraCheckBoxPreference)preference;
//            String key = subPref.getKey();
//            Log.d(TAG, "setSubscription: key = " + key);
//            String splitKey[] = key.split(" ");
//            String sSlotId = splitKey[0].substring(splitKey[0].indexOf("slot") + 4);
//            int slotIndex = Integer.parseInt(sSlotId);
//
//            if (subPref.isChecked()) {
//                if (subArray[slotIndex] != null) {
//                    subArray[slotIndex].setChecked(false);
//                }
//                subArray[slotIndex] = subPref;
//            } else {
//                subArray[slotIndex] = null;
//            }
//            return true;
//        }
//    };
    
    
    private void setSubscription() {
        
        Log.d(TAG, "setSubscription sim1 = " + mAuroraSwitchState[0] + " sim2 = " + mAuroraSwitchState[1]);
        
        int numSubSelected = 0;
        int deactRequiredCount = 0;
        subErr = false;

        for (int i = 0; i < mAuroraSwitchState.length; i++) {
            if (mAuroraSwitchState[i]) {
                numSubSelected++;
            }
        }

        Log.d(TAG, "setSubscription: numSubSelected = " + numSubSelected);

//        if (numSubSelected == 0) {
//            // Show a message to prompt the user to select atleast one.
//            Toast toast = Toast.makeText(getApplicationContext(),
//                    R.string.set_subscription_error_atleast_one,
//                    Toast.LENGTH_SHORT);
//            toast.show();
//            updateUiState();
//        } else if (isPhoneInCall()) {
        if (isPhoneInCall()) {
            // User is not allowed to activate or deactivate the subscriptions
            // while in a voice call.
            displayErrorDialog(R.string.set_sub_not_supported_phone_in_call);
        } else {
            for (int i = 0; i < MAX_SUBSCRIPTIONS; i++) {
                if (mAuroraSwitchState[i] == false) {
                    if (mCurrentSelSub.subscription[i].subStatus ==
                            Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                        Log.d(TAG, "setSubscription: Sub " + i + " not selected. Setting 99999");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        deactRequiredCount++;
                    }
                } else {
                    // Key is the string :  "slot<SlotId> index<IndexId>"
                    // Split the string into two and get the SlotId and IndexId.
//                    String key = subArray[i].getKey();
//                    Log.d(TAG, "setSubscription: key = " + key);
//                    String splitKey[] = key.split(" ");
//                    String sSlotId = splitKey[0].substring(splitKey[0].indexOf("slot") + 4);
//                    int slotId = Integer.parseInt(sSlotId);
//                    String sIndexId = splitKey[1].substring(splitKey[1].indexOf("index") + 5);
//                    int subIndex = Integer.parseInt(sIndexId);
                    int slotId = i;
                    int subIndex = 0;

                    if (mCardSubscrInfo[slotId] == null) {
                        Log.d(TAG, "setSubscription: mCardSubscrInfo is not in sync "
                                + "with SubscriptionManager");
                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
                        mUserSelSub.subscription[i].subId = i;
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_DEACTIVATE;

                        if (mCurrentSelSub.subscription[i].subStatus ==
                                Subscription.SubscriptionStatus.SUB_ACTIVATED) {
                            deactRequiredCount++;
                        }
                        continue;
                    }


                    // Compate the user selected subscriptio with the current subscriptions.
                    // If they are not matching, mark it to activate.
                    mUserSelSub.subscription[i].copyFrom(mCardSubscrInfo[slotId].
                            subscription[subIndex]);
                    mUserSelSub.subscription[i].subId = i;
                    if (mCurrentSelSub != null) {
                        // subStatus used to store the activation status as the mCardSubscrInfo
                        // is not keeping track of the activation status.
                        Subscription.SubscriptionStatus subStatus =
                                mCurrentSelSub.subscription[i].subStatus;
                        mUserSelSub.subscription[i].subStatus = subStatus;
                        if ((subStatus != Subscription.SubscriptionStatus.SUB_ACTIVATED) ||
                            (!mUserSelSub.subscription[i].equals(mCurrentSelSub.subscription[i]))) {
                            // User selected a new subscription.  Need to activate this.
                            mUserSelSub.subscription[i].subStatus = Subscription.
                            SubscriptionStatus.SUB_ACTIVATE;
                        }

                        if (mCurrentSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATED
                                 && mUserSelSub.subscription[i].subStatus == Subscription.
                                 SubscriptionStatus.SUB_ACTIVATE) {
                            deactRequiredCount++;
                        }
                    } else {
                        mUserSelSub.subscription[i].subStatus = Subscription.
                                SubscriptionStatus.SUB_ACTIVATE;
                    }
                }
            }

//            if (deactRequiredCount >= MAX_SUBSCRIPTIONS) {
//                displayErrorDialog(R.string.deact_all_sub_not_supported);
//            } else {
                boolean ret = mSubscriptionManager.setSubscription(mUserSelSub);                
                if (ret) {
                    if(mIsForeground){
//                       showDialog(DIALOG_SET_SUBSCRIPTION_IN_PROGRESS);
                    	showSetSubProgressDialog();
                    }
                    mSubscriptionManager.registerForSetSubscriptionCompleted(mHandler,
                            EVENT_SET_SUBSCRIPTION_DONE, null);
                } else {
                    //TODO: Already some set sub in progress. Display a Toast?
                }
//            }
        }
    }

    private boolean isPhoneInCall() {
        boolean phoneInCall = false;
        for (int i = 0; i < MAX_SUBSCRIPTIONS; i++) {
            if (MSimTelephonyManager.getDefault().getCallState(i)
                    != TelephonyManager.CALL_STATE_IDLE) {
                phoneInCall = true;
                break;
            }
        }
        return phoneInCall;
    }
    
    private AuroraAlertDialog mErrorDialog = null;
    /**
     *  Displays an dialog box with error message.
     *  "Deactivation of both subscription is not supported"
     */
    private void displayErrorDialog(int messageId) {
        Log.d(TAG, "errorMutipleDeactivate(): " + getResources().getString(messageId));
        
        if (mErrorDialog != null) {
        	mErrorDialog.dismiss();
        	mErrorDialog = null;
        }

        mErrorDialog = new AuroraAlertDialog.Builder(this)
            .setTitle(R.string.config_sub_title)
            .setMessage(messageId)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "errorMutipleDeactivate:  onClick");
//                        updateCheckBoxes();
                        updateUiState();
                    }
                })
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(TAG, "errorMutipleDeactivate:  onDismiss");
//                        updateCheckBoxes();
                        updateUiState();
                    }
                }).create();
        
        mErrorDialog.show();
    }

    
    private boolean isFailed(String status) {
        Log.d(TAG, "isFailed(" + status + ")");
        if (status == null ||
            (status != null &&
             (status.equals(SubscriptionManager.SUB_DEACTIVATE_FAILED)
              || status.equals(SubscriptionManager.SUB_DEACTIVATE_NOT_SUPPORTED)
              || status.equals(SubscriptionManager.SUB_ACTIVATE_FAILED)
              || status.equals(SubscriptionManager.SUB_ACTIVATE_NOT_SUPPORTED)))) {
            return true;
        }
        return false;
    }

    String setSubscriptionStatusToString(String status) {
        String retStr = null;
        if (status.equals(SubscriptionManager.SUB_ACTIVATE_SUCCESS)) {
            retStr = getResources().getString(R.string.set_sub_activate_success);
        } else if (status.equals(SubscriptionManager.SUB_DEACTIVATE_SUCCESS)) {
            retStr = getResources().getString(R.string.set_sub_deactivate_success);
        } else if (status.equals(SubscriptionManager.SUB_DEACTIVATE_FAILED)) {
            retStr = getResources().getString(R.string.set_sub_deactivate_failed);
        } else if (status.equals(SubscriptionManager.SUB_DEACTIVATE_NOT_SUPPORTED)) {
            retStr = getResources().getString(R.string.set_sub_deactivate_not_supported);
        } else if (status.equals(SubscriptionManager.SUB_ACTIVATE_FAILED)) {
            retStr = getResources().getString(R.string.set_sub_activate_failed);
        } else if (status.equals(SubscriptionManager.SUB_GLOBAL_ACTIVATE_FAILED)) {
            retStr = getResources().getString(R.string.set_sub_global_activate_failed);
        } else if (status.equals(SubscriptionManager.SUB_GLOBAL_DEACTIVATE_FAILED)) {
            retStr = getResources().getString(R.string.set_sub_global_deactivate_failed);
        } else if (status.equals(SubscriptionManager.SUB_ACTIVATE_NOT_SUPPORTED)) {
            retStr = getResources().getString(R.string.set_sub_activate_not_supported);
        } else if (status.equals(SubscriptionManager.SUB_NOT_CHANGED)) {
            retStr = getResources().getString(R.string.set_sub_no_change);
        }
        return retStr;
    }
    
    void displayAlertDialog(String msg[]) {
        int resSubId[] = {R.string.set_sub_1, R.string.set_sub_2, R.string.set_sub_3};
        String dispMsg = "";
        int title = R.string.set_sub_failed;

        if (msg[0] != null && isFailed(msg[0])) {
            subErr = true;
        }
        if (msg[1] != null && isFailed(msg[1])) {
            subErr = true;
        }

        for (int i = 0; i < msg.length; i++) {
            if (msg[i] != null) {
                dispMsg = dispMsg + getResources().getString(resSubId[i]) +
                                      setSubscriptionStatusToString(msg[i]) + "\n";
            }
        }

        if (!subErr) {
            title = R.string.set_sub_success;
        }

        Log.d(TAG, "displayAlertDialog:  dispMsg = " + dispMsg);
        new AuroraAlertDialog.Builder(this).setMessage(dispMsg)
            .setTitle(title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
//                    updateCheckBoxes();
                	updateUiState();
                }
            })
            .show()
            .setOnDismissListener(this);
    }
    
    private void dismissDialogSafely(int id) {
        Log.d(TAG, "dismissDialogSafely: id = " + id);
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException e) {
            // This is expected in the case where we were in the background
            // at the time we would normally have shown the dialog, so we didn't
            // show it.
        }
    }
    
 	
 	private void updateUiState(){
 	    Log.d(TAG, "updateUiState");
 	   //aurora modify liguangyu 20140530 for BUG #5274 start
 	    mHandler.postDelayed(new Runnable(){
 	    	public void run(){
 	            mButtonOperatorSelectionExpand1.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB1)),false);
 	            mButtonOperatorSelectionExpand2.auroraSetArrowText(getTitleFromOperatorNumber(PhoneUtils.getOperator(MSimConstants.SUB2)),false);
 	    	}
 	    }, 2000);
  	   //aurora modify liguangyu 20140530 for BUG #5274 end
 	    boolean isCardInsert1 = SimInfoUtils.isCardInsert(this, MSimConstants.SUB1);
 	    boolean isCardInsert2 = SimInfoUtils.isCardInsert(this, MSimConstants.SUB2);
 	    Log.d(TAG, "updateUiState, isCardInsert1 = " + isCardInsert1 +  " isCardInsert2 =" + isCardInsert2); 	    
	    updateCheckBoxes();
	    manageSub1.setEnabled(isCardInsert1);
	    manageSub1.setEnabled(isCardInsert2);
	    mSim1Icon.update(isCardInsert1 && mIsSimEnable[0]);
	    mSim2Icon.update(isCardInsert2 && mIsSimEnable[1]);		      
	    mSimSipnner.update();
        if(mDataSpinner != null) {
        	mDataSpinner.update();
        }
        
        mSim1Enable.setOnPreferenceChangeListener(null);
        mSim2Enable.setOnPreferenceChangeListener(null);
        mSim1Enable.setChecked(mIsSimEnable[0]);
        mSim2Enable.setChecked(mIsSimEnable[1]);
        mSim1Enable.setEnabled(isCardInsert1);
        mSim2Enable.setEnabled(isCardInsert2);
        mHandler.post(new Runnable(){
        	public void run(){
                mSim1Enable.setOnPreferenceChangeListener(SetSubscription.this);
                mSim2Enable.setOnPreferenceChangeListener(SetSubscription.this);
        	}
        });
            	
	    mButtonOperatorSelectionExpand1.setEnabled(mIsSimEnable[0]);
	    mButtonOperatorSelectionExpand2.setEnabled(mIsSimEnable[1]);
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(isCardInsert1 && mIsSimEnable[0]) {
         	mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
         	mButtonDataRoam.setEnabled(true);
         	mButtonPreferredNetworkMode.setEnabled(true);
         	mButtonPreferredNetworkMode2.setEnabled(true);
         	mButtonAPNExpand1.setEnabled(true);
         	
        } else {
         	mButtonDataRoam.setChecked(false);
         	mButtonDataRoam.setEnabled(false);
         	mButtonPreferredNetworkMode.setEnabled(false);
         	mButtonPreferredNetworkMode2.setEnabled(false);
         	mButtonAPNExpand1.setEnabled(false);
        }
        if(isCardInsert2 && mIsSimEnable[1]) {
         	mButtonDataRoam2.setChecked(mPhone2.getDataRoamingEnabled());
         	mButtonDataRoam2.setEnabled(true);
         	mButtonPreferredNetworkMode4.setEnabled(true);
         	mButtonAPNExpand2.setEnabled(true);
        } else {
         	mButtonDataRoam2.setChecked(false);
         	mButtonDataRoam2.setEnabled(false);
         	mButtonPreferredNetworkMode4.setEnabled(false);
         	mButtonAPNExpand2.setEnabled(false);
        }   
        
        int dataSub = MSimPhoneFactory.getDataSubscription();
        if(mIsSimEnable[0]) {
            if(dataSub == 0 && cm.getMobileDataEnabled()) {
		         manageSub1.addPreference(mButtonDataRoam);
		         manageSub1.addPreference(mButtonAPNExpand1);
			     manageSub1.addPreference(mButtonPreferredNetworkMode2);
            } else {
	   	         manageSub1.removePreference(mButtonDataRoam);
	   	         manageSub1.removePreference(mButtonAPNExpand1);
	   	         manageSub1.removePreference(mButtonPreferredNetworkMode2);
            }
//	         manageSub1.addPreference(mButtonOperatorSelectionExpand1);	
	         if(SimIconUtils.isIconBySiminfo) {
	        	 manageSub1.addPreference(mSim1Icon);	
	         } else {
	             manageSub1.removePreference(mSim1Icon);
	         }
        } else {
	         manageSub1.removePreference(mButtonDataRoam);
	         manageSub1.removePreference(mButtonAPNExpand1);
	         manageSub1.removePreference(mButtonPreferredNetworkMode2);
//	         manageSub1.removePreference(mButtonOperatorSelectionExpand1);	
	         manageSub1.removePreference(mSim1Icon);	
        }
        
        if(mIsSimEnable[1]) {
            if(dataSub == 1 && cm.getMobileDataEnabled()) {
		         manageSub2.addPreference(mButtonDataRoam2);
		         manageSub2.addPreference(mButtonAPNExpand2);
		         manageSub2.addPreference(mButtonPreferredNetworkMode4);
            } else {
            	manageSub2.removePreference(mButtonDataRoam2);
            	manageSub2.removePreference(mButtonAPNExpand2);
            	manageSub2.removePreference(mButtonPreferredNetworkMode4);
            }
//            manageSub2.addPreference(mButtonOperatorSelectionExpand2);	
	         if(SimIconUtils.isIconBySiminfo) {
	        	 manageSub2.addPreference(mSim2Icon);	
	         }  else {
	        	 manageSub2.removePreference(mSim2Icon);
	         }
        } else {
        	manageSub2.removePreference(mButtonDataRoam2);
        	manageSub2.removePreference(mButtonAPNExpand2);
        	manageSub2.removePreference(mButtonPreferredNetworkMode4);
//            manageSub2.removePreference(mButtonOperatorSelectionExpand2);	
            manageSub2.removePreference(mSim2Icon);	
        }
                
        
        if(cm.getMobileDataEnabled()) {
	        if(dataSub == 1) {
	        	manageSub1.setTitle(getResources().getString(R.string.sub_1));
	         	manageSub2.setTitle(getResources().getString(R.string.sub_2) + " - " + getResources().getString(R.string.third_data_support_title));
	        } else {
	        	manageSub1.setTitle(getResources().getString(R.string.sub_1) + " - " + getResources().getString(R.string.third_data_support_title));
	         	manageSub2.setTitle(getResources().getString(R.string.sub_2));
	        }
        } else {
        	manageSub1.setTitle(getResources().getString(R.string.sub_1));
         	manageSub2.setTitle(getResources().getString(R.string.sub_2));
        }
        
        mData.setEnabled(isCardInsert1 && isCardInsert2 && mIsSimEnable[0] && mIsSimEnable[1]);
        mPrioritySub.setEnabled(isCardInsert1 && isCardInsert2 && mIsSimEnable[0] && mIsSimEnable[1]);
        
 	}
 	
 	 private void displayDisableAllSimsAlertDialog() {
 	        if (!mIsForeground) {
 	            Log.d(TAG, "The activitiy is not in foreground. Do not display dialog!!!");
 	            return;
 	        }
 	        Log.d(TAG, "displayDisableAllSimsAlertDialog!");
 	        new AuroraAlertDialog.Builder(this).setMessage(R.string.disable_all_sim)
 	               .setTitle(android.R.string.dialog_alert_title)
 	               .setIcon(android.R.drawable.ic_dialog_alert)
 	               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 	                    public void onClick(DialogInterface dialog, int whichButton) {
 	        	        	setSubscription();
 	                    }
 	                })
// 	                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
// 	                    public void onClick(DialogInterface dialog, int whichButton) {
// 	                    	updateUiState();
// 	                    }
// 	                })
 	                .setOnDismissListener(new DialogInterface.OnDismissListener() {
 	                    public void onDismiss(DialogInterface dialog) {
 	                    	updateUiState();
 	                    }
 	                })
 	               .show();

       }
 	 
 	 
 	  private void setNewCardSubscription() {
 		    updateCheckBoxes();

 	        boolean simstate[] = {mIsSimEnable[0], mIsSimEnable[1]};
 	        Integer slot = ((MSimPhoneGlobals)PhoneGlobals.getInstance()).pollNewCardSlot();
 	        if(slot == null) {
// 	        	boolean isToSet = false;
// 	        	if(mIsNewCard[0] && !simstate[0]) {
// 	        		simstate[0] = true;
// 	        		isToSet = true;
// 	        	} 	
// 	        	if(mIsNewCard[1] && !simstate[1]) {
// 	        		simstate[1] = true;
// 	        		isToSet = true;
//
// 	        	}	
//	        	mIsNewCard[0] = false;
// 	            mIsNewCard[1] = false;
// 	        	if(!isToSet) {
 	 	        	return;
// 	        	}
 	        } else {
 	        	if(simstate[slot]) {
 	        		return;
 	        	} else {
 	        		simstate[slot] = true;
 	        	}
 	        }
 	        
 	        Log.d(TAG, "setNewCardSubscription sim1 = " + simstate[0] + " sim2 = " + simstate[1]);
 	        
 	        int deactRequiredCount = 0;
 	        subErr = false;

 	        if (isPhoneInCall()) {
// 	            displayErrorDialog(R.string.set_sub_not_supported_phone_in_call);
 	        } else {
 	            for (int i = 0; i < MAX_SUBSCRIPTIONS; i++) {
 	                if (simstate[i] == false) {
 	                    if (mCurrentSelSub.subscription[i].subStatus ==
 	                            Subscription.SubscriptionStatus.SUB_ACTIVATED) {
 	                        Log.d(TAG, "setSubscription: Sub " + i + " not selected. Setting 99999");
 	                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].subId = i;
 	                        mUserSelSub.subscription[i].subStatus = Subscription.
 	                                SubscriptionStatus.SUB_DEACTIVATE;

 	                        deactRequiredCount++;
 	                    }
 	                } else {
 	                    int slotId = i;
 	                    int subIndex = 0;

 	                    if (mCardSubscrInfo[slotId] == null) {
 	                        Log.d(TAG, "setSubscription: mCardSubscrInfo is not in sync with SubscriptionManager");
 	                        mUserSelSub.subscription[i].slotId = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].m3gppIndex = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].m3gpp2Index = SUBSCRIPTION_INDEX_INVALID;
 	                        mUserSelSub.subscription[i].subId = i;
 	                        mUserSelSub.subscription[i].subStatus = Subscription.
 	                                SubscriptionStatus.SUB_DEACTIVATE;

 	                        if (mCurrentSelSub.subscription[i].subStatus ==
 	                                Subscription.SubscriptionStatus.SUB_ACTIVATED) {
 	                            deactRequiredCount++;
 	                        }
 	                        continue;
 	                    }


 	                    // Compate the user selected subscriptio with the current subscriptions.
 	                    // If they are not matching, mark it to activate.
 	                    mUserSelSub.subscription[i].copyFrom(mCardSubscrInfo[slotId].
 	                            subscription[subIndex]);
 	                    mUserSelSub.subscription[i].subId = i;
 	                    if (mCurrentSelSub != null) {
 	                        // subStatus used to store the activation status as the mCardSubscrInfo
 	                        // is not keeping track of the activation status.
 	                        Subscription.SubscriptionStatus subStatus =
 	                                mCurrentSelSub.subscription[i].subStatus;
 	                        mUserSelSub.subscription[i].subStatus = subStatus;
 	                        if ((subStatus != Subscription.SubscriptionStatus.SUB_ACTIVATED) ||
 	                            (!mUserSelSub.subscription[i].equals(mCurrentSelSub.subscription[i]))) {
 	                            // User selected a new subscription.  Need to activate this.
 	                            mUserSelSub.subscription[i].subStatus = Subscription.
 	                            SubscriptionStatus.SUB_ACTIVATE;
 	                        }

 	                        if (mCurrentSelSub.subscription[i].subStatus == Subscription.
 	                                 SubscriptionStatus.SUB_ACTIVATED
 	                                 && mUserSelSub.subscription[i].subStatus == Subscription.
 	                                 SubscriptionStatus.SUB_ACTIVATE) {
 	                            deactRequiredCount++;
 	                        }
 	                    } else {
 	                        mUserSelSub.subscription[i].subStatus = Subscription.
 	                                SubscriptionStatus.SUB_ACTIVATE;
 	                    }
 	                }
 	            }
                boolean ret = mSubscriptionManager.setSubscription(mUserSelSub);
                Log.d(TAG, "mSubscriptionManager setSubscription =  ret = " + ret);
                if (ret) {
                    if(mIsForeground){
//                       showDialog(DIALOG_SET_SUBSCRIPTION_IN_PROGRESS);
                    	showSetSubProgressDialog();
                    }
                    mSubscriptionManager.registerForSetSubscriptionCompleted(mNewCardHandler,
                            EVENT_SET_NEW_SUBSCRIPTION_DONE, null);
                } else {
                    //TODO: Already some set sub in progress. Display a Toast?
                }
 	        }
 	    }
 	  
 	 AuroraAlertDialog mNewCardDialog ;
 	  
 	    private void notifyNewCardAvailable() {
 	        Log.d(TAG, "notifyNewCardAvailable()");

 	        if(mNewCardDialog!= null) {
 	        	mNewCardDialog.dismiss(); 
 	        	mNewCardDialog = null;
 	        }
 	        
 	       mNewCardDialog =  new AuroraAlertDialog.Builder(this).setMessage(R.string.new_cards_available)
 	            .setTitle(R.string.config_sub_title)
 	            .setIcon(android.R.drawable.ic_dialog_alert)
 	            .setPositiveButton(R.string.voicemail_settings, new DialogInterface.OnClickListener() {
 	                    public void onClick(DialogInterface dialog, int whichButton) {
 	                        Log.d(TAG, "new card dialog box:  onClick");
 	                    }
 	                })
 	             .setOnKeyListener(new DialogInterface.OnKeyListener() {
 		        	   @Override
 		        	   public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
 		        	   {
 		        	   if (keyCode == KeyEvent.KEYCODE_BACK)
 			        	    {
 			        	         return true;
 			        	    }
 			        	    else
 			        	    {
 			        	         return false; //默认返回 false，这里false不能屏蔽返回键，改成true就可以了
 			        	    }
 		        	   }
 	        	  })
 	        	.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {            	
 	            	  public void onClick(DialogInterface dialog, int whichButton) {
 	                        Log.d(TAG, "new card dialog box:  NegativeButton"); 	   
 	                        finish();
 	                    }
 	                })
 	            .create();   
 	        
 	        
 	         mNewCardDialog.getWindow().addFlags(
 	                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 	        mNewCardDialog.show();
 	    }
 	    
 	    private boolean mIsNewCardPending = false;
// 	    @Override
// 	    protected void onNewIntent(Intent intent) {
// 	    	 Log.d(TAG,"onNewIntent");
// 	         boolean newCardNotify2 = intent.getBooleanExtra("NOTIFY_NEW_CARD_AVAILABLE2", false);
//             if (newCardNotify2) {
//                 Log.d(TAG, "onNewIntent: Notify new cards are available2!!!!");
//                 if (!mSubscriptionManager.isSetSubscriptionInProgress()) {
//                     setNewCardSubscription();
//                 } else {
//                	 Log.d(TAG, "onNewIntent: delay");
//                	 mIsNewCardPending = true;
//                     mNewCardHandler.sendEmptyMessageDelayed(EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT, 4000);
//                 }
//                 if(mNewCardDialog==null||!mNewCardDialog.isShowing()) {//aurora add zhouxiaobing 20140526
//                	 notifyNewCardAvailable();
//                 }
//             } 
//             
// 	    }
 	    
 	   private Handler mNewCardHandler = new Handler() {
 	        @Override
 	        public void handleMessage(Message msg) {

 	            switch(msg.what) {
 	                case EVENT_SET_NEW_SUBSCRIPTION_DONE:
 	       	            Log.d(TAG, "EVENT_SET_NEW_SUBSCRIPTION_DONE");
 	                    mSubscriptionManager.unRegisterForSetSubscriptionCompleted(mNewCardHandler);
 	                    mNewCardHandler.removeMessages(EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT);
// 	                    dismissDialogSafely(DIALOG_SET_SUBSCRIPTION_IN_PROGRESS);
 	                   dismissAuroraDialogSafely();
 	                    getPreferenceScreen().setEnabled(true);
 	                    if(mIsNewCardPending) {
 	                        setNewCardSubscription();
 	                    	mIsNewCardPending = false;
 	                    } else {
 	                        updateUiState();	
 	                    }
 	                	break;
 	                case EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT:
 	                   Log.d(TAG, "EVENT_SET_NEW_SUBSCRIPTION_TIMEOUT ");
 	                   if(mIsNewCardPending) {
	                        setNewCardSubscription();
	                    	mIsNewCardPending = false;
	                    } else {
	                        updateUiState();	
	                    }
 	                	break;
 	                default:
 	                        Log.w(TAG, "Unknown Event " + msg.what);
 	                        break;
 	            }
 	        }
 	    };
 	    
// 	   private SubscriptionData mUserPrefSubs = null;
// 	   private boolean[] mIsNewCard = new boolean[mNumPhones];
// 	   private static int USER_PREF_SUB_FIELDS = 6;
// 	   private void getNewCardState() {
// 	        boolean errorOnParsing = false;
//
// 	        mUserPrefSubs = new SubscriptionData(mNumPhones);
//
// 	        for(int i = 0; i < mNumPhones; i++) {
// 	            String strUserSub = Settings.Global.getString(getContentResolver(),
// 	                    Settings.Global.MULTI_SIM_USER_PREFERRED_SUBS[i]);
// 	            if (strUserSub != null) {
// 	            	Log.w(TAG, "getUserPreferredSubs: strUserSub = " + strUserSub);
//
// 	                try {
// 	                    String splitUserSub[] = strUserSub.split(",");
//
// 	                    // There should be 6 fields in the user preferred settings.
// 	                    if (splitUserSub.length == USER_PREF_SUB_FIELDS) {
// 	                        if (!TextUtils.isEmpty(splitUserSub[0])) {
// 	                            mUserPrefSubs.subscription[i].iccId = splitUserSub[0];
// 	                        }
// 	                        if (!TextUtils.isEmpty(splitUserSub[1])) {
// 	                            mUserPrefSubs.subscription[i].appType = splitUserSub[1];
// 	                        }
// 	                        if (!TextUtils.isEmpty(splitUserSub[2])) {
// 	                            mUserPrefSubs.subscription[i].appId = splitUserSub[2];
// 	                        }
//
// 	                        try {
// 	                            int subStatus = Integer.parseInt(splitUserSub[3]);
// 	                            mUserPrefSubs.subscription[i].subStatus =
// 	                                SubscriptionStatus.values()[subStatus];
// 	                        } catch (NumberFormatException ex) {
// 	                        	Log.w(TAG, "getUserPreferredSubs: NumberFormatException: " + ex);
// 	                            mUserPrefSubs.subscription[i].subStatus =
// 	                                SubscriptionStatus.SUB_INVALID;
// 	                        }
//
// 	                        try {
// 	                            mUserPrefSubs.subscription[i].m3gppIndex =
// 	                                Integer.parseInt(splitUserSub[4]);
// 	                        } catch (NumberFormatException ex) {
// 	                        	Log.w(TAG,
// 	                                    "getUserPreferredSubs:m3gppIndex: NumberFormatException: "
// 	                                    + ex);
// 	                            mUserPrefSubs.subscription[i].m3gppIndex =
// 	                                Subscription.SUBSCRIPTION_INDEX_INVALID;
// 	                        }
//
// 	                        try {
// 	                            mUserPrefSubs.subscription[i].m3gpp2Index =
// 	                                Integer.parseInt(splitUserSub[5]);
// 	                        } catch (NumberFormatException ex) {
// 	                        	Log.w(TAG,
// 	                                    "getUserPreferredSubs:m3gpp2Index: NumberFormatException: "
// 	                                    + ex);
// 	                            mUserPrefSubs.subscription[i].m3gpp2Index =
// 	                                Subscription.SUBSCRIPTION_INDEX_INVALID;
// 	                        }
//
// 	                    } else {
// 	                    	Log.w(TAG,
// 	                                "getUserPreferredSubs: splitUserSub.length != "
// 	                                + USER_PREF_SUB_FIELDS);
// 	                        errorOnParsing = true;
// 	                    }
// 	                } catch (PatternSyntaxException pe) {
// 	                	Log.w(TAG,
// 	                            "getUserPreferredSubs: PatternSyntaxException while split : "
// 	                            + pe);
// 	                    errorOnParsing = true;
//
// 	                }
// 	            }
//
// 	            if (strUserSub == null || errorOnParsing) {
// 	                String defaultUserSub = "" + ","        // iccId
// 	                    + "" + ","                          // app type
// 	                    + "" + ","                          // app id
// 	                    + Integer.toString(SubscriptionStatus.SUB_INVALID.ordinal()) // activate state
// 	                    + "," + Subscription.SUBSCRIPTION_INDEX_INVALID   // 3gppIndex in the card
// 	                    + "," + Subscription.SUBSCRIPTION_INDEX_INVALID;  // 3gpp2Index in the card
//
// 	                Settings.Global.putString(getContentResolver(),
// 	                        Settings.Global.MULTI_SIM_USER_PREFERRED_SUBS[i], defaultUserSub);
//
// 	                mUserPrefSubs.subscription[i].iccId = null;
// 	                mUserPrefSubs.subscription[i].appType = null;
// 	                mUserPrefSubs.subscription[i].appId = null;
// 	                mUserPrefSubs.subscription[i].subStatus = SubscriptionStatus.SUB_INVALID;
// 	                mUserPrefSubs.subscription[i].m3gppIndex = Subscription.SUBSCRIPTION_INDEX_INVALID;
// 	                mUserPrefSubs.subscription[i].m3gpp2Index = Subscription.SUBSCRIPTION_INDEX_INVALID;
// 	            }
//
// 	            mUserPrefSubs.subscription[i].subId = i;
//
// 	           Log.w(TAG, "getUserPreferredSubs: mUserPrefSubs.subscription[" + i + "] = " + mUserPrefSubs.subscription[i]);
// 	           SubscriptionData cardSubInfo = mCardSubscriptionManager.getCardSubscriptions(i);
//  	          if (cardSubInfo == null) {
//                  mIsNewCard[i] = true;
//                  continue;
//  	           }
//  	          
//              if (cardSubInfo.hasSubscription(mUserPrefSubs.subscription[i])) {
//                  mIsNewCard[i] = false;
//              } else {
//                  mIsNewCard [i] = true;
//              }
//   	        	Log.w(TAG, "getNewCardState: mIsNewCard [" + i + "] = " + mIsNewCard [i]);
// 	        }
// 	         	
// 	    }
// 	    @Override
 	    public void onWindowFocusChanged(boolean hasFocus) {
 	        // the dtmf tones should no longer be played
 	        if(hasFocus) {
 	            mCardSubscriptionManager.registerForSimStateChanged(mHandler, EVENT_SIM_STATE_CHANGED, null);
 	        } else {
 	            mCardSubscriptionManager.unRegisterForSimStateChanged(mHandler);
 	        }
// 	       updateUiState();
 	    }
 	    
 	   @Override
 	    protected void onStop() {
 	        super.onStop();
            if(mNewCardDialog!=null && mNewCardDialog.isShowing()) {
           	 	finish();
            }
 	   }
 	   
 	   //aurora add liguangyu 20140616 for BUG #5255 start
 	   AuroraProgressDialog mProgressDialog = null;
 	   
 	   private void showSetSubProgressDialog() {
 		   if(mProgressDialog != null) {
 			  mProgressDialog.dismiss();
 			  mProgressDialog = null;
 		   }
 		   mProgressDialog = new AuroraProgressDialog(this);

 		   mProgressDialog.setMessage(getResources().getString(R.string.set_uicc_subscription_progress));
 		   mProgressDialog.setCancelable(false);
 		   mProgressDialog.setIndeterminate(true);
           mProgressDialog.show();
 	   } 
 	   
 	    private void dismissAuroraDialogSafely() {
 	        Log.d(TAG, "dismissAuroraDialogSafely");
 	        try {
 	 		   if(mProgressDialog != null) {
 	 			  mProgressDialog.dismiss();
 	 			  mProgressDialog = null;
 	 		   }
 	        } catch (IllegalArgumentException e) {
 	        	e.printStackTrace();
 	        }
 	    }
 	    //aurora add liguangyu 20140616 for BUG #5255 end
 	    
 	    
 	   //aurora add liguangyu 20140819 for BUG #7694 start
 	    private boolean mIsNew = false;
 	    private HomeRecevier mHomeRecevier = new HomeRecevier();
 	    class HomeRecevier extends BroadcastReceiver {
 		  
 	        final String SYSTEM_DIALOG_REASON_KEY = "reason";
 	 
 	        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
 	 
 	        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
 	 
 	        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
 	 
 	        @Override
 	        public void onReceive(Context context, Intent intent) {
 	            String action = intent.getAction();
  	            Log.d(TAG, "home onReceive action =" + action);
 	            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
 	                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
 	                if (reason != null) {
 	    	            Log.d(TAG, "home onReceive reason =" + reason);
 	                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
 	                        //press home
 	                    	finish();
 	                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
 	                        //long press home
 	                    }
 	                }
 	            }
 	        }
 	    }
 	   
 	    @Override
 	    public void onBackPressed() {
 	    	finish();
 	    }
  	    //aurora add liguangyu 20140819 for BUG #7694 end
 	    
 	    private boolean mOkClicked;
 	    //This is a method implemented for DialogInterface.OnClickListener.
 	    //  Used to dismiss the dialogs when they come up.
 	    public void onClick(DialogInterface dialog, int which) {
 		 	   Log.d("dataRoamSwitchPreference", "onClick");
 		     if (which == DialogInterface.BUTTON_POSITIVE) {
 		     	Log.d("dataRoamSwitchPreference", "true");
 		        android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
 		                android.provider.Settings.Global.DATA_ROAMING + 0, 1);
 		         mPhone.setDataRoamingEnabled(true);
 		         mOkClicked = true;
 		     } else {
 		      	Log.d("dataRoamSwitchPreference", "false");
 		     }
 	    }

 		 public void onDismiss(DialogInterface dialog) {
 		 	   Log.d("dataRoamSwitchPreference", "onDismiss");
 		     // Assuming that onClick gets called first
 	         mButtonDataRoam.setChecked(mOkClicked);
 		 }
 		 
 		 
 		private void handleRoamSwitch(final int sub, boolean isChecked ) {
 	    	 
 	    	 if(!isSimEnable(sub)) {
 	    		 return;
 	    	 }    	 
 	    	 
 	    	 if(sub == 0 && isChecked == mPhone.getDataRoamingEnabled()) {
 	 			if (DBG) log("onPreferenceChange: preference = mButtonDataRoam. return 1");
 	    		 return ;
 	    	 }
 	    	 
 	    	 if(sub == 1 && isChecked == mPhone2.getDataRoamingEnabled()) {
 	  			if (DBG) log("onPreferenceChange: preference = mButtonDataRoam. return 2");
 	    		 return ;
 	    	 }
 	    	 
 	    	 
 				if (DBG) log("onPreferenceChange: preference = mButtonDataRoam. sub = " + sub + " isChecked = " + isChecked);
 				
 				DialogInterface.OnClickListener dataRoamOnclickListener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (which == DialogInterface.BUTTON_POSITIVE) {
 							Log.d("dataRoamSwitchPreference" + sub, "true");
 							android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),android.provider.Settings.Global.DATA_ROAMING + sub, 1);
 							PhoneGlobals.getInstance().getPhone(sub).setDataRoamingEnabled(true);
 							mOkClicked = true;
 						} else {
 							Log.d("dataRoamSwitchPreference" + sub, "false");
 						}
 					}
 				};
 				
 				// normally called on the toggle click
 				if (isChecked) {
 					// First confirm with a warning dialog about charges
 					mOkClicked = false;
 					new AuroraAlertDialog.Builder(this)
 							.setMessage(getResources().getString(R.string.roaming_warning))
 							.setTitle(android.R.string.dialog_alert_title)
 							.setIconAttribute(android.R.attr.alertDialogIcon)
 							.setPositiveButton(android.R.string.yes, dataRoamOnclickListener)
 							.setNegativeButton(android.R.string.no, dataRoamOnclickListener)
 						    .setOnDismissListener(new DialogInterface.OnDismissListener() {

 								public void onDismiss(DialogInterface dialog) {
 									Log.d("dataRoamSwitchPreference" + sub, "onDismiss");
 									// Assuming that onClick gets called
 									// first
 									if(sub != 0) {
 										mButtonDataRoam2.setChecked(mOkClicked);
 									} else {
 										mButtonDataRoam.setChecked(mOkClicked);
 									}
 								}
 							}).show();
 				} else {
 					android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(), android.provider.Settings.Global.DATA_ROAMING + sub, 0);
 					PhoneGlobals.getInstance().getPhone(sub).setDataRoamingEnabled(false);
 				}
 	     }
 		
 	     private boolean isSimEnable(int sub) {
 		   	boolean simSubState = mIsSimEnable[sub];
 	     	boolean cardReady = MSimTelephonyManager.getDefault().getSimState(sub) == TelephonyManager.SIM_STATE_READY; 
 	     	boolean result = cardReady && simSubState;
 	        if (DBG) log("isSimEnable sub = " + sub + " result = " + result);
 	     	return result;
 	     }
 	     
 	    private void updateDataPreference() {
 			 if (!DeviceUtils.isSupportDualData()) {
 				 return;
 			 }
 	         Log.d("LOG_TAG", "updateDataPreference");
 	         int dataSub = MSimPhoneFactory.getDataSubscription();
 	        ConnectivityManager cm =
 	                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 	        if(cm.getMobileDataEnabled()) {
	 	  		 if(dataSub != 0) {
	 		         manageSub1.removePreference(mButtonDataRoam);
	 		         manageSub1.removePreference(mButtonAPNExpand1);
	 		         manageSub1.removePreference(mButtonPreferredNetworkMode2);	  
	 		         manageSub2.addPreference(mButtonDataRoam2);
	 		         manageSub2.addPreference(mButtonAPNExpand2);
	 		         manageSub2.addPreference(mButtonPreferredNetworkMode4);	  
	 			 } else {
	 		         manageSub1.addPreference(mButtonDataRoam);
	 		         manageSub1.addPreference(mButtonAPNExpand1);
	 		         manageSub1.addPreference(mButtonPreferredNetworkMode2);	  
	 		         manageSub2.removePreference(mButtonDataRoam2);
	 		         manageSub2.removePreference(mButtonAPNExpand2);
	 		         manageSub2.removePreference(mButtonPreferredNetworkMode4);	 	         
	 			 }  
 	        } else {
		         manageSub1.removePreference(mButtonDataRoam);
 		         manageSub1.removePreference(mButtonAPNExpand1);
 		         manageSub1.removePreference(mButtonPreferredNetworkMode2);
 		         manageSub2.removePreference(mButtonDataRoam2);
 		         manageSub2.removePreference(mButtonAPNExpand2);
 		         manageSub2.removePreference(mButtonPreferredNetworkMode4);	 
 	        }
 	  
 	         
 	     }
 	    
 	   private void updateOperatorSelectionVisibility() {
 	         log("updateOperatorSelectionVisibility. mPhone = " + mPhone.getPhoneName());
 	         Resources res = getResources();
 	         
 	         AuroraPreferenceScreen mPrefScreen = getPreferenceScreen();
 	         if (mButtonOperatorSelectionExpand1 == null) {
 	             android.util.Log.e(TAG, "mButtonOperatorSelectionExpand is null");
 	         } else {
 		         if (!mPhone.isManualNetSelAllowed()) {
 		             log("Manual network selection not allowed.Disabling Operator Selection menu.");
 		             mButtonOperatorSelectionExpand1.setEnabled(false);
 		         } else if (res.getBoolean(R.bool.csp_enabled)) {
 		             if (mPhone.isCspPlmnEnabled()) {
 		                 log("[CSP] Enabling Operator Selection menu.");
 		                 mButtonOperatorSelectionExpand1.setEnabled(true);
 		             } else {
 		                 log("[CSP] Disabling Operator Selection menu.");
 		                 if (mButtonOperatorSelectionExpand1 != null) {
 		                     mPrefScreen.removePreference(mButtonOperatorSelectionExpand1);
 		                     mButtonOperatorSelectionExpand1 = null;
 		                 }
 		             }
 		         }
 	         }
 	         
 	         Phone phone2 = PhoneGlobals.getInstance().getPhone(1); 
 	         if (mButtonOperatorSelectionExpand2 == null) {
 	             android.util.Log.e(TAG, "mButtonOperatorSelectionExpand is null");
 	         } else {
 		         if (!phone2.isManualNetSelAllowed()) {
 		             log("Manual network selection not allowed.Disabling Operator Selection menu.");
 		             mButtonOperatorSelectionExpand2.setEnabled(false);
 		         } else if (res.getBoolean(R.bool.csp_enabled)) {
 		             if (phone2.isCspPlmnEnabled()) {
 		                 log("[CSP] Enabling Operator Selection menu.");
 		                 mButtonOperatorSelectionExpand2.setEnabled(true);
 		             } else {
 		                 log("[CSP] Disabling Operator Selection menu.");
 		                 if (mButtonOperatorSelectionExpand2 != null) {
 		                     mPrefScreen.removePreference(mButtonOperatorSelectionExpand2);
 		                     mButtonOperatorSelectionExpand2 = null;
 		                 }
 		             }
 		         }
 	         }
 	     }
 	   
 	   private int getPreferredNetworkMode(int sub) {
 	         int nwMode;
 	         try {
 	             nwMode = android.telephony.MSimTelephonyManager.getIntAtIndex(
 	                     mPhone.getContext().getContentResolver(),
 	                     android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
 	                     sub);
 	         } catch (SettingNotFoundException snfe) {
 	             log("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
 	             nwMode = preferredNetworkMode;
 	         }
 	         return nwMode;
 	     }

 	     private void setPreferredNetworkMode(int nwMode , int sub) {
 	         android.telephony.MSimTelephonyManager.putIntAtIndex(
 	                     mPhone.getContext().getContentResolver(),
 	                     android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
 	                     sub, nwMode);
 	     }
 	   
 	  static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
      private MyHandler mMobileHandler;
      private class MyHandler extends Handler {

          static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 0;
          static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 1;

          @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                  case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
                      handleGetPreferredNetworkTypeResponse(msg);
                      break;

                  case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                      handleSetPreferredNetworkTypeResponse(msg);
                      break;
              }
          }

          private void handleGetPreferredNetworkTypeResponse(Message msg) {
              AsyncResult ar = (AsyncResult) msg.obj;
              int sub = msg.arg1;

              if (ar.exception == null) {
                  int modemNetworkMode = ((int[])ar.result)[0];

                  if (DBG) {
                      log ("handleGetPreferredNetworkTypeResponse: modemNetworkMode = " +
                              modemNetworkMode);
                  }

                  int settingsNetworkMode = getPreferredNetworkMode(sub);
                  if (DBG) {
                      log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                              settingsNetworkMode);
                  }

                  //check that modemNetworkMode is from an accepted value
                  if ((modemNetworkMode >= Phone.NT_MODE_WCDMA_PREF)
                          && (modemNetworkMode <= Phone.NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA)) {
                      if (DBG) {
                          log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = " +
                                  modemNetworkMode);
                      }

                      //check changes in modemNetworkMode and updates settingsNetworkMode
                      if (modemNetworkMode != settingsNetworkMode) {
                          if (DBG) {
                              log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                      "modemNetworkMode != settingsNetworkMode");
                          }

                          settingsNetworkMode = modemNetworkMode;

                          if (DBG) { log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                  "settingsNetworkMode = " + settingsNetworkMode);
                          }

                          //changes the Settings.System accordingly to modemNetworkMode
                          setPreferredNetworkMode(settingsNetworkMode , sub);
                      }

                      UpdatePreferredNetworkModeSummary(modemNetworkMode);
                      // changes the mButtonPreferredNetworkMode accordingly to modemNetworkMode
                      mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                      if(sub == 0) {
                     	 mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                      } else if(sub == 1) {
                     	 mButtonPreferredNetworkMode4.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                      }
                  } else {
                      if (DBG) log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                      resetNetworkModeToDefault();
                  }
                  // Update '2GOnly checkbox' based on recent preferred network type selection.
//                  Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
              }
          }

          private void handleSetPreferredNetworkTypeResponse(Message msg) {
              AsyncResult ar = (AsyncResult) msg.obj;

              if (ar.exception == null) {
//                  int networkMode = Integer.valueOf(
//                          mButtonPreferredNetworkMode.getValue()).intValue();
//                  setPreferredNetworkMode(networkMode, 0);
                  mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
              } else {
                  mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE, 0 , -1));
              }
          }

          private void resetNetworkModeToDefault() {
              //set the Settings.System
              mButtonPreferredNetworkMode.setValue(Integer.toString(preferredNetworkMode));
              setPreferredNetworkMode(preferredNetworkMode, 0);
              //Set the Modem
              mPhone.setPreferredNetworkType(preferredNetworkMode,
                      this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
          }
      }
      
      
      private int getPreferredNetworkStringId(int sub) {
       	 int NetworkMode = getPreferredNetworkMode(sub);
            switch(NetworkMode) {
            		case 0:
            		case 18:
            			return R.string.aurora_preferred_network_mode_choices_3p;        		
            		case 1:
            			return R.string.aurora_preferred_network_mode_choices_2;        			
            		case 2:
         		case 14:
            			return R.string.aurora_preferred_network_mode_choices_3;
            		default : 
            			return R.string.aurora_preferred_network_mode_choices_4p;
            }     
       }
      
      
      private void UpdatePreferredNetworkModeSummary(int NetworkMode) {
          switch(NetworkMode) {
              case NT_MODE_WCDMA_PREF:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_wcdma_perf_summary);
                  break;
              case NT_MODE_GSM_ONLY:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_gsm_only_summary);
                  break;
              case NT_MODE_WCDMA_ONLY:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_wcdma_only_summary);
                  break;
              case NT_MODE_GSM_UMTS:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_gsm_wcdma_summary);
                  break;
              case NT_MODE_CDMA:
                  switch (mPhone.getLteOnCdmaMode()) {
                      case PhoneConstants.LTE_ON_CDMA_TRUE:
                          mButtonPreferredNetworkMode.setSummary(
                              R.string.preferred_network_mode_cdma_summary);
                      break;
                      case PhoneConstants.LTE_ON_CDMA_FALSE:
                      default:
                          mButtonPreferredNetworkMode.setSummary(
                              R.string.preferred_network_mode_cdma_evdo_summary);
                          break;
                  }
                  break;
              case NT_MODE_CDMA_NO_EVDO:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_cdma_only_summary);
                  break;
              case NT_MODE_EVDO_NO_CDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_evdo_only_summary);
                  break;
              case NT_MODE_LTE_ONLY:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_lte_summary);
                  break;
              case NT_MODE_LTE_GSM_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_lte_gsm_wcdma_summary);
                  break;
              case NT_MODE_LTE_CDMA_AND_EVDO:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_lte_cdma_evdo_summary);
                  break;
              case NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_global_summary);
                  break;
              case NT_MODE_GLOBAL:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_cdma_evdo_gsm_wcdma_summary);
                  break;
             case NT_MODE_LTE_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_lte_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_ONLY:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_only_summary);
                  break;
              case NT_MODE_TD_SCDMA_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_LTE:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_LTE:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_WCDMA_LTE:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_wcdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_WCDMA_LTE:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_wcdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_cdma_evdo_gsm_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_td_scdma_lte_cdma_evdo_gsm_wcdma_summary);
                  break;
              default:
                  mButtonPreferredNetworkMode.setSummary(
                          R.string.preferred_network_mode_global_summary);
          } 
      }
      
      
      public static final int NT_MODE_WCDMA_PREF   = Constants.NETWORK_MODE_WCDMA_PREF;
      public static final int NT_MODE_GSM_ONLY     = Constants.NETWORK_MODE_GSM_ONLY;
      public static final int NT_MODE_WCDMA_ONLY   = Constants.NETWORK_MODE_WCDMA_ONLY;
      public static final int NT_MODE_GSM_UMTS     = Constants.NETWORK_MODE_GSM_UMTS;

      public static final int NT_MODE_CDMA         = Constants.NETWORK_MODE_CDMA;

      public static final int NT_MODE_CDMA_NO_EVDO = Constants.NETWORK_MODE_CDMA_NO_EVDO;
      public static final int NT_MODE_EVDO_NO_CDMA = Constants.NETWORK_MODE_EVDO_NO_CDMA;
      public static final int NT_MODE_GLOBAL       = Constants.NETWORK_MODE_GLOBAL;

      public static final int NT_MODE_LTE_CDMA_AND_EVDO        = Constants.NETWORK_MODE_LTE_CDMA_EVDO;
      public static final int NT_MODE_LTE_GSM_WCDMA            = Constants.NETWORK_MODE_LTE_GSM_WCDMA;
      public static final int NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA  = Constants.NETWORK_MODE_LTE_CMDA_EVDO_GSM_WCDMA;
      public static final int NT_MODE_LTE_ONLY                 = Constants.NETWORK_MODE_LTE_ONLY;
      public static final int NT_MODE_LTE_WCDMA                = Constants.NETWORK_MODE_LTE_WCDMA;
      public static final int PREFERRED_NT_MODE                = Constants.PREFERRED_NETWORK_MODE;

      public static final int NT_MODE_TD_SCDMA_ONLY            = Constants.NETWORK_MODE_TD_SCDMA_ONLY;
      public static final int NT_MODE_TD_SCDMA_WCDMA           = Constants.NETWORK_MODE_TD_SCDMA_WCDMA;
      public static final int NT_MODE_TD_SCDMA_LTE             = Constants.NETWORK_MODE_TD_SCDMA_LTE;
      public static final int NT_MODE_TD_SCDMA_GSM             = Constants.NETWORK_MODE_TD_SCDMA_GSM;
      public static final int NT_MODE_TD_SCDMA_GSM_LTE         = Constants.NETWORK_MODE_TD_SCDMA_GSM_LTE;
      public static final int NT_MODE_TD_SCDMA_GSM_WCDMA       = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA;
      public static final int NT_MODE_TD_SCDMA_WCDMA_LTE       = Constants.NETWORK_MODE_TD_SCDMA_WCDMA_LTE;
      public static final int NT_MODE_TD_SCDMA_GSM_WCDMA_LTE   = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE;
      public static final int NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA;
      public static final int NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA;
      
      
      private static final boolean DBG = true;
      private static void log(String msg) {
          Log.d(TAG, msg);
      }
      
		private void handleSimSwitch(final int sub, boolean isChecked ) {	    
 	        Log.d(TAG, "onCheckedChanged: handleSimSwitch isChecked = " + isChecked+ " sub =" + sub);
 	        
 	       if (mErrorDialog != null && mErrorDialog.isShowing()) {
 	        	return;
 	        }
 	        
 	        
 	       mAuroraSwitchState[sub] = isChecked;
 	       boolean otherChecked = false;
 	       int otherSub;
 	       if(sub > 0) {
 	    	  otherSub = 0;
 	    	  otherChecked = mSim1Enable.isChecked();
 	       } else {
 	    	  otherSub = 1;
 	    	  otherChecked = mSim2Enable.isChecked();
 	       }
            mAuroraSwitchState[otherSub] = otherChecked;
 	        if(!isChecked && !otherChecked) {
 	        	 displayErrorDialog(R.string.deact_all_sub_not_supported);
 	        } else if(PhoneGlobals.getInstance().mManagePhbReading.isInPhbLoadProcess()) {
 	        	 displayErrorDialog(R.string.deact_all_sub_not_supported_for_phb);
 	        } else { 	   
 	        	setSubscription();
 	        }
 	         ((MSimPhoneGlobals)MSimPhoneGlobals.getInstance()).isUserSwitchData = true;
        
		}
}
