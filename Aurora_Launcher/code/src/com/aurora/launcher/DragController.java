/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.aurora.launcher;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.method.MovementMethod;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.launcher.Launcher.TransModeEnum;
import com.aurora.launcher.R;
import com.aurora.launcher.Launcher.EditMode;
import com.aurora.view.QuickIndexLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    private static final String TAG = "Launcher.DragController";

    /** Indicates the drag is a move.  */
    public static int DRAG_ACTION_MOVE = 0;

    /** Indicates the drag is a copy.  */
    public static int DRAG_ACTION_COPY = 1;

    private static final int SCROLL_DELAY = 500;
    private static final int RESCROLL_DELAY = 750;
    private static final int VIBRATE_DURATION = 15;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    static final int SCROLL_NONE = -1;
    static final int SCROLL_LEFT = 0;
    static final int SCROLL_RIGHT = 1;

    private static final float MAX_FLING_DEGREES = 35f;

    private Launcher mLauncher;
    private Handler mHandler;
    private final Vibrator mVibrator;

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /** Whether or not we're dragging. */
    private boolean mDragging;

    /** X coordinate of the down event. */
    private int mMotionDownX;

    /** Y coordinate of the down event. */
    private int mMotionDownY;

    /** the area at the edge of the screen that makes the workspace go left
     *   or right while you're dragging.
     */
    private int mScrollZone;
    private int mScrollZoneY;

    private DropTarget.DragObject mDragObject;

    /** Who can receive drop events */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
    private DropTarget mFlingToDeleteDropTarget;

    /** The window token used as the parent for the DragView. */
    private IBinder mWindowToken;

    /** The view that will be scrolled when dragging to the left and right edges of the screen. */
    private View mScrollView;

    private View mMoveTarget;

    private DragScroller mDragScroller;
    // Aurora <haojj> <2013-9-23> add for folder scroller begin
    private DragScroller mFolderDragScroller;
    private FolderScrollRunnable mFolderScrollRunnable = new FolderScrollRunnable();
    // Aurora <haojj> <2013-9-23> end
    
    // Aurora <haojj> <2013-10-2> add for field begin
    private FolderDelayCloseRunnable mFolderDelayCloseRunnable = new FolderDelayCloseRunnable();
    private static final int FOLDER_DEL_DELAY = 500;
	// Aurora <haojj> <2013-10-2> end
    
    private int mScrollState = SCROLL_OUTSIDE_ZONE;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();

    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;

    private int mLastTouch[] = new int[2];
    private long mLastTouchUpTime = -1;
    private int mDistanceSinceScroll = 0;

    private int mTmpPoint[] = new int[2];
    private Rect mDragLayerRect = new Rect();

    protected int mFlingToDeleteThresholdVelocity;
    private VelocityTracker mVelocityTracker;
    
//AURORA-START::Fix bug #116::Shi guiqiang::20131103
    private TextView mTextView;
//AURORA-END::Fix bug #116::Shi guiqiang::20131103
    
	/**start to add for double touch in Hotseat area,default to false value*/
	private boolean isPintchHotSeat = false;
    
//AURORA-START::customized widget::Shi guiqiang::20131017 
    private ResponsePintchEvent mResponsePintchEvent;
    private float Dist_start;
    private float Dist_end;
//AURORA-END::customized widget::Shi guiqiang::20131017
    
    //AURORA-START::App Index::Shi guiqiang::20140111
    private float X_start;
    private float X_end;
    private float scrollstartX;
    private boolean isReadyToMove = false;
    private boolean isAreaToMove = false;
    private boolean isMovingInArea = false;
    protected int mActivePointerId = -1;
    private float mDownMotionX;
    private boolean respondAppIndex = false;
    public AppSearchImageView mAppSearchImageView;
    public QuickIndexLayout mAppIndexContentView;
    private static float MOVE_RESPONSE_LINE_Y;
    private static final float START_SCROLL_THRESHOLD = 0.2f;
    private static final int MAX_FULL_SCREEN_DELAY = 100;
    private static final int MIN_FLING_VELOCITY = 2000;
    private static final float MOVING_THRESHOLD = 0.01f;
    private int APP_INDEX_CONTENT_DURATION = 700;
    private final float APP_INDEX_CONTENT_DELTA_Y = 0.05f;
    private VelocityTracker appVelocityTracker;
    private ImageView mQuickIndexBgBlurView;
    private ImageView mAppSearchBgImageView;
    private Resources resources;
    private CellLayout cellLayout;
    private Hotseat mHotseat;
    private PageIndicator mPageIndicator;
    private float windowWidth;
	private float windowHeight;
	private float appSearchWidth;
	Animator totalAlphaAnimator;
	//AURORA-END::App Index::Shi guiqiang::20140111
	
	//For voice test
	private GlsurfaceMask mImageLayerMask;
	private FrameLayout mVoiceOutLine;
	private VoiceLayout mVoiceLayout;
	private VoiceButtonImageView mVoiceButton;
	private boolean preventTouchEvent = false;
	
	public TimerTask restoreNavigationTask;  
	public Timer timer;
	//For voice test	
	/**For voice print broadcast */
	private String VOICE_COLLECT_ACTION = "com.iuni.voiceassistant.ACTION_VOICE_COLLECT";
	
	
	// Aurora <haojj> <2014-2-17> add for 刚开机时等待图标加载完成后才能进入appIndex begin
	private boolean isReadyToAppIndex = false;
	protected void setReadyToAppIndex(boolean flag){
		isReadyToAppIndex = flag;
	}
	// Aurora <haojj> <2014-2-17> end
	
	private boolean voiceEnable;
	
	private boolean isHomeOrBackPress;
	
    private View workspaceChild;
    private View scrollingIndicator;
	
    /**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {
        
        /**
         * A drag has begun
         * 
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);
        
        /**
         * The drag has ended
         */
        void onDragEnd();
    }
    
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public DragController(Launcher launcher) {
        Resources r = launcher.getResources();
        mLauncher = launcher;
        mHandler = new Handler();
        timer = new Timer();
        mScrollZone = r.getDimensionPixelSize(R.dimen.scroll_zone);
        mScrollZoneY = r.getDimensionPixelSize(R.dimen.scroll_zoneh);
        mVelocityTracker = VelocityTracker.obtain();
        mVibrator = (Vibrator) launcher.getSystemService(Context.VIBRATOR_SERVICE);

        float density = r.getDisplayMetrics().density;
        mFlingToDeleteThresholdVelocity =
                (int) (r.getInteger(R.integer.config_flingToDeleteMinVelocity) * density);
        appVelocityTracker = VelocityTracker.obtain();
        resources = r;
        
        MOVE_RESPONSE_LINE_Y = mLauncher.getFloatValueFromResourcesDimens(R.dimen.quick_index_move_respones_offset);
        voiceEnable = Utilities.getBooleanValueFromResourcesDimens(launcher.getResources(),R.bool.voice_page_enable);
       
    }

    public boolean dragging() {
        return mDragging;
    }

    /**
     * Starts a drag.
     *
     * @param v The view that is being dragged
     * @param bmp The bitmap that represents the view being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
     *          Makes dragging feel more precise, e.g. you can clip out a transparent border
     */
    public void startDrag(View v, Bitmap bmp, DragSource source, Object dragInfo, int dragAction,
            Rect dragRegion, float initialDragViewScale) {
        int[] loc = mCoordinatesTemp;
        mLauncher.getDragLayer().getLocationInDragLayer(v, loc);
        int dragLayerX = loc[0] + v.getPaddingLeft() +
                (int) ((initialDragViewScale * bmp.getWidth() - bmp.getWidth()) / 2);
        int dragLayerY = loc[1] + v.getPaddingTop() +
                (int) ((initialDragViewScale * bmp.getHeight() - bmp.getHeight()) / 2);

        startDrag(bmp, dragLayerX, dragLayerY, source, dragInfo, dragAction, null, dragRegion,
                initialDragViewScale);

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Starts a drag.
     *
     * @param b The bitmap to display as the drag image.  It will be re-scaled to the
     *          enlarged size.
     * @param dragLayerX The x position in the DragLayer of the left-top of the bitmap.
     * @param dragLayerY The y position in the DragLayer of the left-top of the bitmap.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
     *          Makes dragging feel more precise, e.g. you can clip out a transparent border
     */
    public void startDrag(Bitmap b, int dragLayerX, int dragLayerY,
            DragSource source, Object dragInfo, int dragAction, Point dragOffset, Rect dragRegion,
            float initialDragViewScale) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        for (DragListener listener : mListeners) {
            listener.onDragStart(source, dragInfo, dragAction);
        }

        final int registrationX = mMotionDownX - dragLayerX;
        final int registrationY = mMotionDownY - dragLayerY;

        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;

        mDragging = true;

        mDragObject = new DropTarget.DragObject();

        mDragObject.dragComplete = false;
        mDragObject.xOffset = mMotionDownX - (dragLayerX + dragRegionLeft);
        mDragObject.yOffset = mMotionDownY - (dragLayerY + dragRegionTop);
        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;

        mVibrator.vibrate(VIBRATE_DURATION);

        final DragView dragView = mDragObject.dragView = new DragView(mLauncher, b, registrationX,
                registrationY, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);

        if (dragOffset != null) {
            dragView.setDragVisualizeOffset(new Point(dragOffset));
        }
        if (dragRegion != null) {
            dragView.setDragRegion(new Rect(dragRegion));
        }

        dragView.show(mMotionDownX, mMotionDownY);
        handleMoveEvent(mMotionDownX, mMotionDownY);
        
        //add by tangjun  start 9.25
        if ( mLauncher.getEditMode() != EditMode.APPWIDGET_ADD ) {
//AURORA-START::Thumnail animator::Shi guiqiang::20131106
//        	mLauncher.setScrollIndicatorVisible(false);
        	mLauncher.setThumanailImageVisible(false);
        	mLauncher.thumbnailImageSetVisibility( View.GONE );
//AURORA-END::Thumnail animator::Shi guiqiang::20131106
//        	mLauncher.setThumbnailHorizontalScrollViewAnimator(0f, 1f);
        }
        //add by tangjun end
    }

    // Aurora <haojj> <2013-11-4> add for 拖动到Dock栏的时候交换时回到文件夹的动画 begin
    protected DragView createDragSwapView(Bitmap b, int dragLayerX, int dragLayerY, float initialDragViewScale){
        final DragView dragView = new DragView(mLauncher, b, 0,
                0, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);
        dragView.show(dragLayerX, dragLayerY);
        return dragView;
    }
    // Aurora <haojj> <2013-11-4> end
    
    /**
     * Draw the view into a bitmap.
     */
    Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     *
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    public boolean isDragging() {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (mDragging) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.deferDragViewCleanupPostAnimation = false;
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            mDragObject.dragSource.onDropCompleted(null, mDragObject, false, false);
        }
        endDrag();
    }
    public void onAppsRemoved(HashSet<Intent> appsToRmv, Context context) {
        // Cancel the current drag if we are removing an app that we are dragging
        if (mDragObject != null) {
            Object rawDragInfo = mDragObject.dragInfo;
            if (rawDragInfo instanceof ShortcutInfo) {
                ShortcutInfo dragInfo = (ShortcutInfo) rawDragInfo;
                
                if(dragInfo.intent == null) {
                	return;
                }
                
                String dragPkg = dragInfo.getPackageName();
                String dragClass = dragInfo.getClassName();
                
                if((dragPkg == null) || (dragClass == null)) {
                	return;
                }
                
                for(Intent intent: appsToRmv) {
                	if(intent == null) {
                		continue;
                	}
                	
                	String pkgToRmv = ItemInfo.getPackageName(intent);
                	String classToRmv = ItemInfo.getClassName(intent);
                	
                	if(dragPkg.equals(pkgToRmv) && dragClass.equals(classToRmv)) {
                    	LauncherApplication.logVulcan.print(String.format("onAppsRemoved: to cancel dragging, intent = %s", dragInfo.intent.toUri(0)));
                    	Log.d("vulcan-setup",String.format("onAppsRemoved: to cancel dragging, intent = %s", dragInfo.intent.toUri(0)));
                		cancelDrag();
                		return;
                	}
                }
                
/*
                for (String pn : packageNames) {
                    // Added null checks to prevent NPE we've seen in the wild
                    if (dragInfo != null &&
                        dragInfo.intent != null) {
                        boolean isSamePackage = dragInfo.getPackageName().equals(pn);
                        if (isSamePackage) {
                            cancelDrag();
                            return;
                        }
                    }
                }
*/
            }
        }
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            clearScrollRunnable();
            boolean isDeferred = false;
            if (mDragObject.dragView != null) {
            	// Aurora <haojj> <2013-9-30> add for 解决长按文件夹拖动时，放下后删除图标不消失，但拖动到workspace仍没有解决 begin
                isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
                boolean isClean=mDragObject.cleanDragViewForFolder;
                // Aurora <haojj> <2013-9-30> end
                if ( !isDeferred || !isClean||mLastDropTarget == null) {
                    mDragObject.dragView.remove();
                }
                mDragObject.dragView = null;
            }

            // Only end the drag if we are not deferred
            if (!isDeferred||mLastDropTarget == null) {
                for (DragListener listener : mListeners) {
                    listener.onDragEnd();
                }
            }
        }

        releaseVelocityTracker();
        
        //add by tangjun start 9.25
//AURORA-START::Thumnail animator::Shi guiqiang::20131106
//        mLauncher.thumbnailImageSetVisibility( View.GONE );
//AURORA-END::Thumnail animator::Shi guiqiang::20131106
//        mLauncher.setThumbnailHorizontalScrollViewAnimator(1f, 0f);
        //add by tangjun end
    }

    /**
     * This only gets called as a result of drag view cleanup being deferred in endDrag();
     */
    void onDeferredEndDrag(DragView dragView) {
        dragView.remove();

        // If we skipped calling onDragEnd() before, do it now
        for (DragListener listener : mListeners) {
            listener.onDragEnd();
        }
    }

    void onDeferredEndFling(DropTarget.DragObject d) {
        d.dragSource.onFlingToDeleteCompleted();
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }
    
    /**Clamps the position to the drag layer bounds in common */
    public  int[] getCommonClampedDragLayerPos(float x, float y){
    	return getClampedDragLayerPos(x,y);
    }
    
    
    long getLastGestureUpTime() {
        if (mDragging) {
            return System.currentTimeMillis();
        } else {
            return mLastTouchUpTime;
        }
    }

    void resetLastGestureUpTime() {
        mLastTouchUpTime = -1;
    }

//    public void prepareMoveAction(MotionEvent ev){
//     	if (isReadyToMove & isAreaToMove) {
//    		acquireAppIndexMovementVelocityTracker(ev);
//    		isReadyToMove = isAreaToMove = false;
//    		setRespondAppIndexFlag(true);
//    	}
//    }
    
    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d("linp", "DragController.onInterceptTouchEvent " + ev + " mDragging="
                    + mDragging);
        }  
		if(mLauncher.getEditMode() == EditMode.CLASSIFICATE_ICONS && !mLauncher.isRequestAniRunning()){			
			return false;
		}
        //AURORA-START::customized widget::Shi guiqiang::20131017
        int pointerCount = ev.getPointerCount();
		if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN && mLauncher.getTransModeEnum()==TransModeEnum.NONE && mLauncher.getEditMode() != EditMode.CLASSIFICATE_ICONS ) {
			if (pointerCount == 2) {				
				if(mLauncher.getWorkspace().getFolderOpenState()) return true;
	    		Dist_start = 0f;
	    		Dist_end = 0f;
	    		float threshold = mLauncher.getWorkspace().getHeight()*0.79f;
	    		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;            
				float X1_start = ev.getX(0);
				float Y1_start = ev.getY(0);
				float X2_start = ev.getX(1);
				float Y2_start = ev.getY(1);
				Log.d("linp", "Y1_start:"+Y1_start+", Y2_start:"+Y2_start+", isDragging()="+isDragging()+", threshold="+threshold);
				//Hazel add for avoid accidental contact( like when you scroll workspace and another finger touch dock area)
				if ((Y1_start <= threshold && Y2_start <= threshold && !isDragging())||mLauncher.getWorkspace().mTouchState == mLauncher.getWorkspace().TOUCH_STATE_SCROLLING) {
					float Delta_X_start = Math.abs(X2_start - X1_start);
					float Delta_Y_start = Math.abs(Y2_start - Y1_start);
					Dist_start = FloatMath.sqrt(Delta_X_start*Delta_X_start + Delta_Y_start*Delta_Y_start);
					setIsPintchHotseatArea(true);
					return true;
				}
				 //avoid two pointers move up widgets in hotseat 
				 else if ( (Y1_start > threshold || Y2_start > threshold) && !isDragging()){
					return true;
				  }else{
					Log.d("linp", "the Dist_start is "+Dist_start);
					Dist_start = 0f;
					return false;
				}
			}
		}
		
		

		
    	if (pointerCount == 1 && mLauncher.getEditMode() != EditMode.APPWIDGET_ADD 
    						  && mLauncher.getEditMode() != EditMode.FOLDER_IMPORT
    						  && !mLauncher.getWorkspace().getFolderOpenState()) {

    		mLauncher.setVoiceButtonClickState(mLauncher.getVoiceButtonClickState());
    		
 		    float Y_touch = ev.getY();
	        	if (mLauncher.getAnimationFlag() || mLauncher.isCustomMenuProxyShowing() || mLauncher.isWorkspaceLoading() ||mLauncher.isRequestAniRunning()) {
	        		Log.e("linp", "AnimationFlag still runing.so stop another operation!\n");
	        		return true;
	        	}
	        setRespondAppIndexFlag(false);
    	}
		acquireVelocityTrackerAndAddMovement(ev);

		final int action = ev.getAction();
		final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
		final int dragLayerX = dragLayerPos[0];
		final int dragLayerY = dragLayerPos[1];

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			float Y_touch = ev.getY();
			if (Y_touch > mLauncher.getWorkspace().getHeight()* MOVE_RESPONSE_LINE_Y&& isAreaToMove) {
				X_end = ev.getX();
				//to avoid shake when move up widgets in hotseat
				if (Math.abs(X_end - X_start) > mLauncher.getWorkspace()	.getWidth() * MOVING_THRESHOLD * 8) {
					if (pointerCount == 1
							&& mLauncher.getEditMode() != EditMode.APPWIDGET_ADD
							&& mLauncher.getEditMode() != EditMode.FOLDER_IMPORT) {
						isReadyToMove = true;
						setIsMovingInArea(true);
						setRespondAppIndexFlag(true);

						if (isReadyToMove && isAreaToMove) {
							interceptEventForVoiceContentNull();
							if (preventTouchEvent) {
								Log.e("linp","preventTouchEvent is true so will return true soon!\n");
								return true;
							}
						}

						if (mLauncher.getInputMethodState()) {
							return false;
						}
						// For voice test
						if (mLauncher.getTransModeEnum() != TransModeEnum.APPS_INDEX) {
							if ((X_end - X_start) > 0
									|| mLauncher.getTransModeEnum() == TransModeEnum.VOICE) {
								mLauncher.setVoiceButtonVisible(true);
								mLauncher.updateMarginForVoiceOutLine(false);
							}
						}
						// For voice test
						return true;
					}
				}
			} else {
				if (getIsMovingInArea()) {
					setIsMovingInArea(false);
				}
				isAreaToMove = false;
				isReadyToMove = false;
			}
			break;
		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			getControllObject();
			mMotionDownX = dragLayerX;
			mMotionDownY = dragLayerY;
			mLastDropTarget = null;
			X_start = ev.getX();
			Y_touch = ev.getY();
			
			mImageLayerMask.setVisibility(View.VISIBLE);
			if (Y_touch > mLauncher.getWorkspace().getHeight()* MOVE_RESPONSE_LINE_Y&& mLauncher.getEditMode() != EditMode.QUICK_INDEX) {
				Log.e("linp", "onInterceptTouchEvent MotionEvent.ACTION_DOWN");
				isAreaToMove = true;
				if(mLauncher.getTransModeEnum() == TransModeEnum.NONE && mLauncher.getEditMode() == EditMode.NONE){
					Log.e("linp", "###startBlurTask!");
					mLauncher.startTask();
				}
				/**set it to restore flag.*/
				setHomeOrBackPress(false);
			}
            //to hide the input method on touch in VOICE mode
//            if (mLauncher.getTransModeEnum() == TransModeEnum.VOICE && mLauncher.getInputMethodState()
//    				&& Y_touch <= mLauncher.getWorkspace().getHeight()*MOVE_RESPONSE_LINE_Y) {
//    			if (mInputMethodManager == null) {
//    	            mInputMethodManager = (InputMethodManager)mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE);
//    	        }
//    	        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);
//    		}
			break;
		case MotionEvent.ACTION_UP:
			mLastTouchUpTime = System.currentTimeMillis();
			if (mDragging) {
				PointF vec = isFlingingToDelete(mDragObject.dragSource);
				if (vec != null) {
					// Toast.makeText(mLauncher,
					// "onInterceptTouchEvent Fling to delete",
					// Toast.LENGTH_SHORT).show();
					dropOnFlingToDeleteTarget(dragLayerX, dragLayerY, vec);
				} else {
					drop(dragLayerX, dragLayerY);
				}
			}
			endDrag();
			if (getIsMovingInArea()) {
				setIsMovingInArea(false);
			}
			isAreaToMove = false;
			isReadyToMove = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			if(mLauncher.getTransModeEnum() ==TransModeEnum.VOICE ){
				mLauncher.setVoiceButtonVisible(false);
			}
			cancelDrag();
			break;
		}

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    void setMoveTarget(View view) {
        mMoveTarget = view;
    }    

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    private void clearScrollRunnable() {
        mHandler.removeCallbacks(mScrollRunnable);
        if (mScrollState == SCROLL_WAITING_IN_ZONE) {
            mScrollState = SCROLL_OUTSIDE_ZONE;
            mScrollRunnable.setDirection(SCROLL_RIGHT);
            mDragScroller.onExitScrollArea();
            mLauncher.getDragLayer().onExitScrollArea();
        }
    }
    
    // Aurora <haojj> <2013-9-23> add for folder scroll begin
    private void clearFolderScrollRunnable() {
        mHandler.removeCallbacks(mFolderScrollRunnable);
        if (mScrollState == SCROLL_WAITING_IN_ZONE) {
            mScrollState = SCROLL_OUTSIDE_ZONE;
            mFolderScrollRunnable.setDirection(SCROLL_RIGHT);
            mFolderDragScroller.onExitScrollArea();
        }
    }
	// Aurora <haojj> <2013-9-23> end

    // Aurora <haojj> <2013-10-2> add for 取消在FolderEditText区域的话关闭文件夹 begin
    public void cancelFolderDelayCloseRunnable(){
    	mHandler.removeCallbacks(mFolderDelayCloseRunnable);
    }
	// Aurora <haojj> <2013-10-2> end
    
    private void handleMoveEvent(int x, int y) {
    	//AURORA-START:xiejun:20130923:ID138
    	mDragObject.dragView.bringToFront();
    	//AURORA-END:xiejun:20130923:ID138
        mDragObject.dragView.move(x, y);

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        if (dropTarget != null) {
            DropTarget delegate = dropTarget.getDropTargetDelegate(mDragObject);
            if (delegate != null) {
                dropTarget = delegate;
            }

            if (mLastDropTarget != dropTarget) {
                // Aurora <haojj> <2013-9-29> add for Folder to Del begin
            	if (mLastDropTarget != null) {
                	if((mLastDropTarget instanceof Folder && dropTarget instanceof DeleteDropTarget)) {
                		((Folder)mLastDropTarget).setForceNoCloseFolder(true);
                	} else if((mLastDropTarget instanceof Folder) && (dropTarget instanceof FolderEditText || dropTarget instanceof FolderEmptyDropTarget)){
                		((Folder)mLastDropTarget).setForceNoCloseFolder(true);
                		mHandler.postDelayed(mFolderDelayCloseRunnable, FOLDER_DEL_DELAY);
                	} else if((mLastDropTarget instanceof DeleteDropTarget && dropTarget instanceof FolderEditText)){
                		cancelFolderDelayCloseRunnable();
                	} else if(dropTarget instanceof Folder) {
                		cancelFolderDelayCloseRunnable();
                		((Folder)dropTarget).setForceNoCloseFolder(false);
                	}
                    mLastDropTarget.onDragExit(mDragObject);
                }
            	// Aurora <haojj> <2013-9-29> end
            	
            	// Aurora <haojj> <2013-10-22> add for 增加workspace滑动的时延 begin
            	if(mLastDropTarget != null && mLastDropTarget instanceof FolderEmptyDropTarget && dropTarget instanceof ThumbnailImageItem){
            		((ThumbnailImageItem)dropTarget).forceDelayWorkspaceScroll();
            	}
            	// Aurora <haojj> <2013-10-22> end
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        // Aurora <haojj> <2013-9-23> add for TAG begin
        if(!(dropTarget instanceof Folder) && mLastDropTarget instanceof Folder){
        	clearFolderScrollRunnable();
        }
        // Aurora <haojj> <2013-9-23> end
        mLastDropTarget = dropTarget;

        // After a scroll, the touch point will still be in the scroll region.
        // Rather than scrolling immediately, require a bit of twiddling to scroll again
        final int slop = ViewConfiguration.get(mLauncher).getScaledWindowTouchSlop();
        mDistanceSinceScroll +=
            Math.sqrt(Math.pow(mLastTouch[0] - x, 2) + Math.pow(mLastTouch[1] - y, 2));
        mLastTouch[0] = x;
        mLastTouch[1] = y;
        final int delay = mDistanceSinceScroll < slop ? RESCROLL_DELAY : SCROLL_DELAY;
    	
    	// Aurora <haojj> <2013-9-23> add for folder scroll begin
        if(dropTarget instanceof Folder){
        	//Log.e("HJJ", "dropTarget is Folder, x:" + x + ", mScrollZone:" + mScrollZone + ", mScrollView.getWidth() - mScrollZone:" + (mScrollView.getWidth() - mScrollZone));
        	if (x < mScrollZone) {
	            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
	                mScrollState = SCROLL_WAITING_IN_ZONE;
	                if (mFolderDragScroller.onEnterScrollArea(x, y, SCROLL_LEFT)) {
	                    mFolderScrollRunnable.setDirection(SCROLL_LEFT);
	                    mHandler.postDelayed(mFolderScrollRunnable, delay);
	                }
	            }
	        } else if (x > mScrollView.getWidth() - mScrollZone) {
	            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
	                mScrollState = SCROLL_WAITING_IN_ZONE;
	                if (mFolderDragScroller.onEnterScrollArea(x, y, SCROLL_RIGHT)) {
	                	mFolderScrollRunnable.setDirection(SCROLL_RIGHT);
	                    mHandler.postDelayed(mFolderScrollRunnable, delay);
	                }
	            }
	        } else {
	        	clearFolderScrollRunnable();
	        }
        }
        // Aurora <haojj> <2013-9-23> end
        else {
	        if (x < mScrollZone && y < mScrollZoneY) {
	            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
	                mScrollState = SCROLL_WAITING_IN_ZONE;
	                if (mDragScroller.onEnterScrollArea(x, y, SCROLL_LEFT)) {
	                    mLauncher.getDragLayer().onEnterScrollArea(SCROLL_LEFT);
	                    mScrollRunnable.setDirection(SCROLL_LEFT);
	                    mHandler.postDelayed(mScrollRunnable, delay);
	                }
	            }
	        } else if (x > mScrollView.getWidth() - mScrollZone && y < mScrollZoneY) {
	            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
	                mScrollState = SCROLL_WAITING_IN_ZONE;
	                if (mDragScroller.onEnterScrollArea(x, y, SCROLL_RIGHT)) {
	                    mLauncher.getDragLayer().onEnterScrollArea(SCROLL_RIGHT);
	                    mScrollRunnable.setDirection(SCROLL_RIGHT);
	                    mHandler.postDelayed(mScrollRunnable, delay);
	                }
	            }
	        } else {
	            clearScrollRunnable();
	        }
        }
    }

    public void forceMoveEvent() {
    	/*
        if (mDragging) {
            handleMoveEvent(mDragObject.x, mDragObject.y);
        }
        */
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
    	 if (!mDragging) {
        	//AURORA-START::customized widget::Shi guiqiang::20131017
          	int pointerCount = ev.getPointerCount();
          	if (pointerCount == 2 && isPintchHotSeatArea()) {
          		registPintch(mResponsePintchEvent);
          		if (ev.getAction() == MotionEvent.ACTION_MOVE) {		
          		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT; 
                 	float X1_end = ev.getX(0);
         			float Y1_end = ev.getY(0);
         			float X2_end = ev.getX(1);
         			float Y2_end = ev.getY(1);
         			float Delta_X_end = Math.abs(X2_end - X1_end);
         			float Delta_Y_end = Math.abs(Y2_end - Y1_end);
         			Dist_end = FloatMath.sqrt(Delta_X_end*Delta_X_end + Delta_Y_end*Delta_Y_end);
         			float Delta_Dist = Dist_start - Dist_end;
         			float threshold1 = mLauncher.getWorkspace().getWidth()*0.08f;
         			float threshold2 = mLauncher.getWorkspace().getWidth()*0.1f;
         			if ( Dist_start == 0f) {
         				//Don't response the pintch event
         			} else {
         				if (Delta_Dist >= threshold1) {								//set the sensitivity as threshold1, it comes from experience and test
	         				if (!mLauncher.isCustomMenuProxyShowing() && mLauncher.getEditMode() != EditMode.APPWIDGET_ADD 
	         						&& mLauncher.getEditMode() != EditMode.FOLDER_IMPORT
	         						//AURORA-START::Fix bug #2303::Shi guiqiang::20140212
	         						&& !mLauncher.getWorkspace().getFolderOpenState()
	         						//AURORA-END::Fix bug #2303::Shi guiqiang::20140212
	         						&& mLauncher.getTransModeEnum() == TransModeEnum.NONE
	         						&& !getIsMovingInArea()) {
//	          					mResponsePintchEvent.AddWidgetModeEnter();
//	         					restorePintchHotseatVar();
	         					mLauncher.openMenu();
	         				}
	         			} else if (Delta_Dist <= -threshold2) {
	         				if (mLauncher.getEditMode() == EditMode.APPWIDGET_ADD) {
	      	   				mResponsePintchEvent.AddWidgetModeExit();
	      	   				restorePintchHotseatVar();
	         				}
	         			}
         			}
//          			return true;
          		}
          		//AURORA-START::Fix bug #116::Shi guiqiang::20131103
          		if (mTextView != null) {
          		//AURORA-START::change animator for page navigation::Shi guiqiang::20140114
          			if (mTextView.getAlpha() != 0) {
          				//AURORA-START::Fix bug #2669::Shi guiqiang::20140226
          				mPageIndicator = mLauncher.getPageIndicator();
          				if (mPageIndicator != null) {
          					mPageIndicator.dismissPageNavigation();
          				}
          				//AURORA-END::Fix bug #2669::Shi guiqiang::20140226
          			}
          		//AURORA-END::change animator for page navigation::Shi guiqiang::20140114
          		}
          		//AURORA-END::Fix bug #219::Shi guiqiang::20131103
            }
//AURORA-END::customized widget::Shi guiqiang::20131017
          	
			//AURORA-START::App Index::Shi guiqiang::20140111
          	if (mLauncher.getAnimationFlag()) {
          		return false;
        	}
          	if (preventTouchEvent) {
          		return false;
          	}
          //Aurora <xiangzx> <2015-11-17> begin add for bug 17204
          	if(mLauncher.getEditMode() == EditMode.CLASSIFICATE_ICONS){
          		 return false;
          	}
          //Aurora <xiangzx> <2015-11-17> end add for bug 17204	
          	acquireAppIndexMovementVelocityTracker(ev);
          	if (getRespondAppIndexFlag()) {
          		if (mLauncher.getEditMode() == EditMode.QUICK_INDEX) {
          			return true;
          		}
          		final int action = ev.getAction();
          		//X_end = ev.getX(); //ask shiguiqiang why write it?!
          		float distance = X_end - X_start;

          		getControllObject();
        		
          		switch (action) {
				case MotionEvent.ACTION_DOWN:
					mActivePointerId = ev.getPointerId(0);
	            	if (!mAppSearchImageView.getScroller().isFinished()) {
	            		mAppSearchImageView.getScroller().abortAnimation();
	            	}
	            	if (!mHotseat.getScroller().isFinished()) {
	            		mHotseat.getScroller().abortAnimation();
	            	}
	            	if (!mAppIndexContentView.getScroller().isFinished()) {
	            		mAppIndexContentView.getScroller().abortAnimation();
	            	}
	            	
					break;
					
				case MotionEvent.ACTION_MOVE:
					if (isHomeOrBackPress()){
						return true;
					}
					if (Math.abs(distance) <= windowWidth) {
						X_end = ev.getX();
						float alpha = Math.abs(X_end - X_start)/windowWidth;
						Log.v(TAG,"prevent move_up shake,distance="+distance);
						if (distance >= 0) {
							//Scroll right
							responseRightScroll(distance, alpha);
						} else if (distance < 0) {
							//Scroll left
							responseLeftScroll(distance, alpha);
						}
					}
					break;
					
				case MotionEvent.ACTION_UP:
					if (isHomeOrBackPress()){
							return true;
					}
	            	getControllObject();
	            	int mMaximumVelocity = 20000;
	            	final int activePointerId = mActivePointerId;
	                appVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
	                int velocityX = (int) appVelocityTracker.getXVelocity(activePointerId);
	                final int deltaX = (int) (X_end - mDownMotionX); 
	                velocityX = Math.abs(velocityX);
	                velocityX = Math.max(MIN_FLING_VELOCITY, velocityX);
					int duration = 2 * Math.round(1000 * Math.abs((Math.abs(X_end) - windowWidth) / velocityX));
					
					duration = duration <= 100? 100:duration;
					duration = duration >= 150? 300:duration;
	            	float finalDistance = X_end - X_start;
	            	responseActionUpEvent(finalDistance, duration);
	            	//AURORA-START::Fix bug #2142::Shi guiqiang::20140212
	            	setIsMovingInArea(false);
	        		//AURORA-END::Fix bug #2142::Shi guiqiang::20140212
	            	releaseAppMovementVelocityTracker();
	            	X_start = X_end = 0;
	            	isReadyToMove = isAreaToMove = false;
	            	break;

				case MotionEvent.ACTION_CANCEL:
					//fix scroll hotseat and touch app list more than 2 fingers and  will cause it 
					if(ev.getPointerCount()>2 && (mLauncher.getTransModeEnum() == TransModeEnum.APPS_INDEX)){
						exitAppIndexModeWithTime(50, true);
						if (mAppSearchImageView.getTranslationX() != windowWidth) {
							mAppSearchImageView.setTranslationX(windowWidth);
						}
						X_start = X_end = 0;
						isReadyToMove = isAreaToMove = false;
						return false;
					}
					finalDistance = X_end - X_start;
					if (mLauncher.getTransModeEnum() == TransModeEnum.NONE) {
						if (finalDistance > 0) {
							exitVoiceModeWithTime(50, true);
						} else {
							exitAppIndexModeWithTime(50, true);
						}
						if (mVoiceButton.getTranslationX() != (-1)*windowWidth) {
							mVoiceButton.setTranslationX((-1)*windowWidth);
						}
						if (mAppSearchImageView.getTranslationX() != windowWidth) {
							mAppSearchImageView.setTranslationX(windowWidth);
						}
					}
					X_start = X_end = 0;
					isReadyToMove = isAreaToMove = false;
					break;

				default:
					break;
				}
          		return true;	//keep on processing the touch action
          	}
          	//AURORA-END::App Index::Shi guiqiang::20140111
          	
            return false;
        }
        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Remember where the motion event started
            mMotionDownX = dragLayerX;
            mMotionDownY = dragLayerY;

            if (((dragLayerX < mScrollZone) || (dragLayerX > mScrollView.getWidth() - mScrollZone))&& dragLayerY < mScrollZoneY) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            handleMoveEvent(dragLayerX, dragLayerY);
            break;
        case MotionEvent.ACTION_UP:
            // Ensure that we've processed a move event at the current pointer location.
            handleMoveEvent(dragLayerX, dragLayerY);
            mHandler.removeCallbacks(mScrollRunnable);
            // Aurora <haojj> <2013-10-1> add for TAG begin
            mHandler.removeCallbacks(mFolderDelayCloseRunnable);
            // Aurora <haojj> <2013-10-1> end
            if (mDragging) {
                PointF vec = isFlingingToDelete(mDragObject.dragSource);
                if (vec != null) {
                	//Toast.makeText(mLauncher, "onTouchEvent Fling to delete", Toast.LENGTH_SHORT).show();
                    dropOnFlingToDeleteTarget(dragLayerX, dragLayerY, vec);
                } else {
                    drop(dragLayerX, dragLayerY);
                }
            }
            endDrag();
            break;
        case MotionEvent.ACTION_CANCEL:
            mHandler.removeCallbacks(mScrollRunnable);
            // Aurora <haojj> <2013-10-1> add for TAG begin
            mHandler.removeCallbacks(mFolderDelayCloseRunnable);
            // Aurora <haojj> <2013-10-1> end
            cancelDrag();
            break;
        }

        return true;
    }

    /**
     * Determines whether the user flung the current item to delete it.
     *
     * @return the vector at which the item was flung, or null if no fling was detected.
     */
    private PointF isFlingingToDelete(DragSource source) {
        if (mFlingToDeleteDropTarget == null) return null;
        if (!source.supportsFlingToDelete()) return null;

        ViewConfiguration config = ViewConfiguration.get(mLauncher);
        mVelocityTracker.computeCurrentVelocity(1000, config.getScaledMaximumFlingVelocity());

        if (mVelocityTracker.getYVelocity() < mFlingToDeleteThresholdVelocity) {
            // Do a quick dot product test to ensure that we are flinging upwards
            PointF vel = new PointF(mVelocityTracker.getXVelocity(),
                    mVelocityTracker.getYVelocity());
            PointF upVec = new PointF(0f, -1f);
            float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y)) /
                    (vel.length() * upVec.length()));
            if (theta <= Math.toRadians(MAX_FLING_DEGREES)) {
                return vel;
            }
        }
        return null;
    }

    private void dropOnFlingToDeleteTarget(float x, float y, PointF vel) {
        final int[] coordinates = mCoordinatesTemp;

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        // Clean up dragging on the target if it's not the current fling delete target otherwise,
        // start dragging to it.
        if (mLastDropTarget != null && mFlingToDeleteDropTarget != mLastDropTarget) {
            mLastDropTarget.onDragExit(mDragObject);
        }

        // Drop onto the fling-to-delete target
        boolean accepted = false;
        mFlingToDeleteDropTarget.onDragEnter(mDragObject);
        // We must set dragComplete to true _only_ after we "enter" the fling-to-delete target for
        // "drop"
        mDragObject.dragComplete = true;
        mFlingToDeleteDropTarget.onDragExit(mDragObject);
        if (mFlingToDeleteDropTarget.acceptDrop(mDragObject)) {
            mFlingToDeleteDropTarget.onFlingToDelete(mDragObject, mDragObject.x, mDragObject.y,
                    vel);
            accepted = true;
        }
        
        // Aurora <haojj> <2013-10-8> add for 当支持FlingToDelete的时候要采用这个方法 begin
        if((mDragObject.dragSource instanceof Folder)){
        	accepted = false;
        }
        // Aurora <haojj> <2013-10-8> end

		LauncherApplication.logVulcan.print("dropOnFlingToDeleteTarget: to call onDropCompleted ,mDragObject.dragSource = " + mDragObject.dragSource + ",accepted = " + accepted);
        mDragObject.dragSource.onDropCompleted((View) mFlingToDeleteDropTarget, mDragObject, true,
                accepted);
		return;
    }

    private void drop(float x, float y) {
        final int[] coordinates = mCoordinatesTemp;
        final DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        boolean accepted = false;
        Log.d("vulcan-80","drop: dropTarget = " + dropTarget);
		if (dropTarget != null) {
			mDragObject.dragComplete = true;
			dropTarget.onDragExit(mDragObject);
			if (dropTarget.acceptDrop(mDragObject)) {
				Log.d("vulcan-80", "drop: acceptDrop = true, mDragObject = " + mDragObject);
				dropTarget.onDrop(mDragObject);
				accepted = true;
			} else {
				Log.d("vulcan-80", "drop: acceptDrop = false, mDragObject = " + mDragObject);
			}
			// Aurora <haojj> <2013-9-30> add for 如果是系统应该则删除不掉，回到原来的位置 begin
			if ((mDragObject.dragSource instanceof Folder)
					&& ((dropTarget instanceof DeleteDropTarget))) {
				accepted = false;

				ItemInfo item = (ItemInfo) mDragObject.dragInfo;
				if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
					accepted = true;
				}

				// 如果是拖动到删除区域并且不是shortcut，为了避免动画完成前就出现view，我们应该不调用onDropComplete
				if (!accepted)
					return;
			}
			// Aurora <haojj> <2013-9-30> end
		}

		LauncherApplication.logVulcan.print("drop: to call onDropCompleted ,mDragObject.dragSource = " + mDragObject.dragSource + ",accepted = " + accepted);
        mDragObject.dragSource.onDropCompleted((View) dropTarget, mDragObject, false, accepted);
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i=count-1; i>=0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;

            target.getHitRect(r);

            // Convert the hit rect to DragLayer coordinates
            target.getLocationInDragLayer(dropCoordinates);
            // Aurora <haojj> <2013-9-29> add for TAG begin
            if (!(target instanceof Folder || target instanceof FolderEditText || target instanceof FolderEmptyDropTarget)){
            	r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
            }
            // Aurora <haojj> <2013-9-29> end

            mDragObject.x = x;
            mDragObject.y = y;
        	
            if (r.contains(x, y)) {
                DropTarget delegate = target.getDropTargetDelegate(mDragObject);
                if (delegate != null) {
                    target = delegate;
                    target.getLocationInDragLayer(dropCoordinates);
                }
                
                // Make dropCoordinates relative to the DropTarget
                dropCoordinates[0] = x - dropCoordinates[0];
                dropCoordinates[1] = y - dropCoordinates[1];

                return target;
            }
        }
        return null;
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    // Aurora <haojj> <2013-9-23> add for folder scroll begin
    public void setFolderDragScoller(DragScroller scroller) {
        mFolderDragScroller = scroller;
    }
	// Aurora <haojj> <2013-9-23> end
    
    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

    /**
     * Sets the current fling-to-delete drop target.
     */
    public void setFlingToDeleteDropTarget(DropTarget target) {
        mFlingToDeleteDropTarget = target;
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Set which view scrolls for touch events near the edge of the screen.
     */
    public void setScrollView(View v) {
        mScrollView = v;
    }

    DragView getDragView() {
        return mDragObject.dragView;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else {
                    mDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
                mDistanceSinceScroll = 0;
                mDragScroller.onExitScrollArea();
                mLauncher.getDragLayer().onExitScrollArea();

                /*if (isDragging()) {
                    // Force an update so that we can requeue the scroller if necessary
                    forceMoveEvent();
                }*/
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }
    
    // Aurora <haojj> <2013-9-23> add for folder scroll begin
    private class FolderScrollRunnable implements Runnable {
        private int mDirection;

        FolderScrollRunnable() {
        }

        public void run() {
            if (mFolderDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                	mFolderDragScroller.scrollLeft();
                } else {
                	mFolderDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
                mDistanceSinceScroll = 0;
                mFolderDragScroller.onExitScrollArea();

//                if (isDragging()) {
//                    // Force an update so that we can requeue the scroller if necessary
//                    forceMoveEvent();
//                }
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }
	// Aurora <haojj> <2013-9-23> end
    
    // Aurora <haojj> <2013-10-1> add for 拖动到删除框的延迟处理 begin
    private class FolderDelayCloseRunnable implements Runnable {

    	FolderDelayCloseRunnable() {
        }

        public void run() {
			if(mLastDropTarget != null){
				if(mLastDropTarget instanceof FolderEditText || mLastDropTarget instanceof FolderEmptyDropTarget){
					mLauncher.closeFolder();
				}
			}
        }
    }
	// Aurora <haojj> <2013-10-1> end
    
//AURORA-START::customized widget::Shi guiqiang::20131017 
    public void registPintch(ResponsePintchEvent responsePintchEvent) {
    	mResponsePintchEvent = responsePintchEvent;
    }
//AURORA-END::customized widget::Shi guiqiang::20131017
    
//AURORA-START::Fix bug #116::Shi guiqiang::20131103
	public void setDragControllertextView(TextView textView) {
		mTextView = textView;
	}
//AURORA-END::Fix bug #219::Shi guiqiang::20131103
	
	
	
	//AURORA-START::App Index::Shi guiqiang::20140111	
	public void setAppSearchViewContext(AppSearchImageView mView) {
		mAppSearchImageView = mView;
	}
	
	public void setAppContentViewContext(QuickIndexLayout mView) {
		mAppIndexContentView = mView;
	}
	
	public void setRespondAppIndexFlag(boolean b) {
		respondAppIndex = b;
	}
	
	public boolean getRespondAppIndexFlag() {
		return respondAppIndex;
	}
	
	public void acquireAppIndexMovementVelocityTracker(MotionEvent ev) {
		if (appVelocityTracker == null) {
            appVelocityTracker = VelocityTracker.obtain();
        }
        appVelocityTracker.addMovement(ev);
	}
	
	public void releaseAppMovementVelocityTracker() {
        if (appVelocityTracker != null) {
            appVelocityTracker.recycle();
            appVelocityTracker = null;
        }
    }
	
	public void startToScrollAppSearchView(int startX, int endX, int duration) {
		int appSearchDelta = startX - endX;
		mAppSearchImageView.getScroller().startScroll(startX, 0, -appSearchDelta, 0, duration);
		mAppSearchImageView.invalidate();
	}
	
	public void startToScrollHotseatView(int startX, int dX, int duration) {
		//mLauncher.getHotseat().setLayerType(View.LAYER_TYPE_HARDWARE, null);
		//mLauncher.getHotseat().buildLayer();
		mLauncher.getHotseat().getScroller().startScroll(startX, 0, dX, 0, duration);
		mLauncher.getHotseat().invalidate();
	}

	/**expand it*/
	public void startToScrollAppContentView(int startY, int endY, int duration,boolean direction) {
		int appIndexContentDelta = startY - endY;
		mAppIndexContentView.setLayerType(View.LAYER_TYPE_HARDWARE, null); //ask shiguiqiang why not build hardware
		mAppIndexContentView.buildLayer();
		mAppIndexContentView.setDirection(direction);
		mAppIndexContentView.setQuickIndexLayoutHeight(appIndexContentDelta);
		mAppIndexContentView.getScroller().startScroll(0, startY, 0, -appIndexContentDelta, duration);
		mAppIndexContentView.invalidate();
	 }
	
	public void responseRightScroll(float distance, float alpha) {
		mHotseat.setLayerType(View.LAYER_TYPE_HARDWARE, null); //ask shiguiqiang why not build hardware
		mHotseat.buildLayer();
		if (mLauncher.getTransModeEnum() != TransModeEnum.VOICE) {
			if (mLauncher.getTransModeEnum() == TransModeEnum.APPS_INDEX) {
				mAppSearchImageView.setTranslationX((windowWidth - appSearchWidth)/2 + distance/1.5f);
				mHotseat.scrollTo((int)(windowWidth - distance), 0);
			} else {
				if(!voiceEnable || !mLauncher.getVoicePackageSuccess()){
					if (mHotseat.getTranslationX() < 0) {
					mAppSearchImageView.setTranslationX(windowWidth + distance/2);
        			mHotseat.scrollTo((-1)*(int)(distance >= 0? 0:distance), 0);
        			Log.e("linp", "just right scroll with app page");
        		}
				}else{
				     /**Hazel start noted it will support voice in future*/
					mAppSearchImageView.setTranslationX(windowWidth + distance/2);
					
					mHotseat.scrollTo((-1)*(int)(distance), 0);
					
					mLauncher.setVoiceButtonVisible(true);
	            	mLauncher.updateMarginForVoiceOutLine(false);
					mVoiceButton.setTranslationX(-mLauncher.getWorkspace().getWidth() + distance/2);	//shigq set distance/2 fro test 20140327
					
					synchronizedWorkspaceChildAlpha(distance /windowWidth);
					mImageLayerMask.setAlpha(distance/windowWidth);
				}
			}
		}
	}
	
	public void responseLeftScroll(float distance, float alpha) {
		mHotseat.setLayerType(View.LAYER_TYPE_HARDWARE, null); 
		mHotseat.buildLayer();
		if (mLauncher.getTransModeEnum() != TransModeEnum.APPS_INDEX) {
			if (mLauncher.getTransModeEnum() == TransModeEnum.VOICE) {
				/**Hazel start to noted it*/
				if (voiceEnable || mLauncher.getVoicePackageSuccess()) {
					mHotseat.scrollTo((-1) * (int) (windowWidth + distance), 0);
					mVoiceButton.setTranslationX(distance);
					mLauncher.setVoiceButtonVisible(true);
					mLauncher.updateMarginForVoiceOutLine(false);
				    synchronizedImageLayerMaskAlpha(distance / windowWidth);
				} else {
					Log.e("linp", "just left scroll with app page");
				}
           } else {
        		if (mHotseat.getTranslationX() != (-1)*windowWidth) {
        			mAppSearchImageView.setTranslationX((windowWidth + distance/2));
            		mHotseat.scrollTo((-1)*(int)(distance), 0);
            	}

				// Hazel add it for when snap out VoiceButton then another
				// finger touch Hotseat area will cause overlap problem
				if (voiceEnable || mLauncher.getVoicePackageSuccess()) {
					mLauncher.setVoiceButtonVisible(false);
					mLauncher.updateMarginForVoiceOutLine(true);
				}else{
					Log.e("linp", "just respones App scroller and ignore voice button");
				}
        	}
			
		}
		
	}
	
	public void responseActionUpEvent(float finalDistance, int duration) {
		if (Math.abs(finalDistance) >= (mLauncher.getWorkspace().getWidth())*START_SCROLL_THRESHOLD) {
			if (finalDistance >= 0) {
				switch (mLauncher.getTransModeEnum()) {
				case NONE:
					/**Hazel start to add in order to avoid when snap in and snap out in App Section without Voice section will cause overlap problem*/
					if(voiceEnable&&mLauncher.getVoicePackageSuccess()){
						Log.d("DEBUG", "enter VOICE--------------1");
						enterTransModeTo(TransModeEnum.VOICE, duration);
					}else{
						  restoreMiscAppIndexScroller(duration);
					}
					break;
				case APPS_INDEX:
					//exit APPS_INDEX mode and enter NONE mode
//					duration = 2 * Math.round(1000 * Math.abs((X_end -windowWidth) / velocityX));
					Log.d("DEBUG", "exit from APPS_INDEX-------------1");
					exitAppIndexModeWithTime(duration, true);
					break;
				case VOICE:
					//enter VOICE mode again if current mode is VOICE mode, this should be updated later
					//For voice test
					if (mVoiceButton.getTranslationX() != 0) {
						Log.d("DEBUG", "enter VOICE--------------2");
						enterTransModeTo(TransModeEnum.VOICE, duration);
					} else {
						Log.d("DEBUG", "VOICE Mode original---------------");
						restoreMiscVoiceScoller(0);
					}
					//For voice test
					break;
				default:
					break;
				}
			} else {
				switch (mLauncher.getTransModeEnum()) {
				case NONE:
					//enter the APPS_INDEX mode
					Log.d("DEBUG", "enter APPS_INDEX--------------1");
					hideImageLayerMask();
					showWorkspaceChildAlpha();
					enterTransModeTo(TransModeEnum.APPS_INDEX, duration);
					mLauncher.dismissWorkspaceCling(null);
					break;
				case APPS_INDEX:
					//AURORA-START::Fix bug #1924::Shi guiqiang::20140116
					//keep the APPS_INDEX mode
					if (mAppSearchImageView.getTranslationX() != (int) (windowWidth - appSearchWidth)/2) {
						Log.d("DEBUG", "enter APPS_INDEX--------------2");
						enterTransModeTo(TransModeEnum.APPS_INDEX, duration);
					}
					//AURORA-END::Fix bug #1924::Shi guiqiang::20140116
					break;
				case VOICE:
					//exit VOICE mode and enter NONE mode
					Log.d("DEBUG", "exit from VOICE-------------1");
					exitVoiceModeWithTime(duration, true);
					break;
				default:
					break;
				}
			}
		} else {
			/*
			 * Return to onrignal position
			 */
//			duration = 1 * Math.round(1000 * Math.abs((X_end - windowWidth) / velocityX));
			switch (mLauncher.getTransModeEnum()) {
			case NONE:
				//return to NONE mode
				if (finalDistance > 0) {
					//To be updated
					Log.d("DEBUG", "return to NONE from VOICE--------------1=");
					if(!voiceEnable || !mLauncher.getVoicePackageSuccess()){
						restoreMiscAppIndexScroller(duration);
					}else{
						exitVoiceModeWithTime(duration/2, true);
					}
				} else {
					Log.d("DEBUG", "return to NONE from APPS_INDEX--------------1");
					showWorkspaceChildAlpha();
					hideImageLayerMask();
					restoreMiscAppIndexScroller(duration);
				}
//				if (mVoiceButton.getTranslationX() != (-1)*windowWidth) {
//					mVoiceButton.setTranslationX((-1)*windowWidth);
//				}
				if (mAppSearchImageView.getTranslationX() != windowWidth) {
					Log.d("DEBUG", "mAppSearchImageView.setTranslationX(windowWidth)"+windowWidth);
					mAppSearchImageView.setTranslationX(windowWidth);
				}
				break;
			case APPS_INDEX:
				//return to APPS_INDEX mode
				if (mAppSearchImageView.getTranslationX() != (int) (windowWidth - appSearchWidth)/2) {
					Log.d("DEBUG", "return to APPS_INDEX--------------1");
					enterTransModeTo(TransModeEnum.APPS_INDEX, duration);
				}
				break;
			case VOICE:
				//return to VOICE mode
				if (mVoiceButton.getTranslationX() != 0) {
					Log.d("DEBUG", "return to VOICE--------------1");
					//mHotseat.set
					//enterTransModeTo(TransModeEnum.VOICE, duration);
					restoreMiscVoiceScoller(duration);
				} else {
					Log.d("DEBUG", "return to VOICE in VOICE Mode---------------");
					restoreMiscVoiceScoller(duration);
					mLauncher.updateMarginForVoiceOutLine(true);
					mLauncher.setVoiceButtonVisible(false);
				}
				break;
			default:
				break;
			}
		}
	}

	public void restoreMiscVoiceScoller(final int duration){
		int voiceButtonStarX = (int) mVoiceButton.getTranslationX();
		int voiceButtonDestination = 0;
	    final int hotSeatScrollStartX = (int) mLauncher.getHotseat().getScrollX();
		int hotseatDeltaX = (int) (windowWidth + hotSeatScrollStartX);
	       startToScrollVoiceButton(voiceButtonStarX, voiceButtonDestination, duration);
			startToScrollHotseatView(hotSeatScrollStartX, -hotseatDeltaX, duration);
	    	mLauncher.updateMarginForVoiceOutLine(true);
			mLauncher.setVoiceButtonVisible(false);
	}
	
	
	public void enterTransModeTo(TransModeEnum transModeEnum, final int duration) {
		getControllObject();
		int appSearchScrollStartX = (int) mAppSearchImageView.getTranslationX();
        final int hotSeatScrollStartX = (int) mLauncher.getHotseat().getScrollX();
    	int appContentScrollStartY = (int) mAppIndexContentView.getTranslationY();
    	
    	mLauncher.setNavigationbarStyle();
    	
    	switch (transModeEnum) {
		case APPS_INDEX:
			mLauncher.getQuickIndexFrameLayout().updateMargin(false);
			/**Hazel start to add to restore the mAppIndexContentView*/
			if(mAppIndexContentView.getAlpha() == 0){
				mAppIndexContentView.setAlpha(1);
			}
			/**remove mUpdateMargineDelayRunnable means restore Voice part if it still runing*/
			if (mUpdateMargineDelayRunnable != null) {
				mHandler.removeCallbacks(mUpdateMargineDelayRunnable);
			}
			
			mLauncher.getQuickIndexFrameLayout().getSearchLayout().setVisibility(View.VISIBLE);
			int appSearchDestination = (int) (windowWidth - appSearchWidth)/2;
//			int hotseatDestination = (int) ((-1)*windowWidth);
			int hotseatDeltaX = (int) (windowWidth - hotSeatScrollStartX);
			int appIndexContentDes = 0;
			mLauncher.setTransMode(TransModeEnum.APPS_INDEX);
			mLauncher.show(TransModeEnum.APPS_INDEX);
//			mLauncher.setGaussBlurAlphaAnimator(false, false, 0);//ask shiguiqiang why do this restore value is 300
			
			startToScrollAppSearchView(appSearchScrollStartX, appSearchDestination, duration);
	     	startToScrollHotseatView(hotSeatScrollStartX, hotseatDeltaX, duration);
        	// @贵强，为保证mAppIndexContentView是可见的，需调用该方法 from @haojj
			mLauncher.setQuickIndexListViewVisible(true);
			mLauncher.restoreLetterSideBarAlphaAndVisibility();
			startToScrollAppContentView(appContentScrollStartY, appIndexContentDes, APP_INDEX_CONTENT_DURATION,false);
			
			setVsButtonViewToOrigin();
			int AppIndexDuration =   MAX_FULL_SCREEN_DELAY+200;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mLauncher.enterFullScreenInAppindexMode(0);
				}
			}, AppIndexDuration);
			
			
			break;
		case VOICE:
			//For voice test
			hideCurrentWorkspacePage();
			showImageLayerMask();
		   if(mVoiceLayout.getChildCount()<=0){
		    	mLauncher.getVoiceContextFromActivity();
		    }	
		   mLauncher.sendBroadCastToCheckGreeting();
		   
			int voiceButtonStarX = (int) mVoiceButton.getTranslationX();
	    	int voiceLayoutStartY = (int) mVoiceLayout.getTranslationY();
	    	
			int voiceButtonDestination = 0;
			hotseatDeltaX = (int) (windowWidth + hotSeatScrollStartX);
			int voiceLayoutDes = 0;
			mLauncher.setTransMode(TransModeEnum.VOICE);
			//mLauncher.setGaussBlurAlphaAnimator(false, false, 0);
	        startToScrollVoiceButton(voiceButtonStarX, voiceButtonDestination, duration+100);
			startToScrollHotseatView(hotSeatScrollStartX, -hotseatDeltaX, duration);
			startToScrollVoiceLayout(voiceLayoutStartY, voiceLayoutDes,APP_INDEX_CONTENT_DURATION,false);
			
			setAppSearchViewToOrigin();
			mHandler.postDelayed(mUpdateMargineDelayRunnable, mVoiceLayout.getScroller().getDuration()); 
			break;

		default:
			break;
		}
	}
	
	
	public void exitTransMode() {
		if (mLauncher.getTransModeEnum() == TransModeEnum.APPS_INDEX) {
			exitAppIndexModeWithTime(300, true);
		} else if (mLauncher.getTransModeEnum() == TransModeEnum.VOICE) {
			isReadyToExitVoiceMode(0);
      		
		}
	}
	
	public void exitTransModeWithTime(int duration){
		if (mLauncher.getTransModeEnum() == TransModeEnum.APPS_INDEX) {
			exitAppIndexModeWithTime(duration, true);
		}
	}
	
	public void restoreMiscAppIndexScroller(final int duration){
		int hotSeatScrollStartX = (int) mLauncher.getHotseat().getScrollX();
		int appSearchDestination = (int) windowWidth;
		int appSearchScrollStartX = (int) mAppSearchImageView.getTranslationX();
		int hotseatDeltaX = (int) hotSeatScrollStartX;
		startToScrollAppSearchView(appSearchScrollStartX, appSearchDestination, duration);
		startToScrollHotseatView(hotSeatScrollStartX, -hotseatDeltaX, duration);
	}
	
	
	public void exitAppIndexModeWithTime(int duration, boolean direction) {
		Log.e("linp", "exitAppIndexModeWithTime");
		getControllObject();
		cancelTimerTask();
		mLauncher.restoreNavigationbarStyle();
		mLauncher.getQuickIndexFrameLayout().getSearchLayout().setVisibility(View.INVISIBLE);
		int appSearchScrollStartX = (int) mAppSearchImageView.getTranslationX();
    	int hotSeatScrollStartX = (int) mLauncher.getHotseat().getScrollX();
    	int appContentScrollStartY = (int) mAppIndexContentView.getTranslationY();
    	
    	int appSearchDestination = (int) windowWidth;
		int hotseatDeltaX = (int) hotSeatScrollStartX;
		int appIndexContentDes = (int) windowHeight;
		
		startToScrollAppSearchView(appSearchScrollStartX, appSearchDestination, duration);
		startToScrollHotseatView(hotSeatScrollStartX, -hotseatDeltaX, duration);
		startToScrollAppContentView(appContentScrollStartY, appIndexContentDes,APP_INDEX_CONTENT_DURATION,true);
		
		mLauncher.setGaussBlurAlphaAnimator(true, direction, duration);
	}
	
	public void getControllObject() {
		int currentpage = mLauncher.getWorkspace().getCurrentPage();
		cellLayout = (CellLayout) mLauncher.getWorkspace().getChildAt(currentpage);
		mHotseat = mLauncher.getHotseat();
		mPageIndicator = mLauncher.getPageIndicator();
		
		//For voice test
		mVoiceOutLine = mLauncher.getVoiceOutLine();
		mVoiceLayout = mLauncher.getVoiceLayout();
		mVoiceButton = mLauncher.getVoiceButtonImageView();
		mImageLayerMask  = mLauncher.getImageLayerMask();
		//For voice test
	
		windowWidth = mLauncher.getWorkspace().getWidth();
		windowHeight = mLauncher.getWorkspace().getHeight();
		appSearchWidth = mAppSearchImageView.getWidth();
	}
	
	//AURORA-START::Fix bug #1919::Shi guiqiang::20140117
	public boolean getIsMovingInArea() {
		return isMovingInArea;
	}
	
	public void setIsMovingInArea(boolean b) {
		isMovingInArea = b;
	}
	//AURORA-END::Fix bug #1919::Shi guiqiang::20140117

	//AURORA-END::App Index::Shi guiqiang::20140111
	
	//For voice test
	public void startToScrollVoiceButton(int startX, int endX, int duration) {
		int appIndexContentDelta = startX - endX;
		mVoiceButton.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mVoiceButton.buildLayer();
		mVoiceButton.getScroller().startScroll(startX, 0, -appIndexContentDelta, 0, duration);
		mVoiceButton.invalidate();
	}
	
	/**expand it*/
	public void startToScrollVoiceLayout(int startY, int endY, int duration,boolean direction) {
		int appIndexContentDelta = startY - endY;
		mVoiceLayout.setDirection(direction);
		mVoiceLayout.setVoiceLayoutHeight(appIndexContentDelta);
		mVoiceLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mVoiceLayout.buildLayer();
		mVoiceLayout.getScroller().startScroll(0, startY, 0, -appIndexContentDelta, duration);
		mVoiceLayout.invalidate();
	}
	
	public void exitVoiceModeWithTime(final int duration, final boolean direction) {
		getControllObject();
		cancelTimerTask();
		mLauncher.restoreNavigationbarStyle();
		//Hazel start to set TransMode to None in order to avoid when snap to another Page will cause overlap problem
		showWorkspaceChildAlpha();
		hideImageLayerMask();
		if (mUpdateMargineDelayRunnable != null) {
			mHandler.removeCallbacks(mUpdateMargineDelayRunnable);
		}
		mLauncher.updateMarginForVoiceOutLine(false);
		int voiceButtonStartX = (int) mVoiceButton.getTranslationX();
		int hotSeatScrollStartX = (int) mLauncher.getHotseat().getScrollX();
		int voiceLayoutStartY = (int) mVoiceLayout.getTranslationY();

		int voiceButtonDestination = (-1)
				* (int) mLauncher.getWorkspace().getWidth();
		int hotseatDeltaX = (-1) * (int) hotSeatScrollStartX;
		int voiceLayoutDes = (int) windowHeight;
		startToScrollVoiceButton(voiceButtonStartX, voiceButtonDestination,
					duration);
		startToScrollHotseatView(hotSeatScrollStartX, hotseatDeltaX, duration);
		startToScrollVoiceLayout(voiceLayoutStartY, voiceLayoutDes,
					APP_INDEX_CONTENT_DURATION+150,direction);
	
		mVoiceOutLine.setAlpha(0.1f);
		mLauncher.restoreMiscellaneousObject();
		if (workspaceChild != null && scrollingIndicator != null) {
			workspaceChild.setLayerType(View.LAYER_TYPE_NONE, null);
			workspaceChild.buildLayer();
			scrollingIndicator.setLayerType(View.LAYER_TYPE_NONE, null);
			scrollingIndicator.buildLayer();
		}
		
		//mLauncher.setGaussBlurAlphaAnimator(true, direction, duration);
		mLauncher.sendBroadCastToStopPlayVoice();
		//mLauncher.restoreNavigationbarBackgroundColor();
		//mLauncher.restoreSystemUiVisibility();
		
		//M:shigq fix bug #15313 start
		mLauncher.dismissBothBlurView();
		//M:shigq fix bug #15313 end
	}
	
	public void interceptEventForVoiceContentNull() {
		preventTouchEvent = false;
		if (mLauncher.getTransModeEnum() != TransModeEnum.APPS_INDEX) {
			if (X_end - X_start > 0) {
				if (!mLauncher.getVoicePackageSuccess()) {
					Log.d("DEBUG","intercept touchEvent For VoiceContent Null");
	        		preventTouchEvent = true;
	        	}
			}
		}
	}
	
	Runnable mUpdateMargineDelayRunnable = new Runnable() {
		@Override
		public void run() {
	    	mLauncher.updateMarginForVoiceOutLine(true);
			mLauncher.setVoiceButtonVisible(false);
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mVoiceOutLine.getLayoutParams();
			Log.d("DEBUG", "params.bottom = "+params.bottomMargin);
		}
	};
	
	public void setVsButtonViewToOrigin() {
		if (mVoiceButton.getTranslationX() != (-1)*windowWidth) {
			mVoiceButton.setTranslationX((-1)*windowWidth);
		}
	}
	
	public void isReadyToExitVoiceMode(int duration) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mVoiceOutLine.getLayoutParams();
		Log.d("DEBUG", "the VoiceOutLine margine = "+params.bottomMargin);
		if (mUpdateMargineDelayRunnable != null) {
			mHandler.removeCallbacks(mUpdateMargineDelayRunnable);
		}
		mLauncher.setVoiceButtonVisible(true);
		mLauncher.updateMarginForVoiceOutLine(false);
		Log.d("DEBUG", "the VoiceOutLine margine22 = "+params.bottomMargin);
		exitVoiceModeWithTime(300, true);
	}
	//For voice test
	
	public void setAppSearchViewToOrigin() {
		if (mAppSearchImageView.getTranslationX() != windowWidth) {
			mAppSearchImageView.setTranslationX(windowWidth);
		}
	}
	/**isPintchHotSeat variable for avoid double touch in Hotseat area*/
	private boolean isPintchHotSeatArea(){
		return isPintchHotSeat;
	}

	/**set isPintchHotSeat variable for avoid double touch in Hotseat area*/
	private void setIsPintchHotseatArea(boolean b){
		isPintchHotSeat = b;
	}
	
	/**restore pintch for Hotseat area*/
	private void restorePintchHotseatVar(){
			if(isPintchHotSeatArea()){
				setIsPintchHotseatArea(false);
				Log.e("linp","set PintchHotSeatArea to default!<-----false");
			}
	}
	
	/**snap Hotseat will set workspace child and ScrollingIndicator alpha when in Moving action*/
	private void synchronizedWorkspaceChildAlpha(float progress){
		progress = Math.min(progress, 1.0f);
		progress = Math.max(progress, -1.0f);
		workspaceChild = mLauncher.getWorkspace().getChildAt(mLauncher.getWorkspace().getCurrentPage());
		scrollingIndicator = mLauncher.getWorkspace().getScrollingIndicator();
		int b =(int) + Math.abs(progress);
		float alpha = 1 - Math.abs(progress);
		workspaceChild.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		workspaceChild.buildLayer();
		workspaceChild.setAlpha(alpha);
		scrollingIndicator.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		scrollingIndicator.buildLayer();
		scrollingIndicator.setAlpha(alpha);
	}
	
	/**hide Current Workspace child (set it's alpha to zero)*/
	private void hideCurrentWorkspacePage() {
		mLauncher.getWorkspace().mFadeInAdjacentScreens = false;
		View child = mLauncher.getWorkspace().getChildAt(
				mLauncher.getWorkspace().getCurrentPage());
		View scrollingIndicator = mLauncher.getWorkspace().getScrollingIndicator();
		if (child.getAlpha() != 0.0f) {
			child.setAlpha(0.0f);
			scrollingIndicator.setAlpha(0.0f);
			scrollingIndicator.setVisibility(View.INVISIBLE);
		}
	}
	
	/**show Current Workspace child (set it's alpha to 1f)*/
	private void showWorkspaceChildAlpha(){
		mLauncher.getWorkspace().mFadeInAdjacentScreens = true;
		View child = mLauncher.getWorkspace().getChildAt(mLauncher.getWorkspace().getCurrentPage());
		View scrollingIndicator = mLauncher.getWorkspace().getScrollingIndicator();
			   child.setAlpha(1.0f);
			   child.invalidate();		
			   child.requestLayout();
			   scrollingIndicator.setAlpha(1.0f);
			   scrollingIndicator.setVisibility(View.VISIBLE);
	}
	
	/**snap Hotseat will set Mask  alpha when in Moving action*/
	private  void synchronizedImageLayerMaskAlpha(float progress){
		progress = Math.min(progress, 1.0f);
		progress = Math.max(progress, -1.0f);
		float alpha1 = 1-Math.abs(progress);
		mImageLayerMask.setAlpha(alpha1);
	}
	
	/**show ImageLayer Mask*/
	private void showImageLayerMask(){
		   mImageLayerMask.setAlpha(1.0f);
	}
	
	/**hide ImageLayer Mask*/
	private void hideImageLayerMask(){
		   mImageLayerMask.setBackgroundColor(mLauncher.getResources().getColor(R.color.workspace_layer_mask));
		   mImageLayerMask.setAlpha(0f);	
		   mImageLayerMask.setVisibility(View.GONE);
	}

	/**
	 * set flag when touch hotseat in app retrieval then press home key will
	 * cause still in action_move action and finally happend overlap
	 **/
	public void setHomeOrBackPress(boolean press){
		isHomeOrBackPress = press;
	}
	
	private boolean isHomeOrBackPress(){
		return isHomeOrBackPress;
	}

	/** enter voice mode from voice print required */
	public void enterVoiceModeByVoicePrint(int status) {
		PowerManager pm = (PowerManager) mLauncher.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP,"voicewakeup_lock");
		if (mLauncher.getTransModeEnum() != TransModeEnum.VOICE) {
			final int hotSeatScrollStartX = (int) mLauncher.getHotseat()
					.getScrollX();
			if (mLauncher.getTransModeEnum() == TransModeEnum.APPS_INDEX) {
				if (mLauncher.getEditMode() == EditMode.QUICK_INDEX) {
					mLauncher.ExitQuickSearchFilterMode();
				}
				exitTransModeWithTime(0);
			}
			 if(mLauncher.getEditMode() == EditMode.APPWIDGET_ADD){
				 mLauncher.AddWidgetModeExit();
			}
			 if(mLauncher.isCustomMenuProxyShowing()){
				 mLauncher.dismissCustomMenu();
			 }
			 
			getControllObject();
			hideCurrentWorkspacePage();
			showImageLayerMask();
			mImageLayerMask.setVisibility(View.VISIBLE);
			if (mVoiceLayout.getChildCount() <= 0) {
				mLauncher.getVoiceContextFromActivity();
			}

			int voiceButtonDestination = 0;
			int voiceButtonStarX = (int) mVoiceButton.getTranslationX();
			int voiceLayoutStartY = (int) mVoiceLayout.getTranslationY();
			int hotseatDeltaX = (int) (windowWidth + hotSeatScrollStartX);

			mLauncher.setTransMode(TransModeEnum.VOICE);
			startToScrollVoiceButton(voiceButtonStarX, voiceButtonDestination,
					0);
			startToScrollHotseatView(hotSeatScrollStartX, -hotseatDeltaX, 0);
			startToScrollVoiceLayout(voiceLayoutStartY, 0, 0, false);
			mHandler.postDelayed(mUpdateMargineDelayRunnable, 0);
			mLauncher.sendBroadCastToCheckGreeting();
		} else {
			Log.e("linp", "###already voice mode!");
		}
		mLauncher.sendBroadcast(new Intent(VOICE_COLLECT_ACTION));
		// TODO
		if (status == mLauncher.LAUNCHER_TASK_FLAG_EXIST) {
			mHandler.postDelayed(new wakeLockRunnable(wakeLock), 500);
		} 

	}

	/**runnable for wake up screen and release the lock*/
	class wakeLockRunnable implements Runnable {
		PowerManager.WakeLock wakeLock;
		public wakeLockRunnable(PowerManager.WakeLock l) {
			wakeLock = l;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			wakeLock.acquire();
			releaseWakeLock(wakeLock);
		}
	}
	
	private void releaseWakeLock(PowerManager.WakeLock lock){
		if(lock.isHeld()){
			lock.release();
		}
	}

	public void scheduleTimerTask(){
		cancelTimerTask();
		restoreNavigationTask = new RestoreNavigationbarStyleTimeTask();
		timer.schedule(restoreNavigationTask, 3000);
	}
	
	public void cancelTimerTask(){
		if(timer!=null){
			if(restoreNavigationTask!=null){
				restoreNavigationTask.cancel();
			}
		}
	}
	
	
	public class RestoreNavigationbarStyleTimeTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mLauncher.setNavigationbarStyle();		
				}
			});
		}
	}
	
}

//AURORA-START::customized widget::Shi guiqiang::20131017
interface ResponsePintchEvent {
	void OnResponsePintchEvent();
	void AddWidgetModeEnter();
	void AddWidgetModeExit();
}
//AURORA-END::customized widget::Shi guiqiang::20131017
