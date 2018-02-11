package com.secure.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.lbe.security.sdk.SDKConnection;
import com.lbe.security.sdk.SDKException;
import com.lbe.security.sdk.SDKService;
import com.lbe.security.service.privacy.HIPSService;
import com.lbe.security.service.privacy.PackageFilter;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.lbe.security.service.sdkhelper.SDKHelper;
import com.secure.data.AppInfo;
import com.secure.data.NetForbidHintData;
import com.secure.data.PermissionInfo;
import com.secure.provider.PermissionProvider;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;

public class LBEmodel {
    private static final String TAG = "LBEmodel";
    
	private static LBEmodel instance;
	private Context context;
	private MyConnection lbeConnection;
	private HIPSService hips = null;
	private SDKHelper helper = null;
	private AtomicBoolean isDuringBindService = new AtomicBoolean(false);
	private MyHandler handler;
	
	private LBEmodel(Context context) {
		this.context = context.getApplicationContext();	
		//下面代码必须指定UI线程的Looper，防止如果该类定义在子线程内没有Looper而出错
		handler = new MyHandler(Looper.getMainLooper());
	}

	/**
	 * 必须在UI线程中初始化
	 * 绑定LBE服务是在UI线程中，所以LBEmodel的初始化应该放在UI线程中，这样才会保证在服务绑定成功以前都是堵塞的，
	 * 如果LBEmodel的初始化放在子线程中，就会出现服务未绑定成功，就执行完了LBEmodel的初始化，这样在LBEmodel后续调用中就会出问题
	 * @param context
	 * @return
	 */
	public static synchronized LBEmodel getInstance(Context context) {
		if (instance == null) {
			instance = new LBEmodel(context);
		}
		return instance;
	}
	
	public static synchronized LBEmodel getInstance() {
		return instance;
	}
	
	/**
	 * 打开某个应用中的某个权限
	 * @param packageName
	 * @param permInfo 权限id （例如：SDKConstants.PERM_ID_NETWIFI）
	 * @return true:修改成功  false：修改失败
	 */
	public boolean openPermission(String packageName,PermissionInfo permInfo){
		return changePermissionState(packageName,permInfo,SDKConstants.ACTION_ACCEPT);
	}
	
	/**
	 * 将某个应用中的权限设置成询问
	 * @param packageName
	 * @param permInfo 权限id （例如：SDKConstants.PERM_ID_NETWIFI）
	 * @return true:修改成功  false：修改失败
	 */
	public boolean promptPermission(String packageName,PermissionInfo permInfo){
		return changePermissionState(packageName,permInfo,SDKConstants.ACTION_PROMPT);
	}
	
	/**
	 * 关闭某个应用中的某个权限
	 * @param packageName
	 * @param permId 权限id （例如：SDKConstants.PERM_ID_NETWIFI）
	 * @return true:修改成功  false：修改失败
	 */
	public boolean closePermission(String packageName,PermissionInfo permInfo){
	     return changePermissionState(packageName,permInfo,SDKConstants.ACTION_REJECT);
	}
	
	private PermissionInfo getSENDMMSPermissionInfo(String packageName){
		if(packageName == null)
		{
			return null;
		}
		ArrayList<PermissionInfo> infos = PermissionProvider.getMMSPermissonsByPackageName(context, packageName);
		if(infos == null || infos.size() == 0)
		{
			return null;
		}
		
		for(int i = 0; i<infos.size();i++)
		{
			if(infos.get(i).permId == SDKConstants.PERM_ID_SENDMMS)
			{
				return infos.get(i);
			}
		}
		return null;
	}
	
	
	/**
	 * 关闭打开或关闭某个应用中的某个权限
	 * @param packageName
	 * @param permId 权限id 权限id （例如：SDKConstants.PERM_ID_NETWIFI）
	 * @param state 开启或关闭的状态
	 * @return true:修改成功  false：修改失败
	 */
	private boolean changePermissionState(String packageName,PermissionInfo permInfo,int state) {
		boolean result = false;
		HIPSService hips = this.hips;
		if(StringUtils.isEmpty(packageName) || hips == null){
			return result;
		}	
		try {
			PackageFilter filter = new PackageFilter(packageName,null,null,true,null);
			List<com.lbe.security.bean.Package> pkgList = hips.queryPackage(filter);
			for (com.lbe.security.bean.Package pkg : pkgList) {
				//add by zw for changer trust list to false list 
				if(pkg.isTrust())
				{
					hips.trustPackage(pkg, false);
				}
				//end by zw
				hips.setPackagePermission(pkg, permInfo.permId, state);			
				if(getPermissionState(packageName, permInfo) == state){
					result = true;				
				}
			}
			
			PermissionProvider.UpdatePermState(context, packageName, permInfo);		
			
			if(permInfo.permId == SDKConstants.PERM_ID_NETWIFI){
				NetForbidHintData netForbidHintData = NetForbidHintModel.
	        			getInstance(context).getNetForbidHintData(packageName);
	    		if(netForbidHintData != null && !netForbidHintData.getNeedHintForWifi()){
	    			netForbidHintData.setNeedHintForWifi(true);
	    			NetForbidHintModel.getInstance(context).addOrModifyNetForbidHintData(netForbidHintData);
	    		} 			    			
			}else if(permInfo.permId == SDKConstants.PERM_ID_NETDEFAULT){
				NetForbidHintData netForbidHintData = NetForbidHintModel.
	        			getInstance(context).getNetForbidHintData(packageName);
	    		if(netForbidHintData != null && !netForbidHintData.getNeedHintForSim()){
	    			netForbidHintData.setNeedHintForSim(true);
	    			NetForbidHintModel.getInstance(context).addOrModifyNetForbidHintData(netForbidHintData);
	    		} 

				context.getContentResolver().notifyChange(PermissionProvider.CONTENT_URI, null);
			}
		} catch (Exception e) {
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"error="+e.toString());
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 判断某个应用中某个权限是都开启
	 * @param packageName
	 * @param permId 权限id （例如：SDKConstants.PERM_ID_NETWIFI）
	 * @return true：开启  false：关闭 
	 */
	public boolean isPermissionOpen(String packageName,PermissionInfo permInfo){
		boolean result = false;
		int state = getPermissionState(packageName,permInfo);
		if(state == SDKConstants.ACTION_REJECT){
			result = false;
		}else{
			result = true;
		}
		return result;
	}
	
	/**
	 * 获取指定应用，指定权限的状态
	 * @param packageName
	 * @param permInfo
	 * @return
	 */
	public int getPermissionState(String packageName,PermissionInfo permInfo){
		int state = SDKConstants.ACTION_ACCEPT;
		HIPSService hips = this.hips;
		SDKHelper helper = this.helper;
		if(StringUtils.isEmpty(packageName) || 
				permInfo == null || 
				hips == null ||
				helper == null){
			return state;
		}
		try {
			PackageFilter filter = new PackageFilter(packageName,null,null,true,null);
			List<com.lbe.security.bean.Package> pkgList = hips.queryPackage(filter);
			for (com.lbe.security.bean.Package pkg : pkgList) {			
				state = helper.getAction(pkg, permInfo.permId);
				permInfo.setCurState(state);
				LogUtils.printWithLogCat(
						LBEmodel.class.getName(),
						"check: packageName="+packageName+"; permId="
								+permInfo.permId+"; state="+state);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return state;
	}
	
	/**
	 * 获取某一个应用申请的权限
	 * @param hips
	 * @param helper
	 * @param pkgName
	 * @return 返回值不可能为null
	 */
	public ArrayList<PermissionInfo> getPermList(String pkgName){	
		ArrayList<PermissionInfo> permList = new ArrayList<PermissionInfo>();
		HIPSService hips = this.hips;
		SDKHelper helper = this.helper;
		if(hips == null || 
				helper == null || 
				StringUtils.isEmpty(pkgName) ){
			return permList;
		}
		
		if(!ApkUtils.isSystemApp(ApkUtils.getApplicationInfo(context, pkgName)))
		{
			PermissionInfo info = getSENDMMSPermissionInfo(pkgName);
			if(info == null)
			{
				info = new PermissionInfo();
				info.permId = SDKConstants.PERM_ID_SENDMMS;
				info.setCurState(SDKConstants.ACTION_PROMPT);
				changePermissionState(pkgName, info, SDKConstants.ACTION_PROMPT);
			}else{
				getPermissionState(pkgName, info);
			}
			permList.add(info);
		}
		PackageFilter filter = new PackageFilter(pkgName,null,null,true,null);
		List<com.lbe.security.bean.Package> pkgList1 = hips.queryPackage(filter);
		LogUtils.printWithLogCat(
				LBEmodel.class.getName(),
				"pkgName:"+pkgName+"; func:getPermList()");
		try {
			List<com.lbe.security.bean.Package> pkgList = 
					hips.queryPackage(new PackageFilter(pkgName, null, null, true, null));
			if((null == pkgList) || (pkgList.size() == 0)){
				LogUtils.printWithLogCat(
						LBEmodel.class.getName(),
						"pkgName:"+pkgName+"; func:getPermList()"+"; can not find pkgInfo");
			} else {
				com.lbe.security.bean.Package pkg = pkgList.get(0);
				List<Integer> permIntegerList = helper.getPermissions(pkg);
				int size = permIntegerList == null ? 0 : permIntegerList.size();		
				for(int i =0;i<size;i++){
					int permissionId = permIntegerList.get(i);
					if(permissionId != SDKConstants.PERM_ID_SENDMMS /**&& permissionId != SDKConstants.PERM_ID_NETDEFAULT && permissionId != SDKConstants.PERM_ID_NETWIFI && 
							permissionId != SDKConstants.PERM_ID_MOBILE_CONNECTIVITY && permissionId != SDKConstants.PERM_ID_WIFI_CONNECTIVITY**/)//因为上面添加过彩信权限，所以这里不做添加
					{
						PermissionInfo tmp = new PermissionInfo();
						tmp.permId = permIntegerList.get(i);
						tmp.setCurState(helper.getAction(pkg, tmp.permId));
						permList.add(tmp);
					}
				}
			}
		} catch (Exception e) {
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"pkgName:"+pkgName+"; func:getPermList()"+"; error:"+e.toString());
			e.printStackTrace();
		}
		return permList;
	}
	
	/**
	 * 获取某个权限对应的权限描述
	 * @param perm
	 * @return
	 */
	public String getPermissionDesc(Integer permId){
		String permDesc = "";
		SDKHelper helper = this.helper;
		try {
			if(helper != null){
				permDesc = helper.getPermissionDesc(permId);
			}		
		} catch (SDKException e) {
			e.printStackTrace();
		}
		return permDesc;		
	}
	
	/**
	 * 获取某个权限对应的权限名
	 * @param perm
	 * @return
	 */
	public String getPermName(Integer perm){
		String permName = "";
		SDKHelper helper = this.helper;
		try {
			if(helper != null){
				permName = helper.getPermissionName(perm);
			}			
		} catch (SDKException e) {
			e.printStackTrace();
		}
		return permName;		
	}
	
	/**
	 * 获取拥有联网权限应用包名列表（仅包含联网权限，包含系统组件）
	 */
	public List<com.lbe.security.bean.Package> getHaveNetworkPkgList(){
		HIPSService hips = this.hips;
		if(hips == null){
			return null;
		}
		
		hips.setFilterSystemPackage(false);		
		return hips.queryPackage(
				new PackageFilter(null, null, null, true, SDKConstants.PERM_FILTER_NETWORK));
	}
	
	/**
	 * 判断当前应用是不是有联网权限
	 * @param packageName
	 * @return
	 */
	public boolean isHaveNetworkApp(String packageName){
		HIPSService hips = this.hips;
		if(packageName == null || hips == null){
			return false;
		}
		
		List<com.lbe.security.bean.Package> pkgList = hips.queryPackage(
				new PackageFilter(packageName, null, null, true, SDKConstants.PERM_FILTER_NETWORK));
		if(pkgList == null || pkgList.size() == 0){
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"pkgName:"+packageName+"; not have network");
			return false;
		}else{
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"pkgName:"+packageName+"; have network");
			return true;
		}
	}
		
	public static void resetObject(){
		if(instance != null){
			if(instance.lbeConnection != null){
				try{
					SDKService.unbindSDKService(instance.lbeConnection);
				}catch(Exception e){
					e.printStackTrace();
				}		
			}
			instance = null;
		}
	}
	
/***********************绑定LBE服务的操作******************************/	
	private boolean isHIPSEnabled = true;
	private Object HIPSEnabledLocked = new Object();
	
	/**
	 * 关闭LBE的权限管理功能，让LBE安全大师可以正常使用
	 */
	public void closeLBEFunc(){
		SDKHelper helper = this.helper;
		if(helper == null){
			return ;
		}
		synchronized (HIPSEnabledLocked) {
			helper.setHIPSEnabled(false);
			isHIPSEnabled = false;
		}		
	}
	
	/**
	 * 打开LBE的权限管理功能
	 */
	public void openLBEFunc(){
		SDKHelper helper = this.helper;
		if(helper == null){
			return ;
		}
		synchronized (HIPSEnabledLocked) {
			helper.setHIPSEnabled(true);
			isHIPSEnabled = true;
		}		
	}
	
	/**
	 * 判断LBE的权限管理是否打开
	 * @return
	 */
	public boolean isOpenedLBEFunc(){
		if(helper == null){
			return false;
		}
//		return helper.getHIPSEnabled(); 这个函数太耗时，所以不能这样获取
		synchronized (HIPSEnabledLocked){
			return isHIPSEnabled;
		}
	}
	
	/**
	 * 是否绑定了LBE服务
	 * @return true：绑定成功  false：绑定失败
	 */
	public boolean isBindLBEService(){
		if(hips == null){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 判断授权管理应用是否可用
	 * @return
	 */
	public boolean isAuthorizationMnagementEnable(){
		ApplicationInfo curApplicationInfo = ApkUtils.getApplicationInfo(context,
				mConfig.LBE_PERMISSION_MANAGE_PKG);
		if(curApplicationInfo == null){
			return false;
		}		
		return curApplicationInfo.enabled;
	}
	
	/**
	 * 绑定LBE服务
	 */
	public void bindLBEService(final BindServiceCallback bindCallback){	
		if(isDuringBindService.get()){
			return ;
		}
		isDuringBindService.set(true);
			
		lbeConnection = new MyConnection(context,mConfig.LBE_SERVICE_ID);
		new Thread() {
			@Override
			public void run() {	
				try{				
					boolean reslut = SDKService.bindSDKService(lbeConnection);
					if(reslut){
						//因为绑定是一个比较耗时的操作，所以在此堵塞，直到与服务连接成功
						synchronized (LBEmodel.this) {
							if (hips == null) {
								LogUtils.printWithLogCat(
										LBEmodel.class.getName(),
										"wait");
								LBEmodel.this.wait();
			                }
		                }		
					}
					
					LogUtils.printWithLogCat(
							LBEmodel.class.getName(),
							"bind result=" + reslut);
				}catch(Exception e){
					LogUtils.printWithLogCat(
							LBEmodel.class.getName(),
							"bind result=" + e.toString());
					e.printStackTrace();
				}finally{					
					Message msg = new Message();
					msg.obj = bindCallback;
					handler.sendMessage(msg); 
				}       
			}
		}.start();		
	}
	
	private class MyHandler extends Handler{		
		public MyHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) { 
			if(msg != null){
				BindServiceCallback bindCallback =(BindServiceCallback)msg.obj;
				if(bindCallback != null){
		    		bindCallback.callback(isBindLBEService());
		    	}
			}
			isDuringBindService.set(false);
	    }
	}
	
	private class MyConnection extends SDKConnection {
		public MyConnection(Context context, String sdkAppName) {
			super(context, sdkAppName);
		}
		
		@Override
		public void OnSDKConnected(SDKService service) {
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"SDK Connected");
			if(service != null){
				hips = service.getHIPSService();
				helper = service.getSDKHelper();
				
				synchronized (HIPSEnabledLocked){
					if(helper != null){
						isHIPSEnabled = helper.getHIPSEnabled();
					}else{
						isHIPSEnabled = false;
					}				
				}
			}
			
			synchronized (LBEmodel.this){
				LBEmodel.this.notify();
				LogUtils.printWithLogCat(
						LBEmodel.class.getName(),
						"notify");
		    }	
		}

		@Override
		public void OnSDKDisconnected() {
			LogUtils.printWithLogCat(
					LBEmodel.class.getName(),
					"SDK Disconnected");
			hips = null;
			helper = null;
		}
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
}
