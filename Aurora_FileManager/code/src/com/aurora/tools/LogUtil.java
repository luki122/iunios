package com.aurora.tools;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public class LogUtil {
	private static final String TAG ="Aurora_FileManager";
	private static final boolean isDebug = true;//Log.isLoggable(TAG, Log.INFO);
	private static final boolean elog = true;//Log.isLoggable(TAG, Log.ERROR);
	private static boolean sIsSaveLog = false;
	private static boolean sAuroraLog = true;
	private static String SAVELOG_FILE_NAME = "FileManager.log";
	private static final int MaxSize = 1024 * 1024 * 40;// 40M

	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat sFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSSS");

	public static void d(String TAG, String msg) {
		if (isDebug) {
			if(sAuroraLog){
				Log.d(LogUtil.TAG, TAG+"_"+msg);
			}else {
				Log.d(TAG, msg);
			}
		}
		if (sIsSaveLog) {
			saveToSDCard(formatLog(msg, TAG, "D"));
		}
	}

	public static void e(String TAG, String msg) {
		if (elog) {
			if(sAuroraLog){
				Log.e(LogUtil.TAG, TAG+"_"+msg);
			}else {
				Log.e(TAG, msg);
			}
		
		}
		if (sIsSaveLog) {
			saveToSDCard(formatLog(msg, TAG, "E"));
		}
	}

	public static void w(String TAG, String msg) {
		if (isDebug) {
			if(sAuroraLog){
				Log.w(LogUtil.TAG, TAG+"_"+msg);
			}else {
				Log.w(TAG, msg);
			}
			
		}
		if (sIsSaveLog) {
			saveToSDCard(formatLog(msg, TAG, "w"));
		}
	}

	

	public static void saveToSDCard(String content) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				String sdCardDir = getExternalStorageDirectory();
				String path = sdCardDir + File.separator
						+ "IUNILog/FileManager";
				File pathf = new File(path);
				if (!pathf.exists()) {
					pathf.mkdirs();
				}
				File file = new File(pathf.getPath(), SAVELOG_FILE_NAME);
				if (file.length() > MaxSize) {
					file.delete();
					return;
					
				}
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.seek(file.length());
				raf.write(content.getBytes());
				raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
