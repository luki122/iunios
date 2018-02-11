/**
 * 
 */
package com.aurora.plugin;
import com.aurora.launcher.R;
import java.util.Calendar;

import com.aurora.launcher.FastBitmapDrawable;
import com.aurora.launcher.LauncherApplication;
import com.aurora.launcher.Utilities;
import com.aurora.launcher.Utils;
import com.aurora.util.DeviceProperties;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

/**
 * Vulcan added these code in 下午2:53:15
 *
 */
public class ClockMovingIcon extends DynIconPlg {
	
	private static int mDialWidth;
	private static int mDialHeight;
	private static int mPtHourWidth;
	private static int mPtHourHeight;
	private static int mPtMinuteWidth;
	private static int mPtMinuteHeight;
	private static int mPtSecondWidth;
	private static int mPtSecondHeight;
	private static BitmapDrawable mDrawableDial;
	private static BitmapDrawable mDrawablePtHour;
	private static BitmapDrawable mDrawablePtMinute;
	private static BitmapDrawable mDrawablePtSecond;
	//private static Bitmap mBmpPtHour;
	//private static Bitmap mBmpPtMinute;
	//private static Bitmap mBmpPtSecond;
	private static Bitmap mBmpDialBuf;
	private static Bitmap mBmpDialPtMinuteBuf;
	
	private static final Rect mPtRect = new Rect();

	/**
	 * @param v
	 */
	public ClockMovingIcon(View v) {
		super(v);
		// TODO Auto-generated constructor stub
		mIsDynamic = true;
	}
	
	public int getRedrawMultipleFreq() {
		return 10;
	}
	
	public boolean isDirty() {
		return true;
	}
	
	@Override
	public void resetDirty() {
		return;
	}

	
	public Drawable getCurDrawContent() {

		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int hour = mCalendar.get(Calendar.HOUR);
		int minute = mCalendar.get(Calendar.MINUTE);
		int second = mCalendar.get(Calendar.SECOND);
		int msecond = mCalendar.get(Calendar.MILLISECOND);

		Canvas cv = mCanvas;
		
		//canvas is not ready!
		if(cv == null) {
			return null;
		}		
		
		Bitmap bmpCanvas = null;

		// 1.determine which background will be used
		if (minute != mLastMinute 
				|| mBmpDialPtMinuteBuf == null) {
			// redraw all
			bmpCanvas = mBmpDialBuf.copy(Bitmap.Config.ARGB_8888, true);
			drawBg(cv, bmpCanvas);
			drawPtMinute(cv, minute, second);
			drawPtHour(cv, hour, minute);
			mBmpDialPtMinuteBuf = bmpCanvas.copy(Bitmap.Config.ARGB_8888, true);
			drawPtSecond(cv, second, msecond);
		} else {
			bmpCanvas = mBmpDialPtMinuteBuf.copy(Bitmap.Config.ARGB_8888, true);
			drawBg(cv, bmpCanvas);
			drawPtSecond(cv, second, msecond);
		}

		// LauncherApplication.logVulcan.print("getCurDrawContent:second = " +
		// second);

		mLastHour = hour;
		mLastMinute = minute;

		return new FastBitmapDrawable(bmpCanvas);
	}

	public static void loadRes(Context context) {
		Resources res = context.getResources();

		mDrawableDial = (BitmapDrawable) res.getDrawable(R.drawable.launcher_deskclock_bg);
		mBmpDialBuf = mDrawableDial.getBitmap().copy(Bitmap.Config.ARGB_8888,
				true);
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		mBmpDialBuf = Utils.getRoundedBitmap(mBmpDialBuf, context);
		mBmpDialBuf = Utils.getShadowBitmap1(mBmpDialBuf, shadowDrawable);
		//vulcan added it in 2014-7-1
		//read device property and adjust size of the icon
		//adjust clock dial
		if(DeviceProperties.isNeedScale()) {
			mBmpDialBuf = Utilities.zoomBitmap(mBmpDialBuf, context);
		}
		mDialWidth = mBmpDialBuf.getWidth();
		mDialHeight = mBmpDialBuf.getHeight();

		//load resource of pointer hour
		BitmapDrawable drawablePtHour = (BitmapDrawable) res
				.getDrawable(R.drawable.launcher_deskclock_hour);
		Bitmap bmpPtHour = drawablePtHour.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		if(DeviceProperties.isNeedScale()) {
			bmpPtHour = Utilities.zoomBitmap(bmpPtHour, context);
		}
		mDrawablePtHour = new BitmapDrawable(res, bmpPtHour);
		mPtHourWidth = mDrawablePtHour.getIntrinsicWidth();
		mPtHourHeight = mDrawablePtHour.getIntrinsicHeight();

		//load resource of pointer minute
		BitmapDrawable drawablePtMinute = (BitmapDrawable) res
				.getDrawable(R.drawable.launcher_deskclock_minute);
		Bitmap bmpPtMinute = drawablePtMinute.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		if(DeviceProperties.isNeedScale()) {
			bmpPtMinute = Utilities.zoomBitmap(bmpPtMinute, context);
		}
		mDrawablePtMinute = new BitmapDrawable(res, bmpPtMinute);
		mPtMinuteWidth = mDrawablePtMinute.getIntrinsicWidth();
		mPtMinuteHeight = mDrawablePtMinute.getIntrinsicHeight();

		//load resource of pointer second
		BitmapDrawable drawablePtSecond = (BitmapDrawable) res
				.getDrawable(R.drawable.launcher_deskclock_second);
		Bitmap bmpPtSecond = drawablePtSecond.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		if(DeviceProperties.isNeedScale()) {
			bmpPtSecond = Utilities.zoomBitmap(bmpPtSecond, context);
		}
		mDrawablePtSecond = new BitmapDrawable(res, bmpPtSecond);
		mPtSecondWidth = mDrawablePtSecond.getIntrinsicWidth();
		mPtSecondHeight = mDrawablePtSecond.getIntrinsicHeight();

		mPtRect.left = (mDialWidth - mPtSecondWidth) / 2;
		mPtRect.top = (mDialHeight - mPtSecondHeight) / 2;
		mPtRect.right = (mDialWidth + mPtSecondWidth) / 2;
		mPtRect.bottom = (mDialHeight + mPtSecondHeight) / 2;

		//mCanvas = new Canvas();

		return;
	}

	public void drawBg(Canvas cv, Bitmap bmpCanvas) {

		cv.setBitmap(bmpCanvas);
		//mWidth = cv.getWidth();
		//mHeight = cv.getHeight();
		// LauncherApplication.logVulcan.print("drawBg:(mWidth,mHeight,mBmpDialW,mBmpDialH) = ("
		// + mWidth + ", " + mHeight + ", " + bmpCanvas.getWidth() + ", " +
		// bmpCanvas.getHeight() + ")");
		return;
	}

	// create background with pointers of hour and minute
	public void drawPtHour(Canvas cv, int hour, int minute) {

		//float hDegree = ((hour + (float) minute / 60) / 12) * 360;
		float hDegree = hour * 30.0f + (float)(minute >> 1);

		//int bleft = (mWidth - mPtHourWidth) / 2;
		//int btop = (mHeight - mPtHourHeight) / 2;
		//int bright = (mWidth + mPtHourWidth) / 2;
		//int bbottom = (mHeight + mPtHourHeight) / 2;
		
		cv.save();
		cv.rotate(hDegree, mDialWidth >> 1, mDialHeight >> 1);
		//mDrawablePtHour.setBounds(bleft, btop, bright, bbottom);
		mDrawablePtHour.setBounds(mPtRect);
		mDrawablePtHour.draw(cv);
		cv.restore();
		return;
	}

	public void drawPtMinute(Canvas cv, int minutes, int second) {
		float mDegree = ((minutes + (float) second / 60) / 60) * 360;

		//int bleft = (mWidth - mPtMinuteWidth) / 2;
		//int btop = (mHeight - mPtMinuteHeight) / 2;
		//int bright = (mWidth + mPtMinuteWidth) / 2;
		//int bbottom = (mHeight + mPtMinuteHeight) / 2;
		cv.save();
		cv.rotate(mDegree, mDialWidth >> 1, mDialHeight >> 1);
		//mDrawablePtMinute.setBounds(bleft, btop, bright, bbottom);
		mDrawablePtMinute.setBounds(mPtRect);
		mDrawablePtMinute.draw(cv);
		cv.restore();
		return;
	}

	public void drawPtSecond(Canvas cv, int second, int msecond) {
		//float sDegree = (((float) second + ((float) msecond) / 1000) / 60) * 360;
		float sDegree = (second + msecond / 1000.0f) * 6.0f;

		//int bleft = (mWidth - mPtSecondWidth) / 2;
		//int btop = (mHeight - mPtSecondHeight) / 2;
		//int bright = (mWidth + mPtSecondWidth) / 2;
		//int bbottom = (mHeight + mPtSecondHeight) / 2;

		// LauncherApplication.logVulcan.print("drawPtSecond:sDegree = " +
		// sDegree);
		// LauncherApplication.logVulcan.print("drawPtSecond:(left,top,right,bottom) = ("
		// + bleft + ", " + btop + ", " + bright + ", " + bbottom + ")");
		// LauncherApplication.logVulcan.print("drawPtSecond:(mWidth,mHeight) = ("
		// + mWidth + ", " + mWidth + ")");
		// LauncherApplication.logVulcan.print("drawPtSecond:(mPtSecondWidth,mPtSecondHeight) = ("
		// + mPtSecondWidth + ", " + mPtSecondHeight + ")");

		cv.save();
		cv.rotate(sDegree, mDialWidth >> 1, mDialHeight >> 1);
		//mDrawablePtSecond.setBounds(bleft, btop, bright, bbottom);
		mDrawablePtSecond.setBounds(mPtRect);
		mDrawablePtSecond.draw(cv);
		cv.restore();

		//LauncherApplication.logVulcan.print("drawPtHour: mPtSecondWidth = " + mPtSecondWidth + ", mPtSecondHeight = " + mPtSecondHeight);
		//LauncherApplication.logVulcan.print("drawPtHour: lefttop(x.y): " + bleft + "." + btop);
		//LauncherApplication.logVulcan.print("drawPtHour: rightbottom(x.y): " + bright + "." + bbottom);

		return;
	}


	//v0: normal
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

/*
	//v1: only invalidate
	public boolean refreshDynIcon() {
		hostView.invalidate();
		return true;
	}
*/
/*
	//v2: only get
	public boolean refreshDynIcon() {
		Drawable d = getCurDrawContent();
		return true;
	}
*/

	
	@Override
	public void dump() {
		return;
	}
}
