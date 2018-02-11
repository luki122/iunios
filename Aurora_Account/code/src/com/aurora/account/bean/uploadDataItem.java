package com.aurora.account.bean;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;


public class uploadDataItem{

	// 客户端一条数据的记录 
	private String id;
	// 表示是什么操作
	private String op;
	// 服务端一条数据的记录
	private String syncid;
	// 结果标示
	private String result;
	// 结果描述
	private String desc;
	
	private String body;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getSyncid() {
		return syncid;
	}
	public void setSyncid(String syncid) {
		this.syncid = syncid;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

}
