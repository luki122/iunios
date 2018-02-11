package com.aurora.puremanager.model;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.AutoStartRecordData;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.data.MyArrayList;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.LogUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.utils.mConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 获取有开机启动权限的应用 和 有后台的应用
 */
public class AutoStartModel {
    private static final String TAG = "AutoStartModel";

    private static AutoStartModel instance;
    private AtomicBoolean isDuringGetInfo = new AtomicBoolean(false);
    private Context context;
    private HashMap<String, AutoStartData> appsAutoStartMap;
    private HashMap<String, AutoStartData> noDealAutoStartMap;
    private HashMap<String, String> appsShutDownMap;//申请了关机广播的应用
    private MyArrayList<Handler> callBackHandlers;
    private ActivityManager activityManager;
    private MyHandler handler;
    private List<InputMethodInfo> appList;

    private final String[] Just_ACTION_BOOT_COMPLETED = {
            Intent.ACTION_BOOT_COMPLETED
    };

    private final String[] NeedDealActions = {
            Intent.ACTION_BOOT_COMPLETED //必须
            , Intent.ACTION_TIME_CHANGED //必须
            , Intent.ACTION_BATTERY_CHANGED//必须
//			,Intent.ACTION_MEDIA_BUTTON  //必须,耳机线控会触发这个消息
            , Intent.ACTION_USER_PRESENT //必须
            , Intent.ACTION_MEDIA_MOUNTED //必须
            , "android.net.conn.CONNECTIVITY_CHANGE"//必须
            , "android.intent.action.SERVICE_STATE"
            , "android.net.wifi.WIFI_STATE_CHANGED"
            , Intent.ACTION_POWER_CONNECTED
            , Intent.ACTION_POWER_DISCONNECTED
            , "android.provider.Telephony.SMS_RECEIVED"
            , "android.intent.action.PHONE_STATE"
            , Intent.ACTION_NEW_OUTGOING_CALL
            , Intent.ACTION_PACKAGE_ADDED
            , Intent.ACTION_PACKAGE_REMOVED
    };

    private final String[] NoNeedDealActions = {
            "android.appwidget.action.APPWIDGET_UPDATE"
    };

    /**
     * 注册观察者对象
     *
     * @param observer
     */
    public void attach(Handler callBackHandler) {
        callBackHandlers.add(callBackHandler);
    }

    /**
     * 删除观察者对象
     *
     * @param observer
     */
    public void detach(Handler callBackHandler) {
        callBackHandlers.remove(callBackHandler);
    }

    /**
     * 获取指定包名的自启动数据
     *
     * @param packageName
     * @return
     */
    public AutoStartData getAutoStartData(String packageName) {
        if (appsAutoStartMap == null) {
            return null;
        }
        return appsAutoStartMap.get(packageName);
    }

    /**
     * 通过界面修改自启动状态数据
     * 打开或关闭应用自启动
     *
     * @param packageName
     * @param isOpen
     */
    public void changeAutoStartState(String packageName, boolean isOpen) {
        if (appsAutoStartMap == null || packageName == null) {
            return;
        }
        AutoStartData autoStartData = appsAutoStartMap.get(packageName);
        if (autoStartData == null) {
            return;
        }
        if (isOpen) {
            ApkUtils.openApkAutoStart(context, autoStartData, packageName);
            //AutoStartAppProvider.openAutoStart(context, packageName);
        } else {
            ApkUtils.closeApkAutoStart(context, autoStartData, packageName);
            //AutoStartAppProvider.closeAutoStart(context, packageName);
        }
        changeRecord(packageName, isOpen);
    }

    /**
     * Vulcan created this method in 2015年1月13日 下午5:33:53 .
     *
     * @param pkgName
     * @param isOpen
     */
    public void tryChangeAutoStartState(String pkgName, boolean isOpen) {
        changeAutoStartStateInDB(pkgName, isOpen);
        boolean pkgIsInWhiteList = isInAutoStartWhiteList(pkgName);
        if (!pkgIsInWhiteList) {
            changeAutoStartState(pkgName, isOpen);
        } else if (isOpen) {
            changeAutoStartState(pkgName, isOpen);
        } else {
            //in other case we do nothing
            LogUtils.printWithLogCat("vautostart", "tryChangeAutoStartState: try to set flag of white list app to false, so do nothing");
        }
        return;
    }

    public void freezeAutoStart(String pkgName) {
        changeAutoStartStateInDB(pkgName, true);
        changeAutoStartState(pkgName, true);
    }

    public void unFreezeAutoStart(String pkgName) {
        changeAutoStartStateInDB(pkgName, false);
        changeAutoStartState(pkgName, false);
    }

    /**
     * Vulcan created this method in 2015年1月13日 下午5:29:33 .
     *
     * @param packageName
     * @param isOpen
     */
    public void changeAutoStartStateInDB(String packageName, boolean isOpen) {
        if (appsAutoStartMap == null || packageName == null) {
            return;
        }
        AutoStartData autoStartData = appsAutoStartMap.get(packageName);
        if (autoStartData == null) {
            return;
        }
        if (isOpen) {
            //ApkUtils.openApkAutoStart(context,autoStartData);
            AutoStartAppProvider.openAutoStart(context, packageName);
        } else {
            //ApkUtils.closeApkAutoStart(context,autoStartData);
            AutoStartAppProvider.closeAutoStart(context, packageName);
        }
        //changeRecord(packageName,isOpen);
    }

//    /**
//     * 获取开启自启动应用的个数
//     * @return
//     */
//    public int getAutoStartOpenAppNum(){
//    	int num = 0;
//    	if(appsAutoStartMap != null && appsAutoStartMap.size()>0){
//    		/**
//    		 * 重要：如果在遍历HashMap.keySet()的同时，
//    		 * 执行HashMap.add() 或 HashMap.remove()，会因为数据不同步而异常，
//    		 * 异常名为：java.util.ConcurrentModificationException。
//    		 * 所以在使用HashMap时一定要数据同步
//    		 */
//    		synchronized(appsAutoStartMap){
//    			Set<String> packageNames = appsAutoStartMap.keySet();
//     		    for (String packageName : packageNames){
//     		    	AutoStartData autoStartData = appsAutoStartMap.get(packageName);
//     		    	if(autoStartData != null && autoStartData.getIsOpen()){
//     		    		num ++;
//     		    	}
//     		    } 
//    		}  		
//    	}  	
//    	return num;
//    }

    /**
     * 获取开启自启动应用的个数
     *
     * @return
     */
    public int getAutoStartOpenAppNum() {
        int num = 0;
        AppsInfo userAppsInfo = ConfigModel.getInstance(context).
                getAppInfoModel().getThirdPartyAppsInfo();
        if (userAppsInfo == null) {
            return num;
        }

        HashSet<String> autoStartListInDB = AutoStartAppProvider.loadAutoStartAppListInDB(context);

        for (int i = 0; i < userAppsInfo.size(); i++) {
            AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
            if (appInfo == null || !appInfo.getIsInstalled()) {
                continue;
            }
            AutoStartData autoStartData = getAutoStartData(appInfo.getPackageName());
            if (autoStartData == null) {
                continue;
            }

            if (autoStartListInDB.contains(appInfo.getPackageName())) {
                num++;
            }
            /*
            if(autoStartData.getIsOpen()){
				num ++;
			}		
			*/
        }
        return num;
    }

    /**
     * 获取禁止自启应用的个数
     *
     * @return
     */
    public int getAutoStartCloseAppNum() {
        int num = 0;

        AppsInfo userAppsInfo = ConfigModel.getInstance(context).
                getAppInfoModel().getThirdPartyAppsInfo();
        if (userAppsInfo == null) {
            return num;
        }

        HashSet<String> autoStartListInDB = AutoStartAppProvider.loadAutoStartAppListInDB(context);

        for (int i = 0; i < userAppsInfo.size(); i++) {
            AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
            if (appInfo == null || !appInfo.getIsInstalled()) {
                continue;
            }

            AutoStartData autoStartData = getAutoStartData(appInfo.getPackageName());
            if (autoStartData == null || autoStartListInDB.contains(appInfo.getPackageName())) {
                continue;
            }
            num++;
    		/*
			if(autoStartData.getIsOpen()){
				num ++;
			}
			*/
        }

        return num;
    }

    /**
     * 如果instance为null，不会创建
     *
     * @return
     */
    public static synchronized AutoStartModel getInstance() {
        return instance;
    }

    /**
     * 必须在UI线程中初始化,如果instance为null，则会创建一个
     *
     * @param context
     * @return
     */
    public static synchronized AutoStartModel getInstance(Context context) {
        if (instance == null) {
            instance = new AutoStartModel(context);
        }
        return instance;
    }

    private AutoStartModel(Context context) {
        this.context = context.getApplicationContext();
        appsAutoStartMap = new HashMap<String, AutoStartData>();
        noDealAutoStartMap = new HashMap<String, AutoStartData>();
        callBackHandlers = new MyArrayList<Handler>();
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        //下面代码必须指定UI线程的Looper，防止如果该类定义在子线程内没有Looper而出错
        handler = new MyHandler(Looper.getMainLooper());
        readCacheStr(context);
    }

    /**
     * 应用启动时调用这个函数，
     * 必须是在所有应用数据获取完毕以后才能调用该函数
     */
    public void applicationStart() {
        dealFunc(false, null);
    }

    /**
     * 安装应用的时候调用该函数
     *
     * @param packageName
     */
    public void inStallApp(AppInfo appInfo) {
        if (appInfo == null) {
            Log.e(TAG, "inStallApp, appInfo is null");
            return;
        }
        //applicationStart();
        dealFunc(false, appInfo.getPackageName());
    }

    /**
     * 覆盖安装应用的时候调用该函数
     *
     * @param packageName
     */
    public void coverInStallApp(AppInfo appInfo) {
        applicationStart();
    }

    /**
     * 删除应用时调用该函数
     *
     * @param packageName
     */
    public void unInStallApp(String pkgName) {
        if (pkgName == null) {
            return;
        }

        if (appsAutoStartMap != null &&
                appsAutoStartMap.containsKey(pkgName)) {
            synchronized (appsAutoStartMap) {
                appsAutoStartMap.remove(pkgName);
            }
        }

        AutoStartAppProvider.closeAutoStart(context, pkgName);
        LogUtils.printWithLogCat("vautostart", "externalAppUnAvailable: remove " + pkgName);
    }

    /**
     * 安装在sd卡中的应用变的可用
     *
     * @param pkgList
     */
    public void externalAppAvailable(List<String> pkgList) {
        applicationStart();
    }

    /**
     * 安装在sd卡中的应用变的不可用
     *
     * @param pkgList
     */
    public void externalAppUnAvailable(List<String> pkgList) {
        if (appsAutoStartMap == null || pkgList == null) {
            return;
        }

        for (int i = 0; i < pkgList.size(); i++) {
            synchronized (appsAutoStartMap) {
                if (appsAutoStartMap.containsKey(pkgList.get(i))) {
                    appsAutoStartMap.remove(pkgList.get(i));
                }
            }
        }

    }

    /**
     * 关机时调用,根据自启动配置表，重置个应用广播开启的状态
     */
    public void shutDown() {
        dealFunc(true, null);
    }

    /**
     * 处理逻辑
     *
     * @param isShutDown 是不是关机状态
     */
    private void dealFunc(final boolean isShutDown, final String pkgNameOfNewApp) {
        if (isDuringGetInfo.get()) {
            return;
        }
        isDuringGetInfo.set(true);

        if (appsAutoStartMap == null) {
            appsAutoStartMap = new HashMap<String, AutoStartData>();
        } else {
            synchronized (appsAutoStartMap) {
                appsAutoStartMap.clear();
            }
        }

        if (noDealAutoStartMap == null) {
            noDealAutoStartMap = new HashMap<String, AutoStartData>();
        } else {
            synchronized (noDealAutoStartMap) {
                noDealAutoStartMap.clear();
            }
        }

        LogUtils.printWithLogCat(
                AutoStartModel.class.getName(),
                "applicationStart");
        new Thread() {
            @Override
            public void run() {
                if (isShutDown) {
                    resetShutDownReceiveResolveInfoList(context);
                }
                resetNoDealAutoStartResolveList(context);
                resetbootReceiveResolveInfoList(context);
                removeSysApp(context);
                checkAllApkAutoStart(isShutDown);
                AutoStartAppProvider.initProvider(context);

                if (pkgNameOfNewApp != null) {

                    synchronized (appsAutoStartMap) {
                        if (appsAutoStartMap.get(pkgNameOfNewApp) != null) {
                            boolean newAppIsOpen = appsAutoStartMap.get(pkgNameOfNewApp).getIsOpen();
                            if (newAppIsOpen) {
                                AutoStartAppProvider.addAppInDB(context, pkgNameOfNewApp);
                            } else {
                                LogUtils.printWithLogCat("vautostart", "dealFunc: new app is not auto-startable & is in autostartmap");
                            }
                        } else {
                            LogUtils.printWithLogCat("vautostart", "dealFunc: new app is not auto-startable & not in autostartmap");
                        }
                    }
                }

                LogUtils.printWithLogCat(
                        AutoStartModel.class.getName(),
                        "applicationStart FUNC END");
                if (!isShutDown) {
                    handler.sendEmptyMessage(0);
                }
                isDuringGetInfo.set(false);
            }
        }.start();
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            PersistentModel.getInstance().dealPersistentApp(context);
            if (callBackHandlers == null) {
                return;
            }
            for (int i = 0; i < callBackHandlers.size(); i++) {
                Handler handler = callBackHandlers.get(i);
                if (handler != null) {
                    handler.sendEmptyMessage(0);
                }
            }
        }
    }

    /**
     * 重置所有可以接受关机事件的应用列表
     *
     * @param context
     */
    private void resetShutDownReceiveResolveInfoList(Context context) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SHUTDOWN);
        List<ResolveInfo> resolveInfoList = null;
        try {
            resolveInfoList = context.getPackageManager()
                    .queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resetAppsShutDownMap();
        if (resolveInfoList != null) {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (resolveInfo == null ||
                        resolveInfo.activityInfo == null ||
                        resolveInfo.activityInfo.packageName == null) {
                    continue;
                }
                appsShutDownMap.put(resolveInfo.activityInfo.packageName, "test");
            }
        }
    }

    /**
     * 重置可以接收关机事件广播的应用
     */
    private void resetAppsShutDownMap() {
        if (appsShutDownMap == null) {
            appsShutDownMap = new HashMap<String, String>();
        } else {
            appsShutDownMap.clear();
        }
        /**
         * 多米音乐虽然没有接收关机事件的广播，但是它会一直去改变自身广播的接收状态，
         * 所以在关机重置广播接收状态时，也需要结束掉多米音乐
         */
        appsShutDownMap.put("com.duomi.android", "test");

    }

    /**
     * 重置所有可以接受开机事件的应用列表
     *
     * @param context
     */
    private void resetbootReceiveResolveInfoList(Context context) {
        /**
         * 返回所有自启动的应用，注意：flag要设为PackageManager.GET_DISABLED_COMPONENTS。
         * 它表示在返回时要包含自启动禁止启动的应用。如果不设为这个flag，则返回的只有当前允许自启动的应用。
         */
        if (context == null) {
            return;
        }
        Intent intent = null;
        List<ResolveInfo> resolveInfoList = null;
        String[] actions;
        if (mConfig.isAutoStartControlReceive) {
            actions = NeedDealActions;
        } else {
            actions = Just_ACTION_BOOT_COMPLETED;
        }
        for (String action : actions) {
            intent = new Intent(action);
            try {
                resolveInfoList = context.getPackageManager()
                        .queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dealResolveInfoList(resolveInfoList);
        }
    }

    private void dealResolveInfoList(List<ResolveInfo> resolveInfoList) {
        if (resolveInfoList == null) {
            return;
        }
        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (resolveInfo == null ||
                    resolveInfo.activityInfo == null ||
                    resolveInfo.activityInfo.packageName == null) {
                continue;
            }

            if (!checkNeedDeal(resolveInfo)) {
                continue;
            }

            AutoStartData autoStartData = appsAutoStartMap.get(resolveInfo.activityInfo.packageName);
            if (autoStartData == null) {
                autoStartData = new AutoStartData();
                autoStartData.AddResolveInfo(resolveInfo);
                synchronized (appsAutoStartMap) {
                    appsAutoStartMap.put(resolveInfo.activityInfo.packageName, autoStartData);
                }
            } else {
                autoStartData.AddResolveInfo(resolveInfo);
            }
        }
    }

    /**
     * 重置不需要处理自启动的ResolveList
     */
    private void resetNoDealAutoStartResolveList(Context context) {
        if (context == null) {
            return;
        }

        Intent intent = null;
        List<ResolveInfo> resolveInfoList = null;
        if (mConfig.isAutoStartControlReceive) {
            for (String action : NoNeedDealActions) {
                intent = new Intent(action);
                try {
                    resolveInfoList = context.getPackageManager()
                            .queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dealResolveListForNoDealAutoStart(resolveInfoList);
            }
        }
    }

    private void dealResolveListForNoDealAutoStart(List<ResolveInfo> resolveInfoList) {
        if (resolveInfoList == null) {
            return;
        }
        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (resolveInfo == null ||
                    resolveInfo.activityInfo == null ||
                    resolveInfo.activityInfo.packageName == null ||
                    resolveInfo.activityInfo.name == null) {
                continue;
            }
            AutoStartData autoStartData = noDealAutoStartMap.get(resolveInfo.activityInfo.packageName);
            if (autoStartData == null) {
                autoStartData = new AutoStartData();
                autoStartData.AddResolveInfo(resolveInfo);
                synchronized (noDealAutoStartMap) {
                    noDealAutoStartMap.put(resolveInfo.activityInfo.packageName, autoStartData);
                }
            } else {
                autoStartData.AddResolveInfo(resolveInfo);
            }
        }
    }

    /**
     * 清除appsAutoStartMap中的系统应用
     */
    private void removeSysApp(Context context) {
        if (appsAutoStartMap == null) {
            return;
        }
        List<String> pkgList = new ArrayList<String>();
        synchronized (appsAutoStartMap) {
            Set<String> packageNames = appsAutoStartMap.keySet();
            for (String packageName : packageNames) {
                pkgList.add(packageName);
            }
        }

        for (int i = 0; i < pkgList.size(); i++) {
            if (ApkUtils.isUserApp(ApkUtils.getApplicationInfo(context, pkgList.get(i)))) {
                continue;
            }
            synchronized (appsAutoStartMap) {
                appsAutoStartMap.remove(pkgList.get(i));
            }
        }
    }

    /**
     * 判断该ResolveInfo是否需要做开机自启动的处理
     *
     * @param checkResolveInfo
     * @return true:需要做处理　false:不需要做处理
     */
    private boolean checkNeedDeal(ResolveInfo checkResolveInfo) {
        boolean isNeedDeal = true;
        String checkName;
        if (noDealAutoStartMap == null ||
                (checkName = checkResolveInfo.activityInfo.name) == null) {
            return isNeedDeal;
        }
        AutoStartData noDealAutoStartData = noDealAutoStartMap.get(checkResolveInfo.activityInfo.packageName);
        if (noDealAutoStartData == null ||
                noDealAutoStartData.getResolveInfoList() == null) {
            return isNeedDeal;
        }
        List<ResolveInfo> noDealResolveList = noDealAutoStartData.getResolveInfoList();

        for (int i = 0; i < noDealResolveList.size(); i++) {
            String noDealClassName = noDealResolveList.get(i).activityInfo.name;
            if (checkName.equals(noDealClassName)) {
                isNeedDeal = false;
                LogUtils.printWithLogCat(
                        AutoStartModel.class.getName(),
                        "noDeal pkg=" + checkResolveInfo.activityInfo.packageName +
                                ",className=" + checkResolveInfo.activityInfo.name);
                break;
            }
        }
        return isNeedDeal;
    }

    /**
     * 清除appsAutoStartMap中的不需要处理的应用
     */
    private void removeNoDealApp() {
        if (appsAutoStartMap == null || noDealAutoStartMap == null) {
            return;
        }

        List<String> noDealPkgList = new ArrayList<String>();
        synchronized (noDealAutoStartMap) {
            Set<String> packageNames = noDealAutoStartMap.keySet();
            for (String packageName : packageNames) {
                noDealPkgList.add(packageName);
            }
        }

        String tmpPkg;
        AutoStartData checkAutoStartData;
        for (int i = 0; i < noDealPkgList.size(); i++) {
            tmpPkg = noDealPkgList.get(i);
            checkAutoStartData = appsAutoStartMap.get(tmpPkg);
            if (checkAutoStartData == null) {
                continue;
            }
            removeResolve(noDealAutoStartMap.get(tmpPkg), checkAutoStartData);
        }
    }

    private void removeResolve(AutoStartData noDealAutoStartData,
                               AutoStartData checkAutoStartData) {
        if (noDealAutoStartData == null || checkAutoStartData == null) {
            return;
        }
        List<ResolveInfo> noDealResolveList = noDealAutoStartData.getResolveInfoList();
        List<ResolveInfo> checkResolveList = checkAutoStartData.getResolveInfoList();
        if (noDealResolveList == null || checkResolveList == null) {
            return;
        }
        for (int i = 0; i < noDealResolveList.size(); i++) {
            ResolveInfo noDealResolveInfo = noDealResolveList.get(i);
            if (noDealResolveInfo == null) {
                continue;
            }
            String noDealClassName = noDealResolveInfo.activityInfo.name;

            for (int j = 0; j < checkResolveList.size(); j++) {
                ResolveInfo checkResolveInfo = checkResolveList.get(j);
                if (checkResolveInfo == null) {
                    continue;
                }
                if (noDealClassName.equals(checkResolveInfo.activityInfo.name)) {
                    try {
                        checkResolveList.remove(checkResolveInfo);
                        LogUtils.printWithLogCat(
                                AutoStartModel.class.getName(),
                                "noDeal pkg=" + checkResolveInfo.activityInfo.packageName +
                                        ",className=" + checkResolveInfo.activityInfo.name);
                    } catch (Exception e) {
                    }
                    break;
                }
            }
        }
    }

    /**
     * 检测所有应用的自启动广播是否开启
     */
    private void checkAllApkAutoStart(boolean isShutDown) {
        if (appsAutoStartMap == null) {
            return;
        }

        List<String> pkgList = new ArrayList<String>();
        /**
         * 重点：同步块的区域一定要小，最好不要包含自己定义的方法，
         * 因为有可能同步块内自己写的方法中也加在同样的同步锁，这样就会出现死锁的情况，
         * 所以下面的代码就做了这样的处理，把checkOneApkAutoStart（）和initOneApkAutoStart（）方法
         * 放到同步块外面去了。
         */
        synchronized (appsAutoStartMap) {
            Set<String> packageNames = appsAutoStartMap.keySet();
            for (String packageName : packageNames) {
                pkgList.add(packageName);
            }
        }
        boolean hasAutoStart = false;
        for (int i = 0; i < pkgList.size(); i++) {
            String pkgName = pkgList.get(i);
            checkOneApkAutoStart(appsAutoStartMap.get(pkgName), pkgName);
            hasAutoStart = getOneApkHasAutoStart(appsAutoStartMap.get(pkgName), pkgName);
            AutoStartData data = appsAutoStartMap.get(pkgName);
            if (null == data)
                return;
            appsAutoStartMap.get(pkgName).setHasAutoStart(hasAutoStart);
            initOneApkAutoStart(isShutDown, pkgName, appsAutoStartMap.get(pkgName), context);

            HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(context);
            appsAutoStartMap.get(pkgName).setAutoStartOfUser(autostartApps.contains(pkgName));
        }
    }

    /**
     * Vulcan created this method in 2015年1月14日 下午3:18:30 .
     *
     * @return
     */
    public HashMap<String, Boolean> getListHasAutoStart() {
        HashMap<String, Boolean> listHasAutoStart = new HashMap<String, Boolean>();
        if (appsAutoStartMap == null) {
            return listHasAutoStart;
        }

        List<String> pkgList = new ArrayList<String>();
        /**
         * 重点：同步块的区域一定要小，最好不要包含自己定义的方法，
         * 因为有可能同步块内自己写的方法中也加在同样的同步锁，这样就会出现死锁的情况，
         * 所以下面的代码就做了这样的处理，把checkOneApkAutoStart（）和initOneApkAutoStart（）方法
         * 放到同步块外面去了。
         */
        synchronized (appsAutoStartMap) {
            Set<String> packageNames = appsAutoStartMap.keySet();
            for (String packageName : packageNames) {
                pkgList.add(packageName);
            }
        }

        boolean hasAutoStart = false;
        for (int i = 0; i < pkgList.size(); i++) {
            String pkgName = pkgList.get(i);
            hasAutoStart = getOneApkHasAutoStart(appsAutoStartMap.get(pkgName), pkgName);
            listHasAutoStart.put(pkgName, hasAutoStart);
        }
        return listHasAutoStart;
    }

    /**
     * 检测某个应用的自启动广播是否开启
     *
     * @param autoStartData
     */
    private synchronized void checkOneApkAutoStart(AutoStartData autoStartData, String pkgName) {
        if (autoStartData == null) {
            return;
        }

        List<ResolveInfo> resolveInfoList = autoStartData.getResolveInfoList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            ResolveInfo resolveInfo = resolveInfoList.get(i);
            if (resolveInfo == null) {
                continue;
            }
            if (isReceiveOpen(resolveInfo, context)) {
                autoStartData.setIsOpen(true);
                LogUtils.printWithLogCat("vautostart", String.format("checkOneApkAutoStart: pkgName=%s,isOpen=true", pkgName));
                return;
            }
        }
        //LogUtils.printWithLogCat("vautostart", String.format("checkOneApkAutoStart: pkgName=%s,isOpen=false", pkgName));
        autoStartData.setIsOpen(false);
    }

    /**
     * Vulcan created this method in 2015年1月14日 下午3:16:08 .
     *
     * @param autoStartData
     * @param pkgName
     * @return
     */
    private synchronized boolean getOneApkHasAutoStart(AutoStartData autoStartData, String pkgName) {
        //setHasAutoStart
        if (autoStartData == null) {
            return false;
        }

        List<ResolveInfo> resolveInfoList = autoStartData.getResolveInfoList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            ResolveInfo resolveInfo = resolveInfoList.get(i);
            if (resolveInfo == null) {
                continue;
            }
            if (isReceiveOpen(resolveInfo, context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化某个应用的自启动广播是否开启
     *
     * @param autoStartData
     */
    private synchronized void initOneApkAutoStart(boolean isShutDown,
                                                  String packageName, AutoStartData autoStartData, Context context) {
        if (autoStartData == null || packageName == null) {
            return;
        }
        boolean isNeedOpen = isNeedOpenAutoStart(packageName);
        if (isNeedOpen) {
            ApkUtils.openApkAutoStart(context, autoStartData, packageName);
        } else {
//			if(isShutDown && 
//					appsShutDownMap != null && 
//					appsShutDownMap.containsKey(packageName)){
//				LogUtils.printWithLogCat(
//						AutoStartModel.class.getName(),
//						"forceStopPackage:"+packageName);
//				//在关机时，如果一个应用申请了关机广播，那么先结束这个应用，再关掉这个应用的自启动广播
//				activityManager.forceStopPackage(packageName);			
//			}
//			ApkUtils.closeApkAutoStart(context, autoStartData);

            if (isShutDown) {
//				if(appsShutDownMap != null && 
//						appsShutDownMap.containsKey(packageName)){
//					LogUtils.printWithLogCat(
//							AutoStartModel.class.getName(),
//							"forceStopPackage:"+packageName);
//					//在关机时，如果一个应用申请了关机广播，那么先结束这个应用，再关掉这个应用的自启动广播
//					activityManager.forceStopPackage(packageName);			
//				}
                if (!isInputMethodApp(packageName)) {
                    activityManager.forceStopPackage(packageName);
                }
                ApkUtils.closeApkAutoStart(context, autoStartData, packageName);
            } else {
                ApplicationInfo applicationInfo = ApkUtils.getApplicationInfo(context, packageName);
                if (applicationInfo != null && (applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                    autoStartData.setIsOpen(false);//如果该应用正在运行，则仅仅修改标志量，而不去关闭广播
                } else {
                    ApkUtils.closeApkAutoStart(context, autoStartData, packageName);
                }
            }
        }
    }

    /**
     * 判断当前应用是不是输入法,如果是输入法，则在关机的时候不能停止运行；
     * 如果把输入法停止运行，那么输入法的默认值会改变。
     *
     * @param pkgName
     * @return
     */
    private boolean isInputMethodApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if (appList == null) {
            appList = ApkUtils.getEnabledInputMethodList(context);
        }
        int size = appList == null ? 0 : appList.size();
        for (int i = 0; i < size; i++) {
            InputMethodInfo tmpItem = (InputMethodInfo) appList.get(i);
            if (tmpItem == null) {
                continue;
            }
            if (pkgName.equals(tmpItem.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前软件的开机启动是否打开（说明：这个软件必须有开机动的广播）
     *
     * @param appInfo
     * @return
     */
    private boolean isReceiveOpen(ResolveInfo resolveInfo, Context context) {
        if (resolveInfo == null || context == null) {
            return false;
        }

        ComponentName tmpComponent = new ComponentName(resolveInfo.activityInfo.packageName,
                resolveInfo.activityInfo.name);
        try {
            return ApkUtils.getComponentEnabledState(context, tmpComponent);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否在自启动白名单中
     *
     * @param packageName
     * @return true:在白名单中 false：没有在白名单中
     */
    private boolean isInAutoStartWhiteList(String packageName) {
        return isInAutoStartWhiteList2(packageName);
    }

    @SuppressWarnings("unused")
    private boolean isInAutoStartWhiteList1(String packageName) {
        for (int i = 0; i < Constants.autoStartWhiteList.length; i++) {
            if (packageName != null &&
                    packageName.contains(Constants.autoStartWhiteList[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vulcan created this method in 2015年1月13日 下午6:00:03 .
     *
     * @param packageName
     * @return
     */
    private boolean isInAutoStartWhiteList2(String packageName) {
        for (int i = 0; i < Constants.autoStartWhiteList.length; i++) {
            if (packageName != null &&
                    packageName.toLowerCase(Locale.US).contains(Constants.autoStartWhiteList[i].toLowerCase(Locale.US))) {
                LogUtils.printWithLogCat("vautostart", String.format("isInAutoStartWhiteList2: %s---->%b", packageName, true));
                return true;
            }
        }
        return false;
    }

    public static void releaseObject() {
        if (instance != null) {
            if (mConfig.SET_NULL_OF_CONTEXT) {
                instance.context = null;
            }
            if (instance.appsAutoStartMap != null) {
                synchronized (instance.appsAutoStartMap) {
                    instance.appsAutoStartMap.clear();
                }
            }

            if (instance.appsShutDownMap != null) {
                instance.appsShutDownMap.clear();
            }

            if (instance.callBackHandlers != null) {
                instance.callBackHandlers.clear();
            }
            instance = null;
        }
    }

    /******************
     * 记录用户打开或关闭应用自启动的操作
     ***********************/
    private HashMap<String, AutoStartRecordData> recordMap;

    /**
     * 判断这个应用是否应该打开自启动
     *
     * @param packageName
     * @return
     */
    private boolean isNeedOpenAutoStart(String packageName) {
        return isNeedOpenAutoStart1(packageName);
    }


    private boolean isNeedOpenAutoStart1(String packageName) {
        if (packageName == null) {
            return false;
        }
        AutoStartRecordData recordData = null;
        if (recordMap != null) {
            recordData = recordMap.get(packageName);
        }
        if (recordData != null) {
            return recordData.getIsOpen();
        } else if (isInAutoStartWhiteList(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Vulcan created this method in 2015年1月13日 下午5:02:48 .
     *
     * @param packageName
     * @return
     */
    @SuppressWarnings("unused")
    private boolean isNeedOpenAutoStart2(String packageName) {
        if (packageName == null) {
            return false;
        }
        boolean b = isInAutoStartWhiteList(packageName);
        return b;
    }

    private void changeRecord(String packageName, boolean isOpen) {
        if (packageName == null) {
            return;
        }

        if (recordMap == null) {
            recordMap = new HashMap<String, AutoStartRecordData>();
        }

        AutoStartRecordData recordData = recordMap.get(packageName);
        if (recordData == null) {
            recordData = new AutoStartRecordData();
            recordData.setPackageName(packageName);
            recordData.setIsOpen(isOpen);
            synchronized (recordMap) {
                recordMap.put(packageName, recordData);
            }
        } else {
            recordData.setIsOpen(isOpen);
        }
        saveCacheStr(context);
    }

    private boolean readCacheStr(Context context) {
        boolean result = true;
        String str = null;
        synchronized (mConfig.cache_file_name_of_autoStart) {
            str = FileModel.getInstance(context).readFile(mConfig.cache_file_name_of_autoStart);
            LogUtils.printWithLogCat(
                    AutoStartModel.class.getName(),
                    "read:" + str);
        }
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        try {
            parseItem(str);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private void parseItem(String str) throws Exception {
        if (recordMap == null) {
            recordMap = new HashMap<String, AutoStartRecordData>();
        } else {
            synchronized (recordMap) {
                recordMap.clear();
            }
        }

        JSONObject json = JSON.parseObject(str);
        if (json != null && !json.isEmpty()) {
            JSONArray list = json.getJSONArray("list");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    if (!item.isEmpty()) {
                        AutoStartRecordData recordData = new AutoStartRecordData();
                        if (recordData.parseJson(item)) {
                            synchronized (recordMap) {
                                recordMap.put(recordData.getPackageName(), recordData);
                            }
                        }
                    }
                }
            }
        }
    }

    private void saveCacheStr(Context context) {
        String needSaveStr = getNeedSaveStr();
        if (context == null || StringUtils.isEmpty(needSaveStr)) {
            return;
        }

        synchronized (mConfig.cache_file_name_of_autoStart) {
            LogUtils.printWithLogCat(
                    AutoStartModel.class.getName(),
                    "save:" + needSaveStr);
            FileModel.getInstance(context).writeFile(mConfig.cache_file_name_of_autoStart, needSaveStr);
        }
    }

    private String getNeedSaveStr() {
        JSONObject json = new JSONObject();
        JSONArray jsonList = new JSONArray();

        if (recordMap != null && recordMap.size() > 0) {
            synchronized (recordMap) {
                Set<String> keySet = recordMap.keySet();
                for (String packageName : keySet) {
                    AutoStartRecordData flowData = recordMap.get(packageName);
                    jsonList.add(flowData.getJson());
                }
            }
            json.put("list", jsonList);
        }
        return json.toJSONString();
    }
}
