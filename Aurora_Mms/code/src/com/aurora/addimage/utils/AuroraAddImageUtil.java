package com.aurora.addimage.utils;
// Aurora xuyong 2015-10-15 created for aurora's new feature
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.aurora.addimage.databases.ATOpenHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class AuroraAddImageUtil {
	
	 public static final String TAG = "Mms/AddImage";
	 
	 public static final String APP_PACKAGE_NAME = "com.android.mms";
	 public static final String IMAGE_CACHE_THUMB = "thumbnails";
	 
	 public static final int FILE_MODE = 1;
	 public static final int DATABASE_MODE = 2;
	 
     private static String protocConvert(String path) {
    	 path = path.replaceAll(File.separator, String.valueOf('\1'));
    	 return path;
     }
     
     public static Bitmap getBitmapByPath(Context context, String path) {
    	 ATOpenHelper helper = new ATOpenHelper(context);
    	 return helper.getBitmapByPath(path);
     }
     
     public static void persist(final Context context, final Bitmap bitmap, final String bpPath, final int mode) {
    	 new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mode == FILE_MODE) {
		    		 persistToFile(context, bitmap, bpPath);
		    	 } else if (mode == DATABASE_MODE) {
		    		 persistToDb(context, bitmap, bpPath);
		    	 }
			}
    		 
    	 }).start();
     }
     
     public static void persistToDb(Context context, Bitmap bitmap, String bpPath) {
    	 if (bpPath == null || bitmap == null) {
    		 return;
    	 }
    	 ATOpenHelper helper = new ATOpenHelper(context);
    	 helper.insert(bpPath, bitmap);
     }
     
     public static void persistToFile(Context context, Bitmap bitmap, String bpPath) {
    	 if (bpPath == null || bitmap == null) {
    		 return;
    	 }
    	 File myCaptureFile = new File(context.getDir(IMAGE_CACHE_THUMB,  Context.MODE_PRIVATE).getAbsolutePath() + File.separator + protocConvert(bpPath));
    	 if (!myCaptureFile.exists()) {
    		 myCaptureFile.mkdirs();
    	 }
    	 BufferedOutputStream bos = null;
    	 try {
			bos = new BufferedOutputStream(
			         new FileOutputStream(myCaptureFile));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);
	        bos.flush();
	        bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
     
     public static String getPersistPath(Context context, String bpPath) {
    	 String path = context.getDir(IMAGE_CACHE_THUMB,  Context.MODE_PRIVATE).getAbsolutePath() + File.separator + protocConvert(bpPath);
    	 File persistFile = new File(path);
    	 if (persistFile.exists()) {
    		 return path;
    	 } else {
    		 return bpPath;
    	 }
     }
     
     public static boolean peristFileExist(Context context, String bpPath, int mode) {
    	 if (mode == FILE_MODE) {
	    	 String path = context.getDir(IMAGE_CACHE_THUMB,  Context.MODE_PRIVATE).getAbsolutePath() + File.separator + protocConvert(bpPath);
	    	 File persistFile = new File(path);
	    	 if (persistFile.exists()) {
	    		 return true;
	    	 } else {
	    		 return false;
	    	 }
    	 } else if (mode == DATABASE_MODE) {
    		 ATOpenHelper helper = new ATOpenHelper(context);
    		 return helper.existPath(bpPath);
    	 }
    	 return false;
     }
}
