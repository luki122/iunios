package com.aurora.puremanager.traffic;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.NetworkPolicyManager;
import android.os.INetworkManagementService;
import android.util.Log;

public class AppNetworkControl {
	
    private static final String APPNETWORKCONTROL_CONSTANT = "setFirewallUidChainRule";
    private Context mContext;
    private static AppNetworkControl mAppNetworkControl;

    private AppNetworkControl(Context context) {
        mContext = context;
    }

    public static AppNetworkControl getInstance(Context context) {
        if (mAppNetworkControl == null) {
            mAppNetworkControl = new AppNetworkControl(context);
        }
        return mAppNetworkControl;
    }

    public void reflectNetworkControlAction(int uid, int networkType, boolean allow){
		INetworkManagementService mNetworkService = INetworkManagementService.Stub
				.asInterface(android.os.ServiceManager
						.getService(Context.NETWORKMANAGEMENT_SERVICE));
		try {
			Class c1 = INetworkManagementService.class;
			Method method = c1.getDeclaredMethod(APPNETWORKCONTROL_CONSTANT,
					int.class, int.class, boolean.class);
			method.setAccessible(true);
			Log.d("action", uid + "," + (networkType == 0 ? "mobile" : "wifi") +","+ (allow ? "prohibit":"allow"));
			method.invoke(mNetworkService, uid, networkType, allow);
		} catch (Exception e) {
			Log.d("action", e.toString());
		}
    }
    
    private NetworkPolicyManager getPolicyManager() {
        return NetworkPolicyManager.from(mContext);
    }
    
    /*public void singleNetworkControl(Context context, String packageName, boolean isBlock) {
		try {
			if (AppSavingService.getInstance(context).getSavingService() != null) {
				if (AppSavingService.getInstance(context).getSavingService()
						.isAppBlocked(packageName) != isBlock) {
					AppSavingService.getInstance(context).getSavingService()
							.blockApp(packageName, isBlock);
				}
			}
		} catch (RemoteException e) {
		}
	}*/
}
