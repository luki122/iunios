package com.aurora.puremanager.utils;

import android.os.BatteryStats;
import android.os.Build;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;

import java.lang.reflect.Method;

public class BatteryInfoUtils {
    private static String TAG = "BatteryInfoUtils";

    public static IBatteryStats getBatteryInfo() {
        IBatteryStats batteryInfo;
        if (Build.VERSION.SDK_INT >= 19) { // android 4.4
            batteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        } else {
            batteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        }
        return batteryInfo;
    }

    public static long getNetworkActivityCount(BatteryStatsImpl stats, int type, int which) {
        long networkData = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("com.android.internal.os.BatteryStatsImpl");
            Method method = cls.getMethod("getNetworkActivityCount", int.class, int.class);
            obj = method.invoke(stats, type, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            networkData = Long.valueOf(obj.toString());
        }
        return networkData;
    }

    public static long getNetworkActivityCount(BatteryStats.Uid u, int type, int which) {
        long networkData = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("android.os.BatteryStats$Uid");
            Method method = cls.getMethod("getNetworkActivityCount", int.class, int.class);
            obj = method.invoke(u, type, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            networkData = Long.valueOf(obj.toString());
        }
        return networkData;
    }

    public static long getTcpBytesReceived(BatteryStats.Uid u, int which) {
        long bytesReceived = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("android.os.BatteryStats$Uid");
            Method method = cls.getMethod("getTcpBytesReceived", int.class);
            obj = method.invoke(u, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesReceived = Long.valueOf(obj.toString());
        }
        return bytesReceived;
    }

    public static long getTcpBytesSent(BatteryStats.Uid u, int which) {
        long bytesSent = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("android.os.BatteryStats$Uid");
            Method method = cls.getMethod("getTcpBytesSent", int.class);
            obj = method.invoke(u, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesSent = Long.valueOf(obj.toString());
        }
        return bytesSent;
    }

    public static long getMobileTcpBytesReceived(BatteryStatsImpl stats, int which) {
        long bytesReceived = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("com.android.internal.os.BatteryStatsImpl");
            Method method = cls.getMethod("getMobileTcpBytesReceived", int.class);
            obj = method.invoke(stats, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesReceived = Long.valueOf(obj.toString());
        }
        return bytesReceived;
    }

    public static long getMobileTcpBytesSent(BatteryStatsImpl stats, int which) {
        long bytesSent = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("com.android.internal.os.BatteryStatsImpl");
            Method method = cls.getMethod("getMobileTcpBytesSent", int.class);
            obj = method.invoke(stats, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesSent = Long.valueOf(obj.toString());
        }
        return bytesSent;
    }

    public static long getTotalTcpBytesReceived(BatteryStatsImpl stats, int which) {
        long bytesReceived = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("com.android.internal.os.BatteryStatsImpl");
            Method method = cls.getMethod("getTotalTcpBytesReceived", int.class);
            obj = method.invoke(stats, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesReceived = Long.valueOf(obj.toString());
        }
        return bytesReceived;
    }

    public static long getTotalTcpBytesSent(BatteryStatsImpl stats, int which) {
        long bytesSent = 0;

        Object obj = null;
        try {
            Class cls = Class.forName("com.android.internal.os.BatteryStatsImpl");
            Method method = cls.getMethod("getTotalTcpBytesSent", int.class);
            obj = method.invoke(stats, which);
        } catch (Exception ex) {
            String er = ex.toString();
            Log.e(TAG, er);
        }

        if (obj != null) {
            bytesSent = Long.valueOf(obj.toString());
        }
        return bytesSent;
    }
}
