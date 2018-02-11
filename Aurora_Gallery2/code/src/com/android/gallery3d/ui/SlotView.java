/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumDataLoader.ItemPositionInfo;
import com.android.gallery3d.common.Utils;


//Aurora <paul> <2014-02-27> for NEW_UI begin
import android.graphics.Bitmap;

import java.util.ArrayList;

import com.android.gallery3d.app.AlbumPage;
//Aurora <paul> <2014-02-27> for NEW_UI end


//Aurora <SQF> <2014-05-22>  for NEW_UI begin
import aurora.app.AuroraActivity;

import java.lang.Integer;

//Aurora <SQF> <2014-05-22>  for NEW_UI end
import com.android.gallery3d.fragmentapp.GridViewUtil;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.ui.PhotoView.Model;
import com.android.gallery3d.util.GalleryUtils;
//import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;



public class SlotView extends GLView {
    @SuppressWarnings("unused")
    private static final String TAG = "SlotView";

    private static final boolean WIDE = false;//Iuni <lory><2014-01-16> add for test
    private static final int INDEX_NONE = -1;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    public interface Listener {
        public void onDown(int index);
        public void onUp(boolean followedByLongPress);
        public void onSingleTapUp(int index);
        public void onLongTap(int index);
        public void onScrollPositionChanged(int position, int total);

        //Iuni <lory><2014-02-22> add begin
        public void onScrollPosition(int firstVisibleItem, int bottom, boolean bheader);
        public void onAuroraDown(int index, int noheadindex);
        public void onAuroraSingleTapUp(int index, int noheadindex);
        public void onAuroraLongTap(int index, int noheadindex);
        //Iuni <lory><2014-02-22> add end
    }

    public static class SimpleListener implements Listener {
        @Override public void onDown(int index) {}
        @Override public void onUp(boolean followedByLongPress) {}
        @Override public void onSingleTapUp(int index) {}
        @Override public void onLongTap(int index) {}
        @Override public void onScrollPositionChanged(int position, int total) {}
		
		//Iuni <lory><2014-02-22> add begin
        @Override public void onScrollPosition(int firstVisibleItem, int bottom, boolean bheader) {}
        @Override public void onAuroraDown(int index, int noheadindex){}
        @Override public void onAuroraSingleTapUp(int index, int noheadindex){}
        @Override public void onAuroraLongTap(int index, int noheadindex){}
        //Iuni <lory><2014-02-22> add end
    }

    public static interface SlotRenderer {
        public void prepareDrawing();
        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);
        public void onSlotSizeChanged(int width, int height);
        public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height);
		//Aurora <paul> <2014-02-27> for NEW_UI begin
		public void renderEmptyMessage(GLCanvas canvas, int w, int h);
		public int getPressedIndex();
		public Bitmap getBitmapByIndex(int slotIndex);
		//Aurora <paul> <2014-02-27> for NEW_UI end
        public int renderHeaderSlot(GLCanvas canvas, int index, int width, int height, boolean bDay);
        public int renderHeaderAnimation(GLCanvas canvas, int firstVisibleItem, int bottom, boolean bheader, boolean bDay);
    }

    private final GestureDetector mGestureDetector;
    private final ScrollerHelper mScroller;
    private final Paper mPaper = new Paper();

    private Listener mListener;
    private UserInteractionListener mUIListener;

    private boolean mMoreAnimation = false;
    private SlotAnimation mAnimation = null;

    private final Layout mLayout = new Layout();
    private int mStartIndex = INDEX_NONE;

    // whether the down action happened while the view is scrolling.
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_NONE;//OVERSCROLL_3D, paul disable
    private final SynchronizedHandler mHandler;
    private SlotRenderer mRenderer;
    private int[] mRequestRenderSlots = new int[16];

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    // to prevent allocating memory
    private final Rect mTempRect = new Rect();
    private final Rect mNextRect = new Rect();
	
    private ArrayList<Integer> mHeaderList = null;
    private HashMap<Integer, ItemInfo> mHashMap = null;
    private int mFirstVisibleItem = 0;
    private int mbottom = 0;
    private int m_tWidth = 0;
    private int m_tHeight = 0;
    
    //private boolean bUseArray = false;
    private final Rect mHeadTempRect = new Rect(0, 0 , 50, 50);
    
    private boolean USE_EdgeView = false;
    private EdgeView mEdgeView;//Iuni <lory><2014-02-20> add begin
    
    //Aurora <SQF> <2014-07-29>  for NEW_UI begin
    private boolean mSlotCountChanged = false;
    //Aurora <SQF> <2014-07-29>  for NEW_UI end
    
    //Aurora <SQF> <2014-04-08>  for NEW_UI begin
    private AbstractGalleryActivity mActivity;
    //Aurora <SQF> <2014-04-08>  for NEW_UI end
    
    //Aurora <SQF> <2014-6-3>  for NEW_UI begin
    private int mPrevScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mCurrentScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
    //Aurora <SQF> <2014-6-3>  for NEW_UI end
    
    //Aurora <SQF> <2014-09-18>  for NEW_UI begin
    private AlbumPage mAlbumPage;
    
	private boolean mIsAlbumSet = false;
	public void setAlbumSet(){
		mIsAlbumSet = true;
	}
	
    public void setAlbumPage(AlbumPage albumPage) {
    	mAlbumPage = albumPage;
    }

    //Aurora <SQF> <2014-09-18>  for NEW_UI end
    
    public SlotView(AbstractGalleryActivity activity, Spec spec) {
    	//Aurora <SQF> <2014-04-08>  for NEW_UI begin
    	mActivity = activity;
    	//Aurora <SQF> <2014-04-08>  for NEW_UI end
    	
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLRoot());
        setSlotSpec(spec);
        mAlbumDataLoader = null;
        
        if (!mIsAlbumSet) {

        	mFirstVisibleItem = 0;
        	//mHeaderList = new ArrayList<Integer>();
        	/*
        	if (bUseArray) {
        		mHashMap = new HashMap<Integer, SlotView.ItemInfo>();
			}
			*/
        	
        	mAuroraGestureListener = new MyAuroraGestureListener();
        	mAuroraGestureRecognizer = new GestureRecognizer(activity.getAndroidContext(), mAuroraGestureListener);
        	
        	if (USE_EdgeView) {
        		mEdgeView = new EdgeView(activity.getAndroidContext());
            	addComponent(mEdgeView);
			}
		}

    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        mRenderer = slotDrawer;
        if (mRenderer != null) {
            mRenderer.onSlotSizeChanged(mLayout.mSlotWidth, mLayout.mSlotHeight);
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void setCenterIndex(int index) {
        int slotCount = mLayout.mSlotCount;
        if (index < 0 || index >= slotCount) {
            return;
        }
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int position = WIDE
                ? (rect.left + rect.right - getWidth()) / 2
                : (rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }
    
    public void makeAuroraSlotVisible(int pos) {
		if(mIsAlbumSet) return;
        setScrollPosition(pos);
    }

    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int visibleBegin = WIDE ? mScrollX : mScrollY;
        int visibleLength = WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = WIDE ? rect.left : rect.top;
        int slotEnd = WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }

        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
    }

    @Override
    public void addComponent(GLView view) {
        throw new UnsupportedOperationException();
    }
	
	//paul add start
	private boolean mLayoutComplet = false;
    public void resume() {
        mLayoutComplet = true;
    }
	//paul add end
	
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize || mLayoutComplet) return;
		
        // Make sure we are still at a resonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setSize(r - l, b - t);
        //Aurora <SQF> <2014-05-23>  for NEW_UI begin
        if(mIsAlbumSet) {
       	 	makeSlotVisible(visibleIndex);
		} else {
        	makeSlotVisible(mLayout.getVisibleStart());
		}
        //Aurora <SQF> <2014-05-23>  for NEW_UI end
        //Log.i(TAG, "zll ----- onLayout xxxx l:"+(r - l)+",t:"+(b - t)+",visibleIndex:"+visibleIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }
    
    //Aurora <SQF> <2014-6-23>  for NEW_UI begin
    public void stopScroll() {
    	if(mScroller != null && ! mScroller.isFinished()) {
    		mScroller.forceFinished();
    	}
    }
    //Aurora <SQF> <2014-6-23>  for NEW_UI end

    private void updateScrollPosition(int position, boolean force) {
    	if (!mIsAlbumSet) {
    		if (position < 0) {
    			mScroller.forceFinished();
    			position = 0;
			} else if (position > mLayout.getScrollLimit()) {
				mScroller.forceFinished();
				position = mLayout.getScrollLimit();
			}
		}
    	
        if (!force && (WIDE ? position == mScrollX : position == mScrollY)) return;
        if (WIDE) {
            mScrollX = position;
        } else {
            mScrollY = position;
        }
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }
    
    private class PositionInfo{
    	protected int curRows;
    	protected int bottom;
    	protected boolean bheader;
    	
		public PositionInfo(int curRows, int bottom, boolean bheader) {
			super();
			this.curRows = curRows;
			this.bottom = bottom;
			this.bheader = bheader;
		}
    }
    
    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        mListener.onScrollPositionChanged(newPosition, limit);
    }

    public Rect getSlotRect(int slotIndex) {
        return mLayout.getSlotRect(slotIndex, new Rect());
    }

    @Override
    protected boolean onTouch(MotionEvent event) {

		if(!mIsAlbumSet && handleMulSelect(event)) return true;//paul add
		
        if (mUIListener != null) mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        
        if (!mIsAlbumSet) {
        	mAuroraGestureRecognizer.onTouchEvent(event);
		}

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL://paul add, fix bug
                mPaper.onRelease();
                invalidate();
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }

    private static int[] expandIntArray(int array[], int capacity) {
        while (array.length < capacity) {
            array = new int[array.length * 2];
        }
        return array;
    }

	private boolean mRenderEmptyMessage = false;
	public void setRenderEmptyMessage(boolean renderEmptyMessage){
		mRenderEmptyMessage = renderEmptyMessage;
	}

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);

        if (mRenderer == null) return;
        mRenderer.prepareDrawing();

        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        more |= mLayout.advanceAnimation(animTime);
        int oldX = mScrollX;
        updateScrollPosition(mScroller.getPosition(), false);
		/* paul del
        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit) v = -v;

                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }

        more |= paperActive;
		*/
        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
		
		if(mRenderEmptyMessage){
			mRenderer.renderEmptyMessage(canvas, mLayout.mWidth, mLayout.mHeight);
		}

        canvas.translate(-mScrollX, -mScrollY);

        int requestCount = 0;
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.mVisibleEnd - mLayout.mVisibleStart);
		if(!mIsAlbumSet){

			
        	for (int i = mLayout.mVisibleStart; i < mLayout.mVisibleEnd; ++i) {
                int r = renderItem(canvas, i, 0, false);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                //if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i; paul del
            }
		} else {
			for (int i = mLayout.mVisibleEnd - 1; i >= mLayout.mVisibleStart; --i) {
	            int r = renderItem(canvas, i, 0, false);
	            if ((r & RENDER_MORE_FRAME) != 0) more = true;
	            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
	        }
		}



		/* paul del, not used,
        for (int pass = 1; requestCount != 0; ++pass) {
            int newCount = 0;
            for (int i = 0; i < requestCount; ++i) {
                int r = renderItem(canvas,
                        requestedSlot[i], pass, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[newCount++] = i;
            }
            requestCount = newCount;
        }
		*/
		
        canvas.translate(mScrollX, mScrollY);

        if (more) invalidate();

        final UserInteractionListener listener = mUIListener;
        if (mMoreAnimation && !more && listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUserInteractionEnd();
                }
            });
        }
        mMoreAnimation = more;
		
		/*paul add*/
		if(null != mLongScale && mLongScale.snapBackEnd()){
			stopMakeScale();
		}
    }
    
    //Aurora <paul> <2014-02-27> for NEW_UI begin
	public Bitmap loadBitmap(int index){
		return mRenderer.getBitmapByIndex(index);
	}

	private int mUnrenderIndex = -1;
	public void setUnrenderIndex(int index){
		mUnrenderIndex = index;
	}
	//Aurora <paul> <2014-02-27> for NEW_UI end
	
	private class AuroraAnimationInfo{
		int		rows;
		int		bottom;
		boolean	bheader;
		
		public void set(int rows, int bottom, boolean bheader) {
			this.rows = rows;
			this.bottom = bottom;
			this.bheader = bheader;
		}
	}
	
	private AuroraAnimationInfo mAInfo = new AuroraAnimationInfo();
	private int lastFirstVisibleItem = -1;
	private boolean mIsMonth = false;
	private int renderHeaderItem(GLCanvas canvas, int position, boolean paperActive) {
		if (mAlbumDataLoader == null) {
			return 0;
		}
        
        if (mLayout != null) {
			mLayout.getAnimationInfo(position, mAInfo);
		}
        
        Rect rect = mHeadTempRect;
        {
        	int firstVisibleItem = mAInfo.rows;
        	int bottom = mAInfo.bottom;
        	boolean bheader = mAInfo.bheader;
        	
        	if (firstVisibleItem == 0) {
    			lastFirstVisibleItem = firstVisibleItem;
    			return 0;
    		}
        	
        	if (bheader) {
    			lastFirstVisibleItem = firstVisibleItem;
    			mRenderer.renderHeaderAnimation(canvas, -100, 0, true, mIsMonth);
    			return 0;
    		}
        	
        	canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        	
        	int nextSecPosition =  mAlbumDataLoader.getRowsByCurHeader(firstVisibleItem);
        	if (nextSecPosition == firstVisibleItem + 1) 
    		{
        		int headHeight = rect.bottom-rect.top;
    			int titleHeight = headHeight;
    			int tbottom = bottom;
    			if (bottom < titleHeight) {
    				float pushedDistance = bottom - titleHeight;
    				canvas.translate(rect.left, position+mLayout.getActionBarHeight()+pushedDistance, 0);
    				canvas.setAlpha(1f*bottom/titleHeight);
    			} else {
    				canvas.setAlpha(1f);
    				canvas.translate(rect.left, position+mLayout.getActionBarHeight(), 0);
    			}
    		} else {
    			canvas.translate(rect.left, position+mLayout.getActionBarHeight(), 0);
			}
    		
    		lastFirstVisibleItem = firstVisibleItem;
        }
        
        int result = mRenderer.renderHeaderAnimation(canvas, mAInfo.rows, mAInfo.bottom, mAInfo.bheader, mIsMonth);
        
        canvas.restore();
        return result;
    }
	
    private int renderItem(
            GLCanvas canvas, int index, int pass, boolean paperActive) {

		//Aurora <paul> <2014-02-27> for NEW_UI begin
		if(!mIsAlbumSet)
		if(index == mUnrenderIndex && -1 != mUnrenderIndex){
			return 0;
		}
		//Aurora <paul> <2014-02-27> for NEW_UI end
		canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);

		if (!mIsAlbumSet) {
			boolean bheader = false;
	        boolean bIn = false;
	        int more = 0;//Aurora <paul> <2014-04-10>

			Rect rect = mLayout.getSlotRect(index, mTempRect);

			int rw = rect.width();

			if (rw == m_tWidth) {
        		bheader = true;
			}
        	
        	if (paperActive && !bheader) {
                canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollY), 0);
            } else {
				canvas.translate(rect.left, rect.top, 0);
            }
			
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		if (mAnimation != null && (mAnimation.isActive())) {
                    mAnimation.apply(canvas, index, rect);
                }
        		
				//Aurora <paul> <2014-04-10> start
				if(null != mLongScale){
					mLongScale.play(canvas, index, rect, mNextRect, bheader);
					more = SlotView.RENDER_MORE_FRAME;
				}
				//Aurora <paul> <2014-04-10> end
			} else {
				if (mAnimation != null && mAnimation.isActive()) {
	                mAnimation.apply(canvas, index, rect);
	            }
			}
        	
        	int result = 0;
        	if (bheader) {
				result = mRenderer.renderHeaderSlot(canvas, index, rect.right - rect.left, rect.bottom - rect.top, mIsMonth);
    		} else {
				if(rw != 0){
    				result = mRenderer.renderSlot(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
				}
			}
        
        	canvas.restore();

        	return (result | more);//Aurora <paul> <2014-04-10> return result;
		} else {
			Rect rect = mLayout.getSlotRect(index, mTempRect);
	        if (paperActive) {
	            canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollX), 0);
	        } else {
	            canvas.translate(rect.left, rect.top, 0);
	        }
	        
	        if (mAnimation != null && mAnimation.isActive()) {
	            mAnimation.apply(canvas, index, rect);
	        }
	        
	        int result = mRenderer.renderSlot(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
	        canvas.restore();
	        return result;
		}
    }

    public static abstract class SlotAnimation extends Animation {
        protected float mProgress = 0;

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends SlotAnimation {
        private static final int RISING_DISTANCE = 10000;

        //lory add start
        public RisingAnimation() {
			setDuration(500);
		}
        //lory add end
        
        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        	//canvas.translate(0, target.centerY()*(1 - mProgress), RISING_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }

    }
    //Aurora <SQF> <2014-04-08>  for NEW_UI begin
    public static final int LANDSCAPE_CLOUMNS_NUM6 = 6;
    public static final int LANDSCAPE_CLOUMNS_NUM7 = 7;
    public static final int LANDSCAPE_CLOUMNS_NUM8 = 8;
    public static final int LANDSCAPE_CLOUMNS_NUM9 = 9;
    public static final int LANDSCAPE_CLOUMNS_NUM10 = 10;
    //Aurora <SQF> <2014-04-08>  for NEW_UI end
    
    public static final int CLOUMNS_NUM2 = 2;
    public static final int CLOUMNS_NUM3 = 3;
    public static final int CLOUMNS_NUM4 = 4;
    public static final int CLOUMNS_NUM5 = 5;
    public static final int CLOUMNS_NUM6 = 6;
	//paul for UI_20 start
	public static final int FROM_CLOUMNS = CLOUMNS_NUM4;
	public static final int TO_CLOUMNS = CLOUMNS_NUM6;
	public static final int COUNT_SCALE = 2;
	public static final int MONTH_CLOUMNS = CLOUMNS_NUM6;
	public static final int DEFAULT_CLOUMNS = CLOUMNS_NUM4;
	//paul for UI_20 end
    private int m_Clomuns = DEFAULT_CLOUMNS;
    private final Rect mLastTempRect = new Rect();
	
    private void resumeInitLayoutConfigEx(int num, int setPosition) {
		
		m_Clomuns = num;

    	if (mAlbumDataLoader != null) {
			mAlbumDataLoader.setSortMode(m_Clomuns);
			mIsMonth = (m_Clomuns == MONTH_CLOUMNS) ? true : false;
		}
    	int pos = mLayout.resumeInitLayoutParametersEx(m_Clomuns, setPosition);
    	makeAuroraSlotVisible(pos);

    }

    
    //Aurora <SQF> <2014-04-28>  for NEW_UI begin
    private boolean isMaxColumnNumReached() {
    	boolean isPortrait = GalleryUtils.isPortrait(mActivity.getAndroidContext());
    	return (m_Clomuns == TO_CLOUMNS && isPortrait) || (m_Clomuns ==  LANDSCAPE_CLOUMNS_NUM10 && ! isPortrait);
    }
    private boolean isMinColumnNumReached() {
    	boolean isPortrait = GalleryUtils.isPortrait(mActivity.getAndroidContext());
    	return (m_Clomuns == FROM_CLOUMNS && isPortrait) || (m_Clomuns ==  LANDSCAPE_CLOUMNS_NUM6 && !isPortrait);
    }
    //Aurora <SQF> <2014-04-28>  for NEW_UI end

 
	//Aurora <paul> <2014-04-10> start
	private final float STEP = 0.3f;
	private final float START = 1f;
	private LongScale mLongScale = null;
	private float mSteps[] = new float[5];
	
    //Aurora <SQF> <2014-08-19>  for NEW_UI begin
	private boolean mIsLongScale = false;
    //Aurora <SQF> <2014-08-19>  for NEW_UI end
	
	public void startMakeScale(int col) {
		mIsLongScale = true; //Aurora <SQF> <2014-08-19> add 
		mLongScale = new LongScale(col);
		mLongScale.startScale();
		
	}
	
	public void stopMakeScale() {
		if(null != mLongScale){
			mLongScale.mNeedSnapBack = false;
			mLongScale.endScale();
			if(!mLongScale.mNeedSnapBack){
				mLongScale = null;
				mIsLongScale = false;
			}
		}
	}

	public enum LONG_SCALE_DIRECTION {
		NO_CHANGE,
		ZOOM_OUT,
		ZOOM_IN,
	};
	
	private int getResumeInitScrollPosition(int columnNum, int visibleStart) {
		Rect r = mLayout.getLastSlotRectEx(visibleStart, columnNum, new Rect());
		int scrollPos = r.top - mLayout.mVerticalPadding.get() - mLayout.mHeaderHeight;
		return scrollPos;
	}
	
	public class LongScale {
		private static final float EACH_DELTA_SCALE = 0.5f;

		private static final int STEP = 2;
		private int mOriginal;
		private int mTarget;
		private int mTargetPos;
		private boolean mMinMode = false;
		private boolean mMaxMode = false;
		private LONG_SCALE_DIRECTION mDirection;
		private int mCurrentVisibleStart = 0;
		private boolean mNeedSnapBack = false;
		private float mProgress;
		private float mZoomRatio = -1f;
		public LongScale(int columns) {
			mOriginal = columns;
			mMaxMode = true;
			mMinMode = true;
			mNeedSnapBack = false;
			mDirection = LONG_SCALE_DIRECTION.NO_CHANGE;
			mProgress = 0f;
			mCurrentVisibleStart = mLayout.calculateRealVisibleStart(mLayout.mScrollPosition);
		}
		
		public void setScale(float scale) {
			if(mNeedSnapBack) return;

			mMaxMode = false;
			mMinMode = false;
			if(mDirection == LONG_SCALE_DIRECTION.ZOOM_OUT){
				if(scale < 1) return;
				mProgress = (scale - 1f) / EACH_DELTA_SCALE;
			} else if(mDirection == LONG_SCALE_DIRECTION.ZOOM_IN){
				if(scale > 1) return;
				mProgress = (1f - scale) / EACH_DELTA_SCALE;
			} else {
				if(scale > 1f){
					if(mOriginal == FROM_CLOUMNS){
						mMaxMode = true;
						return;
					} else{
						mDirection = LONG_SCALE_DIRECTION.ZOOM_OUT;
						mTarget = mOriginal - STEP;
					}
				} else if(scale < 1f){
					if(mOriginal == TO_CLOUMNS){
						mMinMode = true;
						return;
					} else{
						mDirection = LONG_SCALE_DIRECTION.ZOOM_IN;
						mTarget = mOriginal + STEP;
					}

				} else {
					return;
				}
				mZoomRatio = (float)mLayout.getWidthByNum(mTarget) / mLayout.getWidthByNum(mOriginal);
				mTargetPos = getResumeInitScrollPosition(mTarget, mCurrentVisibleStart);
			} 
			
			if(mProgress >= 1f){
				mProgress = 1f;
			}
		}

		public void startScale() {
			mLayout.updateMaxVisibleSlotRange();
		}
		
		public void endScale() {
			if(mProgress <= 0f){
				mLayout.updateVisibleSlotRange();
				return;
			}
			if(mProgress >= 1f){
				resumeInitLayoutConfigEx(mTarget, mTargetPos);
				return;
			}
			
			if(mProgress > 0.5f){
				mAnimation = new SnapBack(mProgress, 1f);
			} else {
				mAnimation = new SnapBack(mProgress, 0f);
			}
			mNeedSnapBack = true;
			
			mAnimation.start();
		}
		
		public boolean snapBackEnd(){
			if(!mNeedSnapBack) return false;
			return !mAnimation.isActive();
		}
		
		public void play(GLCanvas canvas, int slotIndex, Rect currentKnotRect, Rect nextRect, boolean isHeader) {
			if (mMinMode || mMaxMode || mZoomRatio < 0f) {
				return;
			}
			if(mNeedSnapBack){
				mProgress = ((SnapBack)mAnimation).getCurrentProgress();
			}
			
			mLayout.getLastSlotRectEx(slotIndex, mTarget, nextRect);

			
			//current ratio
			if(isHeader || nextRect.width() == 0){
				canvas.setAlpha(1 - mProgress);
			} else {
				float z = 1 + (mZoomRatio - 1) * mProgress;
				float offsetY = (nextRect.centerY() - mTargetPos) - 
							(currentKnotRect.centerY() - mLayout.mScrollPosition );
				float x = ((1 - z) / 2f) * currentKnotRect.width();
				float offsetX = nextRect.centerX() - currentKnotRect.centerX();
				canvas.translate(offsetX * mProgress + x, offsetY * mProgress + x);
				canvas.scale(z , z, 1.0f);
			}
		}


	}
	
	public class SnapBack extends SlotAnimation {		
		private float mStartProgress;
		private float mTargetProgress;
		private float mCurrentProgress;
		private float mOffset;
        public SnapBack(float startProgress, float targetProgress) {
        	super();
			setDuration(400);
			mStartProgress = startProgress;
			mCurrentProgress = mStartProgress;
			mTargetProgress = targetProgress;
			mOffset = mTargetProgress - mStartProgress;

		}
        public float getCurrentProgress(){
			return mCurrentProgress;
		}

		
        @Override
        protected void onCalculate(float progress) {
			mCurrentProgress = mStartProgress + mOffset * progress;
        }

		@Override
		public void apply(GLCanvas canvas, int slotIndex, Rect target){
		}

	}
	
    public class LayoutScaleMoveAnimation extends SlotAnimation{
    	private final int AURORA_RISING_DISTANCE2 = 400;
    	protected float mScaleFactor = 0.5f;
    	
		public LayoutScaleMoveAnimation() {
			setDuration(500);
		}

		@Override
		public void apply(GLCanvas canvas, int slotIndex, Rect target) {

			//paul for UI_20 start
			canvas.translate(0, 0, AURORA_RISING_DISTANCE2 * (1 - mProgress/2 - mScaleFactor));
			/*
			if (target.right - target.left != m_tWidth) {
				canvas.translate(0, 0, AURORA_RISING_DISTANCE2 * (1 - mProgress/2 - mScaleFactor));
			} else {
				canvas.translate(0, 0, AURORA_RISING_DISTANCE2 * (1 - mProgress/2 - mScaleFactor)*0.5f);
			}
			*/
			//paul for UI_20 end
		}
    }
    

    public static class ScatteringAnimation extends SlotAnimation {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }
	//paul add for UI_20 start
    public int getViewWidth(){
		return mLayout.getViewWidth();
	}
    public int getHeaderHeight(){
		return mLayout.getHeaderHeight();
	}
	//paul add for UI_20 end

    // This Spec class is used to specify the size of each slot in the SlotView.
    // There are two ways to do it:
    //
    // (1) Specify slotWidth and slotHeight: they specify the width and height
    //     of each slot. The number of rows and the gap between slots will be
    //     determined automatically.
    // (2) Specify rowsLand, rowsPort, and slotGap: they specify the number
    //     of rows in landscape/portrait mode and the gap between slots. The
    //     width and height of each slot is determined automatically.
    //
    // The initial value of -1 means they are not specified.
    public static class Spec {
        public int slotWidth = -1;
        public int slotHeight = -1;
        public int slotHeightAdditional = 0;

        public int rowsLand = -1;
        public int rowsPort = -1;
        public int slotGap = -1;
        
        public int leftlayout = -1;
        public int actionbarHeight = 0;
        
        public int mcloums = 3;
        
        //Aurora <SQF> <2014-03-31> for visual design begin
        public int headerHeight = 0; // equal to mHeaderHeight, but I put this value in aurora_dimension.xml
        public int headerExtraHeight = 0;
        public int slotViewRightMargin3Column = 0;
        public int slotViewRightMargin6Column = 0;
        //Aurora <SQF> <2014-03-31> for visual design end
    }
    
    private static class ItemInfo{
    	private int 	itemIndex;
    	private int 	itemHeader;
    	private int 	itemRows;
    	private Rect 	itemRect;
    	private boolean itemisDrawed;
    	private boolean itemisHeader;
    }
    
    public class Layout {

        private int mVisibleStart;
        private int mVisibleEnd;

        private int mSlotCount;
        private int mSlotWidth;
        private int mSlotHeight;
        private int mSlotGap;
        
        private MyHeaderIndex mHeaderIndex;
        private int m_leftlayoutwidth;
        private int m_actionbarHeight = 0;

        private Spec mSpec;

        public int mWidth = 1080;
        public int mHeight = 1920;

        private int mUnitCount;
        private int mContentLength;
        private int mScrollPosition;
        
        private static final int POSITION_FILLER = -0x01;
        private static final int POSITION_HEADER = -0x02;
        private static final int POSITION_HEADER_FILLER = -0x03;
        private static final int POSITION_VIEW_FILLER = -0x04;
        private int mHeaderHeight = 0; // originally int 60, SQF modified to 51, already put into aurora_dimension.xml,attribute tag "header_height"
        private int mHeaderNum = 0;
        private int []header_pos;

        private IntegerAnimation mVerticalPadding = new IntegerAnimation();
        private IntegerAnimation mHorizontalPadding = new IntegerAnimation();
        
        public void setSlotSpec(Spec spec) {
            mSpec = spec;
        }
		
        private void initHeaderArray() {      	
        	if (header_pos == null || mAlbumDataLoader == null) {
				return;
			}
        	for (int i = 0; i < header_pos.length; i++) {
        		if (i == 0) {
        			header_pos[i] = mVerticalPadding.get();
				} else if (i == 1) {
					header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mSpec.headerHeight;
				} else {
					header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mSpec.headerHeight +  mSpec.headerExtraHeight;
				}

			}
			return;
		}

        public boolean setSlotCount(int slotCount) {
            if (slotCount == mSlotCount) return false;
            //Aurora <SQF> <2014-07-29>  for NEW_UI begin
            mSlotCountChanged = true;
            //Aurora <SQF> <2014-07-29>  for NEW_UI end
            if (mSlotCount != 0) {
                mHorizontalPadding.setEnabled(true);
                mVerticalPadding.setEnabled(true);
            }
            //Aurora <SQF> <2014-07-18>  for NEW_UI begin
			if(!mIsAlbumSet){
	            if(mSlotCount == 0) {
	            	setVisibleRange(0, 0);
	            }
	            //Aurora <SQF> <2014-07-18>  for NEW_UI end
	            if (mAlbumDataLoader != null)  {
	            	mHeaderNum = 0;
	            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
	            	mSlotCount = slotCount;
	            	header_pos = null;
	            	if (mHeaderNum > 0) {
	            		header_pos = new int[mHeaderNum];
					}
				}
			} else {
				mSlotCount = slotCount;
			}
            int hPadding = mHorizontalPadding.getTarget();
            int vPadding = mVerticalPadding.getTarget();
            initLayoutParameters();
            
			if(!mIsAlbumSet){
				return true;
			}
			
            return vPadding != mVerticalPadding.getTarget()
                    || hPadding != mHorizontalPadding.getTarget();
            //Aurora <SQF> <2014-07-29>  for NEW_UI end
        }
        

        public Rect getSlotRect(int index, Rect rect) {
            int col, row;

            if (!mIsAlbumSet && mAlbumDataLoader != null) {
            	ItemPositionInfo mInfo = mAlbumDataLoader.getRowsAndCloumns(index, -1);
            	if (mInfo == null) {
            		rect.set(0, 0, 0, 0);
					return rect;
				}
            	
            	if (WIDE) {
            		col = index / mUnitCount;
	                row = index - col * mUnitCount;
	            } else {
	            	col = mInfo.cloumns;
	                row = mInfo.rows;
	            }
            	
            	int x,y;
            	if (mInfo.type == 0) {//date
					//paul for UI_20 start
					//y = mVerticalPadding.get() + (mInfo.header_index + 1) * mHeaderHeight + (row-mInfo.header_index) * (mSlotHeight + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					y = mVerticalPadding.get() 
						+ (mInfo.header_index) * (mHeaderHeight)
						+ (row - mInfo.header_index) * (mSlotHeight + mSlotGap);
					//paul for UI_20 end
					
					rect.set(0, y, mWidth, y + mHeaderHeight);
				} else {
					x = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap) + m_leftlayoutwidth;
					
					//paul for UI_20 start
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (mSlotHeight + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					y = mVerticalPadding.get() 
						+ (mInfo.header_index + 1) * (mHeaderHeight)
						+(row - mInfo.header_index - 1) * (mSlotHeight + mSlotGap);
					//paul for UI_20 end
					
					rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
				}
      		} else {
				if (WIDE) {
	                col = index / mUnitCount;
	                row = index - col * mUnitCount;
	            } else {
	            	row = index / mUnitCount;
	                col = index - row * mUnitCount;
	            }

	            int x = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap);
	            int y = mVerticalPadding.get() + row * (mSlotHeight + mSlotGap);         

				rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
			}


            return rect;
        }

        public int getSlotWidth() {
            return mSlotWidth;
        }

        public int getSlotHeight() {
            return mSlotHeight;
        }

        // Calculate
        // (1) mUnitCount: the number of slots we can fit into one column (or row).
        // (2) mContentLength: the width (or height) we need to display all the
        //     columns (rows).
        // (3) padding[]: the vertical and horizontal padding we need in order
        //     to put the slots towards to the center of the display.
        //
        // The "major" direction is the direction the user can scroll. The other
        // direction is the "minor" direction.
        //
        // The comments inside this method are the description when the major
        // directon is horizontal (X), and the minor directon is vertical (Y).
        private void initLayoutParameters(
                int majorLength, int minorLength,  /* The view width and height */
                int majorUnitSize, int minorUnitSize,  /* The slot width and height */
                int[] padding) {
        	int unitCount = 0;
        	if (!mIsAlbumSet) {
        		unitCount = m_Clomuns;
			} else {
				unitCount = (minorLength + mSlotGap) / (minorUnitSize + mSlotGap);
			}
        	
            if (unitCount == 0) unitCount = 1;
            mUnitCount = unitCount;
			
			if (!mIsAlbumSet) {
	            mSpec.mcloums = mUnitCount;
	            mSlotCount = mAlbumDataLoader.getSixSlotCount(mUnitCount);
	            mIsMonth = (m_Clomuns == MONTH_CLOUMNS&& GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
            }
		    // We put extra padding above and below the column.
            int availableUnits = Math.min(mUnitCount, mSlotCount);
            int usedMinorLength = 0;
            if (mIsAlbumSet) {
				usedMinorLength = availableUnits * minorUnitSize + (availableUnits - 1) * mSlotGap;
			}
            
            if(!mIsAlbumSet){
				 padding[0] = 0;
			} else {
            	padding[0] = (minorLength - usedMinorLength) / 2;
			}
            // Then calculate how many columns we need for all slots.
            int count = 0;
            if (!mIsAlbumSet && mAlbumDataLoader != null) {
            	mAlbumDataLoader.setCloums(mUnitCount);
            	initHeaderArray();
            	count = mAlbumDataLoader.getAlbumDLTotalRowSize(mUnitCount);
            	if (mHeaderNum > 0) {
            		mContentLength = header_pos[mHeaderNum-1] + mHeaderHeight + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(mHeaderNum-1)-1) * (minorUnitSize+mSlotGap) - header_pos[0] + 3 * mSlotGap + 51;
				} else {
					mContentLength = -mSlotGap;
				}
            	
			} else {
				count = ((mSlotCount + mUnitCount - 1) / mUnitCount);
				mContentLength = count * majorUnitSize + (count - 1) * mSlotGap;	
			}
            

            // If the content length is less then the screen width, put
            // extra padding in left and right.
            if (!mIsAlbumSet && mAlbumDataLoader != null) {
            	padding[1] = 0;//Math.max(0, m_actionbarHeight);
			} else {
				padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
			}
            
        }


        private void initLayoutParameters() {
            // Initialize mSlotWidth and mSlotHeight from mSpec
        	//Aurora <SQF> <2014-04-25>  for NEW_UI begin
			int defaultColumnNum = 0;
			if(!mIsAlbumSet){
	        	stopScroll();
	        	clearWidths();
	        	defaultColumnNum = calculateDefaultColumnNum();
        	}
            if (mSpec.slotWidth != -1) {
                mSlotGap = 0;
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
				if(!mIsAlbumSet){
	                mHeaderHeight = mSpec.headerHeight;
	                mSlotGap = mSpec.slotGap; //Iuni <lory><2014-01-16> add for test
	                
	                m_actionbarHeight = mSpec.actionbarHeight;
	                m_tWidth = mWidth;
	                m_tHeight = mHeaderHeight;

                	float w = (mWidth - (defaultColumnNum - 1) * mSlotGap) / (float)defaultColumnNum;
                	mSlotWidth = (int)(w + 0.5);
					mSpec.slotWidth = mSlotWidth;
					/*
					w = (w - mSlotWidth) * defaultColumnNum;
					
					if(w > 1){
						mSpec.leftlayout = (int)(w / 2);
					} else {
						mSpec.leftlayout = 0;
					}
					*/
					mSlotHeight = mSlotWidth;           	
                	m_Clomuns = defaultColumnNum;

					m_leftlayoutwidth = mSpec.leftlayout;
				}
            	//Aurora <SQF> <2014-04-08>  for NEW_UI end
            } else {
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
            }
            //Aurora <SQF> <2014-04-28>  for NEW_UI begin
			if(!mIsAlbumSet){
	            if (mAlbumDataLoader != null) {
	    			mAlbumDataLoader.setSortMode(m_Clomuns);
	    			mIsMonth = ((m_Clomuns == MONTH_CLOUMNS) && GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
    			
	    			mHeaderNum = 0;
	            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
	            	header_pos = null;
	            	if (mHeaderNum > 0) {
	            		header_pos = new int[mHeaderNum];
					}
	            	mSlotCount = mAlbumDataLoader.getSixSlotCount(m_Clomuns);
	    		}
	            //Aurora <SQF> <2014-04-28>  for NEW_UI end

	            mHeadTempRect.set(0, 0, mWidth, mSpec.headerHeight);
			}
            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }

            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                //mVerticalPadding.startAnimateTo(padding[1]);
                //mHorizontalPadding.startAnimateTo(padding[0]);
            }
            updateVisibleSlotRange();
        }

		//Aurora <paul> <2014-04-10> start		
		private int mWidhts[] = new int[7];
		private boolean mGotWidth = false;
		
		private void clearWidths() {
			mGotWidth = false;
			for(int i=0; i<7; i++) {
				mWidhts[i] = 0;
			}
		}
		
		//paul for UI_20 start
		public int getHeaderHeight(){
			return mSpec.headerHeight;
		}
		public int getViewWidth(){
			return mWidth;
		}

		//paul for UI_20 end
		
		private int getWidthByNum(int num){
			if(num > TO_CLOUMNS){
				return mWidth;
			}
			
			if(mGotWidth){
				return mWidhts[num];
			}
			mGotWidth = true;
			for(int i = FROM_CLOUMNS;i <= TO_CLOUMNS; ++i){
				mWidhts[i] = (int)((float)(mWidth - m_leftlayoutwidth - (i - 1) * mSpec.slotGap) / i + 0.5f);
			}
			return mWidhts[num];
		}
		public Rect getLastSlotRectEx(int index, int num, Rect rect) {
				int col, row;
				if(num == -1) num = m_Clomuns;//SQF ADDED ON 2014.8.30
				int height = mLayout.getWidthByNum(num);
				ItemPositionInfo mInfo;
				if(num == MONTH_CLOUMNS){
					index = mAlbumDataLoader.mapMonthIndex(index);
					if(-1 == index){
						rect.set(0, 0, 0, 0);
						return rect;

					}
					mInfo = mAlbumDataLoader.getItemInfo(index, num, true);
					
				}else {
					index = mAlbumDataLoader.mapDayIndex(index);
					if(-1 == index){
						rect.set(0, 0, 0, 0);
						return rect;

					}
					mInfo = mAlbumDataLoader.getItemInfo(index, num, false);
				}
				if (mInfo == null) {
					rect.set(0, 0, 0, 0);
					return rect;
				}
				if (WIDE) {
					col = index / mUnitCount;
					row = index - col * mUnitCount;
				} else {
					col = mInfo.cloumns;
					row = mInfo.rows;
				}
				
				int x,y;
				if (mInfo.type == 0) {
					x = 0;
					//paul for UI_20 start
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					
					y = mVerticalPadding.get() 
						+ (mInfo.header_index) * (mHeaderHeight)
						+ (row - mInfo.header_index) * (height + mSlotGap);

					rect.set(x, y, mWidth, y + mHeaderHeight);
					//paul for UI_20 end
				} else {
					x = mHorizontalPadding.get() + col * (height + mSlotGap)+m_leftlayoutwidth;
					//paul for UI_20 start
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					y = mVerticalPadding.get() 
						+ (mInfo.header_index + 1) * (mHeaderHeight)
						+(row - mInfo.header_index - 1) * (height + mSlotGap);
					//paul for UI_20 end
					rect.set(x, y, x + height, y + height);
				}
				return rect;
		}
		
		private int resumeInitLayoutParametersEx(int num, int setPosition) {
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	mHeaderNum = 0;
            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
            	header_pos = null;
            	if (mHeaderNum > 0) {
            		header_pos = new int[mHeaderNum];
				}
			}
        	
        	if (mSpec.slotWidth != -1) {	
        		int tmp_width = mWidth;        		
    			mSlotCount = mAlbumDataLoader.getSixSlotCount(num);
				float w = (float)(tmp_width - (num - 1) * mSpec.slotGap) / num;
				mSpec.slotWidth = (int)(w + 0.5);
				/*
				w = (w - mSpec.slotWidth) * num + 0.001f;
				if(w >= 2){
					mSpec.leftlayout = (int)(w / 2);
				} else {
					mSpec.leftlayout = 0;
				}
				*/
				//paul for UI_20 end
	
        		mSpec.slotHeight = mSpec.slotWidth;       		
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
                mSlotGap = mSpec.slotGap; //Iuni <lory><2014-01-16> add for test
                m_leftlayoutwidth = mSpec.leftlayout;
                m_actionbarHeight = mSpec.actionbarHeight;
                
                m_tWidth = mWidth;
                m_tHeight = mHeaderHeight;
            } else {
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
            }

            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }

            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                //mVerticalPadding.startAnimateTo(padding[1]);
                //mHorizontalPadding.startAnimateTo(padding[0]);
            }
            
            
            mScrollPosition = setPosition;
            
            updateVisibleSlotRange();
                        
			return setPosition;
		}
		//
        
        //Aurora <SQF> <2014-6-3>  for NEW_UI begin
        private void recordScreenOrientation() {
        	mPrevScreenOrientation = mCurrentScreenOrientation;
        	mCurrentScreenOrientation = mActivity.getResources().getConfiguration().orientation;
        }
        
        private int calculateDefaultColumnNum() {
        	recordScreenOrientation();
        	if(mPrevScreenOrientation == mCurrentScreenOrientation) {
        		return m_Clomuns;
        	}
        	int defaultColumnNum = m_Clomuns;
        	if(mPrevScreenOrientation == Configuration.ORIENTATION_PORTRAIT && mCurrentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
        		defaultColumnNum = m_Clomuns + 4;
        	} else if(mPrevScreenOrientation == Configuration.ORIENTATION_LANDSCAPE && mCurrentScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
        		defaultColumnNum = m_Clomuns - 4;
        	}
        	return defaultColumnNum;
        }
        //Aurora <SQF> <2014-6-3>  for NEW_UI end

        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
            initLayoutParameters();
        }
        
        public int getAuroraWidth() {
			return mWidth;
		}

        public int getAuroraHeight() {
			return mHeight;
		}
        
        private int calculateCurHeaderByScrollPosition(int position) {
    		int topMargin = header_pos[0];
    		int headerLen = header_pos.length;
    		int midHeaderIndex = ((headerLen - 1)>>1);
    		int curHeader = 0;
    		
    		boolean found = false;
    		//first search the first and the last
    		if(position >= header_pos[0] - topMargin && position < header_pos[0] - topMargin + getContentLengthOfHeader(0)) {
    			curHeader = 0;
    			found = true;
    		} else if (position >= header_pos[headerLen - 1] - topMargin) {
    			curHeader = headerLen - 1;
    			found = true;
    		}
    		//binary search the header_pos
    		int tmpStart = 0;
    		int tmpEnd = headerLen - 1;
			
    		while( ! found) {
    			int midStartPos = header_pos[midHeaderIndex] - topMargin;
    			int midEndPos = header_pos[midHeaderIndex] - topMargin + getContentLengthOfHeader(midHeaderIndex);
    			if( position >= midStartPos && position <= midEndPos ) {
    				curHeader = midHeaderIndex;
    				found = true;
    			} else if(position < midStartPos){
    				tmpEnd = midHeaderIndex;
    				midHeaderIndex = ((tmpStart + midHeaderIndex)>>1);
    			} else if(position > midEndPos) {
    				tmpStart = midHeaderIndex;
    				midHeaderIndex =((midHeaderIndex + tmpEnd)>>1);
    			} else {
    				Log.i("SQF_LOG", "error while 11111111");
    				Utils.assertTrue(false);
    			}
    		}
    		return curHeader;
        }
        
        private int calculateCurrentRowInHeader(int position, int curHeader) {
        	//now we have already know which header we are. we need to determine which row we are.
        	int topMargin = header_pos[0];
    		int howManyRows = mAlbumDataLoader.getAlbumDLCurRowsByHeader(curHeader) - 1;       
    		int curHeaderOffset = position - (header_pos[curHeader] - topMargin);
    		int midRowIndex = ((howManyRows - 1)>>1);
    		int curRow = 0;
    		boolean found = false;

    		int firstRowStartPos = 0;
    		int firstRowEndPos = mHeaderHeight + mSlotHeight + mSlotGap;
    		int lastRowStartPos = mHeaderHeight + (mSlotHeight + mSlotGap) * (howManyRows - 1);

			if(curHeaderOffset < firstRowStartPos) {//paul add
    			curRow = 0;// touch on the header
    			found = true;
 			} else if(curHeaderOffset >= firstRowStartPos && curHeaderOffset < firstRowEndPos) {
    			curRow = 0;
    			found = true;
    		} else if(curHeaderOffset >= lastRowStartPos) {
    			curRow = howManyRows - 1;
    			found = true;
    		}
    		//binary search the rows
    		int tmpStart = 0;
    		int tmpEnd = howManyRows - 1;
    		while( ! found) {

    			int midRowStartPos = mHeaderHeight + (mSlotHeight + mSlotGap) * (midRowIndex);
    			int midRowEndPos = midRowStartPos + (mSlotHeight + mSlotGap);

    			if( curHeaderOffset >= midRowStartPos && curHeaderOffset <= midRowEndPos) {
    				curRow = midRowIndex;
    				found = true;
    			} else if(curHeaderOffset < midRowStartPos) {
    				tmpEnd = midRowIndex;
    				midRowIndex = ((tmpStart + midRowIndex)>>1);

    			} else if(curHeaderOffset > midRowEndPos) {
    				tmpStart = midRowIndex;
    				midRowIndex = ((midRowIndex + tmpEnd)>>1);
    			}
    		}
    		return curRow;
        }
        
        
        private int calculateRealVisibleStart(int position) {
        	if(header_pos == null || mAlbumDataLoader == null) return 0;
        	int curHeader = calculateCurHeaderByScrollPosition(position);
    		int curRow = calculateCurrentRowInHeader(position, curHeader);
    		
    		int totalPhotosBeforeCurHeader = 1;//paul add ,current header count 1
    		int totalPhotosOfCurrentOffset = 0;
    		for(int i = 0; i < curHeader; i++) {
    			totalPhotosBeforeCurHeader += mAlbumDataLoader.getAlbumDLCountForHeader(i);
    		}
    		totalPhotosOfCurrentOffset = curRow * m_Clomuns;
    		int start = totalPhotosBeforeCurHeader + totalPhotosOfCurrentOffset + curHeader;
    		return start;
        }

        public void updateVisibleSlotRange() {
            int position = mScrollPosition;

            if (WIDE) {
                int startCol = position / (mSlotWidth + mSlotGap);
                int start = Math.max(0, mUnitCount * startCol);
                int endCol = (position + mWidth + mSlotWidth + mSlotGap - 1) /
                        (mSlotWidth + mSlotGap);
                int end = Math.min(mSlotCount, mUnitCount * endCol);
                setVisibleRange(start, end);
            } else {
            	if (!mIsAlbumSet && mAlbumDataLoader != null) {
              		if(header_pos == null || header_pos.length == 0) {
            			return;
            		}
            		int start = Math.max(0, calculateRealVisibleStart(position) - 1);//-1 for header
					int end = Math.min(mSlotCount,start + (mHeight / (mSlotHeight + mSlotGap) + 2) * mUnitCount + 1);//paul modify
					
            		setVisibleRange(start, end);
            	} else {
	                int startRow = position / (mSlotHeight + mSlotGap);
	                int start = Math.max(0, mUnitCount * startRow);
	                int endRow = (position + mHeight + mSlotHeight + mSlotGap - 1) /
	                        (mSlotHeight + mSlotGap);
	                int end = Math.min(mSlotCount, mUnitCount * endRow);
	                setVisibleRange(start, end);
				}
            }
        }
		
		/*paul add*/
        public void updateMaxVisibleSlotRange() {
    		int start = Math.max(0, mVisibleStart - TO_CLOUMNS * 2 + 1);
			int end = Math.min(mSlotCount, mVisibleStart + (mHeight / (getWidthByNum(TO_CLOUMNS) + mSlotGap) + 2) * TO_CLOUMNS + 1);
    		setVisibleRange(start, end);
        }
		

        /*
         * get header
         * @param header index
         */
        public int getContentLengthOfHeader(int header) {
        	int contentLength = mHeaderHeight + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(header) - 1) * (mSlotHeight + mSlotGap);
        	if(header > 0) contentLength += mSpec.headerExtraHeight;
        	return contentLength;
        }
        
        
        //Aurora <SQF> <2014-05-19>  for NEW_UI end
        
        public void setScrollPosition(int position) {
            if (mScrollPosition == position) return;
            mScrollPosition = position;
            updateVisibleSlotRange();
        }

        private void setVisibleRange(int start, int end) {
        	if (!mIsAlbumSet){
	            if (start == mVisibleStart && end == mVisibleEnd && ! mSlotCountChanged) return;//Aurora <SQF> <2014-07-29> add " && !mSlotCountChanged"
	             //Aurora <SQF> <2014-07-29>  for NEW_UI begin
	            mSlotCountChanged = false;
	            //Aurora <SQF> <2014-07-29>  for NEW_UI end
			} else {
				 if (start == mVisibleStart && end == mVisibleEnd) return;
			}
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }
            if (mRenderer != null) {
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }
        
        public int getActionBarHeight() {
			return m_actionbarHeight;
		}
        


        public int getVisibleStart() {
            return mVisibleStart;
        }
        
        public int getVisibleEnd() {
            return mVisibleEnd;
        }

        private class MyHeaderIndex {
    		int header;
    	}
        
        private boolean isHeaderSlot(int ypos) {
        	boolean flag = false;
        	
        	if (header_pos != null) {
        		if (ypos <= header_pos[0]+mHeaderHeight) {
					return true;
				}
        		
        		if (ypos >= header_pos[header_pos.length-1] &&
        			ypos <= header_pos[header_pos.length-1] + mHeaderHeight) {
       				return true;
				}
        		
				for (int i = 0; i < header_pos.length; i++) {
					if ((i > 0) &&
						(ypos < header_pos[i]) &&
						(ypos >= header_pos[i-1]) &&
						(ypos <= header_pos[i-1] + mHeaderHeight)) {
						return true;
					}
				}
			}
        	
        	return flag;
		}
        
        public int[] getAuroraSlotIndexByPosition(float x, float y) {
        	int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            absoluteX -= (mHorizontalPadding.get() + m_leftlayoutwidth);
            absoluteY -= mVerticalPadding.get();
			return mAlbumDataLoader.getNoheaderIndexByPos(absoluteX, absoluteY, mSlotWidth + mSlotGap, mHeaderHeight, mUnitCount);
		}
        
        public int getSlotIndexByPosition(float x, float y) {
            int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            

			absoluteX -= mHorizontalPadding.get();
			absoluteY -= mVerticalPadding.get();
			if (absoluteX < 0 || absoluteY < 0) {
                return INDEX_NONE;
            }


            int columnIdx = absoluteX / (mSlotWidth + mSlotGap);
            int rowIdx = absoluteY / (mSlotHeight + mSlotGap);

            if (!WIDE && columnIdx >= mUnitCount) {
                return INDEX_NONE;
            }

            if (WIDE && rowIdx >= mUnitCount) {
                return INDEX_NONE;
            }

            if (absoluteX % (mSlotWidth + mSlotGap) >= mSlotWidth) {
                return INDEX_NONE;
            }

            if (absoluteY % (mSlotHeight + mSlotGap) >= mSlotHeight) {
                return INDEX_NONE;
            }

            int index = WIDE
                    ? (columnIdx * mUnitCount + rowIdx)
                    : (rowIdx * mUnitCount + columnIdx);

            return index >= mSlotCount ? INDEX_NONE : index;
        }

        public int getScrollLimit() {
          	//Aurora <SQF> <2014-05-23>  for NEW_UI begin
			int limit;
			if(!mIsAlbumSet){
	        	int subtract = (header_pos != null && header_pos.length > 0) ? header_pos[0] : 0;  
	        	limit = WIDE ? mContentLength - (mWidth - subtract): mContentLength - (mHeight - subtract);
			} else {
				limit = WIDE ? mContentLength - mWidth : mContentLength - mHeight;
			}
            //Aurora <SQF> <2014-05-23>  for NEW_UI end
             return limit <= 0 ? 0 : limit;
        }

        public boolean advanceAnimation(long animTime) {
            // use '|' to make sure both sides will be executed
            return mVerticalPadding.calculate(animTime) | mHorizontalPadding.calculate(animTime);
        }
        
        public int isHideHeader(int y) {
			int header = 0;
			
			if (y == 0) {
				return header;
			}
        	
			int newposition = y + m_actionbarHeight;
			if (header_pos != null) {
				if (header_pos.length >= 2) {
					if (newposition > header_pos[header_pos.length-1]+mHeaderHeight+mSpec.headerExtraHeight) {
						return (header_pos.length-1);
					} else if (newposition < header_pos[1]+mHeaderHeight) {
						return 0;
					} else {
						for (int i = 1; i < header_pos.length; i++) {
		    				if (newposition < header_pos[i]+mHeaderHeight+mSpec.headerExtraHeight) {
		    					return (i-1);
							}
		    			}
					}
				} else {
					return 0;
				}
				
			}
			
			return header;
		}
        
        public AuroraAnimationInfo getAnimationInfo(int position, AuroraAnimationInfo infos){
        	int rows = 0;
        	int header = 0;
        	int headrows = 0;
        	boolean bheader = false;
        	int bottom = 0;
        	boolean blast = false;
        	
        	int newposition = 0;
        	
        	newposition = position + m_actionbarHeight;
        	
        	if (header_pos != null && mAlbumDataLoader != null) {
        		if (newposition < header_pos[0]+mHeaderHeight) {
        			infos.set(0, bottom, true);
        			return infos;
				} else if (newposition >= header_pos[header_pos.length-1]) {
        			header = header_pos.length-1;
        			blast = true;
				} else {
					for (int i = 0; i < header_pos.length; i++) {
	    				if (newposition < header_pos[i]) {
	    					header = i>0?i-1:i;
	    					break;
						}
	    			}
				}
        		
        		if (header_pos.length > 1) {
        			if (newposition > header_pos[1]) {
        				rows = (position-(header+1)*mHeaderHeight-header*mSpec.headerExtraHeight)/(mSlotHeight + mSlotGap) + header+1;
					} else {
						rows = (position-mHeaderHeight)/(mSlotHeight + mSlotGap) + header+1;
					}
				} else {
					rows = (position-(header+1)*mHeaderHeight)/(mSlotHeight + mSlotGap) + header+1;
				}
        		
        		if (!blast) {
        			if (header > 0) {
						if (newposition >= header_pos[header]-mSlotGap && 
        				    newposition <= header_pos[header] + mHeaderHeight+mSpec.headerExtraHeight) {
							bheader = true;
						}
					} else {
						if (newposition >= header_pos[header+1]-mSlotGap && 
                		    newposition <= header_pos[header+1] + mHeaderHeight) {
							bheader = true;
						}
					}
        			
        			bottom = header_pos[header+1] - newposition - 1;
				} else {
					if (newposition >= header_pos[header]-mSlotGap && 
						newposition <= header_pos[header] + mHeaderHeight+mSpec.headerExtraHeight) {
		        		bheader = true;
					}
				}
        		
        		//Log.i(TAG, "zll ---- rrrr getAnimationInfo rows:"+rows+",bottom:"+bottom+",newposition:"+newposition+",bheader:"+bheader+",header:"+header+",header_pos[header]:"+header_pos[header]);
        		//headrows = mAlbumDataLoader.getRowsByCurHeader(header);
			}
        	
        	infos.set(rows, bottom, bheader);
			return infos;
        }
        
        public int pos2rows(int position){
        	int rows = 0;
        	int header = 0;
        	int headrows = 0;
        	boolean bheader = false;
        	int bottom = 0;
        	boolean blast = false;
        	
        	int newposition = 0;
        	/*if (position > m_actionbarHeight+mHeaderHeight) {
        		newposition = position + m_actionbarHeight+mSlotGap;
			} else {
				newposition = position;
			}*/
        	
        	newposition = position + m_actionbarHeight+mSlotGap;
        	
        	if (header_pos != null && mAlbumDataLoader != null) {
        		if (newposition <= header_pos[0]+mHeaderHeight) {
        			mListener.onScrollPosition(0, bottom, true);
        			return 0;
				} else if (newposition >= header_pos[header_pos.length-1]) {
        			header = header_pos.length-1;
        			blast = true;
				} else {
					for (int i = 0; i < header_pos.length; i++) {
	    				if (newposition < header_pos[i]) {
	    					header = i>0?i-1:i;
	    					break;
						}
	    			}
				}
        		
        		rows = (position-(header+1)*mHeaderHeight)/(mSlotHeight + mSlotGap) + header+1;
        		if (!blast) {
        			if (header > 0) {
						if (newposition >= header_pos[header] && 
        				    newposition <= header_pos[header] + mHeaderHeight) {
							bheader = true;
						}
					} else {
						if (newposition >= header_pos[header+1] && 
                		    newposition <= header_pos[header+1] + mHeaderHeight) {
							bheader = true;
						}
					}
        			
        			bottom = header_pos[header+1] - newposition - 1;
				} else {
					if (newposition >= header_pos[header] && 
						newposition <= header_pos[header] + mHeaderHeight) {
		        		bheader = true;
					}
				}
        		//Log.i(TAG, "zll ---- rrrr pos2rows rows:"+rows+",bottom:"+bottom+",newposition:"+newposition+",bheader:"+bheader+",header:"+header+",header_pos:"+header_pos[header+1]);
        		//headrows = mAlbumDataLoader.getRowsByCurHeader(header);
			}
        	mListener.onScrollPosition(rows, bottom, bheader);
        	return rows;
        }
    }

    //Aurora <SQF> <2014-6-19>  for NEW_UI begin
    private boolean motionInActionBarArea(MotionEvent e) {
    	/*
    	if(mLayout == null || mLayout.mSpec == null) return false;
    	if(e.getY() <= mLayout.mSpec.actionbarHeight) return true;
    	*/
    	return false;
    }
    //Aurora <SQF> <2014-6-19>  for NEW_UI end
    
    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        // We call the listener's onDown() when our onShowPress() is called and
        // call the listener's onUp() when we receive any further event.
        @Override
        public void onShowPress(MotionEvent e) {
            //Aurora <SQF> <2014-09-18>  for NEW_UI begin
            if(!mIsAlbumSet && mAlbumPage.isInSelectionMode()) return;
            //Aurora <SQF> <2014-09-18>  for NEW_UI end
            GLRoot root = getGLRoot();
            root.lockRenderThread();
            try {
                if (isDown) return;
                
                if (!mIsAlbumSet) {
                	int[] index = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
                    if (index[1] != INDEX_NONE) {
                        isDown = true;
                        mListener.onAuroraDown(index[1], index[0]);
                    }
                    
				} else {
					int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
	                if (index != INDEX_NONE) {
	                    isDown = true;
	                    mListener.onDown(index);
	                }
				}
                
            } finally {
                root.unlockRenderThread();
            }
        }

        private void cancelDown(boolean byLongPress) {
            if (!isDown) return;
            isDown = false;
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1,
                MotionEvent e2, float velocityX, float velocityY) {
                
        	if(!mIsAlbumSet) {
	            //Aurora <SQF> <2014-09-26>  for NEW_UI begin
	            if(mIsLongScale) {
	            	return false;
	            }
	            //Aurora <SQF> <2014-09-26>  for NEW_UI end
            }

            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            float velocity = WIDE ? velocityX : velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null) mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1,
                MotionEvent e2, float distanceX, float distanceY) {
        	if(!mIsAlbumSet) {
	            //Aurora <SQF> <2014-09-26>  for NEW_UI begin
	            if(mIsLongScale) {
	            	return false;
	            }
	            //Aurora <SQF> <2014-09-26>  for NEW_UI end
            }
            cancelDown(false);
            float distance = WIDE ? distanceX : distanceY;
            int overDistance = mScroller.startScroll(
                    Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            cancelDown(false);
            //Aurora <SQF> <2014-6-3>  for bug4817 begin
        	//if(!mIsAlbumSet && motionInActionBarArea(e)) return true;
        	//Aurora <SQF> <2014-6-3>  for bug4817 end
            if (mDownInScrolling) return true;
            if (!mIsAlbumSet) {
            	int[] indexs = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
                if (indexs[1] != INDEX_NONE) mListener.onAuroraSingleTapUp(indexs[1], indexs[0]);
			} else {
				int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
	            if (index != INDEX_NONE) mListener.onSingleTapUp(index);
			}
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        	if(!mIsAlbumSet) {
	        	//Aurora <SQF> <2014-09-18>  for NEW_UI begin
	            if(mAlbumPage.isInSelectionMode()) return;
	            //Aurora <SQF> <2014-09-18>  for NEW_UI end
	            //Aurora <SQF> <2014-6-19>  for NEW_UI begin
	        	//if(motionInActionBarArea(e)) return;
	            //Aurora <SQF> <2014-6-19>  for NEW_UI end
			}
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
            	if (!mIsAlbumSet) {
                    int[] indexs = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
                     if (indexs[1] != INDEX_NONE) mListener.onAuroraLongTap(indexs[1], indexs[0]);
				} else {
					int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
	                if (index != INDEX_NONE) mListener.onLongTap(index);
				}
            	
            } finally {
                unlockRendering();
            }
        }
    }

    public void setStartIndex(int index) {
        mStartIndex = index;
    }

    // Return true if the layout parameters have been changed
    public boolean setSlotCount(int slotCount) {
    	
    	boolean changed = false;
    	if (!mIsAlbumSet && mAlbumDataLoader != null) {
    		changed = mLayout.setSlotCount(slotCount+mAlbumDataLoader.getAlbumDLNumHeaders());
		} else {
			changed = mLayout.setSlotCount(slotCount);
		}

        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(WIDE ? mScrollX : mScrollY);
        return changed;
    }
    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public Rect getSlotRect(int slotIndex, GLView rootPane) {
        // Get slot rectangle relative to this root pane.
        Rect offset = new Rect();
        rootPane.getBoundsOf(this, offset);
        Rect r = getSlotRect(slotIndex);
        r.offset(offset.left - getScrollX(),
                offset.top - getScrollY());
        return r;
    }

    private static class IntegerAnimation extends Animation {
        private int mTarget;
        private int mCurrent = 0;
        private int mFrom = 0;
        private boolean mEnabled = false;

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!mEnabled) {
                mTarget = mCurrent = target;
                return;
            }
            if (target == mTarget) return;

            mFrom = mCurrent;
            mTarget = target;
            setDuration(180);
            start();
        }

        public int get() {
            return mCurrent;
        }

        public int getTarget() {
            return mTarget;
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = Math.round(mFrom + progress * (mTarget - mFrom));
            if (progress == 1f) mEnabled = false;
        }
    }
    //Aurora <SQF> <2014-05-15>  for NEW_UI begin
    private boolean mIsUserActionInProgress = false;
    public boolean isSlotViewUserActionNotFinished() {
    	return mIsLongScale | 
    			mIsUserActionInProgress | 
    			(mAnimation != null && mAnimation.isActive()) | 
    			(mScroller != null && ! mScroller.isFinished());
    }
    //Aurora <SQF> <2014-05-15>  for NEW_UI end
    
    
	//Aurora <paul> <2014-02-27> for NEW_UI begin
	public boolean isAnimPlaying(){
		if(null == mAnimation) return false;
		return (mAnimation.isActive() | mIsLongScale);
	}
	
	public boolean isLongScale() {
		return mIsLongScale;
	}

	private boolean mInMulSelectMode = false;
	private float mStartX = 0, mStartY = 0, mEndX = 0, mEndY = 0;
	private int mSelStart = -1;
	private int mSelEnd= -1;
	private boolean mToSelect = true;
    private boolean handleMulSelect(MotionEvent event) {
		if(!mAlbumPage.isInSelectionMode()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
				mStartX = event.getX();
				mStartY = event.getY();
				mEndX = mStartX;
				mEndY = mStartY;
				int[] indexs = mLayout.getAuroraSlotIndexByPosition(mStartX, mStartY);
				mSelStart = indexs[0];
				mSelEnd = mSelStart;
				break;

			case MotionEvent.ACTION_MOVE:
				if(mSelStart != -1 && !mInMulSelectMode){
					if((float)mLayout.mSlotWidth <= Math.abs(event.getX() - mStartX) && 
						(float)mLayout.mSlotWidth > Math.abs(event.getY() - mStartY) * 2) {
						mToSelect = !mAlbumPage.isItemSelected(mSelStart);
						mInMulSelectMode = true;
					}
				}
				if(mInMulSelectMode){
					float v = (float)(mLayout.mSlotWidth >> 1);
					if(Math.abs(event.getX() - mEndX) > v || Math.abs(event.getY() - mEndY) > v) {
						float x = event.getX();
						float y =  event.getY();
						int[] index = mLayout.getAuroraSlotIndexByPosition(x, y);
						int end = index[0];
						if(end < 0) return true;
						if(end != mSelEnd){
							if(end > mSelStart) {
								if(end > mSelEnd) {
									if(mSelStart > mSelEnd){
										mAlbumPage.doMulSelect(mSelEnd, mSelStart - 1, !mToSelect);
										mAlbumPage.doMulSelect(mSelStart, end, mToSelect);
									} else {
										mAlbumPage.doMulSelect(mSelEnd, end, mToSelect);
									}
								} else {
									mAlbumPage.doMulSelect(end + 1, mSelEnd, !mToSelect);
								}
							} else if(end < mSelStart) {
								if(end > mSelEnd) {
									mAlbumPage.doMulSelect(mSelEnd, end - 1, !mToSelect);
								} else {
									if(mSelStart < mSelEnd){
										mAlbumPage.doMulSelect(mSelStart + 1, mSelEnd, !mToSelect);
										mAlbumPage.doMulSelect(end, mSelStart, mToSelect);
									}else{
										mAlbumPage.doMulSelect(end, mSelEnd, mToSelect);
									}
								}
							} else {
								if(end > mSelEnd){
									mAlbumPage.doMulSelect(mSelEnd, end, !mToSelect);
								} else {
									mAlbumPage.doMulSelect(end, mSelEnd, !mToSelect);
								}
							}
							mEndX = x;
							mEndY = y;
							mSelEnd = end;
							invalidate();
						}
					}
					return true;
				}
				break;

            case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mSelStart = -1;
				if(mInMulSelectMode){
					mInMulSelectMode = false;
					invalidate();
				}
                break;

        }

		return false;
    }

	//Aurora <paul> <2014-02-27> for NEW_UI end
    
    //Iuni <lory><2014-01-21> add begin -------------------------------------------
    private AlbumDataLoader mAlbumDataLoader;
    public void setAlbumDataLoader(AlbumDataLoader loader) {
    	mAlbumDataLoader = loader;
		return;
	}
    
    private MyAuroraGestureListener mAuroraGestureListener = null;
    private GestureRecognizer mAuroraGestureRecognizer = null;
    
    private class MyAuroraGestureListener implements GestureRecognizer.Listener{
    	private float mAccScale;
    	private float tScale;

		@Override
		public boolean onSingleTapUp(float x, float y) {
			//Log.i("SQF_LOG", "SlotView::MyAuroraGestureListener::onSingleTapUp x:" + x + " y:" + y);
			return false;
		}

		@Override
		public boolean onDoubleTap(float x, float y) {
			return false;
		}

		@Override
		public boolean onScroll(float dx, float dy, float totalX, float totalY) {
			return false;
		}

		@Override
		public boolean onFling(float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onScaleBegin(float focusX, float focusY) {
			if( ! mScroller.isFinished()) {
				mScroller.forceFinished();
			}
			
			if(null != mAlbumPage && !mAlbumPage.isInSelectionMode() && !mIsLongScale){
				startMakeScale(m_Clomuns);//Aurora <paul> <2014-04-10>
			}
			return true;
		}

		@Override
		public boolean onScale(float focusX, float focusY, float scale) {
			//Log.i(TAG, "zll --- onScale 1 scale:"+scale+",focusX:"+focusX+",focusY:"+focusY);
			//Log.i("SQF_LOG", "MyAuroraGestureListener::onScale focusX:" + focusX + " focusY:" + focusY + " scale:" + scale);
			if (Float.isNaN(scale) || Float.isInfinite(scale)) return false;
			//Aurora <paul> <2014-04-10> start
			if(null != mLongScale){
				mLongScale.setScale(scale);
			}
			
			/*if (mAnimation2 != null) {
				tScale = Math.min(1-mAccScale, 0.5f);
				mAnimation2.auroraCalculate(tScale);
			}*/
			return false;
			//Aurora <paul> <2014-04-10> end
		}

		@Override
		public void onScaleEnd() {
			//Log.i(TAG, "zll --- onScaleEnd");
			//Log.i("SQF_LOG", "MyAuroraGestureListener::onScaleEnd");
			stopMakeScale();//Aurora <paul> <2014-04-10>
		}

		@Override
		public void onDown(float x, float y) {
			mIsUserActionInProgress = true;
			//Log.i("SQF_LOG", "SlotView::MyAuroraGestureListener::onDown x:" + x + " y:" + y);
		}

		@Override
		public void onUp() {
			mIsUserActionInProgress = false;
			//Log.i("SQF_LOG", "SlotView::MyAuroraGestureListener::onUp");
		}
    }
    
    //Iuni <lory><2014-01-21> add end ----------------------------------------------
}
