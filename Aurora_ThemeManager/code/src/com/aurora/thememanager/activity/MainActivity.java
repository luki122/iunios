package com.aurora.thememanager.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import aurora.view.ViewPager;
import aurora.view.ViewPager.OnPageChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;

import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.FragmentPagerAdapter;
import com.aurora.thememanager.fragments.PagerSlidingTabStrip;
import com.aurora.thememanager.fragments.SuperAwesomeCardFragment;
import com.aurora.thememanager.utils.FileSizeUtils;
import com.aurora.thememanager.utils.JsonMapUtils;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.utils.Log;

//@AuroraInit(actionBarTitleId = R.string.app_name,actionBarType = Type.Empty)
public class MainActivity extends BaseActivity implements OnPageChangeListener{

	
	
	private AuroraViewPager mViewPager;
	
	private String[] mTabNames;
	
	private AuroraTabWidget mTab;
	private TabAdapter mTabAdapter;
	
	private BroadcastReceiver mNetReceiver;
	
	private int mShopPosition = -1;
	
	/**
	 * 网络连接状态的监听器
	 */
	private ArrayList<OnNetworkChangeListener> mNetworkListener = new ArrayList<OnNetworkChangeListener>();
	
	public interface OnNetworkChangeListener{
		public void onNetConnnectedChange(boolean hasNetwork);
	}
	
	public void addNetworkListener(OnNetworkChangeListener listener){
		this.mNetworkListener.add(listener);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mApp.registerActivity(this);
		Intent intent = getIntent();
		mNetReceiver = new NetReceiver();
		IntentFilter netConnectFilter = new IntentFilter();
		netConnectFilter.addAction(Action.ACTION_NETWORK_CONNECT);
		registerReceiver(mNetReceiver, netConnectFilter);
		if(intent != null){
			mShopPosition = intent.getIntExtra(Action.KEY_GOTO_SHOP, -1);
		}
		setAuroraContentView(R.layout.activity_main,AuroraActionBar.Type.Empty);
		mActionBar = getAuroraActionBar();
		initTab();
		initActionBar();
		setAuroraActionbarSplitLineVisibility(View.GONE);
		FileSizeUtils fs = new FileSizeUtils();
	}
	private void initTab(){
		
		mTabNames = getResources().getStringArray(R.array.tab_names);
		mTab = (AuroraTabWidget)findViewById(R.id.tab_widget);
		mViewPager = mTab.getViewPager();
		mViewPager.setId(R.id.tab_view_pager);
		mViewPager.setAdapter(new TabAdapter(getFragmentManager()));
		mViewPager.setOffscreenPageLimit(3);
		mTabAdapter = (TabAdapter) mViewPager.getAdapter();
		if(mShopPosition != -1){
			mViewPager.setCurrentItem(mShopPosition);
		}
	}
	public void initActionBar(){
		mActionBar.setElevation(0f);
		mActionBar.setTitle(R.string.app_name);
		mActionBar.addItem(R.drawable.btn_downloaded, R.id.action_bar_item_local,"");
		mActionBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
			
			@Override
			public void onAuroraActionBarItemClicked(int itemId) {
				// TODO Auto-generated method stub
				if(itemId == R.id.action_bar_item_local){
					startActivity(new Intent(MainActivity.this,LocalThemeActivity.class));
				}
			}
		});
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mNetReceiver);
	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	public void gotoPage(View view) {
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	
	
	
	
	public class TabAdapter extends FragmentPagerAdapter {

		private final String[] TITLES = mTabNames;

		public TabAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			return SuperAwesomeCardFragment.newInstance(position);
		}

	}





	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	class NetReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(Action.ACTION_NETWORK_CONNECT.equals(action)){
				int listenerSize = mNetworkListener.size();
				if(listenerSize > 0){
					for(OnNetworkChangeListener listener:mNetworkListener){
						listener.onNetConnnectedChange(SystemUtils.isNetworkConnected(context));
					}
					
				}
			}
		}
		
	}
	
	
	
	
	


}
