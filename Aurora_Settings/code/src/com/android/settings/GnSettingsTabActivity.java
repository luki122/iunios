package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class GnSettingsTabActivity extends AuroraActivity {

    private List<View> mListViews;
    private Context mContext = null;
    private LocalActivityManager mLocalActivityManager = null;
    private TabHost mTabHost = null;
    private ViewPager mPager = null;
    private static final String COMMON_SETTINGS_TAG = "SETTINGS_COMMON";
    private static final String ALL_SETTINGS_TAG = "SETTINGS_ALL";
    private static final int COMMON_SETTINGS_PAGER = 0;
    private static final int ALL_SETTINGS_PAGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gn_settings_tabs);

        mContext = GnSettingsTabActivity.this;

        mPager = ( ViewPager ) findViewById(R.id.gn_settings_viewpager);

        mListViews = new ArrayList<View>();

        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);

        Intent intentCommon = new Intent(mContext, GnSettingsCommon.class);
        mListViews.add(getView("settings_common", intentCommon));
        Intent intentAll = new Intent(mContext, Settings.class);
        mListViews.add(getView("settings_all", intentAll));

        mTabHost = ( TabHost ) findViewById(R.id.gn_settings_tabhost);
        mTabHost.setup(mLocalActivityManager);

        LinearLayout layoutCommon = ( LinearLayout ) LayoutInflater.from(this).inflate(
                R.layout.gn_tab_indicator_dark, null);
        TextView commonTitle = ( TextView ) layoutCommon.findViewById(R.id.tv_title);
        commonTitle.setText(getString(R.string.gn_settings_common));

        LinearLayout layoutAll = ( LinearLayout ) LayoutInflater.from(this).inflate(
                R.layout.gn_tab_indicator_dark, null);
        TextView allTitle = ( TextView ) layoutAll.findViewById(R.id.tv_title);
        allTitle.setText(getString(R.string.gn_settings_all));

        mTabHost.addTab(mTabHost.newTabSpec(COMMON_SETTINGS_TAG).setIndicator(layoutCommon)
                .setContent(intentCommon));
        mTabHost.addTab(mTabHost.newTabSpec(ALL_SETTINGS_TAG).setIndicator(layoutAll).setContent(intentAll));

        mPager.setAdapter(new MyPageAdapter(mListViews));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                if (COMMON_SETTINGS_TAG.equals(tabId)) {
                    mPager.setCurrentItem(COMMON_SETTINGS_PAGER);
                }
                if (ALL_SETTINGS_TAG.equals(tabId)) {
                    mPager.setCurrentItem(ALL_SETTINGS_PAGER);
                }
            }
        });

    }

    private View getView(String id, Intent intent) {
        return mLocalActivityManager.startActivity(id, intent).getDecorView();
    }

    private class MyPageAdapter extends PagerAdapter {

        private List<View> mPageList;

        private MyPageAdapter(List<View> list) {
            this.mPageList = list;
        }

        @Override
        public void destroyItem(View view, int position, Object arg2) {
            ViewPager pViewPager = (( ViewPager ) view);
            pViewPager.removeView(mPageList.get(position));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public Object instantiateItem(View view, int position) {
            ViewPager pViewPager = (( ViewPager ) view);
            pViewPager.addView(mPageList.get(position));
            return mPageList.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

    }

}
