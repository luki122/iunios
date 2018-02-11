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

package com.aurora.callsetting;

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
import android.os.Messenger;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import aurora.preference.*;
import aurora.app.*;

import android.view.KeyEvent;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static com.aurora.callsetting.AuroraMSimConstants.SUBSCRIPTION_KEY;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

public class SetSubscription extends AuroraPreferenceActivity implements
		AuroraPreference.OnPreferenceChangeListener , PhoneServiceStateHandler.Listener {
	private static final String TAG = "AuroraSetSubscription";

	protected boolean mIsForeground = false;
	  private SimHotSwapHandler mSimHotSwapHandler;
	private PhoneServiceStateHandler mStateHandler;

	private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();

	private Phone[] phoneList;
	private RadioPowerPreference[] mSimEnable;
	private SharedPreferences mSharedPreferences;
	private AuroraPreferenceCategory[] manageSub;
	private AuroraSwitchPreference[] mButtonDataRoam;
	private AuroraSwitchPreference mButtonDataRoam2;
	private AuroraListPreference[] mButtonPreferredNetworkModeList;
	private AuroraPreference[] mButtonPreferredNetworkMode;
	private AuroraPreferenceScreen[] mButtonAPNExpand;
	private AuroraPreferenceScreen[] mButtonOperatorSelectionExpand;

	private static final String BUTTON_ROAMING_KEY = "button_roaming_key_";
	private static final String BUTTON_PREFERED_NETWORK_MODE_LIST = "preferred_network_mode_list_key_";
	private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key_";
	private static final String BUTTON_APN_EXPAND_KEY = "button_apn_key_";
	private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY = "button_carrier_sel_key_";

	private SubscriptionManager mSubscriptionManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.i2_sim_management);
		getAuroraActionBar().setTitle(R.string.callind_multi_sim_card);

		mSubscriptionManager = SubscriptionManager.from(this);
		mSharedPreferences = getSharedPreferences("sim_enable_state",
				Context.MODE_PRIVATE);
		AuroraPreferenceScreen prefSet = getPreferenceScreen();
		mMobileHandler = new MyHandler();

		phoneList = new Phone[mNumPhones];
		mIsSimEnable = new boolean[mNumPhones];
		mSimEnable = new RadioPowerPreference[mNumPhones];	
		manageSub = new AuroraPreferenceCategory[mNumPhones];
		mButtonDataRoam = new AuroraSwitchPreference[mNumPhones];
		mButtonPreferredNetworkModeList = new AuroraListPreference[mNumPhones];
		mButtonPreferredNetworkMode = new AuroraPreference[mNumPhones];
		mButtonAPNExpand = new AuroraPreferenceScreen[mNumPhones];
		mButtonOperatorSelectionExpand = new AuroraPreferenceScreen[mNumPhones];

		for (int i = 0; i < mNumPhones; ++i) {
			phoneList[i] = PhoneGlobals.getInstance().getPhone(i);
			final SubscriptionInfo sir = Utils.findRecordBySlotId(
					this, i);
			mSimEnable[i] = (RadioPowerPreference) findPreference("sim_enable_"
					+ i);
			// mSimEnable[i].setOnPreferenceChangeListener(this);
			bindWithRadioPowerManager(mSimEnable[i], sir);
			manageSub[i] = (AuroraPreferenceCategory) prefSet
					.findPreference("sim_category_key_" + i);
			mButtonDataRoam[i] = (AuroraSwitchPreference) prefSet
					.findPreference(BUTTON_ROAMING_KEY + i);
			mButtonDataRoam[i].setOnPreferenceChangeListener(this);
			mButtonPreferredNetworkModeList[i] = (AuroraListPreference) prefSet
					.findPreference(BUTTON_PREFERED_NETWORK_MODE_LIST + i);
			mButtonPreferredNetworkModeList[i]
					.setOnPreferenceChangeListener(this);
			mButtonPreferredNetworkModeList[i].setValue(Integer
					.toString(AuroraNetworkUtils.convertNetworkMode(AuroraNetworkUtils.getPreferredNetworkMode(i))));
			mButtonPreferredNetworkMode[i] = (AuroraPreference) prefSet
					.findPreference(BUTTON_PREFERED_NETWORK_MODE + i);
			mButtonPreferredNetworkMode[i].auroraSetArrowText(
					getResources().getString(
							AuroraNetworkUtils.getPreferredNetworkStringId(i)),
					true);
			Intent intent = mButtonPreferredNetworkMode[i].getIntent();
			intent.putExtra(SUBSCRIPTION_KEY, i);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

			mButtonAPNExpand[i] = (AuroraPreferenceScreen) prefSet
					.findPreference(BUTTON_APN_EXPAND_KEY + i);
			mButtonAPNExpand[i].getIntent().putExtra(SUBSCRIPTION_KEY, i);
			mButtonAPNExpand[i].getIntent().putExtra("sub_id", AuroraSubUtils.getSubIdbySlot(this, i));
			mButtonAPNExpand[i].getIntent().addFlags(
					Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			mButtonAPNExpand[i].getIntent().addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK);

			mButtonOperatorSelectionExpand[i] = (AuroraPreferenceScreen) prefSet
					.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY + i);
			mButtonOperatorSelectionExpand[i].getIntent().putExtra(
					SUBSCRIPTION_KEY, i);
			mButtonOperatorSelectionExpand[i].getIntent().addFlags(
					Intent.FLAG_ACTIVITY_NO_HISTORY);
			mButtonOperatorSelectionExpand[i].auroraSetArrowText(
					getTitleFromOperatorNumber(getOperator(i)), false);

			if (DeviceUtils.isIUNI()) {
				// mButtonPreferredNetworkMode.setEntries(R.array.cm_preferred_network_mode_choices);
				// mButtonPreferredNetworkMode.setEntryValues(R.array.cm_preferred_network_mode_values);
				if (mButtonPreferredNetworkModeList[i] != null) {
					manageSub[i]
							.removePreference(mButtonPreferredNetworkModeList[i]);
				}
			} else {
				if (mButtonPreferredNetworkMode[i] != null) {
					manageSub[i]
							.removePreference(mButtonPreferredNetworkMode[i]);
				}
			}
		}

		updateCheckBoxes();
		IntentFilter homeIf = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mHomeRecevier, homeIf);

		   mSimHotSwapHandler = SimHotSwapHandler.newInstance(this);
		 mStateHandler = new PhoneServiceStateHandler(this);
	        mStateHandler.addPhoneServiceStateListener(this);
	        initIntentFilter();
	        registerReceiver(mSubReceiver, mIntentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mIsForeground = true;
		updateState();
		updateUiState();
	    mSimHotSwapHandler.registerOnSubscriptionsChangedListener();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mIsForeground = false;
	      mSimHotSwapHandler.unregisterOnSubscriptionsChangedListener();
	}

	private boolean isAirplaneModeOn() {
		return Settings.Global.getInt(getContentResolver(),
				Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}

	private void updateState() {
		Log.d(TAG, "updateState");
		for (int i = 0; i < mNumPhones; ++i) {
			mButtonDataRoam[i].setChecked(phoneList[i].getDataRoamingEnabled());
			phoneList[i].getPreferredNetworkType(mMobileHandler.obtainMessage(
					MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, i, -1));
		}
	}

	public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
		final String key = preference.getKey();
		String status;
		Log.d(TAG, "onPreferenceChange:::: ");

		for (int i = 0; i < mNumPhones; ++i) {
			if (preference == mButtonDataRoam[i]) {
				boolean isChecked = (Boolean) objValue;
				handleRoamSwitch(i, isChecked);
			} else if (preference == mButtonPreferredNetworkModeList[i]) {
				int buttonNetworkMode = Integer.valueOf((String) objValue)
						.intValue();
				handleSetNetType(buttonNetworkMode, i);
			}
		}
		return true;
	}

	private final int EVENT_SIM_STATE_CHANGED = 1002;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar;

			switch (msg.what) {
			case EVENT_SIM_STATE_CHANGED:
				Log.d(TAG, "EVENT_SIM_STATE_CHANGED");
				mHandler.postDelayed(new Runnable() {
					public void run() {
						updateUiState();
					}
				}, 1500);
				break;
			default:
				Log.w(TAG, "Unknown Event " + msg.what);
				break;
			}
		}
	};

	// 中国移动的460+ 00 、02 、07
	// 中国联通的460+01、10
	// 中国电信的460+03.
	private String getTitleFromOperatorNumber(String number) {
		Log.w(TAG, "getTitleFromOperatorNumber =" + number);
		int resId = R.string.unknown;
		if (!TextUtils.isEmpty(number)) {
			if (number.equalsIgnoreCase("46000")
					|| number.equalsIgnoreCase("46002")
					|| number.equalsIgnoreCase("46007")) {
				resId = R.string.operator_china_mobile;
				Log.w(TAG, "getTitleFromOperatorNumber2 ="
						+ getResources().getString(resId));
			} else if (number.equalsIgnoreCase("46001")
					|| number.equalsIgnoreCase("46010")) {
				resId = R.string.operator_china_unicom;
			} else if (number.equalsIgnoreCase("46003")) {
				resId = R.string.operator_china_telecom;
			}
		} else {
			return "";
		}
		return getResources().getString(resId);
	}

	protected void onDestroy() {
		super.onDestroy();
		dismissAuroraDialogSafely();
		unregisterReceiver(mHomeRecevier);
		unregisterReceiver(mSubReceiver);
        mStateHandler.removePhoneServiceSateListener();
	}

	private void updateCheckBoxes() {
		mIsSimEnable[0] = false;
		mIsSimEnable[1] = false;
		int dualSimModeSetting = Settings.System.getInt(getContentResolver(),
	                Settings.System.MSIM_MODE_SETTING, -1);
		mIsSimEnable[0] = (dualSimModeSetting & 1) > 0;
		mIsSimEnable[1] = (dualSimModeSetting & 2) > 0;

		Log.d(TAG, "updateCheckBoxes sim1 = " + mIsSimEnable[0] + " sim2 = "
				+ mIsSimEnable[1]);
	}

	private AuroraAlertDialog mErrorDialog = null;

	/**
	 * Displays an dialog box with error message.
	 * "Deactivation of both subscription is not supported"
	 */
	private void displayErrorDialog(int messageId) {
		Log.d(TAG,
				"errorMutipleDeactivate(): "
						+ getResources().getString(messageId));

		if (mErrorDialog != null) {
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}

		mErrorDialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.config_sub_title)
				.setMessage(messageId)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d(TAG, "errorMutipleDeactivate:  onClick");
								updateUiState();
							}
						})
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						Log.d(TAG, "errorMutipleDeactivate:  onDismiss");
						updateUiState();
					}
				}).create();

		mErrorDialog.show();
	}

	private void updateUiState() {
		Log.d(TAG, "updateUiState");
		// aurora modify liguangyu 20140530 for BUG #5274 start
//		mHandler.postDelayed(new Runnable() {
//			public void run() {
//				for (int i = 0; i < mNumPhones; ++i) {
//					mButtonOperatorSelectionExpand[i].auroraSetArrowText(
//							getTitleFromOperatorNumber(getOperator(i)), false);
//				}
//			}
//		}, 2000);
		// aurora modify liguangyu 20140530 for BUG #5274 end
		Log.d(TAG, "updateUiState, isCardInsert1 = " + mIsSimEnable[0]
				+ " isCardInsert2 =" + mIsSimEnable[1]);
		updateCheckBoxes();
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		int dataSub = getDataSub();

		for (int i = 0; i < mNumPhones; ++i) {
			boolean isCardInsert = (TelephonyManager.getDefault().getSimState(i) != TelephonyManager.SIM_STATE_ABSENT)
					&& (TelephonyManager.getDefault().getSimState(i) != TelephonyManager.SIM_STATE_UNKNOWN);
			manageSub[i].setEnabled(isCardInsert);
			mSimEnable[i].setOnPreferenceChangeListener(null);
			mSimEnable[i].setChecked(mIsSimEnable[i]);
			final SubscriptionInfo sir = Utils.findRecordBySlotId(this, i);
			mSimEnable[i].update(sir);
			mSimEnable[i].setEnabled(isCardInsert);
			final int ii = i;		
			mButtonOperatorSelectionExpand[i].setEnabled(isCardInsert
					&& mIsSimEnable[i]);
			mButtonDataRoam[i].setOnPreferenceChangeListener(null);
			if (isCardInsert && mIsSimEnable[i]) {
				mButtonDataRoam[i].setChecked(phoneList[i]
						.getDataRoamingEnabled());
				mButtonDataRoam[i].setEnabled(true);
				mButtonPreferredNetworkModeList[i].setEnabled(true);
				mButtonPreferredNetworkMode[i].setEnabled(true);
				mButtonAPNExpand[i].setEnabled(true);

			} else {
				mButtonDataRoam[i].setChecked(false);
				mButtonDataRoam[i].setEnabled(false);
				mButtonPreferredNetworkMode[i].setEnabled(false);
				mButtonPreferredNetworkMode[i].setEnabled(false);
				mButtonAPNExpand[i].setEnabled(false);
			}
			mHandler.post(new Runnable() {
				public void run() {		
					mSimEnable[ii]
							.setOnPreferenceChangeListener(SetSubscription.this);
					mButtonDataRoam[ii]
							.setOnPreferenceChangeListener(SetSubscription.this);
				}
			});

			if (isCardInsert && mIsSimEnable[i] && dataSub == i
					&& cm.getMobileDataEnabled()) {
				manageSub[i].addPreference(mButtonDataRoam[i]);
				manageSub[i].addPreference(mButtonAPNExpand[i]);
				if (!DeviceUtils.isIUNI()) {
					manageSub[i]
							.addPreference(mButtonPreferredNetworkModeList[i]);
				} else {
					manageSub[i].addPreference(mButtonPreferredNetworkMode[i]);
				}
			} else {
				manageSub[i].removePreference(mButtonDataRoam[i]);
				manageSub[i].removePreference(mButtonAPNExpand[i]);
				if (!DeviceUtils.isIUNI()) {
					manageSub[i]
							.removePreference(mButtonPreferredNetworkModeList[i]);
				} else {
					manageSub[i]
							.removePreference(mButtonPreferredNetworkMode[i]);
				}
			}
			mButtonOperatorSelectionExpand[i].auroraSetArrowText(
					getTitleFromOperatorNumber(getOperator(i)), false);
		}

		if (cm.getMobileDataEnabled()) {
			if (dataSub == 1) {
				manageSub[0].setTitle(getResources().getString(R.string.sub_1));
				manageSub[1].setTitle(getResources().getString(R.string.sub_2)
						+ " - "
						+ getResources().getString(
								R.string.third_data_support_title));
			} else {
				manageSub[0].setTitle(getResources().getString(R.string.sub_1)
						+ " - "
						+ getResources().getString(
								R.string.third_data_support_title));
				manageSub[1].setTitle(getResources().getString(R.string.sub_2));
			}
		} else {
			manageSub[0].setTitle(getResources().getString(R.string.sub_1));
			manageSub[1].setTitle(getResources().getString(R.string.sub_2));
		}

	}

	AuroraProgressDialog mProgressDialog = null;

	private void showSetSubProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = new AuroraProgressDialog(this);

		mProgressDialog.setMessage(getResources().getString(
				R.string.set_uicc_subscription_progress));
		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}

	private void dismissAuroraDialogSafely() {
		Log.d(TAG, "dismissAuroraDialogSafely");
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			if (mErrorDialog != null) {
				mErrorDialog.dismiss();
				mErrorDialog = null;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	// aurora add liguangyu 20140616 for BUG #5255 end

	// aurora add liguangyu 20140819 for BUG #7694 start
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
						// press home
						finish();
					} else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
						// long press home
					}
				}
			}
		}
	}

	private boolean mOkClicked;

	private void handleRoamSwitch(final int sub, boolean isChecked) {
		if (!isSimEnable(sub)) {
			return;
		}

		for (int i = 0; i < mNumPhones; ++i) {
			if (sub == i && isChecked == phoneList[i].getDataRoamingEnabled()) {
				if (DBG)
					log("onPreferenceChange: preference = mButtonDataRoam. return 1");
				return;
			}
		}

		if (DBG)
			log("onPreferenceChange: preference = mButtonDataRoam. sub = "
					+ sub + " isChecked = " + isChecked);

		DialogInterface.OnClickListener dataRoamOnclickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Log.d("dataRoamSwitchPreference" + sub, "true");
					android.provider.Settings.Global
							.putInt(getContentResolver(),
									android.provider.Settings.Global.DATA_ROAMING
											+ sub, 1);
					phoneList[sub]
							.setDataRoamingEnabled(true);
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
					.setMessage(
							getResources().getString(R.string.roaming_warning))
					.setTitle(android.R.string.dialog_alert_title)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setPositiveButton(android.R.string.yes,
							dataRoamOnclickListener)
					.setNegativeButton(android.R.string.no,
							dataRoamOnclickListener)
					.setOnDismissListener(
							new DialogInterface.OnDismissListener() {

								public void onDismiss(DialogInterface dialog) {
									Log.d("dataRoamSwitchPreference" + sub,
											"onDismiss");
									// Assuming that onClick gets called
									// first
									mButtonDataRoam[sub].setChecked(mOkClicked);

								}
							}).show();
		} else {
			android.provider.Settings.Global.putInt(getContentResolver(),
					android.provider.Settings.Global.DATA_ROAMING + sub, 0);
			phoneList[sub]
					.setDataRoamingEnabled(false);
		}
	}

	private boolean isSimEnable(int sub) {
		boolean cardReady = TelephonyManager.getDefault().getSimState(sub) == TelephonyManager.SIM_STATE_READY;
		if (DBG)
			log("isSimEnable sub = " + sub + " cardReady = " + cardReady);
		return cardReady;
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

				// check that modemNetworkMode is from an accepted value
				if ((modemNetworkMode >= Phone.NT_MODE_WCDMA_PREF)
						&& (modemNetworkMode <= 22)) {
					if (DBG) {
						log("handleGetPreferredNetworkTypeResponse: if 1: modemNetworkMode = "
								+ modemNetworkMode);
					}

					// check changes in modemNetworkMode and updates
					// settingsNetworkMode
					if (modemNetworkMode != settingsNetworkMode) {
						if (DBG) {
							log("handleGetPreferredNetworkTypeResponse: if 2: "
									+ "modemNetworkMode != settingsNetworkMode");
						}

						settingsNetworkMode = modemNetworkMode;

						if (DBG) {
							log("handleGetPreferredNetworkTypeResponse: if 2: "
									+ "settingsNetworkMode = "
									+ settingsNetworkMode);
						}

						// changes the Settings.System accordingly to
						// modemNetworkMode
						AuroraNetworkUtils.setPreferredNetworkMode(
								settingsNetworkMode, sub);
					}

					AuroraNetworkUtils.UpdatePreferredNetworkModeSummary(
							mButtonPreferredNetworkModeList[sub],						
							modemNetworkMode);
					// changes the mButtonPreferredNetworkMode accordingly to
					// modemNetworkMode
					mButtonPreferredNetworkModeList[sub].setValue(Integer
							.toString(AuroraNetworkUtils.convertNetworkMode(modemNetworkMode)));
					mButtonPreferredNetworkModeList[sub].auroraSetArrowText(
							getResources().getString(
									AuroraNetworkUtils
											.getPreferredNetworkStringId(sub)),
							true);
					mButtonPreferredNetworkMode[sub].auroraSetArrowText(
							getResources().getString(
									AuroraNetworkUtils
											.getPreferredNetworkStringId(sub)),
							true);

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
				mButtonPreferredNetworkModeList[sub].auroraSetArrowText(
						getResources().getString(
								AuroraNetworkUtils
										.getPreferredNetworkStringId(sub)),
						true);
				mButtonPreferredNetworkMode[sub].auroraSetArrowText(
						getResources().getString(
								AuroraNetworkUtils
										.getPreferredNetworkStringId(sub)),
						true);
			} else {
				log("handleSetPreferredNetworkTypeResponse exception sub = " + sub);
				phoneList[sub].getPreferredNetworkType(obtainMessage(
						MESSAGE_GET_PREFERRED_NETWORK_TYPE, sub, -1));
			}
		}

		private void resetNetworkModeToDefault(int sub) {
			log("resetNetworkModeToDefault sub = " + sub);
			// set the Settings.System
				mButtonPreferredNetworkModeList[sub].setValue(Integer
						.toString(preferredNetworkMode));				
		
			AuroraNetworkUtils.setPreferredNetworkMode(preferredNetworkMode, 0);
			
			mButtonPreferredNetworkModeList[sub].auroraSetArrowText(
					getResources().getString(
							AuroraNetworkUtils
									.getPreferredNetworkStringId(sub)),
					true);
			// Set the Modem
			phoneList[sub].setPreferredNetworkType(preferredNetworkMode, this
					.obtainMessage(
							MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub,
							-1));
		}
	}

	private static final boolean DBG = true;

	private static void log(String msg) {
		Log.d(TAG, msg);
	}

	private boolean[] mIsSimEnable;

	public static String getOperator(int slot) {
		return TelephonyManager.getDefault().getNetworkOperatorForPhone(slot);

	}

	private int getDataSub() {
		int result = mSubscriptionManager.getDefaultDataPhoneId();
		return result > -1 ? result : 0;
	}

	private void handleSetNetType(int buttonNetworkMode, int sub) {
		log("handleSetNetType buttonNetworkMode = " + buttonNetworkMode + " sub = " + sub );
		mButtonPreferredNetworkModeList[sub].setValue(buttonNetworkMode + "");

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
					 mButtonPreferredNetworkModeList[sub], buttonNetworkMode);

			AuroraNetworkUtils.setPreferredNetworkMode(modemNetworkMode, sub);
			phoneList[sub].setPreferredNetworkType(modemNetworkMode,
					mMobileHandler.obtainMessage(
							MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub,
							-1));

		}
	}

	private void bindWithRadioPowerManager(RadioPowerPreference simPreference,
			SubscriptionInfo subInfo) {
		int subId = subInfo == null ? SubscriptionManager.INVALID_SUBSCRIPTION_ID
				: subInfo.getSubscriptionId();
		RadioPowerManager radioMgr = new RadioPowerManager(this);
		radioMgr.bindPreference(simPreference, subId);
		RadioPowerManager.DialogListener l = new RadioPowerManager.DialogListener(){
			 public void showDialog(int resId) {
				 displayErrorDialog(resId);
			 }
		};
		radioMgr.setDialogListener(l);
	}
	
	 
	 @Override
	    public void onServiceStateChanged(ServiceState state, int subId) {
	        Log.d(TAG, "PhoneStateListener:onServiceStateChanged: subId: " + subId
	                + ", state: " + state);
	        if (isRadioSwitchComplete(subId, state)) {
	            handleRadioPowerSwitchComplete();
	        }
	    }
	 
	    private static final boolean RADIO_POWER_OFF = false;
	    private static final boolean RADIO_POWER_ON = true;
	    private static final int MODE_PHONE1_ONLY = 1;
	 
	 private boolean isRadioSwitchComplete(final int subId, ServiceState state) {
	        int slotId = SubscriptionManager.getSlotId(subId);
	        boolean radiosState = getRadioStateForSlotId(slotId);
	        Log.d(TAG, "soltId: " + slotId + ", radiosState is : " + radiosState);
	        if (radiosState && (state.getState() != ServiceState.STATE_POWER_OFF)) {
	            return true;
	        } else if (state.getState() == ServiceState.STATE_POWER_OFF) {
	            return true;
	        }
	        return false;
	    }
	 
	 private boolean getRadioStateForSlotId(final int slotId) {
	        int currentSimMode = Settings.System.getInt(getContentResolver(),
	                Settings.System.MSIM_MODE_SETTING, -1);
	        boolean radiosState = ((currentSimMode & (MODE_PHONE1_ONLY << slotId)) == 0) ?
	                RADIO_POWER_OFF : RADIO_POWER_ON;
	        Log.d(TAG, "soltId: " + slotId + ", radiosState : " + radiosState);
	        return radiosState;
	    }
	 
	 private void handleRadioPowerSwitchComplete() {
		 updateUiState();
	        // M Auto open the other card's data connection. when current card is radio off
//	        mExt.showChangeDataConnDialog(this, isResumed());
	    }
	 
	 
	    private boolean mIsAirplaneModeOn = false;
	    private IntentFilter mIntentFilter;
	   private void initIntentFilter() {
	        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//	        mIntentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
	        // For SIM Switch
	        mIntentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
	        mIntentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
	    }
	 
	  private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            Log.d(TAG, "mSubReceiver action = " + action);
	            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
	                handleAirplaneModeBroadcast(intent);
	            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
//	                handleDataConnectionStateChanged(intent);
	            } else if (isSimSwitchAction(action)) {
	                updateUiState();
	            }
	        }
	    };
	    
	    private void handleAirplaneModeBroadcast(Intent intent) {
	        mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
	        Log.d(TAG, "air plane mode is = " + mIsAirplaneModeOn);
	        updateUiState();
	    }
	    
	    private void handleDataConnectionStateChanged(Intent intent) {
	        String apnTypeList = intent.getStringExtra(PhoneConstants.DATA_APN_TYPE_KEY);
	        /// M: just process default type data change, avoid unnecessary
	        /// change broadcast
	        if ((PhoneConstants.APN_TYPE_DEFAULT.equals(apnTypeList))) {
	            /// M: Auto open the other card's data connection.
	            // when current card is radio off
//	            mExt.dealWithDataConnChanged(intent, isResumed());
	            /// @}
	        }
	    }
	    
	    private boolean isSimSwitchAction(String action) {
	        return action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE) ||
	               action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
	    }
}
