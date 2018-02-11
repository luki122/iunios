package com.aurora.account.bean;

import java.util.ArrayList;
import java.util.List;

public class syncDataObject {

	// 附件数组
	private ArrayList<syncDataItemObject> sycndata = new ArrayList<syncDataItemObject>();

	public ArrayList<syncDataItemObject> getSycndata() {
		return sycndata;
	}

	public void setSycndata(ArrayList<syncDataItemObject> sycndata) {
		this.sycndata = sycndata;
	}



}
