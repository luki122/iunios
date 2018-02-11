package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.activity.CacheManageActivity.PackageDeleteObserver;
import com.secure.adapter.CacheManageListAdapter;
import com.secure.adapter.PrivacyAppGridAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.Constants;
import com.secure.data.MainActivityItemData;
import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.CacheSizeModel;
import com.secure.model.ConfigModel;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.ServiceUtils;
import com.secure.utils.StorageUtil;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.InfoDialog;
import com.secure.view.MyProgressDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

public class PrivacyAppActivity extends AuroraActivity implements OnItemClickListener,
                                                                   OnItemLongClickListener,
                                                                   OnClickListener,
                                                                   Observer,
                                                                   PrivacyAppObserver{	
	public static final int SORT_BY_CACHE_SIZE = 0;
	public static final int SORT_BY_APP_SIZE = 1;
	public static final String ADD_ITEM_FLAG = "add_privacy_item";
	private final AtomicBoolean isDuringDeletePrivacyApp = new AtomicBoolean(false);
	private final AppInfo addItem = new AppInfo();
	private List<BaseData> privacyAppList;	
	private PrivacyAppGridAdapter adapter;
	private GridView gridView;	
	private boolean isShowActionBar = false;
	private final MyProgressDialog dialog = new MyProgressDialog();
	private boolean isChoiceStateOfItem;//当前是否为选择状态
	private final MyArrayList<String> deletePrivacyPkgList = new MyArrayList<String>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.privacy_app_activity,AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.privacy_app);           
        getAuroraActionBar().addItem(AuroraActionBarItem.Type.Edit,R.id.add_menu);               
        registerAuroraViewListener();
        getAuroraActionBar().initActionBottomBarMenu(R.menu.privacy_app_menu,1);
        isDuringDeletePrivacyApp.set(false);
		addItem.setPackageName(ADD_ITEM_FLAG);	
        initView(); 
        ActivityUtils.sleepForloadScreen(200,new LoadCallback(){
			@Override
			public void loaded() {		        
				initData(); 	
			}       	
        });          
    }
       
    private void initView(){
    	gridView = (GridView)findViewById(R.id.gridView);
    }
    
    @Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.changeSortBtn:					
			break;
		}		
	}
    
    /**
     * 更新按钮的状态 upButtonStats
     */
    private void updateRightButtonStats(){ 	
	    if(adapter == null){
			return ;
		}
	    		
		if(adapter.isAllChoice()){
			TextView rightView = (TextView)getAuroraActionBar().getSelectRightButton();
    		rightView.setText(getResources().getString(R.string.un_choice));				
		}else{
			TextView rightView = (TextView)getAuroraActionBar().getSelectRightButton();
    		rightView.setText(getResources().getString(R.string.check_all));				
		} 	
		updateDeleteButtonState();
    }
    
    /**
     * 更新删除按钮的状态
     */
    private void updateDeleteButtonState(){   	
        boolean isEnable ;
    	if(adapter == null || adapter.getChoiceAppList() == null){
    		isEnable = false;
		}else{
		    if(adapter.getChoiceAppList().size() == 0){
		    	isEnable = false;
		    }else{
		    	isEnable = true;
		    }	
		}
    	getAuroraActionBar().getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, isEnable);
    }
    
    private void initData(){ 
		AuroraPrivacyManageModel.getInstance(PrivacyAppActivity.this).attach(this);
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        ServiceUtils.startServiceIfNeed(this);   
        
   	    if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
 		   updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
 	    }else{	
 	   		dialog.show(this, 
 	   				getString(R.string.init), 
 	   				getString(R.string.please_wait_init));
 	    }
    }
    
    private final Handler updateViewHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		    initOrUpdatetListData();
		    updateRightButtonStats();
	    }
	};
       
    /**
     * 更新数据
     */
    private void initOrUpdatetListData(){ 
    	if(!isDuringDeletePrivacyApp.get()){
    		dialog.close(); 
    	} 	
    	
    	if(gridView == null){
    		return ;
    	}
    	
    	if(privacyAppList == null){
    		privacyAppList = new ArrayList<BaseData>();
    	}else{
    		privacyAppList.clear();
    	}
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled()){
        			continue;
        		}
        		if(AuroraPrivacyManageModel.getInstance(this).
        				isCurAccountPrivacyApp(appInfo.getPackageName())){
            		ApkUtils.initAppNameInfo(this, appInfo);
            		privacyAppList.add(appInfo);
        		}
        	}
    	}
    	sortList(privacyAppList);
    	
    	//要加上添加的按钮
    	privacyAppList.add(addItem);
   	     		
    	if(adapter == null){
    		adapter = new PrivacyAppGridAdapter(this,privacyAppList);
        	gridView.setAdapter(adapter);
        	gridView.setOnItemClickListener(this);
        	gridView.setOnItemLongClickListener(this);
    	}else{
    		adapter.notifyDataSetChanged();
    	} 
    }
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),
					   ((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
			
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_BACK ) { 
			 if(getAuroraActionBar().auroraIsEntryEditModeAnimRunning() || 
					 getAuroraActionBar().auroraIsExitEditModeAnimRunning()){
				 return true;
			 }
			 
			 if(!mConfig.isNative && 
					getAuroraActionBar().isShowBottomBarMenu()){
				 hideEditActionBar();
				 actionMenuLeftViewFunc();
	         }
          }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		if(adapter != null){
			adapter.releaseObject();
		}
		if(privacyAppList != null){
			privacyAppList.clear();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(adapter == null){
			return ;
		}
		
		if(isChoiceStateOfItem){
			if(arg2 == adapter.getCount()-1){
				//do nothing
			}else{			
				adapter.dealItemClick(arg2);
				updateRightButtonStats();
			}
		}else{
			if(arg2 == adapter.getCount()-1){
				Intent intent = new Intent(this,AddPrivacyAppActivity.class);
				startActivity(intent);
			}else{
				if(arg2 < adapter.getCount()){
					enterAppointApk((AppInfo)adapter.getItem(arg2));
				}				
			}
		}
	}
	
	private void enterAppointApk(AppInfo appInfo){
		if(appInfo == null){
			return ;
		}
		String pkgName = appInfo.getPackageName();
		ResolveInfo resolveInfo=ApkUtils.getApkMainResolveInfo(this,pkgName);
		if(resolveInfo == null){
			return ;
		}
		try{
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName(pkgName,resolveInfo.activityInfo.name);
			startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	} 
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(adapter == null){
			return false;
		}
		if(arg2 == adapter.getCount()-1){
			Intent intent = new Intent(this,AddPrivacyAppActivity.class);
			startActivity(intent);
			return true;
		}
		adapter.dealItemClick(arg2);
		updateRightButtonStats();
		
		if(!isChoiceStateOfItem){	
			showEditActionBar();
			showOrHideMenu(true);			
			enterChoiceState();			
		}		
		return true;
	}
	
	/**
	 * 绑定Aurora view 事件
	 */
	private void registerAuroraViewListener(){	
		getAuroraActionBar().setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener(){
			@Override
			public void onAuroraActionBarItemClicked(int arg0) {
				showEditActionBar();	
				showOrHideMenu(true);
				enterChoiceState();
			}}) ;  
		
		getAuroraActionBar().getSelectLeftButton().setOnClickListener(
	        new View.OnClickListener() {	
	            @Override
	            public void onClick(View v) {
	            	hideEditActionBar();
					showOrHideMenu(false);
					actionMenuLeftViewFunc();
	            }
	        });
		
		getAuroraActionBar().getSelectRightButton().setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	actionMenuRightView();
                }
            });
		
		/**
		 * 在注册的时候，一定要放在
		 *  getAuroraActionBar().initActionBottomBarMenu(R.menu.cache_manage_menu,1)之前，
		 *  要不然注册不会生效
		 */
		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener(){
			@Override
			public void auroraMenuItemClick(int arg0) {
				if(adapter == null || 
						adapter.getChoiceAppList() == null || 
						adapter.getChoiceAppList().size() == 0){
        			return ;
        		}
				needUninstallApp = false;
				InfoDialog.showDialogWithCheckbox(PrivacyAppActivity.this, 
						R.string.cancel_privacy_app,
						android.R.attr.alertDialogIcon,
						R.string.sure_no_hint_choice_app, 
						R.string.uninstall_choice_app_at_same_time,
						false,
						new OnCheckedChangeListener(){
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								needUninstallApp = isChecked;								
							}},
		                R.string.sure, 
		                new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                    	isDuringDeletePrivacyApp.set(true);
		                    	deletePrivacyPkgList.clear();
		                    	for(int i=0;i<adapter.getChoiceAppList().size();i++){
		    						try{
		    							deletePrivacyPkgList.add(adapter.getChoiceAppList().get(i));
		    						}catch(Exception e){
		    							e.printStackTrace();
		    						}
		    					}
		                    	
		                    	if(needUninstallApp){
		                    		uninstallChoiceApp();
		                    	}else{
		                    		deletePrivacyApp(true);
		                    	}	                    	
		                    }
		                },
		                R.string.cancel,
						null,
						null
						);				 
		}});
	}
	
	private void showEditActionBar(){
		isShowActionBar = true;
		//调用下面的函数既可以显示，又可以关闭
		getAuroraActionBar().showActionBarDashBoard(); 
	}
	
	private void hideEditActionBar(){
		if(isShowActionBar){
			isShowActionBar = false;
			getAuroraActionBar().showActionBarDashBoard();
		}
	}
		
    /**
     * 显示或隐藏底部menu
     * @param flag
     */
	private void showOrHideMenu(boolean flag){
		try{
	        getAuroraActionBar().setShowBottomBarMenu(flag);
	        getAuroraActionBar().showActionBottomeBarMenu();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 退出选择状态
	 */
	private void actionMenuLeftViewFunc(){
		if(adapter != null){
			isChoiceStateOfItem = false;
			adapter.cancelChoiced();
			adapter.dealAddItemShowState();
		}
	}
	
	/**
	 * 进入选择状态
	 */
    private void enterChoiceState(){
		isChoiceStateOfItem = true;
		updateRightButtonStats();
		if(adapter != null){
			adapter.dealAddItemShowState();
		}
    }
    
    /**
     * 判断当前是不是进入选择状态
     * @return
     */
    public boolean getChoiceState(){
    	return isChoiceStateOfItem;
    }
	
	/**
	 * 全选或者反选
	 */
	private void actionMenuRightView(){
		if(adapter == null){
			return ;
		}
		
		if(adapter != null && adapter.isAllChoice()){
			adapter.cancelChoiced();
		}else{
			adapter.checkAllItem();
		}
		updateRightButtonStats();
	}

	@Override
	public void updateOfInit(Subject subject) {
		updateViewHandler.sendEmptyMessage(0);		
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
		// TODO Auto-generated method stub
		/**
		 * 这里不用做处理，因为已经在updateOfPrivacyAppDelete()做了相应的处理
		 */
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		initOrUpdatetListData();		
	}

	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,
			List<String> pkgList) {
		if(pkgList != null && adapter != null){
			for(int i=0;i<pkgList.size();i++){
				adapter.deletePkgFromeChoiceAppList(pkgList.get(i));
			}
			updateRightButtonStats();
		}	
		initOrUpdatetListData();	
	}

	@Override
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		initOrUpdatetListData();		
	}

	@Override
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		if(PrivacyAppList != null && adapter != null){
			for(int i=0;i<PrivacyAppList.size();i++){
				PrivacyAppData tmpData = PrivacyAppList.get(i);
				if(tmpData == null){
					continue ;
				}
				adapter.deletePkgFromeChoiceAppList(tmpData.getPkgName());
			}
			updateRightButtonStats();
		}	
		initOrUpdatetListData();		
	}
	
	@Override
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(accountData != null && 
				accountData.getAccountId() == mConfig.NORMAL_ACCOUNTID){
			finish();
		}else{
			initOrUpdatetListData();		
		}	
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		finish();		
	}

/**********************批量删除隐私应用 begin***********************/	
	/**
	 * 删除隐私应用
	 * @param showDialog true:显示堵塞dialog  false：不显示堵塞dialog
	 */
	private void deletePrivacyApp(boolean showDialog){		
		if(showDialog){
			dialog.show(this, 
				getResources().getString(R.string.cancel_privacy_app),
				getResources().getString(R.string.during_cancel_privacy_app));	
		}
		
		final Handler handler = new Handler() {
		   @Override
		   public void handleMessage(Message msg) {
			    isDuringDeletePrivacyApp.set(false);
			    dialog.close();
			    hideEditActionBar();
				showOrHideMenu(false);
				actionMenuLeftViewFunc();
		   }
		};
		
		new Thread() {
			@Override
			public void run() {
				try{
					Thread.sleep(500);
				}catch(Exception e){
					e.printStackTrace();
				}				
				if(adapter != null){
					AuroraPrivacyManageModel.getInstance(PrivacyAppActivity.this).
					   deletePrivacyApp(deletePrivacyPkgList);
				}
				handler.sendEmptyMessage(0);
			}
		}.start();
	}
	
/**********************批量卸载隐私应用 begin***********************/
	private boolean needUninstallApp = false;
	private int curClearOrUninstallIndex = 0;
	private PackageDeleteObserver objectPackageDeleteObserver = new PackageDeleteObserver();
	private Handler clearOrUninstallHandler = new Handler();
	/**
	 * 批量清除选中应用的缓存或卸载应用
	 */
	private void uninstallChoiceApp(){		
		curClearOrUninstallIndex = 0;		
		dialog.show(this, 
				getResources().getString(R.string.cancel_privacy_app),
				getResources().getString(R.string.during_cancel_privacy_app));		
		uninstallFunc();
    }
	
	private void uninstallFunc(){
		if(adapter == null || 
				curClearOrUninstallIndex >= deletePrivacyPkgList.size()){
			uninstallEndFunc();
		}else{
			String pkgName = deletePrivacyPkgList.get(curClearOrUninstallIndex);
			ApkUtils.SilenceUninstallApp(this,pkgName,objectPackageDeleteObserver);
		}	
	}
	  
	class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {    
        	clearOrUninstallHandler.postDelayed(runnable, 500);
        }
    }
	
    Runnable runnable = new Runnable() {			
		public void run() {
			++curClearOrUninstallIndex;
			uninstallFunc();
		}
	};
	
	/**
	 * 批量卸载应用结束
	 */
	private void uninstallEndFunc(){	
		//卸载完应用后，然后删除隐私应用的数据
		deletePrivacyApp(false);
	}
}
