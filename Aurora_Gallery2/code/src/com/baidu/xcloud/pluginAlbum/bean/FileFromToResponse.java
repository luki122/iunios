package com.baidu.xcloud.pluginAlbum.bean;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * move或copy方法得到的响应类结构
 */
public class FileFromToResponse extends AsyncTaskBaseBean implements Parcelable {

    /** FileFromToInfo类型的 list, 记录 copy或是list方法得到的文件信息 */
    public List<FileFromToInfo> list = null;

    public static final Parcelable.Creator<FileFromToResponse> CREATOR = new Parcelable.Creator<FileFromToResponse>() {
        public FileFromToResponse createFromParcel(Parcel in) {
            return new FileFromToResponse(in);
        }

        public FileFromToResponse[] newArray(int size) {
            return new FileFromToResponse[size];
        }
    };

    public FileFromToResponse() {
        list = new ArrayList<FileFromToInfo>();
    }

    public FileFromToResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    private FileFromToResponse(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        int size = in.readInt();
        if (size > 0) {
            list = new ArrayList<FileFromToInfo>(size);
            for (int i = 0; i < size; i++) {
                FileFromToInfo fileInfo = FileFromToInfo.CREATOR.createFromParcel(in);
                list.add(fileInfo);
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

    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(super.toString());
        if (list != null) {
            sb.append(" list size:" + list.size());
            for (int i = 0; i < list.size(); i++) {
                sb.append(" index:" + i);
                sb.append(" source:" + list.get(i).source);
                sb.append(" target:" + list.get(i).target);
            }
        }
        return sb.toString();
    }
}
