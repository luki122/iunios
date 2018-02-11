package gn.com.android.update.utils;

public class Error {
    public static final int ERROR_CODE_NETWORK_ERROR = 1001;

    public static final int ERROR_CODE_REMOTE_FILE_NOT_FOUND = 1002;

    public static final int ERROR_CODE_MOBILE_NETWORK = 1003;

    public static final int ERROR_CODE_NETWORK_DISCONNECT = 1004;

    public static final int ERROR_CODE_STORAGE_NOT_MOUNTED = 1005;

    /* when /mnt/sdcard or /storage/sdcard0 has no space, will throw this error;no matter the file is in
     * external or internal storage
     */
    public static final int ERROR_CODE_STORAGE_NO_SPACE = 1006;

    // when there is two storages, and the internal storage has no space
    public static final int ERROR_CODE_INTERNAL_STORAGE_NO_SPACE = 1007;

    public static final int ERROR_CODE_FILE_NOT_FOUND = 1008;

    public static final int ERROR_CODE_FILE_VERIFY_FAILED = 1009;

    public static final int ERROR_CODE_DOWNLOADFILE_DELETED = 1010;

    public static final int ERROR_CODE_WRONG_UPDATE_FILE = 1011;

    public static final int ERROR_CODE_BATTREY_LEVEL_LOW = 1012;

    
    /*
     * add when internet error
     * 
     */
    public static final int ERROR_CODE_SERVER_ERROR = 1013;
    
    public static final int ERROR_CODE_CONNECTION_TIME_OUT = 1014;
    
    public static final int ERROR_CODE_SERVER_NOT_FOUND = 1015;
    
    public static final int ERROR_CODE_INTERNET_NOT_USED = 1016;
    
    public static final int ERROR_CODE_BAD_REQUEST = 1017;
    
    public static final String ERROR_STRING_SERVER_ERROR = "serverError";
    
    public static final String ERROR_STRING_CONNECTION_TIME_OUT = "connection time out";
    
    public static final String ERROR_STRING_SERVER_NOT_FOUND = "server not found";
    
    public static final String ERROR_STRING_INTERNET_NOT_USERD = "internet not used";
    
    public static final String ERROR_STRING_BAD_REQUEST = "bad request";
    
    
}
