package com.aurora.thememanager.utils;

import android.os.Environment;

public class ThemeConfig {

	public static final int TIME_WALLPAPER_UNUSED_ID = -1;
	
	public static final String DEFAULT_TYPEFACE_NAME = "default.ttf";
	public static final String DEFAULT_TYPEFACE_HOME = Environment.getDataDirectory().getAbsolutePath()+"/fonts/";
	
	public static final String IMAGE_LOAD_CACHE_DIR = "IUNI/theme/.cache";
	
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";


	public static final String APPLIED_THEME = "applied_theme";
	
	/**
	 * 主题应用后的跟路径
	 */
	
	public static final String THEME_BASE_PATH = "/data/theme/"; // chmod 755
																	// theme
	/**
	 * 当前主题存放的路径
	 */
	public static final String THEME_PATH = THEME_BASE_PATH + "current/";// chmod
																			// 755
																			// current

	/**
	 *已经应用的开机动画存放的路径
	 */
	public static final String THEME_BOOT_PATH = THEME_PATH + "boot/";

	/**
	 * 已经应用的字体的存放路径
	 */
	public static final String THEME_FONTS_PATH = THEME_PATH + "fonts/";

	/**
	 * 已经应用的主题包中的壁纸存放路径
	 */
	public static final String THEME_WALL_PAPAER_PATH = THEME_PATH
			+ "wallpaper/";

	/**
	 * 已经应用的主题的音效存放路径
	 */
	public static final String THEME_AUDIO_PATH = THEME_PATH + "audio/";
	
	/**
	 * 已经应用的主题的锁屏存放路劲
	 */
	public static final String THEME_LOCKSCREEN_PATH = THEME_PATH + "lockscreen/";

	/**
	 * 更改主题时备份上一个主题的路劲
	 */
	public static final String THEME_BACKUP_PATH = THEME_BASE_PATH + "backup/";// chmod
																				// 755

	/**
	 * 主题商店下载的主题包存放路径
	 */
	public static final String THEME_DOWNLLOAD_PATH = Environment.getExternalStorageDirectory()+"/IUNI/theme/download/theme_pkg/";
	
	public static final String THEME_WALL_PAPER_DOWNLLOAD_PATH =  Environment.getExternalStorageDirectory()+"/IUNI/theme/download/wallpaper/";
	
	public static final String THEME_TIME_WALLPAPER_DOWNLLOAD_PATH =  Environment.getExternalStorageDirectory()+"/IUNI/theme/download/time_wallpaper/";
	
	public static final String THEME_RINGTONG_DOWNLLOAD_PATH =  Environment.getExternalStorageDirectory()+"/IUNI/theme/download/ringtong/";

	
	public static final String THEME_CACHE_PATH = "/sdcard/IUNI/theme/.cache/";
	/**
	 * 桌面默认图标存放路劲
	 */
	public static final String THEME_ICONS_CACHE_PATH = "/data/aurora/icons/";

	/*
	 * 以下为手动导入主题包的配置信息
	 */
	/**
	 * 导入的主题包的信息的存放路径
	 */
	public static final String THEME_LOADED_PATH = "/sdcard/IUNI/theme/.data/";

	/**
	 * 导入的主题包的预览图路劲
	 */
	public static final String THEME_LOADED_PREVIEW = THEME_LOADED_PATH+"previews/";
	
	public static final String THEME_LOADED_AVATAR = THEME_LOADED_PATH+"avatar/";
	

	/**
	 * 导入的主题包信息存放路劲
	 */
	public static final String THEME_LOADED_INFO = THEME_LOADED_PATH+"info/";
	/**
	 * 导入的壁纸存放路劲
	 */
	public static final String THEME_LOADED_WALL_PAPER = THEME_LOADED_PATH+"wallpaper/";
	/**
	 * 导入的壁纸信息存放路劲
	 */
	public static final String THEME_LOADED_WALL_PAPER_INFO = THEME_LOADED_WALL_PAPER+"info/";
	/**
	 * 导入的壁纸预览图存放路劲
	 */
	public static final String THEME_LOADED_WALL_PAPER_PREVIEW = THEME_LOADED_WALL_PAPER+"preview/";
	
	
	/**
	 * 导入的字体存放路劲
	 */
	public static final String THEME_LOADED_FONTS = THEME_LOADED_PATH+"fonts/";
	/**
	 * 导入的字体信息存放路劲
	 */
	public static final String THEME_LOADED_FONTS_INFO = THEME_LOADED_FONTS+"info/";
	/**
	 * 导入的字体预览图存放路劲
	 */
	public static final String THEME_LOADED_FONTS_PREVIEW = THEME_LOADED_FONTS+"preview/";

	/*
	 * 主题的类型，在使用ThemeManager
	 */
	/**
	 * 字体类型
	 */
	public static final String THEME_FONTS = "fonts";
	/**
	 * 壁纸类型
	 */
	public static final String THEME_WALLPAPER = "wallpaper";
	/**
	 * 铃声类型
	 */
	public static final String THEME_RINGTONG = "ringtong";
	/**
	 * 主题包类型
	 */
	public static final String THEME_ALL = "all";
	/**
	 * 时光锁屏类型
	 */
	public static final String THEME_TIMES = "times";

	/*
	 * activity跳转时传递数据的KEY
	 */

	/**
	 * 跳转到主题应用页面时的key
	 */
	public static final String KEY_FOR_APPLY_THEME_BUNDLE = "apply_theme_bundle";
	public static final String KEY_FOR_APPLY_THEME = "apply_theme";
	public static final String KEY_FOR_APPLY_THEME_PREVIEW = "apply_theme_preview";
	public static final String KEY_FOR_APPLY_FROM_LOACAL = "from_local";
	
	public static final String KEY_PICK_THEME_FILE_PATH = "result_file_path";
	
	public static final int THEME_DEFAULT_ID = -1;
	
	/*
	 * 用于从其他app跳转到主题后，需要显示的页面的位置
	 */
	public  static final int SHOP_POSITION_TIME_WALL_PAPER = 2;
	
	public  static final int SHOP_POSITION_THEME = 0;
	
	public  static final int SHOP_POSITION_WALLPAPER = 1;
	
	public  static final int SHOP_POSITION_RINGTONG = 3;

	
	
	/**
	 * Status for theme
	 * @author alexluo
	 *
	 */
	public static class ThemeStatus{
		
		/*
		 * status for load theme into thememanager
		 */
		/**
		 * seleted theme file is not exists
		 */
		public static final int STATUS_THEME_LOAD_NO_FOUND_FILE = 0;
		/**
		 * seleted theme file is exists,but empty
		 */
		public static final int STATUS_THEME_LOAD_THEME_FILE_IS_EMPTY = 1;

		/**
		 * theme loaded into theme manager success
		 */
		public static final int STATUS_THEME_LOAD_SUCCESS = 2;
		
		public static final int STATUS_THEME_LOAD_IS_NOT_IUNI_THEME = 3;
		
		
		public static final int STATUS_FAILURE = -1;

		public static final int STATUS_COPY_THEME_SUCCESS = 0;

		public static final int STATUS_DELETE_THEME_SUCCESS = 1;

		public static final int STATUS_MOVE_THEME_SUCCESS = 2;

		public static final int STATUS_TARGET_THEME_NOT_FOUND = 3;

		public static final int STATUS_COPY_THEME_TO_APPLY_DIR = 4;

		public static final int STATUS_BACKUP_CURRENT_THEME = 5;

		public static final int STATUS_DELETE_CURRENT_THEME = 6;

		public static final int STATUS_APPLY_SUCCESS = 7;

		public static final int STATUS_APPLY_FAUILER = 8;

		public static final int STATUS_THEME_IS_APPLY = 9;
		
		public static final int STATUS_DELETE_SUCCESS = 10;
		
		public static final int STATUS_DELETE_FAUILER = 11;
	}
	

	
	
	
	
	
	
	
	
	/**
	 * Config for http
	 * @author alexluo
	 *
	 */
	public static class  HttpConfig{
		
		public static final String HOST_PATH = "http://dev.theme.iunios.com/";//"http://18.8.0.84:8080/theme/theme.json";
		
		/**
		 * 主题包请求的主机地址
		 */
		public static final String THEME_PACKAGE_REQUEST_URL = HOST_PATH+"theme/list";
		
		/**
		 * 壁纸请求的主机地址
		 */
		public static final String THEME_WALLPAPER_REQUEST_URL = HOST_PATH+"wallpaper/list";
		/**
		 * 时光锁屏请求的主机地址
		 */
		public static final String THEME_TIME_WALLPAPER_REQUEST_URL = HOST_PATH+"lockscreen/list";
		
		/**
		 * 时光锁屏详情请求的主机地址
		 */
		public static final String THEME_TIME_WALLPAPER_DETAIL_REQUEST_URL = HOST_PATH+"lockscreen/detail";
		
		/**
		 * 铃声请求的主机地址
		 */
		public static final String THEME_RINTONG_REQUEST_URL = HOST_PATH+"ring/list";
		
		/**
		 * Theme internet config
		 */
		
		public static final String HTTP_CACHE_BASE="/sdcard/IUNI/theme/";
		public static final String HTTP_REQUEST_CACHE="image_cache";
		public static final String HTTP_IMAGE_CACHE="internet_cache";
		
		public static final  int DISKCACHE_SIZE = 50 * 1024 * 1024; // 50MB
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
