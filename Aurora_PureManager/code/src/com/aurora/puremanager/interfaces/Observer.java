package com.aurora.puremanager.interfaces;

import java.util.List;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface Observer {
	
	/**
	 * 完成初始化
	 * @param subject
	 */
	public void updateOfInit(Subject subject);
	
	/**
	 * 安装一个应用
	 * @param subject
	 * @param packageName
	 */
	public void updateOfInStall(Subject subject,String pkgName);
	
	/**
	 * 覆盖安装一个应用
	 * @param subject
	 * @param appInfo
	 */
	public void updateOfCoverInStall(Subject subject,String pkgName);
	
	/**
	 * 删除一个应用
	 * @param subject
	 * @param packageName
	 */
	public void updateOfUnInstall(Subject subject,String pkgName);
	
	/**
	 * LBE推荐的权限配置更新
	 * @param subject
	 */
	public void updateOfRecomPermsChange(Subject subject);	
	
	/**
	 * 安装在外部sd卡中的应用变得可用
	 * @param subject
	 * @param pkgList
	 */
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList);
		
	/**
	 * 安装在外部sd卡中的应用变得不可用
	 * @param subject
	 * @param pkgList
	 */
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList);
}
