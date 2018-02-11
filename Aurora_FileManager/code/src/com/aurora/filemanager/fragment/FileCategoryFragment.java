package com.aurora.filemanager.fragment;

import java.io.File;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.R.bool;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore.Files.FileColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileIconHelper;
import com.aurora.tools.FileIconLoader;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileSortHelper;
import com.aurora.tools.FileUtils;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.OperationAction;
import com.aurora.tools.ToastUtils;
import com.aurora.tools.Util;
import com.aurora.tools.FileSortHelper.SortMethod;
import com.aurora.tools.OperationAction.Operation;
import com.aurora.widget.AuroraFilesAdapter;
import com.aurora.widget.AuroraFilesItemView;
import com.aurora.widget.AuroraLoadAndEmptyView;
import com.aurora.widget.AuroraOperationBarMoreMenu;
import com.aurora.filemanager.R;
import com.privacymanage.PrivacyBroadcastReceiver;

/**
 * 其他分类浏览
 * @author jiangxh
 * @CreateTime 2014年4月24日 下午5:37:49
 * @Description com.aurora.filemanager.fragment FileCategorgFragment.java
 */
public class FileCategoryFragment extends AuroraFragment {
	private AuroraFilesAdapter auroraFilesAdapter;

	private boolean isDestroy = false;
	// private boolean needRefresh = false;//onStop >onStart refresh

	private TextView privacyNum;

	private static final String TAG = "FileCategoryFragment";
	private FileCategory fileCategory;
	private FileSortHelper fileSortHelper = new FileSortHelper();

	private List<FileInfo> privacyFiles;// 隐私数据

	private boolean isPrivacyView;

	public boolean isPrivacyView() {
		return isPrivacyView;
	}

	public void setPrivacyView(boolean isPrivacyView) {
		if (getFileExplorerActivity() != null) {
			getFileExplorerActivity().setHideOperationBarMoreMenu(isPrivacyView);
		}
		this.isPrivacyView = isPrivacyView;
	}

	private View privacyView;

	private static final int REFRESHCATEGORY = 1001;
	private static final int LASTQUERY = 1002;
	private static final int SHOWLOADING = 1003;

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (isDestroy) {
				return;
			}
			auroraSetRubbishBack();// 回收删除框
			switch (msg.what) {
			case REFRESHCATEGORY:
				if (msg.arg1 != REFRESHCATEGORY) {
					fileInfos.clear();
					fileInfos.addAll(tempInfos);
				}
				if (msg.obj != null) {
					fileInfos.removeAll((ArrayList<FileInfo>) msg.obj);
				}
				auroraFilesAdapter.setFileInfos(fileInfos);
				auroraFilesAdapter.notifyDataSetChanged();
				updateListViewState();
				showLoadingImage(false);
				// add by Jxh 2014-8-11 begin
				getAuroraListView().setSelection(getSelectPostion());
				setSelectPostion(0);
				break;
			case LASTQUERY:
				if (isLast) {
					Cursor cursor = (Cursor) msg.obj;
					isLast = false;
					cursorAsyncTask.cancel(true);
					cursorAsyncTask = new CursorAsyncTask();
					// cursorAsyncTask.execute(cursor);
					cursorAsyncTask.executeOnExecutor(getFULL_TASK_EXECUTOR(), cursor);
				}
				break;
			case SHOWLOADING:
				showLoadingImage(true);
				break;
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_category_files, container, false);
	}

	@Override
	public void auroraDelOnClick(int position) {
		super.auroraDelOnClick(position);

		if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video) && !isPrivacyView) {
			if (position == 0) {
				return;
			} else {
				position -= 1;
			}
		}
		FileInfo fileInfo = fileInfos.get(position);
		if (getOperation().fastDeleteFileOrDir(fileInfo)) {
			getFileExplorerActivity().doPrivacyAction();
			getAuroraListView().auroraDeleteSelectedItemAnim();
			if (getFileExplorerActivity().isDoPrivacy() && privacyFiles != null) {
				if (privacyFiles.size() > 0) {
					privacyFiles.remove(fileInfo);
				}
				getFileExplorerActivity().getHashMap().put(FileCategory.Video, privacyFiles);
				getFileExplorerActivity().setPrivacyMedioFileNum(FileCategory.Video, privacyFiles.size());
			}
			getFileExplorerActivity().setDoPrivacy(false);
			setStatistics(11);
		} else {
			ToastUtils.showTast(getFileExplorerActivity(), String.format(getFileExplorerActivity().getString(R.string.del_file), fileInfo.fileName));
		}

	}

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);

		if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
			return;
		}
		// LogUtil.elog(TAG, "position==" + position);
		if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video) && !isPrivacyView && !getFileExplorerActivity().getIsGetMoreNoPrivacy() && privacyFiles != null) {
			if (position == 0) {
				if (isOperationFile()) {
					return;
				}
				getFileExplorerActivity().showEmptyBarItem();
				getAuroraActionBar().changeItemLayout(R.layout.privacy_add, R.id.pri_add_more_liner);
				setAddMoreLister();

				getFileExplorerActivity().setAuroraActionBarTitle(getString(R.string.p_video));
				getFileExplorerActivity().setVideoList(fileInfos);
				fileInfos.clear();
				fileInfos.addAll(privacyFiles);
				auroraFilesAdapter.setFileInfos(fileInfos);
				getAuroraListView().removeHeaderView(view);
				auroraFilesAdapter.notifyDataSetChanged();
				setPrivacyView(true);
				updateListViewState();
				return;
			} else {
				position -= 1;
			}
		}

		FileInfo fileInfo = fileInfos.get(position);
		if (fileInfo == null) {
			return;
		}
		if (isOperationFile()) {
			operationAtion(position, view, fileInfo, true);
			return;
		}
		IntentBuilder.viewFile(getFileExplorerActivity(), fileInfo.filePath);

	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (getOperation() != null && getOperation().isCutOrCopy()) {
			ToastUtils.showTast(getFileExplorerActivity(), R.string.wait_a_moment);
			return true;
		}
		if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
			return true;
		}

		if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video) && !isPrivacyView && privacyFiles != null && !getFileExplorerActivity().getIsGetMoreNoPrivacy()) {
			if (position == 0) {
				return true;
			} else {
				position -= 1;
			}

		}

		FileInfo fileInfo = fileInfos.get(position);
		if (fileInfo == null) {
			return true;
		}
		if (!isOperationFile()) {
			setOperationFile(true);
			setStatistics(12);
			getAuroraListView().auroraEnableSelector(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
			operationAtion(position, view, fileInfo, false);
			if (isLastItemMove) {
				// add By jxh 2014-8-28 begin
				if (position == getAuroraListView().getLastVisiblePosition()) {
					isLastitem = true;
				}
				lastBottom = view.getBottom();
				// add By jxh 2014-8-28 end
			}

			showEditView();
			auroraFilesAdapter.isShowAnim();
			updateAuroraitemActionBarState();
		}
		return true;

	}

	@Override
	public void auroraDeleteItemView(View view, int position) {
		super.auroraDeleteItemView(view, position);
		getAuroraListView().auroraSetRubbishBack();
		try {// 有时候会出现数组越界 原因待查明
			fileInfos.remove(position);
			auroraFilesAdapter.setFileInfos(fileInfos);
			auroraFilesAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateListViewState();
		OperationAction.setLastOperation(Operation.noOperation);
	}

	@Override
	public void auroraOnScrollStateChanged(AbsListView view, int scrollState) {
		super.auroraOnScrollStateChanged(view, scrollState);
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			auroraFilesAdapter.setLoadIcon(false);
		} else {
			auroraFilesAdapter.setLoadIcon(true);
		}
	}

	private boolean onQueryPri;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setAuroraListView((AuroraListView) getView().findViewById(R.id.files_listView));
		getAuroraListView().auroraSetNeedSlideDelete(true);// 滑动删除功能打开
		super.onActivityCreated(savedInstanceState);
		// 获取当前分类
		fileCategory = getNowfileCategory();
		// 初始化Adater
		auroraFilesAdapter = new AuroraFilesAdapter(fileInfos, getFileExplorerActivity(), this);

		if (getActivityInfo() != null && getActivityInfo().name.endsWith(getString(R.string.activity_video))) {
			onQueryPri = true;
			queryPrivacy(true);

		} else if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video)) {
			setPrivacyView(false);
		}
		showPrivacyView();
		if (!isPrivacyView) {
			refreshCategoryInfo(fileCategory);
			// 修改actionBar title
			getCategoryHelper().setmCategory(fileCategory);
			getFileExplorerActivity().setAuroraActionBarTitle(getCategoryHelper().getCurCategoryNameResId());

		}

		if (getFileExplorerActivity().getIsFromOtherAPP() && getFileExplorerActivity().getIsGetMoreVideo() && getFileExplorerActivity().getIsGetMoreNoPrivacy()) {

			getAuroraActionBar().initActionBottomBarMenu(R.menu.aurora_operation_add_bar_menu, 1);
			selectFiles.clear();
			hidPrivacyView();
			if (!isOperationFile()) {
				getAuroraListView().auroraEnableSelector(false);
				getAuroraListView().auroraSetNeedSlideDelete(false);
				setOperationFile(true);
				getAuroraActionBar().goToActionBarSelectView();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						getAuroraActionBar().showActionBottomeBarMenu();
					}
				}, 200);

			}
			updateAuroraItemBottomBarState();

		} else {
			getAuroraActionBar().initActionBottomBarMenu(R.menu.aurora_operation_bar_more_menu, 5);
		}

	}

	private void queryPrivacy(boolean change) {
		showLoadingImage(true);
		getFileExplorerActivity().showEmptyBarItem();
		if (change || getFileExplorerActivity().isPrivacy()) {
			getAuroraActionBar().changeItemLayout(R.layout.privacy_add, R.id.pri_add_more_liner);
			setAddMoreLister();
		}

		setPrivacyView(true);
		getFileExplorerActivity().setAuroraActionBarTitle(getString(R.string.p_video));
		fileInfos.clear();
		if (privacyTask != null) {
			privacyTask.cancel(true);
		}
		privacyTask = new PrivacyTask();
		// privacyTask.execute();
		privacyTask.executeOnExecutor(getFULL_TASK_EXECUTOR());
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isPrivacyView && !onQueryPri && getFileExplorerActivity().isPrivacy()) {
			queryPrivacy(false);
		}

		if (onQueryPri) {
			onQueryPri = false;
		}

		if (fileCategory.equals(FileCategory.Video)
				&& (!getFileExplorerActivity().isPrivacy() || (!getFileExplorerActivity().getIsGetMoreNoPrivacy() && getFileExplorerActivity().getIsFromOtherAPP() && getFileExplorerActivity()
						.getIsGetMoreVideo()))) {
			hidPrivacyView();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				List<String> paths = bundle.getStringArrayList("video");
				if (paths == null || paths.size() == 0) {
					return;
				}
				List<FileInfo> infos = new ArrayList<FileInfo>();
				if (paths != null) {
					for (String string : paths) {
						FileInfo fileInfo = Util.GetFileInfo(string);
						infos.add(fileInfo);
					}
				}

				getOperation().setSelectFiles(infos);
				getOperation().addPrivacyFiles(FileCategory.Video);
			}
		}
	}

	/**
	 * 清空隐私数据
	 */
	public void isNoPriView() {
		if (isAdded()) {
			if (isOperationFile()) {
				unAllOperation();
			}
			showLoadingImage(false);
			fileInfos.clear();
			auroraFilesAdapter.setFileInfos(fileInfos);
			auroraFilesAdapter.notifyDataSetChanged();
			if (getAuroraListView().getCount() != 0) {
				getFileExplorerActivity().showEditBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			} else {
				getFileExplorerActivity().showEmptyBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
			}
			if (getActivityInfo() != null && !getActivityInfo().name.endsWith(getString(R.string.activity_video))) {
				getFileExplorerActivity().setAuroraActionBarTitle(getCategoryHelper().getCurCategoryNameResId());
				fileInfos.clear();
				fileInfos.addAll(getFileExplorerActivity().getVideoList());
				if (privacyFiles != null && privacyFiles.size() > 0) {
					fileInfos.removeAll(privacyFiles);
				}
				if (getFileExplorerActivity().showOperationBarMoreMenu) {
					getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
				}
				auroraFilesAdapter.setFileInfos(fileInfos);
				auroraFilesAdapter.notifyDataSetChanged();
				setPrivacyView(false);
				if (imageButton != null) {
					imageButton.setVisibility(View.GONE);
				}
				updateListViewState();
			} else if (getActivityInfo() != null && getActivityInfo().name.endsWith(getString(R.string.activity_video))) {
				if (imageButton != null) {
					imageButton.setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * 显示隐私视频选择项
	 */
	public void showPrivacyView() {
		if (isAdded() && !isPrivacyView) {
			ConcurrentHashMap<FileCategory, List<FileInfo>> map = getFileExplorerActivity().getHashMap();
			privacyFiles = map.get(FileCategory.Video);
			if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video) && privacyFiles != null && !getFileExplorerActivity().getIsGetMoreNoPrivacy()) {
				if (privacyView != null) {
					getAuroraListView().removeHeaderView(privacyView);
				}
				privacyView = LayoutInflater.from(getFileExplorerActivity()).inflate(R.layout.privacy_video_title, null);
				getAuroraListView().setAdapter(null);
				getAuroraListView().addHeaderView(privacyView);
				getAuroraListView().setAdapter(auroraFilesAdapter);
				privacyNum = (TextView) privacyView.findViewById(R.id.file_num);
				privacyNum.setText(privacyFiles.size() + "");
			}
		}
	}

	/**
	 * 隐藏隐私视频选择项
	 */
	public void hidPrivacyView() {
		if (isAdded()) {
			if (privacyView != null) {
				getAuroraListView().removeHeaderView(privacyView);
			}
		}
	}

	@Override
	public void onDestroyView() {
		getAuroraListView().setAdapter(null);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		isDestroy = true;
		if (isPrivacyView && imageButton != null) {
			imageButton.setVisibility(View.GONE);
		}
		super.onDestroy();
	}

	/**
	 * 初始化 刷新数据
	 * @param fileCategory
	 * @param sort
	 */
	public void refreshCategoryInfo(FileCategory fileCategory) {
		// LogUtil.log(TAG, "refreshCategoryInfo " + fileCategory);
		if (!isAdded()) {
			return;
		}
		SortMethod sort = SortMethod.modifyDate;
		switch (fileCategory) {// Music, Video, Picture, Doc, Apk, Other,
								// DownLoad
		case Music:
			sort = SortMethod.music;
			break;
		case Doc:
			sort = SortMethod.name;
			break;
		case Apk:
			sort = SortMethod.modifyDate;
			break;
		case Video:
			sort = SortMethod.name;
			break;
		default:
			break;
		}
		if (getFileExplorerActivity() != null) {
			getCategoryHelper().query(fileCategory, sort);
		}
	}

	@Override
	public void deleteComplete(List<FileInfo> ids) {
		// fileInfos.removeAll(ids);//fix BUG 5.1MTK
		if (privacyFiles != null && privacyFiles.size() > 0) {
			privacyFiles.removeAll(ids);
		}
		getOperation().mDialogDismiss();

		unAllOperation();
		if (getFileExplorerActivity().isDoPrivacy() && privacyFiles != null) {
			getFileExplorerActivity().getHashMap().put(FileCategory.Video, privacyFiles);
			getFileExplorerActivity().setPrivacyMedioFileNum(FileCategory.Video, privacyFiles.size());
			getFileExplorerActivity().setDoPrivacy(false);
		}
		Message message = handler.obtainMessage();
		message.what = REFRESHCATEGORY;
		message.arg1 = REFRESHCATEGORY;
		message.obj = ids;
		handler.sendMessageDelayed(message, 300);

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
	public void renameComplete(FileInfo old, FileInfo newFileInfo) {
		if (fileCategory != null && !fileCategory.equals(FileCategory.Apk)) {
			fileInfos.remove(old);
			fileInfos.add(newFileInfo);
			fileSortHelper.setSortMethog(SortMethod.name);
			Collections.sort(fileInfos, fileSortHelper.getComparator());// 排序
			// add by Jxh 2014-8-11 begin

			int i = fileInfos.indexOf(newFileInfo);
			if (i != -1) {
				setSelectPostion(i);
			}
			// add by Jxh 2014-8-11 end
		} else if (fileCategory.equals(FileCategory.Apk)) {

			int i = fileInfos.indexOf(old);
			if (i != -1) {
				fileInfos.set(i, newFileInfo);
			}
		}
		unAllOperation();
		Message message = handler.obtainMessage();
		message.what = REFRESHCATEGORY;
		message.arg1 = REFRESHCATEGORY;
		handler.sendMessageDelayed(message, 300);

	}

	private boolean isLast = false;

	@Override
	public void onFileListQueryComplete(final Cursor cursor) {
		showLoadingImage(false);
		if (cursor == null) {
			handler.sendEmptyMessage(REFRESHCATEGORY);
			return;
		}
		if (cursorAsyncTask != null && cursorAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
			isLast = true;
			handler.removeMessages(LASTQUERY);
			Message msg = handler.obtainMessage();
			msg.what = LASTQUERY;
			msg.obj = cursor;
			handler.sendMessageDelayed(msg, 200);
			return;

		}
		isLast = false;
		cursorAsyncTask = new CursorAsyncTask();
		// cursorAsyncTask.execute(cursor);
		cursorAsyncTask.executeOnExecutor(getFULL_TASK_EXECUTOR(), cursor);

	}

	private CursorAsyncTask cursorAsyncTask;

	private List<FileInfo> tempInfos = new ArrayList<FileInfo>();

	private class CursorAsyncTask extends AsyncTask<Cursor, Void, Void> {

		@Override
		protected Void doInBackground(Cursor... params) {

			Cursor cursor = params[0];
			if (cursor.getCount() > 200) {
				handler.sendEmptyMessage(SHOWLOADING);
			}
			tempInfos.clear();
			if (!cursor.isClosed()) {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					if (isCancleTask || isCancelled()) {
						isCancleTask = false;
						return null;
					}

					FileInfo fileInfo = Util.getFileInfo(cursor, null);
					if (fileInfo != null) {
						tempInfos.add(fileInfo);
					} else {// 文件系统不存在这条数据 但是数据库存在
						LogUtil.e(TAG, "filePath is null and dell db id:" + FileColumns._ID);
						long id = cursor.getLong(cursor.getColumnIndex(FileColumns._ID));
						long ids[] = new long[] { id };
						getCategoryHelper().delete(ids);// 删除这条记录
					}
				}
				cursor.close();
			}
			if (fileCategory != null && !fileCategory.equals(FileCategory.Apk)) {
				fileSortHelper.setSortMethog(SortMethod.name);
				Collections.sort(tempInfos, fileSortHelper.getComparator());// 排序
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// 4.3以下 setAdapter要在addHeadView之后调用
			getAuroraListView().setAdapter(auroraFilesAdapter);
			if (isCancelled()) {
				return;
			}
			handler.sendEmptyMessage(REFRESHCATEGORY);
		}
	}

	// 标志是否打断该线程 取消任务
	private boolean isCancleTask = false;

	@Override
	public boolean onFragmentBack() {

		// LogUtil.elog(TAG, "onback 1");
		if (getFileExplorerActivity().showOperationBarMoreMenu) {
			getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
			return true;
		}
		// LogUtil.elog(TAG, "onback 2");
		if (getCategoryHelper() != null) {
			isCancleTask = true;
			getCategoryHelper().cancelQueryEvent(FileCategoryHelper.FILE_CATEGORY_DATAS_TOKEN);
		}

		// LogUtil.elog(TAG, "onback 4");
		if (auroraSetRubbishBack()) {
			return true;
		}
		// LogUtil.elog(TAG, "onback 5");
		// 获取视频返回处理
		if (fileCategory.equals(FileCategory.Video) && getFileExplorerActivity().getIsGetMoreVideo()) {
			if (privacyTask != null) {
				privacyTask.cancel(true);
			}
			getFileExplorerActivity().finish();
			return true;
		}
		// LogUtil.elog(TAG, "onback 6");
		if (unAllOperation()) {
			return true;
		}

		if (cursorAsyncTask != null) {
			cursorAsyncTask.cancel(true);
		}

		// LogUtil.elog(TAG, "onback 7");

		if (getActivityInfo() != null && getActivityInfo().name.endsWith(getString(R.string.activity_video))) {
			// 处理不是隐私帐号，跳入隐私界面。返回
			if (!getFileExplorerActivity().isPrivacy() && isPrivacyView) {
				getFileExplorerActivity().finish();
				return true;
			}
		}
		// 隐私返回数据处理
		if (getFileExplorerActivity().isPrivacy() && fileCategory.equals(FileCategory.Video) && isPrivacyView) {
			if (getActivityInfo() != null && getActivityInfo().name.endsWith(getString(R.string.activity_video))) {
				getFileExplorerActivity().finish();
				return true;
			}
			// 修改actionBar title
			getFileExplorerActivity().setAuroraActionBarTitle(getCategoryHelper().getCurCategoryNameResId());
			getAuroraListView().setAdapter(null);// 4.3及以下 setAdapter
			// 要在addHeaderView之后调用，所以这里先设置NULL
			getAuroraListView().addHeaderView(privacyView);
			if (privacyNum != null && privacyFiles != null) {
				privacyNum.setText(privacyFiles.size() + "");
			}
			fileInfos.clear();
			fileInfos.addAll(getFileExplorerActivity().getVideoList());
			if (privacyFiles != null && privacyFiles.size() > 0) {
				fileInfos.removeAll(privacyFiles);
			}
			getAuroraListView().setAdapter(auroraFilesAdapter);
			auroraFilesAdapter.setFileInfos(fileInfos);
			auroraFilesAdapter.notifyDataSetChanged();
			setPrivacyView(false);
			if (imageButton != null) {
				imageButton.setVisibility(View.GONE);
			}
			updateListViewState();
			return true;
		}

		getSelectPageInterface().selectPage(AuroraConfig.AURORA_HOME_PAGE, null);
		return true;
	}

	/**
	 * 重命名 删除后 刷新数据
	 */
	private void refreshState() {
		if (unAllOperation()) {
			if (getAuroraListView().getCount() != 0) {
				getFileExplorerActivity().showEditBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			} else {
				getFileExplorerActivity().showEmptyBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 获取 AuroraFilesAdapter hand 播放动画使用
	 * @return
	 */
	public AuroraFilesAdapter getAuroraFilesAdater() {
		if (isAdded()) {
			return auroraFilesAdapter;
		}
		return null;
	}

	/**
	 * 取消编辑状态
	 */
	public boolean unAllOperation() {
		if (isOperationFile()) {
			setOperationFile(false);
			getAuroraListView().auroraEnableSelector(true);
			getAuroraListView().auroraSetNeedSlideDelete(true);
			if (selectFiles != null) {
				selectFiles.clear();
				auroraFilesAdapter.setSelectFiles(selectFiles);
			}
			getFileExplorerActivity().hideDeleteMenu();
			// auroraFilesAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}

	public void unCAllOperation() {
		if (isAdded() && isOperationFile()) {
			setOperationFile(false);
			getFileExplorerActivity().hideDeleteMenu();
		}
	}

	private int position = -1;

	/**
	 * 选择ITEM
	 * @param position
	 * @param view
	 * @param fileInfo
	 * @param anmi
	 */
	private void operationAtion(int position, View view, FileInfo fileInfo, boolean anmi) {
		this.position = position;
		if (selectFiles == null) {
			selectFiles = new ArrayList<FileInfo>();
		}
		aurora.widget.AuroraCheckBox checkbox = ((AuroraFilesItemView) view.getTag()).getCheckBox();
		if (selectFiles.contains(fileInfo)) {
			selectFiles.remove(fileInfo);
			checkbox.auroraSetChecked(false, anmi);
		} else {
			if (!tooMorePrivicyVideoAddToast(getOperation().getSelectFiles())) {
				return;
			}
			selectFiles.add(fileInfo);
			checkbox.auroraSetChecked(true, anmi);
		}
		updateAuroraItemBottomBarState();
		updateAuroraitemActionBarState();
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
		if (isLastItemMove) {
			final int editHeight = (int) getFileExplorerActivity().getResources().getDimension(com.aurora.R.dimen.aurora_action_bottom_bar_height);
			final int fristBottom = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.frist_bottom)
					+ (int) getFileExplorerActivity().getResources().getDimension(R.dimen.list_top_navigation_scroll_pane);
			if (isLastitem || lastBottom - fristBottom < 0 && lastBottom - fristBottom + editHeight > 0) {
				isLastitem = false;
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						getAuroraListView().smoothScrollBy(lastBottom - fristBottom + editHeight, 0);
					}
				}, 100);
			}
		}

	}

	/**
	 * 点击activity中 actionBar 编辑按钮
	 */
	public void editShowView() {
		if (isAdded()) {
			if (getOperation().isCutOrCopy()) {
				ToastUtils.showTast(getFileExplorerActivity(), R.string.wait_a_moment);
				return;
			}
			if (isLoadingProgressBarVisibility()) {// 正在加载数据不让点击
				return;
			}
			if (getAuroraListView() != null && getAuroraListView().auroraIsRubbishOut()) {
				getAuroraListView().auroraSetRubbishBack();
				return;
			}
			setOperationFile(true);
			getAuroraListView().auroraEnableSelector(false);
			getAuroraListView().auroraSetNeedSlideDelete(false);
			showEditView();
			auroraFilesAdapter.isShowAnim();
			selectFiles.clear();
			updateAuroraItemBottomBarState();
			updateAuroraitemActionBarState();// paul add for BUG #15035
		}
	}

	@Override
	public void onLeftClick() {
		super.onLeftClick();
		unAllOperation();
	}

	@Override
	public void onRightClick() {
		super.onRightClick();

		if (selectFiles.size() == fileInfos.size()) {
			selectFiles.clear();
		} else {
			if (!tooMorePrivicyVideoAddToast(fileInfos)) {
				return;
			}
			selectFiles.clear();
			selectFiles.addAll(fileInfos);

		}

		updateAuroraItemBottomBarState();

	}

	/**
	 * 改变BottomBar 状态
	 */
	public void updateAuroraItemBottomBarState() {
		AuroraMenu auroraMenu = getAuroraActionBar().getAuroraActionBottomBarMenu();
		auroraFilesAdapter.setSelectFiles(selectFiles);
		getOperation().setSelectFiles(selectFiles);

		if (getFileExplorerActivity().getIsFromOtherAPP() && getFileExplorerActivity().getIsGetMoreVideo() && getFileExplorerActivity().getIsGetMoreNoPrivacy()) {
			if (selectFiles.size() == 0) {
				auroraMenu.setBottomMenuItemEnable(0, false);
			} else {
				auroraMenu.setBottomMenuItemEnable(0, true);
			}
		} else {
			if (selectFiles.size() == 0) {
				auroraMenu.setBottomMenuItemEnable(0, false);
				auroraMenu.setBottomMenuItemEnable(1, false);
				auroraMenu.setBottomMenuItemEnable(2, false);
				auroraMenu.setBottomMenuItemEnable(3, false);
				auroraMenu.setBottomMenuItemEnable(4, false);
				auroraMenu.getLayoutByPosition(4).setVisibility(View.VISIBLE);
			} else if (selectFiles.size() == 1) {
				auroraMenu.setBottomMenuItemEnable(0, true);
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				auroraMenu.setBottomMenuItemEnable(4, true);
				auroraMenu.getLayoutByPosition(4).setVisibility(View.VISIBLE);
			} else {
				auroraMenu.setBottomMenuItemEnable(0, true);
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				if (getFileExplorerActivity().isPrivacy() && getFileExplorerActivity().getNowfileCategory() != null && (getFileExplorerActivity().getNowfileCategory() == FileCategory.Video)) {
					auroraMenu.setBottomMenuItemEnable(4, true);
					auroraMenu.getLayoutByPosition(4).setVisibility(View.VISIBLE);
				} else {
					auroraMenu.setBottomMenuItemEnable(4, false);
					auroraMenu.getLayoutByPosition(4).setVisibility(View.GONE);
				}
			}
		}
		auroraFilesAdapter.notifyDataSetChanged();
	}

	/**
	 * 显示空数据 和Actionbar
	 */
	private void updateListViewState() {
		if (getAuroraListView().getCount() == 0) {
			getFileExplorerActivity().showEmptyBarItem();// 改变actionBar显示
			getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
		} else {
			if (isPrivacyView) {
				getFileExplorerActivity().showEmptyBarItem();// 改变actionBar显示
			} else {
				getFileExplorerActivity().showEditBarItem();// 改变actionBar显示
			}
			getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
		}
	}

	boolean isPrivacyBackToNormal = false;

	@Override
	public void completeRefresh(final List<FileInfo> addFileInfos, final List<FileInfo> removeInfos) {// 添加隐私视频后刷新
		if (!(getFileExplorerActivity().getCurrentFragment() instanceof FileCategoryFragment)) {
			return;
		}
		LogUtil.d(TAG, "------------completeRefresh");
		if (isPrivacyView && !getFileExplorerActivity().isPrivacyBackToNormal()) {
			isPrivacyBackToNormal = false;
			if (addFileInfos != null && addFileInfos.size() > 0) {
				fileInfos.removeAll(addFileInfos);
				fileInfos.addAll(addFileInfos);
				if (privacyFiles != null) {
					privacyFiles.removeAll(addFileInfos);
					privacyFiles.addAll(addFileInfos);
				}
				getFileExplorerActivity().getVideoList().removeAll(removeInfos);
			}
		} else {
			isPrivacyBackToNormal = true;
			if (getFileExplorerActivity().isPrivacyBackToNormal()) {
				getFileExplorerActivity().setPrivacyBackToNormal(false);
				ToastUtils.showTast(getFileExplorerActivity(), R.string.pri_back_movies);
				unAllOperation();
				getFileExplorerActivity().getVideoList().addAll(addFileInfos);
			} else {
				getFileExplorerActivity().setVideoList(fileInfos);
			}
		}
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (isPrivacyBackToNormal) {
					if (removeInfos != null && removeInfos.size() > 0) {
						fileInfos.removeAll(removeInfos);
					}
					if (privacyFiles != null) {
						privacyFiles.removeAll(removeInfos);

					}
				}
				auroraFilesAdapter.setFileInfos(fileInfos);
				auroraFilesAdapter.notifyDataSetChanged();
				updateListViewState();

			}
		}, 500);
		getFileExplorerActivity().refreshPrivacy(FileCategory.Video);

	}

	/** 隐私数据获取同步设置 **/
	private Object object = new Object();
	private boolean unLock = false;

	public void obNotifyAll() {
		if (isAdded()) {
			synchronized (object) {
				object.notifyAll();
				unLock = true;
				// LogUtil.log(TAG, " obNotifyAll unLock==" + unLock);
			}
		}
	}

	/** 隐私数据获取同步设置 **/

	private PrivacyTask privacyTask;

	private class PrivacyTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			ConcurrentHashMap<FileCategory, List<FileInfo>> map = getFileExplorerActivity().getHashMap();
			privacyFiles = map.get(FileCategory.Video);
			if (privacyFiles == null) {
				synchronized (object) {
					if (!unLock) {
						try {
							LogUtil.d(TAG, "object.wait()");
							object.wait();
						} catch (Exception e) {
							e.printStackTrace();
						}
						unLock = false;
					}
				}
				LogUtil.d(TAG, "object.wait() end");
				privacyFiles = map.get(FileCategory.Video);
			}
			if (privacyFiles != null) {
				try {
					fileInfos.clear();
					fileInfos.addAll(privacyFiles);
					fileSortHelper.setSortMethog(SortMethod.name);
					Collections.sort(fileInfos, fileSortHelper.getComparator());// 排序
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.e(TAG, e.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				return;
			}
			getAuroraListView().setAdapter(auroraFilesAdapter);
			auroraFilesAdapter.setFileInfos(fileInfos);
			auroraFilesAdapter.notifyDataSetChanged();
			showLoadingImage(false);
			updateListViewState();
		}

	}

	private ImageButton imageButton;

	// 隐私
	private void setAddMoreLister() {
		imageButton = (ImageButton) getAuroraActionBar().getRootView().findViewById(R.id.pri_add_more);
		if (imageButton == null) {
			return;
		}
		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// add by JXH 2014-7-22 begin BUG
				if (ButtonUtil.isFastDouble()) {// 快速点击
					return;
				}
				Intent mIntent = new Intent();
				mIntent.setAction(AuroraConfig.ACTION_MORE_VIDEO_CONTENT);
				mIntent.setType("video/*");
				Bundle bundle = new Bundle();
				bundle.putInt("size", 100);
				bundle.putBoolean("noPriacy", true);
				mIntent.putExtras(bundle);
				startActivityForResult(mIntent, 1);
			}
		});
	}

	private ButtonUtil buttonUtil = new ButtonUtil();

	/**
	 * 限制每次添加隐私视频提示
	 * @return
	 */
	private boolean tooMorePrivicyVideoAddToast(List<FileInfo> selects) {
		if (getFileExplorerActivity().isPrivacy() && getFileExplorerActivity().getNowfileCategory() != null && getFileExplorerActivity().getNowfileCategory().equals(FileCategory.Video)
				&& getActivityInfo() != null && getActivityInfo().name.endsWith(getString(R.string.privacy_more))) {
			if (selects == null) {
				return true;
			}
			if (getFileExplorerActivity().getVideoSize() <= selects.size()) {

				if (!buttonUtil.isFastClick(3000)) {
					String msg = String.format(getFileExplorerActivity().getString(R.string.get_more_video), getFileExplorerActivity().getVideoSize());
					ToastUtils.showTast(getFileExplorerActivity(), msg);
					return false;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 发送需要添加的隐私视频
	 */
	public void sendMoreVideo() {
		if (isAdded()) {
			List<FileInfo> selects = getOperation().getSelectFiles();
			ArrayList<String> paths = new ArrayList<String>();
			for (FileInfo info : selects) {
				paths.add(info.filePath);
			}
			IntentBuilder.getMoreVideo(getFileExplorerActivity(), paths);
		}
	}

}
