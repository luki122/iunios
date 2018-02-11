package com.aurora.note.sina.weibo;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * 新浪微博授权对象的封装
 * @author JimXia
 * 2014-6-19 上午10:02:50
 */
public class SinaAccessToken {
    private Oauth2AccessToken token;
    private String nickName;
    private String userName;
    
    public Oauth2AccessToken getToken() {
        return token;
    }
    
    public void setToken(Oauth2AccessToken token) {
        this.token = token;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
