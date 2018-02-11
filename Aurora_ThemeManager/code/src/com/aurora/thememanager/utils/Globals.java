/*
 * @author 张伟
 */
package com.aurora.thememanager.utils;

import android.os.Environment;

import java.io.File;


public class Globals {


	public static final boolean isTestData = false;
	//图片类型
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";
	
	public static final String NEW_LINE = "\n\n"; // 换行符
	public static final String LINE = "\n"; // 换行符
	
	public static final String SIGN_NOINDENT_ID = "1";
	public static final String SIGN_INDENT_ID = "2";
	
	
	
	//public static final String HTTP_REQUEST_URL = "http://18.8.0.39/service"; 
	public static final String HTTP_ACTION_PARAM = 
		    "&parmJson=";
	
	
	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;
	
	public static final int NETWORK_4G = 3;
	
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
	
	//主界面更新时间 
	public static final String SHARED_WIFI_APPMAIN_KEY_UPDATETIME = "main_update_time";
	//提示弹出框最大数量
	public static final  int SHOW_SIZE_LIMIT = 6;
}
