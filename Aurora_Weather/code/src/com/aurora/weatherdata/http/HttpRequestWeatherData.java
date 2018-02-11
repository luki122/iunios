package com.aurora.weatherdata.http;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.util.Log;

import com.aurora.weatherdata.util.Globals;
import com.aurora.weatherdata.util.SystemUtils;

public class HttpRequestWeatherData {

	private static final String TAG = "HttpRequestWeatherData";

	public static String getWeatherListObject(Context context, String cityId, String day) throws Exception {
		/*StringWriter writer = new StringWriter();

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cityid", cityId);
			map.put("day", day);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(writer, map);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}*/

		String param = "cityid=" + cityId + "&day=" + day;
		String url = Globals.HTTP_REQUEST_URL + param;
		Log.i("likai", "url = " + url);

		String jasonStr = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			jasonStr = SystemUtils.getFromAssets(context, "weather.json");
		} else {
			jasonStr = HttpRequstData.doRequest(url);
		}
		Log.i("likai", "jasonStr = " + jasonStr);
		return jasonStr;
	}

}