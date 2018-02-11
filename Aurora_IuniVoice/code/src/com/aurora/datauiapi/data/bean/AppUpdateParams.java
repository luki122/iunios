package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;


public class AppUpdateParams {


	private List<AppUpdateInfo> instApps = new ArrayList<AppUpdateInfo>();

	public List<AppUpdateInfo> getInstApps() {
		return instApps;
	}

	public void setInstApps(List<AppUpdateInfo> instApps) {
		this.instApps = instApps;
	}



}
