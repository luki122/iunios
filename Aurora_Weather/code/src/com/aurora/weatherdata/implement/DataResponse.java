package com.aurora.weatherdata.implement;

public class DataResponse<T> implements Runnable, Cloneable {

	public T value;

	public int cacheType;

	public int arg1; // extra parameter

	public int arg2;

	public void run () {
		// do nothing if not overloaded
	}

	/**
	 * Executed before downloading large files. Overload and return false to 
	 * skip downloading, for instance when a list with covers is scrolling.
	 * @return
	 */
	public boolean postCache() {
		return true;
	}

	public void setUserArg1(int arg1) {
		this.arg1 = arg1;
	}

	public void setUserArg2(int arg2) {
		this.arg2 = arg2;
	}

}