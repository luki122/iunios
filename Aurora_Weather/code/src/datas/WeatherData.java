package datas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;


public class WeatherData {
	
	public static final int MAX_CITY_NUMBER = 8;//一个定位城市，7个添加城市
	
	//cities 
	private List<WeatherCityInfo> mCitys = Collections.synchronizedList(new ArrayList<WeatherCityInfo>());
	/**
	 * each city has it's weather infomation
	 * 
	 * String : is city name
	 * 
	 * WeatherDataEveryDay : is this city all weather information !!!
	 */
	private Map<String , WeatherDataEveryDay> mCityWeatherInfo =Collections.synchronizedMap(new HashMap<String , WeatherDataEveryDay>());
	
	/**
	 * weather anim information
	 */
	private WeatherAnimInfo mWeatherAnimInfo;
	
	//the only one instance !!!
	private Context mContext;
	
	private CitysHelp citysHelp;
	public WeatherData(Context context)
	{
		mWeatherAnimInfo = new WeatherAnimInfo(context);
		mContext=context;
		
		citysHelp=CitysHelp.getInstance(mContext);
		getCitys();
	}
	
	/**
	 * 
	 * @return
	 */
	public WeatherAnimInfo getWeatherAnimInfo()
	{
		return mWeatherAnimInfo;
	}
	
	public WeatherCityInfo getCityInfo(int index)
	{
		if ( index < mCitys.size() ) {
			return mCitys.get(index);
		}
		return null;
	}
	
	public List<WeatherCityInfo> getAllCitys()
	{
		return mCitys;
	}
	/**
	 * 
	 * @param cityName
	 * @return
	 */
	public WeatherDataEveryDay getCityWeatherInfo(String cityName)
	{
		return mCityWeatherInfo.get(cityName);
	}
	
	public void deleteCity(int index){
		mCitys.remove(index);
	}
	/**
	 * 
	 * @param name
	 * @param weatherInfo
	 */
	public void setCityWeatherInfo(String name , WeatherDataEveryDay weatherInfo)
	{
		mCityWeatherInfo.put(name, weatherInfo);
	}
	
	/**
	 * init mCitys, we should read city from ourself record
	 */
	private void getCitys()
	{
		mCitys.addAll(citysHelp.getCityFromPhone());
	}
	
	public void addCity(String name , int id, int index)
	{
		int len = mCitys.size();
		
		if(len < MAX_CITY_NUMBER)
		{
			WeatherCityInfo city = new WeatherCityInfo();
			city.setCityName(name);
			city.setID(id);
			mCitys.add(index, city);
		}
	}
	
	public void addCity(String name , int id)
	{
		addCity(name, id, mCitys.size());
	}
	
	public void release()
	{
		destory();
	}
	
	public void save()
	{
		citysHelp.saveCitys(mCitys);
	}
	
	private void destory()
	{
		mCitys.clear();
		mCityWeatherInfo.clear();
		citysHelp.release();
	}
	
}
