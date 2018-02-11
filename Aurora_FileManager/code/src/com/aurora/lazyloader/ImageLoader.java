package com.aurora.lazyloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;

public class ImageLoader {

	public interface ImageProcessingCallback {
		void onImageProcessing(WeakReference<Bitmap> weak, String tag);
	}

	private static final String TAG = "ImageLoader";
	private LruMemoryCache lruMemoryCache;
	private FileCache fileCache;
	// 线程池
	private static ImageLoader mImageLoader = null;
	private int reqWidth = 256;
	private int reqHeight = 256;
	private ThreadPoolExecutor executor;
	public static ImageLoader getInstance(Context context) {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(context);
		}
		return mImageLoader;
	}

	public Bitmap getFromCache(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		Bitmap bitmap = lruMemoryCache.getBitmapFromMemCache(url);
		if (bitmap != null) {
			// LogUtil.elog(TAG, "memCache==" + url);
			return bitmap;
		} else {
			bitmap = fileCache.getFileCache(FileCache.urlToMD5(url));
			if (bitmap != null) {
				return bitmap;
			}
		}
		return null;
	}

	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(
			15); // 保持15个最大任务

	public ImageLoader(Context context) {
		lruMemoryCache = LruMemoryCache.getInstance();
		fileCache = FileCache.getInstance(context);
		int processors = Runtime.getRuntime().availableProcessors();
		// http://www.it165.net/pro/html/201312/8972.html
		// 最大4线程处理 大于15个任务时 抛弃旧的任务 添加新任务
		executor = new ThreadPoolExecutor(1, processors*2, 60L,
				TimeUnit.MILLISECONDS, queue, new PriorityThreadFactory(),
				new ThreadPoolExecutor.DiscardOldestPolicy());

	}

	private Bitmap getBitmap(String url) {
		// 先从文件缓存中查找是否有
		// 最后从指定的url中下载图片
		Bitmap bitmap = null;
		try {
			File imageFile = new File(url);
			try {
				bitmap = decodeFile(imageFile);
			} catch (Exception e) {
				e.printStackTrace();
				if (bitmap != null) {
					bitmap.recycle();
				}
			}

			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(File f) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inDither = false;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(f);
			BitmapFactory.decodeStream(inputStream, null, opts);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		opts.inSampleSize = computeSampleSize(opts, -1, reqHeight * reqWidth);
		// Log.e(TAG, " reqHeight * reqWidth=="+ reqHeight );
		opts.inJustDecodeBounds = false;

		Bitmap bmp = null;
		FileInputStream inputStream2 = null;
		try {
			inputStream2 = new FileInputStream(f);
			bmp = BitmapFactory.decodeStream(inputStream2, null, opts);
			return bmp;
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
			if (bmp != null) {
				bmp.recycle();
			}
			bmp = null;
			System.gc();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (inputStream2 != null)
					inputStream2.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (inputStream2 != null)
					inputStream2.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public void clearCache() {
		fileCache.clearFileCache();
		lruMemoryCache.clear();
	}

	public void displayImage(String url,
			ImageProcessingCallback imageProcessingCallback) {
		displayImage(url, reqWidth, reqHeight, imageProcessingCallback);
	}

	public void displayImage(String url, int reqWidth, int reqHeight,
			ImageProcessingCallback imageProcessingCallback) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		this.reqHeight = reqHeight;
		this.reqWidth = reqWidth;
		Bitmap bitmap = lruMemoryCache.getBitmapFromMemCache(url);
		if (bitmap != null) {
			imageProcessingCallback.onImageProcessing(
					new WeakReference<Bitmap>(bitmap), url);
			bitmap = null;
		} else {
			BitmapDisplayerThread task = new BitmapDisplayerThread(url,
					imageProcessingCallback);
			// LogUtil.elog(TAG, "queue.size()=="+queue.size());
			executor.execute(task);
		}
	}

	private Handler handler = new Handler() {
	};

	private class BitmapDisplayerThread implements Runnable {
		private Bitmap bitmap;
		private String url;
		private ImageProcessingCallback imageProcessingCallback;

		public BitmapDisplayerThread(String url,
				ImageProcessingCallback imageProcessingCallback) {
			super();
			this.url = url;
			this.imageProcessingCallback = imageProcessingCallback;
		}

		@Override
		public void run() {
			bitmap = getFromCache(url);
			if (bitmap == null) {
				bitmap = getBitmap(url);
			}
			if (bitmap != null) {
				lruMemoryCache.addBitmapToMemoryCache(url, bitmap);
				fileCache.saveBitmapByLru(url, bitmap);
			}
			handler.post(new Runnable() {

				@Override
				public void run() {
					imageProcessingCallback.onImageProcessing(
							new WeakReference<Bitmap>(bitmap), url);
					bitmap = null;
				}
			});
		}

	}
	

}