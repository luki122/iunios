package com.aurora.datauiapi.data;

import com.aurora.datauiapi.data.interf.INotifiableController;

import android.os.Handler;
import android.os.Looper;

public class ManagerThread extends Thread {
	private  ManagerThread sManagerThread;
	/**
	 * @uml.property  name="mHandler"
	 * @uml.associationEnd  
	 */
	private Handler mHandler;
	
	//private final InfoManager mInfoManager;

	/**
	 * @uml.property  name="mVideoManager"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private  baseManager mMarketManager;
	

	public ManagerThread(baseManager mg) {
		super("ManagerThread");
		mMarketManager = mg;
	
	}
	public  ManagerThread get() {
		if (sManagerThread == null) {
			sManagerThread = this;//new testManagerThread1(mMarketManager);
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
		mMarketManager.setHandler(mHandler);
		Looper.loop();
	}
	
	public   void quit() {
		if (sManagerThread != null) {
			sManagerThread.mHandler.getLooper().quit();
			sManagerThread = null;
		}
	}
	

	public  baseManager market(INotifiableController controller) {
		baseManager vm = get().mMarketManager;
		vm.reset();
		vm.setController(controller);
		return vm;
	}
	


}
