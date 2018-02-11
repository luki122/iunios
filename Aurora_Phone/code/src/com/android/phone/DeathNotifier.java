package com.android.phone;
import android.os.RemoteException;
import android.os.IBinder.DeathRecipient;
import android.util.Log;
import android.os.Handler;

import com.gionee.aora.numarea.export.INumAreaManager;
 
public class DeathNotifier implements DeathRecipient {
	private final String TAG =  "DeathNotifier";
    private static INumAreaManager mManager;	
	private static Handler mHandler = new Handler();
    public DeathNotifier(INumAreaManager mgr) {
    	mManager = mgr;
    }
	
    public void linkToDeath() throws RemoteException {
    	mManager.asBinder().linkToDeath(this, 0);
    }

    public void unlinkToDeath() {
    	mManager.asBinder().unlinkToDeath(this, 0);
    }
    @Override
    public void binderDied() {
    	Log.i(TAG, "binderDied");
        unlinkToDeath();
        mHandler.postDelayed(new Runnable(){
        	public void run() {
                NumberAreaUtil.bindService(null);	
        	}
        }, 1000);
    }
}