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

package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.notification.NotificationAppList.AppRow;
import com.android.settings.notification.NotificationAppList.Backend;
import com.aurora.utils.Utils2Icon;
import android.graphics.drawable.Drawable;

import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageStats;

/** These settings are per app, so should not be returned in global search results. */
public class AuroraAppNotificationSettings extends AuroraSettingsPreferenceFragment {
    private static final String TAG = "AuroraAppNotificationSettings";
    private static final boolean DEBUG =true;// Log.isLoggable(TAG, Log.DEBUG);

    private static final String KEY_APP_DETAIL = "app_detail";
    private static final String KEY_TOTAL_SIZE = "app_total_size";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_SENSITIVE = "sensitive";

    static final String EXTRA_HAS_SETTINGS_INTENT = "has_settings_intent";
    static final String EXTRA_SETTINGS_INTENT = "settings_intent";

    private final Backend mBackend = new Backend();

    private Context mContext;
    private AuroraAppDetailPreference mAppDetail;
    private AuroraPreferenceCategory mTotalCategory;
    private AuroraSwitchPreference mBlock;
    private AuroraSwitchPreference mPriority;
    private AuroraSwitchPreference mSensitive;
    private AppRow mAppRow;
    private boolean mCreated;

    private PackageManager mPm;
    long cachesize ; 
	long datasize ;  
	long codesize   ;  
	long totalsize  ;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onActivityCreated mCreated=" + mCreated);
        if (mCreated) {
            Log.w(TAG, "onActivityCreated: ignoring duplicate call");
            return;
        }
        mCreated = true;
        if (mAppRow == null) return;
        final View content = getActivity().findViewById(R.id.main_content);
        final ViewGroup contentParent = (ViewGroup) content.getParent();
        final View bar = getActivity().getLayoutInflater().inflate(R.layout.app_notification_header,
                contentParent, false);

        final ImageView appIcon = (ImageView) bar.findViewById(R.id.app_icon);
        appIcon.setImageDrawable(mAppRow.icon);

        final TextView appName = (TextView) bar.findViewById(R.id.app_name);
        appName.setText(mAppRow.label);

        final View appSettings = bar.findViewById(R.id.app_settings);
        if (mAppRow.settingsIntent == null) {
            appSettings.setVisibility(View.GONE);
        } else {
            appSettings.setClickable(true);
            appSettings.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(mAppRow.settingsIntent);
                }
            });
        }
      //  contentParent.addView(bar, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Intent intent = getActivity().getIntent();
        if (DEBUG) Log.d(TAG, "onCreate getIntent()=" + intent);
        if (intent == null) {
            Log.w(TAG, "No intent");
            toastAndFinish();
            return;
        }

        final int uid = intent.getIntExtra(Settings.EXTRA_APP_UID, -1);
        final String pkg = intent.getStringExtra(Settings.EXTRA_APP_PACKAGE);
        if (uid == -1 || TextUtils.isEmpty(pkg)) {
            Log.w(TAG, "Missing extras: " + Settings.EXTRA_APP_PACKAGE + " was " + pkg + ", "
                    + Settings.EXTRA_APP_UID + " was " + uid);
            toastAndFinish();
            return;
        }

        if (DEBUG) Log.d(TAG, "Load details for pkg=" + pkg + " uid=" + uid);
        final PackageManager pm = getPackageManager();
        final PackageInfo info = findPackageInfo(pm, pkg, uid);
        if (info == null) {
            Log.w(TAG, "Failed to find package info: " + Settings.EXTRA_APP_PACKAGE + " was " + pkg
                    + ", " + Settings.EXTRA_APP_UID + " was " + uid);
            toastAndFinish();
            return;
        }
        mPm = mContext.getPackageManager();
        addPreferencesFromResource(R.xml.app_notification_settings);
        mAppDetail = (AuroraAppDetailPreference)findPreference(KEY_APP_DETAIL);
        mTotalCategory = (AuroraPreferenceCategory)findPreference(KEY_TOTAL_SIZE);
        mBlock = (AuroraSwitchPreference) findPreference(KEY_BLOCK);
        mPriority = (AuroraSwitchPreference) findPreference(KEY_PRIORITY);
        mSensitive = (AuroraSwitchPreference) findPreference(KEY_SENSITIVE);

        final boolean secure = new LockPatternUtils(getActivity()).isSecure();
        final boolean enabled = getLockscreenNotificationsEnabled();
        final boolean allowPrivate = getLockscreenAllowPrivateNotifications();
        if (!secure || !enabled || !allowPrivate) {
            getPreferenceScreen().removePreference(mSensitive);
        }

        mAppRow = NotificationAppList.loadAppRow(pm, info.applicationInfo, mBackend);
        /*aurora, linchunhui 统一系统应用图标显示 20150819 begin*/
        Drawable mIcon = Utils2Icon.getInstance(mContext).getIconDrawable(info.applicationInfo.packageName, Utils2Icon.INTER_SHADOW);
        if (mIcon != null) {
            mAppRow.icon = mIcon;
        }
        /*aurora, linchunhui 统一系统应用图标显示 20150819 end*/
        if (intent.hasExtra(EXTRA_HAS_SETTINGS_INTENT)) {
            // use settings intent from extra
            if (intent.getBooleanExtra(EXTRA_HAS_SETTINGS_INTENT, false)) {
                mAppRow.settingsIntent = intent.getParcelableExtra(EXTRA_SETTINGS_INTENT);
            }
        } else {
            // load settings intent
            ArrayMap<String, AppRow> rows = new ArrayMap<String, AppRow>();
            rows.put(mAppRow.pkg, mAppRow);
            NotificationAppList.collectConfigActivities(getPackageManager(), rows);
        }
        mPm.getPackageSizeInfo(mAppRow.pkg, new PkgSizeObserver());
        
        mAppDetail.setAppName(mAppRow.label);
        mAppDetail.setBackgroundResource(android.R.color.transparent);
        mAppDetail.setIcon(mAppRow.icon);
       
    	String versionStr =Utils.getApkVersion(getActivity(),mAppRow.pkg);
    	if(versionStr == null || versionStr.equals("") || versionStr.equals("null")){
    		versionStr = "1.0";
		}
    	versionStr = getResources().getString(R.string.software_version) + versionStr;
    	mAppDetail.setVersion(versionStr);
        
    	//getPreferenceScreen().removePreference(mPriority); //aurora linchunhui 20150827
        mBlock.setChecked(!mAppRow.banned);
        mPriority.setChecked(mAppRow.priority);
        if (mSensitive != null) {
            mSensitive.setChecked(!mAppRow.sensitive);
        }

        mBlock.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
                final boolean block = (Boolean) newValue;
                return mBackend.setNotificationsBanned(pkg, uid, !block);
            }
        });

        mPriority.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
                final boolean priority = (Boolean) newValue;
                return mBackend.setHighPriority(pkg, uid, priority);
            }
        });

        if (mSensitive != null) {
            mSensitive.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
                    final boolean sensitive = (Boolean) newValue;
                    return mBackend.setSensitive(pkg, uid, !sensitive);
                }
            });
        }

        // Users cannot block notifications from system/signature packages
        if (Utils.isSystemPackage(pm, info)) {
            getPreferenceScreen().removePreference(mBlock);
            mPriority.setDependency(null); // don't have it depend on a preference that's gone
        }
    }

    private boolean getLockscreenNotificationsEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0) != 0;
    }

    private boolean getLockscreenAllowPrivateNotifications() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 0) != 0;
    }

    private void toastAndFinish() {
        Toast.makeText(mContext, R.string.app_not_found_dlg_text, Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }

    private static PackageInfo findPackageInfo(PackageManager pm, String pkg, int uid) {
        final String[] packages = pm.getPackagesForUid(uid);
        if (packages != null && pkg != null) {
            final int N = packages.length;
            for (int i = 0; i < N; i++) {
                final String p = packages[i];
                if (pkg.equals(p)) {
                    try {
                        return pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "Failed to load package " + pkg, e);
                    }
                }
            }
        }
        return null;
    }
 
    /**
     * 流程参考应用管理
     * @param context
     * @return
     */
    
    private class PkgSizeObserver extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean arg1)
				throws RemoteException {
			// TODO Auto-generated method stub
			dealPackageStats(pStats);
			cachesize = pStats.cacheSize+pStats.externalCacheSize; //缓存大小
			datasize = pStats.dataSize+pStats.externalDataSize;  //应用程序大小
			codesize =	pStats.codeSize+pStats.externalCodeSize;  //数据大小
			totalsize = cachesize + datasize + codesize ;
	       String	totalStr = getResources().getString(R.string.app_total_size) + Utils.dealMemorySize(getActivity(), totalsize);;
	       if(mTotalCategory  != null){
	    	   mTotalCategory.setTitle(totalStr);
	       }
		}
    	
    }
    
    public static synchronized void dealPackageStats(PackageStats pStats){
    	if(pStats == null){
    		return ;
    	}
    	
    	if(pStats.cacheSize+pStats.externalCacheSize == 12*1024){//1024 = 1kb
			pStats.cacheSize = 0;
			pStats.externalCacheSize  = 0;
		}
    }
    
}
