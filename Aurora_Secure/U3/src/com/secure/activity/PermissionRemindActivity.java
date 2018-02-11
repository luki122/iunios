package com.secure.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.secure.R;
import com.secure.adapter.PermissionRemindAdapter;
import com.secure.data.AppInfo;
import com.secure.data.PermissionInfo;
import com.secure.data.PermissionRemindData;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PermissionSubject;
import com.secure.model.ConfigModel;
import com.secure.model.PermissionRemindModel;
import com.secure.provider.AppInfosProvider;
import com.secure.provider.PermissionProvider;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.mConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionRemindActivity extends AuroraActivity implements 
                                 OnClickListener,OnItemClickListener,PermissionObserver{	
	private PermissionRemindAdapter adapter;
	private PermissionRemindData perRemindData;
	private List<PermissionInfo> permissionInfoList;	
	private ListView listView;	
	private AppInfo appInfo;
	private String packageName;
	private AtomicBoolean isDuringGetAppInfo = new AtomicBoolean(false);
	private AtomicBoolean idDuringSavePerRemindData = new AtomicBoolean(false);
	private UIHandler mUIhandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (mConfig.isNative) {
            setContentView(R.layout.permission_remind_view);
        } else {
            setAuroraContentView(R.layout.permission_remind_view, AuroraActionBar.Type.Empty);
            getAuroraActionBar().setVisibility(View.GONE);
        }
        
  		if (getIntent().getExtras() != null) {
  			packageName = getIntent().getExtras().getString("packageName");
  		}
        if(packageName == null){
        	finish();
        	return;
        }

    	mUIhandler = new UIHandler(Looper.getMainLooper());	
		listView = (ListView)findViewById(R.id.ListView);
		findViewById(R.id.sureBtn).setOnClickListener(this);
   		PermissionSubject.getInstance().attach(this); 	
   		isDuringGetAppInfo.set(false);
   		idDuringSavePerRemindData.set(false);
   		startThreadForGetAppInfo();
    }
    
    @Override
 	protected void onRestart() {
    	startThreadForGetAppInfo();
 		super.onRestart();
 	}
    
    @Override
    protected void onResume() {
        super.onResume();

        changeStatusBar(true);
    }
    
    private void startThreadForGetAppInfo(){
    	if(isDuringGetAppInfo.get()){
    		return ;
    	}
    	isDuringGetAppInfo.set(true);
    	new Thread(){
			@Override
			public void run() {
		        perRemindData = PermissionRemindModel.getInstance(
		        		PermissionRemindActivity.this).getPermissionRemindData(packageName);
		   		if(perRemindData != null && perRemindData.getVersionCode() == 
		   				ApkUtils.getApkVersionCode(PermissionRemindActivity.this, packageName)){
		   			LogUtils.printWithLogCat(PermissionRemindActivity.class.getName(), 
		   					"this app and this version is showed ");
		           	finish();
		            isDuringGetAppInfo.set(false);
		   			return ;
		   		}
		   		
		        getAppInfo();
		        if(appInfo == null){
		        	finish();
		        }else{
		        	if(appInfo.getAppName() == null){
		        		ApkUtils.initAppNameInfo(PermissionRemindActivity.this, appInfo);
		        	}		        	
		        	mUIhandler.sendEmptyMessage(0);
		        }
		        isDuringGetAppInfo.set(false);
				super.run();
			}   		
    	}.start();
    }
    
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
		@Override
		public void handleMessage(Message msg) {			
			initOrUpdateView();	
			super.handleMessage(msg);
		}    	
    };
	  
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sureBtn:
			savePerRemindData();
			finish();
			break;
		}	
	}
	
	@Override
	protected void onDestroy() {
		PermissionSubject.getInstance().detach(this);
		if(adapter != null){
			adapter.realseObject();
		}
		if(permissionInfoList != null){
			permissionInfoList.clear();
		}
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(adapter == null){
			return ;
		}	
		adapter.dealItemClick(arg2);		
	}
	
	public AppInfo getCurAppInfo(){
		return this.appInfo;
	}
	
	private void savePerRemindData(){
		if(idDuringSavePerRemindData.get()){
    		return ;
    	}
		idDuringSavePerRemindData.set(true);
    	new Thread(){
			@Override
			public void run() {
				if(appInfo == null){
					idDuringSavePerRemindData.set(false);
					return ;
				}
				if(perRemindData == null){
					perRemindData = new PermissionRemindData();
				}
				perRemindData.setPackageName(appInfo.getPackageName());
				perRemindData.setVersionCode(ApkUtils.getApkVersionCode(
						PermissionRemindActivity.this, 
						appInfo.getPackageName()));
				ArrayList<Integer> perIdList = new ArrayList<Integer>();
				int size = appInfo.getPermission()==null?0:appInfo.getPermission().size();
		    	for(int i=0;i< size;i++){
		    		PermissionInfo tmpPermissionInfo = appInfo.getPermission().get(i);
		    		if(tmpPermissionInfo == null){
		    			continue;
		    		}
		    		perIdList.add(new Integer(tmpPermissionInfo.permId));
		    	}
		    	perRemindData.setPerIdList(perIdList);
		    	PermissionRemindModel.getInstance(PermissionRemindActivity.this).
		    	     addOrModifyPermissionRemindData(perRemindData);
		    	idDuringSavePerRemindData.set(false);
		        super.run();
			}   		
    	}.start();
	}

	@Override
	public void updateOfPermsStateChange(AppInfo appInfo,
			PermissionInfo permissionInfo) {
		if(appInfo == null|| adapter == null){
			return ;
		}
		if(appInfo.getPackageName().equals(packageName)){
			adapter.updateOfPermState(permissionInfo);
		}	
	}
	
    private void getAppInfo(){
  		if(StringUtils.isEmpty(packageName)){
  			return;
  		}
		ConfigModel instance = ConfigModel.getInstance();
		if(instance != null){
			appInfo = instance.getAppInfoModel().findAppInfo(packageName);
		}
		if(appInfo == null){
			appInfo = AppInfosProvider.getAppsInfo(this, packageName);
	        if(appInfo != null){
	       	  PermissionProvider.queryAppPerm(this, appInfo); 
	        }else{
	    	   appInfo = ApkUtils.getAppFullInfo(this, packageName);
	    	   AppInfosProvider.insertOrUpdateDate(this, appInfo);
               PermissionProvider.insertOrUpdateDate(this, appInfo);
               AppInfosProvider.notifyChangeForNetManageApp(this,appInfo);	
	        }
		}
    }
    
    private void initOrUpdateView(){
    	if(appInfo == null){
    		return ;
    	}
    	if(permissionInfoList == null){
    		permissionInfoList = new ArrayList<PermissionInfo>();
    	}else{
    		permissionInfoList.clear();
    	}  	   	
    	int size = appInfo.getPermission()==null?0:appInfo.getPermission().size();
    	for(int i=0;i< size;i++){
    		PermissionInfo tmpPermissionInfo = appInfo.getPermission().get(i);
    		if(tmpPermissionInfo == null){
    			continue;
    		}
    		if(perRemindData != null && 
    				perRemindData.isHavePermission(tmpPermissionInfo.permId)){
    			continue;
    		}
    		permissionInfoList.add(tmpPermissionInfo);
    	}
    	
    	if(permissionInfoList == null || permissionInfoList.size() == 0){
			LogUtils.printWithLogCat(PermissionRemindActivity.class.getName(), 
					"new app or new version have no new permission");
			savePerRemindData();
        	finish();
			return ;
		}
	
		TextView permissionHintText = (TextView)findViewById(R.id.permissionHintText);	
		permissionHintText.setText(appInfo.getAppName() + 
				getString(R.string.permission_hint_text));		
		
		if(adapter == null){
    		adapter = new PermissionRemindAdapter(this,appInfo.getPackageName(),permissionInfoList);
    		listView.setAdapter(adapter);
    		listView.setOnItemClickListener(this);
    	}else{
    		adapter.notifyDataSetChanged();
    	} 
	}
}