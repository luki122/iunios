package com.xy.smartsms.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import cn.com.xy.sms.sdk.action.AbsSdkDoAction;

import android.util.Log;

/*
Aurora xuyong 2016-03-11 created for bug #20730
This class must be called in the main thread.
*/
public class LocationProvider {

    public final static String TAG = "LocationProvider";
    private LocationClient locationClient;
    private Context context;
    private Handler mHandler;

    private Object  objLock = new Object();

    public LocationProvider(Context context, Handler handler){
        synchronized (objLock) {
            if (locationClient == null) {
                mHandler = handler;
                locationClient = new LocationClient(context);
                initLocation();
                locationClient.registerLocationListener(mBDLocationListener);
            }
        }
    }

    public void start(){
        synchronized (objLock) {
            if(locationClient != null && !locationClient.isStarted()){
                locationClient.start();
            }
        }
    }
    public void stop(){
        synchronized (objLock) {
            if(locationClient != null && locationClient.isStarted()){
                locationClient.stop();
                locationClient.unRegisterLocationListener(mBDLocationListener);
            }
        }
    }

    public int request() {
        int resultCode = locationClient.requestLocation();
        return resultCode;
    }

    private BDLocationListener mBDLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            Log.e(TAG, "onReceiveLocation!");
            if (location != null) {
                Log.e(TAG, "location != null!");
                //if (location.hasAddr()) {
                    Message msg = mHandler.obtainMessage(AbsSdkDoAction.DO_SEND_MAP_QUERY_URL);
                    Bundle bundle = new Bundle();
                    bundle.putDouble("latitude", location.getLatitude());
                    Log.e(TAG, "location.getLatitude() is " + location.getLatitude());
                    bundle.putDouble("longitude", location.getLongitude());
                    Log.e(TAG, "location.getLongitude() is " + location.getLongitude());
                    msg.setData(bundle);
                    msg.sendToTarget();
                    stop();
                    return;
                //}
            }
            stop();

        }
    };

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
        option.setCoorType("gcj02");
        option.setScanSpan(1500);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }


}