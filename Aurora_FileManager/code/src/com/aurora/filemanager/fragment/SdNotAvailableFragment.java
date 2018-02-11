package com.aurora.filemanager.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.filemanager.R;

/**
 * 显示sd卡不可用界面
 * 
 * @author jiangxh
 * @CreateTime 2014年5月26日 上午10:35:39
 * @Description com.aurora.filemanager SdNotAvailableFragment.java
 */
public class SdNotAvailableFragment extends AuroraFragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sd_not_available_page, container,
				false);
		getFileExplorerActivity().showEmptyBarItem();
		getAuroraActionBar().getHomeButton().setVisibility(
				View.GONE);
		return view;
	}

}
