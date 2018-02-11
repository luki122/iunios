package com.aurora.datauiapi.data.implement;

import android.util.Log;

import com.aurora.datauiapi.data.interf.INotifiableManager;

public abstract class CommandBoost<T> implements Runnable {

	public final INotifiableManager mManager;

	public final DataResponse<T> mResponse;

	public CommandBoost(DataResponse<T> response, INotifiableManager manager) {
		mManager = manager;
		mResponse = response;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			doRun();
			mManager.onFinish(mResponse);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("linp", "~~~~~~~~~~~~~~~~CommandBoost caught exception:"+e);
			e.printStackTrace();
		}
	}

	public abstract void doRun() throws Exception;
}
