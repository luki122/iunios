package gn.com.android.update.business;

public abstract class IOtaCheckLocalUpgradeFileCallback extends IBaseCallback{

    public abstract void onError(int errorCode);
    public abstract void onCheckComplete();

    

}
