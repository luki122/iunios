package com.aurora.datauiapi.data.implement;

import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.exception.SessionExpiredException;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.account.AccountApp;
import com.aurora.account.util.Log;
import com.aurora.account.util.WifiHelper;
import com.aurora.account.util.WifiHelper.WifiStateException;



public abstract class Command<T extends BaseResponseObject> implements Runnable {
	public static final String TAG = "Command";
	/**
	 * @uml.property  name="mRetryCount"
	 */
	public int mRetryCount = 0;
	/**
	 * @uml.property  name="mStarted"
	 */
	//public long mStarted = 0;
	/**
	 * @uml.property  name="mManager"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public final INotifiableManager mManager;
	/**
	 * @uml.property  name="mResponse"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public final DataResponse<T> mResponse;
	
	// TODO Disable this when not needed anymore
	/**
	 * @uml.property  name="mCaller"
	 */
	//public final StackTraceElement mCaller;
	
	public static final int MAX_RETRY = 5;
	
	private boolean isCacheData = false;
	

	
	public boolean isCacheData() {
		return isCacheData;
	}
	public void setCacheData(boolean isCacheData) {
		this.isCacheData = isCacheData;
	}
	
	public Command(DataResponse<T> response, INotifiableManager manager) {
		mManager = manager;
		mResponse = response;
		//mStarted = System.currentTimeMillis();
		//mCaller = new Throwable().fillInStackTrace().getStackTrace()[2];
	}
	public Command(DataResponse<T> response, INotifiableManager manager,boolean cacheData) {
		mManager = manager;
		mResponse = response;
		isCacheData = cacheData;
		//mStarted = System.currentTimeMillis();
		//mCaller = new Throwable().fillInStackTrace().getStackTrace()[2];
	}
	
	
	public void run() {
		try {
			mRetryCount ++;
			Log.d(TAG, "Running command counter: " + mRetryCount);
			if(mRetryCount > MAX_RETRY) return;
			WifiHelper.assertWifiState(AccountApp.getInstance());
			doRun();
			//QQLiveLog.i(mCaller.getClassName(), "*** " + mCaller.getMethodName() + ": " + (System.currentTimeMillis() - mStarted) + "ms");
			if (mResponse != null && mResponse.value != null &&
			        mResponse.value.getCode() == BaseResponseObject.CODE_ERROR_SESSION_EXPIRED) {
			    throw new SessionExpiredException("Server session is expired.");
			}
			mManager.onFinish(mResponse);
		} catch (WifiStateException e) {
			mManager.onWrongConnectionState(e.getState(), this);
		} catch (Exception e) {
			mManager.onError(e,this);
		}
	}
	
	public abstract void doRun() throws Exception;

}
