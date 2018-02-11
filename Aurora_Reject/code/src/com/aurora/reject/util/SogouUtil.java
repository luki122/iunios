package com.aurora.reject.util;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.sogou.hmt.sdk.manager.HMTNumber;
import com.sogou.hmt.sdk.manager.HmtSdkManager;

public class SogouUtil {
	private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	private  HMTNumber h = null;
	private  boolean flag=false;
	
	public static boolean isInit(){
		return HmtSdkManager.getInstance().isInit();
	}
	
	public static void init(Context context){
		HmtSdkManager.getInstance().init(context);
	}
	
	public static boolean insertUserMark(Context context, String number,String lable) {
		if (number == null) {
			return false;
		}
		
		ContentValues cv = new ContentValues();
		cv.put("lable", lable);
		cv.put("number", number);
		
		try {
		Cursor cursor = context.getContentResolver().query(mMarkUri, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					if (lable == null) {
						int count = context.getContentResolver().delete(mMarkUri, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
						if (count > 0) {
		      				return true;
		      			}
					} else {
						int count = context.getContentResolver().update(mMarkUri, cv, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
		      			if (count > 0) {
		      				return true;
		      			}
					}
				}
			} finally {
				cursor.close();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Uri uri = context.getContentResolver().insert(mMarkUri, cv);
		if (uri != null) {
			return true;
		}
		
		return false;
	}
	
	public static boolean deleteUserMark(Context context, String number) {
		if (number == null) {
			return false;
		}
		try {
		int count = context.getContentResolver().delete(mMarkUri, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
		if (count > 0) {
			return true;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static String getUserMark(Context context, String number) {
		if (number == null) {
			return null;
		}
		String result = null;
		try {
		Cursor cursor = context.getContentResolver().query(mMarkUri, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					result = cursor.getString(1);
				}
			} finally {
				cursor.close();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static String getMarkContent(String number, Context context) {
		HMTNumber h = null;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {

			if (info.isAvailable()) {
				try {
					
					h = HmtSdkManager.getInstance().checkNumberFromNet(number);
						
					
				} catch (Exception e) {
					h = HmtSdkManager.getInstance()
							.checkNumberFromLocal(number);
				}

			} else {
				h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
			}
		} else {
			h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
		}
		if (h == null) {
			return null;
		} else {
			String s = h.getMarkContent();
			return s;
		}

	}
	
	public static int getMarkNumber(Context context,String number){
			HMTNumber h = null;
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null) {

				if (info.isAvailable()) {
					try {
						h = HmtSdkManager.getInstance().checkNumberFromNet(number);
					} catch (Exception e) {
						h = HmtSdkManager.getInstance()
								.checkNumberFromLocal(number);
					}

				} else {
					h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
				}
			} else {
				h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
			}
			if(h==null){
				 return -1;
			 }else {
				 int i=h.getMarkNumber();
				 return i;
			}
	}
	
	
	
	
	
	
	
	
    public  String getMarkContents(final String number, Context context) {
		
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
								h = HmtSdkManager.getInstance()
										.checkNumberFromLocal(number);
							}

						
						};
						
					}.start();
					
				
			} else {
				h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
			}
		} else {
			h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
		}
		if(flag){
			flag=false;
			synchronized (this) {
				try {
					wait(1000);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
		if (h == null) {
			return null;
		} else {
			String s = h.getMarkContent();
			return s;
		}
	}
    
    
    
    
    public  int getMarkNumbers(Context context,final String number){
    	ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {

			if (info.isAvailable()) {
				flag=true;
					new Thread(){
						public void run() {
							try {
							h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
							System.out.println("nnnnnnnnnnnnnnnnnnnn");
							
							} catch (Exception e) {
								h = HmtSdkManager.getInstance()
										.checkNumberFromLocal(number);
							}
						
						};
						
					}.start();

			} else {
				h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
			}
		} else {
			h = HmtSdkManager.getInstance().checkNumberFromLocal(number);
		}
		if(flag){
			flag=false;
			synchronized (this) {
				try {
					wait(1000);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
		if(h==null){
			 return -1;
		 }else {
			 int i=h.getMarkNumber();
			 return i;
		}
}
	
	
	
	
}
