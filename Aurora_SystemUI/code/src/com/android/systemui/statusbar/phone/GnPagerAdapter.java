/**
	File Description:
		Adapter for PagerView.
	Author: fengjy@gionee.com
	Create Date: 2013/04/24
	Change List:
*/



package com.android.systemui.statusbar.phone;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class GnPagerAdapter extends PagerAdapter {
	
	private List<View> mViewsList;
	
	public GnPagerAdapter(List<View> list) {
		mViewsList = list;
	}

	@Override
	public int getCount() {
		return mViewsList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (View) arg1;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(mViewsList.get(arg1));
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(mViewsList.get(arg1), 0);
		return mViewsList.get(arg1);
	}

}
