package com.aurora.datauiapi.data.bean;

public class ValidateVCObject extends BaseResponseObject {

	/** 缺少IMEI号 */
	public static final int CODE_ERROR_IMEI_ISEMPTY = 200303;
	
	/** 验证码错误或过期 */
	public static final int CODE_ERROR_PHONE_VERIFYCODE_WRONG = 200408;
	
}
