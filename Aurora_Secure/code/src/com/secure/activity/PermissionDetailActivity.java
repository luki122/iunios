package com.secure.activity;

import java.util.ArrayList;
import java.util.List;

import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.PermissionDetailAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.Constants;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PermissionSubject;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.ConfigModel;
import com.secure.model.LBEmodel;
import com.secure.provider.PermissionProvider;
import com.secure.receive.PermissionChangeReciver;
import com.secure.stickylistheaders.StickyListHeadersListView;
import com.secure.totalCount.TotalCount;
import com.secure.utils.ActivityUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.AppDetailInfoView;
import com.secure.view.InfoDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.secure.data.PermissionInfo;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;

public class PermissionDetailActivity extends AuroraActivity implements
                                      Observer,
                                      OnClickListener,
                                      PermissionObserver,
                                      PrivacyAppObserver{	
	private final int REQUEST_CODE_OF_UNINSTALL =1;
	private final int REQUEST_CODE_OF_APP_DETAIL = 2;
	private List<PermissionInfo> allPermissionInfoList;	
	private PermissionDetailAdapter adapter;
	private StickyListHeadersListView permissionListView;	
	private AppInfo curAppInfo = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.permission_detail_activity);
        }else{
        	setAuroraContentView(R.layout.permission_detail_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.app_permission);
        }
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        AuroraPrivacyManageModel.getInstance(this).attach(this);
        PermissionSubject.getInstance().attach(this); 	
        receiveData();
        if(curAppInfo == null || !curAppInfo.getIsInstalled()){
        	InfoDialog.showToast(this, getString(R.string.app_not_install));
        	finish();
        	return ;
        }
        initView(); 
        addBottomMenu();
//        registePermissionChangeReciver();
        ActivityUtils.sleepForloadScreen(150,new LoadCallback(){
			@Override
			public void loaded() {
				initData(); 
			}       	
        });
        
    }
    private static final int MENU_ACCEPT = 0x1111,MENU_PROMPT = 0x1113,MENU_REJECT = 0x1112;
    private void addBottomMenu(){
    	addMenu(MENU_ACCEPT, R.string.permission_accept,new OnMenuItemClickLisener() {
			
			@Override
			public void onItemClick(View arg0) {
				final int position = adapter.getPosition();
				if(position != -1 && position < allPermissionInfoList.size())
				{
					final PermissionInfo permissionInfo = allPermissionInfoList.get(position);
					if(permissionInfo.getCurState() != SDKConstants.ACTION_ACCEPT)
					{
						permissionInfo.setCurState(SDKConstants.ACTION_ACCEPT);
						new Thread(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								super.run();
								ApkUtils.openApkAppointPermission(PermissionDetailActivity.this, curAppInfo, permissionInfo);
							}
						}.start();
					    adapter.notifyDataSetChanged();
					}
				}else{
				}
			}
		});
       addMenu(MENU_PROMPT, R.string.permission_prompt,new OnMenuItemClickLisener() {
			
			@Override
			public void onItemClick(View arg0) {
				// TODO Auto-generated method stub
						final int position = adapter.getPosition();
						if (position != -1&& position < allPermissionInfoList.size()) {
							final PermissionInfo permissionInfo = allPermissionInfoList.get(position);
							if (permissionInfo.getCurState() != SDKConstants.ACTION_PROMPT) {
								permissionInfo.setCurState(SDKConstants.ACTION_PROMPT);
								new Thread() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										super.run();
										ApkUtils.promptApkAppointPermission(PermissionDetailActivity.this,curAppInfo, permissionInfo);
									}
								}.start();
								adapter.notifyDataSetChanged();
							}
						}else{
						}
			}
		});
       addMenu(MENU_REJECT, R.string.permission_reject,new OnMenuItemClickLisener() {
			
			@Override
			public void onItemClick(View arg0) {
				// TODO Auto-generated method stub
				final int position = adapter.getPosition();
						if (position != -1 && position < allPermissionInfoList.size()) {
							final PermissionInfo permissionInfo = allPermissionInfoList.get(position);
							if (permissionInfo.getCurState() != SDKConstants.ACTION_REJECT) {
								permissionInfo.setCurState(SDKConstants.ACTION_REJECT);
								new Thread() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										super.run();
										ApkUtils.closeApkAppointPermission(PermissionDetailActivity.this,curAppInfo, permissionInfo);
									}
								}.start();
								adapter.notifyDataSetChanged();
							}
						}else{
						}
			}
		});
    	
    	
    }
    
    private void registePermissionChangeReciver(){
    	IntentFilter filter = new IntentFilter(PermissionChangeReciver.PERMISSION_CHANGE_ACTION);
    	registerReceiver(permissionChangeReciver, filter);
    }
    
    private void unregisterPermissionChangeReciver(){
    	unregisterReceiver(permissionChangeReciver);
    }
    
    private BroadcastReceiver permissionChangeReciver = new PermissionChangeReciver();
    
    
    private void receiveData() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return ;
		}
		String packageName = extras.getString("packageName");
		if(StringUtils.isEmpty(packageName)){
			return;
		}
		curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().
				findAppInfo(packageName,true);
	/*	int old_size = curAppInfo.getPermission().size();
		curAppInfo.setPermission(ApkUtils.getPermission(this,packageName));
		int new_size = curAppInfo.getPermission().size();
		if(new_size != old_size)
			PermissionProvider.insertOrUpdateDate(this, curAppInfo);*/
	} 
    
    private void initView(){
    	findViewById(R.id.appDetailBtn).setOnClickListener(this);
    	findViewById(R.id.uninstallBtn).setOnClickListener(this);
    	permissionListView = (StickyListHeadersListView)findViewById(R.id.permissionListView);
    	
    	((AppDetailInfoView)findViewById(R.id.appDetailInfoLayout)).setCurAppInfo(curAppInfo);
    }
    
    private void initData(){
    	initOrUpdatetPermissData();
    }
    
    /**
     * 更新数据
     */
    private void initOrUpdatetPermissData(){  
    	if(permissionListView == null){
    		return ;
    	}
    	
    	if(allPermissionInfoList == null){
    		allPermissionInfoList = new ArrayList<PermissionInfo>();
    	}else{
    		allPermissionInfoList.clear();
    	}
    	
    	
    	if(curAppInfo == null || curAppInfo.getPermission() == null){
    		return ;
    	}
    	for(int i=0;i< curAppInfo.getPermission().size();i++){
    		PermissionInfo tmpPermissionInfo = curAppInfo.getPermission().get(i);
    		if(tmpPermissionInfo == null){
    			continue;
    		}
//    		tmpPermissionInfo.setCurState(LBEmodel.getInstance(getBaseContext()).getPermissionState(curAppInfo.getPackageName(), tmpPermissionInfo));
    		if(ApkUtils.isDangerPermission(this,curAppInfo,tmpPermissionInfo)){
    			allPermissionInfoList.add(0,tmpPermissionInfo);//将危险权限放在前面
    		}else{
    			allPermissionInfoList.add(tmpPermissionInfo);
    		}
    	}
    	
		if(adapter == null){
			adapter = new PermissionDetailAdapter(this,allPermissionInfoList,curAppInfo);
        	permissionListView.setAdapter(adapter);
		}else{
			adapter.notifyDataSetChanged();
		}  
    }

	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
		case R.id.appDetailBtn:
			new TotalCount(this, "11", 1).CountData();
			intent = new Intent(this,AppDetailActivity.class);
			intent.setData(Uri.fromParts(Constants.SCHEME, curAppInfo.getPackageName(), null));
			startActivityForResult(intent, REQUEST_CODE_OF_APP_DETAIL);
			break;
		case R.id.uninstallBtn:
			new TotalCount(this, "12", 1).CountData();
			intent = new Intent();
			intent.setAction("android.intent.action.DELETE");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setData(Uri.parse("package:"+curAppInfo.getPackageName()));
			startActivityForResult(intent, REQUEST_CODE_OF_UNINSTALL);
			break;
		}		
	}

	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		PermissionSubject.getInstance().detach(this);
		releaseObject();
//		unregisterPermissionChangeReciver();
		super.onDestroy();
	}

	@Override
	public void updateOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo) {
		if(appInfo == null || curAppInfo == null || adapter == null){
			return ;
		}
		if(appInfo.getPackageName().equals(curAppInfo.getPackageName())){
			new TotalCount(this, "10", 1).CountData();
			
			adapter.updateOfPermState(permissionInfo);
		}
	} 
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		if(allPermissionInfoList != null){
			allPermissionInfoList.clear();
		}
		if(adapter != null){
			adapter.realseObject();
		}
	}

	@Override
	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			finish();
		}		
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		if(curAppInfo == null || pkgList == null){
    		return ;
    	}			
		for(int i=0;i<pkgList.size();i++){
			if(pkgList.get(i).equals(curAppInfo.getPackageName())){
				curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().
						findAppInfo(curAppInfo.getPackageName(),curAppInfo.getIsUserApp());
				initOrUpdatetPermissData();
				break;
			}
		}		
	}

	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,
			List<String> pkgList) {
		// TODO Auto-generated method stub
		
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
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}			
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}				
	}
}
