package gn.com.android.update.business;




public abstract class IOtaCheckVersionCallback extends IBaseCallback{
    public abstract void onCheckResult(boolean result, OtaUpgradeInfo otaUpgradeInfo);
    public abstract void onError(int errorCode);
}
