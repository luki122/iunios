package com.android.settings.securitypasswd;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.KeyStore;
import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;
import com.aurora.utils.AuroraUtils;
import com.mediatek.settings.sim.Log;
import java.lang.reflect.Method;


public class SecurityUtils {
    static final String TAG = "SecurityUtils";
    public static final String CONFIRM_CREDENTIALS = "confirm_credentials";

    static final int MIN_PASSWORD_QUALITY = DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;

    public static final int SECURITYPWD_PATTERN_TYPE = 1;
    public static final int SECURITYPWD_FOUR_NUMBER_TYPE = 2;

    public static final String APP_EXTRA_DATA = "app_extra_data";

    public static final String NEED_CONFIRM_AGAIN = "need_confirm_again";
    public static final String APP_CONFIRM = "app_confirm";

    //security password, locksettings.db
    public static final String SECURITYPASSWORD_TYPE = "securitypassword_type";
    public static final String BACKUP_PASSWORD = "backup_password";
    public static final String BACKUP_PATTERN = "backup_pattern";

    static final long ONE_MIN_TO_MILLI = 60 * 1000;

    public static boolean isSecurityPasswordEnable(LockPatternUtils utils) {
        if (utils.savedPasswordExists()) {
            Log.e(TAG, "isSecurityPasswordEnable savedPasswordExists " + true);
            return true;
        } else if (utils.savedPatternExists()) {
            if (utils.checkPattern(null)) {
                Log.e(TAG, "isSecurityPasswordEnable savedPatternExists " + false);
                return false;
            } else {
                Log.e(TAG, "isSecurityPasswordEnable savedPatternExists " + true);
                return true;
            }
        }
        Log.e(TAG, "isSecurityPasswordEnable " + false);
        return false;
    }

    public static long getWaitInputPwdMinute(int number) {
        long min = (long) Math.pow(2, number - AuroraUtils.START_TIMING_NUMBER);
        return min;

    }

    public static long getSecurityPasswordType() {
        ILockSettings lockSettingsService = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")); 
        /*LockPatternUtilsCache.getInstance(
        ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings")));*/

        long result = 0;
        try {
            result = lockSettingsService.getLong(SECURITYPASSWORD_TYPE, 0, UserHandle.USER_OWNER);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't get long " + SECURITYPASSWORD_TYPE + re);
        }
        return result;

    }

    public static long getSecurityPasswordType(AuroraLockPatternUtils utils) {
        return utils.gnGetLong(SecurityUtils.SECURITYPASSWORD_TYPE, 0);
    }


    public static boolean isSecurityPasswordPatternType() {
        if (getSecurityPasswordType() == SECURITYPWD_PATTERN_TYPE) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSecurityPasswordSupport() {
        boolean value = false;
        try {
            Class<?> mClass = Class.forName("android.util.AmigoSecurityPassWord");
            Method mMethod = mClass.getMethod("isSecurityPasswordEnable", Context.class);

            value = true;

        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException: " + e);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "NoSuchMethodException: " + ex);
        }
        return value;

    }

    public static void resetKeyStore(boolean isUnlocked) {
        KeyStore keyStore = KeyStore.getInstance();
        if (!isUnlocked) {
            keyStore.reset();
        }
    }

    public static boolean isKeyStoreUnlocked() {
        KeyStore keyStore = KeyStore.getInstance();
        boolean isUnlocked = keyStore.isUnlocked();
        return isUnlocked;
    }

}
