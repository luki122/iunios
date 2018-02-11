package com.android.contacts.util;

import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.yulore.framework.YuloreHelper;
import com.yulore.superyellowpage.modelbean.RecognitionTelephone;
import com.yulore.superyellowpage.modelbean.TelephoneFlag;
public class YuloreUtils {
	private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	private static String API_KEY="jfvLhfephClV9jkRuscG9nmpDL9SiOc7";
	private static String API_Secret="0hOdge6xrsi9fcpXRKplnmpub3xXpDdzaeML6HjUCMufLDiwyEtRJuLocasVkoiJ5thxzwDdvan7lsi1zajntau9i5rpoVlrg5jvVC1aksvi4TlLfWyxdlrQB7pnMcNzvD06GHCrrMrju0hzqvYlrvzoFDBh3SoxGjKcd";
	private YuloreHelper uniqueInstance = null;
	private static YuloreUtils mYuloreUtils = null;
	private static boolean flag=false;
	private static Object mWait = new Object();
	private String name=null;
	private RecognitionTelephone rtn=null;
	
	private ConcurrentHashMap<String, RecognitionTelephone> mRecognitionTelephones = new ConcurrentHashMap<String, RecognitionTelephone>();

	private YuloreUtils(Context context){
		uniqueInstance = new YuloreHelper(context);
	}
	
	public static YuloreUtils getInstance(Context context) {
		if (mYuloreUtils == null) {
			synchronized (mWait) {
				if(mYuloreUtils == null){
					mYuloreUtils = new YuloreUtils(ContactsApplication.getInstance());
				}
			}
		}
		return mYuloreUtils;
	}
	
	public boolean isBind(){
		return flag;
	}
	
	public boolean bind(){
		flag=uniqueInstance.bindService(API_KEY, API_Secret);
		return flag;
	}
	
	public void unbind(){
		uniqueInstance.unbindService();
		uniqueInstance = null;
		flag=false;
	}
	
	public void startActivity(int flag, String action, String category, String data, String type, String pkg, String title){
		uniqueInstance.startActivity(flag, action, category, data, type, pkg, title);
	}
	
	public boolean insertUserMark(Context context, String number,String lable) {
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
	
	public boolean deleteUserMark(Context context, String number) {
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
	
	public String getUserMark(Context context, String number) {
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
	
	public String getMarkContent(String number, Context context) {
		String mark=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, number, 1, true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(rt != null){
//    		Log.i("qiaohu", rt.getName());
    		mark=rt.getName();
    		if(mark!=null){
    			return mark;
    		}else{
    			TelephoneFlag flag=rt.getFlag();
    			if(flag!=null){
    				mark=flag.getType();
    			}
    		}
        }
		return mark;
	}
	
	public int getMarkNumber(Context context, String number) {
		int num = 0;
		RecognitionTelephone rt = null;
		try {
			rt = uniqueInstance.queryNumberInfo(API_KEY, API_Secret, number, 1,
					true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(rt != null){
    		TelephoneFlag flag = rt.getFlag();
    		if (flag != null) {
    			num = flag.getNum();
    		}
        }
		return num;

	}

	public String getArea(String num){
		String area=null;
		if(mRecognitionTelephones.containsKey(num)){
			RecognitionTelephone rt = mRecognitionTelephones.get(num);
			area=rt.getName();
    		if(area!=null){
    			return area;
    		}else{
    			return rt.getLocation();
    		}
		}
		return null;
	}
	
	public String getAreaLock(String num){
		String area=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 1, true, true, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rt != null){
			area=rt.getName();
    		if(area!=null){
    			return area;
    		}else{
    			return rt.getLocation();
    		}
		}
		return area;
	}
	
	public String getName(final String num){
		name=null;
		new Thread(){
			public void run() {
				try {
					rtn=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 1, true, true, 1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(rtn != null){
					Log.i("qiaohu", ""+rtn.toString());
					name=rtn.getName();
				}
			};
		}.start();
		
		synchronized (this) {
			try {
				wait(300);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return name;
	}
	
	
	
	
	

	

	

			
		
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
