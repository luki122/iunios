/*
 * @author 张伟
 */
package com.aurora.account.util;

import android.os.Environment;

import java.io.File;



public class Globals {


	public static final boolean isTestData = false;
	//图片类型
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";
	
	public static final String NEW_LINE = "\n\n"; // 换行符
	public static final String LINE = "\n"; // 换行符
	
	public static final String GB = "GB";
	public static final String MB = "MB";
	public static final String KB = "KB";
	public static final String B = "B";
	
	// 相机拍照照片路径
    public static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/Account/icon/");
	
	public final static int REQUEST_CODE_BASE = 1;
	public final static int REQUEST_CODE_CAPTURE = REQUEST_CODE_BASE + 0;
	public final static int REQUEST_CODE_ALBUM = REQUEST_CODE_BASE + 1;
	public final static int REQUEST_CODE_VIDEO = REQUEST_CODE_BASE + 2;
	public final static int REQUEST_GETVIDEO_VIDEO = REQUEST_CODE_BASE + 3;
	public final static int REQUEST_CODE_ADD_REMINDER = REQUEST_CODE_BASE + 4;
	
	public static final String EXTRA_COMMAND = "command"; // 通过onActivityResult带回来的命令码
	public static final int COMMAND_UNKNOW = -1;
	public static final int COMMAND_LOGOUT = 1;
	public static final int COMMAND_REFRESH_EMAIL = 2;
	
	public static final String SIGN_NOINDENT_ID = "1";
	public static final String SIGN_INDENT_ID = "2";
	
	public static final String USER_AGREEMENT_HTML_URL = "http://www.iunios.com/useragreement.html";
	
	public static final String HTTP_REQUEST_KAIFA_URL = "http://18.8.0.244/service"; 

	public static final String HTTP_REQUEST_DEFAULT_URL = "http://ucloud.iunios.com/account"; 
	public static final String HTTPS_REQUEST_DEFAULT_URL = "https://ucloud.iunios.com/account"; 
	public static  String HTTPS_REQUEST_URL = "https://ucloud.iunios.com/account";
	public static  String HTTP_REQUEST_URL = "http://ucloud.iunios.com/account";
	
	public static  String HTTP_REQUEST_TEST_URL = "http://test.ucloud.iunios.com/account";
	public static  String HTTPS_REQUEST_TEST_URL = "https://test.ucloud.iunios.com/account";
	
	//public static final String HTTP_REQUEST_URL = "http://18.8.0.39/service"; 
	public static final String HTTPS_ACTION_PARAM = 
		    "&parmJson=";
	
	/*登录接口 */
	public static final String MODULE_AUTH = "?module=auth";
	
	public static final String HTTPS_COOKIE_METHOD="&action=tgt";	
		
	public static final String HTTPS_LOGIN_METHOD="&action=login";
    public static final String HTTPS_REGISTER_METHOD="&action=register";
    public static final String HTTPS_VERIFYCODE_METHOD="&action=verifycode";
    public static final String HTTPS_FINDPWD_METHOD="&action=findpwd";
    public static final String HTTPS_LOGOUT_METHOD="&action=logout";
    public static final String HTTPS_VCIMG_METHOD="&action=vcimg";
    public static final String HTTPS_VALIDATE_FINDPWDVC = "&action=validatefindpwdvc";
    
    //短信
    public static final String MODULE_SYNC = "?module=sync";
    
    public static final String HTTPS_SYNC_DATA_UPLOAD = "&action=upload";
    public static final String HTTPS_SYNC_DATA_DOWNLOAD = "&action=download";
    public static final String HTTPS_SYNC_DOWN_COUNT = "&action=count";
    public static final String HTTPS_SYNC_UP_POS = "&action=getlastsegmentpos";
    
	public static final String MODULE_PROFILE = "?module=profile";
	
	public static final String HTTPS_USERINFO_METHOD="&action=detail";
	public static final String HTTPS_EDIT_METHOD="&action=edit";
	public static final String HTTPS_CHANGEPWD_METHOD="&action=changepwd";
	public static final String HTTPS_CHANGEPHONE_METHOD="&action=bindphone";
	public static final String HTTPS_CHANGEEMAIL_METHOD="&action=bindemail";
	public static final String HTTPS_GETLASTSYNCTIME_METHOD="&action=getlastsynctime";
	public static final String HTTPS_CHANGEPHOTO_METHOD="&action=changephoto";
	public static final String HPTTS_RESEND_VERIFY_EMAIL="&action=resendverifyemail";
	public static final String HTTPS_GETINITMAP_METHOD="&action=getkeys";
	public static final String HTTPS_GETDATA_BYIDS_METHOD = "&action=getdatabyids";
	public static final String HTTPS_CHECKCURPHONE_METHOD="&action=checkcurphone";
	public static final String HTTPS_VALIDATECHGPHONEVC_METHOD="&action=validatechgphonevc";
	public static final String HTTPS_CHECKCUREMAIL_METHOD="&action=checkcuremail";
	
	public static final String MODULE_ATTACHMENT = "?module=attachment";
	
    public static final String HTTPS_UPLOAD_METHOD="&action=upload";
    public static final String HTTPS_DOWNLOAD_METHOD="&action=download";
    
	
	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;
	
	public static final int NETWORK_ERROR = 500;
	public static final int NO_NETWORK = 501;
	public static final int SERVER_ERROR = 502;
	public static final int SESSION_EXPIRED_ERROR = 503; // 密码不对
	
	//activity result 100
	public static final int REQUEST_CODE_BASE_USER = 100;
	public static final int REQUEST_CODE_ADD_RECORD =  REQUEST_CODE_BASE_USER+1;
	public static final int REQUEST_CODE_OPEN_NEWNOTE =  REQUEST_CODE_BASE_USER+2;
	public static final int REQUEST_CODE_MODIFY_LABEL = REQUEST_CODE_BASE_USER + 3;
	
	
	
	/*SharedPreferences*/
	public static final String SHARED_WIFI_SYNC = "com.aurora.wifi.sync";
	/*SharedPreferences*/
	public static final String SHARED_MODULE_SYNC = "com.aurora.module.sync";
	/*SharedPreferences*/
	public static final String SHARED_MODULE_SYNC_TIME = "com.aurora.module.sync.time";
	/*SharedPreferences*/
	public static final String SHARED_MODULE_ACCOUNT_SWITCH = "com.aurora.module.account.switch";
	/*SharedPreferences*/
	public static final String SHARED_MODULE_ACCOUNT_REPEAT = "com.aurora.module.account.repeat";
	/*SharedPreferences*/
	public static final String SHARED_TIME_SYNC = "com.aurora.time.sync";
	/*SharedPreferences*/
	public static final String SHARED_WIFI_UPDATE = "com.aurora.wifi.update";
	/*SharedPreferences*/
	public static final String SHARED_APP_UPDATE = "com.aurora.app.update";
	/*SharedPreferences*/
	public static final String SHARED_DATA_UPDATE = "com.aurora.data.update";
	// setting for first login
	public static final String SHARED_FIRST_LOGIN_CONFIG = "com.aurora.user.config";
	public static final String SHARED_FIRST_KEY = "com.aurora.isfirst";
	/*SharedPreferences key*/
	public static final String SHARED_WIFI_SYNC_KEY = "com.aurora.wifi.sync.key";
	//通知栏更新时间
	public static final String SHARED_WIFI_APPUPDATE_KEY_UPDATETIME = "update_time";
	//当前的网络状态
	//0 -无网络  1-wifi  2-手机网络
	public static final String SHARED_NETSTATUS_KEY_ISEXITS = "net_status";
	
	//SHARED_TIME_SYNC
	public static final String SHARED_TIMESTMAP_SYNC_KEY = "com.aurora.timestamp.sync.key";
	public static final String SHARED_SERVERTIME_SYNC_KEY = "com.aurora.servertime.sync.key";
	
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
	public static final String BROADCAST_ACTION_DOWNLOAD_START = "com.aurora.account.download.start";
	
	public static final String BROADCAST_ACTION_DOWNLOAD = "com.aurora.account.download";
	
	public static final String BROADCAST_ACTION_DOWNLOAD_PAUSE = "com.aurora.account.download.pause";
	
	public static final String BROADCAST_ACTION_INSTALL_WAIT ="com.aurora.account.download.install_wait";
	
	public static final String MARKET_UPDATE_ACTION = "com.aurora.account.action.update";
	
	
	// 自动备份
	public static final int AUTO_BACKUP_DELAY_TIME = 30;	// 30分钟
	
	// 云相册相关 start
	
	public static final String GALLERY_PACKAGE_NAME = "com.android.gallery3d";	// 云相册包名
	
	public static final String GALLERY_DO_SYNC_TIME = "gallery_do_sync_time";	// 云相册启动同步的时间
	
	/*
	 * 1.发送备份广播 intent action : com.aurora.gallery.upload
	   2.备份时间广播 intent action : com.aurora.gallery.notify.time
             key: time   value:2015-5-27 14:30:22
	 * */
	public static final String ACTION_PHOTO_DO_SYNC = "com.aurora.gallery.upload";
	
	public static final String GALLERY_SYNC_TIME_KEY = "time";
	
	
	// 云相册相关 end
	
}
