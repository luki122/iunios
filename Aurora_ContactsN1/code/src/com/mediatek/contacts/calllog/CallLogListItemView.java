
package com.mediatek.contacts.calllog;

import com.android.contacts.format.PrefixHighlighter;
import com.android.contacts.list.ContactListItemView.PhotoPosition;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.CharArrayBuffer;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.ImageView.ScaleType;

import com.android.contacts.calllog.CallTypeIconsView;
import com.android.contacts.R;

import com.mediatek.contacts.widget.QuickContactBadgeWithPhoneNumber;

public class CallLogListItemView extends ViewGroup implements SelectionBoundsAdjuster {

    private static final String TAG = "CallLogListItemView";

    private static final int QUICK_CONTACT_BADGE_STYLE = 
            com.android.internal.R.attr.quickContactBadgeStyleWindowMedium;

    // Default const defined in layout
    private static final int DEFAULT_ITEM_DATE_BACKGROUND_COLOR = 0x55FFFFFF;
    private static final int DEFAULT_ITEM_TEXT_COLOR = 0xFFFFFFFF;  // default text color is white
    private static final int DEFAULT_ITEM_NAME_TEXT_SIZE = 18;  // 18sp
    private static final int DEFAULT_ITEM_NUMBER_TEXT_SIZE = 14;  // 14sp
    private static final int DEFAULT_ITEM_CALL_COUNT_TEXT_SIZE = 12;  // 12sp
    private static final int DEFAULT_ITEM_CALL_TIME_TEXT_SIZE = 12;  // 12sp
    private final float FONT_SIZE_EXTRA_LARGE = (float) 1.15; //The font size setting in setting
    private final float FONT_SIZE_LARGE = (float) 1.1;
    
    protected final Context mContext;

 // Style values for layout and appearance
    private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mCallButtonPadding;
    private final int mPresenceIconMargin;
    private final int mPresenceIconSize;
    private final int mCountViewTextSize;
    private final int mContactsCountTextColor;
    private final int mTextIndent;
    private Drawable mActivatedBackgroundDrawable;
    private final Drawable mSelectableItemBackgroundDrawable;
    private final int mSecondaryTextColor;

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

    // Horizontal divider between call log item views.
    private boolean mHorizontalDividerVisible = true;
    private int mHorizontalDividerHeight = 1;  // default 3px
    private View mViewHorizontalDivider; // if Drawable not set use view replace

    // Vertical divider between the call icon and the text.
    private static final int VERTICAL_DIVIDER_LEN = 1; // default is 1 px
    private boolean mVerticalDividerVisible;
    private int mVerticalDividerWidth = VERTICAL_DIVIDER_LEN;
    private View mViewVertialDivider; // if Drawable not set use view replace
    private Drawable mDrawableVertialDivider; // get from @dimen/ic_divider_dashed_holo_dark

    // The views inside the call log list item view
    // Header(Date) layout
    private boolean mCallLogDateVisible;
    private Drawable mDrawableCallLogBackgroundDrawable;
    private int mCallLogDateBackgroundHeight;  // default 22dip
    private TextView mTextViewCallLogDate;
    // Horizontal divider between call log date and below item views.
    private int mHorizontalDateDividerHeight = 3;  // default 2dip
    private View mViewHorizontalDateDivider; // if Drawable not set use view replace
    private int mHorizontalDateDividerColor = DEFAULT_ITEM_DATE_BACKGROUND_COLOR;

    // Other views
    private QuickContactBadgeWithPhoneNumber mQuickContactPhoto;
    private TextView mTextViewName;
    private TextView mTextViewNumber;
    private com.android.contacts.calllog.CallTypeIconsView mCallTypeIcon;
    private TextView mTextViewCallCount;
    private TextView mTextViewSimName;
    private TextView mTextViewCallTime;
    // private ImageButton mImageButtonCall;
    private DontPressWithParentImageView mImageViewCall;
    private CheckBox mCheckBoxMultiSel;

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

    private int mTextViewCallLogDateHeight;
    private int mViewHorizontalDateDividerHeight;
    private int mTextViewNameHeight;
    private int mTextViewNumberHeight;
    private int mCallTypeIconHeight;
    private int mTextViewCallCountHeight;
    private int mTextViewSimNameHeight;
    private int mTextViewCallTimeHeight;
    private int mImageViewCallHeight;
    private int mCheckBoxMultiSelHeight;

    // same row.
    private int mCallTypeIconSimNameMaxHeight;

    private OnClickListener mCallButtonClickListener;
    private OnClickListener mListItemOnClickListener;
    // TODO: some TextView fields are using CharArrayBuffer while some are not. Determine which is
    // more efficient for each case or in general, and simplify the whole implementation.
    // Note: if we're sure MARQUEE will be used every time, there's no reason to use
    // CharArrayBuffer, since MARQUEE requires Span and thus we need to copy characters inside the
    // buffer to Spannable once, while CharArrayBuffer is for directly applying char array to
    // TextView without any modification.
    private final CharArrayBuffer mDataBuffer = new CharArrayBuffer(128);
    private final CharArrayBuffer mPhoneticNameBuffer = new CharArrayBuffer(128);

    private boolean mActivatedStateSupported = false;

    private Rect mBoundsWithoutHeader = new Rect();

    /** A helper used to highlight a prefix in a text field. */
    private PrefixHighlighter mPrefixHighligher;
    private CharSequence mUnknownNameText;

    // Const get from @dimen
    private static int mCallLogOuterMarginDim;         // get from @dimen/call_log_outer_margin
    private static int mCallLogInnerMarginDim;         // get from @dimen/call_log_inner_margin
    private static int mImageViewCallWidthDim;       // get from @dimen/call_log_call_action_width
    private static int mImageViewCallHeightDim;      // get from @dimen/call_log_call_action_height
    private static int mVerticalDividerHeightDim;      // get from @demin/call_log_call_action_size
    private static int mQuickContactPhotoWidthDim;     // get from @dimen/call_log_list_contact_photo_size
    private static int mQuickContactPhotoHeightDim;    // get from @dimen/call_log_list_contact_photo_size
    private static int mSimNameWidthMaxDim;            // get from @dimen/calllog_list_item_simname_max_length1
    private static int mCheckBoxMultiSelWidthDim;      // get from @dimen/calllog_multi_select_list_item_checkbox_width
    private static int mCheckBoxMultiSelHeightDim;     // get from @dimen/calllog_multi_select_list_item_checkbox_height

    private static int mListItemQuickContactPaddingTop;   // get from @dimen/calllog_list_item_quick_contact_padding_top
    private static int mListItemQuickContactPaddingBottom;// get from @dimen/calllog_list_item_quick_contact_padding_bottom
    private static int mListItemPaddingLeftDim;           // get from @dimen/calllog_list_margin_left
    private static int mListItemPaddingRightDim;          // get from @dimen/calllog_list_margin_right
    
    private static int mNameWidthMaxDim;            // get from @dimen/calllog_list_item_name_max_length
    private static int mNumberWidthMaxDim;            // get from @dimen/calllog_list_item_number_max_length

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

    public CallLogListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // Read all style values
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContactListItemView);
        mPreferredHeight = a.getDimensionPixelSize(
                R.styleable.ContactListItemView_list_item_height, 0);
        mActivatedBackgroundDrawable = a.getDrawable(
                R.styleable.ContactListItemView_activated_background);
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

        mPrefixHighligher = new PrefixHighlighter(
                a.getColor(R.styleable.ContactListItemView_list_item_prefix_highlight_color,
                        Color.GREEN));
        a.recycle();

        a = mContext.obtainStyledAttributes(null,
                com.android.internal.R.styleable.ViewGroup_Layout,
                QUICK_CONTACT_BADGE_STYLE, 0);
        mPhotoViewWidth = a.getLayoutDimension(
                android.R.styleable.ViewGroup_Layout_layout_width,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPhotoViewHeight = a.getLayoutDimension(
                android.R.styleable.ViewGroup_Layout_layout_height,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        
        a.recycle();
        a = mContext.getTheme().obtainStyledAttributes(new int[] {android.R.attr.selectableItemBackground});
        mSelectableItemBackgroundDrawable = a.getDrawable(0);
        
        a.recycle();
        a = getContext().obtainStyledAttributes(attrs, R.styleable.CallLog);
        // Gionee:huangzy 20120604 modify for CR00616124 start
        /*mSecondaryTextColor = a.getInteger(
                R.styleable.CallLog_call_log_secondary_text_color, 0x999999);*/
        mSecondaryTextColor = a.getColor(
                R.styleable.CallLog_call_log_secondary_text_color, 0x999999);
        // Gionee:huangzy 20120604 modify for CR00616124 end
        a.recycle();

        if (mActivatedBackgroundDrawable != null) {
            mActivatedBackgroundDrawable.setCallback(this);
        }

        initPredefinedData();
    }

    public void adjustListItemSelectionBounds(Rect bounds) {
        // TODO Auto-generated method stub
        bounds.top += mBoundsWithoutHeader.top;
        bounds.bottom = bounds.top + mBoundsWithoutHeader.height();
        bounds.left += mSelectionBoundsMarginLeft;
        bounds.right -= mSelectionBoundsMarginRight;
    }

    /**
     * Installs a call button listener.
     */
    public void setOnCallButtonClickListener(OnClickListener callButtonClickListener) {
        mCallButtonClickListener = callButtonClickListener;
    }

    public void setListItemClickListener(OnClickListener listItemClickListener) {
        mListItemOnClickListener = listItemClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        /*
         * private boolean mCallLogDateVisible;
         * private int mCallLogDateBackgroundHeight;
         * private TextView mTextViewCallLogDate;
         * // Horizontal divider between call log date and below item views.
         * private int mHorizontalDateDividerHeight;
         * private View mViewHorizontalDateDivider;
         *  // Other views 
         * private QuickContactBadge mQuickContactPhoto;
         * private TextView mTextViewName;
         * private TextView mTextViewNumber;
         * private com.android.contacts.calllog.CallTypeIconsView mCallTypeIcon;
         * private TextView mTextViewCallCount;
         * private TextView mTextViewSimName;
         * private TextView mTextViewCallTime;
         * private ImageButton mImageButtonCall;
         */
        final int specWidth = resolveSize(0, widthMeasureSpec);
        final int preferredHeight;
        if (mCallLogDateVisible) {
            preferredHeight = mPreferredHeight + mCallLogDateBackgroundHeight + mHorizontalDateDividerHeight;
        } else {
            preferredHeight = mPreferredHeight;
        }

        mTextViewCallLogDateHeight = 0;
        mViewHorizontalDateDividerHeight = 0;
        mTextViewNameHeight = 0;
        mTextViewNumberHeight = 0;
        mCallTypeIconHeight = 0;
        mTextViewCallCountHeight = 0;
        mTextViewSimNameHeight = 0;
        mTextViewCallTimeHeight = 0;
        mImageViewCallHeight = 0;

        // Go over all visible text views and measure actual width of each of them.
        // Also calculate their heights to get the total height for this entire view.

        // Date - as Header
        if (isVisible(mTextViewCallLogDate)) {
            mTextViewCallLogDate.measure(
                    MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewCallLogDateHeight = mTextViewCallLogDate.getMeasuredHeight();
            mViewHorizontalDateDividerHeight = 3;  // default 3px
        }
        // Data Divider height is const

        // QuickContactPhoto height is const

        // Width each TextView is able to use.
        final int effectiveWidth;
        // All the other Views will honor the photo, so available width for them may be shrunk.
        if (mPhotoViewWidth > 0) {
            effectiveWidth = specWidth - getPaddingLeft() - getPaddingRight()
                    - (mPhotoViewWidth + mGapBetweenImageAndText);
        } else {
            effectiveWidth = specWidth - getPaddingLeft() - getPaddingRight();
        }

        // The width of Quick Contact and name view
        final int iPrimaryActionWidth = specWidth - mImageViewCallWidthDim
                - mVerticalDividerWidth - mCallLogInnerMarginDim;

        // Name
        if (isVisible(mTextViewName)) {
            mTextViewName.measure(
                    MeasureSpec.makeMeasureSpec(iPrimaryActionWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewNameHeight = mTextViewName.getMeasuredHeight();
        }

        // Number
        if (isVisible(mTextViewNumber)) {
            mTextViewNumber.measure(MeasureSpec.makeMeasureSpec(iPrimaryActionWidth,  MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewNumberHeight = mTextViewNumber.getMeasuredHeight();
        }

        // Call Type
        if (isVisible(mCallTypeIcon)) {
            mCallTypeIcon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mCallTypeIconHeight = mCallTypeIcon.getMeasuredHeight();
        }

        // Call Count
        if (isVisible(mTextViewCallCount)) {
            mTextViewCallCount.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewCallCountHeight = mTextViewCallCount.getMeasuredHeight();
        }
        mCallTypeIconSimNameMaxHeight = Math.max(mCallTypeIconHeight, mTextViewCallCountHeight);

        // Sim Name
        if (isVisible(mTextViewSimName)) {
            mTextViewSimName.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewSimNameHeight = mTextViewSimName.getMeasuredHeight();
        }
        mCallTypeIconSimNameMaxHeight = Math.max(mCallTypeIconSimNameMaxHeight, mTextViewSimNameHeight);

        // Call Time
        if (isVisible(mTextViewCallTime)) {
            mTextViewCallTime.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mTextViewCallTimeHeight = mTextViewCallTime.getMeasuredHeight();
        }
        mCallTypeIconSimNameMaxHeight = Math.max(mCallTypeIconSimNameMaxHeight, mTextViewCallTimeHeight);

        if (isVisible(mImageViewCall)) {
            mImageViewCall.measure(mImageViewCallWidthDim, mImageViewCallHeightDim);
            mImageViewCallHeight = mImageViewCall.getMeasuredHeight();
        }

        if (isVisible(mCheckBoxMultiSel)) {
            mCheckBoxMultiSel.measure(mCheckBoxMultiSelWidthDim, mCheckBoxMultiSelWidthDim);
            mCheckBoxMultiSelHeight = mCheckBoxMultiSel.getMeasuredHeight();
        }

        int iPaddingTop = getPaddingTop();
        int iPaddingBottom = getPaddingBottom();
        if (0 == iPaddingTop) {
            iPaddingTop = mListItemQuickContactPaddingTop;
        } else {
            mListItemQuickContactPaddingTop = iPaddingTop;
        }

        if (0 == iPaddingBottom) {
            iPaddingTop = mListItemQuickContactPaddingBottom;
        } else {
            mListItemQuickContactPaddingBottom = iPaddingTop;
        }
        
        // Calculate height including padding.
        int height = (mTextViewCallLogDateHeight + mViewHorizontalDateDividerHeight
                + mTextViewNameHeight + mTextViewNumberHeight + mCallTypeIconSimNameMaxHeight
                + iPaddingBottom + iPaddingTop);

        // Make sure the height is at least as high as the photo
        height = Math.max(height, mPhotoViewHeight + iPaddingTop + iPaddingBottom);

        // Add horizontal divider height
        if (mHorizontalDividerVisible) {
            height += mHorizontalDividerHeight;
        }

        // Make sure height is at least the preferred height
        height = Math.max(height, preferredHeight);
        setMeasuredDimension(specWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        final int height = bottom - top;
        final int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        final int iPaddingLeft = getPaddingLeft();
        final int iPaddingRight = getPaddingRight();
        
        int topBound = 0;
        int bottomBound = height;
        int leftBound = (iPaddingRight == 0) ? mListItemPaddingLeftDim : 0;
        int rightBound = width - ((iPaddingRight == 0) ? mListItemPaddingRightDim : 0);
        

        // Put the Data in the top of the contact view (Text + underline view)
        if (mCallLogDateVisible) {
            mTextViewCallLogDate
                    .layout(leftBound, topBound, rightBound, mTextViewCallLogDateHeight);
            topBound += mTextViewCallLogDateHeight;
            mViewHorizontalDateDivider.layout(leftBound, topBound, rightBound,
                    mViewHorizontalDateDividerHeight + topBound);
            topBound += mViewHorizontalDateDividerHeight;
        }

        // Put horizontal divider at the bottom
        if (mHorizontalDividerVisible) {
            if (null == mViewHorizontalDivider) {
                getHorizontalDivider();
            }
            mViewHorizontalDivider.layout(leftBound, height - mHorizontalDividerHeight, rightBound,
                    height);
            bottomBound -= mHorizontalDividerHeight;
        }

        mBoundsWithoutHeader.set(0, topBound, width, bottomBound);

        if (mActivatedStateSupported && isActivated()) {
            mActivatedBackgroundDrawable.setBounds(mBoundsWithoutHeader);
        }

        // Adjust the rect without header
        topBound += mListItemQuickContactPaddingTop;
        bottomBound -= mListItemQuickContactPaddingBottom;

        // Add Check Box
        if (isVisible(mCheckBoxMultiSel)) {
            final int checkBoxTop = topBound + (bottomBound - topBound - mCheckBoxMultiSelHeight) / 2;
            mCheckBoxMultiSel.layout(
                    leftBound,
                    checkBoxTop,
                    leftBound + mCheckBoxMultiSelWidthDim,
                    checkBoxTop + mCheckBoxMultiSelHeight);
            leftBound += (mCheckBoxMultiSelWidthDim);
        }

        // Add QuickContact View
        if (isVisible(mQuickContactPhoto)) {
            final int photoTop = topBound + (bottomBound - topBound - mPhotoViewHeight) / 2;
            mQuickContactPhoto.layout(
                    leftBound,
                    photoTop,
                    leftBound + mPhotoViewWidth,
                    photoTop + mPhotoViewHeight);
            leftBound += (mPhotoViewWidth + mCallLogInnerMarginDim);
        } else {
            // Draw nothing but keep the padding.
            // leftBound += mPhotoViewWidth + mGapBetweenImageAndText;
        }

        // Layout the call button.
        rightBound = layoutRightSide(height, topBound, bottomBound, rightBound);

        // Center text vertically
        final int totalTextHeight = mTextViewNameHeight + mTextViewNumberHeight
                + mCallTypeIconSimNameMaxHeight;
        int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;

        // Layout all text view and presence icon
        // Put name TextView first
        if (isVisible(mTextViewName)) {
            mTextViewName.layout(leftBound,
                    textTopBound,
                    rightBound,
                    textTopBound + mTextViewNameHeight);
            textTopBound += mTextViewNameHeight;
        }

        // Presence number TextView
        if (isVisible(mTextViewNumber)) {
            mTextViewNumber.layout(
                    leftBound,
                    textTopBound,
                    rightBound,
                    textTopBound + mTextViewNumberHeight);
            //Add 6 for ALPS00249076
            textTopBound += mTextViewNumberHeight + 6;
        }

        // Presence call type ImageView
        // private com.android.contacts.calllog.CallTypeIconsView mCallTypeIcon;
        int thirdLeftBound = leftBound;
        int viewWidth = 0;
        int thirdTopAdjust = textTopBound + bottomBound;
        int thirdTopBound = (thirdTopAdjust - mCallTypeIconHeight) / 2;
        if (isVisible(mCallTypeIcon)) {
            viewWidth = mCallTypeIcon.getMeasuredWidth();
            mCallTypeIcon.layout(thirdLeftBound,
                    thirdTopBound,
                    thirdLeftBound + viewWidth,
                    thirdTopBound + mCallTypeIconHeight);
            thirdLeftBound += (viewWidth + mCallLogInnerMarginDim);
        }

        // Presence call count TextView
        // private TextView mTextViewCallCount;
        if (isVisible(mTextViewCallCount)) {
            viewWidth = mTextViewCallCount.getMeasuredWidth();
            thirdTopBound = (thirdTopAdjust - mTextViewCallCountHeight) / 2;
            mTextViewCallCount.layout(thirdLeftBound,
                    thirdTopBound,
                    thirdLeftBound + viewWidth,
                    thirdTopBound + mTextViewCallCountHeight);
            thirdLeftBound += (viewWidth + mCallLogInnerMarginDim);
        }

        // Presence sim name TextView
        // private TextView mTextViewSimName;
        if (isVisible(mTextViewSimName)) {
            viewWidth = mTextViewSimName.getMeasuredWidth();
            // The max length is mSimNameWidthMaxDim (100dip)
            viewWidth = Math.min(viewWidth, mSimNameWidthMaxDim);
            thirdTopBound = (thirdTopAdjust - mTextViewSimNameHeight) / 2;
            mTextViewSimName.layout(thirdLeftBound,
                    thirdTopBound,
                    thirdLeftBound + viewWidth,
                    thirdTopBound + mTextViewSimNameHeight);
            thirdLeftBound += (viewWidth + mCallLogInnerMarginDim);
        }

        // Presence call time TextView
        // private TextView mTextViewCallTime;
        if (isVisible(mTextViewCallTime)) {
            viewWidth = mTextViewCallTime.getMeasuredWidth();
            thirdTopBound = (thirdTopAdjust - mTextViewCallTimeHeight) / 2;
            mTextViewCallTime.layout(thirdLeftBound,
                    thirdTopBound,
                    thirdLeftBound + viewWidth,
                    thirdTopBound + mTextViewCallTimeHeight);
        }
    }

    private void initPredefinedData() {
        mCallLogOuterMarginDim = 0;         // get from @dimen/call_log_outer_margin
        mCallLogInnerMarginDim = 0;         // get from @dimen/call_log_inner_margin
        mImageViewCallWidthDim = 0;       // get from @dimen/call_log_call_action_width
        mImageViewCallHeightDim = 0;      // get from @dimen/call_log_call_action_height
        mVerticalDividerHeightDim = 0;      // get from @demin/call_log_call_action_size
        mQuickContactPhotoWidthDim = 0;     // get from @dimen/call_log_list_contact_photo_size
        mQuickContactPhotoHeightDim = 0;    // get from @dimen/call_log_list_contact_photo_size
        mSimNameWidthMaxDim = 0;            // get from @dimen/calllog_list_item_simname_max_length
        mListItemQuickContactPaddingTop = 0;   // get from @dimen/calllog_list_item_quick_contact_padding_top
        mListItemQuickContactPaddingBottom = 0;// get from @dimen/calllog_list_item_quick_contact_padding_bottom
        mCheckBoxMultiSelWidthDim = 0;      // get from @dimen/calllog_multi_select_list_item_checkbox_width
        mCheckBoxMultiSelHeightDim = 0;     // get from @dimen/calllog_multi_select_list_item_checkbox_height
        
        mListItemPaddingLeftDim = 0;      // get from @dimen/calllog_list_margin_left
        mListItemPaddingRightDim = 0;     // get from @dimen/calllog_list_margin_right

        mNameWidthMaxDim = 0;             // get from @dimen/calllog_list_item_name_max_length
		mNumberWidthMaxDim = 0;           // get from @dimen/calllog_list_item_number_max_length

        if (null != mContext) {
            mCallLogOuterMarginDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_outer_margin);
            mCallLogInnerMarginDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_inner_margin);
            mImageViewCallWidthDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_call_action_width);
            mImageViewCallHeightDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_call_action_height);
            mVerticalDividerHeightDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_call_action_size);
            mQuickContactPhotoWidthDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_list_contact_photo_size);
            mQuickContactPhotoHeightDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.call_log_list_contact_photo_size);
            float size = getFontSize();
            if (size > (FONT_SIZE_EXTRA_LARGE - 0.01) && size < (FONT_SIZE_EXTRA_LARGE + 0.01)) {
                mSimNameWidthMaxDim = mContext.getResources().getDimensionPixelSize(
                        R.dimen.calllog_list_item_simname_max_length1);
            } else if (size > (FONT_SIZE_LARGE - 0.01) && size < (FONT_SIZE_LARGE + 0.01)) {
                mSimNameWidthMaxDim = mContext.getResources().getDimensionPixelSize(
                        R.dimen.calllog_list_item_simname_max_length2);
            } else {
                mSimNameWidthMaxDim = mContext.getResources().getDimensionPixelSize(
                        R.dimen.calllog_list_item_simname_max_length3);
            }
            mListItemQuickContactPaddingTop = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_item_quick_contact_padding_top);
            mListItemQuickContactPaddingBottom = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_item_quick_contact_padding_bottom);
            
            mCheckBoxMultiSelWidthDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_multi_select_list_item_checkbox_width);
            mCheckBoxMultiSelHeightDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_multi_select_list_item_checkbox_height);
            mListItemPaddingLeftDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_margin_left);
            mListItemPaddingRightDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_margin_right);
            mNameWidthMaxDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_item_name_max_length);
            mNumberWidthMaxDim = mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_item_number_max_length);
            
        } else {
            Log.e(TAG, "Error!!! - initPredefinedData() mContext is null!");
        }
    }
    
    protected boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * Loads the drawable for the vertical divider if it has not yet been loaded.
     */
    private void ensureVerticalDivider() {
        if (mDrawableVertialDivider == null) {
            mDrawableVertialDivider = mContext.getResources().getDrawable(
                    R.drawable.ic_divider_dashed_holo_dark);
            mVerticalDividerWidth = VERTICAL_DIVIDER_LEN;
        }
    }

    protected int layoutRightSide(int height, int topBound, int bottomBound, int rightBound) {
        // Put call button and vertical divider
        if (isVisible(mImageViewCall)) {
            int buttonWidth = mImageViewCall.getMeasuredWidth();
            rightBound -= buttonWidth;
            mImageViewCall.layout(
                    rightBound,
                    topBound,
                    rightBound + buttonWidth,
                    height - mHorizontalDividerHeight);
            mVerticalDividerVisible = true;
            ensureVerticalDivider();
            rightBound -= mVerticalDividerWidth;
            int iDividTopBond = (topBound + height - mVerticalDividerHeightDim) / 2;
            mDrawableVertialDivider.setBounds(
                    rightBound,
                    iDividTopBond,
                    rightBound + mVerticalDividerWidth,
                    iDividTopBond + mVerticalDividerHeightDim);
            rightBound -= mCallLogInnerMarginDim;
        } else {
            mVerticalDividerVisible = false;
        }

        return rightBound;
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

        // ToDo: to check the mViewHorizontalDivider.draw ???
//        if (mHorizontalDividerVisible) {
//            mViewHorizontalDivider.draw(canvas);
//        }

        if (mVerticalDividerVisible) {
            mDrawableVertialDivider.draw(canvas);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public void requestLayout() {
        // We will assume that once measured this will not need to resize
        // itself, so there is no need to pass the layout request to the parent
        // view (ListView).
        forceLayout();
    }

    /**
     * Sets the flag that determines whether a divider should drawn at the bottom
     * of the view.
     */
    public void setDividerVisible(boolean visible) {
        mHorizontalDividerVisible = visible;
    }

    public TextView getSectionDate() {
        if (null == mTextViewCallLogDate) {
            mTextViewCallLogDate = new TextView(mContext);
            mTextViewCallLogDate.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mTextViewCallLogDate.setTypeface(mTextViewCallLogDate.getTypeface(), Typeface.BOLD);
            mTextViewCallLogDate.setSingleLine(true);
            mTextViewCallLogDate.setGravity(Gravity.CENTER_VERTICAL);
            addView(mTextViewCallLogDate);
        }
        if (null == mViewHorizontalDateDivider) {
            mViewHorizontalDateDivider = new View(mContext);
            mViewHorizontalDateDivider.setBackgroundColor(mHorizontalDateDividerColor);
            addView(mViewHorizontalDateDivider);
        }

        mTextViewCallLogDate.setAllCaps(true);
        mCallLogDateVisible = true;

        return mTextViewCallLogDate;
    }
    
    /**
     *     private TextView mTextViewCallLogDate;
    // Horizontal divider between call log date and below item views.
    private int mHorizontalDateDividerHeight = 3;  // default 2dip
    private View mViewHorizontalDateDivider; // if Drawable not set use view replace
    // Other views
    private QuickContactBadge mQuickContactPhoto;
    private TextView mTextViewName;
    private TextView mTextViewNumber;
    private com.android.contacts.calllog.CallTypeIconsView mCallTypeIcon;
    private TextView mTextViewCallCount;
    private TextView mTextViewSimName;
    private TextView mTextViewCallTime;
    private ImageButton mImageButtonCall;
     */
    /**
     * Sets date section(as header) and makes it invisible if the date is null.
     */
    public void setSectionDate(String date) {
        if (!TextUtils.isEmpty(date)) {
            getSectionDate();
            mTextViewCallLogDate.setVisibility(View.VISIBLE);
            mViewHorizontalDateDivider.setVisibility(View.VISIBLE);
            mTextViewCallLogDate.setText(date);
            mCallLogDateVisible = true;
        } else {
            if (null != mTextViewCallLogDate) {
                mTextViewCallLogDate.setVisibility(View.GONE);
            }
            if (null != mViewHorizontalDateDivider) {
                mViewHorizontalDateDivider.setVisibility(View.GONE);
            }
            mCallLogDateVisible = false;
        }
    }

    /**
     * Returns the quick contact badge, creating it if necessary.
     */
    public QuickContactBadgeWithPhoneNumber getQuickContact() {
        if (null == mQuickContactPhoto) {
            mQuickContactPhoto = new QuickContactBadgeWithPhoneNumber(mContext, null, QUICK_CONTACT_BADGE_STYLE);
//            if (mTextViewName != null) {
//                mQuickContactPhoto.setContentDescription(mContext.getString(
//                        R.string.description_quick_contact_for, mTextViewName.getText()));
//            }

            // mQuickContactPhoto.setVisibility(View.VISIBLE);
            addView(mQuickContactPhoto);
        }
        return mQuickContactPhoto;
    }

    /**
     * Adds a call button using the supplied arguments as an id and tag.
     */
    public ImageView getCallButton() {
        if (null == mImageViewCall) {
            mImageViewCall = new DontPressWithParentImageView(mContext, null);
            mImageViewCall.setOnClickListener(mCallButtonClickListener);
            mImageViewCall.setBackgroundDrawable(mSelectableItemBackgroundDrawable);
            mImageViewCall.setImageResource(R.drawable.ic_ab_dialer_holo_dark);
            mImageViewCall.setPadding(mCallLogInnerMarginDim, mCallLogInnerMarginDim,
                    mCallLogInnerMarginDim, mCallLogInnerMarginDim);
            mImageViewCall.setScaleType(ScaleType.CENTER);
            // mImageButtonCall.setNextFocusLeftId(nextFocusLeftId);
            addView(mImageViewCall);

            if (null == mViewVertialDivider) {
                mViewVertialDivider = new View(mContext);
                mViewVertialDivider.setBackgroundResource(R.drawable.ic_divider_dashed_holo_dark);
                addView(mViewVertialDivider);
            }
            mImageViewCall.setVisibility(View.VISIBLE);
            mViewVertialDivider.setVisibility(View.VISIBLE);
        }

        return mImageViewCall;
    }

    public void setCallButton() {
        getCallButton();
    }

    private TruncateAt getTextEllipsis() {
        return TruncateAt.MARQUEE;
    }

    private void setMarqueeText(TextView textView, char[] text, int size) {
        if (TruncateAt.MARQUEE == getTextEllipsis()) {
            setMarqueeText(textView, new String(text, 0, size));
        } else {
            textView.setText(text, 0, size);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if ((event.isTouchEvent()) && (MotionEvent.ACTION_DOWN == event.getAction())) {
            float ix = event.getX();
            
            int leftSide = -1;
            int rightSide = -1;
            if (mImageViewCall != null) {
                rightSide = mImageViewCall.getLeft();
            }
            if (mCheckBoxMultiSel != null) {
                leftSide = mCheckBoxMultiSel.getLeft();
            } else {
                if (mQuickContactPhoto != null) {
                    leftSide = mQuickContactPhoto.getRight();
                }
            }
            Log.i(TAG, "onTouchEvent, rightSide=" + rightSide + ", leftSide=" + leftSide);
            if ((rightSide < 0 || rightSide == 0 || ix < rightSide) && (leftSide < 0 || ix > leftSide)) {
                return super.onTouchEvent(event);
            }
        }
        return true;
    }

    private void setMarqueeText(TextView textView, CharSequence text) {
        if (TruncateAt.MARQUEE == getTextEllipsis()) {
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
     * Returns the text view for the contact name, creating it if necessary.
     */
    public TextView getCallLogNameTextView() {
        if (null == mTextViewName) {
            mTextViewName = new TextView(mContext);
            mTextViewName.setSingleLine(true);
            // mTextViewName.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
            mTextViewName.setTextSize(DEFAULT_ITEM_NAME_TEXT_SIZE);
            mTextViewName.setTextColor(DEFAULT_ITEM_TEXT_COLOR);
            // mTextViewName.setEllipsize(getTextEllipsis());
            mTextViewName.setEllipsize(TruncateAt.END);
            mTextViewName.setMaxWidth(mNameWidthMaxDim);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mTextViewName.setActivated(isActivated());
            mTextViewName.setGravity(Gravity.CENTER_VERTICAL);
            addView(mTextViewName);
        }
        return mTextViewName;
    }

    /**
     * Adds or updates a text view for the call log name.
     */
    public void setCallLogName(char[] text, int size) {
        if ((null == text) || (0 == size)) {
            if (null != mTextViewName) {
                mTextViewName.setVisibility(View.GONE);
            }
        } else {
            getCallLogNameTextView();
            setMarqueeText(mTextViewName, text, size);
            mTextViewName.setVisibility(VISIBLE);
        }
    }

    /**
     * Adds or updates a text view for the call log name.
     */
    public void setCallLogName(String name) {
        if ((null == name) || (0 == name.length())) {
            if (null != mTextViewName) {
                mTextViewName.setVisibility(View.GONE);
            }
        } else {
            getCallLogNameTextView();
            mTextViewName.setText(name);
//            setMarqueeText(mTextViewName, name.toCharArray(), name.length());
            mTextViewName.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the number text, creating it if necessary.
     */
    public TextView getNumberTextView() {
        if (null == mTextViewNumber) {
            mTextViewNumber = new TextView(mContext);
            mTextViewNumber.setSingleLine(true);
            mTextViewNumber.setTextSize(DEFAULT_ITEM_NUMBER_TEXT_SIZE);
            mTextViewNumber.setTextColor(mSecondaryTextColor);
            // mTextViewNumber.setEllipsize(getTextEllipsis());
            mTextViewNumber.setEllipsize(TruncateAt.END);
            mTextViewNumber.setMaxWidth(mNameWidthMaxDim);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mTextViewNumber.setActivated(isActivated());
            addView(mTextViewNumber);
        }
        return mTextViewNumber;
    }

    /**
     * Adds or updates a text view for the phone number
     */
    public void setNumber(String number) {
        if ((null == number) || (0 == number.length())) {
            if (null != mTextViewNumber) {
                mTextViewNumber.setVisibility(View.GONE);
            }
        } else {
            getNumberTextView();
//            setMarqueeText(mTextViewNumber, number.toCharArray(), number.length());
            mTextViewNumber.setText(number);
            mTextViewNumber.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the number text, creating it if necessary.
     */
    public CallTypeIconsView getCallTypeIconView() {
        if (null == mCallTypeIcon) {
            mCallTypeIcon = new com.android.contacts.calllog.CallTypeIconsView(mContext);
            // mCallTypeIcon.setGravity(Gravity.CENTER_VERTICAL);
            mCallTypeIcon.setActivated(isActivated());
            addView(mCallTypeIcon);
        }
        return mCallTypeIcon;
    }

    /**
     * Adds or updates a text view for the phone number
     */
    public void setCallType(int callType) {
        if (null == mCallTypeIcon) {
            getCallTypeIconView();
        }

        if (null != mCallTypeIcon) {
            mCallTypeIcon.setVisibility(VISIBLE);
            mCallTypeIcon.set(callType);
        } else {
            Log.e(TAG, "Error!!! - setCallType() mCallTypeIcon is null!");
        }
        
    }

    /**
     * Returns the text view for the call count text, creating it if necessary.
     */
    public TextView getCallCountTextView() {
        if (null == mTextViewCallCount) {
            mTextViewCallCount = new TextView(mContext);
            mTextViewCallCount.setSingleLine(true);
            mTextViewCallCount.setTextSize(DEFAULT_ITEM_CALL_COUNT_TEXT_SIZE);
            mTextViewCallCount.setTextColor(mSecondaryTextColor);
            mTextViewCallCount.setGravity(Gravity.CENTER_VERTICAL);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mTextViewCallCount.setActivated(isActivated());
            mTextViewCallCount.setText("100000");
            addView(mTextViewCallCount);
        }
        return mTextViewCallCount;
    }

    /**
     * Adds or updates a text view for the call count
     */
    public void setCallCount(String count) {
        if ((null == count) || (0 == count.length())) {
            if (null != mTextViewCallCount) {
                mTextViewCallCount.setVisibility(View.GONE);
            }
        } else {
            getCallCountTextView();
            setMarqueeText(mTextViewCallCount, count.toCharArray(), count.length());
            mTextViewCallCount.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the SIM name text, creating it if necessary.
     */
    public TextView getSimNameTextView() {
        if (null == mTextViewSimName) {
            mTextViewSimName = new TextView(mContext);
            mTextViewSimName.setSingleLine(true);
            mTextViewSimName.setTextSize(mContext.getResources().getDimensionPixelSize(
                    R.dimen.calllog_list_item_simname_text_size));
            mTextViewSimName.setTextColor(DEFAULT_ITEM_TEXT_COLOR);
            mTextViewSimName.setGravity(Gravity.CENTER_VERTICAL);
            mTextViewSimName.setEllipsize(TruncateAt.MIDDLE);
            mTextViewSimName.setMaxWidth(mSimNameWidthMaxDim);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mTextViewSimName.setActivated(isActivated());
            mTextViewSimName.setText("China Mobile Realy?");
            addView(mTextViewSimName);
        }
        return mTextViewSimName;
    }

    /**
     * Adds or updates a text view for the SIM name
     */
    public void setSimName(String simname) {
        if ((null == simname) || (0 == simname.length())) {
            if (null != mTextViewSimName) {
                mTextViewSimName.setVisibility(View.GONE);
            }
        } else {
            getSimNameTextView();
            mTextViewSimName.setText(simname);
            // setMarqueeText(mTextViewSimName, simname.toCharArray(), simname.length());
            mTextViewSimName.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the SIM name text, creating it if necessary.
     */
    public TextView getCallTimeTextView() {
        if (null == mTextViewCallTime) {
            mTextViewCallTime = new TextView(mContext);
            mTextViewCallTime.setSingleLine(true);
            mTextViewCallTime.setTextSize(DEFAULT_ITEM_CALL_TIME_TEXT_SIZE);
            mTextViewCallTime.setTextColor(mSecondaryTextColor);
            mTextViewCallTime.setGravity(Gravity.CENTER_VERTICAL);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mTextViewCallTime.setActivated(isActivated());
            mTextViewCallTime.setText("9:00 pm");
            addView(mTextViewCallTime);
        }
        return mTextViewCallTime;
    }

    /**
     * Adds or updates a text view for the SIM name
     */
    public void setCallTime(String calltime) {
        if ((null == calltime) || (0 == calltime.length())) {
            if (null != mTextViewCallTime) {
                mTextViewCallTime.setVisibility(View.GONE);
            }
        } else {
            getCallTimeTextView();
            mTextViewCallTime.setText(calltime);
            // setMarqueeText(mTextViewSimName, simname.toCharArray(), simname.length());
            mTextViewCallTime.setVisibility(VISIBLE);
        }
    }
    
    public View getHorizontalDivider() {
        if (null == mViewHorizontalDivider) {
            mViewHorizontalDivider = new View(mContext);
            mViewHorizontalDivider.setBackgroundColor(mHorizontalDateDividerColor);
            addView(mViewHorizontalDivider);
        }

        return mViewHorizontalDivider;
    }

    /**
     * Returns the check box
     */
    public CheckBox getCheckBoxMultiSel() {
        if (null == mCheckBoxMultiSel) {
            mCheckBoxMultiSel = new CheckBox(mContext);
            // Manually call setActivated() since this view may be added after the first
            // setActivated() call toward this whole item view.
            mCheckBoxMultiSel.setActivated(isActivated());
            addView(mCheckBoxMultiSel);
        }
        return mCheckBoxMultiSel;
    }

    /**
     * Adds or set check box visible or not
     */
    public void setCheckBoxMultiSel(boolean focusable, boolean clickable) {
        getCheckBoxMultiSel();
        mCheckBoxMultiSel.setFocusable(focusable);
        mCheckBoxMultiSel.setClickable(clickable);
        mCheckBoxMultiSel.setVisibility(VISIBLE);
    }
    
    public static float getFontSize() {
        Configuration mCurConfig = new Configuration();
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        Log.w(TAG, "getFontSize(), Font size is " + mCurConfig.fontScale);
        return mCurConfig.fontScale;

    }
}
