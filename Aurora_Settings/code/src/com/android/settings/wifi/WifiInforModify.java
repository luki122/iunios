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

import com.android.settings.ProxySelector;
import com.android.settings.R;
import com.android.settings.bootanimation.BootanimationPreview;
import com.android.settings.wifi.WifiConfigController.EnterpriseField;
import android.R.anim;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.security.Credentials;
import android.security.KeyStore;
import android.widget.Button;
import aurora.app.AuroraActivity;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.ViewGroup;
import android.view.KeyEvent;
import aurora.app.AuroraAlertDialog;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraSpinner;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageButton;
import android.text.TextUtils;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.CustomAuroraActionBarItem;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import android.view.View.OnTouchListener;
/**
 * Preference to configure the SSID and security settings for Access Point
 * operation
 */

public class WifiInforModify extends AuroraActivity implements TextWatcher,
		AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
		DialogInterface.OnDismissListener {

	// 密码
	// private static final String FILE_WIFI_SUPPLICANT =
	// "/data/misc/wifi/wpa_supplicant.conf";
	// byte[] wifiSupplicantData = getWifiSupplicant(FILE_WIFI_SUPPLICANT);
	// private static final byte[] EMPTY_DATA = new byte[0];
	// public EnterpriseField password = new EnterpriseField("password");
    private static final String TAG="WifiInforModify";
	public static final int OPEN_INDEX = 0;
	public static final int WPA_INDEX = 1;
	public static final int WPA2_INDEX = 2;

	/* This value comes from "wifi_ip_settings" resource array */
	private static final int DHCP = 0;
	private static final int STATIC_IP = 1;

	/* These values come from "wifi_proxy_settings" resource array */
	public static final int PROXY_NONE = 0;
	public static final int PROXY_STATIC = 1;

	// e.g. AccessPoint.SECURITY_NONE
	private int mAccessPointSecurity;
	private EditText mPasswordView;

	private AuroraSpinner mIpSettingsSpinner;
	private TextView mIpAddressView;
	private Button mIgnoreWifiApButton;
	private TextView mGatewayView;
	private TextView mNetworkPrefixLengthView;
	private TextView mDns1View;
	private TextView mDns2View;

	private AuroraSpinner mProxySettingsSpinner;
	private TextView mProxyHostView;
	private TextView mProxyPortView;
	private TextView mProxyExclusionListView;

	private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
	private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
	private IpAssignment mIpAssignmentInto = IpAssignment.UNASSIGNED;
	private ProxySettings mProxySettingsInto = ProxySettings.UNASSIGNED;
	private LinkProperties mLinkProperties = new LinkProperties();

	private View mView;
	private Dialog mShowDialog;
	private CharSequence inputLimit;
	private AuroraEditText mPassword;
	private AuroraSpinner mSecuritySpinner;
	private WifiManager mWifiManager;
	private AuroraActionBar mActionBar;
	private WifiManager.ActionListener mSaveListener;
	private Handler mTextViewChangedHandler;
	WifiConfiguration mWifiConfig;
	static AccessPoint mAccessPoint;
	boolean onItemSelectedChanged = false;
    private static final String DEFAULT_PASSWORD="??????????";

	//
	// public class EnterpriseField {
	// private String varName;
	// private String value;
	//
	// private EnterpriseField(String varName) {
	// this.varName = varName;
	// this.value = null;
	// }
	//
	// public void setValue(String value) {
	// this.value = value;
	// }
	//
	// public String varName() {
	// return varName;
	// }
	//
	// public String value() {
	// return value;
	// }
	// }

	public WifiInforModify() {
	}

	public WifiInforModify(AccessPoint accessPoint) {
		mAccessPoint = accessPoint;
	}

	public WifiConfiguration getConfig() {

		WifiConfiguration config = new WifiConfiguration();

		/**
		 * TODO: SSID in WifiConfiguration for soft ap is being stored as a raw
		 * string without quotes. This is not the case on the client side. We
		 * need to make things consistent and clean it up
		 */
		config = mAccessPoint.getConfig();
		if (canEnableSubmitIfAppropriate()) {
			
			
			// Aurora <penggangding> <2014-05-22> add begin 
			// 保存密码修改
			switch (mAccessPointSecurity) {
			case AccessPoint.SECURITY_NONE:
				break;

			case AccessPoint.SECURITY_WEP:
				if (mPasswordView.length() != 0) {
					int length = mPasswordView.length();
					String password = mPasswordView.getText().toString(); // WEP-40,
																			// WEP-104,
					if (!password.equals(DEFAULT_PASSWORD))
					{
					if ((length == 10 || length == 26 || length == 58)
							&& password.matches("[0-9A-Fa-f]*")) {
						config.wepKeys[0] = password;
					} else {
						config.wepKeys[0] = '"' + password + '"';
						}
					}
				}
				break;

			case AccessPoint.SECURITY_PSK:
				if (mPasswordView.length() != 0) {
					String password = mPasswordView.getText().toString();
					if (!password.equals(DEFAULT_PASSWORD))
					{
					if (password.matches("[0-9A-Fa-f]{64}")) {
						config.preSharedKey = password;
					} else {
						config.preSharedKey = '"' + password + '"';
						}
					}
				}
				break;
			case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                if(!Build.MODEL.equals("GT-I9500"))
                {
			    config.enterpriseConfig.setEapMethod(config.enterpriseConfig.getEapMethod());
			    config.enterpriseConfig.setPhase2Method(config.enterpriseConfig.getPhase2Method());
			    config.enterpriseConfig.setCaCertificateAlias(config.enterpriseConfig.getCaCertificateAlias());
			    config.enterpriseConfig.setClientCertificateAlias(config.enterpriseConfig.getClientCertificateAlias());
			    config.enterpriseConfig.setIdentity(config.enterpriseConfig.getIdentity());
				if (mPasswordView.length() > 0) {
					String password = mPasswordView.getText().toString();
					if (!password.equals(DEFAULT_PASSWORD))
					{
					config.enterpriseConfig.setPassword(mPasswordView.getText()
							.toString());
					}
				}
//				if (mPasswordView.length() > 0) {
//					password.setValue(mPasswordView.getText().toString());
                }
				break;
			default:
				return null;
			} //
			// Aurora <penggangding> <2014-05-22> add end \
			 
			config.proxySettings = mProxySettings;
			config.ipAssignment = mIpAssignment;
			config.linkProperties = new LinkProperties(mLinkProperties);
		}
		return config;
	}

	boolean canEnableSubmitIfAppropriate() {
		boolean enabled = false;
//		 boolean passwordInvalid = false;
//		 if (mPasswordView != null
//		 && ((mAccessPointSecurity == AccessPoint.SECURITY_WEP &&
//		 mPasswordView
//		 .length() == 0) || (mAccessPointSecurity == AccessPoint.SECURITY_PSK
//		 && mPasswordView
//		 .length() < 8))) {
//		 passwordInvalid = true;
//		 }
        WifiSettings.PrintLog(TAG, "  ipAndProxyFieldsAreValid: "+ipAndProxyFieldsAreValid());
		if (ipAndProxyFieldsAreValid()) {
			enabled = true;
		} else {
			enabled = false;
		}
		return enabled;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_wifi_info_modify_layout,
				AuroraActionBar.Type.Dashboard);
		mActionBar = ((AuroraActivity) this).getAuroraActionBar();
//		mActionBar.getOkButton().setTextColor(Color.parseColor("#BFBFBF"));
		mActionBar.getOkButton().setEnabled(false);
//		mActionBar.getOkButton().setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent event) {
//					if(event.getAction() == MotionEvent.ACTION_UP)
//					{
//						mActionBar.getOkButton().setTextColor(Color.parseColor("#585858"));
//					}
//					if(event.getAction() == MotionEvent.ACTION_DOWN)
//					{
//						mActionBar.getOkButton().setTextColor(Color.parseColor("#ff9999"));
//					}
//				return false;
//			}
//		});
		mActionBar.getOkButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				save();
				Intent intent = new Intent("android.net.wifi.WIFI_INFOR_MODIFY");
				sendBroadcast(intent);
				finish();
				// onBackPressed();
			}
		});
		mActionBar.getCancelButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (validate()) {
					showDialog();
				} else {
					onBackPressed();
				}
			}
		});

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mSaveListener = new WifiManager.ActionListener() {
			public void onSuccess() {
				finish();
			}

			public void onFailure(int reason) {
				// Toast.makeText(wifi, R.string.wifi_failed_save_message,
				// Toast.LENGTH_SHORT).show();
				finish();
			}
		};

		mIpSettingsSpinner = (AuroraSpinner) findViewById(R.id.ip_settings);
		mIpSettingsSpinner.setOnItemSelectedListener(this);
		mProxySettingsSpinner = (AuroraSpinner) findViewById(R.id.proxy_settings);
		mProxySettingsSpinner.setOnItemSelectedListener(this);
		mAccessPointSecurity = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE
				: mAccessPoint.security;
		mTextViewChangedHandler = new Handler();

//		mIpAssignmentInto = (mIpSettingsSpinner != null && mIpSettingsSpinner
//				.getSelectedItemPosition() == STATIC_IP) ? IpAssignment.STATIC
//				: IpAssignment.DHCP;
//		mProxySettingsInto = (mProxySettingsSpinner != null && mProxySettingsSpinner
//				.getSelectedItemPosition() == PROXY_STATIC) ? ProxySettings.STATIC
//				: ProxySettings.NONE;
        if(mAccessPoint == null)
        {
        	return;
        }
		if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
			WifiConfiguration config = mAccessPoint.getConfig();
			if (config.ipAssignment == IpAssignment.STATIC) {
				mIpSettingsSpinner.setSelection(STATIC_IP);
				mIpAssignmentInto = IpAssignment.STATIC;
			} else {
				mIpSettingsSpinner.setSelection(DHCP);
				mIpAssignmentInto = IpAssignment.DHCP;
			}

			if (config.proxySettings == ProxySettings.STATIC) {
				mProxySettingsSpinner.setSelection(PROXY_STATIC);
				mProxySettingsInto = ProxySettings.STATIC;
			} else {
				mProxySettingsSpinner.setSelection(PROXY_NONE);
				mProxySettingsInto = ProxySettings.NONE;
			}
		}
		if (mAccessPoint.getConfig().preSharedKey != null) {

		}
		// Aurora <penggangding> <2014-05-22> add begin  
		showSecurityFields();
		// Aurora <penggangding> <2014-05-22> add end 
		showProxyFields();
		showIpConfigFields();
	}
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void afterTextChanged(Editable s) {
		mTextViewChangedHandler.post(new Runnable() {
			public void run() {
				// Aurora <penggangding> <2014-05-22> add begin 				
				displayPWrules();
				// Aurora <penggangding> <2014-05-22> add end
				canEnableSubmitIfAppropriate();
				isProxOrIpChange();
				validate();
			}
		});
	}
	// Aurora <penggangding> <2014-05-22> add begin 
	private void displayPWrules()
	{
            if(mPasswordView == null)
            {
                return;
            }
	    if(!mPasswordView.getText().toString().equals(DEFAULT_PASSWORD))
	    {
	    	String tempSecret = mPasswordView.getText().toString();
	    	if(tempSecret.contains(DEFAULT_PASSWORD))
	        {
	        	mPasswordView.setText(tempSecret.subSequence(10, mPasswordView.getText().toString().length()));
			            mPasswordView.setSelection(mPasswordView.length());
	        }
	    	else if(tempSecret.contains("?????????"))
	    	{
	        	mPasswordView.setText(tempSecret.subSequence(9, mPasswordView.getText().toString().length()));
	            mPasswordView.setSelection(mPasswordView.length());
	    }


			}
	}
	// Aurora <penggangding> <2014-05-22> add end 

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == mIpSettingsSpinner) {
			showIpConfigFields();
			validate();
		} else if (parent == mProxySettingsSpinner) {
			showProxyFields();
			validate();
		}
		// showSecurityFields();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void save() {
		mWifiConfig = getConfig();
		if (mWifiConfig != null) {
			mWifiManager.save(mWifiConfig, mSaveListener);
			if(mWifiConfig.networkId != INVALID_NETWORK_ID)
			{
				mWifiManager.connect(mWifiConfig.networkId, mSaveListener);
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (validate()) {
				showDialog();
			} else {
				onBackPressed();
			}
			return true;
		}
		return false;
	}

	public boolean validate() {
		boolean isChange = false;
		WifiConfiguration config = null;
		if (mAccessPoint != null
				&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
			config = mAccessPoint.getConfig();
		}
		if(config==null)
		{
			return isChange;
		}
		// Aurora <penggangding> <2014-05-22> add begin 
		if(mPasswordView!=null)
		{
			WifiSettings.PrintLog(TAG, " PasswordView:"+ mPasswordView.getText().toString() + "  security:"+mAccessPoint.security);
			if(AccessPoint.SECURITY_EAP==mAccessPoint.security)
			{
				if(mPasswordView.getText().toString().equals("")||mPasswordView.getText().toString().equals(DEFAULT_PASSWORD))
				{
					isChange=false;
				}else
				{
					isChange=true;
				}	
			}else
			{
			if(mPasswordView.getText().toString().equals("")||mPasswordView.getText().toString().equals(DEFAULT_PASSWORD)||mPasswordView.getText().toString().length()<=7)
			{
				isChange=false;
			}else
			{
				isChange=true;
			}
		}
		}
		// Aurora <penggangding> <2014-05-22> add end 
		mIpAssignment = (mIpSettingsSpinner != null && mIpSettingsSpinner
				.getSelectedItemPosition() == STATIC_IP) ? IpAssignment.STATIC
				: IpAssignment.DHCP;
		mProxySettings = (mProxySettingsSpinner != null && mProxySettingsSpinner
				.getSelectedItemPosition() == PROXY_STATIC) ? ProxySettings.STATIC
				: ProxySettings.NONE;
		if (!(mIpAssignmentInto.equals(mIpAssignment))
				|| !(mProxySettingsInto.equals(mProxySettings))) {
			isChange = true;
		}

		ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
		if ((null != proxyProperties) && (null != mProxyHostView) && (null != mProxyPortView) && (null != mProxyExclusionListView)) {
			if (!(mProxyHostView.getText().toString().equals(proxyProperties
					.getHost().toString()))
					|| !(mProxyPortView.getText().toString().equals(Integer
							.toString(proxyProperties.getPort()).toString()))
					|| !(mProxyExclusionListView.getText().toString()
							.equals(proxyProperties.getExclusionList()
									.toString()))) {
				isChange = true;
			}
		}
		
		LinkProperties linkProperties = config.linkProperties;
		if(null != linkProperties) {
			Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses()
					.iterator();
			if (iterator.hasNext() && (null != mIpAddressView) && (null != mNetworkPrefixLengthView)) {
				LinkAddress linkAddress = iterator.next();
				if (!(mIpAddressView.getText().toString().equals(linkAddress
						.getAddress().getHostAddress().toString()))
						|| !(mNetworkPrefixLengthView.getText().toString()
								.equals(Integer.toString(
										linkAddress.getNetworkPrefixLength())
										.toString()))) {
					isChange = true;
				}
			}
			for (RouteInfo route : linkProperties.getRoutes()) {
				if (route.isDefaultRoute() && (null != mGatewayView)) {
					if (!mGatewayView.getText().toString()
							.equals(route.getGateway().getHostAddress().toString())) {
						isChange = true;
					}
					break;
				}
			}
			Iterator<InetAddress> dnsIterator = linkProperties.getDnses()
					.iterator();
			if (dnsIterator.hasNext() && (null != mDns1View)) {
				if (!mDns1View.getText().toString()
						.equals(dnsIterator.next().getHostAddress().toString())) {
					isChange = true;
				}
			}
			if (dnsIterator.hasNext() && (null != mDns2View)) {
				if (!mDns2View.getText().toString()
						.equals(dnsIterator.next().getHostAddress().toString())) {
					isChange = true;
				}
			}
		}

		return isChange;
	}

	private void showDialog() {
		mShowDialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.wifi_info_modify)
				.setMessage(R.string.wifi_info_modify_message)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, this).show();
		mShowDialog.setOnDismissListener(this);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// Assuming that onClick gets called first
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			onBackPressed();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void showProxyFields() {
		WifiConfiguration config = null;

		findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);

		if (mAccessPoint != null
				&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
			config = mAccessPoint.getConfig();
		}
		isProxOrIpChange();


		if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
			// mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
			findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
			if (mProxyHostView == null) {
				mProxyHostView = (TextView) findViewById(R.id.proxy_hostname);
				mProxyHostView.addTextChangedListener(this);
				mProxyPortView = (TextView) findViewById(R.id.proxy_port);
				mProxyPortView.addTextChangedListener(this);
				mProxyExclusionListView = (TextView) findViewById(R.id.proxy_exclusionlist);
				mProxyExclusionListView.addTextChangedListener(this);
			}
			if (config != null) {
				ProxyProperties proxyProperties = config.linkProperties
						.getHttpProxy();
				if (proxyProperties != null) {
					mProxyHostView.setText(proxyProperties.getHost());
					mProxyPortView.setText(Integer.toString(proxyProperties
							.getPort()));
					mProxyExclusionListView.setText(proxyProperties
							.getExclusionList());
				}
			}
		} else {
			// mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
			findViewById(R.id.proxy_fields).setVisibility(View.GONE);
		}

	}

	private void isProxOrIpChange()
	{
		if(validate())
		{
//			mActionBar.getOkButton().setTextColor(Color.parseColor("#585858"));
			mActionBar.getOkButton().setEnabled(true);
		}else
		{
//			mActionBar.getOkButton().setTextColor(Color.parseColor("#ff9999"));
			mActionBar.getOkButton().setEnabled(false);
		}
	}
	private void showIpConfigFields() {
		WifiConfiguration config = null;

		findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

		if (mAccessPoint != null
				&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
			config = mAccessPoint.getConfig();
		}

		isProxOrIpChange();

		
		
		if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
			findViewById(R.id.staticip).setVisibility(View.VISIBLE);
			if (mIpAddressView == null) {
				mIpAddressView = (TextView) findViewById(R.id.ipaddress);
				mIpAddressView.addTextChangedListener(this);
				mGatewayView = (TextView) findViewById(R.id.gateway);
				mGatewayView.addTextChangedListener(this);
				mNetworkPrefixLengthView = (TextView) findViewById(R.id.network_prefix_length);
				mNetworkPrefixLengthView.addTextChangedListener(this);
				mDns1View = (TextView) findViewById(R.id.dns1);
				mDns1View.addTextChangedListener(this);
				mDns2View = (TextView) findViewById(R.id.dns2);
				mDns2View.addTextChangedListener(this);
			}
			if (config != null) {
				LinkProperties linkProperties = config.linkProperties;
				Iterator<LinkAddress> iterator = linkProperties
						.getLinkAddresses().iterator();
				if (iterator.hasNext()) {
					LinkAddress linkAddress = iterator.next();
					mIpAddressView.setText(linkAddress.getAddress()
							.getHostAddress());
					mNetworkPrefixLengthView.setText(Integer
							.toString(linkAddress.getNetworkPrefixLength()));
				}

				for (RouteInfo route : linkProperties.getRoutes()) {
					if (route.isDefaultRoute()) {
						mGatewayView.setText(route.getGateway()
								.getHostAddress());
						break;
					}
				}

				Iterator<InetAddress> dnsIterator = linkProperties.getDnses()
						.iterator();
				if (dnsIterator.hasNext()) {
					mDns1View.setText(dnsIterator.next().getHostAddress());
				}
				if (dnsIterator.hasNext()) {
					mDns2View.setText(dnsIterator.next().getHostAddress());
				}
			}
		} else {
			findViewById(R.id.staticip).setVisibility(View.GONE);
		}
	}

	private boolean ipAndProxyFieldsAreValid() {
		mLinkProperties.clear();
		mIpAssignment = (mIpSettingsSpinner != null && mIpSettingsSpinner
				.getSelectedItemPosition() == STATIC_IP) ? IpAssignment.STATIC
				: IpAssignment.DHCP;

		if (mIpAssignment == IpAssignment.STATIC) {
			int result = validateIpConfigFields(mLinkProperties);
			if (result != 0) {
				return false;
			}
		}

		mProxySettings = (mProxySettingsSpinner != null && mProxySettingsSpinner
				.getSelectedItemPosition() == PROXY_STATIC) ? ProxySettings.STATIC
				: ProxySettings.NONE;

		if (mProxySettings == ProxySettings.STATIC && mProxyHostView != null) {
			String host = mProxyHostView.getText().toString();
			String portStr = mProxyPortView.getText().toString();
			String exclusionList = mProxyExclusionListView.getText().toString();
			int port = 0;
			int result = 0;
			try {
				port = Integer.parseInt(portStr);
				result = ProxySelector.validate(host, portStr, exclusionList);
			} catch (NumberFormatException e) {
				result = R.string.proxy_error_invalid_port;
			}
			if (result == 0) {
				ProxyProperties proxyProperties = new ProxyProperties(host,
						port, exclusionList);
				mLinkProperties.setHttpProxy(proxyProperties);
			} else {
				return false;
			}
		}
		return true;
	}

	private int validateIpConfigFields(LinkProperties linkProperties) {
		if (mIpAddressView == null)
			return 0;

		String ipAddr = mIpAddressView.getText().toString();
		if (TextUtils.isEmpty(ipAddr))
			return R.string.wifi_ip_settings_invalid_ip_address;

		InetAddress inetAddr = null;
		try {
			inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
		} catch (IllegalArgumentException e) {
			return R.string.wifi_ip_settings_invalid_ip_address;
		}

		int networkPrefixLength = -1;
		try {
			networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView
					.getText().toString());
			if (networkPrefixLength < 0 || networkPrefixLength > 32) {
				return R.string.wifi_ip_settings_invalid_network_prefix_length;
			}
			linkProperties.addLinkAddress(new LinkAddress(inetAddr,
					networkPrefixLength));
		} catch (NumberFormatException e) {
			// Set the hint as default after user types in ip address
			mNetworkPrefixLengthView
					.setText(getString(R.string.wifi_network_prefix_length_hint));
		}

		String gateway = mGatewayView.getText().toString();
		if (TextUtils.isEmpty(gateway)) {
			try {
				// Extract a default gateway from IP address
				InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr,
						networkPrefixLength);
				byte[] addr = netPart.getAddress();
				addr[addr.length - 1] = 1;
				mGatewayView.setText(InetAddress.getByAddress(addr)
						.getHostAddress());
			} catch (RuntimeException ee) {
			} catch (java.net.UnknownHostException u) {
			}
		} else {
			InetAddress gatewayAddr = null;
			try {
				gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
			} catch (IllegalArgumentException e) {
				return R.string.wifi_ip_settings_invalid_gateway;
			}
			linkProperties.addRoute(new RouteInfo(gatewayAddr));
		}

		String dns = mDns1View.getText().toString();
		InetAddress dnsAddr = null;

		if (TextUtils.isEmpty(dns)) {
			// If everything else is valid, provide hint as a default option
			mDns1View.setText(getString(R.string.wifi_dns1_hint));
		} else {
			try {
				dnsAddr = NetworkUtils.numericToInetAddress(dns);
			} catch (IllegalArgumentException e) {
				return R.string.wifi_ip_settings_invalid_dns;
			}
			linkProperties.addDns(dnsAddr);
		}

		if (mDns2View.length() > 0) {
			dns = mDns2View.getText().toString();
			try {
				dnsAddr = NetworkUtils.numericToInetAddress(dns);
			} catch (IllegalArgumentException e) {
				return R.string.wifi_ip_settings_invalid_dns;
			}
			linkProperties.addDns(dnsAddr);
		}
		return 0;
	}

	// Aurora <penggangding> <2014-05-22> add begin 
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.password) {
				mPasswordView.setCursorVisible(true);
			}
		}
	};
	// Aurora <penggangding> <2014-05-22> add end
	// Aurora <penggangding> <2014-05-22> add begin  
	  //密码
	private void showSecurityFields() {
	WifiConfiguration config = null;
	  
		if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {
	findViewById(R.id.security_fields).setVisibility(View.GONE); 
	return; 
	}
	findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
	  
		if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
	config = mAccessPoint.getConfig(); 
    }
	  
		if (mPasswordView == null) {
			mPasswordView = (EditText) findViewById(R.id.password);
			mPasswordView.setCursorVisible(false);
			mPasswordView.setOnClickListener(clickListener);
//			mPasswordView.setFocusable(true);   
//			mPasswordView.setFocusableInTouchMode(true);   
//			mPasswordView.requestFocus();   
	mPasswordView.addTextChangedListener(this);
	// ((CheckBox) 
    //mView.findViewById(R.id.show_password).setOnClickListener(this);
	
			if (config != null) {
		    // Aurora <penggangding> <2014-05-22> add begin  
//	mPasswordView.setText(config.preSharedKey);
				mPasswordView.setText(DEFAULT_PASSWORD);
		    // Aurora <penggangding> <2014-05-22> add end  
	}
	
			if (mAccessPoint != null
					&& mAccessPoint.networkId != INVALID_NETWORK_ID) {
	mPasswordView.setHint(R.string.wifi_unchanged);
	}
            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			if(wifiInfo.getSSID().replace("\"", "").toString().equals(mAccessPoint.ssid))
			{
                        if(mAccessPoint.getState()!=null)
                        {                    
			if(mAccessPoint.getState().toString().equals("CONNECTED"))
			{
				mPasswordView.setEnabled(false);
		        }else
	                {
					mPasswordView.setEnabled(true);
			}
			}
                        }
			else
			{
				mPasswordView.setEnabled(true);
			}
	// Aurora <likai> <2013-11-12> add begin  
//	if (!mInXlSetupWizard) 
//	mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
////	findViewById(R.id.aurora_wifi_show_password).setVisibility( View.GONE);
	// Aurora <likai> <2013-11-12> add end } 
	}
	}
	// Aurora <penggangding> <2014-05-22> add end  
	 

	/*
	 * // 读取密码文件 private byte[] getWifiSupplicant(String filename) {
	 * BufferedReader br = null; try { File file = new File(filename); if
	 * (file.exists()) { br = new BufferedReader(new FileReader(file));
	 * StringBuffer relevantLines = new StringBuffer(); boolean started = false;
	 * String line; while ((line = br.readLine()) != null) { if (!started &&
	 * line.startsWith("network")) { started = true; } if (started) {
	 * relevantLines.append(line).append("\n"); } }
	 * android.util.Log.e("hanping", "relevantLines->" +
	 * relevantLines.toString()); if (relevantLines.length() > 0) { return
	 * relevantLines.toString().getBytes(); } else { return EMPTY_DATA; } } else
	 * { return EMPTY_DATA; } } catch (Exception ioe) { return EMPTY_DATA; }
	 * finally { if (br != null) { try { br.close(); } catch (Exception e) { } }
	 * } }
	 */

}
