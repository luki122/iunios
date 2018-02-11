package com.android.auroramusic.adapter;

import java.util.List;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;



public class AuroraPlayerPagerAdapter extends PagerAdapter{
	private static final String TAG = "AuroraPlayerPagerAdapter";
	public List<View> mListViews = null;
	
	
	public AuroraPlayerPagerAdapter(List<View> mListViews) {
		super();
		this.mListViews = mListViews;
		return;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		try {
			((ViewPager) container).removeView(mListViews.get(position));
		} catch (Exception e) {
			Log.i(TAG, "zll ---- destroyItem fail position:"+position);
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		((ViewPager) container).addView(mListViews.get(position), 0);
		return mListViews.get(position);
	}

	@Override
	public int getCount() {
		if (mListViews != null) {
			return mListViews.size();
		}
		
		return 0;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	

}
