package com.aurora.market.model;

public class DownloadManagerItem {

	private DownloadData downloadData;
	private String filePath;
	private long fileSize;
	private long downloadSize;
	private int progress;
	private int downloadStatus;
	private String progressInfo = "";

	public DownloadData getDownloadData() {
		return downloadData;
	}

	public void setDownloadData(DownloadData downloadData) {
		this.downloadData = downloadData;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public void setDownloadSize(long downloadSize) {
		this.downloadSize = downloadSize;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(int downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getProgressInfo() {
		return progressInfo;
	}

	public void setProgressInfo(String progressInfo) {
		this.progressInfo = progressInfo;
	}

}
