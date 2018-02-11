package com.aurora.thememanager.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeBeanFromJson;
import com.aurora.thememanager.entities.ThemeBeanFromJsonList;
import com.aurora.thememanager.entities.ThemeWallpaper;
import com.aurora.thememanager.utils.JsonMapUtils;

public class WallpaperPaser extends ThemeFromInternetPaser{
	
	@Override
	public List<Object> readObject(JSONObject json) {
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {
			try {
				mCode = json.getInt("code");
				mRequestDesc =json.getString("desc");
				mTotalPage =json.getInt("totalPage");
				List<ThemeBeanFromJson> themeBean = JSON.parseArray(json.getString("themes"), ThemeBeanFromJson.class);
				if(themeBean != null && themeBean.size() > 0){
					for(ThemeBeanFromJson bean:themeBean){
						Theme theme = readThemeFromJson(bean);
						theme.themeId |= Theme.TYPE_WALLPAPER;
						theme.type = Theme.TYPE_WALLPAPER;
						themes.add(theme);
					}
				}
				
			} catch (Exception e) {
				Log.d("parser", ""+e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return themes;
	}
	
	
	
	

}
