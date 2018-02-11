package com.aurora.filemanager;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.security.PublicKey;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.fileObserver.AuroraFileObserver;
import com.aurora.fileObserver.AuroraFileObserverListener;
import com.aurora.filemanager.fragment.AuroraMainFragment;
import com.aurora.filemanager.fragment.FileCategoryFragment;
import com.aurora.filemanager.fragment.FileViewFragment;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.fragment.PictureFragment;
import com.aurora.filemanager.fragment.SdNotAvailableFragment;
import com.aurora.filemanager.R;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileIconHelper;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileUtils;
import com.aurora.tools.OperationAction;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.Util;
import com.aurora.tools.OperationAction.Operation;
import com.aurora.widget.AuroraFileBrowserAdater;
import com.aurora.widget.AuroraFilesAdapter;
import com.aurora.widget.AuroraOperationBarMoreMenu;
import com.privacymanage.PrivacyBroadcastReceiver;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.IPrivacyManageService;

import android.R.string;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.test.UiThreadTest;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DebugUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.os.storage.StorageManager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBarItem;
import android.os.storage.StorageVolume;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Downloads;
import android.provider.Settings;

import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.filemanager.inter.SelectPageInterface;

public class FileExplorerTabActivity extends AuroraActivity implements SelectPageInterface, AuroraFileObserverListener, onFileCategoryInfoChangedLisenter {
	public AuroraActionBar auroraActionBar;

	private StorageManager mStorageManager;
	public static String mSDCardPath;
	public static String mSDCard2Path;
	private String lastStorage;
	public static String ROOT_PATH;

	private Boolean isFromPicManager;// 从相册跳入
	private Boolean isFromWallpaperManager;// 从壁纸跳入
	private Boolean isFromWallpaperLocalManager;
	private Boolean isFromAudioManager;// 从通话跳入
	private String isFromWallpaperFileType;

	private boolean isGetFileFromSdList;

	private Boolean isGetMoreNoPrivacy;// 判断从多图,多视频入口调用是否显示隐私数据
	
	private int actionBarTitleLen=630;

	private Boolean isFromOtherAPP;
	private Boolean isGetMorePic;
	private Boolean isGetMoreVideo;

	private Fragment currentFragment;
	private int currentType;
	
	private boolean isNoMenuKey=false;

	// 所有地异步AsyncTask操作使用这个线程池
	private ExecutorService FULL_TASK_EXECUTOR;

	public ExecutorService getFULL_TASK_EXECUTOR() {
		if (FULL_TASK_EXECUTOR == null) {
			FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
		}
		return FULL_TASK_EXECUTOR;
	}

	public Fragment getCurrentFragment() {
		return currentFragment;
	}

	public void setCurrentFragment(Fragment currentFragment) {
		this.currentFragment = currentFragment;
	}

	public int getCurrentType() {
		return currentType;
	}

	public void setCurrentType(int currentType) {
		this.currentType = currentType;
	}

	private FileCategory nowfileCategory;

	private List<FileInfo> picItemList;

	public AuroraOperationBarMoreMenu auroraOperationBarMoreMenu;

	private static final String TAG = "FileExplorerTabActivity";

	public OperationAction operationAction;

	private List<FileInfo> videoList = new ArrayList<FileInfo>();

	public List<FileInfo> getVideoList() {
		return videoList;
	}

	public void setVideoList(List<FileInfo> videoList) {
		this.videoList.clear();
		this.videoList.addAll(videoList);
	}

	private List<FileInfo> storages = new ArrayList<FileInfo>();

	// add by jxh 2014-7-1 begin
	private List<String> storagesStrings = new ArrayList<String>();
	private List<String> allStorages = new ArrayList<String>();
	

	public List<String> getAllStorages() {
		return allStorages;
	}

	/**
	 * @return the storagesStrings
	 */
	public List<String> getStoragesStrings() {
		return storagesStrings;
	}

	/**
	 * @param storagesStrings
	 *            the storagesStrings to set
	 */
	public void setStoragesStrings(List<String> storagesStrings) {
		this.storagesStrings = storagesStrings;
	}

	// add by jxh 2014-7-1 end
	/**
	 * @return the storages
	 */
	public List<FileInfo> getStorages() {
		return storages;
	}

	// add by Jxh 2014-8-25 search back position begin
	private int searchPostion = 0;

	public int getSearchPostion() {
		return searchPostion;
	}

	public void setSearchPostion(int searchPostion) {
		this.searchPostion = searchPostion;
	}

	// add by Jxh 2014-8-25 search back position end
	public String searchKey;

	/**
	 * @return the searchKey
	 */
	public String getSearchKey() {
		return searchKey;
	}

	/**
	 * @param searchKey
	 *            the searchKey to set
	 */
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	private String rootPath;// 储存SD卡路径浏览

	/**
	 * @return the rootPath
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @param rootPath
	 *            the rootPath to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	private String searchPath;// 搜索跳转路径

	/**
	 * @return the searchPath
	 */
	public String getSearchPath() {
		return searchPath;
	}

	/**
	 * @param searchPath
	 *            the searchPath to set
	 */
	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

	private String picPath;

	/**
	 * @return the picPath
	 */
	public String getPicPath() {
		return picPath;
	}

	/**
	 * @param picPath
	 *            the picPath to set
	 */
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public interface IBackPressedListener {
		boolean onBack();
	}

	public ActivityInfo activityInfo;
	private PackageManager pm;

	public ActivityInfo getActivityInfo() {
		return activityInfo;
	}

	public void setActivityInfo(ActivityInfo activityInfo) {
		this.activityInfo = activityInfo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing();
		// long t1 = SystemClock.currentThreadTimeMillis();
		setAuroraContentView(R.layout.aurora_file_manager_main, AuroraActionBar.Type.Normal, true);
		auroraActionBar = getAuroraActionBar();
		actionBarTitleLen = getResources().getDimensionPixelSize(R.dimen.actionbar_title_length);
		// actionBar right icons,just visible one icon
		isNoMenuKey = Util.isNoMeunKey();
		if(isNoMenuKey){
			auroraActionBar.addItem(AuroraActionBarItem.Type.Add, R.id.aurora_action_bar_all_add);
		}
		auroraActionBar.addItem(AuroraActionBarItem.Type.Edit, R.id.aurora_action_bar_all_edit);
		auroraActionBar.addItem(AuroraActionBarItem.Type.Search, R.id.aurora_action_bar_all_search);
		auroraActionBar.addItem(R.drawable.aurora_action_bar_more_normal, R.id.aurora_action_bar_all_more, getResourceString(R.string.aurora_file_manager_detail));
		//add edit search more
		
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		setAuroraSystemMenuCallBack(auroraSystemMenuCallBack);

		operationAction = new OperationAction(this);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		notifiedStateChanged();

		// actionBar点击监听
		auroraActionBar.setOnAuroraActionBarListener(actionBarItemClickListener);
		auroraActionBar.setmOnActionBarBackItemListener(actionBarBackItemClickListener);

		// 初始化more
		auroraOperationBarMoreMenu = new AuroraOperationBarMoreMenu(R.style.ActionBottomBarMorePopupAnimation, R.layout.aurora_actionbar_more_menu_page, FileExplorerTabActivity.this);
		// 设置新建文件夹底部弹出框
		setAuroraMenuItems(R.menu.aurora_bm_menu,com.aurora.R.layout.aurora_menu_fillparent);
		// 默认新建文件夹底部弹出框 不可弹出
		setMenuEnable(false);
		getAuroraMenu().setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				removeFileCoverView();
			}
		});
		pm = getPackageManager();
		try {
			setActivityInfo(pm.getActivityInfo(getComponentName(), 0));
			// LogUtil.log(TAG, "acitivity==" + activityInfo.name + " "
			// + activityInfo.loadLabel(pm).toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (activityInfo.name.endsWith(getString(R.string.activity_picture))) {
			showEmptyBarItem();// 隐藏储存详情按钮
			selectPage(AuroraConfig.AURORA_PIC_ITEM_PAGE, FileCategory.Picture);
		} else if (activityInfo.name.endsWith(getString(R.string.activity_video))) {
			showEmptyBarItem();// 隐藏储存详情按钮
			selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Video);
		} else {
			auroraFromPicIntent = getIntent();
			if (!getOtherIntent()) {// savedInstanceState == null && 开启N多个APP
				// 会重新加载activity
				selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
			}
		}
		showSdNotAvailableView();
		ActivityInitFunction();
		// add by Jxh 2014-9-10 绑定隐私服务 begin
		try {
			Intent intent = new Intent(AuroraConfig.PRIVACYACTION);
			if (!Util.isLowVersion()) {
				intent = Util.createExplicitFromImplicitIntent(FileExplorerTabActivity.this, intent);
			}
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// add by Jxh 2014-9-10 绑定隐私服务 end
		registerAuroraBroadcastReceiver();
		registerPrivacyBroadcastReceiver();

		Uri uri = Files.getContentUri(FileCategoryHelper.volumeName);
		registerContentObserver(uri, mFileContentObserver);
		uri = Audio.Media.getContentUri(FileCategoryHelper.volumeName);
		registerContentObserver(uri, mAudioContentObserver);
		uri = Video.Media.getContentUri(FileCategoryHelper.volumeName);
		registerContentObserver(uri, mVideoContentObserver,true);
		uri = Images.Media.getContentUri(FileCategoryHelper.volumeName);
		registerContentObserver(uri, mImageContentObserver,true);

		// long t2 = SystemClock.currentThreadTimeMillis();
		// LogUtil.d(TAG, "onCreate time :"+(t2-t1));

	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		operationAction.mDialogDismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Message msg = handler.obtainMessage(DOWNLOAD);
		handler.sendMessage(msg);
		// LogUtil.elog(TAG, "onResume()");
		if (mSettingsObserver != null) {
			mSettingsObserver.observe();
		}
		if (!getIsFromOtherAPP()) {
			isStop = false;
		}
		if (Util.getRefreshCategory(getApplication(), FileCategory.Music, true)) {
			audioContentObserverAction();
		}
		if (Util.getRefreshCategory(getApplication(), FileCategory.Picture, true)) {
			imageContentObserverAction();
		}
		if (Util.getRefreshCategory(getApplication(), FileCategory.Video, true)) {
			videoContentObserverAction();
		}
		if (Util.getRefreshCategory(getApplication(), FileCategory.Doc, true)) {
			fileContentObserverAction();
		}

	}

	private boolean isStop = false;

	@Override
	protected void onStop() {

		if (mSettingsObserver != null) {
			mSettingsObserver.unregister();
		}
		if (!getIsFromOtherAPP()) {
			isStop = true;
		}
		// Debug.stopMethodTracing();
		super.onStop();
		// LogUtil.elog(TAG, "stop");

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// LogUtil.elog(TAG, "onNewIntent");
		auroraFromPicIntent = intent;
		if (!getOtherIntent() && isFromOtherAppBefore) {
			unDoAllOperation();
			isFromOtherAppBefore = false;
			setSearchPath("");// 重置跳转路径
			selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
		}
	}

	/**
	 * 用于Sd卡刷新
	 */
	private boolean needRefresh = true;

	public boolean isNeedRefresh() {
		return needRefresh;
	}

	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}

	/**
	 * 隐藏底部编辑按钮
	 */
	public void hideDeleteMenu() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				auroraActionBar.setShowBottomBarMenu(false);
				auroraActionBar.showActionBarDashBoard();
				if (currentFragment instanceof FileViewFragment) {

					AuroraFileBrowserAdater adater = ((FileViewFragment) currentFragment).getAuroraFileBrowserAdater();
					if (adater != null) {
						adater.isShowAnim();
					}
					setMenuEnable(true);

				} else if (currentFragment instanceof FileCategoryFragment) {
					AuroraFilesAdapter adater = ((FileCategoryFragment) currentFragment).getAuroraFilesAdater();
					if (adater != null) {
						adater.isShowAnim();
					}
				}

			}
		});

	}
	
	
	public void setAuroraBottomBarMenuCallBack(){
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
	}

	/**
	 * 隐藏底部编辑按钮
	 */
	public void hideDeleteMenuCopyOrCut() {
		handler.post(hideMenu);
	}

	/**
	 * 隐藏底部编辑按钮 Runnable
	 */
	private final Runnable hideMenu = new Runnable() {

		@Override
		public void run() {

			auroraActionBar.setShowBottomBarMenu(false);
			auroraActionBar.showActionBarDashBoard();
			if (currentFragment instanceof FileViewFragment) {

				AuroraFileBrowserAdater adater = ((FileViewFragment) currentFragment).getAuroraFileBrowserAdater();
				if (adater != null) {
					adater.notifyDataSetChanged();
					adater.isShowAnim();
				}
				setMenuEnable(true);

				if (OperationAction.getLastOperation() == Operation.copy || OperationAction.getLastOperation() == Operation.cut) {
					if (((FileViewFragment) currentFragment).getNowPath().equals(mSDCardPath)) {
						setNeedRefresh(false);
					} else {
						setNeedRefresh(true);
					}
					((FileViewFragment) currentFragment).setNowPath(FileExplorerTabActivity.ROOT_PATH);
					((FileViewFragment) currentFragment).getRootListStorage(ROOT_PATH, false);
					((FileViewFragment) currentFragment).setupOperationPane(true);
				}

			}

		}
	};

	/**
	 * 判断编辑动画是否播放完成
	 * @return
	 */
	public boolean actionBarIsAnimRunning() {
		if (auroraActionBar == null) {// 快速点击图片和手机返回键
			return true;
		}
		if (auroraActionBar.auroraIsEntryEditModeAnimRunning() || auroraActionBar.auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}

	/**
	 * actionBar 回调
	 */
	private OnAuroraActionBarItemClickListener actionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int item) {
			LogUtil.d(TAG, "---actionBarItemClickListener item:"+item);
			switch (item) {
			case R.id.aurora_action_bar_all_more:
				if (currentFragment instanceof AuroraMainFragment) {
					Intent intent = new Intent(FileExplorerTabActivity.this, AuroraStorageDetailActivity.class);
					((AuroraMainFragment) currentFragment).cancelQueryTask();
					((FileApplication) getApplicationContext()).setHashMap(getHashMap());
					if (categoryHelper == null) {
						FileCategoryHelper categoryHelper = ((AuroraMainFragment) currentFragment).getCategoryHelper();
						setStatistics(categoryHelper, 0);
					} else {
						setStatistics(categoryHelper, 0);
					}
					startActivity(intent);
					overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter, com.aurora.R.anim.aurora_activity_open_exit);
				}
				// 显示存储详情
				break;
			case R.id.aurora_action_bar_all_edit:
				// 显示分类及文件浏览
				if (currentFragment instanceof PictureCategoryFragment) {// 点击图片edit按钮
					((PictureCategoryFragment) currentFragment).editShowView();
				} else if (currentFragment instanceof PictureFragment) {
					((PictureFragment) currentFragment).editShowView();
				} else if (currentFragment instanceof FileCategoryFragment) {
					setStatistics(categoryHelper, 13);
					((FileCategoryFragment) currentFragment).editShowView();
				} else if (currentFragment instanceof FileViewFragment) {
					setStatistics(categoryHelper, 13);
					((FileViewFragment) currentFragment).editShowView();
				}
				break;
			case R.id.aurora_action_bar_all_add:
				if(currentFragment instanceof FileViewFragment){
//					showAuroraMenu(getContentView(), Gravity.BOTTOM, 0, 0);
					// add by JXH BUG 5869 begin
					((AuroraFragment) currentFragment).auroraSetRubbishBack();
					// add by jxh end
					operationAction.createNewFolder(((FileViewFragment) currentFragment).getNowPath());
				}
				break;
			case R.id.aurora_action_bar_all_search:
				if(currentFragment instanceof AuroraMainFragment){
					((AuroraMainFragment)currentFragment).showSearchviewLayout();
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * actionBar返回监听
	 */
	private OnAuroraActionBarBackItemClickListener actionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int item) {
			if (item == -1) {
				LogUtil.d(TAG, "-----------actionBarBackItemClickListener onBackPressed");
				onBackPressed();
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) {
			// if(currentFragment instanceof
			// AuroraMainFragment&&!isSearchviewLayoutShow()){
			// moveTaskToBack(false);
			// return super.onKeyDown(keyCode, event);
			// }
			LogUtil.d(TAG, "----KEYCODE_BACK");
			if ((currentFragment instanceof PictureFragment) || (currentFragment instanceof FileCategoryFragment)) {
				if (thread != null) {
					thread.interrupt();
				}
			}
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		LogUtil.d(TAG, "----onBackPressed");
		switch (currentType) {
		case AuroraConfig.AURORA_HOME_PAGE:
			currentFragment = (AuroraMainFragment) currentFragment;
			break;
		case AuroraConfig.AURORA_PIC_PAGE:
			currentFragment = (PictureCategoryFragment) currentFragment;
			break;
		case AuroraConfig.AURORA_CATEGORY_PAGE:
			currentFragment = (FileCategoryFragment) currentFragment;
			break;
		case AuroraConfig.AURORA_FILE_PAGE:
			currentFragment = (FileViewFragment) currentFragment;
			break;
		case AuroraConfig.AURORA_PIC_ITEM_PAGE:
			currentFragment = (PictureFragment) currentFragment;
			break;

		default:
			break;
		}
		// LogUtil.log(TAG, "currentType==" + currentType);
		IBackPressedListener backPressedListener = (IBackPressedListener) currentFragment;
		FileIconHelper.getInstance(FileExplorerTabActivity.this).stopLoading();// 停止正在loading
																				// icon
		if (backPressedListener != null && !backPressedListener.onBack()) {
			super.onBackPressed();
		}
	}

	// add by Jxh 2014-8-26 begin
	public boolean isOpenDir() {
		if ((currentFragment instanceof FileCategoryFragment) || (currentFragment instanceof AuroraMainFragment) || (currentFragment instanceof PictureFragment)) {
			return true;
		}
		return false;
	}

	// add by Jxh 2014-8-26 end
	@Override
	protected void onDestroy() {
		unregisterObserver(mFileContentObserver);
		unregisterObserver(mImageContentObserver);
		unregisterObserver(mVideoContentObserver);
		unregisterObserver(mAudioContentObserver);
		unregisterAuroraBroadcastReceiver();
		unRegisterPrivacyBroadcastReceiver();

		try {
			unbindService(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	/**
	 * 显示可编辑到actionBar
	 */
	public void showEditBarItem() {
		// 从其他APP跳入图片
		if (getIsFromOtherAPP()) {
			showEmptyBarItem();
			return;
		}
		LogUtil.d(TAG, "-----showEditBarItem isNoMenuKey:"+isNoMenuKey);
		if(isNoMenuKey){
			showActionBarItem(0, View.GONE);
			showActionBarItem(1, View.VISIBLE);
			showActionBarItem(2, View.GONE);
			showActionBarItem(3, View.GONE);
		}else {
			showActionBarItem(0, View.VISIBLE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
		}
		//add edit search more
	}

	/**
	 * 显示可编辑and 可添加到actionBar 
	 */
	public void showEditAndAddBarItem(){
		if (getIsFromOtherAPP()) {
			showEmptyBarItem();
			return;
		}
		if(isNoMenuKey){
			showActionBarItem(0, View.VISIBLE);
			showActionBarItem(1, View.VISIBLE);
			showActionBarItem(2, View.GONE);
			showActionBarItem(3, View.GONE);
		}else {
			showActionBarItem(0, View.VISIBLE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
		}
		//add edit search more
	}
	
	/**
	 * 显示可添加到actionBar 
	 */
	public void showAddBarItem(){
		if (getIsFromOtherAPP()) {
			showEmptyBarItem();
			return;
		}
		if(isNoMenuKey){
			showActionBarItem(0, View.VISIBLE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
			showActionBarItem(3, View.GONE);
		}else {
			showActionBarItem(0, View.VISIBLE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
		}
		//add edit search more
	}
	/**
	 * 显示更多actionBar 编辑按钮
	 */
	public void showMoreBarItem() {
		// 消除动画影响
//		Animation menuAnimation = AnimationUtils.loadAnimation(this, R.anim.aurora_action_dismiss_back_button);
//		auroraActionBar.getItem(1).getItemView().startAnimation(menuAnimation);
		if(isNoMenuKey){
			showActionBarItem(0, View.GONE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.VISIBLE);
			showActionBarItem(3, View.VISIBLE);
		}else {
			showActionBarItem(0, View.GONE);
			showActionBarItem(1, View.VISIBLE);
			showActionBarItem(2, View.VISIBLE);
		}
		//add edit search more
	}

	/**
	 * 隐藏actionBar编辑按钮
	 */
	public void showEmptyBarItem() {
		if (auroraActionBar == null) {
			LogUtil.e(TAG, "auroraActionBar ==null ");
			return;
		}
		if(isNoMenuKey){
			showActionBarItem(0, View.GONE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
			showActionBarItem(3, View.GONE);
		}else {
			showActionBarItem(0, View.GONE);
			showActionBarItem(1, View.GONE);
			showActionBarItem(2, View.GONE);
		}
		// 取消动画影响edit bar item
//		Animation menuAnimation = AnimationUtils.loadAnimation(this, R.anim.aurora_action_dismiss_back_button);
//		auroraActionBar.getItem(0).getItemView().startAnimation(menuAnimation);
//		auroraActionBar.getItem(1).getItemView().startAnimation(menuAnimation);
	}

	/**
	 * 显示ActionBar item
	 * @param position
	 * @param show
	 */
	public void showActionBarItem(int position, int show) {
		if (auroraActionBar == null) {
			LogUtil.e(TAG, "auroraActionBar ==null");
			return;
		}
		auroraActionBar.getItem(position).getItemView().setEnabled(show == View.GONE ? false : true);
		auroraActionBar.getItem(position).getItemView().setVisibility(show);
	}

	/**
	 * 设置fragment标记
	 * @param fragment
	 * @param type
	 */
	public void setCurrentFragment(Fragment fragment, int type) {
		try {
			// LogUtil.log(TAG, "fragment=" + fragment + " type=" + type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentFragment = fragment;
		currentType = type;
	}

	/**
	 * 显示fragment
	 * @param page
	 *            {@link AuroraConfig}
	 */
	private void showFragment(int page) {
		Fragment fragment = null;
		switch (page) {
		case AuroraConfig.AURORA_HOME_PAGE:
			FileIconHelper.getInstance(FileExplorerTabActivity.this).clearLoading();
			fragment = new AuroraMainFragment();
			break;
		case AuroraConfig.AURORA_PIC_PAGE:
			fragment = new PictureCategoryFragment();
			operationAction.setOperationInterfaceLisenter((PictureCategoryFragment) fragment);
			break;
		case AuroraConfig.AURORA_PIC_ITEM_PAGE:
			fragment = new PictureFragment();
			operationAction.setOperationInterfaceLisenter((PictureFragment) fragment);
			break;
		case AuroraConfig.AURORA_FILE_PAGE:
			fragment = new FileViewFragment();
			operationAction.setOperationInterfaceLisenter((FileViewFragment) fragment);
			break;
		case AuroraConfig.AURORA_CATEGORY_PAGE:
			fragment = new FileCategoryFragment();
			operationAction.setOperationInterfaceLisenter((FileCategoryFragment) fragment);
			break;
		}
		FragmentManager fragmentManager = getFragmentManager();
		Fragment old = fragmentManager.findFragmentById(R.id.container);
		if (old != null) {
			old.onDetach();
		}
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.commitAllowingStateLoss();
		setCurrentFragment(fragment, page);
	}

	@Override
	public void selectPage(int page, FileCategory fileCategory) {
		setNowfileCategory(fileCategory);
		showFragment(page);
	}

	/**
	 * 通过fragment获取哪一界面
	 * @param fragment
	 * @return
	 */
	public int getPageByFragment(Fragment fragment) {
		if (fragment instanceof FileCategoryFragment) {
			return AuroraConfig.AURORA_CATEGORY_PAGE;
		} else if (fragment instanceof PictureCategoryFragment) {
			return AuroraConfig.AURORA_PIC_PAGE;
		} else if (fragment instanceof PictureFragment) {
			return AuroraConfig.AURORA_PIC_ITEM_PAGE;
		} else if (fragment instanceof AuroraMainFragment) {
			return AuroraConfig.AURORA_HOME_PAGE;
		}
		return -1;
	}

	// add by Jxh 2014-8-26 begin
	/**
	 * 记录跳转前Fragment
	 */
	private Fragment gofragment;

	public Fragment getGofragment() {
		return gofragment;
	}

	public void setGofragment(Fragment gofragment) {
		this.gofragment = gofragment;
	}

	private FileCategory goCategory;

	public FileCategory getGoCategory() {
		return goCategory;
	}

	public void setGoCategory(FileCategory goCategory) {
		this.goCategory = goCategory;
	}

	// add by Jxh 2014-8-26 end

	/**
	 * 记录复制剪切前Fragment
	 */
	private Fragment cutOrCopyfragment;

	/**
	 * @return the cutOrCopyfragment
	 */
	public Fragment getCutOrCopyfragment() {
		return cutOrCopyfragment;
	}

	/**
	 * @param cutOrCopyfragment
	 *            the cutOrCopyfragment to set
	 */
	public void setCutOrCopyfragment(Fragment cutOrCopyfragment) {
		this.cutOrCopyfragment = cutOrCopyfragment;
	}

	private String cutOrCopyePath;

	/**
	 * @return the cutOrCopyePath
	 */
	public String getCutOrCopyePath() {
		return cutOrCopyePath;
	}

	/**
	 * @param cutOrCopyePath
	 *            the cutOrCopyePath to set
	 */
	public void setCutOrCopyePath(String cutOrCopyePath) {
		this.cutOrCopyePath = cutOrCopyePath;
	}

	private FileCategory cutOrCopyCategory;

	/**
	 * @return the cutOrCopyCategory
	 */
	public FileCategory getCutOrCopyCategory() {
		return cutOrCopyCategory;
	}

	/**
	 * @param cutOrCopyCategory
	 *            the cutOrCopyCategory to set
	 */
	public void setCutOrCopyCategory(FileCategory cutOrCopyCategory) {
		this.cutOrCopyCategory = cutOrCopyCategory;
	}

	/**
	 * 标志是否显示复制 剪切 确定取消按钮
	 */
	public boolean isShowOperationPane = false;

	/**
	 * 标志是否对隐私文件操作
	 */
	private boolean doPrivacy = false;

	public boolean isDoPrivacy() {
		return doPrivacy;
	}

	public void setDoPrivacy(boolean doPrivacy) {
		this.doPrivacy = doPrivacy;
	}

	public void doPrivacyAction() {
		if (currentFragment instanceof PictureFragment) {
			if (((PictureFragment) currentFragment).isPrivacy()) {
				setDoPrivacy(true);
			} else {
				setDoPrivacy(false);
			}
		} else if (currentFragment instanceof FileCategoryFragment) {
			if (((FileCategoryFragment) currentFragment).isPrivacyView()) {
				setDoPrivacy(true);
			} else {
				setDoPrivacy(false);
			}
		} else {
			setDoPrivacy(false);
		}
	}

	private OnAuroraMenuItemClickListener auroraSystemMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int item) {
			if (item == R.id.operation_folder) {// 新建文件夹操作
				if (currentFragment instanceof FileViewFragment) {
					// add by JXH BUG 5869 begin
					((AuroraFragment) currentFragment).auroraSetRubbishBack();
					// add by jxh end
					operationAction.createNewFolder(((FileViewFragment) currentFragment).getNowPath());
				}
			}
		}
	};

	/**
	 * actionBar监听回调 全部的增删改 {@link AuroraOperationBarMoreMenu.buttonClick}
	 */
	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int item) {
			switch (item) {
			case R.id.operation_delete:// pictureCategoryFragment 删除
				doPrivacyAction();
				operationAction.deleteFileOrDirBefor(true);
				break;
			case R.id.button_operation_delete:
				doPrivacyAction();
				operationAction.deleteFileOrDirBefor(true);
				break;
			case R.id.button_operation_copy:
				doPrivacyAction();
				setCutOrCopyfragment(currentFragment);
				setCutOrCopyCategory(getNowfileCategory());
				OperationAction.setLastOperation(Operation.copy);
				// LogUtil.log(TAG, OperationAction.getLastOperation() + "");
				if ((currentFragment instanceof PictureFragment)) {
					((PictureFragment) currentFragment).unDoOperation();
				} else if ((currentFragment instanceof FileCategoryFragment)) {
					((FileCategoryFragment) currentFragment).unCAllOperation();
				}
				if (!(currentFragment instanceof FileViewFragment)) {
					isShowOperationPane = true;
					selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				} else {
					setCutOrCopyePath(((FileViewFragment) currentFragment).getNowPath());
					((FileViewFragment) currentFragment).unAllOperationCopyOrCut();

				}
				break;
			case R.id.button_operation_move:
				doPrivacyAction();
				setCutOrCopyfragment(currentFragment);
				setCutOrCopyCategory(getNowfileCategory());
				OperationAction.setLastOperation(Operation.cut);
				// LogUtil.log(TAG, OperationAction.getLastOperation() + "");
				if ((currentFragment instanceof PictureFragment)) {
					((PictureFragment) currentFragment).unDoOperation();
				} else if ((currentFragment instanceof FileCategoryFragment)) {
					((FileCategoryFragment) currentFragment).unCAllOperation();
				}
				if (!(currentFragment instanceof FileViewFragment)) {
					isShowOperationPane = true;
					selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				} else {
					setCutOrCopyePath(((FileViewFragment) currentFragment).getNowPath());
					((FileViewFragment) currentFragment).unAllOperationCopyOrCut();

				}
				break;
			case R.id.button_operation_send:
				operationAction.shareOperation();
				break;
			case R.id.button_operation_more:
				if (!showOperationBarMoreMenu) {
					showAuroraOperationBarMoreMenu();
					/*addCoverView();
					View view = getCoverView();
					view.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if ((currentFragment instanceof PictureFragment)) {
								beforeDisMissAuroraOperationBarMoreMenu();
								return true;
							} else if ((currentFragment instanceof FileCategoryFragment)) {
								beforeDisMissAuroraOperationBarMoreMenu();
								return true;
							} else if ((currentFragment instanceof FileViewFragment)) {
								beforeDisMissAuroraOperationBarMoreMenu();
								return true;
							}
							return false;
						}
					});*/
				} else {
					beforeDisMissAuroraOperationBarMoreMenu();
				}
				break;
			case R.id.operation_add:
				if (currentFragment instanceof FileCategoryFragment) {
					((FileCategoryFragment) currentFragment).sendMoreVideo();
				}
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 搜索跳转到指定路径
	 * @param path
	 */
	public void showFileViewPath(String path) {
		setSearchPath(path);// 设置跳转路径
		selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
	}

	// add by Jxh 2014-8-26 begin
	/**
	 * 跳转到指定路径
	 * @param path
	 */
	public void showFileViewPathByDialoh(String path) {
		setGoPath(path);
		selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
	}

	private String goPath;

	public String getGoPath() {
		return goPath;
	}

	public void setGoPath(String goPath) {
		this.goPath = goPath;
	}

	private String goFilePath;

	public String getGoFilePath() {
		return goFilePath;
	}

	public void setGoFilePath(String goFilePath) {
		this.goFilePath = goFilePath;
	}

	// add by Jxh 2014-8-26 end
	/**
	 * 显示存储器不可用界面
	 */
	public boolean showSdNotAvailableView() {
		if (ROOT_PATH == null || mSDCardPath == null) {
			SdNotAvailableFragment fragment = new SdNotAvailableFragment();
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.container, fragment);
			transaction.commitAllowingStateLoss();
			return true;
		}
		return false;
	}

	/**
	 * 获取所有存储路径
	 */
	public void notifiedStateChanged() {

		mSDCardPath = gionee.os.storage.GnStorageManager.getInstance(FileExplorerTabActivity.this.getApplicationContext()).getInternalStoragePath();
		if (mSDCardPath != null) {
			ROOT_PATH = Util.getPathFromFilepath(mSDCardPath);
		}

		/* 获取系统支持存储路径 start */
		StorageVolume[] storageVolume = mStorageManager.getVolumeList();
		storages.clear();
		storagesStrings.clear();
		allStorages.clear();
		int k = 0;
		for (int i = 0; i < storageVolume.length; i++) {
			String temp = storageVolume[i].getPath();
			File file = new File(temp);
			try {
				// LogUtil.log(TAG, "all sd(" + i + ")==" + temp);
				if (Util.sdIsMounted(temp)) {
					LogUtil.d(TAG, "use sd :" + temp);
					storages.add(Util.getSimpleFileInfo(file));
					storagesStrings.add(temp);

					if (k == 1) {
						mSDCard2Path = temp;
					}
					k++;
				}
				allStorages.add(temp);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "sd info " + e.getMessage());
			}
		}
		if (storages.size() <= 1) {
			mSDCard2Path = null;
		}

		/* 获取系统支持存储路径 end */
		showSdinfo();
		if ((currentFragment instanceof FileViewFragment)) {
			String FileViewPath = ((FileViewFragment) currentFragment).getNowPath();
			if (FileViewPath != null && ROOT_PATH != null && (getStoragesStrings().contains(FileViewPath) || FileViewPath.equals(ROOT_PATH))) {
				((FileViewFragment) currentFragment).updataRootPaths();
			} else {
				backHome();
			}
		}

	}

	private void backHome() {
		if (getRootPath() != null && !Util.sdIsMounted(getRootPath())) {
			setMenuEnable(false);
			// LogUtil.elog(TAG, "back home");
			((FileViewFragment) currentFragment).cancelTask();// 如果在根目录
			if (((FileViewFragment) currentFragment).isOperationFile()) {
				((FileViewFragment) currentFragment).unAllOperation();
			}
			// 依然取消任务
			selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
		}
	}

	private Intent auroraFromPicIntent;
	private boolean isFromOtherAppBefore;

	/**
	 * @return the auroraFromPicIntent
	 */
	public Intent getAuroraFromPicIntent() {
		return auroraFromPicIntent;
	}

	/**
	 * @param auroraFromPicIntent
	 *            the auroraFromPicIntent to set
	 */
	public void setAuroraFromPicIntent(Intent auroraFromPicIntent) {
		this.auroraFromPicIntent = auroraFromPicIntent;
	}

	/**
	 * 其他APP进入文官跳转
	 * @return
	 */
	private boolean getOtherIntent() {
		isFromPicManager = false;
		isFromWallpaperManager = false;
		isFromAudioManager = false;
		isFromOtherAPP = false;
		if (auroraFromPicIntent != null) {
			Bundle auroraBundle = auroraFromPicIntent.getExtras();
			if (auroraBundle != null) {
				isFromPicManager = (Boolean) auroraBundle.get(AuroraConfig.AURORA_PIC_MANAGER);
				isFromWallpaperManager = (Boolean) auroraBundle.get(AuroraConfig.AURORA_WALLPAPER_MANAGER);
				isFromWallpaperLocalManager = (Boolean) auroraBundle.get(AuroraConfig.AURORA_WALLPAPER_CHANGE);
				isFromWallpaperFileType = (String) auroraBundle.get(AuroraConfig.AURORA_WALLPAPER_TYPE);
				isFromAudioManager = (Boolean) auroraBundle.get(AuroraConfig.AURORA_AUDIO_MANAGER);
				if ((isFromPicManager != null && isFromPicManager) || (isFromWallpaperManager != null && isFromWallpaperManager)) {// 其他路口进入到图片预览界面
					hideSearchviewLayout();// 隐藏搜索框
					unDoAllOperation();
					if (mSDCardPath == null || ROOT_PATH == null) {
						showSdNotAvailableView();
						return true;
					}
					showEmptyBarItem();
					selectPage(AuroraConfig.AURORA_PIC_PAGE, null);
					isFromOtherAppBefore = true;
					return true;
				} else if (isFromAudioManager != null && isFromAudioManager) {// 通话录音人口
					hideSearchviewLayout();// 隐藏搜索框
					unDoAllOperation();
					if (mSDCardPath == null || ROOT_PATH == null) {
						showSdNotAvailableView();
						return true;
					}
					showEmptyBarItem();
					String path = mSDCardPath + File.separator + getResourceString(R.string.phone_audios);
					File file = new File(path);
					if (!file.exists() || file.isFile()) {
						file.mkdirs();
					}
					showFileViewPath(path);
					isFromOtherAppBefore = true;
					return true;
				}
			}
			// 获取单张图片入口
			if (auroraFromPicIntent.getAction() == Intent.ACTION_GET_CONTENT || auroraFromPicIntent.getAction() == Intent.ACTION_PICK
					|| auroraFromPicIntent.getAction() == AuroraConfig.ACTION_SINGLE_GET_CONTENT) {
				hideSearchviewLayout();// 隐藏搜索框
				unDoAllOperation();
				showEmptyBarItem();
				selectPage(AuroraConfig.AURORA_PIC_PAGE, null);
				isFromOtherAppBefore = true;
				isFromOtherAPP = true;
				return true;
				// 获取多张图片入口
			} else if ((auroraFromPicIntent.getAction() == AuroraConfig.ACTION_MORE_GET_CONTENT) || (auroraFromPicIntent.getAction() == AuroraConfig.ACTION_MORE_PRI_GET_CONTENT)) {
				hideSearchviewLayout();// 隐藏搜索框
				unDoAllOperation();
				showEmptyBarItem();
				selectPage(AuroraConfig.AURORA_PIC_PAGE, null);
				isFromOtherAppBefore = true;
				isFromOtherAPP = true;
				isGetMorePic = true;
				Bundle bundle = auroraFromPicIntent.getExtras();
				if (bundle != null) {
					int s = bundle.getInt("size");
					if (s != 0) {
						imageSize = s;
					}
					setIsGetMoreNoPrivacy(bundle.getBoolean("noPriacy"));
				}
				return true;
				// 获取多个视频入口
			} else if (auroraFromPicIntent.getAction() == AuroraConfig.ACTION_MORE_VIDEO_CONTENT) {
				hideSearchviewLayout();// 隐藏搜索框
				unDoAllOperation();
				showEmptyBarItem();
				selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Video);
				isFromOtherAppBefore = true;
				isFromOtherAPP = true;
				isGetMoreVideo = true;
				Bundle bundle = auroraFromPicIntent.getExtras();
				if (bundle != null) {
					int s = bundle.getInt("size");
					if (s != 0) {
						videoSize = s;
					}
					setIsGetMoreNoPrivacy(bundle.getBoolean("noPriacy"));
				}
				return true;
			} else if (auroraFromPicIntent.getAction() == AuroraConfig.ACTION_FILE_GET_CONTENT) {
				hideSearchviewLayout();// 隐藏搜索框
				unDoAllOperation();
				showEmptyBarItem();
				isFromOtherAppBefore = true;
				isFromOtherAPP = true;
				isGetFileFromSdList = true;
				setRootPath(FileExplorerTabActivity.getROOT_PATH());
				selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				return true;
			}

		}
		return false;
	}

	private int imageSize = 100;

	private int videoSize = 100;

	public int getVideoSize() {
		return videoSize;
	}

	/**
	 * @return the imageSize
	 */
	public int getImageSize() {
		return imageSize;
	}

	/**
	 * 其他APP
	 */
	public void unDoAllOperation() {
		if (currentFragment instanceof FileViewFragment) {
			((FileViewFragment) currentFragment).unAllOperation();
		} else if (currentFragment instanceof FileCategoryFragment) {
			((FileCategoryFragment) currentFragment).unAllOperation();
		} else if (currentFragment instanceof PictureFragment) {
			((PictureFragment) currentFragment).unDoOperation();
		} else if (currentFragment instanceof PictureCategoryFragment) {
			((PictureCategoryFragment) currentFragment).unDoOperation();
		}
	}

	/**
	 * 壁纸到文管 废弃
	 */
	public void startFromOtherApp() {
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent();
		try {
			if (getIsFromWallpaperLocalManager() || getIsFromWallpaperManager()) {
				if (isFromWallpaperFileType != null && AuroraConfig.AURORA_WALLPAPER_TYPE_1.equals(isFromWallpaperFileType)) {
					intent.setClassName(AuroraConfig.AURORA_WALLPAPER_CHANGE, AuroraConfig.AURORA_WALLPAPER_CHANGE_ACTIVITY);// 桌面第二个
				} else if (isFromWallpaperFileType != null && AuroraConfig.AURORA_WALLPAPER_TYPE_2.equals(isFromWallpaperFileType)) {
					intent.setClassName(AuroraConfig.AURORA_WALLPAPER_CHANGE, AuroraConfig.AURORA_WALLPAPER_CHANGE_ACTIVITY_2);// 第二个
				}

				Bundle bundle = new Bundle();
				if (getPicPath() != null) {
					bundle.putString("folderPath", getPicPath());
					bundle.putSerializable("files", (Serializable) picItemList);
					intent.putExtras(bundle);
				}

			}
			startActivity(intent);
			super.finish();
			overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
	}

	@Override
	public void finish() {
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent();
		ActivityOptions opts = ActivityOptions.makeCustomAnimation(this, R.anim.activity_close_enter, R.anim.activity_close_exit);
		if (isFromWallpaperManager != null && isFromWallpaperManager) {
			if (isFromWallpaperLocalManager != null && isFromWallpaperLocalManager) {
				try {
					if (isFromWallpaperFileType != null && AuroraConfig.AURORA_WALLPAPER_TYPE_1.equals(isFromWallpaperFileType)) {
						intent.setClassName(AuroraConfig.AURORA_WALLPAPER_CHANGE, AuroraConfig.AURORA_WALLPAPER_CHANGE_ACTIVITY_3);//
					} else if (isFromWallpaperFileType != null && AuroraConfig.AURORA_WALLPAPER_TYPE_2.equals(isFromWallpaperFileType)) {
						intent.setClassName(AuroraConfig.AURORA_WALLPAPER_CHANGE, AuroraConfig.AURORA_WALLPAPER_CHANGE_ACTIVITY_4);//
					}
					startActivity(intent, opts.toBundle());
					super.finish();

				} catch (Exception e) {
					Log.i(TAG, e.getMessage());
				}
			}
		} else if (isFromPicManager != null && isFromPicManager) {
			super.finish();
			overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
		}
		super.finish();
		// overridePendingTransition(R.anim.activity_close_enter,
		// R.anim.activity_close_exit);
	}

	/**
	 * 显示SD卡信息
	 */
	public void showSdinfo() {
		if (currentFragment instanceof AuroraMainFragment) {
			if (mSDCard2Path != null) {
				((AuroraMainFragment) currentFragment).showSdcard(true);
				for (String sdPath : getStoragesStrings()) {
					((AuroraMainFragment) currentFragment).setCardInfo(sdPath);
				}
			} else {
				((AuroraMainFragment) currentFragment).showSdcard(false);
				((AuroraMainFragment) currentFragment).setCardInfo(FileExplorerTabActivity.getmSDCardPath());
			}
		}
	}

	/**
	 * @return the mSDCardPath
	 */
	public static String getmSDCardPath() {
		if (TextUtils.isEmpty(mSDCardPath)) {
			return "";
		}
		return mSDCardPath;
	}

	/**
	 * @return the mSDCard2Path
	 */
	public static String getmSDCard2Path() {
		if (TextUtils.isEmpty(mSDCard2Path)) {
			return "";
		}
		return mSDCard2Path;
	}

	/**
	 * @return the ROOT_PATH
	 */
	public static String getROOT_PATH() {
		if (TextUtils.isEmpty(ROOT_PATH)) {
			return "";
		}
		return ROOT_PATH;
	}

	/**
	 * 设置auroraActionBar title
	 * @param rid
	 */
	public void setAuroraActionBarTitle(int rid) {
		if (auroraActionBar != null) {
			auroraActionBar.setTitle(getResourceString(rid));
			if (rid != R.string.aurora_file_manager_title) {
				auroraActionBar.getHomeButton().setVisibility(View.VISIBLE);
				setAuroraActionBarBtn(true);
			} else {
				auroraActionBar.getHomeButton().setVisibility(View.GONE);
				setAuroraActionBarBtn(false);
			}
		}
	}

	/**
	 * 设置auroraActionBar title
	 * @param title
	 */
	public void setAuroraActionBarTitle(String title) {
		if (auroraActionBar != null) {
			CharSequence barTitle = Util.getEllipsizeEnd(title,
					auroraActionBar.getTitleView(), actionBarTitleLen);
			auroraActionBar.setTitle(barTitle);
			auroraActionBar.getHomeButton().setVisibility(View.VISIBLE);
			setAuroraActionBarBtn(true);
		}
	}

	/**
	 * 设置auroraActionBar 是否可用返回
	 * @param enable
	 *            true 可以返回 false 不能
	 */
	public void setAuroraActionBarBtn(boolean enable) {
		if (auroraActionBar != null) {
			auroraActionBar.setDisplayHomeAsUpEnabled(enable);
		}
	}

	public String getResourceString(int resId) {
		return getResources().getString(resId);
	}

	/**
	 * @return the picItemList
	 */
	public List<FileInfo> getPicItemList() {
		return picItemList;
	}

	/**
	 * @param picItemList
	 *            the picItemList to set
	 */
	public void setPicItemList(List<FileInfo> picItemList) {
		this.picItemList = picItemList;
	}

	private View rootView;
	public boolean showOperationBarMoreMenu = false;
	

	public boolean isShowOperationBarMoreMenu() {
		return showOperationBarMoreMenu;
	}

	/**
	 * @param rootView
	 *            the rootView to set
	 */
	public void setRootView(View rootView) {
		this.rootView = rootView;
	}

	private boolean isHideOperationBarMoreMenu;

	public boolean isHideOperationBarMoreMenu() {
		return isHideOperationBarMoreMenu;
	}

	public void setHideOperationBarMoreMenu(boolean isHideOperationBarMoreMenu) {
		this.isHideOperationBarMoreMenu = isHideOperationBarMoreMenu;
	}

	/**
	 * 显示more 菜单
	 */
	public void showAuroraOperationBarMoreMenu() {
		// add By Jxh 2014-9-10 begin
		auroraOperationBarMoreMenu.showPrivacyView(isHideOperationBarMoreMenu());
		// LogUtil.log(TAG, "showPrivacyView " + isHideOperationBarMoreMenu);
		// add By Jxh 2014-9-10 end
		auroraOperationBarMoreMenu.showAtLocation(rootView, Gravity.RIGHT | Gravity.BOTTOM, 0, this.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_action_bottom_bar_height));
		showOperationBarMoreMenu = true;
		AuroraMenu auroraMenu = auroraActionBar.getAuroraActionBottomBarMenu();
		auroraMenu.setBottomMenuItemEnable(0, false);
		auroraMenu.setBottomMenuItemEnable(1, false);
		auroraMenu.setBottomMenuItemEnable(2, false);
		auroraMenu.setBottomMenuItemEnable(3, false);
		auroraOperationBarMoreMenu.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				showOperationBarMoreMenu = false;
				AuroraMenu auroraMenu = auroraActionBar.getAuroraActionBottomBarMenu();
				auroraMenu.setBottomMenuItemEnable(0, true);
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				if (currentFragment instanceof FileViewFragment) {
					((FileViewFragment) currentFragment).updateAuroraItemBottomBarState();
				}
			}
		});
	}

	/**
	 * 隐藏more菜单 取消蒙板状态
	 */
	public void beforeDisMissAuroraOperationBarMoreMenu() {
//		removeCoverView();
		auroraOperationBarMoreMenu.dismiss();
		
	}

	/**
	 * 把路径转换为中文名词
	 * @param rootPath
	 * @return
	 */
	public String getStorageName(String rootPath) {
		if (rootPath == null) {
			return "";
		}
		if (mSDCardPath != null && rootPath.startsWith(mSDCardPath)) {
			return rootPath.replace(mSDCardPath, getResourceString(R.string.root_view) + File.separator + getResourceString(R.string.storage_internal));
		} else if (ROOT_PATH != null && rootPath.endsWith(ROOT_PATH)) {
			return rootPath.replace(rootPath, getResourceString(R.string.root_view) + File.separator + getResourceString(R.string.storage_external));
		} else if (mSDCard2Path != null) {
			String sdRoot = "";
			for (int i = 0; i < getStoragesStrings().size(); i++) {
				if (rootPath.startsWith(getStoragesStrings().get(i))) {
					sdRoot = getStoragesStrings().get(i);
					// LogUtil.elog(TAG, "sdRoot 0==" + sdRoot);
					break;
				}
			}
			// LogUtil.log(TAG, "sdRoot==" + sdRoot);
			return rootPath.replace(sdRoot, getResourceString(R.string.root_view) + File.separator + getResourceString(R.string.storage_external));
		} else if (ROOT_PATH != null && rootPath.startsWith(ROOT_PATH)) {
			return rootPath.replace(ROOT_PATH, getResourceString(R.string.root_view));
		} else {
			return rootPath;
		}

	}

	/**
	 * 获取储存根目录 子文件夹名称
	 * @param rootPath
	 * @return
	 */
	public String getStorageNodeName(String rootPath) {
		if (rootPath == null) {
			return "";
		}
		if (mSDCardPath != null && rootPath.startsWith(mSDCardPath)) {
			return rootPath.replace(mSDCardPath, getResourceString(R.string.storage_internal));
		} else {
			String[] sd = rootPath.split(File.separator);
			String name = "";
			if (sd.length >= 3) {
				name = sd[2].substring(0, 3);
			}
			return name + "-" + getResourceString(R.string.storage_external);
		}
	}

	/**
	 * 中文路径转换
	 * @param name
	 * @return
	 */
	public String getStoragePath(String name) {
		if (name == null) {
			return "";
		}
		String internal = getResourceString(R.string.root_view) + File.separator + getResourceString(R.string.storage_internal);
		String external = getResourceString(R.string.root_view) + File.separator + getResourceString(R.string.storage_external);
		String rootName = getResourceString(R.string.root_view);
		if (name.startsWith(internal) && !TextUtils.isEmpty(mSDCardPath)) {
			return name.replace(internal, mSDCardPath);
		} else if (external.startsWith(external) && !TextUtils.isEmpty(mSDCard2Path)) {
			return name.replace(external, mSDCard2Path);
		} else if (name.startsWith(rootName) && !TextUtils.isEmpty(ROOT_PATH)) {
			return name.replace(rootName, ROOT_PATH);
		} else {
			return name;
		}

	}

	/**
	 * 注册contentObserver
	 * @param uri
	 * @param contentObserver
	 */
	private void registerContentObserver(Uri uri, ContentObserver contentObserver) {
		getContentResolver().registerContentObserver(uri, false, contentObserver);
	}
	/**
	 * 注册contentObserver
	 * @param uri
	 * @param contentObserver
	 */
	private void registerContentObserver(Uri uri, ContentObserver contentObserver,boolean notifyForDescendents) {
		getContentResolver().registerContentObserver(uri, notifyForDescendents, contentObserver);
	}

	/**
	 * 注销contentObserver
	 * @param contentObserver
	 */
	private void unregisterObserver(ContentObserver contentObserver) {
		getContentResolver().unregisterContentObserver(contentObserver);
	}

	private static final int IMAGE = 1023;
	private static final int VIDEO = 1024;
	private static final int AUDIO = 1025;
	private static final int FILE = 1026;
	private static final int DOWNLOAD = 1027;
	private static final int delayMillis = 200;
	private static final int FILEVIEW = 1028;
	private static final int SCAN = 10023;
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// LogUtil.log(TAG, "OperationAction=="
			// + OperationAction.getLastOperation().toString());
			switch (msg.what) {
			case IMAGE:
				 LogUtil.d(TAG, "refresh mImageContentObserver");
				if (currentFragment instanceof AuroraMainFragment) {
					LogUtil.d(TAG, "refresh AuroraMainFragment mImageContentObserver");
					refreshCategoryInfo(FileCategory.Picture);
				}
				if (OperationAction.getLastOperation() != Operation.noOperation) {
					return;
				}
				if (currentFragment instanceof PictureCategoryFragment) {
					LogUtil.d(TAG, "refresh PictureCategoryFragment ");
					((PictureCategoryFragment) currentFragment).refreshCategoryPics();
				} else if (currentFragment instanceof PictureFragment) {
					LogUtil.d(TAG, "refresh PictureFragment");
					((PictureFragment) currentFragment).refreshCategoryPics();
				}
				break;
			case VIDEO:
				// LogUtil.log(TAG, "refresh mVideoContentObserver");
				if (currentFragment instanceof AuroraMainFragment) {
					// LogUtil.log(TAG,
					// "refresh AuroraMainFragment mVideoContentObserver");
					refreshCategoryInfo(FileCategory.Video);
				}
				if (OperationAction.getLastOperation() != Operation.noOperation) {
					return;
				}
				if ((currentFragment instanceof FileCategoryFragment) && (nowfileCategory.equals(FileCategory.Video))) {
					LogUtil.d(TAG, "refresh FileCategoryFragment Video");
					if (!((FileCategoryFragment) currentFragment).isPrivacyView()) {
						((FileCategoryFragment) currentFragment).refreshCategoryInfo(nowfileCategory);
					}
				}
				break;
			case AUDIO:
				// LogUtil.log(TAG, "refresh mAudioContentObserver ");
				if (currentFragment instanceof AuroraMainFragment) {
					// LogUtil.log(TAG,
					// "refresh AuroraMainFragment mAudioContentObserver");
					refreshCategoryInfo(FileCategory.Music);
					// ((AuroraMainFragment) currentFragment)
					// .refreshCategoryInfo(FileCategory.Music);
				}
				if (OperationAction.getLastOperation() != Operation.noOperation) {
					return;
				}
				if ((currentFragment instanceof FileCategoryFragment) && (nowfileCategory.equals(FileCategory.Music))) {
					// LogUtil.log(TAG, "refresh FileCategoryFragment music");
					((FileCategoryFragment) currentFragment).refreshCategoryInfo(nowfileCategory);
				}
				break;
			case FILE:
				// LogUtil.log(TAG, "refresh mFileContentObserver");
				if (currentFragment instanceof AuroraMainFragment) {
					// LogUtil.d(TAG,
					// "refresh AuroraMainFragment mFileContentObserver ");
					refreshCategoryInfo(FileCategory.Doc);
					refreshCategoryInfo(FileCategory.Apk);
				}
				if (OperationAction.getLastOperation() != Operation.noOperation) {
					return;
				}
				if ((currentFragment instanceof FileCategoryFragment) && ((nowfileCategory.equals(FileCategory.Apk) || (nowfileCategory.equals(FileCategory.Doc))))) {
					LogUtil.d(TAG, "refresh FileCategoryFragment doc apk uri");
					((FileCategoryFragment) currentFragment).refreshCategoryInfo(nowfileCategory);
				}
				break;
			case DOWNLOAD:
				if (currentFragment instanceof AuroraMainFragment) {
					// LogUtil
					// .log(TAG,
					// "refresh AuroraMainFragment mDownloadContentObserver ");
					refreshCategoryInfo(FileCategory.DownLoad);
				}
				break;
			case FILEVIEW:
				if (currentFragment instanceof FileViewFragment) {
					if (OperationAction.getLastOperation() != Operation.noOperation) {
						return;
					}
					// LogUtil.log(TAG, "refreshListData()");
					((FileViewFragment) currentFragment).refreshListData();
				}
				break;
			case SCAN:
				if(currentFragment instanceof FileViewFragment){
					if (OperationAction.getLastOperation() != Operation.noOperation) {
						return;
					}
					if(((FileViewFragment)currentFragment).isMovingOperationBar()){
						return;
					}
				}
				OperationAction.setLastOperation(Operation.noOperation);
				break;

			default:
				break;
			}
		}

	};

	/**
	 * file ContentObserver
	 */
	private ContentObserver mFileContentObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			// LogUtil.log(TAG, "refresh mFileContentObserver before " + uri);
			// Toast.makeText(getApplicationContext(), "uri=" + uri, 0).show();
			if (operationAction != null && operationAction.isCreateFolder()) {
				return;
			}
			fileContentObserverAction();

		}
	};
	private String fUri = Util.getPathFromFilepath(Files.getContentUri(FileCategoryHelper.volumeName).toString());
	private ContentObserver mImageContentObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			LogUtil.d(TAG, "refresh mImageContentObserver before " );
			imageContentObserverAction();
		}
	};

	private ContentObserver mVideoContentObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			LogUtil.d(TAG, "refresh mVideoContentObserver before ");
			videoContentObserverAction();
		}
	};

	private ContentObserver mAudioContentObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			LogUtil.d(TAG, "refresh mAudioContentObserver before " + uri);
			audioContentObserverAction();
		}
	};

	private void audioContentObserverAction() {
		if (handler == null) {
			return;
		}
		if (!getIsFromOtherAPP() && isStop) {
			Util.saveRefreshCategory(getApplication(), FileCategory.Music, true);
			return;
		}
		// LogUtil.log(TAG, "audioContentObserverAction");
		handler.removeMessages(AUDIO);
		Message msg = handler.obtainMessage(AUDIO);
		handler.sendMessageDelayed(msg, delayMillis);
	}

	private void videoContentObserverAction() {
		if (handler == null) {
			return;
		}
		if (!getIsFromOtherAPP() && isStop) {
			Util.saveRefreshCategory(getApplication(), FileCategory.Video, true);
			return;
		}
		// LogUtil.log(TAG, "videoContentObserverAction");
		handler.removeMessages(VIDEO);
		Message msg = handler.obtainMessage(VIDEO);
		handler.sendMessageDelayed(msg, delayMillis);
	}

	private void imageContentObserverAction() {
		if (handler == null) {
			return;
		}
		if (!getIsFromOtherAPP() && isStop) {
			Util.saveRefreshCategory(getApplication(), FileCategory.Picture, true);
			return;
		}
		clearCache();
		// LogUtil.d(TAG, "imageContentObserverAction");
		handler.removeMessages(IMAGE);
		Message msg = handler.obtainMessage(IMAGE);
		handler.sendMessageDelayed(msg, delayMillis);
	}

	private void fileContentObserverAction() {
		if (handler == null) {
			return;
		}
		if (!getIsFromOtherAPP() && isStop) {
			Util.saveRefreshCategory(getApplication(), FileCategory.Doc, true);
			return;
		}
		// LogUtil.log(TAG, "fileContentObserverAction");
		handler.removeMessages(FILE);
		Message msg = handler.obtainMessage(FILE);
		handler.sendMessageDelayed(msg, delayMillis);
	}

	private AuroraBroadcastReceiver auroraBroadcastReceiver;

	private void registerAuroraBroadcastReceiver() {
		auroraBroadcastReceiver = new AuroraBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 插入OTG
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// 拔出OTG
		intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);// 插入OTG
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);// 拔出OTG
		// intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addDataScheme("file");
		registerReceiver(auroraBroadcastReceiver, intentFilter);
	}

	private void unregisterAuroraBroadcastReceiver() {
		if (auroraBroadcastReceiver != null) {
			unregisterReceiver(auroraBroadcastReceiver);
		}
	}

	/**
	 * 所有广播接收者
	 * @author jiangxh
	 * @CreateTime 2014年5月22日 上午11:48:04
	 * @Description com.aurora.filemanager FileExplorerTabActivity.java
	 */
	public final class AuroraBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			LogUtil.d(TAG, "AuroraBroadcastReceiver:" + action);
			if (storagesStrings.size() <= 0) {
				return;
			}
			lastStorage = storagesStrings.get(storagesStrings.size() - 1);
			// Toast.makeText(context, action, 1).show();
			if (action.equals(Intent.ACTION_MEDIA_CHECKING) || action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {// 插拔OTG
				notifiedStateChanged();
				if (ROOT_PATH == null || mSDCardPath == null) {// 存储器不可用
					finish();
				}
			}

			if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				if ((currentFragment instanceof FileViewFragment)) {// 触发多个扫描时
																	// 可能导致复制和剪切状态改变
					if (((FileViewFragment) currentFragment).moving_operation_bar != null && (((FileViewFragment) currentFragment).moving_operation_bar.getVisibility() == View.VISIBLE)) {
						// 处于复制和剪切状态
					} else {
						// 扫描完成 重置操作状态 cut copy rename
						handler.removeMessages(SCAN);
						Message msg = handler.obtainMessage(SCAN);
						handler.sendMessageDelayed(msg, delayMillis * 2);
					}
				} else {
					// LogUtil.log(TAG, "扫描完成 重置操作状态 cut copy rename");
					// 扫描完成 重置操作状态 cut copy rename
					// OperationAction.setLastOperation(Operation.noOperation);
					handler.removeMessages(SCAN);
					Message msg = handler.obtainMessage(SCAN);
					handler.sendMessageDelayed(msg, delayMillis * 2);
				}

			}
		}
	}

	/**
	 * @return the nowfileCategory
	 */
	public FileCategory getNowfileCategory() {
		if (nowfileCategory == null) {
			return FileCategory.Doc;
		}
		return nowfileCategory;
	}

	/**
	 * @param nowfileCategory
	 *            the nowfileCategory to set
	 */
	public void setNowfileCategory(FileCategory nowfileCategory) {
		this.nowfileCategory = nowfileCategory;
	}

	/**
	 * @return the isFromPicManager
	 */
	public Boolean getIsFromPicManager() {
		return isFromPicManager;
	}

	/**
	 * @param isFromPicManager
	 *            the isFromPicManager to set
	 */
	public void setIsFromPicManager(Boolean isFromPicManager) {
		this.isFromPicManager = isFromPicManager;
	}

	/**
	 * @return the isFromWallpaperManager
	 */
	public Boolean getIsFromWallpaperManager() {
		return isFromWallpaperManager;
	}

	/**
	 * @param isFromWallpaperManager
	 *            the isFromWallpaperManager to set
	 */
	public void setIsFromWallpaperManager(Boolean isFromWallpaperManager) {
		this.isFromWallpaperManager = isFromWallpaperManager;
	}

	/**
	 * @return the isFromWallpaperLocalManager
	 */
	public Boolean getIsFromWallpaperLocalManager() {
		return isFromWallpaperLocalManager;
	}

	/**
	 * @param isFromWallpaperLocalManager
	 *            the isFromWallpaperLocalManager to set
	 */
	public void setIsFromWallpaperLocalManager(Boolean isFromWallpaperLocalManager) {
		this.isFromWallpaperLocalManager = isFromWallpaperLocalManager;
	}

	/**
	 * @return the isFromAudioManager
	 */
	public Boolean getIsFromAudioManager() {
		if (isFromAudioManager == null) {
			isFromAudioManager = false;
		}
		return isFromAudioManager;
	}

	/**
	 * @param isFromAudioManager
	 *            the isFromAudioManager to set
	 */
	public void setIsFromAudioManager(Boolean isFromAudioManager) {
		this.isFromAudioManager = isFromAudioManager;
	}

	/**
	 * @return the isFromWallpaperFileType
	 */
	public String getIsFromWallpaperFileType() {
		return isFromWallpaperFileType;
	}

	/**
	 * @param isFromWallpaperFileType
	 *            the isFromWallpaperFileType to set
	 */
	public void setIsFromWallpaperFileType(String isFromWallpaperFileType) {
		this.isFromWallpaperFileType = isFromWallpaperFileType;
	}

	/**
	 * @return the isFromOtherAPP
	 */
	public Boolean getIsFromOtherAPP() {
		if (isFromOtherAPP == null) {
			isFromOtherAPP = false;
		}
		return isFromOtherAPP;
	}

	/**
	 * @param isFromOtherAPP
	 *            the isFromOtherAPP to set
	 */
	public void setIsFromOtherAPP(Boolean isFromOtherAPP) {
		this.isFromOtherAPP = isFromOtherAPP;
	}

	/**
	 * @return the isGetMorePic
	 */
	public Boolean getIsGetMorePic() {
		if (isGetMorePic == null) {
			isGetMorePic = false;
		}
		return isGetMorePic;
	}

	/**
	 * @param isGetMorePic
	 *            the isGetMorePic to set
	 */
	public void setIsGetMorePic(Boolean isGetMorePic) {
		this.isGetMorePic = isGetMorePic;
	}

	public Boolean getIsGetMoreVideo() {
		if (isGetMoreVideo == null) {
			isGetMoreVideo = false;
		}
		return isGetMoreVideo;
	}

	public void setIsGetMoreVideo(Boolean isGetMoreVideo) {
		this.isGetMoreVideo = isGetMoreVideo;
	}

	public Boolean getIsGetMoreNoPrivacy() {
		if (isGetMoreNoPrivacy == null) {
			isGetMoreNoPrivacy = false;
		}
		return isGetMoreNoPrivacy;
	}

	public void setIsGetMoreNoPrivacy(Boolean isGetMoreNoPrivacy) {
		this.isGetMoreNoPrivacy = isGetMoreNoPrivacy;
	}

	public boolean isGetFileFromSdList() {
		return isGetFileFromSdList;
	}

	public void setGetFileFromSdList(boolean isGetFileFromSdList) {
		this.isGetFileFromSdList = isGetFileFromSdList;
	}

	/** 文件监听开始 **/
	private AuroraFileObserver auroraFileObserver;

	public void setAuroraObserver(String path) {

		if (auroraFileObserver != null) {
			auroraFileObserver.stopWatching();
			auroraFileObserver = null;
			if (path == null) {
				return;
			}
		}
		// LogUtil.log(TAG, "FileObserver path==" + path);
		auroraFileObserver = new AuroraFileObserver(path, getApplicationContext());
		auroraFileObserver.setFileObserverListener(FileExplorerTabActivity.this);
		auroraFileObserver.startWatching();
	}

	@Override
	public void onFileCreated(String path) {
		// LogUtil.log(TAG, "onFileCreated ");
		handler.removeMessages(FILEVIEW);
		handler.sendEmptyMessageDelayed(FILEVIEW, 2 * delayMillis);

	}

	@Override
	public void onFileDeleted(String path) {

		// add by JXH 2014-7-10 begin
		if (currentFragment instanceof FileViewFragment) {
			if (((FileViewFragment) currentFragment).getIsRoot()) {
				return;
			}
		}
		// add by JXH 2014-7-10 end
		if (OperationAction.getLastOperation() != Operation.noOperation) {
			return;
		}
		// LogUtil.log(TAG, "onFileDeleted ");
		handler.removeMessages(FILEVIEW);
		handler.sendEmptyMessageDelayed(FILEVIEW, 2 * delayMillis);
	}

	@Override
	public void onFileModified(String path) {
	}

	@Override
	public void onFileRenamed(String path) {
		if (OperationAction.getLastOperation() != Operation.noOperation) {
			return;
		}
		// LogUtil.log(TAG, "onFileRenamed");
		handler.removeMessages(FILEVIEW);
		handler.sendEmptyMessageDelayed(FILEVIEW, 2 * delayMillis);
	}

	/** 文件监听结束 **/
	/**
	 * 选择图片分类
	 */
	private int selectPicItem = -1;

	/**
	 * @return the selectPicItem
	 */
	public int getSelectPicItem() {
		return selectPicItem;
	}

	/**
	 * @param selectPicItem
	 *            the selectPicItem to set
	 */
	public void setSelectPicItem(int selectPicItem) {
		this.selectPicItem = selectPicItem;
	}

	/**
	 * 标志是从pic Item 返回 pic
	 */
	private boolean isPicItemBack;

	/**
	 * @return the isPicItemBack
	 */
	public boolean isPicItemBack() {
		return isPicItemBack;
	}

	/**
	 * @param isPicItemBack
	 *            the isPicItemBack to set
	 */
	public void setPicItemBack(boolean isPicItemBack) {
		this.isPicItemBack = isPicItemBack;
	}

	/**
	 * 获取统计数据begin
	 */
	public FileCategoryHelper categoryHelper;

	/**
	 * 初始化统计数据
	 */
	public void initCategoryInfo() {
		if (categoryHelper == null) {
			categoryHelper = new FileCategoryHelper(this, FileExplorerTabActivity.this);
		}
		categoryHelper.initFileCategoryInfo();
	}

	/**
	 * 刷新指定分类统计数据
	 * @param fileCategory
	 */
	public void refreshCategoryInfo(FileCategory fileCategory) {
		if (categoryHelper != null) {
			LogUtil.d(TAG, "----refreshCategoryInfo:" + fileCategory);
			categoryHelper.refreshCategoryInfo(fileCategory);
		}
	}

	@Override
	public void onFileCategoryInfoChanged(FileCategory fc) {
		if (currentFragment instanceof AuroraMainFragment) {
			LogUtil.d(TAG, "------onFileCategoryInfoChanged setFileCategoryInfo");
			((AuroraMainFragment) currentFragment).setFileCategoryInfo();
		}
		// LogUtil.elog(TAG, "onFileCategoryInfoChanged");
		AuroraStorageDetailActivity.fileCategoryHelper = categoryHelper;

	}

	@Override
	public void onFileListQueryComplete(Cursor cursor) {

	}

	/**
	 * 获取统计数据end
	 */

	/**
	 * 图片分类缓存开始
	 */

	private SoftReference<ConcurrentHashMap<String, List<FileInfo>>> categoryPiclistMapCache;
	private SoftReference<ConcurrentHashMap<Integer, String>> picFilePathCache;
	private SoftReference<List<FileInfo>> cachePic;

	/**
	 * @return the categoryPiclistMapCache
	 */
	public SoftReference<ConcurrentHashMap<String, List<FileInfo>>> getCategoryPiclistMapCache() {
		return categoryPiclistMapCache;
	}

	/**
	 * @param categoryPiclistMapCache
	 *            the categoryPiclistMapCache to set
	 */
	public void setCategoryPiclistMapCache(SoftReference<ConcurrentHashMap<String, List<FileInfo>>> categoryPiclistMapCache) {
		this.categoryPiclistMapCache = categoryPiclistMapCache;
	}

	/**
	 * @return the picFilePathCache
	 */
	public SoftReference<ConcurrentHashMap<Integer, String>> getPicFilePathCache() {
		return picFilePathCache;
	}

	/**
	 * @param picFilePathCache
	 *            the picFilePathCache to set
	 */
	public void setPicFilePathCache(SoftReference<ConcurrentHashMap<Integer, String>> picFilePathCache) {
		this.picFilePathCache = picFilePathCache;
	}

	/**
	 * @return the cachePic
	 */
	public SoftReference<List<FileInfo>> getCachePic() {
		return cachePic;
	}

	/**
	 * @param cachePic
	 *            the cachePic to set
	 */
	public void setCachePic(SoftReference<List<FileInfo>> cachePic) {
		this.cachePic = cachePic;
	}

	public void clearCache() {
		if (categoryPiclistMapCache != null) {
			categoryPiclistMapCache.clear();
			categoryPiclistMapCache = null;
		}
		if (cachePic != null) {
			cachePic.clear();
			cachePic = null;
		}
		if (picFilePathCache != null) {
			picFilePathCache.clear();
			picFilePathCache = null;
		}
	}

	/**
	 * 图片分类缓存end
	 */

	public static final String NAVI_KEY_HIDE = "navigation_key_hide"; // Settings.System
	// 对应的键值

	/**
	 * 监听虚拟键状态的改变 通过 ContentObserver#onChange()获得 NAVI_KEY_HIDE 改变的情况
	 */
	private SettingsObserver mSettingsObserver;
	private boolean mHasNaviBar = false;

	public boolean ismHasNaviBar() {
		return mHasNaviBar;
	}

	private void ActivityInitFunction() {
		try {
			mHasNaviBar = getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
			if (mSettingsObserver == null && mHasNaviBar) {
				mSettingsObserver = new SettingsObserver(new Handler());
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

	}

	private class SettingsObserver extends ContentObserver {
		ContentResolver resolver = getContentResolver();

		SettingsObserver(Handler handler) {
			super(handler);
		}

		private void observe() {
			resolver.registerContentObserver(Settings.System.getUriFor(NAVI_KEY_HIDE), false, this);
			// LogUtil.log(TAG,
			// "auroraUpdateSettings registerContentObserver");
		}

		void unregister() {
			resolver.unregisterContentObserver(this);
			// LogUtil
			// .log(TAG, "auroraUpdateSettings unregisterContentObserver");
		}

		@Override
		public void onChange(boolean selfChange) {
			try {
				// LogUtil.log(TAG, "auroraUpdateSettings =="
				// + mSettingsObserver + " Activity=="
				// + FileExplorerTabActivity.this);
				if (currentFragment instanceof AuroraMainFragment) {
					((AuroraMainFragment) currentFragment).update();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

	}

	// add by Jxh 2014-8-11 begin
	private List<FileInfo> selectFiles;
	private ArrayList<String> selectPicPath;

	public List<FileInfo> getSelectFiles() {
		if (selectFiles == null) {
			selectFiles = new ArrayList<FileInfo>();
		}
		return selectFiles;
	}

	public void setSelectFiles(List<FileInfo> selectFiles) {
		this.selectFiles = selectFiles;
	}

	public ArrayList<String> getSelectPicPath() {
		return selectPicPath;
	}

	public void setSelectPicPath(ArrayList<String> selectPicPath) {
		this.selectPicPath = selectPicPath;
	}

	// add by Jxh 2014-8-11 end

	// add by Jxh 2014-8-30 begin 判断从打开目录到图片item刷新
	private boolean picItemRefresh;

	public boolean isPicItemRefresh() {
		return picItemRefresh;
	}

	public void setPicItemRefresh(boolean picItemRefresh) {
		this.picItemRefresh = picItemRefresh;
	}

	// add by Jxh 2014-8-30 end

	// add by Jxh 2014-9-9 begin 隐私相关
	private IPrivacyManageService ipService = null;
	private final ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.d(TAG, "onServiceConnected() called");
			ipService = IPrivacyManageService.Stub.asInterface(service);
			try {
				if (ipService != null) {
					// add by Jxh 2014-9-10 隐私服务获取当前隐私用户 begin
					AidlAccountData aidlAccountData = ipService.getCurrentAccount(getPackageName(), activityInfo.loadLabel(pm).toString());
					// LogUtil.elog(TAG, "aidlAccountData==" +
					// aidlAccountData);
					if (aidlAccountData != null) {
						AidlAccountData accountOld = Util.getPrivacyAccount(FileExplorerTabActivity.this, false);
						if (accountOld != null && accountOld.getAccountId() != 0 && accountOld.getAccountId() != aidlAccountData.getAccountId()) {
							PrivacyBroadcastReceiver receiver = new PrivacyBroadcastReceiver();
							receiver.encryption(accountOld.getHomePath());
						}
						getAccount(aidlAccountData);
						Util.savePrivacyAccount(FileExplorerTabActivity.this, aidlAccountData);
					}
					// add by Jxh 2014-9-10 隐私服务获取当前隐私用户 end
				}
			} catch (Exception e) {
				LogUtil.e(TAG, "onServiceConnected is error " + e.getMessage());
				e.printStackTrace();
			}
		}

		/**
		 * 断开链接需要重新绑定
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// LogUtil.log(TAG, "onServiceDisconnected() called");
			Intent intent = new Intent(AuroraConfig.PRIVACYACTION);

			// paul add for BUG #15498 start
			if (!Util.isLowVersion()) {
				intent = Util.createExplicitFromImplicitIntent(FileExplorerTabActivity.this, intent);
			}
			// paul add for BUG #15498 end

			bindService(intent, conn, Context.BIND_AUTO_CREATE);
			sendExitPrivacyBroadcast();
		}
	};

	/**
	 * 发送退出隐私状态广播
	 */
	private void sendExitPrivacyBroadcast() {
		AidlAccountData curAccount = new AidlAccountData();
		curAccount.setAccountId(0);
		curAccount.setHomePath("");
		Intent intent = new Intent(AuroraConfig.SWITCH_ACCOUNT_ACTION);
		intent.putExtra(AuroraConfig.KEY_ACCOUNT, curAccount);
		sendBroadcast(intent);
	}

	/**
	 * 判断是否是隐私空间数据 判断详情显示
	 * @return
	 */
	public boolean isPribacyView() {
		if (currentFragment instanceof PictureFragment) {
			if (((PictureFragment) currentFragment).isPrivacy()) {
				return true;
			}
		} else if ((currentFragment instanceof FileCategoryFragment) && getNowfileCategory() == FileCategory.Video) {
			if (((FileCategoryFragment) currentFragment).isPrivacyView()) {
				return true;
			}
		}
		return false;

	}

	private boolean isPrivacy;

	public boolean isPrivacy() {
		return isPrivacy;
	}

	public void setPrivacy(boolean is) {
		LogUtil.e(TAG, "setPrivacy::" + is);
		isStaticPrivacy = is;
		isPrivacy = is;
	}

	private static boolean isStaticPrivacy;

	public static boolean isStaticPrivacy() {
		return isStaticPrivacy;
	}

	public static void setStaticPrivacy(boolean isStaticPrivacy) {
		FileExplorerTabActivity.isStaticPrivacy = isStaticPrivacy;
	}

	private long accountId;

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	private static String privacyHomePath;

	public static String getPrivacyHomePath() {
		if (privacyHomePath == null) {
			privacyHomePath = "";
		}
		return privacyHomePath;
	}

	public static void setPrivacyHomePath(String homePath) {
		privacyHomePath = homePath;
	}

	/**
	 * 获取隐私用户
	 * @param aidlAccountData
	 */
	public void getAccount(AidlAccountData aidlAccountData) {

		// LogUtil.d(TAG, "id==" + aidlAccountData.getAccountId()
		// + " homePath==" + aidlAccountData.getHomePath()
		// + " Util.getPathFromPath(aidlAccountData.getHomePath())=="
		// + Util.getPathFromPath(aidlAccountData.getHomePath()));
		// LogUtil.log(TAG, "homePath==" + aidlAccountData.getHomePath());
		setPrivacy(aidlAccountData.getAccountId() != 0);
		setAccountId(aidlAccountData.getAccountId());
		setPrivacyHomePath(aidlAccountData.getHomePath());
		Util.savePrivacyHomePath(FileExplorerTabActivity.this, Util.getPathFromPath(aidlAccountData.getHomePath()));
		clearCache();
		hashMap.clear();
		if (isPrivacy()) {
			QueryPrivacy();
		} else {

			if (activityInfo.name.endsWith(getString(R.string.privacy_more))) {
				// 已经不是隐私状态，关闭添加隐私数据界面
				finish();
				return;
			}
			if (isDoPrivacy() && (currentFragment instanceof FileViewFragment)) {
				// LogUtil.log(TAG, "isDoPrivacy()  copyOrCutBack");
				// onBackPressed();
				((FileViewFragment) currentFragment).copyOrCutBack(true);
				if (getCutOrCopyfragment() instanceof PictureFragment) {
					onBackPressed();
				}
				setDoPrivacy(false);
			}
			if ((currentFragment instanceof FileCategoryFragment) && getNowfileCategory() == FileCategory.Video) {
				if (((FileCategoryFragment) currentFragment).isPrivacyView()) {
					((FileCategoryFragment) currentFragment).isNoPriView();
					((FileCategoryFragment) currentFragment).obNotifyAll();
				}
			} else if ((currentFragment instanceof PictureFragment)) {
				if (((PictureFragment) currentFragment).isPrivacy()) {
					((PictureFragment) currentFragment).isNoPriView();
				}

			}

		}

	}

	/**
	 * 获取隐私分类路径
	 * @param category
	 * @return
	 */
	public static String getPrivacyMedioPath(FileCategory category) {
		return getPrivacyMedioPath(category, getPrivacyHomePath());
	}

	/**
	 * 获取隐私分类路径
	 * @param category
	 * @param path
	 * @return
	 */
	public static String getPrivacyMedioPath(FileCategory category, String path) {
		if (category == null || TextUtils.isEmpty(path)) {
			return null;
		}
		String pPath = null;
		switch (category) {
		case Video:
			pPath = path + Base64.encodeToString((AuroraConfig.VIDEOID).getBytes(), Base64.URL_SAFE);

			break;
		case Picture:
			pPath = path + Base64.encodeToString((AuroraConfig.IMAGEID).getBytes(), Base64.URL_SAFE);

			break;
		case Music:// 通话录音
			pPath = path + Base64.encodeToString((AuroraConfig.AUDIOID).getBytes(), Base64.URL_SAFE);

			break;

		default:
			break;
		}
		pPath = Util.replaceBlank(pPath);
		return pPath;
	}

	/**
	 * 向隐私（APP）写入隐私文件数量
	 * @param category
	 * @param num
	 */

	public void setPrivacyMedioFileNum(FileCategory category, int num) {
		if (ipService == null) {
			LogUtil.e(TAG, "ipService is null");
			return;
		}
		try {
			switch (category) {
			case Video:
				ipService.setPrivacyNum(getPackageName(), getPackageName() + "." + getString(R.string.activity_video), num, getAccountId());
				break;
			case Picture:

				ipService.setPrivacyNum(getPackageName(), getPackageName() + "." + getString(R.string.activity_picture), num, getAccountId());
				break;

			default:
				break;
			}
			// LogUtil.log(TAG, "getPackageName()==" + getPackageName() + " "
			// + category + " " + num + " " + getAccountId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取隐私数据
	 * @param category
	 * @return
	 */
	public synchronized List<FileInfo> getPrivacyMedioFile(FileCategory category) {
		if (category == null || TextUtils.isEmpty(getPrivacyHomePath())) {
			return null;
		}
		List<FileInfo> infos = Util.getPathFilesByBase64(getPrivacyMedioPath(category));
		if (infos == null) {
			return null;
		}
		int num = infos.size();
		setPrivacyMedioFileNum(category, num);
		return infos;
	}

	/**
	 * 异步查询隐私全部数据
	 */
	public void QueryPrivacy() {
		runPrivacyThread(FileCategory.Video);
		runPrivacyThread(FileCategory.Picture);
		runPrivacyThread(FileCategory.Music);
	}

	// 存放隐私数据容器
	private ConcurrentHashMap<FileCategory, List<FileInfo>> hashMap = new ConcurrentHashMap<FileCategoryHelper.FileCategory, List<FileInfo>>();

	public ConcurrentHashMap<FileCategory, List<FileInfo>> getHashMap() {
		return hashMap;
	}

	public static final Object alock = new Object();
	public static Thread thread;

	public void runPrivacyThread(FileCategory category) {
		// LogUtil.d(TAG, "runPrivacyThread isPrivacy==" + isPrivacy
		// + " category==" + category);
		if (!isPrivacy) {
			return;
		}
		thread = new Thread(new TaskRunnable(category, alock));
		thread.start();

	}

	public void refreshPrivacy(FileCategory category) {
		hashMap.remove(category);
		runPrivacyThread(category);

	}

	private class TaskRunnable implements Runnable {
		private FileCategory category;
		private Object lock;

		public TaskRunnable(FileCategory category, Object lock) {
			super();
			this.category = category;
			this.lock = lock;
		}

		@Override
		public void run() {
			// LogUtil.d(TAG, " run() ");
			synchronized (lock) {
				// if(operationAction!=null&&operationAction.getmWakeLock()!=null){
				// operationAction.getmWakeLock().acquire();
				// }
				List<FileInfo> fileInfos = getPrivacyMedioFile(category);
				// hashMap.remove(category);
				// LogUtil.d(TAG, " lock  fileInfos:" + fileInfos.size()
				// +" category:"+category);
				// if(operationAction!=null&&operationAction.getmWakeLock()!=null){
				// operationAction.getmWakeLock().release();
				// }
				if (category == null) {
					return;
				}
				if (fileInfos == null) {
					privacyTaskAfter(category);
					return;
				}
				synchronized (hashMap) {
					// LogUtil.log(TAG, " lock  hashMap=="+hashMap);
					hashMap.put(category, fileInfos);
				}
				if (category.equals(FileCategory.Picture)) {
					FileExplorerTabActivity.this.runOnUiThread(new SendScanPrivacyBroadcast(category));
				}
				privacyTaskAfter(category);

			}
		}
	}

	/**
	 * 发送扫描隐私路径广播
	 */
	public final class SendScanPrivacyBroadcast implements Runnable {
		private FileCategory category;

		public SendScanPrivacyBroadcast(FileCategory category) {
			super();
			this.category = category;
		}

		@Override
		public void run() {
			operationAction.sendBroadcastScan(AuroraConfig.ACTION_DIR_SCAN, getPrivacyMedioPath(category));
		}

	}

	private void privacyTaskAfter(final FileCategory category) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				// LogUtil.d(TAG, "currentFragment==" + currentFragment
				// + " category==" + category);
				if (currentFragment instanceof AuroraMainFragment) {
					((AuroraMainFragment) currentFragment).setFileCategoryInfo();
				}
				if ((category == FileCategory.Picture)) {
					if (currentFragment instanceof PictureCategoryFragment) {
						((PictureCategoryFragment) currentFragment).obNotifyAll();
					} else if ((currentFragment instanceof PictureFragment)) {

						if (((PictureFragment) currentFragment).isPrivacy()) {
							((PictureFragment) currentFragment).obNotifyAll();
							((PictureFragment) currentFragment).refreshPrivacyPics();
						}
					}
				} else if ((category == FileCategory.Video)) {
					if (currentFragment instanceof FileCategoryFragment) {
						((FileCategoryFragment) currentFragment).obNotifyAll();
						((FileCategoryFragment) currentFragment).showPrivacyView();
					}
				}

			}
		});
	}

	/**
	 * 注册隐私帐号变动广播
	 */
	private void registerPrivacyBroadcastReceiver() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(AuroraConfig.SWITCH_ACCOUNT_ACTION);
		filter.addAction(AuroraConfig.DELETE_ACCOUNT_ACTION);
		registerReceiver(privacyBroadcastReceiver, filter);
	}

	/**
	 * 注销隐私帐号变动广播
	 */
	private void unRegisterPrivacyBroadcastReceiver() {
		if (privacyBroadcastReceiver != null) {
			unregisterReceiver(privacyBroadcastReceiver);
		}
	}

	/**
	 * 隐私帐号变动广播
	 */
	private final BroadcastReceiver privacyBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			final AidlAccountData accountData = intent.getParcelableExtra(AuroraConfig.KEY_ACCOUNT);
			if (accountData == null) {
				LogUtil.e(TAG, "accountData is null ");
				return;
			}
			// LogUtil.log(TAG, "Id=" + accountData.getAccountId() + " Path="
			// + accountData.getHomePath() + " ");
			if (action.equals(AuroraConfig.SWITCH_ACCOUNT_ACTION)) {
				getAccount(accountData);
				if (activityInfo.name.endsWith(getString(R.string.activity_picture))) {
					finish();
				} else if (activityInfo.name.endsWith(getString(R.string.activity_video))) {
					finish();
				}

			} else if (action.equals(AuroraConfig.DELETE_ACCOUNT_ACTION)) {
				conn.onServiceDisconnected(getComponentName());
			}

		}

	};

	/**
	 * 隐私状态下，锁屏执行动作
	 */
	public void screenAction() {
		if (currentFragment instanceof PictureFragment) {
			if (((PictureFragment) currentFragment).isPrivacy()) {
				((PictureFragment) currentFragment).unDoOperation();
				if (operationAction != null) {
					if (operationAction.getDialog() != null) {
						operationAction.getDialog().dismiss();
					}
					if (operationAction.getTextInputDialog() != null) {
						operationAction.getTextInputDialog().dismiss();
					}
				}
			}
		} else if ((currentFragment instanceof FileCategoryFragment) && getNowfileCategory() != null && getNowfileCategory().equals(FileCategory.Video)) {
			if (((FileCategoryFragment) currentFragment).isPrivacyView()) {
				((FileCategoryFragment) currentFragment).unAllOperation();
				if (operationAction != null) {
					if (operationAction.getDialog() != null) {
						operationAction.getDialog().dismiss();
					}
					if (operationAction.getTextInputDialog() != null) {
						operationAction.getTextInputDialog().dismiss();
					}
				}
			}
		}
	}

	private boolean isPrivacyBackToNormal;

	public boolean isPrivacyBackToNormal() {
		return isPrivacyBackToNormal;
	}

	public void setPrivacyBackToNormal(boolean isPrivacyBackToNormal) {
		this.isPrivacyBackToNormal = isPrivacyBackToNormal;
	}

	// add by Jxh 2014-9-9 end 隐私相关

	public void imageViewAnim(ImageView imageView, boolean in) {
		if (in) {
			imageView.setImageResource(R.anim.animation_imageview_in);
		} else {
			imageView.setImageResource(R.anim.animation_imageview_out);
		}
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
		animationDrawable.start();
	}

	public void setStatistics(FileCategoryHelper categoryHelper, int position) {
		if (categoryHelper != null) {
			categoryHelper.updateStatistics(categoryHelper.getStatisticsContentValues(AuroraConfig.statisticsTag.get(position)));
		}
	}
	
	/*************** 蒙板开始 ****************/
	private View mCoverView = null;
	private FrameLayout windowLayout;
	private Animation coverAnimation;

	private void loadAnimation(int animId) {
		try {
			coverAnimation = AnimationUtils.loadAnimation(FileExplorerTabActivity.this, animId);
			mCoverView.startAnimation(coverAnimation);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@Override
	public void addCoverView() {
		mCoverView = new TextView(FileExplorerTabActivity.this);
		mCoverView.setBackgroundColor(Color.parseColor("#666666"));
		mCoverView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		windowLayout = (FrameLayout)getWindow().getDecorView();
		windowLayout.addView(mCoverView);
		LogUtil.d(TAG, "----windowLayout:"+windowLayout+" mCoverView:"+mCoverView);
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_enter);
	}

	public void removeFileCoverView() {
		LogUtil.d(TAG, "----windowLayout:"+windowLayout+" mCoverView:"+mCoverView);
		if (mCoverView != null && windowLayout != null) {
			windowLayout.removeView(mCoverView);
		}
		loadAnimation(com.aurora.R.anim.aurora_menu_cover_exit);
	}

	/*************** 蒙板end ****************/
}
