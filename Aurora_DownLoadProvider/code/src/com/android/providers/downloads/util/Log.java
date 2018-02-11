//Gionee <duansw><2012-12-18> modify for CR00789329 begin
package com.android.providers.downloads.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {

    private static final String GLOBAL_TAG = "DownloadManager";
    private static final String ENABLE_SAVELOG_FLAG_FOLDER = "download";
    // Warning:the following do not modify,Default values
    
    private static final String SAVELOG_FILE_NAME = GLOBAL_TAG + "_Log.txt";
    private static final String TAG = GLOBAL_TAG + ".LogUtils";
    private static boolean sEnableLog = true;
    private static boolean sDebug = true;
    private static boolean sIsSaveLog = false;

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");

    public static void loadInitConfigs(Context context) {
        loadInitConfigs();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            int versionCode = pi.versionCode;
            String versionName = pi.versionName;
            Log.d(TAG, Log.getThreadName() + "versionName = " + versionName + " versionCode = "
                    + versionCode);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void loadInitConfigs() {
        android.util.Log.d(TAG, Log.getThreadName());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = getExternalStorageDirectory();
            File saveLogFlagFile = new File(sdCardDir + ENABLE_SAVELOG_FLAG_FOLDER);
            if (saveLogFlagFile.exists()) {
                Log.i(TAG, Log.getThreadName() + GLOBAL_TAG + ",savelog flag is true");
                Log.sIsSaveLog = true;
                Log.sDebug = true;
            } else {
                Log.i(TAG, Log.getThreadName() + GLOBAL_TAG + ",savelog flag is true");
            }
        }
    }

    public static void i(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog) {
            android.util.Log.i(GLOBAL_TAG + "." + tag, "" + msg);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "i"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void v(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog && sDebug) {
            android.util.Log.v(GLOBAL_TAG + "." + tag, "" + msg);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "V"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void d(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog && sDebug) {
            android.util.Log.d(GLOBAL_TAG + "." + tag, msg);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "D"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void w(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog && sDebug) {
            android.util.Log.w(GLOBAL_TAG + "." + tag, msg);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "D"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void e(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog) {
            android.util.Log.e(GLOBAL_TAG + "." + tag + ".E", "" + msg);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "E"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (msg == null) {
            msg = "";
        }
        if (sEnableLog) {
            android.util.Log.e(GLOBAL_TAG + "." + tag, "" + msg, e);
            if (sIsSaveLog) {
                try {
                    saveToSDCard(formatLog(msg, tag, "E"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void saveToSDCard(String content) throws Exception {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String sdCardDir = getExternalStorageDirectory();
                String dir =sdCardDir + File.separator + ENABLE_SAVELOG_FLAG_FOLDER;
                File filedir = new File(dir);
                if(!filedir.exists()){
                	filedir.mkdirs();
                }
                File file = new File(dir,
                        SAVELOG_FILE_NAME);
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
            Log.e(TAG, e.getMessage());
        }
        return sb.toString();
    }

    private static String formatLog(String log, String type, String level) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        synchronized (sFormatter) {
            builder.append(sFormatter.format(Calendar.getInstance().getTime()));
        }
        builder.append("][");
        builder.append(type);
        builder.append("][");
        builder.append(level);
        builder.append("]");
        builder.append(log);
        builder.append("\n");
        return builder.toString();
    }

    private static String getExternalStorageDirectory() {
        String rootpath = Environment.getExternalStorageDirectory().getPath();
        if (!rootpath.endsWith(File.separator)) {
            rootpath += File.separator;
        }
        return rootpath;
    }

}
//Gionee <duansw><2012-12-18> modify for CR00789329 end

