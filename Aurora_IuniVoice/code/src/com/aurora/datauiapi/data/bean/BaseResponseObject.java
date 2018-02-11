package com.aurora.datauiapi.data.bean;


/** 
* @ClassName: BaseResponseObject
* @Description: 服务器返回response信息封装对象的基类
* @author jason
* @date 2015年3月18日 下午3:41:03
* 
*/ 
public class BaseResponseObject {
	
	//返回码，0 成功 1失败 -1未登录
	private int returnCode;
	//错误码
	private int errorCode;
	//错误信息
	private String msg;
	//数据返回时间（毫秒）
	private String respTime;
	
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getRespTime() {
		return respTime;
	}
	public void setRespTime(String respTime) {
		this.respTime = respTime;
	}
	
	
	// =====================  账户系统  ===================== //
	
	
	public static final int CODE_SUCCESS = 1;
	
	/**用户会话过期*/
	public static final int CODE_ERROR_SESSION_EXPIRED = 200307;
	
	// 修改密码相关的错误码
	/**旧密码错误*/
	public static final int CODE_ERROR_OLD_PWD_INCORRECT = 200603;
	
	// 修改绑定邮箱相关的错误
	/**邮箱地址收入错误，拼写不符合规则*/
	public static final int CODE_ERROR_INVALID_EMAIL = 200802;
	/**邮箱地址已被绑定*/
    public static final int CODE_ERROR_EMAIL_ALREADY_BIND = 200803;
    
    // 找回密码相关
    public static final int CODE_ERROR_PHONE_VERIFYCODE_WRONG = 200408;
    /**校验码错误 */
    public static int CODE_ERROR_CHECK_INPUT_ERROR = 200108;
    
    
    //================ 昵称修改错误码 ========================
    /**昵称包含敏感词*/
    public static final int CODE_ERROR_NICK_INVALID = 200503;
    /**昵称已存在*/
    public static final int CODE_ERROR_NICK_ALREADY_EXIST = 200504;
    /**昵称修改时间间隔未过限定期*/
    public static final int CODE_ERROR_NICK_UPDATE_FREQUENTLY = 200505;
    /**帐号信息错误*/
    public static final int CODE_ERROR_NICK_UPDATE_ACCOUNT_INFO_ERROR = 200506;
  //================ 昵称修改错误码结束 ========================
    
  //================ 验证当前绑定手机号错误码 ========================
    /**原手机号错误*/
    public static final int CODE_ERROR_CUR_PHONE_INVALID = 200704;
    /**密码错误*/
    public static final int CODE_ERROR_PWD_ERROR = 200708;
    /**用户未绑定手机号*/
    public static final int CODE_ERROR_NO_PHONE_BIND = 200709;
    /**发送短信过于频繁*/
    public static final int CODE_ERROR_SEND_SMS_TOO_MANY = 200110;
  //================ 验证当前绑定手机号错误码结束 ========================
    
    
  //================ 验证当前绑定手机号验证码错误码 ========================
    /**验证码错误或过期*/
    public static final int CODE_ERROR_VC_ERROR_OR_EXPIRED = 200703;
  //================ 验证当前绑定手机号验证码错误码结束 ========================
    
    
  //================ 验证当前绑定邮箱错误码 ========================
    /**用户未绑定邮箱*/
    public static final int CODE_ERROR_NO_EMAIL_BIND = 200804;
    /**原邮箱地址错误*/
    public static final int CODE_ERROR_CUR_EMAIL_INVALID = 200805;
    /**发送邮件过于频繁*/
    public static final int CODE_ERROR_SEND_MAIL_TOO_MANY = 201106;
  //================ 验证当前绑定邮箱错误码结束 ========================
	
	// 返回码 1：成功 其它失败
	private int code;
	// 成功和失败的描述
	private String desc;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}