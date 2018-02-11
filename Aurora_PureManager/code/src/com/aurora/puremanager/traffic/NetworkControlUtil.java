package com.aurora.puremanager.traffic;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.SparseIntArray;

import com.aurora.puremanager.provider.open.FreezedAppProvider;

public class NetworkControlUtil {

	private final int SYSTEMID_CONSTANT = 10000;
	private ArrayList<NetworkAppInfo> mAppList = new ArrayList<NetworkAppInfo>();
	private final Map<String, NetworkAppInfo> mMapEntries = new HashMap<String, NetworkAppInfo>();
	private boolean isScan = true;
	private Context mContext;

	public void resetAppControl(final Context context) {
		new Thread() {
			@Override
			public void run() {
				getInternetAppList(context);
				ArrayList<NetworkAppInfo> appListInfo = getAppList(context);
				for (NetworkAppInfo appInfo : appListInfo) {
					int status = appInfo.getAppMobileStatus();

					SparseIntArray disableArray = NetworkDisableUids
							.getInstance().getDisableUids(0);
					int uid = appInfo.getAppUid();
					boolean isFlag = true;

					if (status == 0) {
						disableArray.put(uid, 1);
						isFlag = true;
					} else {
						disableArray.delete(uid);
						isFlag = false;
					}
					AppNetworkControl.getInstance(context)
							.reflectNetworkControlAction(appInfo.getAppUid(),
									0, isFlag);

					NetworkDisableUids.getInstance().resetDisableUidsRules(0,
							disableArray);
				}
			}
		}.start();
	}

	private ArrayList<NetworkAppInfo> getAppList(Context mContext) {
		ArrayList<NetworkAppInfo> appListInfo = new ArrayList<NetworkAppInfo>();
		appListInfo.addAll(mAppList);

		Collections.sort(appListInfo, new Comparator<NetworkAppInfo>() {
			@Override
			public int compare(NetworkAppInfo arg0, NetworkAppInfo arg1) {
				// TODO Auto-generated method stub
				return chineseCompare(arg0.getAppName(), arg1.getAppName());
			}
		});

		return appListInfo;
	}

	private void getInternetAppList(Context context) {
		PackageManager pkgManager = context.getPackageManager();
		sleep();
		List<ApplicationInfo> packages = getApplicationInfo(context);
		SparseIntArray mobileDisableUids = getDisableUids(Constant.MOBILE);
		SparseIntArray wifiDisableUids = getDisableUids(Constant.WIFI);

		if (packages.size() != mMapEntries.size()) {
			mMapEntries.clear();
			mAppList.clear();
		}

		for (ApplicationInfo packageInfo : packages) {

			NetworkAppInfo tmpInfo = mMapEntries.get(packageInfo.packageName);
			if (tmpInfo == null) {
				if (getUsesPermission(pkgManager, packageInfo.packageName))
					continue;
				if (packageInfo.uid < SYSTEMID_CONSTANT) {
					continue;
				}

				tmpInfo = new NetworkAppInfo();
				tmpInfo.setAppUid(packageInfo.uid);
				tmpInfo.setAppName(packageInfo.loadLabel(pkgManager).toString());
				tmpInfo.setAppPackageName(packageInfo.packageName);

				int mobileStatus = initStatus(mobileDisableUids,
						packageInfo.uid);
				tmpInfo.setAppMobileStatus(mobileStatus);

				int wifiStatus = initStatus(wifiDisableUids, packageInfo.uid);
				tmpInfo.setAppWifiStatus(wifiStatus);

				// networkControlInSavingService(mobileStatus, context,
				// packageInfo.packageName);

				HashSet<String> freezedApps = FreezedAppProvider
						.loadFreezedAppListInDB(mContext);

				if (!freezedApps.contains(packageInfo.packageName)) {
					mAppList.add(tmpInfo);
				}
				mMapEntries.put(packageInfo.packageName, tmpInfo);
			}
		}

		checkDisableUidsExist(mobileDisableUids, wifiDisableUids, mAppList);
	}

	private int initStatus(SparseIntArray disableUids, int uid) {
		if (disableUids == null) {
			return 1;
		}

		for (int i = 0; i < disableUids.size(); i++) {
			if (disableUids.keyAt(i) == uid) {
				return 0;
			}
		}

		return 1;
	}

	public List<ApplicationInfo> getApplicationInfo(Context context) {
		List<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
		List<ResolveInfo> resolves = getLauncherShowActivity(context);
		for (int i = 0; i < resolves.size(); i++) {
			ResolveInfo info = resolves.get(i);
			ApplicationInfo ai = getApplicationInfo(context,
					info.activityInfo.packageName);
			if (containApplications(applications, ai)) {
				continue;
			}
			applications.add(ai);
		}
		return applications;
	}

	public List<ResolveInfo> getLauncherShowActivity(Context context) {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		return context.getPackageManager().queryIntentActivities(mainIntent, 0);
	}

	public ApplicationInfo getApplicationInfo(Context context, String pkgName) {
		ApplicationInfo result = null;
		try {
			result = context.getPackageManager().getApplicationInfo(pkgName, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private boolean containApplications(List<ApplicationInfo> applications,
			ApplicationInfo ai) {
		for (ApplicationInfo appInfo : applications) {
			if (ai.packageName.equals(appInfo.packageName)) {
				return true;
			}
		}
		return false;
	}

	private boolean getUsesPermission(PackageManager pkgManager,
			String packageName) {
		if (PackageManager.PERMISSION_GRANTED != pkgManager.checkPermission(
				Manifest.permission.INTERNET, packageName)) {
			return true;
		}
		return false;
	}

	private void sleep() {
		while (true) {
			if (isScan) {
				break;
			}
			try {
				Thread.sleep(1);
			} catch (Exception ex) {

			}
		}
	}

	private void checkDisableUidsExist(SparseIntArray mDisUids,
			SparseIntArray wDisUids, ArrayList<NetworkAppInfo> appList) {
		for (int mId = 0; mId < mDisUids.size(); mId++) {
			int uid = mDisUids.keyAt(mId);
			if (!isAppExist(uid, appList)) {
				mDisUids.delete(uid);
			}
		}
		NetworkDisableUids.getInstance().resetDisableUidsRules(Constant.MOBILE,
				mDisUids);

		for (int wId = 0; wId < wDisUids.size(); wId++) {
			int uid = wDisUids.keyAt(wId);
			if (!isAppExist(uid, appList)) {
				wDisUids.delete(uid);
			}
		}
		NetworkDisableUids.getInstance().resetDisableUidsRules(Constant.WIFI,
				wDisUids);
	}

	private int chineseCompare(Object _oChinese1, Object _oChinese2) {
		return Collator.getInstance(Locale.CHINESE).compare(_oChinese1,
				_oChinese2);
	}

	private SparseIntArray getDisableUids(int networkType) {
		return NetworkDisableUids.getInstance().getDisableUids(networkType);
	}

	private boolean isAppExist(int uid, ArrayList<NetworkAppInfo> appList) {
		boolean isExist = false;
		for (int i = 0; i < appList.size(); i++) {
			if (uid == appList.get(i).getAppUid()) {
				isExist = true;
			}
		}
		return isExist;
	}

}
