package com.android.phone;

import com.yulore.superyellowpage.modelbean.*;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;

public class AuroraMarkUtils {
    private static final String LOG_TAG = "AuroraMarkUtils";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
	
	protected static int mNumber = -1;
	protected static String mMark, mUserMark;

    
	public static void resetMarks() {
		mNumber = -1;
		mMark = "";
		mUserMark = "";
		YuLoreUtils.reset();
	}
    	
	
    public static String[] getNote(String number) {
    	String[] result = new String[]{"", ""};    	
		if(RejectUtils.isSupportBlack()) {
			Log.v(LOG_TAG, "getUserMark mUserMark = " + mUserMark);
	    	result[0] = mUserMark;
	    	if(TextUtils.isEmpty(result[0])) {
	    		Log.v(LOG_TAG, "getMarkContent mMark = " + mMark);
	    	  	result[0] = mMark;
	    	    Log.v(LOG_TAG, "getMarkNumber mNumber = " + mNumber);
	  		    result[1] = mNumber + "";
	    	}
		}
    	return result;    		
    }
    
    private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
    private static String getUserMarkInternal(Context context, String number) {
		Log.v(LOG_TAG, "getUserMarkInternal number = " + number);
		String result = null;
		if (!TextUtils.isEmpty(number)) {		
			Cursor cursor = context.getContentResolver().query(mMarkUri, null, "number='" + number + "'", null, null);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						result = cursor.getString(1);
					}
				} finally {
					cursor.close();
				}
			}
		}
		mNumber = -1;
		mMark = "";
		mUserMark = result;
		Log.v(LOG_TAG, "mUserMark = " + mUserMark);
		return result;
	}
    
	private static void getMarkContentFromCalllogInternal(Context context, final String number) {
		Log.v(LOG_TAG, "getMarkContentFromCalllogInternal number = " + number);
		if (TextUtils.isEmpty(number)) {			
			return;
		}	
         Cursor c = context.getContentResolver().query(Uri.withAppendedPath(CallLog.Calls.CONTENT_FILTER_URI, number),
        		 new String[]{"mark", "user_mark"}, 
        		 "(" + CallLog.Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE + " or " + CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + ") and mark IS NOT NULL" , 
        		 null, "_id DESC limit 0,1");
     	if (c != null) {
			try {
				if (c.moveToFirst()) {
			         String mark = c.getString(0);
			         mNumber = c.getInt(1);
			         if(mNumber >= 0) {
			 			mMark = mark;
						mUserMark = "";
			         } else {
			 			mMark = "";
						mUserMark = mark;
			         }
				     Log.v(LOG_TAG, "getMarkContentFromCalllogInternal mNumber = " + mNumber + " mMark= " + mMark );
				}
			} finally {
				c.close();
			}
		}
		
	}	
    
	public static void getNumberInfoInternal(String number) {
		if(RejectUtils.isSupportBlack()) {
	    	String result = getUserMarkInternal(PhoneGlobals.getInstance(), number);
	    	if(TextUtils.isEmpty(result)) {
//	    		result = getInstance().getMarkContentInternal(PhoneGlobals.getInstance(), number);
	    		if(SogouUtils.isInit()) {
	    			result =  SogouUtils.getMarkInternal(number);      
	    		} else if(PhoneGlobals.getInstance().mYuloreUtils.isInit()) {
	    			result =  YuLoreUtils.getMarkInternal(number);
	    		}
	    	}
	    	if(TextUtils.isEmpty(result)) {
	    		getMarkContentFromCalllogInternal(PhoneGlobals.getInstance(), number);
	    	}
		}	
	}
}