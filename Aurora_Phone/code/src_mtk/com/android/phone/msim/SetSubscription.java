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
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.internal.telephony.Phone;

import java.lang.Object;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import aurora.preference.*;
import aurora.app.*;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.view.KeyEvent;
import static com.android.phone.AuroraMSimConstants.SUBSCRIPTION_KEY;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;

public class SetSubscription extends AuroraPreferenceActivity implements DialogInterface.
        OnDismissListener, DialogInterface.OnClickListener, AuroraPreference.OnPreferenceChangeListener  {
    private static final String TAG = "AuroraSetSubscription";


    protected boolean mIsForeground = false;

   
    private static final int mNumPhones = PhoneUtils.getPhoneCount();

    private Phone mPhone = null;
    private Phone mPhone2;
    private Phone[] phoneList;
    

    AuroraPreference mSim1Operator,mSim2Operator;
    AuroraImagePreferenceScreen mSim1Icon, mSim2Icon;
    AuroraSwitchPreference mSim1Enable, mSim2Enable;
    SharedPreferences mSharedPreferences;
    
 	AuroraPreferenceCategory manageSub1, manageSub2;
    private AuroraSwitchPreference mButtonDataRoam;
    private AuroraSwitchPreference mButtonDataRoam2;
    private AuroraListPreference mButtonPreferredNetworkMode, mButtonPreferredNetworkMode3;
    private AuroraPreference mButtonPreferredNetworkMode2, mButtonPreferredNetworkMode4;
    private AuroraPreferenceScreen mButtonAPNExpand1;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand1;
    private AuroraPreferenceScreen mButtonAPNExpand2;
    private AuroraPreferenceScreen mButtonOperatorSelectionExpand2;
    
    private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
    private static final String BUTTON_ROAMING_KEY2 = "button_roaming_key2";
    private static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
    private static final String BUTTON_PREFERED_NETWORK_MODE3 = "preferred_network_mode_key3";
    private static final String BUTTON_PREFERED_NETWORK_MODE2 = "preferred_network_mode_key2";
    private static final String BUTTON_PREFERED_NETWORK_MODE4 = "preferred_network_mode_key4";
    private static final String BUTTON_APN_EXPAND_KEY1 = "button_apn_key_1";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY1 = "button_carrier_sel_key_1";
    private static final String BUTTON_APN_EXPAND_KEY2 = "button_apn_key_2";
    private static final String BUTTON_OPERATOR_SELECTION_EXPAND_KEY2 = "button_carrier_sel_key_2";
    
    private TelephonyManagerEx mTelephonyManagerEx;
    private List<SIMInfo> mSiminfoList = new ArrayList<SIMInfo>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.mtk_sim_management);
        getAuroraActionBar().setTitle(R.string.callind_multi_sim_card);
        
        mTelephonyManagerEx = TelephonyManagerEx.getDefault();

        mSharedPreferences = getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
        mPhone = PhoneGlobals.getInstance().getPhone(AuroraMSimConstants.SUB1);
        mPhone2 = PhoneGlobals.getInstance().getPhone(AuroraMSimConstants.SUB2);
        phoneList = new Phone[2];
        phoneList[0] = mPhone;
        phoneList[1] = mPhone2;
        
        AuroraPreferenceScreen prefSet = getPreferenceScreen();
        mSim1Operator = (AuroraPreference) findPreference("sim1_operator");
        mSim2Operator = (AuroraPreference) findPreference("sim2_operator");
        mSim1Icon = (AuroraImagePreferenceScreen) findPreference("sim1_icon");
        mSim2Icon = (AuroraImagePreferenceScreen) findPreference("sim2_icon");
        mSim1Enable = (AuroraSwitchPreference) findPreference("sim1_enable");
        mSim2Enable = (AuroraSwitchPreference) findPreference("sim2_enable");
        mSim1Enable.setOnPreferenceChangeListener(this);
        mSim2Enable.setOnPreferenceChangeListener(this);
        
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
        mButtonPreferredNetworkMode3 = (AuroraListPreference) prefSet.findPreference(
                BUTTON_PREFERED_NETWORK_MODE3);        
        mButtonPreferredNetworkMode3.setOnPreferenceChangeListener(this);
        mButtonPreferredNetworkMode3.setValue(Integer.toString(getPreferredNetworkMode(1)));
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
        mButtonOperatorSelectionExpand1.auroraSetArrowText(getTitleFromOperatorNumber(getOperator(AuroraMSimConstants.SUB1)),false);
        
        mButtonAPNExpand2 = (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_APN_EXPAND_KEY2);
        mButtonAPNExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mButtonAPNExpand1.getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mButtonOperatorSelectionExpand2 =
                (AuroraPreferenceScreen) prefSet.findPreference(BUTTON_OPERATOR_SELECTION_EXPAND_KEY2);
        mButtonOperatorSelectionExpand2.getIntent().putExtra(SUBSCRIPTION_KEY, 1);
        mButtonOperatorSelectionExpand2.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mButtonOperatorSelectionExpand2.auroraSetArrowText(getTitleFromOperatorNumber(getOperator(AuroraMSimConstants.SUB2)),false);
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

            mIsSimEnable = new boolean[mNumPhones];

            updateCheckBoxes();                                              
            IntentFilter homeIf =new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            registerReceiver(mHomeRecevier, homeIf);
            
            
            if (!DeviceUtils.isIUNI()) {
//              mButtonPreferredNetworkMode.setEntries(R.array.cm_preferred_network_mode_choices);
//              mButtonPreferredNetworkMode.setEntryValues(R.array.cm_preferred_network_mode_values); 
    	        if(mButtonPreferredNetworkMode2 != null) {
    	        	manageSub1.removePreference(mButtonPreferredNetworkMode2);
    	        }
    	        if(mButtonPreferredNetworkMode4 != null) {
    	        	manageSub2.removePreference(mButtonPreferredNetworkMode4);
    	        }
    	  	} else {
    	          if(mButtonPreferredNetworkMode != null) {
    	        	  manageSub1.removePreference(mButtonPreferredNetworkMode);
    	         }	
    	          
    	          if(mButtonPreferredNetworkMode3 != null) {
    	        	  manageSub1.removePreference(mButtonPreferredNetworkMode3);
    	         }	
    	  	}
            
            
       	 if (!DeviceUtils.isSupportDualData()) {
		        if (mButtonPreferredNetworkMode4 != null) {
		        	manageSub2.removePreference(mButtonPreferredNetworkMode4);
		       }
		        
		        if (mButtonPreferredNetworkMode3 != null) {
		        	manageSub2.removePreference(mButtonPreferredNetworkMode3);
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
        PhoneGlobals.getInstance().phoneMgr.registerForSimModeChange(mSwitchRadioStateMsg
                .getBinder(), EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE);
        mIsForeground = true;
        mIsSIMRadioSwitching = false;
        updateState();
        updateUiState();
    }


    @Override
    protected void onPause() {
        super.onPause();
        PhoneGlobals.getInstance().phoneMgr.unregisterForSimModeChange(mSwitchRadioStateMsg.getBinder());
        mIsForeground = false;
    }

    private boolean isAirplaneModeOn() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void updateState() {
        Log.d(TAG, "updateState");
    	mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
    	mButtonDataRoam2.setChecked(mPhone2.getDataRoamingEnabled());
        mPhone.getPreferredNetworkType(mMobileHandler.obtainMessage(
                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 0, -1));
	    mPhone2.getPreferredNetworkType(mMobileHandler.obtainMessage(
	                MyHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE, 1, -1));
        
        updateOperatorSelectionVisibility();   	    	    	
    }


    public boolean onPreferenceChange(AuroraPreference preference, Object objValue) {
        final String key = preference.getKey();
        String status;
        Log.d(TAG, "onPreferenceChange:::: " );

        if (preference == mButtonDataRoam) {
            boolean isChecked = (Boolean)objValue;
            handleRoamSwitch(0, isChecked);
        } else if (preference == mButtonDataRoam2) {
            boolean isChecked = (Boolean)objValue;
            handleRoamSwitch(1, isChecked);
        } else if(preference == mButtonPreferredNetworkMode){
            int buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
        	handleSetNetType(buttonNetworkMode, 0);        
        } else if(preference == mButtonPreferredNetworkMode3){
            int buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
        	handleSetNetType(buttonNetworkMode, 1);        
        } else if(preference == mSim1Enable) {
            boolean isChecked = (Boolean)objValue;
            handleSimSwitch(0, isChecked);
        } else if(preference == mSim2Enable) {
            boolean isChecked = (Boolean)objValue;
            handleSimSwitch(1, isChecked);
        }
        
        return true;
    } 


    private final int EVENT_SIM_STATE_CHANGED = 1002;
    private Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        AsyncResult ar;
	
	        switch(msg.what) {
	            case EVENT_SIM_STATE_CHANGED:
	                Log.d(TAG, "EVENT_SIM_STATE_CHANGED");
	         	    mHandler.postDelayed(new Runnable(){
		     	    	public void run(){
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
        	intent.putExtra(AuroraMSimConstants.SUBSCRIPTION_KEY, preference == mSim1Icon ? 0 : 1);
        	startActivityForResult(intent, 0);
        } 
        return false;
    }  
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
	    	   case RESULT_OK:
	    	        int slot = data.getIntExtra(AuroraMSimConstants.SUBSCRIPTION_KEY, PhoneGlobals.getInstance().getDefaultSubscription());
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

    
    protected void onDestroy () {
        super.onDestroy();
        dismissAuroraDialogSafely();
        unregisterReceiver(mHomeRecevier);
    }
       
    
    //原有的逻辑是更新所有卡的所有卡的所有模的列表，如双模卡就会有两个checkbox可以点击，一般来说一卡模，IUNI的只操作第一个模，双模卡的情况暂时不考虑
    private void updateCheckBoxes() { 
    	mIsSimEnable[0] = false;
    	mIsSimEnable[1] = false;
//        mSiminfoList = SIMInfo.getInsertedSIMList(this);
//        for (int i = 0; i < mSiminfoList.size(); i++) {
//            int sub = mSiminfoList.get(i).mSlot;
//            mIsSimEnable[mSiminfoList.get(i).mSlot] = true;
//        }
//    	mIsSimEnable[0] = TelephonyManagerEx.getDefault().getSimState(0) == TelephonyManager.SIM_STATE_READY;
//    	mIsSimEnable[1] = TelephonyManagerEx.getDefault().getSimState(1) == TelephonyManager.SIM_STATE_READY;
    	  int dualSimModeSetting = System.getInt(getContentResolver(),
                  System.DUAL_SIM_MODE_SETTING, GeminiNetworkSubUtil.MODE_DUAL_SIM);
    	  mIsSimEnable[0] = (dualSimModeSetting & 1) > 0;
    	  mIsSimEnable[1] = (dualSimModeSetting & 2) > 0;
        
        Log.d(TAG, "updateCheckBoxes sim1 = " + mIsSimEnable[0] + " sim2 = " + mIsSimEnable[1]);  
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


    
 	
 	private void updateUiState(){
 	    Log.d(TAG, "updateUiState");
 	   //aurora modify liguangyu 20140530 for BUG #5274 start
 	    mHandler.postDelayed(new Runnable(){
 	    	public void run(){
 	            mButtonOperatorSelectionExpand1.auroraSetArrowText(getTitleFromOperatorNumber(getOperator(AuroraMSimConstants.SUB1)),false);
 	            mButtonOperatorSelectionExpand2.auroraSetArrowText(getTitleFromOperatorNumber(getOperator(AuroraMSimConstants.SUB2)),false);
 	    	}
 	    }, 2000);
  	   //aurora modify liguangyu 20140530 for BUG #5274 end
 	    Log.d(TAG, "updateUiState, isCardInsert1 = " + mIsSimEnable[0] +  " isCardInsert2 =" + mIsSimEnable[1]); 	    
	    updateCheckBoxes();
 	    boolean isCardInsert1 = SimInfoUtils.isCardInsert(this, AuroraMSimConstants.SUB1);
 	    boolean isCardInsert2 = SimInfoUtils.isCardInsert(this, AuroraMSimConstants.SUB2);
	    manageSub1.setEnabled(isCardInsert1);
	    manageSub1.setEnabled(isCardInsert2);
	    mSim1Icon.update(isCardInsert1 && mIsSimEnable[0]);
	    mSim2Icon.update(isCardInsert2 && mIsSimEnable[1]);	      

        
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
            	
	    mButtonOperatorSelectionExpand1.setEnabled(isCardInsert1 && mIsSimEnable[0]);
	    mButtonOperatorSelectionExpand2.setEnabled(isCardInsert2 && mIsSimEnable[1]);
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
         	mButtonPreferredNetworkMode3.setEnabled(true);
         	mButtonPreferredNetworkMode4.setEnabled(true);
         	mButtonAPNExpand2.setEnabled(true);
        } else {
         	mButtonDataRoam2.setChecked(false);
         	mButtonDataRoam2.setEnabled(false);
        	mButtonPreferredNetworkMode3.setEnabled(false);
         	mButtonPreferredNetworkMode4.setEnabled(false);
         	mButtonAPNExpand2.setEnabled(false);
        }   
        
         int dataSub = getDataSub();
        if(isCardInsert1 && mIsSimEnable[0]) {
            if(dataSub == 0 && cm.getMobileDataEnabled()) {
		         manageSub1.addPreference(mButtonDataRoam);
		         manageSub1.addPreference(mButtonAPNExpand1);
		         if (!DeviceUtils.isIUNI()) {
				     manageSub1.addPreference(mButtonPreferredNetworkMode);
		    	 } else {
				     manageSub1.addPreference(mButtonPreferredNetworkMode2);
		    	 }
            } else {
	   	         manageSub1.removePreference(mButtonDataRoam);
	   	         manageSub1.removePreference(mButtonAPNExpand1);
	   	         manageSub1.removePreference(mButtonPreferredNetworkMode2);
	   	         if (!DeviceUtils.isIUNI()) {
				     manageSub1.removePreference(mButtonPreferredNetworkMode);
		    	 } else {
				     manageSub1.removePreference(mButtonPreferredNetworkMode2);
		    	 }
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
	         if (!DeviceUtils.isIUNI()) {
			     manageSub1.removePreference(mButtonPreferredNetworkMode);
	    	 } else {
			     manageSub1.removePreference(mButtonPreferredNetworkMode2);
	    	 }
//	         manageSub1.removePreference(mButtonOperatorSelectionExpand1);	
	         manageSub1.removePreference(mSim1Icon);	
        }
        
        if(isCardInsert2 && mIsSimEnable[1]) {
            if(dataSub == 1 && cm.getMobileDataEnabled()) {
		         manageSub2.addPreference(mButtonDataRoam2);
		         manageSub2.addPreference(mButtonAPNExpand2);
		         if (!DeviceUtils.isIUNI()) {
		        	 manageSub2.addPreference(mButtonPreferredNetworkMode3);
		    	 } else {
			         manageSub2.addPreference(mButtonPreferredNetworkMode4);
		    	 }
            } else {
            	manageSub2.removePreference(mButtonDataRoam2);
            	manageSub2.removePreference(mButtonAPNExpand2);
	   	         if (!DeviceUtils.isIUNI()) {
		        	 manageSub2.removePreference(mButtonPreferredNetworkMode3);
		    	 } else {
			         manageSub2.removePreference(mButtonPreferredNetworkMode4);
		    	 }
            }
//            manageSub2.addPreference(mButtonOperatorSelectionExpand2);	
	         if(SimIconUtils.isIconBySiminfo) {
	        	 manageSub2.addPreference(mSim2Icon);	
	         } else {
	             manageSub2.removePreference(mSim2Icon);	
	         }
        } else {
        	manageSub2.removePreference(mButtonDataRoam2);
        	manageSub2.removePreference(mButtonAPNExpand2);
  	        if (!DeviceUtils.isIUNI()) {
		     	manageSub2.removePreference(mButtonPreferredNetworkMode3);
		    } else {
			    manageSub2.removePreference(mButtonPreferredNetworkMode4);
		    }
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
        
        if(PhoneUtils.isMtk()) {
         	manageSub2.removePreference(mButtonPreferredNetworkMode3);
        }
        
 	}

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
 	 	        if(mErrorDialog != null) {
 	 	        	mErrorDialog.dismiss();
 	 	        	mErrorDialog = null;
 	 	        }
 	        } catch (IllegalArgumentException e) {
 	        	e.printStackTrace();
 	        }
 	    }
 	    //aurora add liguangyu 20140616 for BUG #5255 end
 	    
 	    
 	   //aurora add liguangyu 20140819 for BUG #7694 start
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
 	     	boolean cardReady = mTelephonyManagerEx.getSimState(sub) == TelephonyManager.SIM_STATE_READY; 
 	        if (DBG) log("isSimEnable sub = " + sub + " cardReady = " + cardReady);
 	     	return cardReady;
 	     }
 	     
 	    
 	   private void updateOperatorSelectionVisibility() {
 	         log("updateOperatorSelectionVisibility. mPhone = " + mPhone.getPhoneName());
 	         Resources res = getResources();
 	         
 	         AuroraPreferenceScreen mPrefScreen = getPreferenceScreen();
 	         if (mButtonOperatorSelectionExpand1 == null) {
 	             android.util.Log.e(TAG, "mButtonOperatorSelectionExpand is null");
 	         } else {
 	        	 if (res.getBoolean(R.bool.csp_enabled)) {
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
 	        	 if (res.getBoolean(R.bool.csp_enabled)) {
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
 		   
 		   if(sub == 1) {
 			   return 1;
 		   }
 		   
 		   
 	         int nwMode;
// 	             nwMode = android.telephony.MSimTelephonyManager.getIntAtIndex(
// 	                     mPhone.getContext().getContentResolver(),
// 	                     android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
// 	                     sub);
 	             
 	        	 nwMode =  android.provider.Settings.Secure.getInt(
 	        			 mPhone.getContext().getContentResolver(),
 	                     android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
 	                     preferredNetworkMode);


 	         return nwMode;
 	     }

 	     private void setPreferredNetworkMode(int nwMode , int sub) {
// 	         android.telephony.MSimTelephonyManager.putIntAtIndex(
// 	                     mPhone.getContext().getContentResolver(),
// 	                     android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
// 	                     sub, nwMode);

 	 		   if(sub == 1) {
 	 			   return;
 	 		   }
 	    	 
 	    	  android.provider.Settings.Secure.putInt(
 	    			 mPhone.getContext().getContentResolver(),
 	                 android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
 	                 nwMode);
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
                              modemNetworkMode + " sub =" + sub);
                  }

                  int settingsNetworkMode = getPreferredNetworkMode(sub);
                  if (DBG) {
                      log("handleGetPreferredNetworkTypeReponse: settingsNetworkMode = " +
                              settingsNetworkMode);
                  }

                  //check that modemNetworkMode is from an accepted value
                  if ((modemNetworkMode >= Phone.NT_MODE_WCDMA_PREF)
                          && (modemNetworkMode <= 22)) {
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

                      UpdatePreferredNetworkModeSummary(sub == 0 ? mButtonPreferredNetworkMode : mButtonPreferredNetworkMode3, modemNetworkMode);
                      // changes the mButtonPreferredNetworkMode accordingly to modemNetworkMode     
                      if(sub == 0) {
                          mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                         mButtonPreferredNetworkMode.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                     	 mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                      } else if(sub == 1) {
                          mButtonPreferredNetworkMode3.setValue(Integer.toString(modemNetworkMode));
                         mButtonPreferredNetworkMode3.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                     	 mButtonPreferredNetworkMode4.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(sub)),true);
                      }
                  } else {
                      if (DBG) log("handleGetPreferredNetworkTypeResponse: else: reset to default");
                      resetNetworkModeToDefault(sub);
                  }
                  // Update '2GOnly checkbox' based on recent preferred network type selection.
//                  Use2GOnlyCheckBoxPreference.updateCheckBox(mPhone);
              }
          }

          private void handleSetPreferredNetworkTypeResponse(Message msg) {
              AsyncResult ar = (AsyncResult) msg.obj;
              
              int sub = msg.arg1;

              if (ar.exception == null) {
//                  int networkMode = Integer.valueOf(
//                          mButtonPreferredNetworkMode.getValue()).intValue();
//                  setPreferredNetworkMode(networkMode, 0);
                  if(sub == 0) {
	                  mButtonPreferredNetworkMode.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
	                  mButtonPreferredNetworkMode2.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
                  } else {
                	    mButtonPreferredNetworkMode3.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
                        mButtonPreferredNetworkMode4.auroraSetArrowText(getResources().getString(getPreferredNetworkStringId(0)),true);
                  }
              } else {
            	  phoneList[sub].getPreferredNetworkType(obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE, sub , -1));
              }
          }

          private void resetNetworkModeToDefault(int sub) {
              //set the Settings.System
              if(sub == 0) {
            	  mButtonPreferredNetworkMode.setValue(Integer.toString(preferredNetworkMode));
              } else {
            	  mButtonPreferredNetworkMode3.setValue(Integer.toString(preferredNetworkMode));
              }
              setPreferredNetworkMode(preferredNetworkMode, 0);
              //Set the Modem
              phoneList[sub].setPreferredNetworkType(preferredNetworkMode,
                      this.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub,-1));
          }
      }
      
      
	private int getPreferredNetworkStringId(int sub) {
		int NetworkMode = getPreferredNetworkMode(sub);
		switch (NetworkMode) {
		case 3:
			return R.string.aurora_preferred_network_mode_choices_3p;
		case 1:
			return R.string.aurora_preferred_network_mode_choices_2;
		case 2:
			return R.string.aurora_preferred_network_mode_choices_3;
			default:
				return R.string.aurora_preferred_network_mode_choices_3p;

		}
	}
      
      
      private void UpdatePreferredNetworkModeSummary(AuroraPreference ap, int NetworkMode) {
    	  
    	  
    	 if(PhoneUtils.isMtk()) {
       		 switch(NetworkMode) {
   	    		case NT_MODE_GSM_UMTS:
   	    			ap.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
   	    			break;
   	    		case NT_MODE_WCDMA_ONLY:
   	    			ap.setSummary(R.string.aurora_preferred_network_mode_choices_3);
   	    			break;
   	    		case NT_MODE_GSM_ONLY:
   	    			ap.setSummary(R.string.aurora_preferred_network_mode_choices_2);
   	    			break;
   	            default:
   	            	ap.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
       		 }
       		return;
       	} 
    	  
          switch(NetworkMode) {
              case NT_MODE_WCDMA_PREF:
                  ap.setSummary(
                          R.string.preferred_network_mode_wcdma_perf_summary);
                  break;
              case NT_MODE_GSM_ONLY:
                  ap.setSummary(
                          R.string.preferred_network_mode_gsm_only_summary);
                  break;
              case NT_MODE_WCDMA_ONLY:
                  ap.setSummary(
                          R.string.preferred_network_mode_wcdma_only_summary);
                  break;
              case NT_MODE_GSM_UMTS:
                  ap.setSummary(
                          R.string.preferred_network_mode_gsm_wcdma_summary);
                  break;
              case NT_MODE_CDMA:
                  switch (mPhone.getLteOnCdmaMode()) {
                      case PhoneConstants.LTE_ON_CDMA_TRUE:
                          ap.setSummary(
                              R.string.preferred_network_mode_cdma_summary);
                      break;
                      case PhoneConstants.LTE_ON_CDMA_FALSE:
                      default:
                          ap.setSummary(
                              R.string.preferred_network_mode_cdma_evdo_summary);
                          break;
                  }
                  break;
              case NT_MODE_CDMA_NO_EVDO:
                  ap.setSummary(
                          R.string.preferred_network_mode_cdma_only_summary);
                  break;
              case NT_MODE_EVDO_NO_CDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_evdo_only_summary);
                  break;
              case NT_MODE_LTE_ONLY:
                  ap.setSummary(
                          R.string.preferred_network_mode_lte_summary);
                  break;
              case NT_MODE_LTE_GSM_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_lte_gsm_wcdma_summary);
                  break;
              case NT_MODE_LTE_CDMA_AND_EVDO:
                  ap.setSummary(
                          R.string.preferred_network_mode_lte_cdma_evdo_summary);
                  break;
              case NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_global_summary);
                  break;
              case NT_MODE_GLOBAL:
                  ap.setSummary(
                          R.string.preferred_network_mode_cdma_evdo_gsm_wcdma_summary);
                  break;
             case NT_MODE_LTE_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_lte_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_ONLY:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_only_summary);
                  break;
              case NT_MODE_TD_SCDMA_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_LTE:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_LTE:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_WCDMA_LTE:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_wcdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_GSM_WCDMA_LTE:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_gsm_wcdma_lte_summary);
                  break;
              case NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_cdma_evdo_gsm_wcdma_summary);
                  break;
              case NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA:
                  ap.setSummary(
                          R.string.preferred_network_mode_td_scdma_lte_cdma_evdo_gsm_wcdma_summary);
                  break;
              default:
                  ap.setSummary(
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
      
      private boolean[] mIsSimEnable;
      private boolean mIsSIMRadioSwitching = false;

		private void handleSimSwitch(final int sub, boolean isChecked ) {	    
 	        Log.d(TAG, "onCheckedChanged: handleSimSwitch isChecked = " + isChecked+ " sub =" + sub);
 	        
 	       if (mErrorDialog != null && mErrorDialog.isShowing()) {
 	        	return;
 	        }
 	        
 	        
 	       boolean otherChecked = false;
 	       int otherSub;
 	       if(sub > 0) {
 	    	  otherSub = 0;
 	    	  otherChecked = mSim1Enable.isChecked();
 	       } else {
 	    	  otherSub = 1;
 	    	  otherChecked = mSim2Enable.isChecked();
 	       }
 	        if(!isChecked && !otherChecked) {
 	        	 displayErrorDialog(R.string.deact_all_sub_not_supported);
 	        } else if(PhoneGlobals.getInstance().mManagePhbReading.isInPhbLoadProcess()) {
 	        	 displayErrorDialog(R.string.deact_all_sub_not_supported_for_phb);
 	        } else { 	   
 	           if (!mIsSIMRadioSwitching) {
 	              log("start to turn radio in " + isChecked);
 	               mIsSIMRadioSwitching = true;     
 	               switchSimRadioState(sub);
 	           } 
 	        }
        
		}
		

		
	    private static final int ALL_RADIO_OFF = 0;
	    private static final int SIM_SLOT_1_RADIO_ON = 1;
	    private static final int SIM_SLOT_2_RADIO_ON = 2;
	    private static final int ALL_RADIO_ON = 3;
		
	    private void switchSimRadioState(int slot) {
	        boolean mIsSlot1Insert = false;
	        boolean mIsSlot2Insert = false;
	        log("getSimInfo()");
	        mSiminfoList = SIMInfo.getInsertedSIMList(this);
	        for (int i = 0; i < mSiminfoList.size(); i++) {
	            int sub = mSiminfoList.get(i).mSlot;
	            if (sub == Phone.GEMINI_SIM_1) {
	                mIsSlot1Insert = true;    
	            } else if (sub == Phone.GEMINI_SIM_2) {
	                mIsSlot2Insert = true;
	            }
	        }
	        
	        int dualSimMode = Settings.System.getInt(this.getContentResolver(),
	                Settings.System.DUAL_SIM_MODE_SETTING, -1);
	        log("The current dual sim mode is " + dualSimMode);
	        
	        int dualState = 0;
	        boolean isRadioOn = false;
	        switch (dualSimMode) {
		        case ALL_RADIO_OFF:
		            if (slot == Phone.GEMINI_SIM_1) {
		                dualState = SIM_SLOT_1_RADIO_ON;
		            } else if (slot == Phone.GEMINI_SIM_2) {
		                dualState = SIM_SLOT_2_RADIO_ON;
		            }
		            log("Turning on only sim " + slot);
		            isRadioOn = true;
		            break;
		        case SIM_SLOT_1_RADIO_ON:
		            if (slot == Phone.GEMINI_SIM_1) {
		                dualState = ALL_RADIO_OFF;
		                isRadioOn = false;
		                log("Turning off sim " + slot
		                        + " and all sim radio is off");
		            } else if (slot == Phone.GEMINI_SIM_2) {
		                if (mIsSlot1Insert) {
		                    dualState = ALL_RADIO_ON;
		                    log("sim 0 was radio on and now turning on sim "
		                            + slot);
		                } else {
		                    dualState = SIM_SLOT_2_RADIO_ON;
		                    log("Turning on only sim " + slot);
		                }
		                isRadioOn = true;
		            }
		            break;
		        case SIM_SLOT_2_RADIO_ON:
		            if (slot == Phone.GEMINI_SIM_2) {
		                dualState = ALL_RADIO_OFF;
		                isRadioOn = false;
		                log("Turning off sim " + slot
		                        + " and all sim radio is off");
		            } else if (slot == Phone.GEMINI_SIM_1) {
		                if (mIsSlot2Insert) {
		                    dualState = ALL_RADIO_ON;
		                    log("sim 1 was radio on and now turning on sim "
		                            + slot);
		                } else {
		                    dualState = SIM_SLOT_1_RADIO_ON;
		                    log("Turning on only sim " + slot);
		                }
		                isRadioOn = true;
		            }
		            break;
		        case ALL_RADIO_ON:
		            if (slot == Phone.GEMINI_SIM_1) {
		                dualState = SIM_SLOT_2_RADIO_ON;
		            } else if (slot == Phone.GEMINI_SIM_2) {
		                dualState = SIM_SLOT_1_RADIO_ON;
		            }
		            log("Turning off only sim " + slot);
		            isRadioOn = false;
		            break;
		        default:
		            log("Error not correct values");
		            return;
	        }
	        ///M: only gemini support to show a dialog, for single sim do not show this dlg @{
	        if (PhoneUtils.isMultiSimEnabled()) {
                if(mIsForeground){
                	showSetSubProgressDialog();
               }
	        }
	        ///@}
	        log("dualState=" + dualState + " isRadioOn=" + isRadioOn);
	        Settings.System.putInt(this.getContentResolver(),
	                Settings.System.DUAL_SIM_MODE_SETTING, dualState);
	        Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
	        intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, dualState);
	        sendBroadcast(intent);
	    }


	    private void dealWithSwtichComplete() {
	        log("dealWithSwtichComplete");
	        log("mIsSIMModeSwitching is " + mIsSIMRadioSwitching);
	        if (!mIsSIMRadioSwitching) {
	            log("Error happend, should not be happened...");
	        }
	        mIsSIMRadioSwitching = false;
	        
    		dismissAuroraDialogSafely();
            getPreferenceScreen().setEnabled(true);
	          Toast toast = Toast.makeText(getApplicationContext(), R.string.set_sub_success, Toast.LENGTH_SHORT);
	          toast.show();
	          updateUiState();	           
	        
	    }

	    private static final int EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE = 1;
	    private Messenger mSwitchRadioStateMsg = new Messenger(new Handler() {
	        public void handleMessage(Message msg) {
	            if (EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE == msg.what) {
	                log("dual sim mode changed");
	                dealWithSwtichComplete();
	            }
	        }
	    });
		
	    
	    public static String getOperator(int slot) {
	        return TelephonyManagerEx.getDefault().getNetworkOperator(slot);
	         
	    }
	    
	 	   private int getDataSub(){
	 		   return PhoneGlobals.getInstance().getDataSubscription();
	 	   }
	 	   
	  private void handleSetNetType(int buttonNetworkMode, int sub) {
			if(sub ==0) {
		          mButtonPreferredNetworkMode.setValue(buttonNetworkMode + "");
			} else {
		          mButtonPreferredNetworkMode3.setValue(buttonNetworkMode + "");
			}
          int settingsNetworkMode = getPreferredNetworkMode(sub);
          if (buttonNetworkMode != settingsNetworkMode) {
              int modemNetworkMode;
              if(buttonNetworkMode >= 0 && buttonNetworkMode <=22) {
              	  modemNetworkMode = buttonNetworkMode;
              } else {
                  log("Invalid Network Mode (" + buttonNetworkMode + ") chosen. Ignore.");
                  return;
              }
              UpdatePreferredNetworkModeSummary(sub == 0 ? mButtonPreferredNetworkMode : mButtonPreferredNetworkMode3, buttonNetworkMode);

              setPreferredNetworkMode(modemNetworkMode , sub);
              phoneList[sub].setPreferredNetworkType(modemNetworkMode, mMobileHandler
                        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE, sub, -1));

          }
	  }
}
