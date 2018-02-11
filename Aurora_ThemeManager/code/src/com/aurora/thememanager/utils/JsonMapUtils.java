package com.aurora.thememanager.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.utils.Log;

public class JsonMapUtils {


	
	
	private static final String TAG = "JsonMapUtils";

	public static Map<String, Object> parseJSON2Map(String jsonStr){
	    Map<String, Object> map = new HashMap<String, Object>();
	    //最外层解析
	    try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator<String> keyIterator = json.keys();
			while(keyIterator.hasNext()){
				String key = keyIterator.next();
				Object value = json.get(key);
				if(value instanceof JSONArray){
					List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
					JSONArray array = (JSONArray)value;
					int size = array.length();
					for(int index = 0; index < size;index++){
						JSONObject v = array.getJSONObject(index);
						list.add(parseJSON2Map(v.toString()));
					}
					map.put(key, list);
				}else{
					map.put(key, value);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, ""+e);
		}
	    return map;
	  }
	
	
	
	
	
	
	
	
	
	/**
     * 将json转化为实体POJO
     * @param jsonStr
     * @param obj
     * @return
     */
    public static Object JSONToObj(String jsonStr,Class obj) {
        Object t = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            t = objectMapper.readValue(jsonStr,
                    obj);
        } catch (Exception e) {
        	Log.d("js", ""+e);
            e.printStackTrace();
        }
        return t;
    }
     
    /**
     * 将实体POJO转化为JSON
     * @param obj
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static<T> JSONObject objectToJson(T obj) throws JSONException, IOException {
        ObjectMapper mapper = new ObjectMapper();  
        // Convert object to JSON string  
        String jsonStr = "";
        try {
             jsonStr =  mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw e;
        }
        return new JSONObject(jsonStr);
    }
	
	
	
	
	
	
	
}
