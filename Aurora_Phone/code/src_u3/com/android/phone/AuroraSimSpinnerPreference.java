/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import aurora.preference.*;
import aurora.widget.*;
import com.android.internal.telephony.MSimConstants;
import com.codeaurora.telephony.msim.MSimPhoneFactory;
import com.codeaurora.telephony.msim.Subscription.SubscriptionStatus;
import com.codeaurora.telephony.msim.SubscriptionManager;

public class AuroraSimSpinnerPreference extends AuroraPreference {
    private static final String LOG_TAG = "AuroraSimSpinnerPreference";
    private static final boolean DBG = true;
    private AuroraSpinner mLabel;
    private String[] mNetworks;
    private String[] networkValues;
    private ArrayAdapter<String> mAdapter;
    private boolean mFirst = true;
    SubscriptionManager subManager = SubscriptionManager.getInstance();
    private int mPrioritySubValue = 0;
    private Context mContext;
    private Phone mPhone = null;
    static final int EVENT_SET_DATA_SUBSCRIPTION_DONE = 1;
    static final int EVENT_SET_VOICE_SUBSCRIPTION = 4;
    static final int EVENT_SET_SMS_SUBSCRIPTION = 5;
    static final int EVENT_SET_PRIORITY_SUBSCRIPTION = 8;
    static final int EVENT_SET_PRIORITY_SUBSCRIPTION_DONE = 9;
    static final int EVENT_SET_VOICE_SUBSCRIPTION_DONE = 10;
    private int mVoiceSub = 0;
    
    public AuroraSimSpinnerPreference(Context context) {
        this(context, null);
    }

    public AuroraSimSpinnerPreference(Context context, AttributeSet attrs) {
    	this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public AuroraSimSpinnerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.aurora_spinner);  
        mContext = context;
    	if(AuroraPhoneUtils.isSimulate()) {
            mPhone = MSimSimulatedPhoneFactory.getPhone(MSimConstants.SUB1);
    	} else {
            mPhone = MSimPhoneFactory.getPhone(MSimConstants.SUB1);
    	}
        mNetworks = mPhone.getContext().getResources().getStringArray(R.array.aurora_3rd_sim_choices);
        networkValues = mPhone.getContext().getResources().getStringArray(R.array.aurora_3rd_sim_choices_values);
        mAdapter = new ArrayAdapter<String>(mContext, com.aurora.R.layout.aurora_spinner_list_item, mNetworks);
//        mAdapter = new AutoRecordTypeAdapter(mContext);
    }
 
    
    @Override
    protected void onBindView(View view) {
        log("onBindView");
        super.onBindView(view);         
        mLabel = (AuroraSpinner) view.findViewById(R.id.edit_spinner);
        mLabel.setAdapter(mAdapter);  
        updatePrioritySubState();
        mFirst = true;
        mLabel.setOnItemSelectedListener(mSpinnerListener); 
        updateEnableState();
    }
 
    private OnItemSelectedListener mSpinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(
                AdapterView<?> parent, View view, int position, long id) {
            log("mSpinnerListener onItemSelected: position = " + position);
//        	view.setVisibility(View.GONE);
            if(mFirst) {
            	mFirst = false;
            	return;            
            }
        
            int prioritySubIndex = Integer.parseInt(networkValues[position]);
            if (subManager.getCurrentSubscription(prioritySubIndex).subStatus
                    == SubscriptionStatus.SUB_ACTIVATED) {
                mPrioritySubValue = prioritySubIndex;
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_PRIORITY_SUBSCRIPTION,
                        prioritySubIndex, 0));
                
                //voice
                MSimPhoneFactory.setPromptEnabled(false);
                mVoiceSub = prioritySubIndex;
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_VOICE_SUBSCRIPTION,
                		prioritySubIndex, 0));
                
                //sms
                MSimPhoneFactory.setSMSSubscription(prioritySubIndex);
                
                //data
//                if (mIsForeground) {
//                    showDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
//                }               
//                Message setDdsMsg = Message.obtain(mHandler, EVENT_SET_DATA_SUBSCRIPTION_DONE, null);
//                subManager.setDataSubscription(prioritySubIndex, setDdsMsg);
                
                
            } else {
//                status = mContext.getResources().getString(R.string.set_priority_sub_error);
//                displayAlertDialog(status);
                Toast.makeText(PhoneGlobals.getInstance(), R.string.set_priority_sub_error, Toast.LENGTH_SHORT).show();
            }
            
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
      
    
    private void updatePrioritySubState() {

        try {
            int priorityValue = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.MULTI_SIM_PRIORITY_SUBSCRIPTION);
            mPrioritySubValue = priorityValue;
            if(mLabel!=null) {
            	mLabel.setSelection(mPrioritySubValue);
            }
        } catch (SettingNotFoundException snfe) {
            Log.e(LOG_TAG, "Settings Exception Reading Dual Sim Priority Subscription Values");
        }
    }
    
    private void updatePrioritySub(int priorityIndex) {
        Log.d(LOG_TAG, "updatePrioritySub change priority sub to: " + priorityIndex);
        Message setPrioritySubMsg = Message.obtain(mHandler,
                EVENT_SET_PRIORITY_SUBSCRIPTION_DONE, null);
        mPhone.setPrioritySub(priorityIndex, setPrioritySubMsg);
    }
    
    private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;

        switch(msg.what) {
        case EVENT_SET_DATA_SUBSCRIPTION_DONE:
//            Log.d(TAG, "EVENT_SET_DATA_SUBSCRIPTION_DONE");
//            if (mIsForeground) {
//                dismissDialog(DIALOG_SET_DATA_SUBSCRIPTION_IN_PROGRESS);
//            }
//            getPreferenceScreen().setEnabled(true);
//            updateDataSummary();
//
//            ar = (AsyncResult) msg.obj;
//            String status;
//            if (ar.exception != null) {
//                status = getResources().getString(R.string.set_dds_error)
//                                   + " " + ar.exception.getMessage();
//                displayAlertDialog(status);
//                break;
//            }
//
//            boolean result = (Boolean)ar.result;
//
//            Log.d(TAG, "SET_DATA_SUBSCRIPTION_DONE: result = " + result);
//            if (result == true) {
//                status = getResources().getString(R.string.set_dds_success);
//                Toast toast = Toast.makeText(getApplicationContext(), status,
//                        Toast.LENGTH_LONG);
//                toast.show();
//            } else {
//                status = getResources().getString(R.string.set_dds_failed);
//                displayAlertDialog(status);
//            }

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
                if(mLabel!=null) {
                    mLabel.setOnItemSelectedListener(null);    
                	mLabel.setSelection(mPrioritySubValue);
                    mLabel.setOnItemSelectedListener(mSpinnerListener);    
                }
                MSimPhoneFactory.setPrioritySubscription(mPrioritySubValue);
                break;
                default:
                    Log.w(LOG_TAG, "Unknown Event " + msg.what);
                    break;
        }
    }
};
    
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, msg);
    }
    
    private void updateVoiceSub(int subIndex) {
        Log.d(LOG_TAG, "updateVoiceSub change voice sub to: " + subIndex);
        Message setVoiceSubMsg = Message.obtain(mHandler,
                EVENT_SET_VOICE_SUBSCRIPTION_DONE, null);
        mPhone.setDefaultVoiceSub(subIndex, setVoiceSubMsg);
    } 
    
    private void updateEnableState() {
    	boolean simOldstate[] = new boolean[SubscriptionManager.NUM_SUBSCRIPTIONS];  
        simOldstate[0] = false;
        simOldstate[1] = false;
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences("sim_enable_state", Context.MODE_PRIVATE);
        SIMInfo sim1 = SIMInfo.getSIMInfoBySlot(mContext, 0);
        if(sim1 != null) {
        	simOldstate[0] = mSharedPreferences.getBoolean(String.valueOf(sim1.mSimId), true);
        } 
        SIMInfo sim2 = SIMInfo.getSIMInfoBySlot(mContext, 1);
        if(sim2 != null) {
        	simOldstate[1] = mSharedPreferences.getBoolean(String.valueOf(sim2.mSimId), true);
        }         
    	boolean enable = SIMInfo.getInsertedSIMCount(mContext) > 1;
    	enable = enable && simOldstate[0] && simOldstate[1];
    	this.setEnabled(enable); 
    	if(mLabel != null) {
    		mLabel.setEnabled(enable);
    	}
    }
    
    public void update(){
        updateEnableState();
    }

}
