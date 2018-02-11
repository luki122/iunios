package com.aurora.weatherforecast;

import interfaces.IWeatherView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherdata.util.SystemUtils;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.CityListShowItem;
import datas.DynamicDeskIconService;
import datas.WeatherAnimInfo;
import datas.WeatherData;
import datas.WeatherDataEveryDay;
import datas.WeatherForcastInfo;
import datas.WeatherHourInfo;
import datas.CityListShowData.INotifyListener;
import totalcount.AddCountHelp;
import views.CircleIndexView;
import views.CityDateView;
import views.CityOptionView;
import views.OptionView;
import views.WeatherAnimSurfaceView;
import views.WeatherDetailsView;
import views.WeatherMainView;
import views.WeatherAnimView;
import views.WeatherViewPager;
import android.R.bool;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.service.notification.INotificationListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import adapters.OptionPageAdapter;
@SuppressLint("NewApi")
public class WeatherMainFragment extends Fragment implements OnPageChangeListener, OnTouchListener {
	private View mRootView;
	private ViewPager mWeatherViewPager;
	private int curPosition = 0;
	private int prePosition = 0;
	private boolean pageFlag = false;
	private ArrayList<WeatherMainView> pageViewsList=new ArrayList<WeatherMainView>();
	private LayoutInflater mInflater;
	private CircleIndexView mCircleIndexView;
	private Context mContext;
	private CityOptionView mCityOptionView;
	private ImageView switchOptionView;
	private WeatherAnimSurfaceView mWeatherView;
	private OptionPageAdapter mOptionPageAdapter;
	private static final long TIME_INTERVAL = 60 * 60 * 1000;
	private int mCurCityNumber = 0;
	private int mAddCityIndex = -1;
	private float lastScrollX = 1;
	private View mWeatherBackView;
	private long upDateTime = 0;
	public static final int MARK_HOUR_1 = 6;
	public static final int MARK_HOUR_2 = 18;
	public static final int MARK_HOUR_3 = 12;
	public static final int MARK_HOUR_4 = 20;
	private SharedPreferences mPrefs;
	
	private Handler updateHandler = new Handler();
	
	public static final String UPDATE_BROADCAST_ACTION = "com.aurora.weatherfoecast.updateweather";
	
	private CityListShowData mCityListShowData;
	
	private ImageView iv_bottom;
	
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.weather_main_fragment_layout, container, false);  
		mCityListShowData=CityListShowData.getInstance(mContext);
		updateLayout( );
		setRootViewAnimator();
		mRootView.setVisibility(View.INVISIBLE);
		return mRootView;  
	}
	
	private void saveUpdateTime( ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		//用putString的方法保存数据
		editor.putLong("updateTime", upDateTime);
		//提交当前数据
		editor.commit(); 
	}

	public boolean isShowing(){
		return mRootView!=null&&mRootView.getVisibility()==View.VISIBLE;
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if ( mRefreshDrawable != null ) {
			mRefreshDrawable.stop();
		}
		allViewDestory();
		pageViewsList.clear();
		saveUpdateTime();
		mWeatherView.onDestroy();
	}
	
	private void allViewDestory(){
		for(int i=0;i<pageViewsList.size();i++)
		{
			pageViewsList.get(i).onStateChanged(IWeatherView.QUIT_APP);
		}
	}
	
	public WeatherAnimSurfaceView getWeatherAnimSurfaceView(){
		return mWeatherView;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity;
	}

	private Animator rootViewAnimatorIn,rootViewAnimatorOut;
	private final int ENTER_DURATION=500,EXIT_DURATION=500;
	private void setRootViewAnimator(){
		rootViewAnimatorIn=ObjectAnimator.ofFloat(mRootView, "alpha", 0,1).setDuration(ENTER_DURATION);
		rootViewAnimatorOut=ObjectAnimator.ofFloat(mRootView, "alpha", 1,0).setDuration(EXIT_DURATION);
		rootViewAnimatorIn.addListener(rootViewAnimatorListener);
		rootViewAnimatorOut.addListener(rootViewAnimatorListener);
	}
	
	public void show(boolean isRunAnimation) {
		if (isRunAnimation) {
			rootViewAnimatorIn.start();
		} else {
			mRootView.setVisibility(View.VISIBLE);
			mRootView.setAlpha(1);
			playCommingInAnim();
		}
	}
	
	public void hide(boolean isRunAnimation) {
		if (isRunAnimation) {
			rootViewAnimatorOut.start();
		} else {
			mRootView.setVisibility(View.INVISIBLE);
			mRootView.setAlpha(0);
		}
	}

	public boolean isAnimatorRunning(){
		return rootViewAnimatorIn.isRunning()||rootViewAnimatorOut.isRunning();
	}
	
	public void setBottomViewHeight(int height){
		RelativeLayout.LayoutParams rlp = (LayoutParams) iv_bottom.getLayoutParams();
		rlp.width = LayoutParams.MATCH_PARENT;
		rlp.height = height;
		iv_bottom.setLayoutParams(rlp);
	}
	
	private static final int REQUEST_CODE = 0x4512;
	
	private void updateLayout() {
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addFirstView();
		mWeatherBackView = (View)mRootView.findViewById(R.id.weather_back);
		String firstWeatherType = ((AuroraWeatherMain)mContext).getFirstPageCityWeatherType();
		if(firstWeatherType!=null)
		{
			mWeatherBackView.setBackgroundResource(mCityListShowData.getBgResIdByWeatherType(firstWeatherType));
		}
		iv_bottom = (ImageView)mRootView.findViewById(R.id.iv_bottom);
		mWeatherView = (WeatherAnimSurfaceView)mRootView.findViewById(R.id.weather_animview);
		mOptionPageAdapter = new OptionPageAdapter(pageViewsList);
		mWeatherViewPager = (ViewPager)mRootView.findViewById(R.id.weatherviewpager);
		mWeatherViewPager.setAdapter(mOptionPageAdapter);
		mWeatherViewPager.setOnTouchListener(this);
		mWeatherViewPager.setOnPageChangeListener(this);
		mCircleIndexView = (CircleIndexView) mRootView.findViewById(R.id.circleindexview);
		for (int i = 0; i < mCurCityNumber; i++) {
			mCircleIndexView.addCirclePointView();
		}
		mWeatherViewPager.setCurrentItem(curPosition);
		mCircleIndexView.changeCircleState(curPosition);
		mCityOptionView = (CityOptionView) mRootView.findViewById(R.id.cityoptionview);
		switchOptionView=mCityOptionView.getOptionView().getOptionViewImage();
		mCityOptionView.getOptionView().setVisibility(View.VISIBLE);
		mCityOptionView.getOptionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCityListShowData.getCitySize()==0)
				{
					Intent i=new Intent(mContext, AuroraWeatherSearchCity.class);
					startActivityForResult(i,REQUEST_CODE);
					return;
				}
				// TODO Auto-generated method stub
				if(!isAnimatorRunning()&&((AuroraWeatherMain)mContext).isCityListFragmentIntiOk())
				{
				   ((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.CITYLIST_FRAGMENT_INDEX,true);
				}
			}
		});
		mCityDateView = mCityOptionView.getCityDateView();
		mRefreshRela = mCityOptionView.getRefreshRela();
		mRefreshText = mCityOptionView.getRefreshText();
		mRefreshImage = mCityOptionView.getRefreshImage();
		mRefreshDrawable = (AnimationDrawable)mRefreshImage.getDrawable();
		mRefreshDrawable.stop();
		mCityListShowData.addINotifyListener(new INotifyListener() {
			@Override
			public void notityDataChange(int... opeTypes) {
				changeCityListInfo(opeTypes);
			}
		});
		mPrefs = ((AuroraWeatherMain)mContext).getSharedPreferences("updateTime", Activity.MODE_PRIVATE);
		upDateTime = mPrefs.getLong("updateTime", 0);
		TOTAL_Y = DensityUtil.dip2px(mContext, 66);
		if(SystemUtils.hasActiveNetwork(mContext)&&!((AuroraWeatherMain)mContext).isComeFromNotification)
		{
			playRefreshAnim();
		}
		((AuroraWeatherMain)mContext).isComeFromNotification = false;
	}
	
	private void startLoadMoreViewThread(){
		if(!hasLoadAllViews)
		{
			hasLoadAllViews=true;
			addMoreViewThread.start();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE && resultCode == AuroraWeatherSearchCity.RESULTCODE)
		{
			Log.e("jadon1", "onActivityResult");
			mWeatherView.setIndex(curPosition);
		}
	}
	
	private boolean isAdd=false;
	private void changeCityListInfo( int... opeType ) {
		switch (opeType[0]) {
		case INotifyListener.OPE_DELETE:
			if(opeType[1]==0&&mCityListShowData.getCitySize()==0)
			{
				pageViewsList.get(0).setIndex(-1);
				mCityOptionView.setIndex(-1);
				mCityOptionView.onStateChanged(IWeatherView.NORMAL_SHOWING);
				pageViewsList.get(0).onStateChanged(IWeatherView.NORMAL_SHOWING);
				updateWeatherBackground(-1);
				switchOptionView.setImageResource(R.drawable.add_optionviewsrc_selector);
				updateWeatherAnim();
			}else if(opeType[1] <= pageViewsList.size()-1){
			     deleteCityForIndex(opeType[1]); 
			}
			break;
		case INotifyListener.OPE_ADD:
			isAdd=true;
			addANewCity(opeType[1]); 
			break;
		case INotifyListener.OPE_ITEM_SELECT:
			setCityForIndex(opeType[1]);
			break;
		case INotifyListener.OPE_SWITCH_POSITION:
			switchCityPosition(opeType[1], opeType[2]);
			break;
		case INotifyListener.OPE_UPDATE_DATA:
			boolean isFromDb=opeType[2]==1;
			updateDataByIndex(opeType[1],isFromDb);
			break;
		case INotifyListener.OPE_LOCAL_CITY_PAGE_CHANGE:
			refreshAllWeatherData();
			break;
		case INotifyListener.OPE_ADD_LOCAL_CITY:
			addANewLocalCity(opeType[1]);
			break;
		case INotifyListener.OPE_REQUEST_ALL_BACK:
			boolean fromDb=opeType[1]==1;
			if(fromDb)
			{
				if(SystemUtils.hasActiveNetwork(mContext))
				{
					refreshAllWeatherData();
				}else{
					startLoadMoreViewThread();
				}
			}
			isRefreshing=false;
			break;
		case INotifyListener.OPE_NETWORK_ERROR:
			isRefreshing=false;
			if(mCityListShowData.getCitySize()==0)
			{
				switchOptionView.setImageResource(R.drawable.add_optionviewsrc_selector);
			}
			if(totalY!=0)
			{
			   playRefreshBackAnim(totalY/2,false);
			}
			break;
		case INotifyListener.OPE_LOCALCITY_ENABLE:
			mCircleIndexView.setLocalPosition(0);
			break;
		case INotifyListener.OPE_RESET_LAST_ADD:
			if(opeType[1]!=-1)
			{
				deleteCityForIndex(opeType[1]);
			}
			break;
		default:
			break;
		}
	}
	
	private void deleteCityForIndex(int index) {
		mCircleIndexView.removeCirclePointView(index);
		if ( curPosition >= index ) {
			if ( index == 0 ) {
				curPosition = 0;
			} else {
				curPosition -= 1;
			}
		}
		pageViewsList.remove(index);
		setPageViewsIndex();
		normalshowData(curPosition);
		mWeatherViewPager.setAdapter(mOptionPageAdapter);
		mWeatherViewPager.setCurrentItem(curPosition);
		mCircleIndexView.changeCircleState(curPosition);
		mCurCityNumber = pageViewsList.size(); 
	}
	
	private void setPageViewsIndex(){
		for(int i=0;i<pageViewsList.size();i++)
		{
			pageViewsList.get(i).setIndex(i);
		}
		
	}
	
	private void addANewLocalCity(int index) {
		if(pageViewsList.size()<mCityListShowData.getCitySize())
		{
		   createWeatherMainView(1,false);
		   mOptionPageAdapter.notifyDataSetChanged();
		   mCircleIndexView.addCirclePointView();
		}
		switchOptionView.setImageResource(R.drawable.optionviewsrc_selector);
		mCircleIndexView.setLocalPosition(0);
		refreshAllWeatherData();
		mAddCityIndex = index;
	}
	
	private void addANewCity(int index) {
		if(pageViewsList.size()<mCityListShowData.getCitySize())
		{
		   createWeatherMainView(1,false);
		   mOptionPageAdapter.notifyDataSetChanged();
		   mCircleIndexView.addCirclePointView();
		}
		switchOptionView.setImageResource(R.drawable.optionviewsrc_selector);
		refreshWeatherData(index);
		mAddCityIndex = index;
	}
	
	private void setCityForIndex(int index) {
		mWeatherViewPager.setCurrentItem(index);
	}
	
	private void switchCityPosition(int fromIndex, int toIndex) {
		normalshowData(fromIndex);
		normalshowData(toIndex);
		normalshowData(curPosition);
		mCircleIndexView.changeCircleState(curPosition);
	}
	
	/**
	 * 播放入场动画
	 */
	public void playCommingInAnim( ) {
		if(pageViewsList.size()==0)
			return;
		mCityOptionView.onStateChanged(IWeatherView.COMING_IN);
		pageViewsList.get(curPosition).onStateChanged(IWeatherView.COMING_IN);
		mWeatherView.setIndex(curPosition);
	}
	
	public void playCommingOutAnim( ) {
		if(pageViewsList.size()==0)
			return;
		mCityOptionView.onStateChanged(IWeatherView.COMING_OUT);
		pageViewsList.get(curPosition).onStateChanged(IWeatherView.COMING_OUT);
		mWeatherView.onAnimPause();
	}
	
	private void updateDataByIndex(int index,boolean isFromDb){
		if(index>pageViewsList.size()-1)
		{
			return;
		}
		
		upDateTime = System.currentTimeMillis();
		if(index==curPosition)
		{
			updateWeatherAnim();
			updateWeatherBackground(curPosition);
			mCityOptionView.setIndex(index);
			mCityOptionView.onStateChanged(IWeatherView.NORMAL_SHOWING);
		}
		
		if(index==0&&mCityListShowData.isExistLocalCity())
		{
			sendUpdateInfoToLauncher();
		}
		
		pageViewsList.get(index).setIndex(index);
		
		if(index==curPosition&&!isFromDb)
		{
			if(totalY==TOTAL_Y)
			{
				playRefreshBackAnim(totalY/2, true);
			}else{
				pageViewsList.get(index).onStateChanged(IWeatherView.NORMAL_SHOWING);
				startLoadMoreViewThread();
			}
		}else{
		    pageViewsList.get(index).onStateChanged(IWeatherView.NORMAL_SHOWING);
		}
	}
	
	/**
	 * 更新最新数据
	 */
	private void updateData( int index ) {
		if(index>pageViewsList.size()-1)
			return;
		upDateTime = System.currentTimeMillis();
		if ( index == curPosition ) {
			mCityOptionView.setIndex(curPosition);
			mCityOptionView.onStateChanged(IWeatherView.UPDATING);
			updateWeatherAnim();
		}
		
		if(index==0&&mCityListShowData.isExistLocalCity())
		{
			sendUpdateInfoToLauncher();
		}
		
		pageViewsList.get(index).setIndex(index);
		pageViewsList.get(index).onStateChanged(IWeatherView.UPDATING);
		
	}
	
	public void normalshowData( int curIndex ) {
		if(curIndex>pageViewsList.size()-1)
			return;
		if ( curIndex == curPosition ) {
			mCityOptionView.setIndex(curPosition);
			mCityOptionView.onStateChanged(IWeatherView.NORMAL_SHOWING);
			updateWeatherAnim();
			if(mCityListShowData.isExistLocalCity())
			{
			  sendUpdateInfoToLauncher();
			}
		}
		pageViewsList.get(curIndex).setIndex(curIndex);
		pageViewsList.get(curIndex).onStateChanged(IWeatherView.NORMAL_SHOWING);
		
	}
	
	private void sendUpdateInfoToLauncher( ) {
		CityListShowItem item=mCityListShowData.getWeatherDateItem(0);
		int curTemp=0;
		try {
			curTemp = Integer.parseInt(item.getCurTemp());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Calendar cal = Calendar.getInstance();
		Log.e("jadon", "发送广播到lanuch curTemp="+curTemp + "          time ="+(cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)));
		String weatherType = item.getWeahterType();
		Intent intent = new Intent(UPDATE_BROADCAST_ACTION);
		intent.putExtra("curTemp", curTemp);
		intent.putExtra("weatherType", weatherType);
		intent.putExtra("cityname", item.getCityName());
		mContext.sendBroadcast(intent);

		String airQualityDesc = item.getAirQualityDesc();
		int index = 0;
		if (!TextUtils.isEmpty(airQualityDesc) && (index = airQualityDesc.indexOf(":")) != -1 
				&& (index + 2) < airQualityDesc.length()) {
			airQualityDesc = airQualityDesc.substring(index + 2);
		}
		String title = getString(R.string.desk_tip_content, weatherType, curTemp, airQualityDesc);
		DynamicDeskIconService.insertResolver(mContext.getContentResolver(), title);
	}
	
	private void updateWeatherAnim()
	{
		if(mRootView.getVisibility()==View.VISIBLE&&(!((AuroraWeatherMain)mContext).isCityListVisible()))
		{
			mWeatherView.setIndex(curPosition);
		}
	}
	private final static int FIRST_SHOW_PAGE_NUM=1;
	private void createWeatherMainView(int count,boolean isInit){
		for(int i=0;i<count;i++)
		{
			WeatherMainView weatherMainView = (WeatherMainView)mInflater.inflate(R.layout.viewpagerview, null);
			if(mCityListShowData.getCitySize()!=0)
			{
				weatherMainView.setIndex(pageViewsList.size()+i);
				weatherMainView.onStateChanged(IWeatherView.NORMAL_SHOWING);
			}
			if(isInit)
			{
				tempViews.add(weatherMainView);
			}else{
			    pageViewsList.add(weatherMainView);
			}
		}
	}
	
	private List<WeatherMainView> tempViews=new ArrayList<WeatherMainView>();
	private void addFirstView( ) {
		int pageNumber = mCityListShowData.getDatas().size() > 0 ?mCityListShowData.getDatas().size() : 1;
		createWeatherMainView(FIRST_SHOW_PAGE_NUM,false);
		mCurCityNumber = pageNumber;
	}
	
	private static final int ADD_MORE_VIEW_OK=1;
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ADD_MORE_VIEW_OK:
				pageViewsList.addAll(tempViews);
				tempViews.clear();
				mOptionPageAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};
	
	private boolean isHashLoadMore=false;
	public void initMainFragment(){
	}
	private void addMoreView(){
		if(!isHashLoadMore)
		{
			createWeatherMainView(mCurCityNumber-FIRST_SHOW_PAGE_NUM,true);
			mHandler.sendEmptyMessage(ADD_MORE_VIEW_OK);
			isHashLoadMore=true;
		}
	}
	
	public void updateWeatherBackground( ) {
		updateWeatherBackground(curPosition);
	}
	
	private void updateWeatherBackground( int index ) {
		if(mCityListShowData.getWeatherDateItem(index)==null)
			return;
		mWeatherBackView.setBackgroundResource(mCityListShowData.getWeatherDateItem(index).getBigcirBgRes());
	}
	
	public CityOptionView getCityOptionView( ) {
		return mCityOptionView;
	}
	
    private AnimatorListener rootViewAnimatorListener=new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (animation==rootViewAnimatorIn) {
				mRootView.setVisibility(View.VISIBLE);
				playCommingInAnim( );
				mWeatherView.setIndex(curPosition);
			}else{
				playCommingOutAnim( );
			}
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if (animation==rootViewAnimatorOut) {
				mRootView.setVisibility(View.INVISIBLE);
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
		}
	};
	private boolean isPageViewVerticalScrolling = false;
	private boolean isPageViewHorizontalScrolling = false;
	//操作ViewPager回调
	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (arg0 != 0) {
			isPageViewHorizontalScrolling = true;
		} else {
			isPageViewHorizontalScrolling = false;
		}

		// TODO Auto-generated method stub
		if (pageFlag == true) {
			pageFlag = false;
			long currentTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		changeManViewAlpha(arg1);
	}

	@SuppressLint("NewApi")
	private void changeManViewAlpha(float percent){
		if(Math.abs(percent-0.5)<0.25)
			return;
		if(percent>0.5f)
		{
			mWeatherBackView.setAlpha(percent);
			mWeatherView.setAlphaFactory(percent);
		}else{
			mWeatherBackView.setAlpha(1-percent);
			mWeatherView.setAlphaFactory(1-percent);
		}
	}
	
	@Override
	public void onPageSelected(int position) {
		pageFlag = true;
		prePosition=curPosition;
		curPosition = position;
		
		((AuroraWeatherMain)mContext).setSelectCityListCurrentIndex(curPosition);
		
		mCircleIndexView.changeCircleState(curPosition);
		
		mCityOptionView.setIndex(curPosition);
		mCityOptionView.onStateChanged(IWeatherView.NORMAL_SHOWING);
		updateWeatherAnim();
		updateWeatherBackground(curPosition);
		mCityListShowData.setCurrentPageIndex(position);
		pageViewsList.get(curPosition).onStateChanged(IWeatherView.CURRENT_PAGE_SELECT);
		pageViewsList.get(prePosition).onStateChanged(IWeatherView.CURRENT_PAGE_SELECT);
	}

	// 对ViewPager的Touch事件进行监听，执行刷新操作 start
	private int DownY;
	private int DownX;
	private int preY;
	private int totalY = 0;
	private WeatherDetailsView mWeatherDetailsView;
	private CityDateView mCityDateView;
	private RelativeLayout mRefreshRela;
	private ImageView mRefreshImage;
	private TextView mRefreshText;
	private AnimationDrawable mRefreshDrawable;
	private int TOTAL_Y;
	private static final int START_DISTANCE = 30;
	private boolean mIsInLoading;
	
	private boolean isPinch = false;
	private boolean isAfterDownMove = true;
	private float mBeforeLenght = 0;

	private boolean hasLoadAllViews=false;
	
	private boolean isAllInitOK=false;
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!isAllInitOK&&(!isHashLoadMore||!((AuroraWeatherMain)mContext).isCityListFragmentIntiOk()))
		{
			return true;
		}
		isAllInitOK=true;
		if(mCityListShowData.getCitySize()==0)
		{
			return true;
		}
		
		if (isPageViewHorizontalScrolling) {
			return false;
		}
		
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isAfterDownMove = true;
			isPinch = false;
			viewPagerDownEvent(x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			if ( event.getPointerCount() == 2 && totalY == 0 ) {
				viewPagerMoveEventForPinch(event);
			} else if ( isAfterDownMove ) {
				viewPagerMoveEvent(x, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			isAfterDownMove = true;
			isPinch = false;
			viewPagerUpEvent();
			break;

		default:
			break;
		}

		return isPageViewVerticalScrolling;
	}
	
	/** 
	 * 计算两点间的距离 
	 */  
	private float spacing(MotionEvent event) {  
		float x = event.getX(0) - event.getX(1);  
		float y = event.getY(0) - event.getY(1);  
		return FloatMath.sqrt(x * x + y * y);  
	}  

	private void viewPagerDownEvent(int x, int y) {

		mWeatherDetailsView = pageViewsList.get(curPosition).getWeatherDetailsView();
		preY = y + START_DISTANCE;
		DownY = y;
		DownX = x;
		
		checkCurrentTime( );
	}
	
	/**
	 * 	一、小太阳位置固定，不转动，下拉不松手时显示上一次刷新时间，时间显示逻辑如下：
		1.数据时间与下拉时间相差3分钟内显示：刚刚更新
		2.数据时间与下拉时间相差3分钟以上1小时内显示：X分钟前更新
		3.数据时间与下拉时间相差1小时以上24消失内显示:X小时前更新
		4.数据时间与下拉时间相差1小时以上24消失内显示：XX-XX更新 例如：12-01更新
		二、小太阳位置固定，下拉松手时，小太阳转动，显示内容：正在刷新...
		三、刷新完成拿到数据后，更新数据。
	 */
	private void checkCurrentTime( ) {
		if(isRefreshing)
			return;
		if ( upDateTime == 0 ) {
			mRefreshText.setText(R.string.neverupdate);
			return;
		}
		
		String showText;
        long delta = System.currentTimeMillis() - upDateTime >= 0 ? System.currentTimeMillis() - upDateTime : 0;
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;
        
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH) + 1;

        if ( days == 0 ) {
        	if ( hours == 0 ) {
        		if( minutes <= 3 ) {
        			mRefreshText.setText(R.string.refresh_justnow);
        		} else {
        			showText = mContext.getResources().getString(R.string.refresh_before_minute);
        			mRefreshText.setText(minutes + showText);
        		}
        	} else {
        		showText = mContext.getResources().getString(R.string.refresh_before_hour);
    			mRefreshText.setText(hours + showText);
        	}
        } else {
    		showText = mContext.getResources().getString(R.string.refresh_at_date);
			mRefreshText.setText(month + "月" + day + "日" + showText);
        }
	}
	
	private void viewPagerMoveEventForPinch( MotionEvent event ) {
		
		float length = spacing(event);
		int MIN_DISTANCE = DensityUtil.dip2px(mContext, 22);
		
		if ( isAfterDownMove ) {
			mBeforeLenght = length;
			if ( mBeforeLenght > MIN_DISTANCE ) {
				isPinch = true;
			}
			isAfterDownMove = false;
		} else if ( isPinch && length > 10f ) {
			
			float gapLenght = length - mBeforeLenght;  
			if (gapLenght < -MIN_DISTANCE) {  
				if(!isAnimatorRunning() && mRootView.getVisibility() == View.VISIBLE && mCityListShowData.getDatas().size()> 0)
				{
					((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.CITYLIST_FRAGMENT_INDEX,true);
				}
			}   
		}
	}

	private boolean isRefreshing=false;
	
	private void viewPagerMoveEvent(int x, int y) {
		
		if (y - DownY > START_DISTANCE && (y - DownY > Math.abs(x - DownX))) {
			totalY += y - preY;
			preY = y;

			if (totalY > TOTAL_Y) {
				totalY = TOTAL_Y;
			} else if (totalY < 0) {
				totalY = 0;
			}

			if(mWeatherDetailsView==null)
			{
				mWeatherDetailsView=pageViewsList.get(curPosition).getWeatherDetailsView();
			}
			mWeatherDetailsView.setTranslationY(totalY / 2);
			mCityDateView.setTranslationY(totalY / 2);
			isPageViewVerticalScrolling = true;
			mRefreshRela.setAlpha((float) totalY / TOTAL_Y);
			mRefreshRela.setTranslationY((totalY - TOTAL_Y)/2);
		}
	}
	private boolean isDropDownRefreshFinished = true;
	private void viewPagerUpEvent() {
		if (!isPageViewVerticalScrolling) {
			return;
		}
		if (totalY >= TOTAL_Y) {
			// 处理刷新事件
			if ( SystemUtils.hasActiveNetwork(mContext) ) {
				mRefreshText.setText(R.string.refreshing);
			}
			mRefreshDrawable.stop();
			mRefreshDrawable.start();
			long tempTime = System.currentTimeMillis() - upDateTime;
			//Log.e("111111", "----tempTime = ----" + tempTime + ", isDropDownRefreshFinished = " + isDropDownRefreshFinished);
			if ( (isDropDownRefreshFinished && tempTime >= DROPDOWN_UPDATE_TIME_INTERVAL) || !AuroraWeatherMain.mHasNetWork) {
				refreshAllWeatherData();
				isDropDownRefreshFinished = false;
			} else {
				playRefreshBackAnim( totalY/2 ,true);
			}
			AddCountHelp.addCount(AddCountHelp.SELF_REFRESH, mContext);
		} else {
			playRefreshBackAnim( totalY/2 ,false);
		}
		isPageViewVerticalScrolling = false;
	}
	
	private void playRefreshAnim( ) {
		totalY = TOTAL_Y;
		playRefreshComeAnim(totalY/2);
	}

	/**
	 * 刷新生效开始获取最新数据
	 */
	private void refreshWeatherData(int index) {
		AuroraWeatherMain.mIsNeedNoNetworkToast = true;
		if (mCityListShowData.getDatas().size() == 0 ) {
			((AuroraWeatherMain)mContext).getLocalCity(false);
		} else {
			((AuroraWeatherMain)mContext).requestWeatherInfo(mCityListShowData.getWeatherDateItem(index).getCityId(), false);
		}
	}
	
	private void refreshAllWeatherData( ) {
		if (mCityListShowData.getDatas().size() == 0 ) {
			return;
		}
		isRefreshing=true;
		((AuroraWeatherMain)mContext).requesAllCitysData(false);
	}
	private static final long DROPDOWN_UPDATE_TIME_INTERVAL = 2 * 60 * 1000;
	private void playRefreshBackAnim(final int translationY,final boolean needUpdate) {
		if ( translationY == 0 ) {
			return;
		}
		mRefreshDrawable.stop();
		
		totalY = 0;
		
		if(pageViewsList.size() <= curPosition)
		{
			return;
		}
		mWeatherDetailsView = pageViewsList.get(curPosition).getWeatherDetailsView();
		
		ObjectAnimator translate = ObjectAnimator.ofFloat(mWeatherDetailsView,
				"TranslationY", translationY, 0);
		ObjectAnimator translate1 = ObjectAnimator.ofFloat(mCityDateView,
				"TranslationY", translationY, 0);

		translate.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				Float distance = (Float) animation.getAnimatedValue();
				mRefreshRela.setAlpha((float) 2 * distance / TOTAL_Y);
				mRefreshRela.setTranslationY(distance - TOTAL_Y/2);
			}
		});
		translate.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				    if(needUpdate)
				    {
					   updateData(curPosition);
					   new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							isDropDownRefreshFinished=true;
						}
					}, 400);
				    }
					if(!hasLoadAllViews)
					{
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								startLoadMoreViewThread();
							}
						}, 400);
					}
					
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
		animator.play(translate).with(translate1);
		animator.start();
	}

	public void needUpdateData(){
		playRefreshAnim();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				refreshAllWeatherData();
			}
		}, 400);
		
	}
	
	@SuppressLint("NewApi")
	private void playRefreshComeAnim(final int translationY) {
		mWeatherDetailsView = pageViewsList.get(curPosition).getWeatherDetailsView();
		ObjectAnimator translate = ObjectAnimator.ofFloat(mWeatherDetailsView,
				"TranslationY", 0, translationY);
		ObjectAnimator translate1 = ObjectAnimator.ofFloat(mCityDateView,
				"TranslationY", 0, translationY);

		translate.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				Float distance = (Float) animation.getAnimatedValue();
				mRefreshRela.setAlpha((float) 2 * distance / TOTAL_Y);
				mRefreshRela.setTranslationY(distance - TOTAL_Y/2);
			}
		});
		translate.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				mRefreshDrawable.stop();
				mRefreshDrawable.start();
			}
			@SuppressLint("NewApi")
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
		animator.play(translate).with(translate1);
		animator.start();
	}
	public void locateError(){
		if(mCityListShowData.getCitySize()==0)
		{
			if(totalY==TOTAL_Y)
			{
				playRefreshBackAnim(totalY/2, false);
			}
			switchOptionView.setImageResource(R.drawable.add_optionviewsrc_selector);
		}
		
	}
	private Thread addMoreViewThread =new Thread(){
		public void run() {
			Looper.prepare();
			addMoreView();
		};
	};
}
