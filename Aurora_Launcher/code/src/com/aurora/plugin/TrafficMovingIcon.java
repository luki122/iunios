/**
 * 
 */
package com.aurora.plugin;
import com.aurora.launcher.FastBitmapDrawable;
import com.aurora.launcher.R;
import com.aurora.launcher.Utilities;
//import com.aurora.launcher.Utils;
import com.aurora.util.DeviceProperties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author vulcan
 *
 */
public class TrafficMovingIcon extends DynIconPlg {
	final static int PIC_IDX_VERY_LOW = 0;
	final static int PIC_IDX_NORMAL = 1;
	final static int mPicNum = PIC_IDX_NORMAL + 1;
	final static Bitmap[] mBmpBg = new Bitmap[mPicNum];
	final static Bitmap[] mBmpAnim = new Bitmap[mPicNum];
	final static Bitmap[] mBmpFore = new Bitmap[mPicNum];
	static Bitmap mBmpHintNOPlan = null;
	static Bitmap mBmpHintUsedOut = null;
	final static Rect mAnimDrawDst = new Rect();
	
	final static int TRAFFIC_PERCENT_DEFAULT = 50;
	final static String TRAFFIC_USED_DEFAULT = "0KB";
	

	static int mSavedTrafficPercent = TRAFFIC_PERCENT_DEFAULT;
	static int mShownTrafficPercent = -1;
	static String mSavedTrafficUsed = TRAFFIC_USED_DEFAULT;
	static String mShownTrafficUsed = "-1";
	static boolean mSavedValuePlanIsSet = false;
	static boolean mShownValuePlanIsSet = !mSavedValuePlanIsSet;
	static boolean mSavedValueCreditUsedOut = false;
	static boolean mShownValueCreditUsedOut = !mSavedValueCreditUsedOut;
	static boolean mSavedValueShouldAlarm = false;//indicate if traffic used flow is more than the threshold
	static boolean mShownValueShouldAlarm = !mSavedValueShouldAlarm;
	
	static int mWaterOffsetX = 0;
	static int mWaterOffsetY = 0;
	static int mWaterWidth = 0;
	static int mWaterHeight = 0;
	
	static int mTraffDigitOffsetX = 0;
	static int mTraffDigitOffsetY = 0;
	static int mTraffDigitWidth = 0;
	static int mTraffDigitHeight = 0;
	
	static int mTraffUnitOffsetX = 0;
	static int mTraffUnitOffsetY = 0;
	static int mTraffUnitWidth = 0;
	static int mTraffUnitHeight = 0;
	
	static int mTraffDigitTextSize = 0;
	static int mTraffUnitTextSize = 0;

	static int paddingOffset = 0;
	
	public static class TrafficReceiver extends BroadcastReceiver {

		public TrafficReceiver() {
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String evt = intent.getAction();
			if("com.receive.launcherNetIconUpdate".equals(evt)) {//NetManage inform of percent and used bytes of traffic
				int percent = intent.getIntExtra("percent", 50);
				String usedFlow = intent.getStringExtra("flowNumStr");
				boolean isTrafficPlanSet = intent.getBooleanExtra("isTrafficPlanSet", false);
				boolean isTrafficCreditUsedOut = intent.getBooleanExtra("isTrafficPlanUsedOut", false);
				boolean shouldAlarm = intent.getBooleanExtra("shouldAlarm", false);
				
				Log.d("vulcan-traff",String.format("onReceive: byte:%s,percent:%d,isTrafficPlanSet:%b,isTrafficPlanUsedOut:%b,shouldAlarm:%b",
										usedFlow,percent,
										isTrafficPlanSet,isTrafficCreditUsedOut,shouldAlarm));
				mSavedTrafficPercent = percent;
				mSavedTrafficUsed = (usedFlow != null)?usedFlow:"0KB";
				mSavedValuePlanIsSet = isTrafficPlanSet;
				mSavedValueCreditUsedOut = isTrafficCreditUsedOut;
				mSavedValueShouldAlarm = shouldAlarm;
			}
			
		}
	}
	
	public static void requestTrafficInfo(Context context) {
        // TODO Auto-generated method stub  
        //创建Intent对象  
        Intent intent=new Intent();  
        //设置Intent的Action属性  
        intent.setAction("com.launcher.requestTraffic");
        //发送广播  
        context.sendBroadcast(intent);
        Log.d("vulcan-traff","requestTrafficInfo:sent action is com.launcher.requestTraffic, intent = " + intent);
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
	
	/**
	 * adjust size of all bitmap for special device
	 * @param context
	 */
	static void adjustResForDevice(Context context) {
		for(int ii = 0; ii < mPicNum;ii ++) {
			mBmpBg[ii] = adjustBitmapForDevice(mBmpBg[ii],context);
			mBmpAnim[ii] = adjustBitmapForDevice(mBmpAnim[ii],context);
			mBmpFore[ii] = adjustBitmapForDevice(mBmpFore[ii],context);
		}
		
		mBmpHintNOPlan = adjustBitmapForDevice(mBmpHintNOPlan,context);
		mBmpHintUsedOut = adjustBitmapForDevice(mBmpHintUsedOut,context);
		return;
	}
	
	/**
	 * load all the resource and adjust it for special device
	 * @param context
	 */
	public static void loadResOfDevice(Context context) {
		loadRes(context);
		if(DeviceProperties.isNeedScale()) {
			adjustResForDevice(context);
		}
		return;
	}
	
	/**
	 * fake shadow is used to change size of the bitmap.
	 * @param bm
	 * @param shadow
	 * @return
	 */
	public static Bitmap getShadowBitmap1Fake(Bitmap bm, Drawable shadow) {
		final int SHADOW_LEN = 2;
		// 创建新位图
		Bitmap shadowBitmap = Utilities.drawable2bitmap(shadow);
		Bitmap bg = Bitmap.createBitmap(shadowBitmap.getWidth(),
				shadowBitmap.getHeight(), Config.ARGB_8888);
		// 创建画板
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		
		// 绘制图形
		canvas.save();
		canvas.translate((shadowBitmap.getWidth()-bm.getWidth())/2, (shadowBitmap.getHeight()-bm.getHeight())/2);
		canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
		// 释放资源
		canvas.restore();
		// 绘制背景
		//shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		//shadow.draw(canvas);
		canvas.setBitmap(null);
		return bg;
	}
	
	static void loadRes(Context context) {
		//check if they are valid
		if(mBmpBg.length != mPicNum || mBmpFore.length != mPicNum) {
			return;
		}
		
		Resources res = context.getResources();
		
		//load shadow for common use
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		
		//Log.d("vulcan-size","loadRes: shadowDrawable" + shadowDrawable.getIntrinsicWidth());
		
		//load background for very low traffic
		BitmapDrawable bmpdBg0 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_bg0);
		mBmpBg[PIC_IDX_VERY_LOW] = bmpdBg0.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		/*mBmpBg[PIC_IDX_VERY_LOW] = Utils.getRoundedBitmap(mBmpBg[PIC_IDX_VERY_LOW], context);
		mBmpBg[PIC_IDX_VERY_LOW] = Utils.getShadowBitmap1(mBmpBg[PIC_IDX_VERY_LOW], shadowDrawable);*/

		//load background for normal traffic
		BitmapDrawable bmpdBg1 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_bg1);
		mBmpBg[PIC_IDX_NORMAL] = bmpdBg1.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		/*mBmpBg[PIC_IDX_NORMAL] = Utils.getRoundedBitmap(mBmpBg[PIC_IDX_NORMAL], context);
		mBmpBg[PIC_IDX_NORMAL] = Utils.getShadowBitmap1(mBmpBg[PIC_IDX_NORMAL], shadowDrawable);*/
		
		Log.d("vulcan-size","loadRes: mBmpBg.width = " + mBmpBg[PIC_IDX_NORMAL].getWidth());

		//load hint picture
		BitmapDrawable bmpdHintNOPlan = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_hint_no_plan);
		mBmpHintNOPlan = bmpdHintNOPlan.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		
		//load hint picture
		BitmapDrawable bmpdHintUsedOut = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_hint_used_out);
		mBmpHintUsedOut = bmpdHintUsedOut.getBitmap().copy(Bitmap.Config.ARGB_8888, true);

		//load water picture 1
		BitmapDrawable bmpdWater1 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_water);
		mBmpAnim[PIC_IDX_NORMAL] = bmpdWater1.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		//mBmpAnim[PIC_IDX_NORMAL] = Utils.getRoundedBitmap(mBmpAnim[PIC_IDX_NORMAL], context);
		//mBmpAnim[PIC_IDX_NORMAL] = Utils.getShadowBitmap1(mBmpAnim[PIC_IDX_NORMAL], shadowDrawable);

		//load water picture 0
		BitmapDrawable bmpdWater0 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_water);
		mBmpAnim[PIC_IDX_VERY_LOW] = bmpdWater0.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		//mBmpAnim[PIC_IDX_VERY_LOW] = Utils.getRoundedBitmap(mBmpAnim[PIC_IDX_VERY_LOW], context);
		//mBmpAnim[PIC_IDX_VERY_LOW] = Utils.getShadowBitmap1(mBmpAnim[PIC_IDX_VERY_LOW], shadowDrawable);
		
		//load foreground0
		BitmapDrawable bmpdFore0 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_fore0);
		mBmpFore[PIC_IDX_VERY_LOW] = bmpdFore0.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		//mBmpFore[PIC_IDX_VERY_LOW] = Utils.getRoundedBitmap(mBmpFore[PIC_IDX_VERY_LOW], context);
		mBmpFore[PIC_IDX_VERY_LOW] = getShadowBitmap1Fake(mBmpFore[PIC_IDX_VERY_LOW], shadowDrawable);
		
		//load foreground1
		BitmapDrawable bmpdFore1 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_fore1);
		mBmpFore[PIC_IDX_NORMAL] = bmpdFore1.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		//mBmpFore[PIC_IDX_NORMAL] = Utils.getRoundedBitmap(mBmpFore[PIC_IDX_NORMAL], context);
		mBmpFore[PIC_IDX_NORMAL] = getShadowBitmap1Fake(mBmpFore[PIC_IDX_NORMAL], shadowDrawable);

		return;
	}
	
    public static int sp2px(Context context, float spValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);  
    }
	
	public static void measure(Context context) {
		
		//measure the water
		final float waterOffsetXRate = 35.0f/184.0f;
		final float waterOffsetYRate = 35.0f/184.0f;
		final float waterWidthRate = 114.0f/184.0f;
		final float waterHeightRate = 114.0f/184.0f;
		
		mWaterOffsetX = Math.round(waterOffsetXRate * DynIconPlg.mBgWidth);
		mWaterOffsetY = Math.round(waterOffsetYRate * DynIconPlg.mBgHeight);
		mWaterWidth = Math.round(waterWidthRate * DynIconPlg.mBgWidth);
		mWaterHeight = Math.round(waterWidthRate * DynIconPlg.mBgHeight);
		
		//measure the traffic digit
		final float traffTextOffsetXRate = 35.0f/184.0f;
		final float traffTextOffsetYRate = 65.0f/184.0f;
		final float traffTextWidthRate = 114.0f/184.0f;
		final float traffTextHeightRate = 48.0f/184.0f;

		mTraffDigitOffsetX = Math.round(traffTextOffsetXRate * DynIconPlg.mBgWidth);
		mTraffDigitOffsetY = Math.round(traffTextOffsetYRate * DynIconPlg.mBgHeight);
		mTraffDigitWidth = Math.round(traffTextWidthRate * DynIconPlg.mBgWidth);
		mTraffDigitHeight = Math.round(traffTextHeightRate * DynIconPlg.mBgHeight);
		
		//measure the traffic unit
		final float traffUnitOffsetXRate = 69.0f/184.0f;
		final float traffUnitOffsetYRate = 114.0f/184.0f;
		final float traffUnitWidthRate = 46.0f/184.0f;
		final float traffUnitHeightRate = 25.0f/184.0f;

		mTraffUnitOffsetX = Math.round(traffUnitOffsetXRate * DynIconPlg.mBgWidth);
		mTraffUnitOffsetY = Math.round(traffUnitOffsetYRate * DynIconPlg.mBgHeight);
		mTraffUnitWidth = Math.round(traffUnitWidthRate * DynIconPlg.mBgWidth);
		mTraffUnitHeight = Math.round(traffUnitHeightRate * DynIconPlg.mBgHeight);
		
		
		//change to read form dimes value and put the result to dimes 
		final float traffDigitTextSizeRate = 48.0f/184.0f;
		float digitTextSizePx = traffDigitTextSizeRate * DynIconPlg.mBgWidth;
		
		//mTraffDigitTextSize = (int)digitTextSizePx;
		mTraffDigitTextSize = Utilities.getIntegerValueFromResourcesDimens(context.getResources(), R.dimen.moving_traffic_digit_text_size);
		
		final float traffUnitTextSizeRate = 18.0f/184.0f;
		float unitTextSizePx = traffUnitTextSizeRate * DynIconPlg.mBgWidth;
		//mTraffUnitTextSize = (int)unitTextSizePx;
		mTraffUnitTextSize = Utilities.getIntegerValueFromResourcesDimens(context.getResources(), R.dimen.moving_traffic_unit_text_size);
		
		paddingOffset = Utilities.getIntegerValueFromResourcesDimens(context.getResources(), R.dimen.moving_traffic_unit_padding_top);
		
		Log.d("linp","traffic measure: mTraffDigitTextSize = " + mTraffDigitTextSize);
		Log.d("linp","traffic measure: mTraffUnitTextSize = " + mTraffUnitTextSize);
		
		return;
	}
	
	
	/**
	 * @param v
	 */
	public TrafficMovingIcon(View v) {
		super(v);
	}
	
	/**
	 * 
	 * @param cv Canvas to draw on
	 * @param bmpFore bitmap to be drawn
	 */
	public void drawForeground(Canvas cv, Bitmap bmpFore) {
		Rect dst = new Rect();
		Rect src = new Rect();
		Paint paint = new Paint();
		
		paint.setAntiAlias(true);

		//dst.left = mBgOffsetXToShadow;
		//dst.right = dst.left + mBgWidth;
		//dst.top = mBgOffsetYToShadow;
		//dst.bottom =  dst.top + mBgHeight;

		//src.set(dst);
		//cv.drawBitmap(bmpFore, src, dst, paint);

		cv.drawBitmap(bmpFore, 0, 0, paint);
		
		Log.d("vulcan-size", "drawForeground: bmpFore.width = " + bmpFore.getWidth());
		Log.d("vulcan-size", "drawForeground: cv.width = " + cv.getWidth());
		return;
	}
	
	/**
	 * 
	 * @param percent
	 * @return index of array mBmpBg or mBmpFore, it must be between 0 and nPictures - 1
	 */
	public int trafficPctToPicIdx(int percent) {
		if(percent >= 50) {
			return PIC_IDX_NORMAL;
		}
		return PIC_IDX_VERY_LOW;
	}
	
	/**
	 * make a decision to select one picture
	 * @return return the index of the picture which should be shown.
	 */
	private int getIndexOfBgPic() {
		int picIdx = 0;

		if(!mSavedValueShouldAlarm) {
			picIdx = PIC_IDX_NORMAL;
		}
		else {
			picIdx = PIC_IDX_VERY_LOW;
		}
		return picIdx;
	}
	
	public int trafficPctToOffsetY(int percent, int nBgHeight) {
		float rate = 0;
		
		if(percent > 100 || percent < 0) {
			percent = 0;
		}
		
		percent = 100 - percent;
		
		
		int offsetY = Math.round((percent * nBgHeight) /100.0f);
		return offsetY;
	}
	
	/**
	 * draw hint message of no plan
	 * position of hint is the same as water
	 * @param cv
	 */
	private void drawHintNOPlan(Canvas cv) {
		Rect dst = new Rect();
		Paint paint = new Paint();

		dst.left = mWaterOffsetX;
		dst.right = dst.left + mWaterWidth;
		dst.top = mWaterOffsetY;
		dst.bottom =  mWaterOffsetY + mWaterHeight;

		//modify dst:
		dst.left += mBgOffsetXToShadow;
		dst.right += mBgOffsetXToShadow;
		dst.top += mBgOffsetYToShadow;
		dst.bottom += mBgOffsetYToShadow;
		
		paint.setAntiAlias(true);
		cv.drawBitmap(mBmpHintNOPlan, null, dst, paint);
		return;
	}
	
	/**
	 * draw hint message that traffic credit is used out
	 * @param cv
	 */
	private void drawHintCreditUsedOut(Canvas cv) {
		Rect dst = new Rect();
		Paint paint = new Paint();

		dst.left = mWaterOffsetX;
		dst.right = dst.left + mWaterWidth;
		dst.top = mWaterOffsetY;
		dst.bottom =  mWaterOffsetY + mWaterHeight;

		//modify dst:
		dst.left += mBgOffsetXToShadow;
		dst.right += mBgOffsetXToShadow;
		dst.top += mBgOffsetYToShadow;
		dst.bottom += mBgOffsetYToShadow;
		
		paint.setAntiAlias(true);
		cv.drawBitmap(mBmpHintUsedOut, null, dst, paint);
		return;
	}
	
	
	/**
	 * draw hint message
	 * @param cv
	 * @return
	 */
	private boolean drawHint(Canvas cv) {
		if(!mSavedValuePlanIsSet) {
			drawHintNOPlan(cv);
		}
		else if(mSavedValueCreditUsedOut){
			drawHintCreditUsedOut(cv);
		}
		else {
			drawTrafficText(cv, mSavedTrafficUsed);
		}
		return true;
	}
	
	private boolean drawTrafficText(Canvas cv, String trafficUsed) {
		String digits = null;
		String unit = null;
		Paint paintText = new Paint();
		float textLength = 0;
		float textLeft,textTop;
		FontMetrics fm = null;
		
		if(trafficUsed == null) {
			return false;
		}
		
		if(trafficUsed.length() < 3) {
			return false;
		}
		
		digits = trafficUsed.substring(0, trafficUsed.length() - 2);
		unit = trafficUsed.substring(trafficUsed.length() - 2,trafficUsed.length());
		
		paintText.setColor(Color.WHITE);
		paintText.setTextSize(mTraffDigitTextSize);
		paintText.setAntiAlias(true);
		paintText.setTypeface(Typeface.DEFAULT);
		
		//draw digits
		textLength = paintText.measureText(digits);
		fm = paintText.getFontMetrics();
		textLeft = (mTraffDigitWidth - textLength ) / 2 + mTraffDigitOffsetX;
		textTop = (mTraffDigitHeight - fm.bottom + fm.top) / 2 - fm.top + mTraffDigitOffsetY;
		Log.d("vulcan-traff","drawTrafficText: digit textLength = " + textLength);

		//modify textLeft & textTop before transform of rounded corner and shadow
		textLeft += mBgOffsetXToShadow;
		textTop += mBgOffsetYToShadow;
		cv.drawText(digits, textLeft, textTop, paintText);
		//cv.drawRect(textLeft, textTop, textLeft + textLength, textTop + fm.bottom - fm.top , paintText);
		
		//draw unit
		paintText.setTextSize(mTraffUnitTextSize);
		textLength = paintText.measureText(unit);
		fm = paintText.getFontMetrics();
		textLeft = (mTraffUnitWidth - textLength ) / 2 + mTraffUnitOffsetX;
		textTop = (mTraffUnitHeight - fm.bottom + fm.top) / 2 - fm.top + mTraffUnitOffsetY;
		Log.d("vulcan-traff","drawTrafficText: unit textLength = " + textLength);
		
		//modify textLeft & textTop before transform of rounded corner and shadow
		textLeft += mBgOffsetXToShadow;
		textTop += mBgOffsetYToShadow-paddingOffset;
         Log.e("linp", "###############mBgOffsetYToShadow="+mBgOffsetYToShadow);
		//Log.d("vulcan-traff","drawTrafficText: mTraffDigitTextSize = " + mTraffDigitTextSize);
		//Log.d("vulcan-traff","drawTrafficText: mTraffUnitTextSize = " + mTraffUnitTextSize);

		cv.drawText(unit, textLeft, textTop, paintText);
		return false;
	}
	
	public void drawTrafficText(Canvas cv,long trafficUsed) {
		Log.e("linp", "####################drawTrafficText long");
		Paint paintText = new Paint();
		String strDigits = null;
		String strUnit = null;
		float textLength = 0;
		float textLeft,textTop;
		FontMetrics fm = null;
		
		if(trafficUsed < 0) {//exception
			return;
		}
		else if(trafficUsed < 1024 ) {
			strDigits = Long.toString(trafficUsed);
			strUnit = "B";
		}
		else if(trafficUsed < 1024 * 1024) {
			long digits = Math.round(trafficUsed / (1024.0f));
			strDigits = Long.toString(digits);
			strUnit = "KB";
		}
		else if(trafficUsed < 1024 * 1024 * 1024) {
			long digits = Math.round(trafficUsed / (1024.0f * 1024.0f));
			strDigits = Long.toString(digits);
			strUnit = "MB";
		}
		else if(trafficUsed < 1024 * 1024 * 1024 * 1024) {
			long digits = Math.round(trafficUsed / (1024.0f * 1024.0f * 1024.0f));
			strDigits = Long.toString(digits);
			strUnit = "GB";
		}
		else if(trafficUsed < 1024 * 1024 * 1024 * 1024 * 1024) {
			long digits = Math.round(trafficUsed / (1024.0f * 1024.0f * 1024.0f * 1024.0f));
			strDigits = Long.toString(digits);
			strUnit = "TB";
		}
		else {
			strDigits = "####";
			strUnit = "TB";
		}
		
		paintText.setColor(Color.WHITE);
		paintText.setTextSize(50);
		paintText.setAntiAlias(true);
		paintText.setTypeface(Typeface.DEFAULT);
		
		//draw digits
		textLength = paintText.measureText(strDigits);
		fm = paintText.getFontMetrics();
		textLeft = (mTraffDigitWidth - textLength ) / 2 + mTraffDigitOffsetX;
		textTop = (mTraffDigitHeight - fm.bottom + fm.top) / 2 - fm.top + mTraffDigitOffsetY;
		
		cv.drawText(strDigits, textLeft, textTop, paintText);
		
		//draw unit
		paintText.setTextSize(20);
		textLength = paintText.measureText(strUnit);
		fm = paintText.getFontMetrics();
		textLeft = (mTraffUnitWidth - textLength ) / 2 + mTraffUnitOffsetX;
		textTop = (mTraffUnitHeight - fm.bottom + fm.top) / 2 - fm.top + mTraffUnitOffsetY;

		cv.drawText(strUnit, textLeft, textTop, paintText);
		return;
	}
	
	/**
	 * 
	 * @param cv Canvas to draw on
	 * @param bmpAnim bitmap to be drawn
	 */
	public void drawWater(Canvas cv, Bitmap bmpWater, int offsetX, int offsetY, int alpha) {
		Rect src = new Rect();
		Rect dst = new Rect();

		//int waterWidth = Math.round(mWaterWidthRate * ((float)mBgWidth));
		//int waterHeight = Math.round(mWaterHeightRate * ((float)mBgHeight));
		int offsetXMax = bmpWater.getWidth() - mWaterWidth;
		
		if(offsetXMax > 0) {
			offsetX = offsetX % offsetXMax;
		}
		
		Paint paint = new Paint();
		
		paint.setAlpha(alpha);
		
		/*
		dst.left = mBgOffsetXToShadow + mWaterOffsetX;
		dst.right = dst.left + waterWidth;
		dst.top = mBgOffsetYToShadow + offsetY;
		dst.bottom =  mBgOffsetYToShadow + mWaterOffsetY + waterHeight;
		
		src.left = offsetX;
		src.right = src.left + waterWidth;
		src.top = 0;
		src.bottom = mWaterOffsetY + waterHeight - offsetY;
		cv.drawBitmap(bmpAnim, src, dst, null);
		*/
		dst.left = mWaterOffsetX;
		dst.right = dst.left + mWaterWidth;
		dst.top = mWaterOffsetY + offsetY;
		dst.bottom =  mWaterOffsetY + mWaterHeight;

		//modify dst:
		dst.left += mBgOffsetXToShadow;
		dst.right += mBgOffsetXToShadow;
		dst.top += mBgOffsetYToShadow;
		dst.bottom += mBgOffsetYToShadow;

		src.left = offsetX;
		src.right = src.left + mWaterWidth;
		src.top = 0;
		src.bottom = mWaterHeight - offsetY;
		cv.drawBitmap(bmpWater, src, dst, paint);

		Log.d("vulcan-traff",String.format("src(%d,%d,%d,%d), ", src.left,src.top,src.right,src.bottom)
				 + String.format("dst(%d,%d,%d,%d)", dst.left,dst.top,dst.right,dst.bottom));
		//Log.d("vulcan-traff",String.format("waterWidth = %d, bmpAnim.width=%d", cv.getWidth(), bmpWater.getWidth()));
		//Log.d("vulcan-traff",String.format("mBgWidth=%d,WideWidth=%d", mBgWidth,mBgWidthWithShadow));
		return;
	}
	
	/**
	 * 
	 * @param cv Canvas to draw on.
	 * @param bmpCanvas bitmap as a canvas
	 */
	public void drawBg(Canvas cv, Bitmap bmpCanvas) {

		cv.setBitmap(bmpCanvas);
		//mWidth = cv.getWidth();
		//mHeight = cv.getHeight();
		// LauncherApplication.logVulcan.print("drawBg:(mWidth,mHeight,mBmpDialW,mBmpDialH) = ("
		// + mWidth + ", " + mHeight + ", " + bmpCanvas.getWidth() + ", " +
		// bmpCanvas.getHeight() + ")");
		return;
	}
	
	/**
	 * get drawable when status is normal
	 * @param cv
	 * @return
	 */
	private Drawable getDrawableNormal(Canvas cv) {
		int iPicIdx = getIndexOfBgPic();
		//int iPicIdx = trafficPctToPicIdx(mSavedTrafficPercent);
		int offsetX = Math.round(mTimerCounter * 10);
		int offsetY = trafficPctToOffsetY(mSavedTrafficPercent, mWaterHeight);
		
		Log.d("vulcan-traff","getCurDrawContent: iPicIdx = " + iPicIdx + ", mSavedTrafficPercent = " + mSavedTrafficPercent);
		
		Bitmap bmpCanvas = mBmpBg[iPicIdx].copy(Bitmap.Config.ARGB_8888,true);
		drawBg(cv, bmpCanvas);
		Log.d("vulcan-size","getCurDrawContent:bmpCanvas = " + bmpCanvas.getWidth());
		drawWater(cv,mBmpAnim[iPicIdx], 0, offsetY, 255);
		drawWater(cv,mBmpAnim[iPicIdx], mWaterWidth/3, offsetY, 127);
		drawHint(cv);
		drawForeground(cv,mBmpFore[iPicIdx]);
		//bmpCanvas = Utils.getRoundedBitmap(bmpCanvas);
		//bmpCanvas = Utils.getShadowBitmap1(bmpCanvas, mShadowDrawable);
		return new FastBitmapDrawable(bmpCanvas);
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#getCurDrawContent()
	 */
	@Override
	public Drawable getCurDrawContent() {
		Canvas cv = mCanvas;
		//canvas is not ready!
		if(cv == null) {
			return null;
		}

		return getDrawableNormal(cv);
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#getRedrawMultipleFreq()
	 */
	@Override
	public int getRedrawMultipleFreq() {
		// TODO Auto-generated method stub
		//return 10*60*60*4;//4 hours
		return 20;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#refreshDynIcon()
	 */
	@Override
	public boolean refreshDynIcon() {
		Drawable d = getCurDrawContent();
		
		if (d != null) {
			((TextView)hostView).setCompoundDrawablesWithIntrinsicBounds(null, d, null,
					null);
			hostView.invalidate();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if(mSavedTrafficPercent != mShownTrafficPercent) {
			return true;
		}
		if(!mSavedTrafficUsed.equals(mShownTrafficUsed)) {
			return true;
		}
		if(mSavedValuePlanIsSet != mShownValuePlanIsSet) {
			return true;
		}
		if(mSavedValueCreditUsedOut != mShownValueCreditUsedOut) {
			return true;
		}
		if(mSavedValueShouldAlarm != mShownValueShouldAlarm) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#resetDirty()
	 */
	@Override
	public void resetDirty() {
		mShownTrafficPercent = mSavedTrafficPercent;
		mShownTrafficUsed = mSavedTrafficUsed;
		mShownValuePlanIsSet = mSavedValuePlanIsSet;
		mShownValueCreditUsedOut = mSavedValueCreditUsedOut;
		mShownValueShouldAlarm = mSavedValueShouldAlarm;
	}

	/* (non-Javadoc)
	 * @see com.aurora.plugin.DynIconPlg#dump()
	 */
	@Override
	public void dump() {
		// TODO Auto-generated method stub

	}

}
