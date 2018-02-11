package com.android.contacts.widget;

import com.android.contacts.common.lettertiles.LetterTileDrawable;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.contacts.R;
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
public class TopQuickContactImageView extends QuickContactImageView {

    public TopQuickContactImageView(Context context) {
        this(context, null);
        setup();
    
    }

    public TopQuickContactImageView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    
        setup();
    }

    public TopQuickContactImageView(Context context, AttributeSet attrs, int defStyleAttr) {

        this(context, attrs, defStyleAttr, 0);
        setup();
    }

    public TopQuickContactImageView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {

//        super(context, attrs, defStyleAttr, defStyleRes);
    	 super(context, attrs, defStyleAttr);
         setup();
    }

    private void setup() {
        setScaleType(ScaleType.MATRIX);
    }
    
    @Override
    protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom) {
        float frameWidth = frameRight - frameLeft;
        float frameHeight = frameBottom - frameTop;
        
        if(getSuperDrawable() == null) {
        	 return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
        }
     

        float originalImageWidth = (float)getSuperDrawable().getIntrinsicWidth();
        float originalImageHeight = (float)getSuperDrawable().getIntrinsicHeight();

        float usedScaleFactor = 1;

//        if((frameWidth > originalImageWidth) || (frameHeight > originalImageHeight)) {
            // If frame is bigger than image
            // => Crop it, keep aspect ratio and position it at the bottom and center horizontally

            float fitHorizontallyScaleFactor = frameWidth/originalImageWidth;
            float fitVerticallyScaleFactor = frameHeight/originalImageHeight;

            usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
//        } 

        float newImageWidth = originalImageWidth * usedScaleFactor;
        float newImageHeight = originalImageHeight * usedScaleFactor;
  

        Matrix matrix = getImageMatrix();
        matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0); // Replaces the old matrix completly
//comment matrix.postTranslate if you want crop from TOP
//        matrix.postTranslate((frameWidth - newImageWidth) /2, frameHeight - newImageHeight);
        setImageMatrix(matrix);
        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
    }
    
}
