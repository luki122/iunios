package com.android.settings.wifi;

import com.android.settings.R;
import com.android.settings.TetherSettings;
import com.android.settings.WirelessSettings;

import java.util.ArrayList;

import aurora.app.AuroraAlertDialog;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * SoftAP
 *@param numble
 *@return
 *@exception 
 *@author penggangding
 *@Time
 */

public class AuroraWifiApEnabler 
{
	private final Context mContext;
	private final String MTAG;
    private final AuroraSwitchPreference mCheckBox;
    private final CharSequence mOriginalSummary;

    private WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;

    ConnectivityManager mCm;
    private String[] mWifiRegexs;
    /* Indicates if we have to wait for WIFI_STATE_CHANGED intent */
    private boolean mWaitForWifiStateChange;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            Log.d(MTAG, " action="+action);
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) 
            {
                handleWifiApStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED));
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) 
            {
                if (mWaitForWifiStateChange == true) 
                {
                     handleWifiStateChanged(intent.getIntExtra(
                         WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
                }
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) 
            {
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) 
            {
                enableWifiCheckBox();
            }

        }
    };

    public AuroraWifiApEnabler(Context context, AuroraSwitchPreference checkBox ,String TAG) 
    {
        mContext = context;
        MTAG=TAG;
        mCheckBox = checkBox;
        mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);
        mWaitForWifiStateChange = true;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mWifiRegexs = mCm.getTetherableWifiRegexs();

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    public void resume()
    {
    	Log.d(MTAG, " resume ");
        mContext.registerReceiver(mReceiver, mIntentFilter);
        enableWifiCheckBox();
    }

    public void pause() 
    {
    	Log.d(MTAG, " pause ");
        mContext.unregisterReceiver(mReceiver);
    }

    private void enableWifiCheckBox() 
    {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if(!isAirplaneMode)
        {
            mCheckBox.setEnabled(true);
        } else {
            mCheckBox.setEnabled(false);
        }
    }

    public void setSoftapEnabled(boolean enable)
    {
        final ContentResolver cr = mContext.getContentResolver();
        int wifiSavedState = 0;
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) 
        {
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }
        /**
         * Check if we have to wait for the WIFI_STATE_CHANGED intent
         * before we re-enable the Checkbox.
         */
        if (!enable) 
        {
            mWaitForWifiStateChange = false;
            try 
            {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }

            if (wifiSavedState == 1)
            {
                 mWaitForWifiStateChange = true;
            }
        }

        if (mWifiManager.setWifiApEnabled(null, enable)) 
        {
            /* Disable here, enabled on receiving success broadcast */
            mCheckBox.setEnabled(false);
        } else 
        {
        }

        /**
         * If needed, restore Wifi on tether disable
         */
        if (!enable) 
        {
            if (wifiSavedState == 1) 
            {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    private void handleWifiApStateChanged(int state) 
    {
    	Log.d(MTAG, "handleWifiApStateChanged -> state="+state);
        switch (state)
        {
            case WifiManager.WIFI_AP_STATE_ENABLING:
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /**
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
                mCheckBox.setChecked(true);
                /* Doesnt need the airplane check */
                mCheckBox.setEnabled(true);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                mCheckBox.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                mCheckBox.setChecked(false);
                if (mWaitForWifiStateChange == false)
                {
                    enableWifiCheckBox();
                }
                break;
            default:
                mCheckBox.setChecked(false);
                enableWifiCheckBox();
        }
    }

    private void handleWifiStateChanged(int state) 
    {
    	Log.d(MTAG, " handleWifiStateChanged  -> state="+state);
        switch (state)
        {
            case WifiManager.WIFI_STATE_ENABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
                enableWifiCheckBox();
                break;
            default:
        }
    }
    
    public boolean getSwitchChecked()
    {
    	return mWifiManager.isWifiApEnabled() ? true : false ;
    }
}
