package com.baidu.xcloud.pluginAlbum.bean;

import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * diff方法得到的类结构中用到的类结构
 */
public class DifferEntryInfo extends AsyncTaskBaseBean implements Parcelable {

    /** 记录文件信息的类 */
    public CommonFileInfo commonFileInfo = new CommonFileInfo();

    /** 文件是否被删除 */
    public boolean isDeleted = false;

    public static final Parcelable.Creator<DifferEntryInfo> CREATOR = new Parcelable.Creator<DifferEntryInfo>() {
        public DifferEntryInfo createFromParcel(Parcel in) {
            return new DifferEntryInfo(in);
        }

        public DifferEntryInfo[] newArray(int size) {
            return new DifferEntryInfo[size];
        }
    };

    public DifferEntryInfo() {

    }

    public DifferEntryInfo(Parcel in) {
        readFromParcel(in);
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

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        isDeleted = (in.readInt() == 1) ? true : false;
        commonFileInfo = CommonFileInfo.CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(isDeleted ? 1 : 0);
        if (commonFileInfo != null)
            commonFileInfo.writeToParcel(dest, flags);
    }
}
