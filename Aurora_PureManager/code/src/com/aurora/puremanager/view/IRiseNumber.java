package com.aurora.puremanager.view;

public interface IRiseNumber {
	public void start();

	public void withNumber(float number);

	public void withNumber(int number);
	
	public void clearNumber();

	public void setDuration(long duration);

	public void setOnEndListener(RiseNumberTextView.EndListener callback);
}
