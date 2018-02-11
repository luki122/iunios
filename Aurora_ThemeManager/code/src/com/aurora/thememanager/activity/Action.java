package com.aurora.thememanager.activity;

public class Action {
	
	
	public static final String ACTION_THEME = "com.aurora.thememamager.ACTION_THEME";
	
	public static final String ACTION_WALL_PAPER = "com.aurora.thememamager.ACTION_WALL_PAPER";
	
	public static final String ACTION_FONTS = "com.aurora.thememamager.ACTION_FONTS";
	
	public static final String ACTION_AUDIO = "com.aurora.thememamager.ACTION_AUDIO";
	
	public static final String ACTION_APPLY_THEME = "com.aurora.thememamager.ACTION_APPLY_THEME";
	
	
	public static final String ACTION_PREVIEW_TIME_WALLPAPER = "com.aurora.thememamager.ACTION_PREVIEW_TIME_WALLPAPER";
	
	public static final String ACTION_PREVIEW_WALLPAPER = "com.aurora.thememamager.ACTION_PREVIEW_WALLPAPER";
	
	/**
	 * 显示预览图片列表的action
	 */
	public static final String ACTION_SHOW_PREVIEW_PICTURE_PAGER = "com.aurora.thememanager.ACTION_SHOW_PREVIEW_PICTURE_PAGER";
	
	/**
	 * 网络连接和断开的action
	 */
	public static final String ACTION_NETWORK_CONNECT = "android.net.conn.CONNECTIVITY_CHANGE";
	
	/**
	 * 本地导入主题的action
	 */
	public static final String ACTION_PICK_THEME="com.aurora.thememamager.ACTION_PICK_THEME";
	
	
	/**
	 * 主题切换的广播使用的action
	 */
	public static final String ACTION_THEME_CHANGED="com.aurora.thememamager.ACTION_THEME_CHANGED";
	
	/**
	 * 主题切换后的ThemeID,用于发送给接收方进行判断
	 */
	public static final String KEY_THEME_CHANGED_ID="Applied_theme_id";
	
	
	/**
	 * 显示预览图片中默认的显示位置的KEY
	 */
	public static final String KEY_SHOW_PREIVEW_PICTURE_INDEX = "KEY_SHOW_PREIVEW_PICTURE_INDEX";
	
	/**
	 *缓存好的图片的key
	 */
	public static final String KEY_SHOW_PREIVEW_PICTURE_CACHE = "KEY_SHOW_PREIVEW_PICTURE_CACHE";
	
	/**
	 * 图片的网络URL
	 */
	public static final String KEY_SHOW_PREIVEW_PICTURE_URL = "KEY_SHOW_PREIVEW_PICTURE_URL";
	
	public static final String KEY_SHOW_PREIVEW_PICTURE_SAVED_PATH = "KEY_SHOW_PREIVEW_PICTURE_SAVED_PATH";
	
	/**
	 * 显示时光锁屏主题包预览图的key
	 */
	public static final String KEY_SHOW_TIME_WALL_PAPER_PREVIEW = "KEY_SHOW_TIME_WALL_PAPER_PREVIEW";
	
	public static final String KEY_SHOW_WALL_PAPER_PREVIEW = "KEY_SHOW_WALL_PAPER_PREVIEW";
	
	public static final String KEY_GOTO_LOCAL_TIME_WALLPAPER_LOCATION = "location";

	public static final String KEY_GOTO_SHOP = "go_to_shop";
	
	private Action(){
		
	}
	
	

}
