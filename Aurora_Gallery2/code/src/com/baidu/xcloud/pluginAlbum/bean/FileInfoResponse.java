package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

/** 该类保存了由服务器响应返回后的文件相关信息 */
public class FileInfoResponse extends AsyncTaskBaseBean implements Parcelable {

    /** 记录文件信息的类 */
    public CommonFileInfo commonFileInfo = new CommonFileInfo();

    public static final Parcelable.Creator<FileInfoResponse> CREATOR = new Parcelable.Creator<FileInfoResponse>() {
        @Override
        public FileInfoResponse createFromParcel(Parcel in) {
            return new FileInfoResponse(in);
        }

        @Override
        public FileInfoResponse[] newArray(int size) {
            return new FileInfoResponse[size];
        }
    };

    public FileInfoResponse() {

    }

    public FileInfoResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private FileInfoResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        commonFileInfo = CommonFileInfo.CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (commonFileInfo != null)
            commonFileInfo.writeToParcel(dest, flags);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        sb.append(" path:" + commonFileInfo.path);
        sb.append(" mTime:" + commonFileInfo.mTime);
        sb.append(" cTime:" + commonFileInfo.cTime);
        sb.append(" blockList:" + commonFileInfo.blockList);
        sb.append(" size:" + commonFileInfo.size);
        sb.append(" isDir:" + commonFileInfo.isDir);
        sb.append(" hasSubFolder:" + commonFileInfo.hasSubFolder);
        sb.append(" fsId:" + commonFileInfo.fsId);
        sb.append(" requestId:" + commonFileInfo.requestId);
        return sb.toString();
    }
}