package gn.com.android.update.utils;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.NoSpaceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import java.nio.*;
import java.nio.channels.FileChannel;

import android.content.Context;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final String OTA_FILE_SUFFIX = ".zip";

    public static boolean deleteFileIfExists(File file) {
        if (file == null) {
            LogUtils.loge(TAG, "deleteFileIfExists() file is null ");
            return false;
        }

        LogUtils.logd(TAG, "deleteFile() path = " + file.getPath());

        if (file.exists()) {
            LogUtils.loge(TAG, "deleteFileIfExists() file exists ");
            file.delete();
            return true;
        }
        return false;
    }

    public static boolean verifyFileByMd5(File file, String md5) {
        if (file == null || md5 == null) {
            LogUtils.loge(TAG, "verifyFileByMd5() IllegalArgumentException");
            return false;
        }

        try {
            String fileMd5 = getFileMd5(file);

            if (md5.equals(fileMd5)) {
                LogUtils.logd(TAG, "verifyFileByMd5() successful");
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogUtils.loge(TAG, "verifyFileByMd5() failed");
        return false;
    }

    public static String getFileMd5(File file) throws FileNotFoundException {
        byte[] digest = null;
        FileInputStream in = null;

        try {

            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            in = new FileInputStream(file);
            int byteCount;

            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }

            digest = digester.digest();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (Exception cause) {
            throw new RuntimeException("Unable to compute MD5 of \"" + file + "\"", cause);
        } finally {
            closeInputStream(in);
        }
        return (digest == null) ? "" : byteArrayToHexString(digest);
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toHexString((b >> 4) & 0xf));
            result.append(Integer.toHexString(b & 0xf));
        }
        return result.toString();
    }

    public static void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                LogUtils.loge(TAG, "closeInputStream() e = " + e.toString());
            }

        }
    }

    public static void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                LogUtils.loge(TAG, "closeOutputStream() e = " + e.toString());
            }

        }
    }

    public static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (IOException e) {
                LogUtils.loge(TAG, "closeReader() e = " + e.toString());
            }
        }
    }

    public static String addSeparatorToPath(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }

        return path;
    }

    /*get ota file name ,like /xxxxx.zip
     * 
     * @param md5 the md5 of ota file
     * @return ota file name without storage path ,like /xxxxxxxx.zip
     * */
    public static String getOtaFileNameWithoutStoragePath(String md5) {
        String fileName = File.separator + md5 + OTA_FILE_SUFFIX;
        return fileName;
    }

    /*get already downloaded file 
     * 
     * @param context
     * @param fileNameWithoutStoragePath the file name you want to download ,
     *        notify the name should without storage path
     * 
     * 
     * @return if there is a file exists, return the file ; if no ,return null
     * */
    public static File getAlreadyDownloadedFile(Context context, String fileNameWithoutStoragePath) {
        List<String> storagePaths = StorageUtil.getAllMountedStorageVolumesPath(context);

        List<File> downloadedFiles = getAllDownloadedFile(storagePaths, fileNameWithoutStoragePath);

        return getBiggerFile(downloadedFiles);
    }

    private static List<File> getAllDownloadedFile(List<String> storagePaths,
            String fileNameWithoutStoragePath) {
        List<File> downloadedFiles = new ArrayList<File>();

        for (String storage : storagePaths) {
            File file = new File(storage + fileNameWithoutStoragePath);
            if (file.exists()) {
                downloadedFiles.add(file);
            }
        }

        LogUtils.logd(TAG, "getAllDownloadedFile() downloadedFiles is " + downloadedFiles);
        return downloadedFiles;
    }

    private static File getBiggerFile(List<File> files) {
        if (files.size() == 0) {
            return null;
        }

        File rightFile = files.get(0);

        for (int i = 0; i < files.size() && (i - 1) > 0; i++) {
            File file1 = files.get(i);
            File file2 = files.get(i - 1);

            if (file1.length() > file2.length()) {
                rightFile = file1;
            } else {
                rightFile = file2;
            }
        }

        LogUtils.logd(TAG, "getBiggerFile() file is " + rightFile.getPath());
        return rightFile;
    }

    public static long getFileLength(File file) {
        if (file.exists()) {
            return file.length();
        } else {
            return 0;
        }
    }
    
  public static long getFileLengthByDownloadId(Context context,long downloadId) {
	  if(downloadId == Config.ERROR_DOWNLOAD_ID){
		  return 0;
	  }
	  return CursorUtil.getCurrentBytesById(context, downloadId);
    }
  
  public static long getFileLengthByUrl(Context context,String url){
  	return getFileLengthByDownloadId(context, CursorUtil.getDownloadIdByUrl(context, url));
  }

    public static boolean makeParentFile(File file) {
        File parentFile = file.getParentFile();
        if (!file.exists()) {
            return parentFile.mkdirs();
        }
        return false;
    }

    public static String getDownloadFilePath(Context context, String fileNameWithoutStoragePath,
            int totalLength) throws NoSpaceException {
    	LogUtils.logd(TAG, "android.os.Build.VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        int needLength = totalLength + Config.MIN_STORAGE_SPACE;
        File file = FileUtil.getAlreadyDownloadedFile(context, fileNameWithoutStoragePath);

        if (file != null &&file.exists()) {
            String storagePath = StorageUtil.getStroageOfFile(file);

            long storageSpace = StorageUtil.getAppointedStorageAvailableSpace(context, storagePath);
            if(android.os.Build.VERSION.SDK_INT  >= 21){
            	return file.getPath();
            }
            if (storageSpace > needLength - file.length()) {
                return file.getPath();
            }
            throw new NoSpaceException(true, storagePath);
        } else {
            List<String> storages = StorageUtil.getAllMountedStorageVolumesPath(context);

            if (storages.size() > 0) {
                for (String storage : storages) {
                    if (StorageUtil.getAppointedStorageAvailableSpace(context, storage) > needLength) {
                        String filePath = storage + fileNameWithoutStoragePath;
                        LogUtils.logd(TAG, "getDownloadFilePath() filePath = " + filePath);
                        return filePath;
                    }
                }
            }
            throw new NoSpaceException(false, null);
        }
    }

}
