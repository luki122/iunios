package com.android.keyguard.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import com.android.keyguard.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;

/*
 * 
 * lockscreen background tools
 */
public class LockScreenBgUtils {
	private static final String TAG = "LockScreenBgUtils";
	public static final String LOCK_TAG = "LockpaperChange";
	private static final String PATH = "/data/aurora/change/lockscreen/wallpaper.png";
	private static final String ACTION_CHMOD_FILE = "com.aurora.change.chmodfile";
	private static LockScreenBgUtils sLockScreenUtils;
	private static SoftReference<Bitmap> mSoftRef = null;

	private LockScreenBgUtils() {
	}

	public synchronized static LockScreenBgUtils getInstance() {
		if (sLockScreenUtils == null) {
			sLockScreenUtils = new LockScreenBgUtils();
		}
		return sLockScreenUtils;
	}

	/**
	 * 设置壁纸
	 * 
	 * @param ImageView
	 *            getContext
	 * @return
	 */
	public boolean setViewBg(ImageView v) {
		Bitmap bitmap = getBackgroundBitmap(v.getContext());
		if (bitmap != null) {
			BitmapDrawable bitmapDrawable = new BitmapDrawable(
					v.getResources(), bitmap);
			bitmapDrawable.setDither(false);
			return resetBackground(v, bitmapDrawable);
		} else {
			Drawable drawable = v.getContext().getResources()
					.getDrawable(R.drawable.default_lockpaper);
			v.setImageDrawable(drawable);
			Log.d(LOCK_TAG, "setViewBg: setImageDrawable ");
			return true;
		}
	}

	private boolean resetBackground(ImageView v, Drawable drawable) {
		if (drawable != null) {
			v.setImageDrawable(drawable);
			drawable = null;
			Log.d(LOCK_TAG, "resetBackground: 2 ");
			return true;
		}
		Log.d(LOCK_TAG, "resetBackground: 1 ");
		return false;
	}

	/**
	 * 获取高斯模糊后的壁纸
	 * 
	 * @param view
	 *            getContext
	 * @return
	 */
	public Bitmap getLockScreenBlurBg(View view) {
		Bitmap blurBitmap = null;
		Bitmap oldBitmap = getBackgroundBitmap(view.getContext());
		if (oldBitmap != null) {
			blurBitmap = Blur.fastblur(view.getContext(), small(oldBitmap), 23);
		}
		return blurBitmap;
	}

	private Bitmap getBackgroundBitmap(Context context) {
		File file = new File(PATH);
		if (file.exists()) {
			long modifyTime = file.lastModified();
			long oldTime = DataOperation.getInstance(context).getLong(
					"fileTime", modifyTime);
			Log.d(LOCK_TAG, modifyTime + " = getBackgroundBitmap = " + oldTime);
			if (modifyTime != oldTime) {
				if (mSoftRef != null) {
					mSoftRef.clear();
					mSoftRef = null;
				}
			}
		} else {
			if (mSoftRef != null) {
				mSoftRef.clear();
				mSoftRef = null;
			}
		}
		Bitmap bitmap = null;
		if (mSoftRef != null) {
			bitmap = mSoftRef.get();
			Log.d(LOCK_TAG, TAG + " getBackgroundBitmap: bitmap = " + bitmap);
		}
		if (bitmap == null) {
			if (file.exists()) {
				bitmap = getCurrentWallpaperLocked(context, file);
				if (bitmap != null) {
					Log.d(LOCK_TAG, TAG
							+ " getCurrentWallpaperLocked: bitmap = " + bitmap);
					// return bitmap;
				} else {
					bitmap = getDefaultWallpaperLocked(context);
					Log.d(LOCK_TAG, TAG
							+ " getDefaultWallpaperLocked: bitmap = " + null);
					// return bitmap;
				}
			} else {
				bitmap = getDefaultWallpaperLocked(context);
				Log.d(LOCK_TAG, "2-->getDefaultWallpaperLocked: bitmap = "
						+ null);
				// return bitmap;
			}
		}
		return bitmap;
	}

	private Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(0.08f, 0.08f); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	/**
	 * 获取当前壁纸
	 * 
	 * @param context
	 * @return
	 */
	private Bitmap getCurrentWallpaperLocked(Context context, File file) {
		try {
			/*
			 * File f = new File(PATH); if (!f.exists()) { return null; }
			 */
			if (file != null) {
				try {
					Bitmap bm = decodeSampledBitmapFromFile(
							file.getAbsolutePath());
					if (mSoftRef != null) {
						mSoftRef.clear();
						mSoftRef = null;
					}
					if (bm != null) {
						mSoftRef = new SoftReference<Bitmap>(bm);
						long modifyTime = file.lastModified();
						DataOperation.getInstance(context).putLong("fileTime",
								modifyTime);
						bm = null;
						Log.d(LOCK_TAG,
								"getCurrentWallpaperLocked: modifyTime = "
										+ modifyTime);
						return mSoftRef.get();
					}
				} catch (OutOfMemoryError err) {
					Log.d(LOCK_TAG,
							"getCurrentWallpaperLocked: OutOfMemoryError ");
				} catch (Exception e) {
					Log.d(LOCK_TAG, "getCurrentWallpaperLocked: Exception--> "
							+ e.toString());
				} 
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log.d(LOCK_TAG, "getCurrentWallpaperLocked: " + e.toString());
			sendChmodFileBroadcast(context);
		}
		return null;
	}

	/**
	 * 获取默认壁纸
	 * 
	 * @param context
	 * @return
	 */
	private Bitmap getDefaultWallpaperLocked(Context context) {
		try {
			try {
				Bitmap bm = decodeSampledBitmapFromResource(
						context.getResources(),
						R.drawable.default_lockpaper);
				if (bm != null) {
					/*
					 * mSoftRef = new SoftReference<Bitmap>(bm); bm = null;
					 */
					Log.d(LOCK_TAG, "getDefaultWallpaperLocked: bm");
					return bm;
				}
			} catch (OutOfMemoryError e) {
				Log.d(LOCK_TAG, "getDefaultWallpaperLocked: OutOfMemoryError");
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.d(LOCK_TAG,
					"getDefaultWallpaperLocked: Exception = " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	private void sendChmodFileBroadcast(Context context) {
		// Aurora liugj 2015-01-21 added for bug-11142 start
		boolean isBootCompleted = SystemProperties.get("sys.boot_completed",
				"0").equals("1") ? true : false;
		if (isBootCompleted) {
			Intent intent = new Intent();
			intent.setAction(ACTION_CHMOD_FILE);
			context.sendBroadcast(intent);
		}
		// Aurora liugj 2015-01-21 added for bug-11142 end
	}


	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public Bitmap decodeSampledBitmapFromFile(String path) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		return BitmapFactory.decodeFile(path, options);
	}
	
}
