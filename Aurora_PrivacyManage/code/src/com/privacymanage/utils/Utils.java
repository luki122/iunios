package com.privacymanage.utils;

import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {

	public static void closeQuietly(OutputStream stream){
		if(stream == null){
			return;
		}
		try{
			stream.flush();
			stream.close();
			stream = null;
		}catch (Exception e) {
		  //ignore
		}
	}
	
	public static void closeQuietly(InputStream stream){
		if(stream == null){
			return;
		}
		try{
			stream.close();
			stream = null;
		}catch (Exception e) {
		  //ignore
		}
	}
	
    /**
     * @param activity
     * @return
     */
	public static DisplayMetrics getDisplayMetrics(Activity activity){
		if(!isActivityAvailable(activity)){
			return null;
		}
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return metric;
	}
	
	/**
     * @param context
     * @return
     */
	public static DisplayMetrics getDisplayMetrics(Context context){
		if(context == null){
			return null;
		}
		DisplayMetrics dm = new DisplayMetrics();  
		dm = context.getResources().getDisplayMetrics(); 
		return dm;
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static String getPhoneNum(Context context){
		if(context == null){
			return null;
		}
		
		TelephonyManager phoneMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNum = phoneMgr.getLine1Number();
		if(phoneNum != null && phoneNum.length() != 11){
			phoneNum = null;
		}

		return phoneNum;
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static String getImsi(Context context){
		if(context == null){
			return null;
		}
		TelephonyManager phoneMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return phoneMgr.getSubscriberId();// IMSI
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static String getImei(Context context){
		if(context == null){
			return null;
		}
		TelephonyManager phoneMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return phoneMgr.getDeviceId();//IMEI
	}
	
	public static boolean isSDCardReady() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static String getApplicationFilesPath(Context context){
		if(context == null || context.getFilesDir() == null){
			return null;
		}else{
			return  context.getFilesDir().getPath();
		}		
	}
	
    /**
     * @param context
     * @param edit
     */
	public static void hideSoftInput(Context context,EditText edit){
		if(context == null || edit == null){
			return ;
		}
		
		InputMethodManager imm = (InputMethodManager)context.
				getSystemService(context.INPUT_METHOD_SERVICE);
		//LogUtils.printWithLogCat("vinput", "hideSoftInput: isOpen = " + imm.isActive());
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
		//LogUtils.printWithLogCat("vinput", "hideSoftInput: isOpen2 = " + imm.isActive());
	}
	
	/**
	 * @param context
	 * @param edit
	 */
	public static void showSoftInput(Context context,EditText edit){
		if(context == null || edit == null){
			return ;
		}
		
		InputMethodManager imm = (InputMethodManager)context.
				getSystemService(context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(edit, 0);
	}
	
	/**
	 * @return
	 */
	public static boolean is24(Context context){
		if(context == null){
			return true;
		}
		
		ContentResolver cv = context.getContentResolver();
	    String strTimeFormat = android.provider.Settings.System.getString(cv,
	                                           android.provider.Settings.System.TIME_12_24);
	        
	    if("24".equals(strTimeFormat)){
	        return true;
	    }else{
	        return false;
	    }
	}
    
    /**
     * @param context
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context) throws Exception{
    	if(context == null){
    		return null;
    	}
	    PackageManager packageManager = context.getPackageManager();
	    PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
	    String version = packInfo.versionName;
	    return version;
	}
    
    static String OwnPackageName = null;
    
    /**
     * @param context
     * @return
     * @throws Exception
     */
    public static String getOwnPackageName(Context context){
    	if(OwnPackageName != null){
    		return OwnPackageName;
    	}
    	if(context == null){
    		return null;
    	}
    	
    	try{
    		PackageManager packageManager = context.getPackageManager();
    	    PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
    	    OwnPackageName = packInfo.packageName;
    	    return OwnPackageName;
    	}catch(Exception e){
    		OwnPackageName = null;
    		e.printStackTrace();
    		return null;
    	}	    
	}
    
    /**
     * @param activity
     * @return true false
     */
    public static boolean isActivityAvailable(Activity activity){
    	if(activity == null ||
				activity.isFinishing()){
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * @param context
     * @param receiver
     */
    public static synchronized void unregisterReceiver(Context context,
    		BroadcastReceiver receiver){
    	if(context == null || 
    			receiver == null){
    		return ;
    	}
    	
    	try{
    		context.unregisterReceiver(receiver);
    	}catch(Exception e){
    		//ignore
    	}  	
    } 
}