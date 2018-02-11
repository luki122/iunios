package com.aurora.tools;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * 
 * @author jiangxh
 * @CreateTime 2014年4月16日 上午10:46:19
 * @Description com.aurora.filemanager FileInfo.java
 */
public class FileInfo implements Parcelable {

	public String fileName;
	public String filePath;
	public long fileSize;
	public boolean IsDir;
	public int Count;
	public long ModifiedDate;
	public boolean Selected;
	public boolean canRead;
	public boolean canWrite;
	public boolean isHidden;
	public long dbId; // id in the database, if is from database
	public int orientation;// database orientation image
	public String order;// 排序号
	public boolean isLetterOrDigit;// 是否为特殊字符或者数字
	
	

	public FileInfo() {
		super();
	}

	private FileInfo(Parcel source) {
		fileName = source.readString();
		filePath = source.readString();
		order = source.readString();
		fileSize = source.readLong();
		ModifiedDate = source.readLong();
		dbId = source.readLong();
		orientation=source.readInt();
		Count=source.readInt();
		IsDir = (source.readInt()==0)?false:true;
		isHidden = (source.readInt()==0)?false:true;
		isLetterOrDigit = (source.readInt()==0)?false:true;
		canRead = (source.readInt()==0)?false:true;
		canWrite = (source.readInt()==0)?false:true;
		Selected = (source.readInt()==0)?false:true;
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
		FileInfo other = (FileInfo) obj;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [fileName=" + fileName + ", filePath=" + filePath
				+ ", fileSize=" + fileSize + ", IsDir=" + IsDir + ", Count="
				+ Count + ", ModifiedDate=" + ModifiedDate + ", Selected="
				+ Selected + ", canRead=" + canRead + ", canWrite=" + canWrite
				+ ", isHidden=" + isHidden + ", dbId=" + dbId + ", order="
				+ order + ", isLetterOrDigit=" + isLetterOrDigit + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<FileInfo> CREATOR = new Creator<FileInfo>() {

		@Override
		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}

		@Override
		public FileInfo createFromParcel(Parcel source) {
			return new FileInfo(source);
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fileName);
		dest.writeString(filePath);
		dest.writeString(order);
		dest.writeLong(fileSize);
		dest.writeLong(ModifiedDate);
		dest.writeLong(dbId);
		dest.writeInt(Count);
		dest.writeInt(IsDir ? 1 : 0);
		dest.writeInt(Selected ? 1 : 0);
		dest.writeInt(canRead ? 1 : 0);
		dest.writeInt(canWrite ? 1 : 0);
		dest.writeInt(isHidden ? 1 : 0);
		dest.writeInt(isLetterOrDigit ? 1 : 0);
		dest.writeInt(orientation);

	}

}
