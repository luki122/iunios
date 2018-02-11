package com.aurora.downloader;

import java.io.Serializable;

import com.aurora.downloader.util.DownloadStateUtil.Status;


public class FileInfo implements Serializable {

	public String fileName;

	public String filePath;

	public long fileSize;

	public long ModifiedDate;

	public long dbId; // id in the database, if is from database
	
	public long downloadId;
	
	public boolean isExists;
	
	public boolean isCanShare;
	
	public boolean isDbExists;
	
	public Status status;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (downloadId ^ (downloadId >>> 32));
		return result;
	}

	/* (non-Javadoc)
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
		if (downloadId != other.downloadId)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [fileName=" + fileName + ", filePath=" + filePath
				+ ", fileSize=" + fileSize + ", ModifiedDate=" + ModifiedDate
				+ ", dbId=" + dbId + ", downloadId=" + downloadId
				+ ", isExists=" + isExists + ", isCanShare=" + isCanShare
				+ ", isDbExists=" + isDbExists + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (downloadId != other.downloadId)
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}

*/
	
	
	
}
