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
import android.telephony.MSimTelephonyManager;
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
import static com.android.internal.telephony.MSimConstants.SUBSCRIPTION_KEY;
import com.android.phone.AuroraTelephony.SimInfo;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneProxy;
import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.SubscriptionManager;
import com.codeaurora.telephony.msim.Subscription.SubscriptionStatus;
import com.codeaurora.telephony.msim.MSimGSMPhone;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import android.view.View;

/**
 * "MSim Mobile network settings" screen.  This preference screen lets you
 * enable/disable mobile data, and control data roaming and other
 * network-specific mobile data features.  It's used on non-voice-capable
 * tablets as well as regular phone devices.
 *
 * Note that this PreferenceActivity is part of the phone app, even though
 * you reach it from the "Wireless & Networks" section of the main
 * Settings app.  It's not part of the "Call settings" hierarchy that's
 * available from the Phone app (see MSimCallFeaturesSetting for that.)
 */
public class MSimMobileNetworkSettings extends AuroraPreferenceActivity
        implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, AuroraPreference.OnPreferenceChangeListener{

    // debug data
    private static final String LOG_TAG = "MSimMobileNetworkSettings";
    private static final boolean DBG = true;

    //String keys for preference lookup
    private static final String BUTTON_MANAGE_SUB_KEY = "button_settings_manage_sub";
    private static final String BUTTON_DATA_ENABLED_KEY = "button_data_enabled_key";
    private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
    private static final String BUTTON_DATA_ENABLED_KEY2 = "button_data_enabled_key2";
    private static final String BUTTON_ROAMING_KEY2 = "button_roaming_key2";
    private static final String BUTTON_CDMA_LTE_DATA_SERVICE_KEY = "cdma_lte_data_service_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE2 = "preferred_network_mode_key2";
    private static final String BUTTON_PREFERED_NETWORK_MODE4 = "preferred_network_mode_key4";
    private static final String BUTTON_APN_EXPAND_KEY1 = "button_apn_key_1";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY1 = "button_carrier_sel_key_1";
    private static final String BUTTON_APN_EXPAND_KEY2 = "button_apn_key_2";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY2 = "button_carrier_sel_key_2";
    private static final String KEY_DATA = "data";
    private static final String PRIORITY_SUB = "priority_subscription";

    //Information about logical "up" Activity
    private static final String UP_ACTIVITY_PACKAGE = "com.android.settings";
    private static final String UP_ACTIVITY_CLASS =
            "com.android.settings.Settings$WirelessSettingsActivity";

    private AuroraSwitchPreference mButtonDataRoam;
    private AuroraSwitchPreference mButtonDataEnabled;
    private AuroraSwitchPreference mButtonDataRoam2;
    private AuroraSwitchPreference mButtonDataEnabled2;
    private AuroraPreference mLteDataServicePref;
    private AuroraListPreference mButtonPreferredNetworkMode;
    private AuroraPreference mButtonPreferredNetworkMode2, mButtonPreferredNetworkMode4;
    private AuroraPreferenceScreen mButtonAPNExpand1;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand1;
    private AuroraPreferenceScreen mButtonAPNExpand2;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand2;
    private AuroraPreference mData;
    private AuroraPreference mPrioritySub;
    private AuroraSwitchPreference mAuroraButtonDataEnabled;
    
    private CharSequence[] entries; // Used for entries like Subscription1, Subscription2 ...
    private CharSequence[] entryValues; // Used for entryValues like 0, 1 ,2 ...
    private CharSequence[] summaries; // Used for Summaries like Aubscription1, Subscription2....
    private CharSequence[] entriesPrompt; // Used in case of prompt option is required.
    private CharSequence[] entryValuesPrompt; // Used in case of prompt option is required.
    private CharSequence[] summariesPrompt; // Used in case of prompt option is required.

    private static final String iface = "rmnet0"; //TODO: this will go away

    private Phone mPhone;
    private Phone mPhone2;
    private boolean mOkClicked;

    private AuroraPreference mClickedPreference;

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

    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		try {
			removeMenuById(AuroraMenu.FIRST);
			removeMenuById(AuroraMenu.FIRST + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (preference == mButtonDataRoam) {
            if (DBG) log("onPreferenceTreeClick: preference = mButtonDataRoam");

            //normally called on the toggle click
            if (mButtonDataRoam.isChecked()) {
                // First confirm with a warning dialog about charges
                mOkClicked = false;
                new AuroraAlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.roaming_warning))
                        .setTitle(android.R.string.dialog_alert_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show()
                        .setOnDismissListener(this);
            } else {
                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
    	                android.provider.Settings.Global.DATA_ROAMING + 0, 0);
                mPhone.setDataRoamingEnabled(false);
            }
            return true;
        } else if (preference == mButtonDataEnabled) {
            if (DBG) log("onPreferenceTreeClick: preference == mButtonDataEnabled.");
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            cm.setMobileDataEnabled(mButtonDataEnabled.isChecked());
            return true;
        } else if (preference == mLteDataServicePref) {
            String tmpl = android.provider.Settings.Global.getString(getContentResolver(),
                        android.provider.Settings.Global.SETUP_PREPAID_DATA_SERVICE_URL);
            if (!TextUtils.isEmpty(tmpl)) {
                TelephonyManager tm = (TelephonyManager) getSystemService(
                        Context.TELEPHONY_SERVICE);
                String imsi = tm.getSubscriberId();
                if (imsi == null) {
                    imsi = "";
                }
                final String url = TextUtils.isEmpty(tmpl) ? null
                        : TextUtils.expandTemplate(tmpl, imsi).toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                android.util.Log.e(LOG_TAG, "Missing SETUP_PREPAID_DATA_SERVICE_URL");
            }
            return true;
        } else if (preference == mButtonPreferredNetworkMode) {
            mButtonPreferredNetworkMode.setValue(Integer.toString(getPreferredNetworkMode(0)));
            return true;
        } else if (preference == mData) {
		    String menuSlot0 = entries[0].toString();
		    String menuSlot1 = entries[1].toString();
			addMenu(AuroraMenu.FIRST, menuSlot1,
					new OnMenuItemClickLisener() {
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
			addMenu(AuroraMenu.FIRST, menuSlot1,
					new OnMenuItemClickLisener() {
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
//            preferenceScreen.setEnabled(false);
            // Let the intents be launched by the Preference manager
            return false;
        }
    }
        

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.msim_network_setting);

//        mPhone = ((MSimPhoneGlobals)PhoneGlobals.getInstance()).getDefaultPhone();
        mPhone = PhoneGlobals.getInstance().getPhone(0);
        mPhone2 = PhoneGlobals.getInstance().getPhone(1);
        mHandler = new MyHandler();
        mSubscriptionManager = SubscriptionManager.getInstance();
        //get UI object references
        AuroraPreferenceScreen prefSet = getPreferenceScreen();

        mButtonDataEnabled = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_DATA_ENABLED_KEY);
        mButtonDataEnabled.setOnPreferenceChangeListener(this);
        mButtonDataRoam = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_ROAMING_KEY);
        mButtonDataRoam.setOnPreferenceChangeListener(this);
        mButtonDataEnabled2 = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_DATA_ENABLED_KEY2);
        mButtonDataEnabled2.setOnPreferenceChangeListener(this);
        mButtonDataRoam2 = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_ROAMING_KEY2);
        mButtonDataRoam2.setOnPreferenceChangeListener(this);
//        mButtonDataRoam.setListener(mListener);
        mLteDataServicePref = prefSet.findPreference(BUTTON_CDMA_LTE_DATA_SERVICE_KEY);
        mButtonPreferredNetworkMode = (AuroraListPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE);        
        mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
        mButtonPreferredNetworkMode.setValue(Integer.toString(getPreferredNetworkMode(0)));
        mButtonPreferredNetworkMode2 = (AuroraPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE2);
        mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
        Intent intent = mButtonPreferredNetworkMode2.getIntent();
        intent.putExtra(SUBSCRIPTION_KEY, 0);
        mButtonPreferredNetworkMode4 = (AuroraPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE4);
        mButtonPreferredNetworkMode4.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(1)),true);
        intent = mButtonPreferredNetworkMode4.getIntent();
        intent.putExtra(SUBSCRIPTION_KEY, 1);
        
        mButtonAPNExpand1 = (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_APN_EXPAND_KEY1);
        mButtonAPNExpand1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
        mButtonOperatorSelectionExpand1 =
                (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY1);
        mButtonOperatorSelectionExpand1.getIntent().putExtra(SUBSCRIPTION_KEY, 0);
        
        mButtonAPNExpand2 = (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_APN_EXPAND_KEY2);
        mButtonAPNExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mButtonOperatorSelectionExpand2 =
                (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY2);
        mButtonOperatorSelectionExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        
        mData = findPreference(KEY_DATA);
//        mData.setOnPreferenceChangeListener(this);
        mPrioritySub = findPreference(PRIORITY_SUB);
//        mPrioritySub.setOnPreferenceChangeListener(this);
        mAuroraButtonDataEnabled = (AuroraSwitchPreference) prefSet.findPreference("aurora_data_enabled_key");
        mAuroraButtonDataEnabled.setOnPreferenceChangeListener(this);        
        int numPhones = MSimTelephonyManager.getDefault().getPhoneCount();
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
        CharSequence[] subString = getResources().getTextArray(R.array.multi_sim_entries);
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

        boolean isLteOnCdma = mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE;

        AuroraPreferenceScreen manageSub = (AuroraPreferenceScreen) prefSet.findPreference(
        		BUTTON_MANAGE_SUB_KEY);
        if (manageSub != null) {
            intent = manageSub.getIntent();
            intent.putExtra(SelectSubscription.PACKAGE, "com.android.phone");
            intent.putExtra(SelectSubscription.TARGET_CLASS,
                    "com.android.phone.MSimMobileNetworkSubSettings");
        }
        
        manageSub1 = (AuroraPreferenceCategory) prefSet.findPreference("sim1_category_key");        
        manageSub2 = (AuroraPreferenceCategory) prefSet.findPreference("sim2_category_key");
        
        final boolean missingDataServiceUrl = TextUtils.isEmpty(
                android.provider.Settings.Global.getString(getContentResolver(),
                        android.provider.Settings.Global.SETUP_PREPAID_DATA_SERVICE_URL));
        if (!isLteOnCdma || missingDataServiceUrl) {
            android.util.Log.d(LOG_TAG, "remove ltePref");
//            prefSet.removePreference(mLteDataServicePref);
            manageSub1.removePreference(mLteDataServicePref);
        } else {
            android.util.Log.d(LOG_TAG, "keep ltePref");
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
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
        
        
        if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA || !DeviceUtils.isIUNI()) {
//          mButtonPreferredNetworkMode.setEntries(R.array.cm_preferred_network_mode_choices);
//          mButtonPreferredNetworkMode.setEntryValues(R.array.cm_preferred_network_mode_values); 
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
		        
		        if(mButtonDataEnabled2 != null) {
		        	manageSub2.removePreference(mButtonDataEnabled2);
		        }
		 }
        
		   if(manageSub1 != null) {
		    	prefSet.removePreference(manageSub1);
	        }       
		   if(manageSub2 != null) {
		    	prefSet.removePreference(manageSub2);
	        }    
		   
	    	AuroraPreference dataSummary = prefSet.findPreference("sim_data_support_summary_key");   
	    	dataSummary.setEnabled(false);
	    	
			if (!DeviceUtils.isSupportDualData()) {
			     AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory)findPreference("sim_data_support_key");
			     dataPreferenceCategory.removePreference(mData);  
		    	dataPreferenceCategory.removePreference(dataSummary);
			}
			
            IntentFilter homeIf =new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            registerReceiver(mHomeRecevier, homeIf);
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

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        
        int dataSub = MSimPhoneFactory.getDataSubscription();
        if(dataSub != 0) {
        	mButtonDataEnabled2.setChecked(cm.getMobileDataEnabled());
        } else  {
        	mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        }
        
        mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        

        // Set UI state in onResume because a user could go home, launch some
        // app to change this setting's backend, and re-launch this settings app
        // and the UI state would be inconsistent with actual state
    	mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
    	mButtonDataRoam2.setChecked(mPhone2.getDataRoamingEnabled());
        
        //aurora add liguangyu 20140424 for BUG #4517 start
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);        
      	registerReceiver(mDataReceiver, filter);
      	if(isWifiConnect()) {
      		mCheckDataConnectThread = new CheckDataConnectThread();
      		mCheckDataConnectThread.start();
      		mContinueChecking = true;
      	}
      	//aurora add liguangyu 20140424 for BUG #4517 end
      	
      	
		IntentFilter recordFilter = new IntentFilter(AURORA_STATE_CHANGED_ACTION);      
		registerReceiver(mPhoneStateReceiver, recordFilter);
		updateUiState();
        mPhone.getPreferredNetworkType(mHandler.obtainMessage(
                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 0, -1));
        if(mButtonPreferredNetworkMode4 != null) {
        PhoneGlobals.getInstance().getPhone(1).getPreferredNetworkType(mHandler.obtainMessage(
                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 1, -1));
        }
        
        updateOperatorSelectionVisibility();
        
        IntentFilter intentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mUiReceiver, intentFilter);
        
        updateDataPreference();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //aurora add liguangyu 20140424 for BUG #4517 start
        mIsForeground = false;
        unregisterReceiver(mDataReceiver);
        if(mCheckDataConnectThread != null) {
        	mContinueChecking = false;
        	mCheckDataConnectThread = null;
        }
        //aurora add liguangyu 20140424 for BUG #4517 end
		unregisterReceiver(mPhoneStateReceiver);
	    unregisterReceiver(mUiReceiver);
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            // Commenting out "logical up" capability. This is a workaround for issue 5278083.
            //
            // Settings app may not launch this activity via UP_ACTIVITY_CLASS but the other
            // Activity that looks exactly same as UP_ACTIVITY_CLASS ("SubSettings" Activity).
            // At that moment, this Activity launches UP_ACTIVITY_CLASS on top of the Activity.
            // which confuses users.
            // TODO: introduce better mechanism for "up" capability here.
            /*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(UP_ACTIVITY_PACKAGE, UP_ACTIVITY_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    //aurora add liguangyu 20140424 for BUG #4517 start
    private DataReceiver mDataReceiver = new DataReceiver();
    private class DataReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            // TODO Auto-generated method stub              
            String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
              	if(isWifiConnect()) {
              		if(mCheckDataConnectThread == null) {
	              		mCheckDataConnectThread = new CheckDataConnectThread();
	              		mCheckDataConnectThread.start();
	              		mContinueChecking = true;
              		}
              	} else {
                    if(mCheckDataConnectThread != null) {
                    	mContinueChecking = false;
                    	mCheckDataConnectThread = null;
                    }
              	}
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE)) {
            	setDataConnect();
            } 
        }   
      
    }      
    
    private boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
    
    private Handler mCheckHandler = new Handler(){
        public void handleMessage(Message msg) {
            setDataConnect();            
        }
    };
    
    private void setDataConnect() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i(LOG_TAG, "mDataReceiver()...  data = " + cm.getMobileDataEnabled());
        int dataSub = MSimPhoneFactory.getDataSubscription();
	   	if(!isSimEnable(dataSub)) {
			 return;
		}
        mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        mButtonDataEnabled2.setChecked(cm.getMobileDataEnabled());        
        mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
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
    
    
//    @Override
//    protected void onDestroy() {
//        Log.i(LOG_TAG, "onDestroy()...  this = " + this);
//        super.onDestroy();
//        try {
//        	unregisterReceiver(mDataReceiver);
//        } catch(Exception e) {
//        	e.printStackTrace();
//        }
//        if(mCheckDataConnectThread != null) {
//        	mContinueChecking = false;
//        	mCheckDataConnectThread = null;
//        }
//    }
    //aurora add liguangyu 20140424 for BUG #4517 end
    

 	
 	AuroraPreferenceCategory manageSub1, manageSub2;
	private static final String AURORA_STATE_CHANGED_ACTION = "PhoneServiceStateChanged";
    private final PhoneStateReceiver mPhoneStateReceiver = new PhoneStateReceiver();
    private class PhoneStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {     	
       	    mHandler.postDelayed(new Runnable(){
     	    	public void run(){
     	        	updateUiState();
     	    	}
     	    }, 1500);
        }
    }
     private void updateUiState() {
    	boolean simSubState1 = false, simSubState2 = false;
    	SharedPreferences sharedPreferences = getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
		SIMInfo sim1 = SIMInfo.getSIMInfoBySlot(this, 0);
        if(sim1 != null) {
        	simSubState1 = sharedPreferences.getBoolean(String.valueOf(sim1.mSimId), true);
        } 
        SIMInfo sim2 = SIMInfo.getSIMInfoBySlot(this, 1);
        if(sim2 != null) {
        	simSubState2 = sharedPreferences.getBoolean(String.valueOf(sim2.mSimId), true);
        } 
    	boolean card1Ready = MSimTelephonyManager.getDefault().getSimState(0) == TelephonyManager.SIM_STATE_READY;
    	boolean card2Ready = MSimTelephonyManager.getDefault().getSimState(1) == TelephonyManager.SIM_STATE_READY;
        if (DBG) log("updateUiState: simSubState1 =" + simSubState1 + " simSubState2=" + simSubState2 + " card1Ready=" + card1Ready + " card2Ready=" + card2Ready);

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(card1Ready && simSubState1) {
           	mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
         	mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
        } else {
           	mButtonDataEnabled.setChecked(false);
         	mButtonDataRoam.setChecked(false);
        }
        if(card2Ready && simSubState2) {
           	mButtonDataEnabled2.setChecked(cm.getMobileDataEnabled());
         	mButtonDataRoam2.setChecked(mPhone2.getDataRoamingEnabled());
        } else {
           	mButtonDataEnabled2.setChecked(false);
         	mButtonDataRoam2.setChecked(false);
        }     
        
        boolean iscard1Enable = isSimEnable(0);
        boolean iscard2Enable = isSimEnable(1);
       	manageSub1.setEnabled(iscard1Enable);
       	manageSub2.setEnabled(iscard2Enable);
       	
    	 if (DeviceUtils.isSupportDualData()) {
    	    mAuroraButtonDataEnabled.setEnabled(isSimEnable(0) || isSimEnable(1));
    	 } else {
 	       	mAuroraButtonDataEnabled.setEnabled(isSimEnable(0)); 
    	 }

        mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        mData.setEnabled(iscard1Enable && iscard2Enable);
        mPrioritySub.setEnabled(iscard1Enable && iscard2Enable);
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
     private MyHandler mHandler;
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
//                 Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
             }
         }

         private void handleSetPreferredNetworkTypeResponse(Message msg) {
             AsyncResult ar = (AsyncResult) msg.obj;

             if (ar.exception == null) {
//                 int networkMode = Integer.valueOf(
//                         mButtonPreferredNetworkMode.getValue()).intValue();
//                 setPreferredNetworkMode(networkMode, 0);
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
     
     private void updateOperatorSelectionVisibility() {
         log("updateOperatorSelectionVisibility. mPhone = " + mPhone.getPhoneName());
         Resources res = getResources();
         
         AuroraPreferenceScreen mPrefScreen = getPreferenceScreen();
         if (mButtonOperatorSelectionExpand1 == null) {
             android.util.Log.e(LOG_TAG, "mButtonOperatorSelectionExpand is null");
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
             android.util.Log.e(LOG_TAG, "mButtonOperatorSelectionExpand is null");
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
     
     private BroadcastReceiver mUiReceiver = new UiBroadcastReceiver();
     
     private class UiBroadcastReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             Log.v(LOG_TAG,"Action intent recieved:"+action);
             //gets the subscription information ( "0" or "1")
             int subscription = intent.getIntExtra(SUBSCRIPTION_KEY, PhoneGlobals.getInstance().getDefaultSubscription());
             if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            	 updateUiState();
             }
         }
     }
     
     public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
         String status;
         if (preference == mButtonPreferredNetworkMode) {
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
                 mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                         .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
             }
         } else if (preference == mButtonDataEnabled) {
             boolean isChecked = (Boolean)objValue;
             handleDataSwitch(0, isChecked);
         } else if (preference == mButtonDataEnabled2) {
             boolean isChecked = (Boolean)objValue;
             handleDataSwitch(1, isChecked);
         }  else if (preference == mButtonDataRoam) {
             boolean isChecked = (Boolean)objValue;
             handleRoamSwitch(0, isChecked);
         } else if (preference == mButtonDataRoam2) {
             boolean isChecked = (Boolean)objValue;
             handleRoamSwitch(1, isChecked);
         } else if (preference == mData) {
             int dataSub = Integer.parseInt((String) objValue);
             Log.d(LOG_TAG, "setDataSubscription " + dataSub);
             if (mIsForeground) {
                 showDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
             }

             Message setDdsMsg = Message.obtain(mMobileHandler, EVENT_SET_DATA_SUBSCRIPTION_DONE, null);
             mSubscriptionManager.setDataSubscription(dataSub, setDdsMsg);
         } else if (preference == mPrioritySub) {
             int prioritySubIndex = Integer.parseInt((String) objValue);
             Log.d(LOG_TAG, "onPreferenceChange::::  prioritySubIndex =" + prioritySubIndex );
             if (mSubscriptionManager.getCurrentSubscription(prioritySubIndex).subStatus
                     == SubscriptionStatus.SUB_ACTIVATED) {
                 mPrioritySubValue = prioritySubIndex;
                 mMobileHandler.sendMessage(mMobileHandler.obtainMessage(EVENT_SET_PRIORITY_SUBSCRIPTION,
                         prioritySubIndex, 0));                
                 
                 //voice
                 MSimPhoneFactory.setPromptEnabled(false);
                 mVoiceSub = prioritySubIndex;
                 mMobileHandler.sendMessage(mMobileHandler.obtainMessage(EVENT_SET_VOICE_SUBSCRIPTION,
                 		prioritySubIndex, 0));
                 
                 //sms
                 MSimPhoneFactory.setSMSSubscription(prioritySubIndex);
                 
             } else {
                 status = getResources().getString(R.string.set_priority_sub_error);
                 displayAlertDialog(status);
             }
         } else if(preference == mAuroraButtonDataEnabled) {
             boolean isChecked = (Boolean)objValue;
             handleAuroraDataSwitch(isChecked);
         }

         return true;
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
     
     
     private void handleDataSwitch(int sub, boolean isChecked) {
    	 
    	 if(!isSimEnable(sub)) {
    		 return;
    	 }
    	 
         ConnectivityManager cm =
                 (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	 if(isChecked == cm.getMobileDataEnabled()) {
  			if (DBG) log("onPreferenceChange: preference = mButtonDataEnabled. return");
    		 return ;
    	 }
        
    	 
         if (DBG) log("onPreferenceChange: preference == mButtonDataEnabled. sub = " + sub + " isChecked = " + isChecked);
		if (PhoneUtils.isMultiSimEnabled()) {
			android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
					android.provider.Settings.Global.MOBILE_DATA + sub,
					isChecked ? 1 : 0);
			if (!DeviceUtils.isSupportDualData()) {
				PhoneUtils.setPreferredDataSubscription(0);
			}
		}
//         ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
         cm.setMobileDataEnabled(isChecked);
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
     
     private void updateDataPreference() {
		 if (!DeviceUtils.isSupportDualData()) {
			 return;
		 }
         Log.d("LOG_TAG", "updateDataPreference");
         int dataSub = MSimPhoneFactory.getDataSubscription();
		 if(dataSub != 0) {
	         manageSub1.removePreference(mButtonDataRoam);
	         manageSub1.removePreference(mButtonDataEnabled);
	         manageSub1.removePreference(mButtonAPNExpand1);
	         manageSub1.removePreference(mButtonPreferredNetworkMode2);	  
	         manageSub2.addPreference(mButtonDataRoam2);
	         manageSub2.addPreference(mButtonDataEnabled2);
	         manageSub2.addPreference(mButtonAPNExpand2);
	         manageSub2.addPreference(mButtonPreferredNetworkMode4);	  
		 } else {
	         manageSub1.addPreference(mButtonDataRoam);
	         manageSub1.addPreference(mButtonDataEnabled);
	         manageSub1.addPreference(mButtonAPNExpand1);
	         manageSub1.addPreference(mButtonPreferredNetworkMode2);	  
	         manageSub2.removePreference(mButtonDataRoam2);
	         manageSub2.removePreference(mButtonDataEnabled2);
	         manageSub2.removePreference(mButtonAPNExpand2);
	         manageSub2.removePreference(mButtonPreferredNetworkMode4);	 	         
		 }    
         
     }
     
     private boolean isSimEnable(int sub) {
	   	boolean simSubState = true;
     	SharedPreferences sharedPreferences = getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
 		SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, sub);
        if(simInfo != null) {
         	simSubState = sharedPreferences.getBoolean(String.valueOf(simInfo.mSimId), true);
        } 
     	boolean cardReady = MSimTelephonyManager.getDefault().getSimState(sub) == TelephonyManager.SIM_STATE_READY; 
     	boolean result = cardReady && simSubState;
        if (DBG) log("isSimEnable sub = " + sub + " result = " + result);
     	return result;
     }
     
     protected void updateMultiSimEntriesForData() {
//         mData.setEntries(entries);
//         mData.setEntryValues(entryValues);
         updateDataSummary();
     }
     

     private void updatePrioritySubState() {
//         mPrioritySub.setEntries(entries);
//         mPrioritySub.setEntryValues(entryValues);

         try {
             int priorityValue = Settings.Global.getInt(getContentResolver(),
                     Settings.Global.MULTI_SIM_PRIORITY_SUBSCRIPTION);
//             mPrioritySub.setValue(Integer.toString(priorityValue));
             mPrioritySub.auroraSetArrowText(summaries[priorityValue]);
             mPrioritySubValue = priorityValue;

         } catch (SettingNotFoundException snfe) {
             Log.e(LOG_TAG, "Settings Exception Reading Dual Sim Priority Subscription Values");
         }
     }
     
     private void updateDataSummary() {
         int dataSub = MSimPhoneFactory.getDataSubscription();

         Log.d(LOG_TAG, "updateDataSummary: Data Subscription : = " + dataSub);
//         mData.setValue(Integer.toString(dataSub));
         mData.auroraSetArrowText(summaries[dataSub]);
     }
     
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
     
     private Handler mMobileHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             AsyncResult ar;

             switch(msg.what) {
                 case EVENT_SET_DATA_SUBSCRIPTION_DONE:
                     Log.d(LOG_TAG, "EVENT_SET_DATA_SUBSCRIPTION_DONE");
                     if (mIsForeground) {
                         dismissDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
                     }
                     getPreferenceScreen().setEnabled(true);
                     updateDataSummary();
                 	SharedPreferences prefs = PhoneGlobals.getInstance().getApplicationContext().getSharedPreferences("com.android.phone_preferences", Context.MODE_PRIVATE);  
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("aurora_data_slot", MSimPhoneFactory.getDataSubscription());
                    editor.commit();

                     ar = (AsyncResult) msg.obj;
                     String status;
                     if (ar.exception != null) {
                         status = getResources().getString(R.string.set_dds_error)
                                            + " " + ar.exception.getMessage();
                         displayAlertDialog(status);
                         break;
                     }

                     boolean result = (Boolean)ar.result;

                     Log.d(LOG_TAG, "SET_DATA_SUBSCRIPTION_DONE: result = " + result);
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

                 case EVENT_SET_VOICE_SUBSCRIPTION:
                     updateVoiceSub(msg.arg1);
                     break;
                 case EVENT_SET_VOICE_SUBSCRIPTION_DONE:
                     Log.d(LOG_TAG, "EVENT_SET_VOICE_SUBSCRIPTION_DONE");
                     ar = (AsyncResult) msg.obj;
                     String sub;
                     if (ar.exception != null) {
                         Log.e(LOG_TAG, "SET_VOICE_SUBSCRIPTION_DONE: returned Exception: "
                                 + ar.exception);
                         int voiceSub = MSimPhoneFactory.getVoiceSubscription();
                         mVoiceSub = voiceSub;
                         break;
                     }
                     sub = Integer.toString(mVoiceSub);
                     MSimPhoneFactory.setVoiceSubscription(mVoiceSub);
                     break;           
               
                 case EVENT_SET_PRIORITY_SUBSCRIPTION:
                     updatePrioritySub(msg.arg1);
                     break;
                 case EVENT_SET_PRIORITY_SUBSCRIPTION_DONE:
                     ar = (AsyncResult) msg.obj;
                     if (ar.exception != null) {
                         Log.e(LOG_TAG, "EVENT_SET_PRIORITY_SUBSCRIPTION_DONE: returned Exception: "
                                 + ar.exception);
                         updatePrioritySubState();
                         break;
                     }
                     Log.d(LOG_TAG, "EVENT_SET_PRIORITY_SUBSCRIPTION_DONE : mPrioritySubValue "
                             + mPrioritySubValue);
//                     mPrioritySub.setValue(Integer.toString(mPrioritySubValue));
//                     mPrioritySub.setSummary(summaries[mPrioritySubValue]);
                     mPrioritySub.auroraSetArrowText(summaries[mPrioritySubValue]);
                     MSimPhoneFactory.setPrioritySubscription(mPrioritySubValue);
                     break;                   
                 default:
                         Log.w(LOG_TAG, "Unknown Event " + msg.what);
                         break;
             }
         }
     };
     
     private void updateVoiceSub(int subIndex) {
         Log.d(LOG_TAG, "updateVoiceSub change voice sub to: " + subIndex);
         Message setVoiceSubMsg = Message.obtain(mMobileHandler,
                 EVENT_SET_VOICE_SUBSCRIPTION_DONE, null);
         mPhone.setDefaultVoiceSub(subIndex, setVoiceSubMsg);
     }
     
     private void updatePrioritySub(int priorityIndex) {
         Log.d(LOG_TAG, "updatePrioritySub change priority sub to: " + priorityIndex);
         Message setPrioritySubMsg = Message.obtain(mMobileHandler,
                 EVENT_SET_PRIORITY_SUBSCRIPTION_DONE, null);
         mPhone.setPrioritySub(priorityIndex, setPrioritySubMsg);
     }
     
     protected boolean mIsForeground = false;
     private int mPrioritySubValue = 0;
     private int mVoiceSub = 0;
     SubscriptionManager mSubscriptionManager;
     private static final int DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS = 100;
     
     @Override
     protected Dialog onCreateDialog(int id) {
         if (id == DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS) {
             AuroraProgressDialog dialog = new AuroraProgressDialog(this);
             dialog.setMessage(getResources().getString(R.string.set_data_subscription_progress));
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
         } 
     }
     
     void displayAlertDialog(String msg) {
         if (!mIsForeground) {
             Log.d(LOG_TAG, "The activitiy is not in foreground. Do not display dialog!!!");
             return;
         }
         Log.d(LOG_TAG, "displayErrorDialog!" + msg);
         new AuroraAlertDialog.Builder(this).setMessage(msg)
                .setTitle(android.R.string.dialog_alert_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, this)
                .show()
                .setOnDismissListener(this);
         }
     
     private void   handleAuroraDataSwitch(boolean isChecked) {    	 
    	 
         ConnectivityManager cm =
                 (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	 if(isChecked == cm.getMobileDataEnabled()) {
  			if (DBG) log("onPreferenceChange: preference = mButtonDataEnabled. return");
    		 return ;
    	 }       
         int sub = MSimPhoneFactory.getDataSubscription();
         if (DBG) log("onPreferenceChange: preference == mButtonDataEnabled. sub = " + sub + " isChecked = " + isChecked);
		if (PhoneUtils.isMultiSimEnabled()) {
			android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
					android.provider.Settings.Global.MOBILE_DATA + 0,
					isChecked ? 1 : 0);
			android.provider.Settings.Global.putInt(PhoneGlobals.getInstance().getContentResolver(),
					android.provider.Settings.Global.MOBILE_DATA + 1 ,
					isChecked ? 1 : 0);
		}
		if(DeviceUtils.is8910()) {
			if(!((MSimGSMPhone)((PhoneProxy)mPhone).getActivePhone()).mDcTracker.isApnTypeEnabled(PhoneConstants.APN_TYPE_DEFAULT)) {
				mPhone.enableApnType(PhoneConstants.APN_TYPE_DEFAULT);
			}
		}
//         ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
         cm.setMobileDataEnabled(isChecked);
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
	                        //press home
	                    	finish();
	                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
	                        //long press home
	                    }
	                }
	            }
	        }
	    }
	    
	    protected void onDestroy () {
	        super.onDestroy();
	        unregisterReceiver(mHomeRecevier);
	    }
	    
	    private void handlePreferSub(int prioritySubIndex) {
            Log.d(LOG_TAG, "handlePreferSub::::  prioritySubIndex =" + prioritySubIndex );
            if (mSubscriptionManager.getCurrentSubscription(prioritySubIndex).subStatus
                    == SubscriptionStatus.SUB_ACTIVATED) {
                mPrioritySubValue = prioritySubIndex;
                mMobileHandler.sendMessage(mMobileHandler.obtainMessage(EVENT_SET_PRIORITY_SUBSCRIPTION,
                        prioritySubIndex, 0));                
                
                //voice
                MSimPhoneFactory.setPromptEnabled(false);
                mVoiceSub = prioritySubIndex;
                mMobileHandler.sendMessage(mMobileHandler.obtainMessage(EVENT_SET_VOICE_SUBSCRIPTION,
                		prioritySubIndex, 0));
                
                //sms
                MSimPhoneFactory.setSMSSubscription(prioritySubIndex);
                
            } else {
                String status = getResources().getString(R.string.set_priority_sub_error);
                displayAlertDialog(status);
            }
        
	    }
	    
	    private void handlePreferDataSub(int prioritySubIndex) {
            Log.d(LOG_TAG, "handlePreferDataSub::::  prioritySubIndex =" + prioritySubIndex);
            ((MSimPhoneGlobals)MSimPhoneGlobals.getInstance()).isUserSwitchData = true;
            if (mIsForeground) {
           	 	showDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
            }					
            Message setDdsMsg = Message.obtain(mMobileHandler, EVENT_SET_DATA_SUBSCRIPTION_DONE, null);
            mSubscriptionManager.setDataSubscription(prioritySubIndex, setDdsMsg);
		}
}
