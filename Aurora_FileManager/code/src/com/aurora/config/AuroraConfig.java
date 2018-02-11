package com.aurora.config;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AuroraConfig {

	/**
	 * Control statistics are valid
	 */
	public static final boolean isStatistics = true;


	public static final String fileManagerKey = "250";

	public static List<String> statisticsTag = new ArrayList<String>();

	static {
		for (int i = 1; i < 21; i++) {
			statisticsTag.add(String.format("%03d", i));
		}
	}

	/**
	 * 查询过滤图片大小
	 */
	public static int AURORA_PIC_SIZE = 20 * 1024;
	public static int AURORA_VIDEO_SIZE = 50 * 1024;// 50KB

	public static final int AURORA_HOME_PAGE = 0;
	public static final int AURORA_PIC_PAGE = 1;
	public static final int AURORA_CATEGORY_PAGE = 2;
	public static final int AURORA_FILE_PAGE = 3;
	public static final int AURORA_PIC_ITEM_PAGE = 4;

	public static final String CameraPath = "DCIM/Camera";
	public static final String DOWNLOADACTION = "android.intent.action.VIEW_DOWNLOADS";

	public static String AURORA_PIC_MANAGER = "com.aurora.pic.file.manager.action";
	public static String AURORA_AUDIO_MANAGER = "com.aurora.audio.file.manager.action";
	public static String AURORA_WALLPAPER_MANAGER = "com.aurora.pic.wallpaper.manager.action";
	public static String AURORA_WALLPAPER_CHANGE = "com.aurora.change";
	public static String AURORA_WALLPAPER_CHANGE_ACTIVITY = "com.aurora.change.activities.DesktopWallpaperSourceSelectActivity";
	public static String AURORA_WALLPAPER_CHANGE_ACTIVITY_2 = "com.aurora.change.activities.WallpaperSourceSelectActivity";
	public static String AURORA_WALLPAPER_CHANGE_ACTIVITY_3 = "com.aurora.change.activities.DesktopWallpaperLocalActivity";
	public static String AURORA_WALLPAPER_CHANGE_ACTIVITY_4 = "com.aurora.change.activities.WallpaperLocalActivity";
	public static String AURORA_WALLPAPER_TYPE = "file_type";
	public static String AURORA_WALLPAPER_TYPE_1 = "desktop";
	public static String AURORA_WALLPAPER_TYPE_2 = "lockscreen";

	/**
	 * action for external scanning
	 */
	public final static String ACTION_EXT_SCAN = "android.intent.action.AURORA_EXTERNAL_SCAN";

	/**
	 * action for directory scanning
	 */
	public final static String ACTION_DIR_SCAN = "android.intent.action.AURORA_DIRECTORY_SCAN";

	/**
	 * action for file scanning
	 */
	public final static String ACTION_FILE_SCAN = "android.intent.action.AURORA_FILE_SCAN";

	/**
	 * IUNI 自带APP 跳转到文官图片分类获取单张图片
	 */
	public final static String ACTION_SINGLE_GET_CONTENT = "com.aurora.filemanager.SINGLE_GET_CONTENT";

	/**
	 * IUNI 自带APP 跳转到文官图片分类获取多张图片
	 */
	public final static String ACTION_MORE_GET_CONTENT = "com.aurora.filemanager.MORE_GET_CONTENT";

	/**
	 * 添加隐私图片
	 */
	public final static String ACTION_MORE_PRI_GET_CONTENT = "com.aurora.filemanager.MORE_PRI_GET_CONTENT";

	/**
	 * 添加隐私视频人口
	 */
	public final static String ACTION_MORE_VIDEO_CONTENT = "com.aurora.filemanager.MORE_VIDEO_GET_CONTENT";
	/**
	 * 从SdCard路径获取文件
	 */
	public final static String ACTION_FILE_GET_CONTENT = "com.aurora.filemanager.FILE_GET_CONTENT";

	private static List<String> pathString = new ArrayList<String>();

	/**
	 * @return the pathString
	 */
	public static List<String> getPathString() {
		return pathString;
	}

	static void addStaticPath(String path) {
		pathString.add(path);
	}

	static {
		addStaticPath("/wandoujia"); // 豌豆荚
		addStaticPath("/Tencent/tassistant/apk");// 应用宝
		addStaticPath("/baidu/AppSearch/downloads");// 百度手机助手
		addStaticPath("/UCDownloads");// UC浏览器
		addStaticPath("/QQBroswer");// QQ浏览器
		addStaticPath("/360Broswer/download");// 360浏览器
		addStaticPath("/MxBrowser/Downloads");// 遨游浏览器
		addStaticPath("/baidu/flyflow/downloads");// 百度浏览器
		addStaticPath("/Download");// 欧鹏浏览器,搜狗浏览器
		addStaticPath("/kbrower/download");// 猎豹浏览器
		addStaticPath("/netease/cloudmusic/Music");// 网易云音乐
		addStaticPath("/DUOMI/down");// 多米音乐
		addStaticPath("/Baidu_music/download");// 百度音乐
		addStaticPath("/kgmusic/download");// 酷狗音乐
		addStaticPath("/KuwoMusic/music");// 酷我音乐
		addStaticPath("/qqmusic/song");// QQ音乐
		addStaticPath("/ttpod/song");// 天天动听
		addStaticPath("/诠音/download");// 诠音
		addStaticPath("/NubiaMusic/songs");// NubiaMusic
		addStaticPath("/baidu/download"); // 百度音乐HD
		addStaticPath("/ttpod_hd/song"); // 天天动听HD
		addStaticPath("/360Video");// 360视频
		addStaticPath("/market/apk");// 应用市场

	}

	// add by Jxh 隐私相关 2014-9-9 begin
	public static final String IMAGEID = "image";
	public static final String VIDEOID = "video";
	public static final String AUDIOID = "audio";
	public static final String PRIVACYACTION = "com.privacymanage.service.IPrivacyManageService";
	public static final String DELETE_ACCOUNT_ACTION = "com.aurora.privacymanage.DELETE_ACCOUNT";
	public static final String SWITCH_ACCOUNT_ACTION = "com.aurora.privacymanage.SWITCH_ACCOUNT";

	public static final String KEY_ACCOUNT = "account";
	public static final String KEY_DELETE = "delete";

	// add by Jxh 隐私相关 2014-9-9 end

	public static boolean isOtherAppDel(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				KEY_ACCOUNT, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(KEY_DELETE, false);
	}

	public static void setOtherAppDel(Context context, boolean isDel) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				KEY_ACCOUNT, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(KEY_DELETE, isDel);
		editor.commit();
	}

}
