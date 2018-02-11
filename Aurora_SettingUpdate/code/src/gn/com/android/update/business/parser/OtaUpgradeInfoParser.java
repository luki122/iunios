package gn.com.android.update.business.parser;

import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class OtaUpgradeInfoParser {
    private static final String TAG = "OtaUpgradeInfoParser";

    public static OtaUpgradeInfo parseOtaUpgradeInfo(String data) throws JSONException {
        JSONObject upgradeInJsonObject = new JSONObject(data);

        String downloadUrl = upgradeInJsonObject.getString("url");
        int fileSize = 0;
        try {
            fileSize = Integer.parseInt(upgradeInJsonObject.getString("fsize"));
        } catch (NumberFormatException e) {
            throw new JSONException("file size is wrong");
        }

        String releaseNote = upgradeInJsonObject.getString("desc");
        String md5 = upgradeInJsonObject.getString("md5");

        int downloadedNum = 0;
        try {
            downloadedNum = upgradeInJsonObject.getInt("downcount");
        } catch (JSONException e) {
            LogUtils.loge(TAG, "no downcount");
        }

        String internalVer = upgradeInJsonObject.getString("vc");
        String version = upgradeInJsonObject.getString("vn");
        boolean recoveryUpdate = false;
        try{
            recoveryUpdate = upgradeInJsonObject.getBoolean("recovery");
        }catch (JSONException e) {
            // TODO: handle exception
            recoveryUpdate = false;
        }
        String releaseNoteUrl = "";
        try {
            releaseNoteUrl = upgradeInJsonObject.getString("releaseNote");
        } catch (JSONException je) {
            LogUtils.loge(TAG, "no releaseNote url");
        }

        boolean extPkg = "true".equals(upgradeInJsonObject.getString("extPkg"));

        OtaUpgradeInfo otaUpgradeInfo = new OtaUpgradeInfo();
        otaUpgradeInfo.setReleaseNote(releaseNote);
        otaUpgradeInfo.setReleaseNoteUrl(releaseNoteUrl);
        otaUpgradeInfo.setDownloadedPeopleNum(downloadedNum);
        otaUpgradeInfo.setDownloadurl(downloadUrl);
        otaUpgradeInfo.setFileSize(fileSize);
        otaUpgradeInfo.setInternalVer(internalVer);
        otaUpgradeInfo.setVersion(version);
        otaUpgradeInfo.setMd5(md5);
        otaUpgradeInfo.setExtPkg(extPkg);
        //add for recovery update
        otaUpgradeInfo.setRecoveryUpdate(recoveryUpdate);
        return otaUpgradeInfo;
    }

}
