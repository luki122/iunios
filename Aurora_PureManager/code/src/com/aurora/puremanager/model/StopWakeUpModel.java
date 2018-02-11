package com.aurora.puremanager.model;

import android.content.Context;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.provider.open.AutoSleepAppProvider;
import com.aurora.puremanager.provider.open.StopWakeAppProvider;
import com.aurora.puremanager.utils.ApkUtils;

public class StopWakeUpModel {
    private static final String TAG = "WakeUpModel";
    private static StopWakeUpModel instance;
    private Context mContext;

    private StopWakeUpModel(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized StopWakeUpModel getInstance() {
        return instance;
    }

    public static synchronized StopWakeUpModel getInstance(Context context) {
        if (instance == null) {
            instance = new StopWakeUpModel(context);
        }
        return instance;
    }

    /**
     * 打开或关闭应用禁止唤醒功能
     *
     * @param pkgName
     * @param isOpen
     */
    public void tryChangeStopWakeState(String pkgName, boolean isOpen) {
        changeStopWakeStateInDB(pkgName, isOpen);
        return;
    }

    private void changeStopWakeStateInDB(String packageName, boolean isOpen) {
        if (packageName == null) {
            return;
        }

        if (isOpen) {
            StopWakeAppProvider.addAppInDB(mContext, packageName);
        } else {
            StopWakeAppProvider.deleteAppInDB(mContext, packageName);
        }
    }

    /**
     * 获取打开禁止唤醒应用个数
     *
     * @return
     */
    public int getStopWakeUpOpenAppNum() {
        return StopWakeAppProvider.loadStopWakeAppListInDB(mContext).size();
    }

    /**
     * 安装应用时调用
     *
     * @param appInfo
     */
    public void inStallApp(AppInfo appInfo) {
        if (appInfo == null) {
            return;
        }

        if (ApkUtils.isUserApp(appInfo.getApplicationInfo())) {
            StopWakeAppProvider.addAppInDB(mContext, appInfo.getPackageName());
        }
    }

    /**
     * 卸载应用时调用
     *
     * @param pkgName
     */
    public void unInStallApp(String pkgName) {
        if (pkgName == null) {
            return;
        }

        if (StopWakeAppProvider.isInDB(mContext, pkgName)) {
            StopWakeAppProvider.deleteAppInDB(mContext, pkgName);
        }
    }
}
