package com.aurora.change.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;

import com.aurora.change.adapters.WallpaperPreviewAdapter;
import com.aurora.change.data.WallpaperValue;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.view.CirclePageIndicator;

import com.aurora.thememanager.R;
import android.os.SystemProperties;

// Aurora liugj 2014-07-03 modified for preview wallpaper 
// Aurora liugj 2014-05-20 modified for bug-4788
public class DesktopWallpaperPreviewActivity extends AuroraActivity {
    private static final String TAG = "DesktopWallpaperPreviewActivity";
    
    private static final int ACTION_BTN_DONE = 1;
    
    private static CommonLog log = LogFactory.createLog(TAG);
    private ViewPager mViewPager;
    //private List<SoftReference<Bitmap>> mPicInfos;
    private List<String> mExtraList;
    private Context mContext;
    private Intent mIntent;
    private String mWallpaperType;
    private WallpaperPreviewAdapter mPreviewAdapter;
    //private CirclePageIndicator mIndicator;
//    private ActionBar mActionBar;
    private AuroraActionBar mAuroraActionBar;
    private ImageView mPreView;
    private RelativeLayout mContainer;
    private LayoutTransition mTransition;
    private int pos = 0;
    private float mOldX = 0;
    private float mOldY = 0;

    private static final String IMAGE_CACHE_DIR = "preview_thumbs";
    private ImageResizer mImageResizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.Theme_aurora);
    	
    	//requestWindowFeature must be called before super.onCreate, especially for Android5.0 and later
    	requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setupView();
        
        mIntent = getIntent();
        if (mIntent != null) {
            Bundle bundle = mIntent.getExtras();
            if (bundle == null) {
				return;
			}
            String updatePath = null;
            if (bundle.containsKey("updatepath")) {
    			updatePath =  getSDPath() + bundle.getString("updatepath");
    		}
            initImageCache(updatePath);
            
            pos = bundle.getInt("position");
            mExtraList = bundle.getStringArrayList("extra");
            if (updatePath != null) {
            	mImageResizer.removeImageCache(this, updatePath);
			}
            
            mWallpaperType = bundle.getString(Consts.WALLPAPER_TYPE_KEY, Consts.WALLPAPER_DESKTOP_TYPE);
            mPreviewAdapter = new WallpaperPreviewAdapter(getApplicationContext(), mWallpaperType, mExtraList);
            mPreviewAdapter.setImageResizer(mImageResizer);
            mViewPager.setAdapter(mPreviewAdapter);
            mViewPager.setOnTouchListener(mTouchListener);
            mPreviewAdapter.setViewPager(mViewPager);
            mViewPager.setCurrentItem(pos);
            mViewPager.setOnPageChangeListener(mPageChangeListener);
            //mIndicator.setViewPager(mViewPager);
            //mIndicator.setOnPageChangeListener(mPageChangeListener);
            //setAuroraTitle(0);
            mViewPager.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    private void setupView() {
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFormat(PixelFormat.RGBA_8888); //防止PNG渐变背景图片失真问题，是否有用?有待考证

        setAuroraPicContentView(R.layout.desktop_wallpaper_preview);
        mContainer = (RelativeLayout) findViewById(R.id.wallpaper_preview_container);
        mViewPager = (ViewPager) findViewById(R.id.wallpaper_preview_pager);
        mPreView = (ImageView) findViewById(R.id.desktop_wallpaper_preview);
        
        //shigq add start
        if ("true".equals(SystemProperties.get("phone.type.oversea"))) {
        	mPreView.setImageResource(R.drawable.desktop_wallpaper_pre_india);
        }
        //shigq add end
        
//        mViewPager.setOnTouchListener(mTouchListener);
        //mIndicator = ( CirclePageIndicator ) findViewById(R.id.wallpaper_preview_indicator);
        initAuroraActionBar();
    }
    
    private void initAuroraActionBar() {
    	mAuroraActionBar = getAuroraActionBar();
    	mAuroraActionBar.setDisplayHomeAsUpEnabled(true);
    	mAuroraActionBar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.actionbar_translucent));
    	mAuroraActionBar.getBackground().setAlpha(140);
    	mAuroraActionBar.setTitle(getString(R.string.set_wallpaper));
    	mAuroraActionBar.addItem(R.drawable.aurora_action_btn_done, ACTION_BTN_DONE, "action_done_btn");
    	mAuroraActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
	}
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_DONE:
				setWallpaper(pos);
				break;
			default:
				break;
			}
		}
	};
    
	private void setWallpaper(int position) {
		int len = Consts.LOCAL_WALLPAPERS.length;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(DesktopWallpaperPreviewActivity.this);
		try {
			InputStream is = null;
			if (position < len) {
				is = getStreamFromFile(Consts.DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH
						+ Consts.LOCAL_WALLPAPERS[position], true);
			} else {
				is = getStreamFromFile(mExtraList.get(position - len), false);
			}
			if (is == null || (is != null && is.available() == 0)) {
				showWallpaperSet(true);
				finish();
				if(is != null) {
					is.close();
				}
				is = null;
				return;
			}
			// Aurora liugj 2014-05-20 modified for bug-4621 start
			if (position == sp.getInt("selectpos", -1)) {
				showWallpaperSet(false);
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
			is.close();
			is = null;
			if (position < len) {
				is = getStreamFromFile(Consts.DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH
						+ Consts.LOCAL_WALLPAPERS[position], true);
			} else {
				is = getStreamFromFile(mExtraList.get(position - len), false);
			}
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
			
			Consts.isChangedByLocal = 1;
			SharedPreferences.Editor editor = sp.edit();
			if (position < len) {
				editor.putString("selectpath", "-1");
			}else {
				editor.putString("selectpath", mExtraList.get(position - len).replace(WallpaperValue.WALLPAPER_PATH, ""));
			}
			editor.putInt("selectpos", position);
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
	
	/*private Bitmap getBitmapFromFile(String data, boolean isLocal) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options(); 
		opts.inSampleSize = 1;
		if (isLocal) {
			try {
				AssetManager am = getResources().getAssets();
				InputStream is = am.open(data);
				bitmap = BitmapFactory.decodeStream(is, null, opts);
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			try {
				FileInputStream fis = new FileInputStream(new File(Environment.getExternalStorageDirectory(), data));
				bitmap = BitmapFactory.decodeStream(fis, null, opts);
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}*/
	
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
        /*boolean show = mActionBar.isShowing();
        if (!show) {
            mActionBar.show();
        } else {
            mActionBar.hide();
        }*/
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
        if (mExtraList != null) {
			mExtraList.clear();
			mExtraList = null;
		}
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
    }

    private void initImageCache(String updatePath) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

//        final int longest = (height > width ? height : width) / 2;
//        mImageResizer = new ImageResizer(mContext, longest);
        mImageResizer = new ImageResizer(mContext, width / 2, height / 2);
//        mImageResizer.setLoadingImage(R.drawable.preview_loading);
        mImageResizer.addImageCache(this, IMAGE_CACHE_DIR, updatePath);
    }

    private void setAuroraTitle(int index) {
        /*String title = (index + 1) + "";
        if (mPicInfos != null && mPicInfos.size() > 0) {
            title = (index + 1) + "/" + mPicInfos.size();
        }
        mAuroraActionBar.setTitle(title);*/
    }
    
    private String getSDPath(){ 
        File sdDir = null; 
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
        if(sdCardExist)
        {                               
          sdDir = Environment.getExternalStorageDirectory();//获取跟目录 
        }
        if (sdDir != null) {
        	return sdDir.toString();
		}
        return null;
    }
}
