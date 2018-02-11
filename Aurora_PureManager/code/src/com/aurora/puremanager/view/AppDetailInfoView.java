package com.aurora.puremanager.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Log;

public class AppDetailInfoView extends FrameLayout {

	private static final String TAG = "AppDetailInfoView";
	private AppInfo curAppInfo = null;

	public AppDetailInfoView(Context context) {
		super(context);
		initView();
	}

	public AppDetailInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.app_info_layout, this, true);
	}

	/**
	 * 设置当前的apk信息
	 * 
	 * @param curAppInfo
	 */
	public void setCurAppInfo(AppInfo curAppInfo) {
		this.curAppInfo = curAppInfo;
		if (curAppInfo == null) {
			throw new IllegalArgumentException(
					"ERROR_OF_curAppInfo_NULL_IN_AppSizeView");
		}
		updateView();
	}

	private void updateView() {
		Log.e(TAG, "updateView");
		ImageView appIcon = (ImageView) findViewById(R.id.appIcon);
		TextView appName = (TextView) findViewById(R.id.appName);
		TextView version = (TextView) findViewById(R.id.version);
		Drawable drawable = ApkUtils.getApkIcon(getContext(),
				curAppInfo.getPackageName());
		if (drawable == null) {
			Log.e(TAG, "drawable = NULL");
			drawable = getResources().getDrawable(R.drawable.ic_power_system);
		} else {
			Log.e(TAG, "drawable != NULL");
		}
		appIcon.setImageDrawable(drawable);
		appName.setText(ApkUtils.getApkName(getContext(),
				curAppInfo.getApplicationInfo()));

		String versionStr = ApkUtils.getApkVersion(getContext(),
				curAppInfo.getPackageName());
		if (versionStr == null || versionStr.equals("")
				|| versionStr.equals("null")) {
			versionStr = "1.0";
		}
		version.setText(getResources().getString(R.string.version)
				+ getResources().getString(R.string.colon) + versionStr);
	}

}
