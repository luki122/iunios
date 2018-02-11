/*
 * @author  张伟
 */
package com.aurora.note.util;


public final class Log {
	
	private static boolean mbLoggable = true; 
	
	public static void setLoggable(boolean bLoggable)
	{
		mbLoggable = bLoggable;
	}
	
	public static boolean isLoggable()
	{
		return mbLoggable;
	}
	
	public static boolean isDebuggable()
	{
		return mbLoggable;
	}
	
    public static int i(String tag, String msg) {
    	if (!mbLoggable)
    		return -1;
    
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	
        return android.util.Log.i(tag, ste.getFileName() + "\t  Line " + ste.getLineNumber() + "\t" + msg);
    }
    
    public static int i(String tag, String msg, Throwable tr) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
        return android.util.Log.i(tag, ste.getFileName() + "\t  Line " + ste.getLineNumber() + "\t" + msg, tr);
    }
    
    public static int d(String tag, String msg) {
    	if (!mbLoggable)
    		return -1;
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	
    	return android.util.Log.d(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg);
    }
    
    public static int d(String tag, String msg, Throwable tr) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.d(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }
    
    public static int e(String tag, String msg) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.e(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg);
    }
	
    public static int e(String tag, String msg, Throwable tr) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.e(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }

	public static int v(String tag, String msg) {
    	if (!mbLoggable)
    		return -1;
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	
    	return android.util.Log.v(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg);
    }
    
    public static int v(String tag, String msg, Throwable tr) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.v(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }
    
    public static int w(String tag, String msg) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.w(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg);
    }
	
    public static int w(String tag, String msg, Throwable tr) {
    	if (!mbLoggable)
    		return -1;
    	
    	StackTraceElement ste = new Throwable().getStackTrace()[1];
    	return android.util.Log.w(tag, ste.getFileName() + "\t Line " + ste.getLineNumber() + "\t" + msg, tr);
    }
}
