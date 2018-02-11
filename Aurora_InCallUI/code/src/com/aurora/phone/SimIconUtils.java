package com.android.incallui;

import android.content.ContentUris;
import android.content.ContentValues;
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

import com.android.incallui.AuroraTelephony.SIMInfo;
import com.android.incallui.AuroraTelephony.SimInfo;


public class SimIconUtils {
    private static final String LOG_TAG = "SimIconUtils";
    
    public static int getSimIcon(int slot) {
    	return getSimIconBySlot(slot);
    }
    
    public static int getSmallSimIcon(int slot) {
    	return getSimIconBySlot(slot);
    }
    
    
    public static int getSimIconDefaultPhotoIncoming(int slotid) {
    	int result = -1;
		if(slotid == 0) {
			result = R.drawable.photo_default_sim1;
		} else {
			result = R.drawable.photo_default_sim2;
		}	
		
		
		return result;
	}
   

    public static int getSimIconDefaultPhotoOutgoing(int slotid) {
		int result = -1;
		if(slotid == 0) {
			result = R.drawable.photo_default_outgoing_sim1;
		} else {
			result = R.drawable.photo_default_outgoing_sim2;
		}	
		
		
		return result;
	}

    
    
	private static int getOtherSlot(int slot) {
		return slot == AuroraMSimConstants.SUB1 ? AuroraMSimConstants.SUB2
				: AuroraMSimConstants.SUB1;
	}
    
	  
	  private static int getSimIconBySlot(int slot) {
	    	if(slot == 0) {
      		return R.drawable.smallsim1;
	    	} else {
	    		return R.drawable.smallsim2;
	    	}	
	  }	 
	  
	
	
}