/*
 * @author zw 
 */
package com.aurora.note.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;


public class SDcardManager {
	

    public static boolean checkSDCardMount()
    {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else
        {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (mExternalStorageAvailable && mExternalStorageWriteable)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
   
    public static boolean checkSDCardAvailableSize() {
    	 
		
		//取得SD卡文件路径   
	     File path = Environment.getExternalStorageDirectory();   
	     StatFs sf = new StatFs(path.getPath());   
	     //获取单个数据块的大小(Byte)   
	     long blockSize = sf.getBlockSize();   
	     //空闲的数据块的数量   
	     long freeBlocks = sf.getAvailableBlocks();  
	     //返回SD卡空闲大小   
	     //return freeBlocks * blockSize;  //单位Byte   
	     //return (freeBlocks * blockSize)/1024;   //单位KB 
	     
	     
	     if((freeBlocks * blockSize)/1024 /1024 > 20)
	    	 return true;
	     else
	    	 return false; //单位MB  
}
    		
   public static boolean checkSDCardAvailable(){
	   String state = Environment.getExternalStorageState();
	   if (Environment.MEDIA_MOUNTED.equals(state))
       {
           return true;
       }else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
    	   return true;
       }else{
    	   return false;
       }
   }
  
}
