package gn.com.android.update.utils;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.EnvironmentConfig;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.business.NetworkConfig.ConnectionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceActivity.Header;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.aurora.ota.config.RegionXMLParser;
//import android.provider.Telephony.SIMInfo;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnSettings;
import com.gionee.internal.telephony.GnPhone;

public class HttpUtils {
    private static final String TAG = "HttpUtils";

    public static HttpClient getDefaultHttpClient(boolean isWapNetwork, String imei) {
        HttpClient httpClient = null;

        httpClient = AndroidHttpClient.newInstance(Util.getUaString(imei));

        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                NetworkConfig.GIONEE_CONNECT_TIMEOUT);
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                NetworkConfig.GIONEE_SOCKET_TIMEOUT);
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, Util.getUaString(imei));
        
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params,  NetworkConfig.GIONEE_CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params,  NetworkConfig.GIONEE_CONNECT_TIMEOUT);
        
        if (isWapNetwork) {
            HttpHost proxy = new HttpHost(NetworkConfig.CONNECTION_MOBILE_DEFAULT_HOST,
                    NetworkConfig.CONNECTION_MOBILE_DEFAULT_PORT);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        return httpClient;
    }

    public static String executeHttpPost(boolean isWapNetwork, String checkurl, String sendData, String imei) {
        LogUtils.log(TAG, "executeHttpPost() checkurl = " + checkurl);
        HttpClient httpClient = null;
        HttpGet httpGet = null;
        Log.e("httpstatus", "status:");
        try {
            
            httpClient = getDefaultHttpClient(isWapNetwork, imei);
            httpGet = new HttpGet(checkurl);
            if (sendData != null) {
                //httpPost.setEntity(new ByteArrayEntity(sendData.getBytes("UTF-8")));
            }
            Log.e("httpstatus", "status  2:");
            HttpResponse response = httpClient.execute(httpGet);
            org.apache.http.Header[] head =  response.getAllHeaders();
             for(int i = 0;i < head.length;i++){
            	 LogUtils.log(TAG, "response.getAllHeaders()  " +i +" =  " + head[i]);
             }
            Log.e("httpstatus", "status:");
            int code = response.getStatusLine().getStatusCode();
            LogUtils.log(TAG, "executeHttpPost() HTTP Code:  " + code);
            Log.e("httpstatus", "status:");
            Log.e("httpstatus", "status:"+code);
            if (code == HttpStatus.SC_OK) {
                String result = null;
                result = EntityUtils.toString(response.getEntity());
                LogUtils.log(TAG, "executeHttpPost() result = " + result);
                return result;
            }else if(code == HttpStatus.SC_BAD_REQUEST){
                return Error.ERROR_STRING_BAD_REQUEST;
            }else if(code == HttpStatus.SC_GATEWAY_TIMEOUT){
                return Error.ERROR_STRING_CONNECTION_TIME_OUT;
                
            }else if(code == HttpStatus.SC_INTERNAL_SERVER_ERROR){
                
                return Error.ERROR_STRING_SERVER_ERROR;
                
            }else if(code == HttpStatus.SC_NOT_FOUND){
                return Error.ERROR_STRING_SERVER_NOT_FOUND;
                
            }else if(code == HttpStatus.SC_REQUEST_TIMEOUT){
                return Error.ERROR_STRING_CONNECTION_TIME_OUT;
                
            }

            
            
        } catch (ConnectTimeoutException e) {
//            e.printStackTrace();
            Log.e("httpstatus", "status: 66");
            return Error.ERROR_STRING_CONNECTION_TIME_OUT;
        } catch(SocketTimeoutException e2){
            Log.e("httpstatus", "status: 55");
            return Error.ERROR_STRING_CONNECTION_TIME_OUT;
        }catch(IOException e){
            Log.e("httpstatus", "status: 33");
            
            return Error.ERROR_STRING_CONNECTION_TIME_OUT;
        }catch(Exception e){
            Log.e("httpstatus", "status: 44");
            return Error.ERROR_STRING_CONNECTION_TIME_OUT;
        }finally {
            disconnectHttpMethod(httpGet);
            closeHttpClient((AndroidHttpClient) httpClient);
        }

        return null;
    }

    public static void closeHttpClient(AndroidHttpClient httpClient) {
        if (httpClient != null) {
            httpClient.close();
        }

    }

    public static void disconnectHttpMethod(HttpRequestBase httpRequest) {
        if (httpRequest != null) {
            if (!httpRequest.isAborted()) {
                httpRequest.abort();
            }
        }
    }

    public static String getServerHost() {
	  /* RegionXMLParser parser;  
	    List<com.aurora.ota.config.Config> configs;  
	    int region = -1;
    	File configFile = new File(EnvironmentConfig.GIONEE_OTA_CONFIG_FILE_NAME);
    	if(configFile.exists()){
    			if(configFile.isFile()){
    				 try {  
    	                    InputStream is = new FileInputStream(configFile);  

    	                    parser = new RegionXMLParser();
    	                    configs = parser.parse(is);  //解析输入流  
    	                    for (com.aurora.ota.config.Config config : configs) {  
    	                    	region = config.getRegion();
    	                    }  
    	                    
    	                    if(region == 1){
    	                    	 return NetworkConfig.IUNIOS_ABROAD_HOST;
    	                    }
    	                    
    	                } catch (Exception e) {  
    	                   LogUtils.loge(TAG,"parse the config_file exception");
    	                }  
    			}
    	}*/
    	/*if(Util.isAbroadVersion()){
    		 return NetworkConfig.IUNIOS_ABROAD_HOST;
    	}*/
        int environment = Util.getEnvironment();

        if (environment == EnvironmentConfig.TEST_ENVIRONMENT_TEST_VERSION
                || environment == EnvironmentConfig.TEST_ENVIRONMENT_NORMAL_VERSION) {
            //return NetworkConfig.TEST_HOST;
            return NetworkConfig.IUNIOS_TEST_HOST;
        } else if(environment == EnvironmentConfig.TEST_ENVIRONMENT_DEVELOPER){
            
            return NetworkConfig.IUNIOS_DEVELOPER_HOST;
        }else if(environment == EnvironmentConfig.NORMAL_ENVIRONMENT_TEST_VERSION){
        	
        	return NetworkConfig.IUNIOS_NORMAL_HOST;
        }else{
          //return NetworkConfig.NORMAL_HOST;
            return NetworkConfig.IUNIOS_NORMAL_HOST;
        }
    }

    public static void getAppendNetworkTypeUrl(StringBuffer url, ConnectionType connectionType,
            boolean isWapNetwork) {
        if (url.indexOf("?") != -1) {
            url.append("&");
        } else {
            url.append("?");
        }

        url.append("nt=");
        switch (connectionType) {
            case CONNECTION_TYPE_2G:
                appene2GToUrl(url, isWapNetwork);
                break;

            case CONNECTION_TYPE_3G:
                url.append("3G");
                break;

            case CONNECTION_TYPE_4G:
                url.append("4G");
                break;

            case CONNECTION_TYPE_WIFI:
                url.append("WF");
                break;

            default:
                break;
        }

    }

    private static void appene2GToUrl(StringBuffer url, boolean isWapNetwork) {
        url.append("2G");
        if (isWapNetwork) {
            url.append("&wap");
        }

    }

    public static boolean isNetworkAvailable(Context context) {
        boolean flag = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            flag = true;
        } else {
            flag = false;
        }

        LogUtils.log(TAG, "flag = " + flag);
        return flag;
    }

    public static boolean isMobileNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            LogUtils.logd(TAG, "isMobileNetwork  getType: " + activeNetworkInfo.getType()
                    + " activeNetworkInfo.getState(): " + activeNetworkInfo.getState());
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                    && activeNetworkInfo.getState() == android.net.NetworkInfo.State.CONNECTED) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWIFIConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            int netWorkType = activeNetworkInfo.getType();
            if ((ConnectivityManager.TYPE_WIFI == netWorkType || ConnectivityManager.TYPE_WIMAX == netWorkType)) {
                return true;
            }
        }

        return false;
    }

    private static int getNetworkTypeGemini(Context context) {
        if (Config.IS_MTK_PLATFORM) {
            return getNetworkTypeGeminiForMTK(context);
        } else {
            return getNetworkTypeGeminiForQcom(context);
        }
    }

    private static int getNetworkTypeGeminiForQcom(Context context) {
        int type = 0;
        try {
            Class<?> mSimTelephonyManagerClass = Class.forName("android.telephony.MSimTelephonyManager");
            Method getNetworkTypeMethod = mSimTelephonyManagerClass.getMethod("getNetworkType", int.class);
            Method getPreferredDataSubscriptionMethod = mSimTelephonyManagerClass
                    .getMethod("getPreferredDataSubscription");
            Object mSimTelephonyManager = mSimTelephonyManagerClass.getConstructor(Context.class)
                    .newInstance(context);
            int simId = (Integer) getPreferredDataSubscriptionMethod.invoke(mSimTelephonyManager);

            type = (Integer) getNetworkTypeMethod.invoke(mSimTelephonyManager, simId);

            LogUtils.logd(TAG, "getNetworkTypeGeminiForQcom = " + type);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return type;
    }

    private static int getNetworkTypeGeminiForMTK(Context context) {

        int type = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        int simMode = GnSettings.System.getInt(context.getContentResolver(),
                GnSettings.System.DUAL_SIM_MODE_SETTING, 5/*GnSettings.System.DUAL_SIM_MODE_SETTING_DEFAULT*/);
        int simId = GnSettings.System.getInt(context.getContentResolver(),
                GnSettings.System.GPRS_CONNECTION_SIM_SETTING, GnPhone.GEMINI_SIM_1);
        int soltId = SIMInfo.getSlotById(context, simId);
        int id = GnPhone.GEMINI_SIM_1;

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (simMode == 2
                    || (simMode == 5/*GnSettings.System.DUAL_SIM_MODE_SETTING_DEFAULT*/ && simId == GnPhone.GEMINI_SIM_1)) {
                type = 2/*telephonyManager.getNetworkTypeGemini(GnPhone.GEMINI_SIM_2)*/;
                id = GnPhone.GEMINI_SIM_2;
            } else {
                type = 1/*telephonyManager.getNetworkTypeGemini(GnPhone.GEMINI_SIM_1)*/;
            }
        }

        LogUtils.logd(TAG, "getNetworkTypeGeminiForMTK ( " + id + ")= " + type);
        return type;
    }

    private static int getNetworkTypeClass(int networkType) {
        return TelephonyManager.getNetworkClass(networkType);
    }

    private static int getNetworkSingleSim(Context context) {
        int type = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            type = telephonyManager.getNetworkType();
        }

        LogUtils.logd(TAG, "getNetworkSingleSim  type =  " + type);
        return type;
    }

    private static ConnectionType getMobileConnectionType(Context context) {

        ConnectionType type = ConnectionType.CONNECTION_TYPE_IDLE;
        int specificType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        if (Config.GEMINI_SUPPORT) {
            specificType = getNetworkTypeGemini(context);
        } else {
            specificType = getNetworkSingleSim(context);
        }

        switch (TelephonyManager.getNetworkClass(specificType)) {
            case TelephonyManager.NETWORK_CLASS_2_G:
                type = ConnectionType.CONNECTION_TYPE_2G;
                break;
            case TelephonyManager.NETWORK_CLASS_3_G:
                type = ConnectionType.CONNECTION_TYPE_3G;
                break;
            case TelephonyManager.NETWORK_CLASS_4_G:
                type = ConnectionType.CONNECTION_TYPE_4G;
                break;
            default:
                break;
        }

        return type;
    }

    public static ConnectionType getConnectionType(Context context) {
        ConnectionType type = ConnectionType.CONNECTION_TYPE_IDLE;

        if (isNetworkAvailable(context)) {
            if (isWIFIConnection(context)) {
                type = ConnectionType.CONNECTION_TYPE_WIFI;
            } else {
                type = getMobileConnectionType(context);
            }
        }

        return type;
    }

    public static boolean isWapConnection(Context context) {
        ConnectivityManager lcm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State mobile = lcm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (mobile == NetworkInfo.State.CONNECTED) {

            NetworkInfo info = lcm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            String currentAPN = info.getExtraInfo();
            if (currentAPN != null && currentAPN.contains("wap")) {
                return true;
            } else {
                return false;
            }

        }
        return false;
    }
}
