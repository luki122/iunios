package com.android.gallery3d.selectfragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.PopupWindow.OnDismissListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenu;

import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.widget.CloudSelectPopupWindow;
import com.android.gallery3d.xcloudalbum.widget.ProgressPopupWindow;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.android.gallery3d.R;

public class XcloudMoveFragmentUtil {
	private static final String TAG = "FragmentUtil";
	private XcloudMoveFragment mXcloudMovePhotoFragment;
	private AuroraActivity activity;

	private ProgressPopupWindow progressPopupWindow;

	private Handler handler = new Handler() {
	};

	public XcloudMoveFragment getSelectPopupWindow() {
		return mXcloudMovePhotoFragment;
	}

	public ProgressPopupWindow getProgressPopupWindow() {
		return progressPopupWindow;
	}

	private boolean isShowPopupWindow = false;

	public boolean isShowPopupWindow() {
		return isShowPopupWindow;
	}

	public AuroraActivity getActivity() {
		return activity;
	}

	public void setActivity(AuroraActivity activity) {
		this.activity = activity;
	}

	private static XcloudMoveFragmentUtil fragmentUtil;

	public static XcloudMoveFragmentUtil getInstance(AuroraActivity activity) {
		if (fragmentUtil == null) {
			fragmentUtil = new XcloudMoveFragmentUtil(activity);
		}
		return fragmentUtil;
	}

	public XcloudMoveFragmentUtil(AuroraActivity activity) {
		super();
		this.activity = activity;
	}


	private void addFragment(Fragment fragment, String tag) {
	    FragmentManager manager = activity.getFragmentManager();
	    FragmentTransaction transaction = manager.beginTransaction();
	    transaction.add(R.id.container,fragment, tag);
	    //transaction.addToBackStack(tag);
	    transaction.commit();
	    
	    Fragment itemFragment = manager.findFragmentByTag("CLOUDITEM"); 
	    if(itemFragment != null){
	    	transaction.hide(itemFragment);
	    }
	}
	private void removeFragment( String tag) {
	    FragmentManager manager = activity.getFragmentManager();
	    FragmentTransaction transaction = manager.beginTransaction();
	    Fragment xcloudMovePhotoFragment = manager.findFragmentByTag(tag); 
	    Fragment itemFragment = manager.findFragmentByTag("CLOUDITEM"); 
	    if(itemFragment != null){
	    	transaction.show(itemFragment);
	    }
	    if(xcloudMovePhotoFragment != null){
	    	transaction.remove(xcloudMovePhotoFragment);
	    }
	    transaction.commit();
	}
	
	
	public void showSelectPopupWindow(String title, View rootView,
	@SuppressLint("RtlHardcoded")
			List<CommonFileInfo> fileInfos) {
//		if (rootView == null) {
//			return;
//		}
		setAuroraMenuVisibility(false);
		mXcloudMovePhotoFragment = null;
		mXcloudMovePhotoFragment = new XcloudMoveFragment();
		mXcloudMovePhotoFragment.setFileInfos((CloudActivity)getActivity(),fileInfos);
		addFragment(mXcloudMovePhotoFragment, "XcloudMovePhotoFragment");
		
		((CloudActivity)getActivity()).setCurrentfragmentFlag(CloudActivity.XCLOUDMOVEPHOTOFRAGEMENT);//actionbar "+"号
		((CloudActivity)getActivity()).setAuroraActionBarTitle(((CloudActivity)getActivity()).getResources().getString(R.string.aurora_actionbar_move_to));
		
		isShowPopupWindow = true;
		mXcloudMovePhotoFragment.setTitleText(title);
		
//		((CloudActivity) getActivity()).getOperationUtil().setOperationComplete((BasicFragment) mXcloudMovePhotoFragment);
		
//		activity.addCoverView();
//		View view = activity.getCoverView();
//		view.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				dismissSelectPopupWindow();
//				return true;
//			}
//		});
//		mXcloudMovePhotoFragment.setOnDismissListener(new OnDismissListener() {
//
//			@Override
//			public void onDismiss() {
//				setAuroraMenuVisibility(true);
//				activity.removeCoverView();
//				isShowPopupWindow = false;
//			}
//		});
	}

	public void dismissSelectPopupWindow() {
		if (mXcloudMovePhotoFragment != null) {
			((CloudActivity)getActivity()).setCurrentfragmentFlag(CloudActivity.CLOUDITEM);
			removeFragment("XcloudMovePhotoFragment");
			setAuroraMenuVisibility(true);
			isShowPopupWindow = false;
		}
	}

	private boolean isShowProgressPopupWindow = false;

	public boolean isShowProgressPopupWindow() {
		return isShowProgressPopupWindow;
	}

	public void showProgressPopupWindow(View rootView) {
		// if (progressPopupWindow == null) {
		progressPopupWindow = null;
		progressPopupWindow = new ProgressPopupWindow(activity,
				R.style.ActionBottomBarMorePopupAnimation);
		// }
		progressPopupWindow.showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowProgressPopupWindow = true;

	}

	public void dismissProgressPopupWindow() {
		progressPopupWindow.dismiss();
		isShowProgressPopupWindow = false;
	}

	public void dismissProgressPopupWindow(int delayMillis) {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				progressPopupWindow.dismiss();
				isShowProgressPopupWindow = false;
			}
		}, delayMillis);
	}

	public void setAuroraMenuVisibility(boolean visibility) {
		AuroraMenu auroraMenu = activity.getAuroraActionBar()
				.getAuroraActionBottomBarMenu();
		if (visibility) {
			activity.getAuroraActionBar().setShowBottomBarMenu(true);
			activity.getAuroraActionBar().showActionBarDashBoard();
//			auroraMenu.getContentView().setVisibility(View.VISIBLE);
			return;
		}
		activity.getAuroraActionBar().setShowBottomBarMenu(false);
		activity.getAuroraActionBar().showActionBarDashBoard();
//		auroraMenu.getContentView().setVisibility(View.GONE);
	}

}
