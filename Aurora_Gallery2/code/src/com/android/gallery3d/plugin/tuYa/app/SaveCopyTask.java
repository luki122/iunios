package com.android.gallery3d.plugin.tuYa.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

//import com.gionee.gallery.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class SaveCopyTask {

    private static final String TAG = "SaveCopyTask";
    private static final boolean DEBUG = false;//BuildConfig.DEBUG;
    private static final int MSG_SAVE_FINISH = 1;
    private final Context mContext;
    private final Uri mSourceUri;
    private final DrawingManager mDrawingManager;
    private static final String TIME_STAMP_NAME = "'IMG'_yyyyMMdd_HHmmss";
    private static final String DEFAULT_SAVE_DIRECTORY = "TuYaOnlinePhotos";

    public static final int SPACE_FULL_FAILED_SAVE = -1;
    public static final int FAILED_SAVE = -2;
    public static final int SUCCEED_SAVE = 0;

    private int mFailedType = SUCCEED_SAVE;
    private File mDestinationFile;

    public interface Callback {
        public void onComplete(Uri result, int failedType, String absolutePath);
    }

    public SaveCopyTask(Context context, Uri sourceUri, final Callback callback, DrawingManager drawingManager) {
        mContext = context;
        mSourceUri = sourceUri;
        mDrawingManager = drawingManager;
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (callback != null) {
                    callback.onComplete((Uri) msg.obj, mFailedType,
                            mDestinationFile != null ? mDestinationFile.getAbsolutePath() : null);
                }
            }
        };
        new Thread() {
            public void run() {
                Uri uri = saveImage();
                handler.sendMessage(handler.obtainMessage(MSG_SAVE_FINISH, uri));
            }
        }.start();
    }

    private Uri saveImage() {
        DrawingManager drawingManager = mDrawingManager;
        Bitmap oriBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 1;
            options.inMutable = true;
            oriBitmap = TuYaUtils.loadMutableBitmap(options, mContext, mSourceUri);
            Bitmap bitmap = null;
            try {
                bitmap = drawingManager.apply(oriBitmap);
            } catch (OutOfMemoryError e) {
                final int maxTryNum = 8;
                for (int i = 0; i < maxTryNum; i++) {
                    oriBitmap.recycle();
                    options.inSampleSize *= 2;
                    oriBitmap = TuYaUtils.loadMutableBitmap(options, mContext, mSourceUri);
                    try {
                        bitmap = drawingManager.apply(oriBitmap);
                    } catch (OutOfMemoryError e1) {
                        Log.w(TAG, "  doInBackground:saveBitmap :out of memory when decoding:" + e1);
                        bitmap = null;
                    }
                    if (bitmap != null) {
                        break;
                    }
                }
            }
            if (bitmap == null) {
                mFailedType = FAILED_SAVE;
                return null;
            }
            mDestinationFile = getNewFile(mContext, mSourceUri);
            List<String> rootPaths = TuYaUtils.getLocalRootPath(mContext);
            boolean result = TuYaUtils.saveBitmapToFile(bitmap, mDestinationFile, rootPaths);
            File newFile = null;
            if (result == false) {
                if (rootPaths != null && rootPaths.size() > 1) {
                    String absolutePath = mDestinationFile.getAbsolutePath();
                    newFile = TuYaUtils.createOtherSdCardFile(rootPaths, absolutePath);
                }
            }

            if (newFile != null) {
                if (DEBUG) {
                    Log.d(TAG, "other sdcard");
                }
                mDestinationFile = newFile;
                newFile = null;
                result = TuYaUtils.saveBitmapToFile(bitmap, mDestinationFile, rootPaths);
            }
            if (false == result) {
                mFailedType = SPACE_FULL_FAILED_SAVE;
                return null;
            }
            TuYaUtils.copyExif(mSourceUri, mDestinationFile.getAbsolutePath(), mContext);
            Uri uri = TuYaUtils.insertContent(mContext, mSourceUri, mDestinationFile, mDestinationFile.getName());
            return uri;

        } catch (FileNotFoundException ex) {
            Log.w(TAG, "Failed to save image!", ex);
            mFailedType = FAILED_SAVE;
            return null;
        } catch (Exception e) {
            Log.w(TAG, "Failed to save image!", e);
            mFailedType = FAILED_SAVE;
            return null;
        } finally {
            if (oriBitmap != null) {
                oriBitmap.recycle();
                oriBitmap = null;
            }
        }
    }

    private File getNewFile(Context context, Uri sourceUri) {
        File saveDirectory = getFinalSaveDirectory(context, sourceUri);
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(System.currentTimeMillis()));
        return new File(saveDirectory, filename + ".jpg");
    }

    private File getFinalSaveDirectory(Context context, Uri sourceUri) {
        File saveDirectory = TuYaUtils.getSaveDirectory(context, sourceUri);
        if ((saveDirectory == null) || !saveDirectory.canWrite()) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_SAVE_DIRECTORY);
        }
        // Create the directory if it doesn't exist
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs();
        }
        return saveDirectory;
    }

}
