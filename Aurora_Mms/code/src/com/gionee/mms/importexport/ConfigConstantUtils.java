/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;
import aurora.preference.AuroraPreferenceManager;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessagingPreferenceActivity;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;
import android.os.StatFs;
import java.io.FileOutputStream;
//gionee zhouyj 2012-08-07 add for CR00667794 start 
import android.os.storage.StorageManager;
import gionee.os.storage.GnStorageManager;
//gionee zhouyj 2012-08-07 add for CR00667794 end 

public class ConfigConstantUtils {
    public static final int IMPORT_FILE_READY = 0;
    public static final int EXPORT_COMPLETE_CODE = 1;
    public static final int IMPORT_COMPLETE_CODE = 2;
    public static final int IMPORT_FILE_ERROR = 3;
    public static final int IMPORT_FILE_NOT_EXISTS = 4;
    public static final int EXPORT_FILE_READY = 5;
    public static final int IMPORT_FILE_PATH_ERROR = 6;
    public static final int NO_SDCARD = 7;
    public static final int OTHER_ERROR = 8;
    public static final int IMPORT_FILE_EMPTY = 9;
    public static final int EXPORT_ITEM_COMPLETE_CODE = 10;

    public static final String DATE_FIRST_TYPE = "yyyy-MM-dd_HHmmss";
    public static final String DATE_SECOND_TYPE = "yyyy-MM-dd HH:mm:ss";

    public static final Uri IMPORT_EXPORT_SMS_QUERY_URI = Uri.parse("content://sms");
    public static final Uri IMPORT_SMS_URI = Uri.parse("content://sms/import");
    public static final String SERVICE_ACTION ="android.intent.action.ImportExportService";
    public static final String SDCARD_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String IMPORT_EXPORT_SMS_FILE_EXTENSION = ".txt";
    public static final String IMPORT_EXPORT_SMS_SEPARATOR_TAB_ONE = "\t";
    public static final String IMPORT_EXPORT_SMS_SEPARATOR_TAB_TWO = "\t\t";
    public static final String IMPORT_EXPORT_SMS_PRE = "ImportExportSms";
    public static final String DATE_THIRD_TYPE = "yyyy-MM-dd";
    public static final String DATE_FOURTH_TYPE = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FIFTH_TYPE = "dd/MM/yyyy";
    public static final String SDCARD = "sdcard";
    public static final String SDCARD2 = "sdcard2";
    public static final int SDCARD_NO_SPACE = 11;
    public static final int EXPORT_SMS_CANCEL = 12;
    public static final int IMPORT_SMS_CANCEL = 13;
    public static final int EXPORT_SMS_FAILED = 14;
    public static final int IMPORT_SMS_FAILED = 15;
    public static final String EXPORT_SMS_ACTION = "android.intent.action.MultiExportSmsActivity";
    public static final String IMPORT_SMS_ACTION = "android.intent.action.MultiImportSmsActivity";
    public static final Uri IMPORT_SMS_CANCEL_URI = Uri.parse("content://sms/cancel_import");
    // gionee zhouyj 2012-08-27 add for CR00667581 start
    public static final Uri IMPORT_SMS_CHANGE_MODE = Uri.parse("content://sms/import_change_mode");
    // gionee zhouyj 2012-08-27 add for CR00667581 end

    public static boolean hasSdcard() {
        boolean flag = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        return flag;
    }
    
    //Gionee guoyangxu 20120621 modified for CR00625902 begin
    public static boolean hasSdcardShared() {
        boolean flag = Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED);
        return flag;
    }
    //Gionee guoyangxu 20120621 modified for CR00625902 end

    public static String formatDate2String(String type, long seconds) {
        String mTime = null;
        SimpleDateFormat mFormat = new SimpleDateFormat(type);
        Date mDate = new Date(seconds);
        mTime = mFormat.format(mDate);
        return mTime;
    }

    public static long formatDate2Long(String type, String date) {
        long mTime = 0l;
        SimpleDateFormat mFormat = new SimpleDateFormat(type);
        Date mDate = null;
        try {
            mDate = mFormat.parse(date);
            mTime = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mTime;
    }

    public static void deleteSmsFile(String filePath) {
        if (null != filePath) {
            File mFile = new File(filePath);
            if (mFile.exists() && mFile.isFile()) {
                mFile.delete();
            }
        }
    }

    //MessagingPreferenceActivity.AUTO_DELETE
    public static int getMessageLimit(Context context) {
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        boolean flag = prefs.getBoolean(MessagingPreferenceActivity.AUTO_DELETE, false);
        if (flag) {
            return prefs.getInt("MaxSmsMessagesPerThread",
                    MmsConfig.getDefaultSMSMessagesPerThread());
        } else {
            return 0;
        }
    }

    public static String getLocalLanguage() {
       return Locale.getDefault().getLanguage();
    }

    public static String getLocalDateStr(String formatFrom, String formatTo, String date) {
        String localDate = "";
        localDate = formatDate2String(formatTo, formatDate2Long(formatFrom, date));
        return localDate;
    }

    public static boolean isAvaiableSpace(long size){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
           String sdcard = Environment.getExternalStorageDirectory().getPath();
           StatFs statFs = new StatFs(sdcard);
           long blockSize = statFs.getBlockSize();
           long blocks = statFs.getAvailableBlocks();
           long availableSpare = blocks*blockSize;
           if(size > availableSpare){
               return false;
           }else{
               return true;
           }
        }
        return false;
    }
    
    // gionee zhouyj 2012-08-07 add for CR00667794 start 
    public static boolean isAvaiableSpace(long size, String sdcard, Context context){
          // Aurora liugj 2013-11-12 modified for bug-554 start
        //StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        boolean state = Environment.MEDIA_MOUNTED.equals(GnStorageManager.getVolumeState(sdcard));
          // Aurora liugj 2013-11-12 modified for bug-554 end
        if (state) {
           StatFs statFs = new StatFs(sdcard);
           long blockSize = statFs.getBlockSize();
           long blocks = statFs.getAvailableBlocks();
           long availableSpare = blocks*blockSize;
           //Gionee yuanqingqing 20121009 modify for CR00690011 begin
           if(size > availableSpare || availableSpare <=0){
            //Gionee yuanqingqing 20121009 modify for CR00690011 end
               return false;
           }else{
               return true;
           }
        }
        return false;
    }
    // gionee zhouyj 2012-08-07 add for CR00667794 end 

    public static void deleteEmptySmsFile(String filePath) {
        if (null != filePath) {
            File mFile = new File(filePath);
            if (mFile.exists() && mFile.isFile() && mFile.length() <= 0) {
                mFile.delete();
            }
        }
    }

    public static void addUTF8Header(File file) {
        if (null != file && file.exists()) {
            //UTF-8格式头EFBBBF
            byte b0 = Byte.decode("0xE").byteValue();   //得到16进制E的byte值
            b0 = (byte) (b0 << 4);                      //将16进制E的byte值左移4位
            byte b1 = Byte.decode("0xF").byteValue();   //得到16进制F的byte值
            byte ef = (byte) (b0 | b1);                 //将左移4位后的E与F进行或操作

            byte b2 = Byte.decode("0xB").byteValue();
            b2 = (byte) (b2 << 4);
            byte b3 = Byte.decode("0xB").byteValue();
            byte bb = (byte) (b2 | b3);

            byte b4 = Byte.decode("0xB").byteValue();
            b4 = (byte) (b4 << 4);
            byte b5 = Byte.decode("0xF").byteValue();
            byte bf = (byte) (b4 | b5);

            byte[] utf8Header = new byte[3];
            utf8Header[0] = ef;
            utf8Header[1] = bb;
            utf8Header[2] = bf;

            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(utf8Header);
                outputStream.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }
    
    // gionee zhouyj 2012-06-30 add for CR00632426 start 
    public static String removeUTF8Header(String fileHeaderString) {
        try {
            byte[] byteSrc = fileHeaderString.getBytes("UTF-8");
            byte[] byteDst = new byte[byteSrc.length - 3];
            String hexStr1 = Integer.toHexString(byteSrc[0]);
            hexStr1 = hexStr1.substring(hexStr1.length() -2);
            String hexStr2 = Integer.toHexString(byteSrc[1]);
            hexStr2 = hexStr2.substring(hexStr2.length() -2);
            String hexStr3 = Integer.toHexString(byteSrc[2]);
            hexStr3 = hexStr3.substring(hexStr3.length() -2);
            
            if ((hexStr1.equals("EF") && hexStr2.equals("BB") && hexStr3.equals("BF")) || 
                    (hexStr1.equals("ef") && hexStr2.equals("bb") && hexStr3.equals("bf"))) {
                // UTF-8格式头EFBBBF
                System.arraycopy(byteSrc, 3, byteDst, 0, byteSrc.length - 3);
                return new String(byteDst, "UTF-8");
            } else {
                return fileHeaderString;
            }
        } catch (Exception e) {
            // TODO: handle exception
            return fileHeaderString;
        }
    }
    // gionee zhouyj 2012-06-30 add for CR00632426 end 
}
