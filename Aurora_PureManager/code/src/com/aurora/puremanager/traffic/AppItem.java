package com.aurora.puremanager.traffic;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

public class AppItem implements Parcelable {
    public final int key;
    public boolean restricted;
    public SparseBooleanArray uids = new SparseBooleanArray();
    public long total;

    public AppItem(int key) {
        this.key = key;
    }

    public AppItem(Parcel parcel) {
        key = parcel.readInt();
        uids = parcel.readSparseBooleanArray();
        total = parcel.readLong();
    }

    public void addUid(int uid) {
        uids.put(uid, true);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(key);
        dest.writeSparseBooleanArray(uids);
        dest.writeLong(total);
    }

    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel in) {
            return new AppItem(in);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };
}