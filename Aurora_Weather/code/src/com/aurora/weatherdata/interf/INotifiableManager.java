package com.aurora.weatherdata.interf;

import com.aurora.weatherdata.implement.Command;
import com.aurora.weatherdata.implement.DataResponse;

public interface INotifiableManager {

	public void onFinish(DataResponse<?> response);
	public void onWrongConnectionState(int state, Command<?> cmd);
	public void onError(Exception e, Command<?> cmd);
	public void onMessage(String message);
	public void onMessage(int code, String message);
	public void retry();
	// public void retryAll();

}