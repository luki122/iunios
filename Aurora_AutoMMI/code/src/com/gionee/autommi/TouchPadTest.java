package com.gionee.autommi;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.HashSet;
import java.util.Set;
import android.graphics.Point;


public class TouchPadTest extends BaseActivity {
    private float mAverageHeight;
    private float mAverageWidth;
    private Handler mTouchHandler;
    private Runnable mTouchRunanable, mRestartRunnable;
    private boolean mIsRestart;
    public static final String TAG = "TouchPadTest";
    private static final int RIGHT_MESSAGE = 0;
    private static final int WRONG_MESSAGE = 1;
    private static final int RESTART_MESSAGE = 2;
    private boolean pass;


    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        getWindow().setAttributes(lp);
        setContentView(new TouchPadView(this));
        ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
    
    }
    
    public class TouchPadView extends View {      
        
        private float mX, mY;
        private Path  mPath;
        private Canvas  mCanvas;
        private Bitmap mBitmap;
        private Paint mBitmapPaint;
		// Gionee xiaolin 20121113 modify for CR00730261 start
        private Paint mBackGroudPaint, mLinePaint, mPaint, mTestPaint;
		// Gionee xiaolin  20121113 modify for CR00730261 end
        private RectF mRf = new RectF();
        private float[] mVertBaseline = new float[16];
        private float[] mHorBaseline = new float[11];
        private static final float TOUCH_TOLERANCE = 4;
        private ArrayList mLeftList = new ArrayList<Integer>();
        private ArrayList mRightList = new ArrayList<Integer>();
        private ArrayList mTopList = new ArrayList<Integer>();
        private ArrayList mBottomList = new ArrayList<Integer>();
		// Gionee xiaolin  20121113 add for CR00730261 start
        private ArrayList<RectF> mTestRecs = new ArrayList<RectF>();
        private Set<Integer>  mIdxTRecsBePassed=  new HashSet<Integer>();
		// Gionee xiaolin   20121113 add for CR00730261 end

        public TouchPadView(Context context) {
            super(context);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBackGroudPaint = new Paint();
            mBackGroudPaint.setColor(Color.GRAY);
            // Gionee xiaolin 20121113 add for CR00730261 start
            mTestPaint = new Paint();
            mTestPaint.setColor(Color.YELLOW);
            mTestPaint.setStyle(Style.STROKE);
            // Gionee xiaolin 20121113 add for CR00730261 end
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Style.STROKE);
            
            mLinePaint = new Paint();
            mLinePaint.setColor(Color.GRAY);
            // TODO Auto-generated constructor stub
        }
		
		// Gionee xiaolin 20121113 add for CR00730261 start
		private void setTestRcts() {
            // TODO Auto-generated method stub
            for(int i = 1; i < 9; i++) {
                mTestRecs.add(new RectF(mHorBaseline[i],mVertBaseline[i+2], mHorBaseline[i+1],mVertBaseline[i+3]));
            }
            
            for(int i =1; i < 9; i++) {
                mTestRecs.add(new RectF(mHorBaseline[i],mVertBaseline[9+(2-i)],mHorBaseline[i+1],mVertBaseline[10+(2-i)]));
            }
            
            float hPivot = mHorBaseline[5];
            for(int i =1; i < 14; i++ ) {
                if( 6 == i || 7 ==i)
                    continue;
                mTestRecs.add(new RectF(hPivot-mAverageWidth/2, mVertBaseline[i], hPivot + mAverageWidth/2, mVertBaseline[i+1]));
            }
            
            float vHivot = mVertBaseline[7];
            for(int i =1; i< 9; i++) {
                if (4 == i || 5 ==i)
                    continue;
                mTestRecs.add(new RectF(mHorBaseline[i], vHivot-mAverageHeight/2, mHorBaseline[i+1], vHivot + mAverageHeight/2));
            }
        }
        // Gionee xiaolin 20121113 add for CR00730261 end
		
        @Override
        protected void onDraw(Canvas canvas) {
            if (null == mBitmap) {            
                mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
            }
            canvas.drawColor(Color.WHITE);
            if (0 == mAverageHeight) {
                mAverageWidth = getMeasuredWidth()/10;
                mAverageHeight = getMeasuredHeight()/15;
                for(int i=0; i<14; i++) {
                    mVertBaseline[i] = i * mAverageHeight;
                }
                mVertBaseline[14] = getMeasuredHeight() - mAverageHeight;
                mVertBaseline[15] = getMeasuredHeight();
                
                for(int i=0; i<9; i++) {
                    mHorBaseline[i] = i * mAverageWidth;
                }
                mHorBaseline[9] = getMeasuredWidth() - mAverageWidth;
                mHorBaseline[10] = getMeasuredWidth();
            }
            canvas.drawLine(0, mAverageHeight, getMeasuredWidth(), mAverageHeight, mLinePaint);
            for (int i=0; i<12; i++) {
                canvas.drawLine(0, mVertBaseline[i+2], mAverageWidth, mVertBaseline[i+2], mLinePaint);
                canvas.drawLine(getMeasuredWidth()-mAverageWidth, mVertBaseline[i+2], getMeasuredWidth(), mVertBaseline[i+2], mLinePaint);
            }
            canvas.drawLine(0, getMeasuredHeight() - mAverageHeight, getMeasuredWidth(), getMeasuredHeight() - mAverageHeight, mLinePaint);
            
            canvas.drawLine(mAverageWidth, 0, mAverageWidth, getMeasuredHeight(), mLinePaint);
            for (int i=0; i<7; i++) {
                canvas.drawLine(mHorBaseline[i+2], 0, mHorBaseline[i+2], mAverageHeight, mLinePaint);
                canvas.drawLine(mHorBaseline[i+2], getMeasuredHeight() - mAverageHeight,  mHorBaseline[i+2], getMeasuredHeight(), mLinePaint);
            }
            canvas.drawLine(getMeasuredWidth() - mAverageWidth, 0, getMeasuredWidth() -mAverageWidth, getMeasuredHeight(), mLinePaint);
            
            if (false == mLeftList.isEmpty()) {
                for (int i = 0; i < mLeftList.size(); i++) {
                    mRf.set(0, mVertBaseline[((Integer) mLeftList.get(i)).intValue()],
                            mAverageWidth,
                            mVertBaseline[((Integer) mLeftList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mRightList.isEmpty()) {
                for (int i = 0; i < mRightList.size(); i++) {
                    mRf.set(getMeasuredWidth() - mAverageWidth, mVertBaseline[((Integer) mRightList.get(i)).intValue()],
                            getMeasuredWidth(),
                            mVertBaseline[((Integer) mRightList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mTopList.isEmpty()) {
                for (int i = 0; i < mTopList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mTopList.get(i)).intValue()], 0,
                            mHorBaseline[((Integer) mTopList.get(i)).intValue() + 1],
                            mAverageHeight);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mBottomList.isEmpty()) {
                for (int i = 0; i < mBottomList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mBottomList.get(i)).intValue()], getMeasuredHeight() - mAverageHeight,
                            mHorBaseline[((Integer) mBottomList.get(i)).intValue() + 1],
                            getMeasuredHeight());
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            // Gionee xiaolin 20121113 add for CR00730261 start
			// Gionee xiaolin 20121222
			/*
            Log.d(TAG, "*********mTestRecs.size() = " + mTestRecs.size());
            if (mTestRecs.size() == 0) 
                setTestRcts();
            
            
            drawTestRects(canvas);
            for (int idx : mIdxTRecsBePassed) {
                canvas.drawRect(mTestRecs.get(idx), mBackGroudPaint);
            }
			*/
			// Gionee xiaolin 20121222
			// Gionee xiaolin 20121113 add for CR00730261 end
            canvas.drawPath(mPath, mPaint);
        }

        // Gionee xiaolin 20121113 add for CR00730261 start
        private void drawTestRects(Canvas canvas) {
            // TODO Auto-generated method stub
            for ( RectF rectf : mTestRecs){
                canvas.drawRect(rectf, mTestPaint);
                Log.d(TAG, "drawRect : " + rectf);
                if (rectf.isEmpty())
                    Log.d(TAG, rectf + " is isEmpty!!!"); 
            }        
        }
		// Gionee xiaolin 20121113 add for CR00730261 end
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
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
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
            // Gionee xiaolin 20121113 add for CR00730261  start
            Log.d(TAG, " ****** " + "(" + x + " ," + y + ") ******");
            for ( RectF rf : mTestRecs) {
                if (rf.contains(x, y)) {
                    Log.d(TAG, rf + " contains " + "(" + x + " ," + y + ")");
                    int idx = mTestRecs.indexOf(rf);
                    mIdxTRecsBePassed.add(idx);
                    break;
                }                  
            }
			// Gionee xialin 20121113 add for CR00730261 end
            // Gionee xiaolin 20121018 modify for CR00715724 start
            if (x < mAverageWidth) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mLeftList.contains(i)) {
                    mLeftList.add(i);
                    Log.e("lich", "mLeftList.add(i) = " + i);
                }
            } else if(x > mHorBaseline[9]) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mRightList.contains(i)) {
                    mRightList.add(i);
                }
            } else if (y < mAverageHeight) {
                int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mTopList.contains(i) && i != 9 && i != 0) {
                    mTopList.add(i);
                }
            } else if (y > mVertBaseline[14]) {
                int i;
                    i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                    mBottomList.add(i);
                }
            }
            // Gionee xiaolin 20121018 modify for CR00715724 end
            
            
            
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
					// Gionee xiaolin 20121113 modify for CR00730261 start        
                    //if (46 + 33 == mLeftList.size() + mRightList.size() + mTopList.size()
                    //        + mBottomList.size() + mIdxTRecsBePassed.size()) {
                    if (46 == mLeftList.size() + mRightList.size() + mTopList.size() + mBottomList.size() ) {
				    // Gionee xiaolin 20121113 modify for CR00730261 end
                    	pass = true;
                    	((AutoMMI)getApplication()).recordResult(TAG, "", "1");
                    }
                    break;
            }
            return true;
        }
        
        public int gnBinarySearch(float elem,float[] array,int low,int high) {
            for (int i=0; i<array.length-1; i++) {
                if (elem >= array[i] && elem < array[i+1]) {
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