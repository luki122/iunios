package gn.com.android.update.business;

public enum OtaUpgradeState {
    INITIAL(1), 
    CHECKING(2), 
    READY_TO_DOWNLOAD(3), 
    DOWNLOADING(4), 
    /*when some exception happen, like network error. we can resume the download,
     * use this state
     * */
    DOWNLOAD_INTERRUPT(5), 
    /*when user click pause ,or some serious exception happen,we can not resume the download
     * use this state
     * */
    DOWNLOAD_PAUSE(6),
    DOWNLOAD_COMPLETE(7), 
    INSTALLING(8);

    private int mValue;

    OtaUpgradeState(int value) {
        mValue = value;
    }


    public int getValue() {
        return mValue;
    }
}
