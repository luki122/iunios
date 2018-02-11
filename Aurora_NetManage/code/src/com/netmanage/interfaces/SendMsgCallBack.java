package com.netmanage.interfaces;

/**
 * 发送短信的回调
 *
 */
public interface SendMsgCallBack {

	/**
	 * 发送短信后的回调
	 * @param key
	 * @param num
	 * @param msg
	 * @param isSucess
	 */
	public void result(String key,String num,String msg,boolean isSucess);

}
