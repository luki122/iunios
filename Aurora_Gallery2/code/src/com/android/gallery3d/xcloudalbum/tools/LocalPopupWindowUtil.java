package com.android.gallery3d.xcloudalbum.tools;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow.OnDismissListener;

import com.android.gallery3d.xcloudalbum.uploaddownload.UploadToXCloudListener;
import com.android.gallery3d.xcloudalbum.widget.LocalSelectPopupWindow;
import com.android.gallery3d.xcloudalbum.widget.ProgressPopupWindow;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import aurora.widget.AuroraMenuBase;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenu;
import com.android.gallery3d.R;

public class LocalPopupWindowUtil {
	private static final String TAG="LocalPopupWindowUtil";
	private LocalSelectPopupWindow selectPopupWindow;
	private AuroraActivity activity;

	private ProgressPopupWindow progressPopupWindow;
	
	private UploadToXCloudListener mListener;
	
	private Handler handler = new Handler(){};
		
	public LocalSelectPopupWindow getSelectPopupWindow() {
		return selectPopupWindow;
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

	public AuroraActivity getActivity() {
		return activity;
	}

	private void setActivity(AuroraActivity activity) {
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

	public LocalPopupWindowUtil(AuroraActivity activity) {
		super();
		this.activity = activity;
		if (progressPopupWindow == null) {
			progressPopupWindow = new ProgressPopupWindow(this.activity,
					R.style.ActionBottomBarMorePopupAnimation);
		}
	}
	
	public void setOperationUtil(OperationUtil util) {
		selectPopupWindow.setOperationUtil(util);
	}
	
	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView, OperationUtil util, ArrayList<String> filePaths, final UploadToXCloudListener listener) {
		if (rootView == null) {
			return;
		}
		if (selectPopupWindow == null) {
			selectPopupWindow = new LocalSelectPopupWindow(
					R.style.ActionBottomBarMorePopupAnimation, activity,
					null, util, listener);
		}
		mListener = listener;
		selectPopupWindow.requestNewFolderInfos();
		selectPopupWindow.setFilePaths(filePaths);
		setOperationUtil(util);
		selectPopupWindow.showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		activity.addCoverView();
		View view = activity.getCoverView();
		//setAuroraMenuVisibility(false);
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dismissSelectPopupWindow();
				mListener.uploadFinished(false);
				return true;
			}
		});
		selectPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				//setAuroraMenuVisibility(true);
				activity.removeCoverView();
				isShowPopupWindow = false;
			}
		});
	}
	
	

/*	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView,
			List<CommonFileInfo> fileInfos) {
		if (rootView == null) {
			return;
		}
		if (selectPopupWindow == null) {
			selectPopupWindow = new LocalSelectPopupWindow(
					R.style.ActionBottomBarMorePopupAnimation, activity,
					fileInfos);
		}
		selectPopupWindow.showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		activity.addCoverView();
		View view = activity.getCoverView();
		//setAuroraMenuVisibility(false);
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dismissSelectPopupWindow();
				return true;
			}
		});
		selectPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				//setAuroraMenuVisibility(true);
				activity.removeCoverView();
				isShowPopupWindow = false;
			}
		});
	}
*/
	public void dismissSelectPopupWindow() {
		selectPopupWindow.dismiss();
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
	}

	public void dismissProgressPopupWindow() {
		if(progressPopupWindow != null) {
			progressPopupWindow.dismiss();
			progressPopupWindow = null;
		}
		isShowProgressPopupWindow = false;
	}
	
/*	public void dismissProgressPopupWindow(int delayMillis) {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				progressPopupWindow.setLoadProgressBar(0);
				progressPopupWindow.dismiss();
				isShowProgressPopupWindow = false;
			}
		}, delayMillis);
	}*/

	public void setAuroraMenuVisibility(boolean visibility) {
		AuroraMenu auroraMenu = activity.getAuroraActionBar()
				.getAuroraActionBottomBarMenu();
		if (visibility) {
			auroraMenu.getContentView().setVisibility(View.VISIBLE);
			return;
		}
		auroraMenu.getContentView().setVisibility(View.GONE);
	}

}
