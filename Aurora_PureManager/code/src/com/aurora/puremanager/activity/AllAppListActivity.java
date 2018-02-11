package com.aurora.puremanager.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.view.PagerAdapter;
import aurora.view.ViewPager.OnPageChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;

import com.android.internal.util.MemInfoReader;
import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.AllAppListAdapter;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AppsInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.model.RunningState;
import com.aurora.puremanager.service.WatchDogService;
import com.aurora.puremanager.utils.ActivityBarUtils;
import com.aurora.puremanager.utils.ActivityUtils;
import com.aurora.puremanager.utils.ActivityUtils.LoadCallback;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.LetterSideBar;
import com.aurora.puremanager.view.LetterSideBar.OnTouchingLetterChangedListener;
import com.aurora.puremanager.view.LetterToast;
import com.aurora.puremanager.view.MyProgressDialog;

public class AllAppListActivity extends AuroraActivity implements Observer,
															OnItemClickListener,
															OnTouchingLetterChangedListener,
															OnScrollListener,
															RunningState.OnRefreshUiListener {
	private AuroraTabWidget mTabWidget;
	private AuroraViewPager mViewPage;
	private AuroraPagerAdapter mAuroraTabAdapter;
	private LayoutInflater mInflater;
	 //新建一个viewlist对象来保存各个分页的内容
	private List<View> mViewList;
	private AllAppListAdapter adapter;
	private AuroraListView appListview;
	private Context mContext;
	private List<BaseData> allAppsList = new ArrayList<BaseData>();// 所有软件列表
	public static final int SORT_BY_USER_APP = 0;
	public static final int SORT_BY_SYS_APP = 1;
	public static final int SORT_BY_SYS_SUBGROUP = 2;
	public static final int SORT_BY_RunningProcesses = 3;
	private MyProgressDialog dialog;
	private int lastDealFirstVisibleItem = -1;
	private LetterToast letterToast;
	private LetterSideBar letterSideBar;
	private FrameLayout NoAppLayout/*, FastIndexingLayout*/; 
	private LinearLayout ContentLayout;
	private int curSortWay;
	private int curActionBarStyle = ActivityBarUtils.TYPE_OF_Normal;
	private RelativeLayout mMemorySizeLayout;
	private TextView MemorySizeTextView;
	private TextView percentTextView;
	final MemInfoReader mMemInfoReader = new MemInfoReader();
	long SECONDARY_SERVER_MEM;
	ActivityManager mAm;
	
	private String fromPkgName = "";		// 从哪个APP跳转来

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mConfig.isNative) {
			setContentView(R.layout.applist_tab_activity);
		} else {
			setAuroraContentView(R.layout.applist_tab_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.app_list);
			getAuroraActionBar().setBackgroundResource(R.color.app_manager_title_color);
		}
		mContext = this;
		
		// 如果由应用商店跳转的，需要更改actionbar样式
		fromPkgName = getIntent().getStringExtra("pkgName");
		if (!TextUtils.isEmpty(fromPkgName) && fromPkgName.equals("com.aurora.market")) {
			getAuroraActionBar().setBackgroundResource(R.drawable.aurora_action_bar_top_bg_for_appstore);
		}
		
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
		startService(new Intent(this,WatchDogService.class));
	}

	private void initView() {
		curSortWay = SORT_BY_USER_APP;
		dialog = new MyProgressDialog();
		mViewList = new ArrayList<View>();
		mInflater = getLayoutInflater();
		mViewList.add(mInflater.inflate(R.layout.applist_tab_activity_view, null));
		mViewList.add(mInflater.inflate(R.layout.applist_tab_activity_view, null));
		mViewList.add(mInflater.inflate(R.layout.applist_tab_activity_view, null));
		mViewList.add(mInflater.inflate(R.layout.applist_tab_activity_view, null));
		
		String[] tabTitle = new String[] {
				getResources().getString(R.string.user_app),
				getResources().getString(R.string.sys_app),
				getResources().getString(R.string.sys_subgroup),
				getResources().getString(R.string.running_proc) };
		
		for(int i = 0; i < 4; i++){
			UpdateViewPage(i);
		}
		
		mTabWidget = (AuroraTabWidget) findViewById(R.id.tabwidget);
		mTabWidget.setTitles(tabTitle);
		mTabWidget.seTextColorFocus(R.color.tab_text_focus_color);
		mTabWidget.seTextColorNormal(R.color.tab_text_normal_color);

		mAuroraTabAdapter = new AuroraPagerAdapter();
		mViewPage = mTabWidget.getViewPager();
		mViewPage.setOffscreenPageLimit(3);
		mViewPage.setAdapter(mAuroraTabAdapter);
		mViewPage.setCurrentItem(0);
		mTabWidget.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				switch (arg0) {
				case SORT_BY_USER_APP:
					curSortWay = SORT_BY_USER_APP;
					initOrUpdatetAppsData(SORT_BY_USER_APP);
					break;
				case SORT_BY_SYS_APP:
					curSortWay = SORT_BY_SYS_APP;
					initOrUpdatetAppsData(SORT_BY_SYS_APP);
					break;
				case SORT_BY_SYS_SUBGROUP:
					curSortWay = SORT_BY_SYS_SUBGROUP;
					initOrUpdatetAppsData(SORT_BY_SYS_SUBGROUP);
					break;
				case SORT_BY_RunningProcesses:
					curSortWay = SORT_BY_RunningProcesses;
					updateViewFunc("updateViewWhenChangeSortWay");   
					initOrUpdatetAppsData(SORT_BY_RunningProcesses);
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	private void initData(){
    	updateViewFunc("initData");
	    ConfigModel.getInstance(this).getAppInfoModel().attach(this); 
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
    		initOrUpdatetAppsData(curSortWay);
    	}
    }
	
	@Override
	protected void onResume() {
		if(curSortWay == SORT_BY_RunningProcesses
				|| curSortWay == SORT_BY_USER_APP){
			updateViewFunc("initData");
		}
		super.onResume();
	}

	@Override
	protected void onRestart() {
		if(curSortWay != SORT_BY_RunningProcesses){
			mMemorySizeLayout.setVisibility(View.GONE); 
		}
		super.onRestart();
	}
	
	@Override
	protected void onStop() {
		RunningState.getInstance(this).pause();
		super.onStop();
	}
	
	private void initOrUpdatetAppsData(int appType) {
		if (appListview == null) {
			return;
		}
		
		NoAppLayout = (FrameLayout) mViewList.get(appType).findViewById(R.id.NoAppLayout);
//		FastIndexingLayout =(FrameLayout)mViewList.get(appType).findViewById(R.id.FastIndexingLayout); 
//		letterSideBar = (LetterSideBar)mViewList.get(appType).findViewById(R.id.letterSideBar);
//		letterSideBar.setOnTouchingLetterChangedListener(this);
		
		dialog.close();
		if (allAppsList == null) {
			allAppsList = new ArrayList<BaseData>();
		} else {
			allAppsList.clear();
		}
		if (appType == SORT_BY_USER_APP) {
			if (ConfigModel.getInstance(this).getAppInfoModel()
					.isAlreadyGetAllAppInfo()){
				AppsInfo userAppsInfo = ConfigModel.getInstance(this)
						.getAppInfoModel().getThirdPartyAppsInfo();
				if (userAppsInfo != null) {
					for (int i = 0; i < userAppsInfo.size(); i++) {
						AppInfo appInfo = (AppInfo) userAppsInfo.get(i);
						if (appInfo == null || !appInfo.getIsInstalled()) {
							continue;
						}
						ApkUtils.initAppNameInfo(this, appInfo);
						allAppsList.add(appInfo);
					}
				}
				sortListByAppName();
			}
		} else if (appType == SORT_BY_SYS_APP) {
			if (ConfigModel.getInstance(this).getAppInfoModel()
					.isAlreadyGetAllAppInfo()){
				AppsInfo sysAppsInfo = ConfigModel.getInstance(this)
						.getAppInfoModel().getSysAppsInfo();
				if (sysAppsInfo != null) {
					for (int i = 0; i < sysAppsInfo.size(); i++) {
						AppInfo appInfo = (AppInfo) sysAppsInfo.get(i);
						if (appInfo == null || !appInfo.getIsInstalled()) {
							continue;
						}
						if (appInfo.isHaveLauncher()
								|| appInfo.isHome()
								|| Constants.isPackageNameInList(
										Constants.showInSysAppList,
										appInfo.getPackageName())) {
							ApkUtils.initAppNameInfo(this, appInfo);
							allAppsList.add(appInfo);
						}
					}
				}
				sortListByAppName();
			}
		} else if (appType == SORT_BY_SYS_SUBGROUP) {
			if (ConfigModel.getInstance(this).getAppInfoModel()
					.isAlreadyGetAllAppInfo()){
				AppsInfo sysAppsInfo = ConfigModel.getInstance(this)
						.getAppInfoModel().getSysAppsInfo();
				if (sysAppsInfo != null) {
					for (int i = 0; i < sysAppsInfo.size(); i++) {
						AppInfo appInfo = (AppInfo) sysAppsInfo.get(i);
						if (appInfo == null
								|| !appInfo.getIsInstalled()
								|| appInfo.isHaveLauncher()
								|| appInfo.isHome()
								|| Constants.isPackageNameInList(
										Constants.showInSysAppList,
										appInfo.getPackageName())
										|| Constants.isPackageNameInList(
												Constants.sysSubgrounpNoShowList,
												appInfo.getPackageName())) {
							continue;
						}
						ApkUtils.initAppNameInfo(this, appInfo);
						allAppsList.add(appInfo);
					}
				}
				sortListByAppName();
			}
		} else if (appType == SORT_BY_RunningProcesses) {
			ArrayList<AppInfo> mRunningProcesses = RunningState.getInstance(this).mRunningProcesses;
			if (mRunningProcesses != null) {
				for (int i = 0; i < mRunningProcesses.size(); i++) {
					AppInfo tmpAppInfo = null;
					try {
						tmpAppInfo = mRunningProcesses.get(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (tmpAppInfo == null) {
						continue;
					}
					ApkUtils.initAppNameInfo(this, tmpAppInfo);
					allAppsList.add(tmpAppInfo);
				}
			}
			sortListByUseMemorySize();
		}
		
//		UpdateViewPage(appType);
		adapter.setSortType(appType);
		adapter.notifyDataSetChanged();
		lastDealFirstVisibleItem = -1;
//		setHighlightTitle(0);
		
		if (allAppsList.size() == 0) {
			if (curSortWay != SORT_BY_RunningProcesses) {
				NoAppLayout.setVisibility(View.VISIBLE);
				ContentLayout.setVisibility(View.GONE);
//				FastIndexingLayout.setVisibility(View.GONE);
				mMemorySizeLayout.setVisibility(View.GONE);
			}
		} else {
			NoAppLayout.setVisibility(View.GONE);
			ContentLayout.setVisibility(View.VISIBLE);
			if (curSortWay == SORT_BY_RunningProcesses) {
//				FastIndexingLayout.setVisibility(View.GONE);
				mMemorySizeLayout.setVisibility(View.VISIBLE);
			} else {
//				FastIndexingLayout.setVisibility(View.VISIBLE);
				mMemorySizeLayout.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 根据app的名称来排序
	 */
	private void sortListByAppName() {
		Collections.sort(allAppsList, new Comparator<BaseData>() {
			public int compare(BaseData s1, BaseData s2) {
				return Utils.compare(((AppInfo) s1).getAppNamePinYin(),
						((AppInfo) s2).getAppNamePinYin());
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
	
    private class AuroraPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mViewList.size();
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			try {
				if (mViewList.get(position).getParent() == null) {
					((AuroraViewPager) collection).addView(mViewList.get(position), 0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block e.printStackTrace();
			}
			return mViewList.get(position);
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((AuroraViewPager) collection).removeView(mViewList.get(position));
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == (object);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}
	}
    
    private void UpdateViewPage(int position){
//    	dialog = new MyProgressDialog();
		
    	
		mMemorySizeLayout = (RelativeLayout) mViewList.get(position).findViewById(R.id.memorySizeLayout);
		ContentLayout =(LinearLayout) mViewList.get(position).findViewById(R.id.contentLayout);  
		appListview = (AuroraListView)mViewList.get(position).findViewById(R.id.testappListview);
		appListview.setOnScrollListener(this);
		appListview.setOnItemClickListener(this);
		
		MemorySizeTextView = (TextView)mViewList.get(position).findViewById(R.id.MemorySizeTextView);
		percentTextView = (TextView)mViewList.get(position).findViewById(R.id.percentTextView);
		
		if (adapter == null) {
			adapter = new AllAppListAdapter(mContext, allAppsList);
		}
		adapter.setSortType(curSortWay);
		appListview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
    }

	@Override
	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub
		initOrUpdatetAppsData(curSortWay);
	}

	@Override
	public void updateOfInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		if(curSortWay == SORT_BY_USER_APP){
			initOrUpdatetAppsData(SORT_BY_USER_APP);
		}
	}

	@Override
	public void updateOfCoverInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateOfUnInstall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub
		if(curSortWay == SORT_BY_RunningProcesses){
			RunningState.getInstance(this).deleteProcess(pkgName);
    	}else{
    		RunningState.getInstance(this).pause();
    		initOrUpdatetAppsData(curSortWay);
    	}
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
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
//		setHighlightTitle(firstVisibleItem);	
	}

	@Override
	public void onRefreshUi(boolean change) {
		// TODO Auto-generated method stub
		if(curSortWay == SORT_BY_RunningProcesses){
			initOrUpdatetAppsData(curSortWay);
			updateMemorySizeLayout();
		}
	}

	/**
	 * 更新内存使用情况
	 */
	private void updateMemorySizeLayout(){	
		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memInfo);
		
		mMemInfoReader.readMemInfo();
        long availMem = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize()
                - SECONDARY_SERVER_MEM;        
        if (availMem < 0)  availMem = 0;
        long totalSize = mMemInfoReader.getTotalSize();
//        long usedMem = totalSize-availMem;
        long usedMem = totalSize-memInfo.availMem;
        
        MemorySizeTextView.setText(String.format(getString(R.string.memory_details),
        		Utils.dealMemorySize(this, usedMem),
        		Utils.dealMemorySize(this, totalSize)));
        
        int percent = (int)(usedMem*100/totalSize);    
        percentTextView.setText(String.format(getString(R.string.memory_percent),percent+"%"));
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
		if(allAppsList != null){
			allAppsList.clear();
		}
		if(letterToast != null){
			letterToast.releaseObject();
		}
	}
	
	@Override
	public void onTouchingLetterChanged(String s, float positionOfY) {
		// TODO Auto-generated method stub
//		FastIndexingLayout =(FrameLayout)mViewList.get(curSortWay).findViewById(R.id.FastIndexingLayout); 
//		letterToast = new LetterToast(FastIndexingLayout);
//		appListview = (AuroraListView)mViewList.get(curSortWay).findViewById(R.id.testappListview);
//		letterToast.LetterChanged(s, positionOfY, appListview, adapter,alphaIndexer(adapter, s), 0);
	}
	
	private int alphaIndexer(BaseAdapter adapter,String s) {		
		int position = -1;
		if(StringUtils.isEmpty(s) || adapter == null){
			return position;
		}
		AppInfo appInfo;
		for (int i = 0; i < adapter.getCount(); i++) {
			appInfo = (AppInfo)adapter.getItem(i);
			String pinin = appInfo.getAppNamePinYin();
			if(appInfo != null && pinin != null && !pinin.isEmpty()){
				if(pinin.startsWith(s)){
					position = i;
					break;
				}
			}
		}
		return position;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if(adapter == null){
			return ;
		}
		
		if(adapter != null && position<adapter.getCount()){
			AppInfo appInfo = (AppInfo)adapter.getItem(position);
			if(appInfo != null && !StringUtils.isEmpty(appInfo.getPackageName())){
			   Intent intent = new Intent(this,AppDetailActivity.class);
			   intent.putExtra(mConfig.ACTION_BAR_STYLE, curActionBarStyle);  
			   intent.putExtra("pkgName", fromPkgName);
			   intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
			   startActivity(intent);
			}
		}	
	}
}
