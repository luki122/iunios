package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.io.File;

import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;

import android.os.Environment;
import android.util.Log;

public class XCloudUploadFilter {
	
	public static final String [] POST_FILTER_ARRAY = {"wbmp", "webp"};
	
	private static String sCameraAlbum;
	private static String sScreenshotsAlbum;
	
	static {
		File file = Environment.getExternalStorageDirectory();
		if(null != file){
			String EX_STORAGE = file.toString();
			sCameraAlbum = EX_STORAGE + MediaSetUtils.CAMERA_BUCKET_NAME;
			sScreenshotsAlbum = EX_STORAGE + MediaSetUtils.SNAPSHOT_BUCKET_NAME;
		}
	}
	
	public static boolean postfixShouldBeFiletered(String filePath) {
		for(int i=0; i<POST_FILTER_ARRAY.length; i++) {
			if(filePath.endsWith(POST_FILTER_ARRAY[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean filePathShouldBeFiltered(String filePath) {
		//Log.i("SQF_LOG", "filePath: " + filePath + " sCameraAlbum:" + sCameraAlbum + " sScreenshotsAlbum:" + sScreenshotsAlbum);
		if( ! filePath.startsWith(sCameraAlbum) && ! filePath.startsWith(sScreenshotsAlbum)) {
			return true;
		}
		return false;
	}
}
