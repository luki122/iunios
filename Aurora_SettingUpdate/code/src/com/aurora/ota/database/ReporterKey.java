package com.aurora.ota.database;

public interface ReporterKey {
    public static final String KEY_APP_VERSION = "apkVersion";
    public static final String KEY_APP_NAME = "appName";
    public static final String KEY_IMEI = "imei" ;
    public static final String KEY_CHANEL = "chanel" ;
    
    public static final String KEY_MOBILE_MODEL = "mobileModel";
    public static final String KEY_MOBILE_NUMBER = "mobileNumber" ;
    public static final String KEY_REGISTER_USER_ID = "registerUserId" ;
    public static final String KEY_SHUT_DOWN_TIME = "shutDownTime" ;
    public static final String KEY_CREATE_ITEM_TIME = "startupTime" ;
    public static final String KEY_STATUS = "status"  ;
    public static final String KEY_REPORTED = "reported" ;
    
    public static final String KEY_PHONE_SIZE = "size";
    
    
    
    public static final String KEY_LOCATION="location";
    
    public static final String KEY_APP_NUM = "appNum";
    
    public static final String KEY_BOOT_TIME = "bootTime";
    public static final String KEY_DURATION_TIME = "duration";
    
    /**
     * 增加各个模块的统计
     */
    
	public static final String KEY_MODULE = "module_key";
   public static final String KEY_MODULE_ITEM = "item_tag";
    public static final String KEY_VALUE = "value";
    
}
