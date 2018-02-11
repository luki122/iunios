/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import android.provider.Settings;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.android.settings.HelpUtils;
import com.android.settings.R;

import java.util.List;

import com.gionee.settings.utils.GnUtils;

/**
 * Displays a list of apps and subsystems that consume power, ordered by how much power was
 * consumed since the last time it was unplugged.
 */
public class PowerUsageSummary extends AuroraPreferenceFragment implements /*Runnable,*/ AuroraPreference.OnPreferenceChangeListener {

    private static final boolean DEBUG = false;

    private static final String TAG = "PowerUsageSummary";

    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_BATTERY_STATUS = "battery_status";
    private static final String KEY_BATTERY_PERCENTAGE = "battery_percentage";
    private static final String KEY_CPU_DTM = "cpu_dtm";

    private static final int MENU_STATS_TYPE = Menu.FIRST;
    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
    private static final int MENU_HELP = Menu.FIRST + 2;

    private AuroraPreferenceGroup mAppListGroup;
    private AuroraPreference mBatteryStatusPref;

    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

    private static final int MIN_POWER_THRESHOLD = 5;
    private static final int MAX_ITEMS_TO_LIST = 10;
	private static PowerUsageSummary mPowerUsageSummary ;
    private BatteryStatsHelper mStatsHelper;
    private BatteryStatsImpl mStats;
    private static final String BATTERY_PERCENTAGE = "battery_percentage";
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";

	// qy add begin 2014 06 30 for Aurora_mediaScanner
    private double percentOfMaxMediaScanner;
    private double percentOfTotalMediaScanner;
    private double sipperSortValue =0.0;
    // qy add end

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                String batteryLevel = com.android.settings.Utils.getBatteryPercentage(intent);
                String batteryStatus = com.android.settings.Utils.getBatteryStatus(getResources(),
                        intent);
                String batterySummary = context.getResources().getString(
                        R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
                mBatteryStatusPref.setTitle(batterySummary);
                mStatsHelper.clearStats();
                refreshStats();
            }
        }
    };

//
    public static PowerUsageSummary getPowerUsageSummarysInstance() {
		return mPowerUsageSummary ;
		
    }
    public PowerUsageSummary() {
        mPowerUsageSummary = this;
        GnUtils.setSettingsmkey(TAG) ;        
    }
    //

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStatsHelper = new BatteryStatsHelper(activity, mHandler);
    }
   

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mStatsHelper.create(icicle);
        
        mStats = mStatsHelper.getStats();
        addPreferencesFromResource(R.xml.power_usage_summary);
        mAppListGroup = (AuroraPreferenceGroup) findPreference(KEY_APP_LIST);
        mBatteryStatusPref = mAppListGroup.findPreference(KEY_BATTERY_STATUS);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBatteryInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        refreshStats();
    }

    @Override
    public void onPause() {
        mStatsHelper.pause();
        mHandler.removeMessages(BatteryStatsHelper.MSG_UPDATE_NAME_ICON);
        getActivity().unregisterReceiver(mBatteryInfoReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStatsHelper.destroy();
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference instanceof BatteryHistoryPreference) {
            Parcel hist = Parcel.obtain();
            mStatsHelper.getStats().writeToParcelWithoutUids(hist, 0);
            byte[] histData = hist.marshall();
            Bundle args = new Bundle();
            args.putByteArray(BatteryHistoryDetail.EXTRA_STATS, histData);
            AuroraPreferenceActivity pa = (AuroraPreferenceActivity)getActivity();
            pa.startPreferencePanel(BatteryHistoryDetail.class.getName(), args,
                    R.string.history_details_title, null, null, 0);
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        if (!(preference instanceof PowerGaugePreference)) {
            return false;
        }
        PowerGaugePreference pgp = (PowerGaugePreference) preference;
        BatterySipper sipper = pgp.getInfo();
        mStatsHelper.startBatteryDetailPage((AuroraPreferenceActivity) getActivity(), sipper, true);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) {
            menu.add(0, MENU_STATS_TYPE, 0, R.string.menu_stats_total)
                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
                    .setAlphabeticShortcut('t');
        }
        MenuItem refresh = menu.add(0, MENU_STATS_REFRESH, 0, R.string.menu_stats_refresh)
                .setIcon(R.drawable.ic_menu_refresh_holo_dark)
                .setAlphabeticShortcut('r');
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        String helpUrl;
        if (!TextUtils.isEmpty(helpUrl = getResources().getString(R.string.help_url_battery))) {
            final MenuItem help = menu.add(0, MENU_HELP, 0, R.string.help_label);
            HelpUtils.prepareHelpMenuItem(getActivity(), help, helpUrl);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_TYPE:
                if (mStatsType == BatteryStats.STATS_SINCE_CHARGED) {
                    mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
                } else {
                    mStatsType = BatteryStats.STATS_SINCE_CHARGED;
                }
                refreshStats();
                return true;
            case MENU_STATS_REFRESH:
                mStatsHelper.clearStats();
                refreshStats();
                return true;
            default:
                return false;
        }
    }

    private void addNotAvailableMessage() {
        AuroraPreference notAvailable = new AuroraPreference(getActivity());
        notAvailable.setTitle(R.string.power_usage_not_available);
        mAppListGroup.addPreference(notAvailable);
    }

    private void refreshStats() {
        mAppListGroup.removeAll();
        mAppListGroup.setOrderingAsAdded(false);
        
        // add for percentage
      AuroraSwitchPreference batterrPercentPrf = new AuroraSwitchPreference(getActivity());
      batterrPercentPrf.setTitle(getString(R.string.battery_percent));
      batterrPercentPrf.setKey(KEY_BATTERY_PERCENTAGE);
      batterrPercentPrf.setOnPreferenceChangeListener(this);
      batterrPercentPrf.setOrder(-3);
      
      final boolean enable = Settings.Secure.getInt(getActivity().getContentResolver(),
              BATTERY_PERCENTAGE, 0) != 0;
      
      batterrPercentPrf.setChecked(enable);        
      mAppListGroup.addPreference(batterrPercentPrf);
      //end 

        mBatteryStatusPref.setOrder(-2);
        mBatteryStatusPref.setSelectable(false);
        mAppListGroup.addPreference(mBatteryStatusPref);
		// qy modify
       // BatteryHistoryPreference hist = new BatteryHistoryPreference(
        //        getActivity(), mStatsHelper.getStats());
		 AuroraPreference hist = new  AuroraPreference(getActivity());
        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000,
                BatteryStats.STATS_SINCE_CHARGED);        
        String durationString = Utils.formatElapsedTime(getActivity(), uSecTime / 1000,true);        
        
        /*durationString = getActivity().getString(R.string.battery_stats_on_battery,
                durationString);*/
        
        hist.setTitle(getActivity().getString(R.string.battery_usage_hint));
        hist.setSummary(durationString);
        hist.setSelectable(false);
     // end


        hist.setOrder(-1);
        mAppListGroup.addPreference(hist);
        
        
        Log.i("qy", "PowerProfile.POWER_SCREEN_FULL = "+mStatsHelper.getPowerProfile().getAveragePower(
                PowerProfile.POWER_SCREEN_FULL));

        /*if (mStatsHelper.getPowerProfile().getAveragePower(
                PowerProfile.POWER_SCREEN_FULL) < 10) {
            addNotAvailableMessage();
            return;
        }*/
        mStatsHelper.refreshStats(false);
        List<BatterySipper> usageList = mStatsHelper.getUsageList();
		
		// qy add begin 2014 06 30 
        // sipper.name ==com.aurora.scanner.fileobserver  sipper.name ==android.process.media
        // sipper.name ==system
        for (BatterySipper sipper : usageList) {
        	if(sipper.name !=null && sipper.name.equals("com.aurora.scanner.fileobserver")){
        		sipperSortValue = sipper.getSortValue();
        		percentOfMaxMediaScanner = (sipperSortValue * 100) / mStatsHelper.getMaxPower();
        	    percentOfTotalMediaScanner = ((sipperSortValue /mStatsHelper.getTotalPower()) * 100);
        	}
        }
    	// qy add end 2014 06 30 

        int count = 0;

        for (BatterySipper sipper : usageList) {
            if (sipper.getSortValue() < MIN_POWER_THRESHOLD) continue;
            double percentOfTotal =
                    ((sipper.getSortValue() / mStatsHelper.getTotalPower()) * 100);
            if (percentOfTotal < 1) continue;
			if(sipper.name !=null && sipper.name.equals("com.aurora.scanner.fileobserver"))  continue; // qy add 2014 06 30
         //   PowerGaugePreference pref =
          //          new PowerGaugePreference(getActivity(), sipper.getIcon(), sipper);
				PowerGaugePreference pref = new PowerGaugePreference(getActivity(), sipper);

            double percentOfMax =
                    (sipper.getSortValue() * 100) / mStatsHelper.getMaxPower();
            
            pref.setTitle(sipper.name);
            pref.setOrder(Integer.MAX_VALUE - (int) sipper.getSortValue()); // Invert the order

			// qy add  begin 2014 06 30
            if(sipper.name !=null && sipper.name.equals("Android OS")){
            	percentOfTotal += percentOfTotalMediaScanner;
    			percentOfMax += percentOfMaxMediaScanner;
    			pref.setOrder(Integer.MAX_VALUE - (int) (sipper.getSortValue())-(int)sipperSortValue); // Invert the order
            }else if(sipper.name !=null && sipper.name.equals("android.process.media")){
            	pref.setTitle(R.string.media_volume_title);
            }else if(sipper.name !=null && sipper.name.equals("system")){
            	pref.setTitle(R.string.android_system_power_pref_title);
            }
            // end
			
			sipper.percent = percentOfTotal;
            pref.setPercent(percentOfMax, percentOfTotal);
            if (sipper.uidObj != null) {
                pref.setKey(Integer.toString(sipper.uidObj.getUid()));
            }
            count = count + 1;
            pref.setOrder(count);
            mAppListGroup.addPreference(pref);
            if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST+1)) break;
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BatteryStatsHelper.MSG_UPDATE_NAME_ICON:
                    BatterySipper bs = (BatterySipper) msg.obj;
                    PowerGaugePreference pgp =
                            (PowerGaugePreference) findPreference(
                                    Integer.toString(bs.uidObj.getUid()));
                    if (pgp != null) {
                        pgp.setIcon(bs.icon);
                        pgp.setTitle(bs.name);
                    }
                    break;
                case BatteryStatsHelper.MSG_REPORT_FULLY_DRAWN:
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.reportFullyDrawn();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

	@Override
	public boolean onPreferenceChange(AuroraPreference pref, Object newValue) {
		// TODO Auto-generated method stub
		if (KEY_BATTERY_PERCENTAGE.equals(pref.getKey())){
            /** M:   handle battery percentage checkbox @{ */
            Log.d(TAG, "click battery percentage checkbox");
            AuroraSwitchPreference prf = (AuroraSwitchPreference) pref;
            int state = ((Boolean)newValue) ? 1 : 0;
            Log.d(TAG, "state: " + state);
            Settings.Secure.putInt(getActivity().getContentResolver(), BATTERY_PERCENTAGE, state);
            // Post the intent
            Intent intent = new Intent(ACTION_BATTERY_PERCENTAGE_SWITCH);
            intent.putExtra("state", state);
            Log.d(TAG, "sendBroadcast battery percentage switch");
            getActivity().sendBroadcast(intent);
            /** @} */
        }
		return true;
	}
}
