package com.android.phone;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;

import com.android.internal.telephony.Connection;

public class NoteUtils {
    private static final String LOG_TAG = "NoteUtils";
	public static String getNote(Context con, long rawContactId) {
		
		Log.v("ttwang", " getNote rawContactId = " + rawContactId);
		String re = "";
		
		Cursor c = con.getContentResolver().query(
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
				
		Log.v("ttwang", " re = " + re);
		return re;
	}
	
	private static long getRawContactIdFromConnection(Connection conn) {
		long result = -1;
		if(conn != null) {
	        AuroraCallerInfo ci = null;
	        Object o = conn.getUserData();
	
	        if (o != null && o instanceof AuroraCallerInfo) {
	            ci = (AuroraCallerInfo) o;
	            result = ci.mRawContactId;
	        }
		}
		return result;
	}
	
}