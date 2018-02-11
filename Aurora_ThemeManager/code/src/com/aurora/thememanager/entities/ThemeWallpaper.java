package com.aurora.thememanager.entities;

import com.aurora.thememanager.utils.ThemeConfig;

public class ThemeWallpaper extends Theme {
	public static final String PATH = ThemeConfig.THEME_BASE_PATH+"wallpaper/";
	
	/**
	 * apply to wallpaper
	 */
	public static final int WALL_PAPER = 0;
	/**
	 * apply to lock screen
	 */
	public static final int LOCK_SCREEN = 1;
	/**
	 * apply to wallpaper and lockscreen
	 */
	public static final int ALL = 2;
	
	public int scope = WALL_PAPER;
}
