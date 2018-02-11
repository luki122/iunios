package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import com.android.internal.app.IUsageStats;
import com.aurora.puremanager.R;
//import com.android.internal.os.PkgUsageStats;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.text.TextUtils;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
//import android.os.ServiceManager;
import android.util.Log;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import java.util.Calendar;

public class ApplicationsInfo {
    public final List<ItemInfo> mAppEntries = new ArrayList<ItemInfo>();
    public final Map<String, ItemInfo> mMapEntries = new HashMap<String, ItemInfo>();

    public final List<ItemInfo> mPermissionsAppEntries = new ArrayList<ItemInfo>();
    public final Map<String, ItemInfo> mPermissionsMapEntries = new HashMap<String, ItemInfo>();

    private static final Object LOCK = new Object();
    private static ApplicationsInfo sInstance;
    private static final String TAG = "ApplicationsInfo-->";
    private Context mContext;
    private boolean mReady = false;

    private HashMap<String, Long> mUninstallSortSize = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortSizeList;
    public final List<ItemInfo> mSortSizeAppEntries = new ArrayList<ItemInfo>();
    public final Map<String, ItemInfo> mSortSizeMapEntries = new HashMap<String, ItemInfo>();

    private HashMap<String, Long> mUninstallSortTime = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortTimeList;
    public final List<ItemInfo> mSortTimeAppEntries = new ArrayList<ItemInfo>();
    public final Map<String, ItemInfo> mSortTimeMapEntries = new HashMap<String, ItemInfo>();

    private HashMap<String, Long> mUninstallSortFrequency = new HashMap<String, Long>();
    private List<Map.Entry<String, Long>> mUninstallSortFrequencyList;
    public final List<ItemInfo> mSortFrequencyAppEntries = new ArrayList<ItemInfo>();
    public final Map<String, ItemInfo> mSortFrequencyMapEntries = new HashMap<String, ItemInfo>();

    private long mAppSize;
    private static long DAY_TIME = 24 * 60 * 60 * 1000;
    private static long MONTH_TIME = DAY_TIME * 30;

    public static ApplicationsInfo getInstance() {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new ApplicationsInfo();
            }
            return sInstance;
        }
    }

    public boolean isReady() {
        return mReady;
    }

    public void loadAppEntries(Context context) {
        // synchronized (mMapEntries) {
        DebugUtil.d(TAG, "loadAppEntries");
        hashMapClear();
        mContext = context.getApplicationContext();
        List<ApplicationInfo> applications = HelperUtils_per.getThirdApplicationInfo(context);
        for (int i = 0; i < applications.size(); i++) {
            ApplicationInfo info = applications.get(i);
            ItemInfo appInfo = mMapEntries.get(info.packageName);
            if (appInfo != null) {
                appInfo.mApplicationInfo = info;
            } else {
                appInfo = new ItemInfo(info);
                appInfo.setPackageName(info.packageName);
                appInfo.setName(HelperUtils_per.loadLabel(context, info));
                appInfo.setStauts(PermissionsInfo.STATUS_REQUEST); // initial value = 1
                appInfo.setCheckStatus(false);

                mUninstallSortSize.put(info.packageName, mAppSize);
                mUninstallSortTime.put(info.packageName, getFirstInstallTime(context, info.packageName));

                mAppEntries.add(appInfo);
                mMapEntries.put(info.packageName, appInfo);
            }
        }
        sortBySize();
        loadSortSizeAppEntries(mContext, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(mContext, mUninstallSortTimeList);

        getFrequentAppList(mContext);
    }

    private void hashMapClear() {
        mMapEntries.clear();
        mAppEntries.clear();
        mUninstallSortSize.clear();
        mUninstallSortTime.clear();
        mUninstallSortFrequency.clear();
    }

    private void sortBytime() {
        mUninstallSortTimeList = new ArrayList<Map.Entry<String, Long>>(mUninstallSortTime.entrySet());
        Collections.sort(mUninstallSortTimeList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry, Map.Entry<String, Long> secondMapEntry) {
                return firstMapEntry.getValue().compareTo(secondMapEntry.getValue());
            }
        });
    }

    private long getFirstInstallTime(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            Log.v(TAG, "Package-->" + packageName + " firstInstallTime = " + info.firstInstallTime);
            return info.firstInstallTime;

        } catch (Exception ex) {
            Log.d(TAG, "isRecentInstallPackage throw exption " + ex.getMessage());
        }
        return 0;
    }

    private void sortBySize() {
        mUninstallSortSizeList = new ArrayList<Map.Entry<String, Long>>(mUninstallSortSize.entrySet());
        Collections.sort(mUninstallSortSizeList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry, Map.Entry<String, Long> secondMapEntry) {
                return secondMapEntry.getValue().compareTo(firstMapEntry.getValue());
            }
        });
    }

    public void loadPermissionsAppEntries(Context context) {
        DebugUtil.d(TAG, "loadPermissionsEntries");
        mPermissionsAppEntries.clear();
        mPermissionsMapEntries.clear();
        mContext = context.getApplicationContext();
//        PackageManager pm = context.getPackageManager();
        // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
        String[] projection = new String[] {"packagename", "count (*) as permcount"};
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(PermissionsInfo.URI), projection,
                "1=1) group by (packagename", null, null);
        if(cursor == null){
        	return;
        }
        
        while (cursor.moveToNext()) {
            String packagename = cursor.getString(0);
            int permcount = cursor.getInt(1);
            try {
                ApplicationInfo info = context.getPackageManager().getApplicationInfo(packagename, 0);
                ItemInfo appInfo = mPermissionsMapEntries.get(packagename);
                if (appInfo != null) {
                    appInfo.mApplicationInfo = info;
                } else {
                    appInfo = new ItemInfo(info);
                    appInfo.setPackageName(info.packageName);
                    appInfo.setName(HelperUtils_per.loadLabel(context, info));
                    appInfo.setSummary(getPermissionCount(packagename, permcount));
                    mPermissionsAppEntries.add(appInfo);
                    mPermissionsMapEntries.put(packagename, appInfo);
                }
            } catch (Exception e) {
                Log.e(TAG, "loadPermissionsEntries getApplicationInfo error");
            }
        }
        cursor.close();
        mReady = true;
    }

    public void addPackage(Context context, String pkgName) {
        ItemInfo appInfo = mMapEntries.get(pkgName);
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES
                    | PackageManager.GET_DISABLED_COMPONENTS);
            if (appInfo == null) {
                appInfo = new ItemInfo(info);
                appInfo.setPackageName(info.packageName);
                appInfo.setName(HelperUtils_per.loadLabel(context, info));

                mUninstallSortSize.put(info.packageName, mAppSize);
                mUninstallSortTime.put(info.packageName, getFirstInstallTime(context, info.packageName));

                mAppEntries.add(appInfo);
                mMapEntries.put(pkgName, appInfo);
            } else {
                appInfo.mApplicationInfo = info;
//                appInfo.setMoving(false);
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sortBySize();
        loadSortSizeAppEntries(context, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(context, mUninstallSortTimeList);

        getFrequentAppList(context);
    }

    public void removePackage(Context context, String pkgName) {
        ItemInfo appInfo;
        appInfo = mMapEntries.get(pkgName);
        if (appInfo != null) {
            mAppEntries.remove(appInfo);
            mMapEntries.remove(pkgName);
        }

        appInfo = mSortSizeMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortSizeAppEntries.remove(appInfo);
            mSortSizeMapEntries.remove(pkgName);
        }

        appInfo = mSortTimeMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortTimeAppEntries.remove(appInfo);
            mSortTimeMapEntries.remove(pkgName);
        }

        appInfo = mSortFrequencyMapEntries.get(pkgName);
        if (appInfo != null) {
            mSortFrequencyAppEntries.remove(appInfo);
            mSortFrequencyMapEntries.remove(pkgName);
        }

        sortBySize();
        loadSortSizeAppEntries(context, mUninstallSortSizeList);

        sortBytime();
        loadSortTimeAppEntries(context, mUninstallSortTimeList);

        getFrequentAppList(context);

    }


    // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
    private String getPermissionCount(String pkgname, int permcount) {
        int permissionsCount = permcount;
        if (NfcAdapter.getDefaultAdapter(mContext) == null) {
            String[] projection = new String[] {"permission", "packagename"};
            Cursor cursor = mContext.getContentResolver()
                    .query(Uri.parse(PermissionsInfo.URI), projection,
                            "packagename = ? and permission = 'android.permission.NFC'",
                            new String[] {pkgname}, null);
            if (cursor != null && cursor.getCount() > 0) {
                permissionsCount--;
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return mContext.getResources().getString(R.string.app_permission_count, permissionsCount);
    }

    public void releaseRes() {
        if (mMapEntries != null) {
            mMapEntries.clear();
        }
        if (mAppEntries != null) {
            mAppEntries.clear();
        }
        mPermissionsMapEntries.clear();
        mPermissionsAppEntries.clear();
        sInstance = null;
    }

    class PackageStatsObserver extends IPackageStatsObserver.Stub {
        public boolean retValue = false;
        public PackageStats stats;
        private boolean doneFlag = false;

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            synchronized (this) {
                retValue = succeeded;
                stats = pStats;
                doneFlag = true;
                notifyAll();
            }
        }

        public boolean isDone() {
            return doneFlag;
        }
    }

    public static final long MAX_WAIT_TIME = 10 * 1000;
    public static final long WAIT_TIME_INCR = 10;

    public void getFrequentAppList(Context context) {

        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        List<ResolveInfo> thirdinfos = filterPackage(infos);
        if (thirdinfos.isEmpty()) {
            return;
        }
        //modify by gaoming start 20151203
//        UsageStatsManager usageStatsManager;
//        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USER_SERVICE);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);
//        final List<UsageStats> stats =
//        usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
//        cal.getTimeInMillis(), System.currentTimeMillis());
       /* if (stats == null) {
        
        }else{
            final int statCount = stats.size();
            
            for (ResolveInfo info : thirdinfos) {
                for (int i = 0; i < statCount; i++) {
                    final android.app.usage.UsageStats pkgStats = stats.get(i);
                    if((pkgStats.getPackageName()).equals(info.activityInfo.packageName)){
                        long lastUse = System.currentTimeMillis() - pkgStats.getLastTimeUsed();
                        mUninstallSortFrequency.put(info.activityInfo.packageName, lastUse);
                    }
                }
            }
        }*/
        //modify by gaoming end 20151203
		
        List<ApplicationInfo> applications = HelperUtils_per.getThirdApplicationInfo(context);

        for (int i = 0; i < applications.size(); i++) {
            ApplicationInfo info = applications.get(i);
            if (!mUninstallSortFrequency.containsKey(info.packageName)) {
                mUninstallSortFrequency.put(info.packageName, MONTH_TIME + 1);
            }
        }

        sortByFrequency();
        loadSortFrequencyAppEntries(context, mUninstallSortFrequencyList);

    }

    private void sortByFrequency() {
        mUninstallSortFrequencyList = new ArrayList<Map.Entry<String, Long>>(
                mUninstallSortFrequency.entrySet());
        Collections.sort(mUninstallSortFrequencyList, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> firstMapEntry, Map.Entry<String, Long> secondMapEntry) {
                return secondMapEntry.getValue().compareTo(firstMapEntry.getValue());
            }
        });
    }

    public void loadSortSizeAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortSizeAppEntries.clear();
        mSortSizeMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils_per.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || "".equalsIgnoreCase(info.packageName)
                    || !mUninstallSortSize.containsKey(info.packageName)) {
                continue;
            }
            ItemInfo appInfo = new ItemInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setName(HelperUtils_per.loadLabel(context, info));

            appInfo.setSummary(HelperUtils_per.getStringDate(getFirstInstallTime(context, info.packageName)));
            try {
                size = HelperUtils_per.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
            appInfo.setSize(size);
            mSortSizeAppEntries.add(appInfo);
            mSortSizeMapEntries.put(info.packageName, appInfo);
        }
    }

    public void loadSortFrequencyAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortFrequencyAppEntries.clear();
        mSortFrequencyMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils_per.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || !mUninstallSortSize.containsKey(info.packageName)
                    || mUninstallSortSize.get(info.packageName) == null) {
                continue;
            }
            ItemInfo appInfo = new ItemInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setName(HelperUtils_per.loadLabel(context, info));
            appInfo.setAppFrequency(list.get(i).getValue());
            try {
                size = HelperUtils_per.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
            appInfo.setSize(size);
            mSortFrequencyAppEntries.add(appInfo);
            mSortFrequencyMapEntries.put(info.packageName, appInfo);
        }
    }

    public void loadSortTimeAppEntries(Context context, List<Map.Entry<String, Long>> list) {
        String size;
        mSortTimeAppEntries.clear();
        mSortTimeMapEntries.clear();
        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo info;
            info = HelperUtils_per.getApplicationInfo(context, list.get(i).getKey());
            if (info == null || !mUninstallSortSize.containsKey(info.packageName)
                    || mUninstallSortSize.get(info.packageName) == null) {
                continue;
            }
            ItemInfo appInfo = new ItemInfo(info);
            appInfo.setPackageName(info.packageName);
            appInfo.setName(HelperUtils_per.loadLabel(context, info));
            appInfo.setSummary(HelperUtils_per.getStringDate(list.get(i).getValue()));
            try {
                size = HelperUtils_per.getSizeStr(context, mUninstallSortSize.get(info.packageName));
            } catch (Exception e) {
                Log.w(TAG, "Exception :" + e);
                size = " ";
            }
            appInfo.setSize(size);
            mSortTimeAppEntries.add(appInfo);
            mSortTimeMapEntries.put(info.packageName, appInfo);
        }
    }

    private List<ResolveInfo> filterPackage(List<ResolveInfo> allPackageList) {
        List<ResolveInfo> list = new ArrayList<ResolveInfo>();
        if (!allPackageList.isEmpty()) {
            list.clear();
            int size = allPackageList.size();
            ResolveInfo resolveInfo = null;
            for (int i = 0; i < size; i++) {
                resolveInfo = allPackageList.get(i);
                // 第三方应用
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    list.add(resolveInfo);
                }
            }
        }
        return list;
    }
}
