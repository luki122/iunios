package com.android.gallery3d.xcloudalbum.inter;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public interface IBaiduTaskListener {
	void baiduTaskStatus(FileTaskStatusBean bean);

	void baiduDownloadTaskStatus(FileTaskStatusBean bean);
	
	void baiduUploadTaskStatus(FileTaskStatusBean bean);

}
