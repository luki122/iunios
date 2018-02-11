package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 通用的记录文件信息的类
 */
public class CommonFileInfo extends AsyncTaskBaseBean implements Parcelable {

    /** 文件的路径 */
    public String path = "";

    /** 文件的服务器修改时间,以毫秒表示 */
    public long mTime = 0L;

    /** 文件的服务器创建时间，以毫秒表示 */
    public long cTime = 0L;

    /** 文件或是目录的Md5值 */
    public String blockList = "";

    /** 文件或是目录的大小 */
    public long size = -1;

    /** 请求id */
    public String requestId = "";

    /**
     * 当前路径是否是目录， 该参数在一些方法中没有意义
     */
    public boolean isDir = false;

    /** 该路径下是否包括有子目录 */
    public boolean hasSubFolder = false;

    /** 文件在PCS的临时唯一标识id */
    public long fsId = 0L;

    public static final Parcelable.Creator<CommonFileInfo> CREATOR = new Parcelable.Creator<CommonFileInfo>() {
        @Override
        public CommonFileInfo createFromParcel(Parcel in) {
            return new CommonFileInfo(in);
        }

        @Override
        public CommonFileInfo[] newArray(int size) {
            return new CommonFileInfo[size];
        }
    };

    public CommonFileInfo() {

    }

    private CommonFileInfo(Parcel in) {
        readFromParcel(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 从parcel里读取属性值
     * 
     * @param in
     */
    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        path = in.readString();
        mTime = in.readLong();
        cTime = in.readLong();
        blockList = in.readString();
        size = in.readLong();
        isDir = (in.readInt() == 1) ? true : false;
        hasSubFolder = (in.readInt() == 1) ? true : false;
        fsId = in.readLong();
        requestId = in.readString();
    }

    /**
     * 将属性写入parcel
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(path);
        dest.writeLong(mTime);
        dest.writeLong(cTime);
        dest.writeString(blockList);
        dest.writeLong(size);
        dest.writeInt(isDir ? 1 : 0);
        dest.writeInt(hasSubFolder ? 1 : 0);
        dest.writeLong(fsId);
        dest.writeString(requestId);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append(super.toString());
        sb.append("path:" + path);
        sb.append(" mTime:" + mTime);
        sb.append(" cTime:" + cTime);
        sb.append(" blockList:" + blockList);
        sb.append(" size:" + size);
        sb.append(" isDir:" + isDir);
        sb.append(" hasSubFolder:" + hasSubFolder);
        sb.append(" fsId:" + fsId);
        sb.append(" requestId:" + requestId);
        return super.toString();
    }
}