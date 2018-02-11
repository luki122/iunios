package com.aurora.thememanager.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.aurora.change.adapters.WallpaperPreviewAdapter;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.change.view.CirclePageIndicator;
import com.aurora.change.view.PreviewLayout;
import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.TimeWallpaperPreviewAdapter;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeWallpaper;
import com.aurora.thememanager.fragments.HttpCallBack;
import com.aurora.thememanager.fragments.JsonHttpListener;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.parser.ThemeTimeWallpaperDetailPaser;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.TimeWallpaperDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;

public class TimeWallPapgerPreviewActivity extends BaseActivity implements  HttpCallBack,JsonParser.CallBack,DownloadStatusCallback{
	private static final int MSG_UPDATE_APPLIED_THEME_ITEM = 0;
	private static final int MSG_LOAD_THEME_COMPLETED = 1;
	private static final String TAG = "TimeWallPapgerPreviewActivity";
	private static CommonLog log = LogFactory.createLog(TAG);
	private ViewPager mViewPager;
	private List<PictureInfo> mDisplayPictureInfos;
	private List<PictureInfo> mPictureInfos;
	private Context mContext;
	private Intent mIntent;
	private TimeWallpaperPreviewAdapter mPreviewAdapter;
	private CirclePageIndicator mIndicator;
	// private ActionBar mActionBar;
	private AuroraActionBar mAuroraActionBar;
	private float mOldX = 0;
	private float mOldY = 0;

	private static final String IMAGE_CACHE_DIR = "preview_thumbs";
	private ImageResizer mImageResizer;
	private boolean mDelFlag = false;
	private String mGroupName = "";
	private static final int ITEM_DEL = 0;
	private static final int DIALOG_DEL = 0;

	private PreviewLayout mPreviewLayout;

	private String[] mPreviews;

	private Theme mCurrentTheme;
	
	private ThemeInternetHelper mThemeLoadHelper;
	
	private JsonHttpListener mHttpListener;
	
	private Parser mThemeParser;
	
	private List<Object> mThemes;
	
	private ProgressBtn mProgressBtn;
	
	private Button mApplyButton;

	private DownloadManager mDownloadManager;

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			mDownloadManager.updateProgress(mCurrentTheme);
		}
	};
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_UPDATE_APPLIED_THEME_ITEM){
				mApplyButton.setVisibility(View.VISIBLE);
				mApplyButton.setText(R.string.theme_applyed);
				mProgressBtn.setVisibility(View.GONE);
			}else if(msg.what == MSG_LOAD_THEME_COMPLETED){
			if (mThemes != null && mThemes.size() > 0) {

				int count = mThemes.size();
					Theme theme = (Theme) mThemes.get(0);
					if(theme != null){
					mPreviews = theme.previews;
					mPreviewAdapter = new TimeWallpaperPreviewAdapter(
							getApplicationContext(), mPreviews);
					mViewPager.setAdapter(mPreviewAdapter);
					mViewPager.setOnTouchListener(mTouchListener);
					mPreviewAdapter.setViewPager(mViewPager);
					mViewPager.setCurrentItem(0);

					boolean timeIsBlack = false;
					DbControl mDbControl = new DbControl(mContext);

					if (timeIsBlack) {
						mPreviewLayout.setBlackStyle(true, Color.BLACK);
					} else {
						mPreviewLayout.setBlackStyle(false, Color.WHITE);
					}

					mDbControl.close();

					mPreviewLayout.setViewPager(mViewPager);
					mPreviewLayout.setOnPageChangeListener(mPageChangeListener);
					if (mPreviews.length == 1) {
						mPreviewLayout.setSingle(true);
					} else {
						mPreviewLayout.setSingle(false);
					}
					mAuroraActionBar.setTitle(mCurrentTheme.name);
					mViewPager.setLayerType(View.LAYER_TYPE_HARDWARE, null);
					mAuroraActionBar.setVisibility(View.VISIBLE);
				
				}
			}
		}
		};
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(R.style.Theme_aurora);

		// requestWindowFeature must be called before super.onCreate, especially
		// for Android5.0 and later
		
		mDownloadManager = new DownloadManager(this);
		mDownloadManager.setCallBack(this);
		TimeWallpaperDownloadService.registerUpdateListener(updateListener);
		mThemeLoadHelper = new ThemeInternetHelper(this,CacheManager.CACHE_WALLPAPER);
		mHttpListener = new JsonHttpListener(this);
		mThemeParser = new Parser(new ThemeTimeWallpaperDetailPaser());
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		mApp.registerActivity(this);
		setupView();
		initImageCache();
		mIntent = getIntent();
		if (mIntent != null) {
			mCurrentTheme = mIntent
					.getParcelableExtra(Action.KEY_SHOW_TIME_WALL_PAPER_PREVIEW);
			if (mCurrentTheme != null ) {
				requestTheme();
				setupAuthor();
			}
		}
	}
	
	private void setupAuthor(){
		AuroraActionBar actionBar = getAuroraActionBar();
		if(actionBar != null){
			actionBar.addItem(R.layout.wallpaper_author_text, 0);
	    	TextView authorName = (TextView)actionBar.findViewById(R.id.wallpaper_author_text);
	    	authorName.setText(getResources().getString(R.string.action_bar_author_lable)+mCurrentTheme.author);
		}
	}

	/**
	 * 请求网络数据
	 * @param page
	 */
	private void requestTheme(){
		mThemeLoadHelper.clearRequest();
		int themeId = mCurrentTheme.themeId ^ Theme.TYPE_TIME_WALLPAPER;
		Map paramsMap = HttpUtils.createPostMap("id", themeId, this);
		mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_TIME_WALLPAPER_DETAIL_REQUEST_URL,mHttpListener,HttpUtils.createPostParamsFromMap(paramsMap));
		mThemeLoadHelper.startRequest();
	}
	
	private void setupView() {
		mContext = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setAuroraPicContentView(R.layout.time_wallpaper_preview_layout);
		mViewPager = (ViewPager) findViewById(R.id.wallpaper_preview_pager);
		mIndicator = (CirclePageIndicator) findViewById(R.id.wallpaper_preview_indicator);
		mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.action_bar_gradient_bg_in_preview));
//		mAuroraActionBar.getBackground().setAlpha(140);
		mAuroraActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
		mPreviewLayout = (PreviewLayout) findViewById(R.id.preview_layout);
		mProgressBtn =(ProgressBtn) findViewById(R.id.hotizontal_progress_btn);
		mApplyButton = (Button)findViewById(R.id.btn_apply_theme);
		mViewPager.setOffscreenPageLimit(3);
	}

	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			
			}
		}
	};

	OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mOldX = x;
				mOldY = y;
				break;
			case MotionEvent.ACTION_UP:
				int dx = Math.abs((int) (x - mOldX));
				int dy = Math.abs((int) (y - mOldY));
				if (dx < 5 && dy < 5) {
					toggleActionBar();
				}
				break;

			default:
				break;
			}
			return false;
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleActionBar() {
		boolean show = mAuroraActionBar.getVisibility() == View.VISIBLE;
		if (!show) {
			mAuroraActionBar.setVisibility(View.VISIBLE);
		} else {
			mAuroraActionBar.setVisibility(View.GONE);
		}
	}

	OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
//			setAuroraTitle(position);
			mPreviewLayout.onPageSelected(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (!mPreviewLayout.getSingle()) {
				mPreviewLayout.onPageScrolled(position, positionOffset,
						positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	protected void onDestroy() {
		if (mPreviewAdapter != null) {
			mPreviewAdapter.clearData();
		}
		TimeWallpaperDownloadService.unRegisterUpdateListener(updateListener);
		super.onDestroy();
	};

	@Override
	protected void onPause() {
		if (mPreviewAdapter != null) {
			mPreviewAdapter.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mPreviewAdapter != null) {
			mPreviewAdapter.onResume();
		}
		super.onResume();
		if (mPreviewLayout.getSingle()) {
			mPreviewLayout.updateSingleTime();
		}
		mDownloadManager.updateProgress(mCurrentTheme);
		
		
	}

	private void initImageCache() {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// final int longest = (height > width ? height : width) / 2;
		// mImageResizer = new ImageResizer(mContext, longest);
		mImageResizer = new ImageResizer(mContext, width /* / 2 */, height /* / 2 */);
		// mImageResizer.setLoadingImage(R.drawable.preview_loading);
		mImageResizer.addImageCache(this, IMAGE_CACHE_DIR);
	}

	private void setAuroraTitle(int index) {
		String title = (index + 1) + "";
		int listIndex = 0;
		if (mPictureInfos != null && mPictureInfos.size() > 0) {
			if (index >= mPictureInfos.size()) {
				listIndex = index % mPictureInfos.size();
			} else {
				listIndex = index;
			}
			title = (listIndex + 1) + "/" + mPictureInfos.size();
		}
		mAuroraActionBar.setTitle(title);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DEL:
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
					this);
			builder.setTitle(R.string.wallpaper_crop_del);
			builder.setMessage(R.string.wallpaper_crop_del_msg);
			builder.setCancelable(true);
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delWallpapers(mContext, mGroupName);
							finish();
						}
					});
			return builder.show();
		}
		return null;
	}

	private void delWallpapers(Context context, String groupName) {
		String path = Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH
				+ groupName;
		FileHelper.deleteDirectory(path);
		DbControl dbControl = new DbControl(context);
		dbControl.delPictureGroupByName(groupName);
		dbControl.close();
	}

	private List<PictureInfo> getAllPicture(List<PictureInfo> oldPictures) {
		List<PictureInfo> lists = new ArrayList<PictureInfo>();
		int listIndex = 0;
		int size = oldPictures.size();
		if (size == 1) {
			lists.add(oldPictures.get(size - 1));
		} else {
			for (int i = 0; i < 12; i++) {
				if (i >= size && size != 0) {
					listIndex = i % size;
				} else {
					listIndex = i;
				}
				lists.add(oldPictures.get(listIndex));
			}
		}
		return lists;
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
			
			mThemeParser.setCallBack(this);
			mThemes = mThemeParser.startParser(response.toString());
			if (mThemes != null) {
				mHandler.sendEmptyMessage(MSG_LOAD_THEME_COMPLETED);
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

	@Override
	public void onParserSuccess(boolean success, int statusCode, String desc,
			int totalPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOperationUpdate(final DownloadData data,
			OnClickListener onClickListener) {
		// TODO Auto-generated method stub
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeWallpaperDownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		
		// ProgressBtn
		mProgressBtn.setBtnText(mProgressBtn.getResources().getString(R.string.donwloadman_update));
		mProgressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		mProgressBtn.setOnNormalClickListener(clickListener);
		/*if (onClickListener != null) {
			progressBtn.setOnButtonClickListener(onClickListener);
		} else {
			progressBtn.setOnButtonClickListener(null);
		}*/
		final int downloadId = (int) data.downloadId;
		mProgressBtn.setOnBeginAnimListener(new OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				FileDownloader downloader = TimeWallpaperDownloadService.getDownloaders(TimeWallPapgerPreviewActivity.this)
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
		mHandler.sendEmptyMessage(MSG_UPDATE_APPLIED_THEME_ITEM);
	}

	@Override
	public void showAppling(DownloadData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOperationDownload(final DownloadData data) {
		// TODO Auto-generated method stub
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeWallpaperDownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		
		// ProgressBtn
//		mApplyButton.setVisibility(!mFromLocal?View.GONE:View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		mProgressBtn.showDownloadButtonWithoutText();
		mProgressBtn.setOnNormalClickListener(clickListener);
		final int downloadId = data.downloadId;
		mProgressBtn.setOnBeginAnimListener(new OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				FileDownloader downloader = TimeWallpaperDownloadService.getDownloaders(TimeWallPapgerPreviewActivity.this)
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
	public void showWaitApply(final DownloadData data) {
		// TODO Auto-generated method stub
		Log.d("app", "showWaitApply");
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				final ThemeManager tm = ThemeManager.getInstance(ThemeConfig.THEME_TIMES);
				tm.setCallBack(new ThemeOperationCallBack() {
					
					@Override
					public void onProgressUpdate(int progress) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onCompleted(boolean success, int statusCode) {
						// TODO Auto-generated method stub
						mCurrentTheme.downloadId = mCurrentTheme.themeId;
						tm.setTimeWallpaperApplied(mCurrentTheme, mContext);
						Message msg = new Message();
						msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
						mHandler.sendMessage(msg);
					}
					
					@Override
					public Context getContext() {
						// TODO Auto-generated method stub
						return mContext;
					}
				});
				onClickApply(TimeWallPapgerPreviewActivity.this, mCurrentTheme.name+data.downloadId,data.downloadId);
				mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_INSTALLING);
				Message msg = new Message();
				msg.what = MSG_UPDATE_APPLIED_THEME_ITEM;
				mHandler.sendMessageDelayed(msg, 500);
				mApplyButton.setVisibility(View.GONE);
			}
		};
		mProgressBtn.setVisibility(View.VISIBLE);
		mProgressBtn.setOnFoucsClickListener(listener);
		mProgressBtn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
		mApplyButton.setVisibility(View.GONE);
	}

	
	 private void onClickApply(Context context, String currentGroup,int downloadId) {
	        try {
	            boolean isCopyRight = false;
	            String currentPath = WallpaperUtil.getCurrentLockPaperPath(context, currentGroup);
	            //shigq add start
	            if (currentPath.contains(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/")) {
					File mFile = new File(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/");
					if (mFile.isDirectory()) {
						File[] files = mFile.listFiles();
						for (File myFile : files) {
							Arrays.sort(files, new Comparator<File>() {
								@Override
								public int compare(File file1, File file2) {
									// TODO Auto-generated method stub
									return file1.getName().compareToIgnoreCase(file2.getName());
								}
							});
							break;
						}
						if (files.length == 4) {
							String filePath = files[2].toString();
							
							String fileName = filePath.replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/", "");
							currentPath = Consts.NEXTDAY_WALLPAPER_SAVED + fileName.replace(".jpg", "_comment.jpg");
							if (!FileHelper.fileIsExist(currentPath)) {
								Log.d("Wallpaper_DEBUG", "WallpaperLocalAdapter--------onClickApply------- currentPath file is not exist!!!!"+currentPath);
								currentPath = files[2].toString();
							}
							Log.d("Wallpaper_DEBUG", "WallpaperLocalAdapter-------onClickApply-------fileName = "+fileName+" currentPath = "+currentPath);
						}
					}
				}
	            //shigq add end
	            
	            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//	          bisCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
	            isCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, context);
	            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
	            
	            /*if (isCopyRight) {
	                mHandler.sendEmptyMessage(LOCKPAPER_SET_SUCCESS);
	            }else {
	            	mHandler.sendEmptyMessage(LOCKPAPER_SET_FAILED);
				}*/
	            DbControl controller = new DbControl(context);
	            DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, currentGroup);
	            PictureGroupInfo group = controller.queryGroupByName(currentGroup);
	            if(group != null){
	            	if(downloadId != ThemeConfig.TIME_WALLPAPER_UNUSED_ID){
	            		Theme theme = new Theme();
	            		theme.downloadId = downloadId;
	            		theme.themeId = downloadId;
	            		ThemeManager.setTimeWallpaperApplied(theme, context);
	            	}else{
	            		ThemeManager.setTimeWallpaperUnApplied(context);
	            	}
	            }
	            
	        } catch (Exception e) {
	        	Log.d("app", "catch:"+e);
	            e.printStackTrace();
	        }
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
					TimeWallpaperDownloadService.pauseOrContinueDownload(
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
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeWallpaperDownloadService.pauseOrContinueDownload(
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
		
		// ProgressBtn
		mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
		mProgressBtn.setProgress(progress);
		mProgressBtn.setOnProgressClickListener(clickListener);
		mProgressBtn.setProgressBackground(R.drawable.aurora_progress_pause);
	}

	
	

}
