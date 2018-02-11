/*//<!-- aurora add zhouxiaobing 20130910 --> for dial launcher
package com.android.contacts.activities;
import gionee.provider.GnContactsContract.Contacts;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.SharedPreferencesUtil;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AuroraTabHost;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.R.anim;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CallLog.Calls;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;


@SuppressWarnings("deprecation")
public class AuroraDialActivityV3_backup extends AuroraActivity implements OnTouchListener,View.OnClickListener{
	private Intent oldIntent=null;
	private Uri mRealUri;

	private boolean mHasMissedCall = false;
	private AuroraActionBar actionBar;
	private static String TAG="AuroraDialActivityV3";
	private RelativeLayout fab;



	private Context context = null;
	private LocalActivityManager manager = null;
	private ViewPager pager = null;
	private TextView t1,t2;

	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView cursor;// 动画图片

	private boolean mIsPrivate = false;
	private static final int AURORA_MORE = 1;

	private Bitmap tabTitleBmp;

	private RelativeLayout actionBarLayout;
	private LinearLayout linearLayout1;

	public static PopupWindow popupWindowMore;
	private LinearLayout pop_newcontact;
	private LinearLayout pop_record;
	private LinearLayout pop_setting;
	private LinearLayout search_linearLayout;
	private LinearLayout more_linearLayout;

	private Bitmap createColorBitmap(String rgb, int width, int height) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int color = Color.parseColor(rgb);
		bmp.eraseColor(color);
		return bmp;
	}


	private void getDisplayInfomation() {  
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
    }  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getDisplayInfomation();
		getDensity();
		getScreenSizeOfDevice();
		getScreenSizeOfDevice2();
		Log.d(TAG, "onCreate");

		setAuroraContentView(R.layout.aurora_dial_main2,AuroraActionBar.Type.Empty);
		actionBar = getAuroraActionBar();

//		mIsPrivate = ((AuroraCallLogActivity)getActivity()).isPrivate();
		actionBar.setTitle(R.string.launcherDialer);

		actionBar.addItem(AuroraActionBarItem.Type.More, AURORA_MORE);

		actionBar.setVisibility(View.GONE);


		//		setContentView(R.layout.aurora_dial_main2);
		checkMissedCall();

		context = AuroraDialActivityV3.this;
		manager = new LocalActivityManager(this , true);
		manager.dispatchCreate(savedInstanceState);
		fab=(RelativeLayout)findViewById(R.id.fab);
		fab.setOnClickListener(this);

		InitImageView();
		initTextView();
		initPagerViewer();

		actionBarLayout=(RelativeLayout)findViewById(R.id.actionBar);
		actionBarLayout.setOnTouchListener(this);
		linearLayout1=(LinearLayout)findViewById(R.id.linearLayout1);
		more_linearLayout=(LinearLayout)findViewById(R.id.more_linearLayout);
		search_linearLayout=(LinearLayout)findViewById(R.id.search_linearLayout);

		more_linearLayout.setOnClickListener(this);
		search_linearLayout.setOnClickListener(this);

		AuroraCallLogActivity.setOnSetViewPagerListener(new OnSetViewPagerListener() {

			@Override
			public void setVisibity(boolean flag) {
				// TODO Auto-generated method stub
				if(flag){
					actionBarLayout.setVisibility(View.VISIBLE);
					linearLayout1.setVisibility(View.VISIBLE);
					cursor.setVisibility(View.VISIBLE);
				}else{
					actionBarLayout.setVisibility(View.GONE);
					linearLayout1.setVisibility(View.GONE);
					cursor.setVisibility(View.GONE);
				}
			}
		});


	}


	//监听器，用于子activity进入编辑模式时回调设置viewPager的显示状态
	public interface OnSetViewPagerListener{
		public void setVisibity(boolean flag);		
	}


	*//**
	 * 初始化标题
	 *//*
	private void initTextView() {
		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);

		t1.setOnTouchListener(this);
		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));



	}
	*//**
	 * 初始化PageViewer
	 *//*
	private void initPagerViewer() {
		pager = (ViewPager) findViewById(R.id.viewpage);
		final ArrayList<View> list = new ArrayList<View>();
		Intent intent = new Intent(context, AuroraCallLogActivity.class);
		list.add(getView("A", intent));
		Intent intent2 = new Intent(context, AuroraPeopleActivity.class);
		list.add(getView("B", intent2));


		pager.setAdapter(new MyPagerAdapter(list));
		pager.setOnPageChangeListener(new MyOnPageChangeListener());


		if(currIndex==-1) currIndex=0;
		ContactsApplication.getInstance().setCurrentPage(currIndex);

		if(currIndex==0){
			pager.setCurrentItem(currIndex);
			t1.setTextColor(getResources().getColor(R.color.calllog_tab_current));
			t2.setTextColor(getResources().getColor(R.color.calllog_tab_notcurrent));
		}else if(currIndex==1){
			pager.setCurrentItem(currIndex);
			t1.setTextColor(getResources().getColor(R.color.calllog_tab_notcurrent));
			t2.setTextColor(getResources().getColor(R.color.calllog_tab_current));
		}
	}
	*//**
	 * 初始化动画
	 *//*
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);


		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		bmpW=screenW/2;
		tabTitleBmp = createColorBitmap("#36b9b1", bmpW, DensityUtil.dip2px(context, 2));
		cursor.setImageBitmap(tabTitleBmp);
		offset = 0;// 偏移量
		currIndex=SharedPreferencesUtil.getInstance(context).getInt("currentTab");

		Matrix matrix = new Matrix();
		if(currIndex==0){
			matrix.postTranslate(offset, 0);
		}else if(currIndex==1){
			matrix.postTranslate(bmpW, 0);
			offset=bmpW;
		}
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}


	*//**
	 * 通过activity获取视图
	 * @param id
	 * @param intent
	 * @return
	 *//*
	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	*//**
	 * Pager适配器
	 *//*
	public class MyPagerAdapter extends PagerAdapter{
		List<View> list =  new ArrayList<View>();
		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position,
				Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
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
	*//**
	 * 页卡切换监听
	 *//*
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = bmpW;// 页卡1 -> 页卡2 偏移量


		@Override
		public void onPageSelected(int arg0) {
			AuroraCallLogFragmentV2.dismissPopupWindow();
			dismissPopupWindow();

			Log.e(TAG, "arg0:"+arg0);
			Log.e(TAG, "currIndex:"+currIndex);
			Animation animation = null;
			SharedPreferencesUtil.getInstance(context).putInt("currentTab", arg0);

			switch (arg0) {
			case 0:		

				if (currIndex == 1) {
					Log.e(TAG, "case0 offset:"+offset+" one:"+one);
					if(offset==0){
						animation = new TranslateAnimation(one, offset, 0, 0);
					}else if(offset==bmpW){
						animation = new TranslateAnimation(0, -offset, 0, 0);
					}
				} 

				t1.setTextColor(getResources().getColor(R.color.calllog_tab_current));
				t2.setTextColor(getResources().getColor(R.color.calllog_tab_notcurrent));

				break;
			case 1:

				t1.setTextColor(getResources().getColor(R.color.calllog_tab_notcurrent));
				t2.setTextColor(getResources().getColor(R.color.calllog_tab_current));
				if (currIndex == 0) {
					Log.e(TAG, "case1 offset:"+offset+" one:"+one);
					if(offset==0){
						animation = new TranslateAnimation(0, one, 0, 0);
					}else if(offset==bmpW){
						animation = new TranslateAnimation(-offset, 0, 0, 0);
					}				
				} 

				break;

			}
			currIndex = arg0;
			if(animation!=null){
				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(300);
				cursor.startAnimation(animation);
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}
	*//**
	 * 头标点击监听
	 *//*
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			pager.setCurrentItem(index);
		}
	};



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
		String type = this.getIntent().getType();

		// CallLog.Calls.CONTENT_TYPE
		if (type != null
				&& type.equalsIgnoreCase("vnd.android.cursor.dir/calls")
				|| mHasMissedCall) {
			mIsCalllog = true;
			mHasMissedCall = false;
			//			th.setCurrentTabNoAnimation(0);
		} else if (isDialIntent(this.getIntent())) {
			mIsCalllog = false;
			//			th.setCurrentTabNoAnimation(1);
		} else {
			mIsCalllog = false;
			//			th.setCurrentTabNoAnimation(mCurrentTab);
		}

		if(oldIntent!=null){
			setIntent(oldIntent);
		}


	}

	@Override
	public void onNewIntent(Intent newIntent) {
		oldIntent=getIntent();//bug10983
		if ((newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			return;
		}

		checkMissedCall();

		setReallyUri(newIntent);
		setIntent(newIntent);
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

	@Override
	protected void onDestroy() {
		Log.v("AuroraDialActivity", "onDestroy");
		if (!mIsCalllog) {
			Log.v("AuroraDialActivity", "onDestroy tabIndex= " + ContactsApplication.getInstance().getCurrentPage());

			SharedPreferencesUtil.getInstance(context).putInt("currentTab", ContactsApplication.getInstance().getCurrentPage());
		}
		super.onDestroy();
	}
	//aurora add liguangyu 201311128 end




	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.v("SHIJIAN", "AuroraDialActivity dispatchKeyEvent  "); 
		if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
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
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}




	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("liyang","onTouch1");
		// TODO Auto-generated method stub

		AuroraCallLogFragmentV2.dismissPopupWindow();
		AuroraDialActivityV3.dismissPopupWindow();

		return false;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("liyang","onKeyDown2");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {

		}
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		AuroraCallLogFragmentV2.dismissPopupWindow();
		switch (id) {
		case R.id.fab:{			
			// TODO Auto-generated method stub
			Log.i("liyang","AuroraDialActivityV3 fab onclick");
			Intent intent=new Intent(context,AuroraDialtactsActivityV2.class);
			context.startActivity(intent);
			break;
		}

		case R.id.search_linearLayout:{
			break;
		}

		case R.id.more_linearLayout:{
			View contentView = View.inflate(context,
					R.layout.popupwindowmore, null);

			pop_newcontact = (LinearLayout) contentView
					.findViewById(R.id.pop_newcontact);
			pop_record = (LinearLayout) contentView
					.findViewById(R.id.pop_record);
			pop_setting = (LinearLayout) contentView
					.findViewById(R.id.pop_setting);


			pop_newcontact.setOnClickListener(AuroraDialActivityV3.this);
			pop_record.setOnClickListener(AuroraDialActivityV3.this);
			pop_setting.setOnClickListener(AuroraDialActivityV3.this);

			LinearLayout ll_popup_container = (LinearLayout) contentView
					.findViewById(R.id.ll_popup_container);

			ScaleAnimation sa = new ScaleAnimation( 0.0f,
					1.0f,
					0.0f,
					1.0f,
					Animation.RELATIVE_TO_SELF, 
					1.0f, 
					Animation.RELATIVE_TO_SELF, 
					0.0f);
			sa.setDuration(100);				


			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenW = dm.widthPixels;// 获取分辨率宽度

			popupWindowMore = new PopupWindow(contentView, DensityUtil.dip2px(context, 130), 
					DensityUtil.dip2px(context, 160));
			popupWindowMore.setBackgroundDrawable(new ColorDrawable(
					Color.TRANSPARENT));


			popupWindowMore.showAtLocation(v, Gravity.TOP | Gravity.LEFT,
					screenW-DensityUtil.dip2px(context, 146), DensityUtil.dip2px(context, 26));

			ll_popup_container.startAnimation(sa);

			break;
		}

		case R.id.pop_newcontact:{
			Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				intent.putExtras(extras);
			}
			startActivity(intent);
			dismissPopupWindow();
			break;
		}

		case R.id.pop_record:{
			startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
			dismissPopupWindow();
			break;
		}

		case R.id.pop_setting:{
			startActivity(DialtactsActivity.getCallSettingsIntent());
			dismissPopupWindow();
			break;
		}
		default:
			break;

		}
	}




	public static void dismissPopupWindow() {
		// TODO Auto-generated method stub
		if (popupWindowMore != null && popupWindowMore.isShowing()) {
			popupWindowMore.dismiss();
			popupWindowMore = null;
		}

	}
}

*/