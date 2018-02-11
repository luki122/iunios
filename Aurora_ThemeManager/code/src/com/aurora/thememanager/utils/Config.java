package com.aurora.thememanager.utils;

public class Config {

	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";


	/*
	 * path for the theme that avaliable
	 */
	
	public static final String THEME_BASE_PATH = "/data/theme/"; // chmod 755
																	// theme
	/**
	 * path for new theme
	 */
	public static final String THEME_PATH = THEME_BASE_PATH + "current/";// chmod
																			// 755
																			// current

	/**
	 * path for boot animation and audio
	 */
	public static final String THEME_BOOT_PATH = THEME_PATH + "boot/";

	/**
	 * path for theme's fonts
	 */
	public static final String THEME_FONTS_PATH = THEME_PATH + "fonts/";

	/**
	 * path for theme's wallpaper
	 */
	public static final String THEME_WALL_PAPAER_PATH = THEME_PATH
			+ "wallpaper/";

	/**
	 * path for theme's audio
	 */
	public static final String THEME_AUDIO_PATH = THEME_PATH + "audio/";
	
	/**
	 * path for theme's lockscreen
	 */
	public static final String THEME_LOCKSCREEN_PATH = THEME_PATH + "lockscreen/";

	/**
	 * path for backup theme
	 */
	public static final String THEME_BACKUP_PATH = THEME_BASE_PATH + "backup/";// chmod
																				// 755

	/**
	 * path for downloaded theme
	 */
	public static final String THEME_DOWNLLOAD_PATH = "/sdcard/IUNI/theme/download/";

	public static final String THEME_ICONS_CACHE_PATH = "/data/aurora/icons/";

	/*
	 * path for store theme information that was loaded into thememanager
	 */
	public static final String THEME_LOADED_PATH = "/sdcard/IUNI/theme/.data/";

	public static final String THEME_LOADED_PREVIEW = THEME_LOADED_PATH+"preview/";

	public static final String THEME_LOADED_INFO = THEME_LOADED_PATH+"info/";
	

	/*
	 * theme type for ThemeApplyUtils,all of type only used when create
	 * themeManager
	 */
	public static final String THEME_FONTS = "fonts";
	public static final String THEME_WALLPAPER = "wallpaper";
	public static final String THEME_RINGTONG = "ringtong";
	public static final String THEME_ALL = "all";

	/*
	 * status for load theme into thememanager
	 */
	/**
	 * seleted theme file is not exists
	 */
	public static final int THEME_LOAD_NO_FOUND_FILE = 0;
	/**
	 * seleted theme file is exists,but empty
	 */
	public static final int THEME_LOAD_THEME_FILE_IS_EMPTY = 1;

	/**
	 * theme loaded into theme manager success
	 */
	public static final int THEME_LOAD_SUCCESS = 2;

	public static final String KEY_FOR_APPLY_THEME_BUNDLE = "apply_theme_bundle";
	public static final String KEY_FOR_APPLY_THEME = "apply_theme";
	public static final String KEY_FOR_APPLY_THEME_PREVIEW = "apply_theme_preview";
	
	
	public static final long THEME_DEFAULT_ID = 0L;

	public static final int STATUS_FAILURE = -1;

	public static final int COPY_THEME_SUCCESS = 0;

	public static final int DELETE_THEME_SUCCESS = 1;

	public static final int MOVE_THEME_SUCCESS = 2;

	public static final int TARGET_THEME_NOT_FOUND = 3;

	public static final int COPY_THEME_TO_APPLY_DIR = 4;

	public static final int BACKUP_CURRENT_THEME = 5;

	public static final int DELETE_CURRENT_THEME = 6;

	public static final int STATUS_APPLY_SUCCESS = 7;

	public static final int STATUS_APPLY_FAUILER = 8;

	public static final int STATUS_THEME_IS_APPLY = 9;
	
	

}
