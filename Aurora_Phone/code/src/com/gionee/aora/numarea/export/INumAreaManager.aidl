package com.gionee.aora.numarea.export;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.NumAreaInfo;
/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file INumAreaManager.aidl
 * 摘要:来电归属地管理类,主要提供两个接口,由归属地的Service产生
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
interface INumAreaManager
{
/**
	 * 根据号码获取归属地信息
	 * @param aPhoneNum
	 * 输入的号码
	 * @return
	 * 归属地信息
	 */
	NumAreaInfo getNumAreaInfo(String aPhoneNum);
	/**
	 * 根据输入字符串查找号码归属地
	 * @param aArea
	 *@param aTag
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
	NumAreaInfo[] getAreaNumInfo(String aArea,String aTag);
		/**
	 * 获取常用号码列表
	 * @param aArea
	 * 地区
	 * @return
	 * 存放归属地数据的List
	 */
	NumAreaInfo[] getComAreaNumInfo();
	/**
	 * 更新归属地数据库
	 * @param aObserver
	 * 侦听更新结果的观察者
	 */
	void updataDB(INumAreaObserver aObserver);
	
	/**
	 * 取消更新
	 */
	void cancelUpdata();
	
    /**
	 * 注册一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中没有此对象并添加成功.返回true,其余情况为false
	 */
	boolean registObserver(INumAreaObserver aObserver);
	
	/**
	 * 注销一个观察者
	 * @param aObserver
	 * 观察者对象
	 * @return
	 * 假如观察者列表中有此对象并删除成功,返回true,其余情况为false
	 */
	boolean unregistObserver(INumAreaObserver aObserver);
}