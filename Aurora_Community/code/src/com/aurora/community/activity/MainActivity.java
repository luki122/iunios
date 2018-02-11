package com.aurora.community.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.fragment.BaseFragment;
import com.aurora.community.activity.fragment.NewsCategoryFragment;
import com.aurora.community.activity.fragment.NewsFragment;
import com.aurora.community.activity.fragment.NewsFragment.RefreshMode;
import com.aurora.community.activity.fragment.UserFragment;
import com.aurora.community.activity.picBrowser.ImagePickerActvity;
import com.aurora.community.activity.twitter.TwitterNoteActivity;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.AccountHelper.IAccountChange;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.DensityUtil;
import com.aurora.community.utils.FragmentHelper;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.MessageBoxHolder;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.umeng.analytics.MobclickAgent;


public class MainActivity extends BaseActivity implements View.OnClickListener,
		FragmentHelper.IFragmentController,
		BaseFragment.INewsNotifiableController{

	private FragmentManager manager;

	private TextView tv_home, tv_user;

	private ImageButton ib_post;

	private AccountHelper mAccountHelper;

	private final String FRAGMENT_DEFAULT_NEWS_TAG = "news";
	private final String FRAGMENT_DEFAULT_USER_TAG = "user";

	private CommunityApp app;

	public static final int MSG_LOGIN = 0x01;
    public static final String SHOW_PAGE_KEY = "show_page_key"; 
	public static final String SHOW_USER_CENTER = "show_user_center";
	private NewsCategoryFragment main_manager;
	private Fragment news_manager;
	private Fragment personal_manager;
	// private FragmentHelper fmHelper;

	private Fragment mainFragment;

	/** for no network layout */
	private RelativeLayout content;
	private LinearLayout bottom;
	private RelativeLayout networkLayer;
    private Button SetupNetworkButton;
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;

	public static final String COME_FROM_KEY = "come_from";

	private TextView TextErrorContent;
	private Button NetworkRetryButton;
    private String CurrentTag;
    
    private ProgressBar mLoadingBar;
    
    private LocalLoginReceiver localLoginReceiver;
    
    public static final String REFRESH_MAIN_CATEGORY_ACTION = "com.aurora.refresh.main.category.action";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("linp", "~~~~~~~~~~~~~onCreate");
		super.onCreate(savedInstanceState);
//		IntentFilter mFilter = new IntentFilter();
//		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//		registerReceiver(mReceiver, mFilter);
		
		setContentView(R.layout.activity_main);
		mComanager = new CommunityManager(this);
		// 在任何地方调用时都这么写.
		app = (CommunityApp)this.getApplication();
		app.setInstance(this);
		app.addActivity(this);
		
		manager = getFragmentManager();
		setupViews();
		enableBackItem(false);
		initFragment();
		
		/** read account info from content provider */
		if (AccountUtil.getInstance().isIuniOS()) {
			mAccountHelper = new AccountHelper(this);
			mAccountHelper.setIAccountChangeListener(iAccountChange);
			mAccountHelper.registerAccountContentResolver();
			mAccountHelper.update();
		} else {
			mAccountHelper = new AccountHelper(this);
			mAccountHelper.setIAccountChangeListener(iAccountChange);
			mAccountHelper.updateFromLocal();
			localLoginReceiver = new LocalLoginReceiver();
			IntentFilter filter = new IntentFilter(Globals.LOCAL_LOGIN_ACTION);
			registerReceiver(localLoginReceiver, filter);
		}

		/** setting content area to accurate content */
		
		// count the enter main page
		/*new TotalCount(MainActivity.this, "300", "001", 1).CountData();*/
		showTipImage();
		registeAction();
	}
	
	private int[] userIcons = {R.drawable.aurora_user_selector,R.drawable.have_message_user};
	private Drawable[]  userDrawables = new Drawable[2];
	
	private void setupUserIcons(){
		userDrawables[0] = getResources().getDrawable(userIcons[0]);
		userDrawables[1] = getResources().getDrawable(userIcons[1]);
	}
	
	private void registeAction(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(REFRESH_MAIN_CATEGORY_ACTION);
		registerReceiver(refreshCategoryReciver, filter);
	}
	
	private BroadcastReceiver refreshCategoryReciver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(REFRESH_MAIN_CATEGORY_ACTION))
			{
				if(main_manager != null)
				{
					main_manager.refresh();
				}
			}
			
		};
		
	};
	
	private ImageView iv_publish_tip;
	
	private static final String IS_NEW_IN_KEY = "isNewIn";
	
	private boolean isRunning = true;
	
	private class PublishTipThread extends Thread{
		
		private int maxMargin,minMargin;
		
		private int nowMargin;
		
		private boolean isReverse = false;
		
		public PublishTipThread(){
			maxMargin = DensityUtil.dip2px(getBaseContext(), 12);
			minMargin = DensityUtil.dip2px(getBaseContext(), 5);
			nowMargin = maxMargin;
		}
		
		private void setMargin(int margin){
			if(iv_publish_tip != null)
			{
				RelativeLayout.LayoutParams lp = (LayoutParams) iv_publish_tip.getLayoutParams();
				lp.bottomMargin = margin;
				iv_publish_tip.setLayoutParams(lp);
			}
		}
		@Override
		public void run() {
			super.run();
			
			while(isRunning)
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setMargin(nowMargin);
					}
				});
				
				if(!isReverse)
				{
					nowMargin--;
				}else{
					nowMargin++;
				}
				
				if(nowMargin < minMargin)
				{
					nowMargin = minMargin;
					isReverse = true;
				}else if(nowMargin > maxMargin)
				{
					nowMargin = maxMargin;
					isReverse = false;
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	
	private void showTipImage(){
		if(!SystemUtils.hasActiveNetwork(this))
		{
			return;
		}
		SharedPreferences sp = getPreferences(0);
		if(!sp.contains(IS_NEW_IN_KEY))
		{
			iv_publish_tip = (ImageView) findViewById(R.id.iv_publish_tip);
			new PublishTipThread().start();
			iv_publish_tip.setVisibility(View.VISIBLE);
			sp.edit().putBoolean(IS_NEW_IN_KEY, true).commit();
		}
	}
	
	
	
	@Override
	public void setupAuroraActionBar() {
		// TODO Auto-generated method stub
		super.setupAuroraActionBar();
		setTitleRes(R.string.main_aurora_actionbar_title);
	}
	
	
	private IAccountChange iAccountChange = new IAccountChange() {
		
		@Override
		public void changeAccount() {
			// TODO Auto-generated method stub
			if(personal_manager != null)
			{
				((UserFragment)personal_manager).changeAccount();
			}
		}

		@Override
		public void unLogin() {
			// TODO Auto-generated method stub
			if(personal_manager != null)
			{
				((UserFragment)personal_manager).logOut();
			}
			
			tv_home.performClick();
		}
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		restoreDefaultFocusToWidget();
		requestMessageBox();
		super.onResume();
	}

	private void showMessageButton(boolean isShow){
		if(isShow && AccountHelper.mLoginStatus)
		{
			addActionBarItem(haveNewMessage ? R.drawable.have_message_selector : R.drawable.no_message_selector, MESSAGE_ITEM_ID);
		}else{
			removeActionBarItem(MESSAGE_ITEM_ID);
		}
	}

	@Override
	public void initFragment() {
		boolean showUserCenter = getIntent().getStringExtra(SHOW_PAGE_KEY) != null;
		// TODO Auto-generated method stub
		if (main_manager == null && news_manager == null
				&& personal_manager == null) {
			FragmentTransaction t = manager.beginTransaction();
			main_manager = new NewsCategoryFragment();

			news_manager = new NewsFragment();

			personal_manager = new UserFragment();
			mainFragment = main_manager;
			t.add(R.id.content, mainFragment, "newscategory");
			t.add(R.id.content, personal_manager, "personal");
			t.add(R.id.content, news_manager, "news");
			if(showUserCenter)
			{
				t.hide(main_manager);
				tv_user.setFocusable(true);
				tv_user.requestFocus();
				tv_user.setFocusableInTouchMode(true);
				tv_home.clearFocus();
				tv_home.setFocusable(false);
				tv_home.setFocusableInTouchMode(false);
			}else{
				t.hide(personal_manager);
			}
			t.hide(news_manager);
			t.commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}
    

	@Override
	public void setupViews() {
		setupUserIcons();
		// TODO Auto-generated method stub
		tv_home = (TextView) findViewById(R.id.tv_home);
		tv_user = (TextView) findViewById(R.id.tv_user);
		SetupNetworkButton  =(Button)findViewById(R.id.setup_network);
		ib_post = (ImageButton) findViewById(R.id.ib_post);

		renderTextViewWithDrawables(tv_home,
				getResources().getDrawable(R.drawable.aurora_home_selector));
		renderTextViewWithDrawables(tv_user,userDrawables[0]);

		content = (RelativeLayout) findViewById(R.id.content);
		bottom = (LinearLayout) findViewById(R.id.bottom_layout);
		networkLayer = (RelativeLayout) findViewById(R.id.network_layout);

		TextErrorContent = (TextView) findViewById(R.id.tv_error_content);
		NetworkRetryButton = (Button) findViewById(R.id.bt_retry_network);
		mLoadingBar = (ProgressBar)findViewById(R.id.loadingbar);
		NetworkRetryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if(!SystemUtils.isNetworkAvailable())
				{
					ToastUtil.shortToast(R.string.net_error);
					return;
				}
				mLoadingBar.setVisibility(View.VISIBLE);
		    	content.setVisibility(View.GONE);	
		    	bottom.setVisibility(View.GONE);
		    	networkLayer.setVisibility(View.GONE);
		    	showTipImage();
				if(mainFragment!=null){
					if(mainFragment.getTag().equals("newscategory")){
						main_manager.getCategoryInfo();
						Log.e("linp", "retry button retry getCategoryInfo");
					}else{
						Log.e("linp", "retry button retry getArticleInfo");
						((NewsFragment)news_manager).getArticleInfo(1);	
					}
				}else{
					//for test
					hideNoNetWorkLayer();
					Log.e("linp", "MainActivity NetworkRetryButton mainFragment==null");
				}

			}
		});

		tv_home.setOnClickListener(this);
		tv_user.setOnClickListener(this);
		ib_post.setOnClickListener(this);
		
		tv_home.setFocusable(true);
		tv_home.requestFocus();
		tv_home.setFocusableInTouchMode(true);
	}

	public void switchContent(Fragment fragment,String title) {

		if (mainFragment == fragment) {
			return;
		}
		
		showMessageButton(fragment == personal_manager);
		
		FragmentTransaction t = getFragmentManager().beginTransaction();
		t.hide(mainFragment);
		mainFragment = fragment;
		changeBottomIcon();
		t.show(mainFragment);
		t.commitAllowingStateLoss();
		setTitleText(title);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(mainFragment!=null&&mainFragment == news_manager)
			{
				switchContent(main_manager, getString(R.string.main_aurora_actionbar_title));
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private static final int MESSAGE_ITEM_ID = 0x45212;
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		requestMessageBox();
		switch (arg0.getId()) {
		case R.id.tv_home:
			tv_user.clearFocus();
			tv_user.setFocusable(false);
			tv_user.setFocusableInTouchMode(false);
			tv_home.requestFocus();
			tv_home.setFocusable(true);
			tv_home.setFocusableInTouchMode(true);
			switchContent(main_manager,getString(R.string.main_aurora_actionbar_title));
			/*new TotalCount(MainActivity.this, "300", "002", 1).CountData();*/
			MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_MAIN);
			changeBottomIcon();
			break;

		case R.id.tv_user:
			tv_home.clearFocus();
			tv_home.setFocusable(false);
			tv_home.setFocusableInTouchMode(false);
			tv_user.requestFocus();
			tv_user.setFocusable(true);
			tv_user.setFocusableInTouchMode(true);
			
			if (!AccountUtil.getInstance().isLogin()) {
				startLogin();
			} else {
				/*new TotalCount(MainActivity.this, "300", "004", 1).CountData();*/
				MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_MY);
				switchContent(personal_manager,getString(R.string.user_center_title));
			}
			changeBottomIcon();
			break;

		case R.id.ib_post:
			startTwitterNote();
			/*new TotalCount(MainActivity.this, "300", "003", 1).CountData();*/
			MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_PUBLISH);
			break;

		}
	}


	/** setup bottom textview's drawble */
	private void renderTextViewWithDrawables(TextView tv, Drawable d) {
		tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
		tv.setGravity(android.view.Gravity.CENTER);

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		isRunning = false;
		if(iv_publish_tip != null)
		{
			iv_publish_tip.setVisibility(View.GONE);
			iv_publish_tip = null;
		}
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (AccountUtil.getInstance().isIuniOS()) {
			mAccountHelper.unregisterAccountContentResolver();
		}
		
		if (localLoginReceiver != null) {
			unregisterReceiver(localLoginReceiver);
		}
		
		app.setInstance(null);
		unregisterReceiver(refreshCategoryReciver);
		super.onDestroy();
	}

	private void startLogin() {
		AccountUtil.getInstance().startLogin(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.i("zhangwei", "zhangwei the requestCode=" + requestCode
				+ " the resultCode=" + resultCode);
		switch (requestCode) {
		case Globals.REQUEST_LOGIN_CODE:
			if (resultCode == Activity.RESULT_OK) {
				switchContent(personal_manager,getString(R.string.user_center_title));
				/*new TotalCount(MainActivity.this, "300", "004", 1).CountData();*/
				MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_MY);
				if(personal_manager != null)
				{
			
//					((UserFragment)personal_manager).refreshPageForNewPublish();
				}
				setTitleText(getString(R.string.user_center_title));
			}
			break;
		case Globals.REQUEST_LOGOUT_CODE:
			if (resultCode == Activity.RESULT_OK) {
				if(personal_manager != null)
				{
					((UserFragment)personal_manager).logOut();
				}
				
				tv_home.performClick();
			}
			break;

		default:
			break;
		}

	}


	public void setDisView(String comeFrom)
	{
		if (TextUtils.isEmpty(comeFrom)) {
			return;
		}
		if (comeFrom.equals(PostDetailActivity.class.getName())) {
			if (personal_manager != null) {
				if(tv_home.hasFocus()){
					tv_home.clearFocus();
					tv_home.setFocusable(false);
					tv_home.setFocusableInTouchMode(false);
				}
				tv_user.requestFocus();
				tv_user.setFocusable(true);
				tv_user.setFocusableInTouchMode(true);
				switchContent(personal_manager,getString(R.string.user_center_title));
			}
			
		}else if(comeFrom.equals(TwitterNoteActivity.class.getName()))
		{
			if(tv_home.hasFocus()){
				tv_home.clearFocus();
				tv_home.setFocusable(false);
				tv_home.setFocusableInTouchMode(false);
			}
			tv_user.requestFocus();
			tv_user.setFocusable(true);
			tv_user.setFocusableInTouchMode(true);
			if (personal_manager != null) {
				switchContent(personal_manager,getString(R.string.user_center_title));
				((UserFragment)personal_manager).refreshPageForNewPublish();
			}
		}
	}
	
	
	/** enter post activity */
	private void startTwitterNote() {
		if (!AccountHelper.mLoginStatus) {
			startLogin();
		} else {
			Intent intent = new Intent(this, ImagePickerActvity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onNewsClickResponse() {
		// TODO Auto-generated method stub

	}
	

	@Override
	public void onCategoryClickResponse(String tid,String title,String postCount) {
		// TODO Auto-generated method stub
		Log.e("linp", "tid="+tid);
		((NewsFragment) news_manager).setRefreshMode(RefreshMode.REFRESH_MODE);
		((NewsFragment) news_manager).setTid(tid,postCount);
		switchContent(news_manager,title);
		Log.e("linp", "onCategoryClickResponse");
	}

	private boolean haveNewMessage = false;
	
	private void requestMessageBox(){
		if(!AccountHelper.mLoginStatus)
		{
			return;
		}
		mComanager.messageBox(new DataResponse<MessageBoxHolder>(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						haveNewMessage = value.getData().getDataContext().size() !=0;
						changeBottomIcon();
					}
				}
			}
		},1,"new",null,1+"");
	}
	
	private void changeBottomIcon(){
		if (mainFragment == personal_manager) {
			renderTextViewWithDrawables(tv_user, userDrawables[0]);
			if(getActionBarItem(MESSAGE_ITEM_ID) != null)
			{
			  changeActionBarItemImageRes(haveNewMessage ? R.drawable.have_message_selector : R.drawable.no_message_selector, MESSAGE_ITEM_ID);
			}else{
				addActionBarItem(haveNewMessage ? R.drawable.have_message_selector : R.drawable.no_message_selector, MESSAGE_ITEM_ID);
			}
		} else {
			if (haveNewMessage) {
				renderTextViewWithDrawables(tv_user, userDrawables[1]);
			} else {
				renderTextViewWithDrawables(tv_user, userDrawables[0]);
			}
		}
	}
	    public void handleMessage(String tag){
	    	CurrentTag   =  tag;
	    	mLoadingBar.setVisibility(View.GONE);
	    	TextErrorContent.setText(getResources().getString(R.string.network_exception));
	        showNoNetWorkLayer();
	    }
	    
	    
	    private void showNoNetWorkLayer(){
	    	content.setVisibility(View.GONE);	
	    	bottom.setVisibility(View.GONE);	
	    	mLoadingBar.setVisibility(View.GONE);
	    	networkLayer.setVisibility(View.VISIBLE);	
	    }
	    
	    public void hideNoNetWorkLayer(){
	    	content.setVisibility(View.VISIBLE);	
	    	bottom.setVisibility(View.VISIBLE);	
	    	networkLayer.setVisibility(View.GONE);	
	    	mLoadingBar.setVisibility(View.GONE);
	    }

		@Override
		protected void onActionBarItemClick(View view, int itemId) {
			// TODO Auto-generated method stub
			super.onActionBarItemClick(view, itemId);
			
			if(itemId == BACK_ITEM_ID)
			{
				switchContent(main_manager,getString(R.string.main_aurora_actionbar_title));
			}else if(itemId == MESSAGE_ITEM_ID)
			{
				MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_NEWS);
				Intent intent = new Intent(this,MessageBoxActivity.class);
				startActivity(intent);
				
			}
			
		}
	
		private void restoreDefaultFocusToWidget(){
			//click person button but result is login fail
		      if(!AccountHelper.mLoginStatus){
		    		tv_user.clearFocus();
					tv_user.setFocusable(false);
					tv_user.setFocusableInTouchMode(false);
					tv_home.setFocusable(true);
					tv_home.setFocusableInTouchMode(true);
					if(!tv_home.isFocused()){
						tv_home.requestFocus();
					}
		      }
		}
		
		private class LocalLoginReceiver extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {
				int result = intent.getIntExtra(Globals.LOCAL_LOGIN_RESULT, Globals.LOCAL_LOGIN_FAIL);
				if (result == Globals.LOCAL_LOGIN_SUCCESS) {
					switchContent(personal_manager,getString(R.string.user_center_title));
					/*new TotalCount(MainActivity.this, "300", "004", 1).CountData();*/
					MobclickAgent.onEvent(MainActivity.this, Globals.PREF_TIMES_MY);
					mAccountHelper.updateFromLocal();
					requestMessageBox();
				} else if (result == Globals.LOCAL_LOGOUT_SUCCESS) {
					if (personal_manager != null) {
						((UserFragment) personal_manager).logOut();
					}
					if (tv_home != null) {
						tv_home.performClick();
					}
					if (mAccountHelper != null) {
						mAccountHelper.updateFromLocal();
					}
					startLogin();
				}
			}
			
		};
	
}
