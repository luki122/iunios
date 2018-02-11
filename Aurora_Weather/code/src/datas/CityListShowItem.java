package datas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.aurora.weatherforecast.R;

public class CityListShowItem {

	private int resId = R.drawable.weather_thumb_default;
	private String lowAndHighTempStr;
	private String curTemp="";
	private String weahterType = "";
	private String cityName = "";
	private int cityId = 0;
	private int bigcirBgRes = R.drawable.sunny_morning_bg;

	private String windContent="";
	
	private List<Calendar> forcastDate=new ArrayList<Calendar>();
	
	
	public List<Calendar> getForcastDate() {
		return forcastDate;
	}

	public void setForcastDate(List<Calendar> forcastDate) {
		this.forcastDate = forcastDate;
	}

	public String getWindContent() {
		return windContent;
	}

	public void setWindContent(String windContent) {
		this.windContent = windContent;
	}

	public String getmHumidityContent() {
		return mHumidityContent;
	}

	public void setmHumidityContent(String mHumidityContent) {
		this.mHumidityContent = mHumidityContent;
	}


	public String getAirQualityDesc() {
		return airQualityDesc;
	}

	public void setAirQualityDesc(String airQualityDesc) {
		this.airQualityDesc = airQualityDesc;
	}

	public String getAirQualityValueDes() {
		return airQualityValueDes;
	}

	public void setAirQualityValueDes(String airQualityValueDes) {
		this.airQualityValueDes = airQualityValueDes;
	}

	private String mHumidityContent;
	private String airQualityDesc;
	private String airQualityValueDes;
	
	private int[] forcastWeatherThumb;
	public String[] getForcastWeatheTem() {
		return forcastWeatheTem;
	}

	public void setForcastWeatheTem(String[] forcastWeatheTem) {
		this.forcastWeatheTem = forcastWeatheTem;
	}

	private String[] forcastWeatheTem;
	public int[] getForcastWeatherThumb() {
		return forcastWeatherThumb;
	}

	public void setForcastWeatherThumb(int[] weatherThumbRes) {
		this.forcastWeatherThumb = weatherThumbRes;
	}

	public int getBigcirBgRes() {
		return bigcirBgRes;
	}

	public void setBigcirBgRes(int bigcirBgRes) {
		this.bigcirBgRes = bigcirBgRes;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public CityListShowItem(Context context) {
		super();
		fillDefaultValue(context);
	}

	private boolean hasAirQuality=true;
	
	public boolean isHasAirQuality() {
		return hasAirQuality;
	}

	public void setHasAirQuality(boolean hasAirQuality) {
		this.hasAirQuality = hasAirQuality;
	}

	private final static int MAX_DAY=6;
	private void fillDefaultValue(Context context){
		Calendar c=Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1);
		forcastWeatherThumb=new int[MAX_DAY];
		for(int i=0;i<MAX_DAY;i++)
		{
			forcastDate.add(c);
			c.add(Calendar.DAY_OF_MONTH, 1);
			forcastWeatherThumb[i]=R.drawable.smail_char_n;
		}
		forcastWeatheTem=context.getResources().getStringArray(R.array.weather_temperature_forcast);
		airQualityDesc=context.getString(R.string.airQualityDesc_defalut);
		airQualityValueDes=context.getString(R.string.airQualityValue_value);
		windContent=context.getString(R.string.mWindDirection_default);
		weahterType=context.getString(R.string.weather_default);
		curTemp=context.getString(R.string.default_temperature);
		mHumidityContent=context.getString(R.string.mHumidity_default);
		lowAndHighTempStr=context.getString(R.string.default_temperature);
		cityName=context.getString(R.string.loading);
		
	}
	
	
	
	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getLowAndHighTempStr() {
		return lowAndHighTempStr;
	}

	public void setLowAndHighTempStr(String lowAndHighTempStr) {
		this.lowAndHighTempStr = lowAndHighTempStr;
	}

	public String getCurTemp() {
		return curTemp;
	}

	public void setCurTemp(String curTemp) {
		this.curTemp = curTemp;
	}

	public String getWeahterType() {
		return weahterType;
	}

	public void setWeahterType(String weahterType) {
		this.weahterType = weahterType;
	}

	public void notifyChangeRes() {

	}
}
