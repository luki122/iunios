package com.aurora.change.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.change.model.NextDayPictureInfo;

import android.R.integer;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class NextDayInitTask extends AsyncTask<Object, Object, Boolean>{
	private Handler mHandler;
	
	private Context mContext;
	private String url;
	private JSONObject object;
	private String date;
	private String resolution;
	private String operationType;
	private int dataLength = 0;
	
	private String resultData = null;
	
	public NextDayInitTask(Handler handler) {
		// TODO Auto-generated constructor stub
		mHandler = handler;
	}
	
	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		mContext = (Context) params[0];
		url = (String) params[1];
		object = (JSONObject) params[2];
		date = (String) params[3];
		resolution = (String) params[4];
//		operationType = (String) params[5];
		dataLength = (Integer) params[5];
		
		String result = HttpClientHelper.httpClientPost(url, object);
		if (result == null) return false;
		
		JSONObject initResult = null;
		try {
			initResult = new JSONObject(result);
			if (initResult == null) return false;
			
			if ("1".equals(initResult.optString("code")) && "ok".equals(initResult.optString("desc"))) {
				String data = initResult.optString("data");
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------data = "+data);
				JSONObject dataObject = new JSONObject(data);
				String sysTime = dataObject.optString("sysTime");
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------sysTime = "+sysTime);
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------Long.valueOf(time) = "+Long.valueOf(sysTime));
				Long deltaTime = System.currentTimeMillis() - Long.valueOf(sysTime);
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------deltaTime = "+deltaTime);
//				StringBuilder gettingUrl = new StringBuilder("http://i.iunios.com/lockscreen/getdata");
//				StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA_TEST);
				StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA);
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------dataLength = "+dataLength);
				if (dataLength == 0) {
					gettingUrl.append("?date=" + date + "&resolution=" + resolution);
				} else {
					gettingUrl.append("?date=" + date + "&resolution=" + resolution + "&days=" + String.valueOf(dataLength));
				}
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------gettingUrl = "+gettingUrl.toString());
				
				Long currentTime = System.currentTimeMillis() - deltaTime;
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------currentTime = "+currentTime);
				String token = Consts.NEXTDAY_APP_ID + Consts.NEXTDAY_APP_KEY + String.valueOf(currentTime);
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------token = "+token);
				
				String md5Token = CommonUtil.messageDiestBuilder(token);
//	            Log.d("Wallpaper_DEBUG", "NextDayInitTask---------------doInBackground--------MD5 = "+md5Token);
		        
				resultData = HttpClientHelper.httpClientGet(gettingUrl.toString(), md5Token, Consts.NEXTDAY_APP_ID, String.valueOf(currentTime));
//				Log.d("Wallpaper_DEBUG", "NextDayInitTask---------doInBackground-------resultData = "+resultData);
				
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("Wallpaper_DEBUG", "NextDayInitTask---------doInBackground-------JSONException = "+e);
		}
		
		if (resultData == null) {
			return false;
		} else {
			return true;
		}
	}
	
	protected void onPostExecute(Boolean result) {
 		if (!result) return;
 		
 		JSONObject resultJson;
 		JSONArray resultJsonArray = null;
// 		String dataString = null;
		try {
			resultJson = new JSONObject(resultData);
			if ("1".equals(resultJson.optString("code")) && "ok".equals(resultJson.optString("desc"))) {
				if (dataLength == 0) {
//					dataString = resultJson.optString("data");
					resultJsonArray = new JSONArray();
					resultJsonArray.put((JSONObject) resultJson.opt("data"));
				} else {
					resultJsonArray = resultJson.optJSONArray("data");
				}
			}
			if (resultJsonArray == null) return;
			
			Log.d("Wallpaper_DEBUG", "NextDayInitTask---------onPostExecute-------resultArray.length = "+resultJsonArray.length());
			
			if (mHandler != null) {
				Message message = Message.obtain();
				message.what = Consts.LOCKPAPER_INFO_LOAD_DONE;
				if (resultJsonArray != null) {
					message.obj = resultJsonArray;
				}
				mHandler.sendMessage(message);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("Wallpaper_DEBUG", "NextDayInitTask-----------onPostExecute-------JSONException = "+e);
		}
 		
 	}

}
