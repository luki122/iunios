package com.gionee.mms.online;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class LogUtils {

    public static boolean isSaveLog = false;
    public static final int BACKUP_RESOTRE_AUTO_NOTIFY_ID = 201275;
    public static final String TAG_NETWORK = "Network";
    private static boolean flag = true;
    private static final String TAG = "LogUtils";
    private static final String FILE_NAME = "mms_inline_log.txt";
    private static String LOG_HEAD = "MMS_INLINE";

    public static void log(String TAG, String msg) {
        if (flag) {
            Log.i(LOG_HEAD + "." + TAG, "" + msg);
            if (isSaveLog) {
                try {
                    saveToSDCard("\n" + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void logv(String TAG, String msg) {
        if (flag) {
            Log.v(LOG_HEAD + "." + TAG, "" + msg);
            if (isSaveLog) {
                try {
                    saveToSDCard("\n" + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void logd(String TAG, String msg) {
        if (flag) {
            Log.d(LOG_HEAD + "." + TAG, "" + msg);
            if (isSaveLog) {
                try {
                    saveToSDCard("\n" + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loge(String TAG, String msg) {
        if (flag) {
            Log.e(LOG_HEAD + "." + TAG + ".error", "" + msg);
            if (isSaveLog) {
                try {
                    saveToSDCard("\n" + msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loge(String TAG, String msg, Exception e) {
        if (flag) {
            Log.e(LOG_HEAD + "." + TAG, "" + msg, e);
            if (isSaveLog) {
                try {
                    saveToSDCard("\n" + msg);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void saveToSDCard(String content) throws Exception {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir, FILE_NAME);
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(content.getBytes());
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFunctionName() {
        StringBuffer sb = new StringBuffer();
        sb.append("-> ");
        sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
        sb.append("()");
        sb.append("-> ");
        return sb.toString();
    }

    public static String getThreadName() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(Thread.currentThread().getName());
            sb.append("-> ");
            sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
            sb.append("()");
            sb.append(" ");
        } catch (Exception e) {
            LogUtils.loge(TAG, e.getMessage());
        }
        return sb.toString();
    }

    public synchronized static String createtFileName() {
        java.util.Date dt = new java.util.Date(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        return fmt.format(dt);
    }

    public static String addSeparatorToPath(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
    }

    public static String formatFileLength(long fileLength) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (fileLength >= gb) {
            return String.format("%.1f GB", (float) fileLength / gb);
        } else if (fileLength >= mb) {
            float f = (float) fileLength / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (fileLength >= kb) {
            float f = (float) fileLength / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", fileLength);
    }

    public static String formatFileLengthEndMB(long fileLength) {
        float size = Float.parseFloat(Long.toString(fileLength));
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSize = df.format(size / (1024 * 1024));
        return fileSize + "MB";
    }

    public static byte[] drawable2Bytes(Drawable drawable) {
        Bitmap bitmap = drawable2Bitmap(drawable);
        return bitmap2Bytes(bitmap);
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        // canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            manager.cancel(notificationId);
        } catch (Exception e) {
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static Drawable fetchApplicationIcon(Context context,
            String packageName) {
        Drawable icon = null;
        PackageManager pm = context.getPackageManager();
        try {
            icon = pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return icon;
    }

}