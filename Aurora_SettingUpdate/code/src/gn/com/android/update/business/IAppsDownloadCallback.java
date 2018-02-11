package gn.com.android.update.business;

public abstract class IAppsDownloadCallback extends IBaseCallback {
    public abstract void onProgress(int progress, String packageName);

    public abstract void onError(int errorCode, String packageName);
}
