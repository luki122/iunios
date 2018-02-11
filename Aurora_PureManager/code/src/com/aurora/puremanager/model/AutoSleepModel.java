package com.aurora.puremanager.model;

import android.content.Context;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.provider.open.AutoSleepAppProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;

import java.util.HashSet;
import java.util.Locale;

public class AutoSleepModel {
    private static final String TAG = "AutoSleepModel";
    private static AutoSleepModel instance;
    private Context mContext;


    public AutoSleepModel(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized AutoSleepModel getInstance() {
        return instance;
    }

    public static synchronized AutoSleepModel getInstance(Context context) {
        if (instance == null) {
            instance = new AutoSleepModel(context);
        }
        return instance;
    }

    /**
     * 打开或关闭应用自动休眠功能
     *
     * @param pkgName
     * @param isOpen
     */
    public void tryChangeAutoSleepState(String pkgName, boolean isOpen) {
        changeAutoSleepStateInDB(pkgName, isOpen);
        return;
    }

    private void changeAutoSleepStateInDB(String packageName, boolean isOpen) {
        if (packageName == null) {
            return;
        }

        if (isOpen) {
            AutoSleepAppProvider.addAppInDB(mContext, packageName);
        } else {
            AutoSleepAppProvider.deleteAppInDB(mContext, packageName);
        }
    }

    /**
     * 应用是否是自动休眠模式
     * @param packageName
     * @return
     */
    public boolean isInAutoSleepMode(String packageName) {
        return AutoSleepAppProvider.isInDB(mContext, packageName);
    }

    /**
     * 获取所有自动休眠的应用
     *
     * @return
     */
    public HashSet<String> getAutoSleepOpenApp() {
        HashSet<String> autoSleepAppList = AutoSleepAppProvider.loadAutoSleepAppListInDB(mContext);

        return autoSleepAppList;
    }

    /**
     * 获取打开自动休眠应用个数
     *
     * @return
     */
    public int getAutoSleepOpenAppNum() {
        return getAutoSleepOpenApp().size();
    }

    /**
     * 安装应用时调用
     * 不在白名单应用默认开启自动休眠
     * @param appInfo
     */
    public void inStallApp(AppInfo appInfo) {
        if (appInfo == null) {
            return;
        }

        if (ApkUtils.isUserApp(appInfo.getApplicationInfo())
                && !isInAutoSleepWhiteList(appInfo.getPackageName())) {
            AutoSleepAppProvider.addAppInDB(mContext, appInfo.getPackageName());
        }
    }

    /**
     * 卸载应用时调用
     * 应用卸载时清除数据库
     * @param pkgName
     */
    public void unInStallApp(String pkgName) {
        if (pkgName == null) {
            return;
        }

        if (isInAutoSleepMode(pkgName)) {
            AutoSleepAppProvider.deleteAppInDB(mContext, pkgName);
        }
    }

    private boolean isInAutoSleepWhiteList(String pkgName) {
        for (int i = 0; i < Constants.autoSleepWhiteList.length; i++) {
            if (pkgName != null && pkgName.toLowerCase(Locale.US)
                    .contains(Constants.autoSleepWhiteList[i].toLowerCase(Locale.US))) {
                LogUtils.printWithLogCat(TAG, pkgName + "is in whitelist");
                return true;
            }
        }
        return false;
    }
}
