/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import aurora.preference.AuroraPreference;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;


// Aurora <likai> <2013-10-02> add begin
import android.view.View.OnClickListener;

import com.android.settings.wifi.WifiSettings;;
// Aurora <likai> <2013-10-02> add end
import java.util.ArrayList;
import android.os.Parcelable;

class AccessPoint extends AuroraPreference {
    // Aurora <likai> <2013-10-02> add begin
    private AccessPoint mAccessPoint;
	private WifiSettings mFragment;
    private TextView mStatusView;
	// Aurora <likai> <2013-10-02> add end

    static final String TAG = "Settings.AccessPoint";

    private static final String KEY_DETAILEDSTATE = "key_detailedstate";
    private static final String KEY_WIFIINFO = "key_wifiinfo";
    private static final String KEY_SCANRESULT = "key_scanresult";
    private static final String KEY_CONFIG = "key_config";

    private static final int INDEX_KEY_CONFIG = 0;
    private static final int INDEX_KEY_SCANRESULT = 1;
    private static final int INDEX_KEY_WIFIINFO = 2;

    private static final int[] STATE_SECURED = {
        R.attr.state_encrypted
    };
    private static final int[] STATE_NONE = {};

    /** These values are matched in string arrays -- changes must be kept in sync */
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    String ssid;
    String bssid;
    int security;
    int networkId;
    boolean wpsAvailable = false;

    PskType pskType = PskType.UNKNOWN;

    private WifiConfiguration mConfig;
    /* package */ScanResult mScanResult;

    private int mRssi;
    private WifiInfo mInfo;
    private DetailedState mState;

    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public String getSecurityString(boolean concise) {
        Context context = getContext();
        switch(security) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                    context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                            context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                            context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                            context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                    context.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    AccessPoint(Context context, WifiConfiguration config) {
        super(context);
        // Aurora <likai> <2013-10-02> modify begin
        //setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        setLayoutResource(R.layout.aurora_preference_wifi_ap_layout);
        setWidgetLayoutResource(R.layout.aurora_preference_widget_wifi_ap);
        mAccessPoint = this;
        // Aurora <likai> <2013-10-02> modify end

        loadConfig(config);
        refresh();
    }

    AccessPoint(Context context, ScanResult result) {
        super(context);
        // Aurora <likai> <2013-10-02> modify begin
        //setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        setLayoutResource(R.layout.aurora_preference_wifi_ap_layout);
        setWidgetLayoutResource(R.layout.aurora_preference_widget_wifi_ap);
        mAccessPoint = this;
        // Aurora <likai> <2013-10-02> modify end

        loadResult(result);
        refresh();
    }

    AccessPoint(Context context, Bundle savedState) {
        super(context);
        // Aurora <likai> <2013-10-02> modify begin
        //setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        setLayoutResource(R.layout.aurora_preference_wifi_ap_layout);
        setWidgetLayoutResource(R.layout.aurora_preference_widget_wifi_ap);
        mAccessPoint = this;
        // Aurora <likai> <2013-10-02> modify end

//        mConfig = savedState.getParcelable(KEY_CONFIG);
        mConfig = (WifiConfiguration)savedState.getParcelableArrayList("wifiParacelable").get(INDEX_KEY_CONFIG);
        if (mConfig != null) {
            loadConfig(mConfig);
        }

//        mScanResult = (ScanResult) savedState.getParcelable(KEY_SCANRESULT);
        mScanResult = (ScanResult)savedState.getParcelableArrayList("wifiParacelable").get(INDEX_KEY_SCANRESULT);
        if (mScanResult != null) {
            loadResult(mScanResult);
        }

//        mInfo = (WifiInfo) savedState.getParcelable(KEY_WIFIINFO);
        mInfo = (WifiInfo)savedState.getParcelableArrayList("wifiParacelable").get(INDEX_KEY_WIFIINFO);
        if (savedState.containsKey(KEY_DETAILEDSTATE)) {
            mState = DetailedState.valueOf(savedState.getString(KEY_DETAILEDSTATE));
        }
        update(mInfo, mState);
    }

    public void saveWifiState(Bundle savedState) {
        ArrayList<Parcelable> wifiParcelable = new ArrayList<Parcelable>();
        wifiParcelable.add(mConfig);
        wifiParcelable.add(mScanResult);
        wifiParcelable.add(mInfo);

        savedState.putParcelableArrayList("wifiParacelable", wifiParcelable);

//        savedState.putParcelable(KEY_WIFIINFO, mInfo);
//        savedState.putParcelable(KEY_CONFIG, mConfig);
//        savedState.putParcelable(KEY_SCANRESULT, mScanResult);
//        savedState.putParcelable(KEY_WIFIINFO, mInfo);
        if (mState != null) {
            savedState.putString(KEY_DETAILEDSTATE, mState.toString());
        }
    }

    private void loadConfig(WifiConfiguration config) {
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);
        networkId = config.networkId;
        mRssi = Integer.MAX_VALUE;
        mConfig = config;
//        Log.d("gd","Config ssid :"+ssid+"  bssid:"+bssid+"  security:"+security +  "  networkId:"+networkId+ " mRssi:"+mRssi);
    }

    private void loadResult(ScanResult result) {
        ssid = result.SSID;
        bssid = result.BSSID;
        security = getSecurity(result);
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
        if (security == SECURITY_PSK)
            pskType = getPskType(result);
        networkId = -1;
        mRssi = result.level;
        mScanResult = result;
//        Log.d("gd","Result ssid :"+ssid+"  bssid:"+bssid+"  security:"+security +  "  wpsAvailable:"+wpsAvailable+ " mRssi:"+mRssi);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
        // Aurora <likai> <2013-10-02> modify begin
        /*
        ImageView signal = (ImageView) view.findViewById(R.id.signal);
        if (mRssi == Integer.MAX_VALUE) {
        	signal.setImageDrawable(null);
        } else {
        	signal.setImageLevel(getLevel());
        	signal.setImageResource(R.drawable.wifi_signal);
        	signal.setImageState((security != SECURITY_NONE) ? STATE_SECURED : STATE_NONE, true);
        }
        */
        WifiSettings.PrintLog(TAG, " BindView ");
        if(android.text.TextUtils.isEmpty(getSummary()))
        {
        	 final LinearLayout accesspoint_layout = (LinearLayout) view.findViewById(R.id.accesspoint_layout);
        	 if(accesspoint_layout!=null)
        	 {
        		 ViewGroup.LayoutParams params = accesspoint_layout.getLayoutParams();
        		 params.height = com.aurora.utils.DensityUtil.dip2px(getContext(),43);
        		 accesspoint_layout.setLayoutParams(params);
        	 }
        }
        ImageView signalView = (ImageView) view.findViewById(R.id.wifi_signal);
        if (mRssi == Integer.MAX_VALUE) {
        	signalView.setImageDrawable(null);
            signalView.setVisibility(View.GONE);
        } else {
        	signalView.setImageLevel(getLevel()-1);
        	signalView.setImageResource(R.drawable.aurora_wifi_signal);
        	signalView.setImageState((security != SECURITY_NONE) ? STATE_SECURED : STATE_NONE, true);
            signalView.setVisibility(View.VISIBLE);
        }
        // Aurora <likai> <2013-10-02> modify end

        // Aurora <likai> <2013-10-02> add begin
        ImageView detailView = (ImageView) view.findViewById(R.id.wifi_detail);
        detailView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mFragment != null) {
					mFragment.doPreferenceTreeClick(mAccessPoint, true);
				}
			}
		});

        mStatusView = (TextView) view.findViewById(R.id.wifi_status);
        //refresh();

        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mFragment != null) {
//                	if(mAccessPoint.ssid!=null)
//                	{
//                		WifiSettings.currentSSID=mAccessPoint.ssid;
//                	}
                    mFragment.doPreferenceTreeClick(mAccessPoint, false);
                }
            }
        });
		// Aurora <likai> <2013-10-02> add end
    }

    // Aurora <likai> <2013-10-02> add begin
    public void setFragment(WifiSettings fragment) {
        mFragment = fragment;
    }

    private void setStatus(String status) {
        if (mStatusView != null) {
            mStatusView.setText(status);
            mStatusView.setVisibility(View.VISIBLE);
        }
    }
    // Aurora <likai> <2013-10-02> add end

    @Override
    public int compareTo(AuroraPreference preference) {
        if (!(preference instanceof AccessPoint)) {
            return 1;
        }
        AccessPoint other = (AccessPoint) preference;
        // Active one goes first.
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        // Reachable one goes before unreachable one.
        if ((mRssi ^ other.mRssi) < 0) {
            return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
        }
        // Configured one goes before unconfigured one.
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                int oldLevel = getLevel();
                mRssi = result.level;
                if (getLevel() != oldLevel) {
                    notifyChanged();
                }
            }
            // This flag only comes from scans, is not easily saved in config
            if (security == SECURITY_PSK) {
                pskType = getPskType(result);
            }
            refresh();
            return true;
        }
        return false;
    }

    void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;
        if (info != null && networkId != WifiConfiguration.INVALID_NETWORK_ID
                && networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
//            mRssi = info.getRssi();  
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mState = null;
            refresh();
        }
        if (reorder) {
            notifyHierarchyChanged();
        }
    }

    int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
		int CurrentSignal=WifiManager.calculateSignalLevel(mRssi, 5);
if(CurrentSignal==0)
{
return 1;
}
        return CurrentSignal;
    }

    WifiConfiguration getConfig() {
        return mConfig;
    }

    WifiInfo getInfo() {
        return mInfo;
    }

    DetailedState getState() {
        return mState;
    }

    static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    /** Updates the title and summary; may indirectly call notifyChanged()  */
    private void refresh() {
        setTitle(ssid);

        Context context = getContext();
        if (mState != null) { // This is the active connection
            setSummary(Summary.get(context, mState));
        	//IUNI <penggangding><2014-06-17> added for  begin
        	SharedPreferences.Editor stateEditor = WifiSettings.getWifiSettingsInstance().getEdit();
        	if(mState.toString().equals("CONNECTED"))
        	{
        		stateEditor.putInt(ssid, 2);
        	}else
        	{
        		stateEditor.putInt(ssid, 1);
        	}
        	stateEditor.commit();
        	//IUNI <penggangding><2014-06-17> added for end
        } else if (mRssi == Integer.MAX_VALUE) { // Wifi out of range
//            setSummary(context.getString(R.string.wifi_not_in_range));
        	setSummary("");
        } else if (mConfig != null && mConfig.status == WifiConfiguration.Status.DISABLED) {
            switch (mConfig.disableReason) {
                case WifiConfiguration.DISABLED_AUTH_FAILURE:
//                	    setSummary(context.getString(R.string.wifi_disabled_password_failure));
                	    setSummary("");
                    break;
                case WifiConfiguration.DISABLED_DHCP_FAILURE:
                case WifiConfiguration.DISABLED_DNS_FAILURE:
                    //setSummary(context.getString(R.string.wifi_disabled_network_failure));;
                	setSummary("");
                    break;
                case WifiConfiguration.DISABLED_UNKNOWN_REASON:
                    //setSummary(context.getString(R.string.wifi_disabled_generic));
                	setSummary("");
            }
        } else { // In range, not disabled.
/*            StringBuilder summary = new StringBuilder();
            if (mConfig != null) { // Is saved network
                summary.append(context.getString(R.string.wifi_remembered));
            }

            if (security != SECURITY_NONE) {
                String securityStrFormat;
                if (summary.length() == 0) {
                    securityStrFormat = context.getString(R.string.wifi_secured_first_item);
                } else {
                    securityStrFormat = context.getString(R.string.wifi_secured_second_item);
                }
                summary.append(String.format(securityStrFormat, getSecurityString(true)));
            }

            if (mConfig == null && wpsAvailable) { // Only list WPS available for unsaved networks
                if (summary.length() == 0) {
                    summary.append(context.getString(R.string.wifi_wps_available_first_item));
                } else {
                    summary.append(context.getString(R.string.wifi_wps_available_second_item));
                }
            }
            setSummary(summary.toString());*/
        	setSummary("");
        }
    }

    /**
     * Generate and save a default wifiConfiguration with common values.
     * Can only be called for unsecured networks.
     * @hide
     */
    protected void generateOpenNetworkConfig() {
        if (security != SECURITY_NONE)
            throw new IllegalStateException();
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = AccessPoint.convertToQuotedString(ssid);
        mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
    }
    
    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
    void setLevel(int nLevel) {
        mRssi = nLevel;
    }
    //Gionee <xuwen><2013-07-09> added for CR00834689 end
}
