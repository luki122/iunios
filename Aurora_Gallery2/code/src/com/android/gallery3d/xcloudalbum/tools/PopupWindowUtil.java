package com.android.gallery3d.xcloudalbum.tools;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow.OnDismissListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenu;

import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.widget.CloudSelectPopupWindow;
import com.android.gallery3d.xcloudalbum.widget.ProgressPopupWindow;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.android.gallery3d.R;

public class PopupWindowUtil {
	private static final String TAG = "PopupWindowUtil";
	private CloudSelectPopupWindow selectPopupWindow;
	private AuroraActivity activity;

	private ProgressPopupWindow progressPopupWindow;

	private Handler handler = new Handler() {
	};

	public CloudSelectPopupWindow getSelectPopupWindow() {
		return selectPopupWindow;
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

	private static PopupWindowUtil popupWindowUtil;

	public static PopupWindowUtil getInstance(AuroraActivity activity) {
		if (popupWindowUtil == null) {
			popupWindowUtil = new PopupWindowUtil(activity);
		}
		return popupWindowUtil;
	}

	public PopupWindowUtil(AuroraActivity activity) {
		super();
		this.activity = activity;
	}

	public void setOperationUtil(OperationUtil util) {
		selectPopupWindow.setOperationUtil(util);
	}

	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView,
			OperationUtil util) {
		if (rootView == null) {
			return;
		}
		// if (selectPopupWindow == null) {
		selectPopupWindow = null;
		selectPopupWindow = new CloudSelectPopupWindow(
				R.style.ActionBottomBarMorePopupAnimation, activity);
		// }
		setOperationUtil(util);
		selectPopupWindow.showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		activity.addCoverView();
		View view = activity.getCoverView();
		setAuroraMenuVisibility(false);
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
				setAuroraMenuVisibility(true);
				activity.removeCoverView();
				isShowPopupWindow = false;
			}
		});
	}

	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView,
			List<CommonFileInfo> fileInfos) {
		// long tmp = System.currentTimeMillis();
		if (rootView == null) {
			return;
		}
		selectPopupWindow = null;
		// if (selectPopupWindow == null) {
		selectPopupWindow = new CloudSelectPopupWindow(
				R.style.ActionBottomBarMorePopupAnimation, activity);
		// }
		selectPopupWindow.showAtLocation(rootView, Gravity.BOTTOM
				| Gravity.FILL_HORIZONTAL, 0, 0);
		selectPopupWindow.showList(fileInfos);
		// LogUtil.d(TAG,
		// "show time::"+(System.currentTimeMillis()-tmp)+" ++++++++++++::"+selectPopupWindow);
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		activity.addCoverView();
		View view = activity.getCoverView();
		setAuroraMenuVisibility(false);
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
				setAuroraMenuVisibility(true);
				activity.removeCoverView();
				isShowPopupWindow = false;
			}
		});
	}

	public void dismissSelectPopupWindow() {
		if (selectPopupWindow != null) {
			selectPopupWindow.dismiss();
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
			auroraMenu.getContentView().setVisibility(View.VISIBLE);
			return;
		}
		auroraMenu.getContentView().setVisibility(View.GONE);
	}

}
