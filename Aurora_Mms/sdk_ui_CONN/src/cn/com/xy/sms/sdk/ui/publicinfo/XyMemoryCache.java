package cn.com.xy.sms.sdk.ui.publicinfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.com.xy.sms.sdk.log.LogManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class XyMemoryCache {

	private static final String TAG = "MemoryCache";

	private Map<String, BitmapDrawable> cache = Collections
			.synchronizedMap(new LinkedHashMap<String, BitmapDrawable>(10, 1.5f, true));

	private long size = 0;// current allocated size

	private long limit = 1000000;// max memory in bytes

	public XyMemoryCache() {
		// use 25% of available heap size
		setLimit(Runtime.getRuntime().maxMemory() / 10);
	}

	public void setLimit(long new_limit) {
		limit = new_limit;
		LogManager.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
	}

	public BitmapDrawable get(String id) {
		try {
			if (!cache.containsKey(id))
				return null;
			return cache.get(id);
		} catch (NullPointerException ex) {
			return null;
		}
	}

	public void put(String id, BitmapDrawable bitmap) {
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


	private void checkSize() {
		LogManager.i(TAG, "cache size=" + size + " length=" + cache.size());
		if (size > limit) {

			Iterator<Entry<String, BitmapDrawable>> iter = cache.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, BitmapDrawable> entry = iter.next();
				size -= getSizeInBytes(entry.getValue());
				iter.remove();
				if (size <= limit)
					break;
			}
			LogManager.i(TAG, "Clean cache. New size " + cache.size());
		}
	}

	public void clear() {
		cache.clear();
	}


	long getSizeInBytes(BitmapDrawable bitmap) {
		if (bitmap == null)
			return 0;
		Bitmap bit = bitmap.getBitmap();
		return bit.getRowBytes() * bit.getHeight();
	}
}
