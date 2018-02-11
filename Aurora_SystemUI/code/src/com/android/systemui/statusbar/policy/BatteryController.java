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

package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

import com.android.systemui.Xlog;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import com.gionee.featureoption.FeatureOption;

import android.os.Handler;
import android.os.Build;


public class BatteryController extends BroadcastReceiver {
    private static final String TAG = "StatusBar.BatteryController";

    /// M: Support "Battery Percentage Switch"
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";
    /// M: Broadcast Action: Charger is over voltage.
    private static final String ACTION_CHARGER_OVER_VOLTAGE = "mediatek.intent.action.CHARGER_OVER_VOLTAGE";
    /// M: Broadcast Action: Battery is over temperature.
    private static final String ACTION_BATTER_OVER_TEMPERATURE = "mediatek.intent.action.BATTER_OVER_TEMPERATURE";
    /// M: Broadcast Action: over current-protection situation.
    private static final String ACTION_OVER_CURRENT_PROTECTION = "mediatek.intent.action.OVER_CURRENT_PROTECTION";
    /// M: Broadcast Action: Battery is over voltage.
    private static final String ACTION_BATTER_OVER_VOLTAGE = "mediatek.intent.action.BATTER_OVER_VOLTAGE";
    /// M: Broadcast Action: Over 12hours, battery does not charge full.
    private static final String ACTION_SAFETY_TIMER_TIMEOUT = "mediatek.intent.action.SAFETY_TIMER_TIMEOUT";

    private Context mContext;
    private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();
    /// M: Support "battery percentage". @{
    private boolean mShouldShowBatteryPercentage = false;

    /// @}
    /// M: Support "BATTER_PROTECTION".
    private boolean mBatteryProtection = false;
	// Aurora <zhanggp> <2013-11-01> modified for systemui begin
	private boolean mChargingNow = false;
	 private String mBatteryPercentage = "100";
	 //private String mBatteryPercentage = "100%";
	// Aurora <zhanggp> <2013-11-01> modified for systemui end
	// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
	private boolean pluggedBefore = false;
	private int delayTime = 2000;
	// Aurora <Steve.Tang> 2014-09-22 add charge animation, end

    private ArrayList<BatteryStateChangeCallback> mChangeCallbacks =
            new ArrayList<BatteryStateChangeCallback>();

    public interface BatteryStateChangeCallback {
        public void onBatteryLevelChanged(int level, boolean pluggedIn);
    }

    public BatteryController(Context context) {
        mContext = context;
        /// M: Support "battery percentage".
        mShouldShowBatteryPercentage = (Settings.Secure.getInt(context
                .getContentResolver(), FeatureOption.BATTERY_PERCENTAGE, 0) != 0);
        Xlog.d(TAG, "BatteryController mShouldShowBatteryPercentage is "
                + mShouldShowBatteryPercentage);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        /// M: Support "BATTER_PROTECTION" @{
        filter.addAction(ACTION_BATTER_OVER_TEMPERATURE);
        filter.addAction(ACTION_OVER_CURRENT_PROTECTION);
        filter.addAction(ACTION_BATTER_OVER_VOLTAGE);
        filter.addAction(ACTION_SAFETY_TIMER_TIMEOUT);
        filter.addAction(ACTION_CHARGER_OVER_VOLTAGE);
        /// @}
        /// M: Support "battery percentage".
        filter.addAction(ACTION_BATTERY_PERCENTAGE_SWITCH);
        context.registerReceiver(this, filter);
    }

    public void addIconView(ImageView v) {
        mIconViews.add(v);
    }
// Aurora <zhanggp> <2013-11-01> added for systemui begin
    public void addBgIconView(ImageView v) {
        mIconViews.add(0,v);
		mIconViews.get(0).setImageResource(R.drawable.stat_sys_battery_bg);

    }
// Aurora <zhanggp> <2013-11-01> added for systemui end
    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    /// M: Support "battery percentage". @{
    private  String getBatteryPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra("level", 0);
        int scale = batteryChangedIntent.getIntExtra("scale", 100);
		// Aurora <zhanggp> <2013-11-01> modified for systemui begin
        return String.valueOf(level * 100 / scale);
		//return String.valueOf(level * 100 / scale) + "%";
		// Aurora <zhanggp> <2013-11-01> modified for systemui end
    }
    /// @}

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Xlog.d(TAG,"BatteryController onReceive action is " + action);
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//            final boolean plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
            /// M: Support "BATTER_PROTECTION" @{
            int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            Xlog.d(TAG, "status = " + status);
            if (status != BatteryManager.BATTERY_STATUS_DISCHARGING
                    && status != BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                mBatteryProtection = false;
            }
            
            boolean plugged = false;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                case BatteryManager.BATTERY_STATUS_FULL:
                    plugged = true;
                    break;
            }
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
			final int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			if (plugType == BatteryManager.BATTERY_PLUGGED_USB) {
				delayTime = 7000;
			}else{
				delayTime = 2000;
			}
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, end

            final boolean fulled = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) == 100;
            final int icon = (plugged && !fulled && !mBatteryProtection) ? R.drawable.stat_sys_battery_charge 
                                     : R.drawable.stat_sys_battery;
            /// @}
            Xlog.d(TAG,"plugged is " + plugged + " fulled is " + fulled + " mBatteryProtection = "
                    + mBatteryProtection + "  R.drawable.stat_sys_battery_charge is "
                    + R.drawable.stat_sys_battery_charge + " R.drawable.stat_sys_battery is "
                    + R.drawable.stat_sys_battery + "  icon is " + icon);
            int N = mIconViews.size();
			// Aurora <zhanggp> <2013-11-01> modified for systemui begin
            for (int i=0; i<N; i++) {
                ImageView v = mIconViews.get(i);
                v.setImageResource(icon);
                v.setImageLevel(level);
                v.setContentDescription(mContext.getString(R.string.accessibility_battery_level,
                        level));
            }
			/*
            for (int i=0; i<N; i++) {
                ImageView v = mIconViews.get(i);
                v.setImageResource(icon);
                v.setImageLevel(level);
                v.setContentDescription(mContext.getString(R.string.accessibility_battery_level,
                        level));
            }
			*/
			// Aurora <zhanggp> <2013-11-01> modified for systemui end
            N = mLabelViews.size();
            for (int i=0; i<N; i++) {
                TextView v = mLabelViews.get(i);
                v.setText(mContext.getString(R.string.status_bar_settings_battery_meter_format,
                        level));
            }

            for (BatteryStateChangeCallback cb : mChangeCallbacks) {
                cb.onBatteryLevelChanged(level, plugged);
            }

            /// M: Support "battery percentage". @{
            mBatteryPercentage = getBatteryPercentage(intent);
            Xlog.d(TAG,"mBatteryPercentage is " + mBatteryPercentage + " mShouldShowBatteryPercentage is "
                    + mShouldShowBatteryPercentage + " mLabelViews.size() " + mLabelViews.size());
            TextView v = mLabelViews.get(0);
			// Aurora <zhanggp> <2013-11-01> modified for systemui begin
//			ImageView bgView = mIconViews.get(0);
//			ImageView normalView = mIconViews.get(1);
			ImageView normalView = mIconViews.get(0);
			mChargingNow = (R.drawable.stat_sys_battery_charge == icon ? true : false);


            if (mShouldShowBatteryPercentage && !mChargingNow) {
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
				hidePercentChargeAnim(v);
				showPercentWhileCharging(v, (plugged&&fulled));
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
                v.setText(mBatteryPercentage);
                v.setVisibility(View.VISIBLE);
				normalView.setVisibility(View.GONE);
//				bgView.setVisibility(View.VISIBLE);
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
            } else if(mShouldShowBatteryPercentage && mChargingNow){
				if(pluggedBefore != plugged && plugged){
					showPercentChargeAnim(v);
				} else {
					if(animOver) v.setText(mBatteryPercentage);
				}
				v.setVisibility(View.VISIBLE);
				normalView.setVisibility(View.GONE);

			} else {
				hidePercentChargeAnim(v);
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
                v.setVisibility(View.GONE);
//				bgView.setVisibility(View.GONE);
				normalView.setVisibility(View.VISIBLE);
            }
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
			pluggedBefore = plugged;
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
			/*
            if (mShouldShowBatteryPercentage) {
                v.setText(mBatteryPercentage);
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
			*/
			// Aurora <zhanggp> <2013-11-01> modified for systemui end
            /// M: Support "battery percentage". @}
        }
        /// M: Support "BATTER_PROTECTION" @{
        else if (action.equals(ACTION_BATTER_OVER_TEMPERATURE)
                || action.equals(ACTION_BATTER_OVER_VOLTAGE)
                || action.equals(ACTION_CHARGER_OVER_VOLTAGE)
                || action.equals(ACTION_OVER_CURRENT_PROTECTION)
                || action.equals(ACTION_SAFETY_TIMER_TIMEOUT)) {
            mBatteryProtection = true;
        }
        /// @}
        /// M: Support "battery percentage". @{
        else if (action.equals(ACTION_BATTERY_PERCENTAGE_SWITCH)) {
            mShouldShowBatteryPercentage = (intent.getIntExtra("state",0) == 1);
            Xlog.d(TAG, " OnReceive from mediatek.intent.ACTION_BATTERY_PERCENTAGE_SWITCH  mShouldShowBatteryPercentage" +
                    " is " + mShouldShowBatteryPercentage);
			// Aurora <zhanggp> <2013-11-01> modified for systemui begin		
            TextView v = mLabelViews.get(0);
//			ImageView bgView = mIconViews.get(0);
//			ImageView normalView = mIconViews.get(1);
            ImageView normalView = mIconViews.get(0);
			
            if (mShouldShowBatteryPercentage && !mChargingNow) {
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
				hidePercentChargeAnim(v);
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
                v.setText(mBatteryPercentage);
                v.setVisibility(View.VISIBLE);
				normalView.setVisibility(View.GONE);
//				bgView.setVisibility(View.VISIBLE);
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
            } else if(mShouldShowBatteryPercentage && mChargingNow){
				
				showPercentWhileCharging(v, true);
				v.setVisibility(View.VISIBLE);
				normalView.setVisibility(View.GONE);
			}
			// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
			else {
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
				hidePercentChargeAnim(v);
				// Aurora <Steve.Tang> 2014-09-22 add charge animation, end
                v.setVisibility(View.GONE);
//				bgView.setVisibility(View.GONE);
				normalView.setVisibility(View.VISIBLE);

            }
			/*
            TextView v = mLabelViews.get(0);
            if (mShouldShowBatteryPercentage) {
                v.setText(mBatteryPercentage);
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
			*/
			// Aurora <zhanggp> <2013-11-01> modified for systemui end
        }
        /// @}
    }
	
	// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
	private Handler mHandler = new Handler();
	private boolean animOver = true;

	private Runnable chargePercentAnim = new Runnable() {
		
		@Override
		public void run() {
			TextView v = mLabelViews.get(0);
			showPercentWhileCharging(v, true);
			v.setText(mBatteryPercentage);
			animOver = true;
		}
	};

	private void showPercentWhileCharging(TextView v, boolean charged){
			v.setBackground(null);		
			// Aurora <tongyh> <2015-02-27> bug #11761 begin
			if(Build.VERSION.SDK_INT>18){
				v.setTextColor(charged ? 0xFF07D17C : (PhoneStatusBar.STATUSBAR_IS_INVERT ? 0xFF000000 : 0xFFFFFFFF));
			}else{
				v.setTextColor(charged ? 0xFF07D17C : 0xFFFFFFFF);
			}
			
			
			// Aurora <tongyh> <2015-02-27> bug #11761 end
	}

	private void showPercentChargeAnim(TextView v){
		v.setText("");
		v.setTextColor(0x00000000);
		// Aurora <tongyh> <2015-02-27> bug #11761 begin
		if(Build.VERSION.SDK_INT>18){
			if(PhoneStatusBar.STATUSBAR_IS_INVERT){
				v.setBackgroundResource(R.drawable.aurora_charge_flash_black);
			}else{
				v.setBackgroundResource(R.drawable.aurora_charge_flash);
			}
		}else{
			v.setBackgroundResource(R.drawable.aurora_charge_flash);
		}
		
		// Aurora <tongyh> <2015-02-27> bug #11761 end
		mHandler.removeCallbacks(chargePercentAnim);
		animOver = false;
		mHandler.postDelayed(chargePercentAnim, delayTime);
	}

	private void hidePercentChargeAnim(TextView v){
		showPercentWhileCharging(v, false);
		mHandler.removeCallbacks(chargePercentAnim);
		animOver = true;
	}
	// Aurora <Steve.Tang> 2014-09-22 add charge animation, start
}
