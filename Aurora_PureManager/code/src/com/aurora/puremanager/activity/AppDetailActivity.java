package com.aurora.puremanager.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;

import com.android.internal.content.PackageHelper;
import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.model.DefSoftModel;
import com.aurora.puremanager.permission.ApplicationsInfo;
import com.aurora.puremanager.permission.ItemInfo;
import com.aurora.puremanager.permission.PermissionAppDetail;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.totalCount.TotalCount;
import com.aurora.puremanager.utils.ActivityBarUtils;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.DisableChanger;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.AppDetailInfoView;
import com.aurora.puremanager.view.AppSizeView;
import com.aurora.puremanager.view.InfoDialog;

public class AppDetailActivity extends AuroraActivity implements OnClickListener,
                                                             OnCheckedChangeListener,
                                                             Observer{	  
	private AppInfo curAppInfo = null;
	private Button mForceStopButton;
//	private Button mMoveAppButton;
//	private TextView movingText;
	private Button uninstallBtn;
	private AppSizeView appSizeView;
	private IUsbManager mUsbManager;
	private AppWidgetManager mAppWidgetManager;
	private CanBeOnSdCardChecker mCanBeOnSdCardChecker;
	private boolean mMoveInProgress = false;
	private PackageMoveObserver mPackageMoveObserver;
	private int curActionBarStyle = ActivityBarUtils.TYPE_OF_Normal;
	
//	private final int ID_LOADER_DEFAULT = 10;
//	private AppListLoader mAppListLoader = null;
//	private ArrayList<NetworkAppInfo> mAppList = new ArrayList<NetworkAppInfo>();
//	private AppNetworkControl mAppNetworkControl;
	private  List<ItemInfo> mPermissionsApplist = new ArrayList<ItemInfo>();
	
	private String fromPkgName = "";		// 从哪个APP跳转来
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        IBinder b = ServiceManager.getService(Context.USB_SERVICE);
        mUsbManager = IUsbManager.Stub.asInterface(b);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mCanBeOnSdCardChecker = new CanBeOnSdCardChecker();
        
        if(mConfig.isNative){
        	setContentView(R.layout.app_detail_activity);
        }else{
        	setAuroraContentView(R.layout.app_detail_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.app_detail);
            getAuroraActionBar().setBackgroundResource(R.color.app_manager_title_color);
        }
//        mAppNetworkControl = AppNetworkControl.getInstance(this);
        
        // 如果由应用商店跳转的，需要更改actionbar样式
 		fromPkgName = getIntent().getStringExtra("pkgName");
 		if (!TextUtils.isEmpty(fromPkgName) && fromPkgName.equals("com.aurora.market")) {
 			getAuroraActionBar().setBackgroundResource(R.drawable.aurora_action_bar_top_bg_for_appstore);
 		}
        
        onCreateInitFunc();
    } 
    
    private void onCreateInitFunc(){
    	receiveData();
        if(curAppInfo == null || !curAppInfo.getIsInstalled()){
        	InfoDialog.showToast(this, getString(R.string.app_not_install));
        	finish();
        	return ;
        }
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
//        try {
//			getLoaderManager().restartLoader(ID_LOADER_DEFAULT, null,new AppInitCallbacks());
//		} catch (Exception e) {
//		}
        
        initView();
        initData();    
    }

	@Override
	protected void onRestart() {
    	initView();
    	initData();
		super.onRestart();
	}
    
	private void receiveData() {
		if(getIntent() != null && getIntent().getData() != null){
			String packageName = getIntent().getData().getSchemeSpecificPart();
			curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(packageName);
			
			Bundle bundle = getIntent().getExtras();
    		if(bundle != null){
    			curActionBarStyle = bundle.getInt(mConfig.ACTION_BAR_STYLE, ActivityBarUtils.TYPE_OF_Normal);
    			ActivityBarUtils.changeActionBarStyle(this,curActionBarStyle);	
    		}
		}		
	} 
	
	private void initView(){
		if(curAppInfo == null){
			return ;
		}
//		mAppList = mAppListLoader.getAppList();
		
		mForceStopButton = (Button)findViewById(R.id.mForceStopButton);
    	mForceStopButton.setOnClickListener(this);
//    	mForceStopButton.setEnabled(false);
    	
    	uninstallBtn = (Button)findViewById(R.id.uninstallBtn);
    	uninstallBtn.setOnClickListener(this);
    	
    	appSizeView = (AppSizeView)findViewById(R.id.appSizeView);
    	appSizeView.setCurAppInfo(curAppInfo.getPackageName());
    	
//    	movingText = (TextView)appSizeView.findViewById(R.id.movingText);
//    	
//    	mMoveAppButton = (Button)appSizeView.findViewById(R.id.moveBtn);
//    	mMoveAppButton.setOnClickListener(this);

    	((AppDetailInfoView)findViewById(R.id.appDetailInfoLayout)).setCurAppInfo(curAppInfo);
    	refreshButtons();
    	
    	ArrayList <View> showViews = new ArrayList <View>();
    	   	
    	//自启动区域
    	View autoStartLayout = findViewById(R.id.AutoStartLayout);	
    	if(curAppInfo.getIsUserApp()){
    		AutoStartData autoStartData = AutoStartModel.getInstance(this).
    				getAutoStartData(curAppInfo.getPackageName());
    		if(autoStartData != null){
        		AuroraSwitch autoStartSwitch = (AuroraSwitch)findViewById(R.id.autoStartSwitch);
        		autoStartSwitch.setOnCheckedChangeListener(this);
        		autoStartLayout.setVisibility(View.VISIBLE);
        		showViews.add(autoStartLayout);
        	}else{
        		autoStartLayout.setVisibility(View.GONE);
        	}
    	}else{
    		autoStartLayout.setVisibility(View.GONE);
    	}
    	
    	//权限管理区域
    	View permissionManageLayout = findViewById(R.id.permissionManageLayout);
    	ApplicationsInfo.getInstance().loadPermissionsAppEntries(AppDetailActivity.this);
    	mPermissionsApplist = ApplicationsInfo.getInstance().mPermissionsAppEntries;
    	
    	if(!curAppInfo.getIsUserApp()){
    		permissionManageLayout.setVisibility(View.GONE);
    	}else{
    		if(mPermissionsApplist.size() > 0){
        		for(int i = 0; i < mPermissionsApplist.size(); i++){
        			if(mPermissionsApplist.get(i).getPackageName().equals(curAppInfo.getPackageName())){
        				permissionManageLayout.setVisibility(View.VISIBLE);
        				permissionManageLayout.setOnClickListener(this);
        	    		showViews.add(permissionManageLayout);
        			}
        		}
        	}
    	}
		
    	//清除默认设置区域
    	refreshClearDefaultSetBtnUi();
    	View clearDefaultSetLayout = findViewById(R.id.clearDefaultSetLayout);
    	findViewById(R.id.clearDefaultSetBtn).setOnClickListener(this);
    	showViews.add(clearDefaultSetLayout);
    	
    	for(int i=0;i<showViews.size();i++){
    		View tmpView = showViews.get(i);
    		if(tmpView == null){
    			continue;
    		}
    		
    		/*if(i==0){
    			if(i == showViews.size()-1){
    				tmpView.setBackgroundResource(R.drawable.item_of_alone);
    			}else{
    				tmpView.setBackgroundResource(R.drawable.item_of_up);
    			}		
    		}else if(i == showViews.size()-1){
    			tmpView.setBackgroundResource(R.drawable.item_of_bottom);
    		}else{
    			tmpView.setBackgroundResource(R.drawable.item_of_middle);
    		}*/
    	}
    	
    	//2G/3G联网区域
    	/*View netLayout = findViewById(R.id.netLayout);
    	if(mAppList.size() > 0){
    		for(int i = 0; i < mAppList.size(); i++){
    			if(curAppInfo.getPackageName().equals(mAppList.get(i).getAppPackageName())){
    				netLayout.setVisibility(View.VISIBLE);
    				showViews.add(netLayout);
    			}
    		}
    	}*/
	}
	
	/*private class AppInitCallbacks implements LoaderCallbacks<Object> {

        @Override
        public Loader<Object> onCreateLoader(int id, Bundle args) {
            // TODO Auto-generated method stub
            return mAppListLoader = AppListLoader.getInstance(AppDetailActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Object> arg0, Object arg1) {
            // TODO Auto-generated method stub
        	 initView();
        }

        @Override
        public void onLoaderReset(Loader<Object> arg0) {
            // TODO Auto-generated method stub

        }
    }*/
	/**
	 * 更新卸载按钮的状态
	 */
	private void updateUninstallBtn(){
		if(curAppInfo == null || uninstallBtn == null){
			return ;
		}
		ApplicationInfo curApplicationInfo = ApkUtils.getApplicationInfo(this,
				curAppInfo.getPackageName());
		if(curApplicationInfo == null){
			return ;
		}
		uninstallBtn.setEnabled(true);
		if(curAppInfo.getIsUserApp()){
    		uninstallBtn.setText(R.string.uninstall);   		
    	}else{
    		if((mConfig.isUseSysAppCanNotDisableList && 
    				Constants.isPackageNameInList(Constants.sysAppCanNotDisableList, 
    						curAppInfo.getPackageName())) ||  						
    						curAppInfo.isHaveLauncher() || 
    						curAppInfo.isHome()){
    			if(curApplicationInfo.enabled){
        			uninstallBtn.setText(R.string.disable); 
        			uninstallBtn.setEnabled(false);
        		}else{
        			uninstallBtn.setText(R.string.start); 
        		}
    		}else{
    			initUninstallButtons(uninstallBtn);
    		} 		
    	}
	}
	
	/**
	 * 更新清除默认设置的按钮点击状态
	 * @return false 按钮不可点击，true 按钮可点击
	 */
	private boolean refreshClearDefaultSetBtnUi() {
		Button clearDefaultSetBtn = (Button)findViewById(R.id.clearDefaultSetBtn);
		 
        List<ComponentName> prefActList = new ArrayList<ComponentName>();     
        List<IntentFilter> intentList = new ArrayList<IntentFilter>();
        getPackageManager().getPreferredActivities(intentList, prefActList, curAppInfo.getPackageName());

        boolean hasUsbDefaults = false;
        try {
            hasUsbDefaults = mUsbManager.hasDefaults(curAppInfo.getPackageName(), UserHandle.myUserId());
        } catch (RemoteException e) {  }
        
        boolean hasBindAppWidgetPermission =
                mAppWidgetManager.hasBindAppWidgetPermission(curAppInfo.getPackageName());

        boolean autoLaunchEnabled = prefActList.size() > 0 || hasUsbDefaults;
        if (!autoLaunchEnabled && !hasBindAppWidgetPermission) {
        	clearDefaultSetBtn.setTextColor(getResources().getColor(R.color.head_text));
        	clearDefaultSetBtn.setEnabled(false);
        	return false;
        } else {
        	clearDefaultSetBtn.setEnabled(true);
        	clearDefaultSetBtn.setTextColor(getResources().getColor(R.color.uninstall_list_item_btn_color));
        	return true;
        }       
	}
    
    private void initData(){
    	checkForceStop(); 
    	
    	if(curAppInfo == null){
    		return ;
    	}
    	
    	//自启动区域
    	View autoStartLayout = findViewById(R.id.AutoStartLayout);	
    	if(autoStartLayout.getVisibility() == View.VISIBLE){
    		AuroraSwitch autoStartSwitch = (AuroraSwitch)findViewById(R.id.autoStartSwitch);
    		autoStartSwitch.setOnCheckedChangeListener(this);
    		AutoStartData autoStartData = AutoStartModel.getInstance(this).
    				getAutoStartData(curAppInfo.getPackageName());
    		HashSet<String> autostartApps = AutoStartAppProvider.loadAutoStartAppListInDB(this);
    		if(autoStartData != null && autostartApps.contains(curAppInfo.getPackageName())){
    			autoStartSwitch.setChecked(true);
    		}else{
    			autoStartSwitch.setChecked(false);
    		}
    	}   	
//    	initOrUpdateNetLayout();
    }
    
    /**
     * 2G/3G联网区域
     */
   /* private void initOrUpdateNetLayout(){
    	if(curAppInfo == null){
    		return ;
    	}
    	View netLayout = findViewById(R.id.netLayout);
    	if(netLayout.getVisibility() == View.VISIBLE){
    		AuroraSwitch netSwitch = (AuroraSwitch)findViewById(R.id.netSwitch);
        	netSwitch.setOnCheckedChangeListener(this);
        	if(mAppList.size() > 0){
        		for(int i = 0; i < mAppList.size(); i++){
        			if(curAppInfo.getPackageName().equals(mAppList.get(i).getAppPackageName())){
        				int state = mAppList.get(i).getAppMobileStatus();
        				if(state == 0){
        					netSwitch.setChecked(false);
        				}else{
        					netSwitch.setChecked(true);
        				}
        				return;
        			}
        		}
        	}
    	}
    }*/
    
	@Override
	public void onClick(View v) {
		if(curAppInfo == null){
			return ;
		}
		switch(v.getId()){
		case R.id.mForceStopButton:			
			InfoDialog.showDialog(this, 
				R.string.force_stop_dlg_title,
				android.R.attr.alertDialogIcon,
				R.string.force_stop_dlg_text,
				R.string.sure,
				new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	forceStopPackage(curAppInfo.getPackageName());
                    }
                }, 
                R.string.cancel,
				null,
				null);
			break;
		case R.id.uninstallBtn:
			if(curAppInfo.getIsUserApp()){
				uninstallFunc(); 		
	    	}else{
	    		ApplicationInfo curApplicationInfo = ApkUtils.getApplicationInfo(this,
	    				curAppInfo.getPackageName());
	    		if(curApplicationInfo != null){
	    			if(curApplicationInfo.enabled){
		    			disableApp(curApplicationInfo);
		    		}else{
		    			startApp(curApplicationInfo); 
		    		}
	    		}    		
	    	}
			break;
		case R.id.permissionManageLayout:
			new TotalCount(this, "35", 1).CountData();
			Intent intent = new Intent(this,PermissionAppDetail.class);
			intent.putExtra(mConfig.ACTION_BAR_STYLE, curActionBarStyle);  
			intent.putExtra(PermissionAppDetail.PACKAGENAME, curAppInfo.getPackageName());
			intent.putExtra(PermissionAppDetail.EXTRA_NAME_TITLE, curAppInfo.getAppName());
			startActivity(intent);
			break;
			
			/*intent = new Intent(this, PermissionAppDetail.class);
			intent.putExtra(PermissionAppDetail.PACKAGENAME,
					ApplicationsInfo.getInstance().mPermissionsAppEntries
							.get(position).mApplicationInfo.packageName);
			intent.putExtra(PermissionAppDetail.EXTRA_NAME_TITLE,
					mDataApp.get(position).getName());
			intent.putExtra(PermissionAppDetail.FROM_MYSELF, true);
			startActivity(intent);*/
			
		case R.id.clearDefaultSetBtn:
			new TotalCount(this, "36", 1).CountData();
			getPackageManager().clearPackagePreferredActivities(curAppInfo.getPackageName());
            try {
                mUsbManager.clearDefaults(curAppInfo.getPackageName(), UserHandle.myUserId());
            } catch (RemoteException e) { }
            mAppWidgetManager.setBindAppWidgetPermission(curAppInfo.getPackageName(), false);
            if(!refreshClearDefaultSetBtnUi()){
            	InfoDialog.showToast(this, getString(R.string.clear_sucess));
            }
			break;
//		case R.id.moveBtn:
//			moveApp();
//			break;
		}		
	}
	
	/**
	 * 卸载应用
	 */
	private void uninstallFunc(){
		Intent intent = new Intent();
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:"+curAppInfo.getPackageName()));
		startActivity(intent);
	}
	
	/**
	 * 启用应用
	 */
	private void startApp(ApplicationInfo applicationInfo){
		new DisableChanger(this, applicationInfo,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                updateSysAppStateHandler)
        .execute((Object)null);
	}
	
	/**
	 * 停用应用
	 */
	private void disableApp(final ApplicationInfo applicationInfo){
		InfoDialog.showDialog(this, 
				R.string.app_disable_dlg_title,
				android.R.attr.alertDialogIcon,
				R.string.app_disable_dlg_text,
				R.string.sure,
				new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      // Disable the app
                      new DisableChanger(AppDetailActivity.this, applicationInfo,
                              PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                              updateSysAppStateHandler)
                      .execute((Object)null);
                    }
                }, 
                R.string.cancel,
				null,
				null);
	}
	
	/**
     * 更新系统应用是否可用
     */
    private final Handler updateSysAppStateHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		refreshButtons() ;
    		if(DefSoftModel.getInstance() != null){
	   			 DefSoftModel.getInstance(AppDetailActivity.this).initOrUpdateThread();
	   		}
	    }
    };
		
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(curAppInfo == null){
			return ;
		}
		switch(buttonView.getId()){
		/*case R.id.netSwitch:
			if(mAppList.size() > 0){
				new TotalCount(this, "33", 1).CountData();
        		for(int i = 0; i < mAppList.size(); i++){
        			if(curAppInfo.getPackageName().equals(mAppList.get(i).getAppPackageName())){
//        				SparseIntArray disableArray = NetworkDisableUids.getInstance().getDisableUids(0);
        				int uid = mAppList.get(i).getAppUid();
        	            boolean isFlag = true;
//        				int state = mAppList.get(i).getAppMobileStatus();
        				if(isChecked == true){//开启
        					mAppList.get(i).setAppMobileStatus(1);
//        					disableArray.put(uid, 1);
        	                isFlag = true;
        				}else{
        					mAppList.get(i).setAppMobileStatus(0);
//        					 disableArray.delete(uid);
        		             isFlag = false;
        				}
//        				 mAppNetworkControl.reflectNetworkControlAction(mAppList.get(i).getAppUid(), 0, isFlag);
//        		         NetworkDisableUids.getInstance().resetDisableUidsRules(0, disableArray);
        				return;
        			}
        		}
        	}
			break;*/
		case R.id.autoStartSwitch:	
			AutoStartData autoStartData = AutoStartModel.getInstance(this).
			    getAutoStartData(curAppInfo.getPackageName());
			if(autoStartData != null && autoStartData.getAutoStartOfUser() != isChecked){
				/*AutoStartModel.getInstance(this).changeAutoStartState(
						curAppInfo.getPackageName(),isChecked);*/
				new TotalCount(this, "34", 1).CountData();
				AutoStartModel.getInstance(this).tryChangeAutoStartState(curAppInfo.getPackageName(),isChecked);
			}		
			break;
		}	
	}

	@Override
	protected void onDestroy() {
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		
	} 
     
     /**
      * 强行停止一个应用
      * @param pkgName
      */
     private void forceStopPackage(String pkgName) {
    	 ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
         am.forceStopPackage(pkgName);
         checkForceStop();        
     }

     private void updateForceStopButton(boolean enabled) {
    	 if(mForceStopButton != null){
    		 mForceStopButton.setEnabled(enabled);
    	 }   	 
     }
     
     private void checkForceStop() {
    	 if(curAppInfo == null){
    		 return ;
    	 }
    	 DevicePolicyManager mDpm;
    	 mDpm = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
    	 ApplicationInfo applicationInfo = ApkUtils.getApplicationInfo(this, curAppInfo.getPackageName());
    	 if(applicationInfo == null){
    		 return ;
    	 }
         if (mDpm.packageHasActiveAdmins(curAppInfo.getPackageName())) {
             updateForceStopButton(false);
         } else if ((applicationInfo.flags&ApplicationInfo.FLAG_STOPPED) == 0) {
             updateForceStopButton(true);
         } else {
             Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                     Uri.fromParts("package", curAppInfo.getPackageName(), null));
             intent.putExtra(Intent.EXTRA_PACKAGES, new String[] { curAppInfo.getPackageName() });
             intent.putExtra(Intent.EXTRA_UID, applicationInfo.uid);
             intent.putExtra(Intent.EXTRA_USER_HANDLE, UserHandle.getUserId(applicationInfo.uid));
             sendOrderedBroadcast(intent, null, mCheckKillProcessesReceiver, null,
                     Activity.RESULT_CANCELED, null, null);
         }
     }
     
     private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             updateForceStopButton(getResultCode() != Activity.RESULT_CANCELED);
         }
     };

	/*@Override
	public void updateOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo) {
		if(appInfo == null || curAppInfo == null){
			return ;
		}
		if(appInfo.getPackageName().equals(curAppInfo.getPackageName())){
			initOrUpdateNetLayout();		
		}				
	}*/
	
 	
	/**
	 * 对于系统应用，处理“启动（停用）按钮“显示的text，以及是否可点击的状态
	 * (说明：这部分是copy的系统源码)
	 * @param button
	 */
    private void initUninstallButtons(Button button) {
    	 if(curAppInfo == null){
    		 return ;
    	 }
    	 
         boolean enabled = true;    
         enabled = handleDisableable(button);
         
         // If this is a device admin, it can't be uninstall or disabled.
         // We do this here so the text of the button is still set correctly.
         DevicePolicyManager mDpm = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
         if (mDpm.packageHasActiveAdmins(curAppInfo.getPackageName())) {
            enabled = false;
         }
         button.setEnabled(enabled);
    }
    
    /**
     * (说明：这部分是copy的系统源码)
     * @param button
     * @return
     */
    private boolean handleDisableable(Button button) {
        boolean disableable = false;
        
        if(curAppInfo == null){
        	return disableable;
   	    }
        
		ApplicationInfo curApplicationInfo = ApkUtils.getApplicationInfo(this,
				curAppInfo.getPackageName());
		if(curApplicationInfo == null){
			return disableable;
		}
        
        try {
            // Try to prevent the user from bricking their phone
            // by not allowing disabling of apps signed with the
            // system cert and any launcher app in the system.
            PackageInfo sys = getPackageManager().getPackageInfo("android",
                    PackageManager.GET_SIGNATURES);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setPackage(curAppInfo.getPackageName());
            List<ResolveInfo> homes = getPackageManager().queryIntentActivities(intent, 0);
            if ((homes != null && homes.size() > 0) || isThisASystemPackage()) {
                // Disable button for core system applications.
                button.setText(R.string.disable);
            } else if (curApplicationInfo.enabled) {
                button.setText(R.string.disable);
                disableable = true;
            } else {
                button.setText(R.string.start);
                disableable = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return disableable;
    }
		
    /**
     * (说明：这部分是copy的系统源码)
     * @return
     */
	private boolean isThisASystemPackage() {
		if(curAppInfo == null){
        	return false;
   	    }
		
	    PackageInfo mPackageInfo = null;
        try {
              mPackageInfo = getPackageManager().getPackageInfo(curAppInfo.getPackageName(),
                    PackageManager.GET_DISABLED_COMPONENTS |
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) { }
          
	    if(mPackageInfo == null){
	    	return false;
	    } 
		  
	    try {
	        PackageInfo sys = getPackageManager().getPackageInfo("android", PackageManager.GET_SIGNATURES);
	        return (mPackageInfo != null && mPackageInfo.signatures != null &&
	               sys.signatures[0].equals(mPackageInfo.signatures[0]));
	    } catch (PackageManager.NameNotFoundException e) {
	        return false;
	    }
	}
	
	private void moveApp(){
		if(curAppInfo == null || mMoveInProgress){
			return ;
		}		
		if (mPackageMoveObserver == null) {
            mPackageMoveObserver = new PackageMoveObserver();
        }		
		ApplicationInfo curApplicationInfo =ApkUtils.getApplicationInfo(this,
	 				curAppInfo.getPackageName());
		if(curApplicationInfo != null){
			int moveFlags = (curApplicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0 ?
	                PackageManager.MOVE_INTERNAL : PackageManager.MOVE_EXTERNAL_MEDIA;
			if(moveFlags == PackageManager.MOVE_INTERNAL)
			{
				new TotalCount(this, "31", 1).CountData();
			}
			else if(moveFlags == PackageManager.MOVE_EXTERNAL_MEDIA)
			{
				new TotalCount(this, "30", 1).CountData();
			}
			
	        mMoveInProgress = true;
	        refreshButtons();
	        getPackageManager().movePackage(curAppInfo.getPackageName(), 
	        		mPackageMoveObserver, moveFlags);
		}      
	}
	
	class PackageMoveObserver extends IPackageMoveObserver.Stub {
        public void packageMoved(String packageName, int returnCode) throws RemoteException {
            final Message msg = moveAppHandler.obtainMessage();
            msg.arg1 = returnCode;           
            moveAppHandler.sendMessage(msg);
        }
    }
	 
	 private Handler moveAppHandler = new Handler() {
        public void handleMessage(Message msg) {
        	processMoveMsg(msg);
        }
     };
    
     private void processMoveMsg(Message msg) {
        int result = msg.arg1;
        // Refresh the button attributes.
        mMoveInProgress = false;
        if (result == PackageManager.MOVE_SUCCEEDED) {
            // Refresh size information again.
            if(appSizeView != null){
            	appSizeView.updatePacakgeSize(true);	
            }
        } else {
        	showDialogForMoveFailed(result);
        }
        refreshButtons();
     }
     
     private void showDialogForMoveFailed(int moveErrorCode){
    	 String msg = getMoveErrMsg(moveErrorCode);
    	 InfoDialog.showDialog(this, 
    			 getString(R.string.move_app_failed_dlg_title), 
    			 msg, 
    			 getString(R.string.sure));
     }
     
     private String getMoveErrMsg(int errCode) {
         switch (errCode) {
             case PackageManager.MOVE_FAILED_INSUFFICIENT_STORAGE:
                 return getString(R.string.insufficient_storage);
             case PackageManager.MOVE_FAILED_DOESNT_EXIST:
                 return getString(R.string.does_not_exist);
             case PackageManager.MOVE_FAILED_FORWARD_LOCKED:
                 return getString(R.string.app_forward_locked);
             case PackageManager.MOVE_FAILED_INVALID_LOCATION:
                 return getString(R.string.invalid_location);
             case PackageManager.MOVE_FAILED_SYSTEM_PACKAGE:
                 return getString(R.string.system_package);
             case PackageManager.MOVE_FAILED_INTERNAL_ERROR:
                 return "";
         }
         return "";
     }
     
     private void refreshButtons() {
         if (!mMoveInProgress) {
             initMoveButton();
             updateUninstallBtn();
             if(appSizeView != null){
            	 appSizeView.updateAllButtonState(); 
             } 
//             if(movingText != null){
//            	 movingText.setVisibility(View.INVISIBLE); 
//             }            
         } else {
//        	 if(movingText != null){
//            	 movingText.setVisibility(View.VISIBLE); 
//             } 
//        	 if(mMoveAppButton != null){
//        		 mMoveAppButton.setVisibility(View.INVISIBLE);
//        	 }
             if(uninstallBtn != null){
            	 uninstallBtn.setEnabled(false); 
             }          
             if(appSizeView != null){
            	 appSizeView.disableAllButton(); 
             }  
         }
     }
     
     private void initMoveButton() {
    	 if(curAppInfo == null /*|| mMoveAppButton == null*/){
    		 return ;
    	 }
    	 ApplicationInfo curApplicationInfo =ApkUtils.getApplicationInfo(this,
 				curAppInfo.getPackageName());
    	 if(curApplicationInfo == null){
    		 return ;
    	 }
//    	 mMoveAppButton.setVisibility(View.VISIBLE);
    	 PackageInfo mPackageInfo = ApkUtils.getPackageInfo(this, curAppInfo.getPackageName());
    	 if(Build.MANUFACTURER.equals("samsung")){
    		 if(Utils.getExternalStoragePath(this) == null){
//                 mMoveAppButton.setVisibility(View.INVISIBLE);
                 return;
             } 
    	 }else if(Environment.isExternalStorageEmulated()){
//    		 mMoveAppButton.setVisibility(View.INVISIBLE);
             return;
    	 }
    	 
         boolean dataOnly = false;
         dataOnly = (mPackageInfo == null);
         boolean moveDisable = true;
         if (dataOnly) {
//             mMoveAppButton.setText(R.string.move_app);
         } else if ((curApplicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
//             mMoveAppButton.setText(R.string.move_app_to_internal);
             // Always let apps move to internal storage from sdcard.
             moveDisable = false;
         } else {
//             mMoveAppButton.setText(R.string.move_app_to_sdcard);
             mCanBeOnSdCardChecker.init();
             moveDisable = !mCanBeOnSdCardChecker.check(curApplicationInfo);
         }
         if (moveDisable) {
//             mMoveAppButton.setEnabled(false);
         } else {
//             mMoveAppButton.setOnClickListener(this);
//             mMoveAppButton.setEnabled(true);
         }
     }
     
     final class CanBeOnSdCardChecker {
	    final IPackageManager mPm;
	    int mInstallLocation;
	    
	    CanBeOnSdCardChecker() {
	        mPm = IPackageManager.Stub.asInterface(
	                ServiceManager.getService("package"));
	    }
	    
	    void init() {
	        try {
	            mInstallLocation = mPm.getInstallLocation();
	        } catch (RemoteException e) {
	            return;
	        }
	    }
	    
	    boolean check(ApplicationInfo info) {
	        boolean canBe = false;
	        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
	            canBe = true;
	        } else {
	            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
	                if (info.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
	                        info.installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
	                    canBe = true;
	                } else if (info.installLocation
	                        == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
	                    if (mInstallLocation == PackageHelper.APP_INSTALL_EXTERNAL) {
	                        canBe = true;
	                    }
	                }
	            }
	        }
	        return canBe;
	    }
	}

	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub		
	}

	public void updateOfInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub		
	}

	public void updateOfCoverInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub	
	}

	public void updateOfUnInstall(Subject subject, String pkgName) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			finish();
		}		
	}

	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		if(curAppInfo == null || pkgList == null){
    		return ;
    	}			
		for(int i=0;i<pkgList.size();i++){
			if(pkgList.get(i).equals(curAppInfo.getPackageName())){
				curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().
						findAppInfo(curAppInfo.getPackageName(),curAppInfo.getIsUserApp());
				break;
			}
		}
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		// TODO Auto-generated method stub					
	}
}
