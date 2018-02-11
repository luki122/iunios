package com.aurora.plugin;

import com.aurora.util.DeviceProperties;
import com.aurora.launcher.ALog;
import com.aurora.launcher.Alarm;
import com.aurora.launcher.FastBitmapDrawable;
import com.aurora.launcher.IconCache;
import com.aurora.launcher.LauncherApplication;
import com.aurora.launcher.LogWriter;
import com.aurora.launcher.R;
import com.aurora.launcher.ShortcutInfo;
import com.aurora.launcher.Utilities;
import com.aurora.launcher.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.aurora.utils.Utils2Icon;

public class WeatherMovingIcon extends DynIconPlg {
	
	private static String Weather_Package = "com.aurora.weatherforecast";
	private String REQUEST = "com.aurora.weatherfoecast.request.updateweather";
	private String RESPONSE = "com.aurora.weatherfoecast.updateweather";
	
	static Bitmap mBmpBg =null;
	final static int weatherType[] = new int[11];
	protected final static Canvas mCanvas = new Canvas();
	static String[][] ssStrings;
	private static int  currentTemperature;
	private static String currentWeatherType;
	private int  showCurrentTemperature;
	private String showCurrentWeatherType;
	static final String TAG = "WeatherMovingIcon";
	
	//保存图片(临时缓存)
	private Intent intent;
	
	/*private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String evt = intent.getAction();
			if(evt.equals(RESPONSE)) {
				int temperature = intent.getIntExtra("curTemp", 10);
				String weatherType = intent.getStringExtra("weatherType");
				currentTemperature=temperature;
				currentWeatherType=weatherType;
				//Log.v("iht-w", "________________onReceive________--"+currentTemperature+"----"+currentWeatherType);
			}		
			
		}
	};*/
	
	public static class WeatherBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//Log.v("iht-wea", "=>WeatherBroadcastReceiver"+System.currentTimeMillis()/1000);
			
			String action = intent.getAction();
			String pkg = intent.getDataString();

			Log.v("iht-wea", "action:"+action+",   pkg:"+pkg);
			
			if(pkg != null && pkg.contains(Weather_Package) && action.equals(Intent.ACTION_PACKAGE_DATA_CLEARED) ){
				currentTemperature = 0;
				currentWeatherType = "cleared";
			}else{
				currentTemperature = intent.getIntExtra("curTemp", 10);
				currentWeatherType = intent.getStringExtra("weatherType");
			}
			LauncherApplication.logVulcan.print("WeatherBroadcastReceiver.currentTemperature:::"+currentTemperature);
			Log.v("iht-wea","WeatherBroadcastReceiver.currentTemperature:::"+currentTemperature);
		}
	}
	
	public int getIndexByWeatherType(String weatherType) {
		for (int i = 0; i < ssStrings.length; i++) {
			for (int j = 0; j < ssStrings[i].length; j++) {
				if (weatherType.equals(ssStrings[i][j])) {
					return i;
				}
			}
		}
		return 0;
	}
	
	public WeatherMovingIcon(View v, Intent intent) {
		super(v);
		this.intent = intent;
		
		Intent intnt = new Intent(REQUEST);
		mContext.sendBroadcast(intnt);
		PendingIntent pintent = PendingIntent.getBroadcast(mContext, 0, intnt, 0);
		AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60*60, pintent);
	}
	
	@Override
	public Drawable getCurDrawContent() {
		if(currentWeatherType==null){		
			return new BitmapDrawable(mContext.getResources(), 
					((LauncherApplication)mContext.getApplicationContext()).mIconCache.getIcon(intent));
		}
		if(currentWeatherType.equals("cleared")){
			return Utils2Icon.getInstance(mContext).getIconDrawable(
					mContext.getPackageManager().resolveActivity(intent, 0).activityInfo,Utils2Icon.OUTER_SHADOW);
		}
		if(mCanvas == null){
			return null;
		}
	    int tmp=getIndexByWeatherType(currentWeatherType);
	    return createBaseDrawable(mContext,mCanvas,tmp,currentTemperature);
	}

	@Override
	public boolean refreshDynIcon() {
		Drawable d = getCurDrawContent();
		if (d != null) {
			((TextView)hostView).setCompoundDrawablesWithIntrinsicBounds(null, d, null,null);
			((LauncherApplication)mContext.getApplicationContext()).mIconCache.updateIcon(intent, Utils.drawable2bitmap(d));
			hostView.invalidate();
			return true;
		}
		return false;
	}

	
	@Override
	public int getRedrawMultipleFreq() {
		// TODO Auto-generated method stub
		return 1000*60*30;
	}
	
	@Override
	public boolean isDirty() {
		//Log.i("xie", "__________________isDirty_________________________");	
		// TODO Auto-generated method stub
		if(currentWeatherType==null){
			return false;
		}		
		if(currentTemperature != showCurrentTemperature) {
			return true;
		}
		
		if(!currentWeatherType.equals(showCurrentWeatherType)) {
			return true;
		}
		return false;
	}

	@Override
	public void resetDirty() {
		// TODO Auto-generated method stub
		showCurrentTemperature = currentTemperature;
		showCurrentWeatherType = currentWeatherType;
		
	}

	@Override
	public void dump() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * load all the resource and adjust it for special device
	 * @param context
	 */
	public static void loadResOfDevice(Context context) {
		loadRes(context);
		mContext = context;
		return;
	}
	static void loadRes(Context context) {
		weatherType[0]=R.drawable.qing0;
		weatherType[1]=R.drawable.duoyun1;
		weatherType[2]=R.drawable.yin2;
		weatherType[3]=R.drawable.mai3;
		weatherType[4]=R.drawable.xiaoyu4;
		weatherType[5]=R.drawable.dayu5;
		weatherType[6]=R.drawable.leizhenyu6;
		weatherType[7]=R.drawable.xiaoxue7;
		weatherType[8]=R.drawable.daxue8;
		weatherType[9]=R.drawable.shachenbao9;
		weatherType[10]=R.drawable.yangchen10;
		getResTypeNum(context);
	}  
	
	
	private  Drawable createBaseDrawable(Context context,Canvas mCanvas, int num,int wendu) {	
		Resources res = context.getResources();		
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(weatherType[num]);
		mBmpBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		mBmpBg = Utils.getRoundedBitmap(mBmpBg, context);
		mBmpBg = Utils.getShadowBitmap1(mBmpBg, shadowDrawable);
		if(DeviceProperties.isNeedScale()) {
			adjustResForDevice(context);
		}
		
		Bitmap bmpCanvas = mBmpBg.copy(Bitmap.Config.ARGB_8888,true);
		drawBg(mCanvas, bmpCanvas);
		drawHint(mCanvas,wendu);
		
		return new FastBitmapDrawable(bmpCanvas);
	}
	
   private  Drawable createDefullDrawable(Context context,Canvas mCanvas) {		
		Resources res = context.getResources();		
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.weather_defull); //默认图
		mBmpBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		mBmpBg = Utils.getRoundedBitmap(mBmpBg, context);
		mBmpBg = Utils.getShadowBitmap1(mBmpBg, shadowDrawable);
		if(DeviceProperties.isNeedScale()) {
			adjustResForDevice(context);
		}
		
		Bitmap bmpCanvas = mBmpBg.copy(Bitmap.Config.ARGB_8888,true);
		drawBg(mCanvas, bmpCanvas);
				
		return new FastBitmapDrawable(bmpCanvas);
	}
	/**
	 * draw hint message
	 * @param cv
	 * @return
	 */
	private boolean drawHint(Canvas cv,int num) {
	   drawTemperatureText(cv, String.valueOf(num));
		return true;
	}
	
	private boolean drawTemperatureText(Canvas cv, String temperature) {
		Paint paintText = new Paint();
		float textLength = 0;
		float textLeft,textTop,textTop2;
		FontMetrics fm = null;
		FontMetrics fm2 = null;
		
		//M: shigq for U5, the style of temprature icon has been changed
//		final float temperatureDigitTextSizeRate = 88.0f/184.0f;
		final float temperatureDigitTextSizeRate = 60.0f/184.0f;
		float digitTextSizePx = temperatureDigitTextSizeRate * DynIconPlg.mBgWidth;
		//Log.i("xie", "___________________________________________"+temperatureDigitTextSizeRate+"¡ã"+DynIconPlg.mBgWidth+"----"+digitTextSizePx);	
		paintText.setColor(Color.WHITE);
		paintText.setTextSize((int)digitTextSizePx);
		paintText.setAntiAlias(true);
		try {
			Typeface tf = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
			paintText.setTypeface(tf);
		} catch (Exception e) {
			ALog.i(TAG,"Typeface:"+e);
		}
		
		//M: shigq for U5, the style of temprature icon has been changed
		//draw temprature
		textLength = paintText.measureText(temperature);
		fm = paintText.getFontMetrics();
		
		final float temperatureTextOffsetYRate = 65.0f/184.0f;
		final float temperatureTextHeightRate = 48.0f/184.0f;
		int	mTemperatureDigitOffsetY = Math.round(temperatureTextOffsetYRate * DynIconPlg.mBgHeight);
		int	mTemperatureDigitHeight = Math.round(temperatureTextHeightRate * DynIconPlg.mBgHeight);
		textTop = (mTemperatureDigitHeight - fm.bottom + fm.top) / 2 - fm.top + mTemperatureDigitOffsetY;
		
		textLeft = 20f;
		textTop = textTop + 26f;
		cv.drawText(temperature, textLeft, textTop, paintText);
		
		Paint paintText2 = new Paint();
		String symbol="°";
//		final float textSizeRate = 67.52f/184.0f;
		final float textSizeRate = 50.64f/184.0f;
		float textSize = textSizeRate * DynIconPlg.mBgWidth;
		paintText2.setColor(Color.WHITE);
		paintText2.setTextSize((int)textSize);
		paintText2.setAntiAlias(true);
		
		fm2 = paintText2.getFontMetrics();
		final float temperatureTextOffsetYRate2 = 65.0f/184.0f;		
		final float temperatureTextHeightRate2 = 48.0f/184.0f;	 
		int	mTemperatureDigitOffsetY2 = Math.round(temperatureTextOffsetYRate2 * DynIconPlg.mBgHeight);	   
		int	mTraffDigitHeight2 = Math.round(temperatureTextHeightRate2 * DynIconPlg.mBgHeight);	
		textTop2 = (mTraffDigitHeight2 - fm2.bottom + fm2.top) / 2 - fm2.top + mTemperatureDigitOffsetY2;
		
		cv.drawText(symbol, textLeft+textLength, textTop2 + 26, paintText2);
		
		//draw digits
		/*textLength = paintText.measureText(temperature);
		fm = paintText.getFontMetrics();
		final float temperatureTextOffsetXRate = 35.0f/184.0f;
		final float temperatureTextOffsetYRate = 65.0f/184.0f;
		final float temperatureTextWidthRate = 114.0f/184.0f;
		final float temperatureTextHeightRate = 48.0f/184.0f;
	   int	mTemperatureDigitOffsetX = Math.round(temperatureTextOffsetXRate * DynIconPlg.mBgWidth);
	   int	mTemperatureDigitOffsetY = Math.round(temperatureTextOffsetYRate * DynIconPlg.mBgHeight);
	   int	mTemperatureDigitWidth = Math.round(temperatureTextWidthRate * DynIconPlg.mBgWidth);
	   int	mTemperatureDigitHeight = Math.round(temperatureTextHeightRate * DynIconPlg.mBgHeight);
		textLeft = (mTemperatureDigitWidth - textLength ) / 2 + mTemperatureDigitOffsetX-adjustWeatherContentPos(temperature);
		textTop = (mTemperatureDigitHeight - fm.bottom + fm.top) / 2 - fm.top + mTemperatureDigitOffsetY;
		
		textLeft += mBgOffsetXToShadow;
		textTop += mBgOffsetYToShadow;
		cv.drawText(temperature, textLeft, textTop, paintText);
		
		Paint paintText2 = new Paint();
		String symbol="°";
		final float textSizeRate = 67.52f/184.0f;
		float textSize = textSizeRate * DynIconPlg.mBgWidth;
		paintText2.setColor(Color.WHITE);
		paintText2.setTextSize((int)textSize);
		paintText2.setAntiAlias(true);
		
		
		fm2 = paintText2.getFontMetrics();
		final float temperatureTextOffsetYRate2 = 65.0f/184.0f;		
		final float temperatureTextHeightRate2 = 48.0f/184.0f;	 
	   int	mTemperatureDigitOffsetY2 = Math.round(temperatureTextOffsetYRate2 * DynIconPlg.mBgHeight);	   
	   int	mTraffDigitHeight2 = Math.round(temperatureTextHeightRate2 * DynIconPlg.mBgHeight);	
		textTop2 = (mTraffDigitHeight2 - fm2.bottom + fm2.top) / 2 - fm2.top + mTemperatureDigitOffsetY2;
		
		//Log.i("xie", "___________________________________________"+textLength);	
		cv.drawText(symbol, textLeft+textLength, textTop2, paintText2);*/
		return false;
	}
	/**
	 * 
	 * @param cv Canvas to draw on.
	 * @param bmpCanvas bitmap as a canvas
	 */
	public void drawBg(Canvas cv, Bitmap bmpCanvas) {
		cv.setBitmap(bmpCanvas);		
		return;
	}
	
	static void getResTypeNum(Context context) {
		String[] str = context.getResources().getStringArray(
				R.array.weather_icon);
		ssStrings = new String[str.length][8];

		for (int i = 0; i < str.length; i++) {
			if (str[i].contains(",")) {
				String[] bbStrings = str[i].split(",");
				for (int j = 0; j < bbStrings.length; j++) {
					ssStrings[i][j] = bbStrings[j];
				}
			} else {
				ssStrings[i][0] = str[i];

			}

		}
	}
	/**
	 * adjust size of all bitmap for special device
	 * @param context
	 */
	static void adjustResForDevice(Context context) {		
			mBmpBg = adjustBitmapForDevice(mBmpBg,context);	
		return;		
	}
	/**
	 * adjust size of one bitmap for special device
	 * @param bmp
	 * @param context
	 * @return return new bitmap if old bitmap is valid. return null if old bitmap is invalid.
	 */
	public static Bitmap adjustBitmapForDevice(Bitmap old, Context context) {
		if(old != null) {
			Bitmap newBmp = Utilities.zoomBitmap(old, context);
			return newBmp;
		}
		return null;
	}
	
	
	/**adjust the position of weather content*/
	private int adjustWeatherContentPos(String content){
		int ContentOffset = 0;
		//example : -16
		if(content.length()>2 && content.contains("-")){
			ContentOffset =Utilities.getIntegerValueFromResourcesDimens(mContext.getResources(),R.dimen.weather_content_left_offset_1);
		}else if(content.length() == 2 && content.indexOf("1")==0){
			ContentOffset =Utilities.getIntegerValueFromResourcesDimens(mContext.getResources(),R.dimen.weather_content_left_offset_2);
		}
		Log.e("linp","~~~~~~~~~~~~~~~~ContentOffset="+ContentOffset);
		return ContentOffset;
	}
	

}
