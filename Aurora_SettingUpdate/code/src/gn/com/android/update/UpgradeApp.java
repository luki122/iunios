package gn.com.android.update;

import gn.com.android.update.business.Config;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.utils.BatteryUtil;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class UpgradeApp extends Application {

    public void onCreate() {
        super.onCreate();
        registerReceiver(betteryBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    private BroadcastReceiver betteryBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", 0);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);

            int currentLevel = level * 100 / scale;
            BatteryUtil.setBatteryLevel(currentLevel);

            boolean isCharging = false;
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                isCharging = true;
            }
            BatteryUtil.setCharging(isCharging);

            OtaUpgradeManager otaUpgradeManager = OtaUpgradeManager.getInstance(context);

            if (currentLevel < Config.AUTO_DOWNLOAD_CONNITUE_NEED_MIN_BATTERY_LEVEl && !isCharging) {
                otaUpgradeManager.onBatteryChangedToLow();

            } else {
                otaUpgradeManager.onBatteryChangedToHigh();
            }
        }
    };
}
