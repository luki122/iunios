package com.aurora.downloadIcon.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aurora.downloadIcon.bean.IconResponseObject;
import com.aurora.downloadIcon.bean.IconResponseProp;

import android.content.Context;

public class SystemUtils {
	private static String TAG = "SystemUtils";

	private SystemUtils() {
	}

	
	public static String getFromAssets(Context mcontent, String fileName) {
		StringBuffer sb = new StringBuffer();
		try {
			InputStreamReader inputReader = new InputStreamReader(mcontent
					.getResources().getAssets().open(fileName), "GB2312");
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";

			while ((line = bufReader.readLine()) != null)
				sb.append(line);
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static IconResponseObject readJson2Array(String str){
		ObjectMapper mapper = new ObjectMapper();
		IconResponseObject items = null;
		try {
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);
			items = mapper.readValue(str, IconResponseObject.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items;
	}
}
