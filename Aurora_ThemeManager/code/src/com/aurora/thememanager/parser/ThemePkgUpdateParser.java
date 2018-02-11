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
import com.aurora.thememanager.utils.JsonMapUtils;


public class ThemePkgUpdateParser extends ThemeFromInternetPaser {

	
	@Override
	public List<Object> readObject(JSONObject json) {
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {
			try {
			JSONObject myJson = json.getJSONObject("themes");
			ThemeBeanFromJson themeBean = JSON.parseObject(myJson.toString(), ThemeBeanFromJson.class);
			if(themeBean == null){
				return themes;
			}
			
				mCode = json.getInt("code");
				mRequestDesc = json.getString("desc");
						Theme theme = readThemeFromJson(themeBean);
						theme.themeId |= Theme.TYPE_THEME_PKG;
						theme.type = Theme.TYPE_THEME_PKG;
						themes.add(theme);
				
			} catch (Exception e) {
				Log.d("parser", ""+e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return themes;
	}
	
	protected Theme readThemeFromJson(ThemeBeanFromJson bean){
		Theme theme = new Theme();
		theme.name =bean.name;
		theme.author =bean.author;
		theme.authorIcon =  bean.authorIcon;
		theme.sizeStr =bean.themeSize;
		theme.themeId = bean.id;
		theme.versionCode =  bean.versionCode;
		theme.description =bean.desc;
		theme.preview = bean.preview;
		theme.downloadPath =bean.downloadUrl;
		theme.createTime =bean.createTime;
		theme.hasSoundEffect = bean.ringingInfo;
		theme.previews = getPreviews(bean);
		return theme;
	}

	
}
