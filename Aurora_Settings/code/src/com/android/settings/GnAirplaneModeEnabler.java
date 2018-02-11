/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;

import com.android.settings.Utils;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;
//Gionee <chenml>  <2013-03-13> add for CR00783831 begin

// Aurora <likai> <2013-10-19> modify begin
//import com.mediatek.telephony.TelephonyManagerEx;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import com.gionee.settings.utils.CountUtil;
// Aurora <likai> <2013-10-19> modify end

import android.content.BroadcastReceiver;
import android.os.UserHandle;
import aurora.preference.AuroraPreference;
//Gionee <chenml>  <2013-03-13> add for CR00783831 begin

public class GnAirplaneModeEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "GnAirplaneModeEnabler";
    private final Context mContext;

    private AuroraSwitch mSwitch;

    private static final int EVENT_SERVICE_STATE_CHANGED = 3;

    // / M: @{
    private TelephonyManager mTelephonyManager;

    // Aurora <likai> <2013-10-19> modify begin
    //private TelephonyManagerEx mTelephonyManagerEx;
    private GnTelephonyManagerEx mTelephonyManagerEx;
    // Aurora <likai> <2013-10-19> modify end

    private int mServiceState1 = ServiceState.STATE_POWER_OFF;
    private int mServiceState2 = ServiceState.STATE_POWER_OFF;

    // / @}
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 begin
    private boolean GnGeminiSupport = SystemProperties.get("ro.gn.gemini.support", "no").equals("yes");
    //Gionee <wangguojing> <2013-08-19> add for CR00857643 end

    public GnAirplaneModeEnabler(Context context, AuroraSwitch airplaneModeSwitch) {
        mContext = context;
        mSwitch = airplaneModeSwitch;
      //Gionee <chenml>  <2013-03-13> add for CR00783831 begin 
              mTelephonyManager = (TelephonyManager)context.getSystemService(
                Context.TELEPHONY_SERVICE);

        // Aurora <likai> <2013-10-19> modify begin
        //mTelephonyManagerEx = new TelephonyManagerEx(context);
        mTelephonyManagerEx = GnTelephonyManagerEx.getDefault();
        // Aurora <likai> <2013-10-19> modify begin
    }

    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean airplaneModeEnabled = isAirplaneModeOn(mContext);
            mSwitch.setChecked(airplaneModeEnabled);
            mSwitch.setEnabled(true);
        }
    };
       //Gionee <chenml>  <2013-03-13> add for CR00783831 end 
    /// M: phone state listner @{
    PhoneStateListener mPhoneStateListener1 = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(TAG, "PhoneStateListener1.onServiceStateChanged: serviceState=" + serviceState);
            mServiceState1 = serviceState.getState();
            onAirplaneModeChanged();
        }

    };

    PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.i(TAG, "PhoneStateListener2.onServiceStateChanged: serviceState=" + serviceState);
            mServiceState2 = serviceState.getState();
            onAirplaneModeChanged();
        }
    };

    // / @}
    //Gionee <chenml>  <2013-03-13> add for CR00783831 begin 
    public void resume() {

        mSwitch.setChecked(isAirplaneModeOn(mContext));
        // This is the widget enabled state, not the preference toggled state
        if (!Utils.isWifiOnly(mContext)) {
            mSwitch.setEnabled(true);
            if (FeatureOption.MTK_GEMINI_SUPPORT && GnGeminiSupport) {
  //               mTelephonyManager.listenGemini(mPhoneStateListener1, PhoneStateListener.LISTEN_SERVICE_STATE,
  //                       PhoneConstants.GEMINI_SIM_1);
  //              mTelephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_SERVICE_STATE,
   //                      PhoneConstants.GEMINI_SIM_2);
                mTelephonyManagerEx.listen(mPhoneStateListener1,
                        PhoneStateListener.LISTEN_SERVICE_STATE,
                        1/*PhoneConstants.GEMINI_SIM_1*/);
                mTelephonyManagerEx.listen(mPhoneStateListener2,
                        PhoneStateListener.LISTEN_SERVICE_STATE,
                        2/*PhoneConstants.GEMINI_SIM_2*/);
            } else {
                   mTelephonyManager.listen(mPhoneStateListener1,PhoneStateListener.LISTEN_SERVICE_STATE);
            }
        }
        mSwitch.setOnCheckedChangeListener(this);
        Log.d(TAG, "resume()");

    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
        if (!Utils.isWifiOnly(mContext)) {
            if (FeatureOption.MTK_GEMINI_SUPPORT && GnGeminiSupport) {
  //                mTelephonyManager.listenGemini(mPhoneStateListener1, PhoneStateListener.LISTEN_NONE,
  //                       PhoneConstants.GEMINI_SIM_1);
  //                mTelephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_NONE,
  //                      PhoneConstants.GEMINI_SIM_2);
                mTelephonyManagerEx.listen(mPhoneStateListener1,
                        PhoneStateListener.LISTEN_NONE, 1/*PhoneConstants.GEMINI_SIM_1*/);
                mTelephonyManagerEx.listen(mPhoneStateListener2,
                        PhoneStateListener.LISTEN_NONE, 2/*PhoneConstants.GEMINI_SIM_2*/);
            } else {
                mTelephonyManager.listen(mPhoneStateListener1,
                        PhoneStateListener.LISTEN_NONE);
            }
        } else {
            mContext.unregisterReceiver(mAirplaneModeReceiver);
        }
        Log.d(TAG, "pause()");
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneModeOn(boolean enabling) {
        // Change the system setting
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 
                                enabling ? 1 : 0);
        // Update the UI to reflect system setting
        // / M: @{
        if (!Utils.isWifiOnly(mContext)) {
            mSwitch.setEnabled(false);
        }
        /// @}
        mSwitch.setChecked(enabling);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
 //       mContext.sendBroadcast(intent);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "setAirplaneModeOn = " + enabling);
    }
     //Gionee <chenml>  <2013-03-13> add for CR00783831 end 
    /**
     * Called when we've received confirmation that the airplane mode was set. TODO: We update the checkbox
     * summary when we get notified that mobile radio is powered up/down. We should not have dependency on one
     * radio alone. We need to do the following: - handle the case of wifi/bluetooth failures - mobile does
     * not send failure notification, fail on timeout.
     */
    private void onAirplaneModeChanged() {
        // / M: @{
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);
        Log.d(TAG, "Start! airplaneModeEnabled:" + airplaneModeEnabled);
        if (FeatureOption.MTK_GEMINI_SUPPORT && GnGeminiSupport) {
            // [ALPS00225004] When AirplaneMode On, make sure both phone1 and phone2 are radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF
                        || mServiceState2 != ServiceState.STATE_POWER_OFF) {
                    Log.d(TAG, "Unfinish! serviceState1:" + mServiceState1 + " serviceState2:"
                            + mServiceState2);
                    return;
                }
            }
        } else {
            // [ALPS00127431] When AirplaneMode On, make sure phone is radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF) {
                    Log.d(TAG, "Unfinish! serviceState:" + mServiceState1);
                    return;
                }
            }
        }
        Log.d(TAG, "Finish! airplaneModeEnabled:" + airplaneModeEnabled);
        mSwitch.setChecked(airplaneModeEnabled);
        mSwitch.setEnabled(true);
        // / @}
    }
    
    /**
     * Called when someone clicks on the checkbox preference.
     */

    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            // update database based on the current checkbox state
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            // / M: update summary when the load is not wifi only
            if (!Utils.isWifiOnly(mContext)) {
                onAirplaneModeChanged();
            }
        }
    }

    public void setSwitch(AuroraSwitch airplaneModeSwitch) {
        if (mSwitch == airplaneModeSwitch) {
            return;
        }
        //mSwitch.setOnCheckedChangeListener(null);
        mSwitch = airplaneModeSwitch;
        // Gionee <zhang_xin><2013-05-03> modify for CR00803798 begin
        // mSwitch.setOnCheckedChangeListener(this);

        boolean isAirplaneMode = isAirplaneModeOn(mContext);
        mSwitch.setChecked(isAirplaneMode);
        mSwitch.setEnabled(true);
        mSwitch.setOnCheckedChangeListener(this);
        // Gionee <zhang_xin><2013-05-03> modify for CR00803798 end

        Log.d(TAG, "setSwitch = " + isAirplaneMode);

    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged = " + isChecked);
        CountUtil.getInstance(mContext).update("017", 1);
        if (Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            // In ECM mode, do not update database at this point
        } else {
            setAirplaneModeOn(isChecked);
            mSwitch.setEnabled(false);
        }
    }

}
