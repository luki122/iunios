//Gionee <duansw><2013-3-20> modify for CR00789329 begin
package com.android.providers.downloads.util;

/**
 * create by gionee duansw.
 * date: 2013.3.13
 */
import java.io.File;
import java.io.IOException;

import android.os.StatFs;

public abstract class GnStorage {

    private static final String TAG = "GnStorage";

    public static final int INTERNAL_STORAGE_ID = 1;
    public static final int SDCARD_STORAGE_ID = 2;

    public static final String FILE_HEAD = "file://";

    public static  String SDCARD_DIR = "/sdcard";
    public static  String MNT_SDCARD_DIR = "/mnt/sdcard";
    public static  String MNT_SDCARD2_DIR = "/mnt/sdcard2";
    public static  String STORAGE_SDCARD0_DIR = "/storage/sdcard0";
    public static  String STORAGE_SDCARD1_DIR = "/storage/sdcard1";

    /**
	 * @param sDCARD_DIR the sDCARD_DIR to set
	 */
	public static void setSDCARD_DIR(String sDCARD_DIR) {
		SDCARD_DIR = sDCARD_DIR;
	}

	/**
	 * @param mNT_SDCARD_DIR the mNT_SDCARD_DIR to set
	 */
	public static void setMNT_SDCARD_DIR(String mNT_SDCARD_DIR) {
		MNT_SDCARD_DIR = mNT_SDCARD_DIR;
	}

	/**
	 * @param mNT_SDCARD2_DIR the mNT_SDCARD2_DIR to set
	 */
	public static void setMNT_SDCARD2_DIR(String mNT_SDCARD2_DIR) {
		MNT_SDCARD2_DIR = mNT_SDCARD2_DIR;
	}

	/**
	 * @param sTORAGE_SDCARD0_DIR the sTORAGE_SDCARD0_DIR to set
	 */
	public static void setSTORAGE_SDCARD0_DIR(String sTORAGE_SDCARD0_DIR) {
		STORAGE_SDCARD0_DIR = sTORAGE_SDCARD0_DIR;
	}

	/**
	 * @param sTORAGE_SDCARD1_DIR the sTORAGE_SDCARD1_DIR to set
	 */
	public static void setSTORAGE_SDCARD1_DIR(String sTORAGE_SDCARD1_DIR) {
		STORAGE_SDCARD1_DIR = sTORAGE_SDCARD1_DIR;
	}

	public static final long MIN_RAMAINING_SPACE = 2 * 1024 * 1024;

    public static final boolean checkStorageId(int id) {
        return id == INTERNAL_STORAGE_ID || id == SDCARD_STORAGE_ID;
    }

    public static final GnStorage[] getStorages() {
        return new GnStorage[] {new InternalStorage(), new SDCardStorage()};
    }

    public static final GnStorage getDefaultStorage() {
        GnStorage storage = new SDCardStorage();
        if (!storage.exist() || storage.isFull()) {
            storage = new InternalStorage();
        }
        return storage;
    }

    public static final GnStorage getStorage(String path) {
        GnStorage storage = new InternalStorage();
        if (!storage.isInThisStorage(path)) {
            storage = new SDCardStorage();
            if (!storage.isInThisStorage(path)) {
                storage = null;
            }
        }
        return storage;
    }

    public static final GnStorage getStorage(int storageId) {
        switch (storageId) {
            case INTERNAL_STORAGE_ID:
                return new InternalStorage();
            case SDCARD_STORAGE_ID:
                return new SDCardStorage();
            default:
                throw new RuntimeException("Unknow storage id.");
        }
    }


    public static final String getNowFileName(String path, int storageId) {
//    	Log.d(TAG, "path=="+path+"  storageId=="+storageId);
    	if(path==null){
    		return null;
    	}
        try {
            if (path.startsWith(FILE_HEAD)) {
                GnFile gnFile = getNowFile(path.substring(FILE_HEAD.length()), storageId);
                return FILE_HEAD + gnFile.mFile.getCanonicalPath();
            } else {
                GnFile gnFile = getNowFile(path, storageId);
                return gnFile.mFile.getCanonicalPath();
            }
        } catch (Exception e) {
        	e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return path;
    }

    /**
     * @param path
     * @param storageId
     * @return
     * @throws IllegalAccessException
     */
    public static final GnFile getNowFile(String path, int storageId) throws IllegalAccessException {
//        Log.d(TAG, Log.getThreadName() + ", id = " + storageId + ", path = " + path);
        if (path == null || path.isEmpty()) {
            Log.e(TAG, "No now file.");
            return null;
        }


        GnStorage storage = getStorage(storageId);
        String storageCanonicalPath = storage.getCanonicalPath();
//        Log.d(TAG, "storageCanonicalPath=="+storageCanonicalPath+" STORAGE_SDCARD0_DIR=="+STORAGE_SDCARD0_DIR+" STORAGE_SDCARD1_DIR=="+STORAGE_SDCARD1_DIR);
        if (storageCanonicalPath == null) {
            Log.d(TAG, "No now file.");
            return null;
        }
        File file = new File(path);
        String paramCanonicalPath = null;

        try {
            paramCanonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
        }

        if (paramCanonicalPath == null || paramCanonicalPath.isEmpty()) {
            Log.d(TAG, "No now file.");
            return null;
        }

        boolean isRightStorage = paramCanonicalPath.startsWith(storageCanonicalPath);
        if (!isRightStorage) {
            int length = storageCanonicalPath.length();
            paramCanonicalPath = storageCanonicalPath + paramCanonicalPath.substring(length);
           
        }

        return new GnFile(storage, new File(paramCanonicalPath));
    }

    public boolean isInThisStorage(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        if (path.startsWith(FILE_HEAD)) {
            path = path.substring(FILE_HEAD.length());
        }

        try {
            String paramCanonicalPath = new File(path).getCanonicalPath();
            return paramCanonicalPath.startsWith(getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getRemainedSize() {
        StatFs statFs = new StatFs(getCanonicalPath());
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    public long getTotalSize() {
        StatFs statFs = new StatFs(getCanonicalPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();

        return totalBlocks * blockSize;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GnStorage)) {
            return false;
        }

        GnStorage my = this;
        GnStorage you = (GnStorage) o;

        return my.getStorageID() == you.getStorageID();
    }

    @Override
    public int hashCode() {
        return getStorageID();
    }

    boolean storageExist(String path) {
        File file = new File(path);
        return file.exists() && file.canRead() && file.canWrite();
    }

    public boolean isFull() {
        return getRemainedSize() < MIN_RAMAINING_SPACE;
    }

    public boolean isFull(long minRemainingSpace) {
        return getRemainedSize() < minRemainingSpace;
    }

    public abstract String getCanonicalPath();

    public abstract boolean exist();

    public abstract int getStorageID();

    public abstract String getName();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(": id(");
        sb.append(getStorageID());
        sb.append("), exist(");
        sb.append(exist());
        sb.append(") CanonicalPath: ");
        sb.append(getCanonicalPath());
        return sb.toString();
    }

    /*public static class InternalStorage extends GnStorage {

        @Override
        public String getCanonicalPath() {
            if (!exist()) {
            	Log.d(TAG, "STORAGE_SDCARD1_DIR=="+STORAGE_SDCARD1_DIR);
                return STORAGE_SDCARD1_DIR;
            }

            File sdcard2File = new File(MNT_SDCARD2_DIR);
            Log.d(TAG, "MNT_SDCARD2_DIR=="+MNT_SDCARD2_DIR);
            if (sdcard2File.exists() && sdcard2File.canRead() && sdcard2File.canWrite()) {
                try {
                    return sdcard2File.getCanonicalPath();
                } catch (IOException e) {
                	   Log.d(TAG, "MNT_SDCARD2_DIR=="+MNT_SDCARD2_DIR);
                    return MNT_SDCARD2_DIR;
                }
            }

            File sdcardFile = new File(SDCARD_DIR);
            Log.d(TAG, "SDCARD_DIR=="+SDCARD_DIR);
            if (sdcardFile.exists() && sdcardFile.canRead() && sdcardFile.canWrite()) {
                try {
                    return sdcardFile.getCanonicalPath();
                } catch (IOException e) {
                    return SDCARD_DIR;
                }
            }

            return null;
        }

        @Override
        public boolean exist() {
            return storageExist(MNT_SDCARD_DIR) || storageExist(MNT_SDCARD2_DIR);
        }

        @Override
        public int getStorageID() {
            return INTERNAL_STORAGE_ID;
        }

        @Override
        public String getName() {
            return "Internal";
        }
    }*/
    
    public static class InternalStorage extends GnStorage {

        @Override
        public String getCanonicalPath() {
            if (!exist()) {
//            	Log.d(TAG, "STORAGE_SDCARD1_DIR=="+STORAGE_SDCARD0_DIR);
                return STORAGE_SDCARD0_DIR;
            }

            File sdcard2File = new File(STORAGE_SDCARD0_DIR);
//            Log.d(TAG, "STORAGE_SDCARD0_DIR=="+STORAGE_SDCARD0_DIR);
            if (sdcard2File.exists() && sdcard2File.canRead() && sdcard2File.canWrite()) {
                try {
                    return sdcard2File.getCanonicalPath();
                } catch (IOException e) {
                	   Log.d(TAG, "STORAGE_SDCARD0_DIR=="+STORAGE_SDCARD0_DIR);
                    return STORAGE_SDCARD0_DIR;
                }
            }

            File sdcardFile = new File(STORAGE_SDCARD1_DIR);
//            Log.d(TAG, "STORAGE_SDCARD1_DIR=="+STORAGE_SDCARD1_DIR);
            if (sdcardFile.exists() && sdcardFile.canRead() && sdcardFile.canWrite()) {
                try {
                    return sdcardFile.getCanonicalPath();
                } catch (IOException e) {
                    return STORAGE_SDCARD1_DIR;
                }
            }

            return null;
        }

        @Override
        public boolean exist() {
            return storageExist(STORAGE_SDCARD0_DIR) || storageExist(STORAGE_SDCARD1_DIR);
        }

        @Override
        public int getStorageID() {
            return INTERNAL_STORAGE_ID;
        }

        @Override
        public String getName() {
            return "Internal";
        }
    }

    public static class SDCardStorage extends GnStorage {

        @Override
        public String getCanonicalPath() {
            if (!exist()) {
                return STORAGE_SDCARD1_DIR;
            }

            try {
                return new File(MNT_SDCARD_DIR).getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return MNT_SDCARD_DIR;
            }
        }

        @Override
        public int getStorageID() {
            return SDCARD_STORAGE_ID;
        }

        @Override
        public boolean exist() {
            return storageExist(MNT_SDCARD_DIR)&& storageExist(MNT_SDCARD2_DIR);
        }

        @Override
        public String getName() {
            return "SDCard";
        }
    }

    public static class GnFile {

        public final GnStorage mStorage;
        public final File mFile;

        public GnFile(GnStorage storage, File file) {
            this.mStorage = storage;
            this.mFile = file;
//            Log.d(TAG, Log.getThreadName() + ": " + toString());
        }

        @Override
        public String toString() {
            return ((mStorage == null) ? "No Storage" : mStorage.getClass().getSimpleName())
                    + ": file path = " + mFile.getPath();
        }
    }
}
//Gionee <duansw><2013-3-20> modify for CR00789329 end


