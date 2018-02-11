package gn.com.android.update.business;

public class OtaUpgradeInfo extends UpgradeInfo {
    private String mReleaseNoteUrl = "";
    private String mInternalVer = "";
    private int mDownloadedPeopleNum;
    private boolean mExtPkg = true;

    /* used for recovery update*/
    private boolean mIsRecoveryUpdate = false;
    
    public void setRecoveryUpdate(boolean update){
        mIsRecoveryUpdate = update;
    }
    
    public boolean getRecoveryUpdate(){
        return mIsRecoveryUpdate;
    }
    
    public boolean getExtPkg() {
        return mExtPkg;
    }

    public void setExtPkg(boolean extPkg) {
        mExtPkg = extPkg;
    }

    public String getReleaseNoteUrl() {
        return mReleaseNoteUrl;
    }

    public void setReleaseNoteUrl(String releaseNoteUrl) {
        mReleaseNoteUrl = releaseNoteUrl;
    }

    public String getInternalVer() {
        return mInternalVer;
    }

    public void setInternalVer(String internalVer) {
        mInternalVer = internalVer;
    }

    public int getDownloadedPeopleNum() {
        return mDownloadedPeopleNum;
    }

    public void setDownloadedPeopleNum(int downloadedNum) {
        mDownloadedPeopleNum = downloadedNum;
    }
}
