package com.aurora.market.model;

import java.io.Serializable;


import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DownloadData implements Parcelable{

	private int apkId; // 软件ID，可用在发送通知图标用
	private String apkName; // 软件名字，显示在UI上
	private String apkDownloadPath; // 下载地址
	private String versionName; // 版本，用于对比手机上的
	private int versionCode; // 版本码，用于对比手机上的
	private String packageName; // 包名，用于检查手机是否已安装
	private String apkLogoPath; // 图标位置

	// 以下字段只有在数据库查找时才会有
	private int status; // 状态
	private String fileDir; // 文件存放目录
	private String fileName; // 文件名称
	private long finishTime; // 任务完成时间

	public int getApkId() {
		return apkId;
	}

	public void setApkId(int apkId) {
		this.apkId = apkId;
	}

	public String getApkName() {
		return apkName;
	}

	public void setApkName(String apkName) {
		this.apkName = apkName;
	}

	public String getApkDownloadPath() {
		return apkDownloadPath;
	}

	public void setApkDownloadPath(String apkDownloadPath) {
		this.apkDownloadPath = apkDownloadPath;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getApkLogoPath() {
		return apkLogoPath;
	}

	public void setApkLogoPath(String apkLogoPath) {
		this.apkLogoPath = apkLogoPath;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}


	public  static final Parcelable.Creator<DownloadData> CREATOR = new Creator<DownloadData>() {  
        @Override  
        public DownloadData createFromParcel(Parcel source) {  
        	return new DownloadData(source);
        }  
        @Override  
        public DownloadData[] newArray(int size) {  
            return new DownloadData[size];  
        }  
    }; 
    
    public DownloadData() {
		
	}
	public DownloadData(Parcel in) {
		apkId = in.readInt();
		apkName = in.readString();
		apkDownloadPath = in.readString();
		versionName = in.readString();
		versionCode = in.readInt();
		packageName = in.readString();
		apkLogoPath = in.readString();
		
		status = in.readInt();
		fileDir = in.readString();
		fileName = in.readString();
		finishTime = in.readLong();
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(this.apkId);
		dest.writeString(this.apkName);
		dest.writeString(this.apkDownloadPath);
		dest.writeString(this.versionName);
		dest.writeInt(this.versionCode);
		dest.writeString(this.packageName);
		dest.writeString(this.apkLogoPath);
		
		dest.writeInt(this.status);
		dest.writeString(this.fileDir);
		dest.writeString(this.fileName);
		dest.writeLong(this.finishTime);
	}
	@Override
	public String toString() {
		return "DownloadData [apkId=" + apkId + ", apkName=" + apkName
				+ ", apkDownloadPath=" + apkDownloadPath + ", versionName="
				+ versionName + ", versionCode=" + versionCode
				+ ", packageName=" + packageName + ", apkLogoPath="
				+ apkLogoPath + ", status=" + status + ", fileDir=" + fileDir
				+ ", fileName=" + fileName + ", finishTime=" + finishTime + "]";
	}

}
