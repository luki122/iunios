//<!-- aurora add liyang 2015 --> for dial launcher
package com.android.contacts.activities;

import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.FragmentCallbacks;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.appwidget.MyAppWidgetProvider;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.dialpad.AnimUtils;
import com.android.contacts.dialpad.AnimationListenerAdapter;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.dialpad.DialpadView;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.updateDiapadButtonListener;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharedPreferencesUtil;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AuroraTabHost;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.R.anim;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.StateListAnimator;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.appwidget.AppWidgetManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;
import aurora.widget.floatactionbutton.FloatingActionButton;
import aurora.widget.floatactionbutton.FloatingActionButton.OnFloatActionButtonClickListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.InputType;
import android.text.TextUtils;


@SuppressWarnings("deprecation")
public class Copy_2_of_AuroraDialActivityV3 extends AuroraActivity implements FragmentCallbacks,updateDiapadButtonListener{
	private Intent oldIntent=null;
	private Uri mRealUri;
	public static AuroraSearchView mSearchView;
	public static ImageButton searchViewBackButton;
	private boolean mHasMissedCall = false;
	public static AuroraActionBar actionBar;
	private static String TAG="AuroraDialActivityV3";


	private Context context = null;	

	private boolean mIsPrivate = false;
	private static final int AURORA_MORE = 1;
	private static final int AURORA_SEARCH = 2;

	private View auroratabwidget_layout,dialpad_fragment_layout;

	public FloatingActionButton fab1;	

	private SvQueryTextListener svQueryTextListener;
	public static Copy_2_of_AuroraDialActivityV3 auroraDialActivityV3;

	public static final int SEARCH_ACTION_BROADCAST=0x02;
	public static final int QUERY_ACTION_BROADCAST=0x03;
	public static final int QUERY_QUIT=0x05;
	//	public static final String QUIT_SEARCH_ACTION_BROADCAST="QUIT_SEARCH_ACTION_BROADCAST";
	public static final int SWITCH_TO_PAGE0=0x00;
	public static final int SWITCH_TO_PAGE1=0x04;
	public static final int DESTROY_ACTIVITY=0x01;
	public static final int  ON_RESUME=0x06;
	public static final int  SWITCH_TO_NORMAL_MODE=0x07;

	public static Handler callLogHandler;
	public static Handler contactsHandler;	
	private static final int FAB_SCALE_IN_DURATION = 266;
	private static final int FAB_SCALE_IN_DURATION_ZERO = 0;
	private static final int FAB_SCALE_IN_FADE_IN_DELAY = 100;
	private static final int FAB_ICON_FADE_OUT_DURATION = 66;

	public static void setCallLogHandler(Handler callLogHandler) {
		Copy_2_of_AuroraDialActivityV3.callLogHandler = callLogHandler;
	}


	public static void setContactsHandler(Handler contactsHandler) {
		Copy_2_of_AuroraDialActivityV3.contactsHandler = contactsHandler;
	}

	public void scaleInForFab(int delayMs,int duration) {
		fab1.setVisibility(View.VISIBLE);
		AnimUtils.scaleIn(fab1, duration, delayMs);
		//		AnimUtils.fadeIn(aurora_dialButton_image, FAB_SCALE_IN_DURATION,
		//				delayMs + FAB_SCALE_IN_FADE_IN_DELAY, null);
	}



	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {

			case AURORA_MORE:{
				Log.d(TAG, "fab1.visiblity3:"+fab1.getVisibility());
				Log.d("liyang","click AURORA_MORE");
				setAuroraSystemMenuCallBack(auroraMenuCallBack);
				setAuroraMenuItems(R.menu.actionbar_more_menu);
				showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,
						context.getResources().getDimensionPixelOffset(R.dimen.calllog_popupwindow_margin_right), 
						context.getResources().getDimensionPixelOffset(R.dimen.more_popupwindow_margin_top));


				//				View contentView = View.inflate(context,
				//						R.layout.popupwindowmore, null);
				//				
				//				
				//
				//				pop_newcontact = (LinearLayout) contentView
				//						.findViewById(R.id.pop_newcontact);
				//				pop_record = (LinearLayout) contentView
				//						.findViewById(R.id.pop_record);
				//				pop_setting = (LinearLayout) contentView
				//						.findViewById(R.id.pop_setting);
				//
				//
				//				pop_newcontact.setOnClickListener(AuroraDialActivityV3.this);
				//				pop_record.setOnClickListener(AuroraDialActivityV3.this);
				//				pop_setting.setOnClickListener(AuroraDialActivityV3.this);
				//
				//				LinearLayout ll_popup_container = (LinearLayout) contentView
				//						.findViewById(R.id.ll_popup_container);
				//
				//				ScaleAnimation sa = new ScaleAnimation( 0.0f,
				//						1.0f,
				//						0.0f,
				//						1.0f,
				//						Animation.RELATIVE_TO_SELF, 
				//						1.0f, 
				//						Animation.RELATIVE_TO_SELF, 
				//						0.0f);
				//				sa.setDuration(100);				
				//
				//
				//				
				//
				//				popupWindowMore = new PopupWindow(contentView, DensityUtil.dip2px(context, 130), 
				//						DensityUtil.dip2px(context, 160));
				//				popupWindowMore.setBackgroundDrawable(new ColorDrawable(
				//						Color.TRANSPARENT));
				//
				//
				//				popupWindowMore.showAtLocation(getWindow().getDecorView(), Gravity.TOP | Gravity.LEFT,
				//						screenW-DensityUtil.dip2px(context, 146), DensityUtil.dip2px(context, 26));
				//
				//				ll_popup_container.startAnimation(sa);

				break;
			}

			case AURORA_SEARCH:
				Log.d("liyang","onClick search");
				if(null==mSearchView) return;	
				//				mSearchView.setOnQueryTextListener(svQueryTextListener);新建联系人
				showTopView(false);
				CopyOfMainFragment.pager.setCurrentItem(1);
				Copy_2_of_AuroraDialActivityV3.this.showSearchviewLayout();	
				mSearchView.setQueryHint(context.getResources().getString(R.string.search_contacts));





				CopyOfMainFragment.setPagerCanScroll(false);

				//				Intent intent=new Intent(SEARCH_ACTION_BROADCAST);
				//				sendBroadcast(intent);	

				if(contactsHandler!=null){
					contactsHandler.sendEmptyMessage(SEARCH_ACTION_BROADCAST);
				}
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);



				break;
			default:
				break;
			}
		}
	};

	public void showTopView(boolean isShow){
		if(isShow){
			//			actionBar.setVisibility(View.VISIBLE);
			if(mainFragment!=null&&mainFragment.scrollIconView!=null) mainFragment.scrollIconView.setVisibility(View.VISIBLE);
			if(fab1!=null) fab1.setVisibility(View.VISIBLE);

		}else{
			//			actionBar.setVisibility(View.GONE);
			if(mainFragment!=null&&mainFragment.scrollIconView!=null) mainFragment.scrollIconView.setVisibility(View.GONE);
			if(fab1!=null) fab1.setVisibility(View.GONE);
		}
	}

	public class SvQueryTextListener implements OnQueryTextListener {

		@Override
		public boolean onQueryTextChange(String arg0) {
			// TODO Auto-generated method stub
			Log.d(TAG,"onQueryTextChange:"+arg0);
			//			Intent intent=new Intent(QUERY_ACTION_BROADCAST);
			//			intent.putExtra("queryString",arg0);
			//			sendBroadcast(intent);

			if(contactsHandler!=null){
				Message msgMessage=contactsHandler.obtainMessage();
				msgMessage.what=QUERY_ACTION_BROADCAST;
				msgMessage.obj=arg0;
				contactsHandler.sendMessage(msgMessage);
			}
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

	}


	/*private void getDisplayInfomation() {  
		Point point = new Point();  
		getWindowManager().getDefaultDisplay().getRealSize(point);  
		Log.d("liyang","the screen size is "+point.toString());  
	} 

	private void getDensity() {  
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();  
		Log.d("liyang","Density is "+displayMetrics.density+" densityDpi is "+displayMetrics.densityDpi+" height: "+displayMetrics.heightPixels+  
				" width: "+displayMetrics.widthPixels);  
	}  

	private void getScreenSizeOfDevice() {  
		DisplayMetrics dm = getResources().getDisplayMetrics();  
		int width=dm.widthPixels;  
		int height=dm.heightPixels;  
		double x = Math.pow(width,2);  
		double y = Math.pow(height,2);  
		double diagonal = Math.sqrt(x+y);  

		int dens=dm.densityDpi;  
		double screenInches = diagonal/(double)dens;  
		Log.d("liyang","The screenInches "+screenInches);  
	} 

	private void getScreenSizeOfDevice2() {  
		Point point = new Point();  
		getWindowManager().getDefaultDisplay().getRealSize(point);  
		DisplayMetrics dm = getResources().getDisplayMetrics();  
		double x = Math.pow(point.x/ dm.xdpi, 2);  
		double y = Math.pow(point.y / dm.ydpi, 2);  
		double screenInches = Math.sqrt(x + y);  
		Log.d("liyang", "dm.xdpi:"+dm.xdpi+" dm.ydpi:"+dm.ydpi+" Screen inches : " + screenInches);  
	}*/

	/**
	 * Callback from child DialpadFragment when the dialpad is shown.
	 */
	public void onDialpadShown() {
		if (mDialpadFragment.getAnimate()) {
			mDialpadFragment.getView().startAnimation(mSlideIn);
		} else {
			mDialpadFragment.setYFraction(0);
		}

		//        /// M: Support MTK-DialerSearch @[
		//        if (DialerFeatureOptions.isDialerSearchEnabled()) {
		//            updateSearchFragmentExPosition();
		//        } else {
		//        /// @}
		//            updateSearchFragmentPosition();
		//        }
	}

	private static final String TAG_DIALPAD_FRAGMENT = "dialpad";
	private static final String MAIN_FRAGMENT="mainfragment";
	//	private boolean isDialpadFragmentAdd=false;
	/**
	 * Initiates a fragment transaction to show the dialpad fragment. Animations and other visual
	 * updates are handled by a callback which is invoked after the dialpad fragment is shown.
	 * @see #onDialpadShown
	 */
	private void showDialpadFragment(boolean animate) {
		if (mIsDialpadShown || mStateSaved) {
			return;
		}

		if(mainFragment==null||mDialpadFragment==null) return;

		//		Animation mKeySectionHideAnim = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
		//		mKeySectionHideAnim.setInterpolator(new DecelerateInterpolator());
		//		mKeySectionHideAnim.setFillAfter(true);
		//		mKeySectionHideAnim.setFillEnabled(true);
		//		mKeySectionHideAnim.setAnimationListener(new AnimationListener() {
		//			@Override
		//			public void onAnimationStart(Animation animation) {
		////				actionBar.setVisibility(View.GONE);
		//				
		//				actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Normal);
		//				actionBar.setTitle(R.string.dialpad_title);
		//				actionBar.removeItem(0);
		//				actionBar.removeItem(1);
		//				actionBar.setmOnActionBarBackItemListener(new AuroraActionBar.OnAuroraActionBarBackItemClickListener() {
		//					
		//					@Override
		//					public void onAuroraActionBarBackItemClicked(int arg0) {
		//						// TODO Auto-generated method stub
		//						hideDialpadFragment(true, true);
		//						
		//					}
		//				});
		//				
		//			}
		//			@Override
		//			public void onAnimationRepeat(Animation animation) {
		//
		//			}
		//			@Override
		//			public void onAnimationEnd(Animation animation) {
		//
		//
		//			}
		//		});
		//
		//		mKeySectionHideAnim.setDuration(500);
		//		actionBar.startAnimation(mKeySectionHideAnim);      
		Log.d(TAG,"showDialpadFragment,actionBar:"+actionBar+" fab1:"+fab1+" mDialpadFragment:"+mDialpadFragment);
		if(actionBar!=null){
			actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Normal);
			actionBar.setTitle(R.string.dialpad_title);
			actionBar.removeItem(0);
			actionBar.removeItem(1);
			actionBar.setElevation(1f);
			actionBar.setmOnActionBarBackItemListener(new AuroraActionBar.OnAuroraActionBarBackItemClickListener() {

				@Override
				public void onAuroraActionBarBackItemClicked(int arg0) {
					// TODO Auto-generated method stub
					hideDialpadFragment(true, true);				
				}
			});
		}

		//		if(AuroraDialpadFragmentV2.listview_footer!=null) AuroraDialpadFragmentV2.listview_footer.setVisibility(View.GONE);
		//		if(mDialpadFragment.mListView!=null&&mDialpadFragment.resultLayout!=null){			
		//			mDialpadFragment.mListView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
		//			mDialpadFragment.resultLayout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
		//		}


		mIsDialpadShown = true;
		mDialpadFragment.setAnimate(animate);
		mainFragment.setUserVisibleHint(false);

		final FragmentTransaction ft1=getFragmentManager().beginTransaction();
		ft1.show(mDialpadFragment);
		/// M: fix CR: ALPS01608178, avoid commit JE @{
		/*
        ft.commit();
		 */
		ft1.commitAllowingStateLoss();

		if(fab1!=null){
			AnimUtils.scaleOut(fab1, 250,View.GONE);
		}

		if(mainFragment.scrollIconView!=null){
			AnimUtils.move(mainFragment.scrollIconView,//view
					500,//duration
					0,//delay
					AnimUtils.EASE_OUT_EASE_IN1,//Interpolator
					false,//isHorizontal
					0,//startPosition
					-mainFragment.scrollIconView.getHeight(),//endPosition
					null//AnimatorListenerAdapter
					);

		}
		
		if(mainFragment!=null&&mainFragment.getView()!=null){
			AnimUtils.fadeOut(mainFragment.getView(), 
					200,
					250,
					null,
					AnimUtils.EASE_IN1,
					View.INVISIBLE);
		}
		/// @}

		//        if (animate) {
		//            mFloatingActionButtonController.scaleOut();
		//        } else {
		//            mFloatingActionButtonController.setVisible(false);
		//        }
		//        mActionBarController.onDialpadUp();
		//
		//        if (!isInSearchUi()) {
		//            enterSearchUi(true /* isSmartDial */, mSearchQuery);
		//        }
	}


	private AuroraDialpadFragmentV2 mDialpadFragment;
	private CopyOfMainFragment mainFragment;

	boolean mIsDialpadShown=false;
	private Handler mHandler;
	public static boolean switch_to_contacts_page=false;
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		intent=getIntent();
		Log.d(TAG,"onCreate1:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData()
				+" savedInstanceState:"+savedInstanceState+" this:"+this+" task:"+this.getTaskId()+" flags:"+intent.getFlags());		
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		switch_to_contacts_page=intent.getBooleanExtra("switch_to_contacts_page", false);
		Log.d(TAG,"switch_to_contacts_page:"+switch_to_contacts_page);

		mFirstLaunch = true;
		context = Copy_2_of_AuroraDialActivityV3.this;
		svQueryTextListener=new SvQueryTextListener();
		auroraDialActivityV3=Copy_2_of_AuroraDialActivityV3.this;
		mHandler=new Handler();

		initActionBar();		
		initFab();		
		initFragemnt(savedInstanceState);
		initCallbacks();

		//		checkMissedCall();


		mHandler.postDelayed(new Runnable() {
			public void run() {  
				Intent intent=new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				context.sendBroadcast(intent);
			}
		}, 3000); 



	}




	private void initFab() {
		// TODO Auto-generated method stub
		fab1 = (FloatingActionButton)findViewById(R.id.float_button);	
		//		View view=new View(context);


		//		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(fab1.getWidth(),fab1.getHeight());
		//		layoutParams.setMargins(screenW-DensityUtil.dip2px(context,16)-fab1.getWidth(),
		//				screenH-DensityUtil.dip2px(context,36)-fab1.getHeight(),DensityUtil.dip2px(context,16),DensityUtil.dip2px(context,16));
		//		fab1.setLayoutParams(layoutParams);


		fab1.setOnFloatingActionButtonClickListener(new OnFloatActionButtonClickListener(){
			@Override
			public void onClick(){
				Log.d(TAG,"fab1 onclick");
				//				Intent intent=new Intent(context,AuroraDialtactsActivityV2.class);
				//				context.startActivity(intent);	
				if (!mIsDialpadShown) {
					//                    mInCallDialpadUp = false;


					showDialpadFragment(true);
					///M: WFC <To show WFC notification on status bar>@{
					//                    if (ImsManager.isWfcEnabledByUser(this)) {
					//                        mDialpadFragment.showWfcNotification();
					//                    }
					/// @}
				}
				//				auroratabwidget_layout.setVisibility(View.GONE);
				//				dialpad_fragment_layout.setVisibility(View.VISIBLE);

				/*setAuroraContentView(R.layout.aurora_dialtacts_activity_layout, AuroraActionBar.Type.Empty);
				Fragment frag = getFragmentManager().findFragmentById(R.id.dial_pad_frag);
				if (null != frag && frag instanceof AuroraDialpadFragmentV2) {
					mGnDialpadFragment = (AuroraDialpadFragmentV2) frag;
				} 

				else if (mGnDialpadFragment == null) {
					mGnDialpadFragment = new AuroraDialpadFragmentV2();
					getFragmentManager().beginTransaction().
					add(contentIdandroid.R.id.content, mGnDialpadFragment).commitAllowingStateLoss();//aurora changes zhouxiaobing 20130914
				}*/
				//				isFragmentShown=true;
				////				testFragment=new TestFragment();
				//								if(mGnDialpadFragment==null){
				//									mGnDialpadFragment = new AuroraDialpadFragmentV2();
				//								}
				////				auroratabwidget_layout.setVisibility(View.GONE);
				//				dialpad_fragment_layout.setVisibility(View.VISIBLE);
				////				actionBar.setVisibility(View.GONE);
				//				transaction=getFragmentManager().beginTransaction();
				////				transaction.remove(testFragment);
				//				transaction.replace(R.id.dialpad_fragment_layout, mGnDialpadFragment);
				//				//				transaction.addToBackStack(null);
				//				transaction.commit();

			}

		});
	}


	private void initFragemnt(Bundle savedInstanceState) {

		//		isDialpadFragmentAdd=false;
		//		mDialpadFragment=null;
		// TODO Auto-generated method stub

		if(mDialpadFragment==null){
			Log.d(TAG,"new AuroraDialpadFragmentV2");
			mDialpadFragment=new AuroraDialpadFragmentV2();
			//		final FragmentTransaction ft=getFragmentManager().beginTransaction();
			//		ft.add(R.id.dialpad_fragment_layout,mDialpadFragment, TAG_DIALPAD_FRAGMENT)
			//		.commit();
			//		
			//		final FragmentTransaction ft2 = getFragmentManager().beginTransaction();
			//		ft2.hide(mDialpadFragment);
			//		ft2.commit();
		}

		if(mainFragment==null){
			Log.d(TAG,"new MainFragment");
			mainFragment=new CopyOfMainFragment();
		}
		final FragmentTransaction transaction=getFragmentManager().beginTransaction();

		//		mIsLandscape = getResources().getConfiguration().orientation
		//				== Configuration.ORIENTATION_LANDSCAPE;

		//		if (savedInstanceState == null) {
		transaction
		.add(R.id.auroratabwidget_layout, mainFragment, MAIN_FRAGMENT)
		.add(R.id.dialpad_fragment_layout,mDialpadFragment, TAG_DIALPAD_FRAGMENT)
		.commit();
		//		} 

		if (mIsLandscape) {
			//	            mSlideIn = AnimationUtils.loadAnimation(this,
			//	                    isLayoutRtl ? R.anim.dialpad_slide_in_left : R.anim.dialpad_slide_in_right);
			//	            mSlideOut = AnimationUtils.loadAnimation(this,
			//	                    isLayoutRtl ? R.anim.dialpad_slide_out_left : R.anim.dialpad_slide_out_right);
		} else {
			mSlideIn = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_in_bottom);
			mSlideOut = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_out_bottom);

		}

		mSlideIn.setInterpolator(AnimUtils.EASE_IN);
		mSlideOut.setInterpolator(AnimUtils.EASE_OUT_EASE_IN1);
		mSlideOut.setDuration(400);

		mSlideOut.setAnimationListener(mSlideOutListener);



	}


	private void initCallbacks() {
		// TODO Auto-generated method stub
		if(mDialpadFragment!=null) mDialpadFragment.setmCallbacks(this);
		if(mainFragment!=null) {
			AuroraCallLogFragmentV2.mCallbacks=this;
			AuroraDefaultContactBrowseListFragment.mCallbacks=this;
		}

		setAuroraSystemMenuCallBack(auroraMenuCallBack);

	}

	private boolean isSlideOutAnimating=false;
	/**
	 * Listener for after slide out animation completes on dialer fragment.
	 */
	AnimationListenerAdapter mSlideOutListener = new AnimationListenerAdapter() {


		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			isSlideOutAnimating=true;
			super.onAnimationStart(animation);
		}

		@Override
		public void onAnimationEnd(Animation animation) {

			Log.d(TAG,"mSlideOut onAnimationEnd");
			if(fab1!=null){
				scaleInForFab(400,FAB_SCALE_IN_DURATION_ZERO);
			}
			commitDialpadFragmentHide();
		}
	};

	private boolean mStateSaved;
	/**
	 * Finishes hiding the dialpad fragment after any animations are completed.
	 */
	private void commitDialpadFragmentHide() {
		Log.d(TAG, "commitDialpadFragmentHide");
		if (mDialpadFragment!=null&&!mStateSaved && !mDialpadFragment.isHidden()) {
			Log.d(TAG, "commitDialpadFragmentHide1");
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.hide(mDialpadFragment);
			/// M: Fix CR ALPS01821946. @{
			/* original code:
            ft.commit();
			 */
			ft.commitAllowingStateLoss();
			
			mIsDialpadShown = false;
			isSlideOutAnimating=false;
			/// @}
		}
		//        mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
	}


	private void initActionBar() {
		setAuroraContentView(R.layout.aurora_dial_main2,AuroraActionBar.Type.Empty);
		actionBar = getAuroraActionBar();

		//		mIsPrivate = ((AuroraCallLogActivity)getActivity()).isPrivate();
		actionBar.setTitle(R.string.launcherDialer);	
		actionBar.setElevation(0f);
		setAuroraActionbarSplitLineVisibility(View.GONE);
		actionBar.addItem(AuroraActionBarItem.Type.Search, AURORA_SEARCH);		
		actionBar.addItem(AuroraActionBarItem.Type.More, AURORA_MORE);
		actionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
		//		actionBar.setVisibility(View.GONE);
		//setContentView(R.layout.aurora_dial_main2);

		mSearchView=actionBar.getAuroraActionbarSearchView();	
		mSearchView.setMaxLength(30);		     
		mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		mSearchView.setOnQueryTextListener(svQueryTextListener);
		//		AuroraDialActivityV3.this.setOnSearchViewQuitListener(this);


		searchViewBackButton=actionBar.getAuroraActionbarSearchViewBackButton();

		Log.d(TAG,"mSearchView:"+mSearchView+" searchViewBackButton:"+searchViewBackButton);

		searchViewBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				quitSearch();

			}
		});

		//		setAuroraMenuItems(R.menu.aurora_action);
	}


	private static final int SUBACTIVITY_ACCOUNT_FILTER = 4;
	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			Log.d(TAG,"itemId:"+itemId);
			switch (itemId) {

			case R.id.aurora_menu_contacts_setting: {
				Intent intent=new Intent(context, AuroraContactsSetting.class);
				startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);
				StatisticsUtil.getInstance(context.getApplicationContext()).report(StatisticsUtil.Contact_Setting);
				break;
			}

			case R.id.pop_newcontact:{
				Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
				Bundle extras = getIntent().getExtras();
				if (extras != null) {
					intent.putExtras(extras);
				}
				startActivity(intent);
				break;
			}

			case R.id.pop_record:{
				startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
				break;
			}

			case R.id.blacklist:{
				Intent intent=new Intent();
				intent.setClassName("com.aurora.reject",	"com.aurora.reject.AuroraBlackNameActivity");
				startActivity(intent);
				break;
			}

			case R.id.pop_setting:{
				//				startActivity(DialtactsActivity.getCallSettingsIntent());

				Intent intent=new Intent(context, AuroraContactsSetting.class);
				startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);

				break;
			}

			default:
				break;
			}
		}
	};

	private void quitSearch(){
		Log.d(TAG,"quitSearch");
		if(mSearchView!=null) mSearchView.clearText();
		hideSearchviewLayout();
		showTopView(true);	
		if(mainFragment!=null) mainFragment.setPagerCanScroll(true);

		Message msgMessage=contactsHandler.obtainMessage();
		msgMessage.what=QUERY_QUIT;
		if(contactsHandler!=null)	contactsHandler.sendMessage(msgMessage);

		//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		//		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);



		//		Intent intent=new Intent(QUIT_SEARCH_ACTION_BROADCAST);
		//		sendBroadcast(intent);
	}

	/**
	 * Sets the current tab based on the intent's request type
	 *
	 * @param intent Intent that contains information about which tab should be selected
	 */
	private void displayFragment(Intent intent) {
		// If we got here by hitting send and we're in call forward along to the in-call activity
		//        if (isSendKeyWhileInCall(intent)) {
		//            finish();
		//            return;
		//        }
		//		Log.d(TAG,"onCreate4:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData());		
		if (mDialpadFragment != null) {
			//			Log.d(TAG,"intent:"+intent+" action:"+intent.getAction()+" intent.getData():"+intent.getData()+" isDialIntent(intent)):"+isDialIntent(intent));
			//            final boolean phoneIsInUse = phoneIsInUse();
			if (/*phoneIsInUse || */(intent!=null&& intent.getData() !=  null && isDialIntent(intent))) {
				//                mDialpadFragment.setStartedFromNewIntent(true);
				//                if (phoneIsInUse && !mDialpadFragment.isVisible()) {
				//                    mInCallDialpadUp = true;
				//                }
				showDialpadFragment(false);
			}
		}
	}


	@Override
	public void onAttachFragment(Fragment fragment) {
		//		Log.d(TAG,"onCreate22:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData());		
		Log.d(TAG,"onAttachFragment");
		if (fragment instanceof AuroraDialpadFragmentV2) {
			mDialpadFragment = (AuroraDialpadFragmentV2) fragment;
			if (!mShowDialpadOnResume) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.hide(mDialpadFragment);
				ft.commit();
			}

			if((intent!=null&& intent.getData() !=  null && isDialIntent(intent))){
				showDialpadFragment(true);
			}

			/// M: Support MTK-DialerSearch @{
		} 

		//        else if (DialerFeatureOptions.isDialerSearchEnabled() && fragment instanceof SmartDialSearchFragmentEx) {
		//            mEnhancedSmartDialSearchFragment = (SmartDialSearchFragmentEx) fragment;
		//            mEnhancedSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
		//        } else if (DialerFeatureOptions.isDialerSearchEnabled() && fragment instanceof RegularSearchFragmentEx) {
		//            mEnhancedRegularSearchFragment = (RegularSearchFragmentEx) fragment;
		//            mEnhancedRegularSearchFragment.setOnPhoneNumberPickerActionListener(this);
		//            /// @}
		//
		//        } else if (fragment instanceof SmartDialSearchFragment) {
		//            mSmartDialSearchFragment = (SmartDialSearchFragment) fragment;
		//            mSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
		//        } else if (fragment instanceof SearchFragment) {
		//            mRegularSearchFragment = (RegularSearchFragment) fragment;
		//            mRegularSearchFragment.setOnPhoneNumberPickerActionListener(this);
		//        } else if (fragment instanceof ListsFragment) {
		//            mListsFragment = (ListsFragment) fragment;
		//            mListsFragment.addOnPageChangeListener(this);
		//        }
	}


	/**
	 * True when this activity has been launched for the first time.
	 */
	private boolean mFirstLaunch;
	private boolean mShowDialpadOnResume;
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		Log.d(TAG, "fab1.visiblity:"+fab1.getVisibility());
		mStateSaved = false;
		// TODO Auto-generated method stub
		super.onResume();

		/*if (mFirstLaunch) {
			Log.d(TAG,"onCreate3:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData());		
			displayFragment(intent);
		} 

		//		else if (!phoneIsInUse() && mInCallDialpadUp) {
		//            hideDialpadFragment(false, true);
		//            mInCallDialpadUp = false;
		//        }

		else if (mShowDialpadOnResume) {
			showDialpadFragment(false);
			mShowDialpadOnResume = false;
		}*/

		mFirstLaunch = false;


		//		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
		//			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		//		}
		//		String type = this.getIntent().getType();
		//
		//		// CallLog.Calls.CONTENT_TYPE
		//		if (type != null
		//				&& type.equalsIgnoreCase("vnd.android.cursor.dir/calls")
		//				|| mHasMissedCall) {
		//			mIsCalllog = true;
		//			mHasMissedCall = false;
		//			//			th.setCurrentTabNoAnimation(0);
		//		} else if (isDialIntent(this.getIntent())) {
		//			mIsCalllog = false;
		//			//			th.setCurrentTabNoAnimation(1);
		//		} else {
		//			mIsCalllog = false;
		//			//			th.setCurrentTabNoAnimation(mCurrentTab);
		//		}
		//
		//		if(oldIntent!=null){
		//			setIntent(oldIntent);
		//		}
		//		
		if(callLogHandler != null) {
			callLogHandler.sendEmptyMessage(ON_RESUME);
		}

		Log.d(TAG, "onresume:"+AuroraDefaultContactBrowseListFragment.mIsAuroraSearchMode);
		if(AuroraDefaultContactBrowseListFragment.mIsAuroraSearchMode){
			quitSearch();
		}


	}




	@Override
	public void onNewIntent(Intent newIntent) {
		Log.d(TAG,"onNewIntent");
		intent=newIntent;
		Log.d(TAG,"onNewIntent:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData()
				);	
		//		if ((newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
		//			return;
		//		}

		checkMissedCall();

		setReallyUri(intent);
		setIntent(intent);

		if((intent!=null&& intent.getData() !=  null && isDialIntent(intent))){
			showDialpadFragment(true);
		}else{
			//			hideDialpadFragment(false, true);
		}
	}

	private void setReallyUri(Intent intent) {
		if (intent != null) {
			mRealUri = intent.getData();
		}
	}

	public Uri getReallyUri() {
		Uri data = mRealUri;
		mRealUri = null;
		return data;
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

	//aurora change liguangyu 20131112 for BUG #677 start
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
	//aurora change liguangyu 20131112 for BUG #677 end

	//aurora add liguangyu 201311128 start
	private boolean mIsCalllog = false;


	public static void getCaller(){   
		Throwable ex = new Throwable();
		StackTraceElement[] stackElements = ex.getStackTrace();
		if (stackElements != null) {
			for (int i = 0; i < stackElements.length; i++) {
				Log.d(TAG,stackElements[i].getClassName()+"/t");
				Log.d(TAG,stackElements[i].getFileName()+"/t");
				Log.d(TAG,stackElements[i].getLineNumber()+"/t");
				Log.d(TAG,stackElements[i].getMethodName());
			}
		}
	} 

	@Override
	protected void onDestroy() {

		Log.v(TAG, "onDestroy:"+Copy_2_of_AuroraDialActivityV3.this);
		getCaller();

		//		Intent intent=new Intent(DESTROY_ACTIVITY);
		//		sendBroadcast(intent);
		//		isDialpadFragmentAdd=false;
		mDialpadFragment=null;
		mainFragment=null;

		//		if(callLogHandler!=null) callLogHandler.sendEmptyMessage(DESTROY_ACTIVITY);
		//		if(contactsHandler!=null) contactsHandler.sendEmptyMessage(DESTROY_ACTIVITY);

		//		context.sendBroadcast(new Intent(MyAppWidgetProvider.unregisterContactChangeObserver));
		super.onDestroy();
	}
	//aurora add liguangyu 201311128 end

	private Animation mSlideOut;
	private Animation mSlideIn;
	private boolean mIsLandscape;
	/**
	 * Initiates animations and other visual updates to hide the dialpad. The fragment is hidden in
	 * a callback after the hide animation ends.
	 * @see #commitDialpadFragmentHide
	 */
	public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
		Log.d(TAG,"hideDialpadFragment");
		if (mDialpadFragment == null ||mainFragment==null) {
			return;
		}

		if (!mIsDialpadShown) {
			return;
		}
		//		if(true){
		//			mDialpadFragment.scaleOut();
		//			return;
		//		}

		if(actionBar!=null){
			actionBar.setElevation(0f);
			actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Empty);
			actionBar.setTitle(R.string.launcherDialer);	
			actionBar.addItem(AuroraActionBarItem.Type.Search, AURORA_SEARCH);		
			actionBar.addItem(AuroraActionBarItem.Type.More, AURORA_MORE);		
		}

		if (clearDialpad) {
			mDialpadFragment.clearDigits();
		}
		
		if(mainFragment.scrollIconView!=null){
			AnimUtils.move(mainFragment.scrollIconView,//view
					500,//duration
					0,//delay
					AnimUtils.EASE_OUT_EASE_IN1,//Interpolator
					false,//isHorizontal
					-mainFragment.scrollIconView.getHeight(),//startPosition
					0,//endPosition
					null//AnimatorListenerAdapter
					);

		}
		
		if(mainFragment!=null&&mainFragment.getView()!=null){
			AnimUtils.fadeIn(mainFragment.getView(), 
					200,
					200,
					null,
					AnimUtils.EASE_OUT1);
		}
		
		//		actionBar.setVisibility(View.VISIBLE);
		mDialpadFragment.setAnimate(animate);
		if(mainFragment!=null) mainFragment.setUserVisibleHint(true);
		//        mListsFragment.sendScreenViewForCurrentPosition();

		//        /// M: Support MTK-DialerSearch @{
		//        if (DialerFeatureOptions.isDialerSearchEnabled()) {
		//            updateSearchFragmentExPosition();
		//        /// @}
		//        } else {
		//            updateSearchFragmentPosition();
		//        }
		//
		//        updateFloatingActionButtonControllerAlignment(animate);
		if (animate) {
			/*final AnimatorListenerAdapter showListener = new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					super.onAnimationEnd(animation);
					Log.d(TAG,"onAnimationEnd");
					AuroraDialActivityV3.fab1.setVisibility(View.VISIBLE);
				}		

			};*/

			/*			ViewPropertyAnimator animator0 = DialpadView.dialButton.animate();
			int dialButtonTranslationX=AuroraDialActivityV3.screenW/2-context.getResources().getDimensionPixelOffset(R.dimen.fab_padding_right)
					-context.getResources().getDimensionPixelOffset(R.dimen.dialbutton_width)/2;
			Log.d(TAG,"dialButtonTranslationX:"+dialButtonTranslationX);
			DialpadView.dialButton.setTranslationX(0);
			animator0.translationX(dialButtonTranslationX);
			animator0.setInterpolator(AnimUtils.EASE_OUT)
			.setStartDelay(0)
			.setDuration(500)
			.setListener(showListener)
			.start();*/

			mDialpadFragment.getView().startAnimation(mSlideOut);
			//			mDialpadFragment.main_keyboard.startAnimation(mSlideOut);
			scaleInForFab(400,FAB_SCALE_IN_DURATION);


		} else {
			commitDialpadFragmentHide();

		}

		//        mActionBarController.onDialpadDown();
		//
		//        if (isInSearchUi()) {
		//            if (TextUtils.isEmpty(mSearchQuery)) {
		//                exitSearchUi();
		//            }
		//        }
		//        ///M: WFC <To remove WFC notification from status bar>@{
		//        mDialpadFragment.stopWfcNotification();
		/// @}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.v(TAG, "AuroraDialActivityV3 dispatchKeyEvent ,getKeyCode:"+event.getKeyCode()+" action:"+event.getAction()); 

		//		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK ||event.getKeyCode()==KeyEvent.KEYCODE_MENU){
		//			if(AuroraDialActivityV3.dismissPopupWindow()) {
		//				return true;
		//			}
		//			if(AuroraCallLogFragmentV2.dismissPopupWindow()) {
		//				return true;
		//			}
		//		}

		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){

			if(mIsDialpadShown){
				//				transaction.hide(testFragment);
				//				dialpad_fragment_layout.setVisibility(View.GONE);
				//				auroratabwidget_layout.setVisibility(View.VISIBLE);
				//				actionBar.setVisibility(View.VISIBLE);
				//				isFragmentShown=false;
				Log.d(TAG,"isSlideOutAnimating:"+isSlideOutAnimating);
				if(isSlideOutAnimating) return true;
				hideDialpadFragment(true, true);
				return true;
			}

			if(mSearchView!=null&&mSearchView.isShown()){

				quitSearch();

				return true;
			}
			if(mainFragment==null){
				finish();
				return true;
			}

			if(mainFragment.currIndex==0){
				if(AuroraCallLogFragmentV2.getEditMode()){
					Log.d(TAG, "AuroraCallLogFragmentV2 is edit mode");
					if(callLogHandler!=null) callLogHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}else if(mainFragment.currIndex==1){
				if(AuroraDefaultContactBrowseListFragment.mIsEditMode){
					Log.d(TAG, "AuroraDefaultContactBrowseListFragment is edit mode");
					if(contactsHandler!=null) contactsHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}

			Log.d(TAG, "finish");
			Copy_2_of_AuroraDialActivityV3.this.finish();
			return true;
		}

		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			return true;
		}

		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
			//			Log.d(TAG,"visible:"+(getAuroraMenu()).getVisibility());
			//		   actionBar.setVisibility(visibility)
			//			showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,
			//					DensityUtil.dip2px(context, 16), DensityUtil.dip2px(context, 26));
			//			return true;	

		}

		//		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU && event.getA==KeyEvent.ACTION_UP){
		//			Log.d(TAG, "click menu:"+auroraMenuIsEnable());
		//		
		//			showAuroraMenu();
		//		}

		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}

		Log.d(TAG,"dispatchKeyEvent1");
		return super.dispatchKeyEvent(event);
	}

	//aurora add liguangyu 20140806 for BUG #7262 start
	protected void onSaveInstanceState(Bundle outState) {

	}
	//aurora add liguangyu 20140806 for BUG #7262 end

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
						missCallCursor = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}


	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {  
		// TODO Auto-generated method stub  

		if(mIsDialpadShown){
			boolean isGestureDetected = mGestureDetector.onTouchEvent(ev); 
			if(isGestureDetected) {
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}
		}

		switch(ev.getAction()){  
		case MotionEvent.ACTION_DOWN:  	
			Log.i("liyang", "AuroraDialactsActivity dispatchTouchEvent-ACTION_DOWN..."); 
			//			if(AuroraDialerSearchAdapter.dismissPopupWindow()) return true;

			mIsPressing = true;
			break;  
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mIsPressing = false;
			break; 
		default:break;  
		}  
		return super.dispatchTouchEvent(ev);  
	}  

	public boolean isPressing() {
		return mIsPressing;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onkeydown");

		//		boolean handled = mDialpadFragment.onKeyDown(keyCode, event);
		//		Log.d(TAG,"handled:"+handled);
		//		if (handled) {
		//			return true;
		//		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			Log.d(TAG, "finish");
			Copy_2_of_AuroraDialActivityV3.this.finish();
			return true;
		}

		case KeyEvent.KEYCODE_MENU:{

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
						startActivity(IntentFactory.newCreateContactIntent(mDialpadFragment.getDigitsText()));
					}
				});
				addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_menu_add_exist_contact), new OnMenuItemClickLisener() {
					public void onItemClick(View menu) {
						startActivity(IntentFactory.newInsert2ExistContactIntent(mDialpadFragment.getDigitsText()));
					}
				});
			}
			showCustomMenu();
			return true;

		}
		}

		return super.onKeyDown(keyCode, event);
	}



	private static final int FLING_MIN_VERTICAL_DISTANCE = 150;//350;
	private static final int FLING_MAX_HORIZONTAL_DISTANCE = 160;
	private static final int FLING_MAX_VERTICAL_DISTANCE = 160;
	private static final int FLING_MIN_HORIZONTAL_DISTANCE = 120;
	private GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {

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
			Log.d(TAG,"onScroll2");

			if(mDialpadFragment==null) return false;
			if(mDialpadFragment.getDigits2()==null) return false;
			if(mDialpadFragment.getDigits2().getVisibility()==View.GONE) return false;

			if(ContactsApplication.sIsAuroraYuloreSupport){

			} else {
				//Gionee:huangzy 20130319 modify for CR00786102 start
				//Gionee:huangzy 20120727 modify for CR00652430 start
				if (null != mDialpadFragment &&
						mDialpadFragment.isDialpadShowing() &&
						mDialpadFragment.getDigitsTextLen() > 0) {

					Rect rect = mDialpadFragment.getHideDialpadTouchRect();
					if (null != e1 && null != rect && rect.contains((int)e1.getX(), (int)e1.getY())) {

						mDialpadFragment.showDialpad(false, true);

						return true;
					}
				}

			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.d(TAG,"onFling2");

			if(mDialpadFragment==null) return true;

			if(mDialpadFragment.getDigits2()!=null&&mDialpadFragment.getDigits2().getVisibility()==View.GONE) return false;

			if(ContactsApplication.sIsAuroraYuloreSupport){

			} else {
				if (null != mDialpadFragment) {
					//Gionee:xuhz 20121014 add for CR00686812 start
					if (null != e1 && null != e2) {
						Rect rect = mDialpadFragment.getKeyBoardFlipperRect();
						boolean isDialpadShowing = mDialpadFragment.isDialpadShowing();
						int x1 = (int)e1.getX();
						int y1 = (int)e1.getY();
						int x2 = (int)e2.getX();
						int y2 = (int)e2.getY();
						int distanceX = x2 - x1;
						int distanceY = y2 - y1;

						if (ContactsApplication.sIsGnQwertDialpadSupport) {
							if (Math.abs(distanceY) < FLING_MAX_VERTICAL_DISTANCE &&
									rect.contains(x1, y1) && rect.contains(x2, y2)) {
								if (isDialpadShowing) {
									if (distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
										return true;
									} else if (-distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
										return true;
									}
								}
							}
						}

						rect = mDialpadFragment.getDialpadFlipperRect();
						if (Math.abs(distanceX) < FLING_MAX_HORIZONTAL_DISTANCE/* && rect.contains(x1, y1)*/) {//aurora change zhouxiaobing 20130918
							if (Math.abs(distanceY) > FLING_MIN_VERTICAL_DISTANCE && isDialpadShowing
									/*&& mGnDialpadFragment.getDigitsTextLen() > 0*/) {

								if(mDialpadFragment.getDigitsTextLen() > 0){
									mDialpadFragment.showDialpad(false, true);

									return true;
								}else{
									hideDialpadFragment(true, true);
								}
							}
						}
					}
				}
			}

			return false;
		}
	});


	@Override
	public void onFragmentCallback(int what,Object obj) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onFragmentCallback,what:"+what+" obj:"+obj);
		switch (what) {
		case FragmentCallbacks.FINISH_DIALPAD:
			hideDialpadFragment(true, true);
			break;

		case FragmentCallbacks.COPY_NUMBER_TO_DIALPAD:
			showDialpadFragment(true);
			if(mDialpadFragment!=null) mDialpadFragment.fillDigitsIfNecessary((Intent)obj);
			break;

		case FragmentCallbacks.SWITCH_TO_EDIT_MODE:
		{
			Log.d(TAG,"SWITCH_TO_EDIT_MODE");
			mainFragment.setPagerCanScroll(false);
			if(mainFragment==null||mDialpadFragment==null) return;

			if(mainFragment.scrollIconView!=null) {
				mainFragment.scrollIconView.setVisibility(View.GONE);
				//				AnimUtils.scaleOut(mainFragment.scrollIconView, 500,AnimUtilsView.INVISIBLE);
			}

			if(fab1!=null){
				AnimUtils.move(fab1,0,0,AnimUtils.CURVE_SHOW,false,0,
						context.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_action_bottom_bar_height),null);				
				AnimUtils.scaleOut(fab1, 250,View.GONE);
			}
			break;
		}

		case FragmentCallbacks.SWITCH_TO_NORMAL_MODE:
		{
			Log.d(TAG,"SWITCH_TO_NORMAL_MODE");
			if(mainFragment==null||mDialpadFragment==null) return;
			if(mainFragment.scrollIconView!=null) mainFragment.scrollIconView.setVisibility(View.VISIBLE);
			mainFragment.setPagerCanScroll(true);
			if(fab1!=null){
				AnimUtils.move(fab1,0,0,AnimUtils.CURVE_SHOW,false,
						context.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_action_bottom_bar_height),0,null);	
				mDialpadFragment.scaleIn(fab1, 400);
			}
			break;
		}

		default:
			break;
		}

	}


	@Override
	public void updateDialpadButton(boolean show) {
		// TODO Auto-generated method stub

	}


	private boolean mIsPressing = false;


}