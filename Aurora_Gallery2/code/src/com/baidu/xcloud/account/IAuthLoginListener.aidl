package com.baidu.xcloud.account;

import com.baidu.xcloud.account.AuthResponse;
/**
 * Interface, the call back for OAuth listener.
 */
 interface IAuthLoginListener {


	/**
	 * When login completed successfully, this method will be called.
	 * 
	 * @param response
	 *            
	 */
	 void onComplete(in AuthResponse response);

	/**
	 * When Exception happens, this method will be called.
	 * 
	 * @param errorMsg
	 *            Error message.
	 */
	 void onException(String errorMsg);

	/**
	 * When user canceled login, this method will be called.
	 */
	 void onCancel();
	 
	 /**
      * When login state changed.
      * 
      * @param state : 1  check third token
      *                2  page start
      *                3  page finish
      *                4  login in back
      *                5  start coupon
      */
	 void onStateChanged(int state);
}