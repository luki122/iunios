/*
 *
 * Copyright (C) 20120601 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.android.phone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.os.storage.StorageManager;
import android.content.SharedPreferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.SystemProperties;
import com.android.phone.Constants;
import com.android.phone.R;
import android.util.Base64;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

//Gionee hanbj 20130708 added for amigo build begin
import gionee.os.storage.GnStorageManager;
//Gionee hanbj 20130708 added for amigo build end

public class GnPhoneRecordHelper {
    private static final String LOG_TAG = "GnPhoneRecordHelper";
    public static final String Z_KUOHAO = "(";
    public static final String F_KUOHAO = ")";
    private static final long DEFAULT_RECORD_SIZE = Constants.PHONE_RECORD_LOW_STORAGE_THRESHOLD;
    public static String gnRecordPrefixPath = null;
    public static final String RECORD_SDCARD_PATH = "RECORD_SDCARD_PATH";
    public static final String RECORD_PATH_KEY = "RECORD_PATH_KEY";
    private static final String REG = "[\\\\/:*?\"<>|]+";
    private static final boolean gnOverseaProduct = SystemProperties.get("ro.gn.oversea.product").equals("yes");
    public static final String SDCARD_DEFAULT_PATH = Environment.getExternalStorageDirectory().getPath();
    
    public static String getSecondPath(Context context) {
        StorageManager sManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = sManager.getVolumePaths();
        for (int i=0; i< paths.length; i++) {
            if (!paths[i].equals(SDCARD_DEFAULT_PATH)) {
                return paths[i];
            }
        }
        return SDCARD_DEFAULT_PATH;
    }

    public static String getSDCardPath(Context context) {
//        // Gionee fangbin 20121231 removed for CR00756041 start
//        //gnRecordPrefixPath = null;
//        // Gionee fangbin 20121231 removed for CR00756041 end
//        String sdCardDir = null;
//        StorageManager sManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//        boolean sdcardExit = sManager.getVolumeState(SDCARD_DEFAULT_PATH).equals(Environment.MEDIA_MOUNTED) || sManager.getVolumeState(getSecondPath(context)).equals(Environment.MEDIA_MOUNTED);
//        if(sdcardExit){
//            //Gionee hanbj 20130708 modified for amigo build begin
//            sdCardDir = GnStorageManager.getGnAvailableExternalStoragePath(DEFAULT_RECORD_SIZE);
//            //Gionee hanbj 20130708 modified for amigo build end
//        }
//        sManager = null;
//        return sdCardDir;
    	return  GnStorageManager.getInstance(PhoneGlobals.getInstance()).getSdCardPath(0);
    }
    
    public static String getRecordPath(Context context) {
//        String path = context.getResources().getString(R.string.gn_record) + "/" + 
//                        context.getResources().getString(R.string.gn_phone_record) + "/";
      String path = context.getResources().getString(R.string.gn_phone_record) + "/";
//        if (gnOverseaProduct) {
//            path = context.getResources().getString(R.string.zzzzz_gn_record) + "/" + 
//                            context.getResources().getString(R.string.zzzzz_gn_phone_record) + "/";
//        }
        String sdPath = getSDCardPath(context);
        if (null != sdPath) {
            path = sdPath + "/" + path;
        } else {
            return null;
        }
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return path;
    }
    
    
    public static String getPrivateRecordPath(Context context, long PrivateId) {
    
        String privacyCallRecodingEncodePath = AuroraPrivacyUtils.getPrivateHomePath(PrivateId) + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);

    	privacyCallRecodingEncodePath = replaceBlank(privacyCallRecodingEncodePath); //必须去掉特殊字符
    	return privacyCallRecodingEncodePath;
    }
    
	
	private static String replaceBlank(String str) {
	    String dest = "";
	    if (str != null) {
	        dest = str.replace("\n", "");
	    }
	    return dest;
	}
	    

    public static String getRecordFileName(Context context, String name, String number) {
        String fileName = "";
        String dirPath = getRecordPath(context);
        if (null != dirPath) {
            try {
                File dirFile = new File(dirPath);
                File[] files = dirFile.listFiles();
                if (hasInvalidateCharacter(name)) {
                    name = number;
                }
                ArrayList<String> fileNames = getSamePersonRecordNames(context, files, name);
                int count = fileNames.size();
                String tempStr = "";
                for (int i=0; i<count; i++) {
                    tempStr = convertNum(i+1);
                    if (!fileNames.contains(tempStr)) {
                        break;
                    } else {
                        fileNames.remove(tempStr);
                    }
                }
                if (tempStr.equals("") || fileNames.size() == 0) {
                    tempStr = convertNum(count+1);
                }
                String prefix = context.getResources().getString(R.string.gn_phone_record);
                fileName = formatRecordFileName(prefix, tempStr, name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
    
//    hi，光宇。 通话录音的文件命名，采用这样的格式：201403241355_075588886666(1)  用来区分同一个通话中的多次录音。
//    public static String getAuroraRecordFileName(Context context, String name, String number) {
//        String fileName = "";
//        String dirPath = getRecordPath(context);
//        if (null != dirPath) {
//            File dirFile = new File(dirPath);
//            File[] files = dirFile.listFiles();
//            SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyyMMddHHmm");  
//            String   date   =   sDateFormat.format(new   java.util.Date());
//            String prefix = date + "_" + number;
//            ArrayList<String> fileNames = getAuroraSamePersonRecordNames(context, files, prefix);
//            int count = fileNames.size();
//            String tempStr = "";
//            for (int i=0; i<count; i++) {
//                tempStr = Integer.toString(i+1);
//                if (!fileNames.contains(tempStr)) {
//                    break;
//                } else {
//                    fileNames.remove(tempStr);
//                }
//            }
//            if (tempStr.equals("") || fileNames.size() == 0) {
//                tempStr = Integer.toString(count+1);
//            }        	        	        	        
//            fileName = prefix +  Z_KUOHAO + tempStr + F_KUOHAO;
//        }
//        return fileName;
//    }
    
    public static String getAuroraRecordFileName(Context context, String number) {
        String fileName = "";
        String dirPath = getRecordPath(context);
        if (null != dirPath) {
            SimpleDateFormat   sDateFormat   =   new   SimpleDateFormat("yyyyMMddHHmm");    
            String   date   =   sDateFormat.format(new   java.util.Date());
            fileName = date + "_" + number;   	        	        	        
        }
        return fileName;
    }
    
    private static ArrayList<String> getSamePersonRecordNames (Context context, File[] files, String name) {
        ArrayList<String> recordNames = new ArrayList<String>();
        if (null != files && files.length > 0) {
            String str = "";
            String fileName = "";
            String prefix = context.getResources().getString(R.string.gn_phone_record);
            for (int i=0; i<files.length; i++) {
                fileName = files[i].getName();
                if (fileName.contains(Z_KUOHAO)) {
                    str = fileName.substring(fileName.indexOf(Z_KUOHAO)+1, fileName.lastIndexOf(F_KUOHAO));
                    if (str.equals(name)) {
                        fileName = fileName.substring(0, fileName.indexOf(Z_KUOHAO));
                        fileName = fileName.replace(prefix, "");
                        recordNames.add(fileName);
                    }
                }
            }
        }
        return recordNames;
    }
    
    private static ArrayList<String> getAuroraSamePersonRecordNames (Context context, File[] files, String prefix) {
        ArrayList<String> recordNames = new ArrayList<String>();
        if (null != files && files.length > 0) {
            String str = "";
            String fileName = "";
            for (int i=0; i<files.length; i++) {
                fileName = files[i].getName();
                if (fileName.contains("_") && fileName.contains(Z_KUOHAO) && fileName.contains(F_KUOHAO)) {
                    str = fileName.substring(0, fileName.lastIndexOf(Z_KUOHAO));
                    if (str.equals(prefix)) {
                        fileName = fileName.substring(fileName.indexOf(Z_KUOHAO)+1, fileName.indexOf(F_KUOHAO));
                        recordNames.add(fileName);
                    }
                }
            }
        }
        return recordNames;
    }
    
    private static String formatRecordFileName(String prefix , String num, String name) {
        return prefix + num + Z_KUOHAO + name + F_KUOHAO;
    }
    
    private static String convertNum(int num) {
        String str = "";
        if (num <= 0) {
            num = 1;
        } else if (num < 10) {
            str = "00" + num;
        } else if (num < 100) {
            str = "0" + num;
        } else {
            str = String.valueOf(num);
        }
        return str;
    }
    
    public static String getLastSDCardPath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(RECORD_SDCARD_PATH, Context.MODE_PRIVATE);
        // gionee xuhz 20130312 modify for CR00778005 start
        //old: return sp.getString(RECORD_PATH_KEY, null);
        return sp.getString(RECORD_PATH_KEY, SDCARD_DEFAULT_PATH);
        // gionee xuhz 20130312 modify for CR00778005 end
    }
    
    public static void setLastSDCardPath(Context context, String value) {
        SharedPreferences sp = context.getSharedPreferences(RECORD_SDCARD_PATH, Context.MODE_PRIVATE);
        sp.edit().putString(RECORD_PATH_KEY, value).commit();
    }
    
    public static boolean hasExternalSdcardStorage(Context context) {
        StorageManager sManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        return sManager.getVolumeState(SDCARD_DEFAULT_PATH).equals(Environment.MEDIA_MOUNTED);
    }
    
    private static boolean hasInvalidateCharacter(String name) {
        Pattern pattern = Pattern.compile(REG);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    // Gionee fangbin 20121231 added for CR00756041 start
    public static void reSetRecorderPrefixPath() {
        gnRecordPrefixPath = null;
    }
    // Gionee fangbin 20121231 added for CR00756041 end
    
    //aurora add liguangyu 20140419 for BUG #4384 start
    public static void copyFileContent(File af ,File bf) {
  	  FileInputStream is = null;
  	  FileOutputStream os = null;
  	  if(!bf.exists()){
	  	   try {
	  		   bf.createNewFile();
	  	   } catch (IOException e) {
	  		   e.printStackTrace();
	  	   }
  	  }
  	  try {
	  	   is = new FileInputStream(af);
	  	   os = new FileOutputStream(bf);
	  	   byte b[] = new byte[1024];
	  	   int len;
	  	   try {
		  	    len = is.read(b);
		  	    while (len != -1) {
		  	     os.write(b, 0, len);
		  	     len = is.read(b);
		  	    }
	  	   } catch (IOException e) {
	  		   e.printStackTrace();
	  	   }
  	  } catch (FileNotFoundException e) {
  		  e.printStackTrace();
  	  }finally{
	  	   try {
		  	    if(is != null) is.close();
		  	    if(os != null) os.close();
	  	   } catch (IOException e) {
	  		   e.printStackTrace();
	  	   }
  	  }
  	 }
    //aurora add liguangyu 20140419 for BUG #4384 end
    
    public static String changeAuroraRecordFileName(String name, String prefix) {
    	String fileName = name.substring(name.indexOf("_"));
    	fileName = prefix + fileName;
        return fileName;
    }
    
    public static long getPrivateId() {
    	Log.i("GnPhoneRecordHelper", "isPrivate");
    	CallManager cm = PhoneGlobals.getInstance().mCM;
        Call call = cm.getActiveFgCall();
        int phoneType = call.getPhone().getPhoneType();
        Connection conn = null;
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
              || (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
            conn = call.getEarliestConnection();
        } 
        if(conn != null) {
            Object o = conn.getUserData();
             if (o instanceof AuroraCallerInfo) {
            	 AuroraCallerInfo info = (AuroraCallerInfo) o;
            	 if(info.mPrivateId > 0) {
            	     Log.i("GnPhoneRecordHelper", " isPrivate true");            	     
            		 return info.mPrivateId;
            	 }
            }
        }
	    Log.i("GnPhoneRecordHelper", " isPrivate false");
        return 0;
    }
    
    public static boolean isExternalStorageMounted() {
        StorageManager storageManager = (StorageManager) PhoneGlobals.getInstance().getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            return false;
        }
//        if (PhoneApp.ISGNPHONE) {
            return storageManager.getVolumeState(GnPhoneRecordHelper.SDCARD_DEFAULT_PATH).equals(Environment.MEDIA_MOUNTED) || 
            storageManager.getVolumeState(GnPhoneRecordHelper.getSecondPath(PhoneGlobals.getInstance().getApplicationContext())).equals(Environment.MEDIA_MOUNTED);
//        } else {
//            String storageState = storageManager.getVolumeState(GnStorageManager.getDefaultPath());
//            return storageState.equals(Environment.MEDIA_MOUNTED) ? true : false;
//        }
    }
    
    public static boolean diskSpaceAvailable(long sizeAvailable) {
        return (getDiskAvailableSize() - sizeAvailable) > 0;
    }

    public static boolean diskSpaceAvailable(String defaultPath, long sizeAvailable) {
        if (null == defaultPath) {     
            return diskSpaceAvailable(sizeAvailable);
        } else {
            File sdCardDirectory = new File(defaultPath);
            StatFs statfs;
            try {
                if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                    statfs = new StatFs(sdCardDirectory.getPath());
                } else {
                    log("-----diskSpaceAvailable: sdCardDirectory is null----");
                    return false;
                }
            } catch (IllegalArgumentException e) {
                log("-----diskSpaceAvailable: IllegalArgumentException----");
                return false;
            }
            long blockSize = statfs.getBlockSize();
            long availBlocks = statfs.getAvailableBlocks();
            long totalSize = blockSize * availBlocks;
            return (totalSize - sizeAvailable) > 0;
        }
    } 
    
    public static long getDiskAvailableSize() {
        StorageManager storageManager = (StorageManager) PhoneGlobals.getInstance().getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            log("-----story manager is null----");
            return -1;
        }
        File sdCardDirectory = new File(GnStorageManager.getDefaultPath());
        StatFs statfs;
        try {
            if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                statfs = new StatFs(sdCardDirectory.getPath());
            } else {
                log("-----diskSpaceAvailable: sdCardDirectory is null----");
                return -1;
            }
        } catch (IllegalArgumentException e) {
            log("-----diskSpaceAvailable: IllegalArgumentException----");
            return -1;
        }
        long blockSize = statfs.getBlockSize();
        long availBlocks = statfs.getAvailableBlocks();
        long totalSize = blockSize * availBlocks;
        return totalSize;
    }
    
    public static String getExternalStorageDefaultPath() {
        StorageManager storageManager = (StorageManager) PhoneGlobals.getInstance().getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            log("-----story manager is null----");
            return null;
        }
        return GnStorageManager.getDefaultPath();
    }
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
