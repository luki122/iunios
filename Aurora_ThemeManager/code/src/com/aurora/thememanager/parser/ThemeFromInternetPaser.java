package com.aurora.thememanager.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeBeanFromJson;
import com.aurora.thememanager.entities.ThemeBeanFromJsonList;
import com.aurora.thememanager.utils.JsonMapUtils;

public class ThemeFromInternetPaser extends JsonParser{

	
	
	
	
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
						theme.themeId |= Theme.TYPE_THEME_PKG;
						theme.type = Theme.TYPE_THEME_PKG;
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
	
	
	protected String[] getPreviews(ThemeBeanFromJson bean){
		String[] previews = null;
		try{
		String orgStr = bean.previews.toString();
		
		 Pattern pattern = Pattern.compile("http://[\\w\\.\\-/:]+");  
		 Matcher matcher = pattern.matcher(orgStr);  
		 ArrayList<String> urls = new ArrayList<String>();
		 while(matcher.find()){ 
			 urls.add(matcher.group());
			 }  
		 previews = new String[urls.size()];
		 previews =  urls.toArray(previews);
		return previews;
		}catch(Exception e){
			
			return null;
		}
		
	}
	
	

}
