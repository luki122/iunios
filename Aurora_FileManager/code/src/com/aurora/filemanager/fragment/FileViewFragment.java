package com.aurora.filemanager.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ProgressBar;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraAnimationImageView;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.tools.AuroraFilenameFilter;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileIconHelper;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileSortHelper;
import com.aurora.tools.HanziToPinyin;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.LruMemoryCacheByInteger;
import com.aurora.tools.OperationAction;
import com.aurora.tools.ToastUtils;
import com.aurora.tools.Util;
import com.aurora.tools.FileSortHelper.SortMethod;
import com.aurora.tools.OperationAction.InformationDialogDismissLisenter;
import com.aurora.tools.OperationAction.Operation;
import com.aurora.widget.AuroraFileBrowserAdater;
import com.aurora.widget.AuroraFilesAdapter;
import com.aurora.widget.AuroraFilesItemView;
import com.aurora.widget.AuroraLoadAndEmptyView;
import com.aurora.widget.AuroraOperationBarMoreMenu;
import com.aurora.filemanager.R;
import com.aurora.lazyloader.PriorityThreadFactory;

/**
 * SD卡浏览
 * 
 * @author jiangxh
 * @CreateTime 2014年4月30日 下午3:51:02
 * @Description com.aurora.filemanager.fragment FileViewFragment.java
 */
public class FileViewFragment extends AuroraFragment implements
		InformationDialogDismissLisenter {
	private static final String TAG = "FileViewFragment";

	private AuroraFileBrowserAdater auroraFileBrowserAdater;
	private FileSortHelper fileSortHelper = new FileSortHelper();
	private String nowPath;

	private LruMemoryCacheByInteger cacheByInteger;

	/**
	 * @return the nowPath
	 */
	public String getNowPath() {
		if (TextUtils.isEmpty(nowPath)) {
			return "";
		}
		return nowPath;
	}

	/**
	 * @param nowPath
	 *            the nowPath to set
	 */
	public void setNowPath(String nowPath) {
		this.nowPath = nowPath;
		getFileExplorerActivity().showEmptyBarItem();
		String navTitle = getFileExplorerActivity().getStorageName(nowPath);
		/********************** 量产版本添加 ***********************************/
		if (FileExplorerTabActivity.mSDCardPath != null
				&& getFileExplorerActivity().getIsFromAudioManager() != null
				&& getFileExplorerActivity().getIsFromAudioManager()) {
			String audio = FileExplorerTabActivity.mSDCardPath
					+ File.separator
					+ getFileExplorerActivity()
							.getString(R.string.phone_audios);
			if (audio != null && nowPath != null && nowPath.equals(audio)
					&& !isEnglish()) {
				navTitle = navTitle.replace(
						getFileExplorerActivity().getString(
								R.string.phone_audios),
						getFileExplorerActivity().getString(
								R.string.phone_audio));

			}
		}
		/********************** 量产版本添加 ***********************************/
		String title = Util.getNameFromFilepath(navTitle);
		if (title == null || title.trim().equals("")) {
			getFileExplorerActivity().setAuroraActionBarTitle(navTitle);
		} else {
			getFileExplorerActivity().setAuroraActionBarTitle(title);
		}
		getFileExplorerActivity().setAuroraObserver(nowPath);
		navigationShow(navTitle);
	}

	/**
	 * 判断是否是英文
	 * 
	 * @return
	 */
	private boolean isEnglish() {
		if (!isAdded()) {
			return false;
		}
		return getResources().getConfiguration().locale.getCountry().equals(
				"US")
				|| getResources().getConfiguration().locale.getCountry()
						.equals("UK");
	}

	/**
	 * 显示空数据 和Actionbar
	 */
	private void updateListViewState() {

		// modiy by jxh 2014-6-17 BUG 5872 begin
		if (moving_operation_bar != null
				&& moving_operation_bar.getVisibility() == View.VISIBLE) {
			getFileExplorerActivity().showEmptyBarItem();
			getFileExplorerActivity().setMenuEnable(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
		} else {
			getFileExplorerActivity().showEditBarItem();// 改变actionBar显示
			getFileExplorerActivity().setMenuEnable(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
		}
		// modiy by jxh 2014-6-17 BUG 5872 end
		if (getAuroraListView().getCount() == 0) {
			getFileExplorerActivity().showEmptyBarItem();// 改变actionBar显示
			getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
			// add by jxh BUG 5435 begin
			getFileExplorerActivity().setMenuEnable(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			// add by jxh BUG 5435 end
		} else {
			getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
		}
		if (getFileExplorerActivity().isGetFileFromSdList()) {
			getFileExplorerActivity().setMenuEnable(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
		}
	}

	private static final int SHOWLOAD = 1001;
	private static final int REFRESH = 1000;
	// private static final int SELECTPOSITION = 1002;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (isDestroy) {
				return;
			}
			auroraSetRubbishBack();
			switch (msg.what) {
			case SHOWLOAD:
				showLoadingImage(true);
				break;
			// add by Jxh 2014-8-11 begin
			// case SELECTPOSITION:
			// setListViewPosition();
			// break;
			// add by Jxh 2014-8-11 end
			default:
				break;
			}

		}

	};

	private void refreshListView(boolean setLastLocation) {
		auroraFileBrowserAdater.setFileInfos(fileInfos);
		auroraFileBrowserAdater.notifyDataSetChanged();
		if (isItemClick) {
			getAuroraListView().setSelection(0);
			isItemClick = false;
		} else if (setLastLocation) {
			setLastListViewLocation();
		}

		if (stopPosition != -1) {
			getAuroraListView().setSelection(stopPosition);
			stopPosition = -1;
		}
		// add by jxh 2014-8-26 begin 设置跳转界面 显示位置
		if (goPosition != 0) {
			getAuroraListView().setSelection(goPosition);
			goPosition = 0;
		}
		// add by jxh 2014-8-26 end 设置跳转界面 显示位置
		showLoadingImage(false);
		updateListViewState();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_view_files, container, false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setAuroraListView((AuroraListView) getView().findViewById(
				R.id.files_listView));
		super.onActivityCreated(savedInstanceState);
		showLoadingImage(false);
		if (getOperation() != null) {
			getOperation().setDismissLisenter(this);
		}

		// 初始化Adapter
		auroraFileBrowserAdater = new AuroraFileBrowserAdater(
				new ArrayList<FileInfo>(), getFileExplorerActivity(), this);
		getAuroraListView().setAdapter(auroraFileBrowserAdater);
		getAuroraListView().auroraSetNeedSlideDelete(true);

		/*************************** 添加从列表获取文件方法 开始 2014-10-22 by JXH **************************************/
		if (getFileExplorerActivity().isGetFileFromSdList()) {
			getAuroraListView().auroraSetNeedSlideDelete(false);
		}
		/*************************** 添加从列表获取文件方法 结束 2014-10-22 by JXH **************************************/

		activityInfo = getFileExplorerActivity().getActivityInfo();

		if (getNowPath() == null || getNowPath().equals("")) {
			String rootPath = getFileExplorerActivity().getSearchPath();// 获取搜索界面跳转路径
			if (rootPath == null || rootPath.equals("")) {// 不是搜索路径跳转
				rootPath = getFileExplorerActivity().getGoPath();
				if (rootPath == null || rootPath.equals("")) {// 不是路径跳转
					rootPath = getFileExplorerActivity().getRootPath();
				}
			}
			if (getFileExplorerActivity().isShowOperationPane) {// 如果是复制和剪切状态
				rootPath = FileExplorerTabActivity.ROOT_PATH;
				getFileExplorerActivity().isShowOperationPane = false;
			}
			setNowPath(rootPath);
			if (!getRootListStorage(rootPath, false)) {// 如果不是根目录则进行查询
				showPathListByThread(false);
			}
		}
		getAuroraActionBar().initActionBottomBarMenu(
				R.menu.aurora_operation_bar_more_menu, 5);

	}

	@Override
	public void auroraDelOnClick(int position) {
		super.auroraDelOnClick(position);
		FileInfo fileInfo = fileInfos.get(position);
		if (getOperation().fastDeleteFileOrDir(fileInfo)) {
			getAuroraListView().auroraDeleteSelectedItemAnim();
			setStatistics(11);
		} else if (!fileInfo.IsDir) {
			ToastUtils.showTast(
					getFileExplorerActivity(),
					String.format(
							getFileExplorerActivity().getString(
									R.string.del_file), fileInfo.fileName));
		}
	}

	@Override
	public void auroraDeleteItemView(View view, int position) {
		super.auroraDeleteItemView(view, position);
		auroraSetRubbishBack();
		try {
			fileInfos.remove(position);
			auroraFileBrowserAdater.setFileInfos(fileInfos);
			auroraFileBrowserAdater.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateListViewState();
		OperationAction.setLastOperation(Operation.noOperation);

	}

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);
		if (getFileExplorerActivity().showOperationBarMoreMenu) {
			getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
			updateAuroraItemBottomBarState();
			return;
		}

		if (fileInfos == null || fileInfos.size() == 0) {
			LogUtil.e(TAG, "fileInfos is null or 0");
			return;
		}
		FileInfo fileInfo = fileInfos.get(position);
		if (fileInfo == null || fileInfo.filePath == null) {
			return;
		}
		auroraFileBrowserAdater.setRoot(false);
		// 如果文件被其他方式删除 刷新列表
		File file = new File(fileInfo.filePath);
		if (!file.exists()) {
			clearDataAndView();
			showPathListByThread(false);
			return;
		}

		if (isOperationFile()) {
			operationAtion(position, view, fileInfo, true);
			return;
		}
		if (fileInfo.IsDir) {
			backSd = false;
			getAuroraListView().setLongClickable(true);
			setButtonMovingEnabled(false);
			setNowPath(fileInfo.filePath);
			for (String sdRoot : getFileExplorerActivity().getStoragesStrings()) {
				if (fileInfo.filePath.startsWith(sdRoot)) {
					getFileExplorerActivity().setRootPath(sdRoot);
					break;
				}
			}

			clearDataAndView();
			// 保存当前listView位置
			enqueueLinke(getAuroraListView().getFirstVisiblePosition());
			isItemClick = true;
			showPathListByThread(false);
			return;
		}

		// 获取文件action，item选择
		if (getFileExplorerActivity().isGetFileFromSdList() && !fileInfo.IsDir) {
			Uri uri = Uri.fromFile(file);
			IntentBuilder.getSinglePic(getFileExplorerActivity(), uri);
			return;
		}
		IntentBuilder.viewFile(getFileExplorerActivity(), fileInfo.filePath);
	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		// 获取文件action 长按事件无效
		if (getFileExplorerActivity().isGetFileFromSdList()) {
			return true;
		}
		if (moving_operation_bar != null
				&& moving_operation_bar.getVisibility() == View.VISIBLE) {
			return true;
		}

		if (auroraFileBrowserAdater.isRoot()) {
			return true;
		}
		if (getFileExplorerActivity().showOperationBarMoreMenu) {
			getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
			updateAuroraItemBottomBarState();
			return true;
		}
		if (fileInfos == null || fileInfos.size() == 0) {
			LogUtil.e(TAG, "fileInfos is null or 0");
			return true;
		}
		FileInfo fileInfo = fileInfos.get(position);
		if (fileInfo == null || TextUtils.isEmpty(fileInfo.filePath)) {
			LogUtil.e(TAG, "fileInfo is null or fileInfo.filePath is null");
			return true;
		}
		// 如果文件被其他方式删除 刷新列表
		File file = new File(fileInfo.filePath);
		if (!file.exists()) {
			clearDataAndView();
			showPathListByThread(false);
			return true;
		}

		if (getOperation().isCutOrCopy()) {
			ToastUtils.showTast(getFileExplorerActivity(),
					R.string.wait_a_moment);
			return true;
		}

		if (!isOperationFile()) {
			setOperationFile(true);
			setStatistics(12);
			getAuroraListView().auroraEnableSelector(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
			operationAtion(position, view, fileInfo, false);
			if (isLastItemMove) {
				// add By jxh 2014-8-27 begin
				if (position == getAuroraListView().getLastVisiblePosition()) {
					isLastitem = true;
				}
				lastBottom = view.getBottom();
				// add By jxh 2014-8-27 end
			}
			showEditView();
			auroraFileBrowserAdater.isShowAnim();
		}
		return true;
	}

	@Override
	public void auroraOnScrollStateChanged(AbsListView view, int scrollState) {
		super.auroraOnScrollStateChanged(view, scrollState);
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			auroraFileBrowserAdater.setLoadIcon(false);
		} else {
			auroraFileBrowserAdater.setLoadIcon(true);
		}
	}

	private boolean isItemClick;

	@Override
	public void onPause() {
		super.onPause();
		try {
			auroraFileBrowserAdater.getFileIconHelper().pause();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// add by JXH 2014-7-10 begin
	/**
	 * 返回是否为根路径
	 * 
	 * @return
	 */
	public boolean getIsRoot() {
		if (isAdded()) {
			return auroraFileBrowserAdater.isRoot();
		}
		return false;
	}

	// add by JXH 2014-7-10 end

	/**
	 * 文件观察者刷新列表数据
	 */
	private int stopPosition = -1;

	public void refreshListData() {
		if (isAdded()) {
			stopPosition = getAuroraListView().getFirstVisiblePosition();
			showPathListByThread(false);
		}
	}

	private boolean isDestroy = false;

	@Override
	public void onDestroy() {
		isDestroy = true;
		LogUtil.d(TAG, "--------------------onDestroy");
		getFileExplorerActivity().setAuroraObserver(null);
		if (auroraFileBrowserAdater != null) {
			auroraFileBrowserAdater = null;
		}
		if (listQueue != null) {
			clearQueue();
			listQueue = null;
		}
		if (pathTask != null) {
			pathTask.cancel(true);
			pathTask = null;
		}
		super.onDestroy();
	}

	@Override
	public void deleteComplete(List<FileInfo> ids) {

		// showLoadingImage(true);
		if (cacheByInteger == null) {
			cacheByInteger = LruMemoryCacheByInteger.getInstance();
		}
		for (FileInfo fileInfo : ids) {
			fileInfos.remove(fileInfo);
			cacheByInteger.removeMemoryCache(fileInfo.filePath);
		}
		// LogUtil.log(TAG, "deleteComplete");
		getOperation().mDialogDismiss();
		refreshListView(false);
		delUnAllOperation();
	}

	@Override
	public void folderDelet(FileInfo fileInfo) {
		if (fileInfo != null) {
			getAuroraListView().auroraDeleteSelectedItemAnim();
		} else {
			getAuroraListView().auroraSetRubbishBack();
		}
		getOperation().mDialogDismiss();
	}

	// add by Jxh 2014-8-11 begin
	private int selectPostion = 0;

	public int getSelectPostion() {
		return selectPostion;
	}

	public void setSelectPostion(int selectPostion) {
		this.selectPostion = selectPostion;
	}

	// add by Jxh 2014-8-11 end

	@Override
	public void renameComplete(FileInfo old, final FileInfo newFileInfo) {
		fileInfos.remove(old);
		fileInfos.add(newFileInfo);
		fileSortHelper.setSortMethog(SortMethod.name);
		Collections.sort(fileInfos, fileSortHelper.getComparator());// 排序
		refreshState();

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				int i = fileInfos.indexOf(newFileInfo);
				// LogUtil.log(TAG, "selectPostion set==" + i);
				if (i != -1) {
					setSelectPostion(i);
				}
				auroraFileBrowserAdater.notifyDataSetChanged();
				setListViewPosition();
				// add by Jxh 2014-8-11 end
			}
		}, 300);

	}

	// add by Jxh 2014-8-11 begin
	private void setListViewPosition() {
		if (getAuroraListView() != null) {
			// LogUtil.log(TAG, "selectPostion get==" + getSelectPostion());
			getAuroraListView().setSelection(getSelectPostion());
			setSelectPostion(0);
		}
	}

	// add by Jxh 2014-8-11 end
	@Override
	public void completeRefresh(List<FileInfo> addFileInfos,
			List<FileInfo> removeInfos) {// 复制 剪切完成刷新数据
		// BUG #12643
		if (!(getFileExplorerActivity().getCurrentFragment() instanceof FileViewFragment)) {
			return;
		}
		LogUtil.d(TAG, "------------completeRefresh");
		auroraFileBrowserAdater.clearSelectFile();
		getAuroraListView().auroraSetNeedSlideDelete(true);// 在根目录时 设置了false
		getOperation().mDialogDismiss();
		if (addFileInfos != null && addFileInfos.size() > 0) {
			fileInfos.removeAll(addFileInfos);// BUG #12502 BUG #12602
			fileInfos.addAll(addFileInfos);
			fileSortHelper.setSortMethog(SortMethod.name);
			Collections.sort(fileInfos, fileSortHelper.getComparator());// 排序
			auroraFileBrowserAdater.notifyDataSetChanged();
			if (getOperation() != null && getOperation().isCreateFolder()) {
				// add by Jxh 2014-8-11 begin
				int i = fileInfos.indexOf(addFileInfos.get(0));
				if (i != -1) {
					setSelectPostion(i);
				}
				getOperation().setCreateFolder(false);
			}
			setListViewPosition();
			addFileInfos.clear();
			addFileInfos = null;
			// add by Jxh 2014-8-11 end
		}
		if (removeInfos != null) {
			removeInfos.clear();
			removeInfos = null;
		}
		updateListViewState();

	}

	private ActivityInfo activityInfo;

	@Override
	public boolean onFragmentBack() {

		// LogUtil.elog(TAG, "onback 2");
		if (getFileExplorerActivity().showOperationBarMoreMenu) {
			getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
			updateAuroraItemBottomBarState();
			return true;
		}

		// LogUtil.elog(TAG, "onback 3");

		if (auroraSetRubbishBack()) {
			return true;
		}
		// LogUtil.elog(TAG, "onback 4");
		if (unAllOperation(true)) {
			return true;
		}
		// LogUtil.elog(TAG, "onback 5");
		// 储存器不可用
		if (FileExplorerTabActivity.ROOT_PATH == null) {
			getSelectPageInterface().selectPage(AuroraConfig.AURORA_HOME_PAGE,
					null);
			return true;
		}

		// LogUtil.d(TAG, "onback 6");
		if (getFileExplorerActivity().getIsFromAudioManager() != null
				&& getFileExplorerActivity().getIsFromAudioManager()) {
			String path = FileExplorerTabActivity.mSDCardPath
					+ File.separator
					+ getFileExplorerActivity()
							.getString(R.string.phone_audios);
			if (getNowPath().equals(path)) {
				getFileExplorerActivity().finish();
			} else {
				getFileExplorerActivity().showFileViewPath(path);
			}
			return true;
		}

		// LogUtil.d(TAG, "onback 7");
		if (searchBack()) {
			cancelTask();// 取消任务
			getSelectPageInterface().selectPage(AuroraConfig.AURORA_HOME_PAGE,
					null);
			return true;// 返回到搜索界面
		}
		/** 隐私文件复制剪切返回处理 begin **/
		if (activityInfo != null
				&& (activityInfo.name
						.endsWith(getString(R.string.activity_video)) || activityInfo.name
						.endsWith(getString(R.string.activity_picture)))) {
			if (isPrivacyDoBack) {
				getAuroraListView().setLongClickable(true);
				cancelTask();// 如果在根目录 依然取消任务
				backToCutOrCopyFragment();
				isPrivacyDoBack = false;
				return true;
			}
		}
		/** 隐私文件复制剪切返回处理 end **/

		// LogUtil.d(TAG, "onback 8 ");
		if (getNowPath().equals(FileExplorerTabActivity.getROOT_PATH())
				|| (getFileExplorerActivity().getStoragesStrings().size() == 1 && getNowPath()
						.equals(FileExplorerTabActivity.getmSDCardPath()))) {
			// LogUtil.elog(TAG, "onback 8.1");
			if (getFileExplorerActivity().isGetFileFromSdList()) {
				getFileExplorerActivity().finish();
				return true;
			}
			// 隐藏复制 剪切 确定取消按钮
			if (copyOrCutBack(false)) {
				return true;
			}

			if (!TextUtils.isEmpty(getFileExplorerActivity().getGoPath())) {
				// LogUtil.elog(TAG, "onback 8.2.1");
				getOperation().clearSelectFiles();// 清除选择数据
				cancelTask();// 如果在根目录 依然取消任务
				getFileExplorerActivity().setGoPath("");
				backGoToFragment();
				return true;
			}
			// LogUtil.elog(TAG, "onback 8.3");
			getFileExplorerActivity().setMenuEnable(false);
			// LogUtil.elog(TAG, "onback end");
			cancelTask();// 如果在根目录 依然取消任务
			getSelectPageInterface().selectPage(AuroraConfig.AURORA_HOME_PAGE,
					null);
			return true;

		}
		// LogUtil.elog(TAG, "onback 92222 " + getNowPath());
		if (getFileExplorerActivity().isGetFileFromSdList()
				&& getFileExplorerActivity().getStoragesStrings().size() > 1
				&& getFileExplorerActivity().getStoragesStrings().contains(
						getNowPath())) {
			setNowPath(FileExplorerTabActivity.getROOT_PATH());
			getRootListStorage(true);
			return true;
		}
		// LogUtil.d(TAG, "onback 9");
		if (getRootListStorage(getNowPath(), true)) {
			return true;
		}

		// LogUtil.d(TAG, "onback 10");

		if (!getFileExplorerActivity().getStoragesStrings().contains(
				getNowPath())) {
			cancelTask();// 取消当前正在运行任务
			String parent = Util.getPathFromFilepath(getNowPath());
			setNowPath(parent);
			clearDataAndView();
			showPathListByThread(true);
			return true;
		}
		// LogUtil.d(TAG, "onback 11");
		getFileExplorerActivity().setMenuEnable(false);
		cancelTask();// 如果在根目录 依然取消任务
		getSelectPageInterface()
				.selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
		return true;
	}

	/**
	 * 是否显示复制和剪切状态确定和取消按钮
	 * 
	 * @return
	 */
	public boolean isShowMovingOperationBar() {
		if (isAdded() && moving_operation_bar != null
				&& moving_operation_bar.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	/**
	 * 复制剪切返回
	 * 
	 * @return
	 */
	public boolean copyOrCutBack(boolean isOutPri) {
		if (isAdded() && moving_operation_bar != null
				&& moving_operation_bar.getVisibility() == View.VISIBLE) {
			// LogUtil.elog(TAG, "onback 8.2");
			moving_operation_bar.setVisibility(View.GONE);
			OperationAction.setLastOperation(Operation.noOperation);// 返回时
			// 要取消操作状态
			getOperation().clearSelectFiles();// 清除选择数据
			// add by JXH 2014-7-18 BUG 6644 begin
			getAuroraListView().setLongClickable(true);
			// add by JXH 2014-7-18 BUG 6644 end
			cancelTask();// 如果在根目录 依然取消任务
			backToCutOrCopyFragment(isOutPri);
			return true;
		}
		return false;
	}

	/**
	 * 搜索返回处理
	 * 
	 * @return
	 */
	private boolean searchBack() {
		String key = getFileExplorerActivity().getSearchKey();
		if (key != null && !key.equals("")) {
			String rPath = getFileExplorerActivity().getSearchPath();
			if (rPath != null && !rPath.equals("")) {
				if (rPath.equals(getNowPath())) {
					return true;
				}
			}
		}
		return false;
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
			if (auroraSetRubbishBack()) {
				return;
			}
			setOperationFile(true);
			getAuroraListView().auroraEnableSelector(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
			showEditView();
			auroraFileBrowserAdater.isShowAnim();

			selectFiles.clear();

			updateAuroraItemBottomBarState();
			updateAuroraitemActionBarState();//paul add for BUG #15035
		}
	}

	@Override
	public void onLeftClick() {
		super.onLeftClick();
		unAllOperation(true);
	}

	@Override
	public void onRightClick() {
		super.onRightClick();
		if (selectFiles.size() == fileInfos.size()) {
			selectFiles.clear();
		} else {
			selectFiles.clear();
			selectFiles.addAll(fileInfos);
		}
		updateAuroraItemBottomBarState();
	}

	/** 复制剪切 确定 取消 start **/
	public View moving_operation_bar;
	private AuroraAnimationImageView button_moving_cancel;
	private AuroraAnimationImageView button_moving_confirm;

	public void setupOperationPane(boolean show) {
		if (isAdded()) {
			if (moving_operation_bar == null) {
				moving_operation_bar = getView().findViewById(
						R.id.moving_operation_bar);
			}
			if (show) {
				moving_operation_bar.setVisibility(View.VISIBLE);
				getFileExplorerActivity().setMenuEnable(false);
			}
			button_moving_cancel = (aurora.widget.AuroraAnimationImageView) moving_operation_bar
					.findViewById(R.id.button_moving_cancel);
			button_moving_confirm = (aurora.widget.AuroraAnimationImageView) moving_operation_bar
					.findViewById(R.id.button_moving_confirm);
			button_moving_cancel.setOnClickListener(buttonClick);
			button_moving_confirm.setOnClickListener(buttonClick);
			setButtonMovingEnabled(true);
		}

	}

	/**
	 * 设置确定按钮是否可用 true 不可用
	 */
	private void setButtonMovingEnabled(boolean enable) {
		if (button_moving_confirm == null) {
			// LogUtil.e(TAG, "button_moving_confirm is null");
			return;
		}
		if (getFileExplorerActivity().getStoragesStrings().size() != 1) {
			auroraFileBrowserAdater.setRoot(enable);
		}
		if (auroraFileBrowserAdater.isRoot()) {
			getAuroraListView().auroraSetNeedSlideDelete(false);
			button_moving_confirm.setEnabled(false);
		} else {
			getAuroraListView().auroraSetNeedSlideDelete(true);
			button_moving_confirm.setEnabled(true);
		}
	}

	/**
	 * 标志复制，剪切隐私文件返回处理
	 */
	private boolean isPrivacyDoBack;
	private View.OnClickListener buttonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			moving_operation_bar.setVisibility(View.GONE);
			switch (v.getId()) {
			case R.id.button_moving_cancel:
				// LogUtil.log(TAG, "button_moving_cancel");
				moving_operation_bar.setVisibility(View.GONE);
				getFileExplorerActivity().setMenuEnable(true);
				getOperation().clearSelectFiles();
				OperationAction.setLastOperation(Operation.noOperation);
				getAuroraListView().auroraSetNeedSlideDelete(true);
				// add by JXH 2014-7-18 BUG 6644 begin
				getAuroraListView().setLongClickable(true);
				// add by JXH 2014-7-18 BUG 6644 end
				// 返回原fragment操作
				backToCutOrCopyFragment();
				break;
			case R.id.button_moving_confirm:
				if (activityInfo != null
						&& (activityInfo.name
								.endsWith(getString(R.string.activity_video)) || activityInfo.name
								.endsWith(getString(R.string.activity_picture)))) {
					isPrivacyDoBack = true;
				}
				switch (OperationAction.getLastOperation()) {
				case copy:
					getOperation().copyFileOrDir(getNowPath());
					break;
				case cut:
					getOperation().moveFileOrDir(getNowPath());
					break;
				default:
					break;

				}
				break;

			default:
				break;
			}
		}
	};

	/** 复制剪切 确定 取消 end **/

	/**
	 * 返回原fragment操作
	 */
	private void backToCutOrCopyFragment() {
		backToCutOrCopyFragment(false);
	}

	private void backToCutOrCopyFragment(boolean outPri) {
		Fragment fragment = getFileExplorerActivity().getCutOrCopyfragment();
		if (fragment != null) {
			if (fragment instanceof FileCategoryFragment) {
				int page = getFileExplorerActivity()
						.getPageByFragment(fragment);
				FileCategory category = getFileExplorerActivity()
						.getCutOrCopyCategory();
				if (page != -1 && category != null) {
					getSelectPageInterface().selectPage(page, category);
					if (page != AuroraConfig.AURORA_FILE_PAGE) {
						getFileExplorerActivity().setMenuEnable(false);
					}
				}
			} else if (fragment instanceof PictureFragment) {
				if (outPri) {
					getSelectPageInterface().selectPage(
							AuroraConfig.AURORA_PIC_PAGE, null);
					getFileExplorerActivity().setMenuEnable(false);
				} else {
					int page = getFileExplorerActivity().getPageByFragment(
							fragment);
					if (page != -1) {
						getSelectPageInterface().selectPage(page, null);
						if (page != AuroraConfig.AURORA_FILE_PAGE) {
							getFileExplorerActivity().setMenuEnable(false);
						}
					}
				}
			} else if (fragment instanceof FileViewFragment) {
				// LogUtil.log(TAG, "back FileViewFragment");
				auroraFileBrowserAdater.setRoot(false);
				setNowPath(getFileExplorerActivity().getCutOrCopyePath());
				clearDataAndView();
				showPathListByThread(true);
			}
		}
	}

	// add by Jxh 2014-8-26 begin
	private void backGoToFragment() {
		Fragment fragment = getFileExplorerActivity().getGofragment();
		if (fragment != null) {
			if (fragment instanceof FileCategoryFragment) {
				int page = getFileExplorerActivity()
						.getPageByFragment(fragment);
				FileCategory category = getFileExplorerActivity()
						.getGoCategory();
				if (page != -1 && category != null) {
					getSelectPageInterface().selectPage(page, category);
					if (page != AuroraConfig.AURORA_FILE_PAGE) {
						getFileExplorerActivity().setMenuEnable(false);
					}
				}
			} else if (fragment instanceof AuroraMainFragment) {
				int page = getFileExplorerActivity()
						.getPageByFragment(fragment);
				if (page != -1) {
					getSelectPageInterface().selectPage(page, null);
					if (page != AuroraConfig.AURORA_FILE_PAGE) {
						getFileExplorerActivity().setMenuEnable(false);
					}
				}
			} else if (fragment instanceof PictureFragment) {
				int page = getFileExplorerActivity()
						.getPageByFragment(fragment);
				if (page != -1) {
					getFileExplorerActivity().setPicItemRefresh(true);
					getSelectPageInterface().selectPage(page, null);
					if (page != AuroraConfig.AURORA_FILE_PAGE) {
						getFileExplorerActivity().setMenuEnable(false);
					}
				}
			}
		}
	}

	// add by Jxh 2014-8-26 end

	/** 显示文件路径开始 **/
	private TextView tab_txt;
	private HorizontalScrollView navigation_scroll_pane;
	private static final int DELAYTIME = 10;

	private void navigationShow(String path) {
		if (navigation_scroll_pane == null) {
			navigation_scroll_pane = (HorizontalScrollView) getView()
					.findViewById(R.id.navigation_scroll_pane);
			navigation_scroll_pane.setVisibility(View.VISIBLE);
		}
		if (tab_txt == null) {
			tab_txt = (TextView) getView().findViewById(R.id.tab_txt);
		}
		tab_txt.setText(String.format(getString(R.string.tabs), path));
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				navigation_scroll_pane
						.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, DELAYTIME);
	}

	/** 显示文件路径end **/
	/**
	 * 清除显示数据
	 */
	private synchronized void clearDataAndView() {
		if (fileInfos == null || auroraFileBrowserAdater == null) {
			return;
		}
		fileInfos.clear();
		auroraFileBrowserAdater.setFileInfos(fileInfos);
		auroraFileBrowserAdater.notifyDataSetChanged();
	}

	/**
	 * 重命名 删除后 刷新数据
	 */
	private void refreshState() {
		if (unAllOperation(true)) {
			if (getAuroraListView().getCount() != 0) {
				getFileExplorerActivity().showEditBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			} else {
				getFileExplorerActivity().showEmptyBarItem();
				getLoadAndEmptyView().getEmptyView()
						.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 获取可用到储存路径
	 * 
	 * @param path
	 * @return
	 */
	public boolean getRootListStorage(String path, boolean isBack) {
		if (TextUtils.isEmpty(path) || !isAdded()) {
			return false;
		}
		// LogUtil.log(TAG, "getRootListStorage===" + path);
		if (isBack && moving_operation_bar != null
				&& moving_operation_bar.getVisibility() == View.VISIBLE) {
			String fPath = Util.getPathFromFilepath(path);
			if (FileExplorerTabActivity.ROOT_PATH == null) {
				return false;
			}

			// LogUtil.log(TAG, "fPath==" + fPath);
			if (fPath.equals(FileExplorerTabActivity.ROOT_PATH)
					|| (getFileExplorerActivity().getStoragesStrings()
							.contains(path))) {
				// LogUtil.log(TAG, "ROOT_PATH getRootListStorage===" + path);
				// add by JXH 2014-7-28 begin
				if (getFileExplorerActivity().getStoragesStrings().size() == 1) {
					setNowPath(getFileExplorerActivity().getStoragesStrings()
							.get(0));
					auroraFileBrowserAdater.setRoot(false);
					return false;
				}
				// add by JXH 2014-7-28 end
				setNowPath(FileExplorerTabActivity.ROOT_PATH);
				return getRootListStorage(true);
			}
		}

		else if (!isBack
				&& (FileExplorerTabActivity.ROOT_PATH != null && path
						.equals(FileExplorerTabActivity.ROOT_PATH))) {
			// LogUtil.log(TAG, "ROOT_PATH");
			// add by JXH 2014-7-28 begin
			if (getFileExplorerActivity().getStoragesStrings().size() == 1) {

				setNowPath(getFileExplorerActivity().getStoragesStrings()
						.get(0));
				auroraFileBrowserAdater.setRoot(false);
				// LogUtil.log(TAG,
				// "getFileExplorerActivity().isNeedRefresh()=="
				// + getFileExplorerActivity().isNeedRefresh());
				if (getFileExplorerActivity().isNeedRefresh()) {
					if (getFileExplorerActivity().isGetFileFromSdList()) {
						setupOperationPane(false);
					} else {
						setupOperationPane(true);
					}
					showPathListByThread(true);
				} else {
					getFileExplorerActivity().setNeedRefresh(true);
					if (selectFiles != null && selectFiles.size() != 0) {
						// 如果是剪切操作 需要去掉被剪切到内容
						if (OperationAction.getLastOperation() != null
								&& OperationAction.getLastOperation().equals(
										Operation.cut)) {
							fileInfos.removeAll(selectFiles);
						}
					}
					getAuroraListView().setSelection(0);
				}
				return true;
			}
			// add by JXH 2014-7-28 end
			return getRootListStorage(true);
		} else if (!backSd
				&& (FileExplorerTabActivity.getmSDCardPath() != null && !path
						.equals(FileExplorerTabActivity.getmSDCardPath()))
				&& (getFileExplorerActivity().getStoragesStrings()
						.contains(path))
				&& getFileExplorerActivity().getStoragesStrings().size() > 2) {
			// LogUtil.log(TAG, "mSDCard2Path");
			cancelTask();// 取消当前正在运行任务
			clearDataAndView();
			return getRootListStorage(false);

		}
		return false;

	}

	public boolean backSd = false;

	private boolean getRootListStorage(boolean showPane) {
		List<FileInfo> storages = getFileExplorerActivity().getStorages();
		// LogUtil.log(TAG, "storages.size()==" + storages.size());
		cancelTask();// 取消任务，防止多个无效项
		if (storages != null) {
			fileInfos.clear();
			fileInfos.addAll(storages);
			//paul del for BUG #14733
			//storages.clear();
			//storages = null;
			if (getFileExplorerActivity().isGetFileFromSdList()) {
				setupOperationPane(false);
			} else {
				setupOperationPane(showPane);
			}
			if (!showPane) {
				if (fileInfos.size() > 0) {
					fileInfos.remove(0);
				}
				backSd = true;
			}
			getAuroraListView().setLongClickable(false);
			auroraFileBrowserAdater.setFileInfos(fileInfos);
			auroraFileBrowserAdater.notifyDataSetChanged();
			getFileExplorerActivity().showEmptyBarItem();
			// showLoadingImage(false);
			getFileExplorerActivity().setMenuEnable(false);// 处于SD卡列表 不显示menu
			if (getAuroraListView().getCount() == 0) {
				getLoadAndEmptyView().getEmptyView()
						.setVisibility(View.VISIBLE);
			} else {
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			}
			return true;
		}
		return false;
	}

	/**
	 * 刷新SD卡数据
	 */
	public void updataRootPaths() {
		if (isAdded()) {
			if (auroraFileBrowserAdater.isRoot()) {
				List<FileInfo> storages = getFileExplorerActivity()
						.getStorages();
				fileInfos.clear();
				fileInfos.addAll(storages);
				//paul del for BUG #14733
				//storages.clear();
				//storages = null;
				LogUtil.d(TAG, "updataRootPaths backSd==" + backSd);
				if (backSd) {
					fileInfos.remove(0);
				}
				setupOperationPane(!backSd);
				auroraFileBrowserAdater.setFileInfos(fileInfos);
				auroraFileBrowserAdater.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 选择ITEM
	 * 
	 * @param position
	 * @param view
	 * @param fileInfo
	 * @param anmi
	 */
	private void operationAtion(int position, View view, FileInfo fileInfo,
			boolean anmi) {
		aurora.widget.AuroraCheckBox checkbox = ((AuroraFilesItemView) view
				.getTag()).getCheckBox();
		if (selectFiles.contains(fileInfo)) {
			selectFiles.remove(fileInfo);
			checkbox.auroraSetChecked(false, anmi);
		} else {
			selectFiles.add(fileInfo);
			checkbox.auroraSetChecked(true, anmi);
		}
		updateAuroraItemBottomBarState();
		updateAuroraitemActionBarState();
	}

	/**
	 * 改变BottomBar 状态
	 */
	public void updateAuroraItemBottomBarState() {
		if (isAdded()) {
			if (selectFiles == null) {
				return;
			}
			// LogUtil.log(TAG, "selectFiles.size()==" + selectFiles.size());
			// LogUtil.log(TAG, "fileInfos.size()==" + fileInfos.size());
			AuroraMenu auroraMenu = getAuroraActionBar()
					.getAuroraActionBottomBarMenu();
			auroraFileBrowserAdater.setSelectFiles(selectFiles);
			getOperation().setSelectFiles(selectFiles);
			if (selectFiles.size() == 0) {
				auroraMenu.setBottomMenuItemEnable(1, false);
				auroraMenu.setBottomMenuItemEnable(2, false);
				auroraMenu.setBottomMenuItemEnable(3, false);
				auroraMenu.setBottomMenuItemEnable(4, false);
				auroraMenu.setBottomMenuItemEnable(5, false);
				auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(
						View.VISIBLE);
			} else if (selectFiles.size() == 1) {
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				auroraMenu.setBottomMenuItemEnable(4, true);
				auroraMenu.setBottomMenuItemEnable(5, true);
				auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(
						View.VISIBLE);
			} else {
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				auroraMenu.setBottomMenuItemEnable(4, true);
				auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(View.GONE);
			}
			for (int i = 0; i < selectFiles.size(); i++) {
				if (selectFiles.get(i).IsDir) {
					auroraMenu.setBottomMenuItemEnable(2, false);
					break;
				}
			}
			auroraFileBrowserAdater.notifyDataSetChanged();
		}
	}

	/**
	 * handle 播放动画使用
	 * 
	 * @return
	 */
	public AuroraFileBrowserAdater getAuroraFileBrowserAdater() {
		if (isAdded()) {
			return auroraFileBrowserAdater;
		}
		return null;
	}

	/**
	 * 取消编辑状态
	 */
	public boolean unAllOperation(boolean showAnim) {
		if (isOperationFile()) {
			setOperationFile(false);
			getAuroraListView().auroraEnableSelector(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			if (selectFiles != null) {
				selectFiles.clear();
				auroraFileBrowserAdater.setSelectFiles(selectFiles);
			}
			getFileExplorerActivity().hideDeleteMenu();
			return true;
		}
		return false;
	}

	/**
	 * 取消编辑状态
	 */
	public boolean delUnAllOperation() {
		if (isOperationFile()) {
			setOperationFile(false);
			getAuroraListView().auroraEnableSelector(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			if (selectFiles != null) {
				selectFiles.clear();
				auroraFileBrowserAdater.setSelectFiles(selectFiles);
			}
			getAuroraActionBar().setShowBottomBarMenu(false);
			getAuroraActionBar().showActionBarDashBoard();
			auroraFileBrowserAdater.isShowAnim();
			getFileExplorerActivity().setMenuEnable(true);
			return true;
		}
		return false;
	}

	/**
	 * 取消编辑状态
	 */
	public boolean unAllOperation() {
		if (isAdded() && isOperationFile()) {
			setOperationFile(false);
			getAuroraListView().auroraEnableSelector(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			getFileExplorerActivity().hideDeleteMenu();
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 复制和剪切界面跳转 取消编辑状态
	 * 
	 * @return
	 */
	public boolean unAllOperationCopyOrCut() {
		if (isAdded() && isOperationFile()) {
			setOperationFile(false);
			getAuroraListView().auroraEnableSelector(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			getFileExplorerActivity().hideDeleteMenuCopyOrCut();
			return true;
		}
		return false;
	}

	/**
	 * add by jxh 2014-8-27 判断是否是长按状态下
	 */
	private boolean isLastitem = false;

	private int lastBottom;

	/**
	 * 显示底部按钮
	 */
	private void showEditView() {
		getFileExplorerActivity().setRootView(getView());// 设置VIEW 显示more菜单
		getAuroraActionBar().setShowBottomBarMenu(true);
		getAuroraActionBar().showActionBarDashBoard();
		getFileExplorerActivity().setMenuEnable(false);// 编辑状态不显示
		if (isLastItemMove) {
			final int editHeight = (int) getFileExplorerActivity()
					.getResources().getDimension(
							com.aurora.R.dimen.aurora_action_bottom_bar_height);
			final int fristBottom = (int) getFileExplorerActivity()
					.getResources().getDimension(R.dimen.frist_bottom);
			if (isLastitem || lastBottom - fristBottom < 0
					&& lastBottom - fristBottom + editHeight > 0) {
				isLastitem = false;
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						getAuroraListView().smoothScrollBy(
								lastBottom - fristBottom + editHeight, 0);
					}
				}, 100);
			}
		}
	}

	/**
	 * 设置返回时 前一次位置
	 */
	private void setLastListViewLocation() {
		int q = dequeue();
		getAuroraListView().setSelection(q);
	}

	private static final int maxSize = 600;
	private ShowPathTask pathTask;

	/**
	 * 根据路径显示路径下子文件 子目录
	 */
	public void showPathListByThread(boolean back) {
		// LogUtil.log(TAG, "showPathListByThread back ==" + back);
		getAuroraListView().auroraSetNeedSlideDelete(false);
		if (pathTask != null
				&& pathTask.getStatus() == AsyncTask.Status.RUNNING) {
			if (back) {
				pathTask.cancel(true);
			} else {
				return;
			}
		}
		if (pathTask != null) {
			pathTask.cancel(true);
		}
		pathTask = new ShowPathTask();
		// pathTask.execute(getNowPath());
		pathTask.executeOnExecutor(getFULL_TASK_EXECUTOR(), getNowPath());
	}

	// add by JXH 2014-8-26 begin Jump position
	private int goPosition = 0;

	// add by JXH 2014-8-26 end

	private class ShowPathTask extends AsyncTask<String, Void, List<FileInfo>> {

		private List<FileInfo> infosTemp = new ArrayList<FileInfo>();

		@Override
		protected List<FileInfo> doInBackground(String... params) {
			String path = params[0];
			// May be U disk mode or memory error
			if (path == null) {
				return null;
			}
			File parent = new File(path);
			final File files[] = parent.listFiles(AuroraFilenameFilter
					.getInstance());
			// long time1 = SystemClock.currentThreadTimeMillis();
			if (files != null) {
				int length = files.length;
				if (length > maxSize) {
					handler.sendEmptyMessage(SHOWLOAD);
				}
				infosTemp.clear();

				for (int i = 0; i < files.length; i++) {
					if (isCancelled()) {
						// LogUtil.elog(TAG, "isCancelTask ok");
						return null;
					}
					if (Util.isNormalFile(files[i].getAbsolutePath())) {
						FileInfo fileInfo = Util.getSimpleFileInfo(files[i]);
						if (fileInfo != null) {
							infosTemp.add(fileInfo);
						}
					}
				}
				if (selectFiles != null && selectFiles.size() != 0) {
					// If it is cut operation needs to be cut to remove content
					if (OperationAction.getLastOperation() != null
							&& OperationAction.getLastOperation().equals(
									Operation.cut)) {
						boolean r = infosTemp.removeAll(selectFiles);
					}
				}
				try {
					fileSortHelper.setSortMethog(SortMethod.name);
					Collections.sort(infosTemp, fileSortHelper.getComparator());// 排序
					// add by JXH 2014-8-26 begin 获取跳转界面 显示位置
					String goPath = getFileExplorerActivity().getGoFilePath();
					if (!TextUtils.isEmpty(goPath)) {
						FileInfo fileInfo = Util.getSimpleFileInfo(new File(
								goPath));
						goPosition = infosTemp.indexOf(fileInfo);
						getFileExplorerActivity().setGoFilePath("");
						getFileExplorerActivity().setGoPath("");
						goPath = getFileExplorerActivity().getGoFilePath();
					}
					// add by JXH 2014-8-26 end 获取跳转界面 显示位置
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// LogUtil.log(TAG, "doInBackground ok");
			return infosTemp;
		}

		@Override
		protected void onPostExecute(List<FileInfo> result) {
			super.onPostExecute(result);
			// LogUtil.log(TAG, "onPostExecute");
			if (isCancelled()) {
				return;
			}
			if (result != null) {
				fileInfos.clear();
				fileInfos.addAll(result);
				result.clear();
				result = null;
			}
			refreshListView(true);

		}
	}

	/** 取消当前任务 **/
	public void cancelTask() {
		if (pathTask != null
				&& pathTask.getStatus() == AsyncTask.Status.RUNNING) {
			pathTask.cancel(true);
		}
	}

	@Override
	public void dialogDismiss() {
		updateAuroraItemBottomBarState();
	}

	// add by JXH 2014-7-14 begin

	// 存放返回位置容器
	private LinkedList<Integer> listQueue = new LinkedList<Integer>();

	// 入栈
	public void enqueueLinke(Integer position) {
		// LogUtil.elog(TAG, "enqueueLinke ==" + position);
		listQueue.add(position);
	}

	// 先入先出
	public Integer dequeue() {
		if (listQueue.size() == 0) {
			// LogUtil.elog(TAG, "listQueue.size() == 0");
			return 0;
		}
		int position = listQueue.removeLast();
		// LogUtil.elog(TAG, "dequeue ==" + position);
		return position;
	}

	// 清空栈
	public void clearQueue() {
		listQueue.clear();
	}

	// add by JXH 2014-7-14 end
}
