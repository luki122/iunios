package com.mediatek.wifi;

import aurora.app.AuroraActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceScreen;
//import android.preference.SwitchPreference;
import aurora.preference.*;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Log;

import com.android.settings.R;
//import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.Utils;
import com.mediatek.settings.ext.IWifiExt;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;

public class AdvancedWifiSettingsExt implements  OnPreferenceChangeListener{
    private static final String TAG = "AdvancedWifiSettingsExt";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_AUTO_JOIN = "wifi_ap_auto_join_available";
    private WifiManager mWifiManager;
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_CURRENT_IPV6_ADDRESS = "current_ipv6_address";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private AuroraSettingsPreferenceFragment mFragment;
    private AuroraActivity mActivity;
    private IWifiExt mExt;

    //bug fix for refresh notifiyOpenNetworks when turn on/off wifi
    private IntentFilter mIntentFilter;
    private AuroraSwitchPreference mNotifyOpenNetworks;

    // add for DHCPV6
    protected static final int NOT_FOUND_STRING = -1;
    private static final int ONLY_ONE_IP_ADDRESS = 1;
    private AuroraPreference mMacAddressPref;
    private AuroraPreference mIpAddressPref;
    private AuroraPreference mIpv6AddressPref;

    /// M: add Passpoint
    private PasspointR1Enabler mPasspointR1Enabler;

    /**
     * bug fix for refresh notifiyOpenNetworks when turn on/off wifi
     */
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
           if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
               int state = intent.getIntExtra(
                       WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
               if (state == WifiManager.WIFI_STATE_ENABLED) {
                   mNotifyOpenNetworks.setEnabled(true);
               } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                   mNotifyOpenNetworks.setEnabled(false);
               }
           }
       }
   };

    public AdvancedWifiSettingsExt(AuroraSettingsPreferenceFragment fragment) {
        Log.d(TAG , "AdvancedWifiSettingsExt");
        mFragment = fragment;
        if (fragment != null) {
            mActivity = (AuroraActivity) fragment.getActivity();
        }
    }

    /**
     * create plugin
     */
    public void onCreate() {
        Log.d(TAG , "onCreate");
        mExt = UtilsExt.getWifiPlugin(mActivity);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    /**
     * init some views: plugin view and ipv4 ipv6
     * @param cr
     */
    public void onActivityCreated(ContentResolver cr) {
        Log.d(TAG , "onActivityCreated");
        //init view
        mExt.initConnectView(mActivity, mFragment.getPreferenceScreen());
        mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        mExt.initPreference(cr);
        addWifiInfoPreference();
        mExt.initNetworkInfoView(mFragment.getPreferenceScreen());

    }

    /**
     * update views
     */
    public void onResume() {
        Log.d(TAG , "onResume");
        initPreferences();
        refreshWifiInfo();
        // register receiver
        mActivity.registerReceiver(mReceiver, mIntentFilter);

    }

    public void onPause() {
        // unregister receiver
        mActivity.unregisterReceiver(mReceiver);
    }

    private void initPreferences() {
    	
        mNotifyOpenNetworks = (AuroraSwitchPreference) mFragment.findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        mNotifyOpenNetworks.setOnPreferenceChangeListener(this);
        mNotifyOpenNetworks.setChecked(Settings.Global.getInt(mActivity.getContentResolver(),
                Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        mNotifyOpenNetworks.setEnabled(mWifiManager.isWifiEnabled());
        boolean excluded = WifiUtils.getCMCC();
//        Modify by penggangding begin
//        AuroraListPreference sleepPolicyPref = (AuroraListPreference) mFragment.findPreference(KEY_SLEEP_POLICY);
//        if (sleepPolicyPref != null) {
//            mExt.setSleepPolicyPreference(sleepPolicyPref,
//                    mFragment.getResources().getStringArray((Utils.isWifiOnly(mActivity)
//                    ? R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries)),
//                    mFragment.getResources().getStringArray(R.array.wifi_sleep_policy_values));
//        }
//       Modify by penggangding end
        //M: add for auto join feature
        AuroraSwitchPreference autoJoin = (AuroraSwitchPreference) mFragment.findPreference(KEY_AUTO_JOIN);
        if (mFragment.getResources().getBoolean(R.bool.auto_join_enable) && !excluded) {
            autoJoin.setChecked(Settings.Global.getInt(mActivity.getContentResolver(),
                    Settings.Global.WIFI_AUTO_JOIN, 0) == 1);
            autoJoin.setEnabled(mWifiManager.isWifiEnabled());
        } else {
        	if (autoJoin != null) {
        	    mFragment.getPreferenceScreen().removePreference(autoJoin);
        	}
        }
    }

    /**
     * update WifiInfo, ipv4/ipv6 and plugin's wifiInfo
     */
    private void refreshWifiInfo() {

        if (FeatureOption.MTK_DHCPV6C_WIFI) {
            //refresh wifi ip address preference
            String ipAddress = UtilsExt.getWifiIpAddresses();
            Log.d(TAG, "refreshWifiInfo, the ipAddress is : " + ipAddress);
            if (ipAddress != null) {
                String[] ipAddresses = ipAddress.split(", ");
                int ipAddressesLength = ipAddresses.length;
                Log.d(TAG, "ipAddressesLength is : " + ipAddressesLength);
                for (int i = 0; i < ipAddressesLength; i++) {
                    if (ipAddresses[i].indexOf(":") == NOT_FOUND_STRING) {
                        Log.d(TAG, "ipAddresses[i] is : " + ipAddresses[i]);
                        mIpAddressPref.setSummary(ipAddresses[i] == null ?
                                mActivity.getString(R.string.status_unavailable) : ipAddresses[i]);
                        if (ipAddressesLength == ONLY_ONE_IP_ADDRESS) {
                            mFragment.getPreferenceScreen().removePreference(mIpv6AddressPref);
                        }
                    } else  {
                    	//set one ipv6 address one line
                    	String ipSummary = "";
                    	if(ipAddresses[i] == null) {
                    		ipSummary = mActivity.getString(R.string.status_unavailable);
                    	} else {
                    		String[] ipv6Addresses = ipAddresses[i].split("; ");
                    		for (int j = 0; j < ipv6Addresses.length; j++) {
                    			ipSummary += ipv6Addresses[j]+"\n";
                    		}
                    	}
                    	mIpv6AddressPref.setSummary(ipSummary);
                        //mIpv6AddressPref.setSummary(ipAddresses[i] == null ?
                        //        mActivity.getString(R.string.status_unavailable) : ipAddresses[i]);
                        if (ipAddressesLength == ONLY_ONE_IP_ADDRESS) {
                            mFragment.getPreferenceScreen().removePreference(mIpAddressPref);
                        }
                    }
                }
            } else {
                mFragment.getPreferenceScreen().removePreference(mIpv6AddressPref);
                setDefaultIPAddress();
            }
        } else {
        	setDefaultIPAddress();
        }

        mExt.refreshNetworkInfoView();
    }

    private void setDefaultIPAddress()  {
        String ipAddress = Utils.getWifiIpAddresses(mActivity);
        Log.d(TAG,"default ipAddress = " + ipAddress);
        mIpAddressPref.setSummary(ipAddress == null ?
        		mActivity.getString(R.string.status_unavailable) : ipAddress);
    }
    /**
     *  add wifi mac & ip address preference
     */
    private void addWifiInfoPreference() {
        mMacAddressPref = mFragment.findPreference(KEY_MAC_ADDRESS);
        mIpAddressPref = mFragment.findPreference(KEY_CURRENT_IP_ADDRESS);
        mIpAddressPref.setSelectable(false	);
        AuroraPreferenceScreen screen = mFragment.getPreferenceScreen();
        // set macaddress and ipaddress's order
        int order = 0;
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
        	AuroraPreference preference = screen.getPreference(i);
            if (!KEY_MAC_ADDRESS.equals(preference.getKey()) && !KEY_CURRENT_IP_ADDRESS.equals(preference.getKey())) {
                preference.setOrder(order++);
            }
        }
        mMacAddressPref.setOrder(order++);
        if (mIpAddressPref != null) {
        	mIpAddressPref.setOrder(order++);
        }

        //add ipv6 preference
        if (FeatureOption.MTK_DHCPV6C_WIFI) {
            mIpAddressPref.setTitle(R.string.wifi_advanced_ipv4_address_title);
            mIpv6AddressPref = new AuroraPreference(mActivity, null, android.R.attr.preferenceInformationStyle);
            mIpv6AddressPref.setTitle(R.string.wifi_advanced_ipv6_address_title);
            mIpv6AddressPref.setKey(KEY_CURRENT_IPV6_ADDRESS);
            mFragment.getPreferenceScreen().addPreference(mIpv6AddressPref);
        }
        mFragment.getPreferenceScreen().removePreference(mIpv6AddressPref);
    }

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object object) {
	    boolean isChecked=(Boolean) object;
		if(preference.getKey().equals(KEY_NOTIFY_OPEN_NETWORKS))
		{
			 boolean isSuccess=Settings.Global.putInt(mActivity.getContentResolver(),
	                Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, (isChecked==true) ? 1 : 0);
        }
		return true;
	}

}
