package com.gionee.framework;

import android.os.Build;

public class SIMInfoFactory {
    public static String TAG = "SIMInfoFactory";
    public static final int ANDROID_L_5_0_SDK = 21;
    public static final int ANDROID_L_5_1_SDK = 22;
    public static final int mSDK =  Build.VERSION.SDK_INT;

    private SIMInfoFactory() {
    }
    

    static class SIMInfoHodler {
        private static ISIMInfo instance;
        static {
            if (Common.MTK_PLATFORM.equals(Common.getPlatform()) && mSDK==ANDROID_L_5_0_SDK ) {
                instance = new MTKSIMInfo();
            }
            if (Common.MTK_PLATFORM.equals(Common.getPlatform()) && mSDK>=ANDROID_L_5_1_SDK ) {
                instance = new MTKSIMInfo5_1();
            }
            if (Common.QCOM_PLATFORM.equals(Common.getPlatform()) && mSDK==ANDROID_L_5_0_SDK ) {
                instance = new QCOMSIMInfo();
            }
            if (Common.QCOM_PLATFORM.equals(Common.getPlatform()) && mSDK==ANDROID_L_5_1_SDK ) {
                instance = new QCOMSIMInfo5_1();
            }
        }
    }

    public static ISIMInfo getDefault() {
        return SIMInfoHodler.instance;
    }
}
