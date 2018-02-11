package com.aurora.view;

import java.util.ArrayList;

import com.aurora.launcher.DragView;
import com.aurora.launcher.DropTarget;
import com.aurora.launcher.Launcher;
import com.aurora.launcher.LauncherAnimUtils;
import com.aurora.launcher.QuickDropLocate;
import com.aurora.launcher.QuickDropTarget;
import com.aurora.launcher.QuickDropUninstall;
import com.aurora.launcher.QuickIndexGridAdapter;
import com.aurora.launcher.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.ClipData.Item;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class QuickIndexFrameLayout extends FrameLayout {
	private int mMarginBottom = 0;

	public boolean mDraging = false;

	private Launcher mLauncher;

	private DragView dragView;

	/** X coordinate of the down event. */
	private int mMotionDownX;

	/** Y coordinate of the down event. */
	private int mMotionDownY;

	private final float AppInitScale = 0.97f;

	private final float ItemPickedAlpha = 0.25f;

	private final float ItemRestoreAlpha =1.0f;
	
	private LinearLayout mDropTargetBarLayout;

	private SearchFrameLayout mSearchFrameLayout;

	private ArrayList<QuickDropTarget> mDropTargets = new ArrayList<QuickDropTarget>();

	private QuickDropLocate mQuickDropLoacate;
	private QuickDropUninstall mQuickDropUninstall;

    private  View DragViewItem;
    
	private ObjectAnimator mDropTargetBarAnim;

    /**animation transition start duration*/
	private static final int sTransitionInDuration = 350;
	
	/**bar Height indicate that the Drop bar height*/
	private int mBarHeight;
	
	private QuickIndexGridAdapter ContextGridAdapter;
	
	private static final AccelerateInterpolator sAccelerateInterpolator = new AccelerateInterpolator();
	
	public QuickIndexFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMarginBottom = context.getResources().getDimensionPixelOffset(
				R.dimen.quick_index_outline_framelayout_margin_bottom);
		// TODO Auto-generated constructor stub
		mLauncher = (Launcher) context;
	}

	public void updateMargin(boolean filter) {
		// TODO Auto-generated method stub
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
		if (filter) {
			params.bottomMargin = 0;
		} else {
			params.bottomMargin = mMarginBottom;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		final int action = ev.getAction();
		final int[] dragLayerPos = mLauncher.getDragController()
				.getCommonClampedDragLayerPos(ev.getX(), ev.getY());
		final int dragLayerX = dragLayerPos[0];
		final int dragLayerY = dragLayerPos[1];
		if (getDragState()) {
			return true;
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mMotionDownX = dragLayerX;
			mMotionDownY = dragLayerY;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		final int[] dragLayerPos = mLauncher.getDragController()
				.getCommonClampedDragLayerPos(ev.getX(), ev.getY());
		final int dragLayerX = dragLayerPos[0];
		final int dragLayerY = dragLayerPos[1];
	    if (ev.getAction() == MotionEvent.ACTION_MOVE) {
	    	if(DragViewItem.getAlpha()!=ItemPickedAlpha){
				DragViewItem.setAlpha(ItemPickedAlpha);
			} 
	    	handleDropEvent(dragLayerX, dragLayerY);
			dragView.move(dragLayerX, dragLayerY);
			dragView.invalidate();
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			cancelDrag();
			handleDropEvent(dragLayerX, dragLayerY);
			removeAllDropTargetCategories();
		}else if(ev.getAction() == MotionEvent.ACTION_CANCEL){
			cancelDrag();
			removeAllDropTargetCategories();
		}
		return super.onTouchEvent(ev);  
	}

	public void setDragState(boolean isDraging) {
		mDraging = isDraging;
	}

	public boolean getDragState() {
		return mDraging;
	}

	/**
	 * Desc :Drag item and reference from DragController
	 * */
	public void startDrag(View v, Bitmap bm, int dragLayerX, int dragLayerY,
			Object dragInfo) {
		
		if (!mDraging) { //Avoid the double shadow? 
			Log.e("linp", "start drag!");
			mQuickDropLoacate.initialize(getContext());
			mQuickDropUninstall.initialize(getContext());
			setupQuickDragView(bm, dragLayerX, dragLayerY);
			hideSearchFrameLayout();
			showDropTargetBar();
			addDropTargetCategories();
			startDropDownAnimation(mDropTargetBarLayout);
			mDraging = true;
			DragViewItem = v;
			ContextGridAdapter.mGridEmbed.performHapticFeedback(
					HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		}
	}

	public void setQuickIndexGridAdapterContext(QuickIndexGridAdapter cx){
		ContextGridAdapter = cx;
	}
	
	/** setup dragview parameter and show it */
	private void setupQuickDragView(Bitmap bm, int dragLayerX, int dragLayerY) {
		int registrationX = mMotionDownX - dragLayerX;
		int registrationY = mMotionDownY - dragLayerY;
		dragView = new DragView(mLauncher, bm, registrationX, registrationY, 0, 0,  bm.getWidth(), bm.getHeight(), AppInitScale, this);
		dragView.show(mMotionDownX, mMotionDownY);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		// start parser the widget
		mDropTargetBarLayout = (LinearLayout) findViewById(R.id.quick_index_drop_target_bar);
		mSearchFrameLayout = (SearchFrameLayout) findViewById(R.id.quick_search_header);
		mQuickDropLoacate = (QuickDropLocate) findViewById(R.id.quicklocate_drop_target);
		mQuickDropUninstall = (QuickDropUninstall) findViewById(R.id.uninstall_drop_target);
		/**Start Inflate animation */
		mBarHeight = getResources().getDimensionPixelSize(R.dimen.quick_drop_bar_height);
	}

	public SearchFrameLayout getSearchLayout(){
		return mSearchFrameLayout;
	}
	
	private void showDropTargetBar() {
		mDropTargetBarLayout.setVisibility(View.VISIBLE);
	}

	private void hideDropTargetBar() {
		mDropTargetBarLayout.setVisibility(View.INVISIBLE);
	}

	private void showSearchFrameLayout() {
		mSearchFrameLayout.setVisibility(View.VISIBLE);
	}

	private void hideSearchFrameLayout() {
		mSearchFrameLayout.setVisibility(View.INVISIBLE);
	}

	private void hideQuickSearchFilterScreen(QuickDropTarget mTarget){
	   if(mLauncher.getEditMode() !=mLauncher.getEditMode().QUICK_INDEX  || !(mTarget instanceof QuickDropUninstall)){
 		if(mLauncher.isQuickSearchFilterMode()){
			mLauncher.ExitQuickSearchFilterMode();
		}
       }
	}

	private void startDropDownAnimation(final View v){
		v.setTranslationY(-mBarHeight);
		mDropTargetBarAnim = LauncherAnimUtils.ofFloat(v,
				"translationY", -mBarHeight, 0f);
		mDropTargetBarAnim.setInterpolator(sAccelerateInterpolator);
		mDropTargetBarAnim.setDuration(sTransitionInDuration);
		mDropTargetBarAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				v.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});
		v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		v.buildLayer();
		mDropTargetBarAnim.start();	
	}
	
	private void reverseDropDownAnimation(View v){
		v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		v.buildLayer();
		mDropTargetBarAnim.reverse();
	}
	
	private void addDropTargetCategories() {
		if (mDropTargets != null) {
			mDropTargets.add(mQuickDropLoacate);
			mDropTargets.add(mQuickDropUninstall);
		}
	}
	
	private void removeAllDropTargetCategories() {
		mDropTargets.clear();
	}

	private void handleDropEvent(int DragX, int DragY) {
		int DropContainerSize = mDropTargets.size();
		Rect r = new Rect();
		for (int count = 0; count < DropContainerSize; count++) {
			QuickDropTarget mDrop = mDropTargets.get(count);
			// mDropTmp.handleDrop();
			mDrop.getWidgetRect(r);
			if (r.contains(DragX, DragY)) {
				if (!getDragState()) {
					hideQuickSearchFilterScreen(mDrop);
					mDrop.stopDropTargetAnim();
					mDrop.resetDropTargetBackground();
					mDrop.handleDrop(DragViewItem);
               }else{
            	   //TODO Set QuickDropTarget Background or QuickDropLocate Background
            	   mDrop.startDropTargetAnim();
               }
			}else{
				mDrop.stopDropTargetAnim();
				mDrop.resetDropTargetBackground();
			}
		}
	}
	
	public void cancelDrag(){
		setDragState(false);
		dragView.remove();
		reverseDropDownAnimation(mDropTargetBarLayout);			
		showSearchFrameLayout();
		startDropDownAnimation(mSearchFrameLayout);
		DragViewItem.setAlpha(ItemRestoreAlpha);
		mQuickDropLoacate.clear();
		mQuickDropUninstall.clear();
	}

}
