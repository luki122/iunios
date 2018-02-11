package gn.com.android.update.business;

/*This exception happens when getDownloadPath
 * 
 * 
 * */

public class NoSpaceException extends Exception {

    /*the storage path which have no space
     * 
     * */
    private String mStoragePath = null;

    /*if the file already exists 
     * 
     * */
    private boolean mFileExists = false;
    /**
     * 
     */
    private static final long serialVersionUID = -1637171901961138163L;

    public NoSpaceException(boolean fileExists, String storagePath) {
        mFileExists = fileExists;
        mStoragePath = storagePath;
    }

    public String getStoragePath() {
        return mStoragePath;
    }

    public boolean isFileExists() {
        return mFileExists;
    }
}
