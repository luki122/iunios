package com.android.gallery3d.local.widget;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * 
 * @author jiangxh
 * @Description  MediaFileInfo.java
 */
public class MediaFileInfo implements Parcelable {

	public String fileName;
	public String filePath;
	public long fileSize;
	public boolean IsDir;
	public int Count;
	public long createDate;
	public long dbId; // id in the database, if is from database
	public int orientation;// database orientation image
	public boolean isImage;
	public boolean favorite;
	public String firstPhotoPath;//wenyongzhe
	
	

	public MediaFileInfo() {
		super();
	}

	private MediaFileInfo(Parcel source) {
		fileName = source.readString();
		filePath = source.readString();
		fileSize = source.readLong();
		IsDir = (source.readInt()==0)?false:true;
		Count=source.readInt();
		createDate = source.readLong();
		dbId = source.readLong();
		orientation=source.readInt();
		isImage = (source.readInt()==0)?false:true;
		favorite = (source.readInt()==0)?false:true;
		firstPhotoPath = source.readString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaFileInfo other = (MediaFileInfo) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}
	

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<MediaFileInfo> CREATOR = new Creator<MediaFileInfo>() {

		@Override
		public MediaFileInfo[] newArray(int size) {
			return new MediaFileInfo[size];
		}

		@Override
		public MediaFileInfo createFromParcel(Parcel source) {
			return new MediaFileInfo(source);
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fileName);
		dest.writeString(filePath);
		dest.writeLong(fileSize);
		dest.writeInt(IsDir ? 1 : 0);
		dest.writeInt(Count);
		dest.writeLong(createDate);
		dest.writeLong(dbId);
		dest.writeInt(orientation);
		dest.writeInt(isImage ? 1 : 0);
		dest.writeInt(favorite ? 1 : 0);
		dest.writeString(firstPhotoPath);
	}

	@Override
	public String toString() {
		return "MediaFileInfo [fileName=" + fileName + ", filePath=" + filePath + ", fileSize=" + fileSize + ", IsDir=" + IsDir + ", Count=" + Count + ", createDate=" + createDate + ", dbId=" + dbId
				+ ", orientation=" + orientation + ", isImage=" + isImage + ", favorite=" + favorite + ", firstPhotoPath=" + firstPhotoPath + "]";
	}
	
	

}
