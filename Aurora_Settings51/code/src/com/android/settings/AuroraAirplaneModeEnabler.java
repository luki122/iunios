/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;

import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.dashboard.DashboardTileView;
import com.mediatek.settings.SubscriberPowerStateListener;
import com.mediatek.settings.SubscriberPowerStateListener.onRadioPowerStateChangeListener;


public class AuroraAirplaneModeEnabler implements CompoundButton.OnCheckedChangeListener, AuroraPreference.OnPreferenceChangeListener {

    private final Context mContext;

    private PhoneStateIntentReceiver mPhoneStateReceiver;
    
    private AuroraSwitch mSwitch;
    private AuroraSwitchPreference mAirplaneModePreference;

    private boolean isPreference;

    private boolean isRegister;
    
    private static final int EVENT_SERVICE_STATE_CHANGED = 3;
    
    /// M : add for Bug fix ALPS01772247
    private static final String TAG = "AuroraAirplaneModeEnabler";
    private SubscriberPowerStateListener mListener;
    
    private DashboardTileView mMobileNetworkView;
    private static final int IMAGE_GRAY = 75;//30% of 0xff in transparent
    private static final int ORIGINAL_IMAGE = 255;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SERVICE_STATE_CHANGED:
                    onAirplaneModeChanged();
                    break;
            }
        }
    };

    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onAirplaneModeChanged();
        }
    };

    public AuroraAirplaneModeEnabler(Context context, AuroraSwitch airplaneModeSwitch) {
        
        mContext = context;
        mSwitch = airplaneModeSwitch;
        mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
        isRegister = false;
        isPreference = false;
        
        /// M : bug fix for ALPS01772247 / ALPS01810660 @{
        initListener(context);
        /// @}
    }

    /*Aurora linchunhui add 20160219*/
    public AuroraAirplaneModeEnabler(Context context, AuroraSwitchPreference airplaneModePreference) {
        
        mContext = context;
        mAirplaneModePreference = airplaneModePreference;
        mPhoneStateReceiver = new PhoneStateIntentReceiver(mContext, mHandler);
        mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
        isRegister = false;
        isPreference = true;
        
        /// M : bug fix for ALPS01772247 / ALPS01810660 @{
        initListener(context);
        /// @}
    }

    // Only for phone, tablet no need to monitor SIM state
    private void initListener(Context context) {
        if (!Utils.isWifiOnly(mContext)) {
            mListener = new SubscriberPowerStateListener(context);
            mListener.setRadioPowerStateChangeListener(new onRadioPowerStateChangeListener() {
                @Override
                public void onAllPoweredOff() {
                	if (isPreference) {
                              mAirplaneModePreference.setEnabled(true);
                        } else {
                              mSwitch.setEnabled(true);
                        }
                }
                @Override
                public void onAllPoweredOn() {
                	if (isPreference) {
                              mAirplaneModePreference.setEnabled(true);
                        } else {
                              mSwitch.setEnabled(true);
                        }
                }
            });
        }
    }

    public void resume() {
        if (isPreference) {
                 mAirplaneModePreference.setChecked(isAirplaneModeOn(mContext));
        }else {
                 mSwitch.setChecked(isAirplaneModeOn(mContext));
        }

        mPhoneStateReceiver.registerIntent();
        if (isPreference) {
                mAirplaneModePreference.setOnPreferenceChangeListener(this);
        }else {
                mSwitch.setOnCheckedChangeListener(this);
        }

        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON), true,
                mAirplaneModeObserver);
        isRegister = true;
    }
    
    public void pause() {
    	if(isRegister){
    		mPhoneStateReceiver.unregisterIntent();
                if (isPreference) {
                      mAirplaneModePreference.setOnPreferenceChangeListener(null);
                }else {
                      mSwitch.setOnCheckedChangeListener(null);
                }
    		mContext.getContentResolver().unregisterContentObserver(mAirplaneModeObserver);
    		isRegister = false;
        }
    }

    /// M : add for bug fix ALPS01772247@{
    public void destroy() {
        if (mListener != null) {
            mListener.unRegisterListener();
        }
    }
    /// @}
    
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneModeOn(boolean enabling) {
        /*aurora linchunhui 20150826 only change if it change begin*/
        boolean preEnabling = Settings.Global.getInt(mContext.getContentResolver(),
                               Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (preEnabling != enabling) {
            // Change the system setting
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 
                                    enabling ? 1 : 0);
        }
        /*aurora linchunhui 20150826 only change if it change end*/
        // Update the UI to reflect system setting
        if (isPreference) {
                  mAirplaneModePreference.setEnabled(enabling);
        }else {
                  mSwitch.setEnabled(enabling);
        }
        
        if(mMobileNetworkView != null){
        	if(enabling){
        		mMobileNetworkView.getImageView().setAlpha(IMAGE_GRAY);
        		mMobileNetworkView.getRightView().setAlpha(IMAGE_GRAY);
        		mMobileNetworkView.getTitleTextView().setEnabled(false);
        		mMobileNetworkView.setEnabled(false);
        	}else{
        		mMobileNetworkView.getImageView().setAlpha(ORIGINAL_IMAGE);
        		mMobileNetworkView.getRightView().setAlpha(ORIGINAL_IMAGE);
        		mMobileNetworkView.getTitleTextView().setEnabled(true);
        		mMobileNetworkView.setEnabled(true);
        	}
        }

        /// M : bug fix for ALPS01772247 / ALPS01810660 @{
        registerSubState();
        /// @}
        /*aurora linchunhui 20150826 only notify if it change begin*/
        if (preEnabling != enabling) {
            // Post the intent
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", enabling);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
        /*aurora linchunhui 20150826 only notify if it change end*/
    }
    
    public void setMobileView(DashboardTileView titleView){
    	this.mMobileNetworkView = titleView;
    }

    //Only for phone, tablet no need to register
    private void registerSubState() {
        if (!Utils.isWifiOnly(mContext)) {
                if (isPreference) {
                      mAirplaneModePreference.setEnabled(false);
                }else {
                      mSwitch.setEnabled(false);
                }
                mListener.registerListener();
        }
    }

    /**
     * Called when we've received confirmation that the airplane mode was set.
     * TODO: We update the checkbox summary when we get notified
     * that mobile radio is powered up/down. We should not have dependency
     * on one radio alone. We need to do the following:
     * - handle the case of wifi/bluetooth failures
     * - mobile does not send failure notification, fail on timeout.
     */
    private void onAirplaneModeChanged() {
        if (isPreference) {
                mAirplaneModePreference.setChecked(isAirplaneModeOn(mContext));
        }else {
                mSwitch.setChecked(isAirplaneModeOn(mContext));
        }
    }
    
    /**
     * Called when someone clicks on the checkbox preference.
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged = " + isChecked);
        if (Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
        } else {
            setAirplaneModeOn(isChecked);
        }
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;
        if (Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
        } else {
            setAirplaneModeOn(isChecked);
        }

        return true;
    }

    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            // update database based on the current checkbox state
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            // update summary
            onAirplaneModeChanged();
        }
    }
    

    /*
     * M: Bug fix for ALPS01899413
     * When hot swap happend, need to update listeners for changed subscribers
     */
    public void updateSubscribers() {
        if (mListener != null) {
            Log.d(TAG,"updateSubscribers");
            mListener.updateSubscribers();
        }
    }
}
