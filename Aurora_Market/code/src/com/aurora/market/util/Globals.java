/*
 * @author 张伟
 */
package com.aurora.market.util;

import android.os.Environment;

import java.io.File;


public class Globals {


	public static final boolean isTestData = false;
	//图片类型
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";
	
	public static final String NEW_LINE = "\n\n"; // 换行符
	public static final String LINE = "\n"; // 换行符
	public final static int REQUEST_CODE_BASE = 1;
	public final static int REQUEST_CODE_CAPTURE = REQUEST_CODE_BASE + 0;
	public final static int REQUEST_CODE_ALBUM = REQUEST_CODE_BASE + 1;
	public final static int REQUEST_CODE_VIDEO = REQUEST_CODE_BASE + 2;
	public final static int REQUEST_GETVIDEO_VIDEO = REQUEST_CODE_BASE + 3;
	public final static int REQUEST_CODE_ADD_REMINDER = REQUEST_CODE_BASE + 4;
	
	public static final String SIGN_NOINDENT_ID = "1";
	public static final String SIGN_INDENT_ID = "2";
	
	public static final String HTTP_REQUEST_KAIFA_URL = "http://dev.appmarket.iunios.com/service"; 
	public static final String HTTP_REQUEST_TEST_URL = "http://test.appmarket.iunios.com/service"; 
	public static final String HTTP_REQUEST_DEFAULT_URL = "http://appmarket.iunios.com/service"; 
	public static  String HTTP_REQUEST_URL = "http://appmarket.iunios.com/service"; 
	
	
	//public static final String HTTP_REQUEST_URL = "http://18.8.0.39/service"; 
	public static final String HTTP_ACTION_PARAM = 
		    "&parmJson=";
	
	/*主界面列表 和 排行 等 */
	public static final String HTTP_SERVICE_NAME_APPLIST="?module=app";
	public static final String HTTP_APPLIST_METHOD="&action=list";
	public static final String HTTP_FEEDLIST_METHOD="&action=feedlist";
	public static final String HTTP_MAINLIST_METHOD="&action=mainlist";
	/*新品*/
	public static final String HTTP_APPNEW_METHOD="&action=fresh";
	/*应用排行*/
	public static final String HTTP_RANKLIST_METHOD="&action=rank";
	/*必备*/
	public static final String HTTP_STARTER_METHOD="&action=starter";
	/*设计*/
	public static final String HTTP_DESIGN_METHOD="&action=recommlist";
	/*应用分类 */
	public static final String HTTP_CATEGORYLIST_METHOD="&action=categorize";
	/*应用详情*/
	//public static final String HTTP_SERVICE_NAME_APPDETAILS="?module=app";
	public static final String HTTP_APPDETAILS_METHOD="&action=detail";
	
	
	
	/*应用分类信息接口*/
	public static final String HTTP_SERVICE_NAME_CATOGORY_LIST="?module=category";
	/*搜索推荐接口*/
	public static final String HTTP_SERVICE_NAME_SEARCHRECLIST="?module=search";
	public static final String HTTP_SEARCHRECLIST_METHOD="&action=recommend";	
	
	/*专题接口*/
	public static final String HTTP_SERVICE_NAME_SPECIAL_LIST="?module=special";
	public static final String HTTP_SPECIALLIST_METHOD="&action=applist";
	
	/*搜索应用接口*/
	public static final String HTTP_SEARCHAPPLIST_METHOD="&action=search";	
		
	/*搜索建议接口*/
	public static final String HTTP_SEARCHSUGGEST_METHOD="&action=suggest";		
	/*应用升级和更新检测接口*/
   public static final String HTTP_UPGRADEAPP_METHOD="&action=update";
 
	/*应用升级数量*/
	public static final String HTTP_UPAPPCOUNT_METHOD="&action=updateCount"; 
	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;
	
	// 相机拍照照片路径
	public static final File PHOTO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/photo");
	
	// 相机摄像照片路径
	public static final File VIDEO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/video");
	
	// 生成图片路径
	public static final File PIC_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/pic");
	// 生成录音路径
	public static final File SOUND_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/sound/");
	//activity result 100
	public static final int REQUEST_CODE_BASE_USER = 100;
	public static final int REQUEST_CODE_ADD_RECORD =  REQUEST_CODE_BASE_USER+1;
	public static final int REQUEST_CODE_OPEN_NEWNOTE =  REQUEST_CODE_BASE_USER+2;
	public static final int REQUEST_CODE_MODIFY_LABEL = REQUEST_CODE_BASE_USER + 3;
	
	// 应用类型
	public static final String TYPE_APP = "APP";
	// 游戏类型
	public static final String TYPE_GAME = "GAME";
	public static final int NETWORK_ERROR = 500;
	public static final int NO_NETWORK = 501;
	
	/*SharedPreferences*/
	public static final String SHARED_WIFI_UPDATE = "com.aurora.wifi.update";
	/*SharedPreferences*/
	public static final String SHARED_APP_UPDATE = "com.aurora.app.update";
	/*SharedPreferences*/
	public static final String SHARED_DATA_UPDATE = "com.aurora.data.update";
	// setting for first login
	public static final String SHARED_FIRST_LOGIN_CONFIG = "com.aurora.user.config";
	public static final String SHARED_FIRST_KEY = "com.aurora.isfirst";
	//AURORA UKILIU ADD 2014-10-20 BEGIN
	public static final String SHARED_FIRST_LAUNCH = "com.aurora.firstlaunch";
	public static final String SHARED_FIRST_LAUNCH_KEY = "first_launch";
	public static final String SHARED_ONE_KEY_FOR_HER = "com.aurora.user.onekeyforher";
	public static final String SHARED_CLEAR_CACHE = "com.aurora.clearcache";
    //AURORA UKILIU ADD 2014-10-20 END
	
	//通知栏更新时间
	public static final String SHARED_WIFI_APPUPDATE_KEY_UPDATETIME = "update_time";
	//当前的网络状态
	//0 -无网络  1-wifi  2-手机网络
	public static final String SHARED_NETSTATUS_KEY_ISEXITS = "net_status";
	
	//是否有下载和更新应用
	public static final String SHARED_DOWNORUPDATE_KEY_ISEXITS = "update_status";
	
	//有多少个更新应用
	public static final String SHARED_UPDATE_SUM_KEY_ISEXITS = "update_sum";
	
	
	//缓存数据的时间 
	public static final String SHARED_DATA_CACHE_KEY_UPDATETIME = "update_time";
	//用户正在使用Wi-Fi下载应用时，网络环境切换到2G/3G
	
/*	public static final String SHARED_WIFI_DISCONNECT_ISEXITS = "wifi_disconnect";
	//当次选择的结果 重启归零
	public static final String SHARED_MOBILE_STATUS_ISDOWNLOAD = "mobile_download";
	//每次启动最多提示一次，用户选择对当次启动有效
	//当次选择的结果 重启归零 
	public static final String SHARED_MOBILE_DISCONNECT_COUNT = "mobile_count";*/
	//主界面更新时间 
	public static final String SHARED_WIFI_APPMAIN_KEY_UPDATETIME = "main_update_time";
	//提示弹出框最大数量
	public static final  int SHOW_SIZE_LIMIT = 6;
	// 搜索历史记录 文件保存 关键字定义
	public static final String HISTORY_RECORDS_FILENAME = "market_search_history_records";
	public static final int HISTORY_MAX_LIMIT = 3;
	public static final String HISTORY_RECORDS = "history_records_";
	public static final String HISTORY_NEXT_INSERT_POSITION = "insert_pos";
	
	/*广播*/
	public static final String BROADCAST_ACTION_DOWNLOAD_START = "com.aurora.market.download.start";
	
	public static final String BROADCAST_ACTION_DOWNLOAD = "com.aurora.market.download";
	
	public static final String BROADCAST_ACTION_DOWNLOAD_PAUSE = "com.aurora.market.download.pause";
	
	public static final String BROADCAST_ACTION_INSTALL_WAIT ="com.aurora.market.download.install_wait";
	
	public static final String MARKET_UPDATE_ACTION = "com.aurora.market.action.update";
}
