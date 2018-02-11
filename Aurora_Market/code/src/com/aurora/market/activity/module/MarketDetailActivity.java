package com.aurora.market.activity.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraCustomActionBar.onOptionItemClickListener;
import aurora.widget.AuroraCustomActionBar;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.MarketManager;
import com.aurora.datauiapi.data.bean.appiteminfo;
import com.aurora.datauiapi.data.bean.detailsObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.R;
import com.aurora.market.marketApp;
import com.aurora.market.activity.BaseActivity;
import com.aurora.market.activity.picbrowser.PictureViewActivity;
import com.aurora.market.activity.setting.DownloadManagerActivity;
import com.aurora.market.activity.setting.MarketManagerPreferenceActivity;
import com.aurora.market.download.ApkUtil;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.install.InstallAppManager;
import com.aurora.market.model.DownloadData;
import com.aurora.market.model.InstalledAppInfo;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AppInstallService;
import com.aurora.market.ui.ExpandableTextView;
import com.aurora.market.util.BitmapUtil;
import com.aurora.market.util.CustomAnimCallBack;
import com.aurora.market.util.CustomAnimation;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.Globals;
import com.aurora.market.util.LoadingPageUtil;
import com.aurora.market.util.Log;
import com.aurora.market.util.PicBrowseUtils;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.util.TimeUtils;
import com.aurora.market.util.LoadingPageUtil.OnHideListener;
import com.aurora.market.util.LoadingPageUtil.OnRetryListener;
import com.aurora.market.util.LoadingPageUtil.OnShowListener;
import com.aurora.market.widget.ProgressBtn;
import com.aurora.utils.DensityUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
//import com.aurora.datauiapi.data.bean.appiteminfo;
//import com.aurora.datauiapi.data.bean.detailsObject;

public class MarketDetailActivity extends BaseActivity implements
		OnClickListener, INotifiableController {
	private static final String TAG = "MarketDetailActivity";
	private AuroraActivity mActivity;
	private marketApp app = null;
	private AuroraCustomActionBar mActionBar;
	public ImageView main_update;
	private LinearLayout mAppPicBrowseLayout = null;
	private View mAppRelacContent;
	private ImageView mappavatar;
	private ImageView mDownloadBtn = null;
	private RatingBar mRatingBar;
	private TextView mAppname;
	private TextView mAppTitle;
	private TextView mAppVersion;
	private TextView mAppDeveloper;
	private TextView mAppUpdateTime;
	private TextView mAppCategory;
	private ExpandableTextView mAppDescContent;
	private TextView mDownloadCount, dis_download_text;
	private TextView mAppSize;
	private ProgressBar mProgressBar;
	private TextView mViewComment, download_text;
	private ImageView mShareBtn;
	private ImageView mmoreView;
	private ImageView mToDownloadManagerBtn;
	private FrameLayout mDownloadBtnLayout, mDownloadProLayout;
	private LinearLayout mDownloadBtnInstall;
	private ImageView mCancelDownloadBtn, mDownloadInstallView;
	private ScrollView detail_scrollview;
	private MarketManager mmarketManager;
	private static final int AURORA_NEW_MARKET = 0;
	private detailsObject obj = new detailsObject();

	// 图片加载工具
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions optionsImage, opIconsImage;
	private LinearLayout mLoadingView;
	private ProgressBar mLoadingImg;
	private ManagerThread thread;
	private boolean stopFlag = false;
	private int current_status = FileDownloader.STATUS_DEFAULT;
	private DownloadData downloaddata;
	private AnimationDrawable animationDrawable;
	private boolean isOpenAnimal = true;
	private boolean isUpdate = false;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private SharedPreferences app_update;
	private int update_status = 0;
	private LoadingPageUtil loadingPageUtil;
	private MyBroadcastReciver broadcastReceiver;

	private int mScreenWidth;
	private int mScreenHeight;
	private boolean mIsU3 = false;
	private TextView mAppSource;
	private TextView mAppSourceInfo;

	// private Animation anim;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// this.setTheme(com.aurora.R.style.Theme_Aurora_Dark_Transparent);
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_detail_page,
				AuroraActionBar.Type.NEW_COSTOM, true);

		app_update = getSharedPreferences(Globals.SHARED_APP_UPDATE,
				MODE_APPEND);
		update_status = app_update.getInt(
				Globals.SHARED_DOWNORUPDATE_KEY_ISEXITS, 0);
		downloaddata = (DownloadData) getIntent().getParcelableExtra(
				"downloaddata");
		initActionBar();
		initViews();
		initimageLoad();

		setListener();

		initLoadingPage();

		mmarketManager = new MarketManager();
		thread = new ManagerThread(mmarketManager);

		thread.market(this);
		initdata();
		initBroadCast();
	}

	/**
	 * @Title: initBroadCast
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param
	 * @return void
	 * @throws
	 */
	private void initBroadCast() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Globals.BROADCAST_ACTION_DOWNLOAD);
		intentFilter.addAction(Globals.MARKET_UPDATE_ACTION);
		broadcastReceiver = new MyBroadcastReciver();
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private class MyBroadcastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Globals.BROADCAST_ACTION_DOWNLOAD)
					|| action.equals(Globals.MARKET_UPDATE_ACTION)) {

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						getUpAppSign();
					}
				}, 1000);

			}
		}

	}

	private void getUpAppSign() {

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				int count = AppDownloadService.getDownloaders().size();
				int sum = 0;
				if (count == 0) {
					DataFromUtils up_data = new DataFromUtils();
					sum = up_data.getUpdateSum(MarketDetailActivity.this);
				}
				if ((sum > 0) || (count > 0)) {
					runOnUiThread(new Runnable() {
						public void run() {
							setUpdateSign(1);
						}

					});
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							setUpdateSign(0);
						}

					});
				}

			}

		}.start();
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(this, findViewById(R.id.detailLayout));
		loadingPageUtil.setOnRetryListener(new OnRetryListener() {
			@Override
			public void retry() {
				initdata();
			}
		});
		loadingPageUtil.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow() {
				// mListView.setVisibility(View.GONE);
			}
		});
		loadingPageUtil.setOnHideListener(new OnHideListener() {
			@Override
			public void onHide() {
				// mListView.setVisibility(View.VISIBLE);
			}
		});
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		PicBrowseUtils.resetImgVContainer();
		((MarketManager) mmarketManager).setController(null);
		thread.quit();
		unregisterReceiver(broadcastReceiver);
	}

	/**
	 * @Title: initimageLoad
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param
	 * @return void
	 * @throws
	 */
	private void initimageLoad() {
		// TODO Auto-generated method stub
		optionsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_thumbnail)
				.showImageForEmptyUri(R.drawable.page_thumbnail)
				.showImageOnFail(R.drawable.page_thumbnail).cacheInMemory(true)
				.cacheOnDisc(true).build();

		opIconsImage = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.page_appicon_big)
				.showImageForEmptyUri(R.drawable.page_appicon_big)
				.displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer)))
				.showImageOnFail(R.drawable.page_appicon_big)
				.cacheInMemory(true).cacheOnDisc(true).build();
	}

	private void disView() {
		// TODO Auto-generated method stub

		appiteminfo info = obj.getAppInfo();
		checkSoftwareState(false);
		mAppname.setText(info.getTitle());
		mRatingBar.setRating(info.getLikesRate() / 20);
		mDownloadCount.setText(info.getDownloadCountStr()
				+ getResources().getString(R.string.download_count_str));
		mAppSize.setText(info.getAppSizeStr());
		mAppVersion.setText(info.getVersionName());
		String source = info.getMarket();
		if (source != null && !source.isEmpty()) {
			mAppSourceInfo.setText(source);
			mAppSource.setVisibility(View.VISIBLE);
			mAppSourceInfo.setVisibility(View.VISIBLE);
		} else {
			mAppSource.setVisibility(View.GONE);
			mAppSourceInfo.setVisibility(View.GONE);
		}

		Log.v(TAG, "aurora.jiangmx " + info.getCreateTime());

		mAppUpdateTime.setText(TimeUtils.getFormatDate(Long.parseLong(info
				.getCreateTime())));
		mAppDeveloper.setText(info.getDeveloper());
		mAppCategory.setText(info.getCategory());

		// 开始头像图片异步加载
		if (SystemUtils.isLoadingImage(this)) {
			imageLoader.displayImage(info.getIcons().getPx256(), mappavatar,
					opIconsImage);
		}
		if (isUpdate) {
			mAppTitle.setText(getString(R.string.app_update_desc));

			if (TextUtils.isEmpty(info.getChangelog())) {
				mAppDescContent
						.setText(getString(R.string.app_detail_no_desctext));
			} else {
				mAppDescContent.setText(info.getChangelog());
			}
		} else {
			mAppTitle.setText(getString(R.string.app_intro));
			mAppDescContent.setText(info.getDescription());
		}
		boolean ifFold = false;
		boolean ifHide = false;
		int lines = mAppDescContent.getLineCount();
		if (lines > 3) {
			ifFold = true;
			ifHide = true;
			mAppDescContent.setLines(3);
			mAppDescContent.setCollapseLines(3, true);
			mmoreView.setBackgroundResource(R.drawable.page_content_arrow_down);
			mAppRelacContent.setOnClickListener(new ShowListener(ifFold,
					ifHide, mAppDescContent, mmoreView, lines));

		} else {
			ifFold = false;
			ifHide = false;
			mAppDescContent.setLines(lines);
			mAppDescContent.setCollapseLines(lines, false);
			mmoreView.setVisibility(View.GONE);
			mmoreView.setBackgroundResource(R.drawable.page_content_arrow_up);
		}

		setupAppDetailDisplay(info.getScreenshots());

		loadingPageUtil.hideLoadPage();
		// showLoadingView(false);
	}

	private void initViews() {
		mAppPicBrowseLayout = (LinearLayout) findViewById(R.id.app_pic_browse_view);

		mAppRelacContent = (View) findViewById(R.id.app_desc_content);
		mappavatar = (ImageView) findViewById(R.id.app_avatar);
		mDownloadBtn = (ImageView) findViewById(R.id.download_btn);
		mAppname = (TextView) findViewById(R.id.app_name);
		mRatingBar = (RatingBar) findViewById(R.id.app_rating);
		mAppVersion = (TextView) findViewById(R.id.version_info);
		mAppDeveloper = (TextView) findViewById(R.id.developer_info);
		mAppSource = (TextView) findViewById(R.id.market_source);
		mAppSourceInfo = (TextView) findViewById(R.id.source_info);
		mAppUpdateTime = (TextView) findViewById(R.id.update_time_info);
		mAppCategory = (TextView) findViewById(R.id.category_info);
		mAppDescContent = (ExpandableTextView) findViewById(R.id.desc_content);
		mAppTitle = (TextView) findViewById(R.id.app_title);
		mDownloadCount = (TextView) findViewById(R.id.download_count);

		mAppSize = (TextView) findViewById(R.id.app_size);
		mProgressBar = (ProgressBar) findViewById(R.id.download_progress_rate);
		mViewComment = (TextView) findViewById(R.id.view_comment);
		mmoreView = (ImageView) findViewById(R.id.expand_more_img);
		mDownloadBtnLayout = (FrameLayout) findViewById(R.id.download_btn_layout);
		mDownloadBtnInstall = (LinearLayout) findViewById(R.id.download_btn_install);
		download_text = (TextView) findViewById(R.id.download_text);
		mDownloadProLayout = (FrameLayout) findViewById(R.id.download_progress);
		dis_download_text = (TextView) findViewById(R.id.dis_download_text);
		mCancelDownloadBtn = (ImageView) findViewById(R.id.cancel_download_btn);
		mDownloadInstallView = (ImageView) findViewById(R.id.download_install);
		detail_scrollview = (ScrollView) findViewById(R.id.detail_scrollview);
		mToDownloadManagerBtn = (ImageView) findViewById(R.id.redirect_download_btn);
		if (update_status == 0) {
			setUpdateSign(0);
		} else {
			setUpdateSign(1);
		}
	}

	private void setUpdateSign(int sign_type) {
		if (sign_type == 0) {
			main_update.setVisibility(View.GONE);
		} else {
			main_update.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 检查应用的状态, 并显示相应布局
	 */
	public void checkSoftwareState(boolean type) {

		// 检测是否安装
		InstalledAppInfo installedAppInfo = InstallAppManager
				.getInstalledAppInfo(this, downloaddata.getPackageName());

		// 未安装的情况
		if (installedAppInfo == null) {
			FileDownloader downloader = AppDownloadService.getDownloaders()
					.get(downloaddata.getApkId());

			// 如果下载器任务存在, 显示各状态信息
			if (downloader != null) {
				int status = downloader.getStatus();
				current_status = status;
				mDownloadBtnLayout.setVisibility(View.GONE);
				mDownloadProLayout.setVisibility(View.VISIBLE);

				long downloadSize = downloader.getDownloadSize();
				long fileSize = downloader.getFileSize();
				double pre = 0;
				if (fileSize != 0) {
					pre = (downloadSize * 1.0) / fileSize;
				}
				int progress = (int) (pre * 100);
				if (status == FileDownloader.STATUS_DOWNLOADING) {

					String test = String.format(
							getResources().getString(
									R.string.download_process_tip), progress)
							+ getString(R.string.download_process_sign);
					dis_download_text.setText(test);
					mProgressBar.setProgress(progress);
				} else if ((status == FileDownloader.STATUS_WAIT)
						|| (status == FileDownloader.STATUS_CONNECTING)) {

					dis_download_text
							.setText(getString(R.string.download_process_wait));
					mProgressBar.setProgress(0);
				} else {
					String test = String.format(
							getResources().getString(
									R.string.download_process_pause), progress)
							+ getString(R.string.download_process_sign);
					dis_download_text.setText(test);
					mProgressBar.setProgress(progress);
				}
			} else { // 任务完成或者没有记录
				// long start = System.currentTimeMillis();
				DownloadData tempData = AppDownloadService.getAppDownloadDao()
						.getDownloadData(downloaddata.getApkId());
				// long end = System.currentTimeMillis();
				// Log.i(TAG, "db getDownloadData time: " + (end - start));

				if (null == tempData) {
					mDownloadProLayout.setVisibility(View.GONE);
					mDownloadBtnLayout.setVisibility(View.VISIBLE);
					download_text.setText(R.string.app_download);
				} else {
					int status = tempData.getStatus();

					current_status = status;
					String fileDir = tempData.getFileDir();
					fileDir = fileDir == null ? "" : fileDir;
					String fileName = tempData.getFileName();
					fileName = fileName == null ? "" : fileName;
					final File file = new File(fileDir, fileName);
					// 查看数据库中该任务状态是否为完成, 并且文件是存在的
					if (((status == FileDownloader.STATUS_INSTALLFAILED) || (status == FileDownloader.STATUS_INSTALLED))
							&& file.exists()) {

						if (status == FileDownloader.STATUS_INSTALLED
								&& ((mDownloadProLayout.getVisibility() == View.VISIBLE) || (mDownloadBtnInstall
										.getVisibility() == View.VISIBLE))) {
							return;
						}
						if(status == FileDownloader.STATUS_INSTALLFAILED)
						{
							mDownloadBtnInstall.setVisibility(View.GONE);
							mDownloadProLayout.setVisibility(View.GONE);
							mDownloadBtnLayout.setVisibility(View.VISIBLE);
							download_text.setText(R.string.app_install);
						}
						else
						{
						mDownloadBtnLayout.setVisibility(View.VISIBLE);
						mDownloadProLayout.setVisibility(View.GONE);
						download_text.setText(R.string.app_install);
						}
					} else if (status >= FileDownloader.STATUS_INSTALL_WAIT
							&& file.exists()) {
						// download_text.setText(R.string.app_install);

						mDownloadProLayout.setVisibility(View.GONE);
						
						
						if(status == FileDownloader.STATUS_INSTALLFAILED)
						{
							mDownloadBtnInstall.setVisibility(View.GONE);
							mDownloadBtnLayout.setVisibility(View.VISIBLE);
							download_text.setText(R.string.app_install);
						}
						else
						{
							mDownloadBtnLayout.setVisibility(View.GONE);
							mDownloadBtnInstall.setVisibility(View.VISIBLE);
						}
						
						
						
						
						animationDrawable = (AnimationDrawable) mDownloadInstallView
								.getBackground();

						animationDrawable.start();
					} else { // 条件不符合则显示下载
						mDownloadProLayout.setVisibility(View.GONE);
						mDownloadBtnLayout.setVisibility(View.VISIBLE);
						download_text.setText(R.string.app_download);
					}
				}
			}
		} else {
			// 这里判断是否为最新版本
			/*
			 * if(installedAppInfo.getStatus() !=
			 * AppInstallService.OPERATION_FINISH_INSTALL) {
			 * mDownloadProLayout.setVisibility(View.GONE);
			 * mDownloadBtnLayout.setVisibility(View.GONE);
			 * mDownloadBtnInstall.setVisibility(View.VISIBLE);
			 * animationDrawable=(AnimationDrawable)
			 * mDownloadInstallView.getBackground();
			 * 
			 * animationDrawable.start(); } else {
			 */
			if ((null != animationDrawable) && (animationDrawable.isRunning()))
				animationDrawable.stop();
			if (downloaddata.getVersionCode() > installedAppInfo
					.getVersionCode()) { // 不是最新版本
				FileDownloader downloader = AppDownloadService.getDownloaders()
						.get(downloaddata.getApkId());
				// 如果下载器任务存在, 显示各状态信息
				if (downloader != null) {
					mDownloadBtnInstall.setVisibility(View.GONE);
					mDownloadBtnLayout.setVisibility(View.GONE);
					mDownloadProLayout.setVisibility(View.VISIBLE);
					int status = downloader.getStatus();
					current_status = status;
					long downloadSize = downloader.getDownloadSize();
					long fileSize = downloader.getFileSize();
					double pre = 0;
					if (fileSize != 0) {
						pre = (downloadSize * 1.0) / fileSize;
					}
					int progress = (int) (pre * 100);
					if (status == FileDownloader.STATUS_DOWNLOADING) {

						String test = String.format(
								getResources().getString(
										R.string.download_process_tip),
								progress)
								+ getString(R.string.download_process_sign);
						dis_download_text.setText(test);
						mProgressBar.setProgress(progress);
					} else {
						String test = String.format(
								getResources().getString(
										R.string.download_process_pause),
								progress)
								+ getString(R.string.download_process_sign);
						dis_download_text.setText(test);
						mProgressBar.setProgress(progress);
					}
				} else { // 任务完成或者没有记录
					// long start = System.currentTimeMillis();

					DownloadData tempData = AppDownloadService
							.getAppDownloadDao().getDownloadData(
									downloaddata.getApkId());
					// long end = System.currentTimeMillis();
					// Log.i(TAG, "db getDownloadData time: " + (end - start));
					if (tempData == null) {
						if (!type) {
							isUpdate = true;
						}
						if ((null != animationDrawable) && (animationDrawable.isRunning()))
							animationDrawable.stop();
						mDownloadBtnInstall.setVisibility(View.GONE);
						mDownloadBtnLayout.setVisibility(View.VISIBLE);
						mDownloadProLayout.setVisibility(View.GONE);
						download_text.setText(R.string.download_process_update);
					} else {
						int status = tempData.getStatus();

						current_status = status;
						String fileDir = tempData.getFileDir();
						fileDir = fileDir == null ? "" : fileDir;
						String fileName = tempData.getFileName();
						fileName = fileName == null ? "" : fileName;
						final File file = new File(fileDir, fileName);
						// 查看数据库中该任务状态是否为完成, 并且文件是存在的

						if (tempData.getVersionCode() == downloaddata
								.getVersionCode()) {

							if (status == FileDownloader.STATUS_INSTALLING) { // 安装中
								mDownloadProLayout.setVisibility(View.GONE);
								mDownloadBtnLayout.setVisibility(View.GONE);
								mDownloadBtnInstall.setVisibility(View.VISIBLE);
								animationDrawable = (AnimationDrawable) mDownloadInstallView
										.getBackground();

								animationDrawable.start();
							} else if (((status == FileDownloader.STATUS_INSTALLFAILED) || (status == FileDownloader.STATUS_INSTALLED))
									&& file.exists()) {

								if (status == FileDownloader.STATUS_INSTALLED
										&& ((mDownloadProLayout.getVisibility() == View.VISIBLE) || (mDownloadBtnInstall
												.getVisibility() == View.VISIBLE))) {
									return;
								}

								if ((null != animationDrawable) && (animationDrawable.isRunning()))
									animationDrawable.stop();
								mDownloadBtnInstall.setVisibility(View.GONE);//aurora ukiliu added for BUG #10913
								mDownloadBtnLayout.setVisibility(View.VISIBLE);
								mDownloadProLayout.setVisibility(View.GONE);
								download_text.setText(R.string.app_install);
							} else { // 条件不符合则显示下载
								if (!type) {
									isUpdate = true;
								}
								if (status >= FileDownloader.STATUS_INSTALL_WAIT
										&& (mDownloadProLayout.getVisibility() == View.VISIBLE)) {
									return;
								}
								if ((null != animationDrawable) && (animationDrawable.isRunning()))
									animationDrawable.stop();
								mDownloadBtnInstall.setVisibility(View.GONE);
								mDownloadBtnLayout.setVisibility(View.VISIBLE);
								mDownloadProLayout.setVisibility(View.GONE);
								download_text
										.setText(R.string.download_process_update);
							}
						} else {
							if (!type) {
								isUpdate = true;
							}
							if (status >= FileDownloader.STATUS_INSTALL_WAIT
									&& (mDownloadProLayout.getVisibility() == View.VISIBLE)) {
								return;
							}
							mDownloadBtnLayout.setVisibility(View.VISIBLE);
							mDownloadProLayout.setVisibility(View.GONE);
							download_text
									.setText(R.string.download_process_update);

						}
					}
				}

			} else { // 如果是最新版本
				if ((null != animationDrawable)
						&& (animationDrawable.isRunning()))
					animationDrawable.stop();
				mDownloadBtnInstall.setVisibility(View.GONE);
				mDownloadBtnLayout.setVisibility(View.VISIBLE);
				mDownloadProLayout.setVisibility(View.GONE);
				download_text.setText(R.string.item_open);
				
				
				Animation anim = AnimationUtils.loadAnimation(
						MarketDetailActivity.this, R.anim.scale1);
				
				anim.setFillAfter(true);
				Animation anim1 = AnimationUtils.loadAnimation(
						MarketDetailActivity.this, R.anim.scale2);
				anim1.setFillAfter(true);
				anim.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						
					/*	Animation anim2 = AnimationUtils.loadAnimation(
								MarketDetailActivity.this, R.anim.scale3);
						
						anim2.setFillAfter(true);
						mDownloadBtn.startAnimation(anim2);*/
					}
				});
			
				/*
				 * if (!type) isOpenAnimal = false;
				 */
				if ((current_status == FileDownloader.STATUS_INSTALLING)
						&& isOpenAnimal && type) {
					mDownloadBtn.startAnimation(anim);
					
					download_text.startAnimation(anim1);
					//mDownloadBtn.startAnimation(anim2);
					
					// dotheOpenOpr();
					isOpenAnimal = false;
				}
				current_status = downloaddata.getStatus();
			}
		}
		// }

	}

	private void setListener() {

		mDownloadBtn.setOnClickListener(this);

		mViewComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});

		mCancelDownloadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AppDownloadService.cancelDownload(MarketDetailActivity.this,
						downloaddata);

			}
		});

		mToDownloadManagerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent lInt = new Intent(MarketDetailActivity.this,
						DownloadManagerActivity.class);
				startActivity(lInt);
			}
		});
	}

	private void setShowExPandAnimal() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int lines = msg.what;
				// 这里接受到消息，让后更新TextView设置他的maxLine就行了

				mAppDescContent.setLines(lines);
				mAppDescContent.setCollapseLines(lines, true);
			}
		};
		if (thread != null)
			handler.removeCallbacks(thread);

		Thread thread = new Thread() {
			@Override
			public void run() {
				int count = mAppDescContent.getLineCount();
				while (count-- > 3) {
					// 每隔20mms发送消息
					Message message = new Message();
					message.what = count;
					handler.sendMessage(message);

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				super.run();
			}
		};
		thread.start();

	}

	class ShowListener implements OnClickListener {
		private boolean ifFold;
		private boolean ifHide;
		private ExpandableTextView content;
		private ImageView show;
		private int lines;

		public ShowListener(boolean ifFold, boolean ifHide,
				ExpandableTextView content, ImageView show, int lines) {
			this.ifFold = ifFold;
			this.ifHide = ifHide;
			this.content = content;
			this.show = show;
			this.lines = lines;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (ifFold) {
				if (!ifHide) {
					show.setVisibility(View.VISIBLE);
					mmoreView
							.setBackgroundResource(R.drawable.page_content_arrow_down);
					/*
					 * content.setLines(3); content.setCollapseLines(3, true);
					 */
					ifHide = true;
					setShowExPandAnimal();

				} else {
					show.setVisibility(View.VISIBLE);
					content.setLines(lines);
					content.setCollapseLines(lines, true);
					mmoreView
							.setBackgroundResource(R.drawable.page_content_arrow_up);
					ifHide = false;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							detail_scrollview.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});

				}
			}
		}

	}

	private void initActionBar() {
		mActionBar = getCustomActionBar();
		mActionBar.setTitle(R.string.app_detail_page);
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
		/*mActionBar.setDefaultOptionItemDrawable(getResources().getDrawable(
				R.drawable.btn_main_right_selector));*/
		mActionBar.showDefualtItem(false);
		mActionBar.addItemView(R.layout.actionbar_main_right);
		
		main_update = (ImageView) mActionBar.findViewById(R.id.actionbar_main_update);
		View view = mActionBar.findViewById(R.id.download_layout);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MarketDetailActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initdata() {

		// showLoadingView(true);
		getNetData();

	}

	private void showLoadingView(boolean pIsShow) {

		if (null == mLoadingImg && null == mLoadingView) {

			mLoadingView = (LinearLayout) findViewById(R.id.loading_view);
			mLoadingImg = (ProgressBar) mLoadingView
					.findViewById(R.id.loading_img);

		}

		if (pIsShow) {
			mLoadingView.setVisibility(View.VISIBLE);
			// mLoadingImg.startAnimation( createRotateAnimation() );
		} else
			mLoadingView.setVisibility(View.GONE);

	}

	private void upDownLoadData() {
		appiteminfo list = obj.getAppInfo();
		downloaddata.setApkId(list.getId());
		downloaddata.setApkDownloadPath(list.getDownloadURL());
		downloaddata.setApkLogoPath(list.getIcons().getPx256());
		downloaddata.setApkName(list.getTitle());
		downloaddata.setVersionCode(list.getVersionCode());
		downloaddata.setVersionName(list.getVersionName());

	}

	private void getNetData() {
		mmarketManager.getDetailsItems(new DataResponse<detailsObject>() {
			public void run() {
				if (value != null) {
					Log.i(TAG, "the value=" + value.getCode());
					obj = value;
					if (null != obj)
						upDownLoadData();
					disView();
				}
			}

		}, MarketDetailActivity.this, downloaddata.getPackageName());
	}

	private void setupAppDetailDisplay(String[] icons) {
       
	    ImageView lImg = null;
		View lView = null;
		PicBrowseUtils.resetImgVContainer();
		/*
		 * DisplayMetrics metric = new DisplayMetrics();
		 * getWindowManager().getDefaultDisplay().getMetrics(metric);
		 * mScreenWidth = metric.widthPixels; mScreenHeight =
		 * metric.heightPixels;
		 * 
		 * if( mScreenWidth == SystemUtils.U3_SCREEN_WIDTH && mScreenHeight ==
		 * SystemUtils.U3_SCREEN_HEIGHT ){ mIsU3 = true;
		 * mAppPicBrowseLayout.setLayoutParams( new
		 * FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		 * DensityUtil.dip2px(this, 195))); }else{ mIsU3 = false;
		 * mAppPicBrowseLayout.setLayoutParams( new
		 * FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		 * DensityUtil.dip2px(this, 214))); }
		 */
		mAppPicBrowseLayout
				.setLayoutParams(new FrameLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, getResources()
								.getDimensionPixelOffset(
										R.dimen.app_detail_pic_browse)));
		if ((icons.length == 0) || (!SystemUtils.isLoadingImage(this))) {
			Bitmap bit = BitmapFactory.decodeResource(getResources(),
					R.drawable.page_thumbnail);
			int h = bit.getHeight();
			int w = getResources().getDimensionPixelOffset(
					R.dimen.app_detail_pic_browse)
					* bit.getWidth() / h;

			/*
			 * if(!mIsU3) w= DensityUtil.dip2px(this, 214) * bit.getWidth() / h;
			 * else w= DensityUtil.dip2px(this, 195) * bit.getWidth() / h;
			 */
			for (int i = 0; i < 3; i++) {

				lImg = new ImageView(this);
				// lImg.setBackgroundColor(Color.BLACK); //
				// setBackgroundResource(pResIds[i]);
				/*
				 * if(!mIsU3) lImg.setLayoutParams(new
				 * LinearLayout.LayoutParams(w, DensityUtil.dip2px(this, 214)));
				 * else lImg.setLayoutParams(new LinearLayout.LayoutParams(w,
				 * DensityUtil.dip2px(this, 195)));
				 */
				lImg.setLayoutParams(new LinearLayout.LayoutParams(w,
						getResources().getDimensionPixelOffset(
								R.dimen.app_detail_pic_browse)));
				/*
				 * lImg.setLayoutParams(new
				 * LayoutParams(LayoutParams.WRAP_CONTENT, 640));
				 */
				lImg.setTag(i);
				lImg.setScaleType(ScaleType.FIT_XY);
				lImg.setBackgroundResource(R.drawable.page_thumbnail);
				if (i == 0) {
					mAppPicBrowseLayout.addView(addDivider(52));
				} else {
					mAppPicBrowseLayout.addView(addDivider(36));
				}

				mAppPicBrowseLayout.addView(lImg);

				if (i == 2) {
					mAppPicBrowseLayout.addView(addDivider(51));
				}

			}
		} else {
			for (int i = 0; i < icons.length; i++) {

				lImg = new ImageView(this);
				// lImg.setBackgroundColor(Color.BLACK); //
				// setBackgroundResource(pResIds[i]);

				/*
				 * if(!mIsU3) lImg.setLayoutParams(new LayoutParams(
				 * LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(this, 214)));
				 * else lImg.setLayoutParams(new LayoutParams(
				 * LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(this, 195)));
				 */
				lImg.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, getResources()
								.getDimensionPixelOffset(
										R.dimen.app_detail_pic_browse)));
				lImg.setTag(i);
				lImg.setScaleType(ScaleType.FIT_XY);
				
				if (mIsU3)
					lImg.setPadding(0, DensityUtil.dip2px(this, 10), 0,
							DensityUtil.dip2px(this, 10));

				lImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    
						String index = v.getTag().toString();

						Intent intent = new Intent(MarketDetailActivity.this,
								PictureViewActivity.class);
						
						intent.putExtra("index", Integer.valueOf(index));
						intent.putExtra("content", obj.getAppInfo()
								.getScreenshots());
						
						startActivity(intent);
						overridePendingTransition(0, 0);
						//view.setDrawingCacheEnabled(false);

					}
				});
				// DensityUtil.dip2px(context, dpValue)
				if (SystemUtils.isLoadingImage(this)) {
					imageLoader.displayImage(icons[i], lImg, optionsImage,
							animateFirstListener);
				}
				if (i == 0) {
					mAppPicBrowseLayout.addView(addDivider(DensityUtil.dip2px(
							this, 17)));
				} else {
					mAppPicBrowseLayout.addView(addDivider(DensityUtil.dip2px(
							this, 12)));
				}

				mAppPicBrowseLayout.addView(lImg);
				 int[] location = new  int[2] ;
			        
			     final Rect rect = new Rect();
			     mAppPicBrowseLayout.getLocationOnScreen(location);
				
				Log.v("aurora.jiangmx detail", "layout location top: " + location[1] + "top: " + rect.top);				 
				PicBrowseUtils.addImgV(lImg);
				
				if (i == icons.length - 1) {
					mAppPicBrowseLayout.addView(addDivider(DensityUtil.dip2px(
							this, 17)));
				}

			}
		}
	}

	private class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				final ImageView imageView = (ImageView) view;
				// aurora ukiliu add 2014-09-10 begin
				if (loadedImage.getHeight() < loadedImage.getWidth()) {
					loadedImage = BitmapUtil.rotateBitmap(loadedImage, 90);
				}
				// aurora ukiliu add 2014-09-10 end

				int h = loadedImage.getHeight();
				int w = getResources().getDimensionPixelOffset(
						R.dimen.app_detail_pic_browse)
						* loadedImage.getWidth() / h;
				Log.i(TAG, "zhangwei the h="+h+" the w="+w);
				/*
				 * if(!mIsU3) w= DensityUtil.dip2px(MarketDetailActivity.this,
				 * 214) * loadedImage.getWidth() / h; else w=
				 * DensityUtil.dip2px(MarketDetailActivity.this, 195) *
				 * loadedImage.getWidth() / h;
				 */

				imageView.setLayoutParams(new LinearLayout.LayoutParams(w,
						getResources().getDimensionPixelOffset(
								R.dimen.app_detail_pic_browse)));
				
				imageView.setImageBitmap(loadedImage);
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}

			}
		}

		/**
		 * @Title: runOnUiThread
		 * @Description: TODO(这里用一句话描述这个方法的作用)
		 * @param @param runnable
		 * @return void
		 * @throws
		 */
		private void runOnUiThread(Runnable runnable) {
			// TODO Auto-generated method stub

		}
	}

	private View addDivider(int pDividerLen) {
		View lView = new View(this);
		lView.setBackgroundColor(Color.WHITE);
		lView.setLayoutParams(new LayoutParams(pDividerLen, 640));

		return lView;
	}

	private RotateAnimation createRotateAnimation() {
		RotateAnimation animation = null;
		animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(10000);
		animation.setStartOffset(0);
		animation.setRepeatCount(1000);
		return animation;
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		// TODO Auto-generated method stub
		switch (code) {
		case INotifiableController.CODE_UNKNONW_HOST:
		case INotifiableController.CODE_WRONG_DATA_FORMAT:
		case INotifiableController.CODE_REQUEST_TIME_OUT:
		case INotifiableController.CODE_CONNECT_ERROR:
		case INotifiableController.CODE_GENNERAL_IO_ERROR:
		case INotifiableController.CODE_NOT_FOUND_ERROR:
		case INotifiableController.CODE_JSON_PARSER_ERROR:
		case INotifiableController.CODE_JSON_MAPPING_ERROR:
		case INotifiableController.CODE_UNCAUGHT_ERROR:
			mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
			break;
		case INotifiableController.CODE_NOT_NETWORK:
			mHandler.sendEmptyMessage(Globals.NO_NETWORK);
			break;
		default:
			break;
		}
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			switch (msg.what) {
			case 0:

				mDownloadBtn.getBackground().setAlpha(255);
				mDownloadBtn.setScaleY(1.0f);
				mDownloadBtn.setScaleX(1.0f);
				download_text.setTextSize(18);
				break;

			case Globals.NETWORK_ERROR:
				if (loadingPageUtil.isShowing()) {
					loadingPageUtil.showNetworkError();
				}
				break;
			case Globals.NO_NETWORK:
				if (loadingPageUtil.isShowing()) {
					loadingPageUtil.showNoNetWork();
				}
				break;
			default:
				break;
			}

		}

	};

	public void setAnimal1() {
		// int[] loc = new int[2];

		mProgressBar.setProgress(0);
		/*
		 * dis_download_text.getLocationInWindow(loc); ViewGroup flayout =
		 * (ViewGroup) getWindowLayout(); Rect rect = new Rect();
		 * dis_download_text.getHitRect(rect);
		 * 
		 * flayout.offsetDescendantRectToMyCoords(dis_download_text, rect);
		 */
		TranslateAnimation animation = new TranslateAnimation(0, 0,
				-DensityUtil.dip2px(MarketDetailActivity.this, 5), 0);

		AlphaAnimation animation2 = new AlphaAnimation(0, 1.0f);

		AnimationSet set = new AnimationSet(true);
		set.setDuration(350);//AURORA UKILIU MODIFY 2014-10-10 END
		set.setInterpolator(new DecelerateInterpolator());
		set.addAnimation(animation);
		set.addAnimation(animation2);
		// download_process_wait
		dis_download_text.setText(getString(R.string.download_process_wait));
		dis_download_text.startAnimation(set);

	}

	private void dotheDownOpr() {
		CustomAnimation animation = new CustomAnimation(
				new CustomAnimCallBack() {
					int isStartAnimal = 0;
					int isEndAnimal = 0;

					@Override
					public void callBack(float interpolatedTime,
							Transformation t) {

						Log.i(TAG, "the interpolatedTime=" + interpolatedTime);
						if (isEndAnimal == 1)
							return;

						//AURORA UKILIU MODIFY 2014-10-10 BEGIN
						mDownloadBtn.setScaleX(1.0f - 0.239f * interpolatedTime);
						mDownloadBtn.getBackground().setAlpha(
								(int) (255 * (1.0f - interpolatedTime)));
						mDownloadBtn.setScaleY(1.0f - 0.9f * (interpolatedTime));
						download_text
								.setTextSize(18 * (1 - 0.4f * (interpolatedTime)));
						//AURORA UKILIU MODIFY 2014-10-10 END
						if (interpolatedTime == 1.0f) {
							isEndAnimal = 1;
							mDownloadBtnLayout.setVisibility(View.GONE);
							mHandler.sendEmptyMessageDelayed(0, 200);
							mDownloadProLayout.setVisibility(View.VISIBLE);

							Animation anim1 = AnimationUtils.loadAnimation(
									MarketDetailActivity.this, R.anim.alpha);
							anim1.setInterpolator(new DecelerateInterpolator());
							/* mDownloadProLayout.startAnimation(anim1); */
							mCancelDownloadBtn.startAnimation(anim1);
							mToDownloadManagerBtn.startAnimation(anim1);
							setAnimal1();
							AppDownloadService.startDownload(
									MarketDetailActivity.this, downloaddata);
						}

					}
				});

		animation.setDuration(350);//AURORA UKILIU MODIFY 2014-10-10 END
		animation.setFillAfter(true);
		animation.setInterpolator(new DecelerateInterpolator());

		mDownloadBtn.startAnimation(animation);
	}

	private void dotheOpenOpr() {
		CustomAnimation animation = new CustomAnimation(
				new CustomAnimCallBack() {
					int isStartAnimal = 0;
					int isEndAnimal = 0;

					@Override
					public void callBack(float interpolatedTime,
							Transformation t) {

						Log.i(TAG, "the interpolatedTime=" + interpolatedTime);
						if (isEndAnimal == 1)
							return;
						mDownloadBtn.getBackground().setAlpha(
								(int) (255 * (0.1 + 0.9 * interpolatedTime)));
						mDownloadBtn.setScaleY(0.1f + 0.9f * interpolatedTime);
						download_text
								.setTextSize(18 * (0.8f + 0.2f * interpolatedTime));
						if (interpolatedTime == 1.0f) {
							isEndAnimal = 1;
							// mHandler.sendEmptyMessageDelayed(0, 200);
						}

					}
				});

		animation.setDuration(1000);
		animation.setFillAfter(true);
		animation.setInterpolator(new DecelerateInterpolator());

		mDownloadBtn.startAnimation(animation);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();
		switch (id) {
		case R.id.download_btn:

			if (!SystemUtils.hasNetwork()) {
				Toast.makeText(this,
						getString(R.string.no_network_download_toast),
						Toast.LENGTH_SHORT).show();
				return;
			}
			String dis_text = download_text.getText().toString();

			if (dis_text
					.equals(getResources().getString(R.string.app_download))
					|| dis_text.equals(getResources().getString(
							R.string.download_process_update))) {
				if (!SystemUtils.isDownload(MarketDetailActivity.this)) {

					AuroraAlertDialog mWifiConDialog = new AuroraAlertDialog.Builder(
							MarketDetailActivity.this,
							AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
							.setTitle(
									getResources().getString(
											R.string.dialog_prompt))
							.setMessage(
									getResources().getString(
											R.string.no_wifi_download_message))
							.setNegativeButton(android.R.string.cancel, null)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											SharedPreferences sp = PreferenceManager
													.getDefaultSharedPreferences(MarketDetailActivity.this);
											Editor ed = sp.edit();
											ed.putBoolean("wifi_download_key",
													false);
											ed.commit();
											dotheDownOpr();

										}

									}).create();
					mWifiConDialog.show();
				} else {
					dotheDownOpr();
				}
			} else if (dis_text.equals(getResources().getString(
					R.string.app_install))) {
				mDownloadBtnLayout.setVisibility(view.GONE);
				mDownloadBtnInstall.setVisibility(view.VISIBLE);
				animationDrawable = (AnimationDrawable) mDownloadInstallView
						.getBackground();

				animationDrawable.start();
				/*
				 * DownloadData tempData = AppDownloadService
				 * .getAppDownloadDao().getDownloadData(
				 * downloaddata.getApkId()); String fileDir =
				 * tempData.getFileDir(); fileDir = fileDir == null ? "" :
				 * fileDir; String fileName = tempData.getFileName(); fileName =
				 * fileName == null ? "" : fileName; final File file = new
				 * File(fileDir, fileName);
				 * ApkUtil.installApp(MarketDetailActivity.this, file);
				 */
				DownloadData tempData = AppDownloadService.getAppDownloadDao()
						.getDownloadData(downloaddata.getApkId());
				String fileDir = tempData.getFileDir();
				fileDir = fileDir == null ? "" : fileDir;
				String fileName = tempData.getFileName();
				fileName = fileName == null ? "" : fileName;
				final File file = new File(fileDir, fileName);
				/*
				 * PackageInstallObserver observer = new
				 * PackageInstallObserver();
				 * 
				 * SystemUtils.intstallApp(MarketDetailActivity.this,
				 * tempData.getPackageName(), file, observer);
				 */
				tempData.setStatus(FileDownloader.STATUS_INSTALL_WAIT);
				AppDownloadService.getAppDownloadDao().updateStatus(tempData.getApkId(), 
						FileDownloader.STATUS_INSTALL_WAIT);

				AppInstallService.startInstall(MarketDetailActivity.this,
						tempData, AppInstallService.TYPE_NORMAL);
				/* RootCmdUtil.slientInstall(MarketDetailActivity.this, file); */
			} else if (dis_text.equals(getResources().getString(
					R.string.item_open))) {
				ApkUtil.openApp(MarketDetailActivity.this,
						downloaddata.getPackageName());
			}

			break;
		default:
			break;
		}
	}

	class PackageInstallObserver extends
			android.content.pm.IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			Message msg = mHandler.obtainMessage(1);
			msg.arg1 = returnCode;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (stopFlag) {
			updateListener.downloadProgressUpdate();
			stopFlag = false;
		}
		AppDownloadService.registerUpdateListener(updateListener);
	}

	@Override
	protected void onStop() {
		super.onStop();

		stopFlag = true;
		AppDownloadService.unRegisterUpdateListener(updateListener);
	}

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			/*
			 * FileDownloader downloader = AppDownloadService.getDownloaders()
			 * .get(id); if(null != downloader) { long downloadSize =
			 * downloader.getDownloadSize(); long fileSize =
			 * downloader.getFileSize(); double pre = 0; if (fileSize != 0) {
			 * pre = (downloadSize * 1.0) / fileSize; } int progress = (int)
			 * (pre * 100); String test = String.format(
			 * getResources().getString( R.string.download_process_tip),
			 * progress) + getString(R.string.download_process_sign);
			 * dis_download_text.setText(test);
			 * mProgressBar.setProgress(progress); } else {
			 * mDownloadBtnLayout.setVisibility(View.VISIBLE);
			 * mDownloadProLayout.setVisibility(View.GONE);
			 * download_text.setText(R.string.app_install); }
			 */
			checkSoftwareState(true);
		}
	};
}
