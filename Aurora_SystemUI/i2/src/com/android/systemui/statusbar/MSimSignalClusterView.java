/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2012-2013 The Linux Foundation. All rights reserved.
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

package com.android.systemui.statusbar;

import android.content.Context;
//update to 5.0 begin
//import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
//import com.android.internal.telephony.MSimConstants;

//update to 5.0 end
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.accessibility.AccessibilityEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.MSimNetworkController;

import com.android.systemui.R;

// Intimately tied to the design of res/layout/msim_signal_cluster_view.xml
public class MSimSignalClusterView
        extends LinearLayout
        implements MSimNetworkController.MSimSignalCluster {
	//update to 5.0 begin
	private final int DEFAULT_SUBSCRIPTION = 0;
    //update to 5.0 end
    static final boolean DEBUG = false;
    static final String TAG = "MSimSignalClusterView";

    MSimNetworkController mMSimNC;

    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0, mWifiActivityId = 0;
    private boolean mMobileVisible = false;
    private int[] mMobileStrengthId;
    private int[] mMobileActivityId;
    private int[] mMobileTypeId;
    private int[] mNoSimIconId;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private String mWifiDescription, mMobileTypeDescription;
    private String[] mMobileDescription;

    ViewGroup mWifiGroup;
    ViewGroup[] mMobileGroup;
    ImageView mWifi, mWifiActivity, mAirplane;
    ImageView[] mNoSimSlot;
    ImageView[] mMobile;
    ImageView[] mMobileActivity;
    ImageView[] mMobileType;
    View mSpacer;
    View mWifiSpacer;
    View mSpacer2;
    
    private int[] mMobileGroupResourceId = {R.id.mobile_combo, R.id.mobile_combo_sub2,
                                          R.id.mobile_combo_sub3};
    private int[] mMobileResourceId = {R.id.mobile_signal, R.id.mobile_signal_sub2,
                                     R.id.mobile_signal_sub3};
    private int[] mMobileActResourceId = {R.id.mobile_inout, R.id.mobile_inout_sub2,
                                        R.id.mobile_inout_sub3};
    private int[] mMobileTypeResourceId = {R.id.mobile_type, R.id.mobile_type_sub2,
                                         R.id.mobile_type_sub3};
    private int[] mNoSimSlotResourceId = {R.id.no_sim, R.id.no_sim_slot2, R.id.no_sim_slot3};
  //update to 5.0 begin
//    private int mNumPhones = MSimTelephonyManager.getDefault().getPhoneCount();
    private int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
  //update to 5.0 end
    public MSimSignalClusterView(Context context) {
        this(context, null);
    }

    public MSimSignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSimSignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMobileStrengthId = new int[mNumPhones];
        mMobileDescription = new String[mNumPhones];
        mMobileTypeId = new int[mNumPhones];
        mMobileActivityId = new int[mNumPhones];
        mNoSimIconId = new int[mNumPhones];
        mMobileGroup = new ViewGroup[mNumPhones];
        mNoSimSlot = new ImageView[mNumPhones];
        mMobile = new ImageView[mNumPhones];
        mMobileActivity = new ImageView[mNumPhones];
        mMobileType = new ImageView[mNumPhones];
        for(int i=0; i < mNumPhones; i++) {
            mMobileStrengthId[i] = 0;
            mMobileTypeId[i] = 0;
            mMobileActivityId[i] = 0;
            mNoSimIconId[i] = 0;
        }
    }

    public void setNetworkController(MSimNetworkController nc) {
        if (DEBUG) Slog.d(TAG, "MSimNetworkController=" + nc);
        mMSimNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity   = (ImageView) findViewById(R.id.wifi_inout);
        mSpacer         =             findViewById(R.id.spacer);
        mSpacer2         =             findViewById(R.id.spacer2);
        mWifiSpacer     =             findViewById(R.id.wifispacer);
        mAirplane       = (ImageView) findViewById(R.id.airplane);

        for (int i = 0; i < mNumPhones; i++) {
            mMobileGroup[i]    = (ViewGroup) findViewById(mMobileGroupResourceId[i]);
            mMobile[i]         = (ImageView) findViewById(mMobileResourceId[i]);
            mMobileActivity[i] = (ImageView) findViewById(mMobileActResourceId[i]);
            mMobileType[i]     = (ImageView) findViewById(mMobileTypeResourceId[i]);
            mNoSimSlot[i]      = (ImageView) findViewById(mNoSimSlotResourceId[i]);
        }
        //update to 5.0 begin
//        applySubscription(MSimTelephonyManager.getDefault().getDefaultSubscription());
        applySubscription((int)SubscriptionManager.getDefaultSubId());
        //update to 5.0 end
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup      = null;
        mWifi           = null;
        mWifiActivity   = null;
        mSpacer         = null;
        mSpacer2         = null;
        mWifiSpacer     = null;
        mAirplane       = null;
        for (int i = 0; i < mNumPhones; i++) {
            mMobileGroup[i]    = null;
            mMobile[i]         = null;
            mMobileActivity[i] = null;
            mMobileType[i]     = null;
            mNoSimSlot[i]      = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;
//update to 5.0 begin
//        applySubscription(MSimTelephonyManager.getDefault().getDefaultSubscription());
        applySubscription((int)SubscriptionManager.getDefaultSubId());
        //update to 5.0 end
    }

    @Override
    public void setMobileDataIndicators(boolean visible, int strengthIcon, int activityIcon,
            int typeIcon, String contentDescription, String typeContentDescription,
            int noSimIcon, int subscription) {
        mMobileVisible = visible;
        mMobileStrengthId[subscription] = strengthIcon;
        mMobileActivityId[subscription] = activityIcon;
        mMobileTypeId[subscription] = typeIcon;
        mMobileDescription[subscription] = contentDescription;
        mMobileTypeDescription = typeContentDescription;
        mNoSimIconId[subscription] = noSimIcon;

        applySubscription(subscription);
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;
        //update to 5.0 begin
//        applySubscription(MSimTelephonyManager.getDefault().getDefaultSubscription());
        applySubscription((int)SubscriptionManager.getDefaultSubId());
        //update to 5.0 end
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
		
		//steve.tang 2014-07-16 fix null pointer exception. start
		//if (mMobileVisible && mMobileGroup[MSimConstants.DEFAULT_SUBSCRIPTION].getContentDescription() != null)
        //update to 5.0 begin
        //if (mMobileVisible && mMobileGroup[MSimConstants.DEFAULT_SUBSCRIPTION]!= null && mMobileGroup[MSimConstants.DEFAULT_SUBSCRIPTION].getContentDescription() != null)
        if (mMobileVisible && mMobileGroup[DEFAULT_SUBSCRIPTION]!= null && mMobileGroup[DEFAULT_SUBSCRIPTION].getContentDescription() != null)
        
		//steve.tang 2014-07-16 fix null pointer exception. start
//            event.getText().add(mMobileGroup[MSimConstants.DEFAULT_SUBSCRIPTION].
//                    getContentDescription());
        	event.getText().add(mMobileGroup[DEFAULT_SUBSCRIPTION].
                    getContentDescription());
      //update to 5.0 end
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    // Run after each indicator change.
    private void applySubscription(int subscription) {
		// Steve.Tang 2014-07-17 get data connect state. start
    	//update to 5.0 begin
//    	boolean isDataConnected = MSimTelephonyManager.getDefault().getDataState()==2;
    	boolean isDataConnected = TelephonyManager.getDefault().getDataState()==2;
    	//update to 5.0 end
		// Steve.Tang 2014-07-17 get data connect state. end
        if (mWifiGroup == null) return;

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }
        
        if (mWifiVisible) {
            mWifiSpacer.setVisibility(View.INVISIBLE);
        } else {
            mWifiSpacer.setVisibility(View.GONE);
        }
        
        if (DEBUG) Slog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                (mWifiVisible ? "VISIBLE" : "GONE"), mWifiStrengthId, mWifiActivityId));

        if (mMobileVisible && !mIsAirplaneMode) {
            mMobileGroup[subscription].setVisibility(View.VISIBLE);
            mMobile[subscription].setImageResource(mMobileStrengthId[subscription]);
            mMobileGroup[subscription].setContentDescription(mMobileTypeDescription + " "
                + mMobileDescription[subscription]);
            mMobileActivity[subscription].setImageResource(mMobileActivityId[subscription]);
            mMobileType[subscription].setImageResource(mMobileTypeId[subscription]);
			
			// Steve.Tang 2014-07-17 if wifi on & Data disconnect, dismiss MobileTyoe. start
			//mMobileType[subscription].setVisibility(!mWifiVisible ? View.VISIBLE : View.GONE);
			mMobileType[subscription].setVisibility(
                (!mWifiVisible && isDataConnected) ? View.VISIBLE : View.GONE);
            // Steve.Tang 2014-07-17 if wifi on & Data disconnect, dismiss MobileTyoe. end
            mNoSimSlot[subscription].setImageResource(mNoSimIconId[subscription]);
        } else {
            mMobileGroup[subscription].setVisibility(View.GONE);
        }

        if (mIsAirplaneMode) {
            mAirplane.setVisibility(View.VISIBLE);
            mAirplane.setImageResource(mAirplaneIconId);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        if (subscription != 0) {
            if (mMobileVisible && mWifiVisible && ((mIsAirplaneMode) ||
                    (mNoSimIconId[subscription] != 0))) {
                mSpacer.setVisibility(View.INVISIBLE);
            } else {
                mSpacer.setVisibility(View.GONE);
            }
        }
        

    }
}
