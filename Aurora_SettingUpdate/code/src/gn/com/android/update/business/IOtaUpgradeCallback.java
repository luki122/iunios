package gn.com.android.update.business;

public abstract class IOtaUpgradeCallback extends IBaseCallback{
    public abstract void onError(int errorCode);
}
