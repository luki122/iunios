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
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import com.android.phone.AuroraTelephony.SimInfo;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.internal.telephony.TelephonyIntents;
import android.database.ContentObserver;
import com.mediatek.telephony.TelephonyManagerEx;
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
        implements AuroraPreference.OnPreferenceChangeListener{

    // debug data
    private static final String LOG_TAG = "MSimMobileNetworkSettings";
    private static final boolean DBG = true;

    //String keys for preference lookup
    private static final String KEY_DATA = "data";
    private static final String PRIORITY_SUB = "priority_subscription";

    private AuroraListPreference mData;
    private AuroraListPreference mPrioritySub;
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
        

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.mtk_network_setting);

//        mPhone = ((MSimPhoneGlobals)PhoneGlobals.getInstance()).getDefaultPhone();
        mPhone = PhoneGlobals.getInstance().getPhone(0);
        mPhone2 = PhoneGlobals.getInstance().getPhone(1);
        //get UI object references
        AuroraPreferenceScreen prefSet = getPreferenceScreen();       
        
        mData = (AuroraListPreference) findPreference(KEY_DATA);
        mData.setOnPreferenceChangeListener(this);
        mPrioritySub = (AuroraListPreference) findPreference(PRIORITY_SUB);
        mPrioritySub.setOnPreferenceChangeListener(this);
        mAuroraButtonDataEnabled = (AuroraSwitchPreference) prefSet.findPreference("aurora_data_enabled_key");
        mAuroraButtonDataEnabled.setOnPreferenceChangeListener(this);        
        int numPhones = PhoneUtils.getPhoneCount();
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


		AuroraPreference dataSummary = prefSet.findPreference("sim_data_support_summary_key");
		dataSummary.setEnabled(false);

//		if (!DeviceUtils.isSupportDualData()) {
//			AuroraPreferenceCategory dataPreferenceCategory = (AuroraPreferenceCategory) findPreference("sim_data_support_key");
//			dataPreferenceCategory.removePreference(mData);
//			dataPreferenceCategory.removePreference(dataSummary);
//		}

		IntentFilter homeIf = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mHomeRecevier, homeIf);
		 mHandler = new Handler();
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
        
        mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        
        
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
        
        
        IntentFilter intentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mUiReceiver, intentFilter);
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.GPRS_CONNECTION_SIM_SETTING),
                false, mGprsDefaultSIMObserver);
        
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
        getContentResolver().unregisterContentObserver(mGprsDefaultSIMObserver);
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
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
        int dataSub = getDataSub();
	   	if(!isSimEnable(dataSub)) {
			 return;
		}      
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
    
    private Handler mHandler;
     private void updateUiState() {
    	boolean card1Ready = TelephonyManagerEx.getDefault().getSimState(0) == TelephonyManager.SIM_STATE_READY;
    	boolean card2Ready = TelephonyManagerEx.getDefault().getSimState(1) == TelephonyManager.SIM_STATE_READY;
        if (DBG) log("updateUiState:  card1Ready=" + card1Ready + " card2Ready=" + card2Ready);

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);  
        
        boolean iscard1Enable = isSimEnable(0);
        boolean iscard2Enable = isSimEnable(1);
       	
    	 if (DeviceUtils.isSupportDualData()) {
    	    mAuroraButtonDataEnabled.setEnabled(isSimEnable(0) || isSimEnable(1));
    	 } else {
 	       	mAuroraButtonDataEnabled.setEnabled(isSimEnable(0)); 
    	 }

        mAuroraButtonDataEnabled.setChecked(cm.getMobileDataEnabled());
        mData.setEnabled(iscard1Enable && iscard2Enable);
        mPrioritySub.setEnabled(iscard1Enable && iscard2Enable);
     }
     
     
     
     private BroadcastReceiver mUiReceiver = new UiBroadcastReceiver();
     
     private class UiBroadcastReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             Log.v(LOG_TAG,"Action intent recieved:"+action);
             if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            	 updateUiState();
             }
         }
     }         
     
     
     public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
         String status;
         if (preference == mData) {
             int dataSlot = Integer.parseInt((String) objValue);
	 		  SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), dataSlot);
	 		  long simId = -1;
	 		  if(simInfo != null) {
	 			 simId = simInfo.mSimId;
	 		  }
             Log.d(LOG_TAG, "setDataSubscription " + dataSlot + " simId = " + simId);
			if (mIsForeground) {
				getPreferenceScreen().setEnabled(false);
				   if(mProgressDialog != null) {
			 			  mProgressDialog.dismiss();
			 			  mProgressDialog = null;
			 		   }
				mProgressDialog = new AuroraProgressDialog(this);
				mProgressDialog.setMessage(getResources().getString(R.string.set_data_subscription_progress));
				mProgressDialog.setCancelable(false);
				mProgressDialog.setIndeterminate(true);
				mProgressDialog.show();
			}

             handleSwitchData(simId);
         } else if (preference == mPrioritySub) {
             int prioritySubIndex = Integer.parseInt((String) objValue);
             Log.d(LOG_TAG, "onPreferenceChange::::  prioritySubIndex =" + prioritySubIndex );
             mPrioritySubValue = prioritySubIndex;
             handleSetSub(mPrioritySubValue);
                 
             
         } else if(preference == mAuroraButtonDataEnabled) {
             boolean isChecked = (Boolean)objValue;
             handleAuroraDataSwitch(isChecked);
         }

         return true;
     }
     
     
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
         cm.setMobileDataEnabled(isChecked);
     }
     
     
     private boolean isSimEnable(int sub) {	   
     	boolean cardReady = TelephonyManagerEx.getDefault().getSimState(sub) == TelephonyManager.SIM_STATE_READY; 
        if (DBG) log("isSimEnable sub = " + sub + " result = " + cardReady);
     	return cardReady;
     }
     
     protected void updateMultiSimEntriesForData() {
         mData.setEntries(entries);
         mData.setEntryValues(entryValues);
         updateDataSummary();
     }
     

     private void updatePrioritySubState() {
         mPrioritySub.setEntries(entries);
         mPrioritySub.setEntryValues(entryValues);
         int priorityValue = PhoneGlobals.getInstance().getVoiceSubscription();
         mPrioritySub.setValue(Long.toString(priorityValue));
         if(priorityValue != -1) {
        	 mPrioritySub.auroraSetArrowText(summaries[priorityValue]);
         } else {
        	 mPrioritySub.auroraSetArrowText("");
         }
         mPrioritySubValue = (int)priorityValue;
     }
     
     private void updateDataSummary() {
         int dataSub = getDataSub();

         Log.d(LOG_TAG, "updateDataSummary: Data Subscription : = " + dataSub);
         mData.setValue(Integer.toString(dataSub));
         mData.auroraSetArrowText(summaries[dataSub]);
     }
     
     protected boolean mIsForeground = false;
     private int mPrioritySubValue = 0;
     
     private void  handleAuroraDataSwitch(boolean isChecked) {    	 
    	 
         ConnectivityManager cm =
                 (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	 if(isChecked == cm.getMobileDataEnabled()) {
  			if (DBG) log("onPreferenceChange: preference = mButtonDataEnabled. return");
    		 return ;
    	 }       
         if (DBG) log("onPreferenceChange: preference == mButtonDataEnabled. isChecked = " + isChecked);
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
	 	   if(mProgressDialog != null) {
   			  mProgressDialog.dismiss();
   			  mProgressDialog = null;
   		   }
	        unregisterReceiver(mHomeRecevier);
	    }

	    
	    private void handleSetSub(int prioritySubIndex){
			log("handleSetSub sub =" + prioritySubIndex);
			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), prioritySubIndex);
			long simId = -1;
			if (simInfo == null) {
				return;
			} else {
				simId = simInfo.mSimId;
			}			
            Settings.System.putLong(getContentResolver(),Settings.System.VOICE_CALL_SIM_SETTING, simId);
            Intent intent = new Intent(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            intent.putExtra("simid", simId);
            sendBroadcast(intent);
            
            Settings.System.putLong(getContentResolver(),
                    Settings.System.SMS_SIM_SETTING,  simId);
            intent = new Intent(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED);
            intent.putExtra("simid", simId);
            sendBroadcast(intent);
            
            mPrioritySub.setValue(Integer.toString(mPrioritySubValue));
            mPrioritySub.auroraSetArrowText(summaries[mPrioritySubValue]);
	    }
	    
	    
	    private void handleSwitchData(long simId) {
              switchGprsDefautlSIM(simId);
	    }
	    
	    private void switchGprsDefautlSIM(long simid) {
	        log("switchGprsDefautlSIM() with simid=" + simid);
	        if (simid < 0) {
	            return;
	        }
	        boolean isConnect = (simid > 0) ? true : false;
            long curConSimId = Settings.System.getLong(getContentResolver(),
                Settings.System.GPRS_CONNECTION_SIM_SETTING,
                Settings.System.DEFAULT_SIM_NOT_SET);
            log("curConSimId=" + curConSimId);
            if (simid == curConSimId) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
            intent.putExtra("simid", simid);
            // simid>0 means one of sim card is selected
            // and <0 is close id which is -1 so mean disconnect
            sendBroadcast(intent);    
	    }
	    
	    private ContentObserver mGprsDefaultSIMObserver = new ContentObserver(
	            new Handler()) {
	        @Override
	        public void onChange(boolean selfChange) {
                Log.d(LOG_TAG, "data sub onChange");
         	   if(mProgressDialog != null) {
      			  mProgressDialog.dismiss();
      			  mProgressDialog = null;
      		   }
                getPreferenceScreen().setEnabled(true);
                updateDataSummary();
                String status = getResources().getString(R.string.set_dds_success);
                Toast toast = Toast.makeText(getApplicationContext(), status,
                        Toast.LENGTH_LONG);
                toast.show();     
                updateUiState();
	        }
	    };
	    
	 	   AuroraProgressDialog mProgressDialog = null;
	 	   
	 	   private int getDataSub(){
	 	        return PhoneGlobals.getInstance().getDataSubscription();
	 	   }
}
