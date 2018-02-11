package com.aurora.iunivoice.activity.fragment;

import android.os.Bundle;


public abstract class BaseViewPagerFragment extends BaseFragment {
	
	protected boolean hasLoaded = false;		// 是否已经加载过数据
	protected boolean isFirst = true;			// 是否第一次加载fragment
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		isFirst = false;
	}

	/**
	* @Title: checkAndLoadData
	* @Description: 检查并加载数据
	* @param 
	* @return void
	* @throws
	 */
	public void checkAndLoadData() {
		if (hasLoaded || isFirst) {
			return;
		}
		loadData();
		hasLoaded = true;
	}

	/**
	* @Title: 起始的加载方法
	* @Description: TODO 起始的加载方法应该写在此处
	* @param 
	* @return void
	* @throws
	 */
	protected abstract void loadData();
	
}
