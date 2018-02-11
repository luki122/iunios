package com.baidu.xcloud.pluginAlbum;

import java.util.List;
import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileFromToInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.baidu.xcloud.pluginAlbum.IAlbumCallback;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskCallback;
import com.baidu.xcloud.pluginAlbum.IAlbumSetupCallback;

interface IAlbum {

	/**
	 * 初始化相关参数。
	 * 
	 * @param accountInfo  当前账户
	 * @param bundleConfig 相关设置参数的Bundle，具体参照Demo
     * @param albumSetupCallback  将在albumSetupCallback 接口中异步回调setupFileDescriptor方法
	 * @return
	 */
	void setupWithConfig(in AccountInfo accountInfo, in Bundle bundleConfig,in IAlbumSetupCallback albumSetupCallback);
	
	/**
	 * 得到当前用户的空间配额信息，包括共空间和已用空字，由字节表示。
	 * 
	 * @param accountInfo     当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetQuota返回
	 * @return void 
	 */
	void getQuota(in AccountInfo accountInfo,in IAlbumCallback iAlbumCallback);

	/**
	 * 删除照片
	 * 
	 * @param photos 照片列表
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onDeletePhotos
	 * @return void 
	 */
	void deletePhotos(in AccountInfo accountInfo, in List<String> photos,in IAlbumCallback iAlbumCallback);

	/**
	 * 移动照片
	 * 
	 * @param info 将要移动的照片列表。
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onMovePhotos
	 * @return void 
	 */
	void movePhotos(in AccountInfo accountInfo, in List<FileFromToInfo> info,in IAlbumCallback iAlbumCallback);
	
	/**
	 * 创建一个新照片文件夹
	 * 
	 * @param path 要创建的文件夹的路径
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onMakePhotoDir
	 * @return void 
	 */
	void makePhotoDir(in AccountInfo accountInfo, in  String path, in IAlbumCallback iAlbumCallback);

	/**
	 * 得到指定照片的元信息
	 * 
	 * @param photoPath 要得到元信息的照片路径	 
	 * @param showDirSize  showDirSize
	 * @param accountInfo  当前账户
     * @param iAlbumCallback  将在iAlbumCallback 接口中异步回调onGetPhotoMeta
	 * @return void 
	 */
	void getPhotoMeta(in AccountInfo accountInfo, in String photoPath, boolean showDirSize,in IAlbumCallback iAlbumCallback);

	/**
	 * 得到照片列表。
	 * @param path 路径
	 * @param by 指定返回列表的排序方式，参数值有：time, name 或 size.
	 * @param order 指定返回列表的排序方式，参数值有：asc 或 desc.
	 * @param accountInfo  当前账户
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
	 * @return void 
	 */
	void getPhotoList(in AccountInfo accountInfo, in String path,in String by,in String order, in IAlbumCallback iAlbumCallback);
	
	/**
     * 得到帐号下所有流文件(照片,音视频)
     * @param accountInfo  当前账户
     * @param type 流文件类型(video,audio,image)
     * @param start 缺省为0
     * @param limit 缺省为1000,可配置.
     * @param iAlbumCallback   将在iAlbumCallback 接口中异步回调onGetPhotoList
     * @return void 
     */
    void getStreamFileList(in AccountInfo accountInfo, in String type, in int start, in int limit, in IAlbumCallback iAlbumCallback);
	
	/**
	 * 更改多个照片名
	 * 
	 * @param info 将要更改照片名的列表。
	 * @return void
	 */
	void renamePhotos(in AccountInfo accountInfo,in List<FileFromToInfo> info, in IAlbumCallback iAlbumCallback);
	
	
	/**
	 * 复制多个照片到另一个目录下
	 * 
	 * @param info 要复制的照片列表
	 * @return void
	 */
	void copyPhotos(in AccountInfo accountInfo,in List<FileFromToInfo> info, in IAlbumCallback iAlbumCallback);


	/**
	 * 生成一个缩略图
	 * 
	 * @param path    原图像的路径
	 * @param quality 缩略图的质量，(0,100]。
	 * @param width   缩略图的宽，最大值为850。
	 * @param height  缩略图的高，最大值为580。
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onThumbnail
	 * @return void 
	 */
	void thumbnail(in AccountInfo accountInfo, in String path, int quality,int width, int height, in IAlbumCallback iAlbumCallback);

	/**
     * 文件增量更新操作查询接口
     * 
     * @param cursor
     *            用于标记更新断点。首次调用cursor=null；非首次调用，使用最后一次调用diff接口的返回结果中的cursor
     * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onDiffWithCursor
     * @return void 
     */
    void diffWithCursor(in AccountInfo accountInfo, in String cursor, in IAlbumCallback iAlbumCallback);
	/**
	 * 对指定的照片创建一个链接进行分享,该链接24小时内有效
	 * 
	 * @param path 要分享的路径	 
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onShare
     * @return void 
	 */
	void shareLink(in AccountInfo accountInfo, in String path,in IAlbumCallback iAlbumCallback);
	/**
	 * 取消某一照片的链接
	 * 
	 * @param file 要取消的路径
	 * @param accountInfo  当前账户
     * @param IAlbumCallback  将在IAlbumCallback 接口中异步回调onCancelShare
     * @return void 
	 */
	void cancelShare(in AccountInfo accountInfo, in String path, in IAlbumCallback iAlbumCallback);
	/**
	 * 上传照片
	 * 
	 * @param photoList  要上传的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 * 
     * @return void 
	 */
	void uploadPhotos(in AccountInfo accountInfo, in List<FileUpDownloadInfo> photoList,in IAlbumTaskCallback taskCallback);
	
	/**
	 * 下载照片
	 * 
	 * @param photoList  要下载的照片列表
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
	 */
	void downloadPhotos(in AccountInfo accountInfo, in List<FileUpDownloadInfo> photoList,in IAlbumTaskCallback taskCallback);
    
    /**
     * 对视频文件进行转码，实现实时观看视频功能
     * (为当前用户下载一个视频转码后的m3u8文件)<br>
     * 下载一个指定编码类型的文件，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param type
     *            仅限 M3U8_320_240、M3U8_480_224、M3U8_480_360、
     *            M3U8_640_480和M3U8_854_480, MP4_480P, MP4_360P
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
    void downloadFileAsSpecificType(in AccountInfo accountInfo, in List<FileUpDownloadInfo> fileList, in String type,
            in IAlbumTaskCallback taskCallback);
    /**
     * 以流的方式下载一个文件到本地目录，下载过程中listener监听下载进度。 您需要申请特别的权限才能使用该方法，请联系
     * bd，或发邮件到dev_support@baidu.com申请权限。
     * 
     * @param fileList  要下载的文件列表
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
     * @return void 
     */
    void downloadFileFromStream(in AccountInfo accountInfo, in List<FileUpDownloadInfo> fileList, in IAlbumTaskCallback taskCallback);
	/**
	 * 暂停、继续、删除单个照片上传/下载任务
	 * 
	 * @param type 任务处理类型,参数有：pause,resume,remove
	 * @param taskId  文件任务ID
     * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
	void processPhotoTask(in AccountInfo accountInfo, in String type, long taskId,in IAlbumTaskCallback taskCallback);

	/**
	 * 暂停、继续、删除所有上传（download）、下载（download）或所有（all）任务
	 * 
     * @param accountInfo  当前账户
	 * @param type  type 任务处理类型,参数有：pause,resume,remove 
	 * @param fileType 要处理的文件任务类型,参数有：all,download,upload
     * @param taskCallback  将在taskCallback 返回进度信息及任务状态信息
	 */
	void processPhotoTaskList(in AccountInfo accountInfo, in String type,in String fileType,in IAlbumTaskCallback taskCallback);

	/**
	 * 获取当前账号的任务列表     
	 * @param accountInfo  当前账户
     * @param taskCallback  将在taskCallback 返回文件任务列表
	 */
	void getPhotoTaskList(in AccountInfo accountInfo,in IAlbumTaskCallback taskCallback);
}
