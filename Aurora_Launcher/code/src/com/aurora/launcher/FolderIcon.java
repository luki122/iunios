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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.aurora.launcher.R;
import com.aurora.launcher.DropTarget.DragObject;
import com.aurora.launcher.FolderInfo.FolderListener;
import com.aurora.launcher.Launcher.EditMode;
import com.aurora.plugin.DynIconPlg;
import com.aurora.util.DeviceProperties;

import java.util.ArrayList;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends LinearLayout implements FolderListener {
    private Launcher mLauncher;
    private Folder mFolder;
    private FolderInfo mInfo;
    private static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;
    
    //vulcan added it in 2014-6-20
    //it is used to save the dynamic priview icon in the folder
    private ArrayList<View> mDynamicPreviewList = new ArrayList<View>();
    public DynIconPlg mDynIconPlg = null;//it is used to get dynamic feature for folder

    // The number of icons to display in the
    // AURORA-START::默认布局为4*4::Update::郝晶晶::2013-09-09
    private static final int NUM_ITEMS_IN_PREVIEW = 4;
    // AURORA-END::默认布局为4*4::Update::郝晶晶::2013-09-09
    private static final int CONSUMPTION_ANIMATION_DURATION = 120; 
    private static final int DROP_IN_ANIMATION_DURATION = 400; 
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.15f; // 0.3f

    // The amount of vertical spread between items in the stack [0...1]
    private static final float PERSPECTIVE_SHIFT_FACTOR = 0.24f;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;

    public static Drawable sSharedFolderLeaveBehind = null;

    protected ImageView mPreviewBackground;
    
    //Aurora-start:Application Icon 设计::xiejun::modify
    //private BubbleTextView mFolderName;
    private ShadowTextView mFolderName;
    //Aurora-end:Application Icon 设计::xiejun::modify

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    // Aurora <haojj> <2013-11-2> add for 每个item所占可用preview中的大小 begin
    private int mItemInPreviewSize;
    private int paddingX, paddingY, middlePaddingX, middlePaddingY;
    private float scale;
    
    private boolean mDropOnDock = false;
    protected void notifyDropOnDock(){
    	mDropOnDock = true;
    }
	// Aurora <haojj> <2013-11-2> end
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();
    public static int REAL_ICON_HEIGHT = -1;
    public static int REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME = -1;
    
    //问价夹内被拖拽至删除的图标，不让其显示；ht 2014-09-11；
    public ShortcutInfo mitem;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        REAL_ICON_HEIGHT = getResources().getDimensionPixelSize(R.dimen.app_icon_real_size);
		REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME = getResources().getDimensionPixelSize(R.dimen.app_icon_size);
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.isSmall();
    }
    
    /**
     * vulcan created it in 2014-7-25
     * @param resId
     * @param launcher
     * @param group
     * @param folderInfo
     * @param iconCache
     * @return
     */
    static public FolderIcon createFolderIcon(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache ) {
    	FolderIcon newIcon = null;
    	if(!launcher.hasSaveFolderIcon()) {
    		newIcon = FolderIcon.fromXml(resId,launcher,group,folderInfo,iconCache);
    		return newIcon;
    	}
    	
    	newIcon = launcher.getFolderIconFromSaved(folderInfo.id);
    	
    	if(newIcon == null) {
    		newIcon = FolderIcon.fromXml(resId,launcher,group,folderInfo,iconCache);
    		return newIcon;
    	}
    	
    	folderInfo.opened = true;
    	folderInfo.addListener(newIcon.mFolder);
    	folderInfo.addListener(newIcon);
    	//folderInfo.restoreCheckItems(launcher.mCheckedFolderInfos);

    	newIcon.setTag(folderInfo);
    	newIcon.mFolder.setInfo(folderInfo);
    	newIcon.mInfo = folderInfo;
    	newIcon.mFolder.mContent.setmInfo(folderInfo);
    	newIcon.mFolder.mContent.reloadContentItemInfo(folderInfo);

    	//launcher.reloadCheckedItemInfo(folderInfo);

    	return newIcon;
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        //AUROR-START:Application Icon 设计::xiejun::modify
     	//icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName = (ShadowTextView) icon.findViewById(R.id.folder_icon_name);
        //AUROR-END:Application Icon 设计::xiejun::modify
        icon.mFolderName.setText(folderInfo.title);
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        Resources res = launcher.getResources();
        //TODO:NOTE3
        if(DeviceProperties.isNeedScale()){
        	int w = res.getDimensionPixelSize(R.dimen.folder_preview_size_scale);
        	ViewGroup.LayoutParams ls=icon .mPreviewBackground.getLayoutParams();
        	ls.width = w;
        	ls.height = w;
        	icon.mPreviewBackground.setLayoutParams(ls);
        	icon.mPreviewBackground.setImageResource(R.drawable.portal_foler_icon_outline_scale);
        	//icon.mPreviewBackground.setBackgroundDrawable(res.getDrawable(R.drawable.portal_foler_icon_outline));
        }
        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);
        icon.mFolder = folder;

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);
        
        //vulcan created it in 2014-6-19
        //because content of folder probably changes at any time,
        //so when that happens, we have to refresh the mDynIconPlg member.
        icon.refreshDynPreviewIcon();
		if (icon.getDynPreviewItems().size() > 0) {
			icon.mDynIconPlg = DynIconPlg.produceDynIconPlg(folderInfo, icon);
		}
		else {
			icon.mDynIconPlg = null;
		}

        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        private CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public Drawable mOuterRingDrawable = null;
        public Drawable mInnerRingDrawable = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;
        // Aurora <haojj> <2013-11-2> add for field begin
        public static int sPreviewIconItemPadding = -1;
        public static int sPreviewIconSize = -1;
        public static int sPreviewIconPadding = -1;
        public static int sPreviewIconBottomPadding = -1;
        // Aurora <haojj> <2013-11-2> end
        
        private Animator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();
            mOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_holo);
            mInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);
            
            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            //TODO:NOTE3
            if (sStaticValuesDirty) {
            	if(DeviceProperties.isNeedScale()){
            		sPreviewSize = res.getDimensionPixelSize(R.dimen.folder_preview_size_scale);
                    sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);    
                    sPreviewIconItemPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_item_padding);
                    sPreviewIconSize = res.getDimensionPixelSize(R.dimen.folder_preview_size_scale);
                    sPreviewIconPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_left_padding);
                    sPreviewIconBottomPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_buttom_padding);
                    sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_foler_icon_outline_scale);
                    sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);
                    sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
            	}else{
            		sPreviewSize = res.getDimensionPixelSize(R.dimen.folder_preview_size);
                    sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);    
                    sPreviewIconItemPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_item_padding);
                    sPreviewIconSize = res.getDimensionPixelSize(R.dimen.folder_icon_preview_size);
                    sPreviewIconPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_left_padding);
                    sPreviewIconBottomPadding = res.getDimensionPixelSize(R.dimen.folder_icon_preview_buttom_padding);
                    sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_foler_icon_outline);
                    sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_holo);
                    sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
            	}
                
                sStaticValuesDirty = false;
            }
        }
        
        public void animateToAcceptState(boolean isRepeat) {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            
            ValueAnimator animator1 = LauncherAnimUtils.ofFloat(0f, 1f);
            animator1.setDuration(CONSUMPTION_ANIMATION_DURATION);
            final int previewSize = sPreviewSize;
            AnimatorUpdateListener updateCb = new AnimatorUpdateListener() {
            	public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            };
            animator1.addUpdateListener(updateCb);
            animator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                	// TODO Auto-generated method stub
                	// mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                	// mFolderIcon.invalidate();
                }
            });
            animator1.setInterpolator(new DecelerateInterpolator());
                        
            if(isRepeat){
            	mAcceptAnimator = new AnimatorSet();
	            ValueAnimator animator2 = LauncherAnimUtils.ofFloat(1f, 0f);
	            animator2.setDuration(CONSUMPTION_ANIMATION_DURATION );
	            animator2.addUpdateListener(updateCb);
	            animator2.setRepeatCount(3); //4 to 0 change by haojj
	            animator2.setRepeatMode(Animation.REVERSE);
	            animator2.setStartDelay(400);
	            animator2.setInterpolator(new DecelerateInterpolator(1));
	            ((AnimatorSet)mAcceptAnimator).playSequentially(animator1, animator2);
            }else{
            	mAcceptAnimator = animator1;
            }
            mAcceptAnimator.start();
            //AURORA-START:xiejun:20130923:ID138
        }
        

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(0f, 1f);
            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    // Aurora <haojj> <2013-10-11> add for ALPHA begin
                    //mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    //mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    // Aurora <haojj> <2013-10-11> end
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                    	// Aurora <haojj> <2013-10-11> add for ALPHA begin
                         mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                        // Aurora <haojj> <2013-10-11> end
                    }
                }
            });
            mNeutralAnimator.setInterpolator(new DecelerateInterpolator());
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    public Folder getFolder() {
        return mFolder;
    }
    
	/**
	 * vulcan created and tested it in 2014-6-20
	 * get all the dynamic icons of a folder
	 */
    public ArrayList<View> getDynPreviewItems() {
    	return mDynamicPreviewList;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        return !mFolder.isDestroyed() && willAcceptItem(item);
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item);
    }
    
    /**
     * 文件夹内的图标被移至删除区域；在原位置上，不显示该图标
     * 
     * @author ht 2014-09-11
     */
    public void addItem(){
    	if(mitem != null){
    		mInfo.add(mitem);
    	}
    	mitem = null;
    } 
    
    
	/**
	 * vulcan created and tested it in 2014-6-20
	 * get the newest dynamic icons from current folder's layout
	 */
	public ArrayList<View> refreshDynPreviewIcon() {
		
		//Log.d("vulcan-iconop","refreshPreviewIcon: before size is :" + mPreviewList.size());
		
		// refresh preview list when add new item
		if (mFolder != null) {
			FolderWorkspace w = mFolder.getFolderWorkspace();
			if (w != null) {
				mDynamicPreviewList = w.getDynPreviewItems(NUM_ITEMS_IN_PREVIEW);
			}

		}
		
		//Log.d("vulcan-iconop","refreshPreviewIcon: after size is :" + mPreviewList.size());

		return mDynamicPreviewList;
    }
    
    //AURORA-START:xiejun:20130923:ID138
    private Runnable openFolderRunnable=new Runnable() {	
		@Override
		public void run() {
			openFolder();
		}
	};
	
	private void openFolder(){
		// Aurora <haojj> <2013-9-30> add for 设置folderworkspace背景 begin
		Folder folder = FolderIcon.this.getFolder();
		folder.setOpenFolderAndDrag(true);
		// 当文件夹A中只有两个item的时候，打开拖动其中一个到workspace（此时不松手），拖动打开文件夹A，再拖动到workspace松开手
		// item添加到workspace成功，但此时文件夹A依然存在，但只有一个item，notifyDrop此时是没有价值的，应去掉
		// folder.notifyDrop();
		// Aurora <haojj> <2013-9-30> end
		mLauncher.openFolder(FolderIcon.this, false);
	}
	
	//AURORA-END:xiejun:20130923:ID138
    public void onDragEnter(Object dragInfo, DragObject d) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        if(d != null && d.dragSource instanceof AppsCustomizePagedView) {
        	mFolderRingAnimator.animateToAcceptState(false);
        } else {
        	postDelayed(openFolderRunnable, 1000); 
        	mFolderRingAnimator.animateToAcceptState(true);
        }
        layout.showFolderAccept(mFolderRingAnimator);
    }

    public void onDragOver(Object dragInfo) {
    }

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = ((TextView) destView).getCompoundDrawables()[1];
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = ((TextView) finalView).getCompoundDrawables()[1];
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(), 
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
    	//AURORA-START:xiejun:20130923:ID138
    	if(openFolderRunnable!=null){
    		removeCallbacks(openFolderRunnable);
    	}
    	//AURORA-END:xiejun:20130923:ID138
        mFolderRingAnimator.animateToNaturalState();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
            DragObject d) {
        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                    center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
    }
    
    // Aurora <haojj> <2013-11-11> add for field begin
    public void onDrop(DragObject d, boolean animate) {
        ShortcutInfo item;
        if (d.dragInfo instanceof ApplicationInfo) {
            // Came from all apps -- make a copy
            item = ((ApplicationInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        if(animate){
        	onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
        } else {
        	item.cellX = -1;
            item.cellY = -1;
            //addItem(item); //ht添加回去？！！
            if(item.flags == 0){
            	addItem(item); //系统应用则无法删除；
            }else{
            	mitem = item; //常规应用可以删除
            }
            Log.v("iht-lv", "FolderIcon.onDrop( d , boolean  )+++++++++:::若没有被添加，又被显示出来；---> Launcher.onResume():"+item.flags); 
		}
    }
    // Aurora <haojj> <2013-11-11> end
    
    public void onDrop(DragView dragView, ShortcutInfo item) {
        mFolder.notifyDrop();
        onDrop(item, dragView, null, 1.0f, mInfo.contents.size(), null, null);
    }
    
    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
    	Log.i("compute","drawableSize  =  "+drawableSize+"   totalSize = "+totalSize+" this :"+this.getWidth()+"  "+this.getHeight());
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;
    		int width = getWidth();
    		int height = getHeight();
    		int paddingTop = getPaddingTop();
    		final int previewSize = FolderRingAnimator.sPreviewSize;
            final int previewPadding = FolderRingAnimator.sPreviewPadding;
            final int previewIconItemPadding = FolderRingAnimator.sPreviewIconItemPadding;
            mAvailableSpaceInPreview = previewSize;
            int preIconItemSize = mItemInPreviewSize = (previewSize -3*previewIconItemPadding)/2;
			scale = ((float) preIconItemSize) / previewSize;
			int extraLine = ((int)((mIntrinsicIconSize -mAvailableSpaceInPreview)*scale))/2;
			middlePaddingX = middlePaddingY = previewIconItemPadding;
			paddingX = (mTotalWidth - mAvailableSpaceInPreview) / 2
					+ previewIconItemPadding-extraLine;
			paddingY = previewPadding + previewIconItemPadding-extraLine;
			Log.i("compute", "paddingX = " + paddingX + "    paddingY = "
					+ paddingY + "  middlePaddingX = " + middlePaddingX
					+ "  paddingTop = " + paddingTop
					+ "  mItemInPreviewSize = " + mItemInPreviewSize
					+ "  paddingLeft = " + getPaddingLeft()+"   , previewPadding = "+previewPadding);
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
    	if(d == null){
    		return;
    	}
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int[] center) {
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams);
        if(mDropOnDock){
        	mPreviewOffsetX = 30;
        	mPreviewOffsetY = 30;
        	mDropOnDock = false;
        }
        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mItemInPreviewSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mItemInPreviewSize) / 2;
        
        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

	private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
			PreviewItemDrawingParams params) {
		float transY = 0;
		float transX = 0;
		final int overlayAlpha = 0;
		if (index == 0) {
			transX = paddingX;
			transY = paddingY;
		} else if (index == 1) {
			transX = paddingX + mItemInPreviewSize + middlePaddingX - 1;
			transY = paddingY;
		} else if (index == 2) {
			transX = paddingX;
			transY = paddingY + mItemInPreviewSize + middlePaddingY - 1;
		} else if (index == 3) {
			transX = paddingX + mItemInPreviewSize + middlePaddingX - 1;
			transY = paddingY + mItemInPreviewSize + middlePaddingY - 1;
		} else if(index == NUM_ITEMS_IN_PREVIEW){
			transX = paddingX + mItemInPreviewSize / 2;
			transY = paddingY + mItemInPreviewSize / 2;
		}

		if (params == null) {
			params = new PreviewItemDrawingParams(transX, transY, scale,
					overlayAlpha);
		} else {
			params.transX = transX;
			params.transY = transY;
			params.scale = scale;
			params.overlayAlpha = overlayAlpha;
		}

		return params;
	}
    
    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX, params.transY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;
        if (d != null) {
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            d.setFilterBitmap(true);
            d.setColorFilter(Color.argb(params.overlayAlpha, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
            d.draw(canvas);
            d.clearColorFilter();
            d.setFilterBitmap(false);
        }
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mFolder == null) {
        	super.dispatchDraw(canvas);
        	return ;
        }
        if (mFolder.getItemCount() == 0 && !mAnimating) {
        	super.dispatchDraw(canvas);
        	return;
        }

        super.dispatchDraw(canvas);
        ArrayList<View> items = mFolder.getItemsInReadingOrder(false);
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
            v = (TextView) items.get(0);
            d = v.getCompoundDrawables()[1];
            computePreviewDrawingParams(d);
        }

        int nItemsInPreview = Math.min(items.size(), NUM_ITEMS_IN_PREVIEW);
        if (!mAnimating) {
            for (int i = nItemsInPreview - 1; i >= 0; i--) {
                v = (TextView) items.get(i);
                if (!mHiddenItems.contains(v.getTag())) {
                    d = v.getCompoundDrawables()[1];
                    // AURORA-START::采用新的布局来构造FolderIcon::Update::郝晶晶::2013-09-09
                    // mParams = computePreviewItemDrawingParams(i, mParams);
                    computePreviewItemDrawingParams(i, mParams);
                    // AURORA-END::采用新的布局来构造FolderIcon::Update::郝晶晶::2013-09-09
                    mParams.drawable = d;
                    drawPreviewItem(canvas, mParams);
                }
            }
        } else {
            drawPreviewItem(canvas, mAnimParams);
        }
        items.clear();
        
        drawSelectItemCount(canvas);
    }

    // Aurora <haojj> <2013-10-8> add for 绘制多选添加icon到文件夹时的Folder数目显示 begin
	private boolean isEditMode(){
		return mLauncher.getEditMode() == EditMode.FOLDER_IMPORT;
	}
    
    private void drawSelectItemCount(Canvas canvas){
    	if(!isEditMode()) return;
    	int number = mInfo.checkInfos.size();

    	Log.d("vulcan-crash","drawSelectItemCount: mInfo = " + mInfo.hashCode());
    	
    	if(number > 0) {
	    	// 绘制底图 icons width are equal height
	        int width = REAL_ICON_HEIGHT;
			int realWidth = REAL_ICON_HEIGHT_INCLUDE_ALPHA_ZOME;
	        Drawable mSelectDrawable = getResources().getDrawable(R.drawable.select_count);
			int widthOfSelectDrawable = mSelectDrawable.getIntrinsicWidth();
			int heightOfSelectDrawable = mSelectDrawable.getIntrinsicHeight();
			int left = (getWidth()-width)/2+width-widthOfSelectDrawable/2-1;
			int top = getPaddingTop()/*+(realWidth-width)/2*/-heightOfSelectDrawable/2+1;
			top = top<=0?0:top;
			int right = left+widthOfSelectDrawable;
			int buttom = top+heightOfSelectDrawable;
			if(right>getWidth()){
				left = getWidth()-widthOfSelectDrawable;
				right = getWidth();
			}
			mSelectDrawable.setBounds(left, top, right, buttom);
			mSelectDrawable.draw(canvas);
			
			
			// 绘制数字
			Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG
					| Paint.DEV_KERN_TEXT_FLAG);
			countPaint.setColor(getResources().getColor(R.color.folder_icon_select_txt_color));
			
			countPaint.setTextSize((float)(getResources().getInteger(R.integer.folder_select_count_textsize)));
			countPaint.setTypeface(Typeface.DEFAULT_BOLD);
			countPaint.setTextAlign(Paint.Align.CENTER);
			Rect textBounds = new Rect();
			String numberStr = String.valueOf(number);
			countPaint.getTextBounds(numberStr, 0, numberStr.length(), textBounds);//get text bounds, that can get the text width and height
			int textHeight = textBounds.bottom - textBounds.top;
			canvas.drawText(numberStr, left + widthOfSelectDrawable/2, top + (heightOfSelectDrawable + textHeight)/2,
					countPaint);
    	}
    }
	// Aurora <haojj> <2013-10-8> end
    
    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null);

        final float scale0 = 1.0f;
        final float transX0 = (mAvailableSpaceInPreview - d.getIntrinsicWidth()) / 2;
        final float transY0 = (mAvailableSpaceInPreview - d.getIntrinsicHeight()) / 2;
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }

                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setInterpolator(new DecelerateInterpolator());
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title.toString());
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return result;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

	@Override
	public void onMultiAdd(ArrayList<ShortcutInfo> items) {
		// TODO Auto-generated method stub
		invalidate();
        requestLayout();
	}

	@Override
	public void onMultiRemove(ArrayList<ShortcutInfo> items) {
		// TODO Auto-generated method stub
		invalidate();
        requestLayout();
	}

	@Override
	public void onClearChecked() {
		// TODO Auto-generated method stub
		invalidate();
	}

	@Override
	public void onProcessAfterUnload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateShortcutItem(ShortcutInfo item) {
		// TODO Auto-generated method stub
		invalidate();
	}
	
	@Override
	protected void onAttachedToWindow() {

		super.onAttachedToWindow();
		if (mDynIconPlg != null) {
			Log.d("vulcan-iconlist","FolderIcon:onAttachedToWindow, this is: " + this);
			mDynIconPlg.onAttachedToWindow();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mDynIconPlg != null) {
			Log.d("vulcan-iconlist","FolderIcon:onDetachedFromWindow, this is: " + this);
			mDynIconPlg.onDetachedFromWindow();
		}
	}
	
}
