package com.aurora.account.bean;

public class UploadDataResult {
	private String accessoryid; // 附件id
	private String id; // 客户端模块一条数据的id标示
	private String package_name; // 属于哪个package
	private String file_path;
	private int status; // 下载状态
	private String create_time;
	private String finish_time;
	private String file_size;
	private String upload_size;

	public String getAccessoryid() {
		return accessoryid;
	}

	public void setAccessoryid(String accessoryid) {
		this.accessoryid = accessoryid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}



	public String getFile_path() {
		return file_path;
	}

	public void setFile_path(String file_path) {
		this.file_path = file_path;
	}



	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(String finish_time) {
		this.finish_time = finish_time;
	}

	public String getFile_size() {
		return file_size;
	}

	public void setFile_size(String file_size) {
		this.file_size = file_size;
	}

	public String getUpload_size() {
		return upload_size;
	}

	public void setUpload_size(String upload_size) {
		this.upload_size = upload_size;
	}

}
