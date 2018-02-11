package com.aurora.puremanager.utils;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.aurora.puremanager.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-17 Change List:
 */
public class HelperUtils {

    /**
     * To get application info by package name
     * 
     * @param context
     * @param pkgName
     * @return
     */
    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Log.d("HelperUtils", "getApplicationInfo warning NameNotFoundException:" + e);
        }
        return result;
    }

    /**
     * To get default input method information.
     * 
     * @return InputMethodInfo
     */
    public static InputMethodInfo getDefInputMethod(Context context) {
        String defInput = android.provider.Settings.Secure.getString(context.getContentResolver(),
                "default_input_method");
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mImis = mImm.getEnabledInputMethodList();
        for (int i = 0; i < mImis.size(); i++) {
            InputMethodInfo info = mImis.get(i);
            if (info.getId().equals(defInput)) {
                return info;
            }
        }
        return null;
    }

    /**
     * To get numbers of current system installed third part applications.
     * 
     * @param context
     * @return the number of third app
     */
    public static int getSumThirdApp(Context context) {
        int result = 0;
        List<ApplicationInfo> appInfos = context.getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < appInfos.size(); i++) {
            ApplicationInfo info = appInfos.get(i);
            if (AppFilterUtil.THIRD_PARTY_FILTER.filterApp(info)) {
                result++;
            }
        }
        return result;
    }

    /**
     * To get application of current installed application
     * 
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getApplicationInfo2(Context context) {
        List<ApplicationInfo> mApplications = context.getPackageManager().getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
        for (int i = 0; i < mApplications.size(); i++) {
            final ApplicationInfo info = mApplications.get(i);

            if (!info.enabled && info.enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || info.packageName.equals("android")) {
                mApplications.remove(i);
                i--;
                continue;
            }
        }
        return mApplications;
    }

    public static List<ApplicationInfo> getApplicationInfo(Context context) {
        List<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        List<ResolveInfo> resolves = getLauncherShowActivity(context);
        for (int i = 0; i < resolves.size(); i++) {
            ResolveInfo info = resolves.get(i);
            ApplicationInfo ai = getApplicationInfo(context, info.activityInfo.packageName);
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 begin
            if (null == ai || containApplications(applications, ai)) {
                continue;
            }
            // Gionee <xuwen> <2015-08-25> modify for CR01547240 end
            applications.add(ai);
        }
        return applications;
    }

    private static boolean containApplications(List<ApplicationInfo> applications, ApplicationInfo ai) {
        for (ApplicationInfo appInfo : applications) {
            //Gionee <xuwen><2015-08-05> add for CR01533145 begin
            if (null == ai || null == ai.packageName) {
                return false;
            }
            //Gionee <xuwen><2015-08-05> add for CR01533145 end

            if (ai.packageName.equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static List<ResolveInfo> getLauncherShowActivity(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * To convert String {@value src} to "src(num)"
     * 
     * @param src
     * @param num
     * @return
     */
    public static String joinStr(String src, int num) {
        StringBuffer strBuf = new StringBuffer();
        int index = src.indexOf("(");
        if (index > 0) {
            src = src.substring(0, index);
        }
        if (num >= 0) {
            strBuf.append(src);
            strBuf.append("(");
            strBuf.append(num);
            strBuf.append(")");
            return strBuf.toString();
        } else {
            return src;
        }
    }

    public static String getSizeStr(Context context, long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(context, size);
        }
        return null;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        CharSequence cs = info.loadLabel(context.getPackageManager());
        // String result =
        // info.loadLabel(context.getPackageManager()).toString();
        if (cs == null) {
            return info.packageName;
        }
        return cs.toString();
    }

    public static Drawable loadIcon(Context context, ApplicationInfo info) {
		Drawable result = null;
		try {
			if (info != null) {
				result = info.loadIcon(context.getPackageManager());
			}
			if (result == null) {
				result = context.getResources().getDrawable(
						R.drawable.sym_app_on_sd_unavailable_icon);
			}
		} catch (Exception ex) {
		}

		return result;
    }

    public static String getHtmlString(String str, String sec) {
        StringBuffer tempStr = new StringBuffer();
        tempStr.append("<font color=red>");
        tempStr.append(sec);
        tempStr.append("</font>");
        tempStr.append(str);
        return tempStr.toString();
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    private static Set<String> mWhiteList = null;
    private static Set<String> mDefaultWhiteList = null;

    public static void cleanWhiteList() {
        if (mWhiteList != null) {
            mWhiteList.clear();
            mWhiteList = null;
        }
        if (mDefaultWhiteList != null) {
            mDefaultWhiteList.clear();
            mDefaultWhiteList = null;
        }
    }

    public static void writeAppProcessLimitOptions(SharedPreferences mSharedPreferences) {
        try {
            int limit = mSharedPreferences.getInt("app_process_limit", -1);
            if (limit != -1) {
                ActivityManagerNative.getDefault().setProcessLimit(limit);
            }
        } catch (RemoteException e) {
        }
    }
    
    public static void sendWhiteListChangeBroadcast(Context context) {
        Intent intent = new Intent("com.gionee.softmanager.intent.WHITELIST_CHANGE");
        context.sendBroadcast(intent);
    }

    public static List<String> getPowerSaveWhiteList(Context context) {
        String[] whitearray = context.getResources().getStringArray(R.array.super_power_save_whitelist);
        return Arrays.asList(whitearray);
    }
}
