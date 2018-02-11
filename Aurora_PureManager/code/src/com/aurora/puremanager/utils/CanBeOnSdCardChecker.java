package com.aurora.puremanager.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.content.PackageHelper;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-22 Change List:
 */
public class CanBeOnSdCardChecker {
    final IPackageManager mPm;
    int mInstallLocation;

    public CanBeOnSdCardChecker() {
        mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    public void init() {
        try {
            mInstallLocation = mPm.getInstallLocation();
        } catch (RemoteException e) {
            Log.e("CanBeOnSdCardChecker", "Is Package Manager running?");
            return;
        }
    }

    public boolean check(ApplicationInfo info) {
        boolean canBe = false;
        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            canBe = true;
        } else {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if (info.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL
                        || info.installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
                    canBe = true;
                } else if (info.installLocation == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
                    if (mInstallLocation == PackageHelper.APP_INSTALL_EXTERNAL) {
                        // For apps with no preference and the default value set
                        // to install on sdcard.
                        canBe = true;
                    }
                }
            }
        }
        return canBe;
    }
}
