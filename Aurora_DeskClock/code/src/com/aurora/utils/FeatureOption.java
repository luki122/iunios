package com.aurora.utils;

import android.os.SystemProperties;

public class FeatureOption {

    // Gionee <baorui><2013-05-27> modify for CR00798633 begin
    public static boolean MTK_GEMINI_SUPPORT = "yes".equals(SystemProperties.get("ro.gn.gemini.support",
            "yes"));
    // Gionee <baorui><2013-05-27> modify for CR00798633 end

}
