package datas;

import android.util.Log;

public class WeatherAirQualities {

	private String airQualityDesc;

	private float airQualityValue;

	public String getAirQualityDesc() {
		return airQualityDesc;
	}

	public void setAirQualityDesc(String airQualityDesc) {
		this.airQualityDesc = airQualityDesc;
	}

	public float getAirQualityValue() {
		return airQualityValue;
	}

	public void setAirQualityValue(float airQualityValue) {
		this.airQualityValue = airQualityValue;
	}

	public void print() {
		Log.i("likai", "WeatherAirQualities: { airQualityDesc: " + getAirQualityDesc() + ", airQualityValue: "
				+ getAirQualityValue() + "}");
	}

	public void setAirDesc(String desc)
	{
		airQualityDesc = desc;
	}

	public String getAirDesc()
	{
		return airQualityDesc;
	}

	public void setAirValue(float value)
	{
		airQualityValue = value;
	}

	public float getAirValue()
	{
		return airQualityValue;
	}

	//we just call this method to show !!!!
	public String getAirInfomation()
	{
		return (airQualityDesc + "   " + airQualityValue);
	}

}