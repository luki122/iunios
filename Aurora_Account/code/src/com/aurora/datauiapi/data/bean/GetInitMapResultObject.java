package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class GetInitMapResultObject extends BaseResponseObject {

	private ArrayList<InitMapInfo> records = new ArrayList<InitMapInfo>();

	public ArrayList<InitMapInfo> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<InitMapInfo> records) {
		this.records = records;
	}

	
	
}
