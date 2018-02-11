package com.aurora.change.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aurora.thememanager.R;
import com.aurora.change.activities.WallpaperCropActivity.SaveTask;
import com.aurora.change.adapters.WallpaperPreviewAdapter;
import com.aurora.change.data.DbControl;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.imagecache.ImageWorker;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.view.CirclePageIndicator;
import com.aurora.change.view.PreviewLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.Type;

public class WallpaperPreviewActivity extends AuroraActivity {
    private static final String TAG = "WallpaperPreviewActivity";
    private static CommonLog log = LogFactory.createLog(TAG);
    private ViewPager mViewPager;
    private List<PictureInfo> mDisplayPictureInfos;
    private List<PictureInfo> mPictureInfos;
    private Context mContext;
    private Intent mIntent;
    private String mWallpaperType;
    private WallpaperPreviewAdapter mPreviewAdapter;
    private CirclePageIndicator mIndicator;
//    private ActionBar mActionBar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//setTheme(R.style.Theme_aurora);
    	
    	//requestWindowFeature must be called before super.onCreate, especially for Android5.0 and later
    	requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setupView();
        initImageCache();
        mIntent = getIntent();
        if (mIntent != null) {
            Bundle bundle = mIntent.getExtras();
            mWallpaperType = bundle.getString(Consts.WALLPAPER_TYPE_KEY, Consts.WALLPAPER_LOCKSCREEN_TYPE);
            mPictureInfos = ( List<PictureInfo> ) bundle.getSerializable(Consts.WALLPAPER_PREVIEW_KEY);
            mDisplayPictureInfos = getAllPicture(mPictureInfos);
            mDelFlag = bundle.getBoolean("delFlag");
            mGroupName = bundle.getString(Consts.LOCKSCREEN_WALLPAPER_GROUP_NAME_KEY);
            mPreviewAdapter = new WallpaperPreviewAdapter(getApplicationContext(), mDisplayPictureInfos,
                    mWallpaperType);
            mPreviewAdapter.setImageResizer(mImageResizer);
            mViewPager.setAdapter(mPreviewAdapter);
            mViewPager.setOnTouchListener(mTouchListener);
            mPreviewAdapter.setViewPager(mViewPager);
            mViewPager.setCurrentItem(0);
//            mIndicator.setViewPager(mViewPager);
//            mIndicator.setOnPageChangeListener(mPageChangeListener);
            
            //shigq add start
            /*if (mGroupName.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || mGroupName.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
				mPreviewLayout.setBlackStyle(true, Color.BLACK);
			} else {
				mPreviewLayout.setBlackStyle(false, Color.WHITE);
			}*/
            
            boolean timeIsBlack = false;
        	DbControl mDbControl = new DbControl(mContext);
        	PictureGroupInfo groupInfo = mDbControl.queryGroupByName(mGroupName);

        	if (groupInfo != null) {
    			timeIsBlack = (groupInfo.getIsTimeBlack() == 0)? false : true;
    			Log.d("Wallpaper_DEBUG", "WallpaperPreviewActivity-------onCreate------mGroupName = "+mGroupName+" timeIsBlack = "+timeIsBlack);
    			
    			if (timeIsBlack) {
    				mPreviewLayout.setBlackStyle(true, Color.BLACK);
				} else {
					mPreviewLayout.setBlackStyle(false, Color.WHITE);
				}
    			
    		} else {
    			if (mGroupName.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || mGroupName.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
    	    		Log.d("Wallpaper_DEBUG", "WallpaperPreviewActivity-------onCreate----Color.BLACK ");
    	    		mPreviewLayout.setBlackStyle(true, Color.BLACK);
    			} else {
    				Log.d("Wallpaper_DEBUG", "WallpaperPreviewActivity-------onCreate----Color.WHITE ");
    				mPreviewLayout.setBlackStyle(false, Color.WHITE);
    			}
    		}
        	mDbControl.close();
            //shigq add end
            
            mPreviewLayout.setViewPager(mViewPager);
            mPreviewLayout.setOnPageChangeListener(mPageChangeListener);
            if (mDisplayPictureInfos.size() == 1) {
                mPreviewLayout.setSingle(true);
            } else {
                mPreviewLayout.setSingle(false);
            }
            setAuroraTitle(0);
            mViewPager.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (mDelFlag) {
                mAuroraActionBar.getItem(ITEM_DEL).getItemView().setVisibility(View.GONE);
                mAuroraActionBar.setVisibility(View.GONE);
            } else {
                mAuroraActionBar.getItem(ITEM_DEL).getItemView().setVisibility(View.VISIBLE);
                mAuroraActionBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupView() {
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow()
//                .setFlags(
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN
//                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_wallpaper_preview);
//        setAuroraContentView(R.layout.activity_wallpaper_preview, Type.Normal);
        setAuroraPicContentView(R.layout.activity_wallpaper_preview);
        mViewPager = ( ViewPager ) findViewById(R.id.wallpaper_preview_pager);
//        mViewPager.setOnTouchListener(mTouchListener);
        mIndicator = ( CirclePageIndicator ) findViewById(R.id.wallpaper_preview_indicator);
        /*mActionBar = getActionBar();
        int options = ActionBar.DISPLAY_HOME_AS_UP ^ ActionBar.DISPLAY_SHOW_CUSTOM
                ^ ActionBar.DISPLAY_SHOW_TITLE;
        mActionBar.setDisplayOptions(options);
        mActionBar.hide();*/
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.actionbar_translucent));
        mAuroraActionBar.getBackground().setAlpha(140);
        mAuroraActionBar.addItem(R.drawable.wallpaper_crop_actionbar_del, ITEM_DEL,
                getString(R.string.wallpaper_crop_del));
        mAuroraActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
        mPreviewLayout = ( PreviewLayout ) findViewById(R.id.preview_layout);

    }

    private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
                case ITEM_DEL:
//                actionbarSave();
                    showDialog(DIALOG_DEL);
                    break;
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
                    int dx = Math.abs(( int ) (x - mOldX));
                    int dy = Math.abs(( int ) (y - mOldY));
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

    OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            setAuroraTitle(position);
//            mPreviewAdapter.refreshData(position);
				// Aurora liugj 2014-07-17 modified for bug-6478 start
            /*if (!mDelFlag) {
                mAuroraActionBar.setVisibility(View.GONE);
            }*/
				// Aurora liugj 2014-07-17 modified for bug-6478 end
            mPreviewLayout.onPageSelected(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        	if (!mPreviewLayout.getSingle()) {
        		mPreviewLayout.onPageScrolled(position, positionOffset, positionOffsetPixels);
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
    }

    private void initImageCache() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

//        final int longest = (height > width ? height : width) / 2;
//        mImageResizer = new ImageResizer(mContext, longest);
        mImageResizer = new ImageResizer(mContext, width /*/ 2*/, height /*/ 2*/);
//        mImageResizer.setLoadingImage(R.drawable.preview_loading);
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
            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
            	builder.setTitle(R.string.wallpaper_crop_del);
            	builder.setMessage(R.string.wallpaper_crop_del_msg);
            	builder.setCancelable(true);
            	builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
        String path = Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH + groupName;
        FileHelper.deleteDirectory(path);
        DbControl dbControl = new DbControl(context);
        PictureGroupInfo group = dbControl.queryGroupByName(groupName);
        if(group != null){
      	  String downloadFilePath = group.downloadPkgPath;
      	  if(!TextUtils.isEmpty(downloadFilePath)){
      		  File file = new File(downloadFilePath);
      		  if(file.exists()){
      			  file.delete();
      		  }
      	  }
        }
        dbControl.delPictureGroupByName(groupName);
        dbControl.close();
		 //Aurora liugj 2014-07-17 modified for bug-7211 start
        /*File file = this.getCacheDir();
        Log.d(TAG, "cacheFile=" + file);
        FileHelper.deleteDirectory(file.toString());*/
        //mImageResizer.clearMemoryCache();
		 //Aurora liugj 2014-07-17 modified for bug-7211 end
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
}
