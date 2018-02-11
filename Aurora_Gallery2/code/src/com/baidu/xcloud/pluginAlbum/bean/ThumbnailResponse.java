package com.baidu.xcloud.pluginAlbum.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * thumbnail方法得到的类结构
 */
public class ThumbnailResponse extends AsyncTaskBaseBean implements Parcelable {

    /** 保存缩略图的bitmap */
    public Bitmap bitmap = null;

    public static final Parcelable.Creator<ThumbnailResponse> CREATOR = new Parcelable.Creator<ThumbnailResponse>() {
        public ThumbnailResponse createFromParcel(Parcel in) {
            return new ThumbnailResponse(in);
        }

        public ThumbnailResponse[] newArray(int size) {
            return new ThumbnailResponse[size];
        }
    };

    public ThumbnailResponse() {

    }

    public ThumbnailResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private ThumbnailResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        try {
            bitmap = Bitmap.CREATOR.createFromParcel(in);
        } catch (Exception e) {

        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (bitmap != null)
            bitmap.writeToParcel(dest, flags);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        sb.append(" bitmap:" + bitmap);
        return sb.toString();
    }
}
