package com.gionee.horoscope;
//Gionee <jiating><2013-05-29> modify for CR00000000 begin 
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import com.android.calendar.R;
/**
 * Title: MyLinearLayout.java<br>
 * Version:v1.0
 */
public class GNHoroscopeLinearLayout extends RelativeLayout {
	private ImageView iv1;
	private ViewPager iv2;
	private RelativeLayout lin;
	int left, top;
	float startX, startY;
	float startX1, startY1;
	float currentX, currentY;

	int rootW, rootH;

	int iv1H;
	private Scroller scroller;
//	private GNTouchTool tool;
	boolean isMoved;

	// 最大移动距离,实际情况可以变化
	static final int LEN = 200;

	public GNHoroscopeLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

//	protected void onFinishInflate() {
//		super.onFinishInflate();
//		Log.i("jiating", "onFinishInflate");
//		iv1 = (ImageView) findViewById(R.id.iv1);
//		iv2 = (ViewPager) findViewById(R.id.iv);
//		lin = (RelativeLayout) findViewById(R.id.linearLayout1);
////		setLongClickable(true);
//		scroller = new Scroller(getContext(),
//				new AccelerateDecelerateInterpolator());
//	}

//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		// TODO Auto-generated method stub
//		Log.i("jiating", "onInterceptTouchEvent");
//
//		int action = ev.getAction();
//		if (!scroller.isFinished()) {
//			Log.i("jiating", "!scroller.isFinished()");
//			return super.onTouchEvent(ev);
//		}
//		currentX = ev.getX();
//		currentY = ev.getY();
//		switch (action) {
//
//		case MotionEvent.ACTION_DOWN:
//			left = iv2.getLeft();
//			top = iv2.getTop();
//
//			rootW = getWidth();
//			rootH = getHeight();
//
//			iv1H = iv1.getHeight();
//
//			startX = currentX;
//			startY = currentY;
//			// TouchTool 是滑动工具，是下面的imageview移动的时候，计算能够移动的距离
//			tool = new GNTouchTool(iv2.getLeft(), iv2.getTop(), iv2.getLeft(),
//					iv2.getTop() + LEN);
//			Log.i("jiating", "onInterceptTouchEvent.....ACTION_DOWN=" + left
//					+ "top=" + top + "rootW=" + rootW + "rootH=" + rootH
//					+ "iv1H=" + iv1H + "startX=" + startX + "startY=" + startY);
//			break;
//		case MotionEvent.ACTION_MOVE:
//
//			if (Math.abs(currentY - startY) > Math.abs(currentX - startX)
//					&& Math.abs((currentY - startY)) > 150 && !isMoved) {
//				Log.i("jiating", "onInterceptTouchEvent.......ACTION_MOVE");
//
//				if (tool != null) {
//
//					int l = tool.getScrollX(currentX - startX);
//					int t = tool.getScrollY(currentY - startY);
//					Log.i("jiating", "ACTION_MOVE...l=" + l + "t=" + t
//							+ "iv2.getTop()=" + iv2.getTop()
//							+ "iv1.getBottom()" + iv1.getBottom());
//					if ((t >= top && t <= iv2.getTop() + LEN)
//							|| (t <= top && t >= iv2.getTop() + LEN)) {
//						// 滑动时候，重新定位上面的布局和下面的布局
//					
//						iv2.layout(left, t, left + iv2.getWidth(),
//								t + iv2.getHeight());
//						lin.layout(lin.getLeft(), t - lin.getHeight(),
//								lin.getLeft() + lin.getWidth(), t);
//						iv1.layout(0, 0, iv1.getWidth(), t);
//						Log.i("jiating",
//								"onInterceptTouchEvent..ACTION_MOVE...if...iv2...left="
//										+ left + "t=" + t + "iv2.getWidth()="
//										+ (left + iv2.getWidth())
//										+ "t + iv2.getHeight()=" + t
//										+ iv2.getHeight());
//						Log.i("jiating", "ACTION_MOVE...if...iv1...t=" + t);
//
//					}
//				}
//				return true;
//			} else {
//				return false;
//			}
//
//		case MotionEvent.ACTION_UP:
//			System.out.printf("left1:%s left2:%s\n", iv2.getLeft(), 0);
//			System.out.printf("top1:%s top2:%s\n", iv2.getTop(), iv1H);
//			// 抬起手的时候，慢慢的换到原先位置
//			Log.i("jiating",
//					"onInterceptTouchEvent....ACTION_UP...if...iv1...iv2.getLeft()="
//							+ iv2.getLeft() + "iv2.getTop()=" + iv2.getTop()
//							+ "iv1H - iv2.getTop()" + (iv1H - iv2.getTop())+"isMoved="+isMoved);
//			invalidate();
//			if(!isMoved){
//				isMoved=true;
//			scroller.startScroll(iv2.getLeft(), iv2.getTop(),
//					0 - iv2.getLeft(), iv1H - iv2.getTop(), 200);
//			invalidate();
//			return true;
//			}else{
//				return false;
//			}
//
//		default:
//			break;
//		}
//
//		return super.onInterceptTouchEvent(ev);
//	}
//
//	public boolean onTouchEvent(MotionEvent event) {
//		Log.i("jiating", "onTouchEvent");
//		int action = event.getAction();
//		if (!scroller.isFinished()) {
//			Log.i("jiating", "!scroller.isFinished()");
//			return super.onTouchEvent(event);
//		}
//		currentX = event.getX();
//		currentY = event.getY();
//		Log.i("jiating", "onTouchEventcurrentX..=" + currentX + "currentY="
//				+ currentY);
//		switch (action) {
//		
//		case MotionEvent.ACTION_MOVE:
//			// int l = (int) (left + currentX - startX);
//			// int t = (int) (top + currentY - startY);
//			if (Math.abs(currentY - startY) > Math.abs(currentX - startX)
//					&& Math.abs((currentY - startY)) > 150 && !isMoved) {
//				Log.i("jiating", "onInterceptTouchEvent.......ACTION_MOVE");
//
//				if (tool != null) {
//
//					int l = tool.getScrollX(currentX - startX);
//					int t = tool.getScrollY(currentY - startY);
//					Log.i("jiating", "ACTION_MOVE...l=" + l + "t=" + t
//							+ "iv2.getTop()=" + iv2.getTop()
//							+ "iv1.getBottom()" + iv1.getBottom());
//					if ((t >= top && t <= iv2.getTop() + LEN)
//							|| (t <= top && t >= iv2.getTop() + LEN)) {
//						// 滑动时候，重新定位上面的布局和下面的布局
//					
//						iv2.layout(left, t, left + iv2.getWidth(),
//								t + iv2.getHeight());
//						lin.layout(lin.getLeft(), t - lin.getHeight(),
//								lin.getLeft() + lin.getWidth(), t);
//						iv1.layout(0, 0, iv1.getWidth(), t);
//						Log.i("jiating",
//								"onInterceptTouchEvent..ACTION_MOVE...if...iv2...left="
//										+ left + "t=" + t + "iv2.getWidth()="
//										+ (left + iv2.getWidth())
//										+ "t + iv2.getHeight()=" + t
//										+ iv2.getHeight());
//						Log.i("jiating", "ACTION_MOVE...if...iv1...t=" + t);
//
//					}
//				}
//				return true;
//			} else {
//				return false;
//			}
//		case MotionEvent.ACTION_UP:
//			// iv2.layout(left, top, left + iv2.getWidth(), top +
//			// iv2.getHeight());
//			// iv1.layout(0, 0, iv1.getWidth(), iv1H);
//			System.out.printf("left1:%s left2:%s\n", iv2.getLeft(), 0);
//			System.out.printf("top1:%s top2:%s\n", iv2.getTop(), iv1H);
//			// 抬起手的时候，慢慢的换到原先位置
//			Log.i("jiating",
//					"ACTION_UP...if...iv1...iv2.getLeft()=" + iv2.getLeft()
//							+ "iv2.getTop()=" + iv2.getTop()
//							+ "iv1H - iv2.getTop()" + (iv1H - iv2.getTop()));
//			if(!isMoved){
//				isMoved=true;
//			scroller.startScroll(iv2.getLeft(), iv2.getTop(),
//					0 - iv2.getLeft(), iv1H - iv2.getTop()+100, 200);
//			invalidate();
//			return true;
//			}else{
//				return false;
//			}
//			
//		}
//		return super.onTouchEvent(event);
//	}
//
//	public void computeScroll() {
//		if (scroller.computeScrollOffset()) {
//			int x = scroller.getCurrX();
//			int y = scroller.getCurrY();
//			System.out.println("x=" + x);
//			System.out.println("y=" + y);
//
//			Log.i("jiating", "computeScroll...ifiv1..x=" + x + "y=" + y
//					+ "x + iv1.getWidth()=" + (x + iv1.getWidth()));
//			Log.i("jiating", "computeScroll...ifiv2..x=" + x + "y=" + y
//					+ "x + iv2.getWidth()=" + (x + iv2.getWidth())
//					+ "y + iv2.getHeight()=" + (y + iv2.getHeight()));
//			iv1.layout(0, 0, x + iv1.getWidth(), y);
//			lin.layout(0, y - lin.getHeight(), 0 + lin.getWidth(), y);
//			iv2.layout(x, y, x + iv2.getWidth(), y + iv2.getHeight());
//			invalidate();
//		}
//	}
}
//Gionee <jiating><2013-05-29> modify for CR00000000 end