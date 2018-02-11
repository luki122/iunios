package com.android.transaction;
// Aurora xuyong 2014-10-23 created for privacy feature
import com.privacymanage.service.AuroraPrivacyUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.privacymanage.service.AuroraPrivacyUtils;

public class AuroraPrivacyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        if ("com.aurora.privacymanage.SWITCH_ACCOUNT".equals(action)) {
             // enter or quit privacy
             Intent startService = new Intent(context, AuroraPrivacyBindService.class);
             context.startService(startService);
        } else if ("com.aurora.privacymanage.DELETE_ACCOUNT".equals(action)) {
            
        } else if ("com.aurora.privacy.contact.UPDATE".equals(action)) {
            
        }
    }

}
