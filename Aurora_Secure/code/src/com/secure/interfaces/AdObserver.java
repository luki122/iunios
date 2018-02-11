package com.secure.interfaces;

import java.util.List;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface AdObserver {
	
	/**
	 * 完成初始化
	 * @param subject
	 */
	public void updateOfInit(AdSubject subject);
	
	/**
	 * 安装一个应用
	 * @param subject
	 * @param packageName
	 */
	public void updateOfInStall(AdSubject subject,String pkgName);
	
	/**
	 * 覆盖安装一个应用
	 * @param subject
	 * @param appInfo
	 */
	public void updateOfCoverInStall(AdSubject subject,String pkgName);
	
	/**
	 * 删除一个应用
	 * @param subject
	 * @param packageName
	 */
	public void updateOfUnInstall(AdSubject subject,String pkgName);
	
	/**
	 * 安装在外部sd卡中的应用变得可用
	 * @param subject
	 * @param pkgList
	 */
	public void updateOfExternalAppAvailable(AdSubject subject,List<String> pkgList);
		
	/**
	 * 安装在外部sd卡中的应用变得不可用
	 * @param subject
	 * @param pkgList
	 */
	public void updateOfExternalAppUnAvailable(AdSubject subject,List<String> pkgList);
	
	/**
	 * 应用广告拦截的开关状态改变
	 * @param subject
	 * @param pkgName
	 * @param swtich
	 */
	public void updateOfSwitchChange(AdSubject subject,String pkgName,boolean swtich);
	
	/**
	 * 广告库更新
	 * @param subject
	 */
	public void updateOfAdLibUpdate(AdSubject subject);
	
	/**
	 * 用户手动更新（即重新扫描所有应用）
	 * @param subject
	 */
	public void updateOfManualUpdate(AdSubject subject);
}
