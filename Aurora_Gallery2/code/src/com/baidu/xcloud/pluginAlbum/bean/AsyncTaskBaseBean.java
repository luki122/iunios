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

import android.os.Parcel;

/**
 * 
 * @author zhaopeng05
 */
public class AsyncTaskBaseBean extends BundleObject {

    /**
     * 异步任务开始，需传递taskId给调用者
     */
    public final static int STATUS_TYPE_BEGIN = 1;
    /**
     * 异步任务结束
     */
    public final static int STATUS_TYPE_END = 2;

    /**
     * 当前任务状态
     */
    private int mStatusType = STATUS_TYPE_BEGIN;

    /**
     * 异步任务taskId
     */
    private String mTaskId;

    /** 0 代表 成功 */
    private int mErrorCode = ErrorCode.Error_DefaultError;

    /** 如果操作失败，则记录一些相关信息 */
    private String mErrorMessage;

    public AsyncTaskBaseBean() {

    }

    /**
     * @param errorCode
     * @param errorMsg
     */
    public AsyncTaskBaseBean(int errorCode, String errorMsg) {
        mErrorCode = errorCode;
        mErrorMessage = errorMsg;
    }

    /**
     * 从parcel里读取属性值
     * 
     * @param in
     */
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mStatusType = in.readInt();
        mTaskId = in.readString();
        mErrorCode = in.readInt();
        mErrorMessage = in.readString();
    }

    /**
     * 将属性值写入parcel
     * 
     * @param dest
     * @param flags
     */
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mStatusType);
        dest.writeString(mTaskId);
        dest.writeInt(mErrorCode);
        dest.writeString(mErrorMessage);
    }

    /**
     * @return 当前任务状态
     */
    public int getStatusType() {
        return mStatusType;
    }

    /**
     * 设置当前任务状态
     * 
     * @param statusType
     * 
     */
    public void setStatusType(int statusType) {
        this.mStatusType = statusType;
    }

    /**
     * @return 任务ID
     */
    public String getTaskId() {
        return mTaskId;
    }

    /**
     * 调用任务ID
     * 
     * @param taskId
     */
    public void setTaskId(String taskId) {
        this.mTaskId = taskId;
    }

    /**
     * @return 错误码
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * 设置错误码
     * 
     * @param errorCode
     */
    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }

    /**
     * @return 错误信息
     */
    public String getMessage() {
        return mErrorMessage;
    }

    /**
     * 设置错误信息
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.mErrorMessage = message;
    }

    /**
     * 将对象的属性拼接为字符串
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append(super.toString());
        sb.append("mStatusType:" + mStatusType);
        sb.append(" mTaskId:" + mTaskId);
        sb.append(" mErrorCode:" + mErrorCode);
        sb.append(" mErrorMessage:" + mErrorMessage);
        return sb.toString();
    }
}
