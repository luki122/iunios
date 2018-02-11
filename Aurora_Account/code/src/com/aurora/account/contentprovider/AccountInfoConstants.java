/**
 * 
 */
package com.aurora.account.contentprovider;

import android.net.Uri;

/**
 * @author JimXia
 *
 * @date 2014年11月20日 上午11:22:57
 */
public abstract interface AccountInfoConstants {
    
    /**帐号中心ContentProvider的authority*/
    public static final String AUTHORITY = "com.aurora.account.accountprovider";
    
    /**查询帐号信息*/
    public static final String ACCOUNT_INFO = "account_info";
    
    /**查询帐号cookie*/
    public static final String ACCOUNT_COOKIE = "account_cookie";
    
    /**accountInfo type*/
    public static final String ACCOUNT_INFO_TYPE = "vnd.android.cursor.dir/com.aurora.account.accountprovider-accountInfo";
    
    /**查询帐户信息的Uri*/
    public static final Uri QUERY_URI = Uri.parse("content://" + AUTHORITY + "/" + ACCOUNT_INFO);
    
    /**帐号中心返回的昵称*/
    public static final String ACCOUNT_INFO_NICK = "nick";
    /**帐号中心返回的图像URL*/
//    public static final String ACCOUNT_INFO_ICON_URL = "iconUrl";
    /**帐号中心返回的图像本地Path*/
    public static final String ACCOUNT_INFO_ICON_PATH = "iconPath";
    /**帐号中心返回的最后同步完成时间*/
    public static final String ACCOUNT_INFO_LAST_SYNC_FINISHED_TIME = "lastSyncFinishedTime";
    /**帐号中心返回的是否登录*/
    public static final String ACCOUNT_INFO_HAS_LOGIN = "hasLogin";
    /**token*/
    public static final String ACCOUNT_INFO_TOKEN =  "token";
    /**user_id*/
    public static final String ACCOUNT_INFO_USERID =  "user_id";
    /**cookie*/
    public static final String ACCOUNT_INFO_COOKIE =  "cookie";
    public static final String[] ACCOUNT_INFO_COLUMNS = {
        ACCOUNT_INFO_NICK,
//        ACCOUNT_INFO_ICON_URL,
        ACCOUNT_INFO_ICON_PATH,
        ACCOUNT_INFO_LAST_SYNC_FINISHED_TIME,
        ACCOUNT_INFO_HAS_LOGIN,
        ACCOUNT_INFO_TOKEN,
        ACCOUNT_INFO_COOKIE,
        ACCOUNT_INFO_USERID
        
    };
    public static final String[] ACCOUNT_COOKIE_COLUMNS = {
        ACCOUNT_INFO_COOKIE
    };
}
