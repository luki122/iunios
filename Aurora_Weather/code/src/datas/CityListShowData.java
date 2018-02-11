package datas;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
import com.aurora.weatherforecast.WeatherMainFragment;

import android.content.ClipData.Item;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
/**
 * 
 * 城市列表页面及与天气主页面交互的功能类
 * @author j
 *
 */
public class CityListShowData {
	private List<CityListShowItem> datas=Collections.synchronizedList(new ArrayList<CityListShowItem>());
	private Context mContext;
	private List<INotifyListener> iNotifyListeners=Collections.synchronizedList(new ArrayList<CityListShowData.INotifyListener>());;
	private WeatherData mWeatherData;
	private WeatherAnimInfo mWeatherAnimInfo;
	private final int MORNING=6,AFTER_NOON=18;
	public static final int MARK_HOUR_1 = 6;
	public static final int MARK_HOUR_2 = 18;
	public static final int MARK_HOUR_3 = 12;
	public static final int MARK_HOUR_4 = 20;
	public static final int MAX_CITY_NUMBER = 8;//一个定位城市，7个添加城市
	private List<WeatherCityInfo> mCitys;
	private static CityListShowData instance;
	private AtomicInteger requestCount=new AtomicInteger(0);
	private CityListShowData(Context mContext){
		this.mContext=mContext;
		this.mWeatherData=new WeatherData(mContext);
		this.mWeatherAnimInfo=mWeatherData.getWeatherAnimInfo();
		mCitys=mWeatherData.getAllCitys();
		lastUpdateIndex=getThumbIndex();
		currentIndex=getThumbIndex();
		fillList();
	}

	public synchronized static CityListShowData getInstance(Context context){
		if(instance==null)
		{
			instance=new CityListShowData(context);
		}
		return instance;
	}
	
	private int currentPageIndex=0;
	
	
	
	public int getCurrentPageIndex() {
		return currentPageIndex;
	}

	public void setCurrentPageIndex(int currentPageIndex) {
		this.currentPageIndex = currentPageIndex;
	}

	private CityListShowItem defaultItem =null;
	
	private CityListShowItem getDefaultItem(){
		if(defaultItem==null)
		{
			defaultItem=new CityListShowItem(mContext);
		}
		return defaultItem;
	}
	
	public WeatherCityInfo getCityInfo(int index)
	{
		return mWeatherData.getCityInfo(index);
	}
	
	public void setCityWeatherInfo(String name , WeatherDataEveryDay weatherInfo)
	{
		mWeatherData.setCityWeatherInfo(name, weatherInfo);
	}
	/**
	 * 
	 * @param cityName
	 * @return
	 */
	public WeatherDataEveryDay getCityWeatherInfo(String cityName)
	{
		return mWeatherData.getCityWeatherInfo(cityName);
	}
	
	public WeatherDataEveryDay getCityWeatherInfo(int index)
	{
		return mWeatherData.getCityWeatherInfo(getCityName(index));
	}
	
	private boolean isRelease=false;
	public void save(){
		if(!isRelease)
		{
		   mWeatherData.save();
		}
	}
	
	public WeatherForcastInfo[] getWeatherForcastInfo(int index){
		return mWeatherData.getCityWeatherInfo(getCityName(index)).getWeatherForcastInfo();
	}
	
	public List<WeatherForcastInfo> getWeatherForcecasts(int index) {
		return mWeatherData.getCityWeatherInfo(getCityName(index)).getWeatherForcecasts();
	}
	
	public String getCityName(int index){
		return getWeatherDateItem(index).getCityName();
	}
	
	public boolean isExistLocalCity(){
		for(int i=0;i<mCitys.size();i++)
		{
			if(mCitys.get(i).getIsLocalCity())
			{
				return true;
			}
		}
		return false;
	}
	
	public int getCitySize(){
		return mCitys.size();
	}
	
	public CityListShowItem getLocateCityItem(){
		if(datas.size()==0)
			return null;
		return datas.get(0);
	}
	public List<CityListShowItem> getDatas(){
		return datas;
	}
	
	/**
	 * 根据WeatherData填充城市列表的数据
	 */
	private void fillList(){
		WeatherDataEveryDay weatherDataEveryDay;
		WeatherHourInfo weatherHourInfo;
		for (WeatherCityInfo weatherCityInfo : mCitys) {
			 datas.add(createShowCityItem(weatherCityInfo.getCityName(), weatherCityInfo.getID()));
		}
	}
	
	public void requestCountAdd(){
		requestCount.getAndIncrement();
	}
	
	public void requestCountReduce(){
		if(requestCount.get()>0)
		{
		  requestCount.getAndDecrement();
		}
	}
	
	public CityListShowItem getWeatherDateItem(int index){
		if(index==-1||index>datas.size()-1)
		{
			return getDefaultItem();
		}
		return datas.get(index);
	}
	
	private CityListShowItem createShowCityItem(String cityName,int cityId){
		CityListShowItem item=new CityListShowItem(mContext);
		item.setCityId(cityId);
		item.setCityName(cityName);
		setItemWeatherInfo(item);
		return item;
	}
	
	private final int THUMB_DAY_INDEX=0,THUMB_NIGHT_INDEX=1;
	private int getThumbIndex(){
		Calendar now=Calendar.getInstance();
		int nowHour=now.get(Calendar.HOUR_OF_DAY);
		if(nowHour>=MORNING&&nowHour<AFTER_NOON)
		{
			return THUMB_DAY_INDEX;
		}else{
			return THUMB_NIGHT_INDEX;
		}
	}
	
	//test
	public void testchangeWeahterType(String weatherType){
		datas.get(0).setWeahterType(weatherType);
		datas.get(0).setBigcirBgRes(getBgResIdByWeatherType(weatherType));
		datas.get(0).setResId(getThumbIdByWeatherType(weatherType));
	}
	
	private int getBgResIndex(String weatherType){
		Calendar now=Calendar.getInstance();
		int nowHour=now.get(Calendar.HOUR_OF_DAY);
		if(weatherType.equals(mContext.getString(R.string.weather_default))||weatherType.equals("晴"))
		{
			if(nowHour >= MARK_HOUR_1 && nowHour < MARK_HOUR_3) {
				AuroraWeatherMain.mIsDayTime = true;
				return 0;
			}else if(nowHour >= MARK_HOUR_3 && nowHour < MARK_HOUR_2) {
				AuroraWeatherMain.mIsDayTime = true;
				return 1;
			}else if(nowHour >= MARK_HOUR_2 && nowHour < MARK_HOUR_4) {
				AuroraWeatherMain.mIsDayTime = false;
				return 2;
			}else {
				AuroraWeatherMain.mIsDayTime = false;
				return 3;
			}
		}else{
			if (nowHour >= MARK_HOUR_1 && nowHour < MARK_HOUR_2 ) {
				AuroraWeatherMain.mIsDayTime = true;
				return 0;
			}else{
				AuroraWeatherMain.mIsDayTime = false;
				return 1;
			}
		}
	}
	
	/**
	 * 根据天气类型获取天气类型图表
	 * @param weatherType
	 * @return
	 */
	private int getThumbIdByWeatherType(String weatherType){
		int [] thumbs=mWeatherAnimInfo.getThumbs(weatherType);
		if(thumbs==null)
			return R.drawable.weather_thumb_default;
		int index=getThumbIndex();
		return thumbs[index];
	}
	
	public int getBgResIdByWeatherType(String weatherType){
		if(weatherType.equals(mContext.getString(R.string.weather_default)))
		{
			weatherType="晴";
		}
		int [] bgs=mWeatherAnimInfo.getBackgroundResIds(weatherType);
		if(bgs==null)
		{
			return R.drawable.sunny_morning_bg;
		}
		int index=getBgResIndex(weatherType);
		return bgs[index];
	}
    private final int TODAT_DATE_INDEX=1;
    private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
	private Calendar formatStr2Calendar(String dateStr){
		if(TextUtils.isEmpty(dateStr))
			return null;
		Calendar cal=Calendar.getInstance();
		try {
			cal.setTime(format.parse(dateStr));
			return cal;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	private boolean judgeLocalDateIsOk(String city){
		String dateStr=null;
		try {
			dateStr = mWeatherData.getCityWeatherInfo(city).getWeatherForcecasts().get(TODAT_DATE_INDEX).getWeatherDate();
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		Calendar now=Calendar.getInstance();
		Calendar date=formatStr2Calendar(dateStr);
		if(date==null)
			return false;
		if(now.get(Calendar.YEAR)==date.get(Calendar.YEAR)&&now.get(Calendar.MONTH)==date.get(Calendar.MONTH)&&now.get(Calendar.DAY_OF_MONTH)==date.get(Calendar.DAY_OF_MONTH))
		{
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isHasShow=false;
	private static final int SHOW_LOCAL_TIME_ERROR_MSG=0X1235;
	private void showLocalMsg(String city){
		if(!isHasShow&&!judgeLocalDateIsOk(city))
		{
			Toast.makeText(mContext, mContext.getResources().getString(R.string.local_time_error),200).show();
			isHasShow=true;
		}
	}
	/**
	 * 根据城市id刷新城市列表数据
	 * @param cityId
	 */
	public void refreshData(String cityName, boolean fromDB){
		    int db = fromDB ? 1:0;
		    if(cityName==null)
		    {
		    	if(requestCount.get()==0&&!fromDB)
				{
					sendNotify(INotifyListener.OPE_NETWORK_ERROR,db);
				}
		    	return;
		    }
		    if(!fromDB)
			{
				showLocalMsg(cityName);
			}
			for(int i=0;i<datas.size();i++)
			{
				CityListShowItem item=datas.get(i);
				if(item.getCityName().equals(cityName))
				{
					if(mCitys.size() >0 &&mCitys.get(0).getIsLocalCity() && i == 0 )
					{
						lastAddCityIndex = -1;
					}
				   setItemWeatherInfo(item);
				   sendNotify(INotifyListener.OPE_UPDATE_DATA,i,db);
				}
			}
			if(requestCount.get()==0)
			{
				sendNotify(INotifyListener.OPE_REQUEST_ALL_BACK,db);
			}
	}
	
	public WeatherAnimInfo getWeatherAnimInfo(){
		return mWeatherAnimInfo;
	}
	
	private void setItemWeatherInfo(CityListShowItem item){
		WeatherDataEveryDay weatherDataEveryDay=mWeatherData.getCityWeatherInfo(item.getCityName());
		if(weatherDataEveryDay!=null)
		{
			WeatherHourInfo weatherHourInfo=weatherDataEveryDay.getHourInfo();
			if(weatherHourInfo!=null)
			{
				item.setCurTemp(((int)weatherHourInfo.getCurTemp())+"");
				item.setLowAndHighTempStr(((int)weatherHourInfo.getLowTemp())+"/"+((int)weatherHourInfo.getHighTemp()));
				item.setWeahterType(weatherHourInfo.getmWeatherType());
				item.setResId(getThumbIdByWeatherType(weatherHourInfo.getmWeatherType()));
				item.setBigcirBgRes(getBgResIdByWeatherType(weatherHourInfo.getmWeatherType()));
				if(TextUtils.isEmpty(weatherHourInfo.getmWindLevel())||weatherHourInfo.getmWindLevel().endsWith("0"))
				{
					item.setWindContent(weatherHourInfo.getWindDirection());
				}else{
				       item.setWindContent(mContext.getResources().getString(R.string.tv_wind_location, weatherHourInfo.getWindDirection()+(weatherHourInfo.getmWindLevel())));
				}
			    item.setmHumidityContent(mContext.getResources().getString(R.string.tv_humidity, weatherHourInfo.getmHumidity()+"%"));
			}
			WeatherAirQualities wq=weatherDataEveryDay.getWeatherAirQuality();
			if(wq!=null)
			{
			  item.setAirQualityDesc(mContext.getResources().getString(R.string.tv_air_quality, wq.getAirDesc()));
			  item.setAirQualityValueDes(((int)wq.getAirQualityValue())+"");
			  item.setHasAirQuality(true);
			}else if(weatherHourInfo==null){
				item.setHasAirQuality(true);
			}else{
				item.setHasAirQuality(false);
			}
			WeatherForcastInfo[] infos=weatherDataEveryDay.getWeatherForcastInfo();
			if(infos!=null&&infos.length>0)
			{
				List<Calendar> days=new ArrayList<Calendar>();
				int[] forcastWeatherThumb=new int[6];
				String[] forcastTems=new String[6];
				for(int i=0;i<infos.length;i++)
				{
					days.add(formatStr2Calendar(infos[i].getWeatherDate()));
					forcastWeatherThumb[i]=mWeatherAnimInfo.getSmaillWeatherIcon(infos[i].getmWeatherType(), mContext);
					forcastTems[i]=((int)infos[i].getLowTemp())+"/"+((int)infos[i].getHighTemp());
				}
				item.setForcastDate(days);
				item.setForcastWeatherThumb(forcastWeatherThumb);
				item.setForcastWeatheTem(forcastTems);
			}
		}
	}
	
	/**
	 * 添加监听器
	 * @param iNotifyListener
	 */
	public void addINotifyListener(INotifyListener iNotifyListener){
		if(!iNotifyListeners.contains(iNotifyListener))
		{
			iNotifyListeners.add(iNotifyListener);
		}
	}
	/**
	 * 移除监听器
	 * @param iNotifyListener
	 */
	public void removeINotifyListener(INotifyListener iNotifyListener){
		if(iNotifyListeners.contains(iNotifyListener))
		{
			iNotifyListeners.remove(iNotifyListener);
		}
	}
	
	private int lastUpdateIndex,currentIndex;
	public void updateCityListThumb(){
		currentIndex=getThumbIndex();
		for (CityListShowItem cityListShowItem : datas) {
			int resId=getThumbIdByWeatherType(cityListShowItem.getWeahterType());
			int bgRsId=getBgResIdByWeatherType(cityListShowItem.getWeahterType());
			if(resId!=-1)
			{
			   cityListShowItem.setResId(resId);
			}
			if(bgRsId!=-1)
			{
				cityListShowItem.setBigcirBgRes(bgRsId);
			}
		}
			sendNotify(INotifyListener.OPE_CITY_THUMB_UPDATE);//更换列表缩略图后进行列表刷新
	}
	
	public int getCityIndexByCityId(int cityId){
		for(int i=0;i<datas.size();i++)
		{
			if(datas.get(i).getCityId()==cityId)
			{
				return i;
			}
		}
		return -1;
	}
	
	public boolean checkCityIsAdded(int cityId){
		for (CityListShowItem cityListShowItem : datas) {
			if(cityListShowItem.getCityId()==cityId)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isFillMaxCity(){
		return datas.size() == CityListShowData.MAX_CITY_NUMBER;
	}
	
	private boolean hasLocalCity(){
		if(datas.size()==0)
			return false;
		int localCityId = datas.get(0).getCityId();
		for(int i=1;i<datas.size();i++)
		{
			if(datas.get(i).getCityId() == localCityId)
			{
				return true;
			}
		}
		return false;
	}
	
	public void addFromLocal(String cityName,int cityId){
		if(mCitys.size()>0 && mCitys.get(0).getID()==cityId)
		{
			if(!mCitys.get(0).getIsLocalCity())
			{
				mCitys.get(0).setIsLocalCity(true);
				sendNotify(INotifyListener.OPE_LOCALCITY_ENABLE);
			}
			return;
		}
		boolean isDelete=false;
		if(mCitys.size()>0)
		{
			mWeatherData.getCityInfo(0).setIsLocalCity(false);
		}
		if(isFillMaxCity())
		{
			isDelete=true;
			mCitys.remove(0);
			datas.remove(0);
		}else if(hasLocalCity()){
			isDelete=true;
			mCitys.remove(0);
			datas.remove(0);
		}
		
		mWeatherData.addCity(cityName, cityId,0);
		//设置当前城市
		mWeatherData.getCityInfo(0).setIsLocalCity(true);
		datas.add(0,createShowCityItem(cityName, cityId));
		if(isDelete)
		{
			sendNotify(INotifyListener.OPE_LOCAL_CITY_PAGE_CHANGE,0);
		}else{
			lastAddCityIndex=0;
		    sendNotify(INotifyListener.OPE_ADD_LOCAL_CITY,0);//添加城市操作,第二个参数是索引
		}
	}
	
	private int lastAddCityIndex=-1;
	
	public void add(String cityName,int cityId){
		if(isFillMaxCity())
		{
			Toast.makeText(mContext, R.string.tv_city_add_already_full, 300).show();
			return;
		}
		datas.add(createShowCityItem(cityName, cityId));
		mWeatherData.addCity(cityName, cityId);
		lastAddCityIndex=datas.size()-1;
		sendNotify(INotifyListener.OPE_ADD,datas.size()-1);//添加城市操作,第二个参数是索引
	}
	
	public void deleteByIndex(int index){
		datas.remove(index);
		mWeatherData.deleteCity(index);
		sendNotify(INotifyListener.OPE_DELETE,index);//删除操作第二个参数是删除项的索引
	}
	
	public void resetLastAdd(){
		Log.e("jadon3", "lastAddCityIndex = "+lastAddCityIndex);
		
		if(datas.size()==0||lastAddCityIndex==-1||lastAddCityIndex > datas.size()-1)
		{
			lastAddCityIndex = -1;
			return;
		}
		datas.remove(lastAddCityIndex);
		mWeatherData.deleteCity(lastAddCityIndex);
		sendNotify(INotifyListener.OPE_RESET_LAST_ADD,lastAddCityIndex);
		lastAddCityIndex = -1;
	}
	
	/**
	 * 列表点击跳转到主页时调用，主要用来切换主页当前显示城市
	 * @param index
	 */
	public void itemClick(int index){
		sendNotify(INotifyListener.OPE_ITEM_SELECT,index);//第二个参数是点击的索引
	}
	
	public void release(){
		mWeatherData.release();
		datas.clear();
		iNotifyListeners.clear();
		netResponseCodes.clear();
		isRelease=true;
		instance=null;
	}
	
	private int getIndexByCityId(int cityId){
		for (int i = 0; i < datas.size(); i++) {
			if(datas.get(i).getCityId()==cityId)
			{
				return i;
			}
		}
		return -1;
	}
	
	
	private<T> void sort(List<T> datas,int from ,int to){
		T fromObj=datas.get(from);
		T toObj=datas.get(to);
		datas.remove(fromObj);
		datas.add(to,fromObj);
	}
	
	/**
	 * 城市列表转换位置
	 * @param from
	 * @param to
	 */
	public void switchPosition(int from,int to){
		if(from<0||to<0)
			return;
		sort(mCitys, from, to);
		sort(datas, from, to);
		sendNotify(INotifyListener.OPE_SWITCH_POSITION,from,to);//切换位置的时候，第二个参数是源城市索引，第三个参数是目标位置
		                                                                                           //城市索引              
	}
	/**
	 * 当数据放生改变后，通知页面
	 * @param opeType
	 */
	private void sendNotify(int... opeType){
		Log.e("jadon2", opeType[0]+"");
		for (int i=0;i<iNotifyListeners.size();i++) {
			iNotifyListeners.get(i).notityDataChange(opeType);
		}
	}
	
	private ArrayList<INetResponseCode> netResponseCodes=new ArrayList<CityListShowData.INetResponseCode>();
	public void addINetResponseCode(CityListShowData.INetResponseCode netResponseCode){
		if(!netResponseCodes.contains(netResponseCode))
		{
			netResponseCodes.add(netResponseCode);
		}
	}
	
	public void removeINetResponseCode(CityListShowData.INetResponseCode netResponseCode){
		if(netResponseCode!=null&&netResponseCodes.contains(netResponseCode))
		{
			netResponseCodes.remove(netResponseCode);
		}
	}
	
	public void sendNetResponse(int responseCode,String cityName){
		INetResponseCode netResponseCode=null;
		for(int i=0;i<netResponseCodes.size();i++)
		{
			netResponseCode=netResponseCodes.get(i);
			if(netResponseCode!=null)
			{
				netResponseCode.netResponseCode(responseCode,cityName);
			}
		}
	}
	
	public static interface INetResponseCode{
		void netResponseCode(int netCode,String cityName);
	}
	
	public static interface INotifyListener{
		int OPE_ADD=0;//添加城市操作
	    int OPE_DELETE=1;//删除城市操作
	    int OPE_SWITCH_POSITION=2;//交换位置操作
	    int OPE_UPDATE_DATA=3;//网络获取数据更新操作
	    int OPE_ITEM_SELECT=4;//选中城市操作
	    int OPE_CITY_THUMB_UPDATE=5;//城市列表缩略图刷新
	    int OPE_UPDATE_LOCAL_CITY_DATE=6;//定位城市请求到网络数据刷新
	    int OPE_LOCAL_CITY_PAGE_CHANGE=7;//定位城市页面改变刷新
	    int OPE_RESET_LAST_ADD=8;//撤销添加在列表底部的城市
	    int OPE_ADD_LOCAL_CITY=9;//添加定位城市
	    int OPE_CITYLIST_UPDATE_DATA=10;//网络获取数据更新操作
	    int OPE_REQUEST_ALL_BACK=11;//网络请求的数据已全部返回通知
	    int OPE_NETWORK_ERROR=12;
	    int OPE_LOCALCITY_ENABLE=13;
		void notityDataChange(int... opeType);
	}
}
