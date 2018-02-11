package com.aurora.change.cache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class MemoryCache {

    private static final int MAX_CAPACITY = 200 * 1024;// 一级缓存的最大空间
    private static final long DELAY_BEFORE_PURGE = 60 * 1000;// 定时清理缓存
    // 0.75是加载因子为经验值，true则表示按照最近访问量的高低排序，false则表示按照插入顺序排序
    private HashMap<String, Bitmap> mFirstLevelCache = new LinkedHashMap<String, Bitmap>(MAX_CAPACITY / 2,
            0.75f, true) {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
            if (size() > MAX_CAPACITY) {// 当超过一级缓存阈值的时候，将老的值从一级缓存搬到二级缓存
                mSecondLevelCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            }
            return false;
        };
    };
    // 二级缓存，采用的是软应用，只有在内存吃紧的时候软应用才会被回收，有效的避免了oom
    private ConcurrentHashMap<String, SoftReference<Bitmap>> mSecondLevelCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
            MAX_CAPACITY / 2);

    // 定时清理缓存
    private Runnable mClearCache = new Runnable() {
        @Override
        public void run() {
            clear();
        }
    };
    private Handler mPurgeHandler = new Handler();

    // 通过信号量控制同时执行的线程数
    Semaphore mSemaphore = new Semaphore(100);

    // 重置缓存清理的timer
    private void resetPurgeTimer() {
        mPurgeHandler.removeCallbacks(mClearCache);
        mPurgeHandler.postDelayed(mClearCache, DELAY_BEFORE_PURGE);
    }

    /**
     * 清理缓存
     */
    public void clear() {
        mFirstLevelCache.clear();
        mSecondLevelCache.clear();
    }

    /**
     * 返回缓存，如果没有则返回null
     * 
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = null;
        bitmap = getFromFirstLevelCache(url);// 从一级缓存中拿
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = getFromSecondLevelCache(url);// 从二级缓存中拿
        return bitmap;
    }

    /**
     * 从二级缓存中拿
     * 
     * @param url
     * @return
     */
    private Bitmap getFromSecondLevelCache(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> softReference = mSecondLevelCache.get(url);
        if (softReference != null) {
            bitmap = softReference.get();
            if (bitmap == null) {// 由于内存吃紧，软引用已经被gc回收了
                mSecondLevelCache.remove(url);
            }
        }
        return bitmap;
    }

    /**
     * 从一级缓存中拿
     * 
     * @param url
     * @return
     */
    private Bitmap getFromFirstLevelCache(String url) {
        Bitmap bitmap = null;
        synchronized (mFirstLevelCache) {
            bitmap = mFirstLevelCache.get(url);
            if (bitmap != null) {// 将最近访问的元素放到链的头部，提高下一次访问该元素的检索速度（LRU算法）
                mFirstLevelCache.remove(url);
                mFirstLevelCache.put(url, bitmap);
            }
        }
        return bitmap;
    }

    /**
     * 放入缓存
     * 
     * @param url
     * @param value
     */
    public void addImage2Cache(String url, Bitmap value) {
        if (value == null || url == null) {
            return;
        }
        synchronized (mFirstLevelCache) {
            mFirstLevelCache.put(url, value);
        }
    }

    private static final String TAG = "MemoryCache";
    // 放入缓存时是个同步操作
    // LinkedHashMap构造方法的最后一个参数true代表这个map里的元素将按照最近使用次数由少到多排列，即LRU
    // 这样的好处是如果要将缓存中的元素替换，则先遍历出最近最少使用的元素来替换以提高效率
    private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,
            1.5f, true));
    // 缓存中图片所占用的字节，初始0，将通过此变量严格控制缓存所占用的堆内存
    private long size = 0;// current allocated size
    // 缓存只能占用的最大堆内存
    private long limit = 1000000;// max memory in bytes

    public MemoryCache() {
        // use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory() / 10);
        resetPurgeTimer();
    }

    public void setLimit(long new_limit) {
        limit = new_limit;
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
    }

    public Bitmap get(String id) {
        try {
            if (!cache.containsKey(id))
                return null;
            return cache.get(id);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public void put(String id, Bitmap bitmap) {
        try {
            if (cache.containsKey(id))
                size -= getSizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    /**
     * 严格控制堆内存，如果超过将首先替换最近最少使用的那个图片缓存
     * 
     */
    private void checkSize() {
        Log.i(TAG, "cache size=" + size + " length=" + cache.size());
        if (size > limit) {
            // 先遍历最近最少使用的元素
            Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Bitmap> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if (size <= limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size " + cache.size());
        }
    }

    // public void clear() {
    // cache.clear();
    // }

    /**
     * 图片占用的内存
     * 
     * [url=home.php?mod=space&uid=2768922]@Param[/url] bitmap
     * 
     * @return
     */
    long getSizeInBytes(Bitmap bitmap) {
        if (bitmap == null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
