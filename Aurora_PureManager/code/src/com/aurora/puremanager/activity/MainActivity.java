package com.aurora.puremanager.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.android.internal.util.MemInfoReader;
import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.MainActvitiyListAdapter;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.MainActivityItemData;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.service.WatchDogService;
import com.aurora.puremanager.totalCount.TotalCount;
import com.aurora.puremanager.traffic.TrafficAssistantMainActivity;
import com.aurora.puremanager.utils.Utils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.CircleView;
import com.aurora.puremanager.view.RiseNumberTextView;
import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class MainActivity extends AuroraActivity implements Observer,
		OnItemClickListener {

	private Context mContext;
	private ArrayList<BaseData> itemList;
	private MainActvitiyListAdapter adapter;
	private CircleView mCircleView; // 动画
	private TextView mUserMemory;
	private RiseNumberTextView Memory_Percent;
	private Button mClear;
	private GridView mGridView;
//	private long mSystemMemory; // 系统内存
//	private long mAvailMemory; // 剩余内存
	private int mAppNum;	//所有应用个数
	private int mProgress;
	private static final int CLEAR_MEMORY = 1000;
	private static final int UPDATE_MEMORY = 1001;
	private static final int SET_MEMORY = 1002;
	private static final int SET_BTN_STSTE = 1003;
	private static final int UPDATE_APP_NUMBER = 1004;

	private final Handler updateViewHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		switch (msg.what) {
			case CLEAR_MEMORY:
				mCircleView.setProgress(0);
				Memory_Percent.clearNumber();
				Memory_Percent.setDuration(1500);
				Memory_Percent.start();
				break;
			case UPDATE_MEMORY:
				loadAppInfo();
				break;
			case SET_MEMORY:
				mCircleView.setProgress(mProgress);
				updateViewHandler.sendEmptyMessageDelayed(SET_BTN_STSTE, 3000);
				break;
			case SET_BTN_STSTE:
				mClear.setEnabled(true);
				break;
			case UPDATE_APP_NUMBER:
				loadAppNumber();
				initData();
				break;
			default:
				break;
			}
	    }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if(mConfig.isNative){
        	setContentView(R.layout.activity_main);
        }else{
        	setAuroraContentView(R.layout.activity_main,
            		AuroraActionBar.Type.Empty);
        	setAuroraActionbarSplitLineVisibility(View.GONE);
            getAuroraActionBar().setTitle(R.string.app_name);
            getAuroraActionBar().setBackgroundResource(R.color.main_activity_bg_color);
        }
		
		mCircleView = (CircleView) findViewById(R.id.circle);
		mUserMemory = (TextView) findViewById(R.id.memory_text);
		Memory_Percent = (RiseNumberTextView) findViewById(R.id.appsNumOfA);
		mClear = (Button) findViewById(R.id.men_optimize);
		mClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				mCircleView.setProgress(0);
				clearMemory(mContext);
				updateViewHandler.sendEmptyMessageDelayed(UPDATE_MEMORY, 3000);
			}
		});
		mGridView = (GridView) findViewById(R.id.gridView);
		startService(new Intent(this,WatchDogService.class));
	}

	private void initData() {
		// 初始化listItem数据
		if (itemList == null) {
			itemList = new ArrayList<BaseData>();
		} else {
			itemList.clear();
		}

		MainActivityItemData itemData = new MainActivityItemData();

		// 省电管家
		itemData.setIconRes(R.drawable.powersave_icon);
		itemData.setItemName(getString(R.string.powersave_manage));
		itemData.setComponentName(new ComponentName(this,
				BatterySavingMainActivity.class));
		itemData.setHintStrTail(getString(R.string.intel_mode));
//		itemData.setHintStrTail2(getString(R.string.powersave_manage));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 净化后台
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.puremanage_icon);
		itemData.setItemName(getString(R.string.pure_manage));
		itemData.setComponentName(new ComponentName(this,
				PureBackgroundActivity.class));
		itemData.setHintStrTail(getString(R.string.pure_soft_num_hint));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 应用管理
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_manage);
		itemData.setItemName(getString(R.string.app_manage));
		itemData.setComponentName(new ComponentName(this,
				AppManageActivity.class));
		if(mAppNum == 0){
			updateViewHandler.sendEmptyMessage(UPDATE_APP_NUMBER);
		}
		itemData.setHintStrTail("共" + String.valueOf(mAppNum) + "款应用");
		itemData.setHintStrFront("0");
		itemList.add(itemData);
		
		// 流量管理
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_traffic);
		itemData.setItemName(getString(R.string.net_flow_manage));
		itemData.setComponentName(new ComponentName(this,
				TrafficAssistantMainActivity.class));
		itemData.setHintStrTail(getString(R.string.traffic_prohibit));
//		itemData.setHintStrTail2(getString(R.string.net_flow_manage));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		if (adapter == null) {
			adapter = new MainActvitiyListAdapter(this, itemList);
			mGridView.setAdapter(adapter);
			mGridView.setOnItemClickListener(this);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	final MemInfoReader mMemInfoReader = new MemInfoReader();
	long SECONDARY_SERVER_MEM;
	ActivityManager mAm;
	
	/**
	 * 加载程序必须的数据
	 */
	private void loadAppInfo() {
		ConfigModel.getInstance(this).getAppInfoModel().attach(MainActivity.this);
		
		mAm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memInfo);
		SECONDARY_SERVER_MEM = memInfo.secondaryServerThreshold;
		
		mMemInfoReader.readMemInfo();
        long availMem = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize()
                - SECONDARY_SERVER_MEM;        
        if (availMem < 0)  availMem = 0;
        long totalSize = mMemInfoReader.getTotalSize();
//        long usedMem = totalSize-availMem;
        long usedMem = totalSize-memInfo.availMem;
        
        String Already_used= Utils.dealMemorySize(this, usedMem);
        String sysMemory = Utils.dealMemorySize(this, totalSize);
        /*修改获取系统内存方式				gaoming  20160226-- start*/
//        MemorySizeTextView.setText(String.format(getString(R.string.memory_details),
//        		Utils.dealMemorySize(this, usedMem),
//        		Utils.dealMemorySize(this, totalSize)));
//        
//        int percent = (int)(usedMem*100/totalSize);    
//        percentTextView.setText(String.format(getString(R.string.memory_percent),percent+"%"));
		
//		String Already_used;
		
//		String sysMemory = getTotalMemory();
//		getAvailMemory();

//		if(mSystemMemory < mAvailMemory){
//			String used = Formatter.formatFileSize(getBaseContext(),
//					mSystemMemory - mAvailMemory / 1024);// Byte转换为KB或者MB，内存大小规格化
//			if ( Float.parseFloat(used.substring(0, used.length() - 3)) > 
//			Float.parseFloat(sysMemory.substring(0, sysMemory.length() - 3))) {
//				Already_used = used.substring(0, used.length() - 2) + "MB";
//			} else {
//				Already_used = used.substring(0, used.length() - 2) + "GB";
//			}
//		} else {
//			Already_used = Formatter.formatFileSize(getBaseContext(),
//					mSystemMemory - mAvailMemory);// Byte转换为KB或者MB，内存大小规格化
//		}

//		float system = Float.parseFloat(sysMemory.substring(0,
//				sysMemory.length() - 2));
//		float user = Float.parseFloat(Already_used.substring(0,
//				Already_used.length() - 2));
//		if (user > system) {
//			user = user / 1024;
//		}
		
//		int sys = (int) Math.ceil(system); // 系统内存取整
//		float availMemory = user / sys;
//		DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
//		String Percent = decimalFormat.format(availMemory).substring(2);
        /*修改获取系统内存方式				gaoming  20160226-- end*/
        
        int percent = (int)(usedMem*100/totalSize);
		Memory_Percent.withNumber( Integer.valueOf(percent).intValue());
		Memory_Percent.setDuration(1500);
		Memory_Percent.start();

		mUserMemory
				.setText(Already_used	+ getResources().getText(R.string.already_user) + " / " 
						+ sysMemory+ getResources().getText(R.string.storager));

		mProgress = Integer.valueOf(percent).intValue();
		updateViewHandler.sendEmptyMessage(SET_MEMORY);
		
		loadAppNumber();
	}

	private void loadAppNumber(){
		if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
			   updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
			   int UserApp = ConfigModel.getInstance(this).getAppInfoModel()
						.getUserAppsNum();
				int sysApp = ConfigModel.getInstance(this).getAppInfoModel()
						.getSysAppsNum();
				mAppNum = UserApp + sysApp;
		    }
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadAppInfo();
		initData();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// mCircleView.setProgress(0);
	}

	@Override
	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub
		// startCircleAnim();
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (adapter != null && arg2 < adapter.getCount()) {
			MainActivityItemData item = (MainActivityItemData) adapter
					.getItem(arg2);
			if (item == null) {
				return;
			}
			// add data count
			String itemname = item.getItemName();

			if (itemname.equals(getString(R.string.jurisdiction_manage))) {
				new TotalCount(MainActivity.this, "1", 1).CountData();
			} else if (itemname.equals(getString(R.string.app_manage))) {
				new TotalCount(MainActivity.this, "2", 1).CountData();
			}
			Intent intent = new Intent();
			intent.setComponent(item.getComponentName());
			startActivity(intent);
		}
	}

	/**
	 * 获取android当前可用内存大小
	 * 
	 * @return
	 */
	/*修改获取系统内存方式				gaoming  20160226-- start*/
	/*private String getAvailMemory() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		mAvailMemory = mi.availMem;
		String men = Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化
		return men;
	}*/
	/*修改获取系统内存方式				gaoming  20160226-- end*/

	/*修改获取系统内存方式				gaoming  20160226-- start*/
	/**
	 * 获取android系统内存大小
	 * 
	 * @return
	 */
	/*private String getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		String men = "";
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			mSystemMemory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			if(mSystemMemory < 0){
				mSystemMemory = Integer.valueOf(arrayOfString[1]).intValue();
				String temp = Formatter.formatFileSize(getBaseContext(), mSystemMemory);// Byte转换为KB或者MB，内存大小规格化
				men = temp.substring(0, temp.length() - 2) + "GB";
			}else{
				 men = Formatter.formatFileSize(getBaseContext(), mSystemMemory);// Byte转换为KB或者MB，内存大小规格化
			}
			
//			mSystemMemory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		} catch (IOException e) {
		}
//		String men = Formatter.formatFileSize(getBaseContext(), mSystemMemory);// Byte转换为KB或者MB，内存大小规格化
		return men;
	}*/
	/*修改获取系统内存方式				gaoming  20160226-- end*/
	/**
	 * 清理内存
	 */
	synchronized private void clearMemory(final Context context) {
		mClear.setEnabled(false);
		new Thread(){
			public void run(){
				ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
				if (list != null){
					updateViewHandler.sendEmptyMessage(CLEAR_MEMORY);	
					for (int i = 0; i < list.size(); i++) {
						ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
						if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
							//过滤天气和时钟，进程杀掉后数据不更新
								if(apinfo.processName.contains("com.aurora.weatherforecast")){
									
								}else if(apinfo.processName.contains("com.android.deskclock")){
									
								}else if(apinfo.processName.contains("com.aurora.launcher")){
									
								}else{
									activityManger.killBackgroundProcesses(apinfo.processName);
								}
						}
					}
//					updateViewHandler.sendEmptyMessageDelayed(UPDATE_MEMORY, 3000);
				}
			}
		}.start();
	}
}
