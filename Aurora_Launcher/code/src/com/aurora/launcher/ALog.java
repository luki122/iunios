package com.aurora.launcher;

import android.os.Build;
import android.util.Log;

/**
 * @author xiejun
 */
public class ALog {
	public static final String TAG_TRACE = "Launcher_Trace";
	public static final String AURORA_LAUNCHER_DEFAULT_TAG = "Aurora_Launcher";
	public static boolean DEBUG = false;
	public static boolean DEBUG_TRACE = false;

	public static void dTrace() {
		if (!DEBUG_TRACE)
			return;
		//new Exception(TAG_TRACE).printStackTrace();
		Exception exception = new Exception();
		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		for(StackTraceElement s:stackTraceElements){
			ALog.d(TAG_TRACE,"		"+s.toString());
		}
	}

	public static void dTrace(String tag) {
		if (!DEBUG_TRACE)
			return;
		Exception exception = new Exception();
		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		for(StackTraceElement s:stackTraceElements){
			ALog.d(tag,"		"+s.toString());
		}
	}

	public static void setDebug(boolean debug) {
		DEBUG = debug;
	}

	public static void setDebugTrace(boolean debugTrace) {
		DEBUG_TRACE = debugTrace;
	}

	public static void i(String log) {
		if (!DEBUG)return;
		i(AURORA_LAUNCHER_DEFAULT_TAG, log);
	}

	public static void i(String tag, String log) {
		if (!DEBUG)return;
		Log.i(tag, log);
	}

	public static void d(String log) {
		if (!DEBUG)return;
		d(AURORA_LAUNCHER_DEFAULT_TAG, log);
	}

	public static void d(String tag, String log) {
		if (!DEBUG)return;
		Log.d(tag, log);
	}

	public static void e(String log) {
		if (!DEBUG)return;
		e(AURORA_LAUNCHER_DEFAULT_TAG, log);
	}

	public static void e(String tag, String log) {
		if (!DEBUG)return;
		Log.e(tag, log);
	}
	
	public static void debug(){
		if(DEBUG||DEBUG_TRACE){
			Log.i("DEBUG","Launcher DEBUG CLOSED!");
			setDebug(false);
			setDebugTrace(false);
		}else{
			Log.i("DEBUG","Launcher DEBUG OPENED!");
			setDebug(true);
			setDebugTrace(true);
		}
	}
	
	
	public static void initJustDebug(){
		if(!Build.TYPE.equals("user")){
			setDebug(true);
		}
	}
	
	public static void initDTrace(){
		if(!Build.TYPE.equals("user")){
			setDebugTrace(true);
		}
	}
}
