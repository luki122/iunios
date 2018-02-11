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
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.entities.ThemeAudioBeanFromJson;
import com.aurora.thememanager.entities.ThemeBeanAudioFromJsonList;
import com.aurora.thememanager.entities.ThemeBeanFromJson;
import com.aurora.thememanager.entities.ThemeBeanFromJsonList;
import com.aurora.thememanager.utils.JsonMapUtils;

public class RingTongPaser extends ThemeFromInternetPaser{

	
	
	
	
	@Override
	public List<Object> readObject(JSONObject json) {
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {
			try {
				mCode = json.getInt("code");
				mRequestDesc =json.getString("desc");
				mTotalPage =json.getInt("totalPage");
				List<ThemeAudioBeanFromJson> themeBean = JSON.parseArray(json.getString("themes"), ThemeAudioBeanFromJson.class);
				if(themeBean != null && themeBean.size() > 0){
					for(ThemeAudioBeanFromJson bean:themeBean){
						Theme theme = readThemeAudioFromJson(bean);
						theme.themeId |= Theme.TYPE_RINGTONG;
						theme.type = Theme.TYPE_RINGTONG;
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
	
	protected Theme readThemeAudioFromJson(ThemeAudioBeanFromJson bean) {
		ThemeAudio theme = new ThemeAudio();
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
		theme.ringtongType = bean.type;
//		theme.previews = getPreviews(bean);
		return theme;
		
	}
	
	
	

}
