package com.secure.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.adapter.AddAutoStartAppAdapter;
import com.secure.adapter.AddPrivacyApplistAdapter;
import com.secure.data.AppCategoryData;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.BaseData;
import com.secure.data.DefStartAppInfo;
import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.data.SameCategoryAppInfo;
import com.secure.data.SameFirstCharAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.model.DefSoftModel;
import com.secure.stickylistheaders.StickyListHeadersListView;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.ServiceUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.GridViewForEmbed;
import com.secure.view.LetterSideBar;
import com.secure.view.LetterToast;
import com.secure.view.MyProgressDialog;
import com.secure.view.LetterSideBar.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraListView;

public class AddPrivacyAppActivity extends AuroraActivity implements OnTouchingLetterChangedListener,
                                                                     OnScrollListener,
                                                                     Observer,
                                                                     OnClickListener,
                                                                     PrivacyAppObserver{
	private AddPrivacyApplistAdapter adapter;
	private ListView listView;
	private List<BaseData> canHideAppList;
	private List<BaseData> sameFirstCharAppList;
	private LetterToast letterToast;
	private LetterSideBar letterSideBar;
	private MyProgressDialog dialog;
    private int lastDealFirstVisibleItem = -1;
    private MyArrayList<String> choiceAppList;//存放选中的packageName
    private TextView sureText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setAuroraContentView(R.layout.add_privacy_app_activity,
        		AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.add_privacy_app);
        getAuroraActionBar().addItem(R.layout.add_privacy_right_action_bar_item,
        		R.id.add_privacy_action_bar_item);    
        dialog = new MyProgressDialog();
        choiceAppList = new MyArrayList<String>();
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        AuroraPrivacyManageModel.getInstance(this).attach(this);
        ServiceUtils.startServiceIfNeed(this);   
        initView(); 
        ActivityUtils.sleepForloadScreen(100,new LoadCallback(){
			@Override
			public void loaded() {
				initData(); 	
			}       	
        });  
    }
    
    public MyArrayList<String> getChoiceAppList(){
    	return this.choiceAppList;
    }
    
    /**
     * 更新当前选中应用的个数视图
     */
    public synchronized void updateSureText(){
    	if(sureText != null){
    		if(choiceAppList == null || choiceAppList.size() == 0){
    			sureText.setVisibility(View.INVISIBLE);
    		}else{
    			sureText.setVisibility(View.VISIBLE);
    			sureText.setText(String.format(getString(R.string.sure_num),choiceAppList.size()));
    		}   		
    	}
    }
    
    private void initView(){
    	listView = (ListView)findViewById(R.id.listView);	
		letterSideBar = (LetterSideBar)findViewById(R.id.letterSideBar);
		letterSideBar.setOnTouchingLetterChangedListener(this);
		listView.setOnScrollListener(this);
		sureText = (TextView)findViewById(R.id.sureText);
		sureText.setOnClickListener(this);
		updateSureText();
    }
   
    private void initData(){ 
    	if(dialog == null){
    		return ;
    	}
   		   
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
	
	/**
	 * 刷新listView
	 */
	public void updateListView(){
		if(adapter != null){
			adapter.mNotify();
		}
	}
 
    /**
     * 更新数据
     */
    public void initOrUpdatetListData(){
    	if(dialog != null){
		   dialog.close(); 
	    }	
    	
    	if(listView == null){
    		return ;
    	}
    	if(canHideAppList == null){
    		canHideAppList = new ArrayList<BaseData>(); 
    	}else{
    		canHideAppList.clear();
    	}
    	
    	if(sameFirstCharAppList == null){
    		sameFirstCharAppList = new ArrayList<BaseData>();
    	}else{
    		sameFirstCharAppList.clear();
    	}
    	
    	AppsInfo userAppsInfo = ConfigModel.getInstance(this).getAppInfoModel().getThirdPartyAppsInfo();
    	if(userAppsInfo == null){
    		return ;
    	}
    	
    	for(int i=0;i<userAppsInfo.size();i++){
    		AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
    		if(appInfo == null || 
    				!appInfo.getIsInstalled() || 
    				!ApkUtils.isHaveLauncher(this, appInfo.getPackageName())){
    			//说明：在桌面上有图标的才用添加为隐私应用
    			continue;
    		} 	
    		if(!AuroraPrivacyManageModel.getInstance(this).
    				isPrivacyApp(appInfo.getPackageName())){
    			ApkUtils.initAppNameInfo(this, appInfo); 
        		canHideAppList.add(appInfo);
    		}    		
    	}
    	
    	sortList(canHideAppList);
    	SameFirstCharAppData sameFirstCharAppData = null;
    	for(int i=0;i<canHideAppList.size();i++){
    		AppInfo appInfo = (AppInfo)canHideAppList.get(i);
    		if(appInfo.getAppNamePinYin() == null &&
    				appInfo.getAppNamePinYin().length()==0){
    			continue;	
	        }
    		String labelText = appInfo.getAppNamePinYin().substring(0, 1); 
    		if(labelText == null){
    			continue;
    		}
    		if(labelText.compareTo("A")<0 || labelText.compareTo("Z")>0){
    			labelText = "#";
    		}
    		if(sameFirstCharAppData == null || 
    				!labelText.equals(sameFirstCharAppData.getFirstChar())){
    			sameFirstCharAppData = new SameFirstCharAppData();
    			sameFirstCharAppData.setFirstChar(labelText);
    			sameFirstCharAppList.add(sameFirstCharAppData);
    		}
    		sameFirstCharAppData.getAppList().add(appInfo);
    	}
    	
    	if(adapter == null){
    		adapter = new AddPrivacyApplistAdapter(this,sameFirstCharAppList);
        	listView.setAdapter(adapter);
    	}else{
    		adapter.mNotify();
    	} 
    	
    	lastDealFirstVisibleItem = -1;
    	setHighlightTitle(0);
    	
    	if(sameFirstCharAppList.size() == 0){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.listView).setVisibility(View.GONE);
    		findViewById(R.id.FastIndexingLayout).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.listView).setVisibility(View.VISIBLE);
    		findViewById(R.id.FastIndexingLayout).setVisibility(View.VISIBLE);
    	}
    }
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
    
	@Override
	public void onTouchingLetterChanged(String s,float positionOfY) {
		if(letterToast == null ){
			letterToast = new LetterToast((FrameLayout)findViewById(R.id.FastIndexingLayout));
		}
		letterToast.LetterChanged(s, positionOfY, listView, adapter,alphaIndexer(adapter,s),
				((StickyListHeadersListView)listView).getHeaderHeight());
	}
	
	private int alphaIndexer(BaseAdapter adapter,String s) {		
		int position = -1;
		if(StringUtils.isEmpty(s) || adapter == null){
			return position;
		}
		SameFirstCharAppData sameFirstCharAppData;
		for (int i = 0; i < adapter.getCount(); i++) {
			sameFirstCharAppData = (SameFirstCharAppData)adapter.getItem(i);
			if(sameFirstCharAppData != null && 
					sameFirstCharAppData.getFirstChar().startsWith(s)){
				position = i;
				break;
			}
		}
		return position;
	}
	
	/**
	 * 根据当前ListView中第一个显示的item去确定LetterSideBar中高亮的字符
	 * @param firstVisibleItem
	 */
	private void setHighlightTitle(int firstVisibleItem){
		if(lastDealFirstVisibleItem == firstVisibleItem || 
				sameFirstCharAppList == null || 
				letterSideBar == null ||
				firstVisibleItem >= sameFirstCharAppList.size()){
			return ;
		}
		SameFirstCharAppData sameFirstCharAppData =(SameFirstCharAppData)sameFirstCharAppList.
				get(firstVisibleItem);
		if(sameFirstCharAppData == null){
			return ;
		}
		letterSideBar.setCurChooseTitle(sameFirstCharAppData.getFirstChar());	
		lastDealFirstVisibleItem = firstVisibleItem;	
	}
	
	/**
	 * 保存隐私应用的设置
	 */
	private void savePrivacyApp(){
		if(dialog == null){
			return ;
		}
		dialog.show(this, 
				getResources().getString(R.string.hint_privacy_app),
				getResources().getString(R.string.during_hint_privacy_app));
		
	    final Handler handler = new Handler() {
		   @Override
		   public void handleMessage(Message msg) {
			   if(dialog != null){
				   dialog.close();
			   }	
			   finish();
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
				
				MyArrayList<String> pkgList = new MyArrayList<String>();
				for(int i=0;i<choiceAppList.size();i++){
					try{
						pkgList.add(choiceAppList.get(i));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				AuroraPrivacyManageModel.getInstance(AddPrivacyAppActivity.this).
				    addPrivacyApp(pkgList);
				handler.sendEmptyMessage(0);
			}
		}.start();
	}
	
	private void deletePkgFromeChoiceAppList(String packageName){
		if(choiceAppList == null || StringUtils.isEmpty(packageName)){
			return ;
		}
		
		for(int i=0;i<choiceAppList.size();i++){
			String tmpStr = choiceAppList.get(i);
			if(packageName.equals(tmpStr)){
				choiceAppList.remove(tmpStr);
				break;
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
	
	private void releaseObject(){
		if(canHideAppList != null){
			canHideAppList.clear();
		}
		
		if(sameFirstCharAppList != null){
			sameFirstCharAppList.clear();
		}	
		
		if(choiceAppList != null){
			choiceAppList.clear();
		}
	}

	@Override
	public void updateOfInit(Subject subject) {
		initOrUpdatetListData();		
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		initOrUpdatetListData();			
	}

	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		initOrUpdatetListData();			
	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		 if(pkgName != null && !ApkUtils.canFindAppInfo(this, pkgName)){
			/**
        	 * 由于卸载和覆盖安装都会调用updateOfUnInstall()，
        	 * 所以此处要判断该应用是否真的卸载了。
        	 * 如果是真的卸载，才执行删除隐私应用的逻辑
        	 */
			deletePkgFromeChoiceAppList(pkgName);
			updateSureText();
			initOrUpdatetListData();	
		 }		
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
		if(pkgList != null){
			for(int i=0;i<pkgList.size();i++){
				deletePkgFromeChoiceAppList(pkgList.get(i));
			}
			updateSureText();
		}
		initOrUpdatetListData();			
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

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sureText:
			savePrivacyApp();
			break;
		}
		
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
}
