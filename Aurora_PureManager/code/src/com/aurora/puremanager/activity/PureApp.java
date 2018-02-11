package com.aurora.puremanager.activity;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import com.aurora.puremanager.service.SmartPowerService;
import com.aurora.puremanager.utils.Log;

/**
 * Created by joy on 1/20/16.
 */
public class PureApp extends Application {

    private static final String TAG = "PureApp";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sp = getSharedPreferences(SmartPowerService.SETTINGS_VALUE, MODE_PRIVATE);
        if (sp.getInt(SmartPowerService.SMART_STATE, SmartPowerService.STATE_NORMAL)
                == SmartPowerService.STATE_SMART) {
            Log.e(TAG, "ENTER STATE_SMART");
            Intent intent = new Intent(this, SmartPowerService.class);
            intent.putExtra(SmartPowerService.SMART_STATE, true);
            startService(intent);
        }
    }
}
