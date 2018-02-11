/*
 * AbstractCloudStorageListener.java
 * 
 * Version:
 *
 * Date: 2013-6-7
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
public class AbstractAlbumListener implements IAlbumListener {

    @Override
    public void onGetQuota(QuotaResponse quotaResponse) {

    }

    @Override
    public void onDeleteFiles(SimpleResponse simplefiedResponse) {

    }

    @Override
    public void onMakeDir(FileInfoResponse fileInfoResponse) {

    }

    @Override
    public void onGetFileMeta(MetaResponse metaResponse) {

    }

    @Override
    public void onGetFileList(ListInfoResponse listInfoResponse) {

    }

    @Override
    public void onMoveFiles(FileFromToResponse fileFromToResponse) {

    }

    @Override
    public void onThumbnail(ThumbnailResponse thumbnailResponse) {

    }

    @Override
    public void onCopyFiles(FileFromToResponse fileFromToResponse) {

    }

    @Override
    public void onRenameFiles(FileFromToResponse fileFromToResponse) {

    }

    @Override
    public void onShare(FileLinkResponse fileLinkResponse) {

    }

    @Override
    public void onCancelShare(FileLinkResponse fileLinkResponse) {

    }

    @Override
    public void onDiffWithCursor(DiffResponse diffResponse) {

    }

}
