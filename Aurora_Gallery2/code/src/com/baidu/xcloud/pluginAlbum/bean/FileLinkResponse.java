package com.baidu.xcloud.pluginAlbum.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * (createFileLink)share方法得到的类结构
 */
public class FileLinkResponse extends AsyncTaskBaseBean implements Parcelable {

    /** 分享的文件的链接 */
    public List<String> links = null;

    public static final Parcelable.Creator<FileLinkResponse> CREATOR = new Parcelable.Creator<FileLinkResponse>() {
        public FileLinkResponse createFromParcel(Parcel in) {
            return new FileLinkResponse(in);
        }

        public FileLinkResponse[] newArray(int size) {
            return new FileLinkResponse[size];
        }
    };

    public FileLinkResponse() {
        links = new ArrayList<String>();
    }

    public FileLinkResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private FileLinkResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        if (links == null) {
            links = new ArrayList<String>();
        }
        in.readStringList(links);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (links != null)
            dest.writeStringList(links);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        if (links != null) {
            sb.append(" list size:" + links.size());
            for (int i = 0; i < links.size(); i++) {
                sb.append(" link:" + links.get(i));
            }
        }
        return sb.toString();
    }
}