package com.baidu.xcloud.pluginAlbum.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * list方法得到的类结构
 */
public class ListInfoResponse extends AsyncTaskBaseBean implements Parcelable {

    /**
     * 类型：视频
     * 
     */
    public static final String TYPE_VIDEO = "video";
    /**
     * 类型：音频
     * 
     */
    public static final String TYPE_AUDIO = "audio";
    /**
     * 类型：图片
     * 
     */
    public static final String TYPE_IMAGE = "image";
    /**
     * 类型：文档
     * 
     */
    public static final String TYPE_DOC = "doc";
    /**
     * 类型：应用
     * 
     */
    public static final String TYPE_APP = "app";
    /**
     * 类型：其它
     * 
     */
    public static final String TYPE_OTHER = "other";

    /** CommonFileInfo类型的 list, 记录 list方法得到的文件信息 */
    public List<CommonFileInfo> list = null;

    public static final Parcelable.Creator<ListInfoResponse> CREATOR = new Parcelable.Creator<ListInfoResponse>() {
        @Override
        public ListInfoResponse createFromParcel(Parcel in) {
            return new ListInfoResponse(in);
        }

        @Override
        public ListInfoResponse[] newArray(int size) {
            return new ListInfoResponse[size];
        }
    };

    public ListInfoResponse() {
        list = new ArrayList<CommonFileInfo>();
    }

    public ListInfoResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private ListInfoResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        int size = in.readInt();
        if (size > 0) {
            list = new ArrayList<CommonFileInfo>(size);
            for (int i = 0; i < size; i++) {
                CommonFileInfo fileInfo = CommonFileInfo.CREATOR.createFromParcel(in);
                list.add(fileInfo);
            }
        } else {
            if (list == null) {
                list = new ArrayList<CommonFileInfo>();
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (list != null) {
            int size = list.size();
            dest.writeInt(size);
            for (int i = 0; i < size; i++) {
                list.get(i).writeToParcel(dest, flags);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        if (list != null) {
            sb.append(" list size:" + list.size());
            for (int i = 0; i < list.size(); i++) {
                CommonFileInfo commonFileInfo = list.get(i);
                sb.append(" index:" + i);
                sb.append(" path:" + commonFileInfo.path);
                sb.append(" mTime:" + commonFileInfo.mTime);
                sb.append(" cTime:" + commonFileInfo.cTime);
                sb.append(" blockList:" + commonFileInfo.blockList);
                sb.append(" size:" + commonFileInfo.size);
                sb.append(" isDir:" + commonFileInfo.isDir);
                sb.append(" hasSubFolder:" + commonFileInfo.hasSubFolder);
                sb.append(" fsId:" + commonFileInfo.fsId);
            }
        }
        return sb.toString();
    }
}
