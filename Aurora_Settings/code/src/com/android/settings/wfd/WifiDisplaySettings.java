/*
 * Copyright (C) 2012 The Android Open Source Project
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

//package com.android.settings.wfd;
//
//import aurora.widget.AuroraActionBar;
//import android.app.ActionBar;
//import aurora.app.AuroraActivity;
//import android.app.Activity;
//import aurora.app.AuroraAlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.ContentObserver;
//import android.hardware.display.DisplayManager;
//import android.hardware.display.WifiDisplay;
//import android.hardware.display.WifiDisplayStatus;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import aurora.preference.AuroraPreference;
//import aurora.preference.AuroraPreferenceActivity;
//import aurora.preference.AuroraPreferenceCategory;
//import aurora.preference.AuroraPreferenceGroup;
//import aurora.preference.AuroraPreferenceScreen;
//import android.provider.Settings;
//import android.text.Html;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.CompoundButton;
//import aurora.widget.AuroraEditText;
//import android.widget.ImageView;
//import aurora.widget.AuroraSwitch;
//import android.widget.TextView;
//
//import com.android.settings.ProgressCategory;
//import com.android.settings.R;
//import com.android.settings.SettingsPreferenceFragment;
////Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
//import com.gionee.settings.utils.GnUtils;
////Gionee <wangguojing> <2013-07-25> add for CR00837650 end
//
///**
// * The Settings screen for WifiDisplay configuration and connection management.
// */
//public final class WifiDisplaySettings extends SettingsPreferenceFragment {
//    private static final String TAG = "WifiDisplaySettings";
//
//    private static final int MENU_ID_SCAN = Menu.FIRST;
//
//    private DisplayManager mDisplayManager;
//
//    private boolean mWifiDisplayOnSetting;
//    private WifiDisplayStatus mWifiDisplayStatus;
//
//    private AuroraPreferenceGroup mPairedDevicesCategory;
//    private ProgressCategory mAvailableDevicesCategory;
//
//    private TextView mEmptyView;
//
//    private AuroraSwitch mActionBarSwitch;
//    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
//    private  static WifiDisplaySettings mWifiDisplaySettings ;
//    public static WifiDisplaySettings getWifiDisplaySettingsInstance() {
//        return mWifiDisplaySettings ;
//		
//    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        GnUtils.setSettingsmkey(null) ;
//    }
//
//    //Gionee <wangguojing> <2013-07-25> add for CR00837650 end
//
//    public WifiDisplaySettings() {
//        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
//        mWifiDisplaySettings = this;
//        GnUtils.setSettingsmkey(TAG) ;
//        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
//    }
//
//    @Override
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//
//        mDisplayManager = (DisplayManager)getActivity().getSystemService(Context.DISPLAY_SERVICE);
//
//        addPreferencesFromResource(R.xml.wifi_display_settings);
//        setHasOptionsMenu(true);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        AuroraActivity activity = (AuroraActivity)getActivity();
//        mActionBarSwitch = new AuroraSwitch(activity);
//        if (activity instanceof AuroraPreferenceActivity) {
//            AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
//            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
//                final int padding = activity.getResources().getDimensionPixelSize(
//                        R.dimen.action_bar_switch_padding);
//                mActionBarSwitch.setPadding(0, 0, padding, 0);
//		//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
//		/*
//                activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
//                        AuroraActionBar.DISPLAY_SHOW_CUSTOM);
//                activity.getAuroraActionBar().setCustomView(mActionBarSwitch,
//                        new AuroraActionBar.LayoutParams(
//                                AuroraActionBar.LayoutParams.WRAP_CONTENT,
//                                AuroraActionBar.LayoutParams.WRAP_CONTENT,
//                                Gravity.CENTER_VERTICAL | Gravity.END));
//		*/
//		//AURORA-END::delete temporarily for compile::waynelin::2013-9-14
//            }
//        }
//
//        mActionBarSwitch.setOnCheckedChangeListener(mSwitchOnCheckedChangedListener);
//
//        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
//        getListView().setEmptyView(mEmptyView);
//
//        update();
//
//        if (mWifiDisplayStatus.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE) {
//            activity.finish();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        Context context = getActivity();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED);
//        context.registerReceiver(mReceiver, filter);
//
//        //Gionee <wangguojing> <2013-08-22> modify for CR00845996 begin
//        getContentResolver().registerContentObserver(Settings.Global.getUriFor(
//                Settings.Global.WIFI_DISPLAY_ON), false, mSettingsObserver);
//        //Gionee <wangguojing> <2013-08-22> modify for CR00845996 end
//
//        mDisplayManager.scanWifiDisplays();
//
//        update();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        Context context = getActivity();
//        context.unregisterReceiver(mReceiver);
//
//        getContentResolver().unregisterContentObserver(mSettingsObserver);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        MenuItem item = menu.add(Menu.NONE, MENU_ID_SCAN, 0,
//                mWifiDisplayStatus.getScanState() == WifiDisplayStatus.SCAN_STATE_SCANNING ?
//                        R.string.wifi_display_searching_for_devices :
//                                R.string.wifi_display_search_for_devices);
//        item.setEnabled(mWifiDisplayStatus.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON
//                && mWifiDisplayStatus.getScanState() == WifiDisplayStatus.SCAN_STATE_NOT_SCANNING);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        //Gionee <wangguojing> <2013-08-05> add for CR00845998 begin
//        item.setIcon(R.drawable.ic_search);
//        //Gionee <wangguojing> <2013-08-05> add for CR00845998 end
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case MENU_ID_SCAN:
//                if (mWifiDisplayStatus.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON) {
//                    mDisplayManager.scanWifiDisplays();
//                }
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
//            AuroraPreference preference) {
//        if (preference instanceof WifiDisplayPreference) {
//            WifiDisplayPreference p = (WifiDisplayPreference)preference;
//            WifiDisplay display = p.getDisplay();
//
//            if (display.equals(mWifiDisplayStatus.getActiveDisplay())) {
//                showDisconnectDialog(display);
//            } else {
//                mDisplayManager.connectWifiDisplay(display.getDeviceAddress());
//            }
//        }
//
//        return super.onPreferenceTreeClick(preferenceScreen, preference);
//    }
//
//    private void update() {
//        mWifiDisplayOnSetting = Settings.Global.getInt(getContentResolver(),
//                Settings.Global.WIFI_DISPLAY_ON, 0) != 0;
//        mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
//
//        applyState();
//    }
//
//    private void applyState() {
//        final int featureState = mWifiDisplayStatus.getFeatureState();
//        mActionBarSwitch.setEnabled(featureState != WifiDisplayStatus.FEATURE_STATE_DISABLED);
//        mActionBarSwitch.setChecked(mWifiDisplayOnSetting);
//
//        final AuroraPreferenceScreen preferenceScreen = getPreferenceScreen();
//        preferenceScreen.removeAll();
//
//        if (featureState == WifiDisplayStatus.FEATURE_STATE_ON) {
//            final WifiDisplay[] pairedDisplays = mWifiDisplayStatus.getRememberedDisplays();
//            final WifiDisplay[] availableDisplays = mWifiDisplayStatus.getAvailableDisplays();
//
//            if (mPairedDevicesCategory == null) {
//                mPairedDevicesCategory = new AuroraPreferenceCategory(getActivity());
//                mPairedDevicesCategory.setTitle(R.string.wifi_display_paired_devices);
//            } else {
//                mPairedDevicesCategory.removeAll();
//            }
//            preferenceScreen.addPreference(mPairedDevicesCategory);
//
//            for (WifiDisplay d : pairedDisplays) {
//                mPairedDevicesCategory.addPreference(createWifiDisplayPreference(d, true));
//            }
//            if (mPairedDevicesCategory.getPreferenceCount() == 0) {
//                preferenceScreen.removePreference(mPairedDevicesCategory);
//            }
//
//            if (mAvailableDevicesCategory == null) {
//                mAvailableDevicesCategory = new ProgressCategory(getActivity(), null,
//                        R.string.wifi_display_no_devices_found);
//                mAvailableDevicesCategory.setTitle(R.string.wifi_display_available_devices);
//            } else {
//                mAvailableDevicesCategory.removeAll();
//            }
//            preferenceScreen.addPreference(mAvailableDevicesCategory);
//
//            for (WifiDisplay d : availableDisplays) {
//                if (!contains(pairedDisplays, d.getDeviceAddress())) {
//                    mAvailableDevicesCategory.addPreference(createWifiDisplayPreference(d, false));
//                }
//            }
//            if (mWifiDisplayStatus.getScanState() == WifiDisplayStatus.SCAN_STATE_SCANNING) {
//                mAvailableDevicesCategory.setProgress(true);
//            } else {
//                mAvailableDevicesCategory.setProgress(false);
//            }
//        } else {
//            mEmptyView.setText(featureState == WifiDisplayStatus.FEATURE_STATE_OFF ?
//                    R.string.wifi_display_settings_empty_list_wifi_display_off :
//                            R.string.wifi_display_settings_empty_list_wifi_display_disabled);
//        }
//
//        getActivity().invalidateOptionsMenu();
//    }
//
//    private AuroraPreference createWifiDisplayPreference(final WifiDisplay d, boolean paired) {
//        WifiDisplayPreference p = new WifiDisplayPreference(getActivity(), d);
//        if (d.equals(mWifiDisplayStatus.getActiveDisplay())) {
//            switch (mWifiDisplayStatus.getActiveDisplayState()) {
//                case WifiDisplayStatus.DISPLAY_STATE_CONNECTED:
//                    p.setSummary(R.string.wifi_display_status_connected);
//                    break;
//                case WifiDisplayStatus.DISPLAY_STATE_CONNECTING:
//                    p.setSummary(R.string.wifi_display_status_connecting);
//                    break;
//            }
//        } else if (paired && contains(mWifiDisplayStatus.getAvailableDisplays(),
//                d.getDeviceAddress())) {
//            p.setSummary(R.string.wifi_display_status_available);
//        }
//        if (paired) {
//            p.setWidgetLayoutResource(R.layout.wifi_display_preference);
//        }
//        return p;
//    }
//
//    private void showDisconnectDialog(final WifiDisplay display) {
//        DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (display.equals(mWifiDisplayStatus.getActiveDisplay())) {
//                    mDisplayManager.disconnectWifiDisplay();
//                }
//            }
//        };
//
//        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getActivity())
//                .setCancelable(true)
//                .setTitle(R.string.wifi_display_disconnect_title)
//                .setMessage(Html.fromHtml(getResources().getString(
//                        R.string.wifi_display_disconnect_text, display.getFriendlyDisplayName())))
//                .setPositiveButton(android.R.string.ok, ok)
//                .setNegativeButton(android.R.string.cancel, null)
//                .create();
//        dialog.show();
//    }
//
//    private void showOptionsDialog(final WifiDisplay display) {
//        View view = getActivity().getLayoutInflater().inflate(R.layout.wifi_display_options, null);
//        final AuroraEditText nameEditText = (AuroraEditText)view.findViewById(R.id.name);
//        nameEditText.setText(display.getFriendlyDisplayName());
//
//        DialogInterface.OnClickListener done = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String name = nameEditText.getText().toString().trim();
//                if (name.isEmpty() || name.equals(display.getDeviceName())) {
//                    name = null;
//                }
//                mDisplayManager.renameWifiDisplay(display.getDeviceAddress(), name);
//            }
//        };
//        DialogInterface.OnClickListener forget = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mDisplayManager.forgetWifiDisplay(display.getDeviceAddress());
//            }
//        };
//
//        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getActivity())
//                .setCancelable(true)
//                .setTitle(R.string.wifi_display_options_title)
//                .setView(view)
//                .setPositiveButton(R.string.wifi_display_options_done, done)
//                .setNegativeButton(R.string.wifi_display_options_forget, forget)
//                .create();
//        dialog.show();
//    }
//
//    private static boolean contains(WifiDisplay[] displays, String address) {
//        for (WifiDisplay d : displays) {
//            if (d.getDeviceAddress().equals(address)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private final CompoundButton.OnCheckedChangeListener mSwitchOnCheckedChangedListener =
//            new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            mWifiDisplayOnSetting = isChecked;
//            Settings.Global.putInt(getContentResolver(),
//                    Settings.Global.WIFI_DISPLAY_ON, isChecked ? 1 : 0);
//        }
//    };
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
//                WifiDisplayStatus status = (WifiDisplayStatus)intent.getParcelableExtra(
//                        DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
//                mWifiDisplayStatus = status;
//                applyState();
//            }
//        }
//    };
//
//    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            update();
//        }
//    };
//
//    private final class WifiDisplayPreference extends AuroraPreference
//            implements View.OnClickListener {
//        private final WifiDisplay mDisplay;
//
//        public WifiDisplayPreference(Context context, WifiDisplay display) {
//            super(context);
//
//            mDisplay = display;
//            setTitle(display.getFriendlyDisplayName());
//        }
//
//        public WifiDisplay getDisplay() {
//            return mDisplay;
//        }
//
//        @Override
//        protected void onBindView(View view) {
//            super.onBindView(view);
//
//            ImageView deviceDetails = (ImageView) view.findViewById(R.id.deviceDetails);
//            if (deviceDetails != null) {
//                deviceDetails.setOnClickListener(this);
//
//                if (!isEnabled()) {
//                    TypedValue value = new TypedValue();
//                    getContext().getTheme().resolveAttribute(android.R.attr.disabledAlpha,
//                            value, true);
//                    deviceDetails.setImageAlpha((int)(value.getFloat() * 255));
//                }
//            }
//        }
//
//        @Override
//        public void onClick(View v) {
//            showOptionsDialog(mDisplay);
//        }
//    }
//}
