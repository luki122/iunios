package com.android.contacts.widget;

import com.android.contacts.common.lettertiles.LetterTileDrawable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.contacts.R;
import com.android.contacts.ResConstant;
// Aurora xuyong 2016-01-13 added for aurora 2.0 new feature start
import com.android.contacts.common.util.BitmapUtil;
// Aurora xuyong 2016-01-13 added for aurora 2.0 new feature end

/**
 * An {@link ImageView} designed to display QuickContact's contact photo. When requested to draw
 * {@link LetterTileDrawable}'s, this class instead draws a different default avatar drawable.
 *
 * In addition to supporting {@link ImageView#setColorFilter} this also supports a {@link #setTint}
 * method.
 *
 * This entire class can be deleted once use of LetterTileDrawable is no longer used
 * inside QuickContactsActivity at all.
 */
public class QuickContactImageView extends ImageView {

    private static final String TAG = "QuickContactImageView";
	private Drawable mOriginalDrawable;
    private BitmapDrawable mBitmapDrawable;
    private int mTintColor;
    private boolean mIsBusiness;
    private Context mContext;
    private int contactId; 
    


	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public QuickContactImageView(Context context) {
        this(context, null);
        mContext=context;
    }

    public QuickContactImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext=context;
    }

    public QuickContactImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        mContext=context;
    }

    public QuickContactImageView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
    	 super(context, attrs, defStyleAttr);
    	 Log.d(TAG,"context:"+context);
    	 mContext=context;
    }

    public void setTint(int color) {
        if (mBitmapDrawable == null || mBitmapDrawable.getBitmap() == null
                || mBitmapDrawable.getBitmap().hasAlpha()) {
            setBackgroundColor(color);
        } else {
            setBackground(null);
        }
        mTintColor = color;
        postInvalidate();
    }

    public boolean isBasedOffLetterTile() {
        return mOriginalDrawable instanceof LetterTileDrawable;
    }

    public void setIsBusiness(boolean isBusiness) {
        mIsBusiness = isBusiness;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
    	Log.i("QuickContactImageView","setImageDrawable");
        // There is no way to avoid all this casting. Blending modes aren't equally
        // supported for all drawable types.
        final BitmapDrawable bitmapDrawable;
        if (drawable == null || drawable instanceof BitmapDrawable) {
            bitmapDrawable = (BitmapDrawable) drawable;
            
            mOriginalDrawable = drawable;
            mBitmapDrawable = bitmapDrawable;
            setTint(mTintColor);
            super.setImageDrawable(bitmapDrawable);
            
        } else if (drawable instanceof LetterTileDrawable) {
            if (!mIsBusiness) {
                // Aurora xuyong 2016-01-13 added for aurora 2.0 new feature start
                /*bitmapDrawable = (BitmapDrawable) getResources().getDrawable(
                        R.drawable.person_white_540dp);*/
                // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
                bitmapDrawable =  (BitmapDrawable) getResources().getDrawable(
                        R.drawable.svg_dial_default_photo1);
                // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature end
                // Aurora xuyong 2016-01-13 added for aurora 2.0 new feature end
            } else {
                bitmapDrawable = (BitmapDrawable) getResources().getDrawable(
                        R.drawable.generic_business_white_540dp);
            }
            
            mOriginalDrawable = drawable;
            mBitmapDrawable = bitmapDrawable;
            setTint(mTintColor);
            super.setImageDrawable(bitmapDrawable);
            
        } else if (drawable instanceof VectorDrawable) {
        	Log.d(TAG,"vectorDrawable,contactid:"+contactId);
        	int index=(int) (contactId%(ResConstant.largeRandomContactPhotoId.length));

			if(index<ResConstant.largeRandomContactPhotoId.length&&contactId!=0){
				super.setImageDrawable(mContext.getResources().getDrawable(ResConstant.largeRandomContactPhotoId[index]));
			}else{
				Log.d(TAG,"mContext:"+mContext);
				if(mContext==null){
					super.setImageDrawable(getContext().getResources().getDrawable(R.drawable.large_svg_dial_default_photo1));
				}else{
				super.setImageDrawable(mContext.getResources().getDrawable(R.drawable.large_svg_dial_default_photo1));
				}
			}			
        }      
        else {
            throw new IllegalArgumentException("Does not support this type of drawable");
        }

//        mOriginalDrawable = drawable;
//        mBitmapDrawable = bitmapDrawable;
//        setTint(mTintColor);
//        super.setImageDrawable(bitmapDrawable);
    }

    @Override
    public Drawable getDrawable() {
        return mOriginalDrawable;
    }
    
    public Drawable getSuperDrawable() {
        return super.getDrawable();
    }
    
}
