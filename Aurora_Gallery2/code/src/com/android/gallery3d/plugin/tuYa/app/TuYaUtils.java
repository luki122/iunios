package com.android.gallery3d.plugin.tuYa.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import com.android.gallery3d.common.ApiHelper;
//import com.android.gallery3d.common.GalleryUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Exif;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "seems no problem")
public class TuYaUtils {
    private static final String TAG = "Utils";
    private static final int DEFAULT_COMPRESS_QUALITY = 95;
    private static final int ORI_NORMAL = ExifInterface.ORIENTATION_NORMAL;
    private static final int ORI_ROTATE_90 = ExifInterface.ORIENTATION_ROTATE_90;
    private static final int ORI_ROTATE_180 = ExifInterface.ORIENTATION_ROTATE_180;
    private static final int ORI_ROTATE_270 = ExifInterface.ORIENTATION_ROTATE_270;
    private static final int ORI_FLIP_HOR = ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
    private static final int ORI_FLIP_VERT = ExifInterface.ORIENTATION_FLIP_VERTICAL;
    private static final int ORI_TRANSPOSE = ExifInterface.ORIENTATION_TRANSPOSE;
    private static final int ORI_TRANSVERSE = ExifInterface.ORIENTATION_TRANSVERSE;
    private static final String[] COPY_EXIF_ATTRIBUTES = new String[]{ExifInterface.TAG_APERTURE,
            ExifInterface.TAG_DATETIME, ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH, ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF, ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_GPS_DATESTAMP, ExifInterface.TAG_ISO,
            ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_WHITE_BALANCE,};
    public static final int MOSAIC_MIN_GRID_SIZE = 10;
    public static final int MOSAIC_BLUR_DEFAULT_VALUE = 15;

    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }

    public static Bitmap loadBitmap(Context context, Uri uri, int targetW, int targetH) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                return null;
            }
			/*
            int[] size = Utils.loadBitmapSize(context, uri); TYM
            if (size == null) {
                return null;
            }
            */
            int originalW = targetW;//size[0];
            int originalH = targetH;//size[1];
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = getSampleSize(targetW, targetH, originalW, originalH);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            if (bitmap != null) {
                int resultWidth = bitmap.getWidth();
                int resultHeight = bitmap.getHeight();
                if (options.inSampleSize > 1 && resultWidth == originalW && resultHeight == originalH) {
                    bitmap = resizeBitmapByScale(bitmap, (float) 1 / options.inSampleSize, true);
                }
            }
            return bitmap;

        } catch (FileNotFoundException e) {
            Log.w(TAG, "error", e);
        } finally {
            Utils.closeSilently(is);
        }

        return null;
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight())
            return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle)
            bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    private static int getSampleSize(int targetW, int targetH, int originalW, int originalH) {
        int sampleSize = Math.max(originalW / targetW, originalH / targetH);
        sampleSize = Math.min(sampleSize, Math.max(originalW / targetH, originalH / targetW));
        sampleSize = Math.max(sampleSize, 1);
        if (sampleSize > 1 && sampleSize % 2 != 0) {
            sampleSize += 1;
        }
        return sampleSize;
    }

    public static int getRotation(Context context, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return getOrientationFromPath(uri.getPath());
        }

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    new String[]{ImageColumns.ORIENTATION}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                int ori = cursor.getInt(0);

                switch (ori) {
                    case 0:
                        return ORI_NORMAL;
                    case 90:
                        return ORI_ROTATE_90;
                    case 270:
                        return ORI_ROTATE_270;
                    case 180:
                        return ORI_ROTATE_180;
                    default:
                        return -1;
                }
            } else {
                return -1;
            }
        } catch (SQLiteException e) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        } catch (IllegalArgumentException e) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        } catch (Exception e) {
            int orientation = 0;
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                // / M:
                InputStream is = null;
                try {
                    is = context.getContentResolver().openInputStream(uri);
                    orientation = Exif.getOrientation(is);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } finally {
                    Utils.closeSilently(is);
                }
            } else {
                // Ignore error for no orientation column; just use the default orientation value 0.
            }
            return orientation;
        } finally {
            Utils.closeSilently(cursor);
        }
    }

    public static Bitmap rotateToPortrait(Bitmap bitmap, int ori) {
        Matrix matrix = new Matrix();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (ori == ORI_ROTATE_90 || ori == ORI_ROTATE_270 || ori == ORI_TRANSPOSE || ori == ORI_TRANSVERSE) {
            int tmp = w;
            w = h;
            h = tmp;
        }
        switch (ori) {
            case ORI_ROTATE_90:
                matrix.setRotate(90, w / 2f, h / 2f);
                break;
            case ORI_ROTATE_180:
                matrix.setRotate(180, w / 2f, h / 2f);
                break;
            case ORI_ROTATE_270:
                matrix.setRotate(270, w / 2f, h / 2f);
                break;
            case ORI_FLIP_HOR:
                matrix.preScale(-1, 1);
                break;
            case ORI_FLIP_VERT:
                matrix.preScale(1, -1);
                break;
            case ORI_TRANSPOSE:
                matrix.setRotate(90, w / 2f, h / 2f);
                matrix.preScale(1, -1);
                break;
            case ORI_TRANSVERSE:
                matrix.setRotate(270, w / 2f, h / 2f);
                matrix.preScale(1, -1);
                break;
            case ORI_NORMAL:
            default:
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int getOrientationFromPath(String path) {
        int orientation = -1;
        try {
            ExifInterface EXIF = new ExifInterface(path);
            orientation = EXIF.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    public static List<String> getLocalRootPath(Context context) {
        try {
            StorageManager storageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
            String[] volumePaths = (String[]) method.invoke(storageManager);
            List<String> storagePaths = new ArrayList<String>();
            for (String path : volumePaths) {
                storagePaths.add(path);
            }
            return storagePaths;
        } catch (Exception ex) {
            Log.e(TAG, "error", ex);
        }

        return null;
    }

    public static String convertToFriendlyPath(Context context, String absolutePath) {
        try {

            StorageManager storageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            if (storageManager == null) {
                return absolutePath;
            }
            Class<?> StorageVolume = Class.forName("android.os.storage.StorageVolume");
            Method method = StorageManager.class.getDeclaredMethod("getVolumeList");
            Object[] storageVolume = (Object[]) method.invoke(storageManager);
            if (storageVolume == null) {
                return absolutePath;
            }
            int length = storageVolume.length;
            for (int i = 0; i < length; i++) {
                Method getPathM = StorageVolume.getDeclaredMethod("getPath");
                String rootPath = (String) getPathM.invoke(storageVolume[i]);
                if (absolutePath.startsWith(rootPath)) {
                    Method getDescriptionM = StorageVolume.getDeclaredMethod("getDescription", Context.class);
                    String newRootSt = (String) getDescriptionM.invoke(storageVolume[i], context);
                    absolutePath = newRootSt
                            + absolutePath.substring(rootPath.length(), absolutePath.length());
                    break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
        return absolutePath;
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, File destination, List<String> rootPaths) {
        if (rootPaths != null) {
            String curFileSD = null;
            for (String root : rootPaths) {
                if (destination.getAbsolutePath().startsWith(root)) {
                    curFileSD = root;
                    break;
                }
            }
			/* TYM
            long spaceSize = GalleryUtils.getAvailableBytes(curFileSD);
            long curBitmapSize = bitmap.getWidth() * bitmap.getHeight() * 4;
            if (spaceSize <= curBitmapSize) {
                return false;
            }
            */
        }
        OutputStream os = null;
        boolean result;
        try {
            os = new FileOutputStream(destination);
            result = bitmap.compress(CompressFormat.JPEG, DEFAULT_COMPRESS_QUALITY, os);
        } catch (FileNotFoundException e) {
            Log.v(TAG, "Error in writing " + destination.getAbsolutePath());
            result = false;
        } finally {
            Utils.closeSilently(os);
        }
        return result;
    }

    public static void copyExif(Uri sourceUri, String destPath, Context context) {
        if (ContentResolver.SCHEME_FILE.equals(sourceUri.getScheme())) {
            copyExif(sourceUri.getPath(), destPath);
            return;
        }

        final String[] PROJECTION = new String[]{ImageColumns.DATA};
        Cursor c = null;
        try {
            c = context.getContentResolver().query(sourceUri, PROJECTION, null, null, null);
            if (c.moveToFirst()) {
                String path = c.getString(0);
                if (new File(path).exists()) {
                    copyExif(path, destPath);
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "Failed to copy exif", e);
        } finally {
            Utils.closeSilently(c);
        }
    }

    private static void copyExif(String sourcePath, String destPath) {
        try {
            ExifInterface source = new ExifInterface(sourcePath);
            ExifInterface dest = new ExifInterface(destPath);
            boolean needsSave = false;
            for (String tag : COPY_EXIF_ATTRIBUTES) {
                String value = source.getAttribute(tag);
                if (value != null) {
                    needsSave = true;
                    dest.setAttribute(tag, value);
                }
            }
            if (needsSave) {
                dest.saveAttributes();
            }
        } catch (IOException ex) {
            Log.w(TAG, "Failed to copy exif metadata", ex);
        }
    }

    /**
     * Insert the content (saved file) with proper source photo properties.
     */
    public static Uri insertContent(Context context, Uri sourceUri, File file, String saveFileName) {
        long now = System.currentTimeMillis() / 1000;

        final ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, saveFileName);
        values.put(Images.Media.DISPLAY_NAME, file.getName());
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATE_TAKEN, now * 1000);
        values.put(Images.Media.DATE_MODIFIED, now);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, file.getAbsolutePath());
        values.put(Images.Media.SIZE, file.length());
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int imageLength = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            int imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
                values.put(Images.Media.WIDTH, imageWidth);
                values.put(Images.Media.HEIGHT, imageLength);
            }
        } catch (IOException ex) {
            Log.w(TAG, "ExifInterface throws IOException", ex);
        }
        final String[] projection = new String[]{ImageColumns.DATE_TAKEN, ImageColumns.LATITUDE,
                ImageColumns.LONGITUDE,};
        querySource(context, sourceUri, projection, new ContentResolverQueryCallback() {

            @Override
            public void onCursorResult(Cursor cursor) {
                values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));
                double latitude = cursor.getDouble(1);
                double longitude = cursor.getDouble(2);
                if ((latitude != 0f) || (longitude != 0f)) {
                    values.put(Images.Media.LATITUDE, latitude);
                    values.put(Images.Media.LONGITUDE, longitude);
                }
            }
        });

        return context.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static void querySource(Context context, Uri sourceUri, String[] projection,
                                   ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(sourceUri, projection, null, null, null);
            if ((cursor != null) && cursor.moveToNext()) {
                callback.onCursorResult(cursor);
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static File getSaveDirectory(Context context, Uri sourceUri) {
        final File[] dir = new File[1];
        querySource(context, sourceUri, new String[]{ImageColumns.DATA}, new ContentResolverQueryCallback() {

            @Override
            public void onCursorResult(Cursor cursor) {
                dir[0] = new File(cursor.getString(0)).getParentFile();
            }
        });
        return dir[0];
    }

    public static Bitmap loadMutableBitmap(BitmapFactory.Options options, Context context, Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fd = null;
        Bitmap bitmap = null;
        if (pfd != null) {
            fd = pfd.getFileDescriptor();
        }
        try {
            if (fd != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            }
        } catch (OutOfMemoryError e) {
            final int maxTryNum = 8;
            for (int i = 0; i < maxTryNum; i++) {
                options.inSampleSize *= 2;
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                } catch (OutOfMemoryError e1) {
                    Log.w(TAG, "  saveBitmap :out of memory when decoding:" + e1);
                    bitmap = null;
                }
                if (bitmap != null)
                    break;
            }
        } finally {
            Utils.closeSilently(pfd);
        }

        if (bitmap != null) {
            int orientation = getRotation(context, uri);
            if (orientation > 1) {
                Bitmap rotatedBitmap = rotateToPortrait(bitmap, orientation);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = rotatedBitmap;
                }
            }

        }
        if (bitmap != null) {
            if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                return bitmap;
            } else {
                return bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }
        return null;
    }

    public static File createOtherSdCardFile(List<String> rootPaths, String absolutePath) {
		/* TYM
        File file = Utils.createOtherSdCardFile(rootPaths, absolutePath);
        if (file != null) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            return file;
        }
        */
        return null;
    }

    public static void drawMosaic(Canvas canvas, Bitmap bitmap, int w, int h, int gridSize) {
        Paint paint = new Paint(Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        int horCount = (int) Math.ceil(w / (float) gridSize);
        int verCount = (int) Math.ceil(h / (float) gridSize);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = gridSize * horIndex;
                int t = gridSize * verIndex;
                int r = l + gridSize;
                if (r > w) {
                    r = w;
                }
                int b = t + gridSize;
                if (b > h) {
                    b = h;
                }
                int color = bitmap.getPixel(l, t);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
    }

    public static Bitmap transformBackground(Bitmap bitmap) {
        Bitmap tuYaBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tuYaBitmap);
        Paint paint = new Paint(Paint.DITHER_FLAG);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        bitmap.recycle();
        return tuYaBitmap;
    }
}
