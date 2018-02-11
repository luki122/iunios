/*
 * Copyright (C) 2006 The Android Open Source Project
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
//@author Aurora <tongyh> <2014-06-28> add network speed monitoring
package com.android.systemui.statusbar.policy;

import android.widget.TextView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.provider.Settings;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.graphics.Typeface;
import android.graphics.Paint;

public class AuroraNetworkSpeedMonitoring extends TextView {
	
    private HandlerThread mHandlerThread;
    private AuroraNetworkSpeedMonitoringHandler mMyHandler;
    Handler handler = new Handler();
    private long mPrevNetSpeed = 0;
    private double mNetSpeed = 0.00d;
    private String actualSpeed = null;
    private boolean mIsCountNetSpeed = false;
    private static final String ACTION_NETWORKS_SPEED = "action_isdisplay_network_speed";
    private static final String DISPLAY = "isdisplay";
    private static final String TABLE_NETWORK_DISPLAY = "isdisplay_network_speed";
    private Context mContext;
    private boolean isonAttachedToWindow = false;
    private boolean canSetText = true;
	private boolean canRemoveShow = false;
    
    Runnable mHandlerThreadRunnable = new Runnable()
    {
        public void run()
        {
        	actualSpeed = CalculateNetworkSpeed();
        	mMyHandler.obtainMessage().sendToTarget();
            mMyHandler.postDelayed(mHandlerThreadRunnable, 3000);

/*            Log.v("Baisha", "mHandlerThreadRunnable run() 1");
			if (!(getMobileConnectState() || getWifiConnectState())) {
				 if(getVisibility() == View.VISIBLE){
        		    setVisibility(View.GONE);
        		    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
        		}
			}
			Log.v("Baisha", "mHandlerThreadRunnable run() 2");*/
        }
    };

	Runnable mHandlerThreadShow = new Runnable() 
	{
	    public void run()
	    {
	        Log.v("Baisha", "mHandlerThreadShow run()");
		    setVisibility(View.GONE);
			mMyHandler.removeCallbacks(mHandlerThreadRunnable);	       
	    }
	};
	
    //udpate ui
    Runnable updateUi = new Runnable()
    {
        public void run()
        {
        	if(actualSpeed == null || actualSpeed.equals("")){
        		actualSpeed = "0.00K/s";
        	}
			Log.v("Baisha1", "Runnable run() canSetText = " + canSetText + " actualSpeed = " + actualSpeed);
        	if(!canSetText){
        		actualSpeed = "0.00K/s";
        		canSetText = true;
        	}

			Log.v("Baisha", "setText(actualSpeed) actualSpeed = " + actualSpeed);
			// Aurora <tongyh> <2015-03-18> network speed bold begin
		       getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
				getPaint().setStrokeWidth(0.7f);
			// Aurora <tongyh> <2015-03-18>  network speed bold end
        	setText(actualSpeed);

			Log.v("Baisha1", "Runnable run() 1");
			if (!(getMobileConnectState() || getWifiConnectState())) {
				Log.v("Baisha", "handler.postDelayed(mHandlerThreadShow, 2000);");
				handler.removeCallbacks(mHandlerThreadShow);
				handler.postDelayed(mHandlerThreadShow, 2000);

				canRemoveShow = true;
			} else {
                if (canRemoveShow) {
				    handler.removeCallbacks(mHandlerThreadShow);
					canRemoveShow = false;
					Log.v("Baisha", "handler.removeCallbacks(mHandlerThreadShow);");
                }
			}
			
			Log.v("Baisha1", "Runnable run() 2");
        }
    };
    
    public AuroraNetworkSpeedMonitoring(Context context) {
        this(context, null);
    }

    public AuroraNetworkSpeedMonitoring(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuroraNetworkSpeedMonitoring(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mHandlerThread = new HandlerThread("handler_thread");
	    mHandlerThread.start();
	    mMyHandler = new AuroraNetworkSpeedMonitoringHandler(mHandlerThread.getLooper());
	    canSetText = true;
    }
    
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
    	public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NETWORKS_SPEED)) {
            	mIsCountNetSpeed = intent.getBooleanExtra(DISPLAY, false);
            	if(!(getMobileConnectState() || getWifiConnectState())){
            		//return;
            	}
            	/*if(mIsCountNetSpeed && (getMobileConnectState() || getWifiConnectState())){
            		if(getVisibility() == View.GONE){
            		    setVisibility(View.VISIBLE);
            		}
            		mMyHandler.post(mHandlerThreadRunnable);
            	}else{
            		if(getVisibility() == View.VISIBLE){
            		    setVisibility(View.GONE);
            		}
            		mMyHandler.removeCallbacks(mHandlerThreadRunnable);
            	}*/
            	Log.v("Baisha", "mIntentReceiver ACTION_NETWORKS_SPEED mIsCountNetSpeed = " + mIsCountNetSpeed);
            	openAndColseNetworkSpeedMonitoring();
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
            	if(getVisibility() == View.VISIBLE){
            		if(mIsCountNetSpeed){
            		    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
            		}
            	}
            }else if(Intent.ACTION_SCREEN_ON.equals(action)){
            	if(getVisibility() == View.VISIBLE){
            		if(mIsCountNetSpeed && (getMobileConnectState() || getWifiConnectState())){
            			canSetText = false;
            		    mMyHandler.post(mHandlerThreadRunnable);
            		}
            	}
            }
/*            else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
            	Log.d("0504", "action111111111111111 = " + action);
//            	public static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
            		if(!(getWifiConnectState() || getMobileConnectState())){
            			if(getVisibility() == View.VISIBLE){
            			    setVisibility(View.GONE);
            			}
            			if(mIsCountNetSpeed){
            			    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
            			}
                	}else{
                		Log.d("0504", "mIsCountNetSpeed = " + mIsCountNetSpeed);
                		if(getVisibility() == View.GONE){
                			if(mIsCountNetSpeed){
                		        setVisibility(View.VISIBLE);
                		        Log.d("0504", "mIsCountNetSpeed ----------come in ");
                		        mMyHandler.post(mHandlerThreadRunnable);
                			}
                		}
                		
                	}
            	getWifiConnectState();
            	openAndColseNetworkSpeedMonitoring();
            	
            }*/
            else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
            	Log.d("0504", "action222222222222222 = " + action);
            	/*if(!(getWifiConnectState() || getMobileConnectState())){
        			if(getVisibility() == View.VISIBLE){
        			    setVisibility(View.GONE);
        			}
        			if(mIsCountNetSpeed){
        			    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
        			}
            	}else{
            		Log.d("0504", "mIsCountNetSpeed = " + mIsCountNetSpeed);
            			if(mIsCountNetSpeed){
            		        setVisibility(View.VISIBLE);
            		        Log.d("0504", "mIsCountNetSpeed ----------come in ");
            		        mMyHandler.post(mHandlerThreadRunnable);
            			}
            		
            	}*/
//            	getWifiConnectState();
//            	getMobileConnectState();
            	openAndColseNetworkSpeedMonitoring();
            }
    	}
    };
    
    private class AuroraNetworkSpeedMonitoringHandler extends Handler {
	    public AuroraNetworkSpeedMonitoringHandler(Looper looper) {
	    	super(looper);
	    }
	       
	    @Override
	    public void handleMessage(Message msg) {
	        handler.post(updateUi);
	        super.handleMessage(msg);
	    }
	 }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isonAttachedToWindow = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NETWORKS_SPEED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mIntentReceiver, filter);
        /*if(getMobileConnectState() || getWifiConnectState()){
        	if(mIsCountNetSpeed){
            	setVisibility(View.VISIBLE);
            	mMyHandler.post(mHandlerThreadRunnable);
            }else{
            	if(getVisibility() == View.VISIBLE){
            	    setVisibility(View.GONE);
            	}
            }
    	}*/
	    mIsCountNetSpeed = getIsDisplay();
        
        openAndColseNetworkSpeedMonitoring();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(isonAttachedToWindow){
        	mMyHandler.removeCallbacks(mHandlerThreadRunnable);
        }
        isonAttachedToWindow = false;
    }
    
    private boolean isWifiOnlyDevice() {
        ConnectivityManager cm = ( ConnectivityManager ) mContext.getSystemService(
                mContext.CONNECTIVITY_SERVICE);
        return !(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE));
    }
    
    private long getTotalReceivedBytes() {
        String line;
        String[] segs;
        long received = 0;
        int i;
        long tmp = 0;
        boolean isNum;
        try {
            FileReader fr = new FileReader("/proc/net/dev");
            BufferedReader in = new BufferedReader(fr, 500);
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("rmnet") || line.startsWith("eth") || line.startsWith("wlan") || line.startsWith("netts") 
					|| line.startsWith("ccmni")) {
                    segs = line.split(":")[1].split(" ");
                    for (i = 0; i < segs.length; i++) {
                        isNum = true;
                        try {
                            //tmp = Integer.parseInt(segs[i]);
                            tmp = Long.parseLong(segs[i]);
                        } catch (Exception e) {
                            isNum = false;
                        }
                        if (isNum == true) {
                            received = received + tmp;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return received;
    }
    
    public String CalculateNetworkSpeed(){
    	if (mPrevNetSpeed == 0) {
            mPrevNetSpeed = getTotalReceivedBytes();
        } else {
            mNetSpeed = (getTotalReceivedBytes() - mPrevNetSpeed)/2.00d;
            if(mNetSpeed < 0){
            	mNetSpeed = 0.00d;
            }
            mPrevNetSpeed = getTotalReceivedBytes();
        }
    	
    	DecimalFormat df = new DecimalFormat("###.##");
    	String mNetworkSpeed = null;
    	if (mNetSpeed < 1024*999) {
    		mNetworkSpeed = getNetworkSpeed(df.format(mNetSpeed/1024d)) + "K/s";
        } else {
        	mNetworkSpeed = getNetworkSpeed(df.format(mNetSpeed/(1024d*1024d))) + "M/s";
        }
    		return mNetworkSpeed;
    	
    }
    
    private boolean getMobileConnectState(){
    	// Aurora <tongyh> <2015-03-09> bug 11815 begin
    	ConnectivityManager manager = null;
        try {
			manager = (ConnectivityManager)mContext.getSystemService(
			        Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
        // Aurora <tongyh> <2015-03-09> bug 11815 end
        State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if(mobile == State.CONNECTED){
        	Log.d("0504", "getMobileConnectState---true");
            return true;
        }else{
        	Log.d("0504", "getMobileConnectState---false");
            return false;
        }
    }
    
    private boolean getWifiConnectState(){
//    	WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context
//    			.WIFI_SERVICE);
//    	Log.d("0504", "WifiManager.WIFI_STATE_ENABLED == mWifiManager.getWifiState() = " + (WifiManager.WIFI_STATE_ENABLED == mWifiManager.getWifiState()));
//    	return WifiManager.WIFI_STATE_ENABLED == mWifiManager.getWifiState();
//    	WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context
//		.WIFI_SERVICE);
        ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(
        Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = (NetworkInfo) manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected();
    }
    
    private String getNetworkSpeed(String speed){
    	if(!speed.contains(".")){
    		if(speed.length() == 1){
    			return speed + ".00";
    		}else if(speed.length() == 2){
    			return speed + ".0";
    		}
    		return speed;
    	}else{
    		String[] separationSpeed = speed.split("\\.");
        	if(separationSpeed[0].length() == 1){
        		if(separationSpeed[1].length() < 2){
        			return speed + "0";
        		}
        		return speed;
        	}else if(separationSpeed[0].length() == 2){
        		return (new BigDecimal(speed).setScale(1, BigDecimal.ROUND_HALF_UP)).toString();
        	}else{
        		return (new BigDecimal(speed).setScale(0, BigDecimal.ROUND_HALF_UP)).toString();
        	}
    	}
    	
    	
    }
    
    private void openAndColseNetworkSpeedMonitoring(){
    	Log.v("Baisha", "openAndColseNetworkSpeedMonitoring mIsCountNetSpeed = " + mIsCountNetSpeed);
		Log.v("Baisha", "openAndColseNetworkSpeedMonitoring getMobileConnectState() = " + 
			getMobileConnectState() + " getWifiConnectState() = " + getWifiConnectState());
    	if(mIsCountNetSpeed){
    		if(true/*getMobileConnectState() || getWifiConnectState()*/){
    			if(true/*getVisibility() == View.GONE*/){
					Log.v("Baisha", "openAndColseNetworkSpeedMonitoring getVisibility() == View.GONE");
    			    setVisibility(View.VISIBLE);
    			    canSetText = true;
					mMyHandler.removeCallbacks(mHandlerThreadRunnable);
    			    mMyHandler.post(mHandlerThreadRunnable);
    			}
            	
    		}else{
    			/*if(getVisibility() == View.VISIBLE){
        		    setVisibility(View.GONE);
        		    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
        		}*/
        		
    		}
    	}else{
    		if(getVisibility() == View.VISIBLE){
				Log.v("Baisha", "openAndColseNetworkSpeedMonitoring getVisibility() == View.VISIBLE");
    		    setVisibility(View.GONE);
    		    mMyHandler.removeCallbacks(mHandlerThreadRunnable);
    		}
    	}
    }
    
    
    private boolean getIsDisplay(){
    	try{
	    	Context settingsAppsContext = mContext.createPackageContext("com.android.settings", Context
	    			.CONTEXT_IGNORE_SECURITY);
	    	SharedPreferences sharedPreferences = settingsAppsContext
	        		.getSharedPreferences(TABLE_NETWORK_DISPLAY, Context
	        				.MODE_WORLD_READABLE);
	        return sharedPreferences.getBoolean(DISPLAY, false);
        } catch (NameNotFoundException e) {
        	return false;
        }
    	
    }

}
