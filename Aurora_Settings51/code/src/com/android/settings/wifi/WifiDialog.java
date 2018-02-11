/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
import com.android.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import aurora.app.AuroraAlertDialog;

class WifiDialog extends AuroraAlertDialog implements WifiConfigUiBase {
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    private final boolean mEdit;
    private final DialogInterface.OnClickListener mListener;
    private final AccessPoint mAccessPoint;
    private WifiManager mWifiManager;
    private Resources res;

    private View mView;
    private WifiConfigController mController;
    private boolean mHideSubmitButton;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;

    public WifiDialog(Context context, DialogInterface.OnClickListener listener,
            AccessPoint accessPoint, boolean edit, boolean hideSubmitButton) {
        this(context, listener, accessPoint, edit);
        mHideSubmitButton = hideSubmitButton;
    }

    public WifiDialog(Context context, DialogInterface.OnClickListener listener,
            AccessPoint accessPoint, boolean edit) {
        super(context);
        mEdit = edit;
        mListener = listener;
        mAccessPoint = accessPoint;
        mHideSubmitButton = false;
        res = context.getResources();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public WifiConfigController getController() {
        return mController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(mView);

        mConnectListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            }
            public void onFailure(int reason) {
            }
        };

        mSaveListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            }
            public void onFailure(int reason) {
            }
        };

        setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.wifi_connect),
                  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                              if (null != mAccessPoint) {
                                     connect();
                              } else {
                                     //Aurora linchunhui 20150226 modify for BUG-19794
                                     save();
                              }
                        }
                  });
        setButton(DialogInterface.BUTTON_NEUTRAL, res.getString(R.string.wifi_cancel),
                  new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                         }
                  });

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        /// M: modify IP to IPv4 @{
        modifyIpTitle(mView);
        /// @}
        setInverseBackgroundForced(true);
        mController = new WifiConfigController(this, mView, mAccessPoint, mEdit);
        super.onCreate(savedInstanceState);

        if (mHideSubmitButton) {
            mController.hideSubmitButton();
        } else {
            /* During creation, the submit button can be unavailable to determine
             * visibility. Right after creation, update button visibility */
            mController.enableSubmitIfAppropriate();
        }
        mView.findViewById(R.id.info).setVisibility(View.GONE);
        mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.GONE);

    }

    /* package */ void connect() {
        final WifiConfiguration config = mController.getConfig();

        if (mAccessPoint != null && mAccessPoint.networkId == INVALID_NETWORK_ID) {
            mWifiManager.connect(config, mConnectListener);
        }
    }
	
    public void save() {
        WifiConfiguration config = mController.getConfig();
        if (config != null) {
              mWifiManager.save(config, mSaveListener);
              if(config.networkId != INVALID_NETWORK_ID) {
                    mWifiManager.connect(config.networkId, mConnectListener);
              }
        }
    }

    /**
     * restrict static IP to IPv4
     * @param view current view
     */
    private void modifyIpTitle(View view) {
        TextView ipSettingsView = (TextView) mView.findViewById(R.id.wifi_ip_settings);
        ipSettingsView.setText(ipSettingsView.getText().toString().replace("IP", "IPv4"));
        TextView ipAddressView = (TextView) mView.findViewById(R.id.wifi_ip_address);
        ipAddressView.setText(ipAddressView.getText().toString().replace("IP", "IPv4"));
    }

    @Override
    public boolean isEdit() {
        return mEdit;
    }

    @Override
    public Button getSubmitButton() {
        return getButton(BUTTON_SUBMIT);
    }

    @Override
    public Button getForgetButton() {
        return getButton(BUTTON_FORGET);
    }

    @Override
    public Button getCancelButton() {
        return getButton(BUTTON_NEGATIVE);
    }

    @Override
    public Button getModifyButton() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSubmitButton() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setForgetButton(/*CharSequence text*/) {
        setButton(BUTTON_FORGET, /*text*/"", mListener);
    }

    @Override
    public void setCancelButton() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setModifyButton() {
        // TODO Auto-generated method stub
    }
}
