package com.aurora.thememanager.entities;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.utils.Globals;
import com.aurora.thememanager.utils.SystemUtils;

public class HttpRequestSearchData {
	private static final String TAG = "HttpRequestSearchData";

	// 搜索应用接口
	public static String getSearchAppListObject(String query,
			int page, int count) throws Exception{
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		String model = SystemUtils.getBuildProproperties("ro.product.model");
		model = model.replace(" ", "%20");
		model = model.replace("+", "%2B");
		String vernumber = SystemUtils.getBuildProproperties("ro.gn.iuniznvernumber");
		String romDate = vernumber.substring(vernumber.lastIndexOf("-") + 1);
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("query", HttpRequstData.getDecodeStr(query));
			map.put("model", model);
			map.put("romDate", romDate);
			map.put("count", count);
			map.put("page", page);

			mapper.writeValue(str, map);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NAME_APPLIST,
				Globals.HTTP_SEARCHAPPLIST_METHOD);
		url += Globals.HTTP_ACTION_PARAM+str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(ThemeManagerApplication.getInstance(), "searchApp.json");

		} else {
				returnData = HttpRequstData.doRequest(url);

		}
		return returnData;
	}
	// 搜索实时接口
		public static String getSearchTimeLyObject(String query) throws Exception{
			StringWriter str = new StringWriter();
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				ObjectMapper mapper = new ObjectMapper();
				map.put("query", HttpRequstData.getDecodeStr(query));
				mapper.writeValue(str, map);
			} catch (JsonParseException e) {
				e.printStackTrace();
				return null;
			} catch (JsonMappingException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
					Globals.HTTP_SERVICE_NAME_SEARCHRECLIST,
					Globals.HTTP_SEARCHSUGGEST_METHOD);
			url += Globals.HTTP_ACTION_PARAM+str.toString();

			String returnData = new String();
			if (Globals.isTestData) {
				// 先使用模拟数据
				returnData = SystemUtils.getFromAssets(ThemeManagerApplication.getInstance(), "searchApp.json");

			} else {
					returnData = HttpRequstData.doRequest(url);

			}
			return returnData;
		}
	// 搜索推荐接口
	public static String getSearcjRecObject() throws Exception{
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NAME_SEARCHRECLIST,
				Globals.HTTP_SEARCHRECLIST_METHOD);

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(ThemeManagerApplication.getInstance(), "searchRec.json");

		} else {
				returnData = HttpRequstData.doRequest(url);		
		}
		return returnData;
	}

}
