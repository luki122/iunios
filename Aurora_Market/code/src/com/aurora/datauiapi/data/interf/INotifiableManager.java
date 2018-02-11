package com.aurora.datauiapi.data.interf;

import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;

public interface INotifiableManager {
	
	public void onFinish(DataResponse<?> response);
	public void onWrongConnectionState(int state, Command<?> cmd);
	public void onError(Exception e, Command<?> cmd);
	public void onMessage(String message);
	public void onMessage(int code, String message);
	//public void retryAll();
	public void retry();
	
}
