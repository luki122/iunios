package com.aurora.worldtime;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import com.aurora.worldtime.HanziToPinyin.Token;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
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

public class UsefulUtils {

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
		
		InputMethodManager imm = (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}
	
	/**
	 * @param context
	 * @param edit
	 */
	public static void showSoftInput(Context context,EditText edit){
		if(context == null || edit == null){
			return ;
		}
		
		InputMethodManager imm = (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);
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
    
	/**
	 * 获取运行的进程数
	 * @param activity
	 * @return
	 */
	public static int getProcessCount(Activity activity) {
		if(!isActivityAvailable(activity)){
			return 0;
		}
		ActivityManager am = (ActivityManager)activity.getSystemService(activity.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runingappinfos = am.getRunningAppProcesses();
		if(runingappinfos == null){
			return 0;
		}else{
			return runingappinfos.size();
		}		
	}
	
	/**
	 * 获取当前系统的剩余的可用内存信息 
	 * @param activity
	 * @return long
	 */
	public static long getAvailMemoryInfo(Activity activity) {
		if(!isActivityAvailable(activity)){
			return 0;
		}
		ActivityManager am = (ActivityManager)activity.getSystemService(activity.ACTIVITY_SERVICE);
		
		MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(outInfo);
		return outInfo.availMem;
	}
	
	/**
	 * 将单位为byte的memorySize处理成单位为GB，MB或KB的string
	 * @param memorySize
	 * @return
	 */
	public static String dealMemorySize(Context context, long memorySize){	
		if(context == null){
			return "0.00kb";
		}else{
			float result = memorySize;
	        String suffix = "B";
	        if (result > 900) {
	            suffix = "KB";
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = "MB";
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = "GB";
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = "TB";
	            result = result / 1024;
	        }
	        if (result > 900) {
	            suffix = "PB";
	            result = result / 1024;
	        }
	        String value = String.format("%.2f", result);
	        return value+suffix;
		}	
	}
	
	/**
	 * 去掉空格
	 * @param str
	 * @return
	 */
	public static String removeSpace(String str){
		if(str == null)
			return null;	
		str = str.replaceAll(" ", "");		
		return str;		
	} 
	
	/**
	 * 返回的拼音为大写字母
	 * @param str
	 * @return
	 */
	public static String getSpell(String str){
		StringBuffer buffer=new StringBuffer();
		
		if(str!=null && !str.equals("")){
			char[] cc=str.toCharArray();
			for(int i=0;i<cc.length;i++){
				ArrayList<Token> mArrayList = HanziToPinyin.getInstance().get(String.valueOf(cc[i]));
				if(mArrayList.size() > 0 ){
					String n = mArrayList.get(0).target;
					buffer.append(n);
				}
			}
		}
		String spellStr = buffer.toString();
		return spellStr.toUpperCase();
	}
	
	/**
	 * 比较两个拼音string的大小
	 * @param s1
	 * @param s2
	 * @return
	 */
    public static int compare(String s1, String s2) {
    	Collator collator = ((java.text.RuleBasedCollator)java.text.Collator.
    	    	getInstance(java.util.Locale.ENGLISH));
    	return collator.compare(s1, s2);
    }
    
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */ 
    public static int dip2px(Context context, float dpValue) { 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int) (dpValue * scale + 0.5f); 
    } 

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */ 
    public static int px2dip(Context context, float pxValue) { 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int) (pxValue / scale + 0.5f); 
    }  
}