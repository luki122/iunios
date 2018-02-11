package com.baidu.xcloud.account;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class AccountInfo implements Parcelable {

    private Bundle mKeyValueBundle = new Bundle();

    /**
     * 通过Baidu auth服务获取到的AccessToken，
     */
    private String mAccessToken = "";

    /**
     * UID ,对于某些插件，可以用UID+appid组合在一起，成为本地数据库保存的主键
     */
    private String mUid = "";

    /**
     * appid
     */
    private String mAppid = "";

    public AccountInfo(String accessToken) {
        this.mAccessToken = accessToken;
    }

    public AccountInfo(String accessToken, String uid, String appid) {
        this.mAccessToken = accessToken;
        this.mUid = uid;
        this.mAppid = appid;
    }

    public static final Parcelable.Creator<AccountInfo> CREATOR = new Parcelable.Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel in) {
            return new AccountInfo(in);
        }

        @Override
        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };

    public AccountInfo() {
    }

    private AccountInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        mKeyValueBundle.readFromParcel(in);
        mAccessToken = in.readString();
        mUid = in.readString();
        mAppid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mKeyValueBundle.writeToParcel(dest, flags);
        dest.writeString(mAccessToken);
        dest.writeString(mUid);
        dest.writeString(mAppid);
    }

    /**
     * set access token
     * 
     * @param token the access token to set
     */
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    /**
     * get the access token
     * 
     * @return String, the access token
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * @return the uid
     */
    public String getUid() {
        return mUid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(String uid) {
        this.mUid = uid;
    }

    /**
     * @return the mAppid
     */
    public String getAppid() {
        return mAppid;
    }

    /**
     * @param mAppid the mAppid to set
     */
    public void setAppid(String mAppid) {
        this.mAppid = mAppid;
    }

    public void add(String key, String value) {
        mKeyValueBundle.putString(key, value);
    }

    public String get(String key) {
        return mKeyValueBundle.getString(key);
    }

}