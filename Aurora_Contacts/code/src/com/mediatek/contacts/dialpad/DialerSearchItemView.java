package com.mediatek.contacts.dialpad;

import com.android.contacts.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class DialerSearchItemView extends FrameLayout {

    private static final String TAG = "DialerSearchListItem";
    private static final boolean DBG = true;
    private static  int DialerSearchItemViewHeight;

    protected QuickContactBadge mQuickContactBadge;
    protected TextView mName;
    protected TextView mLabelAndNumber;
    protected ImageView mCallType;
    protected TextView mOperator;
    protected TextView mDate;
    protected View mDivider;
    protected ImageButton mCall;

    private static int mListItemQuickContactPaddingTop;   // get from @dimen/calllog_list_item_quick_contact_padding_top
    private static int mListItemQuickContactPaddingBottom;// get from @dimen/calllog_list_item_quick_contact_padding_bottom

    public DialerSearchItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //use the same padding value as call log list item
        DialerSearchItemViewHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.dialer_search_item_view_initial_height);
        mListItemQuickContactPaddingTop = mContext.getResources().getDimensionPixelSize(
				R.dimen.calllog_list_item_quick_contact_padding_top);
        mListItemQuickContactPaddingBottom= mContext.getResources().getDimensionPixelSize(
				R.dimen.calllog_list_item_quick_contact_padding_bottom);
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    protected void onFinishInflate() {
        mQuickContactBadge = (QuickContactBadge) findViewById(R.id.quick_contact_photo);
        mLabelAndNumber = (TextView) findViewById(R.id.labelAndNumber);
        mName = (TextView) findViewById(R.id.name);
        mCallType = (ImageView) findViewById(R.id.callType);
        mOperator = (TextView) findViewById(R.id.operator);
        mDate = (TextView) findViewById(R.id.date);
        mDivider = findViewById(R.id.divider);
        mCall = (ImageButton) findViewById(R.id.call);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(DBG) log("changed = "+changed+" left = "+left+" top = "+top+" right = "+right+" bottom = "+bottom);

        final int parentLeft = mPaddingLeft;
        final int parentRight = right - left - mPaddingRight;

        final int parentTop = mListItemQuickContactPaddingTop;
        final int parentBottom = bottom - top - mListItemQuickContactPaddingBottom ;
        if(DBG) log("mPaddingTop = "+mPaddingTop+" mPaddingBottom = "+mPaddingBottom+" mPaddingLeft = "+mPaddingLeft+" mPaddingRight = "+mPaddingRight);
        if(DBG) log("parentTop = "+parentTop+" parentBottom = "+parentBottom);

        int width;
        int height;
        int childLeft = left;
        int childTop = top;

        int labelTop;

        int HeightFirstLine = 0;   
        int HeightSecondLine = 0;	
        int HeightThirdLine = 0;   
        int MaxThreeLineHeight = 0;

        FrameLayout.LayoutParams lp;
		
        // QuickContactBadge
        // align left and center vertical
        width = mQuickContactBadge.getMeasuredWidth();
        height = mQuickContactBadge.getMeasuredHeight();
        childTop = parentTop + (parentBottom - parentTop - height) / 2;
        childLeft = parentLeft;
        mQuickContactBadge.layout(childLeft, childTop, childLeft + width, childTop + height);
        final int quickContactBadgeRight = childLeft + width; 

        //Math.max(DialerSearchItemViewHeight, height + mPaddingTop + mPaddingBottom )
        DialerSearchItemViewHeight = height + mListItemQuickContactPaddingTop + mListItemQuickContactPaddingBottom;

        // Call button
        // align right
        //CR:ALPS00245549 modify call button width to cover right start
        //width = mCall.getMeasuredWidth();
        width = mCall.getMeasuredWidth() + mPaddingRight;
        //CR:ALPS00245549 modify call button width to cover right end
        height = mCall.getMeasuredHeight();
        //childTop = parentTop - mListItemQuickContactPaddingTop;
        childTop = (DialerSearchItemViewHeight - height)/2; 
        childLeft = parentRight - width;

        mCall.layout(childLeft, childTop, childLeft + width, childTop + height);

        // Divider
        // left of Call button and center vertical
        width = mDivider.getMeasuredWidth();
        height = mDivider.getMeasuredHeight();
        childTop = parentTop + (parentBottom - parentTop - height) / 2;
        childLeft = childLeft - width;
        mDivider.layout(childLeft, childTop, childLeft + width, childTop + height);

        // name
        // align top and right to mQuickContactBadge
        width = mName.getMeasuredWidth();
        height = mName.getMeasuredHeight();
        lp = (FrameLayout.LayoutParams) mName.getLayoutParams();
        childTop = parentTop;
        childLeft = quickContactBadgeRight + lp.leftMargin;
        mName.layout(childLeft, childTop, childLeft + width, childTop + height);
        final int nameBottom = childTop + height;
        HeightFirstLine  = height  ;
        MaxThreeLineHeight = MaxThreeLineHeight + HeightFirstLine;

        final boolean callog = mCallType.getVisibility() == View.VISIBLE;

        // mCallType is visible, it's a call log item
        if(callog) {
            // Call type
            // align parent bottom and right to QuickContactBadge
            width = mCallType.getMeasuredWidth();
            height = mCallType.getMeasuredHeight();
            lp = (FrameLayout.LayoutParams) mCallType.getLayoutParams();
            childTop = parentBottom  - height ;
            childLeft = quickContactBadgeRight + lp.leftMargin;
            mCallType.layout(childLeft, childTop, childLeft + width, childTop + height);
            final int callTypeRight = childLeft + width;
	        HeightThirdLine =   Math.max(HeightThirdLine, height  ) ;

            // Operator ( sim indicator )
            // align parent bottom and right to Call type
            width = mOperator.getMeasuredWidth();
            height = mOperator.getMeasuredHeight();
            lp = (FrameLayout.LayoutParams) mOperator.getLayoutParams();
            childTop = parentBottom  - height;
			
            childLeft = callTypeRight + lp.leftMargin;
            mOperator.layout(childLeft, childTop, childLeft + width, childTop + height);
            final int operatorRight = childLeft + width;
            HeightThirdLine = Math.max(HeightThirdLine, height );

            // Date
            width = mDate.getMeasuredWidth();
            height = mDate.getMeasuredHeight();
            lp = (FrameLayout.LayoutParams) mDate.getLayoutParams();
            childTop = parentBottom  - height ;
            childLeft = operatorRight + lp.leftMargin;
            mDate.layout(childLeft, childTop, childLeft + width, childTop + height);
            HeightThirdLine = Math.max(HeightThirdLine, height  );
            MaxThreeLineHeight = MaxThreeLineHeight + HeightThirdLine;
        }

        // label and number
        int labelRight = quickContactBadgeRight;
        if(mLabelAndNumber.getVisibility() == View.VISIBLE) {
            width = mLabelAndNumber.getMeasuredWidth();
            height = mLabelAndNumber.getMeasuredHeight();
            lp = (FrameLayout.LayoutParams) mLabelAndNumber.getLayoutParams();
            if(callog) {
                childTop = nameBottom ;
            } else
                childTop = parentBottom - height  ;
            childLeft = quickContactBadgeRight + lp.leftMargin;
            labelRight = childLeft + width;
            mLabelAndNumber.layout(childLeft, childTop, childLeft + width, childTop + height);
            HeightSecondLine =  Math.max(HeightSecondLine, height ) ;
        }

        MaxThreeLineHeight = MaxThreeLineHeight + HeightSecondLine;
        //if 2nd line or 3rd line is null, 2 and 3 can't be both null
        /*
        if(HeightThirdLine == 0) {
            //2nd line null , add height same as 3rd line
            MaxThreeLineHeight = MaxThreeLineHeight + HeightSecondLine; 
            if(DBG) log("(HeightThirdLine == 0)");
        }
        else if(HeightSecondLine == 0) {
            // 3rd line null,add 2nd line height
            MaxThreeLineHeight = MaxThreeLineHeight + HeightThirdLine;
            if(DBG) log("(HeightSecondLine == 0)");
        }*/
        DialerSearchItemViewHeight = Math.max(MaxThreeLineHeight, DialerSearchItemViewHeight );     
    }

    // @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int measuredWidth = getSuggestedMinimumWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension( resolveSizeAndState(measuredWidth, widthMeasureSpec, 0), DialerSearchItemViewHeight);
    }
}
