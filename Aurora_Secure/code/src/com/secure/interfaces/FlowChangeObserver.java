package com.secure.interfaces;

import java.util.HashMap;
import com.netmanage.data.FlowData;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface FlowChangeObserver {
	
	/**
	 * 流量使用值改变
	 * @param subject
	 */
	public void updateOfFlowChange(HashMap<String ,FlowData> flowMap);
}
