package com.android.gallery3d.xcloudalbum.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.viewpager.ViewpagerActivity;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.inter.IActionOnClickListener;
import com.android.gallery3d.xcloudalbum.inter.IBackPressedListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.inter.IOperationComplete;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.widget.AuroraLoadAndEmptyView;
import com.android.gallery3d.xcloudalbum.widget.CloudItemAdapter;
import com.android.gallery3d.xcloudalbum.widget.CloudMainAdapter;
import com.android.gallery3d.R;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class BasicFragment extends Fragment implements IActionOnClickListener,
		IBackPressedListener, IBaiduinterface, IOperationComplete,
		IBaiduTaskListener {

	private static final String TAG = "BasicFragment";
	private CloudActivity activity;
	private ISelectPageListener selectPageListener;
	private AuroraLoadAndEmptyView loadAndEmptyView;

	private BaiduAlbumUtils baiduAlbumUtils;

	public TextView mMiddleTextView;//wenyongzhe 2016.1.25
	
	public BaiduAlbumUtils getBaiduAlbumUtils() {
		return baiduAlbumUtils;
	}

	public OperationUtil getOperationUtil() {
		return getCloudActivity().getOperationUtil();
	}

	private GridView gridView;
	private AuroraListView listView;

	private TextView leftView, rightView;

	private List<CommonFileInfo> imageInfos = Collections.synchronizedList(new ArrayList<CommonFileInfo>());
	private List<CommonFileInfo> selectImages = Collections.synchronizedList(new ArrayList<CommonFileInfo>());

	public  List<CommonFileInfo> getImageInfos() {
		return imageInfos;
	}

	public void setImageInfos(List<CommonFileInfo> imageInfos) {
		this.imageInfos = imageInfos;
	}

	public List<CommonFileInfo> getSelectImages() {
		return selectImages;
	}

	public void setSelectImages(List<CommonFileInfo> selectImages) {
		this.selectImages = selectImages;
	}

	public interface ISelectPageListener {
		void selectPage(int page);
	}

	public CloudActivity getCloudActivity() {
		if (activity == null) {
			activity = (CloudActivity) getActivity();
			Log.w(TAG, "activity is null");
			if (activity == null) {
				Log.e(TAG, "activity is null !!!");
			}
		}
		return activity;
	}

	public ISelectPageListener getSelectPageListener() {
		return selectPageListener;
	}

	public GridView getGridView() {
		return gridView;
	}

	public void setGridView(GridView gridView) {
		this.gridView = gridView;
	}
	
	//wenyongzhe2016.1.8 new_ui start
	public AuroraListView getListView() {
		return listView;
	}
	public void setListView(AuroraListView listView) {
		this.listView = listView;
	}
	//wenyongzhe2016.1.8 new_ui end
	
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
		//wenyongzhe 2.16.1.7 new_ui tabwidget_animale start
		((ViewpagerActivity)((GalleryAppImpl)getActivity().getApplicationContext()).getmActivityContext()).subActivitySelectionModeChange(isOperationFile);

		this.isOperationFile = isOperationFile;
	}

	private boolean isItemPicAnim;

	public boolean isItemPicAnim() {
		return isItemPicAnim;
	}

	public void setItemPicAnim(boolean isItemPicAnim) {
		this.isItemPicAnim = isItemPicAnim;
	}

	public void imageViewAnim(ImageView imageView, boolean in) {
		if (in) {
			imageView.setImageResource(R.anim.animation_imageview_in);
		} else {
			imageView.setImageResource(R.anim.animation_imageview_out);
		}
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView
				.getDrawable();
		animationDrawable.start();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		selectPageListener = (ISelectPageListener) activity;
		this.activity = (CloudActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//wenyongzhe modify 2015.9.10 modify fragment hide/show
		init();
		mMiddleTextView = getOutsideActionBar().getMiddleTextView();//wenyongzhe 2016.1.25
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	//wenyongzhe modify 2015.9.10 modify fragment hide/show
	private void  init(){
		if (this.activity == null) {
			this.activity = (CloudActivity) getActivity();
		}
		if (baiduAlbumUtils == null) {
			baiduAlbumUtils = BaiduAlbumUtils.getInstance(getCloudActivity());
		}
		baiduAlbumUtils.setBaiduinterface(this);
//		actionBarSetOnClickListener();
		if (gridView != null) {
			gridView.setOnItemClickListener(itemClickListener);
			gridView.setOnItemLongClickListener(itemLongClickListener);
		}
		if (listView != null) {
			listView.setOnItemClickListener(itemClickListener);
			listView.setOnItemLongClickListener(itemLongClickListener);
		}
		//wenyongzhe2016.1.7 new_ui
		getSubActionBar().setVisibility(View.INVISIBLE);
	}

//wenyongzhe modify 2015.9.10 modify fragment hide/show
	@Override
	public void onHiddenChanged(boolean hidden) {
		//TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if(hidden == false){
			init();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
	public synchronized void baiduPhotoList(List<CommonFileInfo> list,
			boolean isDirPath, CommonFileInfo infos) {
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
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (!isAdded()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onFragmentBack() {
		if (!isAdded()) {
			return false;
		}
		return false;
	}

	@Override
	public boolean onBack() {
		if (!isAdded()) {
			return false;
		}

		if (getCloudActivity().isShowPopupWindow()) {
			getCloudActivity().dismissSelectPopupWindow();
			return true;
		}

		if (isOperationFile()) {
			//wenyongzhe 2015.10.10  bug
			cancelOperation();
			
			return true;
		}
		return onFragmentBack();
	}

	protected ProgressBar mLoadingProgressBar;
	protected LinearLayout emptyView;
	/**
	 * 是否显示加载进度条
	 * 
	 * @param isShow
	 */
	public void showLoadingImage(boolean isShow, boolean isShowImage) {
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
			mLoadingProgressBar.setVisibility(View.VISIBLE);
		} else {
			mLoadingProgressBar.setVisibility(View.GONE);
		}
		
		emptyView = loadAndEmptyView.getEmptyView();
		if(isShowImage){
			emptyView.setVisibility(View.VISIBLE);
		}else{
			emptyView.setVisibility(View.INVISIBLE);
		}
	}

	public void showLoadingImagview() {
		if (loadAndEmptyView == null) {
			loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
		}
		loadAndEmptyView.getEmptyView().setVisibility(View.VISIBLE);
		loadAndEmptyView.getLoadingImageView().setVisibility(View.VISIBLE);
		loadAndEmptyView.getLoadingImageView().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!isStartAnimationLoading) {
							startAnimationLoading();
							reloadFileList();
						}

					}
				});
	}
	
	public void reloadFileList(){};

	private boolean isStartAnimationLoading = false;

	public void startAnimationLoading() {
		if (loadAndEmptyView == null) {
			loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
		}
		Animation animation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.aurora_album_refresh);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(Animation.INFINITE);
		loadAndEmptyView.getLoadingImageView().startAnimation(animation);
		isStartAnimationLoading = true;
	}

	public void stopAnimationLoading() {
		if(isStartAnimationLoading){
			if (loadAndEmptyView == null) {
				loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
			}
			loadAndEmptyView.getLoadingImageView().clearAnimation();
			isStartAnimationLoading = false;
			loadAndEmptyView.getEmptyView().setVisibility(View.GONE);
			loadAndEmptyView.getLoadingImageView().setVisibility(View.GONE);
		}
	}

	public void setEmptyViewText(int rid) {
		if (loadAndEmptyView == null) {
			loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
		}// R.string.aurora_album_no_picture
		loadAndEmptyView.getEmptyView().setVisibility(View.VISIBLE);
		TextView textView = loadAndEmptyView.getEmptyTextView();
		textView.setText(getString(rid));
	}

	public void setEmptyViewText(String msg) {
		if (loadAndEmptyView == null) {
			loadAndEmptyView = new AuroraLoadAndEmptyView(getView());
		}
		loadAndEmptyView.getEmptyView().setVisibility(View.VISIBLE);
		TextView textView = loadAndEmptyView.getEmptyTextView();
		textView.setText(msg);

	}

	public AuroraLoadAndEmptyView getLoadAndEmptyView() {
		return loadAndEmptyView;
	}

	public AuroraActionBar getOutsideActionBar() {
		//wenyongzhe 2016.1.5 viewpager new ui
		return ((GalleryAppImpl)getCloudActivity().getApplicationContext()).getmAuroraActionBar();
	}
	
	//wenyongzhe 2016.1.7 viewpager new ui start
	public AuroraActionBar getSubActionBar() {
		return activity.getAuroraActionBar();
	}
	//wenyongzhe 2016.1.7 viewpager new ui end

	private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if (isLoadingProgressBarVisibility()) {
				return true;
			}
			//wenyongzhe 2015.10.9 自测BUG
			updateAuroraItemBottomBarState(true);
			
			return auroraOnItemLongClick(parent, view, position, id);
		}
	};

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (isLoadingProgressBarVisibility()) {
				return;
			}

			if (isOperationFile()) {
				//wenyongzhe 2016.1.28  相册
//				if ((getCloudActivity().getCurrentFragment() instanceof CloudMainFragment)
//						&& position < 1) {
//					return;
//				}
				selectImageOperation(position);
				mMiddleTextView.setText(String.format(getResources().getString(R.string.adlum_select_num, selectImages.size())));
				return;
			}
			auroraOnItemClick(parent, view, position, id);
		}
	};

	/**
	 * 正在加载数据不让点击
	 * 
	 * @return
	 */
	public boolean isLoadingProgressBarVisibility() {
		if (mLoadingProgressBar != null
				&& mLoadingProgressBar.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	/**
	 * 用于判断item是否播放动画
	 */
	private int selectPosition = -1;

	public int getSelectPosition() {
		return selectPosition;
	}

	public void setSelectPostion(int selectPosition) {
		this.selectPosition = selectPosition;
	}

	private void selectImageOperation(int position) {
		try {
			setItemPicAnim(true);
			setSelectPostion(position);
			CommonFileInfo fileInfo = imageInfos.get(position);

			if (selectImages.contains(fileInfo)) {
				selectImages.remove(fileInfo);
			} else {
				selectImages.add(fileInfo);
			}
			updateAuroraitemActionBarState();
			updateAuroraItemBottomBarState(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分类左右actionBar监听
	 */
	//wenyongzhe modify 2015.9.8
	private void actionBarSetOnClickListener() {
		leftView = (TextView) getOutsideActionBar().getSelectLeftButton();
		rightView = (TextView) getOutsideActionBar().getSelectRightButton();
		rightView.setText(R.string.myselect_all);
		leftView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isAdded()) {
					return;
				}
				if (!actionBarIsAnimRunning()) {
					onBack();
					//wenyongzhe modify 2015.10.9 自测bug
					updateAuroraItemBottomBarState(true);
					
//					 onLeftClick();
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isAdded()) {
					return;
				}
				if (!actionBarIsAnimRunning()) {
					setItemPicAnim(false);
					if (selectImages.size() == (imageInfos.size() - getChange())) {
						selectImages.clear();
					} else {
						selectImages.clear();
						if(getCloudActivity().getCurrentFragment() instanceof CloudMainFragment){
							List<CommonFileInfo> fileInfos = new ArrayList<CommonFileInfo>(imageInfos);
							//fileInfos.remove(0);//wenyongzhe21016.2.15
							selectImages.addAll(fileInfos);
						}else {
							selectImages.addAll(imageInfos);
						}
					}
					mMiddleTextView.setText(String.format(getResources().getString(R.string.adlum_select_num, selectImages.size())));
					onRightClick();
					updateAuroraItemBottomBarState(true);
				}
			}
		});
	}

	//
	private int change = 0;

	public void setChange(int change) {
		this.change = change;
	}

	public int getChange() {
		return change;
	}

	protected void updateAuroraitemActionBarState() {
		if (isAdded()) {
			actionBarSetOnClickListener();
			if (selectImages.size() == (imageInfos.size() - change)
					&& rightView != null) {
				rightView.setText(getResources().getString(
						R.string.unmyselect_all));
			} else if (rightView != null) {
				rightView.setText(getResources().getString(
						R.string.myselect_all));
			}
		}
	}

	public void updateAuroraItemBottomBarState(boolean notify) {
		if (isAdded()) {
			Fragment fragment = getCloudActivity().getCurrentFragment();
			if (fragment instanceof CloudMainFragment) {
				if (selectImages.size() > 0) {
					setItemBottomBar(2, false);
					setItemBottomBar(1, true);
					setItemBottomBar(0, true);
				} else if (selectImages.size() == 0) {
					setItemBottomBar(0, false);
					setItemBottomBar(1, false);
					setItemBottomBar(2, false);
				} 
				if (notify) {
					CloudMainAdapter adapter = ((CloudMainFragment) fragment)
							.getAdapter();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
				}
				if (!Utils.isConnect(getActivity())) {
					setItemBottomBar(1, false);
					setItemBottomBar(2, false);
				}
			} else if (fragment instanceof CloudItemFragment) {
				if (selectImages.size() == 0) {
					setItemBottomBar(1, false);
					setItemBottomBar(2, false);
					setItemBottomBar(0, false);
				} else {
					setItemBottomBar(1, true);
					setItemBottomBar(2, true);
					setItemBottomBar(0, true);
				}
				if (notify) {
					CloudItemAdapter adapter = ((CloudItemFragment) fragment)
							.getAdapter();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
				}
				if (!Utils.isConnect(getActivity())) {
					setItemBottomBar(1, false);
					setItemBottomBar(2, false);
					setItemBottomBar(0, false);
				} 
				
				//wenyongzhe 2015.10.9 开放移动数据联网
//				else if (!NetworkUtil.checkWifiNetwork(getActivity())) {
//					setItemBottomBar(2, false);
//				}
			}

		}
	}

	private void setItemBottomBar(int position, boolean use) {
		getOutsideActionBar().getAuroraActionBottomBarMenu()
				.setBottomMenuItemEnable(position, use);
	}

	public void showOrHideMenu(boolean show) {
		setItemPicAnim(false);
		getOutsideActionBar().setShowBottomBarMenu(show);
		getOutsideActionBar().showActionBarDashBoard();
	}

	/**
	 * 判断编辑动画是否播放完成
	 * 
	 * @return
	 */
	public boolean actionBarIsAnimRunning() {
		if (getOutsideActionBar() == null) {// 快速点击图片和手机返回键
			return false;
		}
		if (getOutsideActionBar().auroraIsEntryEditModeAnimRunning()
				|| getOutsideActionBar().auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}

	@Override
	public void loginComplete(boolean success) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void renameComplete(boolean success) {
		if (!isAdded()) {
			return;
		}
		if (success) {
			cancelOperation();
		} else {
			ToastUtils.showTast(getCloudActivity(),
					R.string.aurora_rename_error);
		}
	}

	@Override
	public void delComplete(boolean success) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void moveOrCopyComplete(boolean success, boolean isMove,
			int errorCode) {
		if (!isAdded()) {
			return;
		}

	}

	@Override
	public void createAlbumComplete(boolean success) {
		if (!isAdded()) {
			return;
		}

	}

	/**
	 * 取消操作返回到正常状态
	 */
	public void cancelOperation() {
		setOperationFile(false);
		selectImages.clear();
		showOrHideMenu(false);
	}

	@Override
	public void baiduTaskStatus(FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void baiduDownloadTaskStatus(FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
	}

	@Override
	public void baiduUploadTaskStatus(FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
	}
	
	public void onViewPagerSelected(){
//		actionBarSetOnClickListener();//wenyongzhe2016.2.14
	}

}
