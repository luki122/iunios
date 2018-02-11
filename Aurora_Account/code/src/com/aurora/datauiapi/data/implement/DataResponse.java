package com.aurora.datauiapi.data.implement;

import com.aurora.datauiapi.data.bean.BaseResponseObject;

public class DataResponse<T extends BaseResponseObject> implements Runnable, Cloneable {
	/**
	 * @uml.property  name="value"
	 */
	public T value;
	/**
	 * @uml.property  name="cacheType"
	 */
	public int cacheType;
	/**
	 * @uml.property  name="arg1"
	 */
	public int arg1; //extra parameter
	/**
	 * @uml.property  name="arg2"
	 */
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
	public void setUserArg1(int arg1){
		this.arg1=arg1;
	}
	public void setUserArg2(int arg2){
		this.arg2 = arg2;
	}
}