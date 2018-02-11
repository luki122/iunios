/*
 * PCSParcelableObject.java
 * 
 * Version:
 *
 * Date: 2013-4-28
 *
 * Changes:
 * [Date@Author]:Content
 * 
 * Copyright 2012-2013 Baidu. All Rights Reserved
 */

package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Bundle;
import android.os.Parcel;

/**
 * 用于扩展返回一些数据给调用者使用
 * 
 * @author zhaopeng05
 */
public class BundleObject {

    /**
     * 返回数据Bundle
     */
    private Bundle mKeyValueBundle = new Bundle();

    public BundleObject() {
        mKeyValueBundle = new Bundle();
    }

    public void add(String key, String value) {
        mKeyValueBundle.putString(key, value);
    }

    public String get(String key) {
        return mKeyValueBundle.getString(key);
    }

    /**
     * 从parcel里读取属性
     * 
     * @param in
     */
    public void readFromParcel(Parcel in) {
        mKeyValueBundle.readFromParcel(in);
    }

    /**
     * 将属性写入parcel
     * 
     * @param dest
     * @param flags
     */
    public void writeToParcel(Parcel dest, int flags) {
        mKeyValueBundle.writeToParcel(dest, flags);
    }

    public String toString() {
        return mKeyValueBundle.toString();
    }
}
