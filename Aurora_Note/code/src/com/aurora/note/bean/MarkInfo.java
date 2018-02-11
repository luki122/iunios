
package com.aurora.note.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 波形图里的标记信息
 * @author JimXia
 * 2014-7-22 下午4:17:25
 */
public class MarkInfo implements Parcelable {
    private float mDisplayPositionX;
    private int mIndex;
    private float mLockScreenDisplayPositionX;
    private long mMarkElapsedTime;

    public MarkInfo(long markElapsedTime) {
        this.mMarkElapsedTime = markElapsedTime;
    }
    
    public MarkInfo(int index, long markElapsedTime) {
        this.mIndex = index;
        this.mMarkElapsedTime = markElapsedTime;
    }

    private MarkInfo(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.mIndex = in.readInt();
        this.mMarkElapsedTime = in.readLong();
        this.mDisplayPositionX = in.readFloat();
        this.mLockScreenDisplayPositionX = in.readFloat();
    }

    public int describeContents() {
        return 0;
    }

    public float getDisplayPositionX() {
        return this.mDisplayPositionX;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public long getMarkElpasedTime() {
        return this.mMarkElapsedTime;
    }

    public void setDisplayPositionX(float displayPositionX) {
        this.mDisplayPositionX = displayPositionX;
    }

    public void setLockScreenDisplayPositionX(float displayPositionX) {
        this.mLockScreenDisplayPositionX = displayPositionX;
    }

    public String toString() {
        return this.mMarkElapsedTime + "";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mIndex);
        dest.writeLong(this.mMarkElapsedTime);
        dest.writeFloat(this.mDisplayPositionX);
        dest.writeFloat(this.mLockScreenDisplayPositionX);
    }

    public static final Parcelable.Creator<MarkInfo> CREATOR = new Parcelable.Creator<MarkInfo>() {
        public MarkInfo createFromParcel(Parcel in) {
            return new MarkInfo(in);
        }

        public MarkInfo[] newArray(int size) {
            return new MarkInfo[size];
        }
    };
}
