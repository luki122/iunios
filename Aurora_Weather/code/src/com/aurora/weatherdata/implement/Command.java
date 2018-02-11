package com.aurora.weatherdata.implement;

import android.content.Context;

import com.aurora.weatherdata.interf.INotifiableManager;
import com.aurora.weatherdata.util.Log;
import com.aurora.weatherdata.util.WifiHelper;
import com.aurora.weatherdata.util.WifiHelper.WifiStateException;

public abstract class Command<T> implements Runnable {

	public static final String TAG = "Command";

	public int mRetryCount = 0;

	// public long mStarted = 0;

	public final INotifiableManager mManager;

	public final DataResponse<T> mResponse;

	// public final StackTraceElement mCaller;

	public static final int MAX_RETRY = 5;

	private Context mContext;
	private boolean isCacheData = false;

	public boolean isCacheData() {
		return isCacheData;
	}

	public void setCacheData(boolean isCacheData) {
		this.isCacheData = isCacheData;
	}

	public Command(Context context, DataResponse<T> response, INotifiableManager manager) {
		mManager = manager;
		mResponse = response;
		mContext = context;
		// mStarted = System.currentTimeMillis();
		// mCaller = new Throwable().fillInStackTrace().getStackTrace()[2];
	}

	public Command(Context context, DataResponse<T> response, INotifiableManager manager, boolean cacheData) {
		mManager = manager;
		mResponse = response;
		mContext = context;
		isCacheData = cacheData;
		// mStarted = System.currentTimeMillis();
		// mCaller = new Throwable().fillInStackTrace().getStackTrace()[2];
	}

	public void run() {
		try {
			mRetryCount ++;
			Log.d(TAG, "Running command counter: " + mRetryCount);

			if (mRetryCount > MAX_RETRY) return;

			if (!isCacheData) WifiHelper.assertWifiState(mContext);

			doRun();
			// QQLiveLog.i(mCaller.getClassName(), "*** " + mCaller.getMethodName() + ": " 
			// + (System.currentTimeMillis() - mStarted) + "ms");
			mManager.onFinish(mResponse);
		} catch (WifiStateException e) {
			mManager.onWrongConnectionState(e.getState(), this);
		} catch (Exception e) {
			mManager.onError(e, this);
		}
	}

	public abstract void doRun() throws Exception;

}