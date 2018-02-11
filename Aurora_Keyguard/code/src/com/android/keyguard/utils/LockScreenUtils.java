package com.android.keyguard.utils;

import android.content.Context;

import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.internal.widget.LockPatternUtils;

public class LockScreenUtils {

    // Timeout used for keypresses
    public static final int DIGIT_PRESS_WAKE_MILLIS = 5000;
    
    public static boolean USE_UPPER_CASE = true;
    
    private static LockScreenUtils sInstance;
    private final Context mContext;
    private final LockPatternUtils mLockPatternUtils;
    private final KeyguardSecurityModel mSecurityModel;
    
    public static LockScreenUtils getInstance(Context context){
        if (sInstance == null) {
            sInstance = new LockScreenUtils(context);
        }
        return sInstance;
    }
    
    private LockScreenUtils(Context context){
        mContext = context;
        mLockPatternUtils = new LockPatternUtils(mContext);
        mSecurityModel = new KeyguardSecurityModel(mContext);
    }
    
    public boolean isSecure() {
        SecurityMode mode = mSecurityModel.getSecurityMode();
        switch (mode) {
            case Pattern:
                return mLockPatternUtils.isLockPatternEnabled();
            case Password:
            case PIN:
                return mLockPatternUtils.isLockPasswordEnabled();
            case SimPin:
            case SimPuk:
            case Account:
                return true;
            case None:
                return false;
            default:
                throw new IllegalStateException("Unknown security mode " + mode);
        }
    }
    
}
