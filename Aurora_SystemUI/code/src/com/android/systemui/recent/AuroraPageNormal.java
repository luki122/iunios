package com.android.systemui.recent;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.R;

import android.content.res.Configuration;

public class AuroraPageNormal extends ViewGroup implements AuroraPage{
	private ArrayList<View> mList = new ArrayList<View>();

	private int mCellWidth;
	private int mCellHeight;
	private int mPaddingLeft;
	private int mPaddingTop;

	private int mCellWidthLand;
	private int mCellHeightLand;
	private int mPaddingLeftLand;
	private int mPaddingTopLand;

	private onViewDeletedCallback mOnViewDeletedCallback;

    // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Add line up animation to single delete action.
    private static RecentsPanelView mRecentsPanelView = null;
    private boolean mSingleDelete = false;
    private int mAnimDelay;
    // Aurora <Felix.Duan> <2014-9-28> <END> Add line up animation to single delete action.

	public AuroraPageNormal(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		initPaddings();

	}

	private Context mContext;
	private boolean isOrientationPortrait(){
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

	public void setViewDeletedCallback(onViewDeletedCallback cb){
		mOnViewDeletedCallback = cb;
	}
	
	@Override
	public void addView(View v){
		mList.add(v);
		super.addView(v);
	}
	
	@Override
	public void removeView(View v){
		mList.remove(v);
        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
        // Indicate this is a swipe up, need to perform single deletion animation.
        if (v instanceof AuroraRecentlItemView) {
            ((AuroraRecentlItemView)v).setSingleSwipe();
            // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Add line up animation to single delete action.
            // Animate when child deleted
            mSingleDelete = true;
            // Aurora <Felix.Duan> <2014-9-28> <END> Add line up animation to single delete action.
        }
		if(null != mOnViewDeletedCallback){
			mOnViewDeletedCallback.onViewDeleted(v);
		}
		super.removeView(v);
        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel
    }

	public void tempRemoveView(View v){
		mList.remove(v);
		super.removeView(v);
	}
	
	@Override
	public int getPageChildCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public View getChildOnPageAt(int i) {
		// TODO Auto-generated method stub
		return mList.get(i);
	}

	@Override
	public void removeAllViewsOnPage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeViewOnPageAt(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int indexOfChildOnPage(View v) {
		// TODO Auto-generated method stub
		for(int i=0;i<mList.size();++i){
			if(mList.get(i).equals(v)){
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public View getChildOnPageAtPoint(float x,float y) {
		// TODO Auto-generated method stub
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int left = child.getLeft();
            int r = child.getRight();
            int t = child.getTop();
            int b = child.getBottom();
            if(child.getLeft() < x && child.getRight() > x && child.getTop() < y && child.getBottom() > y){
            	return child;
            	
            }
        }
		
		return null;
	}
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
    
    public void measureChild(View child) {
        int cellWidth = mCellWidth;
        int cellHeight = mCellHeight;

		if(!isOrientationPortrait()){
        	cellWidth = mCellWidthLand;
        	cellHeight = mCellHeightLand;
		}
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(cellHeight,MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }
    
	private void initPaddings(){
		mCellWidth = mContext.getResources().getDimensionPixelSize(R.dimen.recent_cell_width);
		mCellHeight = mContext.getResources().getDimensionPixelSize(R.dimen.recent_cell_height);
		mPaddingLeft = mContext.getResources().getDimensionPixelSize(R.dimen.recents_iconview_left_padding);
		mPaddingTop = mContext.getResources().getDimensionPixelSize(R.dimen.recents_iconview_top_padding);		

		mCellWidthLand = mContext.getResources().getDimensionPixelSize(R.dimen.recent_cell_width_land);
		mCellHeightLand = mContext.getResources().getDimensionPixelSize(R.dimen.recent_cell_height_land);
		mPaddingLeftLand = mContext.getResources().getDimensionPixelSize(R.dimen.recents_iconview_left_padding_land);
		mPaddingTopLand = mContext.getResources().getDimensionPixelSize(R.dimen.recents_iconview_top_padding_land);	
	}


	/*
		Layout for screen orientation Land
		screen land start at o(mPaddingLeft, parentHeight-mPaddingTop-mCellHeight)
		so very child n's 
		left keep mPaddingLeft
		top parentHeight-mPaddingTop-n*mCellHeight
		right is left+mCellWidth
		bottom keep top+mCellHeight
		
		---------  -- 
		|       | 
		|   4   |   mCellHeight
		|       |
		---------  --
		|       |
		|   3   |
		|       | 
		---------
		|       |
		|   2   |
		|       | 
		o--------
		|       |
		|   1   |
		|       | 
		---------
		|       |
	    mCellwidth
	*/
	
	private void landLayout(int l, int t, int r, int b){
        int count = getChildCount();
        int leftOffset = mPaddingLeftLand;
		int topOffset = getHeight() - mPaddingTopLand;
		l += mPaddingLeftLand;

        // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Add line up animation to single delete action.
        // Animation based on current layout & interact logic
        // Prepare for animating layout
        int[] pos = new int[2];
        mAnimDelay = 0;
        boolean animate = shouldAnimateLayout();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            // Get view`s former position
            if (animate)
                child.getLocationOnScreen(pos);
            	child.layout(leftOffset, topOffset - mCellHeightLand, mCellWidthLand + leftOffset, topOffset);
            // Animate view to current position
            if (animate && pos[1] != (topOffset - mCellHeight)) {
                buildLineUpAnimation(child, pos[1], (topOffset - mCellHeightLand), mAnimDelay).start();
            }
            topOffset -= mCellHeightLand;
        }
        // Aurora <Felix.Duan> <2014-9-28> <END> Add line up animation to single delete action.
	}
	

	/*
		Layout for screen orientation portrait
		screen land start at o (mPaddingLeft, mPaddingTop)
		so very child n's 
		left is mPaddingLeft+(n-1)*mCellWidth
		top keep mPaddingTop
		right is left+mCellWidth
		bottom keep top+mCellHeight
		
		o--------------------------------  --
		|       |       |       |       |
		|   1   |   2   |   3   |   4   |	mCellHeight
		|       |       |       |       |
		---------------------------------  --
		|       |
	    mCellwidth
	*/

	private void porlayout(int l, int t, int r, int b){
		int count = getChildCount();
        int leftOffset = mPaddingLeft;
		int top = mPaddingTop;

        // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Add line up animation to single delete action.
        // Animation based on current layout & interact logic
        // Prepare for animating layout
        int[] pos = new int[2];
        mAnimDelay = 0;
        boolean animate = shouldAnimateLayout();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            // Get view`s former position
            if (animate)
                child.getLocationOnScreen(pos);
            	child.layout(leftOffset, top, mCellWidth + leftOffset, top + mCellHeight);
            // Animate view to current position
            if (animate && pos[0] != leftOffset) {
                buildLineUpAnimation(child, pos[0], leftOffset, mAnimDelay).start();
            }

            leftOffset += mCellWidth;
        }
	}
    

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
android.util.Log.e("AuroraPageNormal", "AuroraPageNormal => changed is " + changed);
		if(isOrientationPortrait()){
			porlayout(l, t, r, b);		
		}else{
			landLayout(l, t, r, b);	
		}
	}
	
	public interface onViewDeletedCallback{
		public void onViewDeleted(View v);
	}

    // Aurora <Felix.Duan> <2014-9-28> <BEGIN> Add line up animation to single delete action.
    /**
     * Should play line up animation when:
     *     RecentsPanelView is visible
     *     and
     *     child view single deleted
     */
    private boolean shouldAnimateLayout() {
        if (mRecentsPanelView == null)
            mRecentsPanelView = getRecentsPanelView(this);

        if (mRecentsPanelView == null)
            return false; // Where is your parent?

        if (mRecentsPanelView.isShowing() && mSingleDelete) {
            // Reset flag
            mSingleDelete = false;
            return true;
        } else {
            return false;
        }
    }

    private RecentsPanelView getRecentsPanelView(View v) {
        ViewGroup parent = (ViewGroup) v.getParent();
        if (parent instanceof RecentsPanelView)
            return (RecentsPanelView) parent;
        else if (parent == null)
            return null;
        else
            return getRecentsPanelView(parent);
    }

    /**
     * Build line up animation of remaining icons when single delete on icon
     *
     * Visual effect confirmed by LiuHeng
     */
    private AnimatorSet buildLineUpAnimation(View target, int start, int end, int delay) {
        // Tweak the rightmost icon
		String animOrientation = "translationX";
		if(isOrientationPortrait()){
			animOrientation = "translationX";
	        if (start < end) {
	            start = end + mCellWidth;
	        }			
		} else {
			animOrientation = "translationY";
			if (start > end) {
            	start = end - mCellHeight;
        	}
		}

        ObjectAnimator holdAnim = ObjectAnimator.ofFloat(target, animOrientation, start-end, start-end);
        holdAnim.setDuration(delay);
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(target, animOrientation, start-end, 0);
        moveAnim.setDuration(300);
        moveAnim.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(holdAnim, moveAnim);
        mAnimDelay += 25; // Delay between icons
        return set;
    }
    // Aurora <Felix.Duan> <2014-9-28> <END> Add line up animation to single delete action.
}
