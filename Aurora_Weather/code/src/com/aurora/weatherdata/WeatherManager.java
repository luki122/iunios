package com.aurora.weatherdata;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.weatherdata.bean.CacheItem;
import com.aurora.weatherdata.db.CacheDataAdapter;
import com.aurora.weatherdata.http.HttpRequestWeatherData;
import com.aurora.weatherdata.implement.Command;
import com.aurora.weatherdata.implement.DataResponse;
import com.aurora.weatherdata.interf.IWeatherManager;

import datas.WeatherDataEveryDay;

public class WeatherManager extends BaseManager implements IWeatherManager {

	private final String TAG = "WeatherManager";

	@Override
	public void getWeatherList(final DataResponse<WeatherDataEveryDay> response, final Context context,
			final String cityId, final String day, final boolean useCacheData) {
		mHandler.post(new Command<WeatherDataEveryDay>(context, response, this, useCacheData) {
			public void doRun() throws Exception {
				CacheDataAdapter cacheAdapter = new CacheDataAdapter(context);
				cacheAdapter.open();
				String result = null;
				if (useCacheData) {
					result = cacheAdapter.queryData(cityId);
				} else {
					result = HttpRequestWeatherData.getWeatherListObject(context, cityId, day);
					if (!TextUtils.isEmpty(result)&&isJsonObj(result)) {
						/*SharedPreferences pref = context.getSharedPreferences("com.aurora.weatherdata.cache",
								Context.MODE_APPEND);
						Editor editor = pref.edit();
						editor.putString("city_id", cityId);
						editor.commit();*/
						CacheItem item = new CacheItem();
						item.setCityId(cityId);
						item.setCityName("");
						item.setWeatherData(result);
						item.setUpdateTime(System.currentTimeMillis());

						cacheAdapter.deleteData(cityId);
						cacheAdapter.insert(item);
					}
				}
				cacheAdapter.close();
				setResponse(response, result, WeatherDataEveryDay.class);
			}
		});
	}

	private boolean isJsonObj(String result) throws JSONException{
			JSONObject jsObj = new JSONObject(result);
			return true;
	}
	
	
	@Override
	public void postActivity() {
		// TODO Auto-generated method stub
		/*
		 * if(failedRequests!=null){ failedRequests.clear(); }
		 */
		if (failedIORequests != null) {
			failedIORequests.clear();
		}
	}

}