package com.privacymanage.interfaces;

import com.privacymanage.data.AccountData;

/**
 * 观察者接口，主要用于账户改变
 *
 */
public interface AccountObserver {
	/**
	 * 账户切换
	 * @param accountData  当前身份信息
	 */
	public void switchAccount(AccountData accountData);
	
	/**
	 * 删除隐私账户
	 * @param accountData  被删除的账户信息
	 * @param delete true：删除隐私空间数据，false：还原隐私空间数据
	 */
	public void deleteAccount(AccountData accountData,boolean delete);
	
}
