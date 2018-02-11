package com.baidu.xcloud.pluginAlbum.bean;

import java.util.ArrayList;
import java.util.List;

import com.baidu.xcloud.pluginAlbum.bean.AsyncTaskBaseBean;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * diff方法得到的类结构
 */
public class DiffResponse extends AsyncTaskBaseBean implements Parcelable {

    /** DifferEntryInfo类型的 list, 记录 diff方法得到的文件信息 */
    public List<DifferEntryInfo> entries = null;

    /** 是否有更多的信息 */
    public boolean hasMore = false;

    /** 是否是静态的 */
    public boolean isReseted = false;

    /** 记录当前的状态 */
    public String cursor = null;

    public static final Parcelable.Creator<DiffResponse> CREATOR = new Parcelable.Creator<DiffResponse>() {
        public DiffResponse createFromParcel(Parcel in) {
            return new DiffResponse(in);
        }

        public DiffResponse[] newArray(int size) {
            return new DiffResponse[size];
        }
    };

    public DiffResponse() {
        entries = new ArrayList<DifferEntryInfo>();
    }

    public DiffResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private DiffResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        hasMore = (in.readInt() == 1) ? true : false;
        isReseted = (in.readInt() == 1) ? true : false;
        cursor = in.readString();
        int size = in.readInt();
        if (size > 0) {
            entries = new ArrayList<DifferEntryInfo>(size);
            for (int i = 0; i < size; i++) {
                DifferEntryInfo entryInfo = new DifferEntryInfo(in);
                entries.add(entryInfo);
            }
        } else {
            if (entries == null) {
                entries = new ArrayList<DifferEntryInfo>();
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(hasMore ? 1 : 0);
        dest.writeInt(isReseted ? 1 : 0);
        dest.writeString(cursor);
        if (entries != null) {
            int size = entries.size();
            dest.writeInt(size);
            for (int i = 0; i < size; i++) {
                entries.get(i).writeToParcel(dest, flags);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        sb.append(" hasMore:" + hasMore);
        sb.append(" isReseted:" + isReseted);
        sb.append(" cursor:" + cursor);
        if (entries != null) {
            sb.append(" list size:" + entries.size());
            for (int i = 0; i < entries.size(); i++) {
                CommonFileInfo commonFileInfo = entries.get(i).commonFileInfo;
                sb.append(" index:" + i);
                sb.append(" isDeleted:" + entries.get(i).isDeleted);
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