package com.aurora.change.utils;

import android.os.Build;

import com.aurora.thememanager.R;

public class Consts {
    // 判断是否第一次进入应用
    public static final String IS_FIRST_START = "isFirstStart";
    public static final String DEFAULT_LOCKSCREEN_FILES = "default_lockscreen_files";
    // 当前启用的是哪一组壁纸
    public static final String CURRENT_LOCKPAPER_GROUP = "current_lockpaper_group";
    // 传递到壁纸预览页面的key
    public static final String WALLPAPER_PREVIEW_KEY = "wallpaper_preview_key";
    // 传递到壁纸裁剪页面的key
    public static final String WALLPAPER_CROP_KEY = "wallpaper_crop_key";
    // 传递到处理页面的壁纸类型，例如：桌面壁纸，锁屏壁纸
    public static final String WALLPAPER_TYPE_KEY = "wallpaper_type";
    public static final String WALLPAPER_LOCKSCREEN_TYPE = "lockscreen";
    public static final String WALLPAPER_DESKTOP_TYPE = "wallpaper";
    public static final String DEFAULT_NORMAL_LOCKPAPER_GROUP = "Dream";
    public static final String DEFAULT_FORHER_LOCKPAPER_GROUP = "MissPuff";
    public static final String DEFAULT_LOCKPAPER_GROUP = Build.MODEL.equals("IUNI i1") ? DEFAULT_FORHER_LOCKPAPER_GROUP : DEFAULT_NORMAL_LOCKPAPER_GROUP;
    public static final String BLACKSTAYLE_LOCKPAPER_GROUP_1 = "Fascinating";
    public static final String BLACKSTAYLE_LOCKPAPER_GROUP_2 = "MissPuff";
    public static final String WALLPAPER_GPOSITION_KEY = "group_position";
    // 锁屏壁纸存放的地址
    public static final String LOCKSCREEN_WALLPAPER_PATH = "/data/aurora/change/lockscreen/wallpaper.png";
    public static final String DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_PATH = "/system/iuni/aurora/change/lockscreen/";
    public static final String DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH = "/system/iuni/aurora/change/desktop/";
    public static final String DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH = "/mnt/sdcard/aurora/change/lockscreen/";
    public static final String DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_FLAG = "/system/iuni/";
    public static final String DEFAULT_LOCKPAPER_FILE_NAME = "Custom series";

    public static final String LOCKSCREEN_WALLPAPER_GROUP_NAME_KEY = "group_name";
    public static final String LOCKSCREEN_WALLPAPER_CROP_TYPE = "crop_type";
    public static final String LOCKSCREEN_WALLPAPER_CROP_SOURCE = "crop_from_source";

    public static final String FILEMANAGER_WALLPAPER = "com.aurora.pic.wallpaper.manager.action";
    
    public static final String ACTION_COPY_FILE = "com.aurora.change.COPY";
    
    public static final String IMAGE_LOCKSCREEN_CACHE_DIR = "thumbs";
    
    public static final String IMAGE_DESKTOP_CACHE_DIR = "desktop_thumbs";
    
    public static final int DEFAULT_LOCKSCREEN_NUMS = 5;
    
    /*public static final int [] LOCAL_WALLPAPERS = { R.drawable.wallpaper_01, R.drawable.wallpaper_02, R.drawable.wallpaper_03, 
		R.drawable.wallpaper_04, R.drawable.wallpaper_05, R.drawable.wallpaper_06, 
		R.drawable.wallpaper_07, R.drawable.wallpaper_08, R.drawable.wallpaper_09 };*/
    
	public static final String[] LOCAL_WALLPAPERS = { "wallpaper_01.jpg",
			"wallpaper_02.jpg", "wallpaper_03.jpg", "wallpaper_04.jpg",
			"wallpaper_05.jpg", "wallpaper_06.jpg", "wallpaper_07.jpg",
			"wallpaper_08.jpg", "wallpaper_09.jpg" };
    
    public static int isChangedByLocal = 0;
    public static boolean isWallPaperChanged = false;
    
    //shigq add start
    public static final int LOCKPAPER_SWITHING = 2;
    public static final int LOCKPAPER_INFO_LOAD_DONE = 3;
    public static final int LOCKPAPER_NEXTDAY_UPDATE = 4;
    public static final int LOCKPAPER_NEXTDAY_SHOW_TIPS = 5;
    public static final int LOCKPAPER_NEXTDAY_IS_SAVED_TIPS = 6;
    public static final int LOCKPAPER_NEXTDAY_SET_WALLPAPER = 7;
    public static final int LOCKPAPER_NEXTDAY_LOAD_COMPLETE = 8;
    
    // wallpaper版本
    public static final String WALLPAPER_VERSION = "wallpaper_version";
    // default theme
    public static final String DEFAULT_THEME = "default_theme";
    // theme name
  	public static final String THEME_NAME = "theme_name";
  	// color of theme name
  	public static final String THEME_NAME_COLOR = "theme_name_color";
  	// color of time text
  	public static final String TIME_COLOR_BLACK = "time_color_black";
  	// color of status bar
  	public static final String STATUS_BAR_COLOR = "status_bar_color";
  	// time of current group is black
  	public static final String CURRENT_LOCKPAPER_GROUP_TIME_BLACK = "current_lockpaper_group_time_black";
  	// time of current group is black
   	public static final String CURRENT_LOCKPAPER_GROUP_STATUS_BLACK = "current_lockpaper_group_status_black";
    // wallpaper_set.xml文件名
    public static final String WALLPAPER_SET_FILE = "wallpaper_set.xml";
    // lockpaper_set.xml文件名
    public static final String LOCKPAPER_SET_FILE = "lockpaper_set.xml";
    
    // next day server url for init test
    public static final String NEXTDAY_URL_INIT_TEST = "http://adstest.virtual.iunios.com/app/init";
    // next day server url for init
    public static final String NEXTDAY_URL_INIT = "http://i.iunios.com/app/init";
    // next day server url for test
    public static final String NEXTDAY_URL_GETDATA_TEST = "http://adstest.virtual.iunios.com/lockscreen/getdata";
    // next day server url
	public static final String NEXTDAY_URL_GETDATA = "http://i.iunios.com/lockscreen/getdata";
    // app id
    public static final String NEXTDAY_APP_ID = "iunios_lockscreen";
    // app key
    public static final String NEXTDAY_APP_KEY = "nmee1190qdpbhpl6a8l3be88uh";
    // string data for app initiate
    public static final String NEXTDAY_INITDATA_KEY = "imei";
    // string data for app initiate
    public static final String NEXTDAY_INITDATA_VALUE = "008600215140400";
    // path of nextday wallpaper
    public static final String NEXTDAY_WALLPAPER_PATH = "/mnt/sdcard/iuni/.lockscreen/";
    // temp file path for nextday wallpaper
    public static final String NEXTDAY_WALLPAPER_TEMP = "/mnt/sdcard/IUNI_Wallpaper/NextDayTemp/";
    // path for wallpaper saved by nextday
    public static final String NEXTDAY_WALLPAPER_SAVED = "/mnt/sdcard/IUNI_Wallpaper/";
    // the count of picture for nextday
    public static final int NEXTDAY_PICTURE_SIZE = 60;
    // the picture date for nextday
    public static final String NEXTDAY_PICTURE_DATE = "picture_date";
    // the wifi setting for nextday
    public static final String NEXTDAY_WIFI_NETWORK_SETTINGS = "only_wifi_enable";
    // the loading tips for nextday
    public static final String NEXTDAY_LOADING_ANYWAY = "loading_anyway";
    // the loading tips for nextday
    public static final String NEXTDAY_SHOW_COMMENTS = "show_comments";
    
    // nextday wallpaper operation type
    public static final String NEXTDAY_HTTP_TYPE_INIT = "init";
    public static final String NEXTDAY_HTTP_TYPE_PREVIEW = "preview";
    public static final String NEXTDAY_HTTP_TYPE_SHOW = "show";
    
    public static final String NEXTDAY_PICTURE_LOADTYPE_NONE = "none";
    public static final String NEXTDAY_PICTURE_LOADTYPE_INFO = "info";
    public static final String NEXTDAY_PICTURE_LOADTYPE_PICTURE = "picture";
    public static final String NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD = "download";
    
    public static final String NEXTDAY_OPERATION_WAKEUP = "wakeup";
    public static final String NEXTDAY_OPERATION_SAVE = "save";
    public static final String NEXTDAY_OPERATION_SET = "set";
    public static final String NEXTDAY_OPERATION_SHARE = "share";
    
    public static final String NEXTDAY_DB_FIRST_CREATE = "isFirstCreate";
    
    //shigq add end
    
}
