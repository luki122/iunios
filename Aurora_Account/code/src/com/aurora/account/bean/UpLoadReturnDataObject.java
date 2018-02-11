package com.aurora.account.bean;

import java.util.ArrayList;
import java.util.List;

import com.aurora.datauiapi.data.bean.BaseResponseObject;

public class UpLoadReturnDataObject extends BaseResponseObject{

	// 附件数组
	private ArrayList<uploadDataItem> records = new ArrayList<uploadDataItem>();

	public ArrayList<uploadDataItem> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<uploadDataItem> records) {
		this.records = records;
	}

}
