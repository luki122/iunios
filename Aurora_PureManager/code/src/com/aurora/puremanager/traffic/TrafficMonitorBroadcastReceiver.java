//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aurora.puremanager.utils.mConfig;

public class TrafficMonitorBroadcastReceiver extends BroadcastReceiver {

    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private final static String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private final static String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private int mSimIndex;
    private int mSimCount = Constant.SIM_COUNT;
    private boolean[] mTrafficSettings = new boolean[mSimCount];
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            Debuger.print("sim card change!");
            simCardChange(mContext);
            resetSimCardChangeInfo(mContext);
            notifySimCardChange(mContext);
        } else if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {
            Debuger.print("network change!");
            networkChange(mContext);

        } else if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
//            networkControl(mContext);

        }
    }

    private void simCardChange(Context context) {
//        TrafficSettingsPrefsFragment.commitTrafficNotiAction(context);
    }

    private void resetSimCardChangeInfo(Context context) {
        SharedPreferences share = context.getSharedPreferences(mConfig.DEFAULT_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        SIMInfoWrapper.setEmptyObject(context);
        final SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int count = simInfo.getInsertedSimCount();
        for (int i = 0; i < count; i++) {
            try {
                String currentSimIMSIId = MobileTemplate.getSubscriberId(context, i);
                int simIndex = simInfo.getInsertedSimInfo().get(i).mSlot;
                String key = "sim" + simIndex;
                String oldSimIMSIId = share.getString(key, "");
                if (oldSimIMSIId.isEmpty()) {
                    share.edit().putString(key, currentSimIMSIId).commit();
                } else if (!currentSimIMSIId.equals(oldSimIMSIId)) {
                    Log.d("sim", currentSimIMSIId + "," + oldSimIMSIId + ",no equal");
                    share.edit().putString(key, currentSimIMSIId).commit();
                    String[] data = TrafficPreference.getPreference(context, simIndex);
                    data[3] = "0";
                    TrafficPreference.setPreference(context, simIndex, data);
                } else {
                    Log.d("sim", currentSimIMSIId + "," + oldSimIMSIId + ",equal");
                }
            } catch (Exception ex) {
                Debuger.print("Exception :" + ex.toString());
            }
        }
    }
    
    private void notifySimCardChange(Context context) {
    	Intent b = new Intent(mConfig.ACTION_SIM_CHANGE);
    	context.sendBroadcast(b);
    }

    private void networkChange(final Context context) {
        SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
        SIMInfoWrapper.setEmptyObject(context);
        onTrafficSettings(context, share);
        final SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        if (networkCheck(context, share, simInfo)) {

//            onBindSaveingTraffic(context, simInfo);
            startTrafficMonitor(context);

        } else {

            stopTrafficMonitor(context);

        }
    }

//    private void networkControl(Context context) {
//        for (int i = 0; i < Constant.NETWORK_NUM; i++) {
//            SparseIntArray disableArray = NetworkDisableUids.getInstance().getDisableUids(i);
//            for (int j = 0; j < disableArray.size(); j++) {
//                AppNetworkControl.getInstance(context).reflectNetworkControlAction(disableArray.keyAt(j), i,
//                        true);
//            }
//        }
//    }

    private void startTrafficMonitor(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                TrafficProcessorService.processIntent(context, false);
                TrafficProcessorService.processIntent(context, true);
            }
        }).start();
    }

    private void stopTrafficMonitor(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                TrafficProcessorService.processIntent(context, false);
            }
        }).start();
    }

    private boolean networkCheck(Context context, SharedPreferences share, SIMInfoWrapper simInfo) {
        if (simInfo.getInsertedSimCount() == 0) {
            return false;
        }

        if (simInfo.isWiFiActived() || !simInfo.gprsIsOpenMethod("getMobileDataEnabled")) {
            return false;
        }

        mSimIndex = getSimCardNo(context, simInfo);

        if (mSimIndex < 0) {
            return false;
        }

        return true;
    }

    private int getSimCardNo(Context context, SIMInfoWrapper simInfo) {
        int simIndex;
        simIndex = getActivatedSimCardNo(context, simInfo);
        if (simIndex < 0) {
            simIndex = getDefaultNoneActivatedSimCardNo(context, simInfo);
        }
        return simIndex;
    }

    private int getActivatedSimCardNo(Context context, SIMInfoWrapper simInfo) {

        int activatedSimIndex = -1;
        if (simInfo.gprsIsOpenMethod("getMobileDataEnabled")) {
            activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        }
        return activatedSimIndex;

    }

    private int getDefaultNoneActivatedSimCardNo(Context context, SIMInfoWrapper simInfo) {

        int noneActivatedSimIndex = -1;
        // Gionee: mengdw <2015-09-21> modify for CR01557552 begin
        if (null != simInfo && simInfo.getInsertedSimCount() > 1) {
        // Gionee: mengdw <2015-09-21> modify for CR01557552 end
            noneActivatedSimIndex = 0;
        } else {
            // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
            if (null != simInfo && simInfo.getInsertedSimInfo().size() > 0) {
                noneActivatedSimIndex = simInfo.getInsertedSimInfo().get(0).mSlot;
            }
            // Gionee: mengdw <2015-08-25> modify for CR01543192 end
        }
        return noneActivatedSimIndex;

    }

    private void onTrafficSettings(Context context, SharedPreferences share) {

        String[] strSettings = new String[mSimCount];
        String[] strReset = new String[mSimCount];
        boolean[] isReset = new boolean[mSimCount];

        initMonitorFlag(share, strSettings, strReset, isReset);
        resetMonitorFlag(context, share, strSettings, strReset, isReset);
    }

    private void initMonitorFlag(SharedPreferences share, String[] strSettings, String[] strReset,
            boolean[] isReset) {

        for (int index = 0; index < mSimCount; index++) {
            strSettings[index] = TrafficPreference.getSimSetting(index);
            mTrafficSettings[index] = share.getBoolean(strSettings[index], false);

            strReset[index] = TrafficPreference.getSimReset(index);
            isReset[index] = share.getBoolean(strReset[index], false);
        }
    }

    private void resetMonitorFlag(Context context, SharedPreferences share, String[] strSettings,
            String[] strReset, boolean[] isReset) {

        for (int index = 0; index < mSimCount; index++) {

            if (mTrafficSettings[index]) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);

                String[] data = TrafficPreference.getPreference(context, index);
                if (!data[2].isEmpty() && day == Integer.valueOf(data[2])) {
                    if (!isReset[index]) {
                        data[3] = "0";
                        TrafficPreference.setPreference(context, index, data);
                        share.edit().putBoolean(strReset[index], true).commit();
                        share.edit().putBoolean(TrafficPreference.getSimStopWarning(index), false).commit();
                    }
                } else {
                    share.edit().putBoolean(strReset[index], false).commit();
                }
            }
        }
    }

    private void onBindSaveingTraffic(Context context, SIMInfoWrapper simInfo) {
//        if (isPermitBindService(context)) {
//            int activatedSim = simInfo.getSimIndex_CurrentNetworkActivated();
//            String imsi = MobileTemplate.getSubscriberId(context, activatedSim);
//            if (imsi == null) {
//                return;
//            }
//
//            AppTrafficService trafficService = AppTrafficService.getInstance(context);
//            trafficService.setIMSI(imsi);
//            trafficService.unbindTrafficService();
//            trafficService.bindTrafficService();
//            updateSaveingTrafficState(context);
//        }
    }

    private boolean isPermitBindService(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(mConfig.DEFAULT_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        return preferences.getBoolean("traffica_save_switch", false);
    }

    private void updateSaveingTrafficState(Context context) {
        SharedPreferences share = context.getSharedPreferences(mConfig.DEFAULT_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        share.edit().putBoolean("traffic_imsi", true).commit();
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end