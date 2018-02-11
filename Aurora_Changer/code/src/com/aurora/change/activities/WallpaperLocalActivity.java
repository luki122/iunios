package com.aurora.change.activities;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.R.integer;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;

import com.aurora.change.R;
import com.aurora.change.adapters.WallpaperLocalAdapter;
import com.aurora.change.adapters.WallpaperLocalAdapter.ListCheckStateChangeCallback;
import com.aurora.change.data.AdapterDataFactory;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.data.NextDayDbControl;
import com.aurora.change.data.NextDayDbHelper;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.NextDayDbInfoModel;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.CommonUtil;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.ImageLoaderHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.NextDayInitTask;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.change.AuroraChangeApp;

//Aurora liugj 2014-07-17 modified for文管提供图片选择接口
public class WallpaperLocalActivity extends AuroraActivity {

    private static final String TAG = "WallpaperLocalActivity";
    
	private static final String IMAGE_TYPE = "image/*";
	
	private static final int REQUEST_CODE_LOCKSCREEN_WALLPAPER = 1;
	
    private static CommonLog log = LogFactory.createLog(TAG);
//    private ListView mLocalWallPaper = null;
    private AuroraListView mLocalWallPaper = null;
    private String mWallpaperType = "";
    private Context mContext;
    private WallpaperLocalAdapter mAdapter;
//    private ActionBar mActionBar;
    private AuroraActionBar mAuroraActionBar;
    private AdapterDataFactory mDataFactory;
    private static final int LOCKPAPER_SET_SUCCESS = 0;
    private static final int LOCKPAPER_SET_FAILED = 1;

    //private static final String IMAGE_CACHE_DIR = "thumbs";
    private ImageResizer mImageResizer;
    private MyHandler mHandler;
    private static final int ITEM_ADD = 1;
    private static final int DIALOG_DEL = 1;
    private String mGroupName = "";
    
    //shigq add start
    private ArrayList<NextDayPictureInfo> mPictureList;
    private int displayWidth;
	private int displayHeight;
    //shigq add end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        initImageCache();
        try {
            mWallpaperType = getIntent().getExtras().getString(Consts.WALLPAPER_TYPE_KEY,
                    Consts.WALLPAPER_LOCKSCREEN_TYPE);
        } catch (Exception e) {
            mWallpaperType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
            e.printStackTrace();
        }
        mDataFactory = new AdapterDataFactory(mContext, mWallpaperType);
        mAdapter = mDataFactory.getLocalAdapter();
        mAdapter.setImageResizer(mImageResizer);
        mAdapter.setCheckStateChangeCallback(mStateChangeCallback);
        mLocalWallPaper.setAdapter(mAdapter);
        mLocalWallPaper.setOnItemClickListener(mItemClickListener);
        
        //shigq add start
        displayWidth = ((AuroraChangeApp) mContext.getApplicationContext()).getDisplayWidth();
        displayHeight = ((AuroraChangeApp) mContext.getApplicationContext()).getDisplayHeight();
        
        String nextDayPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay";
        if (FileHelper.fileIsExist(nextDayPath)) {
			((AuroraChangeApp) mContext.getApplicationContext()).getImageLoaderHelper().setHandle(mHandler);
			mPictureList = ((AuroraChangeApp) mContext.getApplicationContext()).getNextDayPictureInfoList();
			InitiatePictureList(mContext);
		}
        //shigq add end
        
    }

    private void setupViews() {
//        setContentView(R.layout.activity_wallpaper_local);
        setAuroraContentView(R.layout.activity_wallpaper_local, Type.Normal);
        mContext = this;
//        mLocalWallPaper = ( ListView ) findViewById(R.id.wallpaper_local_list);
        mLocalWallPaper = ( AuroraListView ) findViewById(R.id.wallpaper_local_list);
        mLocalWallPaper.setVerticalScrollBarEnabled(false);
//        mActionBar = getActionBar();
//        int options = ActionBar.DISPLAY_HOME_AS_UP ^ ActionBar.DISPLAY_SHOW_CUSTOM
//                ^ ActionBar.DISPLAY_SHOW_TITLE;
//        mActionBar.setDisplayOptions(options);
        mAuroraActionBar = getAuroraActionBar();
        addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ITEM_ADD);
        mAuroraActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
        mAuroraActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {

            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                finish();
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
            }
        });
        mAuroraActionBar.setTitle(R.string.lockscreen_wallpaper);
        mHandler = new MyHandler();
        
        mLocalWallPaper.auroraEnableSelector(false);
        mLocalWallPaper.auroraSetNeedSlideDelete(true);
        mLocalWallPaper.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
            
            @Override
            public void auroraPrepareDraged(int arg0) {
                
            }
            
            @Override
            public void auroraOnClick(int position) {
//                Toast.makeText(mContext, "onclick:position=" + position, Toast.LENGTH_SHORT).show();
                PictureGroupInfo groupInfo = ( PictureGroupInfo ) mAdapter.getItem(position);
                boolean delFlag = (mAdapter.getGroupState().get(String.valueOf(position)))
                        || (groupInfo.getSystem_flag() == 1);
                mGroupName = groupInfo.getDisplay_name();
                if (!delFlag) {
                    mLocalWallPaper.auroraSetRubbishBack();
                    showDialog(DIALOG_DEL);
                }
            }
            
            @Override
            public void auroraDragedUnSuccess(int arg0) {
                
            }
            
            @Override
            public void auroraDragedSuccess(int arg0) {
                
            }
        });
    }

    private void initImageCache() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

//        final int longest = (height > width ? height : width) / 2;
//        mImageResizer = new ImageResizer(mContext, longest);
        mImageResizer = new ImageResizer(mContext, width / 2, height / 3, true);
        mImageResizer.addImageCache(this, Consts.IMAGE_LOCKSCREEN_CACHE_DIR);
        mImageResizer.setLoadingImage(R.drawable.item_default_bg);
//        mImageResizer.setLoadingImage(R.drawable.preview_loading);
    }

    OnItemClickListener mItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	
        	//shigq add start
        	PictureGroupInfo groupInfo = (PictureGroupInfo) mAdapter.getItem(position);
        	if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(groupInfo.getDisplay_name())) {
        		if (CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.MOBILE_ONLY) {
	        		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
	            	builder.setTitle(getResources().getString(R.string.nextday_wallpaper_loading_dialog_title));
	            	builder.setMessage(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_message));
	            	builder.setNegativeButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_cancel), 
	            																			new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	((AuroraChangeApp) mContext.getApplicationContext()).setIsMobileData(false);
	                    	/*Intent intent = new Intent(mContext, NextDayPreviewActivity.class);
	        				startActivity(intent);*/
	                    }
	                });
	            	builder.setPositiveButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_confirm), 
	            																			new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	
	                    	String initime = CommonUtil.getCurrentTime();
	                		String initDate;
	                    	int initHour = Integer.valueOf(initime.substring(9, 11));
	                    	int initMinute = Integer.valueOf(initime.substring(12, 14));
	                    	
	                    	if (initHour < 9 || (initHour == 9 && initMinute < 30)) {
	                    		initDate = CommonUtil.getDateFromCurrent(-1);
	                		} else {
	                			initDate = CommonUtil.getDateFromCurrent(0);
	                		}
	                    	
	                    	NextDayInitTask mInitTask = new NextDayInitTask(mHandler);
	                    	mInitTask.execute(mContext, Consts.NEXTDAY_URL_INIT, WallpaperConfigUtil.getJsonDataForInit(), initDate, 
	                				String.valueOf(displayWidth) + "*" + String.valueOf(displayHeight), Consts.NEXTDAY_PICTURE_SIZE);
	                    	
	                    	((AuroraChangeApp) mContext.getApplicationContext()).setIsMobileData(true);
	                    	Intent intent = new Intent(mContext, NextDayPreviewActivity.class);
	        				startActivity(intent);
	                    }
	                });
	                builder.show();
	                
        		} else {
        			Intent intent = new Intent(mContext, NextDayPreviewActivity.class);
    				startActivity(intent);
        		}
				
			} else {
//				PictureGroupInfo groupInfo = ( PictureGroupInfo ) mAdapter.getItem(position);
	            boolean delFlag = (mAdapter.getGroupState().get(String.valueOf(position)))
	                    || (groupInfo.getSystem_flag() == 1);
	            List<PictureInfo> pictureInfos = mDataFactory.getItemsByGroup(groupInfo.getId());
	            Intent intent = new Intent(mContext, WallpaperPreviewActivity.class);
	            Bundle bundle = new Bundle();
	            bundle.putSerializable(Consts.WALLPAPER_PREVIEW_KEY, ( Serializable ) pictureInfos);
	            bundle.putString(Consts.WALLPAPER_TYPE_KEY, mWallpaperType);
	            bundle.putBoolean("delFlag", delFlag);
	            bundle.putString(Consts.LOCKSCREEN_WALLPAPER_GROUP_NAME_KEY, groupInfo.getDisplay_name());
	            intent.putExtras(bundle);
	            startActivity(intent);
			}
        }
    };

    private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
                case ITEM_ADD:
                    startFileManagerActivity();
                    break;
            }
        }
    };

    /*public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return true;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_add:
                startFileManagerActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    /*private void startFileManagerActivity() {
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent();
        try {
            intent = packageManager.getLaunchIntentForPackage("com.aurora.filemanager");
            Bundle bundle = new Bundle();
            bundle.putBoolean(Consts.FILEMANAGER_WALLPAPER, true);
            bundle.putBoolean("com.aurora.change", true);
            bundle.putString("file_type", "lockscreen");
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext, R.anim.activity_open_enter, R.anim.activity_open_exit);
//            startActivity(intent);
            startActivity(intent, opts.toBundle());
//            finish();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }*/
    
    private void startFileManagerActivity() {
    	try {
    		Intent intent = new Intent();
    		intent.setAction("com.aurora.filemanager.MORE_GET_CONTENT");
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.setType(IMAGE_TYPE);	
    		Bundle bundle = new Bundle();
    		bundle.putInt("size", 12);//要求获取图片数量
    		intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE_LOCKSCREEN_WALLPAPER);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
                && (requestCode == REQUEST_CODE_LOCKSCREEN_WALLPAPER)) {
			if (data != null && data.getExtras() != null) {
				Intent request = new Intent(WallpaperLocalActivity.this, WallpaperCropActivity.class);
				Bundle bundle = data.getExtras();
				ArrayList<String> imageList = bundle.getStringArrayList("image");
				bundle.putStringArrayList("images", imageList);
	            bundle.putString(Consts.LOCKSCREEN_WALLPAPER_CROP_TYPE, "mult");
                bundle.putBoolean(Consts.LOCKSCREEN_WALLPAPER_CROP_SOURCE, true);
                request.putExtras(bundle);
	            startActivity(request);
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
    
    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.clearCache();
        }
        if (mDataFactory != null) {
            mDataFactory.clearData();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.onPause();
        }
        if (mLocalWallPaper != null) {
            mLocalWallPaper.auroraOnPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
        	
        	//shigq add start
//        	mAdapter.onResume();
        	
        	new ToUpdateNextDayDB().execute(true);
        	//shigq add end
        	
        }
        if (mLocalWallPaper != null) {
            mLocalWallPaper.auroraOnResume();
        }
    }

    ListCheckStateChangeCallback mStateChangeCallback = new ListCheckStateChangeCallback() {

        @Override
        public void onCheckStateChange(String currentGroup, boolean isChecked) {
            if (isChecked) {
            	try {
            		clickApply(mContext, currentGroup);
				} catch (OutOfMemoryError e) {
					
				}
            }
            if (mLocalWallPaper != null) {
                mLocalWallPaper.auroraSetRubbishBack();
            }
        }
    };

    private void clickApply(final Context context, final String currentGroup) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                onClickApply(context, currentGroup);
            }
        }).start();
    }

    private void onClickApply(Context context, String currentGroup) {
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
//          bisCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
            isCopyRight = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
            
            /*if (isCopyRight) {
                mHandler.sendEmptyMessage(LOCKPAPER_SET_SUCCESS);
            }else {
            	mHandler.sendEmptyMessage(LOCKPAPER_SET_FAILED);
			}*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOCKPAPER_SET_SUCCESS:
                    Toast.makeText(mContext, R.string.lockpaper_set_success, Toast.LENGTH_SHORT).show();
                    break;
                    
                case LOCKPAPER_SET_FAILED:
                    Toast.makeText(mContext, R.string.set_wallpaper_failed, Toast.LENGTH_SHORT).show();
                    break;
                    
                //shigq add start
                case Consts.LOCKPAPER_INFO_LOAD_DONE:
                	String resolution = String.valueOf(((AuroraChangeApp) mContext.getApplicationContext()).getDisplayWidth()) + "*" + 
                						String.valueOf(((AuroraChangeApp) mContext.getApplicationContext()).getDisplayHeight());
                	
                	if (msg.obj instanceof JSONArray) {
                		boolean isFirst = DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_DB_FIRST_CREATE, true);
                    	if (isFirst) {
                    		DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_DB_FIRST_CREATE, false);
                    		Log.d("Wallpaper_DEBUG", "WallpaperLocalActivity-----------LOCKPAPER_INFO_LOAD_DONE------create NextDay DB = ");
                    		NextDayDbHelper mNextDayDbHelper = new NextDayDbHelper(mContext);
    					}
                    	
						JSONArray resultArray = (JSONArray) msg.obj;
						Log.d("Wallpaper_DEBUG", "WallpaperLocalActivity-----------LOCKPAPER_INFO_LOAD_DONE------JSONArray.length = "+resultArray.length());
						NextDayDbControl mNextDayDbControl = new NextDayDbControl(mContext);
						mNextDayDbControl.insertPictureInfoSafety(resultArray);
					}
                	
                	break;
                //shigq add end
                default:
                    break;
            }
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DEL:
                return new AuroraAlertDialog.Builder(mContext).setTitle(R.string.wallpaper_crop_del)
                        .setMessage(R.string.wallpaper_crop_del_msg)
                        .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setNeutralButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delWallpapers(mContext, mGroupName);
                                if (mAdapter != null) {
                                    mAdapter.onResume();
                                }
                            }
                        }).create();
        }
        return null;
    }

    private void delWallpapers(Context context, String groupName) {
        String path = Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH + groupName;
        FileHelper.deleteDirectory(path);
        DbControl dbControl = new DbControl(context);
        dbControl.delPictureGroupByName(groupName);
        dbControl.close();
        File file = this.getCacheDir();
        Log.d(TAG, "cacheFile=" + file);
        FileHelper.deleteDirectory(file.toString());
    }
    
    //shigq add start	
    public void InitiatePictureList(Context mContext) {
		String initime = CommonUtil.getCurrentTime();
		String initDate;
    	int initHour = Integer.valueOf(initime.substring(9, 11));
    	int initMinute = Integer.valueOf(initime.substring(12, 14));
    	
    	if (initHour < 9 || (initHour == 9 && initMinute < 30)) {
    		initDate = CommonUtil.getDateFromCurrent(-1);
		} else {
			initDate = CommonUtil.getDateFromCurrent(0);
		}
    	
    	if (CommonUtil.NetWorkType.WIFI == CommonUtil.getNetWorkType(mContext)) {
    		NextDayInitTask mInitTask = new NextDayInitTask(mHandler);
        	mInitTask.execute(mContext, Consts.NEXTDAY_URL_INIT, WallpaperConfigUtil.getJsonDataForInit(), initDate, 
    				String.valueOf(displayWidth) + "*" + String.valueOf(displayHeight), Consts.NEXTDAY_PICTURE_SIZE);
		}
    	
    	Log.d("Wallpaper_DEBUG", "----------------mPictureList.size() = "+mPictureList.size());
    	if (mPictureList.size() == 0) {
	    	for (int i = 0; i < Consts.NEXTDAY_PICTURE_SIZE; i++) {
				NextDayPictureInfo mPictureInfo = new NextDayPictureInfo();
				
				String mDate;
				String time = CommonUtil.getCurrentTime();
//		    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------time = "+time+" and the length = "+time.length());
		    	int currentHour = Integer.valueOf(time.substring(9, 11));
		    	int currentMinute = Integer.valueOf(time.substring(12, 14));
		    	
		    	if (currentHour < 9 || (currentHour == 9 && currentMinute < 30)) {
		    		mDate = CommonUtil.getDateFromCurrent(-i - 1);
				} else {
					mDate = CommonUtil.getDateFromCurrent(-i);
				}
		    	mPictureInfo.setPictureTime(mDate);
		    	
		    	String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + mDate + ".jpg";
		    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + ".jpg";
		    	String previewPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + "_comment" + ".jpg";
		    	
		    	File mFile = new File(originalPath);
		    	if (mFile.exists()) {
		    		mPictureInfo.setPictureOriginalUrl("file://" + originalPath);
				} else {
					mFile = new File(initPath);
					if (mFile.exists()) {
						mPictureInfo.setPictureOriginalUrl("file://" + initPath);
					}
				}
		    	
		    	mFile = new File(previewPath);
		    	if (mFile.exists()) {
					mPictureInfo.setPictureThumnailUrl("file://" + previewPath);
				}
		    	
				mPictureList.add(mPictureInfo);
			}
    	}
    }
    
    private class ToUpdateNextDayDB extends AsyncTask<Object, Object, Boolean> {
    	@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "WallpaperLocalActivity--------ToUpdateNextDayDB---------------doInBackground");
			WallpaperConfigUtil.updateNextDayGroupDB(mContext);
			return true;
		}
		
		protected void onPostExecute(Boolean result) {
     		Log.d("Wallpaper_DEBUG", "WallpaperLocalActivity--------ToUpdateNextDayDB---------------onPostExecute--------result = "+result);
     		if (mAdapter != null) {
//				mAdapter.notifyDataSetChanged();
     			mAdapter.onResume();
			}
     	}
    }
    //shigq add end
    
}
