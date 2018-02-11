package gn.com.android.update.ui;


import gn.com.android.update.utils.LogUtils;
import android.content.Context;
import android.text.TextUtils;
//import aurora.theme.AuroraThemeManager;
import android.os.SystemProperties;

public class GnSettingUpdateThemeUtils {

    private static boolean mDarkTheme = false;
    private static boolean mLightTheme = false;
    public static final String TYPE_DARK_THEME = "DARKTHEME";
    public static final String TYPE_LIGHT_THEME = "LIGHTTHEME";
    private static final boolean mIsGNThemeSupport = SystemProperties.get("ro.gn.theme.prop")
            .equals("yes");

    private static void getGnTheme(Context context) {
/*
        String style;
        if (mIsGNThemeSupport) {
            AuroraThemeManager  themeManager = new AuroraThemeManager(context);
            String tmpStyle = themeManager.getAppThemeStyle(AuroraThemeManager.SETTINGS);
            if (!TextUtils.isEmpty(tmpStyle)) {
                style = tmpStyle.substring(2, 3);
            } else {
                style = SystemProperties.get("ro.gn.theme.style", "0");
            }
        } else {
            style = SystemProperties.get("ro.gn.theme.style", "0");
        }

        LogUtils.logd("GnSettingsThemeUtils", "theme style : " + style);
*/
/*         if ("1".equals(style)) {
            mDarkTheme = false;
            mLightTheme = true;
        } else {
            mDarkTheme = true;
            mLightTheme = false;
        }*/
        
        mDarkTheme = false;
        mLightTheme = true;
    }

    public static String getThemeType(Context context) {
        getGnTheme(context);
        if (mDarkTheme) {
            return TYPE_DARK_THEME;
        } else {
            return TYPE_LIGHT_THEME;
        }
    }
}
