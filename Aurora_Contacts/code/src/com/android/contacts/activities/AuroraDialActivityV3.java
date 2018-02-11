//<!-- aurora add liyang 2015 --> for dial launcher
package com.android.contacts.activities;
import aurora.app.AuroraProgressDialog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.FragmentCallbacks;
import com.android.contacts.R;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.dialpad.AnimUtils;
import com.android.contacts.dialpad.AnimUtils.AnimationCallback;
import com.android.contacts.dialpad.AnimationListenerAdapter;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.updateDiapadButtonListener;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.vcard.VCardService;
import com.android.contacts.widget.AuroraTabHost;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.privacymanage.service.AuroraPrivacyUtils;
import android.provider.CallLog.Calls;
import android.R.anim;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.appwidget.AppWidgetManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener;
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
import android.text.InputType;
import android.text.TextUtils;
import aurora.widget.AuroraActionBar.OnChangeAuroraActionbarTypeListener;

@SuppressWarnings("deprecation")
public class AuroraDialActivityV3 extends AuroraActivity implements FragmentCallbacks,updateDiapadButtonListener{
	private Intent oldIntent=null;
	private Uri mRealUri;
	public static AuroraSearchView mSearchView;
	public static ImageButton searchViewBackButton;
	private boolean mHasMissedCall = false;
	public static AuroraActionBar actionBar;
	private static String TAG="liyang-AuroraDialActivityV3";
	private OnChangeAuroraActionbarTypeListener typeChangeListener=new OnChangeAuroraActionbarTypeListener(){
		public void changed(){
			Log.d(TAG,"mIsDialpadShown:"+mIsDialpadShown);
			if(!actionBarChangeFlag){
				if(actionBar!=null){
					Log.d(TAG,"actionBarChangeFlag1:"+actionBarChangeFlag);
					actionBar.setTitle(R.string.dialpad_title);
					actionBar.setElevation(1f);

					//					if(actionBar.getHomeButton()!=null){
					//						actionBar.getHomeButton().setOnTouchListener(new OnTouchListener() {
					//
					//							@Override
					//							public boolean onTouch(View arg0, MotionEvent arg1) {
					//								Log.d(TAG, "actionBar.getHomeButton() ontouch,action:"+arg1.getAction());
					//								// TODO Auto-generated method stub
					//								//				if(arg1.getAction()==KeyEvent.ACTION_DOWN)
					//								return false;
					//							}
					//						});

					//					}
				}
			}else{
				if(actionBar!=null){
					Log.d(TAG,"actionBarChangeFlag2:"+actionBarChangeFlag);
					actionBar.setElevation(0f);
					actionBar.setTitle(R.string.launcherDialer);	
					actionBar.addItem(AuroraActionBarItem.Type.Search, AURORA_SEARCH);		
					actionBar.addItem(AuroraActionBarItem.Type.More, AURORA_MORE);		
				}
			}
		}
	};

	private Context context = null;	

	private boolean mIsPrivate = false;
	private static final int AURORA_MORE = 1;
	private static final int AURORA_SEARCH = 2;

	private View auroratabwidget_layout,dialpad_fragment_layout;

	public FloatingActionButton fab1;	

	private SvQueryTextListener svQueryTextListener;
	public static AuroraDialActivity auroraDialActivity;

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
		AuroraDialActivityV3.callLogHandler = callLogHandler;
	}


	public static void setContactsHandler(Handler contactsHandler) {
		AuroraDialActivityV3.contactsHandler = contactsHandler;
	}

	public void scaleInForFab(int delayMs,int duration) {
		fab1.setVisibility(View.VISIBLE);
		AnimUtils.scaleIn(fab1, duration, delayMs);
		//		AnimUtils.fadeIn(aurora_dialButton_image, FAB_SCALE_IN_DURATION,
		//				delayMs + FAB_SCALE_IN_FADE_IN_DELAY, null);
	}



	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			if(!mIsDialpadFragmentAdded){
				Log.d(TAG, "mIsDialpadFragmentAdded:"+mIsDialpadFragmentAdded);
				return;
			}
			switch (itemId) {

			case AURORA_MORE:{
				//				Log.d(TAG, "fab1.visiblity3:"+fab1.getVisibility());
				Log.d(TAG,"click AURORA_MORE");
				setAuroraSystemMenuCallBack(auroraMenuCallBack);
				setAuroraMenuItems(R.menu.actionbar_more_menu);
				showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,0,0
						/*context.getResources().getDimensionPixelOffset(R.dimen.calllog_popupwindow_margin_right),
						context.getResources().getDimensionPixelOffset(R.dimen.more_popupwindow_margin_top) */);


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
				ContactsApplication.sendSimContactBroad();
				initSearchView();
				Log.d(TAG,"onClick searchbutton");
				if(null==mSearchView) return;	
				showTopView(false);
				CopyOfMainFragment.pager.setCurrentItem(1);
				AuroraDialActivityV3.this.showSearchviewLayout();	

				CopyOfMainFragment.setPagerCanScroll(false);

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
		Log.d(TAG,"showTopView:"+isShow);
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
		Log.d(TAG,"the screen size is "+point.toString());  
	} 

	private void getDensity() {  
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();  
		Log.d(TAG,"Density is "+displayMetrics.density+" densityDpi is "+displayMetrics.densityDpi+" height: "+displayMetrics.heightPixels+  
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
		Log.d(TAG,"The screenInches "+screenInches);  
	} 

	private void getScreenSizeOfDevice2() {  
		Point point = new Point();  
		getWindowManager().getDefaultDisplay().getRealSize(point);  
		DisplayMetrics dm = getResources().getDisplayMetrics();  
		double x = Math.pow(point.x/ dm.xdpi, 2);  
		double y = Math.pow(point.y / dm.ydpi, 2);  
		double screenInches = Math.sqrt(x + y);  
		Log.d(TAG, "dm.xdpi:"+dm.xdpi+" dm.ydpi:"+dm.ydpi+" Screen inches : " + screenInches);  
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
	}

	private static final String TAG_DIALPAD_FRAGMENT = "dialpad";
	private static final String MAIN_FRAGMENT="mainfragment";

	/**
	 * Initiates a fragment transaction to show the dialpad fragment. Animations and other visual
	 * updates are handled by a callback which is invoked after the dialpad fragment is shown.
	 * @see #onDialpadShown
	 */
	private void showDialpadFragment(boolean animate) {
		if (mIsDialpadShown || mStateSaved) {
			Log.d(TAG, "showDialpadFragment1");
			return;
		}

		if(isFastClick(500)){
			Log.d(TAG,"isFastClick");
			return;
		}
		if(mainFragment==null||mDialpadFragment==null) {
			Log.d(TAG, "showDialpadFragment2");
			return;
		}

		ContactsApplication.sendSimContactBroad();

		Log.d(TAG,"showDialpadFragment,actionBar:"+actionBar+" fab1:"+fab1+" mDialpadFragment:"+mDialpadFragment);
		if(actionBar!=null){
			actionBarChangeFlag=false;
			actionBar.changeAuroraActionbarTypeWitchAlphaAnim(AuroraActionBar.Type.Normal,typeChangeListener);
		}

		if(fab1!=null){
			AnimUtils.scaleOut(fab1, 250,View.GONE);
		}

		//		if(AuroraDialpadFragmentV2.listview_footer!=null) AuroraDialpadFragmentV2.listview_footer.setVisibility(View.GONE);
		//		if(mDialpadFragment.mListView!=null&&mDialpadFragment.resultLayout!=null){			
		//			mDialpadFragment.mListView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
		//			mDialpadFragment.resultLayout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
		//		}



		mDialpadFragment.setAnimate(animate);
		//		mainFragment.setUserVisibleHint(false);

		final FragmentTransaction ft1=getFragmentManager().beginTransaction();
		ft1.show(mDialpadFragment);
		ft1.commitAllowingStateLoss();



		if(mainFragment!=null&&mainFragment.getView()!=null){
			AnimUtils.fadeOut(mainFragment.getView()/*mainFragment.currIndex==0?mainFragment.callLogView:mainFragment.peopleView*/, 
					200,
					0,
					new AnimationCallback(){

				@Override
				public void onAnimationEnd() {
					// TODO Auto-generated method stub
					super.onAnimationEnd();
					Log.d(TAG, "onAnimationEnd");
					//					if(mainFragment.scrollIconView!=null){
					//						AnimUtils.move(mainFragment.scrollIconView,//view
					//					500,//duration
					//					0,//delay
					//					AnimUtils.EASE_OUT_EASE_IN1,//Interpolator
					//					false,//isHorizontal
					//					0,//startPosition
					//					-mainFragment.scrollIconView.getHeight(),//endPosition
					//					null//AnimatorListenerAdapter
					//					);
					//						mainFragment.scrollIconView.setVisibility(View.GONE);
					//					AnimUtils.fadeOut(mainFragment.scrollIconView, 
					//					200,
					//					250,
					//					null,
					//					AnimUtils.EASE_IN1,
					//					View.INVISIBLE);
					//					}
				}

				@Override
				public void onAnimationCancel() {
					// TODO Auto-generated method stub
					super.onAnimationCancel();
				}


			},
			AnimUtils.EASE_IN1,
			View.GONE);
		}

		mIsDialpadShown = true;
	}


	private AuroraDialpadFragmentV2 mDialpadFragment;
	private CopyOfMainFragment mainFragment;

	boolean mIsDialpadShown=false;
	private Handler mHandler;
	public static boolean switch_to_contacts_page=false;
	private Intent intent;
	protected BroadcastReceiver mReceiver = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		ContactsApplication.sendSimContactBroad();
		mSearchView=null;
		searchViewBackButton=null;
		actionBar=null;
		auroraDialActivity=null;
		callLogHandler=null;
		contactsHandler=null;
		AuroraDefaultContactBrowseListFragment.mIsAuroraSearchMode=false;
		AuroraDefaultContactBrowseListFragment.mIsEditMode=false;
		AuroraDefaultContactBrowseListFragment.contactsCount=0;

		intent=getIntent();
		if(intent!=null){
			Log.d(TAG,"onCreate1:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData()
					+" savedInstanceState:"+savedInstanceState+" this:"+this+" taskid:"+this.getTaskId()+" flags:"+intent.getFlags());	
		}
		long time=System.currentTimeMillis();		

		super.onCreate(savedInstanceState);

		//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);


		mFirstLaunch = true;
		context = AuroraDialActivityV3.this;
		auroraDialActivity=(AuroraDialActivity) AuroraDialActivityV3.this;


		initActionBar();		
		initFragemnt(savedInstanceState);


		//		checkMissedCall();

		mHandler=new Handler();
		mHandler.postDelayed(new Runnable() {
			public void run() {
				context.sendBroadcast(new Intent("android.appwidget.action.APPWIDGET_UPDATE1"));
			}
		},1500);

		Log.d(TAG, "spend time:"+(System.currentTimeMillis()-time));

	}




	private void initFab() {
		// TODO Auto-generated method stub
		fab1 = (FloatingActionButton)findViewById(R.id.float_button);	

		fab1.setOnFloatingActionButtonClickListener(new OnFloatActionButtonClickListener(){
			@Override
			public void onClick(){
				Log.d(TAG,"fab1 onclick,mIsDialpadShown:"+mIsDialpadShown);

				if (!mIsDialpadShown) {
					showDialpadFragment(true);
				}
			}
		});	
	}


	private void initFragemnt(Bundle savedInstanceState) {
		if(mainFragment==null){
			Log.d(TAG,"new MainFragment");
			mainFragment=new CopyOfMainFragment();
		}

		final FragmentTransaction transaction=getFragmentManager().beginTransaction();

		//		mIsLandscape = getResources().getConfiguration().orientation
		//				== Configuration.ORIENTATION_LANDSCAPE;

		transaction
		.add(R.id.auroratabwidget_layout, mainFragment, MAIN_FRAGMENT)
		//		.add(R.id.dialpad_fragment_layout,mDialpadFragment, TAG_DIALPAD_FRAGMENT)
		.commit();

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

		if (mReceiver == null) {
			mReceiver = new ImportContactsReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("com.android.action.LAUNCH_CONTACTS_LIST");
			this.registerReceiver(mReceiver, intentFilter);
		}

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
			if(fab1!=null){
				scaleInForFab(0,FAB_SCALE_IN_DURATION);
			}

			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.hide(mDialpadFragment);
			/// M: Fix CR ALPS01821946. @{
			/* original code:
            ft.commit();
			 */
			ft.commitAllowingStateLoss();

			//			mIsDialpadShown = false;
			isSlideOutAnimating=false;
			/// @}
		}
		//        mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
	}


	private void initActionBar() {
		setAuroraContentView(R.layout.aurora_dial_main2,AuroraActionBar.Type.Empty);

		switch_to_contacts_page=intent.getBooleanExtra("switch_to_contacts_page", false);
		Log.d(TAG,"switch_to_contacts_page:"+switch_to_contacts_page);

		actionBar = getAuroraActionBar();
		//		mIsPrivate = ((AuroraCallLogActivity)getActivity()).isPrivate();
		actionBar.setTitle(R.string.launcherDialer);	
		actionBar.setElevation(0f);
		setAuroraActionbarSplitLineVisibility(View.GONE);
		actionBar.addItem(AuroraActionBarItem.Type.Search, AURORA_SEARCH);		
		actionBar.addItem(AuroraActionBarItem.Type.More, AURORA_MORE);
		actionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);

		actionBar.setmOnActionBarBackItemListener(new AuroraActionBar.OnAuroraActionBarBackItemClickListener() {

			@Override
			public void onAuroraActionBarBackItemClicked(int arg0) {
				Log.d(TAG, "onAuroraActionBarBackItemClicked,mIsDialpadShown:"+mIsDialpadShown);
				// TODO Auto-generated method stub
				if(mIsDialpadShown){
					hideDialpadFragment(true, true);	
				}
			}
		});	

		//		setAuroraMenuItems(R.menu.aurora_action);
	}

	public void initSearchView() {		
		if(mSearchView==null){
			svQueryTextListener=new SvQueryTextListener();
			mSearchView=actionBar.getAuroraActionbarSearchView();	
			mSearchView.setMaxLength(30);		     
			mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mSearchView.setOnQueryTextListener(svQueryTextListener);
			mSearchView.setQueryHint(context.getResources().getString(R.string.search_contacts));
			searchViewBackButton=actionBar.getAuroraActionbarSearchViewBackButton();

			Log.d(TAG,"mSearchView:"+mSearchView+" searchViewBackButton:"+searchViewBackButton);

			searchViewBackButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG,"onclick searchViewBackButton");
					// TODO Auto-generated method stub
					quitSearch();
				}
			});
		}

		Log.d(TAG, "initSearchView,searchViewBackButton"+searchViewBackButton);
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
				//				StatisticsUtil.getInstance(context.getApplicationContext()).report(StatisticsUtil.Contact_Setting);
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
		try{
			if (fragment instanceof AuroraDialpadFragmentV2) {		
				initFab();
				initCallbacks();
				mDialpadFragment = (AuroraDialpadFragmentV2) fragment;

				if((intent!=null&& intent.getData() !=  null && isDialIntent(intent))){
					showDialpadFragment(true);
					return;
				}

				if (!mShowDialpadOnResume) {
					final FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.hide(mDialpadFragment);
					ft.commit();

					scaleInForFab(0, FAB_SCALE_IN_DURATION);
				}

				mIsDialpadFragmentAdded=true;
			} 
		}catch(Exception e){
			Log.d(TAG,"e:"+e);
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
		ContactsApplication.sendSimContactBroad();

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

		if(!mIsDialpadFragmentAdded){
			mHandler.removeCallbacks(loadDialFragmentRunnable);
			mHandler.postDelayed(loadDialFragmentRunnable, 700);		

			if(callLogHandler != null) {
				callLogHandler.sendEmptyMessageDelayed(ON_RESUME,1100);
			}
		}else{
			if(callLogHandler != null) {
				callLogHandler.sendEmptyMessage(ON_RESUME);
			}
		}


		//		Log.d(TAG, "onresume:"+AuroraDefaultContactBrowseListFragment.mIsAuroraSearchMode);
		//		if(AuroraDefaultContactBrowseListFragment.mIsAuroraSearchMode){
		//			//			quitSearch();
		//		}

		mFirstLaunch = false;

	}

	private Runnable loadDialFragmentRunnable=new Runnable(){    
		public void run() {    
			Log.d(TAG, "postdelayed AuroraDialActivityV3.this.isFinishing():"+AuroraDialActivityV3.this.isFinishing());
			if(AuroraDialActivityV3.this.isFinishing() ||AuroraDialActivityV3.this.isDestroyed()) return;

			if(mDialpadFragment==null){
				Log.d(TAG,"new AuroraDialpadFragmentV2");
				mDialpadFragment=new AuroraDialpadFragmentV2();
				//				if(mDialpadFragment!=null) mDialpadFragment.setmCallbacks(AuroraDialActivityV3.this);
			}

			final FragmentTransaction transaction=getFragmentManager().beginTransaction();
			transaction
			//		.add(R.id.auroratabwidget_layout, mainFragment, MAIN_FRAGMENT)
			.add(R.id.dialpad_fragment_layout,mDialpadFragment, TAG_DIALPAD_FRAGMENT)
			.commit();





		} 
	};


	private boolean mIsDialpadFragmentAdded=false;

	private AuroraProgressDialog mImportProgressDialog;
	@Override
	public void onNewIntent(Intent newIntent) {
		Log.d(TAG,"onNewIntent:"+newIntent);
		intent=newIntent;

		if(intent==null) return;

		Log.d(TAG,"onNewIntent:"+intent+" "+intent.getAction()+" intent.getdata:"+intent.getData()
				);	
		//		if ((newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
		//			return;
		//		}

		switch_to_contacts_page=intent.getBooleanExtra("switch_to_contacts_page", false);
		Log.d(TAG,"switch_to_contacts_page:"+switch_to_contacts_page);
		boolean isImporting=intent.getBooleanExtra("is_importing", false);
		Log.d(TAG, "isImporting:"+isImporting);

		if(isImporting){//来源与导入联系人跳转
			if (AuroraContactImportExportActivity.is_importexporting_sim || VCardService.mIsstart) {
				//				Toast.makeText(context, R.string.aurora_daochu_daoru_ing, Toast.LENGTH_SHORT).show();

				if (mImportProgressDialog == null) {
					mImportProgressDialog = new AuroraProgressDialog(context, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
					mImportProgressDialog.setTitle(R.string.aurora_daochu_daoru_ing);
					mImportProgressDialog.setIndeterminate(false);
					mImportProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条  
					mImportProgressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消  
					mImportProgressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
					mImportProgressDialog.setMax(100);
				}
				
				try {
					this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mImportProgressDialog.show();
							Log.d(TAG, "mImportProgressDialog  show");
						}
					});
				} catch (Exception e) {
					Log.d(TAG, "e:"+e);
				}

			}
		}

		//		checkMissedCall();

		setReallyUri(intent);
		setIntent(intent);

		if((intent!=null&& intent.getData() !=  null && isDialIntent(intent))){
			Log.d(TAG, "intent!=null&& intent.getData()!=null");
			showDialpadFragment(true);
		}else{
			Log.d(TAG, "intent.getData() =null,mIsDialpadShown:"+mIsDialpadShown);
			if(mIsDialpadShown&&!Intent.ACTION_MAIN.equals(intent.getAction())){
				hideDialpadFragment(false, true);
			}
		}
	}

	private void setReallyUri(Intent intent) {
		if (intent != null) {
			mRealUri = intent.getData();
		}
	}

	class ImportContactsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			int flag=intent.getIntExtra("flag", -1);
			int percentage=intent.getIntExtra("percentage", 0);
			String description=intent.getStringExtra("description");
			if (action.equals("com.android.action.LAUNCH_CONTACTS_LIST")) {
				Log.d(TAG,"[onReceive]intent: " + intent+" flag:"+flag+" percentage:"+percentage);
				if(flag==1){
					try {
						if(mImportProgressDialog!=null){
							mImportProgressDialog.dismiss();
							mImportProgressDialog = null;
						}
					} catch (Exception e) {
						Log.e(TAG, "e:"+e);
					}
				}
				
				if(percentage>0){
					mImportProgressDialog.setProgress(percentage);  
					mImportProgressDialog.setTitle(description);
				}
			}
		}
	};

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


	public void getCaller(){   
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

		Log.v(TAG, "onDestroy:"+AuroraDialActivityV3.this);
		//		getCaller();     
		mDialpadFragment=null;
		mainFragment=null;

		try{
			if (mReceiver != null) {
				this.unregisterReceiver(mReceiver);
				mReceiver = null;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}


	private Animation mSlideOut;
	private Animation mSlideIn;
	private boolean mIsLandscape;
	private boolean actionBarChangeFlag=false;
	/**
	 * Initiates animations and other visual updates to hide the dialpad. The fragment is hidden in
	 * a callback after the hide animation ends.
	 * @see #commitDialpadFragmentHide
	 */
	public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
		Log.d(TAG,"hideDialpadFragment");
		if(isFastClick(500)) {
			Log.d(TAG,"isFastClick");
			return;
		}
		if (mDialpadFragment == null ||mainFragment==null) {
			Log.d(TAG, "mDialpadFragment null");
			return;
		}

		Log.d(TAG, "mIsDialpadShown:"+mIsDialpadShown);
		if (!mIsDialpadShown) {
			Log.d(TAG, "mIsDialpadShown1:"+mIsDialpadShown);
			return;
		}
		//		if(true){
		//			mDialpadFragment.scaleOut();
		//			return;
		//		}



		ContactsApplication.sendSimContactBroad();
		Log.d(TAG,"hideDialpadFragment1");
		if (clearDialpad) {
			mDialpadFragment.clearDigits();
		}

		if(actionBar!=null){
			actionBarChangeFlag=true;
			actionBar.changeAuroraActionbarTypeWitchAlphaAnim(AuroraActionBar.Type.Empty,typeChangeListener);
		}

		//		if(mainFragment.scrollIconView!=null){
		//			//			AnimUtils.move(mainFragment.scrollIconView,//view
		//			//					500,//duration
		//			//					0,//delay
		//			//					AnimUtils.EASE_OUT_EASE_IN1,//Interpolator
		//			//					false,//isHorizontal
		//			//					-mainFragment.scrollIconView.getHeight(),//startPosition
		//			//					0,//endPosition
		//			//					null//AnimatorListenerAdapter
		//			//					);
		//			mainFragment.scrollIconView.setVisibility(View.VISIBLE);
		//
		//		}

		if(mainFragment!=null&&mainFragment.getView()!=null){
			AnimUtils.fadeIn(mainFragment.getView(), 
					200,
					200,
					null,
					AnimUtils.EASE_OUT1);
		}

		//		actionBar.setVisibility(View.VISIBLE);
		mDialpadFragment.setAnimate(animate);
		//		if(mainFragment!=null) mainFragment.setUserVisibleHint(true);
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
			//			scaleInForFab(400,FAB_SCALE_IN_DURATION);


		} else {
			commitDialpadFragmentHide();

		}

		mIsDialpadShown=false;

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
		

		Log.v(TAG, "AuroraDialActivityV3 dispatchKeyEvent ,KeyCode:"+event.getKeyCode()+" action:"+event.getAction()+" RepeatCount():"+event.getRepeatCount()); 

		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_UP){
			boolean isFastClick=isFastClick(400);
			Log.d(TAG, "isFastClick:"+isFastClick);
			if(mainFragment==null){
				return true;
			}

			Log.d(TAG, "AuroraDialActivityV3.this.isFinishing():"+AuroraDialActivityV3.this.isFinishing());
			if(AuroraDialActivityV3.this.isFinishing() ||AuroraDialActivityV3.this.isDestroyed()) {
				Log.d(TAG, "isFinishing|destroy");
				return true;
			}


			Log.d(TAG,"isSlideOutAnimating:"+isSlideOutAnimating+" mIsDialpadShown:"+mIsDialpadShown);
			if(isSlideOutAnimating) return true;

			if(mIsDialpadShown) {
				hideDialpadFragment(true, true);
				return true;
			}

			if(mSearchView!=null&&mSearchView.isShown()&&!isFastClick){
				quitSearch();
				return true;
			}


			if(mainFragment.currIndex==0){
				if(AuroraCallLogFragmentV2.getEditMode()&&!isFastClick){
					Log.d(TAG, "AuroraCallLogFragmentV2 is edit mode");
					if(callLogHandler!=null) callLogHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}else if(mainFragment.currIndex==1){
				if(AuroraDefaultContactBrowseListFragment.mIsEditMode&&!isFastClick){
					Log.d(TAG, "AuroraDefaultContactBrowseListFragment is edit mode");
					if(contactsHandler!=null) contactsHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}

			Log.d(TAG, "finish");
			AuroraDialActivityV3.this.finish();
			return true;	
		}

//		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
//			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
//			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
//			//			Log.d(TAG,"visible:"+(getAuroraMenu()).getVisibility());
//			//		   actionBar.setVisibility(visibility)
//			//			showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,
//			//					DensityUtil.dip2px(context, 16), DensityUtil.dip2px(context, 26));
//			//			return true;	
//
//		}

		//		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU && event.getA==KeyEvent.ACTION_UP){
		//			Log.d(TAG, "click menu:"+auroraMenuIsEnable());
		//		
		//			showAuroraMenu();
		//		}

		//		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
		//			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		//		}

		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK) return true;
		
		Log.d(TAG,"dispatchKeyEvent1.");
		return super.dispatchKeyEvent(event);
	}
	
/*	public boolean dispatchKeyEvent(KeyEvent event) {

		Log.v(TAG, "AuroraDialActivityV3 dispatchKeyEvent ,KeyCode:"+event.getKeyCode()+" action:"+event.getAction()+" RepeatCount():"+event.getRepeatCount()); 

		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_UP){
			if(mainFragment==null){
				return true;
			}

			Log.d(TAG, "AuroraDialActivityV3.this.isFinishing():"+AuroraDialActivityV3.this.isFinishing());
			if(AuroraDialActivityV3.this.isFinishing() ||AuroraDialActivityV3.this.isDestroyed()) {
				Log.d(TAG, "isFinishing|destroy");
				return true;
			}


			Log.d(TAG,"isSlideOutAnimating:"+isSlideOutAnimating+" mIsDialpadShown:"+mIsDialpadShown);
			if(isSlideOutAnimating) return true;

			if(mIsDialpadShown) {
				hideDialpadFragment(true, true);
				return true;
			}

			if(mSearchView!=null&&mSearchView.isShown()&&!isFastClick(400)){
				quitSearch();
				return true;
			}


			if(mainFragment.currIndex==0){
				if(AuroraCallLogFragmentV2.getEditMode()&&!isFastClick(400)){
					Log.d(TAG, "AuroraCallLogFragmentV2 is edit mode");
					if(callLogHandler!=null) callLogHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}else if(mainFragment.currIndex==1){
				if(AuroraDefaultContactBrowseListFragment.mIsEditMode&&!isFastClick(400)){
					Log.d(TAG, "AuroraDefaultContactBrowseListFragment is edit mode");
					if(contactsHandler!=null) contactsHandler.sendEmptyMessage(SWITCH_TO_NORMAL_MODE);
					return true;
				}
			}

			Log.d(TAG, "finish");
			AuroraDialActivityV3.this.finish();
			return true;	
		}

		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
			//			setAuroraMenuItems(R.menu.actionbar_more_menu);
			//			Log.d(TAG,"visible:"+(getAuroraMenu()).getVisibility());
			//		  actionBar.setVisibility(visibility)
			//			showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,
			//					DensityUtil.dip2px(context, 16), DensityUtil.dip2px(context, 26));
			//			return true;	

		}

		//		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU && event.getA==KeyEvent.ACTION_UP){
		//			Log.d(TAG, "click menu:"+auroraMenuIsEnable());
		//		
		//			showAuroraMenu();
		//		}

		//		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
		//			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		//		}

		Log.d(TAG,"dispatchKeyEvent1");
		return super.dispatchKeyEvent(event);
	}*/

	private long lastClickTime; 
	public boolean isFastClick(long duration) { 
		long time = System.currentTimeMillis(); 
		long timeD = time - lastClickTime; 
		if ( 0 < timeD && timeD < duration) {    
			return true;    
		}    
		lastClickTime = time;    
		return false;    
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
			//			if(isGestureDetected) {
			//				ev.setAction(MotionEvent.ACTION_CANCEL);
			//			}
		}

		switch(ev.getAction()){  
		case MotionEvent.ACTION_DOWN:  	
			Log.i(TAG, "dispatchTouchEvent-ACTION_DOWN..."); 

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
	public void onBackPressed() {
		Log.d(TAG, "onbackpressed");
		return;
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
			break;
		}

		case KeyEvent.KEYCODE_MENU:{
//			Log.d(TAG, "fab1.visiblity3:"+fab1.getVisibility());
			Log.d(TAG,"click KEYCODE_MENU");
			setAuroraSystemMenuCallBack(auroraMenuCallBack);
			setAuroraMenuItems(R.menu.actionbar_more_menu);
			showAuroraMenu(getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,0,0
					/*context.getResources().getDimensionPixelOffset(R.dimen.calllog_popupwindow_margin_right),
					context.getResources().getDimensionPixelOffset(R.dimen.more_popupwindow_margin_top) */);
			return true;
			/*

			if (event.getSource() != 99) {
				addMenu(AuroraMenu.FIRST, getString(R.string.call_settings), new OnMenuItemClickLisener() {
					public void onItemClick(View menu) {
//						startActivity(DialtactsActivity.getCallSettingsIntent());
						Intent intent=new Intent(context, AuroraContactsSetting.class);
						startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);
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
		*/}
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
			/*		

			if(mDialpadFragment==null) return true;

			if(null == e1 || null == e2) return true;

			if(mDialpadFragment.getDigits2()==null) return true;

			if(mDialpadFragment.getDigits2().getVisibility()==View.GONE) return true;

				if (null != mDialpadFragment &&
						mDialpadFragment.isDialpadShowing() &&
						mDialpadFragment.getDigitsTextLen() > 0) {

					Rect rect = mDialpadFragment.getHideDialpadTouchRect();
					if (null != e1 && null != rect && rect.contains((int)e1.getX(), (int)e1.getY())) {

						mDialpadFragment.showDialpad(false, true);

						return true;
					}
				}*/

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

			if(null == e1 || null == e2) return true;

			if(mDialpadFragment.getDigits2()!=null&&mDialpadFragment.getDigits2().getVisibility()==View.GONE) return true;

			//						Rect rect = mDialpadFragment.getKeyBoardFlipperRect();
			boolean isDialpadShowing = mDialpadFragment.isDialpadShowing();
			int x1 = (int)e1.getX();
			int y1 = (int)e1.getY();
			int x2 = (int)e2.getX();
			int y2 = (int)e2.getY();
			int distanceX = x2 - x1;
			int distanceY = y2 - y1;

			//						if (ContactsApplication.sIsGnQwertDialpadSupport) {
			//							if (Math.abs(distanceY) < FLING_MAX_VERTICAL_DISTANCE &&
			//									rect.contains(x1, y1) && rect.contains(x2, y2)) {
			//								if (isDialpadShowing) {
			//									if (distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
			//										return true;
			//									} else if (-distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
			//										return true;
			//									}
			//								}
			//							}
			//						}

			Rect rect = mDialpadFragment.getDialpadFlipperRect();
			if (Math.abs(distanceX) < FLING_MAX_HORIZONTAL_DISTANCE/* && rect.contains(x1, y1)*/) {
				if (Math.abs(distanceY) > FLING_MIN_VERTICAL_DISTANCE && isDialpadShowing) {

					if(mDialpadFragment.getDigitsTextLen() > 0){
						mDialpadFragment.showDialpad(false, true);
						return true;
					}else{
						hideDialpadFragment(true, true);
					}
				}
			}

			return true;
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
				AnimUtils.scaleOut(fab1, 0,0,null,View.GONE);
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
				//				fab1.setVisibility(View.VISIBLE);
				AnimUtils.move(fab1,0,0,AnimUtils.CURVE_SHOW,false,
						context.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_action_bottom_bar_height),0,null);	
				scaleInForFab(300, FAB_SCALE_IN_DURATION);
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