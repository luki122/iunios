package com.android.gallery3d.selectfragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.PopupWindow.OnDismissListener;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadToXCloudListener;
import com.android.gallery3d.xcloudalbum.widget.LocalSelectPopupWindow;
import com.android.gallery3d.xcloudalbum.widget.ProgressPopupWindow;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import com.android.gallery3d.R;

public class LocalFragmentUtil {
	private static final String TAG="LocalPopupWindowUtil";
	private LocalSelectFragment selectFragmentWindow;
	private AbstractGalleryActivity activity;

	private ProgressPopupWindow progressPopupWindow;
	
	private UploadToXCloudListener mListener;
	
	private Handler handler = new Handler(){};
		
	public LocalSelectFragment getSelectPopupWindow() {
		return selectFragmentWindow;
	}

	public ProgressPopupWindow getProgressPopupWindow() {
		if (progressPopupWindow == null) {
			progressPopupWindow = new ProgressPopupWindow(this.activity,
					R.style.ActionBottomBarMorePopupAnimation);
		}
		//LogUtil.d(TAG, "progressPopupWindow:::"+progressPopupWindow.hashCode()+" activity::"+activity.hashCode());
		return progressPopupWindow;
	}

	private boolean isShowPopupWindow = false;

	public boolean isShowPopupWindow() {
		return isShowPopupWindow;
	}

	public AbstractGalleryActivity getActivity() {
		return activity;
	}

	private void setActivity(AbstractGalleryActivity activity) {
		this.activity = activity;
	}
	
	
	/*
	private static LocalPopupWindowUtil popupWindowUtil;

	
	public static LocalPopupWindowUtil getInstance(AuroraActivity activity) {
		
		if (popupWindowUtil == null) {
			popupWindowUtil = new LocalPopupWindowUtil(activity);
		}
		popupWindowUtil.setActivity(activity);
		return popupWindowUtil;
	}
	*/
	/*
	public void clearInstance() {
		progressPopupWindow = null;
		selectPopupWindow = null;
	}
	*/

	public LocalFragmentUtil(AbstractGalleryActivity activity) {
		super();
		this.activity = activity;
		if (progressPopupWindow == null) {
			progressPopupWindow = new ProgressPopupWindow(this.activity,
					R.style.ActionBottomBarMorePopupAnimation);
		}
	}
	
	public void setOperationUtil(OperationUtil util) {
		selectFragmentWindow.setOperationUtil(util);
	}
	
	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView, OperationUtil util, ArrayList<String> filePaths, final UploadToXCloudListener listener) {
//		if (rootView == null) {
//			return;
//		}
		selectFragmentWindow = new LocalSelectFragment();
		selectFragmentWindow.initData( activity,null, util, listener);
		addFragment(selectFragmentWindow,"LocalSelectFragmentWindow");
		
		mListener = listener;
		selectFragmentWindow.requestNewFolderInfos();
		selectFragmentWindow.setFilePaths(filePaths);
		setOperationUtil(util);
//		selectFragmentWindow.showAtLocation(rootView, Gravity.BOTTOM
//				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowPopupWindow = true;
//		selectFragmentWindow.setTitleText(title);
//		activity.addCoverView();
//		View view = activity.getCoverView();
		setAuroraMenuVisibility(false);
		
		changeActionBarLayout();
		
		
		
//		view.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				dismissSelectPopupWindow();
//				mListener.uploadFinished(false);
//				return true;
//			}
//		});
//		selectFragmentWindow.setOnDismissListener(new OnDismissListener() {
//
//			@Override
//			public void onDismiss() {
//				//setAuroraMenuVisibility(true);
//				activity.removeCoverView();
//				isShowPopupWindow = false;
//			}
//		});
	}
	
	private void addFragment(Fragment fragment, String tag) {
	    FragmentManager manager = activity.getFragmentManager();
	    FragmentTransaction transaction = manager.beginTransaction();
	    transaction.add(R.id.gallery_root,fragment, tag);
	    transaction.commit();
	}
	private void removeFragment( String tag) {
	    FragmentManager manager = activity.getFragmentManager();
	    FragmentTransaction transaction = manager.beginTransaction();
	    Fragment tagFragment = manager.findFragmentByTag(tag); 
	    if(tagFragment != null){
	    	transaction.remove(tagFragment);
	    }
	    transaction.commit();
	}
	
	//wenyongzhe 2015.11.5 Toash BUG
//	private Runnable mShowActionBarDashBoard = new Runnable() {
//		@Override
//		public void run() {
//			activity.getAuroraActionBar().showActionBarDashBoard();
//		}
//	};
//	public void dismissSelectPopupWindowShowActionBarDashBoard() {
//		dismissSelectPopupWindow();
//		activity.getAuroraActionBar().showActionBarDashBoard();
//		handler.postDelayed(mShowActionBarDashBoard, 700);
//	}
	
	public void dismissSelectPopupWindow() {
//		selectFragmentWindow.dismiss();
		removeFragment("LocalSelectFragmentWindow");
		setAuroraMenuVisibility(true);
		isShowPopupWindow = false;
		
		activity.getAuroraActionBar().changeAuroraActionbarType(AuroraActionBar.Type.Custom);
		activity.getAuroraActionBar().setCustomView(R.layout.aurora_actionbar_custom_view);
		activity.getAuroraActionBar().getHomeButton().setVisibility(View.GONE);
//		activity.getAuroraActionBar().showActionBarDashBoard();
//		activity.getAuroraActionBar().showActionBarDashBoard();
    	
		View cloudAlbumContainer = (View)activity.getAuroraActionBar().findViewById(R.id.aurora_baidu_cloud_album_container);	
		cloudAlbumContainer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				if(view.getId() == R.id.aurora_baidu_cloud_album_container) {
					activity.showNewUploadRedDot(false);
					Intent intent = new Intent(activity, CloudActivity.class);
					activity.startActivity(intent);
				}
			}
		});
	}

	private boolean isShowProgressPopupWindow=false;
	public boolean isShowProgressPopupWindow() {
		return isShowProgressPopupWindow;
	}

	public void showProgressPopupWindow(View rootView) {
		if(isShowProgressPopupWindow) return;
		if(rootView == null) return;
		/*
		if (progressPopupWindow == null) {
			progressPopupWindow = new ProgressPopupWindow(activity,
					R.style.ActionBottomBarMorePopupAnimation);
		}
		*/
		getProgressPopupWindow().showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowProgressPopupWindow = true;
		
		dismissSelectPopupWindow();
		
	}

	public void dismissProgressPopupWindow() {
		if(progressPopupWindow != null) {
			progressPopupWindow.dismiss();
			progressPopupWindow = null;
		}
		isShowProgressPopupWindow = false;
	}
	
	 //wenyongzhe 2015.9.14
    public void changeActionBarLayout() {
    	activity.getAuroraActionBar().changeAuroraActionbarType(
				AuroraActionBar.Type.Normal);
    	activity.getAuroraActionBar().getHomeButton().setVisibility(View.VISIBLE);
	
    	activity.getAuroraActionBar().setTitle(R.string.aurora_actionbar_upload_to);
    	activity.getAuroraActionBar().changeItemLayout(R.layout.aurora_album_add,
				R.id.aurora_album_add_liner);
		ImageButton imageButton = (ImageButton) activity.getAuroraActionBar()
				.getRootView().findViewById(R.id.aurora_album_add);
		if (imageButton != null) {
			
			if (!NetworkUtil.checkNetwork(activity)) {//wenyongzhe 2015.11.3 checkNetwork
				imageButton.setClickable(false);
				return;
			}
			imageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.getOperationUtil().setMoveCreateAlbum(true);
					activity.getOperationUtil().setMoveCheckBox(!false);
					activity.getOperationUtil().createAlbum(
							R.string.aurora_new_album,//wenyongzhe 2015.11.4
							(AuroraActivity) activity, selectFragmentWindow.getFileInfos());
				}
			});
		}	
		
		activity.getAuroraActionBar().setmOnActionBarBackItemListener(
				actionBarBackItemClickListener);
		
	}
    
    /**
	 * actionBar返回监听
	 */
	private OnAuroraActionBarBackItemClickListener actionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int item) {
			dismissSelectPopupWindow();
			activity.onActionBarBack();//wenyongzhe 2015.10
		}
	};
	

//	public void setAuroraMenuVisibility(boolean visibility) {
//		AuroraMenu auroraMenu = activity.getAuroraActionBar()
//				.getAuroraActionBottomBarMenu();
//		if (visibility) {
////			activity.getAuroraActionBar().showActionBarDashBoard();
//			auroraMenu.getContentView().setVisibility(View.VISIBLE);
//			return;
//		}
////		activity.getAuroraActionBar().showActionBarDashBoard();
//		auroraMenu.getContentView().setVisibility(View.GONE);
//	}
	public void setAuroraMenuVisibility(boolean visibility) {
		AuroraMenu auroraMenu = activity.getAuroraActionBar()
				.getAuroraActionBottomBarMenu();
		if (visibility) {
//			activity.getAuroraActionBar().setShowBottomBarMenu(true);
//			activity.getAuroraActionBar().showActionBarDashBoard();
			auroraMenu.getContentView().setVisibility(View.VISIBLE);
			return;
		}
//		activity.getAuroraActionBar().setShowBottomBarMenu(false);
//		activity.getAuroraActionBar().showActionBarDashBoard();
		auroraMenu.getContentView().setVisibility(View.GONE);
	}

}
