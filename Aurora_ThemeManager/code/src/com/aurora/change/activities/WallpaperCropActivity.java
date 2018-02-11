package com.aurora.change.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraAnimationImageView;

import com.aurora.thememanager.R;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.change.adapters.WallpaperCropAdapter;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.imagecache.ImageWorker.ImageLoaderCallback;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.receiver.ChangeReceiver;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.change.view.CropImageView;
import com.aurora.change.view.CropViewPager;
//import com.aurora.filemanager.FileInfo;

//Aurora liugj 2014-07-17 modified for文管提供图片选择接口
public class WallpaperCropActivity extends AuroraActivity implements OnClickListener {

    private static final String TAG = "WallpaperCropActivity";
    private static final String NAVI_KEY_HIDE = "navigation_key_hide"; // Settings.System 对应的键值
    private static CommonLog log = LogFactory.createLog(TAG);
    //private List<FileInfo> mFileInfos;
    //private Intent mIntent;
//    private ViewPager mPager;
    private CropViewPager mPager;
//    private ImageView mPreviousBtn;
//    private ImageView mNextBtn;
    private AuroraAnimationImageView mPreviousBtn;
    private AuroraAnimationImageView mNextBtn;
    private LinearLayout mBottomBar;
    private Context mContext;
    private WallpaperCropAdapter mCropAdapter;
//    private ActionBar mActionBar;
    private AuroraActionBar mAuroraActionBar;
    private ImageResizer mImageResizer;
    private static final int ITEM_OK = 0;

    private static final String IMAGE_CACHE_DIR = "crop_thumbs";
    private String mGroupName = "";
    private String mCropType = "";
    //private boolean mIsFromSource = false;
    private static final int DIALOG_CROP = 1;
    private static final int DIALOG_CANCEL = 2;
    private boolean mCancelFlag = false;
    private boolean mIsSaveFlag = false;
    private boolean mIsCropFlag = false;
    private SdMountRecevier mSdMountRecevier;
    private List<String> mImageList = null;
    private ThemeManager mThemeManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(com.aurora.R.style.Theme_aurora);
    	
    	//requestWindowFeature must be called before super.onCreate, especially for Android5.0 and later
    	requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setupViews();
        mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_TIMES);
        initImageCache();
        Intent mIntent = getIntent();
        if (mIntent != null) {
            mGroupName = getGroupName();
            Bundle bundle = mIntent.getExtras();
            if (bundle != null) {
                mCropType = bundle.getString(Consts.LOCKSCREEN_WALLPAPER_CROP_TYPE, "single");
                //mIsFromSource = bundle.getBoolean(Consts.LOCKSCREEN_WALLPAPER_CROP_SOURCE, false);
                if ("single".equals(mCropType)) {
                	mImageList = new ArrayList<String>();
                    Uri uri = mIntent.getData();
                    if (uri != null) {
                    	mImageList.add(uriToPath(uri));
                    	
    				}
                }else {
                	mImageList = bundle.getStringArrayList("images");
				}
            } else if (mIntent.getData() != null && mIntent.getData().getScheme().equals("file")) {
                mCropType = "single";
                //mIsFromSource = false;
                mImageList = new ArrayList<String>();
                Uri uri = mIntent.getData();
                if (uri != null) {
                	mImageList.add(uri.getEncodedPath());
				}
                Log.d("liugj", TAG+" intent getData : "+uri);
            }
//            mGroupName = bundle.getString(Consts.LOCKSCREEN_WALLPAPER_GROUP_NAME_KEY);
            mCropAdapter = new WallpaperCropAdapter(this, mImageList);
            mPager.setAdapter(mCropAdapter);
            //mCropAdapter.setViewPager(mPager);
            mCropAdapter.setImageResizer(mImageResizer);
            Log.d("crop",
                    "GroupName=" + mGroupName + ",mImageList=" + mImageList.size() + ":"
                            + mImageList.toString());
            if (mImageList != null && mImageList.size() > 0) {
                if (mImageList.size() == 1) {
                    mBottomBar.setVisibility(View.GONE);
                    mAuroraActionBar.getItem(ITEM_OK).getItemView().setVisibility(View.VISIBLE);
                    mAuroraActionBar.getItem(ITEM_OK).getItemView().setEnabled(false);
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAuroraActionBar.getItem(ITEM_OK).getItemView().setEnabled(true);
                        }
                    }, 1000);*/
                } else {
                	mPreviousBtn = ( AuroraAnimationImageView ) findViewById(R.id.wallpaper_crop_previous);
                    mNextBtn = ( AuroraAnimationImageView ) findViewById(R.id.wallpaper_crop_next);
                    mPreviousBtn.setEnabled(false);
                    mNextBtn.setEnabled(false);
                    mPreviousBtn.setOnClickListener(this);
                    mNextBtn.setOnClickListener(this);
                    mBottomBar.setVisibility(View.VISIBLE);
                    mAuroraActionBar.getItem(ITEM_OK).getItemView().setVisibility(View.GONE);
                }
            }
            if (mSdMountRecevier == null) {
                registSdMountRecevier();
            }
        }
    }

    private void setupViews() {
        mContext = this;  
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_wallpaper_crop);
        setAuroraPicContentView(R.layout.activity_wallpaper_crop);
        int hide = Settings.System.getInt(getContentResolver(), NAVI_KEY_HIDE, 1);
        if (hide == 0) {
        	hideNaviBar(true);
		}
        mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setDisplayHomeAsUpEnabled(true);
		mAuroraActionBar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.actionbar_translucent));
        mAuroraActionBar.getBackground().setAlpha(140);
        mAuroraActionBar.setTitle(R.string.wallpaper_crop_title);
        mAuroraActionBar.addItem(R.drawable.aurora_action_btn_done, ITEM_OK,
                getString(R.string.wallpaper_crop_ok));
				
        mPager = ( CropViewPager ) findViewById(R.id.wallpaper_crop_pager);		
        mPager.setOnPageChangeListener(mPageChangeListener);
        mBottomBar = ( LinearLayout ) findViewById(R.id.wallpaper_crop_bottom_bar);
        
        mAuroraActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
        mAuroraActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {

            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                if (mCancelFlag) {
                    showDialog(DIALOG_CANCEL);
                } else {
                    finish();
                }
            }
        });
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNextBtn.setEnabled(true);
            }
        }, 10000);*/
        // 用于actionbar显示和隐藏
        /*mPager.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d("count", "mpager.onsystemUIChange");
                if ((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                    Log.d("count", "mpager.onsystemUIChange hide");
        //                    mActionBar.hide();
                    toggleActionBar(false);
                } else {
                    Log.d("count", "mpager.onsystemUIChange show");
        //                    mActionBar.show();
                    toggleActionBar(true);
                }
            }
        });
        mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        //        mActionBar.hide();
        toggleActionBar(false);*/
    }

    
    /**
     * 通过改写 NAVI_KEY_HIDE 的值，可以控制虚拟键的显示/隐藏。
     * hide = true, 写入1，代表隐藏虚拟键
     * hide = false, 写入0，代表显示虚拟键
     */
    private void hideNaviBar(boolean hide) {
        ContentValues values = new ContentValues();
        values.put("name", NAVI_KEY_HIDE);
        values.put("value", (hide ? 1 : 0));
        getContentResolver().insert(Settings.System.CONTENT_URI, values);
    }

//    public void toggleActionBar(boolean eable) {
//        boolean show = mAuroraActionBar.getVisibility() == View.VISIBLE;
//        if (!show) {
//            mAuroraActionBar.setVisibility(View.VISIBLE);
//        } else {
//            mAuroraActionBar.setVisibility(View.GONE);
//        }
//        if (eable) {
//            mAuroraActionBar.setVisibility(View.VISIBLE);
//        } else {
//            mAuroraActionBar.setVisibility(View.GONE);
//        }
//    }

	 //Aurora liugj 2014-08-14 modified for bug-7650 start
    @Override
    public void onClick(View v) {
    	Log.d(TAG, "mPager.getCurrentItem()=" + mPager.getCurrentItem());
        View view = getViewByPosition(mPager.getCurrentItem());
        CropImageView cropImageView = ( CropImageView ) view
                .findViewById(R.id.wallpaper_crop_item);
        
        switch (v.getId()) {
            case R.id.wallpaper_crop_previous:
            	if (cropImageView.mIsSaveEnable) {
            		if (mPager.getCurrentItem() == 1) {
            			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
					}else {
						showProgress(true, mPager.getCurrentItem());
	            		mPreviousBtn.setEnabled(false);
	                    mNextBtn.setEnabled(false);
	            		new Handler().postDelayed(new Runnable() {
	                        @Override
	                        public void run() {
	                        	showProgress(false, mPager.getCurrentItem());
	                            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
	                            mPreviousBtn.setEnabled(true);
	                            mNextBtn.setEnabled(true);
	                        }
	                    }, 600);
					}
				}
                break;

            case R.id.wallpaper_crop_next:
//                showDialog(DIALOG_CROP);
//                mNextBtn.setEnabled(false);
                
                if (view != null) {
                    if (cropImageView.mIsSaveEnable) {
                    	String fileName = "";
                        if (mPager.getCurrentItem() < 9) {
                            fileName = "data0" + (mPager.getCurrentItem() + 1);
                        } else {
                            fileName = "data" + (mPager.getCurrentItem() + 1);
                        }
                        Log.d(TAG, "fileName=" + fileName);
                        showProgress(true, mPager.getCurrentItem());
                        mIsCropFlag = true;
                        new SaveTask().execute(fileName);
                        mCancelFlag = true;
                        mPreviousBtn.setEnabled(false);
                        mNextBtn.setEnabled(false);
                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false, mPager.getCurrentItem());
                                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                                mPreviousBtn.setEnabled(true);
                                mNextBtn.setEnabled(true);
                            }
                        }, 1500);*/
                    }
                }
                break;

            default:
                break;
        }
    }
	 //Aurora liugj 2014-08-14 modified for bug-7650 end

    OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (mImageList != null && mImageList.size() > 0) {
                mNextBtn.setImageResource(R.drawable.wallpaper_crop_next);
                if (position == 0) {
                    mPreviousBtn.setEnabled(false);
                } else if (position == (mImageList.size() - 1)) {
                    mNextBtn.setImageResource(R.drawable.wallpaper_crop_ok);
                    mPreviousBtn.setEnabled(true);
                } else {
                    mPreviousBtn.setEnabled(true);
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void initImageCache() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
//        final int longest = height > width ? height : width;
        mImageResizer = new ImageResizer(mContext, width, height);
//        mImageResizer = new ImageResizer(mContext, longest);
//        mImageResizer.setLoadingImage(R.drawable.preview_loading);
        mImageResizer.addImageCache(this, IMAGE_CACHE_DIR);
        mImageResizer.setImageLoaderCallback(mCallback);
    }

    private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
                case OnAuroraActionBarItemClickListener.HOME_ITEM:
                    showDialog(DIALOG_CANCEL);
                    return;
                case ITEM_OK:
//                actionbarSave();
//                    showDialog(DIALOG_CROP);
                    View view = getViewByPosition(mPager.getCurrentItem());
                    if (view != null) {
                        CropImageView cropImageView = ( CropImageView ) view
                                .findViewById(R.id.wallpaper_crop_item);
                        if (cropImageView.mIsSaveEnable) {
                            showProgress(true, mPager.getCurrentItem());
                            mIsCropFlag = true;
                            new SaveTask().execute("data01");
                        }
                    }
                    break;
            }
        }
    };

    private void actionbarSave() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveImageFile("data1");
                finish();
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCancelFlag) {
                showDialog(DIALOG_CANCEL);
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mCropAdapter != null) {
            mCropAdapter.onResume();
        }
		int hide = Settings.System.getInt(getContentResolver(), NAVI_KEY_HIDE, 1);
		if(hide == 0) {
			hideNaviBar(true);
		}
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mCropAdapter != null) {
            mCropAdapter.onPause();
        }
		int hide = Settings.System.getInt(getContentResolver(), NAVI_KEY_HIDE, 0);
		if(hide == 1) {
			hideNaviBar(false);
		}
    }

    @Override
    protected void onDestroy() {
        if (mCropAdapter != null) {
            mCropAdapter.clearData();
        }
        if (mImageList != null) {
			mImageList.clear();
			mImageList = null;
		}
        if (mSdMountRecevier != null) {
            unRegistSdMountRecevier();
        }
        if (!mIsSaveFlag) {
            int fileCount = 0;
            int groupCount = -1;
            if (null == mGroupName || (null != mGroupName && mGroupName.equals(""))) {
                super.onDestroy();
                return;
            }
            try {
                DbControl dbControl = new DbControl(mContext);
                PictureGroupInfo groupInfo = dbControl.queryGroupByName(mGroupName);
                dbControl.close();
                File path = new File(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH + mGroupName);
                if (path.exists() && path.isDirectory()) {
                    fileCount = path.listFiles().length;
                }
                if (null != groupInfo) {
                    groupCount = groupInfo.getCount();
                }
                if (fileCount != groupCount) {
                    Log.d(TAG, "isSaveFlag=" + mIsSaveFlag + ",groupName=" + mGroupName);
                    delWallpapers(mContext, mGroupName);
                }
            } catch (Exception e) {

            }
        }
        super.onDestroy();
    }
    
    //M:shigq fix bug15327 start
    //It may be no safe, if this problem happens again, please check here
    public int getCurrentItem() {
    	return mPager.getCurrentItem();
    }
    //M:shigq fix bug15327 end

    class SaveTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean bool = saveImageFile(params[0]);
            return bool;
        }

        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "onPostExecute=" + result);
            if (mImageList != null && (mImageList.size() < 2 || mPager.getCurrentItem() == (mImageList.size() - 1))) {
                DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mGroupName);
                
                //shigq add start
                DbControl mDbControl = new DbControl(mContext);
                PictureGroupInfo groupInfo = mDbControl.queryGroupByName(mGroupName);
                if (groupInfo.getIsTimeBlack() == 0) {
                	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
    			} else {
    				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
    			}
                if (groupInfo.getIsStatusBarBlack() == 0) {
                	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
    			} else {
    				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
    			}
                mDbControl.close();
                //shigq add end
                
                String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, mGroupName);
                
                //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//              boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
                boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
                //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
              
//                dismissDialog(DIALOG_CROP);
                Log.d(ChangeReceiver.LOCK_TAG, "SaveTask: FileHelper.copyFile = " + res);
                Toast.makeText(mContext, R.string.lockpaper_set_success, Toast.LENGTH_SHORT).show();
                mIsSaveFlag = true;
                finish();
            } else {
//                dismissDialog(DIALOG_CROP);
                Log.d(TAG, (mImageList == null) +" = onPostExecute: next = " + mPager.getCurrentItem());
//                showProgress(false, mPager.getCurrentItem());
//                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
//            mNextBtn.setEnabled(result);
            showProgress(false, mPager.getCurrentItem());
            if (mCancelFlag) {
            	mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                mPreviousBtn.setEnabled(true);
                mNextBtn.setEnabled(true);
			}
            mIsCropFlag = false;
        }
    }

	private boolean saveImageFile(String fileName) {
		boolean bool = false;
		Log.d(TAG, "saveImageFile=" + fileName);
		View view = getViewByPosition(mPager.getCurrentItem());
		if (view != null) {
			CropImageView cropImageView = (CropImageView) view
					.findViewById(R.id.wallpaper_crop_item);
			Bitmap bitmap = cropImageView.getCropImage();
			if (bitmap == null) {
				return bool;
			}
			DbControl control = new DbControl(mContext);
			// Aurora liugj 2014-09-23 modified for bug-8003 start
			StringBuffer path = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
			path.append(mGroupName).append("/").append(fileName).append(".png");
			bool = FileHelper.writeImage(bitmap, path.toString(), 100);
			
			//shigq add start
			StringBuffer filePath = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
			filePath.append(mGroupName).append("/").append(Consts.LOCKPAPER_SET_FILE);
			
			ThemeInfo mThemeInfo = new ThemeInfo();
			mThemeInfo.name = mGroupName;
			String defaultGroup = Consts.DEFAULT_LOCKPAPER_GROUP;
			Log.d(TAG, "saveImageFile==============defaultGroup = "+defaultGroup);
            //modify by tangjie for change default color to white
//			if (defaultGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || defaultGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
//	    		mThemeInfo.timeBlack = "true";
//			}
            mThemeInfo.timeBlack = "false";
            //modify by tangjie end
			String fileString = WallpaperConfigUtil.creatWallpaperConfigurationXmlFile(filePath.toString(), mThemeInfo);
			Log.d(TAG, "saveImageFile==============creatWallpaperConfigurationXmlFile========fileString = "+fileString);
			//shigq add end
			
			// Aurora liugj 2014-08-04 modified for bug-7194 start
			if (mImageList != null) {
				//shigq add start
//				updatePictrueGroupDatabase(control, mGroupName, mImageList.size()); // NullPointerException
				updatePictrueGroupDatabase(control, mThemeInfo, mImageList.size());
				//shigq add end
				
				updatePictrueDatabase(control, mGroupName, fileName, path.toString());
				
				//shigq add start
//				control.refreshDb();
				//shigq add end
				// Aurora liugj 2014-09-23 modified for bug-8003 end
			}
			// Aurora liugj 2014-08-04 modified for bug-7194 end
			control.close();
			// Aurora liugj 2014-07-17 modified for bug-5471 start
			if (bitmap != null) {
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
				}
				bitmap = null;
			}
			// Aurora liugj 2014-07-17 modified for bug-5471 end
		}
		return bool;
	}

    
    //shigq add start
    private void updatePictrueGroupDatabase(DbControl control, ThemeInfo mThemeInfo, int group_count) {
        PictureGroupInfo groupInfo = new PictureGroupInfo();
        groupInfo.setDisplay_name(mThemeInfo.name);
        groupInfo.setThemeColor(mThemeInfo.nameColor);
        groupInfo.setIsDefaultTheme("false".equals(mThemeInfo.isDefault)? 0 : 1);
        groupInfo.setIsTimeBlack("false".equals(mThemeInfo.timeBlack)? 0 : 1);
        groupInfo.setIsStatusBarBlack("false".equals(mThemeInfo.statusBarBlack)? 0 : 1);
        groupInfo.setCount(group_count);
        control.insertPictureGroup(groupInfo, false);
        mThemeManager.setTimeWallpaperUnApplied(this);
    }
    //shigq add end

    private void updatePictrueDatabase(DbControl control, String group_name, String pictureTitle, String path) {
        PictureGroupInfo belong_group = control.queryGroupByName(group_name);
        int belong_id = belong_group.getId();
        PictureInfo pictureInfo = new PictureInfo();
        pictureInfo.setBelongGroup(belong_id);
        pictureInfo.setIdentify(pictureTitle);
        pictureInfo.setBigIcon(path);
        control.insertPicture(pictureInfo);
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
    }

    @Override
    protected Dialog onCreateDialog(int id) {
//        View view = View.inflate(mContext, R.layout.progress_dialog, null);
        switch (id) {
            case DIALOG_CROP:
//                return new AuroraAlertDialog.Builder(mContext).setCancelable(false)
//                        .setTitle(R.string.wallpaper_crop_cancel_title).setView(view).create();
                AuroraProgressDialog dialog = new AuroraProgressDialog(mContext);
                dialog.setTitle("");
                dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
                dialog.setMessage(getString(R.string.setting_wallpaper));
                return dialog;

            case DIALOG_CANCEL:
            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
            	builder.setTitle(R.string.wallpaper_crop_cancel_title);
            	builder.setMessage(R.string.wallpaper_crop_cancel_msg);
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
            default:
                break;
        }
        return super.onCreateDialog(id);
    }

    ImageLoaderCallback mCallback = new ImageLoaderCallback() {

        @Override
        public void onImageLoad(boolean success, int position) {
            Log.d(TAG, "callBack=" + success);
            showProgress(false, position);
            /*if (!success) {
                mImageResizer.setPauseWork(false);
                mCropAdapter.refreshData(position);
                mImageResizer.setPauseWork(true);
            }else {*/
				if (mImageList != null) {
					if (mImageList.size() == 1) {
						mAuroraActionBar.getItem(ITEM_OK).getItemView().setEnabled(true);
					}else if (mImageList.size() > 1) {
						mNextBtn.setEnabled(true);
					} 
				}
			//}
        }

        @Override
        public void onImageLoadFailed(int position) {
            Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
            if (mCancelFlag) {
                delWallpapers(mContext, mGroupName);
            }
            finish();
        };
    };

    private View getViewByPosition(int position) {
        View view = null;
        List<View> views = mCropAdapter.getItemViews();
        if (views != null && views.size() > 0) {
            view = views.get(position);
        }
        return view;
    }

    private void showProgress(boolean show, int position) {
        View view = getViewByPosition(position);
        if (show) {
            if (view != null) {
                view.findViewById(R.id.wallpaper_crop_pb).setVisibility(View.VISIBLE);
            }
        } else {
            if (view != null) {
                view.findViewById(R.id.wallpaper_crop_pb).setVisibility(View.INVISIBLE);
            }
        }
    }

	 // Aurora liugj 2014-09-23 modified for bug-8003 start
    private String getGroupName() {
        DbControl control = new DbControl(mContext);
        List<PictureGroupInfo> groupInfos = control.queryAllGroupInfos();
        StringBuffer group_name = new StringBuffer(Consts.DEFAULT_LOCKPAPER_FILE_NAME)/*getString(R.string.wallpaper_crop_custom_name)*/;
        int id = 1;
		// Aurora liugj 2014-05-15 modified for 4.4 crash because ArrayOutOfBoundException start
        if (groupInfos != null && groupInfos.size() != 0) {
		// Aurora liugj 2014-05-15 modified for 4.4 crash because ArrayOutOfBoundException end
        	
        	//shigq add start
//            id = groupInfos.get(groupInfos.size() - 1).getId() + 1;
            String groupName = groupInfos.get(groupInfos.size() - 1).getDisplay_name();
            if (groupName.contains(Consts.DEFAULT_LOCKPAPER_FILE_NAME)) {
				int currentNumber = Integer.valueOf(groupName.replace(Consts.DEFAULT_LOCKPAPER_FILE_NAME, ""));
				id = currentNumber + 1;
				
			} else {
				id = groupInfos.get(groupInfos.size() - 1).getId() + 1;
			}
            //shigq add end
            
        }
        if (id < 10) {
            group_name.append("0").append(id);
        } else {
        	group_name.append(id);
        }
        control.close();
        return group_name.toString();
    }
	 // Aurora liugj 2014-09-23 modified for bug-8003 end

    private String uriToPath(Uri uri) {
        String path = uri.getPath();
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            // 第一行第二列保存路径strRingPath
            cursor.moveToFirst();
            path = cursor.getString(1);
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void registSdMountRecevier() {
        mSdMountRecevier = new SdMountRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mSdMountRecevier, filter);
    }

    private void unRegistSdMountRecevier() {
        unregisterReceiver(mSdMountRecevier);
        mSdMountRecevier = null;
    }

    class SdMountRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            if (Intent.ACTION_MEDIA_SHARED.equals(action) || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)
                    || Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_REMOVED.equals(action)
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                Toast.makeText(mContext, getString(R.string.wallpaper_crop_sdcard_error), Toast.LENGTH_SHORT)
                        .show();
                mIsSaveFlag = false;
                WallpaperCropActivity.this.finish();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsCropFlag || ((mNextBtn != null) && !mNextBtn.isEnabled())) {
            return true;
        } else {
        	try {
        		return super.dispatchTouchEvent(ev);
			} catch (Exception e) {
				return true;
			}
        }
    }
}
