package com.android.gallery3d.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.app.AlbumPage.state;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.local.tools.SqliteUtils;
import com.android.gallery3d.local.tools.StorageUtils;
import com.android.gallery3d.local.tools.ThreadPoolExecutorUtils;
import com.android.gallery3d.local.widget.DragSelectGridView;
import com.android.gallery3d.local.widget.GalleryItemAdapter;
import com.android.gallery3d.local.widget.GalleryItemAdapter.SelectionListener;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.android.gallery3d.R;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class GalleryItemActivity extends BasicActivity implements OnItemClickListener, OnItemLongClickListener, LoaderCallbacks<Cursor>,
		SelectionListener {
	private DragSelectGridView gridView;
	private String albumName;
	private String albumPath;
	private List<MediaFileInfo> mediaFileInfos;
	private GalleryItemAdapter itemAdapter;
	private AuroraActionBar auroraActionBar;
	private static final int RENAME_ALBUM_NAME = 10000;
	private TextView leftView, rightView, empty_text;
	private RelativeLayout empty_relative;
	private ImageView empty_image;
	private boolean isFristLoad = true;
	private final static String TAG = "GalleryItemActivity";
	private boolean isCreate = false;

	public List<MediaFileInfo> getSelectFileInfo() {
		return selectFileInfo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_local_album_item, AuroraActionBar.Type.Normal, false);
		initActionBar();
		gridView = (DragSelectGridView) findViewById(R.id.local_album_item);
		empty_relative = (RelativeLayout) findViewById(R.id.empty_relative);
		empty_image = (ImageView) findViewById(R.id.empty_image);
		empty_text = (TextView) findViewById(R.id.empty_text);
		initBundleData();
		isCreate = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		LogUtil.d(TAG, "---onRestart");
		refreshAlbumPathData();
	}

	/**
	 * 刷新数据
	 */
	private void refreshAlbumPathData() {
		if (isDestroyed() || !isCreate) {
			return;
		}
		LogUtil.d(TAG, "----refreshAlbumPathData");
		if (mediaFileInfos != null) {
			mediaFileInfos.clear();
		}
		if (itemAdapter != null) {
			itemAdapter.setItemPicAnim(false);
		}
		if (!albumPath.equals(GalleryLocalActivity.videoPath)) {
			if (isFristLoad) {
				getLoaderManager().initLoader(SqliteUtils.ImageType, null, this);
				getLoaderManager().initLoader(SqliteUtils.VideoType, null, this);
				isFristLoad = false;
			} else {
				getLoaderManager().restartLoader(SqliteUtils.ImageType, null, this);
				getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
			}
		} else {
			if (isFristLoad) {
				getLoaderManager().initLoader(SqliteUtils.VideoType, null, this);
				isFristLoad = false;
			} else {
				getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
			}
		}
	}

	/**
	 * 初始化加载数据
	 */
	private void initBundleData() {
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			return;
		}
		albumName = bundle.getString("albumName");
		albumPath = bundle.getString("albumPath");
		GalleryLocalUtils.setAuroraActionBarTitle(auroraActionBar, albumName);
		// mediaFileInfos = bundle.getParcelableArrayList("infos");
		mediaFileInfos = ((GalleryAppImpl) getApplication()).getMediaFileInfos();
		if (mediaFileInfos == null || mediaFileInfos.size() == 0) {
			refreshAlbumPathData();
		}
		if (mediaFileInfos != null) {
			LogUtil.d(TAG, "----mediaFileInfos:" + mediaFileInfos.size());
		}
		// mediaFileInfos.clear();//test
		itemAdapter = new GalleryItemAdapter(mediaFileInfos, this);
		gridView.setAdapter(itemAdapter);
		setBaseAdapter(itemAdapter);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);
		itemAdapter.setSelectionListener(this);
		if (bundle.getBoolean("edit")) {
			auroraActionBar.addItem(AuroraActionBarItem.Type.Edit, R.id.aurora_action_bar_edit);
			auroraActionBar.setOnAuroraActionBarListener(actionBarItemClickListener);
		}
		selectFileInfo.clear();
		mHandler.sendEmptyMessage(MSG_EMPTY);
	}

	/**
	 * 初始化auroraActionBar
	 */
	private void initActionBar() {
		auroraActionBar = getAuroraActionBar();
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		auroraActionBar.initActionBottomBarMenu(R.menu.aurora_local_album_item_menu, 5);
		setAuroraSystemMenuCallBack(auroraMenuCallBack);
		setAuroraMenuItems(R.menu.gallery_albumpage_menu);
		getAuroraMenu().setAnimationStyle(com.aurora.R.style.AuroraMenuRightBottomAnimation);
		setAuroraActionBar(auroraActionBar);
		actionBarSetOnClickListener();
		mMenuPaddingRight = getResources().getDimensionPixelSize(R.dimen.sys_menu_padding_right);
		mMenuPaddingBottom = getResources().getDimensionPixelSize(R.dimen.sys_menu_padding_bottom);
	}

	/**
	 * 修改auroraActionBar左右中TextView显示
	 */
	private void updateAuroraitemActionBarState() {
		if (selectFileInfo.size() == mediaFileInfos.size() && rightView != null) {
			rightView.setText(getResources().getString(R.string.unmyselect_all));
		} else if (rightView != null) {
			rightView.setText(getResources().getString(R.string.myselect_all));
		}
		auroraActionBar.getMiddleTextView().setText(String.format(getResources().getString(R.string.adlum_select_num, selectFileInfo.size())));
	}

	/**
	 * 设置全选反选监听
	 */
	private void actionBarSetOnClickListener() {
		leftView = (TextView) getAuroraActionBar().getSelectLeftButton();
		rightView = (TextView) getAuroraActionBar().getSelectRightButton();
		rightView.setText(R.string.myselect_all);
		leftView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning(auroraActionBar)) {
					if (itemAdapter.isOperation()) {
						itemAdapter.setOperation(false);
						GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
					}
					updateAuroraItemBottomBarState(true);
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning(auroraActionBar)) {
					itemAdapter.setItemPicAnim(false);
					if (selectFileInfo.size() == mediaFileInfos.size()) {
						selectFileInfo.clear();
					} else {
						selectFileInfo.clear();
						selectFileInfo.addAll(mediaFileInfos);
					}
					updateAuroraItemBottomBarState(true);
				}
			}
		});
	}

	@Override
	public void imageDataChange() {
		LogUtil.d(TAG, "---imageDataChange");
		refreshAlbumPathData();
	}

	@Override
	public void videoDataChange() {
		refreshAlbumPathData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCreate = false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (itemAdapter.isOperation()) {
			return true;
		}
		selectFileInfo.clear();
		selectFileInfo.add((MediaFileInfo) itemAdapter.getItem(position));
		itemAdapter.setOperation(true);
		itemAdapter.setItemPicAnim(false);
		updateAuroraItemBottomBarState(false);
		GalleryLocalUtils.showOrHideMenu(auroraActionBar, true);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MediaFileInfo fileInfo = mediaFileInfos.get(position);
		if (itemAdapter.isOperation()) {
			itemAdapter.setItemPicAnim(true);
			setSelectPostion(position);
			MediaFileInfo mediaFileInfo = (MediaFileInfo) itemAdapter.getItem(position);
			if (selectFileInfo.contains(mediaFileInfo)) {
				selectFileInfo.remove(mediaFileInfo);
			} else {
				selectFileInfo.add((MediaFileInfo) itemAdapter.getItem(position));
			}
			updateAuroraItemBottomBarState(true);
			return;
		}
		if (fileInfo != null) {
			StorageUtils.setIntPref(getApplication(), "DataChange", 1);
			LogUtil.d(TAG, "--fileInfo.filePath:" + fileInfo.filePath + " isImage:" + fileInfo.isImage + "--dbId:" + fileInfo.dbId);
			GalleryLocalUtils.viewImg(fileInfo.dbId, GalleryItemActivity.this, position, fileInfo.isImage,
					albumPath.equals(GalleryLocalActivity.collectionPath), albumPath.equals(GalleryLocalActivity.videoPath));
		}
	}

	private void updateAuroraItemBottomBarState(boolean notify) {
		for (int i = 0; i < 5; i++) {
			GalleryLocalUtils.setItemBottomBar(auroraActionBar, i, selectFileInfo.isEmpty() ? false : true);
		}
		if (albumPath.equals(GalleryLocalActivity.collectionPath) || isFavorite()) {
			GalleryLocalUtils.setItemBottomBarVisibility(auroraActionBar, 1, false);
			GalleryLocalUtils.setItemBottomBarVisibility(auroraActionBar, 2, true);
		} else {
			GalleryLocalUtils.setItemBottomBarVisibility(auroraActionBar, 2, false);
			GalleryLocalUtils.setItemBottomBarVisibility(auroraActionBar, 1, true);
		}
		if (notify) {
			itemAdapter.notifyDataSetChanged();
		}
		updateAuroraitemActionBarState();
	}

	private boolean isFavorite() {
		if (selectFileInfo.isEmpty()) {
			return false;
		}
		for (int i = 0; i < selectFileInfo.size(); i++) {
			if (!selectFileInfo.get(i).favorite) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && itemAdapter.isOperation() && event.getRepeatCount() == 0) {
			itemAdapter.setOperation(false);
			GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
			return true;
		}
		StorageUtils.setIntPref(getApplication(), "onKeyDown", 1);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LogUtil.d(TAG, "---onCreateDialog--id:" + id);
		if (RENAME_ALBUM_NAME == id) {
			View view = LayoutInflater.from(this).inflate(R.layout.rename_local_album, null);
			inputName = (AuroraEditText) view.findViewById(R.id.gallert_local_rename);
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(GalleryItemActivity.this).setTitle(R.string.aurora_album_rename)
					.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							File oldFile = new File(albumPath);
							File newFile = new File(albumPath.replace(albumName, inputName.getText().toString()));
							int index = 1;
							while (newFile.exists()) {
								newFile = new File(newFile.getAbsolutePath() + index);
								index++;
							}
							if (oldFile.renameTo(newFile)) {
								albumName = inputName.getText().toString();
								albumPath = newFile.getAbsolutePath();
								GalleryLocalUtils.setAuroraActionBarTitle(auroraActionBar, albumName);
								operationUtil.sendBroadcastScan(MediaFileOperationUtil.ACTION_DIR_SCAN, newFile.getAbsolutePath());
							}
							GalleryLocalUtils.hideInputMethod(GalleryItemActivity.this, mDialog.getCurrentFocus());
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							GalleryLocalUtils.hideInputMethod(GalleryItemActivity.this, mDialog.getCurrentFocus());
						}
					});
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			GalleryLocalUtils.showInputMethod(GalleryItemActivity.this);
			return mDialog;
		}
		return super.onCreateDialog(id);
	}

	/**
	 * actionBar 回调
	 */
	private OnAuroraActionBarItemClickListener actionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			if (itemId == R.id.aurora_action_bar_edit) {
				renameAlbumName();
			}
		}
	};

	private void renameAlbumName() {
		showDialog(RENAME_ALBUM_NAME);
		if (inputName != null) {
			inputName.setText("");
		}
		if (mDialog != null && mDialog.isShowing()) {
			final Button positiveButton = mDialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE);
			positiveButton.setEnabled(false);
			inputName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					String newText = inputName.getText().toString();
					if (newText.trim().length() == 0) {
						positiveButton.setEnabled(false);
					} else if (GalleryLocalUtils.isWrongText(newText.trim())) {
						Toast.makeText(GalleryItemActivity.this, getResources().getString(R.string.aurora_album_error_char), 1).show();
						positiveButton.setEnabled(false);
					} else {
						positiveButton.setEnabled(true);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

				@Override
				public void afterTextChanged(Editable arg0) {}
			});
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LogUtil.d(TAG, "--onCreateLoader--albumPath:" + albumPath + "---id:" + id);
		if (albumPath.equals(GalleryLocalActivity.collectionPath)) {
			return SqliteUtils.buildFavoriteCursorLoader(GalleryItemActivity.this);
		}
		if (albumPath.equals(GalleryLocalActivity.videoPath)) {
			return SqliteUtils.buildCursorLoader(id, GalleryItemActivity.this, null);
		}
		return SqliteUtils.buildCursorLoader(id, GalleryItemActivity.this, albumPath);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		LogUtil.d(TAG, "--onLoadFinished " + loader.getId());
		CursorAsyncTask asyncTask = new CursorAsyncTask(loader);
		asyncTask.executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolUtil().getExecutor(), cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {}

	/**
	 * 数据库数据变动数据重新加载
	 * @author jiangxh
	 */
	private class CursorAsyncTask extends AsyncTask<Cursor, Void, List<MediaFileInfo>> {
		private Loader<Cursor> loader;

		public CursorAsyncTask(Loader<Cursor> loader) {
			super();
			this.loader = loader;
		}

		@Override
		protected List<MediaFileInfo> doInBackground(Cursor... params) {
			Cursor cursor = params[0];
			List<MediaFileInfo> infos = new ArrayList<MediaFileInfo>();
			try {
				synchronized (mediaFileInfos) {
					if (cursor != null && !cursor.isClosed()) {
						LogUtil.d(TAG, "---loader.getId():" + loader.getId());
						for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
							MediaFileInfo info = new MediaFileInfo();
							if (loader.getId() == SqliteUtils.ImageType) {
								info.dbId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
								info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
								info.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
								info.fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE));
								info.orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
								info.favorite = (cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 0) ? false : true;
								try {
									if (cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)) == FileColumns.MEDIA_TYPE_VIDEO) {
										info.isImage = false;
									} else {
										info.isImage = true;
									}
								} catch (Exception e) {
									info.isImage = true;
								}
							} else if (loader.getId() == SqliteUtils.VideoType) {
								info.dbId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
								info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
								info.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
								info.fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE));
								info.favorite = (cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 0) ? false : true;
								info.isImage = false;
							}
							infos.add(info);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return infos;
		}

		@Override
		protected void onPostExecute(List<MediaFileInfo> result) {
			super.onPostExecute(result);
			if (!mediaFileInfos.containsAll(result)) {
				mediaFileInfos.addAll(result);
			}
			itemAdapter.notifyDataSetChanged();
			mHandler.removeMessages(MSG_EMPTY);
			mHandler.sendEmptyMessageDelayed(MSG_EMPTY, 500);
			LogUtil.d(TAG, "--onPostExecute");
		}
	}

	private static final int MSG_EMPTY = 101;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (MSG_EMPTY == msg.what) {
				if (itemAdapter.getCount() == 0) {
					empty_relative.setVisibility(View.VISIBLE);
					if (albumPath.endsWith(GalleryLocalActivity.videoPath)) {
						empty_image.setImageResource(R.drawable.no_vedio);
						empty_text.setText(getResources().getString(R.string.aurora_no_video));
					}
				} else {
					empty_relative.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public void onDragSelectionChanged() {
		updateAuroraItemBottomBarState(false);
	}
}
