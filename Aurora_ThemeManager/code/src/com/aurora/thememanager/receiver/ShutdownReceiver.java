package com.aurora.thememanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.thememanager.utils.download.AutoUpdateService;
import com.aurora.thememanager.utils.download.DownloadService;

/**
 * Created on 9/26/14.
 */
public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadService.pauseAllDownloads();
        AutoUpdateService.pauseAutoUpdate();
    }
}
