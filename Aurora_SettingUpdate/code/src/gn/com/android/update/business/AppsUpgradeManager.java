package gn.com.android.update.business;

import java.util.List;


public class AppsUpgradeManager {
    public void checkAppsVersion(IAppsCheckVersionCallback appsCheckVersionCallback) {

    }

    public List<PackageUpgradeInfo> getAllAppsUpgradeInfo() {
        return null;

    }

    public void downloadPackageFile(String packageName, IAppsDownloadCallback appsDownloadCallback) {

    }

    public void upgradePackage(String packageName, IAppsUpgradeCallback appUpgradeCallback) {

    }

    public void ignorePackage(String packageName) {

    }

    public void pausePackageDownload(String packageName, IAppsPauseDownloadCallback appPauseDownloadCallback) {

    }

    public void cancelPackageDownload(String packageName) {

    }

    public void cancelAppsUpgrade() {

    }

}
