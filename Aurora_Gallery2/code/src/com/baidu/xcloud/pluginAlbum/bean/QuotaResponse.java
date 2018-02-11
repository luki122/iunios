package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * quota方法得到的类结构， 包含有空间信息
 */
public class QuotaResponse extends AsyncTaskBaseBean implements Parcelable {

    /** 当前用户的所有空间大小 */
    private long mTotal = 0L;

    /** 当前用户已用的空间大小 */
    private long mUsed = 0L;

    public static final Parcelable.Creator<QuotaResponse> CREATOR = new Parcelable.Creator<QuotaResponse>() {
        public QuotaResponse createFromParcel(Parcel in) {
            return new QuotaResponse(in);
        }

        public QuotaResponse[] newArray(int size) {
            return new QuotaResponse[size];
        }
    };

    public QuotaResponse() {

    }

    public QuotaResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private QuotaResponse(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mTotal = in.readLong();
        mUsed = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mTotal);
        dest.writeLong(mUsed);
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return mTotal;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(long total) {
        this.mTotal = total;
    }

    /**
     * @return the used
     */
    public long getUsed() {
        return mUsed;
    }

    /**
     * @param used the used to set
     */
    public void setUsed(long used) {
        this.mUsed = used;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        sb.append("mTotal:" + mTotal);
        sb.append(" mUsed:" + mUsed);
        return sb.toString();
    }
}