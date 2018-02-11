/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.list;

import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactStatusUtil;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.format.PrefixHighlighter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import android.widget.CheckBox;

import com.android.contacts.widget.GnTextView;
import aurora.widget.AuroraCheckBox;
import com.aurora.utils.DensityUtil;
import aurora.widget.AuroraTextView;

/**
 * A custom view for an item in the contact list.
 * The view contains the contact's photo, a set of text views (for name, status, etc...) and
 * icons for presence and call.
 * The view uses no XML file for layout and all the measurements and layouts are done
 * in the onMeasure and onLayout methods.
 *
 * The layout puts the contact's photo on the right side of the view, the call icon (if present)
 * to the left of the photo, the text lines are aligned to the left and the presence icon (if
 * present) is set to the left of the status line.
 *
 * The layout also supports a header (used as a header of a group of contacts) that is above the
 * contact's data and a divider between contact view.
 */

public class ContactListItemView extends ViewGroup
        implements SelectionBoundsAdjuster {

    private static final int QUICK_CONTACT_BADGE_STYLE =
            com.android.internal.R.attr.quickContactBadgeStyleWindowMedium;

    protected final Context mContext;

    // Style values for layout and appearance
    private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mCallButtonPadding;
    private final int mPresenceIconMargin;
    private final int mPresenceIconSize;
    private final int mHeaderTextColor;
    private final int mHeaderTextIndent;
    private final int mHeaderTextSize;
    private final int mHeaderUnderlineHeight;
    private final int mHeaderUnderlineColor;
    private final int mCountViewTextSize;
    private final int mContactsCountTextColor;
    private final int mTextIndent;
    private Drawable mActivatedBackgroundDrawable;

    /**
     * Used with {@link #mLabelView}, specifying the width ratio between label and data.
     */
    private final int mLabelViewWidthWeight;
    /**
     * Used with {@link #mDataView}, specifying the width ratio between label and data.
     */
    private final int mDataViewWidthWeight;

    // Will be used with adjustListItemSelectionBounds().
    private int mSelectionBoundsMarginLeft;
    private int mSelectionBoundsMarginRight;

    // Horizontal divider between contact views.
    private boolean mHorizontalDividerVisible = false; // aurora <wangth> <2013-12-10> modify for aurora
    private Drawable mHorizontalDividerDrawable;
    private int mHorizontalDividerHeight;

    /**
     * Where to put contact photo. This affects the other Views' layout or look-and-feel.
     */
    public enum PhotoPosition {
        LEFT,
        RIGHT
    }
    public static final PhotoPosition DEFAULT_PHOTO_POSITION = 
    	(ContactsApplication.sIsGnContactsSupport) ? PhotoPosition.LEFT : PhotoPosition.RIGHT; 

    private PhotoPosition mPhotoPosition = DEFAULT_PHOTO_POSITION;

    // Vertical divider between the call icon and the text.
    private boolean mVerticalDividerVisible;
    private Drawable mVerticalDividerDrawable;
    private int mVerticalDividerWidth;

    // Header layout data
    private boolean mHeaderVisible;
    private View mHeaderDivider;
    private int mHeaderBackgroundHeight;
    private TextView mHeaderTextView;

    // The views inside the contact view
    private boolean mQuickContactEnabled = true;
    private QuickContactBadge mQuickContact;
    private ImageView mPhotoView;
    public AuroraTextView mNameTextView;
    private TextView mPhoneticNameTextView;
    private DontPressWithParentImageView mCallButton;
    private TextView mLabelView;
    public TextView mDataView;
    public TextView mSnippetView;
    private TextView mStatusView;
    private TextView mCountView;
    private ImageView mPresenceIcon;
    
    // aurora <wangth> <2014-04-15> add for aurora begin
    private ImageView mAuroraSimIcon;
    // aurora <wangth> <2014-04-15> add for aurora end

    private ColorStateList mSecondaryTextColor;

    private char[] mHighlightedPrefix;

    private int mDefaultPhotoViewSize;
    /**
     * Can be effective even when {@link #mPhotoView} is null, as we want to have horizontal padding
     * to align other data in this View.
     */
    private int mPhotoViewWidth;
    /**
     * Can be effective even when {@link #mPhotoView} is null, as we want to have vertical padding.
     */
    private int mPhotoViewHeight;

    /**
     * Only effective when {@link #mPhotoView} is null.
     * When true all the Views on the right side of the photo should have horizontal padding on
     * those left assuming there is a photo.
     */
    private boolean mKeepHorizontalPaddingForPhotoView;
    /**
     * Only effective when {@link #mPhotoView} is null.
     */
    private boolean mKeepVerticalPaddingForPhotoView;

    /**
     * True when {@link #mPhotoViewWidth} and {@link #mPhotoViewHeight} are ready for being used.
     * False indicates those values should be updated before being used in position calculation.
     */
    private boolean mPhotoViewWidthAndHeightAreReady = false;

    private int mNameTextViewHeight;
    private int mPhoneticNameTextViewHeight;
    private int mLabelViewHeight;
    private int mDataViewHeight;
    private int mSnippetTextViewHeight;
    private int mStatusTextViewHeight;

    // Holds Math.max(mLabelTextViewHeight, mDataViewHeight), assuming Label and Data share the
    // same row.
    private int mLabelAndDataViewMaxHeight;

    private OnClickListener mCallButtonClickListener;
    // TODO: some TextView fields are using CharArrayBuffer while some are not. Determine which is
    // more efficient for each case or in general, and simplify the whole implementation.
    // Note: if we're sure MARQUEE will be used every time, there's no reason to use
    // CharArrayBuffer, since MARQUEE requires Span and thus we need to copy characters inside the
    // buffer to Spannable once, while CharArrayBuffer is for directly applying char array to
    // TextView without any modification.
    private final CharArrayBuffer mDataBuffer = new CharArrayBuffer(128);
    private final CharArrayBuffer mPhoneticNameBuffer = new CharArrayBuffer(128);

    private boolean mActivatedStateSupported;

    private Rect mBoundsWithoutHeader = new Rect();

    /** A helper used to highlight a prefix in a text field. */
    private PrefixHighlighter mPrefixHighligher;
    private CharSequence mUnknownNameText;
    
    //aurora <wangth> <2013-9-23> add for auroro ui begin
    private boolean mDoubleRowMode = false;
    private int mMaxNameWidth = 0;
    //aurora <wangth> <2013-9-23> add for auroro ui end

    /**
     * Special class to allow the parent to be pressed without being pressed itself.
     * This way the line of a tab can be pressed, but the image itself is not.
     */
    // TODO: understand this
    private static class DontPressWithParentImageView extends ImageView {

        public DontPressWithParentImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void setPressed(boolean pressed) {
            // If the parent is pressed, do not set to pressed.
            if (pressed && ((View) getParent()).isPressed()) {
                return;
            }
            super.setPressed(pressed);
        }
    }

    public ContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // Read all style values
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContactListItemView);
        //aurora <wangth> <2013-9-29> modify for auroro ui begin
        /*
        mPreferredHeight = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_height, 0);
        */
        mPreferredHeight = getContext().getResources().
                getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);
//        		getDimensionPixelOffset(R.dimen.aurora_two_lines_list_view_item_height);
        //aurora <wangth> <2013-9-29> modify for auroro ui end
        mActivatedBackgroundDrawable = a.getDrawable(
                R.styleable.ContactListItemView_activated_background);
        //aurora <ukiliu> <2013-11-4> modify for auroro ui begin
        mHorizontalDividerDrawable = getContext().getResources().getDrawable(R.drawable.h_diver);
//        		a.getDrawable(
//                R.styleable.ContactListItemView_list_item_divider);
        //aurora <ukiliu> <2013-11-4> modify for auroro ui end
        mVerticalDividerMargin = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_vertical_divider_margin, 0);
        mGapBetweenImageAndText = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_gap_between_image_and_text, 0);
        mGapBetweenLabelAndData = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_gap_between_label_and_data, 0);
        mCallButtonPadding = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_call_button_padding, 0);
        mPresenceIconMargin = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_presence_icon_margin, 4);
        mPresenceIconSize = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_presence_icon_size, 16);
        mDefaultPhotoViewSize = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_photo_size, 0);
        mHeaderTextIndent = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_header_text_indent, 0);
       	mHeaderTextColor = a.getColor(
                R.styleable.ContactListItemView_list_item_header_text_color, Color.BLACK); 
       	//aurora <wangth> <2013-9-4> modify for auroro ui begin
       	/*
        mHeaderTextSize = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_header_text_size, 12);
        mHeaderBackgroundHeight = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_header_height, 30);
        */
        mHeaderTextSize = getContext().getResources().
                getDimensionPixelOffset(R.dimen.aurora_contact_list_header_text_size);
        mHeaderBackgroundHeight = getContext().getResources().
                getDimensionPixelOffset(R.dimen.aurora_add_button_margin_top);
        mMaxNameWidth = getContext().getResources().
                getDimensionPixelOffset(R.dimen.aurora_contact_max_name_width);
        //aurora <wangth> <2013-9-4> modify for auroro ui end
        mHeaderUnderlineHeight = 0/*a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_header_underline_height, 1)*/;
        //Gionee <huangzy> <2013-06-03> modify for CR00786443 begin
        /*mHeaderUnderlineColor = a.getColor(
                R.styleable.ContactListItemView_list_item_header_underline_color, 0);*/
        mHeaderUnderlineColor = getContext().getResources().getColor(R.color.gn_people_app_theme_color);
        //Gionee <huangzy> <2013-06-03> modify for CR00786443 end        
        mTextIndent = a.getDimensionPixelOffset(
                R.styleable.ContactListItemView_list_item_text_indent, 0);
        mCountViewTextSize = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_contacts_count_text_size, 12);
        mContactsCountTextColor = a.getColor(
                R.styleable.ContactListItemView_list_item_contacts_count_text_color, Color.BLACK);
        mDataViewWidthWeight = a.getInteger(
                R.styleable.ContactListItemView_list_item_data_width_weight, 5);
        mLabelViewWidthWeight = a.getInteger(
                R.styleable.ContactListItemView_list_item_label_width_weight, 3);

        setPadding(
                a.getDimensionPixelOffset(
                        R.styleable.ContactListItemView_list_item_padding_left, 0),
                a.getDimensionPixelOffset(
                        R.styleable.ContactListItemView_list_item_padding_top, 0),
                a.getDimensionPixelOffset(
                        R.styleable.ContactListItemView_list_item_padding_right, 0),
                a.getDimensionPixelOffset(
                        R.styleable.ContactListItemView_list_item_padding_bottom, 0));

        //Gionee:huangzy 20130319 modify for CR00786443 start
        mPrefixHighligher = new PrefixHighlighter(
                a.getColor(R.styleable.ContactListItemView_list_item_prefix_highlight_color, 
                		Color.GREEN));
        //Gionee:huangzy 20130319 modify for CR00786443 end
        a.recycle();

        a = getContext().obtainStyledAttributes(android.R.styleable.Theme);
        mSecondaryTextColor = a.getColorStateList(android.R.styleable.Theme_textColorSecondary);
        a.recycle();

        //mHorizontalDividerHeight = mHorizontalDividerDrawable.getIntrinsicHeight();
        mHorizontalDividerHeight = getContext().getResources().
                getDimensionPixelOffset(R.dimen.aurora_list_item_driver_hight);

        if (mActivatedBackgroundDrawable != null) {
            mActivatedBackgroundDrawable.setCallback(this);
        }
    }

    /**
     * Installs a call button listener.
     */
    public void setOnCallButtonClickListener(OnClickListener callButtonClickListener) {
        mCallButtonClickListener = callButtonClickListener;
    }

    public void setUnknownNameText(CharSequence unknownNameText) {
        mUnknownNameText = unknownNameText;
    }

    public void setQuickContactEnabled(boolean flag) {
        mQuickContactEnabled = flag;
    }
    
    //aurora <wangth> <2013-9-23> add for aurora ui begin 
    public void setDoubleRow(boolean flag) {
        mDoubleRowMode = flag;
    }
    //aurora <wangth> <2013-9-23> add for aurora ui end

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        final int specWidth = resolveSize(0, widthMeasureSpec);
        final int preferredHeight;
        if (mHorizontalDividerVisible) {
            preferredHeight = mPreferredHeight + mHorizontalDividerHeight;
        } else {
            preferredHeight = mPreferredHeight;
        }

        mNameTextViewHeight = 0;
        mPhoneticNameTextViewHeight = 0;
        mLabelViewHeight = 0;
        mDataViewHeight = 0;
        mLabelAndDataViewMaxHeight = 0;
        mSnippetTextViewHeight = 0;
        mStatusTextViewHeight = 0;

        /*
         * New Feature by Mediatek Inc Begin.
         * Description: Add the check-box support for ContactListItemView.
         */
        measureCheckBox();
        /*
         * New Feature by Mediatek Inc End.
         * Description: Add the check-box support for ContactListItemView.
         */
        
        measureAuroraSimIcon();

        ensurePhotoViewSize();

        // Width each TextView is able to use.
        int effectiveWidth;
        // All the other Views will honor the photo, so available width for them may be shrunk.
        if (mPhotoViewWidth > 0 || mKeepHorizontalPaddingForPhotoView) {
            effectiveWidth = specWidth - getPaddingLeft() - getPaddingRight()
                    - (mPhotoViewWidth + mGapBetweenImageAndText);
        } else {
            effectiveWidth = specWidth - getPaddingLeft() - getPaddingRight();
        }

        // Go over all visible text views and measure actual width of each of them.
        // Also calculate their heights to get the total height for this entire view.

        effectiveWidth = effectiveWidth - 230;
        
        if (isVisible(mNameTextView)) {
            mNameTextView.measure(
                    MeasureSpec.makeMeasureSpec(effectiveWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mNameTextViewHeight = mNameTextView.getMeasuredHeight();
        }

        if (isVisible(mPhoneticNameTextView)) {
            mPhoneticNameTextView.measure(
                    MeasureSpec.makeMeasureSpec(effectiveWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mPhoneticNameTextViewHeight = mPhoneticNameTextView.getMeasuredHeight();
        }

        // If both data (phone number/email address) and label (type like "MOBILE") are quite long,
        // we should ellipsize both using appropriate ratio.
        final int dataWidth;
        final int labelWidth;
        if (isVisible(mDataView)) {
            if (isVisible(mLabelView)) {
                final int totalWidth = effectiveWidth - mGapBetweenLabelAndData;
                dataWidth = ((totalWidth * mDataViewWidthWeight)
                        / (mDataViewWidthWeight + mLabelViewWidthWeight));
                labelWidth = ((totalWidth * mLabelViewWidthWeight) /
                        (mDataViewWidthWeight + mLabelViewWidthWeight));
            } else {
                dataWidth = effectiveWidth;
                labelWidth = 0;
            }
        } else {
            dataWidth = 0;
            if (isVisible(mLabelView)) {
                labelWidth = effectiveWidth;
            } else {
                labelWidth = 0;
            }
        }

        if (isVisible(mDataView)) {
            mDataView.measure(MeasureSpec.makeMeasureSpec(dataWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mDataViewHeight = mDataView.getMeasuredHeight();
        }

        if (isVisible(mLabelView)) {
            // For performance reason we don't want AT_MOST usually, but when the picture is
            // on right, we need to use it anyway because mDataView is next to mLabelView.
            final int mode = (mPhotoPosition == PhotoPosition.LEFT
                    ? MeasureSpec.EXACTLY : MeasureSpec.AT_MOST);
            mLabelView.measure(MeasureSpec.makeMeasureSpec(labelWidth, mode),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mLabelViewHeight = mLabelView.getMeasuredHeight();
        }
        mLabelAndDataViewMaxHeight = Math.max(mLabelViewHeight, mDataViewHeight);

        if (isVisible(mSnippetView)) {
            mSnippetView.measure(
                    MeasureSpec.makeMeasureSpec(effectiveWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mSnippetTextViewHeight = mSnippetView.getMeasuredHeight();
        }

        // Status view height is the biggest of the text view and the presence icon
        if (isVisible(mPresenceIcon)) {
            mPresenceIcon.measure(mPresenceIconSize, mPresenceIconSize);
            mStatusTextViewHeight = mPresenceIcon.getMeasuredHeight();
        }

        if (isVisible(mStatusView)) {
            // Presence and status are in a same row, so status will be affected by icon size.
            final int statusWidth;
            if (isVisible(mPresenceIcon)) {
                statusWidth = (effectiveWidth - mPresenceIcon.getMeasuredWidth()
                        - mPresenceIconMargin);
            } else {
                statusWidth = effectiveWidth;
            }
            mStatusView.measure(MeasureSpec.makeMeasureSpec(statusWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mStatusTextViewHeight =
                    Math.max(mStatusTextViewHeight, mStatusView.getMeasuredHeight());
        }

        // Calculate height including padding.
        int height = (mNameTextViewHeight + mPhoneticNameTextViewHeight +
                mLabelAndDataViewMaxHeight +
                mSnippetTextViewHeight + mStatusTextViewHeight);

        if (isVisible(mCallButton)) {
            mCallButton.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }

        // Make sure the height is at least as high as the photo
        height = Math.max(height, mPhotoViewHeight + getPaddingBottom() + getPaddingTop());

        // Add horizontal divider height
        if (mHorizontalDividerVisible) {
            height += mHorizontalDividerHeight;
        }

        // Make sure height is at least the preferred height
        height = Math.max(height, preferredHeight);
        
        //aurora <wangth> <2013-9-16> add for auroro ui begin
        if (!isVisible(mSnippetView) && !mDoubleRowMode) {
//            height = mContext.getResources().getDimensionPixelSize(R.dimen.aurora_single_line_list_view_item_hight);
        	height = mContext.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_list_singleline_height);
        }
        //aurora <wangth> <2013-9-16> add for auroro ui end

        // Add the height of the header if visible
        if (mHeaderVisible) {
            mHeaderTextView.measure(
                    MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            if (mCountView != null) {
                mCountView.measure(
                        MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            }
            mHeaderBackgroundHeight = Math.max(mHeaderBackgroundHeight,
                    mHeaderTextView.getMeasuredHeight());
            height += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
        }

        setMeasuredDimension(specWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int height = bottom - top;
        final int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        int topBound = 0;
        int bottomBound = height;
        int leftBound = getPaddingLeft();
        int rightBound = width - getPaddingRight();

        // Put the header in the top of the contact view (Text + underline view)
        if (mHeaderVisible) {
            mHeaderTextView.layout(leftBound + mHeaderTextIndent,
                    0,
                    rightBound,
                    mHeaderBackgroundHeight);
            if (mCountView != null) {
                mCountView.layout(rightBound - mCountView.getMeasuredWidth(),
                        0,
                        rightBound,
                        mHeaderBackgroundHeight);
            }
            mHeaderDivider.layout(leftBound,
                    mHeaderBackgroundHeight,
                    rightBound,
                    mHeaderBackgroundHeight + mHeaderUnderlineHeight);
            topBound += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
        }

        // Put horizontal divider at the bottom
        if (mHorizontalDividerVisible) {
            mHorizontalDividerDrawable.setBounds(
                    leftBound,
                    height - mHorizontalDividerHeight,
                    rightBound,
                    height);
            bottomBound -= mHorizontalDividerHeight;
        }

        mBoundsWithoutHeader.set(0, topBound, width, bottomBound);

        if (mActivatedStateSupported && isActivated()) {
            mActivatedBackgroundDrawable.setBounds(mBoundsWithoutHeader);
        }

        /*
         * New Feature by Mediatek Inc Begin.
         * Description: Add the check-box support for ContactListItemView.
         */
//    	if(ContactsApplication.sIsGnContactsSupport) {
//    	    // gionee xuhz 20120507 modify for CR00588623 start
//    	    rightBound = gnLayoutCheckBox(rightBound, topBound, bottomBound);
//    	    // gionee xuhz 20120507 modify for CR00588623 end
//    	} else {
        // aurora <wangth> <2013-12-2> modify for aurora begin
        if (mIsCheckBoxShow) {
            leftBound = layoutCheckBox(leftBound, topBound, bottomBound);
            
        }
        
        rightBound = layoutAuroraSimIcon(rightBound, topBound, bottomBound);
        // aurora <wangth> <2013-12-2> modify for aurora end
//    	}

    	/*
         * New Feature by Mediatek Inc End.
         * Description: Add the check-box support for ContactListItemView.
         */

        if (ResConstant.isFouceHideContactListPhoto()) {
        	final View photoView = mQuickContact != null ? mQuickContact : mPhotoView;
        	if (null != photoView) {
        		photoView.setVisibility(View.GONE);	
        	}
        	//aurora <wangth> <2013-9-4> modify for auroro ui begin
        	/*
        	leftBound += getResources().
        		getDimensionPixelOffset(R.dimen.gn_contact_list_item_name_left_margin);
        	*/
        	if (mIsNeedPaddingLeft) {
        	    leftBound += getResources().
                        getDimensionPixelOffset(R.dimen.aurora_contact_list_item_name_left_margin);
        	}
        	//aurora <wangth> <2013-9-4> modify for auroro ui end
        } else {
        	final View photoView = mQuickContact != null ? mQuickContact : mPhotoView;
            if (mPhotoPosition == PhotoPosition.LEFT) {
            	
                // Photo is the left most view. All the other Views should on the right of the photo.
                if (photoView != null) {
                    // Center the photo vertically
                    final int photoTop = topBound + (bottomBound - topBound - mPhotoViewHeight) / 2;
                    // gionee xuhz 20120605 add start
                    if(ContactsApplication.sIsGnContactsSupport) {
                        int gnPhoteViewLeftPadding = mContext.getResources().
                                getDimensionPixelOffset(R.dimen.gn_contact_list_item_photo_left_gap);
                        leftBound = leftBound + gnPhoteViewLeftPadding;
                    }
                    // gionee xuhz 20120605 add end

                    photoView.layout(
                            leftBound,
                            photoTop,
                            leftBound + mPhotoViewWidth,
                            photoTop + mPhotoViewHeight);
                    leftBound += mPhotoViewWidth;
                    photoView.setVisibility(View.VISIBLE);
                } else if (mKeepHorizontalPaddingForPhotoView) {
                    // Draw nothing but keep the padding.
                    leftBound += mPhotoViewWidth;
                }
                
                leftBound += mGapBetweenImageAndText;
            } else {
                // Photo is the right most view. Right bound should be adjusted that way.
                if (photoView != null) {
                    // Center the photo vertically
                    final int photoTop = topBound + (bottomBound - topBound - mPhotoViewHeight) / 2;
                    photoView.layout(
                            rightBound - mPhotoViewWidth,
                            photoTop,
                            rightBound,
                            photoTop + mPhotoViewHeight);
                    rightBound -= (mPhotoViewWidth + mGapBetweenImageAndText);
                }

                // Add indent between left-most padding and texts.
                leftBound += mTextIndent;
            }        	
        }

        // Layout the call button.
        rightBound = layoutRightSide(height, topBound, bottomBound, rightBound);

        // Center text vertically
        final int totalTextHeight = mNameTextViewHeight + mPhoneticNameTextViewHeight +
                mLabelAndDataViewMaxHeight + mSnippetTextViewHeight + mStatusTextViewHeight;
        int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;

        // gionee xuhz 20120517 add start
        if (ContactsApplication.sIsGnContactsSupport) {
            int gnTotalTextHeight = mNameTextViewHeight + mPhoneticNameTextViewHeight +
                mLabelViewHeight + mDataViewHeight + mSnippetTextViewHeight + mStatusTextViewHeight;
            textTopBound = (bottomBound + topBound - gnTotalTextHeight) / 2;
        }
        // gionee xuhz 20120517 add end
        
        // Layout all text view and presence icon
        // Put name TextView first
        if (isVisible(mNameTextView)) {
            // aurora <wangth> <2013-12-10> modify for aurora begin 
            /*
            mNameTextView.layout(leftBound,
                    textTopBound,
                    rightBound,
                    textTopBound + mNameTextViewHeight);
            */
            if (isVisible(mSnippetView) || isVisible(mDataView)) {
                mNameTextView.layout(leftBound,
                        textTopBound - 9,
                        rightBound,
                        textTopBound + mNameTextViewHeight + 6);
//                mNameTextView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + 3);
            } else {
                mNameTextView.layout(leftBound,
                        textTopBound,
                        rightBound,
                        textTopBound + mNameTextViewHeight);
//                mNameTextView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() - 3);
            }
//            mNameTextView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + 100, getPaddingBottom());
            textTopBound += mNameTextViewHeight;
            // aurora <wangth> <2013-12-10> modify for aurora end
        }

        // Presence and status
        int statusLeftBound = leftBound;
        if (isVisible(mPresenceIcon)) {
            int iconWidth = mPresenceIcon.getMeasuredWidth();
            mPresenceIcon.layout(
                    leftBound,
                    textTopBound,
                    leftBound + iconWidth,
                    textTopBound + mStatusTextViewHeight);
            statusLeftBound += (iconWidth + mPresenceIconMargin);
        }

        if (isVisible(mStatusView)) {
            mStatusView.layout(statusLeftBound,
                    textTopBound,
                    rightBound,
                    textTopBound + mStatusTextViewHeight);
        }

        if (isVisible(mStatusView) || isVisible(mPresenceIcon)) {
            textTopBound += mStatusTextViewHeight;
        }

        // Rest of text views
        int dataLeftBound = leftBound;
        if (isVisible(mPhoneticNameTextView)) {
            mPhoneticNameTextView.layout(leftBound,
                    textTopBound,
                    rightBound,
                    textTopBound + mPhoneticNameTextViewHeight);
            textTopBound += mPhoneticNameTextViewHeight;
        }

        // gionee xuhz 20120517 modify start
        if (ContactsApplication.sIsGnContactsSupport) {
            if (isVisible(mDataView)) {
                mDataView.layout(leftBound,
                        textTopBound + 8,
                        rightBound,
                        textTopBound + mDataViewHeight + 8);
                textTopBound += mDataViewHeight;
                // aurora <wangth> <2013-12-10> add for aurora begin
//                mDataView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() - 3);
                mDataView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + 100, getPaddingBottom());
                // aurora <wangth> <2013-12-10> add for aurora end
            }
            
            if (isVisible(mLabelView)) {
                mLabelView.layout(leftBound,
                        textTopBound,
                        rightBound,
                        textTopBound + mLabelViewHeight);
                textTopBound += mLabelViewHeight;
            }
        } else {
            if (isVisible(mLabelView)) {
                if (mPhotoPosition == PhotoPosition.LEFT) {
                    // When photo is on left, label is placed on the right edge of the list item.
                    mLabelView.layout(rightBound - mLabelView.getMeasuredWidth(),
                            textTopBound + mLabelAndDataViewMaxHeight - mLabelViewHeight,
                            rightBound,
                            textTopBound + mLabelAndDataViewMaxHeight);
                    rightBound -= mLabelView.getMeasuredWidth();
                } else {
                    // When photo is on right, label is placed on the left of data view.
                    dataLeftBound = leftBound + mLabelView.getMeasuredWidth();
                    mLabelView.layout(leftBound,
                            textTopBound + mLabelAndDataViewMaxHeight - mLabelViewHeight,
                            dataLeftBound,
                            textTopBound + mLabelAndDataViewMaxHeight);
                    dataLeftBound += mGapBetweenLabelAndData;
                }
            }

            if (isVisible(mDataView)) {
                mDataView.layout(dataLeftBound,
                        textTopBound + mLabelAndDataViewMaxHeight - mDataViewHeight,
                        rightBound,
                        textTopBound + mLabelAndDataViewMaxHeight);
            }
            if (isVisible(mLabelView) || isVisible(mDataView)) {
                textTopBound += mLabelAndDataViewMaxHeight;
            }        	
        }
        // Label and Data align bottom.
        // gionee xuhz 20120517 modify end

        if (isVisible(mSnippetView)) {
            mSnippetView.layout(leftBound,
                    textTopBound + 8,
                    rightBound,
                    textTopBound + mSnippetTextViewHeight + 8);
            // aurora <wangth> <2013-12-10> add for aurora begin
//            mSnippetView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() - 3);
            mSnippetView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + 100, getPaddingBottom());
            // aurora <wangth> <2013-12-10> add for aurora end
        }
    }

    /**
     * Performs layout of the right side of the view
     *
     * @return new right boundary
     */
    protected int layoutRightSide(int height, int topBound, int bottomBound, int rightBound) {
        // Put call button and vertical divider
        if (isVisible(mCallButton)) {
            int buttonWidth = mCallButton.getMeasuredWidth();
            rightBound -= buttonWidth;
            mCallButton.layout(
                    rightBound,
                    topBound,
                    rightBound + buttonWidth,
                    height - mHorizontalDividerHeight);
            mVerticalDividerVisible = true;
            ensureVerticalDivider();
            rightBound -= mVerticalDividerWidth;
            mVerticalDividerDrawable.setBounds(
                    rightBound,
                    topBound + mVerticalDividerMargin,
                    rightBound + mVerticalDividerWidth,
                    height - mVerticalDividerMargin);
        } else {
            mVerticalDividerVisible = false;
        }

        return rightBound;
    }

    @Override
    public void adjustListItemSelectionBounds(Rect bounds) {
        bounds.top += mBoundsWithoutHeader.top;
        bounds.bottom = bounds.top + mBoundsWithoutHeader.height();
        bounds.left += mSelectionBoundsMarginLeft;
        bounds.right -= mSelectionBoundsMarginRight;
    }

    protected boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * Loads the drawable for the vertical divider if it has not yet been loaded.
     */
    private void ensureVerticalDivider() {
        if (mVerticalDividerDrawable == null) {
            mVerticalDividerDrawable = mContext.getResources().getDrawable(
                    R.drawable.divider_vertical_dark);
            mVerticalDividerWidth = mVerticalDividerDrawable.getIntrinsicWidth();
        }
    }

    /**
     * Extracts width and height from the style
     */
    private void ensurePhotoViewSize() {
        if (!mPhotoViewWidthAndHeightAreReady) {
            if (mQuickContactEnabled) {
                // gionee xuhz 20120515 modify start 
                if (ContactsApplication.sIsGnContactsSupport) {
                    mPhotoViewWidth = mPhotoViewHeight = getDefaultPhotoViewSize();
                } else {
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
                // gionee xuhz 20120515 modify end 
            } else if (mPhotoView != null) {
                mPhotoViewWidth = mPhotoViewHeight = getDefaultPhotoViewSize();
            } else {
                final int defaultPhotoViewSize = getDefaultPhotoViewSize();
                mPhotoViewWidth = mKeepHorizontalPaddingForPhotoView ? defaultPhotoViewSize : 0;
                mPhotoViewHeight = mKeepVerticalPaddingForPhotoView ? defaultPhotoViewSize : 0;
            }

            mPhotoViewWidthAndHeightAreReady = true;
        }
    }

    protected void setDefaultPhotoViewSize(int pixels) {
        mDefaultPhotoViewSize = pixels;
    }

    protected int getDefaultPhotoViewSize() {
        return mDefaultPhotoViewSize;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mActivatedStateSupported) {
            mActivatedBackgroundDrawable.setState(getDrawableState());
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mActivatedBackgroundDrawable || super.verifyDrawable(who);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mActivatedStateSupported) {
            mActivatedBackgroundDrawable.jumpToCurrentState();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mActivatedStateSupported && isActivated()) {
            mActivatedBackgroundDrawable.draw(canvas);
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
                mHeaderTextView.setTextColor(mHeaderTextColor);
                mHeaderTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeaderTextSize);
//                mHeaderTextView.setTypeface(mHeaderTextView.getTypeface(), Typeface.BOLD);
                //mHeaderTextView.setGravity(Gravity.CENTER_VERTICAL);
                mHeaderTextView.setGravity(Gravity.BOTTOM);
                // gionee xuhz 20120605 add start
                if(ContactsApplication.sIsGnContactsSupport) {
                	mHeaderTextView.setTextColor(ResConstant.sHeaderTextColor);
//                    mHeaderTextView.setBackgroundResource(R.drawable.list_section_divider_holo_custom);
                	//aurora <wangth> <2013-9-16> modify for auroro ui begin
                	/*
                    mHeaderTextView.setPadding(ResConstant.sHeaderTextLeftPadding, 0, 0, 0);
                    */
                	mHeaderTextView.setPadding(ResConstant.sHeaderTextLeftPadding, 0, 0, 
                	        ContactsUtils.CONTACT_LIST_HEADER_PADDING_BOTTOM);
                	//aurora <wangth> <2013-9-16> modify for auroro ui end
                }
                // gionee xuhz 20120605 add end
                addView(mHeaderTextView);
            }
            if (mHeaderDivider == null) {
                mHeaderDivider = new View(mContext);
                mHeaderDivider.setBackgroundColor(mHeaderUnderlineColor);
                addView(mHeaderDivider);
            }
            setMarqueeText(mHeaderTextView, title);
            mHeaderTextView.setVisibility(View.VISIBLE);
            //aurora <wangth> <2013-9-4> modify for auroro ui begin
            /*
            mHeaderDivider.setVisibility(View.VISIBLE);
            */
            mHeaderDivider.setVisibility(View.GONE);
            //aurora <wangth> <2013-9-4> modify for auroro ui end
            mHeaderTextView.setAllCaps(true);
            mHeaderVisible = true;
        } else {
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.GONE);
            }
            if (mHeaderDivider != null) {
                mHeaderDivider.setVisibility(View.GONE);
            }
            mHeaderVisible = false;
        }
    }

    /**
     * Returns the quick contact badge, creating it if necessary.
     */
    public QuickContactBadge getQuickContact() {
        if (!mQuickContactEnabled) {
            throw new IllegalStateException("QuickContact is disabled for this view");
        }
        if (mQuickContact == null) {
            mQuickContact = new QuickContactBadge(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            // Gionee:huangzy 20120530 add for CR00608714 start
            if (ContactsApplication.sIsGnZoomClipSupport) {
                mQuickContact.setScaleType(ScaleType.CENTER_CROP);
            }
            // Gionee:huangzy 20120530 add for CR00608714 end
            if (mNameTextView != null) {
                mQuickContact.setContentDescription(mContext.getString(
                        R.string.description_quick_contact_for, mNameTextView.getText()));
            }

            addView(mQuickContact);
            mPhotoViewWidthAndHeightAreReady = false;
        }
        return mQuickContact;
    }

    /**
     * Returns the photo view, creating it if necessary.
     */
    public ImageView getPhotoView() {
        if (mPhotoView == null) {
            if (mQuickContactEnabled) {
                mPhotoView = new ImageView(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            } else {
                mPhotoView = new ImageView(mContext);
            }
            
            // Gionee:huangzy 20120530 add for CR00608714 start
            if (ContactsApplication.sIsGnZoomClipSupport) {
                mPhotoView.setScaleType(ScaleType.CENTER_CROP);
            }
            // Gionee:huangzy 20120530 add for CR00608714 end
            
            // Quick contact style used above will set a background - remove it
            mPhotoView.setBackgroundDrawable(null);
            addView(mPhotoView);
            mPhotoViewWidthAndHeightAreReady = false;
        }
        return mPhotoView;
    }

    /**
     * Removes the photo view.
     */
    public void removePhotoView() {
        removePhotoView(false, true);
    }

    /**
     * Removes the photo view.
     *
     * @param keepHorizontalPadding True means data on the right side will have
     *            padding on left, pretending there is still a photo view.
     * @param keepVerticalPadding True means the View will have some height
     *            enough for accommodating a photo view.
     */
    public void removePhotoView(boolean keepHorizontalPadding, boolean keepVerticalPadding) {
        mPhotoViewWidthAndHeightAreReady = false;
        mKeepHorizontalPaddingForPhotoView = keepHorizontalPadding;
        mKeepVerticalPaddingForPhotoView = keepVerticalPadding;
        if (mPhotoView != null) {
            removeView(mPhotoView);
            mPhotoView = null;
        }
        if (mQuickContact != null) {
            removeView(mQuickContact);
            mQuickContact = null;
        }
    }

    /**
     * Sets a word prefix that will be highlighted if encountered in fields like
     * name and search snippet.
     * <p>
     * NOTE: must be all upper-case
     */
    public void setHighlightedPrefix(char[] upperCasePrefix) {
        mHighlightedPrefix = upperCasePrefix;
    }

    /**
     * Returns the text view for the contact name, creating it if necessary.
     */
    public AuroraTextView getNameTextView() {
        if (mNameTextView == null) {
            mNameTextView = new AuroraTextView(mContext);
            mNameTextView.setSingleLine(true);
            mNameTextView.setEllipsize(getTextEllipsis());
			// aurora ukiliu 2013-11-12 modify for aurora ui begin
            mNameTextView.setTextAppearance(mContext, R.style.list_item_name_text_style);
			// aurora ukiliu 2013-11-12 modify for aurora ui end
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mNameTextView.setActivated(isActivated());
            mNameTextView.setGravity(Gravity.CENTER_VERTICAL);
            addView(mNameTextView);
        }
        return mNameTextView;
    }
    
    // aurora <wangth> <2013-11-18> add for aurora begin
    public void setNameTextViewStyle(boolean flag, boolean dataViewFlag) {
        if (mNameTextView == null) {
            mNameTextView = getNameTextView();
        }
        
        if (mNameTextView == null) {
            return;
        }
        
        if (dataViewFlag && mDataView == null) {
            mDataView = getDataView();
        }
        
        if (flag) {
            // no selector change
        	if ((mDataView != null && isVisible(mDataView)) || (mSnippetView != null && isVisible(mSnippetView))) {
        		mNameTextView.setTextAppearance(mContext, com.aurora.R.style.AuroraListMainTextStyle);
        	} else {
        		mNameTextView.setTextAppearance(mContext,  com.aurora.R.style.AuroraListSingleLineStyle);
        	}
            
            if (mDataView != null && isVisible(mDataView)) {
                mDataView.setTextAppearance(mContext, R.style.list_item_data_text_no_change_style);
            }
            
            if (mSnippetView != null && isVisible(mSnippetView)) {
                mSnippetView.setTextAppearance(mContext, R.style.list_item_data_text_no_change_style);
            }
        } else {
            // selector change
        	if ((mDataView != null && isVisible(mDataView)) || (mSnippetView != null && isVisible(mSnippetView))) {
        		mNameTextView.setTextAppearance(mContext,  com.aurora.R.style.AuroraListMainTextStyle);
        	} else {
        		mNameTextView.setTextAppearance(mContext, com.aurora.R.style.AuroraListSingleLineStyle);
        	}
            
            if (mDataView != null && isVisible(mDataView)) {
                mDataView.setTextAppearance(mContext, R.style.list_item_data_text_style);
            }
            
            if (mSnippetView != null && isVisible(mSnippetView)) {
                mSnippetView.setTextAppearance(mContext, R.style.list_item_data_text_style);
            }
        }
    }
    //  aurora <wangth> <2013-11-18> add for aurora end

    /**
     * Adds a call button using the supplied arguments as an id and tag.
     */
    public void showCallButton(int id, int tag) {
        if (mCallButton == null) {
            mCallButton = new DontPressWithParentImageView(mContext, null);
            mCallButton.setId(id);
            mCallButton.setOnClickListener(mCallButtonClickListener);
            mCallButton.setBackgroundResource(R.drawable.call_background);
            mCallButton.setImageResource(android.R.drawable.sym_action_call);
            mCallButton.setPadding(mCallButtonPadding, 0, mCallButtonPadding, 0);
            mCallButton.setScaleType(ScaleType.CENTER);
            addView(mCallButton);
        }

        mCallButton.setTag(tag);
        mCallButton.setVisibility(View.VISIBLE);
    }

    public void hideCallButton() {
        if (mCallButton != null) {
            mCallButton.setVisibility(View.GONE);
        }
    }

    /**
     * Adds or updates a text view for the phonetic name.
     */
    public void setPhoneticName(char[] text, int size) {
        if (text == null || size == 0) {
            if (mPhoneticNameTextView != null) {
                mPhoneticNameTextView.setVisibility(View.GONE);
            }
        } else {
            getPhoneticNameTextView();
            setMarqueeText(mPhoneticNameTextView, text, size);
            mPhoneticNameTextView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the phonetic name, creating it if necessary.
     */
    public TextView getPhoneticNameTextView() {
        if (mPhoneticNameTextView == null) {
            mPhoneticNameTextView = new TextView(mContext);
            mPhoneticNameTextView.setSingleLine(true);
            mPhoneticNameTextView.setEllipsize(getTextEllipsis());
            mPhoneticNameTextView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mPhoneticNameTextView.setTypeface(mPhoneticNameTextView.getTypeface(), Typeface.BOLD);
            mPhoneticNameTextView.setActivated(isActivated());
            addView(mPhoneticNameTextView);
        }
        return mPhoneticNameTextView;
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
            setMarqueeText(mLabelView, text);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data label, creating it if necessary.
     */
    public TextView getLabelView() {
        // gionee xuhz 20120517 add start
        if (ContactsApplication.sIsGnContactsSupport) {
            return gnGetLabelView();
        }
        // gionee xuhz 20120517 add end
        if (mLabelView == null) {
            mLabelView = new TextView(mContext);
            mLabelView.setSingleLine(true);
            mLabelView.setEllipsize(getTextEllipsis());
            mLabelView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            if (mPhotoPosition == PhotoPosition.LEFT) {
                // edit by mediatek 
//                mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mCountViewTextSize);
                mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                mLabelView.setAllCaps(true);
                mLabelView.setGravity(Gravity.RIGHT);
            } else {
                mLabelView.setTypeface(mLabelView.getTypeface(), Typeface.BOLD);
            }
            mLabelView.setActivated(isActivated());
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
        } else {
            getDataView();
            setMarqueeText(mDataView, text, size);
            mDataView.setVisibility(VISIBLE);
        }
    }

    private void setMarqueeText(TextView textView, char[] text, int size) {
        if (getTextEllipsis() == TruncateAt.MARQUEE) {
            setMarqueeText(textView, new String(text, 0, size));
        } else {
            textView.setText(text, 0, size);
        }
    }

    private void setMarqueeText(TextView textView, CharSequence text) {
        if (getTextEllipsis() == TruncateAt.MARQUEE) {
            // To show MARQUEE correctly (with END effect during non-active state), we need
            // to build Spanned with MARQUEE in addition to TextView's ellipsize setting.
            final SpannableString spannable = new SpannableString(text);
            spannable.setSpan(TruncateAt.MARQUEE, 0, spannable.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else {
            textView.setText(text);
        }
    }

    /**
     * Returns the text view for the data text, creating it if necessary.
     */
    public TextView getDataView() {
        if (mDataView == null) {
            mDataView = new TextView(mContext);
            mDataView.setSingleLine(true);
            mDataView.setEllipsize(getTextEllipsis());
//            mDataView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mDataView.setTextColor(R.color.aurora_group_edit_ringtone_text_color);
            mDataView.setActivated(isActivated());
            addView(mDataView);
        }
        return mDataView;
    }

    /**
     * Adds or updates a text view for the search snippet.
     */
    public void setSnippet(String text) {
        // aurora <wangth> <2013-12-17> add for aurora begin
        String temp = "";
        if (text != null && temp.equals(text.substring(0, 1))) {
            text = text.substring(1, text.length() - 1);
        }
        // aurora <wangth> <2013-12-17> add for aurora end
        
        if (TextUtils.isEmpty(text)) {
            if (mSnippetView != null) {
                mSnippetView.setVisibility(View.GONE);
            }
        } else {
            mPrefixHighligher.setText(getSnippetView(), text, mHighlightedPrefix);
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
            mSnippetView.setEllipsize(getTextEllipsis());
//            mSnippetView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
//            mSnippetView.setTypeface(mSnippetView.getTypeface(), Typeface.BOLD);
            mSnippetView.setTextColor(R.color.aurora_group_edit_ringtone_text_color);
            mSnippetView.setActivated(isActivated());
            addView(mSnippetView);
        }
        return mSnippetView;
    }

    /**
     * Returns the text view for the status, creating it if necessary.
     */
    public TextView getStatusView() {
        if (mStatusView == null) {
            mStatusView = new TextView(mContext);
            mStatusView.setSingleLine(true);
            mStatusView.setEllipsize(getTextEllipsis());
            mStatusView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mStatusView.setTextColor(mSecondaryTextColor);
            mStatusView.setActivated(isActivated());
            addView(mStatusView);
        }
        return mStatusView;
    }

    /**
     * Returns the text view for the contacts count, creating it if necessary.
     */
    public TextView getCountView() {
        if (mCountView == null) {
            mCountView = new TextView(mContext);
            mCountView.setSingleLine(true);
            mCountView.setEllipsize(getTextEllipsis());
            mCountView.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
            mCountView.setTextColor(R.color.contact_count_text_color);
            addView(mCountView);
        }
        return mCountView;
    }

    /**
     * Adds or updates a text view for the contacts count.
     */
    public void setCountView(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mCountView != null) {
                mCountView.setVisibility(View.GONE);
            }
        } else {
            getCountView();
            setMarqueeText(mCountView, text);
            mCountView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCountViewTextSize);
            mCountView.setGravity(Gravity.CENTER_VERTICAL);
            mCountView.setTextColor(mContactsCountTextColor);
            mCountView.setVisibility(VISIBLE);
        }
    }

    /**
     * Adds or updates a text view for the status.
     */
    public void setStatus(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mStatusView != null) {
                mStatusView.setVisibility(View.GONE);
            }
        } else {
            getStatusView();
            setMarqueeText(mStatusView, text);
            mStatusView.setVisibility(VISIBLE);
        }
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

    private TruncateAt getTextEllipsis() {
        return TruncateAt.END;
    }

    public void showDisplayName(Cursor cursor, int nameColumnIndex, int displayOrder) {
        CharSequence name = cursor.getString(nameColumnIndex);
        if (!TextUtils.isEmpty(name)) {
            name = mPrefixHighligher.apply(name, mHighlightedPrefix);
        } else {
            name = mUnknownNameText;
        }
        setMarqueeText(getNameTextView(), name);

        // Since the quick contact content description is derived from the display name and there is
        // no guarantee that when the quick contact is initialized the display name is already set,
        // do it here too.
        if (mQuickContact != null) {
            mQuickContact.setContentDescription(mContext.getString(
                    R.string.description_quick_contact_for, mNameTextView.getText()));
        }
    }
    
    //Gionee:huangzy 20120607 add for CR00614801 start
    public String gnShowDisplayName(Cursor cursor, int nameColumnIndex) {
        CharSequence name = cursor.getString(nameColumnIndex);
        if (!TextUtils.isEmpty(name)) {
            name = mPrefixHighligher.apply(name, mHighlightedPrefix);
        } else {
            name = mUnknownNameText;
        }
        setMarqueeText(getNameTextView(), name);

        // Since the quick contact content description is derived from the display name and there is
        // no guarantee that when the quick contact is initialized the display name is already set,
        // do it here too.
        if (mQuickContact != null) {
            mQuickContact.setContentDescription(mContext.getString(
                    R.string.description_quick_contact_for, mNameTextView.getText()));
        }
        
        return name.toString();
    }
    //Gionee:huangzy 20120607 add for CR00614801 end
    
    public String auroraShowDisplayName(Cursor cursor, int nameColumnIndex, String queryStr) {
        CharSequence name = cursor.getString(nameColumnIndex);
        ContactsUtils.aurorahighlightName(getNameTextView(), cursor);

        // Since the quick contact content description is derived from the display name and there is
        // no guarantee that when the quick contact is initialized the display name is already set,
        // do it here too.
        if (mQuickContact != null) {
            mQuickContact.setContentDescription(mContext.getString(
                    R.string.description_quick_contact_for, mNameTextView.getText()));
        }
        
        return name.toString();
    }

    public void hideDisplayName() {
        if (mNameTextView != null) {
            removeView(mNameTextView);
            mNameTextView = null;
        }
    }

    public void showPhoneticName(Cursor cursor, int phoneticNameColumnIndex) {
        cursor.copyStringToBuffer(phoneticNameColumnIndex, mPhoneticNameBuffer);
        int phoneticNameSize = mPhoneticNameBuffer.sizeCopied;
        if (phoneticNameSize != 0) {
            setPhoneticName(mPhoneticNameBuffer.data, phoneticNameSize);
        } else {
            setPhoneticName(null, 0);
        }
    }

    public void hidePhoneticName() {
        if (mPhoneticNameTextView != null) {
            removeView(mPhoneticNameTextView);
            mPhoneticNameTextView = null;
        }
    }

    /**
     * Sets the proper icon (star or presence or nothing) and/or status message.
     */
    public void showPresenceAndStatusMessage(Cursor cursor, int presenceColumnIndex,
            int contactStatusColumnIndex) {
        Drawable icon = null;
        int presence = 0;
        if (!cursor.isNull(presenceColumnIndex)) {
            presence = cursor.getInt(presenceColumnIndex);
            icon = ContactPresenceIconUtil.getPresenceIcon(getContext(), presence);
        }
        setPresence(icon);

        String statusMessage = null;
        if (contactStatusColumnIndex != 0 && !cursor.isNull(contactStatusColumnIndex)) {
            statusMessage = cursor.getString(contactStatusColumnIndex);
        }
        // If there is no status message from the contact, but there was a presence value, then use
        // the default status message string
        if (statusMessage == null && presence != 0) {
            statusMessage = ContactStatusUtil.getStatusString(getContext(), presence);
        }
        setStatus(statusMessage);
    }

    /**
     * Shows search snippet.
     */
    public void showSnippet(Cursor cursor, int summarySnippetColumnIndex) {
        if (cursor.getColumnCount() <= summarySnippetColumnIndex) {
            setSnippet(null);
            return;
        }
        setSnippet(cursor.getString(summarySnippetColumnIndex));        
    }

    /**
     * Shows data element (e.g. phone number).
     */
    public void showData(Cursor cursor, int dataColumnIndex) {
        cursor.copyStringToBuffer(dataColumnIndex, mDataBuffer);
        setData(mDataBuffer.data, mDataBuffer.sizeCopied);
    }

    public void setActivatedStateSupported(boolean flag) {
        this.mActivatedStateSupported = flag;
    }

    @Override
    public void requestLayout() {
        // We will assume that once measured this will not need to resize
        // itself, so there is no need to pass the layout request to the parent
        // view (ListView).
        forceLayout();
    }

    public void setPhotoPosition(PhotoPosition photoPosition) {
        mPhotoPosition = photoPosition;
    }

    public PhotoPosition getPhotoPosition() {
        return mPhotoPosition;
    }

    /**
     * Specifies left and right margin for selection bounds. See also
     * {@link #adjustListItemSelectionBounds(Rect)}.
     */
    public void setSelectionBoundsHorizontalMargin(int left, int right) {
        mSelectionBoundsMarginLeft = left;
        mSelectionBoundsMarginRight = right;
    }

    // The following lines are provided and maintained by Mediatek Inc.

    /*
     * New Feature by Mediatek Inc Begin.
     * Description: Add the check-box support for ContactListItemView.
     */
//    private CheckBox mSelectBox = null;
    private AuroraCheckBox mSelectBox = null; // aurora <wangth> <2013-12-12> modify for aurora

    /**
     * Enable check-box or disable check-box
     * 
     * @param checkable is true, create check-box and set the visibility.
     */
    public void setCheckable(boolean checkable) {
        if (checkable) {
            // aurora <wangth> <2013-12-2> add for aurora begin
            mIsCheckBoxShow = true;
            // aurora <wangth> <2013-12-2> add for aurora end
            if (mSelectBox == null) {
                getCheckBox();
            }
            mSelectBox.setVisibility(View.VISIBLE);
        } else {
            if (mSelectBox != null) {
                mSelectBox.setVisibility(View.GONE);
            }
        }
    }
    
    // aurora <wangth> <2013-12-2> add for aurora begin
    private boolean mIsCheckBoxShow = false;
    private boolean mIsNeedPaddingLeft = true;
    public void auroraSetCheckable(boolean checkable) {
        mIsNeedPaddingLeft = checkable;
    }
    // aurora <wangth> <2013-12-2> add for aurora end

    /**
     * Retrieve the check box view for changing its state between Checked or
     * Unchecked state
     * 
     * @return check-box view
     */
//    public CheckBox getCheckBox() {
    public AuroraCheckBox getCheckBox() {
        if (mSelectBox == null) {
//            mSelectBox = new CheckBox(mContext);
            mSelectBox = new AuroraCheckBox(mContext);
            mSelectBox.setClickable(false);
            mSelectBox.setFocusable(false);
            mSelectBox.setFocusableInTouchMode(false);
            mSelectBox.setWidth(mContext.getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_list_cb_width));
            addView(mSelectBox);
        }
        mSelectBox.setVisibility(View.VISIBLE);
        return mSelectBox;
    }

    /**
     * Measure check-box view
     */
    private void measureCheckBox() {
        if (isVisible(mSelectBox)) {
            mSelectBox.measure(0, 0);
        }
    }

    /**
     * Performs layout of check-box view
     *
     * @return new left boundary
     */
    private int layoutCheckBox(int leftBound, int topBound, int bottomBound) {
        if (this.isVisible(mSelectBox)) {
            int selectBoxWidth = mSelectBox.getMeasuredWidth();
            int selectBoxHeight = mSelectBox.getMeasuredHeight();
            // aurora <wangth> <2013-9-17> modify for aurora ui begin
            /*
            mSelectBox.layout(leftBound,
            (bottomBound + topBound - selectBoxHeight) / 2, 
            leftBound + selectBoxWidth, 
            (bottomBound + topBound + selectBoxHeight) / 2);
            */
            if (mDoubleRowMode && (isVisible(mSnippetView) || isVisible(mDataView))) {
                int paddingTop = 42;
                if (mHeaderVisible) {
                    paddingTop += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
                }
                int paddingButtom = paddingTop + selectBoxHeight;
                mSelectBox.layout(0,
                        paddingTop, 
                        selectBoxWidth, 
                        paddingButtom);
            } else if (!mDoubleRowMode && isVisible(mSnippetView)) {
                int paddingTop = 45;
                if (mHeaderVisible) {
                    paddingTop += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
                }
                int paddingButtom = paddingTop + selectBoxHeight;
                mSelectBox.layout(0,
                        paddingTop, 
                        selectBoxWidth, 
                        paddingButtom);
            } else {
                mSelectBox.layout(0,
                        (bottomBound + topBound - selectBoxHeight) / 2, 
                        selectBoxWidth, 
                        (bottomBound + topBound + selectBoxHeight) / 2);
            }
            // aurora <wangth> <2013-9-17> modify for aurora ui end
            return leftBound + selectBoxWidth;
        }
        return leftBound;
    }
    
    // gionee xuhz 20120507 modify for CR00588623 start
    private int gnLayoutCheckBox(int rightBound, int topBound, int bottomBound) {
        int gnSelectBoxRightGap = mContext.getResources()
            .getDimensionPixelOffset(R.dimen.gn_contact_list_item_checkbox_right_gap);

        if (this.isVisible(mSelectBox)) {
            int selectBoxWidth = mSelectBox.getMeasuredWidth();
            int selectBoxHeight = mSelectBox.getMeasuredHeight();
            int leftBound = rightBound - selectBoxWidth - gnSelectBoxRightGap;
            mSelectBox.layout(leftBound,
            (bottomBound + topBound - selectBoxHeight) / 2, 
            leftBound + selectBoxWidth, 
            (bottomBound + topBound + selectBoxHeight) / 2);
            return rightBound - selectBoxWidth - gnSelectBoxRightGap;
        }
        return rightBound - gnSelectBoxRightGap;
    }
    // gionee xuhz 20120507 modify for CR00588623 end
    
    // gionee xuhz 20120517 add start
    public TextView gnGetLabelView() {
        if (mLabelView == null) {
            mLabelView = new TextView(mContext);
            mLabelView.setSingleLine(true);
            mLabelView.setEllipsize(getTextEllipsis());
            mLabelView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            if (mPhotoPosition == PhotoPosition.LEFT) {
                // edit by mediatek 
//                mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mCountViewTextSize);
                mLabelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                mLabelView.setAllCaps(true);
                mLabelView.setGravity(Gravity.LEFT);
            } else {
                mLabelView.setTypeface(mLabelView.getTypeface(), Typeface.BOLD);
            }
            mLabelView.setActivated(isActivated());
            addView(mLabelView);
        }
        return mLabelView;
    }
    // gionee xuhz 20120517 add end
    
    /*
     * New Feature by Mediatek Inc End.
     * Description: Add the check-box support for ContactListItemView.
     */

    // The previous lines are provided and maintained by Mediatek Inc.
    
    public boolean isDataViewVisible() {
        return isVisible(mDataView);
    }

    public boolean isSnippetViewVisible() {
        return isVisible(mSnippetView);
    }
    
    public void setSimIconable(boolean checkable, int simId) {
        if (checkable) {
            if (simId < 1) {
                if (mAuroraSimIcon != null) {
                    mAuroraSimIcon.setVisibility(View.GONE);
                }
                
                return;
            }
            
            getAuroraSimIcon(simId);
            mAuroraSimIcon.setVisibility(View.VISIBLE);
        } else {
            if (mAuroraSimIcon != null) {
                mAuroraSimIcon.setVisibility(View.GONE);
            }
        }
    }
    
    public void setPrivacyIconable(boolean checkable, int privacyId) {
        if (checkable) {
            if (privacyId < 1) {
                if (mAuroraSimIcon != null) {
                    mAuroraSimIcon.setVisibility(View.GONE);
                }
                
                return;
            }
            
            if (mAuroraSimIcon == null) {
                mAuroraSimIcon = new ImageView(mContext);
                
                mAuroraSimIcon.setClickable(false);
                mAuroraSimIcon.setFocusable(false);
                mAuroraSimIcon.setFocusableInTouchMode(false);
                addView(mAuroraSimIcon);
            }
            mAuroraSimIcon.setBackgroundResource(R.drawable.aurora_privacy_contact_icon);
            mAuroraSimIcon.setVisibility(View.VISIBLE);
        } else {
            if (mAuroraSimIcon != null) {
                mAuroraSimIcon.setVisibility(View.GONE);
            }
        }
    }
    
    public ImageView getAuroraSimIcon(int simId) {
        if (mAuroraSimIcon == null) {
            mAuroraSimIcon = new ImageView(mContext);
            
            mAuroraSimIcon.setClickable(false);
            mAuroraSimIcon.setFocusable(false);
            mAuroraSimIcon.setFocusableInTouchMode(false);
            addView(mAuroraSimIcon);
        }
        mAuroraSimIcon.setBackgroundResource(ContactsUtils.getSimIcon(mContext, simId));
        mAuroraSimIcon.setVisibility(View.VISIBLE);
        return mAuroraSimIcon;
    }
    
    private void measureAuroraSimIcon() {
        if (isVisible(mAuroraSimIcon)) {
            mAuroraSimIcon.measure(0, 0);
        }
    }
    
    private int layoutAuroraSimIcon(int rightBound, int topBound, int bottomBound) {
        int gnSelectBoxRightGap = mContext.getResources()
            .getDimensionPixelOffset(R.dimen.aurora_sim_icon_gap_right);

        if (this.isVisible(mAuroraSimIcon)) {
            int selectBoxWidth = mAuroraSimIcon.getMeasuredWidth();
            int selectBoxHeight = mAuroraSimIcon.getMeasuredHeight();
            int leftBound = rightBound - selectBoxWidth - gnSelectBoxRightGap;
            
            if (isVisible(mSnippetView) || isVisible(mDataView)) {  // mms selector
                int paddingTop = DensityUtil.dip2px(mContext, 42/3);;
                if (mHeaderVisible) {
                    paddingTop += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
                }
                int paddingButtom = paddingTop + selectBoxHeight;
                mAuroraSimIcon.layout(leftBound,
                        paddingTop, 
                        leftBound + selectBoxWidth, 
                        paddingButtom);
            } else {
                mAuroraSimIcon.layout(leftBound,
                        (bottomBound + topBound - selectBoxHeight) / 2, 
                        leftBound + selectBoxWidth, 
                        (bottomBound + topBound + selectBoxHeight) / 2);
            }
        }
        
        return rightBound - gnSelectBoxRightGap;
    }
}
