package com.aurora.puremanager.utils;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.view.inputmethod.InputMethodInfo;

import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.DefMrgSoftIntent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by joy on 12/24/15.
 */
public class SuperPowerSaverUtil {
    private static final String TAG = "SuperPowerSaveUtils";
    private final SharedPreferences mDefaultLauncherPkgnameSharedPreferences;
    private Context mContext;
    public String mDefaultLauncherPkgname;
    private LinkedList<String> mList = new LinkedList<String>();
    private List<String> mWhiteList;
    public static String mDefaultInputMethodPkgName;
    private String mDefaultLauncherActivityname;
    private PackageManager mPm;
    private ActivityManager manager;

    public SuperPowerSaverUtil(Context mContext) {
        this.mContext = mContext;
        mPm = mContext.getPackageManager();
        manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mDefaultLauncherPkgnameSharedPreferences = mContext.getSharedPreferences(Consts.DEFAULT_LAUNCHER_PKG_NAME,
                Context.MODE_MULTI_PROCESS);
        List<String> superWhitelist = HelperUtils.getPowerSaveWhiteList(mContext);
        String[] whitearray = (String[]) superWhitelist.toArray(new String[0]);
        mWhiteList = Arrays.asList(whitearray);
        mWhiteList = new ArrayList<String>(mWhiteList);
        addDefaultAppToWhite(mContext);
        initMainHandler();
    }

    private void addDefaultAppToWhite(Context mContext) {
        //添加默认输入法
        mWhiteList.add(ApkUtils.getCurrentInputMethodId(mContext).split("/")[0]);
        //添加默认短信
        Intent mmsIntent = DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_MMS);
        ResolveInfo matchResolveInfo = mContext.getPackageManager().resolveActivity(
                mmsIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (matchResolveInfo == null ||
                matchResolveInfo.activityInfo == null) {
            return;
        }
        ActivityInfo matchActivityInfo = matchResolveInfo.activityInfo;
        mWhiteList.add(matchActivityInfo.packageName);
    }

    public void intoSuperPowerSaveMode() {
        mList.clear();
        //processDisableComponent(mContext);
        //getDefaultLauncherPkgname();
        saveDefaultLancherPkgname();
        getDefaultInputMethodInfo();
        StateController tmpController = new StateController(mContext);
        tmpController.setWifiState(false);
        tmpController.setWifiApState(false);
        //tmpController.setAdbState(false);
        tmpController.setDataConnectionState(false);
        tmpController.setBluetoothState(false);
        tmpController.setGpsState(false);
        tmpController.setNetDisplayStatus(false);
    }

    public void intoSuperPowerSaveMode2() {
        unFreezePowerSaveLauncher();
        freezeSystemLauncher();
        freezeOtherApps();
        saveObject();
        killCurrentProcesses(mContext);
        //setLauncher(Consts.POWER_SAVE_LAUNCHER_PKG_NAME, Consts.POWER_SAVE_LAUNCHER_ACITVITY_NAME);
        mHandler.sendEmptyMessage(EVENT_INTO_POWERSAVE_LANCHER);
    }

    public void outSuperPowerSaveMode() {
        if (getObject()) {
            unFreezeApps();
            getSaveDefaultLauncherPkgname();
            setLauncher(mDefaultLauncherPkgname, mDefaultLauncherActivityname);
            freezePowerSaveLauncher();
            setLauncher(mDefaultLauncherPkgname, mDefaultLauncherActivityname);
        }
    }

    private void saveObject() {
        try {
            File objFile = new File(Consts.OBJ_PTAH);
            if (objFile.exists() && objFile.delete()) {
                ;
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Consts.OBJ_PTAH));
            out.writeObject(mList);
            out.flush();
            out.close();
            Log.i(TAG, "saveObject mList.size() --------> " + mList.size());
        } catch (Exception e) {
            Log.e(TAG, "saveObject-------->", e);
        }
    }

    private boolean getObject() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(Consts.OBJ_PTAH));
            mList = (LinkedList<String>) in.readObject();
            in.close();
            Log.i(TAG, "getObject mList.size() --------> " + mList.size());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "getObject-------->", e);
            return false;
        }
    }

    private void saveDefaultLancherPkgname() {
        ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
        ComponentName currentDefaultHome = mPm.getHomeActivities(homeActivities);
        if (currentDefaultHome == null) {
            mDefaultLauncherPkgname = Consts.POWER_SAVE_LAUNCHER_PKG_NAME;
            mDefaultLauncherActivityname = Consts.POWER_SAVE_LAUNCHER_ACITVITY_NAME;
        } else {
            mDefaultLauncherPkgname = currentDefaultHome.getPackageName();
            if (mDefaultLauncherPkgname == null) {
                mDefaultLauncherPkgname = Consts.POWER_SAVE_LAUNCHER_PKG_NAME;
                mDefaultLauncherActivityname = Consts.POWER_SAVE_LAUNCHER_ACITVITY_NAME;
            } else {
                mDefaultLauncherActivityname = currentDefaultHome.getClassName();
            }
        }
        mDefaultLauncherPkgnameSharedPreferences.edit().
                putString(Consts.DEFAULT_LAUNCHER_PKG_NAME, mDefaultLauncherPkgname).
                commit();
        mDefaultLauncherPkgnameSharedPreferences.edit().
                putString(Consts.DEFAULT_LAUNCHER_ACTIVITY_NAME, mDefaultLauncherActivityname).
                commit();
    }

    private void getSaveDefaultLauncherPkgname() {
        mDefaultLauncherPkgname = mDefaultLauncherPkgnameSharedPreferences.getString(
                Consts.DEFAULT_LAUNCHER_PKG_NAME, null);
        mDefaultLauncherActivityname = mDefaultLauncherPkgnameSharedPreferences.getString(
                Consts.DEFAULT_LAUNCHER_ACTIVITY_NAME, null);
    }

    private void enterSaveLauncher() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);

        mContext.startActivity(home);
    }

    private void freezeAllLauncher() {
        List<ResolveInfo> riLists = mPm.queryIntentActivities(getLauncherIntent(),
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        int n = riLists.size();
        for (int i = 0; i < n; i++) {
            ResolveInfo r0 = riLists.get(i);
            for (int j = 1; j < n; j++) {
                ResolveInfo riInfo = riLists.get(j);
                if (r0.priority != riInfo.priority || r0.isDefault != riInfo.isDefault) {
                    while (j < n) {
                        riLists.remove(j);
                        n--;
                    }
                }
            }
        }
    }

    private void getDefaultLauncherPkgname() {
        ResolveInfo best = mPm.resolveActivity(getLauncherIntent(),
                PackageManager.MATCH_DEFAULT_ONLY);

        if (best != null && best.activityInfo != null) {
            if (best.activityInfo.packageName.equals("android")) {
                best = null;
            }
        } else {
            best = null;
        }

        List<ResolveInfo> riLists = mPm.queryIntentActivities(getLauncherIntent(),
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        int n = riLists.size();
        for (int i = 0; i < n; i++) {
            ResolveInfo r0 = riLists.get(i);
            for (int j = 1; j < n; j++) {
                ResolveInfo riInfo = riLists.get(j);
                if (r0.priority != riInfo.priority || r0.isDefault != riInfo.isDefault) {
                    while (j < n) {
                        riLists.remove(j);
                        n--;
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            ResolveInfo riInfo = riLists.get(i);
            if (best != null && best.activityInfo.packageName.equals(riInfo.activityInfo.packageName)) {
                mDefaultLauncherPkgname = best.activityInfo.packageName;
            }
        }
    }


    private void getDefaultInputMethodInfo() {
        InputMethodInfo info = HelperUtils.getDefInputMethod(mContext);
        if (info != null) {
            mDefaultInputMethodPkgName = info.getPackageName();
        }
    }

    private List<ApplicationInfo> getApplicationInfo(Context context) {
        List<ApplicationInfo> mApplications = mPm.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
        for (int i = 0; i < mApplications.size(); i++) {
            final ApplicationInfo info = mApplications.get(i);
            if (!info.enabled && info.enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || info.packageName.equals("android")) {
                mApplications.remove(i);
                i--;
                continue;
            }
        }
        return mApplications;
    }

    private void freezePowerSaveLauncher() {
        try {
            mPm.setApplicationEnabledSetting(Consts.POWER_SAVE_LAUNCHER_PKG_NAME,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (Exception e) {
            Log.e(TAG, "exc1:", e);
        }
    }

    private void unFreezePowerSaveLauncher() {
        try {
            mPm.setApplicationEnabledSetting(Consts.POWER_SAVE_LAUNCHER_PKG_NAME,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        } catch (Exception e) {
            Log.e(TAG, "e1:" + e);
        }
    }

    private void freezeSystemLauncher() {
        /*Log.e(TAG, "freezeSystemLauncher");
        if (Consts.AURORA_LAUNCHER_PKG_NAME.equals(mDefaultLauncherPkgname)) {
            Log.e(TAG, "freeze Aurora Launcher");
            freezeAuroraLauncher();
        } else {
            Log.e(TAG, "freeze other default Launcher");
            freezeAuroraLauncher();
            if (mDefaultLauncherPkgname != null
                    && !mDefaultLauncherPkgname.equalsIgnoreCase(Consts.POWER_SAVE_LAUNCHER_PKG_NAME)) {
                freezeOtherDefaultLauncher();
            }
        }*/

        setLauncher(Consts.POWER_SAVE_LAUNCHER_PKG_NAME, Consts.POWER_SAVE_LAUNCHER_ACITVITY_NAME);
    }

    public void freezeAuroraLauncher() {
        freezeApp(Consts.AURORA_LAUNCHER_PKG_NAME);
        mList.addFirst(Consts.AURORA_LAUNCHER_PKG_NAME);
    }

    private void freezeOtherDefaultLauncher() {
        freezeApp(mDefaultLauncherPkgname);
        mList.addFirst(mDefaultLauncherPkgname);
    }

    private void freezeOtherApps() {
        List<ApplicationInfo> applicationInfos = getApplicationInfo(mContext);
        for (ApplicationInfo appInfo : applicationInfos) {
            String pkgName = appInfo.packageName;
            if (!mWhiteList.contains(pkgName)) {
                if (!pkgName.equalsIgnoreCase(mDefaultInputMethodPkgName)) {
                    if (freezeApp(pkgName)) {
                        mList.addFirst(pkgName);
                    }
                }
            }
        }
    }

    private boolean freezeApp(String pkgName) {
        AutoStartData autoStartData = AutoStartModel.getInstance(mContext).
                getAutoStartData(pkgName);
        if (autoStartData != null && !autoStartData.getIsOpen()) {
            AutoStartModel.getInstance(mContext).
                    freezeAutoStart(pkgName);
            return true;
        } else {
            return false;
        }
    }

    private void unFreezeApps() {
        while (!mList.isEmpty()) {
            String pkgName = mList.getFirst();
            AutoStartModel.getInstance(mContext).
                    unFreezeAutoStart(pkgName);
            mList.removeFirst();
        }
    }

    private void forceFreezeApp(String pkgName, int pid) {
        try {
            //mContext.getPackageManager().setApplicationEnabledSetting(pkgName,
            //       PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            //manager.forceStopPackage(pkgName);
            //ExecuteAsRoot.killBackgroundProcesses(pid);
            mPm.setApplicationEnabledSetting(pkgName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void killCurrentProcesses(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        int count = processes.size();
        for (int i = 0; i < count; i++) {
            ActivityManager.RunningAppProcessInfo process = processes.get(i);
            if (process.processName.contains(":")) {
                String pkgName = process.processName.split(":")[0];
                if (mWhiteList.contains(pkgName)) {
                    Log.e(TAG, process.processName + " is mWhiteList");
                } else {
                    Log.e(TAG, "kill sub process " + process.processName);
                    ExecuteAsRoot.killBackgroundProcesses(process.pid);
                    manager.forceStopPackage(pkgName);
                    checkForceStop(process.processName);
                }
                processes.remove(pkgName);
                processes.remove(process.processName);
            }
        }
        count = processes.size();
        for (int i = 0; i < count; i++) {
            ActivityManager.RunningAppProcessInfo process = processes.get(i);
            Log.e(TAG, "running process name: " + process.processName);
            if (mWhiteList.contains(process.processName)) {
                Log.e(TAG, process.processName + " is mWhiteList");
            } else {
                if (getApplicationInfo(mContext, process.processName) == null) {
                    Log.e(TAG, "ignore fake app: " + process.processName);
                    mWhiteList.add(process.processName);
                    ExecuteAsRoot.killBackgroundProcesses(process.pid);
                    continue;
                }
                Log.e(TAG, "kill " + process.processName);
                manager.forceStopPackage(process.processName);
                checkForceStop(process.processName);
            }
        }
    }

    public static synchronized ApplicationInfo getApplicationInfo(Context context, String packageName) {
        if (context == null || StringUtils.isEmpty(packageName)) {
            return null;
        }
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appinfo;
    }


    private void checkForceStop(String pkgName) {
        Log.e(TAG, "checkForceStop " + pkgName);
        DevicePolicyManager mDpm;
        mDpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ApplicationInfo applicationInfo = getApplicationInfo(mContext, pkgName);

        if (applicationInfo == null) {
            Log.e(TAG, "applicationInfo is null: " + pkgName);
            mWhiteList.add(pkgName);
            return;
        }
        if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
            Log.e(TAG, "ApplicationInfo.FLAG_STOPPED");
            return;
        } else {
            Log.e(TAG, "not stopped pkg " + pkgName);

            /*Intent killIntent = new Intent(StateController.EVENT_FORCESTOP_PKG);
            killIntent.putExtra(StateController.EVENT_FORCESTOP_KEY, pkgName);
            mContext.sendBroadcast(killIntent);
            mWhiteList.add(pkgName);
            mList.add(pkgName);*/
        }
    }


    private Intent getLauncherIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return intent;
    }

    private void setDefaultLauncher() {
        Log.e(TAG, "setDefaultLauncher start");
        int n;
        if (mDefaultLauncherPkgname == null) {
            mDefaultLauncherPkgname = Consts.AURORA_LAUNCHER_PKG_NAME;
        }
        ResolveInfo defaultresolveinfo = null;
        Intent intent = getLauncherIntent();
        List<ResolveInfo> riLists = mPm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY
                | PackageManager.GET_RESOLVED_FILTER);
        n = riLists.size();
        Log.e(TAG, "setDefaultLauncher#n " + n);
        for (int i = 0; i < n; i++) {
            ResolveInfo ri = riLists.get(i);

            if (mDefaultLauncherPkgname.equals(ri.activityInfo.packageName)) {
                Log.e(TAG, "setDefaultLauncher# " + ri.activityInfo.packageName);
                defaultresolveinfo = ri;
                break;
            }
        }

        if (defaultresolveinfo == null) {
            Log.e(TAG, "setDefaultLauncher# " + "defaultresolveinfo == null");
            return;
        }

        IntentFilter filter = new IntentFilter();
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        ActivityInfo ai = defaultresolveinfo.activityInfo;
        intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
        if (intent.getAction() != null) {
            filter.addAction(intent.getAction());
        }
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String cat : categories) {
                filter.addCategory(cat);
            }
        }
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        int cat = defaultresolveinfo.match & IntentFilter.MATCH_CATEGORY_MASK;
        Uri data = intent.getData();
        if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
            String mimeType = intent.resolveType(mContext);
            if (mimeType != null) {
                try {
                    filter.addDataType(mimeType);
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    // Log.v("ResolverActivity: ", "" + e);
                    Log.v(TAG, "ResolverActivity: " + e);
                    filter = null;
                }
            }
        }
        if (data != null && data.getScheme() != null) {
            if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                    || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme()))) {
                filter.addDataScheme(data.getScheme());
                Iterator<IntentFilter.AuthorityEntry> aIt = defaultresolveinfo.filter.authoritiesIterator();
                if (aIt != null) {
                    while (aIt.hasNext()) {
                        IntentFilter.AuthorityEntry a = aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                            break;
                        }
                    }
                }
                Iterator<PatternMatcher> pIt = defaultresolveinfo.filter.pathsIterator();
                if (pIt != null) {
                    String path = data.getPath();
                    while (path != null && pIt.hasNext()) {
                        PatternMatcher p = pIt.next();
                        if (p.match(path)) {
                            filter.addDataPath(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
            }
        }

        if (filter != null) {
            ComponentName[] set = new ComponentName[n];
            int bestMatch = 0;
            for (int k = 0; k < n; k++) {
                set[k] = new ComponentName(riLists.get(k).activityInfo.packageName,
                        riLists.get(k).activityInfo.name);
                if (riLists.get(k).match > bestMatch) {
                    bestMatch = riLists.get(k).match;
                }

            }
            mPm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());
        }

        Log.e(TAG, "setDefaultLauncher end");
    }

    private void setLauncher(String defaultLauncherPkg, String defaultLauncherAC) {
        Log.e(TAG, "setLauncher start " + defaultLauncherPkg);

        ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
        mPm.getHomeActivities(homeActivities);
        ComponentName[] mHomeComponentSet = new ComponentName[homeActivities.size()];
        for (int i = 0; i < homeActivities.size(); i++) {
            final ResolveInfo candidate = homeActivities.get(i);
            final ActivityInfo info = candidate.activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            Log.e(TAG, "info.packageName = " + info.packageName + " info.name " + info.name);
            mHomeComponentSet[i] = activityName;
        }

        ComponentName newHome = new ComponentName(defaultLauncherPkg, defaultLauncherAC);
        IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
        mHomeFilter.addCategory(Intent.CATEGORY_HOME);
        mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mPm.replacePreferredActivity(mHomeFilter, IntentFilter.MATCH_CATEGORY_EMPTY,
                mHomeComponentSet, newHome);

        Log.e(TAG, "setLauncher end" + defaultLauncherAC);
    }

    Handler mHandler = null;
    private static final int EVENT_INTO_POWERSAVE_LANCHER = 1;

    private void initMainHandler() {
        mHandler = new Handler(mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case EVENT_INTO_POWERSAVE_LANCHER:
                        intoPowerSaveLancher();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void intoPowerSaveLancher() {
        Log.d(TAG, "StateController->start supermode finished,send broadcast");
        Intent enterintent = new Intent(StateController.EVENT_START_SUPERMODE_FINISH);
        mContext.sendBroadcast(enterintent);
    }

}
