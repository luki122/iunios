package com.aurora.note.activity.picbrowser;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.aurora.note.NoteApp;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.FileLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class ImageManager2 {
	public static String TAG = "ImageManager2";
	private static ImageManager2 imageManager;
	public LruCache<String, Bitmap> mMemoryCache;
	private static NoteApp myapp;

	/** 图片加载队列，后进先出 */
	private Stack<ImageRef> mImageQueue = new Stack<ImageRef>();

	/** 图片请求队列，先进先出，用于存放已发送的请求。 */
	private Queue<ImageRef> mRequestQueue = new LinkedList<ImageRef>();

	/** 图片加载线程消息处理器 */
	private Handler mImageLoaderHandler;

	/** 图片加载线程是否就绪 */
	private boolean mImageLoaderIdle = true;

	/** 请求图片 */
	private static final int MSG_REQUEST = 1;
	/** 图片加载完成 */
	private static final int MSG_REPLY = 2;
	/** 中止图片加载线程 */
	private static final int MSG_STOP = 3;

	/**
	 * 获取单例，只能在UI线程中使用。
	 * 
	 * @param context
	 * @return
	 */
	public static ImageManager2 from(Context context) {

		// 如果不在ui线程中，则抛出异常
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("Cannot instantiate outside UI thread.");
		}

		if (myapp == null) {
			myapp = (NoteApp) context.getApplicationContext();
		}

		if (imageManager == null) {
			imageManager = new ImageManager2(myapp);
		}

		return imageManager;
	}

	/**
	 * 私有构造函数，保证单例模式
	 * 
	 * @param context
	 */
	private ImageManager2(Context context) {
		int memClass = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		memClass = memClass > 32 ? 32 : memClass;
		// 使用可用内存的1/8作为图片缓存
		final int cacheSize = 1024 * 1024 * memClass / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}

		};

	}

	/**
	 * 存放图片信息
	 */
	class ImageRef {

		/** 图片对应ImageView控件 */
		ImageView imageView;
		/** 图片URL地址 */
		String url;
		/** 默认图资源ID */
		int resId;
		int width = 0;
		int height = 0;
		int type = 0;

		/**
		 * 构造函数
		 * 
		 * @param imageView
		 * @param url
		 * @param resId
		 * @param filePath
		 */
		ImageRef(ImageView imageView, String url, int resId, int type) {
			this.imageView = imageView;
			this.url = url;
			this.resId = resId;
			this.type = type;
		}

		ImageRef(ImageView imageView, String url, int resId) {
			this.imageView = imageView;
			this.url = url;
			this.resId = resId;
		}

		ImageRef(ImageView imageView, String url, int resId, int width,
				int height) {
			this.imageView = imageView;
			this.url = url;
			this.resId = resId;
			this.width = width;
			this.height = height;
		}

		ImageRef(ImageView imageView, String url, int resId, int width,
				int height, int type) {
			this.imageView = imageView;
			this.url = url;
			this.resId = resId;
			this.width = width;
			this.height = height;
			this.type = type;
		}

	}

	// 显示视频的原图
	public void displayVideoImage(ImageView imageView, String url, int resId,
			int type) {
		if (imageView == null) {
			return;
		}
		if (imageView.getTag() != null
				&& imageView.getTag().toString().equals(url)) {
			return;
		}
		if (resId >= 0) {
			if (imageView.getBackground() == null) {
				imageView.setBackgroundResource(resId);
			}
			imageView.setImageDrawable(null);

		}
		if (url == null || url.equals("")) {
			return;
		}

		// 添加url tag
		imageView.setTag(url);

		// 读取map缓存
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		queueImage(new ImageRef(imageView, url, resId, type));
	}

	// 显示视频的缩略图
	public void displayVideoImage(ImageView imageView, String url, int width,
			int height, int resId, int type) {
		if (imageView == null) {
			return;
		}
		if (imageView.getTag() != null
				&& imageView.getTag().toString().equals(url)) {
			return;
		}
		if (resId >= 0) {
			if (imageView.getBackground() == null) {
				imageView.setBackgroundResource(resId);
			}
			imageView.setImageDrawable(null);

		}
		if (url == null || url.equals("")) {
			return;
		}

		// 添加url tag
		imageView.setTag(url);

		// 读取map缓存
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		queueImage(new ImageRef(imageView, url, resId,width,height, type));
	}

	/**
	 * 显示图片
	 * 
	 * @param imageView
	 * @param url
	 * @param resId
	 */
	public void displayImage(ImageView imageView, String url, int resId) {
		if (imageView == null) {
			return;
		}
		if (imageView.getTag() != null
				&& imageView.getTag().toString().equals(url)) {
			return;
		}
		if (resId >= 0) {
			if (imageView.getBackground() == null) {
				imageView.setBackgroundResource(resId);
			}
			imageView.setImageDrawable(null);

		}
		if (url == null || url.equals("")) {
			return;
		}

		// 添加url tag
		imageView.setTag(url);

		// 读取map缓存
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			setImageBitmap(imageView, bitmap, false);
			return;
		}
		queueImage(new ImageRef(imageView, url, resId));
	}

	/**
	 * 显示图片固定大小图片的缩略图，一般用于显示列表的图片，可以大大减小内存使用
	 * 
	 * @param imageView
	 *            加载图片的控件
	 * @param url
	 *            加载地址
	 * @param resId
	 *            默认图片
	 * @param width
	 *            指定宽度
	 * @param height
	 *            指定高度
	 */
	public void displayImage(ImageView imageView, String url, int resId,
			int width, int height) {
		if (imageView == null) {
			return;
		}
		if (resId >= 0) {

			if (imageView.getBackground() == null) {
				imageView.setBackgroundResource(resId);
			}
			imageView.setImageDrawable(null);

		}
		if (url == null || url.equals("")) {
			return;
		}

		// 添加url tag
		imageView.setTag(url);
		// 读取map缓存
		Bitmap bitmap = mMemoryCache.get(url + width + height);
		if (bitmap != null) {
			setImageBitmap(imageView, bitmap, false);
			return;
		}

		queueImage(new ImageRef(imageView, url, resId, width, height));
	}

	/**
	 * 入队，后进先出
	 * 
	 * @param imageRef
	 */
	public void queueImage(ImageRef imageRef) {

		// 删除已有ImageView
		Iterator<ImageRef> iterator = mImageQueue.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().imageView == imageRef.imageView) {
				iterator.remove();
			}
		}

		// 添加请求
		mImageQueue.push(imageRef);
		sendRequest();
	}

	/**
	 * 发送请求
	 */
	private void sendRequest() {

		// 开启图片加载线程
		if (mImageLoaderHandler == null) {
			HandlerThread imageLoader = new HandlerThread("image_loader");
			imageLoader.start();
			mImageLoaderHandler = new ImageLoaderHandler(
					imageLoader.getLooper());
		}

		// 发送请求
		if (mImageLoaderIdle && mImageQueue.size() > 0) {
			ImageRef imageRef = mImageQueue.pop();
			Message message = mImageLoaderHandler.obtainMessage(MSG_REQUEST,
					imageRef);
			mImageLoaderHandler.sendMessage(message);
			mImageLoaderIdle = false;
			mRequestQueue.add(imageRef);
		}
	}

	/**
	 * 图片加载线程
	 */
	class ImageLoaderHandler extends Handler {

		public ImageLoaderHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			if (msg == null)
				return;

			switch (msg.what) {

			case MSG_REQUEST: // 收到请求
				Bitmap bitmap = null;
				if (msg.obj != null && msg.obj instanceof ImageRef) {
					ImageRef imageRef = (ImageRef) msg.obj;
					String url = imageRef.url;
					if (url == null)
						return;
					if(imageRef.type == 1){
						bitmap = ThumbnailUtils.createVideoThumbnail(imageRef.url,Images.Thumbnails.MINI_KIND);
						if(bitmap != null){
							bitmap = ThumbnailUtils.extractThumbnail(bitmap, imageRef.width, imageRef.height,
									ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
						}
					}else{
						if (imageRef.width != 0 && imageRef.height != 0) {
//							bitmap = ThumbnailUtils.extractThumbnail(tBitmap,
//									imageRef.width, imageRef.height,
//									ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
							try {
								bitmap = BitmapUtil.getBitmap(url, imageRef.width, imageRef.height);
							} catch (Exception e) {
								FileLog.e(TAG + "::BitmapFactory.decodeFile(url,opt)",e.toString());
								e.printStackTrace();
							}
						} else {
							try {
								bitmap = BitmapUtil.getBitmap(url, 540, 960);
							} catch (Exception e) {
								FileLog.e(TAG + "::BitmapFactory.decodeFile(url,opt)",e.toString());
								e.printStackTrace();
							}
						}
					}
					
					if (bitmap != null) {
						// 写入map缓存
						if (imageRef.width != 0 && imageRef.height != 0) {
							if (mMemoryCache.get(url + imageRef.width
									+ imageRef.height) == null)
								mMemoryCache.put(url + imageRef.width
										+ imageRef.height, bitmap);
						} else {
							if (mMemoryCache.get(url) == null)
								mMemoryCache.put(url, bitmap);
						}

					}
				}

				if (mImageManagerHandler != null) {
					Message message = mImageManagerHandler.obtainMessage(
							MSG_REPLY, bitmap);
					mImageManagerHandler.sendMessage(message);
				}
				break;

			case MSG_STOP: // 收到终止指令
				Looper.myLooper().quit();
				break;

			}
		}
	}

	/** UI线程消息处理器 */
	private Handler mImageManagerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg != null) {
				switch (msg.what) {

				case MSG_REPLY: // 收到应答

					do {
						ImageRef imageRef = mRequestQueue.remove();

						if (imageRef == null)
							break;

						if (imageRef.imageView == null
								|| imageRef.imageView.getTag() == null
								|| imageRef.url == null)
							break;

						if (!(msg.obj instanceof Bitmap) || msg.obj == null) {
							break;
						}
						Bitmap bitmap = (Bitmap) msg.obj;

						// 非同一ImageView
						if (!(imageRef.url).equals((String) imageRef.imageView
								.getTag())) {
							break;
						}

						setImageBitmap(imageRef.imageView, bitmap, true);
					} while (false);

					break;
				}
			}
			// 设置闲置标志
			mImageLoaderIdle = true;

			// 若服务未关闭，则发送下一个请求。
			if (mImageLoaderHandler != null) {
				sendRequest();
			}
		}
	};

	/**
	 * 添加图片显示渐现动画
	 * 
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap,
			boolean isTran) {
		if (isTran) {
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(android.R.color.transparent),
							new BitmapDrawable(NoteApp.ysApp.getResources(), bitmap) });
			td.setCrossFadeEnabled(true);
			imageView.setImageDrawable(td);
			td.startTransition(300);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	/**
	 * Activity#onStop后，ListView不会有残余请求。
	 */
	public void stop() {

		// 清空请求队列
		mImageQueue.clear();

	}

}
