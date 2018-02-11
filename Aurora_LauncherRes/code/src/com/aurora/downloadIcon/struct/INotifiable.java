package com.aurora.downloadIcon.struct;

public interface INotifiable {
	public void onFinish(DataResponse<?> response);
	public void onWrongConnectionState(int state, Command<?> cmd);
	public void onError(Exception e,Command<?> cmd,String coverId,int moduleId);
	public void onMessage(String message);
	public void onMessage(int code, String message);
	public void retry();
}
