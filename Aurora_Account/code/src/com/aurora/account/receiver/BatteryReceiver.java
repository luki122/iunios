/**
 * 
 */
package com.aurora.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;

/**
 * 获取电池电量信息
 * 
 * @author JimXia
 * @date 2015年5月5日 下午2:11:13
 */
public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";
    
    private static final float MIN_BATTERY_LEVEL = 0.2f;
    
    private int mBatteryScale = -1;
    private int mBatteryLevel = -1;
    
    private final OnBatteryChangedCallback mCallback;
    
    private final Handler mHandler;
    
    public BatteryReceiver(OnBatteryChangedCallback callback) {
        mCallback = callback;
        mHandler = new Handler();
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {            
        String action = intent.getAction();
        Log.d(TAG, "Jim, action: " + action);
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Log.d(TAG, "Jim, batteryScale: " + batteryScale + ", batteryLevel: " + batteryLevel);
            mBatteryLevel = batteryLevel;
            mBatteryScale = batteryScale;
            
            doCallback();
        }
    }
    
    private void doCallback() {
        if (mCallback != null) {
            mHandler.post(mCallbackRunnable);
        }
    }
    
    private final Runnable mCallbackRunnable = new Runnable() {
        @Override
        public void run() {
            mCallback.onBatteryChanged(BatteryReceiver.this, mBatteryScale, mBatteryLevel);
        }
    };
    
    public static interface OnBatteryChangedCallback {
        void onBatteryChanged(BatteryReceiver receiver, int batteryScale, int batteryLevel);
    }
    
    public boolean canCheckBattery() {
        return (mBatteryScale != -1 && mBatteryLevel != -1);
    }
    
    public boolean checkBattery() {
        if (mBatteryScale > 0 && ((mBatteryLevel * 1.0f / mBatteryScale) > MIN_BATTERY_LEVEL)) {
            // 电池电量大于20%
            return true;
        } else {
            Log.e(TAG, "Jim, battery level <= 20.");
        }
        
        return false;
    }
    
    public int getBatteryLevel() {
        return mBatteryLevel;
    }
    
    public int getBatteryScale() {
        return mBatteryScale;
    }
    
    public void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(this, intentFilter);
        Log.d(TAG, "Jim, battery receiver is registered.");
    }
    
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(this);
        Log.d(TAG, "Jim, battery receiver is unregistered.");
    }
}
