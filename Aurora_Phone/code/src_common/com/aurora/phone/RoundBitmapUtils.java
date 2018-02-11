package com.android.phone;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;

import com.android.internal.telephony.Connection;

public class RoundBitmapUtils {
    private static final String LOG_TAG = "RoundBitmapUtils";
    
    public static Bitmap toRoundBitmap(Bitmap bitmap) { 
	    int width = bitmap.getWidth(); 
	    int height = bitmap.getHeight(); 
	    float roundPx; 
	    float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom; 
	    if (width <= height) { 
		    roundPx = width / 2; 
		    top = (height-width)/2; 
		    bottom = width + (height-width)/2; 
		    left = 0; 
		    right = width; 
		    height = width; 
		    dst_left = 0; 
		    dst_top = 0; 
		    dst_right = width; 
		    dst_bottom = width; 
	    } else { 
		    roundPx = height / 2; 
		    float clip = (width - height) / 2; 
		    left = clip; 
		    right = width - clip; 
		    top = 0; 
		    bottom = height; 
		    width = height; 
		    dst_left = 0; 
		    dst_top = 0; 
		    dst_right = height; 
		    dst_bottom = height; 
	    } 
	    Bitmap output = Bitmap.createBitmap(width, 
	    height, Config.ARGB_8888); 
	    Canvas canvas = new Canvas(output); 
	    final int color = 0xff424242; 
	    final Paint paint = new Paint(); 
	    final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom); 
	    final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom); 
	    final RectF rectF = new RectF(dst); 
	    paint.setAntiAlias(true); 
	    canvas.drawARGB(0, 0, 0, 0); 
	    paint.setColor(color); 
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 	    
	    canvas.drawBitmap(bitmap, src, dst, paint); 
	    return output; 
    } 
    
    public static Bitmap getAuroraPhotoIconWhenAppropriate(Context context, Bitmap orgBitmap) {
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