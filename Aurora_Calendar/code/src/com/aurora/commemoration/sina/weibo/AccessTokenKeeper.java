/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.commemoration.sina.weibo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * 该类定义了微博授权时所需要的参数。
 * 
 * @author SINA
 * @since 2013-10-07
 */
public class AccessTokenKeeper {
    private static final String PREFERENCES_NAME = "com_weibo_sdk_android";

    private static final String KEY_UID           = "uid";
    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_EXPIRES_IN    = "expires_in";
    private static final String KEY_NICK_NAME    = "nick_name";
    private static final String KEY_USER_NAME    = "user_name";
    
    /**
     * 保存 Token 对象到 SharedPreferences。
     * 
     * @param context 应用程序上下文环境
     * @param token   Token 对象
     */
    public static void writeAccessToken(Context context, SinaAccessToken token) {
        if (null == context || null == token) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        Oauth2AccessToken oauthToken = token.getToken();
        editor.putString(KEY_UID, oauthToken.getUid());
        editor.putString(KEY_ACCESS_TOKEN, oauthToken.getToken());
        editor.putLong(KEY_EXPIRES_IN, oauthToken.getExpiresTime());
        
        editor.putString(KEY_NICK_NAME, token.getNickName());
        editor.putString(KEY_USER_NAME, token.getUserName());
        
        editor.commit();
    }

    /**
     * 从 SharedPreferences 读取 Token 信息。
     * 
     * @param context 应用程序上下文环境
     * 
     * @return 返回 Token 对象
     */
    public static SinaAccessToken readAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        
        SinaAccessToken token = new SinaAccessToken();
        Oauth2AccessToken oauthToken = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        oauthToken.setUid(pref.getString(KEY_UID, ""));
        oauthToken.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
        oauthToken.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
        token.setToken(oauthToken);
        token.setNickName(pref.getString(KEY_NICK_NAME, ""));
        token.setUserName(pref.getString(KEY_USER_NAME, ""));
        
        return token;
    }

    /**
     * 清空 SharedPreferences 中 Token信息。
     * 
     * @param context 应用程序上下文环境
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
