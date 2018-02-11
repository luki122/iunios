package gn.com.android.update;


import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import android.app.IntentService;
import android.content.Intent;

public class UpdateDaemonService extends IntentService {
    private static String TAG = "UpdateDaemonService";
    public UpdateDaemonService() {
        super(null);
    }
    
    public UpdateDaemonService(String name) {
        super(name);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        LogUtils.logd(TAG, "UpdateDaemonService---onStart");
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String filePath = intent.getStringExtra("filePath");
            String fileMd5 = intent.getStringExtra("fileMd5");
            if (filePath == null || filePath.length() == 0) {
                LogUtils.logd(TAG, "invald filePath");
                return;
            }
            
            if(!Util.isAsci(filePath)){
                LogUtils.logd(TAG, "path contain chenese language or inlagel zifu");
                return;
            }
            
            File file = new File(filePath);
            if (!file.exists()) {
                LogUtils.logd(TAG, "file not exist");
                return;
            }

            String fileRealMd5 = FileUtil.getFileMd5(file);
            LogUtils.logd(TAG, "fileRealMd5="+fileRealMd5);
            if (fileMd5 == null || "".equals(fileMd5)) {
                sentUpgradeIntent(file);

            } else {
                if (fileMd5.equals(fileRealMd5)) {
                    sentUpgradeIntent(file);
                }else {
                    LogUtils.loge(TAG, "file verfy fail");
                }
            }
        } catch (RuntimeException e) {
            LogUtils.loge(TAG, "Unable to get MD5");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopSelf();
        }
    }

    public void sentUpgradeIntent(File file) {
        LogUtils.logd(TAG, "filePath = " + file.getPath());
        Intent upgradeIntent = new Intent("android.intent.action.MASTER_CLEAR");
        upgradeIntent.putExtra("OTAUpdate", true);
        try {
            String filePath = file.getPath();
            int index = filePath.indexOf("/", 1);
            index = filePath.indexOf("/", index + 1);
            filePath = filePath.substring(index + 1);

            if (StorageUtil.isFileInInternalStoarge(this, file)) {
                filePath = "/sdcard/" + filePath;
            } else {
                filePath = "/sdcard2/" + filePath;
            }

            upgradeIntent.putExtra("FileName", filePath);
            sendBroadcast(upgradeIntent);

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
