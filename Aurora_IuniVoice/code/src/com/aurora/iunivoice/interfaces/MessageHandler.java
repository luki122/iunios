package com.aurora.iunivoice.interfaces;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

/**
 * 用于处理Message消息的接口
 * <p>主要配合{@link WeakHandler}解决在{@link Activity}中定义
 * {@link Handler}内部类导致内存泄漏的问题
 * 
 * @author JimXia
 * 2014-6-27 上午11:00:33
 */
public interface MessageHandler {
	/**
	 * 处理Handler的消息
	 * @param msg
	 */
	public void handleMessage(Message msg);
}