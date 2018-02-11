package com.android.phone;

import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import aurora.app.*;
import aurora.preference.*;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;

public class SimInfoEditorPreference extends AuroraPreferenceActivity implements
		AuroraPreference.OnPreferenceChangeListener, RadioPowerManager.UpdateListener {
	private static final String TAG = "SimInfoEditorPreference";
	private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
	
	private static final String BUTTON_APN_EXPAND_KEY = "button_apn_key";

	private RadioPowerPreference mSimEnable;
	private AuroraEditTextPreference mName;
	private AuroraEditTextPreference mNumber;
	private AuroraPreferenceScreen mOperator;
	private AuroraPreferenceScreen mButtonAPNExpand;
	private AuroraPreferenceScreen mStk;
	private int mSubscription = 0;
	private SubscriptionInfo mSubInfoRecord;
	private SubscriptionManager mSubscriptionManager;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.siminf_editor);
		// getting selected subscription
		mSubscription = AuroraPhoneUtils.getSlot(getIntent());
		if(mSubscription > 0) {
			getAuroraActionBar().setTitle(R.string.sub2);
		} else {
			getAuroraActionBar().setTitle(R.string.sub1);
		}

		Log.d(TAG, "Getting subscription =" + mSubscription);
		Phone phone = PhoneGlobals.getInstance().getPhone(mSubscription);
		mSubscriptionManager = SubscriptionManager.from(this);
		
		
		mSimEnable = (RadioPowerPreference) findPreference("sim_enable");
		Log.d(TAG, "Getting mSimEnable =" + mSimEnable);
		int dualSimModeSetting = Settings.System.getInt(getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);
		boolean isSimEnable = (dualSimModeSetting & (mSubscription +1)) > 0;	
		mSimEnable.setChecked(isSimEnable);
		mSubInfoRecord = Utils.findRecordBySlotId(this,
				mSubscription);
		bindWithRadioPowerManager(mSimEnable, mSubInfoRecord);
		mSimEnable.update(mSubInfoRecord);
		
		mName = (AuroraEditTextPreference) findPreference("name");
		mName.setOnPreferenceChangeListener(this);
		mNumber = (AuroraEditTextPreference) findPreference("number");
		mNumber.setOnPreferenceChangeListener(this);
		mOperator = (AuroraPreferenceScreen) findPreference("operator");
		mStk = (AuroraPreferenceScreen) findPreference("stk");
		Intent stkIntent = new Intent(Intent.ACTION_MAIN);
		if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
			stkIntent.setClassName("com.android.utk",
					"com.android.utk.UtkLauncherActivity");
			 PackageManager pm = getPackageManager();
//			 ComponentName componentName = new ComponentName("com.android.utk",
//		                "com.android.utk.UtkLauncherActivity");
//			 int state = pm.getComponentEnabledSetting(componentName);
//			 if(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
//				  getPreferenceScreen().removePreference(mStk);
//			 }
		} else {
			stkIntent
					.setClassName("com.android.stk", "com.android.stk.StkMain");
		}
		mStk.setIntent(stkIntent);
		
		mButtonAPNExpand = (AuroraPreferenceScreen) findPreference(BUTTON_APN_EXPAND_KEY);
//		mButtonAPNExpand.getIntent().putExtra(SUBSCRIPTION_KEY, i);
//		mButtonAPNExpand.getIntent().putExtra("sub_id", AuroraSubUtils.getSubIdbySlot(this, i));
//		mButtonAPNExpand.getIntent().addFlags(
//				Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//		mButtonAPNExpand.getIntent().addFlags(
//				Intent.FLAG_ACTIVITY_NEW_TASK);
		   mButtonAPNExpand.setOnPreferenceClickListener(
                   new AuroraPreference.OnPreferenceClickListener() {
                       @Override
                       public boolean onPreferenceClick(AuroraPreference preference) {
                           // We need to build the Intent by hand as the Preference Framework
                           // does not allow to add an Intent with some extras into a Preference
                           // XML file
                           final Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
                           // This will setup the Home and Search affordance
                           intent.putExtra(":settings:show_fragment_as_subsetting", true);
                           intent.putExtra("sub_id", AuroraSubUtils.getSubIdbySlot(SimInfoEditorPreference.this, mSubscription));
                           startActivity(intent);
                           return true;
                       }
           });
			
		mOperator.getIntent().putExtra(
				SUBSCRIPTION_KEY, mSubscription);
		mOperator.getIntent().addFlags(
				Intent.FLAG_ACTIVITY_NO_HISTORY);		
		
		   if(PhoneGlobals.getInstance().getPhone(mSubscription).getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
			   getPreferenceScreen().removePreference(mOperator);
		   } 
		
		updateUiState();
		
		IntentFilter intentFilter = new IntentFilter(
				TelephonyIntents.ACTION_SIM_STATE_CHANGED);
		registerReceiver(mUiReceiver, intentFilter);
	}

	private void bindWithRadioPowerManager(RadioPowerPreference simPreference,
			SubscriptionInfo subInfo) {
		int subId = subInfo == null ? SubscriptionManager.INVALID_SUBSCRIPTION_ID
				: subInfo.getSubscriptionId();
		RadioPowerManager radioMgr = new RadioPowerManager(this);
		radioMgr.bindPreference(simPreference, subId);
		radioMgr.setUpdateListener(this);
		RadioPowerManager.DialogListener l = new RadioPowerManager.DialogListener() {
			public void showDialog(int resId) {
				displayErrorDialog(resId);
			}
		};
		radioMgr.setDialogListener(l);
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
				.setPositiveButton(android.R.string.yes, null)
				.create();

		mErrorDialog.show();
	}

	

	private void updateUiState() {
		Log.d(TAG, "updateUiState");
		
		boolean isSimEnable = false;
		int dualSimModeSetting = Settings.System.getInt(getContentResolver(),
	                Settings.System.MSIM_MODE_SETTING, -1);
		isSimEnable = (dualSimModeSetting & (mSubscription +1)) > 0;

		Log.d(TAG, "updateUiState isSimEnable = " + isSimEnable);
//		mSimEnable.setOnPreferenceChangeListener(null);
//		mSimEnable.setChecked(isSimEnable);
//		mSimEnable.update(mSubInfoRecord);
//		mSimEnable.setOnPreferenceChangeListener(this);
			
		if(isSimEnable) {
			mName.setEnabled(true);
			mNumber.setEnabled(true);
//		   if(PhoneGlobals.getInstance().getPhone(mSubscription).getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
//				mOperator.auroraSetArrowText("", false);
//			   mOperator.setEnabled(false);
//			} else {
				mOperator.setEnabled(true);
//			}
			mStk.setEnabled(true);
			mButtonAPNExpand.setEnabled(true);
		} else {
			mName.setEnabled(false);
			mNumber.setEnabled(false);
			mOperator.setEnabled(false);
			mStk.setEnabled(false);
			mButtonAPNExpand.setEnabled(false);
		}
		
		String operator = AuroraPhoneUtils.getOperatorTitle(mSubscription);
		if(!TextUtils.isEmpty(operator)) {
			mOperator.setSummary(operator);
		}
		
		String name = mSubInfoRecord.getDisplayName().toString();
		Log.d(TAG, "updateUiState name = " + name + "operator =  " + operator);
		if(TextUtils.isEmpty(name)) {
			name = operator;
			Log.d(TAG, "updateUiState operator = " + name);
		}
		if(!TextUtils.isEmpty(name)) {
			mName.setText(name);
		    mName.setSummary(name);
		    getAuroraActionBar().setTitle(name);
		}

		String number = mSubInfoRecord.getNumber();
		Log.d(TAG, "updateUiState number = " + number);
		if(TextUtils.isEmpty(number)) {
			number = getPhoneNumber(mSubInfoRecord);
			Log.d(TAG, "updateUiState telenumber = " + number);
			if(!TextUtils.isEmpty(number)) {
				mNumber.setEnabled(false);
			}
		}
		if(!TextUtils.isEmpty(number)) {
			mNumber.setText(number);
			mNumber.setSummary(number);
		}
		
	}
	
    // Returns the line1Number. Line1number should always be read from TelephonyManager since it can
    // be overridden for display purposes.
    private String getPhoneNumber(SubscriptionInfo info) {
        final TelephonyManager tm =
            (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1NumberForSubscriber(info.getSubscriptionId());
    }

	public boolean onPreferenceChange(AuroraPreference preference,
			Object objValue) {
		final String key = preference.getKey();
		String status;
		Log.d(TAG, "onPreferenceChange:::: ");

		if (preference == mName) {
			String name = (String) objValue;
			handleSetName(name);
		} else if (preference == mNumber) {
			String mNumber = (String) objValue;
			handleSetNumber(mNumber);
		}
		updateUiState();
		return true;
	}
	
	private void handleSetName(String displayName) {
          int subId = mSubInfoRecord.getSubscriptionId();
          mSubInfoRecord.setDisplayName(displayName);
          mSubscriptionManager.setDisplayName(displayName, subId,
                  SubscriptionManager.NAME_SOURCE_USER_INPUT);
	}
	
	private void handleSetNumber(String number) {
		   int subId = mSubInfoRecord.getSubscriptionId();
	          mSubInfoRecord.setNumber(number);
	          mSubscriptionManager.setDisplayNumber(number, subId);
	}

	  public void updateUI() {		 
			mHandler.sendEmptyMessage(UPDATE);
	  }
	  
		private final static int UPDATE = 1;
		private Handler mHandler = new Handler() {
		    @Override
	        public void handleMessage(Message msg) {
		    	if(msg.what == UPDATE) {
		    		updateUiState();
		    	} 
		    }
		};
		
		private BroadcastReceiver mUiReceiver = new UiBroadcastReceiver();

		private class UiBroadcastReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.v(TAG, "Action intent recieved:" + action);
				// gets the subscription information ( "0" or "1")
				if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
					  String simStatus = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
	                  if (simStatus.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
	                	  finish();
	                  } 
				}
			}
		}
		protected void onDestroy() {
			Log.d(TAG, "onDestroy::");	
			super.onDestroy();
			unregisterReceiver(mUiReceiver);
	}
}
