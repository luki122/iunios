package com.secure.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.android.internal.util.MemInfoReader;
import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.AllAppListAdapter;
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
import com.secure.model.ConfigModel;
import com.secure.model.RunningState;
import com.secure.utils.ActivityBarUtils;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.utils.ApkUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.ServiceUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.view.AllAppPopupWindow;
import com.secure.view.LetterSideBar;
import com.secure.view.LetterSideBar.OnTouchingLetterChangedListener;
import com.secure.view.LetterToast;
import com.secure.view.MyProgressDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllAppListActivity extends AuroraActivity implements Observer,
                                                                  OnClickListener,
                                                                  OnItemClickListener,
                                                                  OnTouchingLetterChangedListener,
                                                                  OnScrollListener,
                                                                  RunningState.OnRefreshUiListener,
                                                                  PrivacyAppObserver{
	public static final int SORT_BY_USER_APP = 0;
	public static final int SORT_BY_SYS_APP = 1;
	public static final int SORT_BY_SYS_SUBGROUP = 2;
	public static final int SORT_BY_RunningProcesses = 3;
	private List<BaseData> allAppsList;//所有软件列表	
	private AllAppListAdapter adapter;
	private ListView appListview;
	private LetterToast letterToast;
	private Button changeSortBtn;
	private int curSortWay;
	private AllAppPopupWindow allAppPopupWindow = null;
	private LetterSideBar letterSideBar;
    private int lastDealFirstVisibleItem = -1;
    private MyProgressDialog dialog;
	private TextView MemorySizeTextView;
	private TextView percentTextView;
	final MemInfoReader mMemInfoReader = new MemInfoReader();
	long SECONDARY_SERVER_MEM;
	ActivityManager mAm;
	private int curActionBarStyle = ActivityBarUtils.TYPE_OF_Normal;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	setAuroraContentView(R.layout.all_app_list_activity,
        		AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.app_list);
        getAuroraActionBar().addItem(R.layout.all_app_right_action_bar_item,R.id.all_app_action_bar_item); 
        ServiceUtils.startServiceIfNeed(this);
        
        mAm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memInfo);
        SECONDARY_SERVER_MEM = memInfo.secondaryServerThreshold;
        
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
		super.onResume();
	}

	@Override
	protected void onRestart() {
    	findViewById(R.id.FastIndexingLayout).setVisibility(View.GONE);
    	findViewById(R.id.memorySizeLayout).setVisibility(View.GONE); 
    	updateViewFunc("onRestart");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		RunningState.getInstance(this).pause();
		super.onStop();
	}

	private void initView(){
    	if(getIntent() != null && 
    		(Settings.ACTION_APPLICATION_SETTINGS.equals(getIntent().getAction()) || 
				Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS.equals(getIntent().getAction()) ||
				Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS.equals(getIntent().getAction())
				)){
    		curSortWay = SORT_BY_USER_APP;
    		Bundle bundle = getIntent().getExtras();
    		if(bundle != null && mConfig.PKGNAME_OF_Market.equals(bundle.getString("pkgName"))){
    			curActionBarStyle = ActivityBarUtils.TYPE_OF_Appstore;
    			changeActionBar();	
    		}
    	}else{
    		curSortWay = MySharedPref.getAllAppSortRecord(this);
    	} 	
    	dialog = new MyProgressDialog();
    	changeSortBtn = (Button)findViewById(R.id.changeSortBtn);
    	changeSortBtn.setOnClickListener(this);
    	initOrUpdateSortBtnView();
    	appListview = (ListView)findViewById(R.id.appListview);
    	appListview.setOnScrollListener(this);
		letterSideBar = (LetterSideBar)findViewById(R.id.letterSideBar);
		letterSideBar.setOnTouchingLetterChangedListener(this);
		findViewById(R.id.FastIndexingLayout).setVisibility(View.GONE);
		findViewById(R.id.memorySizeLayout).setVisibility(View.GONE); 
		MemorySizeTextView = (TextView)findViewById(R.id.MemorySizeTextView);
		percentTextView = (TextView)findViewById(R.id.percentTextView);
		findViewById(R.id.gotoMarketBtn).setOnClickListener(this);		
    }
	
	private void changeActionBar(){
		ActivityBarUtils.changeActionBarStyle(this, ActivityBarUtils.TYPE_OF_Appstore);
		Button changeSortBtn = (Button)findViewById(R.id.changeSortBtn);
		ColorStateList csl=(ColorStateList)getResources().getColorStateList(R.color.window_right_button_text_selector_for_appstore);
		changeSortBtn.setTextColor(csl);
		changeSortBtn.setBackgroundResource(R.drawable.all_app_change_sort_btn_of_green_bar);
	}
    
    private void initData(){
    	updateViewFunc("initData");
	    ConfigModel.getInstance(this).getAppInfoModel().attach(this); 
	    AuroraPrivacyManageModel.getInstance(this).attach(this);
    }
    
    private void updateViewFunc(String tag){
    	if(curSortWay == SORT_BY_RunningProcesses){
    		RunningState.getInstance(this).resume(this);
            if (RunningState.getInstance(this).hasData()) {
            	onRefreshUi(true);
            }else{
            	dialog.show(this, 
        				   getString(R.string.init), 
        				   getString(R.string.please_wait_init));
            } 
    	}else{
    		RunningState.getInstance(this).pause();
    		initOrUpdatetAppsData();
    	}
    }
    
    /**
     * 更新Apps数据
     */
    private void initOrUpdatetAppsData(){
    	if(appListview == null){
    		return ;
    	}
    	dialog.close();
    	
    	if(allAppsList == null){
    		allAppsList = new ArrayList<BaseData>();
    	}else{
    		allAppsList.clear();
    	}
    	if(curSortWay == SORT_BY_USER_APP){
    		AppsInfo userAppsInfo = ConfigModel.getInstance(this).
    				getAppInfoModel().getThirdPartyAppsInfo();   	
        	if(userAppsInfo != null){
        		for(int i=0;i<userAppsInfo.size();i++){
            		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
            		if(appInfo == null || 
            				!appInfo.getIsInstalled()){
            			continue;
            		}
            		ApkUtils.initAppNameInfo(this, appInfo);   
            		allAppsList.add(appInfo);		
            	}
        	}
        	sortListByAppName();
    	}else if(curSortWay == SORT_BY_SYS_APP){
    		AppsInfo sysAppsInfo = ConfigModel.getInstance(this).
    				getAppInfoModel().getSysAppsInfo();
        	if(sysAppsInfo != null){
        		for(int i=0;i<sysAppsInfo.size();i++){
            		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
            		if(appInfo == null ||
            				!appInfo.getIsInstalled()){
            			continue;
            		}
            		if(appInfo.isHaveLauncher() || 
            			appInfo.isHome() ||
            			Constants.isPackageNameInList(Constants.showInSysAppList, 
            					appInfo.getPackageName())){
            			ApkUtils.initAppNameInfo(this, appInfo);   
            			allAppsList.add(appInfo);
            		}       		
            	}
        	}
        	sortListByAppName();
    	}else if(curSortWay == SORT_BY_SYS_SUBGROUP){
    		AppsInfo sysAppsInfo = ConfigModel.getInstance(this).
    				getAppInfoModel().getSysAppsInfo();
        	if(sysAppsInfo != null){
        		for(int i=0;i<sysAppsInfo.size();i++){
            		AppInfo appInfo = (AppInfo)sysAppsInfo.get(i);
            		if(appInfo == null ||
            				!appInfo.getIsInstalled() ||
            				appInfo.isHaveLauncher() ||
            				appInfo.isHome() ||
            				Constants.isPackageNameInList(Constants.showInSysAppList, 
                					appInfo.getPackageName()) ||
        					Constants.isPackageNameInList(Constants.sysSubgrounpNoShowList, 
                					appInfo.getPackageName()) ){
            			continue;
            		}
            		ApkUtils.initAppNameInfo(this, appInfo);   
            		allAppsList.add(appInfo);
            	}
        	}
        	sortListByAppName();
    	}else if(curSortWay == SORT_BY_RunningProcesses){
    		ArrayList<AppInfo> mRunningProcesses = RunningState.getInstance(this).mRunningProcesses;
    		if(mRunningProcesses != null){
    			for(int i=0;i<mRunningProcesses.size();i++){
    				AppInfo tmpAppInfo = null;
    				try{
    					tmpAppInfo = mRunningProcesses.get(i);
    				}catch (Exception e) {
						e.printStackTrace();
					}
    				if(tmpAppInfo == null){
    					continue ;
    				}
    				ApkUtils.initAppNameInfo(this, tmpAppInfo);   
    				allAppsList.add(tmpAppInfo);
    			}
    		}
    		sortListByUseMemorySize();
    	} 
    	  	 	
    	if(adapter == null){
    		adapter = new AllAppListAdapter(this,allAppsList);
    		adapter.setSortType(curSortWay);
        	appListview.setAdapter(adapter);
        	appListview.setOnItemClickListener(this);
    	}else{
    		adapter.setSortType(curSortWay);
    		adapter.notifyDataSetChanged();
    	}
    	 	
    	lastDealFirstVisibleItem = -1;
    	setHighlightTitle(0);
    	
    	if(allAppsList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.contentLayout).setVisibility(View.GONE);
    		findViewById(R.id.FastIndexingLayout).setVisibility(View.GONE);   		
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);
    		if(curSortWay == SORT_BY_RunningProcesses){
    			findViewById(R.id.FastIndexingLayout).setVisibility(View.GONE);   
    			findViewById(R.id.memorySizeLayout).setVisibility(View.VISIBLE); 
    		}else{
    			findViewById(R.id.FastIndexingLayout).setVisibility(View.VISIBLE);
    			findViewById(R.id.memorySizeLayout).setVisibility(View.GONE); 
    		}  		
    	}
    }
    
    /**
     * 根据app的名称来排序
     */
    private void sortListByAppName(){
    	Collections.sort(allAppsList, new Comparator<BaseData>(){
   		   public int compare(BaseData s1, BaseData s2) {
   			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
   		   }
   		});
    }
    
    /**
     * 根据应用占用的内存大小排序
     */
    private void sortListByUseMemorySize(){
    	Collections.sort(allAppsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   if(null == s1 && null == s2){
		    		return 0;		    
			   }else if(null == s1 && null != s2){
		    		return -1;
		       }else if(null != s1 && null == s2){
		    		return 1;
		       }
			   
 			   if(((AppInfo)s1).getMemorySize() > ((AppInfo)s2).getMemorySize()){
				  return -1;
			   }else if(((AppInfo)s1).getMemorySize() < ((AppInfo)s2).getMemorySize()){
				  return 1;  
			   }else{
				   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
			   }			   
		   }
		});
    }
	
	@Override
	public void updateOfInit(Subject subject) {		
		updateViewFunc("updateOfInit");
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {	
		updateViewFunc("updateOfInStall");
	}
	
	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		updateViewFunc("updateOfCoverInStall");
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		if(curSortWay == SORT_BY_RunningProcesses){
			RunningState.getInstance(this).deleteProcess(pkgName);
    	}else{
    		RunningState.getInstance(this).pause();
    		initOrUpdatetAppsData();
    	}
	}
	
	@Override
	public void updateOfRecomPermsChange(Subject subject) {
	} 
	
	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		updateViewFunc("updateOfExternalAppAvailable");
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		updateViewFunc("updateOfExternalAppUnAvailable");
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {	
		if(adapter == null){
			return ;
		}
		
		if(adapter != null && arg2<adapter.getCount()){
			AppInfo appInfo = (AppInfo)adapter.getItem(arg2);
			if(appInfo != null && !StringUtils.isEmpty(appInfo.getPackageName())){
			   Intent intent = new Intent(this,AppDetailActivity.class);
			   intent.putExtra(mConfig.ACTION_BAR_STYLE, curActionBarStyle);  
			   intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
			   startActivity(intent);
			}
		}	
	} 
	
	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		if(allAppsList != null){
			allAppsList.clear();
		}
		if(letterToast != null){
			letterToast.releaseObject();
		}
	}
	
	@Override
	public void onTouchingLetterChanged(String s,float positionOfY) {
		if(letterToast == null ){
			letterToast = new LetterToast((FrameLayout)findViewById(R.id.FastIndexingLayout));
		}
		letterToast.LetterChanged(s, positionOfY, appListview, adapter,alphaIndexer(adapter,s),0);
	}
	
	private int alphaIndexer(BaseAdapter adapter,String s) {		
		int position = -1;
		if(StringUtils.isEmpty(s) || adapter == null){
			return position;
		}
		AppInfo appInfo;
		for (int i = 0; i < adapter.getCount(); i++) {
			appInfo = (AppInfo)adapter.getItem(i);
			if(appInfo != null && 
					appInfo.getAppNamePinYin().startsWith(s)){
				position = i;
				break;
			}
		}
		return position;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.changeSortBtn:					
			if(allAppPopupWindow == null){
				allAppPopupWindow = new AllAppPopupWindow(this, this);
			}		
			allAppPopupWindow.showAtLocation(this.findViewById(R.id.ListFrameLayout), 
					Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
			break;
			
		case R.id.SORT_BY_USER_APP:
			updateViewWhenChangeSortWay(SORT_BY_USER_APP);
			break;
		case R.id.SORT_BY_SYS_APP:
			updateViewWhenChangeSortWay(SORT_BY_SYS_APP);
			break;
		case R.id.SORT_BY_SYS_SUBGROUP:
			updateViewWhenChangeSortWay(SORT_BY_SYS_SUBGROUP);
			break;
		case R.id.SORT_BY_RunningProcesses:
			updateViewWhenChangeSortWay(SORT_BY_RunningProcesses);			
			break;
		case R.id.gotoMarketBtn:
			Uri uri = Uri.parse("openapplist://com.aurora.market.applist");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setData(uri);
			intent.putExtra("open_type",5);
			startActivity(intent);
			break;
		}	
	}
	
	private void initOrUpdateSortBtnView(){
		if(changeSortBtn == null){
    		return ;
    	}
    	if(curSortWay == SORT_BY_USER_APP){
    		changeSortBtn.setText(R.string.user_app);
    	}else if(curSortWay == SORT_BY_SYS_APP){
    		changeSortBtn.setText(R.string.sys_app);
    	}else if(curSortWay == SORT_BY_SYS_SUBGROUP){
    		changeSortBtn.setText(R.string.sys_subgroup);
    	}else if(curSortWay == SORT_BY_RunningProcesses){
    		changeSortBtn.setText(R.string.running_proc);
    	} 
	}
		
	/**
     * 当更改排序方式时掉用该函数刷新相应view
     */
    private void updateViewWhenChangeSortWay(int nextSortWay){
    	if(nextSortWay != curSortWay){
        	curSortWay = nextSortWay;
        	MySharedPref.saveAllAppSortRecord(this, curSortWay);
        	initOrUpdateSortBtnView();      	
        	updateViewFunc("updateViewWhenChangeSortWay");    	
		}
    	allAppPopupWindow.dismiss();
    }
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		setHighlightTitle(firstVisibleItem);	
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 根据当前ListView中第一个显示的item去确定LetterSideBar中高亮的字符
	 * @param firstVisibleItem
	 */
	private void setHighlightTitle(int firstVisibleItem){
		if(lastDealFirstVisibleItem == firstVisibleItem || 
				allAppsList == null || 
				letterSideBar == null ||
				firstVisibleItem >= allAppsList.size()){
			return ;
		}
	
		String appName  = ((AppInfo)allAppsList.get(firstVisibleItem)).getAppNamePinYin();
		if(appName != null && appName.length()>0){
			String title = appName.substring(0, 1);
			letterSideBar.setCurChooseTitle(title);			
		}
		lastDealFirstVisibleItem = firstVisibleItem;	
	}

	@Override
	public void onRefreshUi(boolean change) {
		initOrUpdatetAppsData();
		updateMemorySizeLayout();
	}
	
	/**
	 * 更新内存使用情况
	 */
	private void updateMemorySizeLayout(){	
		mMemInfoReader.readMemInfo();
        long availMem = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize()
                - SECONDARY_SERVER_MEM;        
        if (availMem < 0)  availMem = 0;
        long totalSize = mMemInfoReader.getTotalSize();
        long usedMem = totalSize-availMem;
               
        MemorySizeTextView.setText(String.format(getString(R.string.memory_details),
        		Utils.dealMemorySize(this, usedMem),
        		Utils.dealMemorySize(this, totalSize)));
        
        int percent = (int)(usedMem*100/totalSize);    
        percentTextView.setText(String.format(getString(R.string.memory_percent),percent+"%"));
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
		updateViewFunc("updateOfPrivacyAccountSwitch");		
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		updateViewFunc("updateOfDeletePrivacyAccount");					
	}
	
	/*@Override
    public void finish() {
        super.finish();
        
//        if (mNeedExitAnim) {
            overridePendingTransition(
                    com.aurora.R.anim.aurora_activity_close_enter,
                    com.aurora.R.anim.aurora_activity_close_exit);
//        }
    }*/
}