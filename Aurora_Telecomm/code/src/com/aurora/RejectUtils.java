package com.android.server.telecom;  
  
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;   
import java.net.HttpURLConnection;  
import java.net.URL; 

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.os.SystemProperties;
  
public class RejectUtils {  
    private static final String TAG = "RejectUtils";


    private static Uri black_uri = Uri.parse("content://com.android.contacts/black");
    
    private static final String[] BLACK_PROJECTION = new String[] {
    	"_id",   //唯一标示，递增
    	"isblack",   // 标记黑白名单（0: 白名单/1:黑名单）
    	"lable",    //通话记录表中获取的标记String, 或添加黑名单时直接搜搜狗获取的标记
    	"black_name",  // 黑名单中的名字
    	"number", //号码
    	"reject" //标示是否拦截通话，短信（0：不拦截/ 1：拦截通话/2:拦截短信/3同时拦截通话、短信）
    };
        
    public static boolean isBlackNumber(String number) {
		Log.v("isBlackNumber", " number = " + number);
		
		if(!isSupportBlack() || TextUtils.isEmpty(number)) {
			return false;
		}
		
//		Cursor cursor = AuroraGlobals.getInstance().getContentResolver().query(black_uri, BLACK_PROJECTION,
//				"(isblack = '1' OR isblack = '3') AND number = '" + number + "'", null, null);
		Cursor cursor = null;
		try {
			cursor = AuroraGlobals.getInstance().getContentResolver().query(black_uri, BLACK_PROJECTION,
					"(reject = '1' OR reject = '3') AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
					Log.v("isBlackNumber", " cursor = " + cursor);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				mBlackName = cursor.getString(3);
				return true;
			}
	    	return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
		return false;
    }
    
    private static String mBlackName;
    public static String getLastBlackName() {
    	return mBlackName;
    }
    
    public static boolean isToAddBlack(String number) {
		if(!isSupportBlack() || TextUtils.isEmpty(number)) {
			return false;
		}
		
		if(AuroraPrivacyUtils.isSupportPrivate()) {
			int[] privateData = new int[3];
			privateData = AuroraPrivacyUtils.getPrivateData(number);
	        long privateId = privateData != null ? privateData[1] : 0; 
			if(privateId > 0) {
	            return false ;
			}				
		}
		
    	 String[] projection = {CallLog.Calls.NUMBER};
    	 Uri uri = Uri.withAppendedPath(CallLog.Calls.CONTENT_FILTER_URI, Uri.encode(number));
    	 Cursor cursor = AuroraGlobals.getInstance().getContentResolver().query(uri, projection,
                 CallLog.Calls.TYPE + "='" + CallLog.Calls.INCOMING_TYPE + "' AND " + CallLog.Calls.DURATION + "='0' AND " + CallLog.Calls.DATE + " > '" + (System.currentTimeMillis() - 24 * 3600 * 1000 ) + "'"  , null, null);
    	 if(cursor != null) {
    		 Log.v("isToAddBlack", " cursor = " + cursor.getCount());
    	 }
		try {
	    	if (cursor != null && cursor.getCount() >= 2) {
	    		if(!isBlackNumber(number)) {
	    			Log.v("isToAddBlack", " number = " + number);
	    		    return true;
	    		}
	    	}
	    	return false;
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
    }       
        
    
    public static boolean isSupportBlack() {
//        return AuroraGlobals.getInstance().getResources().getBoolan(R.bool.aurora_black_list);
        String prop = SystemProperties.get("ro.aurora.reject.support");
    	return prop.contains("yes");
    }   
    
    
    public static boolean isAuroraNeedHangup(String number, boolean notify) {
		if(AuroraPrivacyUtils.isSupportPrivate()) {
			int[] privateData = new int[3];
			privateData = AuroraPrivacyUtils.getPrivateData(number);
			long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
	        long privateId = privateData != null ? privateData[1] : 0; 
	        int private_noti_type = privateData != null ? privateData[2] : 0; 
			if(privateId != currentPrivateId && privateId > 0 && private_noti_type == 1) {
		    	Log.v(TAG, "isPrivateHangup true "); 
		    	if(notify) {
		    		AuroraGlobals.getInstance().mManagePrivate.notificationMgr.notifyHangupPrivateRingingCallFake(privateId);
		    	}
		       	return true;
			} else if(privateId == 0 && RejectUtils.isBlackNumber(number)) {
		    	if(notify) {
		    		AuroraGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackCall(number);
		    	}
	            return true;
	        }
		} else if(isBlackNumber(number)) {
	    	if(notify) {
	    		AuroraGlobals.getInstance().mManageReject.notificationMgr.notifyHangupBlackCall(number);
	    	}
            return true;	        
		}	
		return false;
    }

}  