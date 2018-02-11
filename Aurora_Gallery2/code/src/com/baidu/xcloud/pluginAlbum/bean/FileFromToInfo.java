package com.baidu.xcloud.pluginAlbum.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 该类在调用move或copy方法时作为参数
 */
public class FileFromToInfo extends AsyncTaskBaseBean implements Parcelable {

    /** 源文件路径 */
    protected String source = null;

    /** 目标文件路径 */
    protected String target = null;

    /**
     * 构造函数
     * 
     * @param source 源文件
     * @param target 目标文件
     */
    public FileFromToInfo(String source, String target) {
        super();
        this.source = source;
        this.target = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Parcelable.Creator<FileFromToInfo> CREATOR = new Parcelable.Creator<FileFromToInfo>() {
        public FileFromToInfo createFromParcel(Parcel in) {
            return new FileFromToInfo(in);
        }

        public FileFromToInfo[] newArray(int size) {
            return new FileFromToInfo[size];
        }
    };

    public FileFromToInfo() {

    }

    private FileFromToInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        source = in.readString();
        target = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(source);
        dest.writeString(target);
    }

    /**
     * @return 获取源文件路径
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置源文件路径
     * 
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return 获取目标文件路径
     */
    public String getTarget() {
        return target;
    }

    /**
     * 设置目标文件路径
     * 
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    public String toString() {
        return new StringBuffer(1024).append(" source:").append(source).append(" target:").append(target).toString();
    }
}
