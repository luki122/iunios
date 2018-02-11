package com.aurora.weather.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {
    
    private static final String ARRAY_TAG = "mWeatherDataEveryDays";
    
    public static WeatherInfo parse(String jasonString) {
        WeatherInfo info = null;
        try {
            JSONObject object = new JSONObject(jasonString);
            JSONArray array = object.getJSONArray(ARRAY_TAG);
            info = new WeatherInfo();
            if (array != null) {
                Log.e("Mms/Weather", "array NOT null");
                JSONObject todayWeatherInfo = array.getJSONObject(0);
                info.setCurTemp(todayWeatherInfo.getString(WeatherInfo.CUR_TEMP_TAG));
                info.setWeatherType(todayWeatherInfo.getString(WeatherInfo.WEATHER_TYPE_TAG));
                info.setLowTemp(todayWeatherInfo.getString(WeatherInfo.LOW_TEMP_TAG));
                info.setHighTemp(todayWeatherInfo.getString(WeatherInfo.HIGH_TEMP_TAG));
                info.setWindDirection(todayWeatherInfo.getString(WeatherInfo.WIND_DIR_TAG));
                info.setWindLevel(todayWeatherInfo.getString(WeatherInfo.WIND_LEVEL_TAG));
                info.setHumidity(todayWeatherInfo.getString(WeatherInfo.HUMI_TAG));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return info;
        
    }
}
