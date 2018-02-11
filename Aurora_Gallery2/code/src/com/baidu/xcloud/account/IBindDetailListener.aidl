package com.baidu.xcloud.account;
import com.baidu.xcloud.account.AuthResponse;

interface IBindDetailListener {

    void onBindResult(in AuthResponse response);
    
    void onException(in String errorMsg);
}