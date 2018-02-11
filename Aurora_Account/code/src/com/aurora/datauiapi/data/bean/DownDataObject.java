package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

import com.aurora.account.bean.syncDataItemObject;

public class DownDataObject extends BaseResponseObject {

	private ArrayList<syncDataItemObject> records = new ArrayList<syncDataItemObject>();

	public ArrayList<syncDataItemObject> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<syncDataItemObject> records) {
		this.records = records;
	}

}
