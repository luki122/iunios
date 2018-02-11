package com.netmanage.interfaces;

public interface IService {
	/**
	 * 添加当前正在显示权限提示界面的app
	 * @param packname
	 */
	public void addDuringPermissionHintApp(String packname);
	
	/**
	 * 当某个app的提示界面消失时，删除对应的记录
	 * @param packname
	 */
	public void deleteDuringPermissionHintApp(String packname);
	
}
