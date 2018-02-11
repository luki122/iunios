package datas;

import android.util.Log;

/**
 * 实时天气
 * @author liuwei
 *
 */
public class WeatherHourInfo {

	//天气类型
	private String mWeatherType;

	//天气日期
	private String mWeatherDate;

	//风的方向
	private String mWindDirection;

	//当前温度
	private float mCurTemp;

	//最低温度
	private float mLowTemp;

	//最高温度
	private float mHighTemp;

	//风的等级
	private String mWindLevel;

	//湿度
	private String mHumidity;

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

	public String getmWeatherDate() {
		return mWeatherDate;
	}

	public void setmWeatherDate(String mWeatherDate) {
		this.mWeatherDate = mWeatherDate;
	}

	public String getmWindDirection() {
		return mWindDirection;
	}

	public void setmWindDirection(String mWindDirection) {
		this.mWindDirection = mWindDirection;
	}

	public float getmCurTemp() {
		return mCurTemp;
	}

	public void setmCurTemp(float mCurTemp) {
		this.mCurTemp = mCurTemp;
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

	public String getmWindLevel() {
		return mWindLevel;
	}

	public void setmWindLevel(String mWindLevel) {
		this.mWindLevel = mWindLevel;
	}

	public String getmHumidity() {
		return mHumidity;
	}

	public void setmHumidity(String mHumidity) {
		this.mHumidity = mHumidity;
	}

	public void print() {
		Log.i("likai", "WeatherForcastInfo: { 天气类型: " + getmWeatherType() + ", 天气日期: " + getmWeatherDate()
				+ ", 风的方向: " + getmWindDirection() + ", 风的等级: " + getmWindLevel()
				+ ", 当前温度: " + getmCurTemp() + ", 湿度: " + getmHumidity()
				+ ", 最低温度: " + getmLowTemp() + ", 最高温度: " + getmHighTemp() + "}");
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

	public void setWeatherDate(String date)
	{
		mWeatherDate = date;
	}

	public String getWeatherDate()
	{
		return mWeatherDate;
	}

	public void setWindDirection(String direction)
	{
		mWindDirection = direction;
	}

	public String getWindDirection()
	{
		return mWindDirection;
	}

	public void setCurTemp(float curTemp)
	{
		mCurTemp = curTemp;
	}

	public float getCurTemp()
	{
		return mCurTemp;
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

	public void setHumidity(String humidity)
	{
		mHumidity = humidity;
	}

	public String getHumidity()
	{
		return mHumidity;
	}

}