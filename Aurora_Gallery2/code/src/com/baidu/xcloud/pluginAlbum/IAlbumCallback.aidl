package com.baidu.xcloud.pluginAlbum;

import java.util.List;

import com.baidu.xcloud.pluginAlbum.bean.FileFromToResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.FileLinkResponse;
import com.baidu.xcloud.pluginAlbum.bean.ListInfoResponse;
import com.baidu.xcloud.pluginAlbum.bean.MetaResponse;
import com.baidu.xcloud.pluginAlbum.bean.QuotaResponse;
import com.baidu.xcloud.pluginAlbum.bean.SimpleResponse;
import com.baidu.xcloud.pluginAlbum.bean.ThumbnailResponse;
import com.baidu.xcloud.pluginAlbum.bean.DiffResponse;

interface IAlbumCallback {

	/**
	 * 得到当前用户的的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @return QuotaResponse，由服务器返回得到。
	 */
	void onGetQuota(in QuotaResponse quotaResponse);

	/**
	 * 删除多个照片
	 * 
	 * @return SimpleResponse，服务器返回得到。
	 */
	void onDeletePhotos(in SimpleResponse simplefiedResponse);

	/**
	 * 创建一个新的照片文件夹
	 * 
	 * @return FileInfoResponse， 服务器返回得到。
	 */
	void onMakePhotoDir(in FileInfoResponse fileInfoResponse);

	/**
	 * 得到指定照片的元信息
	 * 
	 */
	void onGetPhotoMeta(in MetaResponse metaResponse);

	/**
	 * 得到指定目录下的照片列表。
	 * 
	 * @return ListInfoResponse, 服务器返回得到。
	 */
	void onGetPhotoList(in ListInfoResponse listInfoResponse);

	/**
	 * 移动多个照片到另一个目录中
	 * @return FileFromToResponse， 服务器返回得到。
	 */
	void onMovePhotos(in FileFromToResponse fileFromToResponse);

	/**
	 * 更改照片名
	 * @return FileFromToResponse，服务器返回得到。
	 */
	void onRenamePhotos(in FileFromToResponse fileFromToResponse);

	/**
	 * 复制多个照片到另一个目录下
	 * @return FileFromToResponse， 服务器返回得到。
	 */
	void onCopyPhotos(in FileFromToResponse fileFromToResponse);
	/**
	 * 生成缩略图
	 * @return ThumbnailResponse，服务器返回得到。
	 */
	void onThumbnail(in ThumbnailResponse thumbnailResponse);
	/**
     * 增量更新操作查询接口
     * 
     * @return DiffResponse， 服务器返回得到。
     */
    void onDiffWithCursor(in DiffResponse diffResponse);
	
	/**
	 * 分享照片(对指定的照片创建一个链接，该链接24小时内有效)
	 * @return FileLinkResponse, 服务器返回得到
	 */
	void onShare(in FileLinkResponse fileLinkResponse);
	
	/**
	 * 取消分享(取消照片的链接)
	 * @return FileLinkResponse, 服务器返回得到
	 */
	void onCancelShare(in FileLinkResponse fileLinkResponse);
	
	/**
	 * xcloud 框架错误
	 */
	void onXcloudError(in int errorCode);

}
