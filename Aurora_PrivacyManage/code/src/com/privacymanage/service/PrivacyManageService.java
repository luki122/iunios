package com.privacymanage.service;

import com.privacymanage.data.AidlAccountData;
import com.privacymanage.model.AccountModel;
import com.privacymanage.model.ChildModuleModel;
import com.privacymanage.provider.AccountProvider;
import com.privacymanage.utils.LogUtils;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class PrivacyManageService extends Service{
	private final String TAG = PrivacyManageService.class.getName();
	final Object mLock = new Object();
	
    @Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {	
		super.onDestroy();
	}

	@Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    
    private final IPrivacyManageService.Stub mBinder = new IPrivacyManageService.Stub(){

		@Override
		public void setPrivacyNum(String pkgName, String className, int num,long accountId)
				throws RemoteException {
			LogUtils.printWithLogCat(TAG,"setPrivacyNum pkgName="+pkgName+
					",className="+className+",num="+num+",accountId="+accountId);
			ChildModuleModel.getInstance().updateChildModuleItemNum(pkgName,className,num,accountId);			
		}

		@Override
		public AidlAccountData getCurrentAccount(String pkgName,
				String className) throws RemoteException {
			return AccountModel.getInstance().getCurAccount();
		}

		@Override
		public long[] getAllAccountId() throws RemoteException {
			return AccountProvider.getAllAccountId(getApplicationContext());
		}

		@Override
		public void resetPrivacyNumOfAllAccount(String pkgName, String className)
				throws RemoteException {
			LogUtils.printWithLogCat(TAG,"resetPrivacyNum pkgName="+pkgName+
					",className="+className);
			ChildModuleModel.getInstance().resetPrivacyNumOfAllAccount(pkgName, 
					className);			
		}
    };
}

    