package com.aurora.puremanager.fragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats.Uid;
import android.os.Handler;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.DrainType;
import com.aurora.puremanager.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class BatterySipper {

    private final String TAG = "BatterySipper";
    final Context mContext;
    final HashMap<String, UidToDetail> mUidCache = new HashMap<String, UidToDetail>();
    final ArrayList<BatterySipper> mRequestQueue;
    final Handler mHandler;
    String name;
    Drawable icon;
    int iconId;
    Uid uidObj;
    double value;
    double[] values;
    DrainType drainType;
    //long usageTime;
    long cpuTime;
    long gpsTime;
    long wifiRunningTime;
    long cpuFgTime;
    long wakeLockTime;
    double percent;
    //double noCoveragePercent;
    String defaultPackageName;

    public int userId;
    public long mobileRxPackets;
    public long mobileTxPackets;
    public long mobileActive;
    public int mobileActiveCount;
    public double mobilemspp; // milliseconds per packet
    public long wifiRxPackets;
    public long wifiTxPackets;
    public long mobileRxBytes;
    public long mobileTxBytes;
    public long wifiRxBytes;
    public long wifiTxBytes;
    public String[] mPackages;
    public String packageWithHighestDrain;

    static class UidToDetail {
        String name;
        String packageName;
        Drawable icon;
    }

    BatterySipper(Context context, ArrayList<BatterySipper> requestQueue, Handler handler, String label,
                  DrainType drainType, int iconId, Uid uid, double[] values) {
        mContext = context;
        mRequestQueue = requestQueue;
        mHandler = handler;
        this.values = values;
        name = label;
        this.drainType = drainType;
        if (iconId > 0) {
            icon = mContext.getResources().getDrawable(iconId);
        }
        if (values != null) {
            value = values[0];
        }
        if ((label == null || iconId == 0) && uid != null) {
            getQuickNameIconForUid(uid);
        }
        uidObj = uid;
    }

    double getSortValue() {
        return value;
    }

    double[] getValues() {
        return values;
    }

    Drawable getIcon() {
        return icon;
    }

    void getQuickNameIconForUid(Uid uidObj) {
        final int uid = uidObj.getUid();
        final String uidString = Integer.toString(uid);
        if (mUidCache.containsKey(uidString)) {
            UidToDetail utd = mUidCache.get(uidString);
            defaultPackageName = utd.packageName;
            name = utd.name;
            icon = utd.icon;
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        // final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
        String[] packages = pm.getPackagesForUid(uid);
        icon = pm.getDefaultActivityIcon();
        if (packages == null) {
            // name = Integer.toString(uid);
            if (uid == 0) {
                name = mContext.getResources().getString(R.string.process_kernel_label); // Android OS
            } else if ("mediaserver".equals(name)) {
                name = mContext.getResources().getString(R.string.process_mediaserver_label);
            }
            iconId = R.drawable.ic_power_system;
            icon = mContext.getResources().getDrawable(iconId);
            return;
        } else {
            // name = packages[0];
        }
        synchronized (mRequestQueue) {
            mRequestQueue.add(this);
        }
    }

    /**
     * Sets name and icon
     *
     * @param uid Uid of the application
     */
    void getNameIcon() {
        PackageManager pm = mContext.getPackageManager();
        final int uid = uidObj.getUid();
        final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            name = Integer.toString(uid);
            return;
        }

        String[] packageLabels = new String[packages.length];
        System.arraycopy(packages, 0, packageLabels, 0, packages.length);

        int preferredIndex = -1;
        // Convert package names to user-facing labels where possible
        for (int i = 0; i < packageLabels.length; i++) {
            // Check if package matches preferred package
            if (packageLabels[i].equals(name))
                preferredIndex = i;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
                CharSequence label = ai.loadLabel(pm);
                if (label != null) {
                    packageLabels[i] = label.toString();
                }
                if (ai.icon != 0) {
                    defaultPackageName = packages[i];
                    icon = ai.loadIcon(pm);
                    break;
                }
            } catch (NameNotFoundException e) {
            }
        }
        if (icon == null)
            icon = defaultActivityIcon;

        if (packageLabels.length == 1) {
            name = packageLabels[0];
        } else {
            // Look for an official name for this UID.
            for (String pkgName : packages) {
                try {
                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                    if (pi.sharedUserLabel != 0) {
                        final CharSequence nm = pm.getText(pkgName, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            name = nm.toString();
                            if (pi.applicationInfo.icon != 0) {
                                defaultPackageName = pkgName;
                                icon = pi.applicationInfo.loadIcon(pm);
                            }
                            break;
                        }
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        final String uidString = Integer.toString(uidObj.getUid());
        UidToDetail utd = new UidToDetail();
        utd.name = name;
        utd.icon = icon;
        utd.packageName = defaultPackageName;
        mUidCache.put(uidString, utd);
        mHandler.sendMessage(mHandler.obtainMessage(UsageSummaryFragment.MSG_UPDATE_NAME_ICON, this));
    }

    public void computeMobilemspp() {
        long packets = mobileRxPackets + mobileTxPackets;
        mobilemspp = packets > 0 ? (mobileActive / (double) packets) : 0;
    }
}