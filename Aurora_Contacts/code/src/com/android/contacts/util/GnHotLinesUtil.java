//Gionee:huangzy 20120823 add for CR00614805
package com.android.contacts.util;

import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
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
    
	//Gionee:huangzy 20121011 modify for CR00710695 start
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
	//Gionee:huangzy 20121011 modify for CR00710695 end
    
    public static String[] getInfo(Context context, String hotlineNumber) {
    	String[] info = new String[2];
		Cursor hotlinesCursor = context.getContentResolver().query(HOT_LINES_URI, 
				new String[]{T9SearchRetColumns.NAME, T9SearchRetColumns.PHOTO_NAME},
				T9SearchRetColumns.NUMBER + "='" + hotlineNumber + "'", null, T9SearchRetColumns.ID + " LIMIT 1");
        // gionee lwzh add for CR00680514 20120825 begin
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
        // gionee lwzh add for CR00680514 20120825 end
    }
    
    public static Cursor tgSearch(Context context, String t9SearchKey) {
    	Uri uri = Uri.withAppendedPath(T9_SEARCH_HOT_LINES_URI, t9SearchKey);
		Cursor hotlinesCursor = context.getContentResolver().query(uri, null,
                null, null, null);
		
		return hotlinesCursor;
    }
	
	//Gionee:huangzy 20121011 add for CR00710695 start
    public static Cursor compineHotLinesRet(Cursor src, Cursor hotLines) {
    	if (null == hotLines) {
    		return src;
    	} else if (hotLines.getCount() <= 0 || !hotLines.moveToFirst()) {
    		hotLines.close();
    		return src;
    	}
    	
    	MatrixCursor retCursor = new MatrixCursor(GnDialerSearchResultColumns.COLUMN_NAMES);
    	int len = GnDialerSearchResultColumns.COLUMN_NAMES.length;
    	Object[] raw = new Object[len];
    	for (int i = 0; i < len; i++) {
			raw[i] = null;
		}
    	if (null != src) {
    		if (src.moveToFirst() && src.getCount() > 0) {
    			do {
        			for (int i = 0; i < len-4; i++) {
        				raw[i] = src.getString(i);
        			}
        			retCursor.addRow(raw);
        		} while(src.moveToNext());	
    		}
    		
    		src.close();
    	}
    	
    	raw = new Object[len];
    	for (int i = 0; i < len; i++) {
			raw[i] = null;
		}
    	if (null != hotLines) {
    		if (hotLines.moveToFirst()  && hotLines.getCount() > 0) {
    			int id = 0;
        		char[] dataHighLight = new char[2];
        		dataHighLight[0] = 0;
        		do {
        			raw[GnDialerSearchResultColumns.CONTACT_ID_INDEX] = (--id);        			
        			raw[GnDialerSearchResultColumns.NAME_INDEX] = hotLines.getString(T9SearchRetColumns.NAME_INDEX);
        			raw[GnDialerSearchResultColumns.PHONE_NUMBER_INDEX] = hotLines.getString(T9SearchRetColumns.NUMBER_INDEX);    			
        			raw[GnDialerSearchResultColumns.DATA_HIGHLIGHT_INDEX] = hotLines.getString(T9SearchRetColumns.DATA_HIGH_LIGHT_INDEX);
        			raw[GnDialerSearchResultColumns.PINYIN_INDEX] = hotLines.getString(T9SearchRetColumns.NAME_PINYIN_INDEX);
        			raw[GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX] = hotLines.getString(T9SearchRetColumns.PINYIN_HIGH_LIGHT_INDEX);
        			raw[GnDialerSearchResultColumns.PHOTO_ID_INDEX] = hotLines.getString(T9SearchRetColumns.PHOTO_NAME_INDEX);
        			retCursor.addRow(raw);
        			
        		} while(hotLines.moveToNext());    			
    		}
    		hotLines.close();
    	}
    	return retCursor;
    }
	//Gionee:huangzy 20121011 add for CR00710695 end
}
