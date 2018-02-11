package com.aurora.filemanager.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchBackgroundClickListener;
import aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraSearchView;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.CategoryInfo;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.filemanager.R;
import com.aurora.filemanager.AuroraStorageDetailActivity;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.OperationAction;
import com.aurora.tools.Util;
import com.aurora.tools.Util.SDCardInfo;
import com.aurora.widget.AuroraMainView;
import com.aurora.widget.SearchAdapter;

public class AuroraMainFragment extends AuroraFragment {

	private static final String TAG = "AuroraMainFragment";
	private AuroraMainView auroraMainView;
	private String mSearchKey;
	private SearchAdapter searchAdapter;
	private AuroraSearchView mSearchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.aurora_file_manager_main_page, container, false);
		auroraMainView = new AuroraMainView(rootView);
		if (getFileExplorerActivity().ismHasNaviBar()) {
			try {
				update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initEvent();
		mSearchKey = getFileExplorerActivity().getSearchKey();
		getFileExplorerActivity().initCategoryInfo();
		if (mSearchKey != null && !mSearchKey.equals("")) {
			backToSearchView();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		setFileCategoryInfo();
		getFileExplorerActivity().setAuroraActionBarTitle(R.string.aurora_file_manager_title);
		getFileExplorerActivity().showMoreBarItem();
		getFileExplorerActivity().showSdinfo();
		getFileExplorerActivity().setMenuEnable(false);
		// modify by Jxh 2014-8-30 begin// 搜索浏览图片删除图片从新查询数据更新
		if (getAuroraListView() != null && getAuroraListView().getVisibility() == View.VISIBLE) {
			if (mSearchKey != null) {
				if (searchAdapter != null) {
					searchAdapter.changeCursor(null);
				}
				getFileExplorerActivity().getSearchView().setQuery(mSearchKey, false);
				// 清除过时搜索数据
				getFileExplorerActivity().setSearchKey(null);
				getFileExplorerActivity().setSearchPath(null);
				startSearch();
			}
		}
		// modify by Jxh 2014-8-30 end
	}

	@Override
	public void onDestroy() {
		auroraMainView = null;
		searchAdapter = null;
		super.onDestroy();
	}

	/**
	 * 初始化所有事件
	 */
	public void initEvent() {
		setupClick();
	}

	/**
	 * 搜索事件
	 */
	private void searchEvent() {
		setAuroraListView((AuroraListView) getView().findViewById(R.id.search_result_list));
		searchAdapter = new SearchAdapter(searchCursor, getFileExplorerActivity());
		getAuroraListView().setAdapter(searchAdapter);

		// 搜索查询监听
		getFileExplorerActivity().setOnQueryTextListener(new OnSearchViewQueryTextChangeListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (getAuroraListView().getVisibility() == View.GONE) {
					getAuroraListView().setVisibility(View.VISIBLE);
				}
				mSearchKey = newText;
				if (mSearchKey.trim().equals("")) {
					if (searchAdapter != null) {
						searchAdapter.changeCursor(null);
					}
					return true;
				}
				startSearch();
				return true;
			}
		});
		// 搜索退出监听
		getFileExplorerActivity().setOnSearchViewQuitListener(new OnSearchViewQuitListener() {

			@Override
			public boolean quit() {
				// paul add for BUG #14800
				if (null == auroraMainView) {
					return true;
				}
				getAuroraListView().setVisibility(View.GONE);
				auroraMainView.getCategory_page().setVisibility(View.VISIBLE);
				return true;
			}
		});
		// 搜索点击搜索界面蒙版监听
		getFileExplorerActivity().setOnSearchBackgroundClickListener(new OnSearchBackgroundClickListener() {

			@Override
			public boolean searchBackgroundClick() {
				getAuroraListView().setVisibility(View.GONE);
				hideSearchviewLayout();
				return true;
			}
		});
		Button searchCancel = (Button) getFileExplorerActivity().getSearchViewRightButton();
		searchCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getAuroraListView().setVisibility(View.GONE);
				hideSearchviewLayout();
			}
		});
		getAuroraListView().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				getAuroraListView().requestFocus();
				return false;
			}
		});
		getAuroraListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cur = (Cursor) searchAdapter.getItem(position);
				final String path = cur.getString(FileCategoryHelper.COLUMN_PATH);
				if (path != null) {
					File file = new File(path);
					if (file.exists()) {
						// add by Jxh 2014-8-25 search back position begin
						getFileExplorerActivity().setSearchPostion(getAuroraListView().getFirstVisiblePosition());
						// add by Jxh 2014-8-25 search back position end
						if (file.isDirectory()) {
							getFileExplorerActivity().setSearchKey(mSearchKey);
							// 路径处理
							// LogUtil.log(TAG, "mSearchKey==" + mSearchKey);
							getHandler().postDelayed(new Runnable() {

								@Override
								public void run() {
									hideSearchviewLayout();
								}
							}, 100);
							getFileExplorerActivity().showFileViewPath(path);
						} else {
							IntentBuilder.viewFile(getFileExplorerActivity(), path);
						}
					}
				}
			}
		});

		// add by Jxh 2014-8-26 begin 添加显示文件详情功能
		getAuroraListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cur = (Cursor) searchAdapter.getItem(position);
				final String path = cur.getString(FileCategoryHelper.COLUMN_PATH);
				if (path != null) {
					File file = new File(path);
					if (file.exists()) {
						getFileExplorerActivity().setSearchKey(mSearchKey);
						getFileExplorerActivity().setSearchPostion(getAuroraListView().getFirstVisiblePosition());
						OperationAction operationAction = getFileExplorerActivity().operationAction;
						operationAction.onOperationInfo(Util.getFileInfo(file, false), true);
					}
				}
				return true;
			}
		});
		// add by Jxh 2014-8-26 end 添加显示文件详情功能
		// add by JXH 2014-7-14 begin
		getAuroraListView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (null == searchAdapter) {// paul add for BUG #14800
					return;
				}
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					searchAdapter.setLoadIcon(false);
				} else {
					searchAdapter.setLoadIcon(true);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
		// add by JXH 2014-7-14 end
	}

	/**
	 * 搜索
	 */
	private void startSearch() {
		String key = mSearchKey.trim();
		if (key == null || key.equals("")) {
			return;
		}
		if (key.matches(".*[/\\\\:*?\"<>|'].*")) {
			Toast.makeText(getFileExplorerActivity(), getFileExplorerActivity().getString(R.string.invalid_char_prompt), Toast.LENGTH_SHORT).show();
			return;
		}
		key = key.replace("%", "\\%");
		getCategoryHelper().searchQuery(mSearchKey);
	}

	/**
	 * 返回到原来到搜索界面
	 */
	public void backToSearchView() {
		if (getAuroraListView() == null) {
			auroraMainView.getSearchViewStub().inflate();
			searchEvent();
		}
		getFileExplorerActivity().showEmptyBarItem();
		auroraMainView.getCategory_page().setVisibility(View.GONE);
		getFileExplorerActivity().showSearchviewLayout();
		mSearchKey = getFileExplorerActivity().getSearchKey();
		// 返回时 设置前一次搜索数据
		getFileExplorerActivity().getSearchView().setQuery(mSearchKey, true);
		// add by JXH 2014-7-14 begin 返回时通过取消edit焦点达到取消键盘
		getFileExplorerActivity().getSearchView().clearEditFocus();
		// add by JXH 2014-7-14 end
		// 清除过时搜索数据
		getFileExplorerActivity().setSearchKey(null);
		getFileExplorerActivity().setSearchPath(null);

	}

	/**
	 * 初始化点击监听
	 */
	private void setupClick() {
		setupClick(R.id.category_music, 1);
		setupClick(R.id.category_video, 1);
		setupClick(R.id.category_picture, 1);
		setupClick(R.id.category_document, 1);
		setupClick(R.id.category_apk, 1);
		setupClick(R.id.category_download, 1);
		setupClick(R.id.usb_card_storage_single, 0);
		setupClick(R.id.goto_search_mode, 0);
	}

	/**
	 * 如果还有统计任务没有开始 则结束该任务
	 */
	public void cancelQueryTask() {
		if (isAdded()) {
			getCategoryHelper().cancelQueryEvent(FileCategoryHelper.FILE_CATEGORY_NUM_TOKEN);

		}
	}

	/**
	 * 点击是否添加动画
	 * @param id
	 * @param typeAnim
	 */
	private void setupClick(int id, int typeAnim) {
		final View button = getView().findViewById(id);
		button.setOnClickListener(onClickListener);
		if (typeAnim == 1) {
			button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						button.setScaleX((float) 1.0);
						button.setScaleY((float) 1.0);
						button.setPivotX(button.getWidth() / 2);
						button.setPivotX(button.getHeight() / 2);
						break;
					case MotionEvent.ACTION_DOWN:
						button.setScaleX((float) 0.97);
						button.setScaleY((float) 0.97);
						button.setPivotX(button.getWidth() / 2);
						button.setPivotX(button.getHeight() / 2);
						break;

					default:
						break;
					}
					return false;
				}
			});
		}
	}

	ButtonUtil buttonUtil = new ButtonUtil();
	View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (buttonUtil.isFastDoubleClick()) {// 防止短时间内(500ms) 点击其他
				return;
			}
			if (getSelectPageInterface() == null) {
				return;
			}
			cancelQueryTask();

			if (v.getId() == R.id.goto_search_mode) {// 搜索监听这里处理 否则导致储存详情按钮动画异常
				auroraMainView.getCategory_page().setVisibility(View.GONE);
				getFileExplorerActivity().showSearchviewLayout();
				setStatistics(1);
				if (getAuroraListView() == null) {
					auroraMainView.getSearchViewStub().inflate();
					searchEvent();
				}
				return;
			}
			getFileExplorerActivity().showEmptyBarItem();// 点击时 隐藏储存详情按钮

			switch (v.getId()) {
			case R.id.category_music:
				setStatistics(4);
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Music);
				break;
			case R.id.category_download:
				Intent intent = new Intent(AuroraConfig.DOWNLOADACTION);
				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				getFileExplorerActivity().overridePendingTransition(0, 0);
				break;
			case R.id.category_apk:
				setStatistics(5);
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Apk);
				break;
			case R.id.category_document:
				setStatistics(6);
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Doc);
				break;
			case R.id.category_picture:
				setStatistics(2);
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_PIC_PAGE, null);
				break;
			case R.id.category_video:
				setStatistics(3);
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_CATEGORY_PAGE, FileCategory.Video);
				break;
			case R.id.usb_card_storage_single:
				setStatistics(7);
				getFileExplorerActivity().setRootPath(FileExplorerTabActivity.getmSDCardPath());
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				break;
			case R.id.usb_card_storage:
				setStatistics(7);
				getFileExplorerActivity().setRootPath(FileExplorerTabActivity.getmSDCardPath());
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				break;
			case R.id.sd_card_storage_rela:
				getFileExplorerActivity().setRootPath(FileExplorerTabActivity.getmSDCard2Path());
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_FILE_PAGE, null);
				break;
			}

		}
	};

	/**
	 * 设置分类统计数据
	 */
	public void setFileCategoryInfo() {
		if (isAdded()) {
			setFileCategoryInfo(FileCategory.DownLoad);
			setFileCategoryInfo(FileCategory.Music);
			setFileCategoryInfo(FileCategory.Picture);
			setFileCategoryInfo(FileCategory.Video);
			setFileCategoryInfo(FileCategory.Apk);
			setFileCategoryInfo(FileCategory.Doc);
		}
	}

	/**
	 * 设置分类统计数据
	 * @param fc
	 */
	private void setFileCategoryInfo(FileCategory fc) {
		CategoryInfo categoryInfo = getFileExplorerActivity().categoryHelper.getCategoryInfo(fc);
		if (categoryInfo == null) {// 存储器不可用
			return;
		}
		long count = categoryInfo.count;
		if (getFileExplorerActivity().isPrivacy()) {
			List<FileInfo> infos = getFileExplorerActivity().getHashMap().get(fc);
			if (infos != null) {
				count += infos.size();
			}
		}
		auroraMainView.setFileCategoryInfo(fc, count);
	}

	private Cursor searchCursor;
	private SearchSort searchSort;

	@Override
	public void onFileListQueryComplete(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		if (cursor.getCount() == 0) {
			searchAdapter.changeCursor(null);
			searchAdapter.notifyDataSetChanged();
		}
		if (searchSort != null && searchSort.getStatus() == AsyncTask.Status.RUNNING) {
			searchSort.cancel(true);
		}
		searchSort = new SearchSort();
		searchSort.execute(cursor);
	}

	public class SearchSort extends AsyncTask<Cursor, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Cursor... params) {
			Cursor cursor = params[0];
			// 搜索返回
			searchCursor = getCategoryHelper().matrixCursorFromCursor(cursor, mSearchKey, searchSort);
			return searchCursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (isCancelled() || (null == searchAdapter)) {// paul add for BUG
															// #14800
				return;
			}
			if (result.getCount() > 0) {
				getAuroraListView().setVisibility(View.VISIBLE);
				if (searchAdapter != null) {
					searchAdapter.changeCursor(result);
					getAuroraListView().setSelection(0);
					if (mSearchKey.trim().equals("")) {
						if (searchAdapter != null) {
							searchAdapter.changeCursor(null);
						}
					}
				}
			} else {
				getAuroraListView().setVisibility(View.GONE);
			}
			// add by Jxh 2014-8-25 search back position begin
			getAuroraListView().setSelection(getFileExplorerActivity().getSearchPostion());
			getFileExplorerActivity().setSearchPostion(0);
			// add by Jxh 2014-8-25 search back position end
		}

	}

	@Override
	public boolean onBack() {
		LogUtil.d(TAG, "onBack()");
		if (getFileExplorerActivity() != null && getFileExplorerActivity().isSearchviewLayoutShow()) {
			hideSearchviewLayout();
			return true;
		}
		cancelQueryTask();
		return false;
	}

	private long sdTotal = 0;
	private long sdUse = 0;

	/**
	 * 重置储存数据
	 */
	public void setIntegerInit() {
		sdUse = 0;
		sdTotal = 0;
	}

	/**
	 * 设置存储器信息
	 * @param cardInfo
	 */
	public void setCardInfo(String path) {
		if (isAdded()) {
			SDCardInfo cardInfo = Util.getSDCardInfo(true, path);
			if (cardInfo == null) {
				return;
			}

			if (FileExplorerTabActivity.getmSDCardPath() != null && FileExplorerTabActivity.getmSDCardPath().equals(path)) {
				auroraMainView.getUsb_card_storage_size().setText("(" + Util.convertStorage(cardInfo.inUse) + "/" + Util.convertStorage(cardInfo.total) + ")");
				auroraMainView.getUsb_card_storage_size_2().setText("(" + Util.convertStorage(cardInfo.inUse) + "/" + Util.convertStorage(cardInfo.total) + ")");
			} else {
				sdTotal += cardInfo.total;
				sdUse += cardInfo.inUse;
				auroraMainView.getSd_card_storage_size().setText("(" + Util.convertStorage(sdUse) + "/" + Util.convertStorage(sdTotal) + ")");
			}
		}
	}

	/**
	 * 隐藏或则显示SD卡图标
	 * @param show
	 */
	public void showSdcard(boolean show) {
		if (isAdded()) {
			if (show) {
				setIntegerInit();
				auroraMainView.getUsb_card_storage_single().setVisibility(View.GONE);
				if (auroraMainView.getSd_card_storage() == null) {
					auroraMainView.getSdViewStub().inflate();
				}
				auroraMainView.getSd_card_storage().setVisibility(View.VISIBLE);
				setupClick(R.id.usb_card_storage, 0);
				setupClick(R.id.sd_card_storage_rela, 0);
			} else {
				auroraMainView.getUsb_card_storage_single().setVisibility(View.VISIBLE);
				if (auroraMainView.getSd_card_storage() == null) {
					auroraMainView.getSdViewStub().inflate();
				}
				auroraMainView.getSd_card_storage().setVisibility(View.GONE);
			}
			viewParams = auroraMainView.getBottomView().getLayoutParams();
			if (getFileExplorerActivity().getStoragesStrings().size() > 1) {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom_two);

			} else {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom);
			}
			auroraMainView.getBottomView().setLayoutParams(viewParams);

		}
	}

	private ViewGroup.LayoutParams viewParams;

	public void update() throws Exception {
		boolean hide = Settings.System.getInt(getFileExplorerActivity().getContentResolver(), FileExplorerTabActivity.NAVI_KEY_HIDE, 0) != 0;
		viewParams = auroraMainView.getBottomView().getLayoutParams();
		if (hide) {
			if (getFileExplorerActivity().getStoragesStrings().size() > 1) {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom_two);
			} else {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom);
			}

		} else {
			if (getFileExplorerActivity().getStoragesStrings().size() > 1) {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom_two_u3);
			} else {
				viewParams.height = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.view_bottom_u3);
			}

		}
		auroraMainView.getBottomView().setLayoutParams(viewParams);
	}

}
