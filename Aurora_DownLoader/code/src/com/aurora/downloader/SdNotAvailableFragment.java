package com.aurora.downloader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aurora.downloader.DownloadActivity.IBackPressedListener;
import com.aurora.downloader.util.AuroraLog;

/**
 * 显示sd卡不可用界面
 * 
 * @author jiangxh
 * @CreateTime 2014年5月26日 上午10:35:39
 * @Description com.aurora.filemanager SdNotAvailableFragment.java
 */
public class SdNotAvailableFragment extends Fragment implements IBackPressedListener {
	private DownloadActivity downloadActivity;
	private static final String TAG = "SdNotAvailableFragment";

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		downloadActivity = (DownloadActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.sd_not_available_page, container,
				false);
		AuroraLog.elog(TAG, "onCreateView");
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		downloadActivity.showEmptyBarItem();
		downloadActivity.auroraActionBar.getHomeButton().setVisibility(
				View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onBack() {
		// TODO Auto-generated method stub
		return false;
	}


}
