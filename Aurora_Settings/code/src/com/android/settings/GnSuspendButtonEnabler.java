package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;
import com.android.settings.Utils;
import android.content.ContentResolver;
import android.database.ContentObserver;
/**
 * 
 * @author chenml
 * @date 2013-05-29
 *
 */
public class GnSuspendButtonEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "GnSuspendButtonEnabler";
    private final Context mContext;
    private AuroraSwitch mSwitch;

    private static final String GN_SUSPEND_BUTTON = "gn_suspend_button";

    public GnSuspendButtonEnabler(Context context, AuroraSwitch suspendSwitch) {
        mContext = context;
        mSwitch = suspendSwitch;
    }
    public void resume() {
        mSwitch.setChecked(isSuspendButtonOn(mContext));
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }

    public static boolean isSuspendButtonOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                GN_SUSPEND_BUTTON, 0) != 0;
    }

    private void setSuspendButtonOn(boolean enabling) {
        Settings.System.putInt(mContext.getContentResolver(), GN_SUSPEND_BUTTON, 
                                enabling ? 1 : 0);
        mSwitch.setChecked(enabling);
        if(enabling){
            Intent intent = new Intent("com.gionee.floatingtouch.action.START_SERVICE");
            mContext.sendBroadcast(intent);
            Log.e(TAG, "send intent START_SERVICE");
        }else{
            Intent intent = new Intent("com.gionee.floatingtouch.action.STOP_SERVICE");
            mContext.sendBroadcast(intent);
            Log.e(TAG, "send intent STOP_SERVICE");
        }
        Log.d(TAG, "setSuspendButtonOn enabling ="+enabling);
    }

    public void setSwitch(AuroraSwitch suspendSwitch) {
        if (mSwitch == suspendSwitch) {
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = suspendSwitch;
        boolean isSuspendButton = isSuspendButtonOn(mContext);
        mSwitch.setChecked(isSuspendButton);
        mSwitch.setOnCheckedChangeListener(this);
        Log.d(TAG, "setSwitch = " +isSuspendButton);
    }

   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
       Log.d(TAG, "isChecked = " +isChecked);
            setSuspendButtonOn(isChecked);
    }
}
