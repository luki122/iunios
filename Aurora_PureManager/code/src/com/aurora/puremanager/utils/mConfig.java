package com.aurora.puremanager.utils;

public class mConfig {
   public static final boolean isNative = false;
   
   public static final String CONFIG_KEY = "configKey";
   public static final String SORT_BY_CACHE_KEY = "sortByCacheKey";//空间清理界面，是否按照缓存排序
   public static final String ALL_APP_SORT_KEY = "allAppSortKey";
   public static final String AD_LIB_VERSION = "adLibVersion";
   public static final String IS_ALREADY_MANUAL_SCAN_AD_APP = "isHaveScanAdApp";//是否已经手动扫描过广告应用
   public static final String IS_ALREADY_COMPLETE_MANUAL_SCAN_AD_APP = "isCompleteScanAdApp";//是否已经手动完整的扫描过广告应用
   public static final String IS_ALREADY_READ_ASSETS_ADLIB = "alreadyReadAssetsAdlib";//是否已经读取存放在本地的广告库
     
   public static final String LBE_ID = "Gionee";
   public static final String LBE_SERVICE_ID = "com.lbe.security.iunios";
   public static final String LBE_PERMISSION_MANAGE_PKG = "com.lbe.security.iunios";
   
   public static final String cache_file_name_of_RecomPerms = "recomPermsFile";
   public static final String cache_file_name_of_perRemind = "perRemindFile";
   public static final String cache_file_name_of_netHint = "netHintFile";
   public static final String cache_file_name_of_autoStart = "autoStartFile";
   
   /**
    * 存储空间不足提示用户的功能相关 数据记录字段
    */
   public static String STORAGE_NOTIFY_KEY = "storeNotifyKey";
   public static String IS_ALREADY_LOW_KEY = "isAlreadyLowKey";//空间是否已经不足
   public static String ALREADY_NOTIFY_TIMES_KEY = "notifyTimesKey";//已经提醒用户的次数
   public static String LAST_NOTIFY_TIME_KEY = "lastNotifyTimeKey";//上次提醒用户的时间
   
   /**
    * 自启动功能是否需要控制应用的广播接受
    */
   public static final boolean isAutoStartControlReceive = true;
   
   /**
    * 当sd卡插拔时，是否需要刷新应用列表
    */
   public static final boolean isUpdateAppListWhenSDPlugExtract = true;
   
   /**
    * 是否使用 系统应用不能停用名单
    */
   public static final boolean isUseSysAppCanNotDisableList = true;
   
   public static final String PERMISSION_STATE_CHANGE_ACTION = "com.receive.PermissionStateChangeReceiver";
   
   /**
    * 说明：在releaseObject()函数中，一般没有必要把一般的对象设置为null，
    * 因为一般的对象即使设为null，也释放不了多少空间，反而会增加出现空指针的概率。
    * 但是对于全局的context对象是一定要设置为null。经过验证，把context对象置null，可以释放百分之80的内存。
    */
   public static final boolean SET_NULL_OF_CONTEXT = true;
   
   public static final String PKGNAME_OF_Market = "com.aurora.market";
   public static final String ACTION_BAR_STYLE = "actionBarStyle";
   /**
    * 正常账户的身份id
    */
   public static final int NORMAL_ACCOUNTID = 0;
}
