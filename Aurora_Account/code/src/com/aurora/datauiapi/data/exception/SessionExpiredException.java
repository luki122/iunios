/**
 * 
 */
package com.aurora.datauiapi.data.exception;

/**
 * 服务器的会话已过期，在个人中心存储的用户密码已失效（比如用户在官网做了修改）
 * 此时用户需要重新登录
 * 
 * @author JimXia
 *
 * @date 2014年12月15日 下午3:01:32
 */
public class SessionExpiredException extends Exception {
    private static final long serialVersionUID = 1L;

    public SessionExpiredException(String detailMessage) {
        super(detailMessage);
    }

    public SessionExpiredException(Throwable throwable) {
        super(throwable);
    }

    public SessionExpiredException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}