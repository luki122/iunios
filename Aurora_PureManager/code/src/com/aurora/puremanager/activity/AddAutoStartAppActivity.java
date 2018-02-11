package com.aurora.puremanager.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.AddAutoStartAppAdapter;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.utils.ActivityUtils;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.utils.ActivityUtils.LoadCallback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

public class AddAutoStartAppActivity extends AuroraActivity{	
	private List<BaseData> closedAutoStartAppList;	
	private AddAutoStartAppAdapter adapter;
	private ListView ListView;	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.add_auto_start_activity);
        }else{
        	setAuroraContentView(R.layout.add_auto_start_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.add_auto_start_app);
        }
        initView();
        ActivityUtils.sleepForloadScreen(100,new LoadCallback(){
			@Override
			public void loaded() {
				AutoStartModel.getInstance(AddAutoStartAppActivity.this).attach(updateViewHandler);
				updateViewHandler.sendEmptyMessage(0);	
			}       	
        });  
    }
    
    @Override
 	protected void onPause() {
     	if(ListView != null){
     		((AuroraListView)ListView).auroraOnPause();
     	}   	
 		super.onPause();
 	}
  
 	@Override
 	protected void onResume() {
 		if(ListView != null){
 			((AuroraListView)ListView).auroraOnResume();
 		}		
 		super.onResume();
 	}
       
    private void initView(){	
    	ListView = (ListView)findViewById(R.id.ListView);
    }
    
    private final Handler updateViewHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		initOrUpdatetListData();
	    }
    };
    
    /**
     * 
     * Vulcan created this method in 2015年1月13日 下午4:08:21 .
     */
    public void initOrUpdateListData2() {

    	if(ListView == null){
    		return ;
    	}
    	if(closedAutoStartAppList == null){
    		closedAutoStartAppList = new ArrayList<BaseData>();
    	}else{
    		closedAutoStartAppList.clear();
    	}
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo == null){
    		return ;
    	}
    	
    	//HashMap<String, Boolean> listHasAutoStart = AutoStartModel.getInstance(this).getListHasAutoStart();
    	
    	HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(getApplicationContext());
    	
    	for(int i=0;i<userAppsInfo.size();i++){
    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
    		if(appInfo == null || !appInfo.getIsInstalled()){
    			continue;
    		}
    		
    		AutoStartData autoStartData = AutoStartModel.getInstance(this).
    				getAutoStartData(appInfo.getPackageName());
    		if(autoStartData == null){
    			continue ;
    		}
    		
    		if(!autostartApps.contains(appInfo.getPackageName())) {
				ApkUtils.initAppNameInfo(this, appInfo);
				closedAutoStartAppList.add(appInfo);
    		}
    	}
    	
    	sortList(closedAutoStartAppList);
    	
    	if(adapter == null){
    		adapter = new AddAutoStartAppAdapter(this,closedAutoStartAppList);
        	ListView.setAdapter(adapter);
    	}else{
    		adapter.notifyDataSetChanged();
    	} 
    	
    	if(closedAutoStartAppList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.ListView).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.ListView).setVisibility(View.VISIBLE);
    	}
    
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月13日 下午5:07:15 .
     */
    public void initOrUpdatetListData1(){
    	if(ListView == null){
    		return ;
    	}
    	if(closedAutoStartAppList == null){
    		closedAutoStartAppList = new ArrayList<BaseData>();
    	}else{
    		closedAutoStartAppList.clear();
    	}
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo == null){
    		return ;
    	}
    	
    	for(int i=0;i<userAppsInfo.size();i++){
    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
    		if(appInfo == null || !appInfo.getIsInstalled()){
    			continue;
    		}
    		
    		AutoStartData autoStartData = AutoStartModel.getInstance(this).
    				getAutoStartData(appInfo.getPackageName());
    		if(autoStartData == null){
    			continue ;
    		}
    		
			if(!autoStartData.getIsOpen()){
				ApkUtils.initAppNameInfo(this, appInfo);   			
				closedAutoStartAppList.add(appInfo);
			}			
    	}
    	
    	sortList(closedAutoStartAppList);
    	
    	if(adapter == null){
    		adapter = new AddAutoStartAppAdapter(this,closedAutoStartAppList);
        	ListView.setAdapter(adapter);
    	}else{
    		adapter.notifyDataSetChanged();
    	} 
    	
    	if(closedAutoStartAppList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.ListView).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.ListView).setVisibility(View.VISIBLE);
    	}
    }
    
    /**
     * 更新数据
     */
    public void initOrUpdatetListData(){
    	initOrUpdateListData2();
    }
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
		
	@Override
	public void onBackPressed() {
		exitSelf();
	}

	@Override
	protected void onDestroy() {
		AutoStartModel.getInstance(AddAutoStartAppActivity.this).detach(updateViewHandler);	
		releaseObject();
		super.onDestroy();
	}
	
	public void exitSelf(){		
    	finish();
    }
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){		
		if(closedAutoStartAppList != null){
			closedAutoStartAppList.clear();
		}
	}
}
