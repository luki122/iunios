package com.android.incallui;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;

public class NoteUtils {
	
	public static String getNote(Context context, long rawContactId){
		if(rawContactId > 0) {
			String re = "";
			Log.v("NoteUtils", " getNote rawContactId = " + rawContactId);
			
			Cursor c = context.getContentResolver().query(
					Data.CONTENT_URI,
					new String[] { Data.DATA1 },
					Data.MIMETYPE + " = '" + Note.CONTENT_ITEM_TYPE
							+ "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactId + " and is_privacy > -1" ,
					null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					re = c.getString(0);
				}
				c.close();
			}
					
			Log.v("NoteUtils" , " re = " + re);
			return re;
		}
		return null;	
	}
	
}