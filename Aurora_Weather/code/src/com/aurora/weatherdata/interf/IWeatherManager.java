package com.aurora.weatherdata.interf;

import android.content.Context;

import com.aurora.weatherdata.implement.DataResponse;

import datas.WeatherDataEveryDay;

public interface IWeatherManager {

	public void getWeatherList(final DataResponse<WeatherDataEveryDay> response, final Context context,
			final String cityid, final String day, final boolean useCacheData);

	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();

}