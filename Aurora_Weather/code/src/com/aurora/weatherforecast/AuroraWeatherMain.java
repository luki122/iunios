package com.aurora.weatherforecast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.weatherdata.ManagerThread;
import com.aurora.weatherdata.WeatherManager;
import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherdata.db.CNCityAdapter;
import com.aurora.weatherdata.implement.Command;
import com.aurora.weatherdata.implement.DataResponse;
import com.aurora.weatherdata.interf.INotifiableController;
import com.aurora.weatherdata.interf.INotifiableManager;
import com.aurora.weatherdata.util.CustomeToast;
import com.aurora.weatherdata.util.Globals;
import com.aurora.weatherdata.util.SystemUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import totalcount.AddCountHelp;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import datas.CityListShowData;
import datas.CityListShowItem;
import datas.DynamicDeskIconBrocdcastReceiver;
import datas.DynamicDeskIconService;
import datas.LocationProvider;
import datas.LocationProvider.ILocateResult;
import datas.WeatherAirQualities;
import datas.WeatherData;
import datas.WeatherDataEveryDay;
import datas.WeatherForcastInfo;
import datas.WeatherHourInfo;
import com.aurora.weatherforecast.R;

@SuppressLint("NewApi")
public class AuroraWeatherMain extends AuroraActivity implements INotifiableController {

	private WeatherMainFragment mWeatherMainFragment;
	private WeatherCityListFragment mWeatherCityListFragment;
	private FragmentManager mFragmentManager;
	public static boolean mIsDayTime;
	public static boolean mHasNetWork;
	public static boolean mIsNeedNoNetworkToast = true;
	private boolean mIsFirstIn = true;
	
	// add by likai 2014-10-30 begin
	private WeatherManager mManager;
	private ManagerThread mThread;
	// add by likai 2014-10-30 end
    public static final int MAIN_FRAGMENT_INDEX=0;
    public static final int CITYLIST_FRAGMENT_INDEX=1;
    private int currentFragmentShowIndex=MAIN_FRAGMENT_INDEX;
    
    private TextView mNoNetworkText;
    private RelativeLayout mWeatherMainlayout;
    private boolean mIsNeedReceiveNetworkBroadCast;
    
    private boolean mIsFromDB = false;
	//test
    private String[] weatherInfos;
    private String[] forcastInfos;
    
    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	//监听广播对时间变化做相应处理
        	if (!intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") ) {
        		handleReciver.sendEmptyMessage(UPDATE_WEATHER_ARRRIBUTE);
        	} else {
        		handleReciver.sendEmptyMessage(NETWORK_CHANGED);
        	}
        }
    };
    
    private static final int UPDATE_WEATHER_ARRRIBUTE = 0x987;
    private static final int NETWORK_CHANGED = 0x947;
    
    private Handler handleReciver = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    	   switch (msg.what) {
		case UPDATE_WEATHER_ARRRIBUTE:
			updateWeatherAttribute();
			break;
		case NETWORK_CHANGED:
			doWhenNetWorkChanged(AuroraWeatherMain.this);
			break;
		default:
			break;
		}
    	};
    	
    };
    
    
    
    
    public static final boolean isShowTest = false;
    private CityListShowData mCityListShowData;
    
    private long lastStopTime = 0l;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_aurora_weather_main);
    	mCityListShowData=CityListShowData.getInstance(this);
		mFragmentManager = getFragmentManager();
		initManager();
		initFragment();
		AddCountHelp.addCount(AddCountHelp.ENTER_APP, this);
		if(isShowTest)
		{
			//test
			weatherInfos=getResources().getStringArray(R.array.weather_types);
			forcastInfos=getResources().getStringArray(R.array.forcast_types);
			//test
			initTest();
		}
		lastStopTime = 0l;
//		judgeToWarnPage();
		
	}
	
	public boolean isComeFromNotification = false;
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		judgeToWarnPage();
	}
	
	
	
	private void judgeToWarnPage(){
		Intent intent = getIntent();
		if(intent!= null && intent.getIntExtra(DynamicDeskIconService.COME_FROM_KEY, 0)==DynamicDeskIconService.COME_FROM_NOTIFICATION)
		{
			Intent toIntent = new Intent(this, AuroraWeatherWarnDeatilInfo.class);
			toIntent.putExtra(AuroraWeatherWarnDeatilInfo.SHOW_WARN_INDEX, 0);
			toIntent.putExtra(DynamicDeskIconService.COME_FROM_KEY, DynamicDeskIconService.COME_FROM_NOTIFICATION);
			toIntent.putExtra(DynamicDeskIconService.WEATHER_DATE_KEY, intent.getStringExtra(DynamicDeskIconService.WEATHER_DATE_KEY));
			toIntent.putExtra(DynamicDeskIconService.WEATHER_TYPE, intent.getStringExtra(DynamicDeskIconService.WEATHER_TYPE));
			isComeFromNotification = true;
			startActivity(toIntent);
			overridePendingTransition(R.anim.warn_detail_in, R.anim.main_out);
		}
	}
	
	private final String FIRST_PAGE_CITY_NAME="first_page_city_name";
	private final String FIRST_PAGE_CITY_TEMP="first_page_city_temp";
	private final String FIRST_PAGE_CITY_LOW_HIGH_TEMP="first_page_city_low_high_temp";
	private final String FIRST_PAGE_CITY_WEATHERTYPE = "first_page_city_weather_type";
	private void saveFirstPageDate(){
		SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
		if(mCityListShowData!=null && mCityListShowData.getInstance(this).getCitySize()==0)
		{
			sp.edit().remove(FIRST_PAGE_CITY_NAME).remove(FIRST_PAGE_CITY_TEMP).remove(FIRST_PAGE_CITY_LOW_HIGH_TEMP).remove(FIRST_PAGE_CITY_WEATHERTYPE).commit();
			return;
		}
		CityListShowItem item = mCityListShowData.getLocateCityItem();
		if(item == null)
			return;
		sp.edit().putString(FIRST_PAGE_CITY_NAME, item.getCityName())
		.putString(FIRST_PAGE_CITY_TEMP, item.getCurTemp())
		.putString(FIRST_PAGE_CITY_LOW_HIGH_TEMP, item.getLowAndHighTempStr())
		.putString(FIRST_PAGE_CITY_WEATHERTYPE, item.getWeahterType()).commit();
	}
	
	public String getFirstPageCity(){
		return getPreferences(Context.MODE_PRIVATE).getString(FIRST_PAGE_CITY_NAME, null);
	}
	
	public String getFirstPageCityTemp(){
		return getPreferences(Context.MODE_PRIVATE).getString(FIRST_PAGE_CITY_TEMP, null);
	}
	
	public String getFirstPageCityLowHighTemp(){
		return getPreferences(Context.MODE_PRIVATE).getString(FIRST_PAGE_CITY_LOW_HIGH_TEMP, null);
	}
	
	public String getFirstPageCityWeatherType(){
		return getPreferences(Context.MODE_PRIVATE).getString(FIRST_PAGE_CITY_WEATHERTYPE, null);
	}
	
	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		getLocalCityInfo();
	}
	
	public void setBottomViewHeight(int viewHeight){
		mWeatherMainFragment.setBottomViewHeight(viewHeight);
	}
	
	//test
	private void initTest(){
		initTestDialog();//test
		initSelectDialog();//test
		citiesDialog();
		initTodaySmallIconDialog();
	}
	private AuroraAlertDialog testDialog;
	private void initTestDialog(){
		testDialog=new AuroraAlertDialog.Builder(this).setItems(weatherInfos, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mCityListShowData.testchangeWeahterType(weatherInfos[arg1]);
				mWeatherMainFragment.normalshowData(0);
				testDialog.dismiss();
			}
		}).create();
	}
	
	private AuroraAlertDialog todayDialog;
	private void initTodaySmallIconDialog(){
		todayDialog=new AuroraAlertDialog.Builder(this).setItems(forcastInfos, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				WeatherForcastInfo[] forcasts = mCityListShowData.getWeatherForcastInfo(0);
				mCityListShowData.testchangeWeahterType(forcastInfos[arg1]);
				forcasts[1].setmWeatherType(forcastInfos[arg1]);
				mWeatherMainFragment.normalshowData(0);
				mWeatherMainFragment.updateWeatherBackground();
				todayDialog.dismiss();
			}
		}).create();
	}
	
	private void saveLocalFail(){
		SharedPreferences sp=getPreferences(Context.MODE_PRIVATE);
		sp.edit().putBoolean("islocalfail", !sp.getBoolean("islocalfail", false)).commit();
	}
	
	String[] selects={"选择天气类型","选择定位城市（下次进入应用有效）","选择今天的天气预报（小图标）","模拟定位城市失败（下次进入生效）"};
	private AuroraAlertDialog selectDialog;
	private void initSelectDialog(){
		selectDialog=new AuroraAlertDialog.Builder(this).setItems(selects, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if(arg1==0)
				{
					testDialog.show();
				}else if(arg1==1)
				{
					cityDialog.show();
				}else if(arg1 == 2) {
					todayDialog.show();
				}else if(arg1==3)
				{
					 if(mCityListShowData.isExistLocalCity())
                     {
						 mCityListShowData.deleteByIndex(0);
                     }
					saveLocalFail();
				}
				selectDialog.dismiss();
			}
		}).create();
	}
	
	
	private String citys[]={"齐齐哈尔", "深圳","上海","北京","广州","长沙","武汉","南京","南昌","上饶","杭州"};
	private AuroraAlertDialog cityDialog;
	private void citiesDialog(){
		cityDialog=new AuroraAlertDialog.Builder(this).setItems(citys, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				saveLocalCity(citys[arg1]);
				cityDialog.dismiss();
			}
		}).create();
	}
	//test
	private void saveLocalCity(String city){
		SharedPreferences sp=getPreferences(Context.MODE_PRIVATE);
		sp.edit().putString("localCity", city).commit();
	}
	//test
	public String getLocalCity(){
		return getPreferences(Context.MODE_PRIVATE).getString("localCity", null);
	}
	
	private void getLocalCityInfo( ) {
		if ( mCityListShowData.getCitySize() == 0 ) {
			isNeedGetLocalCityWhenDataLoaded = false;
			getLocalCity( false );
		} else {
			mWeatherMainlayout.setBackgroundColor(Color.TRANSPARENT);
			showFragmentWithNetWork();
			//先从数据库取数据
			requesAllCitysData(true);
		}
	}
	
	private void showFragmentWithNetWork( ) {
		
		//Log.e("111111", "---doForHasNetwork mIsFirstIn = ---------" + mIsFirstIn);
		if ( mIsFirstIn ) {
			mNoNetworkText.setVisibility(View.GONE);
			initFragmentContent();
			mWeatherMainFragment.show(false);
			mIsFirstIn = false;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			if(mWeatherCityListFragment !=null && mWeatherCityListFragment.isAnimatorRunning())
			{
				return true;
			}
			
			if(currentFragmentShowIndex==CITYLIST_FRAGMENT_INDEX)
			{
				if(mWeatherCityListFragment!=null)
				{
					mWeatherCityListFragment.showMainFragment();
				}
				return true;
			}
		}else if(keyCode==KeyEvent.KEYCODE_MENU&&mCityListShowData.getCitySize()>0&&isShowTest)//test
		{
			selectDialog.show();
			return true;
		}
		
		if(keyCode==KeyEvent.KEYCODE_BACK && isMainFragmentShow())
		{
			isExist = true;
			if(mWeatherMainFragment.getWeatherAnimSurfaceView() != null)
			{
				mWeatherMainFragment.getWeatherAnimSurfaceView().destoryDrawThread();
			}
		}
//		if (keyCode == KeyEvent.KEYCODE_BACK) {  
//            moveTaskToBack(false);  
//            return true;  
//        }  
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressLint("NewApi")
	private boolean isMainFragmentShow(){
		if(mWeatherMainFragment==null)
		{
			return false;
		}
		
		return mWeatherMainFragment.isShowing();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mWeatherMainFragment.getWeatherAnimSurfaceView()!=null)
		{
			mWeatherMainFragment.getWeatherAnimSurfaceView().stop();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		lastStopTime = Calendar.getInstance().getTimeInMillis();
		//城市添加了但是没取到数据
		mCityListShowData.save();
	}
	
	private final long NEED_UPDTE_TIME = 60*60*1000;
	
	private void judgeNeedUpdate(){
		long now = Calendar.getInstance().getTimeInMillis();
		if(lastStopTime>0 && now - lastStopTime >= NEED_UPDTE_TIME)
		{
			if(mWeatherMainFragment !=null)
			{
				mWeatherMainFragment.needUpdateData();
			}
		}
	}
	
	private void cancelNotification(int cityId){
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(cityId);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		judgeNeedUpdate();
		lastStopTime = 0l;
		if(mCityListShowData.isExistLocalCity())
		{
			cancelNotification(mCityListShowData.getCityInfo(0).getID());
		}
		if(isMainFragmentShow())
		{
			if(mWeatherMainFragment.getWeatherAnimSurfaceView()!=null)
			{
				mWeatherMainFragment.getWeatherAnimSurfaceView().start();
			}
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		saveFirstPageDate();
		// TODO Auto-generated method stub
		unRegisterTimeReceiver( );
		mCityListShowData.release();
        mLocationProvider.dispose();
		mWeatherMainFragment = null;
		mWeatherCityListFragment = null;
		// add by likai 2014-10-30 begin
		if ( mThread != null ) {
			mThread.quit();
		}
		if ( mManager != null ) {
			mManager.setController(null);
		}
		if (mCityListShowData.getCitySize() == 1 && mCityListShowData.getCityWeatherInfo(mCityListShowData.getCityName(0)) == null) {
			mCityListShowData.resetLastAdd();
		}
		// add by likai 2014-10-30 end
		super.onDestroy();
	}
	
	private void registerTimeReceiver( ) {
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        
        //网络变化广播监听
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        
        this.registerReceiver(mIntentReceiver, filter);
	}
	
	private void unRegisterTimeReceiver( ) {
		this.unregisterReceiver(mIntentReceiver);
	}
	
	private void doWhenNetWorkChanged( Context context ) {
		int flag = SystemUtils.isNetStatus(context);
		if(flag!=0)
		{
			if(!mCityListShowData.isExistLocalCity())
			{
				mLocationProvider=new LocationProvider(this, result);
			}
		}
		
		if ( mIsNeedReceiveNetworkBroadCast && flag != 0 && mCityListShowData.getCitySize() == 0 ) {
			isNeedGetLocalCityWhenDataLoaded = false;
			mIsNeedNoNetworkToast = true;
			mNoNetworkText.setVisibility(View.GONE);
			getLocalCity( false );
		}
	}
	
	private void updateWeatherAttribute( ) {
		updateCityListThumb();
		updateCityDateView( );
		updateMainWeatherBackground( );
	}
	
	private void updateCityListThumb(){
		mCityListShowData.updateCityListThumb();
	}
	
	/**
	 * 更新时间日期
	 */
	private void updateCityDateView( ) {
		if ( mWeatherMainFragment != null) {
			mWeatherMainFragment.getCityOptionView().getCityDateView().updateTime();
		}
	}
	
	/**
	 * 更新主界面天气背景
	 */
	private void updateMainWeatherBackground( ) {
		if ( mWeatherMainFragment != null) {
			mWeatherMainFragment.updateWeatherBackground();
		}
	}

	private void judgeIfFragmentExist() {
		mWeatherMainFragment = (WeatherMainFragment) mFragmentManager.findFragmentByTag("" + 1);
		mWeatherCityListFragment = (WeatherCityListFragment) mFragmentManager.findFragmentByTag("" + 0);
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (mWeatherMainFragment != null) {
			transaction.remove(mWeatherMainFragment);
			mWeatherMainFragment = null;
		}
		if (mWeatherCityListFragment != null) {
			transaction.remove(mWeatherCityListFragment);
			mWeatherCityListFragment = null;
		}
		transaction.commit();
	}

	/**
	 * 将所有的Fragment都置为隐藏状态。
	 * 
	 * @param
	 *
	 */
	private void hideFragments( ) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (mWeatherMainFragment != null) {
			transaction.hide(mWeatherMainFragment);
		}
		if (mWeatherCityListFragment != null) {
			transaction.hide(mWeatherCityListFragment);
		}
		transaction.commit();
	}
	
	private void showFragments( ) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (mWeatherCityListFragment != null) {
			transaction.show(mWeatherCityListFragment);
		}
		if (mWeatherMainFragment != null) {
			transaction.show(mWeatherMainFragment);
		}
		transaction.commit();
	}
	
	public void setSelectCityListCurrentIndex(int index){
		if(mWeatherCityListFragment!=null)
		{
			mWeatherCityListFragment.setSelectCityIndex(index);
		}
	}
	
	private void initFragment() {
		mHasNetWork = false;
		mIsNeedNoNetworkToast = true;
		mWeatherMainlayout = (RelativeLayout)findViewById(R.id.weathermainlayout);
		if(getFirstPageCityWeatherType()!=null)
		{
			mWeatherMainlayout.setBackgroundColor(Color.TRANSPARENT);
		}
		mNoNetworkText = (TextView)findViewById(R.id.nonetwork_text);
//		judgeIfFragmentExist();
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (mWeatherCityListFragment == null) {
			mWeatherCityListFragment = new WeatherCityListFragment();
		}
		if (mWeatherMainFragment == null) {
			mWeatherMainFragment = new WeatherMainFragment();
		}
		transaction.add(R.id.weathermainlayout, mWeatherCityListFragment,0 + "");
		transaction.add(R.id.weathermainlayout, mWeatherMainFragment, 1 + "");
		transaction.commit();
	}

	private boolean isAnimalRunning(){
		if(mWeatherMainFragment==null||mWeatherCityListFragment==null)
			return false;
		return mWeatherMainFragment.isAnimatorRunning()||mWeatherCityListFragment.isAnimatorRunning();
	}
	
	public boolean isCityListVisible(){
		return mWeatherCityListFragment==null?false:mWeatherCityListFragment.isShowing();
	}
	
	 /**
     * 选择显示的fragment，0为主页fragment，1为城市列表页面fragment
     * @param index
     */
	public void setSelection(int index,boolean isRunAnimation) {
		if(isRunAnimation&&isAnimalRunning()&&!mWeatherCityListFragment.isInitOk())
			return;
		currentFragmentShowIndex=index;
		if (index == MAIN_FRAGMENT_INDEX) {
			mWeatherMainFragment.show(isRunAnimation);
			mWeatherCityListFragment.hide(isRunAnimation);
		}
		else if (index == CITYLIST_FRAGMENT_INDEX) {
			mWeatherCityListFragment.show(isRunAnimation);
			mWeatherMainFragment.hide(isRunAnimation);
		}
	}

	public LocationProvider mLocationProvider;
	// add by likai 2014-10-30 begin
	private void initManager() {
		mLocationProvider=new LocationProvider(this,result);
		mManager = new WeatherManager();
		mThread = new ManagerThread(mManager);
		mThread.init(this);
		registerTimeReceiver( );
	}
	
	//test
	private int getCityIdFromCityName( String cityName ) {
		int id = -1;
		List<CityItem> listCityItem = new ArrayList<CityItem>();
		
		CNCityAdapter weatherDB = new CNCityAdapter(this);
		weatherDB.open();
		weatherDB.getCityListFromHZ(listCityItem, cityName);
		
		if ( listCityItem.size() > 0 ) {
			id =  listCityItem.get(0).getId();
		}
		weatherDB.close();
		weatherDB = null;
		return id;
	}
	
	private static final int LOCAL_CITY_SUCCESS = 0x1542;
	
	private ILocateResult result=new ILocateResult() {
		
		@Override
		public void locateSuccess(String cityName, int cityId) {
			Message msg = new Message();
			msg.what = LOCAL_CITY_SUCCESS;
			msg.arg1 = cityId;
			msg.obj = cityName;
			mHandler.sendMessage(msg);
		}
		
		@Override
		public void locateError() {
			mHandler.sendEmptyMessage(LOCAL_CITY_ERROR);
		}
	};
	
	
	public void requesAllCitysData(boolean fromDB)
	{
		int citySzie = mCityListShowData.getCitySize();
		for(int i = 0; i < citySzie; i++)
		{
			requestWeatherInfo(mCityListShowData.getCityInfo(i).getID(),fromDB);
		}
	}
	
	/**
	 * we should call this method to request data !!!!
	 * @param cityId
	 * @param
	 */
	public void requestWeatherInfo(int cityId,boolean fromDB)
	{
		
		
		mIsFromDB = fromDB;
		
		String id = ((Integer)cityId).toString();
		
		String maxDays = "6";
		mCityListShowData.requestCountAdd();
		mManager.getWeatherList(new DataResponse<WeatherDataEveryDay>() {
			@Override
			public void run() {
				if (value != null) {
					Log.i("likai", "code: " + value.getCode() + " desc: " + value.getDesc() + " city: " + value.getmCity());

					List<WeatherHourInfo> hourInfoList = value.getmWeatherDataEveryDays();
					if (hourInfoList != null) {
						for (WeatherHourInfo hourInfo : hourInfoList) {
							hourInfo.print();
						}
					}
					List<WeatherAirQualities> airQualityList = value.getWeatherAirQualities();
					if (airQualityList != null) {
						for (WeatherAirQualities airQuality : airQualityList) {
							airQuality.print();
						}
					}
					List<WeatherForcastInfo> forcastInfoList = value.getWeatherForcecasts();
					if (forcastInfoList != null) {
						for (WeatherForcastInfo forcastInfo : forcastInfoList) {
							forcastInfo.print();
						}
					}
				}
			}
		}, this, id, maxDays, fromDB);
		
	}
	
	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source) {
		mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
	}

	private void requestNetBack(){
		mCityListShowData.requestCountReduce();
	}
	
	private static final int OTHER_ERROR=0X56425;
	
	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		if(code!=INotifiableController.CODE_UNCAUGHT_ERROR&&code!=INotifiableController.CODE_NOT_NETWORK)
		{
			mHandler.sendEmptyMessage(OTHER_ERROR);
		}
		switch (code) {
		case INotifiableController.CODE_UNKNONW_HOST:
		case INotifiableController.CODE_WRONG_DATA_FORMAT:
		case INotifiableController.CODE_REQUEST_TIME_OUT:
		case INotifiableController.CODE_CONNECT_ERROR:
		case INotifiableController.CODE_GENNERAL_IO_ERROR:
		case INotifiableController.CODE_NOT_FOUND_ERROR:
		case INotifiableController.CODE_JSON_PARSER_ERROR:
		case INotifiableController.CODE_JSON_MAPPING_ERROR:
		case INotifiableController.CODE_UNCAUGHT_ERROR:
			mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
			break;
		case INotifiableController.CODE_NOT_NETWORK:
			mHandler.sendEmptyMessage(Globals.NO_NETWORK);
			break;
		default:
			break;
		}
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
	}

	/**
	 * this is callback when we request city weather infomation
	 */
	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		
		//get WeatherDataEveryDay information first !!!
		WeatherDataEveryDay wd = (WeatherDataEveryDay) response.value;
		if(wd == null)
		{
			wd=new WeatherDataEveryDay();
		}
		mCityListShowData.setCityWeatherInfo(wd.getmCity(), wd);
		Message msg = Message.obtain();
		msg.what = Globals.HAS_NETWORK;
		msg.obj = wd.getmCity();
		mHandler.sendMessage(msg);
	}

	private void doForNoNetwork() {
		mCityListShowData.resetLastAdd();
		CustomeToast.show(this, R.string.no_network);
		mHasNetWork = false;
		
		mIsNeedReceiveNetworkBroadCast = true;
		requestNetBack();
		mCityListShowData.refreshData(null, mIsFromDB);
		initFragmentContent();
	}
	
	private void doForNetworkError( ) {
		//城市添加了但是没取到数据
		mCityListShowData.resetLastAdd();

		if ( mIsNeedNoNetworkToast ) {
//			Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
			mIsNeedNoNetworkToast = false;
		}
		CustomeToast.show(this, R.string.network_error);
		mHasNetWork = false;
		
		mIsNeedReceiveNetworkBroadCast = true;
		requestNetBack();
		mCityListShowData.refreshData(null, mIsFromDB);
		initFragmentContent();
	}
	
	private int dataIndex = 0;
	private boolean isNeedGetLocalCityWhenDataLoaded = true;
	private void doForHasNetwork(String cityName) {
		
		if( mWeatherMainFragment == null ) {
			return;
		}
		
		if (!mIsFromDB) {
			mHasNetWork = true;
		}
		requestNetBack();
		mCityListShowData.refreshData(cityName, mIsFromDB);
		initFragmentContent();
		dataIndex ++;
		if ( dataIndex > mCityListShowData.getCitySize() ) {
			dataIndex = mCityListShowData.getCitySize();
		}
		
		if ( isNeedGetLocalCityWhenDataLoaded && dataIndex == mCityListShowData.getCitySize() ) {
			//每次进来都要判断当前城市有无变化
			Log.e("111111", "----has city, but we still need to get local city--------------");
			getLocalCity(true);
			isNeedGetLocalCityWhenDataLoaded = false;
		}
	}
	
	private boolean isHasInit=false;
    public void initFragmentContent(){
    	if(!isHasInit)
    	{
    		isHasInit=true;
	    	mWeatherMainFragment.initMainFragment();
    	}
    }
	
	
	private void doForGetLocalCity(String cityName,int cityId) {
		//test
		if(getLocalCity()!=null&&isShowTest)
		{
			cityName=getLocalCity();
			cityId=getCityIdFromCityName(cityName);
		}

		//101280601
		if (cityId != -1) {
			mCityListShowData.addFromLocal(cityName, cityId);
		}
	}

	private boolean isHasNotifyLocateError = false;
	private boolean isExist = false;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isExist) {
				return;
			}
			switch (msg.what) {
			case Globals.NETWORK_ERROR:
				doForNetworkError( );
				mCityListShowData.sendNetResponse(Globals.NETWORK_ERROR,null);
				break;
			case Globals.NO_NETWORK:
				doForNoNetwork( );
				mCityListShowData.sendNetResponse(Globals.NO_NETWORK,null);
				break;
			case Globals.HAS_NETWORK:
				//update UI
				doForHasNetwork((String) msg.obj);
				mCityListShowData.sendNetResponse(Globals.HAS_NETWORK, (String) msg.obj);
				break;
			case LOCAL_CITY_ERROR:
				if(!mCityListShowData.isExistLocalCity()&&!isHasNotifyLocateError)
				{
					CustomeToast.show(AuroraWeatherMain.this, R.string.local_city_error);
				    mWeatherMainFragment.locateError();
				    isHasNotifyLocateError=true;
				}
				mWeatherCityListFragment.initCityListFragment();
				initFragmentContent();
				break;
			case OTHER_ERROR:
				requestNetBack();
				mCityListShowData.sendNetResponse(-1,null);
				mCityListShowData.refreshData(null, mIsFromDB);
				break;
			case LOCAL_CITY_SUCCESS:
				int cityId = msg.arg1;
				String cityName = (String) msg.obj;
				cancelNotification(cityId);
				doForGetLocalCity(cityName, cityId);
				if(mWeatherCityListFragment!=null)
				{
				   mWeatherCityListFragment.initCityListFragment();
				}
				break;
			default:
				break;
			}
		}
	};
	// add by likai 2014-10-30 end
	
	
	public boolean isCityListFragmentIntiOk(){
		return mWeatherCityListFragment.isInitOk();
	}
	
	public Handler getMainHandler()
	{
		return mHandler;
	}
	
	private static final int LOCAL_CITY_ERROR=0X125;
	public void getLocalCity(boolean hasCity) {
		if (!SystemUtils.hasActiveNetwork(this)) {
			doForNoNetwork( );
			if (!hasCity) {
				mNoNetworkText.setVisibility(View.VISIBLE);
			} else {
				mNoNetworkText.setVisibility(View.GONE);
			}
		}else{
		    showFragmentWithNetWork( );
		}
	}
}