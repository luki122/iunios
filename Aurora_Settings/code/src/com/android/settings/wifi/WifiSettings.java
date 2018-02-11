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
import aurora.widget.AuroraActionBar;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import android.app.Activity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import android.security.Credentials;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.GnSettingsUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.wifi.p2p.WifiP2pSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


//Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
import com.android.settings.Settings;


//Gionee <wangguojing> <2013-07-25> add for CR00837650 end
//import aurora.widget.AuroraMenu.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuAdapter;
import aurora.widget.AuroraMenuItem;

// Aurora <likai> <2013-10-02> add begin
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
//Aurora <likai> <2013-10-02> add end

/**
 * Two types of UI are provided here.
 *
 * The first is for "usual Settings", appearing as any other Setup fragment.
 *
 * The second is for Setup Wizard, with a simplified interface that hides the action bar
 * and menus.
 */
public class WifiSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener  {
	private static final boolean PRINT=true;
    private static final String TAG = "WifiSettings";
    private static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_P2P = Menu.FIRST + 2;
    private static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 3;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
    private static final int MENU_ID_SCAN = Menu.FIRST + 5;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
    private static final int MENU_ID_FORGET = Menu.FIRST + 7;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 8;

    private static final int WIFI_DIALOG_ID = 1;
    private static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int WIFI_SKIPPED_DIALOG_ID = 4;
    private static final int WIFI_AND_MOBILE_SKIPPED_DIALOG_ID = 5;

    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    // Instance state keys
    private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;

    private WifiManager mWifiManager;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
    private boolean mP2pSupported;

    // Aurora <likai> <2013-11-02> modify begin
    //private WifiEnabler mWifiEnabler;
    private AuroraWifiEnabler mWifiEnabler;
    // Aurora <likai> <2013-11-02> modify end

    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;

    private DetailedState mLastState;
    private WifiInfo mLastInfo;

    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private int mKeyStoreNetworkId = INVALID_NETWORK_ID;

    private WifiDialog mDialog;

    private TextView mEmptyView;

    /* Used in Wifi Setup context */

    // this boolean extra specifies whether to disable the Next button when not connected
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";

    // this boolean extra specifies whether to auto finish when connection is established
    private static final String EXTRA_AUTO_FINISH_ON_CONNECT = "wifi_auto_finish_on_connect";

    // this boolean extra shows a custom button that we can control
    protected static final String EXTRA_SHOW_CUSTOM_BUTTON = "wifi_show_custom_button";

    // show a text regarding data charges when wifi connection is required during setup wizard
    protected static final String EXTRA_SHOW_WIFI_REQUIRED_INFO = "wifi_show_wifi_required_info";

    // this boolean extra is set if we are being invoked by the Setup Wizard
    private static final String EXTRA_IS_FIRST_RUN = "firstRun";

    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;

    // should activity finish once we have a connection?
    private boolean mAutoFinishOnConnection;

    // Save the dialog details
    private boolean mDlgEdit;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;

    // the action bar uses a different set of controls for Setup Wizard
    private boolean mSetupWizardMode;

    /* End of "used in Wifi Setup context" */
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    private  static WifiSettings mWifiSettings ;
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 end

    // Aurora <likai> <2013-10-02> add begin
    private static final String WIFI_SWITCH_PREFERENCE_KEY = "wifi_switch";
    private static final String WIFI_ADVANCED_SETTINGS_KEY = "wifi_advanced_settings";
    private static final String WIFI_LIST_PREFERENCE_KEY = "wifi_list";

    // Aurora <likai> <2013-11-02> modify begin
    //private AuroraWifiSwitchPreference mWifiSwitch;
    private AuroraSwitchPreference mWifiSwitch;
    // Aurora <likai> <2013-11-02> modify end

    private AuroraPreferenceScreen mWifiAdvancedSettings;
    private AuroraPreferenceCategory mWifiPreferenceCategory;
    private AuroraActionBar mActionBar;    

    private static final int WIFI_REQUEST_CODE = 101;
    protected static final String KEY_RETURN_ACTION = "return_action";
    protected static final String ACTION_SUBMIT = "do_submit";
    protected static final String ACTION_FORGIVE = "do_forget";

    protected static final String KEY_CAN_EDIT = "can_edit";
    protected static final String KEY_ACCESS_POINT = "access_point";
    private WifiInfoActivity mWifiInfoActivity;
    // Aurora <likai> <2013-10-02> add end
    public static void PrintLog(String sTAG,String msg)
    {
    	if(PRINT)
    	{
    		Log.d(sTAG, msg);
    	}
    }

    public WifiSettings() {
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
        mWifiSettings = this;
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mFilter.addAction("android.net.wifi.WIFI_STATEBAR_CHANGED");
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };

        mScanner = new Scanner();

    }
    public SharedPreferences getPrefrerence()
    {
    	return statePreferences;
    }
    public SharedPreferences.Editor getEdit()
    {
    	return stateEditor;
    }
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    public static WifiSettings getWifiSettingsInstance() {
		return mWifiSettings ;
		
    	}
    @Override
    public void onDestroy() {
        super.onDestroy();
        Settings.mKey = null ;
        //获取到sharepreference 对象，
        SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
      	//获取到编辑对象  
      	SharedPreferences.Editor edit = iuniSP.edit();  
      	//添加新的值，该值是保存用户输入的新的签名 
      	edit.putString("wifiState", "unChecked");  
      	//提交.  
      	edit.commit();
    }
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
  //IUNI <penggangding><2014-06-17> added for  begin
    private SharedPreferences statePreferences=null;
    private SharedPreferences.Editor stateEditor=null;
    private static final String STATETABLE="stateXML";
    @Override
    public void onCreate(Bundle icicle) {
        // Set this flag early, as it's needed by getHelpResource(), which is called by super
        mSetupWizardMode = getActivity().getIntent().getBooleanExtra(EXTRA_IS_FIRST_RUN, false);
        statePreferences=getActivity().getSharedPreferences(STATETABLE, Activity.MODE_PRIVATE);
        stateEditor=statePreferences.edit();
  //IUNI <penggangding><2014-06-17> added for  end
/*
        //AURORA-START::change menu::waynelin::2013-9-16
        ArrayList<AuroraMenuItem> menuItems = new ArrayList<AuroraMenuItem>();
        if (mSetupWizardMode) {
            AuroraMenuItem item = new AuroraMenuItem();
            item.setId(MENU_ID_WPS_PBC);
            item.setTitle(R.string.wifi_menu_wps_pbc);
            item.setIcon(R.drawable.ic_wps);
            menuItems.add(item);
            
            item = new AuroraMenuItem();
            item.setId(MENU_ID_ADD_NETWORK);
            item.setTitle(R.string.wifi_add_network);
           // item.setIcon();
            menuItems.add(item);
        }else{
            AuroraMenuItem item = new AuroraMenuItem();
            item.setId(MENU_ID_WPS_PBC);
            item.setTitle(R.string.wifi_menu_wps_pbc);
            item.setIcon(R.drawable.ic_wps);
            menuItems.add(item);
            
            item = new AuroraMenuItem();
            item.setId(MENU_ID_ADD_NETWORK);
            item.setTitle(R.string.wifi_add_network);
            item.setIcon(R.drawable.ic_menu_add);
            menuItems.add(item);
      
            item = new AuroraMenuItem();
            item.setId(MENU_ID_SCAN);
            item.setTitle(R.string.wifi_menu_scan);
           // item.setIcon();
            menuItems.add(item);
            
            item = new AuroraMenuItem();
            item.setId(MENU_ID_WPS_PIN);
            item.setTitle(R.string.wifi_menu_wps_pin);
           // item.setIcon();
            menuItems.add(item);
            
	    item = new AuroraMenuItem();
            item.setId(MENU_ID_ADVANCED);
            item.setTitle(R.string.wifi_menu_advanced);
            //item.setIcon();
            menuItems.add(item);            
        }
        AuroraMenuAdapter auroraMenuAdapter = new AuroraMenuAdapter(getActivity(), menuItems);
        AuroraPreferenceActivity activity =  (AuroraPreferenceActivity)getActivity();
	activity.setAuroraMenuCallBack(auroraMenuCallBack);
        activity.setMenuAdapter(auroraMenuAdapter);
        activity.initMenuData();       
          
       //AURORA-END::change menu::waynelin::2013-9-16
      */


        super.onCreate(icicle);
    }
/*
     //AURORA-START::change menu::waynelin::2013-9-16

    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

	@Override
	public void auroraMenuItemClick(int itemId) {    
		switch (itemId) {
		    case MENU_ID_WPS_PBC:
			showDialog(WPS_PBC_DIALOG_ID);
			break;
		    case MENU_ID_P2P:
			if (getActivity() instanceof AuroraPreferenceActivity) {
			    ((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
			            WifiP2pSettings.class.getCanonicalName(),
			            null,
			            R.string.wifi_p2p_settings_title, null,
			            WifiSettings.mWifiSettings, 0);
			} else {
			    startFragment(WifiSettings.mWifiSettings, WifiP2pSettings.class.getCanonicalName(), -1, null);
			}
			break;
		    case MENU_ID_WPS_PIN:
			showDialog(WPS_PIN_DIALOG_ID);
			break;
		    case MENU_ID_SCAN:
			if (mWifiManager.isWifiEnabled()) {
			    mScanner.forceScan();
			}
			break;
		    case MENU_ID_ADD_NETWORK:
			if (mWifiManager.isWifiEnabled()) {
			    onAddNetworkPressed();
			}
			break;
		    case MENU_ID_ADVANCED:
			if (getActivity() instanceof AuroraPreferenceActivity) {
			    ((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
			            AdvancedWifiSettings.class.getCanonicalName(),
			            null,
			            R.string.wifi_advanced_titlebar, null,
			            WifiSettings.mWifiSettings, 0);
			} else {
			    startFragment(WifiSettings.mWifiSettings, AdvancedWifiSettings.class.getCanonicalName(), -1, null);
			}
			break;

		}
           }
    };
   //AURORA-END::change menu::waynelin::2013-9-16
*/
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mActionBar =((AuroraActivity)getActivity()).getAuroraActionBar();
    	
        if (mSetupWizardMode) {
            View view = inflater.inflate(R.layout.setup_preference, container, false);
            View other = view.findViewById(R.id.other_network);
            other.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWifiManager.isWifiEnabled()) {
                        onAddNetworkPressed();
                    }
                }
            });
            final ImageButton b = (ImageButton) view.findViewById(R.id.more);
            if (b != null) {
                b.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mWifiManager.isWifiEnabled()) {
                            PopupMenu pm = new PopupMenu(inflater.getContext(), b);
                            pm.inflate(R.menu.wifi_setup);
                            pm.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if (R.id.wifi_wps == item.getItemId()) {
                                        showDialog(WPS_PBC_DIALOG_ID);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            pm.show();
                        }
                    }
                });
            }

            Intent intent = getActivity().getIntent();
            if (intent.getBooleanExtra(EXTRA_SHOW_CUSTOM_BUTTON, false)) {
                view.findViewById(R.id.button_bar).setVisibility(View.VISIBLE);
                view.findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.skip_button).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.next_button).setVisibility(View.INVISIBLE);

                Button customButton = (Button) view.findViewById(R.id.custom_button);
                customButton.setVisibility(View.VISIBLE);
                customButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPhone() && !hasSimProblem()) {
                            showDialog(WIFI_SKIPPED_DIALOG_ID);
                        } else {
                            showDialog(WIFI_AND_MOBILE_SKIPPED_DIALOG_ID);
                        }
                    }
                });
            }

            if (intent.getBooleanExtra(EXTRA_SHOW_WIFI_REQUIRED_INFO, false)) {
                view.findViewById(R.id.wifi_required_info).setVisibility(View.VISIBLE);
            }

            return view;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mP2pSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // penggangding begin 08-07
        // set frequency Band {   0 : auto   
        //                        1 : 5GHz
        //                        2 : 2GHz   )
        if(mWifiManager.getFrequencyBand()!=0)
        {
        	mWifiManager.setFrequencyBand(0,true);
        }
        // penggangding end 08-07
        

        mConnectListener = new WifiManager.ActionListener() {
                                   public void onSuccess() {
                                   }
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
                                public void onSuccess() {
                                }
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
                                   public void onSuccess() {
                                   }
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                               R.string.wifi_failed_forget_message,
                                               Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
            mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
            mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
        }

        final AuroraActivity activity = (AuroraActivity)getActivity();
        final Intent intent = activity.getIntent();

        // first if we're supposed to finish once we have a connection
        mAutoFinishOnConnection = intent.getBooleanExtra(EXTRA_AUTO_FINISH_ON_CONNECT, false);

        if (mAutoFinishOnConnection) {
            // Hide the next button
            if (hasNextButton()) {
                getNextButton().setVisibility(View.GONE);
            }

            final ConnectivityManager connectivity = (ConnectivityManager)
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null
                    && connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
                return;
            }
        }

        // if we're supposed to enable/disable the Next button based on our current connection
        // state, start it off in the right state
        mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);

        if (mEnableNextOnConnection) {
            if (hasNextButton()) {
                final ConnectivityManager connectivity = (ConnectivityManager)
                        activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo info = connectivity.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI);
                    changeNextButtonState(info.isConnected());
                }
            }
        }

        // Aurora <likai> <2013-10-02> modify begin
        //addPreferencesFromResource(R.xml.wifi_settings);  
        addPreferencesFromResource(R.xml.aurora_wifi_settings);

        mWifiAdvancedSettings = (AuroraPreferenceScreen) findPreference(WIFI_ADVANCED_SETTINGS_KEY);
        mWifiPreferenceCategory = (AuroraPreferenceCategory) findPreference(WIFI_LIST_PREFERENCE_KEY);
        // Aurora <likai> <2013-11-02> modify begin
        //mWifiSwitch = (AuroraWifiSwitchPreference) findPreference(WIFI_SWITCH_PREFERENCE_KEY);
        mWifiSwitch = (AuroraSwitchPreference) findPreference(WIFI_SWITCH_PREFERENCE_KEY);
        mWifiSwitch.setChecked(mWifiManager.isWifiEnabled());
        // Aurora <likai> <2013-11-02> modify end
        
        //获取到sharepreference 对象，
        SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
      	//获取到编辑对象  
      	SharedPreferences.Editor edit = iuniSP.edit(); 
      	if(mWifiManager.isWifiEnabled()) {
      	//添加新的值，该值是保存用户输入的新的签名 
          	edit.putString("wifiState", "isChecked"); 
      	}
      	//提交.  
      	edit.commit();

        // Aurora <likai> <2013-10-02> modify end

        if (mSetupWizardMode) {
            getView().setSystemUiVisibility(
                    View.STATUS_BAR_DISABLE_BACK |
                    View.STATUS_BAR_DISABLE_HOME |
                    View.STATUS_BAR_DISABLE_RECENT |
                    View.STATUS_BAR_DISABLE_NOTIFICATION_ALERTS |
                    View.STATUS_BAR_DISABLE_CLOCK);
        }

        // On/off switch is hidden for Setup Wizard
        if (true) {
    	    // Aurora <likai> <2013-10-02> modify begin
		    /*
            AuroraSwitch actionBarSwitch = new AuroraSwitch(activity);

            if (activity instanceof AuroraPreferenceActivity) {
                AuroraPreferenceActivity preferenceActivity = (AuroraPreferenceActivity) activity;
                if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                    final int padding = activity.getResources().getDimensionPixelSize(
                            R.dimen.action_bar_switch_padding);
                    actionBarSwitch.setPadding(0, 0, padding, 0);
		
                    activity.getAuroraActionBar().setDisplayOptions(AuroraActionBar.DISPLAY_SHOW_CUSTOM,
                            AuroraActionBar.DISPLAY_SHOW_CUSTOM);
                    activity.getAuroraActionBar().setCustomView(actionBarSwitch, new AuroraActionBar.LayoutParams(
                            AuroraActionBar.LayoutParams.WRAP_CONTENT,
                            AuroraActionBar.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_VERTICAL | Gravity.END));
                }
            }

            mWifiEnabler = new WifiEnabler(activity, actionBarSwitch);
            */
        	//activity.getAuroraActionBar().addItem(AuroraActionBarItem.Type.Add, MENU_ID_ADD_NETWORK);
        	//activity.getAuroraActionBar().setOnAuroraActionBarListener(auroraActionBarItemClickListener);
            mActionBar.addItem(R.drawable.aurora_wifi_add, MENU_ID_ADD_NETWORK, null);
            mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		    // Aurora <likai> <2013-10-02> modify end

            // Aurora <likai> <2013-11-02> add begin
            mWifiEnabler = new AuroraWifiEnabler(activity, mWifiSwitch);
            // Aurora <likai> <2013-11-02> add end
        }

        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);

        if (!mSetupWizardMode) {
            registerForContextMenu(getListView());
        }
        setHasOptionsMenu(true);
    }

    // Aurora <likai> <2013-10-02> add begin
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case MENU_ID_ADD_NETWORK:
				if (mWifiManager.isWifiEnabled()) {
                    onAddNetworkPressed();
                }
				break;
			default:
				break;
			}
		}
	};
	// Aurora <likai> <2013-10-02> add end

    @Override
    public void onResume() {
        super.onResume();
        mWifiSwitch.setChecked(mWifiManager.isWifiEnabled());

        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }

        getActivity().registerReceiver(mReceiver, mFilter);
        if (mKeyStoreNetworkId != INVALID_NETWORK_ID &&
                KeyStore.getInstance().state() == KeyStore.State.UNLOCKED) {
            mWifiManager.connect(mKeyStoreNetworkId, mConnectListener);
        }
        mKeyStoreNetworkId = INVALID_NETWORK_ID;

        updateAccessPoints();
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
        Settings.mKey = TAG ;
        //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    }

    @Override
    public void onPause() {
        super.onPause(); 

        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
        if (mSetupWizardMode) {
            // FIXME: add setIcon() when graphics are available
            menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
                    .setIcon(R.drawable.ic_wps)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
                    .setIcon(R.drawable.ic_wps)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
                    .setIcon(R.drawable.ic_menu_add)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.wifi_menu_scan)
                    //.setIcon(R.drawable.ic_menu_scan_network)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(Menu.NONE, MENU_ID_WPS_PIN, 0, R.string.wifi_menu_wps_pin)
                    .setEnabled(wifiIsEnabled)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            if (mP2pSupported) {
                //Gionee <wangguojing> <2013-08-03> modify for CR00845949 begin
                /*menu.add(Menu.NONE, MENU_ID_P2P, 0, R.string.wifi_menu_p2p)
                        .setEnabled(wifiIsEnabled)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);*/
                //Gionee <wangguojing> <2013-08-03> modify for CR00845949 end
            }
            menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
                    //.setIcon(android.R.drawable.ic_menu_manage)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onCreateOptionsMenu(menu, inflater);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_WPS_PBC:
                showDialog(WPS_PBC_DIALOG_ID);
                return true;
            case MENU_ID_P2P:
                if (getActivity() instanceof AuroraPreferenceActivity) {
                    ((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
                            WifiP2pSettings.class.getCanonicalName(),
                            null,
                            R.string.wifi_p2p_settings_title, null,
                            this, 0);
                } else {
                    startFragment(this, WifiP2pSettings.class.getCanonicalName(), -1, null);
                }
                return true;
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
            case MENU_ID_ADVANCED:
                if (getActivity() instanceof AuroraPreferenceActivity) {
                    ((AuroraPreferenceActivity) getActivity()).startPreferencePanel(
                            AdvancedWifiSettings.class.getCanonicalName(),
                            null,
                            R.string.wifi_advanced_titlebar, null,
                            this, 0);
                } else {
                    startFragment(this, AdvancedWifiSettings.class.getCanonicalName(), -1, null);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        if (info instanceof AdapterContextMenuInfo) {
            AuroraPreference preference = (AuroraPreference) getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo) info).position);

            if (preference instanceof AccessPoint) {
                mSelectedAccessPoint = (AccessPoint) preference;
                menu.setHeaderTitle(mSelectedAccessPoint.ssid);
                if (mSelectedAccessPoint.getLevel() != -1
                        && mSelectedAccessPoint.getState() == null) {
                    menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
                }
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                    menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                    if (!requireKeyStore(mSelectedAccessPoint.getConfig())) {
                        mWifiManager.connect(mSelectedAccessPoint.networkId,
                                mConnectListener);
                    }
                } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    mWifiManager.connect(mSelectedAccessPoint.getConfig(),
                            mConnectListener);
                } else {
                    showDialog(mSelectedAccessPoint, true);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);
                return true;
            }
            case MENU_ID_MODIFY: {
                showDialog(mSelectedAccessPoint, true);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen screen, AuroraPreference preference) {
        if (preference instanceof AccessPoint) {
            mSelectedAccessPoint = (AccessPoint) preference;
            /** Bypass dialog for unsecured, unsaved networks */
            if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE &&
                    mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
                mSelectedAccessPoint.generateOpenNetworkConfig();
                mWifiManager.connect(mSelectedAccessPoint.getConfig(), mConnectListener);
            } else {
                showDialog(mSelectedAccessPoint, false);
            }
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }
    // Aurora <likai> <2013-10-02> add begin
    public void doPreferenceTreeClick(AccessPoint ap, boolean showDetail){
        mSelectedAccessPoint = ap;
        if (showDetail) {
            if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
            	showDialog(mSelectedAccessPoint, true);
            } else {
            	showDialog(mSelectedAccessPoint, false);
            }
            return;
        }

        if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
            if (!requireKeyStore(mSelectedAccessPoint.getConfig())) {
                mWifiManager.connect(mSelectedAccessPoint.networkId,
                        mConnectListener);
            }
        } else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
            /** Bypass dialog for unsecured networks */
            mSelectedAccessPoint.generateOpenNetworkConfig();
            mWifiManager.connect(mSelectedAccessPoint.getConfig(),
                    mConnectListener);
        } else {
            showDialog(mSelectedAccessPoint, false);
        }
    }

    public void startWifiInfoActivity() {
        AccessPoint ap = mDlgAccessPoint;
        if (ap == null) {
            if (mAccessPointSavedState != null) {
                ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                mDlgAccessPoint = ap;
            }
        }

        mSelectedAccessPoint = ap;

        Intent intent = new Intent();
        intent.setClass(getActivity(), WifiInfoActivity.class);

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != WIFI_REQUEST_CODE || requestCode != Activity.RESULT_OK) return;

        if (data != null) {
            Bundle bundle = data.getExtras();
            mSelectedAccessPoint = new AccessPoint(getActivity(), bundle);
        }

        /*
        if (ACTION_SUBMIT.equals(bundle.getString(KEY_RETURN_ACTION))) {
            submit(mWifiInfoActivity.getController());
        } else {
    		forget();
        }
        */
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
    }
    // Aurora <likai> <2013-10-02> add end

    private void showDialog(AccessPoint accessPoint, boolean edit) {
        if (mDialog != null) {
            removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
        }

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDlgEdit = edit;

        // Aurora <likai> <2013-10-03> modify begin
        //showDialog(WIFI_DIALOG_ID);
        startWifiInfoActivity();
        // Aurora <likai> <2013-10-03> modify end
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                AccessPoint ap = mDlgAccessPoint; // For manual launch
                if (ap == null) { // For re-launch from saved state
                    if (mAccessPointSavedState != null) {
                        ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                        // For repeated orientation changes
                        mDlgAccessPoint = ap;
                    }
                }
                // If it's still null, fine, it's for Add Network
                mSelectedAccessPoint = ap;
                mDialog = new WifiDialog(getActivity(), this, ap, mDlgEdit);
                //Gionee <xuwen><2013-07-09> added for CR00834689 begin
                mDialog.setDialogState(true);
                setOnDismissListener(mDismissListener);
                //Gionee <xuwen><2013-07-09> added for CR00834689 end
                return mDialog;
            case WPS_PBC_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.PBC);
            case WPS_PIN_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.DISPLAY);
            case WIFI_SKIPPED_DIALOG_ID:
                return new AuroraAlertDialog.Builder(getActivity())
                            .setMessage(R.string.wifi_skipped_message)
                            .setCancelable(false)
                            .setNegativeButton(R.string.wifi_skip_anyway,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().setResult(Activity.RESULT_FIRST_USER);
                                    getActivity().finish();
                                }
                            })
                            .setPositiveButton(R.string.wifi_dont_skip,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .create();
            case WIFI_AND_MOBILE_SKIPPED_DIALOG_ID:
                return new AuroraAlertDialog.Builder(getActivity())
                            .setMessage(R.string.wifi_and_mobile_skipped_message)
                            .setCancelable(false)
                            .setNegativeButton(R.string.wifi_skip_anyway,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().setResult(Activity.RESULT_FIRST_USER);
                                    getActivity().finish();
                                }
                            })
                            .setPositiveButton(R.string.wifi_dont_skip,
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .create();

        }
        return super.onCreateDialog(dialogId);
    }

    private boolean isPhone() {
        final TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(
                Context.TELEPHONY_SERVICE);
        return telephonyManager != null
                && telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
    * Return true if there's any SIM related impediment to connectivity.
    * Treats Unknown as OK. (Only returns true if we're sure of a SIM problem.)
    */
   protected boolean hasSimProblem() {
       final TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(
               Context.TELEPHONY_SERVICE);
       return telephonyManager != null
               && telephonyManager.getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_GSM
               && telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY
               && telephonyManager.getSimState() != TelephonyManager.SIM_STATE_UNKNOWN;
   }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiConfigController.requireKeyStore(config) &&
                KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
            mKeyStoreNetworkId = config.networkId;
            Credentials.getInstance().unlock(getActivity());
            return true;
        }
        return false;
    }
    
    private static final int MSG_HANDLE_WHAT=1;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HANDLE_WHAT:
                updateAccessPointList(msg);
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};
	
	private void updateAccessPointList(Message msg)
	{
		final Collection<AccessPoint> accessPoints=(Collection<AccessPoint>) msg.obj;
        // Aurora <likai> <2013-10-02> modify begin
        //getPreferenceScreen().removeAll();
		
        mWifiPreferenceCategory.removeAll();
        // Aurora <likai> <2013-10-02> modify end
//        if(accessPoints.size() == 0) {
//            addMessagePreference(R.string.wifi_empty_list_wifi_on);
//        }
        for (AccessPoint accessPoint : accessPoints) {
            // Aurora <likai> <2013-10-02> modify begin
            //getPreferenceScreen().addPreference(accessPoint);
        	accessPoint.setFragment(WifiSettings.this);
            mWifiPreferenceCategory.addPreference(accessPoint);
            // Aurora <likai> <2013-10-02> modify end
        }
	}

    /**
     * Shows the latest access points available with supplimental information like
     * the strength of network and the security for it.
     */
    private void updateAccessPoints() {
        // Safeguard from some delayed event handling
        if (getActivity() == null) return;

        // Aurora <likai> add begin
        ImageButton addItemView = (ImageButton) mActionBar.getItem(0).getItemView();
        // Aurora <likai> add end

        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // Aurora <likai> add begin
                addItemView.setImageResource(R.drawable.aurora_wifi_add);
                mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
                mWifiAdvancedSettings.setEnabled(true);
                mWifiAdvancedSettings.setSelectable(true);
                wifiHandler.postDelayed(wifiTask,500);
                // Aurora <likai> add end

                // AccessPoints are automatically sorted with TreeSet.
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						final Collection<AccessPoint> accessPoints = constructAccessPoints();
						Message message = mHandler.obtainMessage();
						message.obj = accessPoints;
						message.what=MSG_HANDLE_WHAT;
						mHandler.sendMessage(message);
						
					}
				}).start();

/**
		if(accessPoints != null) {
			mWifiSwitch.setChecked(true);
		}else {
			mWifiSwitch.setChecked(false);
		}
*/
                break;

            case WifiManager.WIFI_STATE_ENABLING:
            	// Aurora <likai> <2013-10-02> modify begin
                //getPreferenceScreen().removeAll();
                mWifiPreferenceCategory.removeAll();
                // Aurora <likai> <2013-10-02> modify end
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(R.string.wifi_stopping);
		//mWifiSwitch.setChecked(false);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                wifiHandler.postDelayed(wifiTask,500);
		//mWifiSwitch.setChecked(false);
                break;
        }
    }

    private void addMessagePreference(int messageId) {
        if (mEmptyView != null) mEmptyView.setText(messageId);

        // Aurora <likai> add begin
        ImageButton addItemView = (ImageButton) mActionBar.getItem(0).getItemView();
        addItemView.setImageResource(R.drawable.aurora_wifi_add_unable);
        mActionBar.setOnAuroraActionBarListener(null);
        mWifiAdvancedSettings.setEnabled(false);
        mWifiAdvancedSettings.setSelectable(false);
        // Aurora <likai> add end

        // Aurora <likai> <2013-10-02> modify begin
        //getPreferenceScreen().removeAll();
        mWifiPreferenceCategory.removeAll();
        // Aurora <likai> <2013-10-02> modify end
    }

    /** Returns sorted list of access points */
    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        /** Lookup table to more quickly update AccessPoints by only considering objects with the
         * correct SSID.  Maps SSID -> List of AccessPoints with the given SSID.  */
        Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.ssid, accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
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
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.ssid, accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        Collections.sort(accessPoints);
        return accessPoints;
    }

    /** A restricted multimap for use in constructAccessPoints */
    private class Multimap<K,V> {
        private HashMap<K,List<V>> store = new HashMap<K,List<V>>();
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

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action) ||
                WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION.equals(action) ||
                WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
                updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            //Ignore supplicant state changes when network is connected
            //TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            //introduce a broadcast that combines the supplicant and network
            //network state change events so the apps dont have to worry about
            //ignoring supplicant state change when network is connected
            //to get more fine grained information.
            SupplicantState state = (SupplicantState) intent.getParcelableExtra(
                    WifiManager.EXTRA_NEW_STATE);
            if (!mConnected.get() && SupplicantState.isHandshakeState(state)) {
                updateConnectionState(WifiInfo.getDetailedStateOf(state));
            }else
            {
            	updateConnectionState(null);
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            changeNextButtonState(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
            if (mAutoFinishOnConnection && info.isConnected()) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                }
                return;
            }
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        } else if("android.net.wifi.WIFI_STATEBAR_CHANGED".equals(action)) {
        	//home
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
        	airplaneHandler.post(airplaneTask);
        	isAirplaneChange = true;
      }
    }
    private boolean isAirplaneChange;
    private final Handler airplaneHandler = new Handler();  
    private final Runnable airplaneTask = new Runnable() {
        @Override  
        public void run() {  
            // TODO Auto-generated method stub  
        	boolean mIsAirplaneMode = (android.provider.Settings.Global.getInt(getActivity().getContentResolver(),android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0);
        	final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        	SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
			String wifiState = iuniSP.getString("wifiState", "unChecked");
			PrintLog(TAG,"airplaneTask runnable" );
			if(isAirplaneChange) {
				if(wifiState.equals("isChecked") && !mIsAirplaneMode) {
        			mWifiSwitch.setChecked(true);
        		} else {
        			mWifiSwitch.setChecked(false);
        		}
			}
        	isAirplaneChange = false;
        }  
    }; 
    
    private final Handler wifiHandler = new Handler();  
    private final Runnable wifiTask = new Runnable() {
        @Override  
        public void run() {  
            // TODO Auto-generated method stub  
//        	final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        		if(/*configs != null ||*/ (WifiManager.WIFI_STATE_ENABLED == mWifiManager.getWifiState())) {
                	mWifiSwitch.setChecked(true);
                } else {
                	mWifiSwitch.setChecked(false);
                }
        }  
    };
    
    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        // Aurora <likai> <2013-10-02> modify begin
        // for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
        for (int i = mWifiPreferenceCategory.getPreferenceCount() - 1; i >= 0; --i) {
        // Aurora <likai> <2013-10-02> modify end

            // Maybe there's a WifiConfigPreference

        	// Aurora <likai> <2013-10-02> modify begin
            // AuroraPreference preference = getPreferenceScreen().getPreference(i);
            AuroraPreference preference = mWifiPreferenceCategory.getPreference(i);
            // Aurora <likai> <2013-10-02> modify end

            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastState);
            }
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
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

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
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_fail_to_scan,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    /**
     * Renames/replaces "Next" button when appropriate. "Next" button usually exists in
     * Wifi setup screens, not in usual wifi settings screen.
     *
     * @param connected true when the device is connected to a wifi network.
     */
    private void changeNextButtonState(boolean connected) {
        if (mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(connected);
        }
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == WifiDialog.BUTTON_FORGET && mSelectedAccessPoint != null) {
            forget();
        } else if (button == WifiDialog.BUTTON_SUBMIT) {
            submit(mDialog.getController());
        }
    }

    /* package */ void submit(WifiConfigController configController) {

        final WifiConfiguration config = configController.getConfig();

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && !requireKeyStore(mSelectedAccessPoint.getConfig())
                    && mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
                mWifiManager.connect(mSelectedAccessPoint.networkId,
                        mConnectListener);
            }
        } else if (config.networkId != INVALID_NETWORK_ID) {
            if (mSelectedAccessPoint != null) {
                mWifiManager.save(config, mSaveListener);
            }
        } else {
            if (configController.isEdit() || requireKeyStore(config)) {
                mWifiManager.save(config, mSaveListener);
            } else {
                mWifiManager.connect(config, mConnectListener);
            }
        }

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();
    }

    /* package */ void forget() {
        if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e(TAG, "Failed to forget invalid network " + mSelectedAccessPoint.getConfig());
            return;
        }

        mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);

        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
        updateAccessPoints();

        // We need to rename/replace "Next" button in wifi setup context.
        changeNextButtonState(false);
    }

    /**
     * Refreshes acccess points and ask Wifi module to scan networks again.
     */
    /* package */ void refreshAccessPoints() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }

        // Aurora <likai> <2013-10-02> modify begin
        //getPreferenceScreen().removeAll();
        mWifiPreferenceCategory.removeAll();
        // Aurora <likai> <2013-10-02> modify end
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
        	// Aurora <likai> <2013-10-02> modify begin
            // return getPreferenceScreen().getPreferenceCount();
            return mWifiPreferenceCategory.getPreferenceCount();
            // Aurora <likai> <2013-10-02> modify end
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
        if (mSetupWizardMode) {
            return 0;
        }
        return R.string.help_url_wifi;
    }

    /**
     * Used as the outer frame of all setup wizard pages that need to adjust their margins based
     * on the total size of the available display. (e.g. side margins set to 10% of total width.)
     */
    public static class ProportionalOuterFrame extends RelativeLayout {
        public ProportionalOuterFrame(Context context) {
            super(context);
        }
        public ProportionalOuterFrame(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public ProportionalOuterFrame(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        /**
         * Set our margins and title area height proportionally to the available display size
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
            final Resources resources = getContext().getResources();
            float titleHeight = resources.getFraction(R.dimen.setup_title_height, 1, 1);
            float sideMargin = resources.getFraction(R.dimen.setup_border_width, 1, 1);
            int bottom = resources.getDimensionPixelSize(R.dimen.setup_margin_bottom);
            setPadding(
                    (int) (parentWidth * sideMargin),
                    0,
                    (int) (parentWidth * sideMargin),
                    bottom);
            View title = findViewById(R.id.title_area);
            if (title != null) {
                title.setMinimumHeight((int) (parentHeight * titleHeight));
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    //Gionee <xuwen><2013-07-09> added for CR00834689 begin
    // Called when the dialog is dismissed
    private final DialogInterface.OnDismissListener mDismissListener =
            new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (null != mDialog) {
                        mDialog.setDialogState(false);
                    }
                }
            };
    //Gionee <xuwen><2013-07-09> added for CR00834689 end

}
