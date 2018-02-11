package com.aurora.thememanager.utils.themehelper;

import java.io.File;

import android.text.TextUtils;
import android.util.Log;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;

public class ThemeApplyUtils {
	
	private ThemeTask mTask;
	private Object mLock = new Object();
	
	public ThemeApplyUtils(){
		mTask = new ThemeTask();
	}
	
	
	public void setCallBack(ThemeOperationCallBack callback){
		mTask.setCallBack(callback);
	}
	
	
	public void applyTheme(Theme theme){
		 if(theme == null ){
			 if(mTask.getCallback() != null){
				 mTask.getCallback().onCompleted(false, ThemeConfig.ThemeStatus.STATUS_TARGET_THEME_NOT_FOUND);
			 }
			 return;
		 }
		 Theme currentTheme = new Theme();
			currentTheme.usedPath = ThemeConfig.THEME_PATH;
		 if(theme.themeId == ThemeConfig.THEME_DEFAULT_ID){
			 applyTheme(currentTheme,theme);
			 return;
		 }
		 
		   String themePath = theme.fileDir+File.separatorChar+theme.fileName;
		   File themeFile = new File(themePath);
		   if(TextUtils.isEmpty(themePath) || !themeFile.exists()){
			   if(mTask.getCallback() != null){
					 mTask.getCallback().onCompleted(false, ThemeConfig.ThemeStatus.STATUS_TARGET_THEME_NOT_FOUND);
				 }
			   return;
		   }
			applyTheme(currentTheme,theme);
	}
	
	private void applyTheme(Theme...themes){
		synchronized (mLock) {
				mTask.execute(ThemeTask.MSG_APPLY_THEME,themes);
		}
	}
	
	
	
	public  boolean applyWallpaper(Theme theme){
		synchronized (mLock) {
			mTask.executeSingleTheme(ThemeTask.MSG_APPLY_WALLPAPER,theme);
	}
		return false;
	}
	
	
	public  boolean applyLockScreen(Theme theme){
		synchronized (mLock) {
			mTask.executeSingleTheme(ThemeTask.MSG_APPLY_LOCKSCREEN,theme);
		}
		return false;
	}
	
	
	public boolean applyFonts(Theme theme){ 
		synchronized (mLock) {
			mTask.executeSingleTheme(ThemeTask.MSG_APPLY_FONTS,theme);
	}
		return false;
	}
	
	
	public boolean applyRingtong(Theme theme){
		synchronized (mLock) {
			Log.d("ap", "applyTimeWallpaper:"+android.os.Debug.getCallers(3));
			mTask.executeSingleTheme(ThemeTask.MSG_APPLY_RINGTONG,theme);
	}
		return false;
	}


	public void applyTimeWallpaper(Theme theme) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			Log.d("ap", "applyTimeWallpaper:"+ThemeTask.MSG_APPLY_LOCKSCREEN);
			mTask.executeSingleTheme(ThemeTask.MSG_APPLY_LOCKSCREEN,theme);
		}
	}


	public void deleteTheme(Theme[] theme) {
		// TODO Auto-generated method stub
		mTask.executeDeleteThemes(theme);
	}
	
	
	
	
	
	

}
