package com.netmanage.interfaces;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface SimChangeObserver {
	
	/**
	 * sim热插拔
	 * @param subject
	 */
	public void simChange(SimChangeSubject subject);

}
