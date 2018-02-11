package com.aurora.thememanager.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.aurora.thememanager.entities.Theme;


public class ThemeTimeWallpaperDetailPaser extends JsonParser {

	@Override
	public List<Object> readObject(JSONObject json) {
		// TODO Auto-generated method stub
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {
			try {
				mCode = json.getInt("code");
				mRequestDesc = json.getString("desc");
				JSONObject js = json.getJSONObject("themes");
				if (js != null) {
							Theme theme = new Theme();
							theme.downloadPath = js.getString("downloadUrl");
							JSONObject previews =  js.getJSONObject("previews");
							if(previews != null){
								int previewCount = previews.length();
								theme.previews = new String[previewCount];
								Iterator<String> keys = previews.keys();
								int index = 0;
								while(keys.hasNext()){
									String key = keys.next();
									if(!TextUtils.isEmpty(key)){
										theme.previews[index] = previews.getString(key);
										index ++;
									}
								}
							}
							themes.add(theme);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("Parser", ""+e);
			}
		}
		return themes;
	}

	
	

	
	
}
