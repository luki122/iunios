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

package com.android.phone;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;


import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import java.util.ArrayList;
import aurora.preference.*;
import aurora.widget.*;
import aurora.app.*;
import android.os.AsyncResult;

public class PreferNetworkSettings extends AuroraPreferenceActivity implements
        AuroraPreference.OnPreferenceChangeListener {
    static final String TAG = "PreferNetworkSettings";
    private static final boolean DBG = true;
       
    
//    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    static final int preferredNetworkMode = 0;
    private Phone mPhone;
    private MyHandler mHandler;
    PreNetPreference mRadio[];
    
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pre_net_settings);      
        getAuroraActionBar().setTitle(R.string.preferred_network_mode_title);
        mPhone = PhoneGlobals.getPhone();
        mHandler = new MyHandler();
        
        mRadio = new PreNetPreference[3];
        AuroraPreferenceScreen prefSet = (AuroraPreferenceScreen)getPreferenceScreen();
        mRadio[0] = (PreNetPreference) prefSet.findPreference("pre_net_3p");
        mRadio[1] = (PreNetPreference) prefSet.findPreference("pre_net_2");
        mRadio[2] = (PreNetPreference) prefSet.findPreference("pre_net_3");
        mRadio[0].setOnPreferenceChangeListener(this);
        mRadio[1].setOnPreferenceChangeListener(this);
        mRadio[2].setOnPreferenceChangeListener(this);
        PreNetPreference radio4 = (PreNetPreference) prefSet.findPreference("pre_net_4p");
        if(radio4 != null) {
			prefSet.removePreference(radio4);
        }
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                mPhone.getContext().getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                preferredNetworkMode);
        //aurora modify liguangyu 20140331 for bug 3821 start
        if(settingsNetworkMode <= 2 && settingsNetworkMode >= 0) {
            mRadio[settingsNetworkMode].setChecked();
        } else {
        	//aurora add liguangyu 20140424 for BUG #4556 start
            mRadio[preferredNetworkMode].setChecked();
            //aurora add liguangyu 20140424 for BUG #4556 end
        }
        //aurora modify liguangyu 20140331 for bug 3821 end
        
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(
                Context.TELEPHONY_SERVICE);
        if(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
        	    String mccmnc = telephonyManager.getSubscriberId();
        		if(mccmnc != null 
        				&& (mccmnc.startsWith("46000") ||mccmnc.startsWith("46002")||mccmnc.startsWith("46003"))) {
        			prefSet.removePreference(mRadio[2]);
            	}
        	
        }
        
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().setEnabled(true);
        mPhone.getPreferredNetworkType(mHandler.obtainMessage(
                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE));
        
    }

  	
    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        Log.d(TAG, "onPreferenceChange(): Preference - " + preference
                + ", newValue - " + objValue + ", newValue type - "
                + objValue.getClass());
       
        int buttonNetworkMode = preferredNetworkMode;
    	for(int i= 0;i<3;i++) {
    		if(PreNetPreference.getSelectedKey().equalsIgnoreCase(mRadio[i].getKey())) {
    			buttonNetworkMode = i;
    			break;
    		}
    	}
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                mPhone.getContext().getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE, preferredNetworkMode);
        log ("onPreferenceChange: buttonNetworkMode = " +
        		buttonNetworkMode + " settingsNetworkMode=" + settingsNetworkMode);
        if (buttonNetworkMode != settingsNetworkMode) {
            int modemNetworkMode;
            // if new mode is invalid ignore it
//            switch (buttonNetworkMode) {
//                case Phone.NT_MODE_WCDMA_PREF:
//                case Phone.NT_MODE_GSM_ONLY:
//                case Phone.NT_MODE_WCDMA_ONLY:
//                case Phone.NT_MODE_GSM_UMTS:
//                case Phone.NT_MODE_CDMA:
//                case Phone.NT_MODE_CDMA_NO_EVDO:
//                case Phone.NT_MODE_EVDO_NO_CDMA:
//                case Phone.NT_MODE_GLOBAL:
//                case Phone.NT_MODE_LTE_ONLY:
//                    modemNetworkMode = buttonNetworkMode;
//                    break;
//                default:
//                    Log.i(TAG, "Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
//                    return true;
//            }
            if(buttonNetworkMode >= 0 && buttonNetworkMode <=22) {
          	  modemNetworkMode = buttonNetworkMode;
          } else {
        	  Log.i(TAG, "Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
              return true;
          }

//            UpdatePreferredNetworkModeSummary(buttonNetworkMode);

            android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                    buttonNetworkMode );
            //Set the modem network mode
            log ("onPreferenceChange: modemNetworkMode = " +
                    modemNetworkMode);
            mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                    .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
        }
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

                //check that modemNetworkMode is from an accepted value
//                if (modemNetworkMode == Phone.NT_MODE_WCDMA_PREF ||
//                        modemNetworkMode == Phone.NT_MODE_GSM_ONLY ||
//                        modemNetworkMode == Phone.NT_MODE_WCDMA_ONLY ||
//                        modemNetworkMode == Phone.NT_MODE_GSM_UMTS ||
//                        modemNetworkMode == Phone.NT_MODE_CDMA ||
//                        modemNetworkMode == Phone.NT_MODE_CDMA_NO_EVDO ||
//                        modemNetworkMode == Phone.NT_MODE_EVDO_NO_CDMA ||
//                        modemNetworkMode == Phone.NT_MODE_GLOBAL ||
//                     //   modemNetworkMode == Phone.NT_MODE_LTE_CDMA_AND_EVDO ||//aurora change zhouxiaobing 20131016
//                      //  modemNetworkMode == Phone.NT_MODE_LTE_GSM_WCDMA ||
//                      //  modemNetworkMode == Phone.NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA ||
//                        modemNetworkMode == Phone.NT_MODE_LTE_ONLY// ||
//                       /* modemNetworkMode == Phone.NT_MODE_LTE_WCDMA*/) {
                if(modemNetworkMode >= 0 && modemNetworkMode <=22) {
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

//                    UpdatePreferredNetworkModeSummary(modemNetworkMode);
                    // changes the mButtonPreferredNetworkMode accordingly to modemNetworkMode
//                    mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                    //aurora modify liguangyu 20140331 for bug 3821 start
                    if(modemNetworkMode <= 2 && modemNetworkMode >= 0) {
                        mRadio[modemNetworkMode].setChecked();
                    }
                    //aurora modify liguangyu 20140331 for bug 3821 end
                } else {
                    if (DBG) log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                    resetNetworkModeToDefault();
                }
            }
        }

        private void handleSetPreferredNetworkTypeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            log ("handleSetPreferredNetworkTypeResponse: enter ");
            if (ar.exception == null) {            	
//                int networkMode = Integer.valueOf(
//                        mButtonPreferredNetworkMode.getValue()).intValue();
            	int networkMode = preferredNetworkMode;
            	for(int i= 0;i<3;i++) {
            		if(PreNetPreference.getSelectedKey().equalsIgnoreCase(mRadio[i].getKey())) {
            			networkMode = i;
            			break;
            		}
            	}
                log ("handleSetPreferredNetworkTypeResponse: networkMode = " +
                		networkMode);
                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        networkMode );
            } else {
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            }
        }

        private void resetNetworkModeToDefault() {
            //set the mButtonPreferredNetworkMode
//            mButtonPreferredNetworkMode.setValue(Integer.toString(preferredNetworkMode));
            mRadio[preferredNetworkMode].setChecked();
            //set the Settings.System
            android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        preferredNetworkMode );
            //Set the Modem
            mPhone.setPreferredNetworkType(preferredNetworkMode,
                    this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
        }
    
    }
    
    
    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }

    

    

   
    
   

}