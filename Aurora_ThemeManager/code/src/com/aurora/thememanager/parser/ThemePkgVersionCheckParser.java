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
import com.aurora.thememanager.entities.ThemeBeanFromJson;
import com.aurora.thememanager.entities.ThemeBeanFromJsonList;
import com.aurora.thememanager.utils.JsonMapUtils;


public class ThemePkgVersionCheckParser extends JsonParser {


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
							theme.themeId = js.getInt("id");
							theme.versionCode = (float) js.getDouble("versionCode");
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
