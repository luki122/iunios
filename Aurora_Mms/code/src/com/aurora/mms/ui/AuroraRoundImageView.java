package com.aurora.mms.ui;
//Aurora xuyong 2013-10-11 created for aurora's new feature
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
// Aurora xuyong 2014-04-29 added for aurora's new feature start
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
// Aurora xuyong 2014-04-29 added for aurora's new feature end
import android.util.AttributeSet;
import android.widget.ImageView;
// Aurora xuyong 2014-04-29 added for aurora's new feature start
import android.widget.TextView;
// Aurora xuyong 2014-04-29 added for aurora's new feature end
import com.android.mms.R;

public class AuroraRoundImageView extends ImageView{

    public static final String TAG = "AuroraRoundImageView";

    public static final int DEFAULT_RADIUS = 0;
    public static final int DEFAULT_BORDER = 0;

    private int mCornerRadius;
    private int mBorderWidth;
    private ColorStateList mBorderColor;

    private boolean mRoundBackground = false;
    private boolean mOval = false;

    private Drawable mDrawable;
    private Drawable mBackgroundDrawable;

    private ScaleType mScaleType;

    private static final ScaleType[] sScaleTypeArray = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    public AuroraRoundImageView(Context context) {
        super(context);
        mCornerRadius = DEFAULT_RADIUS;
        mBorderWidth = DEFAULT_BORDER;
        mBorderColor = ColorStateList.valueOf(AuroraRoundDrawable.DEFAULT_BORDER_COLOR);
    }

    public AuroraRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuroraRoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AuroraRoundImageView, defStyle, 0);

        int index = a.getInt(R.styleable.AuroraRoundImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }

        mCornerRadius = a.getDimensionPixelSize(R.styleable.AuroraRoundImageView_corner_radius, -1);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.AuroraRoundImageView_border_width, -1);

        // don't allow negative values for radius and border
        if (mCornerRadius < 0) {
            mCornerRadius = DEFAULT_RADIUS;
        }
        if (mBorderWidth < 0) {
            mBorderWidth = DEFAULT_BORDER;
        }

        mBorderColor = a.getColorStateList(R.styleable.AuroraRoundImageView_border_color);
        if (mBorderColor == null) {
            mBorderColor = ColorStateList.valueOf(AuroraRoundDrawable.DEFAULT_BORDER_COLOR);
        }

        mRoundBackground = a.getBoolean(R.styleable.AuroraRoundImageView_round_background, false);
        mOval = a.getBoolean(R.styleable.AuroraRoundImageView_is_oval, false);

        if (mDrawable instanceof AuroraRoundDrawable) {
            updateDrawableAttrs((AuroraRoundDrawable) mDrawable);
        }

        if (mRoundBackground) {
            if (!(mBackgroundDrawable instanceof AuroraRoundDrawable)) {
                // try setting background drawable now that we got the mRoundBackground param
                setBackgroundDrawable(mBackgroundDrawable);
            }
            if (mBackgroundDrawable instanceof AuroraRoundDrawable) {
                updateDrawableAttrs((AuroraRoundDrawable) mBackgroundDrawable);
            }
        }

        a.recycle();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            switch (scaleType) {
                case CENTER:
                case CENTER_CROP:
                case CENTER_INSIDE:
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
                case FIT_XY:
                    super.setScaleType(ScaleType.FIT_XY);
                    break;
                default:
                    super.setScaleType(scaleType);
                    break;
            }

            if (mDrawable instanceof AuroraRoundDrawable
                    && ((AuroraRoundDrawable) mDrawable).getScaleType() != scaleType) {
                ((AuroraRoundDrawable) mDrawable).setScaleType(scaleType);
            }

            if (mBackgroundDrawable instanceof AuroraRoundDrawable
                    && ((AuroraRoundDrawable) mBackgroundDrawable).getScaleType() != scaleType) {
                ((AuroraRoundDrawable) mBackgroundDrawable).setScaleType(scaleType);
            }
            setWillNotCacheDrawing(true);
            requestLayout();
            invalidate();
        }
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see android.widget.ImageView.ScaleType
     */
    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    // Aurora xuyong 2014-04-29 deleted for aurora's new feature start
    /*@Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null) {
            mDrawable = AuroraRoundDrawable.fromDrawable(drawable, mCornerRadius, mBorderWidth, mBorderColor, mOval);
            updateDrawableAttrs((AuroraRoundDrawable) mDrawable);
        } else {
            mDrawable = null;
        }
        super.setImageDrawable(mDrawable);
    }*/
    // Aurora xuyong 2014-04-29 deleted for aurora's new feature end

    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            mDrawable = new AuroraRoundDrawable(bm, mCornerRadius, mBorderWidth, mBorderColor, mOval);
            updateDrawableAttrs((AuroraRoundDrawable) mDrawable);
        } else {
            mDrawable = null;
        }
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    private void updateDrawableAttrs(AuroraRoundDrawable drawable) {
        drawable.setScaleType(mScaleType);
        drawable.setCornerRadius(mCornerRadius);
        drawable.setBorderWidth(mBorderWidth);
        drawable.setBorderColors(mBorderColor);
        drawable.setOval(mOval);
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        if (mRoundBackground && background != null) {
            mBackgroundDrawable = AuroraRoundDrawable.fromDrawable(background, mCornerRadius, mBorderWidth, mBorderColor, mOval);
            updateDrawableAttrs((AuroraRoundDrawable) mBackgroundDrawable);
        } else {
            mBackgroundDrawable = background;
        }
        super.setBackgroundDrawable(mBackgroundDrawable);
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public int getBorder() {
        return mBorderWidth;
    }

    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    public void setCornerRadius(int radius) {
        if (mCornerRadius == radius) {
            return;
        }

        mCornerRadius = radius;
        if (mDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mDrawable).setCornerRadius(radius);
        }
        if (mRoundBackground && mBackgroundDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mBackgroundDrawable).setCornerRadius(radius);
        }
    }

    public void setBorderWidth(int width) {
        if (mBorderWidth == width) {
            return;
        }

        mBorderWidth = width;
        if (mDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mDrawable).setBorderWidth(width);
        }
        if (mRoundBackground && mBackgroundDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mBackgroundDrawable).setBorderWidth(width);
        }
        invalidate();
    }

    public void setBorderColor(int color) {
        setBorderColors(ColorStateList.valueOf(color));
    }

    public void setBorderColors(ColorStateList colors) {
        if (mBorderColor.equals(colors)) {
            return;
        }

        mBorderColor = colors != null ? colors : ColorStateList.valueOf(AuroraRoundDrawable.DEFAULT_BORDER_COLOR);
        if (mDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mDrawable).setBorderColors(colors);
        }
        if (mRoundBackground && mBackgroundDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mBackgroundDrawable).setBorderColors(colors);
        }
        if (mBorderWidth > 0) {
            invalidate();
        }
    }

    public void setOval(boolean oval) {
        mOval = oval;
        if (mDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mDrawable).setOval(oval);
        }
        if (mRoundBackground && mBackgroundDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mBackgroundDrawable).setOval(oval);
        }
        invalidate();
    }

    public boolean isOval() {
        return mOval;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setImageDrawable(getDrawable());
    }

    public boolean isRoundBackground() {
        return mRoundBackground;
    }

    public void setRoundBackground(boolean roundBackground) {
        if (mRoundBackground == roundBackground) {
            return;
        }

        mRoundBackground = roundBackground;
        if (roundBackground) {
            if (mBackgroundDrawable instanceof AuroraRoundDrawable) {
                updateDrawableAttrs((AuroraRoundDrawable) mBackgroundDrawable);
            } else {
                setBackgroundDrawable(mBackgroundDrawable);
            }
        } else if (mBackgroundDrawable instanceof AuroraRoundDrawable) {
            ((AuroraRoundDrawable) mBackgroundDrawable).setBorderWidth(0);
            ((AuroraRoundDrawable) mBackgroundDrawable).setCornerRadius(0);
        }

        invalidate();
    }
    // Aurora xuyong 2014-04-29 added for aurora's new feature start
    @Override
    protected void onDetachedFromWindow() {
        // This has been detached from Window, so clear the drawable
        setImageDrawable(null);

        super.onDetachedFromWindow();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        // Keep hold of previous Drawable
        final Drawable previousDrawable = getDrawable();
        
        if (drawable != null) {
            mDrawable = AuroraRoundDrawable.fromDrawable(drawable, mCornerRadius, mBorderWidth, mBorderColor, mOval);
            updateDrawableAttrs((AuroraRoundDrawable) mDrawable);
        } else {
            mDrawable = null;
        }

        // Call super to set new Drawable
        super.setImageDrawable(drawable);

        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true);

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof AuroraRoundDrawable) {
            // The drawable is a CountingBitmapDrawable, so notify it
            ((AuroraRoundDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }
    
    private TextView mDurationView;
    private Uri mUri;
    
    public void bindTextView (TextView textView) {
        mDurationView = textView;
    }
    
    public void setDuration(String duration) {
        if (mDurationView != null) {
            mDurationView.setText(duration);
        }
    }
    
    public void setImageUri(Uri uri) {
        mUri = uri;
    }
    
    public Uri getUri() {
        return mUri;
    }
    // Aurora xuyong 2014-04-29 added for aurora's new feature end
}
