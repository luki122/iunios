package com.gionee.mms.geolocation;

import java.util.ArrayList;
import java.util.Iterator;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;

public class GnLocation implements MKSearchListener, LocationListener {

    private Context mContext;
    private BMapManager mBMapMan = null;
    private MKLocationManager mLocationManager;
    private MKSearch mMKSearch;
    private GnLocationListener mCompleteListener = null;
    private GeoPoint mPoint = null;
    private String mAddress;
    private ArrayList<String> mListPoi;
    private int mNetworkType;
    private boolean mNeedLocate;
    

    private final String mStrKey = "EF25ED04E6B0328290CA6D62C4C88F91D45D530A";
    
    private AuroraProgressDialog mDialog;

    public GnLocation(Context context) {
        mContext = context;
        mListPoi = new ArrayList<String>();
        mNeedLocate = true;
    }

    public void init() {
        // gionee zhouyj 2012-12-11 modify for CR00741255 start 
        if(!checkNetworkState()) {
            Intent intent = new Intent("gn.android.intent.action.SHOW_3GWIFIALERT");
            intent.putExtra("appname", mContext.getPackageName());
            mContext.sendBroadcast(intent);
        } else {
            // gionee zhouyj 2012-12-17 add for CR00746553 start 
            if (mCompleteListener != null) {
                mCompleteListener.onShowDialog();
            }
            // gionee zhouyj 2012-12-17 add for CR00746553 end 
            startLocation();
        }
        // gionee zhouyj 2012-12-11 modify for CR00741255 end
    }
    
    private boolean isWithoutTip() {
        SharedPreferences pref = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getBoolean("location_without_tip", false);
    }
    
    public void startLocation() {
        boolean gpsState = checkGPSState();
        mBMapMan = new BMapManager(mContext);
        mBMapMan.init(mStrKey, null);
        mBMapMan.start();
        mMKSearch = new MKSearch();
        mMKSearch.init(mBMapMan, this);
        mLocationManager = mBMapMan.getLocationManager();
        mLocationManager.requestLocationUpdates(this);
        if(gpsState) {
            mLocationManager.enableProvider(MKLocationManager.MK_GPS_PROVIDER);
        }
        //Gionee <zhouyj> <2013-04-18> modify for CR00797118 start
        onShowProgressDialog(mContext.getString(R.string.gn_location_error));
        //Gionee <zhouyj> <2013-04-18> modify for CR00797118 end
    }
    
    private boolean checkNetworkState() {
        ConnectivityManager cManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected()) {
            mNetworkType = info.getType();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean checkGPSState() {
        LocationManager alm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    public interface GnLocationListener {
        void onLocateComplete(String addr);
        void onLocatePoi(ArrayList<String> poi);
        void onShowDialog();
    }
    
    public void setLocationListener(GnLocationListener listener) {
        mCompleteListener = listener;
        if(null != mCompleteListener) {
            init();
        }
    }

    public void onGetPoi() {
        if(!checkNetworkState()) {
            onPause();
            Toast.makeText(mContext, mContext.getString(R.string.gn_location_nonet), Toast.LENGTH_SHORT).show();
            return ;
        }
        mNeedLocate = true;
        if(mListPoi.size() > 2) {
            mCompleteListener.onLocatePoi(mListPoi);
        } else {
            if (null != mPoint) {
                //Gionee <zhouyj> <2013-04-18> modify for CR00797118 start
                onShowProgressDialog(mContext.getString(R.string.gn_location_no_others_poi));
                //Gionee <zhouyj> <2013-04-18> modify for CR00797118 end
                mMKSearch.poiSearchNearBy(mAddress, mPoint, 500);
            }
        }
    }

    public void onPause() {
        if (mBMapMan != null) {
            mBMapMan.stop();
        }
    }

    public void onResume() {
        if (mBMapMan != null) {
            mBMapMan.start();
        }
    }

    public void onDestroy() {
        if (mBMapMan != null) {
            // gionee zhouyj 2012-12-17 add for CR00746553 start 
            Intent intent = new Intent("gn.android.intent.action.APP_EXIT");
            intent.putExtra("appname", mContext.getPackageName());
            mContext.sendBroadcast(intent);
            // gionee zhouyj 2012-12-17 add for CR00746553 end 
            onDismissDialog();
            mNeedLocate = true;
            mLocationManager.removeUpdates(this);
            mBMapMan.destroy();
            mBMapMan = null;
        }
    }

    public void onLocationChanged(Location loc) {
        // TODO Auto-generated method stub
        // gionee zhouyj 2012-12-03 modify for CR00738278 start 
        if (loc != null) {
            double lat = 0d, lon = 0d;
            lat = loc.getLatitude();
            lon = loc.getLongitude();
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putDouble("lat", lat);
            data.putDouble("lon", lon);
            msg.setData(data);
            mHandler.sendMessage(msg);
        } else {
            new Thread(new LocationRunnable()).start();
        }
        // gionee zhouyj 2012-12-03 modify for CR00738278 end 
    }

    public void onGetWalkingRouteResult(
            MKWalkingRouteResult paramMKWalkingRouteResult, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void onGetTransitRouteResult(
            MKTransitRouteResult paramMKTransitRouteResult, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void onGetSuggestionResult(
            MKSuggestionResult paramMKSuggestionResult, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void onGetPoiResult(MKPoiResult poiResult, int paramInt1,
            int iError) {
        // TODO Auto-generated method stub
        if(0 == iError) {
            mListPoi.clear();
            String address;
            for (MKPoiInfo info : poiResult.getAllPoi()) {
                address = info.address + info.name;
                if(!mListPoi.contains(address))
                    mListPoi.add(address);
            }
            if(mListPoi.size() < 1) {
                Toast.makeText(mContext, mContext.getString(R.string.gn_location_no_others_poi), Toast.LENGTH_LONG).show();
            }
            mCompleteListener.onLocatePoi(mListPoi);
        // gionee zhouyj 2012-09-20 add for CR00699139 start 
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.gn_location_no_others_poi), Toast.LENGTH_LONG).show();
        // gionee zhouyj 2012-09-20 add for CR00699139 end 
        }
        onDismissDialog();
    }

    public void onGetDrivingRouteResult(
            MKDrivingRouteResult paramMKDrivingRouteResult, int paramInt) {
        // TODO Auto-generated method stub
    }

    public void onGetBusDetailResult(MKBusLineResult paramMKBusLineResult,
            int paramInt) {
        // TODO Auto-generated method stub
    }

    public void onGetAddrResult(MKAddrInfo info, int paramInt) {
        // TODO Auto-generated method stub
        if (info == null) {
            return;
        }
        mCompleteListener.onLocateComplete(info.strAddr);
        // gionee zhouyj 2012-09-28 modify for CR00705096 start 
        mAddress = info.addressComponents.street;
        // gionee zhouyj 2012-09-28 modify for CR00705096 end 
        int i = 0;
        if (null != info.poiList) {
            mListPoi.clear();
            String address;
            for (MKPoiInfo poiInfo : info.poiList) {
                address = poiInfo.address + poiInfo.name;
                if(!mListPoi.contains(address))
                    mListPoi.add(address);
            }
        }
        onDismissDialog();
    }
    
    public String getSelectPoi(int pos) {
        if(null != mListPoi && mListPoi.size() > 0 && pos < mListPoi.size()) {
            return mListPoi.get(pos);
        }
        return null;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            double lat = msg.getData().getDouble("lat");
            double lon = msg.getData().getDouble("lon");
            if (lat != 0 && lon != 0) {
                GeoPoint point = new GeoPoint((int) (lat * 1E6),
                        (int) (lon * 1E6));
                mMKSearch.reverseGeocode(point);
                mPoint = point;
            } else {
                onDismissDialog();
                Toast.makeText(mContext, mContext.getString(R.string.gn_location_error), Toast.LENGTH_LONG).show();
            }
        }
    };
    
    //Gionee <zhouyj> <2013-04-18> modify for CR00797118 start
    public void onShowProgressDialog(final String toast) {
        if(null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        // gionee zhouyj 2012-08-21 modify for CR00679296 start
        mDialog = AuroraProgressDialog.show(ComposeMessageActivity.getComposeContext(), "", mContext.getString(R.string.gn_location_loading));
        // gionee zhouyj 2012-08-21 modify for CR00679296 end
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        // gionee zhouyj 2012-09-20 add for CR00699139 start 
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mDialog.isShowing()) {
                    Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
                }
                onDismissDialog();
            }
        }, 15000);
        // gionee zhouyj 2012-09-20 add for CR00699139 end 
    }
    //Gionee <zhouyj> <2013-04-18> modify for CR00797118 end
    
    public void onDismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
    }
    
    public boolean isNeedLocated() {
        return mNeedLocate;
    }
    
    public void setNeedLocated(boolean located) {
        mNeedLocate = located;
    }

    class LocationRunnable implements Runnable {
        public void run() {
            Location location = mLocationManager.getLocationInfo();
            double lat = 0d, lon = 0d;
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putDouble("lat", lat);
            data.putDouble("lon", lon);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }
    }
}
