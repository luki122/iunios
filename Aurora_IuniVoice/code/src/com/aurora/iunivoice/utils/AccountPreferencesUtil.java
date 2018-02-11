
package com.aurora.iunivoice.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * 个人中心配置文件操作API
 * @author JimXia
 * 2014-9-25 下午4:09:53
 */
public class AccountPreferencesUtil {

    private final static String TAG = "AccountPerfencesUtil";

    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;
    private static AccountPreferencesUtil sInstance;

    private static final String USER_PHONE = "phone"; // 用户手机号
    private static final String USER_EMAIL = "email"; // 用户邮箱地址
    private static final String USER_ID = "userId"; // 用户ID
    private static final String USER_TOKEN = "token"; // token
    private static final String USER_NICK = "nick"; // 用户昵称
    private static final String USER_PHOTOURL = "photoURL"; // 用户图像地址
    private static final String USER_COOKIE = "cookie"; // 用户cookie
    private static final String USER_KEY = "userKey"; // 用户key
    private static final String STORAGE_TOTAL = "storageTotal"; // 总容量
    private static final String STORAGE_AVAILABLE = "storageAvailable"; // 可用容量
    
    private static final String LAST_USED_PHOTOURL = "lastUsedPhotoURL"; // 当前使用的图像地址
    
    // 同步相关的状态信息
    private static final String AUTO_SYNC_TIME = "autoSyncTime"; // 每天自动启动同步的时间
    private static final String SYNC_DATE = "syncDate"; // 同步完成之后记录同步的日期，实现每天只同步一次
    private static final String IS_AUTO_SYNC_ENABLED = "isAutoSyncEnabled"; // 自动同步是否启用
    private static final String WIFI_SYNC_ONLY = "wifiSyncOnly"; // 仅在wifi同步
    private static final String LAST_SYNC_FINISHED_TIME = "lastSyncFinishedTime"; // 最后同步完成时间
    
    private static final String PENDING_LOGOUT_USER_ID = "pendingLogoutUserId"; // 上次注销没网络，等待有网后再给服务器发请求
    private static final String PENDING_LOGOUT_USER_KEY = "pendingLogoutUserKey";
    
    private static final String VERCODE_PREFIX = "__vercode_"; // 后边会拼上输入的手机号或者邮箱地址
    private static final String VERCODE_LAST_SEND_TIME = VERCODE_PREFIX + "lastSendTime_"; // 后边会拼上输入的手机号或者邮箱地址
    private static final String VERCODE_MODE = VERCODE_PREFIX + "mode_"; // 后边会拼上输入的手机号或者邮箱地址, mode，区分是注册，还是找回密码等等
    
    public static AccountPreferencesUtil getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AccountPreferencesUtil.class) {
                if (sInstance == null) {
                    sInstance = new AccountPreferencesUtil(context);
                }
            }
        }
        
        return sInstance;
    }
    
    private AccountPreferencesUtil(Context context) {
        sp = context.getSharedPreferences("aurora_account_prefences", Context.MODE_PRIVATE);
        editor = sp.edit();
    }
    
    protected synchronized void putStringValue(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    protected synchronized void putIntValue(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    protected synchronized void putLongValue(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public synchronized void putBooleanValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    protected String getString(String key) {
        String temp = sp.getString(key, "");
        return temp;
    }

    protected String getString(String key, String defaultValue) {
        String temp = sp.getString(key, defaultValue);
        return temp;
    }

    protected synchronized void removeKey(String key) {
        editor.remove(key);
        editor.commit();
    }

    protected int getInt(String key) {
        int temp = sp.getInt(key, 0);
        return temp;
    }

    protected int getInt(String key, int defaultValues) {
        int temp = sp.getInt(key, defaultValues);
        return temp;
    }

    protected long getLong(String key) {
        return getLong(key, 0L);
    }

    protected long getLong(String key, long defValue) {
        long temp = sp.getLong(key, defValue);
        return temp;
    }

    protected boolean getBoolean(String key) {
        boolean temp = sp.getBoolean(key, false);
        return temp;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        boolean temp = sp.getBoolean(key, defaultValue);
        return temp;
    }
    
    public void setUserPhone(String phoneNUm) {
        putStringValue(USER_PHONE, getSafeString(phoneNUm));
    }
    
    public String getUserPhone() {
    	return getString(USER_PHONE, "");
    }
    
    public void setUserEmail(String email) {
        putStringValue(USER_EMAIL, getSafeString(email));
    }
    
    public String getUserEmail() {
    	return getString(USER_EMAIL, "");
    }
    
    public void setUserID(String userID) {
        putStringValue(USER_ID, getSafeString(userID));
    }
    
    public String getUserID() {
        return getString(USER_ID, "");
    }
    
    public  String getUserToken() {
		return getString(USER_TOKEN, "");
	}

    public  void setUserToken(String token) {
    	  putStringValue(USER_TOKEN, getSafeString(token));

	}

	public void setUserNick(String nick) {
        putStringValue(USER_NICK, getSafeString(nick));
    }
    
    public String getUserNick() {
        return getString(USER_NICK, "");
    }
    
    public void setUserPhoneURL(String photoURL) {
        putStringValue(USER_PHOTOURL, getSafeString(photoURL));
    }
    
    public void setUserCookie(String cookie) {
        putStringValue(USER_COOKIE, getSafeString(cookie));
    }
    
    public String getUserCookie() {
    	return getString(USER_COOKIE, "");
    }
    public String getUserPhotoURL() {
        return getString(USER_PHOTOURL, "");
    }
    
    public void setLastUsedPhoneURL(String lastUsedPhotoURL) {
        putStringValue(LAST_USED_PHOTOURL, getSafeString(lastUsedPhotoURL));
    }
    
    public String getLastUsedPhotoURL() {
        return getString(LAST_USED_PHOTOURL, "");
    }
    
    public void setUserKey(String userKey) {
        putStringValue(USER_KEY, getSafeString(userKey));
    }
    
    public String getUserKey() {
        return getString(USER_KEY, "");
    }
    
    public void setPendingUserId(String pendingUserId) {
        putStringValue(PENDING_LOGOUT_USER_ID, getSafeString(pendingUserId));
    }
    
    public String getPendingUserId() {
        return getString(PENDING_LOGOUT_USER_ID, "");
    }
    
    public void setPendingUserKey(String pendingUserKey) {
        putStringValue(PENDING_LOGOUT_USER_KEY, getSafeString(pendingUserKey));
    }
    
    public String getPendingUserKey() {
        return getString(PENDING_LOGOUT_USER_KEY, "");
    }
    
    public void setStorageTotal(String total) {
    	putStringValue(STORAGE_TOTAL, getSafeString(total));
    }
    
    public String getStorageTotal() {
    	return getString(STORAGE_TOTAL, "0" + Globals.GB);
    }
    
    public void setStorageAvailable(long available) {
    	if (available >= 0) {
    		putLongValue(STORAGE_AVAILABLE, available);
    	}
    }
    
    public long getStorageAvailable() {
    	return getLong(STORAGE_AVAILABLE, 0);
    }
    
    public void setAutoSyncTime(String autoSyncTime) {
        putStringValue(AUTO_SYNC_TIME, getSafeString(autoSyncTime));
    }
    
    public String getAutoSyncTime() {
        final String[] times = {"22:00","22:20","22:40","23:00","23:20","23:40"};
        int index = new Random().nextInt(times.length);
        index = Math.min(index, times.length-1);
        String autoSyncTime = getString(AUTO_SYNC_TIME, times[index]);
        if (times[index].equals(autoSyncTime)) {
            setAutoSyncTime(autoSyncTime);
        }
        return autoSyncTime;
    }
    
    public void setSyncDate(long syncDate) {
        putLongValue(SYNC_DATE, syncDate);
    }
    
    public long getSyncDate() {
        return getLong(SYNC_DATE, 0L);
    }
    
    public void setLastSyncFinishedTime(long lastSyncFinishedTime) {
        putLongValue(LAST_SYNC_FINISHED_TIME, lastSyncFinishedTime);
    }
    
    public long getLastSyncFinishedTime() {
        return getLong(LAST_SYNC_FINISHED_TIME, 0L);
    }
    
    public void setAutoSyncEnabled(boolean isAutoSyncEnabled) {
        putBooleanValue(IS_AUTO_SYNC_ENABLED, isAutoSyncEnabled);
    }
    
    public boolean isAutoSyncEnabled() {
        return getBoolean(IS_AUTO_SYNC_ENABLED, true);
    }
    
    public void setWifiSyncOnly(boolean wifiSyncOnly) {
        putBooleanValue(WIFI_SYNC_ONLY, wifiSyncOnly);
    }
    
    public boolean isWifiSyncOnly() {
        return getBoolean(WIFI_SYNC_ONLY, true);
    }
    
    public void clear() {
//        editor.clear();
//        editor.commit();
        
//        Map<String, ?> allData = sp.getAll();
//        if (allData != null && !allData.isEmpty()) {
//            Iterator<String> keys = allData.keySet().iterator();
//            while (keys.hasNext()) {
//                String key = keys.next();
//                editor.remove(key);
//            }
//            editor.commit();
//        }
        removeKey(USER_KEY);
        removeKey(USER_ID);
        removeKey(USER_NICK);
        removeKey(USER_PHOTOURL);
        removeKey(LAST_USED_PHOTOURL);
        removeKey(STORAGE_TOTAL);
        removeKey(STORAGE_AVAILABLE);
    }
    
    protected String getSafeString(String str) {
        if (str == null) {
            return "";
        }
        
        return str;
    }

    public void dump() {
        Set<String> keySet = sp.getAll().keySet();
        Map<String, ?> keyToValuesMap = (Map<String, ?>) sp.getAll();
        for (String key : keySet) {
            Log.v(TAG, key + " = " + keyToValuesMap.get(key));
        }
    }
    
    public void recordLastSendTime(String key) {
        recordLastSendTime(key, Integer.MAX_VALUE);
    }
    
    public void recordLastSendTime(String key, int mode) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        
        putLongValue(getVercodeLastSendTimeKey(key), System.currentTimeMillis());
        recordLastSendTimeMode(key, mode);
    }
    
    public void recordLastSendTimeMode(String key, int mode) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        
        if (mode != Integer.MAX_VALUE) {
            putIntValue(getVercodeModeKey(key), mode);
        }
    }
    
    public long getVercodeLastSendTime(String key) {
        if (TextUtils.isEmpty(key)) {
            return 0;
        }
        
        return getLong(getVercodeLastSendTimeKey(key), 0);
    }
    
    private final int VERCODE_VALID_DURATION = 2 * 60 * 1000;
    
    public boolean isVercodeExpired(String key) {
        return isVercodeExpired(key, Integer.MAX_VALUE);
    }
    
    public boolean isVercodeExpired(String key, int mode) {
        if (TextUtils.isEmpty(key)) {
            return true;
        }
        
        long lastSendTime = getVercodeLastSendTime(key);
        int lastMode = getVercodeLastMode(key);
        if (mode == lastMode && (System.currentTimeMillis() - lastSendTime) < VERCODE_VALID_DURATION) {
            return false;
        }
        
        return true;
    }
    
    public int getVercodeLastMode(String key) {
        if (TextUtils.isEmpty(key)) {
            return Integer.MAX_VALUE;
        }
        
        return getInt(getVercodeModeKey(key), Integer.MAX_VALUE);
    }
    
    @SuppressLint("DefaultLocale")
    private String getVercodeLastSendTimeKey(String key) {
        key = key.toLowerCase();
        return VERCODE_LAST_SEND_TIME + key;
    }
    
    @SuppressLint("DefaultLocale")
    private String getVercodeModeKey(String key) {
        key = key.toLowerCase();
        return VERCODE_MODE + key;
    }
    
    public void clearVercodeInfo() {
        Map<String, ?> allData = sp.getAll();
        if (allData != null && !allData.isEmpty()) {
            boolean removed = false;
            Iterator<String> keys = allData.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith(VERCODE_PREFIX)) {
                    if (!removed) {
                        removed = true;
                    }
                    editor.remove(key);
                }
            }
            
            if (removed) {
                editor.commit();
            }
        }
    }
    
    public void removeVercodeInfo(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        
        removeKey(getVercodeLastSendTimeKey(key));
        removeKey(getVercodeModeKey(key));
    }
}