package com.android.server.telecom; 
  

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;

import com.android.server.telecom.AuroraTelephony.SIMInfo;
import com.android.server.telecom.AuroraTelephony.SimInfo;

import android.provider.CallLog.Calls;
import android.telephony.TelephonyManager;
  
public class ProviderUtils {  
    private static final String TAG = "ProviderUtils";


   public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId + " and is_privacy > -1 ", null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    
	   public static long getRawContactId(Uri uri) {
			Log.v("getRawContactId", " uri = " + uri);
			if(uri == null) {
				return 0;
			}
			
	        String url = uri.toString();
			
			if (url.startsWith("content://com.android.contacts/contacts/lookup/") || url.startsWith("content://com.android.contacts/contacts/")) {
				long RawContactId = queryForRawContactId(AuroraGlobals.getInstance().getContentResolver(), Long.parseLong(uri.getLastPathSegment()));
				return RawContactId;
			} else if (url.startsWith("content://com.android.contacts/phone_lookup/")) {
				return getRawContactIdByNumber(uri.getLastPathSegment());
			} else {
				Cursor cursor = AuroraGlobals.getInstance().getContentResolver().query(uri, new String[] { "raw_contact_id"},  null, null, null);
				try {
					if (cursor != null && cursor.getCount() > 0) {
						cursor.moveToFirst();
						return cursor.getLong(0);
					}
					return 0;
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
	    }

	    private static long getRawContactIdByNumber(String number) { 
			Log.v("getRawContactIdByNumber", " number = " + number);
			
			if(TextUtils.isEmpty(number)) {
				return 0;
			}

			Cursor cursor = AuroraGlobals.getInstance().getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number), 
					new String[]{"raw_contact_id"},
					null, null, null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					return cursor.getLong(0);
				}
		    	return 0;
			} finally {
				if(cursor != null) {
					cursor.close();
				}
			}    
	    }
	    
	    public static long getDataIdByRawContactId(long rawContactId) {
			Log.v("getDataIdByRawContactId", " rawContactId = " + rawContactId);
	        long result = -1;
	        if (rawContactId <= 0) {
	            return result;
	        }
	        
	        Cursor cursor = AuroraGlobals.getInstance().getContentResolver().query(Data.CONTENT_URI,
					new String[] {Data._ID},
					Calls.RAW_CONTACT_ID + " = " + rawContactId + " AND is_privacy > -1",
					null, null);    
	        if (null != cursor) {
	            if (cursor.moveToFirst()) {
	                result = cursor.getLong(0);
	            }
	            cursor.close();
	        }
			Log.v("getDataIdByRawContactId", " dataid = " + result);
	        return result;
	    }
	    
}  