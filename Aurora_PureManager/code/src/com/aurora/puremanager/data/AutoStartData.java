package com.aurora.puremanager.data;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;

public class AutoStartData extends BaseData{

	public AutoStartData() {
		super("AutoStartData");
	}
	
	private boolean hasAutoStart = false;//标记相关应用包是否拥有自启动能力
	private boolean isOpen = false;//自启动权限是否打开
	private boolean isOpenByUser = false;//用户是否要求自启动
	private ResolveInfo bootReceiveResolveInfo;//开机广播
	private ServiceInfo[] serviceInfo;//后台服务
	private ActivityInfo[] receiveInfo;//广播
	private List<ResolveInfo> resolveInfoList;
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月15日 上午11:44:13 .
	 * @return
	 */
	public boolean getAutoStartOfUser() {
		return isOpenByUser;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月15日 上午11:53:13 .
	 * @param b
	 */
	public void setAutoStartOfUser(boolean b) {
		isOpenByUser = b;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月14日 下午2:49:04 .
	 * @return
	 */
	public boolean getHasAutoStart() {
		return hasAutoStart;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月14日 下午2:49:31 .
	 * @param b
	 * @return
	 */
	public void setHasAutoStart(boolean b) {
		hasAutoStart = b;
	}
	
	public void setIsOpen(boolean isOpen){
		this.isOpen = isOpen;
	}
	
	public boolean getIsOpen(){
		return this.isOpen;
	}
	
	public void setBootReceiveResolveInfo(ResolveInfo bootReceiveResolveInfo){
		this.bootReceiveResolveInfo = bootReceiveResolveInfo;
	}
	
	public ResolveInfo getBootReceiveResolveInfo(){
		return this.bootReceiveResolveInfo;
	} 
	
	public void setServiceInfo(ServiceInfo[] serviceInfo){
		this.serviceInfo = serviceInfo;
	}
	
	public ServiceInfo[] getServiceInfo(){
		return this.serviceInfo;
	}
	
	public void setReceiveInfo(ActivityInfo[] receiveInfo){
		this.receiveInfo = receiveInfo;
	}
	
	public ActivityInfo[] getReceiveInfo(){
		return this.receiveInfo;
	}
	
	/**
	 * resolveInfoList 中加入resolveInfo，如果遇到重复的receive不会重复加载
	 * @param resolveInfo
	 */
	public void AddResolveInfo(ResolveInfo resolveInfo) {
		if (resolveInfoList == null) {
			resolveInfoList = new ArrayList<ResolveInfo>();
		}
		
		for(int i=0;i<resolveInfoList.size();i++){
			ResolveInfo tmp = resolveInfoList.get(i);
			if(tmp.activityInfo.name.equals(resolveInfo.activityInfo.name)){
				return ;
			}
		}
		
		resolveInfoList.add(resolveInfo);
	}
	
	public List<ResolveInfo> getResolveInfoList(){
		return this.resolveInfoList;
	}
	
	public void ClearResolveInfoList() {
		if (resolveInfoList != null) {
			resolveInfoList.clear();
		}
	}
	
}
