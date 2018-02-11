package gn.com.android.update.business.cache;

import gn.com.android.update.business.OtaUpgradeInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OtaUpgradeInfoPreferencOperator {
    private static final String UPGRADE_ENTITY = "UpgradeEntity";
    private static final String UPGRADE_ENTITY_RELEAST_NOTE = "UpgradeEntity_Desc";
    private static final String UPGRADE_ENTITY_INTERNAL_VERSION = "UpgradeEntity_Internal_Version";
    private static final String UPGRADE_ENTITY_VERSION = "UpgradeEntity_Version";
    private static final String UPGRADE_ENTITY_DOWNLOAD_URL = "UpgradeEntity_DownloadSizeURL";
    private static final String UPGRADE_ENTITY_FILE_SIZE = "UpgradeEntity_FileSize";
    private static final String UPGRADE_ENTITY_FILE_MD5 = "UpgradeEntity_FileMd5";
    private static final String UPGRADE_ENTITY_RELEASENOTE_URL = "UpgradeEntity_ReleaseNote";
    private static final String UPGRADE_ENTITY_NUM_PEPOLE_UPGRDED = "UpgradeEntity_NumPelUpgraded";
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;

    public OtaUpgradeInfoPreferencOperator(Context context) {
        mSharedPreferences = context.getSharedPreferences(UPGRADE_ENTITY, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public OtaUpgradeInfo getOtaUpgradeInfo() {
        OtaUpgradeInfo upgradeInfo = new OtaUpgradeInfo();
        synchronized (this) {
            upgradeInfo.setReleaseNote(mSharedPreferences.getString(UPGRADE_ENTITY_RELEAST_NOTE, ""));
            upgradeInfo.setReleaseNoteUrl(mSharedPreferences.getString(UPGRADE_ENTITY_RELEASENOTE_URL, ""));
            upgradeInfo.setInternalVer(mSharedPreferences.getString(UPGRADE_ENTITY_INTERNAL_VERSION, ""));
            upgradeInfo.setVersion(mSharedPreferences.getString(UPGRADE_ENTITY_VERSION, ""));
            upgradeInfo.setDownloadurl(mSharedPreferences.getString(UPGRADE_ENTITY_DOWNLOAD_URL, ""));
            upgradeInfo.setMd5(mSharedPreferences.getString(UPGRADE_ENTITY_FILE_MD5, ""));
            String fileSizeString = mSharedPreferences.getString(UPGRADE_ENTITY_FILE_SIZE, "0");
            int fileSize = 0;
            try {
                fileSize = Integer.parseInt(fileSizeString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            upgradeInfo.setFileSize(fileSize);
            upgradeInfo.setDownloadedPeopleNum(mSharedPreferences
                    .getInt(UPGRADE_ENTITY_NUM_PEPOLE_UPGRDED, 0));
        }
        return upgradeInfo;

    }

    public void storeOtaUpgradeInfo(OtaUpgradeInfo otaUpgradeInfo) {

        synchronized (this) {
            mEditor.putString(UPGRADE_ENTITY_RELEAST_NOTE, otaUpgradeInfo.getReleaseNote());
            mEditor.putString(UPGRADE_ENTITY_RELEASENOTE_URL, otaUpgradeInfo.getReleaseNoteUrl());
            mEditor.putString(UPGRADE_ENTITY_VERSION, otaUpgradeInfo.getVersion());
            mEditor.putString(UPGRADE_ENTITY_INTERNAL_VERSION, otaUpgradeInfo.getInternalVer());
            mEditor.putString(UPGRADE_ENTITY_DOWNLOAD_URL, otaUpgradeInfo.getDownloadurl());
            mEditor.putString(UPGRADE_ENTITY_FILE_MD5, otaUpgradeInfo.getMd5());
            mEditor.putString(UPGRADE_ENTITY_FILE_SIZE, "" + otaUpgradeInfo.getFileSize());
            mEditor.putInt(UPGRADE_ENTITY_NUM_PEPOLE_UPGRDED, otaUpgradeInfo.getDownloadedPeopleNum());
            mEditor.commit();
        }

    }

    public void initial() {
        synchronized (this) {
            mEditor.clear().commit();
        }

    }
}
