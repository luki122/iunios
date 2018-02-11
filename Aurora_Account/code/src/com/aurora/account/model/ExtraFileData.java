package com.aurora.account.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExtraFileData implements Parcelable {

	// 唯一标识：下载用downloadPath, 上传用uri(暂定)
	
	private int id;	// 附件ID号
	private String name;	// 附件文件名
	private String downloadPath;	// 附件下载地址
	private String uploadPath;		// 附件上传地址
	private String uri;		// 附件URI
	private String fileDir; // 文件存放目录
	private String fileName; // 文件名称
	
	// 以下字段只有在数据库查找时才会有
	private int status; // 状态
	private long finishTime; // 任务完成时间
	
	public ExtraFileData() {
		
	}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDownloadPath() {
		return downloadPath;
	}


	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}


	public String getUploadPath() {
		return uploadPath;
	}


	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}


	public String getUri() {
		return uri;
	}


	public void setUri(String uri) {
		this.uri = uri;
	}


	public String getFileDir() {
		return fileDir;
	}


	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public long getFinishTime() {
		return finishTime;
	}


	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	@Override
	public String toString() {
		return "ExtraFileData [id=" + id + ", name=" + name + ", downloadPath="
				+ downloadPath + ", uploadPath=" + uploadPath + ", uri=" + uri
				+ ", fileDir=" + fileDir + ", fileName=" + fileName
				+ ", status=" + status + ", finishTime=" + finishTime + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.name);
		dest.writeString(this.downloadPath);
		dest.writeString(this.uploadPath);
		dest.writeString(this.uri);
		
		dest.writeInt(this.status);
		dest.writeString(this.fileDir);
		dest.writeString(this.fileName);
		dest.writeLong(this.finishTime);
	}
	
	public static final Parcelable.Creator<ExtraFileData> CREATOR = new Creator<ExtraFileData>() {
		@Override
		public ExtraFileData createFromParcel(Parcel source) {
			return new ExtraFileData(source);
		}

		@Override
		public ExtraFileData[] newArray(int size) {
			return new ExtraFileData[size];
		}
	};
	
	public ExtraFileData(Parcel in) {
		id = in.readInt();
		name = in.readString();
		downloadPath = in.readString();
		uploadPath = in.readString();
		uri = in.readString();
		
		status = in.readInt();
		fileDir = in.readString();
		fileName = in.readString();
		finishTime = in.readLong();
	}
	
}
