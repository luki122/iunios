package com.android.settings.lscreen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.android.settings.lscreen.HanziToPinyin.Token;
import com.android.settings.lscreen.ls.LSOperator;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.android.settings.R;

public class Utils {
	
	public static boolean isNetworkAvailable(Context context) {   
        ConnectivityManager cm = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        if (cm == null) {   
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();   
            if (info != null) {   
                for (int i = 0; i < info.length; i++) {   
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {   
                        return true;   
                    }   
                }   
            }   
        }   
        return false;
	}

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
	public static String dealMemorySize(Context context, long memorySize,String format){	
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
	        String value = String.format(format, result);
	        return value+suffix;
		}	
	} 
	
	/**
	 * 将单位为byte的memorySize处理成单位为GB，MB或KB的string,并且精确到小数点后两位
	 * @param context
	 * @param memorySize
	 * @return
	 */
	public static String dealMemorySize(Context context, long memorySize){		
		return dealMemorySize(context,memorySize,"%.2f");
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
    	if(StringUtils.isEmpty(s1) && StringUtils.isEmpty(s2)){
    		return 0;
    	}else if(StringUtils.isEmpty(s1) && !StringUtils.isEmpty(s2)){
    		return -1;
    	}else if(!StringUtils.isEmpty(s1) && StringUtils.isEmpty(s2)){
    		return 1;
    	}
    	
    	Collator collator = ((java.text.RuleBasedCollator)java.text.Collator.
    	    	getInstance(java.util.Locale.ENGLISH));
    	return collator.compare(s1, s2);
    }
    
    /**
     * 计算view的尺寸
     * @param child
     */
    public static void measureView(View child) {
    	if(child == null){
    		return ;
    	}
    	
 		ViewGroup.LayoutParams p = child.getLayoutParams();
 		if (p == null) {
 			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
 					ViewGroup.LayoutParams.WRAP_CONTENT);
 		}
 		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
 		int lpHeight = p.height;
 		int childHeightSpec;
 		if (lpHeight > 0) {
 			// MeasureSpec.UNSPECIFIED,
 			// 未指定尺寸这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式
 			// MeasureSpec.EXACTLY,精确尺寸
 			// MeasureSpec.AT_MOST最大尺寸
 			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
 					MeasureSpec.EXACTLY);
 		} else {
 			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
 					MeasureSpec.UNSPECIFIED);
 		}
 		child.measure(childWidthSpec, childHeightSpec);
 	}
    
    /**
     * 判断sd卡是否存在
     * @return
     */
    public static boolean ExistSDCard() {  
	    if(android.os.Environment.getExternalStorageState().equals(  
	       android.os.Environment.MEDIA_MOUNTED))  
	      return true;  
	    else  
	      return false;  
	}  
    
    public static String getExternalStoragePath(Context context){
    	return gionee.os.storage.GnStorageManager
 				.getInstance(context).getExternalStoragePath();
    }
    
    
    
    
    /**
     * 解压Assets中的文件
     * @param context上下文对象
     * @param assetName压缩包文件名
     * @param outputDirectory输出目录 // sdcard  mulu 
     * @throws IOException
     */
	public static void unZip(Context context, String assetName,
			String outputDirectory,boolean isReWrite) throws IOException {
		File file = new File(outputDirectory);
		if (!file.exists()) {
			file.mkdirs();
		}
		InputStream inputStream = context.getAssets().open(assetName);
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		byte[] buffer = new byte[1024 * 1024];
		int count = 0;
		while (zipEntry != null) {
			if (zipEntry.isDirectory()) {
				file = new File(outputDirectory + File.separator + zipEntry.getName());
				if(isReWrite || !file.exists()){
					file.mkdir();
				}
			} else {
				file = new File(outputDirectory + File.separator
						+ zipEntry.getName());
				if(isReWrite || !file.exists()){
					file.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					while ((count = zipInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, count);
					}
					fileOutputStream.close();
				}
			}
			zipEntry = zipInputStream.getNextEntry();
		}
		zipInputStream.close();
		boolean b=setChmod(LSOperator.TARGETBECOMEPATH);
		Log.d("gd", "  is set target path limits of authority = "+b);
	}
	
	/**
	 * 根据包名字取系统应用程序图片
	 */
	public static Drawable getSysAppIcon(Context mContext,String packageName)
	{
		Drawable appIcon = null;
		try {
		PackageManager pm = mContext.getPackageManager();
		ApplicationInfo appInfo;
		
			appInfo = pm.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
		    appIcon = pm.getApplicationIcon(appInfo);
		    
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// appIconIv.setImageDrawable(appIcon);
		return appIcon;
	}
	
	
	/**
	 * 将两张图片合并
	 */
    public static Bitmap mergeBitmap(Context mContext,Bitmap firstBitmap, Bitmap secondBitmap) {
    	
        Bitmap bitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(),
                firstBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(firstBitmap, new Matrix(), null);
        canvas.drawBitmap(secondBitmap, 0, 45, null);
        Typeface mFace = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
        Paint paint = new Paint();
        paint.setTypeface(mFace);
        paint.setAntiAlias(true);
        float iconWidth=firstBitmap.getWidth();
        float strLength=paint.measureText(mContext.getString(R.string.system));
        int begin_X=(int) ((iconWidth-strLength)/2-6);
        Log.d("gd", "  iconWidth="+iconWidth+"   strLength="+strLength+" X is="+begin_X +"  "+secondBitmap.getWidth());
//        paint.setTextAlign(Paint.Align.CENTER);  
		paint.setTextSize(mContext.getResources().
				getDimension(R.dimen.list_icon_text_size));
        canvas.drawText(mContext.getString(R.string.system), begin_X, 65, paint);
//        canvas.drawText(mContext.getString(R.string.system), 0, 65, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
        
    } 

    /** * 图片上画字 * */ 
    private Bitmap drawTextAtBitmap(Bitmap bitmap,String text)
    { 
    	int x = bitmap.getWidth(); 
    	int y = bitmap.getHeight(); 
    	// 创建一个和原图同样大小的位图 
    	Bitmap newbit = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888); 
    	Canvas canvas = new Canvas(newbit);
    	Paint paint = new Paint(); 
    	// 在原始位置0，0插入原图 
    	canvas.drawBitmap(bitmap, 0, 0, paint); 
    	paint.setColor(Color.parseColor("#dedbde"));
        paint.setTextSize(20);
        // 在原图指定位置写上字 
        canvas.drawText(text, 53 , 30, paint); 
        canvas.save(Canvas.ALL_SAVE_FLAG); // 存储 
        canvas.restore(); 
        return newbit; 
        }
    
    /**
     * 将指定的图片保存到指定的文件夹下面
     */
	public static void savaBitmap(String path,Bitmap bitmap,String packageName)
	{
		
		File f = new File(path, packageName);
		if (f.exists()) 
		{
			f.delete();
			return ;
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			setChmod(path);
			Log.i("gd", "已经保存");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取外部文件夹中的指定文件
	 */
	public static List<String> GetFiles(String Path, String Extension)
	{
		List<String> lstFile = new ArrayList<String>();
		// public void findFileByEndStr(String dirStr, String str) {
		if (null == Path) 
		{
			throw new RuntimeException("要搜索的目录名不能为null");
		}
		File dir = new File(Path);
		if (!dir.exists()) 
		{
			throw new RuntimeException(Path + "is not existed!");
		}

		if (null == Extension) 
		{
			throw new RuntimeException("要匹配的末尾不能为null");
		}

		File[] files = dir.listFiles();
		int length = files.length;
		Log.d("gd", "zhangwei the filename length="+length);
		for (File file : files) 
		{
			Log.d("gd", "  文件名称="+file.getName().toString() + " path="+Path);
			
			if (file.isFile() && file.toString().endsWith(Extension)) 
			{
				lstFile.add(file.getName().replace(".png", ""));
			}
			if (file.isDirectory()) 
			{
				GetFiles(file.toString(), Extension);
			}
		}

		return lstFile;
	}
	
	public static void delFiles(String Path, String Extension,String packageName)
	{
	        if (null == Path) {
	            throw new RuntimeException("要搜索的目录名不能为null");
	        }
	        File dir = new File(Path);
	        if (!dir.exists()) {
	            throw new RuntimeException(Path + "is not existed!");
	        }

	        if (null == Extension) {
	            throw new RuntimeException("要匹配的末尾不能为null");
	        }

	        File[] files = dir.listFiles();
	        for (File file : files) {
	            if (file.isFile() && file.toString().endsWith(Extension)) {
//	                Log.d("gd",("文件名:" + file.getName() + "\t\t绝对路径:"
//	                        + file.toString()));
	            	if(file.getName().replace(Extension, "").equals(packageName))
	            	{
	            		Log.d("gd", "|||||  delete  |||||");
	            		file.delete();
	            	}
	            }
	            if (file.isDirectory()) {
	            	delFiles(file.toString(), Extension, packageName);
	            }
	    }
	}

	
	public static void unSavaIconInFile(String packageName,String path)
	{
		Log.d("gd", " packageName="+packageName+ " unsave++++++++++++++++++++++++++++++++");
		ArrayList<String> appIconList=(ArrayList<String>) Utils.GetFiles(path,".png");
        if(appIconList.contains(packageName))
        {
        	Log.d("gd", " unsave+++++++++++++++++++++++++++++++++"+packageName);
        	delFiles(path,".png",packageName);
        }
        Utils.GetFiles(path,".png");
	}

    public static void savaIconInFile(Context mContext,String packageName,String path)
    {
    	
        ArrayList<String> appIconList=(ArrayList<String>) Utils.GetFiles(path,".png");
        
        if(appIconList.contains(packageName))
        {
        	return ;
        }
        //默认的合成图片
//        Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.message_underlay);
        //得到系统图片
        Drawable sysIcon=Utils.getSysAppIcon(mContext,packageName);
        BitmapDrawable sysBitMap = (BitmapDrawable) sysIcon;
        Bitmap bm = sysBitMap.getBitmap();
        
        Bitmap mBitmap = Bitmap.createScaledBitmap(bm, 70, 70, true); 
        
        Log.d("gd", "  Model = "+"  System app=  "+bm.toString());
        //合成图片
//        Bitmap mergeIcon=mergeBitmap(mBitmap,bitmap);
        
        savaBitmap(path,mBitmap,packageName+".png");
    }
    
    public static Drawable compositeDrawable(Context mContext,String packageName)
    {
        //默认的合成图片
        Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sys_app_flag);
        
        Drawable sysIcon=Utils.getSysAppIcon(mContext,packageName);
        if(sysIcon==null)
        {
        	Log.d("gd", " sysIcon is null  ");
        	return null;
        }
        BitmapDrawable sysBitMap = (BitmapDrawable) sysIcon;
        Bitmap bm = sysBitMap.getBitmap();
        Bitmap mBitmap = Bitmap.createScaledBitmap(bm, 70, 70, true);
        Bitmap mergeIcon=mergeBitmap(mContext,mBitmap,bitmap);

        BitmapDrawable bd=new BitmapDrawable(mergeIcon);
        return bd;
    }
	
	public static boolean setChmod(String path)
	{
          if (Build.VERSION.SDK_INT > 16) {
              Process p = null;
              try {
                  p = Runtime.getRuntime().exec("chmod -R 777 " + path);
                  int status = p.waitFor();
                  if (status == 0) {
                      return true;
                  } else {
                      return false;
                  }
              } catch(IOException e)
              {
              } catch (InterruptedException e) {
              }
          }
          return false;
	}
    
}