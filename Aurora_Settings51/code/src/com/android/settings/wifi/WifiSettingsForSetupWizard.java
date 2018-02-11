/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.setupwizard.navigationbar.SetupWizardNavBar.NavigationBarListener;

import com.android.settings.R;

import aurora.widget.AuroraListView;

/**
 * This customized version of WifiSettings is shown to the user only during Setup Wizard. Menu
 * selections are limited, clicking on an access point will auto-advance to the next screen (once
 * connected), and, if the user opts to skip ahead without a wifi connection, a warning message
 * alerts of possible carrier data charges or missing software updates.
 */
public class WifiSettingsForSetupWizard extends WifiSettings implements OnClickListener{

    private static final String TAG = "WifiSettingsForSetupWizard";

    // show a text regarding data charges when wifi connection is required during setup wizard
    protected static final String EXTRA_SHOW_WIFI_REQUIRED_INFO = "wifi_show_wifi_required_info";

    private View mAddOtherNetworkItem;
    private ListAdapter mAdapter;
    private TextView mEmptyFooter;
    private boolean mListLastEmpty = false;

    private TextView mNextButton;
    private TextView mBackButton;
    private NavigationBarListener mCallback;

    private IntentFilter mFilter = new IntentFilter();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive, action = " + intent.getAction());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                final NetworkInfo info = (NetworkInfo) bundle
                        .get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    refreshConnectionState();
                }
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        final View view = inflater.inflate(R.layout.setup_preference, container, false);

        final AuroraListView list = (AuroraListView) view.findViewById(android.R.id.list);
        list.setPadding(0, -1 * 5, 0, 0); //Aurora linchunhui 20160303 modify
        list.auroraEnableOverScroll(false);
        final View title = view.findViewById(R.id.title);
        /*if (title == null) {
            final View header = inflater.inflate(R.layout.iuni_wifi_wizard_header, list, false);
            list.addHeaderView(header, null, false);
        }*/

        mAddOtherNetworkItem = inflater.inflate(R.layout.setup_wifi_add_network, list, false);
        list.addFooterView(mAddOtherNetworkItem, null, true);
        mAddOtherNetworkItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWifiManager.isWifiEnabled()) {
                    onAddNetworkPressed();
                }
            }
        });

        final Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(EXTRA_SHOW_WIFI_REQUIRED_INFO, false)) {
            view.findViewById(R.id.wifi_required_info).setVisibility(View.VISIBLE);
        }

        /*Aurora <linchunhui> modify 20160128*/
        mCallback = (NavigationBarListener) getActivity();
        mNextButton = (TextView) view.findViewById(R.id.setup_wizard_navbar_next);
        mBackButton = (TextView) view.findViewById(R.id.setup_wizard_navbar_back);
        mNextButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        refreshConnectionState();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //add to wizard
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("firstRun", false)) {
            removePreference("wifi_switch");
            removePreference("wifi_advanced_settings");
        }
        //end to wizard
        getView().setSystemUiVisibility(
                View.STATUS_BAR_DISABLE_HOME |
                View.STATUS_BAR_DISABLE_RECENT |
                View.STATUS_BAR_DISABLE_NOTIFICATION_ALERTS);
                //View.STATUS_BAR_DISABLE_CLOCK); Aurora linchunhui modify 20160229

        if (hasNextButton()) {
            getNextButton().setVisibility(View.GONE);
        }

        mAdapter = getPreferenceScreen().getRootAdapter();
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateFooter();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getActivity().registerReceiver(mReceiver, mFilter);
        refreshConnectionState();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void registerForContextMenu(View view) {
        // Suppressed during setup wizard
    }

    @Override
    /* package */ AuroraWifiEnabler createWifiEnabler() {
        // Not shown during setup wizard
        return null;
    }

    @Override
    /* package */ void addOptionsMenuItems(Menu menu) {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        final TypedArray ta = getActivity().getTheme()
                .obtainStyledAttributes(new int[] {R.attr.ic_wps});
        menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
                .setIcon(ta.getDrawable(0))
                .setEnabled(wifiIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
                .setEnabled(wifiIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ta.recycle();
    }

    @Override
    protected void connect(final WifiConfiguration config) {
        WifiSetupActivity activity = (WifiSetupActivity) getActivity();
        activity.networkSelected();
        super.connect(config);
    }

    @Override
    protected void connect(final int networkId) {
        WifiSetupActivity activity = (WifiSetupActivity) getActivity();
        activity.networkSelected();
        super.connect(networkId);
    }

    @Override
    protected TextView initEmptyView() {
        mEmptyFooter = new TextView(getActivity());
        mEmptyFooter.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mEmptyFooter.setGravity(Gravity.CENTER);
        mEmptyFooter.setCompoundDrawablesWithIntrinsicBounds(0,
                R.drawable.ic_wifi_emptystate, 0,0);
        return mEmptyFooter;
    }

    protected void updateFooter() {
        final boolean isEmpty = mAdapter.isEmpty();
        if (isEmpty != mListLastEmpty) {
            final ListView list = getListView();
            if (isEmpty) {
                list.removeFooterView(mAddOtherNetworkItem);
                list.addFooterView(mEmptyFooter, null, false);
            } else {
                list.removeFooterView(mEmptyFooter);
                list.addFooterView(mAddOtherNetworkItem, null, true);
            }
            mListLastEmpty = isEmpty;
        }
    }


    @Override
    public void onClick(View v) {
        if (v == mBackButton) {
            mCallback.onNavigateBack();
        } else if (v == mNextButton) {
            mCallback.onNavigateNext();
        }
    }

    private void refreshConnectionState() {
        if (isWifiConnected()) {
            mNextButton.setText(R.string.wifi_setup_next);
        } else {
            mNextButton.setText(R.string.skip_label);
        }
    }

    private boolean isWifiConnected() {
        final ConnectivityManager connectivity = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifiConnected = connectivity != null &&
                connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        return wifiConnected;
    }
}
