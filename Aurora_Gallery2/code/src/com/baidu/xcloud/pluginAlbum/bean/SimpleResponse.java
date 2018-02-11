package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * diff方法得到的类结构
 */
public class SimpleResponse extends AsyncTaskBaseBean implements Parcelable {

    public static final Parcelable.Creator<SimpleResponse> CREATOR = new Parcelable.Creator<SimpleResponse>() {
        public SimpleResponse createFromParcel(Parcel in) {
            return new SimpleResponse(in);
        }

        public SimpleResponse[] newArray(int size) {
            return new SimpleResponse[size];
        }
    };

    public SimpleResponse() {

    }

    private SimpleResponse(Parcel in) {
        readFromParcel(in);
    }

    public SimpleResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public String toString() {
        return super.toString();
    }
}