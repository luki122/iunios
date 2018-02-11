package com.android.auroramusic.downloadex;


public interface DownloadStatusListener {
	public void onDownload(String url, long id, int status, long downloadSize, long fileSize);
}
