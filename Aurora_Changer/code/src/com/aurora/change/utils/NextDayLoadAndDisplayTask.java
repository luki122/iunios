package com.aurora.change.utils;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.change.AuroraChangeApp;
import com.aurora.change.data.NextDayDbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.aurora.change.R;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class NextDayLoadAndDisplayTask extends AsyncTask<Object, Object, Boolean>{
	private Handler mHandler;
	
	private Context mContext;
	private NextDayPictureInfo pictureInfo;
	private String resolution;
	private String loadingType;
	
	private String resultData = null;
	
	private ImageView imageView;
	private DisplayImageOptions mOptions;
	private ProgressBar mProgressBar;
	
	public NextDayLoadAndDisplayTask(Handler handler) {
		// TODO Auto-generated constructor stub
		mHandler = handler;
	}
	
	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		mContext = (Context) params[0];
		pictureInfo = (NextDayPictureInfo) params[1];
		resolution = (String) params[2];
		loadingType = (String) params[3];
		
		imageView = (ImageView) params[4];
		mOptions = (DisplayImageOptions) params[5];
		mProgressBar = (ProgressBar) params[6];
		
		if (Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(loadingType) || Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE.equals(loadingType)) {
			return true;
			
		} else {
			if (Consts.NEXTDAY_PICTURE_LOADTYPE_INFO.equals(loadingType)) {
				String filePath = pictureInfo.getPictureOriginalUrl();
				String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask---------doInBackground-------filePath = "+filePath);
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask---------doInBackground-------originalPath = "+originalPath);
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask---------doInBackground-------fileIsExist(originalPath) = "
				+FileHelper.fileIsExist(originalPath));
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask---------doInBackground-------fileIsExist(filePath) = "
				+FileHelper.fileIsExist(filePath.replace("file://", "")));
				
				if (!filePath.equals(originalPath) && !FileHelper.fileIsExist(originalPath) && FileHelper.fileIsExist(filePath.replace("file://", "")) ) {
					CommonUtil.copyFile(filePath.replace("file://", ""), originalPath);
				}
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
	//				StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA_TEST);
					StringBuilder gettingUrl = new StringBuilder(Consts.NEXTDAY_URL_GETDATA);
					gettingUrl.append("?date=" + pictureInfo.getPictureTime() + "&resolution=" + resolution);
					Long currentTime = System.currentTimeMillis() - deltaTime;
					String token = Consts.NEXTDAY_APP_ID + Consts.NEXTDAY_APP_KEY + String.valueOf(currentTime);
					String md5Token = CommonUtil.messageDiestBuilder(token);
		            resultData = HttpClientHelper.httpClientGet(gettingUrl.toString(), md5Token, Consts.NEXTDAY_APP_ID, String.valueOf(currentTime));
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask---------doInBackground-------JSONException = "+e);
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
 		
 		if (Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(loadingType) || Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE.equals(loadingType)) {
 			//go ahead
 			
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
	     		
//				NextDayPictureInfo pictureInfo = new NextDayPictureInfo();
				pictureInfo.setPictureName(dataJson.optString("date"));
				pictureInfo.setPictureTime(dataJson.optString("date"));
				pictureInfo.setPictureDimension(resolution);
				pictureInfo.setPictureThumnailUrl(dataJson.optString("picUrl" + resolution.replace("*", "X")));
				pictureInfo.setPictureOriginalUrl(dataJson.optString("picUrl" + resolution.replace("*", "X")));
				pictureInfo.setPictureCommentCity(dataJson.optString("commentCity"));
				pictureInfo.setPictureComment(dataJson.optString("comment"));
				pictureInfo.setPictureTimeColor(dataJson.optString("timeWidgetColor"));
				pictureInfo.setPictureStatusColor(dataJson.optString("timeWidgetColor"));
				
				//insert db
				resultJsonArray = new JSONArray();
				resultJsonArray.put(dataJson);
				
				NextDayDbControl mNextDayDbControl = new NextDayDbControl(mContext);
				mNextDayDbControl.insertPictureInfoSafety(resultJsonArray);
				
				if (Consts.NEXTDAY_PICTURE_LOADTYPE_INFO.equals(loadingType)) {
					if (mHandler != null) {
						Message message = Message.obtain();
						message.what = Consts.LOCKPAPER_NEXTDAY_UPDATE;
						message.obj = pictureInfo;
						mHandler.sendMessage(message);
					}
					return;
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask-----------onPostExecute-------JSONException = "+e);
				mProgressBar.setVisibility(View.GONE);
				imageView.setBackgroundResource(R.drawable.wallpaper_load_server_error);
				return;
			}
		}
 		
 		if (mHandler != null) {
			Message message = Message.obtain();
			message.what = Consts.LOCKPAPER_NEXTDAY_UPDATE;
			message.obj = null;
			mHandler.sendMessage(message);
		}
 		Log.d("Wallpaper_DEBUG", "NextDayLoadAndDisplayTask-------onPostExecute-----loadingType = "+loadingType+" url = "+pictureInfo.getPictureOriginalUrl());
 		if (pictureInfo.getPictureOriginalUrl() != null) {
			((AuroraChangeApp) mContext.getApplicationContext()).getImageLoaderHelper().updatePreviewPicture(pictureInfo, 
										pictureInfo.getPictureOriginalUrl(), loadingType, imageView, mOptions, mProgressBar);
		}
 		
 	}

}
