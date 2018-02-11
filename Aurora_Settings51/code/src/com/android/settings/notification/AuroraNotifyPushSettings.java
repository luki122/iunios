package com.android.settings.notification;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.INotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import com.aurora.utils.DensityUtil;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.settings.R;
import com.android.settings.notification.NotificationAppList.AppRow;

public class AuroraNotifyPushSettings extends AuroraSettingsPreferenceFragment
		implements OnPreferenceChangeListener {

	private static final String TAG = "AuroraNotifyPushSettings";
	private final String PREF_APP_ON_BOX = "pref_app_on_box";
    private static final String ACTION_NETWORKS_SPEED = "action_isdisplay_network_speed";
	private static final String DISPLAY = "isdisplay";
	private final String DISPLAY_NETWORK_SPEED = "display_network_speed";
	private static final String TABLE_NETWORK_DISPLAY = "isdisplay_network_speed";

	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private AuroraSwitchPreference mDisplay_net_Speed;
	private AuroraPreferenceCategory mAppOnBox;
	private PackageManager mPm;
	private final Handler mHandler = new Handler();
	private List<PackageInfo> mPkgList = new ArrayList<PackageInfo>();
	private List<PushPkgInfo> mPushPkgList = new ArrayList<PushPkgInfo>();
	private Drawable appIcon;

	private INotificationManager sINM;

	private static final Comparator<PushPkgInfo> mPushComparator = new Comparator<PushPkgInfo>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(PushPkgInfo lhs, PushPkgInfo rhs) {
			return sCollator.compare(lhs.label, rhs.label);
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.aurora_notify_push_settings);
		mAppOnBox = (AuroraPreferenceCategory) findPreference(PREF_APP_ON_BOX);
		mPm = getPackageManager();
		sINM = INotificationManager.Stub.asInterface(ServiceManager
				.getService(Context.NOTIFICATION_SERVICE));

		sharedPreferences = getActivity().getSharedPreferences(
				TABLE_NETWORK_DISPLAY,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		editor = sharedPreferences.edit();
		
		mDisplay_net_Speed = (AuroraSwitchPreference) findPreference(DISPLAY_NETWORK_SPEED);
		mDisplay_net_Speed.setChecked(sharedPreferences.getBoolean(DISPLAY,
				false));
		mDisplay_net_Speed
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(
							AuroraPreference preference, Object check) {
						boolean checked = ((Boolean) check).booleanValue();
						Log.i("pgd", "onPreferenceChange ischecked : "
								+ checked);
						if (getActivity() == null) {
							return false;
						}
						Intent intent = new Intent(ACTION_NETWORKS_SPEED);
						intent.putExtra(DISPLAY, checked);
						editor.putBoolean(DISPLAY, checked);
						editor.commit();
						getActivity().sendBroadcast(intent);
						return true;
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		loadThirdAppsList();
	}

	private void loadThirdAppsList() {
		AsyncTask.execute(mCollectAppsRunnable);
	}

	class PushPkgInfo {
		PackageInfo pkgInfo;
		String label;
	}

	private final Runnable mCollectAppsRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mAppOnBox) {
				mPkgList = mPm.getInstalledPackages(0);
				mPushPkgList.clear();
				for (int i = 0; i < mPkgList.size(); i++) {
					PackageInfo info = mPkgList.get(i);
					if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						PushPkgInfo pushInfo = new PushPkgInfo();
						pushInfo.pkgInfo = info;
						pushInfo.label = info.applicationInfo.loadLabel(mPm)
								.toString();
						mPushPkgList.add(pushInfo);
					}
				}
				Collections.sort(mPushPkgList, mPushComparator);
				mHandler.post(mRefreshPushAppsRunnable);
			}

		}
	};

	private final Runnable mRefreshPushAppsRunnable = new Runnable() {
		@Override
		public void run() {
			refreshDisplayedItems();
		}
	};

	private void refreshDisplayedItems() {
		mAppOnBox.removeAll();
		synchronized (mPushPkgList) {
			if (mPushPkgList.size() <= 0) {
				mAppOnBox.setTitle(R.string.pref_push_noapp_summary);
				return;
			}
			mAppOnBox.setTitle(R.string.pref_push_summary);
			Log.v(TAG,
					"------refreshDisplayedItems--------------mPushPkgList.size()------------==="
							+ mPushPkgList.size());

			for (int i = 0; i < mPushPkgList.size(); i++) {
				PackageInfo info = mPushPkgList.get(i).pkgInfo;
				AuroraSwitchPreference switchPre = new AuroraSwitchPreference(
						getActivity());
				boolean checked = getNotificationsBanned(info.packageName,
						info.applicationInfo.uid);
				Log.v(TAG,
						"------refreshDisplayedItems--------------checked------------==="
								+ checked);
				switchPre.setChecked(checked);
				switchPre.setKey(info.applicationInfo.packageName);
				int px = DensityUtil.dip2px(getActivity(), 48);
				switchPre.setIconSize(px, px);
				switchPre.setIcon(appIcon);
				switchPre.setTitle(info.applicationInfo.loadLabel(mPm)
						.toString());
				switchPre
						.setOnPreferenceChangeListener(AuroraNotifyPushSettings.this);

				mAppOnBox.addPreference(switchPre);
			}

		}
	}


	@Override
	public boolean onPreferenceChange(AuroraPreference preference,
			Object newValue) {
		// TODO Auto-generated method stub
		if (Utils.isMonkeyRunning()) {
			return true;
		}
		boolean checked = ((Boolean) newValue).booleanValue();
		Log.v(TAG,
				"------onPreferenceChange--------------checked------------==="
						+ checked);

		ApplicationInfo applicationInfo = getApplicationInfo(getActivity(),
				preference.getKey());
		if (applicationInfo != null) {
			setNotificationsBanned(preference.getKey(), applicationInfo.uid,
					checked);
		}
		return true;
	}

	public static synchronized ApplicationInfo getApplicationInfo(
			Context context, String packageName) {
		if (context == null || packageName == null) {
			return null;
		}
		ApplicationInfo appinfo = null;
		try {
			appinfo = context.getPackageManager()
					.getPackageInfo(packageName, 0).applicationInfo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appinfo;
	}

	public boolean getNotificationsBanned(String pkg, int uid) {
		try {
			final boolean enabled = sINM.areNotificationsEnabledForPackage(pkg,
					uid);
			return enabled;
		} catch (Exception e) {
			Log.w(TAG, "Error calling NoMan", e);
			return false;
		}
	}

	public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
		try {
			sINM.setNotificationsEnabledForPackage(pkg, uid, banned);
			return true;
		} catch (Exception e) {
			Log.w(TAG, "Error calling NoMan", e);
			return false;
		}
	}

}