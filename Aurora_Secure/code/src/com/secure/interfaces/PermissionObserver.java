package com.secure.interfaces;

import com.secure.data.AppInfo;
import com.secure.data.PermissionInfo;

/**
 * 观察者接口，定义一个更新的接口给那些在目标发生改变的时候被通知的对象
 *
 */
public interface PermissionObserver {
	/**
	 * 指定应用的权限状态更新了
	 * @param appInfo 如果为null，表示多个应用的权限状态改变
	 * @param permissionInfo 如果为null，表示应用的多个权限状态改变
	 */
	public void updateOfPermsStateChange(AppInfo appInfo,PermissionInfo permissionInfo);
	
	
}
