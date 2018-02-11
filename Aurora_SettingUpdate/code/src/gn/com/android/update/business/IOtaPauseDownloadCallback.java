package gn.com.android.update.business;

public abstract class IOtaPauseDownloadCallback extends IBaseCallback {
    public abstract void onPauseComplete();

    public void onError(int errorCode) {

    }
}
