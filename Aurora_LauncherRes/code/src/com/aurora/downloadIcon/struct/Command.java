package com.aurora.downloadIcon.struct;

import com.aurora.downloadIcon.utils.WifiHelper.WifiStateException;

import android.R.integer;

public abstract class Command<T> implements Runnable {
	private int mRetryCount = 0;

	public static final int MAX_RETRY = 5;

	public int mModuleId;

	public String mCoverId;

	private DataResponse<T> mResponse;

	private INotifiable mINotifiable;

	public Command(DataResponse<T> response, INotifiable iNotifiable) {
		mResponse = response;
		mINotifiable = iNotifiable;
	}

	@Override
	public void run() {
		try {
			mRetryCount++;
			if (mRetryCount > MAX_RETRY)
				return;
			doRun();
			mINotifiable.onFinish(mResponse);
		} catch (WifiStateException e) {
			mINotifiable.onWrongConnectionState(e.getState(), this);
		} catch (Exception e) {
			mINotifiable.onError(e, this, mCoverId, mModuleId);
		}
	}

	public abstract void doRun() throws Exception;
}
