package com.android.aurora;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ScannerUtils {

	public static boolean isStatistics=true;
	
	private static final Uri uriStatistics = Uri
			.parse("content://com.iuni.reporter/module/");
	private static final String MODULEKEY = "module_key";
	private static final String ITEMTAG = "item_tag";
	private static final String value = "value";
	
	public static void  updateStatistics(Context context) {
		if (!isStatistics) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(MODULEKEY, "250");
		values.put(ITEMTAG, "009");
		values.put(value, 1);
		context.getContentResolver().update(uriStatistics, values, null, null);
	}
}
