package com.aurora.change.utils;

import java.io.File;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.change.AuroraChangeApp;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.NextDayDbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.aurora.change.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class NextDayLoadAndProcessTask extends AsyncTask<Object, Object, Boolean>{
	private Handler mHandler;
	
	private Context mContext;
	private NextDayPictureInfo pictureInfo;
	private String resolution;
	private String operationType;
	private String loadingType;
	private String filePath;
//	private ProgressBar mProgressBar;
	
	private String resultData = null;
	Bitmap originalBitmap = null;
//	Bitmap targetBitmap = null;
	
	public NextDayLoadAndProcessTask(Handler handler) {
		// TODO Auto-generated constructor stub
		mHandler = handler;
	}
	
	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		mContext = (Context) params[0];
		pictureInfo = (NextDayPictureInfo) params[1];
		resolution = (String) params[2];
		operationType = (String) params[3];
		loadingType = (String) params[4];
		filePath = (String) params[5];
//		mProgressBar = (ProgressBar) params[5];
		
		if (Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(loadingType)) {
			return true;
			
		} else if (Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE.equals(loadingType) && pictureInfo.getPictureComment() != null) {
			return true;
			
		} else {
			if (mHandler != null) {
				Message msgMessage = Message.obtain();
				msgMessage.what = Consts.LOCKPAPER_NEXTDAY_SHOW_TIPS;
				mHandler.sendMessage(msgMessage);
			}
			
			if (Consts.NEXTDAY_PICTURE_LOADTYPE_INFO.equals(loadingType)) {
				String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				File originalFile = new File(originalPath);
				BitmapFactory.Options mOptions = new BitmapFactory.Options();
//				mOptions.inJustDecodeBounds = true;
				mOptions.inTempStorage = new byte[16 * 1024];
				
				if (originalFile.exists()) {
					originalBitmap = BitmapFactory.decodeFile(originalPath, mOptions);
				}
				if (originalBitmap == null) return false;
			}
			
			String result = HttpClientHelper.httpClientPost(Consts.NEXTDAY_URL_INIT, WallpaperConfigUtil.getJsonDataForInit());
			if (result == null) return false;
			
			JSONObject initResult = null;
			try {
				initResult = new JSONObject(result);
				if (initResult == null) return false;
				
				if ("1".equals(initResult.optString("code")) && "ok".equals(initResult.optString("desc"))) {
					String data = initResult.optString("data");
					JSONObject dataObject = new JSONObject(data);
					String sysTime = dataObject.optString("sysTime");
					Long deltaTime = System.currentTimeMillis() - Long.valueOf(sysTime);
//					StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA_TEST);
					StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA);
					gettingUrl.append("?date=" + pictureInfo.getPictureTime() + "&resolution=" + resolution);
//					Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------doInBackground-------gettingUrl = "+gettingUrl.toString());
					Long currentTime = System.currentTimeMillis() - deltaTime;
					String token = Consts.NEXTDAY_APP_ID + Consts.NEXTDAY_APP_KEY + String.valueOf(currentTime);
//					Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------doInBackground-------token = "+token);
					String md5Token = CommonUtil.messageDiestBuilder(token);
//					Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------doInBackground-------md5Token = "+md5Token);
		            resultData = HttpClientHelper.httpClientGet(gettingUrl.toString(), md5Token, Consts.NEXTDAY_APP_ID, String.valueOf(currentTime));
		            Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------doInBackground-------resultData = "+resultData);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------doInBackground-------JSONException = "+e);
			}
			
			if (resultData == null) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	protected void onPostExecute(Boolean result) {
 		if (!result) return;
 		 		
 		if (Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(loadingType)) {
 			WallpaperConfigUtil.pictureProcessForNextDay(mContext, mHandler, pictureInfo, operationType, filePath, null);
 			 			
		} else if (Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE.equals(loadingType) && pictureInfo.getPictureComment() != null) {
			if (pictureInfo.getPictureOriginalUrl() != null) {
				((AuroraChangeApp) mContext.getApplicationContext()).getImageLoaderHelper().LoadAndProcessPicture(pictureInfo, 
													pictureInfo.getPictureOriginalUrl(), operationType, loadingType, filePath, null);
			}
			
		} else {
	 		JSONObject resultJson;
	 		String dataString = null;
	 		JSONArray resultJsonArray = null;
			try {
				resultJson = new JSONObject(resultData);
				if ("1".equals(resultJson.optString("code")) && "ok".equals(resultJson.optString("desc"))) {
					dataString = resultJson.optString("data");
				}
				if (dataString == null) return;
								
				JSONObject dataJson = new JSONObject(dataString);
	     		
				pictureInfo.setPictureName(dataJson.optString("date"));
				pictureInfo.setPictureTime(dataJson.optString("date"));
				pictureInfo.setPictureDimension(resolution);
				pictureInfo.setPictureThumnailUrl(dataJson.optString("picUrl" + resolution.replace("*", "X")));
				pictureInfo.setPictureOriginalUrl(dataJson.optString("picUrl" + resolution.replace("*", "X")));
				pictureInfo.setPictureCommentCity(dataJson.optString("commentCity"));
				pictureInfo.setPictureComment(dataJson.optString("comment"));
				pictureInfo.setPictureTimeColor(dataJson.optString("timeWidgetColor"));
				pictureInfo.setPictureStatusColor(dataJson.optString("timeWidgetColor"));
				
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------onPostExecute-------pictureInfo = "+pictureInfo);
				
				//insert db
				if (Consts.NEXTDAY_OPERATION_WAKEUP.equals(operationType)) {
					Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------onPostExecute-------NEXTDAY_OPERATION_WAKEUP = ");
					resultJsonArray = new JSONArray();
					resultJsonArray.put(dataJson);
					
					NextDayDbControl mNextDayDbControl = new NextDayDbControl(mContext);
					mNextDayDbControl.insertPictureInfoSafety(resultJsonArray);
				}
				
				if (Consts.NEXTDAY_PICTURE_LOADTYPE_INFO.equals(loadingType)) {
					WallpaperConfigUtil.pictureProcessForNextDay(mContext, mHandler, pictureInfo, operationType, filePath, originalBitmap);
					
				} else {
					if (pictureInfo.getPictureOriginalUrl() != null) {
						((AuroraChangeApp) mContext.getApplicationContext()).getImageLoaderHelper().LoadAndProcessPicture(pictureInfo, 
															pictureInfo.getPictureOriginalUrl(), operationType, loadingType, filePath, null);
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask-----------onPostExecute-------JSONException = "+e);
				if (originalBitmap != null) {
					originalBitmap.recycle();
				}
//				mProgressBar.setVisibility(View.GONE);
//				imageView.setBackgroundResource(R.drawable.wallpaper_load_server_error);
//				return;
			}
		}
 		
 	}

}
