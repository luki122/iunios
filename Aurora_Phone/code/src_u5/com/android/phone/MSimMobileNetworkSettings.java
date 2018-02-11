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
public class MSimMobileNetworkSettings extends AuroraPreferenceActivity
		implements AuroraPreference.OnPreferenceChangeListener {

	// debug data
	private static final String LOG_TAG = "MSimMobileNetworkSettings";
	private static final boolean DBG = true;

	private static final String KEY_DATA = "data";
	private static final String PRIORITY_SUB = "priority_subscription";

	private AuroraPreference mData;
	private AuroraPreference mPrioritySub;
	private AuroraSwitchPreference mAuroraButtonDataEnabled;

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

	private Phone mPhone;
	private Phone mPhone2;
	
	private PhoneServiceStateHandler mStateHandler;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.msim_network_setting);

		// mPhone =
		// ((MSimPhoneGlobals)PhoneGlobals.getInstance()).getDefaultPhone();
		mPhone = PhoneGlobals.getInstance().getPhone(0);
		mPhone2 = PhoneGlobals.getInstance().getPhone(1);
		// get UI object references
		AuroraPreferenceScreen prefSet = getPreferenceScreen();

		mData = findPreference(KEY_DATA);
		// mData.setOnPreferenceChangeListener(this);
		mPrioritySub = findPreference(PRIORITY_SUB);
		// mPrioritySub.setOnPreferenceChangeListener(this);
		mAuroraButtonDataEnabled = (AuroraSwitchPreference) prefSet
				.findPreference("aurora_data_enabled_key");
		mAuroraButtonDataEnabled.setOnPreferenceChangeListener(this);
		int numPhones = TelephonyManager.getDefault().getSimCount();
		// Create and Intialize the strings required for MultiSIM
		// Dynamic creation of entries instead of using static array vlues.
		// entries are Subscription1, Subscription2, Subscription3 ....
		// EntryValues are 0, 1 ,2 ....
		// Summaries are Subscription1, Subscription2, Subscription3 ....
		entries = new CharSequence[numPhones];
		entryValues = new CharSequence[numPhones];
		summaries = new CharSequence[numPhones];
		entriesPrompt = new CharSequence[numPhones + 1];
		entryValuesPrompt = new CharSequence[numPhones + 1];
		summariesPrompt = new CharSequence[numPhones + 1];
		CharSequence[] subString = getResources().getTextArray(
				R.array.multi_sim_entries);
		int i = 0;
		for (i = 0; i < numPhones; i++) {
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

		AuroraPreference dataSummary = prefSet
				.findPreference("sim_data_support_summary_key");
		dataSummary.setEnabled(false);

		if (!DeviceUtils.isSupportDualData()) {
			AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory) findPreference("sim_data_support_key");
			dataPreferenceCategory.removePreference(mData);
			dataPreferenceCategory.removePreference(dataSummary);
		}

		IntentFilter homeIf = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mHomeRecevier, homeIf);

		mSubscriptionManager = SubscriptionManager.from(this);
		mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
		mTelephonyManager = TelephonyManager.from(this);
		
		AuroraPreferenceCategory manageSub1 = (AuroraPreferenceCategory) prefSet
				.findPreference("sim1_category_key");
		AuroraPreferenceCategory manageSub2 = (AuroraPreferenceCategory) prefSet
				.findPreference("sim2_category_key");

		if (manageSub1 != null) {
			prefSet.removePreference(manageSub1);
		}
		if (manageSub2 != null) {
			prefSet.removePreference(manageSub2);
		}
		
	    initIntentFilter();
        registerReceiver(mSubReceiver, mIntentFilter);
        
//   	    mStateHandler = new PhoneServiceStateHandler(this);
//        mStateHandler.addPhoneServiceStateListener(this);
	}
	
	/**
	 * Invoked on each preference click in this hierarchy, overrides
	 * PreferenceActivity's implementation. Used to make sure we track the
	 * preference click events.
	 */
	@Override
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		try {
			removeMenuById(AuroraMenu.FIRST);
			removeMenuById(AuroraMenu.FIRST + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (preference == mData) {
			String menuSlot0 = entries[0].toString();
			String menuSlot1 = entries[1].toString();
			addMenu(AuroraMenu.FIRST, menuSlot1, new OnMenuItemClickLisener() {
				public void onItemClick(View menu) {
					handlePreferDataSub(1);
				}
			});
			addMenu(AuroraMenu.FIRST + 1, menuSlot0,
					new OnMenuItemClickLisener() {
						public void onItemClick(View menu) {
							handlePreferDataSub(0);
						}
					});
			showCustomMenu();
			return true;
		} else if (preference == mPrioritySub) {
			String menuSlot0 = entries[0].toString();
			String menuSlot1 = entries[1].toString();
			addMenu(AuroraMenu.FIRST, menuSlot1, new OnMenuItemClickLisener() {
				public void onItemClick(View menu) {
					handlePreferSub(1);
				}
			});
			addMenu(AuroraMenu.FIRST + 1, menuSlot0,
					new OnMenuItemClickLisener() {
						public void onItemClick(View menu) {
							handlePreferSub(0);
						}
					});
			showCustomMenu();
			return true;
		} else {
			// if the button is anything but the simple toggle preference,
			// we'll need to disable all preferences to reject all click
			// events until the sub-activity's UI comes up.
			// preferenceScreen.setEnabled(false);
			// Let the intents be launched by the Preference manager
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// upon resumption from the sub-activity, make sure we re-enable the
		// preferences.
		mIsForeground = true;
		getPreferenceScreen().setEnabled(true);
		updateMultiSimEntriesForData();
		updatePrioritySubState();

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());

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

		IntentFilter recordFilter = new IntentFilter(
				AURORA_STATE_CHANGED_ACTION);
		registerReceiver(mPhoneStateReceiver, recordFilter);
		updateUiState();

		IntentFilter intentFilter = new IntentFilter(
				TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		registerReceiver(mUiReceiver, intentFilter);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// aurora add liguangyu 20140424 for BUG #4517 start
		mIsForeground = false;
		unregisterReceiver(mDataReceiver);
		if (mCheckDataConnectThread != null) {
			mContinueChecking = false;
			mCheckDataConnectThread = null;
		}
		// aurora add liguangyu 20140424 for BUG #4517 end
		unregisterReceiver(mPhoneStateReceiver);
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

	private static final String AURORA_STATE_CHANGED_ACTION = "PhoneServiceStateChanged";
	private final PhoneStateReceiver mPhoneStateReceiver = new PhoneStateReceiver();
	private Handler mHandler = new Handler();

	private class PhoneStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					updateUiState();
				}
			}, 1500);
		}
	}

	private void updateUiState() {

		boolean iscard1Enable = mTelephonyManager.getSimState(0) != TelephonyManager.SIM_STATE_ABSENT;
		boolean iscard2Enable = mTelephonyManager.getSimState(1) != TelephonyManager.SIM_STATE_ABSENT;

		if (DeviceUtils.isSupportDualData()) {
			mAuroraButtonDataEnabled.setEnabled(iscard1Enable
					|| iscard2Enable);
		} else {
			mAuroraButtonDataEnabled.setEnabled(iscard1Enable);
		}

		mAuroraButtonDataEnabled.setChecked(mTelephonyManager.getDataEnabled());
		mData.setEnabled(iscard1Enable && iscard2Enable);
		mPrioritySub.setEnabled(iscard1Enable && iscard2Enable);
	}

	private BroadcastReceiver mUiReceiver = new UiBroadcastReceiver();

	private class UiBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v(LOG_TAG, "Action intent recieved:" + action);
			// gets the subscription information ( "0" or "1")
			if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
				updateUiState();
			}
		}
	}

	public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
		String status;
		if (preference == mAuroraButtonDataEnabled) {
			boolean isChecked = (Boolean) objValue;
			handleAuroraDataSwitch(isChecked);
		}

		return true;
	}


	protected void updateMultiSimEntriesForData() {
		int dataSub = getDataSub();
		Log.d(LOG_TAG, "updateDataSummary: Data Subscription : = " + dataSub);
		mData.auroraSetArrowText(summaries[dataSub]);
	}

	private void updatePrioritySubState() {
		int priorityValue = mSubscriptionManager.getDefaultSmsPhoneId();
		if ( priorityValue < 0) {
			priorityValue = 0;
		}
		mPrioritySub.auroraSetArrowText(summaries[priorityValue]);
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
		super.onDestroy();
		unregisterReceiver(mHomeRecevier);
        unregisterReceiver(mSubReceiver);
//        mStateHandler.removePhoneServiceSateListener();
//        if(needDataSwitch) {
//        	((I2PhoneGlobals)PhoneGlobals.getInstance()).setDataEnableAfterSwitch(mIsEnableDataWhenSwitchDone);
//        }
	}

	private void handlePreferSub(int prioritySubIndex) {
		Log.d(LOG_TAG, "handlePreferSub::::  prioritySubIndex ="
				+ prioritySubIndex);		
		mSubscriptionManager.setDefaultVoiceSubId(mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex));
		mSubscriptionManager.setDefaultSmsSubId(mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex));
		updatePrioritySubState();
	}

	private SubscriptionManager mSubscriptionManager;
	private List<SubscriptionInfo> mSubInfoList;
	private TelephonyManager mTelephonyManager;

	private void handlePreferDataSub(int prioritySubIndex) {
		Log.d(LOG_TAG, "handlePreferDataSub::::  prioritySubIndex ="
				+ prioritySubIndex);
		mIsEnableDataWhenSwitchDone = mTelephonyManager.getDataEnabled();
//		needDataSwitch = true;
		mSubscriptionManager.setDefaultDataSubId(mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex));
        

        android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
				android.provider.Settings.Global.MOBILE_DATA + mSubscriptionManager
				.getSubIdUsingPhoneId(prioritySubIndex),
				mIsEnableDataWhenSwitchDone ? 1 : 0);
        
		String status = getResources().getString(R.string.data_switch_started);
		Toast toast = Toast.makeText(this, status, Toast.LENGTH_LONG);
		toast.show();
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
        if(needDataSwitch) {
        	if(subId == mSubscriptionManager.getDefaultDataSubId()) {
        		if(state.getState() == ServiceState.STATE_IN_SERVICE) {
        			Log.d(LOG_TAG, "PhoneStateListener:onServiceStateChanged done");
        			mTelephonyManager.setDataEnabled(subId, mIsEnableDataWhenSwitchDone);
        	        updateUiState();
        	        needDataSwitch = false;
        		}
        	}
        }
    }
}
