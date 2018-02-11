/*
 * CloudStorageListener.java
 * 
 * Version:
 *
 * Date: 2013-5-16
 *
 * Changes:
 * [Date@Author]:Content
 * 
 * Copyright 2012-2013 Baidu. All Rights Reserved
 */

package com.baidu.xcloud.pluginAlbum;

import com.baidu.xcloud.pluginAlbum.bean.DiffResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.MetaResponse;
import com.baidu.xcloud.pluginAlbum.bean.QuotaResponse;
import com.baidu.xcloud.pluginAlbum.bean.SimpleResponse;
import com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse;

/**
 * 
 * @author zhaopeng05
 */
public interface IAlbumListener {

    public void onGetQuota(QuotaResponse quotaResponse);

    public void onDeleteFiles(SimpleResponse simplefiedResponse);

    public void onMakeDir(FileInfoResponse fileInfoResponse);

    public void onGetFileMeta(MetaResponse metaResponse);

    public void onGetFileList(ListInfoResponse listInfoResponse);

    public void onDiffWithCursor(DiffResponse diffResponse);

    public void onMoveFiles(FileFromToResponse fileFromToResponse);

    public void onThumbnail(ThumbnailResponse thumbnailResponse);

    public void onCopyFiles(FileFromToResponse fileFromToResponse);

    public void onRenameFiles(FileFromToResponse fileFromToResponse);

    public void onShare(FileLinkResponse fileLinkResponse);

    public void onCancelShare(FileLinkResponse fileLinkResponse);

}