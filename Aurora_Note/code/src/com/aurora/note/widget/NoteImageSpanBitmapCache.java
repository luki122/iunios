package com.aurora.note.widget;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 缓存加载过的图片，避免每次都需要重新从SD卡加载图片
 * @author JimXia
 * 2014-5-20 上午10:50:35
 */
public class NoteImageSpanBitmapCache {
    private static final String TAG = "NoteImageSpanBitmapCache";
    
    private static NoteImageSpanBitmapCache sInstance;
    
    private HashMap<String, SoftReference<Bitmap>> mCache;
    
    private NoteImageSpanBitmapCache() {
        mCache = new HashMap<String, SoftReference<Bitmap>>(15);
    }
    
    public static NoteImageSpanBitmapCache getInstance() {
        if (sInstance == null) {
            synchronized (NoteImageSpanBitmapCache.class) {
                if (sInstance == null) {
                    sInstance = new NoteImageSpanBitmapCache();
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
                Log.d(TAG, "Jim, removeBitmap, image for " + src + " is removed");
            }
            mCache.remove(src);
            Log.d(TAG, "Jim, removeBitmap, cached bitmaps: " + mCache.size());
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