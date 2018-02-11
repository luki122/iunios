package com.android.server.telecom;

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
    
	public static void getMarkContentFromCalllogInternal(Context context, final String number) {
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
		if(SogouUtils.isInit()) {
	    	SogouUtils.getNoteInternal(number);      
		} else if(AuroraGlobals.getInstance().mYuloreUtils.isInit()) {
			YuLoreUtils.getMarkInternal(number);
		}
	}
}