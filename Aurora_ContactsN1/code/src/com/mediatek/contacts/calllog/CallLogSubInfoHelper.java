package com.mediatek.contacts.calllog;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.contacts.ContactsUtils;
import android.util.Log;

import com.android.contacts.R;

public class CallLogSubInfoHelper {
	private static final String TAG = "CallLogSubInfoHelper";
    
    
    
    

    private Resources mResources;
    private String mSipCallDisplayName = "";
    private Drawable mDrawableSimSipColor;
    private Drawable mDrawableSimLockedColor;
    private Drawable mDrawableSimColor1;
    private Drawable mDrawableSimColor2;
    private int mInsertSimColor1 = -1;
    private int mInsertSimColor2 = -1;

    public CallLogSubInfoHelper(Resources resources) {
        mResources = resources;
    }

    public String getSimDisplayNameById(int simId) {
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            if ("".equals(mSipCallDisplayName)) {
            	mSipCallDisplayName = mResources.getString(R.string.call_sipcall);
            }
            return mSipCallDisplayName;
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return "";
        } else {
            return ContactsUtils.getSimDisplayNameById(simId);

        }
    }

    public Drawable getSimColorDrawableById(int simId) {
    	log("getSimColorDrawableById() simId == [" + simId +"]" );
    	int mCalllogSimnameHeight = (int) mResources
    	.getDimension(R.dimen.calllog_list_item_simname_height);
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            // The request is sip color
            if (null == mDrawableSimSipColor) {
//                mDrawableSimSipColor = mResources.getDrawable(com.mediatek.internal.R.drawable.sim_background_sip);
        		Bitmap bitmap = BitmapFactory.decodeResource(mResources,
        				R.drawable.sim_background_sip);
				bitmap = Bitmap
						.createScaledBitmap(bitmap, mCalllogSimnameHeight,
								mCalllogSimnameHeight, false);
				mDrawableSimSipColor = new BitmapDrawable(mResources, bitmap);
            }
            return mDrawableSimSipColor.getConstantState().newDrawable();
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return null;
        } else {
            int color = 0;
            log("getSimColorDrawableById() color == [" + color +"]" );
            if (-1 != color) {
                int simColorResId = 0;
                if (-1 != mInsertSimColor1 && mInsertSimColor1 == color ) {
                    // The request color is mInsertSimColor1
                    return mDrawableSimColor1;
                } else if (-1 != mInsertSimColor2 && mInsertSimColor2 == color) {
                    // The request color is mInsertSimColor2
                    return mDrawableSimColor2;
                } else if (-1 == mInsertSimColor1 && -1 != mInsertSimColor2) {
                    // mInsertSimColor1 is not used, use it
                    mInsertSimColor1 = color;
//                    mDrawableSimColor1 = mResources.getDrawable(simColorResId);
					Bitmap bitmap = BitmapFactory.decodeResource(mResources,
							simColorResId);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, mCalllogSimnameHeight,
									mCalllogSimnameHeight, false);
					mDrawableSimColor1 = new BitmapDrawable(mResources, bitmap);
                    return mDrawableSimColor1;
                } else if (-1 == mInsertSimColor2 && -1 != mInsertSimColor1) {
                    // mInsertSimColor2 is not used, use it
                    mInsertSimColor2 = color;
//                    mDrawableSimColor2 = mResources.getDrawable(simColorResId);
					Bitmap bitmap = BitmapFactory.decodeResource(mResources,
							simColorResId);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, mCalllogSimnameHeight,
									mCalllogSimnameHeight, false);
					mDrawableSimColor2 = new BitmapDrawable(mResources, bitmap);
                    return mDrawableSimColor2;
                } else {
                    // There are 3 conditions for getting here
                    // 1. mInsertSimColor1 and mInsertSimColor2 are used but both not correct now
                    // 2. mInsertSimColor1 and mInsertSimColor2 are used but one of them is not correct
                    // 3. both of mInsertSimColor1 and mInsertSimColor2 are not used
                    mInsertSimColor1 = color;
//                    mDrawableSimColor1 = mResources.getDrawable(simColorResId);
                    mInsertSimColor2 = -1;
                    mDrawableSimColor2 = null;
                    
					Bitmap bitmap = BitmapFactory.decodeResource(mResources,
							simColorResId);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, mCalllogSimnameHeight,
									mCalllogSimnameHeight, false);
					mDrawableSimColor1 = new BitmapDrawable(mResources, bitmap);
                    return mDrawableSimColor1;
                }
            } else {
                // The request color is not inserted sim currently
                if (null == mDrawableSimLockedColor) {
//                    mDrawableSimLockedColor = 
//                        mResources.getDrawable(com.mediatek.internal.R.drawable.sim_background_locked);
        			Bitmap bitmap = BitmapFactory.decodeResource(mResources,
        					R.drawable.sim_background_locked);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, mCalllogSimnameHeight,
									mCalllogSimnameHeight, false);
					mDrawableSimLockedColor = new BitmapDrawable(mResources, bitmap);
                }
                // Not inserted sim has same background but different length, so can not share same drawable
                return mDrawableSimLockedColor.getConstantState().newDrawable();
            }
        }
    }

    public void resetCacheInfo() {
        mDrawableSimColor1 = null;
        mDrawableSimColor2 = null;
        mInsertSimColor1 = -1;
        mInsertSimColor2 = -1;
    }
    
    private void log(final String log) {
        Log.i(TAG, log);
    }
}
