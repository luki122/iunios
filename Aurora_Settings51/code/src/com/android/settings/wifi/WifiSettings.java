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
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.preference.*;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSystemMenu;

import com.android.settings.AuroraRestrictedSettingsFragment;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import com.mediatek.settings.FeatureOption;
import com.mediatek.wifi.WifiSettingsExt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Two types of UI are provided here.
 *
 * The first is for "usual Settings", appearing as any other Setup fragment.
 *
 * The second is for Setup Wizard, with a simplified interface that hides the action bar
 * and menus.
 */
public class WifiSettings extends AuroraRestrictedSettingsFragment
        implements DialogInterface.OnClickListener, Indexable  {

    private static final String TAG = "WifiSettings";

    private AuroraActivity mActivity;
    /* package */ static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_SAVED_NETWORK = Menu.FIRST + 2;
    /* package */ static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 3;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
    private static final int MENU_ID_SCAN = Menu.FIRST + 5;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
    private static final int MENU_ID_FORGET = Menu.FIRST + 7;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 8;
    private static final int MENU_ID_WRITE_NFC = Menu.FIRST + 9;
    private static final int MENU_ID_MORE = Menu.FIRST + 10;

    public static final int WIFI_DIALOG_ID = 1;
    /* package */ static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int WRITE_NFC_DIALOG_ID = 6;

    // Combo scans can take 5-6s to complete - set to 10s.
    /// M: change interval time to 6s
    private static final int WIFI_RESCAN_INTERVAL_MS = 6 * 1000;

    // Instance state keys
    private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";

    private static boolean savedNetworksExist;
    private boolean isSaveInstanceStated = false; //Aurora linchunhui 20160330 modify for BUG 21880

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;

    /* package */ WifiManager mWifiManager;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;

    private AuroraWifiEnabler mWifiEnabler;
    // An access point being editted is stored here.
    public AccessPoint mSelectedAccessPoint;

    private NetworkInfo mLastNetworkInfo;
    private WifiInfo mLastInfo;

    private final AtomicBoolean mConnected = new AtomicBoolean(false);

    private WifiDialog mDialog;
    private WriteWifiConfigToNfcDialog mWifiToNfcDialog;

    private TextView mEmptyView;

    // this boolean extra specifies whether to disable the Next button when not connected. Used by
    // account creation outside of setup wizard.
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";
    // This string extra specifies a network to open the connect dialog on, so the user can enter
    // network credentials.  This is used by quick settings for secured networks.
    private static final String EXTRA_START_CONNECT_SSID = "wifi_start_connect_ssid";

    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;

    // Save the dialog details
    private boolean mDlgEdit;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;
    /// M: to solve Write to NFC tag JE @{
    private static final String SAVE_NFC_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state1";
    private Bundle mNFCAccessPointSavedState;
    /// @}

    /** verbose logging flag. this flag is set thru developer debugging options
     * and used so as to assist with in-the-field WiFi connectivity debugging  */
    public static int mVerboseLogging = 0;

    /* End of "used in Wifi Setup context" */

    /** A restricted multimap for use in constructAccessPoints */
    private static class Multimap<K,V> {
        private final HashMap<K,List<V>> store = new HashMap<K,List<V>>();
        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V>emptyList();
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

    private static class Scanner extends Handler {
        private int mRetry = 0;
        private WifiSettings mWifiSettings = null;

        Scanner(WifiSettings wifiSettings) {
            mWifiSettings = wifiSettings;
        }

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
            if (mWifiSettings.mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Activity activity = mWifiSettings.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_fail_to_scan, Toast.LENGTH_LONG).show();
                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }


    /// M: print performance log
    private static boolean mScanResultsAvailable = false;

    ///M: add mtk feature
    private WifiSettingsExt mWifiSettingsExt;

    public WifiSettings() {
        super(DISALLOW_CONFIG_WIFI);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        /// M: add no WAPI certification action
        mFilter.addAction(WifiManager.NO_CERTIFICATION_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };

        mScanner = new Scanner(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mConnectListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                       /// M: update priority after connnect AP @{
                                       mWifiSettingsExt.updatePriority();
                                       /// @}
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                                R.string.wifi_failed_connect_message,
                                                Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        mSaveListener = new WifiManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    /// M: update priority after modify AP config @{
                                    mWifiSettingsExt.updatePriority();
                                    /// @}
                                }
                                @Override
                                public void onFailure(int reason) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        Toast.makeText(activity,
                                            R.string.wifi_failed_save_message,
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };

        mForgetListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                       /// M: update priority after connnect AP @{
                                       mWifiSettingsExt.updatePriority();
                                       /// @}
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                               R.string.wifi_failed_forget_message,
                                               Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        if (savedInstanceState != null) {
            mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
            if (savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
                mAccessPointSavedState =
                    savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
            }
            /// M: to solve Write to NFC tag JE @{
            if (savedInstanceState.containsKey(SAVE_NFC_DIALOG_ACCESS_POINT_STATE)) {
                mNFCAccessPointSavedState =
                    savedInstanceState.getBundle(SAVE_NFC_DIALOG_ACCESS_POINT_STATE);
            }
            /// @}
        }

        // if we're supposed to enable/disable the Next button based on our current connection
        // state, start it off in the right state
        Intent intent = getActivity().getIntent();
        mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);

        if (mEnableNextOnConnection) {
            if (hasNextButton()) {
                final ConnectivityManager connectivity = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo info = connectivity.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI);
                    changeNextButtonState(info.isConnected());
                }
            }
        }

        //addPreferencesFromResource(R.xml.wifi_settings);
        Log.d(TAG, "onActivityCreated----->>> initPrefence");
        addPreferencesFromResource(R.xml.aurora_wifi_settings);
        initPrefence();

        mEmptyView = initEmptyView();
        registerForContextMenu(getListView());//原生添加长按wifi列表弹出menu的方法,已被屏蔽
        setHasOptionsMenu(true); //原生添加menu的方法,已被屏蔽

        ///M: add mtk feature
        mWifiSettingsExt.onActivityCreated(this, mWifiManager);


        if (intent.hasExtra(EXTRA_START_CONNECT_SSID)) {
            String ssid = intent.getStringExtra(EXTRA_START_CONNECT_SSID);
            updateAccessPoints();
            AuroraPreferenceScreen preferenceScreen = (AuroraPreferenceScreen)getPreferenceScreen();
            for (int i = 0; i < getAccessPointsCount(); i++) {
                AuroraPreference preference = mWifiSettingsExt.getPreference(preferenceScreen, i);
                if (preference instanceof AccessPoint) {
                    AccessPoint accessPoint = (AccessPoint) preference;
                    if (ssid.equals(accessPoint.ssid) && accessPoint.networkId == -1
                            && accessPoint.security != AccessPoint.SECURITY_NONE) {
                        onPreferenceTreeClick(preferenceScreen, preference);
                        break;
                    }
                }
            }
        }
    }

    private static final String ADVANCED_KEY="wifi_advanced_settings";
    private static final String SWITCH_KEY="wifi_switch";
    private static final String WIFI_LIST="wifi_list";
    private AuroraSwitchPreference switchPreference;
    private AuroraPreferenceScreen advancedPScreen;
    private AuroraPreferenceCategory mWifiPreferenceCategory;
    private AuroraActionBar mActionBar;
    public AuroraSystemMenu mAuroraMenu;

    private void initPrefence() {
        mActivity = (AuroraActivity) getActivity();
        mActivity.setAuroraMenuItems(R.menu.aurora_wifi_settings_menu);
        mAuroraMenu = mActivity.getAuroraMenu();
        if (mAuroraMenu != null) {
               mAuroraMenu.setAnimationStyle(com.aurora.R.style.AuroraMenuRightTopAnimation);
               mActivity.setAuroraSystemMenuCallBack(auroraSystemMenuCallBack);
        }

    	mActionBar = mActivity.getAuroraActionBar();
    	mActionBar.addItem(AuroraActionBarItem.Type.More, MENU_ID_MORE);
    	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
    	switchPreference=(AuroraSwitchPreference)findPreference(SWITCH_KEY);
    	Log.d(TAG, " switchPreference initialize success  !!!!");
    	mWifiPreferenceCategory=(AuroraPreferenceCategory)findPreference(WIFI_LIST);
    }

    public  AuroraPreferenceCategory getPreferenceCategory() {
    	return mWifiPreferenceCategory;
    }

    /*
     * aurora action bar 点击事件处理方法
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener=new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int arg0) {
			switch (arg0) {
			case MENU_ID_MORE:

                                /* 根据手机的分辨率从 dp 的单位 转成为 px(像素)*/
                                float scale = mActivity.getResources().getDisplayMetrics().density;
                                int x = (int) (33.0 * scale + 0.5f);
                                int y = (int) (37.0 * scale + 0.5f);

                                if (!mWifiManager.isWifiEnabled()) {
                                     mAuroraMenu.removeMenuByItemId(R.id.add_wifi);
                                     mAuroraMenu.removeMenuByItemId(R.id.scan_wifi);
                                } else {
                                     mAuroraMenu.addMenu(R.id.add_wifi, R.string.wifi_add_network, 0, 0);
                                     mAuroraMenu.addMenu(R.id.scan_wifi, R.string.menu_stats_refresh, 0, 1);
                                }
                                mActivity.showAuroraMenu(mActionBar, Gravity.RIGHT | Gravity.TOP, 0, 0);
				break;
			default:
				break;
			}
		}
	};

    /*
     * aurora菜单点击事件处理方法
     */
    private OnAuroraMenuItemClickListener auroraSystemMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.add_wifi: {
                if (mWifiManager.isWifiEnabled()) {
                    onAddNetworkPressed();
                }
                break;
            }
            
            case R.id.scan_wifi: {
                if (mWifiManager.isWifiEnabled()) {
                     mScanner.forceScan();
                }
            	break;
            }

            case R.id.advance_wifi: {
                startFragment(WifiSettings.this, "com.android.settings.wifi.AdvancedWifiSettings",
                            R.string.wifi_advanced_titlebar, -1 /* Do not request a results */,
                            null);
            	break;
            }
            default:
                break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mWifiEnabler != null) {
            mWifiEnabler.teardownSwitchBar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "  onStart  ");
        // On/off switch is hidden for Setup Wizard (returns null)
        if(mWifiEnabler==null)
        {
        	mWifiEnabler = createWifiEnabler();
        }
    }

    /**
     * @return new WifiEnabler or null (as overridden by WifiSettingsFor zard)
     */
    /* package */ AuroraWifiEnabler createWifiEnabler() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if(switchPreference==null)
        {
        	Log.d(TAG, "  switchPreference is null !!!!  ");
        	switchPreference=(AuroraSwitchPreference)findPreference(SWITCH_KEY);
        }
        return new AuroraWifiEnabler(activity, switchPreference);
//        return new WifiEnabler(activity, activity.getSwitchBar());
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        super.onResume();
        isSaveInstanceStated = false; //Aurora linchunhui 20160330 modify for BUG 21880
        if (mWifiEnabler != null) {
            mWifiEnabler.resume(activity);
        }

        activity.registerReceiver(mReceiver, mFilter);
        updateAccessPoints();

        ///M: add mtk feature
        mWifiSettingsExt.onResume();
        mActivity.setAuroraSystemMenuCallBack(auroraSystemMenuCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }

        mActivity.setAuroraSystemMenuCallBack(null);

        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If the dialog is showing, save its state.
        if (mDialog != null && mDialog.isShowing()) {
            outState.putBoolean(SAVE_DIALOG_EDIT_MODE, mDlgEdit);
            if (mDlgAccessPoint != null) {
                mAccessPointSavedState = new Bundle();
                mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, mAccessPointSavedState);
            }
        }
        /// M: to solve Write to NFC tag JE @{
        if (mWifiToNfcDialog != null && mWifiToNfcDialog.isShowing()) {
            if (mSelectedAccessPoint != null) {
                mNFCAccessPointSavedState = new Bundle();
                mSelectedAccessPoint.saveWifiState(mNFCAccessPointSavedState);
                outState.putBundle(SAVE_NFC_DIALOG_ACCESS_POINT_STATE, mNFCAccessPointSavedState);
            }
        }
        /// @}
        isSaveInstanceStated = true; //Aurora linchunhui 20160330 modify for BUG 21880
    } 
    
    /*
     * aurora添加, ap点击事件处理方法
     */
    public void doPreferenceTreeClick(AccessPoint mAccessPoint , boolean showDetail)
    {

        mSelectedAccessPoint = mAccessPoint;
        Log.d("gd", " ssid="+mAccessPoint.ssid+"   networkId="+mAccessPoint.networkId+"  showDetail="+showDetail);
        if (showDetail) {
            	showDialog(mSelectedAccessPoint, false);
                return;
        }
        /*Aurora linchunhui 20160226 modify begin */
        if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
            if (!mSelectedAccessPoint.isActive()) {
                    if (mSelectedAccessPoint.getSummary() != null && 
                        getActivity().getString(R.string.wifi_disabled_password_failure).equals(mSelectedAccessPoint.getSummary())){
                           //点击身份验证失败ap弹出详情页
                           showDetailUI(mSelectedAccessPoint, false);
                    } else if (mSelectedAccessPoint.mRssi == Integer.MAX_VALUE) {
                           //点击不在信号范围内ap弹出详情页
                           showDetailUI(mSelectedAccessPoint, false);
                    } else {
                           //点击已保存/身份验证失败的ap,直接进行连接
                           connect(mSelectedAccessPoint.networkId);
                    }
            } else {
                    //点击已连接的ap,打开详情页
                    showDetailUI(mSelectedAccessPoint, false);
            }

        } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
            /** Bypass dialog for unsecured networks */ 
            //点击免验证的ap,直接进行连接
            mSelectedAccessPoint.generateOpenNetworkConfig();
            connect(mSelectedAccessPoint.getConfig());

        } else {
            //点击未保存过的ap,打开输入密码对话框
            showDialog(mSelectedAccessPoint, false);
        }
        /*Aurora linchunhui 20160226 modify end */
    }
    
    /*
     * Aurora linchunhui add, ap长按点击事件处理方法
     */
    public void doPreferenceTreeLongClick(AccessPoint mAccessPoint) {
        mSelectedAccessPoint = mAccessPoint;
        if (mSelectedAccessPoint == null) {
              return;
        }
        if (ActivityManager.getCurrentUser() == UserHandle.USER_OWNER &&
            (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID ||
             (mSelectedAccessPoint.getNetworkInfo() != null &&
              mSelectedAccessPoint.getNetworkInfo().getState() != State.DISCONNECTED))) {

              AuroraAlertDialog forgetDialog = new AuroraAlertDialog.Builder(getActivity())
               .setTitle(R.string.ignore_wifi_ap)
               .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                 forget();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
              forgetDialog.show();

        }
    }

    public static final String KEY_CAN_EDIT="can_edit";
    public static final String KEY_ACCESS_POINT="access_point";
    public static final int WIFI_REQUEST_CODE=101;

    public void preShow(AccessPoint accessPoint, boolean edit) {
        if (mDialog != null) {
            removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
            mAccessPointSavedState = null;
        }

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDlgEdit = edit;
        
        AccessPoint ap = mDlgAccessPoint; // For manual launch
        if (ap == null) { // For re-launch from saved state
            if (mAccessPointSavedState != null) {
                ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                // For repeated orientation changes
                mDlgAccessPoint = ap;
                // Reset the saved access point data
                mAccessPointSavedState = null;
            }
        }
        // If it's null, fine, it's for Add Network
        mSelectedAccessPoint = ap;
        ///M: add mtk feature
        if (mSelectedAccessPoint != null) {
            mWifiSettingsExt.recordPriority(mSelectedAccessPoint.getConfig());
        }
    }

    public void showDetailUI(AccessPoint accessPoint, boolean edit) {

        preShow(accessPoint, edit);

        Intent intent = new Intent();
        intent.setClass(getActivity(), AuroraWifiDetailUi.class);

        if (mSelectedAccessPoint != null) {
            Bundle bundle = new Bundle();
            mSelectedAccessPoint.saveWifiState(bundle);
            Bundle wifiInfoBundle = new Bundle();
            wifiInfoBundle.putBoolean(KEY_CAN_EDIT, mDlgEdit);
            wifiInfoBundle.putBundle(KEY_ACCESS_POINT, bundle);
            intent.putExtras(wifiInfoBundle);
        }
        startActivityForResult(intent, WIFI_REQUEST_CODE);

    }

    public void showDialog(AccessPoint accessPoint, boolean edit) {

        preShow(accessPoint, edit);
        //Aurora linchunhui 20160330 modify for BUG 21880 start 
        if (!isSaveInstanceStated) {
             showDialog(WIFI_DIALOG_ID);
        }
        //Aurora linchunhui 20160330 modify for BUG 21880 end          
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                mDialog = new WifiDialog(getActivity(), this, mSelectedAccessPoint, mDlgEdit);
                return mDialog;
            case WPS_PBC_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.PBC);
            case WPS_PIN_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.DISPLAY);
            case WRITE_NFC_DIALOG_ID:
                if (mSelectedAccessPoint != null) {
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(
                            getActivity(), mSelectedAccessPoint, mWifiManager);
                    return mWifiToNfcDialog;
                } else {
                    AccessPoint NfcAccessPoint = mSelectedAccessPoint; // For manual launch
                    if (NfcAccessPoint == null) { // For re-launch from saved state
                        if (mNFCAccessPointSavedState != null) {
                            NfcAccessPoint = new AccessPoint(getActivity(), mNFCAccessPointSavedState);
                            // Reset the saved access point data
                            mNFCAccessPointSavedState = null;
                        }
                    }
                    // If it's null, fine, it's for Add Network
                    mSelectedAccessPoint = NfcAccessPoint;
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(
                            getActivity(), NfcAccessPoint, mWifiManager);
                    return mWifiToNfcDialog;
                }

        }
        return super.onCreateDialog(dialogId);
    }

    /**
     * Shows the latest access points available with supplemental information like
     * the strength of network and the security for it.
     */
    private void updateAccessPoints() {
        // Safeguard from some delayed event handling
        if (getActivity() == null) return;

        if (isUiRestricted()) {
            addMessagePreference(R.string.wifi_empty_list_user_restricted);
            return;
        }
        final int wifiState = mWifiManager.getWifiState();

        //when we update the screen, check if verbose logging has been turned on or off
        mVerboseLogging = mWifiManager.getVerboseLoggingLevel();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                final Collection<AccessPoint> accessPoints =
                        constructAccessPoints(getActivity(), mWifiSettingsExt, mWifiManager, mLastInfo,
                                mLastNetworkInfo);
                //getPreferenceScreen().removeAll();
//                mWifiSettingsExt.emptyCategory(getPreferenceScreen());
                mWifiSettingsExt.emptyCategory(mWifiPreferenceCategory);
                
                if (accessPoints.size() == 0) {
                    addMessagePreference(R.string.wifi_empty_list_wifi_on);
                }

                /// M: add ap to screen @{
                Log.d(TAG, "accessPoints.size() = "  + accessPoints.size());
                for (AccessPoint accessPoint : accessPoints) {
                	accessPoint.setFragment(WifiSettings.this);
                    // Ignore access points that are out of range.
                    //if (accessPoint.getLevel() != -1) {
                        mWifiSettingsExt.addPreference(mWifiPreferenceCategory, accessPoint, accessPoint.getConfig() != null);
                  //  }
                }
                mWifiSettingsExt.refreshCategory(mWifiPreferenceCategory);
                /// @}
                /// M: print performance log @{
                if (mScanResultsAvailable) {
                    long endTime = System.currentTimeMillis();
                    Log.i(TAG, "[Performance test][Settings][wifi] wifi search end [" + endTime + "]");
                    mScanResultsAvailable = false;
                }
                /// @}
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                mWifiSettingsExt.emptyScreen(mWifiPreferenceCategory);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
            	//mActionBar.getItem(0).getItemView().setEnabled(false);
                addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
            	//mActionBar.getItem(0).getItemView().setEnabled(false);
            	addMessagePreference(R.string.wifi_empty_list_wifi_off);
                setOffMessage();
                break;
        }
    }

    protected TextView initEmptyView() {
        TextView emptyView = (TextView) getActivity().findViewById(android.R.id.empty);
        getListView().setEmptyView(emptyView);
        return emptyView;
    }

    private void setOffMessage() {
        if (mEmptyView != null) {
            mEmptyView.setText(R.string.wifi_empty_list_wifi_off);
            if (android.provider.Settings.Global.getInt(getActivity().getContentResolver(),
                    android.provider.Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1) {
                mEmptyView.append("\n\n");
                int resId;
                if (android.provider.Settings.Secure.isLocationProviderEnabled(
                        getActivity().getContentResolver(), LocationManager.NETWORK_PROVIDER)) {
                    resId = R.string.wifi_scan_notify_text_location_on;
                } else {
                    resId = R.string.wifi_scan_notify_text_location_off;
                }
                CharSequence charSeq = getText(resId);
                mEmptyView.append(charSeq);
            }
        }
        mWifiSettingsExt.emptyScreen(mWifiPreferenceCategory);
    }

    private void addMessagePreference(int messageId) {
        if (mEmptyView != null) mEmptyView.setText(messageId);
        /// M: empty ap list
        mWifiSettingsExt.emptyScreen(mWifiPreferenceCategory);
    }

    /** Returns sorted list of access points */
    //M:add parameter wifiSettingsExt for Google set this method static by Parish Li
    private static List<AccessPoint> constructAccessPoints(Context context,
            WifiSettingsExt wifiSettingsExt, WifiManager wifiManager, WifiInfo lastInfo, NetworkInfo lastNetworkInfo) {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null) {
            // Update "Saved Networks" menu option.
            if (savedNetworksExist != (configs.size() > 0)) {
                savedNetworksExist = !savedNetworksExist;
                if (context instanceof Activity) {
                    ((Activity) context).invalidateOptionsMenu();
                }
            }
        	
            for (WifiConfiguration config : configs) {
                if (config.selfAdded && config.numAssociation == 0) {
                    continue;
                }

                /// M: Add for EAP-SIM begin @{
                if (wifiSettingsExt.hasChangedSimCard(config, wifiManager, 
                        AccessPoint.removeDoubleQuotes(config.SSID), AccessPoint.getSecurity(config))) {
                    continue;
                }
                /// @}

                AccessPoint accessPoint = new AccessPoint(context, config);
                if (lastInfo != null && lastNetworkInfo != null) {
                    accessPoint.update(lastInfo, lastNetworkInfo);
                }
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = wifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                /// M: print performance log
                mScanResultsAvailable = true;
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(context, result);
                    if (lastInfo != null && lastNetworkInfo != null) {
                        accessPoint.update(lastInfo, lastNetworkInfo);
                    }
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "handleEvent(), action = " + action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                /// print performance log @{
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    mScanResultsAvailable = true;
                }
                ///@}
                updateAccessPoints();
/*Aurora linchunhui 20160305 modify
        } else if (WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action)) {
                WifiConfiguration config = (WifiConfiguration) intent.getExtra(WifiManager.EXTRA_WIFI_CONFIGURATION, null);
                if (null != config && config.disableReason == WifiConfiguration.DISABLED_AUTH_FAILURE) {
                      showDialog(mSelectedAccessPoint, false);
                }
*/
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateNetworkInfo(info);
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateNetworkInfo(null);
        } else if (WifiManager.NO_CERTIFICATION_ACTION.equals(action)) {
            /// M: show error message @{
            String apSSID = "";
            if (mSelectedAccessPoint != null) {
                apSSID = "[" + mSelectedAccessPoint.ssid + "] ";
            }
            Log.i(TAG, "Receive  no certification broadcast for AP " + apSSID);
            String message = getResources().getString(R.string.wifi_no_cert_for_wapi) + apSSID;
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            /// @}
        }
    }

    private void updateNetworkInfo(NetworkInfo networkInfo) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (networkInfo != null &&
                networkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (networkInfo != null) {
            mLastNetworkInfo = networkInfo;
        }

        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            AuroraPreference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastNetworkInfo);
            }
            ///M: add mtk feature
            updateAP(preference);
        }
    }

    private void updateWifiState(int state) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
            	addMessagePreference(R.string.wifi_empty_list_wifi_off);
                setOffMessage();
                break;
        }

        mLastInfo = null;
        mLastNetworkInfo = null;
        mScanner.pause();
    }

    /**
     * Renames/replaces "Next" button when appropriate. "Next" button usually exists in
     * Wifi setup screens, not in usual wifi settings screen.
     *
     * @param enabled true when the device is connected to a wifi network.
     */
    private void changeNextButtonState(boolean enabled) {
        if (mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(enabled);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
            forget();
        } else if (button == WifiDialog.BUTTON_SUBMIT) {
            if (mDialog != null) {
                //submit(mDialog.getController());
            }
        }
    }

    /* package */ void submit(WifiConfigController configController) {

        final WifiConfiguration config = configController.getConfig();
        Log.d(TAG, "submit, config = " + config);
        
        //Add for EAP-SIM,remind user when he use eap-sim/aka in a wrong way
        if (mWifiSettingsExt.hasSimAkaProblem((config != null) ? config : mSelectedAccessPoint.getConfig(),
                getContentResolver())) {
            return;
        }
        ///M: add mtk feature
        if (mSelectedAccessPoint != null) {
            mWifiSettingsExt.submit(config, mSelectedAccessPoint, mSelectedAccessPoint.networkId,
                    mSelectedAccessPoint.getState());
        }

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                if (mSelectedAccessPoint.getState() == null) {
                    connect(mSelectedAccessPoint.networkId);
                }
            }
        } else if (config.networkId != INVALID_NETWORK_ID) {
            if (mSelectedAccessPoint != null) {
                mWifiManager.save(config, mSaveListener);
            }
        } else {
            if (configController.isEdit()) {
                mWifiManager.save(config, mSaveListener);
            } else {
                connect(config);
            }
        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
    }

    /* package */ void forget() {
        if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
            if (mSelectedAccessPoint.getNetworkInfo().getState() != State.DISCONNECTED) {
                // Network is active but has no network ID - must be ephemeral.
                mWifiManager.disableEphemeralNetwork(
                        AccessPoint.convertToQuotedString(mSelectedAccessPoint.ssid));
            } else {
                // Should not happen, but a monkey seems to trigger it
                Log.e(TAG, "Failed to forget invalid network " + mSelectedAccessPoint.getConfig());
                return;
            }
        } else {
            mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
        }


        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();

        // We need to rename/replace "Next" button in wifi setup context.
        changeNextButtonState(false);
        
        /// M: since we lost a configured AP, left ones priority need to be refreshed
        mWifiSettingsExt.updatePriority();
    }

    protected void connect(final WifiConfiguration config) {
        mWifiManager.connect(config, mConnectListener);
    }

    protected void connect(final int networkId) {
        mWifiManager.connect(networkId, mConnectListener);
    }

    /**
     * Refreshes acccess points and ask Wifi module to scan networks again.
     */
    /* package */ void refreshAccessPoints() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }

        mWifiSettingsExt.emptyCategory(mWifiPreferenceCategory);
    }

    /**
     * Called when "add network" button is pressed.
     */
    /* package */ void onAddNetworkPressed() {
        // No exact access point is selected.
        mSelectedAccessPoint = null;
        showDialog(null, true);
    }

    /* package */ int getAccessPointsCount() {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        if (wifiIsEnabled) {
            /// M: return ap count
            return mWifiSettingsExt.getAccessPointsCount(mWifiPreferenceCategory);
        } else {
            return 0;
        }
    }

    /**
     * Requests wifi module to pause wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void pauseWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.pause();
        }
    }

    /**
     * Requests wifi module to resume wifi scan. May be ignored when the module is disabled.
     */
    /* package */ void resumeWifiScan() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_wifi;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
                final Resources res = context.getResources();

                // Add fragment title
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.wifi_settings);
                data.screenTitle = res.getString(R.string.wifi_settings);
                data.keywords = res.getString(R.string.keywords_wifi);
                result.add(data);

                // Add available Wi-Fi access points
                WifiManager wifiManager =
                        (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //M:add for Google set this static
                WifiSettingsExt mwifiSettingsExt = new WifiSettingsExt(context);
                final Collection<AccessPoint> accessPoints =
                        constructAccessPoints(context, mwifiSettingsExt, wifiManager, null, null);
                for (AccessPoint accessPoint : accessPoints) {
                    // We are indexing only the saved Wi-Fi networks.
                    if (accessPoint.getConfig() == null) continue;
                    data = new SearchIndexableRaw(context);
                    data.title = accessPoint.getTitle().toString();
                    data.screenTitle = res.getString(R.string.wifi_settings);
                    data.enabled = enabled;
                    result.add(data);
                }

                return result;
            }
        };

        // add by penggangding begin
        private static WifiSettings mWifiSettings=null;
        private SharedPreferences statePreferences=null;
        private SharedPreferences.Editor stateEditor=null;
        private static final String STATETABLE="stateXML";
        public static WifiSettings getWifiSettingsInstance()
        {
        	return mWifiSettings;
        }
        public SharedPreferences getPrefrerence()
        {
        	return statePreferences;
        }
        public SharedPreferences.Editor getEdit()
        {
        	return stateEditor;
        }
        //  // add by penggangding end
        /**
         * M: add mtk feature and bug fix
         * @param icicle
         */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            mWifiSettings=this;
            statePreferences=getActivity().getSharedPreferences(STATETABLE, Activity.MODE_PRIVATE);
            stateEditor=statePreferences.edit();
            
            mWifiSettingsExt = new WifiSettingsExt(getActivity());
            mWifiSettingsExt.onCreate();
        }

        /**
         *M: unregister priority observer
         */
        @Override
        public void onDestroy() {
            mWifiSettingsExt.unregisterPriorityObserver(getContentResolver());
            super.onDestroy();
        }

        /**
         * M: update ap information
         * @param screen
         * @return
         */
        public void updateAP(AuroraPreference group) {
            if (group instanceof AuroraPreferenceGroup) {
                AuroraPreferenceGroup screen = (AuroraPreferenceGroup) group;
                Log.d(TAG, "updateAP, screen = " + screen);
                for (int i = screen.getPreferenceCount() - 1; i >= 0; --i) {
                    // Maybe there's a WifiConfigPreference
                    AuroraPreference preference = screen.getPreference(i);
                    if (preference instanceof AccessPoint) {
                        final AccessPoint accessPoint = (AccessPoint) preference;
                        accessPoint.update(mLastInfo, mLastNetworkInfo);
                    }
                }
            }
        }

    /**
     * Called when "add network" button in wifi gprs selected is pressed.
     */
    public void addNetworkForSelector() {
        mSelectedAccessPoint = null;
        showDialog(null, true);
    }

    /**
     * @ 原生点击wifi列表的事件方法,已被屏蔽
     */
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            /** Bypass dialog for unsecured, unsaved, and inactive networks */
            if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE &&
                    mSelectedAccessPoint.networkId == INVALID_NETWORK_ID &&
                    !mSelectedAccessPoint.isActive()) {
                /// M: support open ap wps test @{
                if (mSelectedAccessPoint.mAccessPointExt.isOpenApWPSSupported(mSelectedAccessPoint.wpsAvailable)) {
                    showDialog(mSelectedAccessPoint, false);
                } else {
                /// @}
                mSelectedAccessPoint.generateOpenNetworkConfig();
                if (!savedNetworksExist) {
                    savedNetworksExist = true;
                    getActivity().invalidateOptionsMenu();
                }
                connect(mSelectedAccessPoint.getConfig());
                }
            } else {
                showDialog(mSelectedAccessPoint, false);
            }
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }

    /**
     * @ 原生创建菜单键弹出menu的方法, 已被屏蔽
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the user is not allowed to configure wifi, do not show the menu.
        if (isUiRestricted()) return;

        addOptionsMenuItems(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * @param menu 添加菜单键弹出menu的item, 已被屏蔽
     */
    void addOptionsMenuItems(Menu menu) {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        TypedArray ta = getActivity().getTheme().obtainStyledAttributes(
                new int[] {R.attr.ic_menu_add, R.attr.ic_wps});
        menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
                .setIcon(ta.getDrawable(0))
                .setEnabled(wifiIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (savedNetworksExist) {
            menu.add(Menu.NONE, MENU_ID_SAVED_NETWORK, 0, R.string.wifi_saved_access_points_label)
                    .setIcon(ta.getDrawable(0))
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.menu_stats_refresh)
               .setEnabled(wifiIsEnabled)
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        ta.recycle();

        ///M: add mtk feature
        mWifiSettingsExt.onCreateOptionsMenu(wifiIsEnabled, menu);
    }

    /*
     *@ 原生菜单键menu item 点击事件, 已被屏蔽
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If the user is not allowed to configure wifi, do not handle menu selections.
        if (isUiRestricted()) return false;

        switch (item.getItemId()) {
            case MENU_ID_WPS_PBC:
                showDialog(WPS_PBC_DIALOG_ID);
                return true;
                /*
            case MENU_ID_P2P:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            WifiP2pSettings.class.getCanonicalName(),
                            null,
                            R.string.wifi_p2p_settings_title, null,
                            this, 0);
                } else {
                    startFragment(this, WifiP2pSettings.class.getCanonicalName(),
                            R.string.wifi_p2p_settings_title, -1, null);
                }
                return true;
                */
            case MENU_ID_WPS_PIN:
                showDialog(WPS_PIN_DIALOG_ID);
                return true;
            case MENU_ID_SCAN:
                if (mWifiManager.isWifiEnabled()) {
                    mScanner.forceScan();
                }
                return true;
            case MENU_ID_ADD_NETWORK:
                if (mWifiManager.isWifiEnabled()) {
                    onAddNetworkPressed();
                }
                return true;
            case MENU_ID_SAVED_NETWORK:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            SavedAccessPointsWifiSettings.class.getCanonicalName(), null,
                            R.string.wifi_saved_access_points_titlebar, null, this, 0);
                } else {
                    startFragment(this, SavedAccessPointsWifiSettings.class.getCanonicalName(),
                            R.string.wifi_saved_access_points_titlebar,
                            -1 /* Do not request a result */, null);
                }
                return true;
            case MENU_ID_ADVANCED:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            AdvancedWifiSettings.class.getCanonicalName(), null,
                            R.string.wifi_advanced_titlebar, null, this, 0);
                } else {
                    startFragment(this, AdvancedWifiSettings.class.getCanonicalName(),
                            R.string.wifi_advanced_titlebar, -1 /* Do not request a results */,
                            null);
                }
                return true;
        }
        /// M: Wifi Wps EM @{
        if (FeatureOption.MTK_WIFIWPSP2P_NFC_SUPPORT) {
            return mWifiSettingsExt.onOptionsItemSelected(item);
        }
        /// @}
        return super.onOptionsItemSelected(item);
    }

    /*
     *@ 原生长按wifi列表弹出的menu, 已被屏蔽
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        if (info instanceof AdapterContextMenuInfo) {
           AuroraPreference preference = (AuroraPreference) getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo) info).position);

            if (preference instanceof AccessPoint) {
                mSelectedAccessPoint = (AccessPoint) preference;
                menu.setHeaderTitle(mSelectedAccessPoint.ssid);
                if (mSelectedAccessPoint.getLevel(4) != -1) {
                    if (mSelectedAccessPoint.getState() == null) {
                        menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
                    }
                }

                if (ActivityManager.getCurrentUser() == UserHandle.USER_OWNER &&
                        (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID ||
                        (mSelectedAccessPoint.getNetworkInfo() != null &&
                        mSelectedAccessPoint.getNetworkInfo().getState() != State.DISCONNECTED))) {
                    // Allow forgetting a network if the current user is the owner and either the
                    // network is saved or ephemerally connected. (In the latter case, "forget"
                    // blacklists the network so it won't be used again, ephemerally).
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                }

                ///M: add mtk feature
                mWifiSettingsExt.onCreateContextMenu(menu, mSelectedAccessPoint.getState(),
                        mWifiSettingsExt.build(mSelectedAccessPoint.ssid, mSelectedAccessPoint.bssid,
                                mSelectedAccessPoint.networkId, mSelectedAccessPoint.wpsAvailable));

                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                    try {
                        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
                        if (nfcAdapter != null &&
                            nfcAdapter.isEnabled() &&
                            nfcAdapter.getModeFlag(NfcAdapter.MODE_READER) == NfcAdapter.FLAG_ON &&
                            mSelectedAccessPoint.security != AccessPoint.SECURITY_NONE) {
                            // Only allow writing of NFC tags for password-protected networks.
                            menu.add(Menu.NONE, MENU_ID_WRITE_NFC, 0, R.string.wifi_menu_write_to_nfc);
                        }
                    } catch (UnsupportedOperationException ex) {
                        Log.d(TAG, "this device doesn't support NFC");
                    }
                }
            }
        }
    }

    /*
     *@ 原生长按wifi列表弹出的menu item的点击事件, 已被屏蔽
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    connect(mSelectedAccessPoint.networkId);
                } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig());
                } else {
                    showDialog(mSelectedAccessPoint, true);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                forget();
                return true;
            }
            case MENU_ID_MODIFY: {
                showDialog(mSelectedAccessPoint, true);
                return true;
            }
            case MENU_ID_WRITE_NFC:
                showDialog(WRITE_NFC_DIALOG_ID);
                return true;

        }
        ///M: add mtk feature
        if (mWifiSettingsExt != null) {
            return mWifiSettingsExt.onContextItemSelected(item, mSelectedAccessPoint.networkId);
        }
        return super.onContextItemSelected(item);
    }
}
