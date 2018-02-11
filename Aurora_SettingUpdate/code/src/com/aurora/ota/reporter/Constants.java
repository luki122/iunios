package com.aurora.ota.reporter;

public class Constants {

	private Constants(){};
	
	public static final String ACTION_CONNECTION_CHANGED="android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ACTION_SHUTDOWN="android.intent.action.ACTION_SHUTDOWN";
	public static final String ACTION_START_REPORT_SERVICE="com.iuni.REPORTSERVICE";
	public static final String ACTION_FROM_NET_CHANGE = "com.iuni.netchange";
	
	public static final String ACTION_UPDATE_LOCATION = "com.aurora.ota.report.ACTION_UPDATE_LOCATION";
	
    public static final String KEY_START_FROM_BOOT = "KEY_START_FROM_BOOT";
    public static final String NAME_FOR_STORR_REPORT_TIME = "report";
    public static final String NAME_FOR_SRORE_REPORT = "reports";
    public static final String KEY_SHUTDOWN_TIME = "shut_down";
    public static final String KEY_START_UP_TIME = "start_up";
    public static final String KEY_BOOT_NUMBER = "boot_number";
    public static final String KEY_NET_CHANGED = "net";
    public static final String KEY_DATE_CHANGED = "date_changed";
    public static final String KEY_VERSION = "iuni_version";
    public static final String KEY_REPORT_DATE = "report_date";
    public static final String KEY_TODAY_REPORTED = "today_reported";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_COUNTRY = "country";
    
    public static final String KEY_PROVINCE = "provice";
    
    public static final String KEY_CITY = "city";
    
    public static final String KEY_IP = "ip";
    
    public static final String KEY_LOCATION_TIME="location_time";
    
    public static final String KEY_PHONE_WIDTH = "phone_width";
    
    public static final String KEY_PHONE_HEIGHT = "phone_height";
    
    public static final String SPLITE = "-";
    
    
    public static final String OS_VERSION = "os_version";
    
    public static final int VALUE_NET_CHANGED = 100;
    public static final int VALUE_DATE_CHANGED = 101;
    public static final int START_FROM_BOOT = 0x102;
    public static final int MODE_PRIVATE = 0;
    
    
    public static boolean NEED_LOCATION = true;
}
