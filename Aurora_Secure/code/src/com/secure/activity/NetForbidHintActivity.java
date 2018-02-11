package com.secure.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraCheckBox;

import com.aurora.secure.R;
import com.secure.data.AppInfo;
import com.secure.data.NetForbidHintData;
import com.secure.data.PermissionInfo;
import com.secure.model.ConfigModel;
import com.secure.model.NetForbidHintModel;
import com.secure.provider.AppInfosProvider;
import com.secure.provider.PermissionProvider;
import com.secure.service.WatchDogService;
import com.secure.utils.ApkUtils;
import com.secure.utils.NetworkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;

public class NetForbidHintActivity extends Activity implements OnClickListener{	
	private AppInfo appInfo;
	public static String curPackageName;
	public static NetForbidHintActivity instance;
	private boolean isWifiConnection = false;
	private int netState;
	private String packageName;
	private AtomicBoolean isDuringGetAppInfo = new AtomicBoolean(false);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        receiveData() ;
        instance = this;
        setContentView(R.layout.aurora_alert_dialog_internal);         
        ((TextView)findViewById(R.id.aurora_alertTitle)).setText(R.string.net_remind); 
  
        Button button1 = ((Button)findViewById(R.id.button1)); 
    	button1.setText(R.string.cancel); 
    	button1.setOnClickListener(this); 
    	
        Button button2 = ((Button)findViewById(R.id.button2));  
        button2.setText(R.string.sure); 
        button2.setOnClickListener(this);  
        
        isDuringGetAppInfo.set(false);
        startThreadForGetAppInfo();
    }
    
    @Override
	protected void onRestart() {
    	startThreadForGetAppInfo();
		super.onRestart();
	}
    
    private void startThreadForGetAppInfo(){
    	if(isDuringGetAppInfo.get()){
    		return ;
    	}
    	isDuringGetAppInfo.set(true);
    	new Thread(){
			@Override
			public void run() {
		        getAppInfo();
		        if(appInfo == null){
		        	finish();
		        }else{
		        	if(appInfo.getAppName() == null){
		        		ApkUtils.initAppNameInfo(NetForbidHintActivity.this, appInfo); 
		        	}	
		        	UiHandler.sendEmptyMessage(0);
		        }
		        isDuringGetAppInfo.set(false);
				super.run();
			}   		
    	}.start();
    }
    
    private Handler UiHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			updateView();
			super.handleMessage(msg);
		}
    	
    };
    
    private void updateView(){
    	curPackageName = appInfo.getPackageName();       
        netState = NetworkUtils.getNetState(this);
     	if(netState == NetworkUtils.NET_TYPE_WIFI){
     	    isWifiConnection = true; 
     		((TextView)findViewById(R.id.message)).setText(
     				String.format(getString(R.string.forbid_wifi_net_are_you_open),
     						appInfo.getAppName())); 
     	}else if(netState == NetworkUtils.NET_TYPE_MOBILE){
     		isWifiConnection = false;
     		((TextView)findViewById(R.id.message)).setText(
     				String.format(getString(R.string.forbid_2G_3G_net_are_you_open),
     						appInfo.getAppName()));  
     	}
    }
          
	private void receiveData() {
  		Bundle extras = getIntent().getExtras();
  		if (extras == null) {
  			return ;
  		}
  		packageName = extras.getString("packageName");
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
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.button1:
				finish();
				break;
			case R.id.button2:
				PermissionInfo permissionInfo; 
	        	if(isWifiConnection){
	        		permissionInfo = appInfo.getWifiPermissionInfo();
	        	}else{
	        		permissionInfo = appInfo.getNetPermissionInfo();
	        	}
				ApkUtils.openApkAppointPermission(this,appInfo, permissionInfo);		
				finish();
				break;
		}	
	}
	
	@Override
	protected void onDestroy() {
		AuroraCheckBox cb = (AuroraCheckBox)findViewById(R.id.checkBox);
    	if(cb.isChecked()){
    		NetForbidHintData netForbidHintData = NetForbidHintModel.
        			getInstance(this).getNetForbidHintData(curPackageName);
    		if(netForbidHintData == null){
    			netForbidHintData = new NetForbidHintData();
    			netForbidHintData.setPackageName(curPackageName);
    		}
    		if(netState == NetworkUtils.NET_TYPE_WIFI){
    			netForbidHintData.setNeedHintForWifi(false);
    		}else if(netState == NetworkUtils.NET_TYPE_MOBILE){
    			netForbidHintData.setNeedHintForSim(false);
    		}
    		NetForbidHintModel.getInstance(this).addOrModifyNetForbidHintData(netForbidHintData);
    	}
		curPackageName = null;
	    instance = null; 	
		super.onDestroy();
	}
}