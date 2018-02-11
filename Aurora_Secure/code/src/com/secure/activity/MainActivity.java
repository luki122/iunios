package com.secure.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase;

import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.MainActvitiyListAdapter;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.MainActivityItemData;
import com.secure.data.PermissionInfo;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.AdObserver;
import com.secure.interfaces.AdSubject;
import com.secure.interfaces.LBEServiceBindObserver;
import com.secure.interfaces.LBEServiceBindSubject;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PermissionSubject;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AdLibModel;
import com.secure.model.AdScanModel;
import com.secure.model.AppInfoModel;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.model.DefSoftModel;
import com.secure.model.ExitModel;
import com.secure.receive.PermissionChangeReciver;
import com.secure.service.WatchDogService;
import com.secure.totalCount.TotalCount;
import com.secure.utils.LogUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.StorageUtil;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.view.MainCircleLayout;
import com.secure.view.MyProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AuroraActivity implements Observer,
                                                      PermissionObserver,
                                                      OnItemClickListener,
                                                      LBEServiceBindObserver,
                                                      AdObserver,
                                                      PrivacyAppObserver{
	private ArrayList<BaseData> itemList;
	private MainActvitiyListAdapter adapter;
	private MyProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.main_activity);
        }else{
        	setAuroraContentView(R.layout.main_activity,
            		AuroraActionBar.Type.Empty);
            getAuroraActionBar().setTitle(R.string.app_manage);
            setAuroraMenuCallBack(auroraMenuCallBack);  
            setAuroraMenuItems(R.menu.main_menu); 
        }
        initData();
        loadAppInfo();	
        startService(new Intent(this,WatchDogService.class));
//        debugDisplayInfo();
    }
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK) {  
            moveTaskToBack(false);  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);  
    }
    /*private */void debugDisplayInfo() {
        StringBuilder sb = new StringBuilder();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        sb.append("density: ").append(dm.density).append("\n")
        .append("densityDpi").append(dm.densityDpi).append("\n")
        .append("Pixels: ").append(dm.widthPixels).append("x").append(dm.heightPixels);
        
        Log.d("MainActivity", "Jim, display info: " + sb.toString());
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
    }
    
    private void initData(){	
    	//初始化listItem数据
    	if(itemList == null){
    		itemList = new ArrayList<BaseData>();
    	}else{
    		itemList.clear();
    	}
    	
    	//权限管理
    	MainActivityItemData itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.permission_icon);
    	itemData.setItemName(getString(R.string.jurisdiction_manage));
    	itemData.setComponentName(new ComponentName(this,PermissionManageActivity.class));
    	itemData.setHintStrTail(getString(R.string.wait_optimal_num));
    	itemData.setHintStrFront("0");
    	itemList.add(itemData);
    	
    	//自启动管理
    	itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.auto_start_icon);
    	itemData.setItemName(getString(R.string.auto_start_manage));
    	itemData.setComponentName(new ComponentName(this,AutoStartManageActivity.class));
    	itemData.setHintStrTail(getString(R.string.auto_start_soft_num));
    	itemData.setHintStrFront("0");
    	itemList.add(itemData);
    	
    	//联网管理
    	itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.net_icon);
    	itemData.setItemName(getString(R.string.net_manage));
    	itemData.setComponentName(new ComponentName(this,NetManageActivity.class));
    	itemData.setHintStrTail(getString(R.string.forbid_net_soft_num));
    	itemData.setHintStrFront("0");
    	itemList.add(itemData);
    	
    	//缓存管理
    	itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.cache_icon);
    	itemData.setItemName(getString(R.string.clear_space));
    	itemData.setComponentName(new ComponentName(this,CacheManageActivity.class));
    	itemData.setHintStrTail(getString(R.string.remainder));
    	itemData.setHintStrFront("0GB");
    	itemList.add(itemData);
    	
    	//默认软件管理
    	itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.def_soft_icon);
    	itemData.setItemName(getString(R.string.def_soft_manage));
    	itemData.setComponentName(new ComponentName(this,DefSoftManageActivity.class));
    	itemData.setHintStrTail(getString(R.string.no_set_num));
    	itemData.setHintStrFront("0");
    	itemList.add(itemData);
    	
    	//广告拦截
    	itemData = new MainActivityItemData();
    	itemData.setIconRes(R.drawable.ad_block_icon);
    	itemData.setItemName(getString(R.string.ad_block));
    	itemData.setComponentName(new ComponentName(this,AdAppListActivity.class)); 	
    	if(MySharedPref.getAlreadyScanAd(this)){
    		itemData.setHintStrFront("0");
    		itemData.setHintStrTail(getString(R.string.ad_app_num));
		}else{
			itemData.setHintStrFront("");
			itemData.setHintStrTail(getString(R.string.no_scan_ad));
		}
    	itemList.add(itemData);
    	
    	if(adapter == null){
    		adapter = new MainActvitiyListAdapter(this,itemList);
    		GridView gridView = (GridView)findViewById(R.id.gridView);
    		gridView.setAdapter(adapter);
    		gridView.setOnItemClickListener(this);
    	}else{
    		adapter.notifyDataSetChanged();
    	}    
    }
    
    /**
     * 加载程序必须的数据
     */
    private void loadAppInfo(){   
    	DefSoftModel.getInstance(this).attach(updateDefSoftViewHandler);
    	StorageUtil.getInstance(this).attach(updateCacheManageViewHandler);
    	AutoStartModel.getInstance(this).attach(updateAutoStartViewHandler);
    	updateDefSoftViewHandler.sendEmptyMessage(0);
    	updateCacheManageViewHandler.sendEmptyMessage(0);
    	updateAutoStartViewHandler.sendEmptyMessage(0);

    	ConfigModel.getInstance(this).getAppInfoModel().attach(MainActivity.this);
    	AuroraPrivacyManageModel.getInstance(this).attach(this);
    	PermissionSubject.getInstance().attach(this);
    	LBEServiceBindSubject.getInstance().attach(this);
	    if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
		   updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
	    }else{		    
    	    if(dialog == null){
		       dialog = new MyProgressDialog();
	   		}
	   		dialog.show(this, 
	   				getString(R.string.init), 
	   				getString(R.string.please_wait_init));
	    }
    }
    
    private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = 
			new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {			
			case R.id.btn_set:
				new TotalCount(MainActivity.this, "7", 1).CountData();
				Intent intent = new Intent(MainActivity.this,SettingActivity.class);
				startActivity(intent);
				break;
				}
			}
	 };
   	
	/**
     * 更新可用空间的textView
     */
    private final Handler updateCacheManageViewHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		int cacheItemPosition = 3;
        	if(itemList == null || adapter == null || itemList.isEmpty()){
        		return ;
        	} 
         	
        	MainActivityItemData item = (MainActivityItemData)itemList.get(cacheItemPosition);
        	item.setVilibileFlag(View.VISIBLE);
        	
        	long internalAvailable = StorageUtil.getInstance(MainActivity.this).getAvailableInternalMemorySize();
    		long externalAvailable = StorageUtil.getInstance(MainActivity.this).getAvailableExternalMemorySize();
			if(internalAvailable == StorageUtil.ERROR) internalAvailable = 0;
			if(externalAvailable == StorageUtil.ERROR) externalAvailable = 0;				
			long available = internalAvailable+externalAvailable;	
			
        	item.setHintStrFront(Utils.dealMemorySize(MainActivity.this,available));
        	adapter.notifyDataSetChanged();
        	sendBroadcast(new Intent("com.secure.action.deviceStorageChange"));
	    }
    };
    
    /**
     * 更新默认软件显示区域
     */
    private final Handler updateDefSoftViewHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	if(itemList == null || adapter == null || itemList.isEmpty()){
	    		return ;
	    	}
    	
	    	((MainActivityItemData)itemList.get(4)).setVilibileFlag(View.VISIBLE);
			((MainActivityItemData)itemList.get(4)).setHintStrFront(""+
					DefSoftModel.getInstance(MainActivity.this).getNotSetDefSoftNum());
			
	    	adapter.notifyDataSetChanged();
	    }
	};
	
	/**
     * 更新自启动显示区域
     */
    private final Handler updateAutoStartViewHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	if(itemList == null || adapter == null || itemList.isEmpty()){
	    		return ;
	    	}
	    	int autoStartOpenNum = AutoStartModel.getInstance(MainActivity.this).getAutoStartOpenAppNum();
	    	
	    	
	    	((MainActivityItemData)itemList.get(1)).setVilibileFlag(View.VISIBLE);
			((MainActivityItemData)itemList.get(1)).setHintStrFront(""+autoStartOpenNum);
			
			LogUtils.printWithLogCat("vautostart", "handleMessage: autoStartOpenNum = " + autoStartOpenNum);
			
	    	adapter.notifyDataSetChanged();
	    }
	};
	
	private void updateAdBlockView(AdSubject subject){
		if(itemList == null || adapter == null || subject == null || itemList.isEmpty()){
    		return ;
    	}
    	int adAppNum = ((AdScanModel)subject).getAdApkNumForMainActivity();
    	
    	((MainActivityItemData)itemList.get(5)).setVilibileFlag(View.VISIBLE);
		((MainActivityItemData)itemList.get(5)).setHintStrFront(""+adAppNum);
		
		if(MySharedPref.getAlreadyScanAd(this)){
			((MainActivityItemData)itemList.get(5)).setHintStrFront(""+adAppNum);
			((MainActivityItemData)itemList.get(5)).setHintStrTail(getString(R.string.ad_app_num));
		}else{
			((MainActivityItemData)itemList.get(5)).setHintStrFront("");
			((MainActivityItemData)itemList.get(5)).setHintStrTail(getString(R.string.no_scan_ad));
		}		
    	adapter.notifyDataSetChanged();
	}
      
    private void restart(Subject subject){
    	if(subject == null || itemList == null || adapter == null || itemList.isEmpty()){
			return ;
		}
			
		AppInfoModel appInfoModel = (AppInfoModel)subject;
		if(appInfoModel.getThirdPartyAppsInfo() != null){
		
			((MainActivityItemData)itemList.get(0)).setVilibileFlag(View.VISIBLE);
			((MainActivityItemData)itemList.get(0)).setHintStrFront(""+
					appInfoModel.getThirdPartyAppsInfo().getDangerAppsNum());
			
			int forbidNetworkNum = appInfoModel.getThirdPartyAppsInfo().getForbidNetworkNum()+
					appInfoModel.getSysAppsInfo().getForbidNetworkNum();
			((MainActivityItemData)itemList.get(2)).setVilibileFlag(View.VISIBLE);
			((MainActivityItemData)itemList.get(2)).setHintStrFront(""+forbidNetworkNum);
		}		
		adapter.notifyDataSetChanged();
    }
    
    private void startCircleAnim(){ 	
    	final AppInfoModel appInfoModel = ConfigModel.getInstance(this).getAppInfoModel();
		if(appInfoModel != null){
			TextView userAppNum = (TextView)findViewById(R.id.userAppNum);
			userAppNum.setText(getString(R.string.user_app)+
					getString(R.string.colon)+
					appInfoModel.getUserAppsNum());
			
			TextView sysAppNum = (TextView)findViewById(R.id.sysAppNum);
			sysAppNum.setText(getString(R.string.sys_app)+
					getString(R.string.colon)+
					appInfoModel.getSysAppsNum());
			
			findViewById(R.id.circleNumLayout).setOnClickListener(new OnClickListener() {			
				public void onClick(View v) {
					new TotalCount(MainActivity.this, "0", 1).CountData();
					startClickAnim();
					findViewById(R.id.circleNumLayout).setEnabled(false);
				}
			});
			
			final MainCircleLayout view = (MainCircleLayout)findViewById(R.id.mainCircleLayout);		
			view.postDelayed(new Runnable(){
				@Override
				public void run() {
					view.startAnimation(appInfoModel.getUserAppsNum(),
							appInfoModel.getSysAppsNum());
				}},300);    
			
			//在动画执行完以后再去获取sd的空间信息，这样在E6上就不会卡
			view.postDelayed(new Runnable(){
				@Override
				public void run() {
					StorageUtil.getInstance(MainActivity.this).initOrUpdateThread();
					AdLibModel.getInstance(MainActivity.this).initOrUpdateFromNet();
				}},500+view.getAnimationDuration());  
		}
    }
    
    private void updateCircleWhenAppNumChange(){
    	final AppInfoModel appInfoModel = ConfigModel.getInstance(this).getAppInfoModel();
		if(appInfoModel != null){
			TextView userAppNum = (TextView)findViewById(R.id.userAppNum);
			userAppNum.setText(getString(R.string.user_app)+
					getString(R.string.colon)+
					appInfoModel.getUserAppsNum());
			
			TextView sysAppNum = (TextView)findViewById(R.id.sysAppNum);
			sysAppNum.setText(getString(R.string.sys_app)+
					getString(R.string.colon)+
					appInfoModel.getSysAppsNum());
			
			MainCircleLayout view = (MainCircleLayout)findViewById(R.id.mainCircleLayout);
			view.updateViewWhenAppNumChange(appInfoModel.getUserAppsNum(), 
					appInfoModel.getSysAppsNum());  		
		}
    }
	
	@Override
	public void updateOfInit(Subject subject) {
		if(dialog != null){
		   dialog.close();
	    }
		startCircleAnim();		
		restart(subject);
		AdScanModel.getInstance(this).attach(this);
		AdScanModel.getInstance(this).scanForInit();
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		restart(subject);
		updateCircleWhenAppNumChange();
	}
	
	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		restart(subject);
		updateCircleWhenAppNumChange();	
	}

	@Override
	public void updateOfUnInstall(Subject subject,String pkgName) {
		restart(subject);
		updateCircleWhenAppNumChange();
	}
	
	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		restart(subject);	
	}
	
	@Override
	public void updateOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo) {
		/**
		 * 必须要像下面这样处理。
		 * 如果不这样处理，在权限管理界面一键优化时，
		 * 界面会非常的卡顿，因为每处理应用的每一个权限都会调用这个方法
		 */
		if(!PermissionManageActivity.isDuringOptimizeAllPermission){
			restart(ConfigModel.getInstance(MainActivity.this).getAppInfoModel());
		}	
	} 
	
	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		restart(subject);
		updateCircleWhenAppNumChange();			
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		restart(subject);
		updateCircleWhenAppNumChange();					
	}
	
	@Override
	public void LBEServiceBindStateChange(boolean isConnection) {
		restart(ConfigModel.getInstance(MainActivity.this).getAppInfoModel());		
	}	

	@Override
	protected void onRestart() {
		findViewById(R.id.circleNumLayout).setEnabled(true);
		findViewById(R.id.clickAnimView).setVisibility(View.INVISIBLE);
		restart(ConfigModel.getInstance(MainActivity.this).getAppInfoModel());
	    updateAutoStartViewHandler.sendEmptyMessage(0);
	    updateDefSoftViewHandler.sendEmptyMessage(0);	
		super.onRestart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		AdScanModel.getInstance(this).detach(this);
		PermissionSubject.getInstance().detach(this);
		LBEServiceBindSubject.getInstance().detach(this);
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		DefSoftModel.getInstance(this).detach(updateDefSoftViewHandler);
		StorageUtil.getInstance(this).detach(updateCacheManageViewHandler);
		AutoStartModel.getInstance(this).detach(updateAutoStartViewHandler);
		releaseObject();
		ExitModel.clear();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
        if(itemList != null){
        	itemList.clear();
        }
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(adapter != null && arg2 < adapter.getCount()){
			MainActivityItemData item  = (MainActivityItemData)adapter.getItem(arg2);
			if(item == null){
				return ;
			}
			//add data count
			String itemname = item.getItemName();
			
			if(itemname.equals(getString(R.string.jurisdiction_manage)))
			{
				new TotalCount(MainActivity.this, "1", 1).CountData();
			}
			else if(itemname.equals(getString(R.string.auto_start_manage)))
			{
				new TotalCount(MainActivity.this, "2", 1).CountData();
			}
			else if(itemname.equals(getString(R.string.net_manage)))
			{
				new TotalCount(MainActivity.this, "3", 1).CountData();
			}
			else if(itemname.equals(getString(R.string.clear_space)))
			{
				new TotalCount(MainActivity.this, "4", 1).CountData();
			}
			else if(itemname.equals(getString(R.string.def_soft_manage)))
			{
				new TotalCount(MainActivity.this, "5", 1).CountData();
			}
			else if(itemname.equals(getString(R.string.ad_block)))
			{
				new TotalCount(MainActivity.this, "6", 1).CountData();
			}
			//end data count
			Intent intent = new Intent();
			if(item.getItemName().equals(getString(R.string.ad_block))){
				if(MySharedPref.getIsCompleteScanAdApp(this) || 
					ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo().size() == 0){
					intent.setComponent(new ComponentName(this,AdAppListActivity.class));
		    	}else{
		    		intent.setComponent(new ComponentName(this,AdBlockActivity.class));
		    	}
			}else{
				intent.setComponent(item.getComponentName());
			}		
			startActivity(intent);
		}		
	}
		
	private void startClickAnim(){
		final ImageView clickAnimView = (ImageView)findViewById(R.id.clickAnimView);
		Animation animation1 = AnimationUtils.loadAnimation(this,R.anim.circle_click_anim);
		animation1.setInterpolator(new LinearInterpolator());
		animation1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {				
				clickAnimView.postDelayed(new Runnable() {					
					@Override
					public void run() {
						
						
						startActivity(new Intent(MainActivity.this,AllAppListActivity.class));						
					}
				}, 10);			
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { 
				clickAnimView.setVisibility(View.VISIBLE);
			}		
		});
		clickAnimView.startAnimation(animation1);
	}

	@Override
	public void updateOfInit(AdSubject subject) {
		updateAdBlockView(subject);		
	}

	@Override
	public void updateOfInStall(AdSubject subject, String pkgName) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfCoverInStall(AdSubject subject, String pkgName) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfUnInstall(AdSubject subject, String pkgName) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfExternalAppAvailable(AdSubject subject,
			List<String> pkgList) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfExternalAppUnAvailable(AdSubject subject,
			List<String> pkgList) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfSwitchChange(AdSubject subject, String pkgName,
			boolean swtich) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfAdLibUpdate(AdSubject subject) {
		updateAdBlockView(subject);	
	}

	@Override
	public void updateOfManualUpdate(AdSubject subject) {
		updateAdBlockView(subject);	
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
		restart(ConfigModel.getInstance(MainActivity.this).getAppInfoModel());
		updateCircleWhenAppNumChange();
		updateAdBlockView(AdScanModel.getInstance(this));
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		restart(ConfigModel.getInstance(MainActivity.this).getAppInfoModel());
		updateCircleWhenAppNumChange();	
		updateAdBlockView(AdScanModel.getInstance(this));
	}
}
