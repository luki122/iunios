package com.netmanage.interfaces;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface ConfigObserver {
	/**
	 * 每月套餐流量数据更新了
	 * @param subject
	 * @param appInfo
	 */
	public void updateOfMonthlyFlowChange();
	
	/**
	 * 超额预警改变
	 */
	public void updateOfExcessEarlyWarning();
	
	
}
