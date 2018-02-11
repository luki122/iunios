package com.aurora.thememanager.utils.themehelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.aurora.change.data.DbControl;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.thememanager.db.LoadedThemeDao;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.entities.ThemeWallpaper;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.Worker;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.mediatek.audioprofile.AudioProfileManager;

import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 主题应用的主要任务类
 * @author alexluo
 *
 */
public class ThemeTask {

	private static final String TAG = "ThemeTask";

	public static final int MSG_APPLY_THEME = 0x01;

	public static final int MSG_APPLY_WALLPAPER = 0x02;

	public static final int MSG_APPLY_FONTS = 0x03;

	public static final int MSG_APPLY_RINGTONG = 0x04;

	public static final int MSG_APPLY_LOCKSCREEN = 0x05;
	
	public static final int MSG_DELETE_THEME = 0x06;

	private static final int INDEX_CURRENT_THEME = 0;

	private static final int INDEX_NEW_THEME = 1;

	private static final boolean DBG = true;

	private static final String 	DENSITY_720 = "720";
	
	private static final String 	DENSITY_1080 = "1080";
	
	private boolean mIsRunning = false;

	private ThemeOperationCallBack mCallBack;

	private TaskHandler mHandler;

	private Worker mWorker;

	private ExecutorService mThreadPool;

	private long mProgress = 0L;

	private Theme[] mThemes = new Theme[2];

	private Theme mSingleTheme;
	
	private  AudioProfileManager mProfileManager;

	
	public ThemeTask() {
		mThreadPool = Executors.newCachedThreadPool();
		mWorker = new Worker(TAG);
		mHandler = new TaskHandler(mWorker.getLooper());

	}

	/**
	 * 设置主题应用回调
	 * @param callback
	 */
	public void setCallBack(ThemeOperationCallBack callback) {
		mCallBack = callback;
	}

	public ThemeOperationCallBack getCallback() {
		return mCallBack;
	}

	/**
	 * apply theme that installed from theme zip package
	 * 
	 * @param msg
	 * @param themes
	 */
	public void execute(int msg, Theme... themes) {
		mThemes[INDEX_CURRENT_THEME] = themes[0];
		mThemes[INDEX_NEW_THEME] = themes[1];
		Log.d("ThemeTask", "execute.000:");
		mHandler.sendEmptyMessage(msg);

	}

	/**
	 * apply theme like fonts,ringtong and wallpaper
	 * 
	 * @param msg
	 * @param theme
	 */
	public void executeSingleTheme(int msg, Theme theme) {
		mSingleTheme = theme;
		Log.d("ap", "applyTimeWallpape00000r:"+android.os.Debug.getCallers(3));
		mHandler.sendEmptyMessage(msg);
	}

	public void executeDeleteThemes(Theme... theme){
		Message msg = new Message();
		msg.what = MSG_DELETE_THEME;
		msg.obj = theme;
		mHandler.sendMessage(msg);
	}
	
	
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		mProgress = 0L;
	}

	/**
	 * 获取主题名字
	 * @param themePath
	 * @return
	 */
	private String getThemeName(String themePath) {
		String[] splits = themePath.split("/");
		String name = splits[splits.length - 1];
		int pointIndex = name.indexOf(".");
		return name.substring(0, pointIndex);
	}
	/**
	 * 执行应用功能
	 * @param themes
	 * @return
	 */
	protected Boolean doInBackground(Theme... themes) {

		Theme currentTheme = themes[INDEX_CURRENT_THEME];
		Theme newTheme = themes[INDEX_NEW_THEME];
		boolean backup =/* FileUtils.backupCurrentTheme(currentTheme)*/true;
		boolean apply = false;
		if (backup) {
			apply = FileUtils.deleteTheme(currentTheme);
			if (newTheme.themeId != ThemeConfig.THEME_DEFAULT_ID) {
				apply = applyTheme(newTheme);
			}else{
				 realApplyWallpaper(newTheme);
				 apply = true;
			}
		}
		mIsRunning = false;
		if(backup && apply){
			FileUtils.changeThemePermission();
		}
		return backup && apply;
	}

	/**
	 * copy new theme files to apply directory
	 * 
	 * @param themes
	 * @return
	 */
	private boolean applyTheme(Theme themes) {
		Theme theme = themes;
		try {
			String themePath = theme.fileDir+File.separatorChar+theme.fileName;
			Log.d(TAG, "theme:"+themePath);
			File srcTheme = new File(themePath);
			File targetTheme = new File(ThemeConfig.THEME_PATH);
			String themeName = theme.fileName.substring(0, theme.fileName.lastIndexOf("."));
			apply(srcTheme, targetTheme,themeName);
			applyWallpaper(theme);
			applyThemePkgLockScreen(theme);
			return true;
		} catch (Exception ex) {
			Log.d(TAG, "apply theme catched exception-->" + ex);
		}
		return false;
	}

	private void applyThemePkgRingTong(Theme theme) {
		// TODO Auto-generated method stub
		
	}

	protected void onProgressUpdate(long progress) {
		// TODO Auto-generated method stub
		if (mCallBack != null) {
			mCallBack.onProgressUpdate((int) progress);
		}
	}

	private void publishProgress(long progress) {
		onProgressUpdate(progress);
	}

	protected void onPostExecute(boolean success, int statusCode) {
		// TODO Auto-generated method stub
		if (mCallBack != null) {
			mCallBack.onCompleted(success, statusCode);
		}
	}

	class TaskHandler extends Handler {

		public TaskHandler() {
			// TODO Auto-generated constructor stub
		}

		public TaskHandler(Looper looper) {
			// TODO Auto-generated constructor stub
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_DELETE_THEME){
				Theme[] themes = (Theme[]) msg.obj;
				mThreadPool.execute(new DeleteThemeThread(themes));
			}else{
				if (!mIsRunning) {
					onPreExecute();
					mIsRunning = true;
					mThreadPool.execute(new ApplyThemeThread(msg.what));
				} else {
					onPostExecute(false, ThemeConfig.ThemeStatus.STATUS_THEME_IS_APPLY);
				}
			}
	
		}

	}

	public void applyDefaultTheme(Theme theme){
		mThreadPool.execute(new DeleteThemeThread(theme));
	}
	
	/**
	 * 应用壁纸
	 * @param theme
	 */
	private void realApplyWallpaper(Theme theme) {
		if (mCallBack != null) {
			mIsRunning = false;
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(mCallBack.getContext());
			ArrayList<Bitmap> wallpapers = getWallpapers(theme);
			if(wallpapers.size() > 0){
				Log.d(TAG, "hasWallpaper:"+wallpapers.size());
				try {
					wallpaperManager.setBitmap(wallpapers.get(0));
					if(mCallBack != null){
						if(theme.type != Theme.TYPE_THEME_PKG){
								mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "apply wallpaper catched exception-->"+e);
					if(mCallBack != null){
						mCallBack.onCompleted(false, ThemeConfig.ThemeStatus.STATUS_TARGET_THEME_NOT_FOUND);
					}
				}finally{
					wallpapers.get(0).recycle();
				}
			}
		}
	}
	
	/**
	 * 获取主题中的壁纸
	 * @param theme
	 * @return
	 */
	private ArrayList<Bitmap> getWallpapers(Theme theme){
		ArrayList<Bitmap> maps = new ArrayList<Bitmap>();
		String wallpaperDir;
		/*
		 * 如果是单张壁纸的类型，就直接返回对应的壁纸
		 */
		if(theme.type == Theme.TYPE_WALLPAPER){
			String themePath = theme.fileDir+File.separatorChar+theme.fileName;
			Log.d(TAG, "path:"+themePath);
			File themeFile = new File(themePath);
			if(themeFile.exists()){
				InputStream stream = null;
				try {
					stream = new FileInputStream(themeFile);
					if (stream != null) {
						Bitmap bitmap = BitmapFactory.decodeStream(stream);
						maps.add(bitmap);
					}
					if(stream != null) {
						stream.close();
					}
					stream = null;
					
				}catch(Exception e){
					if(mCallBack != null){
						mCallBack.onCompleted(false, ThemeConfig.ThemeStatus.STATUS_TARGET_THEME_NOT_FOUND);
					}
				}
			}else{
				if(mCallBack != null){
					mCallBack.onCompleted(false, ThemeConfig.ThemeStatus.STATUS_TARGET_THEME_NOT_FOUND);
				}
			}
			return maps;
		}
		
		if(theme.themeId == ThemeConfig.THEME_DEFAULT_ID){
			Context context = mCallBack.getContext();
			String defaultWallpaperName = DENSITY_1080;
			if(context != null){
				int displaySize = context.getResources().getDisplayMetrics().densityDpi;
				if(displaySize <  DisplayMetrics.DENSITY_XXHIGH){
					defaultWallpaperName = DENSITY_720;
				}
			}
			String defaultWallpaperPath = ThemeConfig.THEME_DEFAULT_WALLPAPER_PATH+defaultWallpaperName+".jpg";

			if(context != null){
				InputStream stream = null;
				try {
				stream = context.getResources().getAssets().open(defaultWallpaperPath) ;
						if (stream != null) {
							Bitmap bitmap = BitmapFactory.decodeStream(stream);
							maps.add(bitmap);
						}
				}catch(Exception e){
					mCallBack.onCompleted(false, 0);
				}finally{
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				}
			return maps;
		}
		
	    wallpaperDir = ThemeConfig.THEME_WALL_PAPAER_PATH;
		File dir = new File(wallpaperDir);
		if (dir.exists() && dir.list() != null) {
			File[] children = dir.listFiles();
			for (File wallpaper : children) {
				InputStream stream = null;
				try {
					Context context = mCallBack.getContext();
					if(context != null){
						int displaySize = context.getResources().getDisplayMetrics().densityDpi;
						String defaultWallpaperName = DENSITY_720;
						if(displaySize == DisplayMetrics.DENSITY_XXHIGH){
							defaultWallpaperName = DENSITY_1080;
						}
						String wallpaperName = wallpaper.getAbsolutePath();
						if(!wallpaperName.contains(defaultWallpaperName)){
							continue;
						}
					}
					
					stream = new FileInputStream(wallpaper);
					if (stream != null) {
						Bitmap bitmap = BitmapFactory.decodeStream(stream);
						maps.add(bitmap);
					}

				} catch (Exception ex) {
					mCallBack.onCompleted(false, 0);
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
			return maps;
	}
	
	
	
	private void realApplyWallpaperToLockScreen(Theme theme){
		
	}

	/**
	 * 应用壁纸
	 * @param theme
	 */
	private void applyWallpaper(Theme theme){
		int scope = ThemeWallpaper.WALL_PAPER;
		if(theme != null && theme.type == Theme.TYPE_TIME_WALLPAPER){
			ThemeWallpaper wallpaper = (ThemeWallpaper)theme;
			scope = wallpaper.scope;
			theme = wallpaper;
		}
		switch (scope) {
		/*
		 * 同时应用到桌面和锁屏
		 */
		case ThemeWallpaper.ALL:
			break;
			/*
			 * 应用到锁屏
			 */
		case ThemeWallpaper.LOCK_SCREEN:
			
			break;
			/*
			 * 应用到桌面壁纸
			 */
		case ThemeWallpaper.WALL_PAPER:
			realApplyWallpaper(theme);
			break;
			

		default:
			break;
		}
	}
	
	/**
	 * 应用主题包中的时光锁屏
	 * @param theme
	 * @return
	 */
	private boolean applyThemePkgLockScreen(Theme theme){
		Context context = getContext();
		LockScreenApplyHelper helper = new LockScreenApplyHelper(context,theme,true);
		
	
		
		int displaySize = context.getResources().getDisplayMetrics().densityDpi;
		String defaultWallpaperName = DENSITY_720;
		if(displaySize == DisplayMetrics.DENSITY_XXHIGH){
			defaultWallpaperName = DENSITY_1080;
		}
		String path = ThemeConfig.THEME_LOCKSCREEN_PATH+defaultWallpaperName;
		File dir = new File(path);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File[] child = dir.listFiles();
		ArrayList<InputStream> images = new ArrayList<InputStream>();
		if(child != null && child.length > 0){
			for(File f:child){
				try{
				FileInputStream input = new FileInputStream(f);
				images.add(input);
				}catch(Exception e){
					
				}
			}
		}
		ThemeManager.setTimeWallpaperUnApplied(getContext());
		deleteUsedLockScreenInThemePkg(context);
		return helper.applyTimeWallpaper(images);
	}
	
	/**
	 * 主题切换时删除已经使用的主题包中的时光锁屏
	 * @param context
	 */
	private void deleteUsedLockScreenInThemePkg(Context context){
		DbControl db = new DbControl(context);
		if(db != null){
			 List<PictureGroupInfo> group  = db.queryAllGroupInfos();
			 if(group != null && group.size() > 0){
				 for(PictureGroupInfo g:group){
					 if( g.fromThemePkg == 1){
						 String name = g.getDisplay_name();
						 String path = Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH + name;
						  FileHelper.deleteDirectory(path);
						  db.delPictureGroupByName(name);
				          db.close();
					 }
				 }
			 }
		}
	}
	
	
	/**
	 * 应用时光锁屏
	 * @param theme
	 * @return
	 */
	private boolean applyLockScreen(Theme theme) {

		DbControl dbc = new DbControl(getContext());
		dbc.openDb(getContext());
		PictureGroupInfo picGroup = dbc.queryGroupByDownloadId(theme.downloadId);
		if(picGroup != null){
			
			String groupName = picGroup.getDisplay_name();
			if(!TextUtils.isEmpty(groupName)){
				LockScreenApplyHelper.applyTimeWallpaperExists(getContext(), groupName);
				return true;
			}
		}
		LockScreenApplyHelper helper = new LockScreenApplyHelper(getContext(),theme);
		
		ArrayList<InputStream> images = new ArrayList<InputStream>();
		String themeFile = theme.fileDir+File.separatorChar+theme.fileName;
		File themePkgFile = new File(themeFile);
		if(!themePkgFile.exists()){
			return false;
		}
		
		try {
			TimeWallpaperZip zip = new TimeWallpaperZip(themePkgFile);
			if(zip.getSize() <= 0){
				return false;
			}
			
			List<ZipEntry> entries = zip.getEntries();
			
			if(entries.size() > 0){
				for(ZipEntry entry:entries){
					InputStream input = zip.getInputStream(entry);
					if(input != null){
						images.add(input);
					}
				}
			}else{
				return false;
			}
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return helper.applyTimeWallpaper(images);
	}

	public boolean unzipTimeWallpaper(Theme theme){

		DbControl dbc = new DbControl(getContext());
		dbc.openDb(getContext());
		PictureGroupInfo picGroup = dbc.queryGroupByDownloadId(theme.downloadId);
		if(picGroup != null){
			
			String groupName = picGroup.getDisplay_name();
			if(!TextUtils.isEmpty(groupName)){
				LockScreenApplyHelper.applyTimeWallpaperExists(getContext(), groupName);
				return true;
			}
		}
		LockScreenApplyHelper helper = new LockScreenApplyHelper(getContext(),theme);
		
		ArrayList<InputStream> images = new ArrayList<InputStream>();
		String themeFile = theme.fileDir+File.separatorChar+theme.fileName;
		File themePkgFile = new File(themeFile);
		if(!themePkgFile.exists()){
			return false;
		}
		
		try {
			TimeWallpaperZip zip = new TimeWallpaperZip(themePkgFile);
			if(zip.getSize() <= 0){
				return false;
			}
			
			List<ZipEntry> entries = zip.getEntries();
			
			if(entries.size() > 0){
				for(ZipEntry entry:entries){
					InputStream input = zip.getInputStream(entry);
					if(input != null){
						images.add(input);
					}
				}
			}else{
				return false;
			}
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return helper.applyTimeWallpaper(images);
	}
	
	
	
	private void applyFonts() {

	}

	/**
	 * 应用铃声
	 * @param theme
	 * @param ringtongType  
	 * 					铃声类型：通知、来电铃声、触摸反馈、闹钟等
	 */
	private void applyRingTong(Theme theme, int ringtongType) {
		String themePath = theme.fileDir+"/"+theme.fileName;
		Log.e("101010", "--applyRingTong themePath = ---" + themePath);
		Log.e("101010", "--applyRingTong ringtongType = ---" + ringtongType);
		switch (ringtongType) {
		case ThemeAudio.NOTIFICATION:
			setNotification(themePath, getContext());
			break;
		case ThemeAudio.MESSAGE:
			setMessage(themePath, getContext());
			//还原
			((ThemeAudio)theme).ringtongType = ThemeAudio.NOTIFICATION;
			break;
		case ThemeAudio.ALARM:
			setAlarm(themePath, getContext());
			break;
		case ThemeAudio.RINGTONE:
			setRingtone(themePath, getContext());
			break;
		case ThemeAudio.MESSAGE_AND_NOTIFICATION:
			setNotificationAndMessage(themePath, getContext());
			//还原
			((ThemeAudio)theme).ringtongType = ThemeAudio.NOTIFICATION;
			break;
		case ThemeAudio.FEELBACK:
			setFeelBack(themePath, getContext());
			break;

		default:
			break;
		}
		mIsRunning = false;
	}
	
	private void setFeelBack(String path,Context context){
		
	}
	
	private void setRingtone(String path, Context context) {

		Cursor cr= null;
		Uri newUri;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{path}, null); 

		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID)));
		} else {
			File sdfile = new File(path);
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);

			Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
			//Uri uri = MediaStore.Audio.Media.getContentUri("external");
			newUri = context.getContentResolver().insert(uri, values);
		}

		if(cr != null) {
			cr.close();
		}
		
		Log.e("101010", "--setRingtone newUri = ----" + newUri);
		
		if(mProfileManager == null) {
			mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
		}

		if(mProfileManager != null){
			mProfileManager.setRingtoneUri("mtk_audioprofile_general", RingtoneManager.TYPE_RINGTONE, -1, newUri);
		}else{
			RingtoneManager.setActualDefaultRingtoneUri(context,
					RingtoneManager.TYPE_RINGTONE, newUri);
		}

		if(mCallBack != null){
			mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
		}
	}

	private void setNotification(String path, Context context) {

		Cursor cr= null;
		Uri newUri;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{path}, null); 
		
		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID)));
		} else {
			File sdfile = new File(path);
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, sdfile.getPath());
			values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);

			Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getPath());
			//Uri uri = MediaStore.Audio.Media.getContentUri("external");
			Log.e("101010", "--setNotification sdfile.getAbsolutePath() = ----" + sdfile.getAbsolutePath());
			Log.e("101010", "--setNotification sdfile.getPath() = ----" + sdfile.getPath());
			Log.e("101010", "--setNotification uri = ----" + uri);
			newUri = context.getContentResolver().insert(uri, values);
		}
		
		if(cr != null) {
			cr.close();
		}
		
		if(mProfileManager == null) {
			mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
		}
		
		Log.e("101010", "--setNotification newUri = ----" + newUri);
		
		if(mProfileManager != null){
			mProfileManager.setRingtoneUri("mtk_audioprofile_general", RingtoneManager.TYPE_NOTIFICATION, -1, newUri);
		}else{
			RingtoneManager.setActualDefaultRingtoneUri(context,
					RingtoneManager.TYPE_NOTIFICATION, newUri);
		}
		
		if(mCallBack != null){
			mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
		}
	}

	private void setMessage(String path, Context context) {
		
		Cursor cr= null;
		Uri newUri;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{path}, null); 

		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID)));
		} else {
			File sdfile = new File(path);
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);
	
			Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
					.getAbsolutePath());
			newUri = context.getContentResolver().insert(uri, values);
		}
		
		if(cr != null) {
			cr.close();
		}
		
		Log.e("101010", "--setMessage newUri = ----" + newUri);
		Settings.System.putString(context.getContentResolver(), "sms_sound", newUri.toString());
		
		if(mCallBack != null){
			mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
		}
	}
	
	private void setNotificationAndMessage(String path, Context context) {
		
		Cursor cr= null;
		Uri newUri;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{path}, null); 

		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID)));
		} else {
			File sdfile = new File(path);
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);
	
			Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
					.getAbsolutePath());
			newUri = context.getContentResolver().insert(uri, values);
		}
		
		if(cr != null) {
			cr.close();
		}
		
		if(mProfileManager == null) {
			mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
		}
		
		if(mProfileManager != null){
			mProfileManager.setRingtoneUri("mtk_audioprofile_general", RingtoneManager.TYPE_NOTIFICATION, -1, newUri);
		}else{
			RingtoneManager.setActualDefaultRingtoneUri(context,
					RingtoneManager.TYPE_NOTIFICATION, newUri);
		}
		
		Log.e("101010", "--setNotificationAndMessage newUri = ----" + newUri);
		Settings.System.putString(context.getContentResolver(), "sms_sound", newUri.toString());
		
		if(mCallBack != null){
			mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
		}
	}
	
	private void setAlarm(String path, Context context) {
		File sdfile = new File(path);
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, true);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		Uri newUri = context.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(context,
				RingtoneManager.TYPE_ALARM, newUri);
		
		if(mCallBack != null){
			mCallBack.onCompleted(true, ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS);
		}
	}

	private Context getContext() {
		return mCallBack != null ? mCallBack.getContext() : null;
	}

	protected long apply(File input, File output, String themeName) {

		long extractedSize = 0L;

		Enumeration<ZipEntry> entries;

		ZipFile zip = null;
		final String baseDir = output.getAbsolutePath();
		final File out = new File(baseDir);
		try {
			zip = new ZipFile(input);
			long uncompressedSize = getOriginalSize(zip);
			publishProgress(uncompressedSize);
			entries = (Enumeration<ZipEntry>) zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String name = entry.getName();
				if (name.endsWith("info") || name.contains("previews") || name.contains("avatar")
						|| name.contains("banner")) {
					continue;
				}
				if (name.contains(themeName)) {
					name = name.replace(themeName, "");
				}
				int prefixIndex = name.indexOf("/");
				name = name.substring(prefixIndex,name.length());
				File destination = new File(output, name);
				if (!destination.getParentFile().exists()) {
					Log.e(TAG, "make="
							+ destination.getParentFile().getAbsolutePath());
					destination.getParentFile().mkdirs();
				}
				ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(
						destination);
				extractedSize += copy(zip.getInputStream(entry), outStream);
				outStream.close();

			}

		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				zip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return extractedSize;

	}

	private long getOriginalSize(ZipFile file) {

		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
		long originalSize = 0l;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getSize() >= 0) {
				originalSize += entry.getSize();
			}
		}

		return originalSize;

	}

	private int copy(InputStream input, OutputStream output) {
		byte[] buffer = new byte[1024 * 8];
		BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
		BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
		int count = 0, n = 0;
		try {
			while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
				out.write(buffer, 0, n);
				count += n;
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}

		return count;
	}
	
	private boolean  realDeleteThemes(Theme... themes){
		  boolean success = true;
		if(themes != null){
			for(Theme theme:themes){
				String path = theme.fileDir+File.separatorChar+theme.fileName;
				File file = new File(path);
				if(file.exists()){
					success &= FileUtils.deleteFile(file);
					if(getContext() != null){
						LoadedThemeDao dao = new LoadedThemeDao(getContext());
						dao.deleteLoadedTheme(theme);
					}
				}
			}
		}
		return success;
	}
	

	private final class ProgressReportingOutputStream extends FileOutputStream {
		public ProgressReportingOutputStream(File file)
				throws FileNotFoundException {
			super(file);
		}

		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)
				throws IOException {
			super.write(buffer, byteOffset, byteCount);
			mProgress += byteCount;
			publishProgress(mProgress);
		}

	}
	
	class DeleteThemeThread implements Runnable{
		Theme[] themes;
		public  DeleteThemeThread(Theme... theme) {
			super();
			// TODO Auto-generated constructor stub
			this.themes = theme;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(themes != null){
				onSuccess(realDeleteThemes(themes));
			}
		}
		
		private void onSuccess(boolean success) {
			onPostExecute(success, success ? ThemeConfig.ThemeStatus.STATUS_DELETE_SUCCESS
					: ThemeConfig.ThemeStatus.STATUS_DELETE_FAUILER);
		}
		
		
	}

	class ApplyThemeThread implements Runnable {

		private int mMsg = -1;

		public ApplyThemeThread(int msg) {
			// TODO Auto-generated constructor stub
			Log.d("ap", "33333:"+msg);
			mMsg = msg;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			apply(mMsg);

		}

		private void apply(int msg) {
			Log.d("ap", "msg:"+msg);
			switch (msg) {
			case MSG_APPLY_THEME:
				onSuccess(doInBackground(mThemes));
				break;
			case MSG_APPLY_FONTS:

				break;
			case MSG_APPLY_LOCKSCREEN:
				onSuccess(applyLockScreen(mSingleTheme));
				break;

			case MSG_APPLY_RINGTONG:
				applyRingTong(mSingleTheme, ((ThemeAudio)mSingleTheme).ringtongType);
				break;
			case MSG_APPLY_WALLPAPER:
				applyWallpaper(mSingleTheme);
				break;
			default:
				break;
			}
		}

		private void onSuccess(boolean success) {
			onPostExecute(success, success ? ThemeConfig.ThemeStatus.STATUS_APPLY_SUCCESS
					: ThemeConfig.ThemeStatus.STATUS_APPLY_FAUILER);
			mIsRunning = false;
		}

	}

	
	class TimeWallpaperZip extends ZipFile{

		ArrayList<ZipEntry> cache =   new ArrayList<ZipEntry>();
		
		public TimeWallpaperZip(File file) throws ZipException, IOException {
			super(file);
			cache.clear();
			// TODO Auto-generated constructor stub
			try {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<ZipEntry> zipEnumeration = (Enumeration<ZipEntry>) zipFile.entries();
				ZipEntry entry = null;
				while (zipEnumeration.hasMoreElements()) {
					entry = zipEnumeration.nextElement();
					Context context = mCallBack.getContext();
		/*			if(context != null){
						int displaySize = context.getResources().getDisplayMetrics().densityDpi;
						String density = DENSITY_720;
						if(displaySize == DisplayMetrics.DENSITY_XXHIGH){
							density = DENSITY_1080;
						}
						String name = entry.getName();
						if(!name.contains(density)){
							continue;
						}
					}*/
					if (!entry.isDirectory()) {
						cache.add(entry);
					}
				}

			} catch (Exception ex) {
				Log.d(TAG, "open theme zip file exception-->" + ex);
			}
		}
		
		public List<ZipEntry> getEntries(){
	
			return cache;
		}
		
		public int getSize(){
			return cache.size();
		}
		
		
	}
	
}
