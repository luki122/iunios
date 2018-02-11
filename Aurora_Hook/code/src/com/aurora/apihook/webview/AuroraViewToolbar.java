package com.aurora.apihook.webview;

import java.util.Iterator;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.R;

public class AuroraViewToolbar {
    //Gionee CR00311071 jipengfei 20110730 start
    private static final int TOLERANCE_TOUCH = 3;
    private static final int TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT = 12;
    private static final int TOOLBAR_ITEM_PADDING_BOTTOM = 3;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT = 14;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT = 14;
    private static final int TOOLBAR_ARROW_OFFSET_TO_EDGE = 15;
    public static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;
    public static final int ID_CUT = android.R.id.cut;
    public static final int ID_COPY = android.R.id.copy;
    private static final int ID_SWITCH_INPUT_METHOD = android.R.id.switchInputMethod;

    private static final int ID_SELECT_ALL_STR = R.string.aurora_selectAll;
    private static final int ID_START_SELECTING_TEXT_STR = R.string.aurora_select;
    private static final int ID_CUT_STR = R.string.aurora_cut;
    private static final int ID_COPY_STR = R.string.aurora_copy;
    private static final int ID_SWITCH_INPUT_METHOD_STR = R.string.aurora_inputMethod;

    public static final int ID_SEARCH = android.R.id.selectTextMode;
    public static final int ID_SHARE = ID_SWITCH_INPUT_METHOD;
    
    private static final int ID_SEARCH_STR = R.string.aurora_search;
    private static final int ID_SHARE_STR = R.string.aurora_share;
    
    private TextView mItemSelectAll;
    private TextView mItemSearch;
    private TextView mItemShare;
    private TextView mItemCopy;
    private TextView mItemCut;
    //Gionee CR00311071 jipengfei 20110730 end
    protected View mHostView;
    protected Context mContext;

    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams mLayoutParams = null;
    protected LayoutInflater mLayoutInflater;
    protected ViewGroup mToolbarGroup;
    protected View mToolbarView;
    protected ImageView mToolbarPositionArrowView;

    protected boolean mShowing = false;
//gionee 20121210 guoyx modified for CR00734816 begin
    private int mLeftDrawableResId;
    private int mCenterDrawableResId;
    private int mRightDrawableResId;
//gionee 20121210 guoyx modified for CR00734816 end	
    private Drawable mSingleDrawable;
    private Drawable mArrowAboveDrawable;
    private Drawable mArrowBelowDrawable;

    private int mStatusBarHeight;
    private int mToolbarPositionArrowWidth;
    private int mToolbarPositionArrowHeight;

    private int mPositionX, mPositionY;

    protected int mToleranceTouch;
    protected int mToolbarItemPaddingLeftAndRight;
    protected int mToolbarItemPaddingBottom;

    // Gionee <zhangxx> <2013-06-22> add for CR00827967 begin
    private final int TOOLBAR_TOP_OFFSET = 10;
    private float mScale;
    
    private int mEditBarArrowPaddingLeft;
    private int mEditBarArrowPaddingRight;
    
    private boolean mAboveCursor ;
    
    private boolean mSelectionEditable = false;
    // Gionee <zhangxx> <2013-06-22> add for CR00827967 end

    //gionee 20121210 guoyx modified for CR00734816 begin
    /**
     * adjust the toolbar show position when touch on the contact edit area.
     */
    private int mOffsetToolbar = TOOLBAR_TOP_OFFSET;
    //gionee 20121210 guoyx modified for CR00734816 end
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (isShowing()) {
            	if(mItemClickListener != null){
            		mItemClickListener.onItemAction(v.getId());
            	}
            	if(v.getId() != ID_SELECT_ALL){
            		hide();
            	}
            }
        }
    };
    public AuroraViewToolbar(View hostView) {
        this.mHostView = hostView;
        this.mContext = mHostView.getContext();
        // Gionee <zhangxx> <2013-06-22> add for CR00827967 begin 
        mScale = mContext.getResources().getDisplayMetrics().density;
        // Gionee <zhangxx> <2013-06-22> add for CR00827967 end
        // initial resources
        Resources resources = mHostView.getResources();
		//gionee 20121210 guoyx modified for CR00734816 begin
        mLeftDrawableResId = R.drawable.aurora_text_toolbar_left;
        mCenterDrawableResId = R.drawable.aurora_text_toolbar_center;
        mRightDrawableResId = R.drawable.aurora_text_toolbar_right;
		//gionee 20121210 guoyx modified for CR00734816 end
        mSingleDrawable = resources.getDrawable(R.drawable.aurora_text_toolbar_single);
        mArrowAboveDrawable = resources.getDrawable(R.drawable.aurora_text_toolbar_position_arrow_above);
        mArrowBelowDrawable = resources.getDrawable(R.drawable.aurora_text_toolbar_position_arrow_below);
        // initial window manager
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        calculateTolerance();
        mStatusBarHeight = getStatusBarHeight();
        // initial tool bar and it's items.
        mLayoutInflater = LayoutInflater.from(mContext);
        mToolbarView = mLayoutInflater.inflate(R.layout.aurora_text_toolbar, null);
        mToolbarGroup = (ViewGroup) mToolbarView.findViewById(com.aurora.internal.R.id.aurora_toolbar_group);
        //Gionee CR00311071 jipengfei 20110730: comment out this line, it has nonsense
        //mToolbarGroup.setPadding(2, 2, 2, 2);
        mToolbarPositionArrowView = (ImageView) mToolbarView.findViewById(com.aurora.internal.R.id.aurora_toolbar_position_arrow);
       
        // calculate initial size of tool bar.
        mToolbarView.measure(0, 0);
	        mToolbarPositionArrowWidth = mToolbarPositionArrowView.getMeasuredWidth();
	        mToolbarPositionArrowHeight = mToolbarPositionArrowView.getMeasuredHeight();
	        
	        mEditBarArrowPaddingLeft = mToolbarPositionArrowView.getPaddingLeft();
	        mEditBarArrowPaddingRight = mToolbarPositionArrowView.getPaddingRight();
        initToolbarItem();
    }

    /**
     * @return Whether the toolbar is showing.
     */
    public boolean isShowing() {
        return mShowing;
    }
    
    public void updateItem(boolean editable){
    	mSelectionEditable = editable;
    	if(mToolbarGroup != null){
    		mToolbarGroup.findViewById(android.R.id.cut).setVisibility(editable?View.VISIBLE:View.GONE);
    	}
    }

    /**
     * Show toolbar at assigned position relative to left-top of screen.
     * @param screenX
     * @param screenY
     * @param selected
     */
    public void show(int screenX, int screenY, boolean selected) {
        if (!mShowing) {
            showInternal(screenX, screenY, 0, selected);
        }
    }

    /**
     * Move toolbar to assigned position relative to left-top of screen.
     * @param screenX
     * @param screenY
     */
    public void move(int screenX, int screenY, boolean aboveCursor) {
        if (mShowing) {
            moveInternal(screenX, screenY,aboveCursor);
        }
    }
    protected void moveInternal(int screenX, int screenY,  boolean aboveCursor) {
        if (mToolbarGroup.getChildCount() < 1) {
            hide();
            return;
        }
        prepare(screenX, screenY, aboveCursor);
        // reposition the toolbar.
        Log.e("move", "move--------->"+" mPositionX:"+mPositionX+" mPositionY:"+mPositionY);
        WindowManager.LayoutParams lp = mLayoutParams;
        lp.x = mPositionX;
        lp.y = mPositionY;
        mWindowManager.updateViewLayout(mToolbarView, lp);
    }

    /**
     * Hide the toolbar.
     */
    public void hide() {
        if (mShowing) {
            try {
                //Gionee CR00311071 jipengfei 20110730: comment out this line, it has nonsense
                //mToolbarGroup.setPadding(2, 2, 2, 2);
                mToolbarPositionArrowView.setPadding(0, 0, 0, 0);
//                mWindowManager.removeView(mToolbarView);
                mWindowManager.removeViewImmediate(mToolbarView);
            } finally {
                // set showing flag whether hiding view is successful.
                mShowing = false;
            }
        }
    }

    /**
     * Update items of toolbar.
     */
	protected void updateToolbarItems() {
		mToolbarGroup.removeAllViews();

		boolean hasClip = ((ClipboardManager) mHostView.getContext()
				.getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip();
		mToolbarGroup.addView(mItemSelectAll);
		mToolbarGroup.addView(mItemCopy);
		mToolbarGroup.addView(mItemCut);
		mItemCut.setVisibility(View.GONE);
//		if(hasClip){
//			mToolbarGroup.addView(mI);
//		}
		mToolbarGroup.addView(mItemShare);
//		
		mToolbarGroup.addView(mItemSearch);
		mToolbarGroup.postInvalidate();

	}

    protected void showInternal(int screenX, int screenY, int cursorLineHeight, boolean aboveCursor) {
        // update tool bar.
        update();
        prepare(screenX, screenY,aboveCursor);
        if (mToolbarGroup.getChildCount() < 1) {
        	 Log.e("luofu", "mToolbarGroup.getChildCount():");
            hide();
            return;
        }
        
        Log.e("luofu", "mScreenY:"+screenY);
//        prepare(screenX, screenY, cursorLineHeight, selected);
        // reposition the toolbar.
        mPositionX = screenX;
        mPositionY = screenY;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.token = mHostView.getApplicationWindowToken();
        lp.x = mPositionX;
        lp.y = mPositionY;
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        } else {
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
        }
        lp.packageName = mContext.getPackageName();
        mLayoutParams = lp;
        mWindowManager.addView(mToolbarView, lp);
        // set showing flag
        mShowing = true;
      
    }
    
    

    
    private void prepare(int screenX, int screenY,boolean aboveCursor){
    	mToolbarView.measure(0, 0);
        // calculate the position of tool bar.
        //gionee 20121210 guoyx modified for CR00734816 begin
        // set position of arrow representing the trigger point. 
    	mPositionX = screenX;
    	mPositionY = screenY;
        int paddingLeft = screenX - mPositionX - mToolbarPositionArrowWidth / 2 - 20;
        //Gionee CR00311071 jipengfei 20110730: modify the calculations of paddingLeft start
        paddingLeft = Math.max(TOOLBAR_ARROW_OFFSET_TO_EDGE + TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT, paddingLeft);
        paddingLeft = Math.min(mToolbarGroup.getMeasuredWidth() - mToolbarPositionArrowWidth - TOOLBAR_ARROW_OFFSET_TO_EDGE - TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT, paddingLeft);
        if (aboveCursor) {
            mToolbarPositionArrowView.setImageDrawable(mArrowBelowDrawable);
            //mToolbarGroup.setPadding(2, 2, 2, 2);
            mToolbarPositionArrowView.setPadding(mEditBarArrowPaddingLeft, mToolbarGroup.getMeasuredHeight() - mArrowBelowDrawable.getIntrinsicHeight()*2+3,
                    mEditBarArrowPaddingRight, 0);
        } else {
            mToolbarPositionArrowView.setImageDrawable(mArrowAboveDrawable);
            mToolbarGroup.setPadding(0, mOffsetToolbar, 0, 0);
            mToolbarPositionArrowView.setPadding(mEditBarArrowPaddingLeft, mOffsetToolbar+mArrowBelowDrawable.getIntrinsicHeight()-3, mEditBarArrowPaddingRight, 0);
        }
    }


    private void update() {
        updateToolbarItems();
        // set drawable of items.
        int childCount = mToolbarGroup.getChildCount();
        if (childCount >= 2) {
           //gionee 20121210 guoyx modified for CR00734816 begin
            // Gionee <zhangxx> <2013-06-22> add for CR00827967 begin
            // mOffsetToolbar = 25;
            mOffsetToolbar = (int)(TOOLBAR_TOP_OFFSET * mScale);
            // Gionee <zhangxx> <2013-06-22> add for CR00827967 end
           //gionee 20121210 guoyx modified for CR00734816 end
            for (int i = 0; i < childCount; i++) {
                View view = mToolbarGroup.getChildAt(i);
				//gionee 20121210 guoyx modified for CR00734816 begin
                if (i == 0) {
                    view.setBackgroundResource(mLeftDrawableResId);
                    view.setPadding(mToolbarItemPaddingLeftAndRight * 2 - 1, 0, mToolbarItemPaddingLeftAndRight, mToolbarItemPaddingBottom);
                } else if (i == childCount - 1) {
                    view.setBackgroundResource(mRightDrawableResId);
                    view.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight * 2, mToolbarItemPaddingBottom);
                } else {
                    view.setBackgroundResource(mCenterDrawableResId);
                    view.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, mToolbarItemPaddingBottom);
                }
				//gionee 20121210 guoyx modified for CR00734816 end
            }
        } else if (childCount == 1) {
          //gionee 20121210 guoyx modified for CR00734816 begin
            mOffsetToolbar = 0;
          //gionee 20121210 guoyx modified for CR00734816 end
            View view = mToolbarGroup.getChildAt(0);
            view.setBackgroundDrawable(mSingleDrawable);
            view.setPadding(mToolbarItemPaddingLeftAndRight * 2, 0, mToolbarItemPaddingLeftAndRight * 2, mToolbarItemPaddingBottom);
        }
       
    }

    private boolean calculatePosition(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        boolean aboveCursor = true;
        // calculate x
        int x;
        int px = screenX - mHostView.getRootView().getScrollX();
        int half = mToolbarGroup.getMeasuredWidth() / 2;
        int displayWidth = mWindowManager.getDefaultDisplay().getWidth();
        if (px + half < displayWidth) {
            x = px - half;
        } else {
            x = displayWidth - mToolbarGroup.getMeasuredWidth();
        }
        mPositionX = Math.max(0, x);
        // calculate y
        int y;
        int py = screenY - mHostView.getRootView().getScrollY();
        int th = mToolbarGroup.getMeasuredHeight() + mToolbarPositionArrowHeight;
        int lh = cursorLineHeight / 2;
        if (py - th - lh < mStatusBarHeight) {
            y = py + lh + (selected ? mToleranceTouch : 0) + 2;
            mStatusBarHeight = y;//Luofu modified
            aboveCursor = false;
        } else {
            y = py - th - lh - (selected ? mToleranceTouch : 0) + 6;
            aboveCursor = true;
        }
        
        mPositionY = Math.max(mStatusBarHeight, y);
        Log.e("luofu", "mPositionY:"+mPositionY);
        Log.e("luofu", "mPositionY:"+mPositionY);
        
//        Log.e("luofu", "mStatusBarHeight:"+mStatusBarHeight);
//        Log.e("luofu", "mPositionY:"+mPositionY);
//        Log.e("luofu", "mPositionY:"+mPositionY);
        return aboveCursor;
    }

    private void calculateTolerance() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(dm);
        float ratio = 1.0f * dm.densityDpi / DisplayMetrics.DENSITY_MEDIUM;
        mToleranceTouch = Math.round(TOLERANCE_TOUCH * ratio);
        mToolbarItemPaddingLeftAndRight = Math.round(TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT * ratio);
        mToolbarItemPaddingBottom = Math.round(TOOLBAR_ITEM_PADDING_BOTTOM * ratio);
    }
    
    private int getStatusBarHeight () {
        /*Rect rect = new Rect();
        Context context = mHostView.getContext();
        if (context instanceof Activity) {
            Window window = ((Activity)context).getWindow();
            if (window != null) {
              window.getDecorView().getWindowVisibleDisplayFrame(rect);
              android.view.View v = window.findViewById(Window.ID_ANDROID_CONTENT);
    
              android.view.Display display = ((android.view.WindowManager) mHostView.getContext()
                      .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    
              //return result title bar height
              return display.getHeight() - v.getBottom() + rect.top;   
        }
        }
        return 0;*/
        return (int)getStatusBarHeight(mContext);
    }
    public int getStatusBarHeight(Context context){
    	Resources resources = context.getResources();
        int statusBarIdentifier = resources.getIdentifier("status_bar_height",
                "dimen", "android");
        if (0 != statusBarIdentifier) {
            return (int)resources.getDimension(statusBarIdentifier);
        }
        return 0;
    }
    
    public int getScreenWidth(){
         WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
         return wm.getDefaultDisplay().getWidth();
    }
    
    public int getScreenHeight(){
    	  WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
          return wm.getDefaultDisplay().getHeight();
    }
    protected void initToolbarItem() {
        mItemSelectAll = initToolbarItem(ID_SELECT_ALL, ID_SELECT_ALL_STR);
        mItemCopy = initToolbarItem(ID_COPY, ID_COPY_STR);
        mItemSearch = initToolbarItem(ID_SEARCH, ID_SEARCH_STR);
        mItemShare = initToolbarItem(ID_SHARE, ID_SHARE_STR);
        mItemCut = initToolbarItem(ID_CUT, ID_CUT_STR);
    }
    
    protected TextView initToolbarItem(int id, String text) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        // Gionee zhangxx 2013-03-29 modify for CR00791013 begin
        // textView.setTextAppearance(mContext, R.style.TextAppearance_GioneeView_MediumSecond);
        textView.setTextSize(16);
        
//        textView.auroraSetTextBaseLinePadding(3);
        
        textView.setTextColor(mContext.getResources().getColor(R.color.aurora_editor_toolbar_text_color));
        // Gionee zhangxx 2013-03-29 modify for CR00791013 end
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(text);
        textView.setOnClickListener(getOnClickListener());
        return textView;
    }
    
    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        // Gionee zhangxx 2013-03-29 modify for CR00791013 begin
        // textView.setTextAppearance(mContext, R.style.TextAppearance_GioneeView_MediumSecond);
        textView.setTextSize(16);
        
//        textView.auroraSetTextBaseLinePadding(3);
        
        textView.setTextColor(mContext.getResources().getColor(R.color.aurora_editor_toolbar_text_color));
        // Gionee zhangxx 2013-03-29 modify for CR00791013 end
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        Log.e("luofu", ""+mContext.getResources().getString(textResId));
        textView.setOnClickListener(getOnClickListener());
        return textView;
    }
    
    public Context getContext(){
    	
    	return mContext;
    }
    
    public int getWidth(){
    	return mToolbarGroup.getMeasuredWidth();
    }
    
    public int getHeight(){
    	return mToolbarGroup.getMeasuredHeight();
    }
    protected  OnClickListener getOnClickListener(){
    	
    	return mOnClickListener;
    }
    
    private OnItemClickListener mItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
    	this.mItemClickListener = listener;
    }
    
    public interface OnItemClickListener{
    	public void onItemAction(int id);
    }
    
    
    
    
    
}
