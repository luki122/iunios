
package gn.com.android.update.business;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gn.com.android.update.R;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.ui.DetailsInfoActivity;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.NotificationUtil;
import gn.com.android.update.utils.OtaInent;
import gn.com.android.update.utils.Util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.format.DateFormat;
import android.util.Log;

import com.aurora.ota.database.RepoterManager;
import com.aurora.ota.database.RepoterManager.ReportType;
import com.aurora.ota.reporter.Constants;
import com.aurora.ota.reporter.ReporterItem;
import com.aurora.ota.reporter.ReporterService;

public class OtaReceiver extends BroadcastReceiver {
    public static final int START_FROM_BOOT = 0x102;
    public static final String KEY_START_FROM_BOOT = "KEY_START_FROM_BOOT";

    public static final String NAME_FOR_STORR_REPORT_TIME = "report";
    public static final int MODE_PRIVATE = 0;

    public static final String KEY_SHUTDOWN_TIME = "shut_down";
    public static final String KEY_START_UP_TIME = "start_up";

    public static final String KEY_NET_CHANGED = "net";
    public static final int VALUE_NET_CHANGED = 100;
    public static final String TAG = "OtaReceiver";

    private RepoterManager mReportManager;
    private Context mContext;
    private String mOsVersion;
    private String tempOsVersion;
    
    private int mReportCycle = 10;

    
    private final int mReportHour = 12;
    private final int mReportMinute = 30;
    
    public static long startupTime = 0;
    public static long shutdownTime = 0;
    
//    private final int mToday
    @Override
    public void onReceive(Context context, Intent intent) {
    	LogUtils.log(TAG,"onReceive  action =    "  +  intent.getAction());
        mContext = context;
        mReportManager = RepoterManager.getInstance(context);
        if (intent == null) {
            return;
        }
        ReceiverStrategy.getInstance().strategy(context, intent);
        int bootNumber = mReportManager.getBootNumber(Constants.KEY_BOOT_NUMBER);
        LogUtils.log(TAG,"onReceive  bootNumber =    "  +  bootNumber);
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
        	LogUtils.log(TAG, "Action:"+action);
            mOsVersion = Util.getGioneeVersion();
            tempOsVersion = mReportManager.getOSVersion();
            //获取上次开关机的时间
            startupTime = mReportManager.getTime(Constants.KEY_START_UP_TIME);
            shutdownTime = mReportManager.getTime(Constants.KEY_SHUTDOWN_TIME);
            //保存这次的开机时间
            mReportManager.storeTime(Constants.KEY_START_UP_TIME);
            
            LogUtils.log(TAG, "old version:"+tempOsVersion);
            LogUtils.log(TAG, "new version:"+mOsVersion);
            if(mOsVersion.equals(tempOsVersion)){
//                mReportManager.storeBootNumber(Constants.KEY_BOOT_NUMBER,0);
                 reportDelay();
            }else{
                mReportManager.setReportType(ReportType.Update);
                mReportManager.storeOSVersion();
                Constants.NEED_LOCATION = true;
            }
            Intent serverIntent = new Intent();
            serverIntent.setClass(context, ReporterService.class);
//            serverIntent.putExtra(Constants.KEY_START_FROM_BOOT, Constants.START_FROM_BOOT);
//            if (bootNumber == 0) {
//                mReportManager.storeTime(Constants.KEY_START_UP_TIME);
//            }
            context.startService(serverIntent);
        } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
            shutdownAction();
        } else if (action.equals(Constants.ACTION_CONNECTION_CHANGED)) {
        	LogUtils.log(TAG,"go connection change ");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                	mReportManager.setReportType(ReportType.Normal); //网络切换的时候如果service还没有启动，则不去生成记录
                    if (isServiceRunning(mContext,ReporterService.class.getName())) {
                    	LogUtils.log(TAG,"ReporterService  is Running ");
                        Intent i = new Intent();
                        i.setAction(Constants.ACTION_FROM_NET_CHANGE);
                        mContext.sendBroadcast(i);
                    } else {
                    	LogUtils.log(TAG,"ReporterService  is not Running ");
                    	 startupTime = mReportManager.getTime(Constants.KEY_START_UP_TIME);
                        Intent serverIntent = new Intent();
                        serverIntent.putExtra(Constants.KEY_NET_CHANGED, Constants.VALUE_NET_CHANGED);
                        serverIntent.setClass(mContext, ReporterService.class);
                        mContext.startService(serverIntent);
                    }

                }
            }).start();

        }

    }
    private  boolean isServiceRunning(Context ctx,String filePath) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ctx.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (filePath.equalsIgnoreCase(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    private void shutdownAction(){
        Calendar currentCal = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        
        currentCal.setTimeInMillis(currentTime);
        
        int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCal.get(Calendar.MINUTE);
        //保存关机时间
        mReportManager.storeTime(Constants.KEY_SHUTDOWN_TIME);
        if((currentHour < mReportHour) || ((currentHour < mReportHour) && (currentMinute < mReportMinute))){
            mReportManager.storeTodayReported(false);
        }
    }
    
    private void reportDelay(){
/*        Calendar currentCal = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();
        
        currentCal.setTimeInMillis(currentTime);
        
        int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentCal.get(Calendar.MINUTE);
        
        int year = currentCal.get(Calendar.YEAR);
        int month = currentCal.get(Calendar.MONTH)+1;
        int day = currentCal.get(Calendar.DAY_OF_MONTH);
        int currentDate = year+month+day;
        
        
        LogUtils.log(TAG, "hour:"+currentHour+" minute:"+currentMinute);
        if((currentHour > mReportHour) || ((currentHour == mReportHour) && (currentMinute > mReportMinute))){
            boolean todayReported = mReportManager.getTodayReported();
            int date = mReportManager.getReportDate();
            LogUtils.log(TAG, "todayReported:"+todayReported);
            if((!todayReported) ||(date < currentDate)){
                mReportManager.setReportType(ReportType.Delay);
            }
        }*/
        mReportManager.setReportType(ReportType.Delay);  //开机第一次必须上送
   }
}

class ReceiverStrategy {
    private static final String TAG = "ReceiverStrategyFactory";
    private volatile static ReceiverStrategy sReceiverStrategy = null;
    private static final Map<String, IReceiveStrategy> S_RECEIVER_MAP = new HashMap<String, IReceiveStrategy>();

    private ReceiverStrategy() {
        S_RECEIVER_MAP.clear();
        S_RECEIVER_MAP.put("android.net.conn.CONNECTIVITY_CHANGE", new NetConnectChange());
        S_RECEIVER_MAP.put(OtaInent.AUTO_CHECK_ACTION, new AutoCheck());
        S_RECEIVER_MAP.put(OtaInent.LAUNCHER_START_ACTION, new LauncherStart());
        S_RECEIVER_MAP.put(Intent.ACTION_BOOT_COMPLETED, new BootComplete());
    }

    public synchronized static ReceiverStrategy getInstance() {
        if (sReceiverStrategy == null) {
            sReceiverStrategy = new ReceiverStrategy();
        }
        return sReceiverStrategy;
    }

    public void strategy(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.logd(TAG, "action = " + action);
        if (S_RECEIVER_MAP.containsKey(action)) {
            S_RECEIVER_MAP.get(action).strategy(context, intent);
        }
    }

    private final static class NetConnectChange implements IReceiveStrategy {
        private static final String DATEFORMATE = "yyyy-MM-dd";

        @Override
        public void strategy(Context context, Intent intent) {
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            LogUtils.log(TAG, "NetConnectChange    networkInfo  ==null?   "  +( networkInfo==null));
            if (networkInfo == null) {
                return;
            }
            LogUtils.log(TAG, "NetConnectChange    isConnected()?    "  +networkInfo.isConnected());
            if (!networkInfo.isConnected()) {
                return;
            }

            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            LogUtils.log(TAG, "NetConnectChange    wifiState      "  + wifiState);
            if (State.CONNECTED == wifiState) {
                onWifiConnected(context);
            }
        }

        private void onWifiConnected(Context context) {
            String lastDate = OtaSettings.getLastWifiConnectDate(context, "");
            String date = DateFormat.format(DATEFORMATE, System.currentTimeMillis()).toString();
            boolean isAutoCheckAfterBootComplete = OtaSettings.isAutoCheckAfterBootComplete(
                    context, false);
            if (date.equals(lastDate) && !isAutoCheckAfterBootComplete) {
                return;
            }

            OtaSettings.setWifiConnectDate(context, date);

            OtaUpgradeManager otaUpgradeManager = OtaUpgradeManager.getInstance(context);

            OtaUpgradeState otaUpgradeState = otaUpgradeManager.getOtaUpgradeState();

            LogUtils.logd(TAG, "onWifiConnected() otaUpgradeState = " + otaUpgradeState);

            if (isAutoCheckAfterBootComplete) {
                OtaSettings.setAutoCheckAfterBootComplete(context, false);

                if (otaUpgradeState == OtaUpgradeState.DOWNLOAD_PAUSE) {
                    handleFirstWifiConnected(context, otaUpgradeManager);
                    return;
                }

            }

            if (otaUpgradeState == OtaUpgradeState.INITIAL
                    || otaUpgradeState == OtaUpgradeState.READY_TO_DOWNLOAD) {

                try {
                    otaUpgradeManager.autoCheckOtaVersion();
                } catch (ErrorStateException e) {
                    e.printStackTrace();
                }
            }

        }

        private void handleFirstWifiConnected(Context context, OtaUpgradeManager otaUpgradeManager) {
            if (OtaSettings.IsAutoDownload(context, false)) {
                if (otaUpgradeManager.canStartAutoDownload()) {
                    otaUpgradeManager.startAutoDownload();
                }
            }
        }

    }

    private final static class AutoCheck implements IReceiveStrategy {
        private static final String TAG = "AutoCheck";

        @Override
        public void strategy(Context context, Intent intent) {
            LogUtils.logd(TAG, "AutoCheck->strategy()");

            // if (HttpUtils.isMobileNetwork(context)) {
            if (HttpUtils.isNetworkAvailable(context)) {
                try {
                    OtaUpgradeManager.getInstance(context).autoCheckOtaVersion();
                } catch (ErrorStateException e) {

                    e.printStackTrace();
                }
            }
        }

    }

    private final static class LauncherStart implements IReceiveStrategy {
        private static final String TAG = "LauncherStart";

        @Override
        public void strategy(Context context, Intent intent) {
            LogUtils.logd(TAG, "LauncherStart->strategy()");

            String appName = intent.getStringExtra("appname");
            if (appName == null) {
                return;
            }

            if (appName.equals(context.getPackageName())) {
                OtaUpgradeManager otaUpgradeManager = OtaUpgradeManager.getInstance(context);
                OtaUpgradeInfo otaUpgradeInfo = otaUpgradeManager.getOtaUpgradeInfo();
                String currentVersion = Util.getInternalVersion();
                String newVersion = otaUpgradeInfo.getInternalVer();
                if (newVersion.compareTo(currentVersion) > 0) {
                    otaUpgradeManager.sendVersionBroadcastToLauncher(true);
                }
            }
        }

    }

    private final static class BootComplete implements IReceiveStrategy {

        @Override
        public void strategy(Context context, Intent intent) {
            OtaSettings.setAutoCheckAfterBootComplete(context, true);
            if (OtaSettings.getAutoCheckEnabled(context, false)) {
                OtaSettings.startAutoCheck(context);
            }
            if (OtaSettings.getNotifyNewVersionAfterBootComplete(context, false)) {
                OtaSettings.setNotifyNewVersionAfterBootComplete(context, false);
                String currentVersion = Util.getInternalVersion();
                String newVersion = OtaSettings.getLastUpgradeVersion(context, "");
                if (currentVersion.compareTo(newVersion) == 0) {
                	OtaSettings.setShowNotify(context, false);
                    notifyNewVersionAfterBootComplete(context);
                }
            }
        }

        private void notifyNewVersionAfterBootComplete(Context context) {
            String releaseNoteUrl = OtaSettings.getCurrVerReleasenoteUrl(context, "");

            Intent activityIntent = new Intent("gn.com.android.update.action.detail");
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra(DetailsInfoActivity.MROE_INFO_URL, releaseNoteUrl);
            activityIntent.putExtra(DetailsInfoActivity.IS_CURRENT_VERSION_INFO, true);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            Builder builder = new Builder(context);
            builder.setSmallIcon(R.drawable.stat_sys_download_anim0);
            builder.setAutoCancel(true);
            builder.setTicker(context.getString(R.string.upgrade_successful_title));
            builder.setVibrate(new long[] {
                    100, 250, 100, 400
            });
            builder.setContentTitle(context.getString(R.string.upgrade_successful_title));
            builder.setContentText(context.getString(R.string.upgrade_successful_message));
            builder.setContentIntent(contentIntent);

            NotificationUtil.showNotication(context, builder.getNotification(),
                    Config.VERSION_NOTIFICATION_ID);
        }
    }

    private interface IReceiveStrategy {
        void strategy(Context context, Intent intent);
    }
}
