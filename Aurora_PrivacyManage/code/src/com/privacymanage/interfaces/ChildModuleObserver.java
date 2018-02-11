package com.privacymanage.interfaces;

import com.privacymanage.data.ModuleInfoData;

/**
 * 观察者接口，主要用于子模块信息更新
 *
 */
public interface ChildModuleObserver {

	/**
	 * 子模块信息更新
	 * @param moduleInfoData
	 */
	public void update(ChildModuleSubject subject, ModuleInfoData moduleInfoData);
	
}
