/**
 * 
 */
package com.aurora.plugin;

import com.aurora.calender.CalenderAdapter;
import com.aurora.launcher.CellLayout;
import com.aurora.launcher.FastBitmapDrawable;
import com.aurora.launcher.LauncherApplication;
import com.aurora.launcher.R;
import com.aurora.launcher.Utilities;
import com.aurora.launcher.Utils;
import com.aurora.util.DeviceProperties;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Vulcan added these code in 上午10:08:30
 *
 */
public class CalendarIcon extends DynIconPlg {
	
	private static Context mContext;
	protected int mLastShownDay;
	protected int mDayNow;
	final static float mCalendarTextRate = 99.0f/208.0f;//99.0f/208.0f
	final static float mDigitXOffsetRate = 5.0f/208.0f;
	
	//final static int dayResIdList[] = new int[31];//everyday in month
	//static Bitmap mBmpFore = null;
	
	static int nowIndex=0;
	static int prIndex=0;

	static int nowDay=0;
	static int beforDay=0;
	final static Rect mDigitDayPos = new Rect();
	static View v;
	public CalendarIcon(View v) {
		super(v);
		this.v=v;
		// TODO Auto-generated constructor stub
		mIsDynamic = true;
	}

	//public static ArrayList<Drawable> drawDayList = new ArrayList<Drawable>();
	public static ArrayList<Drawable> drawDayListForAdapter = new ArrayList<Drawable>();
	
	public Drawable getCurDrawContent() {
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);
		
		/*if(drawDayList.size() > day - 1) {
			Drawable d = drawDayList.get(day - 1); //传当天日期，因为数组是0游标开始，故（day-1）
			return d;
		}*/

		Drawable drawable = createDrawableDay3(mContext, day);
		if(drawable != null){
			return drawable;
		}
		return null;
	}		
	public boolean refreshDynIcon() {
		Drawable d = getCurDrawContent();
		if (d != null) {
			((TextView)hostView).setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
			hostView.invalidate();
			return true;
		}
		LauncherApplication.logVulcan.print("refreshDynIcon: failure to refresh calendarIcon");
		return false;

	}	
	public static void refreshDyn() {
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH); // iht 当天的日期

		Drawable drawable = createDrawableDay3(mContext, day);
		if(drawable != null){
			((TextView)v).setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			v.invalidate();
		}
	}
	
	@Override
	public int getRedrawMultipleFreq() {
		return 20;
	}
	
	public boolean isDirty() {
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mDayNow = mCalendar.get(Calendar.DAY_OF_MONTH);
		
		//current day is equal to the saved day, so do not refresh
		if(mDayNow == mLastShownDay) {
			return false;
		}
		
		return true;
	}

	public static boolean isPlay() {
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		nowDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		
		if (nowDay == beforDay) {
			return false;
		}

		return true;
	}
	public static void setDay() {
		beforDay = nowDay;
	}
	@Override
	public void resetDirty() {

		mLastShownDay = mDayNow;

		return;
	}
	
	@Override
	public void dump() {
		LauncherApplication.logVulcan.print(">>>>>>>>>>>>>>>>>>>>>CalendarIcon>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		LauncherApplication.logVulcan.print("mIsDynamic = " + mIsDynamic + ", mDayNow = " + mDayNow + ", lastShowDay = " + mLastShownDay);
		LauncherApplication.logVulcan.print("<<<<<<<<<<<<<<<<<<<<<<CalendarIcon<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 
	 * @param dig, number from 1 to 31
	 * @return, must be not null.
	 */
	private static int getResIDByDig(int dig) {
		switch (dig) {
			case 1:return R.drawable.launcher_cal_day_1;//break;
			case 2:return R.drawable.launcher_cal_day_2;//break;
			case 3:return R.drawable.launcher_cal_day_3;//break;
			case 4:return R.drawable.launcher_cal_day_4;//break;
			case 5:return R.drawable.launcher_cal_day_5;//break;
			case 6:return R.drawable.launcher_cal_day_6;//break;
			case 7:return R.drawable.launcher_cal_day_7;//break;
			case 8:return R.drawable.launcher_cal_day_8;//break;
			case 9:return R.drawable.launcher_cal_day_9;//break;
			case 10:return R.drawable.launcher_cal_day_10;//break;
			case 11:return R.drawable.launcher_cal_day_11;//break;
			case 12:return R.drawable.launcher_cal_day_12;//break;
			case 13:return R.drawable.launcher_cal_day_13;//break;
			case 14:return R.drawable.launcher_cal_day_14;//break;
			case 15:return R.drawable.launcher_cal_day_15;//break;
			case 16:return R.drawable.launcher_cal_day_16;//break;
			case 17:return R.drawable.launcher_cal_day_17;//break;
			case 18:return R.drawable.launcher_cal_day_18;//break;
			case 19:return R.drawable.launcher_cal_day_19;//break;
			case 20:return R.drawable.launcher_cal_day_20;//break;
			case 21:return R.drawable.launcher_cal_day_21;//break;
			case 22:return R.drawable.launcher_cal_day_22;//break;
			case 23:return R.drawable.launcher_cal_day_23;//break;
			case 24:return R.drawable.launcher_cal_day_24;//break;
			case 25:return R.drawable.launcher_cal_day_25;//break;
			case 26:return R.drawable.launcher_cal_day_26;//break;
			case 27:return R.drawable.launcher_cal_day_27;//break;
			case 28:return R.drawable.launcher_cal_day_28;//break;
			case 29:return R.drawable.launcher_cal_day_29;//break;
			case 30:return R.drawable.launcher_cal_day_30;//break;
			case 31:return R.drawable.launcher_cal_day_31;//break;
			default:return R.drawable.launcher_cal_day_1;//break;
		}
	}
	
	/**
	 * 
	 * @param context
	 * @return, must be not null
	 */
	private static Bitmap createBitmapBg(Context context) {
		Resources res = context.getResources();

		//get background of day with shadow and rounded corner
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.aurora_launcher_dyn_calendar_bg);
		Bitmap bmpDayBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		bmpDayBg = Utils.getRoundedBitmap(bmpDayBg, context);
		bmpDayBg = Utils.getShadowBitmap1(bmpDayBg, shadowDrawable);
		return bmpDayBg;
	}
	
	/**
	 * 
	 * @param context
	 * @param digit,from 0 to 9.
	 * @return Bitmap value must not be null.
	 */
	private static Bitmap createBitmapDigit(Context context, int digit) {
		Resources res = context.getResources();
		BitmapDrawable bmpdDigit = (BitmapDrawable)res.getDrawable(getResIDByDig(digit));
		Bitmap bmpDigit = bmpdDigit.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		return bmpDigit;
	}
	
	
	/**
	 * create picture of specified day
	 * @param context
	 * @param day
	 * @return
	 */
	private static Drawable createDrawableDay3(Context context, int day) {
		Resources res = context.getResources();
		
		//get background of day with shadow and rounded corner
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.aurora_launcher_dyn_calendar_bg);
		Bitmap bmpDayBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		
		//get bitmap of the digit(from 1~31)
		Bitmap bmpDay = createBitmapDigit(context,day);
		
		//get bitmap of the foreground
		Bitmap bmpFore = createForeBitmap(context);
		
		//measure
		measure();

		Canvas canvas = new Canvas(bmpDayBg);
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		canvas.drawBitmap(bmpDay, mDigitDayPos.left , mDigitDayPos.top, paint);

		canvas.drawBitmap(bmpFore, 0,0, paint);

		//add shadow
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		bmpDayBg = Utils.getRoundedBitmap(bmpDayBg, context);
		bmpDayBg = Utils.getShadowBitmap1(bmpDayBg, shadowDrawable);
		
		//resize for device
		//vulcan added it in 2014-7-1
		if(DeviceProperties.isNeedScale()) {
			bmpDayBg = Utilities.zoomBitmap(bmpDayBg, context);
		}
		
		canvas.setBitmap(null);
		
		return new FastBitmapDrawable(bmpDayBg);
	}
	
	/**
	 * 
	 * @param context
	 * @param day. number from 1 to 31 as a day in month.
	 * @return Drawable may be null if day is not between 1 and 31 or other error happens.
	 */
	
	private static Drawable createDrawableDay2(Context context, int day) {
		Bitmap bmpBg = null;
		int digitRight = 0;
		int digitLeft = 0;
		Bitmap bmpDigitRight = null;
		Bitmap bmpDigitLeft = null;
		int digitsWidth = 0;
		int digitsHeight = 0;
		
	
		
		//1.check input
		//2.create background.
		//3.break a day number into digits and tens.
		//4.create the left digit
		//5.create the right digit
		//6.compute width total.
		if(day < 1 || day > 31) {
			Log.e("vulcan-calendar","createDrawableDay2:day error: " + day);
			return null;
		}
		
		bmpBg = createBitmapBg(context);

		digitRight = day % 10;
		digitLeft = day / 10;
		
		//the left digit must be positive(>0).
		if(digitLeft > 0
			&& digitLeft < 10) {
			bmpDigitLeft = createBitmapDigit(context,digitLeft);
		}
		
		if(digitRight >= 0
				&& digitRight < 10) {
			bmpDigitRight = createBitmapDigit(context,digitRight);
		}
		
		if(bmpDigitRight == null) {
			Log.e("vulcan-calendar","createDrawableDay2:bmpDigitRight is null, day = " + day);
			return null;
		}
		
		if(bmpDigitLeft == null) {
			digitsWidth = bmpDigitRight.getWidth();
			Log.d("vulcan-cal", "createDrawableDay2:digitsWidth = " + digitsWidth);
		}
		else {
			digitsWidth = bmpDigitRight.getWidth() + bmpDigitLeft.getWidth();
			Log.d("vulcan-cal", "createDrawableDay2:digitsWidth = " + digitsWidth);
		}
		
		digitsHeight = bmpDigitRight.getHeight();
		Log.d("vulcan-cal", "createDrawableDay2:digitsHeight = " + digitsHeight);

		Canvas canvas = new Canvas(bmpBg);
		
		int left = ((bmpBg.getWidth() - digitsWidth) >> 1);
		int top = ((bmpBg.getHeight() - digitsHeight) >> 1);
		
		if(day == 1) {
			int offsetX = 0;
			offsetX = Math.round(mDigitXOffsetRate * ((float)bmpBg.getWidth()));
			left += offsetX;
			Log.d("vulcan-cal", "createDrawableDay2:offsetX = " + offsetX);
		}
		
		Log.d("vulcan-cal", "createDrawableDay2:left = " + left + ", top = " + top);

		if (bmpDigitLeft != null) {
			canvas.drawBitmap(bmpDigitLeft, left, top, null);
			left += bmpDigitLeft.getWidth();
			canvas.drawBitmap(bmpDigitRight, left, top, null);
		}
		else {
			canvas.drawBitmap(bmpDigitRight, left, top, null);
		}
		
		//vulcan added it in 2014-7-1
		if(DeviceProperties.isNeedScale()) {
			bmpBg = Utilities.zoomBitmap(bmpBg, context);
		}
		
	
		
		if(bmpBg == null) {
			return null;
		}
		return new FastBitmapDrawable(bmpBg);
	}
	
	private static Drawable createDrawableDay(Context context,int day) {
		Resources res = context.getResources();
		
		//get background of day with shadow and rounded corner
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.aurora_launcher_dyn_calendar_bg);
		Bitmap bmpDayBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		bmpDayBg = Utils.getRoundedBitmap(bmpDayBg, context);
		bmpDayBg = Utils.getShadowBitmap1(bmpDayBg, shadowDrawable);
		
		//Log.d("vulcan-iconlist","createDrawableDay: (" + bmpDayBg.getWidth() + ", " + bmpDayBg.getHeight() + ")");

		Canvas canvas = new Canvas(bmpDayBg);
		Paint paintText = new Paint();
		String strText = Integer.toString(day);
		float textLength = 0;
		float textLeft,textTop;
		FontMetrics fm = null;
		
		paintText.setColor(Color.rgb(227, 100, 8));
		paintText.setTextSize(mCalendarTextRate * bmpDayBg.getHeight());
		paintText.setAntiAlias(true);
		paintText.setTypeface(Typeface.SANS_SERIF);
		
		//get length of text
		textLength = paintText.measureText(strText);
		fm = paintText.getFontMetrics();
		textLeft = (bmpDayBg.getWidth() - textLength ) / 2;
		textTop = (bmpDayBg.getHeight() - fm.bottom + fm.top) / 2 - fm.top;
		
		canvas.drawText(strText, textLeft, textTop, paintText);
		return new FastBitmapDrawable(bmpDayBg);
	}
	
	public static void measure() {
		//measure the water
		final float digitDayOffsetXRate = 33.0f/184.0f;
		final float digitDayOffsetYRate = 33.0f/184.0f;
		final float digitDayWidthRate = 118.0f/184.0f;
		final float digitDayHeightRate = 118.0f/184.0f;
		
		mDigitDayPos.left = Math.round(digitDayOffsetXRate * DynIconPlg.mBgWidth);
		mDigitDayPos.top = Math.round(digitDayOffsetYRate * DynIconPlg.mBgHeight);
		mDigitDayPos.right = mDigitDayPos.left + Math.round(digitDayWidthRate * DynIconPlg.mBgWidth);
		mDigitDayPos.bottom = mDigitDayPos.top + Math.round(digitDayHeightRate * DynIconPlg.mBgHeight);
		return;
	}
	
	/**
	 * create picture of foreground
	 * @param context
	 */
	private static Bitmap createForeBitmap(Context context) {
		Resources res = context.getResources();
		BitmapDrawable bmpdFore = (BitmapDrawable) res.getDrawable(R.drawable.launcher_cal_fore);
		Bitmap bmpFore = bmpdFore.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		return bmpFore;
	}

	//Vulcan created this method in 2014-6-10
	//this method will be called only once to load bitmap resource of calender.
	public static void loadRes(Context context) {
		
		mContext = context;
		
		Log.v("iht-cal", "CalendarIcon.loadRes():::");
		/*
		dayResIdList[0] = R.drawable.launcher_cal_day_1;
		dayResIdList[1] = R.drawable.launcher_cal_day_2;
		dayResIdList[2] = R.drawable.launcher_cal_day_3;
		dayResIdList[3] = R.drawable.launcher_cal_day_4;
		dayResIdList[4] = R.drawable.launcher_cal_day_5;
		dayResIdList[5] = R.drawable.launcher_cal_day_6;
		dayResIdList[6] = R.drawable.launcher_cal_day_7;
		dayResIdList[7] = R.drawable.launcher_cal_day_8;
		dayResIdList[8] = R.drawable.launcher_cal_day_9;
		dayResIdList[9] = R.drawable.launcher_cal_day_10;
		dayResIdList[10] = R.drawable.launcher_cal_day_11;
		dayResIdList[11] = R.drawable.launcher_cal_day_12;
		dayResIdList[12] = R.drawable.launcher_cal_day_13;
		dayResIdList[13] = R.drawable.launcher_cal_day_14;
		dayResIdList[14] = R.drawable.launcher_cal_day_15;
		dayResIdList[15] = R.drawable.launcher_cal_day_16;
		dayResIdList[16] = R.drawable.launcher_cal_day_17;
		dayResIdList[17] = R.drawable.launcher_cal_day_18;
		dayResIdList[18] = R.drawable.launcher_cal_day_19;
		dayResIdList[19] = R.drawable.launcher_cal_day_20;
		dayResIdList[20] = R.drawable.launcher_cal_day_21;
		dayResIdList[21] = R.drawable.launcher_cal_day_22;
		dayResIdList[22] = R.drawable.launcher_cal_day_23;
		dayResIdList[23] = R.drawable.launcher_cal_day_24;
		dayResIdList[24] = R.drawable.launcher_cal_day_25;
		dayResIdList[25] = R.drawable.launcher_cal_day_26;
		dayResIdList[26] = R.drawable.launcher_cal_day_27;
		dayResIdList[27] = R.drawable.launcher_cal_day_28;
		dayResIdList[28] = R.drawable.launcher_cal_day_29;
		dayResIdList[29] = R.drawable.launcher_cal_day_30;
		dayResIdList[30] = R.drawable.launcher_cal_day_31;
		*/
		
		//create drawable for every day of month
		/*Drawable d = null;
		for(int day = 1;day <= 31; day ++) {
			// d = createDrawableDay(context,day);
			d = createDrawableDay3(context, day);
			if (d != null) {
				drawDayList.add(d);
			}
		}*/
		
		drawDayListForAdapter.add(createDrawableDay3(context, yestoday)); //初始化？？？
		drawDayListForAdapter.add(createDrawableDay3(context, nowday));
		nowIndex=nowday;
		beforDay=nowday;
		return;
	}
	
	//日历翻页动画
	public static void refreshDay2(Context context, boolean isAllowCalAnimation) {
		// TODO Auto-generated method stub
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);
		prIndex = nowIndex;

		nowIndex = day;
		drawDayListForAdapter.clear();
		drawDayListForAdapter.add(createDrawableDay3(context, nowIndex));
		drawDayListForAdapter.add(createDrawableDay3(context, prIndex));

		if (myflipViewController == null || mylayout == null) {
			return;
		}
		
		//iht 2015-02-03 若处在“语音”、“检索“界面，则无须播放动画
		if(isAllowCalAnimation){
			refreshDyn();
			setDay();
			return;
		}
		
		myflipViewController.setAdapter(new CalenderAdapter(context,drawDayListForAdapter), 1);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				myflipViewController.next();

			}
		}, 200);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				if (mylayout.getParent() != null) {
					CellLayout layout = (CellLayout) mylayout.getParent().getParent();
					layout.removeView(mylayout);
					layout.markCellsAsOccupiedForView(v);
					refreshDyn(); //动画结束之后，显示当天日期；
				}

			}
		}, 800);
		setDay();

	}

}
