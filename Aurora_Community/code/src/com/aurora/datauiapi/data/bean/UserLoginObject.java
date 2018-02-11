package com.aurora.datauiapi.data.bean;

public class UserLoginObject extends BaseResponseObject {
    public static final int CODE_ERROR_MISSING_ACC = 200301; // 缺少帐号
    public static final int CODE_ERROR_MISSING_PWD = 200302; // 缺少密码
    public static final int CODE_ERROR_MISSING_IMEI = 200303; // 缺少IMEI
    public static final int CODE_ERROR_ACC_OR_PWD_ERROR = 200304; // 用户名或密码错误
    public static final int CODE_ERROR_PWD_ERROR_3_TIMES = 200305; // 密码输错3次
    public static final int CODE_ERROR_VC_CODE_ERROR = 200306; // 验证码错误
    public static final int CODE_ERROR_EMAIL_ACC_NOT_ACTIVE = 200308; // 邮箱未激活
    
	private String userKey;
	
	//等同于服务器返回的cookie
	private String tgt;
	
	//应用信息结构体
	private UserInfo user = new UserInfo();

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getTgt() {
		return tgt;
	}

	public void setTgt(String tgt) {
		this.tgt = tgt;
	}
	
	
}