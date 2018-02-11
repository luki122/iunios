package com.aurora.weather.data;

import android.util.Log;

public class WeatherInfo {
    
    public static final String RECORD_DIVIDER = "-";
    // Aurora xuyong 2015-11-26 added for bug weatherinfo start
    private static final String RECORD_REPLACEMENT = String.valueOf('\1');
    // Aurora xuyong 2015-11-26 added for bug weatherinfo end
    //当前温度
    public static final String CUR_TEMP_TAG = "mCurTemp";
    //天气类型
    public static final String WEATHER_TYPE_TAG = "mWeatherType";
    //最低温度
    public static final String LOW_TEMP_TAG = "mLowTemp";
    //最高温度
    public static final String HIGH_TEMP_TAG = "mHighTemp";
    //风向
    public static final String WIND_DIR_TAG = "mWindDirection";
    //风力级别
    public static final String WIND_LEVEL_TAG = "mWindLevel";
    //湿度
    public static final String HUMI_TAG = "mHumidity";
    
    private String mCurTemp;
    private String mWeatherType;
    private String mLowTemp;
    private String mHighTemp;
    private String mWindDirection;
    private String mWindLevel;
    private String mHumidity;
    private String mCity;
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    private int mWeatherIndex = -1;
    private String mWeatherName;
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    // Aurora xuyong 2015-11-26 added for bug weatherinfo start
    private String mDefaultTemp = "0";
    // Aurora xuyong 2015-11-26 added for bug weatherinfo end
    public WeatherInfo () {
        
    }
    
    public void setCurTemp(String curTemp) {
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo start
        if (null != curTemp) {
            mCurTemp = curTemp.replaceAll(RECORD_REPLACEMENT, RECORD_DIVIDER);
        } else {
            mCurTemp = mDefaultTemp;
        }
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo end
    }
    
    public void setWeatherType(String weatherType) {
        mWeatherType = weatherType;
    }
    
    public void setLowTemp(String lowTemp) {
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo start
        if (null != lowTemp) {
            mLowTemp = lowTemp.replaceAll(RECORD_REPLACEMENT, RECORD_DIVIDER);
        } else {
            mLowTemp = mDefaultTemp;
        }
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo end
    }
    
    public void setHighTemp(String highTemp) {
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo start
        if (null != highTemp) {
            mHighTemp = highTemp.replaceAll(RECORD_REPLACEMENT, RECORD_DIVIDER);
        } else {
            mHighTemp = mDefaultTemp;
        }
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo end
    }
    
    public void setWindDirection(String windDirection) {
        mWindDirection = windDirection;
    }
    public void setWindLevel(String windLevel) {
        mWindLevel = windLevel;
    }
    
    public void setHumidity(String humidity) {
        mHumidity = humidity;
    }
    
    public void setCity(String city) {
        mCity = city;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    public void setWeatherIndex(String index) {
        try {
            mWeatherIndex = Integer.parseInt(index);
        } catch (NumberFormatException e) {
            mWeatherIndex = 0;
            e.printStackTrace();
        }
    }
    
    public void setWeatherName(String name) {
        mWeatherName = name;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    public String getCurTemp() {
        return mCurTemp;
    }
    
    public String getWeatherType() {
        return mWeatherType;
    }
    
    public String getLowTemp() {
        return mLowTemp;
    }
    
    public String getHighTemp() {
        return mHighTemp;
    }
    
    public String getWindDirection() {
        return mWindDirection;
    }
    
    public String getWindLevel() {
        return mWindLevel;
    }
    
    public String getHumidity() {
        return mHumidity;
    }
    
    public String getCity() {
        return mCity;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    public int getWeatherIndex() {
        return mWeatherIndex;
    }
    
    public String getWeatherName() {
        return mWeatherName;
    }
    // Aurora xuyong 2015-05-05 added for bug #13383 start
    public static boolean hasValidWeatherInfo(final String mMessageItem) {
    	if (mMessageItem.startsWith("null")) {
    		return false;
    	}
    	String[] arrays = mMessageItem.split(RECORD_DIVIDER);
    	if (arrays.length <= 8) {
    		return false;
    	}
    	return true;
    }
    // Aurora xuyong 2015-05-05 added for bug #13383 end
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    public static WeatherInfo initFromRecord(String string) {
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo start
        String[] arrays = convert(string).split(RECORD_DIVIDER);
        // Aurora xuyong 2015-11-26 modified for bug weatherinfo end
        WeatherInfo info = new WeatherInfo();
        info.setCity(arrays[0]);
        info.setCurTemp(arrays[1]);
        info.setWeatherType(arrays[2]);
        info.setLowTemp(arrays[3]);
        info.setHighTemp(arrays[4]);
        info.setWindDirection(arrays[5]);
        info.setWindLevel(arrays[6]);
        info.setHumidity(arrays[7]);
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        if (arrays.length > 8) {
            info.setWeatherIndex(arrays[8]);
            info.setWeatherName(arrays[9]);
        }
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
        return info;
    }
    // Aurora xuyong 2015-11-26 added for bug weatherinfo start
    private static String convert(String weatherInfo) {
        return weatherInfo.replace(RECORD_DIVIDER + RECORD_DIVIDER, RECORD_DIVIDER + RECORD_REPLACEMENT);
    }
    // Aurora xuyong 2015-11-26 added for bug weatherinfo start
}
