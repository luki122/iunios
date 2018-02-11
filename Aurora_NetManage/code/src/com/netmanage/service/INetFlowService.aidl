package com.netmanage.service;

import  com.netmanage.service.INetFlowServiceCallback;

interface INetFlowService {
   	void unregisterCallback(INetFlowServiceCallback mCallback);
    void registerCallback(INetFlowServiceCallback mCallback);
    void getFlowData();
    /**
	 * 统计流量开始时间
	 * @return
	 */
    long getFlowBeginTime();
    
    /**
	 * 判断是否设置流量套餐
	 * @return true 已经设置套餐
	 *         false 没有设置套餐
	 */
	boolean isSetedFlowPackage();
 }
