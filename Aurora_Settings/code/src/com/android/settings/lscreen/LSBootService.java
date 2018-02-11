package com.android.settings.lscreen;

import java.io.IOException;

import com.android.settings.lscreen.ls.LSOperator;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LSBootService extends Service {
	
	
	public static final String WHCHAT_PACKAGENAME="com.tencent.mm";
	public static final String MMS_PACKAGENAME="com.android.mms";
    private DataArrayList<String> dataArrayList;
    
    
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("gd", " LSBootService onCreate ");
//		initDefaultDate();    //初始化插入短信事件和微信
	    // 解压图片资源文件到指定的文件目录	
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Utils.unZip(LSBootService.this,"app_icon.zip",LSOperator.TARGETPATH,true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		AuroraLSManageModel.getInstance(LSBootService.this).getALLLSApp();  // 加载获取所有锁屏应用 ；
		registerBroadcastReceiver();
	}
	


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void registerBroadcastReceiver()
	{
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addDataScheme("package");
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		registerReceiver(apkOperaterReceiver, intentFilter);
	}
	
	private BroadcastReceiver apkOperaterReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
			{
	             String packageName = intent.getData().getSchemeSpecificPart();
	             Log.d("gd", " action="+intent.getAction().toString()+ " install packageName="+packageName);
			}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
			{
				 String packageName = intent.getData().getSchemeSpecificPart();
				 Log.d("gd", " action="+intent.getAction().toString()+ " uninstall packageName="+packageName);
				 AuroraLSManageModel.getInstance(LSBootService.this).delLSOrSqllist(packageName);
			}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
			{
				String packageName = intent.getData().getSchemeSpecificPart();
			}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED))
			{
			}
			
		}
	};

}
