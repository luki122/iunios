//<!-- aurora add zhouxiaobing 20130910 --> for dial launcher
package com.android.contacts.activities;
import com.android.contacts.util.ChangeStatusBar;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.calllog.AuroraCallLogFragmentV2.IAuroraCallLogFragment;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.IAuroraDialpadFragment;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.updateDiapadButtonListener;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment.IAuroraContactsFragment;
import com.android.contacts.util.ChangeStatusBar;
import com.android.contacts.util.HapticFeedback;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AuroraContactsFragmentTabHost;
import com.android.contacts.widget.AuroraContactsFragmentTabHost.TabChangeAnimation;
import com.android.contacts.widget.AuroraContactsFragmentTabHost.TabChangeDataSet;
import com.android.contacts.widget.AuroraFragmentTabHost.OnFragmentTabChangeListener;
import com.android.contacts.widget.AuroraFragmentTabHost.TabHostAdapter;
import com.android.contacts.widget.AuroraTabHost;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenu;
import android.view.ViewConfiguration;

@SuppressWarnings("deprecation")
public class AuroraDialActivityV2 extends AuroraActivity implements
		updateDiapadButtonListener {
	private Intent oldIntent = null;
	private TabWidget tw;
	private AuroraContactsFragmentTabHost th;
	private ImageView button2;
	private Uri mRealUri;

	private boolean mHasMissedCall = false;

	private AuroraCallLogFragmentV2 mCallLogFragment;
	private AuroraDialpadFragmentV2 mGnDialpadFragment;
	private AuroraDefaultContactBrowseListFragment mAllFragment;

	// gionee xuhz 20120810 add for CR00672799 start
	private HapticFeedback mHaptic = new HapticFeedback();

	// gionee xuhz 20120810 add for CR00672799 end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SystemUtils.setStatusBarBackgroundTransparent(this);
		// this.setContentView(R.layout.aurora_dial_main_v2);
		setAuroraContentView(R.layout.aurora_dial_main_v2,
				AuroraActionBar.Type.Empty);

		checkMissedCall();

		th = (AuroraContactsFragmentTabHost) this
				.findViewById(android.R.id.tabhost);
		th.setTabChangeAnimation(myTabChangeAnimation);
		th.setTabChangeDataSet(myTabChangeDataSet);
		th.setTabHostAdapter(myTabHostAdapter);


		LayoutInflater inflater = this.getLayoutInflater();
		ImageView button1 = (ImageView) inflater.inflate(
				R.layout.aurora_dial_tab_imageview, null);
		button1.setImageResource(R.drawable.aurora_tab_calllogx);
		button2 = (ImageView) inflater.inflate(
				R.layout.aurora_dial_tab_imageview, null);
		if (ContactsApplication.sIsAuroraYuloreSupport) {
			button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);
		} else {
			button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
		}
		ImageView button3 = (ImageView) inflater.inflate(
				R.layout.aurora_dial_tab_imageview, null);
		button3.setImageResource(R.drawable.aurora_tab_contactx);

		th.addTab(th.newFragmentTabSpec("tab1").setContent(null)
				.setIndicator(button1));
		th.addTab(th.newFragmentTabSpec("tab2").setContent(null)
				.setIndicator(button2));
		th.addTab(th.newFragmentTabSpec("tab3").setContent(null)
				.setIndicator(button3));
		th.setOnTabChangedListener(mTabChangedListener);

		mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
		mCurrentTab = mPrefs.getInt("currentTab", 1);
		th.firstCurrentTab(mCurrentTab);

		tw = th.getTabWidget();
		tw.setDividerDrawable(this.getResources().getDrawable(
				R.drawable.aurora_tab_sep));
		if (GNContactsUtils.isMultiSimEnabled()) {
			tw.setBackgroundResource(R.drawable.aurora_bar_bg);
		}
		initTabButton();
		int tab = th.getCurrentTab();
		switch(tab){
		case 0:
			if(mCallLogFragment.isAdded()){
			}
			break;
		case 1:
			if(mGnDialpadFragment.isAdded()){
				createDialpad();
			}
			break;
		case 2:
			if(mAllFragment.isAdded()){
			}
			break;
		}
	}
	
	private TabHostAdapter myTabHostAdapter = new TabHostAdapter(){

		@Override
		public View getView(int tab) {
			// TODO Auto-generated method stub
			FragmentManager fm = getFragmentManager();
			View view = null;
			switch(tab){
			case 0:
				view = View.inflate(AuroraDialActivityV2.this, R.layout.aurora_call_log_view, null);

				// Create the list fragment and add it as our sole content.
				Fragment frag = fm.findFragmentById(R.id.call_log_frag);
				if (null != frag && frag instanceof AuroraCallLogFragmentV2) {
					mCallLogFragment = (AuroraCallLogFragmentV2) frag;
				} else if (mCallLogFragment == null) {
					mCallLogFragment = new AuroraCallLogFragmentV2();
					getFragmentManager().beginTransaction()
							.add(R.id.call_log_frag, mCallLogFragment).commitAllowingStateLoss();
				}
				mCallLogFragment.setIAuroraCallLogFragment(myIAuroraCallLogFragment);
				break;
			case 1:
				Intent intent = getIntent();
				Intent intent2 = new Intent();
				if (isDialIntent(intent)) {
					intent2 = intent;
					setReallyUri(intent);
				}
				view = View.inflate(AuroraDialActivityV2.this, R.layout.aurora_dialtacts_activity_layout, null);
		    	frag = fm.findFragmentById(R.id.dial_pad_frag);
		        if (null != frag && frag instanceof AuroraDialpadFragmentV2) {
		            mGnDialpadFragment = (AuroraDialpadFragmentV2) frag;
		        } else if (mGnDialpadFragment == null) {
		            mGnDialpadFragment = new AuroraDialpadFragmentV2();
		            getFragmentManager().beginTransaction().
		            	add(/*contentId*/android.R.id.content, mGnDialpadFragment).commitAllowingStateLoss();//aurora changes zhouxiaobing 20130914
		        }
				mGnDialpadFragment.setIAuroraDialpadFragment(myIAuroraDialpadFragment);
				break;
			case 2:

				view = View.inflate(AuroraDialActivityV2.this, R.layout.aurora_default_contacts_browse_list_frg, null);
		        mAllFragment = (AuroraDefaultContactBrowseListFragment)fm.
		                findFragmentById(R.id.aurora_default_contact_browse_list_frg);
//				mAllFragment = new AuroraDefaultContactBrowseListFragment();
				mAllFragment.setIAuroraContactsFragment(myIAuroraContactsFragment);
				mAllFragment.setFooterViewVisibility(View.VISIBLE);
				break;
			}
			return view;
		}
		
	};

	private TabChangeDataSet myTabChangeDataSet = new TabChangeDataSet() {

		@Override
		public void changeDataBeforeSetTab(int newTab) {
			int tab = th.getCurrentTab();
			switch(tab){
			case 0:
				mCallLogFragment.onPause();
				break;
			case 1:
				mGnDialpadFragment.onPause();
				break;
			case 2:
				mAllFragment.onPause();
				break;
			}
			switch(newTab){
			case 0:
				if(mCallLogFragment != null){
		        	getAuroraActionBar().setVisibility(View.VISIBLE);
		        	if(Build.VERSION.SDK_INT == 19
		    				|| Build.VERSION.SDK_INT >= 21) {
		        		ChangeStatusBar.changeStatusBar(AuroraDialActivityV2.this, true);
		        	}
		        	
	    			mCallLogFragment.setDefaultActionBar(AuroraDialActivityV2.this, getAuroraActionBar());
	    			mCallLogFragment.onResume();
				}
				break;
			case 1:
				if(mGnDialpadFragment != null){
		        	getAuroraActionBar().setVisibility(View.GONE);
					mGnDialpadFragment.onResume();
				}
				break;
			case 2:
				if(mAllFragment != null){
		        	getAuroraActionBar().setVisibility(View.VISIBLE);

		        	if(Build.VERSION.SDK_INT == 19
		    				|| Build.VERSION.SDK_INT >= 21) {
		        		ChangeStatusBar.changeStatusBar(AuroraDialActivityV2.this, true);
		        	}
		        	
	    			mAllFragment.setDefaultActionBar(AuroraDialActivityV2.this);
	    			mAllFragment.onResume();
				}
				break;
			}
		}

		@Override
		public void changeDataAfterSetTab() {
			int tab = th.getCurrentTab();
			switch(tab){
			case 0:
				break;
			case 1:
				mGnDialpadFragment.initYellowPages();
				break;
			case 2:
				break;
			}
		}
	};

	private TabChangeAnimation myTabChangeAnimation = new TabChangeAnimation() {

		@Override
		public void animationBeforeSetTab() {
			// TODO Auto-generated method stub
			int tab = th.getCurrentTab();
			switch(tab){
			case 0:
				if(mCallLogFragment != null && mCallLogFragment.isAdded()){
					mCallLogFragment.animationBeforeSetTab();
				}
				break;
			case 1:
				if(mGnDialpadFragment != null && mGnDialpadFragment.isAdded()){
					mGnDialpadFragment.animationBeforeSetTab();
				}
				break;
			case 2:
				if(mAllFragment != null && mAllFragment.isAdded()){
					mAllFragment.animationBeforeSetTab();
				}
				break;
			}
		}

		@Override
		public void animationAfterSetTab() {
			// TODO Auto-generated method stub
			int tab = th.getCurrentTab();
			switch(tab){
			case 0:
				if(mCallLogFragment != null && mCallLogFragment.isAdded()){
					mCallLogFragment.animationAfterSetTab();
				}
				break;
			case 1:
				if(mGnDialpadFragment != null && mGnDialpadFragment.isAdded()){
					mGnDialpadFragment.animationAfterSetTab();
				}
				break;
			case 2:
				if(mAllFragment != null && mAllFragment.isAdded()){
					mAllFragment.animationAfterSetTab();
				}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	    if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE);
	    }
		String type = this.getIntent().getType();

		// CallLog.Calls.CONTENT_TYPE
		if (type != null
				&& type.equalsIgnoreCase("vnd.android.cursor.dir/calls")
				|| mHasMissedCall) {
			mIsCalllog = true;
			mHasMissedCall = false;
			th.setCurrentTabNoAnimation(0);
		} else if (isDialIntent(this.getIntent())) {
			mIsCalllog = false;
			th.setCurrentTabNoAnimation(1);
		} else {
			mIsCalllog = false;
			th.setCurrentTabNoAnimation(mCurrentTab);
		}

		if (oldIntent != null) {
			setIntent(oldIntent);
		}

		int tab = th.getCurrentTab();
		switch(tab){
		case 0:
			if(mCallLogFragment.isAdded()){
			}
			break;
		case 1:
			if(mGnDialpadFragment.isAdded()){
				resumeDialpad();
			}
			break;
		case 2:
			if(mAllFragment.isAdded()){
			}
			break;
		}

	}

	private void initTabButton() {
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (th.getCurrentTab() == 1) {
					if (ContactsApplication.sIsAuroraYuloreSupport) {
						if (!mGnDialpadFragment.isYellowPagesShowing()) {
							mGnDialpadFragment.showYellowPages( true);
							if (button2 != null)
							    button2.setImageResource(R.drawable.aurora_tab_dial_upx);
						} else {
							mGnDialpadFragment.hideYellowPages( true);
							if (button2 != null)
							    button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);
						}
					} else {
						if (!mGnDialpadFragment.isDialpadShowing()) {
							mGnDialpadFragment.showDialpad(true, true);
							if (button2 != null)
								button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
						} else {
							mGnDialpadFragment.showDialpad(false, true);
							if (button2 != null)
								button2.setImageResource(R.drawable.aurora_tab_dial_upx);
						}
					}
				} else {
					th.setCurrentTab(1);
				}
			}
		});

	}
    
    private void fixIntent(Intent intent) {
        // This should be cleaned up: the call key used to send an Intent
        // that just said to go to the recent calls list.  It now sends this
        // abstract action, but this class hasn't been rewritten to deal with it.
        if (Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
            intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
            intent.putExtra("call_key", true);
            setIntent(intent);
        }
    }
	
	private void createDialpad(){
        
        // gionee xuhz 20120810 add for CR00672799 start
        try {
            mHaptic.init(this,
                         getResources().getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e("AuroraDialtactsActivityV2", "Vibrate control bool missing.", nfe);
        }
	}

	private void resumeDialpad() {

		mHaptic.checkSystemSetting();

		// aurora change zhouxiaobing 20130918 start
		if (button2 != null) {
			if (ContactsApplication.sIsAuroraYuloreSupport) {
				if (!mGnDialpadFragment.isYellowPagesShowing()) {
				    button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);
				} else {
					button2.setImageResource(R.drawable.aurora_tab_dial_upx);
				}
			} else {
				if (!mGnDialpadFragment.isDialpadShowing()) {
					button2.setImageResource(R.drawable.aurora_tab_dial_upx);
				} else {
					button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
				}
			}
		}
		// aurora change zhouxiaobing 20130918 end
	}

	@Override
	public void onNewIntent(Intent newIntent) {
		oldIntent = getIntent();// bug10983
		if ((newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			return;
		}

		checkMissedCall();

		setReallyUri(newIntent);
		setIntent(newIntent);
		int tab = th.getCurrentTab();
		switch(tab){
		case 0:
			if(mCallLogFragment.isAdded()){
			}
			break;
		case 1:
			if(mGnDialpadFragment.isAdded()){
				fixIntent(newIntent);
		        if (mGnDialpadFragment != null) {
		            mGnDialpadFragment.configureScreenFromIntent(newIntent);
		        }
			}
			break;
		case 2:
			if(mAllFragment.isAdded()){
			}
			break;
		}
	}

	private void setReallyUri(Intent intent) {
		if (intent != null) {
			mRealUri = intent.getData();
		}
	}
	
    private IAuroraDialpadFragment myIAuroraDialpadFragment = new IAuroraDialpadFragment() {
		
		@Override
		public Uri getReallyUri() {
			Uri data = mRealUri;
			mRealUri = null;
			return data;
		}
	};

	private IAuroraContactsFragment myIAuroraContactsFragment = new IAuroraContactsFragment() {

		@Override
		public void setTabWidget(boolean flag) {
			if (flag) {
				setTabWidgetVisible(View.VISIBLE);
			} else {
				setTabWidgetVisible(View.INVISIBLE);
			}
			mAllFragment.setFooterViewVisibility(flag ? View.VISIBLE
					: View.GONE);
		}
	};

	private IAuroraCallLogFragment myIAuroraCallLogFragment = new IAuroraCallLogFragment() {

		@Override
		public void setTabWidget(final int visible) {
			setTabWidgetVisible(visible);
		}
	};

	private void setTabWidgetVisible(final int visible) {
		LayoutTransition transitioner = new LayoutTransition();
		TranslateAnimation animation = null;

		if (visible == View.VISIBLE) {
			animation = new TranslateAnimation(0, 0, tw.getHeight(), 0);
		} else {
			animation = new TranslateAnimation(0, 0, 0, tw.getHeight());
		}

		animation.setDuration(transitioner
				.getDuration(LayoutTransition.DISAPPEARING));
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				tw.setVisibility(visible);
			}
		});

		animation.setInterpolator(new LinearInterpolator());
		tw.startAnimation(animation);
	}

	public ImageView getTabsicon() {
		return button2;
	}

	private boolean isDialIntent(Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_DIAL.equals(action)) {
			return true;
		}
		if (Intent.ACTION_VIEW.equals(action)) {
			final Uri data = intent.getData();
			if (data != null && "tel".equals(data.getScheme())) {
				return true;
			}
		}

		return false;
	}

	// aurora change liguangyu 20131112 for BUG #677 start
	private boolean isCalllogIntent(Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			final String data = intent.getType();
			if (data != null && data.equals("vnd.android.cursor.dir/calls")) {
				return true;
			}
		}
		return false;
	}

	// aurora change liguangyu 20131112 for BUG #677 end

	// aurora add liguangyu 201311128 start
	private SharedPreferences mPrefs;
	private boolean mIsCalllog = false;
	private int mCurrentTab;

	@Override
	protected void onDestroy() {
		Log.v("AuroraDialActivity", "onDestroy");
		if (!mIsCalllog) {
			Log.v("AuroraDialActivity",
					"onDestroy tabIndex= " + th.getCurrentTab());
			Editor editor = mPrefs.edit();
			editor.putInt("currentTab", th.getCurrentTab());
			editor.commit();
		}
		super.onDestroy();
	}

	// aurora add liguangyu 201311128 end

	private boolean mFirst = true;
	OnFragmentTabChangeListener mTabChangedListener = new OnFragmentTabChangeListener() {
		public void onTabChanged(String tabId) {
			if (mFirst) {
				mFirst = false;
				return;
			}
			int index = Integer.valueOf(tabId.substring(3)) - 1;
			Log.v("AuroraDialActivity", "onTabSelectionChanged tabIndex= "
					+ index);
			if (!mIsCalllog || index != 0) {
				mCurrentTab = index;
			}
			if (index != 0) {
				mIsCalllog = false;
			}

			// aurora add liguangyu 20140925 for #8664 start
			if (!mIsCalllog) {
				Editor editor = mPrefs.edit();
				editor.putInt("currentTab", mCurrentTab);
				editor.commit();
			}
			// aurora add liguangyu 20140925 for #8664 end
		}
	};

	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.v("SHIJIAN", "AuroraDialActivity dispatchKeyEvent  ");
	    if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE);
	    }
		return super.dispatchKeyEvent(event);
	}

	// 让父容器TabActivity在不调用saveInstanceState的情况下onDestroy, 为空是一样的效果
	// aurora add liguangyu 20140806 for BUG #7262 start
	protected void onSaveInstanceState(Bundle outState) {

	}

	// aurora add liguangyu 20140806 for BUG #7262 end

	private void checkMissedCall() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				final String[] CALL_LOG_PROJECTION = new String[] { Calls._ID };
				StringBuilder where = new StringBuilder("type=");
				where.append(Calls.MISSED_TYPE);
				where.append(" AND new=1");
				where.append(" AND (privacy_id = "
						+ AuroraPrivacyUtils.getCurrentAccountId()
						+ " or privacy_id = 0)");
				try {
					Cursor missCallCursor = getContentResolver().query(
							Calls.CONTENT_URI, CALL_LOG_PROJECTION,
							where.toString(), null, Calls.DEFAULT_SORT_ORDER);
					if (missCallCursor != null) {
						if (missCallCursor.getCount() > 0) {
							mHasMissedCall = true;
						}
						missCallCursor.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	// aurora add liguangyu 20131113 for #864 start
	public void updateDialpadButton(boolean show) {
		if (button2 != null) {
    		if(ContactsApplication.sIsAuroraYuloreSupport){
        		if (show) {
        			button2.setImageResource(R.drawable.aurora_tab_dial_upx);				
        		} else {
             		 button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);		
        		}
    		} else {
        		if (show) {
        			button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
        		} else {
        			button2.setImageResource(R.drawable.aurora_tab_dial_upx);						
        		}
    		}
    	}
	}

	// aurora add liguangyu 20131113 for #864 end

	private boolean mIsPressing = false;

	public boolean isPressing() {
		return mIsPressing;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int tab = th.getCurrentTab();
		switch(tab){
		case 1:
			boolean isGestureDetected = mGestureDetector.onTouchEvent(ev);
			if (isGestureDetected) {
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}

			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mIsPressing = true;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsPressing = false;
				break;

			default:
				break;
			}

			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	// aurora modify liguangyu 20140106 for bug #1746 start
	private static final int FLING_MIN_VERTICAL_DISTANCE = 150;// 350;
	// aurora modify liguangyu 20140106 for bug #1746 end
	private static final int FLING_MAX_HORIZONTAL_DISTANCE = 160;

	// Gionee:huangzy 20121023 add for CR00686812 start
	private static final int FLING_MAX_VERTICAL_DISTANCE = 160;
	private static final int FLING_MIN_HORIZONTAL_DISTANCE = 120;
	// Gionee:huangzy 20121023 add for CR00686812 end
	private GestureDetector mGestureDetector = new GestureDetector(
			new GestureDetector.OnGestureListener() {

				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}

				@Override
				public void onShowPress(MotionEvent e) {
				}

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return false;
				}

				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					if (ContactsApplication.sIsAuroraYuloreSupport) {

					} else {
						// Gionee:huangzy 20130319 modify for CR00786102 start
						// Gionee:huangzy 20120727 modify for CR00652430 start
						if (null != mGnDialpadFragment
								&& mGnDialpadFragment.isDialpadShowing()
								&& mGnDialpadFragment.getDigitsTextLen() > 0) {

							Rect rect = mGnDialpadFragment
									.getHideDialpadTouchRect();
							if (null != e1
									&& null != rect
									&& rect.contains((int) e1.getX(),
											(int) e1.getY())) {
								mGnDialpadFragment.showDialpad(false, true);
								// aurora change zhouxiaobing 20130918
								if (button2 != null)
									button2.setImageResource(R.drawable.aurora_tab_dial_upx);
								return true;
							}
						}
						// Gionee:huangzy 20120727 modify for CR00652430 end
						// Gionee:huangzy 20130319 modify for CR00786102 end
					}
					return false;
				}

				@Override
				public void onLongPress(MotionEvent e) {
				}

				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					if (ContactsApplication.sIsAuroraYuloreSupport) {

					} else {
						if (null != mGnDialpadFragment) {
							// Gionee:xuhz 20121014 add for CR00686812 start
							if (null != e1 && null != e2) {
								Rect rect = mGnDialpadFragment
										.getKeyBoardFlipperRect();
								boolean isDialpadShowing = mGnDialpadFragment
										.isDialpadShowing();
								int x1 = (int) e1.getX();
								int y1 = (int) e1.getY();
								int x2 = (int) e2.getX();
								int y2 = (int) e2.getY();
								int distanceX = x2 - x1;
								int distanceY = y2 - y1;

								if (ContactsApplication.sIsGnQwertDialpadSupport) {
									if (Math.abs(distanceY) < FLING_MAX_VERTICAL_DISTANCE
											&& rect.contains(x1, y1)
											&& rect.contains(x2, y2)) {
										if (isDialpadShowing) {
											if (distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
												return true;
											} else if (-distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
												return true;
											}
										}
									}
								}

								rect = mGnDialpadFragment
										.getDialpadFlipperRect();
								if (Math.abs(distanceX) < FLING_MAX_HORIZONTAL_DISTANCE/*
																						 * &&
																						 * rect
																						 * .
																						 * contains
																						 * (
																						 * x1
																						 * ,
																						 * y1
																						 * )
																						 */) {// aurora
																								// change
																								// zhouxiaobing
																								// 20130918
									if (Math.abs(distanceY) > FLING_MIN_VERTICAL_DISTANCE
											&& isDialpadShowing
											&& mGnDialpadFragment
													.getDigitsTextLen() > 0) {
										mGnDialpadFragment.showDialpad(false,
												true);
										if (button2 != null)
											button2.setImageResource(R.drawable.aurora_tab_dial_upx);// aurora
																										// change
																										// zhouxiaobing
																										// 20130918
										return true;
									}
								}
							}
							// Gionee:xuhz 20121014 add for CR00686812 end
						}
					}

					return false;
				}
			});
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		int tab = th.getCurrentTab();
		switch(tab){
		case 0:
	        switch (keyCode) {
	        case KeyEvent.KEYCODE_BACK: {
	        	//aurora modify liguangyu 20140423 for BUG #4491 start
	            if (getAuroraActionBar() != null && 
	                    (getAuroraActionBar().auroraIsExitEditModeAnimRunning() || getAuroraActionBar().auroraIsEntryEditModeAnimRunning() 
	                    		|| (mCallLogFragment.getAdapter() != null && mCallLogFragment.getAdapter().isIs_listitem_changing()))) {
	            //aurora modify liguangyu 20140423 for BUG #4491 end
	                return true;
	            }
	            
	            //aurora add liguangyu 20131108 for BUG #508 start
	            try {
	                boolean deleteIsShow = mCallLogFragment.getAuroraListView().auroraIsRubbishOut();
	                if (deleteIsShow) {
	                    mCallLogFragment.getAuroraListView().auroraSetRubbishBack();
	                    return true;
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            //aurora add liguangyu 20131108 for BUG #508 end
	            
	            if (mCallLogFragment.getEditMode() && null != getAuroraActionBar()) {
	                mCallLogFragment.switch2NormalMode();
	                mCallLogFragment.updateMenuItemState(false);
	                mCallLogFragment.setBottomMenuEnable(false);
	                getAuroraActionBar().setShowBottomBarMenu(false);
	                getAuroraActionBar().showActionBarDashBoard();
	                
	                return true;
	            }
	            
	            break;
	        }
	        
	        default: {
	            
	        }
	        
	        }
//	        moveTaskToBack(true);
			break;
		case 1:
	        boolean handled = mGnDialpadFragment.onKeyDown(keyCode, event);
	        if (handled) {
	            return true;
	        }
	        
	        //aurora change liguangyu 201311128 start
	        if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	  	if (event.getSource() != 99) {
	                addMenu(AuroraMenu.FIRST, getString(R.string.call_settings), new OnMenuItemClickLisener() {
	                    public void onItemClick(View menu) {
	                    	startActivity(DialtactsActivity.getCallSettingsIntent());
	                    }
	                });
	                
	                addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_call_record), new OnMenuItemClickLisener() {
	                    public void onItemClick(View menu) {
	                        startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
	                    }
	                });
	                
	                removeMenuById(AuroraMenu.FIRST + 2);
	        	} else {	
	                addMenu(AuroraMenu.FIRST, getString(R.string.menu_newContact), new OnMenuItemClickLisener() {
	                    public void onItemClick(View menu) {
	                    	startActivity(IntentFactory.newCreateContactIntent(mGnDialpadFragment.getDigitsText()));
	                    }
	                });
	                addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_menu_add_exist_contact), new OnMenuItemClickLisener() {
	                    public void onItemClick(View menu) {
	                    	startActivity(IntentFactory.newInsert2ExistContactIntent(mGnDialpadFragment.getDigitsText()));
	                    }
	                });
	        	}
	        	showCustomMenu();
	    	  	return true;
	        }
	        //aurora change liguangyu 201311128 end 
			break;
		case 2:
	        switch (keyCode) {
	        case KeyEvent.KEYCODE_MENU: {
	            if (mAllFragment.mSearchViewHasFocus) {
	                return false;
	            }
	            
	            if (mAllFragment.getRemoveMemberMode()) {
	                return false;
	            }

	            break;
	        }

	        case KeyEvent.KEYCODE_BACK: {
	            try {
	                boolean deleteIsShow = mAllFragment.getListView().auroraIsRubbishOut();
	                if (deleteIsShow) {
	                    mAllFragment.getListView().auroraSetRubbishBack();
	                    return true;
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            
	            if (getAuroraActionBar() != null && 
	                    (getAuroraActionBar().auroraIsExitEditModeAnimRunning() || getAuroraActionBar().auroraIsEntryEditModeAnimRunning())) {
	                return true;
	            }
	            
	            if (mAllFragment.getRemoveMemberMode()) {
	                try {
	                    Thread.sleep(300);
	                    
	                    if (isSearchviewLayoutShow()) {
	                        hideSearchviewLayout();
	                        
	                        getAuroraActionBar().setShowBottomBarMenu(true);
//	                        getAuroraActionBar().showActionBarDashBoard();
	                        getAuroraActionBar().showActionBottomeBarMenu();
	                    } else {
	                        getAuroraActionBar().setShowBottomBarMenu(false);
	                        getAuroraActionBar().showActionBarDashBoard();
	                        mAllFragment.changeToNormalMode(true);
	                    }
	                    
	                    mAllFragment.getListView().auroraSetNeedSlideDelete(true);
	                    return true;
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	            
	            break;
	        }

	        case KeyEvent.KEYCODE_DEL: {
	            if (deleteSelection()) {
	                return true;
	            }
	            break;
	        }
	        
	        default: {
	            // Bring up the search UI if the user starts typing
	            final int unicodeChar = event.getUnicodeChar();
	            if (unicodeChar != 0 && !Character.isWhitespace(unicodeChar)) {
	                String query = new String(new int[] { unicodeChar }, 0, 1);
	            }
	        }
	        }
			break;
		}
        return super.onKeyDown(keyCode, event);
	}

    private boolean deleteSelection() {
        // TODO move to the fragment
        return false;
    }
}
