package com.baidu.xcloud.pluginAlbum.bean;

import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;

import android.os.Parcel;
import android.os.Parcelable;

/** meta方法得到的类结构 */
public class MetaResponse extends AsyncTaskBaseBean implements Parcelable {

    /**
     * 媒体文件类型
     */
    public static enum MediaType {

        /** 未知文件类型 */
        Media_Unknown,

        /** 音频文件类型 */
        Media_Audio,

        /** 视频文件类型 */
        Media_Video,

        /** 图像文件类型 */
        Media_Image,
    }

    /** 媒体文件类型 */
    public MediaType type = MediaType.Media_Unknown;

    /** 记录文件信息的类 */
    public CommonFileInfo commonFileInfo = new CommonFileInfo();

    public static final Parcelable.Creator<MetaResponse> CREATOR = new Parcelable.Creator<MetaResponse>() {
        public MetaResponse createFromParcel(Parcel in) {
            return new MetaResponse(in);
        }

        public MetaResponse[] newArray(int size) {
            return new MetaResponse[size];
        }
    };

    public MetaResponse() {

    }

    public MetaResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private MetaResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        int actualType = in.readInt();
        switch (actualType) {
            case 0:
                type = MediaType.Media_Unknown;
                break;
            case 1:
                type = MediaType.Media_Audio;
                break;
            case 2:
                type = MediaType.Media_Video;
                break;
            case 3:
                type = MediaType.Media_Image;
                break;
        }
        commonFileInfo = CommonFileInfo.CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        int actualType = 0;
        switch (type) {
            case Media_Unknown:
                actualType = 0;
                break;
            case Media_Audio:
                actualType = 1;
                break;
            case Media_Video:
                actualType = 2;
                break;
            case Media_Image:
                actualType = 3;
                break;
        }
        dest.writeInt(actualType);
        if (commonFileInfo != null)
            commonFileInfo.writeToParcel(dest, flags);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        sb.append(" type:" + type);
        sb.append(" path:" + commonFileInfo.path);
        sb.append(" mTime:" + commonFileInfo.mTime);
        sb.append(" cTime:" + commonFileInfo.cTime);
        sb.append(" blockList:" + commonFileInfo.blockList);
        sb.append(" size:" + commonFileInfo.size);
        sb.append(" isDir:" + commonFileInfo.isDir);
        sb.append(" hasSubFolder:" + commonFileInfo.hasSubFolder);
        sb.append(" fsId:" + commonFileInfo.fsId);
        return sb.toString();
    }
}