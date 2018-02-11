package com.aurora.calendar.report;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ReportUtil {

	public static final String MODULE_KEY = "170";

	public static final String TAG_LUNAR = "lunar";
	public static final String TAG_STARTDAY = "startday";
	public static final String TAG_ADDEVENT = "addevent";

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