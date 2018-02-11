package com.aurora.thememanager.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.change.activities.DesktopWallpaperLocalActivity;
import com.aurora.change.activities.DesktopWallpaperPreviewActivity;
import com.aurora.change.activities.SetWallpaperCropActivity;
import com.aurora.change.adapters.AuroraActionBatchHandler;
import com.aurora.change.adapters.SelectionManager;
import com.aurora.change.adapters.WallpaperLocalGridAdapter;
import com.aurora.thememanager.adapter.WallpaperPreviewAdapter;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.change.data.WallpaperValue;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.thememanager.R;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.IconUtils;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.ToastUtils;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.WallpaperDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;

public class WallPaperPreviewActivity extends BaseActivity implements DownloadStatusCallback{
	
    private static final String TAG = "WallPaperPreviewActivity";
    
    private ViewPager mViewPager;
    private Context mContext;
    private Intent mIntent;
    private String mWallpaperType;
    private WallpaperPreviewAdapter mPreviewAdapter;
    private AuroraActionBar mAuroraActionBar;
    private View mPreView;
    private RelativeLayout mContainer;
    private LayoutTransition mTransition;
    private int pos = 0;
    private float mOldX = 0;
    private float mOldY = 0;
    
    private Theme mCurrentTheme;
    private List<Theme> mThemeList;
    
	private ProgressBtn mProgressBtn;
	private Button mApplyButton;
	
	private RequestQueue mQueue;
	private final int mImageCacheSize = ThemeConfig.HttpConfig.DISKCACHE_SIZE;
	private ImageLoader mInternetImageLoader;
	private DownloadManager mDownloadManager;
	private CacheManager mCacheManager;
	
	private PreferenceManager mPrefManager;
	
	private  ThemeManager mThemeManager;
	AuroraProgressDialog mApplyProgressDialog;
	private AuroraAlertDialog.Builder mAlertDialog;
	
	private LinearLayout mPreViewIconBottomParent;
	
	private LinearLayout mPreViewIconTopParent;
	private SharedPreferences sp;
	
	private Runnable mLoadIconRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mIconUtils.setupWallPaperPreviewBottomIcons(mPreViewIconBottomParent);
			mIconUtils.setupWallpaperPreviewTopIcons(mPreViewIconTopParent);
			
		}
	};
	
	private IconUtils mIconUtils;
	
	private static final int MSG_START_APPLY_WALLPAPER = 0;

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			mDownloadManager.updateProgress(mCurrentTheme);
		}
	};
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_START_APPLY_WALLPAPER:
				mThemeManager.apply(mCurrentTheme);
				break;
			}
		};
		
	};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//requestWindowFeature must be called before super.onCreate, especially for Android5.0 and later
    	requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        mIconUtils = new IconUtils(this);
        createDialog();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mCacheManager = CacheManager.getInstance();
		mDownloadManager = new DownloadManager(this);
		mDownloadManager.setCallBack(this);
		mQueue = RequestQueue.newRequestQueue(this, mCacheManager.getPreviewDiskCache());
		mInternetImageLoader = new ImageLoaderImpl(mQueue, mCacheManager.getBitmapCache(), getResources(), getAssets()){
			@Override
			public void makeRequest(ImageRequest request) {
				// TODO Auto-generated method stub
				super.makeRequest(request);
				request.setCacheExpireTime(TimeUnit.HOURS, 1);
			}
		};
		
		WallpaperDownloadService.registerUpdateListener(updateListener);
		
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_WALLPAPER);
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
					try {
						showWallpaperSet(false);
					} catch (Exception e) {
						// TODO: handle exception
					}
					SharedPreferences.Editor editor = sp.edit();
					StringBuilder ringTonePath = new StringBuilder(mCurrentTheme.fileDir);
					ringTonePath.append("/").append(mCurrentTheme.fileName);
					editor.putString("selectpath", ringTonePath.toString());
					Log.e("101010", "----onCompleted ringTonePath = ---" + ringTonePath);
					Consts.isChangedByLocal = 2;
					Consts.isWallPaperChanged = true;
					finish();
					editor.commit();
				}
			
			}
			
			@Override
			public Context getContext() {
				// TODO Auto-generated method stub
				return WallPaperPreviewActivity.this;
			}
		});
        
        mIntent = getIntent();
        if (mIntent != null) {
        	//mCurrentTheme = mIntent.getParcelableExtra(Action.KEY_SHOW_WALL_PAPER_PREVIEW);
        	mThemeList = mIntent.getParcelableArrayListExtra("wallpaper_preview_list_theme");
            Bundle bundle = mIntent.getExtras();
            if (bundle == null) {
				return;
			}
            
            //pos = bundle.getInt("wallpaper_preview_position");
            pos = mIntent.getIntExtra("wallpaper_preview_position", 0);
            mCurrentTheme = mThemeList.get(pos);
            
            setupView();
            
            if (mCurrentTheme != null && mCurrentTheme.previews != null) {
                mPreviewAdapter = new WallpaperPreviewAdapter(getApplicationContext(), mThemeList);
                mViewPager.setAdapter(mPreviewAdapter);
                mViewPager.setOnTouchListener(mTouchListener);
                mPreviewAdapter.setViewPager(mViewPager);
                mViewPager.setCurrentItem(pos);
                mViewPager.setOnPageChangeListener(mPageChangeListener);
                mViewPager.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                
                mAuroraActionBar.setTitle(mCurrentTheme.name);
            }

        }
    }
    
	private void createDialog(){
		mApplyProgressDialog  = new AuroraProgressDialog(this);
		mApplyProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
		mApplyProgressDialog.setMessage(getResources().getString(R.string.msg_apply_wallpaper));
		
		mAlertDialog =  new AuroraAlertDialog.Builder(this)
		.setTitle(R.string.apply_theme_dialog_title)
		.setMessage(R.string.apply_theme_faliure)
		.setPositiveButton(R.string.ok,null);
	}
	
	public void applyTheme(){
		
		mHandler.sendEmptyMessageDelayed(MSG_START_APPLY_WALLPAPER, 500);
		showDialog(mApplyProgressDialog);
	}
    
    private void setupView() {
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFormat(PixelFormat.RGBA_8888); //防止PNG渐变背景图片失真问题，是否有用?有待考证

        setAuroraPicContentView(R.layout.theme_wallpaper_preview);
        mContainer = (RelativeLayout) findViewById(R.id.wallpaper_preview_container);
        mViewPager = (ViewPager) findViewById(R.id.wallpaper_preview_pager);
        mPreView =  findViewById(R.id.wallpaper_preview_show);
        mPreViewIconBottomParent = (LinearLayout)findViewById(R.id.bottom);
        mPreViewIconTopParent = (LinearLayout)findViewById(R.id.top);
        mHandler.post(mLoadIconRunnable);
		mProgressBtn =(ProgressBtn) findViewById(R.id.hotizontal_progress_btn);
		mApplyButton = (Button)findViewById(R.id.btn_apply_theme);
		mApplyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setWallpaper();
				//applyTheme();
			}
		});
        
        initAuroraActionBar();
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
    
    private void initAuroraActionBar() {
    	mAuroraActionBar = getAuroraActionBar();
    	mAuroraActionBar.setDisplayHomeAsUpEnabled(true);
    	//mAuroraActionBar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.actionbar_translucent));
		mAuroraActionBar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.action_bar_gradient_bg_in_preview));
    	//mAuroraActionBar.getBackground().setAlpha(140);
    	mAuroraActionBar.addItem(R.layout.wallpaper_author_text, 0);
    	TextView authorName = (TextView)mAuroraActionBar.findViewById(R.id.wallpaper_author_text);
    	authorName.setText(this.getResources().getText(R.string.action_bar_author_lable) + mCurrentTheme.author);
    	//mAuroraActionBar.setTitle(getString(R.string.set_wallpaper));
	}
    
	private void setWallpaper() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WallPaperPreviewActivity.this);
		try {
			InputStream is = null;
			StringBuilder ringTonePath = new StringBuilder(mCurrentTheme.fileDir);
			ringTonePath.append("/").append(mCurrentTheme.fileName);
			String filename = ringTonePath.toString();
			
			if (filename.equals(sp.getString("selectpath", ""))) {
				showWallpaperSet(false);
				finish();
				return;
			}
			
			is = getStreamFromFile(filename, true);
			
			if (is == null || (is != null && is.available() == 0)) {
				showWallpaperSet(true);
				finish();
				if(is != null) {
					is.close();
				}
				is = null;
				return;
			}

			// Aurora liugj 2014-05-20 modified for bug-4621 end
			WallpaperManager wpm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
			wpm.setStream(is);

			if (is != null) {
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				is.close();
				is = null;
				DisplayMetrics dm = this.getResources().getDisplayMetrics();
				int width = dm.widthPixels;
				int height = dm.heightPixels;
				if (bitmap != null) {
					final int wallpaperWidthBefore = bitmap.getWidth();
					final int wallpaperHeightBefore = bitmap.getHeight();
					Log.e("liugj", wallpaperWidthBefore+"--w--h--"+wallpaperHeightBefore);
					if (wallpaperWidthBefore < wallpaperHeightBefore) {
						wpm.suggestDesiredDimensions(width, height);
					} else {
						wpm.suggestDesiredDimensions(2 * width, height);
					}
				}
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
			
			Consts.isChangedByLocal = 2;
			Consts.isWallPaperChanged = true;
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("selectpath", filename);
			editor.commit();
			showWallpaperSet(false);
		} catch (Exception e) {
			showWallpaperSet(true);
			Log.e(TAG, "Failed to set wallpaper: " + e.toString());
		}
		// setResult(Activity.RESULT_OK);
		finish();
	}
	
	private InputStream getStreamFromFile(String data, boolean isLocal) {
		InputStream is = null;
		try {
			if (isLocal) {
				is = new FileInputStream(data);
			}else {
				is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), data));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}
	
	private void showWallpaperSet(boolean failed){
		Toast.makeText(this, failed ? R.string.wallpaper_set_failed : R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show();
	}
	
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
                    int dx = Math.abs(( int ) (x - mOldX));
                    int dy = Math.abs(( int ) (y - mOldY));
                    if (mTransition == null) {
                    	mTransition = new LayoutTransition();
					}
                    if (dx < 20 && !mTransition.isRunning()) {
                        toggleActionBar();
                        togglePreView();
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

    private void togglePreView() {
		resetTransition();
		
    	boolean show = mAuroraActionBar.getVisibility() == View.VISIBLE;
        if (!show) {
        	mPreView.setVisibility(View.VISIBLE);
        	ObjectAnimator oa = ObjectAnimator.ofFloat(mPreView, "alpha", 0f, 1f);
        	oa.setDuration(300);
        	mTransition.setAnimator(LayoutTransition.APPEARING, oa);
        } else {
        	mPreView.setVisibility(View.GONE);
        	ObjectAnimator oa = ObjectAnimator.ofFloat(mPreView, "alpha", 1f, 0f);
        	oa.setDuration(300);
        	mTransition.setAnimator(LayoutTransition.DISAPPEARING, oa);
        }
	}
    
    private void resetTransition() {
    	mTransition = new LayoutTransition();
		mContainer.setLayoutTransition(mTransition);
	}
    
    OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
        	pos = position;
        	mCurrentTheme = mThemeList.get(pos);
        	mDownloadManager.updateProgress(mCurrentTheme);
            //setAuroraTitle(position);
//            mPreviewAdapter.refreshData(position);
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    protected void onDestroy() {
        if (mPreviewAdapter != null) {
            mPreviewAdapter.clearData();
        }
        super.onDestroy();
        
        WallpaperDownloadService.unRegisterUpdateListener(updateListener);
        
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
        
        mDownloadManager.updateProgress(mCurrentTheme);
        
    }

	@Override
	public void showOperationUpdate(final DownloadData data,
			OnClickListener onClickListener) {
		// TODO Auto-generated method stub
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				WallpaperDownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		
		mApplyButton.setVisibility(View.GONE);
		// ProgressBtn
		mProgressBtn.setVisibility(View.VISIBLE);
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
				FileDownloader downloader = WallpaperDownloadService.getDownloaders(WallPaperPreviewActivity.this)
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
		Log.e("101010", "---showOperationApplied data.fileDir = ---" + data.fileDir);
		mCurrentTheme.fileDir = data.fileDir;
		mCurrentTheme.fileName = data.fileName;
		mApplyButton.setVisibility(View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
		//mProgressBtn.setOnFoucsClickListener(listener);
		mProgressBtn.setVisibility(View.GONE);
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
				WallpaperDownloadService.startDownload(mProgressBtn.getContext(),
						data);
			}
		};
		
		mApplyButton.setVisibility(View.GONE);
		// ProgressBtn
//		mApplyButton.setVisibility(!mFromLocal?View.GONE:View.VISIBLE);
		mProgressBtn.setVisibility(View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		mProgressBtn.showDownloadButtonWithoutText();
		mProgressBtn.setOnNormalClickListener(clickListener);
		final int downloadId = data.downloadId;
		mProgressBtn.setOnBeginAnimListener(new OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				FileDownloader downloader = WallpaperDownloadService.getDownloaders(WallPaperPreviewActivity.this)
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
		Log.e("101010", "---showWaitApply---");
		// TODO Auto-generated method stub
		mCurrentTheme.fileDir = data.fileDir;
		mCurrentTheme.fileName = data.fileName;
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {}
		};
		mApplyButton.setVisibility(View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
		//mProgressBtn.setOnFoucsClickListener(listener);
		mProgressBtn.setVisibility(View.GONE);
		
		Consts.isChangedByLocal = 2;
		Consts.isWallPaperChanged = true;
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
					WallpaperDownloadService.pauseOrContinueDownload(
							mProgressBtn.getContext(), downloader.getDownloadData());
				}
			}
		};
		
		mApplyButton.setVisibility(View.GONE);
		mProgressBtn.setVisibility(View.VISIBLE);
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
				WallpaperDownloadService.pauseOrContinueDownload(
						mProgressBtn.getContext(), downloader.getDownloadData());
			}
		};
		mApplyButton.setVisibility(View.GONE);
		// ProgressBtn
		mProgressBtn.setVisibility(View.VISIBLE);
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
		
		mApplyButton.setVisibility(View.GONE);
		// ProgressBtn
		mProgressBtn.setVisibility(View.VISIBLE);
		mProgressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
		mProgressBtn.setProgress(progress);
		mProgressBtn.setOnProgressClickListener(clickListener);
		mProgressBtn.setProgressBackground(R.drawable.aurora_progress_pause);
	}

	
}
