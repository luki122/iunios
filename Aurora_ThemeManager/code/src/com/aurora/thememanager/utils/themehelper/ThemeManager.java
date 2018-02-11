package com.aurora.thememanager.utils.themehelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AuroraConfiguration;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DatabaseController;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.themehelper.ThemeOperator;
/**
 * global instance of ThemeManager,if you want to apply theme to
 * the system,you will be write codes like:
 * <pre> {@code
 * 
 * Theme theme = getTheme();
 * ThemeManager mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_RINGTONG);
 * mThemeManager.setCallBack(new ThemeOperationCallBack() {
			
			@Override
			public void onProgressUpdate(int progress) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(boolean success, int statusCode) {
				// TODO Auto-generated method stub
				 //if theme is theme zip package,invoke this block
				mThemeManager.updateThemeConfiguration(theme.themeId);
			}
			
			@Override
			public Context getContext() {
				// TODO Auto-generated method stub
				return mContext;
			}
		};)
 * mThemeManager.apply(theme);
 * 
 * 
 * 
 * 
 * }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author alexluo
 *
 */
public class ThemeManager{

	private HashMap<String,WeakReference<Class>> mThemeHelpClzMap = new HashMap<String, WeakReference<Class>>();
	
	private static  ThemeManager mInstance;
	
	
	private ThemeOperator mHelper;
	
	
	private ThemeManager(){
	}
	
	
	public static ThemeManager getInstance(String themeType){
		ThemeManager tm = new ThemeManager();
		tm.createThemeHelper(themeType);
		return tm;
		
	}
	
	
	/**
	 * create theme helper by given type
	 * @param themeType
	 */
	private void createThemeHelper(String themeType){
		String clzName = ThemeManagerApplication.mThemeHelperMap.get(themeType);
		if(!TextUtils.isEmpty(clzName)){
			Class<?> clz = null;
			if(mThemeHelpClzMap.containsKey(themeType)){
				clz = mThemeHelpClzMap.get(themeType).get();
			} else {
				try {
					 clz = Class.forName(clzName);
					 if(clz != null){
						 mHelper = (ThemeOperator) clz.newInstance();
						 mThemeHelpClzMap.put(themeType, new WeakReference<Class>(clz));
					 }
				} catch (Exception ex) {

				}
			}
		}else{
			throw new IllegalArgumentException("type "+themeType+"  was not found !");
		}
	}
	
	/**
	 * set theme apply callback to themeManager,it can return apply status to invoker
	 * @param callback
	 */
	public void setCallBack(ThemeOperationCallBack callback){
		
		mHelper.setCallBack(callback);
	}
	
	/**
	 * apply theme to system
	 * @param theme
	 * @return
	 */
	public void apply(Theme theme) {
		// TODO Auto-generated method stub
		 mHelper.apply(theme);
	}
	
	/**
	 * udpate system configuration to reload resources
	 * 
	 * @param themeid
	 */
	public static void updateThemeConfiguration(int themeid){
		
			AuroraConfiguration.updateThemeConfiguration(themeid);
	}
	
	
	public void deleteTheme(Theme theme,Context context){
		int type = theme.type;
		DatabaseController dbc = null;
		switch (type) {
		case Theme.TYPE_RINGTONG:
			
			break;
		case Theme.TYPE_WALLPAPER:
			
			break;
		case Theme.TYPE_THEME_PKG:
			deleteThemePkgFile(theme, context);
			break;

		default:
			break;
		}
		
	}
	
	/**
	 * 删除主题包
	 * @param theme
	 * @param context
	 */
	private void deleteThemePkgFile(Theme theme,Context context){
		int themeId = theme.downloadId;
		DatabaseController dbc = DatabaseController.getController(context, DatabaseController.TYPE_DOWNLOAD);
		if(dbc != null){
			dbc.openDatabase();
			DownloadData needDeleteTheme = dbc.getDownloadData(themeId);
			if(needDeleteTheme != null){
				String pkgPath = needDeleteTheme.fileDir+File.separatorChar+needDeleteTheme.fileName;
				File file = new File(pkgPath);
				if(file.exists()){
					file.delete();
					dbc.delete(themeId);
				}
			}
		}
	}
	
	
	public void deleteTheme(Theme... theme){
		mHelper.deleteTheme(theme);
	}
	
	public void deleteTheme(List<Theme> themes){
		int size = themes.size();
		if(size > 0){
			Theme[] th = new Theme[themes.size()];
			for(int i = 0; i < size ;i++){
				th[i] = themes.get(i);
			}
			deleteTheme(th);
		}
		
		
	}
	
	
	public static void restartApplications(final Context context,final int themeId){
		final ThemeManagerApplication app = (ThemeManagerApplication) ((Activity)context).getApplication();
		
		final Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				am.forceStopPackage("com.aurora.launcher");
			}
		});
		
		final Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					t1.join();
					Intent intent = new Intent(Action.ACTION_THEME_CHANGED);
					intent.putExtra(Action.KEY_THEME_CHANGED_ID, themeId);
					context.sendBroadcast(intent);
					long start = System.currentTimeMillis();
					android.content.res.AuroraConfiguration.updateThemeConfiguration(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			
			}
		});
		final Thread t3 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					//引用t2线程，等待t2线程执行完
					t2.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				app.finishThemeManager();
			}
		});
		t3.start();
		t2.start();
		t1.start();
		
		
	}
	
	
	/**
	 * 重启桌面
	 */
	public static void restartLauncher(Context context){
		ThemeManagerApplication app = (ThemeManagerApplication) ((Activity)context).getApplication();
		app.finishThemeManager();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		am.forceStopPackage("com.aurora.launcher");
		
	}
	
	/**
	 * 设置已应用时光锁屏到数据库中
	 * @param data
	 * @param context
	 */
	public static  void setTimeWallpaperApplied(DownloadData data,Context context){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER);
		pm.saveInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER, data.downloadId);
		
		DatabaseController db = DatabaseController.getController(context, DatabaseController.TYPE_TIME_WALLPAPER_DOWNLOAD);
		if(db != null){
			db.openDatabase();
			List<DownloadData> appliedDatas = db.getAppliedDatas();
			if(appliedDatas != null && appliedDatas.size()>0){
				for(DownloadData d:appliedDatas){
					if(d.downloadId == data.downloadId){
						continue;
					}
					db.setUnAppApplied(d.downloadId);
				}
			}
			db.setAppApplied(data.downloadId);
		}
	}

	/**
	 * 设置所有已应用时光锁屏为不可用
	 * @param data
	 * @param context
	 */
	public static void setTimeWallpaperUnApplied(Context context ){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER);
		pm.saveInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER, ThemeConfig.TIME_WALLPAPER_UNUSED_ID);
		
		DatabaseController db = DatabaseController.getController(context, DatabaseController.TYPE_TIME_WALLPAPER_DOWNLOAD);
		if(db != null){
			db.openDatabase();
			List<DownloadData> appliedDatas = db.getAppliedDatas();
			if(appliedDatas != null && appliedDatas.size()>0){
				for(DownloadData d:appliedDatas){
					db.setUnAppApplied(d.downloadId);
				}
			}
		}
	}
	
	public static void setTimeWallpaperUnAppliedById(Context context,int downloadId ){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER);
		pm.saveInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER, ThemeConfig.TIME_WALLPAPER_UNUSED_ID);
		
		DatabaseController db = DatabaseController.getController(context, DatabaseController.TYPE_TIME_WALLPAPER_DOWNLOAD);
		if(db != null){
			db.openDatabase();
			db.setUnAppApplied(downloadId);
		}
	}
	
	
	/**
	 * 设置已应用主题到数据库中
	 * @param data
	 * @param context
	 */
	public void setThemePackageAplied(Theme data,Context context ){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_THEME);
		pm.saveInt(PreferenceManager.KEY_CURRENT_THEME, data.themeId);
		
		DatabaseController db = DatabaseController.getController(context, DatabaseController.TYPE_DOWNLOAD);
		if(db != null){
			db.openDatabase();
			List<DownloadData> appliedDatas = db.getAppliedDatas();
			if(appliedDatas != null && appliedDatas.size()>0){
				for(DownloadData d:appliedDatas){
					if(d.downloadId == data.downloadId){
						continue;
					}
					db.setUnAppApplied(d.downloadId);
				}
			}
			db.setAppApplied(data.downloadId);
		}
	}
	
	
	
	public int getAppliedTimeWallpaperd(Context context){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_TIME_WALLPAPER);
		return usedId;
	}
	
	public int getAppliedThemeId(Context context){
		PreferenceManager pm = PreferenceManager.getInstance(context);
		int usedId = pm.getInt(PreferenceManager.KEY_CURRENT_THEME);
		return usedId;
	}
	
	

}
