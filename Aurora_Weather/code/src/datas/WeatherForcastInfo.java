package datas;

import android.util.Log;

/**
 * 预報天气
 * @author liuwei
 *
 */
public class WeatherForcastInfo {

	//预报日期
	private String mWeatherDate;

	//天气
	private String mWeatherType;

	//最低温度
	private float mLowTemp;

	//最高温度
	private float mHighTemp;

	public String getmWeatherDate() {
		return mWeatherDate;
	}

	public void setmWeatherDate(String mWeatherDate) {
		this.mWeatherDate = mWeatherDate;
	}

	public String getmWeatherType() {
		if(mWeatherType.contains("-"))
		{
			mWeatherType = mWeatherType.replace("-","转");
		}
		return mWeatherType;
	}

	public void setmWeatherType(String mWeatherType) {

		this.mWeatherType = mWeatherType;
	}

	public float getmLowTemp() {
		return mLowTemp;
	}

	public void setmLowTemp(float mLowTemp) {
		this.mLowTemp = mLowTemp;
	}

	public float getmHighTemp() {
		return mHighTemp;
	}

	public void setmHighTemp(float mHighTemp) {
		this.mHighTemp = mHighTemp;
	}

	public void print() {
		Log.i("likai", "WeatherForcastInfo: { 预报日期: " + getmWeatherDate() + ", 天气: " + getmWeatherType()
				+ ", 最低温度: " + getmLowTemp() + ", 最高温度: " + getmHighTemp() + "}");
	}

	public void setWeatherDate(String date)
	{
		mWeatherDate = date;
	}

	public String getWeatherDate()
	{
		return mWeatherDate;
	}

	public void setWeatherType(String type)
	{
		mWeatherType = type;
	}

	public String getWeatherType()
	{
		if(mWeatherType.contains("-"))
		{
			mWeatherType = mWeatherType.replace("-","转");
		}
		return mWeatherType;
	}

	public void setLowTemp(float lowTemp)
	{
		mLowTemp = lowTemp;
	}

	public float getLowTemp()
	{
		return mLowTemp;
	}

	public void setHighTemp(float highTemp)
	{
		mHighTemp = highTemp;
	}

	public float getHighTemp()
	{
		return mHighTemp;
	}

}