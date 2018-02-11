package gn.com.android.update.business;

public abstract class IOtaDownloadCallback extends IBaseCallback {

    public abstract void onDownloadComplete();

    public abstract void onError(int errorCode);

    public abstract void onProgress(int currentProgress);

    public abstract void onVerifySucessful();
}
