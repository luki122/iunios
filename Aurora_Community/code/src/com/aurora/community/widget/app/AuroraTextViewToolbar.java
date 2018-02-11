package com.aurora.community.widget.app;


import android.R.color;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.widget.AuroraEditText;
import com.aurora.community.widget.util.DensityUtil;


@SuppressLint("NewApi")
abstract class AuroraTextViewToolbar extends AuroraViewToolbar {

    private static final String TAG = "AuroraTextViewToolbar";
    protected static final int ID_PASTE = android.R.id.paste;

    protected static final int ID_PASTE_STR = R.string.aurora_paste;

    protected TextView mItemPaste;

    private int mScreenX;
    private int mScreenY;
    private int mLineHeight;

    protected AuroraEditText mEditText;

    AuroraTextViewToolbar(AuroraEditText hostView) {
        super(hostView);
        this.mEditText = hostView;        
    }

    protected void initToolbarItem() {
        // init past view
        mItemPaste = initToolbarItem(ID_PASTE, ID_PASTE_STR);
    }

    public void show() {
        if (!mShowing) {
            calculateScreenPosition();
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            showInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    public void move() {
        if (mShowing) {
            calculateScreenPosition();
            int start = mEditText.getSelectionStart();
            int end = mEditText.getSelectionEnd();
            Log.e(TAG, "move() mScreenY:"+mScreenY);
            moveInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    private Rect getLocalVisibleRect(){
        Rect r = new Rect();
        mEditText.getGlobalVisibleRect(r);
        return r;
    }
    private void calculateScreenPosition() {
        int[] location = new int[2];
        int[] position = new int[2];
        mEditText.getLocationOnScreen(location);
       // mEditText.getLocationInWindow(position);
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        Layout layout = mEditText.getLayout();
        if (layout == null ) {
        	// mEditText.assumeLayout();
        	// layout = mEditText.getLayout();
        	return;
        }
        int line = layout.getLineForOffset(start);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineBottom(line);
        mLineHeight = bottom - top;
        mScreenY = top + mLineHeight / 2 + location[1] + mEditText.getTotalPaddingTop() - mEditText.getScrollY();
        
        if (start == end) {
            mScreenX = Math.round(layout.getPrimaryHorizontal(start)) + location[0] + mEditText.getTotalPaddingLeft() - mEditText.getScrollX();
        } else {
            int left = Math.round(layout.getPrimaryHorizontal(start));
            int right;
            int lineEnd = layout.getLineForOffset(end);
            if (line == lineEnd) {
                right = Math.round(layout.getPrimaryHorizontal(end));
            } else {
                right = Math.round(layout.getLineRight(line));
            }
            mScreenX = (left + right) / 2 + location[0] + mEditText.getTotalPaddingLeft() - mEditText.getScrollX();
        }
//        Log.e(TAG, "getScrollY:"+mEditText.getScrollY()+" mScreenY:"+mScreenY+" location[1]:"+location[1]);
//        Log.e(TAG, "touched pos:"+mEditText.getTouchedPosition()[1]);
//        Log.e(TAG, "Rect Bottom:"+getLocalVisibleRect().bottom);
//        Log.e(TAG, "mEditText Bottom:"+mEditText.getBottom());
        ViewGroup parent = (ViewGroup) mEditText.getParent();
        int parentHeight = 0;
        if(parent != null){
            parentHeight = layout.getHeight();
        }
        int editTextHeight =mEditText.getHeight();
        mEditText.getExtendedPaddingBottom();
        mEditText.getExtendedPaddingTop();
        mEditText.getTranslationY();
        mEditText.getScrollY();
        Rect rect = new Rect();
        mEditText.getDrawingRect(rect);
//        Log.e(TAG, "editTextHeight:"+editTextHeight);
//        Log.e(TAG, "Rect Top:"+mEditText.getExtendedPaddingBottom());
//        Log.e(TAG, "getTranslationY:"+mEditText.getTranslationY());
        Log.e(TAG, "getHeight:"+mEditText.getHeight());
//        Log.e(TAG, "Rect Bottom:"+ rect.top);
        int halfScreen = DensityUtil.getDisplayHeight(mContext)[1] / 2;
        if(mEditText.mIsSelectedAll && (mEditText.getHeight() > halfScreen)){
            mScreenY = mEditText.getTouchedPosition()[1];
        }else{
            mScreenY = Math.max(location[1], mScreenY);
        }
        
    }

    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        // Gionee zhangxx 2013-03-29 modify for CR00791013 begin
        // textView.setTextAppearance(mContext, R.style.TextAppearance_GioneeView_MediumSecond);
        textView.setTextSize(16);
        // Gionee zhangxx 2013-03-29 modify for CR00791013 end
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(getOnClickListener());
        textView.setTextColor(Color.WHITE);
        return textView;
    }

    protected abstract OnClickListener getOnClickListener();

}
