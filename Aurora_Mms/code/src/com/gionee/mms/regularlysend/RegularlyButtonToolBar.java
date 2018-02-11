package com.gionee.mms.regularlysend;

import com.android.mms.R;
import com.gionee.mms.slide.ViewToolbar;

import android.graphics.Color;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.R.integer;

public class RegularlyButtonToolBar extends ViewToolbar implements OnClickListener {

    private static final int STR_SET = R.string.date_time_set;
    private static final int STR_POI = R.string.gn_location_others_poi;
    private static final int STR_CANCER = R.string.gn_menu_delete;
    
    public static final int ACTION_SET    = 0;
    public static final int ACTION_POI    = 1;
    public static final int ACTION_CANCER = 2;

    public static final int FLAG_SET    = 0x00000001;
    public static final int FLAG_POI    = 0x00000010;
    public static final int FLAG_CANCER = 0x00000100;

    private int mFlag = FLAG_SET;

    private int mScreenX;
    private int mScreenY;
    private int mLineHeight = 0;

    private TextView mItemSet;
    private TextView mItemPoi;
    private TextView mItemCancel;

    protected TextView mTextView;

    private static RegularlyButtonToolBar sInstance = null;

    public RegularlyButtonToolBar(View hostView) {
        super(hostView);
        initToolbarItem(); 
    }

    /**
     * called to return singleton 
     * @param hostView
     * @return
     */
    public static RegularlyButtonToolBar getInstance(View hostView) {
        if(sInstance == null) {
            sInstance = new RegularlyButtonToolBar(hostView);
        }
        return sInstance;
    }

    /**
     * called to specify current text view for showing tool bar
     * @param obj current text view to specify
     */
    public void setCurrentTarget(TextView obj) {
        mTextView = obj;
    }

    protected void initToolbarItem() {
        mItemSet = initToolbarItem(ACTION_SET, STR_SET);
        mItemPoi = initToolbarItem(ACTION_POI, STR_POI);
        mItemCancel = initToolbarItem(ACTION_CANCER, STR_CANCER);
    }

    public void show() {
        if (!mShowing) {
            calculatePopupPosition();
            int start = mTextView.getSelectionStart();
            int end = mTextView.getSelectionEnd();
            showInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    public void move() {
        if (mShowing) {
            calculatePopupPosition();
            int start = mTextView.getSelectionStart();
            int end = mTextView.getSelectionEnd();
            moveInternal(mScreenX, mScreenY, mLineHeight, start != end);
        }
    }

    @Deprecated
    private void calculateScreenPosition() {
        int[] location = new int[2];
        mTextView.getLocationOnScreen(location);
        int start = mTextView.getSelectionStart();
        int end = mTextView.getSelectionEnd();
        Layout layout = mTextView.getLayout();
        if (layout == null ) {
            layout = mTextView.getLayout();
        }
        int line = layout.getLineForOffset(start);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineBottom(line);
        mLineHeight = bottom - top;
        mScreenY = top + mLineHeight / 2 + location[1] + mTextView.getTotalPaddingTop() - mTextView.getScrollY();
        if (start == end) {
            mScreenX = Math.round(layout.getPrimaryHorizontal(start)) + location[0] + mTextView.getTotalPaddingLeft() - mTextView.getScrollX();
        } else {
            int left = Math.round(layout.getPrimaryHorizontal(start));
            int right;
            int lineEnd = layout.getLineForOffset(end);
            if (line == lineEnd) {
                right = Math.round(layout.getPrimaryHorizontal(end));
            } else {
                right = Math.round(layout.getLineRight(line));
            }
            mScreenX = (left + right) / 2 + location[0] + mTextView.getTotalPaddingLeft() - mTextView.getScrollX();
        }
        mScreenY = Math.max(location[1], mScreenY);
    }

    private void calculatePopupPosition() {
        int[] location = new int[2];
        mTextView.getLocationOnScreen(location);

        final int x = location[0];
        final int y = location[1];

        final int height = mTextView.getMeasuredHeight();
        final int width  = mTextView.getMeasuredWidth();

        mScreenX = (width>>1) + x;// + mTextView.getTotalPaddingLeft() - mTextView.getScrollX();
        mScreenY = y;// + mTextView.getTotalPaddingTop() - mTextView.getScrollY();
    }

    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(this);
        return textView;
    }

    public void setOptionItemFlag(int flag) {
        mFlag = flag;
    }
    
    public int getOptionItemFlag() {
        return mFlag;
    }

    @Override
    protected void updateToolbarItems() {
        // TODO Auto-generated method stub
        
        mToolbarGroup.removeAllViews();
        // construct toolbar.
        if(mFlag == FLAG_SET) {
            mToolbarGroup.addView(mItemSet);
        } else if (mFlag == FLAG_POI){
            //mItemPoi.setBackgroundResource(R.drawable.gn_text_toolbar_left);
            mToolbarGroup.addView(mItemPoi);
        }
        mToolbarGroup.addView(mItemCancel);
    }

    public void setOnToolBarItemClickListener(ToolBarItemClickListener l) {
        mItemClickListener = l;
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        hide();
        if(mItemClickListener != null && mTextView != null) {
            mItemClickListener.onAcitonClick(mTextView,v.getId());
        }
    }

    private ToolBarItemClickListener mItemClickListener = null;
    public interface ToolBarItemClickListener{
        /**
         * callback to caller of this interface
         * @param targetView operation target view ,as current textview settled by caller
         * @param action action id of user cliked
         */
        void onAcitonClick(View targetView,int action);
    }
}
