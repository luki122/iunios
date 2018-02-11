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
		public int getPressedIndex();
		public Bitmap getBitmapByIndex(int slotIndex);
		//Aurora <paul> <2014-02-27> for NEW_UI end
        public int renderHeaderSlot(GLCanvas canvas, int index, String tile,int width, int height, boolean bDay);
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
    private int mOverscrollEffect = OVERSCROLL_3D;
    private final SynchronizedHandler mHandler;
    private SlotRenderer mRenderer;
    private int[] mRequestRenderSlots = new int[16];

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    // to prevent allocating memory
    private final Rect mTempRect = new Rect();
    
    private ArrayList<ItemInfo> mSlotList = null;
    private boolean mListFlag = false;
    private ArrayList<Integer> mHeaderList = null;
    private HashMap<Integer, ItemInfo> mHashMap = null;
    private int mFirstVisibleItem = 0;
    private int mbottom = 0;
    private int m_tWidth = 0;
    private int m_tHeight = 0;
    
    private int mSlotindex = 0;//Iuni <lory><2014-03-12> add begin
    private boolean bUseArray = false;
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
        //Log.i("SQF_LOG", "SlotView::SlotView will call setSlotSpec---");
        setSlotSpec(spec);
        mAlbumDataLoader = null;
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	mSlotList = new ArrayList<SlotView.ItemInfo>();
        	mListFlag = false;
        	mFirstVisibleItem = 0;
        	//mHeaderList = new ArrayList<Integer>();
        	if (bUseArray) {
        		mHashMap = new HashMap<Integer, SlotView.ItemInfo>();
			}
        	
        	mSlotindex = 0;
        	
        	isAuroraAnimActive = false;
        	mAuroraGestureListener = new MyAuroraGestureListener();
        	mAuroraGestureRecognizer = new GestureRecognizer(activity.getAndroidContext(), mAuroraGestureListener);
        	
        	if (USE_EdgeView) {
        		mEdgeView = new EdgeView(activity.getAndroidContext());
            	addComponent(mEdgeView);
			}
		}
		//Aurora <paul> <2014-04-11> start
        //SQF ANNOATED ON 2014.09.18
        /*
		float setp = START + STEP;
		for(int i = 0;i <= 4;++i){
			mSteps[i] = setp;
			setp += STEP;
		}
		*/
		//Aurora <paul> <2014-04-11> end
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
    	//Log.i("SQF_LOG", "SlotView::setSlotRenderer getVisibleStart():" + getVisibleStart() + " getVisibleEnd():" +  getVisibleEnd());
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
        int position = pos;
        setScrollPosition(position);
    }

    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int visibleBegin = WIDE ? mScrollX : mScrollY;
        int visibleLength = WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = WIDE ? rect.left : rect.top;
        int slotEnd = WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        //Log.i(TAG, "zll --- ooooo 0.1 visibleLength:"+visibleLength+",slotEnd:"+slotEnd+",slotBegin:"+slotBegin+",visibleBegin:"+visibleBegin+",visibleEnd:"+visibleEnd);
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }

        //Log.i(TAG, "zll --- ooooo 0.21 position:"+position+",mScrollY:"+mScrollY);
        //position = mLayout.getScrollPosition();
        
        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        //Log.i("SQF_LOG", "SlotView::setScrollPosition position:" + position + ", mScrollY:" + mScrollY);
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
    }

    @Override
    public void addComponent(GLView view) {
    	if (USE_EdgeView) {
			return;
		}
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize) return;

        if (MySelfBuildConfig.USEGALLERY3D_FLAG && USE_EdgeView) {
        	mEdgeView.layout(0, 0, r - l, b - t);
		}
        // Make sure we are still at a resonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setSize(r - l, b - t);
        //Aurora <SQF> <2014-05-23>  for NEW_UI begin
        //ORIGINALLY:
        //makeSlotVisible(visibleIndex);
        //SQF MODIFIED TO:
        makeSlotVisible(mLayout.getVisibleStart());
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
    	//Log.i("SQF_LOG", "SlotView::updateScrollPosition position:" + position + " force:" + force);
    	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
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
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//mLayout.pos2rows(newPosition);
		}
    }

    public Rect getSlotRect(int slotIndex) {
        return mLayout.getSlotRect(slotIndex, new Rect());
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
    	//Log.i(TAG, "zll ---- onTouch 1");
        if (mUIListener != null) mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	mAuroraGestureRecognizer.onTouchEvent(event);
		}
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL://paul add
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG && USE_EdgeView) {
                	mEdgeView.onRelease();
    			}
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
                    if (MySelfBuildConfig.USEGALLERY3D_FLAG && USE_EdgeView) {
                    	if (v < 0) {
                            mEdgeView.onAbsorb(-(int)v, EdgeView.TOP);
                        } else {
                        	mEdgeView.onAbsorb((int)v, EdgeView.BOTTOM);
                        }
					}
                }
            }
            paperActive = mPaper.advanceAnimation();
        }

        more |= paperActive;
        if (mAnimation != null) {
        	more |= mAnimation.calculate(animTime);
        }
        canvas.translate(-mScrollX, -mScrollY);

        int requestCount = 0;
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.mVisibleEnd - mLayout.mVisibleStart);

        if (mSlotList == null) {
        	mSlotList = new ArrayList<SlotView.ItemInfo>();
        	mListFlag = false;
		} else {
			mSlotList.clear();
			mListFlag = false;
		}
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	for (int i = mLayout.mVisibleStart; i <= mLayout.mVisibleEnd-1; i++) {
            	mListFlag = true;
            	//Log.i("SQF_LOG", "SlotView::render mLayout.mVisibleStart:" + mLayout.mVisibleStart + ", mLayout.mVisibleEnd:" + mLayout.mVisibleEnd);
                int r = renderItem(canvas, i, 0, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
            }
		} else {
			for (int i = mLayout.mVisibleEnd - 1; i >= mLayout.mVisibleStart; --i) {
	        	//Log.i(TAG, "zll ---- rrrrrrrrrrrr 2 0.02 i:"+i);
	        	mListFlag = true;
	            int r = renderItem(canvas, i, 0, paperActive);
	            if ((r & RENDER_MORE_FRAME) != 0) more = true;
	            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
	        }
		}

        mListFlag = false;
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

        canvas.translate(mScrollX, mScrollY);
        
        //Log.i(TAG, "zll ----- mScrollX:"+mScrollX+",mScrollY:"+mScrollY+",mScroller.getPosition():"+mScroller.getPosition());
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG)
        {
        	canvas.translate(-mScrollX, -mScrollY);
            renderHeaderItem(canvas, mScrollY, paperActive);
            canvas.translate(mScrollX, mScrollY);
        }

        if (more) invalidate();

        if (MySelfBuildConfig.USEGALLERY3D_FLAG && USE_EdgeView) {
        	renderChild(canvas, mEdgeView);
		}
        
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

        //code below only take 3 ms 
        if(mAnimation != null && (mAnimation instanceof SnapBack) && mAnimation.isActive()) {
        	SnapBack snapBack = (SnapBack)mAnimation;
        	if(snapBack.getProgress() == 1.0f) {
        		//Log.i("SQF_LOG", "render -------------------------------------> resumeInitLayoutConfigEx: " + snapBack.getResumeDirection() + " setPos :" + snapBack.getAfterResumeScrollPos()); 
        		resumeInitLayoutConfigEx(snapBack.getResumeDirection(), snapBack.getAfterResumeScrollPos());
        		snapBack.forceStop();
        		//snapBack.setAuroraActive(false);
        		mAnimation = null;
        	}
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
	private boolean mbDay = false;
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
    			mRenderer.renderHeaderAnimation(canvas, -100, 0, true, mbDay);
    			return 0;
    		}
        	
        	canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        	
        	int nextSecPosition =  mAlbumDataLoader.getRowsByCurHeader(firstVisibleItem);
        	//Log.i(TAG, "zll ---- renderHeaderItem nextSecPosition:"+nextSecPosition);
        	if (nextSecPosition == firstVisibleItem + 1) 
    		{
        		int headHeight = rect.bottom-rect.top;
    			int titleHeight = headHeight;
    			int tbottom = bottom;
    			if (bottom < titleHeight) {
    				float pushedDistance = bottom - titleHeight;
    				//Log.i(TAG, "zll --- renderHeaderAnimation 3 bottom:"+bottom+",titleHeight:"+titleHeight+",position:"+position+",pushedDistance:"+pushedDistance);
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
        
        int result = mRenderer.renderHeaderAnimation(canvas, mAInfo.rows, mAInfo.bottom, mAInfo.bheader, mbDay);
        
        canvas.restore();
        return result;
    }
	
    private int renderItem(
            GLCanvas canvas, int index, int pass, boolean paperActive) {
    	//Log.i(TAG, "zll ---- renderItem mUnrenderIndex:"+mUnrenderIndex+",index:"+index);
		//Aurora <paul> <2014-02-27> for NEW_UI begin
		if(index == mUnrenderIndex && -1 != mUnrenderIndex){
			return 0;
		}
		//Aurora <paul> <2014-02-27> for NEW_UI end
		canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
		//Log.i(TAG, "zll --- mHashMap xxxx 1 paperActive:"+paperActive+",index:"+index);
		if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
			Rect rect;
			boolean bheader = false;
	        boolean bIn = false;
	        int more = 0;//Aurora <paul> <2014-04-10>
			if (bUseArray && mHashMap != null) {
				ItemInfo tInfo = mHashMap.get(index);
        		if (tInfo != null) {
    				rect = tInfo.itemRect;
    				bIn = true;
    				//Log.i("SQF_LOG", "zll --- mHashMap xxxx 1 size:"+mHashMap.size()+",index:"+index);
    			} else {
    				rect = mLayout.getSlotRect(index, mTempRect);
    				//Log.i("SQF_LOG", "zll --- mHashMap  yyyy 2 size:"+mHashMap.size()+",index:"+index);
    			}
			} else {
				rect = mLayout.getSlotRect(index, mTempRect);
			}

			//Log.i("SQF_LOG", "zll --- index: " + index + " current column:" + m_Clomuns +  " rect:" + rect + " mScrollPosition:" + mLayout.mScrollPosition + 
			//			" mScrollY:" + mScrollY +  
			//			" mLayout.mVisibleStart:" + mLayout.mVisibleStart);
			if (rect.right - rect.left == m_tWidth && rect.bottom - rect.top == m_tHeight) {
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
					mLongScale.play(canvas, index, rect);
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
        		
        		/*if (mLayout != null) {
        			mLayout.getAnimationInfo(mScrollY, mAInfo);
        		} m_HeaderHeight = mHeaderHeight+mSpec.headerExtraHeight;
        		Log.i(TAG, "zll ----- xxxxx bheader:"+mAInfo.bheader+",rows:"+mAInfo.rows+",index:"+index);*/
        		//Log.i(TAG, "zll ---- index:"+index+",mScrollY:"+mScrollY+",top:"+rect.top+",bottom:"+rect.bottom+",m_tHeight:"+m_tHeight);
        		if (mScrollY > m_tHeight && mAlbumDataLoader != null) {
        			int tth = mLayout.isHideHeader(mScrollY);
            		int tth2 = mAlbumDataLoader.getDaterIndex(index);
            		if (tth != tth2) {
            			//Log.i("SQF_LOG", "SlotView::renderItem 111111--> (tth != tth2) index:" + index + " rect:" + rect.toString());
            			result = mRenderer.renderHeaderSlot(canvas, index, null, rect.right - rect.left, rect.bottom - rect.top, mbDay);
    				} else {
    					//Log.i("SQF_LOG", "SlotView::renderItem 222222--> (tth == tth2) index:" + index + " rect:" + rect.toString());
    					result = mRenderer.renderHeaderSlot(canvas, -2, null, rect.right - rect.left, rect.bottom - rect.top, mbDay);
					}
				}else {
					//Log.i("SQF_LOG", "SlotView::renderItem 333333--> index:" + index + " rect:" + rect.toString());
					result = mRenderer.renderHeaderSlot(canvas, index, null, rect.right - rect.left, rect.bottom - rect.top, mbDay);
				}
        		
            	//result = mRenderer.renderHeaderSlot(canvas, index, null, rect.right - rect.left, rect.bottom - rect.top);
    		} else {
    			result = mRenderer.renderSlot(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
    		}
        
        	canvas.restore();
        	
        	if (mSlotList != null && mListFlag) {
            	ItemInfo mInfos = new ItemInfo();
            	mInfos.itemIndex = index;
            	mInfos.itemRect = new Rect(rect.left, rect.top, rect.right, rect.bottom);
            	
            	//Log.i(TAG, "zll ---- rrrrrrrrrrrr 0.02 index:"+index+",rect.left:"+rect.left+",rect.right:"+rect.right+",rect.top:"+rect.top);
            	//Log.i(TAG, "zll ---- rrrrrrrrrrrr 0.03 index:"+index+",mInfos.itemRect.left:"+mInfos.itemRect.left+",mInfos.itemRect.right:"+mInfos.itemRect.right+",mInfos.itemRect.top:"+mInfos.itemRect.top);
            	mSlotList.add(mInfos);
            	if (!bIn && bUseArray) {
            		mHashMap.put(index, mInfos);
    			}
    		}
        	
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
        	//Log.i("SQF_LOG", "RisingAnimation::apply slotIndex:" + slotIndex+", centerX:" + target.centerX() + ", centerY():" + target.centerY());
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
    
    private int m_Clomuns = CLOUMNS_NUM3;
    private int m_LastClomuns = CLOUMNS_NUM3;
    private final Rect mLastTempRect = new Rect();

    private void resumeInitLayoutConfigEx(boolean flag, int setPosition) {
    	
    	//long time = System.currentTimeMillis();
        mLayout.mScrollPosition = setPosition;
    	//Aurora <SQF> <2014-04-04>  for NEW_UI begin
    	if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
    	//Aurora <SQF> <2014-04-04>  for NEW_UI end
			if (flag) {// 6->2
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == CLOUMNS_NUM2) return;
				m_Clomuns -= 1;
				if(m_Clomuns < CLOUMNS_NUM2) m_Clomuns = CLOUMNS_NUM2;
			} else {// 2->6
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == CLOUMNS_NUM6) return;
				m_Clomuns += 1;
				if(m_Clomuns > CLOUMNS_NUM6) m_Clomuns = CLOUMNS_NUM6;
			}
			//Log.i("SQF_LOG", "PPPP resumeInitLayoutConfigEx m_LastClomuns " + m_LastClomuns + " set to:  m_Clomuns:" + m_Clomuns);
    	} 
    	//Aurora <SQF> <2014-04-04>  for NEW_UI begin 
    	else {
    		//Log.i("SQF_LOG", "m_LastClomuns: " + m_LastClomuns + " m_Clomuns:" + m_Clomuns);
    		if (flag) {// 10->8->6
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == LANDSCAPE_CLOUMNS_NUM6) return;
				m_Clomuns -= 1;
				if(m_Clomuns < LANDSCAPE_CLOUMNS_NUM6) m_Clomuns = LANDSCAPE_CLOUMNS_NUM6;
			} else {// 6->8->10
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == LANDSCAPE_CLOUMNS_NUM10) return;
				m_Clomuns += 1;
				if(m_Clomuns > LANDSCAPE_CLOUMNS_NUM10) m_Clomuns = LANDSCAPE_CLOUMNS_NUM10;
			}
    		//Log.i("SQF_LOG", "LLLL resumeInitLayoutConfigEx m_LastClomuns: " + m_LastClomuns + " set to m_Clomuns:" + m_Clomuns);
    	}
    	//Aurora <SQF> <2014-04-04>  for NEW_UI end   	
    	if (mAlbumDataLoader != null) {
			mAlbumDataLoader.setSortMode(m_Clomuns);
			mbDay = ((m_Clomuns == CLOUMNS_NUM6) && GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
		}
    	int visibleIndex = mLayout.getVisibleStart();
    	
    	int pos = mLayout.resumeInitLayoutParametersEx(m_Clomuns, visibleIndex, setPosition);
    	//Log.i("SQF_LOG", "resumeInitLayoutConfigEx visibleIndex:" + visibleIndex + " after call resumeInitLayoutParameters --> pos:" + pos + " setPosition:" + setPosition);
    	makeAuroraSlotVisible(pos);
    	
    	//time = System.currentTimeMillis() - time;
    	//Log.i("SQF_LOG", "time::::::::::::::::::::::::::::::::::::::::::::::::::::::::" + time);
    }

    private void resumeInitLayoutConfig(boolean flag) {
    	//Aurora <SQF> <2014-04-04>  for NEW_UI begin
    	if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
    	//Aurora <SQF> <2014-04-04>  for NEW_UI end
			if (flag) {// 6->2
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == CLOUMNS_NUM2) return;
				m_Clomuns -= 1;
				if(m_Clomuns < CLOUMNS_NUM2) m_Clomuns = CLOUMNS_NUM2;
			} else {// 2->6
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == CLOUMNS_NUM6) return;
				m_Clomuns += 1;
				if(m_Clomuns > CLOUMNS_NUM6) m_Clomuns = CLOUMNS_NUM6;
			}
			//Log.i("SQF_LOG", "PPPP m_LastClomuns set to: " + m_LastClomuns + " m_Clomuns:" + m_Clomuns);
    	} 
    	//Aurora <SQF> <2014-04-04>  for NEW_UI begin 
    	else {
    		//Log.i("SQF_LOG", "m_LastClomuns: " + m_LastClomuns + " m_Clomuns:" + m_Clomuns);
    		if (flag) {// 10->8->6
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == LANDSCAPE_CLOUMNS_NUM6) return;
				m_Clomuns -= 1;
				if(m_Clomuns < LANDSCAPE_CLOUMNS_NUM6) m_Clomuns = LANDSCAPE_CLOUMNS_NUM6;
			} else {// 6->8->10
				m_LastClomuns = m_Clomuns;
				if (m_Clomuns == LANDSCAPE_CLOUMNS_NUM10) return;
				m_Clomuns += 1;
				if(m_Clomuns > LANDSCAPE_CLOUMNS_NUM10) m_Clomuns = LANDSCAPE_CLOUMNS_NUM10;
			}
    		//Log.i("SQF_LOG", "LLLL m_LastClomuns set to: " + m_LastClomuns + " m_Clomuns:" + m_Clomuns);
    	}
    	//Aurora <SQF> <2014-04-04>  for NEW_UI end   	
    	if (mAlbumDataLoader != null) {
			mAlbumDataLoader.setSortMode(m_Clomuns);
			mbDay = ((m_Clomuns == CLOUMNS_NUM6) && GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
		}
    	int visibleIndex = mLayout.getVisibleStart();
    	
    	int pos = mLayout.resumeInitLayoutParameters(m_Clomuns, visibleIndex);
    	//Log.i("SQF_LOG", "resumeInitLayoutConfig visibleIndex:" + visibleIndex + " after call resumeInitLayoutParameters --> pos:" + pos);
    	makeAuroraSlotVisible(pos);
	}
    
    //Aurora <SQF> <2014-04-28>  for NEW_UI begin
    private boolean isMaxColumnNumReached() {
    	boolean isPortrait = GalleryUtils.isPortrait(mActivity.getAndroidContext());
    	return (m_Clomuns == CLOUMNS_NUM6 && isPortrait) || (m_Clomuns ==  LANDSCAPE_CLOUMNS_NUM10 && ! isPortrait);
    }
    private boolean isMinColumnNumReached() {
    	boolean isPortrait = GalleryUtils.isPortrait(mActivity.getAndroidContext());
    	return (m_Clomuns == CLOUMNS_NUM2 && isPortrait) || (m_Clomuns ==  LANDSCAPE_CLOUMNS_NUM6 && !isPortrait);
    }
    //Aurora <SQF> <2014-04-28>  for NEW_UI end

    
    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-13
    /*
    public void startLayoutAnimation(boolean flag) {
    	//Log.i(TAG, "zll --- startLayoutAnimation flag:"+flag+",m_Clomuns:"+m_Clomuns);
    	
    	//Aurora <SQF> <2014-04-08>  for NEW_UI begin
    	if (!flag && isMaxColumnNumReached()) {
    	//Aurora <SQF> <2014-04-08>  for NEW_UI end
    		return;
    	} else if (flag && isMinColumnNumReached()) {
    		//Aurora <SQF> <2014-04-08>  for NEW_UI end
			return;
		} else {
			//long time1 = System.currentTimeMillis();
			resumeInitLayoutConfig(flag);
	    	boolean badd = false;
	    	float scale = 0f;
	    	if (mLayout.getSlotHeight() > mLayout.getLastHeight()) {
	    		scale = (float)mLayout.getLastHeight()/mLayout.getSlotHeight();
			} else {
				badd = true;
				scale = (float)mLayout.getSlotHeight()/mLayout.getLastHeight();
			}

	    	mAnimation = new LayoutMoveAnimation(scale, badd);
	    	mAnimation.start();
		}

    	if (mLayout.mSlotCount != 0) invalidate();
		return;
	}
	*/
    //Aurora <SQF> <2014-08-13>  for NEW_UI end
    
    
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
		
	}
	
	public void stopMakeScale() {
		if(null != mLongScale){
			mLongScale.endScale();
			mLongScale = null;
			mIsLongScale = false; //Aurora <SQF> <2014-08-19> add 
		}
	}

	public enum LONG_SCALE_DIRECTION {
		NO_CHANGE,
		ZOOM_OUT,
		ZOOM_IN,
	};
	
	private int getResumeInitScrollPosition(int theOtherKnot, int visibleStart) {
		int columnNum = 0;
		if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
        	columnNum = CLOUMNS_NUM6 - theOtherKnot;
        } else {
        	columnNum = LANDSCAPE_CLOUMNS_NUM10 - theOtherKnot;
        }

		Rect r = mLayout.getLastSlotRectEx(visibleStart, columnNum, new Rect());
		int scrollPos = r.top - mLayout.mVerticalPadding.get() - mLayout.mHeaderHeight;
		
		//Log.i("SQF_LOG", "  mLayout.mVerticalPadding.get() :" + ( mLayout.mVerticalPadding.get() ) + "   mLayout.mHeaderHeight:" + mLayout.mHeaderHeight);
		//Log.i("SQF_LOG", "getResumeInitScrollPosition  --> visibleStart:" + visibleStart + " columnNum:" + columnNum + " scrollPos:" + scrollPos);
		return scrollPos;
	}
	
	private int mRememberedVisibleStart = 0;
	public class LongScale {
		/*
		//knot model 
		//when portrait,
		//KNOT    4   3   2   1   0
		//COLUMN  2   3   4   5   6
		
		//when landscape 
		//KNOT    4   3   2   1   0
		//COLUMN  6   7   8   9   10
		*/
		
		//Aurora <SQF> <2014-08-12>  for NEW_UI begin
		private static final int MYAURORA_RISING_DISTANCE = 400;
		private static final float EACH_DELTA_SCALE = 0.25f;
		private static final float MIN_MODE_DELTA_SCALE = 2.0f;
		private static final int UNSPECIFIED = -1;
		private int mOriginalKnot = UNSPECIFIED;//when user scale begin
		private int mCurrentKnot = UNSPECIFIED;//maybe mOriginalKnot - 2, or mOriginalKnot - 3, etc.;
		private int mTheOtherKnot = UNSPECIFIED;
		private int mPrevKnot = mCurrentKnot;
		private float mPrevScale = 1.0f;
		private float mDeltaScale = 0.0f;
		private float mProgress = 0.0f;
		private float mMinModeProgress = 0.0f;
		private Rect mTheOtherRect = new Rect();
		private float [] mKnotScales = new float[5];
		private boolean mMinMode = false;
		private boolean mMaxMode = false;
		
		private LONG_SCALE_DIRECTION mDirection;
		
		private int mTheOtherKnotContentLength = 0;
		
		private int mCurrentVisibleStart = 0;

		public LongScale(int columns) {
			calculateOriginalKnot(columns);
			mCurrentKnot = mOriginalKnot;
			formKnotScaleDynamically();
			//mCurrentVisibleStart = mLayout.mVisibleStart;
			mCurrentVisibleStart = mLayout.calculateRealVisibleStart(mLayout.mScrollPosition);
			
		}
		
		//formKnotScaleDynamically must be called after calculateOriginalKnot(int columns)		
		private void formKnotScaleDynamically() {
			for(int i=0; i < mKnotScales.length; i++) {
				mKnotScales[i] = (i - mOriginalKnot) * EACH_DELTA_SCALE + 1.0f;
				//Log.i("SQF_LOG", "formKnotScaleDynamically mKnotScales[" + i + "]:" + mKnotScales[i]);
			}
		}
		
		private void calculateOriginalKnot(int columns) {
			if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
				mOriginalKnot = CLOUMNS_NUM6 - columns;
			} else {
				mOriginalKnot = LANDSCAPE_CLOUMNS_NUM10 - columns;
			}
			//Log.i("SQF_LOG", "mOriginalKnot:" + mOriginalKnot);
		}
		
		
		private void calculateCurrentKnot(float scale) {
			if( scale <= mKnotScales[0] ) mCurrentKnot = 0;
			if( scale >= mKnotScales[4] ) mCurrentKnot = 4;
			for(int i = 1; i <= 3; i++) {
				if( (mPrevScale >= mKnotScales[i] && scale <= mKnotScales[i]) || 
					(mPrevScale <= mKnotScales[i] && scale >= mKnotScales[i])) {
					mCurrentKnot = i;
					//Log.i("SQF_LOG", "!!!!!!!!!!!!!!!!!!!!!!!! !!!  calculateCurrentKnot ----> mCurrentKnot:" + mCurrentKnot + " (mPrevKnot == mCurrentKnot):" + (mPrevKnot == mCurrentKnot));
					break;
				}
			}
		}

		//calculateTheOtherKnot must be called after mCurrent is correctly calculated.
		private int calculateTheOtherKnot(float scale) {
				int i = mCurrentKnot;
				if(i == 4) {
					if(scale >= mKnotScales[i]) {
						return i;
					} else if(scale < mKnotScales[i] && scale > mKnotScales[i - 1]) {
						return i - 1;
					}
				}
				
				if(i == 0) {
					if(scale <= mKnotScales[i]) {
						return 0;
					} else if(scale < mKnotScales[i + 1] && scale > mKnotScales[i]) {
						return i + 1;
					}
				}
				
				if(i == mCurrentKnot) {
					if(scale < mKnotScales[i + 1] && scale > mKnotScales[i]) {
						return i + 1;
					} else if(scale < mKnotScales[i] && scale > mKnotScales[i - 1]) {
						return i - 1;
					} else if(scale == mKnotScales[i]){
						return i;
					} else {
						Log.i("SQF_LOG", "calculateTheOtherKnot ERROR OCCURS ========================");
					}
				}
			return i;
		}
		
		public void setScale(float scale) {
			if(mCurrentKnot >= mKnotScales.length || mCurrentKnot==-1) return;//SQF ADDED ON 2015.5.27     wenyongzhe modify 2015.11.25
			mMinMode = false;
			mMaxMode = false;
			mDeltaScale = scale - mKnotScales[mCurrentKnot];
			mProgress = Math.abs(mDeltaScale) / EACH_DELTA_SCALE;
			mMinModeProgress = Math.abs(mDeltaScale) / MIN_MODE_DELTA_SCALE;
			if(mPrevKnot == 0 && mDeltaScale < 0) {
				mMinMode = true;
				return;
			}
			if(mPrevKnot == 4 && mDeltaScale > 0) {
				//Log.i("SQF_LOG","mMaxMode = true ......");
				mMaxMode = true;
				return;
			}
			mPrevKnot = mCurrentKnot;
			calculateCurrentKnot(scale);
			//Log.i("SQF_LOG", "!!!!!!!!!!!!!!!!!!!!!!!! !!!  BEFORE calculateTheOtherKnot ----> mTheOtherKnot:" + mTheOtherKnot);
			mTheOtherKnot = calculateTheOtherKnot(scale);
			//Log.i("SQF_LOG", "!!!!!!!!!!!!!!!!!!!!!!!! !!!  AFTER calculateTheOtherKnot ----> mTheOtherKnot:" + mTheOtherKnot);

			//Log.i("SQF_LOG", "setScale -->BEFORE RESUME scale:" +  scale  + " mDeltaScale:" + mDeltaScale + /*" delta:" + delta +*/ " mPrevKnot:" + mPrevKnot + " mCurrentKnot:" + mCurrentKnot + 
			//							" mTheOtherKnot:" + mTheOtherKnot +  " mOriginalKnot:" + mOriginalKnot + " mProgress:" + mProgress + 
			//							" mCurrentVisibleStart:" + mCurrentVisibleStart + " mLayout.mScrollPosition:" + mLayout.mScrollPosition);
			
			if(mPrevKnot != UNSPECIFIED && mPrevKnot != mCurrentKnot) {
				int setPos = getResumeInitScrollPosition(mCurrentKnot, mCurrentVisibleStart) ;//getResumeInitScrollPosition(mTheOtherKnot, mLayout.getVisibleStart());
				resumeInitLayoutConfigEx(mPrevKnot < mCurrentKnot, setPos);
				mProgress = 0.0f;
				//Log.i("SQF_LOG", "--------------------------- setScale -->AFTER RESUME resumeInitLayoutConfigEx setPos: " + setPos + " mCurrentKnot:" + mCurrentKnot + " mTheOtherKnot:" + mTheOtherKnot + " mCurrentVisibleStart:" + mCurrentVisibleStart);
			}
			mPrevScale = scale;

		}
		
		public void endScale() {
			if (mMaxMode) return;
			if (mProgress < 1 && mProgress > 0) {
				if (mCurrentKnot != UNSPECIFIED && mTheOtherKnot != UNSPECIFIED) {
					mAnimation = new SnapBack(mCurrentKnot, mTheOtherKnot,
							mProgress, mCurrentVisibleStart,
							mLayout.mScrollPosition,
							getResumeInitScrollPosition(mTheOtherKnot,
									mCurrentVisibleStart));
					// Log.i("SQF_LOG", "SnapBack start..........................................");
					mAnimation.start();
				}
			}
		}

		public void play(GLCanvas canvas, int slotIndex, Rect currentKnotRect) {
			if (mMinMode) {
				//Log.i("SQF_LOG", "LongScale::play mMinModeProgress:" + mMinModeProgress );
				if (currentKnotRect.right - currentKnotRect.left != m_tWidth) {
					canvas.translate(0, 0, MYAURORA_RISING_DISTANCE * mMinModeProgress);
				} else {
					canvas.translate(0, 0, MYAURORA_RISING_DISTANCE * mMinModeProgress *0.5f);
				}
				return;
			}
			int num = 0;
			if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
				num = CLOUMNS_NUM6 - mTheOtherKnot;
			} else {
				num = LANDSCAPE_CLOUMNS_NUM10 - mTheOtherKnot;
			}
			mTheOtherRect = mLayout.getLastSlotRectEx(slotIndex, num, mTheOtherRect);
			float zoomRatio = ( (float) mTheOtherRect.width() / (float) currentKnotRect.width() );
			zoomRatio = 1 + (zoomRatio - 1) * mProgress;
			
			float offsetX = mTheOtherRect.centerX() - currentKnotRect.centerX();
			float offsetY = (mTheOtherRect.centerY() - getResumeInitScrollPosition(mTheOtherKnot, mCurrentVisibleStart)) - 
						(currentKnotRect.centerY() - mLayout.mScrollPosition );

			//Log.i("SQF_LOG", "offsetY:" + offsetY);
			canvas.translate(offsetX * mProgress, offsetY * mProgress);
			float x = (currentKnotRect.width() - currentKnotRect.width() * zoomRatio) / 2;
			canvas.translate(x, x);
			canvas.scale(zoomRatio , zoomRatio, 1.0f);
		}
	}

	private void transformSlotAnimation(GLCanvas canvas, Rect curKnotRect, Rect theOtherKnotRect, float progress, 
										int curScrollPos, int afterResumeScrollPos) {
		float zoomRatio = ( (float) theOtherKnotRect.width() / (float) curKnotRect.width() );
		zoomRatio = 1 + (zoomRatio - 1) * progress;
		
		float offsetX = theOtherKnotRect.centerX() - curKnotRect.centerX();
		float offsetY = (theOtherKnotRect.centerY() - afterResumeScrollPos - theOtherKnotRect.width() / 2) - 
				(curKnotRect.centerY() - mLayout.mScrollPosition - (mLayout.mVerticalPadding.get() + mLayout.mHeaderHeight)- curKnotRect.width() / 2);
		canvas.translate(offsetX * progress, offsetY * progress);

		float x = (curKnotRect.width() - curKnotRect.width() * zoomRatio) / 2;
		canvas.translate(x, x);
		canvas.scale(zoomRatio , zoomRatio, 1.0f);
	}

	public class SnapBack extends SlotAnimation {		
		private static final float BACK_TO_CURRENT_FACTOR = 0.3f;
		private int mCurrentKnot = 3;
		private int mTheOtherKnot = 3;
		private float mCurrentProgress = 0f;
		private boolean mBackToCurrent = true;
		private int mVisibleStart = 0;
		private final static float ERROR_RANGE = 0.001f; 
		private boolean mAlreadySnapBack = false;
		private Rect mTheOtherRect = new Rect();
		private int mCurrentScrollPosition = 0;
		private int mAfterResumeScrollPos = 0;
		private boolean mResumeDirection;

        public SnapBack(int currentKnot, int theOtherKnot, float progress, int visibleStart, int curScrollPos, int afterResumeScrollPos) {
        	super();
			setDuration(600);
			mCurrentScrollPosition = curScrollPos;
			mAfterResumeScrollPos = curScrollPos == 0 ? 0 : afterResumeScrollPos;
			mCurrentKnot = currentKnot;
			mTheOtherKnot = theOtherKnot;
			mCurrentProgress = progress;
			mVisibleStart = visibleStart;
			mBackToCurrent = (progress < BACK_TO_CURRENT_FACTOR) ? true : false;
			mResumeDirection = mCurrentKnot < mTheOtherKnot;
			//setAuroraActive(true);
		}
        
        public boolean getResumeDirection() {
        	return mResumeDirection;
        }
        
        public int getAfterResumeScrollPos() {
        	return mAfterResumeScrollPos;
        }
        
        private float getProgress() {
        	return mProgress;
        }
        
        private int getVisibleStart() {
        	return mVisibleStart;
        }
		
        @Override
        protected void onCalculate(float progress) {
        	if(mBackToCurrent) {
        		mProgress = mCurrentProgress - mCurrentProgress * progress;
        	} else {
        		mProgress = mCurrentProgress + (1 - mCurrentProgress) * progress;
        	}
        }

		@Override
        public void apply(GLCanvas canvas, int slotIndex, Rect currentKnotRect) {
			int num = 0;
			if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
				num = CLOUMNS_NUM6 - mTheOtherKnot;
			} else {
				num = LANDSCAPE_CLOUMNS_NUM10 - mTheOtherKnot;
			}
			mTheOtherRect = mLayout.getLastSlotRectEx(slotIndex, num, mTheOtherRect);
			/////////////////////////////
			float zoomRatio = ( (float) mTheOtherRect.width() / (float) currentKnotRect.width() );
			zoomRatio = 1 + (zoomRatio - 1) * mProgress;
			
			float offsetX = mTheOtherRect.centerX() - currentKnotRect.centerX();
			float offsetY = (mTheOtherRect.centerY() - mAfterResumeScrollPos) - 
						(currentKnotRect.centerY() - mCurrentScrollPosition);
			canvas.translate(offsetX * mProgress, offsetY * mProgress);

			float x = (currentKnotRect.width() - currentKnotRect.width() * zoomRatio) / 2;
			canvas.translate(x, x);
			canvas.scale(zoomRatio , zoomRatio, 1.0f);
			////////////////////////////////
			//transformSlotAnimation(canvas, currentKnotRect, mTheOtherRect, mProgress, mCurrentScrollPosition, mAfterResumeScrollPos);
			//Log.i("SQF_LOG", "apply : mProgress: " + mProgress + " mCurrentKnot:" + mCurrentKnot + " mTheOtherKnot:" + mTheOtherKnot + " (slotIndex == (mLayout.mVisibleEnd - 1):" + (slotIndex == (mLayout.mVisibleEnd - 1)) );
		}

	}

	//Aurora <paul> <2014-04-10> end	
	
    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
	//SQF ANNOTATED ON 2014-08-13
	/*
    public class LayoutMoveAnimation extends SlotAnimation {
        private float mScale = 0f;
        private boolean mbAddScale = false;
        private final int AURORA_RISING_DISTANCE = 1000;
        

        public LayoutMoveAnimation(float scale, boolean badd) {
			setDuration(1000);
			this.mScale = scale;
			this.mbAddScale = badd;
		}
        
		@Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
			//Log.i("SQF_LOG", "LayoutMoveAnimation:: apply --> slotIndex:" + slotIndex + " target:" + target);
			Rect lastrectRect = mLayout.getLastSlotRect(slotIndex, m_LastClomuns, mLastTempRect);
			int offset = lastrectRect.centerY()-target.centerY();
			if (Math.abs(offset) > mLayout.getSlotViewWith()) {
				if (offset > 0) {
					canvas.translate((lastrectRect.centerX()-target.centerX())*(1 - mProgress), (mLayout.getSlotHeight())*(1 - mProgress), 0);
				} else {
					canvas.translate((lastrectRect.centerX()-target.centerX())*(1 - mProgress), -(mLayout.getSlotHeight())*(1 - mProgress), 0);
				}
			} else {
				canvas.translate((lastrectRect.centerX()-target.centerX())*(1 - mProgress), (lastrectRect.centerY()-target.centerY())*(1 - mProgress), 0);
			}

            if (target.right - target.left != m_tWidth) {
            	if (mbAddScale) {
                	canvas.scale((1+(1-mScale)*(1-mProgress)), (1+(1-mScale)*(1-mProgress)), 0);
    			} else {
    				canvas.scale((mScale+(1-mScale)*mProgress), (mScale+(1-mScale)*mProgress), 0);
    			}
			}
            
            return;
        }
    }
    */
	//Aurora <SQF> <2014-08-13>  for NEW_UI end
	
    public class LayoutScaleMoveAnimation extends SlotAnimation{
    	private final int AURORA_RISING_DISTANCE2 = 400;
    	protected float mScaleFactor = 0.5f;
    	
		public LayoutScaleMoveAnimation() {
			setDuration(500);
		}

		@Override
		public void apply(GLCanvas canvas, int slotIndex, Rect target) {
			//Log.i("SQF_LOG", "LayoutScaleMoveAnimation:: apply --> slotIndex:" + slotIndex + " target:" + target);
			if (target.right - target.left != m_tWidth) {
				canvas.translate(0, 0, AURORA_RISING_DISTANCE2 * (1 - mProgress/2 - mScaleFactor));
			} else {
				canvas.translate(0, 0, AURORA_RISING_DISTANCE2 * (1 - mProgress/2 - mScaleFactor)*0.5f);
			}
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
        public int displaywidth = 0;
        public int displayheight = 0;
        
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

        private int mWidth = 1080;
        private int mHeight = 1920;

        private int mUnitCount;
        private int mContentLength;
        private int mScrollPosition;
        
        private int mLastScrollPosition;
        
        private static final int POSITION_FILLER = -0x01;
        private static final int POSITION_HEADER = -0x02;
        private static final int POSITION_HEADER_FILLER = -0x03;
        private static final int POSITION_VIEW_FILLER = -0x04;
        private int mHeaderHeight = 0; // originally int 60, SQF modified to 51, already put into aurora_dimension.xml,attribute tag "header_height"
        private int mHeaderNum = 0;
        private int []header_pos;

        private IntegerAnimation mVerticalPadding = new IntegerAnimation();
        private IntegerAnimation mHorizontalPadding = new IntegerAnimation();
        
        private HashMap<Integer, PositionInfo> mPosMap = null;
        private int mLastHeight = 0;
        private int mSixSlotCount = 0;
        private int mNoSixSlotCount = 0;

        public void setSlotSpec(Spec spec) {
            mSpec = spec;
        }

        private void initHeaderArray() {      	
        	if (header_pos == null || mAlbumDataLoader == null) {
				return;
			}
        	//Aurora <SQF> <2014-03-31> for visual design begin
        	//mHeaderHeight = mSpec.headerHeight;
        	//Aurora <SQF> <2014-03-31> for visual design end
        	for (int i = 0; i < header_pos.length; i++) {
        		if (i == 0) {
        			header_pos[i] = mVerticalPadding.get();
				} else if (i == 1) {
					//Log.i("SQF_LOG", " negative? i:" + i + "-->  (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1):" + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) + " mSlotHeight:" + mSlotHeight);
					header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mSpec.headerHeight;
				} else {
					//Aurora <SQF> <2014-03-31> for visual design begin
					//ORIGINALLY:
					//header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mHeaderHeight;
					//MODIFIED TO:
					//Log.i("SQF_LOG", " negative? i:" + i + "-->  (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1):" + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) + " mSlotHeight:" + mSlotHeight);
					header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mSpec.headerHeight +  mSpec.headerExtraHeight;
					//header_pos[i] = header_pos[i-1] + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(i-1)-1) * (mSlotHeight + mSlotGap)+ mSpec.headerHeight +  20;
					//Aurora <SQF> <2014-03-31> for visual design end
				}
        		//Log.i("SQF_LOG", "SlotView::initHeaderArray i:" + i + ", header_pos[" + i + "]:" + header_pos[i]);
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
            if(mSlotCount == 0) {
            	setVisibleRange(0, 0);
            }
            //Aurora <SQF> <2014-07-18>  for NEW_UI end
            if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null)  {
            	mHeaderNum = 0;
            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
            	mSlotCount = slotCount;
            	header_pos = null;
            	if (mHeaderNum > 0) {
            		header_pos = new int[mHeaderNum];
				}
			} else {
				mSlotCount = slotCount;
			}
            int hPadding = mHorizontalPadding.getTarget();
            int vPadding = mVerticalPadding.getTarget();
            initLayoutParameters();
            
            //Aurora <SQF> <2014-07-29>  for NEW_UI begin
            //ORIGINALLY:
            //return vPadding != mVerticalPadding.getTarget() || hPadding != mHorizontalPadding.getTarget();
            //SQF MODIFIED TO:
            return true;
            //Aurora <SQF> <2014-07-29>  for NEW_UI end
        }
        
        /*
        public Rect getLastSlotRect(int index, int num, Rect rect) {
        	int col, row;
        	boolean bHeader = false;
        	
        	int height = mLastHeight;
        	
        	ItemPositionInfo mInfo = mAlbumDataLoader.getRowsAndCloumns(index, num);
        	if (mInfo == null) {
        		rect.set(0, -mVerticalPadding.get(), m_leftlayoutwidth, 0);
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
				//x = mHorizontalPadding.get();
        		x = 0;
        		if (false) {
        			y = mVerticalPadding.get() + mInfo.header_index* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap);
				} else {
					//Aurora <SQF> <2014-03-31>  for NEW_UI begin
					//ORIGINALLY: 
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap);
					//MODIFIED TO:
					y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					//Aurora <SQF> <2014-03-31>  for NEW_UI end
				}
				//Log.i(TAG, "zll ---- getLastSlotRect mVerticalPadding.get():"+mVerticalPadding.get()+",mHorizontalPadding.get():"+mHorizontalPadding.get());
				//rect.set(x, y, x + mWidth -20, y + mHeaderHeight);
				
				rect.set(x, y, m_leftlayoutwidth, y + mHeaderHeight);
				//rect.set(x, y+mHeaderHeight, mWidth, y + mHeaderHeight*2);
				bHeader = true;
			} else {
				x = mHorizontalPadding.get() + col * (height + mSlotGap)+m_leftlayoutwidth;
				//te = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap);
				//Aurora <SQF> <2014-03-31>  for NEW_UI begin
				//ORIGINALLY: 
				//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (height + mSlotGap);
				//MODIFIED TO:
				y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
				//Aurora <SQF> <2014-03-31>  for NEW_UI end
				rect.set(x, y, x + height, y + height);
				bHeader = false;
			}
        	
        	//Log.i(TAG, "zll --- getLastSlotRect 1 index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row+",header_index:"+mInfo.header_index+",type:"+mInfo.type+",height:"+height);
            //Log.i(TAG, "zll --- getSlotRect 2 m_leftlayoutwidth:"+m_leftlayoutwidth+",mHorizontalPadding.get():"+mHorizontalPadding.get()+",mVerticalPadding.get():"+mVerticalPadding.get()+",mSlotWidth:"+mSlotWidth);
        	
        	
        	
        	return rect;
		}
		*/

        public Rect getSlotRect(int index, Rect rect) {
            int col, row;
            //Log.i("SQF_LOG", "getSlotRect --> index:" + index + " rect:" + rect.toString()); 
            if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	boolean bHeader = false;
            	ItemPositionInfo mInfo = mAlbumDataLoader.getRowsAndCloumns(index, -1);
            	if (mInfo == null) {
            		//Log.i(TAG, "zll ---- getSlotRect == null");
            		rect.set(0, -mVerticalPadding.get(), m_leftlayoutwidth, 0);
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
					//x = mHorizontalPadding.get();
            		x = 0;
            		if (false) {
            			y = mVerticalPadding.get() + mInfo.header_index* mHeaderHeight+(row-mInfo.header_index) * (mSlotHeight + mSlotGap);
					} else {
						//Aurora <SQF> <2014-03-31>  for NEW_UI begin
						//ORIGINALLY: 
						//y = mVerticalPadding.get() + (mInfo.header_index + 1) * mHeaderHeight + (row-mInfo.header_index) * (mSlotHeight + mSlotGap);
						//MODIFIED TO:
						y = mVerticalPadding.get() + (mInfo.header_index + 1) * mHeaderHeight + (row-mInfo.header_index) * (mSlotHeight + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
						//Aurora <SQF> <2014-03-31>  for NEW_UI end
					}
					//Log.i(TAG, "zll ---- mVerticalPadding.get():"+mVerticalPadding.get()+",mHorizontalPadding.get():"+mHorizontalPadding.get()+",y:"+y);
					//rect.set(x, y, x + mWidth -20, y + mHeaderHeight);
					rect.set(x, y, m_leftlayoutwidth, y + mHeaderHeight);
					bHeader = true;
				} else {
					x = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap) + m_leftlayoutwidth;
					//te = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap);
					
					//Aurora <SQF> <2014-03-31> for visual design begin
					//ORIGINALLY: 
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (mSlotHeight + mSlotGap);
					//MODIFIED TO:
					y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (mSlotHeight + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					//Aurora <SQF> <2014-03-31> for visual design end

					rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
					bHeader = false;
				}
            	
            	{
//            		if (mPosMap != null && !mPosMap.containsKey(row)) {
//            			Log.i(TAG, "zll --- getSlotRect 1 index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row+",header_index:"+mInfo.header_index+",rect.bottom:"+rect.bottom);
//            			PositionInfo infos = new PositionInfo(row, rect.bottom, bHeader);
//                		mPosMap.put(row, infos);
//					}
            	}
            	
	            //Log.i(TAG, "zll --- getSlotRect 1 index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row+",header_index:"+mInfo.header_index+",type:"+mInfo.type);
	            //Log.i(TAG, "zll --- getSlotRect 2 m_leftlayoutwidth:"+m_leftlayoutwidth+",mHorizontalPadding.get():"+mHorizontalPadding.get()+",mVerticalPadding.get():"+mVerticalPadding.get()+",mSlotWidth:"+mSlotWidth);
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
				
	            //Log.i(TAG, "zll --- getSlotRect 22 index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row+",mVerticalPadding.get():"+mVerticalPadding.get());
			}
            
            //mFirstVisibleItem = row;
            //Log.i(TAG, "zll --- getSlotRect index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row);

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
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		unitCount = (minorLength + mSlotGap - m_leftlayoutwidth) / (minorUnitSize + mSlotGap);
			} else {
				unitCount = (minorLength + mSlotGap) / (minorUnitSize + mSlotGap);
				//MyLog.i("SQF_LOG", "SlotView::initLayoutParameters unitCount:" + unitCount + " minorLength:" + minorLength + " minorUnitSize:" + minorUnitSize); 
			}
        	
            //Log.i(TAG, "zll ---- initLayoutParameters 1 unitCount:"+unitCount+",mWidth:"+mWidth+",mHeight:"+mHeight);
            if (unitCount == 0) unitCount = 1;
            mUnitCount = unitCount;
            mSpec.mcloums = mUnitCount;
            mSlotCount = mAlbumDataLoader.getSixSlotCount(mUnitCount);
            //MyLog.i("SQF_LOG", "SlotView::initLayoutParameters(.....) mSlotCount --> " + mSlotCount);
            mbDay = (m_Clomuns == CLOUMNS_NUM6 && GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
            // We put extra padding above and below the column.
            //Log.i("SQF_LOG", "zll SlotView::initLayoutParameters --> mUnitCount:" + mUnitCount + " mSlotCount:" + mSlotCount);
            int availableUnits = Math.min(mUnitCount, mSlotCount);
            int usedMinorLength = 0;
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	if (availableUnits < mUnitCount) {
            		usedMinorLength = mUnitCount * minorUnitSize + (mUnitCount - 1) * mSlotGap + m_leftlayoutwidth;
				}else {
					usedMinorLength = availableUnits * minorUnitSize + (availableUnits - 1) * mSlotGap + m_leftlayoutwidth;
				}
            	
			} else {
				usedMinorLength = availableUnits * minorUnitSize + (availableUnits - 1) * mSlotGap;
			}
            
            //Aurora <SQF> <2014-03-31>  for NEW_UI begin
            //padding[0] = (minorLength - usedMinorLength) / 2;
            //MODIFIED TO:
            padding[0] = 0;
            //Aurora <SQF> <2014-03-31>  for NEW_UI end

            // Then calculate how many columns we need for all slots.
            int count = 0;
            //if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null && header_pos != null) {
            if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	mAlbumDataLoader.setCloums(mUnitCount);
            	initHeaderArray();
            	count = mAlbumDataLoader.getAlbumDLTotalRowSize(mUnitCount);
            	//Log.i("SQF_LOG", "initLayoutParameters(...) mUniCount:" + mUnitCount + " count:" + count);
            	if (mHeaderNum > 0) {
            		//MyLog.i("SQF_LOG", "initLayoutParameters(...) mAlbumDataLoader.getAlbumDLCurRowsByHeader(mHeaderNum-1)-1:" + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(mHeaderNum-1)-1) + " mHeaderNum:" + mHeaderNum);
            		mContentLength = header_pos[mHeaderNum-1] + mHeaderHeight + (mAlbumDataLoader.getAlbumDLCurRowsByHeader(mHeaderNum-1)-1) * (minorUnitSize+mSlotGap) - header_pos[0] + 3 * mSlotGap + 51;
            		//MyLog.i("SQF_LOG", "initLayoutParameters(...) mHeaderNum:" + mHeaderNum+", minorUnitSize:"+minorUnitSize+",header_pos[mHeaderNum-1]:"+header_pos[mHeaderNum-1] + " mContentLength:" + mContentLength);
				} else {
					mContentLength = -mSlotGap;
					//MyLog.i("SQF_LOG", "initLayoutParameters(...) 2222 222 mContentLength:" + mContentLength);
				}
            	
			} else {
				//Log.i("SQF_LOG", "initLayoutParameters(...) mContentLength: something wrong...");
				count = ((mSlotCount + mUnitCount - 1) / mUnitCount);
				mContentLength = count * majorUnitSize + (count - 1) * mSlotGap;	
				//mContentLength = -1;//Iuni <lory><2014-01-25> add begin
			}
            
            //Log.i(TAG, "zll ---- initLayoutParameters 1 unitCount:"+unitCount+",count:"+count+",mSlotCount:"+mSlotCount+",mContentLength:"+mContentLength+",mbDay:"+mbDay);

            // If the content length is less then the screen width, put
            // extra padding in left and right.
            if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	//Aurora <SQF> <2014-03-31>  for NEW_UI begin
            	//padding[1] = Math.max(0, 1+m_actionbarHeight);
            	//MODIFIED TO:
            	padding[1] = Math.max(0, m_actionbarHeight);
            	//Aurora <SQF> <2014-03-31>  for NEW_UI end
			} else {
				padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
			}
            
            //Log.i(TAG, "zll ---- initLayoutParameters 2 padding0:"+padding[0]+",padding1:"+padding[1]+",minorLength:"+minorLength+",usedMinorLength:"+usedMinorLength+",mUnitCount:"+mUnitCount);
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
		
		private int getWidthByNum(int num){
			if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
				if(num >= 7){
					//return mSpec.displaywidth;
					return mWidth;
				}
				
				if(mGotWidth){
					return mWidhts[num];
				}
				mGotWidth = true;
				//int tmp_width = mSpec.displaywidth;
				int tmp_width = mWidth;
				//Log.i("SQF_LOG", "mSpec.displaywidth --> " + mSpec.displaywidth);
				for(int i = 2;i < 7; ++i){
					if(CLOUMNS_NUM6 == i) {
						mWidhts[i] = (tmp_width-m_leftlayoutwidth-(i-1)*mSpec.slotGap - mSpec.slotViewRightMargin6Column)/i;
					} else {
						mWidhts[i] = (tmp_width-m_leftlayoutwidth-(i-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/i;
					}
					//Log.i("SQF_LOG", "PPPPPP:  mWidhts[" + i + "] --> " + mWidhts[i]);
				}
				return mWidhts[num];
			} else {
				if(num >= 11){
					Log.w(TAG,"getWidthByNum failed:" + num);
					//return mSpec.displaywidth;
					return mWidth;
				}
				if(mGotWidth){
					return mWidhts[num - 4];
				}
				mGotWidth = true;
				//int tmp_width = mSpec.displayheight;
				int tmp_width = mWidth;
				for(int i = 2;i < 7; ++i){
					int j = i + 4;
					mWidhts[i] = (tmp_width-m_leftlayoutwidth-(j-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/j;
				}
				return mWidhts[num - 4];
			}
		}
		public Rect getLastSlotRectEx(int index, int num, Rect rect) {
				int col, row;
				boolean bHeader = false;
				if(num == -1) num = m_Clomuns;//SQF ADDED ON 2014.8.30
				int height = mLayout.getWidthByNum(num);
				ItemPositionInfo mInfo = mAlbumDataLoader.getRowsAndCloumns(index, num);
				if (mInfo == null) {
					rect.set(0, - mVerticalPadding.get(), m_leftlayoutwidth, 0);
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
					//x = mHorizontalPadding.get();
					x = 0;
					if (false) {
						y = mVerticalPadding.get() + mInfo.header_index* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap);
					} else {
						//SQF_MODIFIED_BEGIN
						//ORIGINALLY: 
						//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap);
						//MODIFIED TO:
						y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
						//SQF_MODIFIED_END
					}
					//Log.i(TAG, "zll ---- getLastSlotRect mVerticalPadding.get():"+mVerticalPadding.get()+",mHorizontalPadding.get():"+mHorizontalPadding.get());
					//rect.set(x, y, x + mWidth -20, y + mHeaderHeight);
						
					rect.set(x, y, m_leftlayoutwidth, y + mHeaderHeight);
					//rect.set(x, y+mHeaderHeight, mWidth, y + mHeaderHeight*2);
					bHeader = true;
				} else {
					x = mHorizontalPadding.get() + col * (height + mSlotGap)+m_leftlayoutwidth;
					//te = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap);
					//SQF_MODIFIED_BEGIN
					//ORIGINALLY: 
					//y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (height + mSlotGap);
					//MODIFIED TO:
					y = mVerticalPadding.get() + (mInfo.header_index+1)* mHeaderHeight+(row-mInfo.header_index-1) * (height + mSlotGap) + mInfo.header_index * mSpec.headerExtraHeight;
					//SQF_MODIFIED_END
					rect.set(x, y, x + height, y + height);
					bHeader = false;
				}
				//Log.i(TAG, "zll --- getLastSlotRect 1 index:"+index+",x:"+x+",y:"+y+",col:"+col+",row:"+row+",header_index:"+mInfo.header_index+",type:"+mInfo.type+",height:"+height);
				
	
				return rect;
		}

		//Aurora <paul> <2014-04-10> end
        private int resumeInitLayoutParameters(int num, int visibleIndex) {
        	//MyLog.i("SQF_LOG", "SlotView::resumeInitLayoutParameters num:" + num + " visibleIndex:" + visibleIndex);
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	mHeaderNum = 0;
            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
            	header_pos = null;
            	if (mHeaderNum > 0) {
            		header_pos = new int[mHeaderNum];
				}
			}
        	
        	if (mSpec.slotWidth != -1) {
        		mLastHeight = mSpec.slotWidth;
        		
        		int tmp_width = mWidth;
//        		int tmp_width = mSpec.displaywidth;
//        		if (MySelfBuildConfig.NUM_CLOUMNS_HORIZONTAL) {
//        			tmp_width = mSpec.displayheight;
//				}
        		
        		//Aurora <SQF> <2014-04-08>  for NEW_UI begin
        		if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
        			//tmp_width = mSpec.displaywidth;
        			
        			mSlotCount = mAlbumDataLoader.getSixSlotCount(num);
        			//MyLog.i("SQF_LOG", "SlotView::resumeInitLayoutParameters() mSlotCount --> " + mSlotCount);
        			if(CLOUMNS_NUM6 == num) {
        				// when 6 column, slotview's right margin is 1dp, otherwise slotview's right margin is 3dp.
        				mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin6Column)/num;
        			} else {
        				mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/num;
        			}
        		} else {
        			//tmp_width = mSpec.displayheight;
        			//Aurora <SQF> <2014-05-07>  for NEW_UI begin
        			mSlotCount = mAlbumDataLoader.getSixSlotCount(num);
        			//Aurora <SQF> <2014-05-07>  for NEW_UI end
        			//mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin6Column)/num;
        			mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/num;
        		}
        		//Aurora <SQF> <2014-04-08>  for NEW_UI end
        		mSpec.slotHeight = mSpec.slotWidth;       		
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
                mSlotGap = mSpec.slotGap; //Iuni <lory><2014-01-16> add for test
                m_leftlayoutwidth = mSpec.leftlayout;
                m_actionbarHeight = mSpec.actionbarHeight;
                
                m_tWidth = m_leftlayoutwidth;
                m_tHeight = mHeaderHeight;
                //mHeaderIndex = new MyHeaderIndex();
                //Log.i(TAG, "zll ---- 1 m_tHeight:"+m_tHeight+",displaywidth:"+mSpec.displaywidth+",slotGap:"+mSpec.slotGap+",slotViewRightMargin6Column:"+mSpec.slotViewRightMargin6Column+",slotViewRightMargin3Column:"+mSpec.slotViewRightMargin3Column);
            } else {
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
                
                //Log.i(TAG, "zll ---- 2 rows:"+rows+",mSlotHeight:"+mSlotHeight+",mSlotWidth:"+mSlotWidth);
            }

            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
            
            //Log.i(TAG, "zll ---- 2 resumeInitLayoutParameters mWidth:"+mWidth+",mHeight:"+mHeight+",mSlotHeight:"+mSlotHeight+",mSlotGap:"+mSlotGap+",m_leftlayoutwidth:"+m_leftlayoutwidth+",m_actionbarHeight:"+m_actionbarHeight);
            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                mVerticalPadding.startAnimateTo(padding[1]);
                mHorizontalPadding.startAnimateTo(padding[0]);
            }
            
            if (visibleIndex > 0) {
            	Rect r = getSlotRect(visibleIndex, new Rect());
            	//Log.i(TAG, "zll ---- mScrollPosition:"+mScrollPosition+",r.top:"+r.top);
            	mScrollPosition = r.top;
			} 
            
            updateVisibleSlotRange();
			return mScrollPosition;
		}

		//
		
		private int resumeInitLayoutParametersEx(int num, int visibleIndex, int setPosition) {
        	//MyLog.i("SQF_LOG", "SlotView::resumeInitLayoutParameters num:" + num + " visibleIndex:" + visibleIndex);
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            	mHeaderNum = 0;
            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
            	header_pos = null;
            	if (mHeaderNum > 0) {
            		header_pos = new int[mHeaderNum];
				}
			}
        	
        	if (mSpec.slotWidth != -1) {
        		mLastHeight = mSpec.slotWidth;
        		
        		int tmp_width = mWidth;
//        		int tmp_width = mSpec.displaywidth;
//        		if (MySelfBuildConfig.NUM_CLOUMNS_HORIZONTAL) {
//        			tmp_width = mSpec.displayheight;
//				}
        		
        		//Aurora <SQF> <2014-04-08>  for NEW_UI begin
        		if(GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
        			//tmp_width = mSpec.displaywidth;
        			
        			mSlotCount = mAlbumDataLoader.getSixSlotCount(num);
        			//MyLog.i("SQF_LOG", "SlotView::resumeInitLayoutParameters() mSlotCount --> " + mSlotCount);
        			if(CLOUMNS_NUM6 == num) {
        				// when 6 column, slotview's right margin is 1dp, otherwise slotview's right margin is 3dp.
        				mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin6Column)/num;
        			} else {
        				mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/num;
        			}
        		} else {
        			//tmp_width = mSpec.displayheight;
        			//Aurora <SQF> <2014-05-07>  for NEW_UI begin
        			mSlotCount = mAlbumDataLoader.getSixSlotCount(num);
        			//Aurora <SQF> <2014-05-07>  for NEW_UI end
        			//mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin6Column)/num;
        			mSpec.slotWidth = (tmp_width-m_leftlayoutwidth-(num-1)*mSpec.slotGap - mSpec.slotViewRightMargin3Column)/num;
        		}
        		//Aurora <SQF> <2014-04-08>  for NEW_UI end
        		mSpec.slotHeight = mSpec.slotWidth;       		
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
                mSlotGap = mSpec.slotGap; //Iuni <lory><2014-01-16> add for test
                m_leftlayoutwidth = mSpec.leftlayout;
                m_actionbarHeight = mSpec.actionbarHeight;
                
                m_tWidth = m_leftlayoutwidth;
                m_tHeight = mHeaderHeight;
                //mHeaderIndex = new MyHeaderIndex();
                //Log.i(TAG, "zll ---- 1 m_tHeight:"+m_tHeight+",displaywidth:"+mSpec.displaywidth+",slotGap:"+mSpec.slotGap+",slotViewRightMargin6Column:"+mSpec.slotViewRightMargin6Column+",slotViewRightMargin3Column:"+mSpec.slotViewRightMargin3Column);
            } else {
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
                
                //Log.i(TAG, "zll ---- 2 rows:"+rows+",mSlotHeight:"+mSlotHeight+",mSlotWidth:"+mSlotWidth);
            }

            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
            
            //Log.i(TAG, "zll ---- 2 resumeInitLayoutParameters mWidth:"+mWidth+",mHeight:"+mHeight+",mSlotHeight:"+mSlotHeight+",mSlotGap:"+mSlotGap+",m_leftlayoutwidth:"+m_leftlayoutwidth+",m_actionbarHeight:"+m_actionbarHeight);
            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                mVerticalPadding.startAnimateTo(padding[1]);
                mHorizontalPadding.startAnimateTo(padding[0]);
            }
            
            if (visibleIndex > 0) {
            	//Rect r = getSlotRect(visibleIndex, new Rect());
            	//Log.i(TAG, "zll ---- mScrollPosition:"+mScrollPosition+",r.top:"+r.top);
            	//mScrollPosition = r.top /*- mLayout.mVerticalPadding.get() - mLayout.mHeaderHeight*/;
            	
			} 
            //Log.i("SQF_LOG", "zll ---- resumeInitLayoutParametersEx --> will update mScrollPosition:" + mScrollPosition);
            
            mScrollPosition = setPosition;
            
            updateVisibleSlotRange();
            
            /*
            if(mScrollPosition != setPosition) {
            	Log.i("SQF_LOG", " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! mScrollPosition:" + mScrollPosition + " setPosition:" + setPosition);
            }
            */
            
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
        		//Log.i("SQF_LOG", "SlotView::calculateDefaultColumnNum mPrevScreenOrientation == mCurrentScreenOrientation return m_Clomuns:" + m_Clomuns);
        		return m_Clomuns;
        	}
        	int defaultColumnNum = m_Clomuns;
        	if(mPrevScreenOrientation == Configuration.ORIENTATION_PORTRAIT && mCurrentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
        		defaultColumnNum = m_Clomuns + 4;
        		//Log.i("SQF_LOG", "SlotView::calculateDefaultColumnNum mPrevScreenOrientation == Configuration.ORIENTATION_PORTRAIT return:" + defaultColumnNum);
        	} else if(mPrevScreenOrientation == Configuration.ORIENTATION_LANDSCAPE && mCurrentScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
        		defaultColumnNum = m_Clomuns - 4;
        		//Log.i("SQF_LOG", "SlotView::calculateDefaultColumnNum mPrevScreenOrientation == Configuration.ORIENTATION_LANDSCAPE return:" + defaultColumnNum);
        	}
        	return defaultColumnNum;
        }
        //Aurora <SQF> <2014-6-3>  for NEW_UI end

        private void initLayoutParameters() {
            // Initialize mSlotWidth and mSlotHeight from mSpec
        	//Aurora <SQF> <2014-04-25>  for NEW_UI begin
        	stopScroll();
        	clearWidths();
        	
        	//Aurora <SQF> <2014-6-3>  for NEW_UI begin
        	int defaultColumnNum = calculateDefaultColumnNum();
        	//Aurora <SQF> <2014-6-3>  for NEW_UI end
        	
        	 //Aurora <SQF> <2014-04-25>  for NEW_UI end
            if (mSpec.slotWidth != -1) {
            	//MyLog.i("SQF_LOG", "initLayoutParameters() ---> 222222222222222222222222 mSpec.slotWidth:" + mSpec.slotWidth + " mSpec.slotHeightL" + mSpec.slotHeight);
                //mSlotGap = 0;//Iuni <lory><2014-01-20> add begin
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
                mHeaderHeight = mSpec.headerHeight;
                mSlotGap = mSpec.slotGap; //Iuni <lory><2014-01-16> add for test
                m_leftlayoutwidth = mSpec.leftlayout;
                m_actionbarHeight = mSpec.actionbarHeight;
                m_tWidth = m_leftlayoutwidth;
                m_tHeight = mHeaderHeight;
                //Aurora <SQF> <2014-04-08>  for NEW_UI begin
                if(!GalleryUtils.isPortrait(mActivity.getAndroidContext())) {
                	//landscape mode
                	//MyLog.i("SQF_LOG", "initLayoutParameters() ---> LANDSCAPE mWidth:" + mWidth);
                	//int defaultColumnNum = LANDSCAPE_CLOUMNS_NUM7;
                	mSlotWidth = (mWidth - m_leftlayoutwidth - (defaultColumnNum - 1) * mSlotGap - mSpec.slotViewRightMargin3Column) / defaultColumnNum;
                	mSlotHeight = mSlotWidth;
                	//Log.i("SQF_LOG", "initLayoutParameters() ---> 3333333333333333 mSlotWidth:" + mSlotWidth + " mSlotHeight:" + mSlotHeight);
                	m_LastClomuns = (defaultColumnNum - 1) < LANDSCAPE_CLOUMNS_NUM6 ?  LANDSCAPE_CLOUMNS_NUM6 : (defaultColumnNum - 1);
                	//m_Clomuns = LANDSCAPE_CLOUMNS_NUM7;
                	m_Clomuns = defaultColumnNum;
                } else {
                	//MyLog.i("SQF_LOG", "initLayoutParameters() ---> PORTRAIT mWidth:" + mWidth);
                	//int defaultColumnNum = CLOUMNS_NUM3;
                	mSlotWidth = (mWidth - m_leftlayoutwidth - (defaultColumnNum - 1) * mSlotGap - mSpec.slotViewRightMargin3Column) / defaultColumnNum;
                	mSlotHeight = mSlotWidth;
                	
                	/*
                	Log.i("SQF_LOG", "initLayoutParameters() ---> 44444444444444444 mWidth:" + mWidth +
                									" m_leftlayoutwidth:" + m_leftlayoutwidth + 
                									" defaultColumnNum:" + defaultColumnNum + 
                									" mSlotGap:" + mSlotGap +
                									" mSpec.slotViewRightMargin3Column:" + mSpec.slotViewRightMargin3Column +
                									" mSlotWidth:" + mSlotWidth + " mSlotHeight:" + mSlotHeight);
                	*/
                	
                	m_LastClomuns = (defaultColumnNum - 1) < CLOUMNS_NUM2 ?  CLOUMNS_NUM2 : (defaultColumnNum - 1);
                	//m_Clomuns = CLOUMNS_NUM3;
                	m_Clomuns = defaultColumnNum;
                }
            	//Aurora <SQF> <2014-04-08>  for NEW_UI end
                //MyLog.i("SQF_LOG", "zll ---- 1 m_actionbarHeight:"+m_actionbarHeight+",mSlotHeight:"+mSlotHeight+",mSlotWidth:"+mSlotWidth+",m_tHeight:"+m_tHeight+",mSpec.slotWidth:"+mSpec.slotWidth);
            } else {
            	//MyLog.i("SQF_LOG", "initLayoutParameters() ---> 55555555555555555555 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
                //MyLog.i("SQF_LOG", "SlotView::initLayoutParameters()-> rows:"+rows+",mSlotHeight:"+mSlotHeight+",mSlotWidth:"+mSlotWidth);
            }
            //Aurora <SQF> <2014-04-28>  for NEW_UI begin
            if (mAlbumDataLoader != null) {
    			mAlbumDataLoader.setSortMode(m_Clomuns);
    			mbDay = ((m_Clomuns == CLOUMNS_NUM6) && GalleryUtils.isPortrait(mActivity.getAndroidContext()))?true:false;
    			
    			mHeaderNum = 0;
            	mHeaderNum = mAlbumDataLoader.getAlbumDLNumHeaders();
            	header_pos = null;
            	if (mHeaderNum > 0) {
            		header_pos = new int[mHeaderNum];
				}
            	mSlotCount = mAlbumDataLoader.getSixSlotCount(m_Clomuns);
            	//MyLog.i("SQF_LOG", "SlotView::initLayoutParameters() mSlotCount --> " + mSlotCount);
    		}
            //Aurora <SQF> <2014-04-28>  for NEW_UI end

            mHeadTempRect.set(0, 0, m_leftlayoutwidth, AuroraStringTexture.sHeight);
            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
            
            //MyLog.i("SQF_LOG", "zll ---- 2 mWidth:"+mWidth+",mHeight:"+mHeight+",mSlotHeight:"+mSlotHeight+",mSlotGap:"+mSlotGap+",m_leftlayoutwidth:"+m_leftlayoutwidth+",m_actionbarHeight:"+m_actionbarHeight);
            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                mVerticalPadding.startAnimateTo(padding[1]);
                mHorizontalPadding.startAnimateTo(padding[0]);
            }
            updateVisibleSlotRange();
        }

        public void setSize(int width, int height) {
        	//Log.i("SQF_LOG", "SlotView::setSize----> width: " + width + " height:" + height + " will call initLayoutParameters");
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
        
        //Aurora <SQF> <2014-05-19>  for NEW_UI begin
        //ORIGINALLY:
        /*
        private void updateVisibleSlotRange() {
            int position = mScrollPosition;
            //Log.i(TAG, "zll ----- updateVisibleSlotRange position:"+position);
			mLastScrollPosition = mScrollPosition;
			
            if (WIDE) {
                int startCol = position / (mSlotWidth + mSlotGap);
                int start = Math.max(0, mUnitCount * startCol);
                int endCol = (position + mWidth + mSlotWidth + mSlotGap - 1) /
                        (mSlotWidth + mSlotGap);
                int end = Math.min(mSlotCount, mUnitCount * endCol);
                setVisibleRange(start, end);
            } else {
            	
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            		long time1 = SystemClock.uptimeMillis();
            		int startRow = 0;
            		int cur_header = 0;
            		//boolean bheader = false;
            		int start = 0;
            		boolean bfirst = false;
            		
            		if (header_pos != null) {
            			if (position <= header_pos[0] + mHeaderHeight) {
            				cur_header = 0;
            				bfirst = true;
            				//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 1111111 cur_header-->" + cur_header + " position:" + position);
    					} else if (position >= header_pos[header_pos.length-1]) {
            				cur_header = header_pos.length-1;
    					} else {
    						for (int i = 1; i < header_pos.length; i++) {
    							if (position < header_pos[i]) {
    								cur_header = i-1;
    								//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 2222222 cur_header-->" + cur_header + " position:" + position);
    								break;
    							}
    						}
						}
            			
            			if (position >= header_pos[cur_header]&&
							position <= header_pos[cur_header] + mHeaderHeight) {
            				if (!bfirst) {
								cur_header = cur_header > 0 ?cur_header-1 : cur_header;
	            				startRow = mAlbumDataLoader.getAlbumDLAllItemsByRows(cur_header)+cur_header+1;
	            				//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 333333333 --> cur_header-->" + cur_header + " startRow:" + startRow);
							}
							start = Math.max(0, startRow-2);
//							Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 44444444 --> cur_header-->" + cur_header + 
//									" startRow:" + startRow + " start:" + start + 
//									" mAlbumDataLoader.getAlbumDLAllItemsByRows(cur_header):" + mAlbumDataLoader.getAlbumDLAllItemsByRows(cur_header));
							//Log.i(TAG, "zll ---- ffff 0.1.1 start:"+start+",startRow:"+startRow+",cur_header:"+cur_header+",header_pos[cur_header]:"+header_pos[cur_header]);
						} else {
							//int tmp_startRow = (position - header_pos[cur_header])/(mSlotHeight + mSlotGap);
							//startRow = mAlbumDataLoader.getAlbumDLCurToatalRowsByHeader(cur_header-1)+tmp_startRow+1;
							//start = Math.max(0, mAlbumDataLoader.getIndexByRows(startRow-cur_header, cur_header));
							int tmp_startRow = (position - header_pos[cur_header]-mHeaderHeight)/(mSlotHeight + mSlotGap);
							startRow = mAlbumDataLoader.getAlbumDLCurToatalRowsByHeader(cur_header) + tmp_startRow + 1;
							
							start = Math.max(0, mAlbumDataLoader.getIndexByRows(tmp_startRow+1, cur_header)-2);
							//Log.i(TAG, "zll ---- ffff 0.1.2 start:"+start+",cur_header:"+cur_header+",startRow:"+startRow+",tmp_startRow:"+tmp_startRow);
						}
            			
            			//Log.i(TAG, "zll ---- ffff 2 start:"+start+",cur_header:"+cur_header);
					} else {
						startRow = position / (mSlotHeight + mSlotGap);
					}
            		
	                int endRow = (position + mHeight + mSlotHeight + mSlotGap - 1) /
	                        (mSlotHeight + mSlotGap);
	                int t_end = mUnitCount * endRow+cur_header+1;
	                if (t_end - start >= 106) {
	                	t_end = start + 105;
					}
	                int end = Math.min(mSlotCount, t_end);
	                
	                //Log.i(TAG, "zll --- 1111 updateVisibleSlotRange 0 tttt start:"+start+",end:"+end+",startRow:"+startRow+",endRow:"+endRow+",position:"+position+",cur_header:"+cur_header);
	                setVisibleRange(start, end);
	                long time2 = SystemClock.uptimeMillis() - time1;
	                Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange ----------------------> use time:" + time2 + " ms");
            	}
            }
        }
        */
        //SQF MODIFIED TO:
        private int calculateCurHeaderByScrollPosition(int position) {
        	//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 0000000000000000000000000000");
    		int topMargin = header_pos[0];
    		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange 1111111 topMargin-->" + topMargin);
    		int headerLen = header_pos.length;
    		int midHeaderIndex = Math.round((headerLen - 1) / 2.0f);
    		int curHeader = 0;
    		
    		boolean found = false;
    		//first search the first and the last
    		if(position >= header_pos[0] - topMargin && position < header_pos[0] - topMargin + getContentLengthOfHeader(0)) {
    			curHeader = 0;
    			found = true;
    		}
    		if(position >= header_pos[headerLen - 1] - topMargin) {
    			curHeader = headerLen - 1;
    			found = true;
    		}
    		//binary search the header_pos
    		int tmpStart = 0;
    		int tmpEnd = headerLen - 1;
    		while( ! found) {
    			int midStartPos = header_pos[midHeaderIndex] - topMargin;
    			int midEndPos = header_pos[midHeaderIndex] - topMargin + getContentLengthOfHeader(midHeaderIndex);
    			//Log.i("SQF_LOG", "while 11111111 midHeaderIndex:" + midHeaderIndex + " midStartPos:" + midStartPos + " midEndPos:" + midEndPos + " position:" + position);
    			if( position >= midStartPos && position <= midEndPos ) {
    				curHeader = midHeaderIndex;
    				found = true;
    			} else if(position < midStartPos){
    				tmpEnd = midHeaderIndex;
    				midHeaderIndex = Math.round( (tmpStart + midHeaderIndex) / 2.0f);
    			} else if(position > midEndPos) {
    				tmpStart = midHeaderIndex;
    				midHeaderIndex = Math.round((midHeaderIndex + tmpEnd) / 2.0f);
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
    		int midRowIndex = Math.round((howManyRows - 1) / 2.0f);
    		int curRow = 0;
    		boolean found = false;
    		/*
    		Log.i("SQF_LOG", "=========================================================================");
    		for(int i=0 ; i<howManyRows; i ++) {
    			int start = (mHeaderHeight + (mSlotHeight + mSlotGap) * (i));
    			int end = start + mSlotHeight + mSlotGap;
    			Log.i("SQF_LOG", "!!!! row:" + i + " start:" + start + " end:" + end);
    		}
    		Log.i("SQF_LOG", "=========================================================================");
    		*/
    		int firstRowStartPos = 0;
    		int firstRowEndPos = mHeaderHeight + mSlotHeight + mSlotGap;
    		int lastRowStartPos = mHeaderHeight + (mSlotHeight + mSlotGap) * (howManyRows - 1);
    		//int lastRowEndPos = lastRowStartPos + (mSlotHeight + mSlotGap);
    		//Log.i("SQF_LOG", "howManyRows:" + howManyRows + " firstRowEndPos:" + firstRowEndPos + " lastRowStartPos:" + lastRowStartPos );
    		if(curHeaderOffset >= firstRowStartPos && curHeaderOffset < firstRowEndPos) {
    			curRow = 0;
    			found = true;
    		}
    		if(curHeaderOffset >= lastRowStartPos) {
    			curRow = howManyRows - 1;
    			found = true;
    		}
    		//binary search the rows
    		int tmpStart = 0;
    		int tmpEnd = howManyRows - 1;
    		while( ! found) {
    			int midRowStartPos = mHeaderHeight + (mSlotHeight + mSlotGap) * (midRowIndex);
    			int midRowEndPos = midRowStartPos + (mSlotHeight + mSlotGap);
    			//Log.i("SQF_LOG", "while 22222222 midRowIndex:" + midRowIndex + " midRowStartPos:" + midRowStartPos + " midRowEndPos:" + midRowEndPos + " curHeaderOffset:" + curHeaderOffset + " position:" + position);
    			if( curHeaderOffset >= midRowStartPos && curHeaderOffset <= midRowEndPos) {
    				curRow = midRowIndex;
    				found = true;
    			} else if(curHeaderOffset < midRowStartPos) {
    				tmpEnd = midRowIndex;
    				midRowIndex = Math.round((tmpStart + midRowIndex) / 2.0f);
    				//Log.i("SQF_LOG", "<== : now: tmpStart:" + tmpStart + " tmpEnd:" + tmpEnd);
    			} else if(curHeaderOffset > midRowEndPos) {
    				tmpStart = midRowIndex;
    				midRowIndex = Math.round((midRowIndex + tmpEnd) / 2.0f);
    				//Log.i("SQF_LOG", "==> : now: tmpStart:" + tmpStart + " tmpEnd:" + tmpEnd);
    			} else {
    				Log.i("SQF_LOG", "error while 2222222222");
    				Utils.assertTrue(false);
    			}
    		}
    		return curRow;
        }
        
        
        private int calculateRealVisibleStart(int position) {
        	if(header_pos == null || mAlbumDataLoader == null) return 0;
        	int curHeader = calculateCurHeaderByScrollPosition(position);
    		int curRow = calculateCurrentRowInHeader(position, curHeader);
    		//Log.i("SQF_LOG", "SlotView::calculateRealVisibleStart curHeader---------->" + curHeader + " curRow:" + curRow);
    		
    		int totalPhotosBeforeCurHeader = 0;
    		int totalPhotosOfCurrentOffset = 0;
    		for(int i = 0; i < curHeader; i++) {
    			totalPhotosBeforeCurHeader += mAlbumDataLoader.getAlbumDLCountForHeader(i);
    		}
    		totalPhotosOfCurrentOffset = curRow * m_Clomuns;
    		int start = totalPhotosBeforeCurHeader + totalPhotosOfCurrentOffset + curHeader;
    		return start;
        }

        private void updateVisibleSlotRange() {
            int position = mScrollPosition;
            //Log.i("SQF_LOG", "updateVisibleSlotRange mScrollPosition:" + mScrollPosition);
			mLastScrollPosition = mScrollPosition;
            if (WIDE) {
                int startCol = position / (mSlotWidth + mSlotGap);
                int start = Math.max(0, mUnitCount * startCol);
                int endCol = (position + mWidth + mSlotWidth + mSlotGap - 1) /
                        (mSlotWidth + mSlotGap);
                int end = Math.min(mSlotCount, mUnitCount * endCol);
                setVisibleRange(start, end);
            } else {
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG && mAlbumDataLoader != null) {
            		//long time1 = SystemClock.uptimeMillis();
            		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange --> mSlotHeight:" + mSlotHeight + " mSlotGap:" + mSlotGap + " mUnitCount:" + mUnitCount + " mHeaderHeight:" + mHeaderHeight);
            		if(header_pos == null || header_pos.length == 0) {
            			//Log.e("SQF_LOG", "SlotView::updateVisibleSlotRange --> ERROR 222");
            			return;
            		}
            		//int topMargin = header_pos[0];
            		
            		/*
            		int curHeader = calculateCurHeaderByScrollPosition(position);
            		int curRow = calculateCurrentRowInHeader(position, curHeader);
            		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange curHeader---------->" + curHeader + " curRow:" + curRow);
            		
            		int totalPhotosBeforeCurHeader = 0;
            		int totalPhotosOfCurrentOffset = 0;
            		for(int i = 0; i < curHeader; i++) {
            			totalPhotosBeforeCurHeader += mAlbumDataLoader.getAlbumDLCountForHeader(i);
            		}
            		totalPhotosOfCurrentOffset = curRow * m_Clomuns;
            		int start = totalPhotosBeforeCurHeader + totalPhotosOfCurrentOffset + curHeader;
            		*/
            		
            		int start = Math.max(0, calculateRealVisibleStart(position) - 15);
            		
            		//start = Math.max(0, start - 15);//substract 15 to avoid white slot rendering while pinching.
            		int increment = Math.round(mHeight * (m_Clomuns + 1) / (mSlotHeight)) + 35;//add 30 to avoid white slot rendering  while pinching.
					
            		/*
            		MyLog.i("SQF_LOG", "SlotView::updateVisibleSlotRange totalPhotosBeforeCurHeader -->" + totalPhotosBeforeCurHeader + 
            							" totalPhotosOfCurrentOffset:" + totalPhotosOfCurrentOffset + 
            							" start:" + start +
            							" increment1111111:" + increment +
            							" mHeight:" + mHeight +
            							" mSlotHeight:" + mSlotHeight +
            							" m_Clomuns:" + m_Clomuns + 
            							" mSlotGap:" + mSlotGap);
					*/
            		increment = Math.min(mSlotCount, increment);
            		
            		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange increment222222:" + increment + " mSlotCount:" + mSlotCount);
            		if(increment >= AlbumDataLoader.MAX_LOAD_COUNT) {
            			increment = AlbumDataLoader.MAX_LOAD_COUNT - 1;
            			//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange increment33333333:" + increment + " mSlotCount:" + mSlotCount);
            		}
            		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange increment44444:" + increment + " mSlotCount:" + mSlotCount);

            		setVisibleRange(start, start + increment);

            		//Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange -----------setVisibleRange-------> start:" + start + " (start + increment) :" + (start + increment));
            		//long time2 = SystemClock.uptimeMillis() - time1;
	                //Log.i("SQF_LOG", "SlotView::updateVisibleSlotRange ----------------------> use time:" + time2 + " ms");
            	}
            }
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
            //Log.i("SQF_LOG", "SlotView::Layout::setScrollPosition  position-->" + position);
            mScrollPosition = position;
            mLastScrollPosition = mScrollPosition;
            updateVisibleSlotRange();
        }

        private void setVisibleRange(int start, int end) {
        	
            if (start == mVisibleStart && end == mVisibleEnd && ! mSlotCountChanged) return;//Aurora <SQF> <2014-07-29> add " && !mSlotCountChanged"
            
            //Log.i("SQF_LOG", "setVisibleRange: prev visibleStart:" + mVisibleStart + " prev visibleEnd:" + mVisibleEnd);
            //Aurora <SQF> <2014-07-29>  for NEW_UI begin
            mSlotCountChanged = false;
            //Aurora <SQF> <2014-07-29>  for NEW_UI end
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }

            //Log.i("SQF_LOG", "setVisibleRange: mVisibleStart:" + mVisibleStart + " mVisibleEnd:" + mVisibleEnd);
            if (mRenderer != null) {
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }
        
        public int getActionBarHeight() {
			return m_actionbarHeight;
		}
        
        public int getLastScrollPosition(){
        	return mLastScrollPosition;
        }
        
        public int getSlotViewWith(){
        	return (mWidth - m_leftlayoutwidth)/2;
        }

        public int getVisibleStart() {
            return mVisibleStart;
        }
        
        public int getLastHeight() {
            return mLastHeight;
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
        			//Log.i(TAG, "zll --- isHeaderSlot 1 header_pos[0]:"+header_pos[0]);
					return true;
				}
        		
        		if (ypos >= header_pos[header_pos.length-1] &&
        			ypos <= header_pos[header_pos.length-1] + mHeaderHeight) {
        			//Log.i(TAG, "zll --- isHeaderSlot 2 header_pos[header_pos.length-1]:"+header_pos[header_pos.length-1]);
					return true;
				}
        		
				for (int i = 0; i < header_pos.length; i++) {
					if ((i > 0) &&
						(ypos < header_pos[i]) &&
						(ypos >= header_pos[i-1]) &&
						(ypos <= header_pos[i-1] + mHeaderHeight)) {
						//Log.i(TAG, "zll --- isHeaderSlot 3 i:"+i+",header_pos[i]:"+header_pos[i-1]);
						return true;
					}
				}
			}
        	
        	return flag;
		}
        
        private int getIndexByPosition(float xpos, int ypos) {
			int index = 0;
			
			int startRow = 0;
    		int cur_header = 0;
    		int start = 0;
    		
    		if (header_pos == null) {
				return INDEX_NONE;
			}
    		
    		if (ypos <= header_pos[0] + mHeaderHeight) {
				cur_header = 0;
			} else if (ypos >= header_pos[header_pos.length-1]) {
				cur_header = header_pos.length-1;
			} else {
				for (int i = 1; i < header_pos.length; i++) {
					if (ypos < header_pos[i]) {
						cur_header = i-1;
						break;
					}
				}
			}
			
			if (ypos >= header_pos[cur_header]&&
				ypos <= header_pos[cur_header] + mHeaderHeight) {
				//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.1 cur_header:"+cur_header+",ypos:"+ypos);
			} else {
				
				if (mSlotList != null) {
					//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.2 size:"+mSlotList.size()+",xpos:"+xpos+",ypos:"+ypos);
					for (int i = 0; i < mSlotList.size(); i++) {
						ItemInfo tInfo = mSlotList.get(i);
						//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.3 tInfo.itemRect.top:"+tInfo.itemRect.top+",tInfo.itemRect.bottom:"+tInfo.itemRect.bottom+",tInfo.itemRect.left:"+tInfo.itemRect.left+",tInfo.itemRect.right:"+tInfo.itemRect.right);
						if (tInfo.itemRect.top < ypos &&
							tInfo.itemRect.bottom > ypos &&
							tInfo.itemRect.right > xpos &&
							tInfo.itemRect.left < xpos) {
							
							//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.4 tInfo.itemIndex:"+tInfo.itemIndex+",cur_header:"+cur_header);
							//return tInfo.itemIndex >= (cur_header+1) ? tInfo.itemIndex - (cur_header+1):tInfo.itemIndex;
							return tInfo.itemIndex;
						}
					}
				}
				/*int tmp_startRow = (ypos - header_pos[cur_header]-mHeaderHeight)/(mSlotHeight + mSlotGap);
				startRow = mAlbumDataLoader.getAlbumDLCurToatalRowsByHeader(cur_header) + tmp_startRow + 1;
				
				start = Math.max(0, mAlbumDataLoader.getIndexByRows(tmp_startRow+1, cur_header)-2);
				Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.4 ypos:"+ypos+",cur_header:"+cur_header+",startRow:"+startRow+",tmp_startRow:"+tmp_startRow);*/
			}
			
			return INDEX_NONE;
		}
        
        public int getAuroraSlotIndexByPosition(float x, float y) {
        	int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            
            int t_y = absoluteY;
            absoluteX -= (mHorizontalPadding.get() + m_leftlayoutwidth);
            absoluteY -= (mVerticalPadding.get()+mHeaderHeight);
            
            if (absoluteX < 0 || 
            	absoluteY < 0 || 
				isHeaderSlot(t_y)) {
				return INDEX_NONE;
			}
            
            int t_index = INDEX_NONE;
            {
            	float xpos = x;
            	int ypos = t_y;
            	int index = 0;
    			int startRow = 0;
        		int cur_header = 0;
        		int start = 0;
        		
        		if (header_pos == null) {
    				return INDEX_NONE;
    			}
        		
        		if (ypos <= header_pos[0] + mHeaderHeight) {
    				cur_header = 0;
    			} else if (ypos >= header_pos[header_pos.length-1]) {
    				cur_header = header_pos.length-1;
    			} else {
    				for (int i = 1; i < header_pos.length; i++) {
    					if (ypos < header_pos[i]) {
    						cur_header = i-1;
    						break;
    					}
    				}
    			}
        		
        		if (ypos >= header_pos[cur_header]&&
        			ypos <= header_pos[cur_header] + mHeaderHeight) {
        				//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.1 cur_header:"+cur_header+",ypos:"+ypos);
        			} else {
        				
        				if (mSlotList != null) {
        					//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.2 size:"+mSlotList.size()+",xpos:"+xpos+",ypos:"+ypos);
        					for (int i = 0; i < mSlotList.size(); i++) {
        						ItemInfo tInfo = mSlotList.get(i);
        						//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1.3 tInfo.itemRect.top:"+tInfo.itemRect.top+",tInfo.itemRect.bottom:"+tInfo.itemRect.bottom+",tInfo.itemRect.left:"+tInfo.itemRect.left+",tInfo.itemRect.right:"+tInfo.itemRect.right);
        						if (tInfo.itemRect.top < ypos &&
        							tInfo.itemRect.bottom > ypos &&
        							tInfo.itemRect.right > xpos &&
        							tInfo.itemRect.left < xpos) {
        							
        							//Log.i(TAG, "zll ---- rrrrrrrrrrrr 2.4 tInfo.itemIndex:"+tInfo.itemIndex+",cur_header:"+cur_header);
        							//return tInfo.itemIndex >= (cur_header+1) ? tInfo.itemIndex - (cur_header+1):tInfo.itemIndex;
        							int slot = tInfo.itemIndex >= (cur_header+1) ? tInfo.itemIndex - (cur_header+1):tInfo.itemIndex;
        							mSlotindex = slot;
        							t_index = tInfo.itemIndex;
        							
        							//Log.i(TAG, "zll ---- rrrrrrrrrrrr 2.4 tInfo.itemIndex:"+tInfo.itemIndex+",cur_header:"+cur_header+",slot:"+slot+",t_index:"+t_index);
        						}
        					}
        				}
        			}
            }
            
            return t_index >= mSlotCount ? INDEX_NONE : t_index;
		}
        
        public int getSlotIndexByPosition(float x, float y) {
            int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            
            int t_y = absoluteY;
            
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	absoluteX -= (mHorizontalPadding.get() + m_leftlayoutwidth);
			} else {
				absoluteX -= mHorizontalPadding.get();
			}
            
            absoluteY -= (mVerticalPadding.get()+mHeaderHeight);

            //Log.i(TAG, "zll ---- rrrrrrrrrrrr 1 x:"+x+",y:"+y+",absoluteX:"+absoluteX+",absoluteY:"+absoluteY+",mScrollPosition:"+mScrollPosition+",t_y:"+t_y+",mHorizontalPadding.get():"+mHorizontalPadding.get());
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
				if (absoluteX < 0 || 
					absoluteY < 0 || 
					isHeaderSlot(t_y)) {
					//Log.i(TAG, "zll ---- rrrrrrrrrrrr 1------- mmmm");
					return INDEX_NONE;
				}
				
				//int t_index = getIndexByPosition(absoluteX+m_leftlayoutwidth, absoluteY);
				int t_index = getIndexByPosition(x, t_y);
				return t_index >= mSlotCount ? INDEX_NONE : t_index;
			} else {
				if (absoluteX < 0 || absoluteY < 0) {
	                return INDEX_NONE;
	            }
			}

            int columnIdx = absoluteX / (mSlotWidth + mSlotGap);
            int rowIdx = absoluteY / (mSlotHeight + mSlotGap);

            //Log.i(TAG, "zll ---- rrrrrrrrrrrr 2-------columnIdx:"+columnIdx+",mUnitCount:"+mUnitCount);
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
            
            //Log.i(TAG, "zll ---- rrrrrrrrrrrr 3-------index:"+index+",rowIdx:"+rowIdx+",columnIdx:"+columnIdx);
            return index >= mSlotCount ? INDEX_NONE : index;
        }

        public int getScrollLimit() {
          	//Aurora <SQF> <2014-05-23>  for NEW_UI begin
        	//ORIGINALLY:
        	//int limit = WIDE ? mContentLength - mWidth : mContentLength - mHeight;
        	//SQF MODIFIED TO:
        	int subtract = (header_pos != null && header_pos.length > 0) ? header_pos[0] : 0;  
        	int limit = WIDE ? mContentLength - (mWidth - subtract): mContentLength - (mHeight - subtract);
            //Aurora <SQF> <2014-05-23>  for NEW_UI end
            //Log.i("SQF_LOG", "SlotView::Layout::getScrollLimit limit:" + limit + " mContentLength:" + mContentLength + " mWidth:" + mWidth + " mHeight:" + mHeight);
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
    	if(mLayout == null || mLayout.mSpec == null) return false;
    	if(e.getY() <= mLayout.mSpec.actionbarHeight) return true;
    	return false;
    }
    //Aurora <SQF> <2014-6-19>  for NEW_UI end
    
    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        // We call the listener's onDown() when our onShowPress() is called and
        // call the listener's onUp() when we receive any further event.
        @Override
        public void onShowPress(MotionEvent e) {
        	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onShowPress ");
            //Aurora <SQF> <2014-09-18>  for NEW_UI begin
            if(mAlbumPage.isInSelectionMode()) return;
            //Aurora <SQF> <2014-09-18>  for NEW_UI end
            GLRoot root = getGLRoot();
            root.lockRenderThread();
            try {
                if (isDown) return;
                
                if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
                	int index = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
                	//Log.i(TAG, "zll ----- onShowPress 1 index:"+index);
                    if (index != INDEX_NONE) {
                        isDown = true;
                        int tIndex = mSlotindex; 
                        //Log.i(TAG, "zll ----- onShowPress 2 index:"+index+",tIndex:"+tIndex);
                        mListener.onAuroraDown(index, tIndex);
                        //Log.i("SQF_LOG", "onShowPress========= index: " + index + " tIndex:" + tIndex);
                    }
                    
				} else {
					int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
	                if (index != INDEX_NONE) {
	                    isDown = true;
	                    //Log.i(TAG, "zll ----- onShowPress index:"+index);
	                    mListener.onDown(index);
	                }
				}
                
            } finally {
                root.unlockRenderThread();
            }
        }

        private void cancelDown(boolean byLongPress) {
        	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onShowPress ");
            if (!isDown) return;
            
            isDown = false;
            //Log.i(TAG, "zll ----- cancelDown byLongPress:"+byLongPress);
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent e) {
        	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onDown ");
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1,
                MotionEvent e2, float velocityX, float velocityY) {
        	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onFling");
            if(isAuroraAnimActive) return false; //Aurora <paul> <2014-05-05>
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            float velocity = WIDE ? velocityX : velocityY;
            //Log.i(TAG, "zll ---- onFling velocity:"+velocity+",scrollLimit:"+scrollLimit);
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null) mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1,
                MotionEvent e2, float distanceX, float distanceY) {
        	
            if(isAuroraAnimActive) return false; //Aurora <paul> <2014-05-05>
            
            //Aurora <SQF> <2014-09-26>  for NEW_UI begin
            if(mIsLongScale) {
            	//Log.i("SQF_LOG", "onScroll 1111111111111111:");
            	return false;
            }
            if(mAnimation != null && (mAnimation instanceof SnapBack) && mAnimation.isActive()) {
            	//Log.i("SQF_LOG", "onScroll 2222222222222222222:" );
            	return false;
            }
            //Aurora <SQF> <2014-09-26>  for NEW_UI end
            
            cancelDown(false);
            float distance = WIDE ? distanceX : distanceY;
            int overDistance = mScroller.startScroll(
                    Math.round(distance), 0, mLayout.getScrollLimit());
            
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
            	mPaper.overScroll(overDistance);
            }
            if (MySelfBuildConfig.USEGALLERY3D_FLAG && USE_EdgeView) {
            	//Log.i(TAG, "zll ---- qqqq overDistance:"+overDistance);
            	if (overDistance < 0) {
            		mEdgeView.onPull(-overDistance, EdgeView.TOP);
                } else {
                	mEdgeView.onPull(overDistance, EdgeView.BOTTOM);
                }
			}
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            cancelDown(false);
            //Aurora <SQF> <2014-6-3>  for bug4817 begin
        	if(motionInActionBarArea(e)) return true;
        	//Aurora <SQF> <2014-6-3>  for bug4817 end
            if (mDownInScrolling) return true;
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	int index = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
            	int tIndex = mSlotindex;
            	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onSingleTapUp index:"+index+",tIndex:"+tIndex);
                if (index != INDEX_NONE) mListener.onAuroraSingleTapUp(index, tIndex);
			} else {
				int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
	            if (index != INDEX_NONE) mListener.onSingleTapUp(index);
			}
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        	//Log.i("SQF_LOG", "SlotView::MyGestureListener::onLongPress ");
        	//Aurora <SQF> <2014-09-18>  for NEW_UI begin
            if(mAlbumPage.isInSelectionMode()) return;
            //Aurora <SQF> <2014-09-18>  for NEW_UI end
            //Aurora <SQF> <2014-6-19>  for NEW_UI begin
        	if(motionInActionBarArea(e)) return;
            //Aurora <SQF> <2014-6-19>  for NEW_UI end
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
                    int index = mLayout.getAuroraSlotIndexByPosition(e.getX(), e.getY());
                    int tIndex = mSlotindex;
                	
                	//Log.i(TAG, "zll -----  getAuroraSlotIndexByPosition index:"+index+",mSlotindex:"+mSlotindex+",tIndex:"+tIndex);
                    if (index != INDEX_NONE) mListener.onAuroraLongTap(index, tIndex);
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
    	//Log.i("SQF_LOG", "SlotView::setSlotCount slotCount:" + slotCount);
    	if (MySelfBuildConfig.USEGALLERY3D_FLAG && bUseArray) {
    		if (mHashMap != null) {
    			//Log.i(TAG, "zll ---- setSlotCount mHashMap != null");
    			mHashMap.clear();
			} else {
//				mHashMap = new HashMap<Integer, SlotView.ItemInfo>();
			}
		}
    	
    	boolean changed = false;
    	if (mAlbumDataLoader != null) {
    		changed = mLayout.setSlotCount(slotCount+mAlbumDataLoader.getAlbumDLNumHeaders());
		} else {
			changed = mLayout.setSlotCount(slotCount);
		}
        //boolean changed = mLayout.setSlotCount(slotCount);

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
    	return isAuroraAnimActive | 
    			mIsUserActionInProgress | 
    			(mAnimation != null && mAnimation.isActive()) | 
    			(mScroller != null && ! mScroller.isFinished());
    }
    //Aurora <SQF> <2014-05-15>  for NEW_UI end
    
    
	//Aurora <paul> <2014-02-27> for NEW_UI begin
	public boolean isAnimPlaying(){
		if(null == mAnimation) return false;
		return (mAnimation.isActive() | isAuroraAnimActive);
	}
	
	public boolean isLongScale() {
		return mIsLongScale;
	}
	//Aurora <paul> <2014-02-27> for NEW_UI end
    
    //Iuni <lory><2014-01-21> add begin -------------------------------------------
    private AlbumDataLoader mAlbumDataLoader;
    public void setAlbumDataLoader(AlbumDataLoader loader) {
    	mAlbumDataLoader = loader;
		return;
	}
    
    private final MyAuroraGestureListener mAuroraGestureListener;
    private final GestureRecognizer mAuroraGestureRecognizer;
    private boolean bAnimation = false;
    private boolean isAuroraAnimActive = false;
    
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
			//Log.i("SQF_LOG", "MyAuroraGestureListener::onScaleBegin focusX:" + focusX + " focusY:" + focusY);
			mAccScale = 1f;
			bAnimation = false;
			mAnimation = null;
			isAuroraAnimActive = true;
			
			if( ! mScroller.isFinished()) {
				mScroller.forceFinished();
			}
			startMakeScale(m_Clomuns);//Aurora <paul> <2014-04-10>
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
			bAnimation = false;
			
			
			isAuroraAnimActive = false;
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
