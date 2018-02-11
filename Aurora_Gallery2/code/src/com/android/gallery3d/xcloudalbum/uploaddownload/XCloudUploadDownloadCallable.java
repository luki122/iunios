package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.List;
import java.util.concurrent.Callable;

import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;


public class XCloudUploadDownloadCallable implements Callable<XCloundUploadDownloadTaskInfo>{

	public enum Type {
		TYPE_UPLOAD,
		TYPE_DOWNLOAD,
	};
	
	private Type mType;
	private AlbumClientProxy mAlbumClient;
	private XCloundUploadDownloadTaskInfo mTaskInfo;

	public XCloudUploadDownloadCallable(AlbumClientProxy albumClient, Type type, XCloundUploadDownloadTaskInfo taskInfo) {
		mType = type;
		mAlbumClient = albumClient;
		mTaskInfo = taskInfo;
	}
	
	@Override
	public XCloundUploadDownloadTaskInfo call() throws Exception {
		// TODO Auto-generated method stub
		if(mType == Type.TYPE_UPLOAD) {
			mAlbumClient.uploadPhotos(mTaskInfo.accountInfo, mTaskInfo.fileList, mTaskInfo.albumTaskListener);
		} else if(mType == Type.TYPE_DOWNLOAD) {
			mAlbumClient.downloadPhotos(mTaskInfo.accountInfo, mTaskInfo.fileList, mTaskInfo.albumTaskListener);
		}
		
		return mTaskInfo;
	}
	
	
}
