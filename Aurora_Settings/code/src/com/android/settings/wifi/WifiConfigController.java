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

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.content.Context;
import android.content.res.Resources;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Handler;
import android.renderscript.Sampler.Value;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import aurora.widget.AuroraEditText;
import aurora.widget.AuroraSpinner;

import com.android.settings.ProxySelector;
import com.android.settings.R;

import java.net.InetAddress;
import java.util.Iterator;

/**
 * The class for allowing UIs like {@link WifiDialog} and {@link WifiConfigUiBase} to
 * share the logic for controlling buttons, text fields, etc.
 */
public class WifiConfigController implements TextWatcher,
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    //google compile 2013-12-02
    public static final String KEYSTORE_URI = "keystore://";
    public static final String ENGINE_DISABLE = "0";
    public static final String ENGINE_ENABLE = "1";
    public static final String KEYSTORE_ENGINE_ID = "keystore";

    public class EnterpriseField {
        private String varName;
        private String value;

        private EnterpriseField(String varName) {
            this.varName = varName;
            this.value = null;
        }

        public void setValue(String value) {
            this.value = value;
        }


        public String varName() {
            return varName;
        }

        public String value() {
            return value;
        }
    }

    public static EnterpriseField key_id; //= new EnterpriseField("key_id");
    public static EnterpriseField ca_cert; // = new EnterpriseField("ca_cert");
    public static EnterpriseField client_cert; // = new EnterpriseField("client_cert");
    public EnterpriseField eap = new EnterpriseField("eap");
    public EnterpriseField phase2 = new EnterpriseField("phase2");
    public EnterpriseField engine = new EnterpriseField("engine");
    public EnterpriseField engine_id = new EnterpriseField("engine_id");
    public EnterpriseField identity = new EnterpriseField("identity");
    public EnterpriseField anonymous_identity = new EnterpriseField("anonymous_identity");
    public EnterpriseField password = new EnterpriseField("password");
    //google compile 2013-12-02

    private static final String KEYSTORE_SPACE = KEYSTORE_URI;

    private static final String PHASE2_PREFIX = "auth=";

    private final WifiConfigUiBase mConfigUi;
    private final View mView;
    private final AccessPoint mAccessPoint;

    private boolean mEdit;

    private TextView mSsidView;

    // e.g. AccessPoint.SECURITY_NONE
    private int mAccessPointSecurity;
    private TextView mPasswordView;

    private AuroraSpinner mSecuritySpinner;
    private AuroraSpinner mEapMethodSpinner;
    private AuroraSpinner mEapCaCertSpinner;
    private AuroraSpinner mPhase2Spinner;
    private AuroraSpinner mEapUserCertSpinner;
    private TextView mEapIdentityView;
    private TextView mEapAnonymousView;

    /* This value comes from "wifi_ip_settings" resource array */
    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    /* These values come from "wifi_proxy_settings" resource array */
    public static final int PROXY_NONE = 0;
    public static final int PROXY_STATIC = 1;

    /* These values from from "wifi_eap_method" resource array */
    public static final int WIFI_EAP_METHOD_PEAP = 0;
    public static final int WIFI_EAP_METHOD_TLS  = 1;
    public static final int WIFI_EAP_METHOD_TTLS = 2;
    public static final int WIFI_EAP_METHOD_PWD  = 3;

    private static final String TAG = "WifiConfigController";

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
    private LinkProperties mLinkProperties = new LinkProperties();

    // True when this instance is used in SetupWizard XL context.
    private final boolean mInXlSetupWizard;

    private final Handler mTextViewChangedHandler;
    
    View mProxySettingsView = null;
    View mIpAssignmentView = null;

    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
    private boolean mIsCustomDialog = true;
    private static boolean mApPwDialogSupport = "yes".equals(android.os.SystemProperties.get(
            "ro.gn.appwdialog.support", "no"));
    //Gionee <xuwen><2013-07-09> added for CR00834689 end
    
    static boolean requireKeyStore(WifiConfiguration config) {
        if (config == null || key_id == null || ca_cert == null ||
            client_cert == null) {
            return false;
        }
        if (!TextUtils.isEmpty(key_id.value())) {
            return true;
        }
        String values[] = {ca_cert.value(), client_cert.value()};
        for (String value : values) {
            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
                return true;
            }
        }
        return false;
    }
 
    public WifiConfigController(
            WifiConfigUiBase parent, View view, AccessPoint accessPoint, boolean edit) {
        mConfigUi = parent;
        mInXlSetupWizard = (parent instanceof WifiConfigUiForSetupWizardXL); 
      InputMethodManager imm = (InputMethodManager)mConfigUi.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mView = view;
//        mView.setX(0.0f);
//        mView.setY(-24.0f);
        mAccessPoint = accessPoint;
        mAccessPointSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE :
                accessPoint.security;
        mEdit = edit;  

        mTextViewChangedHandler = new Handler();
        final Context context = mConfigUi.getContext();
        final Resources resources = context.getResources();

        key_id = new EnterpriseField("key_id"); 
        ca_cert = new EnterpriseField("ca_cert");  
        client_cert = new EnterpriseField("client_cert");

        mIpSettingsSpinner = (AuroraSpinner) mView.findViewById(R.id.ip_settings);  
        mIpSettingsSpinner.setOnItemSelectedListener(this);
        mProxySettingsSpinner = (AuroraSpinner) mView.findViewById(R.id.proxy_settings);  
        mProxySettingsSpinner.setOnItemSelectedListener(this);
        
        
      
        if (mAccessPoint == null) { // new network
        	mConfigUi.setTitle(R.string.wifi_add_network);

            mSsidView = (TextView) mView.findViewById(R.id.ssid);
            mSsidView.addTextChangedListener(this);
            mSecuritySpinner = ((AuroraSpinner) mView.findViewById(R.id.security));
            mSecuritySpinner.setOnItemSelectedListener(this);
            if (mInXlSetupWizard) {
                mView.findViewById(R.id.type_ssid).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.type_security).setVisibility(View.VISIBLE);
                // We want custom layout. The content must be same as the other cases.

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        R.layout.wifi_setup_custom_list_item_1, android.R.id.text1,
                        context.getResources().getStringArray(R.array.wifi_security_no_eap));
                mSecuritySpinner.setAdapter(adapter);
            } else {
                mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            }

            showIpConfigFields();    
            showProxyFields();     
//            mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
//            mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
            mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE); 

            // Aurora <likai> <2013-11-12> add begin
            if (!mInXlSetupWizard) {
            	mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.GONE);
            }
            // Aurora <likai> <2013-11-12> add end

            mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
        } else {
            mConfigUi.setTitle(mAccessPoint.ssid); 

            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info); 

            DetailedState state = mAccessPoint.getState();   
           
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(mConfigUi.getContext(), state)); 
            }

            int level = mAccessPoint.getLevel();    
            Log.d(TAG, "pgd-- SignalLevel:"+level); 
            //Gionee <xuwen><2013-07-09> added for CR00834689 begin
            View rowLevel = null;
            //Gionee <xuwen><2013-07-09> added for CR00834689 end
            if (level != -1) {           
           
                String[] signal = resources.getStringArray(R.array.wifi_signal);  
                //Gionee <xuwen><2013-07-09> modified for CR00834689 begin
                //addRow(group, R.string.wifi_signal, signal[level]);
                Log.d(TAG, "pgd-- ro.gn.appwdialog.support::"+mApPwDialogSupport);
                if (mApPwDialogSupport) {
                    rowLevel = mConfigUi.getLayoutInflater().inflate(
                            R.layout.wifi_dialog_row, group, false);     
                    ((TextView) rowLevel.findViewById(R.id.name)).setText(R.string.wifi_signal);  
                    ((TextView) rowLevel.findViewById(R.id.value)).setText(signal[level]); 
                    group.addView(rowLevel);
                } else {
                    addRow(group, R.string.wifi_signal, signal[level]);  
                }
                //Gionee <xuwen><2013-07-09> modified for CR00834689 end
            }

            WifiInfo info = mAccessPoint.getInfo();  

            if (info != null && info.getLinkSpeed() != -1) {   
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS); 
            }

            addRow(group, R.string.wifi_security, mAccessPoint.getSecurityString(false)); 

            boolean showAdvancedFields = false;
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {    
                WifiConfiguration config = mAccessPoint.getConfig();   
                if (config.ipAssignment == IpAssignment.STATIC) {
                    mIpSettingsSpinner.setSelection(STATIC_IP);    
                    showAdvancedFields = true;
                } else {
                    mIpSettingsSpinner.setSelection(DHCP);   
                }
                //Display IP addresses
                for(InetAddress a : config.linkProperties.getAddresses()) {  
                    addRow(group, R.string.wifi_ip_address, a.getHostAddress());
                }

                if (config.proxySettings == ProxySettings.STATIC) {
                    mProxySettingsSpinner.setSelection(PROXY_STATIC);
                    showAdvancedFields = true;
                } else {
                    mProxySettingsSpinner.setSelection(PROXY_NONE);
                }
                mProxySettingsView = mConfigUi.getLayoutInflater().inflate(R.layout.aurora_wifi_info_row, group, false);
                ((TextView) mProxySettingsView.findViewById(R.id.name)).setText(R.string.proxy_settings_title);  
                ((TextView) mProxySettingsView.findViewById(R.id.value)).setText(mProxySettingsSpinner.getSelectedItem().toString());
                group.addView(mProxySettingsView);
                mIpAssignmentView = mConfigUi.getLayoutInflater().inflate(R.layout.aurora_wifi_info_row, group, false);
                ((TextView) mIpAssignmentView.findViewById(R.id.name)).setText(R.string.wifi_ip_settings);  
                ((TextView) mIpAssignmentView.findViewById(R.id.value)).setText(mIpSettingsSpinner.getSelectedItem().toString());
                group.addView(mIpAssignmentView);
//                addRow(group, R.string.proxy_settings_title, mProxySettingsSpinner.getSelectedItem().toString());
//				addRow(group, R.string.wifi_ip_settings, mIpSettingsSpinner.getSelectedItem().toString());
            }    


            if (mAccessPoint.networkId == INVALID_NETWORK_ID || mEdit) {  
            	
                showSecurityFields();
                
				if(!mEdit) {
					showIpConfigFields();
                	showProxyFields();
				} else {
					showIpConfigFieldsInfo();
                	showProxyFieldsInfo();				
				}
//                mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                if (showAdvancedFields) {    
                    ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(true);
                    mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }

                // Aurora <likai> <2013-11-12> add begin
                if (!mInXlSetupWizard) {
                    if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                    	mView.findViewById(R.id.aurora_wifi_password_fields).setVisibility(View.GONE);
                    }
                    mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.GONE);
                    //mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }
                // Aurora <likai> <2013-11-12> add end
            }

            if (mEdit) {
                // Aurora <likai> modify begin
                //mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));
                if (mInXlSetupWizard) {
                	mConfigUi.setSubmitButton(context.getString(R.string.wifi_save));  
                } else {
                	mView.setFocusable(true);
                	mView.setFocusableInTouchMode(true);
                	imm.hideSoftInputFromWindow(mView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                	mConfigUi.setModifyButton(context.getString(R.string.wifi_modify));
                }
                // Aurora <likai> modify end
            } else {
           
                if (state == null && level != -1) {
                    mConfigUi.setSubmitButton(context.getString(R.string.wifi_connect));  
                } else {
                    mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
                }
                if (mAccessPoint.networkId != INVALID_NETWORK_ID) {
                    mConfigUi.setModifyButton(context.getString(R.string.wifi_modify));
                    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
                    if (mApPwDialogSupport) {
                        mIsCustomDialog = false;
                    }
                    //Gionee <xuwen><2013-07-09> added for CR00834689 end
                }
                //Gionee <xuwen><2013-07-09> added for CR00834689 begin
                if (mApPwDialogSupport && mIsCustomDialog) {
                    if (level != -1) {
                        group.removeView(rowLevel);
                    }
                }
                //Gionee <xuwen><2013-07-09> added for CR00834689 end
            }
        }
        if(mEdit && (mAccessPoint != null)) {
        	mView.findViewById(R.id.ignore_network).setVisibility(View.VISIBLE);
        	mIgnoreWifiApButton = (Button) mView.findViewById(R.id.ignore_network_button);
            mIgnoreWifiApButton.setOnClickListener(this);
        }


        mConfigUi.setCancelButton(context.getString(R.string.wifi_cancel));

        // Aurora <likai> add begin
        if (!mInXlSetupWizard) return;
        // Aurora <likai> add end

        if (mConfigUi.getSubmitButton() != null) {
            enableSubmitIfAppropriate();
        }
    }
    
    public void update() {
        //Display IP addresses
        showProxyFieldsInfoUpdate();
    	showIpConfigFieldsInfoUpdate();
    }

    private static View ipview=null;
    private void addRow(ViewGroup group, int name, String value) {
        //View row = mConfigUi.getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
    	View row = mConfigUi.getLayoutInflater().inflate(R.layout.aurora_wifi_info_row, group, false);
        if(name==R.string.wifi_ip_address)
        {
        	ipview=row;
        }
    	((TextView) row.findViewById(R.id.name)).setText(name);  
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    // Aurora <likai> add begin
    boolean canEnableSubmitIfAppropriate() {
    	boolean enabled = false;
        boolean passwordInvalid = false;

        if (mPasswordView != null &&
            ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mPasswordView.length() == 0) ||
            (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mPasswordView.length() < 8))) {
            passwordInvalid = true;
        }

        if ((mSsidView != null && mSsidView.length() == 0) ||
            ((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
            passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        return enabled;
    }
    // Aurora <likai> add end

   
    void enableSubmitIfAppropriate() {
        Button submit = mConfigUi.getSubmitButton(); 
        if (submit == null) return;

        boolean enabled = false;
        boolean passwordInvalid = false;

        if (mPasswordView != null &&
            ((mAccessPointSecurity == AccessPoint.SECURITY_WEP && mPasswordView.getText().toString().getBytes().length == 0) ||
            (mAccessPointSecurity == AccessPoint.SECURITY_PSK && mPasswordView.getText().toString().getBytes().length < 8))) {
            passwordInvalid = true;
        }

        if ((mSsidView != null && mSsidView.length() == 0) ||
            ((mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) &&
            passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        submit.setEnabled(enabled);
    }
    public static final int WIFI_PEAP_PHASE2_NONE 	    = 0;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2 	= 3;
    public static final int WIFI_PEAP_PHASE2_GTC        = 4;
    public static String unspecifiedCert="unspecifiedCert";
    
    /* package */ WifiConfiguration getConfig() {
    	
    	unspecifiedCert = mConfigUi.getContext().getString(R.string.wifi_unspecified);
    	
    	
        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID && !mEdit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();
    
        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mSsidView.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
            //Iuni penggangding begin
            if(Build.VERSION.RELEASE.contains("4.3")  &&  Build.MODEL.equals("IUNI U810"))
            { 
            config.BSSID = mAccessPoint.bssid;
            }
            //Iuni penggangding	end
        } else {
            config.networkId = mAccessPoint.networkId;
        }


        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    String password = mPasswordView.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:                       
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:    
  
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                WifiSettings.PrintLog(TAG, "buildmode :"+Build.MODEL);
                if(!Build.MODEL.equals("GT-I9500"))
                {
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = mEapMethodSpinner.getSelectedItemPosition();
                int phase2Method = mPhase2Spinner.getSelectedItemPosition();
                config.enterpriseConfig.setEapMethod(eapMethod);

                if(eapMethod==0)
                {
                    switch(phase2Method) 
                    {
                    case WIFI_PEAP_PHASE2_NONE:
                        config.enterpriseConfig.setPhase2Method(Phase2.NONE);
                        break;
                    case WIFI_PEAP_PHASE2_MSCHAPV2:
                    	Log.d(TAG, "pgd-- WIFI_PEAP_PHASE2_MSCHAPV2");
                        config.enterpriseConfig.setPhase2Method(Phase2.MSCHAPV2);  
                        break;
                    case WIFI_PEAP_PHASE2_GTC:
                        config.enterpriseConfig.setPhase2Method(Phase2.GTC);
                        break;
                    default:
                        Log.e(TAG, "Unknown phase2 method" + phase2Method);
                        break;
                    }
                }
                else
                {
                	config.enterpriseConfig.setPhase2Method(phase2Method);
                }
                String caCert = (String) mEapCaCertSpinner.getSelectedItem();
                if (caCert.equals(unspecifiedCert)) caCert = "";
                config.enterpriseConfig.setCaCertificateAlias(caCert);
                String clientCert = (String) mEapUserCertSpinner.getSelectedItem();
                 if(clientCert.equals(unspecifiedCert)) clientCert = "";
                config.enterpriseConfig.setClientCertificateAlias(clientCert);
                config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
                config.enterpriseConfig.setAnonymousIdentity(
                        mEapAnonymousView.getText().toString());
                
                
                if(mPasswordView.length() > 0)
                {
                	config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                }
                }
                eap.setValue((String) mEapMethodSpinner.getSelectedItem()); 
       
                phase2.setValue((mPhase2Spinner.getSelectedItemPosition() == 0) ? "" :    
                        PHASE2_PREFIX + mPhase2Spinner.getSelectedItem().toString());
   
                ca_cert.setValue((mEapCaCertSpinner.getSelectedItemPosition() == 0) ? "" :      
                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                        (String) mEapCaCertSpinner.getSelectedItem());
            
                client_cert.setValue((mEapUserCertSpinner.getSelectedItemPosition() == 0) ?        
                        "" : KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCertSpinner.getSelectedItem());
                
                final boolean isEmptyKeyId = (mEapUserCertSpinner.getSelectedItemPosition() == 0);     
  
                key_id.setValue(isEmptyKeyId ? "" : Credentials.USER_PRIVATE_KEY +         
                        (String) mEapUserCertSpinner.getSelectedItem());
  
                engine.setValue(isEmptyKeyId ? ENGINE_DISABLE : ENGINE_ENABLE);
     
                engine_id.setValue(isEmptyKeyId ? "" : KEYSTORE_ENGINE_ID);
 
                identity.setValue((mEapIdentityView.length() == 0) ? "" : mEapIdentityView.getText().toString());   

                anonymous_identity.setValue((mEapAnonymousView.length() == 0) ? "" : mEapAnonymousView.getText().toString());  

                if (mPasswordView.length() > 0) {      
                    password.setValue(mPasswordView.getText().toString());     
                }
                break;
            default:
                    return null;
        }

        config.proxySettings = mProxySettings;
        config.ipAssignment = mIpAssignment;
        config.linkProperties = new LinkProperties(mLinkProperties);
      
        return config;
   
    }

    private boolean ipAndProxyFieldsAreValid() {
        mLinkProperties.clear();
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            int result = validateIpConfigFields(mLinkProperties);
            if (result != 0) {
                return false;
            }
        }

        mProxySettings = (mProxySettingsSpinner != null &&
                mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                ProxySettings.STATIC : ProxySettings.NONE;

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
                ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                mLinkProperties.setHttpProxy(proxyProperties);
            } else {
                return false;
            }
        }
        return true;
    }

    private int validateIpConfigFields(LinkProperties linkProperties) {
        if (mIpAddressView == null) return 0;

        String ipAddr = mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) return R.string.wifi_ip_settings_invalid_ip_address;

        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
            linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
            mNetworkPrefixLengthView.setText(mConfigUi.getContext().getString(
                    R.string.wifi_network_prefix_length_hint));
        }

        String gateway = mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length-1] = 1;
                mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
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
            //If everything else is valid, provide hint as a default option
            mDns1View.setText(mConfigUi.getContext().getString(R.string.wifi_dns1_hint));
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

    private void showSecurityFields() {

        if (mInXlSetupWizard) {
            // Note: XL SetupWizard won't hide "EAP" settings here.
            //if (!((WifiSettingsForSetupWizardXL)mConfigUi.getContext()).initSecurityFields(mView,
        	if (!((WifiSettingsForSetupWizardXL)mConfigUi).initSecurityFields(mView,
                        mAccessPointSecurity)) {
                return;
            }
        }

        if (mAccessPointSecurity == AccessPoint.SECURITY_NONE) {  
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);

        if (mPasswordView == null) {
            mPasswordView = (TextView) mView.findViewById(R.id.password);
            mPasswordView.addTextChangedListener(this);    
            ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mPasswordView.setHint(R.string.wifi_unchanged);  
            }

            // Aurora <likai> <2013-11-12> add begin
            if (!mInXlSetupWizard) {
            	// Huawei 6 error problems exist glory begin
            	Log.d("gd", " 111111 model="+Build.MODEL.toString());
            	 if(!Build.MODEL.equals("H60"))
            	 {
            		 mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            	 }
            	// Huawei 6 error problems exist glory end
                mView.findViewById(R.id.aurora_wifi_show_password).setVisibility(View.GONE); 
            }
            // Aurora <likai> <2013-11-12> add end
        }

        if (mAccessPointSecurity != AccessPoint.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);   
        


        if (mEapMethodSpinner == null) {       
        	WifiSettings.PrintLog(TAG, " ======= has connecting ======");
            mEapMethodSpinner = (AuroraSpinner) mView.findViewById(R.id.method);
            mEapMethodSpinner.setOnItemSelectedListener(this);
            mPhase2Spinner = (AuroraSpinner) mView.findViewById(R.id.phase2);
            mEapCaCertSpinner = (AuroraSpinner) mView.findViewById(R.id.ca_cert);
            mEapUserCertSpinner = (AuroraSpinner) mView.findViewById(R.id.user_cert);
            mEapIdentityView = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymousView = (TextView) mView.findViewById(R.id.anonymous);

            loadCertificates(mEapCaCertSpinner, Credentials.CA_CERTIFICATE);   
            loadCertificates(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY); 

            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                WifiConfiguration config = mAccessPoint.getConfig();
             // IUNI <penggangding> <2014-06-04> add begin
                final TextView methodview=(TextView)mView.findViewById(R.id.methodview);
                final TextView phase2view=(TextView)mView.findViewById(R.id.phase2view);
                final TextView identityview=(TextView)mView.findViewById(R.id.identityview);
                methodview.setVisibility(View.VISIBLE);
                phase2view.setVisibility(View.VISIBLE);
                identityview.setVisibility(View.VISIBLE);
            // IUNI <penggangding> <2014-06-04> add end
            // IUNI <penggangding> <2014-06-04> add begin
//                setSelection(mEapMethodSpinner, eap.value());
//                final String phase2Method = phase2.value();
//                if (phase2Method != null && phase2Method.startsWith(PHASE2_PREFIX)) {
//                    setSelection(mPhase2Spinner, phase2Method.substring(PHASE2_PREFIX.length()));
//                } else {
//                    setSelection(mPhase2Spinner, phase2Method);
//                }
//                setCertificate(mEapCaCertSpinner, KEYSTORE_SPACE + Credentials.CA_CERTIFICATE,           
//                        ca_cert.value());
//                setCertificate(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY,                   
//                        key_id.value());
//                mEapIdentityView.setText(identity.value());          
//                mEapAnonymousView.setText(anonymous_identity.value());
           // IUNI <penggangding> <2014-06-04> add end
           // IUNI <penggangding> <2014-06-04> add begin
                if(!Build.MODEL.equals("GT-I9500"))
                {
                setSelection(mEapMethodSpinner, selectEapMethod(config.enterpriseConfig.getEapMethod()));
                final String phase2Method=selectPhase2Method(config.enterpriseConfig.getPhase2Method());
                if (phase2Method != null && phase2Method.startsWith(PHASE2_PREFIX)) {
                    setSelection(mPhase2Spinner, phase2Method.substring(PHASE2_PREFIX.length()));
                } else {
                    setSelection(mPhase2Spinner, phase2Method);
                }
                setCertificate(mEapCaCertSpinner, KEYSTORE_SPACE + Credentials.CA_CERTIFICATE,           
                        config.enterpriseConfig.getCaCertificateAlias());
                setCertificate(mEapUserCertSpinner, Credentials.USER_PRIVATE_KEY,                   
                        config.enterpriseConfig.getClientCertificateAlias());
                identityview.setText(config.enterpriseConfig.getIdentity());       
                mEapAnonymousView.setText(config.enterpriseConfig.getAnonymousIdentity());
                methodview.setText(selectEapMethod(config.enterpriseConfig.getEapMethod()));
                phase2view.setText(phase2Method);
                }
                mView.findViewById(R.id.l_method_line).setVisibility(View.GONE);
                mView.findViewById(R.id.l_identity_line).setVisibility(View.GONE);
                mView.findViewById(R.id.l_phase2_line).setVisibility(View.GONE);
                mEapMethodSpinner.setVisibility(View.GONE);
                mPhase2Spinner.setVisibility(View.GONE);
                mEapIdentityView.setVisibility(View.GONE);
            //  mEapIdentityView.setEnabled(false);
            //  methodview.setEnabled(false);
            //  phase2view.setEnabled(false);
          //  IUNI <penggangding> <2014-06-04> add end
        }
        }

        mView.findViewById(R.id.l_method).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.l_identity).setVisibility(View.VISIBLE);

        if (mEapMethodSpinner.getSelectedItemPosition() == WIFI_EAP_METHOD_PWD){
            mView.findViewById(R.id.l_phase2).setVisibility(View.GONE);     
            mView.findViewById(R.id.l_ca_cert).setVisibility(View.GONE);    
            mView.findViewById(R.id.l_user_cert).setVisibility(View.GONE);  
            mView.findViewById(R.id.l_anonymous).setVisibility(View.GONE);  
        } else {
        	// IUNI <penggangding> <2014-06-16> add begin
            mView.findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
            if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) 
        	{
                mView.findViewById(R.id.l_ca_cert).setVisibility(View.GONE);
                mView.findViewById(R.id.l_user_cert).setVisibility(View.GONE);
                mView.findViewById(R.id.l_anonymous).setVisibility(View.GONE);
        	}else
        	{
            mView.findViewById(R.id.l_ca_cert).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.l_user_cert).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
        	}
            // IUNI <penggangding> <2014-06-16> add end
            // IUNI <penggangding> <2014-06-16> add begin
//          mView.findViewById(R.id.l_phase2).setVisibility(View.VISIBLE);
//          mView.findViewById(R.id.l_ca_cert).setVisibility(View.VISIBLE);
//          mView.findViewById(R.id.l_user_cert).setVisibility(View.VISIBLE);
//          mView.findViewById(R.id.l_anonymous).setVisibility(View.VISIBLE);
            // IUNI <penggangding> <2014-06-16> add end
        }
    }
 // IUNI <penggangding> <2014-06-04> add begin
    private String selectPhase2Method(int Location)
    {
    	String Phase2Method="";
    	switch (Location) {
    	case Phase2.NONE:
    		Phase2Method="";
    		break;
		case Phase2.PAP:
			Phase2Method=Phase2.strings[Phase2.PAP];
			break;
		case Phase2.MSCHAP:
			Phase2Method=Phase2.strings[Phase2.MSCHAP];
			break;
		case Phase2.MSCHAPV2:
			Phase2Method=Phase2.strings[Phase2.MSCHAPV2];
			break;
		case Phase2.GTC:
			Phase2Method=Phase2.strings[Phase2.GTC];
			break;
		default:
			break;
		}
    	return Phase2Method;
    }
    
    private String selectEapMethod(int Location)
    {
    	String  EapSelect="";
    	switch (Location) {
		case Eap.PEAP:
			EapSelect=Eap.strings[Eap.PEAP];
			break;
		case Eap.TLS:
			EapSelect=Eap.strings[Eap.TLS];
			break;
		case Eap.TTLS:
			EapSelect=Eap.strings[Eap.TTLS];
			break;
		case Eap.PWD:
			EapSelect=Eap.strings[Eap.PWD];
			break;
		default:
			break;
		}
    	return EapSelect;
    }
 // IUNI <penggangding> <2014-06-04> add end

    private void showIpConfigFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.GONE);
        	}
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress()); 
                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {       
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.VISIBLE);
        	}                                                
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "pgd--  showIpFields State = " + mAccessPoint.getState() + " mAccessPoint  networkId:"+mAccessPoint.networkId);
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) { //  (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mIpSettingsSpinner != null) mIpSettingsSpinner.setEnabled(false);
                if (mIpAddressView != null) mIpAddressView.setEnabled(false);
                if (mGatewayView != null) mGatewayView.setEnabled(false);
                if (mNetworkPrefixLengthView != null) mNetworkPrefixLengthView.setEnabled(false);
                if (mDns1View != null) mDns1View.setEnabled(false);
                if (mDns2View != null) mDns2View.setEnabled(false);
            }
        }
    }
    
    private void showIpConfigFieldsInfoUpdate() {
        WifiConfiguration config = null;
        ViewGroup groupProxy = (ViewGroup) mView.findViewById(R.id.info);
        groupProxy.removeView(mIpAssignmentView);
        mIpAssignmentView = mConfigUi.getLayoutInflater().inflate(R.layout.aurora_wifi_info_row, groupProxy, false);
        ((TextView) mIpAssignmentView.findViewById(R.id.name)).setText(R.string.wifi_ip_settings); 

        mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }else
        {
        	return;
        }

        if ("STATIC".equals(config.ipAssignment.toString())) {
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.GONE);
        	}
        	((TextView) mIpAssignmentView.findViewById(R.id.value)).setText(mConfigUi.getContext().getString(R.string.wifi_ip_settings_static));
        	groupProxy.addView(mIpAssignmentView);
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.VISIBLE);
        	}
        	((TextView) mIpAssignmentView.findViewById(R.id.value)).setText(mConfigUi.getContext().getString(R.string.wifi_ip_settings_dhcp));
        	groupProxy.addView(mIpAssignmentView);
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "showIpFields State = " + mAccessPoint.getState());
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) { //  (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mIpSettingsSpinner != null) mIpSettingsSpinner.setEnabled(false);
                if (mIpAddressView != null) mIpAddressView.setEnabled(false);
                if (mGatewayView != null) mGatewayView.setEnabled(false);
                if (mNetworkPrefixLengthView != null) mNetworkPrefixLengthView.setEnabled(false);
                if (mDns1View != null) mDns1View.setEnabled(false);
                if (mDns2View != null) mDns2View.setEnabled(false);
            }
        }
    }
    
    private void showProxyFieldsInfoUpdate() {
        WifiConfiguration config = null;
        ViewGroup groupProxy = (ViewGroup) mView.findViewById(R.id.info);
        groupProxy.removeView(mProxySettingsView);
        mProxySettingsView = mConfigUi.getLayoutInflater().inflate(R.layout.aurora_wifi_info_row, groupProxy, false);
        ((TextView) mProxySettingsView.findViewById(R.id.name)).setText(R.string.proxy_settings_title);  
//        ((TextView) mProxySettingsView.findViewById(R.id.value)).setText(mProxySettingsSpinner.getSelectedItem().toString());
//        group.addView(mProxySettingsView);

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.GONE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }
        
        if(config == null)
        {
        return;
        }
        
        if ("STATIC".equals(config.proxySettings.toString())) {
        	((TextView) mProxySettingsView.findViewById(R.id.value)).setText(mConfigUi.getContext().getString(R.string.wifi_proxy_settings_manual));
        	groupProxy.addView(mProxySettingsView);
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                }
            }
        } else {
        	((TextView) mProxySettingsView.findViewById(R.id.value)).setText(mConfigUi.getContext().getString(R.string.wifi_proxy_settings_none));
        	groupProxy.addView(mProxySettingsView);
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "showProxyFields State = " + mAccessPoint.getState());
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {  //   (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mProxySettingsSpinner != null) mProxySettingsSpinner.setEnabled(false);
                if (mProxyHostView != null) mProxyHostView.setEnabled(false);
                if (mProxyPortView != null) mProxyPortView.setEnabled(false);
                if (mProxyExclusionListView != null) mProxyExclusionListView.setEnabled(false);
            } 
        }

        // Aurora <likai> <2013-11-12> add begin
        if (!mInXlSetupWizard) {
        	mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
        }
        // Aurora <likai> <2013-11-12> add end
    }

	private void showIpConfigFieldsInfo() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.GONE);
        	}
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {
        	if(ipview!=null)
        	{
        		ipview.setVisibility(View.VISIBLE);
        	}
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "showIpFields State = " + mAccessPoint.getState());
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) { //  (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mIpSettingsSpinner != null) mIpSettingsSpinner.setEnabled(false);
                if (mIpAddressView != null) mIpAddressView.setEnabled(false);
                if (mGatewayView != null) mGatewayView.setEnabled(false);
                if (mNetworkPrefixLengthView != null) mNetworkPrefixLengthView.setEnabled(false);
                if (mDns1View != null) mDns1View.setEnabled(false);
                if (mDns2View != null) mDns2View.setEnabled(false);
            }
        }

    }

    private void showProxyFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                }
            }
        } else {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "showProxyFields State = " + mAccessPoint.getState());
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {  //   (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mProxySettingsSpinner != null) mProxySettingsSpinner.setEnabled(false);
                if (mProxyHostView != null) mProxyHostView.setEnabled(false);
                if (mProxyPortView != null) mProxyPortView.setEnabled(false);
                if (mProxyExclusionListView != null) mProxyExclusionListView.setEnabled(false);
            } 
        }

        // Aurora <likai> <2013-11-12> add begin
        if (!mInXlSetupWizard) {
        	mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
        }
        // Aurora <likai> <2013-11-12> add end
    }

	private void showProxyFieldsInfo() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.GONE);

        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                }
            }
        } else {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }

        if (mAccessPoint != null) {
            Log.v("xiaoyong", "showProxyFields State = " + mAccessPoint.getState());
            if (mAccessPoint.networkId != INVALID_NETWORK_ID) {  //   (mAccessPoint.getState() == DetailedState.CONNECTED) {
                if (mProxySettingsSpinner != null) mProxySettingsSpinner.setEnabled(false);
                if (mProxyHostView != null) mProxyHostView.setEnabled(false);
                if (mProxyPortView != null) mProxyPortView.setEnabled(false);
                if (mProxyExclusionListView != null) mProxyExclusionListView.setEnabled(false);
            } 
        }

        // Aurora <likai> <2013-11-12> add begin
        if (!mInXlSetupWizard) {
        	mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
        }
        // Aurora <likai> <2013-11-12> add end
    }

    private void loadCertificates(AuroraSpinner spinner, String prefix) {
        final Context context = mConfigUi.getContext();
        final String unspecified = context.getString(R.string.wifi_unspecified);

        String[] certs = KeyStore.getInstance().saw(prefix);
        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setCertificate(AuroraSpinner spinner, String prefix, String cert) {
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }

    private void setSelection(AuroraSpinner spinner, String value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public boolean isEdit() {
        return mEdit;
    }

    @Override
    public void afterTextChanged(Editable s) {
        mTextViewChangedHandler.post(new Runnable() {
                public void run() {
                    enableSubmitIfAppropriate();
                }
            });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // work done in afterTextChanged
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // work done in afterTextChanged
    }

    @Override
    public void onClick(View view) {  
        if (view.getId() == R.id.show_password) {
            int pos = mPasswordView.getSelectionEnd();
            mPasswordView.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_PASSWORD));
            if (pos >= 0) {
                ((AuroraEditText)mPasswordView).setSelection(pos);
            }
        } else if (view.getId() == R.id.wifi_advanced_togglebox) {
            if (((CheckBox) view).isChecked()) {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
            } else {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
            }
        } else if(view.getId() == R.id.ignore_network_button) {
        	mConfigUi.setForgetCommit();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mSecuritySpinner) {
            mAccessPointSecurity = position;
            showSecurityFields();
        } else if (parent == mEapMethodSpinner) {
            showSecurityFields();
        } else if (parent == mProxySettingsSpinner) {
            showProxyFields();
        } else {
            showIpConfigFields();
        }
        enableSubmitIfAppropriate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //
    }
}
