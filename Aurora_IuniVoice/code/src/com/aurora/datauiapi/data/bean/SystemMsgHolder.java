package com.aurora.datauiapi.data.bean;

public class SystemMsgHolder extends BaseResponseObject {

	private MsgNotice notice;
	private SystemMsgData data;
	public MsgNotice getNotice() {
		return notice;
	}
	public void setNotice(MsgNotice notice) {
		this.notice = notice;
	}
	public SystemMsgData getData() {
		return data;
	}
	public void setData(SystemMsgData data) {
		this.data = data;
	}
	
}
