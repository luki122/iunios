
package com.android.gallery3d.fragmentutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import java.io.FileDescriptor;
import com.android.gallery3d.fragmentdata.GalleryItem;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageResizer extends ImageWorker {
    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;
    
    public static int cache_ImageWidth;//lory add
    public static int cache_ImageHeight;//lory add
    
    private Context mContext;

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        mContext = context;
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize) {
        super(context);
        mContext = context;
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     *
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
        
        cache_ImageWidth = width;
        cache_ImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     *
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
        if (MySelfBuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + resId);
        }
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
                mImageHeight, getImageCache());
    }
    
    private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) { 
    	Bitmap bitmap = null;  
    	// 获取视频的缩略图  
    	bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
    	bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
    	
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	if (MyUtils.hasHoneycomb()) {
            addInBitmapOptions(options, getImageCache());
        }
    	return bitmap;  
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        //return processBitmap(Integer.parseInt(String.valueOf(data)));
    	//return decodeSampledBitmapFromFile(String.valueOf(data), mImageWidth, mImageHeight, getImageCache());
    	
    	//long time2 = System.currentTimeMillis();
    	
    	if (true) {//very slow 
    		/*//long time1 = System.currentTimeMillis();
    		Bitmap tmp1 =  decodeSampledBitmapFromFile(String.valueOf(data), mImageWidth, mImageHeight, getImageCache());
    		//Log.i(TAG, "zll --- 1 processBitmap time:"+ (System.currentTimeMillis()-time1));
    		//long time2 = System.currentTimeMillis();
    		if (tmp1 == null) {
    			tmp1 = getVideoThumbnail(String.valueOf(data), mImageWidth, mImageHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			}
    		
    		Bitmap tmpBitmap = null;
			String filepath = String.valueOf(data);
			int degree = 0;
			try {
				ExifInterface exifInterface = new ExifInterface(filepath);
				int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
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
									
				default:
					degree = 0;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Log.i(TAG, "zll --- 2 processBitmap degree:"+degree);
			if (tmp1 != null && degree != 0) {
				Matrix matrix = new Matrix();
				matrix.reset();
				matrix.setRotate(degree);
				
				tmpBitmap = Bitmap.createBitmap(tmp1,0,0, tmp1.getWidth(), tmp1.getHeight(), matrix, true);
				tmp1.recycle();
			} else {
				return tmp1;
			}
		
			//Log.i(TAG, "zll --- processBitmap time:"+ (System.currentTimeMillis()-time2));
    		return tmpBitmap;*/
    		
    		GalleryItem items = (GalleryItem)data;
    		Bitmap tmp = null;
    		
    		//Log.i(TAG, "zll --- 2 processBitmap xxxxxx");
    		if (items.getType() == 2) {
    			BitmapFactory.Options options = new BitmapFactory.Options();
    			options.inDither = false;
    			options.inPreferredConfig = Bitmap.Config.RGB_565;
    			
				return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), Integer.parseInt(String.valueOf(items.getUri())), Thumbnails.MINI_KIND, options);
			} 
    		
    		//Log.i(TAG, "zll --- processBitmap getFilePath():"+ items.getFilePath());
    		tmp =  decodeSampledBitmapFromFile(String.valueOf(items.getFilePath()), mImageWidth, mImageHeight, getImageCache());
    		if (tmp == null) {
    			return null;
			}
    		
    		int degree = 0;
			switch (items.getRotation()) {
			case 90:
				degree = 90;
				break;
				
			case 180:
				degree = 180;
				break;
				
			case 270:
				degree = 270;
				break;
								
			default:
				degree = 0;
				break;
			}
			
			Bitmap tmpBitmap = null;
			if (tmp != null && degree != 0) {
				Matrix matrix = new Matrix();
				matrix.reset();
				matrix.setRotate(degree);
				
				tmpBitmap = Bitmap.createBitmap(tmp,0,0, tmp.getWidth(), tmp.getHeight(), matrix, true);
				tmp.recycle();
				return tmpBitmap;
			}
			
			return tmp;
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			
			GalleryItem items = (GalleryItem)data;
			int id = Integer.parseInt(String.valueOf(items.getUri()));
			//<!-- Iuni <lory><2013-12-11> modify start-->
			Bitmap tmp = null;
			
			if (items.getType() == 2) {
				return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), id, Thumbnails.MINI_KIND, options);
			} 
			
			tmp =  MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), id, Thumbnails.MINI_KIND, options);
			if (tmp == null) {
				return tmp;
			}
			
			//Log.i(TAG, "zll --- items.getRotation():"+items.getRotation());
			int degree = 0;
			switch (items.getRotation()) {
			case 90:
				degree = 90;
				break;
				
			case 180:
				degree = 180;
				break;
				
			case 270:
				degree = 270;
				break;
								
			default:
				degree = 0;
				break;
			}
			
			Bitmap tmpBitmap = null;
			if (tmp != null && degree != 0) {
				Matrix matrix = new Matrix();
				matrix.reset();
				matrix.setRotate(degree);
				
				tmpBitmap = Bitmap.createBitmap(tmp,0,0, tmp.getWidth(), tmp.getHeight(), matrix, true);
				tmp.recycle();
				return tmpBitmap;
			}
			
			return tmp;
		}
    	
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (MyUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (MyUtils.hasHoneycomb()) {
            //addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, cache_ImageWidth, cache_ImageHeight);//使图片变成原来的几分之几：譬如：inSampleSize=2,则宽和高均为1/2,大小为1/4

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;//true:只解码图片的Bounds，即图片长宽等信息
        options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (MyUtils.hasHoneycomb()) {
            //addInBitmapOptions(options, cache);
        }

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                if (MySelfBuildConfig.DEBUG) {
                    Log.d(TAG, "Found bitmap to use for inBitmap");
                }
                options.inBitmap = inBitmap;
            }
        }
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
}
