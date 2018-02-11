package com.android.contacts.widget;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import aurora.widget.AuroraTextView;

//Gionee:huangzy 20130326 add for CR00790078
public class GnTextView extends AuroraTextView {
	
     
    private static final int GN_FONT_SIZE_SYSTEM = 0;
     
    private static final int GN_FONT_SIZE_LARGE = 1;
     
     
    private static final int GN_FONT_SIZE_EXTRA_LARGE = 2;
    
    private static int mLargeTextType = GN_FONT_SIZE_SYSTEM;

    private static final String GN_FONT_SIZE = "gn_font_size";
    
    private static final String TAG = "GnTextView";
	
    public GnTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setGnTextSize();
	}

    public GnTextView(Context context) {
        this(context, null);
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);
        setGnTextSize();
    }

    private void setGnTextSize() {
        // if (getResources().getConfiguration().fontScale == 1.1f) {
        if (mLargeTextType > 0) {
            switch (mLargeTextType) {
                case GN_FONT_SIZE_LARGE:
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * 12 / 11);
                    break;
                case GN_FONT_SIZE_EXTRA_LARGE:
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * 14 / 11);
                    break;
                default:
                    break;
            }
        }
    }
    
    public static void onTextSizeChanged(Context context) {
        mLargeTextType = Settings.System.getInt(context.getContentResolver(), GN_FONT_SIZE,
                GN_FONT_SIZE_SYSTEM);
        Log.i(TAG, "Setting textSize changed,mLargeTextType = " + mLargeTextType);
    }

}

