package com.aurora.account.bean;

public class DownloadDataResult {

	private String syncId; // 同步id
	private String path;
	private String package_name; // 属于哪个package
	private String downUrl;		// 下载地址
	private int status; // 下载状态
	private long create_time;
	private long finish_time;
	private long file_size;
	private long downloadSize;

	public String getSyncId() {
		return syncId;
	}

	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(long finish_time) {
		this.finish_time = finish_time;
	}

	public long getFile_size() {
		return file_size;
	}

	public void setFile_size(long file_size) {
		this.file_size = file_size;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public void setDownloadSize(long downloadSize) {
		this.downloadSize = downloadSize;
	}

}
