/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.List;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import aurora.app.AuroraAlertDialog;

import com.aurora.launcher.R;

public class DeleteDropTarget extends ButtonDropTarget {
    private static int DELETE_ANIMATION_DURATION = 250;
    private static int FLING_DELETE_ANIMATION_DURATION = 350;
    private static float FLING_TO_DELETE_FRICTION = 0.035f;
    private static int MODE_FLING_DELETE_TO_TRASH = 0;
    private static int MODE_FLING_DELETE_ALONG_VECTOR = 1;

    private final int mFlingDeleteMode = MODE_FLING_DELETE_ALONG_VECTOR;

    private ColorStateList mOriginalTextColor;
    private TransitionDrawable mUninstallDrawable;
    private TransitionDrawable mRemoveDrawable;
    private Drawable mCurrentDrawable;
    
    private Drawable mNormalDrawable;
    private Drawable mEnterDrawable;
    private Drawable mFolderEnterDrawable;
    
    private Context mContext;
    
    private Drawable mDragEnter;
    private Drawable mDragExit;
    AnimationDrawable mAnimationDrawable;

    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the hover color
        /*Resources r = getResources();
        mHoverColor = r.getColor(R.color.delete_target_hover_tint);
        mUninstallDrawable = (TransitionDrawable) 
                r.getDrawable(R.drawable.uninstall_target_selector);
        mRemoveDrawable = (TransitionDrawable) r.getDrawable(R.drawable.remove_target_selector);

        mRemoveDrawable.setCrossFadeEnabled(true);
        mUninstallDrawable.setCrossFadeEnabled(true);
        
        mNormalDrawable = r.getDrawable(R.drawable.ic_launcher_clear_normal_holo);
        mEnterDrawable = r.getDrawable(R.drawable.ic_launcher_clear_focus_holo);
        mFolderEnterDrawable = r.getDrawable(R.drawable.ic_launcher_clear_folder_focus_holo);
        
        mDragEnter = r.getDrawable(R.drawable.aurora_delete_zone_animation_enter);
        mDragExit = r.getDrawable(R.drawable.aurora_delete_zone_animation_exit);*/
        
        // The current drawable is set to either the remove drawable or the uninstall drawable 
        // and is initially set to the remove drawable, as set in the layout xml.
    }
    
	public void initButtonDropTarget(boolean isSystemApp) {
		
		if(mCleaRunnable!=null)removeCallbacks(mCleaRunnable);
		Resources r = getResources();
		if(mHoverColor==0)mHoverColor = r.getColor(R.color.delete_target_hover_tint);
		if(mUninstallDrawable==null)mUninstallDrawable = (TransitionDrawable)r.getDrawable(R.drawable.uninstall_target_selector);
		if(mRemoveDrawable==null)mRemoveDrawable = (TransitionDrawable)r.getDrawable(R.drawable.remove_target_selector);
		mRemoveDrawable.setCrossFadeEnabled(true);
		mUninstallDrawable.setCrossFadeEnabled(true);
		
		if(!isSystemApp){
			if(mNormalDrawable==null)mNormalDrawable = r.getDrawable(R.drawable.ic_launcher_clear_normal_holo);
			if(mEnterDrawable==null)mEnterDrawable = r.getDrawable(R.drawable.ic_launcher_clear_focus_holo);
			if(mFolderEnterDrawable==null)mFolderEnterDrawable = r.getDrawable(R.drawable.ic_launcher_clear_folder_focus_holo);
			if(mDragEnter==null)mDragEnter = r.getDrawable(R.drawable.aurora_delete_zone_animation_enter);
			if(mDragExit==null)mDragExit = r.getDrawable(R.drawable.aurora_delete_zone_animation_exit);
		}
		if(mCurrentDrawable==null)mCurrentDrawable = getCurrentDrawable();
	}

	public void clearButtonDropTarget() {
		//this.postDelayed(mCleaRunnable, 1000);
		this.postDelayed(mCleaRunnable, 150);
	}

	Runnable mCleaRunnable = new Runnable() {
		@Override
		public void run() {
			mHoverColor = 0;
			mUninstallDrawable = null;
			mRemoveDrawable = null;
			mNormalDrawable = null;
			mEnterDrawable = null;
			mFolderEnterDrawable = null;
			mDragEnter = null;
			mDragExit = null;
			mCurrentDrawable = null;
			setFolderHoverColor();
			
			Log.v("iht-sy", "******：");
			
			setHoverColor();
		}
	};
    

    private boolean isAllAppsApplication(DragSource source, Object info) {
        return (source instanceof AppsCustomizePagedView) && (info instanceof ApplicationInfo);
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        if (source instanceof AppsCustomizePagedView) {
            if (info instanceof PendingAddItemInfo) {
                PendingAddItemInfo addInfo = (PendingAddItemInfo) info;
                switch (addInfo.itemType) {
                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                        return true;
                }
            }
        }
        return false;
    }
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof ShortcutInfo);
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }
    private boolean isWorkspaceFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) && (d.dragInfo instanceof FolderInfo);
    }

    private void setHoverColor() {
    	resetHoverColor();
    	if(mAnimationDrawable!=null){
    		mAnimationDrawable.start();
    	}
    }
    
    private void setFolderHoverColor() {
        setBackground(mFolderEnterDrawable);
    }
    
    private void resetHoverColor() {
        // setBackground(mNormalDrawable);
		if (mAnimationDrawable != null && mAnimationDrawable.isRunning())
			mAnimationDrawable.stop();
		Resources r = getResources();
    	setBackground(mDragEnter);
    	mAnimationDrawable = (AnimationDrawable) getBackground();
    }
    
    private void resetAuroraHoverColor() {
    	if (mAnimationDrawable != null && mAnimationDrawable.isRunning())
			mAnimationDrawable.stop();
    	Resources r = getResources();
    	setBackground(mDragExit);
    	mAnimationDrawable = (AnimationDrawable) getBackground();
    	if(mAnimationDrawable!=null){
    		mAnimationDrawable.start();
    	}
    }
    
    private void startAnimation(){
    	if(mAnimationDrawableRunnable!=null){
    		this.removeCallbacks(mAnimationDrawableRunnable);
    	}
    	this.postDelayed(mAnimationDrawableRunnable, 20);
    }
    
    Runnable mAnimationDrawableRunnable = new Runnable() {
		@Override
		public void run() {
			if (mAnimationDrawable != null && mAnimationDrawable.isRunning())
				mAnimationDrawable.stop();
			if(mAnimationDrawable!=null){
	    		mAnimationDrawable.start();
	    	}	
		}
	};
    
    @Override
    public boolean acceptDrop(DragObject d) {
        // We can remove everything including App shortcuts, folders, widgets, etc.
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
    	boolean pendingpAddItemInfo = info instanceof PendingAddItemInfo;
    	if(pendingpAddItemInfo)
    		return;
        boolean isVisible = true;
        boolean isUninstall = false;
        
        //AURORA_START: iht 2014-12-11 fix BUG#10329
        boolean isSystemApp = false;
        ItemInfo item = (ItemInfo)info;
        if(item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ){
        	ShortcutInfo sinfo = (ShortcutInfo)item;
        	if(sinfo.flags == 0){
        		isSystemApp = true;
        	}
        }
        //initButtonDropTarget();
        initButtonDropTarget(isSystemApp);
        //AURORA_END
        
		// Aurora <jialf> <2013-10-02> modify for dismiss folder begin
		if (info instanceof FolderInfo) {
	        setText(R.string.folder_drop_str);
	        //setFolderHoverColor();
	        setBackground(null);
		} else {
			setText(null);
			setBackground(mNormalDrawable);
			// resetHoverColor();
		}
		mActive = isVisible;
		((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
		// Aurora <jialf> <2013-10-02> modify for dismiss folder end
        /*if (getText().length() > 0) {
            setText(isUninstall ? R.string.delete_target_uninstall_label
                : R.string.delete_target_label);
        }*/
    	// Aurora <jialf> <2013-09-13> modify for Drop target end
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
        clearButtonDropTarget();
    }

    public void onDragEnter(DragObject d) {
    	if(d.dragInfo instanceof FolderInfo) { 
    		setFolderHoverColor();
    		return;
    	}
    	
    	//iht 2014-12-11
    	//若是系统应用，则无须出现垃圾桶
    	if(d.dragInfo instanceof ShortcutInfo){
    		ShortcutInfo info = (ShortcutInfo)d.dragInfo;
    		if(info.flags == 0){
    			return;
    		}
    	}
        super.onDragEnter(d);
        setHoverColor();
    }

    public void onDragExit(DragObject d) {
        super.onDragExit(d);

        if (!d.dragComplete) {
			if (d.dragInfo instanceof FolderInfo) {
				setBackground(null);
			} else { 
				//resetHoverColor();
				//resetAuroraHoverColor();
				//postDelayed(mRunnable, 100);
				
		    	//iht 2014-12-11
		    	//若是系统应用，则无须出现垃圾桶的动画
				if(d.dragInfo instanceof ShortcutInfo){
					ShortcutInfo info = (ShortcutInfo)d.dragInfo;
					if(info.flags == 0){
						postDelayed(mRunnable, 100);
						return;
					}
				}
				resetAuroraHoverColor();
				postDelayed(mRunnable, 100);
			}
        } else {
            // Restore the hover color if we are deleting
            // d.dragView.setColor(mHoverColor);
        }
    }
    
    private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mAnimationDrawable != null && mAnimationDrawable.isRunning())
				mAnimationDrawable.stop();
			setBackground(mNormalDrawable);
		}
	};

    private void animateToTrashAndCompleteDrop(final DragObject d) {
        DragLayer dragLayer = mLauncher.getDragLayer();
        Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
        float scale = (float) to.width() / from.width();

        mSearchDropTargetBar.deferOnDragEnd();
        
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mSearchDropTargetBar.onDragEnd();
                mLauncher.exitSpringLoadedDragMode();
                completeDrop(d);
                // Aurora <haojj> <2013-11-13> add for 如果是拖动到删除区域并且不是shortcut，为了避免动画完成前就出现view，我们在此调用onDropComplete begin
            	if(d.dragSource instanceof Folder) {
            		final ItemInfo item = (ItemInfo) d.dragInfo;
            		if(item.itemType != LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
            			postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
				        		Folder folder = (Folder) d.dragSource;
								d.dragSource.onDropCompleted(DeleteDropTarget.this, d, false, folder.appNotExists(item));
							}
						}, 10);
            		}
            	}
            	// Aurora <haojj> <2013-11-13> end
            }
        };
        
		dragLayer.animateAuroraView(d.dragView, from, to,
				DELETE_ANIMATION_DURATION, onAnimationEndRunnable,
				DragLayer.ANIMATION_END_DISAPPEAR, null);
    }

    private void completeDrop(DragObject d) {
    	// Aurora <jialf> <2013-09-13> modify for Drop target begin
        ItemInfo item = (ItemInfo) d.dragInfo;
        
        LauncherApplication.logVulcan.print("completeDrop:item = " + item + ",dragSource= " + d.dragSource + ",dragTarget" + d.dragView);

		if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
				&& item instanceof ShortcutInfo) {
	        // Aurora <jialf> <2013-09-19> add for unstall apps begin
			ShortcutInfo info = (ShortcutInfo) item;
			ComponentName cn = info.intent.getComponent();

			

			List<ResolveInfo> list = AllAppsList.findActivitiesForPackage(mContext,cn.getPackageName());
			if (list == null || list.isEmpty()) {
				if (d.dragSource instanceof Workspace) {
					Workspace space = mLauncher.getWorkspace();
					if (mFromHotseat) {
			        	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 begin
						if(mSdcardAppHasParent) {
							space.updateAuroraMemoryParams();
							mSdcardAppHasParent = false;
						}
						space.updateHotseatData();
						space.resetAuroraHotseatData();
			        	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 end
						mFromHotseat = false;
					} else {
						int[] dragInfo = space.getDragItemInfo();
						CellLayout layout = (CellLayout) space.getChildAt(dragInfo[0]);
						View view = layout.getShortcutsAndWidgets().getChildAt(
								dragInfo[1], dragInfo[2]);
						layout.removeView(view);
						LauncherApplication.logVulcan.print("completeDrop:layout.removeView: " + view);
					}
				}
				LauncherModel.deleteItemFromDatabase(mContext, item);
			} else {
				LauncherApplication.logVulcan.print("completeDrop:findActivitiesForPackage not null list: " + list);
				//iht 2014-12-12 fix BUG#10329
				//系统应用无法删除，同时也不做相关提示，故增加系统应用判断；
				if(info != null && info.flags != 0){
					if (d.dragSource instanceof Workspace) {
						Workspace wp = mLauncher.getWorkspace();
						if (wp != null) {
							wp.setmNeedDelay(true);
							Message msg = new Message();
							msg.what = Workspace.DELAY_FLAG;
							msg.arg1 = info.flags;
							msg.obj = cn;
							wp.getmUninstallHandler().sendMessage(msg);
							LauncherApplication.logVulcan.print("completeDrop:sent message:" + msg);
						}
					} else {
						LauncherApplication.logVulcan.print("completeDrop:to call startAuroraApplicationUninstallActivity:");
						mLauncher.startAuroraApplicationUninstallActivity(cn, info.flags); //卸载应用
					}
				}
			}
	        // Aurora <jialf> <2013-09-19> add for unstall apps end
		} else if (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
    		// disband folder
			LauncherModel.deleteItemFromDatabase(mLauncher, item);
    	} else if(item instanceof FolderInfo) {
    		LauncherApplication.logVulcan.print("completeDrop: to call confirmDismissFolder!");
    		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
    		Dialog dialog = confirmDismissFolder(item);
    		mLauncher.setDismissFolderDialog(dialog);
    		//add by xiexiujie 11.4 start
    		mLauncher.exitEditMode(true, mLauncher.getWorkspace().ismAuroraSwapTag(), false);	
    		//add by xiexiujie 11.4 end
    		dialog.show();
    	} else if (isWorkspaceOrFolderWidget(d)) {
            // Remove the widget from the workspace
            mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
            
            CellLayout cellLayout = (CellLayout)mLauncher.getWorkspace().getChildAt(item.screen);
            ShortcutAndWidgetContainer sac = cellLayout.getShortcutsAndWidgets();
            cellLayout.removeView(sac.getChildAt(item.cellX, item.cellY));
            if(mLauncher.getEditMode()!=Launcher.EditMode.APPWIDGET_ADD){
	            mLauncher.setNeedAutoManagePagesToExitEditMode(true);
	            mLauncher.autoManagePagesImediatly();
            }
            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
        }
    	// Aurora <jialf> <2013-09-13> modify for Drop target end
    }

    public void onDrop(DragObject d) {
    	// Aurora <haojj> <2013-10-15> add for TAG begin
    	if(!(d.dragSource instanceof Folder)){
    		mLauncher.closeFolder();
    	}
    	// Aurora <haojj> <2013-10-15> end
    	ItemInfo item = (ItemInfo) d.dragInfo;
    	if(isWorkspaceOrFolderWidget(d)) {
			if (mLauncher.getEditMode() != Launcher.EditMode.APPWIDGET_ADD) {
				mLauncher.setNeedAutoManagePagesToExitEditMode(false);
			}
    	}
    	if(item instanceof FolderInfo) {
    		mLauncher.getWorkspace().setmDropTargetTag(true);
    		mLauncher.onExitFullScreen();
    	} else {
    		mFromHotseat = mLauncher.getWorkspace().ismAuroraSwapTag();
    		// Aurora <jialf> <2014-01-06> modify for fix bug #1738 begin
    		mSdcardAppHasParent = false;
    		// Aurora <jialf> <2014-01-06> modify for fix bug #1738 end
    		if (mAnimationDrawable != null && mAnimationDrawable.isRunning())
    			mAnimationDrawable.stop();
    		setBackground(mNormalDrawable);
    	}
        animateToTrashAndCompleteDrop(d);
    }

    /**
     * Creates an animation from the current drag view to the delete trash icon.
     */
    private AnimatorUpdateListener createFlingToTrashAnimatorListener(final DragLayer dragLayer,
            DragObject d, PointF vel, ViewConfiguration config) {
        final Rect to = getIconRect(d.dragView.getMeasuredWidth(), d.dragView.getMeasuredHeight(),
                mCurrentDrawable.getIntrinsicWidth(), mCurrentDrawable.getIntrinsicHeight());
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        // Calculate how far along the velocity vector we should put the intermediate point on
        // the bezier curve
        float velocity = Math.abs(vel.length());
        float vp = Math.min(1f, velocity / (config.getScaledMaximumFlingVelocity() / 2f));
        int offsetY = (int) (-from.top * vp);
        int offsetX = (int) (offsetY / (vel.y / vel.x));
        final float y2 = from.top + offsetY;                        // intermediate t/l
        final float x2 = from.left + offsetX;
        final float x1 = from.left;                                 // drag view t/l
        final float y1 = from.top;
        final float x3 = to.left;                                   // delete target t/l
        final float y3 = to.top;

        final TimeInterpolator scaleAlphaInterpolator = new TimeInterpolator() {
            @Override
            public float getInterpolation(float t) {
                return t * t * t * t * t * t * t * t;
            }
        };
        return new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final DragView dragView = (DragView) dragLayer.getAnimatedView();
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                float tp = scaleAlphaInterpolator.getInterpolation(t);
                float initialScale = dragView.getInitialScale();
                float finalAlpha = 0.5f;
                float scale = dragView.getScaleX();
                float x1o = ((1f - scale) * dragView.getMeasuredWidth()) / 2f;
                float y1o = ((1f - scale) * dragView.getMeasuredHeight()) / 2f;
                float x = (1f - t) * (1f - t) * (x1 - x1o) + 2 * (1f - t) * t * (x2 - x1o) +
                        (t * t) * x3;
                float y = (1f - t) * (1f - t) * (y1 - y1o) + 2 * (1f - t) * t * (y2 - x1o) +
                        (t * t) * y3;

                dragView.setTranslationX(x);
                dragView.setTranslationY(y);
                dragView.setScaleX(initialScale * (1f - tp));
                dragView.setScaleY(initialScale * (1f - tp));
                dragView.setAlpha(finalAlpha + (1f - finalAlpha) * (1f - tp));
            }
        };
    }

    /**
     * Creates an animation from the current drag view along its current velocity vector.
     * For this animation, the alpha runs for a fixed duration and we update the position
     * progressively.
     */
    private static class FlingAlongVectorAnimatorUpdateListener implements AnimatorUpdateListener {
        private DragLayer mDragLayer;
        private PointF mVelocity;
        private Rect mFrom;
        private long mPrevTime;
        private boolean mHasOffsetForScale;
        private float mFriction;

        private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);

        public FlingAlongVectorAnimatorUpdateListener(DragLayer dragLayer, PointF vel, Rect from,
                long startTime, float friction) {
            mDragLayer = dragLayer;
            mVelocity = vel;
            mFrom = from;
            mPrevTime = startTime;
            mFriction = 1f - (dragLayer.getResources().getDisplayMetrics().density * friction);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final DragView dragView = (DragView) mDragLayer.getAnimatedView();
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            long curTime = AnimationUtils.currentAnimationTimeMillis();

            if (!mHasOffsetForScale) {
                mHasOffsetForScale = true;
                float scale = dragView.getScaleX();
                float xOffset = ((scale - 1f) * dragView.getMeasuredWidth()) / 2f;
                float yOffset = ((scale - 1f) * dragView.getMeasuredHeight()) / 2f;

                mFrom.left += xOffset;
                mFrom.top += yOffset;
            }

            mFrom.left += (mVelocity.x * (curTime - mPrevTime) / 1000f);
            mFrom.top += (mVelocity.y * (curTime - mPrevTime) / 1000f);

            dragView.setTranslationX(mFrom.left);
            dragView.setTranslationY(mFrom.top);
            dragView.setAlpha(1f - mAlphaInterpolator.getInterpolation(t));

            mVelocity.x *= mFriction;
            mVelocity.y *= mFriction;
            mPrevTime = curTime;
        }
    };
    private AnimatorUpdateListener createFlingAlongVectorAnimatorListener(final DragLayer dragLayer,
            DragObject d, PointF vel, final long startTime, final int duration,
            ViewConfiguration config) {
        final Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);

        return new FlingAlongVectorAnimatorUpdateListener(dragLayer, vel, from, startTime,
                FLING_TO_DELETE_FRICTION);
    }

    public void onFlingToDelete(final DragObject d, int x, int y, PointF vel) {
        final boolean isAllApps = d.dragSource instanceof AppsCustomizePagedView;
        final boolean isAuroraWidget = d.dragInfo instanceof PendingAddItemInfo;

        // Don't highlight the icon as it's animating
        d.dragView.setColor(0);
        d.dragView.updateInitialScaleToCurrentScale();
        // Don't highlight the target if we are flinging from AllApps
        if (isAllApps && !isAuroraWidget) {
            resetHoverColor();
        }
        
        if(d.dragInfo instanceof FolderInfo) {
    		mLauncher.getWorkspace().setmDropTargetTag(true);
    		mLauncher.onExitFullScreen();
        } else {
        	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 begin
        	Workspace space = mLauncher.getWorkspace();
    		mFromHotseat = space.ismAuroraSwapTag();
    		if(mFromHotseat) {
    			final View view = space.getDragInfo().cell;
    			mSdcardAppHasParent = space.getParentCellLayoutForView(view) != null;
    		}
        	// Aurora <jialf> <2014-01-06> modify for fix bug #1738 end
        	setHoverColor();
        	postDelayed(mRunnable, 100);
        }

        if (mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH) {
            // Defer animating out the drop target if we are animating to it
            mSearchDropTargetBar.deferOnDragEnd();
            mSearchDropTargetBar.finishAnimations();
        }

        final ViewConfiguration config = ViewConfiguration.get(mLauncher);
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final int duration = FLING_DELETE_ANIMATION_DURATION;
        final long startTime = AnimationUtils.currentAnimationTimeMillis();

        // NOTE: Because it takes time for the first frame of animation to actually be
        // called and we expect the animation to be a continuation of the fling, we have
        // to account for the time that has elapsed since the fling finished.  And since
        // we don't have a startDelay, we will always get call to update when we call
        // start() (which we want to ignore).
        final TimeInterpolator tInterpolator = new TimeInterpolator() {
            private int mCount = -1;
            private float mOffset = 0f;

            @Override
            public float getInterpolation(float t) {
                if (mCount < 0) {
                    mCount++;
                } else if (mCount == 0) {
                    mOffset = Math.min(0.5f, (float) (AnimationUtils.currentAnimationTimeMillis() -
                            startTime) / duration);
                    mCount++;
                }
                return Math.min(1f, mOffset + t);
            }
        };
        AnimatorUpdateListener updateCb = null;
        if (mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH) {
            updateCb = createFlingToTrashAnimatorListener(dragLayer, d, vel, config);
        } else if (mFlingDeleteMode == MODE_FLING_DELETE_ALONG_VECTOR) {
            updateCb = createFlingAlongVectorAnimatorListener(dragLayer, d, vel, startTime,
                    duration, config);
        }
		if (!isAuroraWidget)
			mSearchDropTargetBar.deferOnDragEnd();
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mSearchDropTargetBar.onDragEnd();

                // If we are dragging from AllApps, then we allow AppsCustomizePagedView to clean up
                // itself, otherwise, complete the drop to initiate the deletion process
                if (!isAllApps) {
                    mLauncher.exitSpringLoadedDragMode();
                    completeDrop(d);
                }
                mLauncher.getDragController().onDeferredEndFling(d);
            }
        };
        if (isAuroraWidget) { 
        	dragLayer.clearAuroraAnimatedView(d.dragView);
//AURORA-START::Delete the preview of widget after fling is end::Shi guiqiang::20131122
        	mSearchDropTargetBar.onDragEnd();
        	mLauncher.getDragController().onDeferredEndFling(d);
//AURORA-END::Delete the preview of widget after fling is end::Shi guiqiang::20131122
        } else {
	        dragLayer.animateAuroraFlingView(d.dragView, updateCb, duration, tInterpolator, onAnimationEndRunnable,
	                DragLayer.ANIMATION_END_DISAPPEAR, null);
        }
    }

    private Runnable mSnapDelayRunnable = new Runnable() {
		public void run() {
			Workspace workspace = mLauncher.getWorkspace();
			Log.i("xiejun6","workspace.getChildCount()-1  =  "+(workspace.getChildCount()-1));
			int scrolToPage=workspace.getChildCount()-1;
			int currentPage=workspace.getCurrentPage();
			workspace.snapToPage(scrolToPage,workspace.PAGE_SNAP_ANIMATION_DURATION+(scrolToPage-currentPage)*50);
			
		}
	};
	
	private void dismissFolder(ItemInfo item) {
		FolderInfo fi = (FolderInfo) item;
		mLauncher.dissolveAuroraFolder(fi);
	}
	
	private boolean mSendMsg = false;
	private Dialog confirmDismissFolder(final ItemInfo item) {
		final Workspace workspace = mLauncher.getWorkspace();
		final Handler handler = workspace.getmDismissFolderHandler();
		final Message msg = new Message();
		final boolean hotseat = workspace.ismAuroraSwapTag();
		Dialog dialog = new AuroraAlertDialog.Builder(mContext, AlertDialog.THEME_TRADITIONAL)
				.setTitle(R.string.dismiss_folder_title)
				.setMessage(R.string.dismiss_folder_msg)
				.setNegativeButton(R.string.cancel_action,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								/*msg.what = DISMISS_FOLDER_CANCEL_FLAG;
								handler.sendMessage(msg);*/
							}
						})
				.setPositiveButton(R.string.dismiss_folder_confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dismissFolder(item);
								mSendMsg = true;
								if (hotseat)
									msg.what = DISMISS_FOLDER_CONFIRM_FLAG;
								msg.obj = hotseat;
								msg.arg1 = DISMISS_CONFIRM;
								handler.sendMessage(msg);
							}
						})
				.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (mSendMsg) {
							mSendMsg = false;
						} else {
							msg.what = DISMISS_FOLDER_CANCEL_FLAG;
							msg.obj = hotseat;
							handler.sendMessage(msg);
						}
					}
				}).create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}
	
	public static final int DISMISS_FOLDER_CONFIRM_FLAG = 1;
	public static final int DISMISS_FOLDER_CANCEL_FLAG = 2;
	public static final int DISMISS_CONFIRM = 99;
	
    private boolean mFromHotseat;
    private boolean mSdcardAppHasParent;
}
