package com.aurora.weatherdata.util;

public class Globals {

	public static final boolean isTestData = false;

	public static final String HTTP_REQUEST_KAIFA_URL = "http://18.8.0.244/service";
	public static final String HTTP_REQUEST_TEST_URL = "http://test.appmarket.iunios.com/service";
	public static final String HTTP_REQUEST_DEFAULT_URL = "http://appmarket.iunios.com/service";
//	public static String HTTP_REQUEST_URL = "http://18.8.0.244:90/getweatherbycityid?";
//	http://18.8.0.183:82/getweatherbycityid?cityid=101270603
	public static String HTTP_REQUEST_URL = "http://weather.iunios.com/getweatherbycityid?";

    public static final String DB_PATH = "/data/data/com.aurora.weatherforecast/databases";
    public static final String NATIVE_WEATHER_DB_NAME = "weather.db";

	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;

	public static final int NETWORK_ERROR = 500;
	public static final int NO_NETWORK = 501;
	public static final int HAS_NETWORK = 502;
	public static final int GET_LOCALCITY = 503;



}