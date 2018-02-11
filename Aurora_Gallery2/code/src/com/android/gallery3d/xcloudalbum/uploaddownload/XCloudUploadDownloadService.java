package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class XCloudUploadDownloadService extends Service {

	private static final int MAX_THREADS = 3;
	
	private ExecutorService mThreadPool; 
	
	private MyBinder mBinder = new MyBinder();
	

	
	private class MyBinder extends Binder {
		
		public XCloudUploadDownloadService getService(){
            return XCloudUploadDownloadService.this;
        }
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("SQF_LOG", "XCloudUploadDownloadService::onBind");
		return mBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(mThreadPool == null) mThreadPool = Executors.newFixedThreadPool(MAX_THREADS);
		//mThreadPool
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	
	
}
