/**
  Copyright Statement:
  Author:baorui
  Create Date:2012/09/19
  Change List:
 */
package com.aurora.utils;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.provider.MediaStore;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
//Gionee <baorui><2013-05-23> modify for CR00813721 begin
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import com.android.deskclock.AlarmClock;
//Gionee <baorui><2013-05-23> modify for CR00813721 begin

public class GnRingtoneUtil {
	// Gionee baorui 2012-10-22 modify for CR00715234 begin
	public static boolean isRingtoneExist(Uri uri , ContentResolver mContentResolver) {
        try {
            AssetFileDescriptor fd = mContentResolver.openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                fd.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
	
	private static final String EXTERNAL="content://media/external",//get music from external
			                       INTERNAL="content://media/internal/";//get music from internal 
	
	private static String getMusicDisplayName(boolean isInternal,Cursor cursor){
		if(isInternal)
		{
			return cursor.getString(2);
		}else{
			String titleStr=cursor.getString(1);
			int index=titleStr.lastIndexOf("/")+1;
			int lastIndex=titleStr.lastIndexOf(".");
			return titleStr.substring(index, lastIndex);
		}
	}
	
	
	// Gionee baorui 2012-10-22 modify for CR00715234 end
	
    //Gionee <lwzh> <2013-04-29> add for CR00803698 begin
    public static String gnGetRingtoneTile(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "";
        }
        
        if(uri.toString().equals(AlarmClock.VALUE_RINGTONE_SILENT))
        {
        	return "silent";
        }
        
        // Gionee <baorui><2013-05-23> modify for CR00813721 begin
        String mUriStr = null;
        if (RingtoneManager.isDefault(uri)) {
            mUriStr = Settings.System.getString(context.getContentResolver(), Settings.System.ALARM_ALERT);
            if (mUriStr != null) {
                uri = Uri.parse(mUriStr);
            }
        }
        // Gionee <baorui><2013-05-23> modify for CR00813721 end

        Cursor cursor = null;
        ContentResolver res = context.getContentResolver();
        
        String title = "";

        String authority = uri.getAuthority();
        if (MediaStore.AUTHORITY.equals(authority)) {
            String[] MEDIA_COLUMNS = new String[] {
                    MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE
            };
            cursor = res.query(uri, MEDIA_COLUMNS, null, null, null);
        }

        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                // Gionee <baorui><2013-05-23> modify for CR00813721 begin
                // return cursor.getString(2);
                title = cursor.getString(2);
                title=getMusicDisplayName(uri.toString().startsWith(INTERNAL), cursor);
                // Gionee <baorui><2013-05-23> modify for CR00813721 end
            } else {
                // title =
                // context.getResources().getString(R.string.gnDefaultLabel);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // Gionee <baorui><2013-05-23> modify for CR00813721 begin
//        if (!"".equals(title) && mUriStr != null) {
//            title = context.getResources().getString(R.string.default_ringtone) + "（" + title + "）";
//        }
        // Gionee <baorui><2013-05-23> modify for CR00813721 end

        return title;
    }
    //Gionee <lwzh> <2013-04-29> add for CR00803698 end
}
