package com.aurora.mms.transaction;
// Aurora xuyong 2014-10-23 created for privacy feature
import com.aurora.mms.util.Utils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class AuroraPrivacyBroadcastReceiver extends BroadcastReceiver {
    

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        // Aurora xuyong 2016-01-28 added for bug #18254 start
        Utils.updateWidget(context);
        // Aurora xuyong 2016-01-28 added for bug #18254 end
        // Aurora xuyong 2014-11-20 modified for bug #9915 start
        if ("com.aurora.privacymanage.SWITCH_ACCOUNT".equals(action) || "com.aurora.privacymanage.DELETE_ACCOUNT".equals(action)) {
        // Aurora xuyong 2014-11-20 modified for bug #9915 end
             // enter or quit privacy
             Intent startService = new Intent(context, AuroraPrivacyBindService.class);
             context.startService(startService);
             Utils.clearAllInstance();
        // Aurora xuyong 2014-11-20 modified for bug #9915 start
        }/* else if ("com.aurora.privacymanage.DELETE_ACCOUNT".equals(action)) {
            // do nothing
        }*/
        // Aurora xuyong 2014-11-20 modified for bug #9915 end
    }
    

}
