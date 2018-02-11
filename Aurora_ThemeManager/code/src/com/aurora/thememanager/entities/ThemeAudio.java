package com.aurora.thememanager.entities;

import com.aurora.thememanager.utils.ThemeConfig;

public class ThemeAudio extends Theme {
	
	
	public static final String PATH = ThemeConfig.THEME_BASE_PATH+"audio/";
	
	public static final String PATH_RING_TONG = PATH+"ringtong/";
	
	public static final String PATH_FEELBACK = PATH+"feelback/";
	
	/**
	 * ringtong for phone call
	 */
	public static final int RINGTONE = 0;
	/**
	 * audio for notification
	 */
	public static final int NOTIFICATION = 1;
	
	/**
	 * audio for alarm
	 */
	public static final int ALARM = 2;
	
	/**
	 * audio for all but not feelback
	 */
	public static final int RINGTONG_ALL = 3;
	
	/**
	 * audio for feelback
	 */
	public static final int FEELBACK = 4;

	/**
	 * type for ringtong
	 */
	public int ringtongType = -1;
	
	
	
}
