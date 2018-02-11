package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.AdAppListAdapter;
import com.secure.adapter.PermissionManageAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.Constants;
import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.AdObserver;
import com.secure.interfaces.AdSubject;
import com.secure.interfaces.LBEServiceBindObserver;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AdScanModel;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

public class AdAppListActivity extends AuroraActivity implements OnClickListener,
                                                                       Observer,
                                                                       AdObserver,
                                                                       OnItemClickListener,
                                                                       PrivacyAppObserver{	
	private MyArrayList<BaseData> unBlockAdAppsList;
	private MyArrayList<BaseData> blockAdAppsList;
	private MyArrayList<BaseData> allAppsList;
	private AdAppListAdapter adapter;
	private ListView appListview;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setAuroraContentView(R.layout.ad_app_list_activity,
        		AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.ad_block);
        getAuroraActionBar().setmOnActionBarBackItemListener(
        		new OnAuroraActionBarBackItemClickListener (){
			@Override
			public void onAuroraActionBarBackItemClicked(int arg0) {
				onBackPressed();				
			}});
        initView();               
        initOrUpdatetAppsData();
        initOrUpdateNoAppLayout();
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        AuroraPrivacyManageModel.getInstance(this).attach(this);
	    AdScanModel.getInstance(this).attach(this);
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
 		super.onResume();
 	}
    
    private void initView(){
    	findViewById(R.id.adScanBtn).setOnClickListener(this);
    	findViewById(R.id.oneKeyBlockBtn).setOnClickListener(this);
    	appListview = (ListView)findViewById(R.id.appListview);
    }
    
    private void initOrUpdateNoAppLayout(){
    	ConfigModel configModel = ConfigModel.getInstance();
        if(configModel != null){
            AppsInfo userAppsInfo = configModel.getAppInfoModel().getThirdPartyAppsInfo();
            int size = userAppsInfo==null?0:userAppsInfo.size();
            if(size > 0){
            	findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
            }else{
            	findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
            }           
        }else{
        	findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 更新Apps数据
     */
    private void initOrUpdatetAppsData(){
    	if(appListview == null){
    		return ;
    	}
    	if(unBlockAdAppsList == null){
    		unBlockAdAppsList = new MyArrayList<BaseData>();
    	}else{
    		unBlockAdAppsList.clear();
    	}
    	
    	if(blockAdAppsList == null){
    		blockAdAppsList = new MyArrayList<BaseData>();
    	}else{
    		blockAdAppsList.clear();
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
	    			!AdScanModel.getInstance(this).isAdApp(appInfo.getPackageName())){
	    			continue;
	    		}
	    		ApkUtils.initAppNameInfo(this, appInfo);    		
	    		if(appInfo.getIsBlockAd()){
	    			blockAdAppsList.add(appInfo);
	    		}else {
	    			unBlockAdAppsList.add(appInfo);
	    		}  		
	    	}  
    		sortList(blockAdAppsList.getDataList());
    		sortList(unBlockAdAppsList.getDataList());
	    	combineAppsList();
    	}
    	
    	if(adapter == null){
    		adapter = new AdAppListAdapter(this,allAppsList.getDataList());
        	appListview.setAdapter(adapter);
        	appListview.setOnItemClickListener(this);
    	}else{
    		adapter.updateAppBlcokState();
    		adapter.notifyDataSetChanged();
    	} 
    	
    	if(allAppsList.size() == 0){
    		findViewById(R.id.NoAdAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.oneKeyBlockBtn).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAdAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
    		if(unBlockAdAppsList.size() == 0){
    			findViewById(R.id.oneKeyBlockBtn).setVisibility(View.GONE);
    		}else{
    			findViewById(R.id.oneKeyBlockBtn).setVisibility(View.VISIBLE);
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
    			unBlockAdAppsList == null || 
    			blockAdAppsList == null){
    		return ;
    	}
    	
    	for(int i=0;i<unBlockAdAppsList.size();i++){
    		allAppsList.add(unBlockAdAppsList.get(i));
    	}
    	
    	for(int i=0;i<blockAdAppsList.size();i++){
    		allAppsList.add(blockAdAppsList.get(i));
    	}
    }
    
    /**
     * 获取未拦截广告的应用个数
     * @return
     */
    public int getUnBlockAppNum(){
    	if(unBlockAdAppsList == null){
    		return 0;
    	}else{
    		return unBlockAdAppsList.size();
    	}
    }
    
    /**
     * 获取已经拦截广告的应用的个数
     * @return
     */
    public int getBlockedAppNum(){
    	if(blockAdAppsList == null){
    		return 0;
    	}else{
    		return blockAdAppsList.size();
    	}
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.adScanBtn:
			startActivity(new Intent(this,AdBlockActivity.class));
			overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,
	 					com.aurora.R.anim.aurora_activity_open_exit);
			break;
		case R.id.oneKeyBlockBtn:
			blockAllAdApp();
			new TotalCount(this, "25", 1).CountData();
			break;
		}		
	}
		
	/**
	 * 拦截所有未拦截的广告
	 */
	private void blockAllAdApp(){  	
    	final MyProgressDialog dialog = new MyProgressDialog();
		dialog.show(this, 
				getResources().getString(R.string.a_key_block),
				getResources().getString(R.string.during_block_all_apk));
	
	    final Handler handler = new Handler() {
		   @Override
		   public void handleMessage(Message msg) {
			   dialog.close();
			   InfoDialog.showToast(AdAppListActivity.this, 
                    		   getString(R.string.a_key_block_finish));
		   }
		};
		
		new Thread() {
			@Override
			public void run() {	
				List<BaseData> needBlockAdAppsList = new ArrayList<BaseData>();
				int size = unBlockAdAppsList==null?0:unBlockAdAppsList.size();		
				for(int i=0;i<size;i++){
					BaseData baseData = unBlockAdAppsList.get(i);
					if(baseData != null){
						needBlockAdAppsList.add(baseData);
					}					
				}

			    for(int i=0; i< needBlockAdAppsList.size();i++){
				   AppInfo appInfo = (AppInfo)needBlockAdAppsList.get(i);
				   AdScanModel.getInstance(AdAppListActivity.this).setSwitch(appInfo, true);					                
			    }
			   try{
				   Thread.sleep(500);  
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
				new TotalCount(this, "26", 1).CountData();
			   Intent intent = new Intent(this,AdDetailActivity.class);	
			   intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
			   startActivity(intent);
			}
		}		
	}
		
	@Override
	public void onBackPressed() {
		AdBlockActivity.myFinish();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		AdScanModel.getInstance(this).detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		if(unBlockAdAppsList != null){
			unBlockAdAppsList.clear();
		}		
		if(blockAdAppsList != null){
			blockAdAppsList.clear();
		}
		if(allAppsList != null){
			allAppsList.clear();
		}
		if(adapter != null){
			adapter.releaseObject();
		}
	}

	@Override
	public void updateOfInit(AdSubject subject) {
		initOrUpdatetAppsData();
	}

	@Override
	public void updateOfInStall(AdSubject subject, String pkgName) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfCoverInStall(AdSubject subject, String pkgName) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfUnInstall(AdSubject subject, String pkgName) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfExternalAppAvailable(AdSubject subject,
			List<String> pkgList) {
		initOrUpdatetAppsData();	
	}

	@Override
	public void updateOfExternalAppUnAvailable(AdSubject subject,
			List<String> pkgList) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfSwitchChange(AdSubject subject, String pkgName,
			boolean swtich) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfAdLibUpdate(AdSubject subject) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfManualUpdate(AdSubject subject) {
		initOrUpdatetAppsData();		
	}

	@Override
	public void updateOfInit(Subject subject) {
		initOrUpdateNoAppLayout();		
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		initOrUpdateNoAppLayout();			
	}

	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		initOrUpdateNoAppLayout();			
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		initOrUpdateNoAppLayout();			
	}

	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,
			List<String> pkgList) {
		initOrUpdateNoAppLayout();			
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
