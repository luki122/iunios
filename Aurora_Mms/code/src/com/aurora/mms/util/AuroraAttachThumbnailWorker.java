package com.aurora.mms.util;
// Aurora xuyong 2015-10-08 created for aurora's new feature
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.UriImage;
import com.android.mms.ui.VideoAttachmentView;
// Aurora xuyong 2015-10-15 added for aurora's new feature start
import com.aurora.addimage.utils.AuroraAddImageUtil;
// Aurora xuyong 2015-10-15 added for aurora's new feature start
import com.aurora.mms.ui.AuroraRoundDrawable;
import com.aurora.mms.ui.AuroraRoundImageView;
import com.aurora.mms.ui.ThumbnailWorker;
import com.aurora.mms.ui.ThumbnailWorker.WorkDetails;

import com.android.mms.R;

public class AuroraAttachThumbnailWorker extends ThumbnailWorker {
	
	public static final String TAG = "AuroraAttachThumbnailWorker";
	
    private Context mContext;
    protected Resources mResources;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private static final int MAX_TASK_COUNTS = 10;
    private Queue<String> mTaskList = new LinkedList<String>();
    private Map<String, String> mTaskMap = new HashMap<String, String>();
    private static boolean sNeedListCache = true;
    // Aurora xuyong 2015-10-15 added for aurora's new feature start
    private int mLoadMode = AuroraAddImageUtil.DATABASE_MODE;
    // Aurora xuyong 2015-10-15 added for aurora's new feature end
    public AuroraAttachThumbnailWorker(Context context) {
    	super(context);
        mContext = context;
        mResources = context.getResources();
    }
    
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void clearTaskSet() {
        if (mTaskList != null && mTaskList.size() > 0) {
            mTaskList.clear();
        }
        if (mTaskMap != null && mTaskMap.size() > 0) {
            mTaskMap.clear();
        }
    }

    public static void setNeedCache(boolean needCache) {
        sNeedListCache = needCache;
    }

    private static class AsyncDrawable extends AuroraRoundDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    
    private class BitmapWorkerTask extends AsyncTask<Object, Void, WorkDetails> {        

        private Object data;
        private int mDuration;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected WorkDetails doInBackground(Object... params) {

            data = params[0];
            final String dataString = String.valueOf(data);
            BitmapDrawable drawable = null;
            WorkDetails dad = null;
            // Aurora xuyong 2015-10-15 added for aurora's new feature start
            if (mLoadMode == AuroraAddImageUtil.FILE_MODE) {
            	Log.e(TAG, "get from file!");
                data = AuroraAddImageUtil.getPersistPath(mContext, (String)data);
            } else if (mLoadMode == AuroraAddImageUtil.DATABASE_MODE) {
            	if (AuroraAddImageUtil.peristFileExist(mContext, (String)data, mLoadMode)) {
            		Log.e(TAG, "get from database!");
	            	Bitmap bitmap = AuroraAddImageUtil.getBitmapByPath(mContext, (String)data);
	            	if (null != bitmap) {
	            		dad = new WorkDetails();
		            	dad.setPath((String)data);
		                dad.setBitmap(bitmap);
		                drawable = new AuroraRoundDrawable(bitmap, AuroraRoundImageView.DEFAULT_RADIUS, 0, ColorStateList.valueOf(AuroraRoundDrawable.DEFAULT_BORDER_COLOR), false);
		                dad.setDrawble(drawable);
		                return dad;
            	    }
            	}
            }
            Log.e(TAG, "get from -origin!");
            // Aurora xuyong 2015-10-15 added for aurora's new feature end
            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait(100);
                    } catch (InterruptedException e) {}
                }
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (dad == null && !isCancelled() && getAttachedImageView() != null) {
                dad = processBitmap(params[0]);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (dad != null) {
                // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                // which will recycle automagically
                drawable = new AuroraRoundDrawable(dad.getBitmap(), AuroraRoundImageView.DEFAULT_RADIUS, 0, ColorStateList.valueOf(AuroraRoundDrawable.DEFAULT_BORDER_COLOR), false);
                dad.setDrawble(drawable);
            }

            return dad;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(WorkDetails value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled()) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            // Aurora xuyong 2015-10-15 added for aurora's new feature start
                // Aurora xuyong 2015-10-20 modified for bug #16834 start
                if (value != null) {
        	        Bitmap bp = value.getBitmap();
        	        if (!AuroraAddImageUtil.peristFileExist(mContext, value.getPath(), mLoadMode)) {
        		        AuroraAddImageUtil.persist(mContext, bp, value.getPath(), mLoadMode);
               	    }
                }
                // Aurora xuyong 2015-10-20 modified for bug #16834 end
            // Aurora xuyong 2015-10-15 added for aurora's new feature end
            if (value != null && imageView != null) {
                imageView.setImageDrawable(value.getDrawable());
                if (mTaskList.size() >= MAX_TASK_COUNTS) {
                    mTaskMap.remove(mTaskList.remove());
                }
                if (sNeedListCache) {
                    // Aurora xuyong 2015-10-15 modified for aurora's new feature start
                    mTaskList.add(value.getPath());
                    mTaskMap.put(value.getPath(), value.getDrawable().toString());
                    // Aurora xuyong 2015-10-15 modified for aurora's new feature end
                }
            }
        }

        @Override
        protected void onCancelled(WorkDetails value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }
    
    public void loadImage(Object data, ImageView imageView) {
        // Aurora xuyong 2015-10-13 modified for aurora's new feature start
        Bitmap loadingBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.aurora_attach_default_bg);
        // Aurora xuyong 2015-10-13 modified for aurora's new feature end
        if (data == null) {
            return;
        }
        if (cancelPotentialWork(data, imageView) && (imageView.getDrawable() == null || mTaskMap.get(data.toString()) == null
                || !mTaskMap.get(data.toString()).equals(imageView.getDrawable().toString()))) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, loadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
        }
    }
    
    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }
    
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    private final static int THUMBNAIL_BOUNDS_HEIGHT_LIMIT = 336;
    private final static int THUMBNAIL_BOUNDS_WIDTH_LIMIT = 276;
    private Bitmap decodeDefaultResource(int thumbnailBoundsHeightLimit, int thumbnailBoundsWidthLimit) {
        BitmapFactory.Options opt = new BitmapFactory.Options();  
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mResources, R.drawable.aurora_attach_default_bg, opt);
        int outWidth = opt.outWidth;
        int outHeight = opt.outHeight;
        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565; 
        opt.inSampleSize = 1;
        while ((outWidth / opt.inSampleSize > thumbnailBoundsWidthLimit)
                || (outHeight / opt.inSampleSize > thumbnailBoundsHeightLimit)) {
            opt.inSampleSize *= 2;
        }
        opt.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.aurora_attach_default_bg, opt);
        return bitmap;
    }

    private WorkDetails processBitmap(Object data) {
        WorkDetails dad = new WorkDetails();
        Bitmap bitmap = null;
        if (null == data) {
            bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT);
        }
        bitmap = internalGetBitmap((String)data);
        if (bitmap == null) {
            bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT);
        }
        dad.setPath((String)data);
        dad.setBitmap(bitmap);
        return dad;
    }
    
    public static class WorkDetails {
        Bitmap mBitmap;
        Drawable mDrawable;
        String mPath;
        
        public WorkDetails () {
        }
        
        public WorkDetails (Bitmap bitmap, Drawable drawable, String duration, String path) {
            mBitmap = bitmap;
            mDrawable = drawable;
            mPath = path;
        }
        
        public Bitmap getBitmap() {
            return mBitmap;
        }
        
        public Drawable getDrawable() {
            return mDrawable;
        }
        
        public String getPath() {
            return mPath;
        }
        
        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }
        
        public void setDrawble(Drawable drawable) {
            mDrawable = drawable;
        }
        
        public void setPath(String path) {
        	mPath = path;
        }
        
    }

     private Bitmap internalGetBitmap(String path) {
         Bitmap bitmap = null;
         try {
              bitmap = createThumbnailBitmap(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, path);
         } catch (IllegalArgumentException e) {
         } catch (OutOfMemoryError ex) {
         }
         return bitmap;
     }

    private Bitmap createThumbnailBitmap(int thumbnailBoundsHeightLimit, int thumbnailBoundsWidthLimit, String path) {
    	Uri uri = Uri.fromFile(new File(path));
    	UriImage uriImage = new UriImage(mContext, uri);
        int outWidth = uriImage.getWidth();
        int outHeight = uriImage.getHeight();

        int s = 1;
        // Aurora xuyong 2014-07-15 modified for bug #5394 start
        while ((outWidth / s > thumbnailBoundsWidthLimit)
                || (outHeight / s > thumbnailBoundsHeightLimit)) {
        // Aurora xuyong 2014-07-15 modified for bug #5394 end
            s *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = s;

        InputStream input = null;
        InputStream inputForRotate = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(input, null, options);
            return b;
        } catch (FileNotFoundException e) {
            return null;
        } catch (OutOfMemoryError ex) {
            throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
