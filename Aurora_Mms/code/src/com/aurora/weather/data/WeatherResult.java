package com.aurora.weather.data;
// Aurora xuyong 2015-04-23 created for aurora's new feature
public class WeatherResult {
    
    // sunny 
    public static final int AURORA_SUNNY_INDEX = 0;
    // cloudy
    public static final int AURORA_CLOUDY_INDEX = 1;
    // rainy
    public static final int AURORA_RAINY_INDEX = 2;
    // snow
    public static final int AURORA_SNOW_INDEX = 3;
    // sand storm
    public static final int AURORA_SAND_STORM_INDEX = 4;
    
    String mWeatherName;
    int mWeatherIndex;
    
    public WeatherResult() {
        mWeatherName = null;
        mWeatherIndex = -1;
    }
    
    public WeatherResult(String name, int index) {
        mWeatherName = name;
        mWeatherIndex = index;
    }
    
    public void setName(String name) {
        mWeatherName = name;
    }
    
    public void setIndex(int index) {
        mWeatherIndex = index;
    }
    
    
    public String getName() {
        return mWeatherName;
    }
    
    public int getIndex() {
        return mWeatherIndex;
    }
    
}
