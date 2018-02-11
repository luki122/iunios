package com.secure.data;

import java.util.HashMap;
import com.lbe.security.service.sdkhelper.SDKConstants;
import android.Manifest.permission;
import com.aurora.secure.R;

/**
 * 常量
 */
public class Constants {
	
	/**
	 * 获取lbe权限对应的图标
	 * @param perId
	 * @return
	 */
	public static final int getLbePermissionIcon(int perId){
		int permissionIcon;
		switch(perId){
		case SDKConstants.PERM_ID_CALLLOG:
			permissionIcon = R.drawable.permission_img_of_calllog;
			break;
		case SDKConstants.PERM_ID_CALLMONITOR:
			permissionIcon = R.drawable.permission_img_of_callmonitor;
			break;
		case SDKConstants.PERM_ID_CALLPHONE:
			permissionIcon = R.drawable.permission_img_of_callphone;
			break;
		case SDKConstants.PERM_ID_CALLSTATE:
			permissionIcon = R.drawable.permission_img_of_callstate;
			break;
		case SDKConstants.PERM_ID_CONTACT:
			permissionIcon = R.drawable.permission_img_of_contact;
			break;
		case SDKConstants.PERM_ID_LOCATION:
			permissionIcon = R.drawable.permission_img_of_location;
			break;
		case SDKConstants.PERM_ID_NETDEFAULT:
			permissionIcon = R.drawable.permission_img_of_netdefault;
			break;
		case SDKConstants.PERM_ID_NETWIFI:
			permissionIcon = R.drawable.permission_img_of_netwifi;
			break;
		case SDKConstants.PERM_ID_PHONEINFO:
			permissionIcon = R.drawable.permission_img_of_phoneinfo;
			break;
		case SDKConstants.PERM_ID_AUDIO_RECORDER:
		    permissionIcon = R.drawable.permission_img_of_audio_recorder;
            break;
		case SDKConstants.PERM_ID_VIDEO_RECORDER:
			permissionIcon = R.drawable.permission_img_of_recorder;
			break;
		case SDKConstants.PERM_ID_ROOT:
			permissionIcon = R.drawable.permission_img_of_root;
			break;
		case SDKConstants.PERM_ID_SENDSMS:
			permissionIcon = R.drawable.permission_img_of_sendmsg;
			break;
		case SDKConstants.PERM_ID_SETTINGS:
			permissionIcon = R.drawable.permission_img_of_settings;
			break;
		case SDKConstants.PERM_ID_SMSDB:
			permissionIcon = R.drawable.permission_img_of_smsdb;
			break;
		default:
			permissionIcon = R.drawable.permission_img_of_def;
			break;
		}
		return permissionIcon;
	}
	
	/**
	 * 自启动白名单
	 */
	public final static String[] autoStartWhiteList={
		"alipay",
		"taobao",
		"tencent",
		"baidu",
		"jp.naver.line.android",//line 
		"com.xiaomi.channel",//米聊
		"com.netease.mobimail",//网易邮箱
		"com.immomo.momo",//陌陌
		"com.sina.weibo",//新浪微博
		"com.whatsapp",//whatsapp
		"im.yixin",//易信
		"cn.com.fetion",//飞信
		"com.feinno.felio",//飞聊
		"com.alibaba.android.babylon",//来往
		"com.alibaba.mobileim",//旺信
		"weather",//天气类软件
		"tianqi",//天气类软件
		"clock"//时钟类软件
	};
	
	public final static String[] autoStartWhiteList2={
		"alipay",
		"taobao",
		"tencent",
		"com.tencent",
//		"com.tencent.mm",//微信
		"jp.naver.line.android",//line 
		"com.xiaomi.channel",//米聊
//		"com.tencent.mobileqq",//QQ
//		"com.tencent.androidqqmail",//QQ邮箱 
		"com.netease.mobimail",//网易邮箱
		"com.immomo.momo",//陌陌
		"com.sina.weibo",//新浪微博
		"com.whatsapp",//whatsapp
		"im.yixin",//易信
		"cn.com.fetion",//飞信
		"com.feinno.felio",//飞聊
		"com.alibaba.android.babylon",//来往
		"com.alibaba.mobileim",//旺信
		"weather",//天气类软件
		"tianqi",//天气类软件
		"clock"//时钟类软件
	};
	
	/**
	 * 系统应用显示白名单,三星手机 
	 * 该配置表同在应用管理与流量统计，注意做到两边同步
	 */
	public final static String[] sysAppWhiteList={		
		"com.android.calendar", //S日历
		"com.android.chrome",//chrome
		"com.android.gallery3d",//相册
		"com.aurora.filemanager",//文件管理
		"com.android.providers.downloads.test",//下载管理
		"com.samsung.everglades.video",//视频
		"com.sec.android.app.music",//音乐
		"com.sec.android.widgetapp.diotek.smemo",//S备忘录
		"com.vlingo.midas",//S voice
		"gn.com.android.update",//系统升级
		"com.android.email",//电子邮件
		"com.google.android.syncadapters.calendar",//google日历同步
		"com.google.android.syncadapters.contacts",//google通讯录同步
		"com.sec.android.app.clockpackage",//时钟
		"com.aurora.launcher",
		"com.baidu.map.location"		//网络位置
	};
	
	/**
	 * 不用在列表中显示的系统组件
	 */
	public final static String[] sysSubgrounpNoShowList={
		"com.caf.fmradio" //FM 收音机
	};
	
	/**
	 * 系统应用不能停用名单
	 */
	public final static String[]sysAppCanNotDisableList={
		"com.aurora.secure",
		"com.lbe.security.iunios",
		"com.android.systemui",
		"com.sohu.inputmethod.sogouoem"
	};
	
	/**
	 * 有些系统应用没有launcher属性，但是要显示在系统应用列表（不显示在系统组件中）
	 */
	public final static String[] showInSysAppList={
		"com.sohu.inputmethod.sogouoem"
	};
	
	/**
	 * 判断指定packageName是不是在指定的List中
	 * @return
	 */
	public static boolean isPackageNameInList(String[] list,String packageName){
		if(list == null || packageName == null){
			return false;
		}
		
		for(int i=0;i<list.length;i++){
			if(packageName.equals(list[i])){
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * 联系人，拨号包名
	 */
	public static final String CONTACT_PKG = "com.android.contacts";
	
	/**
	 * IUNI自带应用包名表
	 */
	public static final String[] iuniPackageNameList={
		"com.android.music",//音乐
		"com.sohu.inputmethod.sogouoem", //输入法：
		"com.android.browser", //浏览器
		"com.android.gallery3d", //图库
		"com.android.contacts", //联系人
		"com.android.mms", //短信
		"com.android.phone", //拨号
		"com.aurora.launcher", //桌面
		"com.android.camera" //相机
		,"com.android.chrome"//浏览器
		,"com.oppo.camera"//一加相机
		,"com.sec.android.app.camera"//三星相机
	};
	
//	public static final String[] NOT_IUNI_SYS_APP_PKG_LIST={
//		"com.sec.android.gallery3d",
//		"com.sec.android.app.videoplayer",
//		"com.sec.android.app.music",
//		"com.sec.android.mmapp"
//	};
	
	public static final String SCHEME = "package";
	
	/**
	 * “正在运行”显示黑名单：
	 */
	public static final String[] NOT_SHOW_RUNNINGA_APP = {
		"android",	//Android系统
		"com.android.systemui",	//系统用户界面
		"com.android.phone",//手机
		"com.android.providers.contacts",	//联系人存储
		"com.qualcomm.logkit",	//Log Kit
		"com.qualcomm.services.location",	//LocationServices
		"com.aurora.scanner.fileobserver",	//Aurora_MediaScanner
		"com.qualcomm.qcrilmsgtunnel",	//com.qualcomm.qcrilmsgtunnel
		"com.qualcomm.timeservice",	//com.qualcomm.timeservice
		"com.qualcomm.rcsimsbootstrap",	//com.qualcomm.rcsimsbootstrap
		"com.qualcomm.atfwd",	//com.qualcomm.atfwd
		"com.qualcomm.rcsimsbootstraputil"//com.qualcomm.rcsimsbootstraputil
	};
	
	/**
	 * 需要显示在当前正在运行应用列表中的系统组件
	 */
	public static final String[] SHOW_RUNNINGA_APP_FOR_SYS_APP_SUB = {
		"com.android.providers.downloads.test",
	};
	
	
	
}
