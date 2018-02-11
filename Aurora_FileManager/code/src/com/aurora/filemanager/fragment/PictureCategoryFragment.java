package com.aurora.filemanager.fragment;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Text;

import android.R.string;
import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.filemanager.R;
import com.aurora.filemanager.FileApplication;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileSortHelper;
import com.aurora.tools.HanziToPinyin;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.OperationAction;
import com.aurora.tools.ToastUtils;
import com.aurora.tools.Util;
import com.aurora.tools.FileSortHelper.SortMethod;
import com.aurora.tools.OperationAction.Operation;
import com.aurora.widget.AuroraLoadAndEmptyView;
import com.aurora.widget.AuroraPicTwoAdapter;

/**
 * 图片分类浏览
 * 
 * @author jiangxh
 * @CreateTime 2014年4月24日 下午5:36:52
 * @Description com.aurora.filemanager.fragment PictureCategoryFragment.java
 */
public class PictureCategoryFragment extends AuroraFragment {
	private static final String TAG = "PictureCategoryFragment";
	private AuroraPicTwoAdapter auroraPicTwoAdapter;

	private ConcurrentHashMap<String, List<FileInfo>> categoryPiclistMapTemp = new ConcurrentHashMap<String, List<FileInfo>>();
	private ConcurrentHashMap<Integer, String> picFilePathTemp = new ConcurrentHashMap<Integer, String>();

	private ConcurrentHashMap<String, List<FileInfo>> categoryPiclistMap = new ConcurrentHashMap<String, List<FileInfo>>();
	private ConcurrentHashMap<Integer, String> picFilePath = new ConcurrentHashMap<Integer, String>();

	private List<FileInfo> picItemList;

	private List<String> deleteFileFolder = new ArrayList<String>();

	private static final int picInfos = 1001;
	private static final int LASTQUERY = 1002;

	private boolean isDestroy = false;

	/**
	 * @return the auroraPicTwoAdapter
	 */
	public AuroraPicTwoAdapter getAuroraPicTwoAdapter() {
		return auroraPicTwoAdapter;
	}

	private void editState() {
		// LogUtil.elog(TAG, "editState isDestroy==" + isDestroy);
		if (!isDestroy) {
			if (getGridView().getCount() == 0) {
				getFileExplorerActivity().showEmptyBarItem();// 改变actionBar显示
				getLoadAndEmptyView().getEmptyView()
						.setVisibility(View.VISIBLE);
			} else {
				if (getFileExplorerActivity().getIsFromWallpaperManager() != null
						&& getFileExplorerActivity()
								.getIsFromWallpaperManager()) {
					getFileExplorerActivity().showEmptyBarItem();// 从壁纸跳入
																	// 不让编辑

				} else {
					getFileExplorerActivity().showEditBarItem();// 改变actionBar显示
				}
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			}
		}

	}

	private Handler picHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case picInfos:
				if (PictureCategoryFragment.this.isDestroy) {// 如果这个fragment
//					LogUtil.elog(TAG, "isDestroy");
					return;
				}
				auroraPicTwoAdapter.notifyDataSetChanged();
				if (getFileExplorerActivity().isPicItemBack()) {// 是从ITEM 返回
					getGridView().setSelection(
							getFileExplorerActivity().getSelectPicItem());
				}
				editState();

				break;
			case LASTQUERY:
				if (isLast) {
					Cursor cursor = (Cursor) msg.obj;
					isLast = false;
					cursorAsyncTask.cancel(true);
					cursorAsyncTask = new CursorAsyncTask();
//					cursorAsyncTask.execute(cursor);
					cursorAsyncTask.executeOnExecutor(getFULL_TASK_EXECUTOR(), cursor);
				}
				break;

			default:
				break;
			}
			showLoadingImage(false);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_category_pic, container, false);
	}

	private boolean isCache = false;

	private boolean isonActivityCreated = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setGridView((GridView) getView().findViewById(R.id.picBrowerListView));
		super.onActivityCreated(savedInstanceState);
		isonActivityCreated = true;
		// 初始化2格Picture适配器
		auroraPicTwoAdapter = new AuroraPicTwoAdapter(categoryPiclistMap,
				picFilePath, getFileExplorerActivity(), this);
		getGridView().setAdapter(auroraPicTwoAdapter);
		// modify by JXH 2014-7-28 begin
		SoftReference<ConcurrentHashMap<String, List<FileInfo>>> cate = getFileExplorerActivity()
				.getCategoryPiclistMapCache();
		SoftReference<ConcurrentHashMap<Integer, String>> path = getFileExplorerActivity()
				.getPicFilePathCache();
		SoftReference<List<FileInfo>> cacheFileInfo = getFileExplorerActivity()
				.getCachePic();
		if (path != null && cate != null && cacheFileInfo != null) {
			List<FileInfo> tempInfos = cacheFileInfo.get();
			ConcurrentHashMap<Integer, String> tempPath = path.get();
			ConcurrentHashMap<String, List<FileInfo>> tempCate = cate.get();
			if (tempCate != null && tempInfos != null && tempPath != null) {
				fileInfos.clear();
				picFilePath.clear();
				categoryPiclistMap.clear();
				if (getFileExplorerActivity().isPrivacy()) {
					List<FileInfo> pList = getFileExplorerActivity()
							.getHashMap().get(FileCategory.Picture);
					if (pList != null) {
						String pathTemp = FileExplorerTabActivity
								.getPrivacyMedioPath(FileCategory.Picture);
					}
				}

				fileInfos.addAll(tempInfos);
				picFilePath.putAll(tempPath);
				categoryPiclistMap.putAll(tempCate);
				auroraPicTwoAdapter.setCategoryPiclistMap(tempCate);
				auroraPicTwoAdapter.setPicFilePath(tempPath);
				auroraPicTwoAdapter.notifyDataSetChanged();
				if (getFileExplorerActivity().isPicItemBack()) {// 是从ITEM 返回
					getGridView().setSelection(
							getFileExplorerActivity().getSelectPicItem());
				}
				showLoadingImage(false);
				editState();
				isCache = true;
			}
		}

		getCategoryHelper().setmCategory(FileCategory.Picture);
		if (!AuroraConfig.isOtherAppDel(getFileExplorerActivity())
				|| getFileExplorerActivity().isPicItemBack()) {
			refreshCategoryPics();
		}
		// modify by JXH 2014-7-28 end
		// 修改actionBar title
		getFileExplorerActivity().setAuroraActionBarTitle(
				getCategoryHelper().getCurCategoryNameResId());

		// 初始化、设置actionBar回调
		getAuroraActionBar().initActionBottomBarMenu(
				R.menu.aurora_operation_bar_menu, 1);

		// 多选图
		if (getFileExplorerActivity().getIsFromOtherAPP()
				&& getFileExplorerActivity().getIsGetMorePic()) {
			getAuroraActionBar().changeItemLayout(R.layout.aurora_more_pic,
					R.id.pic_more_liner);
			actionBarMorePicSetOnClickListener();
			// add by Jxh 2014-8-11 begin
			picPath = getFileExplorerActivity().getSelectPicPath();
			if (picPath == null) {
				picPath = new ArrayList<String>();
			}
			changeActionBarTextView();
		}
	}

	private boolean isBeforePri = false;

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);

		String temp = picFilePath.get(position);
		if (temp == null) {
			return;
		}
		if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
			return;
		}
		if (isOperationFile()) {
			if (getFileExplorerActivity().isPrivacy() && position == 0) {
				List<FileInfo> pList = getFileExplorerActivity().getHashMap()
						.get(FileCategory.Picture);
				if (pList != null) {
					return;
				}
			}
			operationPicture(position);
			return;
		}

		picItemList = categoryPiclistMap.get(temp);
		getFileExplorerActivity().setPicItemList(picItemList);
//		LogUtil.d(TAG, "-------temp:"+temp);
		temp=getFileExplorerActivity().getStoragePath(temp);
//		LogUtil.d(TAG, "-------temp:"+temp);
		getFileExplorerActivity().setPicPath(temp);
		getFileExplorerActivity().setSelectPicItem(
				getGridView().getFirstVisiblePosition());
		// 选择壁纸----废弃
		if (getFileExplorerActivity().getIsFromWallpaperManager() != null
				&& getFileExplorerActivity().getIsFromWallpaperManager()) {
			getFileExplorerActivity().startFromOtherApp();
			return;
		}

		getSelectPageInterface().selectPage(AuroraConfig.AURORA_PIC_ITEM_PAGE,
				null);

	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		// 从其他APP跳入图片
		if (getFileExplorerActivity().getIsFromOtherAPP()) {
			return true;
		}
		if (getFileExplorerActivity().isPrivacy() && position == 0) {
			List<FileInfo> pList = getFileExplorerActivity().getHashMap().get(
					FileCategory.Picture);
			if (pList != null) {
				return true;
			}
		}

		if (getOperation().isCutOrCopy()) {
			ToastUtils.showTast(getFileExplorerActivity(),
					R.string.wait_a_moment);
			return true;
		}
		String temp = picFilePath.get(position);
		if (temp == null) {
			return true;
		}
		if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
			return true;
		}
		if (!isOperationFile()) {
			setOperationFile(true);
			showDeleteMenu();
			deleteFileFolder.clear();
			deleteFileFolder.add(temp);
			updateAuroraItemBottomBarState();
		}
		return true;

	}

	@Override
	public void onResume() {
		super.onResume();
//		LogUtil.log(TAG, "getFileExplorerActivity().isPrivacy()"
//				+ getFileExplorerActivity().isPrivacy());
		// add by Jxh 2014-9-4 begin修复 从相册进入文官图片分类图片未刷新问题
		if (AuroraConfig.isOtherAppDel(getFileExplorerActivity())
				&& !getFileExplorerActivity().isPicItemBack()) {
//			LogUtil.log(TAG, "isOtherAppDel");
			refreshCategoryPics();
			AuroraConfig.setOtherAppDel(getFileExplorerActivity(), false);
		} else if (getFileExplorerActivity().isPrivacy()) {
//			LogUtil.log(TAG, "refreshCategoryPics");
			refreshCategoryPics();
		} else if (!getFileExplorerActivity().isPrivacy() && isBeforePri) {// 从隐私帐号切换为非隐私帐号数据刷新
//			LogUtil.log(TAG, "refreshCategoryPics222");
			refreshCategoryPics();
		}
		// add by Jxh 2014-9-4 end

	}

	@Override
	public void onDestroy() {
		isDestroy = true;
		super.onDestroy();
	}

	// add by JXH 2014-8-18 多图选择改变ActionBar显示 begin
	private ArrayList<String> picPath = new ArrayList<String>();

	private LinearLayout layout;

	/**
	 * 选择多张图片时 actionBar右边按钮监听
	 */
	private void actionBarMorePicSetOnClickListener() {
		if (layout == null) {
			layout = (LinearLayout) getAuroraActionBar().getRootView()
					.findViewById(R.id.pic_more_liner);
		}
		if (layout == null) {
			return;
		}
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// add by JXH 2014-7-22 begin BUG
				ButtonUtil buttonUtil = new ButtonUtil();
				if (buttonUtil.isFastClick(600)) {// 快速点击
					return;
				}

				// add by JXH 2014-7-22 end
				if (picPath != null && picPath.size() > 0) {
					IntentBuilder
							.getMorePic(getFileExplorerActivity(), picPath);
				}
			}
		});
		layout.setClickable(false);
	}

	private TextView textView;

	private void changeActionBarTextView() {
		if (textView == null) {
			textView = (TextView) getAuroraActionBar().getRootView()
					.findViewById(R.id.pic_more);
		}

		if (picPath != null && picPath.size() > 0) {
			textView.setText(String.format(
					getFileExplorerActivity().getString(
							R.string.have_select_pic), picPath.size()));
			textView.setTextAppearance(getFileExplorerActivity(),
					R.style.more_pic_select);
			if (layout != null) {
				layout.setClickable(true);
			}
		} else {
			if (layout != null) {
				layout.setClickable(false);
			}
		}
		// textView.setTypeface(Util.auroraCreateTitleFont());
	}

	// add by JXH 2014-8-18 多图选择改变ActionBar显示 end

	/**
	 * 初始化 刷新数据
	 */
	public void refreshCategoryPics() {
		// 初始化FileCategoryHelper 查询Picture数据
		if (isAdded()) {
			getCategoryHelper().query(FileCategory.Picture, SortMethod.date);
		}
	}

	/**
	 * 点击activity中 actionBar 编辑按钮
	 */
	public void editShowView() {
		if (isAdded()) {
			if (getOperation().isCutOrCopy()) {
				ToastUtils.showTast(getFileExplorerActivity(),
						R.string.wait_a_moment);
				return;
			}
			if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
				return;
			}
			setOperationFile(true);
			showDeleteMenu();
			deleteFileFolder.clear();
			updateAuroraItemBottomBarState();
			updateAuroraitemActionBarState();//paul add for BUG #15035
		}
	}

	@Override
	public void onLeftClick() {
		super.onLeftClick();
		unDoOperation();
	}

	@Override
	public void onRightClick() {
		super.onRightClick();
		int size = picFilePath.size();
		if (getFileExplorerActivity().isPrivacy()
				&& picFilePath.contains(FileExplorerTabActivity
						.getPrivacyMedioPath(FileCategory.Picture))) {
			size -= 1;
		}
		if (deleteFileFolder.size() == size) {
			deleteFileFolder.clear();
		} else {
			deleteFileFolder.clear();
			deleteFileFolder.addAll(picFilePath.values());
			if (getFileExplorerActivity().isPrivacy()) {
				deleteFileFolder.remove(FileExplorerTabActivity
						.getPrivacyMedioPath(FileCategory.Picture));
			}
		}
		updateAuroraItemActionBarStatePic();
		updateAuroraItemBottomBarState();
	}

	public int selectPosition = -1;

	/**
	 * 选择图片文件夹
	 * 
	 * @param position
	 */
	private void operationPicture(int position) {
		// auroraPicTwoAdapter.selectCheckBox(position);
		setItemPicAnim(true);
		selectPosition = position;
		String temp = picFilePath.get(position);
		if (deleteFileFolder.contains(temp)) {
			deleteFileFolder.remove(temp);
		} else {
			deleteFileFolder.add(picFilePath.get(position));
		}
		updateAuroraItemActionBarStatePic();
		updateAuroraItemBottomBarState();
	}

	/**
	 * 改变actionBar 文字显示
	 */
	private void updateAuroraItemActionBarStatePic() {
		int size = picFilePath.size();
		if (getFileExplorerActivity().isPrivacy()
				&& picFilePath.contains(FileExplorerTabActivity
						.getPrivacyMedioPath(FileCategory.Picture))) {
			size -= 1;
		}
		if (deleteFileFolder.size() == size && getRightView() != null) {
			getRightView().setText(
					getResources().getString(R.string.invert_select));
		} else if (getRightView() != null) {
			getRightView().setText(
					getResources().getString(R.string.all_select));
		}
	}

	/**
	 * 改变BottomBar 状态
	 */
	public void updateAuroraItemBottomBarState() {
//		LogUtil.log(TAG,
//				"updateAuroraItemBottomBarState deleteFileFolder.size()=="
//						+ deleteFileFolder.size());
		if (deleteFileFolder.size() == 0) {
			getAuroraActionBar().getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(1, false);
		} else {
			getAuroraActionBar().getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(1, true);
		}
		auroraPicTwoAdapter.setSelects(deleteFileFolder);
		auroraPicTwoAdapter.notifyDataSetChanged();
		if (selectFiles == null) {
			selectFiles = new ArrayList<FileInfo>();
		} else {
			selectFiles.clear();
		}
		for (String path : deleteFileFolder) {
			selectFiles.addAll(categoryPiclistMap.get(path));
		}
		getOperation().setDeleteFolderCount(deleteFileFolder.size());
		getOperation().setSelectFiles(selectFiles);

	}

	private boolean isItemPicAnim;

	public boolean isItemPicAnim() {
		return isItemPicAnim;
	}

	public void setItemPicAnim(boolean isItemPicAnim) {
		this.isItemPicAnim = isItemPicAnim;
	}

	/**
	 * 弹出底部垃圾桶
	 */
	private void showDeleteMenu() {
		setItemPicAnim(false);
		getAuroraActionBar().setShowBottomBarMenu(true);
		getAuroraActionBar().showActionBarDashBoard();
	}

	/**
	 * 隐藏底部垃圾桶
	 */
	private void hideDeleteMenu() {
		getAuroraActionBar().setShowBottomBarMenu(false);
		getAuroraActionBar().showActionBarDashBoard();
	}

	private boolean isCancleTask = false;// 标志线程十分被打断 取消当前任务

	@Override
	public boolean onFragmentBack() {
		// LogUtil.elog(TAG, "onBack() 2");
		if (unDoOperation()) {// 退出编辑状态
			return true;
		}

		getCategoryHelper().cancelQueryEvent(
				FileCategoryHelper.FILE_CATEGORY_DATAS_TOKEN);// 取消没有开始到查询任务
		// LogUtil.log(TAG, "cancel FILE_CATEGORY_DATAS_TOKEN");
		isCancleTask = true;
		if (cursorAsyncTask != null) {
			cursorAsyncTask.cancel(true);
		}
		// 返回相册 壁纸
		if (getFileExplorerActivity().getIsFromPicManager() != null
				&& getFileExplorerActivity().getIsFromPicManager()
				|| (getFileExplorerActivity().getIsFromWallpaperManager() != null && getFileExplorerActivity()
						.getIsFromWallpaperManager())
				|| getFileExplorerActivity().getIsFromOtherAPP() != null
				&& getFileExplorerActivity().getIsFromOtherAPP()) {
			getFileExplorerActivity().finish();
			return true;
		}
		// 图片别名activity返回
		if (getFileExplorerActivity().getActivityInfo().name
				.endsWith(getString(R.string.activity_picture))) {
			getFileExplorerActivity().finish();
			return true;
		}
		getFileExplorerActivity().setPicItemBack(false);
		getSelectPageInterface()
				.selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
		return true;
	}

	/**
	 * 退出编辑状态
	 * 
	 * @return
	 */
	public boolean unDoOperation() {
		if (isAdded() && isOperationFile()) {// 退出编辑状态
			setOperationFile(false);
			hideDeleteMenu();
			deleteFileFolder.clear();
			auroraPicTwoAdapter.notifyDataSetChanged();// 还原图片选择状态
			return true;
		}
		return false;
	}

	private boolean isLast = false;

	@Override
	public void onFileListQueryComplete(Cursor cursor) {
//		LogUtil.d(TAG, "onFileListQueryComplete");
		if (isCache || !isonActivityCreated) {
			showLoadingImage(false);
		} else {
			isonActivityCreated = false;
			showLoadingImage(true);
		}
		if (cursor == null) {
			getFileExplorerActivity().clearCache();
			Message message = picHandler.obtainMessage();
			message.what = picInfos;
			picHandler.sendMessage(message);
			return;
		}
		if (cursorAsyncTask != null
				&& cursorAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
			// cursorAsyncTask.cancel(true);
			isLast = true;
			picHandler.removeMessages(LASTQUERY);
			Message msg = picHandler.obtainMessage();
			msg.what = LASTQUERY;
			msg.obj = cursor;
			picHandler.sendMessageDelayed(msg, 200);
			return;

		}
		isLast = false;
		cursorAsyncTask = new CursorAsyncTask();
//		cursorAsyncTask.execute(cursor);
		cursorAsyncTask.executeOnExecutor(getFULL_TASK_EXECUTOR(), cursor);

	}

	private CursorAsyncTask cursorAsyncTask;

	private class CursorAsyncTask extends AsyncTask<Cursor, Void, Void> {

		protected List<FileInfo> fileInfosTemp = new ArrayList<FileInfo>();

		@Override
		protected Void doInBackground(Cursor... params) {
			Cursor cursor = params[0];
			fileInfosTemp.clear();
			if (cursor != null && !cursor.isClosed()) {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					if (isCancleTask || isCancelled()) {
						isCancleTask = false;
						getFileExplorerActivity().clearCache();
						return null;
					}

					FileInfo fileInfo = Util.getFileInfo(cursor,
							FileCategory.Picture);
					if (fileInfo != null) {
						if (fileInfo.filePath != null
								&& !fileInfo.filePath.equals(""))
							fileInfosTemp.add(fileInfo);
					} else {// 文件系统不存在这条数据 但是数据库存在
						long id = cursor.getLong(cursor
								.getColumnIndex(FileColumns._ID));
						long ids[] = new long[] { id };
						getCategoryHelper().delete(ids);// 删除这条记录
						LogUtil.e(TAG, " delete db id::"+id);
					}
				}
				cursor.close();
			}

			 //LogUtil.e(TAG, "doInBackground fileInfos.size()==" + fileInfos.size());
			try {
//				synchronized (lockSort) {
					sortPicInfos(fileInfosTemp);
//				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// LogUtil.log(TAG,
			// "sort end time==" + SystemClock.currentThreadTimeMillis());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isCancelled() || isCancleTask) {
				getFileExplorerActivity().clearCache();
				return;
			}
			// add by JXH 2014-7-28 添加图片缓存 begin
			SoftReference<ConcurrentHashMap<String, List<FileInfo>>> cate = new SoftReference<ConcurrentHashMap<String, List<FileInfo>>>(
					categoryPiclistMap);
			getFileExplorerActivity().setCategoryPiclistMapCache(cate);
			SoftReference<ConcurrentHashMap<Integer, String>> path = new SoftReference<ConcurrentHashMap<Integer, String>>(
					picFilePath);
			getFileExplorerActivity().setPicFilePathCache(path);
			SoftReference<List<FileInfo>> cachePic = new SoftReference<List<FileInfo>>(
					fileInfos);
			getFileExplorerActivity().setCachePic(cachePic);
			// add by JXH 2014-7-28 添加图片缓存 end
			// LogUtil.elog(TAG, "fileInfos.size()==" + fileInfos.size());
			Message message = picHandler.obtainMessage();
			message.what = picInfos;
			picHandler.sendMessage(message);
		}
	}

	private List<String> cameraPaths = new ArrayList<String>();

	private Object object = new Object();
	private boolean unLock = false;

	public void obNotifyAll() {
//		LogUtil.log(TAG, "obNotifyAll");
		if (isAdded()) {
			synchronized (object) {
				object.notifyAll();
				unLock = true;
			}
		}
	}

	private final Object lockSort = new Object();
	private List<FileInfo> priFileInfos;

	/**
	 * 图片数据处理(截取与排序)
	 */
	private synchronized void sortPicInfos(List<FileInfo> fileInfosTemp)
			throws Exception {
		if (fileInfosTemp == null) {
			return;
		}
		categoryPiclistMapTemp.clear();
		picFilePathTemp.clear();
		int index = 0;
		 LogUtil.e(TAG, "sortPicInfos fileInfos==" + fileInfos.size());
		for (FileInfo fileInfo : fileInfosTemp) {
			if (fileInfo != null) {
				String path = fileInfo.filePath;
				if (path != null && fileInfo.fileName != null) {
					// LogUtil.elog(TAG, "path==" + fileInfo.filePath);
					String filePath = Util.getPathFromFilepath(path);
					// LogUtil.elog(TAG, "filePath==" + filePath);
					if (filePath != null) {
						if (TextUtils.isEmpty(FileExplorerTabActivity
								.getPrivacyHomePath())
								|| !filePath.startsWith(FileExplorerTabActivity
										.getPrivacyHomePath())) {
							// modify by JXH 2014-7-30 begin
							if (getFileExplorerActivity().getStoragesStrings()
									.contains(filePath)) {
								filePath = getFileExplorerActivity()
										.getStorageName(filePath);
							}
							// modify by JXH 2014-7-30 end
							if (!picFilePathTemp.contains(filePath)) {
								if (filePath.endsWith(AuroraConfig.CameraPath)) {
									if (!cameraPaths.contains(filePath)) {
										cameraPaths.add(filePath);
									}
								} else {
									picFilePathTemp.put(index, filePath);
									index++;
								}
							}
							if (categoryPiclistMapTemp.containsKey(filePath)) {// 防止重复添加fileinfo
								if (!categoryPiclistMapTemp.get(filePath)
										.contains(fileInfo)) {
									categoryPiclistMapTemp.get(filePath).add(
											fileInfo);
								}
							} else {
								List<FileInfo> tempFileInfos = new ArrayList<FileInfo>();
								tempFileInfos.add(fileInfo);
								categoryPiclistMapTemp.put(filePath,
										tempFileInfos);
							}
						}
					}
				}
			}
		}

		if (fileInfosTemp.size() > 0) {
			@SuppressWarnings("rawtypes")
			Map.Entry[] entries = new Map.Entry[picFilePathTemp.size()];
			entries = getSortedHashtableByValue(picFilePathTemp);
			int size = cameraPaths.size();
			for (int i = 0; i < size; i++) {
				picFilePathTemp.put(i, cameraPaths.get(i));
			}
			cameraPaths.clear();
			for (int i = 0; i < entries.length; i++) {
				picFilePathTemp.put(size + i, entries[i].getValue().toString());
			}

		}

		// add by Jxh 隐私图片添加 begin
		if (getFileExplorerActivity().isPrivacy()
				&& !getFileExplorerActivity().getIsGetMoreNoPrivacy()
				&& !getFileExplorerActivity().getIsFromOtherAPP()) {
			getFileExplorerActivity().clearCache();
			List<FileInfo> pList = getFileExplorerActivity().getHashMap().get(
					FileCategory.Picture);
			if (pList == null) {
				synchronized (object) {
					if (!unLock) {
						LogUtil.d(TAG, "object.wait()");
						try {
							object.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
						unLock = false;
					}

				}
				LogUtil.d(TAG, "object.wait() end");
				pList = getFileExplorerActivity().getHashMap().get(
						FileCategory.Picture);
				priFileInfos = pList;
			}
			if (pList != null) {
				isBeforePri = true;
				String path = FileExplorerTabActivity
						.getPrivacyMedioPath(FileCategory.Picture);
				if (picFilePathTemp.size() > 0) {
					ConcurrentHashMap<Integer, String> temp = new ConcurrentHashMap<Integer, String>();
					temp.putAll(picFilePathTemp);
					picFilePathTemp.clear();
					picFilePathTemp.put(0, path);
					for (int i = 0; i < temp.size(); i++) {
						picFilePathTemp.put(i + 1, temp.get(i));
					}
				} else {
					picFilePathTemp.put(0, path);
				}
				categoryPiclistMapTemp.put(path, pList);
				// fileInfos.addAll(pList);
			}
		}
		// add by Jxh 隐私图片添加 end
		picFilePath.clear();
		categoryPiclistMap.clear();

		if (picFilePathTemp.size() != 0) {
			picFilePath.putAll(picFilePathTemp);
		}
		if (categoryPiclistMapTemp.size() != 0) {
			categoryPiclistMap.putAll(categoryPiclistMapTemp);
		}
		auroraPicTwoAdapter.setCategoryPiclistMap(categoryPiclistMap);
		auroraPicTwoAdapter.setPicFilePath(picFilePath);
		categoryPiclistMapTemp.clear();
		picFilePathTemp.clear();
		fileInfos.clear();
		fileInfos.addAll(fileInfosTemp);
		fileInfosTemp.clear();

	}

	/**
	 * @param h
	 * @return 实现对map按照value升序排序
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized Map.Entry[] getSortedHashtableByValue(Map h) {
		Set set = h.entrySet();
		Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set
				.size()]);
		Arrays.sort(entries, new Comparator() {

			@Override
			public int compare(Object lhs, Object rhs) {
				Collator comparator = Collator
						.getInstance(java.util.Locale.CHINA);
				String key1 = ((Map.Entry) lhs).getValue().toString();
				String key2 = ((Map.Entry) rhs).getValue().toString();
				key1 = key1.substring(key1.lastIndexOf("/") + 1);
				key2 = key2.substring(key2.lastIndexOf("/") + 1);
				// return key1.compareTo(key2);
				return comparator.compare(key1, key2);
			}

		});

		return entries;
	}

	private Set<String> setTemp;

	@Override
	public void deleteComplete(List<FileInfo> ids) {
		getFileExplorerActivity().clearCache();
		if (ids != null && ids.size() == 0) {
			LogUtil.e(TAG, "del file size is 0");
		}
		setTemp = new HashSet<String>();
		for (FileInfo fileInfo : ids) {
			String path = fileInfo.filePath.substring(0,
					fileInfo.filePath.lastIndexOf("/"));
			setTemp.add(path);
			// LogUtil.log(TAG, "del==" + path);
		}
		if (picFilePathTemp == null) {
			picFilePathTemp = new ConcurrentHashMap<Integer, String>();
		} else {
			picFilePathTemp.clear();
		}
		if (categoryPiclistMapTemp == null) {
			categoryPiclistMapTemp = new ConcurrentHashMap<String, List<FileInfo>>();
		} else {
			categoryPiclistMapTemp.clear();
		}
		picFilePathTemp.putAll(picFilePath);
		categoryPiclistMapTemp.putAll(categoryPiclistMap);

		picFilePath.clear();
		categoryPiclistMap.clear();
		int k = 0;
		for (int i = 0; i < picFilePathTemp.size(); i++) {
			// LogUtil.log(TAG, "pic =="+picFilePathTemp.get(i));
			String path = getFileExplorerActivity().getStoragePath(
					picFilePathTemp.get(i));
			if (!setTemp.contains(path)) {
				picFilePath.put(k, picFilePathTemp.get(i));
				categoryPiclistMap.put(picFilePathTemp.get(i),
						categoryPiclistMapTemp.get(picFilePathTemp.get(i)));
				k++;
			}
		}
		unDoOperation();
		getOperation().mDialogDismiss();
		editState();
	}

}
