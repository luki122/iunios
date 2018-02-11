package com.android.contacts.util;

import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.yulore.framework.YuloreHelper;
import com.yulore.superyellowpage.modelbean.RecognitionTelephone;
import com.yulore.superyellowpage.modelbean.TelephoneFlag;
public class YuloreUtils {
	private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	private static final String TAG = "YuloreUtils";
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
	
	public String getAreaSmart(String num){
		Log.i("getAreaSmart", "num");
		if(!TextUtils.isEmpty(num)) {
			num = num.replaceAll(" ", "");
			int length = num.length();
			Log.i("getAreaSmart", "length = " + length);
			if(length == 4 || length > 10) {
				return getAreaLock(num);
			} else {
				return getAreaLocal(num);
			}
		} else {
			return "";
		}
	}
	
	//  功能描述:该方法返回电话号码相关的各种信息,并将信息缓存到本地,方法中操作为耗时操作,建议
	//  在创建异步线程中执行该方法。
	//  应用对应的 APIKEY
	//  参数:apiKey
	//  secret 应用对应的 SECRET
	//  telNum 查询电话实例
	//  type 1 呼入, 2 呼出
	//  smart 智能模式开关
	//  immediately  立即返回数据
	//  limit  1=不限制 2=wifi 下获取数据 3=不联网
	public String getAreaLocal(String num){
		Log.i("getAreaLocal", "num = " + num);
		String area=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 2, true, true, 3);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rt != null){
			area=rt.getName();//名称
			Log.d(TAG, "rt.getName():"+rt.getName());
    		if(area!=null){
    			return area;
    		}else{
    			Log.d(TAG, "rt.getLocation():"+rt.getLocation());
    			return rt.getLocation();//归属地
    		}
		}
		return area;
	}
	
	public String getAreaLock(String num){
		Log.i(TAG, "getAreaLock,num = " + num);
		String area=null;
		RecognitionTelephone rt=null;
		try {
			rt=uniqueInstance.queryNumberInfo(API_KEY, API_Secret, num, 2, true, false, 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rt != null){
			area=rt.getName();
			Log.d(TAG, "rt.getName():"+rt.getName());
    		if(area!=null){
    			return area;
    		}else{
    			Log.d(TAG, "rt.getLocation():"+rt.getLocation());
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
