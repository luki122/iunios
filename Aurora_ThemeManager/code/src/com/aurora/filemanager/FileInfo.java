package com.aurora.filemanager;

import java.io.Serializable;

/**
 * 跟壁纸相关联	2014-07-17 废弃使用for文管提供接口  modified by liugj
 * 
 * @author jiangxh
 * @CreateTime 2014年4月16日 上午10:46:19
 * @Description com.aurora.filemanager FileInfo.java
 */
public class FileInfo implements Serializable {

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
	
	public int orientation;//database orientation image

	public String order;// 排序号
	public boolean isLetterOrDigit;// 是否为特殊字符或者数字

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (IsDir ? 1231 : 1237);
		result = prime * result + (int) (ModifiedDate ^ (ModifiedDate >>> 32));
		result = prime * result + (int) (dbId ^ (dbId >>> 32));
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result + (isLetterOrDigit ? 1231 : 1237);
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (IsDir != other.IsDir)
			return false;
		if (ModifiedDate != other.ModifiedDate)
			return false;
		if (dbId != other.dbId)
			return false;
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
		if (fileSize != other.fileSize)
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

}
