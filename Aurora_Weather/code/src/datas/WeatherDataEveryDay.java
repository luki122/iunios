package datas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.util.Log;

public class WeatherDataEveryDay {

	public final int MAX_FORCAST_DAY = 6;

	private String code;

	private String desc;

	private String mCity;

	private List<WeatherForcastInfo> weatherForcecasts = Collections.synchronizedList(new ArrayList<WeatherForcastInfo>());

	private List<WeatherHourInfo> mWeatherDataEveryDays =Collections.synchronizedList(new ArrayList<WeatherHourInfo>());

	private List<WeatherAirQualities> weatherAirQualities = Collections.synchronizedList(new ArrayList<WeatherAirQualities>());

	private List<WeatherWarningInfo> weatherWarningList = Collections.synchronizedList(new ArrayList<WeatherWarningInfo>());

	public List<WeatherWarningInfo> getWeatherWarningList() {
		return weatherWarningList;
	}

	public void setWeatherWarningList(List<WeatherWarningInfo> weatherWarningList) {
		this.weatherWarningList = weatherWarningList;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getmCity() {
		return mCity;
	}

	public void setmCity(String mCity) {
		this.mCity = mCity;
	}

	public List<WeatherForcastInfo> getWeatherForcecasts() {
		return weatherForcecasts;
	}

	public void setWeatherForcecasts(ArrayList<WeatherForcastInfo> weatherForcecasts) {
		this.weatherForcecasts = weatherForcecasts;
	}

	public List<WeatherHourInfo> getmWeatherDataEveryDays() {
		return mWeatherDataEveryDays;
	}

	public void setmWeatherDataEveryDays(ArrayList<WeatherHourInfo> mWeatherDataEveryDays) {
		this.mWeatherDataEveryDays = mWeatherDataEveryDays;
	}

	public List<WeatherAirQualities> getWeatherAirQualities() {
		return weatherAirQualities;
	}

	public void setWeatherAirQualities(ArrayList<WeatherAirQualities> weatherAirQualities) {
		this.weatherAirQualities = weatherAirQualities;
	}

	public void setWeatherForcastInfo(WeatherForcastInfo[] info) {
		weatherForcecasts.clear();
		for (int i = 0; i < info.length; i++) {
			weatherForcecasts.add(info[i]);
		}
	}

	public WeatherForcastInfo[] getWeatherForcastInfo() {
		WeatherForcastInfo mForcastInfo[] = new WeatherForcastInfo[weatherForcecasts.size()];
		return weatherForcecasts.toArray(mForcastInfo);
	}

	public void setHourInfo(WeatherHourInfo info) {
		mWeatherDataEveryDays.clear();
		mWeatherDataEveryDays.add(info);
	}

	public WeatherHourInfo getHourInfo() {
		if (mWeatherDataEveryDays.size() > 0) {
			return mWeatherDataEveryDays.get(0);
		}

		return null;
	}

	public WeatherAirQualities getWeatherAirQuality() {
		if (weatherAirQualities.size() > 0) {
			return weatherAirQualities.get(0);
		}

		return null;
	}

	public void setWeatherAirQuality(WeatherAirQualities weatherAirQuality) {
		weatherAirQualities.clear();
		weatherAirQualities.add(weatherAirQuality);
	}

}