package com.aurora.thememanager.utils.themehelper;

import java.io.File;

import android.content.res.AuroraConfiguration;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;

public abstract class ThemeHelper implements ThemeOperator {

	/**
	 * utils for apply theme to system
	 */
	protected ThemeApplyUtils mApplyUtils;

	public ThemeHelper() {
		mApplyUtils = new ThemeApplyUtils();
	}

	@Override
	public void setCallBack(ThemeOperationCallBack callback) {
		mApplyUtils.setCallBack(callback);
	}



	/**
	 * new theme is default or not
	 * @param theme
	 * @return
	 */
	public boolean isDefaultTheme(Theme theme) {
		return theme.themeId == ThemeConfig.THEME_DEFAULT_ID;
	}
	
	/**
	 * apply new theme to system
	 * @param theme
	 */
	public void applyTheme(Theme theme) {
		 mApplyUtils.applyTheme(theme);
	}
	

	/**
	 * set wallpaper to launcher
	 * @param theme
	 * @return
	 */
	public void applyWallpaper(Theme theme) {

		 mApplyUtils.applyWallpaper(theme);
	}

	/**
	 * set wallpaper to keyguard screen
	 * @param theme
	 * @return
	 */
	public void applyLockScreen(Theme theme) {

		 mApplyUtils.applyLockScreen(theme);
	}

	/**
	 * apply fonts to system
	 * @param theme
	 * @return
	 */
	public void applyFonts(Theme theme) {

		 mApplyUtils.applyFonts(theme);
	}

	/**
	 * set new ringtong for mms and phone
	 * @param theme
	 * @return
	 */
	public void applyRingtong(Theme theme) {
		 mApplyUtils.applyRingtong(theme);
	}
	
	public void applyTimeWallpaper(Theme theme){
		mApplyUtils.applyTimeWallpaper(theme);
	}

	/**
	 * get current avaliable theme
	 * @return
	 */
	public abstract Theme getCurrentTheme();

	@Override
	public void deleteTheme(Theme... theme){
		mApplyUtils.deleteTheme(theme);
	}
	
}
