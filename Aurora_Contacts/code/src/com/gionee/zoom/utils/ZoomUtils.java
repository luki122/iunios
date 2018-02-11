package com.gionee.zoom.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
//Gionee fangbin 20120716 added for CR00636040 start
import java.io.ByteArrayOutputStream;
import android.os.StatFs;
import android.graphics.Bitmap.CompressFormat;
//Gionee fangbin 20120716 added for CR00636040 end
// Gionee fangbin 20120809 added for CR00671959 start
import gionee.os.storage.GnStorageManager;
// Gionee fangbin 20120809 added for CR00671959 end
//Gionee fangbin 20121030 added for CR00721643 start
import java.io.Closeable;
import java.io.FileNotFoundException;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;

import android.R.anim;
import android.content.ContentResolver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.MediaStore.Images.ImageColumns;
//Gionee fangbin 20121030 added for CR00721643 end

public class ZoomUtils {
    private static final String TAG = "ZoomUtils";
    public static final Uri IMAGES_MEDIA_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public static String[] PROJECTIONS = new String[]{Images.Media.DATA}; 
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    public static final String URI_SCHEMA_CONTENT = "content";
    public static final String URI_SCHEMA_FILE = "file";
    
    public static String getImagePathByUri(Context context, Uri imageUri) {
        String path = null;
        if (null != imageUri) {
            String schema = imageUri.getScheme();
            if (schema.equals(URI_SCHEMA_CONTENT)) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(imageUri, PROJECTIONS, null, null, null);
                    if (null != cursor && cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            path = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
                            Log.i(TAG, "path: " + path);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != cursor && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else if (schema.equals(URI_SCHEMA_FILE)) {
                path = imageUri.getPath();
            }
        }
        return path;
    }
    
    /* 获取缩略图
    * 
    * @return
    */
   public static Bitmap getThumbnailBitmap(String absPath, float aimWidth, float aimHeight) {
       // FileInputStream fis = null;
       //
       // try
       // {
       // fis = new FileInputStream(new File(absPath));
       // }
       // catch (FileNotFoundException e)
       // {
       // e.printStackTrace();
       //
       // return null;
       // }
       //
       // return getThumbnailBitmap(fis, aimWidth, aimHeight);

       BitmapFactory.Options options = new BitmapFactory.Options();
       options.inJustDecodeBounds = true; // 获取这个图片的宽和高

       BitmapFactory.decodeFile(absPath, options); // 此时返回 bm 为 空
       options.inJustDecodeBounds = false; // 计算缩放比

       int issW = (aimWidth > 0) ? (int) (options.outWidth / aimWidth) : 1;
       int issH = (aimHeight > 0) ? (int) (options.outHeight / aimHeight) : 1;

       int iss = Math.max(issW, issH);
       iss = Math.max(1, iss);

       options.inSampleSize = iss; // 重新读入图片，注意这次要把
       options.inJustDecodeBounds = false;

       return BitmapFactory.decodeFile(absPath, options);
   }
   
   public static Uri savePicasaImageToMediaProvider(Context context, Bitmap bitmap, File file) {
       ContentValues values = new ContentValues();
       if (null != bitmap) {
           long now = System.currentTimeMillis() / 1000;
           values.put(Images.Media.DISPLAY_NAME, file.getName());
           values.put(Images.Media.DATE_MODIFIED, now);
           values.put(Images.Media.DATE_ADDED, now);
           values.put(Images.Media.ORIENTATION, 0);
           values.put(Images.Media.DATA, file.getAbsolutePath());
           values.put(Images.Media.SIZE, file.length());
           values.put(WIDTH, bitmap.getWidth());
           values.put(HEIGHT, bitmap.getHeight());
       }
       return context.getContentResolver().insert(
               Images.Media.EXTERNAL_CONTENT_URI, values);
   }
   
   
   public static String getSDCardPath() {
       File sdCardDir = null;
       boolean sdcardExit = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
       if(sdcardExit){
           sdCardDir = Environment.getExternalStorageDirectory();
       }
       return sdCardDir.toString();
   }
   
   public static Bitmap rotate(Bitmap bitmap, float degrees) {
       int width = bitmap.getWidth();
       int height = bitmap.getHeight();

       Matrix matrix = new Matrix();
       matrix.postRotate(degrees);

       // Gionee fangbin 20120912 modified for CR00687307 start
       Bitmap ret = null;
       try {
           ret = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
       } catch (OutOfMemoryError e) {
           ret = null;
       } finally {
           // Gionee fangbin 20120921 removed for CR00699125 start
           //bitmap.recycle();
           // Gionee fangbin 20120921 removed for CR00699125 end
       }
       // Gionee fangbin 20120912 modified for CR00687307 end

       return ret;
   }
   
   public static int getRotationByUri(Context context, Uri uri) {
       int rotation = 0;
       try {
           InputStream is = context.getContentResolver().openInputStream(uri);
           rotation = getOrientation(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       return rotation;
   }
   
   
   private static int getOrientation(InputStream is) {
       if (is == null) {
           return 0;
       }

       byte[] buf = new byte[8];
       int length = 0;

       // ISO/IEC 10918-1:1993(E)
       while (read(is, buf, 2) && (buf[0] & 0xFF) == 0xFF) {
           int marker = buf[1] & 0xFF;

           // Check if the marker is a padding.
           if (marker == 0xFF) {
               continue;
           }

           // Check if the marker is SOI or TEM.
           if (marker == 0xD8 || marker == 0x01) {
               continue;
           }
           // Check if the marker is EOI or SOS.
           if (marker == 0xD9 || marker == 0xDA) {
               return 0;
           }

           // Get the length and check if it is reasonable.
           if (!read(is, buf, 2)) {
               return 0;
           }
           length = pack(buf, 0, 2, false);
           if (length < 2) {
               Log.e(TAG, "Invalid length");
               return 0;
           }
           length -= 2;

           // Break if the marker is EXIF in APP1.
           if (marker == 0xE1 && length >= 6) {
               if (!read(is, buf, 6)) return 0;
               length -= 6;
               if (pack(buf, 0, 4, false) == 0x45786966 &&
                   pack(buf, 4, 2, false) == 0) {
                   break;
               }
           }

           // Skip other markers.
           try {
               is.skip(length);
           } catch (IOException ex) {
               return 0;
           }
           length = 0;
       }

       // JEITA CP-3451 Exif Version 2.2
       if (length > 8) {
           int offset = 0;
           byte[] jpeg = new byte[length];
           if (!read(is, jpeg, length)) {
               return 0;
           }

           // Identify the byte order.
           int tag = pack(jpeg, offset, 4, false);
           if (tag != 0x49492A00 && tag != 0x4D4D002A) {
               Log.e(TAG, "Invalid byte order");
               return 0;
           }
           boolean littleEndian = (tag == 0x49492A00);

           // Get the offset and check if it is reasonable.
           int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
           if (count < 10 || count > length) {
               Log.e(TAG, "Invalid offset");
               return 0;
           }
           offset += count;
           length -= count;

           // Get the count and go through all the elements.
           count = pack(jpeg, offset - 2, 2, littleEndian);
           while (count-- > 0 && length >= 12) {
               // Get the tag and check if it is orientation.
               tag = pack(jpeg, offset, 2, littleEndian);
               if (tag == 0x0112) {
                   // We do not really care about type and count, do we?
                   int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                   switch (orientation) {
                       case 1:
                           return 0;
                       case 3:
                           return 180;
                       case 6:
                           return 90;
                       case 8:
                           return 270;
                   }
                   Log.i(TAG, "Unsupported orientation");
                   return 0;
               }
               offset += 12;
               length -= 12;
           }
       }

       Log.i(TAG, "Orientation not found");
       return 0;
   }

   private static int pack(byte[] bytes, int offset, int length,
           boolean littleEndian) {
       int step = 1;
       if (littleEndian) {
           offset += length - 1;
           step = -1;
       }

       int value = 0;
       while (length-- > 0) {
           value = (value << 8) | (bytes[offset] & 0xFF);
           offset += step;
       }
       return value;
   }

   private static boolean read(InputStream is, byte[] buf, int length) {
       try {
           return is.read(buf, 0, length) == length;
       } catch (IOException ex) {
           return false;
       }
   }
   
   // Gionee fangbin 20120716 added for CR00636040 start
   // Gionee fangbin 20120809 modified for CR00671959 start
   public static final long DEFAULT_REDUNDANCY_VALUE = 499 * 499;
   public static boolean isAvaiableSpace(Bitmap bitmap, CompressFormat extension, Context context){
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       long size = 0;
       try {
           bitmap.compress(extension, 100, baos);
           size = baos.toByteArray().length;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
       }
       return getPrefixPath(context, size)!=null;
       /*
       if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
          String sdcard = Environment.getExternalStorageDirectory().getPath();
          StatFs statFs = new StatFs(sdcard);
          long blockSize = statFs.getBlockSize();
          long blocks = statFs.getAvailableBlocks();
          long availableSpare = blocks*blockSize;
          if(size + DEFAULT_REDUNDANCY_VALUE > availableSpare){
              return false;
          }else{
              return true;
          }
       }
       return false;
       */
   }
   // Gionee fangbin 20120809 modified for CR00671959 end
   // Gionee fangbin 20120716 added for CR00636040 end
   
   // Gionee fangbin 20120809 added for CR00671959 start
   public static String getPrefixPath(Context context, long bytes) {
//       boolean cardAvaiable = GnStorageManager.getVolumeState("/mnt/sdcard").equals(Environment.MEDIA_MOUNTED) || 
//                               GnStorageManager.getVolumeState("/mnt/sdcard2").equals(Environment.MEDIA_MOUNTED);
	   String aString = GnStorageManager.getInstance(ContactsApplication.getInstance()).getSdCardPath(0);
	   String bString = GnStorageManager.getInstance(ContactsApplication.getInstance()).getSdCardPath(1);
       boolean cardAvaiable = GnStorageManager.getVolumeState(aString).equals(Environment.MEDIA_MOUNTED) || 
                                 GnStorageManager.getVolumeState(bString).equals(Environment.MEDIA_MOUNTED);
       if (cardAvaiable) {
    	   String pathString = GnStorageManager.getInstance(ContactsApplication.getInstance()).getGnAvailableExternalStoragePath_ex(bytes + DEFAULT_REDUNDANCY_VALUE);
    	   return pathString;
//           return Environment.getExternalStorageDirectory().getPath();
       }
       return null;
   }
   // Gionee fangbin 20120809 added for CR00671959 end
   
   // Gionee fangbin 20121030 added for CR00721643 start
   private static Bitmap decodeBitmap(Uri uri, int width, int height, Context context) {
       InputStream is = null;
       Bitmap bitmap = null;
       
       Rect bounds = null;//to log more details
       int sampleSize = -1;//to log more details
       try {
           // TODO: Take max pixels allowed into account for calculation to avoid possible OOM.
           bounds = getBitmapBounds(uri, context);
           sampleSize = Math.max(bounds.width() / width, bounds.height() / height);
           sampleSize = Math.min(sampleSize,
                   Math.max(bounds.width() / height, bounds.height() / width));

           BitmapFactory.Options options = new BitmapFactory.Options();
           options.inSampleSize = Math.max(sampleSize, 1);
           options.inPreferredConfig = Bitmap.Config.ARGB_8888;

           is = context.getContentResolver().openInputStream(uri);
           bitmap = BitmapFactory.decodeStream(is, null, options);
       } catch (FileNotFoundException e) {
           Log.e(TAG, "FileNotFoundException: " + uri);
       } catch (OutOfMemoryError e) {
           Log.e(TAG, "decodeBitmap() OutOfMemoryError: ", e);
       } finally {
           closeStream(is);
       }
       // Ensure bitmap in 8888 format, good for editing as well as GL compatible.
       if ((bitmap != null) && (bitmap.getConfig() != Bitmap.Config.ARGB_8888)) {
           Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
           bitmap.recycle();
           bitmap = copy;
       }
       if (bitmap != null) {
           // Scale down the sampled bitmap if it's still larger than the desired dimension.
           float scale = Math.min((float) width / bitmap.getWidth(),
                   (float) height / bitmap.getHeight());
           scale = Math.max(scale, Math.min((float) height / bitmap.getWidth(),
                   (float) width / bitmap.getHeight()));
           if (scale < 1) {
               Matrix m = new Matrix();
               m.setScale(scale, scale);
               Bitmap transformed = createBitmap(
                       bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m);
               bitmap.recycle();
               return transformed;
           }
       }
       return bitmap;
   }
   
   private static Rect getBitmapBounds(Uri uri, Context context) {
       Rect bounds = new Rect();
       InputStream is = null;

       try {
           is = context.getContentResolver().openInputStream(uri);
           BitmapFactory.Options options = new BitmapFactory.Options();
           options.inJustDecodeBounds = true;
           BitmapFactory.decodeStream(is, null, options);

           bounds.right = options.outWidth;
           bounds.bottom = options.outHeight;
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } finally {
           closeStream(is);
       }

       return bounds;
   }
   
   private static void closeStream(Closeable stream) {
       if (stream != null) {
           try {
               stream.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
   
   private static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m) {
       // Re-implement Bitmap createBitmap() to always return a mutable bitmap.
       Canvas canvas = new Canvas();

       Bitmap bitmap;
       Paint paint;
       if ((m == null) || m.isIdentity()) {
           bitmap = Bitmap.createBitmap(width, height, source.getConfig());
           paint = null;
       } else {
           RectF rect = new RectF(0, 0, width, height);
           m.mapRect(rect);
           bitmap = Bitmap.createBitmap(
                   Math.round(rect.width()), Math.round(rect.height()), source.getConfig());

           canvas.translate(-rect.left, -rect.top);
           canvas.concat(m);

           paint = new Paint(Paint.FILTER_BITMAP_FLAG);
           if (!m.rectStaysRect()) {
               paint.setAntiAlias(true);
           }
       }
       bitmap.setDensity(source.getDensity());
       canvas.setBitmap(bitmap);

       Rect srcBounds = new Rect(x, y, x + width, y + height);
       RectF dstBounds = new RectF(0, 0, width, height);
       canvas.drawBitmap(source, srcBounds, dstBounds, paint);
       return bitmap;
   }
   
   public static Bitmap getBitmap(Uri uri, int width, int height, Context context) {
       Bitmap bitmap = decodeBitmap(uri, width, height, context);

       // Rotate the decoded bitmap according to its orientation if it's necessary.
       if (bitmap != null) {
           int orientation = 0;
           Cursor cursor = null;
           try {
               cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.ORIENTATION}, null, null, null);
               if ((cursor != null) && cursor.moveToNext()) {
                   orientation = cursor.getInt(0);
               // Gionee fangbin 20121101 added for CR00721643 start
               } else {
                   if (uri != null) {
                       try {
                           InputStream is = context.getContentResolver().openInputStream(uri);
                           orientation = getOrientation(is);
                           is.close();
                       } catch (Exception e1) {
                           e1.printStackTrace();
                       }
                   } 
               }
               // Gionee fangbin 20121101 added for CR00721643 end
           } catch (Exception e) {
               // Gionee fangbin 20121101 modified for CR00721643 start
               if (uri != null) {
               // Gionee fangbin 20121101 modified for CR00721643 end
                   try {
                       InputStream is = context.getContentResolver().openInputStream(uri);
                       orientation = getOrientation(is);
                       is.close();
                   } catch (Exception e1) {
                       e1.printStackTrace();
                   }
               } else {
                   // Ignore error for no orientation column; just use the default orientation value 0.
               }
           } finally {
               if (cursor != null) {
                   cursor.close();
               }
           }
           if (orientation != 0) {
               Matrix m = new Matrix();
               m.setRotate(orientation);
               Bitmap transformed = createBitmap(
                       bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m);
               bitmap.recycle();
               return transformed;
           }
       }
       return bitmap;
   }
   // Gionee fangbin 20121030 added for CR00721643 end
}
