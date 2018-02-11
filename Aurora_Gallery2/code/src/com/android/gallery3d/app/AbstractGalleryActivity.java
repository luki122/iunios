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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import aurora.app.AuroraAlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.filtershow.FilterShowActivity;

import aurora.app.AuroraProgressDialog;

import com.android.gallery3d.selectfragment.LocalFragmentUtil;
import com.android.gallery3d.ui.AuroraStringTexture;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.Globals;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.LightCycleHelper.PanoramaViewHelper;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.GalleryAppImpl;

import aurora.app.AuroraActivity;

import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.inter.IOperationComplete;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager.FileUploadTaskInfo;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.aurora.utils.SystemUtils;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

import android.app.ActivityManager;
import android.os.SystemProperties;


public class AbstractGalleryActivity extends AuroraActivity implements GalleryContext, IOperationComplete {
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractGalleryActivity";
    private GLRootView mGLRootView;
    private StateManager mStateManager;
    private GalleryActionBar mActionBar;
    private OrientationManager mOrientationManager;
    private TransitionStore mTransitionStore = new TransitionStore();
    private boolean mDisableToggleStatusBar;
    private PanoramaViewHelper mPanoramaViewHelper;
    //Aurora <SQF> <2014-07-21>  for NEW_UI begin
    protected boolean mBackFromFilterShowActivity; 
    //Aurora <SQF> <2015-04-24>  for NEW_UI end
    
    public static final Uri BINDACCOUNTPROVIDERPROVIDER_URI = Uri.parse("content://com.android.gallery3d.BindAccountProvider");
    
    private LocalPopupWindowUtil mLocalPopupWindowUtil;
//    private LocalFragmentUtil mLocalFragmentWindowUtil;
    
    
    private boolean mFromXCloudAlbumMultiSelection;
    private boolean mFromLocalAlbumMultiSelection;//wenyongzhe 2016.3.2
    
    private AuroraProgressDialog mLoginBaiduProgressDialog;
    protected AccountHelper mAccountHelper; 
    private boolean mIsActive; //SQF ADDED ON 2015.5.18
    
    public AccountHelper getAccountHelper() {
    	return mAccountHelper;
    }
    
    
    /**
     * move to Application by JXH
     */
	/*private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap = new ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>>();

	public ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> getConcurrentHashMap() {
		return concurrentHashMap;
	}*/

    //Aurora <SQF> <2015-04-24>  for NEW_UI begin

    private AuroraAlertDialog mAlertDialog = null;
    private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getExternalCacheDir() != null) onStorageReady();
        }
    };
    private IntentFilter mMountFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//		Globals.OVERSEA_VERSION = true;//disable cloud album
		
        String value = SystemProperties.get("phone.type.oversea");
        if (!value.equalsIgnoreCase("true") || TextUtils.isEmpty(value)) {//for india
        	Globals.OVERSEA_VERSION = false;
        } else {
        	Globals.OVERSEA_VERSION = true;
        }
        
        MediaSetUtils.getCameraBucketID(this);//Iuni <lory><2013-12-17> add begin
        mOrientationManager = new OrientationManager(this);
        toggleStatusBarByOrientation();
	//	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//Iuni <paul><2013-12-24> added for gallery
        getWindow().setBackgroundDrawable(null);
        mPanoramaViewHelper = new PanoramaViewHelper(this);
        mPanoramaViewHelper.onCreate();
        
        //SQF ADDED ON 2015.4.24 begin
        if(!Globals.OVERSEA_VERSION){
	        mLocalPopupWindowUtil = new LocalPopupWindowUtil(this);
        	//wenyongzhe
//	        mLocalFragmentWindowUtil = new LocalFragmentUtil(this);
	        
	       	mAccountHelper = new AccountHelper(this);
			mAccountHelper.registerAccountContentResolver();
			mAccountHelper.update();
			
			mOperationUtil = new OperationUtil(this);
			mOperationUtil.setOperationComplete(this);
        }
		//SQF ADDED ON 2015.4.24 end
    }
    
    //wenyongzhe
    public LocalPopupWindowUtil getLocalPopupWindowUtil() {
    	return mLocalPopupWindowUtil;
    }
//    public LocalFragmentUtil getmLocalFragmentWindowUtil() {
//		return mLocalFragmentWindowUtil;
//	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        mGLRootView.lockRenderThread();
        try {
            super.onSaveInstanceState(outState);
            getStateManager().saveState(outState);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mStateManager.onConfigurationChange(config);
        getGalleryActionBar().onConfigurationChanged();
        invalidateOptionsMenu();
        toggleStatusBarByOrientation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return getStateManager().createOptionsMenu(menu);
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public DataManager getDataManager() {
        return ((GalleryApp) getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((GalleryApp) getApplication()).getThreadPool();
    }

    public synchronized StateManager getStateManager() {
        if (mStateManager == null) {
            mStateManager = new StateManager(this);
        }
        return mStateManager;
    }

    public GLRoot getGLRoot() {
    	if (mGLRootView == null) {//lory add
    		mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
		}
        return mGLRootView;
    }

    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);
        mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
    }
    
    //lory add
    public void startContentView(int resId) {
    	mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
	}

    protected void onStorageReady() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
            unregisterReceiver(mMountReceiver);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getExternalCacheDir() == null) {
            OnCancelListener onCancel = new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
				
					//paul modify
                    //finish();
					ClearAppUserData(getPackageName());
					
                }
            };
            OnClickListener onClick = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
			//paul modify
			/*
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.no_external_storage_title)
                    .setMessage(R.string.no_external_storage)
                    .setNegativeButton(android.R.string.cancel, onClick)
                    .setOnCancelListener(onCancel);
			*/
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.aurora_error_notice_title)
                    .setMessage(R.string.aurora_error_notice)
                    .setNegativeButton(android.R.string.ok, onClick)
                    .setOnCancelListener(onCancel);
            if (ApiHelper.HAS_SET_ICON_ATTRIBUTE) {
                setAlertDialogIconAttribute(builder);
            } else {
                builder.setIcon(android.R.drawable.ic_dialog_alert);
            }
            mAlertDialog = builder.show();
            registerReceiver(mMountReceiver, mMountFilter);
        }
        mPanoramaViewHelper.onStart();
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    private static void setAlertDialogIconAttribute(
            AuroraAlertDialog.Builder builder) {
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog != null) {
            unregisterReceiver(mMountReceiver);
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        mPanoramaViewHelper.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;//SQF ADDED ON 2015.5.18
        mGLRootView.lockRenderThread();
        try {
            getStateManager().resume();
            getDataManager().resume();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        mGLRootView.onResume();
        mOrientationManager.resume();
		if(!Globals.OVERSEA_VERSION){
        	XCloudTaskListenerManager.getInstance(this).registerListener(mAlbumTaskListener);//SQF ADDED ON 2015.5.5
		}
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        mIsActive = false;//SQF ADDED ON 2015.5.18
        mOrientationManager.pause();
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            getStateManager().pause();
            getDataManager().pause();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        clearBitmapPool(MediaItem.getMicroThumbPool());
        clearBitmapPool(MediaItem.getThumbPool());

        MediaItem.getBytesBufferPool().clear();
        
        //AuroraStringTexture.StringCache.clear();//paul add
        
    }

    private static void clearBitmapPool(BitmapPool pool) {
        if (pool != null) pool.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!Globals.OVERSEA_VERSION){
        	mAccountHelper.unregisterAccountContentResolver();
			XCloudTaskListenerManager.getInstance(this).unregisterListener(mAlbumTaskListener);//SQF ADDED ON 2015.5.5
        }
        mGLRootView.lockRenderThread();
        try {
            getStateManager().destroy();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGLRootView.lockRenderThread();
        try {
            getStateManager().notifyActivityResult(
                    requestCode, resultCode, data);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onBackPressed() {
        // send the back event to the top sub-state
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            getStateManager().onBackPressed();
        } finally {
            root.unlockRenderThread();
        }
    }

    //wenyongzhe 2015.10.29
    public void onActionBarBack(){
		try {
			if(getStateManager().getTopState() instanceof AlbumPage){
				AlbumPage mAlbumPage = (AlbumPage) getStateManager().getTopState();
				mAlbumPage.leaveSelectionMode();
			}
		} catch (Exception e) {
		} 
    }
    
	//lory add 12.6
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Iuni <lory><2013-12-21> add begin
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
    		//wenyongzhe
    		try {
    			if(mLocalPopupWindowUtil.isShowPopupWindow()){
    				mLocalPopupWindowUtil.dismissSelectPopupWindow();
//	    		if(mLocalFragmentWindowUtil.isShowPopupWindow()){
//	        		mLocalFragmentWindowUtil.dismissSelectPopupWindow();
	    			if(getStateManager().getTopState() instanceof AlbumPage){
	    				AlbumPage mAlbumPage = (AlbumPage) getStateManager().getTopState();
	    				mAlbumPage.leaveSelectionMode();
	    			}
	    			return true;
	    		}
    		} catch (Exception e) {
    		} 
    		//wenyongzhe
        	
    		GLRoot root = getGLRoot();
    		root.lockRenderThread();
	        try {
	             if (getStateManager().onMyKeyDownEvent(keyCode, event)) {
	            	 return true;
	             }
	        } finally {
	            root.unlockRenderThread();
	        }
		}
		//Iuni <lory><2013-12-21> add end
		return super.onKeyDown(keyCode, event);
	}
    
    public GalleryActionBar getGalleryActionBar() {
        if (mActionBar == null) {
            mActionBar = new GalleryActionBar(this);
        }
        return mActionBar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            return getStateManager().itemSelected(item);
        } finally {
            root.unlockRenderThread();
        }
    }

    protected void disableToggleStatusBar() {
        mDisableToggleStatusBar = true;
    }

    // Shows status bar in portrait view, hide in landscape view
    private void toggleStatusBarByOrientation() {
        if (mDisableToggleStatusBar) return;
    }

    public TransitionStore getTransitionStore() {
        return mTransitionStore;
    }

    public PanoramaViewHelper getPanoramaViewHelper() {
        return mPanoramaViewHelper;
    }

    protected boolean isFullscreen() {
        return (getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }
	
	//paul add start
    public  void ClearAppUserData(String pkgName){

    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		try {			
			am.clearApplicationUserData(pkgName, null);
		}catch(Exception ex){
    		ex.printStackTrace() ;
    	} 

    }	
	
	public boolean inCloudView(){
		return false;
	}
    public void onIndexChanged(int curIndex){

	}
	public boolean isImgDownloaded(String name){
		return false;
	}
	//paul add end
    
    
    
    //SQF ADD ON 2015.4.27 begin
    private OperationUtil mOperationUtil;
    public OperationUtil getOperationUtil() {
    	return mOperationUtil;
    }
    
    @Override
	public void renameComplete(boolean success) {

	}

    @Override
	public void delComplete(boolean success) {

	}
	
    @Override
	public void moveOrCopyComplete(boolean success,boolean isMove,int errorCode) {

	}
	
    @Override
	public void createAlbumComplete(boolean success) {

	}
    
    private IAlbumTaskListener mAlbumTaskListener = new IAlbumTaskListener() {

		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {
			// TODO Auto-generated method stub
			//Log.i("SQF_LOG", "AbstractGalleryActivity::onGetTaskStatus --> " + bean.getSource() + " " + bean.getTarget());
			UploadTaskListManager manager = ((GalleryAppImpl)getApplication()).getUploadTaskListManager();
			//wenyongzhe 2015.11.4 upload toash bug start
			/*if (manager.updateFileUploadTaskInfo(bean) == UploadTaskListManager.UPDATE_FILE_UPLOAD_TASK_OK ) {
				try {
					ToastUtils.showTast(AbstractGalleryActivity.this,R.string.aurora_upload_complete);
				} catch (Exception e) {
				}
			}*/
			//wenyongzhe 2015.11.4 upload toash bug end
			updateUploadProgress(bean);
			updateNewUploadRedDot(bean);
		}

		@Override
		public long progressInterval() {
			// TODO Auto-generated method stub
			return 200;
		}

		@Override
		public void onGetTaskListFinished(
				List<FileTaskStatusBean> fileTaskStatusBeanList) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    public void removeTaskList() {
    	
    }
    
    public void setIsFromXCloudAlbumMultiSelection(boolean isMultiSelection) {
    	mFromXCloudAlbumMultiSelection = isMultiSelection;
    }
    
    
    public boolean isFromXCloudAlbumMultiSelection() {
    	return mFromXCloudAlbumMultiSelection;
    }

    public boolean showingPhotoView() {
    	if(getStateManager() == null) return false;
    	ActivityState topState = getStateManager().getTopState();
    	if(topState == null || ! (topState instanceof AlbumPage)) return false;
    	AlbumPage albumPage = (AlbumPage)topState;
    	return albumPage.isShowingPhotoView();
    }
    
    public void showUploadProgress() {
		if(!mIsActive || isFinishing() || Globals.OVERSEA_VERSION) return;//paul add for BUG #15505<2015-08-06>
    	if(showingPhotoView()) return;
    	if(isFromXCloudAlbumMultiSelection()) return;
		if(! mLocalPopupWindowUtil.isShowProgressPopupWindow()) {
			View rootView = findViewById(R.id.gallery_root);
			View parent = (View)rootView.getParent();
			mLocalPopupWindowUtil.showProgressPopupWindow(parent);
			updateUploadProgress(null);
		}
//    	if(! mLocalFragmentWindowUtil.isShowProgressPopupWindow()) {
//			View rootView = findViewById(R.id.gallery_root);
//			View parent = (View)rootView.getParent();
//			mLocalFragmentWindowUtil.showProgressPopupWindow(parent);
//			updateUploadProgress(null);
//		}
    }
    
    public void dismissUploadProgress() {
		if(Globals.OVERSEA_VERSION) return;//paul add
    	if(mLocalPopupWindowUtil.isShowProgressPopupWindow() ) {
    		mLocalPopupWindowUtil.dismissProgressPopupWindow();
		}
//		if(mLocalFragmentWindowUtil.isShowProgressPopupWindow() ) {
//			mLocalFragmentWindowUtil.dismissProgressPopupWindow();
//		}
    }
    
    public void showNewUploadRedDot(boolean show) {
    	ImageView imageView = (ImageView)getAuroraActionBar().findViewById(R.id.aurora_new_upload_red_dot);
    	if(imageView == null) return;
    	if(show) {
    		imageView.setVisibility(View.VISIBLE);
    	} else {
    		imageView.setVisibility(View.INVISIBLE);
    	}
    }
    
    public void updateNewUploadRedDot(FileTaskStatusBean bean) {
    	if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE && bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		//MyLog.i2("SQF_LOG", "AbstractGalleryActivity::updateNewUploadRedDot");
    		showNewUploadRedDot(true);
    	}
    }
    
    public void updateUploadProgress(FileTaskStatusBean bean) {
    	if( ! mIsActive || Globals.OVERSEA_VERSION) {
    		//Log.i("SQF_LOG", "AbstractGalleryActivity::updateUploadProgress --------------------! mIsActive return");
    		return;
    	}
    	UploadTaskListManager manager = ((GalleryAppImpl)getApplication()).getUploadTaskListManager();
    	//wenyongzhe
//    	LocalFragmentUtil util = mLocalFragmentWindowUtil;
    	LocalPopupWindowUtil util = mLocalPopupWindowUtil;
    	FileUploadTaskInfo fileInfo = manager.getCurrentBeanInfo();
    	int i = manager.getCurrentTaskParcelIndex();
    	int total = manager.howManyPhotosUnderCurrentParcel();
    	int complete = manager.completeNumberUnderCurrentParcel();
    	int progress = total == 0 ?  0 : (complete * 100 / total);
    	if(fileInfo != null) {
    		util.getProgressPopupWindow().displayIconImage(fileInfo.uploadInfo);
    	}
    	if(total == 0) {
    		util.getProgressPopupWindow().setLoadStatusText("");
    	} else {
    		util.getProgressPopupWindow().setLoadStatusText(complete + "/" + total);
    	}
	    //util.getProgressPopupWindow().setLoadNumText((i+1) + "");
	    util.getProgressPopupWindow().setLoadProgressBar(progress);
	    util.getProgressPopupWindow().setTaskTitle(R.string.aurora_album_upload_status);
	    //add by JXH 2015-5-14 jump type begin
	    util.getProgressPopupWindow().setJumpDownLoadTab(false);
	    //add by JXH 2015-5-14 jump type end
	    if(manager.getParcelSize() == 0) {
	    	dismissUploadProgress();
	    }
	    //wenyongzhe 2016.1.25 new_ui
//	    downloadNotification(total,complete);
    }
    
  //wenyongzhe 2016.1.25 new_ui
    private void downloadNotification(int total, int complete) {
    	complete++;
		NotificationManager mNotifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification.Builder builder = new Notification.Builder(
				this);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
		if(total == complete){
			builder.setContentTitle(getString(R.string.aurora_downloaded));
		}else{
			builder.setContentTitle(getString(R.string.aurora_downloading));
		}
		builder.setTicker("downloading");
		builder.setContentText(complete+"/"+total);
		Notification notification = builder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotifManager.notify(0, notification);
	}
    
    
	private AuroraProgressDialog createAuroraProgressDialog(int titleId, int progressMax) {
		AuroraProgressDialog dialog = new AuroraProgressDialog(this);
		dialog.setTitle(titleId);
		dialog.setMax(progressMax);
		dialog.setCancelable(true);
		dialog.setIndeterminate(true);
		if (progressMax > 1) {
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		return dialog;
	}
	
	public void showLoginBaiduAuroraProgressDialog() {
		if(null == mLoginBaiduProgressDialog) {
			mLoginBaiduProgressDialog = createAuroraProgressDialog(R.string.baidu_cloud_login_progress_tip, 0);
		}
		mLoginBaiduProgressDialog.show();
	}
	
	public void dismissAuroraProgressDialog() {
		if(null != mLoginBaiduProgressDialog ) {
			mLoginBaiduProgressDialog.dismiss();
			mLoginBaiduProgressDialog = null;
		}
	}
    //SQF ADD ON 2015.4.27 end

	//wenyongzhe 2016.3.2 start
	public boolean ismFromLocalAlbumMultiSelection() {
		return mFromLocalAlbumMultiSelection;
	}

	public void setmFromLocalAlbumMultiSelection(
			boolean mFromLocalAlbumMultiSelection) {
		this.mFromLocalAlbumMultiSelection = mFromLocalAlbumMultiSelection;
	}
	//wenyongzhe 2016.3.2 end
	
}
