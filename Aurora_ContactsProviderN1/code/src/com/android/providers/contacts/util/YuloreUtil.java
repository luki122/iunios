package com.android.providers.contacts.util;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.providers.contacts.R;

import com.yulore.framework.YuloreHelper;
import com.yulore.superyellowpage.modelbean.RecognitionTelephone;
import com.yulore.superyellowpage.modelbean.Tag;
import com.yulore.superyellowpage.modelbean.TelephoneFlag;

public class YuloreUtil {
	private static final Uri mMarkUri = Uri
			.parse("content://com.android.contacts/mark");

	private static final String KEY = "jfvLhfephClV9jkRuscG9nmpDL9SiOc7";
	private static final String SECRET = "0hOdge6xrsi9fcpXRKplnmpub3xXpDdzaeML6HjUCMufLDiwyEtRJuLocasVkoiJ5thxzwDdvan7lsi1zajntau9i5rpoVlrg5jvVC1aksvi4TlLfWyxdlrQB7pnMcNzvD06GHCrrMrju0hzqvYlrvzoFDBh3SoxGjKcd";

	private YuloreHelper mYuloreHelper;
	private static YuloreUtil mYuloreUtil;
	private static Object mClock = new Object();

	private YuloreUtil(Context context) {
		mYuloreHelper = new YuloreHelper(context.getApplicationContext());
	}

	public static YuloreUtil getInstance(Context context) {
		long time = System.currentTimeMillis();
		if (mYuloreUtil == null) {
			synchronized (mClock) {
				if (mYuloreUtil == null) {
					if (context != null) {
						mYuloreUtil = new YuloreUtil(
								context.getApplicationContext());
					} else {
						Log.e("YuloreUtil",
								"context is null, YuloreHelper can't create");
					}
				}
			}
		}
		return mYuloreUtil;
	}
	
	public static String getArea(Context context, String number) {
		if (number == null) {
			return null;
		}
		
		String result = getNumAreaFromAora(context, number);
      	if(TextUtils.isEmpty(result)) { 
	      	if(!TextUtils.isEmpty(number) 
	      			&& (number.replace(" ", "").length() == 7 || number.replace(" ", "").length() == 8)
	      			&& !number.startsWith("0")
	        			&& !number.startsWith("400")
	        			&& !number.startsWith("800")
	      			) {
	      		result = context.getResources().getString(R.string.aurora_local_number);
	      	}  else if (!TextUtils.isEmpty(number) 	              			
	        			&& (number.startsWith("400") || number.startsWith("800"))
	        			&& number.replace(" ", "").length() == 10
	        			){
	      		result = context.getResources().getString(R.string.aurora_service_number);
	        } else {
	        	result = "";
	      	}
//	      	if(result.equalsIgnoreCase("")) {
//	  			String[] hotlineInfo = GnHotLinesUtil.getInfo(context, number);
//	  			if (null != hotlineInfo) {
//	  				result = hotlineInfo[0];
//	  			}	
//	      	}
      	}
		return result;
	}

	public static String getNumAreaFromAora(Context context, String number) {
		YuloreUtil yuloreUtil = YuloreUtil.getInstance(context);
		if (yuloreUtil != null) {
			RecognitionTelephone recognition = null;
			try {
				recognition = yuloreUtil.queryNumberInfo(number, 1, true, true,
						1);
			} catch (RemoteException e) {
				Log.e("YuloreUtil",
						"getNumAreaFromAora: e = " + e);
			}
			if(recognition != null){
				String result = recognition.getLocation();
				return result;
			}
		}
		return null;
	}

	private RecognitionTelephone queryNumberInfo(String telNum, int type,
			boolean samrt, boolean immediately, int limit)
			throws RemoteException {
		return mYuloreHelper.queryNumberInfo(KEY, SECRET, telNum, type, samrt,
				immediately, limit);
	}
	
	public static String getMarkContent(Context context, String number) {
		Log.e("yudingmin",
				"getMarkContent");
		if (context == null || number == null) {
			return null;
		}
		YuloreUtil yuloreUtil = YuloreUtil.getInstance(context);
		if (yuloreUtil != null) {
			RecognitionTelephone recognitionTelephone = null;
			try{
				recognitionTelephone = yuloreUtil.queryNumberInfo(number,  1, true, true,
						1);
			} catch (RemoteException e) {
				Log.e("YuloreUtil",
						"getMarkContent: e = " + e);
			}
			if(recognitionTelephone != null){
				TelephoneFlag flag = recognitionTelephone.getFlag();
				if(flag != null){
					Log.e("yudingmin","tag name = "+flag.getType());
					return flag.getType();
				}
			}
		}
		return null;
	}
	
	public static int getMarkNumber(Context context, String number) {
		Log.e("yudingmin",
				"getMarkCount");
		if (context == null || number == null) {
			return -1;
		}
		YuloreUtil yuloreUtil = YuloreUtil.getInstance(context);
		if (yuloreUtil != null) {
			RecognitionTelephone recognitionTelephone = null;
			try{
				recognitionTelephone = yuloreUtil.queryNumberInfo(number,  1, true, true,
						1);
			} catch (RemoteException e) {
				Log.e("YuloreUtil",
						"getMarkContent: e = " + e);
			}
			if(recognitionTelephone != null){
				TelephoneFlag flag = recognitionTelephone.getFlag();
				if(flag != null){
					Log.e("yudingmin","tag name = "+flag.getNum());
					return flag.getNum();
				}
			}
		}
		return -1;
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
		
		int count = 0;
		try {
			count = context.getContentResolver().delete(mMarkUri, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (count > 0) {
			return true;
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
	
	public boolean bindService() {
		return mYuloreHelper.bindService(KEY, SECRET);
	}
	
	public void unbindService() {
		mYuloreHelper.unbindService();
	}
}
