package com.aurora.market.activity.module;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.app.AuroraAlertDialog;
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
import com.aurora.market.activity.setting.UpdateSettingsPreferenceActivity;
import com.aurora.market.util.Globals;
import com.aurora.market.util.SearchUtils;

public class AppListActivity extends BaseActivity implements OnSearchViewQuitListener, 
	INotifiableController {

	public static final String OPEN_TYPE = "open_type";
	public static final String CATEGORY_ID = "category_id";
	public static final String CATEGORY_NAME = "category_name";
	public static final String SPECIAL_ID = "special_id";
	public static final String SPECIAL_NAME = "special_name";
	// 0主界面 1 新品 2排行 3分类 4.专题  5 必备  6 设计 7 专题主页 8 分类主页
	public static final int TYPE_NEW = 1;
	public static final int TYPE_RANK = 2;
	public static final int TYPE_CATEGORY = 3;
	public static final int TYPE_SPECIAL = 4;
	public static final int TYPE_STARTER = 5;
	public static final int TYPE_DESIGN = 6;
	public static final int TYPE_SPECIAL_MAIN = 7;
	public static final int TYPE_CATEGORY_MAIN = 8;
	private AppListFragment appListFragment;

	private AuroraCustomActionBar mActionBar;
	public ImageView main_update;
	public View installAllBtn;
	private int openType;
	private String categoryName;
	private int categoryId;
	private String specialName;
	private int specialId;

	private SearchUtils mSearchUtils;
	private ImageView mSearchImgV;
	SharedPreferences oneKeyPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_applist,
				AuroraActionBar.Type.NEW_COSTOM, true);
		
		getIntentData();
		initViews();
		initActionBar();
		setListener();
		initdata();

		mSearchUtils = new SearchUtils();
		mSearchUtils.initSearchMode(this);
	}
	
	@Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        
		if (this.isSearchviewLayoutShow()) {
			hideSearchviewLayout();
		}
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		mSearchUtils.removeSearchMode();
	}

	private void getIntentData() {
		openType = getIntent().getIntExtra(OPEN_TYPE, TYPE_STARTER);
		if (openType == TYPE_CATEGORY) {
			categoryName = getIntent().getStringExtra(CATEGORY_NAME);
			categoryId = getIntent().getIntExtra(CATEGORY_ID, -1);
		} else if (openType == TYPE_SPECIAL) {
			specialName = getIntent().getStringExtra(SPECIAL_NAME);
			specialId = getIntent().getIntExtra(SPECIAL_ID, -1);
		}
	}

	public void setAnimal1(final ImageView view) {
		appListFragment.setAnimal1(view);
	}

	private void initViews() {
		installAllBtn = findViewById(R.id.download_btn_layout);
		String girls = getResources().getString(R.string.girls_language);
		if (specialName != null && !specialName.isEmpty() && specialName.equals(girls)) {
			oneKeyPref = getSharedPreferences(
					Globals.SHARED_ONE_KEY_FOR_HER, Activity.MODE_PRIVATE);
			boolean enable = oneKeyPref.getBoolean(
					"one_key_4her", true);
			if (enable) {
				installAllBtn.setVisibility(View.VISIBLE);
				installAllBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						appListFragment.installAll();
						Animation animation;
						animation = AnimationUtils.loadAnimation(AppListActivity.this, com.aurora.R.anim.aurora_menu_exit); 
						installAllBtn.startAnimation(animation); 
						installAllBtn.setVisibility(View.GONE);
						oneKeyPref.edit().putBoolean("one_key_4her", false)
								.commit();
					}
					
				});
			} else {
				installAllBtn.setVisibility(View.GONE);
			}
		}
		
	}
	
	private void initActionBar() {
		mActionBar = getCustomActionBar();
		if (openType == TYPE_NEW) {
			mActionBar.setTitle(R.string.tab_new);
		} 
		else if (openType == TYPE_STARTER) {
			mActionBar.setTitle(R.string.tab_starter);
		} 
		else if (openType == TYPE_DESIGN) {
			mActionBar.setTitle(R.string.tab_design);
		} 
		else if (openType == TYPE_SPECIAL) {
			mActionBar.setTitle(specialName);
		} else {
			mActionBar.setTitle(categoryName);
		}
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
				Intent intent = new Intent(AppListActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});
		
	}

	private void initdata() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		int type = 0;
		type = openType;
		if (type == 4) {
			appListFragment = AppListFragment.newInstance(type, Globals.TYPE_APP,
					specialId);
		} else {
			appListFragment = AppListFragment.newInstance(type, Globals.TYPE_APP,
					categoryId);
		}
		ft.add(R.id.container, appListFragment);
		ft.commit();
	}

	private void setListener() {
		
	/*	mActionBar.setOnOptionItemClickListener(new onOptionItemClickListener() {
			@Override
			public void click(View view) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AppListActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});*/
		
		mSearchImgV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				appListFragment.getmListView().auroraOnPause();
				mSearchUtils.startSearchMode();
			}
		});
		
		setOnSearchViewQuitListener(new OnSearchViewQuitListener() {
			
			@Override
			public boolean quit() {
				appListFragment.getmListView().auroraOnResume();
				return false;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			
			if( mSearchUtils.isInSearchMode() ){
				AppListActivity.this.hideSearchViewLayoutWithOnlyAlphaAnim();
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

	@Override
	public boolean quit() {
		// TODO Auto-generated method stub
		return false;
	}

}
