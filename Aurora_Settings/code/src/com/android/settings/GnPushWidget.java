// Gionee <wangyaohui><2013-05-30> add
package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;

import com.android.settings.Utils;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import android.os.SystemProperties;
import com.mediatek.telephony.TelephonyManagerEx;
import android.content.BroadcastReceiver;
import android.os.UserHandle;
import aurora.preference.AuroraPreference;

public class GnPushWidget implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "GnPushWidget";
    private Context mContext;

    private AuroraSwitch mSwitch;

    private static final int EVENT_SERVICE_STATE_CHANGED = 3;
    private static final String GN_FANFAN_WIDGET_AUTO_PUSH = "gn_fanfan_widget_auto_push";

    public GnPushWidget(Context context, AuroraSwitch pushSwitch) {
        mContext = context;
        mSwitch = pushSwitch;
    }

    public void resume() {
        //mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(isGnFanfanWidgetAutoPushOn(mContext));
        Log.d(TAG, "resume()");
    }

    public void pause() {
        // mSwitch.setOnCheckedChangeListener(null);
        Log.d(TAG, "pause()");
    }

    public static boolean isGnFanfanWidgetAutoPushOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                GN_FANFAN_WIDGET_AUTO_PUSH, 0) != 0;
    }
    public void setSwitch(AuroraSwitch pushSwitch) {
		Log.d(TAG, "setSwitch");
		mSwitch = pushSwitch;
		mSwitch.setChecked(isGnFanfanWidgetAutoPushOn(mContext));
        mSwitch.setOnCheckedChangeListener(this);        
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged = " + isChecked);
		Settings.System.putInt(mContext.getContentResolver(), GN_FANFAN_WIDGET_AUTO_PUSH, 
                                isChecked ? 1 : 0);
    }

}
