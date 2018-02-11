package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;



public class AppUpgradeObject {

	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//更新界面列表字段
	private List<AppUpgradeInfo> upgradeApps = new ArrayList<AppUpgradeInfo>();

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<AppUpgradeInfo> getUpgradeApps() {
		return upgradeApps;
	}

	public void setUpgradeApps(List<AppUpgradeInfo> upgradeApps) {
		this.upgradeApps = upgradeApps;
	}

}
