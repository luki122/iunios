package com.aurora.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AutoUpdateService;

/**
 * Created on 9/26/14.
 */
public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppDownloadService.pauseAllDownloads();
        AutoUpdateService.pauseAutoUpdate();
    }
}
