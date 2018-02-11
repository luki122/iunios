package com.android.phone;

import android.os.Handler;
import android.content.Intent;
import android.os.IBinder;
import android.app.Service;

public class FloatWindowService extends Service {

	/**
	 * 用于在线程中创建或移除悬浮窗。
	 */
	private Handler handler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 开启定时器，每隔0.5秒刷新一次
		FloatWindowManager.createWindow(getApplicationContext());
		return super.onStartCommand(intent, flags, startId);
	}
	
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        FloatWindowManager.removeWindow(getApplicationContext());  
    }  


}