package com.aurora.thememanager.utils.download;

public class DownloadAction {
	private DownloadAction(){}
	
	public static final String ACTION_DOWNLOAD_BROADCAST = "com.aurora.download.ACTION_DOWNLOAD_BROADCAST";
	
	public static class KEY{
		public static final String KEY_DOWNLOADED_DATA_ID = "downloaded_data_id";
	}

	
	public static class NET_TYPE{
		//wifi网络类型标志
		public static final int NETWORK_WIFI = 0;
		//2G网络类型标志
		public static final int NETWORK_2G = 1;
		//3G网络类型标志
		public static final int NETWORK_3G = 2;
	}
	
}
