package com.baidu.xcloud.account;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * XCloud登陆参数类
 * 
 * @author yangyang07
 * @date 2014-2-13 下午1:09:07
 */
public class AuthInfo implements Parcelable {

    /**
     * 登陆方式定义
     */
    private static final int AUTHTYPE_BASE = 0;
    /**
     * 普通登陆
     */
    public static final int AUTHTYPE_OTHORIZED = AUTHTYPE_BASE + 1;

    /**
     * 明绑
     */
    public static final int AUTHTYPE_EXPLICITBIND = AUTHTYPE_BASE + 2;

    /**
     * 暗绑
     */
    public static final int AUTHTYPE_IMPLICITBIND = AUTHTYPE_BASE + 3;

    /**
     * 新明绑
     */
    public static final int AUTHTYPE_NEWEXPLICITBIND = AUTHTYPE_BASE + 4;

    /**
     * 正常化
     */
    public static final int AUTHTYPE_NORMALIZE = AUTHTYPE_BASE + 5;

    private int authType;

    private String apiKey;
    private String appId;
    private String apiSecretKey;
    private List<String> permissionsList = new ArrayList<String>();

    private String accessToken;
    private String thirdAccessToken;
    private String devId;
    private String manufecture;

    /**
     * 普通登陆构造函数，使用百度账号
     * 
     * @param apiKey 应用Api Key
     * @param appId 应用Id
     * @param permissionsList 应用使用的权限字符串，默认为个人信息和网盘
     */
    public AuthInfo(String apiKey, String appId, List<String> permissions) {
        authType = AUTHTYPE_OTHORIZED;
        this.apiKey = apiKey;
        this.appId = appId;
        if (permissions != null && permissions.size() > 0) {
            permissionsList.addAll(permissions);
        }
    }

    /**
     * 第三方账号明绑登陆构造函数
     * 
     * @param apiKey 应用Api Key
     * @param appId 应用Id
     * @param thirdAccessToken 第三方访问Token
     * @param permissionsList 应用使用的权限字符串，默认为个人信息和网盘
     */
    public AuthInfo(String apiKey, String appId, String thirdAccessToken, List<String> permissions) {
        this.authType = AUTHTYPE_EXPLICITBIND;
        this.apiKey = apiKey;
        this.appId = appId;
        this.thirdAccessToken = thirdAccessToken;
        if (permissions != null && permissions.size() > 0) {
            permissionsList.addAll(permissions);
        }
    }

    /**
     * 新明绑或第三方账号暗绑登陆构造函数
     * 
     * @param apiKey 应用Api Key
     * @param appId 应用Id
     * @param apiSecretKey 应用Api Secret Key
     * @param thirdAccessToken 第三方访问Token
     * @param permissionsList 应用使用的权限字符串，默认为个人信息和网盘
     * @param authType 新明绑或暗绑的登录方式，必需
     */
    public AuthInfo(String apiKey, String appId, String apiSecretKey, String thirdAccessToken,
            List<String> permissions, int authType) {
        this.authType = authType;
        this.apiKey = apiKey;
        this.appId = appId;
        this.apiSecretKey = apiSecretKey;
        this.thirdAccessToken = thirdAccessToken;
        if (permissions != null && permissions.size() > 0) {
            permissionsList.addAll(permissions);
        }
    }

    /**
     * 第三方账号正常化登陆构造函数
     * 
     * @param apiKey 应用Api Key
     * @param appId 应用Id
     * @param apiSecretKey 应用Api Secret Key
     * @param thirdAccessToken 第三方访问Token
     * @param accessToken 第三方账号绑定的百度账号访问Token
     * @param manufecture 厂商标识,用于成功后页面显示
     * @param permissionsList 应用使用的权限字符串，默认为个人信息和网盘
     */
    public AuthInfo(String apiKey, String appId, String apiSecretKey, String thirdAccessToken, String accessToken,
            String manufecture) {
        authType = AUTHTYPE_NORMALIZE;
        this.apiKey = apiKey;
        this.appId = appId;
        this.apiSecretKey = apiSecretKey;
        this.thirdAccessToken = thirdAccessToken;
        this.accessToken = accessToken;
        this.manufecture = manufecture;
    }

    /**
     * 内部序列化构造函数
     * 
     * @param in
     */
    private AuthInfo(Parcel in) {
        readFromParcel(in);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<String> getPermissionsList() {
        return permissionsList;
    }

    public void setPermissionsList(List<String> permissionsList) {
        this.permissionsList = permissionsList;
    }

    public String getThirdAccessToken() {
        return thirdAccessToken;
    }

    public void setThirdAccessToken(String thirdAccessToken) {
        this.thirdAccessToken = thirdAccessToken;
    }

    public int getAuthType() {
        return authType;
    }

    public void setAuthType(int authType) {
        this.authType = authType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiSecretKey() {
        return apiSecretKey;
    }

    public void setApiSecretKey(String apiSecretKey) {
        this.apiSecretKey = apiSecretKey;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public final String getManufecture() {
        return manufecture;
    }

    public final void setManufecture(String manufecture) {
        this.manufecture = manufecture;
    }

    public static final Parcelable.Creator<AuthInfo> CREATOR = new Parcelable.Creator<AuthInfo>() {
        public AuthInfo createFromParcel(Parcel in) {
            return new AuthInfo(in);
        }

        public AuthInfo[] newArray(int size) {
            return new AuthInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        authType = in.readInt();
        apiKey = in.readString();
        appId = in.readString();
        apiSecretKey = in.readString();
        in.readStringList(permissionsList);
        thirdAccessToken = in.readString();
        devId = in.readString();
        manufecture = in.readString();
        accessToken = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(authType);
        dest.writeString(apiKey);
        dest.writeString(appId);
        dest.writeString(apiSecretKey);
        dest.writeStringList(permissionsList);
        dest.writeString(thirdAccessToken);
        dest.writeString(devId);
        dest.writeString(manufecture);
        dest.writeString(accessToken);
    }
}