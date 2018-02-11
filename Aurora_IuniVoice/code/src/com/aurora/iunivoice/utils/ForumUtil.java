package com.aurora.iunivoice.utils;

import java.util.ArrayList;

import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.content.SharedPreferences;

import com.aurora.datauiapi.data.bean.ForumData;
import com.aurora.datauiapi.data.bean.ForumInfo;

public class ForumUtil {

	public static final String TAG = "ForumUtil";
	public static final String FORUM_PREFERENCES = "forum_preferences";
	public static final String FORUM_DATA = "forum_data";

	public static ArrayList<ForumInfo> getForumList(Context context) {
		SharedPreferences sp = context.getSharedPreferences(FORUM_PREFERENCES, Context.MODE_PRIVATE);
		String forumDataStr = sp.getString(FORUM_DATA, null);
		if (forumDataStr != null) {
			ForumData forumData = readValue(forumDataStr);
			if (forumData != null) {
				return forumData.getData().getForumlist();
			}
		}

		return null;
	}

	public static void saveForumData(Context context, ForumData forumData) {
		if (forumData == null) return;
		if (context == null) return;

		String forumDataStr = writeValue(forumData);
		if (forumDataStr != null) {
			SharedPreferences sp = context.getSharedPreferences(FORUM_PREFERENCES, Context.MODE_PRIVATE);
			sp.edit().putString(FORUM_DATA, forumDataStr).commit();
		}
	}

	private static ForumData readValue(String forumDataStr) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		mapper.getDeserializationConfig().set(
				org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ForumData forumData = null;
		try {
			forumData = mapper.readValue(forumDataStr, ForumData.class);
		} catch (Exception e) {
			forumData = null;
			Log.e(TAG, "readValue failed");
		}

		return forumData;
	}

	private static String writeValue(ForumData forumData) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		mapper.getDeserializationConfig().set(
				org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String forumDataStr = null;
		try {
			forumDataStr = mapper.writeValueAsString(forumData);
		} catch (Exception e) {
			forumDataStr = null;
			Log.e(TAG, "writeValue failed");
		}

		return forumDataStr;
	}

}
