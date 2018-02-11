package com.aurora.calendar.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
// import android.provider.DrmStore;
import android.provider.MediaStore;
import android.util.Log;

import com.android.calendar.R;

public class CalendarFeatureConstants {

	public interface FeatureOption {
		public static boolean MTK_THEMEMANAGER_APP = false;
		public static boolean MTK_BEAM_PLUS_SUPPORT = false;
	}

	public static String getRingtoneTitle(Context context, String uriStr) {
    	if (null == uriStr) {
    		return context.getResources().getString(R.string.aurora_default_ring);
    	}
    	
    	return getRingtoneTitle(context, Uri.parse(uriStr));
    }

    public static String getRingtoneTitle(Context context, Uri uri) {
        Cursor cursor = null;
        ContentResolver res = context.getContentResolver();
        
        String title = null;

        //Gionee <huangzy><2013-06-05> modify for CR00818865 begin
        if (uri != null) {
            String authority = uri.getAuthority();	
            /*if (DrmStore.AUTHORITY.equals(authority)) {
                String[] DRM_COLUMNS = new String[] {
                    DrmStore.Audio._ID,
                    DrmStore.Audio.DATA,
                    DrmStore.Audio.TITLE
                };
                cursor = res.query(uri, DRM_COLUMNS, null, null, 
                		DrmStore.Audio._ID + " Limit 1");
            } else*/ if (MediaStore.AUTHORITY.equals(authority)) {
            	String[] MEDIA_COLUMNS = new String[] {
	                    MediaStore.Audio.Media._ID,
	                    MediaStore.Audio.Media.DATA,
	                    MediaStore.Audio.Media.TITLE
	                };
                cursor = res.query(uri, MEDIA_COLUMNS, null, null, 
                		MediaStore.Audio.Media._ID + " Limit 1");
            }
            
            if (null == cursor && uri.toString().startsWith("file://")) {
            	Log.i("James", uri.toString());
            	
            	String[] MEDIA_COLUMNS = new String[] {
	                    MediaStore.Audio.Media._ID,
	                    MediaStore.Audio.Media.DATA,
	                    MediaStore.Audio.Media.TITLE
	                };
            	
            	String data = uri.toString().substring(7);
            	try {
            		data = URLDecoder.decode(data, "UTF-8");	
            	} catch (UnsupportedEncodingException e) {
            		e.printStackTrace();
            	}
            	Log.i("James", data);
            	
            	Uri mediaUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
            	if (data.contains("sdcard")) {
            		mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            	}            	
            	Log.i("James", "mediaUri  " + mediaUri.toString());
            	
            	cursor = res.query(mediaUri, MEDIA_COLUMNS,
            			MediaStore.Audio.Media.DATA + " = '" + data + "'",
            			null, MediaStore.Audio.Media._ID + " Limit 1");
            }
            
            if (null != cursor) {
            	try {
                    if (cursor.moveToFirst()) {
                    	title =  cursor.getString(2);
                    }
                } finally {
                	cursor.close();
                }	
            }
        }
        
        if (null == title) {
        	title = context.getResources().getString(R.string.aurora_default_ring);
        }
        //Gionee <huangzy><2013-06-05> modify for CR00818865 end
        
        return title;
    }

}