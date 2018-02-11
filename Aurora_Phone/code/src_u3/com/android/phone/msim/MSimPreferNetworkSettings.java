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
import android.content.SharedPreferences;
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
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.MSimTelephonyManager;


import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import java.util.ArrayList;
import aurora.preference.*;
import aurora.widget.*;
import aurora.app.*;
import android.os.AsyncResult;
import static com.android.internal.telephony.MSimConstants.SUBSCRIPTION_KEY;

public class MSimPreferNetworkSettings extends AuroraPreferenceActivity implements
        AuroraPreference.OnPreferenceChangeListener {
    static final String TAG = "MSimPreferNetworkSettings";
    private static final boolean DBG = true;
       
    
//    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    static final int preferredNetworkMode = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE;
    private Phone mPhone;
    private MyHandler mHandler;
    PreNetPreference mRadio[];
    private int mSubscription;
    
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pre_net_settings);      
        getAuroraActionBar().setTitle(R.string.preferred_network_mode_title);
        mSubscription = getIntent().getIntExtra(SUBSCRIPTION_KEY, PhoneGlobals.getInstance().getDefaultSubscription());
        log("onCreate subscription :" + mSubscription);
        
        mPhone = PhoneGlobals.getInstance().getPhone(mSubscription);
        mHandler = new MyHandler();
        
        mRadio = new PreNetPreference[4];
        AuroraPreferenceScreen prefSet = (AuroraPreferenceScreen)getPreferenceScreen();
        mRadio[0] = (PreNetPreference) prefSet.findPreference("pre_net_3p");
        mRadio[1] = (PreNetPreference) prefSet.findPreference("pre_net_2");
        mRadio[2] = (PreNetPreference) prefSet.findPreference("pre_net_3");
        mRadio[3] = (PreNetPreference) prefSet.findPreference("pre_net_4p");
        mRadio[0].setOnPreferenceChangeListener(this);
        mRadio[1].setOnPreferenceChangeListener(this);
        mRadio[2].setOnPreferenceChangeListener(this);
        mRadio[3].setOnPreferenceChangeListener(this);
        int settingsNetworkMode = getPreferredNetworkMode(mSubscription);
        mRadio[nettype2index(settingsNetworkMode)].setChecked();       
		prefSet.removePreference(mRadio[2]);
        
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
    	for(int i= 0;i<4;i++) {
    		if(PreNetPreference.getSelectedKey().equalsIgnoreCase(mRadio[i].getKey())) {
    			buttonNetworkMode = index2nettype(i);   			
    			break;
    		}
    	}
        int settingsNetworkMode = getPreferredNetworkMode(mSubscription);
        log ("onPreferenceChange: buttonNetworkMode = " +
        		buttonNetworkMode + " settingsNetworkMode=" + settingsNetworkMode);
        if (buttonNetworkMode != settingsNetworkMode) {
            int modemNetworkMode;
            if(buttonNetworkMode >= 0 && buttonNetworkMode <=22) {
          	  modemNetworkMode = buttonNetworkMode;
          } else {
        	  Log.i(TAG, "Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
              return true;
          }

            setPreferredNetworkMode(buttonNetworkMode,mSubscription);
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

                int settingsNetworkMode = getPreferredNetworkMode(mSubscription);

                if (DBG) {
                    log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                            settingsNetworkMode);
                }

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
                        setPreferredNetworkMode(settingsNetworkMode, mSubscription);
                    }

                    mRadio[nettype2index(modemNetworkMode)].setChecked();
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
            	int networkMode = preferredNetworkMode;
            	for(int i= 0;i<4;i++) {
            		if(PreNetPreference.getSelectedKey().equalsIgnoreCase(mRadio[i].getKey())) {
            			networkMode = index2nettype(i);
            		}
            	}
                log ("handleSetPreferredNetworkTypeResponse: networkMode = " +
                		networkMode);
                setPreferredNetworkMode(networkMode, mSubscription);
                SharedPreferences sp = PhoneGlobals.getInstance().getApplicationContext().getSharedPreferences("last_nettype", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor =  sp.edit();
        		editor.putInt("nettype" + mSubscription, networkMode);
        		editor.commit();
            } else {
                mPhone.getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE));
            }
        }

        private void resetNetworkModeToDefault() {
            //set the mButtonPreferredNetworkMode
            mRadio[nettype2index(preferredNetworkMode)].setChecked();
            //set the Settings.System
            setPreferredNetworkMode(preferredNetworkMode, mSubscription);
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
    
    private int nettype2index(int modemNetworkMode) {
        if(modemNetworkMode == Constants.NETWORK_MODE_GSM_ONLY) {  
            return 1;
        } else if(modemNetworkMode == Constants.NETWORK_MODE_TD_SCDMA_WCDMA) {
            return 2;     	  
        } else if(modemNetworkMode == Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA) {
            return 0;     	  
        } else {
            return 3;
        }
    }
    
    private int index2nettype(int index) {
    	
        log("index2nettype index = " + index);
			int buttonNetworkMode = index;
			if(index == 3) {
				buttonNetworkMode = preferredNetworkMode;
			} else if(index == 2) {
				buttonNetworkMode = Constants.NETWORK_MODE_TD_SCDMA_WCDMA;
			} else if(index == 0) {
				buttonNetworkMode = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA;
			}
			return buttonNetworkMode;		
    }

}