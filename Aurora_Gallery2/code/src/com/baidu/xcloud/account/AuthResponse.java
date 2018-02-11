package com.baidu.xcloud.account;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * XCloud登陆结果类
 * 
 * @author yangyang07
 * @date 2014-2-13 下午1:08:29
 */
public class AuthResponse implements Parcelable {

    public static final int THIRD_BINDTYPE_NOT = 101;
    public static final int THIRD_BINDTYPE_EXPLICIT = 102;
    public static final int THIRD_BINDTYPE_IMPLICIT = 103;

    private String accessToken = null;
    private String userName = null;
    private String expiresIn = null;
    private String refreshToken = null;
    private String userId = null;
    private int bindType;
    private boolean isCouponed = false;

    /**
     * 构造函数
     */
    public AuthResponse() {
    }

    /**
     * 内部序列化构造函数
     * 
     * @param in
     */
    private AuthResponse(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<AuthResponse> CREATOR = new Parcelable.Creator<AuthResponse>() {
        public AuthResponse createFromParcel(Parcel in) {
            return new AuthResponse(in);

        }

        public AuthResponse[] newArray(int size) {
            return new AuthResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        accessToken = in.readString();
        userName = in.readString();
        expiresIn = in.readString();
        refreshToken = in.readString();
        userId = in.readString();
        bindType = in.readInt();
        isCouponed = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(userName);
        dest.writeString(expiresIn);
        dest.writeString(refreshToken);
        dest.writeString(userId);
        dest.writeInt(bindType);
        dest.writeInt(isCouponed ? 1 : 0);
    }

    /**
     * 设置访问Token
     * 
     * @param token
     */
    public void setAccessToken(String token) {
        accessToken = token;
    }

    /**
     * 获取访问Token
     * 
     * @return
     */
    public String getAccessToken() {
        return accessToken;
    }

    public void setUserName(String un) {
        userName = un;
    }

    public void setUserId(String ui) {
        userId = ui;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setExpiresIn(String ei) {
        expiresIn = ei;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setRefreshToken(String rt) {
        refreshToken = rt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setBindType(int type) {
        bindType = type;
    }

    public int getBindType() {
        return bindType;
    }

    public final boolean isCouponed() {
        return isCouponed;
    }

    public final void setCouponed(boolean isCouponed) {
        this.isCouponed = isCouponed;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AuthResponse: ");
        sb.append("bindtype=" + bindType);
        sb.append(", isCouponed=" + isCouponed);
        sb.append(", accessToken=" + accessToken);
        sb.append(", userId=" + userId);
        sb.append(", userName=" + userName);
        sb.append(", expiresIn=" + expiresIn);
        sb.append(", refreshToken=" + refreshToken);

        return sb.toString();
    }
}