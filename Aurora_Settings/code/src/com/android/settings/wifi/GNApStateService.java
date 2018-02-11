/*
 * Author: xuwen
 *
 * Date: 2012-07-09
 *
 * Description: Monitoring AP states.
 */

package com.android.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.app.Service;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.ScanResult;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.net.NetworkInfo.DetailedState;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.security.KeyStore;
import android.view.WindowManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.settings.R;

public class GNApStateService extends Service implements DialogInterface.OnClickListener {

    private final String TAG = "GNApStateService";

    private final String AP_PW_FAILED_FOUND = "found";

    private final String AP_PW_FAILED_CHECKED = "checked";

    private static final int WIFI_RESCAN_INTERVAL_MS = 6 * 1000;

    private Context mContext;

    private WifiConfiguration mConfig;

    private WifiManager mWifiManager;

    private AlertDialog mAlertDialog = null;

    private WifiManager.ActionListener mConnectListener;

    private AccessPoint mAccessPoint = null;

    private Map<String, AccessPoint> mAPMap = new HashMap<String, AccessPoint>();

    private Map<String, String> mAPDialogMap = new HashMap<String, String>();

    private boolean mIsContinueCheck = true;

    private boolean mIsPhoneStateIdle = true;

    private IntentFilter mFilter;

    private BroadcastReceiver mReceiver;

    private Scanner mScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mConnectListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
            }
        };

        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };

        getApplicationContext().registerReceiver(mReceiver, mFilter);
        mScanner = new Scanner();

        // Register a monitor for Monitoring the phone state
        TelephonyManager telManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        updateAccessPoints();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        getApplicationContext().unregisterReceiver(mReceiver);
        mScanner.pause();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void updateAccessPoints() {

        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                if (!GNApPwDialog.getContinueState() || !mIsPhoneStateIdle) {
                    return;
                }
                mScanner.resume();
                constructAccessPoints();
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                resetParameters();
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                break;
            default:
                break;
        }

        mScanner.pause();
    }

    private void checkApState() {
        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config != null && config.status == WifiConfiguration.Status.DISABLED) {
                    switch (config.disableReason) {
                        case WifiConfiguration.DISABLED_AUTH_FAILURE:
                            showDialog(config);
                            break;
                    }
                }
            }
        }
    }

    private void constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (null != configs) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(mContext, config);
                if (-1 == accessPoint.getLevel()) {
                    accessPoint.setLevel(0);
                }
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (null != results) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    accessPoint.networkId = INVALID_NETWORK_ID;
                    String strSSID = "\"" + accessPoint.ssid + "\"";
                    if (null == mAPMap.get(strSSID)) {
                        mAPMap.put(strSSID, accessPoint);
                    }
                    checkApState();
                }
            }
        }
    }

    private class Multimap<K, V> {
        private HashMap<K, List<V>> store = new HashMap<K, List<V>>();

        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V> emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<V>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == WifiDialog.BUTTON_SUBMIT) {
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            GNApPwDialog.setContinueState(false);
        }
    }

    private void showDialog(WifiConfiguration config) {
        if (null == mAPDialogMap.get(config.SSID)) {
            mAPDialogMap.put(config.SSID, AP_PW_FAILED_FOUND);
        }

        AccessPoint accessPoint = mAPMap.get(config.SSID);
        if (GNApPwDialog.getContinueState() && null != accessPoint
                && AP_PW_FAILED_FOUND.equalsIgnoreCase(mAPDialogMap.get(config.SSID))) {

            if (WifiDialog.getDialogState()) {
                return;
            }

            mAccessPoint = accessPoint;
            GNApPwDialog.setAccessPoint(mAccessPoint);
            Intent serviceIntent = new Intent(mContext, GNApPwDialog.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(serviceIntent);
            collapseStatusBar();
            mAPDialogMap.put(config.SSID, AP_PW_FAILED_CHECKED);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getDetailedState() == DetailedState.CONNECTED) {
                resetParameters();
                GNApPwDialog.setContinueState(false);
            } else if (info.getDetailedState() == DetailedState.DISCONNECTED) {
                GNApPwDialog.setContinueState(true);
            }
        }
    }

    protected void collapseStatusBar() {
        // TODO Auto-generated method stub collapsePanels()
        IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager
                .getService(Context.STATUS_BAR_SERVICE));
        try {
            statusBarService.collapsePanels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetParameters() {
        mAPDialogMap.clear();
        mAPMap.clear();
        GNApPwDialog.setContinueState(true);
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            Log.e("xuwen", "aaaaaaaaaaaaaaaaaaaaaaaaaa");
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                if (null != mContext) {
                    Toast.makeText(mContext, R.string.wifi_fail_to_scan, Toast.LENGTH_LONG).show();
                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    mIsPhoneStateIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    mIsPhoneStateIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    mIsPhoneStateIdle = false;
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
}
