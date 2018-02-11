package com.android.keyguard.view;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter{

    private List<View> mPageList;

    public ViewPagerAdapter(List<View> list) {
        this.mPageList = list;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ViewPager pViewPager = (( ViewPager ) container);
        pViewPager.removeView(mPageList.get(position));
    }

    @Override
    public void finishUpdate(View view) {
    }

    @Override
    public int getCount() {
        return mPageList.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ViewPager viewPager = (( ViewPager ) container);
        viewPager.addView(mPageList.get(position));
        return mPageList.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View container) {
    }

}
