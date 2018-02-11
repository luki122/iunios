package com.aurora.mediascanner;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class ScannerUtils {
	
	
	/**
     * action for external scanning
     */
    public final static String ACTION_EXT_SCAN = "android.intent.action.AURORA_EXTERNAL_SCAN";
    
    /**
     * action for directory scanning
     */
    public final static String ACTION_DIR_SCAN = "android.intent.action.AURORA_DIRECTORY_SCAN";
    
    /**
     * action for file scanning
     */
    public final static String ACTION_FILE_SCAN = "android.intent.action.AURORA_FILE_SCAN";
    
    
    

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
