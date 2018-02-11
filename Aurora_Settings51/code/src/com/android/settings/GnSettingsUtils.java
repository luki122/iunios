package com.android.settings;
/*Gionee fangbin 20120619 added for CR00622030*/
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.text.TextUtils;
import com.aurora.featureoption.FeatureOption;
//import aurora.theme.AuroraThemeManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;

public class GnSettingsUtils {

    private static boolean mDarkTheme = false;
    private static boolean mLightTheme = false;
    public static final String TYPE_DARK_THEME = "DARKTHEME";
    public static final String TYPE_LIGHT_THEME = "LIGHTTHEME";
    
    /*Gionee: huangsf 20121210 add for CR00741405 start*/
    public static final boolean sGnSettingSupport = android.os.SystemProperties.get("ro.gn.settings.prop", "no").equals("yes");
    /*Gionee: huangsf 20121210 add for CR00741405 end*/
    
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 begin
    private static final Uri HIDE_APP_URI = Uri.parse("content://com.gionee.settings.HideAppProvider/hide");
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 end
    
    //AURORA_START::delete gnTheme::waynelin::2013-9-11
/*
    private static void getGnTheme(Context context) {
        String style;
        if (FeatureOption.GN_APP_THEME_SUPPORT) {
//          AuroraThemeManager themeManager = (AuroraThemeManager) context.getSystemService(Context.THEME_MANAGER_SERVICE);
            AuroraThemeManager themeManager = new AuroraThemeManager(context.getApplicationContext());
            String tmpStyle = themeManager.getAppThemeStyle(AuroraThemeManager.SETTINGS);
            if (!TextUtils.isEmpty(tmpStyle)) {
                style = tmpStyle.substring(2, 3);
            } else {
                style = SystemProperties.get("ro.gn.theme.style", "0");
            }
        } else {
            style = SystemProperties.get("ro.gn.theme.style", "0");
        }

        Log.i("GnSettingsThemeUtils", "theme style : " + style);
        if ("1".equals(style)) {
            mDarkTheme   = false;
            mLightTheme  = true;
        } else if ("2".equals(style)) {
            mDarkTheme   = true;
            mLightTheme  = false;
        } else {
            mDarkTheme   = false;
            mLightTheme  = false;
        }
    }
    */
//AURORA_END::delete gnTheme::waynelin::2013-9-11

    public static String getThemeType(Context context) {
        // Gionee <huangsf> <2013-06-24> modify for theme type begin
        /*
        getGnTheme(context);
        if (mDarkTheme) {
            return TYPE_DARK_THEME;
        } else {
            return TYPE_LIGHT_THEME;
        }
        */
        return TYPE_LIGHT_THEME;
        // Gionee <huangsf> <2013-06-24> modify for theme type begin
    }
    
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 begin
    public static void setPackageEnabled(Context context, boolean enabled) {
        Cursor cursor = context.getContentResolver().query(HIDE_APP_URI, new String[] {
                "package", "class"
        }, " status=?", new String[] {
            String.valueOf(1)
        }, null);
        if (cursor != null && cursor.moveToFirst()) {
            //ContentValues cv = new ContentValues();
            while (!cursor.isAfterLast()) {
                String pkg = cursor.getString(0);
                String mainClss = cursor.getString(1);
                if (mainClss != null) {
                    ComponentName cm = new ComponentName(cursor.getString(0), cursor.getString(1));
                    cm.toString();
                    if (enabled) {
                        context.getPackageManager().setComponentEnabledSetting(cm,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                    } else {
                        context.getPackageManager().setComponentEnabledSetting(cm,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                    //cv.put("hide_flag", String.valueOf(enabled ? 1 : 0));
                    //context.getContentResolver().update(HIDE_APP_URI, cv, " package=? and class=?",
                    //        new String[] {pkg, mainClss});
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
    }
    // Gionee <qiuxd> <2013-05-13> add for CR00809610 end
}
