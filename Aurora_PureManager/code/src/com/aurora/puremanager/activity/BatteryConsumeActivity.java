package com.aurora.puremanager.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.FragmentPagerAdapter;
import com.aurora.puremanager.fragment.UsageSummaryFragment;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.view.PagerAdapter;
import aurora.view.ViewPager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraTabWidget;

public class BatteryConsumeActivity extends AuroraActivity {

    private List<Fragment> mList;
    private PagerAdapter mAdapter;
    private AuroraTabWidget mTabWidget;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.activity_battery_consume, AuroraActionBar.Type.Normal);

        initActionBar();
        initData();
        setAdapter();
    }

    private void setAdapter() {
        mViewPager.setAdapter(mAdapter);
    }

    private void initData() {
        mList = new ArrayList<Fragment>();
        addFragment2List();
        mTabWidget = (AuroraTabWidget) findViewById(R.id.tab_view);
        mViewPager = mTabWidget.getViewPager();
        mViewPager.setId(8899);
        mAdapter = new BatteryFragmentAdapter(getFragmentManager());
    }

    public void initActionBar() {
        AuroraActionBar bar = getAuroraActionBar();
        bar.setBackgroundResource(R.color.power_green);
        bar.setTitle(R.string.consume_rank);
    }

    private void addFragment2List() {
        for (int i = 0; i < 2; i++) {
            Fragment fragment = new UsageSummaryFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("fragment_key", i);
            fragment.setArguments(bundle);
            mList.add(fragment);
        }
    }

    class BatteryFragmentAdapter extends FragmentPagerAdapter {

        public BatteryFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }
    }

}
