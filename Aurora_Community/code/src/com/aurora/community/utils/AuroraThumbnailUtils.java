package com.aurora.community.utils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.util.Log;

public class AuroraThumbnailUtils {

	private static final int TARGET_SIZE_MINI_THUMBNAIL = 320;
	private static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;
	private static final int MAX_NUM_PIXELS_THUMBNAIL = 512 * 384;
	private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = 160 * 120;
	private static final int OPTIONS_RECYCLE_INPUT = 0x2;
	private static final int UNCONSTRAINED = -1;

	private static class SizedThumbnailBitmap {
		public byte[] mThumbnailData;
		public Bitmap mBitmap;
		public int mThumbnailWidth;
		public int mThumbnailHeight;
	}

	public static Bitmap createImageThumbnail(String filePath, int kind) {
		boolean wantMini = (kind == Images.Thumbnails.MINI_KIND);
		int targetSize = wantMini ? TARGET_SIZE_MINI_THUMBNAIL
				: TARGET_SIZE_MICRO_THUMBNAIL;
		int maxPixels = wantMini ? MAX_NUM_PIXELS_THUMBNAIL
				: MAX_NUM_PIXELS_MICRO_THUMBNAIL;
		SizedThumbnailBitmap sizedThumbnailBitmap = new SizedThumbnailBitmap();
		Bitmap bitmap = null;
		int lastDo = filePath.lastIndexOf(".");
		if (lastDo != 0) {
			String type = filePath.substring(lastDo - 1).toUpperCase(
					Locale.ROOT);
			if (type.equals("JPEG") || type.equals("JPG")) {
				createThumbnailFromEXIF(filePath, targetSize, maxPixels,
						sizedThumbnailBitmap);
				bitmap = sizedThumbnailBitmap.mBitmap;
			}
		}
		if (bitmap == null) {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(filePath);
				FileDescriptor fd = stream.getFD();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd, null, options);
				if (options.mCancel || options.outWidth == -1
						|| options.outHeight == -1) {
					return null;
				}
				options.inSampleSize = computeSampleSize(options, targetSize,
						maxPixels);
				options.inJustDecodeBounds = false;

				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
			} catch (IOException ex) {
			} catch (OutOfMemoryError oom) {
			} finally {
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException ex) {
				}
			}

		}

		if (kind == Images.Thumbnails.MICRO_KIND) {
			// now we make it a "square thumbnail" for MICRO_KIND thumbnail
			bitmap = ThumbnailUtils.extractThumbnail(bitmap,
					TARGET_SIZE_MICRO_THUMBNAIL, TARGET_SIZE_MICRO_THUMBNAIL,
					OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	private static int computeSampleSize(BitmapFactory.Options options,
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

	private static void createThumbnailFromEXIF(String filePath,
			int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
		if (filePath == null)
			return;

		ExifInterface exif = null;
		byte[] thumbData = null;
		try {
			exif = new ExifInterface(filePath);
			thumbData = exif.getThumbnail();
		} catch (IOException ex) {
		}

		BitmapFactory.Options fullOptions = new BitmapFactory.Options();
		BitmapFactory.Options exifOptions = new BitmapFactory.Options();
		int exifThumbWidth = 0;
		int fullThumbWidth = 0;

		// Compute exifThumbWidth.
		if (thumbData != null) {
			exifOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length,
					exifOptions);
			exifOptions.inSampleSize = computeSampleSize(exifOptions,
					targetSize, maxPixels);
			exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
		}

		// Compute fullThumbWidth.
		fullOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, fullOptions);
		fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize,
				maxPixels);
		fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;

		// Choose the larger thumbnail as the returning sizedThumbBitmap.
		if (thumbData != null && exifThumbWidth >= fullThumbWidth) {
			int width = exifOptions.outWidth;
			int height = exifOptions.outHeight;
			exifOptions.inJustDecodeBounds = false;
			sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData,
					0, thumbData.length, exifOptions);
			if (sizedThumbBitmap.mBitmap != null) {
				sizedThumbBitmap.mThumbnailData = thumbData;
				sizedThumbBitmap.mThumbnailWidth = width;
				sizedThumbBitmap.mThumbnailHeight = height;
			}
		} else {
			fullOptions.inJustDecodeBounds = false;
			sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath,
					fullOptions);
		}
	}

}
