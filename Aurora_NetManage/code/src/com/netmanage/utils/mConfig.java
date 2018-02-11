package com.netmanage.utils;

public class mConfig {
   public static final boolean isNative = false;
   
   public static final String TRAFFIC_VALUE_KEY = "trafficValue";
   public static final String ALL_APK_TOTAL_TRAFFIC_KEY = "allApkTotalTrafficKey";
   public static final String ALL_APK_MOBILE_TRAFFIC_KEY = "allApkMobileTrafficKey";
   
   public static final String CONFIG_KEY = "configKey";
   public static final String INIT_AUTO_START = "initAutoStart";//根据自启动白名单进行相应的配置
   public static final String INIT_SQLITE_KEY = "initSqliteKey";
   
   public static final String LBE_ID = "Gionee";
   public static final String LBE_SERVICE_ID = "com.lbe.security.iunios";  
   public static final String cache_file_name_of_RecomPerms = "recomPermsFile";  
   public static final String FILE_CONFIG = "config.json";
   public static final String FILE_ERROR_FLOW= "errorFlowFile";
   
   /**
    * 当sd卡插拔时，是否需要刷新应用列表
    */
   public static final boolean isUpdateAppListWhenSDPlugExtract = true;
   

   public static final String EarlyWarning_NOTIFY_KEY = "EarlyWarningNotifyKey";//预警提示
   public static final String EveryDay_NOTIFY_KEY = "EveryDayNotifyKey";//每天提示
   public static final String BackgroundFlow_NOTIFY_KEY = "BackgroundFlowNotifyKey";//后台流量提示
   public static final String Excess_NOTIFY_KEY = "ExcessNotifyKey";//超额提示
   public static final String SIM_CHANGE_KEY = "simChangeNotifyKey";//
   
   public static final String ALREADY_NOTIFY_TIMES_KEY = "notifyTimesKey";//已经提醒用户的次数
   public static final String LAST_NOTIFY_YEAR_KEY = "lastNotifyYearKey";//上次提醒用户年份
   public static final String LAST_NOTIFY_MONTH_KEY = "lastNotifyMonthKey";//上次提醒用户
   public static final String LAST_NOTIFY_DAY_KEY = "lastNotifyDayKey";//上次提醒用户
   
   public static final String SIM_INFO_KEY = "simInfoKey";//sim卡信息 
   public static final String IMSI_KEY = "imsiKey";//imsi信息 
   public static final String LAST_IMSI_KEY = "lastimsiKey";//最后imsi信息 
   
   public static final String CORRECT_FLOW_BY_SMS_INFO_KEY = "CorrectFlowBySmsInfo";
   public static final String province_code_KEY = "province_code";
   public static final String city_code_KEY = "city_code";
   public static final String carry_code_KEY = "carry_code";
   public static final String brand_code_KEY = "brand_code";
   public static final String province_name_KEY = "province_name";
   public static final String city_name_KEY = "city_name";
   public static final String carry_name_KEY = "carry_name";
   public static final String brand_name_KEY = "brand_name";
   public static final String isAutoCorrect_INFO_KEY = "isAutoCorrectInfo";
   
   public static final String PERMISSION_STATE_CHANGE_ACTION = "com.receive.PermissionStateChangeReceiver";
   
   public static final String LAUNCHER_NET_ICON_UPDATE_ACTION="com.receive.launcherNetIconUpdate";
}
