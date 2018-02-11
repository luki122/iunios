package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.lang.ref.WeakReference;

import com.android.gallery3d.xcloudalbum.tools.cache.http.HttpCacheManager;
import com.android.gallery3d.xcloudalbum.tools.cache.image.FileCache;
import com.android.gallery3d.xcloudalbum.tools.cache.image.LruMemoryCache;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

public class ContentProviderThumbnailUtil {
	
	private static ContentProviderThumbnailUtil instance;
	
	private ContentProviderThumbnailUtil () {}
	
	public static ContentProviderThumbnailUtil getInstance() {
		if(instance == null) {
			instance = new ContentProviderThumbnailUtil();
		}
		return instance;
	}
	
	private LruMemoryCache lruMemoryCache;
	private FileCache fileCache;

	public void setLruMemoryCache(LruMemoryCache lruMemoryCache) {
		this.lruMemoryCache = lruMemoryCache;
	}

	public void setFileCache(FileCache fileCache) {
		this.fileCache = fileCache;
	}
	
	public void getThumbnail(Context context, String filePath, ImageProcessingCallback imageProcessingCallback) {

		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		String[] projection = {Thumbnails._ID };
		String selection = Thumbnails.DATA + "='" + filePath + "'";
		
		int id = -1;
		Cursor cursor = cr.query(uri, projection, selection, null, null);
		
		BitmapFactory.Options options = new BitmapFactory.Options();    
	    options.inDither = false;    
	    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//	    options.outWidth = 200;
//	    options.outHeight = 200;
	    String md5 = filePath;
	    while(cursor.moveToNext()) {
	    	id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			if(id != -1) {
				Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(cr, id, Thumbnails.MICRO_KIND, options);
				imageProcessingCallback.onImageProcessing(new WeakReference<Bitmap>(bmp), filePath);
				if (lruMemoryCache == null || fileCache == null) {
					Log.e("SQF_LOG", "lruMemoryCache  or fileCache is null");
				}
				if (bmp != null) {
					//Log.e("SQF_LOG", "save content provider thumbnail ok");
					lruMemoryCache.addBitmapToMemoryCache(md5, bmp);
					fileCache.saveBitmapByLru(md5, bmp);
				}
				break;
			}
	    }
	    //wenyongzhe 2015.11.9 StrictMode start
	    if(!cursor.isClosed()){
	    	cursor.close();
	    	cursor = null;
	    }
	    //wenyongzhe 2015.11.9 StrictMode end
	}
	
	
}
