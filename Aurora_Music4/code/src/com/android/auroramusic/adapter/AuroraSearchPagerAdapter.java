package com.android.auroramusic.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class AuroraSearchPagerAdapter extends PagerAdapter{
	private List<View> mListViews; 
	
	public AuroraSearchPagerAdapter(List<View> listViews){
		this.mListViews = listViews;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView(mListViews.get(position));
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		container.addView(mListViews.get(position), position);
		return mListViews.get(position);
	}
}
