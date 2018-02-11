/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.netmanage.model;

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static android.net.NetworkPolicy.LIMIT_DISABLED;
import static android.net.NetworkPolicy.WARNING_DISABLED;
import static android.net.NetworkPolicyManager.EXTRA_NETWORK_TEMPLATE;
import static android.net.NetworkPolicyManager.POLICY_NONE;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkPolicyManager.computeLastCycleBoundary;
import static android.net.NetworkPolicyManager.computeNextCycleBoundary;
import static android.net.NetworkTemplate.MATCH_MOBILE_3G_LOWER;
import static android.net.NetworkTemplate.MATCH_MOBILE_4G;
import static android.net.NetworkTemplate.MATCH_MOBILE_ALL;
import static android.net.NetworkTemplate.MATCH_WIFI;
import static android.net.NetworkTemplate.buildTemplateEthernet;
import static android.net.NetworkTemplate.buildTemplateMobile3gLower;
import static android.net.NetworkTemplate.buildTemplateMobile4g;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;
import static android.net.TrafficStats.GB_IN_BYTES;
import static android.net.TrafficStats.MB_IN_BYTES;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.internal.telephony.PhoneConstants;
import com.google.android.collect.Lists;
import com.netmanage.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import libcore.util.Objects;

/**
 * Panel showing data usage history across various networks, including options
 * to inspect based on usage cycle and control through {@link NetworkPolicy}.
 */
public class DataUsageSummary {
    private static final String TAG = "DataUsageSummary";
    private static final boolean LOGD = false;

    // TODO: remove this testing code
    private static final boolean TEST_ANIM = false;
    private static final boolean TEST_RADIOS = false;

    private static final String TEST_RADIOS_PROP = "test.radios";
    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";

    private static final String TAB_3G = "3g";
    private static final String TAB_4G = "4g";
    private static final String TAB_MOBILE = "mobile";
    private static final String TAB_WIFI = "wifi";
    private static final String TAB_ETHERNET = "ethernet";

    private static final String TAG_CONFIRM_DATA_DISABLE = "confirmDataDisable";
    private static final String TAG_CONFIRM_DATA_ROAMING = "confirmDataRoaming";
    private static final String TAG_CONFIRM_LIMIT = "confirmLimit";
    private static final String TAG_CYCLE_EDITOR = "cycleEditor";
    private static final String TAG_WARNING_EDITOR = "warningEditor";
    private static final String TAG_LIMIT_EDITOR = "limitEditor";
    private static final String TAG_CONFIRM_RESTRICT = "confirmRestrict";
    private static final String TAG_DENIED_RESTRICT = "deniedRestrict";
    private static final String TAG_CONFIRM_APP_RESTRICT = "confirmAppRestrict";
    private static final String TAG_CONFIRM_AUTO_SYNC_CHANGE = "confirmAutoSyncChange";
    private static final String TAG_APP_DETAILS = "appDetails";

    private static final int LOADER_CHART_DATA = 2;
    private static final int LOADER_SUMMARY = 3;

    private INetworkManagementService mNetworkService;
    private INetworkStatsService mStatsService;
    private NetworkPolicyManager mPolicyManager;
    private ConnectivityManager mConnService;

    private INetworkStatsSession mStatsSession;

    private static final String PREF_FILE = "data_usage";
    private static final String PREF_SHOW_WIFI = "show_wifi";
    private static final String PREF_SHOW_ETHERNET = "show_ethernet";

    private NetworkTemplate mTemplate;

    private Context context;
    private GetFlowListener getFlowListener;
    private String imsiOfLastGetFlow;//最后一次获取流量的sim卡的imsi号；

    public DataUsageSummary(Context context) {
        this.context = context;

        mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        mStatsService = INetworkStatsService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mPolicyManager = NetworkPolicyManager.from(getActivity());
        mConnService = ConnectivityManager.from(getActivity());

        try {
            mStatsSession = mStatsService.openSession();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    
    private Context getActivity(){
       return this.context;	
    }
    
    public INetworkStatsSession getStatsSession(){
    	return this.mStatsSession;
    }
    
    public NetworkTemplate getTemplate(){
    	return this.mTemplate;
    }
    
    /**
     * 最后一次获取流量的sim卡的imsi号
     * @return
     */
    public String getImsiOfLastGetFlow(){
    	return imsiOfLastGetFlow;
    }
    
    /**
     * 获取流量数据
     */
    public void getFlowData(){
    	NetModel instance = NetModel.getInstance();
    	//在已经获取流量的情况下在去刷新，这样的就以免刷新耗时太久，而出现界面等待
    	if(instance != null && instance.isGetFlow()){
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {           	
                	 try {
//                		 Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
                		 //这个函数有可能耗时特别久
                		 Log.i(TAG,"forceUpdate net flow");
                         mStatsService.forceUpdate();
                     } catch (Exception e) {
                     } 
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    updateBody(TAB_MOBILE);
                    updateDetailData();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    	}else{
    		updateBody(TAB_MOBILE);
            updateDetailData();
    	}   	 
    }
    
    private void updateBody(String currentTab) {
        if (TAB_MOBILE.equals(currentTab)) {
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(getActivity()));
        } else if (TAB_3G.equals(currentTab)) {
            mTemplate = buildTemplateMobile3gLower(getActiveSubscriberId(getActivity()));
        } else if (TAB_4G.equals(currentTab)) {
            mTemplate = buildTemplateMobile4g(getActiveSubscriberId(getActivity()));
        } else if (TAB_WIFI.equals(currentTab)) {
            mTemplate = buildTemplateWifiWildcard();
        } else if (TAB_ETHERNET.equals(currentTab)) {
            mTemplate = buildTemplateEthernet();
        } else {
            throw new IllegalStateException("unknown tab: " + currentTab);
        }
    }
    
    private String getActiveSubscriberId(Context context) {
    	//huangbin hide at 20140917 begin
//        final TelephonyManager tele = TelephonyManager.from(context);
//        final String actualSubscriberId = tele.getSubscriberId();
//        return SystemProperties.get(TEST_SUBSCRIBER_PROP, actualSubscriberId);
    	//huangbin hide at 20140917 end
    	
    	//huangbin add at 20140917 begin
        final String actualSubscriberId =Utils.getImsi(context);
        imsiOfLastGetFlow = actualSubscriberId;
        return SystemProperties.get(TEST_SUBSCRIBER_PROP, actualSubscriberId);
        //huangbin add at 20140917 end
    }
    
    private void updateDetailData() {     	
    	new SummaryForAllUidLoader(getActivity(), 
    			mStatsSession,
    			SummaryForAllUidLoader.buildArgs(mTemplate),this).execute();
    }
    
    public void endFuncOfGetFlow(NetworkStats forShowNetworkStats){
    	if(getFlowListener != null){
    		getFlowListener.finish(forShowNetworkStats);
    	}
    }
        
    public void setGetFlowListener(GetFlowListener getFlowListener){
    	this.getFlowListener = getFlowListener;
    }
    
    /**
     * 获取流量的状态监控
     * @author chengrq
     *
     */
    public interface GetFlowListener {
    	/**
    	 * 流量获取结束
    	 * @param forShowNetworkStats
    	 * @param forStatisticsNetworkStats
    	 */
    	void finish(NetworkStats forShowNetworkStats);
    }
}
