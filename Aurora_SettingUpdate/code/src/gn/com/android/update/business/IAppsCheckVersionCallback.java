package gn.com.android.update.business;

import gn.com.android.update.business.PackageUpgradeInfo;

import java.util.List;

public abstract class IAppsCheckVersionCallback extends IBaseCallback {
    public abstract void onCheckResult(boolean result, List<PackageUpgradeInfo> list);

    public abstract void onError(int errorCode);
}
