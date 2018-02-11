package com.aurora.puremanager.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.provider.open.AutoSleepAppProvider;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.provider.open.StopWakeAppProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PureBackgroundLoader extends AsyncTaskLoader<Object> {
    private static final String TAG = "PureBackgroundLoader";

    private Context mContext;
    private List<BaseData> AppList = new ArrayList<BaseData>();

    public static final int ID_LOADER_DEFAULT = 100;
    public static final int ID_LOADER_PURE_APP_COUNT = ID_LOADER_DEFAULT + 1;
    public static final int ID_LOADER_USER_APP_LIST = ID_LOADER_DEFAULT + 2;

    public PureBackgroundLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Object loadInBackground() {
        Object result = null;
        switch (getId()) {
            case ID_LOADER_PURE_APP_COUNT:
                result = loadPureAppCount();
                break;
            case ID_LOADER_USER_APP_LIST:
                result = loadUserAppList();
                break;
            default:
                break;
        }

        return result;
    }

    private int loadPureAppCount() {
        int count = 0;
        AppsInfo userAppsInfo = ConfigModel.getInstance(mContext).getAppInfoModel()
                .getThirdPartyAppsInfo();
        if (userAppsInfo == null) {
            return 0;
        }

        for (int i = 0; i < userAppsInfo.size(); i++) {
            AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
            if (appInfo == null || !appInfo.getIsInstalled()) {
                continue;
            }

            ApkUtils.initAppNameInfo(mContext, appInfo);
            if (StopWakeAppProvider.isInDB(mContext, appInfo.getPackageName())
                    && AutoSleepAppProvider.isInDB(mContext, appInfo.getPackageName())) {
                AutoStartData autoStartData = AutoStartModel.getInstance(mContext).
                        getAutoStartData(appInfo.getPackageName());
                if (autoStartData == null) {
                    count++;
                } else if (!AutoStartAppProvider.isInDB(mContext, appInfo.getPackageName())) {
                    count++;
                } else {
                }
            }
        }

        return count;
    }

    private List<BaseData> loadUserAppList() {
        if (AppList != null) {
            AppList.clear();
        }

        AppsInfo userAppsInfo = ConfigModel.getInstance(mContext).getAppInfoModel()
                .getThirdPartyAppsInfo();
        if (userAppsInfo == null) {
            return null;
        }

        for (int i = 0; i < userAppsInfo.size(); i++) {
            AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
            if (appInfo == null || !appInfo.getIsInstalled()) {
                continue;
            }

            ApkUtils.initAppNameInfo(mContext, appInfo);
            AutoStartData autoStartData = AutoStartModel.getInstance(mContext).
                    getAutoStartData(appInfo.getPackageName());
            if (autoStartData == null || AutoStartAppProvider.isInDB(mContext, appInfo.getPackageName())) {
                appInfo.setIsStopAutoStart(false);
            } else {
                appInfo.setIsStopAutoStart(true);
            }

            if (StopWakeAppProvider.isInDB(mContext, appInfo.getPackageName())) {
                appInfo.setIsStopWakeup(true);
            } else {
                appInfo.setIsStopWakeup(false);
            }

            if (AutoSleepAppProvider.isInDB(mContext, appInfo.getPackageName())) {
                appInfo.setIsAutoSleep(true);
            } else {
                appInfo.setIsAutoSleep(false);
            }

            AppList.add(appInfo);
        }

        sortList(AppList);
        return AppList;
    }

    private void sortList(List<BaseData> appsList) {
        Collections.sort(appsList, new Comparator<BaseData>() {
            public int compare(BaseData s1, BaseData s2) {
                return Utils.compare(((AppInfo) s1).getAppNamePinYin(),
                        ((AppInfo) s2).getAppNamePinYin());
            }
        });
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        super.onStartLoading();
    }
}
