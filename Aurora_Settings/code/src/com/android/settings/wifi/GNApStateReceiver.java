/*
 * Author: xuwen
 *
 * Date: 2012-07-09
 *
 * Description: Receiver the wifi states.
 */

package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.Log;

public class GNApStateReceiver extends BroadcastReceiver {

    private static boolean mApPwDialogSupport = "yes".equals(android.os.SystemProperties.get(
            "ro.gn.appwdialog.support", "no"));

    @Override
    public void onReceive(Context context, Intent intent) {
        String strAction = intent.getAction();

        if (!mApPwDialogSupport || WifiDialog.getDialogState()) {
            return;
        }

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(strAction)
                || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(strAction)) {
            Intent serviceIntent = new Intent(context, GNApStateService.class);
            context.startService(serviceIntent);
        }
    }
}
