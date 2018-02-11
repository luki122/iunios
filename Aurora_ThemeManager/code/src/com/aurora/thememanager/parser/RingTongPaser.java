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
import com.aurora.thememanager.entities.ThemeAudio;

public class RingTongPaser extends JsonParser{

	
	
	
	
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
							ThemeAudio theme = new ThemeAudio();
							theme.author =  js.getString("author");
							//Log.e("101010", "---RingTongPaser readObject theme.downloadUrl = --------" + theme.downloadPath);
							theme.themeId =  js.getInt("id");
							theme.downloadPath =js.getString("downloadUrl");
							theme.ringtongType = js.getInt("type");
							theme.name = js.getString("name");
							//Log.e("101010", "---RingTongPaser readObject theme.name = --------" + theme.name);
							theme.themeId |= Theme.TYPE_RINGTONG;
							theme.type = Theme.TYPE_RINGTONG;
							themes.add(theme);
						}
				}
			} catch (JSONException e) {
				Log.d("parser", ""+e);
				Log.e("101010", "---RingTongPaser JSONException e--------" + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return themes;
	}
	
	
	
	

}
