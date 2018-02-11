package com.aurora.mms.ui;
// Aurora xuyong 2015-12-09 created for aurora 2.0 new feature
import android.content.ActivityNotFoundException;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new end
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature
import android.database.Cursor;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new end
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Shader.TileMode;
import android.net.Uri;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
// Aurora xuyong 2016-03-29 added for bug #21898 start
import android.os.Handler;
// Aurora xuyong 2016-03-29 added for bug #21898 end
import android.provider.CallLog;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
import android.widget.ImageView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
import com.android.mms.R;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
import com.privacymanage.service.AuroraPrivacyUtils;
// Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end

public class AuroraMmsQuickContactBadge extends ImageView implements OnClickListener {
    // Aurora xuyong 2016-01-08 added for bug #18250 start
    public static final String CONTACT_INFO_DIVIDER = String.valueOf('\1');
    // Aurora xuyong 2016-01-08 added for bug #18250 end
    private static final long PRIVACY_DEFAULT = 0l;
    private long mPrivacy = PRIVACY_DEFAULT;

    private int mSideLength = -1;
    private int mCircleDepth = 0;
    private int mCircleColor;

    private float SCALE_DIAMETER_ONE = 40;
    private float SCALE_DIAMETER_DOUBLE = 30;
    private float SCALE_DIAMETER_TRIPLE = 24;
    private float SCALE_DIAMETER_QUADRA = 20;

    public Drawable mOverlay;
    private int mClickOverlayColor;

    private Drawable mDefaultSrc = null;

    private int mTotalContactCount = 0;
    // Aurora xuyong 2016-01-08 modified for bug #18250 start
    private HashMap<String, Drawable> mAvatarMap = new HashMap<String, Drawable>();
    // Aurora xuyong 2016-01-08 modified for bug #18250 end
    // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
    private ArrayList<UriDrawable> mDrawableList = new ArrayList<UriDrawable>();
    // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
    //private Uri mContactUri = null;
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
    private boolean mHasAssigned = false;

    public AuroraMmsQuickContactBadge(Context context) {
        this(context, null);
    }

    public AuroraMmsQuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuroraMmsQuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AuroraMmsQuickContactBadge(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AuroraMmsQuickContactBadge, defStyleAttr, 0);
        mOverlay = a.getDrawable(R.styleable.AuroraMmsQuickContactBadge_overlay);
        mClickOverlayColor = a.getColor(R.styleable.AuroraMmsQuickContactBadge_overlay_color, -1);
        mDefaultSrc = a.getDrawable(R.styleable.AuroraMmsQuickContactBadge_default_src);
        mSideLength = a.getDimensionPixelSize(R.styleable.AuroraMmsQuickContactBadge_side_length, -1);
        mCircleColor = a.getColor(R.styleable.AuroraMmsQuickContactBadge_circle_color, -1);
        mCircleDepth = a.getDimensionPixelSize(R.styleable.AuroraMmsQuickContactBadge_circle_depth, 0);
        a.recycle();

        setOnClickListener(this);
    }

    public void setPrivacy(long privacy) {
        mPrivacy = privacy;
    }

    public void assignContactUri(Uri contactUri) {
        if (!mHasAssigned) {
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
            //mContactUri = contactUri;
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
            mHasAssigned = true;
        }
    }
    // Aurora xuyong 2016-01-08 modified for bug #18250 start
    public void addContactDrawable(String contactInfo, Drawable drawable) {
        if (mAvatarMap.get(contactInfo) == null) {
    // Aurora xuyong 2016-01-08 modified for bug #18250 end
        // Aurora xuyong 2015-01-08 added for bug #18237 start
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
            /*if (drawable == null) {
                // Aurora xuyong 2016-01-08 modified for bug #18250 start
                mAvatarMap.put(contactInfo, mDefaultSrc);
                // Aurora xuyong 2016-01-08 modified for bug #18250 start
            } else {*/
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
                // Aurora xuyong 2016-01-08 modified for bug #18250 start
                mAvatarMap.put(contactInfo, drawable);
                // Aurora xuyong 2016-01-08 modified for bug #18250 start
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
            //}
            // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
        // Aurora xuyong 2016-01-08 modified for bug #18250 start
        } else if (!mAvatarMap.get(contactInfo).equals(drawable)) {
        // Aurora xuyong 2016-01-08 modified for bug #18250 end
        // Aurora xuyong 2015-01-08 added for bug #18237 end
            // Aurora xuyong 2016-01-08 modified for bug #18250 start
            mAvatarMap.put(contactInfo, drawable);
            // Aurora xuyong 2016-01-08 modified for bug #18250 end
        } else {
            return;
        }
        if (mAvatarMap.size() == mTotalContactCount) {
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
            mDrawableList.clear();
            for (String key : mAvatarMap.keySet()) {
                mDrawableList.add(new UriDrawable(mAvatarMap.get(key), getContactUriByString(key), getContactNumberByString(key), getContactPrivacyByString(key)));
            }
            if (mAvatarMap.size() <= 0) {
                return;
            }
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
            invalidate();
        }
    }
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
    //private Drawable mXyDrawable = null;

    //public void setImageDrawable(Drawable drawable, String number) {
        //mXyDrawable = drawable;
    //}
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
    public void setContactCount(int count) {
        // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature start
        //mTotalContactCount = count;
        // Aurora xuyong 2016-01-12 deleted for aurora 2.0 new feature end
        clearContactCache();
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
        mTotalContactCount = count;
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
    }

    public void clearContactCache() {
        mAvatarMap.clear();
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
        mDrawableList.clear();
        mTotalContactCount = 0;
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
    }

    private void drawContacts(Canvas canvas, ArrayList<UriDrawable> list) {
        float scalepram = 1f;
        switch (list.size()) {
            case 0:
                // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature start
                scalepram = SCALE_DIAMETER_ONE / SCALE_DIAMETER_ONE;
                zeroDraw(canvas, scalepram);
                // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature end
                break;
            case 1:
                scalepram = SCALE_DIAMETER_ONE / SCALE_DIAMETER_ONE;
                oneDraw(canvas, list, scalepram);
                break;
            case 2:
                scalepram = SCALE_DIAMETER_DOUBLE / SCALE_DIAMETER_ONE;
                doubleDraw(canvas, list, scalepram);
                break;
            case 3:
                scalepram = SCALE_DIAMETER_TRIPLE / SCALE_DIAMETER_ONE;
                tripleDraw(canvas, list, scalepram);
                break;
            case 4:
            default:
                scalepram = SCALE_DIAMETER_QUADRA / SCALE_DIAMETER_ONE;
                quadraDraw(canvas, list, scalepram);
                break;
        }
    }

    private void drawBitmap(Canvas canvas, Drawable drawable, int contactCount, int index, float scalepram, boolean needBorder, boolean needOverlay) {
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        Matrix matrix = new Matrix();
        float bitmapDiameter = mSideLength * scalepram;
        float scale = (float)bitmapDiameter / bitmap.getHeight();
        matrix.postScale(scale, scale);
        canvas.translate(getTranslateX(contactCount, index, scalepram), getTranslateY(contactCount, index, scalepram));
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,true);
        BitmapShader bitmapShader = new BitmapShader(resizeBmp, TileMode.CLAMP, TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
        canvas.drawCircle(bitmapDiameter / 2, bitmapDiameter / 2, bitmapDiameter / 2, paint);
        if (needBorder) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setColor(mCircleColor);
            paint.setStrokeWidth(mCircleDepth);
            canvas.drawCircle(bitmapDiameter / 2, bitmapDiameter / 2, bitmapDiameter/ 2, paint);
        }
        if (needOverlay) {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setColor(mClickOverlayColor);
            canvas.drawCircle(bitmapDiameter / 2, bitmapDiameter / 2, bitmapDiameter/ 2, paint);
        }
    }

    private float getTranslateX(int contactCount, int index, float scalepram) {
        switch (contactCount) {
            case 0:
            case 1:
                return 0;
            case 2:
                switch (index) {
                    case FIRST:
                        return mSideLength * (1 - scalepram);
                    case SECOND:
                        return 0;
                }
            case 3:
                switch (index) {
                    case FIRST:
                        return mSideLength * (1 - scalepram);
                    case SECOND:
                        return mSideLength * (1 - scalepram) / 2;
                    case THIRD:
                        return 0;
                }
            case 4:
            default:
                switch (index) {
                    case FIRST:
                        return 0;
                    case SECOND:
                        return mSideLength * (1 - scalepram);
                    case THIRD:
                        return 0;
                    case FOURTH:
                        return mSideLength * (1 - scalepram);
                }
        }
        return 0;
    }

    private float getTranslateY(int contactCount, int index, float scalepram) {
        switch (contactCount) {
            case 1:
                return 0;
            case 2:
                switch (index) {
                    case FIRST:
                    case SECOND:
                        return mSideLength * (1 - scalepram) / 2;
                }
            case 3:
                switch (index) {
                    case FIRST:
                        return mSideLength * (1 - scalepram);
                    case SECOND:
                        return 0;
                    case THIRD:
                        return mSideLength * (1 - scalepram);
                }
            case 4:
            default:
                switch (index) {
                    case FIRST:
                        return 0;
                    case SECOND:
                        return 0;
                    case THIRD:
                        return mSideLength * (1 - scalepram);
                    case FOURTH:
                        return mSideLength * (1 - scalepram);
                }
        }
        return 0;
    }

    private int drawOverlayIndex(int contactcount, float scalepram) {
        switch (contactcount) {
            case 1:
                 return FIRST;
            case 2:
                 if (mClickX < scalepram * mSideLength / 2) {
                     return SECOND;
                 } else {
                     return FIRST;
                 }
            case 3:
                if (mClickX < scalepram * mSideLength && mClickY > (1 - scalepram) * mSideLength) {
                    return THIRD;
                } else if (mClickY < (1 - scalepram) * mSideLength) {
                    return SECOND;
                } else if (mClickX > scalepram * mSideLength && mClickY > (1 - scalepram) * mSideLength) {
                    return FIRST;
                }
            case 4:
            default:
                if (mClickX < scalepram * mSideLength && mClickY < scalepram * mSideLength) {
                    return FIRST;
                } else if (mClickX > scalepram * mSideLength && mClickY < scalepram * mSideLength) {
                    return SECOND;
                } else if (mClickX < scalepram * mSideLength && mClickY > scalepram * mSideLength) {
                    return THIRD;
                } else if (mClickX > scalepram * mSideLength && mClickY > scalepram * mSideLength) {
                    return FOURTH;
                }

        }
        return -1;
    }

    private final int FIRST = 0;
    private final int SECOND = 1;
    private final int THIRD = 2;
    private final int FOURTH = 3;
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
    /*private void xyDrawableDraw(Canvas canvas) {
        float scalepram = SCALE_DIAMETER_ONE / SCALE_DIAMETER_ONE;
        mClickIndex = drawOverlayIndex(1, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        Drawable drawable = mXyDrawable;
        if (FIRST == mClickIndex) {
            mUriDrawable = null;
        }
        drawBitmap(canvas, drawable, 1, FIRST, scalepram, false, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }*/
    // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
    // Aurora xuyong 2016-01-28 added for xy-smartsms end
    // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature start
    private void zeroDraw(Canvas canvas, float scalepram) {
        mClickIndex = drawOverlayIndex(1, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        Drawable drawable = mDefaultSrc;
        if (FIRST == mClickIndex) {
            mUriDrawable = null;
        }
        drawBitmap(canvas, drawable, 1, FIRST, scalepram, false, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }
    // Aurora xuyong 2016-01-22 added for aurora 2.0 new feature end 
    private void oneDraw(Canvas canvas, ArrayList<UriDrawable> list, float scalepram) {
        int contactNum = list.size();
        mClickIndex = drawOverlayIndex(contactNum, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        UriDrawable uriDrawable = list.get(FIRST);
        Drawable drawable = uriDrawable.getDrawable();
        if (FIRST == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, FIRST, scalepram, false, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }

    private void doubleDraw(Canvas canvas, ArrayList<UriDrawable> list, float scalepram) {
        int contactNum = list.size();
        mClickIndex = drawOverlayIndex(contactNum, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        UriDrawable uriDrawable = list.get(FIRST);
        Drawable drawable = uriDrawable.getDrawable();
        if (FIRST == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, FIRST, scalepram, true, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(SECOND);
        drawable = uriDrawable.getDrawable();
        if (SECOND == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, SECOND, scalepram, true, SECOND == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }

    private void tripleDraw(Canvas canvas, ArrayList<UriDrawable> list, float scalepram) {
        int contactNum = list.size();
        mClickIndex = drawOverlayIndex(contactNum, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        UriDrawable uriDrawable = list.get(FIRST);
        Drawable drawable = uriDrawable.getDrawable();
        if (FIRST == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, FIRST, scalepram, true, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(SECOND);
        drawable = uriDrawable.getDrawable();
        if (SECOND == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, SECOND, scalepram, true, SECOND == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(THIRD);
        drawable = uriDrawable.getDrawable();
        if (THIRD == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, THIRD, scalepram, true, THIRD == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }

    private void quadraDraw(Canvas canvas, ArrayList<UriDrawable> list, float scalepram) {
        int contactNum = list.size();
        mClickIndex = drawOverlayIndex(contactNum, scalepram);
        int saveCount = canvas.getSaveCount();
        canvas.save();
        UriDrawable uriDrawable = list.get(FIRST);
        Drawable drawable = uriDrawable.getDrawable();
        if (FIRST == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, FIRST, scalepram, false, FIRST == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(SECOND);
        drawable = uriDrawable.getDrawable();
        if (SECOND == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, SECOND, scalepram, false, SECOND == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(THIRD);
        drawable = uriDrawable.getDrawable();
        if (THIRD == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, THIRD, scalepram, false, THIRD == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.getSaveCount();
        canvas.save();
        uriDrawable = list.get(FOURTH);
        drawable = uriDrawable.getDrawable();
        if (FOURTH == mClickIndex) {
            mUriDrawable = uriDrawable;
        }
        if (drawable == null) {
            drawable = mDefaultSrc;
        }
        drawBitmap(canvas, drawable, contactNum, FOURTH, scalepram, false, FOURTH == mClickIndex && mNeedOverlay);
        canvas.restoreToCount(saveCount);
    }
    // Aurora xuyong 2016-01-08 added for bug #18250 start
    private Uri getContactUriByString(String contactString) {
        String[] result = contactString.split(CONTACT_INFO_DIVIDER);
        return Uri.parse(result[0]);
    }

    private String getContactNumberByString(String contactString) {
        String[] result = contactString.split(CONTACT_INFO_DIVIDER);
        return result[1];
    }

    private String getContactPrivacyByString(String contactString) {
        String[] result = contactString.split(CONTACT_INFO_DIVIDER);
        return result[2];
    }
    // Aurora xuyong 2016-01-08 added for bug #18250 end
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature start
        // Aurora xuyong 2016-01-28 modified for xy-smartsms start
        // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
        //if (mXyDrawable == null) {
        // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
            drawContacts(canvas, mDrawableList);
        // Aurora xuyong 2016-01-30 deleted for xy-smartsms start
        /*} else {
            xyDrawableDraw(canvas);
        }*/
        // Aurora xuyong 2016-01-30 deleted for xy-smartsms end
        // Aurora xuyong 2016-01-28 modified for xy-smartsms end
        // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature end
        /*if (!isEnabled()) {
            // not clickable? don't show triangle
            return;
        }

        if (mOverlay == null || mOverlay.getIntrinsicWidth() == 0 ||
                mOverlay.getIntrinsicHeight() == 0) {
            // nothing to draw
            return;
        }

        mOverlay.setBounds(0, 0, getWidth(), getHeight());

        if (mPaddingTop == 0 && mPaddingLeft == 0) {
            mOverlay.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.translate(mPaddingLeft, mPaddingTop);
            mOverlay.draw(canvas);
            canvas.restoreToCount(saveCount);
        }*/
    }

    /*@Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mOverlay != null && mOverlay.isStateful()) {
            mOverlay.setState(getDrawableState());
            invalidate();
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mOverlay != null) {
            mOverlay.setHotspot(x, y);
        }
    }*/
    private float mClickX;
    private float mClickY;
    private boolean mNeedOverlay = false;
    private int mClickIndex = -1;
    private UriDrawable mUriDrawable = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Aurora xuyong 2016-01-30 modified for aurora 2.0 new feature start
        if (!mHasClicked) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mNeedOverlay = true;
                mClickX = event.getX();
                mClickY = event.getY();
            } else {
                mNeedOverlay = false;
            }
            invalidate();
        }
        // Aurora xuyong 2016-01-30 modified for aurora 2.0 new feature end
        return super.onTouchEvent(event);
    }

    private boolean checkContactExistByUri(Uri uri) {
        return uri != null && !uri.toString().endsWith("/0");
    }
    // Aurora xuyong 2016-01-30 added for aurora 2.0 new feature start
    public void setmHasClicked(boolean status) {
        mHasClicked = status;
    }
    // Aurora xuyong 2016-01-30 added for aurora 2.0 new feature end
    @Override
    public void onClick(View v) {
        // Aurora xuyong 2016-01-09 modified for bug #18296 start
        // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature start
        // Aurora xuyong 2016-01-19 added for bug #18465 start
        // Aurora xuyong 2016-03-29 modified for bug #21898 start
        if (mHasClicked) {
        // Aurora xuyong 2016-03-29 modified for bug #21898 end
            return;
        }
        // Aurora xuyong 2016-03-29 modified for bug #21898 start
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHasClicked = false;
            }
        }, 50);
        // Aurora xuyong 2016-03-29 modified for bug #21898 end
        mHasClicked = true;
        // Aurora xuyong 2016-01-19 added for bug #18465 end
        Intent intent = null;
        if (mUriDrawable != null) {
            if (checkContactExistByUri(mUriDrawable.getUri())) {
                // Aurora xuyong 2016-01-09 modified for bug #18296 end
                intent = new Intent(Intent.ACTION_VIEW, mUriDrawable.getUri());
                intent.putExtra("is_privacy_contact", mPrivacy > 0);
                // not activity context
                // Aurora xuyong 2016-01-19 deleted for bug #18456 start
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
                // Aurora xuyong 2016-01-19 deleted for bug #18456 end
            } else {
                CallDetailInfo info = mUriDrawable.getCallDetailInfo();
                if (info != null) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.AuroraCallDetailActivityV2"));
                    intent.putExtra("number", mUriDrawable.getNumber());
                    // Aurora xuyogn 2016-01-14 modified for aurora 2.0 new feature start
                    if (info.getCallsCount() > 0) {
                        intent.putExtra("_count", info.getCallsCount());
                        intent.putExtra("ids", info.getIds());
                    } else {
                        intent.putExtra("isCallHistoryEmpty", true);
                    }
                    // Aurora xuyogn 2016-01-14 modified for aurora 2.0 new feature end
                    intent.addCategory("android.intent.category.GIONEE");
                    // Aurora xuyong 2016-01-19 deleted for bug #18456 start
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    // Aurora xuyong 2016-01-19 deleted for bug #18456 end
                }
            }
         // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature end
            try {
            // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature start
                if (intent != null) {
                    // Aurora xuyong 2016-01-18 added for bug #18456 start
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // Aurora xuyong 2016-01-19 added for bug #18456 end
                    mContext.startActivity(intent);
                }
            // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature end
            } catch (ActivityNotFoundException e) {
                // do nothing..
            }
            // Aurora xuyong 2016-01-19 added for bug #18465 start
            mHasClicked = false;
            // Aurora xuyong 2016-01-19 added for bug #18465 end
        }
    }
    // Aurora xuyong 2016-01-19 added for bug #18465 start
    private boolean mHasClicked = false;
    // Aurora xuyong 2016-01-19 added for bug #18465 end
    private class UriDrawable extends BitmapDrawable {
        Uri mUri = null;
        Drawable mDrawable = null;
        // Aurora xuyong 2016-01-08 added for bug #18250 start
        String mNumber = null;
        long mPrivacy = PRIVACY_DEFAULT;
        // Aurora xuyong 2016-01-08 added for bug #18250 end
        // Aurora xuyong 2016-01-08 modified for bug #18250 start
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
        CallDetailInfo mCallDetailInfo;

        CallInfoQueryHandler mHandler = new CallInfoQueryHandler(mContext.getContentResolver());

        private final Uri mQueryUri = Uri.parse("content://call_log/gncallsjoindataview_matchnumber");

        private String getSelection() {
            //return "(privacy_id = 0 or privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId() + ")";
            return "(number = ? and privacy_id = ?)";
        }
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
        // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature start
        public UriDrawable(Drawable drawble, Uri uri, String number, final String privacy) {
        // Aurora xuyong 2016-01-12 modified for aurora 2.0 new feature end
        // Aurora xuyong 2016-01-08 modified for bug #18250 end
            mUri = uri;
            mDrawable = drawble;
            // Aurora xuyong 2016-01-08 added for bug #18250 start
            mNumber = number;
            mPrivacy = Long.parseLong(privacy);
            // Aurora xuyong 2016-01-08 added for bug #18250 end
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mHandler.startQuery(0, null, mQueryUri, PROJECTION_CALLS_JOIN_DATAVIEW, getSelection(), new String[] {"" + mNumber, privacy}, Calls.DEFAULT_SORT_ORDER);
                }
            }, 100);
            // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
        }
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
        private class CallInfoQueryHandler extends AsyncQueryHandler {

            public CallInfoQueryHandler(ContentResolver cr) {
                super(cr);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                mCallDetailInfo  = new CallDetailInfo(cursor);
                // Aurora xuyong 2016-02-01 added for aurora 2.0 new feature start
                if (cursor != null && !cursor.isClosed()) {
                   cursor.close();
                }
                // Aurora xuyong 2016-02-01 added for aurora 2.0 new feature end
            }
        }
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end

        public Uri getUri() {
            return mUri;
        }

        public Drawable getDrawable () {
            return mDrawable;
        }
        // Aurora xuyong 2016-01-08 added for bug #18250 start
        public String getNumber() {
            return  mNumber;
        }

        public long getPrivacy() {
            return mPrivacy;
        }
        // Aurora xuyong 2016-01-08 added for bug #18250 end
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
        public CallDetailInfo getCallDetailInfo() {
            return mCallDetailInfo;
        }
        // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end
    }
    // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature start
    public static final String[] PROJECTION_CALLS_JOIN_DATAVIEW = new String[] {
            Calls._ID,                          // 0
            Calls.NUMBER,                       // 1
            Calls.DATE,                         // 2
            Calls.DURATION,                     // 3
            Calls.TYPE,                         // 4
            Calls.VOICEMAIL_URI,                // 5
            Calls.COUNTRY_ISO,                  // 6
            Calls.GEOCODED_LOCATION,            // 7
            Calls.IS_READ,                      // 8
            Calls.SIM_ID,                       // 9
            Calls.VTCALL,                       // 10
            Calls.RAW_CONTACT_ID,               // 11
            Calls.DATA_ID,                      // 12

            Contacts.DISPLAY_NAME,              // 13
            "calllognumbertype",                   // 14
            "calllognumbertypeid",                // 15
            Data.PHOTO_ID,                      // 16
            RawContacts.INDICATE_PHONE_SIM,     // 17
            RawContacts.CONTACT_ID,             // 18
            Contacts.LOOKUP_KEY,                // 19
            Data.PHOTO_URI,                      // 20
            "area",
            "reject",
            "mark",
            "user_mark",
            "privacy_id"
    };

    public static final int  CALLS_JOIN_DATA_VIEW_CALLS_COUNT_IDS = 27;

    private class CallDetailInfo {

        private int[] mIds;
        private int mCallsCount;
        private int mCallId;

        public CallDetailInfo(int[] ids, String number, int callsCount, int callId) {
            mIds = ids;
            mCallsCount = callsCount;
            mCallId = callId;
        }

        public CallDetailInfo(Cursor cursor) {
            int count = 0;
            StringBuilder builder = new StringBuilder();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String ids = cursor.getString(CALLS_JOIN_DATA_VIEW_CALLS_COUNT_IDS);
                    builder.append(ids);
                    if (!cursor.isLast()) {
                        builder.append(",");
                    }
                } while (cursor.moveToNext());
                String[] resultIds = builder.toString().split(",");
                mIds = new int[resultIds.length];
                for (int i = 0; i < resultIds.length; i++) {
                    mIds[i] = Integer.parseInt(resultIds[i]);
                }
                mCallsCount = mIds.length;
            }
        }

        public int[] getIds() {
            return mIds;
        }

        public int getCallsCount() {
            return mCallsCount;
        }

        public int getCallId() {
            return mCallId;
        }
    }
    // Aurora xuyong 2016-01-12 added for aurora 2.0 new feature end

}
