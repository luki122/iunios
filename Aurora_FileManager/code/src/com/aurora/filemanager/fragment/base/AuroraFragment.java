package com.aurora.filemanager.fragment.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.FileCategoryFragment;
import com.aurora.filemanager.fragment.FileViewFragment;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.fragment.PictureFragment;
import com.aurora.filemanager.inter.IActionOnClickListener;
import com.aurora.filemanager.inter.OperationInterfaceLisenter;
import com.aurora.filemanager.inter.SelectPageInterface;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.OperationAction;
import com.aurora.tools.Util;
import com.aurora.widget.AuroraLoadAndEmptyView;
import com.aurora.filemanager.R;

public class AuroraFragment extends Fragment implements IBackPressedListener, onFileCategoryInfoChangedLisenter, OperationInterfaceLisenter, IActionOnClickListener {
	/**
	 * Control last item move
	 */
	public boolean isLastItemMove = false;

	private static final String TAG = "AuroraFragment";
	private FileExplorerTabActivity activity;
	private SelectPageInterface selectPageInterface;
	private FileCategoryHelper categoryHelper;
	private AuroraListView auroraListView;
	private GridView gridView;
	protected boolean isSearchviewLayoutShow;// for OS 2.0

	private TextView leftView, rightView;

	private AuroraLoadAndEmptyView loadAndEmptyView;

	protected List<FileInfo> fileInfos = new ArrayList<FileInfo>();
	protected List<FileInfo> selectFiles = new ArrayList<FileInfo>();

	private Handler handler = new Handler() {};

	private boolean isOperationFile;

	/**
	 * @return the isOperationFile
	 */
	public boolean isOperationFile() {
		return isOperationFile;
	}

	/**
	 * @param isOperationFile
	 *            the isOperationFile to set
	 */
	public void setOperationFile(boolean isOperationFile) {
		this.isOperationFile = isOperationFile;
	}

	/**
	 * use onAttach after
	 * @return
	 */
	public FileExplorerTabActivity getFileExplorerActivity() {
		if (activity == null) {
			LogUtil.e(TAG, "activity is null");
			activity = (FileExplorerTabActivity) getActivity();
			if (activity == null) {
				LogUtil.e(TAG, "activity is null !!!!");
			}
		}
		return activity;
	}

	/**
	 * use onAttach after
	 * @return
	 */
	public SelectPageInterface getSelectPageInterface() {
		return selectPageInterface;
	}

	public TextView getRightView() {
		return rightView;
	}

	/**
	 * use onAttach after
	 * @return
	 */
	public FileCategoryHelper getCategoryHelper() {
		if (categoryHelper == null) {
			categoryHelper = new FileCategoryHelper(this, getFileExplorerActivity());
		}
		return categoryHelper;
	}

	public AuroraListView getAuroraListView() {
		return auroraListView;
	}

	public void setAuroraListView(AuroraListView auroraListView) {
		this.auroraListView = auroraListView;
	}

	public GridView getGridView() {
		return gridView;
	}

	public void setGridView(GridView gridView) {
		this.gridView = gridView;
	}

	public OperationAction getOperation() {
		return getFileExplorerActivity().operationAction;
	}

	public AuroraActionBar getAuroraActionBar() {
		return getFileExplorerActivity().auroraActionBar;
	}

	public ExecutorService getFULL_TASK_EXECUTOR() {
		return getFileExplorerActivity().getFULL_TASK_EXECUTOR();
	}

	/**
	 * use onAttach after
	 * @return
	 */
	public ActivityInfo getActivityInfo() {
		return getFileExplorerActivity().getActivityInfo();
	}

	public Fragment getCurrentFragment() {
		return getFileExplorerActivity().getCurrentFragment();
	}

	public Handler getHandler() {
		return handler;
	}

	public FileCategory getNowfileCategory() {
		return getFileExplorerActivity().getNowfileCategory();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		selectPageInterface = (SelectPageInterface) activity;
		this.activity = (FileExplorerTabActivity) activity;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.activity = (FileExplorerTabActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
		if (getCurrentFragment() instanceof FileCategoryFragment || getCurrentFragment() instanceof PictureFragment || getCurrentFragment() instanceof PictureCategoryFragment
				|| getCurrentFragment() instanceof FileViewFragment) {
			actionBarSetOnClickListener();
		}
		initEventListeners();

	}

	@Override
	public void deleteComplete(List<FileInfo> ids) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void folderDelet(FileInfo fileInfo) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void renameComplete(FileInfo old, FileInfo newFileInfo) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void completeRefresh(List<FileInfo> addFileInfos, List<FileInfo> removeInfos) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void onFileCategoryInfoChanged(FileCategory fc) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void onFileListQueryComplete(Cursor cursor) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void onLeftClick() {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void onRightClick() {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void auroraDelOnClick(int position) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void auroraDeleteItemView(View view, int position) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (!isAdded()) {
			return false;
		}
		return false;
	}

	@Override
	public void auroraOnScrollStateChanged(AbsListView view, int scrollState) {

	}

	/**
	 * 事件监听
	 */
	private void initEventListeners() {

		if (auroraListView != null) {
			auroraListView.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {

				@Override
				public void auroraPrepareDraged(int position) {

				}

				@Override
				public void auroraOnClick(int position) {
					if (!Util.isFastDoubleClick()) {
						auroraDelOnClick(position);
					}
				}

				@Override
				public void auroraDragedUnSuccess(int position) {

				}

				@Override
				public void auroraDragedSuccess(int position) {

				}
			});
			auroraListView.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {

				@Override
				public void auroraDeleteItem(View view, int position) {
					auroraDeleteItemView(view, position);
				}
			});
			auroraListView.setOnItemClickListener(itemClickListener);
			auroraListView.setOnItemLongClickListener(itemLongClickListener);
		} else if (gridView != null) {
			gridView.setOnItemClickListener(itemClickListener);
			gridView.setOnItemLongClickListener(itemLongClickListener);
		}

	}

	private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			return auroraOnItemLongClick(parent, view, position, id);
		}
	};

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			auroraOnItemClick(parent, view, position, id);
		}
	};

	/**
	 * 分类左右actionBar监听
	 */
	private void actionBarSetOnClickListener() {
		leftView = (TextView) getAuroraActionBar().getSelectLeftButton();
		rightView = (TextView) getAuroraActionBar().getSelectRightButton();
		rightView.setText(R.string.all_select);
		leftView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isAdded()) {
					return;
				}
				if (isOperationFile() && getFileExplorerActivity().getIsFromOtherAPP() && getFileExplorerActivity().getIsGetMoreVideo() && (getCurrentFragment() instanceof FileCategoryFragment)) {
					onBack();
					return;
				}
				if (!activity.actionBarIsAnimRunning()) {
					onLeftClick();
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isAdded()) {
					return;
				}
				if (getCurrentFragment() instanceof PictureCategoryFragment) {
					((PictureCategoryFragment) getCurrentFragment()).setItemPicAnim(false);
				} else if (getCurrentFragment() instanceof PictureFragment) {
					((PictureFragment) getCurrentFragment()).setItemPicAnim(false);
				}

				if (!activity.actionBarIsAnimRunning()) {
					onRightClick();
					updateAuroraitemActionBarState();
				}
			}
		});
	}

	protected void hideSearchviewLayout() {
		getFileExplorerActivity().hideSearchviewLayout();
		isSearchviewLayoutShow = false;
	}

	@Override
	public void onResume() {
		if (auroraListView != null) {
			if ((activity.getCurrentFragment() instanceof FileViewFragment) || (activity.getCurrentFragment() instanceof FileCategoryFragment)) {
				auroraListView.auroraOnResume();
			}
		}
		super.onResume();
	}

	private String getCurrentFragmentName() {
		if (activity.getCurrentFragment() instanceof FileViewFragment) {
			return "FileViewFragment";
		} else if (activity.getCurrentFragment() instanceof PictureCategoryFragment) {
			return "PictureCategoryFragment";
		} else if (activity.getCurrentFragment() instanceof PictureFragment) {
			return "PictureFragment";
		} else if (activity.getCurrentFragment() instanceof FileCategoryFragment) {
			return getNowfileCategory() + " FileCategoryFragment";
		}
		return "AuroraFragment";
	}

	@Override
	public void onPause() {
		if (auroraListView != null) {
			if ((activity.getCurrentFragment() instanceof FileViewFragment) || (activity.getCurrentFragment() instanceof FileCategoryFragment)) {
				auroraListView.auroraOnPause();
			}
		}
		super.onPause();

	}

	@Override
	public boolean onBack() {
		LogUtil.d(TAG, "-------onBack");
		if (!isAdded()) {
			LogUtil.e(TAG, "onback return false  isAdded() is false");
			return false;
		}
		if (activity == null) {
			return false;
		}
		// LogUtil.elog(TAG, "onBack() 0");
		if (activity.actionBarIsAnimRunning()) {// 退出动画状态
			return true;
		}
		return onFragmentBack();
	}

	private ProgressBar mLoadingProgressBar;

	/**
	 * 是否显示加载进度条
	 * @param isShow
	 */
	public void showLoadingImage(boolean isShow) {
		if (!isAdded()) {
			return;
		}
		if (loadAndEmptyView == null) {
			loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
		}
		if (mLoadingProgressBar == null) {
			mLoadingProgressBar = loadAndEmptyView.getLoadingProgressbar();
		}
		if (isShow) {
			if (activity.getCurrentFragment() instanceof FileViewFragment) {
				auroraListView.auroraSetNeedSlideDelete(false);
				activity.setMenuEnable(false);
			}
			mLoadingProgressBar.setVisibility(View.VISIBLE);
		} else {
			if (activity.getCurrentFragment() instanceof FileViewFragment) {
				activity.setMenuEnable(true);
				auroraListView.auroraSetNeedSlideDelete(true);
			}
			mLoadingProgressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * use after {@link AuroraFragment#showLoadingImage(boolean)}
	 * @return
	 */
	public AuroraLoadAndEmptyView getLoadAndEmptyView() {
		return loadAndEmptyView;
	}

	/**
	 * use after {@link AuroraFragment#showLoadingImage(boolean)}
	 * @return
	 */
	public boolean isLoadingProgressBarVisibility() {
		if (mLoadingProgressBar != null && mLoadingProgressBar.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	public boolean auroraSetRubbishBack() {
		if (getAuroraListView() != null && getAuroraListView().auroraIsRubbishOut()) {
			getAuroraListView().auroraSetRubbishBack();
			return true;
		}
		return false;
	}

	// add by JXH 2014-8-6 begin
	public void auroraSetRubbishBackNoAnim() {
		if (getAuroraListView() != null && getAuroraListView().auroraIsRubbishOut()) {
			getAuroraListView().auroraSetRubbishBackNoAnim();
		}
	}

	// add by JXH 2014-8-6 end

	protected void updateAuroraitemActionBarState() {
		if (isAdded()) {
			if (selectFiles.size() == fileInfos.size() && rightView != null) {
				rightView.setText(getResources().getString(R.string.invert_select));
			} else if (rightView != null) {
				rightView.setText(getResources().getString(R.string.all_select));
			}
			if(!(this instanceof PictureCategoryFragment)){
				getAuroraActionBar().getMiddleTextView().setText(String.format(getResources().getString(R.string.select_num, selectFiles.size())));
			}
		}
	}

	@Override
	public void onCompleteRefresh(List<FileInfo> addFileInfos, List<FileInfo> removeInfos) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onFragmentBack() {

		return false;
	}

	public void setStatistics(int position) {
		if (activity == null) {
			LogUtil.e(TAG, "activity is null");
			return;
		}
		activity.setStatistics(getCategoryHelper(), position);
	}

}
