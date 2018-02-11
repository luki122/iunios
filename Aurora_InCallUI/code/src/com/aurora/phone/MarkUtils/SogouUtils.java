package com.android.incallui;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sogou.hmt.sdk.manager.HMTNumber;
import com.sogou.hmt.sdk.manager.HmtSdkManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;

public class SogouUtils extends AuroraMarkUtils{
    private static final String LOG_TAG = "SogouUtils";

	private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	
	public static boolean isInit(){
		if(!isSupportSogou()) {
			return false;
		}
		return HmtSdkManager.getInstance().isInit();
	}
	
	public static void init(Context context){
		if(!isSupportSogou()) {
			return;
		}
		HmtSdkManager.getInstance().init(context);
	}
	
	private static SogouUtils sMe = null;
	
	private SogouUtils() {}
	
	public static SogouUtils getInstance(){
		if(sMe == null) {
			sMe = new SogouUtils();
		}
		return sMe;
	}
	
//	public static boolean insertUserMark(Context context, String number,String lable) {
//		if (TextUtils.isEmpty(number)) {
//			return false;
//		}
//		
//		ContentValues cv = new ContentValues();
//		cv.put("lable", lable);
//		cv.put("number", number);
//		
//		Cursor cursor = context.getContentResolver().query(mMarkUri, null, "number='" + number + "'", null, null);
//		if (cursor != null) {
//			try {
//				if (cursor.moveToFirst()) {
//					if (lable == null) {
//						int count = context.getContentResolver().delete(mMarkUri, "number='" + number + "'", null);
//						if (count > 0) {
//		      				return true;
//		      			}
//					} else {
//						int count = context.getContentResolver().update(mMarkUri, cv, "number='" + number + "'", null);
//		      			if (count > 0) {
//		      				return true;
//		      			}
//					}
//				}
//			} finally {
//				cursor.close();
//			}
//		}
//		
//		Uri uri = context.getContentResolver().insert(mMarkUri, cv);
//		if (uri != null) {
//			return true;
//		}
//		
//		return false;
//	}
	
//	public static boolean deleteUserMark(Context context, String number) {
//		if (TextUtils.isEmpty(number)) {
//			return false;
//		}
//		int count = context.getContentResolver().delete(mMarkUri, "number=?", new String[] {number});
//		if (count > 0) {
//			return true;
//		}
//		
//		return false;
//	}
	
	


	private static HMTNumber h = null;
	private boolean flag=false;
	public  String getMarkContentInternal(Context context, final String number) {	
		Log.v(LOG_TAG, "getMarkContentInternal number = " + number);
		
		
		if(!isSupportSogou()) {
			mNumber = -1;
			mMark = "";
			mUserMark = "";
			return null;
		}
		
		h = null;	
		
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {

			if (info.isAvailable()) {
				flag=true;	
				new Thread(){
					public void run() {
						try {
							h = HmtSdkManager.getInstance().checkNumberFromNet(number);
							System.out.println("nnnnnnnnnnnnnnnnnnnn");							
						} catch (Exception e) {
							h = checkNumberFromLocal(number);
						}
					
					}				
				}.start();

			} else {
				h = checkNumberFromLocal(number);
			}
		} else {
			h = checkNumberFromLocal(number);
		}
		if(flag){
			synchronized (this) {
				try {
					wait(600);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
		if (h == null) {
			Log.v(LOG_TAG, "getMarkContentInternal null" );
			mNumber = -1;
			mMark = "";
			mUserMark = "";
			return null;
		} else {
			String s = h.getMarkContent();
			mNumber = h.getMarkNumber();
			mMark = s;
			mUserMark = "";
			h = null;
			Log.v(LOG_TAG, "getMarkContentInternal mNumber = " + mNumber + " mMark= " + mMark );
			return s;
		}
	}
		
	
	
//	public static boolean isSpam(String smsBody,ArrayList arrayList){
//		boolean b= HmtSdkManager.getInstance().isSpam(smsBody, arrayList);
//		if(b){
//			if(arrayList.size()>0){
//				String s=arrayList.toString();
//				System.out.println(s);
//			}
//		}
//		return b;
//	}
	
	private static HMTNumber checkNumberFromLocal(final String number) {
		try {
			return HmtSdkManager.getInstance().checkNumberFromLocal(number);					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
    public static String getMarkInternal(String number) {  	
	    return getInstance().getMarkContentInternal(InCallApp.getInstance(), number);	  
			
    }
    
    
    public static boolean isSupportSogou() {
//    	return SystemProperties.get("persist.sys.country").equals("CN");
    	return !DeviceUtils.isIndia();
    }
}
