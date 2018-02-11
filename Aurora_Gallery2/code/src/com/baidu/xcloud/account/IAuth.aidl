package com.baidu.xcloud.account;
import com.baidu.xcloud.account.IAuthLoginListener;
import com.baidu.xcloud.account.IAuthExpireListener;
import com.baidu.xcloud.account.IBindDetailListener;
import com.baidu.xcloud.account.AuthInfo;

interface IAuth {
   
    /**
     * Start xcloud account authentication, this is an async method.
     * 
     * @param authInfo
     *            account detail information      
     * @param listener
     *            authentication result callback
     */
    void startAuth(in AuthInfo authInfo, in IAuthLoginListener listener);  

    /**
     * Expire xcloud accout access token, this is an async method.
     * 
     * @param token
     *            the access token
     * @param listener
     *            expire token result callback
     */
     void expireToken(in String accessToken, in IAuthExpireListener listener);
     
    /**
     * Query bind details of third account, this is an async method.
     * 
     * @param apiKey
     *            api key of app
     * @param thirdToken
     *            the third access token
     * @param listener
     *            
     */
     void queryThirdBindDetails(in String apiKey, in String thirdToken, in IBindDetailListener listener);
}
