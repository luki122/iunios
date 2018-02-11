/**
 * 
 */
package com.aurora.datauiapi.data.exception;

/**
 * 服务器异常
 * 
 * @author JimXia
 * @date 2014年11月18日 上午10:25:04
 */
public class ServerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private int mServerResponseCode;

    public ServerException(int serverResponseCode) {
        mServerResponseCode = serverResponseCode;
    }

    public ServerException(int serverResponseCode, String detailMessage) {
        super(detailMessage);
        mServerResponseCode = serverResponseCode;
    }

    public ServerException(Throwable throwable) {
        super(throwable);
    }

    public ServerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}