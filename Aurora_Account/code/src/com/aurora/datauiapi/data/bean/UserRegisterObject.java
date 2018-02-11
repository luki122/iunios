package com.aurora.datauiapi.data.bean;

public class UserRegisterObject extends BaseResponseObject {
	
	public static int CODE_ERROR_VERCODE_ERROR = 200209;
	public static int CODE_ERROR_PHONENUM_ALREADY_REGISTER = 200210;
	public static int CODE_ERROR_EMAIL_ALREADY_REGISTER = 200211;
	public static int CODE_ERROR_SEND_MSG_FREQUENT = 200110;
	
	private String userKey;
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
}