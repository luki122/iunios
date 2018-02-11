package com.aurora.mms.ui;
// Aurora xuyong 2015-10-08 created for aurora's new feature
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.android.mms.R;

public class AuroraHorizontalListView extends AdapterView<ListAdapter> {

	private static final String TAG = "AuroraHorizontalListView";
	
	public boolean mAlwaysOverrideTouch = true;  
    protected ListAdapter mAdapter;  
    private int mLeftViewIndex = -1;  
    private int mRightViewIndex = 0;  
    protected int mCurrentX;  
    protected int mNextX;  
    private int mMaxX = Integer.MAX_VALUE;  
    private int mDisplayOffset = 0;  
    protected Scroller mScroller;  
    private GestureDetector mGesture;  
    // Aurora xuyong 2015-10-15 modified for aurora's new feature start
    private Queue<View> mRemovedViewQueue = new LinkedList<View>();
    // Aurora xuyong 2015-10-15 modified for aurora's new feature end
    private boolean mDataChanged = false;
    
    private int mLeftSpace, mRightSpace;
    private Context mContext;
    
	private Handler mHanlder = new Handler();
    
	public AuroraHorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView();
		TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.AuroraHorizontalListView);
        if (t != null) {
	        mLeftSpace = t.getDimensionPixelOffset(R.styleable.AuroraHorizontalListView_left_space, 0);
	        Log.e(TAG, "mLeftSpace is " + mLeftSpace);
	        mRightSpace = t.getDimensionPixelOffset(R.styleable.AuroraHorizontalListView_right_space, 0);
	        Log.e(TAG, "mRightSpace is " + mRightSpace);
	        t.recycle();
        }
	}
	
	private synchronized void initView() {  
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;  
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }
	
	@Override  
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {

    }
      
    @Override  
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){

    }
      
    @Override  
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {

    }
    
    private DataSetObserver mDataObserver = new DataSetObserver() {
    	  
        @Override
        public void onChanged() {
            synchronized(AuroraHorizontalListView.this){
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override  
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }
          
    };

	@Override
	public ListAdapter getAdapter() {
		// TODO Auto-generated method stub
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		if(mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
	    mAdapter = adapter;
	    mAdapter.registerDataSetObserver(mDataObserver);
	    reset();
	}
	
    private synchronized void reset(){
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

	@Override
	public void setSelection(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if(params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mAdapter == null){
            return;
        }
        if(mDataChanged){
            int oldCurrentX = mCurrentX;
            initView();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }
        if(mScroller.computeScrollOffset()){
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
        }
        if(mNextX <= 0){
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        if(mNextX >= mMaxX) {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }
        int dx = mCurrentX - mNextX;
        //removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);
        mCurrentX = mNextX;
        if(!mScroller.isFinished()){
            post(new Runnable(){
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
	}
  
	private void fillList(final int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount()-1);
        if(child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);
          
        edge = 0;
        child = getChildAt(0);
        if(child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);
	}
  
	private void fillListRight(int rightEdge, final int dx) {
        while(rightEdge + dx < getWidth() + 500 && mRightViewIndex < mAdapter.getCount()) {
            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();
            if(mRightViewIndex == mAdapter.getCount()-1) {
                mMaxX = mCurrentX + rightEdge - getWidth();
                // Aurora xuyong 2015-10-27 added for aurora's new feature start
                if (mMaxX < 800) {
                    mMaxX += (mLeftSpace + mRightSpace);
                }
                // Aurora xuyong 2015-10-27 added for aurora's new feature end
            }
            if (mMaxX < 0) {
                mMaxX = 0;
            }
            mRightViewIndex++;
        }
	}
  
	private void fillListLeft(int leftEdge, final int dx) {
        while(leftEdge + dx > -500 && mLeftViewIndex >= 0) {
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
	}
  
	private void removeNonVisibleItems(final int dx) {
        View child = getChildAt(0);
        while(child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);  
        }
          
        child = getChildAt(getChildCount()-1);
        while(child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount()-1);
        }
	}
  
	private void positionItems(final int dx) {
        if(getChildCount() > 0){
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for(int i = 0 ; i < getChildCount(); i++){
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                if (i == 0) {
	                child.layout(left + mLeftSpace, 0, left + childWidth + mLeftSpace, child.getMeasuredHeight());
	                left += childWidth + mLeftSpace;
                } else if (i == getChildCount() - 1) {
                	child.layout(left, 0, left + childWidth + mRightSpace, child.getMeasuredHeight());
                    left += childWidth + mRightSpace;
                } else {
                	child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                	left += childWidth;
                }
            }
        }
	}
  
	public synchronized void scrollTo(int x) {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
	}
  
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        return handled;
	}
  
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
	                        float velocityY) {
        synchronized(AuroraHorizontalListView.this){
            mScroller.fling(mNextX, 0, (int)-velocityX, 0, 0, mMaxX, 0, 0);
        }
        requestLayout();
        return true;
	}  
  
	protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        // Aurora xuyong 2015-10-14 added for bug #16765 start
        mHasScroll = false;
        // Aurora xuyong 2015-10-14 added for bug #16765 end
        return true;
	}
	
	private boolean mHasScroll = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (null != mScroller && !mScroller.isFinished()) {
			return true;
		}
		if (mHasScroll && event.getAction() == MotionEvent.ACTION_UP) {
			mHasScroll = false;
			return true;
		}
		return super.onInterceptTouchEvent(event);
	}
  
	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {
	
        @Override
        public boolean onDown(MotionEvent e) {
            return AuroraHorizontalListView.this.onDown(e);
        }
	
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                        float velocityY) {
        	mHasScroll = false;
            return AuroraHorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }
	
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2,  
                        float distanceX, float distanceY) {
        	mHasScroll = true;
            synchronized(AuroraHorizontalListView.this){
                mNextX += (int)distanceX;
            }
            requestLayout();
            return true;
        }
	
        @Override  
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }
	          
        @Override  
        public void onLongPress(MotionEvent e) {
        }
	
	    private boolean isEventWithinView(MotionEvent e, View child) {
		    Rect viewRect = new Rect();
		    int[] childPosition = new int[2];
		    child.getLocationOnScreen(childPosition);
		    int left = childPosition[0];
		    int right = left + child.getWidth();
		    int top = childPosition[1];
		    int bottom = top + child.getHeight();
		    viewRect.set(left, top, right, bottom);
		    return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
	    }
	};
 
}
