package com.aurora.filemanager.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.dbutil.FileCategoryHelper.onFileCategoryInfoChangedLisenter;
import com.aurora.filemanager.FileApplication;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.filemanager.R;
import com.aurora.tools.AnalysisUtils;
import com.aurora.tools.LogUtil;
import com.aurora.tools.ButtonUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.IntentBuilder;
import com.aurora.tools.OperationAction;
import com.aurora.tools.ThreadPoolExecutorUtils;
import com.aurora.tools.ToastUtils;
import com.aurora.tools.Util;
import com.aurora.tools.FileSortHelper.SortMethod;
import com.aurora.tools.OperationAction.Operation;
import com.aurora.widget.AuroraLoadAndEmptyView;
import com.aurora.widget.AuroraOperationBarMoreMenu;
import com.aurora.widget.AuroraPicItemAdapter;

/**
 * 这里显示 每一个图片文件夹里面 图片大图
 * @author jiangxh
 * @CreateTime 2014年5月4日 上午10:27:15
 * @Description com.aurora.filemanager.fragment PictrueFragment.java
 */
public class PictureFragment extends AuroraFragment {
	private AuroraPicItemAdapter auroraPicItemAdapter;
	private static final String TAG = "PictureFragment";
	private ArrayList<String> picPath = new ArrayList<String>();

	/**
	 * 用于控制图片右上角选择动画，点击position
	 */
	private int selectPosition = -1;

	public int getSelectPosition() {
		return selectPosition;
	}

	public void setSelectPosition(int selectPosition) {
		this.selectPosition = selectPosition;
	}

	/**
	 * 用于判断是否点击图片右上角选择动画
	 */
	private boolean isItemPicAnim;

	public boolean isItemPicAnim() {
		return isItemPicAnim;
	}

	public void setItemPicAnim(boolean isItemPicAnim) {
		this.isItemPicAnim = isItemPicAnim;
	}

	private boolean isPrivacy;

	public boolean isPrivacy() {
		return isPrivacy;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fileInfos.clear();
		fileInfos = getFileExplorerActivity().getPicItemList();
		LogUtil.e(TAG, "fileInfos==" + fileInfos);
		if (fileInfos != null && fileInfos.size() > 0) {
			String temp;
			String tempPath = fileInfos.get(0).filePath;
			if (!TextUtils.isEmpty(tempPath)) {
				temp = getFileExplorerActivity().getStorageName(tempPath);
				temp = Util.getPathFromFilepath(temp);
				tempPath = Util.getPathFromFilepath(tempPath);
				if (temp.endsWith("DCIM")) {
					setStatistics(9);
				}
				String title = temp.substring(temp.lastIndexOf("/") + 1, temp.length());
				if (this.getFileExplorerActivity().isPrivacy() && tempPath.equals(FileExplorerTabActivity.getPrivacyMedioPath(FileCategory.Picture))) {
					title = getFileExplorerActivity().getString(R.string.p_picture);
					this.getFileExplorerActivity().setHideOperationBarMoreMenu(true);
					isPrivacy = true;

				} else {
					this.getFileExplorerActivity().setHideOperationBarMoreMenu(false);
					isPrivacy = false;
				}
				if (this.getFileExplorerActivity().getIsGetMorePic()) {
					TextView textView = getAuroraActionBar().getTitleView();
					if (textView != null) {
						int length = (int) getFileExplorerActivity().getResources().getDimension(R.dimen.pic_title);
						title = (String) Util.getEllipsizeEnd(title, textView, length);
					}
				}
				this.getFileExplorerActivity().setAuroraActionBarTitle(title);
			}
		} else if (!TextUtils.isEmpty(this.getFileExplorerActivity().getPicPath())
				&& this.getFileExplorerActivity().getPicPath().equals(FileExplorerTabActivity.getPrivacyMedioPath(FileCategory.Picture)) && this.getFileExplorerActivity().isPrivacy()) {
			String title = getFileExplorerActivity().getString(R.string.p_picture);
			this.getFileExplorerActivity().setHideOperationBarMoreMenu(true);
			isPrivacy = true;
			this.getFileExplorerActivity().setAuroraActionBarTitle(title);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_category_item_pic, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setGridView((GridView) getView().findViewById(R.id.picListView));
		super.onActivityCreated(savedInstanceState);
		getAuroraActionBar().initActionBottomBarMenu(R.menu.aurora_operation_bar_more_menu, 5);
		if (fileInfos == null) {// getPicItemList() maybe is null
			fileInfos = new ArrayList<FileInfo>();
		}
		auroraPicItemAdapter = new AuroraPicItemAdapter(fileInfos, getFileExplorerActivity(), this);
		getGridView().setAdapter(auroraPicItemAdapter);

		showLoadingImage(false);
		if (getActivityInfo().name.endsWith(getString(R.string.activity_picture))) {

			String title = getFileExplorerActivity().getString(R.string.p_picture);
			getFileExplorerActivity().setHideOperationBarMoreMenu(true);
			isPrivacy = true;
			showLoadingImage(true);
			getFileExplorerActivity().setAuroraActionBarTitle(title);
			getFileExplorerActivity().setPicItemRefresh(true);
		}

		if (!isPrivacy) {
			if (getGridView().getCount() != 0) {
				getFileExplorerActivity().showEditBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
			} else {
				// LogUtil.log(TAG, "getEmptyView() VISIBLE");
				getFileExplorerActivity().showEmptyBarItem();
				getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
			}
		}
		// 每次进入更新状态 防止在多选状态时界面显示错误
		setOperationFile(false);
		selectFiles = getFileExplorerActivity().getSelectFiles();

		// 多选图
		if (getFileExplorerActivity().getIsFromOtherAPP() && getFileExplorerActivity().getIsGetMorePic()) {
			getAuroraActionBar().changeItemLayout(R.layout.aurora_more_pic, R.id.pic_more_liner);
			setOperationFile(true);
			actionBarMorePicSetOnClickListener();
			// add by Jxh 2014-8-11 begin
			picPath = getFileExplorerActivity().getSelectPicPath();

			if (picPath == null) {
				picPath = new ArrayList<String>();
			}
			updateAuroraItemBottomBarState();
			// add by Jxh 2014-8-11 end
			changeActionBarTextView();
		}

		auroraPicItemAdapter.setSelects(selectFiles);

		// add by Jxh 2014-8-30 begin 根据条件刷新数据
		if (getFileExplorerActivity().isPicItemRefresh()) {
			if (isPrivacy && getFileExplorerActivity().isPrivacy()) {
				fileInfos = getFileExplorerActivity().getHashMap().get(FileCategory.Picture);
				refreshState(false);
			} else {
				refreshCategoryPics();
			}
			getFileExplorerActivity().setPicItemRefresh(false);
		}
		// add by Jxh 2014-8-30 end 根据条件刷新数据
	}

	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);

		if (isOperationFile() && !getFileExplorerActivity().getIsFromOtherAPP()) {
			operationPicture(position);
			return;
		}
		if (getFileExplorerActivity().getIsFromOtherAPP()) {
			FileInfo fileInfo = fileInfos.get(position);
			if (fileInfo != null) {
				String path = fileInfo.filePath;
				if (!TextUtils.isEmpty(path)) {
					if (getFileExplorerActivity().getIsGetMorePic()) {
						if (picPath.size() >= getFileExplorerActivity().getImageSize()) {
							if (picPath.contains(path)) {
								picPath.remove(path);
							} else {
								if (!ButtonUtil.isFastClick()) {
									String msg = String.format(getFileExplorerActivity().getString(R.string.get_more_pic), getFileExplorerActivity().getImageSize());
									ToastUtils.showTast(getFileExplorerActivity(), msg);
								}
							}
							if (selectFiles.contains(fileInfo)) {
								selectFiles.remove(fileInfo);
							}
						} else {
							if (!picPath.contains(path)) {
								picPath.add(path);
							} else {
								picPath.remove(path);
							}

							if (selectFiles.contains(fileInfo)) {
								selectFiles.remove(fileInfo);
							} else {
								selectFiles.add(fileInfo);
							}
						}
						// add by JXH 2014-8-11 begin
						getFileExplorerActivity().setSelectFiles(selectFiles);
						getFileExplorerActivity().setSelectPicPath(picPath);
						// add by JXH 2014-8-11 end
						auroraPicItemAdapter.notifyDataSetChanged();
						changeActionBarTextView();
					} else {
						long ids = fileInfos.get(position).dbId;
						Uri uri = Uri.parse(Images.Media.getContentUri(FileCategoryHelper.volumeName) + File.separator + ids);
						IntentBuilder.getSinglePic(getFileExplorerActivity(), uri);
					}
				}
			}
		} else {
			IntentBuilder.viewFile(getFileExplorerActivity(), fileInfos.get(position).filePath, "image/*");
		}

	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view, int position, long id) {

		// 从其他APP跳入图片
		if (getFileExplorerActivity().getIsFromOtherAPP()) {
			return true;
		}

		if (getOperation().isCutOrCopy()) {
			ToastUtils.showTast(getFileExplorerActivity(), R.string.wait_a_moment);
			return true;
		}
		if (!isOperationFile()) {
			setOperationFile(true);
			showEditView();
			selectFiles.clear();
			selectFiles.add(fileInfos.get(position));
			updateAuroraItemBottomBarState();
			updateAuroraitemActionBarState();
		}
		return true;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				List<String> paths = bundle.getStringArrayList("image");
				List<FileInfo> infos = new ArrayList<FileInfo>();
				if (paths != null) {
					for (String string : paths) {
						infos.add(Util.GetFileInfo(string));
					}
				}
				getOperation().setSelectFiles(infos);
				getOperation().addPrivacyFiles(FileCategory.Picture);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isPrivacy && getFileExplorerActivity().isPrivacy()) {
			fileInfos = getFileExplorerActivity().getHashMap().get(FileCategory.Picture);
			refreshState(false);
		}
		// 隐私
		if (isPrivacy && !getFileExplorerActivity().getIsGetMorePic()) {
			getFileExplorerActivity().showEmptyBarItem();
			getAuroraActionBar().changeItemLayout(R.layout.privacy_add, R.id.pri_add_more_liner);
			setAddMoreLister();

		}
		if (isPrivacy && !getFileExplorerActivity().isPrivacy() && (!getActivityInfo().name.endsWith(getString(R.string.activity_picture)))) {
			getFileExplorerActivity().setPicItemBack(true);
			if (isOperationFile()) {// 隐私姿态退出，编辑状态修改
				if (getFileExplorerActivity().showOperationBarMoreMenu) {
					getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
				}
				unDoOperation();
				getHandler().post(new Runnable() {

					@Override
					public void run() {
						getSelectPageInterface().selectPage(AuroraConfig.AURORA_PIC_PAGE, FileCategory.Picture);
					}
				});
			} else {
				getSelectPageInterface().selectPage(AuroraConfig.AURORA_PIC_PAGE, FileCategory.Picture);
			}
		}
		if (isStop && !isShowImageButton || noPriv) {
			if (imageButton != null) {
				imageButton.setVisibility(View.GONE);
			}
		}

	}

	private boolean isShowImageButton = false;
	private boolean isStop = false;

	@Override
	public void onStop() {
		super.onStop();
		// LogUtil.elog(TAG, "onStop()");
		if (getOperation() != null) {
			getOperation().mDialogDismiss();
		}
		isStop = true;
		if (imageButton != null && imageButton.getVisibility() == View.VISIBLE) {
			isShowImageButton = true;
		} else {
			isShowImageButton = false;
		}

	}

	private boolean isOnDestroy = false;

	@Override
	public void onDestroy() {
		isOnDestroy = true;
		if (isPrivacy && imageButton != null) {
			imageButton.setVisibility(View.GONE);
		}
		super.onDestroy();
	}

	@Override
	public boolean onFragmentBack() {
		if (getFileExplorerActivity() != null && getFileExplorerActivity().getIsGetMorePic() && layout != null) {
			layout.setVisibility(View.GONE);
		}
		// LogUtil.elog(TAG, "onBack() 1");
		if (getFileExplorerActivity().showOperationBarMoreMenu) {
			getFileExplorerActivity().beforeDisMissAuroraOperationBarMoreMenu();
			return true;
		}

		// LogUtil.elog(TAG, "onBack() 2");
		if (!getFileExplorerActivity().getIsGetMorePic() && unDoOperation()) {// 退出编辑状态
			return true;
		}
		// LogUtil.elog(TAG, "onBack() 3");
		if (isPrivacy && (getActivityInfo().name.endsWith(getString(R.string.activity_picture)))) {
			getFileExplorerActivity().finish();
			return true;
		}

		// LogUtil.elog(TAG, "onBack() end");
		getFileExplorerActivity().setPicItemBack(true);
		if (imageButton != null) {
			imageButton.setVisibility(View.GONE);
		}
		getSelectPageInterface().selectPage(AuroraConfig.AURORA_PIC_PAGE, FileCategory.Picture);
		return true;
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
			setOperationFile(true);
			showEditView();
			selectFiles.clear();
			updateAuroraItemBottomBarState();
			updateAuroraitemActionBarState();// paul add for BUG #15035
		}
	}

	private LinearLayout layout;

	/**
	 * 选择多张图片时 actionBar右边按钮监听
	 */
	private void actionBarMorePicSetOnClickListener() {
		if (layout == null) {
			layout = (LinearLayout) getAuroraActionBar().getRootView().findViewById(R.id.pic_more_liner);
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
					IntentBuilder.getMorePic(getFileExplorerActivity(), picPath);
				}
			}
		});
		layout.setClickable(false);
	}

	@Override
	public void onLeftClick() {
		super.onLeftClick();
		unDoOperation();
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

	/**
	 * 选择图片文件夹
	 * @param position
	 */
	private void operationPicture(int position) {
		// auroraPicItemAdapter.selectCheckBox(position);
		setItemPicAnim(true);
		setSelectPosition(position);
		FileInfo temp = fileInfos.get(position);
		if (selectFiles.contains(temp)) {
			selectFiles.remove(temp);
		} else {
			selectFiles.add(temp);
		}
		updateAuroraitemActionBarState();
		updateAuroraItemBottomBarState();
	}

	/**
	 * 改变BottomBar 状态
	 */
	public void updateAuroraItemBottomBarState() {
		AuroraMenu auroraMenu = getAuroraActionBar().getAuroraActionBottomBarMenu();
		getOperation().setSelectFiles(selectFiles);
		auroraPicItemAdapter.setSelects(selectFiles);
		try {
			if (selectFiles.size() == 0) {
				auroraMenu.setBottomMenuItemEnable(1, false);
				auroraMenu.setBottomMenuItemEnable(2, false);
				auroraMenu.setBottomMenuItemEnable(3, false);
				auroraMenu.setBottomMenuItemEnable(4, false);
				auroraMenu.setBottomMenuItemEnable(5, false);
				auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(View.VISIBLE);
			} else if (selectFiles.size() == 1) {
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				auroraMenu.setBottomMenuItemEnable(4, true);
				auroraMenu.setBottomMenuItemEnable(5, true);
				auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(View.VISIBLE);
			} else {
				auroraMenu.setBottomMenuItemEnable(1, true);
				auroraMenu.setBottomMenuItemEnable(2, true);
				auroraMenu.setBottomMenuItemEnable(3, true);
				auroraMenu.setBottomMenuItemEnable(4, true);
				if (getFileExplorerActivity().isPrivacy()) {
					auroraMenu.setBottomMenuItemEnable(5, true);
					auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(View.VISIBLE);
				} else {
					auroraMenu.setBottomMenuItemEnable(5, false);
					auroraMenu.getAuroraBottomBarViewAt(5).setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LogUtil.e(TAG, e.getLocalizedMessage());
		}
		auroraPicItemAdapter.notifyDataSetChanged();
	}

	/**
	 * @return
	 */
	public boolean unDoOperation() {
		if (isAdded() && isOperationFile()) {// 退出编辑状态
			setOperationFile(false);
			getFileExplorerActivity().hideDeleteMenu();
			// LogUtil.elog(TAG, "unDoOperation");
			return true;
		}
		return false;
	}

	/**
	 * 显示底部按钮
	 */
	private void showEditView() {
		setItemPicAnim(false);
		getFileExplorerActivity().setRootView(getView());// 设置VIEW 显示more菜单
		getAuroraActionBar().setShowBottomBarMenu(true);
		getAuroraActionBar().showActionBarDashBoard();
	}

	/**
	 * 重命名 删除后 刷新数据
	 */
	private void refreshState(boolean isunDo) {
		auroraPicItemAdapter.setFileInfos(fileInfos);
		auroraPicItemAdapter.notifyDataSetChanged();
		if (isunDo) {
			unDoOperation();
		}
		if (getGridView().getCount() != 0) {

			if (isPrivacy) {
				getFileExplorerActivity().showEmptyBarItem();
			} else {
				getFileExplorerActivity().showEditBarItem();
			}
			getLoadAndEmptyView().getEmptyView().setVisibility(View.GONE);
		} else {
			getFileExplorerActivity().showEmptyBarItem();
			// LogUtil.log(TAG, "getEmptyView() VISIBLE 2");
			getLoadAndEmptyView().getEmptyView().setVisibility(View.VISIBLE);
		}
		showLoadingImage(false);
	}

	@Override
	public void deleteComplete(List<FileInfo> ids) {
		getFileExplorerActivity().clearCache();
		for (FileInfo fileInfo : ids) {
			fileInfos.remove(fileInfo);
		}
		refreshState(true);
		getOperation().mDialogDismiss();
		if (getFileExplorerActivity().isDoPrivacy()) {
			ConcurrentHashMap<FileCategory, List<FileInfo>> hashMap = getFileExplorerActivity().getHashMap();
			hashMap.put(FileCategory.Picture, fileInfos);
			getFileExplorerActivity().setPrivacyMedioFileNum(FileCategory.Picture, fileInfos.size());
		}

	}

	@Override
	public void renameComplete(FileInfo old, FileInfo newFileInfo) {
		int i = fileInfos.indexOf(old);
		if (i != -1) {
			fileInfos.set(i, newFileInfo);
		}
		refreshState(true);
	}

	/**
	 * 初始化 刷新数据
	 */
	public void refreshCategoryPics() {
		// LogUtil.d(TAG,
		// "refreshCategoryPics "+getFileExplorerActivity().getPicPath());
		if (isAdded() && !isPrivacy) {
			// 初始化FileCategoryHelper 查询Picture数据
			getCategoryHelper().queryByKey(FileCategory.Picture, SortMethod.date, getFileExplorerActivity().getPicPath());
			// LogUtil.elog(TAG, "refreshCategoryPics Picture ");
		} else if (isAdded()) {
			// LogUtil.elog(TAG, "refreshCategoryPics refreshPrivacy");
			getFileExplorerActivity().runPrivacyThread(FileCategory.Picture);
		}
	}

	private final Object object = new Object();
	private boolean unLock = false;

	public void obNotifyAll() {
		if (isAdded()) {
			synchronized (object) {
				object.notifyAll();
				unLock = true;
			}
		}
	}

	private boolean noPriv = false;

	public void isNoPriView() {
		if (isAdded()) {
			// LogUtil.elog(TAG, "isNoPriView");
			if (fileInfos != null) {
				fileInfos.clear();
			} else {
				fileInfos = new ArrayList<FileInfo>();
			}
			refreshState(false);
			// if (!getFileExplorerActivity().isPrivacy()) {
			if (imageButton != null) {
				imageButton.setVisibility(View.GONE);
				noPriv = true;
			}
			// }
		}
	}

	/**
	 * 刷新隐私数据
	 */
	public void refreshPrivacyPics() {
		if (isAdded()) {
			showLoadingImage(true);
			try {
				ThreadPoolExecutorUtils.getPrivacyUtils().getExecutor().execute(new Runnable() {

					@Override
					public void run() {
						List<FileInfo> infos = getFileExplorerActivity().getHashMap().get(FileCategory.Picture);
						if (infos == null) {
							synchronized (object) {
								if (!unLock) {
									try {
										object.wait();
									} catch (Exception e) {
										e.printStackTrace();
									}
									unLock = false;
								}

							}
							infos = getFileExplorerActivity().getHashMap().get(FileCategory.Picture);
						}
						if (infos != null) {
							fileInfos.clear();
							fileInfos.addAll(infos);
						}
						getFileExplorerActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								showLoadingImage(false);
								refreshState(false);
							}
						});

					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "refreshPrivacyPics error ::" + e.getMessage());
			}

		}
	}

	// add by JXH 2014-7-16 多图选择改变ActionBar显示 begin
	private TextView textView;

	private void changeActionBarTextView() {
		if (textView == null) {
			textView = (TextView) getAuroraActionBar().getRootView().findViewById(R.id.pic_more);
		}

		if (picPath != null && picPath.size() > 0) {
			textView.setText(String.format(getFileExplorerActivity().getString(R.string.nexts), picPath.size()));
			textView.setTextAppearance(getFileExplorerActivity(), R.style.more_pic_select);
			if (layout != null) {
				layout.setClickable(true);
			}
		} else {
			textView.setText(getFileExplorerActivity().getString(R.string.next));
			textView.setTextAppearance(getFileExplorerActivity(), R.style.more_pic_no_select);
			if (layout != null) {
				layout.setClickable(false);
			}
		}
		// textView.setTypeface(Util.auroraCreateTitleFont());
	}

	// add by JXH 2014-7-16 多图选择改变ActionBar显示 end

	@Override
	public void onFileListQueryComplete(Cursor cursor) {
		// LogUtil.d(TAG, "onFileListQueryComplete");
		if (!isOnDestroy) {
			synchronized (fileInfos) {
				fileInfos.clear();
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					FileInfo fileInfo = Util.getFileInfo(cursor, FileCategory.Picture);
					// LogUtil.d(TAG,
					// "onFileListQueryComplete fileInfo:"+fileInfo.toString());
					fileInfos.add(fileInfo);
				}
				cursor.close();
				if (getFileExplorerActivity().getIsGetMorePic()) {
					setOperationFile(true);
				}
				refreshState(false);
			}
		}
		// LogUtil.d(TAG,
		// "onFileListQueryComplete fileInfos.size:"+fileInfos.size());
	}

	@Override
	public void completeRefresh(List<FileInfo> addFileInfos, List<FileInfo> removeInfos) {// 隐私图片添加
		if (!(getFileExplorerActivity().getCurrentFragment() instanceof PictureFragment)) {
			return;
		}
		if (isPrivacy && !getFileExplorerActivity().isPrivacyBackToNormal()) {
			synchronized (fileInfos) {
				fileInfos.removeAll(addFileInfos);
				fileInfos.addAll(addFileInfos);
				refreshState(false);
				getFileExplorerActivity().refreshPrivacy(FileCategory.Picture);
			}
		} else {
			synchronized (fileInfos) {
				fileInfos.removeAll(removeInfos);
				refreshState(false);
				getFileExplorerActivity().refreshPrivacy(FileCategory.Picture);
			}
			if (getFileExplorerActivity().isPrivacyBackToNormal()) {
				getFileExplorerActivity().setPrivacyBackToNormal(false);
				ToastUtils.showTast(getFileExplorerActivity(), R.string.pri_back_dcim);
				unDoOperation();
			}
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
				mIntent.setAction(AuroraConfig.ACTION_MORE_PRI_GET_CONTENT);
				mIntent.setType("image/*");
				Bundle bundle = new Bundle();
				bundle.putInt("size", 100);
				bundle.putBoolean("noPriacy", true);
				mIntent.putExtras(bundle);
				startActivityForResult(mIntent, 1);
			}
		});
	}

}
