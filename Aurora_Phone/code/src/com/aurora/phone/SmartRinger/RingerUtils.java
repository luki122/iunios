package com.android.phone;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class RingerUtils {
    private static final String LOG_TAG = "RingerUtils";
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    //aurora add liguangyu 20140904 for BUG #8161 start
    public static boolean isFileExist(Context mContext, Uri mCustomRingtoneUri) {    	      	  
        log("isFileExist()...  mCustomRingtoneUri = " + mCustomRingtoneUri);
    	String[] proj = {MediaStore.Audio.Media.DATA};
    	try {
    		Cursor c = mContext.getContentResolver().query(mCustomRingtoneUri ,proj, null, null, null);
        	if(c != null && c.getCount() > 0) {	    	
    	    	c.moveToFirst();     	    	  
    	    	String path = c.getString(0);    	    	
		    	File file = new File(path);
		    	if(file.exists()) {
		    	 	if(c != null) {
		        		c.close();
		        	}
		            log("isFileExist()... true");
		    		return true;
		    	}		    		
    	 
        	}
        	if(c != null) {
        		c.close();
        	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    //aurora add liguangyu 20140904 for BUG #8161 end
	
    public static AuroraRingerPolicy getAuroraRingerPolicy(Context mContext, AudioManager audioManager) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        AuroraRingerPolicy result;
        if(currentVolume == maxVolume) {
        	result = new AurorMAXPolicy(mContext);
        } else if (currentVolume <= 0.3 * maxVolume) {
          	result = new AurorMinPolicy(mContext);
        } else {
          	result = new AurorNormalPolicy(mContext);
        }
        return result;
    }
    
	
}