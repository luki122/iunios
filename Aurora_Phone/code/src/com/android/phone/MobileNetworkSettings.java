/*
 * Copyright (C) 2006 The Android Open Source Project
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

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
//aurora add liguangyu 20131102 for Settings Theme start
import aurora.widget.AuroraActionBar;
import aurora.preference.*;
import aurora.app.*;
//aurora add liguangyu 20131102 for Settings Theme end
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
/**
 * "Mobile network settings" screen.  This preference screen lets you
 * enable/disable mobile data, and control data roaming and other
 * network-specific mobile data features.  It's used on non-voice-capable
 * tablets as well as regular phone devices.
 *
 * Note that this PreferenceActivity is part of the phone app, even though
 * you reach it from the "Wireless & Networks" section of the main
 * Settings app.  It's not part of the "Call settings" hierarchy that's
 * available from the Phone app (see CallFeaturesSetting for that.)
 */
public class MobileNetworkSettings extends AuroraPreferenceActivity
        implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, AuroraPreference.OnPreferenceChangeListener{

    // debug data
    private static final String LOG_TAG = "NetworkSettings";
    private static final boolean DBG = true;
    public static final int REQUEST_CODE_EXIT_ECM = 17;

    //String keys for preference lookup
    private static final String BUTTON_DATA_ENABLED_KEY = "button_data_enabled_key";
    private static final String BUTTON_DATA_USAGE_KEY = "button_data_usage_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE2 = "preferred_network_mode_key2";
    private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
    private static final String BUTTON_CDMA_LTE_DATA_SERVICE_KEY = "cdma_lte_data_service_key";

    static int preferredNetworkMode = Constants.PREFERRED_NETWORK_MODE;

    //Information about logical "up" Activity
    private static final String UP_ACTIVITY_PACKAGE = "com.android.settings";
    private static final String UP_ACTIVITY_CLASS =
            "com.android.settings.Settings$WirelessSettingsActivity";

    //UI objects
    private AuroraListPreference mButtonPreferredNetworkMode;
    private AuroraPreference mButtonPreferredNetworkMode2;
    private AuroraSwitchPreference mButtonDataRoam;
    private AuroraSwitchPreference mButtonDataEnabled;
    private AuroraPreference mLteDataServicePref;

    private AuroraPreference mButtonDataUsage;
//    private DataUsageListener mDataUsageListener;//aurora change zhouxiaobing 20131202
    private static final String iface = "rmnet0"; //TODO: this will go away

    private Phone mPhone;
    private MyHandler mHandler;
    private boolean mOkClicked;

    //GsmUmts options and Cdma options
    GsmUmtsOptions mGsmUmtsOptions;
    CdmaOptions mCdmaOptions;

    private AuroraPreference mClickedPreference;


    //This is a method implemented for DialogInterface.OnClickListener.
    //  Used to dismiss the dialogs when they come up.
    public void onClick(DialogInterface dialog, int which) {
    	   Log.d("dataRoamSwitchPreference", "onClick");
        if (which == DialogInterface.BUTTON_POSITIVE) {
        	Log.d("dataRoamSwitchPreference", "true");
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
//    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        /** TODO: Refactor and get rid of the if's using subclasses */
        if (mGsmUmtsOptions != null &&
                mGsmUmtsOptions.preferenceTreeClick(preference) == true) {
            return true;
//        } else if (mCdmaOptions != null &&
//                   mCdmaOptions.preferenceTreeClick(preference) == true) {
//            if (Boolean.parseBoolean(
//                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
//
//                mClickedPreference = preference;
//
//                // In ECM mode launch ECM app dialog
//                startActivityForResult(
//                    new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
//                    REQUEST_CODE_EXIT_ECM);
//            }
//            return true;
        } else if (preference == mButtonPreferredNetworkMode) {
            //displays the value taken from the Settings.System
            int settingsNetworkMode = android.provider.Settings.Global.getInt(mPhone.getContext().
                    getContentResolver(), android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                    preferredNetworkMode);
            mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
            return true;
        } else if (preference == mButtonDataRoam) {
            if (DBG) log("onPreferenceTreeClick: preference == mButtonDataRoam.");

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
                mPhone.setDataRoamingEnabled(false);
            }
            return true;
        } else if (preference == mButtonDataEnabled) {
            if (DBG) log("onPreferenceTreeClick: preference == mButtonDataEnabled.");
//            ConnectivityManager cm =
//                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            cm.setMobileDataEnabled(mButtonDataEnabled.isChecked());
 	       AuroraPlatformUtils.setMobileDataEnabled(mButtonDataEnabled.isChecked());
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
        } else {
            // if the button is anything but the simple toggle preference,
            // we'll need to disable all preferences to reject all click
            // events until the sub-activity's UI comes up.
            preferenceScreen.setEnabled(false);
            // Let the intents be launched by the Preference manager
            return false;
        }
    }
    
    AuroraAlertDialog mRoamingDialog = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.network_setting);
        
        //aurora change liguangyu 20131102 for Settings Theme start
        getAuroraActionBar().setTitle(R.string.mobile_networks);
        //aurora change liguangyu 20131102 for Settings Theme end

        mPhone = PhoneGlobals.getPhone();
        mHandler = new MyHandler();
        
        if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
        	preferredNetworkMode = Constants.NETWORK_MODE_GLOBAL;
        }
        //get UI object references
        AuroraPreferenceScreen prefSet = (AuroraPreferenceScreen)getPreferenceScreen();

        mButtonDataEnabled = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_DATA_ENABLED_KEY);
        mButtonDataEnabled.setOnPreferenceChangeListener(this);
        mButtonDataRoam = (AuroraSwitchPreference) prefSet.findPreference(BUTTON_ROAMING_KEY);
        mButtonDataRoam.setOnPreferenceChangeListener(this);
        mButtonPreferredNetworkMode = (AuroraListPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE);
        mButtonPreferredNetworkMode2 = (AuroraPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE2);
        mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId()),true);
        mButtonDataUsage = prefSet.findPreference(BUTTON_DATA_USAGE_KEY);
        mLteDataServicePref = prefSet.findPreference(BUTTON_CDMA_LTE_DATA_SERVICE_KEY);

        boolean isLteOnCdma = mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE;
        if (getResources().getBoolean(R.bool.world_phone) == true) {
            // set the listener for the mButtonPreferredNetworkMode list preference so we can issue
            // change Preferred Network Mode.
            mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);

            //Get the networkMode from Settings.System and displays it
            int settingsNetworkMode = android.provider.Settings.Global.getInt(mPhone.getContext().
                    getContentResolver(),android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                    preferredNetworkMode);
            mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
//            mCdmaOptions = new CdmaOptions(this, prefSet, mPhone);
            mGsmUmtsOptions = new GsmUmtsOptions(this, prefSet);
        } else {
//            if (!isLteOnCdma) {
//                prefSet.removePreference(mButtonPreferredNetworkMode);
//            }
            int phoneType = mPhone.getPhoneType();
            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
//                mCdmaOptions = new CdmaOptions(this, prefSet, mPhone);
                if (isLteOnCdma) {
                    mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
                    int settingsNetworkMode = android.provider.Settings.Global.getInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                            preferredNetworkMode);
                    mButtonPreferredNetworkMode.setValue(
                            Integer.toString(settingsNetworkMode));
                }

            } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
                mGsmUmtsOptions = new GsmUmtsOptions(this, prefSet);
                mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode);
                mButtonPreferredNetworkMode.setValue(
                        Integer.toString(settingsNetworkMode));
                mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId()),true);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }

        final boolean missingDataServiceUrl = TextUtils.isEmpty(
                android.provider.Settings.Global.getString(getContentResolver(),
                        android.provider.Settings.Global.SETUP_PREPAID_DATA_SERVICE_URL));
        if (!isLteOnCdma || missingDataServiceUrl) {
            prefSet.removePreference(mLteDataServicePref);
        } else {
            android.util.Log.d(LOG_TAG, "keep ltePref");
        }

//        ThrottleManager tm = (ThrottleManager) getSystemService(Context.THROTTLE_SERVICE);
//        mDataUsageListener = new DataUsageListener(this, mButtonDataUsage, prefSet);//aurora change zhouxiaobing 20131202

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA || !DeviceUtils.isIUNI()) {
//            mButtonPreferredNetworkMode.setEntries(R.array.cm_preferred_network_mode_choices);
//            mButtonPreferredNetworkMode.setEntryValues(R.array.cm_preferred_network_mode_values); 
        	if(DeviceUtils.isHonor6()) {
        	    mButtonPreferredNetworkMode.setEntries(R.array.h6_preferred_network_mode_choices);
                mButtonPreferredNetworkMode.setEntryValues(R.array.h6_preferred_network_mode_values); 
        	} else if(DeviceUtils.is9008v() || DeviceUtils.isI2()) {
        	    mButtonPreferredNetworkMode.setEntries(R.array.s9008v_preferred_network_mode_choices);
                mButtonPreferredNetworkMode.setEntryValues(R.array.s9008v_preferred_network_mode_values);         		
        	}
	        if(mButtonPreferredNetworkMode2 != null) {
	        	  prefSet.removePreference(mButtonPreferredNetworkMode2);
	        }
    	} else {
            if(mButtonPreferredNetworkMode != null) {
        	  prefSet.removePreference(mButtonPreferredNetworkMode);
          	}	
    	}
    }

    @Override
    protected void onResume() {
        super.onResume();

        // upon resumption from the sub-activity, make sure we re-enable the
        // preferences.
        // TODO: BUG: This will reenable all preferences, including ones that
        // are supposed to be disabled (operator selection button is one example)
        getPreferenceScreen().setEnabled(true);
        // TODO: Call this to redisable preferences due to bug above
        if (mGsmUmtsOptions != null) mGsmUmtsOptions.onResume();

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());

        // Set UI state in onResume because a user could go home, launch some
        // app to change this setting's backend, and re-launch this settings app
        // and the UI state would be inconsistent with actual state
        mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());

        if (getPreferenceScreen().findPreference(BUTTON_PREFERED_NETWORK_MODE2) != null || getPreferenceScreen().findPreference(BUTTON_PREFERED_NETWORK_MODE) != null)  {
            mPhone.getPreferredNetworkType(mHandler.obtainMessage(
                    MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE));
        }
//        mDataUsageListener.resume();//aurora change zhouxiaobing 20131202
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
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "onPause()... ");
        super.onPause();
        //aurora add liguangyu 20140424 for BUG #4517 start
        unregisterReceiver(mDataReceiver);
        if(mCheckDataConnectThread != null) {
        	mContinueChecking = false;
        	mCheckDataConnectThread = null;
        }
        //aurora add liguangyu 20140424 for BUG #4517 end
//        mDataUsageListener.pause();//aurora change zhouxiaobing 20131202
    }

    /**
     * Implemented to support onPreferenceChangeListener to look for preference
     * changes specifically on CLIR.
     *
     * @param preference is the preference to be changed, should be mButtonCLIR.
     * @param objValue should be the value of the selection, NOT its localized
     * display value.
     */
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        if (preference == mButtonPreferredNetworkMode) {
            //NOTE onPreferenceChange seems to be called even if there is no change
            //Check if the button value is changed from the System.Setting
            mButtonPreferredNetworkMode.setValue((String) objValue);
            int buttonNetworkMode;
            buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
            int settingsNetworkMode = android.provider.Settings.Global.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            if (buttonNetworkMode != settingsNetworkMode) {
                int modemNetworkMode;
                // if new mode is invalid ignore it
//                switch (buttonNetworkMode) {
//                    case NT_MODE_WCDMA_PREF:
//                    case NT_MODE_GSM_ONLY:
//                    case NT_MODE_WCDMA_ONLY:
//                    case NT_MODE_GSM_UMTS:
//                    case NT_MODE_CDMA:
//                    case NT_MODE_CDMA_NO_EVDO:
//                    case NT_MODE_EVDO_NO_CDMA:
//                    case NT_MODE_GLOBAL:
//                    case NT_MODE_LTE_CDMA_AND_EVDO://aurora change zhouxiaobing 20131016
//                    case NT_MODE_LTE_GSM_WCDMA:
//                    case NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
//                    case NT_MODE_LTE_ONLY:
//                    case NT_MODE_LTE_WCDMA:
//                        // This is one of the modes we recognize
//                        modemNetworkMode = buttonNetworkMode;
//                        break;
//                    default:
//                        loge("Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
//                        return true;
//                }
                if(buttonNetworkMode >= 0 && buttonNetworkMode <=22) {
                	  modemNetworkMode = buttonNetworkMode;
                } else {
                    loge("Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
                  return true;
                }

                UpdatePreferredNetworkModeSummary(buttonNetworkMode);

                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        buttonNetworkMode );
                //Set the modem network mode
                mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
            }
        } else if (preference == mButtonDataEnabled) {
            boolean isChecked = (Boolean)objValue;
            handleDataSwitch(isChecked);
        } else if (preference == mButtonDataRoam) {
            boolean isChecked = (Boolean)objValue;
            handleRoamSwitch(isChecked);
        }  

        // always let the preference setting proceed.
        return true;
    }

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

            if (ar.exception == null) {
                int modemNetworkMode = ((int[])ar.result)[0];

                if (DBG) {
                    log ("handleGetPreferredNetworkTypeResponse: modemNetworkMode = " +
                            modemNetworkMode);
                }

                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode);

                if (DBG) {
                    log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                            settingsNetworkMode);
                }

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
                    android.provider.Settings.Global.putInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                            settingsNetworkMode );
                }

                UpdatePreferredNetworkModeSummary(modemNetworkMode);
                // changes the mButtonPreferredNetworkMode accordingly to modemNetworkMode
                mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId()),true);

                //Update '2GOnly checkbox' based on recent preferred network type selection.
                Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                int networkMode = Integer.valueOf(
                        mButtonPreferredNetworkMode.getValue()).intValue();
                if (DBG) log("handleSetPreferredNetworkTypeResponse: networkMode = " + networkMode);
                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        networkMode );
                //Update '2GOnly checkbox' based on recent preferred network type selection.
                Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
                mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId()),true);

            } else {
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            }
        }
    }

    private void UpdatePreferredNetworkModeSummary(int NetworkMode) {
    	
    	if(DeviceUtils.is9008v() || DeviceUtils.isI2()) {
    		 switch(NetworkMode) {
	    		case NT_MODE_LTE_GSM_WCDMA:
	                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_4p);
	    			break;
	    		case NT_MODE_WCDMA_PREF:
	                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
	    			break;
	    		case NT_MODE_WCDMA_ONLY:
	                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_3);
	    			break;
	    		case NT_MODE_GSM_ONLY:
	                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_2);
	    			break;
	            default:
	                mButtonPreferredNetworkMode.setSummary(R.string.preferred_network_mode_global_summary);
    		 }
    		return;
    	} 
    	
     	if(DeviceUtils.isHonor6()) {
	   		 switch(NetworkMode) {
		    		case 1:
		                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_2);
		    			break;
		    		case 3:
		                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
		    			break;
		    		case 9:
		                mButtonPreferredNetworkMode.setSummary(R.string.aurora_preferred_network_mode_choices_4p);
		                break;
		            default:
		                mButtonPreferredNetworkMode.setSummary(R.string.preferred_network_mode_global_summary);
	   		 }
	   		return;
   	   } 
    	
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
        case REQUEST_CODE_EXIT_ECM:
            Boolean isChoiceYes =
                data.getBooleanExtra(EmergencyCallbackModeExitDialog.EXTRA_EXIT_ECM_RESULT, false);
            if (isChoiceYes) {
                // If the phone exits from ECM mode, show the CDMA Options
//                mCdmaOptions.showDialog(mClickedPreference);
            } else {
                // do nothing
            }
            break;

        default:
            break;
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, msg);
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
    
    
    private int getPreferredNetworkStringId() {
   	 int NetworkMode = android.provider.Settings.Global.getInt(
   	            PhoneGlobals.getInstance().getContentResolver(),
   	            android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
   	            preferredNetworkMode);	
        switch(NetworkMode) {
        		case 0:
        			return R.string.aurora_preferred_network_mode_choices_3p;        		
        		case 1:
        			return R.string.aurora_preferred_network_mode_choices_2;        			
        		default:
        			return R.string.aurora_preferred_network_mode_choices_3;        			
        }     
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
        mButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
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
  
    
    protected void onDestroy() {
        super.onDestroy();
        if (mRoamingDialog != null) {
            mRoamingDialog.dismiss();
            mRoamingDialog = null;
        }
    }
    
    private void handleDataSwitch(boolean isChecked) {
        if (DBG) log("handleDataSwitch: preference == mButtonDataEnabled. isChecked = " + isChecked);
//        ConnectivityManager cm =
//                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        cm.setMobileDataEnabled(isChecked);
        AuroraPlatformUtils.setMobileDataEnabled(isChecked);
    }
    
    private void handleRoamSwitch(boolean isChecked) {
        Log.d("handleRoamSwitch", "onCheckedChanged: preference == dataRoamSwitchPreference,ischeced= " + isChecked);
        if (isChecked) {
	      mOkClicked = false;
	      mRoamingDialog = new AuroraAlertDialog.Builder(MobileNetworkSettings.this).setMessage(
	    		  MobileNetworkSettings.this.getResources().getString(R.string.roaming_warning))
	              .setTitle(android.R.string.dialog_alert_title)
	              .setIconAttribute(android.R.attr.alertDialogIcon)
	              .setPositiveButton(android.R.string.yes, MobileNetworkSettings.this)
	              .setNegativeButton(android.R.string.no, MobileNetworkSettings.this)
	              .setOnDismissListener(MobileNetworkSettings.this)
	      		  .create();
	      mRoamingDialog.show();
        } else {
            mPhone.setDataRoamingEnabled(false);
        }
    }
}
