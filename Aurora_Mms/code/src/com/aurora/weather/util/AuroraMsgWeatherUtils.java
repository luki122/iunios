package com.aurora.weather.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;

import com.android.mms.MmsApp;
import com.android.mms.transaction.SmsReceiverService;
import com.android.mms.transaction.SmsReceiverService.NameAndBody;
import com.aurora.mms.util.Utils;
import com.aurora.weather.data.JSONParser;
import com.aurora.weather.data.WeatherInfo;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.aurora.weather.data.WeatherResult;
// Aurora xuyong 2015-04-23 added for aurora's new feature end
import com.aurora.weather.databases.CTWOpenHelper;
import com.aurora.weather.databases.CWOpenHelper;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.android.mms.R;
// Aurora xuyong 2015-04-23 added for aurora's new feature end
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.format.DateFormat;
import android.util.Log;

public class AuroraMsgWeatherUtils {
    
    public static final String TAG = "Mms/Weather";
    
    public static final boolean TEST_FLAG = false;
    public static final boolean sHasWeatherFeature = true;
    
    public static final String APP_PACKAGE_NAME = "com.android.mms";
    public static final String WEATHER_DB_NAME = "weather.db";
    public static final String NATIVE_WEATHER_DB_NAME = "weather.db";
    public static final String MMS_WEATHER_DB_PATH = File.separator + "data" + File.separator + "data"
                                                         + File.separator + APP_PACKAGE_NAME + File.separator + "databases";
    
    private static final String HTTP_REQUEST_URL = TEST_FLAG ? "http://iunios.weather.cm.com/" : "http://weather.iunios.com/";

    private static final String HTTP_TODAY_WEATHERINFO_SUFFIX  = "getTodayWeatherByCityId?";
    private static final String HTTP_WEEK_WEATHERINFO_SUFFIX  = "getweatherbycityid?";
    
    public static boolean copyAssetsToFilesystem(Context context, String assetsSrc, String desPath) {
        final String des = desPath + File.separator + assetsSrc;

        InputStream istream = null;
        OutputStream ostream = null;
        try {
            AssetManager am = context.getAssets();
            istream = am.open(assetsSrc);
            ostream = new FileOutputStream(des);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = istream.read(buffer)) > 0) {
                ostream.write(buffer, 0, length);
            }
            istream.close();
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (istream != null)
                    istream.close();
                if (ostream != null)
                    ostream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            return false;
        }
        return true;
    }
    
    public static String getAreaName(Context context, String address) {
        String cityName = Utils.getAreaName(context, address, Utils.CITY_NAME_INDEX);
        Log.e("Mms/Weather", "first get cityName = " + cityName);
        if (cityName == null) {
            String location = Utils.getAreaName(context, address, Utils.RIGION_NAME_INDEX);
            Log.e("Mms/Weather", " get location = " + location);
            cityName = AuroraMsgWeatherUtils.getCityNameFromLocation(context, location);
            Log.e("Mms/Weather", "second get cityName = " + cityName);
        }
        return cityName;
    }
    
    public static String getWeatherInfoFromNet(Context context, String cityId) throws Exception{
        String weatherInfo = getWeatherInfoFromNewUrl(context, cityId);
        Log.e("Mms/Weather", "fisrt get weatherInfo = " + weatherInfo);
        if (weatherInfo == null) {
            weatherInfo = getWeatherInfoFromOldUrl(context, cityId);
            Log.e("Mms/Weather", "second get weatherInfo = " + weatherInfo);
            return weatherInfo;
        }
        return weatherInfo;
    }
    
    private static String getWeatherInfoFromOldUrl(Context context, String cityId) throws Exception {
        final String url = HTTP_REQUEST_URL + HTTP_WEEK_WEATHERINFO_SUFFIX + "cityid=" + cityId + "&day=6";
        Log.e(TAG, " post old url = " + url);
        return doRequest(url);
    }
    
    private static String getWeatherInfoFromNewUrl(Context context, String cityId) throws Exception {
        final String url = HTTP_REQUEST_URL + HTTP_TODAY_WEATHERINFO_SUFFIX + "cityid=" + cityId;
        Log.e(TAG, "post new url = " + url);
        return doRequest(url);
    }
    
    public static String doRequest(String request_url) throws IOException {

        // do the decode base64
        try{
            URL url = new URL(request_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.e(TAG, request_url);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(6000);
            conn.connect();
            int stutas = conn.getResponseCode();
            Log.e(TAG, "stutas " + stutas);
            if (stutas == 200) {
                Log.e(TAG, "http success");
                InputStream inStream = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

                StringBuilder builder = new StringBuilder();
                String content = "";
                while ((content = in.readLine()) != null) {
                    builder.append(content);
                }
                in.close();

                return builder.toString();
            } else {
                return null;
            }
        } catch (HttpHostConnectException e) {
            e.printStackTrace();
            throw e;
        } catch (ConnectException e) {
            e.printStackTrace();
            throw e;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public static String getCityIdByCityName(Context context, String cityName) {
        CWOpenHelper instance = new CWOpenHelper(context);
        return instance.getCityIdByName(cityName);
    }
    
    public static String getTimeSection() {
        // Aurora xuyong 2015-04-25 modified for aurora's new feature start
        String timeSection = DateFormat.format("yyyyMMddHH", System.currentTimeMillis()).toString();
        // Aurora xuyong 2015-04-25 modified for aurora's new feature end
        Log.e(TAG, "time format = " + timeSection);
        return timeSection;
    }
    
    public static String getWeatherInfoFromCache(Context context, String cityName, String timeSection) {
        Log.e(TAG, "cityName = " + cityName + ", timeSection = " + timeSection);
        CTWOpenHelper instance = new CTWOpenHelper(context);
        // Aurora xuyong 2015-04-23 modified for aurora's new feature start
        if (null != instance) {
            return instance.getWeatherInfoCache(cityName, timeSection);
        }
        return null;
        // Aurora xuyong 2015-04-23 modified for aurora's new feature end
    }
    
    public static void insertNewInfo(Context context, String cityName, String timeSection, String weatherInfo) {
        Log.e(TAG, "cityName = " + cityName + ", timeSection = " + timeSection + ", weatherInfo = " + weatherInfo);
        CTWOpenHelper instance = new CTWOpenHelper(context);
        // Aurora xuyong 2015-04-23 modified for aurora's new feature start
        if (null != instance) {
            instance.insert(cityName, timeSection, weatherInfo);
        }
        // Aurora xuyong 2015-04-23 modified for aurora's new feature end
    }
    
    public static WeatherInfo extractUsefulInfo(String jsonString) {
        Log.e(TAG, "jsonString = " + jsonString);
        if (jsonString != null) {
            return JSONParser.parse(jsonString);
        } else {
            return null;
        }
    }
    
    public static String reformat(WeatherInfo info) {
        if (info != null) {
            return info.getCurTemp() + WeatherInfo.RECORD_DIVIDER +
                    info.getWeatherType() + WeatherInfo.RECORD_DIVIDER +
                    info.getLowTemp() + WeatherInfo.RECORD_DIVIDER +
                    info.getHighTemp() + WeatherInfo.RECORD_DIVIDER +
                    info.getWindDirection() + WeatherInfo.RECORD_DIVIDER +
                    info.getWindLevel() + WeatherInfo.RECORD_DIVIDER +
                    info.getHumidity();
        }
        return null;
    }
    
    public static boolean needShowWeatherInfo(Context context, NameAndBody instance) {
        return sHasWeatherFeature && !MmsApp.sNotCNFeature && (!Utils.isNotificationMsg(context, instance.getNumber(), instance.getBody()));
    }
    
    public static String getCityNameFromLocation(Context context, String location) {
        CWOpenHelper instance = new CWOpenHelper(context);
        // Aurora xuyong 2015-04-23 modified for aurora's new feature start
        if (null != instance) {
            return instance.getCityNameFromLocation(location);
        }
        return null;
        // Aurora xuyong 2015-04-23 modified for aurora's new feature end
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    public static WeatherResult getWeatherResult(Context context, WeatherInfo info) {
        CWOpenHelper instance = new CWOpenHelper(context);
        if (null != instance) {
            return instance.getWeatherResult(info);
        }
        return null;
    }
    
    public static int getResourceIdByIndex(int index) {
        int resourceId = -1;
        switch (index) {
            case WeatherResult.AURORA_SUNNY_INDEX:
                resourceId = R.drawable.aurora_sunny;
                break;
            case WeatherResult.AURORA_CLOUDY_INDEX:
                resourceId = R.drawable.aurora_cloudy;
                break;
            case WeatherResult.AURORA_RAINY_INDEX:
                resourceId = R.drawable.aurora_rainly;
                break;
            case WeatherResult.AURORA_SNOW_INDEX:
                resourceId = R.drawable.aurora_snow;
                break;
            case WeatherResult.AURORA_SAND_STORM_INDEX:
                resourceId = R.drawable.aurora_sand_storm;
                break;
        }
        return resourceId;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
}
