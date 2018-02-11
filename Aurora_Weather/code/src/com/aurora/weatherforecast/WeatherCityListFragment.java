package com.aurora.weatherforecast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import totalcount.AddCountHelp;
import views.HoursContents;
import views.PinchRelativeLayout;
import views.PinchRelativeLayout.IPinchDoListener;
import views.WheatherBottomContainer;
import adapters.CityListAdapter;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraAbsActionBar;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraListView.AuroraHandleOnTouchEvnetSelf;

import com.aurora.utils.DensityUtil;
import com.aurora.utils.FontUtils;
import com.aurora.weatherforecast.AuroraWeatherSearchCity;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.CityListShowData.INotifyListener;
import datas.CityListShowItem;
import datas.WeatherCityInfo;
import datas.WeatherData;
import datas.WeatherDataEveryDay;
import datas.WeatherHourInfo;
import android.widget.AdapterView;
@SuppressLint("NewApi")
public class WeatherCityListFragment extends Fragment implements
		AnimatorListener {
	private String TAG = "VIEW_LOG_CAT";
	private AuroraListView citiesListView;
	private AuroraActionBar titleBar;
	private Context mContext;
	
	private View mRootView;
	
	private LayoutInflater inflater;
	
	private CityListAdapter citiesAdapter;
	
	private AnimatorSet enterAnimatorSet,exitAnimatorSet;
	
	private List<Animator> enterAnimators = new ArrayList<Animator>();
	
	private float scaleOneValue=0.5f,scaleTwoValue=1.04f,scaleThreeValue=1f, alphaFromValue=0f, alphaToValue=1f;
	
	private int currentSelectCityItem = 0;
	
	private ImageView bigCircle;
	
	private Animator bigCircleEnterAnimator, bigCircleExitAnimator;
	
	// 动画持续时间
	private final int DURATION = 380;
	
	private final int ENTER_ANIMATOR_TYPE = 0;
	private final int EXIT_ANIMATOR_TYPE = 1;
	
	private ObjectAnimator titleBarEnterAnimator,titleBarExitAnimator;
	private HashMap<Integer, View> citiesItemViewMap=new HashMap<Integer, View>();
	
	private List<CityListShowItem> datas=new ArrayList<CityListShowItem>();
	
	public void setSelectCityIndex(int index){
		if(!isInitOk||index==currentSelectCityItem)
			return;
		this.currentSelectCityItem=index;
		setBigCircleIconResId();
		setCitiesSelection();
	}
	
	private boolean setCitiesSelection(){
		int citiesFirstVisibleItem=citiesListView.getFirstVisiblePosition();
		int citiesLastVisibleItem=citiesListView.getLastVisiblePosition();
		if(currentSelectCityItem<citiesFirstVisibleItem||currentSelectCityItem>citiesLastVisibleItem)
		{
           citiesListView.setSelection(currentSelectCityItem);
           return true;
		}else{
		int[] location=new int[2];
		if(citiesItemViewMap.get(citiesLastVisibleItem)!=null)
		{
			citiesItemViewMap.get(citiesLastVisibleItem).getLocationInWindow(location);
			boolean isBottomOut=location[1]+CITYLIST_ITEM_HEIGHT>SCREENT_HEIGHT;
			if((citiesFirstVisibleItem==currentSelectCityItem&&citiesListView.getChildAt(0).getTop()<0)||
					(citiesLastVisibleItem==currentSelectCityItem&&isBottomOut))
			{
				citiesListView.setSelection(currentSelectCityItem);
				return true;
			}
		}
		}
		return false;
	}
	
	private float CITYLIST_ITEM_HEIGHT;
	private int AURORA_ACTIONBAR_HEIGHT;
	private float BIGCIRCLE_X_LOCATION;
	private int DIVIDE_HEIGHT_PX;
	private float bigcleYLocation;
	private float CITYLIST_ITEM_PADDING_TOP;
	
	private int SCREENT_HEIGHT;
	private void initConstHeight(){
		AURORA_ACTIONBAR_HEIGHT=(int) mContext.getResources().getDimension(com.aurora.R.dimen.aurora_action_bar_height);
		CITYLIST_ITEM_HEIGHT=mContext.getResources().getDimension(R.dimen.citylist_item_height);
		BIGCIRCLE_X_LOCATION=mContext.getResources().getDisplayMetrics().widthPixels-mContext.getResources().getDimension(R.dimen.citylist_item_icon_width)-mContext.getResources().getDimension(R.dimen.citylist_item_padding_left_right);
        CITYLIST_ITEM_PADDING_TOP=(int)(CITYLIST_ITEM_HEIGHT-mContext.getResources().getDimension(R.dimen.citylist_item_icon_width))/2;
        DIVIDE_HEIGHT_PX=(int)mContext.getResources().getDimension(R.dimen.citylist_divide_height)+1;
        SCREENT_HEIGHT=mContext.getResources().getDisplayMetrics().heightPixels;
	}
	
	private void calculateBigCircleY(){
		int firstVisibleItem=citiesListView.getFirstVisiblePosition();
		int delCount=currentSelectCityItem-firstVisibleItem;
		float topY=citiesListView.getChildAt(0)==null?0:citiesListView.getChildAt(0).getTop();
		AURORA_ACTIONBAR_HEIGHT=titleBar.getMeasuredHeight();
		bigcleYLocation= (AURORA_ACTIONBAR_HEIGHT+delCount*DIVIDE_HEIGHT_PX+delCount*(CITYLIST_ITEM_HEIGHT)+CITYLIST_ITEM_PADDING_TOP-Math.abs(topY));
	}
	
	private static final int SET_BIG_RES=2;
	private void setBigCircleIconResId(){
		   if(currentSelectCityItem>mCityListShowData.getCitySize()-1)
		   {
			   return;
		   }
		   CityListShowItem cityListShowItem= mCityListShowData.getDatas().get(currentSelectCityItem);
		   if(cityListShowItem!=null)
		   {
		     Message msg=new Message();
		     msg.what=SET_BIG_RES;
		     msg.arg1=cityListShowItem.getBigcirBgRes();
		     mHandler.sendMessage(msg);
		   }
	}
	
	private ImageView getCircleImageView(int position){
		if(citiesItemViewMap.containsKey(position)&&citiesItemViewMap.get(position)!=null)
		{
			 return (ImageView)citiesItemViewMap.get(position).findViewById(R.id.item_city_list_city_right_circle_view);
		}
		return null;
	}
	
	private void initAnimatorValue(){
		for(int i=0;i<citiesItemViewMap.size();i++)
		{
			View view=citiesItemViewMap.get(i);
			view.findViewById(R.id.item_city_content_container).setAlpha(alphaFromValue);
			
			ImageView cirView=(ImageView)view.findViewById(R.id.item_city_list_city_right_circle_view);
			
			cirView.setScaleX(scaleOneValue);
			
			cirView.setScaleY(scaleOneValue);
			cirView.setAlpha(alphaFromValue);
		}
	}
	
	private void setTitleBarAlphaAnimator(){
//		titleBar.setAlpha(0);
		titleBarEnterAnimator=ObjectAnimator.ofFloat(titleBar, "alpha", 0f,1).setDuration(DURATION);
		titleBarExitAnimator=ObjectAnimator.ofFloat(titleBar, "alpha", 1f,0f).setDuration(DURATION);
	}
	
	private int ITEM_ANIMATOR_DURATION=220;
	private final float RATE=(float) (0.58*186/ITEM_ANIMATOR_DURATION);
	private void setBigCircleLocation(boolean isEnter){
		tempCircleView=getCircleImageView(currentSelectCityItem);
		if (tempCircleView != null) 
		{
			bigCircle.setX(BIGCIRCLE_X_LOCATION);
			if(isEnter)
			{
				calculateBigCircleY();
				bigCircle.setY(bigcleYLocation);
			}else{
				RelativeLayout parent = (RelativeLayout) tempCircleView.getParent();
				int[] location = new int[2];
				parent.getLocationInWindow(location);
				float y = location[1] + tempCircleView.getY();
				bigCircle.setY(y);
			}
		}
	}
	
	private final int DELAY_TIME=35;
	private AccelerateDecelerateInterpolator mDefineInterpolator=new AccelerateDecelerateInterpolator();
	/**
	 * 创建alpha动画
	 * 
	 * @param view
	 * @return
	 */
	private Animator createCitiesItemTextContentAlphaAniamator(int position) {
		View parent = citiesItemViewMap.get(position);
		return ObjectAnimator.ofFloat(parent.findViewById(R.id.item_city_content_container),
				"alpha", alphaFromValue, alphaToValue);
	}

	private ImageView tempCircleView;
	private void startEnterAnimator() {
	     setEnterAnimators();
		 bigCircleEnterAnimator.start();
	}
	/**
	 * 初始化入场动画
	 */
	private void setEnterAnimators() {
		citiesListView.setAlpha(1);
		enterAnimators.clear();
		initAnimatorValue();
		setEnterListItemAnimator();
		setBigCircleIconResId();
		setBigCircleLocation(true);
	}

	
	private void setEnterListItemAnimator(){
		createEnterSelectItemAnimator();
		createEnterItemDetailAnimator();
		createEnterItemAnimatorTimeSequence();
	}
	
	private void createEnterSelectItemAnimator(){
		if (getCircleImageView(currentSelectCityItem) != null) {
			AnimatorSet set = new AnimatorSet();
			tempCircleView = getCircleImageView(currentSelectCityItem);
			set.playTogether(createEnterScaleAndAlphaAnimaor(tempCircleView),createCitiesItemTextContentAlphaAniamator(currentSelectCityItem));
			set.setDuration(ITEM_ANIMATOR_DURATION);
			enterAnimators.add(set);
		}
	}
	
	private void createEnterItemDetailAnimator(){
		int listFirstIndex=0,listLastIndex=citiesItemViewMap.size()-1;
		int min = currentSelectCityItem - 1;
		int max = currentSelectCityItem + 1;
		while (min >= listFirstIndex && max <=listLastIndex) {
			Animator[] animators = new Animator[4];
			AnimatorSet set = new AnimatorSet();
			tempCircleView = getCircleImageView(min);
			if (tempCircleView != null) {
				animators[0] = createEnterScaleAndAlphaAnimaor(tempCircleView);
				animators[1] = createCitiesItemTextContentAlphaAniamator(min);
			}
			tempCircleView = getCircleImageView(max);
			if (tempCircleView != null) {
				animators[2] = createEnterScaleAndAlphaAnimaor(tempCircleView);
				animators[3] = createCitiesItemTextContentAlphaAniamator(max);
			}
			set.playTogether(animators);
			set.setDuration(ITEM_ANIMATOR_DURATION);
			enterAnimators.add(set);
			min--;
			max++;
		}
		if (min >= listFirstIndex) {
			for (int i = min; i >= listFirstIndex; i--) {
				tempCircleView = getCircleImageView(i);
				if (tempCircleView != null) {
					AnimatorSet set = new AnimatorSet();
					set.playTogether(createEnterScaleAndAlphaAnimaor(tempCircleView),createCitiesItemTextContentAlphaAniamator(i));
					set.setDuration(ITEM_ANIMATOR_DURATION);
					enterAnimators.add(set);
				}
			}
		}
		if (max <=listLastIndex) {
			for (int i = max; i <=listLastIndex; i++) {
				tempCircleView = getCircleImageView(i);
				if (tempCircleView != null) {
					AnimatorSet set = new AnimatorSet();
					set.playTogether(createEnterScaleAndAlphaAnimaor(tempCircleView),createCitiesItemTextContentAlphaAniamator(i));
					set.setDuration(ITEM_ANIMATOR_DURATION);
					enterAnimators.add(set);
				}
			}
		}
	}
	
	private void createEnterItemAnimatorTimeSequence() {
		for (int i = 0; i < enterAnimators.size(); i++) {
			enterAnimators.get(i).setStartDelay(i * DELAY_TIME);
			enterAnimators.get(i).setInterpolator(mDefineInterpolator);
		}
		enterAnimatorSet = new AnimatorSet();
		enterAnimatorSet.playTogether(enterAnimators);
		enterAnimatorSet.addListener(this);
	}
	
	private Animator createEnterScaleAndAlphaAnimaor(View target){
		AnimatorSet set = new AnimatorSet();
		Animator animatorStep1 = createMulityPropertiesAnimator(target,new String[] { "scaleX", "scaleY" }, scaleOneValue,scaleTwoValue, scaleOneValue, scaleTwoValue);
		Animator animatorStep2 = createMulityPropertiesAnimator(target,new String[] { "scaleX", "scaleY" }, scaleTwoValue,scaleThreeValue, scaleTwoValue, scaleThreeValue);
		set.playSequentially(animatorStep1, animatorStep2);
		Animator alphaAnimator = null;
		alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", 0, 1);
		AnimatorSet retSet = new AnimatorSet();
		retSet.playTogether(set, alphaAnimator);
		return retSet;
	}
	
	private Animator cityListAlphaAnimator;
	
	private void setExitAnimators(){
		exitAnimatorSet=new AnimatorSet();
		exitAnimatorSet.addListener(this);
		exitAnimatorSet.setDuration(EXIT_DURATION);
		exitAnimatorSet.playTogether(titleBarExitAnimator,cityListAlphaAnimator,rootViewExitAnimator,bigCircleExitAnimator);
	}
	
	private boolean isInitOk=false;
	private Thread initThread=new Thread(){
		public void run() {
			initView();
		};
	};
	
	public boolean isInitOk(){
		return isInitOk;
	}
	
	/**
	 * 初始化出场动画
	 */
	private void setExitAnimatorsLocation() {
		setBigCircleIconResId();
		setBigCircleLocation(false);
	}

	
	private void startExitAnimator() {
		 if(exitAnimatorSet!=null&&exitAnimatorSet.isRunning())
		    return;
		 setExitAnimatorsLocation();
		 isRun=false;
		 exitAnimatorSet.start();
	}

	private CityListShowData mCityListShowData;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.weather_citylist_fragment, null);
		mCityListShowData=CityListShowData.getInstance(mContext);
		bigCircle = (ImageView) mRootView.findViewById(R.id.cv_big_circle);
		titleBar=(AuroraActionBar)mRootView.findViewById(R.id.bar);
		titleBar.addItem(aurora.widget.AuroraActionBarItem.Type.Add);
		titleBar.setTitle(R.string.tv_city_list_fragment_title);
		mRootView.setAlpha(0);
		mRootView.setVisibility(View.INVISIBLE);
		((PinchRelativeLayout)mRootView).setIPinchDoListener(new IPinchDoListener() {
			
			@Override
			public void exit() {
				if(!isAnimatorRunning())
				{
					showMainFragment();
				}
			}
			@Override
			public void enter() {
				
			}
		});
		return mRootView;
	}
	
	private boolean initTheadHasStart=false;
	public void initCityListFragment(){
		if(!initTheadHasStart)
		{
		  initTheadHasStart=true;
		  initThread.start();
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
		inflater = LayoutInflater.from(mContext);
	}
	
	private ObjectAnimator rootViewEnterAnimator,rootViewExitAnimator;
	private final int ENTER_DURAION=650,EXIT_DURATION=500;
	private void setRootViewAlphaAnimator(){
		rootViewEnterAnimator=ObjectAnimator.ofFloat(mRootView, "alpha", 0,1).setDuration(ENTER_DURAION);
		rootViewExitAnimator=ObjectAnimator.ofFloat(mRootView, "alpha", 1,0).setDuration(EXIT_DURATION);
		rootViewEnterAnimator.addListener(rootViewAnimatorListener);
		rootViewExitAnimator.addListener(rootViewAnimatorListener);
	}
	
	private AnimatorListener rootViewAnimatorListener=new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			if (animation==rootViewEnterAnimator) {
				mRootView.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if(animation==rootViewExitAnimator)
			{
				mRootView.setVisibility(View.INVISIBLE);
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
		}
	};
	private void showNotify(){
		if(mCityListShowData.isExistLocalCity())
		{
		   setLocalCityDate();
		}
		citiesAdapter.notifyDataSetChanged();
	}

	public void show(boolean isRunAnimation) {
		isToNextPage=false;
		isClickBackBtn=false;
		if (isRunAnimation) {
			if (rootViewEnterAnimator != null) {
				rootViewEnterAnimator.start();
				startEnterAnimator();
			}
		} else {
			mRootView.setVisibility(View.VISIBLE);
			mRootView.setAlpha(1);
		}
	}
	
	public void hide(boolean isRunAnimation) {
		if (isRunAnimation) {
			startExitAnimator();
		} else {
			mRootView.setVisibility(View.INVISIBLE);
			mRootView.setAlpha(0);
		}
	}
	
	public boolean isShowing(){
		return mRootView.getVisibility()==View.VISIBLE;
	}
	
	/**
	 * 页面初始化
	 */
	private void initView() {
		setRootViewAlphaAnimator();
		initConstHeight();
		initTitleBar();

		setBigCircleAnimator();

		initCitiesListView();

		setExitAnimators();
		
	}
	
	private boolean isToNextPage=false;
	private boolean isClickBackBtn=false;
	@Override
	public void onResume() {
		super.onResume();
		isToNextPage=false;
		isClickBackBtn=false;
	}
	
	private boolean isCanBack(){
		if(citiesListView.auroraIsRubbishOut())
		{
			citiesListView.auroraSetRubbishBack();
			return false;
		}
		return !isAnimatorRunning()&&!isToNextPage&&!isClickBackBtn;
	}
	private void initTitleBar(){
		
        titleBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {
			@Override
			public void onAuroraActionBarBackItemClicked(int arg0) {
				if(arg0==OnAuroraActionBarBackItemClickListener.HOME_ITEM)
				{
					if (isCanBack()) {
						showMainFragment();
						isClickBackBtn=true;
					}
				}
			}
		});
		titleBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
			@Override
			public void onAuroraActionBarItemClicked(int arg0) {
				if(mCityListShowData.getCitySize()==CityListShowData.MAX_CITY_NUMBER)
				{
					Toast.makeText(mContext, R.string.tv_city_add_already_full, 500).show();
				}else{
					if (!isClickBackBtn) {
						if (arg0 == 0) {
							isToNextPage=true;
							Intent intent = new Intent(mContext,
									AuroraWeatherSearchCity.class);
							startActivityForResult(intent, REQUEST_CODE);
						}
					}
				}
			}
		});
		
		setTitleBarAlphaAnimator();
	}
	
	private final int REQUEST_CODE=12;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_CODE&&resultCode==AuroraWeatherSearchCity.RESULTCODE)
		{
			((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.MAIN_FRAGMENT_INDEX,false);
			mRootView.setVisibility(View.INVISIBLE);
		}
	}
	 private View sortView;
	 
	/**
	 * 
	 */
	private void initCitiesListView()
	{
		citiesListView = (AuroraListView) mRootView.findViewById(R.id.mAuroraListView);
		citiesListView.setmSuspendBitmapBg(BitmapFactory.decodeResource(getResources(), R.drawable.long_click_shadow));
		citiesListView.auroraSetHeaderViewYOffset(DensityUtil.dip2px(mContext, -100));
		 cityListAlphaAnimator=ObjectAnimator.ofFloat(citiesListView, "alpha", 1,0).setDuration(ITEM_ANIMATOR_DURATION);
		//slide left
		citiesListView.auroraSetNeedSlideDelete(true);
		//sort item 
		citiesListView.enableSortItem();
		citiesListView.auroraSetOnTouchEventSelf(true);
		citiesListView.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {
			
			@Override
			public void auroraDeleteItem(View child, int arg1) {
				AddCountHelp.addCount(AddCountHelp.DELETE_CITY, mContext);
				// TODO Auto-generated method stub
				mCityListShowData.deleteByIndex(arg1);
				ViewGroup.LayoutParams vlp=child.getLayoutParams();
				if(vlp!=null)
				{
					vlp.height=(int) CITYLIST_ITEM_HEIGHT;
					child.setLayoutParams(vlp);
				}
			}
		});
		
		
		citiesListView.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
			
			@Override
			public void auroraPrepareDraged(int arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void auroraOnClick(int arg0) {
				citiesListView.auroraDeleteSelectedItemAnim();
			}
			
			@Override
			public void auroraDragedUnSuccess(int arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void auroraDragedSuccess(int arg0) {
				// TODO Auto-generated method stub
			}
		});
		citiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			
				if (isCanBack()) {
					isClickBackBtn=true;
					setSelectCityIndex(position);
					mCityListShowData.itemClick(currentSelectCityItem);
					showMainFragment();
					
				}
				
			}
		});
		citiesListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				citiesListView.onSortActionDown();
				return true;
			}
			
		});
		
		citiesListView.auroraSetItemChangedListener(new AuroraListView.AuroraItemChangedListener(){
			@Override
			public void auroraExchangeItem(int src, int dest)
			{
				if(src != dest)
				{
					
					exchangeData(src , dest);
				}
			}
		});
		datas.addAll(mCityListShowData.getDatas());
		citiesAdapter = new CityListAdapter(mContext,citiesItemViewMap,datas);
		if(mCityListShowData.isExistLocalCity())
		{
			addLocalCityView();
			setLocalCityDate();
		}
		//CityListAdapter reply on mCitys !!!
		mHandler.sendEmptyMessage(SET_ADAPTER);
		addNotifyListener();
		mHandler.sendEmptyMessage(INIT_OK);
	}
	
	private static final int SET_ADAPTER=1;
	private static final int INIT_OK=4;
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SET_ADAPTER:
				if(citiesListView.getAdapter()==null)
				{
				   citiesListView.setAdapter(citiesAdapter);
				}
				break;
			case SET_BIG_RES:
				bigCircle.setImageResource(msg.arg1);
				break;
			case INIT_OK:
				isInitOk=true;
				break;
			default:
				break;
			}
		};
		
	};
	
	public boolean isAnimatorRunning(){
		return isEnterAnimatorRunning()||isExitAnimatorRunning();
	}
	
	private boolean isEnterAnimatorRunning(){
		if(rootViewEnterAnimator==null||bigCircleEnterAnimator==null||enterAnimatorSet==null)
		{
			return false;
		}
		return rootViewEnterAnimator.isRunning()||enterAnimatorSet.isRunning()||bigCircleEnterAnimator.isRunning();
	}
	
	private boolean isExitAnimatorRunning(){
		if(exitAnimatorSet==null)
		{
			return false;
		}
		return exitAnimatorSet.isRunning();
	}
	
	
	private void addNotifyListener(){
		mCityListShowData.addINotifyListener(new INotifyListener() {
			@Override
			public void notityDataChange(int... opeType) {
				 if(!isInitOk)
				  {
					  return;
				  }
				switch (opeType[0]) {
				case INotifyListener.OPE_DELETE:
					  if(opeType[0]<=currentSelectCityItem)
					  {
						  currentSelectCityItem--;
					  }
				      citiesItemViewMap.remove(citiesItemViewMap.size()-1);
				      datas.remove(opeType[1]);
				      citiesAdapter.notifyDataSetChanged();
				      break;
				      
				case INotifyListener.OPE_REQUEST_ALL_BACK:
					  if(!isInitOk)
					  {
						  return;
					  }
					  if(isAnimatorRunning())
					  {
						  return;
					  }
					  if(mCityListShowData.isExistLocalCity())
					  {
						  if(localCityHeadView==null)
						  {
						    addLocalCityView();
						    citiesAdapter=new CityListAdapter(mContext, citiesItemViewMap, datas);
						    citiesListView.setAdapter(citiesAdapter);
						  }
					      setLocalCityDate();
					  }
					  datas.clear();
					  datas.addAll(mCityListShowData.getDatas());
					  citiesAdapter.notifyDataSetChanged();
					  break;
					  
				case INotifyListener.OPE_ADD:
					  datas.add(opeType[1], mCityListShowData.getWeatherDateItem(opeType[1]));
					  citiesAdapter.notifyDataSetChanged();
					  break;
				case INotifyListener.OPE_ADD_LOCAL_CITY:
					  if(mCityListShowData.isExistLocalCity())
					  {
						 if(localCityHeadView==null)
						 {
							 addLocalCityView();
							 citiesAdapter=new CityListAdapter(mContext, citiesItemViewMap, datas);
							 citiesListView.setAdapter(citiesAdapter);
						 }
					     setLocalCityDate();
					  }
					  break;
				case INotifyListener.OPE_SWITCH_POSITION:
					  datas.clear();
					  datas.addAll(mCityListShowData.getDatas());
					  citiesAdapter.notifyDataSetChanged();
					  break;
				case INotifyListener.OPE_CITY_THUMB_UPDATE:
					  CityListShowItem cityListShowItem=mCityListShowData.getLocateCityItem();
					  if(cityListShowItem!=null && mCityListShowData.isExistLocalCity() && item_city_list_city_right_circle_view !=null)
					  {
						  item_city_list_city_right_circle_view.setImageResource(cityListShowItem.getResId());
					  }
					  datas.clear();
					  datas.addAll(mCityListShowData.getDatas());
					  citiesAdapter.notifyDataSetChanged();
					  break;
				case INotifyListener.OPE_RESET_LAST_ADD:
					  if(datas.size() == 0)
					  {
						  return;
					  }
					  datas.remove(datas.size()-1);
					  citiesAdapter.notifyDataSetChanged();
					  break;
				}
				
			}
		});
	}
	
	private View localCityHeadView;
	private TextView item_city_list_city_name,item_city_list_city_terperature,tv_temperature;
	private ImageView item_city_list_city_right_circle_view,iv_temperature_point;
	
	private void addLocalCityView(){
		
		localCityHeadView = inflater.inflate(R.layout.headview_city_list, null);
		
		localCityHeadView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					if (isCanBack()) {
						isClickBackBtn=true;
						setSelectCityIndex(0);
						mCityListShowData.itemClick(currentSelectCityItem);
						showMainFragment();
					}
			}
		});
		item_city_list_city_name=(TextView)localCityHeadView.findViewById(R.id.item_city_list_city_name);
		
		item_city_list_city_terperature=(TextView)localCityHeadView.findViewById(R.id.item_city_list_city_terperature);
		
		tv_temperature=(TextView)localCityHeadView.findViewById(R.id.tv_temperature);
		
		item_city_list_city_right_circle_view=(ImageView)localCityHeadView.findViewById(R.id.item_city_list_city_right_circle_view);
		iv_temperature_point=(ImageView)localCityHeadView.findViewById(R.id.iv_temperature_point);
		localCityHeadView.findViewById(R.id.iv_divider).setBackgroundResource(com.aurora.R.drawable.aurora_list_divider_light);
		
		tv_temperature.setTypeface(Typeface.createFromFile(CityListAdapter.TTF_FROMAT));
		
		citiesListView.addHeaderView(localCityHeadView);
		
		citiesItemViewMap.put(LOCAL_CITY_INDEX, localCityHeadView);
	}
	
	private final int LOCAL_CITY_INDEX=0;
	private void setLocalCityDate(){
		CityListShowItem localItem=mCityListShowData.getLocateCityItem();
		if(localItem==null)
		{
			return;
		}
		if (localCityHeadView == null) {
			addLocalCityView();
		}
		item_city_list_city_name.setText(localItem.getCityName());
		String currentTemp=localItem.getCurTemp();
		if(!currentTemp.equals(mContext.getString(R.string.default_temperature)))
		{
			iv_temperature_point.setVisibility(View.VISIBLE);
			tv_temperature.setVisibility(View.VISIBLE);
		}else{
			iv_temperature_point.setVisibility(View.INVISIBLE);
			tv_temperature.setVisibility(View.INVISIBLE);
		}
		tv_temperature.setText(currentTemp);
		if(localItem.getLowAndHighTempStr()!=null&&!localItem.getLowAndHighTempStr().equals(""))
		{
			item_city_list_city_terperature.setText(localItem.getLowAndHighTempStr());
		}
		item_city_list_city_right_circle_view.setImageResource(localItem.getResId());
		if(currentSelectCityItem==0)
		{
			setBigCircleIconResId();
		}
		
	}
	
	private final int BIGCIRCLE_DURATION=400;
	private boolean isRun=false;
	private final float MAX_PX=4464;//大圆动画最大扩大的像素
	private void setBigCircleAnimator()
	{
		
		float circleWidth=mContext.getResources().getDimension(R.dimen.citylist_item_icon_width);
		ITEM_ANIMATOR_DURATION=(int) (0.58*circleWidth/RATE);
		float bigValue = MAX_PX/circleWidth;
		float small = 0f;
		bigCircleEnterAnimator=createMulityPropertiesAnimator(bigCircle, new String[]{"scaleX","scaleY"}, bigValue,small,bigValue,small).setDuration(BIGCIRCLE_DURATION);
		bigCircleEnterAnimator.setDuration(BIGCIRCLE_DURATION);
		bigCircleEnterAnimator.addListener(this);
		bigCircleExitAnimator=createMulityPropertiesAnimator(bigCircle, new String[]{"scaleX","scaleY"},1, bigValue,1,bigValue).setDuration(EXIT_DURATION);
	}
	
	private Animator createMulityPropertiesAnimator(View target,String[] propertiesName,float... values){
		PropertyValuesHolder[] pvhs=new PropertyValuesHolder[propertiesName.length];
		for(int i=0;i<propertiesName.length;i++)
		{
			pvhs[i]=PropertyValuesHolder.ofFloat(propertiesName[i], values[i*2],values[i*2+1]);
		}
		return ObjectAnimator.ofPropertyValuesHolder(target, pvhs);
	}
	
	public void showMainFragment(){
		if(mCityListShowData.getCitySize()==0)
		{
			((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.MAIN_FRAGMENT_INDEX,false);
			return;
		}
		if(citiesListView.auroraIsRubbishOut())
		{
			citiesListView.auroraSetRubbishBack();
			return;
		}
		boolean isSet=setCitiesSelection();
		if(isSet)
		{
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.MAIN_FRAGMENT_INDEX,true);
				}
			}, 100);
		}else{
			((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.MAIN_FRAGMENT_INDEX,true);
		}
		
	}
	
	@Override
	public void onAnimationStart(Animator animation) {
		if(animation==bigCircleEnterAnimator)
		{
			bigCircle.setVisibility(View.VISIBLE);
		}
	}
	

	@Override
	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub
		if(animation==bigCircleEnterAnimator)
		{
			enterAnimatorSet.start();
			titleBarEnterAnimator.start();
		}else if(animation==enterAnimatorSet)
		{
			enterAnimators.clear();
			showNotify();
		}else if(animation==exitAnimatorSet)
		{
			mRootView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
		if (animation == exitAnimatorSet) {
			((AuroraWeatherMain)mContext).setSelection(AuroraWeatherMain.MAIN_FRAGMENT_INDEX,true);
		}else if(animation==bigCircleEnterAnimator)
		{
			enterAnimatorSet.start();
			titleBarEnterAnimator.start();
		}
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub

	}
	
	private void exchangeData(int src , int dest)
	{
		AddCountHelp.addCount(AddCountHelp.SORT_CITY, mContext);
		mCityListShowData.switchPosition(src, dest);
	}
}
