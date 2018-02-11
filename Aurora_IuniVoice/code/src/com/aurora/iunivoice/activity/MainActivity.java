package com.aurora.iunivoice.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.fragment.BaseViewPagerFragment;
import com.aurora.iunivoice.activity.fragment.FindFragment;
import com.aurora.iunivoice.activity.fragment.ForumFragment;
import com.aurora.iunivoice.activity.fragment.HomePageFragment;
import com.aurora.iunivoice.activity.fragment.PersonalFragment;
import com.aurora.iunivoice.adapter.FragmentAdapter;
import com.aurora.iunivoice.service.SystemTipService;
import com.aurora.iunivoice.update.UpdateApp;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.DialogUtil.IAlertDialogClick;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ToastUtil;

public class MainActivity extends BaseActivity implements OnClickListener {

	public static final String TAG = "MainActivity";

	private static final int ACTIONBAR_ID_MESSAGE = 0x4521; // actionbar消息id
	private static final int ACTIONBAR_ID_SETTING = 0x1245;

	private AccountHelper mAccountHelper;
	private LocalLoginReceiver localLoginReceiver;

	private ViewPager viewPager;
	private LinearLayout tab_homepage;
	private LinearLayout tab_forum;
	private LinearLayout tab_post;
	private LinearLayout tab_find;
	private LinearLayout tab_personal;

	private FragmentAdapter adapter;
	private List<Fragment> fragmentList;
	private HomePageFragment homePageFragment;
	private ForumFragment forumFragment;
	private FindFragment findFragment;
	private PersonalFragment personalFragment;

	private int tabPostion = -1; // 当前选中tab位置

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/** read account info from content provider */
		if (AccountUtil.getInstance().isIuniOS()) {
			mAccountHelper = AccountHelper.getInstance(this);
			mAccountHelper.registerAccountContentResolver();
			mAccountHelper.update();
		} else {
			mAccountHelper = AccountHelper.getInstance(this);
			mAccountHelper.updateFromLocal();
			localLoginReceiver = new LocalLoginReceiver();
			IntentFilter filter = new IntentFilter(Globals.LOCAL_LOGIN_ACTION);
			registerReceiver(localLoginReceiver, filter);
		}
		boolean isLogin = getIntent().getBooleanExtra("login", false);
		if(isLogin){
			AccountUtil.getInstance().startLogin(this);
		}
		enableBackItem(false);

		setupViews();
		initData();

		changeActionBarItem(0);
		changeTabStyle(0);

		UpdateApp.getInstance().checkUpdateApp(this, false,
				new IuniVoiceManager(this));
		toNewsPage();

	}

	/**
	 * 点击通知栏跳转到我的消息页面
	 */
	private void toNewsPage(){
		if (getIntent() == null) {
			return;
		}
		boolean isFromTipService = getIntent().getBooleanExtra(
				SystemTipService.FROM_TIP_SERVICE_KEY, false);
		if (isFromTipService) {
			if(!AccountUtil.getInstance().isLogin())
            {
				AccountUtil.getInstance().startLogin(this);
				return;
			}
			startActivity(new Intent(this, MineNewsActivity.class));
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		toNewsPage();
	}
	
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.main_homepage);
	}

	@Override
	public void setupViews() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		tab_homepage = (LinearLayout) findViewById(R.id.tab_homepage);
		tab_forum = (LinearLayout) findViewById(R.id.tab_forum);
		tab_post = (LinearLayout) findViewById(R.id.tab_post);
		tab_find = (LinearLayout) findViewById(R.id.tab_find);
		tab_personal = (LinearLayout) findViewById(R.id.tab_personal);

		tab_homepage.setOnClickListener(this);
		tab_forum.setOnClickListener(this);
		tab_post.setOnClickListener(this);
		tab_find.setOnClickListener(this);
		tab_personal.setOnClickListener(this);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				changeTabStyle(position);
				changeActionBarItem(position);
				loadFragmentData(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		viewPager.setOffscreenPageLimit(4); // ViewPager页4项缓存
	}

	public void initData() {
		homePageFragment = new HomePageFragment();
		forumFragment = new ForumFragment();
		findFragment = new FindFragment();
		personalFragment = new PersonalFragment();
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(homePageFragment);
		fragmentList.add(forumFragment);
		fragmentList.add(findFragment);
		fragmentList.add(personalFragment);
		adapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList);
		viewPager.setAdapter(adapter);
	}

	private void changeActionBarItem(int position) {
		if (tabPostion == position)
			return;

		tabPostion = position;
		if (tabPostion == 0) {
			removeActionBarItem(ACTIONBAR_ID_SETTING);
			addActionBarItem(R.drawable.icon_message_selector, ACTIONBAR_ID_MESSAGE);
		} else if (position == 3) {
			removeActionBarItem(ACTIONBAR_ID_MESSAGE);
			addActionBarItem(R.drawable.icon_setting_selector, ACTIONBAR_ID_SETTING);
		} else {
			removeActionBarItem(ACTIONBAR_ID_SETTING);
			removeActionBarItem(ACTIONBAR_ID_MESSAGE);
		}

	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);

		switch (itemId) {
		case ACTIONBAR_ID_MESSAGE:// 跳转到我的消息页面
			if(!AccountUtil.getInstance().isLogin())
            {
				AccountUtil.getInstance().startLogin(this);
				return;
			}
			startActivity(new Intent(this, MineNewsActivity.class));
			break;
		case ACTIONBAR_ID_SETTING:// 跳转到设置页面

			startActivity(new Intent(this, MineSettingActivity.class));
			break;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_homepage:
			viewPager.setCurrentItem(0, false);
			break;
		case R.id.tab_forum:
			viewPager.setCurrentItem(1, false);
			break;
		case R.id.tab_post:
			if (!AccountUtil.getInstance().isLogin()) {
				AccountUtil.getInstance().startLogin(this);
			} else {
				if (!TextUtils.isEmpty(homePageFragment.getFormhash())) {
					Intent i = new Intent(MainActivity.this,
							PublishActivity.class);
					i.putExtra(PublishActivity.FORMHASH,
							homePageFragment.getFormhash());
					startActivity(i);
				} else {
					ToastUtil.longToast(R.string.network_error_please_try_again_later);
				}
			}

			break;
		case R.id.tab_find:
			viewPager.setCurrentItem(2, false);
			break;
		case R.id.tab_personal:
			viewPager.setCurrentItem(3, false);
			break;
		}
	}

	/**
	 * “我的页面”点击随便看看时调用
	 */
	public void showMainFragment() {
		changeTabStyle(0);
		viewPager.setCurrentItem(0);
	}

	private void changeTabStyle(int position) {
		switch (position) {
		case 0:
			tab_homepage.setSelected(true);
			tab_forum.setSelected(false);
			tab_find.setSelected(false);
			tab_personal.setSelected(false);

			setTitleRes(R.string.main_homepage);
			break;
		case 1:
			tab_homepage.setSelected(false);
			tab_forum.setSelected(true);
			tab_find.setSelected(false);
			tab_personal.setSelected(false);

			setTitleRes(R.string.forum_list);
			break;
		case 2:
			tab_homepage.setSelected(false);
			tab_forum.setSelected(false);
			tab_find.setSelected(true);
			tab_personal.setSelected(false);

			setTitleRes(R.string.main_find);
			break;
		case 3:
			tab_homepage.setSelected(false);
			tab_forum.setSelected(false);
			tab_find.setSelected(false);
			tab_personal.setSelected(true);

			setTitleRes(R.string.main_personal);
			break;
		}
	}

	private void loadFragmentData(int position) {
		Log.i(TAG, "MainActivity loadFragmentData: " + position);
		BaseViewPagerFragment f = (BaseViewPagerFragment) fragmentList
				.get(position);
		f.checkAndLoadData();
	}

	private class LocalLoginReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int result = intent.getIntExtra(Globals.LOCAL_LOGIN_RESULT,
					Globals.LOCAL_LOGIN_FAIL);

			if (result == Globals.LOCAL_LOGIN_SUCCESS) {
				if (mAccountHelper != null) {
					mAccountHelper.updateFromLocal();
				}
				if (personalFragment != null) {
					personalFragment.layoutChange();
				}
			} else if (result == Globals.LOCAL_LOGOUT_SUCCESS) {
				if (personalFragment != null) {
					personalFragment.layoutChange();
				}
			}
		}

	};

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);

		if (arg0 == Globals.REQUEST_LOGIN_CODE) {
			if (arg1 == RESULT_OK) {
				if (personalFragment != null) {
					personalFragment.layoutChange();
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(localLoginReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			DialogUtil.getAlertDialog(MainActivity.this,
					getString(R.string.main_homepage_dialog_exit_tips),
					R.string.dialog_cancel, R.string.dialog_confirm,
					new IAlertDialogClick() {

						@Override
						public void sureClick() {
							finish();
						}

						@Override
						public void cancelClick() {

						}

					}).show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
