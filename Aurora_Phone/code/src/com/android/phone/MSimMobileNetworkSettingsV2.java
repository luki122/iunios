/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants;

import aurora.preference.*;
import aurora.widget.*;
import aurora.app.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.content.BroadcastReceiver;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.IntentFilter;
import android.content.res.Resources;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.telephony.TelephonyManager.SIM_STATE_READY;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneProxy;

import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import android.view.View;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;

import com.mediatek.telecom.TelecomManagerEx;

import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;

import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.internal.telephony.ltedc.svlte.SvlteRatController;
import com.mediatek.internal.telephony.ltedc.LteDcPhoneProxy;
import com.android.ims.ImsManager;
import com.android.phone.PhoneUtils;

/**
 * "MSim Mobile network settings" screen. This preference screen lets you
 * enable/disable mobile data, and control data roaming and other
 * network-specific mobile data features. It's used on non-voice-capable tablets
 * as well as regular phone devices.
 *
 * Note that this PreferenceActivity is part of the phone app, even though you
 * reach it from the "Wireless & Networks" section of the main Settings app.
 * It's not part of the "Call settings" hierarchy that's available from the
 * Phone app (see MSimCallFeaturesSetting for that.)
 */
public class MSimMobileNetworkSettingsV2 extends TimeConsumingPreferenceActivity
		implements AuroraPreference.OnPreferenceChangeListener, PhoneServiceStateHandler.Listener {

	// debug data
	private static final String LOG_TAG = "MSimMobileNetworkSettingsV2";
	private static final boolean DBG = true;

	private static final String KEY_DATA = "data";
	private static final String PRIORITY_SUB = "priority_subscription";

	private AuroraListPreference mData;
	private AuroraListPreference mPrioritySub;
	private AuroraSwitchPreference mAuroraButtonDataEnabled;
	private AuroraPreferenceScreen mSim1, mSim2;
	private AuroraListPreference mButtonPreferredNetworkModeList;
	private AuroraListPreference mButtonCdmaPreferredNetworkModeList;
    private Enhanced4GLteSwitchPreference mEnhancedButton4glte;

	private CharSequence[] entries; // Used for entries like Subscription1,
									// Subscription2 ...
	private CharSequence[] entryValues; // Used for entryValues like 0, 1 ,2 ...
	private CharSequence[] summaries; // Used for Summaries like Aubscription1,
										// Subscription2....
	private CharSequence[] entriesPrompt; // Used in case of prompt option is
											// required.
	private CharSequence[] entryValuesPrompt; // Used in case of prompt option
												// is required.
	private CharSequence[] summariesPrompt; // Used in case of prompt option is
											// required.

	private Phone[] phoneList;
	private Phone mPhone;
	private Phone mPhone2;
	
	private PhoneServiceStateHandler mStateHandler;

	@Override
	protected void onCreate(Bundle icicle) {
		log("onCreate");
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.msim_network_setting_v2);

		mSubscriptionManager = SubscriptionManager.from(this);
		mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
		// mPhone =
		// ((MSimPhoneGlobals)PhoneGlobals.getInstance()).getDefaultPhone();
		mPhone = PhoneGlobals.getInstance().getPhone(0);
		mPhone2 = PhoneGlobals.getInstance().getPhone(1);
		phoneList = new Phone[2];
		phoneList[0] = mPhone;
		phoneList[1] = mPhone2;
		// get UI object references
		AuroraPreferenceScreen prefSet = getPreferenceScreen();
	
		mAuroraButtonDataEnabled = (AuroraSwitchPreference) prefSet
				.findPreference("aurora_data_enabled_key");
		mAuroraButtonDataEnabled.setOnPreferenceChangeListener(this);
		
		mSim1 = (AuroraPreferenceScreen) prefSet
				.findPreference("sim_1");
		mSim1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
		mSim2 = (AuroraPreferenceScreen) prefSet
				.findPreference("sim_2");
		mSim2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
		int numPhones = TelephonyManager.getDefault().getSimCount();
		// Create and Intialize the strings required for MultiSIM
		// Dynamic creation of entries instead of using static array vlues.
		// entries are Subscription1, Subscription2, Subscription3 ....
		// EntryValues are 0, 1 ,2 ....
		// Summaries are Subscription1, Subscription2, Subscription3 ....
		int num = numPhones + 1;
		entries = new CharSequence[num];
		entryValues = new CharSequence[num];
		summaries = new CharSequence[num];
		entriesPrompt = new CharSequence[num];
		entryValuesPrompt = new CharSequence[num];
		summariesPrompt = new CharSequence[num];
		CharSequence[] subString = getResources().getTextArray(
				R.array.multi_sim_entries);
		int i = 0;
		for (i = 0; i < num ; i++) {
			entries[i] = subString[i];
			String name = getSimName(i);
			if(!TextUtils.isEmpty(name)) {
				entries[i] = name;
			}
			summaries[i] = subString[i];
			summariesPrompt[i] = subString[i];
			entriesPrompt[i] = subString[i];
			entryValues[i] = Integer.toString(i);
			entryValuesPrompt[i] = Integer.toString(i);
		}

		if (!DeviceUtils.isSupportDualData()) {
			AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory) findPreference("sim_3rd_support_key");
			dataPreferenceCategory.removePreference(mData);
		}

		IntentFilter homeIf = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mHomeRecevier, homeIf);

		mTelephonyManager = TelephonyManager.from(this);		
		
	    initIntentFilter();
        registerReceiver(mSubReceiver, mIntentFilter);
        
   	    mStateHandler = new PhoneServiceStateHandler(this);
        mStateHandler.addPhoneServiceStateListener(this);
    	mMobileHandler = new MyHandler();
    	
    	int datasub = getDataSub();
    	
    	AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory) findPreference("sim_data_support_key");
    	
    	mButtonPreferredNetworkModeList = (AuroraListPreference) prefSet
				.findPreference("preferred_network_mode_key");
    	mButtonPreferredNetworkModeList
				.setOnPreferenceChangeListener(this);
		mButtonPreferredNetworkModeList.setValue(Integer
				.toString(AuroraNetworkUtils.convertNetworkMode(AuroraNetworkUtils.getPreferredNetworkMode(getDataSub()))));
		
	  	mButtonCdmaPreferredNetworkModeList = (AuroraListPreference) prefSet
				.findPreference("cdma_preferred_network_mode_key");
	  	mButtonCdmaPreferredNetworkModeList.setOnPreferenceChangeListener(this);
	  	updateCmdaNettype();
	  	
//		if(PhoneGlobals.getInstance().getPhone(datasub).getPhoneType()  == PhoneConstants.PHONE_TYPE_CDMA) {		
//			dataPreferenceCategory.removePreference(mButtonPreferredNetworkModeList);
//		} else {
//			dataPreferenceCategory.removePreference(mButtonCdmaPreferredNetworkModeList);
//		}
	  	
        mEnhancedButton4glte = (Enhanced4GLteSwitchPreference) prefSet
				.findPreference("aurora_4g_enabled_key");
	    updateEnhanced4GLteSwitchPreference();
	    if(ImsManager.isVolteEnabledByPlatform(this)) {
            mEnhancedButton4glte.setOnPreferenceChangeListener(this);
        } else {
        	dataPreferenceCategory.removePreference(mEnhancedButton4glte);
        }
	
		mData = (AuroraListPreference)findPreference(KEY_DATA);		
		mData.setOnPreferenceChangeListener(this);
		
		mPrioritySub = (AuroraListPreference)findPreference(PRIORITY_SUB);
		mPrioritySub.setOnPreferenceChangeListener(this);
		
		mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
	}
		
	@Override
	public void onResume() {
		log("onCreate");
		super.onResume();

		// upon resumption from the sub-activity, make sure we re-enable the
		// preferences.
		mIsForeground = true;
		getPreferenceScreen().setEnabled(true);


		// aurora add liguangyu 20140424 for BUG #4517 start
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);
		registerReceiver(mDataReceiver, filter);
		if (isWifiConnect()) {
			mCheckDataConnectThread = new CheckDataConnectThread();
			mCheckDataConnectThread.start();
			mContinueChecking = true;
		}
		// aurora add liguangyu 20140424 for BUG #4517 end

		updateUiStateInternal();

		IntentFilter intentFilter = new IntentFilter(
				TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		registerReceiver(mUiReceiver, intentFilter);
		
		phoneList[getDataSub()].getPreferredNetworkType(mMobileHandler.obtainMessage(
				MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, getDataSub(), -1));
	
	}

	@Override
	public void onPause() {
		super.onPause();
		// aurora add liguangyu 20140424 for BUG #4517 start
		mIsForeground = false;
		unregisterReceiver(mDataReceiver);
		if (mCheckDataConnectThread != null) {
			mContinueChecking = false;
			mCheckDataConnectThread = null;
		}
		// aurora add liguangyu 20140424 for BUG #4517 end
		unregisterReceiver(mUiReceiver);
	}

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	// aurora add liguangyu 20140424 for BUG #4517 start
	private DataReceiver mDataReceiver = new DataReceiver();

	private class DataReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				if (isWifiConnect()) {
					if (mCheckDataConnectThread == null) {
						mCheckDataConnectThread = new CheckDataConnectThread();
						mCheckDataConnectThread.start();
						mContinueChecking = true;
					}
				} else {
					if (mCheckDataConnectThread != null) {
						mContinueChecking = false;
						mCheckDataConnectThread = null;
					}
				}
			} else if (action
					.equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE)) {
				setDataConnect();
			}
		}

	}

	private boolean isWifiConnect() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	private Handler mCheckHandler = new Handler() {
		public void handleMessage(Message msg) {
			setDataConnect();
		}
	};

	private void setDataConnect() {
		Log.i(LOG_TAG, "setDataConnect()");
		int dataSub = getDataSub();
		if (mTelephonyManager.getSimState(dataSub) != SIM_STATE_READY) {
			return;
		}
		mAuroraButtonDataEnabled.setChecked(mTelephonyManager.getDataEnabled());
	}

	private volatile boolean mContinueChecking;
	private Thread mCheckDataConnectThread;
	
	private class CheckDataConnectThread extends Thread {
		public void run() {
			while (mContinueChecking) {
				mCheckHandler.sendEmptyMessage(0);
				SystemClock.sleep(2000);
			}
		}
	}

	private Handler mHandler = new Handler() {
	    @Override
        public void handleMessage(Message msg) {
	    	if(msg.what == UPDATE) {
	    		updateUiStateInternal();
	    	} else if(msg.what == DATE_TIMEOUT) {
	            if(needDataSwitch) {	        
                		MSimMobileNetworkSettingsV2.this.onFinished(mData, false);
            			Log.d(LOG_TAG, "set data sub timeout");
            	        updateUiState();
            	        needDataSwitch = false;	   
	            }
	    	}
	    }
	};
	
	private final static int UPDATE = 1;
	private final static int DATE_TIMEOUT = 2;
	private void updateUiState() {
		mHandler.removeMessages(UPDATE);
		mHandler.sendEmptyMessageDelayed(UPDATE, 1000);
	}

	private void updateUiStateInternal() {
		Log.d(LOG_TAG, "updateUiStateInternal");		
		
		for (int i = 0; i < TelephonyManager.getDefault().getSimCount() ; i++) {
			String name = getSimName(i);
			if(!TextUtils.isEmpty(name)) {
				entries[i] = name;
			}
		}

		updateMultiSimEntriesForData();
		updatePrioritySubState();
		
		int dataSub = getDataSub();		
	  	AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory) findPreference("sim_data_support_key");
	  	
		boolean iscard1Enable = mTelephonyManager.getSimState(0) != TelephonyManager.SIM_STATE_ABSENT;
		boolean iscard2Enable = mTelephonyManager.getSimState(1) != TelephonyManager.SIM_STATE_ABSENT;
	  	
		if(PhoneGlobals.getInstance().getPhone(getDataSub()).getPhoneType()  == PhoneConstants.PHONE_TYPE_CDMA) {		
			dataPreferenceCategory.removePreference(mButtonPreferredNetworkModeList);
			dataPreferenceCategory.addPreference(mButtonCdmaPreferredNetworkModeList);
		  	updateCmdaNettype();
		} else {
			dataPreferenceCategory.removePreference(mButtonCdmaPreferredNetworkModeList);
			dataPreferenceCategory.addPreference(mButtonPreferredNetworkModeList);
			mButtonPreferredNetworkModeList.setValue(Integer
					.toString(AuroraNetworkUtils.convertNetworkMode(AuroraNetworkUtils.getPreferredNetworkMode(dataSub))));
			mButtonPreferredNetworkModeList.setSummary(getResources().getString(AuroraNetworkUtils.getPreferredNetworkStringId(dataSub)));
		}		
		
		mEnhancedButton4glte.setSubId(mSubscriptionManager.getDefaultDataPhoneId());
		if(mTelephonyManager.getSimState(dataSub) == TelephonyManager.SIM_STATE_ABSENT) {
			mButtonPreferredNetworkModeList.setEnabled(false);
			mButtonCdmaPreferredNetworkModeList.setEnabled(false);
			mEnhancedButton4glte.setEnabled(false);
		} else {
			mButtonPreferredNetworkModeList.setEnabled(true);
			mButtonCdmaPreferredNetworkModeList.setEnabled(true);
			mEnhancedButton4glte.setEnabled(true);
		}

		if (DeviceUtils.isSupportDualData()) {
			mAuroraButtonDataEnabled.setEnabled(iscard1Enable
					|| iscard2Enable);
		} else {
			mAuroraButtonDataEnabled.setEnabled(iscard1Enable);
		}

		mAuroraButtonDataEnabled.setChecked(mTelephonyManager.getDataEnabled());
		mData.setEnabled(iscard1Enable && iscard2Enable && AuroraPhoneUtils.isShowDoubleButton());
		mPrioritySub.setEnabled(iscard1Enable && iscard2Enable && AuroraPhoneUtils.isShowDoubleButton());
		
		int dualSimModeSetting = Settings.System.getInt(getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);
		
		SubscriptionInfo mSubInfoRecord = Utils.findRecordBySlotId(this,
				0);
		String number = "";
		if(mSubInfoRecord != null) {
			number = mSubInfoRecord.getNumber();
			Log.d(LOG_TAG, "updateUiState number = " + number);
			if(TextUtils.isEmpty(number)) {
				number = getPhoneNumber(mSubInfoRecord);
				Log.d(LOG_TAG, "updateUiState telenumber = " + number);
			}
			if(!TextUtils.isEmpty(number)) {
				mSim1.setSummary(number);
			}

			boolean isSimEnable = (dualSimModeSetting & 1) > 0;
			if(isSimEnable) {
							
				String name = mSubInfoRecord.getDisplayName().toString();
				if(TextUtils.isEmpty(name)) {
					String operator = AuroraPhoneUtils.getOperatorTitle(0);	
					name = operator;
				}
				
				if(!TextUtils.isEmpty(name)) {
					mSim1.setTitle(name);
				} else {
					mSim1.setTitle(R.string.sub1);
				}
			} else {
				mSim1.setTitle(R.string.no_active_sim_title);
			}
			mSim1.setEnabled(true);
		} else {
			mSim1.setTitle(R.string.no_sim_title);
			mSim1.setEnabled(false);
		}

		mSubInfoRecord = Utils.findRecordBySlotId(this,
				1);
		number = "";
		if(mSubInfoRecord != null) {
			number = mSubInfoRecord.getNumber();
			Log.d(LOG_TAG, "updateUiState number2 = " + number);
			if(TextUtils.isEmpty(number)) {
				number = getPhoneNumber(mSubInfoRecord);
				Log.d(LOG_TAG, "updateUiState telenumber2 = " + number);
			}
			if(!TextUtils.isEmpty(number)) {
				mSim2.setSummary(number);
			}		

			boolean isSimEnable = (dualSimModeSetting & 2) > 0;
			if(isSimEnable) {
				
				String name = mSubInfoRecord.getDisplayName().toString();
				if(TextUtils.isEmpty(name)) {
					String operator = AuroraPhoneUtils.getOperatorTitle(1);	
					name = operator;
				}
				
				if(!TextUtils.isEmpty(name)) {
					mSim2.setTitle(name);
				} else {
					mSim2.setTitle(R.string.sub2);
				}
			} else {
				mSim2.setTitle(R.string.no_active_sim_title);
			}
			mSim2.setEnabled(true);
		} else {
			mSim2.setTitle(R.string.no_sim_title);
			mSim2.setEnabled(false);
		}
		Log.d(LOG_TAG, "updateUiStateInternal end");		
	  		
	}
	
	// Returns the line1Number. Line1number should always be read from TelephonyManager since it can
    // be overridden for display purposes.
    private String getPhoneNumber(SubscriptionInfo info) {
        final TelephonyManager tm =
            (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1NumberForSubscriber(info.getSubscriptionId());
    }

	private BroadcastReceiver mUiReceiver = new UiBroadcastReceiver();
	private Boolean mIsFirstRecieve = true;

	private class UiBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v(LOG_TAG, "Action intent recieved:" + action);
			if(mIsFirstRecieve) {
				mIsFirstRecieve = false;
				return;
			}
			// gets the subscription information ( "0" or "1")
			if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
				  String simStatus = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                  if (simStatus.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
                	  finish();
                  } 
			}
		}
	}

	public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
		String status;
		if (preference == mAuroraButtonDataEnabled) {
			boolean isChecked = (Boolean) objValue;
			handleAuroraDataSwitch(isChecked);
		} else if (preference == mButtonPreferredNetworkModeList) {
			int buttonNetworkMode = Integer.valueOf((String) objValue)
					.intValue();
			handleSetNetType(buttonNetworkMode);
		} else if(preference == mButtonCdmaPreferredNetworkModeList) {
			int buttonNetworkMode = Integer.valueOf((String) objValue)
					.intValue();
			handleSetCdmaNetType(buttonNetworkMode);
		} else if(preference == mEnhancedButton4glte) {
			boolean isChecked = (Boolean) objValue;
			ImsManager.setEnhanced4gLteModeSetting(this, isChecked);
		} else if(preference == mData) {
			int slot = Integer.valueOf((String) objValue)
					.intValue();
			handlePreferDataSub(slot);
		} else if(preference == mPrioritySub) {
			int slot = Integer.valueOf((String) objValue)
					.intValue();
			handlePreferSub(slot);
		}

		return true;
	}


	protected void updateMultiSimEntriesForData() {
		int dataSub = getDataSub();
		Log.d(LOG_TAG, "updateDataSummary: Data Subscription : = " + dataSub);
		if(dataSub != 1 && dataSub != 0){
			dataSub = 2;
		}
		CharSequence[] dataitems = new CharSequence[2];
		dataitems[0] = entries[0];
		dataitems[1] = entries[1];
		CharSequence[] datavalues = new CharSequence[2];
		datavalues[0] = String.valueOf(0);
		datavalues[1] = String.valueOf(1);
		mData.setEntries(dataitems);
		mData.setEntryValues(datavalues);
		mData.setSummary(entries[dataSub]);
		mData.setValue(String.valueOf(dataSub));
	}

	private void updatePrioritySubState() {
		int priorityValue = mSubscriptionManager.getDefaultSmsPhoneId();
	
		if(priorityValue != 1 && priorityValue != 0){
			priorityValue = 2;
		}
		CharSequence[] values = new CharSequence[3];
		values[0] = String.valueOf(0);
		values[1] = String.valueOf(1);
		values[2] = String.valueOf(2);
		mPrioritySub.setEntries(entries);
	    mPrioritySub.setEntryValues(values); 
		mPrioritySub.setSummary(entries[priorityValue]);
		mPrioritySub.setValue(String.valueOf(priorityValue));
	}

	protected boolean mIsForeground = false;

	private void handleAuroraDataSwitch(boolean isChecked) {
		int subId = mSubscriptionManager.getDefaultDataSubId();
		boolean isEnable = mTelephonyManager.getDataEnabled(subId);
		if (isChecked == isEnable) {
			if (DBG)
				log("onPreferenceChange: preference = mButtonDataEnabled. return");
			return;
		}
		if (DBG)
			log("onPreferenceChange: preference == mButtonDataEnabled. sub = "
					+ subId + " isChecked = " + isChecked);
		mTelephonyManager.setDataEnabled(subId, isChecked);
	}

	private HomeRecevier mHomeRecevier = new HomeRecevier();

	class HomeRecevier extends BroadcastReceiver {

		final String SYSTEM_DIALOG_REASON_KEY = "reason";

		final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";

		final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			log("home onReceive action =" + action);
			if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reason != null) {
					log("home onReceive reason =" + reason);
					if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
						// press home
						finish();
					} else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
						// long press home
					}
				}
			}
		}
	}

	protected void onDestroy() {
		Log.d(LOG_TAG, "onDestroy::");	
		super.onDestroy();
		mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
		unregisterReceiver(mHomeRecevier);
        unregisterReceiver(mSubReceiver);
        mStateHandler.removePhoneServiceSateListener();
        mHandler.removeMessages(DATE_TIMEOUT);
        mHandler.removeMessages(UPDATE);
	}

	private void handlePreferSub(int prioritySubIndex) {
		Log.d(LOG_TAG, "handlePreferSub::::  prioritySubIndex ="
				+ prioritySubIndex);		
		if(prioritySubIndex == 0 || prioritySubIndex ==1) {
			mSubscriptionManager.setDefaultVoiceSubId(mSubscriptionManager
					.getSubIdUsingPhoneId(prioritySubIndex));
			mSubscriptionManager.setDefaultSmsSubId(mSubscriptionManager
					.getSubIdUsingPhoneId(prioritySubIndex));
		} else {
			mSubscriptionManager.setDefaultVoiceSubId(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
			mSubscriptionManager.setDefaultSmsSubId(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
		}
		updatePrioritySubState();
	}

	private SubscriptionManager mSubscriptionManager;
	private List<SubscriptionInfo> mSubInfoList;
	private TelephonyManager mTelephonyManager;

	private void handlePreferDataSub(int prioritySubIndex) {
		Log.d(LOG_TAG, "handlePreferDataSub::::  prioritySubIndex ="
				+ prioritySubIndex);
		
		if(prioritySubIndex == getDataSub()) {
			return;
		} 
		
		mIsEnableDataWhenSwitchDone = mTelephonyManager.getDataEnabled();
		needDataSwitch = true;
		mSubscriptionManager.setDefaultDataSubId(mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex));
		this.onStarted(mData, false);
        

        android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
				android.provider.Settings.Global.MOBILE_DATA + mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex),
				mIsEnableDataWhenSwitchDone ? 1 : 0);
        
        mHandler.sendEmptyMessageDelayed(DATE_TIMEOUT, 60* 1000);
        
//		String status = getResources().getString(R.string.data_switch_started);
//		Toast toast = Toast.makeText(this, status, Toast.LENGTH_LONG);
//		toast.show();
	}
	
    private boolean mIsAirplaneModeOn = false;
    private IntentFilter mIntentFilter;
    
    
    private void initIntentFilter() {
        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        // For PhoneAccount
        mIntentFilter.addAction(TelecomManagerEx.ACTION_DEFAULT_ACCOUNT_CHANGED);
        mIntentFilter.addAction(TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED);
        // For SIM Switch
        mIntentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        mIntentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
    }
    
    
	   // Receiver to handle different actions
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "mSubReceiver action = " + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                handleAirplaneModeBroadcast(intent);
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
            	updateUiState();
            } else if (isPhoneAccountAction(action)) {
            	updateUiState();
            } 
        }
    };
    
    private void handleAirplaneModeBroadcast(Intent intent) {
        mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.d(LOG_TAG, "air plane mode is = " + mIsAirplaneModeOn);
        updateUiState();
    }
    
    private boolean isPhoneAccountAction(String action) {
        return action.equals(TelecomManagerEx.ACTION_DEFAULT_ACCOUNT_CHANGED) ||
                action.equals(TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED);
    }

	private int getDataSub() {
		int result = mSubscriptionManager.getDefaultDataPhoneId();
		return result > -1 ? result : 0;
	}
	
	private boolean needDataSwitch = false;
	private boolean mIsEnableDataWhenSwitchDone = false;
    public void onServiceStateChanged(ServiceState state, int subId) {
        Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged: subId: " + subId
                + ", state: " + state);
        Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged: needDataSwitch: " + needDataSwitch
                + ", getDefaultDataSubId: " + mSubscriptionManager.getDefaultDataSubId());
        if(needDataSwitch) {
        	if(subId == mSubscriptionManager.getDefaultDataSubId()) {
        		if(state.getState() == ServiceState.STATE_IN_SERVICE) {
            		this.onFinished(mData, false);
        			Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged done");
           	    	int slot = mSubscriptionManager.getDefaultDataPhoneId();
    	        	if (slot > -1 && PhoneGlobals.getInstance(). getPhone(slot).getPhoneType()  != PhoneConstants.PHONE_TYPE_CDMA) {
    	    			AuroraNetworkUtils.setPreferredNetworkMode(Constants.NETWORK_MODE_LTE_GSM_WCDMA, slot);
    	    			PhoneGlobals.getInstance().getPhone(slot).setPreferredNetworkType(Constants.NETWORK_MODE_LTE_GSM_WCDMA, null);
    	        	}
//        			mTelephonyManager.setDataEnabled(subId, mIsEnableDataWhenSwitchDone);
        	        updateUiState();
        	        needDataSwitch = false;
        	        mHandler.removeMessages(DATE_TIMEOUT);
        		}
        	}
        }
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
				int modemNetworkMode = ((int[]) ar.result)[0];

				if (DBG) {
					log("handleGetPreferredNetworkTypeResponse: modemNetworkMode = "
							+ modemNetworkMode + " sub =" + sub);
				}

				int settingsNetworkMode = AuroraNetworkUtils
						.getPreferredNetworkMode(sub);
				if (DBG) {
					log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = "
							+ settingsNetworkMode);
				}

//				// check that modemNetworkMode is from an accepted value
//				if ((modemNetworkMode >= Phone.NT_MODE_WCDMA_PREF)
//						&& (modemNetworkMode <= 22)) {
//					if (DBG) {
//						log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = "
//								+ modemNetworkMode);
//					}
//
//					// check changes in modemNetworkMode and updates
//					// settingsNetworkMode
//					if (modemNetworkMode != settingsNetworkMode) {
//						if (DBG) {
//							log("handleGetPreferredNetworkTypeResponse: if 2: "
//									+ "modemNetworkMode != settingsNetworkMode");
//						}
//
//						settingsNetworkMode = modemNetworkMode;
//
//						if (DBG) {
//							log("handleGetPreferredNetworkTypeResponse: if 2: "
//									+ "settingsNetworkMode = "
//									+ settingsNetworkMode);
//						}
//
//						// changes the Settings.System accordingly to
//						// modemNetworkMode
//						AuroraNetworkUtils.setPreferredNetworkMode(
//								settingsNetworkMode, sub);
//					}
				
				  //check that modemNetworkMode is from an accepted value
                if (modemNetworkMode == Phone.NT_MODE_WCDMA_PREF ||
                        modemNetworkMode == Phone.NT_MODE_GSM_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_WCDMA_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_GSM_UMTS ||
                        modemNetworkMode == Phone.NT_MODE_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_CDMA_NO_EVDO ||
                        modemNetworkMode == Phone.NT_MODE_EVDO_NO_CDMA ||
                        modemNetworkMode == Phone.NT_MODE_GLOBAL ||
                        modemNetworkMode == Phone.NT_MODE_LTE_CDMA_AND_EVDO ||
                        modemNetworkMode == Phone.NT_MODE_LTE_GSM_WCDMA ||
                        modemNetworkMode == Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA ||
                        modemNetworkMode == Phone.NT_MODE_LTE_ONLY ||
                        modemNetworkMode == Phone.NT_MODE_LTE_WCDMA) {
                    if (DBG) {
                        log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = " +
                                modemNetworkMode);
                    }

                    // Framework's Phone.NT_MODE_GSM_UMTS is same as app's
                    // NT_MODE_WCDMA_PREF, this is related with feature option
                    // MTK_RAT_WCDMA_PREFERRED. In app side, we should change
                    // the setting system's value to NT_MODE_WCDMA_PREF, and keep
                    // sync with Modem's value.
                    if (modemNetworkMode == Phone.NT_MODE_GSM_UMTS
                            && TelephonyUtils.isWCDMAPreferredSupport()) {
                        modemNetworkMode = Phone.NT_MODE_WCDMA_PREF;
                        if (settingsNetworkMode != Phone.NT_MODE_WCDMA_PREF) {
                            settingsNetworkMode = Phone.NT_MODE_WCDMA_PREF;
//                            android.provider.Settings.Global.putInt(
//                                    mPhone.getContext().getContentResolver(),
//                                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
//                                    settingsNetworkMode);
    						AuroraNetworkUtils.setPreferredNetworkMode(settingsNetworkMode, sub);
                            if (DBG) {
                                log("handleGetPreferredNetworkTypeResponse: settingNetworkMode");
                            }
                        }
                    } else {
                        //check changes in modemNetworkMode
                        if (modemNetworkMode != settingsNetworkMode) {
                            if (DBG) {
                                log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                        "modemNetworkMode != settingsNetworkMode");
                            }

                            settingsNetworkMode = modemNetworkMode;

                            if (DBG) { log("handleGetPreferredNetworkTypeResponse: if 2: " +
                                    "settingsNetworkMode = " + settingsNetworkMode);
                            }

                        }
                    }

//					AuroraNetworkUtils.UpdatePreferredNetworkModeSummary(
//							mButtonPreferredNetworkModeList,						
//							modemNetworkMode);
					// changes the mButtonPreferredNetworkMode accordingly to
					// modemNetworkMode
					mButtonPreferredNetworkModeList.setValue(Integer
							.toString(AuroraNetworkUtils.convertNetworkMode(modemNetworkMode)));
					mButtonPreferredNetworkModeList.setSummary(
							getResources().getString(
									AuroraNetworkUtils
											.getPreferredNetworkStringId(sub)));

				} else {
					if (DBG)
						log("handleGetPreferredNetworkTypeResponse: else: reset to default");
					resetNetworkModeToDefault(sub);
				}
				// Update '2GOnly checkbox' based on recent preferred network
				// type selection.
				// Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
			}
		}

		private void handleSetPreferredNetworkTypeResponse(Message msg) {
			AsyncResult ar = (AsyncResult) msg.obj;

			int sub = msg.arg1;

			if (ar.exception == null) {
				log("handleSetPreferredNetworkTypeResponse normal sub = " + sub);
				mButtonPreferredNetworkModeList.setSummary(
						getResources().getString(
								AuroraNetworkUtils
										.getPreferredNetworkStringId(sub)));
			} else {
				log("handleSetPreferredNetworkTypeResponse exception sub = " + sub);
			}
			phoneList[sub].getPreferredNetworkType(obtainMessage(
					MESSAGE_GET_PREFERRED_NETWORK_TYPE, sub, -1));
		}

		private void resetNetworkModeToDefault(int sub) {
			log("resetNetworkModeToDefault sub = " + sub);
			// set the Settings.System
				mButtonPreferredNetworkModeList.setValue(Integer
						.toString(preferredNetworkMode));				
		
			AuroraNetworkUtils.setPreferredNetworkMode(preferredNetworkMode, sub);
			
			mButtonPreferredNetworkModeList.setSummary(
					getResources().getString(
							AuroraNetworkUtils
									.getPreferredNetworkStringId(sub)));
			// Set the Modem
			phoneList[sub].setPreferredNetworkType(preferredNetworkMode, this
					.obtainMessage(
							MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub,
							-1));
		}
	
    }
    
	private void handleSetNetType(int buttonNetworkMode) {
		log("handleSetNetType buttonNetworkMode = " + buttonNetworkMode);
		mButtonPreferredNetworkModeList.setValue(buttonNetworkMode + "");
		int sub = getDataSub();
		int settingsNetworkMode = AuroraNetworkUtils
				.getPreferredNetworkMode(sub);
		if (buttonNetworkMode != settingsNetworkMode) {
			int modemNetworkMode;
			if (buttonNetworkMode >= 0 && buttonNetworkMode <= 22) {
				modemNetworkMode = buttonNetworkMode;
			} else {
				log("Invalid Network Mode (" + buttonNetworkMode
						+ ") chosen. Ignore.");
				return;
			}
			AuroraNetworkUtils.UpdatePreferredNetworkModeSummary(
					 mButtonPreferredNetworkModeList, buttonNetworkMode);

			AuroraNetworkUtils.setPreferredNetworkMode(modemNetworkMode, sub);
			phoneList[sub].setPreferredNetworkType(modemNetworkMode,
					mMobileHandler.obtainMessage(
							MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub,
							-1));

		}
	}
	
	private void handleSetCdmaNetType(int buttonNetworkMode) {
		log("handleSetCdmaNetType buttonNetworkMode = " + buttonNetworkMode);
		mButtonCdmaPreferredNetworkModeList.setValue(buttonNetworkMode + "");
		int sub = getDataSub();
		int subid = AuroraSubUtils.getSubIdbySlot(PhoneGlobals.getInstance(), sub);
       Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
                AuroraPhoneUtils.getCdmaRatModeKey(subid),  buttonNetworkMode == (SvlteRatController.RAT_MODE_SVLTE_2G
		                 | SvlteRatController.RAT_MODE_SVLTE_3G) ? TelephonyManagerEx.SVLTE_RAT_MODE_3G :  TelephonyManagerEx.SVLTE_RAT_MODE_4G);
        LteDcPhoneProxy lteDcPhoneProxy = (LteDcPhoneProxy) (PhoneGlobals.getInstance().getPhone(sub));
        lteDcPhoneProxy.getSvlteRatController().setRadioTechnology(buttonNetworkMode, null);
	}
	
	private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
		@Override
		public void onSubscriptionsChanged() {
			Log.d(LOG_TAG, "onSubscriptionsChanged start");
	        /// M: add for hot swap @{
            if (TelephonyUtils.isHotSwapHanppened(
            		mSubInfoList, PhoneUtils.getActiveSubInfoList())) {
                log("onSubscriptionsChanged:hot swap hanppened");
                dissmissDialog(mButtonPreferredNetworkModeList);
                dissmissDialog(mButtonCdmaPreferredNetworkModeList);
                finish();
                return;
            }
            /// @}
			updateUiState();       
		}
	};
	
	private void updateCmdaNettype() {
		int datasub = getDataSub();
		if(PhoneGlobals.getInstance().getPhone(datasub).getPhoneType()  == PhoneConstants.PHONE_TYPE_CDMA) {
			int subid = AuroraSubUtils.getSubIdbySlot(PhoneGlobals.getInstance(), datasub);
		  	int cdmaType = Settings.Global.getInt(getContentResolver(),
	                AuroraPhoneUtils.getCdmaRatModeKey(subid),  TelephonyManagerEx.SVLTE_RAT_MODE_4G);

		  	String value = cdmaType == TelephonyManagerEx.SVLTE_RAT_MODE_3G ? String.valueOf(SvlteRatController.RAT_MODE_SVLTE_2G
		                 | SvlteRatController.RAT_MODE_SVLTE_3G) : String.valueOf(SvlteRatController.RAT_MODE_SVLTE_2G
		                 | SvlteRatController.RAT_MODE_SVLTE_3G | SvlteRatController.RAT_MODE_SVLTE_4G);
		  	mButtonCdmaPreferredNetworkModeList.setValue(value);
		  	mButtonCdmaPreferredNetworkModeList.setSummary(getResources().getString(AuroraNetworkUtils.getCdmaPreferredNetworkStringId(Integer.valueOf(value))));
		}
	}
	
    private static final String PROPERTY_VOLTE_ENALBE = "persist.mtk.volte.enable";
	  /**
     * Update the subId in mEnhancedButton4glte.
     */
    private void updateEnhanced4GLteSwitchPreference() {
//            if (isCapabilityPhone(mPhone) && findPreference(BUTTON_4G_LTE_KEY) == null) {
//                getPreferenceScreen().addPreference(mEnhancedButton4glte);
//            } else if (!isCapabilityPhone(mPhone) && findPreference(BUTTON_4G_LTE_KEY) != null) {
//                getPreferenceScreen().removePreference(mEnhancedButton4glte);
//            }
 
            int isChecked = Settings.Global.getInt(getContentResolver(),
                    Settings.Global.IMS_SWITCH, 0);
            int oldVoLTEValue = SystemProperties.getInt(PROPERTY_VOLTE_ENALBE, 0);   
            if(isChecked != oldVoLTEValue) {
            	SystemProperties.set(PROPERTY_VOLTE_ENALBE, String.valueOf(isChecked)); 
            }
            log("[updateEnhanced4GLteSwitchPreference] + isChecked = " + isChecked);
            mEnhancedButton4glte.setChecked(isChecked == 1);
            
        
    }
    
    private void dissmissDialog(AuroraListPreference preference) {
    	Dialog dialog = null;
        if (preference != null) {
            dialog = preference.getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }
    
    private String  getSimName(int i) {
    	if(i != 0 && i != 1) {
    		return null;
    	}
    	SubscriptionInfo mSubInfoRecord = Utils.findRecordBySlotId(this,
				i);
    	if(mSubInfoRecord == null) {
    		return null;
    	}
		String name = mSubInfoRecord.getDisplayName().toString();
		if(TextUtils.isEmpty(name)) {
		  	String operator = AuroraPhoneUtils.getOperatorTitle(i);
			name = operator;
		}
		return name;
    	
    }
}
