package com.aurora.thememanager.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.aurora.thememanager.entities.Theme;


public class ThemeJsonInfoPaser extends JsonParser {

	@Override
	public List<Object> readObject(JSONObject json) {
		// TODO Auto-generated method stub
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {

			try {
				JSONArray array = json.getJSONArray("theme");
				if (array != null) {
						int length = array.length();
						for(int i = 0;i < length;i++){
							JSONObject js = (JSONObject) array.opt(i);
							Theme theme = new Theme();
							theme.name = js.getString("name");
							theme.author =  js.getString("author");
							theme.version =  js.getString("versionCode");
							theme.description = js.getString("desc");
							theme.soundEffect = js.getBoolean("ringingInfo");
							theme.hasSoundEffect = String.valueOf(theme.soundEffect);
							themes.add(theme);
						}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
			}
		}
		return themes;
	}

	
	

	
	
}
