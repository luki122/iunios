package com.aurora.thememanager.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;


public class ImageSpanBitmapCache {
	 private static final String TAG = "ImageSpanBitmapCache";
	    
	    private static ImageSpanBitmapCache sInstance;
	    
	    private HashMap<String, SoftReference<Bitmap>> mCache;
	    
	    private ImageSpanBitmapCache() {
	        mCache = new HashMap<String, SoftReference<Bitmap>>(15);
	    }
	    
	    public static ImageSpanBitmapCache getInstance() {
	        if (sInstance == null) {
	            synchronized (ImageSpanBitmapCache.class) {
	                if (sInstance == null) {
	                    sInstance = new ImageSpanBitmapCache();
	                }
	            }
	        }
	        
	        return sInstance;
	    }
	    
	    public void putBitmap(String src, Bitmap bitmap) {
	        if (!TextUtils.isEmpty(src) && bitmap != null) {
	            mCache.put(src, new SoftReference<Bitmap>(bitmap));
	        }
	    }
	    
	    public Bitmap getBitmap(String src) {
	        SoftReference<Bitmap> bitmap = mCache.get(src);
	        if (bitmap != null) {
	            return bitmap.get();
	        }
	        
	        return null;
	    }
	    
	    public boolean isBitmapInCache(Bitmap bitmap) {
	        Iterator<SoftReference<Bitmap>> iterator = mCache.values().iterator();
	        while (iterator.hasNext()) {
	            SoftReference<Bitmap> value = iterator.next();
	            if (value != null) {
	                Bitmap b = value.get();
	                if (bitmap == b) {
	                    return true;
	                }
	            }
	        }
	        
	        return false;
	    }
	    
	    public void removeBitmap(String src) {
	        SoftReference<Bitmap> sBitmap = mCache.get(src);
	        if (sBitmap != null) {
	            Bitmap bitmap = sBitmap.get();
	            if (bitmap != null && !bitmap.isRecycled()) {
	                bitmap.recycle();
	            }
	            mCache.remove(src);
	        }
	    }
	    
	    public void clear() {
	        Iterator<SoftReference<Bitmap>> iterator = mCache.values().iterator();
	        while (iterator.hasNext()) {
	            Bitmap bitmap = iterator.next().get();
	            if (bitmap != null && !bitmap.isRecycled()) {
	                bitmap.recycle();
	            }
	        }
	        mCache.clear();
	        mCache = null;
	        sInstance = null;
	    }
	    
	    public int size() {
	        return mCache.size();
	    }
}
