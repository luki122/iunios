//Gionee:huangzy 20120823 add for CR00614805
package com.android.providers.contacts.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class GnHotLinesUtil {
	/** The authority for the contacts provider */
    public static final String AUTHORITY = "com.gionee.hotlines";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    
    public static final Uri INIT_HOT_LINES_URI = Uri.withAppendedPath(BASE_URI, "init");
    public static final Uri HOT_LINES_URI = Uri.withAppendedPath(BASE_URI, "search");
    public static final Uri T9_SEARCH_HOT_LINES_URI = Uri.withAppendedPath(BASE_URI, "t9_search");
    public static final Uri HOT_LINES_DISPLAY_PHOTO = Uri.withAppendedPath(BASE_URI, "display_photo");
    
    //Copy from HotLinesProvider
	public interface T9SearchRetColumns {
		String ID = BaseColumns._ID;
		String NAME = "name";
		String NUMBER = "number";
		String NAME_PINYIN = "name_pinyin";
		String PHOTO_NAME = "photo_name";
		String DATA_HIGH_LIGHT = "data_highlight_offset";
		String PINYIN_HIGH_LIGHT = "pinyin_highlight_offset";

		String[] COLUMN_NAMES = { ID, NAME, NUMBER, NAME_PINYIN,
				PHOTO_NAME, PINYIN_HIGH_LIGHT, DATA_HIGH_LIGHT};

		int ID_INDEX = 0;
		int NAME_INDEX = 1;
		int NUMBER_INDEX = 2;
		int NAME_PINYIN_INDEX = 3;
		int PHOTO_NAME_INDEX = 4;
		int DATA_HIGH_LIGHT_INDEX = 5;
		int PINYIN_HIGH_LIGHT_INDEX = 6;
	}
    
    public static String[] getInfo(Context context, String hotlineNumber) {
    	String[] info = new String[2];
		Cursor hotlinesCursor = context.getContentResolver().query(HOT_LINES_URI, 
				new String[]{T9SearchRetColumns.NAME, T9SearchRetColumns.PHOTO_NAME},
				T9SearchRetColumns.NUMBER + "='" + hotlineNumber + "'", null, T9SearchRetColumns.ID + " LIMIT 1");
        if (null != hotlinesCursor) {
            if (hotlinesCursor.moveToFirst()) {
                info[0] = hotlinesCursor.getString(0);
                info[1] = hotlinesCursor.getString(1);
            } else {
                info = null;
            }
            hotlinesCursor.close();
        }

        return info;
    }
    

}
