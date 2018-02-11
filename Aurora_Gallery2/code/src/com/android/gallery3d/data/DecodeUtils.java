/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;
import android.util.FloatMath;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecodeUtils {
    private static final String TAG = "DecodeUtils";

    private static class DecodeCanceller implements CancelListener {
        Options mOptions;

        public DecodeCanceller(Options options) {
            mOptions = options;
        }

        @Override
        public void onCancel() {
            mOptions.requestCancelDecode();
        }
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    public static void setOptionsMutable(Options options) {
        if (ApiHelper.HAS_OPTIONS_IN_MUTABLE) options.inMutable = true;
    }

    public static Bitmap decode(JobContext jc, FileDescriptor fd, Options options) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeFileDescriptor(fd, null, options));
    }

    public static void decodeBounds(JobContext jc, FileDescriptor fd,
            Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, Options options) {
        return decode(jc, bytes, 0, bytes.length, options);
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, int offset,
            int length, Options options) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeByteArray(bytes, offset, length, options));
    }

    public static void decodeBounds(JobContext jc, byte[] bytes, int offset,
            int length, Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeByteArray(bytes, offset, length, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decodeThumbnail(
            JobContext jc, String filePath, Options options, int targetSize, int type) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return decodeThumbnail(jc, fd, options, targetSize, type);
        } catch (Exception ex) {
        	Log.i("SQF_LOG", "decodeThumbnail failed...", ex);
            //Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    //Aurora <SQF> <2014-05-12>  for NEW_UI begin
    
    public static Bitmap decodeClearThumbnail(JobContext jc, String filePath, Options options, int targetSize, int type) {
    	FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return decodeClearThumbnail(jc, fd, options, targetSize, type);
        } catch (Exception ex) {
        	Log.i("SQF_LOG", "decodeClearThumbnail failed...", ex);
            //Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }
    

    //Aurora <SQF> <2014-07-18>  for NEW_UI begin
    
    public static void decodeBounds(String filePath, Rect outBounds) {
    	FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            outBounds.right = options.outWidth;
            outBounds.bottom = options.outHeight;
        } catch (Exception ex) {
        	Log.i("SQF_LOG", "decodeBounds failed...", ex);
            return;
        } finally {
            Utils.closeSilently(fis);
        }
    }
    //Aurora <SQF> <2014-07-18>  for NEW_UI end
    
    public static Bitmap decodeClearThumbnail(JobContext jc, FileDescriptor fd, Options options, int targetSize, int type) {
    	if (options == null) options = new Options();
    	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        jc.setCancelListener(new DecodeCanceller(options));
        if (jc.isCancelled()) return null;
        
        decodeBounds(jc, fd, options);
        int w = options.outWidth;
        int h = options.outHeight;
        options.inJustDecodeBounds = false;   
        float scale = (float) targetSize / Math.min(w, h);
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        setOptionsMutable(options);
    	try {
    		BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fd, false);
    		Rect region = new Rect(0, 0, w, h);
    		int cropSize = targetSize;
    		cropSize = w >= h ? h : w;
    		int left = (w - cropSize) >> 1;
    		int top = (h- cropSize) >> 1;
    		region.set(left, top, left + cropSize, top + cropSize);
    		Bitmap bmp = decoder.decodeRegion(region, options);
//    		Log.i("SQF_LOG", "DecodeUtils::decodeCenterCrop decode region: " + region.toString() + 
//					" region:" + region.toString() + 
//					" w:" + w +
//					" h:" + h + 
//					" bmp.getWidth:" + bmp.getWidth() + 
//					" bmp.getHeight:" + bmp.getHeight());
    		return bmp;
    		//return BitmapUtils.resizeDownBySideLength(bmp, targetSize, true);
    	} catch(IOException	ex) {
    		//Log.i("SQF_LOG", "DecodeUtils::decodeClearThumbnail failed..." + ex.toString());
    	} finally {
        }
    	return null;
    }
    //Aurora <SQF> <2014-05-12>  for NEW_UI end

    public static Bitmap decodeThumbnail(
            JobContext jc, FileDescriptor fd, Options options, int targetSize, int type) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (jc.isCancelled()) return null;

        int w = options.outWidth;
        int h = options.outHeight;

        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            // We center-crop the original image as it's micro thumbnail. In this case,
            // we want to make sure the shorter side >= "targetSize".
            float scale = (float) targetSize / Math.min(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);

            // For an extremely wide image, e.g. 300x30000, we may got OOM when decoding
            // it for TYPE_MICROTHUMBNAIL. So we add a max number of pixels limit here.
            final int MAX_PIXEL_COUNT = 640000; // 400 x 1600
            if ((w / options.inSampleSize) * (h / options.inSampleSize) > MAX_PIXEL_COUNT) {
                options.inSampleSize = BitmapUtils.computeSampleSize(
                        FloatMath.sqrt((float) MAX_PIXEL_COUNT / (w * h)));
            }
        } else {
            // For screen nail, we only want to keep the longer side >= targetSize.
            float scale = (float) targetSize / Math.max(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        }

        options.inJustDecodeBounds = false;
        setOptionsMutable(options);

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) return null;

        // We need to resize down if the decoder does not support inSampleSize
        // (For example, GIF images)
        float scale = (float) targetSize / (type == MediaItem.TYPE_MICROTHUMBNAIL
                ? Math.min(result.getWidth(), result.getHeight())
                : Math.max(result.getWidth(), result.getHeight()));

        if (scale <= 0.5) result = BitmapUtils.resizeBitmapByScale(result, scale, true);
        return ensureGLCompatibleBitmap(result);
    }

    /**
     * Decodes the bitmap from the given byte array if the image size is larger than the given
     * requirement.
     *
     * Note: The returned image may be resized down. However, both width and height must be
     * larger than the <code>targetSize</code>.
     */
    public static Bitmap decodeIfBigEnough(JobContext jc, byte[] data,
            Options options, int targetSize) {
        if (options == null) options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (jc.isCancelled()) return null;
        if (options.outWidth < targetSize || options.outHeight < targetSize) {
            return null;
        }
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);

        return ensureGLCompatibleBitmap(
                BitmapFactory.decodeByteArray(data, 0, data.length, options));
    }

    // TODO: This function should not be called directly from
    // DecodeUtils.requestDecode(...), since we don't have the knowledge
    // if the bitmap will be uploaded to GL.
    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null) return bitmap;
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        return newBitmap;
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, byte[] bytes, int offset, int length,
            boolean shareable) {
        if (offset < 0 || length <= 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException(String.format(
                    "offset = %s, length = %s, bytes = %s",
                    offset, length, bytes.length));
        }

        try {
            return BitmapRegionDecoder.newInstance(
                    bytes, offset, length, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, String filePath, boolean shareable) {
            try {
				return BitmapRegionDecoder.newInstance(filePath, shareable);
			//Aurora <SQF> <2014-05-27>  for NEW_UI begin
			//ORIGINALLY:
//		        } catch (Throwable t)  {
//	            Log.w(TAG, t);
//	            return null;
//	        }
			//SQF MODIFIED TO:
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, e);
	            return null;
			}
            //Aurora <SQF> <2014-05-27>  for NEW_UI end
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, FileDescriptor fd, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(fd, shareable);
        } catch (Throwable t)  {
            Log.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(
            JobContext jc, InputStream is, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(is, shareable);
        } catch (Throwable t)  {
            // We often cancel the creating of bitmap region decoder,
            // so just log one line.
            Log.w(TAG, "requestCreateBitmapRegionDecoder: " + t);
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decode(JobContext jc, byte[] data, int offset,
            int length, BitmapFactory.Options options, BitmapPool pool) {
        if (pool == null) {
            return decode(jc, data, offset, length, options);
        }

        if (options == null) options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1)
                ? findCachedBitmap(pool, jc, data, offset, length, options) : null;
        try {
            Bitmap bitmap = decode(jc, data, offset, length, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                pool.recycle(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) throw e;

            Log.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            pool.recycle(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, data, offset, length, options);
        }
    }

    // This is the same as the method above except the source data comes
    // from a file descriptor instead of a byte array.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decode(JobContext jc,
            FileDescriptor fileDescriptor, Options options, BitmapPool pool) {
        if (pool == null) {
            return decode(jc, fileDescriptor, options);
        }

        if (options == null) options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1)
                ? findCachedBitmap(pool, jc, fileDescriptor, options) : null;
        try {
            Bitmap bitmap = DecodeUtils.decode(jc, fileDescriptor, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                pool.recycle(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) throw e;

            Log.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            pool.recycle(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, fileDescriptor, options);
        }
    }

    private static Bitmap findCachedBitmap(BitmapPool pool, JobContext jc,
            byte[] data, int offset, int length, Options options) {
        if (pool.isOneSize()) return pool.getBitmap();
        decodeBounds(jc, data, offset, length, options);
        return pool.getBitmap(options.outWidth, options.outHeight);
    }

    private static Bitmap findCachedBitmap(BitmapPool pool, JobContext jc,
            FileDescriptor fileDescriptor, Options options) {
        if (pool.isOneSize()) return pool.getBitmap();
        decodeBounds(jc, fileDescriptor, options);
        return pool.getBitmap(options.outWidth, options.outHeight);
    }
}
