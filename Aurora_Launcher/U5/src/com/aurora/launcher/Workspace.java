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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.aurora.gesture.AuroraGesture;
import com.aurora.launcher.FolderIcon.FolderRingAnimator;
import com.aurora.launcher.Launcher.EditMode;
import com.aurora.launcher.Launcher.TransModeEnum;
import com.aurora.launcher.LauncherSettings.Favorites;
import com.aurora.plugin.DynIconDriver;
import com.aurora.plugin.DynIconPlg;
import android.appwidget.AppWidgetProviderInfo;

/**
 * The workspace is a wide area with a wallpaper and a finite number of pages.
 * Each page contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends PagedView
        implements DropTarget, DragSource, DragScroller, View.OnTouchListener,
        DragController.DragListener, LauncherTransitionable, ViewGroup.OnHierarchyChangeListener, 
        PagedView.PageSwitchListener{
    private static final String TAG = "Launcher.Workspace";

    // Y rotation to apply to the workspace screens
    private static final float WORKSPACE_OVERSCROLL_ROTATION = 3500f;

    private static final int CHILDREN_OUTLINE_FADE_OUT_DELAY = 0;
    private static final int CHILDREN_OUTLINE_FADE_OUT_DURATION = 375;
    private static final int CHILDREN_OUTLINE_FADE_IN_DURATION = 100;

    private static final int BACKGROUND_FADE_OUT_DURATION = 350;
    private static final int ADJACENT_SCREEN_DROP_DURATION = 300;
    private static final int FLING_THRESHOLD_VELOCITY = 500;

    // These animators are used to fade the children's outlines
    private ObjectAnimator mChildrenOutlineFadeInAnimation;
    private ObjectAnimator mChildrenOutlineFadeOutAnimation;
    private float mChildrenOutlineAlpha = 0;

    // These properties refer to the background protection gradient used for AllApps and Customize
    private ValueAnimator mBackgroundFadeInAnimation;
    private ValueAnimator mBackgroundFadeOutAnimation;
    private Drawable mBackground;
    boolean mDrawBackground = true;
    private float mBackgroundAlpha = 0;
    private float mOverScrollMaxBackgroundAlpha = 0.0f;

    private float mWallpaperScrollRatio = 1.0f;
    private int mOriginalPageSpacing;

    private final WallpaperManager mWallpaperManager;
    private IBinder mWindowToken;
    private static final float WALLPAPER_SCREENS_SPAN = 2f;

    private int mDefaultPage;

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;

    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = new int[2];
    private int mDragOverX = -1;
    private int mDragOverY = -1;

    static Rect mLandscapeCellLayoutMetrics = null;
    static Rect mPortraitCellLayoutMetrics = null;

    /**
     * The CellLayout that is currently being dragged over
     */
    private CellLayout mDragTargetLayout = null;
    /**
     * The CellLayout that we will show as glowing
     */
    private CellLayout mDragOverlappingLayout = null;

    /**
     * The CellLayout which will be dropped to
     */
    private CellLayout mDropToLayout = null;

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;

    // These are temporary variables to prevent having to allocate a new object just to
    // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];
    private float[] mDragViewVisualCenter = new float[2];
    private float[] mTempDragCoordinates = new float[2];
    private float[] mTempCellLayoutCenterCoordinates = new float[2];
    private float[] mTempDragBottomRightCoordinates = new float[2];
    private Matrix mTempInverseMatrix = new Matrix();

    private SpringLoadedDragController mSpringLoadedDragController;
    private float mSpringLoadedShrinkFactor;

    private static final int DEFAULT_CELL_COUNT_X = 4;
    private static final int DEFAULT_CELL_COUNT_Y = 4;
    
    //vulcan added this code in 2014-7-31
    //actual X&Y size of workspace
    private static int mCellCountX;
    private static int mCellCountY;

    // State variable that indicates whether the pages are small (ie when you're
    // in all apps or customize mode)
	//AURORA-START:XIEJUN:MODIFY:WORKSPACE STATE
    //enum State { NORMAL, SPRING_LOADED, SMALL,FOLDER_IMPORT,WIDGET_ADD };
    enum State { NORMAL, SPRING_LOADED,  SMALL,WORKSPACE_EDIT};
	//AURORA-END:XIEJUN:MODIFY:WORKSPACE STATE
    private State mState = State.NORMAL;
    private boolean mIsSwitchingState = false;

    boolean mAnimatingViewIntoPlace = false;
    boolean mIsDragOccuring = false;
    boolean mChildrenLayersEnabled = true;

    /** Is the user is dragging an item near the edge of a page? */
    private boolean mInScrollArea = false;

    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private Bitmap mDragOutline = null;
    private final Rect mTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private int[] mTempVisiblePagesRange = new int[2];
    private float mOverscrollFade = 0;
    private boolean mOverscrollTransformsSet;
    public static final int DRAG_BITMAP_PADDING = 2;
    private boolean mWorkspaceFadeInAdjacentScreens;

    enum WallpaperVerticalOffset { TOP, MIDDLE, BOTTOM };
    int mWallpaperWidth;
    int mWallpaperHeight;
    WallpaperOffsetInterpolator mWallpaperOffset;
    boolean mUpdateWallpaperOffsetImmediately = false;
    private Runnable mDelayedResizeRunnable;
    private Runnable mDelayedSnapToPageRunnable;
    private Point mDisplaySize = new Point();
    private boolean mIsStaticWallpaper;
    private int mWallpaperTravelWidth;
    private int mSpringLoadedPageSpacing;
    private int mCameraDistance;

    // Variables relating to the creation of user folders by hovering shortcuts over shortcuts
    private static final int FOLDER_CREATION_TIMEOUT = 0;
    private static final int REORDER_TIMEOUT = 250;
    private final Alarm mFolderCreationAlarm = new Alarm();
    private final Alarm mReorderAlarm = new Alarm();
    private FolderRingAnimator mDragFolderRingAnimator = null;
    private FolderIcon mDragOverFolderIcon = null;
    private boolean mCreateUserFolderOnDrop = false;
    private boolean mAddToExistingFolderOnDrop = false;
    private DropTarget.DragEnforcer mDragEnforcer;
    private float mMaxDistanceForFolderCreation;

    // Variables relating to touch disambiguation (scrolling workspace vs. scrolling a widget)
    private float mXDown;
    private float mYDown;
    final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

    // Relating to the animation of items being dropped externally
    public static final int ANIMATE_INTO_POSITION_AND_DISAPPEAR = 0;
    public static final int ANIMATE_INTO_POSITION_AND_REMAIN = 1;
    public static final int ANIMATE_INTO_POSITION_AND_RESIZE = 2;
    public static final int COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION = 3;
    public static final int CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION = 4;

    // Related to dragging, folder creation and reordering
    private static final int DRAG_MODE_NONE = 0;
    private static final int DRAG_MODE_CREATE_FOLDER = 1;
    private static final int DRAG_MODE_ADD_TO_FOLDER = 2;
    private static final int DRAG_MODE_REORDER = 3;
    private int mDragMode = DRAG_MODE_NONE;
    private int mLastReorderX = -1;
    private int mLastReorderY = -1;

    private SparseArray<Parcelable> mSavedStates;
    private final ArrayList<Integer> mRestoredPages = new ArrayList<Integer>();

    // These variables are used for storing the initial and final values during workspace animations
    private int mSavedScrollX;
    private float mSavedRotationY;
    private float mSavedTranslationX;
    private float mCurrentScaleX;
    private float mCurrentScaleY;
    private float mCurrentRotationY;
    private float mCurrentTranslationX;
    private float mCurrentTranslationY;
    private float[] mOldTranslationXs;
    private float[] mOldTranslationYs;
    private float[] mOldScaleXs;
    private float[] mOldScaleYs;
    private float[] mOldBackgroundAlphas;
    private float[] mOldAlphas;
    private float[] mNewTranslationXs;
    private float[] mNewTranslationYs;
    private float[] mNewScaleXs;
    private float[] mNewScaleYs;
    private float[] mNewBackgroundAlphas;
    private float[] mNewAlphas;
    private float[] mNewRotationYs;
    private float mTransitionProgress;
    private AuroraGesture mAuroraGesture;
    private int hidePageIndex = -1;

    //to be deleted View
    public List<ShortcutInfo> removeViewList;

    
    
    private final Runnable mBindPages = new Runnable() {
        @Override
        public void run() {
            mLauncher.getModel().bindRemainingSynchronousPages();
        }
    };

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContentIsRefreshable = false;
        mOriginalPageSpacing = mPageSpacing;

        mDragEnforcer = new DropTarget.DragEnforcer(context);
        // With workspace, data is available straight from the get-go
        setDataIsReady();

        mContext = context;
        mLauncher = (Launcher) context;
        final Resources res = getResources();
        mWorkspaceFadeInAdjacentScreens = res.getBoolean(R.bool.config_workspaceFadeAdjacentScreens);
        mFadeInAdjacentScreens = true;
        mWallpaperManager = WallpaperManager.getInstance(context);

        int cellCountX = DEFAULT_CELL_COUNT_X;
        int cellCountY = DEFAULT_CELL_COUNT_Y;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Workspace, defStyle, 0);

        if (LauncherApplication.isScreenLarge()) {
            // Determine number of rows/columns dynamically
            // TODO: This code currently fails on tablets with an aspect ratio < 1.3.
            // Around that ratio we should make cells the same size in portrait and
            // landscape
            TypedArray actionBarSizeTypedArray =
                context.obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
            final float actionBarHeight = actionBarSizeTypedArray.getDimension(0, 0f);

            Point minDims = new Point();
            Point maxDims = new Point();
            mLauncher.getWindowManager().getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);

            cellCountX = 1;
            while (CellLayout.widthInPortrait(res, cellCountX + 1) <= minDims.x) {
                cellCountX++;
            }

            cellCountY = 1;
            while (actionBarHeight + CellLayout.heightInLandscape(res, cellCountY + 1)
                <= minDims.y) {
                cellCountY++;
            }
        }

        mSpringLoadedShrinkFactor =
            res.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f;
        mSpringLoadedPageSpacing =
                res.getDimensionPixelSize(R.dimen.workspace_spring_loaded_page_spacing);
        mCameraDistance = res.getInteger(R.integer.config_cameraDistance);

        // if the value is manually specified, use that instead
        cellCountX = a.getInt(R.styleable.Workspace_cellCountX, cellCountX);
        cellCountY = a.getInt(R.styleable.Workspace_cellCountY, cellCountY);
        mDefaultPage = a.getInt(R.styleable.Workspace_defaultScreen, 0);
        a.recycle();

        setOnHierarchyChangeListener(this);
        LauncherModel.updateWorkspaceLayoutCells(cellCountX, cellCountY);
        mCellCountX = cellCountX;
        mCellCountY = cellCountY;
        
        Log.d("vulcan-repeat","Workspace: mCellCountY = " + mCellCountY);
        
        setHapticFeedbackEnabled(false);

        initWorkspace();

        // Disable multitouch across the workspace/all apps/customize tray
        setMotionEventSplittingEnabled(true);

        // Unless otherwise specified this view is important for accessibility.
        if (getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        //xiejun add for gestrue start
        mAuroraGesture = new AuroraGesture(context);
        //xiejun gesture end
    }
    
    @Override
    protected Interpolator getScrollInterpolator() {
    	// TODO Auto-generated method stub
    	return new QuadInterpolator();
    }

    // estimate the size of a widget with spans hSpan, vSpan. return MAX_VALUE for each
    // dimension if unsuccessful
    public int[] estimateItemSize(int hSpan, int vSpan,
            ItemInfo itemInfo, boolean springLoaded) {
        int[] size = new int[2];
        if (getChildCount() > 0) {
            CellLayout cl = (CellLayout) mLauncher.getWorkspace().getChildAt(0);
            Rect r = estimateItemPosition(cl, itemInfo, 0, 0, hSpan, vSpan);
            size[0] = r.width();
            size[1] = r.height();
            if (springLoaded) {
                size[0] *= mSpringLoadedShrinkFactor;
                size[1] *= mSpringLoadedShrinkFactor;
            }
            return size;
        } else {
            size[0] = Integer.MAX_VALUE;
            size[1] = Integer.MAX_VALUE;
            return size;
        }
    }
    public Rect estimateItemPosition(CellLayout cl, ItemInfo pendingInfo,
            int hCell, int vCell, int hSpan, int vSpan) {
        Rect r = new Rect();
        cl.cellToRect(hCell, vCell, hSpan, vSpan, r);
        return r;
    }

    public void onDragStart(DragSource source, Object info, int dragAction) {
    	//AURORA-START:
    	//we need to decrease the statusbar height  when  entering fullsreen
//AURORA-START::customized widget::Shi guiqiang::20131012 
    	if (mLauncher.getEditMode() == Launcher.EditMode.APPWIDGET_ADD) {
    		//do nothing in order to keep the widget adding mode
    		mLauncher.onEnterFullScreen();
    	} else {
    		//Aurora-start:xiejun:BUG #93
    		/*ViewGroup.LayoutParams params = getLayoutParams();
	    	if (params instanceof FrameLayout.LayoutParams) {
	    		((FrameLayout.LayoutParams) params).topMargin = 75;
	    	}*/
    		Runnable runnable = mLauncher.getExitRunnable();
    		if(runnable!=null)removeCallbacks(runnable);
    		if(mExitEditModeRunnable!=null)removeCallbacks(mExitEditModeRunnable);
	    	mLauncher.onEnterFullScreen();
	    	mLauncher.enterEditMode(Launcher.EditMode.DRAG,true);
    		/*postDelayed(new Runnable() {
				@Override
				public void run() {
					mLauncher.enterEditMode(Launcher.EditMode.DRAG,true);	
				}
			}, 100);*/
	    	//Aurora-end:xiejun:BUG#93
    	}
//AURORA-END::customized widget::Shi guiqiang::20131012 
    	//AURORA-END:
        mIsDragOccuring = true;
        updateChildrenLayersEnabled(false);
        mLauncher.lockScreenOrientation();
        //setChildrenBackgroundAlphaMultipliers(1f);
        // Prevent any Un/InstallShortcutReceivers from updating the db while we are dragging
        InstallShortcutReceiver.enableInstallQueue();
        UninstallShortcutReceiver.enableUninstallQueue();
    }

    public void onDragEnd() {
        mIsDragOccuring = false;
        updateChildrenLayersEnabled(false);
      
        mLauncher.unlockScreenOrientation(false);

        // Re-enable any Un/InstallShortcutReceiver and now process any queued items
        InstallShortcutReceiver.disableAndFlushInstallQueue(getContext());
        UninstallShortcutReceiver.disableAndFlushUninstallQueue(getContext());
    }

    /**
     * Initializes various states for this workspace.
     */
    protected void initWorkspace() {
        Context context = getContext();
        mCurrentPage = mDefaultPage;
        Launcher.setScreen(mCurrentPage);
        LauncherApplication app = (LauncherApplication)context.getApplicationContext();
        mIconCache = app.getIconCache();
        setWillNotDraw(false);
        setChildrenDrawnWithCacheEnabled(true);

        final Resources res = getResources();
        //TODO:xiejun
        /*try {
            mBackground = res.getDrawable(R.drawable.apps_customize_bg);
        } catch (Resources.NotFoundException e) {
            // In this case, we will skip drawing background protection
        }*/

        mWallpaperOffset = new WallpaperOffsetInterpolator();
        Display display = mLauncher.getWindowManager().getDefaultDisplay();
        display.getSize(mDisplaySize);
        mWallpaperTravelWidth = (int) (mDisplaySize.x *
                wallpaperTravelToScreenWidthRatio(mDisplaySize.x, mDisplaySize.y));

        mMaxDistanceForFolderCreation = (0.45f * res.getDimensionPixelSize(R.dimen.app_icon_size));
        mAuroraMaxDistanceForFolderCreation = (0.55f * res.getDimensionPixelSize(R.dimen.app_icon_size));
        mFlingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY * mDensity);
        
        //vulcan added it in 2014-6-20
        //register to notify DynIconDriver that page switched
        setPageSwitchListener(this);
    }

    /*@Override
    protected int getScrollMode() {
        return SmoothPagedView.X_LARGE_MODE;
    }*/

    @Override
    public void onChildViewAdded(View parent, View child) {
    	super.onChildViewAdded(parent, child);
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        CellLayout cl = ((CellLayout) child);
        cl.setOnInterceptTouchListener(this);
        cl.setClickable(true);
        cl.setContentDescription(getContext().getString(
                R.string.workspace_description_format, getChildCount()));
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }

    protected boolean shouldDrawChild(View child) {
        final CellLayout cl = (CellLayout) child;
        return super.shouldDrawChild(child) &&
            (cl.getShortcutsAndWidgets().getAlpha() > 0 ||
             cl.getBackgroundAlpha() > 0);
    }

    /**
     * @return The open folder on the current screen, or null if there is none
     */
    Folder getOpenFolder() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        int count = dragLayer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof Folder) {
                Folder folder = (Folder) child;
                if (folder.getInfo().opened)
                    return folder;
            }
        }
        return null;
    }

    /**
	 * @return The specify folder on the current screen, or null if there is
	 *         none
	 */
	FolderIcon getFolderByContainer(long Container) {
		ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
		for (final CellLayout layoutParent : cellLayouts) {
			final ViewGroup layout = layoutParent.getShortcutsAndWidgets();
			int childCount = layout.getChildCount();
			for (int j = 0; j < childCount; j++) {
				final View view = layout.getChildAt(j);
				Object tag = view.getTag();
				if (view instanceof FolderIcon) {
					FolderIcon fi = (FolderIcon) view;
					if (fi != null) {
						FolderInfo info = fi.getFolderInfo();
						if (info.id == Container) {
							return fi;
						}
					}
				}
			}
		}
		return null;
	}
    
    
    boolean isTouchActive() {
        return mTouchState != TOUCH_STATE_REST;
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, container, screen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY,
            boolean insert) {
        if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
            if (screen < 0 || screen >= getChildCount()) {
                Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                    + " (was " + screen + "); skipping child");
                return;
            }
        }

        final CellLayout layout;
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            layout = mLauncher.getHotseat().getLayout();
            // Aurora <jialf> <2013-10-31> remove for fix bug #323 begin
            // child.setOnKeyListener(null);
            // Aurora <jialf> <2013-10-31> remove for fix bug #323 end

            // Hide folder title in the hotseat
            if (child instanceof FolderIcon) {
            	// Aurora <jialf> <2013-10-11> modify for Folder swap to Dock begin
                ((FolderIcon) child).setTextVisible(true);
            	// Aurora <jialf> <2013-10-11> modify for Folder swap to Dock end
            }

            if (screen < 0) {
                screen = mLauncher.getHotseat().getOrderInHotseat(x, y);
            } else {
                // Note: We do this to ensure that the hotseat is always laid out in the orientation
                // of the hotseat in order regardless of which orientation they were added
                x = mLauncher.getHotseat().getCellXFromOrder(screen);
                y = mLauncher.getHotseat().getCellYFromOrder(screen);
            }
        } else {
            // Show folder title if not in the hotseat
            if (child instanceof FolderIcon) {
                ((FolderIcon) child).setTextVisible(true);
            }

            layout = (CellLayout) getChildAt(screen);
            // Aurora <jialf> <2013-10-31> remove for fix bug #323 begin
            // child.setOnKeyListener(new IconKeyEventListener());
            // Aurora <jialf> <2013-10-31> remove for fix bug #323 end
        }

        LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        if (spanX < 0 && spanY < 0) {
            lp.isLockedToGrid = false;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        int childId = LauncherModel.getCellLayoutChildId(container, screen, x, y, spanX, spanY);
        boolean markCellsAsOccupied = !(child instanceof Folder);
        if (!layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
            // TODO: This branch occurs when the workspace is adding views
            // outside of the defined grid
            // maybe we should be deleting these items from the LauncherModel?
            Log.w(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout");
        }

        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }
        
		//AURORA-START::Customized widget::Shi guiqiang::20131023
        if (mLauncher.getEditMode() == EditMode.APPWIDGET_ADD) {
        	int count=this.getChildCount();
			View itemcontainer = this.getChildAt(count - 1);
			if(itemcontainer instanceof CellLayout){
				if((((CellLayout) itemcontainer).getShortcutsAndWidgets().getChildCount()) > 0){
					//AURORA-START::Fix Bug #2761::Shi guiqiang::20140305
					if (!mScroller.isFinished()) {
						postDelayed(mCreatNewPageRunnable, mScroller.getDuration());
					} else {
						mLauncher.createNewPage();
					}
					//AURORA-END::Fix Bug #2761::Shi guiqiang::20140305
				}
			}
        }
		//AURORA-END::Customized widget::Shi guiqiang::20131023
        
    }
    //add by xiexiujie for calender plugin icon start 10.13
	void addCalenderIconInScreen(View child, long container, int screen, int x,
			int y, int spanX, int spanY, boolean insert) {

		if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			if (screen < 0 || screen >= getChildCount()) {

				return;
			}
		}

		final CellLayout layout;
		if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			
			layout = mLauncher.getHotseat().getLayout();

			// Hide folder title in the hotseat
			if (child instanceof FolderIcon) {

				((FolderIcon) child).setTextVisible(true);

			}

			if (screen < 0) {
				screen = mLauncher.getHotseat().getOrderInHotseat(x, y);
			} else {				
				x = mLauncher.getHotseat().getCellXFromOrder(screen);
				y = mLauncher.getHotseat().getCellYFromOrder(screen);
			}
		} else {
		
			if (child instanceof FolderIcon) {
				((FolderIcon) child).setTextVisible(true);
			}

			layout = (CellLayout) getChildAt(screen);

		}

		LayoutParams genericLp = child.getLayoutParams();
		CellLayout.LayoutParams lp;
		if (genericLp == null
				|| !(genericLp instanceof CellLayout.LayoutParams)) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
			
		} else {
			if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
				lp = (CellLayout.LayoutParams) genericLp;
				lp.cellX = x;
				lp.cellY = y;
				lp.cellHSpan = spanX;
				lp.cellVSpan = spanY;

			} else {
				lp = (CellLayout.LayoutParams) genericLp;
				lp.cellX = x;
				lp.cellY = y;
				lp.cellHSpan = spanX;
				lp.cellVSpan = spanY;
				
			}

		}

		if (spanX < 0 && spanY < 0) {
			lp.isLockedToGrid = false;
		}

		
		int childId = LauncherModel.getCellLayoutChildId(container, screen, x,
				y, spanX, spanY);

		
		boolean markCellsAsOccupied = !(child instanceof Folder);

		layout.addWorkSpaceCalenderViewToCellLayout(child, -1, childId, lp,
				markCellsAsOccupied);

	}
	  //add by xiexiujie for calender plugin icon end 10.13
    /**
     * Check if the point (x, y) hits a given page.
     */
    private boolean hitsPage(int index, float x, float y) {
        final View page = getChildAt(index);
        if (page != null) {
            float[] localXY = { x, y };
            mapPointFromSelfToChild(page, localXY);
            return (localXY[0] >= 0 && localXY[0] < page.getWidth()
                    && localXY[1] >= 0 && localXY[1] < page.getHeight());
        }
        return false;
    }

    @Override
    protected boolean hitsPreviousPage(float x, float y) {
        // mNextPage is set to INVALID_PAGE whenever we are stationary.
        // Calculating "next page" this way ensures that you scroll to whatever page you tap on
        final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;

        // Only allow tap to next page on large devices, where there's significant margin outside
        // the active workspace
        return LauncherApplication.isScreenLarge() && hitsPage(current - 1, x, y);
    }

    @Override
    protected boolean hitsNextPage(float x, float y) {
        // mNextPage is set to INVALID_PAGE whenever we are stationary.
        // Calculating "next page" this way ensures that you scroll to whatever page you tap on
        final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;

        // Only allow tap to next page on large devices, where there's significant margin outside
        // the active workspace
        return LauncherApplication.isScreenLarge() && hitsPage(current + 1, x, y);
    }

    private boolean mDropTargetTag = false;
    
    public void setmDropTargetTag(boolean dropTargetTag) {
		this.mDropTargetTag = dropTargetTag;
	}

	/**
     * Called directly from a CellLayout (not by the framework), after we've been added as a
     * listener via setOnInterceptTouchEventListener(). This allows us to tell the CellLayout
     * that it should intercept touch events, which is not something that is normally supported.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // return (isSmall() || !isFinishedSwitchingState());
    	
    	return mDropTargetTag;
    }

    public boolean isSwitchingState() {
        return mIsSwitchingState;
    }

    /** This differs from isSwitchingState in that we take into account how far the transition
     *  has completed. */
    public boolean isFinishedSwitchingState() {
        return !mIsSwitchingState || (mTransitionProgress > 0.5f);
    }

    protected void onWindowVisibilityChanged (int visibility) {
        mLauncher.onWindowVisibilityChanged(visibility);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        //if (isSmall() || !isFinishedSwitchingState()) {
    	if (isSmall()) {
            // when the home screens are shrunken, shouldn't allow side-scrolling
            return false;
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mLauncher.getTransModeEnum()==TransModeEnum.VOICE){
        	return true;
        }
    	switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mXDown = ev.getX();
            mYDown = ev.getY();
            break;
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_REST) {
                final CellLayout currentPage = (CellLayout) getChildAt(mCurrentPage);
                if (!currentPage.lastDownOnOccupiedCell()) {
                    onWallpaperTap(ev);
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
    	if(mLauncher.getTransModeEnum()==TransModeEnum.VOICE){
        	return true;
        }
		return super.onTouchEvent(ev);
	}

	protected void reinflateWidgetsIfNecessary() {
        final int clCount = getChildCount();
        for (int i = 0; i < clCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            ShortcutAndWidgetContainer swc = cl.getShortcutsAndWidgets();
            final int itemCount = swc.getChildCount();
            for (int j = 0; j < itemCount; j++) {
                View v = swc.getChildAt(j);

                if (v.getTag() instanceof LauncherAppWidgetInfo) {
                    LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) v.getTag();
                    LauncherAppWidgetHostView lahv = (LauncherAppWidgetHostView) info.hostView;
                    if (lahv != null && lahv.orientationChangedSincedInflation()) {
                        mLauncher.removeAppWidget(info);
                        // Remove the current widget which is inflated with the wrong orientation
                        cl.removeView(lahv);
                        mLauncher.bindAppWidget(info);
                    }
                }
            }
        }
    }

    @Override
    protected void determineScrollingStart(MotionEvent ev) {
       // if (isSmall()) return;
       // if (!isFinishedSwitchingState()) return;

        float deltaX = Math.abs(ev.getX() - mXDown);
        float deltaY = Math.abs(ev.getY() - mYDown);

        if (Float.compare(deltaX, 0f) == 0) return;

        float slope = deltaY / deltaX;
        float theta = (float) Math.atan(slope);

        if (deltaX > mTouchSlop || deltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        if (theta > MAX_SWIPE_ANGLE) {
            // Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the workspace
        	/*if(this.mState==Workspace.State.NORMAL&&mLauncher.isEnableExpandStatusBar()){
        		mAuroraGesture.gestureSwip(mXDown, mYDown, ev.getX(), ev.getY());
        	}*/
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
            // Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE, we want to
            // increase the touch slop to make it harder to begin scrolling the workspace. This
            // results in vertically scrolling widgets to more easily. The higher the angle, the
            // more we increase touch slop.
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float)
                    Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            // Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything special
            super.determineScrollingStart(ev);
        }
    }

    @Override
    public boolean expandStatusBar(MotionEvent ev) {
    	return mAuroraGesture.gestureSwip(mXDown, mYDown, ev.getX(), ev.getY());
    }
    
    @Override
    public boolean isDealGesture(MotionEvent ev) {
    	
    	float deltaX = Math.abs(ev.getX() - mXDown);
        float deltaY = Math.abs(ev.getY() - mYDown);

        if (Float.compare(deltaX, 0f) == 0) return false;

        float slope = deltaY / deltaX;
        float theta = (float) Math.atan(slope);
    	return theta > MAX_SWIPE_ANGLE&&this.mState==Workspace.State.NORMAL;
    }
    

    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        if (isHardwareAccelerated()) {
        	mChildrenLayersEnabled = true;
             //updateChildrenLayersEnabled(false);
        } else {
            if (mNextPage != INVALID_PAGE) {
                // we're snapping to a particular screen
                enableChildrenCache(mCurrentPage, mNextPage);
            } else {
                // this is when user is actively dragging a particular screen, they might
                // swipe it either left or right (but we won't advance by more than one screen)
                enableChildrenCache(mCurrentPage - 1, mCurrentPage + 1);
            }
        }

        // Only show page outlines as we pan if we are on large screen
        if (LauncherApplication.isScreenLarge()) {
            showOutlines();
            mIsStaticWallpaper = mWallpaperManager.getWallpaperInfo() == null;
        }

        // If we are not fading in adjacent screens, we still need to restore the alpha in case the
        // user scrolls while we are transitioning (should not affect dispatchDraw optimizations)
        /*
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); ++i) {
                ((CellLayout) getPageAt(i)).setShortcutAndWidgetAlpha(1f);
            }
        }
        */
//AURORA-START::Add the page indicator::Add::Shi guiqiang::20130908
       // ((PageIndicator)getScrollingIndicator()).setCurrentPosition(mCurrentPage);
//AURORA-END::Add the page indicator::Add::Shi guiqiang::20130908
        
//AURORA-START::Comment out for page indicator showing all the time::Comment out::Shi guiqiang::20130908
        // Show the scroll indicator as you pan the page
        //showScrollingIndicator(false);
//AURORA-END::Comment out for page indicator showing all the time::Comment out::Shi guiqiang::20130908
    }

    protected void onPageEndMoving() {
        super.onPageEndMoving();

        if (isHardwareAccelerated()) {
            //updateChildrenLayersEnabled(false);
        	mChildrenLayersEnabled = false;
        	//enableHwLayersOnVisiblePages();
        } else {
            clearChildrenCache();
        }


        /*if (mDragController.isDragging()) {
            if (isSmall()) {
                // If we are in springloaded mode, then force an event to check if the current touch
                // is under a new page (to scroll to)
                mDragController.forceMoveEvent();
            }
        } else {
            // If we are not mid-dragging, hide the page outlines if we are on a large screen
            if (LauncherApplication.isScreenLarge()) {
                hideOutlines();
            }

            // Hide the scroll indicator as you pan the page
            if (!mDragController.isDragging()) {
                hideScrollingIndicator(false);
            }
        }*/
        mOverScrollMaxBackgroundAlpha = 0.0f;

        if (mDelayedResizeRunnable != null) {
            mDelayedResizeRunnable.run();
            mDelayedResizeRunnable = null;
        }

        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
            mDelayedSnapToPageRunnable = null;
        }
//AURORA-START::Add the page indicator::Add::Shi guiqiang::20130908
       // ((PageIndicator)getScrollingIndicator()).setCurrentPosition(mCurrentPage); 
//AURORA-END::Add the page indicator::Add::Shi guiqiang::20130908
    }

    @Override
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        Launcher.setScreen(mCurrentPage);
    };
    
    //vulcan added it in 2014-6-20
    //test if there is dynamic icon in the specified layout
    private boolean celllayoutHasDynamicIcon(CellLayout layout) {
    	int childCount = layout.getShortcutsAndWidgetsChildCount();
    	Log.d("vulcan-iconop","celllayoutHasDynamicIcon: childCount: " + childCount);
    	for(int i = 0;i < childCount;i ++) {
    		if(DynIconPlg.viewIsDynamic(layout.getShortcutsAndWidgetsChildAt(i))) {
    			
    			return true;
    		}
    	}
    	return false;
    }
    
    //vulcan added it in 2014-6-20
    //test if there is dynamic icon in the hotseat layout
    public boolean hotseatHasDynamicIcon() {
		if (mLauncher != null) {
			if (mLauncher.getHotseat() != null) {
				CellLayout layout = mLauncher.getHotseat().getLayout();
				if (layout != null) {
					return celllayoutHasDynamicIcon(layout);
				}
			}
		}
		return false;
    }
    
    //vulcan added it in 2014-6-20
    //test if there is dynamic icon in the specified page of desktop
    public boolean desktopHasDynamicIcon(View page) {
    	Log.d("vulcan-iconop","desktopHasDynamicIcon: page: " + page);
		if (page != null) {
			if (page instanceof CellLayout) {
				return celllayoutHasDynamicIcon((CellLayout) page);
			}
		}
		return false;
    }

    //vulcan added it in 2014-6-20
    //notify DynIconDriver that new status of desktop and hotseat
    public void onPageSwitch(View newPage, int newPageIndex) {
    	Log.d("vulcan-iconop", "onPageSwitch:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + newPageIndex);
    	//Log.d("vulcan-iconop", "stack = " + LogWriter.StackToString(new Throwable()));
    	DynIconDriver.setHotseatSwitch(hotseatHasDynamicIcon());
    	DynIconDriver.setDesktopSwitch(desktopHasDynamicIcon(newPage));
    	Log.d("vulcan-iconop", "onPageSwitch:desktopHasDynamicIcon: " + desktopHasDynamicIcon(newPage));
    	Log.d("vulcan-iconop", "onPageSwitch:hotseatHasDynamicIcon: " + hotseatHasDynamicIcon());
    	Log.d("vulcan-iconop", "onPageSwitch:<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    	return;
    }

    // As a ratio of screen height, the total distance we want the parallax effect to span
    // horizontally
    private float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16/10f;
        final float ASPECT_RATIO_PORTRAIT = 10/16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
            (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
            (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    // The range of scroll values for Workspace
    private int getScrollRange() {
        return getChildOffset(getChildCount() - 1) - getChildOffset(0);
    }
    
    //
    private int getScrollRange2() {
        //return getChildOffset(getChildCount() - 2) - getChildOffset(0);
    	//return getChildOffset2(getChildCount() - 2) - getChildOffset2(0);
    	return getChildOffset(getChildCount() - 2) - getChildOffset(0);
    } 
    
    
    //Aurora_xiejun:start
    protected void setWallpaperDimension(boolean firstime){
    	if(firstime){
    		postDelayed(new Runnable() {
				
				@Override
				public void run() {
					setDefaultWallPaper();
		    		setFlagToLoadDefaultWorkspaceLater();
		    		setBackground(null);
				}
			}, 5000);
    	}else{
    		setWallpaperDimension();
    	}
    }
	private static final String WALLPAPER_FIRST_TIME_SET =
            "WALLPAPER_FIRST_TIME_SET";
	boolean mNeedWallPapaperLoad=false;
	private void setFlagToLoadDefaultWorkspaceLater() {
        SharedPreferences.Editor editor = mLauncher.getSharedPreferences().edit();
        editor.putBoolean(WALLPAPER_FIRST_TIME_SET, false);
        editor.commit();
    }
	public void setWallpaperLoad(boolean b){
		mNeedWallPapaperLoad=b;
	}
//Aurora_xiejun:end

    protected void setWallpaperDimension() {
    	//Aurora-start:xiejun:20131010:ID-WallPaper
    	Drawable drawable = mWallpaperManager.getFastDrawable();
        if (drawable == null) {
            return;
        }
        final int wallpaperWidth = drawable.getIntrinsicWidth();
        final int wallpaperHeight = drawable.getIntrinsicHeight();
        if(drawable!=null&&drawable instanceof BitmapDrawable){
        	Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        	if(bitmap!=null && !bitmap.isRecycled()){
        		bitmap.recycle();
        	}
        	drawable = null;
        }
        DisplayMetrics dm =   this.getResources().getDisplayMetrics(); 
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        boolean mbSingleScreenWallpaper=false;
        if (wallpaperWidth >= wallpaperHeight&&!mNeedWallPapaperLoad) {
            mbSingleScreenWallpaper = false;
        } else {
            mbSingleScreenWallpaper = true;
        }
        if(mNeedWallPapaperLoad){
        	setFlagToLoadDefaultWorkspaceLater();
        }
    	//Aurora-end:xiejun:20131009
		Point minDims = new Point();
		Point maxDims = new Point();
		Log.i("xiejun", "mbSingleScreenWallpaper=" + mbSingleScreenWallpaper);
		mLauncher.getWindowManager().getDefaultDisplay()
				.getCurrentSizeRange(minDims, maxDims);

		final int maxDim = Math.max(maxDims.x, maxDims.y);
		// final int minDim = Math.min(minDims.x, minDims.y);
		final int minDim = Math.max(minDims.x, minDims.y);
		Log.i("xiejun", "minDims=" + minDims.toString() + "  ,maxDims="
				+ maxDims.toString());

		// We need to ensure that there is enough extra space in the
		// wallpaper for the intended
		// parallax effects

		if (LauncherApplication.isScreenLarge()) {
			mWallpaperWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(
					maxDim, minDim));
			mWallpaperHeight = maxDim;
		} else {
			if(!mbSingleScreenWallpaper){
				mWallpaperWidth = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN),
						maxDim);
				mWallpaperHeight = maxDim;
			}else{
				mWallpaperWidth = minDim;
				mWallpaperHeight = maxDim;
			}
		}
		Log.i("xiejun","mWallpaperWidth="+wallpaperWidth+"  ,mWallpaperHeight="+wallpaperHeight);
		Log.i("xiejun","******************setWallpaperDimension end*********************");
		//Aurora-start:xiejun:20131010:ID-WallPaper
        new Thread("setWallpaperDimension") {
            public void run() {
            		mWallpaperManager.suggestDesiredDimensions(mWallpaperWidth, mWallpaperHeight);
            }
        }.start();
    }

	private float wallpaperOffsetForCurrentScroll() {
		// Set wallpaper offset steps (1 / (number of screens - 1))
		mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 1.0f);

		// For the purposes of computing the scrollRange and overScrollOffset,
		// we assume
		// that mLayoutScale is 1. This means that when we're in spring-loaded
		// mode,
		// there's no discrepancy between the wallpaper offset for a given page.
		float layoutScale = mLayoutScale;
		mLayoutScale = 1f;

		int scrollRange = getScrollRange(); // 滚动的跨度
		// Again, we adjust the wallpaper offset to be consistent between values
		// of mLayoutScale
		float adjustedScrollX = Math.max(0, Math.min(getScrollX(), mMaxScrollX));

		adjustedScrollX *= mWallpaperScrollRatio;
		mLayoutScale = layoutScale;
		float scrollProgress = adjustedScrollX / (float) scrollRange;

		if (LauncherApplication.isScreenLarge() && mIsStaticWallpaper) {
			// The wallpaper travel width is how far, from left to right, the
			// wallpaper will move
			// at this orientation. On tablets in portrait mode we don't move
			// all the way to the
			// edges of the wallpaper, or otherwise the parallax effect would be
			// too strong.
			int wallpaperTravelWidth = Math.min(mWallpaperTravelWidth,mWallpaperWidth);

			float offsetInDips = wallpaperTravelWidth * scrollProgress + (mWallpaperWidth - wallpaperTravelWidth) / 2; // center it
			float offset = offsetInDips / (float) mWallpaperWidth;
			return offset;
		} else {
			
			//Log.v("iht-wall", "scrollProgress22222222222:::"+scrollProgress);
			
			return scrollProgress; // iht 2014-12-03 偏移量？？？？
		}

		/*
		 * // Set wallpaper offset steps (1 / (number of screens - 1))
		 * mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() -
		 * 1), 1.0f);
		 * 
		 * // For the purposes of computing the scrollRange and
		 * overScrollOffset, we assume // that mLayoutScale is 1. This means
		 * that when we're in spring-loaded mode, // there's no discrepancy
		 * between the wallpaper offset for a given page. float layoutScale =
		 * mLayoutScale; mLayoutScale = 1f; int scrollRange = getScrollRange();
		 * 
		 * // Again, we adjust the wallpaper offset to be consistent between
		 * values of mLayoutScale float adjustedScrollX = Math.max(0,
		 * Math.min(getScrollX(), mMaxScrollX)); adjustedScrollX *=
		 * mWallpaperScrollRatio; mLayoutScale = layoutScale;
		 * 
		 * float scrollProgress = adjustedScrollX / (float) scrollRange;
		 * 
		 * if (LauncherApplication.isScreenLarge() && mIsStaticWallpaper) { //
		 * The wallpaper travel width is how far, from left to right, the
		 * wallpaper will move // at this orientation. On tablets in portrait
		 * mode we don't move all the way to the // edges of the wallpaper, or
		 * otherwise the parallax effect would be too strong. int
		 * wallpaperTravelWidth = Math.min(mWallpaperTravelWidth,
		 * mWallpaperWidth);
		 * 
		 * float offsetInDips = wallpaperTravelWidth * scrollProgress +
		 * (mWallpaperWidth - wallpaperTravelWidth) / 2; // center it float
		 * offset = offsetInDips / (float) mWallpaperWidth; return offset; }
		 * else { return scrollProgress; }
		 */
	}
    
    //Aurora_START:使用宽屏壁纸，当workspace由State.NORMAL转换到State.EDIT状态，会增加一页，背景重新定位时内容跳动，不让其跳动，并保持当前页的状态；
    //iht 2014-12-04 
    private float wallpaperOffsetForCurrentScroll(int childcunt, int scrollRange) {
    	
    	mWallpaperManager.setWallpaperOffsetSteps(1.0f / (childcunt - 1), 1.0f);
        // For the purposes of computing the scrollRange and overScrollOffset, we assume
        // that mLayoutScale is 1. This means that when we're in spring-loaded mode,
        // there's no discrepancy between the wallpaper offset for a given page.
        float layoutScale = mLayoutScale;
       
        //mLayoutScale = 1f; //在缩放状态，不应该是1f
        //int scrollRange = getScrollRange(); //滚动的跨度  

        // Again, we adjust the wallpaper offset to be consistent between values of mLayoutScale
        float adjustedScrollX = Math.max(0, Math.min(getScrollX(), mMaxScrollX));  //1420 = getScrollX() + Scrall
        
        //adjustedScrollX *= mWallpaperScrollRatio; //
        mLayoutScale = layoutScale;

        float scrollProgress = adjustedScrollX / (float) scrollRange;
        
        if (LauncherApplication.isScreenLarge() && mIsStaticWallpaper) {
            // The wallpaper travel width is how far, from left to right, the wallpaper will move
            // at this orientation. On tablets in portrait mode we don't move all the way to the
            // edges of the wallpaper, or otherwise the parallax effect would be too strong.
            int wallpaperTravelWidth = Math.min(mWallpaperTravelWidth, mWallpaperWidth);

            float offsetInDips = wallpaperTravelWidth * scrollProgress + (mWallpaperWidth - wallpaperTravelWidth) / 2; // center it
            float offset = offsetInDips / (float) mWallpaperWidth;
            return offset;
        } else {
        	
        	//Log.v("iht-wall", "scrollProgress:::"+scrollProgress);
        	
        	return scrollProgress; //iht 2014-12-03 偏移量？？？？
        }
    }
    
    
	//Aurora_START: iht 2014-12-10
	//供使用workspace.syncWallpaperOffsetWithScroll(); 
	//在workspace页数有变化时，及时更新该值，以确保在workspace处在编辑状态（增加了一页），滑动时背景不跳
    public int before_edit_pagesCount = 0;

    private void syncWallpaperOffsetWithScroll() {
    	Log.v("iht-wall", this.getChildCount()+"------------before:"+before_edit_pagesCount+"---------------current:"+getCurrentPage()+"--------"+this.mState);
        final boolean enableWallpaperEffects = isHardwareAccelerated();
        if (enableWallpaperEffects) {
        	//mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());            
        	if(this.mState.equals(State.NORMAL)){
        		
        		if(this.getChildCount() != before_edit_pagesCount){
        			postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());
						}
					}, 600);
        		}else{
        			mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());	
        		}
            }else{
            	mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll(this.getChildCount() - 1, getScrollRange2()));	
            }
        }
    }

    public void updateWallpaperOffsetImmediately() {
        mUpdateWallpaperOffsetImmediately = true;
    }

    private void updateWallpaperOffsets() {
        boolean updateNow = false;
        boolean keepUpdating = true;
        if (mUpdateWallpaperOffsetImmediately) {
            updateNow = true;
            keepUpdating = false;
            mWallpaperOffset.jumpToFinal();
            mUpdateWallpaperOffsetImmediately = false;
        } else {
            updateNow = keepUpdating = mWallpaperOffset.computeScrollOffset();
        }
        if (updateNow) {
            if (mWindowToken != null) {
                mWallpaperManager.setWallpaperOffsets(mWindowToken,
                        mWallpaperOffset.getCurrX(), mWallpaperOffset.getCurrY());
            }
        }
        if (keepUpdating) {
            invalidate();
        }
    }

    @Override
    protected void updateCurrentPageScroll() {
        super.updateCurrentPageScroll();
        computeWallpaperScrollRatio(mCurrentPage);
    }

    @Override
    protected void snapToPage(int whichPage) {
        super.snapToPage(whichPage);
        computeWallpaperScrollRatio(whichPage);
    }

    @Override
    protected void snapToPage(int whichPage, int duration) {
        super.snapToPage(whichPage, duration);
        computeWallpaperScrollRatio(whichPage);
    }

    protected void snapToPage(int whichPage, Runnable r) {
        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
        }
        mDelayedSnapToPageRunnable = r;
        snapToPage(whichPage, SLOW_PAGE_SNAP_ANIMATION_DURATION);
    }
    
    protected void snapToPage(int whichPage, int duration, Runnable r) {
        if (mDelayedSnapToPageRunnable != null) {
            mDelayedSnapToPageRunnable.run();
        }
        mDelayedSnapToPageRunnable = r;
        snapToPage(whichPage, duration);
    }

    private void computeWallpaperScrollRatio(int page) {
        // Here, we determine what the desired scroll would be with and without a layout scale,
        // and compute a ratio between the two. This allows us to adjust the wallpaper offset
        // as though there is no layout scale.
        float layoutScale = mLayoutScale;
        int scaled = getChildOffset(page) - getRelativeChildOffset(page);
        mLayoutScale = 1.0f;
        float unscaled = getChildOffset(page) - getRelativeChildOffset(page);
        mLayoutScale = layoutScale;
        if (scaled > 0) {
            mWallpaperScrollRatio = (1.0f * unscaled) / scaled;
        } else {
            mWallpaperScrollRatio = 1f;
        }
    }

    class WallpaperOffsetInterpolator {
        float mFinalHorizontalWallpaperOffset = 0.0f;
        float mFinalVerticalWallpaperOffset = 0.5f;
        float mHorizontalWallpaperOffset = 0.0f;
        float mVerticalWallpaperOffset = 0.5f;
        long mLastWallpaperOffsetUpdateTime;
        boolean mIsMovingFast;
        boolean mOverrideHorizontalCatchupConstant;
        float mHorizontalCatchupConstant = 0.35f;
        float mVerticalCatchupConstant = 0.35f;

        public WallpaperOffsetInterpolator() {
        }

        public void setOverrideHorizontalCatchupConstant(boolean override) {
            mOverrideHorizontalCatchupConstant = override;
        }

        public void setHorizontalCatchupConstant(float f) {
            mHorizontalCatchupConstant = f;
        }

        public void setVerticalCatchupConstant(float f) {
            mVerticalCatchupConstant = f;
        }

        public boolean computeScrollOffset() {
            if (Float.compare(mHorizontalWallpaperOffset, mFinalHorizontalWallpaperOffset) == 0 &&
                    Float.compare(mVerticalWallpaperOffset, mFinalVerticalWallpaperOffset) == 0) {
                mIsMovingFast = false;
                return false;
            }
            boolean isLandscape = mDisplaySize.x > mDisplaySize.y;

            long currentTime = System.currentTimeMillis();
            long timeSinceLastUpdate = currentTime - mLastWallpaperOffsetUpdateTime;
            timeSinceLastUpdate = Math.min((long) (1000/30f), timeSinceLastUpdate);
            timeSinceLastUpdate = Math.max(1L, timeSinceLastUpdate);

            float xdiff = Math.abs(mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset);
            if (!mIsMovingFast && xdiff > 0.07) {
                mIsMovingFast = true;
            }

            float fractionToCatchUpIn1MsHorizontal;
            if (mOverrideHorizontalCatchupConstant) {
                fractionToCatchUpIn1MsHorizontal = mHorizontalCatchupConstant;
            } else if (mIsMovingFast) {
                fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.5f : 0.75f;
            } else {
                // slow
                fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.27f : 0.5f;
            }
            float fractionToCatchUpIn1MsVertical = mVerticalCatchupConstant;

            fractionToCatchUpIn1MsHorizontal /= 33f;
            fractionToCatchUpIn1MsVertical /= 33f;

            final float UPDATE_THRESHOLD = 0.00001f;
            float hOffsetDelta = mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset;
            float vOffsetDelta = mFinalVerticalWallpaperOffset - mVerticalWallpaperOffset;
            boolean jumpToFinalValue = Math.abs(hOffsetDelta) < UPDATE_THRESHOLD &&
                Math.abs(vOffsetDelta) < UPDATE_THRESHOLD;

            // Don't have any lag between workspace and wallpaper on non-large devices
            if (!LauncherApplication.isScreenLarge() || jumpToFinalValue) {
                mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
                mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
            } else {
                float percentToCatchUpVertical =
                    Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsVertical);
                float percentToCatchUpHorizontal =
                    Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsHorizontal);
                mHorizontalWallpaperOffset += percentToCatchUpHorizontal * hOffsetDelta;
                mVerticalWallpaperOffset += percentToCatchUpVertical * vOffsetDelta;
            }

            mLastWallpaperOffsetUpdateTime = System.currentTimeMillis();
            return true;
        }

        public float getCurrX() {
            return mHorizontalWallpaperOffset;
        }

        public float getFinalX() {
            return mFinalHorizontalWallpaperOffset;
        }

        public float getCurrY() {
            return mVerticalWallpaperOffset;
        }

        public float getFinalY() {
            return mFinalVerticalWallpaperOffset;
        }

        public void setFinalX(float x) {
            mFinalHorizontalWallpaperOffset = Math.max(0f, Math.min(x, 1.0f));
        }

        public void setFinalY(float y) {
            mFinalVerticalWallpaperOffset = Math.max(0f, Math.min(y, 1.0f));
        }

        public void jumpToFinal() {
            mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
            mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        syncWallpaperOffsetWithScroll();
    }

    void showOutlines() {
        if (!isSmall() && !mIsSwitchingState) {
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            mChildrenOutlineFadeInAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 1.0f);
            mChildrenOutlineFadeInAnimation.setDuration(CHILDREN_OUTLINE_FADE_IN_DURATION);
            mChildrenOutlineFadeInAnimation.start();
        }
    }

    void hideOutlines() {
        if (!isSmall() && !mIsSwitchingState) {
            if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
            if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
            mChildrenOutlineFadeOutAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 0.0f);
            mChildrenOutlineFadeOutAnimation.setDuration(CHILDREN_OUTLINE_FADE_OUT_DURATION);
            mChildrenOutlineFadeOutAnimation.setStartDelay(CHILDREN_OUTLINE_FADE_OUT_DELAY);
            mChildrenOutlineFadeOutAnimation.start();
        }
    }

    public void showOutlinesTemporarily() {
        if (!mIsPageMoving && !isTouchActive()) {
            snapToPage(mCurrentPage);
        }
    }

    public void setChildrenOutlineAlpha(float alpha) {
        mChildrenOutlineAlpha = alpha;
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cl = (CellLayout) getChildAt(i);
            cl.setBackgroundAlpha(alpha);
        }
    }

    public float getChildrenOutlineAlpha() {
        return mChildrenOutlineAlpha;
    }

    void disableBackground() {
        mDrawBackground = false;
    }
    void enableBackground() {
        mDrawBackground = true;
    }

    private void animateBackgroundGradient(float finalAlpha, boolean animated) {
        if (mBackground == null) return;
        if (mBackgroundFadeInAnimation != null) {
            mBackgroundFadeInAnimation.cancel();
            mBackgroundFadeInAnimation = null;
        }
        if (mBackgroundFadeOutAnimation != null) {
            mBackgroundFadeOutAnimation.cancel();
            mBackgroundFadeOutAnimation = null;
        }
        float startAlpha = getBackgroundAlpha();
        if (finalAlpha != startAlpha) {
            if (animated) {
                mBackgroundFadeOutAnimation = LauncherAnimUtils.ofFloat(startAlpha, finalAlpha);
                mBackgroundFadeOutAnimation.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setBackgroundAlpha(((Float) animation.getAnimatedValue()).floatValue());
                    }
                });
                mBackgroundFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
                mBackgroundFadeOutAnimation.setDuration(BACKGROUND_FADE_OUT_DURATION);
                mBackgroundFadeOutAnimation.start();
            } else {
                setBackgroundAlpha(finalAlpha);
            }
        }
    }

    public void setBackgroundAlpha(float alpha) {
        if (alpha != mBackgroundAlpha) {
            mBackgroundAlpha = alpha;
            invalidate();
        }
    }

    public float getBackgroundAlpha() {
        return mBackgroundAlpha;
    }

    float backgroundAlphaInterpolator(float r) {
        float pivotA = 0.1f;
        float pivotB = 0.4f;
        if (r < pivotA) {
            return 0;
        } else if (r > pivotB) {
            return 1.0f;
        } else {
            return (r - pivotA)/(pivotB - pivotA);
        }
    }

    float overScrollBackgroundAlphaInterpolator(float r) {
        float threshold = 0.08f;

        if (r > mOverScrollMaxBackgroundAlpha) {
            mOverScrollMaxBackgroundAlpha = r;
        } else if (r < mOverScrollMaxBackgroundAlpha) {
            r = mOverScrollMaxBackgroundAlpha;
        }

        return Math.min(r / threshold, 1.0f);
    }

    private void updatePageAlphaValues(int screenCenter) {
        boolean isInOverscroll = mOverScrollX < 0 || mOverScrollX > mMaxScrollX;
        if (mWorkspaceFadeInAdjacentScreens &&
                mState == State.NORMAL &&
                !mIsSwitchingState &&
                !isInOverscroll) {
            for (int i = 0; i < getChildCount(); i++) {
                CellLayout child = (CellLayout) getChildAt(i);
                if (child != null) {
                    float scrollProgress = getScrollProgress(screenCenter, child, i);
                    float alpha = 1 - Math.abs(scrollProgress);
                    child.getShortcutsAndWidgets().setAlpha(alpha);
                    if (!mIsDragOccuring) {
                        child.setBackgroundAlphaMultiplier(
                                backgroundAlphaInterpolator(Math.abs(scrollProgress)));
                    } else {
                        child.setBackgroundAlphaMultiplier(1f);
                    }
                }
            }
        }
    }

    private void setChildrenBackgroundAlphaMultipliers(float a) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout child = (CellLayout) getChildAt(i);
            child.setBackgroundAlphaMultiplier(a);
        }
    }

    @Override
    protected void screenScrolled(int screenCenter) {
        super.screenScrolled(screenCenter);
        //updatePageAlphaValues(screenCenter);
        //enableHwLayersOnVisiblePages();

        if (mOverScrollX < 0 || mOverScrollX > mMaxScrollX) {
		   //AURORA:START:XIEJUN:MODIFY:WHEN SCRLL OVER ,WE DON'T NEED GO AHEAD;
            int index = mOverScrollX < 0 ? 0 : getChildCount() - 1;
            CellLayout cl = (CellLayout) getChildAt(index);
            int index2=-1;
            int count = getChildCount();
            CellLayout cl2=null;
            boolean positiveDirection=false;
            if(count>1){
            	if(mOverScrollX < 0){
            		index2=1;
            		positiveDirection = false;
            	}else if(mOverScrollX > mMaxScrollX){
            		index2=count-2;
            		positiveDirection = true;
            	}
            }
			if (index2 != -1)
				cl2 = (CellLayout) getChildAt(index2);
            float scrollProgress = getScrollProgress(screenCenter, cl, index);
            //cl.setOverScrollAmount(Math.abs(scrollProgress), index == 0);
            //float rotation = - WORKSPACE_OVERSCROLL_ROTATION * scrollProgress;
            //cl.setRotationY(rotation);
            float distance = - WORKSPACE_OVERSCROLL_ROTATION * scrollProgress;
            //setFadeForOverScroll(Math.abs(scrollProgress));
            
            //M:shigq change distance as distance/2 for Workspace sliding on Android5.0 platform begin
//            cl.setTranslationX(distance);
            cl.setTranslationX(distance/2);
            //M:shigq change distance as distance/2 for Workspace sliding on Android5.0 platform end
            
			if (!isEditmode() && mFadeInAdjacentScreens ) {
				if (positiveDirection) {
					cl.setAlpha((-scrollProgress + 1) * (-scrollProgress + 1)
							* (-scrollProgress + 1) * (-scrollProgress + 1));
				} else {
					cl.setAlpha((scrollProgress + 1) * (scrollProgress + 1)
							* (scrollProgress + 1) * (scrollProgress + 1));
				}
			}
            if(cl2!=null&&isEditmode()){
            	float scrollProgress2 = getScrollProgress(screenCenter, cl2, index2);
            	float distance2 = - WORKSPACE_OVERSCROLL_ROTATION * scrollProgress2;
            	
            	//M:shigq change distance as distance/2 for Workspace sliding on Android5.0 platform begin
//            	cl2.setTranslationX(distance2);
            	cl2.setTranslationX(distance2/2);
            	//M:shigq change distance as distance/2 for Workspace sliding on Android5.0 platform end
            	
            }
            if (!mOverscrollTransformsSet) {
                mOverscrollTransformsSet = true;
                cl.setCameraDistance(mDensity * mCameraDistance);
                /*cl.setPivotX(cl.getMeasuredWidth() * (index == 0 ? 0.75f : 0.25f));
                cl.setPivotY(cl.getMeasuredHeight() * 0.5f);*/
                cl.setOverscrollTransformsDirty(true);
				if (cl2 != null&&isEditmode()) {
					cl2.setCameraDistance(mDensity * mCameraDistance);
					/*cl2.setPivotX(cl2.getMeasuredWidth()
							* (index == 0 ? 0.75f : 0.25f));
					cl2.setPivotY(cl2.getMeasuredHeight() * 0.5f);*/
					cl2.setOverscrollTransformsDirty(true);
				}
            }
		//AURORA:START:XIEJUN:MODIFY:WHEN SCRLL OVER ,WE DON'T NEED GO AHEAD;
        } else {
            /*if (mOverscrollFade != 0) {
                setFadeForOverScroll(0);
            }*/
            if (mOverscrollTransformsSet) {
                mOverscrollTransformsSet = false;
                ((CellLayout) getChildAt(0)).resetOverscrollTransforms();
                ((CellLayout) getChildAt(getChildCount() - 1)).resetOverscrollTransforms();
                int count =getChildCount();
                if(count>2&&isEditmode()){
                	((CellLayout) getChildAt(1)).resetOverscrollTransforms();
                	((CellLayout) getChildAt(getChildCount() - 2)).resetOverscrollTransforms();
                }
            }
        }
    }

    @Override
    protected void overScroll(float amount) {
    	dampedOverScroll(amount);
        //acceleratedOverScroll(amount);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWindowToken = getWindowToken();
        computeScroll();
        mDragController.setWindowToken(mWindowToken);
    }

    protected void onDetachedFromWindow() {
        mWindowToken = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            mUpdateWallpaperOffsetImmediately = true;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateWallpaperOffsets();

        // Draw the background gradient if necessary
    	/*
        if (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground) {
            int alpha = (int) (mBackgroundAlpha * 255);
            mBackground.setAlpha(alpha);
            mBackground.setBounds(getScrollX(), 0, getScrollX() + getMeasuredWidth(),
                    getMeasuredHeight());
            mBackground.draw(canvas);
        }
        */

        super.onDraw(canvas);

        // Call back to LauncherModel to finish binding after the first draw
        post(mBindPages);
    }

    boolean isDrawingBackgroundGradient() {
        return (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
        }
        return false;
    }

    @Override
    public int getDescendantFocusability() {
        if (isSmall()) {
            return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
        }
        return super.getDescendantFocusability();
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                openFolder.addFocusables(views, direction);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }

    public boolean isSmall() {
        return mState == State.SMALL || mState == State.SPRING_LOADED;
    }

    void enableChildrenCache(int fromPage, int toPage) {
        if (fromPage > toPage) {
            final int temp = fromPage;
            fromPage = toPage;
            toPage = temp;
        }

        final int screenCount = getChildCount();

        fromPage = Math.max(fromPage, 0);
        toPage = Math.min(toPage, screenCount - 1);

        for (int i = fromPage; i <= toPage; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
        final int screenCount = getChildCount();
        for (int i = 0; i < screenCount; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
            // In software mode, we don't want the items to continue to be drawn into bitmaps
            if (!isHardwareAccelerated()) {
                layout.setChildrenDrawingCacheEnabled(false);
            }
        }
    }


    private void updateChildrenLayersEnabled(boolean force) {
        boolean small = mState == State.SMALL || mIsSwitchingState;
        boolean enableChildrenLayers = force || small || mAnimatingViewIntoPlace || isPageMoving();
        if (enableChildrenLayers != mChildrenLayersEnabled) {
            mChildrenLayersEnabled = enableChildrenLayers;
            if (mChildrenLayersEnabled) {
                enableHwLayersOnVisiblePages();
            } else {
                for (int i = 0; i < getPageCount(); i++) {
                    final CellLayout cl = (CellLayout) getChildAt(i);
                    cl.disableHardwareLayers();
                }
            }
        }
    }

    private void enableHwLayersOnVisiblePages() {
        if (mChildrenLayersEnabled) {
            final int screenCount = getChildCount();
            getVisiblePages(mTempVisiblePagesRange);
            int leftScreen = mTempVisiblePagesRange[0];
            int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen == rightScreen) {
                // make sure we're caching at least two pages always
                if (rightScreen < screenCount - 1) {
                    rightScreen++;
                } else if (leftScreen > 0) {
                    leftScreen--;
                }
            }
            for (int i = 0; i < screenCount; i++) {
                final CellLayout layout = (CellLayout) getChildAt(i);
                if (!(leftScreen <= i && i <= rightScreen && shouldDrawChild(layout))) {
                    layout.disableHardwareLayers();
                }
            }
            for (int i = 0; i < screenCount; i++) {
                final CellLayout layout = (CellLayout) getChildAt(i);
                if (leftScreen <= i && i <= rightScreen && shouldDrawChild(layout)) {
                    layout.enableHardwareLayers();
                }
            }
        }
    }

    public void buildPageHardwareLayers() {
        // force layers to be enabled just for the call to buildLayer
        updateChildrenLayersEnabled(true);
        if (getWindowToken() != null) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                CellLayout cl = (CellLayout) getChildAt(i);
                cl.buildHardwareLayer();
            }
        }
        updateChildrenLayersEnabled(false);
    }

    protected void onWallpaperTap(MotionEvent ev) {
        final int[] position = mTempCell;
        getLocationOnScreen(position);

        int pointerIndex = ev.getActionIndex();
        position[0] += (int) ev.getX(pointerIndex);
        position[1] += (int) ev.getY(pointerIndex);

        mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                ev.getAction() == MotionEvent.ACTION_UP
                        ? WallpaperManager.COMMAND_TAP : WallpaperManager.COMMAND_SECONDARY_TAP,
                position[0], position[1], 0, null);
    }

    /*
     * This interpolator emulates the rate at which the perceived scale of an object changes
     * as its distance from a camera increases. When this interpolator is applied to a scale
     * animation on a view, it evokes the sense that the object is shrinking due to moving away
     * from the camera.
     */
    static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
        }
    }

    /*
     * The exact reverse of ZInterpolator.
     */
    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;
        public InverseZInterpolator(float foc) {
            zInterpolator = new ZInterpolator(foc);
        }
        public float getInterpolation(float input) {
            return 1 - zInterpolator.getInterpolation(1 - input);
        }
    }

    /*
     * ZInterpolator compounded with an ease-out.
     */
    static class ZoomOutInterpolator implements TimeInterpolator {
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(0.75f);
        private final ZInterpolator zInterpolator = new ZInterpolator(0.13f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(zInterpolator.getInterpolation(input));
        }
    }

    /*
     * InvereZInterpolator compounded with an ease-out.
     */
    static class ZoomInInterpolator implements TimeInterpolator {
        private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

        public float getInterpolation(float input) {
            return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
        }
    }

    private final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();

    /*
    *
    * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
    * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
    *
    * These methods mark the appropriate pages as accepting drops (which alters their visual
    * appearance).
    *
    */
    public void onDragStartedWithItem(View v) {
        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(v, canvas, DRAG_BITMAP_PADDING);
    }

    public void onDragStartedWithItem(PendingAddItemInfo info, Bitmap b, boolean clipAlpha) {
        final Canvas canvas = new Canvas();

        int[] size = estimateItemSize(info.spanX, info.spanY, info, false);

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(b, canvas, DRAG_BITMAP_PADDING, size[0],
                size[1], clipAlpha);
    }

    public void exitWidgetResizeMode() {
        DragLayer dragLayer = mLauncher.getDragLayer();
        dragLayer.clearAllResizeFrames();
    }
    
    private void initAnimationArrays() {
        final int childCount = getChildCount();
        if (mOldTranslationXs != null&& (mOldTranslationXs.length == childCount)) return;
        mOldTranslationXs = new float[childCount];
        mOldTranslationYs = new float[childCount];
        mOldScaleXs = new float[childCount];
        mOldScaleYs = new float[childCount];
        mOldBackgroundAlphas = new float[childCount];
        mOldAlphas = new float[childCount];
        mNewTranslationXs = new float[childCount];
        mNewTranslationYs = new float[childCount];
        mNewScaleXs = new float[childCount];
        mNewScaleYs = new float[childCount];
        mNewBackgroundAlphas = new float[childCount];
        mNewAlphas = new float[childCount];
        mNewRotationYs = new float[childCount];
    }

    Animator getChangeStateAnimation(final State state, boolean animated) {
        return getChangeStateAnimation(state, animated, 0);
    }

    Animator getChangeStateAnimation(final State state, boolean animated, int delay) {
        if (mState == state) {
            return null;
        }

        // Initialize animation arrays for the first time if necessary
        initAnimationArrays();

        AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;

        // Stop any scrolling, move to the current page right away
        setCurrentPage(getNextPage());

        final State oldState = mState;
        final boolean oldStateIsNormal = (oldState == State.NORMAL);
        //final boolean oldStateIsSpringLoaded = (oldState == State.SPRING_LOADED);
        final boolean oldStateIsSpringLoaded = (oldState == State.SPRING_LOADED);
        final boolean oldStateIsSmall = (oldState == State.SMALL);
        mState = state;
        final boolean stateIsNormal = (state == State.NORMAL);
        final boolean stateIsSpringLoaded = (state == State.SPRING_LOADED);
        final boolean stateIsSmall = (state == State.SMALL);
        float finalScaleFactor = 1.0f;
        float finalBackgroundAlpha = stateIsSpringLoaded ? 1.0f : 0f;
        float translationX = 0;
        float translationY = 0;
        boolean zoomIn = true;

        if (state != State.NORMAL) {
            finalScaleFactor = mSpringLoadedShrinkFactor - (stateIsSmall ? 0.1f : 0);
            setPageSpacing(mSpringLoadedPageSpacing);
            if (oldStateIsNormal && stateIsSmall) {
                zoomIn = false;
                setLayoutScale(finalScaleFactor);
                updateChildrenLayersEnabled(false);
            } else {
                finalBackgroundAlpha = 1.0f;
                setLayoutScale(finalScaleFactor);
            }
        } else {
            setPageSpacing(mOriginalPageSpacing);
            setLayoutScale(1.0f);
        }

        final int duration = zoomIn ?
                getResources().getInteger(R.integer.config_workspaceUnshrinkTime) :
                getResources().getInteger(R.integer.config_appsCustomizeWorkspaceShrinkTime);
        for (int i = 0; i < getChildCount(); i++) {
            final CellLayout cl = (CellLayout) getChildAt(i);
            float finalAlpha = (!mWorkspaceFadeInAdjacentScreens || stateIsSpringLoaded ||
                    (i == mCurrentPage)) ? 1f : 0f;
            float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
            float initialAlpha = currentAlpha;

            // Determine the pages alpha during the state transition
            if ((oldStateIsSmall && stateIsNormal) ||
                (oldStateIsNormal && stateIsSmall)) {
                // To/from workspace - only show the current page unless the transition is not
                //                     animated and the animation end callback below doesn't run;
                //                     or, if we're in spring-loaded mode
                if (i == mCurrentPage || !animated || oldStateIsSpringLoaded) {
                    finalAlpha = 1f;
                } else {
                    initialAlpha = 0f;
                    finalAlpha = 0f;
                }
            }

            mOldAlphas[i] = initialAlpha;
            mNewAlphas[i] = finalAlpha;
            if (animated) {
                mOldTranslationXs[i] = cl.getTranslationX();
                mOldTranslationYs[i] = cl.getTranslationY();
                mOldScaleXs[i] = cl.getScaleX();
                mOldScaleYs[i] = cl.getScaleY();
                mOldBackgroundAlphas[i] = cl.getBackgroundAlpha();

                mNewTranslationXs[i] = translationX;
                mNewTranslationYs[i] = translationY;
                mNewScaleXs[i] = finalScaleFactor;
                mNewScaleYs[i] = finalScaleFactor;
                mNewBackgroundAlphas[i] = finalBackgroundAlpha;
            } else {
                cl.setTranslationX(translationX);
                cl.setTranslationY(translationY);
                cl.setScaleX(finalScaleFactor);
                cl.setScaleY(finalScaleFactor);
                cl.setBackgroundAlpha(finalBackgroundAlpha);
                //cl.setShortcutAndWidgetAlpha(finalAlpha);
            }
        }

        if (animated) {
            for (int index = 0; index < getChildCount(); index++) {
                final int i = index;
                final CellLayout cl = (CellLayout) getChildAt(i);
                float currentAlpha = cl.getShortcutsAndWidgets().getAlpha();
                if (mOldAlphas[i] == 0 && mNewAlphas[i] == 0) {
                    cl.setTranslationX(mNewTranslationXs[i]);
                    cl.setTranslationY(mNewTranslationYs[i]);
                    cl.setScaleX(mNewScaleXs[i]);
                    cl.setScaleY(mNewScaleYs[i]);
                    cl.setBackgroundAlpha(mNewBackgroundAlphas[i]);
                    //cl.setShortcutAndWidgetAlpha(mNewAlphas[i]);
                    cl.setRotationY(mNewRotationYs[i]);
                } else {
                    LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(cl);
                    a.translationX(mNewTranslationXs[i])
                        .translationY(mNewTranslationYs[i])
                        .scaleX(mNewScaleXs[i])
                        .scaleY(mNewScaleYs[i])
                        .setDuration(duration)
                        .setInterpolator(mZoomInInterpolator);
                    anim.play(a);

                    if (mOldAlphas[i] != mNewAlphas[i] || currentAlpha != mNewAlphas[i]) {
                        LauncherViewPropertyAnimator alphaAnim =
                            new LauncherViewPropertyAnimator(cl.getShortcutsAndWidgets());
                        alphaAnim.alpha(mNewAlphas[i])
                            .setDuration(duration)
                            .setInterpolator(mZoomInInterpolator);
                        anim.play(alphaAnim);
                    }
                    if (mOldBackgroundAlphas[i] != 0 ||
                            mNewBackgroundAlphas[i] != 0) {
                            ValueAnimator bgAnim = LauncherAnimUtils.ofFloat(0f, 1f).setDuration(duration);
                            bgAnim.setInterpolator(mZoomInInterpolator);
                            bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
                                    public void onAnimationUpdate(float a, float b) {
                                        cl.setBackgroundAlpha(
                                                a * mOldBackgroundAlphas[i] +
                                                b * mNewBackgroundAlphas[i]);
                                    }
                                });
                            anim.play(bgAnim);
                    }
                    /*
                    if (mOldBackgroundAlphas[i] != 0 ||
                        mNewBackgroundAlphas[i] != 0) {
                    	if(getCurrentPage()==i){
		                    ValueAnimator bgAnim = LauncherAnimUtils.ofFloat(0f, 1f).setDuration(duration);
		                    bgAnim.setInterpolator(mZoomInInterpolator);
		                    bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
		                            public void onAnimationUpdate(float a, float b) {
		                            	if(cl!=null&&cl.getBackground()!=null){
		                            		cl.getBackground().setAlpha((int)(b*255));
		                            	}	
		                            }
		                        });
		                    anim.play(bgAnim);
                    	}else{
                    		cl.getBackground().setAlpha(255);
                    	}
                    }else{
                    	if(getCurrentPage()==i){
	                    	ValueAnimator bgAnim = LauncherAnimUtils.ofFloat(1f, 0f).setDuration(duration);
	                        bgAnim.setInterpolator(mZoomInInterpolator);
	                        bgAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
	                                public void onAnimationUpdate(float a, float b) {
	                                	if((getCurrentPage()==i)&&cl!=null&&cl.getBackground()!=null){
	                                		cl.getBackground().setAlpha((int)(b*255));
	                                	}
	                                }
	                            });
	                        anim.play(bgAnim);
                    	}else{
                    		cl.getBackground().setAlpha(0);
                    	}
                    }
                    */                    
                }
            }
            //buildPageHardwareLayers();
            anim.setStartDelay(delay);
        }

       /* if (stateIsSpringLoaded) {
            // Right now we're covered by Apps Customize
            // Show the background gradient immediately, so the gradient will
            // be showing once AppsCustomize disappears
            animateBackgroundGradient(getResources().getInteger(
                    R.integer.config_appsCustomizeSpringLoadedBgAlpha) / 100f, false);
        } else {
            // Fade the background gradient away
            animateBackgroundGradient(0f, true);
        }*/
        return anim;
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        mIsSwitchingState = true;
        //cancelScrollingIndicatorAnimations();
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        mTransitionProgress = t;
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        mIsSwitchingState = false;
        mWallpaperOffset.setOverrideHorizontalCatchupConstant(false);
        //updateChildrenLayersEnabled(false);
        // The code in getChangeStateAnimation to determine initialAlpha and finalAlpha will ensure
        // ensure that only the current page is visible during (and subsequently, after) the
        // transition animation.  If fade adjacent pages is disabled, then re-enable the page
        // visibility after the transition animation.
        /*
        if (!mWorkspaceFadeInAdjacentScreens) {
            for (int i = 0; i < getChildCount(); i++) {
                final CellLayout cl = (CellLayout) getChildAt(i);
                cl.setShortcutAndWidgetAlpha(1f);
            }
        }
        */
    }

    @Override
    public View getContent() {
        return this;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
        final Rect clipRect = mTempRect;
        v.getDrawingRect(clipRect);

        boolean textVisible = false;

        destCanvas.save();
        if (v instanceof TextView && pruneToDrawable) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
            destCanvas.translate(padding / 2, padding / 2);
            d.draw(destCanvas);
        } else {
            if (v instanceof FolderIcon) {
                // For FolderIcons the text can bleed into the icon area, and so we need to
                // hide the text completely (which can't be achieved by clipping).
                if (((FolderIcon) v).getTextVisible()) {
                    ((FolderIcon) v).setTextVisible(false);
                    textVisible = true;
                }
            } else if (v instanceof BubbleTextView) {
                final BubbleTextView tv = (BubbleTextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V +
                        tv.getLayout().getLineTop(0);
            } else if (v instanceof TextView) {
                final TextView tv = (TextView) v;
                clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() +
                        tv.getLayout().getLineTop(0);
            }
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Op.REPLACE);
            v.draw(destCanvas);

            // Restore text visibility of FolderIcon if necessary
            if (textVisible) {
                ((FolderIcon) v).setTextVisible(true);
            }
        }
        destCanvas.restore();
    }

    /**
     * Returns a new bitmap to show when the given View is being dragged around.
     * Responsibility for the bitmap is transferred to the caller.
     */
    public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
        Bitmap b;

        if (v instanceof TextView) {
            Drawable d = ((TextView) v).getCompoundDrawables()[1];
            b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
                    d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);
        } else {
            b = Bitmap.createBitmap(
                    v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
        }

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        canvas.setBitmap(null);

        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
        final int outlineColor = getResources().getColor(android.R.color.white);
        final Bitmap b = Bitmap.createBitmap(
                v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        drawDragView(v, canvas, padding, true);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);
        return b;
    }

    /**
     * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
     * Responsibility for the bitmap is transferred to the caller.
     */
    private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h,
            boolean clipAlpha) {
        final int outlineColor = getResources().getColor(android.R.color.white);
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
        float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                (h - padding) / (float) orig.getHeight());
        int scaledWidth = (int) (scaleFactor * orig.getWidth());
        int scaledHeight = (int) (scaleFactor * orig.getHeight());
        Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(orig, src, dst, null);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor,
                clipAlpha);
        canvas.setBitmap(null);

        return b;
    }
    
    void startDrag(CellLayout.CellInfo cellInfo) {
    	
        View child = cellInfo.cell;

        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }

        mDragInfo = cellInfo;
        child.setVisibility(INVISIBLE);
        CellLayout layout = (CellLayout) child.getParent().getParent();
        layout.prepareChildForDrag(child);
        
        // Aurora <jialf> <2013-10-30> add for fix bug #174 begin
        resetAuroraHotseatData();
        getAuroraPosition(cellInfo);
        // Aurora <jialf> <2013-10-30> add for fix bug #174 end

        mHotseatChildCount = mLauncher.getHotseat().getLayout()
				.getShortcutsAndWidgets().getChildCount();
        
        child.clearFocus();
        child.setPressed(false);

        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(child, canvas, DRAG_BITMAP_PADDING);
        beginDragShared(child, this);
    }
    
    // Aurora <haojj> <2013-10-10> add for field begin
    void startDragWithSource(CellLayout.CellInfo cellInfo, DragSource source) {
        View child = cellInfo.cell;

        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }

        mDragInfo = cellInfo;
        child.setVisibility(INVISIBLE);
        CellLayout layout = (CellLayout) child.getParent().getParent();
        layout.prepareChildForDrag(child);

        child.clearFocus();
        child.setPressed(false);
        
        mHotseatChildCount = mLauncher.getHotseat().getLayout()
				.getShortcutsAndWidgets().getChildCount();

        final Canvas canvas = new Canvas();

        // The outline is used to visualize where the item will land if dropped
        mDragOutline = createDragOutline(child, canvas, DRAG_BITMAP_PADDING);
        beginDragShared(child, source);
    }
    // Aurora <haojj> <2013-10-10> end

    // Aurora <haojj> <2013-11-4> add for field begin
    public DragView createDragSwapView(View swapView, DragView dView){
    	final Bitmap b = createDragBitmap(swapView, new Canvas(), DRAG_BITMAP_PADDING);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(dView, mTempXY);
        int dragLayerX =
                Math.round(mTempXY[0] - (bmpWidth - scale * bmpWidth) / 2); //swapView.getWidth()
        int dragLayerY =
                Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);
        return mDragController.createDragSwapView(b, dragLayerX, dragLayerY, scale);
    }
    // Aurora <haojj> <2013-11-4> end
    
    public void beginDragShared(View child, DragSource source) {
        Resources r = getResources();

        // The drag bitmap follows the touch point around on the screen
        final Bitmap b = createDragBitmap(child, new Canvas(), DRAG_BITMAP_PADDING);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
        int dragLayerX =
                Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY =
                Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2
                        - DRAG_BITMAP_PADDING / 2);

        Point dragVisualizeOffset = null;
        Rect dragRect = null;
        if (child instanceof BubbleTextView || child instanceof PagedViewIcon) {
            int iconSize = r.getDimensionPixelSize(R.dimen.app_icon_size);
            int iconPaddingTop = r.getDimensionPixelSize(R.dimen.app_icon_padding_top);
            int top = child.getPaddingTop();
            int left = (bmpWidth - iconSize) / 2;
            int right = left + iconSize;
            int bottom = top + iconSize;
            dragLayerY += top;
            // Note: The drag region is used to calculate drag layer offsets, but the
            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
            dragVisualizeOffset = new Point(-DRAG_BITMAP_PADDING / 2,
                    iconPaddingTop - DRAG_BITMAP_PADDING / 2);
            dragRect = new Rect(left, top, right, bottom);
        } else if (child instanceof FolderIcon) {
            int previewSize = 0;
            //TODO:NOTE3
            if(false){
            	previewSize = r.getDimensionPixelSize(R.dimen.folder_preview_size);
            }else{
            	previewSize = r.getDimensionPixelSize(R.dimen.folder_preview_size_scale);
            }
            		
            dragRect = new Rect(0, 0, child.getWidth(), previewSize);
        }

        // Clear the pressed state if necessary
        if (child instanceof BubbleTextView) {
            BubbleTextView icon = (BubbleTextView) child;
            icon.clearPressedOrFocusedBackground();
        }

        mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale);
        b.recycle();

        // Show the scrolling indicator when you pick up an item
        //showScrollingIndicator(false);
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout target, long container, int screen,
            int cellX, int cellY, boolean insertAtFirst, int intersectX, int intersectY) {
        View view = mLauncher.createShortcut(R.layout.application, target, (ShortcutInfo) info);

        final int[] cellXY = new int[2];
        target.findCellForSpanThatIntersects(cellXY, 1, 1, intersectX, intersectY);
        addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1, insertAtFirst);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen, cellXY[0],
                cellXY[1]);
    }

    public boolean transitionStateShouldAllowDrop() {
        return ((!isSwitchingState() || mTransitionProgress > 0.5f) && mState != State.SMALL);
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragObject d) {
        // If it's an external drop (e.g. from All Apps), check if it should be accepted
        CellLayout dropTargetLayout = mDropToLayout;
        if (d.dragSource != this) {
            // Don't accept the drop if we're not over a screen at time of drop
            if (dropTargetLayout == null) {
                return false;
            }
            if (!transitionStateShouldAllowDrop()) return false;

            // Aurora <haojj> <2013-10-10> add for 如果需要交换位置则可以交换 begin
        	if(mNeedSwapPosition){
        		return true;
        	// Aurora <jialf> <2014-01-24> modify for fix bug #2126 begin
			} else {
				if (mHotseatChildCount == 4 && mLauncher.isHotseatLayout(dropTargetLayout))
					return false;
			}
        	// Aurora <jialf> <2014-01-24> modify for fix bug #2126 end
        	// Aurora <haojj> <2013-10-10> end
            mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
                    d.dragView, mDragViewVisualCenter);

            // We want the point to be mapped to the dragTarget.
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }

            int spanX = 1;
            int spanY = 1;
            if (mDragInfo != null) {
                final CellLayout.CellInfo dragCellInfo = mDragInfo;
                spanX = dragCellInfo.spanX;
                spanY = dragCellInfo.spanY;
            } else {
                final ItemInfo dragInfo = (ItemInfo) d.dragInfo;
                spanX = dragInfo.spanX;
                spanY = dragInfo.spanY;
            }

            int minSpanX = spanX;
            int minSpanY = spanY;
            if (d.dragInfo instanceof PendingAddWidgetInfo) {
                minSpanX = ((PendingAddWidgetInfo) d.dragInfo).minSpanX;
                minSpanY = ((PendingAddWidgetInfo) d.dragInfo).minSpanY;
            }
            
//AURORA-START::change the span for big widget cover small widget::Shi guiqiang::20131115
//            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
//                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, dropTargetLayout,
//                    mTargetCell);
            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout,
                    mTargetCell);
//AURORA-END::change the span for big widget cover small widget::Shi guiqiang::20131115
            
            Log.d("DEBUG", "mTargetCell[0] = "+mTargetCell[0]+" mTargetCell[1] = "+mTargetCell[1]);
            float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                    mDragViewVisualCenter[1], mTargetCell);
            if (willCreateUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance, true)) {
                return true;
            }
            if (willAddToExistingUserFolder((ItemInfo) d.dragInfo, dropTargetLayout,
                    mTargetCell, distance)) {
                return true;
            }

            if(!mLauncher.isHotseatLayout(dropTargetLayout)) {
	            int[] resultSpan = new int[2];
	           //Aurora-start
	            boolean isWidgetOverView=false;
	            Log.i("jialf", "[ " + mTargetCell[0] +", "+mTargetCell[1]+" ], " + mLauncher.isHotseatLayout(dropTargetLayout));
	            boolean isFoundCell=mTargetCell[0]<0||mTargetCell[1]<0;
	            if(d.dragInfo instanceof PendingAddWidgetInfo && !isFoundCell){
	            	outer:for(int i=mTargetCell[0];i<mTargetCell[0]+((PendingAddWidgetInfo)(d.dragInfo)).spanX&&i<dropTargetLayout.getCountX();i++){
	            		for(int j=mTargetCell[1];j<mTargetCell[1]+((PendingAddWidgetInfo)(d.dragInfo)).spanY&&j<dropTargetLayout.getCountY();j++){
	            			if(dropTargetLayout.isOccupied(i, j)){
	            				isWidgetOverView=true;
	            				break outer;
	            			}
	            		}
	            	}
	            }
	            if(!isWidgetOverView){
		            mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
		                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
		                    null, mTargetCell, resultSpan, CellLayout.MODE_ACCEPT_DROP);
	            }else{
	            	mTargetCell[0]=mTargetCell[1]=-1;
	            }
	            //Aurora_end
            // Aurora <jialf> <2014-03-04> modify for fix bug #2796 begin
            } else {
				if (mTargetCell[0] != -1) {
					View v = dropTargetLayout.getShortcutsAndWidgets().getChildAt(mTargetCell[0]);
					if ((v != null) && v.getTag() instanceof FolderInfo) {
	            		mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
			                    (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
			                    null, mTargetCell, null, CellLayout.MODE_ACCEPT_DROP);
						Log.i(TAG, "from folder ondrop, find cell is : [ "
								+ mTargetCell[0] + ", " + mTargetCell[1] + " ]");
					}
            	}
            }
            
            boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

            // Don't accept the drop if there's no room for the item
            if (!foundCell) {
            	 //If space is insufficient, appwidget added to the last screen --star  add by xiexiujie 9.16
				int numberPage = 0;
				CellLayout moveToCellLayout = null;

				ItemInfo auroraInfo = (ItemInfo) d.dragInfo;
				if ((d.dragInfo instanceof PendingAddWidgetInfo)||(d.dragInfo instanceof PendingAddShortcutInfo)) {

					CellLayout currentPage = (CellLayout) getPageAt(getCurrentPage());
					if (currentPage.findCellForSpan(mTempEstimate,
							auroraInfo.spanX, auroraInfo.spanY)) {

						return false;
					}
					CellLayout mDropLayout = (CellLayout) getPageAt(getPageCount() - 2);
					if (mDropLayout.findCellForSpan(mTempEstimate,
							auroraInfo.spanX, auroraInfo.spanY)) {

						numberPage = getPageCount() - 2;
						moveToCellLayout = mDropLayout;

					} else {
						numberPage = getPageCount() - 1;
						moveToCellLayout = (CellLayout) getPageAt(getPageCount() - 1);
					}

					if (d.dragSource != this) {

						View swapView = null;
						if (d.dragSource instanceof Folder && mNeedSwapPosition) {

							swapView = mDropLayout.getChildAt(mTargetCell[0],
									mTargetCell[1]);
							mDropLayout.removeView(swapView);
						}

						if (!mNeedSwapPosition
								&& auroraInfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
							boolean hasMovedLayouts = (getParentCellLayoutForView(mDragInfo.cell) != dropTargetLayout);
							if (!hasMovedLayouts) {

								CellLayout hotseat = mLauncher.getHotseat()
										.getLayout();
								Log.i(TAG, "Removed aurora child info : "
										+ auroraInfo.toString());
								hotseat.getShortcutsAndWidgets().removeViewAt(
										auroraInfo.cellX);
							}
						}
						mNeedSwapPosition = false;
						if (mDisableView != null)
							mDisableView = null;

						final int[] touchXY = new int[] {
								(int) mDragViewVisualCenter[0],
								(int) mDragViewVisualCenter[1] };
						onWidgetDropExternal(touchXY, d.dragInfo,
								moveToCellLayout, true, d, numberPage);

					

						resetAuroraHotseatData();

					}
				}
               // end adb by xiexiujie 9.16
                return false;
            }
        } 
        return true;
    }
    //add by xiexiujie for calender plugin icon start 10.13
	private void onWidgetDropExternal(final int[] touchXY, final Object dragInfo,
			final CellLayout cellLayout, boolean insertAtFirst, DragObject d,
			final int numberPage) {
		

		final ItemInfo info = (ItemInfo) dragInfo;
		int spanX = info.spanX;
		int spanY = info.spanY;
		if (mDragInfo != null) {
			spanX = mDragInfo.spanX;
			spanY = mDragInfo.spanY;
		}

		final long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		final int screen = numberPage;
		if (!mLauncher.isHotseatLayout(cellLayout) && screen != mCurrentPage
				&& mState != State.SPRING_LOADED) {
			snapToPage(screen);
		}

		if (info instanceof PendingAddItemInfo) {
			final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;

			boolean findNearestVacantCell = false;


			final ItemInfo item = (ItemInfo) d.dragInfo;
			boolean updateWidgetSize = false;


			Runnable onAnimationCompleteRunnable = new Runnable() {
				@Override
				public void run() {				
					switch (pendingInfo.itemType) {
					case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
						int span[] = new int[2];
						span[0] = info.spanX;
						span[1] = info.spanY;

						mLauncher.addAppWidgetFromDrop(
								(PendingAddWidgetInfo) pendingInfo, container,
								numberPage, mTargetCell, span, null);

						break;
					case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:

						mLauncher.processShortcutFromDrop(
								pendingInfo.componentName, container,
								numberPage, mTargetCell, null);

						break;
					default:
						throw new IllegalStateException("Unknown item type: "
								+ pendingInfo.itemType);
					}
				}
			};
			View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET ? ((PendingAddWidgetInfo) pendingInfo).boundWidget
					: null;	
			if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
				AppWidgetHostView awhv = (AppWidgetHostView) finalView;
				AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher,
						item.spanX, item.spanY);
			}

			int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
			if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
					&& ((PendingAddWidgetInfo) pendingInfo).info.configure != null) {
				animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
			}
			animateWidgetDrop(info, cellLayout, d.dragView,
					onAnimationCompleteRunnable, animationStyle, finalView,
					true);

		}
	}
	 //add by xiexiujie for calender plugin icon end 10.13

    boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell, float
            distance, boolean considerTimeout) {
        if (distance > mMaxDistanceForFolderCreation) return false;
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);

        if (dropOverView != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY)) {
                return false;
            }
        }

        boolean hasntMoved = false;
        if (mDragInfo != null) {
            hasntMoved = dropOverView == mDragInfo.cell;
        }

        if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
            return false;
        }

        boolean aboveShortcut = (dropOverView.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut =
                (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);

        return (aboveShortcut && willBecomeShortcut);
    }

    boolean willAddToExistingUserFolder(Object dragInfo, CellLayout target, int[] targetCell,
            float distance) {
		if (distance > mMaxDistanceForFolderCreation) return false;
        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);

        if (dropOverView != null) {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dropOverView.getLayoutParams();
            if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.tmpCellY)) {
                return false;
            }
        }

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(dragInfo)) {
                return true;
            }
        }
        return false;
    }

    boolean createUserFolderIfNecessary(View newView, long container, CellLayout target,
            int[] targetCell, float distance, boolean external, DragView dragView,
            Runnable postAnimationRunnable) {
        if (distance > mMaxDistanceForFolderCreation) return false;
        View v = target.getChildAt(targetCell[0], targetCell[1]);

        boolean hasntMoved = false;
		// Aurora <jialf> <2013-10-08> modify for Dock data begin
        if (mDragInfo != null && getParentCellLayoutForView(mDragInfo.cell)!=null) {
            CellLayout cellParent = getParentCellLayoutForView(mDragInfo.cell);
            hasntMoved = (mDragInfo.cellX == targetCell[0] &&
                    mDragInfo.cellY == targetCell[1]) && (cellParent == target);
        }
		// Aurora <jialf> <2013-10-08> modify for Dock data end

        if (v == null || hasntMoved || !mCreateUserFolderOnDrop) return false;
        mCreateUserFolderOnDrop = false;
        final int screen = (targetCell == null) ? mDragInfo.screen : indexOfChild(target);

        boolean aboveShortcut = (v.getTag() instanceof ShortcutInfo);
        boolean willBecomeShortcut = (newView.getTag() instanceof ShortcutInfo);

        if (aboveShortcut && willBecomeShortcut) {
            ShortcutInfo sourceInfo = (ShortcutInfo) newView.getTag();
            ShortcutInfo destInfo = (ShortcutInfo) v.getTag();
            String title  =  mLauncher.getModel().getFolderNameByCommonTag(sourceInfo, destInfo);
           // Aurora <jialf> <2013-10-08> modify for Dock data begin
            // if the drag started here, we need to remove it from the workspace
			if (!external && getParentCellLayoutForView(mDragInfo.cell) != null) {
                getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
            }
			// Aurora <jialf> <2013-10-08> modify for Dock data end

            Rect folderLocation = new Rect();
            float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf(v, folderLocation);
            target.removeView(v);
            
            FolderIcon fi =
                mLauncher.addFolder(target, container, screen, targetCell[0], targetCell[1],title);
            destInfo.cellX = -1;
            destInfo.cellY = -1;
            sourceInfo.cellX = -1;
            sourceInfo.cellY = -1;

            // If the dragView is null, we can't animate
            boolean animate = dragView != null;
            if (animate) {
                fi.performCreateAnimation(destInfo, v, sourceInfo, dragView, folderLocation, scale,
                        postAnimationRunnable);
            } else {
                fi.addItem(destInfo);
                fi.addItem(sourceInfo);
            }
            
			if (mAuroraSwapTag) {
				updateHotseatData();
				resetAuroraHotseatData();
			}
            
            return true;
        }
        return false;
    }

    boolean addToExistingFolderIfNecessary(View newView, CellLayout target, int[] targetCell,
            float distance, DragObject d, boolean external) {
        if (distance > mMaxDistanceForFolderCreation) return false;

        View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
        if (!mAddToExistingFolderOnDrop) return false;
        mAddToExistingFolderOnDrop = false;

        if (dropOverView instanceof FolderIcon) {
            FolderIcon fi = (FolderIcon) dropOverView;
            if (fi.acceptDrop(d.dragInfo)) {
        		// Aurora <jialf> <2013-10-14> modify for Dock data begin
                // need request hotseat layout
            	ShortcutInfo info = (ShortcutInfo) d.dragInfo;
            	boolean fromHotseat = false;
				if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT
						&& mAuroraSwapTag) {
					fromHotseat = true;
					ShortcutAndWidgetContainer sac = mLauncher.getHotseat()
							.getLayout().getShortcutsAndWidgets();
            		updateAuroraMemoryParams(info,sac);
				}
            	
				// Aurora <haojj> <2013-12-4> add for 如果target文件夹是在Dock栏则他的offset有些不一致因此要调用该方法 begin
            	if(mLauncher.isHotseatLayout(target)){
            		fi.notifyDropOnDock();
            	}
            	// Aurora <haojj> <2013-12-4> end     
            	fi.onDrop(d);

                // if the drag started here, we need to remove it from the workspace
				if (!external && getParentCellLayoutForView(mDragInfo.cell)!=null) {
                    getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                    if(fromHotseat) {
                    	final CellLayout parentLayout = mLauncher.getHotseat().getLayout();
    					parentLayout.setAuroraCellX(false);
    					parentLayout.setAuroraGridSize(-1);
    					updateHotseatData();
    					resetAuroraHotseatData();
    					fromHotseat = false;
                    }
                }
				// Aurora <jialf> <2013-11-22> modify for fix bug #643 begin
				if (mAuroraSwapTag)
					resetAuroraHotseatData();
				// Aurora <jialf> <2013-11-22> modify for fix bug #643 end
				// Aurora <jialf> <2013-10-08> modify for Dock data end
                return true;
            }
        }
        return false;
    }
    
    public void onDrop(final DragObject d) {
    	// Aurora <haojj> <2013-9-29> add for 打开文件夹的拖动时放下关闭folder begin
    	mLauncher.closeFolder();
		// Aurora <haojj> <2013-9-29> end
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView,
                mDragViewVisualCenter);

        CellLayout dropTargetLayout = mDropToLayout;

        // We want the point to be mapped to the dragTarget.
        if (dropTargetLayout != null) {
            if (mLauncher.isHotseatLayout(dropTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
            }
        }		

        int snapScreen = -1;
        boolean resizeOnDrop = false;
        View auroraSwapView = null;
		if (d.dragSource != this) {
        	// Aurora <haojj> <2013-10-10> add for 与文件夹中icon交换时的特殊处理 begin
        	View swapView = null;
        	if(d.dragSource instanceof Folder &&  mNeedSwapPosition){
        		swapView = mDropToLayout.getChildAt(mTargetCell[0], mTargetCell[1]);
        		mDropToLayout.removeView(swapView);
        	}
        	// Aurora <haojj> <2013-10-10> end
        	
        	// Aurora <jialf> <2013-11-11> add for fix bug #622 #643 begin
        	ItemInfo auroraInfo = (ItemInfo) d.dragInfo;
			if (!mNeedSwapPosition
					&& auroraInfo.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
				boolean hasMovedLayouts = (getParentCellLayoutForView(mDragInfo.cell) != dropTargetLayout);
				if (!hasMovedLayouts) {
					CellLayout hotseat = mLauncher.getHotseat().getLayout();
					Log.i(TAG, "Removed aurora child info : " + auroraInfo.toString());
					hotseat.getShortcutsAndWidgets().removeViewAt(auroraInfo.cellX);
				}
        	}
			mNeedSwapPosition = false;
			if (mDisableView != null)
				mDisableView = null;
        	// Aurora <jialf> <2013-11-11> add for fix bug #622 #643 end
        	
            final int[] touchXY = new int[] { (int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1] };
            onDropExternal(touchXY, d.dragInfo, dropTargetLayout, false, d);
            
            // Aurora <haojj> <2013-11-4> add for 交换 begin
            if(swapView != null){
            	Folder folder = (Folder) d.dragSource;
            	DragView dragView = createDragSwapView(swapView, d.dragView);
            	
            	ItemInfo swapInfo = (ItemInfo) swapView.getTag();
        		if(swapInfo instanceof ShortcutInfo){
        			folder.getFolderIcon().onDrop(dragView, (ShortcutInfo)swapInfo);
                }
        		/*
        		ItemInfo swapInfo = (ItemInfo) swapView.getTag();
        		if(swapInfo instanceof ShortcutInfo){
        			folder.mInfo.add((ShortcutInfo) swapInfo);
        		}
        		folder.notifyDrop();*/
            }
    		resetAuroraHotseatData();
            // Aurora <haojj> <2013-11-4> end
        } else if (mDragInfo != null) {
            final View cell = mDragInfo.cell;

            Runnable resizeRunnable = null;
            if (dropTargetLayout != null) {
                // Move internally
                boolean hasMovedLayouts = (getParentCellLayoutForView(cell) != dropTargetLayout);
                boolean hasMovedIntoHotseat = mLauncher.isHotseatLayout(dropTargetLayout);
                long container = hasMovedIntoHotseat ?
                        LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                        LauncherSettings.Favorites.CONTAINER_DESKTOP;
                int screen = (mTargetCell[0] < 0) ?
                        mDragInfo.screen : indexOfChild(dropTargetLayout);
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;
                // First we find the cell nearest to point at which the item is
                // dropped, without any consideration to whether there is an item there.

                mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                        mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);

                // If the item being dropped is a shortcut and the nearest drop
                // cell also contains a shortcut, then create a folder with the two shortcuts.
                if (!mInScrollArea && createUserFolderIfNecessary(cell, container,
                        dropTargetLayout, mTargetCell, distance, false, d.dragView, null)) {
                	resetAuroraHotseatData();                	          	                
                    return;
                }

                if (addToExistingFolderIfNecessary(cell, dropTargetLayout, mTargetCell,
                        distance, d, false)) {               	
                    return;
                }

            	// Aurora <jialf> <2013-10-08> add for Dock data begin
                if(mNeedSwapPosition) {               	
                	auroraSwapView = mLauncher.getHotseat().getLayout()
							.getChildAt(mTargetCell[0], mTargetCell[1]);
					ItemInfo swapInfo = (ItemInfo) auroraSwapView.getTag();
					getParentCellLayoutForView(auroraSwapView).removeView(auroraSwapView);
			        // Aurora <jialf> <2013-10-30> modify for fix bug #174 begin
					swapInfo.screen = mDragItemInfo[0];
					swapInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
					swapInfo.cellX = mDragItemInfo[1];
					swapInfo.cellY = mDragItemInfo[2];
					auroraSwapView.setTag(swapInfo);
					CellLayout.LayoutParams clp = (CellLayout.LayoutParams) auroraSwapView.getLayoutParams();
					clp.cellX = clp.tmpCellX = mTargetCell[0];
					clp.cellY = clp.tmpCellY = mTargetCell[1];
					clp.cellHSpan = swapInfo.spanX;
					clp.cellVSpan = swapInfo.spanY;
					clp.useTmpCoords = false;
					clp.isLockedToGrid = true;
                    cell.setId(LauncherModel.getCellLayoutChildId(container, swapInfo.screen,
                    		swapInfo.cellX, swapInfo.cellY, swapInfo.spanX, swapInfo.spanY));                 
					addInScreen(auroraSwapView, swapInfo.container, swapInfo.screen, swapInfo.cellX, swapInfo.cellY, 1, 1);
					LauncherModel.moveItemInDatabase(mLauncher, swapInfo, swapInfo.container, swapInfo.screen, swapInfo.cellX,
							swapInfo.cellY);
			        // Aurora <jialf> <2013-10-30> modify for fix bug #174 end
					auroraSwapView.setVisibility(VISIBLE);
            	}
            	// Aurora <jialf> <2013-10-08> add for Dock data end

                // Aside from the special case where we're dropping a shortcut onto a shortcut,
                // we need to find the nearest cell location that is vacant
                ItemInfo item = (ItemInfo) d.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }

                int[] resultSpan = new int[2];
				//Aurora-start:xiejun:20130927:ID138
                final View dragOverView=dropTargetLayout.getChildAt(mTargetCell[0], mTargetCell[1]);
                boolean isWidgetOverView=false;
                boolean isViewOverWidget=false;
                Log.i("xiejun","mTargetCell[0]="+mTargetCell[0]+" , mTargetCell[1]="+ mTargetCell[1]);
                Log.i("xiejun","isWidgetOverView="+isWidgetOverView+" ,isViewOverWidget="+isViewOverWidget);
                boolean isFoundCell=mTargetCell[0]<0||mTargetCell[1]<0;
                if(d.dragInfo instanceof LauncherAppWidgetInfo ){
                	if(!isFoundCell){
		            	outer:for(int i=mTargetCell[0];i<mTargetCell[0]+((LauncherAppWidgetInfo)(d.dragInfo)).spanX;i++){
		            		for(int j=mTargetCell[1];j<mTargetCell[1]+((LauncherAppWidgetInfo)(d.dragInfo)).spanY;j++){
		            			if(dropTargetLayout.isOccupied(i, j)){
		            				isWidgetOverView=true;
		            				break outer;
		            			}
		            		}
		            	}
                	}
                }else{
                	if(dragOverView!=null && dragOverView.getTag() instanceof LauncherAppWidgetInfo){
                		isViewOverWidget=true;
                	}
                }
                if(!isWidgetOverView&&!isViewOverWidget&&!isFoundCell){               	
	                mTargetCell = dropTargetLayout.createArea((int) mDragViewVisualCenter[0],
	                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY, cell,
	                        mTargetCell, resultSpan, CellLayout.MODE_ON_DROP);
                }else{                	
                	mTargetCell[0] = mTargetCell[1] = resultSpan[0] = resultSpan[1] = -1;
                }
                Log.i("xiejun","mTargetCell[0]="+mTargetCell[0]+" , mTargetCell[1]="+ mTargetCell[1]);
                boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
                // if the widget resizes on drop
            	
                if (foundCell && (cell instanceof AppWidgetHostView) &&
                        (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY)) {
                    resizeOnDrop = true;
                    item.spanX = resultSpan[0];
                    item.spanY = resultSpan[1];
                    AppWidgetHostView awhv = (AppWidgetHostView) cell;
                    AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, resultSpan[0],
                            resultSpan[1]);
                }
                
              //Aurora-end:xiejun:20130927:ID138

                if (mCurrentPage != screen && !hasMovedIntoHotseat) {
                    snapScreen = screen;
                    snapToPage(screen);
                }

                if (foundCell) {               	
               
                    final ItemInfo info = (ItemInfo) cell.getTag();		
            		// Aurora <jialf> <2013-10-08> modify for Dock data begin			
                    if (hasMovedLayouts) {
                        // Reparent the view
                		// Aurora <jialf> <2013-10-22> modify for fix bug #41 begin	
                        final CellLayout parentLayout = getParentCellLayoutForView(mDragInfo.cell);
						if (parentLayout != null) {
							if (mAuroraSwapTag) {
								ShortcutAndWidgetContainer sac = parentLayout
										.getShortcutsAndWidgets();
								updateAuroraMemoryParams(info, sac);
								parentLayout.removeView(mDragInfo.cell);
								parentLayout.setAuroraCellX(false);
								parentLayout.setAuroraGridSize(-1);
								// updateHotseatData();
							} else {
								parentLayout.removeView(cell);
							}
						}
                		// Aurora <jialf> <2013-10-22> modify for fix bug #41 end					
                        addInScreen(cell, container, screen, mTargetCell[0], mTargetCell[1],
                                info.spanX, info.spanY);

                        if(mNeedSwapPosition && auroraSwapView!=null) {
							CellLayout auroraParent = (CellLayout) auroraSwapView.getParent().getParent();
							auroraParent.markCellsAsOccupiedForView(auroraSwapView);
							mNeedSwapPosition = false;
							auroraSwapView = null;
                        }
                    }
            		// Aurora <jialf> <2013-10-08> modify for Dock data end

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = lp.tmpCellX = mTargetCell[0];
                    lp.cellY = lp.tmpCellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                    lp.isLockedToGrid = true;
                    cell.setId(LauncherModel.getCellLayoutChildId(container, mDragInfo.screen,
                            mTargetCell[0], mTargetCell[1], mDragInfo.spanX, mDragInfo.spanY));
                    
               
                    
                    if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                            cell instanceof LauncherAppWidgetHostView) {
                        final CellLayout cellLayout = dropTargetLayout;
                        // We post this call so that the widget has a chance to be placed
                        // in its final location
                        //Aurora-start:xiejun:ID138
                        
                        final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                        AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
                        if (pinfo != null &&
                                pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
                            final Runnable addResizeFrame = new Runnable() {
                                public void run() {
                                    DragLayer dragLayer = mLauncher.getDragLayer();
                                    dragLayer.addResizeFrame(info, hostView, cellLayout);
                                }
                            };
                            resizeRunnable = (new Runnable() {
                                public void run() {
                                    if (!isPageMoving()) {
                                        addResizeFrame.run();
                                    } else {
                                        mDelayedResizeRunnable = addResizeFrame;
                                    }
                                }
                            });
                        }
                        
                      //Aurora-end:xiejun:ID138
                    }

                    	                          
                    LauncherModel.moveItemInDatabase(mLauncher, info, container, screen, lp.cellX,
                            lp.cellY);

                } else {
                	
            		// Aurora <jialf> <2013-10-08> modify for Dock data begin
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    // Aurora <jialf> <2013-11-20> modify for fix bug #648 begin
                    CellLayout cellParent = getParentCellLayoutForView(cell);
					if (cellParent == null) {
	                // Aurora <jialf> <2013-11-20> modify for fix bug #648 end
						if(mAuroraSwapTag) {
							resetHotseatChildToScreen(item, cell);
						} else {
							ItemInfo dragItem = (ItemInfo) cell.getTag();
							dragItem.screen = mDragItemInfo[0];
							dragItem.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
							dragItem.cellX = mDragItemInfo[1];
							dragItem.cellY = mDragItemInfo[2];
							cell.setTag(dragItem);
							CellLayout.LayoutParams clp = (CellLayout.LayoutParams) cell
									.getLayoutParams();
							clp.cellX = clp.tmpCellX = mDragItemInfo[2];
							clp.cellY = clp.tmpCellY = mDragItemInfo[2];
							clp.cellHSpan = dragItem.spanX;
							clp.cellVSpan = dragItem.spanY;
							clp.useTmpCoords = false;
							clp.isLockedToGrid = true;
							cell.setId(LauncherModel.getCellLayoutChildId( dragItem.container, dragItem.screen,
									dragItem.cellX, dragItem.cellY, dragItem.spanX, dragItem.spanY));
							addInScreen(cell, dragItem.container, dragItem.screen, dragItem.cellX,
									dragItem.cellY, 1, 1);
							LauncherModel.moveItemInDatabase(mLauncher, dragItem, dragItem.container,
									dragItem.screen, dragItem.cellX, dragItem.cellY);
						}
					} else {
	                    CellLayout layout = (CellLayout) cell.getParent().getParent();
	                    layout.markCellsAsOccupiedForView(cell);
					}
					// Aurora <jialf> <2013-10-08> modify for Dock data end
                }
            }

    		// Aurora <jialf> <2013-10-08> add for Dock data begin
            updateHotseatData();
            resetAuroraHotseatData();
			// Aurora <jialf> <2013-10-08> add for Dock data end

            final CellLayout parent = (CellLayout) cell.getParent().getParent();
            final Runnable finalResizeRunnable = resizeRunnable;
            // Prepare it to be animated into its new position
            // This must be called after the view has been re-parented
            final Runnable onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    mAnimatingViewIntoPlace = false;
                    //updateChildrenLayersEnabled(false);
                    if (finalResizeRunnable != null) {
                        finalResizeRunnable.run();
                    }
                }
            };
            mAnimatingViewIntoPlace = true;
            if (d.dragView.hasDrawn()) {
                final ItemInfo info = (ItemInfo) cell.getTag();
                if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
                    int animationType = resizeOnDrop ? ANIMATE_INTO_POSITION_AND_RESIZE :
                            ANIMATE_INTO_POSITION_AND_DISAPPEAR;
                    animateWidgetDrop(info, parent, d.dragView,
                            onCompleteRunnable, animationType, cell, false);
                } else {
                    int duration = snapScreen < 0 ? -1 : ADJACENT_SCREEN_DROP_DURATION;              
                    mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, cell, duration,
                            onCompleteRunnable, this);
                }
            } else {
                d.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
            }
            parent.onDropChild(cell);
        }
    }

    public void setFinalScrollForPageChange(int screen) {
        if (screen >= 0) {
            mSavedScrollX = getScrollX();
            CellLayout cl = (CellLayout) getChildAt(screen);
            // Aurora <haojj> <2013-10-28> add for 刚开机时会有出空指针的可能性，如果screen不在当前page的范围 BUG #240 begin
            if(cl == null) return;
            // Aurora <haojj> <2013-10-28> end
            mSavedTranslationX = cl.getTranslationX();
            mSavedRotationY = cl.getRotationY();
            final int newX = getChildOffset(screen) - getRelativeChildOffset(screen);
            setScrollX(newX);
            cl.setTranslationX(0f);
            cl.setRotationY(0f);
        }
    }

    public void resetFinalScrollForPageChange(int screen) {
        if (screen >= 0) {
            CellLayout cl = (CellLayout) getChildAt(screen);
            setScrollX(mSavedScrollX);
            cl.setTranslationX(mSavedTranslationX);
            cl.setRotationY(mSavedRotationY);
        }
    }

    public void getViewLocationRelativeToSelf(View v, int[] location) {
        getLocationInWindow(location);
        int x = location[0];
        int y = location[1];

        v.getLocationInWindow(location);
        int vX = location[0];
        int vY = location[1];

        location[0] = vX - x;
        location[1] = vY - y;
    }

    public void onDragEnter(DragObject d) {
        mDragEnforcer.onDragEnter();
        mCreateUserFolderOnDrop = false;
        mAddToExistingFolderOnDrop = false;

        mDropToLayout = null;
        CellLayout layout = getCurrentDropLayout();
        setCurrentDropLayout(layout);
        setCurrentDragOverlappingLayout(layout);

        // Because we don't have space in the Phone UI (the CellLayouts run to the edge) we
        // don't need to show the outlines
        if (LauncherApplication.isScreenLarge()) {
            showOutlines();
        }

        //add by tangjun start 2013.10.12
        mLauncher.setPageIndicatotorTextViewAlpha(0f);
        //add by tangjun end
    }

    static Rect getCellLayoutMetrics(Launcher launcher, int orientation) {
        Resources res = launcher.getResources();
        Display display = launcher.getWindowManager().getDefaultDisplay();
        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);
        if (orientation == CellLayout.LANDSCAPE) {
            if (mLandscapeCellLayoutMetrics == null) {
                int paddingLeft = res.getDimensionPixelSize(R.dimen.workspace_left_padding_land);
                int paddingRight = res.getDimensionPixelSize(R.dimen.workspace_right_padding_land);
                int paddingTop = res.getDimensionPixelSize(R.dimen.workspace_top_padding_land);
                int paddingBottom = res.getDimensionPixelSize(R.dimen.workspace_bottom_padding_land);
                int width = largestSize.x - paddingLeft - paddingRight;
                int height = smallestSize.y - paddingTop - paddingBottom;
                mLandscapeCellLayoutMetrics = new Rect();
                CellLayout.getMetrics(mLandscapeCellLayoutMetrics, res,
                        width, height, LauncherModel.getCellCountX(), LauncherModel.getCellCountY(),
                        orientation);
            }
            return mLandscapeCellLayoutMetrics;
        } else if (orientation == CellLayout.PORTRAIT) {
            if (mPortraitCellLayoutMetrics == null) {
                int paddingLeft = res.getDimensionPixelSize(R.dimen.workspace_left_padding_land);
                int paddingRight = res.getDimensionPixelSize(R.dimen.workspace_right_padding_land);
                int paddingTop = res.getDimensionPixelSize(R.dimen.workspace_top_padding_land);
                int paddingBottom = res.getDimensionPixelSize(R.dimen.workspace_bottom_padding_land);
                int width = smallestSize.x - paddingLeft - paddingRight;
                int height = largestSize.y - paddingTop - paddingBottom;
                mPortraitCellLayoutMetrics = new Rect();
                CellLayout.getMetrics(mPortraitCellLayoutMetrics, res,
                        width, height, LauncherModel.getCellCountX(), LauncherModel.getCellCountY(),
                        orientation);
            }
            return mPortraitCellLayoutMetrics;
        }
        return null;
    }

    public void onDragExit(DragObject d) {
        mDragEnforcer.onDragExit();

        // Here we store the final page that will be dropped to, if the workspace in fact
        // receives the drop
        if (mInScrollArea) {
            if (isPageMoving()) {
                // If the user drops while the page is scrolling, we should use that page as the
                // destination instead of the page that is being hovered over.
                mDropToLayout = (CellLayout) getPageAt(getNextPage());
            } else {
                mDropToLayout = mDragOverlappingLayout;
            }
        } else {
            mDropToLayout = mDragTargetLayout;
        }

        if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
            mCreateUserFolderOnDrop = true;
        } else if (mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
            mAddToExistingFolderOnDrop = true;
        }
        
		if (mDisableView != null)
			mDisableView.setVisibility(VISIBLE);

        // Reset the scroll area and previous drag target
        onResetScrollArea();
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);

        mSpringLoadedDragController.cancel();

        if (!mIsPageMoving&&LauncherApplication.isScreenLarge()) {
            hideOutlines();
        }
    }

    void setCurrentDropLayout(CellLayout layout) {
        if (mDragTargetLayout != null) {
            mDragTargetLayout.revertTempState();
            mDragTargetLayout.onDragExit();
        }
        mDragTargetLayout = layout;
        if (mDragTargetLayout != null) {
            mDragTargetLayout.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDragOverlappingLayout(CellLayout layout) {
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(false);
        }
        mDragOverlappingLayout = layout;
        if (mDragOverlappingLayout != null) {
            mDragOverlappingLayout.setIsDragOverlapping(true);
        }
        invalidate();
    }

    void setCurrentDropOverCell(int x, int y) {
        if (x != mDragOverX || y != mDragOverY) {
            mDragOverX = x;
            mDragOverY = y;
            setDragMode(DRAG_MODE_NONE);
        }
    }

    void setDragMode(int dragMode) {
        if (dragMode != mDragMode) {
            if (dragMode == DRAG_MODE_NONE) {
                cleanupAddToFolder();
                // We don't want to cancel the re-order alarm every time the target cell changes
                // as this feels to slow / unresponsive.
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (dragMode == DRAG_MODE_REORDER) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            mDragMode = dragMode;
        }
    }

    private void cleanupFolderCreation() {
        if (mDragFolderRingAnimator != null) {
            mDragFolderRingAnimator.animateToNaturalState();
        }
        mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupAddToFolder() {
        if (mDragOverFolderIcon != null) {
            mDragOverFolderIcon.onDragExit(null);
            mDragOverFolderIcon = null;
        }
    }

    private void cleanupReorder(boolean cancelAlarm) {
        // Any pending reorders are canceled
        if (cancelAlarm) {
            mReorderAlarm.cancelAlarm();
        }
        mLastReorderX = -1;
        mLastReorderY = -1;
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    /*
    *
    * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
    * coordinate space. The argument xy is modified with the return result.
    *
    */
   void mapPointFromSelfToChild(View v, float[] xy) {
       mapPointFromSelfToChild(v, xy, null);
   }

   /*
    *
    * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
    * coordinate space. The argument xy is modified with the return result.
    *
    * if cachedInverseMatrix is not null, this method will just use that matrix instead of
    * computing it itself; we use this to avoid redundant matrix inversions in
    * findMatchingPageForDragOver
    *
    */
   void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
       if (cachedInverseMatrix == null) {
           v.getMatrix().invert(mTempInverseMatrix);
           cachedInverseMatrix = mTempInverseMatrix;
       }
       int scrollX = getScrollX();
       if (mNextPage != INVALID_PAGE) {
           scrollX = mScroller.getFinalX();
       }
       xy[0] = xy[0] + scrollX - v.getLeft();
       xy[1] = xy[1] + getScrollY() - v.getTop();
       cachedInverseMatrix.mapPoints(xy);
   }


   void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] xy) {
       hotseat.getLayout().getMatrix().invert(mTempInverseMatrix);
       xy[0] = xy[0] - hotseat.getLeft() - hotseat.getLayout().getLeft();
       xy[1] = xy[1] - hotseat.getTop() - hotseat.getLayout().getTop();
       mTempInverseMatrix.mapPoints(xy);
   }

   /*
    *
    * Convert the 2D coordinate xy from this CellLayout's coordinate space to
    * the parent View's coordinate space. The argument xy is modified with the return result.
    *
    */
   void mapPointFromChildToSelf(View v, float[] xy) {
       v.getMatrix().mapPoints(xy);
       int scrollX = getScrollX();
       if (mNextPage != INVALID_PAGE) {
           scrollX = mScroller.getFinalX();
       }
       xy[0] -= (scrollX - v.getLeft());
       xy[1] -= (getScrollY() - v.getTop());
   }

   static private float squaredDistance(float[] point1, float[] point2) {
        float distanceX = point1[0] - point2[0];
        float distanceY = point2[1] - point2[1];
        return distanceX * distanceX + distanceY * distanceY;
   }

    /*
     *
     * Returns true if the passed CellLayout cl overlaps with dragView
     *
     */
    boolean overlaps(CellLayout cl, DragView dragView,
            int dragViewX, int dragViewY, Matrix cachedInverseMatrix) {
        // Transform the coordinates of the item being dragged to the CellLayout's coordinates
        final float[] draggedItemTopLeft = mTempDragCoordinates;
        draggedItemTopLeft[0] = dragViewX;
        draggedItemTopLeft[1] = dragViewY;
        final float[] draggedItemBottomRight = mTempDragBottomRightCoordinates;
        draggedItemBottomRight[0] = draggedItemTopLeft[0] + dragView.getDragRegionWidth();
        draggedItemBottomRight[1] = draggedItemTopLeft[1] + dragView.getDragRegionHeight();

        // Transform the dragged item's top left coordinates
        // to the CellLayout's local coordinates
        mapPointFromSelfToChild(cl, draggedItemTopLeft, cachedInverseMatrix);
        float overlapRegionLeft = Math.max(0f, draggedItemTopLeft[0]);
        float overlapRegionTop = Math.max(0f, draggedItemTopLeft[1]);

        if (overlapRegionLeft <= cl.getWidth() && overlapRegionTop >= 0) {
            // Transform the dragged item's bottom right coordinates
            // to the CellLayout's local coordinates
            mapPointFromSelfToChild(cl, draggedItemBottomRight, cachedInverseMatrix);
            float overlapRegionRight = Math.min(cl.getWidth(), draggedItemBottomRight[0]);
            float overlapRegionBottom = Math.min(cl.getHeight(), draggedItemBottomRight[1]);

            if (overlapRegionRight >= 0 && overlapRegionBottom <= cl.getHeight()) {
                float overlap = (overlapRegionRight - overlapRegionLeft) *
                         (overlapRegionBottom - overlapRegionTop);
                if (overlap > 0) {
                    return true;
                }
             }
        }
        return false;
    }

    /*
     *
     * This method returns the CellLayout that is currently being dragged to. In order to drag
     * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
     * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
     *
     * Return null if no CellLayout is currently being dragged over
     *
     */
    private CellLayout findMatchingPageForDragOver(
            DragView dragView, float originX, float originY, boolean exact) {
        // We loop through all the screens (ie CellLayouts) and see which ones overlap
        // with the item being dragged and then choose the one that's closest to the touch point
        final int screenCount = getChildCount();
        CellLayout bestMatchingScreen = null;
        float smallestDistSoFar = Float.MAX_VALUE;

        for (int i = 0; i < screenCount; i++) {
            CellLayout cl = (CellLayout) getChildAt(i);

            final float[] touchXy = {originX, originY};
            // Transform the touch coordinates to the CellLayout's local coordinates
            // If the touch point is within the bounds of the cell layout, we can return immediately
            cl.getMatrix().invert(mTempInverseMatrix);
            mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);

            if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                    touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                return cl;
            }

            if (!exact) {
                // Get the center of the cell layout in screen coordinates
                final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                cellLayoutCenter[0] = cl.getWidth()/2;
                cellLayoutCenter[1] = cl.getHeight()/2;
                mapPointFromChildToSelf(cl, cellLayoutCenter);

                touchXy[0] = originX;
                touchXy[1] = originY;

                // Calculate the distance between the center of the CellLayout
                // and the touch point
                float dist = squaredDistance(touchXy, cellLayoutCenter);

                if (dist < smallestDistSoFar) {
                    smallestDistSoFar = dist;
                    bestMatchingScreen = cl;
                }
            }
        }
        return bestMatchingScreen;
    }

    // This is used to compute the visual center of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
            DragView dragView, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;

        return res;
    }

    private boolean isDragWidget(DragObject d) {
        return (d.dragInfo instanceof LauncherAppWidgetInfo ||
                d.dragInfo instanceof PendingAddWidgetInfo);
    }
    private boolean isExternalDragWidget(DragObject d) {
        return d.dragSource != this && isDragWidget(d);
    }

    // Aurora <haojj> <2013-10-10> add for 判断folder的打开或关闭状态 begin
    private boolean mFolderOpened = false;
    protected void setFolderOpenState(boolean open) {
		mFolderOpened = open;
	}
    protected boolean getFolderOpenState(){
    	return mFolderOpened;
    }
    // Aurora <haojj> <2013-10-10> end
    
    public void onDragOver(DragObject d) {
        // Skip drag over events while we are dragging over side pages
		//if (mInScrollArea || mIsSwitchingState || mState == State.SMALL) return;
        if (mInScrollArea || mState == State.SMALL) return;
        if(isDragOverNavigateBar(d)){
        	if (mDisableView != null) {
				mDisableView.setVisibility(VISIBLE);
				mDisableView = null;
			}
        	return;
        }
        
        Rect r = new Rect();
        CellLayout layout = null;
        ItemInfo item = (ItemInfo) d.dragInfo;

        // Ensure that we have proper spans for the item that we are dropping
        if (item.spanX < 0 || item.spanY < 0) throw new RuntimeException("Improper spans found");
        mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
            d.dragView, mDragViewVisualCenter);

        final View child = (mDragInfo == null) ? null : mDragInfo.cell;
        // Identify whether we have dragged over a side page
        if (isSmall()) {
            if (mLauncher.getHotseat() != null && !isExternalDragWidget(d)) {
                mLauncher.getHotseat().getHitRect(r);
                if (r.contains(d.x, d.y)) {
                    layout = mLauncher.getHotseat().getLayout();
                }
            }
            if (layout == null) {
                layout = findMatchingPageForDragOver(d.dragView, d.x, d.y, false);
            }
            if (layout != mDragTargetLayout) {

                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);

                boolean isInSpringLoadedMode = (mState == State.SPRING_LOADED);
                if (isInSpringLoadedMode) {
                    if (mLauncher.isHotseatLayout(layout)) {
                        mSpringLoadedDragController.cancel();
                    } else {
                        mSpringLoadedDragController.setAlarm(mDragTargetLayout);
                    }
                }
            }
        } else {
            // Test to see if we are over the hotseat otherwise just use the current page
            if (mLauncher.getHotseat() != null && !isDragWidget(d)) {
                mLauncher.getHotseat().getHitRect(r);
                if (r.contains(d.x, d.y)) {
                    layout = mLauncher.getHotseat().getLayout();
                }
            }
            if (layout == null) {
                layout = getCurrentDropLayout();
            }
            if (layout != mDragTargetLayout) {
                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);
            }
        }

        // Handle the drag over
        if (mDragTargetLayout != null) {
            // We want the point to be mapped to the dragTarget.
            if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
                mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
            }

            ItemInfo info = (ItemInfo) d.dragInfo;

            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], item.spanX, item.spanY,
                    mDragTargetLayout, mTargetCell);
            setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

            float targetCellDistance = mDragTargetLayout.getDistanceFromCell(
                    mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);

		    final View dragOverView = mDragTargetLayout.getChildAt(mTargetCell[0],
		            mTargetCell[1]);

    		final float distance = mDragTargetLayout.getAuroraDistanceFromCell(
                    mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);
    		// Aurora <jialf> <2013-10-26> modify for fix bug #250 begin
    		if(mLauncher.isHotseatLayout(mDragTargetLayout)) {
    			manageFolderFeedback(info, mDragTargetLayout, mTargetCell,
    					distance, dragOverView, d);
		        // Aurora <jialf> <2013-10-30> add for fix bug #174 begin
				if (mDisableView != null) {
    				if(mDisableView != dragOverView) {
    					mDisableView.setVisibility(VISIBLE);
    				}
				}
		        // Aurora <jialf> <2013-10-30> add for fix bug #174 end
    		} else {
			    manageFolderFeedback(info, mDragTargetLayout, mTargetCell,
			            targetCellDistance, dragOverView, d);
    		}
    		// Aurora <jialf> <2013-10-26> modify for fix bug #250 end

    		// Aurora <jialf> <2013-10-08> add for Dock data begin
//AURORA-START::Customized widget::Shi guiqiang::20131012
		    if(item instanceof PendingAddItemInfo) {
		    	//do nothing in order to get drag event for widget
		    } else {
//AURORA-END::Customized widget::Shi guiqiang::20131012
	            final CellLayout parentLayout = getParentCellLayoutForView(mDragInfo.cell);
	            ShortcutAndWidgetContainer sac;
		        // Aurora <jialf> <2013-10-30> add for fix bug #174 begin
	            if(mLauncher.isHotseatLayout(parentLayout)){
	            	sac = parentLayout.getShortcutsAndWidgets();
	            	if(!r.contains(d.x, d.y)) {
						if (mAuroraSwapTag) {
							mAuroraDragOverFlag = true;
						} else {
							mFromHotseat = false;
						}
						mNeedSwapPosition = false;
						updateAuroraMemoryParams(item, parentLayout, sac);
	            	} else if(mFromHotseat){
	            		float newDistance = mDragTargetLayout.getAuroraDistanceFromCell(
	                            mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);
						float oldDistance = mDragTargetLayout.getAuroraOldDistanceFromCell(
								mDragViewVisualCenter[0], mDragViewVisualCenter[1], mTargetCell);
						Log.i(TAG, "mTargetCell = " + mTargetCell[0] + ", oldDistance = " + oldDistance
								+", newDistance = "+newDistance);
						if (oldDistance < newDistance) {
							updateAuroraMemoryParams(item, parentLayout, sac);
							
							if(mTargetCell[0] > 0) {
								mDisableView = mDragTargetLayout.getChildAt(mTargetCell[0] - 1, mTargetCell[1]);
							} else {
								mDisableView = mDragTargetLayout.getChildAt(mTargetCell[0], mTargetCell[1]);
							}
							if(mDisableView != null && mDisableView instanceof BubbleTextView)
								mDisableView.setVisibility(INVISIBLE);
							if(mTargetCell[0] == 0) {
								mTempLoc = -1;
							} else {
								mTempLoc = mTargetCell[0];
							}
							mNeedSwapPosition = true;
							mFromHotseat = false;
						} else {
							mNeedSwapPosition = false;
						}
	            	}
	            } else {
	            	if(r.contains(d.x, d.y) && !mFromHotseat) {
	             		CellLayout hotseat = mLauncher.getHotseat().getLayout();
	             		if(!mAuroraSwapTag) {
		            		mFromDesktopTag = true;
		             		if(hotseat.hasAuroraEmptyCell()){
		             			if(distance < mAuroraMaxDistanceForFolderCreation) {
		             				mDisableView = dragOverView;
		             				if(mDisableView instanceof BubbleTextView) {
		             					mDisableView.setVisibility(INVISIBLE);
		             					mNeedSwapPosition = true;
		             				} else {
		             					mDisableView.setVisibility(VISIBLE);
		             					mNeedSwapPosition = false;
		             				}
								} else if (mTempLoc != mTargetCell[0]
										|| mTempLoc == -1 && mTargetCell[0] == -1){
									if (mDisableView != null) {
										mDisableView.setVisibility(VISIBLE);
										mDisableView = null;
									}
				            		hotseat.setAuroraCellX(true);
				            		mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
				                             (int) mDragViewVisualCenter[1], item.spanX, item.spanY,
				                             mDragTargetLayout, mTargetCell);
				            		setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);
									updateAuroraHotseatParams(item, parentLayout, hotseat, mTargetCell[0]);
		
									mNeedSwapPosition = false;
									mFromHotseat = true;
		             			}
		             		} else {
								if(dragOverView instanceof BubbleTextView && distance < mAuroraMaxDistanceForFolderCreation) {
									mDisableView = dragOverView;
									mNeedSwapPosition = true;
									dragOverView.setVisibility(INVISIBLE);
								} else {
									mNeedSwapPosition = false;
									if(dragOverView != null) 
										dragOverView.setVisibility(VISIBLE);
								}
		             		}
	             		} else {
	             			hotseat.setAuroraCellX(true);
		            		mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
		                             (int) mDragViewVisualCenter[1], item.spanX, item.spanY,
		                             mDragTargetLayout, mTargetCell);
		            		setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);
							updateAuroraHotseatParams(item, parentLayout, hotseat, mTargetCell[0]);
							mNeedSwapPosition = false;
							mFromDesktopTag = false;
	             		}
	            	} else {
	      				if (mDisableView != null) {
							mDisableView.setVisibility(VISIBLE);
							mDisableView = null;
						}
	      				mNeedSwapPosition = false;
	      				mFromDesktopTag = false;
	            	}
	            }
	        // Aurora <jialf> <2013-10-30> add for fix bug #174 end
		    }
    		// Aurora <jialf> <2013-10-08> modify for Dock data end
            
            if(dragOverView instanceof AppWidgetHostView){
            	Log.i("xiejun","dragOverView is appWidgetHostView");
            }
            if(d.dragInfo instanceof PendingAddWidgetInfo){
            	d.dragView.setIsDragPendingWidget(true);
            }
            int minSpanX = item.spanX;
            int minSpanY = item.spanY;
            if (item.minSpanX > 0 && item.minSpanY > 0) {
                minSpanX = item.minSpanX;
                minSpanY = item.minSpanY;
            }

            boolean nearestDropOccupied = mDragTargetLayout.isNearestDropLocationOccupied((int)
                    mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1], item.spanX,
                    item.spanY, child, mTargetCell);
            //Aurora_xiejun_start
            boolean isWidgetOverView=false;
            boolean isFoundCell=mTargetCell[0]<0||mTargetCell[1]<0;
            if(d.dragInfo instanceof PendingAddWidgetInfo && !isFoundCell){
            	outer:for(int i=mTargetCell[0];i<mTargetCell[0]+((PendingAddWidgetInfo)(d.dragInfo)).spanX;i++){
            		for(int j=mTargetCell[1];j<mTargetCell[1]+((PendingAddWidgetInfo)(d.dragInfo)).spanY;j++){
            			if(layout.isOccupied(i, j)){
            				isWidgetOverView=true;
            				break outer;
            			}
            		}
            	}
            }
			//Aurora_xiejun_end

            if (!nearestDropOccupied&&!isWidgetOverView && (mAuroraSwapTag || !mFromDesktopTag)) {
                mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                        (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                        mTargetCell[0], mTargetCell[1], item.spanX, item.spanY, false,
                        d.dragView.getDragVisualizeOffset(), d.dragView.getDragRegion());
            } else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
                    && !mReorderAlarm.alarmPending() && (mLastReorderX != mTargetCell[0] ||
                    mLastReorderY != mTargetCell[1]) && (mAuroraSwapTag || !mFromDesktopTag)) {
                // Otherwise, if we aren't adding to or creating a folder and there's no pending
                // reorder, then we schedule a reorder
                ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
                        minSpanX, minSpanY, item.spanX, item.spanY, d.dragView, child);
                mReorderAlarm.setOnAlarmListener(listener);
                mReorderAlarm.setAlarm(REORDER_TIMEOUT);
            }

            if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER ||
                    !nearestDropOccupied) {
                if (mDragTargetLayout != null) {
                    mDragTargetLayout.revertTempState();
                }
            }
        }
    }


    private void manageFolderFeedback(ItemInfo info, CellLayout targetLayout,
            int[] targetCell, float distance, View dragOverView, DragObject d) {
        boolean userFolderPending = willCreateUserFolder(info, targetLayout, targetCell, distance,
                false)&&!mLauncher.isHotseatLayout(targetLayout);

        if (mDragMode == DRAG_MODE_NONE && userFolderPending &&
                !mFolderCreationAlarm.alarmPending()) {
            mFolderCreationAlarm.setOnAlarmListener(new
                    FolderCreationAlarmListener(targetLayout, targetCell[0], targetCell[1]));
            mFolderCreationAlarm.setAlarm(FOLDER_CREATION_TIMEOUT);
            return;
        }

        boolean willAddToFolder =
                willAddToExistingUserFolder(info, targetLayout, targetCell, distance);

        if (willAddToFolder && mDragMode == DRAG_MODE_NONE) {
            mDragOverFolderIcon = ((FolderIcon) dragOverView);
            mDragOverFolderIcon.onDragEnter(info, d);
            if (targetLayout != null) {
                targetLayout.clearDragOutlines();
            }
            setDragMode(DRAG_MODE_ADD_TO_FOLDER);
            return;
        }

        if (mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder) {
            setDragMode(DRAG_MODE_NONE);
        }
        if (mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending) {
            setDragMode(DRAG_MODE_NONE);
        }

        return;
    }

    class FolderCreationAlarmListener implements OnAlarmListener {
        CellLayout layout;
        int cellX;
        int cellY;

        public FolderCreationAlarmListener(CellLayout layout, int cellX, int cellY) {
            this.layout = layout;
            this.cellX = cellX;
            this.cellY = cellY;
        }

        public void onAlarm(Alarm alarm) {
            if (mDragFolderRingAnimator == null) {
                mDragFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
            }
            mDragFolderRingAnimator.setCell(cellX, cellY);
            mDragFolderRingAnimator.setCellLayout(layout);
            mDragFolderRingAnimator.animateToAcceptState(false);
            layout.showFolderAccept(mDragFolderRingAnimator);
            layout.clearDragOutlines();
            setDragMode(DRAG_MODE_CREATE_FOLDER);
        }
    }

    class ReorderAlarmListener implements OnAlarmListener {
        float[] dragViewCenter;
        int minSpanX, minSpanY, spanX, spanY;
        DragView dragView;
        View child;

        public ReorderAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
                int spanY, DragView dragView, View child) {
            this.dragViewCenter = dragViewCenter;
            this.minSpanX = minSpanX;
            this.minSpanY = minSpanY;
            this.spanX = spanX;
            this.spanY = spanY;
            this.child = child;
            this.dragView = dragView;
        }

        public void onAlarm(Alarm alarm) {
            int[] resultSpan = new int[2];
            mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                    (int) mDragViewVisualCenter[1], spanX, spanY, mDragTargetLayout, mTargetCell);
            mLastReorderX = mTargetCell[0];
            mLastReorderY = mTargetCell[1];
			//Aurora_xiejun_start
            boolean isPendingWidgetOverView=false;
            boolean isFoundCell=mTargetCell[0]<0||mTargetCell[1]<0;
            if(((DragView)dragView).getIsDragPendingWidget() && !isFoundCell){
//AURORA-START::customized widget::Shi guiqiang::20131018
            	outer:for(int i=mTargetCell[0];i<mTargetCell[0]+spanX;i++){
            		for(int j=mTargetCell[1];j<mTargetCell[1]+spanY;j++){
//AURORA-END::customized widget::Shi guiqiang::20131018
            			if(mDragTargetLayout.isOccupied(i, j)){
            				isPendingWidgetOverView=true;
            				break outer;
            			}
            		}
            	}
            }
            if(!(child instanceof AppWidgetHostView)&&!isPendingWidgetOverView){
			//Aurora_xiejun_end
            mTargetCell = mDragTargetLayout.createArea((int) mDragViewVisualCenter[0],
                (int) mDragViewVisualCenter[1], minSpanX, minSpanY, spanX, spanY,
                child, mTargetCell, resultSpan, CellLayout.MODE_DRAG_OVER);
            }else{
            	mTargetCell[0]=mTargetCell[1]=resultSpan[0]=resultSpan[1]=-1;
            }

            if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
                mDragTargetLayout.revertTempState();
            } else {
                setDragMode(DRAG_MODE_REORDER);
            }

            boolean resize = resultSpan[0] != spanX || resultSpan[1] != spanY;
            mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                mTargetCell[0], mTargetCell[1], resultSpan[0], resultSpan[1], resize,
                dragView.getDragVisualizeOffset(), dragView.getDragRegion());
        }
    }

    @Override
    public void getHitRect(Rect outRect) {
        // We want the workspace to have the whole area of the display (it will find the correct
        // cell layout to drop to in the existing drag/drop logic.
		if (mLauncher.getEditMode() == EditMode.APPWIDGET_ADD) {
			int h = mLauncher.getHotseat().getHeight();
			ThumbnailImage tl = mLauncher.getPreviewContent();
			if (tl != null) {
				h += tl.getHeight();
			} else {
				float height = getResources().getDimension(R.dimen.thumbnail_image_height);
				h += height;
			}
			outRect.set(0, 0, mDisplaySize.x, mDisplaySize.y - h);
		} else {
			//outRect.set(0, 0, mDisplaySize.x, mDisplaySize.y);
			float height = getResources().getDimension(R.dimen.display_height);
			outRect.set(0, 0, mDisplaySize.x, (int)height);
    	}
    }

    /**
     * Add the item specified by dragInfo to the given layout.
     * @return true if successful
     */
    public boolean addExternalItemToScreen(ItemInfo dragInfo, CellLayout layout) {
        if (layout.findCellForSpan(mTempEstimate, dragInfo.spanX, dragInfo.spanY)) {
            onDropExternal(dragInfo.dropPos, (ItemInfo) dragInfo, (CellLayout) layout, false);
            return true;
        }
        mLauncher.showOutOfSpaceMessage(mLauncher.isHotseatLayout(layout));
        return false;
    }

    private void onDropExternal(int[] touchXY, Object dragInfo,
            CellLayout cellLayout, boolean insertAtFirst) {
        onDropExternal(touchXY, dragInfo, cellLayout, insertAtFirst, null);
    }

    /**
     * Drop an item that didn't originate on one of the workspace screens.
     * It may have come from Launcher (e.g. from all apps or customize), or it may have
     * come from another app altogether.
     *
     * NOTE: This can also be called when we are outside of a drag event, when we want
     * to add an item to one of the workspace screens.
     */
    private void onDropExternal(final int[] touchXY, final Object dragInfo,
            final CellLayout cellLayout, boolean insertAtFirst, DragObject d) {
        final Runnable exitSpringLoadedRunnable = new Runnable() {
            @Override
            public void run() {
                mLauncher.exitSpringLoadedDragModeDelayed(true, false, null);
            }
        };

        ItemInfo info = (ItemInfo) dragInfo;
        int spanX = info.spanX;
        int spanY = info.spanY;
        if (mDragInfo != null) {
            spanX = mDragInfo.spanX;
            spanY = mDragInfo.spanY;
        }

        final long container = mLauncher.isHotseatLayout(cellLayout) ?
                LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                    LauncherSettings.Favorites.CONTAINER_DESKTOP;
        final int screen = indexOfChild(cellLayout);
        if (!mLauncher.isHotseatLayout(cellLayout) && screen != mCurrentPage
                && mState != State.SPRING_LOADED) {
            snapToPage(screen);
        }

        if (info instanceof PendingAddItemInfo) {
            final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;

            boolean findNearestVacantCell = true;
            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                if (willCreateUserFolder((ItemInfo) d.dragInfo, cellLayout, mTargetCell,
                        distance, true) || willAddToExistingUserFolder((ItemInfo) d.dragInfo,
                                cellLayout, mTargetCell, distance)) {
                    findNearestVacantCell = false;
                }
            }

            final ItemInfo item = (ItemInfo) d.dragInfo;
            boolean updateWidgetSize = false;
            if (findNearestVacantCell) {
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }
                int[] resultSpan = new int[2];
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], minSpanX, minSpanY, info.spanX, info.spanY,
                        null, mTargetCell, resultSpan, CellLayout.MODE_ON_DROP_EXTERNAL);

                if (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY) {
                    updateWidgetSize = true;
                }
                item.spanX = resultSpan[0];
                item.spanY = resultSpan[1];
            }

            Runnable onAnimationCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    // When dragging and dropping from customization tray, we deal with creating
                    // widgets/shortcuts/folders in a slightly different way
                    switch (pendingInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        int span[] = new int[2];
                        span[0] = item.spanX;
                        span[1] = item.spanY;
                        mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
                                container, screen, mTargetCell, span, null);
                        break;
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:                
                        mLauncher.processShortcutFromDrop(pendingInfo.componentName,
                                container, screen, mTargetCell, null);
                        break;
                    default:
                        throw new IllegalStateException("Unknown item type: " +
                                pendingInfo.itemType);
                    }
                }
            };
            View finalView = pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                    ? ((PendingAddWidgetInfo) pendingInfo).boundWidget : null;

            if (finalView instanceof AppWidgetHostView && updateWidgetSize) {
                AppWidgetHostView awhv = (AppWidgetHostView) finalView;
                AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, item.spanX,
                        item.spanY);
            }

            int animationStyle = ANIMATE_INTO_POSITION_AND_DISAPPEAR;
            if (pendingInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET &&
                    ((PendingAddWidgetInfo) pendingInfo).info.configure != null) {
                animationStyle = ANIMATE_INTO_POSITION_AND_REMAIN;
            }
            animateWidgetDrop(info, cellLayout, d.dragView, onAnimationCompleteRunnable,
                    animationStyle, finalView, true);
        } else {
            // This is for other drag/drop cases, like dragging from All Apps
            View view = null;

            switch (info.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (info.container == NO_ID && info instanceof ApplicationInfo) {
                    // Came from all apps -- make a copy
                    info = new ShortcutInfo((ApplicationInfo) info);
                }
                view = mLauncher.createShortcut(R.layout.application, cellLayout,
                        (ShortcutInfo) info);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
                        (FolderInfo) info, mIconCache);
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + info.itemType);
            }

            // First we find the cell nearest to point at which the item is
            // dropped, without any consideration to whether there is an item there.
            if (touchXY != null) {
                mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                        cellLayout, mTargetCell);
                float distance = cellLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);
                d.postAnimationRunnable = exitSpringLoadedRunnable;
                if (createUserFolderIfNecessary(view, container, cellLayout, mTargetCell, distance,
                        true, d.dragView, d.postAnimationRunnable)) {
                    return;
                }
                if (addToExistingFolderIfNecessary(view, cellLayout, mTargetCell, distance, d,
                        true)) {
                    return;
                }
            }

            if (touchXY != null) {
                // when dragging and dropping, just find the closest free spot
                mTargetCell = cellLayout.createArea((int) mDragViewVisualCenter[0],
                        (int) mDragViewVisualCenter[1], 1, 1, 1, 1,
                        null, mTargetCell, null, CellLayout.MODE_ON_DROP_EXTERNAL);
            } else {
                cellLayout.findCellForSpan(mTargetCell, 1, 1);
            }
        	
            addInScreen(view, container, screen, mTargetCell[0], mTargetCell[1], info.spanX,
                    info.spanY, insertAtFirst);
            cellLayout.onDropChild(view);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
            cellLayout.getShortcutsAndWidgets().measureChild(view);

            if(Favorites.CONTAINER_HOTSEAT == container) {
            	mDropId = info.id;
    			mDropCellX = mTargetCell[0];
    			mDropContainer = container;
    			updateHotseatData();
            }

            LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen,
                    lp.cellX, lp.cellY);

            if (d.dragView != null) {
                // We wrap the animation call in the temporary set and reset of the current
                // cellLayout to its final transform -- this means we animate the drag view to
                // the correct final location.
                setFinalTransitionTransform(cellLayout);           
                mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, view,
                        exitSpringLoadedRunnable);
                resetTransitionTransform(cellLayout);
            }
        }
    }

    public Bitmap createWidgetBitmap(ItemInfo widgetInfo, View layout) {
        int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize(widgetInfo.spanX,
                widgetInfo.spanY, widgetInfo, false);
        int visibility = layout.getVisibility();
        layout.setVisibility(VISIBLE);

        int width = MeasureSpec.makeMeasureSpec(unScaledSize[0], MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(unScaledSize[1], MeasureSpec.EXACTLY);
        Bitmap b = Bitmap.createBitmap(unScaledSize[0], unScaledSize[1],
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        layout.measure(width, height);
        layout.layout(0, 0, unScaledSize[0], unScaledSize[1]);
        layout.draw(c);
        c.setBitmap(null);
        layout.setVisibility(visibility);
        return b;
    }

    private void getFinalPositionForDropAnimation(int[] loc, float[] scaleXY,
            DragView dragView, CellLayout layout, ItemInfo info, int[] targetCell,
            boolean external, boolean scale) {
        // Now we animate the dragView, (ie. the widget or shortcut preview) into its final
        // location and size on the home screen.
        int spanX = info.spanX;
        int spanY = info.spanY;

        Rect r = estimateItemPosition(layout, info, targetCell[0], targetCell[1], spanX, spanY);
        loc[0] = r.left;
        loc[1] = r.top;

        setFinalTransitionTransform(layout);
        float cellLayoutScale =
                mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(layout, loc);
        resetTransitionTransform(layout);

        float dragViewScaleX;
        float dragViewScaleY;
        if (scale) {
            dragViewScaleX = (1.0f * r.width()) / dragView.getMeasuredWidth();
            dragViewScaleY = (1.0f * r.height()) / dragView.getMeasuredHeight();
        } else {
            dragViewScaleX = 1f;
            dragViewScaleY = 1f;
        }

        // The animation will scale the dragView about its center, so we need to center about
        // the final location.
        loc[0] -= (dragView.getMeasuredWidth() - cellLayoutScale * r.width()) / 2;
        loc[1] -= (dragView.getMeasuredHeight() - cellLayoutScale * r.height()) / 2;

        scaleXY[0] = dragViewScaleX * cellLayoutScale;
        scaleXY[1] = dragViewScaleY * cellLayoutScale;
    }

    public void animateWidgetDrop(ItemInfo info, CellLayout cellLayout, DragView dragView,
            final Runnable onCompleteRunnable, int animationType, final View finalView,
            boolean external) {
        Rect from = new Rect();
        mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, from);

        int[] finalPos = new int[2];
        float scaleXY[] = new float[2];
        boolean scalePreview = !(info instanceof PendingAddShortcutInfo);
        getFinalPositionForDropAnimation(finalPos, scaleXY, dragView, cellLayout, info, mTargetCell,
                external, scalePreview);

        Resources res = mLauncher.getResources();
        int duration = res.getInteger(R.integer.config_dropAnimMaxDuration) - 200;

        // In the case where we've prebound the widget, we remove it from the DragLayer
        if (finalView instanceof AppWidgetHostView && external) {
            Log.d(TAG, "6557954 Animate widget drop, final view is appWidgetHostView");
            mLauncher.getDragLayer().removeView(finalView);
        }
        if ((animationType == ANIMATE_INTO_POSITION_AND_RESIZE || external) && finalView != null) {
            Bitmap crossFadeBitmap = createWidgetBitmap(info, finalView);
            dragView.setCrossFadeBitmap(crossFadeBitmap);
            dragView.crossFade((int) (duration * 0.8f));
        } else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET && external) {
            scaleXY[0] = scaleXY[1] = Math.min(scaleXY[0],  scaleXY[1]);
        }

        DragLayer dragLayer = mLauncher.getDragLayer();
        if (animationType == CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION) {        
            mLauncher.getDragLayer().animateViewIntoPosition(dragView, finalPos, 0f, 0.1f, 0.1f,
                    DragLayer.ANIMATION_END_DISAPPEAR, onCompleteRunnable, duration);
        } else {
            int endStyle;
            if (animationType == ANIMATE_INTO_POSITION_AND_REMAIN) {
                endStyle = DragLayer.ANIMATION_END_REMAIN_VISIBLE;
            } else {
                endStyle = DragLayer.ANIMATION_END_DISAPPEAR;;
            }

            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    if (finalView != null) {
                        finalView.setVisibility(VISIBLE);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                }
            };        
            dragLayer.animateViewIntoPosition(dragView, from.left, from.top, finalPos[0],
                    finalPos[1], 1, 1, 1, scaleXY[0], scaleXY[1], onComplete, endStyle,
                    duration, this);
        }
    }

    public void setFinalTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            int index = indexOfChild(layout);
            // Aurora <haojj> <2013-10-18> add for 文件夹如果在Dock被打开有可能出现-1的情况 begin
            if(index == -1||index+1 >= getChildCount()) return;
            // Aurora <haojj> <2013-10-18> end
            mCurrentScaleX = layout.getScaleX();
            mCurrentScaleY = layout.getScaleY();
            mCurrentTranslationX = layout.getTranslationX();
            mCurrentTranslationY = layout.getTranslationY();
            mCurrentRotationY = layout.getRotationY();
            layout.setScaleX(mNewScaleXs[index]);
            layout.setScaleY(mNewScaleYs[index]);
            layout.setTranslationX(mNewTranslationXs[index]);
            layout.setTranslationY(mNewTranslationYs[index]);
            layout.setRotationY(mNewRotationYs[index]);
        }
    }
    public void resetTransitionTransform(CellLayout layout) {
        if (isSwitchingState()) {
            mCurrentScaleX = layout.getScaleX();
            mCurrentScaleY = layout.getScaleY();
            mCurrentTranslationX = layout.getTranslationX();
            mCurrentTranslationY = layout.getTranslationY();
            mCurrentRotationY = layout.getRotationY();
            layout.setScaleX(mCurrentScaleX);
            layout.setScaleY(mCurrentScaleY);
            layout.setTranslationX(mCurrentTranslationX);
            layout.setTranslationY(mCurrentTranslationY);
            layout.setRotationY(mCurrentRotationY);
        }
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    /**
     * Return the current CellInfo describing our current drag; this method exists
     * so that Launcher can sync this object with the correct info when the activity is created/
     * destroyed
     *
     */
    public CellLayout.CellInfo getDragInfo() {
        return mDragInfo;
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     *
     * pixelX and pixelY should be in the coordinate system of layout
     */
    private int[] findNearestArea(int pixelX, int pixelY,
            int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

    void setup(DragController dragController) {
        mSpringLoadedDragController = new SpringLoadedDragController(mLauncher);
        mDragController = dragController;

        // hardware layers on children are enabled on startup, but should be disabled until
        // needed
        updateChildrenLayersEnabled(false);
        //Aurora-start:xiejun
        setWallpaperDimension();
        //setWallpaperDimension(mNeedWallPapaperLoad);
        //Aurora-start:end
    }

    private boolean mAuroraDragOverFlag;
    /**
     * Called at the end of a drag which originated on the workspace.
     */
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete,
            boolean success) {
    	if (success) {
            if (target != this) {
                if (mDragInfo != null) {
                	// Aurora <jialf> <2013-09-13> modify for Drop target begin
                    if (target instanceof DropTarget) {
                        // mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                    	//AURORA-START:xiejun:20130923:ID138
                		// Aurora <jialf> <2013-10-02> modify for dismiss folder begin
						if (target instanceof Folder) {
							if(null != mDragInfo.cell) {
								CellLayout layout = getParentCellLayoutForView(mDragInfo.cell);
								if(null != layout){
			                		// Aurora <jialf> <2013-10-22> modify for fix bug #41 begin	
									if (layout == mLauncher.getHotseat().getLayout()) {
										layout.removeViewWithoutMarkingCells(mDragInfo.cell);
									} else {
										layout.removeView(mDragInfo.cell);
									}
			                		// Aurora <jialf> <2013-10-22> modify for fix bug #41 end	
								}
							}
                    	}else{
                    		// Aurora <jialf> <2013-11-12> modify for fix bug #434 begin
                    		// Aurora <jialf> <2013-10-08> add for Dock data begin
                    		View cell = mDragInfo.cell;
                    		ItemInfo info = (ItemInfo) cell.getTag();
							LauncherApplication.logVulcan.print("onDropCompleted:mDragInfo.cell: " + info);
							if (info instanceof ShortcutInfo) {
								ShortcutInfo sInfo = (ShortcutInfo) info;
								CellLayout parent = getParentCellLayoutForView(cell);
								switch(sInfo.itemType) {
								case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
									if (parent != null && sInfo.container == Favorites.CONTAINER_DESKTOP
											&& !(target instanceof ThumbnailImageItem)) {
										parent.removeView(cell);
										if (target instanceof DeleteDropTarget)
											resetDragInfo();
									}
									if(mAuroraSwapTag) {
										updateHotseatData();
										resetAuroraHotseatData();
									}
									break;
								case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
									
									boolean systemApp = (sInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0;
									if(systemApp) {
										if(mAuroraSwapTag) {
											if(parent == null) {
												resetHotseatChildToScreen(sInfo, cell);
												updateHotseatData();
												resetAuroraHotseatData();
											}
											cell.setVisibility(VISIBLE);
										} else {
											if (parent == null) {
												resetDesktopChildToScreen(sInfo, cell);
												LauncherApplication.logVulcan.print("onDropCompleted:called resetDesktopChildToScreen,info=" + sInfo);
											}
											cell.setVisibility(VISIBLE);
										}
										if (getParentCellLayoutForView(cell) != null)
											getParentCellLayoutForView(cell).markCellsAsOccupiedForView(cell);
									}
									break;
								}
							} else if(info instanceof FolderInfo && (target instanceof ThumbnailImageItem)) {
								CellLayout parent = getParentCellLayoutForView(cell);
								if ((parent != null) && mAuroraSwapTag && !mAuroraDragOverFlag) {
									CellLayout parentLayout = mLauncher.getHotseat().getLayout();
									ShortcutAndWidgetContainer sac = parentLayout.getShortcutsAndWidgets();
									updateAuroraMemoryParams((FolderInfo)info, parentLayout, sac);
								}
								if (mAuroraSwapTag) {
									updateHotseatData();
									resetAuroraHotseatData();
								}
							}else if(info instanceof LauncherAppWidgetInfo){
								mLauncher.resetAuroraUninstallData();
					     	}
		            		// Aurora <jialf> <2013-10-08> add for Dock data end
                    		// Aurora <jialf> <2013-11-12> modify for fix bug #434 end
                    	}
						// Aurora <jialf> <2013-10-02> modify for dismiss folder end
                    	//AURORA-START:xiejun:20130923:ID138
                    } else {
                        getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                    }
                	// Aurora <jialf> <2013-09-13> modify for Drop target end
                }
            } else {
            	resetDragInfo();
            	mLauncher.resetAuroraUninstallData();
            }
        } else if (mDragInfo != null) {
            CellLayout cellLayout;
            if (mLauncher.isHotseatLayout(target)) {
                cellLayout = mLauncher.getHotseat().getLayout();
            } else {
                cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
            }
            //cellLayout.onDropChild(mDragInfo.cell);
            //ht 2014-10-14 cellLayout为null
            if(cellLayout != null){
            	cellLayout.onDropChild(mDragInfo.cell);
            }
        }
      
        if (d.cancelled &&  mDragInfo.cell != null||target == null) {
                mDragInfo.cell.setVisibility(VISIBLE);
        }
		boolean folder = !(d.dragInfo instanceof FolderInfo)
				|| (target instanceof ThumbnailImageItem)
				|| target instanceof Workspace || (target == null);
		if (folder) {
	        mDragOutline = null;
	        mDragInfo = null;
        }

        // Hide the scrolling indicator after you pick up an item
        hideScrollingIndicator(false);
        
//AURORA-START::Fix bug #171::Shi guiqiang::20131025
// There is delay for app deleting, so it needs to delay to match the timeo
        Runnable runnable = mLauncher.getExitRunnable();
        if(success && mFolderOpened && (target instanceof Folder)) {
    		LauncherApplication.logVulcan.print("onDropCompleted: to call exitEditMode(true)!");
    		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
			mLauncher.setFromFolderDrop(true);
			mLauncher.exitEditMode(true);
        } else if(folder){
			if (mLauncher.getEditMode() != Launcher.EditMode.APPWIDGET_ADD) {
				LauncherApplication.logVulcan.print("onDropCompleted: to call postDelayed(exitEditMode)!mNeedDelay = " + mNeedDelay);
				LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
				if(mNeedDelay) {
				postDelayed(mExitEditModeRunnable,250);
				} else {
					postDelayed(mExitEditModeRunnable,350);
				}
			}
			postDelayed(runnable,300);
        }
//AURORA-END::Fix bug #171::Shi guiqiang::20131025
    }	
    
    private boolean mNeedDelay;
    private ComponentName mComponent;
    private int mFlags;
    public static final int DELAY_FLAG = 1;
    
    public void setmNeedDelay(boolean needDelay) {
		mNeedDelay = needDelay;
	}

	private Handler mUninstallHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case DELAY_FLAG:
				mComponent = (ComponentName) msg.obj;
				mFlags = msg.arg1;
				break;
			}
		}
    };
    
	public Handler getmUninstallHandler() {
		return mUninstallHandler;
	}

	private Runnable mExitEditModeRunnable =new Runnable() {
		@Override
		public void run() {
			mLauncher.exitEditMode(true, ismAuroraSwapTag(), true);	
			if (mNeedDelay && mComponent != null) {
				mLauncher.startAuroraApplicationUninstallActivity(mComponent, mFlags);
				mNeedDelay = false;
			}
		}
	};
    void updateItemLocationsInDatabase(CellLayout cl) {
        int count = cl.getShortcutsAndWidgets().getChildCount();

        int screen = indexOfChild(cl);
        int container = Favorites.CONTAINER_DESKTOP;

        if (mLauncher.isHotseatLayout(cl)) {
            screen = -1;
            container = Favorites.CONTAINER_HOTSEAT;
        }

        LauncherApplication.logVulcan.print("140728-p1-updateItemLocationsInDatabase: will request deleting db first item right now, now is " + System.currentTimeMillis()%60000);
        for (int i = 0; i < count; i++) {
            View v = cl.getShortcutsAndWidgets().getChildAt(i);
            ItemInfo info = (ItemInfo) v.getTag();
            // Null check required as the AllApps button doesn't have an item info
            if (info != null && info.requiresDbUpdate) {
                info.requiresDbUpdate = false;
                LauncherModel.modifyItemInDatabase(mLauncher, info, container, screen, info.cellX,
                        info.cellY, info.spanX, info.spanY);
            }
        }
        LauncherApplication.logVulcan.print("140728-p1-updateItemLocationsInDatabase: already requested deleting db last item now, now is " + System.currentTimeMillis()%60000);
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // Do nothing
    }

    public boolean isDropEnabled() {
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Launcher.setScreen(mCurrentPage);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        // We don't dispatch restoreInstanceState to our children using this code path.
        // Some pages will be restored immediately as their items are bound immediately, and 
        // others we will need to wait until after their items are bound.
        mSavedStates = container;
    }

    public void restoreInstanceStateForChild(int child) {
        if (mSavedStates != null) {
            mRestoredPages.add(child);
            CellLayout cl = (CellLayout) getChildAt(child);
            cl.restoreInstanceState(mSavedStates);
        }
    }

    public void restoreInstanceStateForRemainingPages() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!mRestoredPages.contains(i)) {
                restoreInstanceStateForChild(i);
            }
        }
        mRestoredPages.clear();
    }

    @Override
    public void scrollLeft() {
        if (!isSmall() && !mIsSwitchingState) {
            super.scrollLeft();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override
    public void scrollRight() {
        if (!isSmall() && !mIsSwitchingState) {
            super.scrollRight();
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override
    public boolean onEnterScrollArea(int x, int y, int direction) {
        // Ignore the scroll area if we are dragging over the hot seat
        boolean isPortrait = !LauncherApplication.isScreenLandscape(getContext());
        if (mLauncher.getHotseat() != null && isPortrait) {
            Rect r = new Rect();
            mLauncher.getHotseat().getHitRect(r);
            if (r.contains(x, y)) {
                return false;
            }
        }

        boolean result = false;
        //if (!isSmall() && !mIsSwitchingState) {
        if (!isSmall()) {
            mInScrollArea = true;

            final int page = getNextPage() +
                       (direction == DragController.SCROLL_LEFT ? -1 : 1);

            // We always want to exit the current layout to ensure parity of enter / exit
            setCurrentDropLayout(null);

            if (0 <= page && page < getChildCount()) {
                CellLayout layout = (CellLayout) getChildAt(page);
                setCurrentDragOverlappingLayout(layout);

                // Workspace is responsible for drawing the edge glow on adjacent pages,
                // so we need to redraw the workspace when this may have changed.
                invalidate();
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean onExitScrollArea() {
        boolean result = false;
        if (mInScrollArea) {
            invalidate();
            CellLayout layout = getCurrentDropLayout();
            setCurrentDropLayout(layout);
            setCurrentDragOverlappingLayout(layout);

            result = true;
            mInScrollArea = false;
        }
        return result;
    }

    private void onResetScrollArea() {
        setCurrentDragOverlappingLayout(null);
        mInScrollArea = false;
    }

    /**
     * Returns a specific CellLayout
     */
    CellLayout getParentCellLayoutForView(View v) {
        ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layout : layouts) {
            if (layout.getShortcutsAndWidgets().indexOfChild(v) > -1) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the CellLayouts in the workspace.
     */
    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            layouts.add(((CellLayout) getChildAt(screen)));
        }
        if (mLauncher.getHotseat() != null) {
            layouts.add(mLauncher.getHotseat().getLayout());
        }
        return layouts;
    }

    /**
     * We should only use this to search for specific children.  Do not use this method to modify
     * ShortcutsAndWidgetsContainer directly. Includes ShortcutAndWidgetContainers from
     * the hotseat and workspace pages
     */
    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                new ArrayList<ShortcutAndWidgetContainer>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            childrenLayouts.add(((CellLayout) getChildAt(screen)).getShortcutsAndWidgets());
        }
        if (mLauncher.getHotseat() != null) {
            childrenLayouts.add(mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        return childrenLayouts;
    }

    public Folder getFolderForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag && f.getInfo().opened) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public View getViewForTag(Object tag) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    void clearDropTargets() {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                View v = layout.getChildAt(j);
                if (v instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget) v);
                }
            }
        }
    }

    /**
     * this method deletes applications, shortcuts and widgets in the workspace
     * @param appsToRmv applications and shortcuts. must not be null
     * @param widgetsToRmv widgets. must not be null
     */
    void removeItems(final HashSet<Intent> appsToRmv, final HashSet<String> widgetsToRmv) {

		//Thread.dumpStack();
        //final HashSet<Intent> appsToRmv = new HashSet<Intent>();
        //appsToRmv.addAll(packages);

    	if(mLauncher != null) {
    		if(mLauncher.isWorkspaceBinding()) {
    			LauncherApplication.logVulcan.print("removeItems: it is very dangerous to call this method when binding workspace!!!");
    		}
    	}

    	for(Intent intentToRmv: appsToRmv) {
    		LauncherApplication.logVulcan.print("removeItems: to remove item: " + intentToRmv.toUri(0));
    	}

    	//solution to prevent make mistakes.
    	//if(mLauncher != null) {
    	//	if(mLauncher.isWorkspaceBinding()) {
    	//		Handler handler = new Handler(Looper.getMainLooper());
    	//		handler.postDelayed(new Runnable() {
    	//			public void run() {
    	//				removeItems(appsToRmv,widgetsToRmv);
    	//			}
    	//		},100);
    	//	}
    	//}

        ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
        for (final CellLayout layoutParent: cellLayouts) {
            final ViewGroup layout = layoutParent.getShortcutsAndWidgets();

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();

                    // Aurora <haojj> <2013-12-18> add for ALPHA begin
                    ArrayList<ShortcutInfo> removeQuickIndexItems = new ArrayList<ShortcutInfo>();
                    // Aurora <haojj> <2013-12-18> end
                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        
                        if(view == null) {
                        	LauncherApplication.logVulcan.print(String.format("removeItems: childView = %d/%d",j,childCount));
                        	continue;
                        }

                        Object tag = view.getTag();

                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            final ComponentName name = intent.getComponent();

                            if (name != null) {
                                //if (packageNames.contains(name.getPackageName())) {
                                if (appsToRmv.contains(intent)) {
                                	
                                	Log.d("vulcan-setup","removeItems: intent of desktop = " + intent);
                                	LauncherApplication.logVulcan.print("to call deleteItemFromDatabase,info = " + info.id);
                                	removeQuickIndexItems.add(info);
                                    LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                    childrenToRemove.add(view);
                                }
							}
                            // Aurora <jialf> <2014-02-14> added for fix bug #2214 begin
                            else if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
									&& info.iconResource != null) {
								//String sPkgName = info.iconResource.packageName;
								//if (sPkgName != null && packageNames.contains(sPkgName)) {
								if (appsToRmv.contains(intent)) {
									LauncherModel.deleteItemFromDatabase(mLauncher, info);
									childrenToRemove.add(view);
								}
							}
                            // Aurora <jialf> <2014-02-14> added for fix bug #2214 end
                        } else if (tag instanceof FolderInfo) {
                            final FolderInfo info = (FolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final int contentsCount = contents.size();
                            final ArrayList<ShortcutInfo> appsToRemoveFromFolder =
                                    new ArrayList<ShortcutInfo>();

                            //save to be deleted view list;
                            //ht 2014-09-04
                            if(removeViewList == null){
                            	removeViewList = new ArrayList<ShortcutInfo>();
                            }
                            removeViewList.clear();

                            for (int k = 0; k < contentsCount; k++) {
                                final ShortcutInfo appInfo = contents.get(k);
                                final Intent intent = appInfo.intent;
                                final ComponentName name = intent.getComponent();

                                if (name != null) {
                                    //if (packageNames.contains(name.getPackageName())) {
                                	if (appsToRmv.contains(intent)) {
                                        appsToRemoveFromFolder.add(appInfo);
                                    }
                                }
                                else if (appInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT
    									&& appInfo.iconResource != null) {
    								//String sPkgName = appInfo.iconResource.packageName;
    								//if (sPkgName != null && packageNames.contains(sPkgName)) {
                                	if (appsToRmv.contains(intent)) {
    									appsToRemoveFromFolder.add(appInfo);
    								}
    							}
                            }

                            //ht 2014-09-04
                            removeViewList.addAll(appsToRemoveFromFolder);

                            for (ShortcutInfo item: appsToRemoveFromFolder) {
                            	// Aurora <haojj> <2013-10-2> add for ALPHA begin
                            	removeQuickIndexItems.add(item);
                            	info.onProcessAfterUnload();
								// Aurora <haojj> <2013-10-2> end
                                info.remove(item);
                                LauncherModel.deleteItemFromDatabase(mLauncher, item);
                            }
                        } else if (tag instanceof LauncherAppWidgetInfo) {
                            final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                            final ComponentName provider = info.providerName;
                            if (provider != null) {
                                //if (packageNames.contains(provider.getPackageName())) {
                            	if(widgetsToRmv.contains(provider.getPackageName())){
                                    LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                    childrenToRemove.add(view);
                                }
                            }
                        }
                    }

                    // Aurora <haojj> <2013-12-18> add for begin
                    mLauncher.removeQuickIndexItems(removeQuickIndexItems);
                    // Aurora <haojj> <2013-12-18> end
                    
                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        ItemInfo info = (ItemInfo) child.getTag();
                        boolean fromHotseat = false;
        				if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
        					fromHotseat = true;
        					ShortcutAndWidgetContainer sac = mLauncher.getHotseat()
        							.getLayout().getShortcutsAndWidgets();
                    		updateAuroraMemoryParams(info,sac);
        				}
                        // Note: We can not remove the view directly from CellLayoutChildren as this
                        // does not re-mark the spaces as unoccupied.
                        layoutParent.removeViewInLayout(child);
                        if(fromHotseat) {
                        	final CellLayout parentLayout = mLauncher.getHotseat().getLayout();
        					parentLayout.setAuroraCellX(false);
        					parentLayout.setAuroraGridSize(-1);
        					updateHotseatData();
        					resetAuroraHotseatData();
        					fromHotseat = false;
                        }
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }
                    }

                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                    }
                }
            });
        }

        // Clean up new-apps animation list
        final Context context = getContext();
        post(new Runnable() {
            @Override
            public void run() {
                String spKey = LauncherApplication.getSharedPreferencesKey();
                SharedPreferences sp = context.getSharedPreferences(spKey,
                        Context.MODE_PRIVATE);
                Set<String> newApps = sp.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY,
                        null);

                // Remove all queued items that match the same package
                if (newApps != null) {
                	ArrayList<String> pkgList = new ArrayList<String>();
                	for(Intent intent: appsToRmv) {
                		String pkgName = ItemInfo.getPackageName(intent);
                		if(pkgName == null) {
                			continue;
                		}
                		pkgList.add(pkgName);
                	}
                    synchronized (newApps) {
                        Iterator<String> iter = newApps.iterator();
                        while (iter.hasNext()) {
                            try {
                                Intent intent = Intent.parseUri(iter.next(), 0);
                                String pn = ItemInfo.getPackageName(intent);
                                if (pkgList.contains(pn)) {
                                    iter.remove();
                                }

                                // It is possible that we've queued an item to be loaded, yet it has
                                // not been added to the workspace, so remove those items as well.
                                ArrayList<ItemInfo> shortcuts;
                                shortcuts = LauncherModel.getWorkspaceShortcutItemInfosWithIntent(
                                        intent);
                                for (ItemInfo info : shortcuts) {
                                    LauncherModel.deleteItemFromDatabase(context, info);
                                }
                            } catch (URISyntaxException e) {}
                        }
                    }
                }
            }
        });
    }

    void updateShortcuts(ArrayList<ApplicationInfo> apps) {
        ArrayList<ShortcutAndWidgetContainer> childrenLayouts = getAllShortcutAndWidgetContainers();
        for (ShortcutAndWidgetContainer layout: childrenLayouts) {
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo) tag;
                    // We need to check for ACTION_MAIN otherwise getComponent() might
                    // return null for some shortcuts (for instance, for shortcuts to
                    // web pages.)
                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                            Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                        final int appCount = apps.size();
                        for (int k = 0; k < appCount; k++) {
                            ApplicationInfo app = apps.get(k);
                            if (app.componentName.equals(name)) {
                                BubbleTextView shortcut = (BubbleTextView) view;
                                info.updateIcon(mIconCache);
                                info.title = app.title.toString();
                                shortcut.applyFromShortcutInfo(info, mIconCache);
                            }
                        }
                    }
                }
            }
        }
    }

    void moveToDefaultScreen(boolean animate) {
		// if (!isSmall()){
        if (!isSmall()&&mState!=State.WORKSPACE_EDIT) {
        	Log.i("xiejun9","mDefaultPage="+mDefaultPage+"  ,mCurrentPage="+mCurrentPage);
            if (animate) {
                snapToPage(mDefaultPage,PAGE_SNAP_ANIMATION_DURATION+(Math.abs(mCurrentPage-mDefaultPage))*50);
            } else {
                setCurrentPage(mDefaultPage);
            }
        }
        getChildAt(mDefaultPage).requestFocus();
    }

    @Override
    public void syncPages() {
    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
    }

    @Override
    protected String getCurrentPageDescription() {
        int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
        return String.format(getContext().getString(R.string.workspace_scroll_format),
                page + 1, getChildCount());
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    void setFadeForOverScroll(float fade) {
        if (!isScrollingIndicatorEnabled()) return;

        mOverscrollFade = fade;
        float reducedFade = 0.5f + 0.5f * (1 - fade);
        final ViewGroup parent = (ViewGroup) getParent();
//AURORA-START::Fix bug #314::Shi guiqiang::20131031
//        final ImageView qsbDivider = (ImageView) (parent.findViewById(R.id.qsb_divider));
//        final ImageView dockDivider = (ImageView) (parent.findViewById(R.id.dock_divider));
//AURORA-END::Fix bug #314::Shi guiqiang::20131031
        //final View scrollIndicator = getScrollingIndicator();

        //cancelScrollingIndicatorAnimations();
//AURORA-START::Comment out for page indicator showing all the time::Comment out::Shi guiqiang::20130908
        /*
        if (qsbDivider != null) qsbDivider.setAlpha(reducedFade);
        if (dockDivider != null) dockDivider.setAlpha(reducedFade);
        scrollIndicator.setAlpha(1 - fade);
        */
//AURORA-END::Comment out for page indicator showing all the time::Comment out::Shi guiqiang::20130908
    }

    // Aurora <jialf> <2013-09-09> add for loading data begin
	public int getmDefaultPage() {
		return mDefaultPage;
	}
    // Aurora <jialf> <2013-09-09> add for loading data end
	public void setCelllayoutSEditbackgroud(boolean setOrNot,boolean animated){
		//Log.i("xiejun","setCelllayoutSEditbackgroud="+setOrNot+"this.getChildCount()");
		CellLayout cl=null;
		if(setOrNot){
			for(int i=0;i<this.getChildCount();i++){
				cl = (CellLayout) getChildAt(i);
				cl.setBackgroundResource(R.drawable.celllayout_scale_bg);
				if(i==getCurrentPage()){
					if(cl!=null&&animated){
						cl.getBackground().setAlpha(0);
					}else{
						cl.getBackground().setAlpha(255);
					}
				}else{
					if(cl!=null){
						cl.getBackground().setAlpha(255);
					}
				}
			}
		}else{
			for(int i=0;i<this.getChildCount();i++){
				cl = (CellLayout) getChildAt(i);
				if(animated){
					if(i!=getCurrentPage()&&cl!=null&&cl.getBackground()!=null){
						cl.getBackground().setAlpha(0);
					}
				}else{
					if(cl!=null&&cl.getBackground()!=null){
						cl.getBackground().setAlpha(0);
					}
				}
			}
		}
	}

	// Aurora <jialf> <2013-10-08> modify for Dock data begin
	// Aurora <jialf> <2013-09-10> add for dock data begin
	private long mDropContainer = -1;
	private int mDropCellX = -1;
	private long mDropId = -1;
	boolean mNeedUpdateHotseat;
	private Context mContext;
	
	public void updateHotseatData() {
		/*if (mDropCellX == -1 || mDropId == -1 || mDropContainer == -1)
			return;*/
		
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		CellLayout hotseat = mLauncher.getHotseat().getLayout();
		ShortcutAndWidgetContainer sac = hotseat.getShortcutsAndWidgets();
		int count = sac.getChildCount();
		Log.i(TAG, mDropId +", " + mDropCellX +", " + mDropContainer +", count = "+count);
		for (int i = 0; i < count; i++) {
			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newUpdate(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION);
			View v = sac.getChildAt(i);
			if(v instanceof BubbleTextView){
				ItemInfo info = (ItemInfo) v.getTag();
				
				CellLayout.LayoutParams clp = (CellLayout.LayoutParams) v.getLayoutParams();
				ContentValues values = LauncherModel.modifyHotseatChild(clp.cellX);
				
				builder.withValues(values).withSelection("_id=" + info.id, null);
				operationList.add(builder.build());	
				
			}
			
		}
		try {
			mContext.getContentResolver().applyBatch(
					LauncherProvider.AUTHORITY, operationList);
		} catch (Exception e) {
			Log.i(TAG, "Workspace applyBatch uodate exception ...");
		}
		operationList.clear();
		
		/*String where = null;
		if (mDropContainer == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			where = LauncherSettings.Favorites.CELLX + ">" + mDropCellX
					+ " and " + LauncherSettings.Favorites.CONTAINER + "="
					+ LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		} else if (mDropContainer == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			where = LauncherSettings.Favorites.CELLX + ">=" + mDropCellX
					+ " and " + LauncherSettings.Favorites.CONTAINER + "="
					+ LauncherSettings.Favorites.CONTAINER_HOTSEAT + " and "
					+ LauncherSettings.Favorites._ID + "!=" + mDropId;
		}
		Log.i(TAG, "?????????-----where = " + where);
		Cursor cur = LauncherModel.queryHotseatData(mLauncher, where, null);
		if (cur != null) {
			Log.i(TAG, "============ cur.count = " + cur.getCount());
			while (cur.moveToNext()) {
				int idIndex = cur
						.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
				int cellXIndex = cur
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				int screenIndex = cur
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				LauncherModel.updateHotseatCellX(mLauncher,
						cur.getInt(idIndex), cur.getInt(cellXIndex),
						cur.getInt(screenIndex), (int) mDropContainer);
			}
			cur.close();
		}*/
		
	}

	// Aurora <jialf> <2013-11-12> add for fix bug #434 begin
	public void resetHotseatChildToScreen(ItemInfo item, View dragView){
		final View cell = dragView;
		final ItemInfo sInfo = item;
        sInfo.screen = mDragItemInfo[0];
		sInfo.cellX = mDragItemInfo[1];
		sInfo.cellY = mDragItemInfo[2];
		sInfo.container = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		mDropId = sInfo.id;
		mDropContainer = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		mDropCellX = sInfo.cellX;
		if (mAuroraSwapTag) {
			CellLayout hotseat = mLauncher.getHotseat().getLayout();
			hotseat.setAuroraCellX(true);
			ShortcutAndWidgetContainer sac = hotseat.getShortcutsAndWidgets();
			CellLayout.LayoutParams clp;
			ItemInfo info;
			int count = sac.getChildCount();
			for (int i = 0; i < count; i++) {
				View v = sac.getChildAt(i);
				clp = (CellLayout.LayoutParams)v.getLayoutParams();
				if (clp.cellX >= mDropCellX) {
					info = (ItemInfo) v.getTag();
					info.cellX++;
					info.screen++;
					clp.cellX++;
					clp.useTmpCoords = false;
					v.setTag(info);
				}
			}
			cell.setTag(sInfo);
			CellLayout.LayoutParams cl = (CellLayout.LayoutParams) cell.getLayoutParams();
			cl.cellX = cl.tmpCellX = sInfo.cellX;
			cl.cellY = cl.tmpCellY = sInfo.cellY;
			cl.cellHSpan = sInfo.spanX;
			cl.cellVSpan = sInfo.spanY;
			cl.useTmpCoords = false;
			cl.isLockedToGrid = true;
			cell.setId(LauncherModel.getCellLayoutChildId(sInfo.container,
					sInfo.screen, sInfo.cellX, sInfo.cellY, sInfo.spanX, sInfo.spanY));
			sac.addView(cell, mDragItemInfo[0]);
			// updateHotseatData();
			hotseat.setAuroraGridSize(-1);
			LauncherModel.moveItemInDatabase(mLauncher, sInfo, sInfo.container,
							sInfo.screen, sInfo.cellX, sInfo.cellY);
		}
	}
	
	private void resetDesktopChildToScreen(final ItemInfo info, final View cell) {
		info.screen = mDragItemInfo[0];
		info.cellX = mDragItemInfo[1];
		info.cellY = mDragItemInfo[2];
		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		cell.setTag(info);
		CellLayout.LayoutParams clp = (CellLayout.LayoutParams) cell
				.getLayoutParams();
		clp.cellX = clp.tmpCellX = info.cellX;
		clp.cellY = clp.tmpCellY = info.cellY;
		clp.cellHSpan = info.spanX;
		clp.cellVSpan = info.spanY;
		clp.useTmpCoords = false;
		clp.isLockedToGrid = true;
		cell.setId(LauncherModel.getCellLayoutChildId(info.container
				, info.screen, info.cellX, info.cellY, info.spanX, info.spanY));
		addInScreen(cell, info.container, info.screen, info.cellX, info.cellY, 1, 1);
	}

	// Aurora <jialf> <2013-11-15> modify for fix bug #748 begin
	public void resetAuroraChild(View dragView, boolean paused) {
		final View cell = dragView;
		CellLayout parent = getParentCellLayoutForView(cell);
        ItemInfo item = (ItemInfo) cell.getTag();
        /*
        ShortcutInfo sInfo = null;
        if(item instanceof ShortcutInfo)
        	sInfo = (ShortcutInfo) item;
		boolean systemApp = sInfo != null && (sInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0;
		*/
		if (mDisableView != null) {
			mDisableView.setVisibility(VISIBLE);
			mDisableView = null;
		}
		final int cellX = item.cellX;
		final long container = item.container;
		
		boolean desktopChild = paused && (parent != null) && !mAuroraSwapTag && container == LauncherSettings.Favorites.CONTAINER_DESKTOP;
		
		//AURORA_START:ht-2014-10-30 fix BUG#9416;
		/*if (desktopChild || (!paused  && (parent != null || systemApp)))
			return;
		*/
		if(desktopChild || (!paused  && (parent != null)))
			return;
		//AURORA_END:ht-2014-10-30 fix BUG#9416;
		
		
		item.screen = mDragItemInfo[0];
		item.cellX = mDragItemInfo[1];
		item.cellY = mDragItemInfo[2];
		if(mAuroraSwapTag) {
			resetHotseatChildToScreen(item, cell);
			updateHotseatData();
		} else {
			item.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
			cell.setTag(item);
			Log.i(TAG, "after reset date is : " + cell.getTag().toString());
			CellLayout.LayoutParams clp = (CellLayout.LayoutParams) cell
					.getLayoutParams();
			clp.cellX = clp.tmpCellX = item.cellX;
			clp.cellY = clp.tmpCellY = item.cellY;
			clp.cellHSpan = item.spanX;
			clp.cellVSpan = item.spanY;
			clp.useTmpCoords = false;
			clp.isLockedToGrid = true;
			cell.setId(LauncherModel.getCellLayoutChildId(item.container
					, item.screen, item.cellX, item.cellY, item.spanX, item.spanY));
			if (parent != null && container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
				parent.removeView(cell);
				CellLayout hotseat = mLauncher.getHotseat().getLayout();
				if (mDisableView == null) {
					updateAuroraMemoryParams(item.id, cellX, hotseat);
					updateHotseatData();
				}
			}
			addInScreen(cell, item.container, item.screen, item.cellX, item.cellY, 1, 1);
		}
		resetAuroraHotseatData();
	}
	// Aurora <jialf> <2013-11-15> modify for fix bug #748 end
	// Aurora <jialf> <2013-11-12> add for fix bug #434 end
	
    // Aurora <jialf> <2013-09-10> add for dock data end
	
	//Aurora-start:xiejun:20130928:ID194
	private int mWillPage=0;
	private int mCurrentWillDeletePage=0;
	public synchronized void autoManagePages(){
		Log.i("linp","----------------------------autoManagePages begin------------------------------");
		LauncherApplication.logVulcan.print("autoManagePages: to call mAutoManagerPagesRunable,delay = 20!");
		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
		LauncherModel.enableInstallQueue();
		removeCallbacks(mAutoManagerPagesRunable);
		postDelayed(mAutoManagerPagesRunable, 20);

	}
	
	public synchronized void autoManagePagesImediatly(){
		
		if(mLauncher.isWorkspaceLoading()){
			LauncherApplication.logVulcan.print("140728-autoManagePagesImediatly: it's canceled becuase now is binding workspace");
			return;
		}
		
		LauncherApplication.logVulcan.print("140728-autoManagePagesImediatly: it is doing page autoManagement");
		
		//LauncherModel.enableInstallQueue();
		mCurrentWillDeletePage=mWillPage=getCurrentPage();
		ArrayList<View> views=findEmptyScreens();

		LauncherApplication.logVulcan.print("autoManagePagesImediatly: to call deleteEmpty!");
		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));

		if(views.size()<=0){
			mLauncher.writePagesInDBCompleted();
			before_edit_pagesCount = getChildCount();		
			return;
		}
	
		
		Log.d("vulcan-automan","autoManagePagesImediatly: view.size = " + views.size());
		
		deleteEmpty(views);
		deleteEmptyPagesInDB();
		if(mWillPage<=0||getChildCount()==1){
			mWillPage=0;
		}
		setCurrentPage(mWillPage);
		before_edit_pagesCount = getChildCount();		
		//LauncherModel.disableAndFlushInstallQueue();
	}
	Runnable mAutoManagerPagesRunable =new Runnable() {
		@Override
		public void run() {
			autoManagePagesImediatly();
			LauncherModel.disableAndFlushInstallQueue();
		
			Log.i("linp","---------------------------- autoManagePages end --------------------------------");
		}
	};
	
	// Aurora <jialf> <2013-10-08> modify for Dock data end
	private ArrayList<View> findEmptyScreens(){
		LauncherApplication.logVulcan.print("140728-p2-findEmptyScreens: will read workspace right now!, now is " + System.currentTimeMillis()%60000);
		
		ArrayList<View> views=new ArrayList<View>();
		int count=this.getChildCount();
		for(int i=count -1;i>=0;i--){
			View container = this.getChildAt(i);
			if(container instanceof CellLayout){
				if((((CellLayout) container).getShortcutsAndWidgets().getChildCount())<=0){
					views.add(container);
					if(this.indexOfChild(container)<mCurrentWillDeletePage){
						mWillPage--;
					}
				}
					
			}
		}
		
		LauncherApplication.logVulcan.print("140728-p2-findEmptyScreens: have already completed reading now!, now is " + System.currentTimeMillis()%60000);
		return views;
	}
	
	/**
	 * vulcan edited this method in 2014-8-13
	 * this method no longer accesses launcher.db
	 * @param views
	 * @return
	 */
	private boolean deleteEmpty(ArrayList<View> views){
		int currentRemovedScreen=-1;
		for (View v:views) {
			currentRemovedScreen=this.indexOfChild(v);
			if (getChildCount() == 1){
				views.clear();
				return true;
			}
			removeView(v);
			notifyPageCountChange(currentRemovedScreen,true);
			
			//vulcan removed it in 2014-8-13
			//we will deleted garbage pages in the loader task
			//updateDataForRemoveEmptyPage(currentRemovedScreen);
		}
		views.clear();
		return true;
	}
	
	/**
	 * vulcan created this method in 2014-8-13
	 * this method tells load task to delete garbage pages.
	 */
	private void deleteEmptyPagesInDB() {
		Log.e("linp", "###############deleteEmptyPagesInDB");
		if(null == mLauncher) {
			return;
		}
		
		LauncherModel lm = mLauncher.getModel();
		if(null == lm) {
			return;
		}
		
		lm.apm.writePagesInWorkerThread();
		
		return;
	}
	
	private boolean  updateDataForRemoveEmptyPage(int screen){

		LauncherApplication.logVulcan.print("updateDataForRemoveEmptyPage: screen = " + screen);
		Log.i("xiejun7","*************** auto manage page delete:"+screen+"*****************");
		int count =getChildCount();
		for(int i=screen;i<count;i++){
			View view=getChildAt(i);
			if(view!=null&&view instanceof CellLayout){
				((CellLayout)view).updateCelllayoutScreen();
			}
		}
		Log.i("xiejun7","***************auto manage page delete:"+screen+"*****************");
		return false;
	}
	//Aurora-end:xiejun:20130928:ID194

	// Aurora <jialf> <2013-10-08> add for Dock data begin
	private boolean mNeedSwapPosition;
    // Aurora <jialf> <2013-10-30> modify for fix bug #174 begin
    private View mDisableView;
    private int mTempLoc = -1;
    private boolean mFromHotseat;
    private boolean mAuroraSwapTag;
    private boolean mFromDesktopTag;
	private int[] mDragItemInfo = new int[] { -1, -1, -1 };
	private float mAuroraMaxDistanceForFolderCreation;
	
    private int mHotseatChildCount = -1;

	// Aurora <jialf> <2013-10-31> modify for fix bug #313 begin
	public boolean ismAuroraSwapTag() {
		return mAuroraSwapTag;
	}
	// Aurora <jialf> <2013-10-31> modify for fix bug #313 end

	private int[] getAuroraPosition(final CellLayout.CellInfo cellInfo){
    	final ItemInfo info = (ItemInfo) cellInfo.cell.getTag();
    	mDragItemInfo[0] = info.screen;
    	mDragItemInfo[1] = info.cellX;
    	mDragItemInfo[2] = info.cellY;
		if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			mAuroraSwapTag = true;
		}
    	return mDragItemInfo;
    }
	
	private void resetDragInfo() {
		mDragItemInfo[0] = -1;
		mDragItemInfo[1] = -1;
		mDragItemInfo[2] = -1;
	}
	
	public void updateAuroraMemoryParams(ItemInfo info,
			CellLayout parentLayout, ShortcutAndWidgetContainer sac) {
		updateAuroraMemoryParams(info, sac);
		// Aurora <jialf> <2013-11-22> modify for fix bug #643 begin
		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		mDragInfo.cell.setTag(info);
		// Aurora <jialf> <2013-11-22> modify for fix bug #643 end
		parentLayout.removeView(mDragInfo.cell);
		parentLayout.setAuroraCellX(false);
		parentLayout.setAuroraGridSize(-1);
		/*updateHotseatData();
		resetAuroraData();*/
	}
	
	// Aurora <jialf> <2014-02-11> modify for fix bug #2165 begin
	public void updateAuroraHoseatDataFromFolder() {
		CellLayout layout = getParentCellLayoutForView(mDragInfo.cell);
		if (layout != null && mLauncher.isHotseatLayout(layout)) {
			Log.i("jialf", "********updateAuroraHoseatDataFromFolder()******");
			final ItemInfo info = (ItemInfo) mDragInfo.cell.getTag();
			updateAuroraMemoryParams(info, layout,
					layout.getShortcutsAndWidgets());
			resetAuroraHotseatData();
		}
	}
	// Aurora <jialf> <2014-02-11> modify for fix bug #2165 end

	public void updateAuroraMemoryParams(ItemInfo info,
			ShortcutAndWidgetContainer sac) {
		mDropId = info.id;
		mDropCellX = info.cellX;
		mDropContainer = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		CellLayout.LayoutParams cl;
		ItemInfo sInfo;
		int count = sac.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = sac.getChildAt(i);
			sInfo = (ItemInfo) v.getTag();
			cl = (CellLayout.LayoutParams) v.getLayoutParams();
			if (cl.cellX > mDropCellX) {
				cl.cellX--;
				cl.useTmpCoords = false;
				sInfo.cellX--;
				sInfo.screen--;
				v.setLayoutParams(cl);
				v.setTag(sInfo);
			}
		}
	}

	// Aurora <jialf> <2013-11-15> add for fix bug #748 begin
	public void updateAuroraMemoryParams(long id, int cellX,
			CellLayout parentLayout) {
		mDropId = id;
		mDropCellX = cellX;
		mDropContainer = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		CellLayout.LayoutParams cl;
		ItemInfo sInfo;
		ShortcutAndWidgetContainer sac = parentLayout.getShortcutsAndWidgets();
		int count = sac.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = sac.getChildAt(i);
			sInfo = (ItemInfo) v.getTag();
			cl = (CellLayout.LayoutParams) v.getLayoutParams();
			if (cl.cellX > mDropCellX) {
				cl.cellX--;
				cl.useTmpCoords = false;
				sInfo.cellX--;
				sInfo.screen--;
				v.setLayoutParams(cl);
				v.setTag(sInfo);
			}
		}
		parentLayout.setAuroraCellX(false);
		parentLayout.setAuroraGridSize(-1);
		/*updateHotseatData();
		resetAuroraHotseatData();*/
	}
	// Aurora <jialf> <2013-11-15> add for fix bug #748 end
	
	public void updateAuroraHotseatParams(ItemInfo item,
			CellLayout parentLayout, CellLayout hotseat, int cellX) {
		mDropId = item.id;
 		mDropContainer = LauncherSettings.Favorites.CONTAINER_DESKTOP;
 		mDropCellX = cellX;
 		ShortcutAndWidgetContainer sac = hotseat.getShortcutsAndWidgets();
		CellLayout.LayoutParams cl;
		ItemInfo sInfo ;
		int count = sac.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = sac.getChildAt(i);
			sInfo = (ItemInfo) v.getTag();
			cl = (CellLayout.LayoutParams) v.getLayoutParams();
			if (cl.cellX >= mDropCellX) {
				cl.cellX++;
				cl.useTmpCoords = false;
				sInfo.cellX++;
				sInfo.screen++;
				v.setLayoutParams(cl);
				v.setTag(sInfo);
			}
		}
		View cell = mDragInfo.cell;
		if (parentLayout != null)
			parentLayout.removeView(cell);
		sInfo = (ItemInfo) cell.getTag();
		cl = (CellLayout.LayoutParams) cell.getLayoutParams();
		sInfo.screen = cellX;
		cl.cellX = sInfo.cellX = cellX;
		cl.cellY = sInfo.cellY = 0;
		cl.useTmpCoords = false;
		sInfo.container = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		cell.setTag(sInfo);
		cell.setLayoutParams(cl);
    	// Aurora <jialf> <2013-11-11> modify for fix bug #622 begin
		sac.addView(cell, cellX);
    	// Aurora <jialf> <2013-11-11> modify for fix bug #622 end
		hotseat.setAuroraGridSize(-1);
		// updateHotseatData();
	}
    
    public void resetAuroraData(){
    	mDropId = -1;
		mDropCellX = -1;
		mDropContainer = -1;
		mTempLoc = -1;
		mFromHotseat = false;
		mFromDesktopTag = false;
		mDisableView = null;
		mHotseatChildCount = -1;
    }
    
    public void resetAuroraHotseatData(){
    	resetAuroraData();
		mAuroraSwapTag = false;
		mAuroraDragOverFlag = false;
    }
    
    public void setAuroraDragInfoVisible() {
		if (mDragInfo != null) {
			View v = mDragInfo.cell;
			v.setVisibility(VISIBLE);
			if (v instanceof FolderIcon) {
				((FolderIcon) v).setTextVisible(true);
			} else if(v instanceof BubbleTextView){
				((BubbleTextView) v).setTextColor(getResources()
						.getColor(R.color.workspace_icon_text_color));
			}
		}
    }
    // Aurora <jialf> <2013-10-30> modify for fix bug #174 end
    
    private void setDefaultWallPaper(){
    	DisplayMetrics dm =   this.getResources().getDisplayMetrics(); 
        int width=dm.widthPixels;
        int height=dm.heightPixels;
    	if(mWallpaperManager!=null){
    		try {
    			mWallpaperManager.setResource(R.drawable.wallpaper_01);	
    			Drawable wallpaper = getResources().getDrawable(
    					R.drawable.wallpaper_01);
    			if(wallpaper.getIntrinsicHeight()>wallpaper.getIntrinsicWidth()){
    				mWallpaperManager.suggestDesiredDimensions(width, height);
    			}else{
    				mWallpaperManager.suggestDesiredDimensions(2*width, height);
    			}		
			} catch (IOException e) {
				Log.i(TAG,"Setting default wallpaper fail.");
			}
    	}
    }
	// Aurora <jialf> <2013-10-08> add for Dock data end
    
    public int[] getDragItemInfo(){
    	return mDragItemInfo;
    }
    
    //ht 2014-11-28
    //去掉文件夹解散后占用位置; 但图标被隐藏了;
    /*public void removeItemViewFromItsParent(boolean isFromHotseat){
    	CellLayout aparent = getParentCellLayoutForView(mLauncher.mAuroraDragView);
		if(aparent != null){
			ItemInfo item = (ItemInfo)mLauncher.mAuroraDragView.getTag();
            ShortcutAndWidgetContainer sac = aparent.getShortcutsAndWidgets();
            aparent.removeView(sac.getChildAt(item.cellX, item.cellY));
            
            before_edit_pagesCount = getChildCount();
            
            if(isFromHotseat){
                aparent.setAuroraCellX(false);
                aparent.setAuroraGridSize(-1);
            }
		}
    }*/

    Handler mDismissFolderHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mAuroraSwapTag = (Boolean) msg.obj;
			switch(msg.what) {
			case DeleteDropTarget.DISMISS_FOLDER_CONFIRM_FLAG:
				updateAuroraHotseatData();
				break;
			case DeleteDropTarget.DISMISS_FOLDER_CANCEL_FLAG:
				if (mDragInfo != null) {
					View cell = mDragInfo.cell;
					CellLayout parent = getParentCellLayoutForView(cell);
					Log.i(TAG, "[ " + mAuroraSwapTag + ", " + (parent == null) + " ]");
					if (parent == null) {
						resetAuroraFolder(parent, cell);
					}
					cell.setVisibility(VISIBLE);
				}
				if(!mAuroraSwapTag) {
					CellLayout layout = (CellLayout) getChildAt(mDragItemInfo[0]);
					View view = layout.getShortcutsAndWidgets().getChildAt(
							mDragItemInfo[1], mDragItemInfo[2]);
					layout.markCellsAsOccupiedForView(view);
				}
				break;
			}
			resetAuroraHotseatData();
			mLauncher.setNeedAutoManagePagesToExitEditMode(false);
			mLauncher.exitEditMode(true);
			mLauncher.autoManagePagesImediatly();
			mLauncher.setNeedAutoManagePagesToExitEditMode(true);
			if (msg.arg1 == DeleteDropTarget.DISMISS_CONFIRM) {
				Workspace workspace = mLauncher.getWorkspace();
				int scrolToPage=workspace.getChildCount()-1;
				int currentPage=workspace.getCurrentPage();
				int duration=Workspace.PAGE_SNAP_ANIMATION_DURATION+(scrolToPage-currentPage)*120;
				workspace.snapToPage(scrolToPage,duration>2000?2000:duration);
				playDismissFolderAnimation(duration);
				resetDragInfo();
			}
			before_edit_pagesCount = getChildCount();
			mDropTargetTag = false;
			mDragOutline = null;
	        mDragInfo = null;
		}
    };
    
    private void resetAuroraFolder(CellLayout parent, View cell) {
		ItemInfo info = (ItemInfo) cell.getTag();
		mDropId = info.id;
		mDropContainer = LauncherSettings.Favorites.CONTAINER_DESKTOP;
    	if(mAuroraSwapTag) {
    		CellLayout hotseat = mLauncher.getHotseat().getLayout();
    		hotseat.setAuroraCellX(true);
    		updateAuroraHotseatParams(info, parent, hotseat, mDragItemInfo[0]);
    		((FolderIcon) cell).setTextVisible(true);
    		updateHotseatData();
    	} else {
    		info.screen = mDragItemInfo[0];
    		info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
    		info.cellX = mDragItemInfo[1];
    		info.cellY = mDragItemInfo[2];
    		cell.setTag(info);
			CellLayout.LayoutParams clp = (CellLayout.LayoutParams) cell.getLayoutParams();
			clp.cellX = clp.tmpCellX = mDragItemInfo[1];
			clp.cellY = clp.tmpCellY = mDragItemInfo[2];
			clp.cellHSpan = info.spanX;
			clp.cellVSpan = info.spanY;
			clp.useTmpCoords = false;
			clp.isLockedToGrid = true;
            cell.setId(LauncherModel.getCellLayoutChildId(info.container, info.screen,
            		info.cellX, info.cellY, info.spanX, info.spanY));
			addInScreen(cell, info.container, info.screen, info.cellX, info.cellY, 1, 1);
			LauncherModel.moveItemInDatabase(mLauncher, info, info.container, info.screen, info.cellX,
					info.cellY);
    	}
    }
    
    private void updateAuroraHotseatData(){
		if (mDragInfo != null) {
			ItemInfo info = (ItemInfo) mDragInfo.cell.getTag();
			mDropId = info.id;
			mDropCellX = mDragItemInfo[1];
			mDropContainer = Favorites.CONTAINER_HOTSEAT;
			updateHotseatData();
		}
    }

	public Handler getmDismissFolderHandler() {
		return mDismissFolderHandler;
	}
	
	private Animator singleItemAnimator(final View target, long duration,
			Interpolator ln) {
		ValueAnimator va = LauncherAnimUtils.ofFloat(0.5f, 1.0f);
		va.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				float progress = (Float) animation.getAnimatedValue();
				target.setScaleX(progress);
				target.setScaleY(progress);
			}
		});
		va.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				target.setScaleX(0.5f);
				target.setScaleY(0.5f);
				target.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {}
		});
		va.setDuration(duration);
		va.setInterpolator(ln);
		return va;
	}

	ArrayList<Animator> mAuroraFolderItems = new ArrayList<Animator>();
	Interpolator mAuroraInterpolator = new OvershootInterpolator();

	public void addAuroraFolderItems(View target) {
		mAuroraFolderItems.add(singleItemAnimator(target, 200,
				mAuroraInterpolator));
	}
	
	public void cleanFolderItems() {
		mAuroraFolderItems.clear();
	}
	
	private ArrayList<Animator> mAuroraPreScreenItems = new ArrayList<Animator>();
	private ArrayList<Animator> mAuroraLastScreenItems = new ArrayList<Animator>();
	public void filterValues(int emptyCount) {
		int count = 0;
		int totalSize = mAuroraFolderItems.size();
		Log.i(TAG, "total = " + totalSize +", emptyCount = " + emptyCount);
		mAuroraPreScreenItems.clear();
		mAuroraLastScreenItems.clear();
		if (mAuroraFolderItems.size() > emptyCount) {
			count = totalSize - emptyCount;
		} else {
			for (int i = 0; i < totalSize; i++) {
				mAuroraPreScreenItems.add(mAuroraFolderItems.get(i));
			}
			return;
		}
		final int S = LauncherModel.getCellCountX() * LauncherModel.getCellCountY();
		int left = count % S;
		int mode = count / S;
		Log.i(TAG, " S = " + S +", [ " + left +", " + mode+" ]");
		int start = -1;
		if (mode > 0) {
			start = (mode - 1) * S + emptyCount;
		} else {
			start = emptyCount;
		}
		for (int i = 0; i < start; i++) {
			mAuroraPreScreenItems.add(mAuroraFolderItems.get(i));
		}
		for (int i = start; i < totalSize; i++) {
			mAuroraLastScreenItems.add(mAuroraFolderItems.get(i));
		}
	}

	public void playDismissFolderAnimation(int duration) {
		AnimatorSet set = new AnimatorSet();
		Log.i(TAG, "playDismissFolderAnimation()--------" + mAuroraPreScreenItems.size());
		set.playTogether(mAuroraPreScreenItems);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				setLayerType(View.LAYER_TYPE_HARDWARE, null);
				buildLayer();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setLayerType(View.LAYER_TYPE_NONE, null);
					}
				});
				//iht 2014-12-10
				playDismissFolderLastScreenAnimation();
			}
		});
		set.setStartDelay(duration > 1000 ? 1000 : duration);
		set.start();
	}
	
	public void playDismissFolderLastScreenAnimation() {
		if (mAuroraLastScreenItems.size() == 0
				|| mAuroraLastScreenItems.isEmpty())
			return;
		Log.i(TAG, "playDismissFolderLastScreenAnimation()--------" + mAuroraLastScreenItems.size());
		AnimatorSet set = new AnimatorSet();
		set.playTogether(mAuroraLastScreenItems);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				setLayerType(View.LAYER_TYPE_HARDWARE, null);
				buildLayer();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				//setLayerType(View.LAYER_TYPE_NONE, null);
				//buildLayer();
				post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setLayerType(View.LAYER_TYPE_NONE, null);
					}
				});
			}
		});
		set.start();
	}
	
	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 begin
	public void updateAuroraMemoryParams() {
		mDropContainer = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
		CellLayout.LayoutParams cl;
		ItemInfo sInfo;
		CellLayout parentLayout = mLauncher.getHotseat().getLayout();
		Log.i("jialf", "mDragItemInfo[1] = " + mDragItemInfo[1]);
		parentLayout.removeViewAt(mDragItemInfo[1]);
		ShortcutAndWidgetContainer sac = parentLayout.getShortcutsAndWidgets();
		int count = sac.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = sac.getChildAt(i);
			sInfo = (ItemInfo) v.getTag();
			cl = (CellLayout.LayoutParams) v.getLayoutParams();
			if (cl.cellX > mDropCellX) {
				cl.cellX--;
				cl.useTmpCoords = false;
				sInfo.cellX--;
				sInfo.screen--;
				v.setLayoutParams(cl);
				v.setTag(sInfo);
			}
		}
		parentLayout.setAuroraCellX(false);
		parentLayout.setAuroraGridSize(-1);
	}
	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 end
	
	//AURORA-START::Fix Bug #2761::Shi guiqiang::20140305
	Runnable mCreatNewPageRunnable=new Runnable() {
		@Override
		public void run() {
			mLauncher.createNewPage();
		}
	};
	//AURORA-END::Fix Bug #2761::Shi guiqiang::20140305
	
	public void clearHidePage(){
		hidePageIndex = -1;
	}
	
	public int getHidePageIndex(){
		return hidePageIndex;
	}
	
	public void setHidePageIndex(int i){
		hidePageIndex = i ;
	}
	
	@Override
	protected boolean isEditmode() {
		return mState==State.WORKSPACE_EDIT;
	}
	
	private boolean isDragOverNavigateBar(DragObject d){
		float height = getResources().getDimension(R.dimen.display_height);
		float navigateBarHeight = getResources().getDimension(R.dimen.navigatebar_height);
		return d.y>(height-navigateBarHeight);
	}
	
	
    
    public class ItemPos {
    	public int screen;
    	public int x;
    	public int y;
    	
    	public ItemPos getNextPos() {
    		final int xCount = mCellCountX;
    		final int yCount = mCellCountY;
    		ItemPos newPos = new ItemPos();
    		
			newPos.screen = this.screen;
			newPos.x = this.x;
			newPos.y = this.y;

    		//exception
    		if(xCount <= 0 || yCount <= 0) {
    			return newPos;
    		}

    		newPos.x = newPos.x + 1;
    		if(newPos.x >= xCount) {
    			newPos.y = newPos.y + newPos.x / xCount;
    			newPos.x = newPos.x % xCount;
    			if(newPos.y >= yCount) {
    				newPos.screen = screen + newPos.y/yCount;
    				newPos.y = newPos.y % yCount;
    			}
    		}
    		return newPos;
    	}
    	
    	public ItemPos set(ItemPos itemPos) {
    		screen = itemPos.screen;
    		x = itemPos.x;
    		y = itemPos.y;
    		return this;
    	}
    	
    	public boolean equals(ItemPos itemPos) {
    		if(itemPos.screen != screen) {
    			return false;
    		}
    		
    		if(itemPos.y != y) {
    			return false;
    		}
    		
    		if(itemPos.x != x) {
    			return false;
    		}
    		return true;
    	}
    	
    	public boolean equals(int screen,int x,int y) {
    		if(this.screen != screen) {
    			return false;
    		}
    		
    		if(this.y != y) {
    			return false;
    		}
    		
    		if(this.x != x) {
    			return false;
    		}
    		return true;
    	}

    }
	
	/**
	 * check if there is a child at specified place in workspace
	 * @param screen
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean hasChildAtWorkspace(int screen,int x, int y) {
		CellLayout cellLayout = null;
		View vIcon = null;//vIcon = page.getChildAt(x, y);
		
		//if(mWorkspace == null) {
		//	return false;
		//}
		
		cellLayout = (CellLayout) this.getChildAt(screen);
		if(cellLayout == null) {
			return false;
		}
		
		vIcon = cellLayout.getChildAt(x, y);
		if(vIcon == null) {
			return false;
		}
		Log.d("vulcan-repeat",String.format("hasChildAtWorkspace: screen=%d,x=%d,y=%d,v = %s",screen,x,y,vIcon.toString()));
		return true;
	}
	
	/**
	 * check if there is a dragged item at specified place
	 * @param screen
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean hasDragItemAt(int screen,int x,int y) {
		ItemPos itemPos = null;
		
		itemPos = getWorkspaceDragItemPos();
		
		if(itemPos == null) {
			return false;
		}
		
		if(!itemPos.equals(screen, x, y)) {
			return false;
		}
		return true;
	}
	
	/**
	 * check if there is any child at specified place when some item in workspace is dragged.
	 * @param x x coordinate.
	 * @param y y coordinate
	 * @return return true if there is a child at specified place
	 */
	private boolean hasChildAtDragWorkspace(int screen,int x,int y) {
		
		if(hasChildAtWorkspace(screen,x,y)) {
			Log.d("vulcan-repeat",String.format("getWorkspaceDragItemPos: hasChildAtWorkspace"));
			return true;
		}
		
		if(hasDragItemAt(screen,x,y)) {
			Log.d("vulcan-repeat",String.format("getWorkspaceDragItemPos: hasDragItemAt"));
			return true;
		}	
		return false;
	}
	
	/**
	 * find the first vacancy according our standard in a page
	 * @param pageNumber the page to look for
	 * @param itemPos postion of icon
	 * @return return true if it finds out a postion
	 */
	private ItemPos findFirstVacancyOfPage(int pageNumber) {
		final int row_max = mCellCountY - 1;
		final int col_max = mCellCountX - 1;
		int row = 0;
		int col = 0;
		int lastIconPos = 0;
		boolean bHasChild = false;
		ItemPos itemPos = new ItemPos();
		
		//test if we are likely to find one vacancy
		//vIcon = page.getChildAt(row_max - 1, line_max - 1);
		bHasChild = hasChildAtDragWorkspace(pageNumber,col_max, row_max);
		if(bHasChild) {//it indicates that we can't find one vacancy
			Log.d("vulcan-repeat",String.format("findFirstVacancyOfPage: has child at bottom-right,pageNumber = %d,row_max = %d, col_max = %d",pageNumber,row_max,col_max));
			return null;
		}

		//look for the most bottom-right icon
		for(row = row_max;row >= 0;row --) {
			for(col = col_max;col >= 0;col --) {
				//vIcon = page.getChildAt(row, line);
				bHasChild = hasChildAtDragWorkspace(pageNumber,col,row);
				if(bHasChild) {
					Log.d("vulcan-repeat",String.format("findFirstVacancyOfPage: child is found, screen = %d, row = %d, col = %d",pageNumber,row,col));
					break;
				}
			}
			if(bHasChild) {
				break;
			}
		}

		if(!bHasChild) {//it indicates that this page is empty
			itemPos.screen = pageNumber;
			itemPos.x = 0;
			itemPos.y = 0;
			Log.d("vulcan-repeat",String.format("findFirstVacancyOfPage: page is null, pageNumber = %d",pageNumber));
			return itemPos;
		}

		
		itemPos.screen = pageNumber;
		itemPos.x = col;
		itemPos.y = row;
		itemPos.set(itemPos.getNextPos());
		Log.d("vulcan-repeat",String.format("findFirstVacancyOfPage: vacancy position is found, screen = %d, row = %d, col = %d",itemPos.screen,itemPos.y,itemPos.x));
		return itemPos;
	}
	
	/**
	 * compare the position of two icons.
	 * @param pos1 an array contains screen, x, y
	 * @param pos2 an array contains screen, x, y
	 * @return return 0 if pos1 is equal to pos2, return -1 if pos 1 is on the left of pos 2, return 1 if 
	 * pos 1 is on the right of pos 2. NOTICE: the pos1 & pos2 must be not null and with 3 elements
	 */
	private int iconPostionCompare(ItemPos pos1, ItemPos pos2) {
		
		if(pos1.screen < pos2.screen) {//screen number is less
			return -1;
		}
		else if(pos1.screen == pos2.screen
				&& pos1.y < pos2.y) {//screen number is equal and y is less
			return -1;
		}
		else if(pos1.screen == pos2.screen
				&& pos1.y == pos2.y
				&& pos1.x < pos2.x) {//screen number is equal and y is equal and x is less
			return -1;
		}
		else if(pos1.screen == pos2.screen
				&& pos1.y == pos2.y
				&& pos1.x == pos2.x) {//screen number,y,x are all equal
			return 0;
		}
		return 1;
	}
	
	/**
	 * query position of the dragged item in the workspace
	 * @return return null if there is no dragged item.
	 */
	private ItemPos getWorkspaceDragItemPos() {
		int [] dragItemInfo = null;//mWorkspace.getDragItemInfo();
		Launcher l = null;
		
		//l = (Launcher)getCallback();
		l = this.mLauncher;
		if(l == null) {
			return null;
		}
		
		if(!l.isDragMode()) {
			return null;
		}
		
		//if(mWorkspace == null) {
		//	return null;
		//}
		
		dragItemInfo = this.getDragItemInfo();
		
		if(dragItemInfo == null) {
			return null;
		}
		
		if(dragItemInfo.length != 3) {
			return null;
		}
		if(dragItemInfo[1] == -1 || dragItemInfo[2] == -1) {
			return null;
		}
		
		ItemPos itemPos = new ItemPos();
		itemPos.screen = dragItemInfo[0];
		itemPos.x = dragItemInfo[1];
		itemPos.y = dragItemInfo[2];
		
		Log.d("vulcan-repeat",String.format("getWorkspaceDragItemPos: isEditMode = %b,screen = %d,x= %d,y= %d",l.isEditMode(),itemPos.screen,itemPos.x,itemPos.y));
		return itemPos;
	}

	/**
	 * look for the first vacancy to install an application
	 * @return, return the position if it succeeds in getting the first vacancy
	 */
	public ItemPos findFirstVacancyOfWorkspace(){
		
		if(Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException();
		}

		//boolean uninstalling = false;
		//NOTICE: the caller must give valid parameter screen,x,y.
		
		//if(mWorkspace == null) {
		//	return null;
		//}
		
		//step 1: get valid reference to launcher.
		//Launcher launcher = (Launcher)getCallback();
		Launcher launcher = this.mLauncher;
		if(launcher == null) {
			return null;
		}

		//step 2: read flag uninstalling
		//uninstalling = launcher.isUninstalling();

		//step 3:
		//steps to find the most bottom-right vacancy
		//step 3.1:first stop is to look for the most bottom-right icon.
		CellLayout cellLayout = null;
		int cellLayoutChildNum = 0;
		boolean pageFound = false;
		int lastPageNumber = 0;
		final int pageTotal = getChildCount();//mWorkspace.getChildCount();
		ItemPos itemPos = null;
		for(int ii = pageTotal - 1;ii >= 0 ; ii --) {
			cellLayout = (CellLayout) getChildAt(ii);//(CellLayout) mWorkspace.getChildAt(ii);
			cellLayoutChildNum = cellLayout.getShortcutsAndWidgetsChildCount();
			if(cellLayoutChildNum > 0) {
				pageFound = true;
				lastPageNumber = ii;
				break;
			}
			itemPos = getWorkspaceDragItemPos();
			if (itemPos != null) {
				Log.d("vulcan-repeat", String.format(
						"findFirstVacancyOfWorkspace: exists dragged item: dragItemPos = %s",
						itemPos.toString()));
				if(itemPos.screen == ii) {
					pageFound = true;
					lastPageNumber = ii;
					break;
				}
			}
		}
		Log.d("vulcan-repeat",String.format("findFirstVacancyOfWorkspace: pageFound = %b, lastPageNumber=%d",pageFound,lastPageNumber));
		if(!pageFound) {//it indicates that there isn't any icon in workspace
			itemPos = new ItemPos();
			itemPos.screen = 0;
			itemPos.x = 0;
			itemPos.y = 0;
			return itemPos;
		}
		
		//step 3.2: look for the first vacancy in this page
		itemPos = findFirstVacancyOfPage(lastPageNumber);
		if(itemPos == null) {//this page is full, so place it in the new page
			itemPos = new ItemPos();
			itemPos.screen = lastPageNumber + 1;
			itemPos.x = 0;
			itemPos.y = 0;
			Log.d("vulcan-repeat",String.format("findFirstVacancyOfWorkspace: findFirstVacancyOfPage: item= (%d,%d,%d)",
					itemPos.screen,itemPos.x,itemPos.y));
			return itemPos;
		}

		return itemPos;
	}
	
	/**set up current page's alpha when classify action happens*/
	public void setCurrentPagesAlpha(float alpha){
		View curPageView = getChildAt(getCurrentPage());
		curPageView.setAlpha(alpha);
	}

	public List<ShortcutInfo> getRemoveViewList() {
		return removeViewList;
	}

	public void setRemoveViewList(List<ShortcutInfo> removeViewList) {
		this.removeViewList = removeViewList;
	}
}
