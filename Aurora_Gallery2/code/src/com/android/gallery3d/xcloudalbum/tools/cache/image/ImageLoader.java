package com.android.gallery3d.xcloudalbum.tools.cache.image;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.uploaddownload.ContentProviderThumbnailUtil;

public class ImageLoader {

	public interface ImageProcessingCallback {
		void onImageProcessing(WeakReference<Bitmap> weak, String tag);
	}

	private static final String TAG = "ImageLoader";
	private LruMemoryCache lruMemoryCache;
	private FileCache fileCache;
	private BaiduAlbumUtils baiduAlbumUtils;
	private ContentProviderThumbnailUtil contentProviderThumbnailUtils;
	// 线程池
	private static ImageLoader mImageLoader = null;
	private ThreadPoolExecutor executor;
	private Context mContext;

	public static ImageLoader getInstance(Context context) {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(context);
		}
		return mImageLoader;
	}

	public Bitmap getFromCache(String md5) {
		if (TextUtils.isEmpty(md5)) {
			return null;
		}
		Bitmap bitmap = lruMemoryCache.getBitmapFromMemCache(md5);
		if (bitmap != null) {
			return bitmap;
		} else {
			bitmap = fileCache.getFileCache(md5);
			if (bitmap != null) {
				return bitmap;
			}
		}
		return null;
	}

	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(
			15); // 保持15个最大任务

	public ImageLoader(Context context) {
		this.mContext = context;
		lruMemoryCache = LruMemoryCache.getInstance();
		fileCache = FileCache.getInstance(context);
		int processors = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(1, processors * 2, 60L,
				TimeUnit.MILLISECONDS, queue, new PriorityThreadFactory(),
				new ThreadPoolExecutor.DiscardOldestPolicy());
		baiduAlbumUtils = BaiduAlbumUtils.getInstance(context
				.getApplicationContext());
		baiduAlbumUtils.setFileCache(fileCache);
		baiduAlbumUtils.setLruMemoryCache(lruMemoryCache);
		contentProviderThumbnailUtils = ContentProviderThumbnailUtil.getInstance();
		contentProviderThumbnailUtils.setFileCache(fileCache);
		contentProviderThumbnailUtils.setLruMemoryCache(lruMemoryCache);
	}

	public BaiduAlbumUtils getBaiduAlbumUtils() {
		return baiduAlbumUtils;
	}

	public void clearCache() {
		fileCache.clearFileCache();
		lruMemoryCache.clear();
	}

	public void displayImage(String url, String md5,
			ImageProcessingCallback imageProcessingCallback) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Bitmap bitmap = lruMemoryCache.getBitmapFromMemCache(md5);
		if (bitmap != null) {
			imageProcessingCallback.onImageProcessing(
					new WeakReference<Bitmap>(bitmap), md5);
			bitmap = null;
		} else {
			BitmapDisplayerThread task = new BitmapDisplayerThread(url, md5,
					imageProcessingCallback);
			executor.execute(task);
		}
	}
	
	public void displayThumbnail(Context context, String filePath, String md5, ImageProcessingCallback imageProcessingCallback) {
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		Bitmap bitmap = lruMemoryCache.getBitmapFromMemCache(md5);
		if (bitmap != null) {
			imageProcessingCallback.onImageProcessing(
					new WeakReference<Bitmap>(bitmap), md5);
			bitmap = null;
		} else {
			ThumbnailDisplayerThread task = new ThumbnailDisplayerThread(context, filePath, md5,
					imageProcessingCallback);
			executor.execute(task);
		}
	}

	private Handler handler = new Handler() {
	};
	
	private class ThumbnailDisplayerThread implements Runnable {
		private Context context;
		private Bitmap bitmap;
		private String filePath;
		private String md5;
		private ImageProcessingCallback imageProcessingCallback;

		public ThumbnailDisplayerThread(Context context, String filePath,String md5,
				ImageProcessingCallback imageProcessingCallback) {
			super();
			this.context = context;
			this.filePath = filePath;
			this.md5 = md5;
			this.imageProcessingCallback = imageProcessingCallback;
		}

		@Override
		public void run() {
			bitmap = getFromCache(md5);
			if (bitmap == null) {
				contentProviderThumbnailUtils.getThumbnail(context, filePath, imageProcessingCallback);
			}
			if (bitmap != null) {
				lruMemoryCache.addBitmapToMemoryCache(md5, bitmap);
				fileCache.saveBitmapByLru(md5, bitmap);
			}
			handler.post(new Runnable() {

				@Override
				public void run() {
					imageProcessingCallback.onImageProcessing(
							new WeakReference<Bitmap>(bitmap), md5);
					bitmap = null;
				}
			});
		}

	}

	private class BitmapDisplayerThread implements Runnable {
		private Bitmap bitmap;
		private String url;
		private String md5;
		private ImageProcessingCallback imageProcessingCallback;

		public BitmapDisplayerThread(String url, String md5,
				ImageProcessingCallback imageProcessingCallback) {
			super();
			this.url = url;
			this.md5 = md5;
			this.imageProcessingCallback = imageProcessingCallback;
		}

		@Override
		public void run() {
			bitmap = getFromCache(md5);
			if (bitmap == null) {
				if(NetworkUtil.checkNetwork(mContext)){//wenyongzhe 2015.10.26
					baiduAlbumUtils.getThumbnailsFromBaidu(url, md5,
							imageProcessingCallback);
				}
			}
			if (bitmap != null) {
				lruMemoryCache.addBitmapToMemoryCache(md5, bitmap);
				fileCache.saveBitmapByLru(md5, bitmap);
			}
			handler.post(new Runnable() {

				@Override
				public void run() {
					imageProcessingCallback.onImageProcessing(
							new WeakReference<Bitmap>(bitmap), md5);
					bitmap = null;
				}
			});
		}

	}

}