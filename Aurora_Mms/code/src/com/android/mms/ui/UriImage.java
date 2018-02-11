/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import com.android.mms.model.ImageModel;
import com.android.mms.LogTag;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.pdu.PduPart;
// Aurora xuyong 2015-09-08 added for bug #15971 start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2015-09-08 added for bug #15971 end
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.storage.StorageManager;
// Aurora liugj 2013-11-12 modified for bug-627 start
import gionee.os.storage.GnStorageManager;
// Aurora liugj 2013-11-12 modified for bug-627 end
import android.provider.MediaStore.Images;
import android.provider.Telephony.Mms.Part;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import android.media.ExifInterface;
import android.drm.DrmManagerClient;

public class UriImage {
    private static final String TAG = "Mms/image";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Context mContext;
    private final Uri mUri;
    private String mContentType;
    private String mPath;
    private String mSrc;
    private int mWidth;
    private int mHeight;
    private final String JPEGCONTENTTYPE = "image/jpeg"; 
    private final String PNGCONTENTTYPE = "image/png"; 
    // Aurora xuyong 2015-08-14 modified for bug #15651 start
    public UriImage(Context context, Uri uri) throws IllegalArgumentException{
    // Aurora xuyong 2015-08-14 modified for bug #15651 end
        if ((null == context) || (null == uri)) {
            throw new IllegalArgumentException();
        }

        String scheme = uri.getScheme();
        if (scheme.equals("content")) {
            initFromContentUri(context, uri);
        } else if (uri.getScheme().equals("file")) {
            initFromFile(context, uri);
        }
        // check if this image is from temp dir captured by camera.
        // if Yes, change the name for coflict if two image are got from the temp dir, 
        // they are the same name before saveDraft
          // Aurora liugj 2013-11-12 modified for bug-627 start
        //StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        File mTempFile = GnStorageManager.getInstance(context).getMTKExternalCacheDir(context.getPackageName());
          // Aurora liugj 2013-11-12 modified for bug-627 end
        // Aurora xuyong 2015-08-14 added for bug #15651 start
        if (mPath == null) {
        	throw new IllegalArgumentException("Path is Null.");
        }
        // Aurora xuyong 2015-08-14 added for bug #15651 end
        if (mTempFile!=null&& mPath.equals(mTempFile.getAbsolutePath() + "/" + ".temp.jpg")) {
            mSrc = "image" + Long.toString(System.currentTimeMillis());
        } else {
            mSrc = mPath.substring(mPath.lastIndexOf('/') + 1);
        }

        if(mSrc.startsWith(".") && mSrc.length() > 1) {
            mSrc = mSrc.substring(1);
        }

        // Some MMSCs appear to have problems with filenames
        // containing a space.  So just replace them with
        // underscores in the name, which is typically not
        // visible to the user anyway.
        mSrc = mSrc.replace(' ', '_');
        Log.i(TAG, "ImageModel got mSrc: " + mSrc);
        mContext = context;
        mUri = uri;

        decodeBoundsInfo();

        if (LOCAL_LOGV) {
            Log.v(TAG, "UriImage uri: " + uri + " mPath: " + mPath + " mWidth: " + mWidth +
                    " mHeight: " + mHeight);
        }
    }

    private void initFromFile(Context context, Uri uri) {
        mPath = uri.getPath();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(mPath).toLowerCase();
        if (TextUtils.isEmpty(extension)) {
            // getMimeTypeFromExtension() doesn't handle spaces in filenames nor can it handle
            // urlEncoded strings. Let's try one last time at finding the extension.
            int dotPos = mPath.lastIndexOf('.');
            if (0 <= dotPos) {
                extension = mPath.substring(dotPos + 1);
                extension = extension.toLowerCase();
            }
        }
        Log.i(TAG, "ImageModel got file path: " + mPath);
        Log.i(TAG, "ImageModel got file extension: " + extension);
        mContentType = mimeTypeMap.getMimeTypeFromExtension(extension);
        if (mContentType == null && extension.equals("dcf")) {
            DrmManagerClient drmManager= new DrmManagerClient(mContext);
            mContentType = drmManager.getOriginalMimeType(mPath);
            Log.i(TAG, "ImageModel got drm content, mContentType: " + mContentType);
        }
        Log.i(TAG, "ImageModel got mContentType: " + mContentType);
        // It's ok if mContentType is null. Eventually we'll show a toast telling the
        // user the picture couldn't be attached.
    }

    private void initFromContentUri(Context context, Uri uri) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                            uri, null, null, null, null);

        if (c == null) {
            throw new IllegalArgumentException(
                    "Query on " + uri + " returns null result.");
        }

        try {
            if ((c.getCount() != 1) || !c.moveToFirst()) {
                throw new IllegalArgumentException(
                        "Query on " + uri + " returns 0 or multiple rows.");
            }

            String filePath;
            if (ImageModel.isMmsUri(uri)) {
                filePath = c.getString(c.getColumnIndexOrThrow(Part.FILENAME));
                if (TextUtils.isEmpty(filePath)) {
                    filePath = c.getString(
                            c.getColumnIndexOrThrow(Part._DATA));
                }
                mContentType = c.getString(
                        c.getColumnIndexOrThrow(Part.CONTENT_TYPE));
            } else {
                //ALPS00289861
                //filePath = uri.getPath();
                filePath = c.getString(
                    c.getColumnIndexOrThrow(Images.Media.DATA));
                mContentType = c.getString(
                        c.getColumnIndexOrThrow(Images.Media.MIME_TYPE));
            }
            mPath = filePath;
            // Aurora xuyong 2015-09-08 added for bug #15971 start
            if (null == mPath) {
            	mPath = Utils.getImageAbsolutePath(context, uri);
            }
            // Aurora xuyong 2015-09-08 added for bug #15971 end
            Log.i(TAG, "ImageModel got file path: " + mPath);
            Log.i(TAG, "ImageModel got mContentType: " + mContentType);
        } finally {
            c.close();
        }
    }

    private void decodeBoundsInfo() {
        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(mUri);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opt);
            mWidth = opt.outWidth;
            mHeight = opt.outHeight;
        } catch (FileNotFoundException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening stream", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }
    public String getSrc() {
        return mSrc;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    /**
     * Get a version of this image resized to fit the given dimension and byte-size limits. Note
     * that the content type of the resulting PduPart may not be the same as the content type of
     * this UriImage; always call {@link PduPart#getContentType()} to get the new content type.
     *
     * @param widthLimit The width limit, in pixels
     * @param heightLimit The height limit, in pixels
     * @param byteLimit The binary size limit, in bytes
     * @return A new PduPart containing the resized image data
     */
    public PduPart getResizedImageAsPart(int widthLimit, int heightLimit, int byteLimit) {
        PduPart part = new PduPart();

        byte[] data = getResizedImageData(widthLimit, heightLimit, byteLimit);
        if (data == null) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Resize image failed.");
            }
            return null;
        }

        part.setData(data);
        // getResizedImageData ALWAYS compresses to JPEG, regardless of the original content type
        //part.setContentType(ContentType.IMAGE_JPEG.getBytes());
        part.setContentType(mContentType.getBytes());
        //ALPS00289861
        if (!TextUtils.isEmpty(mSrc)) {
            part.setFilename(mSrc.getBytes());
        }
        return part;
    }

    private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;

    /**
     * Resize and recompress the image such that it fits the given limits. The resulting byte
     * array contains an image in JPEG format, regardless of the original image's content type.
     * @param widthLimit The width limit, in pixels
     * @param heightLimit The height limit, in pixels
     * @param byteLimit The binary size limit, in bytes
     * @return A resized/recompressed version of this image, in JPEG format
     */
    private byte[] getResizedImageData(int widthLimit, int heightLimit, int byteLimit) {
        int outWidth = mWidth;
        int outHeight = mHeight;

        int scaleFactor = 1;
        while ((outWidth / scaleFactor > widthLimit) || (outHeight / scaleFactor > heightLimit)) {
            scaleFactor *= 2;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "getResizedImageData: wlimit=" + widthLimit +
                    ", hlimit=" + heightLimit + ", sizeLimit=" + byteLimit +
                    ", mWidth=" + mWidth + ", mHeight=" + mHeight +
                    ", initialScaleFactor=" + scaleFactor);
        }

        InputStream input = null;
        InputStream inputForRotate = null;
        ByteArrayOutputStream os = null;
        try {
            int attempts = 1;

            do {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = scaleFactor;
                input = mContext.getContentResolver().openInputStream(mUri);
                // Don't know why need two copy of inputStream if we use ExifInterface, 
                // this is only for getting rotation degree
                inputForRotate = mContext.getContentResolver().openInputStream(mUri);
                // Aurora xuyong 2014-05-31 modified for bug #4868 start
                int orientation = 0;
                int degree = 0;
                String path = null;
                try {
                    if (inputForRotate != null) {
                        Cursor cursor = null;
                        try {
                             cursor = mContext.getContentResolver().query(mUri, null, null,
                                    null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                path = cursor.getString(1);
                            // Aurora xuyong 2014-06-14 modified for bug #4868 start
                            } else if (mUri.toString().startsWith("file")) {
                                   path = mUri.toString().replace("file://", "");
                            }
                            // Aurora xuyong 2014-06-14 modified for bug #4868 end
                            // Aurora xuyong 2014-06-04 deleted for multisim start
                            //cursor.close();
                            // Aurora xuyong 2014-06-04 deleted for multisim end
                        } catch (Exception e) {
                        } finally {
                          // Aurora xuyong 2014-06-04 modified for multisim start
                            if (cursor != null && !cursor.isClosed()) {
                                cursor.close();
                            }
                          // Aurora xuyong 2014-06-04 modified for multisim end
                        }
                       // if path is null here, you can't add the image to be a attachment
                       // Aurora xuyong 2014-06-04 modified for upper reasaon start
                       if (path != null) {
                                ExifInterface exif = new ExifInterface(path);
                                if (exif != null) {
                                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                degree = getExifRotation(orientation);
                            }
                        }
                        // Aurora xuyong 2014-06-04 modified for upper reasaon end
                    }   
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    if (inputForRotate != null) {
                        try {
                            inputForRotate.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                
                Log.i(TAG, "image rotation is" + degree + " degree");
                int quality = MessageUtils.IMAGE_COMPRESSION_QUALITY;
                try {
                    Bitmap b = BitmapFactory.decodeStream(input, null, options);
                    if (b == null) {
                        return null;
                    }
                    b = rotate(b, degree);
                    // Aurora xuyong 2014-05-31 modified for bug #4868 end
                    if (options.outWidth > widthLimit || options.outHeight > heightLimit) {
                        // The decoder does not support the inSampleSize option.
                        // Scale the bitmap using Bitmap library.
                        int scaledWidth = outWidth / scaleFactor;
                        int scaledHeight = outHeight / scaleFactor;

                        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                            Log.v(TAG, "getResizedImageData: retry scaling using " +
                                    "Bitmap.createScaledBitmap: w=" + scaledWidth +
                                    ", h=" + scaledHeight);
                        }

                        b = Bitmap.createScaledBitmap(b, outWidth / scaleFactor,
                                outHeight / scaleFactor, false);
                        if (b == null) {
                            return null;
                        }
                    }

                    // Compress the image into a JPG. Start with MessageUtils.IMAGE_COMPRESSION_QUALITY.
                    // In case that the image byte size is still too large reduce the quality in
                    // proportion to the desired byte size. Should the quality fall below
                    // MINIMUM_IMAGE_COMPRESSION_QUALITY skip a compression attempt and we will enter
                    // the next round with a smaller image to start with.
                    os = new ByteArrayOutputStream();
//                    if (!b.hasAlpha()) {
                    b.compress(CompressFormat.JPEG, quality, os);
                    mContentType = JPEGCONTENTTYPE;
                    // For CU Server can not support image/png. And CU server will change image/png to
                    // application/oct-stream. Lead to ALPS00246023
                    // } else {
//                        b.compress(CompressFormat.PNG, quality, os);
//                        mContentType = PNGCONTENTTYPE;
//                    }
                    int jpgFileSize = os.size();
                    if (jpgFileSize > byteLimit) {
                        int reducedQuality = (quality * byteLimit) / jpgFileSize;
                        if (reducedQuality >= MessageUtils.MINIMUM_IMAGE_COMPRESSION_QUALITY) {
                            quality = reducedQuality;

                            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                                Log.v(TAG, "getResizedImageData: compress(2) w/ quality=" + quality);
                            }

                            os = new ByteArrayOutputStream();
//                            if (!b.hasAlpha()) {
                            b.compress(CompressFormat.JPEG, quality, os);
                            mContentType = JPEGCONTENTTYPE;
//                            } else {
//                                b.compress(CompressFormat.PNG, quality, os);
//                                mContentType = PNGCONTENTTYPE;
//                            }
                        }
                    }
                    b.recycle();        // done with the bitmap, release the memory
                } catch (java.lang.OutOfMemoryError e) {
                    Log.w(TAG, "getResizedImageData - image too big (OutOfMemoryError), will try "
                            + " with smaller scale factor, cur scale factor: " + scaleFactor);
                    // fall through and keep trying with a smaller scale factor.
                }
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.v(TAG, "attempt=" + attempts
                            + " size=" + (os == null ? 0 : os.size())
                            + " width=" + outWidth / scaleFactor
                            + " height=" + outHeight / scaleFactor
                            + " scaleFactor=" + scaleFactor
                            + " quality=" + quality);
                }
                scaleFactor *= 2;
                attempts++;
            } while ((os == null || os.size() > byteLimit) && attempts < NUMBER_OF_RESIZE_ATTEMPTS);
            return os == null ? null : os.toByteArray();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * corresponding orientation of EXIF to degrees.
     */
    public static int getExifRotation(int orientation) {
        int degrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                degrees = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
        }
        return degrees;
    }
    
    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                b.setHasAlpha(true);
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }
}
