package com.aurora.puremanager.model;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

public final class DefMrgSoftIntent {
	public static final int DEF_BROWSER = 1;
	public static final int DEF_PHONE = DEF_BROWSER + 1;
	public static final int DEF_MMS = DEF_BROWSER + 2;
	public static final int DEF_HOME = DEF_BROWSER + 3;
	public static final int DEF_CAMERA = DEF_BROWSER + 4;
	public static final int DEF_GALLAY = DEF_BROWSER + 5;
	public static final int DEF_MUSIC = DEF_BROWSER + 6;
	public static final int DEF_VIDEO = DEF_BROWSER + 7;
	public static final int DEF_READER = DEF_BROWSER + 8;
	public static final int DEF_DIAL = DEF_BROWSER + 9;
	public static final int DEF_CALL = DEF_BROWSER + 10;
	public static final int DEF_CROP = DEF_BROWSER + 11;
    
    public static final String[] MUSIC_DATA_TYPES = new String[]{"audio/*", "application/ogg",
        "application/x-ogg", "application/itunes", "application/aac", "application/imy" };
    
    public static Intent getDefIntent(int i) {
        Intent intent = null;
        switch (i) {
            case DEF_BROWSER:
                intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
//              intent.addCategory("android.intent.category.BROWSABLE");
                intent.setData(Uri.parse("http://www.baidu.com"));
                break;
            case DEF_PHONE:
               // intent = new Intent("com.android.contacts.action.LIST_CONTACTS");
        	    intent = new Intent(Intent.ACTION_VIEW);
        	    intent.setData(ContactsContract.Contacts.CONTENT_URI);
                break;
            case DEF_MMS:
//                intent = new Intent();
//                intent.setAction("android.intent.action.MAIN");
//                intent.setType("vnd.android-dir/mms-sms");
        	    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:10010"));   
        	    intent.addCategory(Intent.CATEGORY_DEFAULT);
        	    intent.putExtra("sms_body", "");  
                break;
            case DEF_HOME:
                intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                break;
            case DEF_CAMERA:
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                break;
            case DEF_GALLAY:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "image/*");
                break;
            case DEF_MUSIC:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "audio/mpeg");
                break;
            case DEF_VIDEO:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "video/*");
                break;
            case DEF_READER:
                intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("file:///android_asset/gionee"), "text/plain");
                break;
            case DEF_DIAL:  
                intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"));
                break;
            case DEF_CALL:  
                intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:13888888888"));
                break;
            case DEF_CROP:
            	intent = new Intent("com.android.camera.action.CROP");   
        		intent.setDataAndType(Uri.fromFile(new File("file:///android_asset/gionee")), "image/*"); 
            default:
                break;
        }
        return intent;
    }
}
