package com.aurora.reject.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.yulore.framework.YuloreHelper;
import com.yulore.superyellowpage.modelbean.RecognitionTelephone;
import com.yulore.superyellowpage.modelbean.TelephoneFlag;
public class YuloreUtil {
	private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	private static String API_KEY="jfvLhfephClV9jkRuscG9nmpDL9SiOc7";
	private static String API_Secret="0hOdge6xrsi9fcpXRKplnmpub3xXpDdzaeML6HjUCMufLDiwyEtRJuLocasVkoiJ5thxzwDdvan7lsi1zajntau9i5rpoVlrg5jvVC1aksvi4TlLfWyxdlrQB7pnMcNzvD06GHCrrMrju0hzqvYlrvzoFDBh3SoxGjKcd";
	private static YuloreHelper uniqueInstance = null;  
	private static boolean flag=false;

	
	public static YuloreHelper getInstance(Context context) {
		if (uniqueInstance == null) {
			uniqueInstance = new YuloreHelper(context);
		}
		return uniqueInstance;
	}
	
	public static boolean isBind(){
		return flag;
	}
	
	public static boolean bind(){
		flag=uniqueInstance.bindService(API_KEY, API_Secret);
		return flag;
	}
	
	public static void unbind(){
		uniqueInstance.unbindService();
		uniqueInstance = null;
		flag=false;
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
		String mark=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, number, 1, true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Log.i("qiaohu", rt.getName());
		mark=rt.getName();
		if(mark!=null){
			return mark;
		}else{
			TelephoneFlag flag=rt.getFlag();
			if(flag!=null){
				mark=flag.getType();
			}
		}
		return mark;
	}
	
	public static int getMarkNumber(Context context, String number) {
		int num = 0;
		RecognitionTelephone rt = null;
		try {
			rt = uniqueInstance.queryNumberInfo(API_KEY, API_Secret, number, 1,
					true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TelephoneFlag flag = rt.getFlag();
		if (flag != null) {
			num = flag.getNum();
		}
		return num;

	}
	
	
	
	public static String getArea(String num){
		String area=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 1, true, true, 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		area=rt.getName();
		if(area!=null){
			return area;
		}else{
			area=rt.getLocation();
		}
		return area;
	}
	
	
	public static String getName(String num){
		String name=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 1, true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("qiaohu", ""+rt.toString());
		name=rt.getName();
		return name;
	}
	
}
