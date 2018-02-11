package datas;

import java.util.HashMap;

import com.aurora.weatherdata.db.CityConditionAdapter;
import com.aurora.weatherforecast.R;

import android.content.Context;
import android.util.Log;

public class WeatherAnimInfo {
	
	/**
	 * String : weather Type
	 * WeatherAnimData
	 */
	private HashMap<String, WeatherAnimData> mMap = new HashMap<String, WeatherAnimData>();
	
	private final int[][] backgroundResIds = new int[][]{
		//小雨 background
		{R.drawable.day_rain_small_bg, R.drawable.night_rain_small_bg},
		// 阵雨
		{R.drawable.day_rain_small_bg, R.drawable.night_rain_small_bg},
		//中雨 background
		{R.drawable.day_rain_middle_bg, R.drawable.night_rain_middle_bg}, 
		//小到中雨
		{R.drawable.day_rain_middle_bg, R.drawable.night_rain_middle_bg},
		//冻雨
		{R.drawable.day_rain_middle_bg, R.drawable.night_rain_middle_bg},
		//大雨 background
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//中到大雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//大到暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//暴雨到大暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//大暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//大暴雨到特大暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//特大暴雨
		{R.drawable.day_rain_large_bg, R.drawable.night_rain_large_bg},
		//雷阵雨 background
		{R.drawable.dayrainthundershowerbg, R.drawable.nightrainthundershowerbg},
		//雷阵雨伴有冰雹
		{R.drawable.dayrainthundershowerbg, R.drawable.nightrainthundershowerbg},
		//阴 background
		{R.drawable.overcast_bg , R.drawable.overcast_night},
		//晴 background
		{R.drawable.sunny_morning_bg , R.drawable.sunny_noon_bg,R.drawable.sunny_afternoon_bg , R.drawable.sunny_night_bg},
		//小雪 background
		{R.drawable.day_snow_small_bg ,R.drawable.night_snow_small_bg},
		//阵雪
		{R.drawable.day_snow_small_bg ,R.drawable.night_snow_small_bg},
		//中雪 background
		{R.drawable.day_snow_middle_bg ,R.drawable.night_snow_middle_bg},
		//小到中雪
		{R.drawable.day_snow_middle_bg ,R.drawable.night_snow_middle_bg},
		//大雪 bg
		{R.drawable.day_snow_large_bg ,R.drawable.night_snow_large_bg},
		//中到大雪
		{R.drawable.day_snow_large_bg ,R.drawable.night_snow_large_bg},
		//大到暴雪
		{R.drawable.day_snow_large_bg ,R.drawable.night_snow_large_bg},
		//暴雪
		{R.drawable.day_snow_large_bg ,R.drawable.night_snow_large_bg},
		//雨夹雪 bg
		{R.drawable.day_rain_with_snow_bg ,R.drawable.night_rain_with_snow_bg},
		//多云
		{R.drawable.cloudy_day_bg ,R.drawable.cloudy_night_bg},
		//霾
		{R.drawable.haze_bg ,R.drawable.haze_night_bg},
		//雾
		{R.drawable.foggy_bg ,R.drawable.foggy_night_bg},
		//沙尘暴
		{R.drawable.duststorm_day_bg,R.drawable.duststorm_night_bg},
		//强沙尘暴
	    {R.drawable.duststorm_day_bg,R.drawable.duststorm_night_bg},
		//扬沙
		{R.drawable.sand_day_bg,R.drawable.sand_night_bg},
		//浮尘
		{R.drawable.sand_day_bg,R.drawable.sand_night_bg}
	    };
	
	private final int[][] mThumbs = new int[][]{
		//小雨 thumbs
		{R.drawable.day_rain_small_thumb , R.drawable.night_rain_small_thumb},
		//阵雨
		{R.drawable.day_rain_small_thumb , R.drawable.night_rain_small_thumb},
		//中雨 thumbs
		{R.drawable.day_rain_middle_thumb , R.drawable.night_rain_middle_thumb},
		//小到中雨
		{R.drawable.day_rain_middle_thumb , R.drawable.night_rain_middle_thumb},
		//冻雨
		{R.drawable.day_rain_middle_thumb , R.drawable.night_rain_middle_thumb},
		//大雨 thumbs
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//中到大雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//大到暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//暴雨到大暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//大暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//大暴雨到特大暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//特大暴雨
		{R.drawable.day_rain_large_thumb , R.drawable.night_rain_large_thumb},
		//雷阵雨 thumbs
		{R.drawable.day_rain_thundershower_thumb , R.drawable.night_rain_whenthundering_thumb},
		//雷阵雨伴有冰雹
		{R.drawable.day_rain_thundershower_thumb , R.drawable.night_rain_whenthundering_thumb},
		//阴
		{R.drawable.overcast_thumb , R.drawable.overcast_night_thumb},
		//晴
		{R.drawable.sunny_morning_thumb , R.drawable.sunny_night_thumb},
		//小雪
		{R.drawable.day_snow_small_thumb , R.drawable.night_snow_small_thumb},
		//阵雪
		{R.drawable.day_snow_small_thumb , R.drawable.night_snow_small_thumb},
		//中雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//小到中雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//大雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//中到大雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//大到暴雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//暴雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//雨夹雪
		{R.drawable.day_snow_middle_thumb , R.drawable.night_snow_middle_thumb},
		//多云
		{R.drawable.cloudy_day_thumb , R.drawable.cloudy_night_thumb},
		//霾
		{R.drawable.haze_thumb , R.drawable.haze_night_thumb},
		//雾
		{R.drawable.foggy_thumb , R.drawable.foggy_night_thumb},
		//沙尘暴
		{R.drawable.duststorm_day_thumb , R.drawable.duststorm_night_thumb},
		//强沙尘暴
		{R.drawable.duststorm_day_thumb , R.drawable.duststorm_night_thumb},
		//扬沙
		{R.drawable.sand_day_thumb , R.drawable.sand_night_thumb},
		//浮尘
		{R.drawable.sand_day_thumb , R.drawable.sand_night_thumb},
		};
	
	//private final int ANIM_NUMBER = 8;
	
	private final int[] mWidgetWeatherIcon = new int[] {
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_rain,
			R.drawable.widget_thundershower,
			R.drawable.widget_thundershower,
			R.drawable.widget_cloudy,
			R.drawable.widget_sunny,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_snow,
			R.drawable.widget_cloudy,
			R.drawable.widget_foggy,
			R.drawable.widget_foggy,
			R.drawable.widget_duststorm,
			R.drawable.widget_duststorm,
			R.drawable.widget_duststorm,
			R.drawable.widget_duststorm,
	};
	
	
	private final int[] mSmallWeatherIcon = new int[] {
			//晴
			R.drawable.s_sunny,
			//晴转多云
			R.drawable.s_sunny_to_cloudy,
			//晴转雾
			R.drawable.s_sunny_to_fog,
			//晴转雨
			R.drawable.s_sunny_to_rainy,
			//晴转雷阵雨
			R.drawable.s_sunny_to_thundershower,
			//晴转雪
			R.drawable.s_sunny_to_snow,
			//晴转沙尘
			R.drawable.s_sunny_to_dust,
			//多云
			R.drawable.s_cloudy,
			//多云转雾
			R.drawable.s_cloudy_to_fog,
			//多云转雨
			R.drawable.s_cloudy_to_rainy,
			//多云转雷阵雨
			R.drawable.s_cloudy_to_thundershower,
			//多云转雪
			R.drawable.s_cloudy_to_snow,
			//多云转沙尘
			R.drawable.s_cloudy_to_dust,
			//雾
			R.drawable.s_fog,
			//雾转雨
			R.drawable.s_fog_to_rainy,
			//雾转雷阵雨
			R.drawable.s_fog_to_thundershower,
			//雾转雪
			R.drawable.s_fog_to_snow,
			//雾转沙尘
			R.drawable.s_fog_to_dust,
			//雨
			R.drawable.s_rainy,
			//雨转雷阵雨
			R.drawable.s_rainy_to_thundershower,
			//雨转雪
			R.drawable.s_rainy_to_snow,
			//雨转沙尘
			R.drawable.s_rainy_to_dust,
			//雷阵雨
			R.drawable.s_thundershower,  
			//雷阵雨转雪
			R.drawable.s_thundershower_snow,
			//雷阵雨转沙尘
			R.drawable.s_thundershower_dust,
			//雪
			R.drawable.s_snow,
			//雪转沙尘
			R.drawable.s_snow_to_dust,
			//沙尘
			R.drawable.s_dust,
			//阵雨
			R.drawable.s_shower,
			//小雨
			R.drawable.s_small_rain,
			//中雨
			R.drawable.s_middle_rain,
			//大雨
			R.drawable.s_large_rain,
			//暴雨
			R.drawable.s_heavy_rain,
			//雷阵雨伴有冰雹
			R.drawable.s_thundershower_with_hail,
			//冻雨
			R.drawable.s_ice_rain,
			//霾
			R.drawable.s_haze,
			//扬尘
			R.drawable.s_raise_dust,
			//阴
			R.drawable.s_shade,
			//小雪
			R.drawable.s_small_snow,
			//中雪
			R.drawable.s_middle_snow,
			//大雪
			R.drawable.s_large_snow,
			//暴雪
			R.drawable.s_heavy_snow,
			//雨夹雪
			R.drawable.s_rain_with_snow,
			//阵雪
			R.drawable.s_snow_shower
		};
	
	public int getSmaillWeatherIcon(String weatherType,Context context){
		CityConditionAdapter weatherConditionAdapter = new CityConditionAdapter(context);
		weatherConditionAdapter.open();
		int index=weatherConditionAdapter.queryPicId(weatherType);
		if(index!=-1)
		{
			return mSmallWeatherIcon[index];
		}else{
			return R.drawable.smail_char_n;
		}
	}
	
	public WeatherAnimInfo(Context context)
	{
		setHashMapData(context);
	}
	
	private void setHashMapData(Context context) {
		
		String[] mWeatherAnimClassNames = context.getResources().getStringArray(R.array.weather_anim_class_name);
		
		String[] mWeatherTypes = context.getResources().getStringArray(R.array.weather_types);
		
		String[] mWidgetWeatherTypes = context.getResources().getStringArray(R.array.widget_weather_types);
		
		for( int i = 0; i < backgroundResIds.length; i++ ) {
			
			WeatherAnimData weatherAnimData = new WeatherAnimData();
			
			weatherAnimData.setClassName(mWeatherAnimClassNames[i]);
			weatherAnimData.setBackgroundResids(backgroundResIds[i]);
			weatherAnimData.setThumbID(mThumbs[i]);
			weatherAnimData.setWidgetWeatherType(mWidgetWeatherTypes[i]);
			weatherAnimData.setWidgetThumbID(mWidgetWeatherIcon[i]);
			
			mMap.put(mWeatherTypes[i], weatherAnimData);
		}
	}
	public String getWeatherAnimClassName(String type) {
		type = handleWeatherType(type);
		if (mMap.containsKey(type)) {
			return mMap.get(type).getClassName();
		}
		return null;
	}
	
	private String handleWeatherType(String type){
		if(type.contains("转"))
		{
			type = type.split("转")[1];
		}
		return type;
	}
	
	public int[] getBackgroundResIds(String type) {
		type = handleWeatherType(type);
		if (mMap.containsKey(type)) {
			return mMap.get(type).getBackgroundResids();
		}
		return null;
	}
	
	public int[] getThumbs(String type) {
		type = handleWeatherType(type);
		if (mMap.containsKey(type)) {
			return mMap.get(type).getThumbID();
		}
		return null;
	}
	
	public String getWidgetWeatherTypeString(String type) {
		type = handleWeatherType(type);
		if (type != null && mMap.containsKey(type)) {
			return mMap.get(type).getWidgetWeatherType();
		}
		return null;
	}
	
	public int getWidgetWeatherIcon(String type) {
		type = handleWeatherType(type);
		if (type != null && mMap.containsKey(type)) {
			return mMap.get(type).getWidgetThumbID();
		}
		return -1;
	} 
}
