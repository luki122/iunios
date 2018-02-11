
package com.aurora.ota.location;

//import android.content.Context;
//import android.content.Entity;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.TextUtils;
//import android.util.Log;
//import android.webkit.GeolocationPermissions;
//
//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationListener;
//import com.amap.api.location.LocationManagerProxy;
//import com.amap.api.location.LocationProviderProxy;
//import com.aurora.ota.reporter.Constants;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.List;
//import java.util.Locale;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class LocationHandler /*implements AMapLocationListener*/ {

//    private static final int MSG_STOP_LOCATION = 0x001;
//    
//    private static final String IP_138 = "http://iframe.ip138.com/ic.asp";
//    private static final String PC_ONLINE = "http://whois.pconline.com.cn/ipJson.jsp";
//    
//    private static final String ADDRESS_URL="http://maps.google.com/maps/api/geocode/json?";
//    private static final String ADDRESS_PARAM="latlng=";
//    private static final String ADDRESS_PARAMS2 = "&language=zh-CN&sensor=true";
//
//    private LocationManagerProxy aMapLocManager = null;
//    private String myLocation = "";
//    private AMapLocation aMapLocation;// 用于判断定位超时
//
//    private Context mContext;
//
//    private LocationInfo mLocationInfo;
//
//    private Callback mCallback;
//    
//    private AMapLocation mLocation;
//
//    public interface Callback {
//
//        public void callback(LocationInfo info);
//
//        public void ipCallback(String ip);
//        
//        public void countryCallback(String country);
//
//    }
//
//    public LocationHandler(Context context, LocationManagerProxy mManager) {
//        this.mContext = context;
//        mLocationInfo = new LocationInfo();
//        this.aMapLocManager = mManager;
//    }
//
//    public void setCallback(Callback callback) {
//        this.mCallback = callback;
//    }
//
//    public void start() {
//        if (aMapLocManager == null) {
//            aMapLocManager = LocationManagerProxy.getInstance(mContext);
//        }
//        aMapLocManager.setGpsEnable(false);
//        aMapLocManager.requestLocationUpdates(
//                LocationProviderProxy.AMapNetwork, 2000, 10, this);
//        Log.e("loo", "start");
//    }
//
//    public void stopLocation() {
//        if (aMapLocManager != null) {
//            aMapLocManager.removeUpdates(LocationHandler.this);
//            aMapLocManager.destory();
//        }
//        aMapLocManager = null;
//    }
//
//    public LocationInfo getLocation() {
//
//        return mLocationInfo;
//    }
//
//    public String getTestLocation() {
//        return myLocation;
//    }
//
//    @Override
//    public void onLocationChanged(Location arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onProviderDisabled(String arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onProviderEnabled(String arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle bundle) {
//        // TODO Auto-generated method stub
//        Log.e("loo", status + "");
//    }
//
//    @Override
//    public void onLocationChanged(AMapLocation location) {
//        // TODO Auto-generated method stub
//        List<Address> address = null;
//        if (location != null) {
//            this.aMapLocation = location;// 判断超时机制
//            Double geoLat = location.getLatitude();
//            Double geoLng = location.getLongitude();
//            String cityCode = "";
//            String desc = "";
//            Bundle locBundle = location.getExtras();
//            if (locBundle != null) {
//                cityCode = locBundle.getString("citycode");
//                desc = locBundle.getString("desc");
//            }
//            // String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
//            // + "\n精    度    :" + location.getAccuracy() + "米"
//            // + "\n定位方式:" + location.getProvider() + "\n定位时间:"
//            // + cityCode + "\n位置描述:" + desc + "\n省:"
//            // + location.getProvince() + "\n市:" + location.getCity()
//            // + "\n区(县):" + location.getDistrict() + "\n区域编码:" + location
//            // .getAdCode());
//            // Log.e("loo", str);
//            // synchronized (myLocation) {
//            // myLocation = str;
//            // }
//            this.mLocation = location;
//            new IPAddressTask().execute(ADDRESS_URL+ADDRESS_PARAM+geoLat+","+geoLng+ADDRESS_PARAMS2);
//           
//
//            new IPAddressTask().execute(PC_ONLINE);
////            stopLocation();
//        } else {
//            Constants.NEED_LOCATION = true;
//            new IPAddressTask().execute(IP_138);
//        }
//    }
//    
//    private void updateLocation(String country){
//        if(!TextUtils.isEmpty(country)){
//            if(mLocationInfo != null){
//                mLocationInfo.setCountry(country);
//                Log.e("add", ""+mLocationInfo.toString());
//            }
//           
//        }
//    }
//
//    private String IPParser(String html) {
//        if (TextUtils.isEmpty(html)) {
//            return "";
//        }
//        Document doc = Jsoup.parse(html);
//        Element body = null;
//        String ip = null;
//        if (doc != null) {
//            body = doc.body();
//            Log.e("html", body.text());
//            if (body != null) {
//                ip = body.toString();
//            }
//        }
//        if (ip != null) {
//            ip.replace("[", "");
//            ip.replace("]", "");
//            return ip;
//        } else {
//            return "";
//        }
//
//    }
//
//    private String IPParser2(String html)  {
//        String ip = "";
//        if(TextUtils.isEmpty(html)){
//            return "";
//        }
//        String[] child = html.split("k\\(");
//        if(child != null){
//            if(child.length > 1){
//                String data = child[1];
//                if(!TextUtils.isEmpty(data)){
//                    String needData = data.substring(0, data.length()-3);
//                    JSONObject json;
//                    try {
//                        json = new JSONObject(needData);
//                        ip = json.getString("ip");
//                    } catch (JSONException e) {
//                        // TODO Auto-generated catch block
//                        ip = "";
//                    }
//                   
//                }
//            }
//        }
//        return ip;
//    }
//
//    class IPAddressTask extends AsyncTask<String, Integer, String> {
//        private IPAddressUtils ipUtil;// new IPAddressUtils(IP_138).start();
//        private String charset = "gb2312";
//
//        @Override
//        protected String doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            StringBuilder resultStr = new StringBuilder();
//            ipUtil = new IPAddressUtils(params[0]);
//            try {
//                InputStream input = ipUtil.start();
//                if(!IP_138.equals(ipUtil.getUrl()) && !PC_ONLINE.equals(ipUtil.getUrl())){
//                    charset = "utf-8";
//                }
//                String str = null;
//                int index = 0;
//                if (input != null) {
//                    InputStreamReader inputReader = new InputStreamReader(input, charset);
//                    BufferedReader reader = new BufferedReader(inputReader);
//
//                    while ((str = reader.readLine()) != null) {
//                        resultStr.append(str);
//
//                    }
//
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                //e.printStackTrace();
//                Log.i("ip", "get ip failed");
//            }
//            String result = "";
//            if (IP_138.equals(ipUtil.getUrl())) {
//                result = IPParser(resultStr.toString());
//            } else if (PC_ONLINE.equals(ipUtil.getUrl())) {
//                result = IPParser2(resultStr.toString());
//            }else{
//                result = new AddressParser().parser(resultStr.toString());
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            // TODO Auto-generated method stub
//            if (IP_138.equals(ipUtil.getUrl()) ||PC_ONLINE.equals(ipUtil.getUrl())){
//                if (mCallback != null) {
//                    mCallback.ipCallback(result);
//                }
//            }else{
//                synchronized (mLocationInfo) {
//                    
//                    mLocationInfo.setCountry(result);
//                    mLocationInfo.setProvince(mLocation.getProvince());
//                    mLocationInfo.setCity(mLocation.getCity());
//                    Log.e("add", ""+mLocationInfo.toString());
//                    if (mCallback != null) {
//                        mCallback.callback(mLocationInfo);
//                    }
////                    Constants.NEED_LOCATION = false;
//                   
//                }
//                
//            }
//            super.onPostExecute(result);
//        }
//
//    }
//    
//    class AddressParser{
//        String parser(String json){
//            String country = "";
//            try {
//                
//                JSONObject jsonObj = new JSONObject(json);
//                if("OK".equals(jsonObj.getString("status"))){
//                    JSONArray array = jsonObj.getJSONArray("results");
//                    if(array != null && array.length() > 0){
//                            JSONArray child = array.getJSONObject(0).getJSONArray("address_components");
//                            if(child != null){
//                                child.getJSONObject(child.length()-1);
//                                
//                                country = child.getJSONObject(child.length()-2).getString("long_name");
//                            }
//                    }
//                }
//               
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                country = "";
//            }
//            return country;
//        }
//        
//    }
//
//    class IPAddressUtils {
//        private int OK = 200;
//        private HttpClient mClient;
//        private HttpResponse mResponse;
//        private String url;
//
//        public IPAddressUtils(String url) {
//            this.url = url;
//            // TODO Auto-generated constructor stub
//        }
//
//        public String getUrl() {
//            return url;
//        }
//
//        public InputStream start() {
//            InputStream input = null;
//            HttpGet request = new HttpGet(url);
//            mClient = new DefaultHttpClient();
//
//            try {
//                mResponse = mClient.execute(request);
//                int statuCode = mResponse.getStatusLine().getStatusCode();
//                Log.e("ip", "Code:" + statuCode);
//                if (statuCode == OK) {
//                    HttpEntity entity = mResponse.getEntity();
//                    input = entity.getContent();
//
//                }
//            } catch (ClientProtocolException e) {
//                // TODO Auto-generated catch block
//                // e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                // e.printStackTrace();
//            }
//
//            return input;
//
//        }
//
//    }

}
