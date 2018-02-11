/*
 * Author: xuwen
 *
 * Date: 2012-07-09
 *
 * Description: display dialog to make user reset the password.
 */

package com.android.settings.wifi;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.security.KeyStore;
import android.hardware.usb.UsbManager;

import com.android.settings.R;

public class GNApPwDialog extends Activity implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

    private final String TAG = "GNApPwDialog";

    private final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";

    private final String AP_PW_FAILED_FOUND = "found";

    private final String AP_PW_FAILED_CHECKED = "checked";

    private static final int WIFI_RESCAN_INTERVAL_MS = 6 * 1000;

    private Context mContext;

    private WifiConfiguration mConfig;

    private WifiManager mWifiManager;

    private WifiDialog mDialog;

    private WifiManager.ActionListener mConnectListener;

    public static AccessPoint mAccessPoint = null;

    private static boolean mIsContinueCheck = true;

    private boolean mIsUsbConnected = false;

    private boolean mIsSubmitBtnClicked = false;

    private IntentFilter mFilter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (ACTION_PHONE_STATE.equals(action)) {
                mIsContinueCheck = false;
                GNApPwDialog.this.finish();
            } else if (UsbManager.ACTION_USB_STATE.equals(action)) {
                boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
                if (connected) {
                    mIsUsbConnected = connected;
                }

                if (mIsUsbConnected && !connected) {
                    mIsUsbConnected = false;
                    GNApPwDialog.this.finish();
                }
            }
        }
    };

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_ap_pw_dialog);
        mContext = this;
        mFilter = new IntentFilter();
        mFilter.addAction(ACTION_PHONE_STATE);
        mFilter.addAction(UsbManager.ACTION_USB_STATE);

        mContext.registerReceiver(mReceiver, mFilter);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if (null != mAccessPoint) {
            mDialog = new WifiDialog(mContext, this, mAccessPoint, false);
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    WifiDialog.setDialogState(false);
                    if (!mIsSubmitBtnClicked) {
                        mIsContinueCheck = false;
                    }
                    GNApPwDialog.this.finish();
                }
            });
            mIsSubmitBtnClicked = false;
            mDialog.show();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO Auto-generated method stub
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "mReceiver is not registered!!" + e);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == WifiDialog.BUTTON_SUBMIT) {
            mIsSubmitBtnClicked = true;
            submit(mDialog.getController());
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            mIsContinueCheck = false;
        }
    }

    void submit(WifiConfigController configController) {
        final WifiConfiguration config = configController.getConfig();

        if (null == config) {
            if (null != mAccessPoint && !requireKeyStore(mAccessPoint.getConfig())
                    && INVALID_NETWORK_ID != mAccessPoint.networkId) {
                mWifiManager.connect(mAccessPoint.networkId, mConnectListener);
            } else {
                Log.e(TAG, "Connect AP failed!");
            }
        } else {
            mWifiManager.connect(config, mConnectListener);
        }
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiConfigController.requireKeyStore(config)
                && KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
            return true;
        }
        return false;
    }

    public static void setAccessPoint(AccessPoint accessPoint) {
        mAccessPoint = accessPoint;
    }

    public static boolean getContinueState() {
        return mIsContinueCheck;
    }

    public static void setContinueState(boolean bState) {
        mIsContinueCheck = bState;
    }
}
