package com.aurora.iunivoice.utils;

import android.os.Environment;

import java.io.File;

public class Globals {
    public static final boolean isTestData = false;

    public static final String LINE = "\n"; // 换行符

    //0 成功
    public static final int CODE_SUCCESS = 0;
    //1失败
    public static final int CODE_FAILED = 1;
    //-1未登录
    public static final int CODE_NOLOGIN = -1;

    //start activity for result
    public static final int REQUEST_LOGIN_CODE = 100;
    public static final int REQUEST_POSTDETAIL_CODE = 101;
    public static final int REQUEST_LOGOUT_CODE = 102;

    public static final int NETWORK_ERROR = 500;
    public static final int NO_NETWORK = 501;

    //wifi网络类型标志
    public static final int NETWORK_WIFI = 0;
    //2G网络类型标志
    public static final int NETWORK_2G = 1;
    //3G网络类型标志
    public static final int NETWORK_3G = 2;
    //http 请求地址配置  分为 3部分  url  + module + action
    
    // IuniVoice主url
    public static String HTTP_IUNIVOICE_REQUEST_URL = "http://bbs.iuni.com/api/mobile/index.php";
    // 首页-列表数据
    public static String HTTP_HOMEPAGE_LIST = HTTP_IUNIVOICE_REQUEST_URL + "?module=forumdisplayall";
    // 版块列表
    public static String HTTP_FORUM_LIST = HTTP_IUNIVOICE_REQUEST_URL + "?module=forumindex&type=1";
    // 版块内容
    public static String HTTP_FORUM_DISPLAY = HTTP_IUNIVOICE_REQUEST_URL + "?module=forumdisplay";
    // 每日签到
    public static String HTTP_SIGN_DAILY = HTTP_IUNIVOICE_REQUEST_URL + "?module=signin&id=dsu_paulsign:sign&operation=qiandao";
    // 发表新贴
    public static String HTTP_PUBLISH = "http://bbs.iuni.com/api/mobile/index.php?module=newthread";
    // 上传图片
    public static String HTTP_UPLOAD_IMAGE_FILE = "http://bbs.iuni.com/api/mobile/index.php?module=forumupload";


    //主url地址
    public static String HTTP_REQUEST_FAVOR_URL = "http://bbs.iuni.com/api/mobile/index.php?module=forummisc&action=recommend&do=add";
    public static String HTTP_REQUEST_SCORE_URL = "http://bbs.iuni.com/api/mobile/index.php?module=forummisc&action=rate";
    public static String HTTP_DETAIL_QUERY_URL = "http://bbs.iuni.com/api/mobile/index.php?module=threadinteraction";

    //首页-话题圈列表
    public static final String HTTP_SERVICE_NAME_APPLIST = "/tag";
    public static final String HTTP_APPLIST_METHOD = "/discover";

    //上传图片
    public static final String HTTP_SERVICE_UPLOAD_PHOTO = "/attachment";
    public static final String HTTP_UPLOAD_PHOTO_METHOD = "/upload";

    //上传文章
    public static final String HTTP_UPLOAD_ARTICLE_METHOD = "/save";

    //个人主页-发表

    public static final String HTTP_SERVICE_PUBLISH = "/user";
    public static final String HTTP_PUBLISH_METHOD = "/record";//发表
    public static final String HTTP_COLLECTION_METHOD = "/favor";//私藏

    public static final String HTTP_ADD_FAVOUR = "/addfavor";//添加私藏
    public static final String HTTP_CANCEL_FAVOUR = "/delfavor";//取消私藏

    //文章详情

    public static final String HTTP_SERVICE_POST = "/post";
    public static final String HTTP_INDEX_METHOD = "/index";//文章详情
    public static final String HTTP_DELETE_POST_METHOD = "/delete";//删除文章

    //评论
    public static final String HTTP_SERVICE_COMMENT = "/comment";
    public static final String HTTP_GETLIST_METHOD = "/getlist";//获取评论列表
    public static final String HTTP_ADD_COMMENT_METHOD = "/add";//评论文章（或者评论）

    //圈子列表文章
    public static final String HTTP_POSTS_METHOD = "/posts";

    public static final String HTTP_SERVICE_NOTICE = "/notice";
    public static final String HTTP_LIST_METHOD = "/getlist";
    public static final String HTTP_MESSAGE_READALL = "/readall";
    public static final String HTTP_MESSAGE_CLEAR = "/delall";
    public static final String HTTP_MESSAGE_DELETE = "/del";

    public static final String GB = "GB";
    public static final String MB = "MB";
    public static final String KB = "KB";
    public static final String B = "B";

    public static final String USER_AGREEMENT_HTML_URL = "http://www.iunios.com/useragreement.html";

    // 相机拍照照片路径
    public static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/IuniVoice/.icon/");

    public static final String EXTRA_COMMAND = "command"; // 通过onActivityResult带回来的命令码
    public static final int COMMAND_UNKNOW = -1;
    public static final int COMMAND_LOGOUT = 1;
    public static final int COMMAND_REFRESH_EMAIL = 2;

    public static final String LOCAL_LOGIN_ACTION = "LOCAL_LOGIN_ACTION";
    public static final String LOCAL_LOGIN_RESULT = "login_result";
    public static final int LOCAL_LOGIN_FAIL = 0;
    public static final int LOCAL_LOGIN_SUCCESS = 1;
    public static final int LOCAL_LOGOUT_SUCCESS = 2;

    public static String HTTPS_ACCOUNT_REQUEST_URL = "https://ucloud.iunios.com/account";
    public static String HTTP_ACCOUNT_REQUEST_URL = "http://ucloud.iunios.com/account";
    public static String HTTP_PAGE_REQUEST_URL = "http://bbs.iuni.com/api/mobile/index.php";
    public static String HTTP_WEB_PAGE_REQUEST_URL = "http://bbs.iuni.com/thread-";
    public static String HTTP_WEB_PAGE_DETAIL_URL="http://bbs.iuni.com/api/mobile/index.php?module=threadview";
    public static String HTTP_WEB_PAGE_DETAIL_URL_NEW = "http://bbs.iuni.com/forum.php?mod=viewthread";
    
    public static final String HTTP_WEB_PAGE_DETAIL_MOBILE = "&mobile=4";
    public static final String HTTP_WEB_PAGE_DETAIL_TIP="&tip=";

    /*登录接口 */
    public static final String MODULE_AUTH = "?module=auth";

    public static final String HTTPS_COOKIE_METHOD = "&action=tgt";

    public static final String HTTPS_LOGIN_METHOD = "&action=login";
    public static final String HTTPS_REGISTER_METHOD = "&action=register";
    public static final String HTTPS_VERIFYCODE_METHOD = "&action=verifycode";
    public static final String HTTPS_FINDPWD_METHOD = "&action=findpwd";
    public static final String HTTPS_LOGOUT_METHOD = "&action=logout";
    public static final String HTTPS_VCIMG_METHOD = "&action=vcimg";
    public static final String HTTPS_VALIDATE_FINDPWDVC = "&action=validatefindpwdvc";

    public static final String MODULE_PROFILE = "?module=profile";
    public static final String MODULE_SEND_REPLY = "?module=sendreply";
    public static final String MODULE_GET_PAGE = "?module=threadview";

    public static final String HTTPS_USERINFO_METHOD = "&action=detail";
    public static final String HTTPS_EDIT_METHOD = "&action=edit";
    public static final String HTTPS_CHANGEPWD_METHOD = "&action=changepwd";
    public static final String HTTPS_CHANGEPHONE_METHOD = "&action=bindphone";
    public static final String HTTPS_CHANGEEMAIL_METHOD = "&action=bindemail";
    public static final String HTTPS_CHANGEPHOTO_METHOD = "&action=changephoto";
    public static final String HPTTS_RESEND_VERIFY_EMAIL = "&action=resendverifyemail";
    public static final String HTTPS_CHECKCURPHONE_METHOD = "&action=checkcurphone";
    public static final String HTTPS_VALIDATECHGPHONEVC_METHOD = "&action=validatechgphonevc";
    public static final String HTTPS_CHECKCUREMAIL_METHOD = "&action=checkcuremail";


    public static final String PREF_TIMES_MAIN = "main_times";    //点击“主页"时，统计一次
    public static final String PREF_TIMES_MY = "my_times";  // 点击“我的”时，统计一次
    public static final String PREF_TIMES_FINISH = "finish_times";    //选择图片点击“完成”时，统计一次
    public static final String PREF_TIMES_PUBLISH2 = "pulish2_times";    //文章编辑界面点击“发表”时，统计一次
    public static final String PREF_TIMES_GROUP = "group_times";  //点击话题组①时，统计一次
    public static final String PREF_TIMES_GROUP2 = "group2_times";        //点击话题组②时，统计一次
    public static final String PREF_TIMES_GROUP3 = "group3_times";   //点击话题组③时，统计一次
    public static final String PREF_TIMES_GROUP4 = "group4_times";   //点击话题组④时，统计一次
    public static final String PREF_TIMES_GROUP5 = "group5_times";                //点击话题组⑤时，统计一次
    public static final String PREF_TIMES_COLLECT = "collect_times";            //查看文章详情点击“收藏”时，统计一次
    public static final String PREF_TIMES_SHARE = "share_times";        //查看文章详情点击“分享”时，统计一次
    public static final String PREF_TIMES_PUBLISH = "publish_times";        //公共底部点击“发布”按钮时，统计一次
    public static final String PREF_TIMES_PUBLISH3 = "publish3_times";    //个人主页点击“发表”时，统计一次
    public static final String PREF_TIMES_COLLECTION = "collection_times";        //个人主页点击“私藏”时，统计一次
    public static final String PREF_TIMES_EDIT = "edit_times";                //个人主页点击“编辑个人资料”箭头时，统计一次
    public static final String PREF_TIMES_NEWS = "news_times";             //个人主页点击“消息”icon时，统计一次

    public static final String APP_CACAHE_DIRNAME = "/app_webview";
    public static final String SETTING_SP = "settingSp";
    public static final String VERSION = "version";
    public static final String SCHEME_WTAI_MC = "wtai://wp/mc;";

	//版本更新
	public static final String BROADCAST_ACTION_UPLOAD = "com.aurora.iunivoice.VERSION_UPLOAD";
	public static final String BROADCAST_ACTION_DOWNLOAD = "com.aurora.iunivoice.VERSION_DOWNLOAD";

    //评论key
    public static final String COMMENT_IUNI_BBS_IDENTIFY = "iunibbsapp";
    public static final String COMMENT_REPLY_SUBMIT = "replysubmit";
    public static final String COMMENT_FORM = "formhash";
    public static final String COMMENT_RATE = "ratesubmit";
    public static final String COMMENT_PLAT = "inajax";
    public static final String COMMENT_SCORE = "score1";
    public static final String COMMENT_REASON = "reason";
    public static final String COMMENT_HASH = "hash";
    public static final String COMMENT_MESSAGE_CONTENT = "message";
    public static final String COMMENT_FID ="fid";
    public static final String COMMENT_TID = "tid";
    public static final String COMMENT_YES = "yes";
    
    //版本更新
	public static  String HTTP_APPUPDATE_REQUEST_URL = "http://appmarket.iunios.com/systemappupgradeservice";
	//测试
//	public static String HTTP_APPUPDATE_REQUEST_URL= "http://test.appmarket.iunios.com/systemappupgradeservice";
	
	public static final String HTTP_APPUPDATE_SERVICE_NAME_APPLIST = "?module=systemapp";
	public static final String HTTP_APPUPDATE_UPGRADEAPP_METHOD="&action=update";
}
