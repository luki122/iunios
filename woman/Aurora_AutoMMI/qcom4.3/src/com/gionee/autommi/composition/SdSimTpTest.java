package com.gionee.autommi.composition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.TouchPadTest.TouchPadView;

import com.gionee.autommi.SimCardTest;
import com.gionee.autommi.SdCardTest;

import com.gionee.autommi.TouchPadTest;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.StatFs;

import android.content.Context;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;

public class SdSimTpTest extends BaseActivity {
	private float mAverageHeight;
	private float mAverageWidth;
	private Handler mTouchHandler;
	private Runnable mTouchRunanable, mRestartRunnable;
	private boolean mIsRestart;
	private static final int RIGHT_MESSAGE = 0;
	private static final int WRONG_MESSAGE = 1;
	private static final int RESTART_MESSAGE = 2;
	private boolean pass;
	
	private MSimTelephonyManager phoneManagerEx;
	private StorageManager storageManager;
	private TelephonyManager tm;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		getWindow().setAttributes(lp);
		setContentView(new TouchPadView(this));
		((AutoMMI) getApplication()).recordResult(TouchPadTest.TAG, "", "0");
		
		storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = storageManager.getVolumeList();
		String info = "";
		for(int i = 0; i < volumes.length; i++){
			String path = volumes[i].getPath();
			String state = storageManager.getVolumeState(path);
			if(path.contains("sdcard")) {
				StatFs stat = new StatFs(path);
				int allVol = (int) (((long)stat.getBlockCount()*stat.getBlockSize())/(1024*1024));
				int avaiableVol = (int)(((long)stat.getAvailableBlocks()*stat.getBlockSize())/(1024*1024));
				info += path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
			}
		}
		info = info.substring(0, info.length()-1);
		((AutoMMI)getApplication()).recordResult(SdCardTest.TAG, info, "2");
		Toast.makeText(this, info.replace("|", "\n"), Toast.LENGTH_LONG).show();		
		/*
		 * if external sd card exist
		 *     /storage/sdcard0 ---->  external sd
		 *     /storage/sdcard1 ---->  internal sd
		 *  else 
		 *     /storage/sdcard0 -----> internal sd
		 *     /storage/sdcard1 -----> removed
		 * */
		
		phoneManagerEx = (MSimTelephonyManager) this.getSystemService(Context.MSIM_TELEPHONY_SERVICE);
		info = "";
		if (null != phoneManagerEx) {
			int sim1State = phoneManagerEx.getSimState(0);
			int sim2State = phoneManagerEx.getSimState(1);
			info = "sim1:" + sim1State + "|smi2:" + sim2State;
			Log.d(SimCardTest.TAG, info);
		} else {
			tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			int simState = tm.getSimState();
		    info = "sim:" + simState;
		}
		((AutoMMI)getApplication()).recordResult(SimCardTest.TAG, info, "2");
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
		/* SIM_STATE_ABSENT          1
		 * SIM_STATE_NETWORK_LOCKED  4
		 * SIM_STATE_PIN_REQUIRED    2
		 * SIM_STATE_PUK_REQUIRED    3
		 * SIM_STATE_READY           5
		 * SIM_STATE_UNKNOWN         0
		 */

	}

	public class TouchPadView extends View {
		private float mX, mY;
		private Path mPath;
		private Canvas mCanvas;
		private Bitmap mBitmap;
		private Paint mBitmapPaint;
		private Paint mBackGroudPaint, mLinePaint, mPaint, mTestPaint;
		private RectF mRf = new RectF();
		private float[] mVertBaseline = new float[16];
		private float[] mHorBaseline = new float[11];
		private static final float TOUCH_TOLERANCE = 4;
		private ArrayList mLeftList = new ArrayList<Integer>();
		private ArrayList mRightList = new ArrayList<Integer>();
		private ArrayList mTopList = new ArrayList<Integer>();
		private ArrayList mBottomList = new ArrayList<Integer>();
		private ArrayList<RectF> mTestRecs = new ArrayList<RectF>();
		private Set<Integer> mIdxTRecsBePassed = new HashSet<Integer>();

		public TouchPadView(Context context) {
			super(context);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			mBackGroudPaint = new Paint();
			mBackGroudPaint.setColor(Color.GRAY);
			mTestPaint = new Paint();
			mTestPaint.setColor(Color.YELLOW);
			mTestPaint.setStyle(Style.STROKE);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Style.STROKE);

			mLinePaint = new Paint();
			mLinePaint.setColor(Color.GRAY);
			// TODO Auto-generated constructor stub
		}

		private void setTestRcts() {
			for (int i = 1; i < 9; i++) {
				mTestRecs.add(new RectF(mHorBaseline[i], mVertBaseline[i + 2],
						mHorBaseline[i + 1], mVertBaseline[i + 3]));
			}

			for (int i = 1; i < 9; i++) {
				mTestRecs.add(new RectF(mHorBaseline[i],
						mVertBaseline[9 + (2 - i)], mHorBaseline[i + 1],
						mVertBaseline[10 + (2 - i)]));
			}

			float hPivot = mHorBaseline[5];
			for (int i = 1; i < 14; i++) {
				if (6 == i || 7 == i)
					continue;
				mTestRecs.add(new RectF(hPivot - mAverageWidth / 2,
						mVertBaseline[i], hPivot + mAverageWidth / 2,
						mVertBaseline[i + 1]));
			}

			float vHivot = mVertBaseline[7];
			for (int i = 1; i < 9; i++) {
				if (4 == i || 5 == i)
					continue;
				mTestRecs.add(new RectF(mHorBaseline[i], vHivot
						- mAverageHeight / 2, mHorBaseline[i + 1], vHivot
						+ mAverageHeight / 2));
			}
		}

		// Gionee xiaolin 20121113 add for CR00730261 end

		@Override
		protected void onDraw(Canvas canvas) {
			if (null == mBitmap) {
				mBitmap = Bitmap.createBitmap(getMeasuredWidth(),
						getMeasuredHeight(), Bitmap.Config.ARGB_8888);
				mCanvas = new Canvas(mBitmap);
			}
			canvas.drawColor(Color.WHITE);
			if (0 == mAverageHeight) {
				mAverageWidth = getMeasuredWidth() / 10;
				mAverageHeight = getMeasuredHeight() / 15;
				for (int i = 0; i < 14; i++) {
					mVertBaseline[i] = i * mAverageHeight;
				}
				mVertBaseline[14] = getMeasuredHeight() - mAverageHeight;
				mVertBaseline[15] = getMeasuredHeight();

				for (int i = 0; i < 9; i++) {
					mHorBaseline[i] = i * mAverageWidth;
				}
				mHorBaseline[9] = getMeasuredWidth() - mAverageWidth;
				mHorBaseline[10] = getMeasuredWidth();
			}
			canvas.drawLine(0, mAverageHeight, getMeasuredWidth(),
					mAverageHeight, mLinePaint);
			for (int i = 0; i < 12; i++) {
				canvas.drawLine(0, mVertBaseline[i + 2], mAverageWidth,
						mVertBaseline[i + 2], mLinePaint);
				canvas.drawLine(getMeasuredWidth() - mAverageWidth,
						mVertBaseline[i + 2], getMeasuredWidth(),
						mVertBaseline[i + 2], mLinePaint);
			}
			canvas.drawLine(0, getMeasuredHeight() - mAverageHeight,
					getMeasuredWidth(), getMeasuredHeight() - mAverageHeight,
					mLinePaint);

			canvas.drawLine(mAverageWidth, 0, mAverageWidth,
					getMeasuredHeight(), mLinePaint);
			for (int i = 0; i < 7; i++) {
				canvas.drawLine(mHorBaseline[i + 2], 0, mHorBaseline[i + 2],
						mAverageHeight, mLinePaint);
				canvas.drawLine(mHorBaseline[i + 2], getMeasuredHeight()
						- mAverageHeight, mHorBaseline[i + 2],
						getMeasuredHeight(), mLinePaint);
			}
			canvas.drawLine(getMeasuredWidth() - mAverageWidth, 0,
					getMeasuredWidth() - mAverageWidth, getMeasuredHeight(),
					mLinePaint);

			if (false == mLeftList.isEmpty()) {
				for (int i = 0; i < mLeftList.size(); i++) {
					mRf.set(0, mVertBaseline[((Integer) mLeftList.get(i))
							.intValue()], mAverageWidth,
							mVertBaseline[((Integer) mLeftList.get(i))
									.intValue() + 1]);
					canvas.drawRect(mRf, mBackGroudPaint);
				}
			}

			if (false == mRightList.isEmpty()) {
				for (int i = 0; i < mRightList.size(); i++) {
					mRf.set(getMeasuredWidth() - mAverageWidth,
							mVertBaseline[((Integer) mRightList.get(i))
									.intValue()], getMeasuredWidth(),
							mVertBaseline[((Integer) mRightList.get(i))
									.intValue() + 1]);
					canvas.drawRect(mRf, mBackGroudPaint);
				}
			}

			if (false == mTopList.isEmpty()) {
				for (int i = 0; i < mTopList.size(); i++) {
					mRf.set(mHorBaseline[((Integer) mTopList.get(i)).intValue()],
							0,
							mHorBaseline[((Integer) mTopList.get(i)).intValue() + 1],
							mAverageHeight);
					canvas.drawRect(mRf, mBackGroudPaint);
				}
			}

			if (false == mBottomList.isEmpty()) {
				for (int i = 0; i < mBottomList.size(); i++) {
					mRf.set(mHorBaseline[((Integer) mBottomList.get(i))
							.intValue()], getMeasuredHeight() - mAverageHeight,
							mHorBaseline[((Integer) mBottomList.get(i))
									.intValue() + 1], getMeasuredHeight());
					canvas.drawRect(mRf, mBackGroudPaint);
				}
			}

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			/*
			 * Log.d(TAG, "*********mTestRecs.size() = " + mTestRecs.size()); if
			 * (mTestRecs.size() == 0) setTestRcts();
			 * 
			 * 
			 * drawTestRects(canvas); for (int idx : mIdxTRecsBePassed) {
			 * canvas.drawRect(mTestRecs.get(idx), mBackGroudPaint); }
			 */
			canvas.drawPath(mPath, mPaint);
		}

		private void drawTestRects(Canvas canvas) {
			// TODO Auto-generated method stub
			for (RectF rectf : mTestRecs) {
				canvas.drawRect(rectf, mTestPaint);
			}
		}

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			if (null != mCanvas) {
				mPath.lineTo(mX, mY);
				mCanvas.drawPath(mPath, mPaint);
				// kill this so we don't double draw
				mPath.reset();
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			for (RectF rf : mTestRecs) {
				if (rf.contains(x, y)) {
					int idx = mTestRecs.indexOf(rf);
					mIdxTRecsBePassed.add(idx);
					break;
				}
			}

			if (x < mAverageWidth) {
				int i = gnBinarySearch(y, mVertBaseline, 0, 14);
				if (-1 != i && false == mLeftList.contains(i)) {
					mLeftList.add(i);
					Log.e("lich", "mLeftList.add(i) = " + i);
				}
			} else if (x > mHorBaseline[9]) {
				int i = gnBinarySearch(y, mVertBaseline, 0, 14);
				if (-1 != i && false == mRightList.contains(i)) {
					mRightList.add(i);
				}
			} else if (y < mAverageHeight) {
				int i = gnBinarySearch(x, mHorBaseline, 0, 9);
				if (-1 != i && false == mTopList.contains(i) && i != 9
						&& i != 0) {
					mTopList.add(i);
				}
			} else if (y > mVertBaseline[14]) {
				int i;
				i = gnBinarySearch(x, mHorBaseline, 0, 9);
				if (-1 != i && false == mBottomList.contains(i) && i != 9
						&& i != 0) {
					mBottomList.add(i);
				}
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				// if (46 + 33 == mLeftList.size() + mRightList.size() +
				// mTopList.size()
				// + mBottomList.size() + mIdxTRecsBePassed.size()) {
				if (46 == mLeftList.size() + mRightList.size()
						+ mTopList.size() + mBottomList.size()) {
					pass = true;
					((AutoMMI) getApplication()).recordResult(TouchPadTest.TAG,
							"", "1");
				}
				break;
			}
			return true;
		}

		public int gnBinarySearch(float elem, float[] array, int low, int high) {
			for (int i = 0; i < array.length - 1; i++) {
				if (elem >= array[i] && elem < array[i + 1]) {
					return i;
				}
			}
			return -1;
		}

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
}
