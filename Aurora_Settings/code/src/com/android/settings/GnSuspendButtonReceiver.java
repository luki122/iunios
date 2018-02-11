package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 
 * @author chenml
 * @date 2013-06-14
 *
 */
public class GnSuspendButtonReceiver extends BroadcastReceiver {
    
   private static final String TAG = "GnSuspendButtonReceiver";
   
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        Log.e(TAG, "action = "+action);
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            boolean isChecked = GnSuspendButtonEnabler.isSuspendButtonOn(context);
            Log.e(TAG, "isChecked = "+isChecked);
            
            if(isChecked) {
                Intent intent1 = new Intent("com.gionee.floatingtouch.action.START_SERVICE");
                context.sendBroadcast(intent1);
            }
            Log.e(TAG, "send com.gionee.floatingtouch.action.START_SERVICE");
            
        }
    }

}
