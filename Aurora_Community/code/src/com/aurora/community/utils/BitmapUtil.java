package com.aurora.community.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 */
public class BitmapUtil {
	private static final String TAG = "BitmapUtil";
	private static int MB = 1024 * 1024;
	/** 请求相册 */
	public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
	/** 请求相册(android KITKAT以上版本)  */
	public static final int REQUEST_CODE_GETIMAGE_BYSDCARD_KITKAT = 1;
	/** 请求相机 */
	public static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 2;
	/** 请求裁剪 */
	public static final int REQUEST_CODE_GETIMAGE_BYCROP = 3;
	/** 上传照片最大限制 */
	public static final int MAX_IMAGE = 500;
	
	public static Bitmap readBitmap(Context context, InputStream is) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		return BitmapFactory.decodeStream(is, null, opt);
	}

	public static Bitmap readBitmap(Context context, InputStream is,
			int screenWidth, int screenHight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inInputShareable = true;
		options.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
		return getBitmap(bitmap, screenWidth, screenHight);
	}

	/***
	 * 等比例压缩图片
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @param screenHight
	 * @return
	 */
	public static Bitmap getBitmap(Bitmap bitmap, int screenWidth,
			int screenHight) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Log.e("jj", "图片宽度" + w + ",screenWidth=" + screenWidth);
		Matrix matrix = new Matrix();
		float scale = (float) screenWidth / w;
		float scale2 = (float) screenHight / h;

		scale = scale < scale2 ? scale : scale2;

		// 保证图片不变形.
		matrix.postScale(scale, scale);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		if(bitmap != null && !bitmap.isRecycled())
		{
			bitmap.recycle();
			bitmap = null;
		}
		// w,h是原图的属性.
		return newBitmap;
	}
	/***
	 * 等比例压缩图片
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @param screenHight
	 * @return
	 */
	public static Bitmap getBitmap(Bitmap bitmap, int screenWidth) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Log.e("jj", "图片宽度" + w + ",screenWidth=" + screenWidth);
		Matrix matrix = new Matrix();
		float scale = (float) screenWidth / w;
	
		// 保证图片不变形.
		matrix.postScale(scale, scale);
		// w,h是原图的属性.
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}
	public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		Bitmap newbmp = null;
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidht = ((float) w / width);
			float scaleHeight = ((float) h / height);
			matrix.postScale(scaleWidht, scaleHeight);
			newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
					true);
		}
		return newbmp;
	}

	@SuppressWarnings("deprecation")
	public static int freeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;

		return (int) sdFreeMB;
	}

	public static void saveBitmap(String path, Bitmap mBitmap) {
		FileOutputStream fOut = null;
		try {
			File f = new File(path);
			f.createNewFile();
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();

		} catch (IOException e) {
			e.printStackTrace();
			FileLog.e(TAG, e.toString());
		} finally {
			if (fOut != null) {
				try {
					fOut.close();
				} catch (IOException e) {
					e.printStackTrace();
					FileLog.e(TAG, e.toString());
				}
			}
		}
	}

	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable == null)
			return null;
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap
					.createBitmap(
							drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		} else {
			return null;
		}
	}

	public static Bitmap getLocalBitmap(Bitmap bitmap) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scale = (float) 150 / w;

		// 保证图片不变形.
		matrix.postScale(scale, scale);
		// w,h是原图的属性.
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}

	public static Bitmap getBitmap(String path, int width, int height) {
		Options ops = new Options();
		ops.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, ops);
		int XScale = ops.outWidth / width;
		int YScale = ops.outHeight / height;
		int scale = XScale > YScale ? XScale : YScale;
		if (scale < 1) {
			scale = 1;
		}
		ops.inJustDecodeBounds = false;
		ops.inSampleSize = scale;
		Bitmap bm = BitmapFactory.decodeFile(path, ops);
		return bm;
	}

	public static Bitmap compressBmpFromBmp(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 100;
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		while (baos.toByteArray().length / 1024 > 1024) {
			baos.reset();
			options -= 10;
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
	}

	public static Bitmap compressImageFromFile(String srcPath, int width,
			int height) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);


		int be = newOpts.outWidth / width;
		int heightBe = newOpts.outHeight / height;

		/*int degree = BitmapUtil.readPictureDegree(srcPath);
		if (degree == 90 || degree == 270) {
			be = newOpts.outHeight / width;
			heightBe = newOpts.outWidth / height;
		}*/

		be = Math.min(be, heightBe);
		if (be <= 0) {
			be = 1;
		}
		
		newOpts.inJustDecodeBounds = false;
		newOpts.inSampleSize = be;// 设置采样率
		newOpts.inPreferredConfig = Config.RGB_565;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		// int degree = BitmapUtil.readPictureDegree(srcPath);
	/*	if (degree > 0) {
			bitmap = BitmapUtil.rotateBitmap(bitmap, degree);
		}*/
		
		if(newOpts.outWidth < width || newOpts.outHeight < height)
			return bitmap;
		
		return scaleBitmap(bitmap, width, height);
	}
	public static Bitmap compressImageFromFile(String srcPath, int width) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);


		int be = newOpts.outWidth / width;
		//int heightBe = newOpts.outHeight / height;

		/*int degree = BitmapUtil.readPictureDegree(srcPath);
		if (degree == 90 || degree == 270) {
			be = newOpts.outHeight / width;
			heightBe = newOpts.outWidth / height;
		}*/
	
		
		//be = Math.min(be, heightBe);
		if (be <= 0) {
			//be = 1;
			return BitmapFactory.decodeFile(srcPath);
		}
		
		newOpts.inJustDecodeBounds = false;
		newOpts.inSampleSize = be;// 设置采样率
		newOpts.inPreferredConfig = Config.RGB_565;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		// int degree = BitmapUtil.readPictureDegree(srcPath);
	/*	if (degree > 0) {
			bitmap = BitmapUtil.rotateBitmap(bitmap, degree);
		}*/
		
		
		return scaleBitmap(bitmap, width);
	}
	
	public static Bitmap scaleBitmap(Bitmap bitmap, int targetWidth) {
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();

			if (width == targetWidth) {
				return bitmap;
			}
		
			float widthScale = (targetWidth * 1f) / width;
		


		/*	if (widthScale > 10) {
				recycleBitmap(bitmap);
				return null;
			}*/

			Matrix matrix;
			synchronized (BitmapUtil.class) {
				matrix = sMatrix;
				sMatrix = null;
			}
			if (matrix == null) {
				matrix = new Matrix();
			}
			matrix.reset();
			Log.d(TAG, "Jim, scaleBitmap, widthScale: " + widthScale);
			matrix.setScale(widthScale, widthScale);
			Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);

			synchronized (BitmapUtil.class) {
				sMatrix = matrix;
			}

			if (newBitmap == null) {
				newBitmap = bitmap;
			} else {
				if (newBitmap != bitmap) {
					recycleBitmap(bitmap);
				}
				Log.d(TAG,
						"Jim, scaleBitmap, after scale, width: "
								+ newBitmap.getWidth() + ", height: "
								+ newBitmap.getHeight());
			}

			return newBitmap;
		}

		return bitmap;
	}

	private static Matrix sMatrix = new Matrix();

	private static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}

	public static Bitmap scaleBitmap(Bitmap bitmap, int targetWidth,
			int targetHeight) {
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			Log.d(TAG, "Jim, scaleBitmap, width: " + width + ", height: "
					+ height);

			if (width == targetWidth || height == targetHeight) {
				return cropBitmap(bitmap, targetWidth, targetHeight);
			}
		
			float widthScale = (targetWidth * 1f) / width;
			float heightScale = (targetHeight * 1f) / height;

			if (widthScale < 1 && heightScale < 1) {
				widthScale = Math.max(widthScale, heightScale);
				heightScale = widthScale;
			} else if (widthScale > 1 && heightScale > 1) {
				widthScale = Math.max(widthScale, heightScale);
				heightScale = widthScale;
			} else {
				// 一个维度要放大，一个维度要缩小
				widthScale = Math.max(widthScale, heightScale);
				heightScale = widthScale;
			}

			if (widthScale > 10) {
				recycleBitmap(bitmap);
				return null;
			}

			Matrix matrix;
			synchronized (BitmapUtil.class) {
				matrix = sMatrix;
				sMatrix = null;
			}
			if (matrix == null) {
				matrix = new Matrix();
			}
			matrix.reset();
			Log.d(TAG, "Jim, scaleBitmap, widthScale: " + widthScale
					+ ", heightScale: " + heightScale);
			matrix.setScale(widthScale, heightScale);
			Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
					matrix, true);

			synchronized (BitmapUtil.class) {
				sMatrix = matrix;
			}

			if (newBitmap == null) {
				newBitmap = bitmap;
			} else {
				if (newBitmap != bitmap) {
					recycleBitmap(bitmap);
				}
				Log.d(TAG,
						"Jim, scaleBitmap, after scale, width: "
								+ newBitmap.getWidth() + ", height: "
								+ newBitmap.getHeight());
			}

			return newBitmap;
		}

		return bitmap;
	}

	public static Bitmap cropBitmap(Bitmap bitmap, int targetWidth,
			int targetHeight) {
		return cropBitmap(bitmap, targetWidth, targetHeight, false);
	}

	/**
	 * 裁剪图片
	 * 
	 * @param bitmap
	 * @param targetWidth
	 * @param targetHeight
	 * @param cropFromTop
	 *            :true表示从顶部开始裁剪，false表示保留中间的
	 * @return
	 */
	public static Bitmap cropBitmap(Bitmap bitmap, int targetWidth,
			int targetHeight, boolean cropFromTop) {
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			Log.d(TAG, "Jim, cropBitmap, width: " + width + ", height: "
					+ height);
			if (width > targetWidth || height > targetHeight) {
				Rect src = new Rect();
				Rect dst = new Rect(0, 0, targetWidth, targetHeight);
				if (width > targetWidth) {
					src.left = (width - targetWidth) / 2;
					src.right = width - src.left;
				} else {
					src.left = 0;
					src.right = width;
				}

				if (height > targetHeight) {
					if (!cropFromTop) {
						src.top = (height - targetHeight) / 2;
						src.bottom = height - src.top;
					} else {
						src.top = 0;
						src.bottom = targetHeight;
					}
				} else {
					src.top = 0;
					src.bottom = height;
				}
				Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
						targetHeight, bitmap.getConfig());
				Canvas canvas = new Canvas(targetBitmap);
				canvas.drawBitmap(bitmap, src, dst, null);
				recycleBitmap(bitmap);
				bitmap = targetBitmap;
			}
		}
		return bitmap;
	}

	// 将图片做圆角处理
	public static Bitmap roundCorners(Bitmap source, float radius) {
		int width = source.getWidth();
		int height = source.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(android.graphics.Color.WHITE);

		Bitmap clipped = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(clipped);
		canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius,
				paint);

		paint.setXfermode(new PorterDuffXfermode(
				android.graphics.PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);

		//source.recycle();

		return clipped;
	}

	// 将图片做上圆角处理
	public static Bitmap roundTopCorners(Bitmap source, float radius) {
		int width = source.getWidth();
		int height = source.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(android.graphics.Color.WHITE);

		Bitmap clipped = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(clipped);
		canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius,
				paint);
		// 遮住下圆角
		canvas.drawRect(new Rect(0, height / 2, width, height), paint);

		paint.setXfermode(new PorterDuffXfermode(
				android.graphics.PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);

	//	source.recycle();

		return clipped;
	}

	// 压缩图片
	public static byte[] compressImage(Bitmap image) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		int options = 100;
		while (bos.toByteArray().length > MAX_IMAGE * 1024) {
			bos.reset();
			options -= 10;
			Log.i("test2", String.valueOf(options));
			image.compress(Bitmap.CompressFormat.JPEG, options, bos);
		}
		return bos.toByteArray();
	}

	public static Bitmap getVideoThumbnail(String videoPath, int width,
			int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}

		return bitmap;
	}

	public static Bitmap getRoundVideoThumbnail(String videoPath, int width,
			int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		if (bitmap != null) {
			bitmap = roundTopCorners(bitmap, 10);
		}

		return bitmap;
	}

	// 图片和成
	public static Bitmap combineBitmap(Bitmap mainBitmap, Bitmap warterMark) {
		if (mainBitmap == null)
			return null;
		int mainBitmapWidth = mainBitmap.getWidth();
		int mainBitmapHeight = mainBitmap.getHeight();
		if (warterMark == null) {
			return mainBitmap;
		}
		int warkMarkWidth = warterMark.getWidth();
		int warkMarkHeight = warterMark.getHeight();
		Bitmap newBitmap = Bitmap.createBitmap(mainBitmapWidth,
				mainBitmapHeight, Config.RGB_565);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(mainBitmap, 0, 0, null);
		cv.drawBitmap(warterMark, (mainBitmapWidth - warkMarkWidth) / 2,
				(mainBitmapHeight - warkMarkHeight) / 2, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		mainBitmap.recycle();
		warterMark.recycle();
		return newBitmap;
	}

	/**
	 * 计算ImageView的大小（BitmapDrawable）
	 * 
	 * @param resources
	 * @param resourceId
	 * @return
	 */
	public static int[] computeWH(Resources resources, int resourceId) {
		int[] wh = { 0, 0 };
		if (resources == null)
			return wh;
		Bitmap mBitmap = BitmapFactory.decodeResource(resources, resourceId);
		BitmapDrawable bDrawable = new BitmapDrawable(resources, mBitmap);
		wh[0] = bDrawable.getIntrinsicWidth();
		wh[1] = bDrawable.getIntrinsicHeight();

		return wh;
	}

	/**
	 * 计算ImageView的大小（decodeFileDescriptor）
	 * 
	 * @param imageFile
	 * @return
	 */
	public static int[] computeWH_1(String imageFile) {
		int[] wh = { 0, 0 };
		if (imageFile == null || imageFile.length() == 0)
			return wh;

		final String FILE_PREFIX = "file://";
		if (imageFile.startsWith(FILE_PREFIX)) {
			imageFile = imageFile.substring(FILE_PREFIX.length());
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(imageFile);
			FileDescriptor fd = fis.getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return wh;
			}
			wh[0] = options.outWidth;
			wh[1] = options.outHeight;

		} catch (Exception e) {
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}

		return wh;
	}

	/**
	 * 计算ImageView的大小（decodeFile）
	 * 
	 * @param imgFile
	 * @return
	 */
	public static int[] computeWH_2(String imgFile) {
		int[] wh = { 0, 0 };

		if (imgFile == null || imgFile.length() == 0)
			return wh;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgFile, options);
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			return wh;
		}

		wh[0] = options.outWidth;
		wh[1] = options.outHeight;

		return wh;
	}

	/**
	 * 计算ImageView的大小（decodeResource）
	 * 
	 * @param resources
	 * @param resourceId
	 * @return
	 */
	public static int[] computeWH_3(Resources resources, int resourceId) {
		int[] wh = { 0, 0 };
		if (resources == null)
			return wh;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resourceId, options);
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			return wh;
		}

		wh[0] = options.outWidth;
		wh[1] = options.outHeight;

		return wh;
	}

	/**
	 * 计算ImageView的大小
	 * 
	 * @param imgFile
	 * @return
	 */
	public static int[] computeWH_4(String imgFile) {
		int[] wh = { 0, 0 };

		if (imgFile == null || imgFile.length() == 0)
			return wh;

		try {
			final String FILE_PREFIX = "file://";
			if (imgFile.startsWith(FILE_PREFIX)) {
				imgFile = imgFile.substring(FILE_PREFIX.length());
			}
			ExifInterface exifInterface = new ExifInterface(imgFile);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int width = exifInterface.getAttributeInt(
					ExifInterface.TAG_IMAGE_WIDTH,
					ExifInterface.ORIENTATION_UNDEFINED);
			int length = exifInterface.getAttributeInt(
					ExifInterface.TAG_IMAGE_LENGTH,
					ExifInterface.ORIENTATION_UNDEFINED);
			int degree = 0;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}

			if (degree == 0 || degree == 180) {
				wh[0] = width;
				wh[1] = length;
			} else {
				wh[0] = length;
				wh[1] = width;
			}
		} catch (IOException e) {
			FileLog.e(TAG, e.getMessage());
		}

		return wh;
	}

	/**
	 * 获得照片的旋转角度
	 * 
	 * @param imagePath
	 * @return
	 */
	public static int readPictureDegree(String imagePath) {
		int degree = 0;
		try {
			final String FILE_PREFIX = "file://";
			if (imagePath.startsWith(FILE_PREFIX)) {
				imagePath = imagePath.substring(FILE_PREFIX.length());
			}
			ExifInterface exifInterface = new ExifInterface(imagePath);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			FileLog.e(TAG, e.getMessage());
		}

		return degree;
	}

	/**
	 * 将指定的图片旋转指定的角度
	 * 
	 * @param bitmap
	 * @param angle
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
		if (bitmap != null && angle > 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			Bitmap roatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
			bitmap = roatedBitmap;
		}

		return bitmap;
	}

	public static Bitmap getThumbBitmap(String path, int width, int height) {
		Bitmap bm = compressImageFromFile(path, width, height);

		if (bm != null && (bm.getWidth() > width || bm.getHeight() > height)) {
			bm = cropBitmap(bm, width, height, true);
		}

		return bm;
	}

	public static byte[] bmpToByteArray(final Bitmap bmp) {
		ByteBuffer buffer = ByteBuffer.allocate(bmp.getByteCount());
		bmp.copyPixelsToBuffer(buffer);
		return buffer.array();
	}

	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 获取图片文件的信息，是否旋转了90度，如果是则反转
	 * 
	 * @param bitmap
	 *            需要旋转的图片
	 * @param path
	 *            图片的路径
	 */
	public static Bitmap reviewPicRotate(Bitmap bitmap, String path) {
		int degree = getPicRotate(path);
		if (degree != 0) {
			Matrix m = new Matrix();
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			m.setRotate(degree); // 旋转angle度
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
		}
		return bitmap;
	}

	/**
	 * 读取图片文件旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return 图片旋转的角度
	 */
	public static int getPicRotate(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	
	public static Bitmap makeRoundCorner(Bitmap bitmap)  
	{  
	    int width = bitmap.getWidth();  
	    int height = bitmap.getHeight();  
	    int left = 0, top = 0, right = width, bottom = height;  
	    float roundPx = height/2;  
	    if (width > height) {  
	        left = (width - height)/2;  
	        top = 0;  
	        right = left + height;  
	        bottom = height;  
	    } else if (height > width) {  
	        left = 0;  
	        top = (height - width)/2;  
	        right = width;  
	        bottom = top + width;  
	        roundPx = width/2;  
	    }  

	  
	    Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
	    Canvas canvas = new Canvas(output);  
	    int color = 0xff424242;  
	    Paint paint = new Paint();  
	    Rect rect = new Rect(left, top, right, bottom);  
	    RectF rectF = new RectF(rect);  
	  
	    paint.setAntiAlias(true);  
	    canvas.drawARGB(0, 0, 0, 0);  
	    paint.setColor(color);  
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
	    paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));  
	    canvas.drawBitmap(bitmap, rect, rect, paint);  
	    return output;  
	}  
	
	/**
	 * @Title: createRotateAnimation
	 * @Description: 创建旋转动画
	 * @param @return
	 * @return RotateAnimation
	 * @throws
	 */
    public static RotateAnimation createRotateAnimation(boolean reverse) {
		RotateAnimation animation = null;
		if (!reverse) {
			animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		} else {
			animation = new RotateAnimation(0, -3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		}
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(10000);
		animation.setStartOffset(0);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.RESTART);
		
		return animation;
	}
    
    /**
     * 裁剪圆的图片
     * @param bitmap
     * @param diameter 直径
     * @return
     */
    public static Bitmap clipCircleBitmap(Bitmap bitmap, int diameter) {
        if (bitmap == null) {
            return bitmap;
        }
        
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Config.ARGB_8888);
        Canvas canvas = new Canvas(output); 
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF rect = new RectF(0f, 0f, diameter, diameter);
        paint.setColor(Color.WHITE);
        canvas.drawOval(rect, paint);      
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        return output;
    }
    
}