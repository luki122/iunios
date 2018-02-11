package com.aurora.mms.ui;
// Aurora xuyong 2014-04-29 created for aurora's new feature
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
// Aurora xuyong 2015-03-18 added for bug #12316 start
import java.util.HashMap;
// Aurora xuyong 2015-03-18 added for bug #12316 end
import java.util.LinkedList;
// Aurora xuyong 2015-03-18 added for bug #12316 start
import java.util.Map;
// Aurora xuyong 2015-03-18 added for bug #12316 end
import java.util.Queue;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.android.mms.R;
// Aurora xuyong 2014-07-15 added for bug #5934 start
import com.aurora.utils.DensityUtil;
// Aurora xuyong 2014-07-15 added for bug #5934 end
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.UriImage;
import com.android.mms.ui.VideoAttachmentView;
import com.aurora.mms.util.AsyncTask;
import com.aurora.mms.util.Utils;

public class ThumbnailWorker {
    
    private Context mContext;
    protected Resources mResources;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
   // Aurora xuyong 2014-05-04 modified for aurora's new feature start
    // Aurora xuyong 2015-08-20 modified for bug #15782 start
    private static final int MAX_TASK_COUNTS = 8;
    // Aurora xuyong 2015-08-20 modified for bug #15782 end
   // Aurora xuyong 2014-05-04 modified for aurora's new feature end
   // Aurora xuyong 2014-05-07 modified for bug 4693 start
    private Queue<String> mTaskList = new LinkedList<String>();
    // Aurora xuyong 2015-03-18 added for bug #12316 start
    private Map<String, String> mTaskMap = new HashMap<String, String>();
    // Aurora xuyong 2015-03-18 added for bug #12316 end
   // Aurora xuyong 2014-05-07 modified for bug 4693 end
   // Aurora xuyong 2014-05-05 added for aurora's new feature start
    private static boolean sNeedListCache = true;
    // Aurora xuyong 2014-05-05 added for aurora's new feature end
    public ThumbnailWorker(Context context) {
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
    // Aurora xuyong 2014-05-07 modified for bug 4693 start
    public void clearTaskSet() {
   // Aurora xuyong 2014-05-07 modified for bug 4693 end
        if (mTaskList != null && mTaskList.size() > 0) {
            mTaskList.clear();
        }
        // Aurora xuyong 2015-03-18 added for bug #12316 start
        if (mTaskMap != null && mTaskMap.size() > 0) {
            mTaskMap.clear();
        }
        // Aurora xuyong 2015-03-18 added for bug #12316 end
    }
    // Aurora xuyong 2014-05-05 added for aurora's new feature start
    public static void setNeedCache(boolean needCache) {
        sNeedListCache = needCache;
    }
    // Aurora xuyong 2014-05-05 added for aurora's new feature end
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
        // Aurora xuyong 2014-10-31 deleted for bug #9398 start
        //private final int ANIMI_DURATION = 100;
        // Aurora xuyong 2014-10-31 deleted for bug #9398 end
        private Object data;
        private int mType;
        private int mDuration;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView, int type) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            mType = type;
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

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        // Aurora xuyong 2014-10-31 modified for bug #9398 start
                        mPauseWorkLock.wait(100);
                        // Aurora xuyong 2014-10-31 modified for bug #9398 end
                    } catch (InterruptedException e) {}
                }
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (dad == null && !isCancelled() && getAttachedImageView() != null) {
                dad = processBitmap(params[0], mType);
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
            if (value != null && imageView != null) {
                imageView.setImageDrawable(value.getDrawable());
                if (imageView instanceof AuroraRoundImageView) {
                    ((AuroraRoundImageView)imageView).setDuration(value.getDuration());
                }
                // Aurora xuyong 2014-10-31 deleted for bug #9398 start
                //final AlphaAnimation anim = new AlphaAnimation(0, 1);
                //anim.setDuration(ANIMI_DURATION);
                //imageView.setAnimation(anim);
                // Aurora xuyong 2014-10-31 deleted for bug #9398 end
                if (mTaskList.size() >= MAX_TASK_COUNTS) {
                    // Aurora xuyong 2015-03-18 modified for bug #12316 start
                    mTaskMap.remove(mTaskList.remove());
                    // Aurora xuyong 2015-03-18 modified for bug #12316 end
                }
             // Aurora xuyong 2014-05-05 modified for aurora's new feature start
                if (sNeedListCache) {
                    mTaskList.add(data.toString());
                    // Aurora xuyong 2015-03-18 added for bug #12316 start
                    mTaskMap.put(data.toString(), value.getDrawable().toString());
                    // Aurora xuyong 2015-03-18 added for bug #12316 s
                }
             // Aurora xuyong 2014-05-05 modified for aurora's new feature end
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
    
    public void loadImage(Object data, ImageView imageView, int type) {
    	// Aurora xuyong 2015-10-15 modified for aurora's new feature start
        Bitmap loadingBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.aurora_attach_default_bg);
        // Aurora xuyong 2015-10-15 modified for aurora's new feature end
        if (data == null) {
            return;
        }
        // Aurora xuyong 2014-05-23 added for small issue start
        // Aurora xuyong 2015-03-18 modified for bug #12316 start
        if (cancelPotentialWork(data, imageView) && (imageView.getDrawable() == null || mTaskMap.get(data.toString()) == null
                || !mTaskMap.get(data.toString()).equals(imageView.getDrawable().toString()))) {
        // Aurora xuyong 2015-03-18 modified for bug #12316 end
        // Aurora xuyong 2014-05-23 added for small issue end
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, type);
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
    // Aurora xuyong 2014-07-15 added for bug #5934 start
    private final static int THUMBNAIL_BOUNDS_HEIGHT_LIMIT = 336;
    private final static int THUMBNAIL_BOUNDS_WIDTH_LIMIT = 276;
    private Bitmap decodeDefaultResource(int thumbnailBoundsHeightLimit, int thumbnailBoundsWidthLimit, int type) {
        BitmapFactory.Options opt = new BitmapFactory.Options();  
        opt.inJustDecodeBounds = true; 
        switch (type) {
            case MessageListItem.isImage:
                BitmapFactory.decodeResource(mResources, R.drawable.aurora_image_thumbnail, opt);
                break;
            case MessageListItem.isAudio:
                BitmapFactory.decodeResource(mResources, R.drawable.aurora_audio_thumbnail, opt);   
                break;
            case MessageListItem.isVideo:
                BitmapFactory.decodeResource(mResources, R.drawable.aurora_video_thumbnail, opt); 
                break;
        }
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
        Bitmap bitmap = null;
        switch (type) {
            case MessageListItem.isImage:
                bitmap = BitmapFactory.decodeResource(mResources, R.drawable.aurora_image_thumbnail, opt);
                break;
            case MessageListItem.isAudio:
                bitmap = BitmapFactory.decodeResource(mResources, R.drawable.aurora_audio_thumbnail, opt);  
                break;
            case MessageListItem.isVideo:
                bitmap = BitmapFactory.decodeResource(mResources, R.drawable.aurora_video_thumbnail, opt);  
                break;
        }
        return bitmap;
    }
    // Aurora xuyong 2014-07-15 added for bug #5934 end
    private WorkDetails processBitmap(Object data, int type) {
        WorkDetails dad = new WorkDetails();
        Bitmap bitmap = null;
        String duration = "";
        switch (type) {
            case MessageListItem.isImage:
                if (null == data) {
                   // Aurora xuyong 2014-07-15 modified for bug #5934 start
                    bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, MessageListItem.isImage);
                   // Aurora xuyong 2014-07-15 modified for bug #5934 end
                    break;
                }
                bitmap = internalGetBitmap((Uri)data);
                if (bitmap == null) {
                // Aurora xuyong 2014-07-15 modified for bug #5934 start
                    bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, MessageListItem.isImage);
                // Aurora xuyong 2014-07-15 modified for bug #5934 end
                }
                break;
            case MessageListItem.isAudio:
             // Aurora xuyong 2014-07-15 modified for bug #5934 start
                bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, MessageListItem.isAudio);
             // Aurora xuyong 2014-07-15 modified for bug #5934 end
                int durA = VideoAttachmentView.getMediaDuration(mContext, (Uri)data);
                duration = VideoAttachmentView.initMediaDuration(durA);
                break;
            case MessageListItem.isVideo:
                if (null == data) {
                // Aurora xuyong 2014-07-15 modified for bug #5934 start
                    bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, MessageListItem.isVideo);
                // Aurora xuyong 2014-07-15 modified for bug #5934 end
                    break;
                }
                bitmap = VideoAttachmentView.createVideoThumbnail(mContext, (Uri)data);
                if (bitmap == null) {
                // Aurora xuyong 2014-07-15 modified for bug #5934 start
                    bitmap = decodeDefaultResource(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, MessageListItem.isVideo);
                // Aurora xuyong 2014-07-15 modified for bug #5934 end
                }
                int durV = VideoAttachmentView.getMediaDuration(mContext, (Uri)data);
                duration = VideoAttachmentView.initMediaDuration(durV);
                break;
        }
        dad.setUri((Uri)data);
        dad.setBitmap(bitmap);
        dad.setDuration(duration);
        return dad;
    }
    
    public static class WorkDetails {
        Bitmap mBitmap;
        Drawable mDrawable;
        String mDuration;
        Uri mUri;
        
        public WorkDetails () {
        }
        
        public WorkDetails (Bitmap bitmap, Drawable drawable, String duration, Uri uri) {
            mBitmap = bitmap;
            mDrawable = drawable;
            mDuration = duration;
            mUri = uri;
        }
        
        public Bitmap getBitmap() {
            return mBitmap;
        }
        
        public Drawable getDrawable() {
            return mDrawable;
        }
        
        public String getDuration() {
            return mDuration;
        }
        
        public Uri getUri() {
            return mUri;
        }
        
        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }
        
        public void setDrawble(Drawable drawable) {
            mDrawable = drawable;
        }
        
        public void setDuration(String duration) {
            mDuration = duration;
        }
        
        public void setUri(Uri uri) {
            mUri = uri;
        }
    }
    
     private static final int THUMBNAIL_BOUNDS_LIMIT = 480;
     private Bitmap internalGetBitmap(Uri uri) {
         Bitmap bitmap = null;
         try {
           // Aurora xuyong 2014-07-15 modified for bug #5394 start
              bitmap = createThumbnailBitmap(THUMBNAIL_BOUNDS_HEIGHT_LIMIT, THUMBNAIL_BOUNDS_WIDTH_LIMIT, uri);
           // Aurora xuyong 2014-07-15 modified for bug #5394 end
         // Aurora xuyong 2014-05-04 added for aurora's new feature start
         } catch (IllegalArgumentException e) {
         // Aurora xuyong 2014-05-04 added for aurora's new feature end
         } catch (OutOfMemoryError ex) {
            // fall through and return a null bitmap. The callers can handle a null
            // result and show R.drawable.ic_missing_thumbnail_picture
         }
         return bitmap;
     }
    // Aurora xuyong 2014-07-15 modified for bug #5394 start 
    private Bitmap createThumbnailBitmap(int thumbnailBoundsHeightLimit, int thumbnailBoundsWidthLimit, Uri uri) {
    // Aurora xuyong 2014-07-15 modified for bug #5394 end
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
