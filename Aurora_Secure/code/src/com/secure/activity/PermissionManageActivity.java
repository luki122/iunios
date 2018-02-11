package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.PermissionManageAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.MyArrayList;
import com.secure.data.PermissionInfo;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.LBEServiceBindObserver;
import com.secure.interfaces.LBEServiceBindSubject;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PermissionSubject;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.ConfigModel;
import com.secure.totalCount.TotalCount;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.InfoDialog;
import com.secure.view.MyProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

public class PermissionManageActivity extends AuroraActivity implements OnClickListener,
                                                                       Observer,
                                                                       PermissionObserver,
                                                                       OnItemClickListener,
                                                                       LBEServiceBindObserver,
                                                                       PrivacyAppObserver{	
	private final int REQUEST_CODE_OF_PermissionDetailActivity =1;
	private MyArrayList<BaseData> needOptimizeAppsList;//需要优化的软件列表
	private MyArrayList<BaseData> ordinaryAppsList;//普通软件列表
	private MyArrayList<BaseData> allAppsList;//所有软件列表	
	private PermissionManageAdapter adapter;
	private ListView appListview;
	public static boolean isDuringOptimizeAllPermission = false;;
	private Object  OptimizeAllPermissionLock = new Object();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.permission_manage_activity);
        }else{
        	setAuroraContentView(R.layout.permission_manage_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.jurisdiction_manage);
        }
        isDuringOptimizeAllPermission = false;
        initView();        
        ActivityUtils.sleepForloadScreen(100,new LoadCallback(){
			@Override
			public void loaded() {
				initData(); 		
			}       	
        });
    }
    
    @Override
   	protected void onPause() {
       	if(appListview != null){
       		((AuroraListView)appListview).auroraOnPause();
       	}   	
   		super.onPause();
   	}
    
   	@Override
   	protected void onResume() {
   		if(appListview != null){
   			((AuroraListView)appListview).auroraOnResume();
   		}		
   		initOrUpdatetAppsData();
   		super.onResume();
   	}
    
    private void initView(){
    	findViewById(R.id.aKeyOptimizeBtn).setOnClickListener(this);
    	appListview = (ListView)findViewById(R.id.appListview);
    }
    
    private void initData(){
    	initOrUpdatetAppsData();
	    ConfigModel.getInstance(this).getAppInfoModel().attach(this);
	    AuroraPrivacyManageModel.getInstance(this).attach(this);
	    PermissionSubject.getInstance().attach(this); 	
	    LBEServiceBindSubject.getInstance().attach(this);
    }
    
    /**
     * 更新Apps数据
     */
    private void initOrUpdatetAppsData(){
    	if(appListview == null){
    		return ;
    	}
    	if(needOptimizeAppsList == null){
    		needOptimizeAppsList = new MyArrayList<BaseData>();
    	}else{
    		needOptimizeAppsList.clear();
    	}
    	
    	if(ordinaryAppsList == null){
    		ordinaryAppsList = new MyArrayList<BaseData>();
    	}else{
    		ordinaryAppsList.clear();
    	}
    	
    	if(allAppsList == null){
    		allAppsList = new MyArrayList<BaseData>();
    	}else{
    		allAppsList.clear();
    	}
    	  	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
	    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
	    		if(appInfo == null || 
	    				!appInfo.getIsInstalled() || 
	    				appInfo.getPermission() == null || 
	    				appInfo.getPermission().size() == 0){
	    			continue;
	    		}
	    		ApkUtils.initAppNameInfo(this, appInfo);	    		
	    		if(appInfo.getIsNeedOptimize()){
	    			needOptimizeAppsList.add(appInfo);
	    		}else {
	    			ordinaryAppsList.add(appInfo);
	    		}  		
	    	}
    		sortList(needOptimizeAppsList.getDataList());
    		sortList(ordinaryAppsList.getDataList());
	    	combineAppsList();
    	}
    	
    	if(adapter == null){
    		adapter = new PermissionManageAdapter(this,allAppsList.getDataList());
        	appListview.setAdapter(adapter);
        	appListview.setOnItemClickListener(this);
    	}else{
    		adapter.updateAppOptimizeState();
    		adapter.notifyDataSetChanged();
    	} 
    	
    	if(allAppsList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
    		TextView noAppTitle = (TextView)findViewById(R.id.noAppTitle);
    		TextView noAppText = (TextView)findViewById(R.id.noAppText);
    		if(ApkUtils.isLBEServiceConnect()){
    			noAppTitle.setText(R.string.no_danger_permission_app);
    			noAppText.setVisibility(View.GONE);
    		}else{
    			noAppTitle.setText(R.string.lbe_monitor_close_hint_title);
    			noAppText.setVisibility(View.VISIBLE);
    			noAppText.setText(R.string.lbe_monitor_close_hint_text);
    		}
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
    		if(needOptimizeAppsList.size() == 0){
    			findViewById(R.id.btnLayout).setVisibility(View.GONE);
    		}else{
    			findViewById(R.id.btnLayout).setVisibility(View.VISIBLE);
    		}
    	}
    }
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
    
    /**
     * 将风险软件和普通软件合并到一起
     */
    private void combineAppsList(){
    	if(allAppsList == null || 
    			needOptimizeAppsList == null || 
    			ordinaryAppsList == null){
    		return ;
    	}
    	
    	for(int i=0;i<needOptimizeAppsList.size();i++){
    		allAppsList.add(needOptimizeAppsList.get(i));
    	}
    	
    	for(int i=0;i<ordinaryAppsList.size();i++){
    		allAppsList.add(ordinaryAppsList.get(i));
    	}
    }
    
    /**
     * 获取需要优化软件的个数
     * @return
     */
    public int getVentureSoftNum(){
    	if(needOptimizeAppsList == null){
    		return 0;
    	}else{
    		return needOptimizeAppsList.size();
    	}
    }
    
    /**
     * 获取普通软件的个数
     * @return
     */
    public int getOrdinarySoftNum(){
    	if(ordinaryAppsList == null){
    		return 0;
    	}else{
    		return ordinaryAppsList.size();
    	}
    }
	
	@Override
	public void updateOfInit(Subject subject) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
	}
	
	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		initOrUpdatetAppsData();	
	}
	
	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		
		initOrUpdatetAppsData();
	} 
	
	@Override
	public void updateOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo) {
		synchronized (OptimizeAllPermissionLock) {
			if(isDuringOptimizeAllPermission){
				return ;
			}
		}
		initOrUpdatetAppsData();	
	} 
	
	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		initOrUpdatetAppsData();	
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		initOrUpdatetAppsData();					
	}
	
	@Override
	public void LBEServiceBindStateChange(boolean isConnection) {
		initOrUpdatetAppsData();			
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.aKeyOptimizeBtn:
			new TotalCount(this, "8", 1).CountData();
			optimizeAllPermission();					
			break;
		}		
	}
		
	/**
	 * 优化所有权限
	 */
	private void optimizeAllPermission(){  	
    	final MyProgressDialog dialog = new MyProgressDialog();
		dialog.show(this, 
				getResources().getString(R.string.a_key_optimize),
				getResources().getString(R.string.during_optimize_all_apk));
	
	    final Handler handler = new Handler() {
		   @Override
		   public void handleMessage(Message msg) {
			   synchronized (OptimizeAllPermissionLock) {
					isDuringOptimizeAllPermission = false;
			   }
			   dialog.close();
			   InfoDialog.showToast(PermissionManageActivity.this, 
                    		   getString(R.string.a_key_optimize_finish));
			   PermissionSubject.getInstance().notifyObserversOfPermsStateChangeFunc(null,null);
		   }
		};
		
		new Thread() {
			@Override
			public void run() {
				synchronized (OptimizeAllPermissionLock) {
					isDuringOptimizeAllPermission = true;
				}
				List<BaseData> appsList = new ArrayList<BaseData>();
				int size = needOptimizeAppsList==null?0:needOptimizeAppsList.size();		
				for(int i=0;i<size;i++){
					BaseData baseData = needOptimizeAppsList.get(i);
					if(baseData != null){
						appsList.add(baseData);
					}					
				}
				
				for(int i=0; i< appsList.size();i++){
					AppInfo appInfo = (AppInfo)appsList.get(i);					
					ApkUtils.OptimizeOneApkAllPermission(
							PermissionManageActivity.this,
							appInfo);
				}
			   try{
				   Thread.sleep(1000);  
			   }catch(Exception e){
				   // 
			   }		   
	           handler.sendEmptyMessage(1);   
			}
		}.start();  
    }
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(appListview == null){
			return ;
		}
		ListAdapter adapter = appListview.getAdapter();
		if(adapter != null && arg2<adapter.getCount()){
			AppInfo appInfo = (AppInfo)adapter.getItem(arg2);
			if(appInfo != null && !StringUtils.isEmpty(appInfo.getPackageName())){
				new TotalCount(this, "9", 1).CountData();
			   Intent intent = new Intent(this,PermissionDetailActivity.class);
			   intent.putExtra("packageName", appInfo.getPackageName());
			   startActivityForResult(intent, REQUEST_CODE_OF_PermissionDetailActivity);
			}
		}		
	} 
	
	@Override
	protected void onDestroy() {
		PermissionSubject.getInstance().detach(this);
		LBEServiceBindSubject.getInstance().detach(this);
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
        if(needOptimizeAppsList != null){
        	needOptimizeAppsList.clear();
        }
        if(ordinaryAppsList != null){
        	ordinaryAppsList.clear();
        }
        if(allAppsList != null){
        	allAppsList.clear();
        }
        
        if(adapter != null){
        	adapter.releaseObject();
        }
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
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		initOrUpdatetAppsData();					
	}
}
