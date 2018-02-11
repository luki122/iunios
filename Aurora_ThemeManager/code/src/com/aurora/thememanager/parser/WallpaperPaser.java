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
import com.aurora.thememanager.entities.ThemeWallpaper;

public class WallpaperPaser extends JsonParser{
	
	@Override
	public List<Object> readObject(JSONObject json) {
		ArrayList<Object> themes = new ArrayList<Object>();
		if (json != null) {

			try {
				mCode = json.getInt("code");
				mRequestDesc = json.getString("desc");
				mTotalPage = json.getInt("totalPage");
				JSONArray array = json.getJSONArray("themes");
				if (array != null) {
						int length = array.length();
						for(int i = 0;i < length;i++){
							JSONObject js = (JSONObject) array.opt(i);
							Theme theme = new ThemeWallpaper();
							theme.author =  js.getString("author");
							theme.themeId =  js.getInt("id");
							theme.downloadPath =js.getString("downloadUrl");
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
								theme.preview = theme.previews[0];
							}
							theme.themeId |= Theme.TYPE_WALLPAPER;
							theme.type = Theme.TYPE_WALLPAPER;
							themes.add(theme);
						}
				}
			} catch (JSONException e) {
				Log.d("parser", ""+e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return themes;
	}
	
	
	
	

}
