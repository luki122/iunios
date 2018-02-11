package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.List;

import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

public class XCloundUploadDownloadTaskInfo {
	public AccountInfo accountInfo;
	public List<FileUpDownloadInfo> fileList;
	public IAlbumTaskListener albumTaskListener;
}
