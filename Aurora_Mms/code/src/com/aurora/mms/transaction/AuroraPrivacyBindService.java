package com.aurora.mms.transaction;
// Aurora xuyong 2014-10-23 created for privacy feature 
import com.android.mms.transaction.MessagingNotification;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuroraPrivacyBindService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                AuroraPrivacyUtils.setCurrentAccountId(AuroraPrivacyBindService.this);
                // Aurora xuyong 2014-11-12 modified for bug #9761 start
                MessagingNotification.blockingUpdateAllNotifications(AuroraPrivacyBindService.this);
                // Aurora xuyong 2014-11-12 modified for bug #9761 end
            }
            
        }).start();
        return Service.START_NOT_STICKY;
    }

}
