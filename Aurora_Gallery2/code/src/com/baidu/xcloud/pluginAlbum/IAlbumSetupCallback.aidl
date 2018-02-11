package com.baidu.xcloud.pluginAlbum;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

/**
 * ICloudStorage 的初始化回调接口
 * 
 */
interface IAlbumSetupCallback {

	/**
	 * 设置FileDescriptor，解决跨进程文件访问权限问题
	 */
	 void setupFileDescriptor(inout List<FileUpDownloadInfo> fileUpDownloadInfoList);

	 /**
	  * xcloud 框架错误
	  */
	 void onXcloudError(in int errorCode);	 
}
