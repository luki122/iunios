package com.aurora.change.activities;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraAlertDialog;

import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DatabaseController;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.download.WallpaperDownloadService;
import com.aurora.change.adapters.AuroraActionBatchHandler;
import com.aurora.change.adapters.SelectionManager;
import com.aurora.change.adapters.WallpaperLocalGridAdapter;
import com.aurora.change.data.WallpaperValue;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.Consts;

// Aurora liugj 2014-05-20 modified for bug-4664
//Aurora liugj 2014-07-17 modified for文管提供图片选择接口
// Aurora liugj 2014-07-30 added for wallpaper pic resources divide
public class DesktopWallpaperLocalActivity extends AuroraActivity{
	
	private static final String DEFAULT_SORT_ORDER = "modified DESC";
	
	private static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
	private static final String IMAGE_TYPE = "image/*";
	
	private static final int ACTION_BTN_NEW = 1;
	private static final int MSG_DELETE_COMPLETED = 3;
	private static final int MSG_DELETE_ERROR = 4;
	
	private static final int REQUEST_CODE_DESKTOP_WALLPAPER = 1;
	
	private static final int REQUEST_CODE_SETWALLPAPER = 101;
	private static final int MAX_DATA_COUNT = 90 - Consts.LOCAL_WALLPAPERS.length;
	
	private AuroraActionBar mActionBar;
	private GridView mGridView;
	private WallpaperLocalGridAdapter mAdapter;
	private SharedPreferences sp;
	private List<String> mDataList = null;
	private List<String> mDataListForCut = null;
	private AuroraActionBatchHandler<Integer> mActionBatchHandler = null;
	//private AuroraMenu mAuroraMenu;
	private AuroraMenu mBottomAuroraMenu;
	private AuroraProgressDialog mProgressDialog;
	private int mSelectCount = 0;
	private int mSelectPos = -1;
	private String mSelectPath;
	private String updatePath = null;
	private ImageResizer mImageResizer;
	
	private TextView mGoToShop;
	private TextView mGoToGallery;
	private boolean mFromShop = false;
	
	private List<DownloadData> wallpaperDownloadData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent != null){
        	mFromShop = intent.getBooleanExtra(Action.KEY_GOTO_LOCAL_TIME_WALLPAPER_LOCATION, false);
        }
		setAuroraContentView(R.layout.wallpaper_grid_layout, AuroraActionBar.Type.Normal);
		
        initAuroraActionBar();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        //mSelectPos = sp.getInt("selectpos", -1);
        initImageCache();
        initGridView();
	}

	private void initAuroraActionBar() {
		mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.desktop_wallpaper);
        mActionBar.setDisplayHomeAsUpEnabled(true);
		//addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ACTION_BTN_NEW);
		mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		mActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {
            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                finish();
                overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
            }
        });
		mGoToShop = (TextView)findViewById(R.id.goto_shop_item);
		if(mFromShop){
			mGoToShop.setVisibility(View.GONE);
		}else{
			mGoToShop.setText(getResources().getString(R.string.network_wallpaper));
		}
		mGoToShop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DesktopWallpaperLocalActivity.this,MainActivity.class);
				intent.putExtra(Action.KEY_GOTO_SHOP,ThemeConfig.SHOP_POSITION_WALLPAPER);
				startActivity(intent);
				overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
			}
		});

		mGoToGallery = (TextView)findViewById(R.id.goto_gallery);
		mGoToGallery.setText(getResources().getString(R.string.gallery));
		mGoToGallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startFileManagerActivity();
			}
		});
	}
	
	private void initImageCache() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        mImageResizer = new ImageResizer(this, width / 6, height / 8);
        mImageResizer.addImageCache(this, Consts.IMAGE_DESKTOP_CACHE_DIR);
        mImageResizer.setLoadingImage(R.drawable.item_default_bg_normal);
    }
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(null != mActionBatchHandler) {
            	mActionBatchHandler.leaveSelectionMode();
			}
			fillImageDataList();
			mAdapter.setDataList(mDataList, mDataListForCut, null);
			mAdapter.setSelectPos(mSelectPos);
			/*SharedPreferences.Editor editor = sp.edit();
			editor.putInt("selectpos", mSelectPos);
			editor.commit();*/
			mAdapter.notifyDataSetChanged();
			dismissProgressDialogIfShown();
			switch (msg.what) {
			case MSG_DELETE_COMPLETED:
				Toast.makeText(DesktopWallpaperLocalActivity.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
				break;
			case MSG_DELETE_ERROR:
				Toast.makeText(DesktopWallpaperLocalActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
				break;
			}
		}
		
	};
	
	private void dismissProgressDialogIfShown() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
	
	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_NEW:
				startFileManagerActivity();
				break;
			default:
				break;
			}
		}
	};
	
	/*private void startFileManagerActivity() {
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent();
        try {
            intent = packageManager.getLaunchIntentForPackage("com.aurora.filemanager");
            Bundle bundle = new Bundle();
            bundle.putBoolean(Consts.FILEMANAGER_WALLPAPER, true);
            bundle.putBoolean("com.aurora.change", true);
            bundle.putString("file_type", "desktop");
            intent.putExtras(bundle);
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(DesktopWallpaperLocalActivity.this, com.aurora.R.anim.aurora_activity_open_enter, com.aurora.R.anim.aurora_activity_open_exit);
            startActivity(intent, opts.toBundle());
        } catch (Exception e) {
            Log.e("liugj", e.toString());
        }
    }*/
	
	private void startFileManagerActivity() {
		try {
			Intent intent = new Intent();
			intent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setType(IMAGE_TYPE);	
			startActivityForResult(intent, REQUEST_CODE_DESKTOP_WALLPAPER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
                && (requestCode == REQUEST_CODE_DESKTOP_WALLPAPER)) {
			if (data != null && data.getData() != null) {
				Uri uri = data.getData();
				Intent request = new Intent(DesktopWallpaperLocalActivity.this, SetWallpaperCropActivity.class);
	            request.setDataAndType(uri, IMAGE_TYPE)
	            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
	            .putExtra(KEY_SET_AS_WALLPAPER, true);
	            startActivity(request);
			}
		}else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.wallpaper_gridview);
		mDataList = new ArrayList<String>();
		mDataListForCut = new ArrayList<String>();
		fillImageDataList();
		mAdapter = new WallpaperLocalGridAdapter(DesktopWallpaperLocalActivity.this, mDataList, mDataListForCut, mSelectPos);
		mAdapter.setImageResizer(mImageResizer);
		mGridView.setAdapter(mAdapter);
		/*mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mAdapter.getGridScrollState() != scrollState && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mAdapter.setGridScrollState(scrollState);
                    mAdapter.notifyDataSetChanged();
                }
                mAdapter.setGridScrollState(scrollState);
            }

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				
			}
		});*/
		mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if (null != mActionBatchHandler && mActionBatchHandler.isInSelectionMode()) {
            		if (position < Consts.LOCAL_WALLPAPERS.length || position == mSelectPos) {
            			return;
            		}
					//ImageView iv = (ImageView) view.findViewById(R.id.item_selected);
            		ImageView cb = (ImageView)view.findViewById(R.id.item_check);
					//boolean isChecked = iv.isActivated();
            		boolean isChecked = cb.getVisibility() == View.VISIBLE ? true : false;
					//iv.setActivated(!isChecked);
					cb.setVisibility(isChecked ? View.GONE : View.VISIBLE);
					mActionBatchHandler.getSelectionManger().toggle(position);
				}else {
					Intent intent = new Intent(DesktopWallpaperLocalActivity.this, DesktopWallpaperPreviewActivity.class);
	                Bundle bundle = new Bundle();
	                bundle.putString(Consts.WALLPAPER_TYPE_KEY, Consts.WALLPAPER_DESKTOP_TYPE);
	                bundle.putInt("position", position);
	                bundle.putStringArrayList("extra", (ArrayList<String>) mDataList);
	                bundle.putStringArrayList("extra2", (ArrayList<String>) mDataListForCut);
	                if (updatePath != null) {
						bundle.putString("updatepath", updatePath);
					}
	                intent.putExtras(bundle);
	                startActivity(intent);
				}
            }
		});
		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < Consts.LOCAL_WALLPAPERS.length || mAdapter.getEditMod()) {
					return false;
				}
				if(null == mActionBatchHandler && !mActionBar.auroraIsExitEditModeAnimRunning()) {
		            initActionBatchMode();
		            mActionBatchHandler.enterSelectionMode(position == mSelectPos ? null : position);
		            return true;
		        }
				return false;
			}
			
		});
	}
	
	private OnAuroraMenuItemClickListener mAuroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.aurora_menu_del:
				if (mActionBatchHandler != null
						&& mActionBatchHandler.isInSelectionMode()) {
					final List<Integer> selectList = mActionBatchHandler.getSelected();
					final int len = selectList.size();
					if (len == 0) {
						return;
					}
					confirmDeleteDialog(new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mProgressDialog = AuroraProgressDialog.show(
									DesktopWallpaperLocalActivity.this, null,
									getString(R.string.wallpaper_is_deleting),
									true, false);

							try {
								
								StringBuffer sb = new StringBuffer(
										WallpaperValue.WALLPAPER_FILENAME);
								sb.append(" IN ('");
								boolean flag = false;
								for (int i = 0; i < len; i++) {
									Log.e("101010", "---confirmDeleteDialog selectList.get(i) = ----" + selectList.get(i));
									if(selectList.get(i)- Consts.LOCAL_WALLPAPERS.length < mDataListForCut.size()) {
										flag = true;
										sb.append(mDataListForCut.get(selectList.get(i)- Consts.LOCAL_WALLPAPERS.length).replace(WallpaperValue.WALLPAPER_PATH,""));
										sb.append("', '");
										
										File file = new File(mDataListForCut.get(selectList.get(i)
												- Consts.LOCAL_WALLPAPERS.length));
										if (file != null && file.exists()) {
											file.delete();
										}
									}
								}
								
								int start = 0;
								if(flag) {
									sb = sb.delete(sb.lastIndexOf("', '"), sb.length());
									sb.append("')");
									
									Log.e("101010", "---confirmDeleteDialog sb = ----" + sb.toString());
									int res = getContentResolver().delete(
											WallpaperValue.LOCAL_WALLPAPER_URI,
											sb.toString(), null);
								}
								
								//int selectPos = mSelectPos;
								for (int j = 0; j < len; j++) {
									/*if (Consts.LOCAL_WALLPAPERS.length <= selectPos && selectList.get(j) < selectPos) {
											mSelectPos --;
									}*/
									
									//M: aurora delete by tangjun
									/*
									File file = new File(mDataListForCut.get(selectList.get(j)
													- Consts.LOCAL_WALLPAPERS.length));
									if (file != null && file.exists()) {
										file.delete();
									}
									*/
									//M: aurora delete by tangjun
								}
								
								//删除在线下载的壁纸
								deleteLocalDownLoadedWallPaper(selectList);
								
								mHandler.sendEmptyMessage(MSG_DELETE_COMPLETED);
							} catch (Exception e) {
								mHandler.sendEmptyMessage(MSG_DELETE_ERROR);
								Log.e("101010", "---confirmDeleteDialog e = ----" + e);
								e.printStackTrace();
							}
						}
					}, R.string.confirm_delete_selected_message);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void confirmDeleteDialog(OnClickListener listener, int messageId) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.confirm_ok, listener);
        builder.setNegativeButton(R.string.confirm_cancel, null);
        builder.setMessage(messageId);

        builder.show();
    }
	
	private void deleteLocalDownLoadedWallPaper(List<Integer> selectList) {
			for(int i = 0; i < selectList.size(); i++) {
				if(selectList.get(i)- Consts.LOCAL_WALLPAPERS.length >= mDataListForCut.size() && 
						selectList.get(i)- Consts.LOCAL_WALLPAPERS.length - mDataListForCut.size() < mDataList.size()) {
					Theme theme = (Theme)wallpaperDownloadData.get(selectList.get(i)- Consts.LOCAL_WALLPAPERS.length - mDataListForCut.size());
					DatabaseController.getController(this, DatabaseController.TYPE_WALLPAPER_DOWNLOAD).delete(theme.downloadId);
					File f = new File(theme.fileDir, theme.fileName);
					if (f != null && f.exists()) {
						f.delete();
					}
				}
			}
	}
	
	private void initActionBatchMode() {
		setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack); // 必须写在布局前
		mActionBar.initActionBottomBarMenu(R.menu.aurora_menu_delete, 1);
		//mActionBar.showActionBarMenu();
		mActionBar.setShowBottomBarMenu(true);
		mActionBar.showActionBarDashBoard();
		// mActionBar.showActionBottomeBarMenu();
		//mAuroraMenu = mActionBar.getActionBarMenu();
		mBottomAuroraMenu = mActionBar.getAuroraActionBottomBarMenu();
		initActionBatchHandler();
		mBottomAuroraMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				mAdapter.setEditMode(false);
				// Aurora liugj 2014-08-06 modified for bug-7313 start
				if (mActionBatchHandler != null) {
					mActionBatchHandler.destroyAction();
				}
				// Aurora liugj 2014-08-06 modified for bug-7313 end
				mAdapter.notifyDataSetChanged();
				// setMenuEnable(true);

				mActionBatchHandler = null;

			}
		});
	}
	
	protected void initActionBatchHandler() {
		mActionBatchHandler = new AuroraActionBatchHandler<Integer>(this, mActionBar) {
			
			@Override
			public void enterSelectionMode(Integer itemPressing) {
				mAdapter.setEditMode(true);
				//setMenuEnable(false);
				super.enterSelectionMode(itemPressing);
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public Set getDataSet() {
				// TODO Auto-generated method stub
				int size = mDataList.size() + mDataListForCut.size();
				if (size > 0) {
					Set<Integer> dataSet = new HashSet<Integer>(size);
	                for(int i = 0; i < size; i++) {
	                	int position = Consts.LOCAL_WALLPAPERS.length + i;
	                	if (position != mSelectPos) {
	                		dataSet.add(position);
						}
	                }
	                return dataSet;
				}
                return null;
			}
			
			@Override
			public void leaveSelectionMode() {
				if (mBottomAuroraMenu != null) {
					mActionBar.setShowBottomBarMenu(false);
					mActionBar.showActionBarDashBoard();
					//mActionBar.showActionBarMenu();
					//mAuroraMenu.dismiss();
				}
			}

			@Override
			public void updateUi() {
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
				mSelectCount = null != getSelected() ? getSelected().size() : 0;
				mBottomAuroraMenu.setBottomMenuItemEnable(1, mSelectCount == 0 ? false : true);
			}

			public void bindToAdapter(SelectionManager<Integer> selectionManager) {
                // TODO Auto-generated method stub
                mAdapter.setSelectionManager(selectionManager);
            }
		};
	}
	
	private void fillImageDataList() {
		
		Log.e("101010", "---fillImageDataList");
		if (mDataList != null) {
			mDataList.clear();
		}
		if (mDataListForCut != null) {
			mDataListForCut.clear();
		}
		Cursor cursor = null;
		mSelectPath = sp.getString("selectpath", "-1");
		if (mSelectPath.equals("-1")) {
			mSelectPos = sp.getInt("selectpos", -1);
		}
		
		try {
            cursor = getContentResolver().query(WallpaperValue.LOCAL_WALLPAPER_URI, new String[] { WallpaperValue.WALLPAPER_FILENAME }, null, null, DEFAULT_SORT_ORDER);
            if ((cursor != null)) {
            	while (cursor.moveToNext() && mDataListForCut.size() < MAX_DATA_COUNT) {
            		String path = WallpaperValue.WALLPAPER_PATH + cursor.getString(0);
            		if (!mSelectPath.equals("-1") && mSelectPath.equals(cursor.getString(0))) {
						mSelectPos = Consts.LOCAL_WALLPAPERS.length + mDataListForCut.size();
						if (Consts.isChangedByLocal == 2) {
							updatePath = path;
						}
					}
            		Log.d("101010", "path:"+path);
            		mDataListForCut.add(path);
				}
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
		wallpaperDownloadData = DatabaseController.getController(this, DatabaseController.TYPE_WALLPAPER_DOWNLOAD).getDownloadedDatas();
		for(int i = 0; i < wallpaperDownloadData.size(); i++) {
			
			StringBuilder ringTonePath = new StringBuilder(wallpaperDownloadData.get(i).fileDir);
			ringTonePath.append("/").append(wallpaperDownloadData.get(i).fileName);
			
			if (!mSelectPath.equals("-1") && mSelectPath.equals(ringTonePath.toString())) {
				mSelectPos = Consts.LOCAL_WALLPAPERS.length + mDataListForCut.size() + mDataList.size();
			}
			
			mDataList.add(ringTonePath.toString());
		}
		if(mDataList.size() > 0) {
			Log.d("101010", "fillImageDataList mDataList = :"+ mDataList.get(0));
		}
		if(mDataListForCut.size() > 0) {
			Log.d("101010", "fillImageDataList mDataListForCut = :"+ mDataListForCut.get(0));
		}
		Log.d("101010", "fillImageDataList mSelectPos = :"+ mSelectPos);
		
	}
	
	@Override
    protected void onResume() {
		super.onResume();
		if (Consts.isWallPaperChanged) {
			if (Consts.isChangedByLocal == 2) {
				fillImageDataList();
				if (mAdapter != null) mAdapter.setDataList(mDataList, mDataListForCut, updatePath);
			}else if (Consts.isChangedByLocal == 1){
				mSelectPos = sp.getInt("selectpos", -1);
			}
			if (mAdapter != null) {
				mAdapter.setSelectPos(mSelectPos);
				//mAdapter.notifyDataSetChanged();
				mAdapter.onResume();
			}
			mGridView.setSelection(mSelectPos);
			//mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();
			Consts.isChangedByLocal = 0;
			Consts.isWallPaperChanged = false;
		}else {
			if (mAdapter != null) {
				mAdapter.onResume();
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.onPause();
        }
    }
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
        if (mDataList != null) {
			mDataList.clear();
			mDataList = null;
		}
        if (mDataListForCut != null) {
        	mDataListForCut.clear();
        	mDataListForCut = null;
		}
        if (mAdapter != null) {
			//mAdapter.clear();
			mAdapter.clearCache();
		}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (mActionBatchHandler != null && mActionBatchHandler.isInSelectionMode()) {
        		if (!mActionBar.auroraIsEntryEditModeAnimRunning()) {
        			mActionBatchHandler.leaveSelectionMode();
				}
			}else {
				finish();
	            overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
			}
        }
        return true;
    }
}
