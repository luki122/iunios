package com.gionee.astro;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.calendar.R;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.legalholiday.HolidayData;
import com.gionee.legalholiday.LegalHolidayUtils;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class GNAstroUtils {
	private static final String LOG_TAG = "Astro";
	
	private static final String ASTRO_ADDRESS_PREFIX = "http://app.data.qq.com/?umod=astrothipart&act=astro";
	private static final String ASTRO_ARGS = "&t=?&var=h1info&a=?&y=?&m=?&d=?";
	
	public static final int ASTRO_INFO_TYPE_WEEK = 3;
	public static final int ASTRO_INFO_TYPE_DAY = 4;
	
	public static final String ASTRO_NAME_ARIES = "aries";
	public static final String ASTRO_NAME_TAURUS = "taurus";
	public static final String ASTRO_NAME_GEMINI = "gemini";
	public static final String ASTRO_NAME_CANCER = "cancer";
	public static final String ASTRO_NAME_LEO = "leo";
	public static final String ASTRO_NAME_VIRGO = "virgo";
	public static final String ASTRO_NAME_LIBRA = "libra";
	public static final String ASTRO_NAME_SCORPIO = "scorpio";
	public static final String ASTRO_NAME_SAGITTARIUS = "sagittarius";
	public static final String ASTRO_NAME_CAPRICORN = "capricorn";
	public static final String ASTRO_NAME_AQUARIUS = "aquarius";
	public static final String ASTRO_NAME_PISCES = "pisces";
	
	public static final String KEY_ASTRO_INDEX = "astro_index";
	
	public static final int INDEX_ARIES = 0;
	public static final int INDEX_TAURUS = 1;
	public static final int INDEX_GEMINI = 2;
	public static final int INDEX_CANCER = 3;
	public static final int INDEX_LEO = 4;
	public static final int INDEX_VIRGO = 5;
	public static final int INDEX_LIBRA = 6;
	public static final int INDEX_SCORPIO = 7;
	public static final int INDEX_SAGITTARIUS = 8;
	public static final int INDEX_CAPRICORN = 9;
	public static final int INDEX_AQUARIUS = 10;
	public static final int INDEX_PISCES = 11;
	
	public static final int COUNT_ASTRO = 12;
	
	public static final int GET_ASTRO_STATUS_OK = 0;
	public static final int GET_ASTRO_STATUS_NO_NETWORK = 1;
	public static final int GET_ASTRO_STATUS_NO_DATA = 2;
	
	public static final int GET_ASTRO_STATUS_ERROR_DATA = 3;
	public static final int GET_ASTRO_STATUS_OTHER_ERROR = 4;
	
	public static void saveAstroIndexToPref(Context context, int index) {
		if(context == null) return;
		if(index < 0 || index >= COUNT_ASTRO) {
			Log.e(LOG_TAG, "astro index out of range: " + index);
			return;
		}
		
		SharedPreferences pref = context.getSharedPreferences(
				GNCalendarUtils.GN_PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(KEY_ASTRO_INDEX, index);
		editor.commit();
	}
	
	public static int getAstroIndexFromPref(Context context) {
		if(context == null) return INDEX_ARIES;
		
		SharedPreferences pref = context.getSharedPreferences(
				GNCalendarUtils.GN_PREF_NAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_ASTRO_INDEX, INDEX_ARIES);
	}
	
	public static String getAstroNameById(int id) {
		switch(id) {
			case INDEX_ARIES:
				return ASTRO_NAME_ARIES;
			case INDEX_TAURUS:
				return ASTRO_NAME_TAURUS;
			case INDEX_GEMINI:
				return ASTRO_NAME_GEMINI;
			case INDEX_CANCER:
				return ASTRO_NAME_CANCER;
			case INDEX_LEO:
				return ASTRO_NAME_LEO;
			case INDEX_VIRGO:
				return ASTRO_NAME_VIRGO;
			case INDEX_LIBRA:
				return ASTRO_NAME_LIBRA;
			case INDEX_SCORPIO:
				return ASTRO_NAME_SCORPIO;
			case INDEX_SAGITTARIUS:
				return ASTRO_NAME_SAGITTARIUS;
			case INDEX_CAPRICORN:
				return ASTRO_NAME_CAPRICORN;
			case INDEX_AQUARIUS:
				return ASTRO_NAME_AQUARIUS;
			case INDEX_PISCES:
				return ASTRO_NAME_PISCES;
			default:
				return null;
		}
	} // end of getAstroNameById()
	
	public static final int ICON_TYPE_BIG = 1;
	public static final int ICON_TYPE_SMALL = 2;
	
	public static int findItemIconResByIndex(int index, int type) {
		switch(index) {
			case INDEX_ARIES:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_aries;
				} else {
					return R.drawable.icon_big_baiyang;
				}
			case INDEX_TAURUS:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_taurus;
				} else {
					return R.drawable.icon_big_jinniu;
				}
			case INDEX_GEMINI:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_gemini;
				} else {
					return R.drawable.icon_big_shuangzi;
				}
			case INDEX_CANCER:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_cancer;
				} else {
					return R.drawable.icon_big_juxie;
				}
			case INDEX_LEO:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_leo;
				} else {
					return R.drawable.icon_big_shizi;
				}
			case INDEX_VIRGO:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_virgo;
				} else {
					return R.drawable.icon_big_chunv;
				}
			case INDEX_LIBRA:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_libra;
				} else {
					return R.drawable.icon_big_tianchen;
				}
			case INDEX_SCORPIO:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_scorpio;
				} else {
					return R.drawable.icon_big_tianxie;
				}
			case INDEX_SAGITTARIUS:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_sagittarius;
				} else {
					return R.drawable.icon_big_sheshou;
				}
			case INDEX_CAPRICORN:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_capricorn;
				} else {
					return R.drawable.icon_big_mojie;
				}
			case INDEX_AQUARIUS:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_aquarius;
				} else {
					return R.drawable.icon_big_shuiping;
				}
			case INDEX_PISCES:
				if(type == ICON_TYPE_SMALL) {
					return R.drawable.icon_small_pisces;
				} else {
					return R.drawable.icon_big_shuangyu;
				}
			default:
				return -1;
		}
	} // end of findItemIconResByIndex()
	
	private static final String JSON_NODE_ASTRO = "astro";
	private static final String JSON_NODE_FORTUNE = "fortune";
	private static final String JSON_NODE_TYPE = "type";
	private static final String JSON_NODE_CONTENT = "content";
	private static final String JSON_NODE_WEEK_BEGIN = "weekbeg";
	private static final String JSON_NODE_WEEK_END = "weekend";
	
	
	
	private static DBOperations dbo;
	
	private static int percentString2Int(String percent) {
		int num;
		
		if(percent == null || percent.length() == 0) {
			//throw new RuntimeException("empty percent string");
			return 0;
		}
		
		int len = percent.length();
		if(len < 2 || len > 3) {
			//throw new RuntimeException("percent string has invlaid lenth " + len);
			return 0;
		}
		if(percent.charAt(len - 1) != '%') {
			//throw new RuntimeException("the last character is not %");
			return 0;
		}
		
		num = Integer.parseInt(percent.substring(0, len - 1));
		if((num>=0)&&(num<=100))
		{
			return num;
		}
		else
		{
			return 0;
		}
		
	}
	
	public static class DayAstroInfo {
		public static final int INDEX_FULL_MARKS = 100;
		
		//json infor
		public String selfAstro;
		//public String matchAstro;
		
		public String typeComprehension;
		public int indexComprehension;
		public float indexComprehensionStar;
		
		public String typeLove;
		public int indexLove;
		public float indexLoveStar;
		
		public String typeCareer;
		public int indexCareer;
		public float indexCareerStar;
		
		public String typeFinance;
		public int indexFinance;
		public float indexFinanceStar;
		
		public String typeHealth;
		public int indexHealth;
		public float indexHealthStar;
		
		public String typeLuckyColor;
		public String LuckyColor;
		
		public String typeluckyNumber;
		public String luckyNumber;
		
		public String typeQFriend;
		public String QFriend;
		
		public String typeDescription;
		public String description;
		
		public String day;
		
		//Astro parameter of tencent 
		public int year;
		public int month;
		public int monthDay;
		public String astroName;
	}
	
	public static class WeekAstroInfo {
		
		public String astro;
		public String weekBegin;
		public String weekEnd;
		
		//fortune
		public String[] fortuneType;
		public String[] fortuneContent;
		public int num;
		
		
		//Astro parameter of tencent 
		public int year;
		public int month;
		public int monthDay;
		public String astroName;
	}
	
	public WeekAstroInfo getWeekAstroInfo() {
		return null;
	}
	
	public DayAstroInfo getDayAstroInfo() {
		return null;
	}
	
	
	
	
	
	
	
	
	public class AstroRequest {
		public String url;
		public int year;
		public int month;
		public int monthDay;
		public int astroInfoType;
		public String astroName;
		Context context;
		Message message;
		
		
		public AstroRequest(String url, int year, int month, int monthDay,int astroInfoType,String astroName,Context context,Message message) {
			this.url = url;
			this.year = year;
			this.month = month;
			this.monthDay = monthDay;
			this.astroInfoType = astroInfoType;
			this.astroName = astroName;
			this.context = context;
			this.message = message;
			
		}
	}
	
	
	private String parseConstellationUrl(String constellation, int year, int month, int monthDay, int astroInfoType, String astroName) {
		String searchStr ="&t=?&var=h1info&a=?&y=?&m=?&d=?"; 
				//"/var=hlinfo&a=?&y=?&m=?&d=?";
		searchStr = searchStr.replaceFirst("\\?", String.valueOf(astroInfoType));
		searchStr = searchStr.replaceFirst("\\?", astroName);
		searchStr = searchStr.replaceFirst("\\?", String.valueOf(year));
		searchStr = searchStr.replaceFirst("\\?", String.valueOf(month));
		searchStr = searchStr.replaceFirst("\\?", String.valueOf(monthDay));
		
		return ASTRO_ADDRESS_PREFIX.concat(searchStr);
	}
	
	private JSONObject getConstellationJSON(String url) {
		if(TextUtils.isEmpty(url)) throw new RuntimeException("empty url");
		
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
		HttpConnectionParams.setSoTimeout(httpParams, 3000);
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
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
		}
		
		String jsonStr = null;
		try {
			jsonStr = EntityUtils.toString(response.getEntity());
			Log.d(LOG_TAG, "HttpResponse: " + jsonStr);
		} catch (ParseException e1) {
			Log.d(LOG_TAG, "failed to parse JSON string 1");
			return null;
		} catch (IOException e2) {
			Log.d(LOG_TAG, "failed to parse JSON string 2");
			return null;
		}

		//temp
		if(jsonStr.length()<=13)
		{
			return null;
		}
		
		
		JSONObject result = null;
		try {
			//temp
			result = new JSONObject(jsonStr.substring(13));
			
		} catch(JSONException e) {
			Log.d(LOG_TAG, "throws JSONException");
			return null;
		}
		
		return result;
	}
	
	

	
	
	public class AstroAsyncTask extends AsyncTask<AstroRequest, Void, Void> {
		
		public int astroInfoType;
		Context context;
		
		public int year;
		public int month;
		public int monthDay;
		public String astroName;
		Message message;
		
		
		@Override
		protected Void doInBackground(AstroRequest... requests) {
			
			Log.i("Astro", "come in 3");
			if(requests == null || requests.length == 0) throw new RuntimeException("empty request");
			
			AstroRequest request = requests[0];
			String url = parseConstellationUrl(request.url, request.year, request.month, request.monthDay,request.astroInfoType,request.astroName);
			Log.d(LOG_TAG, "astro url: " + url);
			
			astroInfoType = request.astroInfoType;
			context = request.context;
			astroName =request.astroName; 
			year =request.year;
			month =request.month;
			monthDay =request.monthDay;
			message = request.message;
			
			
			JSONObject jsonobject;
			
			dbo = DBOperations.getInstances(context);
			
			if(astroInfoType==ASTRO_INFO_TYPE_DAY)
			{
				DayAstroInfo dayAstroInfo = null;
				
				dayAstroInfo = dbo.queryDayAstro(context, year, month, monthDay, astroName);
				
				if(dayAstroInfo==null)
				{
					Log.d(LOG_TAG, "query DayAstroInfo is null ");
					
					if(false == getNetworkStatus()) {
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_NO_NETWORK;
						return null;
					}
					
					jsonobject = getConstellationJSON(url);
					if(null==jsonobject)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_NO_DATA;
						return null;
					}
					
					
					dayAstroInfo = parseDayAstroJson(jsonobject);
					
					if(null==dayAstroInfo)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_ERROR_DATA;
						return null;
					}
					
					dayAstroInfo.astroName =astroName; 
					dayAstroInfo.year =year;
					dayAstroInfo.month =month;
					dayAstroInfo.monthDay =monthDay;
					
					long id = -1;
					id = dbo.createDayAstroRecord(context, dayAstroInfo);
					if(id ==-1)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_OTHER_ERROR;
						return null;
					}
					
				}
				else
				{
					//
				}
				
				
				dayAstroInfo.indexCareerStar = (float) (dayAstroInfo.indexCareer/20.0); 
				dayAstroInfo.indexComprehensionStar = (float) (dayAstroInfo.indexComprehension/20.0);
				dayAstroInfo.indexFinanceStar = (float) (dayAstroInfo.indexFinance/20.0);
				dayAstroInfo.indexHealthStar = (float) (dayAstroInfo.indexHealth/20.0);
				dayAstroInfo.indexLoveStar = (float) (dayAstroInfo.indexLove/20.0);
				
				message.arg1 = GET_ASTRO_STATUS_OK;
				message.obj = dayAstroInfo;
				
				
				Log.d(LOG_TAG, "DayAstroInfo: " + dayAstroInfo.selfAstro+" "+ 
						dayAstroInfo.day+" "+ dayAstroInfo.indexComprehension+" "+ 
						dayAstroInfo.indexLove +" "+ dayAstroInfo.indexCareer +
						" "+ dayAstroInfo.indexFinance+
						" "+ dayAstroInfo.indexHealth+
						" "+ dayAstroInfo.LuckyColor+
						" "+ dayAstroInfo.luckyNumber+
						" "+ dayAstroInfo.QFriend+
						" "+ dayAstroInfo.description
						);
			}
			else if(astroInfoType==ASTRO_INFO_TYPE_WEEK)
			{
				WeekAstroInfo weekAstroInfo = null;
				
				weekAstroInfo = dbo.queryWeekAstro(context, year, month, monthDay, astroName);
				
				if(weekAstroInfo==null)
				{
					Log.d(LOG_TAG, "query weekAstroInfo is null ");
					
					if(false == getNetworkStatus()) {
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_NO_NETWORK;
						return null;
					}
					
					jsonobject = getConstellationJSON(url);
					if(null==jsonobject)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_NO_DATA;
						return null;
					}
					
					
					weekAstroInfo = parseConstellationJSON(jsonobject);
					
					if(null==weekAstroInfo)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_ERROR_DATA;
						return null;
					}
					
					weekAstroInfo.astroName =astroName; 
					weekAstroInfo.year =year;
					weekAstroInfo.month =month;
					weekAstroInfo.monthDay =monthDay;
					
					long id = -1;
					id = dbo.createWeekAstroRecord(context, weekAstroInfo);
					if(id ==-1)
					{
						//error do
						message.obj = null;
						message.arg1 = GET_ASTRO_STATUS_OTHER_ERROR;
						return null;
					}
					
				}
				else
				{
					//
				}
				
				
				message.obj = weekAstroInfo;
				message.arg1 = GET_ASTRO_STATUS_OK;
				
				Log.d(LOG_TAG, "weekAstroInfo: " + weekAstroInfo.astro+" "+ 
						weekAstroInfo.weekBegin+" "+ weekAstroInfo.weekEnd+" "+ 
						weekAstroInfo.fortuneType[0]+" "+ weekAstroInfo.fortuneContent[0]+
						" "+ weekAstroInfo.num+" ");
				
			}
			else
			{
				
			}
			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void abc) {
			
			message.sendToTarget();
			
			Log.i("Astro", "come in 4");
			
			
		}
		
		private boolean getNetworkStatus()
		{
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
  
	        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	        
	        if(networkInfo == null || !networkInfo.isAvailable()) {
	        	Log.i("astro", "networkInfo is not Available");
//	        	Intent intent = new Intent("gn.android.intent.action.SHOW_3GWIFIALERT");
//	        	intent.putExtra("appname", "com.android.calendar");
//	        	context.sendBroadcast(intent);

	        	return false;
	        } else {
	        	Log.i("astro", "networkInfo is Available");
	        	return true;	        	
	        }
		}
	}
	
	
	private WeekAstroInfo parseConstellationJSON(JSONObject result) {
		if(result == null) throw new RuntimeException("empty JSONObject");
		
		WeekAstroInfo response = new WeekAstroInfo();
		
		
		try {
			String astroValue = result.getString(JSON_NODE_ASTRO);
			Log.d(LOG_TAG, "astroValue: " + astroValue);
			String begValue = result.getString(JSON_NODE_WEEK_BEGIN);
			Log.d(LOG_TAG, "begValue: " + begValue);
			String endValue = result.getString(JSON_NODE_WEEK_END);
			Log.d(LOG_TAG, "endValue: " + endValue);
			
			response.astro = astroValue;
			response.weekBegin = begValue;
			response.weekEnd = endValue;
		} catch (JSONException e) {
			Log.d(LOG_TAG, "throws JSONException when parse " + JSON_NODE_ASTRO);
			return null;
		}
		
		try {
			JSONArray fortune = result.getJSONArray(JSON_NODE_FORTUNE);
			//result.g
			Log.d(LOG_TAG, "fortune array length: " + fortune.length());
			
			response.fortuneType = new String[fortune.length()];
			response.fortuneContent = new String[fortune.length()];
			response.num = fortune.length();
			
			for(int i = 0, len = fortune.length(); i < len; ++i) {
				JSONObject fortuneObject = fortune.getJSONObject(i);
				String typeValue = fortuneObject.getString(JSON_NODE_TYPE);
				Log.d(LOG_TAG, "typeValue: " + typeValue);
				
				String contentValue = fortuneObject.getString(JSON_NODE_CONTENT);
				Log.d(LOG_TAG, "contentValue: " + contentValue);
				
				response.fortuneType[i] = typeValue;
				response.fortuneContent[i] = contentValue;
				
			}
		} catch (JSONException e) {
			Log.d(LOG_TAG, "throws JSONException when parse " + JSON_NODE_FORTUNE);
			return null;
		}
		
		return response;
	}
	
	private DayAstroInfo parseDayAstroJson(JSONObject weekJson) {
		
		if(weekJson == null) throw new RuntimeException("empty JSONObject of day");
		
		DayAstroInfo info = new DayAstroInfo();
		
		try {
			String astroValue = weekJson.getString("astro");
			info.selfAstro = astroValue; 
			Log.d(LOG_TAG, "astroValue: " + astroValue);
		} catch(JSONException e) {
			Log.d(LOG_TAG, "fail to parse JSONArray");
			return null;
		}
		
		try {
			String dayValue = weekJson.getString("day");
			info.day = dayValue;
			Log.d(LOG_TAG, "astroValue: " + dayValue);
		} catch(JSONException e) {
			Log.d(LOG_TAG, "fail to parse JSONArray");
			return null;
		}
		
		
		
		JSONArray fortune = null;
		try {
			fortune = weekJson.getJSONArray("fortune");
			
			if(fortune.length()!=9)
			{
				Log.d(LOG_TAG, "the JSONArray length is error");
				return null;				
			}
		} catch(JSONException e) {
			Log.d(LOG_TAG, "fail to parse JSONArray");
			return null;
		}		
		
		try {
			int i = 0;
			JSONObject element;
			String type;
			String content;
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeComprehension = type;
			info.indexComprehension =percentString2Int(content);
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeLove = type;
			info.indexLove =percentString2Int(content);
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeCareer = type;
			info.indexCareer =percentString2Int(content);
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeFinance = type;
			info.indexFinance =percentString2Int(content);
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeHealth = type;
			info.indexHealth =percentString2Int(content);
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeLuckyColor = type;
			info.LuckyColor =content;
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeluckyNumber = type;
			info.luckyNumber =content;
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeQFriend = type;
			info.QFriend =content;
			
			element = fortune.getJSONObject(i);
			i++;
			type = element.getString("type");
			content = element.getString("content");
			info.typeDescription = type;
			info.description =content;
			
			
			
			
					

			
		} catch(JSONException e) {
			Log.d(LOG_TAG, "fail to parse JSONArray elements");
			return null;
		}
		
		
		
		return info;
	}
	
	
	public static void getAstroInfo(String constellation, int year, int month, int monthDay, int astroInfoType,Context context,Message message) {
		
		GNAstroUtils gnAstroUtils = new GNAstroUtils();
		
		AstroRequest request = gnAstroUtils.new AstroRequest(constellation, year, month, monthDay, astroInfoType,constellation,context,message);
				//("123", 2013, 5, 23,3,ASTRO_NAME_CANCER);
		AstroAsyncTask task = gnAstroUtils.new AstroAsyncTask();
    	task.execute(request);
    	Log.i("Astro", "come in 2");
	}
	
	
	public void getDatabase() {
		
		
		
	}
	
}
