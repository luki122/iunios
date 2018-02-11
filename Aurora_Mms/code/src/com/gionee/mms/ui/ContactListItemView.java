/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

//Gionee <zhouyj> <2013-04-17> add for CR00798863 start
import com.android.mms.MmsApp;
//Gionee <zhouyj> <2013-04-17> add for CR00798863 end
import com.android.mms.R;

/**
 * A custom view for an item in the contact list.
 */
public class ContactListItemView extends ViewGroup {

    private static final int QUICK_CONTACT_BADGE_STYLE =
            com.android.internal.R.attr.quickContactBadgeStyleWindowMedium;

    private final Context mContext;

    private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mPaddingTop;
    private final int mPaddingRight;
    private final int mPaddingBottom;
    private final int mPaddingLeft;
    private final int mPaddingLeftNoPhoto;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mCallButtonPadding;
    private final int mPresenceIconMargin;
    private final int mHeaderTextWidth;
    private boolean mHorizontalDividerVisible;
    private Drawable mHorizontalDividerDrawable;
    private int mHorizontalDividerHeight;
    private boolean mVerticalDividerVisible;
    private Drawable mVerticalDividerDrawable;
    private int mVerticalDividerWidth;

    private boolean mHeaderVisible;
    private Drawable mHeaderBackgroundDrawable;
    private int mHeaderBackgroundHeight;
    private TextView mHeaderTextView;

    private QuickContactBadge mQuickContact;
    private ImageView mPhotoView;
    private TextView mNameTextView;
    //private DontPressWithParentImageView mCallButton;
    private TextView mLabelView;
    private TextView mDataView;
    private TextView mSnippetView;
    private ImageView mPresenceIcon;

    private CheckBox mCheckBox;

    private int mPhotoViewWidth;
    private int mPhotoViewHeight;
    private int mLine1Height;
    private int mLine2Height;
    private int mLine3Height;

    private OnClickListener mCallButtonClickListener;
    
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
    private boolean mInContactFragment = false;
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 end

    public ContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // Obtain preferred item height from the current theme
        TypedArray a = context.obtainStyledAttributes(null, com.android.internal.R.styleable.Theme);
        mPreferredHeight =
                a.getDimensionPixelSize(android.R.styleable.Theme_listPreferredItemHeight, 0);
        a.recycle();

        Resources resources = context.getResources();
        mVerticalDividerMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_vertical_divider_margin);
        mPaddingTop =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_top);
        mPaddingBottom =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_bottom);
        mPaddingLeft =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_left);
        mPaddingLeftNoPhoto =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_right_nophoto);
        mPaddingRight =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_right);
        mGapBetweenImageAndText =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_image_and_text);
        mGapBetweenLabelAndData =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_label_and_data);
        mCallButtonPadding =
                resources.getDimensionPixelOffset(R.dimen.list_item_call_button_padding);
        mPresenceIconMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_presence_icon_margin);
        mHeaderTextWidth =
                resources.getDimensionPixelOffset(R.dimen.list_item_header_text_width);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        int width = resolveSize(0, widthMeasureSpec);
        int height = 0;

        mLine1Height = 0;
        mLine2Height = 0;
        mLine3Height = 0;

        // Obtain the natural dimensions of the name text (we only care about height)
        mNameTextView.measure(0, 0);

        mLine1Height = mNameTextView.getMeasuredHeight();

        if (isVisible(mLabelView)) {
            mLabelView.measure(0, 0);
            mLine2Height = mLabelView.getMeasuredHeight();
        }

        if (isVisible(mDataView)) {
            mDataView.measure(0, 0);
            mLine2Height = Math.max(mLine2Height, mDataView.getMeasuredHeight());
        }

        if (isVisible(mSnippetView)) {
            mSnippetView.measure(0, 0);
            mLine3Height = mSnippetView.getMeasuredHeight();
        }

        height += mLine1Height + mLine2Height + mLine3Height;

        if (isVisible(mPresenceIcon)) {
            mPresenceIcon.measure(0, 0);
        }

        ensurePhotoViewSize();

        height = Math.max(height, mPhotoViewHeight);
        height = Math.max(height, mPreferredHeight);

        if (mHeaderVisible) {
            ensureHeaderBackground();
            mHeaderTextView.measure(
                    MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            height += mHeaderBackgroundDrawable.getIntrinsicHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        int topBound = 0;

        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.setBounds(
                    0,
                    0,
                    width,
                    mHeaderBackgroundHeight);
            mHeaderTextView.layout(0, 0, width, mHeaderBackgroundHeight);
            topBound += mHeaderBackgroundHeight;
        }

        // Positions of views on the left are fixed and so are those on the right side.
        // The stretchable part of the layout is in the middle.  So, we will start off
        // by laying out the left and right sides. Then we will allocate the remainder
        // to the text fields in the middle.

        // Left side
        int leftBound = mPaddingLeft + 5;
        View photoView = mQuickContact != null ? mQuickContact : mPhotoView;
        if (photoView != null) {
            // Center the photo vertically
            int photoTop = topBound + (height - topBound - mPhotoViewHeight) / 2;
            photoView.layout(
                    leftBound,
                    photoTop,
                    leftBound + mPhotoViewWidth,
                    photoTop + mPhotoViewHeight);
            leftBound += mPhotoViewWidth + mGapBetweenImageAndText;
        } else {
            leftBound += mPaddingLeftNoPhoto;
        }

        // Right side
        int rightBound = right;
        mVerticalDividerVisible = false;

        if (isVisible(mPresenceIcon)) {
            int iconWidth = mPresenceIcon.getMeasuredWidth();
            rightBound -= mPresenceIconMargin + iconWidth;
            mPresenceIcon.layout(
                    rightBound,
                    topBound,
                    rightBound + iconWidth,
                    height);
        }

        if (mHorizontalDividerVisible) {
            ensureHorizontalDivider();
            mHorizontalDividerDrawable.setBounds(
                    0,
                    height - mHorizontalDividerHeight,
                    width,
                    height);
        }

        topBound += mPaddingTop;
        int bottomBound = height - mPaddingBottom;

        // Text lines, centered vertically
        rightBound -= (mPaddingRight + mPhotoViewWidth);

        // Center text vertically
        int totalTextHeight = mLine1Height + mLine2Height + mLine3Height;
        int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;

        mNameTextView.layout(leftBound,
                textTopBound,
                rightBound,
                textTopBound + mLine1Height);

        int dataLeftBound = leftBound;
        if (isVisible(mLabelView)) {
            dataLeftBound = leftBound + mLabelView.getMeasuredWidth();
            mLabelView.layout(leftBound,
                    textTopBound + mLine1Height,
                    dataLeftBound,
                    textTopBound + mLine1Height + mLine2Height);
            dataLeftBound += mGapBetweenLabelAndData;
        }

        if (isVisible(mDataView)) {
            mDataView.layout(dataLeftBound,
                    textTopBound + mLine1Height,
                    rightBound,
                    textTopBound + mLine1Height + mLine2Height);
        }

        if (isVisible(mSnippetView)) {
            mSnippetView.layout(leftBound,
                    textTopBound + mLine1Height + mLine2Height,
                    rightBound,
                    textTopBound + mLine1Height + mLine2Height + mLine3Height);
        }

        topBound -= mPaddingTop;

        if (isVisible(mCheckBox)) {
            // Center the type vertically
            int nTypeTop = topBound + (height - topBound - mPhotoViewHeight) / 2;
            // gionee zhouyj 2012-06-05 modify for CR00616296 start
            //Gionee <zhouyj> <2013-04-25> modify for CR00796299 start
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
            if (MmsApp.mGnAlphbetIndexSupport && mInContactFragment) {
                rightBound -= 50;
            } else {
                rightBound -= 10;
            }
            //Gionee <zhouyj> <2013-04-17> add for CR00798863 end
            //Gionee <zhouyj> <2013-04-25> modify for CR00796299 end
            mCheckBox.layout(rightBound + 38,
                    nTypeTop,
                    rightBound + mPhotoViewWidth + 28,
                    nTypeTop + mPhotoViewHeight);
            // gionee zhouyj 2012-06-05 modify for CR00616296 end
        }
    }

    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * Loads the drawable for the horizontal divider if it has not yet been loaded.
     */
    private void ensureHorizontalDivider() {
        if (mHorizontalDividerDrawable == null) {
            //gionee gaoj 2012-5-18 modified for CR00601632 start
            mHorizontalDividerDrawable = mContext.getResources().getDrawable(
                    -1/*R.drawable.gn_expandlist_line*/);
            //gionee gaoj 2012-5-18 modified for CR00601632 end
            mHorizontalDividerHeight = mHorizontalDividerDrawable.getIntrinsicHeight();
        }
    }

    /**
     * Loads the drawable for the header background if it has not yet been loaded.
     */
    private void ensureHeaderBackground() {
        if (mHeaderBackgroundDrawable == null) {
//            mHeaderBackgroundDrawable = mContext.getResources().getDrawable(
//                    android.R.drawable.dark_header);
            mHeaderBackgroundHeight = mHeaderBackgroundDrawable.getIntrinsicHeight();
        }
    }

    /**
     * Extracts width and height from the style
     */
    private void ensurePhotoViewSize() {
        if (mPhotoViewWidth == 0 && mPhotoViewHeight == 0) {
            TypedArray a = mContext.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.ViewGroup_Layout,
                    QUICK_CONTACT_BADGE_STYLE, 0);
            mPhotoViewWidth = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mPhotoViewHeight = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.draw(canvas);
        }
        if (mHorizontalDividerVisible) {
            mHorizontalDividerDrawable.draw(canvas);
        }
        if (mVerticalDividerVisible) {
            mVerticalDividerDrawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * Sets the flag that determines whether a divider should drawn at the bottom
     * of the view.
     */
    public void setDividerVisible(boolean visible) {
        mHorizontalDividerVisible = visible;
    }

    /**
     * Sets section header or makes it invisible if the title is null.
     */
    public void setSectionHeader(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mHeaderTextView == null) {
                mHeaderTextView = new TextView(mContext);
                mHeaderTextView.setTypeface(mHeaderTextView.getTypeface(), Typeface.BOLD);
//                mHeaderTextView.setTextColor(mContext.getResources()
//                        .getColor(com.android.internal.R.color.dim_foreground_dark));
                mHeaderTextView.setTextColor(Color.WHITE);
                mHeaderTextView.setTextSize(14);
                mHeaderTextView.setGravity(Gravity.CENTER);
                addView(mHeaderTextView);
            }
            mHeaderTextView.setText(title);
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderVisible = true;
        } else {
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.GONE);
            }
            mHeaderVisible = false;
        }
    }

    /**
     * Returns the quick contact badge, creating it if necessary.
     */
    public QuickContactBadge getQuickContact() {
        if (mQuickContact == null) {
            mQuickContact = new QuickContactBadge(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            mQuickContact.setExcludeMimes(new String[] { Contacts.CONTENT_ITEM_TYPE });
            addView(mQuickContact);
        }
        return mQuickContact;
    }

    /**
     * Returns the photo view, creating it if necessary.
     */
    public ImageView getPhotoView() {
        if (mPhotoView == null) {
            mPhotoView = new ImageView(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            // Quick contact style used above will set a background - remove it
            mPhotoView.setBackgroundDrawable(null);
            addView(mPhotoView);
        }
        return mPhotoView;
    }

    /**
     * Returns the text view for the contact name, creating it if necessary.
     */
    public TextView getNameTextView() {
        if (mNameTextView == null) {
            mNameTextView = new GnTextView(mContext);
            mNameTextView.setSingleLine(true);
            mNameTextView.setEllipsize(TruncateAt.MARQUEE);
            mNameTextView.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
            mNameTextView.setGravity(Gravity.CENTER_VERTICAL);
            mNameTextView.setPadding(0, 0, 0, 5);
            //gionee gaoj 2012-5-18 modified for CR00601632 start
//            mNameTextView.setTextColor(Color.WHITE);
            //gionee gaoj 2012-5-18 modified for CR00601632 end
            addView(mNameTextView);
        }
        return mNameTextView;
    }


    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(char[] text, int size) {
        if (text == null || size == 0) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text, 0, size);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data label, creating it if necessary.
     */
    public TextView getLabelView() {
        if (mLabelView == null) {
            mLabelView = new TextView(mContext);
            mLabelView.setSingleLine(true);
            mLabelView.setEllipsize(TruncateAt.MARQUEE);
            mLabelView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mLabelView.setTypeface(mLabelView.getTypeface(), Typeface.BOLD);
            addView(mLabelView);
        }
        return mLabelView;
    }

    /**
     * Adds or updates a text view for the data element.
     */
    public void setData(char[] text, int size) {
        if (text == null || size == 0) {
            if (mDataView != null) {
                mDataView.setVisibility(View.GONE);
            }
            return;
        } else {
            getDataView();
            mDataView.setText(text, 0, size);
            mDataView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data text, creating it if necessary.
     */
    public TextView getDataView() {
        if (mDataView == null) {
            mDataView = new TextView(mContext);
            mDataView.setSingleLine(true);
            mDataView.setEllipsize(TruncateAt.MARQUEE);
            mDataView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mDataView.setGravity(Gravity.CENTER_VERTICAL);
            //gionee gaoj 2012-5-18 modified for CR00601632 start
//            mDataView.setTextColor(Color.WHITE);
            //gionee gaoj 2012-5-18 modified for CR00601632 end
            addView(mDataView);
        }
        return mDataView;
    }

    /**
     * Adds or updates a text view for the search snippet.
     */
    public void setSnippet(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mSnippetView != null) {
                mSnippetView.setVisibility(View.GONE);
            }
        } else {
            getSnippetView();
            mSnippetView.setText(text);
            mSnippetView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the search snippet, creating it if necessary.
     */
    public TextView getSnippetView() {
        if (mSnippetView == null) {
            mSnippetView = new TextView(mContext);
            mSnippetView.setSingleLine(true);
            mSnippetView.setEllipsize(TruncateAt.MARQUEE);
            mSnippetView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mSnippetView.setTypeface(mSnippetView.getTypeface(), Typeface.BOLD);
            addView(mSnippetView);
        }
        return mSnippetView;
    }

    /**
     * Adds or updates the presence icon view.
     */
    public void setPresence(Drawable icon) {
        if (icon != null) {
            if (mPresenceIcon == null) {
                mPresenceIcon = new ImageView(mContext);
                addView(mPresenceIcon);
            }
            mPresenceIcon.setImageDrawable(icon);
            mPresenceIcon.setScaleType(ScaleType.CENTER);
            mPresenceIcon.setVisibility(View.VISIBLE);
        } else {
            if (mPresenceIcon != null) {
                mPresenceIcon.setVisibility(View.GONE);
            }
        }
    }

    public CheckBox getCheckBox() {
        if (mCheckBox == null) {
            mCheckBox = new CheckBox(mContext);
            addView(mCheckBox);
        }
        mCheckBox.setFocusable(false);
        mCheckBox.setFocusableInTouchMode(false);
        mCheckBox.setClickable(false);
        mCheckBox.setTextColor(Color.WHITE);
        return mCheckBox;
    }
    
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 start
    public void setInContactFragment(boolean value) {
        mInContactFragment = value;
    }
    //Gionee <zhouyj> <2013-04-17> add for CR00798863 end

}
