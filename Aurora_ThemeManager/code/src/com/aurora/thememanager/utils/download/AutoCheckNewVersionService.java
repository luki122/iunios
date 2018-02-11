package com.aurora.thememanager.utils.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.fragments.HttpCallBack;
import com.aurora.thememanager.fragments.JsonHttpListener;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.parser.ThemePkgVersionCheckParser;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * 检测主题包版本更新的后台服务
 * @author alexluo
 *
 */
public class AutoCheckNewVersionService extends Service implements HttpCallBack{

	/**
	 * 数据库操作类
	 */
	private DatabaseController mDbController;
	
	private DatabaseController mUpdateDbController;
	
	private ThemeInternetHelper mThemeLoadHelper;
	
	private JsonHttpListener mHttpListener;
	
	private List<DownloadData> mDownloadedThemes;
	
	private Parser mCheckParser; 
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDbController = DatabaseController.getController(this, DatabaseController.TYPE_DOWNLOAD);
		mUpdateDbController = DatabaseController.getController(this, DatabaseController.TYPE_AUTO_UPDATE);
		mHttpListener = new JsonHttpListener(this);
		mThemeLoadHelper = new ThemeInternetHelper(this);
		mCheckParser = new Parser(new ThemePkgVersionCheckParser());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(mDbController != null){
			mDbController.openDatabase();
			mDownloadedThemes =mDbController.getDownloadedDatas();
			if(mDownloadedThemes != null && mDownloadedThemes.size()>0){
				for(DownloadData theme:mDownloadedThemes){
					if(theme.status == FileDownloader.STATUS_APPLY_WAIT
							&& themeFileExist(theme.fileDir, theme.fileName)){
						checkNewVersion(theme.downloadId, theme.versionCode);
					}
				}
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	
	private boolean themeFileExist(String dir,String name){
		File file = new File(dir+File.separatorChar+name);
		return file.exists();
	}
	
	
	
	/**
	 * 请求网络数据
	 * @param page
	 */
	private void checkNewVersion(int themeId,float versionCode){
		mThemeLoadHelper.clearRequest();
		mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_NEW_VERSION_CHECK_URL,mHttpListener,
				HttpUtils.createCheckThemeNewVersion(this, themeId,versionCode));
		mThemeLoadHelper.startRequest();
	}



	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onSuccess(Object response) {
		// TODO Auto-generated method stub
		if(response != null){
			if(mUpdateDbController == null){
				mUpdateDbController = DatabaseController.getController(this, DatabaseController.TYPE_AUTO_UPDATE);
			}
			mUpdateDbController.openDatabase();
			List<Object> themes = mCheckParser.startParser(response.toString());
			if(themes != null && themes.size() > 0){
				for(Object obj:themes){
					Theme theme = (Theme)obj;
					mUpdateDbController.updateNewVersionState(theme.themeId, 1);
				}
			}
		}
	}



	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onNetworking() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onUsedCache() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onRetry() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onProgressChange(long fileSize, long downloadedSize) {
		// TODO Auto-generated method stub
		
	}



	public static void startAutoUpdate(Context context, int i) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context,AutoCheckNewVersionService.class);
		context.startService(intent);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
