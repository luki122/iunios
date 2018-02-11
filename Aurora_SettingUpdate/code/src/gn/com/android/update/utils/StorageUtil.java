package gn.com.android.update.utils;

import gn.com.android.update.business.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.RemoteException;
import android.util.Log;

public class StorageUtil {
    private static final String TAG = "StorageUtil";
    private static final String[] STORAGE_NAME = {"/storage/sdcard", "/mnt/sdcard", "/sdcard"};
    private static final int DEFAULT_INTERNAL_STORAGE_INDEX = 1;
/*
 * 各分区的挂载状态
 */
    public static String getStorageVolumeState(Context context, String mountPoint) {
        String state = Environment.MEDIA_REMOVED;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager != null) {
            state = storageManager.getVolumeState(mountPoint);
        }

        LogUtils.logd(TAG, "getStorageVolumeState() mountPoint = " + mountPoint + " , state = " + state);
        return state;
    }

    /*return AvailableSpace of AppointedStorage,
     * Unit is kb
     * 
     * */
    public static long getAppointedStorageAvailableSpace(Context context, String mountPoint) {
        if (Environment.MEDIA_MOUNTED.equals(getStorageVolumeState(context, mountPoint))) {

            StatFs sf = new StatFs(mountPoint);
            long length = ((long) sf.getBlockSize()) * sf.getAvailableBlocks();
            LogUtils.logd(TAG, "getAppointedStorageAvailableSpace() length = " + length);
            return length;

        }

        return 0;
    }

    public static List<String> getStorageVolumesPath(Context context) {
        List<String> storages = new ArrayList<String>();

        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return storages;
        }

        String[] paths = storageManager.getVolumePaths();

        if (paths == null) {
            return storages;
        }

        for (String path : paths) {
            //if (isRightStorageName(path)) {
                storages.add(path);
            //}
        }

        LogUtils.logd(TAG, "getStorageVolumesPath() storages = " + storages.toString());
        return storages;
    }

    public static boolean isRightStorageName(String mountPoint) {
        for (String storage : STORAGE_NAME) {
            if (mountPoint.startsWith(storage)) {
                return true;
            }
        }

        return false;
    }

    public static List<String> getAllMountedStorageVolumesPath(Context context) {
    	LogUtils.log(TAG, "Run getAllMountedStorageVolumesPath ");
        List<String> mountedPaths = new ArrayList<String>();
        List<String> paths = getStorageVolumesPath(context);

        for (String path : paths) {
            if (Environment.MEDIA_MOUNTED.equals(getStorageVolumeState(context, path))) {
                mountedPaths.add(path);
            }
        }
        return mountedPaths;
    }

    public static String getExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getExternalStorageState() {
        return Environment.getExternalStorageState();
    }

    public static boolean checkExternalStorageMounted() {
        boolean flag = Environment.MEDIA_MOUNTED.equals(getExternalStorageState());
        LogUtils.logd(TAG, "checkExternalStorageMounted()  " + flag);

        return flag;
    }

    public static long getExternalStorageAvailableSpace(Context context) {
        return getAppointedStorageAvailableSpace(context, getExternalStoragePath());
    }

    public static String getInternalStoragePath(Context context) {
    	List<String> mountPoints = getAllMountedStorageVolumesPath(context);

        if (mountPoints.size() > 1) {
            return mountPoints.get(DEFAULT_INTERNAL_STORAGE_INDEX);
        }

        return getExternalStoragePath();
    }

    public static String getInternalStorageState(Context context) {

        return getStorageVolumeState(context, getInternalStoragePath(context));
    }

    public static boolean checkInternalStorageMounted(Context context) {
        boolean flag = Environment.MEDIA_MOUNTED.equals(getInternalStorageState(context));
        LogUtils.logd(TAG, "checkInternalStorageMounted()  " + flag);

        return flag;
    }

    public static long getInternalStorageAvailableSpace(Context context) {
    	return getAppointedStorageAvailableSpace(context, getInternalStoragePath(context));
    }

    public static boolean isExSdcardInserted(Context context) {
        return getAllMountedStorageVolumesPath(context).size() > 1;
    }

    public static String getStroageOfFile(File file) {
        String filePath = file.getPath();

        /*int secondSeparatorIndex = filePath.indexOf(File.separator, 1);

        if (secondSeparatorIndex == -1) {
            throw new IllegalArgumentException("wrong filePath");
        }

        int thirdSeparatorIndex = filePath.indexOf(File.separator, secondSeparatorIndex + 1);

        if (thirdSeparatorIndex == -1) {
            throw new IllegalArgumentException("wrong filePath");
        }

        String storage = filePath.substring(0, thirdSeparatorIndex);*/

        int lastSeparatorIndex = filePath.lastIndexOf(File.separator);
        String storage = filePath.substring(0, lastSeparatorIndex);

        LogUtils.logd(TAG, "getStroageOfFile() storage = " + storage);
        return storage;
    }

    public static boolean hasEnoughSpaceForDownload(Context context, String fileNameWithoutStoragePath,
            int totalLength) {

        long storageSpace = 0;
        int needLength = totalLength + Config.MIN_STORAGE_SPACE;
        File file = FileUtil.getAlreadyDownloadedFile(context, fileNameWithoutStoragePath);
        LogUtils.logd(TAG, "hasEnoughSpaceForDownload --> android.os.Build.VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        if (file != null) {
            String storagePath = getStroageOfFile(file);

            storageSpace = getAppointedStorageAvailableSpace(context, storagePath);
           
            if(android.os.Build.VERSION.SDK_INT  >= 21){
            	 return true;
            }
            if (storageSpace > needLength - file.length()) {
                return true;
            }

        } else {

            List<String> storages = getAllMountedStorageVolumesPath(context);

            if (storages.size() > 0) {
                for (String storage : storages) {
                    if (getAppointedStorageAvailableSpace(context, storage) > needLength) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isInternalStorage(Context context, String storagePath) {
        LogUtils.logd(TAG, "isInternalStorage() storagePath = " + storagePath);

        boolean flag;
        if (isExSdcardInserted(context) && getInternalStoragePath(context).equals(storagePath)) {
            flag = false;
        } else {
            flag = true;
        }

        LogUtils.logd(TAG, "isFileInInternalStoarge() " + flag);
        return flag;
    }

    public static boolean isFileInInternalStoarge(Context context, File file) {
        LogUtils.logd(TAG, "isFileInInternalStoarge() file path = " + file.getPath());
        String storagePath = getStroageOfFile(file);

        return isInternalStorage(context, storagePath);
    }

    public static boolean isNoStorageMounted(Context context) {
        if (getAllMountedStorageVolumesPath(context).size() > 0) {
            return false;
        } else {
            return true;
        }
    }
}
