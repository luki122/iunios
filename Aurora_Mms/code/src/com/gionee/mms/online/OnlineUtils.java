package com.gionee.mms.online;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OnlineUtils {

    public static enum NETWORK_TPYE {
        NOT_CONNECTED, WIFI_TYPE, MOBILE_TYPE
    }

    public static String getModelName() {
        return Build.MODEL;
    }

    public static String getRomVersion() {
        String romVer = SystemProperties.get("ro.gn.gnromvernumber", "GiONEE ROM4.0.1");
        int index = romVer.indexOf("M");
        return romVer.substring(index == -1 ? 0 : index + 1);
    }

    public static String getIMEI(Context ctx) {
        return ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE))
        .getDeviceId();
    }
    
    public static String getMmsVersion(Context ctx) {
        PackageManager pManager = ctx.getPackageManager();
        String version = null;
        try {
            version = pManager.getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            return version;
        }
    }
    
    public static void startPublicNetworkModule(Context ctx) {
        Intent intent = new Intent("gn.android.intent.action.APP_START");
        intent.putExtra("appname", ctx.getPackageName());
        ctx.sendBroadcast(intent);
    }

    public static void showWifiAlert(Context ctx) {
        Intent intent = new Intent("gn.android.intent.action.SHOW_3GWIFIALERT");
        intent.putExtra("appname", ctx.getPackageName());
        ctx.sendBroadcast(intent);
    }

    public static void stopPublicNetworkModule(Context ctx) {
        Intent intent = new Intent("gn.android.intent.action.APP_EXIT");
        intent.putExtra("appname", ctx.getPackageName());
        ctx.sendBroadcast(intent);
    }

    public static NETWORK_TPYE checkNetworkState(Context ctx) {
        ConnectivityManager cManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_TPYE.WIFI_TYPE;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_TPYE.MOBILE_TYPE;
            }
        }
        return NETWORK_TPYE.NOT_CONNECTED;
    }
}
