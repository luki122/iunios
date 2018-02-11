/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import com.android.internal.util.ArrayUtils;
import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.applications.ManageApplications;
import com.android.settings.bluetooth.BluetoothEnabler;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.vpn2.VpnSettings;
import com.android.settings.wifi.WifiEnabler;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.android.internal.widget.ActionBarView;
import com.android.internal.widget.ActionBarContainer;
import com.gionee.settings.utils.CountUtil;
import com.gionee.settings.utils.GnUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.net.NetworkInfo.State;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.telephony.TelephonyManager;
import android.os.SystemProperties;
import android.preference.PreferenceActivity.Header;
// Aurora <likai> <2013-10-24> add begin
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
// Aurora <likai> <2013-10-24> add end


//Gionee <wangguojing> <2013-07-20> add  begin
import com.gionee.featureoption.FeatureOption;
import com.mediatek.wireless.UsbSharingInfo;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.view.MenuItem;

import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.wifi.WifiSettings;
import com.android.settings.wifi.p2p.WifiP2pSettings;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.applications.ManageApplications;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.DreamSettings;
import com.android.settings.vpn2.VpnSettings;
import com.android.settings.UserDictionarySettings;
import com.android.settings.inputmethod.UserDictionaryAddWordFragment;
//import com.android.settings.wfd.WifiDisplaySettings;




import gionee.telephony.GnTelephonyManager;

//Gionee <wangguojing> <2013-07-20> add  end

import gionee.telephony.GnTelephonyManager;
import android.net.Uri;
/**
 * Top-level settings activity to handle single pane and double pane UI layout.
 */
public class Settings extends AuroraPreferenceActivity
        implements ButtonBarHandler, OnAccountsUpdateListener {

    private static final String LOG_TAG = "Settings";

    private static final String META_DATA_KEY_HEADER_ID =
        "com.android.settings.TOP_LEVEL_HEADER_ID";
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
        "com.android.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE =
        "com.android.settings.PARENT_FRAGMENT_TITLE";
    private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
        "com.android.settings.PARENT_FRAGMENT_CLASS";

    private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";

    private static final String SAVE_KEY_CURRENT_HEADER = "com.android.settings.CURRENT_HEADER";
    private static final String SAVE_KEY_PARENT_HEADER = "com.android.settings.PARENT_HEADER";

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;
    //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
    private ActionBarView mActionBar;
    //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end

    // Show only these settings for restricted users
    private int[] SETTINGS_FOR_RESTRICTED = {
            R.id.wireless_section,
            R.id.wifi_settings,
            R.id.bluetooth_settings,
            R.id.tether_settings,
            R.id.data_usage_settings,
            R.id.wireless_settings,
            R.id.device_section,
            R.id.sound_settings,
            R.id.display_settings,
            R.id.storage_settings,
            R.id.application_settings,
            R.id.battery_settings,
            R.id.personal_section,
            R.id.location_settings,
            R.id.security_settings,
            R.id.language_settings,
            R.id.user_settings,
            R.id.account_settings,
            R.id.account_add,
            R.id.system_section,
            R.id.date_time_settings,
            R.id.about_settings,
            R.id.feedback,
            R.id.accessibility_settings,
            R.id.advanced_settings    // Aurora <likai> add
            
    }; 

    private SharedPreferences mDevelopmentPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;

    // TODO: Update Call Settings based on airplane mode state.

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();

    private AuthenticatorHelper mAuthenticatorHelper;
    private Header mLastHeader;
    private boolean mListeningToAccountUpdates;
    ///M: add for gray the imageview
    private static final int IMAGE_GRAY = 75;//30% of 0xff in transparent
    private static final int ORIGINAL_IMAGE = 255;
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    public static String mKey ;
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 end

    // Aurora <likai> <2013-10-24> add begin
    public static int mListSeparatorHeight;
    private BroadcastReceiver mReceiver;
	private BroadcastReceiver mHomeReceiver;
    private IntentFilter mFilter;
    private WifiManager mWifiManager;
    // Aurora <likai> <2013-10-24> add end

	private static boolean mIsAirplaneMode = false;
	private static boolean mHasUseSimCard = false;
	//add by jiyouguang 
	private CountUtil mCountUtil;
	public static boolean isHiden = false;  //if true,Sim is available
	//end
	
	
	private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION_UPDATE_FONT_BOLD)){				
				
			       finish();
			}
			
		}
	};
	
	private static final boolean MULTI_SIM_ENABLE = GnTelephonyManager.isMultiSimEnabled();

	public static final String ACTION_UPDATE_FONT_BOLD = "com.android.settings.ACTION_UPDATE_FONT_BOLD";
	// qy add 2014 06 20 begin
	private void startLightSensorService( boolean state){
    	Intent it = new Intent();
    	it.putExtra("AUTO_ADJ_LIGHT", state);
		it.setClass(Settings.this,AuroraLightSensorService.class);
		startService(it);
    }

	// Add begin by aurora.jiangmx
    private final String[] PROJECTION = {
    		"hasLogin"
    };
	// Add end
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// start service
    	SharedPreferences sp = getSharedPreferences("iuni", Context.MODE_PRIVATE);
    	boolean autoLight = sp.getBoolean("aurora_automatic", true);
    	startLightSensorService( autoLight);
    	// qy add 2014 06 20 end
    	
    	mCountUtil = CountUtil.getInstance(getApplicationContext());
    	
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        if (getIntent().getBooleanExtra(EXTRA_CLEAR_UI_OPTIONS, false)) {
            getWindow().setUiOptions(0);
        }

        // Aurora <likai> <2013-10-24> add begin
        mListSeparatorHeight = (int) getResources().getDimension(R.dimen.list_separator_height);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mFilter = new IntentFilter();
		mFilter.addAction("android.intent.action.SERVICE_STATE");
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mHomeReceiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
				WifiSettings.PrintLog(TAG, "Action : "+ intent.getAction());
            	if((!MULTI_SIM_ENABLE && isCanUseSim()) || (MULTI_SIM_ENABLE && (isCanUseSim(0)||isCanUseSim(1) )) ) {
					mHasUseSimCard = true;
				} else {
					mHasUseSimCard = false;
				}
				mIsAirplaneMode = (android.provider.Settings.Global.getInt(context.getContentResolver(),android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0);
                handleEvent(intent);
            }
        };
        // Aurora <likai> <2013-10-24> add end
        // qy 2014 04 29 begin
        registerReceiver(mFinishReceiver, new IntentFilter(ACTION_UPDATE_FONT_BOLD));
        // qy 2014 04 29 end

        mAuthenticatorHelper = new AuthenticatorHelper();
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, null);

        mDevelopmentPreferences = getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE);

        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader(mTopLevelHeaderId);
            // Force the title so that it doesn't get overridden by a direct launch of
            // a specific settings screen.
            setTitle(R.string.settings_label);
        }

        // Retrieve any saved state
        if (savedInstanceState != null) {
            mCurrentHeader = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_HEADER);
            mParentHeader = savedInstanceState.getParcelable(SAVE_KEY_PARENT_HEADER);
        }

        // If the current header was saved, switch to it
        if (savedInstanceState != null && mCurrentHeader != null) {
            //switchToHeaderLocal(mCurrentHeader);
            showBreadCrumbs(mCurrentHeader.title, null);
        }

        if (mParentHeader != null) {
            setParentTitle(mParentHeader.title, null, new OnClickListener() {
                public void onClick(View v) {
                    switchToParent(mParentHeader.fragment);
                }
            });
        }

        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
        // Override up navigation for multi-pane, since we handle it in the fragment breadcrumbs
        /*
        if (onIsMultiPane()) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
        }
        */
        if (onIsMultiPane() && getAuroraActionBar() != null) {
            getAuroraActionBar().setDisplayHomeAsUpEnabled(false);
            getAuroraActionBar().setHomeButtonEnabled(false);
        }
        if (getAuroraActionBar() != null) {
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        /*
            getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14

        //AURORA-START::add for settings actionbar::waynelin::2013-9-18
            //Gionee <wangguojing> <2013-07-22> add for CR00837650 begin
            //getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
            //Gionee <wangguojing> <2013-07-23> add for CR00837650 end
            if(getClass().getName().equals("com.android.settings.Settings")){
		getAuroraActionBar().setDisplayHomeAsUpEnabled(false);
                getAuroraActionBar().setHomeButtonEnabled(false);
                getAuroraActionBar().setTitle(R.string.settings_label);
            }else{
                getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
            }
  	 //AURORA-END::add for settings actionbar::waynelin::2013-9-18
        }

        /*View view = null;
        ActionBarView actionBar = null;
        
        view = findViewById(com.android.internal.R.id.action_bar);
        if (view != null) {
            try {
                if (view instanceof ActionBarContainer) {
                    actionBar = (ActionBarView)view.findViewById(com.android.internal.R.id.action_bar);
                } else {
                    actionBar = (ActionBarView)view;
                }
                if (actionBar != null && !actionBar.isSplitActionBar()) {
                    actionBar.setSplitActionBar(true);
                }
            } catch (ClassCastException excetion) {
                excetion.printStackTrace();
            }
        }*/
        //Gionee:zhang_xin 2012-12-26 add for CR00746738 start
        getListView().setSelector(android.R.color.transparent);
        getListView().setDivider(null);
        //Gionee:zhang_xin 2012-12-26 add for CR00746738 end

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
            ///M: add for sim management
            ((HeaderAdapter) listAdapter).isSimManagementAvailable(this);
        }

        invalidateHeaders();
        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current fragment, if it is the same as originally launched
        if (mCurrentHeader != null) {
            outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
        }
        if (mParentHeader != null) {
            outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isClickHeader = false;
        // Aurora <likai> <2013-10-24> add begin
        registerReceiver(mReceiver, mFilter);
        // Aurora <likai> <2013-10-24> add end
		registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mDevelopmentPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                invalidateHeaders();
            }
        };
        mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
            /*
            ///M: add for sim management
            ((HeaderAdapter) listAdapter).isSimManagementAvailable(this);

            // Aurora <likai> <2013-10-24> add begin
            ((HeaderAdapter) listAdapter).updateWifiStatus(getWifiStatus());
            // Aurora <likai> <2013-10-24> add end
			((HeaderAdapter) listAdapter).updateBluetoothStatus(getBluetoothStatus());
			((HeaderAdapter) listAdapter).updateMobileNetworkStatus(getMobileNetworkStatus());
			
			// Add begin by aurora.jiangmx
			((HeaderAdapter) listAdapter).updatePersonSpaceLoginStatus(getPersonSpaceLoginStatus());
			// Add end
        */}
        invalidateHeaders();
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // Aurora <likai> <2013-10-24> add begin
        unregisterReceiver(mReceiver);
        // Aurora <likai> <2013-10-24> add end

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }

        mDevelopmentPreferences.unregisterOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);
        mDevelopmentPreferencesListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		unregisterReceiver(mHomeReceiver);
        if (mListeningToAccountUpdates) {
            AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        }
        //Gionee:zhang_xin 2013-02-01 add for CR00767874 start
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }
        //Gionee:zhang_xin 2013-02-01 add for CR00767874 end
        
        unregisterReceiver(mFinishReceiver);
    }

	class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";// home key
		static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {
						// home key处理点
            			((Settings) context).finish();
					} else if (reason.equals(SYSTEM_RECENT_APPS)) {
						// long home key处理点
					}
				}
			}
		}
	}

    // Aurora <likai> <2013-10-24> add begin
    private void handleEvent(Intent intent) {
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
			((HeaderAdapter) listAdapter).isSimManagementAvailable(this);
            ((HeaderAdapter) listAdapter).updateWifiStatus(getWifiStatus());
			((HeaderAdapter) listAdapter).updateBluetoothStatus(getBluetoothStatus());
			((HeaderAdapter) listAdapter).updateMobileNetworkStatus(getMobileNetworkStatus());
			// Add begin by aurora.jiangmx
			((HeaderAdapter) listAdapter).updatePersonSpaceLoginStatus(getPersonSpaceLoginStatus());
		    // Add end
        }
        invalidateHeaders();
    }
    private static final String TAG="Settings";
    private static List<ScanResult> scanResultsCache=new ArrayList<ScanResult>();
    private String getWifiStatus() {
        String wifiStatus = null;
        if (isWIFIConnection(this)) {
           //   penggangding begin 08-07
        	String resultssid="<unknown ssid>";
        	final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            final List<ScanResult> results = mWifiManager.getScanResults();
            Log.d(TAG, "  wifiInfo="+(wifiInfo==null)+"  results="+(results.size()==0));
			if (results.size() != 0) 
			{
				scanResultsCache = results;
			}else if(wifiInfo != null)
			{
            	resultssid=wifiInfo.getSSID().replace("\"", "").toString();
    			if(!"<unknown ssid>".equals(resultssid)) 
    			{
    				wifiStatus=resultssid;
                }
			}
			if (scanResultsCache != null && wifiInfo != null)
			{
				String infobssid = wifiInfo.getBSSID();
				for (ScanResult result : scanResultsCache) 
				{
					if (infobssid != null)
					{
						if (result.BSSID.equals(infobssid)) 
						{
							resultssid = result.SSID;
							break;
						}
					} else 
					{
						resultssid = wifiInfo.getSSID().replace("\"", "").toString();
						break;
					}
				}
				if (!"<unknown ssid>".equals(resultssid)) 
				{
					wifiStatus = resultssid;
				}
			}
        //    penggangding end 08-07
        }else if(!isWIFIConnection(this) && mWifiManager.isWifiEnabled()) {
			wifiStatus = "";
		} else {
			wifiStatus = getString(R.string.aurora_no_connect_ssid);
		}
        return wifiStatus;
    }
    
    public static boolean isWIFIConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            int netWorkType = activeNetworkInfo.getType();
            if ((ConnectivityManager.TYPE_WIFI == netWorkType || ConnectivityManager.TYPE_WIMAX == netWorkType)) {
                return true;
            }
        }

        return false;
    }
	
    // Aurora <likai> <2013-10-24> add end

    // Add begin by aurora.jiangmx
    private String getPersonSpaceLoginStatus(){
    	String lPerSpaceStatus = null;
	    Uri lUri = Uri.parse("content://com.aurora.account.accountprovider/account_info");
        Cursor lCursor = getContentResolver().query(lUri, PROJECTION, null, null, null);
        
        int lIsLogin = 0;
        
        if( lCursor != null ){
        	if(lCursor.moveToFirst()){
        		lIsLogin = lCursor.getInt(lCursor.getColumnIndex("hasLogin"));
        		 
        	}
        }
        
        if(lIsLogin == 0 ){
        	lPerSpaceStatus = getString(R.string.login_status_no);
        }else{
        	lPerSpaceStatus = getString(R.string.login_status_yes);
        }
        
        return lPerSpaceStatus;
    }
	// Add end

	//mobilenetwork connect
	private String getMobileNetworkStatus() {
        String mobileNetworkStatus = null;
        if (isMobileNetworkConnection(this)) {
			//GPRS打开
        } else {
			mobileNetworkStatus = getString(R.string.aurora_no_connect_ssid);
		}
        return mobileNetworkStatus;
    }

	public static boolean isMobileNetworkConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State state = activeNetworkInfo.getState();
		if (getMobileDataStatus(context)) { // 判断是否正在使用GPRS网络
			return true;
		} else {
			return false;
		}
    }

	public static boolean getMobileDataStatus(Context context) {
		ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		Class<?> conMgrClass = null; // ConnectivityManager类
		Field iConMgrField = null; // ConnectivityManager类中的字段
		Object iConMgr = null; // IConnectivityManager类的引用
		Class<?> iConMgrClass = null; // IConnectivityManager类
		Method getMobileDataEnabledMethod = null; // setMobileDataEnabled方法

		try {
			// 取得ConnectivityManager类
			conMgrClass = Class.forName(conMgr.getClass().getName());
			// 取得ConnectivityManager类中的对象mService
			iConMgrField = conMgrClass.getDeclaredField("mService");
			// 设置mService可访问
			iConMgrField.setAccessible(true);
			// 取得mService的实例化类IConnectivityManager
			iConMgr = iConMgrField.get(conMgr);
			// 取得IConnectivityManager类
			iConMgrClass = Class.forName(iConMgr.getClass().getName());
			// 取得IConnectivityManager类中的getMobileDataEnabled(boolean)方法
			getMobileDataEnabledMethod = iConMgrClass
					.getDeclaredMethod("getMobileDataEnabled");
			// 设置getMobileDataEnabled方法可访问
			getMobileDataEnabledMethod.setAccessible(true);
			// 调用getMobileDataEnabled方法
			return (Boolean) getMobileDataEnabledMethod.invoke(iConMgr);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	//bluetooth connect

	private String getBluetoothStatus() {
        String bluetoothStatus = null;
		/**if (isBluetoothConnection(this)) {
			
        	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
            if (bluetooth.isEnabled()) {
            	wifiStatus = getString(R.string.aurora_wifi_connect_ssid, wifiInfo.getSSID()).replace("\"", "");
            }
			
        } else {
				bluetoothStatus = getString(R.string.aurora_wifi_no_connect_ssid);
		}*/
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
//		Log.i("qy", "bluetooth.isEnabled()"+bluetooth.isEnabled());
		if (bluetooth != null && !bluetooth.isEnabled()) {
            	bluetoothStatus = getString(R.string.aurora_no_connect_ssid);
        }
        return bluetoothStatus;
    }

	public static boolean isBluetoothConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
		State state = activeNetworkInfo.getState();
		if (State.CONNECTED == state) { // 判断是否正在使用BlueTooth
			return true;
		} else {
			return false;
		}
    }

	public boolean isCanUseSim() { 
	    try { 
	        TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
	        return TelephonyManager.SIM_STATE_READY == mgr.getSimState(); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
	}

	private boolean isCanUseSim(int slotId) { 
		isCanUseSim();
		return TelephonyManager.SIM_STATE_READY == GnTelephonyManager.getSimStateGemini(slotId);
	}

    private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    @Override
    public void switchToHeader(Header header) {
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }
        super.switchToHeader(header);
    }

    /**
     * AuroraSwitch to parent fragment and store the grand parent's info
     * @param className name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
                highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment
                        = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, "Could not find parent activity : " + className);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // If it is not launched from history, then reset to top-level
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (mFirstHeader != null && !onIsHidingHeaders() && onIsMultiPane()) {
                switchToHeaderLocal(mFirstHeader);
            }
            getListView().setSelectionFromTop(0, 0);
        }
    }

    private void highlightHeader(int id) {
        if (id != 0) {
            Integer index = mHeaderIndexMap.get(id);
            if (index != null) {
                getListView().setItemChecked(index, true);
                if (isMultiPane()) {
                    getListView().smoothScrollToPosition(index);
                }
            }
        }
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and
     * returns the class name to load as a fragment.
     */
    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) return null;

        if ("com.android.settings.ManageApplications".equals(intentClass)
                || "com.android.settings.RunningServices".equals(intentClass)
                || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            // Old names of manage apps.
            intentClass = com.android.settings.applications.ManageApplications.class.getName();
        }

        return intentClass;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be launched
     * for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
            int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args,
                titleRes, shortTitleRes);

        // some fragments want to avoid split actionbar
        if (DataUsageSummary.class.getName().equals(fragmentName) ||
                PowerUsageSummary.class.getName().equals(fragmentName) ||
                AccountSyncSettings.class.getName().equals(fragmentName) ||
                UserDictionarySettings.class.getName().equals(fragmentName) ||
                Memory.class.getName().equals(fragmentName) ||
                ManageApplications.class.getName().equals(fragmentName) ||
                WirelessSettings.class.getName().equals(fragmentName) ||
                SoundSettings.class.getName().equals(fragmentName) ||
                PrivacySettings.class.getName().equals(fragmentName) ||
                ManageAccountsSettings.class.getName().equals(fragmentName) ||
                VpnSettings.class.getName().equals(fragmentName) ||
                SecuritySettings.class.getName().equals(fragmentName) ||
                InstalledAppDetails.class.getName().equals(fragmentName) ||
                ChooseLockGenericFragment.class.getName().equals(fragmentName)) {
            intent.putExtra(EXTRA_CLEAR_UI_OPTIONS, true);
        }

        intent.setClass(this, SubSettings.class);
        return intent;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {
        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
        //loadHeadersFromResource(R.xml.settings_headers, headers);
    	// Aurora <likai> modify begin
        //loadHeadersFromResource(R.xml.gn_settings_headers_all, headers);
        loadHeadersFromResource(R.xml.aurora_settings_headers, headers);
        // Aurora <likai> modify end
        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end

        updateHeaderList(headers);
        
        //Gionee:zhang_xin 2012-12-15 add for CR00746738 start
        getFrameListBackground(Settings.this);
        mPreferenceBackgroundIndexs = getPreferenceBackgroundIndexs(headers);
        //Gionee:zhang_xin 2012-12-15 add for CR00746738 end
    }

//    更新系统中是否有这些程序
    private void updateHeaderList(List<Header> target) {
        final boolean showDev = mDevelopmentPreferences.getBoolean(
                DevelopmentSettings.PREF_SHOW,
                android.os.Build.TYPE.equals("eng"));
        int i = 0;

        mHeaderIndexMap.clear();
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int) header.id;
            if (id == R.id.operator_settings || id == R.id.manufacturer_settings) {
                Utils.updateHeaderToSpecificActivityFromMetaDataOrRemove(this, target, header);
            } else if (id == R.id.wifi_settings) {
                // Remove WiFi Settings if WiFi service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) { 
                    target.remove(i);
                }
            } else if (id == R.id.bluetooth_settings) {
                // Remove Bluetooth Settings if Bluetooth service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    target.remove(i);
                }
            } else if (id == R.id.sim_settings) {
                if (!(FeatureOption.MTK_GEMINI_SUPPORT && GnUtils.isDoubleCard())) {
                    target.remove(header);
                }
            } else if (id == R.id.data_usage_settings) {
                //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
                /*
                // Remove data usage when kernel module not enabled
                final INetworkManagementService netManager = INetworkManagementService.Stub
                        .asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
                try {
                    if (!netManager.isBandwidthControlEnabled()) {
                        target.remove(i);
                    }
                } catch (RemoteException e) {
                    // ignored
                }
                */
                target.remove(header);
                //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
            } else if (id == R.id.account_settings) {
                int headerIndex = i + 1;
                i = insertAccountsHeaders(target, headerIndex);
            } else if (id == R.id.user_settings) {
                if (!UserHandle.MU_ENABLED
                        || !UserManager.supportsMultipleUsers()
                        || Utils.isMonkeyRunning()) {
                    target.remove(i);
                }
            //Gionee <wangguojing> <2013-08-12> modify for CR00854591 begin
            } /*else if (id == R.id.development_settings) {
               if (!showDev) {
                    target.remove(i);
                }
            }*/
            //Gionee <wangguojing> <2013-08-12> modify for CR00854591 end
            // Gionee zengxuanhui 20130328 add for CR00788327 begin
            else if (id == R.id.smart_gesture_settings) {
                boolean isSmartGestureSupport = SystemProperties.get("ro.gn.gesture.support", "no").equals(
                        "yes");
                if (!isSmartGestureSupport) {
                    target.remove(header);
                }
            //AURORA-START::delete this item::waynelin::2013-9-18
                target.remove(header);
	    //AURORA-END::delete this item::waynelin::2013-9-18	
            }
            // Gionee zengxuanhui 20130328 add for CR00788327 end
            //Gionee lihq 2012-09-04 add for CR00682401 start
            else if (id == R.id.backup_restore_settings) {
                boolean isExitBackup = SystemProperties.get("ro.gn.backup.manage.support", "no")
                        .startsWith("yes");
                if (!isExitBackup) {
                    target.remove(header);
                }
            }
            //Gionee lihq 2012-09-04 add for CR00682401 end
            
            //Gionee lihq 2012-09-04 add for CR00687213 start
            else if(id == R.id.power_settings){
                boolean isPowerNotSupport = SystemProperties.get("ro.gn.schpower.not.support", "no")
                .equals("yes");
                if(isPowerNotSupport){
                    target.remove(header);
                }
            }
            //Gionee lihq 2012-09-04 add for CR00687213 end
          // Gionee <chenml> <2013-05-20> add for CR00817466 begin 
            else if(id== R.id.gn_sound_control){
                boolean isSoundControl = SystemProperties.get("ro.gn.soundctrl.support", "no")
                        .equals("yes");
                if(!isSoundControl){
                    target.remove(header);
                }
                
            }
         // Gionee <chenml> <2013-05-20> add for CR00817466 end

          // Gionee <chenml> <2013-05-30> add for CR00821135 begin 
            else if(id== R.id.gn_suspend_button){
                boolean isSuspendButtonSupport = SystemProperties.get("ro.gn.suspendbutton.support", "no")
                        .equals("yes");
                if(!isSuspendButtonSupport){
                    target.remove(header);
                }
            }
            else if(id== R.id.gn_single_hand_operation){
                boolean isSinglHandSupport = SystemProperties.get("ro.gn.operation.support", "no")
                        .equals("yes");
                if(!isSinglHandSupport){
                    target.remove(header);
                }
            }
         // Gionee <chenml> <2013-05-30> add for CR00821135 end
         	// Gionee <wangyaohui><2013-06-04> add for CR00820909 begin
			else if(id== R.id.gn_fanfan_widget){
                if ( !SystemProperties.get("ro.gn.fanfanwidget.support").equals("yes")){
                    target.remove(header);
                }
            }
			// Gionee <wangyaohui><2013-06-04> add for CR00820909 end
			// Gionee <wangyaohui><2013-06-05> add for CR00823496 begin
			else if(id== R.id.gn_respirationlamp_settings){
                if ( !SystemProperties.get("ro.gn.respirationlamp.support").equals("yes")){
                    target.remove(header);
                }
            }
			// Gionee <wangyaohui><2013-06-05> add for CR00823496 end
            //Gionee <wangguojing> <2013-07-20> add  begin
            else if(id== R.id.location_settings){
                if(FeatureOption.MTK_GEMINI_SUPPORT){
                    target.remove(header);
                }
            }
            else if(id== R.id.location_settings_mtk){
                if(!FeatureOption.MTK_GEMINI_SUPPORT){
                    target.remove(header);
                }
            }
            //Gionee <wangguojing> <2013-07-20> add  begin

			//add by steve.tang, SIM management for dual sim {@
/*			else if(id == R.id.sim_management) {
				if(!GnTelephonyManager.isMultiSimEnabled()) {
					target.remove(header);
				}
			}*/
			//@}add by steve.tang, SIM management for dual sim 
			
			//add for theme
            if(id == R.id.theme_settings){
            	/*
               String theme = SystemProperties.get("persist.sys.aurora.overlay","");
               if(theme == null  || theme.isEmpty()){
            	   target.remove(header);
               }else  if(!theme.equals("/system/theme/woman/") && !theme.equals("/system/theme/kity/")){
            	   target.remove(header);
               }
               */
            	target.remove(header);
            }
            
            
            
            if (target.get(i) == header
                    && UserHandle.MU_ENABLED && UserHandle.myUserId() != 0
                    && !ArrayUtils.contains(SETTINGS_FOR_RESTRICTED, id)) {
                target.remove(i);
            }

            // Increment if the current one wasn't removed by the Utils code.
            if (target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the top-level
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }

    private int insertAccountsHeaders(List<Header> target, int headerIndex) {
        String[] accountTypes = mAuthenticatorHelper.getEnabledAccountTypes();
        List<Header> accountHeaders = new ArrayList<Header>(accountTypes.length);
        for (String accountType : accountTypes) {
            CharSequence label = mAuthenticatorHelper.getLabelForType(this, accountType);
            if (label == null) {
                continue;
            }

            Account[] accounts = AccountManager.get(this).getAccountsByType(accountType);
            boolean skipToAccount = accounts.length == 1
                    && !mAuthenticatorHelper.hasAccountPreferences(accountType);
            Header accHeader = new Header();
            accHeader.title = label;
            if (accHeader.extras == null) {
                accHeader.extras = new Bundle();
            }
            if (skipToAccount) {
                accHeader.breadCrumbTitleRes = R.string.account_sync_settings_title;
                accHeader.breadCrumbShortTitleRes = R.string.account_sync_settings_title;
                accHeader.fragment = AccountSyncSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                // Need this for the icon
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.extras.putParcelable(AccountSyncSettings.ACCOUNT_KEY, accounts[0]);
                accHeader.fragmentArguments.putParcelable(AccountSyncSettings.ACCOUNT_KEY,
                        accounts[0]);
            } else {
                accHeader.breadCrumbTitle = label;
                accHeader.breadCrumbShortTitle = label;
                accHeader.fragment = ManageAccountsSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE,
                        accountType);
                if (!isMultiPane()) {
                    accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL,
                            label.toString());
                }
            }
            accountHeaders.add(accHeader);
        }

        // Sort by label
        Collections.sort(accountHeaders, new Comparator<Header>() {
            @Override
            public int compare(Header h1, Header h2) {
                return h1.title.toString().compareTo(h2.title.toString());
            }
        });

        for (Header header : accountHeaders) {
            target.add(headerIndex++, header);
        }
        
       
        
        if (!mListeningToAccountUpdates) {
            AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
        return headerIndex;
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) return;
            mTopLevelHeaderId = ai.metaData.getInt(META_DATA_KEY_HEADER_ID);
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);

            // Check if it has a parent specified and create a Header object
            final int parentHeaderTitleRes = ai.metaData.getInt(META_DATA_KEY_PARENT_TITLE);
            String parentFragmentClass = ai.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
            if (parentFragmentClass != null) {
                mParentHeader = new Header();
                mParentHeader.fragment = parentFragmentClass;
                if (parentHeaderTitleRes != 0) {
                    mParentHeader.title = getResources().getString(parentHeaderTitleRes);
                }
            }
        } catch (NameNotFoundException nnfe) {
            // No recovery
        }
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
        // private final WifiEnabler mWifiEnabler;
        // private final BluetoothEnabler mBluetoothEnabler;
        private final GnAirplaneModeEnabler mAirplaneModeEnabler;
		// Gionee <wangyaohui><2013-06-04> add for CR00820909 begin
		private final GnPushWidget mPushWidget;
		// Gionee <wangyaohui><2013-06-04> add for CR00820909 end
        //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
        //Gionee <chenml> <2013-05-30> add for CR00821135 begin
        private final GnSuspendButtonEnabler mSuspendButtonEnabler;
        //Gionee <chenml> <2013-05-30> add for CR00821135 end
        ///M: add for tablet feature check whether sim exist
        private boolean mIsSimEnable = false;
        private AuthenticatorHelper mAuthHelper;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            AuroraSwitch switch_;
            // Aurora <likai> <2013-10-24> add begin
            TextView status;
            ImageView right;
            // Aurora <likai> <2013-10-24> add end
        }

        // Aurora <likai> <2013-10-24> add begin
        private String mWifiStatus = null;
        // Aurora <likai> <2013-10-24> add end
		private String mBluetoothStatus = null;
		private String mMobileNetworkStatus = null;		

		// Add begin by aurora.jiangmx
        private String mPerSpaceStatus = null;
		// Add end
		
        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
            /*
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
                return HEADER_TYPE_SWITCH;
            } else {
                return HEADER_TYPE_NORMAL;
            }
            */
            if (header.fragment == null && header.intent == null && 
                 //   header.id != R.id.gn_toggle_airplane && header.id != R.id.gn_suspend_button   ) {
                 header.id != R.id.gn_toggle_airplane && header.id != R.id.gn_suspend_button && header.id != R.id.gn_fanfan_widget &&
                 header.id != R.id.personal_space && header.id != R.id.feedback) {

                return HEADER_TYPE_CATEGORY;
            } else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings ) {
                return HEADER_TYPE_NORMAL;
            } else if (header.id == R.id.gn_toggle_airplane|| header.id == R.id.gn_suspend_button || header.id == R.id.gn_fanfan_widget) {
                return HEADER_TYPE_SWITCH;
            } else {
                return HEADER_TYPE_NORMAL;
            }
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects,
                AuthenticatorHelper authenticatorHelper) {
            super(context, 0, objects);

            mAuthHelper = authenticatorHelper;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
            // mWifiEnabler = new WifiEnabler(context, new AuroraSwitch(context));
            // mBluetoothEnabler = new BluetoothEnabler(context, new AuroraSwitch(context));
            mAirplaneModeEnabler = new GnAirplaneModeEnabler(context, new AuroraSwitch(context));
			// Gionee <wangyaohui><2013-06-04> add for CR00820909 begin
			  mPushWidget = new GnPushWidget(context, new AuroraSwitch(context));
			// Gionee <wangyaohui><2013-06-04> add for CR00820909 end
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end

           //Gionee <chenml> <2013-05-30> add for CR00821135 begin
            mSuspendButtonEnabler = new GnSuspendButtonEnabler(context, new AuroraSwitch(context));
           //Gionee <chenml> <2013-05-30> add for CR00821135 end
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);

                        // Aurora <likai> add begin
                        view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mListSeparatorHeight));
                        // Aurora <likai> add end

                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_SWITCH:
                        view = mInflater.inflate(R.layout.preference_header_switch_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(android.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(android.R.id.summary);
                        holder.switch_ = (AuroraSwitch) view.findViewById(R.id.switchWidget);
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(android.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(android.R.id.summary);

                        // Aurora <likai> <2013-10-24> add begin
                        holder.status = (TextView) view.findViewById(R.id.status);
                        holder.right = (ImageView) view.findViewById(R.id.right);
                        // Aurora <likai> <2013-10-24> add end
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                	// Aurora <likai> modify begin
                    //holder.title.setText(header.getTitle(getContext().getResources()));
                    holder.title.setText(null);
                    // Aurora <likai> modify end
                    break;

                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
                    /*
                    if (header.id == R.id.wifi_settings) {
                        mWifiEnabler.setSwitch(holder.switch_);
                    } else {
                        mBluetoothEnabler.setSwitch(holder.switch_);
                    }
                    */
                    if (header.id == R.id.gn_toggle_airplane) {
                        mAirplaneModeEnabler.setSwitch(holder.switch_);
                    }
                    //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end
                 //Gionee <chenml> <2013-05-30> add for CR00821135 begin
                    if(header.id == R.id.gn_suspend_button){
                        mSuspendButtonEnabler.setSwitch(holder.switch_);
                    }
                //Gionee <chenml> <2013-05-30> add for CR00821135 end
				// Gionee <wangyaohui><2013-06-04> add for CR00820909 begin
                    else if (header.id == R.id.gn_fanfan_widget) {
                        mPushWidget.setSwitch(holder.switch_);
                    }
					// Gionee <wangyaohui><2013-06-04> add for CR00820909 end
                    // No break, fall through on purpose to update common fields

                    //$FALL-THROUGH$
                case HEADER_TYPE_NORMAL:
                    if (header.extras != null
                            && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                        String accType = header.extras.getString(
                                ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
                        lp.width = getContext().getResources().getDimensionPixelSize(
                                R.dimen.header_icon_width);
                        lp.height = lp.width;
                        holder.icon.setLayoutParams(lp);
                        Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                        holder.icon.setImageDrawable(icon);
                    } else {
                        holder.icon.setImageResource(header.iconRes);
                    }
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    Log.d(TAG, "HEADER_TYPE_NORMAL   getTitle="+header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }

//                    设置wifi的状态
                    // Aurora <likai> <2013-10-24> add begin
                    if (holder.status != null) {
                    	if (header.id == R.id.wifi_settings && mWifiStatus != null) {
                            holder.status.setVisibility(View.VISIBLE);
                            holder.status.setText(mWifiStatus);
                            WifiSettings.PrintLog(TAG, "WifiStatus:"+mWifiStatus);
                            
                        } else if(header.id == R.id.bluetooth_settings && mBluetoothStatus != null) {
							holder.status.setVisibility(View.VISIBLE);
                            holder.status.setText(mBluetoothStatus);
						} else if(header.id == R.id.gn_ic_settings_mobile_network && mMobileNetworkStatus != null) {
							holder.status.setVisibility(View.VISIBLE);
                            holder.status.setText(mMobileNetworkStatus);
						} else if( header.id == R.id.personal_space && mPerSpaceStatus != null){
                        	holder.status.setVisibility(View.VISIBLE);
                            holder.status.setText(mPerSpaceStatus);
                        }else {
                            holder.status.setVisibility(View.GONE);
                        }
                    }
                    // Aurora <likai> <2013-10-24> add end
                    break;
            }
            // /M: add for sim management feature
            // Gionee <wangyaohui><2013-03-25> modify for CR00787755 begin
            // if (header.id == R.id.sim_settings) {

			//steve.tang 2014-06-05 for dual sim mobile network settings,start 
			if(header.id == R.id.gn_ic_settings_mobile_network){
				if(!MULTI_SIM_ENABLE){
					header.intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
					header.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				} else {
					header.intent.setClassName("com.android.phone", "com.android.phone.MSimMobileNetworkSettings");
					header.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				}
			}
			//steve.tang 2014-06-05 for dual sim mobile network settings,end 
           
			//Begin add by gary.gou
/*			if(header.id == R.id.sim_management){
				header.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				//header.intent.setClassName("com.android.phone", "com.android.phone.AuroraMultiSimSettings");
			}*/
			//End add by gary.gou
			
            if ((header.id == R.id.sim_settings) ||(header.id == R.id.gn_ic_settings_mobile_network)) {
				handleDisableHolder(holder, view);
            } else {
                handleEnableHolder(holder, view);
            }
            // Gionee <wangyaohui><2013-03-25> modify for CR00787755 end 

            //Gionee:zhang_xin 2012-12-15 add for CR00746738 start
            if (mPreferenceBackgroundIndexs != null && mPreferenceBackgroundIndexs.length > position) {
                if (mPreferenceBackgroundRes != null
                        && mPreferenceBackgroundIndexs[position] < mPreferenceBackgroundRes.length
                        && mPreferenceBackgroundRes[mPreferenceBackgroundIndexs[position]] > 0) {
                    view.setBackgroundResource(mPreferenceBackgroundRes[mPreferenceBackgroundIndexs[position]]);
                }
            }
            //Gionee:zhang_xin 2012-12-15 add for CR00746738 end

            return view;
        }
        /*
         * M: set the icon title and summary for sim management
         *@ param holder the view holder
         *@ param view the view
         */
        private void handleEnableHolder(HeaderViewHolder holder, View view) {
            if (holder.icon != null) {
                holder.icon.setEnabled(true);
                // /M: set the icon back to original alpha @{
                holder.icon.setAlpha(ORIGINAL_IMAGE);
                // /@}
            }

            // Aurora <likai> <2013-10-24> add begin
            if (holder.right != null) {
            	holder.right.setEnabled(true);
            	holder.right.setAlpha(ORIGINAL_IMAGE);
            }
            // Aurora <likai> <2013-10-24> add end

            if (holder.title != null) {
                holder.title.setEnabled(true);
            }
            if (holder.summary != null) {
                holder.summary.setEnabled(true);
            }
            view.setClickable(false);
        }
        /*
         * M: Disable the holder if there is no sim card 
         */
        private void handleDisableHolder(HeaderViewHolder holder, View view) {
        	isHiden = (mIsSimEnable && !mIsAirplaneMode && mHasUseSimCard);
            holder.icon.setEnabled(isHiden);
            /**M: add to gray the imageview when there is no sim card inserted @{*/ 
            holder.icon.setAlpha(isHiden ? ORIGINAL_IMAGE : IMAGE_GRAY);
            /**@}*/

            // Aurora <likai> <2013-10-24> add begin
            if (holder.right != null) {
            	holder.right.setEnabled(isHiden);
            	holder.right.setAlpha(isHiden ? ORIGINAL_IMAGE : IMAGE_GRAY);
            }
            // Aurora <likai> <2013-10-24> add end
            if(holder.status != null){
            	holder.status.setEnabled(isHiden);
            }

            holder.title.setEnabled(isHiden);
            holder.summary.setEnabled(isHiden);
            view.setClickable(!isHiden);
            // Aurora <likai> <2013-11-20> add begin
            view.setEnabled(isHiden);
            // Aurora <likai> <2013-11-20> add end
        }
        public void isSimManagementAvailable(Context context) {
            //Gionee huangsf 2013-06-28 add for sim disable start
            mIsSimEnable = GnUtils.isSimManagementAvailable(context);
            //Gionee huangsf 2013-06-28 add for sim disable start
        }

        // Aurora <likai> <2013-10-24> add begin
        public void updateWifiStatus(String wifiStatus) {
            mWifiStatus = wifiStatus;
        }
        // Aurora <likai> <2013-10-24> add end
		public void updateBluetoothStatus(String bluetoothStatus) {
            mBluetoothStatus = bluetoothStatus;
        }
		public void updateMobileNetworkStatus(String mobileNetworkStatus) {
            mMobileNetworkStatus = mobileNetworkStatus;
        }

		// Add begin by aurora.jiangmx
		public void updatePersonSpaceLoginStatus(String pPerSpaceStatus) {
	    	mPerSpaceStatus = pPerSpaceStatus;
	    }
		// Add end
        public void resume() {
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
            // mWifiEnabler.resume();
            // mBluetoothEnabler.resume();
            mAirplaneModeEnabler.resume();
            mSuspendButtonEnabler.resume();
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end       
        }

        public void pause() {
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 start
            // mWifiEnabler.pause();
            // mBluetoothEnabler.pause();
            mAirplaneModeEnabler.pause();
            mSuspendButtonEnabler.pause();
            //Gionee:zhang_xin 2013-02-01 modify for CR00767874 end       
        }
    }
    // qy add 2014 07 17 for animation begin
    private boolean isClickHeader = false;
    // qy add 2014 07 17 for animation end
    @Override
    public void onHeaderClick(Header header, int position) {
        boolean revert = false;
        if (header.id == R.id.account_add) {
            revert = true;
        }
        if(header.id == R.id.wallpaper_settings && !isClickHeader) {
            Intent intent = new Intent("aurora.intent.action.wallpaper_set");
            intent.putExtra("fromApp", "settings");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        }

        super.onHeaderClick(header, position);

        if (revert && mLastHeader != null) {
            highlightHeader((int) mLastHeader.id);
        } else {
            mLastHeader = header;
        }
        // qy add begin 2014 04 20
        
        if(header.id == R.id.sound_settings && !isClickHeader) {
        	Intent t = new Intent("gn.com.android.audioprofile.action.AUDIO");
        	t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	
        	startActivity(t);
        	overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        	
        	
        	
        }
        
      if(header.id == R.id.personal_space && !isClickHeader) {
        	try{
        	Intent t = new Intent();
        	t.setAction(Intent.ACTION_VIEW);
        	t.setData(Uri.parse("openaccount://com.aurora.account.login"));
        	t.putExtra("needSetExitAnimation", true);  //需要有退出动画
        	t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	
        	startActivity(t);
        	}catch(Exception e){
        		
        	}
       }
      
      if(header.id == R.id.feedback && !isClickHeader)
      {
    	  Intent intent=new Intent();
          intent.setAction("com.android.settings.action.feedback");
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);
          overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
      }
        
        isClickHeader = true;
        countClick(header.id); //统计点击次数
        
/*        if(header.id == R.id.sound_settings) {
        	Intent t = new Intent("gn.com.android.audioprofile.action.AUDIO");
        	t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	startActivity(t);
        	overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        }*/
        // qy add end 2014 04 20
       
    }
    
    public void countClick(long id){
    	
    	if( R.id.wifi_settings == id){
    		mCountUtil.update("018", 1);
    	}else if(R.id.gn_ic_settings_mobile_network == id){
    		mCountUtil.update("019", 1);
    	} else if(R.id.bluetooth_settings == id){
    		mCountUtil.update("020", 1);
    	}else if(R.id.tether_settings == id){
    		mCountUtil.update("021", 1);
    	}else if(R.id.display_settings == id){
    		mCountUtil.update("023", 1);
    	}else if(R.id.wallpaper_settings == id){
    		mCountUtil.update("026", 1);
    	}/*else if(R.id.bootanimation_settings == id){
    		mCountUtil.update("024", 1);
    	}*/else if(R.id.date_time_settings == id){
    		mCountUtil.update("022", 1);
    	}else if(R.id.sound_settings == id){
    		mCountUtil.update("025", 1);
    	}else if(R.id.notify_push_settings == id){
    		mCountUtil.update("027", 1);
    	}else if(R.id.personal_space == id){
    		mCountUtil.update("028", 1);
    	}else if(R.id.advanced_settings == id){
    		mCountUtil.update("029", 1);
    	}else if(R.id.about_settings == id){
    		mCountUtil.update("030", 1);
    	}
    }

    @Override
    public boolean onPreferenceStartFragment(AuroraPreferenceFragment caller, AuroraPreference pref) {
        // Override the fragment title for Wallpaper settings
        int titleRes = pref.getTitleRes();
        if (pref.getFragment().equals(WallpaperTypeSettings.class.getName())) {
            titleRes = R.string.wallpaper_settings_fragment_title;
        } else if (pref.getFragment().equals(OwnerInfoSettings.class.getName())
                && UserHandle.myUserId() != UserHandle.USER_OWNER) {
            titleRes = R.string.user_info_settings_title;
        }
        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(),
                null, 0);
        return true;
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, Settings.class));
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // TODO: watch for package upgrades to invalidate cache; see 7206643
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, accounts);
        invalidateHeaders();
    }

    //Gionee <wangguojing> <2013-07-25> add for CR00837650 begin
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment  = null;
        Log.e("wangguojing","mKey ="+mKey);
        if(mKey == null){
            return super.onOptionsItemSelected(item);
        }
	
        if(mKey.equals("BluetoothSettings")) {
            fragment=BluetoothSettings.getBluetoothSettingsInstance();
        }else if(mKey.equals("WifiSettings")){
            fragment=WifiSettings.getWifiSettingsInstance();
        }else if(mKey.equals("WifiP2pSettings")){
            fragment=WifiP2pSettings.getWifiP2pSettingsInstance();
        }else if(mKey.equals("PowerUsageSummary")){
            fragment=PowerUsageSummary.getPowerUsageSummarysInstance();
        }else if(mKey.equals("ManageApplications")){
            fragment=ManageApplications.getManageApplicationsInstance();
        }/*else if(mKey.equals("AccountSyncSettings")){
            fragment=AccountSyncSettings.getAccountSyncSettingsInstance();
        }else if(mKey.equals("ManageAccountsSettings")){
            fragment=ManageAccountsSettings.getManageAccountsSettingsInstance();
        }*/else if(mKey.equals("DreamSettings")){
            fragment=DreamSettings.getDreamSettingsInstance();
        }else if(mKey.equals("VpnSettings")){
            fragment=VpnSettings.getVpnSettingsInstance();
        }else if(mKey.equals("UserDictionarySettings")){
            fragment=UserDictionarySettings.getUserDictionarySettingsInstance();
        }else if(mKey.equals("UserDictionaryAddWord")){
            fragment=UserDictionaryAddWordFragment.getUserDictionaryAddWordInstance();
        }/*else if(mKey.equals("WifiDisplaySettings")){
            fragment=WifiDisplaySettings.getWifiDisplaySettingsInstance();
        }*/
	
        if(fragment != null){
            fragment.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
   }
    //Gionee <wangguojing> <2013-07-25> add for CR00837650 end
    
    //Gionee <wangguojing> <2013-08-26> add for CR00873441 begin
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Fragment fragment  = null;
        if(mKey == null){
            return super.onContextItemSelected(item);
        }
		
        if(mKey.equals("VpnSettings")){
            fragment=VpnSettings.getVpnSettingsInstance();
        }
		
        if(fragment != null){
            fragment.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }
    //Gionee <wangguojing> <2013-08-26> add for CR00873441 end
	
    /*
     * Settings subclasses for launching independently.
     */
    public static class BluetoothSettingsActivity extends Settings { /* empty */ }
    public static class WirelessSettingsActivity extends Settings { /* empty */ }
    public static class TetherSettingsActivity extends Settings { /* empty */ }
    public static class VpnSettingsActivity extends Settings { /* empty */ }
    public static class DateTimeSettingsActivity extends Settings { /* empty */ }
    public static class StorageSettingsActivity extends Settings { /* empty */ }
    public static class WifiSettingsActivity extends Settings { /* empty */ }
    public static class WifiP2pSettingsActivity extends Settings { /* empty */ }
    public static class InputMethodAndLanguageSettingsActivity extends Settings { /* empty */ }
    public static class KeyboardLayoutPickerActivity extends Settings { /* empty */ }
    public static class InputMethodAndSubtypeEnablerActivity extends Settings { /* empty */ }
    public static class SpellCheckersSettingsActivity extends Settings { /* empty */ }
    public static class LocalePickerActivity extends Settings { /* empty */ }
    public static class UserDictionarySettingsActivity extends Settings { /* empty */ }
    public static class SoundSettingsActivity extends Settings { /* empty */ }
    public static class DisplaySettingsActivity extends Settings { /* empty */ }
    public static class DeviceInfoSettingsActivity extends Settings { /* empty */ }
    public static class ApplicationSettingsActivity extends Settings { /* empty */ }
    public static class ManageApplicationsActivity extends Settings { /* empty */ }
    public static class StorageUseActivity extends Settings { /* empty */ }
    public static class DevelopmentSettingsActivity extends Settings { /* empty */ }
    public static class AccessibilitySettingsActivity extends Settings { /* empty */ }
    public static class SecuritySettingsActivity extends Settings { /* empty */ }
    public static class LocationSettingsActivity extends Settings { /* empty */ }
    public static class PrivacySettingsActivity extends Settings { /* empty */ }
    public static class RunningServicesActivity extends Settings { /* empty */ }
//    public static class ManageAccountsSettingsActivity extends Settings { /* empty */ }
    public static class PowerUsageSummaryActivity extends Settings { /* empty */ }
//    public static class AccountSyncSettingsActivity extends Settings { /* empty */ }
//    public static class AccountSyncSettingsInAddAccountActivity extends Settings { /* empty */ }    
  
    
    public static class CryptKeeperSettingsActivity extends Settings { /* empty */ }
    public static class DeviceAdminSettingsActivity extends Settings { /* empty */ }
    public static class DataUsageSummaryActivity extends Settings { /* empty */ }
    public static class AdvancedWifiSettingsActivity extends Settings { /* empty */ }
    public static class TextToSpeechSettingsActivity extends Settings { /* empty */ }
    public static class AndroidBeamSettingsActivity extends Settings { /* empty */ }
    public static class WifiDisplaySettingsActivity extends Settings { /* empty */ }
	public static class NotifyPushSettingsActivity extends Settings { /* empty */ }

	public static class TwoSimSettingsActivity extends Settings { /* empty */ } //add by gary.gou
	
    // Aurora <likai> add begin
    public static class AdvancedSettingsActivity extends Settings { /* empty */ }
    // Aurora <likai> add end

    public static class NotificationAccessSettingsActivity extends Settings { /* empty */ }

    //Gionee:zhang_xin 2012-12-15 add for CR00746738 start
    private static int[] mPreferenceBackgroundIndexs;
    private static int[] mPreferenceBackgroundRes;
    
    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;
    
    private int[] getPreferenceBackgroundIndexs(List<Header> header) {
        if (header == null || header.size() <= 0) {
            return null;
        }
        
        
        int[] arrays = new int[header.size()];
        for (int i = 0; i < header.size(); i++) {
            if (HeaderAdapter.getHeaderType(header.get(i)) == HeaderAdapter.HEADER_TYPE_CATEGORY) {
                arrays[i] = FRAME_LIST_BACKGROUND_NULL;
                continue;
            }
            
            if (i > 0) {
                switch (arrays[i - 1]) {
                    case 0:
                        arrays[i] = FRAME_LIST_BACKGROUND_FULL;
                        break;
                    case 1:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_TOP;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 2:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 3:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 4:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_MIDDLE;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    default:
                        break;
                }
            } else {
                arrays[i] = FRAME_LIST_BACKGROUND_FULL;
            }
        }
        return arrays;
    }
    
    private void getFrameListBackground(Context context) {
        mPreferenceBackgroundRes = new int[FRAME_LIST_BACKGROUND_TOTAL];
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_NULL] = 0;
        TypedValue outValue = new TypedValue();

        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListTopBackground,
               outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListMiddleBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBottomBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_BOTTOM] = outValue.resourceId;
    }
    //Gionee:zhang_xin 2012-12-15 add for CR00746738 end
}
