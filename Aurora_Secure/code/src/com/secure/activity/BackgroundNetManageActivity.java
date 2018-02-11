package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.netmanage.data.FlowData;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.BackgroundNetManageAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.Constants;
import com.secure.data.PermissionInfo;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.FlowChangeObserver;
import com.secure.interfaces.LBEServiceBindObserver;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PermissionSubject;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraNetManageModel;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.ConfigModel;
import com.secure.totalCount.TotalCount;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.utils.ApkUtils;
import com.secure.utils.ServiceUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.view.MyProgressDialog;

import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class BackgroundNetManageActivity extends AuroraActivity implements 
																Observer,
																OnItemClickListener,
																PermissionObserver,
																LBEServiceBindObserver,
																PrivacyAppObserver,
                                                                FlowChangeObserver{
	private List<BaseData> allowNetAppsList;//允许联网软件列表
	private List<BaseData> allAppsList;//所有软件列表
	private List<BaseData> prohibitNetAppsList;//禁止联网软件列表	
	private BackgroundNetManageAdapter adapter;
	private MyProgressDialog dialog = new MyProgressDialog();
	private ListView appListview;
	public long totalNetFlow;//总流量
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.background_net_manage_activity);
        }else{
        	setAuroraContentView(R.layout.background_net_manage_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.service_network_info);
        }
        PermissionSubject.getInstance().attach(this);
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);	
	    AuroraPrivacyManageModel.getInstance(this).attach(this);
	    AuroraNetManageModel.getInstance(this).attach(this);
	    ServiceUtils.startServiceIfNeed(this); 
	    initView();
        ActivityUtils.sleepForloadScreen(50,new LoadCallback(){
			@Override
			public void loaded() {
				initData();
			}       	
        });        
    }
    
    private void initData(){		    	
    	if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
    		updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
   	    }else{		    
   	   		dialog.show(this, 
   	   				getString(R.string.init), 
   	   				getString(R.string.please_wait_init));
   	    }
    }
    
    private void initView(){
    	appListview = (ListView)findViewById(R.id.appListview);
    }
    
    private void initOrUpdateTextView(){
    	TextView backgroundFlowText = (TextView)findViewById(R.id.backgroundFlowText);
    	TextView timeDuringText = (TextView)findViewById(R.id.timeDuringText);   	
    	if(AuroraNetManageModel.getInstance(this).isSetedFlowPackage()){
			timeDuringText.setVisibility(View.VISIBLE);
			long startTime =AuroraNetManageModel.getInstance(this).getFlowBeginTime();
	    	long endTime = TimeUtils.getCurrentTimeMillis();
			
	    	timeDuringText.setText(getString(R.string.time_during)+
	    			getString(R.string.colon)+
	    			TimeUtils.timeStampToMonth(startTime)+"."+
	    			TimeUtils.timeStampToDay(startTime)+"-"+
	    			TimeUtils.timeStampToMonth(endTime)+"."+
	    			TimeUtils.timeStampToDay(endTime));
		}else{
			timeDuringText.setVisibility(View.GONE);
		}
    	   	
    	backgroundFlowText.setText(
    			Utils.dealMemorySize(this,getTotalNetFlow()) +
    			getString(R.string.background_flow));
    }
    
    /**
     * 更新Apps数据
     */
    public void initOrUpdatetAppsData(){
    	if(dialog != null){
  		   dialog.close(); 
  	    }
    	
    	if(appListview == null){
    		return ;
    	}
    	
    	if(prohibitNetAppsList == null){
    		prohibitNetAppsList = new ArrayList<BaseData>();
    	}else{
    		prohibitNetAppsList.clear();
    	}
    	
    	if(allowNetAppsList == null){
    		allowNetAppsList = new ArrayList<BaseData>();
    	}else{
    		allowNetAppsList.clear();
    	}
    	
    	if(allAppsList == null){
    		allAppsList = new ArrayList<BaseData>();
    	}else{
    		allAppsList.clear();
    	}
    	totalNetFlow = 0;
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled()){
        			continue;
        		}
        		if(appInfo.getIsHaveNetworkingPermission()){
        			ApkUtils.initAppNameInfo(this, appInfo);
        			if(appInfo.getIsNetPermissionOpen()){
        				addData(allowNetAppsList,appInfo);
            		}else{
            			addData(prohibitNetAppsList,appInfo);
            		}
        		}      		
        	}
    	}	
    	
    	AppsInfo sysAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getSysAppsInfo();
    	if(sysAppsInfo != null){
    		for(int i=0;i<sysAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled()){
        			continue;
        		}
        		if(appInfo.getIsHaveNetworkingPermission()){
        			ApkUtils.initAppNameInfo(this, appInfo);
        			if(appInfo.getIsNetPermissionOpen()){
        				addData(allowNetAppsList,appInfo);
            		}else{
            			addData(prohibitNetAppsList,appInfo);
            		}
        		}  
        	}
    	}
    	sortByFlow(allowNetAppsList);
    	sortByFlow(prohibitNetAppsList);
    	
    	combineAppsList();
    	
    	if(adapter == null){
    		adapter = new BackgroundNetManageAdapter(this,allAppsList);
        	appListview.setAdapter(adapter);
        	appListview.setOnItemClickListener(this);
    	}else{
    		adapter.updateAppNetSwitchState();
    		adapter.notifyDataSetChanged();
    	}
    	
    	if(allAppsList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
    	}
    }
    
    /**
     * 将元素添加到列表中
     * @param appsList
     * @param appInfo
     */
    private void addData(List<BaseData> appsList,AppInfo appInfo){
    	if(appsList == null || appInfo == null){
    		return ;
    	}
    	
    	HashMap<String ,FlowData> flowMap =AuroraNetManageModel.getInstance(this).getFlowMap();
    	if(flowMap == null){
    		return ;
    	}else{
    		FlowData flowData = flowMap.get(appInfo.getPackageName());
    		if(flowData == null || flowData.getBackBytes() == 0){
    			return ;
    		}else{
    			appInfo.setApkTotalBackgroundNetBytes(flowData.getBackBytes());
    		}
    	} 	
    	totalNetFlow = totalNetFlow+appInfo.getApkTotalBackgroundNetBytes();
    	appsList.add(appInfo);
    }
    
    /**
     * 按照流量多少排序
     * @param appsList
     */
    private void sortByFlow(List<BaseData> appsList){
    	Collections.sort(appsList,new Comparator<BaseData>(){
 		   public int compare(BaseData s1, BaseData s2) {			   
 			   int result = 0;
 			   if(((AppInfo)s1).getApkTotalBackgroundNetBytes() > ((AppInfo)s2).getApkTotalBackgroundNetBytes()){
 				  result = -1;
 			   }else if(((AppInfo)s1).getApkTotalBackgroundNetBytes() <((AppInfo)s2).getApkTotalBackgroundNetBytes()){
 				  result = 1;
 			   }else{
 				  result = Utils.compare(((AppInfo)s1).getAppNamePinYin(),
 						  ((AppInfo)s2).getAppNamePinYin());
 			   }
 			   return result;
 		   }
 		});
    }
    
    /**
     * 将风险软件和普通软件合并到一起
     */
    private void combineAppsList(){
    	if(allAppsList == null || 
    			prohibitNetAppsList == null || 
    					allowNetAppsList == null){
    		return ;
    	}
    	
    	for(int i=0;i<prohibitNetAppsList.size();i++){
    		allAppsList.add(prohibitNetAppsList.get(i));
    	}
    	
    	for(int i=0;i<allowNetAppsList.size();i++){
    		allAppsList.add(allowNetAppsList.get(i));
    	}
    }
    
    /**
     * 获取禁止联网软件的个数
     * @return
     */
    public int getProhibitNetAppNum(){
    	if(prohibitNetAppsList == null){
    		return 0;
    	}else{
    		return prohibitNetAppsList.size();
    	}
    }
    
    /**
     * 总流量
     * @return
     */
    public long getTotalNetFlow(){
    	return totalNetFlow;
    }  

	@Override
	protected void onResume() {	
		super.onResume();
	}
	
	@Override
	protected void onPause() {			
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		AuroraNetManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		PermissionSubject.getInstance().detach(this);
		releaseObject();
		super.onDestroy();
	}
	
	@Override
	public void updateOfInit(Subject subject) {
    	initOrUpdatetAppsData();
		initOrUpdateTextView();
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
		initOrUpdateTextView();
	}
	
	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
		initOrUpdateTextView();
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
		initOrUpdateTextView();
	}
	
	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub	
	} 
	
	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		initOrUpdatetAppsData();		
		initOrUpdateTextView();			
	}

	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,
			List<String> pkgList) {
		initOrUpdatetAppsData();		
		initOrUpdateTextView();			
	}
	
	@Override
	public void updateOfPermsStateChange(AppInfo appInfo,
			PermissionInfo permissionInfo) {
		if(permissionInfo != null && 
				permissionInfo.permId == SDKConstants.PERM_ID_NETDEFAULT){
			new TotalCount(this, "16", 1).CountData();
			netPermsStateChange(appInfo,permissionInfo);
		}			
	}

	@Override
	public void LBEServiceBindStateChange(boolean isConnection) {
		initOrUpdatetAppsData();		
		initOrUpdateTextView();					
	}
	
    private void netPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo){
    	if(appListview == null  || adapter == null || 
    			appInfo == null || permissionInfo == null){
    		return ;
    	}

    	AppInfo findAppInfo;
    	if(permissionInfo.getIsOpen()){
    		findAppInfo = getAppInfo(prohibitNetAppsList,appInfo.getPackageName());
    		if(findAppInfo != null){
    			prohibitNetAppsList.remove(findAppInfo);	
    			allowNetAppsList.add(findAppInfo);
    			sortByFlow(allowNetAppsList);
    		}else{
    			return ;
    		}   		
    	}else{
    		findAppInfo = getAppInfo(allowNetAppsList,appInfo.getPackageName());
    		if(findAppInfo != null){
    			allowNetAppsList.remove(findAppInfo);	
    			prohibitNetAppsList.add(findAppInfo);
    			sortByFlow(prohibitNetAppsList);
    		}else{
    			return ;
    		} 
    	}
    	allAppsList.clear();
    	combineAppsList();   	   	
    	adapter.updateAppNetSwitchState();
		adapter.notifyDataSetChanged();
    }
    
    private AppInfo getAppInfo(List<BaseData> appList,String pkgName){
    	for(int i=0;i<appList.size();i++){
    		AppInfo appInfo = (AppInfo)appList.get(i);
    		if(appInfo.getPackageName().equals(pkgName)){
    			return appInfo;
    		}
    	}
    	return null;
    }
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){		
		if(allAppsList != null){
			allAppsList.clear();
			allAppsList = null;
		}
		adapter = null;
		appListview = null;
	}

	@Override
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		initOrUpdatetAppsData();		
		initOrUpdateTextView();		
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		initOrUpdatetAppsData();		
		initOrUpdateTextView();						
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(appListview == null){
			return ;
		}
		ListAdapter adapter = appListview.getAdapter();
		if(adapter != null && arg2<adapter.getCount()){
			AppInfo appInfo = (AppInfo)adapter.getItem(arg2);
			if(appInfo != null){
			   Intent intent = new Intent(this,AppDetailActivity.class);
			   intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
			   startActivity(intent);
			}
		}		
	}

	@Override
	public void updateOfFlowChange(HashMap<String, FlowData> flowMap) {
    	initOrUpdatetAppsData();  
		initOrUpdateTextView();
	}	
}
