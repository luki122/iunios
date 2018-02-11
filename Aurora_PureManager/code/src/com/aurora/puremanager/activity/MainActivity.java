package com.aurora.puremanager.activity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.Log;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.MainActvitiyListAdapter;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.MainActivityItemData;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.model.RunningState;
import com.aurora.puremanager.permission.PermissionMrgActivity;
import com.aurora.puremanager.service.WatchDogService;
import com.aurora.puremanager.totalCount.TotalCount;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.CircleView;
import com.aurora.puremanager.view.MyProgressDialog;

public class MainActivity extends AuroraActivity implements Observer,
		OnItemClickListener {

	private Context mContext;
	private ArrayList<BaseData> itemList;
	private MainActvitiyListAdapter adapter;
	private CircleView mCircleView; // 动画
	private TextView mUserMemory, Memory_Percent;
	private Button mClear;
	private long mSystemMemory; // 系统内存
	private long mAvailMemory; // 剩余内存
	private int mProgress;
	private static final int CLEAR_MEMORY = 1000;
	private static final int UPDATE_MEMORY = 1001;
	private static final int SET_MEMORY = 1002;

	private final Handler updateViewHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		switch (msg.what) {
			case CLEAR_MEMORY:
				mCircleView.setProgress(0);
				break;
			case UPDATE_MEMORY:
				loadAppInfo();
				break;
			case SET_MEMORY:
				mCircleView.setProgress(mProgress);
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
            getAuroraActionBar().setTitle(R.string.app_name);
            getAuroraActionBar().getTitleView().setTextColor(R.color.white);
            getAuroraActionBar().setBackgroundResource(R.color.main_title_color);
        }
		
		mCircleView = (CircleView) findViewById(R.id.circle);
		mUserMemory = (TextView) findViewById(R.id.memory_text);
		Memory_Percent = (TextView) findViewById(R.id.appsNumOfA);
		mClear = (Button) findViewById(R.id.men_optimize);
		mClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				mCircleView.setProgress(0);
				clearMemory(mContext);
			}
		});
		initData();
		startService(new Intent(this,WatchDogService.class));
	}

	private void initData() {
		// 初始化listItem数据
		if (itemList == null) {
			itemList = new ArrayList<BaseData>();
		} else {
			itemList.clear();
		}

		// 权限管理
		MainActivityItemData itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.permission_icon);
		itemData.setItemName(getString(R.string.jurisdiction_manage));
		itemData.setComponentName(new ComponentName(this,
				PermissionMrgActivity.class));
		itemData.setHintStrTail(getString(R.string.permission_control));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		// 应用管理
		itemData = new MainActivityItemData();
		itemData.setIconRes(R.drawable.app_manage);
		itemData.setItemName(getString(R.string.app_manage));
		itemData.setComponentName(new ComponentName(this,
				AppManageActivity.class));
		itemData.setHintStrTail(getString(R.string.auto_start_prohibit));
		itemData.setHintStrFront("0");
		itemList.add(itemData);

		if (adapter == null) {
			adapter = new MainActvitiyListAdapter(this, itemList);
			GridView gridView = (GridView) findViewById(R.id.gridView);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(this);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 加载程序必须的数据
	 */
	private void loadAppInfo() {
		
		ConfigModel.getInstance(this).getAppInfoModel().attach(MainActivity.this);
		 if(ConfigModel.getInstance(this).getAppInfoModel().isAlreadyGetAllAppInfo()){
			   updateOfInit(ConfigModel.getInstance(this).getAppInfoModel());
		    }
		
		String sysMemory = getTotalMemory();
		getAvailMemory();

		String Already_used = Formatter.formatFileSize(getBaseContext(),
				mSystemMemory - mAvailMemory);// Byte转换为KB或者MB，内存大小规格化

		float system = Float.parseFloat(sysMemory.substring(0,
				sysMemory.length() - 2));
		float user = Float.parseFloat(Already_used.substring(0,
				Already_used.length() - 2));
		if (user > system) {
			user = user / 1024;
		}
		int sys = (int) Math.ceil(system); // 系统内存取整
		float availMemory = user / sys;
		DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
		String Percent = decimalFormat.format(availMemory).substring(2);
		Memory_Percent.setText(Percent);

		mUserMemory
				.setText(Already_used.substring(0, Already_used.length() - 1)
						+ getResources().getText(R.string.already_user) + "/" + String.valueOf(sys) + "G"
						+ getResources().getText(R.string.storager));

		mProgress = Integer.valueOf(Percent).intValue();
		updateViewHandler.sendEmptyMessage(SET_MEMORY);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadAppInfo();
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
	private String getAvailMemory() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		mAvailMemory = mi.availMem;
		String men = Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化
		return men;
	}

	/**
	 * 获取android系统内存大小
	 * 
	 * @return
	 */
	private String getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
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
			localBufferedReader.close();

		} catch (IOException e) {
		}
		String men = Formatter.formatFileSize(getBaseContext(), mSystemMemory);// Byte转换为KB或者MB，内存大小规格化
		return men;
	}

	/**
	 * 清理内存
	 */
	private void clearMemory(Context context) {
		ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null){
			updateViewHandler.sendEmptyMessage(CLEAR_MEMORY);	
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
				String[] pkgList = apinfo.pkgList;
				if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					for (int j = 0; j < pkgList.length; j++) {
						activityManger.killBackgroundProcesses(pkgList[j]);
					}
				}
			}
			updateViewHandler.sendEmptyMessageDelayed(UPDATE_MEMORY, 3000);
		}
	}
}
