package com.gionee.legalholiday;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.android.calendar.AllInOneActivity;


public class HolidayData extends AsyncTask<String, Void, String> {
	
	private static final String LOG_TAG = "HolidayData";
	String result = null;
	String para = null;
	
	private Context mContext = null;
	
	public HolidayData(Context context) {
		mContext = context;
	}
	
	

	@Override
	protected String doInBackground(String... requests) {
		Log.d(LOG_TAG, "testb1");
		
		para = requests[0];
		result = getHolidayData();
		
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		LegalHolidayUtils utils=null;
		
		if(para.compareTo("update") == 0){
			utils = LegalHolidayUtils.getInstance();
			if(null == result) {
				return;
			}
			Log.d(LOG_TAG, "update HolidayData");
			utils.parseJsonString(result,para);
			return;
		}else if(para.compareTo("create") == 0) {
			LegalHolidayUtils.createLegalHolidayUtils(mContext, null);
			utils = LegalHolidayUtils.getInstance();
			if(null == result) {
				return;
			}
			Log.d(LOG_TAG, "create HolidayData");
			utils.parseJsonString(result,para);
		}
		
	}

	
	
	

	
	private String getHolidayData() {
		
		Time t = new Time();
		t.setToNow();
		String year;
		year = String.valueOf(t.year);
		
		String url ="http://t-calendar.gionee.com/holiday/"+year;
		Log.d(LOG_TAG, url);
		
		//
		if(TextUtils.isEmpty(url)) throw new RuntimeException("empty url");
		//
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = null;
		

		try {
			response = httpClient.execute(request);
		} catch(ClientProtocolException e1) {
			Log.d(LOG_TAG, "throws ClientProtocolException");
			return null;			
		} catch(IOException e2) {
			Log.d(LOG_TAG, "throws IOException");
			return null;
		} catch(Exception e) {
			Log.d(LOG_TAG, "throws Exception e");
			return null;
		}
		
		
		String jsonStr = null;
		
		StringBuilder buffer = new StringBuilder(300);
		
		try {
			jsonStr = EntityUtils.toString(response.getEntity());
			if(false == invalidString(jsonStr)) {
				Log.d(LOG_TAG, "failed http");
				return null;
			}
			int offset = jsonStr.indexOf('{');
			Log.d(LOG_TAG, "first { at " + offset);
			buffer.append(jsonStr, offset, jsonStr.length());
		} catch (IOException e2) {
			Log.d(LOG_TAG, "failed to parse JSON string 2");
			return null;
		}
		
		Log.d(LOG_TAG, jsonStr);
		return buffer.toString();
		
	}
	
	private boolean invalidString(String jsonStr) {
		
		if(null == jsonStr) {
			return false;
		}
		
		if(false == jsonStr.contains("year")) {
			return false;
		}
		if(false == jsonStr.contains("version")) {
			return false;
		}
		if(false == jsonStr.contains("{")) {
			return false;
		}
		if(false == jsonStr.contains("}")) {
			return false;
		}
		return true;
		
	}
	
}
