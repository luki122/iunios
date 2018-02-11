package gn.com.android.update.business;

public enum PackageUpgradeState {
    INITIAL(1), 
    READY_TO_DOWNLOAD(2), 
    DOWNLOADING(3), 
    DOWNLOAD_INTERRUPT(4), 
    DOWNLOAD_PAUSE(5), 
    DOWNLOAD_COMPLETE(6), 
    PATCHING(7), 
    INSTALLING(8), 
    INSTALL_COMPLETE(9); 

    private int mValue;

    PackageUpgradeState(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }
}
