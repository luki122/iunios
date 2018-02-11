package com.aurora.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Handler;
import android.util.Log;

import android.os.HandlerThread;
import android.text.format.DateFormat;

//added by vulcan in 2014-5-17
//LogWriter class
public class LogWriter {
	static boolean mSwitch = true;// determine if to use LogWriter
	static boolean mIsOpen = false;// indicate if it is opened
	static Writer mWriter = null;
	static final SimpleDateFormat df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ", Locale.CHINA);
    static HandlerThread mWriterThread;
	static Handler myHandler;
	
    static {
    	open();
    	mWriterThread = new HandlerThread("logWriterTask");
    	mWriterThread.start();
    	myHandler = new Handler(mWriterThread.getLooper());
    }

	static int open(){
		
		String logFileName = "logVulcan.txt";

		Log.d("vulcan", "enter _open,name = " + logFileName);

		String logDir = File.separator + "sdcard" + File.separator + "IUNILog";
		//String logDir = File.separator + "sdcard" + File.separator + "IUNILog";
		Log.d("vulcan","_open: logDir = " + logDir);

		String logPath = logDir + File.separator + logFileName;
		Log.d("vulcan","_open: logPath = " + logPath);
		
		File f = createFileWithPath(logPath);
		if(null == f) {
			Log.d("vulcan","open: failure in calling createFileWithPath, logPath = " + logPath);
			return -1;
		}

		try {
			FileWriter fWriter = new FileWriter(f,true);
			mWriter = new BufferedWriter(fWriter, 2048);
			Log.d("vulcan","open: calling BufferedWriter or FileWriter, fWriter = " + fWriter);
			Log.d("vulcan","open: calling BufferedWriter or FileWriter, mWriter = " + mWriter);
		} catch (IOException e) {
			Log.d("vulcan","open: failure in calling BufferedWriter or FileWriter");
			e.printStackTrace();
			return -1;
		} catch (IllegalArgumentException e){
			Log.d("vulcan","open: failure on IllegalArgumentException");
			e.printStackTrace();
			return -1;
		}

		mIsOpen = true;
		Log.d("vulcan","open: succeed in opening logWriter, mWriter = " + mWriter);

		return 0;
	}

	static File createFileWithPath(String fPath) {
		File f = new File(fPath);
		
		//1.check if the file represented by the fPath is there.
		//2.if it is there and it is file,simply return it;
		//3.if it is there but it is directory,we have to do nothing and return null.
		//4.if it is not there,we will create the parent directory and the file
		if (f.exists()) {
			if (f.isFile()) {
				return f;
			} else {
				return null;
			}
		}

		//1.if parent directory is root,simple return the file created.
		//2.if parent directory is not root,try to create the parent
		//3.if mkdirs return success or the parent is already there,return the file created.
		//4.or it is actually a file,we have to do nothing and return null
		if(f.getParentFile() == null) {
			Log.d("vulcan","createFileWithPath: file is in root directory");
			return f;
		} else {
			if(f.getParentFile().mkdirs() || f.getParentFile().isDirectory()) {
				Log.d("vulcan","createFileWithPath: succeed in making parent directory");
				return f;
			}
		}
		
		Log.d("vulcan","createFileWithPath: fail to create log file!!!");
		return null;
	}

	static void _close() {
		try {
			if (mWriter != null) {
				mWriter.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		mWriter = null;
		mIsOpen = false;
		return;
	}

	private void _print(String log) {
		//Log.e("vulcan-print", "calling _print,log = " + log + df.format(new Date()));

		try {
			if (mWriter != null) {
				mWriter.write(df.format(new Date()));
				mWriter.write(log);
				mWriter.write("\n");
				mWriter.flush();
			} else {
				Log.e("vulcan", "_print: NullPointer from LogWriter");
			}
		} catch (IOException e) {
			Log.e("vulcan","_print: IOException from LogWriter");
			e.printStackTrace();
		}
	}

	public void print(final String log) {
		Log.d("logWriter", "log = " + log);
		
		//if log switch is off or log module is not open, we don't send print request!!!
		if (!mSwitch || !mIsOpen) {
			Log.d("vulcan","print: cancel printing: mSwitch = " + mSwitch + ", mIsOpen = " + mIsOpen);
			return;
		}

		myHandler.post(new Runnable() {
			public void run() {
				LogWriter.this._print(log);
			}
		});
	}

	public void close() {
		//if log switch is off or log module is not open, we don't send close request!!!
		if (!mSwitch || !mIsOpen) {
			Log.d("vulcan","close: cancel closing: mSwitch = " + mSwitch + ", mIsOpen = " + mIsOpen);
			return;
		}

		myHandler.post(new Runnable() {
			public void run() {
				LogWriter._close();
			}
		});
	}
	
	public static String StackToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
	
	public static String timeBuilt(){
	    return "Built at @timeBuilt@ on @dateBuilt@";
	}
	
	
	/**
	 * iconcat, convert from time of long type to human-readable time.
	 * @param time
	 * @return if time is invalid, return "0", else return the human-readable time.
	 */
	public static String LongTimeToFormatTime(long time) {
		CharSequence cs = DateFormat.format("yyyy:MM:dd hh:mm:ss", time);
		if(cs != null) {
			return cs.toString();
		}
		return "0";
	}

}