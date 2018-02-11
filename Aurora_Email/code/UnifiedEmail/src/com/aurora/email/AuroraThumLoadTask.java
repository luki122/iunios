package com.aurora.email;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;

import com.android.mail.providers.Attachment;
import com.android.mail.ui.AttachmentBitmapHolder;
import com.android.mail.utils.MyLog;
import com.android.ex.photo.util.ImageUtils;
import com.android.ex.photo.util.Exif;

public class AuroraThumLoadTask extends AsyncTask<Uri, Void, Bitmap> {

	private static final String TAG = "AuroraComposeActivity";

	private final AttachmentBitmapHolder mHolder;
	private final int mWidth;
	private final int mHeight;
	private String contentType;
	private Context mContext;
	
	public static void setupThumbnailPreview(AuroraThumLoadTask task,
			final AttachmentBitmapHolder holder, final Attachment attachment,
			final Attachment prevAttachment,Context context) {
		final int width = holder.getThumbnailWidth();
		final int height = holder.getThumbnailHeight();
		final String Type = attachment.getContentType();

		if (attachment == null || width == 0 || height == 0
				|| (!ImageUtils.isImageMimeType(Type)&&!MimeTypeUtil.isApkMimeType(Type))) {
			holder.setThumbnailToDefault();
			return;
		}

		final Uri thumbnailUri = attachment.thumbnailUri;
		final Uri contentUri = attachment.contentUri;
		final Uri uri = (prevAttachment == null) ? null : prevAttachment
				.getIdentifierUri();
		final Uri prevUri = (prevAttachment == null) ? null : prevAttachment
				.getIdentifierUri();

	/*	MyLog.d(TAG,
				"thumbnailUri:" + thumbnailUri + " contentUri:" + contentUri
						+ " holder.bitmapSetToDefault():"
						+ holder.bitmapSetToDefault() + " prevUri:" + prevUri
						+ " uri:" + uri);*/
		if ((thumbnailUri != null || contentUri != null)
				&& (holder.bitmapSetToDefault() || prevUri == null || !uri
						.equals(prevUri))) {
			// cancel/dispose any existing task and start a new one
			if (task != null) {
				task.cancel(true);
			}

			task = new AuroraThumLoadTask(holder, width, height,Type,context);
			task.executeOnExecutor(THREAD_POOL_EXECUTOR, thumbnailUri,
					contentUri);
			// task.execute(thumbnailUri, contentUri);
		} else if (thumbnailUri == null && contentUri == null) {
			// not an image, or no thumbnail exists. fall back to default.
			// async image load must separately ensure the default appears upon
			// load failure.
			holder.setThumbnailToDefault();
		}
	}

	public AuroraThumLoadTask(AttachmentBitmapHolder holder, int width,
			int height ,String type,Context context) {
		mHolder = holder;
		mWidth = width;
		mHeight = height;
		contentType = type;
		mContext = context;
	}

	@Override
	protected Bitmap doInBackground(Uri... arg0) {
	//	MyLog.d(TAG, "doInBackground.....start!!!");
		Bitmap result = loadBitmap(arg0[0]);
		if (result == null) {
			result = loadBitmap(arg0[1]);
		}
	//	MyLog.d(TAG, "doInBackground.....");
		return result;
	}

	private Bitmap loadBitmapfromFile(final Uri thumbnailUri) {
	//	MyLog.d(TAG, "loadBitmapfromFile.....");
		final BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inDensity = DisplayMetrics.DENSITY_LOW;
		BitmapFactory.decodeFile(thumbnailUri.getPath(), opts);

		if (isCancelled() || opts.outWidth == -1 || opts.outHeight == -1) {
			return null;
		}
	//	MyLog.d(TAG, "opts.outWidth:"+opts.outWidth+" mWidth:"+mWidth);
		opts.inJustDecodeBounds = false;
		final int wDivider = Math.max(opts.outWidth / mWidth, 1);
		final int hDivider = Math.max(opts.outHeight / mHeight, 1);
		opts.inSampleSize = Math.min(wDivider, hDivider);
		Bitmap originalBitmap = BitmapFactory.decodeFile(
				thumbnailUri.getPath(),opts);
		//MyLog.d(TAG, "bitmapsize111:"+getBitmapsize(originalBitmap)+" bitHeight:"+originalBitmap.getHeight());
		originalBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, mWidth, mHeight,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return originalBitmap;
	}
	
	private Bitmap loadBitmap(final Uri thumbnailUri) {
		if (thumbnailUri == null) {
			return null;
		}
		
		if(MimeTypeUtil.isApkMimeType(contentType)){
			
			return loadBitmapFromApk(thumbnailUri);
		}
		
		if (thumbnailUri.getScheme().equals(FujianInfo.FILE_SCHEMA)) {
			return loadBitmapfromFile(thumbnailUri);
		}

		final int orientation = getOrientation(thumbnailUri);
		AssetFileDescriptor fd = null;
		try {
			fd = mHolder.getResolver().openAssetFileDescriptor(thumbnailUri,
					"r");
			if (isCancelled() || fd == null) {
				return null;
			}

			final BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			opts.inDensity = DisplayMetrics.DENSITY_LOW;
			// BitmapFactory.decodeFile(thumbnailUri.getPath(), opts);
			BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null,
					opts);
			if (isCancelled() || opts.outWidth == -1 || opts.outHeight == -1) {
				return null;
			}

			opts.inJustDecodeBounds = false;
			// Shrink both X and Y (but do not over-shrink)
			// and pick the least affected dimension to ensure the thumbnail is
			// fillable
			// (i.e. ScaleType.CENTER_CROP)
			final int wDivider = Math.max(opts.outWidth / mWidth, 1);
			final int hDivider = Math.max(opts.outHeight / mHeight, 1);
			opts.inSampleSize = Math.min(wDivider, hDivider);

			Bitmap originalBitmap = BitmapFactory.decodeFileDescriptor(
					fd.getFileDescriptor(), null, opts);
			if (originalBitmap != null && orientation != 0) {
				final Matrix matrix = new Matrix();
				matrix.postRotate(orientation);
				return Bitmap.createBitmap(originalBitmap, 0, 0,
						originalBitmap.getWidth(), originalBitmap.getHeight(),
						matrix, true);
			}
			originalBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, mWidth, mHeight,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			return originalBitmap;
		} catch (Throwable t) {

		} finally {
			if (fd != null) {
				try {
					fd.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}


	private Bitmap loadBitmapFromApk(final Uri thumbnailUri){
		
		if (!thumbnailUri.getScheme().equals(FujianInfo.FILE_SCHEMA)) {
			return null;
		}
		Drawable drawable = getApkIcon(mContext, thumbnailUri.getPath());
	//	MyLog.d(TAG, "drawable:"+drawable);
		if(drawable!=null&&drawable instanceof BitmapDrawable){
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			
			return ThumbnailUtils.extractThumbnail(bitmap, mWidth, mHeight,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return null;
	}
	
	public static Drawable getApkIcon(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			try {
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e) {
				
			}
		}
		return null;
	}
	
	private int getOrientation(final Uri thumbnailUri) {
		if (thumbnailUri == null) {
			return 0;
		}

		InputStream in = null;
		try {
			final ContentResolver resolver = mHolder.getResolver();
			in = resolver.openInputStream(thumbnailUri);
			return Exif.getOrientation(in, -1);
		} catch (Throwable t) {

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {

				}
			}
		}
		return 0;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {

		if (result == null) {

			mHolder.thumbnailLoadFailed();
			return;
		}
		//MyLog.d(TAG, "bitmapsize:"+getBitmapsize(result)+" bitHeight:"+result.getHeight());
		mHolder.setThumbnail(result);
	}
	
	public long getBitmapsize(Bitmap bitmap) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();

	}
}
