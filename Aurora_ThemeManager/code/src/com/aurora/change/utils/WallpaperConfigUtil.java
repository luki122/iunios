package com.aurora.change.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.R;
import com.aurora.change.activities.WallpaperManagerActivity;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.data.DbHelper;
import com.aurora.change.data.NextDayDbControl;
import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.view.NextDayClockImageView;
import com.aurora.change.view.NextDayTimeLayout;

import android.R.integer;
import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.Xml;

public class WallpaperConfigUtil {
	private static String mConfigVersion;
	
	private static boolean mSwithFlag = false;
	private static String mDestPath = null;
	
	//for NextDay wallpaper
	private static int firstMinute = -1;

	
	public static String getConfigVersion() {
		Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------getConfigVersion");
		String filePath = Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_PATH + Consts.WALLPAPER_SET_FILE;
		parseFile(filePath, null);
		
		return mConfigVersion;
	}

	public static ThemeInfo parseSystemThemeByName(Context mContext, String themeName) {
//		String filePath = themeName + "/" + Consts.LOCKPAPER_SET_FILE;
		String filePath = themeName + File.separator + Consts.LOCKPAPER_SET_FILE;
		ThemeInfo mThemeInfo = new ThemeInfo();
		parseFile(filePath, mThemeInfo);
		
		return mThemeInfo;
	}
	
	public static void parseFile(String filePath, ThemeInfo mThemeInfo) {
		File mFile = new File(filePath);

        XmlPullParser parser = Xml.newPullParser();
		try {
			InputStream inputStream = new FileInputStream(mFile);
			parser.setInput(inputStream, "utf-8");
			int eventType = parser.getEventType();
			
			while (XmlPullParser.END_DOCUMENT != eventType) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					
					break;
					
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					if ("wallpaper".equals(tagName)) {
						if (filePath.contains(Consts.WALLPAPER_SET_FILE)) {
							mConfigVersion = parser.getAttributeValue(0);
//							Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------mConfigVersion = "+mConfigVersion);
							break;
						}
						
					} else if ("lockpaper".equals(tagName)) {
						if (mThemeInfo != null) {
							mThemeInfo.name = parser.getAttributeValue(0);
							mThemeInfo.isDefault = parser.getAttributeValue(1);
						}
//						Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------mThemeInfo.name = "+mThemeInfo.name);
//						Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------mThemeInfo.isDefault = "+mThemeInfo.isDefault);
						
					} else if ("title_color".equals(tagName)) {
						if (mThemeInfo != null) {
							mThemeInfo.nameColor = parser.nextText();
						}
//						Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------mThemeInfo.nameColor = "+mThemeInfo.nameColor);
						
					} else if ("time_black".equals(tagName)) {
						if (mThemeInfo != null) {
							mThemeInfo.timeBlack = parser.nextText();
						}
//						Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------mThemeInfo.timeBlack = "+mThemeInfo.timeBlack);
						
					} else if ("statusbar_black".equals(tagName)) {
						if (mThemeInfo != null) {
							mThemeInfo.statusBarBlack = parser.nextText();
						}
//						Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------statusBarBlack = "+mThemeInfo.statusBarBlack);
						
					}
					break;
					
				case XmlPullParser.END_TAG:
					String endName = parser.getName();
					
					break;
				}
				eventType = parser.next();
			}
			
		} catch (Exception e) {
			Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil-------parseFile-----------e = "+e);
		}
	}
	
	public static void setSwithFlag(boolean value) {
		mSwithFlag = value;
	}
	
	public static void setDestinationPath(String path) {
		mDestPath = path;
	}
	
	public static boolean getSwithFlag() {
		return mSwithFlag;
	}
	
	public static String getDestinationPath() {
		return mDestPath;
	}
	
	public static void updateSystemDefaultWallpaper(Context mContext){
//        File file = new File(Consts.LOCKSCREEN_WALLPAPER_PATH);
        String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, null);
        if (current_group == null) {
        	DbControl mDbControl = new DbControl(mContext);
        	PictureGroupInfo groupInfo = mDbControl.queryDefaultGroup();
        	if (groupInfo != null) {
        		current_group = groupInfo.getDisplay_name();
			} else {
				current_group = Consts.DEFAULT_LOCKPAPER_GROUP;
			}
        	mDbControl.close();
		}
        
        Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateSystemDefaultWallpaper-------current_group = "+current_group);
        String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, current_group);
        
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//      FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
        FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
    }

    public static String creatWallpaperConfigurationXmlFile(String filePath, ThemeInfo mThemeInfo) {
    	File myFile = new File(filePath);
		
    	try {
    		FileOutputStream fos = new FileOutputStream(myFile);
        	
        	XmlSerializer serializer = Xml.newSerializer();
        	/*StringWriter writer = new StringWriter();
    		serializer.setOutput(writer);*/
        	
        	serializer.setOutput(fos, "UTF-8");
        	
    		// <?xml version=”1.0″ encoding=”UTF-8″ standalone=”yes”?>
    		serializer.startDocument("UTF-8", true);
    		
    		// <wallpaper>
    		serializer.startTag("", "wallpaper");
    		
    		// <lockpaper name="name", default="defaultValue">
    		serializer.startTag("", "lockpaper");
    		serializer.attribute("", "name", mThemeInfo.name);
    		serializer.attribute("", "default", mThemeInfo.isDefault);
    		
    		// <title_color>titleColor</title_color>
    		serializer.startTag("", "title_color");
    		serializer.text(mThemeInfo.nameColor);
    		serializer.endTag("", "title_color");
    		
    		// <time_black>timeBlack</time_black>
    		serializer.startTag("", "time_black");
    		serializer.text(mThemeInfo.timeBlack);
    		serializer.endTag("", "time_black");
    		
    		// <statusbar_black>statusbarBlack</statusbar_black>
    		serializer.startTag("", "statusbar_black");
    		serializer.text(mThemeInfo.statusBarBlack);
    		serializer.endTag("", "statusbar_black");
    		
    		// </lockpaper>
    		serializer.endTag("", "lockpaper");
    		    		
    		// </wallpaper>
    		serializer.endTag("","wallpaper");
    		serializer.endDocument();
    		
    		fos.flush();
    		fos.close();
    		
//    		return writer.toString();
    		return filePath;
    		
    	} catch(Exception e) {
    		Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------creatWallpaperConfigurationXmlFile-------e = "+e);
    		throw new RuntimeException(e);
    	}
    }
    
    //for NextDay wallpaper
	public static JSONObject getJsonDataForInit() {
		JSONObject initObj = new JSONObject();
    	try {
			initObj.put(Consts.NEXTDAY_INITDATA_KEY, Consts.NEXTDAY_INITDATA_VALUE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------getJsonDataForInit-------Exception = "+e);
		}
    	return initObj;
	}
	
	public static void checkNextDayWallpaperSetting(Context mContext) {
    	String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
    	if (!mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(current_group)) return;
    	
    	if (CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.NO_NET) return;
    	
    	boolean isWifi = DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_WIFI_NETWORK_SETTINGS, false);
    	if (isWifi) {
			if (CommonUtil.getNetWorkType(mContext) != CommonUtil.NetWorkType.WIFI) return;
		}
    	
    	String time = CommonUtil.getCurrentTime();
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------time = "+time+" and the length = "+time.length());
    	int currentHour = Integer.valueOf(time.substring(9, 11));
    	int currentMinute = Integer.valueOf(time.substring(12, 14));
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------currentHour = "+currentHour+" and the currentMinute = "+currentMinute);
    	String pictureDate = DataOperation.getStringPreference(mContext, Consts.NEXTDAY_PICTURE_DATE, null);
    	int hour = 0;
    	int minute = 0;
    	
    	if (pictureDate == null) {
    		DataOperation.setStringPreference(mContext, Consts.NEXTDAY_PICTURE_DATE, time);
    		hour = currentHour;
    		minute = currentMinute;
    		
		} else {
			if (currentHour < 9) return;
			
			hour = Integer.valueOf(pictureDate.substring(9, 11));
			minute = Integer.valueOf(pictureDate.substring(12, 14));
		}
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------firstMinute = "+firstMinute);
    	if (firstMinute < 0) {
			firstMinute = currentMinute;
			
		} else {
			if (pictureDate != null && time.substring(0, 11).equals(pictureDate.substring(0, 11))) {
				if (currentMinute - firstMinute < 10) return;
			}
		}
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------firstMinute222 = "+firstMinute);

    	String filePath = Consts.NEXTDAY_WALLPAPER_SAVED + time.substring(0, 8) + "_comment" + ".jpg";
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------filePath = "+filePath+" file.exist = "+FileHelper.fileIsExist(filePath));
    	if (FileHelper.fileIsExist(filePath)) return;
    	
    	Log.d("Wallpaper_DEBUG", "checkNextDayWallpaperSetting---------start to load image!!!!!!!!!!!!");
    	
    	int width = ((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayWidth();
    	int height = ((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayHeight();
    	
    	NextDayPictureInfo pictureInfo = new NextDayPictureInfo();
    	pictureInfo.setPictureTime(time.substring(0, 8));
    	NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(null);
		loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, 
				Consts.NEXTDAY_OPERATION_WAKEUP, Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD, filePath);

    }
	
	public static Bitmap nextdayPictureCompose(Context mContext, String operationType, NextDayPictureInfo pictureInfo, 
													Bitmap originalBitmap, Bitmap targetBitmap) {		
		int width = originalBitmap.getWidth();
		int hight = originalBitmap.getHeight();
		
		targetBitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);	//建立一个空的BItMap
		Canvas canvas = new Canvas(targetBitmap);//初始化画布绘制的图像到targetBitmap上
		
		Paint photoPaint = new Paint();	//建立画笔
		photoPaint.setDither(true); //获取跟清晰的图像采样
		photoPaint.setFilterBitmap(true);//过滤一些
		photoPaint.setAntiAlias(true);
		
		Bitmap logoBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.nextday_vendor_logo);
		
		canvas.drawBitmap(originalBitmap, 0, 0, photoPaint);
//		canvas.drawBitmap(logoBitmap, 22f*3, 580.67f*3, photoPaint);
		canvas.drawBitmap(logoBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_comment_left), 
									  mContext.getResources().getDimension(R.dimen.nextday_preview_logo_top), 
									  photoPaint);
		
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);	//建立画笔
//		textPaint.setTextSize(36.0f);
		//textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		
		if (Consts.NEXTDAY_OPERATION_SAVE.equals(operationType) || Consts.NEXTDAY_OPERATION_SHARE.equals(operationType)) {
			String time = pictureInfo.getPictureTime();
			int[] TIME_KEY_RES = new int[11];
			if ("white".equals(pictureInfo.getPictureTimeColor())) {
				TIME_KEY_RES = NextDayClockImageView.TIME_KEY_WHITE;
			} else {
				TIME_KEY_RES = NextDayClockImageView.TIME_KEY_BLACK;
			}
			
			String number;
			Bitmap timeBitmap;
			
			//draw the picture of date, such as 04/28
			number = time.substring(4, 5);
			timeBitmap = BitmapFactory.decodeResource(mContext.getResources(), TIME_KEY_RES[Integer.valueOf(number)]);
//			canvas.drawBitmap(timeBitmap, 62.67f*3, 80.33f*3, photoPaint);
			canvas.drawBitmap(timeBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_clock_left), 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_top), photoPaint);
			
			number = time.substring(5, 6);
			timeBitmap = BitmapFactory.decodeResource(mContext.getResources(), TIME_KEY_RES[Integer.valueOf(number)]);
//			canvas.drawBitmap(timeBitmap, 62.67f*3 + 148 + 3, 80.33f*3, photoPaint);
			canvas.drawBitmap(timeBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_clock_left) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_width) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_1dp), 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_top), photoPaint);
			
			timeBitmap = BitmapFactory.decodeResource(mContext.getResources(), TIME_KEY_RES[Integer.valueOf(10)]);
//			canvas.drawBitmap(timeBitmap, 62.67f*3 + 148*2 + 3 + 6*3, 80.33f*3, photoPaint);
			canvas.drawBitmap(timeBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_clock_left) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_width) * 2 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_1dp) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_6dp), 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_top), photoPaint);
			
			number = time.substring(6, 7);
			timeBitmap = BitmapFactory.decodeResource(mContext.getResources(), TIME_KEY_RES[Integer.valueOf(number)]);
//			canvas.drawBitmap(timeBitmap, 62.67f*3 + 148*2 + 3 + 6*2*3 + 69, 80.33f*3, photoPaint);
			canvas.drawBitmap(timeBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_clock_left) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_width) * 2 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_1dp) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_6dp) * 2 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_divider), 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_top), photoPaint);
			
			number = time.substring(7, 8);
			timeBitmap = BitmapFactory.decodeResource(mContext.getResources(), TIME_KEY_RES[Integer.valueOf(number)]);
//			canvas.drawBitmap(timeBitmap, 62.67f*3 + 148*3 + 3*2 + 6*2*3 + 69, 80.33f*3, photoPaint);
			canvas.drawBitmap(timeBitmap, mContext.getResources().getDimension(R.dimen.nextday_preview_clock_left) + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_width) * 3 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_1dp) * 2 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_margin_6dp) * 2 + 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_divider), 
										  mContext.getResources().getDimension(R.dimen.nextday_preview_clock_top), photoPaint);
			
			//draw the month-day and week
			if ("white".equals(pictureInfo.getPictureTimeColor())) {
				textPaint.setColor(Color.WHITE);
			} else {
				textPaint.setColor(Color.BLACK);
			}
//			textPaint.setTextSize(54.5f);
			textPaint.setTextSize(mContext.getResources().getDimension(R.dimen.lock_screen_date_size));
			number = time.substring(4, 6);
			String[] MONTH_TABLE = NextDayTimeLayout.MONTH_TABLE;
//			canvas.drawText(MONTH_TABLE[Integer.valueOf(number) - 1], 114f*3, 164.33f*3 + 19f*3, textPaint);
			canvas.drawText(MONTH_TABLE[Integer.valueOf(number) - 1], mContext.getResources().getDimension(R.dimen.nextday_preview_date_month_left), 
																	  mContext.getResources().getDimension(R.dimen.nextday_preview_date_month_top) +
																	  mContext.getResources().getDimension(R.dimen.nextday_preview_date_month_height), 
																	  textPaint);
			number = time.substring(6, 8);
//			canvas.drawText(number + "th", 151f*3, 164.33f*3 + 19f*3, textPaint);
			canvas.drawText(number + "th", mContext.getResources().getDimension(R.dimen.nextday_preview_date_day_left), 
					  					   mContext.getResources().getDimension(R.dimen.nextday_preview_date_month_top) +
					  					   mContext.getResources().getDimension(R.dimen.nextday_preview_date_month_height), 
					  					   textPaint);
			
			Calendar calendar = Calendar.getInstance();
			int week = calendar.get(Calendar.DAY_OF_WEEK);
			String[] dayOfWeek = mContext.getResources().getStringArray(R.array.day_of_week);
//			textPaint.setTextSize(48.5f);
			textPaint.setTextSize(mContext.getResources().getDimension(R.dimen.lock_screen_week_size));
			if (week > 0 && dayOfWeek != null) {
//	            canvas.drawText(dayOfWeek[week - 1], 197f*3, 166f*3 + 19f*3 - 2f*3, textPaint);
	            canvas.drawText(dayOfWeek[week - 1], mContext.getResources().getDimension(R.dimen.nextday_preview_date_week_left), 
	            									 mContext.getResources().getDimension(R.dimen.nextday_preview_date_week_top) +
	            									 mContext.getResources().getDimension(R.dimen.nextday_preview_date_week_height), 
	            									 textPaint);
	        }
			
			
			if (timeBitmap != null) {
				timeBitmap.recycle();
			}
		
		}
		
		//draw the commentCity and comment and the shadow of comment
		Paint shadowPaint = new Paint();	//建立画笔
		shadowPaint.setColor(Color.parseColor("#50000000"));
		float textWidth = shadowPaint.measureText(pictureInfo.getPictureComment().toString());
		Log.d("Wallpaper_DEBUG", "@@@@@@@@@@@@@@@@@@@@@@@  textWidth = "+textWidth);
		float density = mContext.getResources().getDisplayMetrics().density;
		Log.d("Wallpaper_DEBUG", "@@@@@@@@@@@@@@@@@@@@@@@  getDisplayMetrics().density = "+density);
//		Rect textBackground = new Rect(0, 600*3, 338*3 - 25*3, 620*3);	//创建一个指定的新矩形的坐标
//		Rect textBackground = new Rect(0, 600*3, ((int)textWidth + 44)*3, 620*3);	//创建一个指定的新矩形的坐标
		//创建一个指定的新矩形的坐标
		Rect textBackground = new Rect(0, (int)mContext.getResources().getDimension(R.dimen.nextday_preview_comment_shadow_top), 
										  (int)(textWidth * density + mContext.getResources().getDimension(R.dimen.nextday_preview_comment_shadow_right)), 
										  (int)mContext.getResources().getDimension(R.dimen.nextday_preview_comment_shadow_bottom));
		canvas.drawRect(textBackground, shadowPaint);
		
		textPaint.setColor(Color.WHITE);
//		textPaint.setTextSize(36.0f);
		textPaint.setTextSize(mContext.getResources().getDimension(R.dimen.nextday_preview_comment_text_size));
		
//		canvas.drawText(pictureInfo.getPictureCommentCity().toString(), 41.33f*3, 581f*3 + 14f*3 - 1.33f*3, textPaint);
//		canvas.drawText(pictureInfo.getPictureComment().toString(), 22f*3, 600f*3 + 14f*3 + 0.67f*3, textPaint);
		canvas.drawText(pictureInfo.getPictureCommentCity().toString(), mContext.getResources().getDimension(R.dimen.nextday_preview_comment_city_left), 
																		mContext.getResources().getDimension(R.dimen.nextday_preview_comment_city_top), 
																		textPaint);
		canvas.drawText(pictureInfo.getPictureComment().toString(), mContext.getResources().getDimension(R.dimen.nextday_preview_comment_left), 
																	mContext.getResources().getDimension(R.dimen.nextday_preview_comment_top), 
																	textPaint);
		
		canvas.save( Canvas.ALL_SAVE_FLAG );//保存
		canvas.restore();
		
		if (logoBitmap != null) {
			logoBitmap.recycle();
		}
		
		return targetBitmap;
	}
	
	public static void updatePictureForNextDay(Context mContext, DbControl mDbControl) {
		boolean result = true;
		for (int i = 0; i < 3; i++) {
			NextDayPictureInfo mPictureInfo = new NextDayPictureInfo();
			String mDate = CommonUtil.getDateFromCurrent(-i);
	    	mPictureInfo.setPictureTime(mDate);
	    	
	    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + ".jpg";
	    	File originalFile = new File(originalPath);
	    	if (originalFile.exists()) {
	    		result = result & true;
	    	} else {
	    		result = result & false;
	    		break;
	    	}
		}
		
		if (!result) return;
		
		boolean needChange = false;
		for (int i = 0; i < 3; i++) {
			NextDayPictureInfo mPictureInfo = new NextDayPictureInfo();
			String mDate = CommonUtil.getDateFromCurrent(-i);
	    	mPictureInfo.setPictureTime(mDate);
	    	
	    	String targetPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + mDate + ".jpg";
	    	File targetFile = new File(targetPath);
	    	if (targetFile.exists()) continue;
	    	
	    	Log.d("Wallpaper_DEBUG", "updatePictureForNextDay------------do change = ");
	    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + ".jpg";
    		boolean res = CommonUtil.copyFile(originalPath, targetPath);
            Log.d("Wallpaper_DEBUG", "updatePictureForNextDay------------res = "+res);
            
    		if (res) {
    			//sort the file name and delete the last two picture
				File file = new File(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay");
				File[] files = file.listFiles();
				if (files.length >= 5) {
	                if (file.isDirectory()) {
						for (File tempFile : files) {
							Arrays.sort(files, new Comparator<File>() {
								@Override
								public int compare(File file1, File file2) {
									// TODO Auto-generated method stub
									return file1.getName().compareToIgnoreCase(file2.getName());
								}
							});
							break;
						}
					}
	                String delPath;
	                if (files[0].toString().equals(targetPath)) {
						delPath = files[i + 1].toString();
					} else {
						delPath = files[0].toString();
					}
	                Log.d("Wallpaper_DEBUG", "updatePictureForNextDay------------delPath = "+delPath);
					FileHelper.deleteFile(delPath);
					
					mDbControl.updateNextDayDB(targetPath, delPath);
				}
				needChange = true;
				
			} else {
				result = result & false;
				break;
			}
	    	
		}
		
		if (result && mDbControl != null && needChange) {
//			DbControl mDbControl = new DbControl(mContext);
//			mDbControl.refreshDb();
			File mFile = new File(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/");
			if (mFile.isDirectory()) {
				File[] files = mFile.listFiles();
				for (File myFile : files) {
					Arrays.sort(files, new Comparator<File>() {
						@Override
						public int compare(File file1, File file2) {
							// TODO Auto-generated method stub
							return file1.getName().compareToIgnoreCase(file2.getName());
						}
					});
					break;
				}
				if (files.length == 4) {
					String targetPath = files[2].toString();
					Log.d("Wallpaper_DEBUG", "updatePictureForNextDay--------targetPath = "+targetPath);
					
					//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//	                FileHelper.copyFile(targetPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
	                FileHelper.copyFile(targetPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
	                //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
				}
				
			}
			
			//delete the item that removed by update, make sure the size = 3
			PictureGroupInfo groupInfo = mDbControl.queryGroupByName("NextDay");
			List<PictureInfo> pictureInfoList = mDbControl.queryAllItemsByGroupId(groupInfo.getId());
			int countInDb = pictureInfoList.size();
			Log.d("Wallpaper_DEBUG", "updatePictureForNextDay------------pictureInfList.size() = "+pictureInfoList.size());
			if (countInDb > 3) {
				for (int i = 0; i < countInDb - 3; i++) {
					String delPathInDb = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/" + pictureInfoList.get(i).getIdentify().replace(File.separator, "");
					Log.d("Wallpaper_DEBUG", "updatePictureForNextDay------------final delPathInDb = "+delPathInDb);
					mDbControl.delPictureByPath(delPathInDb);
					
				}
			}
			
//			mDbControl.close();
		}
		
	}
	
	public static void pictureProcessForNextDay(Context mContext, Handler mHandler, NextDayPictureInfo pictureInfo, 
																String operationType, String filePath, Bitmap originalBitmap) {
		
		String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + pictureInfo.getPictureTime() + ".jpg";
    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
    	
		if (originalBitmap != null) {
			Bitmap targetBitmap = null;
			if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, false) || 
											   Consts.NEXTDAY_OPERATION_SET.equals(operationType) || 
											   Consts.NEXTDAY_OPERATION_WAKEUP.equals(operationType)) {
				targetBitmap = WallpaperConfigUtil.nextdayPictureCompose(mContext, operationType, pictureInfo, originalBitmap, targetBitmap);
			} else {
				targetBitmap = originalBitmap;
			}
			boolean result = FileHelper.writeImage(targetBitmap, filePath, 100);
			Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------------pictureProcessForNextDay--------writeImage preview = "+result);
			
			if (Consts.NEXTDAY_OPERATION_WAKEUP.equals(operationType)) {
				boolean resultInit = FileHelper.writeImage(originalBitmap, initPath, 100);
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------------pictureProcessForNextDay--------writeImage init = "+resultInit);
				boolean resultOriginal = FileHelper.writeImage(originalBitmap, originalPath, 100);
				Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------------pictureProcessForNextDay--------writeImage original = "+resultOriginal);
			}
			
			originalBitmap.recycle();
			if (targetBitmap != null) {
				targetBitmap.recycle();
			}
		}
		
		if (Consts.NEXTDAY_OPERATION_WAKEUP.equals(operationType)) {
			if ("white".equals(pictureInfo.getPictureTimeColor())) {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
			} else {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
			}
			if ("white".equals(pictureInfo.getPictureStatusColor())) {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
			} else {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
			}
			
			//sort the file name and delete the last two picture
			File file = new File(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay");
			File[] files = file.listFiles();
			if (files.length >= 5) {
                if (file.isDirectory()) {
					for (File mFile : files) {
						Arrays.sort(files, new Comparator<File>() {
							@Override
							public int compare(File file1, File file2) {
								// TODO Auto-generated method stub
								return file1.getName().compareToIgnoreCase(file2.getName());
							}
						});
						break;
					}
				}
                String delPath;
                if (files[0].toString().equals(initPath)) {
					delPath = files[files.length - 2].toString();
				} else {
					delPath = files[0].toString();
				}
				FileHelper.deleteFile(delPath);
                
                File[] tempFiles = files;
                
				DbControl mDbControl = new DbControl(mContext);
				
				List<PictureGroupInfo> groupInfos = mDbControl.queryAllGroupInfos();
		    	for (int i = 0; i < groupInfos.size(); i++) {
		            if (groupInfos.get(i).getDisplay_name().equals("NextDay")) {
		                mDbControl.delPictureByPath(files[2].toString());
		                mDbControl.delPictureByPath(files[1].toString());
		                mDbControl.delPictureByPath(files[0].toString());
		                
		                for (int j = 0; j < tempFiles.length; j++) {
		                	String insertPath = "";
		                	if (j == 0) {
								insertPath = initPath;
								
							} else if (j == 1) {
								insertPath = tempFiles[2].toString();
								
							} else if (j == 2) {
								insertPath = tempFiles[1].toString();
							}
		                	ContentValues values = new ContentValues();
		                	String identify = insertPath.replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator, "").replace(".jpg", "");
		                	values.put(DbInfoModel.ImageColumns.IDENTIFY, identify);
		                	values.put(DbInfoModel.ImageColumns.PATH, insertPath);
		                	values.put(DbInfoModel.ImageColumns.BELONG_GROUP, groupInfos.get(i).getId());
		                	
		                	boolean b = mDbControl.insertPicture(values);
		                	Log.d("Wallpaper_DEBUG", "updateNextDayDB--------insert "+identify+" = "+b);
		                	
		                	if (j == 2) break;
						}
		                
		                break;
		            }
		        }
				
                /*mDbControl.updateNextDayDB(initPath, files[2].toString());
                mDbControl.updateNextDayDB(tempFiles[2].toString(), files[1].toString());
                mDbControl.updateNextDayDB(tempFiles[1].toString(), files[0].toString());*/
                
                //delete the item that removed by update, make sure the size = 3
    			PictureGroupInfo groupInfo = mDbControl.queryGroupByName("NextDay");
    			List<PictureInfo> pictureInfoList = mDbControl.queryAllItemsByGroupId(groupInfo.getId());
    			int countInDb = pictureInfoList.size();
    			if (countInDb > 3) {
    				for (int i = 0; i < countInDb - 3; i++) {
    					String delPathInDb = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay/" + pictureInfoList.get(i).getIdentify().replace(File.separator, "");
    					mDbControl.delPictureByPath(delPathInDb);
    				}
    			}
                
            	mDbControl.close();
			}
			
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//          boolean res = FileHelper.copyFile(filePath, Consts.LOCKSCREEN_WALLPAPER_PATH);
            boolean res = FileHelper.copyFile(filePath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
			
		} else if (Consts.NEXTDAY_OPERATION_SAVE.equals(operationType)) {
			//insert picture to the database of file manager
			String destinationPath = filePath.replace("/mnt/sdcard", "");
			File sdcardDir=Environment.getExternalStorageDirectory();
			File mDestinationFile = new File(sdcardDir.toString() + destinationPath);
			
			Long time = Calendar.getInstance().getTimeInMillis();
			String dimention = pictureInfo.getPictureDimension();
			if (dimention == null) return;
			
			int width = Integer.valueOf(dimention.substring(0, dimention.indexOf("*")));
			int height = Integer.valueOf(dimention.substring(dimention.indexOf("*") + 1, dimention.length()));
			
			ContentValues insertValues = CommonUtil.getContentValues(mContext, null, mDestinationFile, time, width, height);
			Object insertResult = mContext.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, insertValues);
			
			CommonUtil.showToast(mContext, mContext.getResources().getString(R.string.nextday_wallpaper_loading_completed));
			Log.d("Wallpaper_DEBUG", "NextDayLoadAndProcessTask---------------onPostExecute--------insertResult = "+insertResult);
				
		} else if (Consts.NEXTDAY_OPERATION_SET.equals(operationType)) {
			Message msgMessage = Message.obtain();
			msgMessage.what = Consts.LOCKPAPER_NEXTDAY_SET_WALLPAPER;
			msgMessage.obj = filePath;
			mHandler.sendMessage(msgMessage);
			
		} else {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_SUBJECT, "share");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
            
            Message msgMessage = Message.obtain();
			msgMessage.what = Consts.LOCKPAPER_NEXTDAY_LOAD_COMPLETE;
			msgMessage.obj = intent;
			mHandler.sendMessage(msgMessage);
            
//			mContext.startActivity(Intent.createChooser(intent, mContext.getResources().getString(R.string.share_wallpaper)));
		}
	}
	
	public static void updateDBForNextDayGroup(Context mContext, ArrayList<String> resultFileList) {
		String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay";
		
		//sort the file by name
		File file = new File(initPath);
		File[] files = file.listFiles();
		if (files.length >= 4) {
            if (file.isDirectory()) {
				for (File mFile : files) {
					Arrays.sort(files, new Comparator<File>() {
						@Override
						public int compare(File file1, File file2) {
							// TODO Auto-generated method stub
							return file1.getName().compareToIgnoreCase(file2.getName());
						}
					});
					break;
				}
			}
		}
		
		Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateDBForNextDayGroup----resultFileList.size() = "+resultFileList.size());
		Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateDBForNextDayGroup----files.length = "+files.length);
		if (resultFileList.size() == 3) {
			for (int i = 0; i < files.length - 1; i++) {
				if (!files[i].toString().equals(resultFileList.get(0)) && 
					!files[i].toString().equals(resultFileList.get(1)) && 
					!files[i].toString().equals(resultFileList.get(2))) {
					
					boolean del = FileHelper.deleteFile(files[i].toString());
					Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateDBForNextDayGroup--------" +
							"delete init File---files["+i+"] = "+files[i].toString()+"  "+del);
				}
			}
			
		} else {
			resultFileList.clear();
			resultFileList.add(0, files[2].toString());
			resultFileList.add(1, files[1].toString());
			resultFileList.add(2, files[0].toString());
		}
		
		DbControl mDbControl = new DbControl(mContext);
		
		List<PictureGroupInfo> groupInfos = mDbControl.queryAllGroupInfos();
    	for (int i = 0; i < groupInfos.size(); i++) {
            if (groupInfos.get(i).getDisplay_name().equals("NextDay")) {
            	int groupId = groupInfos.get(i).getId();
            	List<PictureInfo> pictureList = mDbControl.queryAllItemsByGroupId(groupId);
            	
            	for (int j = 0; j < pictureList.size(); j++) {
            		String delPitcurePath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + pictureList.get(j).getIdentify() + ".jpg";
					boolean b = mDbControl.delPictureByPath(delPitcurePath);
					Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateDBForNextDayGroup-----------delete delPitcurePath "+delPitcurePath+"  "+b);
				}
            	
            	for (int j = 0; j < resultFileList.size(); j++) {
            		ContentValues values = new ContentValues();
 	                String identify = resultFileList.get(j).replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator, "").replace(".jpg", "");
 	                values.put(DbInfoModel.ImageColumns.IDENTIFY, identify);
 	                values.put(DbInfoModel.ImageColumns.PATH, resultFileList.get(j));
 	                values.put(DbInfoModel.ImageColumns.BELONG_GROUP, groupId);
 	                boolean b = mDbControl.insertPicture(values);
 	                Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------updateDBForNextDayGroup-----------insert "+identify+"  "+b);
				}
                                
                break;
            }
        }
    	mDbControl.close();
    	
    	String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
    	if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(current_group)) {
    		//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
        	boolean res = FileHelper.copyFile(resultFileList.get(0), Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
		}
    	
    	//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//      boolean res = FileHelper.copyFile(resultFileList.get(0), Consts.LOCKSCREEN_WALLPAPER_PATH);
//    	boolean res = FileHelper.copyFile(resultFileList.get(0), Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
		
	}
	
	public static void updateNextDayGroupDB(Context mContext) {
		if (!FileHelper.fileIsExist(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay")) return;
		
		String mDate;
		String time = CommonUtil.getCurrentTime();
    	int currentHour = Integer.valueOf(time.substring(9, 11));
    	int currentMinute = Integer.valueOf(time.substring(12, 14));
    	
    	int count = 0;
    	ArrayList<String> resultFileList = new ArrayList<String>();
    	String resultFilePath = null;
    	
    	for (int i = 0; i < Consts.NEXTDAY_PICTURE_SIZE; i++) {
    		if (currentHour < 9 || (currentHour == 9 && currentMinute < 30)) {
    			mDate = CommonUtil.getDateFromCurrent(-i - 1);
    	    } else {
    	    	mDate = CommonUtil.getDateFromCurrent(-i);
    	    }
    		
    		String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + ".jpg";
    		String previewPath = Consts.NEXTDAY_WALLPAPER_SAVED + mDate + "_comment.jpg";
    		String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + mDate + ".jpg";
    		
    		resultFilePath = originalPath.replace(Consts.NEXTDAY_WALLPAPER_SAVED, Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator);
    		
    		if (FileHelper.fileIsExist(originalPath) && FileHelper.fileIsExist(initPath)) {
				if (count != 3) {
					resultFileList.add(count, resultFilePath);
//					resultFileList.add(count, originalPath);
					count = count + 1;
				}
    			
			} else if (FileHelper.fileIsExist(originalPath) && !FileHelper.fileIsExist(initPath)) {
				if (count != 3) {
//					CommonUtil.copyFile(originalPath, initPath);
					resultFileList.add(count, resultFilePath);
//					resultFileList.add(count, originalPath);
					count = count + 1;
				}
				
			} else if (!FileHelper.fileIsExist(originalPath) && FileHelper.fileIsExist(initPath)) {
				if (count != 3) {
					CommonUtil.copyFile(initPath, originalPath);
					resultFileList.add(count, resultFilePath);
//					resultFileList.add(count, originalPath);
					count = count + 1;
				}
				
			} else {
				continue;
			}
    		
    		if (!FileHelper.fileIsExist(previewPath)) {
    			NextDayDbControl mNextDayDbControl = new NextDayDbControl(mContext);
    			NextDayPictureInfo pictureInfoDb = mNextDayDbControl.queryPictureInfoByName(mDate);
    			if (pictureInfoDb != null && pictureInfoDb.getPictureName() != null) {
    				BitmapFactory.Options mOptions = new BitmapFactory.Options();
//    				mOptions.inJustDecodeBounds = true;
    				mOptions.inTempStorage = new byte[16 * 1024];
    				
    				Bitmap originalBitmap = BitmapFactory.decodeFile(originalPath, mOptions);
    				if (originalBitmap == null) break;
    				
    				Bitmap targetBitmap = null;
    				targetBitmap = WallpaperConfigUtil.nextdayPictureCompose(mContext, Consts.NEXTDAY_OPERATION_SET, 
    						pictureInfoDb, originalBitmap, targetBitmap);
    				
    				boolean result = FileHelper.writeImage(targetBitmap, previewPath, 100);
    				Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------------updateInitNextDayAndDb--------writeImage preview = "+result);
    				
    				if (originalBitmap != null) {
						originalBitmap.recycle();
					}
    				if (targetBitmap != null) {
						targetBitmap.recycle();
					}
    			}
    			mNextDayDbControl.close();
			}
    		Log.d("Wallpaper_DEBUG", "WallpaperConfigUtil---------------updateInitNextDayAndDb--------count = "+count);
    		
    		if (count == 3) break;
    		
		}
    	
    	if (count == 3) {
    		for (int j = 0; j < count; j++) {
    			if (!FileHelper.fileIsExist(resultFileList.get(j))) {
    				String tempOriginalPath = resultFileList.get(j).replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator, 
    																										Consts.NEXTDAY_WALLPAPER_SAVED);
    				CommonUtil.copyFile(tempOriginalPath, resultFileList.get(j));
    			}
    		}
		}
    	
		updateDBForNextDayGroup(mContext, resultFileList);
    	
	}
	
}
