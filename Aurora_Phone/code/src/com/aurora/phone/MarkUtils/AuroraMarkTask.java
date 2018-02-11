package com.android.phone;   

import com.yulore.superyellowpage.modelbean.RecognitionTelephone;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.telephony.Connection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AuroraMarkTask extends AsyncTask<Connection, Void, Boolean> {
    private static final String LOG_TAG = "AuroraMarkTask";
    @Override
    protected void onPreExecute() {
    	Log.v(LOG_TAG, "onPreExecute = ");  
    	AuroraMarkUtils.resetMarks();
    }
	
	
    @Override
    protected Boolean doInBackground(Connection... c) {   
    	AuroraMarkUtils.getNumberInfoInternal(c[0].getAddress());
        if(PhoneGlobals.getInstance().mYuloreUtils.isInit()) {
        	Log.v(LOG_TAG, "doInBackground setphoto");  
	        AuroraCallerInfo callerInfo = null;
	        do {
	        	callerInfo = PhoneGlobals.getInstance().notifier.getCallerInfoFromConnection(c[0]);
		        if(callerInfo != null) {
			        Bitmap photo = YuLoreUtils.getPhoto();
			        if(photo != null) {
				        Bitmap photoIcon = getAuroraPhotoIconWhenAppropriate(PhoneGlobals.getInstance(), photo);
				        callerInfo.cachedPhoto = new BitmapDrawable(photo);
				        callerInfo.cachedPhotoIcon = photoIcon;
				        callerInfo.isCachedPhotoCurrent = true;
			        }
		        } else {
	                SystemClock.sleep(200);	
		        }
	        } while(callerInfo == null);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    	Log.v(LOG_TAG, "onPostExecute = ");   
        PhoneGlobals.getInstance().updateInCallScreen();
    }
    
    private static Bitmap getAuroraPhotoIconWhenAppropriate(Context context, Bitmap orgBitmap) {
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.notification_icon_size);
        int orgWidth = orgBitmap.getWidth();
        int orgHeight = orgBitmap.getHeight();
        int longerEdge = orgWidth > orgHeight ? orgWidth : orgHeight;
        // We want downscaled one only when the original icon is too big.
        if (longerEdge > iconSize) {
            float ratio = ((float) longerEdge) / iconSize;
            int newWidth = (int) (orgWidth / ratio);
            int newHeight = (int) (orgHeight / ratio);
            // If the longer edge is much longer than the shorter edge, the latter may
            // become 0 which will cause a crash.
            if (newWidth <= 0 || newHeight <= 0) {
                Log.w(LOG_TAG, "Photo icon's width or height become 0.");
                return null;
            }

            // It is sure ratio >= 1.0f in any case and thus the newly created Bitmap
            // should be smaller than the original.
            return Bitmap.createScaledBitmap(orgBitmap, newWidth, newHeight, true);
        } else {
            return orgBitmap;
        }
    }
}