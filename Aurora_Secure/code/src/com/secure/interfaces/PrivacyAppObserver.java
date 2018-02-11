package com.secure.interfaces;

import java.util.List;

import com.privacymanage.data.AidlAccountData;
import com.secure.data.PrivacyAppData;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface PrivacyAppObserver {
	
	/**
	 * 完成初始化
	 * @param subject
	 */
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject);
	
	/**
	 * 增加隐私应用
	 * @param subject
	 * @param packageName
	 */
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,List<PrivacyAppData>PrivacyAppList);
	
	/**
	 * 删除隐私应用
	 * @param subject
	 * @param appInfo
	 */
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,List<PrivacyAppData>PrivacyAppList);
	
	/**
	 * 隐私身份切换
	 * @param subject
	 * @param accountData 当前账户
	 */
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,AidlAccountData accountData);
	
	/**
	 * 删除隐私账户
	 * @param subject
	 * @param accountData  被删除的隐私账户
	 */
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,AidlAccountData accountData);
}
