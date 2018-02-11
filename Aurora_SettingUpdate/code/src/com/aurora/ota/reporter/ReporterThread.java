
package com.aurora.ota.reporter;

import gn.com.android.update.utils.LogUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.ota.database.DataBaseHandler;
import com.aurora.ota.database.ReporterKey;
import com.aurora.ota.location.LocationInfo;

public class ReporterThread extends Thread {

    private static final String LOCATION_COUNTRY = "locationCountry";
    
    private static final String LOCATION_PROVINCE="locationProvince";
    
    private static final String LOCATION_CITY="locationCity";
    
    private static final String LOCATION_TIME="locationTime";
    
    private static final String PHONE_NUMBER = "mobileNumber";
    
    private static final String PHONE_WIDTH = "modelWidth";
    
    private static final String PHONE_HEIGHT="modelHeight";
    
    private static final String NET_WORK_IP = "networkIp";
    
    private static final int STATUS_REPORTED = 1;
    private static final int STATUS_NOT_REPORTED = 0;

    private static final int TIMEOUT = 60;

    private static final String TAG = "ReporterThread";
    /*Test Server*/
//    private static final String REPORTER_ADDR = ""
//            +
//            "http://18.8.5.121:8580/dp/sendAppAccessLog";
    private static final String REPORTER_ADDR = ""
            + "http://data.iunios.com/dp/sendAppAccessLog";
//    private static final String REPORTER_ADDR = ""
//          + "http://data.iunios.com/dp/sendAppAccessLog";
    
    private ReporterItem mItem;

    private NetFocade mNetFacade;
    private List<ReporterItem> items = new ArrayList<ReporterItem>();

    private DefaultHttpClient httpClient;
    StringBuilder result = new StringBuilder();

    private CallBack callback;
    
    private String mCountry;
    private String mProvince;
    private String mCity;
    private String mLocationTime;
    private String mIp;
    

    public interface CallBack {
        void error(ReporterItem item, Error error);

        void success(ReporterItem item);

        void interupt(ReporterItem item, Error error);
    }

    public enum Error {
        INTERUPT, NET_WORK_ERROR, BYTE_OVERFLOW,DO_NOT_HAS_ADDRESS;
    }

    @Override
    public void run() {
        if (mItem != null) {
            report(mItem);
        }
    }

    public void setItem(ReporterItem item) {
        this.mItem = item;
    }

    public ReporterThread(ReporterItem item, NetFocade netFacade) {
        this.mItem = item;
        this.mNetFacade = netFacade;

    }
    
    public ReporterThread(ReporterItem item, NetFocade netFacade,LocationInfo location) {
        this.mItem = item;
        this.mNetFacade = netFacade;

        if(location != null){
            Log.d(TAG, "ReporterThread:"+location.getCoutry());
            mCity = location.getCity();
            mProvince = location.getProvince();
            mCountry = location.getCoutry();
            mLocationTime = location.getTime();
            mIp = location.getIp();
        }
    }

    public void registerCallBack(CallBack callback) {
        this.callback = callback;
    }

    public static final HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192 * 5);
        return params;
    }

    
    private void setLocation(){
        
    }
    
    private void report(ReporterItem item) {
        
//        if(item != null){
//            if(item.getLocation().equals(Constants.SPLITE)){
//                if(callback != null){
//                    callback.error(item, Error.DO_NOT_HAS_ADDRESS);
//                    return;
//                }
//            }
//        }
        Log.d(TAG, "country:"+mCountry+" province:"+mProvince+" city:"+mCity);
        if(TextUtils.isEmpty(mCountry) || TextUtils.isEmpty(mCity) || TextUtils.isEmpty(mProvince)){
//            if(callback != null){
//                callback.error(item, Error.DO_NOT_HAS_ADDRESS);
//                return;
//            }
        }
        
        HttpParams paramsw = createHttpParams();
        // long time = System.currentTimeMillis();
        // SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(mContext);

        HttpPost httppost = new HttpPost(REPORTER_ADDR);
        Log.d(TAG, "REPORTER_ADDR "  + REPORTER_ADDR);
        httpClient = new DefaultHttpClient(paramsw);
        try {
            if (item.getReported() == STATUS_REPORTED) {
                if (callback != null) {
                    callback.success(item);
                }
                
                return;
            }
            JSONObject json = getReporterJson(item);

            StringEntity data = new StringEntity(json.toString(),HTTP.UTF_8);
//            if(data != null){
////                Log.e("luofu", json.toString());
//            }
            
            httppost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httppost.setEntity(data);
            if (!mNetFacade.isWIFIConnection()) {//wifi 下上传
                callback.error(item, Error.NET_WORK_ERROR);
                return;
            }

            if (mNetFacade.isWIFIConnection()) {
                if (mNetFacade.getMaxByteOverWifi() < 1000) {
                    executeHttp(httpClient, httppost, item);
                } else {
                    callback.interupt(item, Error.BYTE_OVERFLOW);
                }

            } else if (mNetFacade.isMobileNetwork()) {
                if (mNetFacade.getMaxByteOverMobile() < 100) {
                    executeHttp(httpClient, httppost, item);
                } else {
                    callback.interupt(item, Error.BYTE_OVERFLOW);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "json Exception");
        }

    }

    private void executeHttp(DefaultHttpClient httpClient, HttpUriRequest request, ReporterItem item) {
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            Log.d(TAG, "executeHttp");
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.d(TAG, "statusCode:"+statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                // 获得返回结果
                String entity = EntityUtils.toString(httpResponse.getEntity());
                Log.d(TAG,"report HttpResult =   "+ entity);
                JSONObject resultJson = new JSONObject(entity);
                int status = resultJson.getInt("state");
                if (status == STATUS_REPORTED) {
                    item.setReported(1);

                    if (callback != null) {
                        callback.success(item);
                        items.remove(item);
                    }

                } else {
                    item.setReported(0);
                    if (callback != null) {
                        callback.error(item, Error.INTERUPT);
                    }
                }

            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "Got Exception", e);
        }

    }

    private JSONObject getReporterJson(ReporterItem item) {
        JSONObject json = new JSONObject();
        try {

            json.put(ReporterKey.KEY_APP_VERSION, item.getApkVersion());
            json.put(ReporterKey.KEY_APP_NAME, item.getAppName());
            json.put("channelName", item.getmChanel());

            json.put(ReporterKey.KEY_IMEI, item.getImei());
            json.put(ReporterKey.KEY_REGISTER_USER_ID, item.getRegisterUserId());
            json.put(ReporterKey.KEY_STATUS, item.getStatus() + "");

            json.put(ReporterKey.KEY_CREATE_ITEM_TIME, item.getCreatItemTime());
            json.put(ReporterKey.KEY_SHUT_DOWN_TIME, item.getShutdownTime());
            json.put(ReporterKey.KEY_MOBILE_MODEL, item.getMobileModel());// android.os.Build.MODEL);
            json.put(ReporterKey.KEY_MOBILE_NUMBER, item.getMobileNumber());// getPhoneNumber(mContext));
            json.put(LOCATION_COUNTRY, mCountry);
            json.put(LOCATION_PROVINCE, mProvince);
            json.put(LOCATION_CITY, mCity);
            json.put(LOCATION_TIME, mLocationTime);
            json.put(PHONE_HEIGHT, item.getPhoneHeight());
            json.put(PHONE_WIDTH, item.getPhoneWidth());
            json.put(NET_WORK_IP, mIp);
            	json.put(ReporterKey.KEY_APP_NUM, "" + item.getAppNum());
            json.put(ReporterKey.KEY_BOOT_TIME, item.getStartupTime());
            json.put(ReporterKey.KEY_DURATION_TIME, item.getDuration());
            	
            Log.d(TAG, "josn =    "+json.toString());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return json;
    }

}
