package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.CacheManageListAdapter;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.Constants;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.CacheSizeModel;
import com.secure.model.ConfigModel;
import com.secure.totalCount.TotalCount;
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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

/**
 * 说明：
 * 1.在该界面没有必要监听软件信息的变化（例如软件信息初始化完成，软件安装，软件卸载），即不需要实现：Observer接口；
 * 2.只需要监听软件缓存信息的变化就行；
 * 3.因为先有软件信息的变化，才会导致软件缓存数据的变化，在缓存变化处理完以后，才能进行界面显示或更新。
 * 4.在MainActivity中根据软件信息的变化，对应用缓存做相应的变化处理。
 */
public class CacheManageActivity extends AuroraActivity implements OnItemClickListener,
                                                                   OnItemLongClickListener,
                                                                   OnClickListener,
                                                                   Observer,
                                                                   PrivacyAppObserver{	
	public static final int SORT_BY_CACHE_SIZE = 0;
	public static final int SORT_BY_APP_SIZE = 1;
	private AtomicBoolean isDuringClearCacheOrUninstallApk = new AtomicBoolean(false);
	/**
	 * 当前的排序方式，排序方式有两种：
	 * SORT_BY_CACHE_SIZE
	 * SORT_BY_APP_SIZE
	 */
	private int curSortWay = 0;
	private List<BaseData> allAppList;	
	private CacheManageListAdapter adapter;
	private ListView ListView;	
	private TextView phoneMemoryAvailSize;
	private TextView sdMemoryAvailSize;
	private TextView canSaveMemorySize;
	private int curClearOrUninstallIndex;//当前清理或卸载应用的角标
	private Handler clearOrUninstallHandler;
	private ClearCacheObserver objectClearCacheObserver = new ClearCacheObserver();
	private PackageDeleteObserver objectPackageDeleteObserver = new PackageDeleteObserver();
	private MyProgressDialog dialog;
	private Button changeSortBtn;
	private boolean isShowActionBar = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        AuroraPrivacyManageModel.getInstance(this).attach(this);
        if(CacheSizeModel.getInstance(this).isNeedInit()){
        	CacheSizeModel.getInstance(this).initAllApkCacheSize();
        } 
        
        if(mConfig.isNative){
        	setContentView(R.layout.cache_manage_activity);
        }else{
        	setAuroraContentView(R.layout.cache_manage_activity,AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.clear_space);           
            getAuroraActionBar().addItem(AuroraActionBarItem.Type.Edit,R.id.add_menu);               
            registerAuroraViewListener();
            getAuroraActionBar().initActionBottomBarMenu(R.menu.cache_manage_menu,1);   
        }
        isDuringClearCacheOrUninstallApk.set(false);
        ServiceUtils.startServiceIfNeed(this);
        
        dialog = new MyProgressDialog();
        clearOrUninstallHandler = new Handler();
        initView(); 
        ActivityUtils.sleepForloadScreen(100,new LoadCallback(){
			@Override
			public void loaded() {
				initData(); 	
			}       	
        });          
    }
       
    private void initView(){
    	changeSortBtn = (Button)findViewById(R.id.changeSortBtn);
    	changeSortBtn.setOnClickListener(this);
    	curSortWay = 0;
    	ListView = (ListView)findViewById(R.id.ListView);
    	phoneMemoryAvailSize = (TextView)findViewById(R.id.phoneMemoryAvailSize);
    	sdMemoryAvailSize = (TextView)findViewById(R.id.sdMemoryAvailSize);
    	canSaveMemorySize = (TextView)findViewById(R.id.canSaveMemorySize);
    	canSaveMemorySize.setText(getResources().getString(R.string.please_choice));
    	canSaveMemorySize.setVisibility(View.GONE);
    	updateChoiceClearItemTextView();
    	initSortWay();	
    	
    	StorageUtil.getInstance(this).attach(updateCacheManageViewHandler);
    	if(StorageUtil.getInstance(this).isDuringUpdate()){
    		//wait,由于耗时不太多所以这里不显示wait动画
    	}else{
    		updateCacheManageViewHandler.sendEmptyMessage(1);
    	}
    }
    
    @Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.changeSortBtn:					
			InfoDialog.showSingleChoiceDialog(				
			   this,
			   R.array.choice_sort_way_list,
               curSortWay,
               new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == curSortWay){
							dialog.dismiss();
							return ;
						}
						new TotalCount(CacheManageActivity.this, "17", 1).CountData();
						if(which == 0){
	                		MySharedPref.saveIsSortByCache(CacheManageActivity.this,true);
	                		updateViewWhenChangeSortWay(SORT_BY_CACHE_SIZE);   
						}else{
	                		MySharedPref.saveIsSortByCache(CacheManageActivity.this,false);
	                		updateViewWhenChangeSortWay(SORT_BY_APP_SIZE);
						}
						dialog.dismiss();
					}             
	        });
			break;
		}		
	}
    
    /**
     * 当更改排序方式时掉用该函数刷新相应view
     */
    private void updateViewWhenChangeSortWay(int nextSortWay){ 	 	
    	curSortWay = nextSortWay;
    	if(curSortWay == SORT_BY_APP_SIZE){
    		if(changeSortBtn != null){
        		changeSortBtn.setText(R.string.sort_by_app_size);
        	}
    	}else{
    		if(changeSortBtn != null){
        		changeSortBtn.setText(getString(R.string.sort_by_cache_size) +
        				getString(R.string.left_brackets)+
        				getString(R.string.have_system_app)+
        				getString(R.string.right_brackets));
        	}
    	}
    	
    	initOrUpdatetListData();
    	
    	if(adapter != null){
			adapter.cancelChoiced();
		}
    	updateChoiceClearItemTextView();
    	updateRightButtonStats();
    }
    
    /**
     * 获取当前的排序方式
     * @return SORT_BY_CACHE_SIZE SORT_BY_APP_SIZE 
     */
    public int getCurSortWay(){
    	return curSortWay;
    }
    
    /**
     * 更新可用空间的textView
     */
    private final Handler updateCacheManageViewHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		if(phoneMemoryAvailSize == null || 
        			sdMemoryAvailSize == null){
        		return ;
        	}
        	
    		if(StorageUtil.getInstance(CacheManageActivity.this).isInternalSDMemorySizeAvailable()){
    			long AvailableInternalMemorySize = 
            			StorageUtil.getInstance(CacheManageActivity.this).getAvailableInternalMemorySize();
            	if(AvailableInternalMemorySize>-1){
            		phoneMemoryAvailSize.setText(
            				getResources().getString(R.string.phone_memory)+
            				getResources().getString(R.string.colon)+
            				String.format(getResources().getString(R.string.remainder),
            						Utils.dealMemorySize(CacheManageActivity.this,AvailableInternalMemorySize))
            				);
            	}
    		}else{
    			phoneMemoryAvailSize.setText(R.string.storage_not_available);
    		}
        	        	
        	long AvailableExternalMemorySize = 
        			StorageUtil.getInstance(CacheManageActivity.this).getAvailableExternalMemorySize();
        	if(AvailableExternalMemorySize >-1){
        		sdMemoryAvailSize.setVisibility(View.VISIBLE);
        		sdMemoryAvailSize.setText(
        				getResources().getString(R.string.sd_card)+
        				getResources().getString(R.string.colon)+      				
        		        String.format(getResources().getString(R.string.remainder),
        				  Utils.dealMemorySize(CacheManageActivity.this,
        						  AvailableExternalMemorySize)));
        	}else{
        		sdMemoryAvailSize.setVisibility(View.GONE);
        	} 
	    }
    };
    
    /**
     * 更新与选择清除软件个数有关的textView
     */
    private void updateChoiceClearItemTextView(){
    	if(adapter == null || 
    			canSaveMemorySize == null){
			return ;
		}
		if(adapter.getNeedClearList() == null || adapter.getNeedClearList().size() == 0){
			canSaveMemorySize.setVisibility(View.VISIBLE);
			canSaveMemorySize.setText(getResources().getString(R.string.please_choice));
		}else{
			canSaveMemorySize.setVisibility(View.VISIBLE);
			if(getCurSortWay() == SORT_BY_CACHE_SIZE){
				canSaveMemorySize.setText(getString(R.string.can_save_cache)+
						Utils.dealMemorySize(this, adapter.getNeedClearCanSaveSpace()));
			}else{
				canSaveMemorySize.setText(getString(R.string.can_save_space_by_delete_app)+
						Utils.dealMemorySize(this, adapter.getNeedClearCanSaveSpace())
//						+getString(R.string.space)
						);
			}			
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
			if(!mConfig.isNative){	
				TextView rightView = (TextView)getAuroraActionBar().getSelectRightButton();
	    		rightView.setText(getResources().getString(R.string.un_choice));
	    	}				
		}else{
			if(!mConfig.isNative){
				TextView rightView = (TextView)getAuroraActionBar().getSelectRightButton();
	    		rightView.setText(getResources().getString(R.string.check_all));
	    	}					
		} 	
		updateDeleteButtonState();
    }
    
    /**
     * 更新删除按钮的状态
     */
    private void updateDeleteButtonState(){   	
        boolean isEnable ;
    	if(adapter == null || adapter.getNeedClearList() == null){
    		isEnable = false;
		}else{
		    if(adapter.getNeedClearList().size() == 0){
		    	isEnable = false;
		    }else{
		    	isEnable = true;
		    }	
		}
    	getAuroraActionBar().getAuroraActionBottomBarMenu().setBottomMenuItemEnable(1, isEnable);
    }
    
    private void initData(){ 
    	if(dialog == null){
    		return ;
    	}
    	//监听应用缓存的变化
	    CacheSizeModel.getInstance(this).setCallBackHandler(updateViewHandler);
   	    if(CacheSizeModel.getInstance(this).getIsDuringGetCacheSize()){
   		   dialog.show(this, 
   				   getString(R.string.init), 
   				   getString(R.string.please_wait_init));
   	    }else{
   		   updateViewHandler.sendEmptyMessage(0);
   	    }
    }
    
    /**
     * 初始化排序方式
     */
    private void initSortWay(){
    	if(MySharedPref.getIsSortByCache(CacheManageActivity.this)){
	    	curSortWay = SORT_BY_CACHE_SIZE;
	    }else{
	    	curSortWay = SORT_BY_APP_SIZE;
	    }
    	if(curSortWay == SORT_BY_APP_SIZE){
    		if(changeSortBtn != null){
        		changeSortBtn.setText(R.string.sort_by_app_size);
        	}
    	}else{
    		if(changeSortBtn != null){
    			changeSortBtn.setText(getString(R.string.sort_by_cache_size) +
        				getString(R.string.left_brackets)+
        				getString(R.string.have_system_app)+
        				getString(R.string.right_brackets));
        	}
    	}
    }
    
    private final Handler updateViewHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	if(isDuringClearCacheOrUninstallApk.get()){
	    		return ;
	    	}
		    if(dialog != null){
			   dialog.close(); 
		    }	        
		    initOrUpdatetListData();
		    updateChoiceClearItemTextView();
		    updateCacheManageViewHandler.sendEmptyMessage(1);
		    updateRightButtonStats();
	    }
	};
       
    /**
     * 更新数据
     */
    private void initOrUpdatetListData(){   
    	if(ListView == null){
    		return ;
    	}
    	
    	TextView menuText = getAuroraActionBar().
 			   getAuroraActionBottomBarMenu().getTitleViewByPosition(0);
    	if(getCurSortWay() == SORT_BY_CACHE_SIZE){
    		menuText.setText(R.string.clear_cache);
    	}else{
    		menuText.setText(R.string.uninstall);
    	}

    	
    	if(allAppList == null){
    		allAppList = new ArrayList<BaseData>();
    	}else{
    		allAppList.clear();
    	}
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo != null){
    		for(int i=0;i<userAppsInfo.size();i++){
        		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
        		if(appInfo == null || !appInfo.getIsInstalled()){
        			continue;
        		}
        		ApkUtils.initAppNameInfo(this, appInfo);
        		allAppList.add(appInfo);
        	}
    	}
    	
    	if(getCurSortWay() == SORT_BY_CACHE_SIZE){
    		AppsInfo sysAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getSysAppsInfo();
        	if(sysAppsInfo != null){
        		for(int i=0;i<sysAppsInfo.size();i++){
            		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
            		if(appInfo == null || !appInfo.getIsInstalled() || !appInfo.getIsSysWhiteApp()){
            			continue;
            		}
            		ApkUtils.initAppNameInfo(this, appInfo);
            		allAppList.add(appInfo);
            	}
        	}
    	} 	
    	sortList();
       		
    	if(adapter == null){
    		adapter = new CacheManageListAdapter(this,allAppList);
        	ListView.setAdapter(adapter);
        	ListView.setOnItemClickListener(this);
        	ListView.setOnItemLongClickListener(this);
    	}else{
    		adapter.notifyDataSetChanged();
    	} 
    }
    
    /**
     * @param appsList
     * @param appInfo
     */
    private void sortList(){
    	Collections.sort(allAppList,new Comparator<BaseData>(){
 		   public int compare(BaseData s1, BaseData s2) {
 			   
 			   HashMap<String ,PackageStats> packageStatsMap = 
 			   			 CacheSizeModel.getInstance(CacheManageActivity.this).getPackageStatsMap();
 			   	
 			   PackageStats stats1 = packageStatsMap.get(((AppInfo)s1).getPackageName());
	 		   PackageStats stats2 = packageStatsMap.get(((AppInfo)s2).getPackageName());
 			   long flowByte1,flowByte2;
 			   if(curSortWay == SORT_BY_CACHE_SIZE){
 				   flowByte1 = stats1==null?0:(stats1.cacheSize+stats1.externalCacheSize);
 	 			   flowByte2 = stats2==null?0:(stats2.cacheSize+stats2.externalCacheSize);
 			   }else{
 				   flowByte1 = stats1==null?0:(stats1.cacheSize+stats1.externalCacheSize+
 						   stats1.codeSize+stats1.externalCodeSize+
 						   stats1.dataSize+stats1.externalDataSize);
	 			   flowByte2 = stats2==null?0:(stats2.cacheSize+stats2.externalCacheSize+
	 					   stats2.codeSize+stats2.externalCodeSize+
	 					   stats2.dataSize+stats2.externalDataSize);
 			   }
 			    
 			   int result = 0;
 			   if(flowByte1 > flowByte2){
				  result = -1; 
			   }else if(flowByte1 < flowByte2){
				  result =1;  
			   }else{
				   return Utils.compare(
						   ((AppInfo)s1).getAppNamePinYin(),
						   ((AppInfo)s2).getAppNamePinYin());
			   }
 			   return result;
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
		
	@Override
	protected void onRestart() {
		final Handler handler = new Handler() {
   		    @Override
   		    public void handleMessage(Message msg) {
   		    	initOrUpdatetListData();
   		   }
   		};
		new Thread() {
			@Override
			public void run() {	
				try{
					Thread.sleep(100);
				}catch(Exception e){
					e.printStackTrace();
				}		   
	            handler.sendEmptyMessage(1);   
			}
		}.start(); 	
		super.onRestart();
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
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		CacheSizeModel.getInstance(this).setCallBackHandler(null);
		StorageUtil.getInstance(this).detach(updateCacheManageViewHandler);
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
		if(allAppList != null){
			allAppList.clear();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(adapter == null){
			return ;
		}
		
		if(adapter.isChoiceStateOfItem()){
			adapter.dealItemClick(arg2);
			updateChoiceClearItemTextView();
			updateRightButtonStats();
		}else{
			if(adapter != null && arg2<adapter.getCount()){
				AppInfo appInfo = (AppInfo)adapter.getItem(arg2);
				if(appInfo != null && !StringUtils.isEmpty(appInfo.getPackageName())){
					
					
					Intent intent;
					if(curSortWay == SORT_BY_APP_SIZE){
						intent = new Intent(this,AppDetailActivity.class);
					}else{
						new TotalCount(CacheManageActivity.this, "21", 1).CountData();
						intent = new Intent(this,CacheDetailActivity.class);
					}			   
					intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
				   startActivity(intent);
				}
			}	
		}	
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(adapter == null){
			return false;
		}
		new TotalCount(this, "18", 1).CountData();
		adapter.dealItemClick(arg2);
		updateChoiceClearItemTextView();
		updateRightButtonStats();
		
		if(!adapter.isChoiceStateOfItem()){	
			if(!mConfig.isNative){
				showEditActionBar();
				showOrHideMenu(true);
			}			
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
						adapter.getNeedClearList() == null || 
						adapter.getNeedClearList().size() == 0){
        			return ;
        		}
				int titleStrId,msgStrId;
				if(getCurSortWay() == SORT_BY_CACHE_SIZE){
					titleStrId = R.string.clear_cache;
					msgStrId = R.string.clear_some_cache_dlg_text;
				}else{
					titleStrId = R.string.uninstall_app;
					msgStrId = R.string.uninstall_app_dlg_text;
				}			
				InfoDialog.showDialog(CacheManageActivity.this, 
						titleStrId,
						android.R.attr.alertDialogIcon,
						msgStrId, 
		                R.string.sure, 
		                new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                    	clearCacheDataOrUninstall();
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
		if(ListView != null){
			((AuroraListView)ListView).auroraEnableSelector(false);
		}
	}
	
	private void hideEditActionBar(){
		if(isShowActionBar){
			isShowActionBar = false;
			if(ListView != null){
				((AuroraListView)ListView).auroraEnableSelector(true);
			}
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
			adapter.setItemState(false);
			adapter.cancelChoiced();
			findViewById(R.id.canSaveMemorySize).setVisibility(View.GONE);
			findViewById(R.id.memorySizeLayout).setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 进入选择状态
	 */
    private void enterChoiceState(){
    	if(adapter != null){			
			adapter.setItemState(true);
			findViewById(R.id.canSaveMemorySize).setVisibility(View.VISIBLE);
			findViewById(R.id.memorySizeLayout).setVisibility(View.GONE);
			updateRightButtonStats();
			updateChoiceClearItemTextView();
		}
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
		updateChoiceClearItemTextView();
		updateRightButtonStats();
	}
	
	/**
	 * 批量清除选中应用的缓存或卸载应用
	 */
	private void clearCacheDataOrUninstall(){		
		if(dialog == null){
			return ;
		}
		curClearOrUninstallIndex = 0;		
		int titleStrId,msgStrId;
		if(getCurSortWay() == SORT_BY_CACHE_SIZE){
			titleStrId = R.string.clear_cache;
			msgStrId = R.string.during_clear_choiceed_app_cache_data;
		}else{
			titleStrId = R.string.uninstall_app;
			msgStrId = R.string.during_uninstall_choiceed_app;			
		}	
		isDuringClearCacheOrUninstallApk.set(true);
		dialog.show(this, 
				getResources().getString(titleStrId),
				getResources().getString(msgStrId));		
		clearCacheDataOrUninstallFunc();
    }
	
	private void clearCacheDataOrUninstallFunc(){
		if(adapter == null || 
				adapter.getNeedClearList() == null || 
				curClearOrUninstallIndex >= adapter.getNeedClearList().size()){
			BatchCleadOrUninstallEndFunc();
		}else{
			String pkgName = adapter.getNeedClearList().get(curClearOrUninstallIndex);
			if(getCurSortWay() == SORT_BY_CACHE_SIZE){
				new TotalCount(CacheManageActivity.this, "20", 1).CountData();
				ApkUtils.ClearAppCacheData(this,pkgName,objectClearCacheObserver);
			}else{
				new TotalCount(CacheManageActivity.this, "19", 1).CountData();
				ApkUtils.SilenceUninstallApp(this,pkgName,objectPackageDeleteObserver);
			}
		}	
	}
		
	class ClearCacheObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
        	clearOrUninstallHandler.postDelayed(runnable, 500);
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
			clearCacheDataOrUninstallFunc();
		}
	};
	
	/**
	 * 批量清理或卸载应用结束
	 */
	private void BatchCleadOrUninstallEndFunc(){	
		hideEditActionBar();
		showOrHideMenu(false);
		actionMenuLeftViewFunc();
		CacheSizeModel.getInstance(CacheManageActivity.this).initAllApkCacheSize();
		if(getCurSortWay() == SORT_BY_CACHE_SIZE){
			InfoDialog.showToast(this, R.string.cache_clear_finish);
		}else{
			InfoDialog.showToast(this, R.string.app_uninstall_finish);
		}
		LogUtils.printWithLogCat(CacheManageActivity.class.getName(), "BatchCleadOrUninstallEndFunc");
		isDuringClearCacheOrUninstallApk.set(false);
		updateViewHandler.sendEmptyMessage(0);		
	}

	@Override
	public void updateOfInit(Subject subject) {
		 if(CacheSizeModel.getInstance(this).isNeedInit()){
	        	CacheSizeModel.getInstance(this).initAllApkCacheSize();
	     } 		
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
	}

	@Override
	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,
			List<String> pkgList) {
		// TODO Auto-generated method stub		
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
		initOrUpdatetListData();		
	}  
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		initOrUpdatetListData();				
	}
}
