package com.privacymanage.service;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.aurora.mms.util.Utils;
import com.privacymanage.data.AccountConfigData;
import com.privacymanage.service.IPrivacyManageService;

public class AuroraPrivacyUtils {
    
    private static boolean mIsServiceConnected = false;
    private static final String SERVICE_ACTION = "com.privacymanage.service.IPrivacyManageService";
    
    private static final String PACKAGENAME = "com.android.mms";
    private static final String CLASSNAME = "com.aurora.mms.ui.AuroraPrivConvActivity";
    
    private static IPrivacyManageService mPrivacyManSer;
    public static Context sContext;
    
    private static final Uri URL_STR  = Uri.parse("content://com.privacymanage.provider.ConfigProvider");
    


    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }


    
    public static void bindService(Context context) {
        if (context == null) {
            return;
        }
        sContext = context.getApplicationContext();
        Intent intent = new Intent(SERVICE_ACTION);
        if (Utils.hasLollipop()) {
        	intent = getExplicitIntent(sContext, intent);
        }
        if (!mIsServiceConnected) {
            boolean c = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
        }
    }
    
    public static void resetPrivacyNumOfAllAccount() {
        if (mPrivacyManSer == null) {
            bindService(sContext);
        }
        if (null != mPrivacyManSer) {
            try {
                mPrivacyManSer.resetPrivacyNumOfAllAccount(PACKAGENAME, CLASSNAME);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static void setPrivacy(int num, long accountId) {
        if (mPrivacyManSer == null) {
            bindService(sContext);
        }
        if (null != mPrivacyManSer) {
            try {
                mPrivacyManSer.setPrivacyNum(PACKAGENAME, CLASSNAME, num, accountId);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static long sCurrentAccountId = 0;
    
    public static long setCurrentAccountId(Context context) {
        long result = 0;
        try {
            bindService(context);
            if (mPrivacyManSer != null) {
                result = mPrivacyManSer.getCurrentAccount(PACKAGENAME, CLASSNAME).getAccountId();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        } finally {
            sCurrentAccountId = result;
        }
        return sCurrentAccountId;
    }
    
    public static long getCurrentAccountId() {
        if (mPrivacyManSer != null) {
            return sCurrentAccountId;
        } else {
            return setCurrentAccountId(sContext);
        }
    }
    
    
    private static ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mIsServiceConnected = true;
            mPrivacyManSer = IPrivacyManageService.Stub.asInterface(service);
            try {
                // Aurora xuyong 2015-04-20 modified for bug #13071 start
                if (mPrivacyManSer != null) {
                    sCurrentAccountId = mPrivacyManSer.getCurrentAccount(PACKAGENAME, CLASSNAME).getAccountId();
                }
                // Aurora xuyong 2015-04-20 modified for bug #13071 end
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mIsServiceConnected = false;
            mPrivacyManSer = null;
        }
        
    };
    
    private static final String ACCOUNT_ID = "account_id";
    private static final String MSG_NOTIFY_SWITCH = "msg_notify_switch";
    private static final String MSG_NOTIFY_HINT = "msg_notify_hint";
    
    private static final int INDEX_MN_SWITCH = 0;
    private static final int INDEX_MN_HINT   = 1;
    private static final String[] PRIVACT_NOTIFY_SELECTION = new String[] {
        MSG_NOTIFY_SWITCH,
        MSG_NOTIFY_HINT
    };
    
    public static AccountConfigData getAccountCongfiData(Context context) {;
        Cursor result = context.getContentResolver().query(URL_STR, PRIVACT_NOTIFY_SELECTION, ACCOUNT_ID + " = ", new String[] { String.valueOf(getCurrentAccountId()) }, null);
        if (result != null && result.moveToFirst()) {
            AccountConfigData data = new AccountConfigData();
            boolean notify = result.getInt(INDEX_MN_SWITCH) == 1 ? true : false;
            data.setMsgNotifySwitch(notify);
            data.setMsgNotifyHintStr(result.getString(INDEX_MN_HINT));
            return data;
        }
        return null;
    }
}