package com.aurora.note.report;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ReportUtil {

	public static final String MODULE_KEY = "150";

	public static final String TAG_ADD = "add";
	public static final String TAG_SHARE = "share";
	public static final String TAG_CHATS = "chats";
	public static final String TAG_MOMENTS = "moments";
	public static final String TAG_WEIBO = "weibo";
	public static final String TAG_GALLERY = "gallery";
	public static final String TAG_REMINDER = "reminder";
	public static final String TAG_TAG = "tag";
	public static final String TAG_IMAGE = "image";
	public static final String TAG_RECORD = "record";
	public static final String TAG_VIDEO = "video";
	public static final String TAG_BACKUP = "backup";
	public static final String TAG_RESTORE= "restore";
	public static final String TAG_CHATS_2 = "chats2";
	public static final String TAG_MOMENTS_2 = "moments2";
	public static final String TAG_WEIBO_2 = "weibo2";
	public static final String TAG_TOTA = "tota";

	private static final Uri CONTENT_URI = Uri.parse("content://com.iuni.reporter/module/");
	private static ContentResolver cr = null;

	private static ContentResolver getContentResolver(Context context) {
		if (cr == null) {
			cr = context.getContentResolver();
		}
		return cr;
	}

	public static int updateData(Context context, String itemTag, int value) {
		return updateData(context, MODULE_KEY, itemTag, value);
	}

	public static int updateData(Context context, String moduleKey, String itemTag, int value) {
		ContentValues values = new ContentValues();
		values.put("module_key", moduleKey);
		values.put("item_tag", itemTag);
		values.put("value", value);
		return getContentResolver(context).update(CONTENT_URI, values, null, null);
	}

}