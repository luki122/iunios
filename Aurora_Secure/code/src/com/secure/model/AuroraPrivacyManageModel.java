package com.secure.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.IPrivacyManageService;
import com.secure.activity.PrivacyAppActivity;
import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.provider.open.PrivacyAppProvider;
import com.secure.utils.ApkUtils;
import com.secure.utils.DisableChanger;
import com.secure.utils.LogUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

/**
 * 绑定隐私管理模块
 */
public class AuroraPrivacyManageModel extends PrivacyAppSubject{	
	static Object sGlobalLock = new Object();
	private final String TAG = AuroraPrivacyManageModel.class.getName();
	private final int MSG_addPrivacyApp = 1;
	private final int MSG_deletePrivacyApp = 2;
	private final int MSG_bindServiceCallBack = 3;
	private final int MSG_accountSwitch = 4;
	private final int MSG_deleteAccount = 5;
	private final int MSG_resetPrivacyNumOfAllAccount = 6;
	private final int MSG_setPrivacyAppNumToPrivacyManage = 7;
	private static AuroraPrivacyManageModel instance;
	private Context context;
	private IPrivacyManageService iPrivacyManageService = null;
	private final AidlAccountData curAccountData = new AidlAccountData();
	private final AtomicBoolean isCurAccountInfoInited = new AtomicBoolean(false);
	/**
	 * 存放所有账户下的隐私应用
	 */
	private final MyArrayList<PrivacyAppData> privacyAppList;
	private final UIHandler mUIhandler;
	private final HandlerThread mQueueThread;
	private final QueueHandler mQueueHandler;
    private final Object mLock = new Object();
    private final Object mLockJustForBindService = new Object();
		
    /**
     * 获取instance，如果instance为null，会创建一个实例
     * @param context
     * @return 返回不会为null
     */
	public static AuroraPrivacyManageModel getInstance(Context context) {
		synchronized (sGlobalLock) {
			if (instance == null) {
				instance = new AuroraPrivacyManageModel(context);
			}
			return instance;
		}
	}
	
	/**
	 * 获取instance，如果instance为null，不会创建一个实例
	 * @return 返回会为null
	 */
	public static AuroraPrivacyManageModel getInstance(){
		synchronized (sGlobalLock) {
			return instance;
		}
	}
	
	private AuroraPrivacyManageModel(Context context) {
		this.context = context.getApplicationContext();	
		isCurAccountInfoInited.set(false);
		curAccountData.setAccountId(mConfig.NORMAL_ACCOUNTID);
		privacyAppList = PrivacyAppProvider.getPrivacyAppInfo(this.context);
		mUIhandler = new UIHandler(Looper.getMainLooper());	
        mQueueThread = new HandlerThread(TAG+":Background");
        mQueueThread.start();
        mQueueHandler = new QueueHandler(mQueueThread.getLooper()); 
	}
	
	/**
	 * 获取当前隐私身份Id
	 * @return
	 */
	public long getCurAccountId(){	
		if (!isCurAccountInfoInited.get()) {
			bindService(null);
        }
		return curAccountData.getAccountId();
	}
	
	/**
	 * 处理应用卸载
	 * @param pkgList
	 */
	public synchronized void dealUnInstallApp(MyArrayList<String> pkgList){
		deletePrivacyApp(pkgList);
	}
	
	/**
	 * 处理外部应用可用
	 * @param pkgList
	 */
	public synchronized void dealExternalAppAvailable(List<String> pkgList){
		dealExternalAppUnAvailable(pkgList);
	}
	
	/**
	 * 处理外部应用不可用
	 * @param pkgList
	 */
	public synchronized void dealExternalAppUnAvailable(List<String> pkgList){
		if(pkgList == null){
			return ;
		}
		MyArrayList<Long> needUpdateAccountIds = new MyArrayList<Long>();
		for(int i=0;i<pkgList.size();i++){
			PrivacyAppData privacyAppData = findPrivacyAppData(pkgList.get(i));
			if(privacyAppData == null){
				continue ;
			}
			addForNeedUpdateAccountIds(needUpdateAccountIds,privacyAppData.getAccountId());
		}		
		if(needUpdateAccountIds.size()>0){
			setPrivacyAppNumToPrivacyManage(needUpdateAccountIds);
		}
	}
	
	/**
	 * 关机时调用该函数，确保退出隐私空间
	 */
	public void shutDown(){
		AidlAccountData norAccount = new AidlAccountData();
		norAccount.setAccountId(mConfig.NORMAL_ACCOUNTID);
		norAccount.setHomePath("");
		switchAccount(norAccount,false);		
	}
	
	/**
	 * 身份切换
	 * @param accountData
	 */
	public void switchAccount(AidlAccountData newAccountData){
		switchAccount(newAccountData,true);
	}  
	
	private void switchAccount(AidlAccountData newAccountData,boolean needBindService){
		synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_accountSwitch;
			msg.obj = newAccountData;
			mQueueHandler.sendMessage(msg);	
		}
		if(needBindService && !isBindService()){
			bindService(null);
		}
	} 
	
	
	/**
	 *  删除隐私账户
	 * @param needDeleteAccount
	 * @param delete true：删除隐私空间数据，false：还原隐私空间数据
	 */
	public void deleteAccount(AidlAccountData needDeleteAccount,boolean delete){
        synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_deleteAccount;
			msg.obj = needDeleteAccount;
			msg.arg1 = delete?1:0;
			mQueueHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 添加隐私应用
	 * @param pkgList
	 */
	public void addPrivacyApp(MyArrayList<String> pkgList){
		synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_addPrivacyApp;
			msg.obj = pkgList;
			mQueueHandler.sendMessage(msg);	
		}
	}
		
	/**
	 * 删除隐私应用
	 * @param pkgList
	 */
	public void deletePrivacyApp(MyArrayList<String> pkgList){
		synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_deletePrivacyApp;
			msg.obj = pkgList;
			mQueueHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 向隐私管理设置隐私app的数目
	 * @param needUpdateAccountIds 需要更新的账户id
	 */
	public void setPrivacyAppNumToPrivacyManage(MyArrayList<Long> needUpdateAccountIds){
		synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_setPrivacyAppNumToPrivacyManage;
			msg.obj = needUpdateAccountIds;
			mQueueHandler.sendMessage(msg);	
		}
	}
	
	/**
	 * 重置指定模块所有隐私空间下的隐私个数
	 */
	public void resetPrivacyNumOfAllAccount(){
		synchronized (mLock){
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_resetPrivacyNumOfAllAccount;
			mQueueHandler.sendMessage(msg);	
		}		
	}
	
	/**
	 * 绑定服务
	 * @param bindCallback
	 */
	private void bindService(final BindServiceCallback bindCallback){	
		synchronized (mLock){
			mQueueHandler.removeMessages(MSG_bindServiceCallBack);
			
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_bindServiceCallBack;
			msg.obj = bindCallback;
			mQueueHandler.sendMessage(msg);	
		}		
	}
			
    final class QueueHandler extends Handler {
        public QueueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
				case MSG_accountSwitch:
					switchAccountFunc((AidlAccountData)msg.obj);
	 				break;
	 			case MSG_deleteAccount:
	 				deleteAccountFunc((AidlAccountData)msg.obj,msg.arg1==1?true:false);
	 				break;
	 			case MSG_addPrivacyApp:
	 				addPrivacyAppFunc((MyArrayList<String>)msg.obj);
	 				break;
	 			case MSG_deletePrivacyApp:
	 				deletePrivacyAppFunc((MyArrayList<String>)msg.obj);
	 				break;
	 			case MSG_resetPrivacyNumOfAllAccount:
	 				resetPrivacyNumOfAllAccountFunc(iPrivacyManageService);
	 				break;
	 			case MSG_setPrivacyAppNumToPrivacyManage:
	 				setPrivacyAppNumToPrivacyManageFunc((MyArrayList<Long>)msg.obj,
	 						iPrivacyManageService);
	 				break;
	 			case MSG_bindServiceCallBack:
	 				bindServiceFunc((BindServiceCallback)msg.obj);
	 				break;
	        }
	    };
    }
    
	private void switchAccountFunc(AidlAccountData newAccountData){
		if(newAccountData == null){
			return ;
		}		
		if(getCurAccountId() == newAccountData.getAccountId()){
			return ;
		}	
		LogUtils.printWithLogCat(TAG,"switch Account id="+newAccountData.getAccountId());
		if(newAccountData.getAccountId() != mConfig.NORMAL_ACCOUNTID){
			enterPrivacySpace(newAccountData);
		}else{
			quitPrivacySpace(curAccountData);
		}		
		synchronized (curAccountData){
			curAccountData.setAccountId(newAccountData.getAccountId());
			curAccountData.setHomePath(newAccountData.getHomePath());
		}
		runWhenCurAccountInfoInited();		
		Message mUIhandlerMsg = mUIhandler.obtainMessage();
        mUIhandlerMsg.what = MSG_accountSwitch;
        mUIhandlerMsg.obj = curAccountData;
        mUIhandler.sendMessage(mUIhandlerMsg);	
	}
	
    private void deleteAccountFunc(AidlAccountData needDeleteAccount,boolean delete){
		if(needDeleteAccount == null || 
				needDeleteAccount.getAccountId() == mConfig.NORMAL_ACCOUNTID){
			return ;
		}
		if(getCurAccountId() != needDeleteAccount.getAccountId()){
			return ;
		}	
		LogUtils.printWithLogCat(TAG,"delete Account ,id="+needDeleteAccount.getAccountId());
		MyArrayList<String> curAccountPrivacyAppList = new MyArrayList<String>();
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue ;
			}
			if(privacyAppData.getAccountId() == needDeleteAccount.getAccountId()){
				curAccountPrivacyAppList.add(privacyAppData.getPkgName());
			}
		}		
		for(int i=0;i<curAccountPrivacyAppList.size();i++){
			if(delete){
				ApkUtils.SilenceUninstallApp(context, curAccountPrivacyAppList.get(i),null);
			}else{
				enableApp(ApkUtils.getApplicationInfo(context, curAccountPrivacyAppList.get(i)));
			}
		}
		deletePrivacyAppFunc(curAccountPrivacyAppList);		
		synchronized (curAccountData) {
			curAccountData.setAccountId(mConfig.NORMAL_ACCOUNTID);
			curAccountData.setHomePath("");
		}
		runWhenCurAccountInfoInited();
		Message mUIhandlerMsg = mUIhandler.obtainMessage();
        mUIhandlerMsg.what = MSG_deleteAccount;
        mUIhandlerMsg.obj = needDeleteAccount;
        mUIhandler.sendMessage(mUIhandlerMsg);	
	}
    
	private void addPrivacyAppFunc(MyArrayList<String> pkgList){
		if(pkgList == null){
			return ;
		}
		ArrayList <PrivacyAppData> addPrivacyAppList = new ArrayList <PrivacyAppData>();
		MyArrayList<Long> needUpdateAccountIds = new MyArrayList<Long>();
		for(int i=0;i<pkgList.size();i++){
			String pkgName = pkgList.get(i);
			if(pkgName == null){
				continue ;
			}
			if(PrivacyAppProvider.insertOrUpdateDate(context, 
					getCurAccountId(), pkgName)){				
				PrivacyAppData privacyAppData = new PrivacyAppData();
				privacyAppData.setAccountId(getCurAccountId());
				privacyAppData.setPkgName(pkgName);
				
				privacyAppList.add(privacyAppData);
				addPrivacyAppList.add(privacyAppData);
				addForNeedUpdateAccountIds(needUpdateAccountIds,privacyAppData.getAccountId());
			}
		}
		
		if(needUpdateAccountIds.size()>0){
			setPrivacyAppNumToPrivacyManage(needUpdateAccountIds);
		}
		
		if(addPrivacyAppList.size()>0){			
			Message mUIhandlerMsg = mUIhandler.obtainMessage();
	        mUIhandlerMsg.what = MSG_addPrivacyApp;
	        mUIhandlerMsg.obj = addPrivacyAppList;
	        mUIhandler.sendMessage(mUIhandlerMsg);	
		}
	}
	
	private synchronized void addForNeedUpdateAccountIds(MyArrayList<Long> list,long accountId){
		Long tmpAccountId;
		for(int i=0;i<list.size();i++){
			tmpAccountId = list.get(i);
			if(tmpAccountId == null){
				continue ;
			}else if(tmpAccountId.longValue() == accountId){
				return ;
			}
		}
		list.add(new Long(accountId));		
	}
	
	private void deletePrivacyAppFunc(MyArrayList<String> pkgList){
		if(pkgList == null){
			return ;
		}
		ArrayList <PrivacyAppData> deletePrivacyAppList = new ArrayList <PrivacyAppData>();
		MyArrayList<Long> needUpdateAccountIds = new MyArrayList<Long>();
		for(int i=0;i<pkgList.size();i++){
			String pkgName = pkgList.get(i);
			if(pkgName == null){
				continue ;
			}
			PrivacyAppProvider.deleteDate(context,pkgName);
			PrivacyAppData privacyAppData = findPrivacyAppData(pkgName);
			if(privacyAppData != null){
				privacyAppList.remove(privacyAppData);
				deletePrivacyAppList.add(privacyAppData);
				addForNeedUpdateAccountIds(needUpdateAccountIds,privacyAppData.getAccountId());
			}
		}
		
		if(needUpdateAccountIds.size()>0){
			setPrivacyAppNumToPrivacyManage(needUpdateAccountIds);
		}
		
		if(deletePrivacyAppList.size()>0){
			Message mUIhandlerMsg = mUIhandler.obtainMessage();
	        mUIhandlerMsg.what = MSG_deletePrivacyApp;
	        mUIhandlerMsg.obj = deletePrivacyAppList;
	        mUIhandler.sendMessage(mUIhandlerMsg);	
		}
	}
	
	private void setPrivacyAppNumToPrivacyManageFunc(
			final MyArrayList<Long> needUpdateAccountIds,
			IPrivacyManageService iPrivacyManageService){
		if(needUpdateAccountIds == null){
			return ;
		}
		if(iPrivacyManageService != null){
			try {
				for(int i=0;i<needUpdateAccountIds.size();i++){
					Long idObject = needUpdateAccountIds.get(i);
					if(idObject == null){
						continue ;
					}
					
					iPrivacyManageService.setPrivacyNum(
							Utils.getOwnPackageName(context),
							PrivacyAppActivity.class.getName(), 
							getAppointAccountPrivacyAppNum(idObject.longValue()),
							idObject.longValue());	
				}
			 } catch (RemoteException e) {
	                e.printStackTrace();
	         }
		}else{
			//表示服务未绑定，需要重新绑定
			bindService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					setPrivacyAppNumToPrivacyManage(needUpdateAccountIds);		
				}}
			);
		}
	}
	
	private void resetPrivacyNumOfAllAccountFunc(
			IPrivacyManageService iPrivacyManageService){		
		if(iPrivacyManageService != null){
			try {
				iPrivacyManageService.resetPrivacyNumOfAllAccount(
						Utils.getOwnPackageName(context),
						PrivacyAppActivity.class.getName());	
			 } catch (RemoteException e) {
	                e.printStackTrace();
	         }
		}else{
			//表示服务未绑定，需要重新绑定
			bindService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					resetPrivacyNumOfAllAccount();				
				}}
			);
		}
	}
	
	
	private  boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 20) {
			return true;
		}
		return false;
	}

	private Intent createExplicitFromImplicitIntent(Context context,
			Intent implicitIntent) {
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,
				0);

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
	
	
	private void bindServiceFunc(final BindServiceCallback bindCallback){
		try{
			boolean reslut = false;
			if(isLowVersion())
			{
				reslut = context.bindService(
				new Intent(IPrivacyManageService.class.getName()),connection, 
					Context.BIND_AUTO_CREATE);
			}else{
				reslut = context.bindService(
						createExplicitFromImplicitIntent(context,new Intent(IPrivacyManageService.class.getName())),connection, 
							Context.BIND_AUTO_CREATE);
				
			}
			if(reslut){
				synchronized (mLockJustForBindService) {
					if (null == iPrivacyManageService) {
						LogUtils.printWithLogCat(TAG,"bind wait");
						mLockJustForBindService.wait();
	                }
                }		
			}
			LogUtils.printWithLogCat(TAG,"bind result=" + reslut);
		}catch(Exception e){
			LogUtils.printWithLogCat(TAG,"bind result=" + e.toString());
			e.printStackTrace();
		}finally{					
			Message msg = mUIhandler.obtainMessage();
			msg.what = MSG_bindServiceCallBack;
			msg.obj = bindCallback;
			mUIhandler.sendMessage(msg); 
		}   
	}
	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) {  
 			switch (msg.what) {
 			case MSG_addPrivacyApp:
 				notifyObserversOfPrivacyAppAdd((ArrayList<PrivacyAppData>)msg.obj);
 				break;
 			case MSG_deletePrivacyApp:
 				notifyObserversOfPrivacyAppDelete((ArrayList<PrivacyAppData>)msg.obj);
 				break;
 			case MSG_bindServiceCallBack:
 				BindServiceCallback bindCallback =(BindServiceCallback)msg.obj;
				if(bindCallback != null){
		    		bindCallback.callback(isBindService());
		    	}
 				break;
 			case MSG_accountSwitch:
 				notifyObserversOfPrivacyAccountSwitch((AidlAccountData)msg.obj);
 				break;
 			case MSG_deleteAccount:
 				notifyObserversOfDeletePrivacyAccount((AidlAccountData)msg.obj);
 				break;
 	        }
 	    }
 	 }
	
	/**
	 * 进入隐私空间
	 * @param tmpAccountData
	 */
	private void enterPrivacySpace(AidlAccountData newAccountData){
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(privacyAppData.getAccountId() == newAccountData.getAccountId()){
				enableApp(ApkUtils.getApplicationInfo(context, privacyAppData.getPkgName()));
			}
		}						
	}
	
	/**
	 * 退出隐私空间
	 * @param tmpAccountData
	 */
	private void quitPrivacySpace(AidlAccountData oldAccountData){
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(privacyAppData.getAccountId() == oldAccountData.getAccountId()){
				disableApp(ApkUtils.getApplicationInfo(context, privacyAppData.getPkgName()));
			}
		}
	}
	
	/**
	 * 启用应用
	 */
	private void enableApp(ApplicationInfo applicationInfo){
		if(applicationInfo == null){
			return ;
		}
		new DisableChanger(context, applicationInfo,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                null)
         .execute((Object)null);
	}
	
	/**
	 * 停用应用
	 * @param applicationInfo
	 */
	private void disableApp(ApplicationInfo applicationInfo){
		if(applicationInfo == null){
			return ;
		}
		new DisableChanger(context, applicationInfo,
                 PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, null)
         .execute((Object)null);
	}
	
	/**
	 * 是否是当前隐私身份下的隐私应用
	 * @param pkgName
	 * @return 
	 */
	public boolean isCurAccountPrivacyApp(String pkgName){
		boolean result = false;
		if(pkgName == null){
			return result ;
		}
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(privacyAppData.getAccountId() == getCurAccountId() &&
					pkgName.equals(privacyAppData.getPkgName())){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 是否是非当前隐私身份下的隐私应用
	 * @param pkgName
	 * @return 
	 */
	public boolean isOtherAccountPrivacyApp(String pkgName){
		boolean result = false;
		if(pkgName == null){
			return result ;
		}
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(privacyAppData.getAccountId() != getCurAccountId() &&
					pkgName.equals(privacyAppData.getPkgName())){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 判断当前应用是不是隐私应用
	 * @param pkgName
	 * @return
	 */
	public boolean isPrivacyApp(String pkgName){
		boolean result = false;
		if(pkgName == null){
			return result ;
		}
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(pkgName.equals(privacyAppData.getPkgName())){
				result = true;
				break;
			}
		}
		return result;
	}		

	/**
	 * 获取指定账户下的隐私应用
	 * @param accountId
	 * @return
	 */
	private int getAppointAccountPrivacyAppNum(long accountId){
		int privacyAppNum = 0;
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(privacyAppData.getAccountId() == accountId &&
					ApkUtils.isAppInstalled(context, privacyAppData.getPkgName())){
				privacyAppNum++;
			}
		}
		return privacyAppNum;
	}
	
	private PrivacyAppData findPrivacyAppData(String pkgName){
		if(pkgName == null){
			return null;
		}
		for(int i=0;i<privacyAppList.size();i++){
			PrivacyAppData privacyAppData = privacyAppList.get(i);
			if(privacyAppData == null){
				continue;
			}
			if(pkgName.equals(privacyAppData.getPkgName())){
				return privacyAppData;
			}
		}
		return null;
	}
		
	/**
	 * 是否绑定了服务
	 * @return true：绑定成功  false：绑定失败
	 */
	public boolean isBindService(){
		if(null == iPrivacyManageService){
			return false;
		}else{
			return true;
		}	
	}
				
	private ServiceConnection connection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder service) {			
			synchronized (mLockJustForBindService){
				iPrivacyManageService = IPrivacyManageService.Stub.asInterface(service);
				mLockJustForBindService.notify();
				LogUtils.printWithLogCat(TAG,"notify");
		    }
			LogUtils.printWithLogCat(TAG,"connection privacy manage service");
			AidlAccountData tmpAccountData = null;
			try {
				if(iPrivacyManageService != null){
					tmpAccountData = iPrivacyManageService.getCurrentAccount(null,null);
					if(tmpAccountData != null){
						switchAccount(tmpAccountData);
					}else{
						tmpAccountData = new AidlAccountData();
						tmpAccountData.setAccountId(mConfig.NORMAL_ACCOUNTID);
						switchAccount(tmpAccountData);
					}
				}else{
					tmpAccountData = new AidlAccountData();
					tmpAccountData.setAccountId(mConfig.NORMAL_ACCOUNTID);
					switchAccount(tmpAccountData);
				}			
            } catch (RemoteException e) {
                e.printStackTrace();
            }			
		}

		public void onServiceDisconnected(ComponentName name) {
			try {
				//
            }finally{
            	LogUtils.printWithLogCat(TAG,"disconnected privacy manage service");
            	synchronized (mLockJustForBindService){
            		iPrivacyManageService = null;	
            		Log.e("jadon4", "222222222222");
            		mLockJustForBindService.notify();
            	}            	
            	
            	//回到正常账户
            	AidlAccountData newAccountData = new AidlAccountData();
            	newAccountData.setAccountId(mConfig.NORMAL_ACCOUNTID);
            	newAccountData.setHomePath("");
            	switchAccount(newAccountData,false);
            }					
		}
	};
	
	private void runWhenCurAccountInfoInited(){
		isCurAccountInfoInited.set(true);
	}
	
	/**
	 * 绑定服务的回调
	 */
	public interface BindServiceCallback {
		/**
		 * 是否绑定成功
		 * @param result  true：绑定成功  false：绑定失败
		 */
		public void callback(boolean result);
	}
	
	public static void relaseObject(){  	
    	if(instance != null){
    		instance.context = null;
    		instance = null;
    	}
	}
}
