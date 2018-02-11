package com.aurora.market.activity.module;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomActionBar;
import aurora.widget.AuroraCustomActionBar.onOptionItemClickListener;

import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.MarketMainActivity;
import com.aurora.market.R;
import com.aurora.market.activity.BaseActivity;
import com.aurora.market.activity.fragment.AppListFragment;
import com.aurora.market.activity.setting.MarketManagerPreferenceActivity;
import com.aurora.market.adapter.FragmentAdapter;
import com.aurora.market.util.Globals;
import com.aurora.market.util.SearchUtils;

public class AppRankingActivity extends BaseActivity implements
		OnClickListener, INotifiableController {

	private ViewPager viewPager;
	private FragmentAdapter adapter;

	private TextView tab_app_ranking;
	private TextView tab_game_ranking;
	private View tabLine;

	private List<Fragment> fragmentList;
	private AppListFragment appRankingFragment;
	private AppListFragment gameRankingFragment;

	private AuroraCustomActionBar mActionBar;
	public ImageView main_update;
	private static final int MENU_SETTING = 0;
	private static final int MENU_SEARCH = 1;

	private SearchUtils mSearchUtils;

	private ImageView mSearchImgV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// this.setTheme(com.aurora.R.style.Theme_Aurora_Dark_Transparent);
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_app_ranking,
				AuroraActionBar.Type.NEW_COSTOM, true);

		initViews();
		initActionBar();
		setListener();
		initdata();

		mSearchUtils = new SearchUtils();
		mSearchUtils.initSearchMode(this);
	}

	private void initViews() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		tab_app_ranking = (TextView) findViewById(R.id.tab_app_ranking);
		tab_game_ranking = (TextView) findViewById(R.id.tab_game_ranking);
		tabLine = findViewById(R.id.tab_line);

		changeTabStyle(0);
	}

	private void initActionBar() {
		mActionBar = getCustomActionBar();
		mActionBar.setTitle(R.string.tab_ranking);
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));

		mActionBar
		.setDefaultOptionItemDrawable(getResources()
				.getDrawable(
						R.drawable.btn_main_right_selector));
		
		
		mActionBar.showDefualtItem(false);
		mActionBar.addItemView(R.layout.actionbar_main_right);
		mActionBar.addItemView(R.layout.actionbar_search_item);
		mSearchImgV = (ImageView) mActionBar.findViewById(R.id.bar_search_item);
		main_update = (ImageView) mActionBar.findViewById(R.id.actionbar_main_update);
		View view = mActionBar.findViewById(R.id.download_layout);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AppRankingActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});
		// addAuroraActionBarItem(AuroraActionBarItem.Type.Add,
		// AURORA_NEW_MARKET);
		/*mActionBar.addItem(R.layout.actionbar_search_item, MENU_SEARCH);
		mSearchImgV = (ImageView) mActionBar.findViewById(R.id.bar_search_item);

		mActionBar.addItem(R.drawable.btn_main_right_selector, MENU_SETTING,
				getResources().getString(R.string.app_name));
		
		mActionBar
				.setOnAuroraActionBarListener(auroraActionBarItemClickListener);*/
		
		/*mActionBar
		.setOnOptionItemClickListener(new onOptionItemClickListener() {

			@Override
			public void click(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AppRankingActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});*/
		// addAuroraActionBarItem(AuroraActionBarItem.Type.Add,
		// AURORA_NEW_MARKET);
		/*mActionBar.addItem(R.layout.actionbar_search_item, MENU_SEARCH);
		mSearchImgV = (ImageView) mActionBar.findViewById(R.id.bar_search_item);

		mActionBar.addItem(R.drawable.btn_main_right_selector, MENU_SETTING,
				getResources().getString(R.string.app_name));
		mActionBar
				.setOnAuroraActionBarListener(auroraActionBarItemClickListener);*/
	}
	
	public void setAnimal1(final ImageView view) {
		((AppListFragment)fragmentList.get(viewPager.getCurrentItem())).setAnimal1(view);
	}
	
	private void setListener() {
		tab_app_ranking.setOnClickListener(this);
		tab_game_ranking.setOnClickListener(this);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				changeTabStyle(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				/**
				 * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。 其中三个参数的含义分别为： position:
				 * 当前页面，及你点击滑动的页面。 positionOffset: 当前页面偏移的百分比。
				 * positionOffsetPixels: 当前页面偏移的像素位置。
				 */

				if (positionOffsetPixels != 0 && position == 0) {
					tabLine.setTranslationX((float) positionOffsetPixels / 2);
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		mSearchImgV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Fragment fragment = fragmentList.get(viewPager.getCurrentItem());
				if (fragment != null) {
					if(null!=((AppListFragment) fragment).getmListView())
						((AppListFragment) fragment).getmListView().auroraOnPause();
				}
				mSearchUtils.startSearchMode();
			}
		});
		
		setOnSearchViewQuitListener(new OnSearchViewQuitListener() {
			
			@Override
			public boolean quit() {
				Fragment fragment = fragmentList.get(viewPager.getCurrentItem());
				if (fragment != null && ((AppListFragment) fragment).getmListView() != null) {
					((AppListFragment) fragment).getmListView().auroraOnResume();
				}
				return false;
			}
		});
	}

	private void initdata() {
		appRankingFragment = AppListFragment.newInstance(AppListActivity.TYPE_RANK, Globals.TYPE_APP,
				-1);
		gameRankingFragment = AppListFragment.newInstance(AppListActivity.TYPE_RANK, Globals.TYPE_GAME,
				-1);
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(appRankingFragment);
		fragmentList.add(gameRankingFragment);
		adapter = new FragmentAdapter(getFragmentManager(), fragmentList);
		viewPager.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_app_ranking:
			if (fragmentList != null && fragmentList.size() > 0) {
				viewPager.setCurrentItem(0);
			}
			break;
		case R.id.tab_game_ranking:
			if (fragmentList != null && fragmentList.size() > 1) {
				viewPager.setCurrentItem(1);
			}
			break;
		}
	}

	private void changeTabStyle(int position) {
		if (position == 0) {
			tab_app_ranking.setSelected(true);
			tab_game_ranking.setSelected(false);
		} else {
			tab_app_ranking.setSelected(false);
			tab_game_ranking.setSelected(true);
		}
	}

	
	
	@Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        
        if(this.isSearchviewLayoutShow()){
            hideSearchviewLayout();
        }

    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		mSearchUtils.removeSearchMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			
			if( mSearchUtils.isInSearchMode() ){
				AppRankingActivity.this.hideSearchViewLayoutWithOnlyAlphaAnim();
				mSearchUtils.setSearchMode(false);
			}
			
		}
		
		return super.onKeyDown(keyCode, event);
	}
    
	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);

		}

	};
}
