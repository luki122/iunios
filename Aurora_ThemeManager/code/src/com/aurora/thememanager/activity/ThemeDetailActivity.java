package com.aurora.thememanager.activity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.AuroraConfiguration;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnScrollChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.fragments.HttpCallBack;
import com.aurora.thememanager.fragments.JsonHttpListener;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.parser.ThemePkgUpdateParser;
import com.aurora.thememanager.preference.PreferenceManager;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.DensityUtils;
import com.aurora.thememanager.utils.download.DatabaseController;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.themehelper.LockScreenApplyHelper;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.utils.themeloader.PictureLoader;
import com.aurora.thememanager.utils.themeloader.ThemePackageLoader;
import com.aurora.thememanager.utils.themeloader.ThemeLoadListener;
import com.aurora.thememanager.utils.themeloader.ThemeLoader;
import com.aurora.thememanager.widget.DownloadHorizontalButton;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ThemeDetailActivity extends BaseActivity implements OnScrollChangeListener,DownloadStatusCallback,HttpCallBack{

	private static final int MSG_START_APPLY_THEME = 0;
	
	private static final int MSG_END_APPLY_THEME = 1;
	
	private static final int MSG_UPDATE_APPLIED_THEME_ITEM = 2;
	
	private static final int MSG_UPDATE_APPLIED_THEME_FAULUER= 3;
	
	private static final int MSG_RESTTART_LAUNCHER = 4;
	
	private static final int MSG_UPDATE_NEW_VERSION_INFO = 5;
	
	private static final int ID_APPLY_PROGRESS_DIALOG = 0;
	
	private static final int ID_APPLY_THEME_FAILUER = 1;
	
	
	/**
	 * Current theme's name
	 */
	private TextView mTitle;
	/**
	 * Current theme's size
	 */
	private TextView mSize;
	/**
	 * Current theme's description
	 */
	private TextView mDescription;
	
	/**
	 * Current theme's author
	 */
	private TextView mAuthor;
	
	/**
	 * Parent view of previews
	 */
	private LinearLayout mPreviewLayout;
	
	/**
	 * Button for theme operation,such as,download,apply
	 */
	private DownloadHorizontalButton mOptionButton;
	
	/**
	 * Parent view of index for preview page
	 */
	private LinearLayout mIndexLayout;
	
	private View mSoundEffectLayout;
	
	/**
	 * Current theme 
	 */
	private Theme mCurrentTheme;
	
	/**
	 * saved previews' Url here
	 */
	private String[] mPreviews;
	
	private int mPageIndexSize;
	
	private int mPageIndexMargin;
	
	private int mPreviewWidth;
	
	private int mPreviewHeight;
	
	private RequestQueue mQueue;
	
	private final int mImageCacheSize = ThemeConfig.HttpConfig.DISKCACHE_SIZE;
	
	private ImageLoader mInternetImageLoader;
	
	/**
	 * ScrollView that contain previews
	 */
	private HorizontalScrollView mPreviewScrollView;
	
	private View mProgressParent;
	
	private ProgressBtn mProgressBtn;
	
	private Button mApplyButton;
	
	private DownloadManager mDownloadManager;
	
	private CacheManager mCacheManager;
	
	private boolean mFromLocal = false;
	
	private boolean mApplySuccess = false;
	
	private boolean mHasSoundEffect = false;
	
	AuroraProgressDialog mApplyProgressDialog;
	
	private AuroraAlertDialog.Builder mAlertDialog;
	
	private  ThemeManager mThemeManager;
	
	private PreferenceManager mPrefManager;
	
	private int mUsedThemeid = -2;
	
	private boolean mApplied =false;
	
	private boolean mDownloaded = false;
	
	private ThemeInternetHelper mThemeLoadHelper;
	
	private JsonHttpListener mHttpListener;
	
	private boolean mHasNewVersion = false;
	
	private View mProgress;
	
	private View mOptionButtonParent;
	
	private View mInfoParent;
	
	private Runnable mDeleteThemeThread = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mDownloaded && !mApplied){
				mThemeManager.deleteTheme(mCurrentTheme, ThemeDetailActivity.this);
			}
		}
	};
	
	private Runnable mVersionCheckRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mHasNewVersion = hasNewVersion();
			if(mHasNewVersion){
				requestNewVersion();
			}else{
				initThemeInfoViews();
			}
		}
	};
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_START_APPLY_THEME:
				mThemeManager.apply(mCurrentTheme);
				break;
			case MSG_UPDATE_APPLIED_THEME_ITEM:
				int themeId = (int) msg.obj;
				if(mCurrentTheme.themeId == ThemeConfig.THEME_DEFAULT_ID){
					LockScreenApplyHelper.applyDefaultTimeWallpaper(ThemeDetailActivity.this);
				}
				ThemeManager.restartApplications(ThemeDetailActivity.this, themeId);
				
				break;
			case MSG_UPDATE_APPLIED_THEME_FAULUER:
				Toast.makeText(ThemeDetailActivity.this, getResources().getString(R.string.apply_theme_failure), Toast.LENGTH_LONG).show();
				break;
			case MSG_UPDATE_NEW_VERSION_INFO:
				
				initThemeInfoViews();
				
				break;
			}
		};
		
	};
	
	private void showInfo(){
		mProgress.setVisibility(View.GONE);
		mInfoParent.setVisibility(View.VISIBLE);
		mOptionButtonParent.setVisibility(View.VISIBLE);
	}
	
	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			mDownloadManager.updateProgress(mCurrentTheme);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		createDialog();
		mHttpListener = new JsonHttpListener(this);
		mApp.registerActivity(this);
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_ALL);
		mPrefManager = PreferenceManager.getInstance(this);
		mUsedThemeid = mThemeManager.getAppliedThemeId(this);
		mCacheManager = CacheManager.getInstance();
		mDownloadManager = new DownloadManager(this);
		mDownloadManager.setCallBack(this);
		Intent intent = getIntent();
		mThemeLoadHelper = new ThemeInternetHelper(this);
		if(intent != null){
			mCurrentTheme = (Theme) intent.getExtra(ThemeConfig.KEY_FOR_APPLY_THEME);
			mFromLocal = intent.getBooleanExtra(ThemeConfig.KEY_FOR_APPLY_FROM_LOACAL, false);
			if(mFromLocal){
				mCurrentTheme.downloadId = mCurrentTheme.themeId;
				mCurrentTheme.type = Theme.TYPE_THEME_PKG;
//				mCurrentTheme.versionCode = 
			}
		}
		mApplied = currentThemeapplied();
		mQueue = RequestQueue.newRequestQueue(this, mCacheManager.getPreviewDiskCache());
		mInternetImageLoader = new ImageLoaderImpl(mQueue, mCacheManager.getBitmapCache(), getResources(), getAssets()){
			@Override
			public void makeRequest(ImageRequest request) {
				// TODO Auto-generated method stub
				super.makeRequest(request);
				request.setCacheExpireTime(TimeUnit.HOURS, 1);
			}
		};
		
		DownloadService.registerUpdateListener(updateListener);
		setAuroraContentView(R.layout.theme_detail_activity, AuroraActionBar.Type.Normal);
		initView();
		mThemeManager.setCallBack(new ThemeOperationCallBack() {
			
			@Override
			public void onProgressUpdate(int progress) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted(final boolean success, int statusCode) {
				// TODO Auto-generated method stub
				mApplyProgressDialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						if(!success){
							showDialog(mAlertDialog.create());
						}
					}
				});
				dismissDialog(mApplyProgressDialog);
				if(success){
					mCurrentTheme.downloadId = mCurrentTheme.themeId;
				    mThemeManager.setThemePackageAplied(mCurrentTheme, ThemeDetailActivity.this);
					Message msg = new Message();
					msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
					msg.obj = mCurrentTheme.themeId;
					mHandler.sendMessageDelayed(msg, 200);
				}
				
			}
			
			@Override
			public Context getContext() {
				// TODO Auto-generated method stub
				return ThemeDetailActivity.this;
			}
		});
		if(mFromLocal){
			mProgressParent.setVisibility(View.GONE);
			mApplyButton.setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * 请求新版本
	 */
	private void requestNewVersion(){
		mThemeLoadHelper.clearRequest();
		mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_NEW_VERSION_CHECK_URL,mHttpListener,
				HttpUtils.createCheckThemeNewVersion(this, mCurrentTheme.themeId,mCurrentTheme.versionCode));
		mThemeLoadHelper.startRequest();
	}
	
	/**
	 * 判断当前主题是否已应用
	 * @return
	 */
	private boolean currentThemeapplied(){
		DatabaseController dbController = DownloadService.getDownloadController();
		if(dbController != null){
			return dbController.getApplied(mCurrentTheme.themeId) && (mCurrentTheme.themeId == mUsedThemeid);
		}
		
		return false;
	}
	
	private boolean hasNewVersion(){
		if(!mFromLocal){
			return false;
		}
		boolean newVersion = false;
		DatabaseController dbc = DatabaseController.getController(this, DatabaseController.TYPE_DOWNLOAD);
		if(dbc != null){
			dbc.openDatabase();
			newVersion = dbc.hasNewVersion(mCurrentTheme.themeId);
		}
		return newVersion;
	}

	
	private void dismissDialog(Dialog dialog){
//		if(dialog.isShowing()){
			dialog.dismiss();
//		}
	}
	private void showDialog(Dialog dialog){
		if(dialog.isShowing()){
			dialog.dismiss();
		}
		dialog.show();
	}
	
	
	private void createDialog(){
		mApplyProgressDialog  = new AuroraProgressDialog(this);
		mApplyProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
		mApplyProgressDialog.setMessage(getResources().getString(R.string.msg_apply_theme));
		mApplyProgressDialog.setCancelable(false);
		mApplyProgressDialog.setCanceledOnTouchOutside(false);
		mAlertDialog =  new AuroraAlertDialog.Builder(this)
		.setTitle(R.string.apply_theme_dialog_title)
		.setMessage(R.string.apply_theme_faliure)
		.setPositiveButton(R.string.ok,null);
	}
	
	public void applyTheme(View view){
		if(mUsedThemeid == mCurrentTheme.themeId){
			Toast.makeText(ThemeDetailActivity.this, R.string.theme_applied, Toast.LENGTH_SHORT).show();
			return;
		}
		
		mHandler.sendEmptyMessageDelayed(MSG_START_APPLY_THEME, 500);
		showDialog(mApplyProgressDialog);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DownloadService.unRegisterUpdateListener(updateListener);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}
	
	
	/**
	 * initial all view here
	 */
	private void initView(){
		mTitle = (TextView)findViewById(R.id.theme_title);
		mSize = (TextView)findViewById(R.id.theme_size);
		mDescription = (TextView)findViewById(R.id.theme_desc);
		mAuthor = (TextView)findViewById(R.id.theme_author);
		mPreviewLayout = (LinearLayout)findViewById(R.id.theme_detail_previews_layout);
		mIndexLayout = (LinearLayout)findViewById(R.id.theme_detail_preview_index_layout);
		mPreviewScrollView = (HorizontalScrollView)findViewById(R.id.theme_detail_preview_scroller);
		mPageIndexSize = getResources().getDimensionPixelSize(R.dimen.page_index_size);
		mPageIndexMargin = getResources().getDimensionPixelSize(R.dimen.page_index_margin);
		mPreviewWidth = getResources().getDimensionPixelSize(R.dimen.theme_preview_width);
		mPreviewHeight = getResources().getDimensionPixelSize(R.dimen.theme_preview_height);
		mProgressBtn =(ProgressBtn) findViewById(R.id.hotizontal_progress_btn);
		mApplyButton = (Button)findViewById(R.id.btn_apply_theme);
		mActionBar = getAuroraActionBar();
		mProgressParent = findViewById(R.id.progress_btn_parent);
		mOptionButtonParent = findViewById(R.id.horizontal_progress_btn_layout);
		mProgress = findViewById(R.id.progress);
		mInfoParent = findViewById(R.id.info_parent);
		/*
		 * show theme information 
		 */
		addDeleteThemeButton();
		
		mHandler.post(mVersionCheckRunnable);
	}
	
	private void initThemeInfoViews(){
		String pageName = "";
		showInfo();
		if(mCurrentTheme != null){
			mPreviews = mCurrentTheme.previews;
			mTitle.setText(mCurrentTheme.name);
			mSize.setText(mCurrentTheme.sizeStr);
			mDescription.setText(mCurrentTheme.description);
			mAuthor.setText(mCurrentTheme.author);
			pageName = mCurrentTheme.name;
			mHasSoundEffect = !"false".equals(mCurrentTheme.hasSoundEffect);
			mSoundEffectLayout = findViewById(R.id.theme_ringtong_layout);
			mSoundEffectLayout.setVisibility(/*mHasSoundEffect?View.VISIBLE:*/View.GONE);
			loadPreviewsIndex();
		}
		
		
		mActionBar.setTitle(pageName);
		
		mPreviewScrollView.setOnScrollChangeListener(this);
		if(mFromLocal){
			applied();
		}
		
		if(mHasNewVersion ){
			handleUpdate(mCurrentTheme);
		}
	}
	
	private void addDeleteThemeButton(){
		/*
		 * 
		 * 删除主题包
		 * 
		 */
		if (mActionBar != null  ) {
			mActionBar.addItem(R.drawable.btn_delete_theme_selector, R.id.btn_delete_theme,null);
			mActionBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {

						@Override
						public void onAuroraActionBarItemClicked(int itemId) {
							if(itemId == R.id.btn_delete_theme){
								deleteCurrentTheme();
							}
						}
					});
			
		}
	}
	
	private void deleteCurrentTheme(){
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
		.setTitle(R.string.delete_theme_title)
		.setMessage(R.string.delete_current_theme_msg)
		.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mHandler.post(mDeleteThemeThread);
				mApplyButton.setVisibility(View.GONE);
				mProgressParent.setVisibility(View.VISIBLE);
				showOperationDownload(mCurrentTheme);
			}
		})
		.setNegativeButton(R.string.cancel,null)
		.create();
		
		dialog.show();
	
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDownloadManager.updateProgress(mCurrentTheme);
		
	}
	
	/**
	 * Create previews and preview index
	 */
	private void loadPreviewsIndex(){
		if(mPreviews != null && mPreviews.length > 0){
			int count = mPreviews.length;
			mIndexLayout.removeAllViews();
			final String[] previewKey = new String[count];
			for(int i =0;i<count;i++){
				final int position = i;
				ImageView index = new ImageView(this);
				NetworkImageView preview = new NetworkImageView(this);
				preview.setScaleType(ScaleType.FIT_XY);
				preview.setDefaultImageResId(R.drawable.item_default_bg);
				if(mFromLocal){
					if(mCurrentTheme.themeId == ThemeConfig.THEME_DEFAULT_ID){
						preview.setImageUrl(ImageLoaderImpl.RES_ASSETS+mPreviews[i], mInternetImageLoader);
					}else{
						preview.setImageUrl(ImageLoaderImpl.RES_SDCARD+mPreviews[i], mInternetImageLoader);
					}
				}else{
					preview.setImageUrl(mPreviews[i], mInternetImageLoader);
				}
				preview.setOnClickListener(new OnClickListener() {
				
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(Action.ACTION_SHOW_PREVIEW_PICTURE_PAGER);
						intent.putExtra(Action.KEY_SHOW_PREIVEW_PICTURE_INDEX, position);
						intent.putExtra(Action.KEY_SHOW_PREIVEW_PICTURE_URL, mPreviews);
						intent.putExtra(ThemeConfig.KEY_FOR_APPLY_FROM_LOACAL, mFromLocal);
						intent.putExtra(ThemeConfig.KEY_FOR_APPLY_FROM_DEFAULT, mCurrentTheme.themeId == ThemeConfig.THEME_DEFAULT_ID);
						startActivity(intent);
					}
				});
				
				LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(mPreviewWidth,mPreviewHeight);
				if(i > 0){
					previewParams.leftMargin = mPageIndexMargin;
				}
				mPreviewLayout.addView(preview, previewParams);
				index.setImageResource(R.drawable.icon_page_indicator);
				LinearLayout.LayoutParams indexParams = new LinearLayout.LayoutParams(mPageIndexSize,mPageIndexSize);
				if(i > 0){
					indexParams.leftMargin = mPageIndexMargin;
					index.setEnabled(false);
				}
				mIndexLayout.addView(index,indexParams);
			}
		}
	}
	

	@Override
	public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		// TODO Auto-generated method stub
		final int measuredWidth = mPreviewScrollView.getMeasuredWidth();
		int index = -1;
		if(oldScrollX< scrollX){
			 index = (scrollX + measuredWidth - mPreviewWidth+mPageIndexMargin)/ mPreviewWidth;
		}else{
			 index = (scrollX + measuredWidth - mPreviewWidth-mPageIndexMargin)/ mPreviewWidth;
		}
		updateIndexState(index);
		
	}

	/**
	 * update indicator state when user scroll previews
	 * 
	 * @param index
	 */
	private void updateIndexState(int index){
		int indexCount = mIndexLayout.getChildCount();
		if(index > indexCount || index < 0){
			return;
		}
		if(indexCount > 0){
			for(int i = 0;i< indexCount;i++){
				View child = mIndexLayout.getChildAt(i);
				child.setEnabled(i==index);
			}
		}
	}

	@Override
	public void showOperationUpdate(DownloadData data,
			OnClickListener onClickListener) {
		handleUpdate(data);
	}
	
	private void handleUpdate(final DownloadData data){
		data.downloadId = mCurrentTheme.themeId;
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.post(mDeleteThemeThread);
				mApplyButton.setVisibility(View.GONE);
				DownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		disableDelete();
		// ProgressBtn
		mProgressParent.setVisibility(View.VISIBLE);
		mApplyButton.setVisibility(View.GONE);
		mProgressBtn.setBtnText(mProgressBtn.getResources().getString(R.string.donwloadman_update));
		mProgressBtn.setStatus(ProgressBtn.STATUS_UPDATE_FROM_DETAIL);
		mProgressBtn.setOnNormalClickListener(clickListener);
		final int downloadId = (int) data.downloadId;
		mProgressBtn.setOnBeginAnimListener(new OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				FileDownloader downloader = DownloadService.getDownloaders(ThemeDetailActivity.this)
						.get(downloadId);
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_DOWNLOADING) {
						view.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
					}
				}
			}
		});
	}

	@Override
	public void showOperationApplied(DownloadData data) {
		// TODO Auto-generated method stub
		applied();
	}

	@Override
	public void showAppling(DownloadData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOperationDownload(final DownloadData data) {
		// TODO Auto-generated method stub
		mHasNewVersion = hasNewVersion();
		if(mHasNewVersion){
			handleUpdate(data);
			return;
		}
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				DownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		
		// ProgressBtn
		disableDelete();
//		mApplyButton.setVisibility(!mFromLocal?View.GONE:View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		mProgressBtn.showDownloadButtonWithoutText();
		mProgressBtn.setOnNormalClickListener(clickListener);
		final int downloadId = data.downloadId;
		mProgressBtn.setOnBeginAnimListener(new OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				FileDownloader downloader = DownloadService.getDownloaders(ThemeDetailActivity.this)
						.get(downloadId);
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == FileDownloader.STATUS_CONNECTING
							|| status == FileDownloader.STATUS_DOWNLOADING) {
						view.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
					}
				}
			}
		});
	}

	@Override
	public void showWaitApply(DownloadData data) {
		// TODO Auto-generated method stub
	
		mCurrentTheme.fileDir = data.fileDir;
		mCurrentTheme.fileName = data.fileName;
		enableDelete();
		mApplyButton.setVisibility(View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
//		mProgressBtn.setOnFoucsClickListener(listener);
		mProgressParent.setVisibility(View.GONE);
		mDownloaded = true;
		Log.d("td", "showWaitApply:"+mCurrentTheme.downloadId);
	}

	@Override
	public void showOperationRetry(final FileDownloader downloader, int progress) {
		// TODO Auto-generated method stub
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!SystemUtils.hasNetwork()) {
					Toast.makeText(mProgressBtn.getContext(), mProgressBtn.getContext()
							.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
				} else {
					DownloadService.pauseOrContinueDownload(
							mProgressBtn.getContext(), downloader.getDownloadData());
				}
			}
		};
		
		// ProgressBtn
		mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_RETRY);
		mProgressBtn.setProgress(progress);
		mProgressBtn.setOnProgressClickListener(clickListener);
		mProgressBtn.setProgressBackground(R.drawable.aurora_progress_refresh);
	}

	@Override
	public void showOperationDownloading(final FileDownloader downloader, int progress) {
		// TODO Auto-generated method stub
		int status = downloader.getStatus();
		disableDelete();
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				DownloadService.pauseOrContinueDownload(
						mProgressBtn.getContext(), downloader.getDownloadData());
			}
		};
		// ProgressBtn
		if (status == FileDownloader.STATUS_WAIT) {
			if (!mProgressBtn.isRuningStartAnim()) {
				mProgressBtn.setStatus(ProgressBtn.STATUS_WAIT_DOWNLOAD);
			}
		} else {
			if (!mProgressBtn.isRuningStartAnim()) {
				mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
			}
		
			int id =  mProgressBtn.getTag() == null ? 0 : (Integer) mProgressBtn.getTag();
			if (!mProgressBtn.isRuningStartAnim()) {
				if (id == downloader.getDownloadData().downloadId) {
					mProgressBtn.setProgressAnim(progress);
				} else {
					mProgressBtn.setProgress(progress);
				}
			}
			mProgressBtn.setOnProgressClickListener(clickListener);
			mProgressBtn.setProgressBackground(R.drawable.aurora_progress_downloading);
		}
	}

	@Override
	public void showOperationContinue(final FileDownloader downloader, int progress) {
		// TODO Auto-generated method stub
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDownloadManager.doOperationContinue(downloader, mProgressBtn.getContext());
			}
		};
		disableDelete();
		// ProgressBtn
		mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
		mProgressBtn.setProgress(progress);
		mProgressBtn.setOnProgressClickListener(clickListener);
		mProgressBtn.setProgressBackground(R.drawable.aurora_progress_pause);
	}

	
	
	private void applied(){
		if(mApplied){
			mProgressParent.setVisibility(View.GONE);
			mApplyButton.setVisibility(View.VISIBLE);
			mApplyButton.setText(R.string.theme_applyed);
			disableDelete();
		}
	}
	
	private void disableDelete(){
		AuroraActionBarItem deleteItem = mActionBar.getItem(0);
		if(deleteItem != null){
			deleteItem.getItemView().setEnabled(false);
			deleteItem.getItemView().setVisibility(View.INVISIBLE);
		}
	}
	
	private void enableDelete(){
		AuroraActionBarItem deleteItem = mActionBar.getItem(0);
		if(deleteItem != null){
			deleteItem.getItemView().setEnabled(true);
			deleteItem.getItemView().setVisibility(View.VISIBLE);
		}
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
			Parser parser = new Parser(new ThemePkgUpdateParser());
			List<Object> themes = parser.startParser(response.toString());
			if(themes != null && themes.size() > 0){
				Theme newTheme = (Theme) themes.get(0);
				mCurrentTheme.setTo(newTheme);
				Log.d("update","newVersionId:"+mCurrentTheme.downloadId);
				mHandler.sendEmptyMessage(MSG_UPDATE_NEW_VERSION_INFO);
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
	
}
