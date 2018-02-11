/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import aurora.widget.AuroraActionBar;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.gadget.WidgetUtils;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.setting.tools.SettingLocalUtils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.LightCycleHelper;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GalleryAppImpl extends Application implements GalleryApp {

    private static final String DOWNLOAD_FOLDER = "download";
    private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private DataManager mDataManager;
    private ThreadPool mThreadPool;
    private DownloadCache mDownloadCache;
    private StitchingProgressManager mStitchingProgressManager;
    
    //wenyongzhe 2016.1.6
    private AuroraActionBar mAuroraActionBar;
    private Context mActivityContext;
    
    private UploadTaskListManager mUploadTaskListManager;//SQF ADDED ON 2015.5.4
    
    private ArrayList<String> mSelectedFilesForXCloud; //SQF ADDED ON 2015.5.4
    
    //SQF ADDED ON 2015.5.4
    
    public UploadTaskListManager getUploadTaskListManager() {
    	return mUploadTaskListManager;
    }
    
    public void setSelectedFilesForXCloud(ArrayList<String> filePaths) {
    	mSelectedFilesForXCloud = filePaths;
    }
    
    public ArrayList<String> getSelectedFilesForXCloud() {
		return mSelectedFilesForXCloud;
	}


	@Override
    public void onCreate() {
        super.onCreate();
        /**
         * 检测内存泄露
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .penaltyDeath()
        .build());
          */
        //com.android.camera.Util.initialize(this); paul del
        initializeAsyncTask();
        GalleryUtils.initialize(this);
        WidgetUtils.initialize(this);
        PicasaSource.initialize(this);

        mStitchingProgressManager = LightCycleHelper.createStitchingManagerInstance(this);
        if (mStitchingProgressManager != null) {
            mStitchingProgressManager.addChangeListener(getDataManager());
        }
        
        mUploadTaskListManager = new UploadTaskListManager();
        downloadTaskListManager = new DownloadTaskListManager();//add by JXH 2015-5-27
        
        SettingLocalUtils.inint(this);//wenyongzhe 2016.3.8
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    @Override
    public StitchingProgressManager getStitchingProgressManager() {
        return mStitchingProgressManager;
    }

    @Override
    public ImageCacheService getImageCacheService() {
        // This method may block on file I/O so a dedicated lock is needed here.
        synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public synchronized DownloadCache getDownloadCache() {
        if (mDownloadCache == null) {
            File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);

            if (!cacheDir.isDirectory()) cacheDir.mkdirs();

            if (!cacheDir.isDirectory()) {
                throw new RuntimeException(
                        "fail to create: " + cacheDir.getAbsolutePath());
            }
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
        }
        return mDownloadCache;
    }

    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }
    //add by JXH for Cloud Album  2015-5-4 begin
    private List<List<CommonFileInfo>> downloadTask =Collections.synchronizedList(new ArrayList<List<CommonFileInfo>>());

	public synchronized List<List<CommonFileInfo>> getDownloadTask() {
		synchronized (downloadTask) {
			return downloadTask;
		}
	}
	private DownloadTaskListManager downloadTaskListManager;

	public DownloadTaskListManager getDownloadTaskListManager() {
		return downloadTaskListManager;
	}

	public AuroraActionBar getmAuroraActionBar() {
		return mAuroraActionBar;
	}

	public void setmAuroraActionBar(AuroraActionBar mAuroraActionBar) {
		this.mAuroraActionBar = mAuroraActionBar;
	}

	public Context getmActivityContext() {
		return mActivityContext;
	}

	public void setmActivityContext(Context mActivityContext) {
		this.mActivityContext = mActivityContext;
	}
	
	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap = new ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>>();

	public ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> getConcurrentHashMap() {
		return concurrentHashMap;
	}
	
	
    //add by JXH for Cloud Album  2015-5-4 end
	
	//add by JXH for local Albu, 2016-3-9 begin
	private List<MediaFileInfo> mediaFileInfos;

	public List<MediaFileInfo> getMediaFileInfos() {
		return mediaFileInfos;
	}

	public void setMediaFileInfos(List<MediaFileInfo> mediaFileInfos) {
		this.mediaFileInfos = mediaFileInfos;
	}
	
	//add by JXH for local Albu, 2016-3-9 end
	
	
}
