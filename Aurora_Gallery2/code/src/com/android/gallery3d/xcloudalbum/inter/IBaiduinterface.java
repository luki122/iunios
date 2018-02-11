package com.android.gallery3d.xcloudalbum.inter;

import java.util.List;

import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public interface IBaiduinterface {
	void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath,
			CommonFileInfo info);

	void loginComplete(boolean success);
}
