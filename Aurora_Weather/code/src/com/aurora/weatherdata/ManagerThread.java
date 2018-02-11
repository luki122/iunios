package com.aurora.weatherdata;

import com.aurora.weatherdata.interf.INotifiableController;

import android.os.Handler;
import android.os.Looper;

public class ManagerThread extends Thread {

	private ManagerThread sManagerThread;

	private Handler mHandler;

	// private final InfoManager mInfoManager;

	private BaseManager mManager;

	public ManagerThread(BaseManager mg) {
		super("ManagerThread");
		mManager = mg;
	}

	public ManagerThread get() {
		if (sManagerThread == null) {
			sManagerThread = this; // new testManagerThread1(mMarketManager);
			sManagerThread.start();
			// thread must be entirely started
			while (sManagerThread.mHandler == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return sManagerThread;
	}

	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		mManager.setHandler(mHandler);
		Looper.loop();
	}

	public void quit() {
		if (sManagerThread != null) {
			sManagerThread.mHandler.getLooper().quit();
			sManagerThread = null;
		}
	}

	public BaseManager init(INotifiableController controller) {
		BaseManager vm = get().mManager;
		vm.reset();
		vm.setController(controller);
		return vm;
	}

}