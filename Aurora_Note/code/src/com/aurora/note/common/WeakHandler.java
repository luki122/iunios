package com.aurora.note.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 定义对Handler持有的Context弱引用的Handler
 * 防止直接在{@link Activity}直接定义内部{@link Handler}类引起的内存泄漏问题
 * 
 * @author JimXia
 * 2014-6-27 上午11:00:16
 */
public class WeakHandler extends Handler {
	private final WeakReference<MessageHandler> mMessageHandler;

	public WeakHandler(MessageHandler handler) {
		super();
		this.mMessageHandler=new WeakReference<MessageHandler>(handler);
	}

	public WeakHandler(MessageHandler handler,Callback callback) {
		super(callback);
		this.mMessageHandler=new WeakReference<MessageHandler>(handler);
	}

	public WeakHandler(MessageHandler handler,Looper looper, Callback callback) {
		super(looper, callback);
		this.mMessageHandler=new WeakReference<MessageHandler>(handler);
	}

	public WeakHandler(MessageHandler handler,Looper looper) {
		super(looper);
		this.mMessageHandler=new WeakReference<MessageHandler>(handler);
	}

	@Override
	public void handleMessage(Message msg) {
		MessageHandler handler=mMessageHandler.get();
		if(handler!=null){
			handler.handleMessage(msg);
		}
	}	
}