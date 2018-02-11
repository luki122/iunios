
package com.aurora.ota.reporter;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aurora.ota.database.DataBaseHandler;
import com.aurora.ota.database.ReporterKey;

public class Reporter {

    
    private static final int MSG_DELETE = 0x001;
    private static final int MSG_UPDATE = 0x002;
    private static final int STATUS_REPORTED = 1;
    private static final int STATUS_NOT_REPORTED = 0;
    
    private static final int TIMEOUT = 60;
    
    private static final String TAG = "luofu";
    private static final String REPORTER_ADDR = "http://18.8.5.121:8180/drs/report/rptData?type=&id=&uid=&sid=&data=&0.6250465558841825";
    private static Reporter appStatus;
    private Context mContext;

    private DataBaseHandler dbHandler;
    private ReporterItem mItem;

    private List<ReporterItem> items = new ArrayList<ReporterItem>();
    private List<ReporterItem> needUpdateItems = new ArrayList<ReporterItem>();
    private DefaultHttpClient httpClient;
    
    private Handler mDataUpdateHandler;
    
    StringBuilder result = new StringBuilder();
    
    
    private CallBack callback;
    public interface CallBack{
        void error(ReporterItem item,Error error);
        void success(ReporterItem item);
    }
    
    public enum Error{
        INTERUPT,NET_WORK_ERROR;
    }

    public synchronized static Reporter getAppStatus(Context context) {
        if (appStatus == null) {
            appStatus = new Reporter(context);
        }
        return appStatus;
    }

    private Reporter(Context context) {
        this.mContext = context;
        dbHandler = new DataBaseHandler(mContext);
        
    }
    
    public void registerCallBack(CallBack callback){
        this.callback = callback;
    }
    
    public void storeItem(ReporterItem item){
        dbHandler.insert(item);
    }

    public static final HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192 * 5);
        return params;
    }

    public void startReport(String userName, int resportStatus) {

        final String uname = userName;

        final int rStatus = resportStatus;

        Thread thread = new Thread() {
            @Override
            public void run() {
                List<ReporterItem> tempItem = dbHandler.queryList();
                if (tempItem != null && tempItem.size() > 0) {
                    Log.e("luofu", "size:"+tempItem.size());
                    items.clear();
                    items.addAll(tempItem);
                }
                report(uname, rStatus);

            }
        };
        thread.start();

    }

    private String startupTime;

    private String shutdownTime;

    private void report(String userName, int resportStatus) {
        HttpParams paramsw = createHttpParams();
        String uName = userName;
        int rStatus = resportStatus;
        long time = System.currentTimeMillis();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (resportStatus == 0) {
            startupTime = time + "";
            shutdownTime = null;
            Editor pEdit = prefs.edit();
            pEdit.putString("startupTime", time + "");
            pEdit.commit();

        } else if (resportStatus == 1) {
            startupTime = prefs.getString("startupTime", null);
            shutdownTime = time + "";
        }

        // 真实环境地址
        /*
         * HttpPost httppost = new HttpPost(
         * "http://rd.gionee.com/dp/sendAppAccessLog");
         */
        // 开发机测试地址
        HttpPost httppost = new HttpPost(REPORTER_ADDR);

        httpClient = new DefaultHttpClient(paramsw);
        try {

            if (items.size() > 0) {
               
                for (int index = 0;index<items.size();index++) {
                    ReporterItem i = items.get(index);
                    Log.e(TAG, ""+i.toString());
                    if(i.getReported() == STATUS_REPORTED){
                        if(callback != null){
                            callback.success(i);
                        }
                        continue;
                    }
                    JSONObject json = getReporterJson(i);

                    StringEntity data = new StringEntity(json.toString());

                    /*
                     * data.setContentEncoding(new
                     * BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                     */

                    httppost.addHeader("Content-Type", "application/json");
                    httppost.setEntity(data);

                    HttpResponse httpResponse = httpClient.execute(httppost);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        // 获得返回结果
                        String entity = EntityUtils.toString(httpResponse.getEntity());
                        Log.e(TAG,
                                "EntityUtils="
                                        + entity);
                        JSONObject resultJson = new JSONObject(entity);
                        int status = resultJson.getInt("state");
                        if(status == STATUS_REPORTED){
                            i.setReported(1);
                           
                            if(callback != null){
                                callback.success(i);
                                items.remove(i);
                            }
                            
                        }else{
                            i.setReported(0);
                            Log.e("luofu", "update");
                            if(callback != null){
                                callback.error(i, Error.INTERUPT);
                            }
                        }
                        
                    }

                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Got Exception", e);
        }

    }

    private JSONObject getReporterJson(ReporterItem item) {
        JSONObject json = new JSONObject();
        try {

            json.put(ReporterKey.KEY_APP_VERSION, item.getApkVersion());
            json.put(ReporterKey.KEY_APP_NAME, item.getAppName());
            json.put("channelName", "gbt5700a 1017");

            json.put(ReporterKey.KEY_IMEI, item.getImei());
            
            json.put("channelName", item.getmChanel());
            json.put(ReporterKey.KEY_REGISTER_USER_ID, item.getRegisterUserId());
            json.put(ReporterKey.KEY_STATUS, item.getStatus() + "");

            json.put(ReporterKey.KEY_CREATE_ITEM_TIME, item.getStartupTime());
            json.put(ReporterKey.KEY_SHUT_DOWN_TIME, item.getShutdownTime());
            json.put(ReporterKey.KEY_MOBILE_MODEL, item.getMobileModel());// android.os.Build.MODEL);
            json.put(ReporterKey.KEY_MOBILE_NUMBER, item.getMobileNumber());// getPhoneNumber(mContext));

        } catch (Exception e) {
            // TODO: handle exception
        }
        return json;
    }


}
