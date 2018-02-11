package com.privacymanage.data;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 记录HTTP请求时的数据
 * @author bin.huang
 */
public class HttpData {
	public static enum STATUS{
		SUCESS, //请求成功
		ERROR_OF_NET,//网络异常
		ERROR_OF_TIMEOUT,//连接超时
		ERROR_OF_504//
	}
	
	private AtomicBoolean duringRequest;//记录是不是正在请求
	private STATUS requestStatus;//记录请求结束后的结果
	
	public HttpData(){
		duringRequest = new AtomicBoolean(false);
		requestStatus = STATUS.SUCESS;
	}
	
	public void setRequestStatus(STATUS status){
		requestStatus = status;
	}
	
	public STATUS getRequestStatus(){
		return requestStatus;
	}
	
	/**
	 * 请求开始
	 */
	public void start(){
		duringRequest.set(true);
	}
	
	/**
	 * 请求结束
	 */
	public void end(){
		synchronized (duringRequest) {
			duringRequest.set(false);
			duringRequest.notifyAll();
		}
	}
	
	/**
	 * 调用这个函数的目的：
	 * 1.如果HTTP正在请求，这个函数会阻塞,直到请求结束
	 * 2.如果HTTP没有请求，则不阻塞
	 */
	public void waitIfDuringRequest() {
		if (duringRequest.get()) {
			synchronized (duringRequest) {
				try {
					duringRequest.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
//			Log.i(tag,"UI not wait http");
		}
	}
}
