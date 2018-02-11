package com.android.settings.lscreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.R;

import com.android.settings.lscreen.LetterSideBar.OnTouchingLetterChangedListener;
import com.android.settings.lscreen.ls.LSCustomPreference;
import com.android.settings.lscreen.ls.LSOperator;
import com.android.settings.lscreen.Utils;
import com.secure.stickylistheaders.StickyListHeadersListView;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import android.os.SystemProperties;

public class LSAppActivity extends AuroraActivity implements OnClickListener,
                                                               OnScrollListener,
                                                               OnTouchingLetterChangedListener,
                                                               LSAppObserver
                                                               {
/*    public static String OUTPUT_DIRECTORY = Environment  
            .getExternalStorageDirectory().getAbsolutePath() + "/lsimage";*/
	public static final String PKGNAME="pkgname";
	public static final String BUNDLE_STAG="bundle_stag";
	private AddLSAppListAdapter adapter;
	private List<BaseData> appInfos;
	private List<BaseData> sameFirstCharAppList=new ArrayList<BaseData>();;
	private LSProgressDialog lScreenProgressDialog;
	private ListView listView;
	private LetterToast letterToast;
	private static final int MSG_DATA_LOAD_SUC=1;
	private TextView sureText;
	private DataArrayList<String> choiceAppList;
	private LetterSideBar letterSideBar;
	private DataArrayList<String> pkgList;
	public static String mTransferPkgName=null;
	
	private int lastDealFirstVisibleItem = -1;
	
	private Handler dateHandler=new Handler()
	{
		public void handleMessage(Message msg) 
		{
			if(msg.what==MSG_DATA_LOAD_SUC)
			{
				handleAppData(appInfos);
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isTransferPkg();
    	setAuroraContentView(R.layout.add_privacy_app_activity,
        		AuroraActionBar.Type.Normal);
    	getAuroraActionBar().setTitle(R.string.add_privacy_app);
        getAuroraActionBar().addItem(R.layout.add_privacy_right_action_bar_item,
        		R.id.add_privacy_action_bar_item);
        choiceAppList = new DataArrayList<String>();
        initView();
        AuroraLSManageModel.getInstance(this).attach(this);
        initDateOrRefresh();
	}
	
	/*
	 * 对点击传递过来的包名进行操作，将默认的包名显示在界面上
	 */
	LSSameFirstCharAppData mCacheSsameFirstCharAppData = new LSSameFirstCharAppData();
	private void isTransferPkg()
	{
		Bundle bundle = getIntent().getBundleExtra(BUNDLE_STAG);
		if(bundle!=null)
		{
		    String packageName=bundle.getString(PKGNAME);
		    mTransferPkgName=packageName;
		    Log.d("gd","packageName="+packageName);
		    if(packageName.equals(LSCustomPreference.ADD_ITEM_FLAG))
		    {
		    	return ;
		    }
        	String labelText = "*";
        	mCacheSsameFirstCharAppData.setFirstChar(labelText);
			/*
			 * 从所有APP中获取对应的APP信息
			 */
			AppInfo appInfo = (AppInfo) AuroraLSManageModel.getInstance(this)
					.setPkgToAppInfo(packageName);
			if(appInfo!=null)
			{
				appInfo.setAppName("无");
				mCacheSsameFirstCharAppData.getAppList().add(appInfo);
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
//		updateSureText();
    }
	
	
	private void handleAppData(List<BaseData> appInfos)
	{
	    List<BaseData> aCacheAppInfos=new ArrayList<BaseData>();
		if(lScreenProgressDialog!=null)
		{
			lScreenProgressDialog.close();
		}
		/*
		 * 当进行删除操作时候，
		 * 更新界面的时候;
		 */
//		if(delOrUpdate)
//		{
//			if(sameFirstCharAppList!=null)
//			{
//				Log.d("gd", " sameFirstCharAppList is clear  ");
//				sameFirstCharAppList.clear();
//				mCacheSsameFirstCharAppData.clear();
//				LScreenActivity.mTransferPkgName="";
//			}
//			delOrUpdate=false;
//		}
		/*
		 * 过滤掉两个APP
		 */
		Log.d("gd", "  appInfos size="+appInfos.size());
		for(int i=0;i<appInfos.size();i++)
		{
			AppInfo appInfo = (AppInfo)appInfos.get(i);
    		if(appInfo.getAppNamePinYin() == null && appInfo.getAppNamePinYin().length()==0)
    		{
    			continue;
	        }
    		
    		if(appInfo.getPackageName().equals(mTransferPkgName))
    		{
    			continue;
    		}
    		if(appInfo.getPackageName().equals(LSOperator.IUNI_MMS) || appInfo.getPackageName().equals(LSOperator.IUNI_EMAIL))
    		{
    			String labelText = mCacheSsameFirstCharAppData.getFirstChar();
    			if(labelText.equals(""))
    			{
    				labelText="*";
    				mCacheSsameFirstCharAppData.setFirstChar(labelText);
    			}
    			Log.d("gd", " in * app info =" + appInfo.getAppName()+"    "+appInfo.getAppNamePinYin() + "  "+appInfo.getPackageName());
    			mCacheSsameFirstCharAppData.getAppList().add(appInfo);
    		    continue;
    		}
    		aCacheAppInfos.add(appInfo);
		}
		
		Log.d("gd", " in *  app size is " + mCacheSsameFirstCharAppData.getAppList().size());
		
		if(mCacheSsameFirstCharAppData.getAppList().size()!=0)
		{
			sameFirstCharAppList.add(mCacheSsameFirstCharAppData);
		}
		
		sortList(aCacheAppInfos);
		
    	LSSameFirstCharAppData sameFirstCharAppData = null;
    	
        Log.d("gd", "  aCacheAppInfos size="+aCacheAppInfos.size());
    	for(int i=0;i<aCacheAppInfos.size();i++)
    	{
    		AppInfo appInfo = (AppInfo)aCacheAppInfos.get(i);
    		if(appInfo.getAppNamePinYin() == null &&
    				appInfo.getAppNamePinYin().length()==0)
    		{
    			continue;	
	        }
    		String labelText=null;
			labelText = appInfo.getAppNamePinYin().substring(0, 1);
//			Log.d("gd", " appName=" + appInfo.getAppName() + "  labelText="+ labelText);
			if (labelText == null)
			{
				continue;
			}
			if (labelText.compareTo("A") < 0 || labelText.compareTo("Z") > 0) 
			{
				labelText = "#";
				Log.d("gd", " in # app info ="+appInfo.getAppName()+ "   "+ appInfo.getAppNamePinYin() + "  "+appInfo.getPackageName());
			}
			if (sameFirstCharAppData == null || !labelText.equals(sameFirstCharAppData.getFirstChar())) 
			{
				sameFirstCharAppData = new LSSameFirstCharAppData();
				sameFirstCharAppData.setFirstChar(labelText);
				sameFirstCharAppList.add(sameFirstCharAppData);
			}
			sameFirstCharAppData.getAppList().add(appInfo);
    	}
    	
    	if(adapter==null)
    	{
    		adapter=new AddLSAppListAdapter(this, sameFirstCharAppList);
    		listView.setAdapter(adapter);
    	}else
    	{
    		Log.d("gd", "adapter notifyDataSetChanged ");
    		adapter.notifyDataSetChanged();
    	}
		
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
	
	
	
	private void initDateOrRefresh()
	{
		
		if(lScreenProgressDialog==null)
		{
			lScreenProgressDialog=new LSProgressDialog();
		}
		
		lScreenProgressDialog.show(this, getString(R.string.init),
				getString(R.string.please_wait_init));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				appInfos = AuroraLSManageModel.getInstance(LSAppActivity.this).queryAppInfo();
				dateHandler.sendEmptyMessage(MSG_DATA_LOAD_SUC);
			}
		}).start();
	}

	
    /**
     * 更新当前选中应用的个数视图
     */
    public synchronized void updateSureText(){
    	if(sureText != null){
    		Log.d("gd", "Size of choice App="+choiceAppList.size());
    		if(choiceAppList == null || choiceAppList.size() == 0){
    			sureText.setVisibility(View.INVISIBLE);
    		}else{
    			sureText.setVisibility(View.VISIBLE);
    			sureText.setText(String.format(getString(R.string.sure_num),choiceAppList.size()));
    		}
    		/**
    		 * 限制选中个数显示；
    		 */
    		if((choiceAppList.size() + LSContentProvideImp.getLSAppInfo(LSAppActivity.this).size()) > 3)
    		{
    			sureText.setVisibility(View.INVISIBLE);
    			Toast.makeText(LSAppActivity.this, "已经超出可以保存的总数据", 50);
    		}
    	}
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
		LSSameFirstCharAppData sameFirstCharAppData =(LSSameFirstCharAppData)sameFirstCharAppList.
				get(firstVisibleItem);
		if(sameFirstCharAppData == null){
			return ;
		}
		
		letterSideBar.setCurChooseTitle(sameFirstCharAppData.getFirstChar());	
		lastDealFirstVisibleItem = firstVisibleItem;	
	}
    
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
    

	@Override
	protected void onDestroy() {
		AuroraLSManageModel.getInstance(this).detach(this);
		releaseObject();
		if(lScreenProgressDialog!=null)
		{
			lScreenProgressDialog.close();
			lScreenProgressDialog=null;
		}
		super.onDestroy();
		
	}

	private void releaseObject(){
		
		if(sameFirstCharAppList != null){
			sameFirstCharAppList.clear();
		}	
		
		if(choiceAppList != null){
			choiceAppList.clear();
		}
	}
	
	public DataArrayList<String> getChoiceAppList()
	{
		
		return choiceAppList;
	}


	@Override
	public void onTouchingLetterChanged(String s, float positionOfY) {
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
		LSSameFirstCharAppData sameFirstCharAppData;
		for (int i = 0; i < adapter.getCount(); i++) {
			sameFirstCharAppData = (LSSameFirstCharAppData)adapter.getItem(i);
			if(sameFirstCharAppData != null && 
					sameFirstCharAppData.getFirstChar().startsWith(s)){
				position = i;
				break;
			}
		}
		return position;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.lscreen, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sureText:
//			saveLScreenApp();
			break;
		}
		
	}
	
    public void delOrUpdateLScreenApp(String packageName)
    {
    	DataArrayList<String> appDatas =new DataArrayList<String>(); 
        if(packageName!=null)
        {
        	appDatas.add(packageName);
        	AuroraLSManageModel.getInstance(this).delLSApp(appDatas);
        }
    }
	
	public void saveLScreenApp()
	{
		if(lScreenProgressDialog!=null)
		{
			lScreenProgressDialog.show(this, getString(R.string.init),
					getString(R.string.please_wait_init));
		}else
		{
			return;
		}
		
		final int MSG_LOAD_FINISH=2;
		
	    final Handler handler= new Handler(){

			@Override
			public void handleMessage(Message msg) {
                if(msg.what==MSG_LOAD_FINISH)
                {
                	lScreenProgressDialog.close();
                    finish();
                }
			}
		};
		
		if(pkgList==null)
		{
			pkgList=new DataArrayList<String>();
		}else
		{
			pkgList.clear();
		}
		
		final ArrayList<AppInfo> appDatas= new ArrayList<AppInfo>();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(choiceAppList.size()==0)
				{
					return ;
				}
				
				for(int i=0;i<choiceAppList.size();i++){
					try{
						if(choiceAppList.get(i)==null)
						{
						    continue;
						}
						
						pkgList.add(choiceAppList.get(i));
						
						Utils.savaIconInFile(LSAppActivity.this,choiceAppList.get(i),LSOperator.TARGETBECOMEPATH);
						
						Log.d("gd", " choiceAppList="+choiceAppList.get(i).toString());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				AuroraLSManageModel.getInstance(LSAppActivity.this).addLSApp(pkgList);
				handler.sendEmptyMessage(MSG_LOAD_FINISH);
			}
		}).start();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		setHighlightTitle(firstVisibleItem);
	}

	@Override
	public void initOrUpdateLSApp(LSAppSubject subject) {
		
	}

	@Override
	public void addOrUpdateLSApp(LSAppSubject subject, List<AppInfo> datas) {
		
	}
	
    /*
     * 该参数只在该界面删除更新界面时候有用
     */
//	private boolean delOrUpdate=false;
	@Override
	public void delOrUpdateLSApp(LSAppSubject subject, List<AppInfo> datas) {
		Log.d("gd", "LScreenActivity  delOrUpdateLSApp ");
//		delOrUpdate=true;
//		initDateOrRefresh();
	}

	@Override
	public void allAppAchieve(LSAppSubject subject, List<AppInfo> datas) {
		// TODO Auto-generated method stub
		
	}
}
