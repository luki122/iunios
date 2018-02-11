package com.aurora.downloadIcon.mananger;

import com.aurora.downloadIcon.struct.INotifiableController;

import android.os.Handler;
import android.os.Looper;

public class ThreadManager extends Thread {
	public static final String TAG = "ThreadManager";
	private static ThreadManager sThreadManager;
	private DownloadManager mDownloadManager;
	private Handler mHandler;
	private ThreadManager(){
		super("ThreadManager");
		mDownloadManager = new DownloadManager();
	}
	@Override
	public void run() {
		super.run();
		Looper.prepare();
		mHandler =new Handler();
		mDownloadManager.setThreadHandle(mHandler);
		Looper.loop();
	}
	
	public synchronized static void quit() {
		if (sThreadManager != null) {
			sThreadManager.mHandler.getLooper().quit();
			sThreadManager = null;
		}
	}
	
	public static ThreadManager get() {
		if (sThreadManager == null) {
			sThreadManager = new ThreadManager();
			sThreadManager.start();
			// thread must be entirely started
			while (sThreadManager.mHandler == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return sThreadManager;
	}
	
	public static DownloadManager getDownloadManager(INotifiableController controller){
		final DownloadManager dm = get().mDownloadManager;
		dm.reset();
		dm.setController(controller);
		return dm;
	}

}
