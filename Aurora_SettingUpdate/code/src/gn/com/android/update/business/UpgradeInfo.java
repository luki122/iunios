package gn.com.android.update.business;

public class UpgradeInfo {
    private String mReleaseNote = "";
    private String mMd5 = "";
    private String mDownloadurl = "";
    private String mVersion = "";
    private int mFileSize = 0;

    public String getReleaseNote() {
        return mReleaseNote;
    }

    public void setReleaseNote(String releaseNote) {
        mReleaseNote = releaseNote;
    }

    public String getMd5() {
        return mMd5;
    }

    public void setMd5(String md5) {
        mMd5 = md5;
    }

    public String getDownloadurl() {
        return mDownloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        mDownloadurl = downloadurl;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public int getFileSize() {
        return mFileSize;
    }

    public void setFileSize(int fileSize) {
        mFileSize = fileSize;
    }

}
