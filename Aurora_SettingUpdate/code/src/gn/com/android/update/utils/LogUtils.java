package gn.com.android.update.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class LogUtils {

    public static boolean sIsSaveLog = false;
    private static final String FILE_NAME = "gn_upgrade_log.txt";
    private static final boolean FLAG = true;
    private static final String LOG_HEAD = "OTA";
    private static final boolean SHOW_FLAG = true;
    private static final Format FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS", Locale.US);
    
    private static final int LOG_VERBOSE = 0;
    private static final int LOG_DEBUG = 1;
    private static final int LOG_INFO= 2;
    private static final int LOG_WARN = 3;
    private static final int LOG_ERROR = 4;
    

    private static String formatLog(String log, String type, String level) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(FORMATTER.format(Calendar.getInstance().getTime()));
        builder.append("][");
        builder.append(getThreadId());
        builder.append("][");
        builder.append(level);
        builder.append("][");
        builder.append(type);
        builder.append("]");
        builder.append("\n");
        return builder.toString();
    }

    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    public static void log(String tag, String msg) {
    	if(SHOW_FLAG){
	        if (FLAG) {
	            Log.i(LOG_HEAD , tag+"--->" + msg); 
	
	            if (sIsSaveLog) {
	                saveToSDCard(formatLog(msg, tag, "D"));
	            }
	        } else {
	        	standardLog(tag,msg,LOG_DEBUG);
	        }
    	}
    }

    public static void logv(String tag, String msg) {
    	if(SHOW_FLAG){
	        if (FLAG) {
	            Log.v(LOG_HEAD , tag+"--->" + msg);
	
	            if (sIsSaveLog) {
	                saveToSDCard(formatLog(msg, tag, "V"));
	            }
	        } else {
	        	standardLog(tag,msg,LOG_VERBOSE);
	        }
    	}
    }

    public static void logd(String tag, String msg) {
    	if(SHOW_FLAG){
	        if (FLAG) {
	            Log.d(LOG_HEAD , tag+"--->" + msg);
	
	            if (sIsSaveLog) {
	                saveToSDCard(formatLog(msg, tag, "d"));
	            }
	        } else {
	        	standardLog(tag,msg,LOG_DEBUG);
	        }
    	}
    }

    public static void loge(String tag, String msg) {
    	if(SHOW_FLAG){
	        if (FLAG) {
	            Log.e(LOG_HEAD , tag+"--->" + msg);
	
	            if (sIsSaveLog) {
	                saveToSDCard(formatLog(msg, tag, "E"));
	            }
	        } else {
	        	standardLog(tag,msg,LOG_ERROR);
	        }
    	}
    }
    /*
     * 没有统一的Tag的时候，使用各自的Tag
     * level 是输出的log级别
     */
    public static void standardLog(String tag,String msg,int level){
    	switch(level){
	    	case LOG_VERBOSE:
		    	Log.v(tag,msg);
		    	break;
	    	case LOG_DEBUG:
		    	Log.d(tag,msg);
		    	break;
	    	case LOG_INFO:
		    	Log.i(tag,msg);
		    	break;
	    	case LOG_WARN:
		    	Log.w(tag,msg);
		    	break;
	    	case LOG_ERROR:
		    	Log.e(tag,msg);
		    	break;
		    default:
		    	Log.e(LOG_HEAD,"Error Log Level !!!!");
		    	break;
    	}
    }

    public static void saveToSDCard(String content) {
        if (StorageUtil.checkExternalStorageMounted()) {
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

}
