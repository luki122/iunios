package gn.com.android.update.business;

public abstract class IAppsPauseDownloadCallback extends IBaseCallback {
    public abstract void onPauseComplete(String packageName);

    public void onError(int errorCode) {

    }
}
