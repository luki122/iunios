package com.android.gallery3d.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.android.gallery3d.app.AlbumPage.state;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.local.tools.AuroraFilenameFilter;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.tools.SqliteUtils;
import com.android.gallery3d.local.tools.StorageUtils;
import com.android.gallery3d.local.tools.ThreadPoolExecutorUtils;
import com.android.gallery3d.local.widget.GalleryAdapter;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.viewpager.ViewpagerActivity;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadService;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.android.gallery3d.R;

public class GalleryLocalActivity extends BasicActivity implements LoaderCallbacks<Cursor>, OnItemClickListener, OnItemLongClickListener,
		OnScrollListener {
	private static final String TAG = "GalleryLocalActivity";
	private AuroraListView listview;
	private GalleryAdapter galleryAdapter;
	private int firstPositionTop, firstVisiblePosition;
	private ConcurrentHashMap<String, List<MediaFileInfo>> mediaFilesMap = new ConcurrentHashMap<String, List<MediaFileInfo>>();
	private ConcurrentHashMap<Integer, String> foldersMap = new ConcurrentHashMap<Integer, String>();
	private ConcurrentHashMap<Integer, String> foldersMapTemp = new ConcurrentHashMap<Integer, String>();
	private TextView leftView, rightView;
	private AuroraActionBar auroraActionBar;
	private ActivityInfo activityInfo;
	private boolean isShieldActivity;
	private List<String> dcimPathFolders = new ArrayList<String>();
	private int pagePosition = -1;
	private RelativeLayout emptyView;

	public boolean isShieldActivity() {
		return isShieldActivity;
	}

	public List<String> getFoldersPath() {
		return foldersPath;
	}

	public AuroraListView getListview() {
		return listview;
	}

	public ConcurrentHashMap<Integer, String> getFoldersMap() {
		return foldersMap;
	}

	public void setFoldersMap(ConcurrentHashMap<Integer, String> map) {
		foldersMap.clear();
		foldersMap.putAll(map);
		map.clear();
		galleryAdapter.setFoldersMap(foldersMap);
		galleryAdapter.notifyDataSetChanged();
	}

	public ConcurrentHashMap<String, List<MediaFileInfo>> getMediaFilesMap() {
		return mediaFilesMap;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(TAG, "--------onCreate");
		setAuroraContentView(R.layout.gallery_local_listview);
		emptyView = (RelativeLayout) findViewById(R.id.empty_relative);
		addSystemPaths();
		try {
			activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
			LogUtil.d(TAG, "----activityInfo:" + activityInfo.name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (activityInfo != null && activityInfo.name.endsWith(getString(R.string.aurora_shield))) {
			auroraActionBar = getAuroraActionBar();
			auroraActionBar.setTitle(R.string.aurora_shield_manager);
			setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
			auroraActionBar.initActionBottomBarMenu(R.menu.aurora_local_album_set_menu, 1);
			actionBarSetOnClickListener();
			isShieldActivity = true;
			FrameLayout.LayoutParams lp = (LayoutParams) emptyView.getLayoutParams();
			lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.aurora_no_pic_paddingtop_local), 0, 0);
			emptyView.setLayoutParams(lp);
		} else {
			getAuroraActionBar().setVisibility(View.GONE);
			auroraActionBar = ((GalleryAppImpl) getApplication()).getmAuroraActionBar();
		}
		setAuroraActionBar(auroraActionBar);
		listview = (AuroraListView) findViewById(R.id.albums_listView);
		listview.setOnItemClickListener(this);
		listview.setOnItemLongClickListener(this);
		listview.setOnScrollListener(this);
		listview.auroraEnableOverScroll(false);
		initLoader();
		Intent intent = new Intent(this, XCloudAutoUploadService.class);
		startService(intent);
	}

	public void initAuroraActionBar() {
		LogUtil.d(TAG, "----------initAuroraActionBar");
		((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext()).setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		auroraActionBar.initActionBottomBarMenu(R.menu.aurora_local_album_menu, 2);
		actionBarSetOnClickListener();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		boolean isNeedReload = StorageUtils.getIntPref(getApplication(), "DataChange", 0) == 1;
		boolean isOnKeyDown = StorageUtils.getIntPref(getApplication(), "onKeyDown", 0) == 1;
		LogUtil.d(TAG, "--------onRestart restartLoader " + isNeedReload+"--isOnKeyDown:"+isOnKeyDown);
		if (isNeedReload&&isOnKeyDown) {
			foldersMap.clear();
			foldersMapTemp.clear();
			mediaFilesMap.clear();
			getLoaderManager().restartLoader(SqliteUtils.ImageType, null, this);
			getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
			
		}else if (!isOnKeyDown) {
			foldersMap.clear();
			foldersMapTemp.clear();
			mediaFilesMap.clear();
			getLoaderManager().restartLoader(SqliteUtils.ImageType, null, this);
			getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
		}
		StorageUtils.setIntPref(getApplication(), "DataChange", 0);
		StorageUtils.setIntPref(getApplication(), "onKeyDown", 0);
	}

	@Override
	public void onResume() {
		LogUtil.d(TAG, "--------onResume");
		super.onResume();
	}

	@Override
	protected void onStart() {
		LogUtil.d(TAG, "--------onStart");
		super.onStart();
	}

	@Override
	protected void onPause() {
		LogUtil.d(TAG, "--------onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		LogUtil.d(TAG, "--------onStop");
		super.onStop();
		getLoaderManager().destroyLoader(SqliteUtils.ImageType);
		getLoaderManager().destroyLoader(SqliteUtils.VideoType);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		foldersMap.clear();
		foldersMapTemp.clear();
		mediaFilesMap.clear();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ViewpagerActivity activity = ((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext());
		LogUtil.d(TAG, "--------onKeyDown ");
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && galleryAdapter.isOperation()) {
			galleryAdapter.setOperation(false);
			GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
			if (!isShieldActivity) {
				activity.subActivitySelectionModeChange(false);
			}
			return true;
		}
		if (isShieldActivity || activity.getCurrentActivity() instanceof GalleryLocalActivity) {
			return super.onKeyDown(keyCode, event);
		} else {
			return false;
		}
	}

	/**
	 * 第一次loader
	 */
	private void initLoader() {
		LogUtil.d(TAG, "----initLoader");
		getLoaderManager().initLoader(SqliteUtils.ImageType, null, this);
		getLoaderManager().initLoader(SqliteUtils.VideoType, null, this);
	}

	public static final String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String dcimPath = sdPath + "/" + Environment.DIRECTORY_DCIM + "/";
	public static final String cameraPath = dcimPath + "Camera";
	public static final String screenShotsPath = sdPath + "/Screenshots";
	public static final String cloudPath = dcimPath + "cloud";
	public static final String videoPath = "videoPath";
	public static final String collectionPath = "collectionPath";
	// 特殊排序相册1视频2收藏3系统截图4云下载5
	private List<String> systemPaths = new ArrayList<String>();

	public List<String> getSystemPaths() {
		return systemPaths;
	}

	private void addSystemPaths() {
		systemPaths.clear();
		systemPaths.add(cameraPath);
		systemPaths.add(videoPath);
		systemPaths.add(collectionPath);
		systemPaths.add(screenShotsPath);
		systemPaths.add(cloudPath);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LogUtil.d(TAG, "-----------onCreateLoader id:" + id);
		return SqliteUtils.buildCursorLoader(id, this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		LogUtil.d(TAG, "-----------onLoadFinished id:" + loader.getId());
		CursorAsyncTask asyncTask = new CursorAsyncTask(loader);
		asyncTask.executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolUtil().getExecutor(), cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		LogUtil.d(TAG, "-----------onLoaderReset id:" + loader.getId());
	}

	private class CursorAsyncTask extends AsyncTask<Cursor, Void, Void> {
		private Loader<Cursor> loader;

		public CursorAsyncTask(Loader<Cursor> loader) {
			super();
			this.loader = loader;
		}

		private void putMediaFileInfoData(int index, String path, MediaFileInfo info, List<String> noShowPaths) {
			if (isShieldActivity && (!noShowPaths.contains(path)) || !isShieldActivity && noShowPaths.contains(path)) {
				return;
			}
			while (foldersMapTemp.containsKey(index)) {
				index++;
			}
			if (!foldersMapTemp.containsValue(path)) {
				foldersMapTemp.put(index, path);
			}
			if (mediaFilesMap.containsKey(path)) {// 防止重复添加MediaFileInfo
				if (!mediaFilesMap.get(path).contains(info)) {
					mediaFilesMap.get(path).add(info);
				}
			} else {
				List<MediaFileInfo> Infos = new ArrayList<MediaFileInfo>();
				Infos.add(info);
				mediaFilesMap.put(path, Infos);
			}
		}

		@Override
		protected Void doInBackground(Cursor... params) {
			try {
				synchronized (foldersMapTemp) {// fix 文件排序混乱
					Cursor cursor = params[0];
					List<String> noShowPaths = GalleryLocalUtils.doParseXml(GalleryLocalActivity.this);
					allNoShowPaths.clear();
					allNoShowPaths.addAll(noShowPaths);
					if (cursor != null && cursor.moveToFirst()) {
						int index = 0;
						do {
							MediaFileInfo info = new MediaFileInfo();
							String path = "";
							if (loader.getId() == SqliteUtils.ImageType) {
								info.dbId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
								info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
								path = info.filePath.substring(0, info.filePath.lastIndexOf("/"));
								info.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
								info.fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE));
								info.orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
								info.favorite = (cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 0) ? false : true;
								info.isImage = true;
							} else if (loader.getId() == SqliteUtils.VideoType) {
								info.dbId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
								info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
								info.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
								info.fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE));
								info.favorite = (cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 0) ? false : true;
								info.isImage = false;
								LogUtil.d(TAG, "--filePath:" + info.filePath);
								path = info.filePath.substring(0, info.filePath.lastIndexOf("/"));
								putMediaFileInfoData(index, path, info, noShowPaths);
								path = videoPath;
							}
							putMediaFileInfoData(index, path, info, noShowPaths);
							if (info.favorite) {
								putMediaFileInfoData(index, collectionPath, info, noShowPaths);
							}
						} while (cursor.moveToNext());
						int k = 0;
						for (String sysPaths : systemPaths) {
							if (foldersMapTemp.containsValue(sysPaths)) {// 特殊排序相册1
								foldersMap.put(k, sysPaths);
								LogUtil.d(TAG, "----sysPaths:" + sysPaths + " k:" + k);
								k++;
							}
						}
						File file = new File(dcimPath);
						File[] files = file.listFiles(AuroraFilenameFilter.getInstance());
						dcimPathFolders.clear();
						if (files != null) {
							for (int i = 0; i < files.length; i++) {
								String temp = files[i].getAbsolutePath();
								if (foldersMapTemp.containsValue(temp)) {
									foldersMap.put(k, temp);
									dcimPathFolders.add(temp);
									k++;
								}
							}
						}
						int size = foldersMapTemp.size();
						for (int i = 0; i < size; i++) {
							String tempPath = foldersMapTemp.get(i);
							if (!(systemPaths.contains(tempPath) || dcimPathFolders.contains(tempPath))) {
								foldersMap.put(k, tempPath);
								k++;
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isDestroyed()) {
				return;
			}
			if (galleryAdapter == null) {
				galleryAdapter = new GalleryAdapter(GalleryLocalActivity.this);
				listview.setAdapter(galleryAdapter);
				setBaseAdapter(galleryAdapter);
			}
			galleryAdapter.setFoldersMap(foldersMap);
			galleryAdapter.setMediaFilesMap(mediaFilesMap);
			galleryAdapter.notifyDataSetChanged();
			listview.setSelectionFromTop(firstVisiblePosition, firstPositionTop);
			if (galleryAdapter.getCount() == 0) {
				emptyView.setVisibility(View.VISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void imageDataChange() {
		foldersMap.clear();
		foldersMapTemp.clear();
		mediaFilesMap.clear();
		// galleryAdapter.notifyDataSetChanged();
		LogUtil.d(TAG, "---imageDataChange");
		getLoaderManager().restartLoader(SqliteUtils.ImageType, null, this);
		getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
	}

	@Override
	public void videoDataChange() {
		foldersMap.clear();
		foldersMapTemp.clear();
		mediaFilesMap.clear();
		// galleryAdapter.notifyDataSetChanged();
		LogUtil.d(TAG, "---videoDataChange");
		getLoaderManager().restartLoader(SqliteUtils.ImageType, null, this);
		getLoaderManager().restartLoader(SqliteUtils.VideoType, null, this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (galleryAdapter == null) {
			return;
		}
		if (galleryAdapter.isOperation()) {
			galleryAdapter.setItemPicAnim(true);
			setSelectPostion(position);
			String path = (String) galleryAdapter.getItem(position);
			if (foldersPath.contains(path)) {
				foldersPath.remove(path);
			} else {
				foldersPath.add(path);
			}
			LogUtil.d(TAG, "------------------------------------------foldersPath:" + foldersPath.size());
			updateAuroraItemBottomBarState(true);
			return;
		}
		ArrayList<MediaFileInfo> mediaFileInfos = (ArrayList<MediaFileInfo>) galleryAdapter.getItemMediaFileInfos(position);
		String albumName = (String) galleryAdapter.getItem(position);
		Bundle bundle = new Bundle();
		String systemAlbumName = galleryAdapter.getSystemAblumName(GalleryLocalActivity.this, albumName);
		if (TextUtils.isEmpty(systemAlbumName)) {
			int pos = albumName.lastIndexOf('/');
			systemAlbumName = albumName.substring(pos + 1);
		}
		bundle.putString("albumName", systemAlbumName);
		bundle.putString("albumPath", albumName);
		bundle.putBoolean("edit", dcimPathFolders.contains(albumName));
		((GalleryAppImpl) getApplication()).setMediaFileInfos(mediaFileInfos);
		// bundle.putParcelableArrayList("infos", mediaFileInfos);
		Intent intent = new Intent(this, GalleryItemActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (galleryAdapter.isOperation()) {
			return true;
		}
		foldersPath.clear();
		foldersPath.add((String) galleryAdapter.getItem(position));
		galleryAdapter.setItemPicAnim(false);
		galleryAdapter.setOperation(true);
		updateAuroraItemBottomBarState(false);
		GalleryLocalUtils.showOrHideMenu(auroraActionBar, true);
		if (!isShieldActivity) {
			((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext()).subActivitySelectionModeChange(true);
		}
		return true;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 不滚动时保存当前滚动到的位置
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			firstVisiblePosition = listview.getFirstVisiblePosition();
			View firstView = listview.getChildAt(0);
			if (firstView != null) {
				firstPositionTop = firstView.getTop();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

	private void updateAuroraItemBottomBarState(boolean notify) {
		if (!isShieldActivity) {
			if (foldersPath.isEmpty()) {
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 1, false);
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 0, false);
			} else {
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 1, true);
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 0, true);
			}
		} else {
			if (foldersPath.isEmpty()) {
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 0, false);
			} else {
				GalleryLocalUtils.setItemBottomBar(auroraActionBar, 0, true);
			}
		}
		if (notify) {
			galleryAdapter.notifyDataSetChanged();
		}
		updateAuroraitemActionBarState();
	}

	/**
	 * 修改auroraActionBar左右中TextView显示
	 */
	private void updateAuroraitemActionBarState() {
		if (foldersPath.size() == foldersMap.size() && rightView != null) {
			rightView.setText(getResources().getString(R.string.unmyselect_all));
		} else if (rightView != null) {
			rightView.setText(getResources().getString(R.string.myselect_all));
		}
		auroraActionBar.getMiddleTextView().setText(String.format(getResources().getString(R.string.adlum_select_num, foldersPath.size())));
	}

	/**
	 * 设置全选反选监听
	 */
	private void actionBarSetOnClickListener() {
		leftView = (TextView) auroraActionBar.getSelectLeftButton();
		rightView = (TextView) auroraActionBar.getSelectRightButton();
		rightView.setText(R.string.myselect_all);
		leftView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning(auroraActionBar)) {
					if (galleryAdapter.isOperation()) {
						galleryAdapter.setOperation(false);
						GalleryLocalUtils.showOrHideMenu(auroraActionBar, false);
					}
					updateAuroraItemBottomBarState(true);
					((ViewpagerActivity) ((GalleryAppImpl) getApplicationContext()).getmActivityContext()).subActivitySelectionModeChange(false);
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning(auroraActionBar)) {
					galleryAdapter.setItemPicAnim(false);
					if (foldersPath.size() == foldersMap.size()) {
						foldersPath.clear();
					} else {
						foldersPath.clear();
						foldersPath.addAll(foldersMap.values());
					}
					updateAuroraItemBottomBarState(true);
				}
			}
		});
	}
}
