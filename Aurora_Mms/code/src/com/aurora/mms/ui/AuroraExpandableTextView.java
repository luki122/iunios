package com.aurora.mms.ui;


import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.ui.MessageListItem;
import com.aurora.mms.util.AuroraLinkMovementMethod;
// Aurroa xuyong 2016-01-25 added for bug #18263 start
import com.aurora.mms.util.Utils;
// Aurroa xuyong 2016-01-25 added for bug #18263 end

public class AuroraExpandableTextView extends LinearLayout implements View.OnClickListener {

    public static final String TAG = "AuroraExpandableTextView";

    /* The default line count */
    private static final int MAX_COLLAPSED_LINES = 15;

    /* The default animation duration */
    private static final int DEFAULT_ANIM_DURATION = 300;

    /* The default alpha value when the animation starts */
    private static final float DEFAULT_ANIM_ALPHA_START = 0.8f;

    protected TextView mContent;

    protected TextView mTip;

    private boolean mRelayout;

    private boolean mCollapsed = true;

    private int mCollapsedHeight;

    private int mTextHeightWithMaxLines;

    private int mMaxCollapsedLines;

    private int mMarginBetweenTxtAndBottom;

    private String mExpandString;
    private String mCollapseString;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;

    /* For saving collapsed status when used in ListView */
    private static SparseBooleanArray mCollapsedStatus = new SparseBooleanArray();
    // Aurroa xuyong 2016-01-25 modified for bug #18263 start
    private int mPositionInParent = -1;
    // Aurroa xuyong 2016-01-25 modified for bug #18263 end
    private Context mContext;

    public AuroraExpandableTextView(Context context) {
        this(context, null);
        mContext = context;
    }

    public AuroraExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AuroraExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
        mContext = context;
    }


    @Override
    public void setOrientation(int orientation){
        // only support vertical oritation
        super.setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public void onClick(View view) {
        if (mTip.getVisibility() != View.VISIBLE) {
            return;
        }

        if (mTip.getText().equals(mContext.getString(R.string.aurora_cp_id_num))) {
            Message msg = mHandler.obtainMessage(MessageListItem.ITEM_COPY_IDENTIFY_NUM);
            msg.obj = mIdCode;
            msg.sendToTarget();
            return;
        }
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        if (mCollapsedStatus != null) {
            mCollapsedStatus.put(mPositionInParent, !mCollapsed);
        }
        mTip.setText(!mCollapsed ? mExpandString : mCollapseString);
        // Aurroa xuyong 2016-01-25 modified for bug #18263 end
        ContentValues values = new ContentValues(1);
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        if (!mCollapsed) {
        // Aurroa xuyong 2016-01-25 modified for bug #18263 end
            values.put("fold", 1);
        } else {
            values.put("fold", 0);
        }
        if (mMsgUri != null) {
            mContext.getContentResolver().update(mMsgUri, values, null, null);
        }

        // mark that the animation is in progress
        mAnimating = true;

        Animation animation;
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        if (!mCollapsed) {
            if (mCollapsedHeight != 0) {
                animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
            } else {
                animation = new ExpandCollapseAnimation(this, getHeight(), getCollapedTextViewHeight(mContent));
            }
        // Aurroa xuyong 2016-01-25 modified for bug #18263 end
        } else {
            animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() +
                    mTextHeightWithMaxLines - mContent.getHeight());
        }

        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                applyAlphaAnimation(mContent, mAnimAlphaStart);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // clear animation here to avoid repeated applyTransformation() calls
                clearAnimation();
                // clear the animation flag
                mAnimating = false;
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        clearAnimation();
        startAnimation(animation);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // while an animation is in progress, intercept all the touch events to children to
        // prevent extra clicks during the animation
        return mAnimating;
    }

    @Override
    protected void onFinishInflate() {
        findViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If no change, measure and return
        if (!mRelayout || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout = false;

        // Setup with optimistic case
        // i.e. Everything fits. No button needed
        mTip.setVisibility(View.GONE);
        mContent.setMaxLines(Integer.MAX_VALUE);

        // Measure
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // If the text fits in collapsed mode, we are done.
        if (mContent.getLineCount() <= mMaxCollapsedLines) {
            // Aurroa xuyong 2016-01-25 added for bug #18263 start
            if (mIdCode != null) {
                mTip.setText(getContext().getString(R.string.aurora_cp_id_num));
                mTip.setVisibility(VISIBLE);
            }
            // Aurroa xuyong 2016-01-25 added for bug #18263 end
            return;
        }

        // Saves the text height w/ max lines
        mTextHeightWithMaxLines = getRealTextViewHeight(mContent);

        // Doesn't fit in collapsed mode. Collapse text view as needed. Show
        // button.
        if (mCollapsed) {
            mContent.setMaxLines(mMaxCollapsedLines);
        }
        // Aurroa xuyong 2016-01-25 added for bug #18263 start
        mTip.setText(mCollapsed ? mExpandString : mCollapseString);
        // Aurroa xuyong 2016-01-25 added for bug #18263 end
        mTip.setVisibility(View.VISIBLE);

        // Re-measure with new setup
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mCollapsed) {
            // Gets the margin between the TextView's bottom and the ViewGroup's bottom
            mContent.post(new Runnable() {
                @Override
                public void run() {
                    mMarginBetweenTxtAndBottom = getHeight() - mContent.getHeight();
                }
            });
            // Saves the collapsed height of this ViewGroup
            mCollapsedHeight = getMeasuredHeight();
        }
    }

    public void setTransformationMethod(HideReturnsTransformationMethod instance) {
        if (mContent != null) {
            mContent.setTransformationMethod(instance);
        }
    }

    public void setMovementMethod(AuroraLinkMovementMethod instance) {
        if (mContent != null) {
            mContent.setMovementMethod(instance);
        }
    }

    public void setMaxWidth(int value) {
        if (mContent != null) {
            mContent.setMaxWidth(value);
        }
    }

    private Handler mHandler;
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setText(CharSequence text) {
        mRelayout = true;
        mContent.setText(text);
        mContent.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private String mIdCode;

    public void setText(CharSequence text, int position, boolean status) {
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        mPositionInParent = position;
        // Aurroa xuyong 2016-01-25 modified for bug #18263 end
        boolean isCollapsed = mCollapsedStatus.get(position, true);
        if (!isCollapsed && status) {
            isCollapsed = false;
        } else {
            isCollapsed = status;
        }
        mCollapsedStatus.put(position, status);
        clearAnimation();
        mCollapsed = isCollapsed;
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        //mTip.setText(mCollapsed ? mExpandString : mCollapseString);
        // Aurroa xuyong 2016-01-25 modified for bug #18263 start
        setText(text);
        // Aurroa xuyong 2016-01-25 added for bug #18263 start
        mIdCode = Utils.getUsefulCode(this.getContext(), text.toString());
        // Aurroa xuyong 2016-01-25 added for bug #18263 end
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        requestLayout();
    }

    public static void clearCollapsedStatus() {
        if (mCollapsedStatus.size() >= 0) {
            mCollapsedStatus.clear();
        }
    }

    public CharSequence getText() {
        if (mContent == null) {
            return "";
        }
        return mContent.getText();
    }

    public void setCollapsed(boolean status) {
        mCollapsed = status;
        invalidate();
    }

    private Uri mMsgUri;
    public void setMsgUri(Uri uri) {
        mMsgUri = uri;
    }

    public TextView getTextView() {
        return mContent;
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.AuroraExpandableTextView);
        mMaxCollapsedLines = typedArray.getInt(R.styleable.AuroraExpandableTextView_max_collapsed_lines, MAX_COLLAPSED_LINES);
        mAnimationDuration = typedArray.getInt(R.styleable.AuroraExpandableTextView_anim_duration, DEFAULT_ANIM_DURATION);
        mAnimAlphaStart = typedArray.getFloat(R.styleable.AuroraExpandableTextView_anim_alpha_start, DEFAULT_ANIM_ALPHA_START);
        if (mExpandString == null) {
            mExpandString = getContext().getString(R.string.aurora_show_detail_unfold);
        }
        if (mCollapseString == null) {
            mCollapseString = getContext().getString(R.string.aurora_show_detail_fold);
        }
        typedArray.recycle();
        // enforces vertical orientation
        setOrientation(LinearLayout.VERTICAL);
        // default visibility is gone
        setVisibility(GONE);
    }

    private void findViews() {
        mContent = (TextView) findViewById(R.id.aurora_expand_content);
        mTip = (TextView) findViewById(R.id.aurora_expand_tip);
        mTip.setText(mCollapsed ? mExpandString : mCollapseString);
        mTip.setOnClickListener(this);
    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        if (isPostHoneycomb()) {
            view.setAlpha(alpha);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
            // make it instant
            alphaAnimation.setDuration(0);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    private int getRealTextViewHeight(TextView textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return textHeight + padding;
    }
    // Aurroa xuyong 2016-01-25 added for bug #18263 start
    private int getCollapedTextViewHeight(TextView textView) {
        int textHeight = textView.getLayout().getLineTop(MAX_COLLAPSED_LINES);
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        int tipHeight = mTip.getHeight();
        return textHeight + padding + tipHeight;
    }
    // Aurroa xuyong 2016-01-25 added for bug #18263 end
    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int)((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            mContent.setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
                applyAlphaAnimation(mContent, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
            }
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize( int width, int height, int parentWidth, int parentHeight ) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds( ) {
            return true;
        }
    };
}
