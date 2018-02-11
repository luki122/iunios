package com.android.settings.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;


public class ThemeShutDownReceiver extends BroadcastReceiver{
	 public static final String TAG = "ThemeSettings";
	 public static final String THEME_DB = "iuni_theme";
	 
	 public static final String Properties = "persist.sys.aurora.overlay";
	 public static final String SYSTEM_THEME_WOMEN = "/system/theme/woman/";
	 public static final String SYSTEM_THEME_KITTY = "/system/theme/kity/";
	 
	 public static final String SCHEMA = "file://";
	 public static final String DEFAULT_WALLPAPER_NAME = "wallpaper_01.jpg";
	 public static final String SYSTEM_NORMAL_DESKTOP = "/system/iuni/aurora/change/desktop/normal";
	 public static final String SYSTEM_WOMAN_DESKTOP = "/system/iuni/aurora/change/desktop/woman";
	 public static final String CLEAR_FILE_PATH = "/data/aurora/icons";
	
	 private String mThemeProp;
	 private String mThemeDb;
	 
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")){
			mThemeProp = SystemProperties.get(Properties,SYSTEM_THEME_KITTY);
			mThemeDb = Settings.System.getString(context.getContentResolver(), THEME_DB);
			if(mThemeProp == null ||mThemeDb == null || mThemeDb.isEmpty()){
				return;
			}
			Log.v(TAG, "-------onReceive------mThemeProp---=="+mThemeProp);
			Log.v(TAG, "-------onReceive------mThemeDb---=="+mThemeDb);
			
			if(!mThemeProp.equals(mThemeDb)){
				SystemProperties.set(Properties,mThemeDb);
				setWallpaperAndLockScreen(context);
				cleanIcons(new File(CLEAR_FILE_PATH));
			}
			
		}
		
	}

	public void setWallpaperAndLockScreen(Context context){
		Log.v(TAG, "--onReceive----setWallpaperAndLockScreen------");
		String themeExtra = SYSTEM_THEME_KITTY;
		WallpaperManager wpm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
		try{
			String url = SCHEMA+"/system/iuni/aurora/change/desktop/"+DEFAULT_WALLPAPER_NAME;
			if(mThemeDb.equals(SYSTEM_THEME_WOMEN)){
				url = SCHEMA + SYSTEM_WOMAN_DESKTOP + DEFAULT_WALLPAPER_NAME;
				themeExtra = SYSTEM_THEME_WOMEN;
			}else if(mThemeDb.equals(SYSTEM_THEME_KITTY)){
				url = SCHEMA + SYSTEM_NORMAL_DESKTOP + DEFAULT_WALLPAPER_NAME;
				themeExtra = SYSTEM_THEME_KITTY;
			}
			Log.v(TAG, "--onReceive----setWallpaperAndLockScreen-----url-----===="+url);
			InputStream ins = new URL(url).openStream();
			wpm.setStream(ins);
		}catch(IOException e){
			Log.v(TAG, "--onReceive----setWallpaperAndLockScreen----IOException--");
			e.printStackTrace();
		}
		
		Intent intent  = new Intent("com.aurora.loackscreen.wallpaperchanger");
		intent.putExtra("theme", themeExtra);
		context.sendBroadcast(intent);
	}
	
	public void cleanIcons(File file){
		try {
			if(file.isFile()){
	            file.delete();
	            return;
	        }
			
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
	            if(childFile == null || childFile.length == 0){
	                file.delete();
	                return;
	            }
	            for(File f : childFile){
	            	cleanIcons(f);
	            }
	            
	            Log.v(TAG, "---onReceive--cleanIcons---");
	            file.delete();
			}
		} catch (Exception e) {
			Log.v(TAG, "---onReceive----cleanIcons----Exception-");
			e.printStackTrace();
		}
	}
	
}