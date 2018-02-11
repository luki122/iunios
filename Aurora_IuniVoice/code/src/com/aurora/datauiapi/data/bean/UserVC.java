/*
 * Copyright (C) 2010-2012 TENCENT Inc.All Rights Reserved.
 *
 * FileName: BannerItem
 *
 * Description:  海报图中每项数据
 *
 * History:
 *  1.0   kodywu (kodytx@gmail.com) 2010-11-30   Create
 */
package com.aurora.datauiapi.data.bean;



public class UserVC extends BaseResponseObject{
    /**手机号已注册*/
    public static final int CODE_ERROR_PHONE_NUM_ALREADY_REGISTERED = 200103;
    
    /**短信验证码发送失败*/
    public static final int CODE_ERROR_FAILED_TO_SEND = 200105;
    
    /**手机号未注册*/
    public static final int CODE_ERROR_PHONE_NUM_NOT_REGISTERED = 200104;
    
    /**发送短信过于频繁*/
    public static int CODE_ERROR_SEND_MSG_FREQUENT = 200110;
    
    /**邮箱地址未注册*/
    public static final int CODE_ERROR_EMAIL_NOT_REGISTERED = 200409;

    /**发送验证邮件失败*/
    public static final int CODE_ERROR_EMAIL_SEND_ERROR = 200411;
    
    /**缺少校验码*/
    public static final int CODE_ERROR_CHECKCODE_NOT_INPUT = 200412;
    
}
