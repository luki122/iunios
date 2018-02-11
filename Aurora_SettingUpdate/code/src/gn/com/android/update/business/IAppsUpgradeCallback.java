package gn.com.android.update.business;


public abstract class IAppsUpgradeCallback extends IBaseCallback{
    public abstract void onUpgradeResult(int resultCode, String packageName);
}
