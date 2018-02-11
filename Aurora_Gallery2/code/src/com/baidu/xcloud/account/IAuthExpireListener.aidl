package com.baidu.xcloud.account;

/**
 * Callback interface for logout.
 * 
 */
interface IAuthExpireListener {

	/**
	 * When logout completed, this method will be called..
	 * 
	 * @param result
	 *            true represent success or false represent failed
	 * 
	 */
	 void onResult(boolean result);
	 
    /**
     * When Exception happens, this method will be called.
     * 
     * @param errorMsg
     *            Error message.
     */
     void onException(String errorMsg);
}